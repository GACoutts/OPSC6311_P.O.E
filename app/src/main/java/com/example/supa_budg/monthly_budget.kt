package com.example.supa_budg

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar

class MonthlyBudget : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.monthly_budget)

        // Dummy values
        val budget = 1000f
        val spent = 7500f
        val percentageUsed = ((spent / budget) * 100).toInt()

        val budgetTextView = findViewById<TextView>(R.id.budgetText)
        val spentTextView = findViewById<TextView>(R.id.spentText)

        budgetTextView.text = "R$budget"
        spentTextView.text = "R$spent"


        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val percentageText = findViewById<TextView>(R.id.percentageText)
        val budgetText = findViewById<TextView>(R.id.budgetText)
        val spentText = findViewById<TextView>(R.id.spentText)
        val budgetSettingsButton = findViewById<Button>(R.id.budgetSettingsButton)
        budgetSettingsButton.setOnClickListener {
            showBudgetSettingsModal()
        }

        progressBar.progress = percentageUsed
        percentageText.text = "$percentageUsed%"
        budgetText.text = "Budget: $${budget.toInt()}"
        spentText.text = "Spent: $${spent.toInt()}"

        setupFooter()
    }

    private fun setupFooter() {

        // Footer items
        val homeButton = findViewById<ImageButton>(R.id.footerHome)
        val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
        val addEntryButton = findViewById<ImageButton>(R.id.footerGraph)
        val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

        budgetButton.setColorFilter(ContextCompat.getColor(this, R.color.blue))
        budgetButton.isEnabled = false

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

    private fun showBudgetSettingsModal() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_budget_settings, null)

        // Setup category spinner
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val categories = listOf("Food", "Transport", "Entertainment", "Utilities", "Other") // Dummy categories
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Date range button
        val dateRangeButton = dialogView.findViewById<Button>(R.id.dateRangeButton)
        dateRangeButton.setOnClickListener {
            showDateRangePicker()
        }

        // Edit Budgets button
        val editBudgetsButton = dialogView.findViewById<Button>(R.id.editBudgetsButton)
        editBudgetsButton.setOnClickListener {
            val selectedCategory = categorySpinner.selectedItem.toString()
            val intent = Intent(this, SetMonthyBudget::class.java)
            intent.putExtra("selectedCategory", selectedCategory)
            startActivity(intent)
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Budget Settings")
        builder.setView(dialogView)
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
                Toast.makeText(this, "Selected range: $startDate - $endDate", Toast.LENGTH_SHORT).show()
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


}
