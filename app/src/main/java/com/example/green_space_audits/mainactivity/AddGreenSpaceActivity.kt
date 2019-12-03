package com.example.green_space_audits.mainactivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.location.LocationListener
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.firebase.database.*

class AddGreenSpaceActivity : AppCompatActivity() {

    private lateinit var nameET: EditText
    private lateinit var qualityTV: TextView
    private lateinit var qualityRG: RadioGroup
    private lateinit var recreationTV: TextView
    private lateinit var recreationRG: RadioGroup
    private lateinit var commentET: EditText
    private lateinit var anonButton: CheckBox
    private lateinit var saveButton: Button
    private lateinit var user: String
    private lateinit var quietRG: RadioGroup
    private lateinit var hazardsRG: RadioGroup
    private lateinit var acresET: EditText
    private lateinit var gsDatabase: DatabaseReference
    private lateinit var usersDatabase: DatabaseReference
    private lateinit var username: String
    private lateinit var userComments: MutableMap<String, Comment>
    private lateinit var userBadges: MutableList<String>
    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: String
    private var lat = 0.0
    private var long = 0.0
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

    private val recreationType: Recreation
        get() {
            when (recreationRG.checkedRadioButtonId) {
                R.id.peopleRec -> {
                    return Recreation.PEOPLEPOWERED
                }
                else -> {
                    return Recreation.NATUREBASED
                }
            }
        }

    private val isQuiet: Boolean
        get() {
            when (quietRG.checkedRadioButtonId) {
                R.id.yesQuiet -> {
                    return true
                }
                else -> {
                    return false
                }
            }
        }

    private val isNearHazards: Boolean
        get() {
            when (hazardsRG.checkedRadioButtonId) {
                R.id.yesHazards -> {
                    return true
                }
                else -> {
                    return false
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super .onCreate(savedInstanceState)
        setContentView(R.layout.activity_addgreenspace)

        nameET = findViewById<EditText>(R.id.nameET)
        qualityRG = findViewById<RadioGroup>(R.id.qualityGroup)
        qualityTV = findViewById<TextView>(R.id.quality)
        recreationTV = findViewById<TextView>(R.id.recreation)
        recreationRG = findViewById<RadioGroup>(R.id.recreationGroup)
        commentET = findViewById<EditText>(R.id.comment)
        anonButton = findViewById<CheckBox>(R.id.anonComment)
        saveButton = findViewById<Button>(R.id.save)
        hazardsRG = findViewById<RadioGroup>(R.id.hazardsGroup)
        quietRG = findViewById<RadioGroup>(R.id.quietGroup)
        acresET = findViewById<EditText>(R.id.acresET)


        gsDatabase = FirebaseDatabase.getInstance().getReference("GreenSpaces")
        usersDatabase = FirebaseDatabase.getInstance().getReference("Users")

        user = FirebaseAuth.getInstance().currentUser!!.uid

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = locationManager.getBestProvider(Criteria(), true)

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        val lastknownLocation = locationManager.getLastKnownLocation(locationProvider)
        lat = lastknownLocation.latitude
        long = lastknownLocation.longitude


        // use an addValueListener to get the current user's information
        usersDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                username = dataSnapshot.child(user).getValue<User>(User::class.java)!!.userName
                userPoints = dataSnapshot.child(user).getValue<User>(User::class.java)!!.userPoints
                userBadges = dataSnapshot.child(user).getValue<User>(User::class.java)!!.userBadges
                if(dataSnapshot.child(user).child("uComments").value != null){
                    userComments = dataSnapshot.child(user).child("uComments").value as MutableMap<String, Comment>
                } else {
                    userComments = mutableMapOf<String, Comment>()
                }
            }
            // I'm not sure why this is necessary, but it was included in the Firebase lab
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        saveButton.setOnClickListener{
            save()
        }
    }

    private fun save() {
        val name = nameET.text.toString()
        val commentText = commentET.text.toString()
        val acresString = acresET.text.toString()
        var commentsList = mutableMapOf<String, Comment>()
        var badgeEarned = false

        // check to see if a name has been provided
        if(!TextUtils.isEmpty(name)) {

            // check to see if the acreage has been provided
            if(!TextUtils.isEmpty(acresString)) {
                // convert the acres from a string to a float
                val acres = acresString.toFloat()

                // check to see if the user left a comment
                if(!TextUtils.isEmpty(commentText)){
                    // give the user 5 points for commenting
                    pointsEarned += 5

                    // if this is the user's first time commenting, give them the comment badge
                    if(!userBadges.contains(Badge.COMMENT.displayStr)){
                        userBadges.add(Badge.COMMENT.displayStr)
                        usersDatabase.child(user).child("userBadges").setValue(userBadges)
                        badgeEarned = true
                    }

                    // get a unique ID for the comment
                    val commentID = gsDatabase.push().key

                    // check if the comment anonymously button is checked,
                    // create a comment object, and add it to the comments list
                    if(anonButton.isChecked){
                        val comment = Comment(user, "Anonymous", commentText)
                        commentsList[commentID!!] = comment
                        userComments[commentID!!] = comment
                        usersDatabase.child(user).child("uComments").setValue(userComments)

                    } else {
                        val comment = Comment(user, username, commentText)
                        commentsList[commentID!!] = comment
                        userComments[commentID!!] = comment
                        usersDatabase.child(user).child("uComments").setValue(userComments)
                    }


                }

                if(lat == 0.0 && long == 0.0){
                    Toast.makeText(this, "Unable to find location", Toast.LENGTH_LONG).show()
                } else {
                    // create a green space object
                    val newGS = GreenSpace(
                        name,
                        user,
                        lat.toFloat(),
                        long.toFloat(),
                        acres,
                        (quality.ordinal + 1).toFloat(),
                        1,
                        recreationType,
                        commentsList,
                        isQuiet,
                        isNearHazards
                    )

                    Log.i("NEW GREEN SPACE", "GS: $newGS")
                    Log.i("NEW GREEN SPACE", "user: " + usersDatabase.child(user))

                    //getting a unique id using push().getKey() method
                    //it will create a unique id and we will use it as the primary key for our green space
                    val greenSpaceID = gsDatabase.push().key

                    // add the new green space to the database
                    gsDatabase.child(greenSpaceID!!).setValue(newGS)

                    // give the user 10 points for checking in
                    pointsEarned += 10

                    // update the user's points
                    usersDatabase.child(user).child("userPoints").setValue(userPoints + pointsEarned)

                    // if this is the user's first time adding a green space, give them the add badge
                    if(!userBadges.contains(Badge.ADD.displayStr)){
                        userBadges.add(Badge.ADD.displayStr)
                        usersDatabase.child(user).child("userBadges").setValue(userBadges)
                        badgeEarned = true
                    }


                    // display a toast telling the user the addition was successful and how many points they earned
                    Toast.makeText(this, "Added! You earned ${pointsEarned} points!", Toast.LENGTH_LONG).show()

                    // display a toast if the user earned a badge
                    if(badgeEarned){
                        Toast.makeText(this, "You earned a new badge!", Toast.LENGTH_LONG).show()
                    }

                    val enter =
                        Intent(this@AddGreenSpaceActivity, DisplayGreenSpaceActivity::class.java)
                    enter.putExtra("gsID", greenSpaceID)
                    startActivity(enter)
                    overridePendingTransition(0, 0)
                }
            } else {
                Toast.makeText(this, "Please enter the acreage", Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show()
        }

    }

}