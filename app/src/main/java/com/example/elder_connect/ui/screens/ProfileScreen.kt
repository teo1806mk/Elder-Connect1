package com.example.elder_connect.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.elder_connect.data.firestore.UserProfile
import com.example.elder_connect.data.firestore.UserRole
import com.example.elder_connect.ui.viewmodels.ProfileUpdateStatus
import com.example.elder_connect.ui.viewmodels.ProfileViewModel

/**
 * Οθόνη Προφίλ Χρήστη.
 *
 * Εμφανίζει:
 *  - Φωτογραφία προφίλ (με δυνατότητα αλλαγής από gallery)
 *  - Όνομα, email, πόλη, ηλικία, ρόλος
 *
 * Η αλλαγή φωτογραφίας ανεβάζει στο Firebase Storage και ενημερώνει το Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    currentProfile: UserProfile,
    onBack: () -> Unit
) {
    // Αρχικοποίηση ViewModel με το τρέχον profile
    LaunchedEffect(currentProfile.uid) {
        viewModel.loadProfile(currentProfile)
    }

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadProfileImage(it) }
    }

    // Reactions στο status
    LaunchedEffect(status) {
        when (val s = status) {
            is ProfileUpdateStatus.Success -> {
                snackbarHostState.showSnackbar("✅ Η φωτογραφία ενημερώθηκε!")
                viewModel.clearStatus()
            }
            is ProfileUpdateStatus.Error -> {
                snackbarHostState.showSnackbar("❌ ${s.message}")
                viewModel.clearStatus()
            }
            else -> Unit
        }
    }

    val isLoading = status is ProfileUpdateStatus.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Προφίλ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Πίσω")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ─── Avatar με κουμπί αλλαγής ────────────────────────────────────
            Box(contentAlignment = Alignment.BottomEnd) {
                // Εικόνα προφίλ
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(enabled = !isLoading) {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = profile?.photoUrl ?: ""
                    if (photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Φωτογραφία προφίλ",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder: αρχικό ονόματος
                        Text(
                            text = (profile?.fullName ?: "?").take(1).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Loading overlay
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Κουμπί camera (overlay)
                SmallFloatingActionButton(
                    onClick = { if (!isLoading) imagePickerLauncher.launch("image/*") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = "Αλλαγή φωτογραφίας",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Hint
            Text(
                text = "Πάτα για αλλαγή φωτογραφίας",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // ─── Στοιχεία προφίλ ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileInfoCard(
                    label = "Ονοματεπώνυμο",
                    value = profile?.fullName ?: "—"
                )
                ProfileInfoCard(
                    label = "Email",
                    value = profile?.email?.ifBlank { "—" } ?: "—"
                )
                ProfileInfoCard(
                    label = "Πόλη",
                    value = profile?.city?.ifBlank { "—" } ?: "—"
                )
                ProfileInfoCard(
                    label = "Ηλικία",
                    value = if ((profile?.age ?: 0) > 0) "${profile?.age} ετών" else "—"
                )
                ProfileInfoCard(
                    label = "Ρόλος",
                    value = UserRole.fromKey(profile?.role ?: "user").label,
                    isRole = true
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Reusable card για πληροφορίες ────────────────────────────────────────────

@Composable
private fun ProfileInfoCard(
    label: String,
    value: String,
    isRole: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isRole) FontWeight.Bold else FontWeight.Normal,
                    color = if (isRole) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
