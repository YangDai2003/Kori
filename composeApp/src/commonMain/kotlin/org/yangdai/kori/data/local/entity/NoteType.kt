package org.yangdai.kori.data.local.entity

import kotlinx.serialization.Serializable

@Serializable
enum class NoteType {
    PLAIN_TEXT,
    MARKDOWN,
    TODO,
    Drawing
    // 未来可以在这里添加更多类型
}