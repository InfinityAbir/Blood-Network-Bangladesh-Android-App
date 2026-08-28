package com.bloodnetwork.bangladesh.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.donorProfileDataStore by preferencesDataStore(name = "donor_profile")

class DonorProfileStore(private val context: Context) {

    private object Keys {
        val BLOOD_GROUP = stringPreferencesKey("blood_group")
        val GENDER = stringPreferencesKey("gender")
        val DATE_OF_BIRTH = stringPreferencesKey("date_of_birth")
        val DIVISION_ID = stringPreferencesKey("division_id")
        val DIVISION_NAME = stringPreferencesKey("division_name")
        val DISTRICT_ID = stringPreferencesKey("district_id")
        val DISTRICT_NAME = stringPreferencesKey("district_name")
        val UPAZILA_ID = stringPreferencesKey("upazila_id")
        val UPAZILA_NAME = stringPreferencesKey("upazila_name")
        val AREA = stringPreferencesKey("area")
        val LAST_DONATION_DATE = stringPreferencesKey("last_donation_date")
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
        val lastDonationDate: String = "",
    )

    val data: Flow<DonorProfileData> = context.donorProfileDataStore.data.map { prefs ->
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
            lastDonationDate = prefs[Keys.LAST_DONATION_DATE] ?: "",
        )
    }

    suspend fun save(data: DonorProfileData) {
        context.donorProfileDataStore.edit { prefs ->
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
            prefs[Keys.LAST_DONATION_DATE] = data.lastDonationDate
        }
    }

    suspend fun clear() {
        context.donorProfileDataStore.edit { it.clear() }
    }
}
