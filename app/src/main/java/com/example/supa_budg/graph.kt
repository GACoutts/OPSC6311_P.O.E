package com.example.supa_budg

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.Calendar

class Graph : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graph)

        val lineChart = findViewById<LineChart>(R.id.lineChart)

        // Create dummy data
        val entries = ArrayList<com.github.mikephil.charting.data.Entry>()
        for (i in 0..10) {
            entries.add(com.github.mikephil.charting.data.Entry(i.toFloat(), (Math.random() * 100).toFloat()))
        }

        val blueColor = ContextCompat.getColor(this, R.color.blue)

        // Create a dataset and give it a type
        val dataSet = LineDataSet(entries, "Dummy Data").apply {
            color = blueColor
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(blueColor)
        }

        // Set data to chart
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Customize chart appearance
        lineChart.description.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.animateX(1000)

        setupFooter()

        val openModalButton = findViewById<Button>(R.id.openModalButton)
        openModalButton.setOnClickListener {
            showGraphSettingsModal()
        }

    }

    private fun showGraphSettingsModal() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_graph_settings, null)

        // Reference to Spinner
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val categories = listOf("Food", "Transport", "Entertainment", "Utilities", "Other") // Dummy categories
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Reference to Date Range Button
        val dateRangeButton = dialogView.findViewById<Button>(R.id.dateRangeButton)
        dateRangeButton.setOnClickListener {
            showDateRangePicker()
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Graph Settings")
        builder.setView(dialogView)
        builder.setPositiveButton("OK") { dialog, _ ->
            // You can handle selections here
            val selectedCategory = categorySpinner.selectedItem.toString()
            // TODO: Save or use this selection as needed
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()

        val startDateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val startDate = "${dayOfMonth}/${month + 1}/${year}"

            val endDateListener = DatePickerDialog.OnDateSetListener { _, endYear, endMonth, endDay ->
                val endDate = "${endDay}/${endMonth + 1}/${endYear}"
                // TODO: Save or display these dates as needed
                Toast.makeText(this, "  Selected range: $startDate - $endDate", Toast.LENGTH_SHORT).show()
            }

            val endDatePicker = DatePickerDialog(
                this,
                endDateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            endDatePicker.setTitle("Select End Date")
            endDatePicker.show()
        }

        val startDatePicker = DatePickerDialog(
            this,
            startDateListener,
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
            val intent = Intent(this, SetMonthyBudget::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}


