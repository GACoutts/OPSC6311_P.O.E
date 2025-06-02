package com.example.supa_budg

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

    private val IMAGE_PICK_REQUEST_CODE = 1001
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

        // Set the click listener for the image picker container
        imagePickerContainer.setOnClickListener {
            openImagePicker()
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

                            categoryNameInput.text.clear()
                            budgetGoalInput.text.clear()
                            imageFileName.text = ""
                            selectedImageUri = null
                        }
                    }
                }
            }
        }

        // Footer items
        val homeButton = findViewById<ImageButton>(R.id.footerHome)
        val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
        val addEntryButton = findViewById<ImageButton>(R.id.footerAddCategory)
        val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

        homeButton.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        addEntryButton.setOnClickListener {
            val intent = Intent(this, AddCategory::class.java)
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
            val intent = Intent(this, SetMonthyBudget::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    // Handling the result of the image picker activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
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
