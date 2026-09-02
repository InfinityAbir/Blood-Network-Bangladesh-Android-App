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
        // Matches ApiClient's timeouts - the backend can be cold-starting (Render free
        // tier) on this call just as easily as on the request that triggered the 401.
        .connectTimeout(75, TimeUnit.SECONDS)
        .readTimeout(75, TimeUnit.SECONDS)
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

            return when (val result = runBlocking { doRefresh(refreshToken) }) {
                is RefreshResult.Success -> {
                    runBlocking { tokenStore.saveSession(result.auth) }
                    val newRequest = request.newBuilder()
                        .header("Authorization", "Bearer ${result.auth.accessToken}")
                        .build()
                    chain.proceed(newRequest)
                }

                // The server actively rejected the refresh token (expired, revoked, reused).
                // This is the only case where the session is genuinely unrecoverable, so it's
                // the only case that may sign the user out.
                RefreshResult.Rejected -> {
                    runBlocking { tokenStore.clear() }
                    chain.proceed(request)
                }

                // Couldn't reach the server, or it failed on its own account (cold start on
                // Render's free tier, 5xx, rate limit). Saying nothing about the refresh
                // token's validity — keep the session and let this one call fail. Wiping it
                // here is what used to log people out over a transient network blip.
                RefreshResult.Transient -> chain.proceed(request)
            }
        }
    }

    /**
     * Outcome of a refresh attempt. The distinction matters: only [Rejected] means the
     * refresh token itself is no longer good. Collapsing every failure into "signed out"
     * is what made a flaky network look like an expired session.
     */
    private sealed interface RefreshResult {
        data class Success(val auth: com.bloodnetwork.bangladesh.data.model.AuthResponse) : RefreshResult
        data object Rejected : RefreshResult
        data object Transient : RefreshResult
    }

    private suspend fun doRefresh(refreshToken: String): RefreshResult {
        return try {
            val body = """{"refreshToken":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaType())
            val request = okhttp3.Request.Builder()
                .url("${baseUrl}api/auth/refresh")
                .post(body)
                .build()
            plainClient.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        val text = response.body?.string()
                        if (text.isNullOrEmpty()) {
                            RefreshResult.Transient
                        } else {
                            val json = kotlinx.serialization.json.Json {
                                ignoreUnknownKeys = true
                                explicitNulls = false
                            }
                            runCatching {
                                json.decodeFromString<com.bloodnetwork.bangladesh.data.model.AuthResponse>(text)
                            }.fold(
                                onSuccess = { RefreshResult.Success(it) },
                                // A success status we couldn't parse says nothing about the
                                // token — don't sign the user out over a parse failure.
                                onFailure = { RefreshResult.Transient },
                            )
                        }
                    }
                    // The refresh endpoint answers 401 (and only 401) when the token is
                    // expired, revoked, or reused. Everything else — 429 from the auth
                    // rate limiter, 5xx, a proxy error — is not the token's fault.
                    response.code == 401 -> RefreshResult.Rejected
                    else -> RefreshResult.Transient
                }
            }
        } catch (_: Exception) {
            // Timeout, DNS failure, connection reset, cold-starting backend.
            RefreshResult.Transient
        }
    }
}
