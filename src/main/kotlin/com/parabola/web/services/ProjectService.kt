package com.parabola.web.services

import com.parabola.web.database.daos.UserDao
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
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
        return  addCompanyToProjectResponse {  }

    }
}