package id.tru.authentication.demo.api

import id.tru.authentication.demo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    val apiClient: ApiService by lazy {

        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        val okHttpClient = OkHttpClient()
                .newBuilder()
                //httpLogging interceptor for logging network requests
                .addInterceptor(httpLoggingInterceptor)
                .build()

        Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
    }
}