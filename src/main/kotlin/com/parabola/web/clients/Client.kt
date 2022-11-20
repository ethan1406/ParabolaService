package com.parabola.web.clients

import com.parabola.web.services.*
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.Closeable
import java.util.concurrent.TimeUnit

class Client(private val channel: ManagedChannel) : Closeable {
    private val userStub = UserServiceGrpcKt.UserServiceCoroutineStub(channel)
    private val projectStub = ProjectServiceGrpcKt.ProjectServiceCoroutineStub(channel)

    suspend fun signup() {


        val response = userStub.getAllProjects(
            getAllProjectsRequest {
                company = company {
                    username = "ethan_dad"
                    companyName = "lyft"
                }
            }
        )
        val projectResponse = projectStub.getAllCompanies(
            getAllCompaniesRequest {

            }
        )

        println("got it")
        println(projectResponse.companiesList)
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

suspend fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 50051

    val channel = ManagedChannelBuilder
        .forAddress("localhost", port)
        .usePlaintext()
        .executor(Dispatchers.IO.asExecutor())
        .build()

    val client = Client(channel)

    client.signup()

}