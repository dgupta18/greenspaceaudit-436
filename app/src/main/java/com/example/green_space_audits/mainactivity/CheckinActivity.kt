package com.example.green_space_audits.mainactivity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils

import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.util.*


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
    private lateinit var userBadges: MutableList<String>
    private lateinit var favoriteButton: CheckBox
    private var gsAvgQual = 0.toFloat()
    private var gsNumRankings = 0
    private var userPoints = 0

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PERMISSIONS = 4
    private val PERMISSION_CODE = 1001

    private var mImageView: ImageView? = null
    private var mStorageRef: StorageReference? = null
    private var submitBitmap: Bitmap? = null

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
        mImageView = findViewById<ImageView>(R.id.TreeImageView)
        mStorageRef = FirebaseStorage.getInstance().getReference()

        greenspaceID = intent.getStringExtra("gsID")

        gsDatabase = FirebaseDatabase.getInstance().getReference("GreenSpaces")
        usersDatabase = FirebaseDatabase.getInstance().getReference("Users")

        user = FirebaseAuth.getInstance().currentUser!!.uid

        // use an addValueListener to get the green space's information
        gsDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                gsName = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsName
                gsComments = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsComments
                gsAvgQual = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.gsAvgQuality
                gsNumRankings = dataSnapshot.child(greenspaceID).getValue<GreenSpace>(GreenSpace::class.java)!!.numRankings

                // set the name TextView's text based on the name of the green space
                nameTV.text = "Check in to:\n" + gsName
            }
            // I'm not sure why this is necessary, but it was included in the Firebase lab
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        // use an addValueListener to get the current user's information
        usersDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                username = dataSnapshot.child(user).getValue<User>(User::class.java)!!.userName
                userBadges = dataSnapshot.child(user).getValue<User>(User::class.java)!!.userBadges
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

                // check the favorites button if this green space is already in the user's favorites list
                if(userFavorites.contains(greenspaceID)){
                    favoriteButton.setChecked(true)
                } else {
                    favoriteButton.setChecked(false)
                }
            }
            // I'm not sure why this is necessary, but it was included in the Firebase lab
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        // request permissions to take pictures if the camera icon is clicked
        mImageView!!.setOnClickListener{

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                    val permission = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    requestPermissions(permission, PERMISSION_CODE)

                } else {
                    //permission was already granted
                    DispatchTakePictureIntent()
                }

            }

        }

        finishButton.setOnClickListener{
            finishCheckin()
        }
    }

    private fun finishCheckin() {
        val commentText = commentET.text.toString()
        var badgeEarned = false
        var pointsEarned = 0

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

        // update the average quality ranking and the number of rankings for the green space
        val newQuality = ((gsAvgQual * gsNumRankings) + quality.ordinal + 1) / (gsNumRankings + 1)
        gsDatabase.child(greenspaceID).child("gsAvgQuality").setValue(newQuality)
        gsDatabase.child(greenspaceID).child("numRankings").setValue(gsNumRankings + 1)

        // if the favorite button is checked and the green space isn't already one of the users favorites,
        // add it to their favorites list
        if(favoriteButton.isChecked && !userFavorites.contains(greenspaceID)){
            userFavorites[greenspaceID] = gsName
            usersDatabase.child(user).child("userFavorites").setValue( userFavorites)

            // if this is the user's first time favoriting a green space, give them the favorite badge
            if(!userBadges.contains(Badge.FAVORITE.displayStr)){
                userBadges.add(Badge.FAVORITE.displayStr)
                usersDatabase.child(user).child("userBadges").setValue(userBadges)
                badgeEarned = true
            }
        // otherwise, if the favorites button is unchecked but the green space is in the users favorites list, remove it
        } else if (!favoriteButton.isChecked && userFavorites.contains(greenspaceID)) {
            userFavorites.remove(greenspaceID)
            usersDatabase.child(user).child("userFavorites").setValue( userFavorites)
        }

        // give the user 10 points for checking in
        pointsEarned += 10

        // update the user's points
        usersDatabase.child(user).child("userPoints").setValue(userPoints + pointsEarned)

        // if this is the user's first time checking in to a green space, give them the checkin badge
        if(!userBadges.contains(Badge.CHECKIN.displayStr)){
            userBadges.add(Badge.CHECKIN.displayStr)
            usersDatabase.child(user).child("userBadges").setValue(userBadges)
            badgeEarned = true
        }

        // if a photo was uploaded, send it to firebase storage
        if(submitBitmap != null) {
            val filename = UUID.randomUUID().toString()

            val myFireBaseRef = FirebaseStorage.getInstance().reference.child("/$greenspaceID/$filename")

            val baos = ByteArrayOutputStream()
            submitBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            var data = baos.toByteArray()
            myFireBaseRef.putBytes(data)

            // if this is the user's first time uploading a photo, give them the photo badge
            if(!userBadges.contains(Badge.PHOTO.displayStr)){
                userBadges.add(Badge.PHOTO.displayStr)
                usersDatabase.child(user).child("userBadges").setValue(userBadges)
                badgeEarned = true
            }
        }

        // display a toast telling the user how many points they earned
        Toast.makeText(this, "You earned ${pointsEarned} points!", Toast.LENGTH_LONG).show()

        // display a toast if the user earned a badge
        if(badgeEarned){
            Toast.makeText(this, "You earned a new badge!", Toast.LENGTH_LONG).show()
        }

        // return the user to the maps activity
        val enter = Intent(this@CheckinActivity, MapsActivity::class.java)
        startActivity(enter)

    }

    private fun DispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,true)
            startActivityForResult(takePictureIntent,1)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val extras = data.extras
                val imageBitmap = extras!!.get("data") as Bitmap?

                submitBitmap = imageBitmap!!.copy(imageBitmap.config, imageBitmap.isMutable)

                mImageView!!.setImageBitmap(imageBitmap)
            }


        }
    }

}