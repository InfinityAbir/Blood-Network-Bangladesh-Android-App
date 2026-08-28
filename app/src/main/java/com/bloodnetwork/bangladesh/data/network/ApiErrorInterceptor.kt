package com.bloodnetwork.bangladesh.data.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ApiErrorInterceptor(private val json: Json) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) return response

        val body = response.body?.string()
        val message = try {
            val parsed = json.parseToJsonElement(body ?: "{}")
            parsed.jsonObject["message"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        } ?: defaultMessage(response.code)

        response.close()
        throw ApiException(response.code, message)
    }

    private fun defaultMessage(code: Int): String = when (code) {
        400 -> "Invalid request. Please check your input."
        401 -> "Session expired. Please log in again."
        403 -> "You don't have permission for this action."
        404 -> "Resource not found."
        408 -> "Connection timed out. Please try again."
        409 -> "This conflicts with existing data."
        422 -> "Invalid data. Please check your input."
        429 -> "Too many requests. Please wait a moment."
        in 500..599 -> "Server error. Please try again later."
        else -> "Something went wrong. Please try again."
    }
}

class ApiException(val code: Int, override val message: String) : IOException(message)
