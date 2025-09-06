package com.example.budget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budget.data.CategorySum
import com.example.budget.data.Transaction
import com.example.budget.data.TransactionRepository
import com.example.budget.data.TransactionType
import com.example.budget.data.UserCategory
import com.example.budget.data.CategoryRepository
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class BudgetViewModel(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseByCategory: StateFlow<List<CategorySum>> = repository.expenseByCategory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeByCategory: StateFlow<List<CategorySum>> = repository.incomeByCategory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<Double> = transactions.combine(expenseByCategory) { transactions, _ ->
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _editTransactionId = MutableStateFlow<Int?>(null)
    val editTransaction: StateFlow<Transaction?> = _editTransactionId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getTransactionById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Category management methods
    val categories = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize default categories only once when ViewModel is created
        viewModelScope.launch {
            categoryRepository.initializeDefaultCategories()
        }
    }


    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    fun getTransactionById(id: Int) = repository.getTransactionById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setEditTransactionId(id: Int?) {
        _editTransactionId.value = id
    }
    
    fun addCategory(name: String, color: Color) = viewModelScope.launch {
        categoryRepository.addCategory(name, color)
    }
    
    fun deleteCategory(category: UserCategory) = viewModelScope.launch {
        categoryRepository.deleteCategory(category)
    }
    
    fun updateCategory(category: UserCategory) = viewModelScope.launch {
        categoryRepository.updateCategory(category)
    }
    
    fun getTransactionsByCategory(categoryName: String) = repository.getTransactionsByCategory(categoryName)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
}

class BudgetViewModelFactory(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository, categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 