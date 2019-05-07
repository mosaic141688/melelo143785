package com.melelo.melelo

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

class MyAdatpter(private val myDataset: ArrayList<Contact>, activity: WriteNFCActivity) :
    RecyclerView.Adapter<MyAdatpter.MyViewHolder>() {

    val parent:WriteNFCActivity = activity

    class MyViewHolder(val linearLayout: RelativeLayout) : RecyclerView.ViewHolder(linearLayout) {
        public var textView: TextView
        public var deleteButton: ImageButton

        init {
            textView = linearLayout.findViewById(R.id.contact_tv)
            deleteButton = linearLayout.findViewById(R.id.delete_btn)
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyAdatpter.MyViewHolder {
        Log.e("ONcreateView", "Here")
        // create a new view
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_ly, parent, false) as RelativeLayout
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(linearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        // holder.text.text = myDataset[position]
        Log.e("Oncreate View ", "here")
        holder.textView.text = myDataset.get(position).number
        holder.deleteButton.setOnClickListener {
            parent.onDelete(position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

}