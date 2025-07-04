package org.yangdai.kori.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.yangdai.kori.presentation.theme.Blue
import org.yangdai.kori.presentation.theme.Cyan
import org.yangdai.kori.presentation.theme.Green
import org.yangdai.kori.presentation.theme.Orange
import org.yangdai.kori.presentation.theme.Purple
import org.yangdai.kori.presentation.theme.Red
import org.yangdai.kori.presentation.theme.Yellow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
@Entity(
    tableName = "folders",
    indices = [
        Index(value = ["name"]), // 为 name 添加索引
        Index(value = ["created_at"]) // 为 created_at 添加索引
    ]
)
data class FolderEntity @OptIn(ExperimentalTime::class) constructor(
    @PrimaryKey
    val id: String = "", // 使用 UUID 字符串表示
    @ColumnInfo(name = "name")
    val name: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: String = Clock.System.now().toString(),
    @ColumnInfo(name = "color_value")
    val colorValue: Long = defaultFolderColor, // 存储颜色的 Long 值, 默认为 Color.Unspecified.packedValue
    @ColumnInfo(name = "is_starred")
    val isStarred: Boolean = false // 是否被星标，默认为 false
)

val defaultFolderColor = (0x10UL).toLong() // 默认颜色值
val folderColorOptions = listOf(
    Red,
    Orange,
    Yellow,
    Green,
    Cyan,
    Blue,
    Purple
)