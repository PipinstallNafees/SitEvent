package com.example.sitevent.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.AppRole
import com.example.sitevent.data.model.Event
import com.example.sitevent.ui.Navigation.NavigationItem
import com.example.sitevent.ui.viewModel.AuthViewModel
import com.example.sitevent.ui.viewModel.ClubCategoryViewModel
import com.example.sitevent.ui.viewModel.ClubViewModel
import com.example.sitevent.ui.viewModel.EventViewModel
import com.example.sitevent.ui.viewModel.UserViewModel
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private fun Timestamp.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(this.toDate().toInstant(), ZoneId.systemDefault())

/**
 * Display model for event organizer initials.
 */
data class OrganizerDisplay(val initials: String, val id: String)

/**
 * Model for quick action items
 */
data class QuickAction(val label: String, val icon: ImageVector, val gradient: Brush)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    categoryVm: ClubCategoryViewModel = hiltViewModel(),
    clubVm: ClubViewModel = hiltViewModel(),
    eventVm: EventViewModel = hiltViewModel(),
    userVm: UserViewModel = hiltViewModel(),
) {
    // State collectors
    val categories by categoryVm.categories.collectAsStateWithLifecycle()
    val clubs by clubVm.allClubs.collectAsStateWithLifecycle()
    val eventsRes by eventVm.allEvents.collectAsState()
    val userRes by userVm.observeUser.collectAsState()


    val allEvents by eventVm.allEvents.collectAsState()

    val displayName = when (userRes) {
        is Resource.Loading -> "Loading…"
        is Resource.Error   -> "Error"
        is Resource.Success -> (userRes as Resource.Success).data?.name.orEmpty()
        else                -> "…"
    }
    val appRole = (userRes as? Resource.Success)?.data?.appRole ?: AppRole.MEMBER

    val now = LocalDateTime.now(ZoneId.systemDefault())
    val ongoing = allEvents.filter { e ->
        e.startTime != null && e.endTime != null &&
                !now.isBefore(e.startTime.toLocalDateTime()) &&
                !now.isAfter(e.endTime.toLocalDateTime())
    }
    val upcoming = allEvents.filter { e ->
        e.startTime != null && now.isBefore(e.startTime.toLocalDateTime())
    }.sortedBy { it.startTime!!.toLocalDateTime() }
    val spotlight = allEvents
        .sortedByDescending { it.ticketIds.size }
        .take(10)
    val organizers = allEvents
        .flatMap { it.organizers }
        .distinctBy { it.userId }
        .map { OrganizerDisplay(it.userId.take(2).uppercase(), it.userId) }

    BottomBarScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "👋 $displayName",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Clubs Category
            item {
                SectionHeader(
                    title = "Clubs Category",
                    actionLabel = "See all",
                    onActionClick = { navController.navigate(NavigationItem.AllCategory.route) }
                )
                if (categories.isEmpty() && appRole == AppRole.ADMIN) {
                    CreateCategoryCard { navController.navigate(NavigationItem.CreateCategory.route) }
                } else {
                    HomeScreenCategoryGrid(categories, appRole, navController)
                }
            }

            // My Clubs
            item {
                SectionHeader(
                    title = "My Clubs",
                    actionLabel = "See all",
                    onActionClick = { navController.navigate(NavigationItem.AllClubs.route) }
                )
                if (clubs.isEmpty()) {
                    Text(
                        "No clubs found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(clubs) { club ->
                            ClubCard(
                                club = club,
                                modifier = Modifier.width(screenWidth)
                            ) {
                                navController.navigate(
                                    NavigationItem.ClubDetail.createRoute(
                                        club.categoryId, club.clubId
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Ongoing Events
            item {
                SectionHeader(title = "Ongoing Events")
                if (ongoing.isEmpty()) {
                    Text(
                        "No ongoing events",
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ongoing.forEach { event ->
                        EventCard(
                            event = event,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // Upcoming Events
            item {
                SectionHeader(title = "Upcoming Events")
                if (upcoming.isEmpty()) {
                    Text(
                        "No upcoming events",
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    upcoming.forEach { event ->
                        UpcomingEventCard(event = event)
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // Spotlight
            item {
                SectionHeader(title = "Spotlight")
                if (spotlight.isEmpty()) {
                    Text(
                        "No spotlight events",
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(spotlight) { event ->
                            SpotlightCard(event = event)
                        }
                    }
                }
            }

            // Organizers
            item {
                SectionHeader(
                    title = "Organizers",
                    actionLabel = "See all",
                    onActionClick = { /* nav to organizers */ }
                )
                if (organizers.isEmpty()) {
                    Text(
                        "No organizers",
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(organizers) { org ->
                            OrganizerCard(initials = org.initials, id = org.id)
                        }
                    }
                }
            }

            // Quick Actions
            item {
                SectionHeader(title = "Quick Actions")
                val actions = listOf(
                    QuickAction(
                        "Create", Icons.Default.Add, Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    ),
                    QuickAction(
                        "Tickets", Icons.AutoMirrored.Filled.AirplaneTicket, Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                    QuickAction(
                        "Chat", Icons.Default.Email, Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.tertiaryContainer,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                    QuickAction(
                        "Map", Icons.Default.Map, Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.errorContainer,
                                MaterialTheme.colorScheme.error
                            )
                        )
                    )
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    actions.forEach { action ->
                        QuickActionItem(action = action) { /* TODO */ }
                    }
                }
            }
        }
    }
}


@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "★ $title",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onActionClick() }
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

// --- Quick Action Item ---
@Composable
fun QuickActionItem(
    action: QuickAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .width(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(action.gradient)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.label,
            modifier = Modifier.size(38.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = action.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- Event Card for Ongoing Events ---
@Composable
fun EventCard(event: Event, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- Upcoming Event Card ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingEventCard(event: Event) {
    val start = event.startTime?.toLocalDateTime()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    start?.format(DateTimeFormatter.ofPattern("MMM dd")) ?: "",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    start?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { /* TODO: remind - implement notification scheduling */ },
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text("Remind")
                }
            }
        }
    }
}

// --- Spotlight Card ---
@Composable
fun SpotlightCard(event: Event) {
    val brush = Brush.sweepGradient(
        listOf(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.tertiary
        )
    )
    Card(
        modifier = Modifier.size(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(brush)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                event.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- Organizer Card ---
@Composable
fun OrganizerCard(initials: String, id: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .clickable { /* TODO: navigate to organizer detail */ }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initials,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            id,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

