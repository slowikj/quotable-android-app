//package com.example.quotableapp.usecases.authors
//
//import com.example.quotableapp.data.AuthorsFactory
//import com.example.quotableapp.data.converters.toDb
//import com.example.quotableapp.data.converters.toDomain
//import com.example.quotableapp.data.remote.datasources.FetchAuthorsListParams
//import com.example.quotableapp.data.remote.services.AuthorsRemoteService
//import com.example.quotableapp.data.repository.authors.DefaultAuthorsRepository
//import com.google.common.truth.Truth
//import com.nhaarman.mockitokotlin2.*
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert.*
//import org.junit.Test
//import org.mockito.ArgumentMatchers
//import java.io.IOException
//
//@ExperimentalCoroutinesApi
//class GetExemplaryAuthorsUseCaseTest {
//
//    @Test
//    fun given_WorkingAPIConnection_when_updateExemplaryAuthors_then_ReturnSuccess(): Unit =
//        runTest {
//            // given
//            val authorResponseSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
//            val authorResponseDTO = AuthorsFactory.getResponseDTO(size = authorResponseSize)
//            whenever(
//                dependencyManager.remoteDataSource.fetch(
//                    FetchAuthorsListParams(
//                        page = 1,
//                        limit = authorResponseSize,
//                        sortBy = AuthorsRemoteService.SortByType.QuoteCount,
//                        orderType = AuthorsRemoteService.OrderType.Desc
//                    )
//                )
//            ).thenReturn(Result.success(authorResponseDTO))
//
//            val authorEntities =
//                authorResponseDTO.results.map { it.toDb() }
//
//            // when
//            val res = dependencyManager.repository.updateExemplaryAuthors()
//
//            // then
//            Truth.assertThat(res.isSuccess).isTrue()
//            verify(dependencyManager.localDataSource, times(1))
//                .refresh(eq(authorEntities), any(), ArgumentMatchers.anyLong())
//        }
//
//    @Test
//    fun given_NoAPIConnection_when_updateExemplaryAuthors_then_ReturnFailure() = runTest {
//        // given
//        val authorResponseSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
//        whenever(
//            dependencyManager.remoteDataSource.fetch(
//                FetchAuthorsListParams(
//                    page = 1,
//                    limit = authorResponseSize,
//                    sortBy = AuthorsRemoteService.SortByType.QuoteCount,
//                    orderType = AuthorsRemoteService.OrderType.Desc
//                )
//            )
//        ).thenReturn(Result.failure(IOException()))
//
//        // when
//        val res = dependencyManager.repository.updateExemplaryAuthors()
//
//        // then
//        Truth.assertThat(res.isFailure).isTrue()
//        verify(dependencyManager.localDataSource, never())
//            .insert(any())
//    }
//
//    @Test
//    fun given_AvailableLocalData_when_getExemplaryAuthorsFlow_then_returnFlowWithData() =
//        runTest {
//            // given
//            val originParams = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_ORIGIN_PARAMS
//            val entitiesSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
//            val authorEntities = AuthorsFactory.getEntities(size = entitiesSize)
//            whenever(
//                dependencyManager.localDataSource
//                    .getAuthorsSortedByQuoteCountDesc(
//                        originParams = originParams,
//                        limit = entitiesSize
//                    )
//            ).thenReturn(flowOf(authorEntities))
//
//            val authors = authorEntities.map { it.toDomain() }
//
//            // when
//            val authorsFlow = dependencyManager.repository.exemplaryAuthorsFlow
//
//            // then
//            Truth.assertThat(authorsFlow.single()).isEqualTo(authors)
//
//        }
//
//    @Test
//    fun given_NoLocalData_when_getExemplaryAuthorsFlow_then_returnFlowWithNoEmission() =
//        runTest {
//            // given
//            val originParams = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_ORIGIN_PARAMS
//            val entitiesSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
//            whenever(
//                dependencyManager.localDataSource
//                    .getAuthorsSortedByQuoteCountDesc(
//                        originParams = originParams,
//                        limit = entitiesSize
//                    )
//            ).thenReturn(flowOf(emptyList()))
//
//            // when
//            val authorsFlow = dependencyManager.repository.exemplaryAuthorsFlow
//
//            // then
//            Truth.assertThat(authorsFlow.count()).isEqualTo(0)
//        }
//}