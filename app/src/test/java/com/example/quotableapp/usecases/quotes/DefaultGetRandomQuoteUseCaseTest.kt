package com.example.quotableapp.usecases.quotes

import com.example.quotableapp.fakes.factories.QuotesFactory
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.local.entities.quote.QuoteEntity
import com.example.quotableapp.data.local.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.remote.model.QuoteDTO
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
class DefaultGetRandomQuoteUseCaseTest {

    private lateinit var localDataSource: QuotesLocalDataSource

    private lateinit var remoteDataSource: FakeQuotesRemoteDataSource

    @Before
    fun setUp() {
        localDataSource = mock()
        remoteDataSource = FakeQuotesRemoteDataSource()
    }

    @Test
    fun given_RemoteAPIWorking_when_FetchRandomQuote_then_ReturnValidQuote() = runTest {
        // given
        val quoteDTO = QuotesFactory.getDTOs(1).first()

        remoteDataSource.fetchRandomQuoteCompletableDeferred
            .complete(Result.success(quoteDTO))

        val useCase = createUseCase(this)

        // when
        val response = useCase.fetch()

        // then
        assertThat(response.isSuccess).isTrue()
        assertThat(response.getOrNull()).isEqualTo(quoteDTO.toDomain())
        verify(localDataSource, times(1))
            .insert(listOf(quoteDTO.toDb()))
    }

    @Test
    fun given_RemoteAPINotWorking_when_FetchRandomQuote_then_ReturnFailure() = runTest {
        // given
        remoteDataSource.fetchRandomQuoteCompletableDeferred
            .complete(Result.failure(IOException()))

        val useCase = createUseCase(this)

        // when
        val response = useCase.fetch()

        // then
        assertThat(response.isFailure).isTrue()
    }

    @Test
    fun given_LocalDataAvailable_when_GetRandomQuoteFlow_then_ReturnFlowWithQuote() = runTest {
        // given
        val quoteEntity = QuoteEntity(id = "1", content = "content")
        whenever(
            localDataSource.getFirstQuotesSortedById(
                any(),
                limit = anyInt()
            )
        ).thenReturn(flowOf(listOf(quoteEntity)))

        val useCase = createUseCase(this)

        // when
        val randomQuoteFlow = useCase.flow

        // then
        assertThat(randomQuoteFlow.single()).isEqualTo(quoteEntity.toDomain())
    }

    @Test
    fun given_NoLocalDataAvailable_when_GetRandomQuote_then_ReturnFlowWithNull() = runTest {
        // given
        whenever(
            localDataSource.getFirstQuotesSortedById(
                originParams = any(),
                limit = anyInt()
            )
        ).thenReturn(flowOf(emptyList()))

        val useCase = createUseCase(this)

        // when
        val randomQuoteFlow = useCase.flow

        // then
        assertThat(randomQuoteFlow.toList()).isEqualTo(listOf(null))
    }

    @Test
    fun given_NoAPIConnection_when_UpdateRandomQuote_then_ReturnResultFailure() = runTest {
        // given
        remoteDataSource.fetchRandomQuoteCompletableDeferred
            .complete(Result.failure(IOException()))

        val useCase = createUseCase(this)

        // when
        val res = useCase.update()

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_UpdateRandomQuote_then_ReturnResultSuccess(): Unit =
        runTest {
            // given
            val randomQuote = QuoteDTO(
                id = "1", content = "random content"
            )

            remoteDataSource.fetchRandomQuoteCompletableDeferred
                .complete(Result.success(randomQuote))

            val useCase = createUseCase(this)

            // when
            val res = useCase.update()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(localDataSource, times(1))
                .refresh(
                    entities = eq(listOf(randomQuote.toDb())),
                    originParams = eq(QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)),
                    lastUpdatedMillis = anyLong()
                )
        }

    private fun createUseCase(testScope: TestScope): GetRandomQuoteUseCase {
        return DefaultGetRandomQuoteUseCase(
            dispatchersProvider = testScope.getTestDispatchersProvider(),
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource
        )
    }
}