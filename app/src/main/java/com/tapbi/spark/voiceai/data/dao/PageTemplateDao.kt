package com.tapbi.spark.voiceai.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.data.model.PagesTemplate

@Dao
@JvmSuppressWildcards
interface PageTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<PagesTemplate>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(templateDB: PagesTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertResult(templateDB: PagesTemplate): Long

    @Update
    fun updateResult(templateDB: PagesTemplate): Int

    @Update
    fun update(template: PagesTemplate)

    @Delete
    fun delete(template: PagesTemplate)

    @Query("DELETE FROM ${Constant.TEMPLATE_TABLE} WHERE id = :id")
    fun deleteById(id: Long)

    @Delete
    fun deleteResult(template: PagesTemplate): Int

    @Query("Select * from " + Constant.TEMPLATE_TABLE + " where id =:id")
    fun getPageTemplateById(id: Long): PagesTemplate?

    @Query("SELECT * FROM ${Constant.TEMPLATE_TABLE}")
    suspend fun getAllList(): List<PagesTemplate>
}
