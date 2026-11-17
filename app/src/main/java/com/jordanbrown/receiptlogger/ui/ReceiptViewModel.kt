package com.jordanbrown.receiptlogger.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jordanbrown.receiptlogger.data.AppDatabase
import com.jordanbrown.receiptlogger.data.Receipt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Holds the app's state and talks to the database. Shared by all screens. */
class ReceiptViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.get(application).receiptDao()

    val receipts: StateFlow<List<Receipt>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Running total to claim, in pence. Updates automatically on add / edit / delete. */
    val totalPence: StateFlow<Long> = dao.observeTotalPence()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun receipt(id: Long): Flow<Receipt?> = dao.observeById(id)

    fun add(photoPath: String, takenAt: Long, amountPence: Long, description: String) {
        viewModelScope.launch {
            dao.insert(
                Receipt(
                    photoPath = photoPath,
                    takenAt = takenAt,
                    amountPence = amountPence,
                    description = description.trim()
                )
            )
        }
    }

    fun update(receipt: Receipt) {
        viewModelScope.launch { dao.update(receipt) }
    }

    fun delete(receipt: Receipt) {
        viewModelScope.launch {
            dao.delete(receipt)
            withContext(Dispatchers.IO) { File(receipt.photoPath).delete() }
        }
    }
}
