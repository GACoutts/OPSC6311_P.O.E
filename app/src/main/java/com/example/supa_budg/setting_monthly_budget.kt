package com.example.supa_budg//package com.example.supa_budg
//  DEPRICATED
//import android.os.Bundle
//import android.widget.*
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class setting_monthly_budget : AppCompatActivity() {
//
//    private lateinit var categorySpinner: Spinner//    private lateinit var budgetDisplay: TextView
//    private lateinit var buttonExpense: Button
//    private lateinit var buttonIncome: Button
//    private lateinit var numberPadButtons: List<Button>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_setting_monthly_budget)
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        setupViews()
//        setupSpinner()
//        setupNumberPad()
//        setupButtons()
//    }
//
//    private fun setupViews() {
//        categorySpinner = findViewById(R.id.category_spinner)
//        budgetDisplay = findViewById(R.id.budget_display)
//        buttonExpense = findViewById(R.id.button_expense)
//        buttonIncome = findViewById(R.id.button_income)
//
//        numberPadButtons = listOf(
//            findViewById(R.id.button_0),
//            findViewById(R.id.button_1),
//            findViewById(R.id.button_2),
//            findViewById(R.id.button_3),
//            findViewById(R.id.button_4),
//            findViewById(R.id.button_5),
//            findViewById(R.id.button_6),
//            findViewById(R.id.button_7),
//            findViewById(R.id.button_8),
//            findViewById(R.id.button_9),
//            findViewById(R.id.button_dot),
//            findViewById(R.id.button_backspace)
//        )
//    }
//
//    private fun setupSpinner() {
//        val categories = listOf("Overall", "Food", "Transport", "Utilities", "Entertainment")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        categorySpinner.adapter = adapter
//    }
//
//    private fun setupNumberPad() {
//        for (button in numberPadButtons) {
//            button.setOnClickListener {
//                when (val label = (it as Button).text.toString()) {
//                    "⌫" -> {
//                        budgetDisplay.text = budgetDisplay.text.dropLast(1)
//                    }
//                    "." -> {
//                        if (!budgetDisplay.text.contains(".")) {
//                            budgetDisplay.append(".")
//                        }
//                    }
//                    else -> {
//                        budgetDisplay.append(label)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun setupButtons() {
//        buttonExpense.setOnClickListener {
//            Toast.makeText(this, "Expense selected: ${budgetDisplay.text}", Toast.LENGTH_SHORT).show()
//        }
//
//        buttonIncome.setOnClickListener {
//            Toast.makeText(this, "Income selected: ${budgetDisplay.text}", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
