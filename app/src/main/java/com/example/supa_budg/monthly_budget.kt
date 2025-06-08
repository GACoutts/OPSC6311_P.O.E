package com.example.supa_budg

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
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.AppDatabase
import com.example.supa_budg.data.Category
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class MonthlyBudget : AppCompatActivity() {

    // Database Objects
    private lateinit var categories: List<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.monthly_budget)
        var percentageUsed = 0

        // Default values
        val budget = 0f
        val spent = 0f

        if(budget !== 0f || spent !== 0f) {
            percentageUsed = ((spent / budget) * 100).toInt()
        }

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
        budgetText.text = "Budget: R${budget.toInt()}"
        spentText.text = "Spent: R${spent.toInt()}"

        setupFooter()
        loadCategories()
    }

    private fun loadCategories() {
        val db = AppDatabase.getDatabase(applicationContext)
        val categoryDao = db.categoryDao()

        lifecycleScope.launch {
            try {
                categories = categoryDao.getAllCategoriesNow()
            } catch (e: Exception) {
                Toast.makeText(this@MonthlyBudget, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
                categories = emptyList()
                categories = emptyList()
            }
        }
    }

    private fun setupFooter() {

        // Footer items
        val homeButton = findViewById<ImageButton>(R.id.footerHome)
        val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
        val addEntryButton = findViewById<ImageButton>(R.id.footerGraph)
        val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

        budgetButton.setColorFilter(ContextCompat.getColor(this, R.color.blue))
        budgetButton.setBackgroundResource(R.drawable.footer_button_bg)
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

        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val editBudgetsButton = dialogView.findViewById<Button>(R.id.editBudgetsButton)

        val categoryNames = if (::categories.isInitialized && categories.isNotEmpty()) {
            categories.map { it.name }.toMutableList()
        } else {
            mutableListOf("Food", "Transport", "Entertainment", "Utilities", "Other")
        }

        categoryNames.add("Add Category")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                if (position == categoryNames.lastIndex) {
                    // Navigate to AddCategory page
                    val intent = Intent(this@MonthlyBudget, AddCategory::class.java)
                    startActivity(intent)
                    categorySpinner.setSelection(0) // Reset selection
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        editBudgetsButton.setOnClickListener {
            val selectedCategoryName = categorySpinner.selectedItem.toString()
            if (selectedCategoryName != "Add Category") {
                val intent = Intent(this, SetMonthlyBudget::class.java)
                intent.putExtra("selectedCategory", selectedCategoryName)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
            }
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("Budget Settings")
            .setView(dialogView)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Set") { dialog, _ ->
                val selectedCategoryName = categorySpinner.selectedItem.toString()
                if (selectedCategoryName != "Add Category") {
                    val selectedCategory = categories.find { it.name == selectedCategoryName }
                    if (selectedCategory != null) {
                        lifecycleScope.launch {
                            updateCategoryBudget(selectedCategory)
                            runOnUiThread {
                                val showingResultsText = findViewById<TextView>(R.id.showingResultsText)
                                showingResultsText.text = "Showing results for: ${selectedCategory.name}"
                            }
                        }
                    } else {
                        Toast.makeText(this, "Selected category not found", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
        builder.create().show()
    }

    private suspend fun updateCategoryBudget(category: Category) {
        val db = AppDatabase.getDatabase(applicationContext)
        val entryDao = db.entryDao()
        try {
            val netTotal = (entryDao.getNetTotalByCategory(category.categoryid) ?: 0.0).absoluteValue
            val goal = category.goal.toFloat()

            val percentageUsed = ((netTotal / goal) * 100).toInt()

            runOnUiThread {
                updateBudgetWithValues(goal, netTotal.toFloat(), percentageUsed)
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Error loading entries: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBudgetWithValues(goal: Float, netTotal: Float, percentageUsed: Int) {
        val budgetTextView = findViewById<TextView>(R.id.budgetText)
        val spentTextView = findViewById<TextView>(R.id.spentText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val percentageText = findViewById<TextView>(R.id.percentageText)

        budgetTextView.text = "Budget: R${goal.toInt()}"
        spentTextView.text = "Spent: R${netTotal.toInt()}"
        progressBar.progress = percentageUsed.coerceIn(0, 100)
        percentageText.text = "$percentageUsed%"
    }
}
