package com.example.mytravelcompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytravelcompanion.ui.theme.blu
import com.example.mytravelcompanion.ui.theme.ciano
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.mytravelcompanion.data.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.mytravelcompanion.R
import com.example.mytravelcompanion.ui.theme.myTipography2
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun Plan() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val factory = remember { TripViewModelFactory(db.tripDao(), db.MarkerDAO(), db.JourneyDAO()) }
    val viewModel: TripViewModel = viewModel(factory = factory)
    val trips by viewModel.trips.collectAsState(initial = emptyList())
    val today = LocalDate.now()
    val scheduledTrips = trips.filter { it.startDate != null && it.startDate.isAfter(today) }

    var destination by remember { mutableStateOf("") }
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTripType by remember { mutableStateOf(TripType.LOCAL) }
    var showStartModal by remember { mutableStateOf(false) }
    var showEndModal by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val scrollState = rememberScrollState()

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
                "Nuovo viaggio",
                fontSize = 26.sp,
                style = myTipography2.titleLarge
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = blu,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Destinazione
                OutlinedTextField(
                    maxLines = 1,
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destinazione") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Tipo di viaggio
                DropDownDemo(
                    modifier = Modifier.fillMaxWidth(),
                    selectedItem = selectedTripType,
                    onItemSelected = { selectedTripType = it }
                )

                // Data partenza
                OutlinedTextField(
                    value = selectedStartDate?.format(formatter) ?: "",
                    onValueChange = {},
                    label = { Text("Data partenza") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartModal = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Seleziona data")
                        }
                    }
                )
                if (showStartModal) {
                    DatePickerModal(
                        onDateSelected = { selectedStartDate = it },
                        onDismiss = { showStartModal = false }
                    )
                }

                if (showEndModal) {
                    DatePickerModal(
                        onDateSelected = { selectedEndDate = it },
                        onDismiss = { showEndModal = false }
                    )
                }

                // Data fine (solo se viaggio di più giorni)
                if (selectedTripType == TripType.MULTIDAY) {
                    OutlinedTextField(
                        value = selectedEndDate?.format(formatter) ?: "",
                        onValueChange = {},
                        label = { Text("Data fine") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showEndModal = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleziona data")
                            }
                        }
                    )
                }

                // Bottone Salva
                Button(
                    onClick = {
                        val finalEndDate = if (selectedTripType == TripType.MULTIDAY) {
                            selectedEndDate
                        } else {
                            selectedStartDate
                        }

                        viewModel.addTrip(
                            Trip(
                                destination = destination,
                                tripType = selectedTripType,
                                startDate = selectedStartDate,
                                endDate = finalEndDate
                            )
                        )

                        destination = ""
                        selectedStartDate = null
                        selectedEndDate = null
                        selectedTripType = TripType.LOCAL
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ciano),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Icona salvataggio",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Salva viaggio",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text="Viaggi programmati",style = myTipography2.titleLarge)

            if (scheduledTrips.isEmpty()) {
                Text(
                    text = "Nessun viaggio programmato",
                    fontSize = 18.sp,
                    style = myTipography2.bodyLarge
                )
            } else {
                scheduledTrips.forEach {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = blu,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp)
                    )
                    {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = it.destination,
                                    fontSize = 18.sp,
                                    style = myTipography2.titleLarge
                                )
                                Text(
                                    text = "${it.startDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} - ${it.endDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                    fontSize = 12.sp,
                                    style = myTipography2.bodyLarge
                                )
                                Text(
                                    text = "(${it.tripType.displayName})",
                                    fontSize = 12.sp,
                                    style = myTipography2.bodyLarge
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Icona elimina",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            viewModel.deleteTrip(it)
                                        }
                                )
                            }
                        }


                    }

                }
            }
            Button(
                onClick = {
                    viewModel.deleteAllTrips()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Icona elimina tutti",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Elimina tutti i viaggi",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedDate: LocalDate? = datePickerState.selectedDateMillis?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                onDateSelected(selectedDate)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun DropDownDemo(
    modifier: Modifier = Modifier,
    selectedItem: TripType,
    onItemSelected: (TripType) -> Unit
){
    val isDropDownExpanded = remember { mutableStateOf(false) }
    val tripTypes = TripType.values()

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDropDownExpanded.value = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Tipologia: ",
                style = myTipography2.labelMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = selectedItem.displayName,
                style = myTipography2.bodyLarge)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Icona dropdown",
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            expanded = isDropDownExpanded.value,
            onDismissRequest = { isDropDownExpanded.value = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            tripTypes.forEachIndexed { index, type ->
                DropdownMenuItem(
                    text = { Text(
                        text = type.displayName,
                        style = myTipography2.bodyLarge,) },
                    onClick = {
                        onItemSelected(type)
                        isDropDownExpanded.value = false
                    }
                )
            }
        }
    }
}

