package org.yangdai.kori.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    suspend fun putString(key: String, value: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun putFloat(key: String, value: Float)
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun putStringSet(key: String, value: Set<String>)

    fun intFlow(key: String, defaultValue: Int = 0): Flow<Int>
    fun stringFlow(key: String, defaultValue: String = ""): Flow<String>
    fun floatFlow(key: String, defaultValue: Float = 1f): Flow<Float>
    fun booleanFlow(key: String, defaultValue: Boolean = false): Flow<Boolean>
    fun stringSetFlow(key: String, defaultValue: Set<String> = emptySet()): Flow<Set<String>>

    fun getString(key: String, defaultValue: String = ""): String
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun getInt(key: String, defaultValue: Int): Int
    fun getFloat(key: String, defaultValue: Float): Float
}