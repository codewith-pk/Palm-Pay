package com.codewithpk.palmpay.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codewithpk.palmpay.ui.theme.PalmPayTheme
import com.codewithpk.palmpay.ui.scan.ScanViewModel
import com.codewithpk.palmpay.ui.theme.GreenSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onReRegisterPalm: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    scanViewModel: ScanViewModel = hiltViewModel()
) {
    val enrolledPalm by scanViewModel.enrolledPalm.collectAsState(initial = null)
    val isPalmRegistered = enrolledPalm != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Profile Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Photo",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "John Doe", // Mock Name
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ID: user_001", // Mock User ID
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isPalmRegistered) "Palm Registered" else "Palm Not Registered",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isPalmRegistered) GreenSuccess else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.size(4.dp))
                        Icon(
                            imageVector = if (isPalmRegistered) Icons.Default.Verified else Icons.Default.Warning,
                            contentDescription = "Palm Status",
                            tint = if (isPalmRegistered) GreenSuccess else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Settings List
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile",
                    onClick = { /* TODO: Navigate to Edit Profile */ }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingItem(
                    icon = Icons.Default.Payment,
                    title = "Payment Settings",
                    onClick = { /* TODO: Navigate to Payment Settings */ }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingItem(
                    icon = Icons.Default.AddReaction, // Using a generic palm icon
                    title = "Re-register Palm Scan",
                    onClick = onReRegisterPalm
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingItem(
                    icon = Icons.Default.Key,
                    title = "Change Biometric Login",
                    onClick = { /* TODO: Link to system biometric settings or re-prompt */ }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    onClick = onLogout,
                    textColor = MaterialTheme.colorScheme.error // Highlight logout
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        headlineContent = { Text(title, color = textColor) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        supportingContent = { /* Optional: Add secondary text here */ },

        )
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    PalmPayTheme {
        ProfileScreen(onReRegisterPalm = {}, onLogout = {}, onBack = {})
    }
}