package com.parabola.web.services

import com.parabola.web.database.daos.ProjectDao
import com.parabola.web.database.daos.UserDao
import com.parabola.web.database.tables.ObjectTable
import com.parabola.web.database.tables.ObjectUserTable
import com.parabola.web.database.tables.ProjectTable
import com.parabola.web.database.tables.ProjectUserTable
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

class ProjectService(
    private val dataSource: DataSource
): ProjectServiceGrpcKt.ProjectServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    override suspend fun getAllCompanies(request: GetAllCompaniesRequest): GetAllCompaniesResponse {

        val companyResult = withContext(Dispatchers.IO) {
            runCatching {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)
                    UserDao.all().map {
                        company {
                            username = it.username
                            companyName = it.companyName
                        }
                    }
                }
            }.onFailure {
                logger.error(it) { }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return getAllCompaniesResponse {
            companies.addAll(companyResult.getOrNull() ?: emptyList())
        }
    }

    override suspend fun addCompanyToProject(request: AddCompanyToProjectRequest): AddCompanyToProjectResponse {

        withContext(Dispatchers.IO) {
            transaction(Database.connect(dataSource)) {
                addLogger(StdOutSqlLogger)

                ProjectUserTable.insert {
                    it[user] = request.company.username
                    it[companyName] = request.company.companyName
                    it[project] = request.projectId
                }
            }
        }

        return addCompanyToProjectResponse {  }
    }

    override suspend fun getAllCompaniesInProject(request: GetAllCompaniesInProjectRequest): GetAllCompaniesInProjectResponse {

        val userResult = withContext(Dispatchers.IO) {
            runCatching {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)
                    ProjectDao.findById(request.projectId)?.users?.map {
                        company {
                            username = it.username
                            companyName = it.companyName
                            role = it.role
                        }
                    }
                }
            }.onFailure {
                logger.error(it) { }
                throw StatusException(Status.UNKNOWN)
            }
        }


        return getAllCompaniesInProjectResponse {
            companies.addAll(userResult.getOrNull() ?: emptyList())
        }
    }

    override suspend fun createObjectInProject(request: CreateObjectRequest): CreateObjectResponse {

        return withContext(Dispatchers.IO) {
            try {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)

                    val defaultVersion = "1.0.0"

                    val id = ObjectTable.insert {
                        it[name] = request.objectName
                        it[project] = request.projectId
                        it[latestVersion] = defaultVersion
                    } get ObjectTable.id

                    ObjectUserTable.insert {
                        it[user] = request.username
                        it[projectObject] = id
                        it[isUserPrimary] = true
                        it[pendingVersion] = defaultVersion
                    }

                    createObjectResponse {
                        objectId = id.value
                    }
                }
            } catch (e: Exception) {
                logger.error(e) {  }
                throw StatusException(Status.UNKNOWN)
            }
        }

    }

    override suspend fun removeCompanyInProject(request: RemoveCompanyInProjectRequest): RemoveCompanyInProjectResponse {

        withContext(Dispatchers.IO) {
            try {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)

                    ProjectUserTable.deleteWhere {
                        (user eq request.company.username) and
                                (project eq request.projectId)
                    }
                }

            } catch (e: Exception) {
                logger.error(e) { "error deleting company in project" }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return removeCompanyInProjectResponse { }
    }
}