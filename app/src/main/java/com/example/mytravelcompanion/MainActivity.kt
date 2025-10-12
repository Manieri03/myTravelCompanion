package com.example.mytravelcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mytravelcompanion.ui.theme.MyTravelCompanionTheme
import com.example.mytravelcompanion.navigation.NavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
           MyTravelCompanionTheme {
                NavHost()
            }
        }
    }
}
