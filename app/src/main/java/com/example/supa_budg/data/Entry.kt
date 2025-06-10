package com.example.supa_budg.data

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Entry(
    var entryId: String? = null,
    var amount: Int = 0,
    var date: String? = null,  // Store date as ISO string for Firebase
    var categoryid: String? = null,
    var notes: String? = null,
    var photoUri: String? = null,
    var isExpense: Boolean = false
) : Parcelable {

    // Helper to get date as LocalDateTime from the String
    val dateTime: LocalDateTime
        get() = try {
            LocalDateTime.parse(date)
        } catch (e: Exception) {
            LocalDateTime.now()
        }

    val createdDateFormat: String
        get() = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    constructor(parcel: Parcel) : this(
        entryId = parcel.readString(),
        amount = parcel.readInt(),
        date = parcel.readString() ?: LocalDateTime.now().toString(),
        categoryid = parcel.readString(),
        notes = parcel.readString(),
        photoUri = parcel.readString(),
        isExpense = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(entryId)
        parcel.writeInt(amount)
        parcel.writeString(date)  // already string
        parcel.writeString(categoryid)
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
