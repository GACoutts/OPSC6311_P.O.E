package com.example.supa_budg

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class IncompleteAchievementActivity : AppCompatActivity() {

    private lateinit var konfettiView: KonfettiView
    private lateinit var dbRef: DatabaseReference
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incomplete_achievement)

        // Find views from XML layout
        val titleText = findViewById<TextView>(R.id.lockedAchievementTitle)
        val descriptionText = findViewById<TextView>(R.id.lockedAchievementDescription)
        val completeButton = findViewById<Button>(R.id.completeButton)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("categories")

        // Create and inject konfetti view into layout
        konfettiView = KonfettiView(this)
        addContentView(
            konfettiView,
            android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Populate title and description from intent
        val title = intent.getStringExtra("title") ?: "Locked Achievement"
        val description = intent.getStringExtra("description") ?: "Achievement details missing."

        titleText.text = title
        descriptionText.text = description

        // Close button functionality
        closeButton.setOnClickListener {
            finish() // Closes this activity and returns to previous screen
        }

        // Confetti on "Completed" button click
        completeButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            FirebaseFirestore.getInstance()
                .collection("incomes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Show confetti
                        konfettiView.start(/* confetti setup */)

                        // Update Firestore achievements collection
                        FirebaseFirestore.getInstance()
                            .collection("achievements")
                            .document("add_income_$userId") // example ID
                            .set(
                                mapOf(
                                    "title" to "Added an Income",
                                    "description" to "You've successfully added an income entry!",
                                    "iscompleted" to true,
                                    "userId" to userId,
                                    "category" to "income"
                                )
                            )

                        // You can also navigate back or update UI state
                    } else {
                        Toast.makeText(this, "No income found. Please add an income first.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
}
