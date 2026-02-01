package com.soul.neurokaraoke

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.soul.neurokaraoke.data.model.User
import com.soul.neurokaraoke.ui.MainScreen
import com.soul.neurokaraoke.ui.theme.NeuroKaraokeTheme
import com.soul.neurokaraoke.viewmodel.AuthViewModel
import com.soul.neurokaraoke.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle deep link if app was launched from it
        handleDeepLink(intent)

        setContent {
            val playerState by playerViewModel.uiState.collectAsState()
            val currentSinger = playerState.currentSong?.singer?.name

            NeuroKaraokeTheme(currentSinger = currentSinger) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        playerViewModel = playerViewModel,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link when app is already running (singleTask)
        handleDeepLink(intent)
    }

    /**
     * Handle incoming deep links for Discord auth callback
     *
     * Expected URL formats:
     * - neurokaraoke://auth?id=xxx&username=xxx&discriminator=xxx&avatar=xxx&token=xxx
     * - https://neurokaraoke.com/app-auth?id=xxx&username=xxx&discriminator=xxx&avatar=xxx&token=xxx
     */
    private fun handleDeepLink(intent: Intent?) {
        val uri: Uri? = intent?.data

        if (uri == null) return

        Log.d("MainActivity", "Received deep link: $uri")

        // Check if this is an auth callback
        val isAuthCallback = when {
            uri.scheme == "neurokaraoke" && uri.host == "auth" -> true
            uri.scheme == "https" && uri.host == "neurokaraoke.com" && uri.path == "/app-auth" -> true
            else -> false
        }

        if (!isAuthCallback) return

        // Parse user data from URL parameters
        val userId = uri.getQueryParameter("id")
        val username = uri.getQueryParameter("username")
        val discriminator = uri.getQueryParameter("discriminator") ?: "0"
        val avatar = uri.getQueryParameter("avatar")
        val accessToken = uri.getQueryParameter("token")

        Log.d("MainActivity", "Auth callback: user=$username, id=$userId")

        if (userId != null && username != null) {
            val user = User(
                id = userId,
                username = username,
                discriminator = discriminator,
                avatar = avatar,
                accessToken = accessToken
            )
            authViewModel.handleUserFromDeepLink(user)
        }
    }
}
