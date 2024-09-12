package com.example.musicplayerapp

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import com.example.musicplayerapp.PlayerActivity.Companion.binding
import com.example.musicplayerapp.PlayerActivity.Companion.musicService

class MusicService : Service() {

    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable:Runnable

    override fun onBind(p0: Intent?): IBinder{
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder:Binder(){
        fun currentService():MusicService{
            return this@MusicService
        }
    }

    fun showNotification(playPauseBtn:Int){
        val intent = Intent(baseContext, MainActivity::class.java)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)

        val imgArt = getImageArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
                .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
                .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
                .setSmallIcon(R.drawable.baseline_library_music_24).setLargeIcon(image)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.previous, "Previous", prevPendingIntent)
                .addAction(playPauseBtn, "PlayPause", playPendingIntent)
                .addAction(R.drawable.next, "Next", nextPendingIntent)
                .addAction(R.drawable.exit, "Exit", exitPendingIntent)
                .build()

        startForeground(13, notification)
    }
     fun createMusicPlayer(){
         try {
             if (mediaPlayer == null) mediaPlayer = MediaPlayer()
             mediaPlayer?.reset()
             mediaPlayer?.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
             mediaPlayer?.prepare()

             PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause)
             showNotification(R.drawable.pause)
             PlayerActivity.binding.tvSeekBarStart.text =
                 formatDuration(mediaPlayer!!.currentPosition.toLong())
             PlayerActivity.binding.tvSeekBarEnd.text =
                 formatDuration(mediaPlayer!!.duration.toLong())
             PlayerActivity.binding.seekBarPA.progress = 0
             PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration
             PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
         } catch (e: Exception) {
             return
         }
    }
       fun seekBarSetup(){
           runnable = Runnable {
               binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
               PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
               Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
           }
           Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
       }
  }