package com.example.elder_connect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.entities.User
import com.example.elder_connect.data.firestore.UserProfile
import com.example.elder_connect.data.sync.UserSyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Κεντρικό ViewModel για το user session.
 *
 * Διαχειρίζεται:
 * - Το Firebase user profile (από Firestore)
 * - Το Room user ID (για foreign keys)
 * - Συγχρονισμός μεταξύ των δύο
 *
 * Όλα τα άλλα ViewModels πρέπει να παίρνουν τον user από εδώ.
 */
class SessionViewModel(
    private val userSyncService: UserSyncService
) : ViewModel() {

    // Firebase profile (από Firestore)
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Room user (local cache)
    private val _localUser = MutableStateFlow<User?>(null)
    val localUser: StateFlow<User?> = _localUser.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Καλείται όταν ο χρήστης κάνει login.
     * Συγχρονίζει το Firebase profile με τη Room.
     */
    fun onLoginSuccess(firebaseUid: String, profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Συγχρονισμός Firebase → Room
                val roomUserId = userSyncService.syncUser(firebaseUid, profile)

                // Φόρτωση του Room user
                val roomUser = userSyncService.getCurrentLocalUser()

                // Update state
                _userProfile.value = profile
                _localUser.value = roomUser
            } catch (e: Exception) {
                e.printStackTrace()
                // Σε περίπτωση error, τουλάχιστον έχουμε το profile
                _userProfile.value = profile
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Καλείται όταν ο χρήστης κάνει logout.
     * Καθαρίζει το session state.
     */
    fun onLogout() {
        _userProfile.value = null
        _localUser.value = null
    }

    /**
     * Επαναφόρτωση του τρέχοντος user (για refresh)
     */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val roomUser = userSyncService.getCurrentLocalUser()
                _localUser.value = roomUser
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Helper: Επιστρέφει το Room user ID (για ViewModels που το χρειάζονται)
     */
    fun getRoomUserId(): Long? = _localUser.value?.id

    /**
     * Helper: Είναι Admin ο τρέχων χρήστης;
     */
    fun isAdmin(): Boolean = _userProfile.value?.role == "ADMIN"

    /**
     * Helper: Είναι Elder ο τρέχων χρήστης;
     */
    fun isElder(): Boolean = _userProfile.value?.role == "ELDER"

    /**
     * Helper: Είναι Caregiver ο τρέχων χρήστης;
     */
    fun isCaregiver(): Boolean = _userProfile.value?.role == "USER"
}