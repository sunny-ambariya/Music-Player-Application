package com.example.musicplayerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent?.action){
            ApplicationClass.PREVIOUS->  prevNextSong(increment = false, context = context!!)
            ApplicationClass.PLAY -> if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT->  prevNextSong(increment = true, context = context!!)
            ApplicationClass.EXIT -> {
                exitApplication()
            }
        }
    }

    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause)

    }
    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.baseline_play_arrow_24)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.baseline_play_arrow_24)
    }

    private fun prevNextSong(increment: Boolean, context:Context){
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMusicPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player).centerCrop())
            .into(PlayerActivity.binding.songImgPA)
        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player).centerCrop())
            .into(NowFragment.binding.songImgNP)
        NowFragment.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
    }

}