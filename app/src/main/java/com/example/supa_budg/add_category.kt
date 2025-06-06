package com.example.supa_budg

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.AppDatabase
import com.example.supa_budg.data.Category
import kotlinx.coroutines.launch

class AddCategory : AppCompatActivity() {

    private lateinit var categoryNameInput: EditText
    private lateinit var imagePickerContainer: LinearLayout
    private lateinit var imagePicker: ImageView
    private lateinit var imageFileName: TextView
    private lateinit var budgetGoalInput: EditText
    private lateinit var saveCategoryButton: Button
    private lateinit var errorText: TextView
    private lateinit var backButton: ImageButton

    private val imagePickRequestCode = 1001
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_category)

        // Initialize views
        categoryNameInput = findViewById(R.id.categoryNameInput)
        imagePickerContainer = findViewById(R.id.imagePickerContainer)
        imagePicker = findViewById(R.id.imagePicker)
        imageFileName = findViewById(R.id.imageFileName)
        budgetGoalInput = findViewById(R.id.budgetGoalInput)
        saveCategoryButton = findViewById(R.id.saveCategoryButton)
        errorText = findViewById(R.id.errorText)
        backButton = findViewById(R.id.backButton)

        // Set the click listener for the image picker container
        imagePickerContainer.setOnClickListener {
            openImagePicker()
        }

        // Handle back button click
        backButton.setOnClickListener {
            finish()
        }

        // Set the click listener for the save button
        saveCategoryButton.setOnClickListener {
            val categoryName = categoryNameInput.text.toString().trim()
            val imageUrl = selectedImageUri?.toString() ?: ""
            val budgetGoal = budgetGoalInput.text.toString().toIntOrNull()

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val categoryDao = db.categoryDao()

                // Validation logic
                when {
                    categoryName.isEmpty() -> showError(errorText, "Category name cannot be empty.")
                    categoryName.length <= 4 -> showError(errorText, "Category name must be longer than 4 characters.")
                    categoryName.any { it.isDigit() } -> showError(errorText, "Category name cannot contain numbers.")
                    categoryDao.getCategoryByName(categoryName) != null -> showError(errorText, "Category already exists.")
                    budgetGoal == null -> showError(errorText, "Please enter a valid budget goal.")
                    budgetGoal < 0 -> showError(errorText, "Budget goal cannot be negative.")
                    else -> {
                        // Category does not exist, proceed with saving
                        val newCategory = Category(
                            name = categoryName,
                            imageUrl = imageUrl,
                            goal = budgetGoal
                        )
                        categoryDao.insertCategory(newCategory)

                        runOnUiThread {
                            errorText.text = "Category added successfully."
                            errorText.setTextColor(getColor(R.color.green))
                            errorText.visibility = TextView.VISIBLE

                            // Clear inputs
                            categoryNameInput.text.clear()
                            budgetGoalInput.text.clear()
                            imageFileName.text = ""
                            selectedImageUri = null

                            // Return to previous screen
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, imagePickRequestCode)
    }

    // Handling the result of the image picker activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagePickRequestCode && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            val imagePath = selectedImageUri.toString()
            imageFileName.text = imagePath
        } else {
            Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to show error messages
    private fun showError(textView: TextView, message: String) {
        runOnUiThread {
            textView.text = message
            textView.setTextColor(getColor(R.color.red))
            textView.visibility = TextView.VISIBLE
        }
    }
}
