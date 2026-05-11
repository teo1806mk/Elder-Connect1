package com.example.elder_connect.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.firestore.AuthService
import com.example.elder_connect.data.firestore.StorageService
import com.example.elder_connect.data.firestore.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUpdateStatus {
    data object Idle    : ProfileUpdateStatus()
    data object Loading : ProfileUpdateStatus()
    data object Success : ProfileUpdateStatus()
    data class  Error(val message: String) : ProfileUpdateStatus()
}

/**
 * ViewModel για την οθόνη προφίλ χρήστη.
 *
 * Λειτουργίες:
 *  - Εμφάνιση τρέχοντος profile (fullName, email, role, photoUrl)
 *  - Upload νέας εικόνας προφίλ → Firebase Storage → ενημέρωση Firestore
 */
class ProfileViewModel(
    private val authService: AuthService,
    private val storageService: StorageService
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _status = MutableStateFlow<ProfileUpdateStatus>(ProfileUpdateStatus.Idle)
    val status: StateFlow<ProfileUpdateStatus> = _status.asStateFlow()

    /**
     * Αρχικοποίηση με το profile από το SessionViewModel / AuthViewModel.
     * Καλείται από το ProfileScreen μέσω LaunchedEffect.
     */
    fun loadProfile(profile: UserProfile) {
        _profile.value = profile
    }

    /**
     * Ανεβάζει νέα εικόνα προφίλ.
     *
     * Ροή: URI (gallery) → Firebase Storage → download URL → Firestore update
     */
    fun uploadProfileImage(imageUri: Uri) {
        val profile = _profile.value ?: return

        viewModelScope.launch {
            _status.value = ProfileUpdateStatus.Loading
            try {
                // 1. Upload στο Firebase Storage
                val uploadResult = storageService.uploadProfileImage(profile.uid, imageUri)

                uploadResult.fold(
                    onSuccess = { imageUrl ->
                        // 2. Ενημέρωση Firestore profile με το νέο URL
                        val updatedProfile = profile.copy(photoUrl = imageUrl)
                        authService.updateUserProfile(updatedProfile).fold(
                            onSuccess = {
                                _profile.value = updatedProfile
                                _status.value = ProfileUpdateStatus.Success
                            },
                            onFailure = { e ->
                                _status.value = ProfileUpdateStatus.Error(
                                    "Αποτυχία αποθήκευσης: ${e.message}"
                                )
                            }
                        )
                    },
                    onFailure = { e ->
                        _status.value = ProfileUpdateStatus.Error(
                            "Αποτυχία ανεβάσματος εικόνας: ${e.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _status.value = ProfileUpdateStatus.Error("Σφάλμα: ${e.message}")
            }
        }
    }

    fun clearStatus() {
        _status.value = ProfileUpdateStatus.Idle
    }
}
