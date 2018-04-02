package com.ekremh.tracker

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.item_room.view.*

/**
 * Created by ekremh on 6.01.2018.
 */
class RoomAdapter(options: FirestoreRecyclerOptions<Room>) : FirestoreRecyclerAdapter<Room, RoomAdapter.RoomViewHolder>(options) {
    override fun onBindViewHolder(holder: RoomViewHolder, position: Int, model: Room) {

        holder.itemView.room_name.text = model.name
        holder.itemView.created_date.text = "Created time : ${model.createdDay}"
        holder.itemView.member_count.text = model.users.size.toString()
        holder.itemView.room_name.setOnClickListener({view ->
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra(MainActivity.ROOM_NUMBER,snapshots.getSnapshot(position).id)
            holder.itemView.context.startActivity(intent)
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RoomViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val cellForRow = inflater.inflate(R.layout.item_room,parent,false)
        return RoomViewHolder(cellForRow)
    }
    class RoomViewHolder(v: View) : RecyclerView.ViewHolder(v)

}




