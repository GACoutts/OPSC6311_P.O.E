package com.example.supa_budg

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.AppDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {

    // Instance Fields
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var errorText: TextView
    private lateinit var registerText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)


        // Get fields from boxes
        usernameField = findViewById(R.id.usernameField)
        passwordField = findViewById(R.id.passwordField)
        loginButton = findViewById(R.id.loginButton)
        errorText = findViewById(R.id.errorText)
        registerText = findViewById(R.id.registerText)

        // if they start typing with an error remove it
        usernameField.setOnFocusChangeListener { _, _ -> errorText.visibility = View.GONE }
        passwordField.setOnFocusChangeListener { _, _ -> errorText.visibility = View.GONE }

        // Try login login
        loginButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Query database on background thread
            lifecycleScope.launch {
                val userDao = AppDatabase.getDatabase(this@Login).userDao()
                val user = userDao.getUserByUsername(username)

                withContext(Dispatchers.Main) {
                    if (user != null && user.password == password) {
                        Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@Login, Dashboard::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Login, "Invalid username or password", Toast.LENGTH_SHORT).show()
                        // When information is incorrect
                        errorText.text = getString(R.string.error_invalid_login)
                        errorText.visibility = View.VISIBLE
                    }
                }
            }
        }

        registerText.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }

    }
}
