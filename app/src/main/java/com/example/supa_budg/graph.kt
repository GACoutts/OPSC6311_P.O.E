package com.example.supa_budg

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.supa_budg.data.Entry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry as ChartEntry

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

        // Create a dataset and give it a type
        val dataSet = LineDataSet(entries, "Dummy Data").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.BLUE)
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
    }

    private fun setupFooter() {
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
}


