package com.riffforge.core.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val remoteAvatarCache = ConcurrentHashMap<String, ImageBitmap>()

@Composable
fun ProfileAvatar(
    photoData: String,
    photoUrl: String,
    size: Dp = 64.dp,
    showEditBadge: Boolean = false,
    isLoading: Boolean = false,
    badgeBorderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: (() -> Unit)? = null
) {
    val customBitmap = remember(photoData) {
        if (photoData.isBlank()) null
        else runCatching {
            val bytes = Base64.decode(photoData, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }

    var remoteBitmap by remember(photoUrl) {
        mutableStateOf(if (photoUrl.isNotBlank()) remoteAvatarCache[photoUrl] else null)
    }

    LaunchedEffect(photoUrl) {
        if (photoUrl.isBlank()) {
            remoteBitmap = null
            return@LaunchedEffect
        }
        val cached = remoteAvatarCache[photoUrl]
        if (cached != null) {
            remoteBitmap = cached
            return@LaunchedEffect
        }
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                val conn = (URL(photoUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    instanceFollowRedirects = true
                }
                conn.inputStream.use { stream ->
                    val bytes = stream.readBytes()
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                }
            }.getOrNull()
        }
        if (loaded != null) {
            remoteAvatarCache[photoUrl] = loaded
            remoteBitmap = loaded
        }
    }

    val displayBitmap = customBitmap ?: remoteBitmap
    val clickableMod = if (onClick != null && !isLoading) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .then(clickableMod),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(size * 0.42f),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.5.dp
                )
                displayBitmap != null -> Image(
                    bitmap = displayBitmap,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                else -> Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(size * 0.53f),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (showEditBadge && !isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, badgeBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar foto",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

fun encodeProfilePhotoFromUri(context: Context, uri: Uri): Result<String> = runCatching {
    val sourceBitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: error("No se pudo abrir la imagen seleccionada.")
    }

    val side = min(sourceBitmap.width, sourceBitmap.height).coerceAtLeast(1)
    val x = ((sourceBitmap.width - side) / 2).coerceAtLeast(0)
    val y = ((sourceBitmap.height - side) / 2).coerceAtLeast(0)
    val square = Bitmap.createBitmap(sourceBitmap, x, y, side, side)
    val scaled = if (side != 256) Bitmap.createScaledBitmap(square, 256, 256, true) else square

    val stream = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 78, stream)
    val encoded = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

    if (square !== sourceBitmap) square.recycle()
    if (scaled !== square && scaled !== sourceBitmap) scaled.recycle()

    require(encoded.length <= 170_000) {
        "La imagen seleccionada es demasiado pesada. Elige otra imagen."
    }
    encoded
}
