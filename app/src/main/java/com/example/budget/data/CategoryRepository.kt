package com.example.budget.data

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CategoryRepository(
    private val userCategoryDao: UserCategoryDao,
    private val transactionRepository: TransactionRepository
) {
    
    private var isInitialized = false
    
    fun getAllCategories(): Flow<List<UserCategory>> {
        return userCategoryDao.getAllCategories()
    }
    
    suspend fun addCategory(name: String, color: Color) {
        val category = UserCategory(
            name = name,
            color = UserCategory.fromColor(color),
            isDefault = false
        )
        userCategoryDao.insertCategory(category)
    }
    
    suspend fun deleteCategory(category: UserCategory) {
        if (!category.isDefault) {
            // Update all transactions with this category to "OTHER" before deleting the category
            transactionRepository.updateTransactionsCategory(category.name, "OTHER")
            userCategoryDao.deleteCategory(category)
        }
    }
    
    suspend fun updateCategory(category: UserCategory) {
        userCategoryDao.updateCategory(category)
    }
    
    suspend fun getCategoryByName(name: String): UserCategory? {
        return userCategoryDao.getCategoryByName(name)
    }
    
    suspend fun initializeDefaultCategories() {
        // Prevent multiple initializations in the same session
        if (isInitialized) {
            return
        }
        
        val existingCategories = userCategoryDao.getAllCategories().first()
        
        // Check if colors are corrupted (all transparent) or if we have duplicates
        val hasCorruptedColors = existingCategories.any { it.getColor().alpha == 0f }
        val hasDuplicates = existingCategories.groupBy { it.name }.any { it.value.size > 1 }
        val hasTooManyCategories = existingCategories.size > 20 // More than 2x expected default categories
        
        if (hasCorruptedColors || hasDuplicates || hasTooManyCategories || existingCategories.isEmpty()) {
            userCategoryDao.deleteAllUserCategories()
            
            val defaultCategories = listOf(
                UserCategory(name = "HOME", color = UserCategory.fromColor(Color(0xFFFF6F00)), isDefault = true),
                UserCategory(name = "GIFTS", color = UserCategory.fromColor(Color(0xFFFFC107)), isDefault = true),
                UserCategory(name = "FOOD", color = UserCategory.fromColor(Color(0xFF4CAF50)), isDefault = true),
                UserCategory(name = "CAR", color = UserCategory.fromColor(Color(0xFF2196F3)), isDefault = true),
                UserCategory(name = "IT", color = UserCategory.fromColor(Color(0xFF9C27B0)), isDefault = true),
                UserCategory(name = "EDUCATION", color = UserCategory.fromColor(Color(0xFF1976D2)), isDefault = true),
                UserCategory(name = "TAX", color = UserCategory.fromColor(Color.Black), isDefault = true),
                UserCategory(name = "HEALTH", color = UserCategory.fromColor(Color(0xFFF44336)), isDefault = true),
                UserCategory(name = "LEISURE", color = UserCategory.fromColor(Color(0xFF3F51B5)), isDefault = true),
                UserCategory(name = "OTHER", color = UserCategory.fromColor(Color(0xFF9E9E9E)), isDefault = true)
            )
            
            // Insert all default categories
            defaultCategories.forEach { category ->
                userCategoryDao.insertCategory(category)
            }
        } else {
        }
        
        isInitialized = true
    }
    
    fun getCategoryColor(categoryName: String): Flow<Color> {
        return userCategoryDao.getAllCategories().map { categories ->
            categories.find { it.name == categoryName }?.getColor() ?: Color.Gray
        }
    }
}
