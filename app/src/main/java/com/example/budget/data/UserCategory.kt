package com.example.budget.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.Color

@Entity(tableName = "user_categories")
data class UserCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: Long, // Color as Long value
    val isDefault: Boolean = false
) {
    fun getColor(): Color {
        return Color(color)
    }
    
    companion object {
        fun fromColor(color: Color): Long {
            // Convert Color to ARGB integer, then to Long
            val argb = (color.alpha * 255).toInt() shl 24 or
                      ((color.red * 255).toInt() shl 16) or
                      ((color.green * 255).toInt() shl 8) or
                      (color.blue * 255).toInt()
            return argb.toLong()
        }
    }
}
