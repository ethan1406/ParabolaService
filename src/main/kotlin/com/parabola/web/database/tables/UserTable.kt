package com.parabola.web.database.tables

import org.jetbrains.exposed.dao.id.IdTable

object UserTable: IdTable<String>("USER") {
    val username = varchar("username", 30).uniqueIndex()
    val companyName = varchar("company_name", 30).uniqueIndex()
    val role = varchar("role", 30)

    override val id = username.entityId()
}