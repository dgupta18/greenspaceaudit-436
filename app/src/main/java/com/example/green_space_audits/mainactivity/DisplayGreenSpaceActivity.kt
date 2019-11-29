package com.example.green_space_audits.mainactivity

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import android.util.Log
import kotlinx.android.synthetic.main.activity_displaygreenspace.*
import androidx.core.content.ContextCompat

class DisplayGreenSpaceActivity : AppCompatActivity() {
    private lateinit var nameTV: TextView
    private lateinit var acresTV: TextView
    private lateinit var qualityTV: TextView
    private lateinit var typeTV: TextView
    private lateinit var quietTV: TextView
    private lateinit var hazardsTV: TextView
    private lateinit var gsDatabase: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_displaygreenspace)

        gsDatabase = FirebaseDatabase.getInstance().getReference("GreenSpaces")

        nameTV = findViewById<TextView>(R.id.nameView)
        acresTV = findViewById<TextView>(R.id.acresView)
        qualityTV = findViewById<TextView>(R.id.qualityView)
        typeTV = findViewById<TextView>(R.id.typeView)
        quietTV = findViewById<TextView>(R.id.quietView)
        hazardsTV = findViewById<TextView>(R.id.hazardsView)
        val context = this


        // use an addValueListener to get the current user's username
        gsDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                nameTV.text = dataSnapshot.child("-Luk55Tvcj5CjArCxliA").getValue<GreenSpace>(GreenSpace::class.java)!!.gsName
                acresTV.text = dataSnapshot.child("-Luk55Tvcj5CjArCxliA").getValue<GreenSpace>(GreenSpace::class.java)!!.gsAcres.toString()

                val qual = (dataSnapshot.child("-Luk55Tvcj5CjArCxliA").getValue<GreenSpace>(GreenSpace::class.java)!!.gsAvgQuality + 0.5).toInt()
                if(qual == 1){
                    qualityTV.text = "Low"
                } else if(qual == 2) {
                    qualityTV.text = "Medium"
                } else {
                    qualityTV.text = "High"
                }

                typeTV.text = dataSnapshot.child("-Luk55Tvcj5CjArCxliA").getValue<GreenSpace>(GreenSpace::class.java)!!.gsType.displayStr

                if(dataSnapshot.child("-Luk55Tvcj5CjArCxliA").getValue<GreenSpace>(GreenSpace::class.java)!!.isQuiet) {
                    quietTV.text = "Quiet"
                } else {
                    quietTV.text = "Not quiet"
                }

                if(dataSnapshot.child("-Luk55Tvcj5CjArCxliA").getValue<GreenSpace>(GreenSpace::class.java)!!.isNearHazards) {
                    hazardsTV.text = "Near hazards"
                } else {
                    hazardsTV.text = "Not near hazards"
                }

                for(entry in dataSnapshot.child("-Luk55Tvcj5CjArCxliA").getValue<GreenSpace>(GreenSpace::class.java)!!.gsComments){
                    val commentTV = TextView(context)
                    val authorTV = TextView(context)
                    commentTV.textSize = 20f
                    commentTV.text = entry.value.comment
                    commentTV.setPadding(70,0,0,40)

                    authorTV.textSize = 20f
                    authorTV.text = entry.value.authorDisplayName
                    authorTV.setTypeface(authorTV.getTypeface(), Typeface.BOLD)
                    authorTV.setPadding(70,0,0,0)
                    authorTV.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))

                    // add TextView to LinearLayout
                    linear_layout.addView(authorTV)
                    linear_layout.addView(commentTV)
                }
            }
            // I'm not sure why this is necessary, but it was included in the Firebase lab
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })




    }

}