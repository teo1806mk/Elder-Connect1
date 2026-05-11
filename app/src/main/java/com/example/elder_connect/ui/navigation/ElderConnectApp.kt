package com.example.elder_connect.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.elder_connect.ElderConnectApplication
import com.example.elder_connect.data.firestore.UserRole
import com.example.elder_connect.ui.screens.AddAnnouncementScreen
import com.example.elder_connect.ui.screens.AddEditContactScreen
import com.example.elder_connect.ui.screens.CallOptionsScreen
import com.example.elder_connect.ui.screens.ContactsScreen
import com.example.elder_connect.ui.screens.EmergencyPhonesScreen
import com.example.elder_connect.ui.screens.HomeScreen
import com.example.elder_connect.ui.screens.MoodCheckScreen
import com.example.elder_connect.ui.screens.NewsScreen
import com.example.elder_connect.ui.screens.auth.CreateElderScreen
import com.example.elder_connect.ui.viewmodels.AddEditContactViewModel
import com.example.elder_connect.ui.viewmodels.AuthViewModel
import com.example.elder_connect.ui.viewmodels.CallOptionsViewModel
import com.example.elder_connect.ui.viewmodels.ContactsViewModel
import com.example.elder_connect.ui.viewmodels.ElderViewModelFactory
import com.example.elder_connect.ui.viewmodels.HomeViewModel
import com.example.elder_connect.ui.viewmodels.MoodViewModel
import com.example.elder_connect.ui.viewmodels.NewsViewModel
import com.example.elder_connect.ui.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

/**
 * Root composable του app.
 * Αν ο χρήστης δεν είναι logged in → AuthNavGraph (login/register).
 * Αν είναι logged in → MainAppNavGraph (το κύριο app).
 */
