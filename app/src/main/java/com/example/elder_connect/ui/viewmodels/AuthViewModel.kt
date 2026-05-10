package com.example.elder_connect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.firestore.AuthService
import com.example.elder_connect.data.firestore.UserProfile
import com.example.elder_connect.data.firestore.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel για όλα τα authentication flows.
 *
 * Χειρίζεται 2 ξεχωριστές πηγές αλήθειας:
 *  1. Firebase Auth state (για ADMIN/USER)
 *  2. Elder session (για ELDER - δεν χρησιμοποιεί Firebase Auth)
 *
 * Κάνει merge σε ένα ενιαίο currentUser StateFlow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {

    // Πηγή 1: Firebase Auth user profile (από Firestore)
    private val firebaseProfile: StateFlow<UserProfile?> = authService.authStateFlow()
        .flatMapLatest { firebaseUser ->
            if (firebaseUser != null) {
                authService.currentUserProfileFlow()
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,  // ξεκινάει αμέσως
            initialValue = null
        )

    // Πηγή 2: Elder session (manual)
    private val _elderSession = MutableStateFlow<UserProfile?>(null)
    val elderSession: StateFlow<UserProfile?> = _elderSession.asStateFlow()

    /**
     * Combined: αν υπάρχει elder session ή firebase profile, αυτό είναι ο current user.
     * Elder session έχει προτεραιότητα.
     */
    val currentUser: StateFlow<UserProfile?> = combine(
        firebaseProfile,
        _elderSession
    ) { fbProfile, elderProfile ->
        elderProfile ?: fbProfile
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private val _authStatus = MutableStateFlow<AuthStatus>(AuthStatus.Idle)
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authStatus.value = AuthStatus.Loading
            val result = authService.loginWithEmail(email, password)
            _authStatus.value = result.fold(
                onSuccess = { AuthStatus.Success(it) },
                onFailure = { AuthStatus.Error(translateFirebaseError(it.message)) }
            )
        }
    }

    fun loginElderWithCode(code: String) {
        viewModelScope.launch {
            _authStatus.value = AuthStatus.Loading
            val result = authService.loginElderWithCode(code)
            result.fold(
                onSuccess = {
                    _elderSession.value = it   // Set elder session
                    _authStatus.value = AuthStatus.Success(it)
                },
                onFailure = {
                    _authStatus.value = AuthStatus.Error(
                        "Λάθος κωδικός. Επικοινώνησε με τον υπεύθυνό σου."
                    )
                }
            )
        }
    }

    fun register(
        email: String,
        password: String,
        fullName: String,
        age: Int,
        city: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            _authStatus.value = AuthStatus.Loading
            val result = authService.registerUser(email, password, fullName, age, city, role)
            _authStatus.value = result.fold(
                onSuccess = { AuthStatus.Success(it) },
                onFailure = { AuthStatus.Error(translateFirebaseError(it.message)) }
            )
        }
    }

    fun createElderProfile(
        caregiverId: String,
        elderName: String,
        elderAge: Int,
        elderCity: String,
        onSuccess: (pairingCode: String) -> Unit
    ) {
        viewModelScope.launch {
            _authStatus.value = AuthStatus.Loading
            val result = authService.createElderProfile(
                caregiverId = caregiverId,
                elderName = elderName,
                elderAge = elderAge,
                elderCity = elderCity
            )
            result.fold(
                onSuccess = { profile ->
                    _authStatus.value = AuthStatus.ElderCreated(profile.pairingCode!!)
                    onSuccess(profile.pairingCode)
                },
                onFailure = {
                    _authStatus.value = AuthStatus.Error(translateFirebaseError(it.message))
                }
            )
        }
    }

    fun logout() {
        authService.logout()
        _elderSession.value = null
        _authStatus.value = AuthStatus.Idle
    }

    fun clearStatus() {
        _authStatus.value = AuthStatus.Idle
    }

    private fun translateFirebaseError(message: String?): String {
        return when {
            message == null -> "Άγνωστο σφάλμα"
            message.contains("password is invalid", ignoreCase = true) ||
                    message.contains("incorrect", ignoreCase = true) ->
                "Λάθος κωδικός πρόσβασης"
            message.contains("no user record", ignoreCase = true) ||
                    message.contains("user-not-found", ignoreCase = true) ->
                "Δεν βρέθηκε χρήστης με αυτό το email"
            message.contains("email address is already", ignoreCase = true) ||
                    message.contains("email-already-in-use", ignoreCase = true) ->
                "Το email χρησιμοποιείται ήδη"
            message.contains("badly formatted", ignoreCase = true) ||
                    message.contains("email address is badly", ignoreCase = true) ->
                "Λάθος μορφή email"
            message.contains("weak password", ignoreCase = true) ||
                    message.contains("at least 6", ignoreCase = true) ->
                "Ο κωδικός πρέπει να έχει τουλάχιστον 6 χαρακτήρες"
            message.contains("network", ignoreCase = true) ->
                "Πρόβλημα σύνδεσης στο διαδίκτυο"
            else -> message
        }
    }
}

sealed class AuthStatus {
    data object Idle : AuthStatus()
    data object Loading : AuthStatus()
    data class Success(val profile: UserProfile) : AuthStatus()
    data class ElderCreated(val pairingCode: String) : AuthStatus()
    data class Error(val message: String) : AuthStatus()
}