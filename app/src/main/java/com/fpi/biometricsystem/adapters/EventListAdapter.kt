package com.fpi.biometricsystem.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fpi.biometricsystem.data.EventInfo
import com.fpi.biometricsystem.databinding.EventListItemBinding


class EventListAdapter(private val events: List<EventInfo>, private val listener: OnItemClickListener ) :
    RecyclerView.Adapter<EventListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = EventListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val comment = events[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    inner class MyViewHolder(private val binding: EventListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var eventInfo: EventInfo

        fun bind(event: EventInfo) {
            this.eventInfo = event
            binding.apply {
//                eventId.text = event.eventNumber
                eventName.text = event.title
                eventLocation.text = "Location: ${event.location}\nDate: ${event.date}\nTime: ${event.time}"
            }
            itemView.setOnClickListener { listener.onItemClick(event) }
        }
    }
}

interface OnItemClickListener {
    fun onItemClick(item: EventInfo)
}