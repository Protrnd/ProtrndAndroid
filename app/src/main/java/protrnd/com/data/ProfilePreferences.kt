package protrnd.com.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    companion object {
        private val Context.DATASTORE by preferencesDataStore("Protrnd_Store")
        private val KEY_AUTH = stringPreferencesKey("jwt_auth")
        private val PROFILE_KEY = stringPreferencesKey("profile_key")
    }
}