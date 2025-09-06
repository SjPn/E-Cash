package com.example.budget.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

data class CategorySum(
    val category: String,
    val total: Double
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * from transactions WHERE id = :id")
    fun getTransactionById(id: Int): Flow<Transaction?>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' GROUP BY category ORDER BY total DESC")
    fun getExpenseSumByCategory(): Flow<List<CategorySum>>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'INCOME' GROUP BY category ORDER BY total DESC")
    fun getIncomeSumByCategory(): Flow<List<CategorySum>>

    
    @Query("UPDATE transactions SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateTransactionsCategory(oldCategory: String, newCategory: String)
    
    @Query("SELECT * FROM transactions WHERE category = :categoryName")
    fun getTransactionsByCategory(categoryName: String): Flow<List<Transaction>>
} 