package com.example.elder_connect.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ElderRoute(val route: String) {
    // Auth flow
    data object Login : ElderRoute("login")
    data object Register : ElderRoute("register")
    data object CreateElder : ElderRoute("create_elder")
    data object Profile         : ElderRoute("profile")
    // Main app
    data object Home : ElderRoute("home")
    data object Contacts : ElderRoute("contacts")
    data object News : ElderRoute("news")
    data object MoodCheck : ElderRoute("mood")


    data object AddContact : ElderRoute("contact/new")
    data object EditContact : ElderRoute("contact/edit/{contactId}") {
        fun createRoute(contactId: Long) = "contact/edit/$contactId"
    }


    data object AddAnnouncement : ElderRoute("announcement/new")  // ΝΕΟ

    data object EmergencyPhones : ElderRoute("emergency_phones")
    data object Statistics : ElderRoute("statistics")

    data object VideoCall : ElderRoute("videocall/{contactId}") {
        fun createRoute(contactId: Long) = "videocall/$contactId"
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(ElderRoute.Home.route, "ΑΡΧΙΚΗ", Icons.Filled.Home),
    BottomNavItem(ElderRoute.Contacts.route, "ΕΠΑΦΕΣ", Icons.Filled.Phone),
    BottomNavItem(ElderRoute.News.route, "ΝΕΑ", Icons.Filled.Newspaper),
    BottomNavItem(ElderRoute.MoodCheck.route, "ΠΩΣ ΝΙΩΘΕΙΣ", Icons.Filled.SentimentSatisfied)
)