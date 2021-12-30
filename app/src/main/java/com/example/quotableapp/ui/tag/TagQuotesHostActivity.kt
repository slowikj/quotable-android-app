package com.example.quotableapp.ui.tag

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.quotableapp.R
import com.example.quotableapp.ui.common.helpers.setNavIntentExtras
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagQuotesHostActivity : AppCompatActivity(R.layout.activity_base_host) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNavIntentExtras(R.id.nav_host_fragment, R.navigation.tag_quotes_nav_graph)
    }
}