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
  return JSON.parse(text);
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

      const targetDescription = videoId
        ? `Video de YouTube:\n- Título del video: "${videoTitle}"\n- Canal/Artista en YouTube: "${channelAuthor}"\n- URL: ${youtubeUrl}`
        : `Canción / consulta ingresada por el músico: "${rawInput}"`;

      const systemPrompt =
        "Eres un guitarrista profesional y director musical experto en armonía para guitarra. " +
        "Responde ÚNICAMENTE con un objeto JSON válido (sin markdown ni texto extra). " +
        "Usa cifrado americano estándar para los nombres de acordes (C, Am, Fmaj7, Dsus4, E7, F#m, Bb) porque la app los transpone automáticamente. " +
        "NO reproduzcas letras completas con copyright; usa únicamente guías breves de estructura musical y frases cortas de referencia con acordes entre corchetes como [Em7] [G] [Dsus4] [A7sus4].";

      const userPrompt = `${targetDescription}
Estilo/Enfoque solicitado: ${styleHint}
Idioma de explicaciones: ${language === "es" ? "Español" : "English"}

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
  "content": "[Intro]\\n[Em7] [G] [Dsus4] [A7sus4]\\n\\n[Verso]\\n[Em7] Entrada suave [G] marca el compás\\n[Dsus4] Mantén la tensión [A7sus4] antes de resolver\\n\\n[Coro]\\n[Cadd9] [G] [D] [Em7] (rasgueo abierto)",
  "practiceTip": "Consejo práctico en 1-2 oraciones en español (escala sugerida para improvisar, dinámica o digitación clave)."
}`;

      try {
        const aiResult = await env.AI.run(MODEL_ID, {
          messages: [
            { role: "system", content: systemPrompt },
            { role: "user", content: userPrompt },
          ],
          max_tokens: 1400,
          temperature: 0.25,
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
