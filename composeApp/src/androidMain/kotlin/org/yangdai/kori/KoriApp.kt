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
        val noteShortcut = ShortcutInfoCompat.Builder(applicationContext, "id_plain_text")
            .setShortLabel(getString(R.string.new_plain_text))
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext,
                    R.drawable.new_text_24px
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.DEEP_LINK}/note?noteType=0").toUri()
                }
            )
            .build()

        val noteMarkdownShortcut = ShortcutInfoCompat.Builder(applicationContext, "id_markdown")
            .setShortLabel(getString(R.string.new_markdown))
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext,
                    R.drawable.new_markdown_24px
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.DEEP_LINK}/note?noteType=1").toUri()
                }
            )
            .build()

        val noteTodoShortcut = ShortcutInfoCompat.Builder(applicationContext, "id_todo")
            .setShortLabel(getString(R.string.new_todo))
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext,
                    R.drawable.new_todo_24px
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.DEEP_LINK}/note?noteType=2").toUri()
                }
            )
            .build()

        val noteDrawingShortcut = ShortcutInfoCompat.Builder(applicationContext, "id_drawing")
            .setShortLabel(getString(R.string.new_drawing))
            .setIcon(
                IconCompat.createWithResource(
                    applicationContext,
                    R.drawable.new_draw_24px
                )
            )
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.DEEP_LINK}/note?noteType=3").toUri()
                }
            )
            .build()

        ShortcutManagerCompat.removeAllDynamicShortcuts(applicationContext)
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, noteShortcut)
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, noteMarkdownShortcut)
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, noteTodoShortcut)
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, noteDrawingShortcut)
    }
}