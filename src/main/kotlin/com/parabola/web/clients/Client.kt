//package com.parabola.web.clients
//
//import com.parabola.web.services.*
//import io.grpc.ManagedChannel
//import io.grpc.ManagedChannelBuilder
//import io.grpc.Metadata
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.asExecutor
//import java.io.Closeable
//import java.util.concurrent.TimeUnit
//
//class Client(private val channel: ManagedChannel) : Closeable {
//    private val userStub = UserServiceGrpcKt.UserServiceCoroutineStub(channel)
//    private val projectStub = ProjectServiceGrpcKt.ProjectServiceCoroutineStub(channel)
//    private val objectStub = ObjectServceGrpcKt.ObjectServceCoroutineStub(channel)
//
//    suspend fun signup() {
//
////
////        val response = userStub.createProject(
////            createProjectRequest {
////                company = company {
////                    username = "ethan_dad"
////                    companyName = "lyft"
////                }
////                projectName = "Big ass company"
////            }
////        )
////        val projectResponse = projectStub.createObjectInProject(
////            createObjectRequest {
////                projectId = 4
////                username = "tony_son"
////                objectName = "ethan is my master"
////            }
////        )
//
////        objectStub.updateObjectVersion(
////            updateObjectVersionRequest {
////                username = "tony_son"
////                updatedName = "new dad coming #2"
////                updatedVersion = "1.03"
////                objectId = 7
////            }
////        )
////        val response = objectStub.approveObjectVersion(
////            approveObjectVersionRequest {
////                username = "ethan_dad"
////                objectId = 7
////            }
////        )
//
////        val objectResponse = objectStub.getAllObjectsInProjectForUser(
////            getAllObjectsInProjectForUserRequest {
////                username = "ethan_dad"
////                projectId = 4
////            }
////        )
//
//        //val response = userStub.getCompanyInfo(getCompanyInfoRequest { username = "ethan_dad" })
//
//        val response = objectStub.deleteObject(deleteObjectRequest { objectId = 18 })
//
//
//        println("got it")
//        println(response)
//    }
//
//    override fun close() {
//        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
//    }
//}
//
//suspend fun main() {
//    val port = System.getenv("PORT")?.toInt() ?: 50051
//
//    val channel = ManagedChannelBuilder
//        .forAddress("localhost", port)
//        .usePlaintext()
//        .executor(Dispatchers.IO.asExecutor())
//        .build()
//
//    val client = Client(channel)
//
//    client.signup()
//
//}