package com.codewithpk.palmpay.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codewithpk.palmpay.ui.theme.PalmPayTheme
import com.codewithpk.palmpay.data.local.PalmScan
import com.codewithpk.palmpay.ui.theme.GreenSuccess
import com.codewithpk.palmpay.ui.theme.RedFailure
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Mock Data for Preview
val mockTransactions = listOf(
    PalmScan(id = 1, userId = "mock_user", scanTimestamp = System.currentTimeMillis() - 86400000, metadata = "Payment to Coffee Shop, amount: 299"),
    PalmScan(id = 2, userId = "mock_user", scanTimestamp = System.currentTimeMillis() - 172800000, metadata = "Payment to Grocery Store, amount: 550"),
    PalmScan(id = 3, userId = "mock_user", scanTimestamp = System.currentTimeMillis() - 259200000, metadata = "Failed payment to Online Service, amount: 100", imageUrl = "failed"), // Simulate failed
    PalmScan(id = 4, userId = "mock_user", scanTimestamp = System.currentTimeMillis() - 345600000, metadata = "Received from Friend, amount: 75"),
    PalmScan(id = 5, userId = "mock_user", scanTimestamp = System.currentTimeMillis() - 432000000, metadata = "Payment to Restaurant, amount: 800")
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit,
    historyViewModel: TransactionHistoryViewModel = hiltViewModel()
) {
    val transactions by historyViewModel.allPalmScans.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter dropdown (placeholder for now)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // TODO: Implement actual filter dropdowns (by date, amount, merchant)
                Text("Filters (Coming Soon)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (transactions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No transactions",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No transactions yet.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    items(transactions) { transaction ->
                        TransactionCard(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: PalmScan) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault()) }
    val (statusIcon, statusColor) = when {
        transaction.metadata?.contains("failed", ignoreCase = true) == true || transaction.imageUrl == "failed" ->
            Icons.Default.Warning to RedFailure // Simulating failed status
        else -> Icons.Default.CheckCircle to GreenSuccess
    }

    // Parse amount and merchant from metadata for display (simulated)
    val metadataParts = transaction.metadata?.split(", ")
    val merchantName = metadataParts?.find { it.contains("Payment to", ignoreCase = true) || it.contains("Received from", ignoreCase = true) }?.substringAfter(" to ")?.substringAfter(" from ") ?: "Unknown"
    val amount = metadataParts?.find { it.contains("amount:", ignoreCase = true) }?.substringAfter("amount: ") ?: "0"

    val formattedAmount = remember(transaction.amount) {
        DecimalFormat("₹#,##0.00").format(transaction.amount)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = merchantName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(transaction.scanTimestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$formattedAmount",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.size(8.dp))
                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Status",
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTransactionHistoryScreen() {
    PalmPayTheme {
        TransactionHistoryScreen(onBack = {})
    }
}