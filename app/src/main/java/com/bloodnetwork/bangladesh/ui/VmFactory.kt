package com.bloodnetwork.bangladesh.ui

import com.bloodnetwork.bangladesh.data.BloodNetworkRepository

/**
 * Creates a ViewModelProvider.Factory that injects the shared repository
 * into any ViewModel whose constructor takes a [BloodNetworkRepository].
 * Used in Compose via viewModel(factory = ...).
 */
class VmFactory(
    private val repository: BloodNetworkRepository,
) : androidx.lifecycle.ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        val ctor = modelClass.getConstructor(BloodNetworkRepository::class.java)
        return ctor.newInstance(repository) as T
    }
}
