package com.codewithpk.palmpay.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var alpha by remember { mutableStateOf(0f) }

    // Animate alpha for fade-in effect
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(
            durationMillis = 1500, // Fade in over 1.5 seconds
            easing = FastOutSlowInEasing
        ), label = "Splash Fade In"
    )

    LaunchedEffect(Unit) {
        alpha = 1f // Start fade in
        delay(2000) // Stay visible for 2 seconds after fade in
        onTimeout()
    }

    val gradientBrush =
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.primaryContainer
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
            .alpha(animatedAlpha),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Face,
            contentDescription = "App Logo",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "PalmPay Lite",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "The Future of Payment is in Your Palm.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        )
    }
}