package com.example.supa_budg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supa_budg.data.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class Dashboard : AppCompatActivity() {

    private lateinit var entryRecyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var totalAllTime: TextView
    private lateinit var total7Days: TextView
    private lateinit var total30Days: TextView

    private lateinit var dbRef: DatabaseReference
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        entryRecyclerView = findViewById(R.id.rvEntries)
        addButton = findViewById(R.id.btnAddEntry)
        totalAllTime = findViewById(R.id.tvTotalAllTime)
        total7Days = findViewById(R.id.tvTotal7Days)
        total30Days = findViewById(R.id.tvTotal30Days)
        val settingsIcon = findViewById<ImageView>(R.id.ivSettings)

        settingsIcon.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        entryRecyclerView.layoutManager = LinearLayoutManager(this)

        addButton.setOnClickListener {
            startActivity(Intent(this, AddEntry::class.java))
        }

        dbRef = FirebaseDatabase.getInstance().getReference("User").child(uid).child("Entry")

        loadDashboardData()
        setupFooter()
    }

    private fun loadDashboardData() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = mutableListOf<Entry>()
                for (child in snapshot.children) {
                    val entry = child.getValue(Entry::class.java)
                    if (entry != null) {
                        entries.add(entry)
                    }
                }

                updateTotals(entries)
            }

            override fun onCancelled(error: DatabaseError) {
                totalAllTime.text = "Error loading entries"
                total7Days.text = ""
                total30Days.text = ""
            }
        })
    }

    private fun updateTotals(entries: List<Entry>) {
        val today = LocalDate.now()
        val last7 = today.minusDays(7)
        val last30 = today.minusDays(30)

        var totalAll = 0
        var total7 = 0
        var total30 = 0

        for (entry in entries) {
            val entryDate = Instant.parse(entry.date.toString()).atZone(ZoneId.systemDefault()).toLocalDate()
            val amount = if (entry.isExpense) -entry.amount else entry.amount

            totalAll += amount
            if (entryDate >= last30) total30 += amount
            if (entryDate >= last7) total7 += amount
        }

        totalAllTime.text = "All Time: R$totalAll"
        total7Days.text = "Last 7 Days: R$total7"
        total30Days.text = "Last 30 Days: R$total30"
    }

    private fun setupFooter() {
        val homeButton = findViewById<ImageButton>(R.id.footerHome)
        val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
        val addEntryButton = findViewById<ImageButton>(R.id.footerGraph)
        val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

        homeButton.setColorFilter(ContextCompat.getColor(this, R.color.blue))
        homeButton.setBackgroundResource(R.drawable.footer_button_bg)
        homeButton.isEnabled = false

        homeButton.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }

        addEntryButton.setOnClickListener {
            startActivity(Intent(this, Graph::class.java))
            finish()
        }

        calendarButton.setOnClickListener {
            startActivity(Intent(this, EntryCalender::class.java))
            finish()
        }

        budgetButton.setOnClickListener {
            startActivity(Intent(this, MonthlyBudget::class.java))
            finish()
        }
    }
}
