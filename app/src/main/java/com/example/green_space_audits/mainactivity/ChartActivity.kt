package com.example.green_space_audits.mainactivity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        var chart = findViewById<PieChart>(R.id.pieChart)
        chart.setEntryLabelTextSize(12f)
        chart.setEntryLabelColor(Color.BLACK)
        chart.legend.textSize = 12f
        chart.legend.isWordWrapEnabled = true
        chart.description.text = ""
        chart.description.textSize = 32F
        chart.setBackgroundColor(Color.WHITE)
        chart.setHoleColor(Color.WHITE)

        var mDatabaseRef = FirebaseDatabase.getInstance().reference!!.child("GreenSpaces")
        var entries: ArrayList<PieEntry> = ArrayList()

        mDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var greenspaces: HashMap<String, Any> =
                    dataSnapshot.getValue() as HashMap<String, Any>

                for ((id, gsRaw) in greenspaces) {
                    Log.i("GS: ", id)
                    Log.i("GS: ", gsRaw.toString())

                    // cast
                    val gs: HashMap<String, Any> = gsRaw as HashMap<String, Any>

                    // get greenspace info
                    val name = gs["gsName"]!!.toString()
                    val checkIns: Long = gs["numRankings"] as Long
                    entries.add(PieEntry(checkIns.toFloat(), name))
                }
                var set = PieDataSet(entries, "")
                set.valueTextSize = 16f
                set.valueTextColor = Color.BLACK
                set.setColors(
                    Color.parseColor("#FFF6C4"),
                    Color.parseColor("#EB6F45"),
                    Color.parseColor("#D3E397"),
                    Color.parseColor("#8DCDC1"),
                    Color.parseColor("#FFCC33"),
                    Color.parseColor("#92CBFF"),
                    Color.parseColor("#FAA195"),
                    Color.parseColor("#028C79"),
                    Color.parseColor("#DBA797"),
                    Color.parseColor("#5A753F"),
                    Color.parseColor("#EB9844"),
                    Color.parseColor("#549D9E"),
                    Color.parseColor("#429783"),
                    Color.parseColor("#908F95"),
                    Color.parseColor("#C9CEA9"),
                    Color.parseColor("#429783"),
                    Color.parseColor("#CDBBAD"),
                    Color.parseColor("#796091")
                    )
                var data = PieData(set)
                chart.data = data
                chart.invalidate()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }

        })
    }
}
