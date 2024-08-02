package com.fpi.biometricsystem.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fpi.biometricsystem.data.EventInfo
import com.fpi.biometricsystem.data.ExamEvent
import com.fpi.biometricsystem.databinding.EventListItemBinding
import com.fpi.biometricsystem.databinding.ExamEventListItemBinding


class ExamEventListAdapter(private val events: List<ExamEvent>, private val listener: OnExamItemClickListener ) :
    RecyclerView.Adapter<ExamEventListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ExamEventListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val comment = events[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    inner class MyViewHolder(private val binding: ExamEventListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var eventInfo: ExamEvent

        fun bind(event: ExamEvent) {
            this.eventInfo = event
            binding.apply {
                eventName.text = event.courseName
                eventLocation.text = "Course Code: ${event.courseCode}\nDate: ${event.examDate}\nDuration: ${event.duration}"
            }
            itemView.setOnClickListener { listener.onItemClick(event) }
        }
    }
}

interface OnExamItemClickListener {
    fun onItemClick(item: ExamEvent)
}