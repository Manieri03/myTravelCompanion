package com.example.mytravelcompanion.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.data.TripViewModelFactory
import com.example.mytravelcompanion.ui.theme.ciano
import com.example.mytravelcompanion.ui.theme.myTipography2
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import com.example.mytravelcompanion.R

@Composable
fun Gallery() {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: TripViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = TripViewModelFactory(db.tripDao(), db.MarkerDAO(), db.JourneyDAO(), db.PointDAO())
    )

    val memories by viewModel.photos.collectAsState()
    val scrollState = rememberScrollState()

    var selectedImage by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                val bitmap = remember(path) {
                                    try {
                                        if (path.startsWith("content://")) {
                                            val stream =
                                                context.contentResolver.openInputStream(android.net.Uri.parse(path))
                                            BitmapFactory.decodeStream(stream).also { stream?.close() }
                                        } else {
                                            BitmapFactory.decodeFile(path)
                                        }
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "Foto ricordo",
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(2.dp, ciano, RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedImage = it
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedImage != null) {
        Dialog(onDismissRequest = { selectedImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = selectedImage!!.asImageBitmap(),
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
                    TextButton(onClick = { selectedImage = null }) {
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
