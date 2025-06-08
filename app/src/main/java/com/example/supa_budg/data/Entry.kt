package com.example.supa_budg.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
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
    val photoUri: String? = null,
    val isExpense: Boolean
) : Parcelable {

    val createdDateFormat: String
        get() = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    constructor(parcel: Parcel) : this(
        entryId = parcel.readInt(),
        amount = parcel.readInt(),
        date = LocalDateTime.parse(parcel.readString()),
        categoryid = parcel.readInt(),
        notes = parcel.readString() ?: "",
        photoUri = parcel.readString(),
        isExpense = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(entryId)
        parcel.writeInt(amount)
        parcel.writeString(date.toString())
        parcel.writeInt(categoryid)
        parcel.writeString(notes)
        parcel.writeString(photoUri)
        parcel.writeByte(if (isExpense) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Entry> {
        override fun createFromParcel(parcel: Parcel): Entry {
            return Entry(parcel)
        }

        override fun newArray(size: Int): Array<Entry?> {
            return arrayOfNulls(size)
        }
    }
}
