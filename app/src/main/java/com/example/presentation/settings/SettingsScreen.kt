package com.example.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.MainViewModel
import com.example.presentation.components.AppTopBar
import com.example.presentation.components.ConfirmDeleteDialog
import com.example.presentation.components.PrimaryButton

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userCurrency by viewModel.userCurrency.collectAsState()
    val userBudget by viewModel.monthlyBudget.collectAsState()
    val userTheme by viewModel.userTheme.collectAsState()
    val notificationConsentState by viewModel.notificationsEnabled.collectAsState()

    var nameInput by remember { mutableStateOf("") }
    var budgetInput by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("$") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var confirmationStatusText by remember { mutableStateOf("") }

    LaunchedEffect(userName, userCurrency, userBudget) {
        nameInput = userName
        selectedCurrency = userCurrency
        budgetInput = userBudget.toString()
    }

    if (showResetConfirmDialog) {
        ConfirmDeleteDialog(
            title = "Warning: Wipe Database",
            text = "Are you absolutely sure you want to permanently delete all tasks, notes, habits, expenses, study records, and conversational history files off this hardware device? This operation cannot be undone.",
            onConfirm = {
                viewModel.resetAllData()
                confirmationStatusText = "Device cleared! Please restart application."
            },
            onDismiss = { showResetConfirmDialog = false }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "DailyOS Configuration",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            
            // Section 1: User Profile Settings
            Text("User Profile Customization", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Display Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_name_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Section 2: Budget Preference Settings
            Text("Finance & Ledger Setup", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Currency selection
                listOf("$", "€", "£", "¥", "₨").forEach { sym ->
                    val isSelected = selectedCurrency == sym
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val txtColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { selectedCurrency = sym }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sym, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = txtColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = budgetInput,
                onValueChange = { budgetInput = it },
                label = { Text("Monthly Budget Limit ($selectedCurrency)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_budget_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Visual Identity Theme Selector
            Text("Dynamic Styling Themes", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("System", "Light", "Dark").forEach { mode ->
                    val isSelected = userTheme == mode
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val txtColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .clickable {
                                viewModel.updateUserTheme(mode)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(mode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = txtColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 4: Local Notifications Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Reminders Signal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Receive prompt alerts for habits and deep work timers.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Switch(
                    checked = notificationConsentState,
                    onCheckedChange = { 
                        viewModel.updateNotificationsEnabled(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // General CTA Update Profiler button
            PrimaryButton(
                text = "Apply Profiles Update",
                onClick = {
                    viewModel.updateUserName(nameInput.trim().ifEmpty { "Innovator" })
                    viewModel.updateUserCurrency(selectedCurrency)
                    viewModel.updateMonthlyBudget(budgetInput.toDoubleOrNull() ?: 1000.0)
                    confirmationStatusText = "Preferences synchronized successfully!"
                }
            )

            if (confirmationStatusText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(confirmationStatusText, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Critical Reset Area
            Text("Critical System Area", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.forcePrepopulateMockSampleDataNow()
                        confirmationStatusText = "Seeded complete mockup! Return to dashboard."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Seed Sample Mock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showResetConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Format All Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
