package com.example.quotableapp.data.network.di

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.network.AuthorsRemoteService
import com.example.quotableapp.data.network.QuotesRemoteService
import com.example.quotableapp.data.network.TagsRemoteService
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.common.DefaultQuotableApiResponseInterpreter
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
    fun getQuotesService(@DefaultRetrofitClient retrofitClient: Retrofit): QuotesRemoteService =
        retrofitClient.create(QuotesRemoteService::class.java)

    @Provides
    fun getAuthorsService(@DefaultRetrofitClient retrofitClient: Retrofit): AuthorsRemoteService =
        retrofitClient.create(AuthorsRemoteService::class.java)

    @Provides
    fun getTagsService(@DefaultRetrofitClient retrofitClient: Retrofit): TagsRemoteService =
        retrofitClient.create(TagsRemoteService::class.java)

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
            .callTimeout(1, TimeUnit.MINUTES)
            .build()

    @Provides
    fun provideHttpResultInterpreter(coroutineDispatchers: CoroutineDispatchers): ApiResponseInterpreter =
        DefaultQuotableApiResponseInterpreter(coroutineDispatchers)
}

