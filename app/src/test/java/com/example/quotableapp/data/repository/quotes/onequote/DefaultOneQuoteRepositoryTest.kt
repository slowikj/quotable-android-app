package com.example.quotableapp.data.repository.quotes.onequote

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.QuotesFactory
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.getFakeApiResponseInterpreter
import com.example.quotableapp.data.getTestCoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.services.QuotesRemoteService
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
import org.mockito.ArgumentMatchers.anyLong
import retrofit2.Response
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class DefaultOneQuoteRepositoryTest {

    class DependencyManager(
        val coroutineDispatchers: CoroutineDispatchers = getTestCoroutineDispatchers(),
        val remoteService: QuotesRemoteService = mock(),
        val converters: QuoteConverters = mock(),
        val localDataSource: QuotesLocalDataSource = mock(),
        val apiResponseInterpreter: ApiResponseInterpreter = getFakeApiResponseInterpreter()
    ) {
        val repository: DefaultOneQuoteRepository
            get() = DefaultOneQuoteRepository(
                coroutineDispatchers = coroutineDispatchers,
                quotesRemoteService = remoteService,
                quoteConverters = converters,
                quotesLocalDataSource = localDataSource,
                apiResponseInterpreter = apiResponseInterpreter
            )
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_NoAPIConnection_when_UpdateRandomQuote_then_ReturnResultFailure() = runBlockingTest {
        // given
        whenever(dependencyManager.remoteService.fetchRandomQuote()).thenReturn(
            Response.error(404, "".toResponseBody())
        )

        // when
        val res = dependencyManager.repository.updateRandomQuote()

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_UpdateRandomQuote_then_ReturnResultSuccess() =
        runBlockingTest {
            // given
            val randomQuote = QuoteDTO(
                id = "1", content = "random content"
            )
            whenever(dependencyManager.remoteService.fetchRandomQuote()).thenReturn(
                Response.success(randomQuote)
            )

            val quoteEntity = QuoteEntity(id = randomQuote.id, content = randomQuote.content)
            whenever(dependencyManager.converters.toDb(any()))
                .thenReturn(quoteEntity)

            // when
            val res = dependencyManager.repository.updateRandomQuote()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(
                dependencyManager.localDataSource, times(1)
            ).refresh(
                entities = eq(listOf(quoteEntity)),
                originParams = eq(QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)),
                lastUpdatedMillis = anyLong()
            )
        }

    @Test
    fun given_NoAPIConnection_when_updateQuote_then_ReturnFailure() = runBlockingTest {
        // given
        val quoteId = "1"
        whenever(dependencyManager.remoteService.fetchQuote(quoteId)).thenReturn(
            Response.error(
                404,
                "".toResponseBody()
            )
        )

        // when
        val res = dependencyManager.repository.updateQuote(id = quoteId)

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_UpdateQuote_then_ReturnSuccess() = runBlockingTest {
        // given
        val quoteId = "1"
        val quoteDTO = QuoteDTO(id = quoteId, content = "abc")
        val quoteEntity = QuoteEntity(id = quoteId, content = "abc")
        whenever(dependencyManager.remoteService.fetchQuote(quoteDTO.id)).thenReturn(
            Response.success(quoteDTO)
        )
        whenever(dependencyManager.converters.toDb(quoteDTO)).thenReturn(quoteEntity)

        // when
        val res = dependencyManager.repository.updateQuote(quoteId)

        // then
        assertThat(res.isSuccess).isTrue()
        verify(dependencyManager.localDataSource, times(1)).insert(entities = listOf(quoteEntity))
    }

    @Test
    fun given_LocalDataAvailable_when_GetRandomQuote_then_ReturnFlowWithQuote() = runBlockingTest {
        // given
        val quoteEntity = QuoteEntity(id = "1", content = "content")
        val quote = Quote(id = "1", content = "content")
        whenever(
            dependencyManager.localDataSource.getFirstQuotesSortedById(
                originParams = QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM),
                limit = 1
            )
        ).thenReturn(flowOf(listOf(quoteEntity)))

        whenever(dependencyManager.converters.toDomain(quoteEntity))
            .doReturn(quote)

        // when
        val randomQuoteFlow = dependencyManager.repository.randomQuote

        // then
        assertThat(randomQuoteFlow.single()).isEqualTo(quote)
    }

    @Test
    fun given_NoLocalDataAvailable_when_GetRandomQuote_then_ReturnFlowWithNoEmission() =
        runBlockingTest {
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
            assertThat(randomQuoteFlow.count()).isEqualTo(0)
        }

    @Test
    fun given_LocalDataAvailable_when_GetQuote_then_ReturnFlowWithQuote() = runBlockingTest {
        // given
        val quoteId = "1"
        val quoteEntity = QuoteEntity(id = quoteId, content = "content")
        val quote = Quote(id = quoteId, content = "content")

        whenever(dependencyManager.localDataSource.getQuoteFlow(id = quoteId))
            .thenReturn(flowOf(quoteEntity))

        whenever(dependencyManager.converters.toDomain(any<QuoteEntity>()))
            .thenReturn(quote)

        // when
        val quoteFlow = dependencyManager.repository.getQuoteFlow(id = quoteId)

        // then
        assertThat(quoteFlow.single()).isEqualTo(quote)
    }

    @Test
    fun given_NoLocalDataAvailable_when_GetQuote_thenReturnFlowWithNoEmission() = runBlockingTest {
        // given
        val quoteId = "1"
        whenever(dependencyManager.localDataSource.getQuoteFlow(quoteId))
            .thenReturn(flowOf(null))

        // when
        val quoteFlow = dependencyManager.repository.getQuoteFlow(id = quoteId)

        // then
        assertThat(quoteFlow.count()).isEqualTo(0)
    }

    @Test
    fun given_RemoteAPIWorking_when_getRandomQuote_then_ReturnValidQuote() = runBlockingTest {
        // given
        val quoteDTO = QuotesFactory.getDTOs(1).first()
        val quoteEntity = QuotesFactory.getEntities(1).first()
        val quote = QuotesFactory.getQuotes(1).first()

        whenever(dependencyManager.converters.toDomain(quoteDTO))
            .thenReturn(quote)
        whenever(dependencyManager.converters.toDb(quoteDTO))
            .thenReturn(quoteEntity)

        whenever(dependencyManager.remoteService.fetchRandomQuote())
            .thenReturn(Response.success(quoteDTO))

        // when
        val response = dependencyManager.repository.getRandomQuote()

        // then
        assertThat(response.isSuccess).isTrue()
        assertThat(response.getOrNull()).isEqualTo(quote)
        verify(dependencyManager.localDataSource, times(1)).insert(listOf(quoteEntity))
    }

    @Test
    fun given_RemoteAPINotWorking_when_getRandomQuote_then_ReturnFailure() = runBlockingTest {
        // given
        whenever(dependencyManager.remoteService.fetchRandomQuote())
            .thenReturn(Response.error(500, "".toResponseBody()))

        // when
        val response = dependencyManager.repository.getRandomQuote()

        // then
        assertThat(response.isFailure).isTrue()
    }
}