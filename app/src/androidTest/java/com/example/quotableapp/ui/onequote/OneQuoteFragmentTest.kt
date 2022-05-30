package com.example.quotableapp.ui.onequote

import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.quotableapp.R
import com.example.quotableapp.data.launchFragmentInHiltContainer
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.ui.common.rvAdapters.QuoteTagsAdapter
import com.google.common.truth.Truth
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class OneQuoteFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    val mockViewModel = mockk<OneQuoteViewModel>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun test_quoteContentIsDisplayed() {
        // given
        val quote = Quote(
            id = "123",
            content = "XXX",
            author = "YYY",
            authorSlug = "yyy",
            tags = listOf()
        )
        mockViewModelToReturnAQuote(quote)

        // when
        val scenario = launchFragmentInHiltContainer<OneQuoteFragment>(
            fragmentArgs = bundleOf("quote" to quote)
        ) {
        }

        // then
        onView(withId(R.id.tv_content)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_content)).check(matches(withText(quote.content)))
        onView(withId(R.id.tv_author)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_author)).check(matches(withText(quote.author)))
    }

    private fun mockViewModelToReturnAQuote(quote: Quote) {
        val quoteUi = QuoteUi(
            quote = quote,
            authorPhotoUrl = null
        )

        every { mockViewModel.quoteUiState } returns MutableStateFlow(
            QuoteUiState(
                isLoading = false,
                error = null,
                data = quoteUi
            )
        )
    }

    @Test
    fun when_ClickOnAuthor_then_NavigateToAuthorFragment() {
        val quote = Quote(
            id = "123",
            content = "XXX",
            author = "YYY",
            authorSlug = "yyy",
            tags = listOf()
        )

        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )

        val scenario = launchFragmentInHiltContainer<OneQuoteFragment>(
            fragmentArgs = bundleOf("quote" to quote)
        ) {
            navController.setGraph(R.navigation.main_nav_graph)
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.tv_author)).perform(click())

        Truth.assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.authorFragment)
    }

    @Test
    fun when_ClickOnTag_then_NavigateToTagFragment() {
        val quote = Quote(
            id = "123",
            content = "XXX",
            author = "YYY",
            authorSlug = "yyy",
            tags = listOf("A", "B")
        )

        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )

        val scenario = launchFragmentInHiltContainer<OneQuoteFragment>(
            fragmentArgs = bundleOf("quote" to quote)
        ) {
            navController.setGraph(R.navigation.main_nav_graph)
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.rv_tags))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<QuoteTagsAdapter.ViewHolder>(
                    1,
                    click()
                )
            )

        Truth.assertThat(navController.currentDestination?.id)
            .isEqualTo(R.id.tagsListFragment)
    }
}