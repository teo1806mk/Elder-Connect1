package com.example.elder_connect
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.elder_connect.ui.navigation.ElderConnectApp
import com.example.elder_connect.ui.theme.ElderConnectTheme

class MainActivity : ComponentActivity() {
    /**
     * Permission launcher για notifications (Android 13+).
     * Καταχωρείται με registerForActivityResult πριν το onCreate.
     */
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        // Όχι κάτι άλλο εδώ - αν αρνηθεί, απλά δε στέλνουμε notifications
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ζήτησε permission για notifications σε Android 13+
        requestNotificationPermissionIfNeeded()

        setContent {
            ElderConnectTheme {
                ElderConnectApp()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}