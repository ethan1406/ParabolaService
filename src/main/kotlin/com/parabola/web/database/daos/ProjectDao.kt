package com.parabola.web.database.daos

import com.parabola.web.database.tables.ProjectTable
import com.parabola.web.database.tables.ProjectUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ProjectDao(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<ProjectDao>(ProjectTable)

    var projectId by ProjectTable.id
    var projectName by ProjectTable.projectName

    var users by UserDao via ProjectUserTable
}