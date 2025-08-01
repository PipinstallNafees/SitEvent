package com.example.sitevent.ui.screen.Club.Event

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.EventMode
import com.example.sitevent.data.model.Team
import com.example.sitevent.data.model.Ticket
import com.example.sitevent.ui.viewModel.EventViewModel
import com.example.sitevent.ui.viewModel.TicketViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventRegistrationScreen(
    navController: NavController,
    categoryId: String,
    clubId: String,
    eventId: String,
    userId: String,
    eventViewModel: EventViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel(),
) {
    // 1) Load event
    LaunchedEffect(eventId) {
        eventViewModel.getEvent(categoryId, clubId, eventId)
    }
    val event by eventViewModel.event.collectAsStateWithLifecycle()
    val actionStatus by ticketViewModel.actionStatus.collectAsStateWithLifecycle(null)

    // 2) Local UI state
    var participation by remember {
        mutableStateOf(
            when (event?.mode) {
                EventMode.SINGLE -> "Individual"
                EventMode.GROUP -> "Group"
                EventMode.BOTH -> "Group"
                else -> "Individual"
            }
        )
    }
    var teamName by remember { mutableStateOf("") }
    val memberIds = remember { mutableStateListOf<String>() }
    LaunchedEffect(event?.minTeamSize) {
        memberIds.clear()
        repeat(event?.minTeamSize ?: 1) { memberIds.add("") }
    }

    // Additional‐info answers
    val extraAnswers = remember { mutableStateMapOf<String, String>() }
        .apply { event?.additionalInfoAskFromUser?.forEach { putIfAbsent(it.key, "") } }

    // Dialog controls
    var showMemberDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val candidates by ticketViewModel.membersNotRegistered.collectAsStateWithLifecycle(emptyList())
    val selectedIds = remember { mutableStateListOf<String>() }

    // Fetch candidates when dialog opens
    LaunchedEffect(showMemberDialog) {
        if (showMemberDialog && event != null) {
            ticketViewModel.getMembersNotRegistered(
                categoryId,
                clubId,
                eventId,
                event!!.participantsIds
            )
            selectedIds.clear()
        }
    }

    // Main content
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Header
        Column(Modifier.padding(24.dp)) {
            Text(
                text = event?.title ?: "",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(8.dp))
            event?.startTime?.let {
                Text(
                    text = "${it.toDateString()} • ${it.toTimeString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // Participation section
        SectionCard(title = "Participation") {
            if (event?.mode == EventMode.BOTH) {
                SegmentedControl(
                    options = listOf("Individual", "Group"),
                    selected = participation,
                    onSelect = { participation = it }
                )
            } else {
                Text(
                    text = if (event?.mode == EventMode.SINGLE) "Individual" else "Group",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Team details
//        if (participation == "Group") {
            SectionCard(title = "Team Details") {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { showMemberDialog = true }) {
                    Text("Select Members (${memberIds.size}/${event?.maxTeamSize})")
                }
                Spacer(Modifier.height(8.dp))
                memberIds.forEach { id ->
                    Text("• $id", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                }
            }
//        }

        // Additional-info fields
        if (!event?.additionalInfoAskFromUser.isNullOrEmpty()) {
            SectionCard(title = "Additional Information") {
                event!!.additionalInfoAskFromUser.forEach { info ->
                    OutlinedTextField(
                        value = extraAnswers[info.key]!!,
                        onValueChange = { extraAnswers[info.key] = it },
                        label = { Text(info.key) },
                        isError = info.required && extraAnswers[info.key]?.isBlank() == true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Submit button
        Button(
            onClick = {
                val team = Team(
                    teamName = teamName,
                    teamMemberIds = memberIds.filter { it.isNotBlank() },
                    teamLeaderId = userId,
                    eventId = eventId,
                    clubId = clubId,
                    categoryId = categoryId
                )
                val ticket = Ticket(
                    categoryId = categoryId,
                    clubId = clubId,
                    eventId = eventId,
                    userId = userId,
                    teamId = if (participation == "Group") team.teamId else "",
                    additionalInfoAskByEventOrganizer = event?.additionalInfoAskFromUser
                        ?: emptyList()
                )
                ticketViewModel.issueTicket(ticket, team)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Register", style = MaterialTheme.typography.labelLarge)
        }
    }

    // Member-selection dialog
    if (showMemberDialog) {
        AlertDialog(
            onDismissRequest = { showMemberDialog = false },
            title = { Text("Select Members") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search by ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    val filtered = candidates.filter {
                        it.userId.contains(searchQuery, ignoreCase = true)
                    }
                    Column(modifier = Modifier.height(200.dp)) {
                        filtered.forEach { user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (user.userId in selectedIds) selectedIds.remove(user.userId)
                                        else if (selectedIds.size < (event?.maxTeamSize
                                                ?: Int.MAX_VALUE)
                                        )
                                            selectedIds.add(user.userId)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = user.userId in selectedIds,
                                    onCheckedChange = {
                                        if (it && selectedIds.size < (event?.maxTeamSize
                                                ?: Int.MAX_VALUE)
                                        )
                                            selectedIds.add(user.userId)
                                        else
                                            selectedIds.remove(user.userId)
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(user.userId, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val minSize = event?.minTeamSize ?: 1
                Button(
                    onClick = {
                        memberIds.clear()
                        memberIds.addAll(selectedIds)
                        showMemberDialog = false
                    },
                    enabled = selectedIds.size in minSize..(event?.maxTeamSize ?: Int.MAX_VALUE)
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMemberDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Handle Loading / Error / Success
    when (actionStatus) {
        is Resource.Loading -> FullScreenLoading()
        is Resource.Error -> FullScreenError((actionStatus as Resource.Error).exception)
        is Resource.Success -> LaunchedEffect(Unit) { navController.popBackStack() }
        else -> {}
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        options.forEach { opt ->
            Box(
                Modifier
                    .weight(1f)
                    .clickable { onSelect(opt) }
                    .background(if (opt == selected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    opt,
                    color = if (opt == selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FullScreenError(e: Throwable?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(e?.localizedMessage ?: "Error", color = MaterialTheme.colorScheme.error)
    }
}

private fun Timestamp.toDateString(): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(this.toDate())

private fun Timestamp.toTimeString(): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(this.toDate())
