package com.example.budget

import android.app.Application
import com.example.budget.data.AppDatabase
import com.example.budget.data.TransactionRepository

class BudgetApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
} 