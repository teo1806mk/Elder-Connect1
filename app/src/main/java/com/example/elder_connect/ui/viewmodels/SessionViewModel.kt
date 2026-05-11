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
 * - Το Firebase/Elder user profile (από Firestore)
 * - Το Room user (για foreign keys στις επαφές / mood entries)
 * - Συγχρονισμός μεταξύ Firestore ↔ Room
 *
 * ΣΗΜΕΙΩΣΗ: Δέχεται μόνο UserProfile — το uid εξάγεται από profile.uid.
 * Αυτό καλύπτει και Firebase Auth users (uid = Firebase UID)
 * και Elder users (uid = "elder_XXXXXX").
 */
class SessionViewModel(
    private val userSyncService: UserSyncService
) : ViewModel() {

    // Firebase/Elder profile
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Room user (local cache)
    private val _localUser = MutableStateFlow<User?>(null)
    val localUser: StateFlow<User?> = _localUser.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Καλείται μετά από επιτυχές login.
     *
     * Χρησιμοποιεί profile.uid για sync — λειτουργεί για ΟΛΑ τα role types:
     *  - ADMIN / USER : profile.uid = Firebase Auth UID
     *  - ELDER        : profile.uid = "elder_XXXXXX"
     */
    fun onLoginSuccess(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sync Firestore profile → Room
                val roomUser = userSyncService.syncUser(profile.uid, profile)
                _userProfile.value = profile
                _localUser.value = roomUser
            } catch (e: Exception) {
                e.printStackTrace()
                // Τουλάχιστον κρατάμε το profile
                _userProfile.value = profile
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Καλείται κατά το logout. Καθαρίζει όλο το session state.
     */
    fun onLogout() {
        _userProfile.value = null
        _localUser.value = null
    }

    // ─── Helpers ──────────────────────────────────────────────────

    fun getRoomUserId(): Long? = _localUser.value?.id
    fun isAdmin(): Boolean = _userProfile.value?.role == "ADMIN"
    fun isElder(): Boolean = _userProfile.value?.role == "ELDER"
    fun isCaregiver(): Boolean = _userProfile.value?.role == "USER"
}
