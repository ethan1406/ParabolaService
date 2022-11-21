package com.parabola.web.database.tables

import org.jetbrains.exposed.sql.Table

object ObjectUserTable: Table("OBJECT_USER") {
    val user = reference("user", UserTable)
    val projectObject = reference("object", ObjectTable)
    val isUserPrimary = bool("is_user_primary")
    val pendingVersion = varchar("current_version", 5)
    val didApprove = bool("did_approve").default(false)

    override val primaryKey = PrimaryKey(user, projectObject)
}