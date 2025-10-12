package com.example.mytravelcompanion.ui.screens

import androidx.compose.foundation.background
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

@Composable
fun Plan() {
    var destination by remember { mutableStateOf("") }
    var selectedStartDate by remember { mutableStateOf<Long?>(null) }
    var selectedEndDate by remember { mutableStateOf<Long?>(null) }
    var showStartModal by remember { mutableStateOf(false) }
    var showEndModal by remember { mutableStateOf(false) }
    var selectedTripType by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        Row {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Icona pianificazione",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 8.dp)
            )
            Text("Pianifica il tuo viaggio", fontSize = 20.sp)
        }

        //destinazione
        OutlinedTextField(
            maxLines = 1,
            value = destination,
            onValueChange = { destination = it },
            label = { Text("Destinazione") },
            modifier = Modifier.fillMaxWidth()
        )

        //tipo di viaggio
        DropDownDemo(
            modifier = Modifier.fillMaxWidth(),
            selectedItem = selectedTripType,
            onItemSelected = { selectedTripType = it }
        )

        // data partenza
        OutlinedTextField(
            value = selectedStartDate?.let {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
            } ?: "",
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
                onDateSelected = {
                    selectedStartDate = it
                    showStartModal = false
                },
                onDismiss = { showStartModal = false }
            )
        }

        // data fine (solo se viaggio di più giorni)
        if (selectedTripType == 2) {
            OutlinedTextField(
                value = selectedEndDate?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                } ?: "",
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

            if (showEndModal) {
                DatePickerModal(
                    onDateSelected = {
                        selectedEndDate = it
                        showEndModal = false
                    },
                    onDismiss = { showEndModal = false }
                )
            }
        }

        Button(
            onClick = {
                //salvataggio...
                println("Viaggio salvato!")
                println("Destinazione: $destination")
                println("Tipo viaggio: $selectedTripType")
                println("Data partenza: $selectedStartDate")
                println("Data fine: $selectedEndDate")
            },
            colors = ButtonDefaults.buttonColors(containerColor = ciano),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Icona pianificazione",
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Salva viaggio",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyLarge)
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
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
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
){
    val isDropDownExpanded = remember { mutableStateOf(false) }
    val tripTypes = listOf("Viaggio locale", "Gita di un giorno","Viaggio di più giorni")

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDropDownExpanded.value = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Tipologia: ")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = tripTypes[selectedItem])
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
                    text = { Text(text = type) },
                    onClick = {
                        onItemSelected(index)
                        isDropDownExpanded.value = false
                    }
                )
            }
        }
    }
}
