package com.example.quotableapp.di

import com.example.quotableapp.data.network.AuthorsService
import com.example.quotableapp.data.network.QuotesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultRetrofitClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthorPhotoUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.quotable.io"

    @Provides
    fun getQuotesService(@DefaultRetrofitClient retrofitClient: Retrofit): QuotesService =
        retrofitClient.create(QuotesService::class.java)

    @Provides
    fun getAuthorsService(@DefaultRetrofitClient retrofitClient: Retrofit): AuthorsService =
        retrofitClient.create(AuthorsService::class.java)

    @Provides
    @AuthorPhotoUrl
    fun getAuthorPhotoUrl(authorSlug: String, size: Int = 200) =
        "https://images.quotable.dev/profile/$size/$authorSlug.jpg"

    @Provides
    @DefaultRetrofitClient
    fun getRetrofitClient(@DefaultOkHttpClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @DefaultOkHttpClient
    fun createOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
            .build()
}

