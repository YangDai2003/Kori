package org.yangdai.kori.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.yangdai.kori.data.local.createDataStore
import org.yangdai.kori.data.local.dataStoreFileName
import java.nio.file.Files
import java.nio.file.Paths

fun createDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val userHome: String = System.getProperty("user.home")
        val koriDirPath = Paths.get(userHome, ".kori")
        if (!Files.exists(koriDirPath)) Files.createDirectories(koriDirPath)
        koriDirPath.resolve(dataStoreFileName).toAbsolutePath().toString()
    }
)