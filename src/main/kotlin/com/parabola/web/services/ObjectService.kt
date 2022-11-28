package com.parabola.web.services

import com.parabola.web.database.daos.ObjectDao
import com.parabola.web.database.tables.ObjectTable
import com.parabola.web.database.tables.ObjectUserTable
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

class ObjectService(
    private val dataSource: DataSource
): ObjectServceGrpcKt.ObjectServceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    override suspend fun getAllObjectsInProjectForUser(request: GetAllObjectsInProjectForUserRequest): GetAllObjectsInProjectForUserResponse {
        val objectsInProject = withContext(Dispatchers.IO) {
            runCatching {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)
                    ObjectUserTable
                        .join(
                            ObjectTable, JoinType.INNER,
                            onColumn = ObjectUserTable.projectObject,
                            otherColumn = ObjectTable.id,
                            additionalConstraint = {
                                (ObjectUserTable.user eq request.username) and
                                        (ObjectTable.project eq request.projectId)
                            }
                        )
                        .slice(
                            ObjectTable.id, ObjectTable.name, ObjectTable.latestVersion,
                            ObjectUserTable.pendingVersion, ObjectUserTable.isUserPrimary, ObjectUserTable.didApprove
                        )
                        .selectAll().map {
                            object_ {
                                objectId = it[ObjectTable.id].value
                                name = it[ObjectTable.name]
                                latestVersion = it[ObjectTable.latestVersion]
                                pendingVersion = it[ObjectUserTable.pendingVersion]
                                didApprove = it[ObjectUserTable.didApprove]
                                isPrimary = it[ObjectUserTable.isUserPrimary]
                            }
                        }
                }
            }.onFailure {
                logger.error(it) { }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return getAllObjectsInProjectForUserResponse {
            objects.addAll(objectsInProject.getOrNull()?: emptyList())
        }
    }

    override suspend fun getAllCompaniesInObject(request: GetAllCompaniesInObjectRequest): GetAllCompaniesInObjectResponse {
        val companyResult = withContext(Dispatchers.IO) {
            runCatching {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)
                    ObjectDao.findById(request.objectId)?.users?.map {
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

        return getAllCompaniesInObjectResponse {
            companies.addAll(companyResult.getOrNull() ?: emptyList())
        }
    }

    override suspend fun addCompanyToObject(request: AddCompanyToObjectRequest): AddCompanyToObjectResponse {
        withContext(Dispatchers.IO) {
            try {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)

                    ObjectDao.findById(request.objectId)?.latestVersion?.let { version ->
                        ObjectUserTable.insert {
                            it[user] = request.usernameAdded
                            it[projectObject] = request.objectId
                            it[isUserPrimary] = false
                            it[pendingVersion] = version
                        }
                    }
                }

            } catch (e: Exception) {
                logger.error(e) {  }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return addCompanyToObjectResponse {  }
    }

    override suspend fun updateObjectVersion(request: UpdateObjectVersionRequest): UpdateObjectVersionResponse {
        withContext(Dispatchers.IO) {
            try {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)

                    ObjectTable.update({ ObjectTable.id eq request.objectId }) {
                        it[name] = request.updatedName
                        it[latestVersion] = request.updatedVersion
                    }

                    ObjectUserTable.update({ ObjectUserTable.projectObject eq request.objectId }) {
                        it[didApprove] = false
                    }
                }

            } catch (e: Exception) {
                logger.error(e) {  }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return updateObjectVersionResponse {  }
    }

    override suspend fun approveObjectVersion(request: ApproveObjectVersionRequest): ApproveObjectVersionResponse {
        withContext(Dispatchers.IO) {
            try {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)
                    ObjectDao.findById(request.objectId)?.latestVersion?.let { version ->
                        ObjectUserTable.update(
                            { (ObjectUserTable.projectObject eq request.objectId) and
                                    (ObjectUserTable.user eq request.username)
                            }
                        ) {
                            it[didApprove] = true
                            it[pendingVersion] = version
                        }
                    }

                }

            } catch (e: Exception) {
                logger.error(e) {  }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return approveObjectVersionResponse { }
    }

    override suspend fun removeCompanyInObject(request: RemoveCompanyInObjectRequest): RemoveCompanyInObjectResponse {

        withContext(Dispatchers.IO) {
            try {
                transaction(Database.connect(dataSource)) {
                    addLogger(StdOutSqlLogger)

                    ObjectUserTable.deleteWhere {
                        (user eq request.company.username) and
                                (projectObject eq request.objectId)
                    }
                }

            } catch (e: Exception) {
                logger.error(e) { "error deleting company in project" }
                throw StatusException(Status.UNKNOWN)
            }
        }

        return removeCompanyInObjectResponse { }
    }

    override suspend fun deleteObject(request: DeleteObjectRequest): DeleteObjectResponse {

        withContext(Dispatchers.IO) {
            transaction(Database.connect(dataSource)) {
                addLogger(StdOutSqlLogger)

                ObjectUserTable.deleteWhere {
                    projectObject eq request.objectId
                }

                ObjectTable.deleteWhere {
                    id eq request.objectId
                }
            }
        }

        return deleteObjectResponse { }
    }
}