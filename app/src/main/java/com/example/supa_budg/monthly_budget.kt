package com.example.supa_budg

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.Category
import com.example.supa_budg.data.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.absoluteValue

class MonthlyBudget : AppCompatActivity() {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var categoryList: List<Category>

    private lateinit var progressBar: ProgressBar
    private lateinit var percentageText: TextView
    private lateinit var budgetText: TextView
    private lateinit var spentText: TextView
    private lateinit var showingResultsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.monthly_budget)

        progressBar = findViewById(R.id.progressBar)
        percentageText = findViewById(R.id.percentageText)
        budgetText = findViewById(R.id.budgetText)
        spentText = findViewById(R.id.spentText)
        showingResultsText = findViewById(R.id.showingResultsText)

        findViewById<Button>(R.id.budgetSettingsButton).setOnClickListener {
            showBudgetSettingsModal()
        }

        setupFooter()
        loadCategories()
    }

    private fun loadCategories() {
        val catRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("categories")
        catRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList = snapshot.children.mapNotNull {
                    val id = it.key ?: return@mapNotNull null
                    val name = it.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    val goal = it.child("goal").getValue(Int::class.java) ?: 0
                    Category(id, name, "", goal)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MonthlyBudget, "Error loading categories.", Toast.LENGTH_SHORT).show()
                categoryList = emptyList()
            }
        })
    }

    private fun showBudgetSettingsModal() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_budget_settings, null)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val editBudgetsButton = dialogView.findViewById<Button>(R.id.editBudgetsButton)

        val categoryNames = categoryList.map { it.name }.toMutableList()
        categoryNames.add("Add Category")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.setSelection(0)

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                if (position == categoryNames.lastIndex) {
                    startActivity(Intent(this@MonthlyBudget, AddCategory::class.java))
                    categorySpinner.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        editBudgetsButton.setOnClickListener {
            val selected = categorySpinner.selectedItem.toString()
            if (selected != "Add Category") {
                val intent = Intent(this, SetMonthlyBudget::class.java)
                intent.putExtra("selectedCategory", selected)
                startActivity(intent)
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Budget Settings")
            .setView(dialogView)
            .setPositiveButton("Set") { dialog, _ ->
                val selectedCategoryName = categorySpinner.selectedItem.toString()
                val selectedCategory = categoryList.find { it.name == selectedCategoryName }

                if (selectedCategory != null) {
                    lifecycleScope.launch {
                        updateCategoryBudget(selectedCategory)
                        runOnUiThread {
                            showingResultsText.text = "Showing results for: ${selectedCategory.name}"
                        }
                    }
                } else {
                    Toast.makeText(this, "Selected category not found", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private suspend fun updateCategoryBudget(category: Category) {
        try {
            val entryRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("entries")
            val snapshot = entryRef.get().await()
            val matchingEntries = snapshot.children.mapNotNull {
                it.getValue(Entry::class.java)
            }.filter { it.categoryid == category.categoryid }

            val netTotal = matchingEntries.sumOf {
                if (it.isExpense) it.amount else -it.amount
            }.absoluteValue.toFloat()

            val goal = category.goal.toFloat()
            val percentageUsed = if (goal > 0) ((netTotal / goal) * 100).toInt() else 0

            runOnUiThread {
                updateBudgetWithValues(goal, netTotal, percentageUsed)
            }

        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Error loading budget: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBudgetWithValues(goal: Float, netTotal: Float, percentageUsed: Int) {
        budgetText.text = "Budget: R${goal.toInt()}"
        spentText.text = "Spent: R${netTotal.toInt()}"
        progressBar.progress = percentageUsed.coerceIn(0, 100)
        percentageText.text = "$percentageUsed%"
    }

    private fun setupFooter() {
        val homeButton = findViewById<ImageButton>(R.id.footerHome)
        val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
        val addEntryButton = findViewById<ImageButton>(R.id.footerGraph)
        val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

        budgetButton.setColorFilter(ContextCompat.getColor(this, R.color.blue))
        budgetButton.setBackgroundResource(R.drawable.footer_button_bg)
        budgetButton.isEnabled = false

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
