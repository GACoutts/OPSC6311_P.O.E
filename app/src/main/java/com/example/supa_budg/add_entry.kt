package com.example.supa_budg

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.Entry
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class AddEntry : AppCompatActivity() {

    private lateinit var dateText: TextView
    private lateinit var attachPhotoText: TextView
    private val pickImageRequest = 1

    private lateinit var userKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_expense)

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

        val calendar = Calendar.getInstance()
        updateDateText(calendar)

        // Find actual userKey using saved UID
        val uid = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("uid", "") ?: ""

        lifecycleScope.launch {
            val userSnapshot = FirebaseDatabase.getInstance().getReference("User")
                .orderByChild("uid")
                .equalTo(uid)
                .get()
                .await()

            if (userSnapshot.exists()) {
                userKey = userSnapshot.children.first().key!!
            } else {
                Toast.makeText(this@AddEntry, "User not found in DB", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            toggleButton.text = if (isChecked) getString(R.string.hint_income) else getString(R.string.hint_expense)
            amountPrefix.text = if (isChecked) "R" else "- R"
        }

        calendarRow.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                updateDateText(calendar)
            }, year, month, day).show()
        }

        attachPhotoRow.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), pickImageRequest)
        }

        checkButton.setOnClickListener {
            val amountText = amountInput.text.toString()
            val amount = amountText.toDoubleOrNull()?.toInt() ?: 0
            val notes = findViewById<EditText>(R.id.notes).text.toString()
            val selectedDateStr = dateText.text.toString()
            val selectedDate = SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).parse(selectedDateStr)
            val photoUri = attachPhotoText.text.toString().takeIf { it != getString(R.string.hint_image) }
            val selectedCategoryName = categoryTextView.text.toString()
            val isExpense = !toggleButton.isChecked

            when {
                amount <= 0 -> return@setOnClickListener showError(errorText, "Please enter a valid amount.")
                selectedDate == null -> return@setOnClickListener showError(errorText, "Invalid date.")
                selectedCategoryName == getString(R.string.hint_category) -> return@setOnClickListener showError(errorText, "Please select a category.")
                photoUri == null -> return@setOnClickListener showError(errorText, "Please attach a photo.")
            }

            errorText.visibility = TextView.GONE

            lifecycleScope.launch {
                try {
                    val categoryRef = FirebaseDatabase.getInstance()
                        .getReference("User").child(userKey).child("Category")

                    val categorySnapshot = categoryRef.orderByChild("name")
                        .equalTo(selectedCategoryName)
                        .get()
                        .await()

                    if (!categorySnapshot.exists()) {
                        showError(errorText, "Selected category not found.")
                        return@launch
                    }

                    val categoryId = categorySnapshot.children.first().key ?: return@launch

                    val entryRef = FirebaseDatabase.getInstance()
                        .getReference("User").child(userKey).child("Entry")
                    val entryId = entryRef.push().key ?: return@launch

                    val newEntry = Entry(
                        entryId = entryId,
                        amount = amount,
                        date = LocalDateTime.now(),
                        categoryid = categoryId,
                        notes = notes,
                        photoUri = photoUri,
                        isExpense = isExpense
                    )

                    entryRef.child(entryId).setValue(newEntry).addOnSuccessListener {
                        Toast.makeText(this@AddEntry, "Entry saved!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@AddEntry, Dashboard::class.java))
                        finish()
                    }

                } catch (e: Exception) {
                    showError(errorText, "Error: ${e.message}")
                }
            }
        }

        categoryTextView.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val catSnapshot = FirebaseDatabase.getInstance()
                        .getReference("User").child(userKey).child("Category")
                        .get().await()

                    val categories = catSnapshot.children.mapNotNull {
                        it.child("name").getValue(String::class.java)
                    }.toMutableList()

                    categories.add("Add Category")

                    runOnUiThread {
                        val builder = AlertDialog.Builder(this@AddEntry)
                        builder.setTitle("Select Category")
                        builder.setItems(categories.toTypedArray()) { _, which ->
                            if (which == categories.lastIndex) {
                                startActivity(Intent(this@AddEntry, AddCategory::class.java))
                            } else {
                                categoryTextView.text = categories[which]
                                errorText.visibility = TextView.GONE
                            }
                        }
                        builder.show()
                    }

                } catch (e: Exception) {
                    showError(errorText, "Failed to load categories.")
                }
            }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.relativeLayout)) { view, insets ->
            view.setPadding(
                view.paddingLeft,
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
    }

    private fun updateDateText(calendar: Calendar) {
        val format = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
        dateText.text = format.format(calendar.time)
    }

    @Deprecated("Use Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequest && resultCode == RESULT_OK && data?.data != null) {
            val uri = data.data!!
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

    private fun showError(textView: TextView, message: String) {
        runOnUiThread {
            textView.text = message
            textView.setTextColor(getColor(R.color.red))
            textView.visibility = TextView.VISIBLE
        }
    }
}
