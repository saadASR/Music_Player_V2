package com.example.musicv3

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private var musicService: MusicService? = null
    private var isBound = false
    private var currentSongIndex = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var adapter: SongAdapter

    // Define the songs list as a member variable
    private val songs = listOf(
        Song("Creep", "Lucifer", R.raw.song1),
        Song("Guerilla", "Soolking", R.raw.song2)
    )

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.LocalBinder
            musicService = binder.getService()
            isBound = true
            binder.setSongs(songs) // Pass songs to the service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        playPauseButton = findViewById(R.id.playPauseButton)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.previousButton)

        // Bind to MusicService
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }

        // Set up RecyclerView and adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SongAdapter(songs) { song ->
            currentSongIndex = songs.indexOf(song)
            playSong(currentSongIndex)
        }
        recyclerView.adapter = adapter

        // Set up play/pause button
        playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        // Set up next and previous buttons
        nextButton.setOnClickListener {
            playNextSong()
        }

        previousButton.setOnClickListener {
            playPreviousSong()
        }
    }

    private fun playSong(index: Int) {
        currentSongIndex = index
        musicService?.playAudio(songs[currentSongIndex].resourceId)
        updateToolbarTitle(songs[currentSongIndex]) // Update the toolbar with the song title
        adapter.setCurrentlyPlayingIndex(currentSongIndex) // Highlight the currently playing song
        updatePlayPauseButton(true)
    }

    private fun togglePlayPause() {
        if (musicService?.isPlaying == true) {
            musicService?.pauseAudio()
            updatePlayPauseButton(false)
        } else {
            musicService?.resumeAudio() // Resume audio if it was paused
            playSong(currentSongIndex) // Ensure we play the current song
            updatePlayPauseButton(true)
        }
    }

    private fun playNextSong() {
        currentSongIndex = (currentSongIndex + 1) % songs.size // Cycle to the next song
        musicService?.playNextSong() // Play the next song in the service
        playSong(currentSongIndex)
    }

    private fun playPreviousSong() {
        currentSongIndex = (currentSongIndex - 1 + songs.size) % songs.size // Cycle to the previous song
        musicService?.playPreviousSong() // Play the previous song in the service
        playSong(currentSongIndex)
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        playPauseButton.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play) // Change icon
    }

    private fun updateToolbarTitle(song: Song) {
        supportActionBar?.title = song.title // Set the toolbar title to the currently playing song's title
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}
