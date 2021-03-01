package id.tru.authentication.demo.api

import id.tru.authentication.demo.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/check")
    suspend fun createSubscriberCheck(@Body user: SubscriberCheckPost): Response<SubscriberCheck>

    @GET("/check_status")
    suspend fun getSubscriberCheckResult(@Query(value = "check_id") checkId: String): Response<SubscriberCheckResult>
}