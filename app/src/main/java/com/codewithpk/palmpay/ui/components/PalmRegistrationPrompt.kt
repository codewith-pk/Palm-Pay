package com.codewithpk.palmpay.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.codewithpk.palmpay.ui.theme.PalmPayTheme

@Composable
fun PalmRegistrationPrompt(
    modifier: Modifier = Modifier,
    onRegisterNow: () -> Unit,
    onLearnMore: () -> Unit,
    onDismiss: () -> Unit // To dismiss the dialog if user chooses not to register now
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Your Palm") },
        text = { Text("To enable palm-based payments, please register your palm scan now. This is a one-time setup.") },
        confirmButton = {
            TextButton(onClick = onRegisterNow) {
                Text("Register Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onLearnMore) {
                Text("Learn More")
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewPalmRegistrationPrompt() {
    PalmPayTheme {
        PalmRegistrationPrompt(onRegisterNow = {}, onLearnMore = {}, onDismiss = {})
    }
}