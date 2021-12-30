package com.example.quotableapp.ui.onequote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.quotableapp.R
import com.example.quotableapp.ui.common.helpers.setNavIntentExtras
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OneQuoteHostActivity : AppCompatActivity(R.layout.activity_base_host) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNavIntentExtras(R.id.nav_host_fragment, R.navigation.onequote_nav_graph)
    }
}