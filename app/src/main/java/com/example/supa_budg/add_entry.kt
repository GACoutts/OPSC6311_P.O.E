import android.os.Bundle
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.example.supa_budg.R

class AddEntry : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the file
        setContentView(R.layout.add_income_expense)

        // Get Fields
        val toggleButton = findViewById<ToggleButton>(R.id.toggleButton)
        val textView = findViewById<TextView>(R.id.amount)
        var isExpense = true

        // Use Fields
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleButton.text = getString(R.string.hint_income)
                textView.text = getString(R.string.hint_positive_amount)
                isExpense = false
            } else {
                toggleButton.text = getString(R.string.hint_expense)
                textView.text = getString(R.string.hint_initial_amount)
                isExpense = true
            }
        }
    }
}