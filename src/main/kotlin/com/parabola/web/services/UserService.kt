package com.parabola.web.services

import com.parabola.web.database.tables.UserTable
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

class UserService(
    private val dataSource: DataSource
): UserServiceGrpcKt.UserServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    override suspend fun signup(request: SignupRequest): SignupResponse {

        withContext(Dispatchers.IO) {
            try {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)

                    UserTable.insert {
                        it[username] = request.userName
                        it[companyName] = request.company
                        it[role] = request.role
                    }
                }
            } catch (e: ExposedSQLException) {
                logger.error(e) { "error creating user for ${request.userName}" }

                if (e.sqlState == "23505" || e.sqlState == "23000") {
                    logger.error("user already exists")
                    throw StatusException(Status.ALREADY_EXISTS)
                }

                throw StatusException(Status.INTERNAL)
            } catch (e: Exception) {
                logger.error(e) { "error creating for ${request.userName}" }
            }
        }

        return signupResponse { }
    }
}