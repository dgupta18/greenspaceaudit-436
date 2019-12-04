package com.example.green_space_audits.mainactivity

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_displaygreenspace.*
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class DisplayGreenSpaceActivity : AppCompatActivity() {
    private lateinit var nameTV: TextView
    private lateinit var acresTV: TextView
    private lateinit var qualityTV: TextView
    private lateinit var typeTV: TextView
    private lateinit var quietTV: TextView
    private lateinit var hazardsTV: TextView
    private lateinit var gsDatabase: DatabaseReference
    private lateinit var greenspaceID: String

    private lateinit var imageView: LinearLayout

    private lateinit var mStorageRef: StorageReference


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

//        imageView = findViewById(R.id.thumbnails)


        val context = this
        greenspaceID = intent.getStringExtra("gsID")

        // check to make sure the green space id from the intent is not null
        if(greenspaceID == null){
            Toast.makeText(this, "Unable to display this greenspace", Toast.LENGTH_LONG).show()
        } else {

            // fetching the images from firebase Storage and displaying
            mStorageRef = FirebaseStorage.getInstance().getReference(greenspaceID)
            mStorageRef.listAll().addOnSuccessListener(OnSuccessListener<ListResult> { result ->
                for (fileRef in result.items) {
                    fileRef.downloadUrl.addOnSuccessListener {


                        var imageView = ImageView(applicationContext)

                        Picasso.get().load(it).into(imageView)

                        thumbnails.addView(imageView)


                    }
                }
            }).addOnFailureListener(OnFailureListener {
                // Handle any errors
            })






            val commentsSet = mutableSetOf<String>()

            // use an addValueListener to get the green space's information
            gsDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.i("DISPLAY: ", "id: " + greenspaceID)
                    Log.i("DISPLAY: ", "nameTv: " + nameTV.toString())
                    Log.i("DISPLAY: ", "child: " + dataSnapshot.child(greenspaceID).toString())
                    Log.i("DISPLAY: ", "getValue: " + dataSnapshot.child(greenspaceID).getValue(GreenSpace::class.java).toString())
                    Log.i("DISPLAY: ", "gsName: " + dataSnapshot.child(greenspaceID).getValue(GreenSpace::class.java)!!.gsName)

                    // set the text value for the name, acres, and type text views
                    nameTV.text = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsName
                    acresTV.text = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsAcres.toString()
                    typeTV.text = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsType.displayStr

                    // determine which quality level to display based on the avereage quality ranking of the green space
                    val qual = (dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsAvgQuality + 0.5).toInt()
                    if(qual == 1){
                        qualityTV.text = "Low"
                    } else if(qual == 2) {
                        qualityTV.text = "Medium"
                    } else {
                        qualityTV.text = "High"
                    }

                    // set the quiet TextView text based on the value of gsIsQuiet
                    Log.i("QUIET: ", "" + dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsIsQuiet)
                    if(dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsIsQuiet) {
                        quietTV.text = "Quiet"
                    } else {
                        quietTV.text = "Noisy"
                    }

                    // set the hazards TextView text based on the value of gsIsNearHazards
                    if(dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsIsNearHazards) {
                        hazardsTV.text = "Near hazards"
                    } else {
                        hazardsTV.text = "Not near hazards"
                    }

                    // loop through the green space's comments, create a TextView for each comment author and comment text,
                    // and add them to the comments linear layout
                    for(entry in dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsComments){
                        // check to make sure this comment isn't already being displayed
                        // this is necessary because this will be called everytime the database is updated and
                        // we don't want duplicate comments
                        if(!commentsSet.contains(entry.key)) {
                            // add the comment id to commentsSet so it won't be displayed again
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

                            // add TextViews to LinearLayout
                            comments_linear_layout.addView(authorTV)
                            comments_linear_layout.addView(commentTV)
                        }
                    }
                }
                // I'm not sure why this is necessary, but it was included in the Firebase lab
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

    }

}