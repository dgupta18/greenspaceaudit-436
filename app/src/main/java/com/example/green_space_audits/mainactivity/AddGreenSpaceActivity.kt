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
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var lat = 0.0
    private var long = 0.0

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

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lat = location.latitude
                long = location.longitude
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null)

        // use an addValueListener to get the current user's username and comments
        usersDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                username = dataSnapshot.child(user).getValue<User>(User::class.java)!!.userName
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

        // check to see if a name has been provided
        if(!TextUtils.isEmpty(name)) {

            // check to see if the acreage has been provided
            if(!TextUtils.isEmpty(acresString)) {
                // convert the acres from a string to a float
                val acres = acresString.toFloat()

                // check to see if the user left a comment
                if(!TextUtils.isEmpty(commentText)){
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

                // TODO I don't know how likely it is that the location can't be found. Do we need to worry about this?
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

                    Toast.makeText(this, "Green space added", Toast.LENGTH_LONG).show()

                    // TODO: which activity do we want to launch?
                    val enter =
                        Intent(this@AddGreenSpaceActivity, DisplayGreenSpaceActivity::class.java)
                    enter.putExtra("gsID", greenSpaceID)
                    startActivity(enter)
                }
            } else {
                Toast.makeText(this, "Please enter the acreage", Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show()
        }

    }

}