  package com.ekremh.tracker

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import android.graphics.Bitmap
import android.provider.MediaStore.Images.Media.getBitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.res.TypedArrayUtils.getResourceId
import android.content.res.TypedArray
import android.support.v4.content.res.ResourcesCompat


  class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    val TAG = MapsActivity::class.java.simpleName
    private lateinit var mMap: GoogleMap
    lateinit var room_number: String
    var mapMarker : MutableMap<String,Marker>  = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        room_number = intent.extras.getString(MainActivity.ROOM_NUMBER)

        Log.d(TAG,"Your room name : $room_number")
        FirebaseFirestore.getInstance().collection("Rooms")
                .document(room_number)
                .get().addOnSuccessListener { task ->
            val room = task.toObject(Room::class.java)
            for(item in room.users.keys){
               Log.d(TAG,"Your UID : $item")
                FirebaseFirestore.getInstance().collection("Users").document(item).addSnapshotListener({
                    documentSnapshot, firebaseFirestoreException ->
                    if(documentSnapshot.exists()){
                        Log.d(TAG,"Changed excepted")
                        val user = documentSnapshot.toObject(User::class.java)
                        Log.d(TAG,"Langitude : ${user.longitude} , Latitude : ${user.latitude}")

                        val position = LatLng(user.latitude,user.longitude)
                        if(mapMarker.containsKey(item)){
                            mapMarker[item]?.position = position
                        }
                        else{
                            val images = resources.obtainTypedArray(R.array.avatar_images)

                            val bitmapdraw = ResourcesCompat.getDrawable(resources,images.getResourceId(user.image, R.drawable.avatar1),null) as BitmapDrawable
                            val b = bitmapdraw.bitmap
                            val smallMarker = Bitmap.createScaledBitmap(b, 70, 70, false)
                            val marker = MarkerOptions().position(position).title(user.name).icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            images.recycle()

                            mapMarker.put(item,mMap.addMarker(marker))
                        }
                    }
                })

            }


        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.maps_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.exit_menu -> {
                MaterialDialog.Builder(this)
                        .title("Leave room")
                        .content("Are you sure leave this room?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive({dialog, which ->
                            FirebaseFirestore.getInstance().collection("Rooms").document(room_number)
                                    .update(mapOf("users.${FirebaseAuth.getInstance().currentUser?.uid}" to false)).addOnSuccessListener { void ->
                                finish()
                            }

                        })
                        .show()
            }
        }
        return true
    }
}
