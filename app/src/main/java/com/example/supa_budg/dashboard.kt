package com.example.supa_budg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supa_budg.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate


class Dashboard : AppCompatActivity() {

    private val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var adapter: EntryAdapter

    private lateinit var entryRecyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var totalAllTime: TextView
    private lateinit var total7Days: TextView
    private lateinit var total30Days: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Initialize views using findViewById
        entryRecyclerView = findViewById(R.id.rvEntries)
        addButton = findViewById(R.id.btnAddEntry)
        totalAllTime = findViewById(R.id.tvTotalAllTime)
        total7Days = findViewById(R.id.tvTotal7Days)
        total30Days = findViewById(R.id.tvTotal30Days)

        // Setup RecyclerView
        adapter = EntryAdapter(emptyList())
        entryRecyclerView.layoutManager = LinearLayoutManager(this)
        entryRecyclerView.adapter = adapter

        // Handle Add Button Click
        addButton.setOnClickListener {
            startActivity(Intent(this, add_income_expense::class.java))
        }

        // Load dashboard data
        loadDashboardData()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            val today = LocalDate.now()
            val last7 = today.minusDays(7)
            val last30 = today.minusDays(30)

            db.EntryDao().getEntriesBetween(last30.atStartOfDay(), today.atTime(23, 59, 59))
                .observe(this@Dashboard) { entryList ->
                    adapter.updateEntries(entryList)
                }


            val totalAll = withContext(Dispatchers.IO) {
                db.EntryDao().getTotalAmount()
            }

            val total7 = withContext(Dispatchers.IO) {
                db.EntryDao().getTotalAmountFromDate(last7.toString(), today.toString())
            }

            val total30 = withContext(Dispatchers.IO) {
                db.EntryDao().getTotalAmountFromDate(last30.toString(), today.toString())
            }

            adapter.updateEntries(entries)

            // Update text views
            totalAllTime.text = "All Time: $totalAll"
            total7Days.text = "Last 7 Days: $total7"
            total30Days.text = "Last 30 Days: $total30"
        }
    }
}
