package org.yangdai.kori.data.local

import androidx.room.TypeConverter
import org.yangdai.kori.data.local.entity.NoteType

class DatabaseConverters {
    @TypeConverter
    fun fromNoteType(noteType: NoteType): String {
        return noteType.name
    }

    @TypeConverter
    fun toNoteType(value: String): NoteType {
        return try {
            NoteType.valueOf(value)
        } catch (_: Exception) {
            NoteType.PLAIN_TEXT
        }
    }
}