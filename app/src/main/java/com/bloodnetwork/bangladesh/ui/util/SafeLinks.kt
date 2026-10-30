package com.bloodnetwork.bangladesh.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Guards for strings that come from the server or from other users (photo URLs, the
 * admin-editable About links, phone numbers). None of them may reach an intent or an image
 * loader unless they are well-formed and use a scheme we expect.
 */
object SafeLinks {

    const val MAX_URL_LENGTH = 500

    /** Trimmed [raw] if it is a plain `https://host/...` URL without credentials, else null. */
    fun httpsUrlOrNull(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty() || value.length > MAX_URL_LENGTH || value.any { it.isWhitespace() }) return null
        val url = value.toHttpUrlOrNull() ?: return null
        if (!url.isHttps || url.username.isNotEmpty() || url.password.isNotEmpty()) return null
        return value
    }

    /** Digits (with an optional leading +) of [raw], or null when it can't be a phone number. */
    fun dialableOrNull(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        val cleaned = buildString {
            value.forEachIndexed { i, c -> if (c.isDigit() || (c == '+' && i == 0)) append(c) }
        }
        return cleaned.takeIf { it.count(Char::isDigit) in 5..15 }
    }

    fun emailOrNull(raw: String?): String? =
        raw?.trim()?.takeIf { it.length <= 254 && Patterns.EMAIL_ADDRESS.matcher(it).matches() }
}

private fun Context.startSafely(intent: Intent): Boolean = try {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}

fun Context.openHttpsUrl(url: String?): Boolean {
    val safe = SafeLinks.httpsUrlOrNull(url) ?: return false
    return startSafely(Intent(Intent.ACTION_VIEW, Uri.parse(safe)).addCategory(Intent.CATEGORY_BROWSABLE))
}

fun Context.openDialer(phone: String?): Boolean {
    val number = SafeLinks.dialableOrNull(phone) ?: return false
    return startSafely(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
}

fun Context.openEmail(email: String?, subject: String? = null, body: String? = null): Boolean {
    val address = SafeLinks.emailOrNull(email) ?: return false
    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null)).apply {
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        body?.let { putExtra(Intent.EXTRA_TEXT, it) }
    }
    return startSafely(intent)
}
