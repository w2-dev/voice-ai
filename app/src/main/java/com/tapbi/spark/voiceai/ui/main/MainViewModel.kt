package com.tapbi.spark.voiceai.ui.main

import androidx.lifecycle.viewModelScope
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(preferences: PreferencesDataStoreHelper) :
    BaseViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.EMPTY)
    val uiState = _uiState.asStateFlow()
    init {
        preferences
            .getFlowString(PrefKeys.CURRENT_LANGUAGE)
            .onEach { languageCode ->
                _uiState.update {
                    it.copy(currentLocale = Locale.forLanguageTag(languageCode))
                }
            }
            .catch {
                _uiState.update {
                    it.copy(currentLocale = Locale.ENGLISH)
                }
            }
            .launchIn(viewModelScope)
    }
    fun updatePremium(value: Boolean) {
        _uiState.update { it.copy(isPro = value) }
    }

    fun updateWhenPurchased(value: Boolean) {
        _uiState.update { it.copy(onPurchased = value) }
    }
}