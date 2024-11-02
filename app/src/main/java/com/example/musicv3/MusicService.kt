package com.example.musicv3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MusicService : Service() {

    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var songs: List<Song>
    private var currentSongIndex = 0 // Track the current song index
    var isPlaying = false // Track whether music is playing

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService

        fun setSongs(songList: List<Song>) {
            songs = songList
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun playAudio(resourceId: Int) {
        try {
            mediaPlayer?.release() // Release previous media player if it exists
            mediaPlayer = MediaPlayer.create(this, resourceId)
            mediaPlayer?.setOnCompletionListener {
                // Handle completion
                playNextSong() // Automatically play the next song when current song completes
            }
            mediaPlayer?.start()
            isPlaying = true // Update play state
            showNotification(resourceId) // Show notification when audio starts

        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
        }
    }

    fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false // Update play state
        showNotification(mediaPlayer?.audioSessionId ?: 0) // Update notification when paused
    }

    fun resumeAudio() {
        mediaPlayer?.start()
        isPlaying = true // Update play state
        showNotification(mediaPlayer?.audioSessionId ?: 0) // Update notification when resumed
    }

    fun playNextSong() {
        if (songs.isNotEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songs.size // Move to the next song, looping back to the first
            playAudio(songs[currentSongIndex].resourceId) // Play the next song
        }
    }

    fun playPreviousSong() {
        if (songs.isNotEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songs.size) % songs.size // Move to the previous song, looping back to the last
            playAudio(songs[currentSongIndex].resourceId) // Play the previous song
        }
    }

    private fun showNotification(resourceId: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "music_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Music Service", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        // Create intents for notification actions
        val playIntent = Intent(this, MusicService::class.java).setAction(ACTION_PLAY)
        val pauseIntent = Intent(this, MusicService::class.java).setAction(ACTION_PAUSE)
        val nextIntent = Intent(this, MusicService::class.java).setAction(ACTION_NEXT)
        val previousIntent = Intent(this, MusicService::class.java).setAction(ACTION_PREVIOUS)

        val playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val nextPendingIntent = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val previousPendingIntent = PendingIntent.getService(this, 3, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Playing: ${songs[currentSongIndex].title}")
            .setContentText("Your song is playing")
            .setSmallIcon(R.drawable.music) // Your notification icon
            .addAction(R.drawable.ic_previous, "Previous", previousPendingIntent) // Previous button
            .addAction(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                if (isPlaying) pausePendingIntent else playPendingIntent) // Play/Pause button
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent) // Next button
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_PLAY -> resumeAudio()
                ACTION_PAUSE -> pauseAudio()
                ACTION_NEXT -> playNextSong() // Handle next song logic
                ACTION_PREVIOUS -> playPreviousSong() // Handle previous song logic
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        const val ACTION_PLAY = "com.example.musicv3.action.PLAY"
        const val ACTION_PAUSE = "com.example.musicv3.action.PAUSE"
        const val ACTION_NEXT = "com.example.musicv3.action.NEXT"
        const val ACTION_PREVIOUS = "com.example.musicv3.action.PREVIOUS"
    }
}
