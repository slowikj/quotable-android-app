package com.example.quotableapp.data.paging

import androidx.paging.*
import com.example.quotableapp.data.DataTestUtil
import com.example.quotableapp.data.networking.QuotesService
import com.example.quotableapp.data.networking.model.QuoteDTO
import com.example.quotableapp.data.networking.model.QuotesResponseDTO
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner::class)
class QuotesRemoteMediatorTest {

    @Test
    fun mediatorReturnsErrorWhenApiThrowsIOException(): Unit = runBlocking {
        val mediator = prepareRemoteMediator(object : QuotesService {
            override suspend fun fetchQuotes(page: Int, limit: Int): QuotesResponseDTO {
                throw IOException()
            }
        })

        val res = mediator.load(
            LoadType.REFRESH,
            PagingState(listOf(), null, PagingConfig(10), 10)
        )
        assertTrue(res is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun mediatorReturnsSuccessWithoutEndReached() = runBlocking {
        val mediator = prepareRemoteMediator(object : QuotesService {
            override suspend fun fetchQuotes(page: Int, limit: Int): QuotesResponseDTO {
                return QuotesResponseDTO(10, 100, 1, 10, 10,
                    List(10) { QuoteDTO(it.toString(), "", "", "", 0, emptyList()) })
            }
        })

        val res = mediator.load(
            LoadType.REFRESH,
            PagingState(listOf(), null, PagingConfig(10), 10)
        )

        assertTrue(
            res is RemoteMediator.MediatorResult.Success
                    && !res.endOfPaginationReached
        )
    }

    private fun prepareRemoteMediator(api: QuotesService) =
        QuotesRemoteMediator(
            database = DataTestUtil.prepareInMemoryDatabase(),
            remoteService = api
        )
}