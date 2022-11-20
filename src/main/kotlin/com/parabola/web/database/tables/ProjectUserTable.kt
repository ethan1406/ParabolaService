package com.parabola.web.database.tables

import org.jetbrains.exposed.sql.Table

object ProjectUserTable: Table("PROJECT_USER") {
    val user = reference("user", UserTable)
    val companyName = varchar("company_name", 30).references(UserTable.companyName)
    val project = reference("project", ProjectTable)

    override val primaryKey = PrimaryKey(user, project)
}