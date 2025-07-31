// EventDetailScreen.kt
package com.example.sitevent.ui.screen.Club.Event

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Event
import com.example.sitevent.data.model.Ticket
import com.example.sitevent.ui.Navigation.NavigationItem
import com.example.sitevent.ui.viewModel.EventViewModel
import com.example.sitevent.ui.viewModel.TicketViewModel
import com.example.sitevent.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    categoryId: String,
    clubId: String,
    eventId: String,
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel()
) {
    // Observe user
    val userRes by userViewModel.observeUser.collectAsStateWithLifecycle()
    val userId = (userRes as? Resource.Success)?.data?.userId.orEmpty()

    // Load data
    LaunchedEffect(categoryId, clubId, eventId) {
        eventViewModel.getEvent(categoryId, clubId, eventId)
        ticketViewModel.getTicketsForEvent(categoryId, clubId, eventId)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (event == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderSection(event!!)
                    ExpandableCard("About") { AboutContent(event!!) }
                    ExpandableCard("Registration") { RegistrationContent(event!! ) }
                    ExpandableCard("Perks") { PerksContent(event!! ) }
                    ExpandableCard("Additional Info") { AdditionalInfoContent(event!!) }
                    ExpandableCard("Organizers") { OrganizersContent(event!!) }
                    ExpandableCard("Sponsors") { SponsorsContent(event!!) }
//                    ExpandableCard("Teams (${tickets.size} registered)") { TeamsContent(event!!, tickets, navController) }
//                    ExpandableCard("Tickets") { TicketsContent(tickets) }
//                    ExpandableCard("Registration Questions") { QuestionsContent(event!!) }
//                    ExpandableCard("Sub-Events") { SubEventsContent(event!!, navController, categoryId, clubId, eventId) }
//                    ExpandableCard("Event Results") { ResultsContent(event!!) }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(event: Event) {
    event.posterUrl?.let { url ->
        Image(
            painter = rememberAsyncImagePainter(url),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
    Text(event.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(
        text = "${event.startTime?.toDate()} – ${event.endTime?.toDate()}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Place, contentDescription = null)
        Text(if (event.isOnline) "Online" else event.venue, Modifier.padding(start = 4.dp))
    }
}

@Composable
fun ExpandableCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable fun AboutContent(event: Event) {
    Text(event.about.orEmpty(), style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(4.dp))
    Text(event.description, style = MaterialTheme.typography.bodySmall)
}

@Composable fun RegistrationContent(event: Event) {
    Text("Registration: ${event.registrationStartTime} – ${event.registrationEndTime}", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Place, contentDescription = null)
        Text(event.venue, Modifier.padding(start = 4.dp))
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Register") }
        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Share") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerksContent(event: Event) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        event.perks.forEach { perk ->
            AssistChip(
                onClick = { /* Optional click action */ },
                label = { Text(text = perk) }
            )
        }
    }
}

@Composable fun AdditionalInfoContent(event: Event) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        event.additionalInfoDuringRegistration.forEach { (k,v) ->
            Text("• $k: $v", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable fun OrganizersContent(event: Event) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(event.organizers) { org ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(shape = CircleShape, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(org.userId.take(2).uppercase())
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(org.role.name, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable fun SponsorsContent(event: Event) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(event.sponsors) { url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

//@Composable fun TeamsContent(event: Event, tickets: List<Ticket>, navController: NavController) {
//    tickets.groupBy { it.teamId }.forEach { (teamId, teamTickets) ->
//        Card(modifier = Modifier.fillMaxWidth()) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { /* open team detail */ }
//                    .padding(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(teamId, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
//                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
//            }
//        }
//        Spacer(Modifier.height(8.dp))
//    }
//}

//@Composable fun TicketsContent(tickets: List<Ticket>) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        tickets.forEach { ticket ->
//            Card(modifier = Modifier.fillMaxWidth()) {
//                Row(
//                    modifier = Modifier.padding(12.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(ticket.type)
//                        Text(ticket.ticketId, style = MaterialTheme.typography.bodySmall)
//                    }
//                    Text(ticket.status.name, color = when(ticket.status) {
//                        Resource.Success -> MaterialTheme.colorScheme.primary
//                        Resource.Loading -> MaterialTheme.colorScheme.secondary
//                        is Resource.Error -> MaterialTheme.colorScheme.error
//                    })
//                }
//            }
//        }
//    }
//}

//@Composable fun QuestionsContent(event: Event) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        event.registrationQuestions.forEach { (q,a) ->
//            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp)) {
//                Text(q, fontWeight = FontWeight.Medium)
//                Text(a, style = MaterialTheme.typography.bodySmall)
//            }
//        }
//    }
//}

//@Composable fun SubEventsContent(
//    event: Event,
//    navController: NavController,
//    categoryId: String,
//    clubId: String,
//    eventId: String
//) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        event.subEvents.forEach { sub ->
//            Card(modifier = Modifier.fillMaxWidth().clickable {
//                navController.navigate("/sub/$categoryId/$clubId/$eventId/${sub.id}")
//            }) {
//                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
//                    Column(Modifier.weight(1f)) {
//                        Text(sub.title)
//                        Text(sub.time.toString(), style = MaterialTheme.typography.bodySmall)
//                    }
//                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
//                }
//            }
//        }
//    }
//}

//@Composable fun ResultsContent(event: Event) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        event.results.forEach { file ->
//            Card(modifier = Modifier.fillMaxWidth()) {
//                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
//                    Text(file.name)
//                    TextButton(onClick = { /* download */ }) { Text("Download") }
//                }
//            }
//        }
//    }
//}
