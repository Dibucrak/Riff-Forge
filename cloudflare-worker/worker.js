const HARMONY_MODEL_ID = "@cf/google/gemma-4-26b-a4b-it";
const HARMONY_FALLBACK_MODEL_ID = "@cf/openai/gpt-oss-120b";
const FORMATTER_MODEL_ID = "@cf/meta/llama-3.3-70b-instruct-fp8-fast";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Accept",
};

const CHORD_TOKEN_RE =
  /^[A-G](?:#|b)?(?:(?:maj|min|dim|aug|sus|add|alt|dom|m|M)?(?:\d+)?(?:sus[24]|add\d+|b\d|#\d|[+-])?){0,3}(?:\/[A-G](?:#|b)?)?$/;

const SHARP_NOTES = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];
const FLAT_NOTES = ["C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"];
const NOTE_TO_SEMITONE = {
  C: 0, "B#": 0, "C#": 1, Db: 1, D: 2, "D#": 3, Eb: 3, E: 4, Fb: 4,
  F: 5, "E#": 5, "F#": 6, Gb: 6, G: 7, "G#": 8, Ab: 8, A: 9, "A#": 10, Bb: 10, B: 11, Cb: 11,
};
const OPEN_EASY_CHORDS = new Set(["C", "G", "D", "A", "E", "Am", "Em", "Dm"]);

function jsonResponse(payload, status = 200) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      ...CORS_HEADERS,
    },
  });
}

function extractYouTubeVideoId(rawInput) {
  const text = String(rawInput || "").trim();
  const patterns = [
    /(?:youtube\.com\/watch\?(?:[^#&\s]*&)?v=|music\.youtube\.com\/watch\?(?:[^#&\s]*&)?v=)([A-Za-z0-9_-]{11})/i,
    /youtu\.be\/([A-Za-z0-9_-]{11})/i,
    /youtube\.com\/(?:shorts|embed|live)\/([A-Za-z0-9_-]{11})/i,
  ];
  for (const re of patterns) {
    const match = text.match(re);
    if (match && match[1]) {
      return match[1];
    }
  }
  return null;
}

function looksLikeUrl(rawInput) {
  return /^https?:\/\//i.test(rawInput) || /youtu(\.be|be\.com)/i.test(rawInput);
}

function cleanTrackText(text) {
  let cleaned = String(text || "");
  cleaned = cleaned.replace(/\|.*$/g, "");
  cleaned = cleaned.replace(
    /\((?:official\s*(?:music\s*)?(?:video|audio|lyric\s*video|visualizer)|lyrics?|letra|audio|hd|4k|remastered[^)]*|live[^)]*|en\s+vivo[^)]*|video\s+oficial)\)/gi,
    " "
  );
  cleaned = cleaned.replace(
    /\[(?:official\s*(?:music\s*)?(?:video|audio|lyric\s*video|visualizer)|lyrics?|letra|audio|hd|4k|remastered[^\]]*|live[^\]]*|en\s+vivo[^\]]*|video\s+oficial)\]/gi,
    " "
  );
  cleaned = cleaned.replace(/\b(?:VEVO|Official)\b/gi, " ");
  cleaned = cleaned.replace(/\s*-\s*Topic\b/gi, "");
  cleaned = cleaned.replace(/[\u{1F300}-\u{1FAFF}\u{2600}-\u{27BF}]/gu, " ");
  return cleaned.replace(/\s+/g, " ").trim();
}

function trimLyricsForPrompt(rawLyrics, maxChars = 1500) {
  const normalized = String(rawLyrics || "")
    .replace(/\r\n/g, "\n")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
  if (!normalized || normalized.length <= maxChars) {
    return normalized;
  }
  const slice = normalized.slice(0, maxChars);
  const lastParagraphBreak = slice.lastIndexOf("\n\n");
  if (lastParagraphBreak >= Math.floor(maxChars * 0.5)) {
    return slice.slice(0, lastParagraphBreak).trim();
  }
  const lastLineBreak = slice.lastIndexOf("\n");
  if (lastLineBreak >= Math.floor(maxChars * 0.5)) {
    return slice.slice(0, lastLineBreak).trim();
  }
  return slice.trim();
}

