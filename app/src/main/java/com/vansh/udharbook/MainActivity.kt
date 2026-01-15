package com.vansh.udharbook

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vansh.udharbook.data.AppDatabase
import com.vansh.udharbook.data.Business
import com.vansh.udharbook.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val customerDao = database.customerDao()
        val prefs = getSharedPreferences("udharbook_prefs", Context.MODE_PRIVATE)

        // 1. Check if PIN exists
        val savedPin = prefs.getString("app_pin", null)
        val startRoute = if (savedPin != null) "app_lock" else "home"

        // Safe Initialization
        // Safe Initialization of Default Business
        lifecycleScope.launch {
            val businesses = customerDao.getAllBusinesses().first()
            if (businesses.isEmpty()) {
                // CHANGE: Default to "My Dairy" and enable "Dairy" mode automatically
                customerDao.insertBusiness(Business(name = "My Dairy", category = "Dairy"))
            }
        }

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = startRoute, // <--- Dynamic Start Destination
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) }
            ) {
                // LOCK SCREEN ROUTE
                composable("app_lock") {
                    AppLockScreen(navController, savedPin ?: "")
                }

                composable("home") {
                    HomeScreen(navController, customerDao)
                }
                composable(
                    "add_party/{businessId}",
                    arguments = listOf(navArgument("businessId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val businessId = backStackEntry.arguments?.getInt("businessId") ?: return@composable
                    AddPartyScreen(navController, customerDao, businessId)
                }
                composable(
                    "customer_details/{customerId}",
                    arguments = listOf(navArgument("customerId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getInt("customerId") ?: return@composable
                    CustomerDetailsScreen(navController, customerDao, customerId)
                }
                composable(
                    "customer_profile/{customerId}",
                    arguments = listOf(navArgument("customerId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getInt("customerId") ?: return@composable
                    CustomerProfileScreen(navController, customerDao, customerId)
                }
                composable(
                    "settings/{businessId}",
                    arguments = listOf(navArgument("businessId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val businessId = backStackEntry.arguments?.getInt("businessId") ?: return@composable
                    SettingsScreen(navController, customerDao, businessId)
                }
                composable(
                    "cashbook/{businessId}",
                    arguments = listOf(navArgument("businessId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val businessId = backStackEntry.arguments?.getInt("businessId") ?: return@composable
                    CashbookScreen(navController, customerDao, businessId)
                }
                composable(
                    "due_list/{businessId}",
                    arguments = listOf(navArgument("businessId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val businessId = backStackEntry.arguments?.getInt("businessId") ?: return@composable
                    DueListScreen(navController, customerDao, businessId)
                }
            }
        }
    }
}