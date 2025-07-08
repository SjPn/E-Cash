package com.example.budget.data

import kotlinx.coroutines.flow.Flow
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val expenseByCategory: Flow<List<CategorySum>> = transactionDao.getExpenseSumByCategory()
    val incomeByCategory: Flow<List<CategorySum>> = transactionDao.getIncomeSumByCategory()

    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetweenDates(startDate, endDate)

    fun getExpenseSumByCategoryBetweenDates(startDate: Date, endDate: Date): Flow<List<CategorySum>> =
        transactionDao.getExpenseSumByCategoryBetweenDates(startDate, endDate)

    fun getIncomeSumByCategoryBetweenDates(startDate: Date, endDate: Date): Flow<List<CategorySum>> =
        transactionDao.getIncomeSumByCategoryBetweenDates(startDate, endDate)

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

    suspend fun deleteAll() {
        transactionDao.deleteAll()
    }
} 