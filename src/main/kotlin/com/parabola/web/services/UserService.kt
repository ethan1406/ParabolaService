package com.parabola.web.services

import com.parabola.web.database.daos.UserDao
import com.parabola.web.database.tables.ProjectTable
import com.parabola.web.database.tables.ProjectUserTable
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

                throw StatusException(Status.UNKNOWN)
            } catch (e: Exception) {
                logger.error(e) { "error creating for ${request.userName}" }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return signupResponse { }
    }

    override suspend fun createProject(request: CreateProjectRequest): CreateProjectResponse {

        withContext(Dispatchers.IO) {
            transaction(Database.connect(dataSource)) {
                addLogger(StdOutSqlLogger)

                val projectId = ProjectTable.insert {
                    it[projectName] = request.projectName
                } get ProjectTable.id

                ProjectUserTable.insert {
                    it[user] = request.company.username
                    it[companyName] = request.company.companyName
                    it[project] = projectId
                }
            }
        }

        return createProjectResponse { }
    }

    override suspend fun getAllProjects(request: GetAllProjectsRequest): GetAllProjectsResponse {

        val userProjects = withContext(Dispatchers.IO) {
            runCatching {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)
                    UserDao.findById(request.company.username)?.projects?.map {
                            project {
                                id = it.id.value
                                name = it.projectName
                            }
                        }
                }
            }.onFailure {
                logger.error(it) { }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return getAllProjectsResponse {
            projects.addAll(userProjects.getOrNull() ?: emptyList())
        }
    }

}