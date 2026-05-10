package com.example.elder_connect.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.elder_connect.data.firestore.UserRole

data class DrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val description: String? = null,
    val rolesAllowed: Set<UserRole> = setOf(UserRole.ADMIN, UserRole.USER, UserRole.ELDER)
)

/** Κύρια items - όλοι έχουν πρόσβαση */
val mainDrawerItems = listOf(
    DrawerItem(ElderRoute.Home.route, "Αρχική", Icons.Filled.Home),
    DrawerItem(ElderRoute.Contacts.route, "Επαφές", Icons.Filled.Group),
    DrawerItem(ElderRoute.News.route, "Νέα Δήμου", Icons.Filled.Newspaper),
    DrawerItem(ElderRoute.MoodCheck.route, "Πώς Νιώθεις", Icons.Filled.SentimentSatisfied)
)

/** Έξτρα items - role-specific */
val extraDrawerItems = listOf(
    DrawerItem(
        route = ElderRoute.AddContact.route,
        label = "Νέα Επαφή",
        icon = Icons.Filled.Add,
        rolesAllowed = setOf(UserRole.ADMIN, UserRole.USER)  // Όχι Elder
    ),
    DrawerItem(
        route = ElderRoute.CreateElder.route,
        label = "Δημιουργία Υπερήλικα",
        icon = Icons.Filled.Elderly,
        rolesAllowed = setOf(UserRole.ADMIN, UserRole.USER)  // Μόνο Caregivers
    ),
    DrawerItem(
        route = ElderRoute.EmergencyPhones.route,
        label = "Έκτακτη Ανάγκη",
        icon = Icons.Filled.LocalHospital
    ),
    DrawerItem(
        route = ElderRoute.Statistics.route,
        label = "Στατιστικά",
        icon = Icons.Filled.Info,
        rolesAllowed = setOf(UserRole.ADMIN, UserRole.USER)
    )
)

/** Επιστρέφει τα φιλτραρισμένα drawer items για τον τρέχοντα χρήστη */
fun filterDrawerItems(items: List<DrawerItem>, role: UserRole): List<DrawerItem> {
    return items.filter { role in it.rolesAllowed }
}