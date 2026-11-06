package com.example.dattcnpm.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun MMRANavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MMRADestinations.HOME,
        modifier = modifier
    ) {
        composable(MMRADestinations.HOME) {
            PlaceholderScreen(
                title = "Trang chủ",
                actionLabel = "Mở tài khoản",
                onActionClick = {
                    navController.navigate(MMRADestinations.MY_ACCOUNT)
                }
            )
        }

        composable(MMRADestinations.MEDICINE) {
            PlaceholderScreen(title = "Thuốc")
        }

        composable(MMRADestinations.SCHEDULE) {
            PlaceholderScreen(title = "Lịch uống")
        }

        composable(MMRADestinations.SCAN) {
            PlaceholderScreen(
                title = "Quét thuốc",
                actionLabel = "Quay lại",
                onActionClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(MMRADestinations.HISTORY) {
            PlaceholderScreen(title = "Lịch sử")
        }

        composable(MMRADestinations.FAMILY) {
            PlaceholderScreen(title = "Gia đình")
        }

        composable(MMRADestinations.ACCOUNT) {
            PlaceholderScreen(
                title = "Tài khoản",
                actionLabel = "Quay lại",
                onActionClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(MMRADestinations.MY_ACCOUNT) {
            PlaceholderScreen(
                title = "Tài khoản của tôi",
                actionLabel = "Quay lại",
                onActionClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )

        if (actionLabel != null) {
            TextButton(onClick = onActionClick) {
                Text(actionLabel)
            }
        }
    }
}