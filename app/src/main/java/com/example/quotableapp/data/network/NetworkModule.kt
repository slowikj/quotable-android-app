package com.example.quotableapp.data.network

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.network.services.AuthorsRemoteService
import com.example.quotableapp.data.network.services.QuotesRemoteService
import com.example.quotableapp.data.network.services.TagsRemoteService
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.common.DefaultQuotableApiResponseInterpreter
import com.example.quotableapp.data.network.datasources.DefaultQuotesRemoteDataSource
import com.example.quotableapp.data.network.datasources.QuotesRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultRetrofitClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.quotable.io"

    @Provides
    @Singleton
    fun getQuotesService(@DefaultRetrofitClient retrofitClient: Retrofit): QuotesRemoteService =
        retrofitClient.create(QuotesRemoteService::class.java)

    @Provides
    @Singleton
    fun getAuthorsService(@DefaultRetrofitClient retrofitClient: Retrofit): AuthorsRemoteService =
        retrofitClient.create(AuthorsRemoteService::class.java)

    @Provides
    @Singleton
    fun getTagsService(@DefaultRetrofitClient retrofitClient: Retrofit): TagsRemoteService =
        retrofitClient.create(TagsRemoteService::class.java)

    @Provides
    @DefaultRetrofitClient
    @Singleton
    fun getRetrofitClient(@DefaultOkHttpClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @DefaultOkHttpClient
    @Singleton
    fun createOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
            .callTimeout(1, TimeUnit.MINUTES)
            .build()

    @Provides
    fun provideHttpResultInterpreter(dispatchersProvider: DispatchersProvider): ApiResponseInterpreter =
        DefaultQuotableApiResponseInterpreter(dispatchersProvider)

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {

        @Binds
        fun bindQuotesRemoteDataSource(quotesRemoteDataSource: DefaultQuotesRemoteDataSource): QuotesRemoteDataSource

    }
}

