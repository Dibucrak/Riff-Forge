const HARMONY_MODEL_ID = "@cf/google/gemma-4-26b-a4b-it";
const HARMONY_FALLBACK_MODEL_ID = "@cf/openai/gpt-oss-120b";
const FORMATTER_MODEL_ID = "@cf/meta/llama-3.3-70b-instruct-fp8-fast";
const GEMINI_MODEL_ID = "gemini-2.5-flash";

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

const BROWSER_UA =
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

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
    /\((?:official\s*(?:music\s*)?(?:video|audio|lyric\s*video|visualizer)|lyrics?|letra|audio|hd|4k|remastered[^)]*|live[^)]*|en\s+vivo[^)]*|video\s+oficial|sub\.?\s*español)\)/gi,
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

function decodeHtmlEntities(input) {
  const named = {
    amp: "&",
    lt: "<",
    gt: ">",
    quot: '"',
    apos: "'",
    nbsp: " ",
    aacute: "á",
    eacute: "é",
    iacute: "í",
    oacute: "ó",
    uacute: "ú",
    ntilde: "ñ",
    uuml: "ü",
    Aacute: "Á",
    Eacute: "É",
    Iacute: "Í",
    Oacute: "Ó",
    Uacute: "Ú",
    Ntilde: "Ñ",
    Uuml: "Ü",
    iquest: "¿",
    iexcl: "¡",
  };
  return String(input || "")
    .replace(/&#(\d+);/g, (_, code) => String.fromCharCode(Number(code)))
    .replace(/&#x([0-9a-f]+);/gi, (_, hex) => String.fromCharCode(parseInt(hex, 16)))
    .replace(/&([a-zA-Z]+);/g, (full, name) => (name in named ? named[name] : full));
}

async function decodeResponseHtml(resp) {
  const buf = await resp.arrayBuffer();
  const ctype = (resp.headers.get("content-type") || "").toLowerCase();
  const useLatin1 = ctype.includes("iso-8859-1") || ctype.includes("latin1") || ctype.includes("windows-1252");
  try {
    return new TextDecoder(useLatin1 ? "iso-8859-1" : "utf-8").decode(buf);
  } catch {
    return new TextDecoder("utf-8").decode(buf);
  }
}

function toLaCuerdaSlug(str) {
  return String(str || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/&/g, " ")
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "");
}

function tokenizeNormalized(str) {
  return toLaCuerdaSlug(str)
    .split("_")
    .filter((w) => w.length >= 2);
}

function buildArtistSlugCandidates(artist) {
  const full = toLaCuerdaSlug(artist);
  if (!full) return [];
  const candidates = [full];
  const noArticle = full.replace(/^(?:los|las|el|la|the)_/, "");
  if (noArticle && !candidates.includes(noArticle)) {
    candidates.push(noArticle);
  }
  const parts = full.split("_").filter(Boolean);
  if (parts.length > 1 && parts[0].length >= 3 && !candidates.includes(parts[0])) {
    candidates.push(parts[0]);
  }
  return candidates;
}

function buildSongSlugCandidates(title) {
  const full = toLaCuerdaSlug(title);
  if (!full) return [];
  const candidates = [full];
  const strippedVersion = full.replace(/_(?:en_vivo|live|acustico|acoustic|unplugged|version)$/, "");
  if (strippedVersion && !candidates.includes(strippedVersion)) {
    candidates.push(strippedVersion);
  }
  return candidates;
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

async function fetchRealLyrics(videoTitle, channelAuthor, rawInput, resolvedArtist = "", resolvedTitle = "") {
  const cleanTitle = cleanTrackText(resolvedTitle || videoTitle);
  const cleanAuthor = cleanTrackText(resolvedArtist || channelAuthor);
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
          "User-Agent": "RiffForge-AI/1.3 (+https://github.com/Dibucrak/Riff-Forge)",
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

async function resolveSongIdentity(env, rawInput, videoTitle, channelAuthor) {
  const sourceTitle = cleanTrackText(videoTitle);
  const sourceAuthor = cleanTrackText(channelAuthor);

  // Fast heuristic parse for explicit "Artist - Title"
  let heuristicArtist = "";
  let heuristicTitle = "";
  const candidateText = sourceTitle || (!looksLikeUrl(rawInput) ? cleanTrackText(rawInput) : "");
  const dashSplit = candidateText.split(/\s+[-–—]\s+/);
  if (dashSplit.length >= 2 && dashSplit[0].length >= 2 && dashSplit[1].length >= 2) {
    heuristicArtist = dashSplit[0].trim();
    heuristicTitle = dashSplit.slice(1).join(" - ").trim();
  } else if (sourceAuthor && sourceTitle) {
    heuristicArtist = sourceAuthor;
    heuristicTitle = sourceTitle;
  }

  try {
    const prompt = videoTitle
      ? `YouTube video title: "${videoTitle}" | Uploader/Channel: "${channelAuthor}"`
      : `User song search query: "${rawInput}"`;

    const res = await withTimeout(
      env.AI.run(FORMATTER_MODEL_ID, {
        messages: [
          {
            role: "system",
            content:
              "Extract the canonical music artist and song title as strict JSON {\"artist\":\"\",\"title\":\"\"}. " +
              "Fix obvious typos or phonetic misspellings when it clearly refers to a real song (for example: 'escrivir todo denuevo de cuchillado' -> {\"artist\":\"Cuchillazo\",\"title\":\"Escribir todo de nuevo\"}). " +
              "Output ONLY valid JSON.",
          },
          { role: "user", content: prompt },
        ],
        max_tokens: 140,
        temperature: 0.0,
      }),
      6500
    );
    const parsed = parseAiJson(extractModelText(res));
    const artist = String(parsed?.artist || heuristicArtist || sourceAuthor || "").trim();
    const title = String(parsed?.title || heuristicTitle || candidateText || rawInput).trim();
    if (title) {
      return { artist, title };
    }
  } catch {
    // Heuristic fallback below
  }

  return {
    artist: heuristicArtist || sourceAuthor,
    title: heuristicTitle || candidateText || rawInput,
  };
}

async function resolveYouTubeVideoForQuery(artist, title, rawInput) {
  const searchQuery = [artist, title].filter(Boolean).join(" ").trim() || rawInput;
  if (!searchQuery || looksLikeUrl(searchQuery)) return null;

  try {
    const searchUrl = `https://www.youtube.com/results?search_query=${encodeURIComponent(searchQuery)}`;
    const resp = await fetch(searchUrl, {
      headers: {
        "User-Agent": BROWSER_UA,
        "Accept-Language": "es-419,es;q=0.9,en;q=0.8",
      },
      signal: AbortSignal.timeout(4000),
    });
    if (!resp.ok) return null;
    const html = await resp.text();
    const match = html.match(/"videoId":"([A-Za-z0-9_-]{11})"/);
    if (!match || !match[1]) return null;

    const videoId = match[1];
    const youtubeUrl = `https://www.youtube.com/watch?v=${videoId}`;
    const oembedUrl = `https://www.youtube.com/oembed?url=${encodeURIComponent(youtubeUrl)}&format=json`;
    const oembedResp = await fetch(oembedUrl, {
      headers: { "User-Agent": "RiffForge-AI/1.3" },
      signal: AbortSignal.timeout(3000),
    });
    if (!oembedResp.ok) {
      return {
        videoId,
        youtubeUrl,
        thumbnailUrl: `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`,
        videoTitle: "",
        channelAuthor: "",
      };
    }
    const meta = await oembedResp.json();
    return {
      videoId,
      youtubeUrl,
      thumbnailUrl: `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`,
      videoTitle: String(meta?.title || "").trim(),
      channelAuthor: String(meta?.author_name || "").trim(),
    };
  } catch {
    return null;
  }
}

function normalizeSectionHeaderLine(text) {
  const trimmed = String(text || "").trim();
  const bracketed = trimmed.match(/^\[([^\]]+)\]$/);
  if (bracketed && !isChordList(bracketed[1])) {
    return {
      header: trimmed,
      remainder: "",
    };
  }
  const match = trimmed.match(
    /^(Intro(?:ducci[oó]n)?|Verso(?:\s*\d+)?|Verse(?:\s*\d+)?|Estrofa(?:\s*\d+)?|Pre-?Coro|Pre-?Chorus|Coro|Chorus|Estribillo|Puente|Bridge|Solo|Interludio|Interlude|Outro|Final)\s*(?:\([^)]*\))?(?:\s*[:.]|\s+-)?\s*(.*)$/i
  );
  if (!match) return null;
  let label = match[1].trim();
  label = label.charAt(0).toUpperCase() + label.slice(1);
  if (/^intro/i.test(label)) label = "Intro";
  else if (/^estribillo/i.test(label)) label = "Coro";
  return {
    header: `[${label}]`,
    remainder: (match[2] || "").trim(),
  };
}

function parseLaCuerdaPre(rawPreHtml, rawOdes, maxWidth = 42) {
  let clean = String(rawPreHtml || "").replace(/\r\n/g, "\n");
  clean = clean.replace(/<div[^>]*>\s*<\/div>/gi, "").replace(/<\/?div[^>]*>/gi, "");

  const rawLines = clean.split("\n");
  const taggedLines = rawLines.map((line) => {
    const hadChordTag = /<a\b[^>]*>[^<]+<\/a>/i.test(line);
    const plain = decodeHtmlEntities(
      line
        .replace(/<a\b[^>]*>([\s\S]*?)<\/a>/gi, (_, inner) => inner.replace(/@/g, "#"))
        .replace(/<[^>]+>/g, "")
    ).replace(/\s+$/g, "");
    return { text: plain, hadChordTag };
  });

  // Detect chord-only lines even if missing <a> tags in minor spots, using strict token heuristic
  const analyzed = taggedLines.map(({ text, hadChordTag }) => {
    const t = text.trim();
    const cleanedTokens = t
      .replace(/[()[\]|]/g, " ")
      .replace(/\s*-\s*/g, " ")
      .replace(/\b[xX]\d+\b/g, " ")
      .split(/\s+/)
      .filter(Boolean);
    const allValidChords =
      cleanedTokens.length > 0 && cleanedTokens.every((tok) => CHORD_TOKEN_RE.test(tok));
    const isChordLine = hadChordTag || (t.length > 0 && allValidChords && cleanedTokens.length >= 1 && t.length <= 72);
    return { text, hadChordTag, isChordLine };
  });

  // Expand section prefixes such as "Intro:(C-Em)" or "Coro:(Bis)"
  const expanded = [];
  for (const item of analyzed) {
    const trimmed = item.text.trim();
    if (!trimmed) {
      expanded.push({ text: "", isChordLine: false, isSection: false });
      continue;
    }

    const sec = normalizeSectionHeaderLine(trimmed);
    if (sec) {
      expanded.push({ text: sec.header, isChordLine: false, isSection: true });
      if (sec.remainder) {
        const remClean = sec.remainder.replace(/^[(:]+|[)]+$/g, "").trim();
        const chordParts = remClean
          .replace(/\b[xX]\d+\b|\b\d+[xX]\b/g, " ")
          .replace(/[()]/g, " ")
          .replace(/-/g, "   ")
          .split(/\s+/)
          .filter(Boolean);
        if (chordParts.length > 0 && chordParts.every((p) => CHORD_TOKEN_RE.test(p))) {
          expanded.push({ text: chordParts.join("   "), isChordLine: true, isSection: false });
        } else if (remClean && !/^bis$/i.test(remClean)) {
          expanded.push({ text: sec.remainder, isChordLine: false, isSection: false });
        }
      }
      continue;
    }

    // Also normalize inline chord sequences like "C-Em (x2)" into clean spacing "C   Em  (x2)"
    if (item.isChordLine) {
      const normalizedChordLine = item.text
        .replace(/([A-G](?:#|b)?[^ \t\n()-]*)-([A-G])/g, "$1  $2")
        .trimEnd();
      expanded.push({ text: normalizedChordLine, isChordLine: true, isSection: false });
    } else {
      expanded.push({ text: item.text, isChordLine: false, isSection: false });
    }
  }

  // Strip trailing uploader notes/chatter after the last musical section
  let lastChordIdx = -1;
  for (let i = expanded.length - 1; i >= 0; i--) {
    if (expanded[i].isChordLine) {
      lastChordIdx = i;
      break;
    }
  }

  if (lastChordIdx >= 0) {
    // Allow at most 2 immediate lyric lines right beneath the final chord line before any blank gap
    let cutoff = lastChordIdx;
    for (let j = lastChordIdx + 1; j < expanded.length && j <= lastChordIdx + 3; j++) {
      const t = expanded[j].text.trim();
      if (!t) break;
      if (
        /\b(weno|bueno esta|espero que|espero q|saludos|chau+|transcripci[oó]n|punteos|dedicado|cualquier duda|gracias|comenten|califiquen|suscr[ií]|visiten|suerte|ojal[aá])\b/i.test(
          t
        ) ||
        /@/.test(t) ||
        /https?:\/\//i.test(t)
      ) {
        break;
      }
      cutoff = j;
    }
    expanded.splice(cutoff + 1);
  }

  // Filter out pure chatter lines anywhere else (rare, at top or middle) and collapse extra blanks
  const cleanedLines = [];
  for (const row of expanded) {
    const t = row.text.trim();
    if (!t) {
      if (cleanedLines.length > 0 && cleanedLines[cleanedLines.length - 1].text !== "") {
        cleanedLines.push({ text: "", isChordLine: false, isSection: false });
      }
      continue;
    }
    if (
      !row.isChordLine &&
      !row.isSection &&
      /\b(otra transcripci|espero q|espero que|mandare los punteos|cuidenase|chau{2,})\b/i.test(t)
    ) {
      continue;
    }
    cleanedLines.push(row);
  }
  while (cleanedLines.length > 0 && cleanedLines[cleanedLines.length - 1].text === "") {
    cleanedLines.pop();
  }

  // Collect ordered chords from odes first, then supplement from sheet chord lines
  const detectedChords = [];
  const addChord = (tok) => {
    const c = String(tok || "").replace(/@/g, "#").trim();
    if (c && CHORD_TOKEN_RE.test(c) && !detectedChords.includes(c)) {
      detectedChords.push(c);
    }
  };

  for (const tok of String(rawOdes || "").replace(/@/g, "#").split(/\s+/)) {
    addChord(tok);
  }
  for (const row of cleanedLines) {
    if (!row.isChordLine) continue;
    const tokens = row.text.replace(/[()[\]|,-]/g, " ").split(/\s+/);
    for (const tok of tokens) addChord(tok);
  }

  // Build final structured lines (marking which lines are chord lines for Simplificado mapping & wrapping)
  const finalLines = [];
  for (let i = 0; i < cleanedLines.length; i++) {
    const curr = cleanedLines[i];
    const next = i + 1 < cleanedLines.length ? cleanedLines[i + 1] : null;
    if (curr.isChordLine && next && !next.isChordLine && !next.isSection && next.text.trim()) {
      const cText = curr.text;
      const lText = next.text.replace(/^\s+/, "").trimEnd();
      const leadSpaces = next.text.length - next.text.replace(/^\s+/, "").length;
      const adjChord =
        leadSpaces > 0 && cText.startsWith(" ".repeat(leadSpaces))
          ? cText.slice(leadSpaces)
          : cText;

      if (Math.max(adjChord.length, lText.length) > maxWidth) {
        const wrapped = wrapOverLyricPair(adjChord, lText, maxWidth);
        for (let w = 0; w < wrapped.length; w++) {
          const lineStr = wrapped[w];
          const wTokens = lineStr.trim().replace(/[()[\]|,-]/g, " ").split(/\s+/).filter(Boolean);
          const isWChord = wTokens.length > 0 && wTokens.every((tok) => CHORD_TOKEN_RE.test(tok));
          finalLines.push({ text: lineStr, isChordLine: isWChord });
        }
      } else {
        finalLines.push({ text: adjChord, isChordLine: true });
        finalLines.push({ text: lText, isChordLine: false });
      }
      i += 1;
    } else {
      finalLines.push({ text: curr.text, isChordLine: curr.isChordLine });
    }
  }

  return {
    lines: finalLines,
    chords: detectedChords.slice(0, 16),
  };
}

async function fetchLaCuerdaPage(url) {
  try {
    const resp = await fetch(url, {
      headers: {
        "User-Agent": BROWSER_UA,
        Accept: "text/html,application/xhtml+xml",
        "Accept-Language": "es-419,es;q=0.9",
      },
      signal: AbortSignal.timeout(4500),
    });
    if (!resp.ok) return null;
    return await decodeResponseHtml(resp);
  } catch {
    return null;
  }
}

function extractLaCuerdaFromHtml(html) {
  if (!html) return null;
  const preMatch = html.match(/<div\s+id=["']?t_body["']?[^>]*>\s*<pre[^>]*>([\s\S]*?)<\/pre>/i);
  if (!preMatch || !preMatch[1]) return null;

  const odesMatch = html.match(/odes\s*=\s*'([^']*)'/i);
  const obandMatch = html.match(/oband\s*=\s*'([^']*)'/i);
  const orolaMatch = html.match(/orola\s*=\s*'([^']*)'/i);

  const parsed = parseLaCuerdaPre(preMatch[1], odesMatch ? odesMatch[1] : "", 42);
  if (parsed.chords.length === 0 || parsed.lines.length < 3) return null;

  return {
    artist: decodeHtmlEntities(obandMatch ? obandMatch[1] : "").trim(),
    title: decodeHtmlEntities(orolaMatch ? orolaMatch[1] : "").trim(),
    chords: parsed.chords,
    lines: parsed.lines,
  };
}

async function fetchLaCuerdaChordSheet(artist, title) {
  const artistSlugs = buildArtistSlugCandidates(artist);
  const songSlugs = buildSongSlugCandidates(title);
  if (artistSlugs.length === 0 || songSlugs.length === 0) return null;

  // 1. Direct URL fast lookup
  const directCandidates = [];
  for (const aSlug of artistSlugs.slice(0, 2)) {
    for (const sSlug of songSlugs.slice(0, 2)) {
      directCandidates.push(`https://acordes.lacuerda.net/${aSlug}/${sSlug}.shtml`);
    }
  }

  for (const url of directCandidates.slice(0, 3)) {
    const html = await fetchLaCuerdaPage(url);
    const sheet = extractLaCuerdaFromHtml(html);
    if (sheet) {
      return { ...sheet, sourceUrl: url };
    }
  }

  // 2. Artist directory fallback (handles slight differences like "de_nuevo" vs "d_nuevo" or "-2")
  const queryTokens = tokenizeNormalized(title);
  for (const aSlug of artistSlugs.slice(0, 2)) {
    const indexHtml = await fetchLaCuerdaPage(`https://acordes.lacuerda.net/${aSlug}/`);
    if (!indexHtml) continue;

    const itemRe = /<li[^>]*lcd=['"]([^'"]*)['"][^>]*>\s*<a\s+href="([^"]+)">([\s\S]*?)<\/a>\s*<\/li>/gi;
    let match;
    let bestEntry = null;
    let bestScore = -1;

    while ((match = itemRe.exec(indexHtml)) !== null) {
      const lcd = match[1] || "";
      const href = match[2] || "";
      const rawLabel = decodeHtmlEntities(match[3].replace(/<[^>]+>/g, " "))
        .replace(/\bacordes\b/gi, "")
        .trim();
      const entryTokens = tokenizeNormalized(rawLabel);
      if (entryTokens.length === 0 || queryTokens.length === 0) continue;

      let overlap = 0;
      for (const qt of queryTokens) {
        if (entryTokens.includes(qt)) overlap += 1;
      }
      const ratio = overlap / Math.max(queryTokens.length, entryTokens.length);
      if (overlap === 0 || ratio < 0.5) continue;

      const isChordType = lcd.startsWith("R") || /<em>acordes<\/em>/i.test(match[3]);
      const score = ratio * 10 + (isChordType ? 2 : 0) - Math.abs(entryTokens.length - queryTokens.length) * 0.2;
      if (score > bestScore) {
        bestScore = score;
        bestEntry = href;
      }
    }

    if (bestEntry) {
      const cleanHref = bestEntry.replace(/^\//, "");
      const pageUrl = cleanHref.endsWith(".shtml")
        ? `https://acordes.lacuerda.net/${aSlug}/${cleanHref}`
        : `https://acordes.lacuerda.net/${aSlug}/${cleanHref}.shtml`;
      const html = await fetchLaCuerdaPage(pageUrl);
      const sheet = extractLaCuerdaFromHtml(html);
      if (sheet) {
        return { ...sheet, sourceUrl: pageUrl };
      }
    }
  }

  return null;
}

function expandTabs(str, tabSize = 8) {
  let out = "";
  let col = 0;
  for (const ch of String(str || "")) {
    if (ch === "\t") {
      const spaces = tabSize - (col % tabSize);
      out += " ".repeat(spaces);
      col += spaces;
    } else {
      out += ch;
      col += 1;
    }
  }
  return out;
}

function parseGuitareTabHtml(html, maxWidth = 42) {
  if (!html) return null;
  const preMatch = html.match(/<pre[^>]*class=["'][^"']*js-tab-fit-to-screen[^"']*["'][^>]*>([\s\S]*?)<\/pre>/i);
  if (!preMatch || !preMatch[1]) return null;

  const rawPre = preMatch[1];
  const parts = rawPre.split(/<span[^>]*class=["'][^"']*js-tab-row[^"']*["'][^>]*>/i);
  const rawRows = parts.length > 1
    ? parts.slice(1).map((part) => ({
        hadChordTag: /gt-chord/i.test(part),
        text: expandTabs(
          decodeHtmlEntities(part.replace(/<[^>]+>/g, ""))
            .replace(/\r/g, "")
            .replace(/\n/g, "")
        ).replace(/\s+$/g, ""),
      }))
    : rawPre.replace(/\r\n/g, "\n").split("\n").map((line) => ({
        hadChordTag: /gt-chord/i.test(line),
        text: expandTabs(decodeHtmlEntities(line.replace(/<[^>]+>/g, ""))).replace(/\s+$/g, ""),
      }));

  let capo = 0;
  let tuning = "E Standard";
  let tabLineCount = 0;

  for (let i = 0; i < Math.min(rawRows.length, 20); i++) {
    const t = rawRows[i].text.trim();
    if (!t) continue;
    const capoMatch =
      t.match(/\bcapo\b[^0-9\n]{0,12}(\d)/i) ||
      (/\bcapo\b/i.test(t) ? t.match(/(\d)(?:st|nd|rd|th)\s+fret/i) : null);
    if (capoMatch && !capo) {
      const n = Number(capoMatch[1]);
      if (Number.isFinite(n) && n >= 0 && n <= 9) capo = n;
    }
    if (/\b(?:half[\s-]*step\s+down|eb\s+standard|d#\s+standard)\b/i.test(t)) {
      tuning = "Eb Standard (½ tono abajo)";
    }
  }

  const expanded = [];
  for (const row of rawRows) {
    const t = row.text.trim();
    if (!t) {
      expanded.push({ text: "", isChordLine: false, isSection: false });
      continue;
    }
    if (/^\s*[eBGDAE]\|[-0-9hpb/\\x|:\s~^()]+/i.test(row.text)) {
      tabLineCount += 1;
      continue;
    }
    if (/^[-=_~*]{3,}$/.test(t)) continue;
    if (
      /^\s*chords\s*:/i.test(t) ||
      /^\s*(?:chords\s*:\s*)?[A-G][^\s]{0,10}\s*[:=-]?\s*[xX0-9]{5,6}\b/i.test(t)
    ) {
      continue;
    }

    const sec = normalizeSectionHeaderLine(t);
    if (sec) {
      expanded.push({ text: sec.header, isChordLine: false, isSection: true });
      if (sec.remainder) {
        const remClean = sec.remainder.replace(/^[(:]+|[)]+$/g, "").trim();
        const chordParts = remClean
          .replace(/\b[xX]\d+\b|\b\d+[xX]\b/g, " ")
          .replace(/[()]/g, " ")
          .replace(/-/g, "   ")
          .split(/\s+/)
          .filter(Boolean);
        if (chordParts.length > 0 && chordParts.every((p) => CHORD_TOKEN_RE.test(p))) {
          expanded.push({ text: chordParts.join("   "), isChordLine: true, isSection: false });
        }
      }
      continue;
    }

    const cleanedTokens = t
      .replace(/[()[\]|]/g, " ")
      .replace(/\s*-\s*/g, " ")
      .replace(/\b[xX]\d+\b|\b\d+[xX]\b/g, " ")
      .split(/\s+/)
      .filter(Boolean);
    const strictAllValidChords =
      cleanedTokens.length > 0 && cleanedTokens.every((tok) => CHORD_TOKEN_RE.test(tok));
    const isChordLine =
      (row.hadChordTag || strictAllValidChords) && cleanedTokens.length > 0 && (strictAllValidChords || row.hadChordTag);

    if (isChordLine && strictAllValidChords) {
      expanded.push({
        text: row.text.replace(/([A-G](?:#|b)?[^ \t\n()-]*)-([A-G])/g, "$1  $2").trimEnd(),
        isChordLine: true,
        isSection: false,
      });
    } else if (isChordLine && row.hadChordTag && cleanedTokens.some((tok) => CHORD_TOKEN_RE.test(tok))) {
      expanded.push({ text: row.text.trimEnd(), isChordLine: true, isSection: false });
    } else {
      expanded.push({ text: row.text.trimEnd(), isChordLine: false, isSection: false });
    }
  }

  // Strip uploader preamble before the first chord line (keeping immediately preceding section header)
  const firstChordIdx = expanded.findIndex((r) => r.isChordLine);
  if (firstChordIdx < 0) return null;
  let startIdx = firstChordIdx;
  if (firstChordIdx > 0 && expanded[firstChordIdx - 1].isSection) {
    startIdx = firstChordIdx - 1;
  }
  const trimmedHead = expanded.slice(startIdx);

  // Strip trailing uploader chatter after the last chord line + up to 2 lyric lines
  let lastChordIdx = -1;
  for (let i = trimmedHead.length - 1; i >= 0; i--) {
    if (trimmedHead[i].isChordLine) {
      lastChordIdx = i;
      break;
    }
  }
  if (lastChordIdx >= 0) {
    let cutoff = lastChordIdx;
    for (let j = lastChordIdx + 1; j < trimmedHead.length && j <= lastChordIdx + 3; j++) {
      const lt = trimmedHead[j].text.trim();
      if (!lt) break;
      if (
        /\b(enjoy|hope you|first tab|thank|thanks|comment|rate|subscribe|written by|submitted by)\b/i.test(lt) ||
        /@/.test(lt) ||
        /https?:\/\//i.test(lt)
      ) {
        break;
      }
      cutoff = j;
    }
    trimmedHead.splice(cutoff + 1);
  }

  // Collapse extra blank lines
  const cleanedLines = [];
  for (const row of trimmedHead) {
    if (!row.text.trim()) {
      if (cleanedLines.length > 0 && cleanedLines[cleanedLines.length - 1].text !== "") {
        cleanedLines.push({ text: "", isChordLine: false, isSection: false });
      }
      continue;
    }
    cleanedLines.push(row);
  }
  while (cleanedLines.length > 0 && cleanedLines[cleanedLines.length - 1].text === "") {
    cleanedLines.pop();
  }

  const detectedChords = [];
  for (const row of cleanedLines) {
    if (!row.isChordLine) continue;
    const tokens = row.text.replace(/[()[\]|,-]/g, " ").split(/\s+/);
    for (const tok of tokens) {
      const c = tok.trim();
      if (c && CHORD_TOKEN_RE.test(c) && !detectedChords.includes(c)) {
        detectedChords.push(c);
      }
    }
  }

  let lyricPairs = 0;
  const finalLines = [];
  for (let i = 0; i < cleanedLines.length; i++) {
    const curr = cleanedLines[i];
    const next = i + 1 < cleanedLines.length ? cleanedLines[i + 1] : null;
    if (curr.isChordLine && next && !next.isChordLine && !next.isSection && next.text.trim()) {
      if (next.text.trim().length >= 8) lyricPairs += 1;
      const cText = curr.text;
      const lText = next.text.replace(/^\s+/, "").trimEnd();
      const leadSpaces = next.text.length - next.text.replace(/^\s+/, "").length;
      const adjChord =
        leadSpaces > 0 && cText.startsWith(" ".repeat(leadSpaces))
          ? cText.slice(leadSpaces)
          : cText;

      if (Math.max(adjChord.length, lText.length) > maxWidth) {
        const wrapped = wrapOverLyricPair(adjChord, lText, maxWidth);
        for (const lineStr of wrapped) {
          const wTokens = lineStr.trim().replace(/[()[\]|,-]/g, " ").split(/\s+/).filter(Boolean);
          const isWChord = wTokens.length > 0 && wTokens.every((tok) => CHORD_TOKEN_RE.test(tok));
          finalLines.push({ text: lineStr, isChordLine: isWChord });
        }
      } else {
        finalLines.push({ text: adjChord, isChordLine: true });
        finalLines.push({ text: lText, isChordLine: false });
      }
      i += 1;
    } else {
      finalLines.push({ text: curr.text, isChordLine: curr.isChordLine });
    }
  }

  if (detectedChords.length < 2 || lyricPairs < 4 || tabLineCount > 6) {
    return null;
  }

  return {
    chords: detectedChords.slice(0, 16),
    lines: finalLines,
    capo,
    tuning,
    lyricPairs,
    tabLineCount,
  };
}

async function fetchGuitareTabChordSheet(artist, title, styleHint = "Original") {
  const artistTokens = tokenizeNormalized(artist);
  const titleTokens = tokenizeNormalized(title);
  if (titleTokens.length === 0) return null;

  const searchQueries = [];
  const fullQuery = [artist, title].filter(Boolean).join(" ").trim();
  if (fullQuery) searchQueries.push(fullQuery);
  if (title && !searchQueries.includes(title.trim())) searchQueries.push(title.trim());

  const candidates = [];
  for (const query of searchQueries) {
    try {
      const searchUrl = `https://www.guitaretab.com/fetch/?type=tab&query=${encodeURIComponent(query)}`;
      const resp = await fetch(searchUrl, {
        headers: {
          "User-Agent": BROWSER_UA,
          Accept: "text/html,application/xhtml+xml",
          "Accept-Language": "en-US,en;q=0.9",
        },
        signal: AbortSignal.timeout(5000),
      });
      if (!resp.ok) continue;
      const html = await resp.text();

      const itemBlocks = [];
      const liRe = /<li[^>]*class=["'][^"']*gt-list__item[^"']*["'][^>]*>([\s\S]*?)<\/li>/gi;
      let liMatch;
      while ((liMatch = liRe.exec(html)) !== null) {
        itemBlocks.push(liMatch[1]);
      }
      if (itemBlocks.length === 0) {
        const anchorRe = /(<a[^>]*href=["']\/[a-z0-9]\/[^"']+\.html["'][^>]*>[\s\S]*?<\/a>[\s\S]{0,260})/gi;
        let aMatch;
        while ((aMatch = anchorRe.exec(html)) !== null) {
          itemBlocks.push(aMatch[1]);
        }
      }

      for (const block of itemBlocks) {
        const hrefMatch = block.match(/href=["'](\/[a-z0-9]\/([^/"']+)\/\d+\.html)["'][^>]*>([\s\S]*?)<\/a>/i);
        if (!hrefMatch) continue;
        const href = hrefMatch[1];
        const artistSlug = hrefMatch[2] || "";
        const linkLabel = decodeHtmlEntities(hrefMatch[3].replace(/<[^>]+>/g, " ")).replace(/\s+/g, " ").trim();
        const blockText = decodeHtmlEntities(block.replace(/<[^>]+>/g, " ")).replace(/\s+/g, " ").trim();

        if (!/\bchords\b/i.test(linkLabel)) continue;
        if (/\b(tab|drum|bass|ukulele|gp|guitar\s*pro|solo|intro)\b/i.test(linkLabel)) continue;

        const urlArtistTokens = tokenizeNormalized(artistSlug.replace(/-/g, " "));
        let artistOverlapCount = 0;
        for (const at of artistTokens) {
          if (urlArtistTokens.includes(at)) artistOverlapCount += 1;
        }
        if (artistTokens.length > 0 && artistOverlapCount === 0) continue;
        const artistOverlap = artistTokens.length > 0 ? artistOverlapCount / Math.max(artistTokens.length, urlArtistTokens.length) : 0.5;

        const cleanLabelTitle = linkLabel
          .replace(/\bchords\b/gi, " ")
          .replace(/\b(?:ver|version)\s*\d+\b/gi, " ")
          .replace(/\b(?:acoustic|easy|simple|live|unplugged|fingerstyle)\b/gi, " ")
          .replace(/[()[\]]/g, " ")
          .trim();
        const entryTitleTokens = tokenizeNormalized(cleanLabelTitle);
        if (entryTitleTokens.length === 0) continue;

        let titleOverlapCount = 0;
        for (const tt of titleTokens) {
          if (entryTitleTokens.includes(tt)) titleOverlapCount += 1;
        }
        const titleOverlap = titleOverlapCount / Math.max(titleTokens.length, entryTitleTokens.length);
        if (titleOverlap < 0.5) continue;

        const hitsMatch = blockText.match(/([\d,]+)\s*hits\b/i);
        const hits = hitsMatch ? parseInt(hitsMatch[1].replace(/,/g, ""), 10) || 0 : 0;
        const isStandardChords = !/\b(acoustic|easy|simple|live|unplugged|fingerstyle)\b/i.test(linkLabel);
        const score =
          titleOverlap * 12 +
          artistOverlap * 10 +
          Math.log10(hits + 10) +
          (isStandardChords && styleHint === "Original" ? 4 : 0);

        if (!candidates.some((c) => c.href === href)) {
          candidates.push({ href, label: linkLabel, score, isStandardChords });
        }
      }
      if (candidates.length > 0) break;
    } catch {
      // Try next query
    }
  }

  if (candidates.length === 0) return null;
  candidates.sort((a, b) => b.score - a.score);
  const topCandidates = candidates.slice(0, 3);

  const fetched = await Promise.all(
    topCandidates.map(async (cand) => {
      try {
        const url = `https://www.guitaretab.com${cand.href}`;
        const resp = await fetch(url, {
          headers: {
            "User-Agent": BROWSER_UA,
            Accept: "text/html,application/xhtml+xml",
            "Accept-Language": "en-US,en;q=0.9",
          },
          signal: AbortSignal.timeout(5000),
        });
        if (!resp.ok) return null;
        const pageHtml = await resp.text();
        const parsed = parseGuitareTabHtml(pageHtml, 42);
        if (!parsed) return null;
        const rank =
          cand.score * 10 +
          Math.min(parsed.lyricPairs, 20) * 3 +
          Math.min(parsed.chords.length, 8) * 2 -
          parsed.tabLineCount * 20;
        return { cand, parsed, url, rank };
      } catch {
        return null;
      }
    })
  );

  const valid = fetched.filter(Boolean).sort((a, b) => b.rank - a.rank);
  if (valid.length === 0) return null;

  const winner = valid[0];
  return {
    artist: artist || "",
    title: title || "",
    chords: winner.parsed.chords,
    lines: winner.parsed.lines,
    capo: winner.parsed.capo,
    tuning: winner.parsed.tuning,
    sourceUrl: winner.url,
  };
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
    chordMap,
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

function wrapOverLyricPair(chordLine, lyricLine, maxWidth = 42) {
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

    const lSlice = lRest.slice(splitIdx);
    const stripCount = lSlice.length - lSlice.replace(/^ +/, "").length;
    lRest = lSlice.slice(stripCount);
    const cSlice = cRest.slice(splitIdx);
    const cLeadSpaces = cSlice.length - cSlice.replace(/^ +/, "").length;
    cRest = cSlice.slice(Math.min(stripCount, cLeadSpaces));
  }

  if (cRest.trim()) out.push(cRest.trimEnd());
  if (lRest.trim()) out.push(lRest.trimEnd());
  return out;
}

function chordProToOverLyric(rawContent, maxWidth = 42) {
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

async function enrichGroundedMetadata(env, artist, title, chords, language) {
  const defaults = {
    key: normalizeKey(chords[0] || "C"),
    bpm: 120,
    difficulty: language === "es" ? "Intermedio" : "Intermediate",
    strummingPattern: "D - D U - U D U",
    practiceTip:
      language === "es"
        ? `Practica los cambios entre ${chords.slice(0, 4).join(" - ")} primero lento con metrónomo.`
        : `Practice switching between ${chords.slice(0, 4).join(" - ")} slowly with a metronome first.`,
  };

  try {
    const res = await withTimeout(
      env.AI.run(FORMATTER_MODEL_ID, {
        messages: [
          {
            role: "system",
            content:
              "You are a guitar coach. Given the verified chords of a song, return ONLY compact JSON: " +
              `{"key":"","bpm":120,"difficulty":"${language === "es" ? "Fácil|Intermedio|Avanzado" : "Easy|Intermediate|Advanced"}","strummingPattern":"D - D U - U D U","practiceTip":"1 short sentence in ${language === "es" ? "Spanish" : "English"}"}`,
          },
          {
            role: "user",
            content: `Song: ${artist} - ${title}\nVerified chords: ${chords.join(", ")}`,
          },
        ],
        max_tokens: 180,
        temperature: 0.1,
      }),
      6500
    );
    const parsed = parseAiJson(extractModelText(res));
    const rawBpm = Number(parsed?.bpm);
    return {
      key: normalizeKey(parsed?.key || defaults.key),
      bpm: Number.isFinite(rawBpm) ? Math.min(240, Math.max(40, Math.round(rawBpm))) : defaults.bpm,
      difficulty: String(parsed?.difficulty || defaults.difficulty).trim().slice(0, 40) || defaults.difficulty,
      strummingPattern:
        String(parsed?.strummingPattern || defaults.strummingPattern).trim().slice(0, 100) ||
        defaults.strummingPattern,
      practiceTip: String(parsed?.practiceTip || defaults.practiceTip).trim(),
    };
  } catch {
    return defaults;
  }
}

async function callGemini(apiKey, contents, useSearch = false) {
  const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL_ID}:generateContent`;
  const payload = {
    contents,
    generationConfig: {
      temperature: 0.1,
      maxOutputTokens: 2400,
    },
  };
  if (useSearch) {
    payload.tools = [{ google_search: {} }];
  }

  const resp = await fetch(endpoint, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "x-goog-api-key": apiKey,
    },
    body: JSON.stringify(payload),
    signal: AbortSignal.timeout(45000),
  });
  if (!resp.ok) {
    throw new Error(`GEMINI_HTTP_${resp.status}`);
  }
  const data = await resp.json();
  const text = (data?.candidates?.[0]?.content?.parts || [])
    .map((p) => p?.text || "")
    .join("\n")
    .trim();
  if (!text) {
    throw new Error("GEMINI_EMPTY");
  }
  return parseAiJson(text);
}

async function analyzeWithGemini(env, youtubeUrl, artist, title, targetDescription, language) {
  const apiKey = String(env?.GEMINI_API_KEY || "").trim();
  if (!apiKey) return null;

  const schemaPrompt = `Analyze this song for guitar chords (${artist || ""} - ${title || ""}).
Return ONLY compact valid JSON (no markdown fences):
{"title":"","artist":"","key":"e.g. Bm or F#m or G","tuning":"E Standard","bpm":120,"capo":0,"difficulty":"${language === "es" ? "Fácil|Intermedio|Avanzado" : "Easy|Intermediate|Advanced"}","strummingPattern":"e.g. D - D U - U D U","chords":["..."],"sections":{"intro":"","verse":"","preChorus":"","chorus":"","bridge":"","outro":""},"practiceTip":"1 short sentence in ${language === "es" ? "Spanish" : "English"}"}`;

  // 1. Direct YouTube multimodal audio analysis if URL is available
  if (youtubeUrl) {
    try {
      const parsed = await callGemini(
        apiKey,
        [
          {
            parts: [
              { fileData: { fileUri: youtubeUrl } },
              {
                text: `Listen carefully to the actual studio audio and guitar harmony in this YouTube video. Do NOT guess randomly—transcribe the real chords heard in the recording.\n${schemaPrompt}`,
              },
            ],
          },
        ],
        false
      );
      if (parsed && Array.isArray(parsed.chords) && parsed.chords.length > 0) {
        return { model: GEMINI_MODEL_ID, chordSource: "gemini_audio", parsed };
      }
    } catch {
      // Fallback to Google Search grounded Gemini below
    }
  }

  // 2. Google Search grounded chord lookup via Gemini 2.5 Flash
  try {
    const parsed = await callGemini(
      apiKey,
      [
        {
          parts: [
            {
              text: `${targetDescription}\nSearch the web for the verified guitar chord sheet of this exact song before answering.\n${schemaPrompt}`,
            },
          ],
        },
      ],
      true
    );
    if (parsed && Array.isArray(parsed.chords) && parsed.chords.length > 0) {
      return { model: GEMINI_MODEL_ID, chordSource: "gemini_search", parsed };
    }
  } catch {
    return null;
  }

  return null;
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
      45000
    );
    const text = extractModelText(r);
    if (String(text || "").trim()) {
      return { model: HARMONY_MODEL_ID, chordSource: "ai_estimated", parsed: parseAiJson(text) };
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
  return { model: HARMONY_FALLBACK_MODEL_ID, chordSource: "ai_estimated", parsed: parseAiJson(fallbackText) };
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
        geminiConfigured: Boolean(env?.GEMINI_API_KEY),
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
            headers: { "User-Agent": "RiffForge-AI/1.3" },
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

      try {
        // Step 1: Canonicalize artist & title (fixes typos like "escrivir todo denuevo de cuchillado")
        const identity = await resolveSongIdentity(env, rawInput, videoTitle, channelAuthor);
        const resolvedArtist = identity.artist;
        const resolvedTitle = identity.title;

        // Step 2: In parallel, try LaCuerda + GuitareTab verified chord sheets + YouTube video resolution
        const [lacuerdaSheet, guitareTabSheet, resolvedVideo] = await Promise.all([
          fetchLaCuerdaChordSheet(resolvedArtist, resolvedTitle),
          fetchGuitareTabChordSheet(resolvedArtist, resolvedTitle, styleHint),
          !videoId ? resolveYouTubeVideoForQuery(resolvedArtist, resolvedTitle, rawInput) : Promise.resolve(null),
        ]);

        if (!videoId && resolvedVideo) {
          videoId = resolvedVideo.videoId;
          youtubeUrl = resolvedVideo.youtubeUrl;
          thumbnailUrl = resolvedVideo.thumbnailUrl;
          if (!videoTitle && resolvedVideo.videoTitle) videoTitle = resolvedVideo.videoTitle;
          if (!channelAuthor && resolvedVideo.channelAuthor) channelAuthor = resolvedVideo.channelAuthor;
        }

        // ── Tier 1A / 1B: Grounded human-transcribed chord sheet (LaCuerda / GuitareTab) ──
        const groundedSheet = lacuerdaSheet
          ? { ...lacuerdaSheet, sourceKey: "lacuerda", capo: 0, tuning: "E Standard" }
          : guitareTabSheet
          ? { ...guitareTabSheet, sourceKey: "guitaretab" }
          : null;

        if (groundedSheet) {
          const finalArtist = groundedSheet.artist || resolvedArtist || channelAuthor || "Artista";
          const finalTitle = groundedSheet.title || resolvedTitle || videoTitle || "Canción";
          const meta = await enrichGroundedMetadata(env, finalArtist, finalTitle, groundedSheet.chords, language);

          let chords = groundedSheet.chords;
          let capo = Number.isFinite(groundedSheet.capo) ? groundedSheet.capo : 0;
          const tuning = groundedSheet.tuning || "E Standard";
          let difficulty = meta.difficulty;
          let practiceTip = meta.practiceTip;
          let finalLines = groundedSheet.lines;

          if (styleHint === "Simplificado") {
            const simplified = simplifyBlueprint(
              {
                title: finalTitle,
                artist: finalArtist,
                key: meta.key,
                capo,
                chords,
                sections: {},
              },
              practiceTip,
              language
            );
            capo = simplified.blueprint.capo;
            chords = simplified.blueprint.chords;
            difficulty = simplified.difficulty;
            practiceTip = simplified.practiceTip;
            finalLines = groundedSheet.lines.map((row) =>
              row.isChordLine
                ? { ...row, text: replaceChordsInProgression(row.text, simplified.chordMap) }
                : row
            );
          }

          const content = finalLines
            .map((r) => r.text)
            .join("\n")
            .replace(/\n{3,}/g, "\n\n")
            .trim();

          return jsonResponse({
            title: finalTitle,
            artist: finalArtist,
            key: meta.key,
            tuning,
            bpm: meta.bpm,
            capo,
            difficulty,
            strummingPattern: meta.strummingPattern,
            chords,
            content,
            practiceTip,
            hasLyrics: content.split("\n").length > 4,
            lyricsSource: groundedSheet.sourceKey,
            chordSource: groundedSheet.sourceKey,
            youtubeUrl,
            youtubeVideoId: videoId || "",
            thumbnailUrl,
            videoTitle,
            model: `${groundedSheet.sourceKey}-verified`,
          });
        }

        const targetDescription = videoId
          ? `YouTube Video:\n- Song/Title: "${resolvedTitle || videoTitle}"\n- Artist: "${resolvedArtist || channelAuthor}"\n- Video title: "${videoTitle}"\n- URL: ${youtubeUrl}`
          : `Song query: "${resolvedArtist ? `${resolvedArtist} - ` : ""}${resolvedTitle || rawInput}" (original input: "${rawInput}")`;

        // ── Tier 2: Gemini 2.5 Flash (YouTube multimodal audio + Search grounding) if key configured ──
        // ── Tier 3: Cloudflare Workers AI fallback ──
        const [fetchedLyrics, geminiResult] = await Promise.all([
          fetchRealLyrics(videoTitle, channelAuthor, rawInput, resolvedArtist, resolvedTitle),
          analyzeWithGemini(env, youtubeUrl, resolvedArtist, resolvedTitle, targetDescription, language),
        ]);

        const harmonyResult =
          geminiResult || (await analyzeHarmonyBlueprint(env, targetDescription, language));

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

        const title =
          String(parsed?.title || resolvedTitle || videoTitle || rawInput).trim().slice(0, 160) || "Canción IA";
        const artist =
          String(parsed?.artist || resolvedArtist || channelAuthor || "YouTube").trim().slice(0, 120) || "YouTube";
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
        const content = chordProToOverLyric(plainChordPro, 42);

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
          chordSource: harmonyResult.chordSource,
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
