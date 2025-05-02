package com.example.supa_budg.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface CategoryDao {
    @Query("SELECT * FROM Category")
    fun getAllCategories(): LiveData<List<Category>>
}