package com.codewithpk.palmpay.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codewithpk.palmpay.ui.theme.PalmPayTheme
import java.text.DecimalFormat

@Composable
fun GetPaymentDialog(
    onDismiss: () -> Unit,
    onPaymentAmountEntered: (amount: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val currencyFormatter = remember { DecimalFormat("₹#,##0.00") } // For Indian Rupee formatting

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Payment Amount") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        // Basic validation to allow only digits and one decimal point
                        val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                        if (filteredValue.count { it == '.' } <= 1) {
                            amountText = filteredValue
                        }
                    },
                    label = { Text("Amount") },
                    leadingIcon = { Text("₹") }, // Rupee symbol
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                // Optional: show formatted value
                if (amountText.isNotEmpty() && amountText.matches(Regex("^-?\\d+(\\.\\d{1,2})?\$"))) {

                    val formatted = currencyFormatter.format(amountText.toDouble())
                    Text(text = "Will pay: $formatted", style = MaterialTheme.typography.labelSmall)

                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountText.toDoubleOrNull() != null && amountText.toDouble() > 0) {
                        onPaymentAmountEntered(amountText)
                    } else {
                        // Show a toast or error message for invalid amount
                    }
                },
                enabled = amountText.toDoubleOrNull() != null && amountText.toDouble() > 0
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewGetPaymentDialog() {
    PalmPayTheme {
        GetPaymentDialog(onDismiss = {}, onPaymentAmountEntered = {})
    }
}