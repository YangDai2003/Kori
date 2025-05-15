package org.yangdai.kori.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // 优化索引配置以匹配NoteDao中的查询模式
    indices = [
        // --- Indices for Filtering and Sorting Active Notes ---
        // Covers getAllNotesOrderBy* and searchNotesByKeywordOrderBy* (partially)
        // Prioritizes the common filter and the primary sort key (is_pinned)
        // Uses updated_at as the default secondary sort key example
        Index(
            value = ["is_deleted", "is_template", "is_pinned", "updated_at"],
            name = "idx_notes_active_pin_updated"
        ),
        Index(
            value = ["is_deleted", "is_template", "is_pinned", "created_at"],
            name = "idx_notes_active_pin_created"
        ),
        Index(
            value = ["is_deleted", "is_template", "is_pinned", "title"],
            name = "idx_notes_active_pin_title"
        ),

        // --- Index for Foreign Key (often implicitly useful, but good practice) ---
        // While Room manages FK constraints, explicitly indexing the FK column
        // used in joins or frequent lookups (like getNotesByFolderId*) is beneficial.
        // The composite index idx_notes_folder_active_pin_updated already covers this,
        // but a simpler index might be useful if you query *only* by folder_id sometimes.
        Index(value = ["folder_id"], name = "idx_notes_folder_id")
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String = "", // 使用 UUID 字符串作为主键
    val title: String = "",
    val content: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: String = Clock.System.now().toString(), // ISO 8601 format
    @ColumnInfo(name = "updated_at")
    val updatedAt: String = Clock.System.now().toString(),
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "folder_id")
    val folderId: String? = null, // 可空类型，允许笔记不属于文件夹
    @ColumnInfo(name = "note_type")
    val noteType: NoteType = NoteType.PLAIN_TEXT, // 类型有纯文本、Lite Markdown、Standard Markdown等, 默认是未指定
    @ColumnInfo(name = "is_template")
    val isTemplate: Boolean = false, // 判断笔记是否是模板笔记
)
