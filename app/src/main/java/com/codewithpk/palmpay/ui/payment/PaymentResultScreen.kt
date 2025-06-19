package com.codewithpk.palmpay.ui.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codewithpk.palmpay.ui.theme.GreenSuccess
import com.codewithpk.palmpay.ui.theme.PalmPayTheme
import com.codewithpk.palmpay.ui.theme.RedFailure
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentResultScreen(
    isSuccess: Boolean,
    amount: String,
    merchant: String,
    onDone: () -> Unit,
    onShareReceipt: () -> Unit
) {
    val backgroundColor = if (isSuccess) GreenSuccess else RedFailure
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Close
    val message = if (isSuccess) "Payment of ₹$amount received." else "Payment of ₹$amount failed."
    val detailsMessage = if (isSuccess) "Paid to $merchant" else "Transaction to $merchant failed"
    val timestamp = SimpleDateFormat("MMM dd, yyyy HH:mm a", Locale.getDefault()).format(Date())

    var visible by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { with(density) { -40.dp.roundToPx() } } + expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Status Icon",
                    modifier = Modifier.size(120.dp),
                    tint = Color.White
                )
                Spacer(Modifier.height(32.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    fontSize = 32.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = detailsMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = backgroundColor)
        ) {
            Text("Done", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onShareReceipt,
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.material3.ButtonDefaults.outlinedButtonBorder(true)
        ) {
            Text("Share Receipt", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPaymentSuccessScreen() {
    PalmPayTheme {
        PaymentResultScreen(isSuccess = true, amount = "1250", merchant = "Kiran Kirana Store", onDone = {}, onShareReceipt = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPaymentFailureScreen() {
    PalmPayTheme {
        PaymentResultScreen(isSuccess = false, amount = "500", merchant = "Coffee Shop", onDone = {}, onShareReceipt = {})
    }
}