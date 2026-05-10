package com.example.elder_connect.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.entities.MoodEntry
import com.example.elder_connect.data.entities.MoodType
import com.example.elder_connect.data.repository.ElderConnectRepository
import com.example.elder_connect.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State holder για την Mood Check screen.
 *
 * Επεκτείνει AndroidViewModel για να έχει access στο Application context
 * (απαραίτητο για να στέλνει notifications).
 */
class MoodViewModel(
    application: Application,
    private val repository: ElderConnectRepository
) : AndroidViewModel(application) {

    private val _selectedMood = MutableStateFlow<MoodType?>(null)
    val selectedMood: StateFlow<MoodType?> = _selectedMood.asStateFlow()

    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    fun selectMood(mood: MoodType) {
        _selectedMood.value = mood
    }

    fun saveMood(userId: Long) {
        val mood = _selectedMood.value ?: return

        viewModelScope.launch {
            try {
                _saveStatus.value = SaveStatus.Saving
                repository.insertMoodEntry(
                    MoodEntry(
                        userId = userId,
                        mood = mood
                    )
                )

                // Στέλνουμε notification επιβεβαίωσης
                NotificationHelper.showMoodSavedNotification(
                    context = getApplication(),
                    moodLabel = mood.label,
                    moodEmoji = mood.emoji
                )

                _saveStatus.value = SaveStatus.Success
                _selectedMood.value = null
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Σφάλμα")
            }
        }
    }

    fun clearStatus() {
        _saveStatus.value = SaveStatus.Idle
    }
}

sealed class SaveStatus {
    data object Idle : SaveStatus()
    data object Saving : SaveStatus()
    data object Success : SaveStatus()
    data class Error(val message: String) : SaveStatus()
}