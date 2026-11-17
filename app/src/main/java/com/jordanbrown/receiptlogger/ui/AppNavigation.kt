package com.jordanbrown.receiptlogger.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jordanbrown.receiptlogger.ui.screens.AddReceiptScreen
import com.jordanbrown.receiptlogger.ui.screens.ReceiptDetailScreen
import com.jordanbrown.receiptlogger.ui.screens.ReceiptListScreen

private object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val DETAIL = "detail/{id}"
    fun detail(id: Long) = "detail/$id"
}

/** Three screens: list (home) -> add a receipt, list -> receipt details. */
@Composable
fun AppNavigation(viewModel: ReceiptViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            ReceiptListScreen(
                viewModel = viewModel,
                onAddReceipt = { navController.navigate(Routes.ADD) },
                onOpenReceipt = { id -> navController.navigate(Routes.detail(id)) }
            )
        }

        composable(Routes.ADD) {
            AddReceiptScreen(
                onSave = { photoPath, takenAt, amountPence, description ->
                    viewModel.add(photoPath, takenAt, amountPence, description)
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            ReceiptDetailScreen(
                receiptId = entry.arguments?.getLong("id") ?: 0L,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
