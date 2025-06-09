package com.example.supa_budg

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var errorText: TextView
    private lateinit var registerText: TextView

    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // UI elements
        usernameField = findViewById(R.id.usernameField)
        passwordField = findViewById(R.id.passwordField)
        loginButton = findViewById(R.id.loginButton)
        errorText = findViewById(R.id.errorText)
        registerText = findViewById(R.id.registerText)

        dbRef = FirebaseDatabase.getInstance().getReference("users_auth")

        usernameField.setOnFocusChangeListener { _, _ -> errorText.visibility = View.GONE }
        passwordField.setOnFocusChangeListener { _, _ -> errorText.visibility = View.GONE }

        loginButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Query the custom auth path
            dbRef.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val storedPassword = snapshot.child("password").getValue(String::class.java)
                        val uid = snapshot.child("uid").getValue(String::class.java)

                        if (storedPassword == password && uid != null) {
                            // Save uid to SharedPreferences
                            getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                                .edit()
                                .putString("uid", uid)
                                .apply()

                            Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login, Dashboard::class.java))
                            finish()
                        } else {
                            showLoginError()
                        }
                    } else {
                        showLoginError()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Login, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        registerText.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }
    }

    private fun showLoginError() {
        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
        errorText.text = getString(R.string.error_invalid_login)
        errorText.visibility = View.VISIBLE
    }
}
