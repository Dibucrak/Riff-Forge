package com.riffforge.core.presentation.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

expect fun decodeBase64Image(base64: String): ImageBitmap?
expect suspend fun loadRemoteImage(url: String): ImageBitmap?

private val kmpAvatarCache = mutableMapOf<String, ImageBitmap>()

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
        if (photoData.isBlank()) null else decodeBase64Image(photoData)
    }

    var remoteBitmap by remember(photoUrl) {
        mutableStateOf(if (photoUrl.isNotBlank()) kmpAvatarCache[photoUrl] else null)
    }

    LaunchedEffect(photoUrl) {
        if (photoUrl.isBlank()) {
            remoteBitmap = null
            return@LaunchedEffect
        }
        val cached = kmpAvatarCache[photoUrl]
        if (cached != null) {
            remoteBitmap = cached
            return@LaunchedEffect
        }
        val loaded = loadRemoteImage(photoUrl)
        if (loaded != null) {
            kmpAvatarCache[photoUrl] = loaded
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
