package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Draft copy of the donor profile form (date of birth, address, last donation date...).
 * Health/personal data, so it lives in Keystore-encrypted storage rather than plain DataStore.
 */
class DonorProfileStore(context: Context) {

    private val store = SecureStringStore(context, "donor_profile_secure", legacyDataStoreName = "donor_profile")

    private object Keys {
        const val BLOOD_GROUP = "blood_group"
        const val GENDER = "gender"
        const val DATE_OF_BIRTH = "date_of_birth"
        const val DIVISION_ID = "division_id"
        const val DIVISION_NAME = "division_name"
        const val DISTRICT_ID = "district_id"
        const val DISTRICT_NAME = "district_name"
        const val UPAZILA_ID = "upazila_id"
        const val UPAZILA_NAME = "upazila_name"
        const val AREA = "area"
        const val CUSTOM_ADDRESS = "custom_address"
        const val LAST_DONATION_DATE = "last_donation_date"
    }

    data class DonorProfileData(
        val bloodGroup: String = "",
        val gender: String = "",
        val dateOfBirth: String = "",
        val divisionId: String = "",
        val divisionName: String = "",
        val districtId: String = "",
        val districtName: String = "",
        val upazilaId: String = "",
        val upazilaName: String = "",
        val area: String = "",
        val customAddress: String = "",
        val lastDonationDate: String = "",
    )

    val data: Flow<DonorProfileData> = store.data.map { prefs ->
        DonorProfileData(
            bloodGroup = prefs[Keys.BLOOD_GROUP] ?: "",
            gender = prefs[Keys.GENDER] ?: "",
            dateOfBirth = prefs[Keys.DATE_OF_BIRTH] ?: "",
            divisionId = prefs[Keys.DIVISION_ID] ?: "",
            divisionName = prefs[Keys.DIVISION_NAME] ?: "",
            districtId = prefs[Keys.DISTRICT_ID] ?: "",
            districtName = prefs[Keys.DISTRICT_NAME] ?: "",
            upazilaId = prefs[Keys.UPAZILA_ID] ?: "",
            upazilaName = prefs[Keys.UPAZILA_NAME] ?: "",
            area = prefs[Keys.AREA] ?: "",
            customAddress = prefs[Keys.CUSTOM_ADDRESS] ?: "",
            lastDonationDate = prefs[Keys.LAST_DONATION_DATE] ?: "",
        )
    }

    suspend fun save(data: DonorProfileData) {
        store.edit { prefs ->
            prefs[Keys.BLOOD_GROUP] = data.bloodGroup
            prefs[Keys.GENDER] = data.gender
            prefs[Keys.DATE_OF_BIRTH] = data.dateOfBirth
            prefs[Keys.DIVISION_ID] = data.divisionId
            prefs[Keys.DIVISION_NAME] = data.divisionName
            prefs[Keys.DISTRICT_ID] = data.districtId
            prefs[Keys.DISTRICT_NAME] = data.districtName
            prefs[Keys.UPAZILA_ID] = data.upazilaId
            prefs[Keys.UPAZILA_NAME] = data.upazilaName
            prefs[Keys.AREA] = data.area
            prefs[Keys.CUSTOM_ADDRESS] = data.customAddress
            prefs[Keys.LAST_DONATION_DATE] = data.lastDonationDate
        }
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }
}
