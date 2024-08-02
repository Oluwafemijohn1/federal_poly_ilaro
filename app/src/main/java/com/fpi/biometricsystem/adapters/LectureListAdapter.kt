package com.fpi.biometricsystem.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fpi.biometricsystem.data.Lecture
import com.fpi.biometricsystem.databinding.EventListItemBinding


class LectureListAdapter(private val lectures: List<Lecture>, private val listener: OnItemClickListenerLecture ) :
    RecyclerView.Adapter<LectureListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = EventListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val lecture = lectures[position]
        holder.bind(lecture)
    }

    override fun getItemCount(): Int {
        return lectures.size
    }

    inner class MyViewHolder(private val binding: EventListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var lectureInfo: Lecture

        fun bind(lectureInfo: Lecture) {
            this.lectureInfo = lectureInfo
            binding.apply {
//                eventId.text = lectureInfo.eventNumber
                eventName.text = lectureInfo.topic
                eventLocation.text = "Date: ${lectureInfo.dateTime}"
            }
            itemView.setOnClickListener { listener.onItemClickLecture(lectureInfo) }
        }
    }
}

interface OnItemClickListenerLecture {
    fun onItemClickLecture(item: Lecture)
}