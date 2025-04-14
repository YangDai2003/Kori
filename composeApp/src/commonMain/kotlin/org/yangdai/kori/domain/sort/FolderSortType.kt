package org.yangdai.kori.domain.sort

/**
 * 文件夹排序字段
 */
enum class FolderSortType(val value: Int) {
    CREATE_TIME_DESC(0),
    CREATE_TIME_ASC(1),
    NAME_DESC(2),
    NAME_ASC(3);

    companion object {
        val entries = FolderSortType.entries

        fun fromValue(value: Int): FolderSortType {
            return entries.firstOrNull { it.value == value } ?: CREATE_TIME_DESC
        }
    }
}