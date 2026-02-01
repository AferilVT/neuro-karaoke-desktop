package com.soul.neurokaraoke.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.soul.neurokaraoke.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        loadSavedUser()
    }

    private fun loadSavedUser() {
        val userId = prefs.getString(KEY_USER_ID, null)
        val username = prefs.getString(KEY_USERNAME, null)
        val discriminator = prefs.getString(KEY_DISCRIMINATOR, "0")
        val avatar = prefs.getString(KEY_AVATAR, null)
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)

        if (userId != null && username != null) {
            _currentUser.value = User(
                id = userId,
                username = username,
                discriminator = discriminator ?: "0",
                avatar = avatar,
                accessToken = accessToken
            )
            _isLoggedIn.value = true
        }
    }

    /**
     * Get the Discord OAuth2 authorization URL
     */
    fun getAuthorizationUrl(): String {
        return DISCORD_AUTH_URL
    }

    /**
     * Exchange authorization code for access token and fetch user info
     * This would typically be handled by your backend server
     */
    suspend fun handleAuthCallback(code: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // In a real app, you would:
            // 1. Send the code to your backend
            // 2. Backend exchanges code for token with Discord
            // 3. Backend returns user info

            // For now, we'll simulate a successful login
            // The actual implementation would depend on your backend API

            // This is a placeholder - you'll need to implement the actual OAuth flow
            // through your neurokaraoke.com backend
            Result.failure(Exception("OAuth callback needs backend implementation"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save user after successful authentication
     */
    fun saveUser(user: User) {
        prefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_DISCRIMINATOR, user.discriminator)
            putString(KEY_AVATAR, user.avatar)
            putString(KEY_ACCESS_TOKEN, user.accessToken)
            apply()
        }
        _currentUser.value = user
        _isLoggedIn.value = true
    }

    /**
     * Log out the current user
     */
    fun logout() {
        prefs.edit().clear().apply()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean = _isLoggedIn.value

    companion object {
        private const val PREFS_NAME = "neurokaraoke_auth"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_DISCRIMINATOR = "discriminator"
        private const val KEY_AVATAR = "avatar"
        private const val KEY_ACCESS_TOKEN = "access_token"

        // Discord OAuth URL - Coming soon
        private const val DISCORD_AUTH_URL = ""
    }
}
