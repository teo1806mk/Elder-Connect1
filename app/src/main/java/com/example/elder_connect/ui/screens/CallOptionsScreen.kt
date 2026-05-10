package com.example.elder_connect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elder_connect.ui.theme.ElderGreen
import com.example.elder_connect.ui.viewmodels.CallOptionsViewModel
import com.example.elder_connect.utils.CallHelper

/**
 * CallOptionsScreen - Δείχνει στον χρήστη επιλογές κλήσης για μια επαφή.
 *
 * Επιλογές:
 * - 📞 Κανονική κλήση (Phone Dialer μέσω Intent)
 * - 🎥 Βιντεοκλήση WhatsApp
 * - 📞 Φωνητική κλήση WhatsApp
 *
 * Κάθε επιλογή έχει popup confirmation πριν την κλήση,
 * για να αποφεύγονται άσκοπες κλήσεις από παρακρόσσια.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallOptionsScreen(
    viewModel: CallOptionsViewModel,
    contactId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val contact by viewModel.contact.collectAsStateWithLifecycle()

    LaunchedEffect(contactId) {
        viewModel.loadContact(contactId)
    }

    val whatsappAvailable = remember { CallHelper.isWhatsAppInstalled(context) }
    var pendingCallType by remember { mutableStateOf<CallType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Κλήση", fontWeight = FontWeight.Bold) },
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
        contact?.let { c ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                // ----------- Avatar -----------
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = c.name.take(1),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = c.name,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = c.relationship,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = c.phoneNumber,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(48.dp))

                Text(
                    text = "Πώς θες να καλέσεις;",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                CallOptionButton(
                    icon = Icons.Filled.Phone,
                    title = "Κανονική Κλήση",
                    subtitle = "Μέσω τηλεφώνου",
                    iconColor = ElderGreen,
                    onClick = { pendingCallType = CallType.PHONE }
                )

                Spacer(Modifier.height(12.dp))

                if (whatsappAvailable && c.supportsVideoCall) {
                    CallOptionButton(
                        icon = Icons.Filled.Videocam,
                        title = "Βιντεοκλήση WhatsApp",
                        subtitle = "Με εικόνα",
                        iconColor = Color(0xFF25D366),
                        onClick = { pendingCallType = CallType.WHATSAPP_VIDEO }
                    )

                    Spacer(Modifier.height(12.dp))

                    CallOptionButton(
                        icon = Icons.Filled.Phone,
                        title = "Φωνητική WhatsApp",
                        subtitle = "Δωρεάν μέσω internet",
                        iconColor = Color(0xFF25D366),
                        onClick = { pendingCallType = CallType.WHATSAPP_VOICE }
                    )
                } else if (!whatsappAvailable) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Εγκατάστησε το WhatsApp για βιντεοκλήσεις",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    pendingCallType?.let { callType ->
        contact?.let { c ->
            AlertDialog(
                onDismissRequest = { pendingCallType = null },
                title = {
                    Text(
                        text = when (callType) {
                            CallType.PHONE -> "Κλήση ${c.name};"
                            CallType.WHATSAPP_VIDEO -> "Βιντεοκλήση ${c.name};"
                            CallType.WHATSAPP_VOICE -> "WhatsApp κλήση ${c.name};"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Θα κληθεί στο: ${c.phoneNumber}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when (callType) {
                                CallType.PHONE ->
                                    CallHelper.makePhoneCall(context, c.phoneNumber)
                                CallType.WHATSAPP_VIDEO ->
                                    CallHelper.makeWhatsAppVideoCall(context, c.phoneNumber)
                                CallType.WHATSAPP_VOICE ->
                                    CallHelper.makeWhatsAppVoiceCall(context, c.phoneNumber)
                            }
                            pendingCallType = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElderGreen)
                    ) {
                        Text("ΚΛΗΣΗ", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingCallType = null }) {
                        Text("ΑΚΥΡΩΣΗ", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun CallOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private enum class CallType {
    PHONE, WHATSAPP_VIDEO, WHATSAPP_VOICE
}