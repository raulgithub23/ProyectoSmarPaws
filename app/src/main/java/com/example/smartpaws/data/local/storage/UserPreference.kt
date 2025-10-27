package com.example.smartpaws.data.local.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {

    //almacenar el ID del usuario (un Long)
    private val LOGGED_IN_USER_ID_KEY = longPreferencesKey("logged_in_user_id")

    // Flow para observar el ID. Emite null si no hay nadie logueado.
    val loggedInUserId: Flow<Long?> = context.dataStore.data
        .map { prefs ->
            prefs[LOGGED_IN_USER_ID_KEY]
        }

    // guardar el ID al iniciar sesión
    suspend fun saveUserId(userId: Long) {
        context.dataStore.edit { prefs ->
            prefs[LOGGED_IN_USER_ID_KEY] = userId
        }
    }

    // limpiar el ID al cerrar sesión
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(LOGGED_IN_USER_ID_KEY)
        }
    }
}