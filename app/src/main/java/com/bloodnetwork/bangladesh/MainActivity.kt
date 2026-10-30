package com.bloodnetwork.bangladesh

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bloodnetwork.bangladesh.notifications.NotificationRouting
import com.bloodnetwork.bangladesh.notifications.PushMessagingService
import com.bloodnetwork.bangladesh.ui.AppRoot
import com.bloodnetwork.bangladesh.ui.LocalLanguageStore
import com.bloodnetwork.bangladesh.ui.LocalThemeStore
import com.bloodnetwork.bangladesh.ui.i18n.LocalAppLanguage
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.theme.BloodNetworkTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val container by lazy { (application as BloodNetworkApp).container }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    /** Screen requested by a tapped push notification; consumed (reset to null) once navigated to. */
    private val pendingDeepLink = MutableStateFlow<String?>(null)

    /**
     * Only ever derives a route from the notification's type + id, never from a route string in
     * the intent: this activity is exported, so any app can send it an intent.
     */
    private fun deepLinkFrom(intent: Intent?): String? =
        if (intent?.action == PushMessagingService.ACTION_OPEN_NOTIFICATIONS) {
            NotificationRouting.routeFor(
                intent.getStringExtra(PushMessagingService.EXTRA_TYPE),
                intent.getStringExtra(PushMessagingService.EXTRA_RELATED_ID),
            )
        } else {
            null
        }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // The app was already running when the notification was tapped: without this the tap
        // only brought the app forward and never opened the target screen.
        setIntent(intent)
        deepLinkFrom(intent)?.let { pendingDeepLink.value = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Not restored from a saved state: a recreated activity (rotation) must not replay the link.
        if (savedInstanceState == null) pendingDeepLink.value = deepLinkFrom(intent)

        setContent {
            val deepLink by pendingDeepLink.collectAsStateWithLifecycle()
            val themeMode by container.themeStore.mode.collectAsStateWithLifecycle()
            val appLanguage by container.languageStore.language.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalThemeStore provides container.themeStore,
                LocalLanguageStore provides container.languageStore,
                LocalAppLanguage provides appLanguage,
            ) {
                BloodNetworkTheme(themeMode = themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AppRoot(
                            repository = container.repository,
                            initialDeepLink = deepLink,
                            onDeepLinkConsumed = { pendingDeepLink.value = null },
                        )
                    }
                }
            }

            // Ask for notification permission only once the user is signed in and so has a
            // reason to want match alerts — a permission prompt on the very first screen,
            // before the app has shown any value, is mostly refused.
            val signedIn by container.tokenStore.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
            if (signedIn &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                LaunchedEffect(Unit) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // This client has no built-in automatic reconnect, so re-poke on every foreground —
        // a harmless no-op if already connected, a fresh connection attempt otherwise (e.g.
        // after the app sat backgrounded long enough for Android to kill the socket).
        if (container.tokenStore.isLoggedInSync()) {
            container.notificationSocket.start()
            // Same self-heal as the socket above: re-upsert the FCM token on every
            // foreground so a registration that silently failed at cold start (e.g. a
            // transient DNS blip before the network stack is ready) or a backend token
            // that got dropped isn't stuck until the next logout/login.
            lifecycleScope.launch { container.repository.registerFcmIfNeeded() }
        }
    }
}