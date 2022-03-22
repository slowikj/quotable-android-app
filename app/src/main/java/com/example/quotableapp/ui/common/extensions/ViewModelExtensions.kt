package com.example.quotableapp.ui.common.extensions

import kotlinx.coroutines.flow.SharingStarted

val defaultSharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5000)