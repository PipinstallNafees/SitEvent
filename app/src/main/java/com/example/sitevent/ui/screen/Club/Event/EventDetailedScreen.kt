// EventDetailScreen.kt
package com.example.sitevent.ui.screen.Club.Event

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.AdditionalInfo
import com.example.sitevent.data.model.Event
import com.example.sitevent.data.model.EventOrganizer
import com.example.sitevent.data.model.EventResult
import com.example.sitevent.data.model.RegistrationStatus
import com.example.sitevent.data.model.Team
import com.example.sitevent.data.model.Ticket
import com.example.sitevent.ui.Navigation.NavigationItem
import com.example.sitevent.ui.theme.SitEventTheme
import com.example.sitevent.ui.viewModel.EventViewModel
import com.example.sitevent.ui.viewModel.TicketViewModel
import com.example.sitevent.ui.viewModel.UserViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    categoryId: String,
    clubId: String,
    eventId: String,
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel(),
) {
    val userRes by userViewModel.observeUser.collectAsStateWithLifecycle()
    val userId = (userRes as? Resource.Success)?.data?.userId.orEmpty()
    val team by ticketViewModel.team.collectAsStateWithLifecycle()
    val teams by ticketViewModel.teamsForEvent.collectAsStateWithLifecycle()

    LaunchedEffect(categoryId, clubId, eventId) {
        eventViewModel.getEvent(categoryId, clubId, eventId)
        ticketViewModel.getTicketsForEvent(categoryId, clubId, eventId)
        ticketViewModel.getTeamsForEvent(categoryId, clubId, eventId)
    }

    val event by eventViewModel.event.collectAsStateWithLifecycle()
    val tickets by ticketViewModel.ticketsForEvent.collectAsStateWithLifecycle()
    val actionStatus by ticketViewModel.actionStatus.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(NavigationItem.EventSetting.createRoute(categoryId,clubId,eventId))
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Event Setting")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (event == null) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HeaderSection(
                    posterUrl = event!!.posterUrl.orEmpty(),
                    title = event!!.title,
                    startTime = event!!.startTime!!,
                    isOnline = event!!.isOnline,
                    venue = event!!.venue
                )
                AboutSection(
                    about = event!!.about!!,
                    description = event!!.description
                )
                RegistrationSection(
                    categoryId,
                    clubId,
                    event!!,
                    userId,
                    navController,
                    ticketViewModel,
                    onRegister = {
                        navController.navigate(NavigationItem.EventRegistration.createRoute(categoryId,clubId,eventId))
                    }
                )
                PerksSection(event!!.perks)
                AdditionalInfoSection(event!!.additionalInfo)
                OrganizersSection(event!!.organizers)
//                SponsorsSection(event!!)
                TeamsSection(teams){
                    navController.navigate(NavigationItem.AllTeamsForEvent.createRoute(
                        categoryId,
                        clubId,
                        eventId
                    ))
                }
                TicketsSection(tickets,navController)
//                SubEventsSection(event!!, navController, categoryId, clubId, eventId)
//                ResultsSection(event!!)
            }
        }
    }
}


@Composable
fun HeaderSection(
    posterUrl: String,
    title: String,
    startTime: Timestamp,
    isOnline: Boolean,
    venue: String,
) {
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    val date = startTime.toDate()
    val formattedTime = timeFormatter.format(date)
    val formattedDate = dateFormatter.format(date)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                        ),
                        startY = 100f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )

                Spacer(Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Time",
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Venue",
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isOnline) "Online" else venue,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}


