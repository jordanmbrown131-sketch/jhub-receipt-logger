package com.jordanbrown.receiptlogger.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

/** "Amount to claim" field shared by the add and detail screens. Shows a number keypad. */
@Composable
fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        // Only allow characters that can make up an amount.
        onValueChange = { new -> if (new.all { it.isDigit() || it == '.' || it == ',' }) onValueChange(new) },
        label = { Text("Amount to claim") },
        prefix = { Text("£") },
        placeholder = { Text("0.00") },
        isError = error != null,
        supportingText = { Text(error ?: "Enter the amount shown on the receipt, e.g. 12.50") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 60) onValueChange(it) },
        label = { Text("Description (optional)") },
        placeholder = { Text("e.g. Taxi to station") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier.fillMaxWidth()
    )
}
