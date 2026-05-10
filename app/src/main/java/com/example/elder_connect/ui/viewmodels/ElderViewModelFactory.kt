package com.example.elder_connect.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.elder_connect.ElderConnectApplication
import com.example.elder_connect.data.repository.ElderConnectRepository

/**
 * Factory για δημιουργία ViewModels με dependencies.
 *
 * Περνάει το Application και το Repository σε όλα τα ViewModels που το χρειάζονται.
 */
class ElderViewModelFactory(
    private val application: Application,
    private val repository: ElderConnectRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // Session Management
            modelClass.isAssignableFrom(SessionViewModel::class.java) -> {
                SessionViewModel(repository.userSyncService) as T
            }

            // Authentication
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                val app = application as ElderConnectApplication
                AuthViewModel(app.authService) as T
            }

            // Home
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }

            // Mood
            modelClass.isAssignableFrom(MoodViewModel::class.java) -> {
                MoodViewModel(application, repository) as T
            }

            // Contacts
            modelClass.isAssignableFrom(ContactsViewModel::class.java) -> {
                ContactsViewModel(repository) as T
            }

            // Add/Edit Contact
            modelClass.isAssignableFrom(AddEditContactViewModel::class.java) -> {
                AddEditContactViewModel(repository) as T
            }

            // Call Options
            modelClass.isAssignableFrom(CallOptionsViewModel::class.java) -> {
                CallOptionsViewModel(repository) as T
            }

            // News
            modelClass.isAssignableFrom(NewsViewModel::class.java) -> {
                NewsViewModel(application, repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}