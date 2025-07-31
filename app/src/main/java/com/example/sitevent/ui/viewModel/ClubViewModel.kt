package com.example.sitevent.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Club
import com.example.sitevent.data.model.ClubUser
import com.example.sitevent.data.repository.Inteface.ClubRepository
import com.example.sitevent.data.repository.Inteface.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ClubViewModel @Inject constructor(
    private val repo: ClubRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    val allClubs: StateFlow<List<Club>> = repo.getAllClubs()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private val _clubByCategory = MutableStateFlow<List<Club>>(emptyList())
    val clubByCategory: StateFlow<List<Club>> = _clubByCategory.asStateFlow()

    fun getClubByCategory(categoryId: String) = viewModelScope.launch {
        repo.getAllClubsByCategory(categoryId).collect { list ->
            _clubByCategory.value = list
        }
    }

    private val _club = MutableStateFlow<Club?>(null)
    val club: StateFlow<Club?> = _club.asStateFlow()

    fun getClub(categoryId: String, clubId: String) = viewModelScope.launch {
        repo.getClub(categoryId, clubId).collect { club ->
            _club.value = club
        }
    }


    // Save/update/delete operations
    private val _operationStatus = MutableSharedFlow<Resource<Unit>>(replay = 0)
    val operationStatus: SharedFlow<Resource<Unit>> = _operationStatus.asSharedFlow()

    fun createClub(club: Club) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(repo.createClub(club))
    }

    fun updateClub(club: Club) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(repo.updateClub(club))
    }

    fun deleteClub(categoryId: String, clubId: String) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(repo.deleteClub(categoryId, clubId))
    }

    // Join/leave club
    private val _joinState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val joinState: StateFlow<Resource<Unit>> = _joinState.asStateFlow()

    fun joinClub(categoryId: String, clubId: String, userId: String) = viewModelScope.launch {
        _joinState.value = Resource.Loading
        val clubRes = repo.joinClub(categoryId, clubId, userId)
        val userRes = if (clubRes is Resource.Success) {
            userRepo.joinClubForUser(userId, categoryId, clubId)
        } else {
            Resource.Error(Exception("Join failed"))
        }

        _joinState.value = if (clubRes is Resource.Success && userRes is Resource.Success) {
            Resource.Success(Unit).also { getClub(categoryId, clubId) }
        } else {
            val err = (clubRes as? Resource.Error)?.exception
                ?: (userRes as? Resource.Error)?.exception
                ?: Exception("Unknown error")
            Resource.Error(err) // Pass the actual Exception
        }
    }

    fun leaveClub(categoryId: String, clubId: String, userId: String) = viewModelScope.launch {
        _joinState.value = Resource.Loading
        val clubRes = repo.leaveClub(userId, categoryId, clubId)
        val userRes = if (clubRes is Resource.Success) {
            userRepo.leaveClubForUser(userId, categoryId, clubId)
        } else {
            Resource.Error(Exception("Leave failed"))
        }

        _joinState.value = if (clubRes is Resource.Success && userRes is Resource.Success) {
            Resource.Success(Unit).also { getClub(categoryId, clubId) }
        } else {
            val err = (clubRes as? Resource.Error)?.exception
                ?: (userRes as? Resource.Error)?.exception
                ?: Exception("Unknown error")
            Resource.Error(err) // Pass the actual Exception
        }
    }

    private val _changeVisibility = MutableStateFlow<Triple<String, String, Boolean>?>(null)
    val changeVisibilityState: StateFlow<Resource<Unit>> = _changeVisibility
        .filterNotNull()
        .flatMapLatest { (cat, club, visibility) ->
            flow {
                emit(Resource.Loading)
                emit(repo.changeClubVisibility(cat, club, visibility))
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, Resource.Idle)


    fun changeVisibility(categoryId: String, clubId: String, visibility: Boolean) {
        _changeVisibility.value = Triple(categoryId, clubId, visibility)
    }

    //club member Collection

    private val _clubAllMembers = MutableStateFlow<List<ClubUser>>(emptyList())
    val clubAllMembers: StateFlow<List<ClubUser>> = _clubAllMembers.asStateFlow()

    fun getAllClubMembers(categoryId: String, clubId: String) = viewModelScope.launch {
        repo.getAllClubMembers(categoryId, clubId).collect { list ->
            _clubAllMembers.value = list
        }
    }

    private val _clubMember = MutableStateFlow<ClubUser?>(null)
    val clubMember: StateFlow<ClubUser?> = _clubMember.asStateFlow()

    fun getClubMember(categoryId: String, clubId: String, userId: String) = viewModelScope.launch {
        repo.getClubMember(categoryId, clubId, userId).collect { club ->
            _clubMember.value = club
        }
    }

    private val _clubMemberOperationStatus = MutableSharedFlow<Resource<Unit>>(replay = 0)
    val clubMemberOperationStatus: SharedFlow<Resource<Unit>> = _clubMemberOperationStatus.asSharedFlow()

    fun clubMemberChangeRole(categoryId: String, clubId: String, userId: String, role: String) = viewModelScope.launch {
        _clubMemberOperationStatus.emit(Resource.Loading)
        _clubMemberOperationStatus.emit(repo.changeClubUserRole(categoryId, clubId, userId, role))
    }


}