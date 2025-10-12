package com.example.mytravelcompanion.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*

import com.example.mytravelcompanion.ui.components.Navbar
import com.example.mytravelcompanion.ui.screens.Plan
import com.example.mytravelcompanion.ui.screens.Gallery
import com.example.mytravelcompanion.ui.screens.Live
import com.example.mytravelcompanion.ui.screens.Story
import com.example.mytravelcompanion.ui.screens.Graphics

@Composable
fun NavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { Navbar(navController) }
    ) { paddingValues ->
        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = "Plan",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("Plan") { Plan() }
            composable("Live") { Live() }
            composable("Story") { Story() }
            composable("Graphics") { Graphics() }
            composable("Gallery") { Gallery() }
        }
    }
}