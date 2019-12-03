package com.example.green_space_audits.mainactivity

import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class PreCheckInActivity: AppCompatActivity() {

    var greenspaces: MutableMap<String,String> = mutableMapOf()
    private lateinit var matchesHolder: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_precheckin)

        val context = this

        // save all extras into greenspaces map
        // name -> id
        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                greenspaces[key] = bundle.get(key) as String
            }
        }

        // get matchesHolder
        matchesHolder = findViewById(R.id.matches_container)

        // now insert a new TextView for each greenspace
        // onclick: the checkin page for the greenspace should come up
        for ((name, id) in greenspaces) {
            val match = TextView(context)
            match.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryDark))
            match.textSize = 20f
            match.text = name
            match.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            match.setOnClickListener {
                val intent = Intent(this@PreCheckInActivity, CheckinActivity::class.java)
                intent.putExtra("gsID", id)
                startActivity(intent)
                overridePendingTransition(0,0)
            }
            matchesHolder.addView(match)
        }
    }
}