package com.alilopez.kt_demohilt.features.feature01.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class HomeViewModel @Inject constructor(): ViewModel() {
    private val _msn = MutableStateFlow("Home Screen")
    val msn = _msn.asStateFlow()
}