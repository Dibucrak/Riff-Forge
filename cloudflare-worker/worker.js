const MODEL_ID = "@cf/meta/llama-3.3-70b-instruct-fp8-fast";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Accept",
};

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

function trimLyricsForPrompt(rawLyrics, maxChars = 2200) {
  const normalized = String(rawLyrics || "")
    .replace(/\r\n/g, "\n")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
  if (!normalized || normalized.length <= maxChars) {
    return normalized;
  }
  const slice = normalized.slice(0, maxChars);
  const lastParagraphBreak = slice.lastIndexOf("\n\n");
  if (lastParagraphBreak >= Math.floor(maxChars * 0.55)) {
    return slice.slice(0, lastParagraphBreak).trim();
  }
  const lastLineBreak = slice.lastIndexOf("\n");
  if (lastLineBreak >= Math.floor(maxChars * 0.55)) {
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
        (item) => item && item.instrumental !== true && typeof item.plainLyrics === "string" && item.plainLyrics.trim().length > 20
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

      const trimmed = trimLyricsForPrompt(best.plainLyrics, 2200);
      if (trimmed) {
        return trimmed;
      }
    } catch {
      // Silently fallback so the AI endpoint never breaks if LRCLIB times out
    }
  }
  return "";
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
    if (ch === "\"") {
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
        model: MODEL_ID,
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
      const styleHint = String(body?.styleHint || "Original").trim().slice(0, 40) || "Original";
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

      const fetchedLyrics = await fetchRealLyrics(videoTitle, channelAuthor, rawInput);

      const targetDescription = videoId
        ? `Video de YouTube:\n- Título del video: "${videoTitle}"\n- Canal/Artista en YouTube: "${channelAuthor}"\n- URL: ${youtubeUrl}`
        : `Canción / consulta ingresada por el músico: "${rawInput}"`;

      const lyricsInstructionBlock = fetchedLyrics
        ? `\n\nLetra real de referencia (conserva estos versos en su idioma original y coloca los acordes [Acorde] exactamente antes de la palabra o sílaba donde cambia cada acorde):\n${fetchedLyrics}`
        : `\n\nIncluye la letra real conocida de la canción en su idioma original con los acordes [Acorde] colocados inline exactamente antes de la palabra o sílaba donde cambia cada acorde (si es puramente instrumental, detalla la estructura por secciones y compases).`;

      const systemPrompt =
        "Eres un guitarrista profesional y transcriptor experto de cifrados ChordPro. " +
        "Responde ÚNICAMENTE con un objeto JSON válido (sin markdown, sin bloques ``` ni texto fuera del JSON; usa \\n escapado dentro de strings JSON). " +
        "Usa cifrado americano estándar para los nombres de acordes (C, Am, Em7, Dsus4, E7, F#m, Bb) porque la app los transpone automáticamente. " +
        "Debes incluir la letra real de la canción en su idioma original dentro de \"content\" con los acordes entre corchetes [Acorde] colocados exactamente antes de la palabra o sílaba donde cambia cada acorde, organizada por secciones ([Intro], [Verso 1], [Pre-Coro], [Coro], [Verso 2], [Puente], [Outro]).";

      const userPrompt = `${targetDescription}
Estilo/Enfoque solicitado: ${styleHint}
Idioma de explicaciones: ${language === "es" ? "Español" : "English"}${lyricsInstructionBlock}

Devuelve exactamente un JSON con esta estructura:
{
  "title": "Nombre limpio de la canción",
  "artist": "Artista o banda",
  "key": "Tonalidad principal en cifrado americano, ej. G, Am, F#m",
  "tuning": "Afinación recomendada, ej. E Standard o Drop D",
  "bpm": 120,
  "capo": 0,
  "difficulty": "Fácil | Intermedio | Avanzado",
  "strummingPattern": "Patrón rítmico breve en español, ej. D - D U - U D U",
  "chords": ["Em7", "G", "Dsus4", "A7sus4"],
  "content": "[Intro]\\n[Em7] [G] [Dsus4] [A7sus4]\\n\\n[Verso 1]\\n[Em7] Today is gonna be the [G] day that they're gonna throw it [Dsus4] back to [A7sus4] you\\n...",
  "practiceTip": "Consejo práctico en 1-2 oraciones en español (escala sugerida para improvisar, dinámica o digitación clave)."
}`;

      try {
        const aiResult = await env.AI.run(MODEL_ID, {
          messages: [
            { role: "system", content: systemPrompt },
            { role: "user", content: userPrompt },
          ],
          max_tokens: 3000,
          temperature: 0.2,
        });

        const parsed = parseAiJson(aiResult?.response ?? aiResult);

        const rawBpm = Number(parsed?.bpm);
        const bpm = Number.isFinite(rawBpm) ? Math.min(240, Math.max(40, Math.round(rawBpm))) : 120;

        const rawCapo = Number(parsed?.capo);
        const capo = Number.isFinite(rawCapo) ? Math.min(9, Math.max(0, Math.round(rawCapo))) : 0;

        const chords = Array.isArray(parsed?.chords)
          ? parsed.chords.map((c) => String(c || "").trim()).filter(Boolean).slice(0, 16)
          : [];

        const title = String(parsed?.title || videoTitle || rawInput).trim().slice(0, 160) || "Canción IA";
        const artist = String(parsed?.artist || channelAuthor || "YouTube").trim().slice(0, 120) || "YouTube";
        const key = String(parsed?.key || "C").trim().slice(0, 20) || "C";
        const tuning = String(parsed?.tuning || "E Standard").trim().slice(0, 60) || "E Standard";
        const difficulty = String(parsed?.difficulty || "Intermedio").trim().slice(0, 40) || "Intermedio";
        const strummingPattern = String(parsed?.strummingPattern || "D - D U - U D U").trim().slice(0, 100);
        const content = String(parsed?.content || "").trim() ||
          `[Intro]\n${chords.map((c) => `[${c}]`).join(" ") || "[C] [G] [Am] [F]"}`;
        const practiceTip = String(parsed?.practiceTip || "Practica con metrónomo lento antes de subir al tempo original.").trim();

        return jsonResponse({
          title,
          artist,
          key,
          tuning,
          bpm,
          capo,
          difficulty,
          strummingPattern,
          chords: chords.length > 0 ? chords : [key],
          content,
          practiceTip,
          hasLyrics: Boolean(fetchedLyrics || content.split("\n").length > 6),
          lyricsSource: fetchedLyrics ? "lrclib" : "llm",
          youtubeUrl,
          youtubeVideoId: videoId || "",
          thumbnailUrl,
          videoTitle,
          model: MODEL_ID,
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
