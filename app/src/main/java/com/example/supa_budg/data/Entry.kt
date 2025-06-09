package com.example.supa_budg.data

data class Entry(
    var entryId: Int = 0,
    var amount: Int = 0,
    var date: String = "", // Save date as ISO string
    var categoryid: Int = 0,
    var notes: String = "",
    var photoUri: String? = null,
    var isExpense: Boolean = false
)
