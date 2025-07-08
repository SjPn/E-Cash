package com.example.budget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.budget.ui.screens.AddTransactionScreen
import com.example.budget.ui.screens.EditTransactionScreen
import com.example.budget.ui.screens.HomeScreen
import com.example.budget.ui.theme.BudgetTheme
import com.example.budget.ui.viewmodel.BudgetViewModel
import com.example.budget.ui.viewmodel.BudgetViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: BudgetViewModel by viewModels {
        BudgetViewModelFactory((application as BudgetApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BudgetAppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun BudgetAppNavigation(viewModel: BudgetViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            "add?type={type}",
            arguments = listOf(navArgument("type") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            AddTransactionScreen(
                navController = navController,
                viewModel = viewModel,
                transactionType = backStackEntry.arguments?.getString("type")
            )
        }
        composable(
            "edit/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) { backStackEntry ->
            EditTransactionScreen(
                navController = navController,
                viewModel = viewModel,
                transactionId = backStackEntry.arguments?.getInt("transactionId")
            )
        }
        // TODO: Add other screens here
    }
} 