package com.example.sitevent.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Club
import com.example.sitevent.data.model.ClubUser
import com.example.sitevent.data.repository.Inteface.ClubRepository
import com.example.sitevent.data.repository.Inteface.UserRepository
import com.example.sitevent.domain.CreateClubUseCase
import com.example.sitevent.domain.DeleteClubUseCase
import com.example.sitevent.domain.JoinClubUseCase
import com.example.sitevent.domain.LeaveClubUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ClubViewModel @Inject constructor(
    private val repo: ClubRepository,
    private val userRepo: UserRepository,
    private val createClubUseCase: CreateClubUseCase,  // renamed
    private val deleteClubUseCase: DeleteClubUseCase,
    private val joinClubUseCase: JoinClubUseCase,
    private val leaveClubUseCase: LeaveClubUseCase
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
        _operationStatus.emit(createClubUseCase(club))
    }



    fun updateClub(club: Club) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(repo.updateClub(club))
    }

    fun deleteClub(categoryId: String, clubId: String) = viewModelScope.launch {
        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(deleteClubUseCase(categoryId, clubId))
    }

    // Join/leave club
    private val _joinState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val joinState: StateFlow<Resource<Unit>> = _joinState.asStateFlow()

    fun joinClub(categoryId: String, clubId: String, userId: String) = viewModelScope.launch {

        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(joinClubUseCase(categoryId, clubId, userId))
    }

    fun leaveClub(categoryId: String, clubId: String, userId: String) = viewModelScope.launch {

        _operationStatus.emit(Resource.Loading)
        _operationStatus.emit(leaveClubUseCase(categoryId, clubId, userId))
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