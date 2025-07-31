package com.example.sitevent.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Ticket
import com.example.sitevent.data.repository.Inteface.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TicketViewModel @Inject constructor(
    private val repo: TicketRepository
) : ViewModel() {

    val allTickets: StateFlow<List<Ticket>> = repo.getAllTickets()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private val _ticketsForEvent = MutableStateFlow<List<Ticket>>(emptyList())
    val ticketsForEvent: StateFlow<List<Ticket>> = _ticketsForEvent.asStateFlow()

    fun getTicketsForEvent(categoryId: String, clubId: String, eventId: String) = viewModelScope.launch {
        repo.getAllTicketsForEvent(categoryId, clubId, eventId).collect { list ->
            _ticketsForEvent.value = list
        }
    }

    private val _ticket = MutableStateFlow<Ticket?>(null)
    val ticket: StateFlow<Ticket?> = _ticket.asStateFlow()

    fun getTicket(categoryId: String, clubId: String, eventId: String, ticketId: String) = viewModelScope.launch {
        repo.getTicket(categoryId, clubId, eventId, ticketId).collect { ticket ->
            _ticket.value = ticket
        }
    }

    private val _userTickets = MutableStateFlow<List<Ticket>>(emptyList())
    val userTickets: StateFlow<List<Ticket>> = _userTickets.asStateFlow()

    fun getUserTickets(userId: String) = viewModelScope.launch {
        repo.getAllTicketsForUser(userId).collect { list ->
            _userTickets.value = list
        }
    }

    private val _singleUserTicket = MutableStateFlow<Ticket?>(null)
    val singleUserTicket: StateFlow<Ticket?> = _singleUserTicket.asStateFlow()

    fun getSingleUserTicket(userId: String, ticketId: String) = viewModelScope.launch {
        repo.getSingleTicketForUser(userId, ticketId).collect { ticket ->
            _singleUserTicket.value = ticket
        }
    }

    // — All single‑ticket operations share this status flow —
    private val _actionStatus = MutableSharedFlow<Resource<Unit>>(replay = 0)
    val actionStatus: SharedFlow<Resource<Unit>> = _actionStatus.asSharedFlow()

    fun issueTicket(ticket: Ticket) = viewModelScope.launch {
        _actionStatus.emit(Resource.Loading)
        val res = repo.issueTicket(ticket)
        _actionStatus.emit(res)
    }

    fun updateTicket(ticket: Ticket) = viewModelScope.launch {
        _actionStatus.emit(Resource.Loading)
        val res = repo.updateTicket(ticket)
        _actionStatus.emit(res)
    }

    fun deleteTicket(ticket: Ticket) = viewModelScope.launch {
        _actionStatus.emit(Resource.Loading)
        val res = repo.deleteTicket(ticket)
        _actionStatus.emit(res)
    }

    fun redeemTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        userId: String
    ) = viewModelScope.launch {
        _actionStatus.emit(Resource.Loading)
        val res = repo.redeemTicket(categoryId, clubId, eventId, ticketId,userId)
        _actionStatus.emit(res)
    }
    fun resetActionStatus() {
        viewModelScope.launch {
            _actionStatus.emit(Resource.Idle)
        }
    }
}
