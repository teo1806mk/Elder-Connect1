package com.example.elder_connect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elder_connect.data.entities.Contact
import com.example.elder_connect.data.repository.ElderConnectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── State & Status models ────────────────────────────────────────────────────

/**
 * Ολόκληρη κατάσταση της φόρμας σε ένα αντικείμενο.
 * Χρησιμοποιείται από [AddEditContactScreen] μέσω [AddEditContactViewModel.formState].
 */
data class ContactFormState(
    val name: String = "",
    val relationship: String = "",
    val phoneNumber: String = "",
    val imageUri: String? = null,
    val isFavorite: Boolean = false,
    val supportsVideoCall: Boolean = true,
    val isEditMode: Boolean = false,
    // Validation errors (null = χωρίς error)
    val nameError: String? = null,
    val phoneError: String? = null
)

/**
 * Αποτέλεσμα αποθήκευσης / διαγραφής.
 */
sealed class FormSaveStatus {
    data object Idle    : FormSaveStatus()
    data object Saving  : FormSaveStatus()
    data object Success : FormSaveStatus()
    data class  Error(val message: String) : FormSaveStatus()
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

/**
 * ViewModel για προσθήκη/επεξεργασία/διαγραφή επαφής.
 *
 * API που περιμένει η [AddEditContactScreen]:
 *  - formState     : StateFlow<ContactFormState>
 *  - saveStatus    : StateFlow<FormSaveStatus>
 *  - updateName / updateRelationship / updatePhone
 *  - toggleFavorite / toggleVideoCall
 *  - loadContact(contactId)
 *  - save()
 *  - delete()
 *  - clearStatus()
 *
 * Ο Room user ID πρέπει να οριστεί από έξω (πριν το save) μέσω [setRoomUserId].
 * Στο ElderConnectApp.kt: καλείται σε LaunchedEffect(localUser?.id).
 */
class AddEditContactViewModel(
    private val repository: ElderConnectRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(ContactFormState())
    val formState: StateFlow<ContactFormState> = _formState.asStateFlow()

    private val _saveStatus = MutableStateFlow<FormSaveStatus>(FormSaveStatus.Idle)
    val saveStatus: StateFlow<FormSaveStatus> = _saveStatus.asStateFlow()

    // Edit mode tracking
    private var editingContactId: Long? = null
    private var ownerUserId: Long? = null   // userId της επαφής (edit mode)
    private var roomUserId: Long? = null    // τρέχων logged-in Room user

    // ─── Initialization ───────────────────────────────────────────

    /**
     * Ορίζει το Room user ID του τρέχοντα χρήστη.
     * Καλείται από ElderConnectApp.kt μέσω LaunchedEffect(localUser?.id).
     */
    fun setRoomUserId(id: Long) {
        roomUserId = id
    }

    /**
     * Φόρτωση επαφής για επεξεργασία (edit mode).
     */
    fun loadContact(contactId: Long) {
        viewModelScope.launch {
            try {
                val contact = repository.getContactById(contactId) ?: run {
                    _saveStatus.value = FormSaveStatus.Error("Δεν βρέθηκε η επαφή")
                    return@launch
                }
                editingContactId = contactId
                ownerUserId = contact.userId
                _formState.value = ContactFormState(
                    name            = contact.name,
                    relationship    = contact.relationship,
                    phoneNumber     = contact.phoneNumber,
                    imageUri        = contact.avatarResource,
                    isFavorite      = contact.isFavorite,
                    supportsVideoCall = contact.supportsVideoCall,
                    isEditMode      = true
                )
            } catch (e: Exception) {
                _saveStatus.value = FormSaveStatus.Error("Σφάλμα φόρτωσης: ${e.message}")
            }
        }
    }

    // ─── Field updaters ───────────────────────────────────────────

    fun updateName(value: String) {
        _formState.value = _formState.value.copy(name = value, nameError = null)
    }

    fun updateRelationship(value: String) {
        _formState.value = _formState.value.copy(relationship = value)
    }

    fun updatePhone(value: String) {
        _formState.value = _formState.value.copy(phoneNumber = value, phoneError = null)
    }

    fun updateImageUri(uri: String?) {
        _formState.value = _formState.value.copy(imageUri = uri)
    }

    fun toggleFavorite() {
        _formState.value = _formState.value.copy(isFavorite = !_formState.value.isFavorite)
    }

    fun toggleVideoCall() {
        _formState.value = _formState.value.copy(supportsVideoCall = !_formState.value.supportsVideoCall)
    }

    // ─── Actions ──────────────────────────────────────────────────

    /**
     * Αποθήκευση επαφής (add ή update).
     */
    fun save() {
        val state = _formState.value

        // Validation
        var hasError = false
        if (state.name.isBlank()) {
            _formState.value = _formState.value.copy(nameError = "Το όνομα είναι υποχρεωτικό")
            hasError = true
        }
        if (state.phoneNumber.isBlank()) {
            _formState.value = _formState.value.copy(phoneError = "Το τηλέφωνο είναι υποχρεωτικό")
            hasError = true
        }
        if (hasError) return

        // Βρίσκουμε το userId: σε edit mode χρησιμοποιούμε το ownerUserId,
        // σε add mode χρησιμοποιούμε το roomUserId του logged-in χρήστη.
        val userId = ownerUserId ?: roomUserId ?: run {
            _saveStatus.value = FormSaveStatus.Error(
                "Δεν βρέθηκε ο χρήστης. Παρακαλώ αποσυνδεθείτε και συνδεθείτε ξανά."
            )
            return
        }

        viewModelScope.launch {
            _saveStatus.value = FormSaveStatus.Saving
            try {
                if (editingContactId != null) {
                    // Edit mode: ενημέρωση υπάρχουσας επαφής
                    repository.updateContact(
                        Contact(
                            id               = editingContactId!!,
                            userId           = userId,
                            name             = state.name,
                            relationship     = state.relationship,
                            phoneNumber      = state.phoneNumber,
                            avatarResource   = state.imageUri,
                            isFavorite       = state.isFavorite,
                            supportsVideoCall = state.supportsVideoCall
                        )
                    )
                } else {
                    // Add mode: νέα επαφή
                    repository.insertContact(
                        Contact(
                            userId           = userId,
                            name             = state.name,
                            relationship     = state.relationship,
                            phoneNumber      = state.phoneNumber,
                            avatarResource   = state.imageUri,
                            isFavorite       = state.isFavorite,
                            supportsVideoCall = state.supportsVideoCall
                        )
                    )
                }
                _saveStatus.value = FormSaveStatus.Success
            } catch (e: Exception) {
                _saveStatus.value = FormSaveStatus.Error("Σφάλμα αποθήκευσης: ${e.message}")
            }
        }
    }

    /**
     * Διαγραφή επαφής (μόνο σε edit mode).
     */
    fun delete() {
        val contactId = editingContactId ?: return

        viewModelScope.launch {
            _saveStatus.value = FormSaveStatus.Saving
            try {
                val contact = repository.getContactById(contactId)
                if (contact != null) {
                    repository.deleteContact(contact)
                    _saveStatus.value = FormSaveStatus.Success
                } else {
                    _saveStatus.value = FormSaveStatus.Error("Δεν βρέθηκε η επαφή")
                }
            } catch (e: Exception) {
                _saveStatus.value = FormSaveStatus.Error("Σφάλμα διαγραφής: ${e.message}")
            }
        }
    }

    fun clearStatus() {
        _saveStatus.value = FormSaveStatus.Idle
    }
}
