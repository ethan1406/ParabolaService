package com.parabola.web

import com.parabola.web.database.scripts.InitTableScript
import com.parabola.web.services.ObjectService
import com.parabola.web.services.ProjectService
import com.parabola.web.services.UserService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.grpc.Server
import io.grpc.ServerBuilder
import org.apache.log4j.BasicConfigurator
import javax.sql.DataSource

class ParabolaApplication(
    private val port: Int,
    dataSource: DataSource,
) {
    val server: Server = ServerBuilder
        .forPort(port)
        .addService(UserService(dataSource))
        .addService(ProjectService(dataSource))
        .addService(ObjectService(dataSource))
        .build()


    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@ParabolaApplication.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

fun main() {
    BasicConfigurator.configure()

    val port = System.getenv("PORT")?.toInt() ?: 50051

    val datasource = getHikariDataSource()
    InitTableScript.init(datasource)
    val server = ParabolaApplication(port, datasource)

    server.start()
    server.blockUntilShutdown()

}

private fun getHikariDataSource() =
    HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://${System.getenv("DB_HOST")}:${System.getenv("DB_PORT")}/${System.getenv("DB_NAME")}"
            username = System.getenv("DB_USER")
            password = System.getenv("DB_PASSWORD")
            driverClassName = "com.mysql.cj.jdbc.Driver"
            addDataSourceProperty("rewriteBatchedStatements", "true")
        }
    )