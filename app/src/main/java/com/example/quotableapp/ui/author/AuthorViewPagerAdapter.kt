package com.example.quotableapp.ui.author

import androidx.fragment.app.Fragment
import androidx.paging.ExperimentalPagingApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlin.time.ExperimentalTime

@FlowPreview
@ExperimentalTime
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class AuthorViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    companion object {
        const val TABS_NUM = 2
    }

    override fun getItemCount(): Int {
        return TABS_NUM
    }

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> AuthorDetailsFragment()
            1 -> AuthorQuotesFragment()
            else -> throw RuntimeException("unsupported position no. $position")
        }
}