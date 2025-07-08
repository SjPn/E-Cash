package com.example.budget.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.budget.R

object Categories {
    val list = listOf(
        "HOME",
        "GIFTS",
        "FOOD",
        "CAR",
        "IT",
        "SPORT",
        "OTHER",
        "EDUCATION",
        "TAX",
        "HEALTH",
        "LEISURE"
    )

    val icons: Map<String, ImageVector> = mapOf(
        "HOME" to Icons.Default.Home,
        "GIFTS" to Icons.Default.Favorite,
        "FOOD" to Icons.Default.ShoppingCart,
        "OTHER" to Icons.Default.Info,
        "EDUCATION" to Icons.Default.Edit,
        "TAX" to Icons.Default.MailOutline,
        "HEALTH" to Icons.Default.ThumbUp,
        "LEISURE" to Icons.Default.Face,
        "CAR" to Icons.Default.LocationOn,
        "IT" to Icons.Default.Call,
        "SPORT" to Icons.Default.CheckCircle
    )

    val colors: Map<String, Color> = mapOf(
        "HOME" to Color(0xFFFFC107),      // Желтый
        "GIFTS" to Color(0xFFE91E63),     // Розовый
        "FOOD" to Color(0xFF4CAF50),      // Зеленый
        "CAR" to Color(0xFF2196F3),       // Синий
        "IT" to Color(0xFF9C27B0),        // Фиолетовый
        "SPORT" to Color(0xFFFF5722),     // Оранжевый
        "EDUCATION" to Color(0xFF009688), // Бирюзовый
        "TAX" to Color(0xFF795548),       // Коричневый
        "HEALTH" to Color(0xFFF44336),    // Красный
        "LEISURE" to Color(0xFF3F51B5),   // Индиго
        "OTHER" to Color.Black            // Черный
    )

    val darkCategoryColors: Map<String, Color> = mapOf(
        "HOME" to Color(0xFFFFD54F),      // Мягкий жёлтый
        "GIFTS" to Color(0xFFF48FB1),     // Мягкий розовый
        "FOOD" to Color(0xFF81C784),      // Мягкий зелёный
        "CAR" to Color(0xFF64B5F6),       // Мягкий синий
        "IT" to Color(0xFFBA68C8),        // Мягкий фиолетовый
        "SPORT" to Color(0xFFFF8A65),     // Мягкий оранжевый
        "EDUCATION" to Color(0xFF4DB6AC), // Мягкий бирюзовый
        "TAX" to Color(0xFFA1887F),       // Мягкий коричневый
        "HEALTH" to Color(0xFFE57373),    // Мягкий красный
        "LEISURE" to Color(0xFF7986CB),   // Мягкий индиго
        "OTHER" to Color(0xFFB0BEC5)      // Мягкий серый
    )
} 