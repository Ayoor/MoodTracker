package com.basic.programming.mygridlayoutapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//import com.basic.programming.mygridlayoutapp.R
import com.basic.programming.mygridlayoutapp.model.mooddata
import com.example.mt.R

class historyadapter(var context: Context, var arrayList: ArrayList<mooddata>) :
    RecyclerView.Adapter<historyadapter.ItemHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val viewHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_moodhistorygridlayout, parent, false)
        return ItemHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val charItem: mooddata = arrayList[position]

        holder.icons.setImageResource(charItem.icons!!)
        holder.titles.text = "You were "+charItem.mood


    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var icons = itemView.findViewById<ImageView>(R.id.smiley)
        var titles = itemView.findViewById<TextView>(R.id.moodtext)

    }


}