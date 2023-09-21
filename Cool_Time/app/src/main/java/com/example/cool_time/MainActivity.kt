package com.example.cool_time
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.cool_time.R
import com.example.cool_time.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding : ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setUpActionBar()
        addMainFragment()
    }

    fun addMainFragment(){
        supportFragmentManager.beginTransaction().add(R.id.frag_container, MainFragment()).commit()
    }
    fun setUpActionBar(){
        setSupportActionBar(binding!!.toolbar) //toolbar -> actionbar
        val actionbar = supportActionBar    //actionbar object (getSupportActionBar())

        actionbar!!.setDisplayShowTitleEnabled(false) //Delete Title(App name)
        actionbar!!.setDisplayHomeAsUpEnabled(true) //Activate Menu Button

        actionbar!!.setHomeAsUpIndicator(R.drawable.menu_24dp)  //Replace Home button with Menu button

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Activate Share button
        menuInflater.inflate(R.menu.actionbar_share_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.share-> {  //share button
                Toast.makeText(this, "Click Share Button", Toast.LENGTH_SHORT).show()
                //TODO KAKAOTALK SHARE FUNCTION
            }
            android.R.id.home->{    //menu button
                Toast.makeText(this, "Click Menu Button", Toast.LENGTH_SHORT).show()
            }
            else ->{
                Log.e("Another Button", "Click")
            }
        }
        return super.onOptionsItemSelected(item)
    }
}