package com.tapbi.spark.voiceai.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.data.dao.PageTemplateDao
import com.tapbi.spark.voiceai.data.database.AppDatabase
import com.tapbi.spark.voiceai.data.network.RetrofitClient
import com.tapbi.spark.voiceai.data.preferences.PreferencesDataStoreHelper
import com.tapbi.spark.voiceai.data.remote.ThemeApi
import com.tapbi.spark.voiceai.extension.prefsDatastore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun providePreferencesDatastore(context: Application): DataStore<Preferences> {
        return context.prefsDatastore
    }

    @Provides
    @Singleton
    fun providePreferencesDatastoreHelper(datastore: DataStore<Preferences>): PreferencesDataStoreHelper {
        return PreferencesDataStoreHelper(datastore)
    }

    @Provides
    @Singleton
    fun provideRoomDb(context: Application): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, Constant.DB_NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun pageTemplateDao(db: AppDatabase): PageTemplateDao {
        return db.pageTemplateDao()
    }

    //api
    @Provides
    @Singleton
    fun dataThemeApi(): ThemeApi {
        return RetrofitClient.getThemeApi(Constant.BASE_URL)
    }
}