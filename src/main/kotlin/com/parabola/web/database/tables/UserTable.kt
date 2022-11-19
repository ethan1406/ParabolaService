package com.parabola.web.database.tables

import org.jetbrains.exposed.sql.Table

object UserTable: Table("USER") {
    val username = varchar("username", 30)
    val companyName = varchar("company_name", 30).uniqueIndex()
    val role = varchar("company_name", 30)

    override val primaryKey = PrimaryKey(username)
}