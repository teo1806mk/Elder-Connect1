package com.example.elder_connect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.data.repository.ElderConnectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Φορτώνει μία επαφή από τη βάση βάσει contactId.
 * Χρησιμοποιείται από την CallOptionsScreen.
 */
class CallOptionsViewModel(
    private val repository: ElderConnectRepository
) : ViewModel() {

    private val _contact = MutableStateFlow<Contact?>(null)
    val contact: StateFlow<Contact?> = _contact.asStateFlow()

    fun loadContact(contactId: Long) {
        viewModelScope.launch {
            val user = repository.currentUser.first() ?: return@launch
            val allContacts = repository.getContactsByUser(user.id).first()
            _contact.value = allContacts.firstOrNull { it.id == contactId }
        }
    }
}