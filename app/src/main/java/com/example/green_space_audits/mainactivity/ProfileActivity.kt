package com.example.green_space_audits.mainactivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.measurement.module.Analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var nameHolder: TextView
    private lateinit var emailHolder: TextView
    private lateinit var pointsHolder: TextView
    private var badgesHolder: LinearLayout? = null
    private lateinit var favoritesHolder: LinearLayout
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var points: String
    private lateinit var badges: MutableList<String>
    private lateinit var favorites: MutableMap<String,String>
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        val id = mAuth!!.currentUser!!.uid
        val isAdmin = mAuth!!.currentUser!!.displayName == "admin"
        pref = getSharedPreferences("adminKey",Context.MODE_PRIVATE)
//        val isAdmin: Boolean = pref.getString(id, "false").toBoolean()
//        Log.i("ADMIN: ", pref.all.toString())
        if (isAdmin) {
            Log.i("ADMIN: ", "setting view as profile admin")
            setContentView(R.layout.activity_profile_admin)
            var analytics = findViewById<Button>(R.id.save_data)
            analytics!!.setOnClickListener {
                val intent = Intent(this, ChartActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        } else {
            setContentView(R.layout.activity_profile)
        }


        val context = this

        nameHolder = findViewById<View>(R.id.profile_name) as TextView
        emailHolder = findViewById<View>(R.id.profile_email) as TextView
        pointsHolder = findViewById<View>(R.id.profile_points) as TextView
        if (!isAdmin) {
            badgesHolder = findViewById(R.id.badges_container) as LinearLayout
        }
        favoritesHolder = findViewById(R.id.favorites_container) as LinearLayout

        // get database info
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")



        // Attach a listener to read the data
        mDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currUser = dataSnapshot.child(id).getValue(User::class.java)
                Log.i("USER: ", currUser.toString())

                // get user values that we want to display
                name = currUser!!.userName
                email = currUser!!.userEmail
                points = currUser!!.userPoints.toString()
                badges = currUser!!.userBadges
                favorites = currUser!!.userFavorites

                // set values in textviews
                nameHolder.text = name
                emailHolder.text = email
                pointsHolder.text = points

                // BADGE STUFF ONLY IF NOT ADMIN
                if (!isAdmin) {
                    // add a badge for number of points earned, if applicable
                    if(currUser!!.userPoints > 50){
                        val num = (currUser!!.userPoints / 50) * 50 // gets the highest multiple of 50 less than their current number of points
                        badges.add("Earned ${num} points")
                    }

                    Log.i("BADGES: ", badges.toString())
                    if (badges.isEmpty() || badges.toString().equals("[]")) {
                        val emptyBadges = TextView(context)
                        emptyBadges.setTextColor(ContextCompat.getColor(context, R.color.colorGreyAccent))
                        emptyBadges.text = "No badges yet!"
                        emptyBadges.textSize = 24f
                        emptyBadges.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                        badgesHolder!!.addView(emptyBadges)
                    }
                    for (entry in badges){

                        val scale = context.resources.displayMetrics.density

                        // create relative layout for new badge
                        val newBadge = RelativeLayout(context)
                        newBadge.layoutParams = RelativeLayout.LayoutParams((100 * scale + 0.5f).toInt(), (120 * scale + 0.5f).toInt())

                        // add badge icon
                        val icon = ImageView(context)
                        icon.setImageResource(R.drawable.badge_icon)
                        icon.id = R.id.img_id
                        icon.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,(80 * scale + 0.5f).toInt())
                        newBadge.addView(icon)
                        Log.i("ADDED ICON: ", icon.toString())

                        // add title
                        val title = TextView(context)
                        title.text = entry
                        title.textSize = 14f
                        title.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                        title.setTextColor(ContextCompat.getColor(context, R.color.colorGreyAccent))
                        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
                        layoutParams.addRule(RelativeLayout.BELOW, icon.id)
                        layoutParams.setMargins(0,(3 * scale + 0.5f).toInt(),0,0)
                        title.layoutParams = layoutParams
                        newBadge.addView(title)
                        Log.i("ADDED TITLE: ", title.toString())

                        // add new badge view into the linear layout
                        badgesHolder!!.addView(newBadge)
                        Log.i("ADDED BADGE: ", badgesHolder.toString())
                    }
                }

                Log.i("FAVORITES: ", favorites.toString())
                if (favorites.isEmpty() || favorites.toString().equals("[]")) {
                    val emptyFavorites = TextView(context)
                    emptyFavorites.setTextColor(ContextCompat.getColor(context, R.color.colorGreyAccent))
                    emptyFavorites.text = "No favorites yet!"
                    emptyFavorites.textSize = 24f
                    emptyFavorites.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                    favoritesHolder.addView(emptyFavorites)
                }
                for ((favId,favName) in favorites) {
                    val scale = context.resources.displayMetrics.density

                    // create relative layout for new favorite
                    val newFav = RelativeLayout(context)
                    newFav.layoutParams = RelativeLayout.LayoutParams((100 * scale + 0.5f).toInt(), (120 * scale + 0.5f).toInt())
                    newFav.setOnClickListener{
                        val intent = Intent(this@ProfileActivity, DisplayGreenSpaceActivity::class.java)
                        intent.putExtra("gsID", favId)
                        startActivity(intent)
                    }

                    // add favorites icon
                    val icon = ImageView(context)
                    icon.setImageResource(R.drawable.favorite_icon)
                    icon.scaleType = ImageView.ScaleType.FIT_CENTER
                    icon.setPadding(10,10,10,10)
                    icon.id = R.id.img_id
                    icon.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,(80 * scale + 0.5f).toInt())
                    newFav.addView(icon)
                    Log.i("ADDED ICON: ", icon.toString())

                    // add title
                    val title = TextView(context)
                    title.text = favName
                    title.textSize = 14f
                    title.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    title.setTextColor(ContextCompat.getColor(context, R.color.colorGreyAccent))
                    val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
                    layoutParams.addRule(RelativeLayout.BELOW, icon.id)
                    layoutParams.setMargins(0,(3 * scale + 0.5f).toInt(),0,0)
                    title.layoutParams = layoutParams
                    newFav.addView(title)
                    Log.i("ADDED TITLE: ", title.toString())

                    // add new badge view into the linear layout
                    favoritesHolder.addView(newFav)
                    Log.i("ADDED FAVORITE: ", favoritesHolder.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })
    }

    fun logOut(view: View) {
        val editor: SharedPreferences.Editor = pref.edit()
        editor.clear().commit()
        val intent = Intent(this@ProfileActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

}