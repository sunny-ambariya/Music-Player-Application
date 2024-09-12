package com.example.musicplayerapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayerapp.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle  // this is object of the Drawer ok.
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit var MusicListMA : ArrayList<DataMusic>
        lateinit var musicListSearch : ArrayList<DataMusic>
        var search: Boolean = false

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPinkNav)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // toggle Drawer
        toggle = ActionBarDrawerToggle(this@MainActivity, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if ( requestRuntimePermission())
         initialization()


        binding.playlistBtn.setOnClickListener {

            val intent = Intent(this@MainActivity, PlaylistActivity::class.java)
            startActivity(intent)
        }

        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","MainActivity")
            startActivity(intent)
        }

        binding.favouriteBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, FavouriteActivity::class.java))
        }

         binding.navigation.setNavigationItemSelectedListener {
             when(it.itemId){
                 R.id.navFeedback -> Toast.makeText(baseContext, "Feedback", Toast.LENGTH_SHORT).show()
                 R.id.navSettings -> Toast.makeText(baseContext, "Setting", Toast.LENGTH_SHORT).show()
                 R.id.navAbout -> Toast.makeText(baseContext, "About", Toast.LENGTH_SHORT).show()
                 R.id.navExit -> {
                     val builder = MaterialAlertDialogBuilder(this)
                     builder.setTitle("Exit")
                         .setMessage("Do you want to close app?")
                         .setPositiveButton("Yes"){ _, _ ->
                             exitApplication()

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
             true
         }

    }
    // ya valla code permission laga external device sha  thik ha Sir.
     private fun requestRuntimePermission():Boolean{
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
                 if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                     != PackageManager.PERMISSION_GRANTED) {
                     ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 13)
                     return false
                 }
             }
        else{
                 if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
                     != PackageManager.PERMISSION_GRANTED) {
                     ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), 13)
                     return false
                 }
             }
        return true
     }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted",Toast.LENGTH_SHORT).show()
                 initialization()
        }
    }

      // override drawer mendatory
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

     @SuppressLint("SetTextI18n")
     private fun initialization(){
         search = false
         MusicListMA = getAllAudio()
         binding.musicRV.setHasFixedSize(true)
         binding.musicRV.setItemViewCacheSize(13)
         binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
         musicAdapter = MusicAdapter(this, MusicListMA)
         binding.musicRV.adapter = musicAdapter
         binding.totalSongs.text = "Total Songs: "+musicAdapter.itemCount

     }


      @SuppressLint("Recycle", "Range")
      private fun getAllAudio(): ArrayList<DataMusic>{
          val tempList = ArrayList<DataMusic>()

          val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
          val projection = arrayOf(
              MediaStore.Audio.Media._ID,
              MediaStore.Audio.Media.TITLE,
              MediaStore.Audio.Media.ALBUM,
              MediaStore.Audio.Media.ARTIST,
              MediaStore.Audio.Media.DURATION,
              MediaStore.Audio.Media.DATE_ADDED,
              MediaStore.Audio.Media.DATA,
              MediaStore.Audio.Media.ALBUM_ID
              )
          val cursor = this.contentResolver.query(
              MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
              MediaStore.Audio.Media.DATE_ADDED+ "", null)

          if(cursor != null){
              if(cursor.moveToFirst()){
                  do {
                      val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                      val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                      val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                      val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                      val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                      val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                      val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                      val uri = Uri.parse("content://media/external/audio/albumart")
                      val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                      val music = DataMusic(id = idC, title = titleC, album = albumC, artist = artistC, duration = durationC, path = pathC, artUri = artUriC)
                      val file = File(music.path)
                      if (file.exists())
                          tempList.add(music)
                  }while (cursor.moveToNext())
                  cursor.close()

              }

          }
          return tempList

      }

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerActivity.isPlaying && PlayerActivity.musicService!=null){
            exitApplication()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)

        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if(newText != null) {
                    val userInput = newText.lowercase()
                    for (song in MusicListMA)
                        if (song.title.lowercase().contains(userInput))
                            musicListSearch.add(song)
                    search = true
                    musicAdapter.updateMusicList(searchList = musicListSearch)
                }
                return true
            }
        })
            return super.onCreateOptionsMenu(menu)

    }
}