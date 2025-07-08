package com.example.budget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budget.data.CategorySum
import com.example.budget.data.Transaction
import com.example.budget.data.TransactionRepository
import com.example.budget.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

enum class TimeFilter { ALL, WEEK, MONTH, YEAR }

class BudgetViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _timeFilter = MutableStateFlow(TimeFilter.ALL)
    val timeFilter: StateFlow<TimeFilter> = _timeFilter

    val transactions: StateFlow<List<Transaction>> = _timeFilter.flatMapLatest { filter ->
        getFilteredTransactions(filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseByCategory: StateFlow<List<CategorySum>> = _timeFilter.flatMapLatest { filter ->
        getFilteredExpenseByCategory(filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeByCategory: StateFlow<List<CategorySum>> = _timeFilter.flatMapLatest { filter ->
        getFilteredIncomeByCategory(filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun setTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
    }

    private fun getFilteredTransactions(filter: TimeFilter) = when (filter) {
        TimeFilter.ALL -> repository.allTransactions
        else -> {
            val (start, end) = getDatesForFilter(filter)
            repository.getTransactionsBetweenDates(start, end)
        }
    }

    private fun getFilteredExpenseByCategory(filter: TimeFilter) = when (filter) {
        TimeFilter.ALL -> repository.expenseByCategory
        else -> {
            val (start, end) = getDatesForFilter(filter)
            repository.getExpenseSumByCategoryBetweenDates(start, end)
        }
    }

    private fun getFilteredIncomeByCategory(filter: TimeFilter) = when (filter) {
        TimeFilter.ALL -> repository.incomeByCategory
        else -> {
            val (start, end) = getDatesForFilter(filter)
            repository.getIncomeSumByCategoryBetweenDates(start, end)
        }
    }

    private fun getDatesForFilter(filter: TimeFilter): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        when (filter) {
            TimeFilter.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            TimeFilter.MONTH -> calendar.add(Calendar.MONTH, -1)
            TimeFilter.YEAR -> calendar.add(Calendar.YEAR, -1)
            TimeFilter.ALL -> calendar.timeInMillis = 0 // Should not be used with this path
        }
        val startDate = calendar.time
        return Pair(startDate, endDate)
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
}

class BudgetViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 