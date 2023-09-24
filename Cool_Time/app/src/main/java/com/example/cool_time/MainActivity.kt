package com.example.cool_time
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cool_time.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var binding : ActivityMainBinding? = null
    private var appBarConfiguration : AppBarConfiguration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setUpActionBar()    //액션바 세팅
        //setUpDrawer()   //드로워 세팅 작업
        //Navigation을 통한 메뉴 옵션 프래그먼트 이동 처리, back button 클릭 시
        // startDestination(메인 화면)으로 다시 돌아옴
        setUpNavViewController()
        supportActionBar!!.elevation = 4.0f
        Log.e("elevation", supportActionBar!!.elevation.toString())

    }


    //메뉴 옵션을 클릭하였을 때 처리하는 함수
    //Navigation을 통한 프래그먼트 이동 (메뉴 항목 당 프래그먼트 연결)
    fun setUpNavViewController(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.frag_container) as NavHostFragment
        val navController = navHostFragment.navController

        //navigation view를 navigation 컨트롤러와 연결
        binding!!.navView.setupWithNavController(navController)

        //드로워와 navigation graph를 연결
        appBarConfiguration = AppBarConfiguration(navController.graph, binding!!.dlMain)
        //액션바, 네비게이션 컨트롤러, 드로워 연결
        setupActionBarWithNavController(navController, binding!!.dlMain)


        navController.addOnDestinationChangedListener { _, navDestination, bundle ->
            // 현재 프래그먼트가 메인이면 흰색 메뉴, 아니면 흰색 백 버튼
            // 이 코드가 없으면 검은 색 버튼들로 동작
            if(navDestination.id !== R.id.nav_main){
                supportActionBar!!.setHomeAsUpIndicator(R.drawable.arrow_back_24dp)
            }
            else{
                supportActionBar!!.setHomeAsUpIndicator(R.drawable.menu_24dp)
            }

            //프래그먼트에 따라서 툴바 가운데 텍스트를 변경
            when(navDestination.id){
                R.id.nav_use_stat -> {
                    binding!!.tvToolbarName.text = "사용시간 통계"
                }
                R.id.nav_alarm ->{
                    binding!!.tvToolbarName.text = "알람"
                }
                R.id.nav_direct_lock -> {
                    binding!!.tvToolbarName.text= "바로 잠금"
                }
                R.id.nav_phone_lock -> {
                    binding!!.tvToolbarName.text = "폰 잠금"
                }
                R.id.nav_calendar -> {
                    binding!!.tvToolbarName.text = "캘린더"
                }

            }
        }


    }



    fun setUpActionBar(){
        setSupportActionBar(binding!!.toolbar) //toolbar -> actionbar
        val actionbar = supportActionBar    //actionbar object (getSupportActionBar())
        actionbar!!.setDisplayShowTitleEnabled(false) //Delete Title(App name)
        //actionbar!!.setDisplayHomeAsUpEnabled(true) //Activate Menu Button
        //actionbar!!.setHomeAsUpIndicator(R.drawable.menu_24dp)  //Replace Home button with Menu button


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

        }
        return  super.onOptionsItemSelected(item)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = binding!!.fragContainer.findNavController()

        return navController.navigateUp(appBarConfiguration!!)
    }

}