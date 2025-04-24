package org.yangdai.kori.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.yangdai.kori.domain.repository.DataStoreRepository

class DataStoreRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataStoreRepository {
    override suspend fun putString(key: String, value: String) = withContext(ioDispatcher) {
        val preferencesKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putInt(key: String, value: Int) = withContext(ioDispatcher) {
        val preferencesKey = intPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putFloat(key: String, value: Float) = withContext(ioDispatcher) {
        val preferencesKey = floatPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) = withContext(ioDispatcher) {
        val preferencesKey = booleanPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putStringSet(key: String, value: Set<String>) = withContext(ioDispatcher) {
        val preferencesKey = stringSetPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override fun intFlow(key: String): Flow<Int> {
        val preferencesKey = intPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: 0
        }.flowOn(ioDispatcher)
    }

    override fun stringFlow(key: String): Flow<String> {
        val preferencesKey = stringPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: ""
        }.flowOn(ioDispatcher)
    }

    override fun floatFlow(key: String): Flow<Float> {
        val preferencesKey = floatPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: 1f
        }.flowOn(ioDispatcher)
    }

    override fun booleanFlow(key: String): Flow<Boolean> {
        val preferencesKey = booleanPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] == true
        }.flowOn(ioDispatcher)
    }

    override fun stringSetFlow(key: String): Flow<Set<String>> {
        val preferencesKey = stringSetPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: emptySet()
        }.flowOn(ioDispatcher)
    }

    override fun getString(key: String, defaultValue: String): String {
        val preferencesKey = stringPreferencesKey(key)
        return runBlocking(ioDispatcher) {
            dataStore.data.map { preferences ->
                preferences[preferencesKey] ?: defaultValue
            }.first()
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val preferencesKey = booleanPreferencesKey(key)
        return runBlocking(ioDispatcher) {
            dataStore.data.map { preferences ->
                preferences[preferencesKey] ?: defaultValue
            }.first()
        }
    }
}
