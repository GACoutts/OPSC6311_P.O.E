package com.example.supa_budg

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class Signup : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signupButton: Button
    private lateinit var loginRedirect: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        emailInput = findViewById(R.id.emailField)
        passwordInput = findViewById(R.id.passwordField)
        signupButton = findViewById(R.id.loginButton)
        loginRedirect = findViewById(R.id.loginText)

        val dbRef = FirebaseDatabase.getInstance().getReference("User")

        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim().lowercase()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emailKey = email.hashCode().toString()
            val uid = UUID.randomUUID().toString()

            val userData = mapOf(
                "email" to email,
                "password" to password,
                "uid" to uid
            )

            dbRef.child(emailKey).setValue(userData).addOnSuccessListener {
                // Save uid to shared prefs
                getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                    .edit()
                    .putString("uid", uid)
                    .apply()

                Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Dashboard::class.java))
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Signup failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        loginRedirect.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }
}
