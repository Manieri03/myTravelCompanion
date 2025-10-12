package com.example.mytravelcompanion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mytravelcompanion.R

val ChauPhilomeneFamily = FontFamily(
    Font(R.font.chau_philomene_regular, FontWeight.Normal),
    Font(R.font.chau_philomene_italic, FontWeight.Medium)
)

val myTipography = Typography(
    bodyLarge = TextStyle(
        fontFamily = ChauPhilomeneFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ChauPhilomeneFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ChauPhilomeneFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)
