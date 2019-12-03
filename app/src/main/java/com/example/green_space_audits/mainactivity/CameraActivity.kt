package com.example.green_space_audits.mainactivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log


class CameraActivity : AppCompatActivity() {

    private var submitButton: Button? = null

    private var diameterEditText: EditText? = null

    private var mImageView: ImageView? = null
    private var mStorageRef: StorageReference? = null


    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_PERMISSIONS = 4
    val PERMISSION_CODE = 1001


    private var submitBitmap: Bitmap? = null
    var mCurrentPhotoPath: String? = null


    private var photoURI: Uri? = null


    private lateinit var greenspaceID: String
    private lateinit var gsDatabase: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_camera)


        //getSupportActionBar().setTitle("Add New Tree");
        submitButton = findViewById(R.id.SubmitButton)

        mImageView = findViewById<ImageView>(R.id.TreeImageView)
        mStorageRef = FirebaseStorage.getInstance().getReference()


        mImageView!!.setOnClickListener{

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                ) {

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

        submitButton!!.setOnClickListener{

//            gsDatabase = FirebaseDatabase.getInstance().getReference("GreenSpaces")
//            greenspaceID = intent.getStringExtra("gsID")

            val filename = "1"

            val myFireBaseRef = FirebaseStorage.getInstance().reference.child("/images/$filename")


            val baos = ByteArrayOutputStream()
            submitBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            var  data = baos.toByteArray()

            myFireBaseRef.putBytes(data)


        }


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
