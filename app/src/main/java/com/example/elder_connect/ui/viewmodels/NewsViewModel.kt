package com.example.elder_connect.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.entities.User
import com.example.elder_connect.data.firestore.Announcement
import com.example.elder_connect.data.firestore.AnnouncementCategory
import com.example.elder_connect.data.firestore.Interest
import com.example.elder_connect.data.repository.ElderConnectRepository
import com.example.elder_connect.notifications.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(
    application: Application,
    private val repository: ElderConnectRepository
) : AndroidViewModel(application) {

    private val _selectedCategory = MutableStateFlow(AnnouncementCategory.ALL)
    val selectedCategory: StateFlow<AnnouncementCategory> = _selectedCategory.asStateFlow()

    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val announcements: StateFlow<List<Announcement>> = _selectedCategory
        .flatMapLatest { category ->
            if (category == AnnouncementCategory.ALL) {
                repository.getAllAnnouncements()
            } else {
                repository.getAnnouncementsByCategory(category.key)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _operationStatus = MutableStateFlow<NewsOperationStatus>(NewsOperationStatus.Idle)
    val operationStatus: StateFlow<NewsOperationStatus> = _operationStatus.asStateFlow()

    fun selectCategory(category: AnnouncementCategory) {
        _selectedCategory.value = category
    }

    fun getUserInterest(announcementId: String): kotlinx.coroutines.flow.Flow<Interest?> {
        val userId = currentUser.value?.id ?: return flowOf(null)
        return repository.getUserInterest(announcementId, userId)
    }

    fun setInterest(announcementId: String, isInterested: Boolean) {
        val user = currentUser.value ?: return

        // Βρες την ανακοίνωση για το notification message
        val announcementTitle = announcements.value
            .firstOrNull { it.id == announcementId }
            ?.title ?: "Ανακοίνωση"

        viewModelScope.launch {
            try {
                _operationStatus.value = NewsOperationStatus.Saving
                val result = repository.setInterest(
                    announcementId = announcementId,
                    userId = user.id,
                    userName = user.fullName,
                    isInterested = isInterested
                )
                result.fold(
                    onSuccess = {
                        // Notification επιβεβαίωσης
                        NotificationHelper.showInterestRegisteredNotification(
                            context = getApplication(),
                            announcementTitle = announcementTitle,
                            isInterested = isInterested
                        )
                        _operationStatus.value = NewsOperationStatus.Success("Καταχωρήθηκε!")
                    },
                    onFailure = {
                        _operationStatus.value = NewsOperationStatus.Error(it.message ?: "Σφάλμα")
                    }
                )
            } catch (e: Exception) {
                _operationStatus.value = NewsOperationStatus.Error(e.message ?: "Σφάλμα")
            }
        }
    }

    fun addAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            try {
                _operationStatus.value = NewsOperationStatus.Saving
                val result = repository.addAnnouncement(announcement)
                result.fold(
                    onSuccess = { newId ->
                        // Notification για νέα ανακοίνωση
                        NotificationHelper.showNewAnnouncementNotification(
                            context = getApplication(),
                            announcementId = newId,
                            title = announcement.title,
                            description = announcement.description
                        )
                        _operationStatus.value = NewsOperationStatus.Success("Προστέθηκε!")
                    },
                    onFailure = {
                        _operationStatus.value = NewsOperationStatus.Error(it.message ?: "Σφάλμα")
                    }
                )
            } catch (e: Exception) {
                _operationStatus.value = NewsOperationStatus.Error(e.message ?: "Σφάλμα")
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            try {
                _operationStatus.value = NewsOperationStatus.Saving
                val result = repository.deleteAnnouncement(id)
                _operationStatus.value = result.fold(
                    onSuccess = { NewsOperationStatus.Success("Διαγράφηκε!") },
                    onFailure = { NewsOperationStatus.Error(it.message ?: "Σφάλμα") }
                )
            } catch (e: Exception) {
                _operationStatus.value = NewsOperationStatus.Error(e.message ?: "Σφάλμα")
            }
        }
    }

    fun clearStatus() {
        _operationStatus.value = NewsOperationStatus.Idle
    }
}

sealed class NewsOperationStatus {
    data object Idle : NewsOperationStatus()
    data object Saving : NewsOperationStatus()
    data class Success(val message: String) : NewsOperationStatus()
    data class Error(val message: String) : NewsOperationStatus()
}