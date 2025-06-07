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
import com.example.supa_budg.data.CategoryDao
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class Graph : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var resultsTextView: TextView

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

        loadBarChartData()
    }

    private fun loadBarChartData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val entryDao = AppDatabase.getDatabase(applicationContext).entryDao()

            // Calculate date range: Past 2 months to Next 2 months
            val currentMonth = YearMonth.now()
            val startMonth = currentMonth.minusMonths(2)
            val endMonth = currentMonth.plusMonths(2)

            val startDate = startMonth.atDay(1).atStartOfDay()
            val endDate = endMonth.atEndOfMonth().atTime(23, 59, 59)

            val entries = entryDao.getEntriesBetweenNow(startDate, endDate)

            // Group by month
            val monthlyData = mutableMapOf<YearMonth, Pair<Float, Float>>()  // Income, Expense

            for (entry in entries) {
                val ym = YearMonth.from(entry.date)
                val (income, expense) = monthlyData.getOrDefault(ym, Pair(0f, 0f))
                if (entry.isExpense) {
                    monthlyData[ym] = Pair(income, expense + entry.amount)
                } else {
                    monthlyData[ym] = Pair(income + entry.amount, expense)
                }
            }

            // Prepare chart data
            val barEntries = ArrayList<BarEntry>()
            val xLabels = ArrayList<String>()
            var index = 0f

            val sortedMonths = monthlyData.keys.sorted()
            val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")


            for (month in sortedMonths) {
                val data = monthlyData[month]!!
                barEntries.add(BarEntry(index, floatArrayOf(data.first, data.second)))
                xLabels.add(month.format(monthFormatter))
                index += 1f
            }

            val barDataSet = BarDataSet(barEntries, "Income vs Expense").apply {
                colors = intArrayOf(
                    ContextCompat.getColor(this@Graph, R.color.blue),
                    ContextCompat.getColor(this@Graph, R.color.red)
                ).toList()
                stackLabels = arrayOf("Income", "Expense")
                valueTextColor = Color.BLACK
                valueTextSize = 12f
            }

            val barData = BarData(barDataSet)
            barData.barWidth = 0.4f

            launch(Dispatchers.Main) {
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

            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")

            val resultsText = "Results for ${startMonth.format(formatter)} - ${endMonth.format(formatter)}"

            runOnUiThread {
                resultsTextView.text = resultsText
            }
        }
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
            val selectedCategory = categorySpinner.selectedItem.toString()
            // TODO: Apply filtering by category
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        val startDateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val startDate = "${dayOfMonth}/${month + 1}/${year}"
            val endDateListener = DatePickerDialog.OnDateSetListener { _, endYear, endMonth, endDay ->
                val endDate = "${endDay}/${endMonth + 1}/${endYear}"
                Toast.makeText(this, "Selected range: $startDate - $endDate", Toast.LENGTH_SHORT).show()
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

    suspend fun calculateMinMaxGoals(categoryDao: CategoryDao): Pair<Int, Int> {
        val categories = categoryDao.getAllCategoriesNow()
        val totalGoal = categories.sumOf { it.goal }
        // If goals don't vary, min and max are the same
        return Pair(totalGoal, totalGoal)
    }
}
