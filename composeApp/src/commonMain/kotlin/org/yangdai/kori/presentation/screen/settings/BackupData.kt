package org.yangdai.kori.presentation.screen.settings

import kotlinx.serialization.Serializable
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.data.local.entity.NoteEntity

@Serializable
data class BackupData(
    val notes: List<NoteEntity>,
    val folders: List<FolderEntity>
)

private const val KEY = "OpenNoteBackupKey@COPYRIGHT-YANGSCODEHUB"

/**
 * Attempts to de-obfuscate the data if it's obfuscated, otherwise returns it as-is
 * @param data The potentially obfuscated data
 * @return Original JSON string
 */
fun decryptBackupDataWithCompatibility(data: String): String {
    // Check if data is obfuscated by looking for the marker
    if (!data.startsWith("ENC:")) {
        return data // Not obfuscated, return as-is for backward compatibility
    }

    // Remove marker
    val obfuscatedText = data.substring(4)

    // De-obfuscate using XOR with the same key
    return obfuscateWithXOR(obfuscatedText)
}

/**
 * XOR-based obfuscation/de-obfuscation function
 * Same function works for both encryption and decryption due to XOR properties
 */
private fun obfuscateWithXOR(input: String): String {
    val result = StringBuilder()
    val inputChars = input.toCharArray()
    val keyChars = KEY.toCharArray()

    for (i in inputChars.indices) {
        val keyChar = keyChars[i % keyChars.size]
        val obfuscatedChar = inputChars[i].code xor keyChar.code
        result.append(obfuscatedChar.toChar())
    }

    return result.toString()
}

@Serializable
data class ONoteEntity(
    val id: Long? = null,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val isMarkdown: Boolean = true,
    val isDeleted: Boolean = false,
    val timestamp: Long
)

@Serializable
data class OFolderEntity(
    val id: Long? = null,
    val name: String = "",
    val color: Int? = null
)

@Serializable
data class OpenNoteBackupData(
    val notes: List<ONoteEntity>,
    val folders: List<OFolderEntity>
)