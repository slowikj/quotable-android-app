package com.example.quotableapp.data.repository.quotes.quoteslist.paging

import androidx.paging.*
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.QuotesFactory
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.getFakeApiResponseInterpreter
import com.example.quotableapp.data.getTestCoroutineDispatchers
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@ExperimentalPagingApi
class QuotesRemoteMediatorTest {

    class DependencyManager(
        val persistenceManager: QuotesListPersistenceManager = mock(),
        val cacheTimeoutMillis: Long = 100,
        var remoteService: IntPagedRemoteService<QuotesResponseDTO>? = null,
        val apiResultInterpreter: ApiResponseInterpreter = getFakeApiResponseInterpreter(),
        val dtoToEntityConverter: Converter<QuotesResponseDTO, List<QuoteEntity>> = mock(),
        val coroutineDispatchers: CoroutineDispatchers = getTestCoroutineDispatchers()
    ) {
        val mediator: QuotesRemoteMediator
            get() = QuotesRemoteMediator(
                persistenceManager = persistenceManager,
                cacheTimeoutMilliseconds = cacheTimeoutMillis,
                remoteService = remoteService!!,
                apiResultInterpreter = apiResultInterpreter,
                dtoToEntityConverter = dtoToEntityConverter,
                coroutineDispatchers = coroutineDispatchers
            )
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_WorkingRemoteAPIWithNewDataAndNoPreviousPageKey_when_loadRefresh_then_ReturnSuccessWithNoEndReached() =
        runBlocking {
            runTestWorkingAPIWithDataFor(
                loadType = LoadType.REFRESH,
                pageKey = null,
                lastUpdated = null
            )
            verify(dependencyManager.persistenceManager, times(1)).refresh(any(), eq(1))
        }

    @Test
    fun given_WorkingRemoteAPIWithNewDataAndExistingPreviousPageKey_when_loadAppend_then_ReturnSuccessWithNoEndReached() =
        runBlocking {
            val previousPageKey = 1
            runTestWorkingAPIWithDataFor(
                loadType = LoadType.APPEND,
                pageKey = previousPageKey,
                lastUpdated = 1233
            )
            verify(dependencyManager.persistenceManager, times(1)).append(
                any(),
                eq(previousPageKey + 1)
            )
        }

    @Test
    fun given_WorkingRemoteAPIWithNoDataNoPreviousPageKey_when_loadRefresh_then_ReturnSuccessWithEndReached() =
        runBlocking {
            runTestWorkingAPIWithNoDataFor(
                loadType = LoadType.REFRESH,
                pageKey = null,
                lastUpdated = null
            )
        }

    @Test
    fun given_WorkingRemoteAPIWithNoDataAndExistingPreviousPageKey_when_loadAppend_then_ReturnSuccessWithEndReached() =
        runBlocking {
            val previousPageKey = 1
            runTestWorkingAPIWithNoDataFor(
                loadType = LoadType.APPEND,
                pageKey = previousPageKey,
                lastUpdated = 123
            )
        }

    @Test
    fun given_NotWorkingRemoteAPIAndExistingPreviousPageKey_when_loadRefresh_then_ReturnFailure() =
        runBlocking {
            runTestForNotWorkingAPI(loadType = LoadType.REFRESH, pageKey = 1, lastUpdated = 123)
        }

    @Test
    fun given_NotWorkingRemoteAPI_when_loadAppend_then_ReturnFailure() = runBlocking {
        runTestForNotWorkingAPI(loadType = LoadType.APPEND, pageKey = 1, lastUpdated = 123)
    }

    private fun getEmptyPagingState(pageSize: Int) = PagingState<Int, QuoteEntity>(
        pages = listOf(),
        anchorPosition = null,
        config = PagingConfig(pageSize = pageSize, initialLoadSize = pageSize),
        leadingPlaceholderCount = pageSize
    )

    private fun mockSuccessfulAPIWithDataAndProperConverters(dataSize: Int) {
        val responseDTO = QuotesFactory.getResponseDTO(size = dataSize)

        dependencyManager.remoteService = { page: Int, limit: Int ->
            Response.success(responseDTO)
        }

        val quoteEntities = responseDTO.results.map { QuoteEntity(id = it.id) }
        whenever(dependencyManager.dtoToEntityConverter.invoke(responseDTO))
            .thenReturn(quoteEntities)
    }

    private suspend fun runTestWorkingAPIWithNoDataFor(
        loadType: LoadType,
        pageKey: Int?,
        lastUpdated: Long?
    ) {
        val pageSize = 10
        mockSuccessfulAPIWithDataAndProperConverters(dataSize = 0)
        mockPersistenceManagerRemotePageKey(lastUpdated = lastUpdated, pageKey = pageKey)
        val pagingState = getEmptyPagingState(pageSize = pageSize)

        // when
        val mediatorResult = dependencyManager.mediator.load(
            loadType = loadType,
            state = pagingState
        )

        // then
        assertThat(mediatorResult is RemoteMediator.MediatorResult.Success).isTrue()
        assertThat((mediatorResult as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
    }

    private suspend fun runTestWorkingAPIWithDataFor(
        loadType: LoadType,
        pageKey: Int?,
        lastUpdated: Long?
    ) {
        // given
        val pageSize = 10
        mockSuccessfulAPIWithDataAndProperConverters(dataSize = pageSize)
        mockPersistenceManagerRemotePageKey(pageKey = pageKey, lastUpdated = lastUpdated)
        val pagingState = getEmptyPagingState(pageSize = pageSize)

        // when
        val mediatorResult = dependencyManager.mediator.load(
            loadType = loadType,
            state = pagingState
        )

        // then
        assertThat(mediatorResult is RemoteMediator.MediatorResult.Success).isTrue()
        assertThat((mediatorResult as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isFalse()
    }

    private suspend fun runTestForNotWorkingAPI(
        loadType: LoadType,
        pageKey: Int?,
        lastUpdated: Long?
    ) {
        // given
        val pageSize = 10
        mockUnsuccessfulAPI()
        mockPersistenceManagerRemotePageKey(pageKey = pageKey, lastUpdated = lastUpdated)
        val pagingState = getEmptyPagingState(pageSize = pageSize)

        // when
        val mediatorResult = dependencyManager.mediator.load(
            loadType = loadType,
            state = pagingState
        )

        // then
        assertThat(mediatorResult is RemoteMediator.MediatorResult.Error).isTrue()
    }

    private suspend fun mockUnsuccessfulAPI() {
        dependencyManager.remoteService = { page: Int, limit: Int ->
            throw IOException()
        }
    }

    private suspend fun mockPersistenceManagerRemotePageKey(pageKey: Int?, lastUpdated: Long?) {
        whenever(dependencyManager.persistenceManager.getLastUpdated())
            .thenReturn(lastUpdated)

        whenever(dependencyManager.persistenceManager.getLatestPageKey())
            .thenReturn(pageKey)
    }

}