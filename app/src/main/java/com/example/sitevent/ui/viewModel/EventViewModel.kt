package com.example.sitevent.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Event
import com.example.sitevent.data.repository.Inteface.EventRepository
import com.example.sitevent.domain.CreateEventUseCase
import com.example.sitevent.domain.DeleteEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventViewModel @Inject constructor(
    private val repo: EventRepository,
    private val createEventUseCase: CreateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    // ——————————————————————————————————————————————————————————————
    // Real-time all events
    val allEvents: StateFlow<List<Event>> = repo.getAllEvents()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private val _eventsByClubId = MutableStateFlow<List<Event>>(emptyList())
    val eventsByClubId: StateFlow<List<Event>> = _eventsByClubId.asStateFlow()

    fun getEventsByClubId(categoryId: String, clubId: String) = viewModelScope.launch {
        repo.getEventsByClubId(categoryId, clubId).collect { list ->
            _eventsByClubId.value = list
        }
    }

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()


    fun getEvent(categoryId: String, clubId: String, eventId: String) = viewModelScope.launch {
        repo.getEvent(categoryId, clubId, eventId).collect { event ->
            _event.value = event
        }
    }



    private val _operationStatus = MutableSharedFlow<Resource<Unit>>(replay = 0)
    val operationStatus: SharedFlow<Resource<Unit>> = _operationStatus.asSharedFlow()

    fun createEvent(event: Event) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        val result = createEventUseCase(event)
        _operationStatus.emit(result)
    }

    fun updateEvent(event: Event) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        val result = repo.updateEvent(event)
        _operationStatus.emit(result)
    }

    fun deleteEvent(categoryId:String, clubId: String, eventId: String) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        val result = repo.deleteEvent(categoryId, clubId, eventId)
        _operationStatus.emit(result)
    }

}
