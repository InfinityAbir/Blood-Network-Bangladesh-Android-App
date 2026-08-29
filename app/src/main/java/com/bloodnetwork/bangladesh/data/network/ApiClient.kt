package com.bloodnetwork.bangladesh.data.network

import com.bloodnetwork.bangladesh.BuildConfig
import com.bloodnetwork.bangladesh.data.prefs.TokenStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun create(
        baseUrl: String,
        authInterceptor: AuthInterceptor,
        tokenStore: TokenStore,
    ): BloodNetworkApi {
        // Full request/response bodies (tokens, passwords, personal data) are only ever
        // logged in debug builds - never in release, where Logcat is not a safe sink.
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttp = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(TokenRefreshInterceptor(tokenStore, baseUrl))
            .addInterceptor(ApiErrorInterceptor(json))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(BloodNetworkApi::class.java)
    }
}
