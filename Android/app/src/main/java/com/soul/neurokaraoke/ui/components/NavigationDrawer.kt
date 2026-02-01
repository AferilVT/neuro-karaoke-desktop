package com.soul.neurokaraoke.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soul.neurokaraoke.R
import com.soul.neurokaraoke.navigation.Screen
import com.soul.neurokaraoke.ui.theme.DuetColor
import com.soul.neurokaraoke.ui.theme.EvilColor
import com.soul.neurokaraoke.ui.theme.LocalThemeMode
import com.soul.neurokaraoke.ui.theme.LocalThemeToggle
import com.soul.neurokaraoke.ui.theme.NeuroColor
import com.soul.neurokaraoke.ui.theme.ThemeMode

@Composable
fun NavigationDrawerContent(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    onClose: () -> Unit,
    isLoggedIn: Boolean = false,
    userName: String? = null,
    userAvatarUrl: String? = null,
    onSignInClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeMode = LocalThemeMode.current
    val setThemeMode = LocalThemeToggle.current

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.mipmap.neuro_foreground),
                contentDescription = "NeuroKaraoke Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Neuro Karaoke",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main navigation items
        Screen.mainNavItems.forEach { screen ->
            NavigationDrawerItem(
                screen = screen,
                isSelected = currentRoute == screen.route,
                onClick = {
                    onNavigate(screen)
                    onClose()
                }
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outline
        )

        // Library section
        Text(
            text = "YOUR LIBRARY",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Screen.libraryItems.forEach { screen ->
            NavigationDrawerItem(
                screen = screen,
                isSelected = false,
                comingSoon = true,
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outline
        )

        // Theme selector
        Text(
            text = "THEME",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            ThemeChip(
                text = "Auto",
                isSelected = themeMode == ThemeMode.AUTO,
                color = MaterialTheme.colorScheme.primary,
                onClick = { setThemeMode(ThemeMode.AUTO) },
                modifier = Modifier.weight(1f)
            )
            ThemeChip(
                text = "Neuro",
                isSelected = themeMode == ThemeMode.NEURO,
                color = NeuroColor,
                onClick = { setThemeMode(ThemeMode.NEURO) },
                modifier = Modifier.weight(1f)
            )
            ThemeChip(
                text = "Evil",
                isSelected = themeMode == ThemeMode.EVIL,
                color = EvilColor,
                onClick = { setThemeMode(ThemeMode.EVIL) },
                modifier = Modifier.weight(1f)
            )
            ThemeChip(
                text = "Duet",
                isSelected = themeMode == ThemeMode.DUET,
                color = DuetColor,
                onClick = { setThemeMode(ThemeMode.DUET) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // User profile (Coming Soon)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Coming soon!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NavigationDrawerItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit,
    comingSoon: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val backgroundColor = if (isSelected) {
        primaryColor.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (comingSoon) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    } else if (isSelected) {
        primaryColor
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(if (!comingSoon) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        screen.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            text = screen.title,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
        if (comingSoon) {
            Text(
                text = "Coming soon",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeChip(
    text: String,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
