package com.example.quotableapp.ui.common.extensions

import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

fun AppCompatActivity.setNavIntentExtras(
    @IdRes fragmentContainerId: Int,
    @NavigationRes navGraphId: Int
) {
    val navHostFragment =
        supportFragmentManager.findFragmentById(fragmentContainerId) as NavHostFragment
    navHostFragment
        .navController
        .setGraph(navGraphId, intent.extras)
}