package org.yangdai.kori

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import kotlin.apply
import kotlin.jvm.java

class ManageSpaceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val intent = Intent(this, MainActivity::class.java).apply {
//            Intent.setFlags = Intent.FLAG_ACTIVITY_NEW_TASK or      // 在新任务中启动
//                    Intent.FLAG_ACTIVITY_CLEAR_TASK       // 清除所有已存在的任务
//            Intent.setData = "$LINK/settings".toUri()
//        }
//        startActivity(intent)
        finish()
    }
}
