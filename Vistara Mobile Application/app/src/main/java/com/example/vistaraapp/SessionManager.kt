package com.example.vistaraapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// DataStore setup
private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class SessionManager(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val ROLE_KEY = stringPreferencesKey("user_role")
    }

    // SAVE TOKEN
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    // GET TOKEN
    suspend fun getToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[TOKEN_KEY]
    }

    // SAVE ROLE
    suspend fun saveRole(role: String) {
        context.dataStore.edit { prefs ->
            prefs[ROLE_KEY] = role
        }
    }

    // GET ROLE
    suspend fun getRole(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[ROLE_KEY]
    }

    // CLEAR SESSION (LOGOUT)
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}