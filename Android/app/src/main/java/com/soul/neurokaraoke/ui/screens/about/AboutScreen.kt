package com.soul.neurokaraoke.ui.screens.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.soul.neurokaraoke.ui.theme.EvilColor

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("About", "Credits", "Privacy", "Contact")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> AboutContent()
            1 -> CreditsContent()
            2 -> PrivacyContent()
            3 -> ContactContent()
        }
    }
}

@Composable
private fun AboutContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main About Section
        SectionCard(title = "About Neuro Karaoke") {
            Text(
                text = "This Android app is a mobile client for the Neuro & Evil Karaoke Web Player, " +
                    "a fan-made project created by Soul. It is a community platform dedicated to " +
                    "preserving and enjoying songs covered by Neuro and Evil, along with related fan content.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Warning box
            WarningBox(
                text = "This app is unofficial and is not affiliated with any official Vedal AI entities."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Visit Website Button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://neurokaraoke.com"))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Visit neurokaraoke.com")
            }
        }

        // Features Section
        SectionCard(title = "App Features") {
            val features = listOf(
                "Listen to karaoke songs from the collection",
                "Browse multiple setlists/playlists",
                "Search across all available songs",
                "Background playback with media notifications",
                "Queue management",
                "Neuro and Evil theme modes"
            )
            features.forEach { feature ->
                BulletPoint(text = feature)
            }
        }

        // Disclaimer Section
        SectionCard(title = "Fan-Made Project Disclaimer") {
            Text(
                text = "This is a non-commercial, fan-made project and is not officially affiliated with Neuro or Evil.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All content is provided for personal enjoyment only. Commercial use is prohibited.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CreditsContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Site Creation & Management
        SectionCard(title = "Site Creation & Management") {
            CreditItem(name = "Soul", role = "Creator & Developer")
        }

        // Android App
        SectionCard(title = "Android App") {
            CreditItem(name = "Aferil", role = "App Developer")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "For any bugs please message in discord: @aferil. (dot included :>)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Community Artwork
        SectionCard(title = "Community Artwork") {
            Text(
                text = "All artwork displayed on this site is used with explicit permission from the respective artists.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Artist credits in a grid-like layout
            val artists = listOf(
                "Ame0" to "x.com/HDaHrBUapT57ndh",
                "Copper1ion" to "x.com/Cooper1ion",
                "Donzduck" to "x.com/JackTheFridge",
                "Douraze" to "x.com/DourazeE57303",
                "eenightlamp" to "x.com/eenightlamp",
                "EOcelot" to "x.com/EggpieART",
                "fians" to "x.com/fiansand",
                "Fur31mu" to "x.com/Fur31mu",
                "kan" to "x.com/kan1360",
                "kaze" to "x.com/koishiflandre1",
                "klef" to "x.com/k_lef111256",
                "KyaraShard" to "x.com/KyaraShard",
                "Ladi" to "x.com/LaaaaDi_",
                "LEXingXD" to "x.com/lukuwo2333",
                "Lunacy" to "x.com/lunacy_420",
                "Moneka" to "x.com/Monikaphobia",
                "mr.fish399" to "x.com/MrFish399",
                "P3R" to "x.com/atari_desu",
                "paccha!!" to "x.com/paccha_7",
                "Pchan" to "x.com/pinkpink939",
                "Pius" to "x.com/kapxapius",
                "PTITSA QAQ" to "x.com/PtitsaQAQ",
                "Railyx" to "x.com/raily_x",
                "reyforn" to "x.com/reyforn_",
                "rukooo" to "x.com/ruruk_o2",
                "sena_ink" to "bsky.app/profile/sena.ink",
                "Shinbaru" to "x.com/_shinbaru",
                "tanhuluu" to "x.com/tanhuluu",
                "Taprieiko" to "x.com/Taprieiko",
                "Ultimage" to "x.com/UltimageYujin",
                "UsagiChuuu" to "x.com/UsagiChuuu",
                "White" to "x.com/White45838787",
                "WindSketchy" to "x.com/WindSketchy",
                "LUANMA" to "x.com/luanma96",
                "二十七度火" to "space.bilibili.com/278484760",
                "六狸木芸茴" to "space.bilibili.com/24521463",
                "Marisa Una" to "space.bilibili.com/11156485",
                "shanzha114514" to "@shanzha114514",
                "kasoke" to "x.com/kasoke308",
                "成吉柯德1560" to "space.bilibili.com/295017712",
                "曈月_Gigetsu" to "space.bilibili.com/12390629",
                "liquain" to "x.com/liquain_",
                "lingmiaoi" to "x.com/lingmiaoooi",
                "炭块UwU" to "space.bilibili.com/100840024",
                "炭烤龙尾巴" to "space.bilibili.com/2108308",
                "瑟林SOREN" to "x.com/sorenFTT",
                "零悠" to "x.com/lingyouzzz"
            )

            artists.forEach { (name, link) ->
                ArtistCreditItem(name = name, link = link, context = context)
            }
        }

        // Special Thanks
        SectionCard(title = "Special Thanks") {
            CheckPoint(text = "Waya (@waya13) - Helped obtain artist permissions from B2")
            CheckPoint(text = "Dodo (@dodo8071795) - Helped upload artworks to the website")
        }

        // Banner & Branding
        SectionCard(title = "Banner & Branding") {
            CheckPoint(text = "Website banner art: Created by Fians (x.com/fiansand), edited by Promote")
            CheckPoint(text = "Website logo: Created by Shinbaru (x.com/_shinbaru)")
        }

        // Historical Archive Source
        SectionCard(title = "Historical Archive Source") {
            CheckPoint(text = "All cover files dated November 26, 2025 and earlier are from the Unofficial Neuro Karaoke Archive")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Archive managed by: @ninjakai03, @turuumgl, @nyss_7, @inforno_fire",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Additional Contributions
        SectionCard(title = "Additional Contributions") {
            CheckPoint(text = "Promote - Helped retrieve community votes from Twitch polls")
            CheckPoint(text = "FlashFire8 - Video gallery editing and uploads")
            CheckPoint(text = "Rachinova & CJ - Soundbite creation and editing")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Testing, bug reporting & song metadata:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "flashfire8, promote., emuz, germaninfantry, waya13, kyarashard, ttsuyuki, sena.ink, rachinova, ninjakai03, czadymny, gbritannia, sir_recker, dodo8071795",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // View Full Credits Link
        SectionCard(title = "Full Credits") {
            Text(
                text = "View the complete credits list on the website:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "neurokaraoke.com/about",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://neurokaraoke.com/about"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun CreditItem(name: String, role: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = " - $role",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ArtistCreditItem(name: String, link: String, context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable {
                val url = if (link.startsWith("http") || link.startsWith("@")) {
                    if (link.startsWith("@")) "https://x.com/${link.removePrefix("@")}"
                    else link
                } else {
                    "https://$link"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = " - ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = link,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = TextDecoration.Underline
        )
    }
}

@Composable
private fun PrivacyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Privacy") {
            Text(
                text = "We collect only minimal data required for functionality:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        SectionCard(title = "Guest Users") {
            CheckPoint(text = "No account required to use the app")
            CheckPoint(text = "Playlist data stored locally on device")
        }

        SectionCard(title = "Logged-in Users (Discord)") {
            CheckPoint(text = "Only your Discord user ID and avatar are used")
            CheckPoint(text = "No email addresses or private Discord data are collected")
            CheckPoint(text = "Authentication is used solely for display and access control")
        }

        SectionCard(title = "Data Storage") {
            CheckPoint(text = "Preferences stored locally on your device")
            CheckPoint(text = "No personal data sent to third parties")
            CheckPoint(text = "We do not collect emails, real names, or sensitive data")
        }

        SectionCard(title = "No Liability") {
            Text(
                text = "The app is provided \"as-is\". We are not responsible for data loss, " +
                    "service availability, or third-party claims.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ContactContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "App Feedback & Bug Reports") {
            Text(
                text = "For any issues, bugs, or suggestions regarding this Android app, please contact:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            ContactBox(
                name = "@aferil.",
                platform = "on Discord"
            )
        }

        SectionCard(title = "Website Contact") {
            Text(
                text = "For inquiries about the website, credit corrections, or copyright take-down requests:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            ContactBox(
                name = "@soul1419",
                platform = "on Discord"
            )
        }

        SectionCard(title = "Links") {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://neurokaraoke.com"))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Visit neurokaraoke.com")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://neurokaraoke.com/about"))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Full About Page")
            }
        }
    }
}

// ==========================================
// Reusable Components
// ==========================================

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun WarningBox(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFF9800).copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = Color(0xFFFF9800).copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFFF9800),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFFF9800)
        )
    }
}

@Composable
private fun ContactBox(name: String, platform: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = platform,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "▸ ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CheckPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
