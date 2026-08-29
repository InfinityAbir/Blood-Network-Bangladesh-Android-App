package com.bloodnetwork.bangladesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodnetwork.bangladesh.ui.AppRoot
import com.bloodnetwork.bangladesh.ui.LocalThemeStore
import com.bloodnetwork.bangladesh.ui.theme.BloodNetworkTheme

class MainActivity : ComponentActivity() {
    private val container by lazy { (application as BloodNetworkApp).container }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by container.themeStore.mode.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalThemeStore provides container.themeStore) {
                BloodNetworkTheme(themeMode = themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AppRoot(repository = container.repository)
                    }
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
