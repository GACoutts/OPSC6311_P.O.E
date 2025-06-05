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
        }
    }
