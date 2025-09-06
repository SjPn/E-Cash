package com.example.budget.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budget.R
import com.example.budget.data.CategorySum
import com.example.budget.data.Transaction
import com.example.budget.data.TransactionType
import com.example.budget.data.Categories
import com.example.budget.data.UserCategory
import com.example.budget.ui.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun setLocale(context: Context, language: String): ContextWrapper {
        persist(context, language)
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        return ContextWrapper(context.createConfigurationContext(config))
    }

    fun getLanguage(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(SELECTED_LANGUAGE, Locale.getDefault().language) ?: "en"
    }

    private fun persist(context: Context, language: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(SELECTED_LANGUAGE, language).apply()
    }
}

// Function to get category color from user categories or fallback to default
fun getCategoryColor(categoryName: String, categories: List<UserCategory>, isDark: Boolean = false): Color {
    val userCategory = categories.find { it.name == categoryName }
    return if (userCategory != null) {
        userCategory.getColor()
    } else {
        if (isDark) {
            Categories.darkCategoryColors[categoryName] ?: Color.Gray
        } else {
            Categories.colors[categoryName] ?: Color.Gray
        }
    }
}

// Вспомогательная функция для корректного сравнения месяцев
fun isSameMonth(date: Date, targetMonth: Int, targetYear: Int): Boolean {
    return try {
        val cal = Calendar.getInstance().apply { 
            time = date
            // Сбрасываем время до начала дня для избежания проблем с часовыми поясами
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
        
        
        result
    } catch (e: Exception) {
        println("ERROR: Failed to compare months for date $date: ${e.message}")
        false
    }
}

// Вспомогательная функция для проверки года
fun isSameYear(date: Date, targetYear: Int): Boolean {
    return try {
        val cal = Calendar.getInstance().apply { 
            time = date
            // Сбрасываем время до начала дня для избежания проблем с часовыми поясами
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.get(Calendar.YEAR) == targetYear
    } catch (e: Exception) {
        println("ERROR: Failed to compare years for date $date: ${e.message}")
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: BudgetViewModel
) {
    val allTransactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var langMenuExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val baseContext = LocalContext.current
    val currentLang = LocaleHelper.getLanguage(baseContext)
    val localizedContext = remember(currentLang) { LocaleHelper.setLocale(baseContext, currentLang) }

    val today = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableStateOf(today.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(today.get(Calendar.YEAR)) }
    
    // Функция для автоматического переключения месяца при изменении транзакций
    LaunchedEffect(allTransactions) {
        // Проверяем, есть ли транзакции в текущем месяце
        val hasTransactionsInCurrentMonth = allTransactions.any { transaction ->
            isSameMonth(transaction.date, currentMonth, currentYear)
        }
        
        // Если нет транзакций в текущем месяце, переключаемся на месяц с последней транзакцией
        if (!hasTransactionsInCurrentMonth && allTransactions.isNotEmpty()) {
            val lastTransaction = allTransactions.maxByOrNull { it.date }
            lastTransaction?.let { transaction ->
                val cal = Calendar.getInstance().apply { time = transaction.date }
                currentMonth = cal.get(Calendar.MONTH)
                currentYear = cal.get(Calendar.YEAR)
            }
        }
    }

    // Фильтрация транзакций по текущему месяцу и году
    val filteredTransactions = remember(allTransactions, currentMonth, currentYear, selectedTab) {
        try {
            
            allTransactions.filter { transaction ->
                try {
                    val matchesMonth = isSameMonth(transaction.date, currentMonth, currentYear)
                    val matchesType = if (selectedTab == 0) transaction.type == TransactionType.EXPENSE else transaction.type == TransactionType.INCOME
                    
                    matchesMonth && matchesType
                } catch (e: Exception) {
                    println("ERROR: Failed to filter transaction ${transaction.name}: ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            println("ERROR: Failed to filter transactions: ${e.message}")
            emptyList()
        }
    }

    // Группировка по категориям для текущего месяца
    val filteredCategorySums = remember(filteredTransactions, categories) {
        if (selectedTab == 0) {
            // Используем все категории из базы данных (предопределенные + пользовательские)
            categories.map { userCategory ->
                val sum = filteredTransactions.filter { it.category == userCategory.name }.sumOf { it.amount }
                CategorySum(userCategory.name, sum)
            }.filter { it.total > 0 }
            .sortedByDescending { it.total } // Сортировка по убыванию суммы
        } else {
            listOf(CategorySum("INCOME", filteredTransactions.sumOf { it.amount }))
        }
    }
    val sum = filteredCategorySums.sumOf { it.total }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    title = { 
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.shapes.medium
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("categories") }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Manage Categories",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { langMenuExpanded = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Change language")
                        }
                        DropdownMenu(
                            expanded = langMenuExpanded,
                            onDismissRequest = { langMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Українська") },
                                onClick = {
                                    LocaleHelper.setLocale(baseContext, "uk")
                                    langMenuExpanded = false
                                    (baseContext as? Activity)?.recreate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    LocaleHelper.setLocale(baseContext, "en")
                                    langMenuExpanded = false
                                    (baseContext as? Activity)?.recreate()
                                }
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val isDark = isSystemInDarkTheme()
                    
                    // Расчет баланса для текущего месяца
                    val totalIncome = try {
                        allTransactions.filter { 
                            it.type == TransactionType.INCOME && isSameMonth(it.date, currentMonth, currentYear)
                        }.sumOf { it.amount }
                    } catch (e: Exception) {
                        println("ERROR: Failed to calculate total income: ${e.message}")
                        0.0
                    }
                    
                    val totalExpense = try {
                        allTransactions.filter { 
                            it.type == TransactionType.EXPENSE && isSameMonth(it.date, currentMonth, currentYear)
                        }.sumOf { it.amount }
                    } catch (e: Exception) {
                        println("ERROR: Failed to calculate total expense: ${e.message}")
                        0.0
                    }
                    
                    val diff = totalIncome - totalExpense
                    val diffColor = if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    
                    // Расчет годового баланса
                    val yearIncome = try {
                        allTransactions.filter { 
                            it.type == TransactionType.INCOME && isSameYear(it.date, currentYear)
                        }.sumOf { it.amount }
                    } catch (e: Exception) {
                        println("ERROR: Failed to calculate year income: ${e.message}")
                        0.0
                    }
                    
                    val yearExpense = try {
                        allTransactions.filter { 
                            it.type == TransactionType.EXPENSE && isSameYear(it.date, currentYear)
                        }.sumOf { it.amount }
                    } catch (e: Exception) {
                        println("ERROR: Failed to calculate year expense: ${e.message}")
                        0.0
                    }
                    
                    val yearBalance = yearIncome - yearExpense
                    val yearBalanceColor = if (yearBalance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    
                    // Отображение балансов в ряд
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                MaterialTheme.shapes.medium
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Месячный баланс
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.balance),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.0f ₴", diff),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = diffColor
                            )
                        }
                        
                        // Разделитель
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                        )
                        
                        // Годовой баланс
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.year_balance),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.0f ₴", yearBalance),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = yearBalanceColor
                            )
                        }
                    }
                    
                    TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResource(R.string.expenses_tab)) })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResource(R.string.income_tab)) })
                    }
                    

                    
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .pointerInput(currentMonth, currentYear) {
                                detectHorizontalDragGestures { change, dragAmount ->
                                    if (dragAmount > 40) {
                                        // Свайп влево - предыдущий месяц
                                        if (currentMonth == 0) {
                                            currentMonth = 11
                                            currentYear -= 1
                                        } else {
                                            currentMonth -= 1
                                        }
                                    } else if (dragAmount < -40) {
                                        // Свайп вправо - следующий месяц
                                        if (currentMonth == 11) {
                                            currentMonth = 0
                                            currentYear += 1
                                        } else {
                                            currentMonth += 1
                                        }
                                    }
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                        ) {
                            val isExpenseTab = selectedTab == 0
                            Box(modifier = Modifier.fillMaxWidth()) {
                                val incomeColors = listOf(
                                    Color(0xFF43A047), // насыщенный зелёный
                                    Color(0xFF66BB6A), // светлее
                                    Color(0xFF81C784), // ещё светлее
                                    Color(0xFFA5D6A7), // пастель
                                    Color(0xFFC8E6C9)  // очень светлый
                                )
                                ExpensePieChart(
                                    categorySums = filteredCategorySums,
                                    sum = sum,
                                    isExpenseTab = isExpenseTab,
                                    categories = categories,
                                    showGray = filteredCategorySums.isEmpty(),
                                    incomeColors = if (!isExpenseTab) incomeColors else null,
                                    month = currentMonth,
                                    year = currentYear
                                )
                                val fabColor = if (isExpenseTab) Color(0xFFF44336) else Color(0xFF4CAF50)
                                val fabIconColor = Color.White
                                FloatingActionButton(
                                    onClick = {
                                        if (isExpenseTab) {
                                            navController.navigate("add?type=${TransactionType.EXPENSE.name}")
                                        } else {
                                            navController.navigate("add?type=${TransactionType.INCOME.name}")
                                        }
                                    },
                                    containerColor = fabColor,
                                    contentColor = fabIconColor,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(start = 16.dp, bottom = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Transaction", tint = fabIconColor)
                                }
                            }
                            CategorySummary(
                                categorySums = filteredCategorySums,
                                transactions = filteredTransactions,
                                categories = categories,
                                onEditTransaction = { id -> navController.navigate("edit/$id") },
                                isIncomeTab = selectedTab == 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySummary(categorySums: List<CategorySum>, transactions: List<Transaction>, onEditTransaction: (Int) -> Unit, categories: List<UserCategory>, isIncomeTab: Boolean = false) {
    val expandedCategories = remember { mutableStateListOf<String>() }
    val isDark = isSystemInDarkTheme()
    val sum = categorySums.sumOf { it.total }
    
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
        if (isIncomeTab) {
            val color = if (isDark) Color(0xFF43A047) else Color(0xFF43A047)
    Card(
        modifier = Modifier
            .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(2.dp, color, MaterialTheme.shapes.medium),
                colors = if (isDark)
                    CardDefaults.cardColors(containerColor = Color.Black)
                else
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
                val textColor = if (isDark) Color.White else Color.Black
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
        ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Income",
                            tint = textColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = stringResource(R.string.income),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = textColor,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
            Text(
                            text = String.format(Locale.getDefault(), "%.0f ₴", categorySums.firstOrNull()?.total ?: 0.0),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                    }
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                        val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                        transactions.sortedByDescending { it.date }.forEach { transaction ->
    Row(
        modifier = Modifier
            .fillMaxWidth()
                                    .clickable { onEditTransaction(transaction.id) },
                                horizontalArrangement = Arrangement.SpaceBetween
    ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = transaction.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = textColor
                                    )
                                    Text(
                                        text = dateFormat.format(transaction.date) + " " + timeFormat.format(transaction.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFFBDBDBD) else Color(0xFF888888)
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%.0f ₴", transaction.amount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        } else {
            categorySums.forEach { summary ->
                val color = getCategoryColor(summary.category, categories, isDark)
    Card(
        modifier = Modifier
            .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(2.dp, color, MaterialTheme.shapes.medium),
                    colors = if (isDark)
                        CardDefaults.cardColors(containerColor = Color.Black)
                    else
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (expandedCategories.contains(summary.category)) {
                                    expandedCategories.remove(summary.category)
                                } else {
                                    expandedCategories.add(summary.category)
                                }
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = com.example.budget.data.Categories.icons[summary.category] ?: com.example.budget.data.Categories.icons["OTHER"]!!
                            val textColor = if (isDark) Color.White else Color.Black
                            Icon(
                                imageVector = icon,
                                contentDescription = summary.category,
                                tint = textColor,
                                modifier = Modifier.size(28.dp)
                            )
                            val percent = if (sum > 0) (summary.total / sum * 100).toInt() else 0
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor,
                                modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (summary.category) {
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
                                        else -> summary.category // Показываем имя пользовательской категории как есть
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = textColor
                                )
                            }
                        Text(
                                text = String.format(Locale.getDefault(), "%.0f ₴", summary.total),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                        )
                    }
                    if (expandedCategories.contains(summary.category)) {
                            val isExpense = transactions.firstOrNull()?.type == TransactionType.EXPENSE || transactions.isEmpty()
                            val filtered = transactions.filter { it.category == summary.category && (if (isExpense) it.type == TransactionType.EXPENSE else it.type == TransactionType.INCOME) }
                            val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                            val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                            val grouped = filtered.groupBy { transaction ->
                                // Используем нашу вспомогательную функцию для корректного форматирования даты
                                val cal = Calendar.getInstance().apply { time = transaction.date }
                                dateFormat.format(cal.time)
                            }
                            Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)) {
                                var isFirst = true
                                grouped.forEach { (date, txns) ->
                                    if (!isFirst) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    isFirst = false
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp)
                                            .border(2.dp, color, MaterialTheme.shapes.medium),
                                        colors = if (isDark)
                                            CardDefaults.cardColors(containerColor = Color.Black)
                                        else
                                            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        val textColor = if (isDark) Color.White else Color.Black
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = date,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isDark) Color(0xFFBDBDBD) else Color(0xFF888888),
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            txns.forEach { transaction ->
                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { onEditTransaction(transaction.id) },
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = transaction.name,
                                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = textColor
                                                        )
                                                        Text(
                                                            text = timeFormat.format(transaction.date),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (isDark) Color(0xFFBDBDBD) else Color(0xFF888888)
                                                        )
                                                    }
                                    Text(
                                                        text = String.format(Locale.getDefault(), "%.0f ₴", transaction.amount),
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = textColor
                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (filtered.isEmpty()) {
                                Text(
                                    text = if (isExpense) stringResource(R.string.no_expenses) else stringResource(R.string.no_incomes),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensePieChart(
    categorySums: List<CategorySum>,
    sum: Double,
    isExpenseTab: Boolean,
    categories: List<UserCategory>,
    showGray: Boolean = false,
    incomeColors: List<Color>? = null,
    month: Int? = null,
    year: Int? = null
) {
    val total = remember(categorySums) { categorySums.sumOf { it.total } }
    val currentMonthYear = remember(month to year) {
        val locale = Locale.getDefault()
        val cal = Calendar.getInstance()
        if (month != null && year != null) {
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.DAY_OF_MONTH, 1) // Устанавливаем первый день месяца для корректного отображения
        }
        java.text.SimpleDateFormat("LLLL yyyy", locale).format(cal.time).replaceFirstChar { it.uppercase(locale) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
            ) {
                Canvas(
                modifier = Modifier.size(220.dp)
                ) {
                    var startAngle = -90f
                if (showGray) {
                    drawArc(
                        color = Color(0xFFBDBDBD), // светло-серый
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 40f, cap = StrokeCap.Butt)
                    )
                } else {
                    categorySums.forEachIndexed { idx, summary ->
                        val sweepAngle = if (total > 0) (summary.total / total * 360).toFloat() else 0f
                        val color = incomeColors?.getOrNull(idx % incomeColors.size)
                            ?: getCategoryColor(summary.category, categories)
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 40f, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }
            }
            val absSum = kotlin.math.abs(sum)
            val formatted = absSum.toLong().toString().reversed().chunked(3).joinToString(" ").reversed()
            val displaySum = if (isExpenseTab) "-$formatted ₴" else "$formatted ₴"
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displaySum,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Text(
                    text = currentMonthYear,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
} 