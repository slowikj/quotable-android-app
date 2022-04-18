package com.example.quotableapp.usecases.quotes

import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.local.entities.quote.QuoteEntity
import com.example.quotableapp.data.remote.model.QuoteDTO
import com.example.quotableapp.fakes.remotedatasources.FakeQuotesRemoteDataSource
import com.example.quotableapp.fakes.getTestDispatchersProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
class DefaultGetQuoteUseCaseTest {

    private lateinit var localDataSource: QuotesLocalDataSource

    private lateinit var remoteDataSource: FakeQuotesRemoteDataSource

    @Before
    fun setUp() {
        localDataSource = mock()
        remoteDataSource = FakeQuotesRemoteDataSource()
    }

    @Test
    fun given_NoAPIConnection_when_updateQuote_then_ReturnFailure() = runTest {
        // given
        val quoteId = "1"
        remoteDataSource.fetchQuoteCompletableDeferred
            .complete(Result.failure(IOException()))

        val useCase = createUseCase(this)

        // when
        val res = useCase.update(id = quoteId)

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_UpdateQuote_then_ReturnSuccess() = runTest {
        // given
        val quoteId = "1"
        val quoteDTO = QuoteDTO(id = quoteId, content = "abc")
        remoteDataSource.fetchQuoteCompletableDeferred
            .complete(Result.success(quoteDTO))

        val useCase = createUseCase(this)

        // when
        val res = useCase.update(id = quoteId)


        // then
        assertThat(res.isSuccess).isTrue()
        verify(localDataSource, times(1))
            .insert(entities = listOf(quoteDTO.toDb()))
    }

    @Test
    fun given_LocalDataAvailable_when_GetQuote_then_ReturnFlowWithQuote() = runTest {
        // given
        val quoteId = "1"
        val quoteEntity = QuoteEntity(id = quoteId, content = "content")

        whenever(localDataSource.getQuoteFlow(id = quoteId))
            .thenReturn(flowOf(quoteEntity))

        val useCase = createUseCase(this)

        // when
        val quoteFlow = useCase.getFlow(id = quoteId)

        // then
        assertThat(quoteFlow.single()).isEqualTo(quoteEntity.toDomain())
    }

    @Test
    fun given_NoLocalDataAvailable_when_GetQuote_thenReturnFlowWithNull() = runTest {
        // given
        val quoteId = "1"
        whenever(localDataSource.getQuoteFlow(quoteId))
            .thenReturn(flowOf(null))

        val useCase = createUseCase(this)

        // when
        val quoteFlow = useCase.getFlow(id = quoteId)

        // then
        assertThat(quoteFlow.toList()).isEqualTo(listOf(null))
    }

    private fun createUseCase(testScope: TestScope): GetQuoteUseCase {
        return DefaultGetQuoteUseCase(
            dispatchersProvider = testScope.getTestDispatchersProvider(),
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource
        )
    }
}