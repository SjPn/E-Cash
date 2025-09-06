package com.example.budget.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val expenseByCategory: Flow<List<CategorySum>> = transactionDao.getExpenseSumByCategory()
    val incomeByCategory: Flow<List<CategorySum>> = transactionDao.getIncomeSumByCategory()


    fun getTransactionById(id: Int): Flow<Transaction?> = transactionDao.getTransactionById(id)

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    
    suspend fun updateTransactionsCategory(oldCategory: String, newCategory: String) {
        transactionDao.updateTransactionsCategory(oldCategory, newCategory)
    }
    
    fun getTransactionsByCategory(categoryName: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(categoryName)
    }
} 