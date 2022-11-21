package com.parabola.web.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object ObjectTable: IntIdTable("OBJECT") {
    val name = varchar("name", 30)
    val project = reference("project", ProjectTable)
    val latestVersion = varchar("latest_version", 5).default("1.00")
}