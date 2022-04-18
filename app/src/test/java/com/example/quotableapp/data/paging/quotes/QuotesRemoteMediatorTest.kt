package com.example.quotableapp.data.paging.quotes

import androidx.paging.*
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.Converter
import com.example.quotableapp.data.local.entities.quote.QuoteEntity
import com.example.quotableapp.data.remote.model.QuotesResponseDTO
import com.example.quotableapp.fakes.FakeIntPagedRemoteDataSource
import com.example.quotableapp.fakes.factories.QuotesFactory
import com.example.quotableapp.fakes.getTestDispatchersProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
@ExperimentalPagingApi
class QuotesRemoteMediatorTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    private lateinit var persistenceManager: QuotesListPersistenceManager

    private lateinit var remoteDataSource: FakeIntPagedRemoteDataSource<QuotesResponseDTO>

    private lateinit var dtoToEntityConverter: Converter<QuotesResponseDTO, List<QuoteEntity>>

    private val cacheTimeoutMillis: Long = 100L

    @Before
    fun setUp() {
        persistenceManager = mock()
        remoteDataSource = FakeIntPagedRemoteDataSource()
        dtoToEntityConverter = mock()
    }

    @Test
    fun given_WorkingRemoteAPIWithNewDataAndNoPreviousPageKey_when_loadRefresh_then_ReturnSuccessWithNoEndReached() =
        runTest {
            runTestWorkingAPIWithDataFor(
                loadType = LoadType.REFRESH,
                pageKey = null,
                lastUpdated = null
            )
            verify(persistenceManager, times(1)).refresh(any(), eq(1))
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
            verify(persistenceManager, times(1)).append(
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

        remoteDataSource.completableDeferred
            .complete(Result.success(responseDTO))

        val quoteEntities = responseDTO.results.map { QuoteEntity(id = it.id) }
        whenever(dtoToEntityConverter.invoke(responseDTO))
            .thenReturn(quoteEntities)
    }

    private suspend fun TestScope.runTestWorkingAPIWithNoDataFor(
        loadType: LoadType,
        pageKey: Int?,
        lastUpdated: Long?
    ) {
        val pageSize = 10
        mockSuccessfulAPIWithDataAndProperConverters(dataSize = 0)
        mockPersistenceManagerRemotePageKey(lastUpdated = lastUpdated, pageKey = pageKey)
        val pagingState = getEmptyPagingState(pageSize = pageSize)

        val mediator = createRemoteMediator(this)

        // when
        val mediatorResult = mediator.load(
            loadType = loadType,
            state = pagingState
        )

        // then
        assertThat(mediatorResult is RemoteMediator.MediatorResult.Success).isTrue()
        assertThat((mediatorResult as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
    }

    private suspend fun TestScope.runTestWorkingAPIWithDataFor(
        loadType: LoadType,
        pageKey: Int?,
        lastUpdated: Long?
    ) {
        // given
        val pageSize = 10
        mockSuccessfulAPIWithDataAndProperConverters(dataSize = pageSize)
        mockPersistenceManagerRemotePageKey(pageKey = pageKey, lastUpdated = lastUpdated)
        val pagingState = getEmptyPagingState(pageSize = pageSize)

        val mediator = createRemoteMediator(this)

        // when
        val mediatorResult = mediator.load(
            loadType = loadType,
            state = pagingState
        )

        // then
        assertThat(mediatorResult is RemoteMediator.MediatorResult.Success).isTrue()
        assertThat((mediatorResult as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isFalse()
    }

    private suspend fun TestScope.runTestForNotWorkingAPI(
        loadType: LoadType,
        pageKey: Int?,
        lastUpdated: Long?
    ) {
        // given
        val pageSize = 10
        mockUnsuccessfulAPI()
        mockPersistenceManagerRemotePageKey(pageKey = pageKey, lastUpdated = lastUpdated)
        val pagingState = getEmptyPagingState(pageSize = pageSize)

        val mediator = createRemoteMediator(this)


        // when
        val mediatorResult = mediator.load(
            loadType = loadType,
            state = pagingState
        )

        // then
        assertThat(mediatorResult is RemoteMediator.MediatorResult.Error).isTrue()
    }

    private suspend fun mockUnsuccessfulAPI() {
        remoteDataSource.completableDeferred
            .completeExceptionally(IOException())
    }

    private suspend fun mockPersistenceManagerRemotePageKey(pageKey: Int?, lastUpdated: Long?) {
        whenever(persistenceManager.getLastUpdated())
            .thenReturn(lastUpdated)

        whenever(persistenceManager.getLatestPageKey())
            .thenReturn(pageKey)
    }

    private fun createRemoteMediator(testScope: TestScope): QuotesRemoteMediator {
        return QuotesRemoteMediator(
            persistenceManager = persistenceManager,
            cacheTimeoutMilliseconds = cacheTimeoutMillis,
            remoteDataSource = remoteDataSource,
            dtoToEntityConverter = dtoToEntityConverter,
            dispatchersProvider = testScope.getTestDispatchersProvider()
        )
    }

}