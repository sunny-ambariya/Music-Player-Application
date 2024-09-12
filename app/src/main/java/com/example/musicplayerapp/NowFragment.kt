package com.example.musicplayerapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayerapp.databinding.FragmentNowBinding


class NowFragment : Fragment() {


    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowBinding
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now, container, false)
        binding = FragmentNowBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.playPauseBtnNP.setOnClickListener {
            if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
        }

        binding.nextBtnNP.setOnClickListener {
            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMusicPlayer()
            Glide.with(requireContext())
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player).centerCrop())
                .into(binding.songImgNP)
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.pause)
            playMusic()
        }
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "NowFragment")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true
            Glide.with(requireContext())
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player).centerCrop())
                .into(binding.songImgNP)
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            if(PlayerActivity.isPlaying) binding.playPauseBtnNP.setIconResource(R.drawable.pause)
            else binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
        }
    }

    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.pause)
        PlayerActivity.binding.nextBtnPA.setIconResource(R.drawable.pause)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause)
    }
    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
        PlayerActivity.binding.nextBtnPA.setIconResource(R.drawable.baseline_play_arrow_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
    }
}