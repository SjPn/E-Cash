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
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: BudgetViewModel,
    transactionType: String?
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val typeState = if (transactionType == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }

    val baseContext = LocalContext.current
    val currentLang = com.example.budget.ui.screens.LocaleHelper.getLanguage(baseContext)
    val localizedContext = remember(currentLang) { com.example.budget.ui.screens.LocaleHelper.setLocale(baseContext, currentLang) }
    val categories by viewModel.categories.collectAsState()
    
    // Initialize category with first available category from database
    var category by remember { mutableStateOf("") }
    
    // Initialize default categories and set first category when categories are loaded
    
    LaunchedEffect(categories) {
        categories.forEach { cat ->
        }
        if (categories.isNotEmpty() && category.isEmpty()) {
            category = categories.first().name
        }
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        val title = if (typeState == TransactionType.INCOME) stringResource(R.string.add_income_title) else stringResource(R.string.add_expense_title)
    Scaffold(
        topBar = {
                TopAppBar(title = { Text(title) })
        }
    ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
        Column(
            modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            if (typeState == TransactionType.EXPENSE) {
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
                                    .fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(color = categories.find { it.name == category }?.getColor() ?: LocalContentColor.current)
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
                    val categoryToSave = if (typeState == TransactionType.EXPENSE) category else ""
                    if (name.isNotBlank() && amountDouble != null && (typeState == TransactionType.INCOME || categoryToSave.isNotBlank())) {
                        viewModel.insert(
                            Transaction(
                                name = name,
                                amount = amountDouble,
                                category = categoryToSave,
                                date = Date(),
                                type = typeState
                            )
                        )
                        navController.popBackStack()
                    }
                },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
            ) {
                Text(stringResource(R.string.save_transaction))
                    }
                }
            }
        }
    }
} 