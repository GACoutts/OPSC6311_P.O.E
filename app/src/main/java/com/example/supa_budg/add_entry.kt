package com.example.supa_budg

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class AddEntry : AppCompatActivity() {

    private lateinit var dateText: TextView
    private lateinit var attachPhotoText: TextView
    private lateinit var dbRef: DatabaseReference

    private val uid get() = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
    .getString("uid", null)
    private val pickImageRequest = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_expense)

        Log.d("AddEntry", "Retrieved UID: $uid")

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

        dbRef = FirebaseDatabase.getInstance().getReference("User").child(uid.toString())

        val calendar = Calendar.getInstance()
        updateDateText(calendar)

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleButton.text = getString(R.string.hint_income)
                amountPrefix.text = "R"
            } else {
                toggleButton.text = getString(R.string.hint_expense)
                amountPrefix.text = "- R"
            }
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

            // Validation
            when {
                amount <= 0 -> return@setOnClickListener showError(errorText, "Please enter a valid amount.")
                selectedDate == null -> return@setOnClickListener showError(errorText, "Invalid date.")
                selectedCategoryName == getString(R.string.hint_category) -> return@setOnClickListener showError(errorText, "Please select a category.")
                photoUri == null -> return@setOnClickListener showError(errorText, "Please attach a photo.")
            }

            errorText.visibility = TextView.GONE

            // Save to Firebase
            lifecycleScope.launch {
                try {
                    val categorySnapshot = dbRef.child("Category").get().await()

                    if (!categorySnapshot.exists()) {
                        showError(errorText, "No categories found.")
                        return@launch
                    }

                    // Find category node where name == selectedCategoryName
                    val matchingCategory = categorySnapshot.children.firstOrNull { snapshot ->
                        val name = snapshot.child("name").getValue(String::class.java)
                        name == selectedCategoryName
                    }

                    if (matchingCategory == null) {
                        showError(errorText, "Selected category not found.")
                        return@launch
                    }

                    val categoryId = matchingCategory.key ?: ""

                    val entryId = dbRef.child("Entry").push().key ?: return@launch

                    // Format the date properly, you might want to use selectedDate here
                    val entryDate = LocalDateTime.now().toString()

                    val newEntry = Entry(
                        entryId = entryId,
                        amount = amount,
                        date = entryDate,
                        categoryid = categoryId,
                        notes = notes,
                        photoUri = photoUri,
                        isExpense = isExpense
                    )

                    dbRef.child("Entry").child(entryId).setValue(newEntry).addOnSuccessListener {
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
                    val snapshot = dbRef.child("Category").get().await()
                    val categories = snapshot.children.mapNotNull {
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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
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
