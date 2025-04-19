package org.yangdai.kori.domain.repository

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    suspend fun putString(key: String, value: String): Preferences
    suspend fun putInt(key: String, value: Int): Preferences
    suspend fun putFloat(key: String, value: Float): Preferences
    suspend fun putBoolean(key: String, value: Boolean): Preferences
    suspend fun putStringSet(key: String, value: Set<String>): Preferences

    fun intFlow(key: String): Flow<Int>
    fun stringFlow(key: String): Flow<String>
    fun floatFlow(key: String): Flow<Float>
    fun booleanFlow(key: String): Flow<Boolean>
    fun stringSetFlow(key: String): Flow<Set<String>>

    fun getString(key: String, defaultValue: String): String
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
}