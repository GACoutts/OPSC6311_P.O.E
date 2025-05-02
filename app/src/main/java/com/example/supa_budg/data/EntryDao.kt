package com.example.supa_budg.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Insert
import java.time.LocalDateTime

@Dao
interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: Entry)

    @Query("SELECT * FROM Entry ORDER BY date DESC")
    fun getAllEntries(): LiveData<List<Entry>>

    @Query("SELECT * FROM Entry WHERE categoryid = :categoryId ORDER BY date DESC")
    fun getEntriesByCategory(categoryId: Int): LiveData<List<Entry>>

    @Query("SELECT * FROM Entry WHERE entryId = :id LIMIT 1")
    suspend fun getEntryById(id: Int): Entry?

    @Query("SELECT * FROM Entry WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getEntriesBetween(start: LocalDateTime, end: LocalDateTime): LiveData<List<Entry>>

    @Update
    suspend fun updateEntry(entry: Entry)

    @Delete
    suspend fun deleteEntry(entry: Entry)

    @Query("SELECT SUM(amount) FROM Entry WHERE date BETWEEN :start AND :end")
    suspend fun getTotalAmountFromDate(start: String, end: String): Double?

    @Query("SELECT SUM(amount) FROM Entry")
    suspend fun getTotalAmount(): Double?

}