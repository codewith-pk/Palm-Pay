package com.codewithpk.palmpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.codewithpk.palmpay.ui.history.TransactionHistoryScreen
import com.codewithpk.palmpay.ui.home.HomeScreen
import com.codewithpk.palmpay.ui.login.LoginScreen
import com.codewithpk.palmpay.ui.payment.PaymentResultScreen
import com.codewithpk.palmpay.ui.profile.ProfileScreen
import com.codewithpk.palmpay.ui.scan.PalmScanForPaymentScreen
import com.codewithpk.palmpay.ui.scan.PalmScanRegistrationScreen
import com.codewithpk.palmpay.ui.splash.SplashScreen
import com.codewithpk.palmpay.ui.theme.PalmPayTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PalmPayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PalmPayApp()
                }
            }
        }
    }
}

// Define all app routes and argument keys
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val PALM_REGISTRATION_PROMPT = "palm_registration_prompt"
    const val PALM_REGISTRATION_SCAN = "palm_registration_scan"
    const val HOME = "home"
    const val GET_PAYMENT_DIALOG = "get_payment_dialog" // This will be a dialog route
    const val PALM_SCAN_FOR_PAYMENT = "palm_scan_for_payment"
    const val PAYMENT_RESULT = "payment_result/{isSuccess}/{amount}/{merchant}"
    fun paymentResultRoute(isSuccess: Boolean, amount: String, merchant: String) =
        "payment_result/$isSuccess/$amount/$merchant"

    const val TRANSACTION_HISTORY = "transaction_history"
    const val PROFILE = "profile"
}

@Composable
fun PalmPayApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(onTimeout = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) {
                        inclusive = true
                    }
                }
            })
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onBiometricSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBiometricUnavailable = { }
            )
        }

        composable(Routes.PALM_REGISTRATION_PROMPT) {
            // This dialog will automatically show after login if palm not registered
            // It will then navigate to PALM_REGISTRATION_SCAN or HOME
            // We'll handle this as a dialog launched from the HomeScreen or an initial check
        }
        composable(Routes.PALM_REGISTRATION_SCAN) {
            PalmScanRegistrationScreen(
                onRegistrationComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                },
                onCancel = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                onScanToPay = { navController.navigate(Routes.PALM_SCAN_FOR_PAYMENT) },
                onNavigateToHistory = { navController.navigate(Routes.TRANSACTION_HISTORY) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onInitiatePayment = { /* Handled via dialog in HomeScreen */ }
            )
        }
        // This is a dialog, not a full screen. Will be launched from HomeScreen.


        composable(Routes.PALM_SCAN_FOR_PAYMENT) {
            PalmScanForPaymentScreen(
                onPaymentProcessed = { isSuccess, amount, merchant ->
                    navController.navigate(Routes.paymentResultRoute(isSuccess, amount, merchant)) {
                        popUpTo(Routes.HOME)
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.PAYMENT_RESULT,
            arguments = listOf(
                navArgument("isSuccess") { type = NavType.BoolType },
                navArgument("amount") { type = NavType.StringType },
                navArgument("merchant") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            val amount = backStackEntry.arguments?.getString("amount") ?: ""
            val merchant = backStackEntry.arguments?.getString("merchant") ?: ""
            PaymentResultScreen(
                isSuccess = isSuccess,
                amount = amount,
                merchant = merchant,
                onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                    }
                },
                onShareReceipt = { /* TODO: Implement sharing logic */ }
            )
        }
        composable(Routes.TRANSACTION_HISTORY) {
            TransactionHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onReRegisterPalm = { navController.navigate(Routes.PALM_REGISTRATION_SCAN) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    PalmPayTheme {
        PalmPayApp()
    }
}