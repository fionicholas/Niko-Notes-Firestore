package com.fionicholas.samplefirestore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.items_notes.view.*
import java.util.*

class MainAdapter : RecyclerView.Adapter<MainAdapter.AudioTrainerViewHolder>() {
    private var notes = ArrayList<Notes>()

    fun setData(data: List<Notes>?) {
        if (data == null) return
        this.notes.clear()
        this.notes.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioTrainerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items_notes, parent, false)
        return AudioTrainerViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioTrainerViewHolder, position: Int) {
        val movies = notes[position]
        holder.bind(movies)
    }

    override fun getItemCount(): Int = notes.size

    inner class AudioTrainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: Notes) {
            with(itemView) {
                tvTitle.text = data.title
                tvMessage.text = data.message
            }
        }
    }
}