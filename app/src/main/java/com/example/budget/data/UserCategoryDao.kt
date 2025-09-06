package com.example.budget.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCategoryDao {
    @Query("SELECT * FROM user_categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<UserCategory>>
    
    @Query("SELECT * FROM user_categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): UserCategory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: UserCategory)
    
    @Delete
    suspend fun deleteCategory(category: UserCategory)
    
    @Update
    suspend fun updateCategory(category: UserCategory)
    
    @Query("DELETE FROM user_categories")
    suspend fun deleteAllUserCategories()
}
