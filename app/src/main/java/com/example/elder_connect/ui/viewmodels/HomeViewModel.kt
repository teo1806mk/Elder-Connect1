package com.example.elder_connect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.data.entities.User
import com.example.elder_connect.data.repository.ElderConnectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

/**
 * State holder για την Home Screen.
 *
 * Δέχεται τον τρέχοντα user από το SessionViewModel μέσω [setCurrentUser].
 * Αυτό αποφεύγει την ανάγκη για repository.currentUser (που δεν υπάρχει)
 * και καλύπτει και τους Elder users (που δεν έχουν Firebase Auth session).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: ElderConnectRepository
) : ViewModel() {

    // Τρέχων logged-in user — τίθεται από ElderConnectApp.kt μέσω LaunchedEffect
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /**
     * Ορίζει τον τρέχοντα user (καλείται από ElderConnectApp.kt).
     * Ενημερώνει αυτόματα τις αγαπημένες επαφές.
     */
    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }

    /**
     * Οι αγαπημένες επαφές για videocall.
     * Ενημερώνεται αυτόματα όταν αλλάζει ο _currentUser.
     */
    val favoriteContacts: StateFlow<List<Contact>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getFavoriteVideoCallContacts(user.id)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Χαιρετισμός βάσει ώρας ημέρας.
     */
    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11  -> "Καλημέρα"
            in 12..17 -> "Καλησπέρα"
            else      -> "Καληνύχτα"
        }
    }
}
