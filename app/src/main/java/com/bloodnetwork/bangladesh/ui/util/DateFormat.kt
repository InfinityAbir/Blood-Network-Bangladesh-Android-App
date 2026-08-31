package com.bloodnetwork.bangladesh.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

/** Backend ISO-8601 timestamp (e.g. "2026-08-31T16:56:55.259978Z") -> "Aug 31, 2026 · 4:56 PM"
 * in the device's local timezone. Falls back to the raw string if it isn't parseable. */
fun formatDateTime(iso: String): String = try {
    Instant.parse(iso).atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
} catch (e: DateTimeParseException) {
    iso
}

/** Same as [formatDateTime] but date-only -> "Aug 31, 2026". */
fun formatDate(iso: String): String = try {
    Instant.parse(iso).atZone(ZoneId.systemDefault()).format(dateFormatter)
} catch (e: DateTimeParseException) {
    iso
}
