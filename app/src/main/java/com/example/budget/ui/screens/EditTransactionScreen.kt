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
import com.example.budget.data.UserCategory
import com.example.budget.ui.viewmodel.BudgetViewModel
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
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
    val categories by viewModel.categories.collectAsState()

    CompositionLocalProvider(LocalContext provides localizedContext) {
        // Initialize default categories
        
        LaunchedEffect(transactionId) {
            viewModel.setEditTransactionId(transactionId)
        }
        val transactionState by viewModel.editTransaction.collectAsState()
        val transaction = transactionState
        if (transaction == null) {
            Box {}
            return@CompositionLocalProvider
        }

        var name by remember(transaction.id) { mutableStateOf(transaction.name) }
        var amount by remember(transaction.id) { mutableStateOf(transaction.amount.toString()) }
        var category by remember(transaction.id) { mutableStateOf(transaction.category) }
        var transactionType by remember(transaction.id) { mutableStateOf(transaction.type) }
        var date by remember(transaction.id) { mutableStateOf(transaction.date) }

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
                    onClick = { 
                        val calendar = Calendar.getInstance().apply { time = date }
                        val picker = android.app.DatePickerDialog(
                            baseContext,
                            { _, year, month, dayOfMonth ->
                                val cal = Calendar.getInstance()
                                cal.set(year, month, dayOfMonth)
                                date = cal.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        try {
                            picker.show()
                        } catch (e: Exception) {
                            // Игнорируем ошибки
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dateFormat.format(date))
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

            if (transactionType == TransactionType.EXPENSE) {
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
    
                            "OTHER" -> stringResource(R.string.category_other)
                            "EDUCATION" -> stringResource(R.string.category_education)
                            "TAX" -> stringResource(R.string.category_tax)
                            "HEALTH" -> stringResource(R.string.category_health)
                            "LEISURE" -> stringResource(R.string.category_leisure)
                            else -> category // Show user-created category name as is
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
                    onDismissRequest = { isCategoryMenuExpanded = false },
                    modifier = Modifier.heightIn(max = 300.dp) // Увеличиваем максимальную высоту
                ) {
                        categories.forEach { userCategory ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = when (userCategory.name) {
                                            "HOME" -> stringResource(R.string.category_home)
                                            "GIFTS" -> stringResource(R.string.category_gifts)
                                            "FOOD" -> stringResource(R.string.category_food)
                                            "CAR" -> stringResource(R.string.category_car)
                                            "IT" -> stringResource(R.string.category_it)
                                            "OTHER" -> stringResource(R.string.category_other)
                                            "EDUCATION" -> stringResource(R.string.category_education)
                                            "TAX" -> stringResource(R.string.category_tax)
                                            "HEALTH" -> stringResource(R.string.category_health)
                                            "LEISURE" -> stringResource(R.string.category_leisure)
                                            else -> userCategory.name // Показываем имя пользовательской категории как есть
                                        }, 
                                        color = userCategory.getColor()
                                    ) 
                                },
                                onClick = {
                                    category = userCategory.name
                                    isCategoryMenuExpanded = false
                                    }
                                )
                    }
                }
                }
            }

            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    val categoryToSave = if (transactionType == TransactionType.EXPENSE) category else ""
                    if (name.isNotBlank() && amountDouble != null && (transactionType == TransactionType.INCOME || categoryToSave.isNotBlank())) {
                        viewModel.update(
                            transaction.copy(
                                name = name,
                                amount = amountDouble,
                                category = categoryToSave,
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