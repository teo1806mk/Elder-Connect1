package com.example.elder_connect.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elder_connect.ui.viewmodels.MoodViewModel
import com.example.elder_connect.ui.viewmodels.SaveStatus
import com.example.elder_connect.data.entities.MoodType
import kotlinx.coroutines.delay

/**
 * MoodCheckScreen - "Πώς νιώθεις τώρα;"
 *
 * Ο χρήστης επιλέγει ένα από τα 5 emojis και πατάει "ΑΠΟΘΗΚΕΥΣΕ".
 * Η επιλογή του αποθηκεύεται στη Room DB ως MoodEntry.
 */
@Composable
fun MoodCheckScreen(
    viewModel: MoodViewModel,
    userId: Long,
    onSaved: () -> Unit = {}
) {
    val selectedMood by viewModel.selectedMood.collectAsStateWithLifecycle()
    val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()

    // Όταν επιτυχίας, εμφανίζουμε snackbar και επιστρέφουμε
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(saveStatus) {
        when (val status = saveStatus) {
            is SaveStatus.Success -> {
                snackbarHostState.showSnackbar("Αποθηκεύτηκε!")
                delay(800)
                viewModel.clearStatus()
                onSaved()
            }
            is SaveStatus.Error -> {
                snackbarHostState.showSnackbar("Σφάλμα: ${status.message}")
                viewModel.clearStatus()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Πώς Νιώθεις τώρα;",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Επέλεξε το εικονίδιο που σε εκφράζει.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // Πρώτη σειρά: Τέλεια - Καλά
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MoodOption(
                    mood = MoodType.TELEIA,
                    isSelected = selectedMood == MoodType.TELEIA,
                    onClick = { viewModel.selectMood(MoodType.TELEIA) },
                    modifier = Modifier.weight(1f)
                )
                MoodOption(
                    mood = MoodType.KALA,
                    isSelected = selectedMood == MoodType.KALA,
                    onClick = { viewModel.selectMood(MoodType.KALA) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Δεύτερη σειρά: Μέτρια - Όχι καλά
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MoodOption(
                    mood = MoodType.METRIA,
                    isSelected = selectedMood == MoodType.METRIA,
                    onClick = { viewModel.selectMood(MoodType.METRIA) },
                    modifier = Modifier.weight(1f)
                )
                MoodOption(
                    mood = MoodType.OXI_KALA,
                    isSelected = selectedMood == MoodType.OXI_KALA,
                    onClick = { viewModel.selectMood(MoodType.OXI_KALA) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Τρίτη σειρά (κεντραρισμένη): Άσχημα
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MoodOption(
                    mood = MoodType.ASXIMA,
                    isSelected = selectedMood == MoodType.ASXIMA,
                    onClick = { viewModel.selectMood(MoodType.ASXIMA) },
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
            }

            Spacer(Modifier.weight(1f))

            // Save button
            Button(
                onClick = { viewModel.saveMood(userId) },
                enabled = selectedMood != null && saveStatus !is SaveStatus.Saving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                if (saveStatus is SaveStatus.Saving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "ΑΠΟΘΗΚΕΥΣΕ",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onSaved) {
                Text(
                    text = "Θα απαντήσω μετά",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MoodOption(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else Color.Transparent
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = modifier
            .border(3.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = mood.emoji, fontSize = 48.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = mood.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}