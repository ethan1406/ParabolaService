package com.parabola.web.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object ProjectTable: IntIdTable("PROJECT") {
    val projectName = varchar("project_name", 30)

}