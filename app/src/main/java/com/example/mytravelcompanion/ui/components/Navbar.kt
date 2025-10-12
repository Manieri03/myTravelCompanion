package com.example.mytravelcompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mytravelcompanion.navigation.Screen
import com.example.mytravelcompanion.ui.theme.*

@Composable
fun Navbar(navController: NavHostController) {
    val items = listOf(
        Screen.Plan,
        Screen.Live,
        Screen.Story,
        Screen.Graphics,
        Screen.Gallery
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(beige),
        containerColor = ciano,
        tonalElevation = 6.dp
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            //box per selezione
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp)
                    .clip(
                        RoundedCornerShape(if (selected) 18.dp else 12.dp)
                    )
                    .background(
                        if (selected) blu else Color.Transparent
                    )
                    .clickable {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    .padding(vertical = if (selected) 10.dp else 6.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        screen.icon,
                        contentDescription = screen.title,
                        tint = if (selected) Color.White else blu,
                        modifier = Modifier.size(if (selected) 34.dp else 28.dp)
                    )
                    Text(
                        text = screen.title,
                        fontSize = if (selected) 13.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else blu
                    )
                }
            }
        }
    }
}
