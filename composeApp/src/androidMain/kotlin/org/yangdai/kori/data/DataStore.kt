package org.yangdai.kori.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.yangdai.kori.data.local.dataStoreFileName

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = dataStoreFileName)