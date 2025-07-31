package com.example.sitevent.ui.screen.Club.Event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.AdditionalInfo
import com.example.sitevent.data.model.Event
import com.example.sitevent.data.model.EventMode
import com.example.sitevent.ui.viewModel.EventViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    categoryId: String,
    clubId: String,
    viewModel: EventViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    // --- Form state
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var about by rememberSaveable { mutableStateOf("") }
    var prizePool by rememberSaveable { mutableStateOf("") }
    var perks by rememberSaveable { mutableStateOf("") }    // comma‐separated
    var venue by rememberSaveable { mutableStateOf("") }
    var posterUri by remember { mutableStateOf<Uri?>(null) }
    var mode by rememberSaveable { mutableStateOf(EventMode.SINGLE) }
    var minTeam by rememberSaveable { mutableStateOf("") }
    var maxTeam by rememberSaveable { mutableStateOf("") }
    var isOnline by rememberSaveable { mutableStateOf(false) }
    var registrationRequired by rememberSaveable { mutableStateOf(false) }
    var haveSubEvents by rememberSaveable { mutableStateOf(false) }

    // Additional dynamic fields
    var extraInfo by remember { mutableStateOf(listOf<AdditionalInfo>()) }

    // Date & Time (nullable => blank initially)
    var regStartDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var regStartTime by rememberSaveable { mutableStateOf<LocalTime?>(null) }
    var regEndDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var regEndTime by rememberSaveable { mutableStateOf<LocalTime?>(null) }
    var eventStartDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var eventStartTime by rememberSaveable { mutableStateOf<LocalTime?>(null) }
    var eventEndDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var eventEndTime by rememberSaveable { mutableStateOf<LocalTime?>(null) }

    // Picker helpers
    fun pickDate(initial: LocalDate, onDate: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onDate(LocalDate.of(year, month + 1, day))
            },
            initial.year, initial.monthValue - 1, initial.dayOfMonth
        ).show()
    }
    fun pickTime(initial: LocalTime, onTime: (LocalTime) -> Unit) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                onTime(LocalTime.of(hour, minute))
            },
            initial.hour, initial.minute, true
        ).show()
    }

    // --- Observe creation status
    val opStatus by viewModel.operationStatus.collectAsState(initial = null)
    LaunchedEffect(opStatus) {
        when (opStatus) {
            is Resource.Success -> navController.popBackStack()
            is Resource.Error -> scope.launch {
                snackbarHost.showSnackbar((opStatus as Resource.Error).toString())
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Event") },
                navigationIcon = {
                    IconButton(onClick =  { navController.popBackStack() })
                    { Icon(Icons.Default.Edit, contentDescription = "Back") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton (onClick =  { navController.popBackStack() } ){ Text("Cancel") }
                Button(
                    onClick = {
                        // helper to convert date+time to Timestamp?
                        fun toTs(d: LocalDate?, t: LocalTime?) =
                            d?.atTime(t ?: LocalTime.MIDNIGHT)
                                ?.atZone(ZoneId.systemDefault())
                                ?.toInstant()?.let { Timestamp(it) }

                        viewModel.createEvent(
                            Event(
                                title = title,
                                description = description,
                                about = about.ifBlank { null },
                                prizePool = prizePool.ifBlank { null },
                                perks = perks.split(",")
                                    .map(String::trim).filter(String::isNotEmpty),
                                clubId = clubId,
                                categoryId = categoryId,
                                posterUrl = posterUri?.toString(),
                                venue = if (isOnline) "Online" else venue,
                                registrationStartTime = toTs(regStartDate, regStartTime),
                                registrationEndTime   = toTs(regEndDate,   regEndTime),
                                startTime             = toTs(eventStartDate, eventStartTime),
                                endTime               = toTs(eventEndDate,   eventEndTime),
                                mode = mode,
                                minTeamSize = minTeam.toIntOrNull() ?: 1,
                                maxTeamSize = maxTeam.toIntOrNull() ?: 1,
                                isOnline = isOnline,
                                registrationRequired = registrationRequired,
                                haveSubEvent = haveSubEvents,
                                additionalInfoDuringRegistration = extraInfo
                            )
                        )
                    },
                    modifier = Modifier.height(48.dp)
                ) { Text("Create") }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // — Basic Info & Poster
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Short Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .verticalScroll(rememberScrollState())
                    )
                    OutlinedTextField(
                        value = about,
                        onValueChange = { about = it },
                        label = { Text("About / Details") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .verticalScroll(rememberScrollState())
                    )
                    OutlinedTextField(
                        value = prizePool,
                        onValueChange = { prizePool = it },
                        label = { Text("Prize Pool") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = perks,
                        onValueChange = { perks = it },
                        label = { Text("Perks (comma‑separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = posterUri,
                            contentDescription = "Poster",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                .clickable { /* TODO: pick image */ }
                        )
                        TextButton(onClick = { /* TODO: pick image */ }) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Upload Poster")
                        }
                    }
                }
            }

            // — Settings / Team / Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Settings", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EventMode.values().forEach { m ->
                            FilterChip(
                                selected = mode == m,
                                onClick = { mode = m },
                                label = { Text(m.name.capitalize()) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minTeam,
                            onValueChange = { minTeam = it.filter(Char::isDigit) },
                            label = { Text("Min Team") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = maxTeam,
                            onValueChange = { maxTeam = it.filter(Char::isDigit) },
                            label = { Text("Max Team") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isOnline, onCheckedChange = { isOnline = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Online Event")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = registrationRequired, onCheckedChange = { registrationRequired = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Registration Required")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = haveSubEvents, onCheckedChange = { haveSubEvents = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Have Sub‑Events")
                    }
                }
            }

            // — Date & Time Sections
            DateTimeSection(
                label = "Registration Window",
                startDate = regStartDate,
                startTime = regStartTime,
                endDate = regEndDate,
                endTime = regEndTime,
                onPickStartDate = { pickDate(regStartDate ?: LocalDate.now()) { regStartDate = it } },
                onPickStartTime = { pickTime(regStartTime ?: LocalTime.now()) { regStartTime = it } },
                onPickEndDate = { pickDate(regEndDate ?: LocalDate.now()) { regEndDate = it } },
                onPickEndTime = { pickTime(regEndTime ?: LocalTime.now()) { regEndTime = it } }
            )
            Spacer(Modifier.height(16.dp))
            DateTimeSection(
                label = "Event Schedule",
                startDate = eventStartDate,
                startTime = eventStartTime,
                endDate = eventEndDate,
                endTime = eventEndTime,
                onPickStartDate = { pickDate(eventStartDate ?: LocalDate.now()) { eventStartDate = it } },
                onPickStartTime = { pickTime(eventStartTime ?: LocalTime.now()) { eventStartTime = it } },
                onPickEndDate = { pickDate(eventEndDate ?: LocalDate.now()) { eventEndDate = it } },
                onPickEndTime = { pickTime(eventEndTime ?: LocalTime.now()) { eventEndTime = it } }
            )

            // — Additional Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Additional Info", style = MaterialTheme.typography.titleMedium)
                    extraInfo.forEachIndexed { idx, info ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = info.key,
                                onValueChange = { newKey ->
                                    extraInfo = extraInfo.toMutableList().also {
                                        it[idx] = it[idx].copy(key = newKey)
                                    }
                                },
                                label = { Text("Key") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = info.value,
                                onValueChange = { newVal ->
                                    extraInfo = extraInfo.toMutableList().also {
                                        it[idx] = it[idx].copy(value = newVal)
                                    }
                                },
                                label = { Text("Value") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    TextButton(onClick = {
                        extraInfo = extraInfo + AdditionalInfo()
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Field")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateTimeSection(
    label: String,
    startDate: LocalDate?,
    startTime: LocalTime?,
    endDate: LocalDate?,
    endTime: LocalTime?,
    onPickStartDate: () -> Unit,
    onPickStartTime: () -> Unit,
    onPickEndDate: () -> Unit,
    onPickEndTime: () -> Unit,
) {
    val dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeFmt = DateTimeFormatter.ofPattern("hh:mm a")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPickStartDate,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(startDate?.format(dateFmt) ?: "Select date")
                }
                OutlinedButton(
                    onClick = onPickStartTime,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(startTime?.format(timeFmt) ?: "Select time")
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPickEndDate,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(endDate?.format(dateFmt) ?: "Select date")
                }
                OutlinedButton(
                    onClick = onPickEndTime,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(endTime?.format(timeFmt) ?: "Select time")
                }
            }
        }
    }
}