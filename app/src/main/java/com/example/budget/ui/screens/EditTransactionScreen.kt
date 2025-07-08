package com.example.budget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.budget.R
import com.example.budget.data.Categories
import com.example.budget.data.Transaction
import com.example.budget.data.TransactionType
import com.example.budget.ui.viewmodel.BudgetViewModel
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    navController: NavController,
    viewModel: BudgetViewModel,
    transactionId: Int?
) {
    val baseContext = LocalContext.current
    val currentLang = com.example.budget.ui.screens.LocaleHelper.getLanguage(baseContext)
    val localizedContext = remember(currentLang) { com.example.budget.ui.screens.LocaleHelper.setLocale(baseContext, currentLang) }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        LaunchedEffect(transactionId) {
            viewModel.setEditTransactionId(transactionId)
        }
        val transactionState by viewModel.editTransaction.collectAsState()
        val transaction = transactionState
        if (transaction == null) {
            Box {}
            return@CompositionLocalProvider
        }

        var isInitialized by remember { mutableStateOf(false) }
        var name by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
        var date by remember { mutableStateOf(Date()) }
        var showDatePicker by remember { mutableStateOf(false) }

        if (!isInitialized) {
            name = transaction.name
            amount = transaction.amount.toString()
            category = transaction.category
            transactionType = transaction.type
            date = transaction.date
            isInitialized = true
        }

        var isCategoryMenuExpanded by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.edit_transaction)) })
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val dateFormat = remember { java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dateFormat.format(date))
                }
                if (showDatePicker) {
                    val calendar = Calendar.getInstance().apply { time = date }
                    DatePickerDialog(
                        initialYear = calendar.get(Calendar.YEAR),
                        initialMonth = calendar.get(Calendar.MONTH),
                        initialDay = calendar.get(Calendar.DAY_OF_MONTH),
                        onDateSelected = { year, month, day ->
                            val cal = Calendar.getInstance()
                            cal.set(year, month, day)
                            date = cal.time
                            showDatePicker = false
                        },
                        onDismissRequest = { showDatePicker = false }
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = isCategoryMenuExpanded,
                    onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = when (category) {
                            "HOME" -> stringResource(R.string.category_home)
                            "GIFTS" -> stringResource(R.string.category_gifts)
                            "FOOD" -> stringResource(R.string.category_food)
                            "CAR" -> stringResource(R.string.category_car)
                            "IT" -> stringResource(R.string.category_it)
                            "SPORT" -> stringResource(R.string.category_sport)
                            "OTHER" -> stringResource(R.string.category_other)
                            "EDUCATION" -> stringResource(R.string.category_education)
                            "TAX" -> stringResource(R.string.category_tax)
                            "HEALTH" -> stringResource(R.string.category_health)
                            "LEISURE" -> stringResource(R.string.category_leisure)
                            else -> stringResource(R.string.category_other)
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryMenuExpanded,
                        onDismissRequest = { isCategoryMenuExpanded = false }
                    ) {
                        Categories.list.forEach { categoryName ->
                            val categoryResId = when (categoryName) {
                                "HOME" -> R.string.category_home
                                "GIFTS" -> R.string.category_gifts
                                "FOOD" -> R.string.category_food
                                "CAR" -> R.string.category_car
                                "IT" -> R.string.category_it
                                "SPORT" -> R.string.category_sport
                                "OTHER" -> R.string.category_other
                                "EDUCATION" -> R.string.category_education
                                "TAX" -> R.string.category_tax
                                "HEALTH" -> R.string.category_health
                                "LEISURE" -> R.string.category_leisure
                                else -> R.string.category_other
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(categoryResId)) },
                                onClick = {
                                    category = categoryName
                                    isCategoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val amountDouble = amount.toDoubleOrNull()
                        if (name.isNotBlank() && amountDouble != null && category.isNotBlank()) {
                            viewModel.update(
                                transaction.copy(
                                    name = name,
                                    amount = amountDouble,
                                    category = category,
                                    type = transactionType,
                                    date = date
                                )
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_changes), color = MaterialTheme.colorScheme.onSurface)
                }
                Button(
                    onClick = {
                        viewModel.delete(transaction)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val picker = android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(year, month, dayOfMonth)
            },
            initialYear, initialMonth, initialDay
        )
        picker.setOnCancelListener { onDismissRequest() }
        picker.show()
        onDispose { picker.dismiss() }
    }
} 