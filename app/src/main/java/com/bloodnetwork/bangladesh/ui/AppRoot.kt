package com.bloodnetwork.bangladesh.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.screens.ChatbotScreen
import com.bloodnetwork.bangladesh.ui.screens.DonorDashboardScreen
import com.bloodnetwork.bangladesh.ui.screens.DonorProfileScreen
import com.bloodnetwork.bangladesh.ui.screens.AboutScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminAnalyticsScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminDashboardScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminEligibilityQuestionsScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminSystemSettingsScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminUserManagementScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminReportsScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminBloodRequestsScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminMatchesScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminAuditLogsScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminSettingsScreen
import com.bloodnetwork.bangladesh.ui.screens.EditProfileScreen
import com.bloodnetwork.bangladesh.ui.screens.EligibilityScreen
import com.bloodnetwork.bangladesh.ui.screens.FindBloodScreen
import com.bloodnetwork.bangladesh.ui.screens.LandingScreen
import com.bloodnetwork.bangladesh.ui.screens.NotificationsScreen
import com.bloodnetwork.bangladesh.ui.screens.RequestBloodScreen
import com.bloodnetwork.bangladesh.ui.screens.MyRequestsScreen
import com.bloodnetwork.bangladesh.ui.screens.DonorMatchesScreen
import com.bloodnetwork.bangladesh.ui.screens.RequestDetailsScreen
import com.bloodnetwork.bangladesh.ui.screens.auth.LoginScreen
import com.bloodnetwork.bangladesh.ui.screens.auth.RegisterScreen
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.ChatbotViewModel

private const val ANIM_DURATION = 300

