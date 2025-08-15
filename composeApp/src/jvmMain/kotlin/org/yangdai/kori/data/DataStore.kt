package org.yangdai.kori.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.yangdai.kori.data.local.createDataStore
import org.yangdai.kori.data.local.dataStoreFileName
import java.io.File

fun createDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val userHome: String = System.getProperty("user.home")
        val koriDir = File(userHome, ".kori")
        if (!koriDir.exists()) koriDir.mkdirs()
        val dsFile = File(koriDir, dataStoreFileName)
        dsFile.absolutePath
    }
)