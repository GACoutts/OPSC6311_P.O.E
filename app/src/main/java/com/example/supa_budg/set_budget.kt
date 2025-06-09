package com.example.supa_budg

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.supa_budg.data.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SetMonthlyBudget : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var budgetDisplay: TextView
    private lateinit var btnConfirmBudget: Button
    private lateinit var numberPadButtons: List<Button>
    private lateinit var closeButton: ImageButton

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var categories: List<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.set_monthly_budget)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        categorySpinner = findViewById(R.id.category_spinner)
        budgetDisplay = findViewById(R.id.budget_display)
        btnConfirmBudget = findViewById(R.id.btnConfimBudget)
        closeButton = findViewById(R.id.btnClose)

        numberPadButtons = listOf(
            findViewById(R.id.button_0), findViewById(R.id.button_1), findViewById(R.id.button_2),
            findViewById(R.id.button_3), findViewById(R.id.button_4), findViewById(R.id.button_5),
            findViewById(R.id.button_6), findViewById(R.id.button_7), findViewById(R.id.button_8),
            findViewById(R.id.button_9), findViewById(R.id.button_dot), findViewById(R.id.button_backspace)
        )

        setupNumberPad()
        setupCloseButton()
        loadCategories()
        setupConfirmButton()
    }

    private fun loadCategories() {
        val catRef = FirebaseDatabase.getInstance().getReference("User").child(uid).child("Category")

        catRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories = snapshot.children.mapNotNull {
                    val id = it.key ?: return@mapNotNull null
                    val name = it.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    Category(id, name, "", 0)
                }

                val categoryNames = categories.map { it.name }

                val adapter = ArrayAdapter(
                    this@SetMonthlyBudget,
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter

                // Auto-select if coming from MonthlyBudget
                intent.getStringExtra("selectedCategory")?.let { selected ->
                    val index = categoryNames.indexOf(selected)
                    if (index >= 0) categorySpinner.setSelection(index)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SetMonthlyBudget, "Failed to load categories.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupConfirmButton() {
        btnConfirmBudget.setOnClickListener {
            val amountStr = budgetDisplay.text.toString().removePrefix("R")
            val amount = amountStr.toFloatOrNull()
            val selectedCategory = categorySpinner.selectedItem.toString()

            if (amount == null) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = categories.find { it.name == selectedCategory }
            if (category == null) {
                Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryRef = FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("Category").child(category.categoryid)

            categoryRef.child("goal").setValue(amount.toInt())
                .addOnSuccessListener {
                    Toast.makeText(this, "Budget saved: $selectedCategory = R${amount.toInt()}", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save budget.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupCloseButton() {
        closeButton.setOnClickListener { finish() }
    }

    private fun setupNumberPad() {
        for (button in numberPadButtons) {
            button.setOnClickListener {
                val label = (it as Button).text.toString()
                val currentText = budgetDisplay.text.toString().removePrefix("R")

                when (label) {
                    "⌫" -> {
                        val newText = currentText.dropLast(1)
                        budgetDisplay.text = "R" + (if (newText.isEmpty()) "0" else newText)
                    }

                    "." -> {
                        if (!currentText.contains(".")) {
                            budgetDisplay.text = "R$currentText."
                        }
                    }

                    else -> {
                        val digitCount = currentText.count { ch -> ch.isDigit() }
                        if (digitCount < 6) {
                            val updated = if (currentText == "0") label else currentText + label
                            budgetDisplay.text = "R$updated"
                        } else {
                            Toast.makeText(this, "Maximum 6 digits allowed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
