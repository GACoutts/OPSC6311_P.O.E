package com.example.supa_budg

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.supa_budg.data.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_category)

        categoryNameInput = findViewById(R.id.categoryNameInput)
        budgetGoalInput = findViewById(R.id.budgetGoalInput)
        saveCategoryButton = findViewById(R.id.saveCategoryButton)
        errorText = findViewById(R.id.errorText)
        imageFileName = findViewById(R.id.imageFileName)
        imagePicker = findViewById(R.id.imagePicker)
        imagePickerContainer = findViewById(R.id.imagePickerContainer)

        dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("categories")

        imagePickerContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
        }

        saveCategoryButton.setOnClickListener {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            imageFileName.text = selectedImageUri.toString()
        }
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.setTextColor(getColor(R.color.red))
        errorText.visibility = TextView.VISIBLE
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