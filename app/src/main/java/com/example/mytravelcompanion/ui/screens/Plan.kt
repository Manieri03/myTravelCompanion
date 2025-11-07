package com.example.mytravelcompanion.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.example.mytravelcompanion.R
import com.example.mytravelcompanion.ui.theme.myTipography2
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun Plan() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val factory = remember { TripViewModelFactory(db.tripDao(), db.MarkerDAO(), db.JourneyDAO(), db.PointDAO()) }
    val viewModel: TripViewModel = viewModel(factory = factory)
    val trips by viewModel.trips.collectAsState(initial = emptyList())
    val points by viewModel.points.collectAsState(initial = emptyList())
    LaunchedEffect(Unit) {
        viewModel.loadPoints(context)
    }

    val today = LocalDate.now()
    val scheduledTrips = trips.filter { it.startDate != null && it.startDate.isAfter(today) }

    //viaggio
    var destination by remember { mutableStateOf("") }
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTripType by remember { mutableStateOf(TripType.LOCAL) }
    var showStartModal by remember { mutableStateOf(false) }
    var showEndModal by remember { mutableStateOf(false) }

    //punto di interesse
    var name by remember { mutableStateOf("") }
    var showMap by remember{mutableStateOf(false)}

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val scrollState = rememberScrollState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 18.dp, end = 18.dp, top = 55.dp, bottom = 18.dp)
            .verticalScroll(scrollState)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(45.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.tc_logo),
            contentDescription = "Logo app",
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Text(
            "Nuovo viaggio",
            fontSize = 26.sp,
            style = myTipography2.titleLarge
        )

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

                        val today = LocalDate.now()

                        if (destination.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Inserisci una destinazione prima di salvare")
                            }
                            return@Button
                        }

                        if (selectedStartDate == null || selectedStartDate!!.isBefore(today)) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("La data di partenza non può essere precedente ad oggi")
                            }
                            return@Button
                        }

                        if (selectedTripType == TripType.MULTIDAY) {
                            if (selectedEndDate == null || selectedEndDate!!.isBefore(selectedStartDate)) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("La data di fine deve essere dopo la partenza")
                                }
                                return@Button
                            }
                        }
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

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Viaggio salvato con successo")
                        }

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ciano),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
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

        Text(
            "Nuovo punto",
            fontSize = 26.sp,
            style = myTipography2.titleLarge
        )

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

                OutlinedTextField(
                    maxLines = 1,
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = { showMap = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ciano),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Icona salvataggio",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Salva sulla mappa",
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }


        if (showMap) {
            PointMapDialog(
                name = name,
                onDismiss = { showMap = false },
                onSavePoint = { lat, lng ->
                    viewModel.addPoint(Point(name = name, latitude = lat, longitude = lng),context)
                    name = ""
                    showMap = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Punto di interesse salvato con successo")
                    }
                }
            )
        }

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Text("I tuoi viaggi e luoghi", style=myTipography2.titleLarge, fontSize = 26.sp)

            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Icona viaggi",
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Viaggi programmati",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleLarge
                )
            }

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
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Viaggio programmato eliminato con successo")
                                            }
                                        }
                                )
                            }
                        }


                    }

                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Icona punti",
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Punti di interesse",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (points.isEmpty()) {
                Text(
                    text = "Nessun punto salvato",
                    fontSize = 18.sp,
                    style = myTipography2.bodyLarge
                )
            } else {
                points.forEach { point ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = blu,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = point.name,
                                    fontSize = 18.sp,
                                    style = myTipography2.titleLarge
                                )
                                Text(
                                    text = "Lat: ${"%.5f".format(point.latitude)}, Lng: ${"%.5f".format(point.longitude)}",
                                    fontSize = 12.sp,
                                    style = myTipography2.bodyLarge
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Elimina punto",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            viewModel.deletePoint(point,context)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Punto di interesse eliminato con successo")
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        snackbar = { snackbarData ->
            Snackbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = ciano,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    color = Color.White,
                    style=myTipography2.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )

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

@Composable
fun PointMapDialog(
    name: String,
    onDismiss: () -> Unit,
    onSavePoint: (lat: Double, lng: Double) -> Unit
) {
    var customPointIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState()

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation!!, 14f)
                }
            }
        }
    }

    // Recupero posizione attuale
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation!!, 14f)
                }
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    selectedLatLng?.let { onSavePoint(it.latitude, it.longitude) }
                    onDismiss()
                }
            ) {
                Text("Salva punto", style=myTipography2.bodyLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla", style=myTipography2.bodyLarge) }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (userLocation == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Recupero posizione...")
                    }
                } else {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        onMapClick = { latLng ->
                            selectedLatLng = latLng
                        },
                        onMapLoaded={
                            try {
                                val sizeDp = 40
                                val density = context.resources.displayMetrics.density
                                val sizePx = (sizeDp * density).toInt()

                                fun loadScaledIcon(resId: Int): BitmapDescriptor {
                                    val bmp = android.graphics.BitmapFactory.decodeResource(context.resources, resId)
                                    val scaled = android.graphics.Bitmap.createScaledBitmap(bmp, sizePx, sizePx, false)
                                    return BitmapDescriptorFactory.fromBitmap(scaled)
                                }

                                customPointIcon = loadScaledIcon(R.drawable.point_interest_icon)
                            } catch (e: Exception) {
                                customPointIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                            }
                        }
                    ) {
                        selectedLatLng?.let {
                            Marker(
                                state = MarkerState(position = it),
                                title = name.ifEmpty { "Punto di interesse" },
                                icon = customPointIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                            )
                        }
                    }
                }
            }
        }
    )
}