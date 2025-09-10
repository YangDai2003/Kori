package org.yangdai.kori.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.yangdai.kori.data.local.createDataStore
import org.yangdai.kori.data.local.dataStoreFileName
import org.yangdai.kori.koriDirPath
import java.nio.file.Files

fun createDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        if (!Files.exists(koriDirPath)) Files.createDirectories(koriDirPath)
        koriDirPath.resolve(dataStoreFileName).toAbsolutePath().toString()
    }
)