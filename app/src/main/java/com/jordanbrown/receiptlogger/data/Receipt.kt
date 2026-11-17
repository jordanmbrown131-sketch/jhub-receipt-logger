package com.jordanbrown.receiptlogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One photographed receipt.
 *
 * @property photoPath absolute path of the JPEG in the app's private storage.
 * @property takenAt when the photo was taken (epoch milliseconds). Read from the photo's
 *   EXIF "date taken" metadata, falling back to the moment the camera returned the photo.
 * @property amountPence amount to claim, stored in pence to avoid floating-point rounding errors.
 * @property description optional free-text label, e.g. "Taxi to station".
 */
@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,
    val takenAt: Long,
    val amountPence: Long,
    val description: String = ""
)
