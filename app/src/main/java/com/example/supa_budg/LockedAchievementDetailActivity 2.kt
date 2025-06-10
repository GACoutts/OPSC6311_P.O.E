/*
package com.example.supa_budg

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LockedAchievementDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incomplete_achievement)

        // Get data from intent
        val title = intent.getStringExtra("title") ?: "Locked Achievement"
        val description = intent.getStringExtra("description") ?: "Details about what you need to do to unlock this achievement."

        // Assign views
        val titleTextView = findViewById<TextView>(R.id.lockedAchievementTitle)
        val descriptionTextView = findViewById<TextView>(R.id.lockedAchievementDescription)
        val lockIcon = findViewById<ImageView>(R.id.lockIcon)
        val completeButton = findViewById<Button>(R.id.completeButton)


        titleTextView.text = title
        descriptionTextView.text = description
        lockIcon.setImageResource(R.drawable.ic_lock)

        //  "Completed" button
        completeButton.setOnClickListener {
            Toast.makeText(this, "Well done! Keep working to unlock this achievement.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
*/