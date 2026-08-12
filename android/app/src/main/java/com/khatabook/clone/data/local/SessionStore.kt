package com.khatabook.clone.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.khatabook.clone.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "khatabook_session")

/** Auth token, chosen language and the API host all survive process death here. */
class SessionStore(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val PHONE = stringPreferencesKey("phone")
        val NAME = stringPreferencesKey("name")
        val BUSINESS = stringPreferencesKey("business_name")
        val LANGUAGE = stringPreferencesKey("language")
        val BASE_URL = stringPreferencesKey("base_url")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }
    val language: Flow<String?> = context.dataStore.data.map { it[Keys.LANGUAGE] }
    val baseUrl: Flow<String> = context.dataStore.data.map { it[Keys.BASE_URL] ?: BuildConfig.DEFAULT_BASE_URL }

    val profile: Flow<StoredProfile> = context.dataStore.data.map {
        StoredProfile(
            phone = it[Keys.PHONE],
            name = it[Keys.NAME],
            businessName = it[Keys.BUSINESS],
        )
    }

    suspend fun saveToken(token: String) = edit { it[Keys.TOKEN] = token }

    suspend fun saveLanguage(code: String) = edit { it[Keys.LANGUAGE] = code }

    suspend fun saveBaseUrl(url: String) = edit { it[Keys.BASE_URL] = normalizeUrl(url) }

    suspend fun saveProfile(phone: String?, name: String?, businessName: String?) = edit { prefs ->
        phone?.let { prefs[Keys.PHONE] = it }
        name?.let { prefs[Keys.NAME] = it }
        businessName?.let { prefs[Keys.BUSINESS] = it }
    }

    /** Logout keeps the API host so the next login does not have to set it again. */
    suspend fun clear() = edit { prefs ->
        val url = prefs[Keys.BASE_URL]
        val language = prefs[Keys.LANGUAGE]
        prefs.clear()
        url?.let { prefs[Keys.BASE_URL] = it }
        language?.let { prefs[Keys.LANGUAGE] = it }
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }
}

data class StoredProfile(
    val phone: String? = null,
    val name: String? = null,
    val businessName: String? = null,
)
