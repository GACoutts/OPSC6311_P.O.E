package com.example.supa_budg

import android.os.Bundle
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class EntryCalender : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.income_expense_calender)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)

        // Set the default date to today
        val today = Calendar.getInstance().timeInMillis
        calendarView.date = today
    }
}
