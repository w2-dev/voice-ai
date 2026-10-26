package com.tapbi.spark.voiceai.ui.main.language

import androidx.lifecycle.viewModelScope
import com.tapbi.spark.voiceai.ads.feature.AdsUiState
import com.tapbi.spark.voiceai.App
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.navigation.MainNavigator
import com.tapbi.spark.voiceai.ui.base.BaseViewModel
import com.tapbi.spark.voiceai.utils.LocaleUtils
import com.tapbi.spark.ui_common_sdk.components.ContentState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val navigation: MainNavigator,
    private val preferences: PreferencesDataStoreHelper,
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(LanguageUiState.EMPTY)
    val uiState = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            val listLanguages = LocaleUtils.getLanguages(App.instance)
            val locale =
                preferences.getString(PrefKeys.CURRENT_LANGUAGE)
            _uiState.update {
                it.copy(
                    contentState = ContentState.LOADED,
                    data = listLanguages,
                    selectedLocale = locale
                )
            }
        }
    }

    fun updateSelectedLocale(locale: String) {
        _uiState.update {
            it.copy(selectedLocale = locale)
        }
    }

    fun setLanguage(locale: String) {
        viewModelScope.launch {
            preferences.putString(PrefKeys.CURRENT_LANGUAGE, locale)
        }
    }

    fun finishSetLanguage(isFromSplash: Boolean) {
        navigateToOnboarding()
    }

    internal fun navigateToOnboarding() {
        navigation.openOnboardingFromLanguage()
    }

    internal fun finishApp() {
        navigation.finishApp()
    }

    internal fun closeAdsFull(){
        _uiState.update { it.copy(adsUiState = AdsUiState(false, "")) }
    }
}
