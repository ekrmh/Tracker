package com.ekremh.tracker

import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by ekremh on 6.01.2018.
 */
data class User(var name: String = "",var image:Int=1,var latitude: Double = 0.0, var longitude: Double = 0.0, @ServerTimestamp var lastUpdated:Date = Date())

data class Room(var name:String = "", @ServerTimestamp var createdDay:Date = Date(), var users:Map<String,Boolean> = HashMap())