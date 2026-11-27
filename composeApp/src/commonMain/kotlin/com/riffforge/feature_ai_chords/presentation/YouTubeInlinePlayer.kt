package com.riffforge.feature_ai_chords.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal val KMP_VALID_VIDEO_ID_REGEX = Regex("^[A-Za-z0-9_-]{11}$")

internal fun buildYouTubeEmbedHtml(safeVideoId: String): String =
    """
    <!DOCTYPE html>
    <html>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          html, body { width: 100%; height: 100%; background: #000000; overflow: hidden; }
          iframe { width: 100%; height: 100%; border: 0; display: block; }
        </style>
      </head>
      <body>
        <iframe
          src="https://www.youtube.com/embed/$safeVideoId?playsinline=1&rel=0&modestbranding=1"
          title="YouTube video player"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          referrerpolicy="strict-origin-when-cross-origin"
          allowfullscreen>
        </iframe>
      </body>
    </html>
    """.trimIndent()

@Composable
expect fun YouTubeInlinePlayer(
    videoId: String,
    videoTitle: String,
    youtubeUrl: String,
    modifier: Modifier = Modifier
)
