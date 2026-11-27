package com.riffforge.feature_ai_chords.data

import com.riffforge.feature_ai_chords.domain.AiChordsErrorResponse
import com.riffforge.feature_ai_chords.domain.AiChordsRequest
import com.riffforge.feature_ai_chords.domain.AiChordsResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AiChordsRepository {

    private val jsonCodec = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun extractChords(
        input: String,
        styleHint: String = "Original"
    ): Result<AiChordsResult> {
        val cleaned = input.trim()
        if (cleaned.isBlank()) {
            return Result.failure(Exception("Ingresa un enlace de YouTube o el nombre de la canción."))
        }

        val payload = jsonCodec.encodeToString(
            AiChordsRequest(
                input = cleaned,
                styleHint = styleHint,
                language = "es"
            )
        )

        val httpRes = postAiChordsJson(AI_CHORDS_ENDPOINT, payload)
        return httpRes.fold(
            onSuccess = { response ->
                if (response.statusCode !in 200..299) {
                    val serverMsg = runCatching {
                        jsonCodec.decodeFromString<AiChordsErrorResponse>(response.body).error
                    }.getOrNull()?.takeIf { it.isNotBlank() }
                    Result.failure(
                        Exception(serverMsg ?: "Error del servicio de IA (HTTP ${response.statusCode}).")
                    )
                } else {
                    runCatching {
                        val parsed = jsonCodec.decodeFromString<AiChordsResult>(response.body)
                        parsed.copy(
                            bpm = parsed.bpm.coerceIn(40, 240),
                            capo = parsed.capo.coerceIn(0, 9),
                            chords = parsed.chords.ifEmpty { listOf(parsed.key.ifBlank { "C" }) }
                        )
                    }.recoverCatching { err ->
                        throw Exception("Respuesta de IA inesperada: ${err.message ?: "JSON inválido"}")
                    }
                }
            },
            onFailure = { err ->
                Result.failure(err)
            }
        )
    }

    companion object {
        private const val AI_CHORDS_ENDPOINT = "https://riff-forge-ai.riffforge-ai.workers.dev/api/ai-chords"
    }
}
