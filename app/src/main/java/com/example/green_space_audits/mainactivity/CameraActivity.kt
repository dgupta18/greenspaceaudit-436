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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


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

            gsDatabase = FirebaseDatabase.getInstance().getReference("GreenSpaces")
            greenspaceID = intent.getStringExtra("gsID")
            val filename = greenspaceID

            val myFireBaseRef = FirebaseStorage.getInstance().reference.child("/$filename")

            Log.i("i I am ","where")

            val x = myFireBaseRef.putFile(photoURI!!)




        }


    }




    private fun DispatchTakePictureIntent() { //Straight from android dev guide
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = CreateImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.d("greenSpace", "Error creating image file!")
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,true)
                startActivityForResult(takePictureIntent,1)
            }
        }


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val extras = data.extras
                val imageBitmap = extras!!.get("data") as Bitmap?
                mImageView!!.setImageBitmap(imageBitmap)
            }

            var bitmap: Bitmap? = null
            val file = File(mCurrentPhotoPath)
            try {
                photoURI = data!!.data
                bitmap = MediaStore.Images.Media.getBitmap(
                    applicationContext.contentResolver,
                    Uri.fromFile(file)
                )
            } catch (e: IOException) {
                Log.d("LeafSpace", "Photo wasn't found!")
            }

            if (bitmap != null) {
                mImageView!!.setImageBitmap(bitmap)


                val aspectRatio = bitmap.width / bitmap.height.toFloat()
                val width = 480
                val height = Math.round(width / aspectRatio)

                submitBitmap = Bitmap.createScaledBitmap(
                    bitmap, width, height, false
                )


            }



        }
    }



    private fun CreateImageFile() : File {
        // Create an image file name
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "LeafSpace_" + timeStamp + "_";
        var storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        var image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath()
        return image
    }























}
