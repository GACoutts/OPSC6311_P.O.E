package com.example.supa_budg

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.supa_budg.data.AppDatabase
import com.example.supa_budg.data.User
import kotlinx.coroutines.launch

class Signup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val usernameField = findViewById<EditText>(R.id.usernameField)
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val errorText = findViewById<TextView>(R.id.errorText)
        val loginText = findViewById<TextView>(R.id.loginText)

        loginButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val userDao = db.userDao()

                // Validation logic
                when {
                    username.length <= 3 -> showError(errorText, "Username must be more than 3 characters.")
                    userDao.getUserByUsername(username) != null -> showError(errorText, "Username already exists.")
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showError(errorText, "Invalid email format.")
                    userDao.getAllUsers().any { it.email == email } -> showError(errorText, "Email already registered.")
                    password.length < 5 || !password.any { it.isDigit() } -> showError(errorText, "Password must be at least 5 characters and include a number.")

                    else -> {
                        val user = User(name = username, email = email, password = password)
                        userDao.insert(user)
                        runOnUiThread {
                            errorText.text = getString(R.string.successful_register)
                            errorText.setTextColor(getColor(R.color.green))
                            errorText.visibility = TextView.VISIBLE

                            val intent = Intent(this@Signup, Login::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }

        loginText.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun showError(textView: TextView, message: String) {
        runOnUiThread {
            textView.text = message
            textView.setTextColor(getColor(R.color.red))
            textView.visibility = TextView.VISIBLE
        }
    }
}
