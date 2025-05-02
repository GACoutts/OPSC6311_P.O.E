package com.example.supa_budg.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "Entry", foreignKeys = [
 ForeignKey(entity = Category::class, parentColumns = ["categoryid"], childColumns = ["categoryid"])
])
data class Entry(
    @PrimaryKey(autoGenerate = true) val entryId: Int = 0,
    val amount: Int,
    val date: LocalDateTime = LocalDateTime.now(),
    val categoryid: Int,
    val notes: String,
    //val photo:
    val isExpense: Boolean
) : Parcelable{
    val createdDateFormat : String
        get() = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
}
//need to add photo
//need to add proper foregin keys