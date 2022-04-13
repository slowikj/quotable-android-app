package com.example.quotableapp.data.repository.quotes.quoteslist.paging

import androidx.paging.*
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.QuotesFactory
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.getTestdispatchersProvider
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteDataSource
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalPagingApi
class QuotesRemoteMediatorTest {

    @get:Rule
    val mainCoroutineDispatcherRule =  MainCoroutineDispatcherRule()

    class DependencyManager(
        val persistenceManager: QuotesListPersistenceManager = mock(),
        val cacheTimeoutMillis: Long = 100,
        var remoteDataSource: IntPagedRemoteDataSource<QuotesResponseDTO>? = null,
        val dtoToEntityConverter: Converter<QuotesResponseDTO, List<QuoteEntity>> = mock(),
        val dispatchersProvider: DispatchersProvider = getTestdispatchersProvider()
    ) {
        val mediator: QuotesRemoteMediator
            get() = QuotesRemoteMediator(
                persistenceManager = persistenceManager,
                cacheTimeoutMilliseconds = cacheTimeoutMillis,
                remoteDataSource = remoteDataSource!!,
                dtoToEntityConverter = dtoToEntityConverter,
                dispatchersProvider = dispatchersProvider
            )
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_WorkingRemoteAPIWithNewDataAndNoPreviousPageKey_when_loadRefresh_then_ReturnSuccessWithNoEndReached() =
        runTest {
            runTestWorkingAPIWithDataFor(
                loadType = LoadType.REFRESH,
                pageKey = null,
                lastUpdated = null
            )
            verify(dependencyManager.persistenceManager, times(1)).refresh(any(), eq(1))
        }

    @Test
    fun given_WorkingRemoteAPIWithNewDataAndExistingPreviousPageKey_when_loadAppend_then_ReturnSuccessWithNoEndReached() =
        runTest {
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
        runTest {
            runTestWorkingAPIWithNoDataFor(
                loadType = LoadType.REFRESH,
                pageKey = null,
                lastUpdated = null
            )
        }

    @Test
    fun given_WorkingRemoteAPIWithNoDataAndExistingPreviousPageKey_when_loadAppend_then_ReturnSuccessWithEndReached() =
        runTest {
            val previousPageKey = 1
            runTestWorkingAPIWithNoDataFor(
                loadType = LoadType.APPEND,
                pageKey = previousPageKey,
                lastUpdated = 123
            )
        }

    @Test
    fun given_NotWorkingRemoteAPIAndExistingPreviousPageKey_when_loadRefresh_then_ReturnFailure() =
        runTest {
            runTestForNotWorkingAPI(loadType = LoadType.REFRESH, pageKey = 1, lastUpdated = 123)
        }

    @Test
    fun given_NotWorkingRemoteAPI_when_loadAppend_then_ReturnFailure() = runTest {
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

        dependencyManager.remoteDataSource = { page: Int, limit: Int ->
            Result.success(responseDTO)
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
        dependencyManager.remoteDataSource = { page: Int, limit: Int ->
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