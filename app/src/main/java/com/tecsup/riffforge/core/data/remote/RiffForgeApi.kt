package com.riffforge.core.data.remote

import retrofit2.http.GET

interface RiffForgeApi {

    @GET("ping")
    suspend fun ping(): String
}