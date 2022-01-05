package com.prasoon.apodkotlin.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import kotlinx.android.synthetic.main.item_apod.view.*

//                                                                          2. Extend holder
//                      4. Populate objects
class ApodListAdapter(var apodModels: ArrayList<ApodModel>): RecyclerView.Adapter<ApodListAdapter.ApodViewHolder>() {

    // 3. Override methods
    // todo: view may not be val
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_apod, parent, false)
        return ApodViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApodViewHolder, position: Int) {
        holder.bind(apodModels[position])
    }

    override fun getItemCount() = apodModels.size

    // 1. Create Holder
    class ApodViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val itemImageView = view.item_apod_image
        private val itemTitle = view.item_title
        private val itemDate = view.item_date

        // ***Binding between view and data
        fun bind(apodModel: ApodModel) {
            if (apodModel.mediaType.equals("image")) {
                itemImageView.loadImage(apodModel.url)
            } else {
                // todo: add for videoview
                //itemImageView.loadVideo(apodModel.url)
            }

            itemTitle.text = apodModel.title
            itemDate.text = apodModel.date
        }
    }
}