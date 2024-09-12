package com.example.musicplayerapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayerapp.databinding.MusicItemViewBinding

class MusicAdapter(private val context: Context, private var musicList:ArrayList<DataMusic>) : RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {
    class MyViewHolder(binding: MusicItemViewBinding) : RecyclerView.ViewHolder(binding.root){

        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(MusicItemViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player).centerCrop())
            .into(holder.image)

        holder.root.setOnClickListener {

            when{
                MainActivity.search -> sendIntent(ref = "MusicAdapterSearch", pos = position)
                musicList[position].id == PlayerActivity.nowPlayingId ->
                    sendIntent(ref = "NowFragment", pos = PlayerActivity.songPosition)
                else -> sendIntent(ref = "MusicAdapter", pos = position)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList : ArrayList<DataMusic>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref:String, pos:Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index",pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context,intent, null)
    }
}