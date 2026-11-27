package com.riffforge.feature_ai_chords.data

import kotlin.coroutines.resume
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size <= 0) return ByteArray(0)
    val out = ByteArray(size)
    out.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return out
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}

actual suspend fun postAiChordsJson(url: String, jsonBody: String): Result<AiHttpResponse> =
    suspendCancellableCoroutine { cont ->
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            if (cont.isActive) cont.resume(Result.failure(Exception("URL de AI Chords inválida.")))
            return@suspendCancellableCoroutine
        }
        val request = NSMutableURLRequest.requestWithURL(nsUrl).apply {
            setHTTPMethod("POST")
            setValue("application/json; charset=utf-8", forHTTPHeaderField = "Content-Type")
            setValue("application/json", forHTTPHeaderField = "Accept")
            setHTTPBody(jsonBody.encodeToByteArray().toNSData())
        }
        val config = NSURLSessionConfiguration.defaultSessionConfiguration.apply {
            timeoutIntervalForRequest = 75.0
            timeoutIntervalForResource = 75.0
        }
        val session = NSURLSession.sessionWithConfiguration(config)
        val task = session.dataTaskWithRequest(request) { data, response, error ->
            if (!cont.isActive) return@dataTaskWithRequest
            if (error != null) {
                cont.resume(
                    Result.failure(
                        Exception("No se pudo conectar con AI Chords: ${error.localizedDescription}")
                    )
                )
                return@dataTaskWithRequest
            }
            val statusCode = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
            val body = data?.toByteArray()?.decodeToString().orEmpty()
            cont.resume(Result.success(AiHttpResponse(statusCode = statusCode, body = body)))
        }
        cont.invokeOnCancellation { task.cancel() }
        task.resume()
    }
