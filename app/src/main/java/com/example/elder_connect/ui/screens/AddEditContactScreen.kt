package com.example.elder_connect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import com.example.elder_connect.ui.theme.ElderRed
import com.example.elder_connect.ui.viewmodels.AddEditContactViewModel
import com.example.elder_connect.ui.viewmodels.FormSaveStatus

/**
 * AddEditContactScreen.
 *
 * Δύο modes:
 * - Add mode (contactId = null): δημιουργία νέας επαφής
 * - Edit mode (contactId != null): επεξεργασία/διαγραφή υπάρχουσας
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    viewModel: AddEditContactViewModel,
    contactId: Long? = null,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updateImageUri(uri.toString())
            }
        }
    )

    // Φόρτωση αν είμαστε σε edit mode
    LaunchedEffect(contactId) {
        if (contactId != null && contactId > 0) {
            viewModel.loadContact(contactId)
        }
    }

    // Reaction στο save status
    LaunchedEffect(saveStatus) {
        when (val status = saveStatus) {
            is FormSaveStatus.Success -> {
                snackbarHostState.showSnackbar(
                    if (state.isEditMode) "Αποθηκεύτηκε!" else "Προστέθηκε!"
                )
                viewModel.clearStatus()
                onSaved()
            }
            is FormSaveStatus.Error -> {
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
                        text = if (state.isEditMode) "Επεξεργασία Επαφής" else "Νέα Επαφή",
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
                actions = {
                    if (state.isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Διαγραφή",
                                tint = ElderRed
                            )
                        }
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
            // ----------- Φωτογραφία -----------
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.imageUri != null) {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = "Contact Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Προσθήκη",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ----------- Όνομα -----------
            Text(
                text = "Όνομα",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                placeholder = { Text("π.χ. Ελένη") },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(16.dp))

            // ----------- Σχέση -----------
            Text(
                text = "Σχέση",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.relationship,
                onValueChange = viewModel::updateRelationship,
                placeholder = { Text("π.χ. Κόρη, Γιός, Εγγονή") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(16.dp))

            // ----------- Τηλέφωνο -----------
            Text(
                text = "Τηλέφωνο",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = viewModel::updatePhone,
                placeholder = { Text("+30 6971234567") },
                isError = state.phoneError != null,
                supportingText = state.phoneError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(24.dp))

            // ----------- Toggles -----------
            ToggleRow(
                title = "Αγαπημένη επαφή",
                description = "Εμφανίζεται στην αρχική",
                checked = state.isFavorite,
                onCheckedChange = { viewModel.toggleFavorite() }
            )

            Spacer(Modifier.height(8.dp))

            ToggleRow(
                title = "Υποστηρίζει βιντεοκλήση",
                description = "Εμφανίζεται πράσινη ένδειξη",
                checked = state.supportsVideoCall,
                onCheckedChange = { viewModel.toggleVideoCall() }
            )

            Spacer(Modifier.height(40.dp))

            // ----------- Save button -----------
            Button(
                onClick = viewModel::save,
                enabled = saveStatus !is FormSaveStatus.Saving,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                if (saveStatus is FormSaveStatus.Saving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (state.isEditMode) "ΑΠΟΘΗΚΕΥΣΕ" else "ΠΡΟΣΘΗΚΗ",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ----------- Delete confirmation dialog -----------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Διαγραφή επαφής;",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Είσαι σίγουρος ότι θες να διαγράψεις την επαφή \"${state.name}\";",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    }
                ) {
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
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}