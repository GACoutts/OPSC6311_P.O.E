package com.example.supa_budg

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.Entry
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class EntryCalender : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var incomeText: TextView
    private lateinit var expenseText: TextView
    private lateinit var totalText: TextView
    private lateinit var showingDateText: TextView

    private lateinit var dbRef: DatabaseReference
    private val uid get() = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        .getString("uid", null)

    private var selectedCategory: String? = null
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null

    // Flag to control if date range filter is active
    private var isDateRangeFilterActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.income_expense_calender)

        calendarView = findViewById(R.id.calendarView)
        incomeText = findViewById(R.id.incomeValue)
        expenseText = findViewById(R.id.expenseValue)
        totalText = findViewById(R.id.totalValue)
        showingDateText = findViewById(R.id.showingResultsDate)

        dbRef = FirebaseDatabase.getInstance().getReference("User").child(uid.toString()).child("Entry")

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Only load single date entries if no date range filter is active
            if (!isDateRangeFilterActive) {
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                loadEntriesForDate(selectedDate)
            }
        }

        findViewById<Button>(R.id.openModalButton).setOnClickListener {
            showGraphSettingsModal()
        }

        setupFooter()

        // Load today's entries on startup by default
        if (!isDateRangeFilterActive) {
            loadEntriesForDate(LocalDate.now())
        }
    }

    private fun loadEntriesForDate(date: LocalDate) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var income = 0
                var expense = 0

                for (child in snapshot.children) {
                    val entry = child.getValue(Entry::class.java) ?: continue

                    val entryDate = try {
                        LocalDateTime.parse(entry.date.toString())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    } catch (e: Exception) {
                        continue
                    }

                    val matchesCategory = selectedCategory == null || selectedCategory == entry.categoryid

                    if (entryDate == date && matchesCategory) {
                        if (entry.isExpense) {
                            expense += entry.amount
                        } else {
                            income += entry.amount
                        }
                    }
                }

                val total = income - expense

                incomeText.text = "R $income"
                expenseText.text = "R $expense"
                totalText.text = "R $total"
                showingDateText.text = "Showing results for ${formatDate(date.dayOfMonth, date.monthValue - 1, date.year)}"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EntryCalender, "Error loading entries.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showGraphSettingsModal() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_graph_settings, null)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)

        lifecycleScope.launch {
            try {
                val catRef = FirebaseDatabase.getInstance()
                    .getReference("User")
                    .child(uid.toString())
                    .child("Category")

                val snapshot = catRef.get().await()

                val categoryNames = snapshot.children.mapNotNull {
                    it.child("name").getValue(String::class.java)
                }.toMutableList()

                Log.d("categoryNames", categoryNames.toString())

                categoryNames.add("Add Category")

                runOnUiThread {
                    val adapter = ArrayAdapter(
                        this@EntryCalender,
                        android.R.layout.simple_spinner_item,
                        categoryNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = adapter
                }
            } catch (e: Exception) {
                Toast.makeText(this@EntryCalender, "Failed to load categories.", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.dateRangeButton).setOnClickListener {
            showDateRangePicker()
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Calendar Settings")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val selected = categorySpinner.selectedItem.toString()
                if (selected != "Add Category") {
                    selectedCategory = selected
                    if (selectedStartDate != null && selectedEndDate != null) {
                        isDateRangeFilterActive = true
                        applyDateRangeFilter()
                    } else {
                        // No date range selected, so disable filter and reload today or selected date
                        isDateRangeFilterActive = false
                        loadEntriesForDate(LocalDate.now())
                    }
                } else {
                    startActivity(Intent(this, AddCategory::class.java))
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        val startDateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedStartDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

            val endDateListener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
                selectedEndDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                Toast.makeText(this, "Selected range: $selectedStartDate to $selectedEndDate", Toast.LENGTH_SHORT).show()
                applyDateRangeFilter()
            }

            DatePickerDialog(this, endDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        DatePickerDialog(this, startDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun applyDateRangeFilter() {
        if (selectedStartDate == null || selectedEndDate == null) return

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start = LocalDate.parse(selectedStartDate, formatter).atStartOfDay()
        val end = LocalDate.parse(selectedEndDate, formatter).plusDays(1).atStartOfDay()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var income = 0
                var expense = 0

                for (child in snapshot.children) {
                    val entry = child.getValue(Entry::class.java) ?: continue

                    Log.d("EntryCalender", "Raw entry: $entry")

                    val entryDate = try {
                        LocalDateTime.parse(entry.date.toString())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                    } catch (e: Exception) {
                        Log.e("EntryCalender", "Invalid date format for entry: ${entry.date}", e)
                        continue
                    }

                    Log.d("EntryCalender", "Parsed entryDate: $entryDate")

                    val matchesCategory = selectedCategory == null || selectedCategory == entry.categoryid

                    Log.d("EntryCalender", "Category: ${entry.categoryid}, selectedCategory: $selectedCategory, matches: $matchesCategory")
                    Log.d("EntryCalender", "Checking date: $entryDate isAfter ${start.minusSeconds(1)} && isBefore $end")

                    if (entryDate.isAfter(start.minusSeconds(1)) && entryDate.isBefore(end) && matchesCategory) {
                        if (entry.isExpense) {
                            expense += entry.amount
                            Log.d("EntryCalender", "Added expense: ${entry.amount}, Total expense: $expense")
                        } else {
                            income += entry.amount
                            Log.d("EntryCalender", "Added income: ${entry.amount}, Total income: $income")
                        }
                    } else {
                        Log.d("EntryCalender", "Entry filtered out (did not match date range or category)")
                    }
                }

                val total = income - expense

                incomeText.text = "R $income"
                expenseText.text = "R $expense"
                totalText.text = "R $total"

                showingDateText.text = buildString {
                    append("Results for: $selectedStartDate to $selectedEndDate")
                    selectedCategory?.let { append(" | Category: $it") }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EntryCalender, "Error filtering entries.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatDate(day: Int, monthZeroBased: Int, year: Int): String {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return "$day ${months[monthZeroBased]}, $year"
    }

    private fun setupFooter() {
        val homeButton = findViewById<ImageButton>(R.id.footerHome)
        val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
        val addEntryButton = findViewById<ImageButton>(R.id.footerGraph)
        val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

        calendarButton.setColorFilter(ContextCompat.getColor(this, R.color.blue))
        calendarButton.setBackgroundResource(R.drawable.footer_button_bg)
        calendarButton.isEnabled = false

        homeButton.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }

        addEntryButton.setOnClickListener {
            startActivity(Intent(this, Graph::class.java))
            finish()
        }

        calendarButton.setOnClickListener {
            startActivity(Intent(this, EntryCalender::class.java))
            finish()
        }

        budgetButton.setOnClickListener {
            startActivity(Intent(this, MonthlyBudget::class.java))
            finish()
        }
    }
}
