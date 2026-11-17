package com.jordanbrown.receiptlogger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jordanbrown.receiptlogger.data.Receipt
import com.jordanbrown.receiptlogger.ui.ReceiptViewModel
import com.jordanbrown.receiptlogger.util.DateTimeUtil
import com.jordanbrown.receiptlogger.util.Money
import kotlinx.coroutines.launch
import java.io.File

/** Full-size photo, timestamp, and the option to correct the amount or delete the receipt. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    receiptId: Long,
    viewModel: ReceiptViewModel,
    onBack: () -> Unit
) {
    val receipt by remember(receiptId) { viewModel.receipt(receiptId) }
        .collectAsStateWithLifecycle(initialValue = null)
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }, enabled = receipt != null) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete receipt")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val current = receipt
        if (current == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ReceiptEditor(
                receipt = current,
                onSave = { updated -> viewModel.update(updated) },
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(padding)
            )
        }
    }

    val toDelete = receipt
    if (showDeleteDialog && toDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this receipt?") },
            text = { Text("${Money.format(toDelete.amountPence)} will be removed from your total. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete(toDelete)
                    onBack()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ReceiptEditor(
    receipt: Receipt,
    onSave: (Receipt) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var amountText by rememberSaveable(receipt.id) { mutableStateOf(Money.toInput(receipt.amountPence)) }
    var description by rememberSaveable(receipt.id) { mutableStateOf(receipt.description) }
    var amountError by rememberSaveable(receipt.id) { mutableStateOf<String?>(null) }

    val parsed = Money.parse(amountText)
    val hasChanges = description.trim() != receipt.description ||
        (parsed as? Money.ParseResult.Ok)?.pence != receipt.amountPence

    fun save() {
        when (parsed) {
            is Money.ParseResult.Error -> amountError = parsed.message
            is Money.ParseResult.Ok -> {
                onSave(receipt.copy(amountPence = parsed.pence, description = description.trim()))
                focusManager.clearFocus()
                scope.launch { snackbarHostState.showSnackbar("Changes saved") }
            }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = File(receipt.photoPath),
                contentDescription = "Photo of the receipt",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        Text(
            "Photo taken: ${DateTimeUtil.format(receipt.takenAt)}",
            style = MaterialTheme.typography.bodyLarge
        )
        AmountField(
            value = amountText,
            onValueChange = { amountText = it; amountError = null },
            error = amountError
        )
        DescriptionField(
            value = description,
            onValueChange = { description = it },
            onDone = { focusManager.clearFocus() }
        )
        Button(
            onClick = ::save,
            enabled = hasChanges,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Save changes")
        }
    }
}
