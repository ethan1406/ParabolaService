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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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
                        it[username] = request.company.username
                        it[companyName] = request.company.companyName
                        it[role] = request.company.role
                    }
                }
            } catch (e: ExposedSQLException) {
                logger.error(e) { "error creating user for ${request.company}" }

                if (e.sqlState == "23505" || e.sqlState == "23000") {
                    logger.error("user already exists")
                    throw StatusException(Status.ALREADY_EXISTS)
                }

                throw StatusException(Status.UNKNOWN)
            } catch (e: Exception) {
                logger.error(e) { "error creating for ${request.company}" }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return signupResponse { }
    }

    override suspend fun getCompanyInfo(request: GetCompanyInfoRequest): GetCompanyInfoResponse {

        val userCompany = withContext(Dispatchers.IO) {
            try {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)

                    UserTable.select { UserTable.username eq request.username }
                        .map {
                            company {
                                username = request.username
                                companyName = it[UserTable.companyName]
                                role = it[UserTable.role]
                            }
                        }
                        .firstOrNull() ?: throw StatusException(Status.UNKNOWN)

                }
            } catch (e: ExposedSQLException) {
                logger.error(e) { "error creating user for ${request.username}" }

                if (e.sqlState == "23505" || e.sqlState == "23000") {
                    logger.error("user already exists")
                    throw StatusException(Status.ALREADY_EXISTS)
                }

                throw StatusException(Status.UNKNOWN)
            } catch (e: Exception) {
                logger.error(e) { "error creating for ${request.username}" }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return getCompanyInfoResponse {
            company = userCompany
        }
    }

    override suspend fun createProject(request: CreateProjectRequest): CreateProjectResponse {

        return withContext(Dispatchers.IO) {
            transaction(Database.connect(dataSource)) {
                addLogger(StdOutSqlLogger)

                val id = ProjectTable.insert {
                    it[projectName] = request.projectName
                } get ProjectTable.id

                ProjectUserTable.insert {
                    it[user] = request.company.username
                    it[companyName] = request.company.companyName
                    it[project] = id
                }

                createProjectResponse {
                    projectId = id.value
                }
            }
        }
    }

    override suspend fun updateProject(request: UpdateProjectRequest): UpdateProjectResponse {

        withContext(Dispatchers.IO) {
            transaction(Database.connect(dataSource)) {
                addLogger(StdOutSqlLogger)

                ProjectTable.update({ ProjectTable.id eq request.projectId }) {
                    it[projectName] = request.projectName
                }
            }
        }

        return updateProjectResponse { }

    }

    override suspend fun deleteProject(request: DeleteProjectRequest): DeleteProjectResponse {

        withContext(Dispatchers.IO) {
            transaction(Database.connect(dataSource)) {
                addLogger(StdOutSqlLogger)

                ProjectUserTable.deleteWhere {
                    project eq request.projectId
                }

                ProjectTable.deleteWhere {
                    id eq request.projectId
                }
            }
        }

        return deleteProjectResponse { }
    }

    override suspend fun getAllProjects(request: GetAllProjectsRequest): GetAllProjectsResponse {

        val userProjects = withContext(Dispatchers.IO) {
            runCatching {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)
                    UserDao.findById(request.username)?.projects?.map {
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