async function fetchRealLyrics(videoTitle, channelAuthor, rawInput) {
  const cleanTitle = cleanTrackText(videoTitle);
  const cleanAuthor = cleanTrackText(channelAuthor);
  const cleanRaw = looksLikeUrl(rawInput) ? "" : cleanTrackText(rawInput);

  const queries = [];
  if (cleanTitle) {
    if (cleanAuthor && !cleanTitle.toLowerCase().includes(cleanAuthor.toLowerCase())) {
      queries.push(`${cleanAuthor} ${cleanTitle}`);
    }
    queries.push(cleanTitle);
  }
  if (cleanRaw && !queries.includes(cleanRaw)) {
    queries.push(cleanRaw);
  }

  const titleWords = (cleanTitle || cleanRaw)
    .toLowerCase()
    .split(/[^a-z0-9áéíóúñü]+/i)
    .filter((w) => w.length >= 3);

  for (const query of queries.slice(0, 2)) {
    try {
      const searchUrl = `https://lrclib.net/api/search?q=${encodeURIComponent(query)}`;
      const resp = await fetch(searchUrl, {
        headers: {
          "User-Agent": "RiffForge-AI/1.2 (+https://github.com/Dibucrak/Riff-Forge)",
          Accept: "application/json",
        },
        signal: AbortSignal.timeout(4500),
      });
      if (!resp.ok) continue;
      const results = await resp.json();
      if (!Array.isArray(results) || results.length === 0) continue;

      const candidates = results.filter(
        (item) =>
          item &&
          item.instrumental !== true &&
          typeof item.plainLyrics === "string" &&
          item.plainLyrics.trim().length > 20
      );
      if (candidates.length === 0) continue;

      let best = candidates[0];
      if (titleWords.length > 0) {
        let bestScore = -1;
        for (const item of candidates) {
          const hay = `${item.trackName || ""} ${item.artistName || ""} ${item.name || ""}`.toLowerCase();
          let score = 0;
          for (const word of titleWords) {
            if (hay.includes(word)) score += 1;
          }
          if (score > bestScore) {
            bestScore = score;
            best = item;
          }
        }
      }

      const trimmed = trimLyricsForPrompt(best.plainLyrics, 1500);
      if (trimmed) {
        return trimmed;
      }
    } catch {
      // Silently fallback if LRCLIB times out
    }
  }
  return "";
}

