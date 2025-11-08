package com.example.mytravelcompanion.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.ui.theme.ciano
import com.example.mytravelcompanion.ui.theme.myTipography2
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mytravelcompanion.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Gallery(viewModel: TripViewModel) {
    val context = LocalContext.current

    val memories by viewModel.photos.collectAsState()
    val scrollState = rememberScrollState()

    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMemories()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 18.dp, end = 18.dp, top = 55.dp, bottom = 18.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(45.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tc_logo),
                contentDescription = "Logo app",
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "La tua galleria",
                fontSize = 26.sp,
                style = myTipography2.titleLarge
            )
        }

        if (memories.isEmpty()) {
            Text(
                "Non hai ancora aggiunto foto",
                style = myTipography2.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            memories.forEach { (tripDestination, photos) ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Viaggio a $tripDestination",
                        fontSize = 20.sp,
                        style = myTipography2.titleMedium
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        photos.forEach { marker ->
                            marker.photoPath?.let { path ->
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(path)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto ricordo",
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(2.dp, ciano, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedImagePath = path
                                        }
                                )

                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedImagePath != null) {
        Dialog(onDismissRequest = { selectedImagePath = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(selectedImagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto ingrandita",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, ciano, RoundedCornerShape(16.dp))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, ciano, RoundedCornerShape(16.dp))
                        .background(ciano)
                ) {
                    TextButton(onClick = { selectedImagePath = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

}
