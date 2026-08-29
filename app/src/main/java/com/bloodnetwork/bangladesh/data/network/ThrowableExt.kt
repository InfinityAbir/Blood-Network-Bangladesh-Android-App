package com.bloodnetwork.bangladesh.data.network

import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Central place to turn any caught [Throwable] into copy the user should actually see.
 * [ApiException]'s message is already a clean, backend-provided (or status-mapped) string —
 * see [ApiErrorInterceptor]. Everything else (no internet, DNS failure, timeout, an
 * unexpected serialization/runtime error) is a raw technical message that must never be
 * shown as-is, so it's mapped to a generic explanation instead.
 */
fun Throwable.toDisplayMessage(fallback: String = "Something went wrong. Please try again."): String = when (this) {
    is ApiException -> message
    is UnknownHostException -> "No internet connection. Please check your network."
    is SocketTimeoutException -> "Connection timed out. Please try again."
    is java.io.IOException -> "Network error. Please check your connection and try again."
    else -> fallback
}
