package com.example.mytravelcompanion.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Plan : Screen("Plan", "Piani", Icons.Default.DateRange)
    object Live : Screen("Live", "Live", Icons.Default.KeyboardArrowUp)
    object Story : Screen("Story", "Storia", Icons.Default.AccountBox)
    object Graphics : Screen("Graphics", "Grafici", Icons.Default.Info)
    object Gallery:Screen("Gallery", "Gallery", Icons.Default.Star)
}