package com.example.elder_connect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elder_connect.data.firestore.Announcement
import com.example.elder_connect.data.firestore.AnnouncementCategory
import com.example.elder_connect.ui.theme.ElderGreen
import com.example.elder_connect.ui.theme.ElderRed
import com.example.elder_connect.ui.viewmodels.NewsOperationStatus
import com.example.elder_connect.ui.viewmodels.NewsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * NewsScreen — Ανακοινώσεις δήμου από Firestore.
 *
 * Λειτουργίες:
 * - Φιλτράρισμα κατηγορίας (chips στο πάνω μέρος)
 * - Λίστα ανακοινώσεων (τίτλος, περιγραφή, ημερομηνία, πόλη)
 * - Κουμπιά "Ενδιαφέρομαι" / "Δεν Ενδιαφέρομαι"
 * - FAB για προσθήκη νέας ανακοίνωσης (admin feature)
 */
@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onAddAnnouncement: () -> Unit
) {
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationStatus) {
        when (val status = operationStatus) {
            is NewsOperationStatus.Success -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.clearStatus()
            }
            is NewsOperationStatus.Error -> {
                snackbarHostState.showSnackbar("Σφάλμα: ${status.message}")
                viewModel.clearStatus()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAnnouncement,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Νέα ανακοίνωση",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Νέα από τον Δήμο",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ----------- Category filter chips -----------
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AnnouncementCategory.values()) { category ->
                    CategoryChip(
                        category = category,
                        selected = category == selectedCategory,
                        onClick = { viewModel.selectCategory(category) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ----------- Announcements list -----------
            if (announcements.isEmpty()) {
                EmptyAnnouncementsView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = announcements, key = { it.id }) { announcement ->
                        AnnouncementCard(
                            announcement = announcement,
                            onInterested = {
                                viewModel.setInterest(announcement.id, true)
                            },
                            onNotInterested = {
                                viewModel.setInterest(announcement.id, false)
                            },
                            onDelete = {
                                viewModel.deleteAnnouncement(announcement.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: AnnouncementCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = "${category.emoji} ${category.label}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AnnouncementCard(
    announcement: Announcement,
    onInterested: () -> Unit,
    onNotInterested: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                val cat = AnnouncementCategory.values()
                    .firstOrNull { it.key == announcement.category }
                    ?: AnnouncementCategory.GENERAL
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${cat.emoji} ${cat.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Διαγραφή",
                        tint = ElderRed.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            // Description
            Text(
                text = announcement.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Event date (αν υπάρχει)
            announcement.eventDate?.let { ts ->
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatDate(ts.toDate().time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // City (αν υπάρχει)
            if (announcement.city.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = announcement.city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onInterested,
                    colors = ButtonDefaults.buttonColors(containerColor = ElderGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Ενδιαφέρομαι",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onNotInterested,
                    colors = ButtonDefaults.buttonColors(containerColor = ElderRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Δεν Ενδιαφέρομαι",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Διαγραφή ανακοίνωσης;") },
            text = { Text("Είσαι σίγουρος ότι θες να διαγράψεις την ανακοίνωση \"${announcement.title}\";") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("ΔΙΑΓΡΑΦΗ", color = ElderRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ΑΚΥΡΩΣΗ")
                }
            }
        )
    }
}

@Composable
private fun EmptyAnnouncementsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Δεν υπάρχουν ανακοινώσεις",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Πάτα + για να προσθέσεις",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("EEEE, d MMMM yyyy 'στις' HH:mm", Locale("el", "GR"))
    return formatter.format(java.util.Date(timestamp))
}