package com.fpi.biometricsystem.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fpi.biometricsystem.data.HomeItem
import com.fpi.biometricsystem.R
import com.fpi.biometricsystem.databinding.HomeMenuListItemBinding


class HomeMenuListAdapter(private val menuItems: List<HomeItem>, private val listener: OnHomeItemClickListener ) :
    RecyclerView.Adapter<HomeMenuListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = HomeMenuListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val homeItem = menuItems[position]
        holder.bind(homeItem)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    inner class MyViewHolder(private val binding: HomeMenuListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var homeItem: HomeItem

        fun bind(menuItem: HomeItem) {
            this.homeItem = menuItem
            val src = when(menuItem.resId){
                0-> R.drawable.student_registration
                1 -> R.drawable.staff_registration
                2 -> R.drawable.student_attendance
                3 -> R.drawable.staff_attendance
                4 -> R.drawable.examinations
                5 -> R.drawable.settings
                else -> R.drawable.settings
            }
            binding.apply {
                title.text = menuItem.title
                cardBg.setImageResource(src)
            }
            itemView.setOnClickListener { listener.onItemClick(homeItem) }
        }


    }
}

interface OnHomeItemClickListener {
    fun onItemClick(item: HomeItem)
}

