package com.parabola.web.database.scripts

import com.parabola.web.database.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object InitTableScript {

    fun init(dataSource: DataSource) {
        transaction (Database.connect(dataSource)){
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(UserTable, ObjectTable, ObjectUserTable,
            ProjectUserTable, ProjectTable)
        }
    }
}