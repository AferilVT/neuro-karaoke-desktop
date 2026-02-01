package com.soul.neurokaraoke.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soul.neurokaraoke.data.model.User
import com.soul.neurokaraoke.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isLoggedIn = user != null
                )
            }
        }
    }

    /**
     * Get intent to open Discord authorization in browser
     */
    fun getSignInIntent(): Intent {
        val authUrl = authRepository.getAuthorizationUrl()
        return Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
    }

    /**
     * Handle OAuth callback (when redirected back from Discord)
     */
    fun handleAuthCallback(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.handleAuthCallback(code).fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoggedIn = true,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Authentication failed"
                    )
                }
            )
        }
    }

    /**
     * Handle user data received from deep link (after website OAuth callback)
     */
    fun handleUserFromDeepLink(user: User) {
        authRepository.saveUser(user)
        _uiState.value = _uiState.value.copy(
            user = user,
            isLoggedIn = true,
            isLoading = false
        )
    }

    /**
     * Log out the current user
     */
    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
