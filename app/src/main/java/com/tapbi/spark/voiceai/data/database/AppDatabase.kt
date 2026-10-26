package com.tapbi.spark.voiceai.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.data.converters.Converters
import com.tapbi.spark.voiceai.data.dao.PageTemplateDao
import com.tapbi.spark.voiceai.data.model.PagesTemplate


@Database(
    entities = [PagesTemplate::class],
    version = Constant.DB_VERSION
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pageTemplateDao(): PageTemplateDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
            }
        }
    }
}