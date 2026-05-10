package com.example.elder_connect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.data.entities.User
import com.example.elder_connect.data.repository.ElderConnectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

/**
 * State holder για την Home Screen.
 *
 * Συνδυάζει:
 * - Τον τρέχοντα χρήστη (Θοδωρή)
 * - Τις αγαπημένες επαφές που υποστηρίζουν videocall
 *   (αυτές που εμφανίζονται στην αρχική: Ελένη, Κώστας)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: ElderConnectRepository
) : ViewModel() {

    /**
     * Ο τρέχων χρήστης. Αρχικά null μέχρι να φορτωθεί από τη βάση.
     */
    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Οι αγαπημένες επαφές για videocall (μέγιστο 2 στην αρχική).
     * flatMapLatest: όταν αλλάζει ο user, αλλάζει και η ροή επαφών.
     */
    val favoriteContacts: StateFlow<List<Contact>> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getFavoriteVideoCallContacts(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Επιστρέφει χαιρετισμό βάσει ώρας ημέρας.
     */
    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Καλημέρα"
            in 12..17 -> "Καλησπέρα"
            else -> "Καληνύχτα"
        }
    }
}