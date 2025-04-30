package com.example.supa_budg

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        val textView = findViewById<TextView>(R.id.amount)
        dateText = findViewById(R.id.dateText)
        val calendarRow = findViewById<LinearLayout>(R.id.calender)
        val attachPhotoRow = findViewById<LinearLayout>(R.id.attachPhotoRow)
        attachPhotoText = findViewById(R.id.attachPhoto)

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
                textView.text = getString(R.string.hint_positive_amount)
                isExpense = false
            } else {
                toggleButton.text = getString(R.string.hint_expense)
                textView.text = getString(R.string.hint_initial_amount)
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