@Composable
fun AppRoot(
    repository: BloodNetworkRepository,
    initialDeepLink: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val factory = remember(repository) { VmFactory(repository) }

    val authVm: AuthViewModel = viewModel(factory = factory)

    // Read synchronously from EncryptedSharedPreferences (initialized in TokenStore ctor)
    // so the correct start destination is known on the very first frame — no flash of
    // the wrong dashboard while flows emit.
    val start = remember {
        val loggedIn = repository.isLoggedInSync()
        val role = repository.currentUserRoleSync()
        if (loggedIn && role == com.bloodnetwork.bangladesh.data.model.UserRole.Admin) Routes.ADMIN_DASHBOARD
        else if (loggedIn) Routes.DONOR_DASHBOARD
        else Routes.LANDING
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // The assistant button floats over content, so keep it off screens where it would sit on
    // top of form fields / submit buttons, and off admin screens (the assistant is for members).
    val chatFabHiddenRoutes = setOf(
        Routes.CHATBOT, Routes.LOGIN, Routes.REGISTER, Routes.EDIT_PROFILE,
        Routes.REQUEST_BLOOD, Routes.DONOR_PROFILE, Routes.ELIGIBILITY,
    )
    val showChatFab = currentRoute != null &&
        currentRoute !in chatFabHiddenRoutes &&
        !currentRoute.startsWith("admin_") &&
        !currentRoute.startsWith(Routes.REQUEST_DETAILS)

    // A refresh token the server rejected (expired, revoked by a sign-in elsewhere, account
    // deleted) clears the stored session. Without this the user stayed on a stale signed-in
    // screen showing "Session expired" with a Retry button that could never work.
    val signedIn by repository.isLoggedIn.collectAsStateWithLifecycle(initialValue = repository.isLoggedInSync())
    var wasSignedIn by remember { mutableStateOf(signedIn) }
    var sessionLost by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(signedIn) {
        if (wasSignedIn && !signedIn) sessionLost = true
        if (signedIn) sessionLost = false
        wasSignedIn = signedIn
    }
    // Kept separate from the effect above on purpose: on a cold start the session can be lost
    // (refresh rejected) before NavHost has produced a route, and a one-shot check at that moment
    // would find no route and never redirect. Waiting for a route makes it reliable.
    androidx.compose.runtime.LaunchedEffect(sessionLost, currentRoute) {
        if (sessionLost && currentRoute != null) {
            val publicRoutes = setOf(
                Routes.LANDING, Routes.LOGIN, Routes.REGISTER, Routes.FIND_BLOOD,
                Routes.ELIGIBILITY, Routes.CHATBOT, Routes.ABOUT,
            )
            if (currentRoute !in publicRoutes) {
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            }
            sessionLost = false
        }
    }

    androidx.compose.runtime.LaunchedEffect(initialDeepLink) {
        if (initialDeepLink != null) {
            if (repository.isLoggedInSync()) navController.navigate(initialDeepLink)
            // Consumed either way, so a later tap on the same notification type navigates again.
            onDeepLinkConsumed()
        }
    }

    CompositionLocalProvider(LocalVmFactory provides factory) {
        Scaffold(
            floatingActionButton = {
                if (showChatFab) {
                    FloatingActionButton(
                        onClick = { navController.navigate(Routes.CHATBOT) },
                        containerColor = BloodRed,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Icon(Icons.Filled.SmartToy, contentDescription = "AI Assistant")
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = start,
                // Insets are handled once, here, for every screen:
                //  - consumeWindowInsets(innerPadding): the status/navigation bar space is already applied as
                //    padding, so each screen's own Scaffold/TopAppBar must not add it a second time (that
                //    double padding left a large empty gap above every title).
                //  - imePadding(): shrinks the visible area by the keyboard's height, so the focused field -
                //    and the suggestion list under a dropdown - can be scrolled into view above the keyboard
                //    instead of being drawn behind it. It must wrap the screens (not sit after a screen's
                //    verticalScroll, where it only pads the content and leaves the viewport under the keyboard).
                modifier = androidx.compose.ui.Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
                enterTransition = { fadeIn(tween(ANIM_DURATION)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_DURATION)) },
                exitTransition = { fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { fadeOut(tween(ANIM_DURATION)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ANIM_DURATION)) },
            ) {
                composable(Routes.LANDING) { LandingScreen(onNavigate = navController::navigate) }
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onNavigate = navController::navigate,
                        vm = authVm,
                        onLoggedIn = {
                            val user = authVm.uiState.value.user
                            val redirect = authVm.pendingRedirectRoute
                            authVm.pendingRedirectRoute = null
                            if (redirect != null) {
                                // Came from a specific in-progress action (e.g. Request Blood on a
                                // donor's card) — only drop Login itself, so back still returns to
                                // wherever that action started (Find Blood), not a dead end.
                                navController.navigate(redirect) { popUpTo(Routes.LOGIN) { inclusive = true } }
                            } else {
                                val dest = if (user?.role == com.bloodnetwork.bangladesh.data.model.UserRole.Admin) Routes.ADMIN_DASHBOARD else Routes.DONOR_DASHBOARD
                                navController.navigate(dest) { popUpTo(Routes.LANDING) { inclusive = true } }
                            }
                        },
                        onBack = {
                            authVm.pendingRedirectRoute = null
                            navController.popBackStack()
                        },
                    )
                }
                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onNavigate = navController::navigate,
                        vm = authVm,
                        onRegistered = {
                            val user = authVm.uiState.value.user
                            val redirect = authVm.pendingRedirectRoute
                            authVm.pendingRedirectRoute = null
                            if (redirect != null) {
                                navController.navigate(redirect) { popUpTo(Routes.LOGIN) { inclusive = true } }
                            } else {
                                val dest = if (user?.role == com.bloodnetwork.bangladesh.data.model.UserRole.Admin) Routes.ADMIN_DASHBOARD else Routes.DONOR_DASHBOARD
                                navController.navigate(dest) { popUpTo(Routes.LANDING) { inclusive = true } }
                            }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.FIND_BLOOD) {
                    FindBloodScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() }, authVm = authVm)
                }
                composable(Routes.REQUEST_BLOOD) {
                    RequestBloodScreen(onBack = { navController.popBackStack() }, authVm = authVm)
                }
                composable(Routes.MY_REQUESTS) {
                    MyRequestsScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
                }
                composable(Routes.MY_MATCHES) {
                    DonorMatchesScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    "${Routes.REQUEST_DETAILS}/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: return@composable
                    RequestDetailsScreen(requestId = id, onBack = { navController.popBackStack() })
                }
                composable(Routes.ELIGIBILITY) {
                    EligibilityScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
                }
                composable(Routes.NOTIFICATIONS) {
                    NotificationsScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
                }
                composable(Routes.DONOR_DASHBOARD) {
                    DonorDashboardScreen(
                        onNavigate = navController::navigate,
                        vm = authVm,
                        onLogout = {
                            authVm.logout()
                            navController.navigate(Routes.LANDING) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                }
                composable(Routes.DONOR_PROFILE) {
                    DonorProfileScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
                }
                composable(Routes.CHATBOT) {
                    val vm: ChatbotViewModel = viewModel(factory = factory)
                    ChatbotScreen(
                        onBack = { navController.popBackStack() },
                        vm = vm,
                        onSignIn = { navController.navigate(Routes.LOGIN) },
                    )
                }
                composable(Routes.EDIT_PROFILE) {
                    EditProfileScreen(
                        onNavigate = navController::navigate,
                        onBack = { navController.popBackStack() },
                        authVm = authVm,
                        onAccountDeleted = {
                            navController.navigate(Routes.LANDING) { popUpTo(0) { inclusive = true } }
                        },
                    )
                }
                composable(Routes.ADMIN_DASHBOARD) {
                    AdminDashboardScreen(
                        onNavigate = navController::navigate,
                        onBack = { navController.popBackStack() },
                        onLogout = {
                            authVm.logout()
                            navController.navigate(Routes.LANDING) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                }
                composable(Routes.ADMIN_ANALYTICS) {
                    AdminAnalyticsScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    "${Routes.ADMIN_USERS}?role={role}&verify={verify}",
                    arguments = listOf(
                        navArgument("role") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("verify") { type = NavType.StringType; nullable = true; defaultValue = null },
                    ),
                ) { backStackEntry ->
                    AdminUserManagementScreen(
                        onNavigate = navController::navigate,
                        onBack = { navController.popBackStack() },
                        initialRole = backStackEntry.arguments?.getString("role"),
                        initialVerification = backStackEntry.arguments?.getString("verify"),
                    )
                }
                composable(
                    "${Routes.ADMIN_REPORTS}?status={status}",
                    arguments = listOf(navArgument("status") { type = NavType.StringType; nullable = true; defaultValue = null }),
                ) { backStackEntry ->
                    AdminReportsScreen(
                        onNavigate = navController::navigate,
                        onBack = { navController.popBackStack() },
                        initialStatus = backStackEntry.arguments?.getString("status"),
                    )
                }
                composable(
                    "${Routes.ADMIN_BLOOD_REQUESTS}?status={status}",
                    arguments = listOf(navArgument("status") { type = NavType.StringType; nullable = true; defaultValue = null }),
                ) { backStackEntry ->
                    AdminBloodRequestsScreen(
                        onBack = { navController.popBackStack() },
                        initialStatus = backStackEntry.arguments?.getString("status"),
                    )
                }
                composable(
                    "${Routes.ADMIN_MATCHES}?response={response}",
                    arguments = listOf(navArgument("response") { type = NavType.StringType; nullable = true; defaultValue = null }),
                ) { backStackEntry ->
                    AdminMatchesScreen(
                        onBack = { navController.popBackStack() },
                        initialResponse = backStackEntry.arguments?.getString("response"),
                    )
                }
                composable(Routes.ADMIN_AUDIT_LOGS) {
                    AdminAuditLogsScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_SETTINGS) {
                    AdminSettingsScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() }, vm = authVm)
                }
                composable(Routes.ADMIN_ELIGIBILITY_QUESTIONS) {
                    AdminEligibilityQuestionsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_SYSTEM_SETTINGS) {
                    AdminSystemSettingsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ABOUT) {
                    val authState by authVm.uiState.collectAsStateWithLifecycle()
                    AboutScreen(
                        onBack = { navController.popBackStack() },
                        isAdmin = authState.user?.role == com.bloodnetwork.bangladesh.data.model.UserRole.Admin,
                    )
                }
            }
        }
    }
}
