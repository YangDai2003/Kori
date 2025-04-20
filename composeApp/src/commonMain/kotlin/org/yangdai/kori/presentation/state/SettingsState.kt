package org.yangdai.kori.presentation.state

data class StylePaneState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val color: AppColor = AppColor.DYNAMIC,
    val isAppInAmoledMode: Boolean = false
)

data class SecurityPaneState(
    val isScreenProtected: Boolean = false,
    val password: String = "",
    val isCreatingPass: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

enum class AppTheme(private val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: SYSTEM
        fun AppTheme.toInt() = value
    }
}

enum class AppColor(private val value: Int) {
    DYNAMIC(0),
    PURPLE(1),
    BLUE(2),
    GREEN(3),
    ORANGE(4),
    RED(5);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: DYNAMIC
        fun AppColor.toInt() = value
    }
}