package com.example.quotableapp.data.repository.quotes.onequote

import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.QuotesFactory
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.local.entities.quote.QuoteEntity
import com.example.quotableapp.data.local.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.getTestdispatchersProvider
import com.example.quotableapp.data.remote.datasources.FetchQuoteParams
import com.example.quotableapp.data.remote.datasources.QuotesRemoteDataSource
import com.example.quotableapp.data.remote.model.QuoteDTO
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import java.io.IOException
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalTime
class DefaultOneQuoteRepositoryTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    class DependencyManager(
        val dispatchersProvider: DispatchersProvider = getTestdispatchersProvider(),
        val remoteService: QuotesRemoteDataSource = mock(),
        val localDataSource: QuotesLocalDataSource = mock(),
    ) {
        val repository: DefaultOneQuoteRepository
            get() = DefaultOneQuoteRepository(
                dispatchersProvider = dispatchersProvider,
                quotesRemoteDataSource = remoteService,
                quotesLocalDataSource = localDataSource,
            )
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_NoAPIConnection_when_UpdateRandomQuote_then_ReturnResultFailure() = runTest {
        // given
        whenever(dependencyManager.remoteService.fetchRandom()).thenReturn(
            Result.failure(IOException())
        )

        // when
        val res = dependencyManager.repository.updateRandomQuote()

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
            whenever(dependencyManager.remoteService.fetchRandom()).thenReturn(
                Result.success(randomQuote)
            )

            // when
            val res = dependencyManager.repository.updateRandomQuote()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(
                dependencyManager.localDataSource, times(1)
            ).refresh(
                entities = eq(listOf(randomQuote.toDb())),
                originParams = eq(QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)),
                lastUpdatedMillis = anyLong()
            )
        }

    @Test
    fun given_NoAPIConnection_when_updateQuote_then_ReturnFailure() = runTest {
        // given
        val quoteId = "1"
        whenever(dependencyManager.remoteService.fetch(FetchQuoteParams(id = quoteId))).thenReturn(
            Result.failure(IOException())
        )

        // when
        val res = dependencyManager.repository.updateQuote(id = quoteId)

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_UpdateQuote_then_ReturnSuccess() = runTest {
        // given
        val quoteId = "1"
        val quoteDTO = QuoteDTO(id = quoteId, content = "abc")
        whenever(dependencyManager.remoteService.fetch(FetchQuoteParams(id = quoteId))).thenReturn(
            Result.success(quoteDTO)
        )

        // when
        val res = dependencyManager.repository.updateQuote(quoteId)

        // then
        assertThat(res.isSuccess).isTrue()
        verify(
            dependencyManager.localDataSource,
            times(1)
        ).insert(entities = listOf(quoteDTO.toDb()))
    }

    @Test
    fun given_LocalDataAvailable_when_GetRandomQuote_then_ReturnFlowWithQuote() = runTest {
        // given
        val quoteEntity = QuoteEntity(id = "1", content = "content")
        whenever(
            dependencyManager.localDataSource.getFirstQuotesSortedById(
                originParams = QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM),
                limit = 1
            )
        ).thenReturn(flowOf(listOf(quoteEntity)))

        // when
        val randomQuoteFlow = dependencyManager.repository.randomQuote

        // then
        assertThat(randomQuoteFlow.single()).isEqualTo(quoteEntity.toDomain())
    }

    @Test
    fun given_NoLocalDataAvailable_when_GetRandomQuote_then_ReturnFlowWithNull() = runTest {
        // given
        whenever(
            dependencyManager.localDataSource.getFirstQuotesSortedById(
                originParams = QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM),
                limit = 1
            )
        ).thenReturn(flowOf(emptyList()))

        // when
        val randomQuoteFlow = dependencyManager.repository.randomQuote

        // then
        assertThat(randomQuoteFlow.toList()).isEqualTo(listOf(null))
    }

    @Test
    fun given_LocalDataAvailable_when_GetQuote_then_ReturnFlowWithQuote() = runTest {
        // given
        val quoteId = "1"
        val quoteEntity = QuoteEntity(id = quoteId, content = "content")

        whenever(dependencyManager.localDataSource.getQuoteFlow(id = quoteId))
            .thenReturn(flowOf(quoteEntity))

        // when
        val quoteFlow = dependencyManager.repository.getQuoteFlow(id = quoteId)

        // then
        assertThat(quoteFlow.single()).isEqualTo(quoteEntity.toDomain())
    }

    @Test
    fun given_NoLocalDataAvailable_when_GetQuote_thenReturnFlowWithNull() = runTest {
        // given
        val quoteId = "1"
        whenever(dependencyManager.localDataSource.getQuoteFlow(quoteId))
            .thenReturn(flowOf(null))

        // when
        val quoteFlow = dependencyManager.repository.getQuoteFlow(id = quoteId)

        // then
        assertThat(quoteFlow.toList()).isEqualTo(listOf(null))
    }

    @Test
    fun given_RemoteAPIWorking_when_getRandomQuote_then_ReturnValidQuote() = runTest {
        // given
        val quoteDTO = QuotesFactory.getDTOs(1).first()

        whenever(dependencyManager.remoteService.fetchRandom())
            .thenReturn(Result.success(quoteDTO))

        // when
        val response = dependencyManager.repository.getRandomQuote()

        // then
        assertThat(response.isSuccess).isTrue()
        assertThat(response.getOrNull()).isEqualTo(quoteDTO.toDomain())
        verify(dependencyManager.localDataSource, times(1)).insert(listOf(quoteDTO.toDb()))
    }

    @Test
    fun given_RemoteAPINotWorking_when_getRandomQuote_then_ReturnFailure() = runTest {
        // given
        whenever(dependencyManager.remoteService.fetchRandom())
            .thenReturn(Result.failure(IOException()))

        // when
        val response = dependencyManager.repository.getRandomQuote()

        // then
        assertThat(response.isFailure).isTrue()
    }

}