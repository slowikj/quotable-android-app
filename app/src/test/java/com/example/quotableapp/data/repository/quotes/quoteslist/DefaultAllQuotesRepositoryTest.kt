package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.QuotesFactory
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.getFakeApiResponseInterpreter
import com.example.quotableapp.data.getTestCoroutineDispatchers
import com.example.quotableapp.data.getTestPagingConfig
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.services.QuotesRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesRemoteMediatorFactory
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import retrofit2.Response
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalPagingApi
class DefaultAllQuotesRepositoryTest {

    class DependencyManager(
        val remoteMediatorFactory: QuotesRemoteMediatorFactory = mock(),
        val localDataSource: QuotesLocalDataSource = mock(),
        val pagingConfig: PagingConfig = getTestPagingConfig(),
        val apiResponseInterpreter: ApiResponseInterpreter = getFakeApiResponseInterpreter(),
        val remoteService: QuotesRemoteService = mock(),
        val coroutineDispatchers: CoroutineDispatchers = getTestCoroutineDispatchers(),
    ) {
        val repository: DefaultAllQuotesRepository by lazy {
            DefaultAllQuotesRepository(
                quotesRemoteMediatorFactory = remoteMediatorFactory,
                quotesLocalDataSource = localDataSource,
                pagingConfig = pagingConfig,
                apiResponseInterpreter = apiResponseInterpreter,
                quotesRemoteService = remoteService,
                coroutineDispatchers = coroutineDispatchers
            )
        }
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_NoAPIConnection_when_updateExemplaryQuotes_then_ReturnFailure() = runBlocking {
        // given
        whenever(
            dependencyManager.remoteService.fetchQuotes(
                page = anyInt(),
                limit = anyInt(),
                sortBy = any(),
                order = any()
            )
        ).thenReturn(Response.error(500, "".toResponseBody()))

        // when
        val res = dependencyManager.repository.updateExemplaryQuotes()

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateExemplaryQuotes_then_ReturnSuccess(): Unit =
        runBlocking {
            // given
            val quotesResponseDTO = QuotesFactory.getResponseDTO(size = 10)
            whenever(
                dependencyManager.remoteService.fetchQuotes(
                    page = anyInt(),
                    limit = anyInt(),
                    sortBy = any(),
                    order = any()
                )
            ).thenReturn(Response.success(quotesResponseDTO))

            // when
            val res = dependencyManager.repository.updateExemplaryQuotes()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(dependencyManager.localDataSource, times(1))
                .refresh(any(), any(), anyLong())
        }

    @Test
    fun given_LocalDataAvailable_when_GetExemplaryQuotes_then_ReturnFlowWithQuotes() =
        runBlocking {
            // given
            val quotesEntities = QuotesFactory.getEntities(size = 10)
            val quotes = quotesEntities.map { Quote(id = it.id) }
            whenever(
                dependencyManager.localDataSource.getFirstQuotesSortedById(
                    originParams = eq(
                        QuoteOriginParams(
                            QuoteOriginParams.Type.DASHBOARD_EXEMPLARY
                        )
                    ),
                    limit = anyInt()
                )
            ).thenReturn(flowOf(quotesEntities))

            // when
            val resFlow = dependencyManager.repository.exemplaryQuotes

            // then
            assertThat(resFlow.single()).isEqualTo(quotes)
        }

    @Test
    fun given_NoLocalDataAvailable_when_GetExemplaryData_then_NoFlowEmission() = runBlocking {
        // given
        whenever(dependencyManager.localDataSource.getFirstQuotesSortedById(any(), anyInt()))
            .thenReturn(flowOf(emptyList()))

        // when
        val resFlow = dependencyManager.repository.exemplaryQuotes

        // then
        assertThat(resFlow.count()).isEqualTo(0)
    }

}