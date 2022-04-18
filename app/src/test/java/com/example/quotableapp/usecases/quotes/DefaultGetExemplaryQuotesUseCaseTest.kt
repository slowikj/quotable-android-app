package com.example.quotableapp.usecases.quotes

import com.example.quotableapp.fakes.factories.QuotesFactory
import com.example.quotableapp.data.local.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.local.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.fakes.remotedatasources.FakeQuotesRemoteDataSource
import com.example.quotableapp.fakes.getTestDispatchersProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import java.io.IOException

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
class DefaultGetExemplaryQuotesUseCaseTest {

    private lateinit var localDataSource: QuotesLocalDataSource

    private lateinit var remoteDataSource: FakeQuotesRemoteDataSource

    private val exemplaryItemsLimit: Int = 10

    @Before
    fun setUp() {
        localDataSource = mock()
        remoteDataSource = FakeQuotesRemoteDataSource()
    }

    @Test
    fun given_NoAPIConnection_when_updateExemplaryQuotes_then_ReturnFailure() = runTest {
        // given
        remoteDataSource.fetchQuotesListCompletableDeferred
            .complete(Result.failure(IOException()))

        val useCase = createUseCase(this)

        // when
        val res = useCase.update()

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateExemplaryQuotes_then_ReturnSuccess(): Unit =
        runTest {
            // given
            val quotesResponseDTO = QuotesFactory.getResponseDTO(size = 10)
            remoteDataSource.fetchQuotesListCompletableDeferred
                .complete(Result.success(quotesResponseDTO))

            val useCase = createUseCase(this)

            // when
            val res = useCase.update()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(localDataSource, times(1))
                .refresh(any(), any(), anyLong())
        }

    @Test
    fun given_LocalDataAvailable_when_GetExemplaryQuotes_then_ReturnFlowWithQuotes() =
        runTest {
            // given
            val quotesEntities = QuotesFactory.getEntities(size = 10)
            val quotes = quotesEntities.map { Quote(id = it.id) }
            whenever(
                localDataSource.getFirstQuotesSortedById(
                    originParams = eq(
                        QuoteOriginParams(
                            QuoteOriginParams.Type.DASHBOARD_EXEMPLARY
                        )
                    ),
                    limit = anyInt()
                )
            ).thenReturn(flowOf(quotesEntities))

            val useCase = createUseCase(this)

            // when
            val resFlow = useCase.flow

            // then
            assertThat(resFlow.single()).isEqualTo(quotes)
        }

    @Test
    fun given_NoLocalDataAvailable_when_GetExemplaryData_then_EmitEmptyList() = runTest {
        // given
        whenever(localDataSource.getFirstQuotesSortedById(any(), anyInt()))
            .thenReturn(flowOf(emptyList()))

        val useCase = createUseCase(this)

        // when
        val resFlow = useCase.flow

        // then
        assertThat(resFlow.toList()).isEqualTo(listOf(emptyList<Quote>()))
    }

    private fun createUseCase(testScope: TestScope): GetExemplaryQuotesUseCase {
        return DefaultGetExemplaryQuotesUseCase(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            dispatchersProvider = testScope.getTestDispatchersProvider(),
            itemsLimit = exemplaryItemsLimit
        )
    }
}