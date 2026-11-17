package com.jordanbrown.receiptlogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jordanbrown.receiptlogger.ui.AppNavigation
import com.jordanbrown.receiptlogger.ui.ReceiptViewModel
import com.jordanbrown.receiptlogger.ui.theme.ReceiptLoggerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReceiptLoggerTheme {
                val viewModel: ReceiptViewModel = viewModel()
                AppNavigation(viewModel)
            }
        }
    }
}
