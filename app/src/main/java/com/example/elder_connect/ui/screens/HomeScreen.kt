package com.example.elder_connect.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.ui.theme.ElderGreen
import com.example.elder_connect.ui.theme.ElderRed
import com.example.elder_connect.ui.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HomeScreen με ΔΙΑΦΟΡΕΤΙΚΟ LAYOUT ανά orientation.
 *
 * Portrait: vertical scrollable, 2 αγαπημένες επαφές, συντομεύσεις κάτω
 * Landscape: split-screen δύο στηλών — αριστερά μέχρι 4 αγαπημένες,
 *            δεξιά ένα γρήγορο dashboard με συντομεύσεις
 *
 * Καλύπτει την απαίτηση:
 * "Η εφαρμογή θα υποστηρίζει αυτόματη αλλαγή εμφάνισης όταν αλλάζει
 *  κατεύθυνση η συσκευή. Η αλλαγή θα πιστοποιείται με παρουσίαση
 *  διαφορετικού περιεχομένου ανά κατεύθυνση."
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onContactClick: (Contact) -> Unit,
    onContactsListClick: () -> Unit,
    onSosClick: () -> Unit = {},
    onEmergencyPhonesClick: () -> Unit = {},
    onNewsClick: () -> Unit = {},
    onMoodClick: () -> Unit = {}
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteContacts.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        HomeScreenLandscape(
            userName = user?.fullName ?: "",
            greeting = viewModel.getGreeting(),
            favorites = favorites,
            onContactClick = onContactClick,
            onContactsListClick = onContactsListClick,
            onSosClick = onSosClick,
            onEmergencyPhonesClick = onEmergencyPhonesClick,
            onNewsClick = onNewsClick,
            onMoodClick = onMoodClick
        )
    } else {
        HomeScreenPortrait(
            userName = user?.fullName ?: "",
            greeting = viewModel.getGreeting(),
            favorites = favorites,
            onContactClick = onContactClick,
            onContactsListClick = onContactsListClick,
            onSosClick = onSosClick,
            onEmergencyPhonesClick = onEmergencyPhonesClick
        )
    }
}

/* ================================================================
                          PORTRAIT LAYOUT
   ================================================================ */
