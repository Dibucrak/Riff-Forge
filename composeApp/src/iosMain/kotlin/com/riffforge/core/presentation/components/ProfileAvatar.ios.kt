package com.riffforge.core.presentation.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.create
import platform.Foundation.dataTaskWithURL
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

actual fun decodeBase64Image(base64: String): ImageBitmap? = runCatching {
    val nsData = NSData.create(base64EncodedString = base64, options = 0u) ?: return null
    val bytes = nsData.toByteArray()
    if (bytes.isEmpty()) return null
    Image.makeFromEncoded(bytes).toComposeImageBitmap()
}.getOrNull()

actual suspend fun loadRemoteImage(url: String): ImageBitmap? = suspendCancellableCoroutine { cont ->
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl == null) {
        if (cont.isActive) cont.resume(null)
        return@suspendCancellableCoroutine
    }
    val task = NSURLSession.sharedSession.dataTaskWithURL(nsUrl) { data, _, _ ->
        val bitmap = if (data != null) {
            runCatching {
                val bytes = data.toByteArray()
                if (bytes.isNotEmpty()) Image.makeFromEncoded(bytes).toComposeImageBitmap() else null
            }.getOrNull()
        } else null
        if (cont.isActive) cont.resume(bitmap)
    }
    cont.invokeOnCancellation { task.cancel() }
    task.resume()
}
