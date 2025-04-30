package com.example.supa_budg

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class category_view : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var timeFrameButton: Button
    private lateinit var amountText: TextView
    private lateinit var dateCalendarIcon: ImageView

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        setupCategorySpinner()
        setupCalendarButton()
    }

    private fun setupViews() {
        categorySpinner = findViewById(R.id.category_spinner)
        timeFrameButton = findViewById(R.id.time_frame_button)
        amountText = findViewById(R.id.amount_text)
        dateCalendarIcon = findViewById(R.id.calendar_icon)
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Food", "Transport", "Utilities", "Entertainment", "Misc")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupCalendarButton() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        timeFrameButton.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dateCalendarIcon.setOnClickListener {
            timeFrameButton.performClick()
        }
    }

    private fun updateDateInView() {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        timeFrameButton.text = format.format(calendar.time)
    }
}
// need button to confirm selection