package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import app.cash.turbine.test
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.getExemplaryPagingConfig
import com.example.quotableapp.data.getFakeApiResponseInterpreter
import com.example.quotableapp.data.getTestCoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.network.services.QuotesRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesRemoteMediatorFactory
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import retrofit2.Response
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class DefaultAllQuotesRepositoryTest {

    class DependencyManager(
        val remoteMediatorFactory: QuotesRemoteMediatorFactory = mock(),
        val localDataSource: QuotesLocalDataSource = mock(),
        val pagingConfig: PagingConfig = getExemplaryPagingConfig(),
        val converters: QuoteConverters = mock(),
        val apiResponseInterpreter: ApiResponseInterpreter = getFakeApiResponseInterpreter(),
        val remoteService: QuotesRemoteService = mock(),
        val coroutineDispatchers: CoroutineDispatchers = getTestCoroutineDispatchers(),
    ) {
        val repository: DefaultAllQuotesRepository by lazy {
            DefaultAllQuotesRepository(
                quotesRemoteMediatorFactory = remoteMediatorFactory,
                quotesLocalDataSource = localDataSource,
                pagingConfig = pagingConfig,
                quotesConverters = converters,
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
    fun given_NoAPIConnection_when_updateExemplaryQuotes_then_ReturnFailure() = runBlockingTest {
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
    fun given_WorkingAPIConnection_when_updateExemplaryQuotes_then_ReturnSuccess() =
        runBlockingTest {
            // given
            val quotesDTO = (1..10).map { QuoteDTO(id = it.toString(), content = it.toString()) }
            whenever(
                dependencyManager.remoteService.fetchQuotes(
                    page = anyInt(),
                    limit = anyInt(),
                    sortBy = any(),
                    order = any()
                )
            ).thenReturn(Response.success(prepareResponseFrom(quotesDTO)))

            whenever(dependencyManager.converters.toDomain(any<QuoteDTO>()))
                .thenReturn(Quote(id = "xxxx"))

            // when
            val res = dependencyManager.repository.updateExemplaryQuotes()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(dependencyManager.localDataSource, times(1))
                .refresh(any(), any(), anyLong())
        }

    @Test
    fun given_LocalDataAvailable_when_GetExemplaryQuotes_then_ReturnFlowWithQuotes() =
        runBlockingTest {
            // given
            val quotesEntities = getExemplaryQuoteEntities(size = 10)
            val quotes = quotesEntities.map { Quote(id = it.id) }
            whenever(
                dependencyManager.localDataSource.getFirstQuotesSortedById(
                    originParams = eq(QuoteOriginParams(
                        QuoteOriginParams.Type.EXAMPLE_FROM_DASHBOARD
                    )),
                    limit = anyInt()
                )
            ).thenReturn(flowOf(quotesEntities))

            for(entity in quotesEntities) {
                whenever(dependencyManager.converters.toDomain(entity))
                    .thenReturn(Quote(id = entity.id))
            }

            // when
            val resFlow = dependencyManager.repository.exemplaryQuotes

            // then
            assertThat(resFlow.single()).isEqualTo(quotes)
        }

    @Test
    fun given_NoLocalDataAvailable_when_GetExemplaryData_then_NoFlowEmission() = runBlockingTest {
        // given
        whenever(dependencyManager.localDataSource.getFirstQuotesSortedById(any(), anyInt()))
            .thenReturn(flowOf(emptyList()))

        // when
        val resFlow = dependencyManager.repository.exemplaryQuotes

        // then
        assertThat(resFlow.count()).isEqualTo(0)
    }

    private fun getExemplaryQuoteEntities(size: Int): List<QuoteEntity> = (1..size).map {
        QuoteEntity(id = it.toString())
    }

    private fun prepareResponseFrom(quotesDTO: List<QuoteDTO>): QuotesResponseDTO =
        QuotesResponseDTO(
            count = quotesDTO.size,
            totalCount = quotesDTO.size,
            page = 1,
            totalPages = 1,
            lastItemIndex = quotesDTO.size - 1,
            results = quotesDTO,
        )
}