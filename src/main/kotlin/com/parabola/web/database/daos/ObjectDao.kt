package com.parabola.web.database.daos

import com.parabola.web.database.tables.ObjectTable
import com.parabola.web.database.tables.ObjectUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ObjectDao(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<ObjectDao>(ObjectTable)

    var objectId by ObjectTable.id
    var projectId by ObjectTable.project
    var name by ObjectTable.name
    var latestVersion by ObjectTable.latestVersion

    var users by UserDao via ObjectUserTable
}