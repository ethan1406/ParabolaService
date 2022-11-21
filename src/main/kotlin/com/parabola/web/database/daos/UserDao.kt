package com.parabola.web.database.daos

import com.parabola.web.database.tables.ObjectUserTable
import com.parabola.web.database.tables.ProjectUserTable
import com.parabola.web.database.tables.UserTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserDao(id: EntityID<String>): Entity<String>(id) {
    companion object: EntityClass<String, UserDao>(UserTable)

    var username by UserTable.username
    var companyName by UserTable.companyName
    var role by UserTable.role

    var projects by ProjectDao via ProjectUserTable
    var objects by ObjectDao via ObjectUserTable
}