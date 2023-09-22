package com.example.cool_time
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.cool_time.R
import com.example.cool_time.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener

class MainActivity : AppCompatActivity() {
    private var binding : ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.dlMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)    //스와이프로 드로워를 여는 것을 금지
        setUpActionBar()    //액션바 세팅
        addMainFragment()   //메인 프래그먼트로 메인화면을 채움
        setupMenuOptions()  //메뉴 옵션에 대한 처리
    }

    //메뉴 옵션을 클릭하였을 때 처리하는 함수
    fun setupMenuOptions(){
        binding!!.navView.setNavigationItemSelectedListener(object : OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when(item.itemId){  //각각 옵션을 클릭할 때마다 프래그먼트 컨테이너를 바꿔주고, 드로워를 닫아줌
                    R.id.phone_lock -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frag_container, PhoneLockFragment()).commit()
                        Toast.makeText(this@MainActivity, "PHONE LOCK", Toast.LENGTH_SHORT).show()
                        binding!!.dlMain.closeDrawer(GravityCompat.START)
                    }
                    R.id.direct_lock -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frag_container, DirectLockFragment()).commit()
                        Toast.makeText(this@MainActivity, "DIRECT LOCK", Toast.LENGTH_SHORT).show()
                        binding!!.dlMain.closeDrawer(GravityCompat.START)
                    }
                    R.id.use_stat -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frag_container, UseStatFragment()).commit()
                        Toast.makeText(this@MainActivity, "USE STAT", Toast.LENGTH_SHORT).show()
                        binding!!.dlMain.closeDrawer(GravityCompat.START)
                    }
                    R.id.calendar -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frag_container, CalendarFragment()).commit()
                        Toast.makeText(this@MainActivity, "CALENDAR", Toast.LENGTH_SHORT).show()
                        binding!!.dlMain.closeDrawer(GravityCompat.START)
                    }
                    R.id.alarm -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frag_container, AlarmFragment()).commit()
                        Toast.makeText(this@MainActivity, "ALARM", Toast.LENGTH_SHORT).show()
                        binding!!.dlMain.closeDrawer(GravityCompat.START)
                    }

                }
                return true
            }
        })
    }

    fun addMainFragment(){
        supportFragmentManager.beginTransaction().add(R.id.frag_container, MainFragment()).commit()
    }
    fun setUpActionBar(){
        setSupportActionBar(binding!!.toolbar) //toolbar -> actionbar
       val actionbar = supportActionBar    //actionbar object (getSupportActionBar())
        actionbar!!.elevation = 10.0F
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
            android.R.id.home-> {    //menu button
                binding!!.dlMain.openDrawer(GravityCompat.START)    //메뉴 버튼 클릭하였을 때 드로워 열릴 수 있도록 함
            }
        }
        return super.onOptionsItemSelected(item)
    }

}