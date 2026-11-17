package com.jordanbrown.receiptlogger.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtil {

    private val displayFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.UK)
    private val exifFormat = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss", Locale.US)

    /** Epoch millis -> "17 Nov 2026, 10:14" in the device's time zone. */
    fun format(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        displayFormat.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

    /**
     * Parses an EXIF DateTimeOriginal value ("2026:11:17 10:14:05", local time) to epoch millis.
     * Returns null if the value is missing or malformed.
     */
    fun parseExif(value: String?, zone: ZoneId = ZoneId.systemDefault()): Long? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDateTime.parse(value.trim(), exifFormat).atZone(zone).toInstant().toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }
}
