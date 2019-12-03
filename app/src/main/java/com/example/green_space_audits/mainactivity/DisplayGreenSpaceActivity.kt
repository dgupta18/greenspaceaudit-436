package com.example.green_space_audits.mainactivity

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import android.util.Log
import kotlinx.android.synthetic.main.activity_displaygreenspace.*
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_displaygreenspace.linear_layout
import kotlinx.android.synthetic.main.testingdisplay.*
import com.google.firebase.storage.StorageReference
import java.io.File


class DisplayGreenSpaceActivity : AppCompatActivity() {
    private lateinit var nameTV: TextView
    private lateinit var acresTV: TextView
    private lateinit var qualityTV: TextView
    private lateinit var typeTV: TextView
    private lateinit var quietTV: TextView
    private lateinit var hazardsTV: TextView
    private lateinit var gsDatabase: DatabaseReference
    private lateinit var greenspaceID: String

    private var mStorageRef: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_displaygreenspace)

        gsDatabase = FirebaseDatabase.getInstance().getReference("GreenSpaces")
        mStorageRef = FirebaseStorage.getInstance().getReference()

        nameTV = findViewById<TextView>(R.id.nameView)
        acresTV = findViewById<TextView>(R.id.acresView)
        qualityTV = findViewById<TextView>(R.id.qualityView)
        typeTV = findViewById<TextView>(R.id.typeView)
        quietTV = findViewById<TextView>(R.id.quietView)
        hazardsTV = findViewById<TextView>(R.id.hazardsView)

        val context = this
        greenspaceID = intent.getStringExtra("gsID")

        // this is for testing purposes
//        greenspaceID = "-Luk55Tvcj5CjArCxliA"
//        greenspaceID = "-Luxf6N0TQ4dDATfyYQY"

        val commentsSet = mutableSetOf<String>()

        // use an addValueListener to get the current user's username
        gsDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i("DISPLAY: ", "id: " + greenspaceID)
                Log.i("DISPLAY: ", "nameTv: " + nameTV.toString())
                Log.i("DISPLAY: ", "child: " + dataSnapshot.child(greenspaceID).toString())
                Log.i("DISPLAY: ", "getValue: " + dataSnapshot.child(greenspaceID).getValue(GreenSpace::class.java).toString())
                Log.i("DISPLAY: ", "gsName: " + dataSnapshot.child(greenspaceID).getValue(GreenSpace::class.java)!!.gsName)

                nameTV.text = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsName
                acresTV.text = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsAcres.toString()

                val qual = (dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsAvgQuality + 0.5).toInt()
                if(qual == 1){
                    qualityTV.text = "Low"
                } else if(qual == 2) {
                    qualityTV.text = "Medium"
                } else {
                    qualityTV.text = "High"
                }

                typeTV.text = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsType.displayStr

                Log.i("QUIET: ", "" + dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsIsQuiet)
                if(dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsIsQuiet) {
                    quietTV.text = "Quiet"
                } else {
                    quietTV.text = "Noisy"
                }

                if(dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsIsNearHazards) {
                    hazardsTV.text = "Near hazards"
                } else {
                    hazardsTV.text = "Not near hazards"
                }

                for(entry in dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsComments){
                    if(!commentsSet.contains(entry.key)) {

                        var localFile = File.createTempFile("images", "jpg")
                        mStorageRef!!.getFile(localFile)










                        commentsSet.add(entry.key)
                        val commentTV = TextView(context)
                        val authorTV = TextView(context)
                        commentTV.textSize = 20f
                        commentTV.text = entry.value.comment
                        commentTV.setPadding(70, 0, 0, 40)

                        authorTV.textSize = 20f
                        authorTV.text = entry.value.authorDisplayName
                        authorTV.setTypeface(authorTV.getTypeface(), Typeface.BOLD)
                        authorTV.setPadding(70, 0, 0, 0)
                        authorTV.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))

                        // add TextView to LinearLayout
                        linear_layout.addView(authorTV)
                        linear_layout.addView(commentTV)
                        // add pictures
//                        picture_layout.addView()

                    }
                }
            }
            // I'm not sure why this is necessary, but it was included in the Firebase lab
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

}