@Composable
private fun HomeScreenPortrait(
    userName: String,
    greeting: String,
    favorites: List<Contact>,
    onContactClick: (Contact) -> Unit,
    onContactsListClick: () -> Unit,
    onSosClick: () -> Unit,
    onEmergencyPhonesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        GreetingCard(greeting = greeting, userName = userName, compact = false)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Τι θα ήθελες να κάνεις σήμερα",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        // 2 αγαπημένες σε Row
        if (favorites.isEmpty()) {
            EmptyFavoritesCard(onAddClick = onContactsListClick)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                favorites.take(2).forEach { contact ->
                    FavoriteContactCard(
                        contact = contact,
                        onClick = { onContactClick(contact) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (favorites.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Συντομεύσεις",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(12.dp))

        ShortcutCard(
            icon = Icons.Filled.Warning,
            iconColor = ElderRed,
            title = "Κουμπί άμεσης ανάγκης",
            subtitle = "Αποστολή SOS",
            onClick = onSosClick,
            isEmergency = true
        )
        Spacer(Modifier.height(12.dp))

        ShortcutCard(
            icon = Icons.Filled.Videocam,
            iconColor = MaterialTheme.colorScheme.primary,
            title = "Επαφές",
            subtitle = "Όλες οι επαφές σου",
            onClick = onContactsListClick
        )
        Spacer(Modifier.height(12.dp))

        ShortcutCard(
            icon = Icons.Filled.LocalHospital,
            iconColor = MaterialTheme.colorScheme.primary,
            title = "Τηλέφωνα Έκτακτης ανάγκης",
            subtitle = "Γρήγορη πρόσβαση",
            onClick = onEmergencyPhonesClick
        )

        Spacer(Modifier.height(16.dp))
    }
}

/* ================================================================
                         LANDSCAPE LAYOUT
   ================================================================ */
@Composable
private fun HomeScreenLandscape(
    userName: String,
    greeting: String,
    favorites: List<Contact>,
    onContactClick: (Contact) -> Unit,
    onContactsListClick: () -> Unit,
    onSosClick: () -> Unit,
    onEmergencyPhonesClick: () -> Unit,
    onNewsClick: () -> Unit,
    onMoodClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Compact greeting πάνω-πάνω (μία γραμμή)
        GreetingCard(greeting = greeting, userName = userName, compact = true)

        Spacer(Modifier.height(16.dp))

        // Δύο στήλες: Αριστερά αγαπημένες | Δεξιά Quick Actions Dashboard
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ----------- Αριστερή στήλη: Αγαπημένες (μέχρι 4) -----------
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Κάλεσε αγαπημένο",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(12.dp))

                if (favorites.isEmpty()) {
                    EmptyFavoritesCard(onAddClick = onContactsListClick)
                } else {
                    // Grid με 2 αγαπημένες ανά σειρά (μέχρι 4)
                    val displayed = favorites.take(4)
                    displayed.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { contact ->
                                FavoriteContactCardCompact(
                                    contact = contact,
                                    onClick = { onContactClick(contact) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // ----------- Δεξιά στήλη: Quick Actions Dashboard -----------
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Γρήγορες Ενέργειες",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(12.dp))

                // SOS button - ΜΕΓΑΛΟ στο landscape
                LargeSosButton(onClick = onSosClick)

                Spacer(Modifier.height(12.dp))

                // 2x2 grid από συντομεύσεις (μόνο σε landscape)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionTile(
                        icon = Icons.Filled.Videocam,
                        label = "Επαφές",
                        onClick = onContactsListClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Filled.LocalHospital,
                        label = "Έκτακτη Ανάγκη",
                        onClick = onEmergencyPhonesClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionTile(
                        icon = Icons.Filled.Newspaper,
                        label = "Νέα Δήμου",
                        onClick = onNewsClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        icon = Icons.Filled.SentimentSatisfied,
                        label = "Πώς Νιώθεις",
                        onClick = onMoodClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/* ================================================================
                         REUSABLE COMPONENTS
   ================================================================ */

@Composable
private fun GreetingCard(greeting: String, userName: String, compact: Boolean) {
    val today = remember_today()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 48.dp else 64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).ifEmpty { "?" },
                    style = if (compact) MaterialTheme.typography.titleLarge
                    else MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(if (compact) 12.dp else 16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$greeting $userName",
                    style = if (compact) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = today,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun FavoriteContactCard(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ElderGreen)
                    .border(3.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Videocam, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(contact.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(contact.relationship, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = ElderGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ΚΑΛΕΣΕ", color = Color.White, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
        }
    }
}

/** Compact card για landscape (πιο μικρή) */
@Composable
private fun FavoriteContactCardCompact(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.name.take(1), style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(ElderGreen)
                        .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Videocam, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text(contact.relationship, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("ΚΑΛΕΣΕ", style = MaterialTheme.typography.labelMedium,
                    color = ElderGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LargeSosButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = ElderRed.copy(alpha = 0.1f),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ElderRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Warning, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Άμεση Ανάγκη", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = ElderRed)
                Text("Πάτα για αποστολή SOS", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyFavoritesCard(onAddClick: () -> Unit) {
    Surface(
        onClick = onAddClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Δεν έχεις ακόμα αγαπημένες επαφές",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Πάτα εδώ για να προσθέσεις",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ShortcutCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isEmergency: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isEmergency) ElderRed.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, title, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isEmergency) ElderRed else MaterialTheme.colorScheme.onSurface)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun remember_today(): String {
    return remember {
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("el", "GR"))
        formatter.format(Date())
    }
}