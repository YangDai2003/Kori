package org.yangdai.kori

import android.app.Application
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import org.koin.android.ext.koin.androidContext
import org.yangdai.kori.data.di.KoinInitializer
import org.yangdai.kori.presentation.util.Constants

class KoriApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize your app here
        KoinInitializer.init {
            androidContext(this@KoriApp)
            // Add other Koin modules here
        }

        val shortcut = ShortcutInfoCompat.Builder(applicationContext, "id1")
            .setShortLabel(getString(R.string.compose))
            .setLongLabel(getString(R.string.compose))
            .setIcon(IconCompat.createWithResource(applicationContext, R.drawable.compose))
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.DEEP_LINK}/note").toUri()
                }
            )
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcut)
    }
}