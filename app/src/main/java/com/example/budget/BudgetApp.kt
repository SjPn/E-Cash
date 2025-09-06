package com.example.budget

import android.app.Application
import com.example.budget.data.AppDatabase
import com.example.budget.data.TransactionRepository
import com.example.budget.data.CategoryRepository

class BudgetApp : Application() {
    val database by lazy { 
        AppDatabase.getDatabase(this) 
    }
    val repository by lazy { 
        TransactionRepository(database.transactionDao()) 
    }
    val categoryRepository by lazy { 
        CategoryRepository(database.userCategoryDao(), repository) 
    }
} 