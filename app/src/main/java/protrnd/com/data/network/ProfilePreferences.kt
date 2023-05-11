package protrnd.com.data.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import protrnd.com.data.models.CardData
import protrnd.com.data.models.Profile

class ProfilePreferences(context: Context) {
    private val dataStore: DataStore<Preferences> = context.DATASTORE

    val authToken: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH]
        }

    val profile: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[PROFILE_KEY]
        }

    val pin: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[PAYMENT_PIN]
        }

    val cardData: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[CARD_DETAILS]
        }

    suspend fun savePaymentPin(pin: String) {
        dataStore.edit { pref ->
            pref[PAYMENT_PIN] = pin
        }
    }

    suspend fun saveAuthToken(authToken: String) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTH] = authToken
        }
    }

    suspend fun saveProfile(profile: Profile) {
        dataStore.edit { preferences ->
            val profileJson = Gson().toJson(profile)
            preferences[PROFILE_KEY] = profileJson
        }
    }

    suspend fun saveCardDetails(cardData: CardData) {
        dataStore.edit { preferences ->
            val cardDataJson = Gson().toJson(cardData)
            preferences[CARD_DETAILS] = cardDataJson
        }
    }

    suspend fun logoutProfile() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH)
            preferences.remove(PROFILE_KEY)
            preferences.remove(PAYMENT_PIN)
            preferences.remove(CARD_DETAILS)
        }
    }

    companion object {
        private val Context.DATASTORE by preferencesDataStore("Protrnd_Store")
        private val KEY_AUTH = stringPreferencesKey("jwt_auth")
        private val PROFILE_KEY = stringPreferencesKey("profile_key")
        private val PAYMENT_PIN = stringPreferencesKey("protrnd_payment_key")
        private val CARD_DETAILS = stringPreferencesKey("card_details_key")
    }
}