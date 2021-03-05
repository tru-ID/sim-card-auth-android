package id.tru.authentication.demo.api

import id.tru.authentication.demo.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/subscriber-check")
    suspend fun createSubscriberCheck(@Body user: SubscriberCheckPost): Response<SubscriberCheck>

    @GET("/subscriber-check/{check_id}")
    suspend fun getSubscriberCheckResult(@Path(value = "check_id") checkId: String): Response<SubscriberCheckResult>
}