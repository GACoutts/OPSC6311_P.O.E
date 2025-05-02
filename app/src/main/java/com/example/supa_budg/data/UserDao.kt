package com.example.supa_budg.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

//DAO = data access objects. This is what you call when you are doing stuff
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM User")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM User WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM User WHERE name = :name")
    suspend fun getUserByUsername(name: String): User?

    @Query("SELECT * FROM User")
    fun observeAllUsers(): LiveData<List<User>>

    // UPDATE
    @Update
    suspend fun updateUser(user: User)

    // DELETE
    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM User WHERE id = :id")
    suspend fun deleteUserById(id: Int)
}