package com.riffforge.shared.di

import com.riffforge.shared.data.createHttpClient
import io.ktor.client.*
import org.koin.dsl.module

val networkModule = module {
    factory { createHttpClient() }
}
