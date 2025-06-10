/*
package com.example.supa_budg

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import java.util.concurrent.TimeUnit

class AchievementDetailActivity : AppCompatActivity() {

    private lateinit var konfettiView: KonfettiView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement_detail)

        konfettiView = findViewById(R.id.konfettiView)
        val titleTextView = findViewById<TextView>(R.id.titleText)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionText)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)

        // Get data from intent
        val title = intent.getStringExtra("title") ?: "Achievement"
        val description = intent.getStringExtra("description") ?: "You've completed a task!"

        // Set text
        titleTextView.text = title
        descriptionTextView.text = description

        // Close button click listener: close this activity and return
        closeButton.setOnClickListener {
            finish()
        }

        // Start konfetti
        konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(100),
                position = Position.Relative(0.5, 0.3),
                shapes = listOf(Shape.Circle)
            )
        )
    }
}
*/