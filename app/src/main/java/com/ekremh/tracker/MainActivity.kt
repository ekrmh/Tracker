package com.ekremh.tracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.ekremh.tracker.R.mipmap.ic_launcher
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_maps.*
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(),LocationListener {

    companion object {
        val ROOM_NUMBER = "ROOM_NUMBER"
    }

    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    var my_rooms = db
            .collection("Rooms")
            .whereEqualTo("users.${firebaseAuth.uid.toString()}",true)

    val options:FirestoreRecyclerOptions<Room> =  FirestoreRecyclerOptions.Builder<Room>().setQuery(my_rooms,Room::class.java).build()
    val TAG: String = MainActivity::class.java.simpleName
    val CODE = 90

    val adapter:RoomAdapter = RoomAdapter(options)
    var locationManager:LocationManager?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        user_name.setOnClickListener({view ->
            MaterialDialog.Builder(this)
                .title("Change your name")
                .content("Enter your name")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("", "", {dialog, input ->
                    if(input.isNotEmpty())
                        db.collection("Users").document(firebaseAuth.uid.toString()).update(mapOf("name" to input.toString()))
                }).show()

        })
        img_photo.setOnClickListener({view ->

            val images :TypedArray = resources.obtainTypedArray(R.array.avatar_images)
            val random = Random().nextInt(8)+1
            try {
                img_photo.setImageResource(images.getResourceId(random,R.drawable.avatar1))
                images.recycle()
                db.collection("Users").document(firebaseAuth.uid.toString()).update(mapOf("image" to random))
            }catch (e:Exception){

            }



        })
        listenUser()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        recyler_room.layoutManager = LinearLayoutManager(this)
        recyler_room.adapter = adapter
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),CODE)
                return
            }
        }
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000,
                10f,
                this)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),CODE)
                }

            }
            else {
                Toast.makeText(getApplicationContext(),  "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser == null)
            register()

        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
    private fun createRoom(room_name:String) {
        db.collection("Rooms").whereEqualTo("name",room_name).get().addOnSuccessListener { task ->
            if(task.isEmpty )
                db.collection("Rooms").add(Room(room_name, Date(), hashMapOf(firebaseAuth.uid.toString() to true))).addOnSuccessListener {
                    Toast.makeText(this, "Your room number : $room_name", Toast.LENGTH_SHORT).show()
                }
            else
                Toast.makeText(this, "Your room number already use", Toast.LENGTH_SHORT).show();

        }

    }
    private fun joinRoom(room_name:String){
        db.collection("Rooms").whereEqualTo("name",room_name).get().addOnSuccessListener { task ->
            if(!task.isEmpty) {
               for(document in task.documents){
                   val room = document.toObject(Room::class.java)
                   if(room.users.keys.contains(firebaseAuth.uid.toString()))
                       Toast.makeText(this, "Your room number already joined", Toast.LENGTH_SHORT).show();
                   else
                       db.collection("Rooms").document(document.reference.id).update(mapOf("users" to mapOf(firebaseAuth.uid.toString() to true).mergeReduce(room.users)))
               }
            }

        }

    }
    private fun register() {
        firebaseAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = task.result.user.uid
                val images :TypedArray = resources.obtainTypedArray(R.array.avatar_images)
                val random = Random().nextInt(8)+1
                img_photo.setImageResource(images.getResourceId(random,R.drawable.avatar1))
                images.recycle()
                db.collection("Users").document(uid).set(User("user_" + Random().nextInt(100) + 10,random))
                listenUser()
            }


        }
    }

    private fun listenUser() {
        db.collection("Users").document(firebaseAuth.uid.toString()).addSnapshotListener { task, e ->
            if (e != null)
                Log.d(TAG, "Listen failed.")


            if (task != null && task.exists()) {
                val model: User = task.toObject(User::class.java)
                user_name.text = "Username : ${model.name}"
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                dateFormat.timeZone = TimeZone.getTimeZone("GMT")

                last_track.text = "Last Updated : ${dateFormat.format(model.lastUpdated)}"
                val image_number = model.image
                val images :TypedArray = resources.obtainTypedArray(R.array.avatar_images)
                img_photo.setImageResource(images.getResourceId(image_number,R.drawable.avatar1))
                img_photo.setImageResource(images.getResourceId(image_number,R.drawable.avatar1))
                images.recycle()




            } else
                Log.d(TAG, "Current data: null");


        }

    }

    fun <K, V> Map<K, V>.mergeReduce(other: Map<K, V>, reduce: (V, V) -> V = { _, b -> b }): Map<K, V> =
            this.toMutableMap().apply { other.forEach { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                merge(it.key, it.value, reduce)
            }
            } }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.create_room -> {Toast.makeText(applicationContext, "Create a room", Toast.LENGTH_SHORT).show()
                MaterialDialog.Builder(this)
                        .title("Create a room")
                        .content("Please enter a room name")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("","",{dialog, input ->
                            Toast.makeText(applicationContext,input,Toast.LENGTH_SHORT).show()
                            createRoom(input.toString())
                        })
                        .show()
            }
            R.id.join_room -> {Toast.makeText(applicationContext, "Join a room", Toast.LENGTH_SHORT).show()
                MaterialDialog.Builder(this)
                        .title("Join a room")
                        .content("Please enter a room name")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("","",{dialog, input ->
                            Toast.makeText(applicationContext,input,Toast.LENGTH_SHORT).show()
                            joinRoom(input.toString())
                        })
                        .show()

            }
        }
        return true
    }
    override fun onLocationChanged(location: Location?) {
        val latitude =  location?.latitude
        val longitude = location?.longitude

        updateLocation(latitude,longitude)
        Log.d(TAG,"Latitude : $latitude , Longitude : $longitude")

    }

    private fun updateLocation(latitude: Double?, longitude: Double?) {
        db.collection("Users").document(firebaseAuth.uid.toString()).update(mapOf("latitude" to latitude,"longitude" to longitude,"lastUpdated" to Date()))
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }
}
