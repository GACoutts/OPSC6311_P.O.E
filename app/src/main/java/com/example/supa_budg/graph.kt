    package com.example.supa_budg

    import android.app.DatePickerDialog
    import android.content.Intent
    import android.graphics.Color
    import android.os.Bundle
    import android.widget.*
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import com.example.supa_budg.data.Category
    import com.example.supa_budg.data.Entry
    import com.github.mikephil.charting.charts.BarChart
    import com.github.mikephil.charting.components.LimitLine
    import com.github.mikephil.charting.components.XAxis
    import com.github.mikephil.charting.data.BarData
    import com.github.mikephil.charting.data.BarDataSet
    import com.github.mikephil.charting.data.BarEntry
    import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.*
    import kotlinx.coroutines.*
    import kotlinx.coroutines.tasks.await
    import java.time.*
    import java.time.format.DateTimeFormatter
    import java.util.*

    class Graph : AppCompatActivity() {

        private lateinit var barChart: BarChart
        private lateinit var resultsTextView: TextView
        private val uid get() = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            .getString("uid", null)

        private var selectedCategory: String? = null
        private var selectedStartDate: String? = null
        private var selectedEndDate: String? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.graph)

            barChart = findViewById(R.id.barChart)
            resultsTextView = findViewById(R.id.resultsTextView)

            setupFooter()

            findViewById<Button>(R.id.openModalButton).setOnClickListener {
                showGraphSettingsModal()
            }

            loadBarChartDataWithGoals()
        }

        private fun showGraphSettingsModal() {
            val dialogView = layoutInflater.inflate(R.layout.dialog_graph_settings, null)
            val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)

            val catRef = FirebaseDatabase.getInstance().getReference("User").child(uid.toString()).child("Category")
            catRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categories = mutableListOf<String>()
                    for (child in snapshot.children) {
                        child.child("name").getValue(String::class.java)?.let { categories.add(it) }
                    }
                    categories.add("Add Category")

                    val adapter = ArrayAdapter(this@Graph, android.R.layout.simple_spinner_item, categories)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            dialogView.findViewById<Button>(R.id.dateRangeButton).setOnClickListener {
                showDateRangePicker()
            }

            AlertDialog.Builder(this)
                .setTitle("Graph Settings")
                .setView(dialogView)
                .setPositiveButton("OK") { dialog, _ ->
                    val selected = categorySpinner.selectedItem.toString()
                    if (selected == "Add Category") {
                        startActivity(Intent(this, AddCategory::class.java))
                    } else {
                        selectedCategory = selected
                        loadBarChartDataWithGoals()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        private fun showDateRangePicker() {
            val calendar = Calendar.getInstance()
            val startDateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                selectedStartDate = "$year-${month + 1}-$dayOfMonth"
                val endDateListener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
                    selectedEndDate = "$y-${m + 1}-$d"
                    Toast.makeText(this, "Selected range: $selectedStartDate to $selectedEndDate", Toast.LENGTH_SHORT).show()
                    loadBarChartDataWithGoals()
                }

                DatePickerDialog(this, endDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            DatePickerDialog(this, startDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        private fun loadBarChartDataWithGoals() {
            val categoryRef = FirebaseDatabase.getInstance().getReference("User").child(uid.toString()).child("Category")
            val entryRef = FirebaseDatabase.getInstance().getReference("User").child(uid.toString()).child("Entry")

            CoroutineScope(Dispatchers.Main).launch {
                val categories = withContext(Dispatchers.IO) {
                    val snap = categoryRef.get().await()
                    snap.children.mapNotNull {
                        val id = it.key ?: return@mapNotNull null
                        val name = it.child("name").getValue(String::class.java) ?: return@mapNotNull null
                        val goal = it.child("goal").getValue(Int::class.java) ?: 0
                        Category(id, name, "", goal)
                    }
                }

                val entries = withContext(Dispatchers.IO) {
                    val snap = entryRef.get().await()
                    snap.children.mapNotNull { it.getValue(Entry::class.java) }
                }

                renderChart(entries, categories)
            }
        }

        private fun renderChart(entries: List<Entry>, categories: List<Category>) {
            val selectedCategoryId = selectedCategory?.let { catName ->
                categories.find { it.name == catName }?.categoryid
            }

            // Define date range: past 2 months to next 2 months from now
            val startMonth = YearMonth.now().minusMonths(2)
            val endMonth = YearMonth.now().plusMonths(2)

            println("=== All entries ===")
            entries.forEach {
                println("Entry: categoryid=${it.categoryid}, amount=${it.amount}, isExpense=${it.isExpense}, date=${it.createdDateFormat}")
            }

            // Filter and parse entries
            val filteredEntries = entries.mapNotNull { entry ->
                val dateStr = entry.createdDateFormat ?: return@mapNotNull null
                val date = try {
                    LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd")).atStartOfDay()
                } catch (e: Exception) {
                    null
                }
                if (date != null &&
                    !YearMonth.from(date).isBefore(startMonth) &&
                    !YearMonth.from(date).isAfter(endMonth) &&
                    (selectedCategoryId == null || entry.categoryid == selectedCategoryId)
                ) {
                    entry to date
                } else {
                    null
                }
            }

            println("=== Filtered entries within date range and category ===")
            filteredEntries.forEach { (entry, date) ->
                println("Entry: categoryid=${entry.categoryid}, amount=${entry.amount}, isExpense=${entry.isExpense}, date=$date")
            }

            // Aggregate income and expense by YearMonth
            val monthlyData = mutableMapOf<YearMonth, Pair<Float, Float>>()  // Pair<Income, Expense>
            for ((entry, date) in filteredEntries) {
                val ym = YearMonth.from(date)
                val current = monthlyData.getOrDefault(ym, 0f to 0f)
                if (entry.isExpense) {
                    monthlyData[ym] = current.first to current.second + entry.amount
                } else {
                    monthlyData[ym] = current.first + entry.amount to current.second
                }
            }

            // Generate full list of months from start to end
            val monthsInRange = mutableListOf<YearMonth>()
            var currentMonth = startMonth
            while (!currentMonth.isAfter(endMonth)) {
                monthsInRange.add(currentMonth)
                currentMonth = currentMonth.plusMonths(1)
            }

            // Prepare data for chart: for each month use monthlyData or zeroes
            val barEntries = mutableListOf<BarEntry>()
            val xLabels = mutableListOf<String>()
            var index = 0f
            for (monthKey in monthsInRange) {
                val data = monthlyData[monthKey] ?: (0f to 0f)
                barEntries.add(BarEntry(index, floatArrayOf(data.first, data.second)))
                xLabels.add(monthKey.month.name.substring(0, 3))  // Jan, Feb, etc.
                index += 1f
            }

            // If no data at all, clear chart and show message
//            if (barEntries.all { it.ySum == 0f }) {
//                barChart.clear()
//                resultsTextView.text = "No data available for the selected period."
//                return
//            }

            val barDataSet = BarDataSet(barEntries, "Income vs Expense").apply {
                colors = listOf(
                    ContextCompat.getColor(this@Graph, R.color.blue),
                    ContextCompat.getColor(this@Graph, R.color.red)
                )
                stackLabels = arrayOf("Income", "Expense")
                valueTextColor = Color.BLACK
                valueTextSize = 12f
            }

            val barData = BarData(barDataSet).apply { barWidth = 0.4f }

            barChart.apply {
                data = barData
                description.isEnabled = false
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
                xAxis.granularity = 1f
                axisLeft.removeAllLimitLines()

                // Optional goal lines (using all categories goals)
                val goalRange = categories.map { it.goal }
                if (goalRange.isNotEmpty()) {
                    goalRange.minOrNull()?.let { addGoalLine(it.toFloat(), "Min Goal", Color.RED) }
                    goalRange.maxOrNull()?.takeIf { it > (goalRange.minOrNull() ?: 0) }
                        ?.let { addGoalLine(it.toFloat(), "Max Goal", Color.GREEN) }
                }

                setFitBars(true)
                animateY(1000)
                invalidate()
            }

            resultsTextView.text = "Showing data from ${startMonth.atDay(1)} to ${endMonth.atEndOfMonth()}" +
                    (selectedCategory?.let { " for category: $it" } ?: "")
        }




        private fun BarChart.addGoalLine(value: Float, label: String, color: Int) {
            val goalLine = LimitLine(value, label).apply {
                lineColor = color
                lineWidth = 2f
                textColor = color
                textSize = 12f
            }
            axisLeft.addLimitLine(goalLine)
        }

        private fun setupFooter() {
            val homeButton = findViewById<ImageButton>(R.id.footerHome)
            val calendarButton = findViewById<ImageButton>(R.id.footerCalender)
            val graph = findViewById<ImageButton>(R.id.footerGraph)
            val budgetButton = findViewById<ImageButton>(R.id.footerBudget)

            graph.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            graph.setBackgroundResource(R.drawable.footer_button_bg)
            graph.isEnabled = false

            homeButton.setOnClickListener {
                startActivity(Intent(this, Dashboard::class.java))
                finish()
            }

            graph.setOnClickListener {
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

        // Helpers
        private fun String.toLocalDateOrNull(): LocalDate? {
            return try {
                val parts = this.split("-")
                LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } catch (e: Exception) {
                null
            }
        }
    }
