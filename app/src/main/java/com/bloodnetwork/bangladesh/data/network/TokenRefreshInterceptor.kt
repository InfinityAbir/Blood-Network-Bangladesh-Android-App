package com.bloodnetwork.bangladesh.data.network

import com.bloodnetwork.bangladesh.data.prefs.TokenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

class TokenRefreshInterceptor(
    private val tokenStore: TokenStore,
    private val baseUrl: String,
) : Interceptor {

    // Serializes refresh attempts across OkHttp's dispatcher thread pool. A ThreadLocal
    // guard only stops a thread from re-entering itself; concurrent 401s on different
    // threads would each start their own refresh call and race on rotating the refresh
    // token. This lock ensures only one refresh network call is ever in flight, and
    // every other thread that was waiting simply retries with the token it produced.
    private val refreshLock = Any()

    private val plainClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code != 401) return response
        response.close()

        synchronized(refreshLock) {
            val tokenAtRequestTime = request.header("Authorization")?.removePrefix("Bearer ")
            val latestToken = runBlocking { tokenStore.currentAccessToken() }

            // Another thread already refreshed while we were waiting on the lock:
            // just retry with the token it produced instead of refreshing again.
            if (!latestToken.isNullOrEmpty() && latestToken != tokenAtRequestTime) {
                val retryRequest = request.newBuilder()
                    .header("Authorization", "Bearer $latestToken")
                    .build()
                return chain.proceed(retryRequest)
            }

            val refreshToken = runBlocking { tokenStore.refreshToken.first() }
            if (refreshToken.isNullOrEmpty()) return chain.proceed(request)

            val auth = runBlocking { doRefresh(refreshToken) }
            if (auth != null) {
                runBlocking { tokenStore.saveSession(auth) }
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer ${auth.accessToken}")
                    .build()
                return chain.proceed(newRequest)
            }

            runBlocking { tokenStore.clear() }
            return chain.proceed(request)
        }
    }

    private suspend fun doRefresh(refreshToken: String): com.bloodnetwork.bangladesh.data.model.AuthResponse? {
        return try {
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
