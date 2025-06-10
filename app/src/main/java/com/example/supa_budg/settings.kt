package com.example.supa_budg

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        val exitSection = findViewById<LinearLayout>(R.id.exitSection)
        exitSection.setOnClickListener {
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val settingsIcon = findViewById<LinearLayout>(R.id.achievementsSection)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, Achievements::class.java)
            startActivity(intent)
        }

        val closeButton = findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
