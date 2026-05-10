package com.example.elder_connect.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elder_connect.data.firestore.UserRole
import com.example.elder_connect.ui.viewmodels.AuthStatus
import com.example.elder_connect.ui.viewmodels.AuthViewModel

/**
 * RegisterScreen - εγγραφή νέου Caregiver (USER) ή Admin.
 *
 * ELDER λογαριασμοί ΔΕΝ φτιάχνονται από εδώ - τους δημιουργεί ο
 * Caregiver μέσα στο app (στο "Δημιούργησε λογαριασμό υπερήλικα").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistered: () -> Unit,
    onBack: () -> Unit
) {
    val authStatus by viewModel.authStatus.collectAsStateWithLifecycle()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }

    LaunchedEffect(authStatus) {
        if (authStatus is AuthStatus.Success) {
            viewModel.clearStatus()
            onRegistered()
        }
    }

    val passwordsMatch = password == passwordConfirm
    val isValid = fullName.isNotBlank() && email.isNotBlank() &&
            password.length >= 6 && passwordsMatch &&
            age.toIntOrNull() != null && city.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Εγγραφή", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Πίσω")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // ----------- Role selection -----------
            Text(
                "Τι είδος λογαριασμός;",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            RoleOptionCard(
                role = UserRole.USER,
                icon = Icons.Filled.Person,
                selected = selectedRole == UserRole.USER,
                onClick = { selectedRole = UserRole.USER }
            )
            Spacer(Modifier.height(8.dp))
            RoleOptionCard(
                role = UserRole.ADMIN,
                icon = Icons.Filled.AdminPanelSettings,
                selected = selectedRole == UserRole.ADMIN,
                onClick = { selectedRole = UserRole.ADMIN }
            )

            Spacer(Modifier.height(24.dp))

            // ----------- Form fields -----------
            FormField("Ονοματεπώνυμο", fullName) { fullName = it }
            Spacer(Modifier.height(12.dp))

            FormField(
                "Email", email,
                keyboardType = KeyboardType.Email
            ) { email = it.trim() }
            Spacer(Modifier.height(12.dp))

            FormField(
                "Ηλικία", age,
                keyboardType = KeyboardType.Number
            ) { if (it.all { c -> c.isDigit() }) age = it }
            Spacer(Modifier.height(12.dp))

            FormField("Πόλη", city) { city = it }
            Spacer(Modifier.height(12.dp))

            FormField(
                "Κωδικός (τουλάχιστον 6 χαρακτήρες)",
                password,
                isPassword = true
            ) { password = it }
            Spacer(Modifier.height(12.dp))

            FormField(
                "Επιβεβαίωση κωδικού",
                passwordConfirm,
                isPassword = true,
                isError = passwordConfirm.isNotEmpty() && !passwordsMatch
            ) { passwordConfirm = it }

            if (passwordConfirm.isNotEmpty() && !passwordsMatch) {
                Text(
                    "Οι κωδικοί δεν ταιριάζουν",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // ----------- Register button -----------
            Button(
                onClick = {
                    viewModel.register(
                        email = email,
                        password = password,
                        fullName = fullName,
                        age = age.toIntOrNull() ?: 0,
                        city = city,
                        role = selectedRole
                    )
                },
                enabled = isValid && authStatus !is AuthStatus.Loading,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (authStatus is AuthStatus.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "ΕΓΓΡΑΦΗ",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error
            if (authStatus is AuthStatus.Error) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (authStatus as AuthStatus.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RoleOptionCard(
    role: UserRole,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    role.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    role.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        visualTransformation = if (isPassword) PasswordVisualTransformation()
        else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = keyboardType
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}