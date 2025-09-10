package org.yangdai.kori

import androidx.compose.ui.awt.ComposeWindow
import org.jetbrains.skiko.SkiaLayer
import java.awt.Container
import javax.swing.JComponent

private fun <T : JComponent> Container.findComponent(targetClass: Class<T>): T? {
    for (component in components) {
        if (targetClass.isInstance(component)) @Suppress("UNCHECKED_CAST") return component as T
        if (component is Container) {
            val foundInChild = component.findComponent(targetClass)
            if (foundInChild != null) return foundInChild
        }
    }
    return null
}

private inline fun <reified T : JComponent> Container.findComponent() = findComponent(T::class.java)

fun ComposeWindow.findSkiaLayer(): SkiaLayer? = findComponent<SkiaLayer>()