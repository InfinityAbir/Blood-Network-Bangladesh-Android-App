package com.bloodnetwork.bangladesh.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bloodnetwork.bangladesh.data.BloodNetworkRepository
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.screens.ChatbotScreen
import com.bloodnetwork.bangladesh.ui.screens.DonorDashboardScreen
import com.bloodnetwork.bangladesh.ui.screens.DonorProfileScreen
import com.bloodnetwork.bangladesh.ui.screens.AboutScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminAnalyticsScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminDashboardScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminUserManagementScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminReportsScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminAuditLogsScreen
import com.bloodnetwork.bangladesh.ui.screens.AdminSettingsScreen
import com.bloodnetwork.bangladesh.ui.screens.EditProfileScreen
import com.bloodnetwork.bangladesh.ui.screens.EligibilityScreen
import com.bloodnetwork.bangladesh.ui.screens.FindBloodScreen
import com.bloodnetwork.bangladesh.ui.screens.LandingScreen
import com.bloodnetwork.bangladesh.ui.screens.NotificationsScreen
import com.bloodnetwork.bangladesh.ui.screens.RequestBloodScreen
import com.bloodnetwork.bangladesh.ui.screens.auth.LoginScreen
import com.bloodnetwork.bangladesh.ui.screens.auth.RegisterScreen
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.ChatbotViewModel

private const val ANIM_DURATION = 300

@Composable
fun AppRoot(repository: BloodNetworkRepository) {
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
    val showChatFab = currentRoute != Routes.CHATBOT

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
                modifier = androidx.compose.ui.Modifier.padding(innerPadding),
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
                            val dest = if (user?.role == com.bloodnetwork.bangladesh.data.model.UserRole.Admin) Routes.ADMIN_DASHBOARD else Routes.DONOR_DASHBOARD
                            navController.navigate(dest) { popUpTo(Routes.LANDING) { inclusive = true } }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onNavigate = navController::navigate,
                        vm = authVm,
                        onRegistered = {
                            val user = authVm.uiState.value.user
                            val dest = if (user?.role == com.bloodnetwork.bangladesh.data.model.UserRole.Admin) Routes.ADMIN_DASHBOARD else Routes.DONOR_DASHBOARD
                            navController.navigate(dest) { popUpTo(Routes.LANDING) { inclusive = true } }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.FIND_BLOOD) {
                    FindBloodScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
                }
                composable(Routes.REQUEST_BLOOD) {
                    RequestBloodScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
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
                    ChatbotScreen(onBack = { navController.popBackStack() }, vm = vm)
                }
                composable(Routes.EDIT_PROFILE) {
                    EditProfileScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() }, authVm = authVm)
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
                composable(Routes.ADMIN_USERS) {
                    AdminUserManagementScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_REPORTS) {
                    AdminReportsScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_AUDIT_LOGS) {
                    AdminAuditLogsScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_SETTINGS) {
                    AdminSettingsScreen(onNavigate = navController::navigate, onBack = { navController.popBackStack() }, vm = authVm)
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
