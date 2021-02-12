package id.tru.authentication.demo.api

import id.tru.authentication.demo.data.PhoneCheck
import id.tru.authentication.demo.data.PhoneCheckPost
import id.tru.authentication.demo.data.PhoneCheckResult
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/check")
    suspend fun getPhoneCheck(@Body user: PhoneCheckPost): Response<PhoneCheck>

    @GET("/check_status")
    suspend fun getPhoneCheckResult(@Query(value = "check_id") checkId: String): Response<PhoneCheckResult>
}