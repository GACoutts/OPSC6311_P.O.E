package com.example.supa_budg

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.*
import kotlin.collections.ArrayList

class Graph : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var resultsTextView: TextView

    private var selectedCategory: String? = null
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graph)

        barChart = findViewById(R.id.barChart)
        resultsTextView = findViewById(R.id.resultsTextView)

        setupFooter()

        val openModalButton = findViewById<Button>(R.id.openModalButton)
        openModalButton.setOnClickListener {
            showGraphSettingsModal()
        }

        loadBarChartDataWithGoals()
    }

    private fun showGraphSettingsModal() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_graph_settings, null)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val categories = listOf("Food", "Transport", "Entertainment", "Utilities", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val dateRangeButton = dialogView.findViewById<Button>(R.id.dateRangeButton)
        dateRangeButton.setOnClickListener {
            showDateRangePicker()
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Graph Settings")
        builder.setView(dialogView)
        builder.setPositiveButton("OK") { dialog, _ ->
            selectedCategory = categorySpinner.selectedItem.toString()
            // Apply filters by category and date range
            loadBarChartDataWithGoals()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        val startDateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedStartDate = "${year}-${month + 1}-$dayOfMonth"
            val endDateListener = DatePickerDialog.OnDateSetListener { _, endYear, endMonth, endDay ->
                selectedEndDate = "${endYear}-${endMonth + 1}-$endDay"
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

    private fun setupFooter() {
        val homeButton = findViewById<ImageButton>(R.id.footerHome)
        val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
        val graph = findViewById<ImageButton>(R.id.footerGraph)
        val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

        graph.setColorFilter(ContextCompat.getColor(this, R.color.blue))
        graph.setBackgroundResource(R.drawable.footer_button_bg)
        graph.isEnabled = false

        homeButton.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        graph.setOnClickListener {
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

    private fun loadBarChartDataWithGoals() {
        lifecycleScope.launch(Dispatchers.IO) {
            val currentMonth = YearMonth.now()
            val startMonth = currentMonth.minusMonths(2)
            val endMonth = currentMonth.plusMonths(2)

            val startDate = startMonth.atDay(1).atStartOfDay()
            val endDate = endMonth.atEndOfMonth().atTime(23, 59, 59)

            val db = AppDatabase.getDatabase(applicationContext)
            val categoryDao = db.categoryDao()
            val entryDao = db.entryDao()

            val entries = entryDao.getEntriesBetweenNow(startDate, endDate)

            val categories = categoryDao.getAllCategoriesNow()

            val entriesByMonth = entries.groupBy { YearMonth.from(it.date) }

            val monthlyGoals = mutableMapOf<YearMonth, Int>()

            var month = startMonth
            while (!month.isAfter(endMonth)) {
                val monthEntries = entriesByMonth[month].orEmpty()
                val categoriesInMonth = monthEntries.map { it.categoryid }.toSet()

                val sumGoal = categories.filter { it.categoryid in categoriesInMonth }
                    .sumOf { it.goal }

                monthlyGoals[month] = sumGoal
                month = month.plusMonths(1)
            }

            val goalsList = monthlyGoals.values
            val minGoal = goalsList.minOrNull() ?: 0
            val maxGoal = goalsList.maxOrNull() ?: 0

            val barEntries = ArrayList<BarEntry>()
            val xLabels = ArrayList<String>()
            var index = 0f

            val monthlyData = mutableMapOf<YearMonth, Pair<Float, Float>>()

            for (entry in entries) {
                val ym = YearMonth.from(entry.date)
                val (income, expense) = monthlyData.getOrDefault(ym, Pair(0f, 0f))
                if (entry.isExpense) {
                    monthlyData[ym] = Pair(income, expense + entry.amount)
                } else {
                    monthlyData[ym] = Pair(income + entry.amount, expense)
                }
            }

            val sortedMonths = monthlyData.keys.sorted()

            for (monthKey in sortedMonths) {
                val data = monthlyData[monthKey]!!
                barEntries.add(BarEntry(index, floatArrayOf(data.first, data.second)))
                xLabels.add(monthKey.month.name.substring(0,3))
                index += 1f
            }

            val barDataSet = BarDataSet(barEntries, "Income vs Expense").apply {
                colors = listOf(
                    ContextCompat.getColor(this@Graph, R.color.blue),
                    ContextCompat.getColor(this@Graph, R.color.red)
                )
                stackLabels = arrayOf("Income", "Expense")
                valueTextColor = Color.BLACK
                valueTextSize = 12f
            }

            val barData = BarData(barDataSet)
            barData.barWidth = 0.4f

            launch(Dispatchers.Main) {
                val leftAxis = barChart.axisLeft
                leftAxis.removeAllLimitLines()

                if (minGoal > 0) {
                    val minGoalLine = LimitLine(minGoal.toFloat(), "Min Goal")
                    minGoalLine.lineColor = Color.RED
                    minGoalLine.lineWidth = 2f
                    minGoalLine.textColor = Color.RED
                    minGoalLine.textSize = 12f
                    leftAxis.addLimitLine(minGoalLine)
                }

                if (maxGoal > 0 && maxGoal != minGoal) {
                    val maxGoalLine = LimitLine(maxGoal.toFloat(), "Max Goal")
                    maxGoalLine.lineColor = Color.GREEN
                    maxGoalLine.lineWidth = 2f
                    maxGoalLine.textColor = Color.GREEN
                    maxGoalLine.textSize = 12f
                    leftAxis.addLimitLine(maxGoalLine)
                }

                barChart.apply {
                    data = barData
                    description.isEnabled = false
                    axisRight.isEnabled = false
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
                    xAxis.granularity = 1f
                    setFitBars(true)
                    animateY(1000)
                    invalidate()
                }
            }
        }
    }
}