@Composable
fun ElderConnectApp() {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    if (currentUser == null) {
        AuthNavGraph(
            authViewModel = authViewModel,
            onAuthSuccess = { /* currentUser γίνεται non-null, recompose αυτόματα */ }
        )
    } else {
        MainAppNavGraph(authViewModel = authViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as ElderConnectApplication
    val factory = remember { ElderViewModelFactory(app, app.repository) }

    val sessionViewModel: SessionViewModel = viewModel(factory = factory)
    val authProfile by authViewModel.currentUser.collectAsStateWithLifecycle()
    val localUser by sessionViewModel.localUser.collectAsStateWithLifecycle()

    // ─── FIX #1: Συγχρονισμός — onLoginSuccess δέχεται μόνο UserProfile ───────
    // Το uid εξάγεται εσωτερικά από profile.uid (καλύπτει Firebase Auth + Elder)
    LaunchedEffect(authProfile) {
        authProfile?.let { sessionViewModel.onLoginSuccess(it) }
    }

    // ─── FIX #2: Ενημέρωση HomeViewModel με τον τρέχοντα user ─────────────────
    val homeVm: HomeViewModel = viewModel(factory = factory)
    LaunchedEffect(localUser) {
        localUser?.let { homeVm.setCurrentUser(it) }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val userRole = remember(authProfile) {
        authProfile?.role?.let { UserRole.fromKey(it) } ?: UserRole.USER
    }

    val hideTopBar = currentRoute in listOf(
        ElderRoute.AddContact.route,
        ElderRoute.EditContact.route,
        ElderRoute.AddAnnouncement.route,
        ElderRoute.VideoCall.route,
        ElderRoute.CreateElder.route
    )

    // Δείχνουμε loading spinner μέχρι να ολοκληρωθεί ο Room sync
    if (localUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !hideTopBar,
        drawerContent = {
            ElderDrawerContent(
                userName = authProfile?.fullName ?: "",
                userRole = userRole,
                currentRoute = currentRoute,
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(ElderRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    sessionViewModel.onLogout()
                    authViewModel.logout()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (!hideTopBar) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = getScreenTitle(currentRoute),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Μενού",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = ElderRoute.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                // ─── Home ─────────────────────────────────────────────────────
                composable(ElderRoute.Home.route) {
                    HomeScreen(
                        viewModel = homeVm,
                        onContactClick = { contact ->
                            navController.navigate(ElderRoute.VideoCall.createRoute(contact.id))
                        },
                        onContactsListClick = {
                            navController.navigate(ElderRoute.Contacts.route)
                        },
                        onSosClick = {
                            navController.navigate(ElderRoute.EmergencyPhones.route)
                        },
                        onEmergencyPhonesClick = {
                            navController.navigate(ElderRoute.EmergencyPhones.route)
                        },
                        onNewsClick = {
                            navController.navigate(ElderRoute.News.route)
                        },
                        onMoodClick = {
                            navController.navigate(ElderRoute.MoodCheck.route)
                        }
                    )
                }

                // ─── Contacts ─────────────────────────────────────────────────
                composable(ElderRoute.Contacts.route) {
                    val vm: ContactsViewModel = viewModel(factory = factory)

                    // ─── FIX #3: Ενημέρωση ContactsViewModel με το userId ────
                    LaunchedEffect(localUser?.id) {
                        localUser?.id?.let { vm.setUserId(it) }
                    }

                    ContactsScreen(
                        viewModel = vm,
                        onContactClick = { c ->
                            navController.navigate(ElderRoute.VideoCall.createRoute(c.id))
                        },
                        onContactLongClick = { c ->
                            navController.navigate(ElderRoute.EditContact.createRoute(c.id))
                        },
                        onAddContact = {
                            navController.navigate(ElderRoute.AddContact.route)
                        }
                    )
                }

                // ─── News ─────────────────────────────────────────────────────
                composable(ElderRoute.News.route) {
                    val vm: NewsViewModel = viewModel(factory = factory)
                    NewsScreen(
                        viewModel = vm,
                        onAddAnnouncement = {
                            navController.navigate(ElderRoute.AddAnnouncement.route)
                        }
                    )
                }

                // ─── Mood Check ───────────────────────────────────────────────
                composable(ElderRoute.MoodCheck.route) {
                    val vm: MoodViewModel = viewModel(factory = factory)
                    MoodCheckScreen(
                        viewModel = vm,
                        userId = localUser?.id ?: 0L,
                        onSaved = {
                            navController.navigate(ElderRoute.Home.route) {
                                popUpTo(ElderRoute.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                // ─── Add Contact ──────────────────────────────────────────────
                composable(ElderRoute.AddContact.route) {
                    val vm: AddEditContactViewModel = viewModel(factory = factory)

                    // ─── FIX #4: Δίνουμε roomUserId στο ViewModel ────────────
                    LaunchedEffect(localUser?.id) {
                        localUser?.id?.let { vm.setRoomUserId(it) }
                    }

                    AddEditContactScreen(
                        viewModel = vm,
                        contactId = null,
                        onSaved = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                // ─── Edit Contact ─────────────────────────────────────────────
                composable(
                    route = ElderRoute.EditContact.route,
                    arguments = listOf(navArgument("contactId") { type = NavType.LongType })
                ) { backStack ->
                    val contactId = backStack.arguments?.getLong("contactId")
                    val vm: AddEditContactViewModel = viewModel(factory = factory)

                    // ─── FIX #4 (edit mode): Δίνουμε roomUserId ──────────────
                    LaunchedEffect(localUser?.id) {
                        localUser?.id?.let { vm.setRoomUserId(it) }
                    }

                    AddEditContactScreen(
                        viewModel = vm,
                        contactId = contactId,
                        onSaved = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                // ─── Add Announcement ─────────────────────────────────────────
                composable(ElderRoute.AddAnnouncement.route) {
                    val vm: NewsViewModel = viewModel(factory = factory)
                    AddAnnouncementScreen(
                        viewModel = vm,
                        onSaved = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                // ─── Create Elder ─────────────────────────────────────────────
                composable(ElderRoute.CreateElder.route) {
                    CreateElderScreen(
                        viewModel = authViewModel,
                        caregiverId = authProfile?.uid ?: "",
                        onDone = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                // ─── Emergency Phones ─────────────────────────────────────────
                composable(ElderRoute.EmergencyPhones.route) {
                    EmergencyPhonesScreen()
                }

                // ─── Statistics (placeholder) ─────────────────────────────────
                composable(ElderRoute.Statistics.route) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Στατιστικά (σύντομα)",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                // ─── Video Call / Call Options ────────────────────────────────
                composable(
                    route = ElderRoute.VideoCall.route,
                    arguments = listOf(navArgument("contactId") { type = NavType.LongType })
                ) { backStack ->
                    val contactId = backStack.arguments?.getLong("contactId") ?: 0L
                    val vm: CallOptionsViewModel = viewModel(factory = factory)
                    CallOptionsScreen(
                        viewModel = vm,
                        contactId = contactId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Drawer Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ElderDrawerContent(
    userName: String,
    userRole: UserRole,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
        // Header με avatar + όνομα
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).ifEmpty { "?" },
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userRole.label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Κύρια items (φιλτραρισμένα ανά role)
        filterDrawerItems(mainDrawerItems, userRole).forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, null, Modifier.size(28.dp)) },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Extra items
        val filteredExtras = filterDrawerItems(extraDrawerItems, userRole)
        if (filteredExtras.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = "Γρήγορες Ενέργειες",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )
            filteredExtras.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, null, Modifier.size(28.dp)) },
                    label = { Text(item.label, style = MaterialTheme.typography.titleMedium) },
                    selected = currentRoute == item.route,
                    onClick = { onItemClick(item.route) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Logout
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Logout, null, Modifier.size(28.dp)) },
            label = {
                Text(
                    "Αποσύνδεση",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun getScreenTitle(route: String?): String = when (route) {
    ElderRoute.Home.route          -> "Αρχική"
    ElderRoute.Contacts.route      -> "Επαφές"
    ElderRoute.News.route          -> "Νέα Δήμου"
    ElderRoute.MoodCheck.route     -> "Πώς Νιώθεις"
    ElderRoute.EmergencyPhones.route -> "Έκτακτη Ανάγκη"
    ElderRoute.Statistics.route    -> "Στατιστικά"
    else                           -> "Elder-Connect"
}
