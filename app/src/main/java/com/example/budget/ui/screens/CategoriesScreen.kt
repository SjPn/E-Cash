package com.example.budget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.budget.R
import com.example.budget.data.Categories
import com.example.budget.data.UserCategory
import com.example.budget.ui.viewmodel.BudgetViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(navController: NavController, viewModel: BudgetViewModel) {
    val baseContext = LocalContext.current
    val currentLang = LocaleHelper.getLanguage(baseContext)
    val localizedContext = remember(currentLang) { LocaleHelper.setLocale(baseContext, currentLang) }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        val categories by viewModel.categories.collectAsState()
        
        // Initialize default categories on first launch
        
        var showAddDialog by remember { mutableStateOf(false) }
        var newCategoryName by remember { mutableStateOf("") }
        var newCategoryColor by remember { mutableStateOf(Color(0xFF4CAF50)) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.manage_categories)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showAddDialog = true }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Category")
                        }
                    }
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onDelete = {
                            viewModel.deleteCategory(category)
                        }
                    )
                }
            }
        }

        // Add Category Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(stringResource(R.string.add_category)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text(stringResource(R.string.category_name)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.select_color))
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(8),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(120.dp)
                        ) {
                            val colors = listOf(
                                // Основные цвета
                                Color(0xFF4CAF50), // Green
                                Color(0xFF2196F3), // Blue
                                Color(0xFFFF9800), // Orange
                                Color(0xFF9C27B0), // Purple
                                Color(0xFFF44336), // Red
                                Color(0xFF00BCD4), // Cyan
                                Color(0xFFFFC107), // Yellow
                                Color(0xFF795548), // Brown
                                
                                // Дополнительные цвета
                                Color(0xFFE91E63), // Pink
                                Color(0xFF607D8B), // Blue Grey
                                Color(0xFF3F51B5), // Indigo
                                Color(0xFF009688), // Teal
                                Color(0xFF8BC34A), // Light Green
                                Color(0xFFCDDC39), // Lime
                                Color(0xFFFFEB3B), // Amber
                                Color(0xFFFF5722), // Deep Orange
                                
                                // Темные цвета
                                Color(0xFF424242), // Dark Grey
                                Color(0xFF212121), // Very Dark Grey
                                Color(0xFF1976D2), // Dark Blue
                                Color(0xFF388E3C), // Dark Green
                                Color(0xFFD32F2F), // Dark Red
                                Color(0xFF7B1FA2), // Dark Purple
                                Color(0xFFF57C00), // Dark Orange
                                Color(0xFF303F9F)  // Dark Indigo
                            )
                            items(colors.size) { index ->
                                val color = colors[index]
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color,
                                            CircleShape
                                        )
                                        .clickable { newCategoryColor = color }
                                ) {
                                    if (newCategoryColor == color) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Color.Black.copy(alpha = 0.3f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.addCategory(newCategoryName, newCategoryColor)
                                showAddDialog = false
                                newCategoryName = ""
                            }
                        }
                    ) {
                        Text(stringResource(R.string.add))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: UserCategory,
    onDelete: () -> Unit
) {
    val categoryName = when (category.name) {
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
        else -> category.name
    }

    val categoryColor = category.getColor()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(categoryColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = categoryName,
                modifier = Modifier.weight(1f)
            )
            if (!category.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
