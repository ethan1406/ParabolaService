package com.parabola.web.database.tables

import org.jetbrains.exposed.sql.Table

object ObjectUserTable: Table("OBJECT_USER") {
    val user = reference("user", UserTable)
    val companyName = varchar("company_name", 30).references(UserTable.companyName)
    val project = reference("project", ProjectTable)
    val projectObject = reference("object", ObjectTable)
    val isUserPrimary = bool("is_user_primary")
    val didApprove = bool("did_approve").default(false)

    override val primaryKey = PrimaryKey(user, projectObject)
}