package com.example.supa_budg

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.supa_budg.data.AppDatabase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class SetMonthlyBudget : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var budgetDisplay: TextView
    private lateinit var btnConfirmBudget: Button
    private lateinit var numberPadButtons: List<Button>
    val db = AppDatabase.getDatabase(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.set_monthly_budget)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        setupSpinner()
        setupNumberPad()
        setupButtons()
        setupCloseButton()
    }

    private fun setupViews() {
        categorySpinner = findViewById(R.id.category_spinner)
        budgetDisplay = findViewById(R.id.budget_display)
        btnConfirmBudget = findViewById(R.id.btnConfimBudget)

        numberPadButtons = listOf(
            findViewById(R.id.button_0),
            findViewById(R.id.button_1),
            findViewById(R.id.button_2),
            findViewById(R.id.button_3),
            findViewById(R.id.button_4),
            findViewById(R.id.button_5),
            findViewById(R.id.button_6),
            findViewById(R.id.button_7),
            findViewById(R.id.button_8),
            findViewById(R.id.button_9),
            findViewById(R.id.button_dot),
            findViewById(R.id.button_backspace)
        )
    }

    private fun setupCloseButton() {
        val btnClose: ImageButton = findViewById(R.id.btnClose)
        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun setupSpinner() {
        // Observe category data from database
        db.categoryDao().getAllCategories().observe(this) { categoryList ->
            // Extract names, optionally add "Overall" as the first option
            val categoryNames = mutableListOf("Overall")
            categoryNames.addAll(categoryList.map { it.name })

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }
    }

    private fun setupNumberPad() {
        for (button in numberPadButtons) {
            button.setOnClickListener {
                val label = (it as Button).text.toString()
                val currentText = budgetDisplay.text.toString()

                // Remove the leading 'R' to work with the numeric part
                val numericPart = if (currentText.startsWith("R")) {
                    currentText.substring(1)
                } else {
                    currentText
                }

                when (label) {
                    "⌫" -> {
                        val newText = if (numericPart.isNotEmpty()) {
                            numericPart.dropLast(1)
                        } else {
                            ""
                        }
                        budgetDisplay.text = "R" + (if (newText.isEmpty()) "0" else newText)
                    }
                    "." -> {
                        if (!numericPart.contains(".")) {
                            budgetDisplay.text = "R" + (numericPart + ".")
                        }
                    }
                    else -> {
                        // Count only digits (exclude any dots)
                        val digitCount = numericPart.count { it.isDigit() }

                        // Only allow adding digits if there are less than 6
                        if (digitCount < 6) {
                            var updatedText = numericPart
                            if (updatedText == "0") {
                                updatedText = "" // Remove initial zero
                            }
                            updatedText += label
                            budgetDisplay.text = "R" + updatedText
                        } else {
                            Toast.makeText(this, "Maximum 6 digits allowed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }



    private fun setupButtons() {
        btnConfirmBudget.setOnClickListener {
            val goalAmountText = budgetDisplay.text.toString()
            val selectedCategory = categorySpinner.selectedItem.toString()

            // Validation
            if (goalAmountText.isBlank()) {
                Toast.makeText(this, "Please enter a goal amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goalAmount = goalAmountText.toFloatOrNull()
            if (goalAmount == null) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use coroutine to access DB
            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val categoryDao = db.categoryDao()
                    categoryDao.updateGoalByName(selectedCategory, goalAmount.toInt())

                    Toast.makeText(
                        this@SetMonthlyBudget,
                        "Budget saved: $selectedCategory = R${goalAmount.toInt()}",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@SetMonthlyBudget,
                        "Error saving budget: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}