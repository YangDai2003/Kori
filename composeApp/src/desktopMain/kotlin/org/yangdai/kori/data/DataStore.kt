package org.yangdai.kori.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.yangdai.kori.data.local.createDataStore
import org.yangdai.kori.data.local.dataStoreFileName
import java.io.File

fun createDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val dsFile = File(System.getProperty("java.io.tmpdir"), dataStoreFileName)
        dsFile.absolutePath
    }
)