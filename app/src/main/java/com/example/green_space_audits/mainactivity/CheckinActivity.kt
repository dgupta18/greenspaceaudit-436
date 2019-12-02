package com.example.green_space_audits.mainactivity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CheckinActivity : AppCompatActivity() {
    private lateinit var nameTV: TextView
    private lateinit var qualityRG: RadioGroup
    private lateinit var commentET: EditText
    private lateinit var anonButton: CheckBox
    private lateinit var finishButton: Button
    private lateinit var greenspaceID: String
    private lateinit var gsDatabase: DatabaseReference
    private lateinit var usersDatabase: DatabaseReference
    private lateinit var user: String
    private lateinit var gsComments: MutableMap<String, Comment>
    private lateinit var userComments: MutableMap<String, Comment>
    private lateinit var userFavorites: MutableMap<String, String>
    private lateinit var gsName: String
    private lateinit var username: String
    private lateinit var favoriteButton: CheckBox
    private var gsAvgQual = 0.toFloat()
    private var gsNumRankings = 0
    private var userPoints = 0
    private var pointsEarned = 0

    private val quality: Quality
        get() {
            when (qualityRG.checkedRadioButtonId) {
                R.id.qualityLow -> {
                    return Quality.LOW
                }
                R.id.qualityHigh -> {
                    return Quality.HIGH
                }
                else -> {
                    return Quality.MED
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkin)

        nameTV = findViewById<TextView>(R.id.nameView)
        qualityRG = findViewById<RadioGroup>(R.id.qualityGroup)
        commentET = findViewById<EditText>(R.id.comment)
        anonButton = findViewById<CheckBox>(R.id.anonComment)
        finishButton = findViewById<Button>(R.id.finish)
        favoriteButton = findViewById<CheckBox>(R.id.favoriteButton)

        //greenspaceID = intent.getStringExtra("gsID")
        // TODO this hardcoded ID is for testing only. use the intent to pass the ID ^
        greenspaceID = "-Luk55Tvcj5CjArCxliA"
        gsDatabase = FirebaseDatabase.getInstance().getReference("GreenSpaces")
        usersDatabase = FirebaseDatabase.getInstance().getReference("Users")

        user = FirebaseAuth.getInstance().currentUser!!.uid

        // use an addValueListener to get the green space's name and comments
        gsDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                gsName = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsName
                nameTV.text = "Check in to " + gsName
                gsComments = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsComments
                gsAvgQual = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsAvgQuality
                gsNumRankings = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.numRankings
            }
            // I'm not sure why this is necessary, but it was included in the Firebase lab
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        // use an addValueListener to get the current user's username and comments
        usersDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                username = dataSnapshot.child(user).getValue<User>(User::class.java)!!.userName
                userPoints = dataSnapshot.child(user).getValue<User>(User::class.java)!!.userPoints
                if(dataSnapshot.child(user).child("uComments").value != null){
                    userComments = dataSnapshot.child(user).child("uComments").value as MutableMap<String, Comment>
                } else {
                    userComments = mutableMapOf<String, Comment>()
                }
                if(dataSnapshot.child(user).child("userFavorites").value != null){
                    userFavorites = dataSnapshot.child(user).child("userFavorites").value as MutableMap<String, String>
                } else {
                    userFavorites = mutableMapOf<String, String>()
                }
            }
            // I'm not sure why this is necessary, but it was included in the Firebase lab
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        finishButton.setOnClickListener{
            finishCheckin()
        }
    }

    private fun finishCheckin() {
        val commentText = commentET.text.toString()

        // check to see if the user left a comment
        if(!TextUtils.isEmpty(commentText)){
            // give the user 5 points for commenting
            pointsEarned += 5

            // get a unique ID for the comment
            val commentID = gsDatabase.push().key

            // check if the comment anonymously button is checked
            if(anonButton.isChecked){
                // create a comment object
                val comment = Comment(user, "Anonymous", commentText)
                // update the comment lists for the user and the green space
                gsComments[commentID!!] = comment
                userComments[commentID!!] = comment
                usersDatabase.child(user).child("uComments").setValue(userComments)
                gsDatabase.child(greenspaceID).child("gsComments").setValue(gsComments)

            } else {
                // create a comment object
                val comment = Comment(user, username, commentText)
                // update the comment lists for the user and the green space
                gsComments[commentID!!] = comment
                userComments[commentID!!] = comment
                usersDatabase.child(user).child("uComments").setValue(userComments)
                gsDatabase.child(greenspaceID).child("gsComments").setValue(gsComments)
            }

        }

        // update the average quality ranking for the greenspace
        val newQuality = ((gsAvgQual * gsNumRankings) + quality.ordinal + 1) / (gsNumRankings + 1)

        gsDatabase.child(greenspaceID).child("gsAvgQuality").setValue(newQuality)
        gsDatabase.child(greenspaceID).child("numRankings").setValue(gsNumRankings + 1)

        if(favoriteButton.isChecked){
            userFavorites[greenspaceID] = gsName
            usersDatabase.child(user).child("userFavorites").setValue( userFavorites)
        }

        // give the user 10 points for checking in
        pointsEarned += 10

        // update the user's points
        usersDatabase.child(user).child("userPoints").setValue(userPoints + pointsEarned)

        Toast.makeText(this, "You earned ${pointsEarned} points!", Toast.LENGTH_LONG).show()

        val enter = Intent(this@CheckinActivity, MapsActivity::class.java)
        startActivity(enter)

    }

}