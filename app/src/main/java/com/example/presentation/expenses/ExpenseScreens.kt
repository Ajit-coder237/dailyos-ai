package com.example.presentation.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.core.database.ExpenseEntity
import com.example.presentation.MainViewModel
import com.example.presentation.components.*
import com.example.ui.theme.ColorCritical
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseScreen(
    viewModel: MainViewModel,
    onNavigateToAddExpense: () -> Unit,
    onBack: () -> Unit
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val todayExpenses by viewModel.todayExpenses.collectAsState()
    val userCurrency by viewModel.userCurrency.collectAsState()
    val userBudget by viewModel.monthlyBudget.collectAsState()

    val currencySymbol = remember(userCurrency) { userCurrency }

    val todaySpendTotal = todayExpenses.sumOf { it.amount }
    val monthlySpendTotal = expenses.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        val curCal = Calendar.getInstance()
        cal.get(Calendar.YEAR) == curCal.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == curCal.get(Calendar.MONTH)
    }.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppTopBar(
                title = "Expense Command Ledger",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )

            // Dynamic spent banners
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Spent Today",
                    value = "$currencySymbol ${String.format("%.1f", todaySpendTotal)}",
                    icon = Icons.Default.Receipt,
                    accentColor = Color(0xFF0D9488)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Spent This Month",
                    value = "$currencySymbol ${String.format("%.1f", monthlySpendTotal)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = Color(0xFF3B82F6)
                )
            }

            // Budget warning progress indicator
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val budgetPercent = if (userBudget > 0) (monthlySpendTotal / userBudget).toFloat() else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Monthly Limit Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = "${(budgetPercent * 100).toInt()}% • Limit $currencySymbol ${String.format("%.1f", userBudget)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (budgetPercent > 1f) ColorCritical else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { minOf(1f, budgetPercent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = if (budgetPercent > 1f) ColorCritical else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    if (budgetPercent > 1f) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Warning! Spending has exceeded safety thresholds.",
                            color = ColorCritical,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            SectionHeader(title = "Transaction Cash Ledger (${expenses.size})")

            if (expenses.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyState(
                        title = "No logged expenses",
                        subtitle = "Ready to audit transactions! Use the '+' button to record expenses.",
                        icon = Icons.Default.AccountBalance
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(expenses) { expense ->
                        var showConfirmDelete by remember { mutableStateOf(false) }

                        if (showConfirmDelete) {
                            ConfirmDeleteDialog(
                                title = "Remove Ledger Record",
                                text = "Delete transaction \"${expense.note}\"?",
                                onConfirm = { viewModel.deleteExpense(expense) },
                                onDismiss = { showConfirmDelete = false }
                            )
                        }

                        ExpenseRowItem(
                            expense = expense,
                            currencySymbol = currencySymbol,
                            onDeleteClick = { showConfirmDelete = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddExpenseScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var errorText by remember { mutableStateOf("") }

    val categories = listOf("Food", "Transport", "Rent", "Bills", "Fun", "Health", "Other")

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            AppTopBar(
                title = "Log Spend",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Transaction Label (e.g. Starbucks) *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_title_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_amount_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = category == cat
                    CategoryChip(
                        category = cat,
                        isSelected = isSelected,
                        onClick = { category = cat }
                    )
                }
            }

            if (errorText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Add Transaction to Ledger",
                onClick = {
                    val amountVal = amountText.toDoubleOrNull()
                    if (title.trim().isEmpty() || amountVal == null || amountVal <= 0.0) {
                        errorText = "Please enter valid label and positive amount description."
                    } else {
                        viewModel.addExpense(
                            amount = amountVal,
                            category = category,
                            note = title.trim(),
                            date = System.currentTimeMillis()
                        )
                        onBack()
                    }
                }
            )
        }
    }
}

@Composable
fun ExpenseRowItem(
    expense: ExpenseEntity,
    currencySymbol: String,
    onDeleteClick: () -> Unit
) {
    val dateText = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.date))

    val iconPair = when (expense.category) {
        "Food" -> Icons.Default.Fastfood to Color(0xFFF57C00)
        "Transport" -> Icons.Default.DirectionsCar to Color(0xFF1E88E5)
        "Rent" -> Icons.Default.Home to Color(0xFF3F51B5)
        "Bills" -> Icons.Default.Receipt to Color(0xFFEF5350)
        "Fun" -> Icons.Default.SportsEsports to Color(0xFFEC407A)
        "Health" -> Icons.Default.Favorite to Color(0xFF26A69A)
        else -> Icons.Default.ShoppingBag to Color(0xFF78909C)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconPair.second.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconPair.first, contentDescription = null, tint = iconPair.second, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(expense.note, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Category: ${expense.category} • $dateText", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "- $currencySymbol ${expense.amount}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = ColorCritical
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete transaction", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
