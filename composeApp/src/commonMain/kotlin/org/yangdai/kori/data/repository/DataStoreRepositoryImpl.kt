package org.yangdai.kori.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.yangdai.kori.domain.repository.DataStoreRepository

class DataStoreRepositoryImpl(private val dataStore: DataStore<Preferences>) : DataStoreRepository {
    override suspend fun putString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putFloat(key: String, value: Float) {
        val preferencesKey = floatPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putStringSet(key: String, value: Set<String>) {
        val preferencesKey = stringSetPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override fun intFlow(key: String, defaultValue: Int): Flow<Int> {
        val preferencesKey = intPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: defaultValue
        }
    }

    override fun stringFlow(key: String, defaultValue: String): Flow<String> {
        val preferencesKey = stringPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: defaultValue
        }
    }

    override fun floatFlow(key: String, defaultValue: Float): Flow<Float> {
        val preferencesKey = floatPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: defaultValue
        }
    }

    override fun booleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        val preferencesKey = booleanPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: defaultValue
        }
    }

    override fun stringSetFlow(key: String, defaultValue: Set<String>): Flow<Set<String>> {
        val preferencesKey = stringSetPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: defaultValue
        }
    }

    override fun getString(key: String, defaultValue: String): String {
        val preferencesKey = stringPreferencesKey(key)
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[preferencesKey] ?: defaultValue
            }.first()
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val preferencesKey = booleanPreferencesKey(key)
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[preferencesKey] ?: defaultValue
            }.first()
        }
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        val preferencesKey = intPreferencesKey(key)
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[preferencesKey] ?: defaultValue
            }.first()
        }
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        val preferencesKey = floatPreferencesKey(key)
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[preferencesKey] ?: defaultValue
            }.first()
        }
    }
}