function normalizeKey(rawKey) {
  const s = String(rawKey || "").trim();
  if (!s) return "C";
  const m = s.match(/^([A-G](?:#|b)?)\s*(minor|min|menor|m|major|maj|mayor)?/i);
  if (!m) return s.slice(0, 20);
  const root = m[1];
  const qual = (m[2] || "").toLowerCase();
  if (qual === "minor" || qual === "min" || qual === "menor" || qual === "m") {
    return `${root}m`;
  }
  return root;
}

function isChordList(inner) {
  const tokens = String(inner || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  return tokens.length > 0 && tokens.every((t) => CHORD_TOKEN_RE.test(t));
}

function stripComplexExtensions(chord) {
  const clean = String(chord || "").trim();
  const baseOnly = clean.split("/")[0];
  const m = baseOnly.match(/^([A-G](?:#|b)?)(.*)$/);
  if (!m) return clean;
  const root = m[1];
  const rest = m[2] || "";
  const isMinor = /^m(?!aj)/i.test(rest) || /^min/i.test(rest);
  const isDim = /^dim/i.test(rest);
  if (isDim) return `${root}m`;
  return isMinor ? `${root}m` : root;
}

function transposeSimpleChord(chord, semitones) {
  const m = String(chord || "").trim().match(/^([A-G](?:#|b)?)(m?)$/);
  if (!m) return chord;
  const idx = NOTE_TO_SEMITONE[m[1]];
  if (idx === undefined) return chord;
  const target = ((idx + semitones) % 12 + 12) % 12;
  const preferFlats = target === 10 || target === 3;
  const noteName = preferFlats ? FLAT_NOTES[target] : SHARP_NOTES[target];
  return `${noteName}${m[2]}`;
}

function chordDifficultyPenalty(chord) {
  if (OPEN_EASY_CHORDS.has(chord)) return 0;
  if (chord === "F") return 0.5;
  return 1.5;
}

function replaceChordsInProgression(text, chordMap) {
  const s = String(text || "");
  if (!s) return "";
  return s.replace(
    /(?<![A-Za-z])([A-G](?:#|b)?(?:(?:maj|min|dim|aug|sus|add|alt|dom|m|M)?(?:\d+)?(?:sus[24]|add\d+|b\d|#\d|[+-])?){0,3}(?:\/[A-G](?:#|b)?)?)(?![A-Za-z0-9#b])/g,
    (token) => chordMap.get(token) || token
  );
}

function simplifyBlueprint(blueprint, practiceTip, language) {
  const uniqueOriginal = Array.from(new Set(blueprint.chords));
  const stripped = uniqueOriginal.map((c) => stripComplexExtensions(c));

  const basePenalty = stripped.reduce((acc, c) => acc + chordDifficultyPenalty(c), 0);
  let bestShift = 0;
  let bestPenalty = basePenalty;

  for (let shift = 1; shift <= 7; shift++) {
    if (blueprint.capo + shift > 9) break;
    const shifted = stripped.map((c) => transposeSimpleChord(c, -shift));
    const pen = shifted.reduce((acc, c) => acc + chordDifficultyPenalty(c), 0);
    if (pen + 0.75 <= bestPenalty) {
      bestPenalty = pen;
      bestShift = shift;
    }
  }

  const chordMap = new Map();
  for (const orig of uniqueOriginal) {
    const simple = stripComplexExtensions(orig);
    const finalChord = bestShift > 0 ? transposeSimpleChord(simple, -bestShift) : simple;
    chordMap.set(orig, finalChord);
  }

  const simplifiedChords = Array.from(new Set(blueprint.chords.map((c) => chordMap.get(c) || c)));
  const newCapo = blueprint.capo + bestShift;
  const newSections = {};
  for (const [k, v] of Object.entries(blueprint.sections || {})) {
    newSections[k] = replaceChordsInProgression(v, chordMap);
  }

  const capoNote =
    language === "es"
      ? bestShift > 0
        ? ` Versión simplificada con capo en traste ${newCapo} (${simplifiedChords.slice(0, 5).join(" - ")}) para evitar cejillas manteniendo el tono original.`
        : ` Versión simplificada con acordes abiertos (${simplifiedChords.slice(0, 5).join(" - ")}).`
      : bestShift > 0
      ? ` Simplified with capo on fret ${newCapo} (${simplifiedChords.slice(0, 5).join(" - ")}) to avoid barre chords in the original key.`
      : ` Simplified using open chords (${simplifiedChords.slice(0, 5).join(" - ")}).`;

  return {
    blueprint: {
      ...blueprint,
      capo: newCapo,
      chords: simplifiedChords,
      sections: newSections,
    },
    practiceTip: `${practiceTip}${capoNote}`.trim(),
    difficulty: language === "es" ? "Fácil" : "Easy",
  };
}

function wrapOverLyricPair(chordLine, lyricLine, maxWidth = 44) {
  const out = [];
  let cRest = String(chordLine || "").trimEnd();
  let lRest = String(lyricLine || "").trimEnd();

  while (cRest.length > maxWidth || lRest.length > maxWidth) {
    let splitIdx = -1;
    const limit = Math.min(maxWidth, Math.max(cRest.length, lRest.length));

    for (let i = limit; i >= Math.floor(maxWidth * 0.4); i--) {
      const lOk = i >= lRest.length || lRest[i] === " ";
      const cOk = i >= cRest.length || cRest[i] === " ";
      if (lOk && cOk) {
        splitIdx = i;
        break;
      }
    }

    if (splitIdx <= 0) {
      for (let i = limit; i >= Math.floor(maxWidth * 0.35); i--) {
        const cChar = i < cRest.length ? cRest[i] : " ";
        const cPrev = i > 0 && i - 1 < cRest.length ? cRest[i - 1] : " ";
        if (cChar === " " && cPrev === " ") {
          splitIdx = i;
          break;
        }
      }
    }

    if (splitIdx <= 0) {
      splitIdx = maxWidth;
    }

    const cHead = cRest.slice(0, splitIdx).trimEnd();
    const lHead = lRest.slice(0, splitIdx).trimEnd();
    if (cHead) out.push(cHead);
    if (lHead) out.push(lHead);

    cRest = cRest.slice(splitIdx).replace(/^ +/, "");
    lRest = lRest.slice(splitIdx).replace(/^ +/, "");
  }

  if (cRest.trim()) out.push(cRest.trimEnd());
  if (lRest.trim()) out.push(lRest.trimEnd());
  return out;
}

function chordProToOverLyric(rawContent, maxWidth = 44) {
  const lines = String(rawContent || "").replace(/\r\n/g, "\n").split("\n");
  const result = [];

  for (const rawLine of lines) {
    const trimmed = rawLine.trim();
    if (!trimmed) {
      result.push("");
      continue;
    }

    const sectionOnly = trimmed.match(/^\[([^\]]+)\]$/);
    if (sectionOnly && !isChordList(sectionOnly[1])) {
      result.push(trimmed);
      continue;
    }

    if (!rawLine.includes("[") || !rawLine.includes("]")) {
      result.push(rawLine.trimEnd());
      continue;
    }

    const bracketRe = /\[([^\]]+)\]/g;
    let lastIndex = 0;
    let lyricLine = "";
    const placements = [];
    let match;

    while ((match = bracketRe.exec(rawLine)) !== null) {
      const before = rawLine.slice(lastIndex, match.index);
      lyricLine += before;
      const inner = match[1].trim();
      lastIndex = match.index + match[0].length;

      if (isChordList(inner)) {
        const chordText = inner.split(/\s+/).filter(Boolean).join("  ");
        placements.push({ col: lyricLine.length, chord: chordText });
        if (
          rawLine[lastIndex] === " " &&
          (lyricLine.length === 0 || lyricLine.endsWith(" "))
        ) {
          lastIndex += 1;
        }
      } else {
        lyricLine += inner;
      }
    }
    lyricLine += rawLine.slice(lastIndex);

    if (placements.length === 0) {
      result.push(rawLine.trimEnd());
      continue;
    }

    if (!lyricLine.trim()) {
      result.push(placements.map((p) => p.chord).join("   "));
      continue;
    }

    let chordLine = "";
    for (const p of placements) {
      const targetCol = chordLine.length === 0 ? p.col : Math.max(p.col, chordLine.length + 2);
      if (targetCol > chordLine.length) {
        chordLine += " ".repeat(targetCol - chordLine.length);
      }
      chordLine += p.chord;
    }

    const cleanLyric = lyricLine.replace(/^\s+/, "").trimEnd();
    const leadingSpaces = lyricLine.length - lyricLine.replace(/^\s+/, "").length;
    const adjustedChordLine =
      leadingSpaces > 0 && chordLine.startsWith(" ".repeat(leadingSpaces))
        ? chordLine.slice(leadingSpaces)
        : chordLine;

    const wrapped = wrapOverLyricPair(adjustedChordLine, cleanLyric, maxWidth);
    result.push(...wrapped);
  }

  return result.join("\n").replace(/\n{3,}/g, "\n\n").trim();
}

function extractModelText(aiResult) {
  if (!aiResult) return "";
  if (typeof aiResult === "string") return aiResult;
  const choiceContent = aiResult?.choices?.[0]?.message?.content;
  if (typeof choiceContent === "string" && choiceContent.trim()) {
    return choiceContent;
  }
  if (typeof aiResult?.response === "string") {
    return aiResult.response;
  }
  return aiResult?.response ?? aiResult;
}

function escapeLiteralNewlinesInStrings(jsonText) {
  let out = "";
  let inString = false;
  let escaped = false;
  for (let i = 0; i < jsonText.length; i++) {
    const ch = jsonText[i];
    if (escaped) {
      out += ch;
      escaped = false;
      continue;
    }
    if (ch === "\\" && inString) {
      out += ch;
      escaped = true;
      continue;
    }
    if (ch === '"') {
      inString = !inString;
      out += ch;
      continue;
    }
    if (inString && ch === "\n") {
      out += "\\n";
      continue;
    }
    if (inString && ch === "\r") {
      continue;
    }
    if (inString && ch === "\t") {
      out += "\\t";
      continue;
    }
    out += ch;
  }
  return out;
}

function parseAiJson(rawOutput) {
  if (rawOutput && typeof rawOutput === "object" && !Array.isArray(rawOutput)) {
    return rawOutput;
  }
  let text = String(rawOutput || "").trim();
  text = text.replace(/^```(?:json)?\s*/i, "").replace(/\s*```$/i, "").trim();
  const firstBrace = text.indexOf("{");
  const lastBrace = text.lastIndexOf("}");
  if (firstBrace >= 0 && lastBrace > firstBrace) {
    text = text.slice(firstBrace, lastBrace + 1);
  }
  try {
    return JSON.parse(text);
  } catch {
    return JSON.parse(escapeLiteralNewlinesInStrings(text));
  }
}

function stripMarkdownFences(rawText) {
  return String(rawText || "")
    .trim()
    .replace(/^```(?:[a-z]+)?\s*/i, "")
    .replace(/\s*```$/i, "")
    .trim();
}

function buildFallbackContentFromSections(sections, chords) {
  const labels = [
    ["Intro", sections?.intro],
    ["Verso", sections?.verse],
    ["Pre-Coro", sections?.preChorus],
    ["Coro", sections?.chorus],
    ["Puente", sections?.bridge],
    ["Outro", sections?.outro],
  ];
  const parts = [];
  for (const [label, seq] of labels) {
    const cleanSeq = String(seq || "").trim();
    if (cleanSeq) {
      parts.push(`[${label}]\n${cleanSeq.replace(/\s*-\s*/g, "   ")}`);
    }
  }
  if (parts.length > 0) {
    return parts.join("\n\n");
  }
  return `[Intro]\n${chords.join("   ") || "C   G   Am   F"}`;
}

function withTimeout(promise, ms) {
  return Promise.race([
    promise,
    new Promise((_, reject) => setTimeout(() => reject(new Error("AI_TIMEOUT")), ms)),
  ]);
}

async function analyzeHarmonyBlueprint(env, targetDescription, language) {
  const systemPrompt =
    "You are a master session guitarist. Output ONLY compact valid JSON (no markdown). " +
    "Always provide the authentic studio guitar chords, key, tuning, capo and BPM of the real recording.";

  const userPrompt = `${targetDescription}
Return ONLY compact JSON for the ORIGINAL studio recording:
{"title":"","artist":"","key":"e.g. Bm or F#m or G","tuning":"E Standard","bpm":120,"capo":0,"difficulty":"${language === "es" ? "Fácil|Intermedio|Avanzado" : "Easy|Intermediate|Advanced"}","strummingPattern":"e.g. D - D U - U D U","chords":["..."],"sections":{"intro":"","verse":"","preChorus":"","chorus":"","bridge":"","outro":""},"practiceTip":"1 short sentence in ${language === "es" ? "Spanish" : "English"}"}`;

  try {
    const r = await withTimeout(
      env.AI.run(HARMONY_MODEL_ID, {
        messages: [
          { role: "system", content: systemPrompt },
          { role: "user", content: userPrompt },
        ],
        reasoning_effort: "low",
        max_tokens: 2800,
        temperature: 0.1,
      }),
      50000
    );
    const text = extractModelText(r);
    if (String(text || "").trim()) {
      return { model: HARMONY_MODEL_ID, parsed: parseAiJson(text) };
    }
  } catch {
    // Fast fallback to gpt-oss-120b (~8s)
  }

  const fallbackRes = await env.AI.run(HARMONY_FALLBACK_MODEL_ID, {
    messages: [
      { role: "system", content: systemPrompt },
      { role: "user", content: userPrompt },
    ],
    reasoning_effort: "low",
    max_tokens: 1800,
    temperature: 0.1,
  });
  const fallbackText = extractModelText(fallbackRes);
  return { model: HARMONY_FALLBACK_MODEL_ID, parsed: parseAiJson(fallbackText) };
}

async function alignLyricsWithBlueprint(env, blueprint, fetchedLyrics, language) {
  const sectionsJson = JSON.stringify(blueprint.sections || {});
  const allowedChords = blueprint.chords.join(", ");
  const sectionHeaders =
    language === "es"
      ? "[Intro], [Verso 1], [Pre-Coro], [Coro], [Verso 2], [Puente], [Outro]"
      : "[Intro], [Verse 1], [Pre-Chorus], [Chorus], [Verse 2], [Bridge], [Outro]";

  const systemPrompt =
    "You are a ChordPro guitar sheet formatter. " +
    "Output ONLY plain ChordPro text (NO JSON, NO markdown ``` fences, NO commentary before or after). " +
    "CRITICAL RULES:\n" +
    "1. Use ONLY the exact chords from the verified harmony blueprint below—never invent or alter chords.\n" +
    "2. Wrap EVERY chord strictly inside square brackets like [Em7] [G] [A7sus4] right before the word or syllable where the chord changes.\n" +
    `3. Use section headers: ${sectionHeaders}.\n` +
    "4. Keep the lyrics in their original language verbatim.";

  const lyricsBlock = fetchedLyrics
    ? `Lyrics to annotate:\n${fetchedLyrics}`
    : `No external lyrics provided. Include the well-known real lyrics in their original language annotated with [Chord] before each chord change (or section progressions if instrumental).`;

  const userPrompt = `Song: ${blueprint.title} - ${blueprint.artist}
Key: ${blueprint.key} | Capo: ${blueprint.capo} | Allowed chords: ${allowedChords}
Verified section progressions: ${sectionsJson}

${lyricsBlock}`;

  try {
    const r = await env.AI.run(FORMATTER_MODEL_ID, {
      messages: [
        { role: "system", content: systemPrompt },
        { role: "user", content: userPrompt },
      ],
      max_tokens: 1800,
      temperature: 0.1,
    });
    const plain = stripMarkdownFences(extractModelText(r));
    if (plain && plain.length > 20) {
      return plain;
    }
  } catch {
    // Deterministic section fallback below
  }
  return buildFallbackContentFromSections(blueprint.sections, blueprint.chords);
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    if (request.method === "GET" && (url.pathname === "/health" || url.pathname === "/")) {
      return jsonResponse({
        ok: true,
        service: "riff-forge-ai",
        model: HARMONY_MODEL_ID,
      });
    }

    if (request.method === "POST" && url.pathname === "/api/ai-chords") {
      let body;
      try {
        body = await request.json();
      } catch {
        return jsonResponse({ error: "Cuerpo JSON inválido." }, 400);
      }

      const rawInput = String(body?.input || "").trim();
      const rawStyle = String(body?.styleHint || "Original").trim();
      const styleHint = rawStyle === "Simplificado" ? "Simplificado" : "Original";
      const language = String(body?.language || "es").trim().toLowerCase() === "en" ? "en" : "es";

      if (!rawInput || rawInput.length > 400) {
        return jsonResponse(
          { error: "Ingresa un enlace válido de YouTube o el nombre de la canción y artista (máx. 400 caracteres)." },
          400
        );
      }

      if (!env.AI) {
        return jsonResponse({ error: "Workers AI no está configurado en el servidor." }, 500);
      }

      let videoId = extractYouTubeVideoId(rawInput);
      let youtubeUrl = "";
      let thumbnailUrl = "";
      let videoTitle = "";
      let channelAuthor = "";

      if (looksLikeUrl(rawInput)) {
        if (!videoId) {
          return jsonResponse(
            { error: "Enlace de YouTube no reconocido. Usa un link como youtube.com/watch?v=... o youtu.be/..." },
            400
          );
        }
        youtubeUrl = `https://www.youtube.com/watch?v=${videoId}`;
        thumbnailUrl = `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`;

        try {
          const oembedUrl = `https://www.youtube.com/oembed?url=${encodeURIComponent(youtubeUrl)}&format=json`;
          const oembedResp = await fetch(oembedUrl, {
            headers: { "User-Agent": "RiffForge-AI/1.2" },
          });
          if (!oembedResp.ok) {
            return jsonResponse(
              { error: "No se pudo consultar este video en YouTube. Verifica que el video exista y sea público." },
              400
            );
          }
          const meta = await oembedResp.json();
          videoTitle = String(meta?.title || "").trim();
          channelAuthor = String(meta?.author_name || "").trim();
        } catch {
          return jsonResponse(
            { error: "No se pudo conectar con YouTube para leer los metadatos del video." },
            400
          );
        }
      }

      const targetDescription = videoId
        ? `YouTube Video:\n- Video title: "${videoTitle}"\n- Channel/Artist: "${channelAuthor}"\n- URL: ${youtubeUrl}`
        : `Song query from guitarist: "${rawInput}"`;

      try {
        const [fetchedLyrics, harmonyResult] = await Promise.all([
          fetchRealLyrics(videoTitle, channelAuthor, rawInput),
          analyzeHarmonyBlueprint(env, targetDescription, language),
        ]);

        const parsed = harmonyResult.parsed || {};
        const rawBpm = Number(parsed?.bpm);
        const bpm = Number.isFinite(rawBpm) ? Math.min(240, Math.max(40, Math.round(rawBpm))) : 120;

        const rawCapo = Number(parsed?.capo);
        let capo = Number.isFinite(rawCapo) ? Math.min(9, Math.max(0, Math.round(rawCapo))) : 0;

        const parsedChords = Array.isArray(parsed?.chords)
          ? parsed.chords
              .map((c) => (typeof c === "string" ? c : String(c?.name || "")).trim())
              .filter(Boolean)
              .slice(0, 16)
          : [];

        const title = String(parsed?.title || videoTitle || rawInput).trim().slice(0, 160) || "Canción IA";
        const artist = String(parsed?.artist || channelAuthor || "YouTube").trim().slice(0, 120) || "YouTube";
        const key = normalizeKey(parsed?.key || parsedChords[0] || "C");
        let chords = parsedChords.length > 0 ? parsedChords : [key];
        const tuning = String(parsed?.tuning || "E Standard").trim().slice(0, 60) || "E Standard";
        let difficulty = String(parsed?.difficulty || "Intermedio").trim().slice(0, 40) || "Intermedio";
        const strummingPattern = String(parsed?.strummingPattern || "D - D U - U D U").trim().slice(0, 100);
        let practiceTip = String(
          parsed?.practiceTip || "Practica con metrónomo lento antes de subir al tempo original."
        ).trim();

        let blueprint = {
          title,
          artist,
          key,
          capo,
          chords,
          sections: parsed?.sections || {},
        };

        if (styleHint === "Simplificado") {
          const simplified = simplifyBlueprint(blueprint, practiceTip, language);
          blueprint = simplified.blueprint;
          capo = blueprint.capo;
          chords = blueprint.chords;
          practiceTip = simplified.practiceTip;
          difficulty = simplified.difficulty;
        }

        const plainChordPro = await alignLyricsWithBlueprint(env, blueprint, fetchedLyrics, language);
        const content = chordProToOverLyric(plainChordPro, 44);

        return jsonResponse({
          title,
          artist,
          key,
          tuning,
          bpm,
          capo,
          difficulty,
          strummingPattern,
          chords,
          content,
          practiceTip,
          hasLyrics: Boolean(fetchedLyrics || content.split("\n").length > 6),
          lyricsSource: fetchedLyrics ? "lrclib" : "llm",
          youtubeUrl,
          youtubeVideoId: videoId || "",
          thumbnailUrl,
          videoTitle,
          model: harmonyResult.model,
        });
      } catch (err) {
        return jsonResponse(
          { error: `No se pudo generar la progresión con IA: ${String(err?.message || err)}` },
          502
        );
      }
    }

    return jsonResponse({ error: "Ruta no encontrada." }, 404);
  },
};
