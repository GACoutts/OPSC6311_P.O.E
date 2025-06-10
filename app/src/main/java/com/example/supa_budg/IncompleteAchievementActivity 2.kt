/*
package com.example.supa_budg

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class IncompleteAchievementActivity : AppCompatActivity() {

    private lateinit var konfettiView: KonfettiView
    private lateinit var dbRef: DatabaseReference
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incomplete_achievement)

        // Find views
        val titleText = findViewById<TextView>(R.id.lockedAchievementTitle)
        val descriptionText = findViewById<TextView>(R.id.lockedAchievementDescription)
        val completeButton = findViewById<Button>(R.id.completeButton)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)

        // Create konfetti view
        konfettiView = KonfettiView(this)
        addContentView(
            konfettiView,
            android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Get data from intent
        val title = intent.getStringExtra("title") ?: "Locked Achievement"
        val description = intent.getStringExtra("description") ?: "Achievement details missing."

        titleText.text = title
        descriptionText.text = description

        // Close button functionality
        closeButton.setOnClickListener {
            finish()
        }

        // Confetti and DB update logic
        completeButton.setOnClickListener {
            if (uid.isEmpty()) return@setOnClickListener

            val incomesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("incomes")

            incomesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Show konfetti
                        konfettiView.start(
                            Party(
                                speed = 0f to 30f,
                                maxSpeed = 50f,
                                damping = 0.9f,
                                spread = 360,
                                colors = listOf(0xFFF44336.toInt(), 0xFF4CAF50.toInt(), 0xFF2196F3.toInt()),
                                emitter = Emitter(duration = 1, TimeUnit.SECONDS).perSecond(100),
                                position = Position.Relative(0.5, 0.3),
                                shapes = listOf(Shape.Circle, Shape.Square)
                            )
                        )

                        // Update Realtime Database with achievement
                        val achievementRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("achievements")
                            .child("add_income")

                        val achievementData = mapOf(
                            "title" to "Added an Income",
                            "description" to "You've successfully added an income entry!",
                            "iscompleted" to true,
                            "category" to "income"
                        )

                        achievementRef.setValue(achievementData)
                    } else {
                        Toast.makeText(this@IncompleteAchievementActivity, "No income found. Please add an income first.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@IncompleteAchievementActivity, "Error checking incomes.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
*/