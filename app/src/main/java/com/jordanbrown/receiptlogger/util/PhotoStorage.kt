package com.jordanbrown.receiptlogger.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.util.UUID

/** Receipt photos live in the app's private storage, so other apps can't see them. */
object PhotoStorage {

    private fun receiptsDir(context: Context): File =
        File(context.filesDir, "receipts").apply { mkdirs() }

    fun newPhotoFile(context: Context): File =
        File(receiptsDir(context), "receipt_${UUID.randomUUID()}.jpg")

    /** A content:// URI the camera app is allowed to write the photo into. */
    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** The time the photo was taken, from its EXIF metadata, or null if the camera didn't record it. */
    fun readTakenAt(file: File): Long? = try {
        val exif = ExifInterface(file.absolutePath)
        DateTimeUtil.parseExif(
            exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
        )
    } catch (e: Exception) {
        null
    }
}
