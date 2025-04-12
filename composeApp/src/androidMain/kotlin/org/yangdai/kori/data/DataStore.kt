package org.yangdai.kori.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.yangdai.kori.data.local.dataStoreFileName
import org.yangdai.kori.data.local.createDataStore

fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)