package com.example.supa_budg

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Achievements : AppCompatActivity() {

    // Achievement details: key = circle view ID, value = Pair<status, message>
    private val achievementDetails: Map<Int, Pair<String, String>> = mapOf(
        R.id.circle1 to Pair("incomplete", "Login for the first time - Locked 🔒"),
        R.id.circle2 to Pair("incomplete", "add your first income - Locked 🔒"),
        R.id.circle3 to Pair("incomplete", "add 5 incomes total - Locked 🔒"),
        R.id.circle4 to Pair("incomplete", "add your first expense - Locked 🔒"),
        R.id.circle5 to Pair("incomplete", "create your first budget - Locked 🔒"),
        R.id.circle6 to Pair("incomplete", "Avoid takeout food for a month - Locked 🔒"),
        R.id.circle7 to Pair("incomplete", "Review your budget weekly - Locked 🔒"),
        R.id.circle8 to Pair("incomplete", "Achieve your monthly savings goal - Locked 🔒"),
        R.id.circle9 to Pair("incomplete", "Refer a friend - Locked 🔒"),
        R.id.circle10 to Pair("incomplete", "Set and stick to a daily spending limit - Locked 🔒"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.achievements)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Closes this activity and returns to the previous one
        }

        // Show Toast message on long click for all achievement circles
        achievementDetails.forEach { (viewId, details) ->
            val circleView = findViewById<FrameLayout>(viewId)
            circleView?.setOnLongClickListener {
                Toast.makeText(this, details.second, Toast.LENGTH_SHORT).show()
                true
            }
        }

// Updated listeners for incomplete locked achievements
        findViewById<FrameLayout>(R.id.circle1)?.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "Login for the first time")
            intent.putExtra("description", "Log into your account at least once to unlock this achievement.")
            startActivity(intent)
        }

        findViewById<FrameLayout>(R.id.circle2)?.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "Add your first income")
            intent.putExtra("description", "Record your first income entry to unlock this achievement.")
            startActivity(intent)
        }

        findViewById<FrameLayout>(R.id.circle3)?.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "Add 5 incomes total")
            intent.putExtra("description", "Add 5 income entries in total. Progress will be tracked and the achievement unlocked when complete.")
            startActivity(intent)
        }

        findViewById<FrameLayout>(R.id.circle4)?.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "Add your first expense")
            intent.putExtra("description", "Track your first expense to unlock this achievement.")
            startActivity(intent)
        }

        findViewById<FrameLayout>(R.id.circle5)?.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "Create your first budget")
            intent.putExtra("description", "Build your first budget and take control of your finances.")
            startActivity(intent)
        }


        val circle6 = findViewById<FrameLayout>(R.id.circle6)
        circle6.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "add another income")
            intent.putExtra("description", "you have to add another income ")
            startActivity(intent)
        }

        val circle7 = findViewById<FrameLayout>(R.id.circle7)
        circle7.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "login 5 times")
            intent.putExtra("description", "you need to login 5 times")
            startActivity(intent)
        }

        val circle8 = findViewById<FrameLayout>(R.id.circle8)
        circle8.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "Monthly Savings Goal")
            intent.putExtra("description", "Reach your self-set savings goal for the month to earn this achievement. Stay disciplined and focused!")
            startActivity(intent)
        }

        val circle9 = findViewById<FrameLayout>(R.id.circle9)
        circle9.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "Refer a Friend")
            intent.putExtra("description", "Help a friend start budgeting by referring them to this app. When they sign up, you'll unlock this achievement!")
            startActivity(intent)
        }

        val circle10 = findViewById<FrameLayout>(R.id.circle10)
        circle10.setOnClickListener {
            val intent = Intent(this, IncompleteAchievementActivity::class.java)
            intent.putExtra("title", "Stick to a Daily Limit")
            intent.putExtra("description", "To complete this challenge, set a daily spending limit and stick to it for 7 consecutive days.")
            startActivity(intent)
        }

        findViewById<FrameLayout>(R.id.circle10)?.setOnClickListener {
            val intent = Intent(this, LockedAchievementDetailActivity::class.java)
            intent.putExtra("title", "Daily Spending Limit")
            intent.putExtra("description", "Set and stick to a daily spending limit. Mastering this habit keeps your budget balanced.")
            startActivity(intent)
        }

    }
}
