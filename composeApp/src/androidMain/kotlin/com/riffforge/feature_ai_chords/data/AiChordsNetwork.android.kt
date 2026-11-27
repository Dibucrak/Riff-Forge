package com.riffforge.feature_ai_chords.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

actual suspend fun postAiChordsJson(url: String, jsonBody: String): Result<AiHttpResponse> =
    withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 75_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            Result.success(AiHttpResponse(statusCode = code, body = body))
        } catch (e: Exception) {
            Result.failure(
                Exception("No se pudo conectar con AI Chords: ${e.message ?: "verifica tu conexión"}")
            )
        } finally {
            conn?.disconnect()
        }
    }
