package com.riffforge.shared.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
