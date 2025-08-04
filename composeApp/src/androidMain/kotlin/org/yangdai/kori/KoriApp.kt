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
        KoinInitializer.init {
            androidContext(this@KoriApp)
        }

        // Note shortcut
        val noteShortcut = ShortcutInfoCompat.Builder(applicationContext, "id1")
            .setShortLabel(getString(R.string.compose))
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext,
                    R.drawable.outline_note_add_24
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.DEEP_LINK}/note").toUri()
                }
            )
            .build()

        // Settings shortcut
        val settingsShortcut = ShortcutInfoCompat.Builder(applicationContext, "id_settings")
            .setShortLabel(getString(R.string.settings))
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext,
                    R.drawable.outline_settings_24
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.DEEP_LINK}/settings").toUri()
                }
            )
            .build()

        // Folders shortcut
        val foldersShortcut = ShortcutInfoCompat.Builder(applicationContext, "id_folders")
            .setShortLabel(getString(R.string.folders))
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext,
                    R.drawable.outline_folders_24
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.DEEP_LINK}/folders").toUri()
                }
            )
            .build()

        // Template shortcut
        val templateShortcut = ShortcutInfoCompat.Builder(applicationContext, "id_template")
            .setShortLabel(getString(R.string.template))
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext,
                    R.drawable.outline_add_template_24
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.DEEP_LINK}/template").toUri()
                }
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, noteShortcut)
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, templateShortcut)
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, settingsShortcut)
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, foldersShortcut)
    }
}