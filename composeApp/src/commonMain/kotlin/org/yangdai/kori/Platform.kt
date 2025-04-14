package org.yangdai.kori

sealed class Platform {
    abstract val name: String

    // 定义所有可能的平台类型
    object Android : Platform() {
        override val name: String = "Android"
    }

    object IOS : Platform() {
        override val name: String = "iOS"
    }

    object Desktop : Platform() {
        override val name: String = "Desktop"
    }

    object Web : Platform() {
        override val name: String = "Web"
    }
}

expect fun currentPlatform(): Platform