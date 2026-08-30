package com.bloodnetwork.bangladesh

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.bloodnetwork.bangladesh.notifications.PushMessagingService
import com.bloodnetwork.bangladesh.ui.AppRoot
import com.bloodnetwork.bangladesh.ui.LocalLanguageStore
import com.bloodnetwork.bangladesh.ui.LocalThemeStore
import com.bloodnetwork.bangladesh.ui.i18n.LocalAppLanguage
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.theme.BloodNetworkTheme

class MainActivity : ComponentActivity() {
    private val container by lazy { (application as BloodNetworkApp).container }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deepLinkRoute = if (intent?.action == PushMessagingService.ACTION_OPEN_NOTIFICATIONS) {
            Routes.NOTIFICATIONS
        } else {
            null
        }

        setContent {
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
                        AppRoot(repository = container.repository, initialDeepLink = deepLinkRoute)
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
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
        }
    }
}