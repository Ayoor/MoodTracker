//package com.basic.programming.mygridlayoutapp.adapters
//
//import android.app.Activity
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import androidx.recyclerview.widget.RecyclerView
//import com.basic.programming.mygridlayoutapp.model.CharItem
//import com.example.mt.Moodhistory
//import com.example.mt.R
//
//class AlphaAdapters(private val activity: Activity, private val arrayList: ArrayList<CharItem>) :
//    RecyclerView.Adapter<AlphaAdapters.ItemHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
//        val viewHolder = LayoutInflater.from(parent.context)
//            .inflate(R.layout.activity_moodhistorygridlayout, parent, false)
//        return ItemHolder(viewHolder)
//    }
//
//    override fun getItemCount(): Int {
//        return arrayList.size
//    }
//
//    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
//        val charItem: CharItem = arrayList[position]
//
//        holder.icons.setImageResource(charItem.icons!!)
//        holder.cardView.setOnClickListener {
//            val intent = Intent(activity, Moodhistory::class.java)
//            intent.putExtra("mood", charItem.alpha)
//            intent.putExtra("moodImg", charItem.icons!!)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            activity.startActivity(intent)
//        }
//    }
//
//    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val icons: ImageView = itemView.findViewById(R.id.smiley)
//        val cardView: View = itemView.findViewById(R.id.card_view)
//    }
//}
