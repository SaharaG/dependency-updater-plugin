package com.sahara.plugin.jetbrains.action

import com.google.common.collect.Sets
import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.InspectionsBundle
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.codeInspection.ex.InspectionManagerEx
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiDocumentManager

/**
 * @author liao
 * Create on 2020/8/11 10:25
 */
class DependencyUpdaterInspectionAction : AnAction() {
    private val logger = Logger.getInstance(DependencyUpdaterInspectionAction::class.java)

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent) {
        // fixme
        val shortName = "MavenDependencyUpdaterInspection"
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val psiElement = e.getData(CommonDataKeys.PSI_ELEMENT)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val element = psiFile ?: psiElement
        val currentProfile = InspectionProjectProfileManager.getInstance(project).currentProfile
        val toolWrapper = if (element != null) {
            currentProfile.getInspectionTool(shortName, element)
        } else {
            currentProfile.getInspectionTool(shortName, project)
        }
        logger.assertTrue(toolWrapper != null, "Missed inspection: $shortName")
        if (toolWrapper == null) {
            return
        }

        val managerEx = InspectionManager.getInstance(project) as InspectionManagerEx
        val allWrappers: LinkedHashSet<InspectionToolWrapper<*, *>> = Sets.newLinkedHashSet()
        allWrappers.add(toolWrapper)
        currentProfile.collectDependentInspections(toolWrapper, allWrappers, managerEx.project)

        var scope: AnalysisScope? = null
        if (psiFile != null) {
            scope = AnalysisScope(psiFile)

            // make sure only one tool window at the same time
            val toolWindow = ToolWindowManager.getInstance(managerEx.project).getToolWindow(ToolWindowId.INSPECTION)
            if (toolWindow != null) {
                val contentManager = toolWindow.contentManager
                val contentTitle = InspectionsBundle.message(
                    "inspection.results.for.profile.toolwindow.title",
                    currentProfile.displayName,
                    scope.shortenName
                )
                val content = contentManager.contents.firstOrNull {
                    it.tabName == contentTitle || it.tabName.endsWith(contentTitle)
                }
                content?.let {
                    contentManager.removeContent(content, true)
                }
            }

            // fixme make sure version check triggered only
            val inspectionContext = GlobalInspectionContextImpl(managerEx.project, managerEx.contentManager)
            inspectionContext.doInspections(scope)
        }
    }
}
