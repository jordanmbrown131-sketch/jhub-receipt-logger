package com.jordanbrown.receiptlogger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jordanbrown.receiptlogger.data.Receipt
import com.jordanbrown.receiptlogger.ui.ReceiptViewModel
import com.jordanbrown.receiptlogger.util.DateTimeUtil
import com.jordanbrown.receiptlogger.util.Money
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptListScreen(
    viewModel: ReceiptViewModel,
    onAddReceipt: () -> Unit,
    onOpenReceipt: (Long) -> Unit
) {
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val totalPence by viewModel.totalPence.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Receipt Logger") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddReceipt,
                icon = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) },
                text = { Text("Add receipt") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TotalCard(totalPence = totalPence, count = receipts.size)

            if (receipts.isEmpty()) {
                EmptyState(Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    // Extra bottom padding so the last receipt isn't hidden behind the button.
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(receipts, key = { it.id }) { receipt ->
                        ReceiptRow(receipt = receipt, onClick = { onOpenReceipt(receipt.id) })
                    }
                }
            }
        }
    }
}

/** The running total, pinned above the list so it is always visible. */
@Composable
private fun TotalCard(totalPence: Long, count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            // Read as one item by screen readers, and re-announced when the total changes.
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Total to claim", style = MaterialTheme.typography.titleMedium)
            Text(
                Money.format(totalPence),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                when (count) {
                    0 -> "No receipts yet"
                    1 -> "From 1 receipt"
                    else -> "From $count receipts"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ReceiptRow(receipt: Receipt, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "View receipt details", onClick = onClick)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = File(receipt.photoPath),
                contentDescription = null, // the row's text describes the receipt
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    receipt.description.ifBlank { "Receipt" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    DateTimeUtil.format(receipt.takenAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                Money.format(receipt.amountPence),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Receipt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Text("No receipts yet", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap \"Add receipt\" to photograph a receipt and record how much to claim.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
