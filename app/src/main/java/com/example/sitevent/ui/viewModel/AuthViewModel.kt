package com.example.sitevent.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.User
import com.example.sitevent.data.repository.Inteface.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _loginState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val loginState: StateFlow<Resource<Unit>> = _loginState

    private val _signUpState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val signUpState: StateFlow<Resource<Unit>> = _signUpState

    private val _sendEmailLinkState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val sendEmailLinkState: StateFlow<Resource<Unit>> = _sendEmailLinkState

    private val _verifyState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val verifyState: StateFlow<Resource<Unit>> = _verifyState

    private val _signOutState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val signoutState: StateFlow<Resource<Unit>> = _signOutState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            _loginState.value = authRepository.login(email, password)
        }
    }

    fun signInStatus(): Boolean {
        return authRepository.signInStatus()
    }

    fun getCurrentUser() = authRepository.getCurrentUser()

    fun signUp(user: User, password: String) {
        viewModelScope.launch {
            _signUpState.value = Resource.Loading
            _signUpState.value = authRepository.signUp(user, password)
        }
    }

    fun resendVerification() {
        viewModelScope.launch {
            _sendEmailLinkState.value = Resource.Loading
            _sendEmailLinkState.value = authRepository.sendVerificationEmail()
        }
    }

    fun checkEmailVerified() {
        viewModelScope.launch {
            _verifyState.value = Resource.Loading
            _verifyState.value = authRepository.refreshUserEmailVerification()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _signOutState.value = Resource.Loading
            val result = authRepository.signOut()
            _signOutState.value = result
        }
    }

    fun clearSignOutState() {
        _signOutState.value = Resource.Idle
    }
}