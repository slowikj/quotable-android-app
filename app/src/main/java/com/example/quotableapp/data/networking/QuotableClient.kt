package com.example.quotableapp.data.networking

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object QuotableClient {

    private const val BASE_URL = "https://api.quotable.io"

    private var retrofit: Retrofit? = null

    fun getQuotesService(): QuotesService =
        getClient().create(QuotesService::class.java)

    private fun getClient(): Retrofit {
        if (retrofit == null) {
            retrofit = createRetrofitClient()
        }
        return retrofit!!
    }

    private fun createRetrofitClient() = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(createOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun createOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { HttpLoggingInterceptor.Level.BODY })
            .build()

}