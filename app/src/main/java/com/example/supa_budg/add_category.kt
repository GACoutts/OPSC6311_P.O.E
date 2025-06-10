package com.example.supa_budg

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.supa_budg.data.Category
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

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

    private lateinit var dbRef: DatabaseReference
    private lateinit var userKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_category)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        categoryNameInput = findViewById(R.id.categoryNameInput)
        budgetGoalInput = findViewById(R.id.budgetGoalInput)
        saveCategoryButton = findViewById(R.id.saveCategoryButton)
        errorText = findViewById(R.id.errorText)
        imageFileName = findViewById(R.id.imageFileName)
        imagePicker = findViewById(R.id.imagePicker)
        imagePickerContainer = findViewById(R.id.imagePickerContainer)

        val savedUid = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("uid", "") ?: ""

        // 🔍 Resolve the user's key using the stored uid
        FirebaseDatabase.getInstance().getReference("User")
            .orderByChild("uid")
            .equalTo(savedUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userKey = snapshot.children.first().key!!
                        dbRef = FirebaseDatabase.getInstance().getReference("User").child(userKey).child("Category")
                    } else {
                        Toast.makeText(this@AddCategory, "User not found in database.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AddCategory, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        imagePickerContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
        }

        saveCategoryButton.setOnClickListener {
            if (!::dbRef.isInitialized) {
                showError("Database not ready. Please try again in a second.")
                return@setOnClickListener
            }

            val categoryName = categoryNameInput.text.toString().trim()
            val imageUrl = selectedImageUri?.toString() ?: ""
            val budgetGoal = budgetGoalInput.text.toString().toIntOrNull()

            when {
                categoryName.isEmpty() -> showError("Category name cannot be empty.")
                categoryName.length <= 4 -> showError("Category name must be longer than 4 characters.")
                categoryName.any { it.isDigit() } -> showError("Category name cannot contain numbers.")
                budgetGoal == null || budgetGoal < 0 -> showError("Please enter a valid, non-negative budget goal.")
                else -> checkIfCategoryExists(categoryName, imageUrl, budgetGoal)
            }
        }
    }

    private fun checkIfCategoryExists(name: String, imageUrl: String, goal: Int) {
        dbRef.orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        showError("Category already exists.")
                    } else {
                        saveCategory(name, imageUrl, goal)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Database error: ${error.message}")
                }
            })
    }

    private fun saveCategory(name: String, imageUrl: String, goal: Int) {
        val catId = dbRef.push().key!!
        val newCategory = Category(catId, name, imageUrl, goal)

        dbRef.child(catId).setValue(newCategory).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show()
                clearForm()
            } else {
                showError("Failed to save category.")
            }
        }
    }

    private fun clearForm() {
        categoryNameInput.text.clear()
        budgetGoalInput.text.clear()
        imageFileName.text = ""
        selectedImageUri = null
        errorText.text = ""
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.setTextColor(getColor(R.color.red))
        errorText.visibility = TextView.VISIBLE
    }

    @Deprecated("Use Activity Result API")
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

    private fun showError(textView: TextView, message: String) {
        runOnUiThread {
            textView.text = message
            textView.setTextColor(getColor(R.color.red))
            textView.visibility = TextView.VISIBLE
        }
    }
}
