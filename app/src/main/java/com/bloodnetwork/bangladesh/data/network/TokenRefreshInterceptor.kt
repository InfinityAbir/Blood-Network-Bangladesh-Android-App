package com.bloodnetwork.bangladesh.data.network

import com.bloodnetwork.bangladesh.data.model.RefreshTokenRequest
import com.bloodnetwork.bangladesh.data.prefs.TokenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit

class TokenRefreshInterceptor(
    private val tokenStore: TokenStore,
) : Interceptor {

    private val isRefreshing = ThreadLocal<Boolean>()

    private val plainClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code != 401) return response
        if (isRefreshing.get() == true) return response

        val refreshToken: String = runBlocking { tokenStore.refreshToken.first() } ?: return response
        if (refreshToken.isEmpty()) return response

        response.close()

        try {
            isRefreshing.set(true)
            val auth = runBlocking { doRefresh(refreshToken) }
            if (auth != null) {
                runBlocking { tokenStore.saveSession(auth) }
                val newRequest = chain.request().newBuilder()
                    .header("Authorization", "Bearer ${auth.accessToken}")
                    .build()
                return chain.proceed(newRequest)
            }
        } finally {
            isRefreshing.set(false)
        }

        runBlocking { tokenStore.clear() }
        return chain.proceed(chain.request())
    }

    private suspend fun doRefresh(refreshToken: String): com.bloodnetwork.bangladesh.data.model.AuthResponse? {
        return try {
            val baseUrl = "https://blood-network-bangladesh.onrender.com/"
            val body = """{"refreshToken":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaType())
            val request = okhttp3.Request.Builder()
                .url("${baseUrl}api/auth/refresh")
                .post(body)
                .build()
            val response = plainClient.newCall(request).execute()
            if (response.isSuccessful) {
                val text = response.body?.string() ?: return null
                val json = kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
                json.decodeFromString<com.bloodnetwork.bangladesh.data.model.AuthResponse>(text)
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
