package com.example.supa_budg.data

data class Achievements(
    var id: String ? = null,
    var title: String ? = null,
    var description: String ? = null,
    var iscompleted: Boolean = false,
    var userId: String ? = null,
    var goal: Int = 0,
    var category: String ? = null
)