@Composable
fun AboutSection(
    about: String,
    description: String,
) {
    Column {
        Text("About", style = MaterialTheme.typography.titleMedium)
        Text(about.orEmpty(), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text("Description", style = MaterialTheme.typography.titleMedium)
        Text(description, style = MaterialTheme.typography.bodyMedium)
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationSection(
    categoryId: String,
    clubId: String,
    event: Event,
    userId: String,
    navController: NavController,
    ticketViewModel: TicketViewModel,
    onRegister: () -> Unit = {}
) {
    // Formatters
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Ensure registration end time safe
    val endDate = remember(event) { event.registrationEndTime?.toDate() }
    if (endDate == null) return
    val formattedDate = remember(endDate) { dateFormatter.format(endDate) }
    val formattedTime = remember(endDate) { timeFormatter.format(endDate) }

    val registered = remember(event.participantsIds, userId) {
        event.participantsIds.contains(userId)
    }

    // Trigger ticket load only when registered
    LaunchedEffect(registered) {
        if (registered) {
            ticketViewModel.getTicketForUserInEvent(event.eventId, userId)
        }
    }

    // Collect ticket state
    val ticket by ticketViewModel.ticket.collectAsStateWithLifecycle()

    // Derive team info from ticket whenever it updates
    val teamId = remember(ticket) { ticket?.teamId.orEmpty() }
    val teamMemberIds = remember(ticket) { ticket?.participantIds ?: emptyList() }

    // Countdown state
    var remainingMillis by remember { mutableStateOf(endDate.time - System.currentTimeMillis()) }
    LaunchedEffect(endDate) {
        while (remainingMillis > 0) {
            remainingMillis = endDate.time - System.currentTimeMillis()
            delay(1000L)
        }
    }

    // Compute days, hours, minutes, seconds
    val days = (remainingMillis / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    val hours = (remainingMillis / (1000 * 60 * 60) % 24).coerceAtLeast(0)
    val minutes = (remainingMillis / (1000 * 60) % 60).coerceAtLeast(0)
    val seconds = (remainingMillis / 1000 % 60).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Registration Ends $formattedDate at $formattedTime",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Countdown display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "%02dd %02dh %02dm %02ds",
                        days, hours, minutes, seconds
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (registered) {
                // Log once when ticket arrives
                LaunchedEffect(ticket) {
                    Log.d("RegistrationSection", "Loaded Ticket: $ticket")
                    Log.d("RegistrationSection", "teamId: $teamId, members: $teamMemberIds")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            ticket?.ticketId?.let { tid ->
                                navController.navigate(NavigationItem.UserTicketDetail.createRoute(userId,tid))
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Show Ticket", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = {
                            ticketViewModel.cancelTicket(
                                categoryId,
                                clubId,
                                event.eventId,
                                ticket!!.ticketId,
                                teamId,
                                userId,
                                teamMemberIds
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Icon(Icons.Default.Cancel, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel Ticket", style = MaterialTheme.typography.labelLarge)
                    }
                }
            } else {
                Button(
                    onClick = onRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Default.EventAvailable, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Register Now", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerksSection(perks: List<String>) {
    Column {
        Text("Perks", style = MaterialTheme.typography.titleMedium)

        // Animate between two accent colors
        val transition = rememberInfiniteTransition()
        val chipColor by transition.animateColor(
            initialValue = MaterialTheme.colorScheme.primaryContainer,
            targetValue = MaterialTheme.colorScheme.secondaryContainer,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            perks.forEach { perk ->
                AssistChip(
                    onClick = { /* maybe filter or highlight */ },
                    label = {
                        Text(
                            text = perk,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = chipColor,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                )
            }
        }
    }


}

@Composable
fun AdditionalInfoSection(additionalInfo: List<AdditionalInfo>) {


    // Animated accent color for items
    val transition = rememberInfiniteTransition()
    val accentColor by transition.animateColor(
        initialValue = MaterialTheme.colorScheme.primaryContainer,
        targetValue = MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column {
        Text("Additional Info", style = MaterialTheme.typography.titleMedium)

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            items(additionalInfo.toList()) { (key, value) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = accentColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }


}

@Composable
fun OrganizersSection(
    eventOrganizers: List<EventOrganizer>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        SectionTitle("Organizers")
        Spacer(modifier = Modifier.height(12.dp))

        // If ≤4, center them; else scroll horizontally

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(eventOrganizers) { org ->
                OrganizerChip(org)
            }
        }

    }
}

@Composable
private fun OrganizerChip(org: EventOrganizer) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { /* maybe navigate/profile */ }
            .padding(8.dp)
    ) {
        // Avatar circle with drop shadow
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.size(60.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()        // ← make the box fill the card
            ) {
                Text(
                    text = org.userId.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = org.userId,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = org.role.name.lowercase().replace('_', ' ').split(' ')
                .joinToString(" ") { it.replaceFirstChar(Char::titlecase) },
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun TeamsSection(
    teams: List<Team>,
    onClick: () -> Unit
) {
    val uniqueTeams = teams.groupBy { it.teamId }.values.map { it.first() }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Teams (${uniqueTeams.size} Registered)",style = MaterialTheme.typography.titleMedium)
            Text(
                text = "See all",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {onClick()}
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

        }

        Spacer(Modifier.height(12.dp))

        teams.forEach { team ->
            Log.d("TeamsSection", "Team: ${team.teamId}")
            TeamCard(team,{})
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCard(
    team: Team,
    onClick: () -> Unit = {}
) {
    // Generate a gradient accent based on teamId hash
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )
    val accent = colors[abs(team.teamId.hashCode()) % colors.size]
    val gradient = Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.6f)))

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Accent stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .background(gradient)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Team: ${team.teamName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = team.eventName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = team.teamLeaderId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Members count avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.2f))
            ) {
                Text(
                    text = team.teamMemberIds.size.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = accent
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewTeamsSection() {
//    // Sample teams for preview
//    val sampleTeams = listOf(
//        Team(
//            teamId        = "team1",
//            teamName      = "Alpha Squad",
//            teamMemberIds = listOf("u1", "u2", "u3"),
//            teamLeaderId  = "u1"
//        ),
//        Team(
//            teamId        = "team2",
//            teamName      = "Beta Crew",
//            teamMemberIds = listOf("u4", "u5"),
//            teamLeaderId  = "u4"
//        ),
//        Team(
//            teamId        = "team3",
//            teamName      = "",
//            teamMemberIds = listOf("u6"),
//            teamLeaderId  = "u6"
//        )
//    )
//
//    // Wrap in your app theme (replace MyAppTheme with your actual theme)
//    SitEventTheme {
//        Surface(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            TeamsSection(teams = sampleTeams)
//        }
//    }
//}
//

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TicketsSection(
    tickets: List<Ticket>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("Tickets (${tickets.size})")

        tickets.forEach { ticket ->
            TicketCard(ticket)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TicketCard(ticket: Ticket) {
    // pick an accent color per ticket
    val accentColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )
    val accent = accentColors[abs(ticket.ticketId.hashCode()) % accentColors.size]

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // colored stripe
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            Spacer(Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = ticket.userId,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "ID: ${ticket.ticketId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                // formatted date
                val issuedDate = remember(ticket.issuedAt) {
                    Instant
                        .ofEpochMilli(ticket.issuedAt)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                }

                Text(
                    text = "Issued: $issuedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // QR icon if present
            ticket.qrCodeUrl?.let {
                IconButton(onClick = { /* show QR */ }) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        tint = accent
                    )
                }
            }

            // status badge
            val statusColor = when (ticket.status) {
                RegistrationStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                RegistrationStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                RegistrationStatus.CLAIMED -> MaterialTheme.colorScheme.tertiaryContainer
                RegistrationStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .background(statusColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = ticket.status.name.lowercase().replace('_', ' ').capitalizeWords(),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// extension to title-case words
private fun String.capitalizeWords() = split(' ')
    .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }


//@Composable
//fun SubEventsSection(
//    event: Event,
//    navController: NavController,
//    categoryId: String,
//    clubId: String,
//    eventId: String
//) {
//    SectionTitle("Sub-Events")
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        event.subEvents.forEach { sub ->
//            Text(sub.title, style = MaterialTheme.typography.bodyMedium)
//        }
//    }
//}

@Composable
fun ResultsSection(
    eventResults: List<EventResult>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Event Results")

        eventResults.forEach { result ->
            ResultCard(result)
        }
    }
}

@Composable
fun ResultCard(
    result: EventResult,
    modifier: Modifier = Modifier,
) {
    // deterministic accent based on event
    val accentColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )
    val accent = accentColors[abs(result.eventId.hashCode()) % accentColors.size]

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Event: ${result.eventId}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                result.subEventId?.let {
                    Text(
                        text = "| Sub: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Winner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = accent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = result.winnerTeamId.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Column {
                    Text(
                        text = "Winner",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = accent
                    )
                    Text(
                        text = result.winnerTeamId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Runner-ups
            if (result.runnerUpTeamIds.isNotEmpty()) {
                Text(
                    text = "Runner-Ups",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(result.runnerUpTeamIds) { map ->
                        val (teamId, rank) = map.entries.first()
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(
                                    color = accent.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 6.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "$rank. $teamId",
                                style = MaterialTheme.typography.bodySmall,
                                color = accent
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Scores
            if (result.teamScores.isNotEmpty()) {
                Text(
                    text = "Scores",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    result.teamScores.forEach { (teamId, score) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = teamId,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = score.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = accent
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Download button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { /* handle download */ }) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Download",
                        tint = accent
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Download",
                        color = accent
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewResultsSection() {
    // Sample EventResults for preview
    val sampleResults = listOf(
        EventResult(
            eventId = "eventA",
            subEventId = "sub1",
            categoryId = "catX",
            winnerTeamId = "Alpha",
            runnerUpTeamIds = listOf(
                mapOf("Beta" to 2),
                mapOf("Gamma" to 3)
            ),
            teamScores = mapOf(
                "Alpha" to 95,
                "Beta" to 88,
                "Gamma" to 76
            ),
            recordedAt = System.currentTimeMillis()
        ),
        EventResult(
            eventId = "eventB",
            categoryId = "catY",
            winnerTeamId = "Delta",
            // no runner‐ups for this one
            runnerUpTeamIds = emptyList(),
            teamScores = mapOf(
                "Delta" to 100,
                "Epsilon" to 92
            ),
            recordedAt = System.currentTimeMillis()
        )
    )

    // Assuming your Event class looks like this:
    // data class Event(val eventId: String, val eventResults: List<EventResult>)


    SitEventTheme {  // replace with your actual theme
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ResultsSection(sampleResults)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
}
