package com.codewithpk.palmpay.ui.login

import android.content.Intent
import android.graphics.drawable.Icon
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import com.codewithpk.palmpay.ui.theme.PalmPayTheme

@Composable
fun LoginScreen(
    onBiometricSuccess: () -> Unit,
    onBiometricUnavailable: () -> Unit // For when biometrics are not set up or hardware issue
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    val enrollLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (BiometricManager.from(activity)
                .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            Toast.makeText(activity, "Biometric enrolled! Try logging in.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(activity, "Biometric enrollment not confirmed.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    val biometricPrompt = remember {
        BiometricPrompt(
            activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(activity, "Auth error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(activity, "Authentication Succeeded!", Toast.LENGTH_SHORT).show()
                    onBiometricSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        activity,
                        "Authentication Failed. Try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Welcome to PalmPay Lite!")
            .setSubtitle("Secure palm-based payment login.")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }

    val gradientBrush =
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                MaterialTheme.colorScheme.background
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = "Welcome to PalmPay Lite",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(48.dp))

        // Palm/Fingerprint/Face Scan Icon
        Icon(
            imageVector = Icons.Rounded.Fingerprint, // Using Fingerprint icon as a generic biometric
            contentDescription = "Biometric Icon",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Secure palm-based payment login.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val biometricManager = BiometricManager.from(activity)
                when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        biometricPrompt.authenticate(promptInfo)
                    }

                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        Toast.makeText(
                            activity,
                            "No biometric hardware available.",
                            Toast.LENGTH_LONG
                        ).show()
                        onBiometricUnavailable()
                    }

                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                        Toast.makeText(
                            activity,
                            "Biometric hardware is currently unavailable.",
                            Toast.LENGTH_LONG
                        ).show()
                        onBiometricUnavailable()
                    }

                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        Toast.makeText(
                            activity,
                            "No biometrics enrolled. Please enroll a fingerprint or face.",
                            Toast.LENGTH_LONG
                        ).show()
                        val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                            )
                        }
                        enrollLauncher.launch(enrollIntent)
                    }

                    else -> {
                        Toast.makeText(
                            activity,
                            "Biometric authentication not supported or failed setup.",
                            Toast.LENGTH_LONG
                        ).show()
                        onBiometricUnavailable()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Login with Biometrics")
        }

        Spacer(Modifier.weight(1f))

        // Footer links
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.6f
                            )
                        )
                    ) {
                        append("Privacy")
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.outline)) {
                        append("  |  ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.6f
                            )
                        )
                    ) {
                        append("Terms")
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.outline)) {
                        append("  |  ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.6f
                            )
                        )
                    ) {
                        append("Help")
                    }
                },
                style = MaterialTheme.typography.labelMedium.copy(textDecoration = TextDecoration.Underline)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    PalmPayTheme {
        LoginScreen(onBiometricSuccess = {}, onBiometricUnavailable = {})
    }
}