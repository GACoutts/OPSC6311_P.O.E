package com.example.supa_budg.data

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class Entry(
    var entryId: String ? = null,
    var amount: Int = 0,
    var date: LocalDateTime = LocalDateTime.now(),
    var categoryid: String ? = null,
    var notes: String ? = null,
    var photoUri: String? = null,
    var isExpense: Boolean = false
) : Parcelable {

    val createdDateFormat: String
        get() = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    constructor(parcel: Parcel) : this(
        entryId = parcel.readString(),
        amount = parcel.readInt(),
        date = LocalDateTime.parse(parcel.readString()),
        categoryid = parcel.readString(),
        notes = parcel.readString() ?: "",
        photoUri = parcel.readString(),
        isExpense = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(entryId)
        parcel.writeInt(amount)
        parcel.writeString(date.toString())
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
