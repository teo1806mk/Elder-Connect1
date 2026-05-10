package com.example.elder_connect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elder_connect.data.firestore.Announcement
import com.example.elder_connect.data.firestore.AnnouncementCategory
import com.example.elder_connect.ui.viewmodels.NewsOperationStatus
import com.example.elder_connect.ui.viewmodels.NewsViewModel
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date

/**
 * AddAnnouncementScreen - φόρμα προσθήκης νέας ανακοίνωσης στη Firestore.
 *
 * Σκοπός: αποδεικνύει το CRUD της απομακρυσμένης βάσης (απαίτηση εκφώνησης).
 * Σε ένα πραγματικό app αυτή η οθόνη θα ήταν διαθέσιμη μόνο σε admins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnnouncementScreen(
    viewModel: NewsViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Θεσσαλονίκη") }
    var selectedCategory by remember { mutableStateOf(AnnouncementCategory.EXCURSION) }
    var daysAhead by remember { mutableStateOf(7) }  // εκδήλωση σε X μέρες

    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationStatus) {
        when (val status = operationStatus) {
            is NewsOperationStatus.Success -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.clearStatus()
                onSaved()
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Νέα Ανακοίνωση",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Πίσω"
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Title
            Text("Τίτλος", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("π.χ. Εκδρομή στη Σαντορίνη") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Description
            Text("Περιγραφή", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Αναλυτικές πληροφορίες...") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(Modifier.height(16.dp))

            // City
            Text("Πόλη", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Category selector
            Text("Κατηγορία", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Column {
                AnnouncementCategory.values()
                    .filter { it != AnnouncementCategory.ALL }
                    .forEach { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                            Text(
                                text = "${category.emoji} ${category.label}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
            }

            Spacer(Modifier.height(16.dp))

            // Days ahead slider
            Text(
                text = "Εκδήλωση σε $daysAhead ημέρες",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Slider(
                value = daysAhead.toFloat(),
                onValueChange = { daysAhead = it.toInt() },
                valueRange = 1f..60f,
                steps = 58
            )

            Spacer(Modifier.height(32.dp))

            // Save button
            Button(
                onClick = {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, daysAhead)
                    val announcement = Announcement(
                        title = title,
                        description = description,
                        category = selectedCategory.key,
                        city = city,
                        eventDate = Timestamp(cal.time),
                        createdAt = Timestamp.now()
                    )
                    viewModel.addAnnouncement(announcement)
                },
                enabled = title.isNotBlank() && description.isNotBlank() &&
                        operationStatus !is NewsOperationStatus.Saving,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                if (operationStatus is NewsOperationStatus.Saving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "ΔΗΜΟΣΙΕΥΣΗ",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}