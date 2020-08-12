package com.sahara.plugin.jetbrains.icons

import com.intellij.ui.IconManager
import javax.swing.Icon

/**
 * @author liao
 * Create on 2020/8/11 23:30
 */
object DependencyUpdaterIcons {
    private fun load(path: String): Icon {
        return IconManager.getInstance().getIcon(path, DependencyUpdaterIcons::class.java)
    }

    val DependencyUpdaterLogo = load("/icons/updater.svg")
}
