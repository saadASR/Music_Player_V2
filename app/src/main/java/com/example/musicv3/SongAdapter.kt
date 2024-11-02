package com.example.musicv3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(
    private val songs: List<Song>,
    private val onClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var currentlyPlayingIndex: Int = -1 // Track currently playing song index

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.songTitle)
        val artist: TextView = view.findViewById(R.id.songArtist)

        init {
            itemView.setOnClickListener {
                val song = songs[adapterPosition]
                onClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.artist.text = song.artist

        // Highlight the currently playing song
        holder.itemView.setBackgroundColor(
            if (position == currentlyPlayingIndex) {
                ContextCompat.getColor(holder.itemView.context, R.color.highlightColor) // Use color resource for highlight
            } else {
                ContextCompat.getColor(holder.itemView.context, android.R.color.transparent) // Use transparent color
            }
        )
    }

    override fun getItemCount() = songs.size

    // Method to update the currently playing index and notify changes
    fun setCurrentlyPlayingIndex(index: Int) {
        val oldIndex = currentlyPlayingIndex
        currentlyPlayingIndex = index
        notifyItemChanged(oldIndex) // Refresh the old item
        notifyItemChanged(currentlyPlayingIndex) // Refresh the new item
    }
}
