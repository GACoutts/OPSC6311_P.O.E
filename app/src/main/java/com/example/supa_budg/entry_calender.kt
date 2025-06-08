package com.example.supa_budg

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar

class EntryCalender : AppCompatActivity() {

    private var selectedCategory: String? = null
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null

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

                    // Update showingResultsDate TextView with nicely formatted date
                    val showingResultsDate = findViewById<TextView>(R.id.showingResultsDate)
                    showingResultsDate.text = "Showing results for ${formatDate(dayOfMonth, month, year)}"
                }
            }
        }


        val openModalButton = findViewById<Button>(R.id.openModalButton)
        openModalButton.setOnClickListener {
            showGraphSettingsModal()
        }

        setupFooter()
    }

    private suspend fun applyDateRangeFilter() {
        if (selectedStartDate == null || selectedEndDate == null) return

        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start = LocalDateTime.parse("${selectedStartDate}T00:00:00")
        val end = LocalDateTime.parse("${selectedEndDate}T00:00:00").plusDays(1)

        val categoryId = getCategoryId(selectedCategory)

        val entries = AppDatabase.getDatabase(applicationContext)
            .entryDao()
            .getEntriesBetweenNowFiltered(start, end, categoryId)

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

            val showingResultsDate = findViewById<TextView>(R.id.showingResultsDate)
            showingResultsDate.text = buildString {
                append("Results for: $selectedStartDate to $selectedEndDate")
                selectedCategory?.let {
                    append(" | Category: $it")
                }
            }
        }
    }

    private suspend fun getCategoryId(categoryName: String?): Int? {
        if (categoryName.isNullOrEmpty()) return null
        val db = AppDatabase.getDatabase(applicationContext)
        val category = db.categoryDao().getCategoryByName(categoryName)
        return category?.categoryid
    }

    private fun showGraphSettingsModal() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_graph_settings, null)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)

        // Load categories from database
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val categories = db.categoryDao().getAllCategoriesNow().map { it.name }.toMutableList()

            // Add "Add Category" option at the end
            categories.add("Add Category")

            // Switch to Main thread to update UI
            launch(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@EntryCalender, android.R.layout.simple_spinner_item, categories)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
        }

        val dateRangeButton = dialogView.findViewById<Button>(R.id.dateRangeButton)
        dateRangeButton.setOnClickListener {
            showDateRangePicker()
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Calender Settings")
        builder.setView(dialogView)
        builder.setPositiveButton("OK") { dialog, _ ->
            val selected = categorySpinner.selectedItem.toString()
            if (selected == "Add Category") {
                val intent = Intent(this, AddCategory::class.java)
                startActivity(intent)
            } else {
                selectedCategory = selected
                lifecycleScope.launch {
                    applyDateRangeFilter()
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        val startDateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedStartDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            val endDateListener = DatePickerDialog.OnDateSetListener { _, endYear, endMonth, endDay ->
                selectedEndDate = String.format("%04d-%02d-%02d", endYear, endMonth + 1, endDay)
                Toast.makeText(this, "Selected range: $selectedStartDate to $selectedEndDate", Toast.LENGTH_SHORT).show()
            }

            val endDatePicker = DatePickerDialog(
                this, endDateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            endDatePicker.setTitle("Select End Date")
            endDatePicker.show()
        }

        val startDatePicker = DatePickerDialog(
            this, startDateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        startDatePicker.setTitle("Select Start Date")
        startDatePicker.show()
    }

    private fun formatDate(day: Int, monthZeroBased: Int, year: Int): String {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthName = months[monthZeroBased]
        return "$day $monthName"
    }


    private fun setupFooter() {

        // Footer items
        val homeButton = findViewById<ImageButton>(R.id.footerHome)
        val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
        val addEntryButton = findViewById<ImageButton>(R.id.footerGraph)
        val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

        calendarButton.setColorFilter(ContextCompat.getColor(this, R.color.blue))
        calendarButton.setBackgroundResource(R.drawable.footer_button_bg)
        calendarButton.isEnabled = false;

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
            val intent = Intent(this, MonthlyBudget::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
