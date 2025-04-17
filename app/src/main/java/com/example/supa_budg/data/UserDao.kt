package com.example.supa_budg.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

//DAO = data access objects. This is what you call when you are doing stuff
@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM User")
    suspend fun getAllUsers(): List<User>
}