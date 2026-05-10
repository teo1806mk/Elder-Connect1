package com.example.elder_connect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.data.repository.ElderConnectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel για προσθήκη/επεξεργασία επαφής.
 *
 * ΣΗΜΑΝΤΙΚΟ: Χρειάζεται το SessionViewModel για να πάρει το Room user ID
 */
class AddEditContactViewModel(
    private val repository: ElderConnectRepository
) : ViewModel() {

    // Form state
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _relationship = MutableStateFlow("")
    val relationship: StateFlow<String> = _relationship.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _supportsVideoCall = MutableStateFlow(true)
    val supportsVideoCall: StateFlow<Boolean> = _supportsVideoCall.asStateFlow()

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // Edit mode
    private var editingContactId: Long? = null
    private var currentUserId: Long? = null

    /**
     * Φόρτωση επαφής για επεξεργασία
     */
    fun loadContact(contactId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val contact = repository.getContactById(contactId)
                if (contact != null) {
                    editingContactId = contactId
                    currentUserId = contact.userId
                    _name.value = contact.name
                    _relationship.value = contact.relationship
                    _phoneNumber.value = contact.phoneNumber
                    _isFavorite.value = contact.isFavorite
                    _supportsVideoCall.value = contact.supportsVideoCall
                } else {
                    _errorMessage.value = "Δεν βρέθηκε η επαφή"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Σφάλμα φόρτωσης: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Ενημέρωση πεδίων
     */
    fun updateName(value: String) { _name.value = value }
    fun updateRelationship(value: String) { _relationship.value = value }
    fun updatePhoneNumber(value: String) { _phoneNumber.value = value }
    fun toggleFavorite() { _isFavorite.value = !_isFavorite.value }
    fun toggleVideoCall() { _supportsVideoCall.value = !_supportsVideoCall.value }

    /**
     * Αποθήκευση επαφής.
     *
     * ⚠️ Χρειάζεται το Room user ID από το SessionViewModel
     */
    fun saveContact(roomUserId: Long) {
        viewModelScope.launch {
            // Validation
            if (_name.value.isBlank()) {
                _errorMessage.value = "Το όνομα είναι υποχρεωτικό"
                return@launch
            }
            if (_phoneNumber.value.isBlank()) {
                _errorMessage.value = "Το τηλέφωνο είναι υποχρεωτικό"
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            try {
                if (editingContactId != null) {
                    // Edit mode
                    val updated = Contact(
                        id = editingContactId!!,
                        userId = currentUserId ?: roomUserId,
                        name = _name.value,
                        relationship = _relationship.value,
                        phoneNumber = _phoneNumber.value,
                        isFavorite = _isFavorite.value,
                        supportsVideoCall = _supportsVideoCall.value
                    )
                    repository.updateContact(updated)
                } else {
                    // Create mode
                    val newContact = Contact(
                        userId = roomUserId,
                        name = _name.value,
                        relationship = _relationship.value,
                        phoneNumber = _phoneNumber.value,
                        isFavorite = _isFavorite.value,
                        supportsVideoCall = _supportsVideoCall.value
                    )
                    repository.insertContact(newContact)
                }

                _saveSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Σφάλμα αποθήκευσης: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Διαγραφή επαφής (μόνο σε edit mode)
     */
    fun deleteContact(onSuccess: () -> Unit) {
        if (editingContactId == null) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val contact = repository.getContactById(editingContactId!!)
                if (contact != null) {
                    repository.deleteContact(contact)
                    onSuccess()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Σφάλμα διαγραφής: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}