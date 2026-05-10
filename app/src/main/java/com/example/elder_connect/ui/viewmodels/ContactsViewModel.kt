package com.example.elder_connect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.data.repository.ElderConnectRepository
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
 * State holder για την Contacts screen.
 *
 * Λειτουργίες:
 * - Φόρτωση όλων των επαφών του χρήστη
 * - Αναζήτηση επαφών (με LIKE query)
 * - Δημιουργία/επεξεργασία/διαγραφή επαφής
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModel(
    private val repository: ElderConnectRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val currentUser = repository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Όλες οι επαφές του χρήστη, φιλτραρισμένες με τον search query.
     * Συνδυάζει 2 flows: τον user και το search query.
     */
    val contacts: StateFlow<List<Contact>> = combine(
        currentUser,
        _searchQuery
    ) { user, query ->
        Pair(user, query)
    }.flatMapLatest { (user, query) ->
        when {
            user == null -> flowOf(emptyList())
            query.isBlank() -> repository.getContactsByUser(user.id)
            else -> repository.searchContacts(user.id, query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addContact(name: String, relationship: String, phone: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.insertContact(
                Contact(
                    userId = user.id,
                    name = name,
                    relationship = relationship,
                    phoneNumber = phone,
                    isFavorite = false
                )
            )
        }
    }

    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch {
            repository.updateContact(contact.copy(isFavorite = !contact.isFavorite))
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }
}