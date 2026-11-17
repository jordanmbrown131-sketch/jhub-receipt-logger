package com.jordanbrown.receiptlogger.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    /** All receipts, newest photo first. Emits again whenever the table changes. */
    @Query("SELECT * FROM receipts ORDER BY takenAt DESC")
    fun observeAll(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    fun observeById(id: Long): Flow<Receipt?>

    /** Running total of all amounts to claim, in pence (0 when there are no receipts). */
    @Query("SELECT COALESCE(SUM(amountPence), 0) FROM receipts")
    fun observeTotalPence(): Flow<Long>

    @Insert
    suspend fun insert(receipt: Receipt): Long

    @Update
    suspend fun update(receipt: Receipt)

    @Delete
    suspend fun delete(receipt: Receipt)
}
