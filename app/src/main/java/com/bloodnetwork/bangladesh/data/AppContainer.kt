package com.bloodnetwork.bangladesh.data

import android.content.Context
import com.bloodnetwork.bangladesh.data.network.ApiClient
import com.bloodnetwork.bangladesh.data.network.AuthInterceptor
import com.bloodnetwork.bangladesh.data.network.BloodNetworkApi
import com.bloodnetwork.bangladesh.data.prefs.TokenStore

/**
 * Lightweight manual dependency container. Avoids Hilt/KSP for a simpler,
 * more robust build. Add new dependencies here and expose them to ViewModels.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val tokenStore: TokenStore by lazy { TokenStore(appContext) }

    val authInterceptor: AuthInterceptor by lazy { AuthInterceptor(tokenStore) }

    private val baseUrl: String =
        appContext.getString(com.bloodnetwork.bangladesh.R.string.base_api_url)

    val api: BloodNetworkApi by lazy {
        ApiClient.create(baseUrl, authInterceptor, tokenStore)
    }

    val repository: BloodNetworkRepository by lazy {
        BloodNetworkRepository(api, tokenStore)
    }
}
