package com.example.supa_budg

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.AppDatabase
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar

class EntryCalender : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.income_expense_calender)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)

        // Set the default date to today
        val today = Calendar.getInstance().timeInMillis
        calendarView.date = today

        val db = AppDatabase.getDatabase(this)
        val entryDao = db.entryDao()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val startOfDay = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
            val endOfDay = startOfDay.plusDays(1)

            lifecycleScope.launch {
                val entries = entryDao.getEntriesBetweenNow(startOfDay, endOfDay)
                var income = 0
                var expense = 0

                for (entry in entries) {
                    if (entry.isExpense) {
                        expense += entry.amount
                    } else {
                        income += entry.amount
                    }
                }

                val total = income - expense

                runOnUiThread {
                    findViewById<TextView>(R.id.incomeValue).text = "R $income"
                    findViewById<TextView>(R.id.expenseValue).text = "R $expense"
                    findViewById<TextView>(R.id.totalValue).text = "R $total"
                }
            }
        }

        // Footer items
        val homeButton = findViewById<ImageButton>(R.id.footerHome)
        val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
        val addEntryButton = findViewById<ImageButton>(R.id.footerGraph)
        val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

        homeButton.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        addEntryButton.setOnClickListener {
            val intent = Intent(this, Graph::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        calendarButton.setOnClickListener {
            val intent = Intent(this, EntryCalender::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        budgetButton.setOnClickListener {
            val intent = Intent(this, SetMonthyBudget::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showCustomModal() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Custom Modal")
            .setMessage("This is a custom modal dialog.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}
