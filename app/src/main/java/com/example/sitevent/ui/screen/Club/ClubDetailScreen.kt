package com.example.sitevent.ui.screen.Club

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.ClubRole
import com.example.sitevent.data.model.Event
import com.example.sitevent.ui.Navigation.NavigationItem
import com.example.sitevent.ui.viewModel.ClubViewModel
import com.example.sitevent.ui.viewModel.EventViewModel
import com.example.sitevent.ui.viewModel.UserViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(
    navController: NavController,
    categoryId: String,
    clubId: String,
    userId: String,
    clubViewModel: ClubViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    Log.d("ClubDetailScreen", "userId: $userId")

    LaunchedEffect(categoryId, clubId) {
        clubViewModel.getClub(categoryId, clubId)
        clubViewModel.getClubMember(categoryId, clubId, userId)
        eventViewModel.getEventsByClubId(categoryId, clubId)
    }

    val club by clubViewModel.club.collectAsStateWithLifecycle()
    val clubMember by clubViewModel.clubMember.collectAsStateWithLifecycle()
    val eventsByClubId by eventViewModel.eventsByClubId.collectAsState()

    val isAdmin = clubMember?.role == ClubRole.ADMIN
    val isPublic = club?.isPublic ?: false
    val joined = clubMember != null

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Banner with back & settings
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(
                    onClick = { navController.navigate(NavigationItem.ClubSetting.createRoute(categoryId, clubId)) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    // Avatar initials
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = club?.name
                            ?.split(" ")
                            ?.mapNotNull { it.firstOrNull()?.toString() }
                            ?.joinToString("") ?: ""
                        Text(initials, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                    }
                    Spacer(Modifier.height(8.dp))

                    Text(text = club?.name ?: "Loading...", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(text = club?.description ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Group, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Members: ${club?.members?.size ?: 0}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Events: ${club?.events?.size ?: 0}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Join/Leave Button
                    Button(
                        onClick = {
                            if (isPublic) {
                                if (joined) clubViewModel.leaveClub(categoryId, clubId, userId)
                                else clubViewModel.joinClub(categoryId, clubId, userId)
                            } else {
                                Toast.makeText(context, "Club is private, you can't join or leave", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (joined) "Leave Club" else "Join Club")
                    }

                    Spacer(Modifier.height(8.dp))

                    if (isAdmin) {
                        OutlinedButton(
                            onClick = { navController.navigate(NavigationItem.CreateEvent.createRoute(categoryId, clubId)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Event")
                        }
                    }
                }
            }

            // Tabs and event listing
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("Upcoming", "Past")
            TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.background) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }


                    val events = eventsByClubId
                    val now = Instant.now()
                    val filtered = if (selectedTab == 0) {
                        events.filter { it.startTime?.toDate()?.toInstant()?.isAfter(now) == true }
                    } else {
                        events.filter { it.endTime?.toDate()?.toInstant()?.isBefore(now) == true }
                    }
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (filtered.isEmpty()) Text("No ${tabs[selectedTab].lowercase()} events", style = MaterialTheme.typography.bodyMedium)
                        else filtered.forEach { event ->
                            EventCard(event = event) {
                                navController.navigate(NavigationItem.EventDetail.createRoute(categoryId, clubId, event.eventId))
                            }
                        }
                    }


        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventCard(
    event: Event,
    isRegistered: Boolean = false,
    onRegisterClick: () -> Unit = {},
    onDetailsClick: () -> Unit = {}
) {
    val zoned = event.startTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())
    val dateText = zoned?.let {
        val day = it.dayOfMonth
        val month = it.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
        "$day $month"
    } ?: "--"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .width(64.dp)
                    .fillMaxHeight()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(dateText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), fontSize = 16.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.padding(vertical = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(text = zoned?.let { DateTimeFormatter.ofPattern("hh:mm a").format(it) }.orEmpty(), style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(text = event.venue, style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRegisterClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors()) {
                        Text(if (isRegistered) "Registered" else "Register")
                    }
                    OutlinedButton(onClick = onDetailsClick, modifier = Modifier.weight(1f)) {
                        Text("Details")
                    }
                }
            }
        }
    }
}
