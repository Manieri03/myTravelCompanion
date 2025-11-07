package com.example.mytravelcompanion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mytravelcompanion.R

val SoraFamily= FontFamily(
    Font(R.font.sora_regular, FontWeight.Normal),
    Font(R.font.sora_bold, FontWeight.Bold),
    Font(R.font.sora_semibold, FontWeight.SemiBold)
)

val myTipography2 = Typography(
    bodyLarge = TextStyle(
        fontFamily = SoraFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SoraFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SoraFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )
)
