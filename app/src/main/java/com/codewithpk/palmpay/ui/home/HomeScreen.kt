package com.codewithpk.palmpay.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.codewithpk.palmpay.Routes
import com.codewithpk.palmpay.data.local.EnrolledPalm
import com.codewithpk.palmpay.ui.components.GetPaymentDialog
import com.codewithpk.palmpay.ui.components.PalmRegistrationPrompt
import com.codewithpk.palmpay.ui.scan.ScanViewModel
import com.codewithpk.palmpay.ui.theme.GreenSuccess
import com.codewithpk.palmpay.ui.theme.PalmPayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onScanToPay: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onInitiatePayment: (amount: String) -> Unit = {} // This will be handled by dialog
) {
    val scanViewModel: ScanViewModel = hiltViewModel()
    val enrolledPalm by scanViewModel.enrolledPalm.collectAsState(initial = null)
    var showRegistrationPrompt by remember { mutableStateOf(false) }
    var showGetPaymentDialog by remember { mutableStateOf(false) }

    // Conditional display of registration prompt
    if (enrolledPalm == null) {
        showRegistrationPrompt = true // Always show if no palm is registered for demo
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PalmPay Lite", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                actions = {
                    IconButton(onClick = { /* TODO: Notification screen */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { onNavigateToProfile() }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = { /* Already on Home */ }) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                        }
                        Text("Home", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    // Scan - Central CTA
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        IconButton(
                            onClick = { showGetPaymentDialog = true }, // Show dialog to enter amount
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .clip(CircleShape)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
                        }
                        Text("Get Payment", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    // History
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("History", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Wallet Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = "Wallet Icon",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "₹1,250.00", // Mock balance
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Optional Info Tiles (mock data)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoTile(title = "Scans Made", value = "25", icon = Icons.Default.QrCodeScanner, accentColor = MaterialTheme.colorScheme.primary)
                InfoTile(title = "Last Payment", value = "₹150 (Received)", icon = Icons.Default.History, accentColor = GreenSuccess)
            }
        }
    }

    // Show Palm Registration Prompt if needed
    if (showRegistrationPrompt && enrolledPalm == null) {
        PalmRegistrationPrompt(
            onRegisterNow = {
                navController.navigate(Routes.PALM_REGISTRATION_SCAN) // Go to registration screen
                showRegistrationPrompt = false
            },
            onLearnMore = {
                // Handle "Learn More" action, e.g., show an info dialog or navigate to help
                Toast.makeText(navController.context, "Learn more about palm registration...", Toast.LENGTH_SHORT).show()
                showRegistrationPrompt = false // Dismiss for now
            },
            onDismiss = { showRegistrationPrompt = false } // Dismiss if user closes
        )
    }

    // Show Get Payment Dialog if triggered by Bottom Nav
    if (showGetPaymentDialog) {
        GetPaymentDialog(
            onDismiss = { showGetPaymentDialog = false },
            onPaymentAmountEntered = { amount ->
                showGetPaymentDialog = false
                // Navigate to palm scan for payment screen with amount as argument
                navController.navigate(Routes.PALM_SCAN_FOR_PAYMENT)
            }
        )
    }
}

@Composable
fun InfoTile(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accentColor: Color? = null) {
    Card(
        modifier = Modifier
            .size(150.dp, 100.dp) // Fixed size for tiles
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = accentColor ?: MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor ?: MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    PalmPayTheme {
        HomeScreen(
            navController = rememberNavController(),
            onScanToPay = {},
            onNavigateToHistory = {},
            onNavigateToProfile = {}
        )
    }
}