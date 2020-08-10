package com.github.liaow.mavenversionsupdaterpluginnew.services

import com.intellij.openapi.project.Project
import com.github.liaow.mavenversionsupdaterpluginnew.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
