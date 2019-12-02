package com.example.green_space_audits.mainactivity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_displaygreenspace.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var profileFragment: ProfileActivity
    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: String
    private lateinit var mMap: GoogleMap
    private lateinit var gsDatabase: DatabaseReference
    private var alreadyAdded = arrayListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        gsDatabase = FirebaseDatabase.getInstance().getReference("GreenSpaces")

    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        var x = 38.9897
        var y = -76.9378
        var circleOptions = CircleOptions()
            .center(LatLng(x, y))
            .radius(400.toDouble()).fillColor(Color.GREEN).clickable(true)
        mMap.addCircle(circleOptions)
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true




        var information = "Population: 4,137,400 " +
                "green: yes " +
                "man made "
        mMap.addMarker(MarkerOptions().position(LatLng(x,y)).title("College park here").snippet(information).alpha(0.0f))


        centerMapToUser()
        makeMarkers()


    }

    private fun centerMapToUser() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        locationProvider = locationManager.getBestProvider(Criteria(), true)
        mMap.isMyLocationEnabled = true

        val lastknownLocation = locationManager.getLastKnownLocation(locationProvider)
        val curx = lastknownLocation.latitude
        val cury = lastknownLocation.longitude
//        val currentLocation = LatLng(38.9897, -76.9378)


        // The actual one. Never delete this one
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(curx, cury), DEFAULT_ZOOM_LEVEL.toFloat()))



//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM_LEVEL.toFloat()))


    }

    private fun makeMarkers(){

        var fileReader: BufferedReader? = null
//        var arrayList: ArrayList<Places> = ArrayList()
        var line: String?

        try {


            var count = 0

            val stream = getResources().openRawResource(R.raw.data)
            fileReader = BufferedReader(InputStreamReader(stream, Charset.forName("UTF-8")))

            line = fileReader.readLine()
            line = fileReader.readLine()

            gsDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnapshot in dataSnapshot.children) {
                        val gsID = postSnapshot.key
                        // check to make sure the green space isnt already on the map
                        if(!alreadyAdded.contains(gsID)) {
                            alreadyAdded.add(gsID!!)
                            val lat = postSnapshot.getValue<GreenSpace>(GreenSpace::class.java)!!.gsLat.toDouble()
                            var long = postSnapshot.getValue<GreenSpace>(GreenSpace::class.java)!!.gsLong.toDouble()
                            val name = postSnapshot.getValue<GreenSpace>(GreenSpace::class.java)!!.gsName
                            val info = postSnapshot.getValue<GreenSpace>(GreenSpace::class.java)!!.gsType.displayStr
                            val location = LatLng(lat,long)

                            var circleOptions = CircleOptions()
                                .center(location)
                                .radius(400.toDouble()).fillColor(Color.GREEN).clickable(true)
                            mMap.addCircle(circleOptions)

                            mMap.addMarker(MarkerOptions().position(location).title(name).snippet(info).alpha(0.0f))
                        }
                    }
                }
                // I'm not sure why this is necessary, but it was included in the Firebase lab
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })


            while (line != null) {
                var tokens = line.split(",")
                var name = tokens[7]

                var x = tokens[3].toDouble()
                var y = tokens[4].toDouble()
                val locat = LatLng(x,y)
                var info = tokens[5]


                // might delete
                var circleOptions = CircleOptions()
                    .center(locat)
                    .radius(400.toDouble()).fillColor(Color.GREEN).clickable(true)
                mMap.addCircle(circleOptions)


                mMap.addMarker(MarkerOptions().position(LatLng(x,y)).title(name).snippet(info).alpha(0.0f))

//                mMap.setOnMarkerClickListener{
//                    marker ->
//                }

//                val place = Places(name,locat,info)
//                arrayList.add(place)




                if(count > 4){
                    break
                }
                count++
                line = fileReader.readLine()


            }


        }catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
        }




    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM_LEVEL = 12

    }



}