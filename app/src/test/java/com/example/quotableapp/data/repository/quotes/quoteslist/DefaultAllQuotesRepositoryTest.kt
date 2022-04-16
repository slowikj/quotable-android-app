package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.QuotesFactory
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.getTestPagingConfig
import com.example.quotableapp.data.getTestdispatchersProvider
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.datasources.FetchQuotesListParams
import com.example.quotableapp.data.network.datasources.QuotesRemoteDataSource
import com.example.quotableapp.data.paging.quotes.QuotesRemoteMediatorFactory
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import java.io.IOException
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalTime
@ExperimentalPagingApi
class DefaultAllQuotesRepositoryTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    class DependencyManager(
        val remoteMediatorFactory: QuotesRemoteMediatorFactory = mock(),
        val localDataSource: QuotesLocalDataSource = mock(),
        val pagingConfig: PagingConfig = getTestPagingConfig(),
        val remoteDataSource: QuotesRemoteDataSource = mock(),
        val dispatchersProvider: DispatchersProvider = getTestdispatchersProvider(),
    ) {
        val repository: DefaultAllQuotesRepository by lazy {
            DefaultAllQuotesRepository(
                quotesRemoteMediatorFactory = remoteMediatorFactory,
                quotesLocalDataSource = localDataSource,
                pagingConfig = pagingConfig,
                quotesRemoteDataSource = remoteDataSource,
                dispatchersProvider = dispatchersProvider
            )
        }
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_NoAPIConnection_when_updateExemplaryQuotes_then_ReturnFailure() = runTest {
        // given
        whenever(
            dependencyManager.remoteDataSource.fetch(any<FetchQuotesListParams>())
        ).thenReturn(Result.failure(IOException()))

        // when
        val res = dependencyManager.repository.updateExemplaryQuotes()

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateExemplaryQuotes_then_ReturnSuccess(): Unit =
        runTest {
            // given
            val quotesResponseDTO = QuotesFactory.getResponseDTO(size = 10)
            whenever(
                dependencyManager.remoteDataSource.fetch(any<FetchQuotesListParams>())
            ).thenReturn(Result.success(quotesResponseDTO))

            // when
            val res = dependencyManager.repository.updateExemplaryQuotes()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(dependencyManager.localDataSource, times(1))
                .refresh(any(), any(), anyLong())
        }

    @Test
    fun given_LocalDataAvailable_when_GetExemplaryQuotes_then_ReturnFlowWithQuotes() =
        runTest {
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
    fun given_NoLocalDataAvailable_when_GetExemplaryData_then_NoFlowEmission() = runTest {
        // given
        whenever(dependencyManager.localDataSource.getFirstQuotesSortedById(any(), anyInt()))
            .thenReturn(flowOf(emptyList()))

        // when
        val resFlow = dependencyManager.repository.exemplaryQuotes

        // then
        assertThat(resFlow.count()).isEqualTo(0)
    }

}