package com.riffforge.feature_ai_chords.data.repository

import com.riffforge.feature_ai_chords.domain.model.AiChordsResult
import com.riffforge.feature_ai_chords.domain.repository.AiChordsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AiChordsRepositoryImpl @Inject constructor() : AiChordsRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun extractChords(
        input: String,
        styleHint: String
    ): Result<AiChordsResult> = withContext(Dispatchers.IO) {
        val cleanedInput = input.trim()
        if (cleanedInput.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Ingresa un enlace de YouTube o el nombre de la canción.")
            )
        }

        try {
            val payload = JSONObject().apply {
                put("input", cleanedInput)
                put("styleHint", styleHint)
                put("language", "es")
            }
            val request = Request.Builder()
                .url(AI_CHORDS_ENDPOINT)
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val serverMsg = runCatching {
                        JSONObject(rawBody).optString("error")
                    }.getOrNull()?.takeIf { it.isNotBlank() }
                    return@withContext Result.failure(
                        Exception(serverMsg ?: "Error del servicio de IA (HTTP ${response.code}).")
                    )
                }

                val json = JSONObject(rawBody)
                val chordsArray = json.optJSONArray("chords") ?: JSONArray()
                val chords = buildList {
                    for (i in 0 until chordsArray.length()) {
                        val chord = chordsArray.optString(i).trim()
                        if (chord.isNotBlank()) add(chord)
                    }
                }

                Result.success(
                    AiChordsResult(
                        title = json.optString("title", "Canción IA").ifBlank { "Canción IA" },
                        artist = json.optString("artist", "YouTube").ifBlank { "YouTube" },
                        key = json.optString("key", "C").ifBlank { "C" },
                        tuning = json.optString("tuning", "E Standard").ifBlank { "E Standard" },
                        bpm = json.optInt("bpm", 120).coerceIn(40, 240),
                        capo = json.optInt("capo", 0).coerceIn(0, 9),
                        difficulty = json.optString("difficulty", "Intermedio").ifBlank { "Intermedio" },
                        strummingPattern = json.optString("strummingPattern", "D - D U - U D U"),
                        chords = chords.ifEmpty { listOf(json.optString("key", "C").ifBlank { "C" }) },
                        content = json.optString("content", ""),
                        practiceTip = json.optString("practiceTip", ""),
                        youtubeUrl = json.optString("youtubeUrl", ""),
                        youtubeVideoId = json.optString("youtubeVideoId", ""),
                        thumbnailUrl = json.optString("thumbnailUrl", ""),
                        videoTitle = json.optString("videoTitle", ""),
                        model = json.optString("model", "")
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(
                Exception("No se pudo conectar con AI Chords: ${e.localizedMessage ?: "verifica tu conexión a internet"}")
            )
        }
    }

    companion object {
        private const val AI_CHORDS_ENDPOINT = "https://riff-forge-ai.riffforge-ai.workers.dev/api/ai-chords"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
