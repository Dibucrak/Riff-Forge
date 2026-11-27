package com.riffforge.feature_ai_chords.data

data class AiHttpResponse(
    val statusCode: Int,
    val body: String
)

expect suspend fun postAiChordsJson(url: String, jsonBody: String): Result<AiHttpResponse>
