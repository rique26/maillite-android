package com.rique.maillite.core.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "maillite_session")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val TOKEN = stringPreferencesKey("jwt_token")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences -> preferences[Keys.TOKEN] }

    suspend fun getToken(): String? = tokenFlow.first()

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences -> preferences[Keys.TOKEN] = token }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences -> preferences.remove(Keys.TOKEN) }
    }
}