package com.tapbi.spark.voiceai.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.data.dao.PageTemplateDao
import com.tapbi.spark.voiceai.data.model.ListPageTemplate
import com.tapbi.spark.voiceai.data.model.PageTemplateUpdate
import com.tapbi.spark.voiceai.data.model.ResultListPageTemplate
import com.tapbi.spark.voiceai.data.preferences.PrefKeys
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.data.remote.ThemeApi
import com.tapbi.spark.ui_common_sdk.components.ContentState
import javax.inject.Inject

class ThemeRepository @Inject constructor(
    val mPrefHelper: PreferencesDataStoreHelper,
    val pageTemplateDao: PageTemplateDao,
    val themeApi: ThemeApi
) {

    suspend fun getThemes(): ResultListPageTemplate {
        try {
            val listPageTemplate = pageTemplateDao.getAllList()
            val versionGetData = FirebaseRemoteConfig.getInstance()
                .getLong(Constant.FIREBASE_REMOTE_VERSION_UPDATE_THEME)
            val currentVersionGetData =
                mPrefHelper.getLong(PrefKeys.FIREBASE_REMOTE_VERSION_UPDATE_THEME, 0L)

            if (listPageTemplate.isEmpty() || currentVersionGetData < versionGetData) {
                val data = getUpdateTheme(currentVersionGetData)
                data?.apply {
                    pageTemplateDao.insert(list)
                    val dataCheckChange = dataCheckChange
                    mPrefHelper.putLong(
                        PrefKeys.FIREBASE_REMOTE_VERSION_UPDATE_THEME,
                        dataCheckChange.version.toLong()
                    )
                }
            }
            return getListTheme()
        } catch (_: Exception) {
            return ResultListPageTemplate(ContentState.ERROR, listOf())
        }
    }

    private suspend fun getListTheme(): ResultListPageTemplate {
        try {
            val listCheck = pageTemplateDao.getAllList()
            if (listCheck.isNotEmpty()) {
                return ResultListPageTemplate(ContentState.LOADED, listCheck)
            } else {
                val data = getThemeFromServer()
                return if (data != null) {
                    pageTemplateDao.insert(data.list)
                    val sortedList = data.list.sortedByDescending { it.id }
                    ResultListPageTemplate(ContentState.LOADED, sortedList)
                } else {
                    ResultListPageTemplate(ContentState.ERROR, listOf())
                }
            }
        } catch (_: Exception) {
            return ResultListPageTemplate(ContentState.ERROR, listOf())
        }
    }

    private suspend fun getUpdateTheme(version: Long): PageTemplateUpdate? {
        return try {
            val xApiKey =
                FirebaseRemoteConfig.getInstance().getString(Constant.FIREBASE_CONFIG_X_API_KEY)
            themeApi.getUpdateTheme(xApiKey, mapOf("version" to version.toInt()))
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getThemeFromServer(): ListPageTemplate? {
        return try {
            val xApiKey =
                FirebaseRemoteConfig.getInstance().getString(Constant.FIREBASE_CONFIG_X_API_KEY)
            themeApi.getThemes(xApiKey)
        } catch (_: Exception) {
            null
        }
    }
}