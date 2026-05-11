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
 * Λαμβάνει το userId από [setUserId] (καλείται από ElderConnectApp.kt μέσω
 * LaunchedEffect), αποφεύγοντας εξάρτηση από repository.currentUser που
 * δεν υπάρχει στο repository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModel(
    private val repository: ElderConnectRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Ορίζει το Room user ID. Καλείται από ElderConnectApp.kt σε LaunchedEffect(localUser?.id).
     */
    fun setUserId(id: Long) {
        _userId.value = id
    }

    /**
     * Λίστα επαφών, φιλτραρισμένη με search query.
     */
    val contacts: StateFlow<List<Contact>> = combine(
        _userId,
        _searchQuery
    ) { userId, query ->
        Pair(userId, query)
    }.flatMapLatest { (userId, query) ->
        when {
            userId == null -> flowOf(emptyList())
            query.isBlank() -> repository.getContactsByUser(userId)
            else -> repository.searchContacts(userId, query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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
