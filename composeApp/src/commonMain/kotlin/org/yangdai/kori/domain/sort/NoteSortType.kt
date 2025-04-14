package org.yangdai.kori.domain.sort

/**
 * 笔记排序字段
 */
enum class NoteSortType(val value: Int) {
    UPDATE_TIME_DESC(0),
    UPDATE_TIME_ASC(1),
    CREATE_TIME_DESC(2),
    CREATE_TIME_ASC(3),
    NAME_DESC(4),
    NAME_ASC(5);

    companion object {
        val entries = NoteSortType.entries

        fun fromValue(value: Int): NoteSortType {
            return entries.firstOrNull { it.value == value } ?: UPDATE_TIME_DESC
        }
    }
}