package org.yangdai.kori.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    suspend fun putString(key: String, value: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun putFloat(key: String, value: Float)
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun putStringSet(key: String, value: Set<String>)

    fun intFlow(key: String): Flow<Int>
    fun stringFlow(key: String): Flow<String>
    fun floatFlow(key: String): Flow<Float>
    fun booleanFlow(key: String): Flow<Boolean>
    fun stringSetFlow(key: String): Flow<Set<String>>

    fun getString(key: String, defaultValue: String): String
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getInt(key: String, defaultValue: Int): Int
}