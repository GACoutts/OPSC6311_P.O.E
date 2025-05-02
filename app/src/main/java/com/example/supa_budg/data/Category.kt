package com.example.supa_budg.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryid: Int = 0,
    val name: String,
    val imageUrl: String,
    val goal: Int
)
