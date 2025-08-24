package org.yangdai.kori.presentation.screen.settings

data class DataActionState(
    val progress: Float = Float.MIN_VALUE, // MIN_VALUE 表示未开始初始状态，0f-1f 表示进度，1f 表示完成
    val infinite: Boolean = false, // 是否显示百分比进度
    val message: String = ""
)
