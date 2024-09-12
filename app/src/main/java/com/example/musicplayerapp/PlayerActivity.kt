package com.example.musicplayerapp

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayerapp.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object{
        lateinit var musicListPA : ArrayList<DataMusic>
        var songPosition: Int = 0
        var isPlaying:Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId : String = ""

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
         initializeLayout()
        binding.backBtnPA.setOnClickListener { finish() }

        binding.playPauseBtnPA.setOnClickListener { if (isPlaying) pauseMusic() else playMusic() }

        binding.previousBtnPA.setOnClickListener { preNextSong(increment = false) }
        binding.nextBtnPA.setOnClickListener { preNextSong(increment = true) }
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(esskBar: SeekBar?, progress: Int, fromUser: Boolean) {
               if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekbar: SeekBar?) = Unit
        })

    }

    private fun setLayout(){
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title
         if(min15 || min30 || min60) binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
    }

    private fun createMusicPlayer(){
         try{
             if (musicService!!.mediaPlayer==null) musicService!!.mediaPlayer = MediaPlayer()
             musicService!!.mediaPlayer!!.reset()
             musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
             musicService!!.mediaPlayer!!.prepare()
             musicService!!.mediaPlayer!!.start()
             isPlaying = true
             binding.playPauseBtnPA.setIconResource(R.drawable.pause)
             musicService!!.showNotification(R.drawable.pause)
             //for seek bar progress
             binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
             binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
             binding.seekBarPA.progress = 0
             binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
              musicService!!.mediaPlayer!!.setOnCompletionListener(this)
             nowPlayingId = musicListPA[songPosition].id
         }catch (e:Exception) { return }

        binding.timerBtnPA.setOnClickListener {
            val timer = min15 || min30 || min60
            if (!timer) showBottomSheetDialog()
            else{
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to close app?")
                    .setPositiveButton("Yes"){ _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))


                    }
                    .setNegativeButton("No"){dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
            }

         // share using intent-Filter action
        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))

        }
    }


    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){

            "NowFragment"->{
                setLayout()
                binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) binding.playPauseBtnPA.setIconResource(R.drawable.pause)
                else binding.playPauseBtnPA.setIconResource(R.drawable.baseline_play_arrow_24)
            }

            "MusicAdapterSearch" ->{
                // service start
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdapter" ->{
                // service start
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }

           "MainActivity" -> {
               // service start
               val intentService = Intent(this, MusicService::class.java)
               bindService(intentService, this, BIND_AUTO_CREATE)
               startService(intentService)
               musicListPA = ArrayList()
               musicListPA.addAll(MainActivity.MusicListMA)
               musicListPA.shuffle()
               setLayout()
           }

        }
    }

    private fun playMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.pause)
        musicService!!.showNotification(R.drawable.pause)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.baseline_play_arrow_24)
        musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun preNextSong(increment: Boolean){
        if (increment){
           setSongPosition(increment = true)
            setLayout()
           createMusicPlayer()
        }
        else{
           setSongPosition(increment = false)
            setLayout()
            createMusicPlayer()
        }

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
       val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMusicPlayer()
        musicService!!.seekBarSetup()


    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMusicPlayer()
        try {
             setLayout()
        }catch (e:Exception){
            return
        }
    }

     // this function for bottom dialog show.
    private fun showBottomSheetDialog() {
         val dialog = BottomSheetDialog(this@PlayerActivity)
         dialog.setContentView(R.layout.bottom_sheet_dialog)
         dialog.show()
         dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
             Toast.makeText(baseContext, "Music will stop after 15 minutes", Toast.LENGTH_SHORT)
                 .show()
             binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
             min15 = true
             Thread {Thread.sleep((15 * 60000).toLong())
                 if (min15) exitApplication()}.start()
                 dialog.dismiss()
             }

             dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
                 Toast.makeText(baseContext, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
                 binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                 min30 = true
                 Thread{Thread.sleep((30 * 60000).toLong())
                     if(min30) exitApplication()}.start()
                 dialog.dismiss()
             }
             dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
                 Toast.makeText(baseContext, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
                 binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                 min60 = true
                 Thread{Thread.sleep((60 * 60000).toLong())
                     if(min60) exitApplication()}.start()
                 dialog.dismiss()
             }

         }
     }