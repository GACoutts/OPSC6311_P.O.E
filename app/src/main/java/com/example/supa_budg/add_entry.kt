package com.example.supa_budg

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.example.supa_budg.data.Entry

import kotlinx.coroutines.launch

import java.util.Date

class AddEntry : AppCompatActivity() {

    // Variables
    private lateinit var dateText: TextView
    private lateinit var attachPhotoText: TextView
    private val pickImageRequest = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_expense)

        // Get Fields
        val toggleButton = findViewById<ToggleButton>(R.id.toggleButton)
        val amountPrefix = findViewById<TextView>(R.id.currencyPrefix)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val categoryTextView = findViewById<TextView>(R.id.category)
        val errorText = findViewById<TextView>(R.id.errorText)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val checkButton = findViewById<ImageButton>(R.id.checkButton)
        val calendarRow = findViewById<LinearLayout>(R.id.calender)
        val attachPhotoRow = findViewById<LinearLayout>(R.id.attachPhotoRow)
        dateText = findViewById(R.id.dateText)
        attachPhotoText = findViewById(R.id.attachPhoto)

        // Amount field
        val amountText = amountInput.text.toString()
        val amount = amountText.toDoubleOrNull() ?: 0.0

        // Database fields
        val db = AppDatabase.getDatabase(applicationContext)
        val categoryDao = db.categoryDao()

        var isExpense = true

        // Set default date
        val calendar = Calendar.getInstance()
        updateDateText(calendar, dateText)

        // Set today's date
        val currentDate = SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(Date())
        dateText.text = currentDate

        // Toggle logic
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleButton.text = getString(R.string.hint_income)
                amountPrefix.text = "R"
                isExpense = false
            } else {
                toggleButton.text = getString(R.string.hint_expense)
                amountPrefix.text = "- R"
                isExpense = true
            }
        }

        // Show DatePicker on calendar icon click
        calendarRow.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(Calendar.YEAR, selectedYear)
                    calendar.set(Calendar.MONTH, selectedMonth)
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                    updateDateText(calendar, dateText)
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        // When pressing on the attach photo open up image model
        attachPhotoRow.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), pickImageRequest)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.relativeLayout)) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(
                view.paddingLeft,
                statusBarHeight,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }

        backButton.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Submit an enityy
        checkButton.setOnClickListener {
            val amountText = amountInput.text.toString()
            val amount = amountText.toDoubleOrNull()?.toInt() ?: 0
            val notes = findViewById<EditText>(R.id.notes).text.toString()
            val selectedDateStr = dateText.text.toString()
            val selectedDate = SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).parse(selectedDateStr)
            val photoUri = attachPhotoText.text.toString().takeIf { it != getString(R.string.hint_image) }
            val selectedCategory = categoryTextView.text.toString()

            // Validation
            when {
                amount <= 0 -> {
                    errorText.text = "Please enter a valid amount greater than 0."
                    errorText.setTextColor(getColor(R.color.red))
                    errorText.visibility = TextView.VISIBLE
                    return@setOnClickListener
                }

                selectedDate == null -> {
                    errorText.text = "Please select a valid date."
                    errorText.setTextColor(getColor(R.color.red))
                    errorText.visibility = TextView.VISIBLE
                    return@setOnClickListener
                }

                selectedCategory == getString(R.string.hint_category) -> {
                    errorText.text = "Please select a category."
                    errorText.setTextColor(getColor(R.color.red))
                    errorText.visibility = TextView.VISIBLE
                    return@setOnClickListener
                }

                photoUri == null -> {
                    errorText.text = "Please attach a photo."
                    errorText.setTextColor(getColor(R.color.red))
                    errorText.visibility = TextView.VISIBLE
                    return@setOnClickListener
                }
            }

            // All validations passed
            errorText.visibility = TextView.GONE

            val localDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()

            lifecycleScope.launch {
                val categories = categoryDao.getAllCategoriesNow()
                val category = categories.find { it.name == selectedCategory }

                if (category == null) {
                    runOnUiThread {
                        errorText.text = "Selected category not found."
                        errorText.setTextColor(getColor(R.color.red))
                        errorText.visibility = TextView.VISIBLE
                    }
                    return@launch
                }

                val newEntry = Entry(
                    amount = amount,
                    date = localDate,
                    categoryid = category.categoryid,
                    notes = notes,
                    photoUri = photoUri,
                    isExpense = !toggleButton.isChecked
                )

                val entryDao = db.entryDao()
                entryDao.insertEntry(newEntry)

                runOnUiThread {
                    Toast.makeText(this@AddEntry, "Entry saved!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AddEntry, Dashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }


        // Select category modal
        categoryTextView.setOnClickListener {
            lifecycleScope.launch {
                val categories = categoryDao.getAllCategoriesNow()
                val categoryNames = categories.map { it.name }.toMutableList()
                categoryNames.add("Add Category")

                runOnUiThread {
                    val builder = AlertDialog.Builder(this@AddEntry)
                    builder.setTitle("Select Category")
                    builder.setItems(categoryNames.toTypedArray()) { _, which ->
                        if (which == categoryNames.size - 1) {
                            // User chose "Add Category"
                            val intent = Intent(this@AddEntry, AddCategory::class.java)
                            startActivity(intent)
                        } else {
                            // User chose an existing category
                            categoryTextView.text = categoryNames[which]
                            errorText.visibility = TextView.GONE
                        }
                    }
                    builder.show()
                }
            }
        }

    }

    private fun updateDateText(calendar: Calendar, textView: TextView) {
        val format = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
        textView.text = format.format(calendar.time)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageRequest && resultCode == RESULT_OK && data != null && data.data != null) {
            val uri: Uri = data.data!!
            val fileName = getFileNameFromUri(uri)
            attachPhotoText.text = fileName
        }
    }

        private fun getFileNameFromUri(uri: Uri): String {
            var name = "Selected Photo"
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst() && nameIndex >= 0) {
                    name = it.getString(nameIndex)
                }
            }
            return name
        }
}