package com.jordanbrown.receiptlogger.ui.screens

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jordanbrown.receiptlogger.util.DateTimeUtil
import com.jordanbrown.receiptlogger.util.Money
import com.jordanbrown.receiptlogger.util.PhotoStorage
import java.io.File

/**
 * Take a photo of a receipt, enter the amount to claim, save.
 * The camera opens straight away to save the user a tap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReceiptScreen(
    onSave: (photoPath: String, takenAt: Long, amountPence: Long, description: String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // rememberSaveable keeps all of this through screen rotation.
    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var takenAt by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraOpenedOnce by rememberSaveable { mutableStateOf(false) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraError by rememberSaveable { mutableStateOf<String?>(null) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val newFile = pendingPath?.let { File(it) }
        pendingPath = null
        if (success && newFile != null && newFile.length() > 0) {
            photoPath?.let { File(it).delete() } // replacing a previous photo (retake)
            photoPath = newFile.absolutePath
            // Timestamp: the photo's own "date taken" metadata, else the time the camera returned it.
            takenAt = PhotoStorage.readTakenAt(newFile) ?: System.currentTimeMillis()
            cameraError = null
        } else {
            newFile?.delete() // user cancelled; don't leave an empty file behind
        }
    }

    fun openCamera() {
        val file = PhotoStorage.newPhotoFile(context)
        pendingPath = file.absolutePath
        try {
            cameraLauncher.launch(PhotoStorage.uriFor(context, file))
        } catch (e: ActivityNotFoundException) {
            file.delete()
            pendingPath = null
            cameraError = "No camera app was found on this device."
        }
    }

    fun discardAndClose() {
        photoPath?.let { File(it).delete() }
        onClose()
    }

    fun requestClose() {
        if (photoPath != null) showDiscardDialog = true else onClose()
    }

    fun save() {
        val path = photoPath ?: return
        when (val result = Money.parse(amountText)) {
            is Money.ParseResult.Error -> amountError = result.message
            is Money.ParseResult.Ok -> onSave(path, takenAt ?: System.currentTimeMillis(), result.pence, description)
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraOpenedOnce) {
            cameraOpenedOnce = true
            openCamera()
        }
    }

    BackHandler(onBack = ::requestClose)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New receipt") },
                navigationIcon = {
                    IconButton(onClick = ::requestClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("1. Photo of receipt", style = MaterialTheme.typography.titleMedium)

            val currentPhoto = photoPath
            if (currentPhoto == null) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No photo yet", style = MaterialTheme.typography.bodyLarge)
                        cameraError?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = ::openCamera) { Text("Take photo") }
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = File(currentPhoto),
                        contentDescription = "Photo of the receipt",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .heightIn(max = 320.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                takenAt?.let {
                    Text(
                        "Photo taken: ${DateTimeUtil.format(it)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = ::openCamera, modifier = Modifier.fillMaxWidth()) {
                    Text("Retake photo")
                }
            }

            Text("2. Amount to claim", style = MaterialTheme.typography.titleMedium)
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
                enabled = photoPath != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (photoPath == null) "Take a photo to continue" else "Save receipt")
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard this receipt?") },
            text = { Text("The photo and amount you've entered won't be saved.") },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; discardAndClose() }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") }
            }
        )
    }
}
