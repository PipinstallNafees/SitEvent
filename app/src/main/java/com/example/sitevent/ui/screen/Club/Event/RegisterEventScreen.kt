package com.example.sitevent.ui.screen.Club.Event

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import kotlin.math.roundToInt


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
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
    // Load event
    LaunchedEffect(eventId) {
        eventViewModel.getEvent(categoryId, clubId, eventId)
    }
    val event by eventViewModel.event.collectAsStateWithLifecycle()
    val actionStatus by ticketViewModel.actionStatus.collectAsStateWithLifecycle(null)

    // UI state…
    var participation by remember {
        mutableStateOf(
            when (event?.mode) {
                EventMode.SINGLE -> "Individual"
                EventMode.GROUP -> "Group"
                else -> "Individual"
            }
        )
    }
    var teamName by remember { mutableStateOf("") }
    var memberCount by remember { mutableStateOf(event?.minTeamSize ?: 1) }
    val memberIds = remember { mutableStateListOf<String>() }
    LaunchedEffect(event?.minTeamSize) {
        memberIds.clear()
        repeat(event?.minTeamSize ?: 1) { memberIds.add("") }
    }
    val extraAnswers = remember { mutableStateMapOf<String, String>() }
        .apply { event?.additionalInfoAskFromUser?.forEach { putIfAbsent(it.key, "") } }

    Surface(
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                text = event?.title ?: "",
                style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
            )
            Spacer(Modifier.height(8.dp))
            event?.startTime?.let {
                Text(
                    text = "${it.toDateString()} • ${it.toTimeString()}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // 1) Participation Mode Section
        SectionCard(title = "How will you participate?") {
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

        // 2) Team Details (if Group)
        if (participation == "Group") {
            SectionCard(title = "Team Details") {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                val minSize = event?.minTeamSize?.toFloat() ?: 1f
                val maxSize = event?.maxTeamSize?.toFloat() ?: minSize
                Text("Members: $memberCount", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = memberCount.toFloat(),
                    onValueChange = {
                        memberCount = it.roundToInt().coerceIn(minSize.toInt(), maxSize.toInt())
                        if (memberCount > memberIds.size) repeat(memberCount - memberIds.size) {
                            memberIds.add(
                                ""
                            )
                        }
                        else repeat(memberIds.size - memberCount) { memberIds.removeLast() }
                    },
                    valueRange = minSize..maxSize,
                    steps = (maxSize - minSize).toInt().coerceAtLeast(0)
                )
                Spacer(Modifier.height(12.dp))
                memberIds.forEachIndexed { idx, value ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { memberIds[idx] = it },
                        label = { Text("Member ${idx + 1} ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // 3) Additional Questions
        if (!event?.additionalInfoAskFromUser.isNullOrEmpty()) {
            SectionCard(title = "Extra Info") {
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

        // 4) Submit Button
        Button(
            onClick = {
                val team = Team(
                    teamName = teamName,
                    teamMemberIds = if (participation == "Group") memberIds.filter { it.isNotBlank() } else emptyList(),
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
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.filledTonalButtonColors()
        ) {
            Text("Confirm Registration", style = MaterialTheme.typography.labelLarge)
        }

        // 5) Loading/Error Handling
        when (actionStatus) {
            is Resource.Loading -> {
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is Resource.Error -> {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = (actionStatus as Resource.Error).exception.localizedMessage
                        ?: "Something went wrong",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            is Resource.Success -> {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }

            else -> {}
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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

private fun Timestamp.toDateString(): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(this.toDate())

private fun Timestamp.toTimeString(): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(this.toDate())