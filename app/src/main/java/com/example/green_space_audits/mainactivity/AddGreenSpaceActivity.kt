package com.example.green_space_audits.mainactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.google.firebase.database.*

class AddGreenSpaceActivity : AppCompatActivity() {

    private lateinit var nameET: EditText
    private lateinit var qualityTV: TextView
    private lateinit var qualityRG: RadioGroup
    private lateinit var recreationTV: TextView
    private lateinit var recreationRG: RadioGroup
    private lateinit var commentET: EditText
    private lateinit var anonButton: RadioButton
    private lateinit var saveButton: Button
    private lateinit var user: String
    private lateinit var quietRG: RadioGroup
    private lateinit var hazardsRG: RadioGroup
    private lateinit var acresET: EditText
    private lateinit var gsDatabase: DatabaseReference
    private lateinit var usersDatabase: DatabaseReference
    private lateinit var username: String
    private lateinit var userComments: MutableMap<String, Comment>

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
        anonButton = findViewById<RadioButton>(R.id.anonComment)
        saveButton = findViewById<Button>(R.id.save)
        hazardsRG = findViewById<RadioGroup>(R.id.hazardsGroup)
        quietRG = findViewById<RadioGroup>(R.id.quietGroup)
        acresET = findViewById<EditText>(R.id.acresET)


        gsDatabase = FirebaseDatabase.getInstance().getReference("GreenSpaces")
        usersDatabase = FirebaseDatabase.getInstance().getReference("Users")

        user = FirebaseAuth.getInstance().currentUser!!.uid

        // use an addValueListener to get the current user's username
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

//                    val usersComments = usersDatabase.child(user).child("uComments") as MutableMap<String, Comment>

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


                // create a green space object
                // TODO: figure out how to get the lat long values
                // use the users location ^
                val newGS = GreenSpace(name, user, 0.toFloat(), 0.toFloat(), acres, (quality.ordinal + 1).toFloat(), 1, recreationType, commentsList, isQuiet, isNearHazards)

                // TODO: add newGS to the database
                // I don't want to actually add anything until we decide what structure we want
                Log.d("NEW GREEN SPACE", "GS: $newGS")
                Log.d("NEW GREEN SPACE", "user: " + usersDatabase.child(user))

                //getting a unique id using push().getKey() method
                //it will create a unique id and we will use it as the primary key for our green space
                val greenSpaceID = gsDatabase.push().key

                // add the new green space to the database
                gsDatabase.child(greenSpaceID!!).setValue(newGS)

                Toast.makeText(this, "Green space added", Toast.LENGTH_LONG).show()

                // TODO: which activity do we want to launch?
                val enter = Intent(this@AddGreenSpaceActivity, DisplayGreenSpaceActivity::class.java)
//                startActivity(enter)
//                overridePendingTransition(0, 0)
            } else {
                Toast.makeText(this, "Please enter the acreage", Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show()
        }

    }

}