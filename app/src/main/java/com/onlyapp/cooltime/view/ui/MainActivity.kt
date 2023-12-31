package com.onlyapp.cooltime.view.ui

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.window.OnBackInvokedDispatcher
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.databinding.ActivityMainBinding
import com.onlyapp.cooltime.receiver.MyBroadcastReceiver
import com.onlyapp.cooltime.service.UseTimeService
import com.onlyapp.cooltime.utils.Permission
import com.onlyapp.cooltime.view.adapter.PermissionScreenAdapter
import com.onlyapp.cooltime.view.ui.dialog.ShareDialog
import com.onlyapp.cooltime.view.ui.permission.CheckPermissionActivity

var backTime: Long = 0

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    val binding get() = _binding!!
    private var appBarConfiguration: AppBarConfiguration? = null


    lateinit var adapter: PermissionScreenAdapter// = PermissionScreenAdapter(datas = datas, this, test!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()    //액션바 세팅
        setUpNavViewController()

        // 모든 권한이 다 설정되었는지 체크하고 만약 안되어 있는게 있다면 권한 설정하는 화면으로 이동
        if (!Permission.checkAllPermission(this))
            startActivityForResult(Intent(this, CheckPermissionActivity::class.java), 0)

        startService(Intent(this, UseTimeService::class.java))

        registerReceiver(MyBroadcastReceiver(), IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_DATE_CHANGED)
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.frag_container) as NavHostFragment
                val navController = navHostFragment.navController
                val navDestination = navController.currentDestination

                navDestination?.let {
                    if (it.id == R.id.main) {
                        if (System.currentTimeMillis() - backTime >= 2000 || backTime == 0.toLong()) { //2초내에 다시 누른게 아니면
                            backTime =
                                System.currentTimeMillis() // 마지막으로 back버튼을 누른 시간 갱신
                            Snackbar.make(
                                binding.root,
                                "뒤로가기 버튼을 한번 더 누르면 종료됩니다.",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                            //뒤로가기 버튼 한번 더 누르면 종료된다고 메세지 띄우기
                        } else {
                            super.onBackPressed()
                        }
                    } else super.onBackPressed()
                }
            }
        } else {
            onBackPressedDispatcher.addCallback(this /* lifecycle owner */) {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.frag_container) as NavHostFragment
                val navController = navHostFragment.navController
                val navDestination = navController.currentDestination

                navDestination?.let {
                    if (it.id == R.id.main) {
                        if (System.currentTimeMillis() - backTime >= 2000 || backTime == 0.toLong()) { //2초내에 다시 누른게 아니면
                            backTime =
                                System.currentTimeMillis() // 마지막으로 back버튼을 누른 시간 갱신
                            Snackbar.make(
                                binding.root,
                                "뒤로가기 버튼을 한번 더 누르면 종료됩니다.",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                            //뒤로가기 버튼 한번 더 누르면 종료된다고 메세지 띄우기
                        } else {
                            super.onBackPressed()
                        }
                    } else super.onBackPressed()
                }
            }
        }
    }


    // 권한 설정 화면으로 이동했다가 뒤로 가는 등 권한 설정하는 액티비티에서 종료 했을때 넘어오는 종료값을 보고서
    // 종료값이 0이면 그냥 뒤로가기로 돌아온 것이므로 앱 종료
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == 0) {
            finish()
        }
    }


    //메뉴 옵션을 클릭하였을 때 처리하는 함수
    //Navigation을 통한 프래그먼트 이동 (메뉴 항목 당 프래그먼트 연결)
    private fun setUpNavViewController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.frag_container) as NavHostFragment
        val navController = navHostFragment.navController

        //navigation view를 navigation 컨트롤러와 연결
        binding.navView.setupWithNavController(navController)

        //드로워와 navigation graph를 연결
        //AppBarConfiguration()은 Navigation 메소드에 대한 옵션 설정 같은 것 **메소드 : 어떤 기능을 실행하는 코드 블록**
        //첫번째 graph인자는 Up button(뒤로가기)을 표시하지 않을 최상위 목적지를 넣는 곳 -> graph로 넣어주면 graph의 start destination이 최상위 목적지로 자동으로 넘어가 해당 최상위 목적지에서는 Up button을 표시하지 않음
        //navController.graph 대신에 setOf(최상위 디렉토리들)로 설정해서 넘겨줘도 동작 본 앱에선 ex:) setOf(R.id.main)을 넘겨줘도 main화면에 위치해 있을 때 나오지 않음
        //두번째 인자는 Navigation 버튼이 토글되었을 때(눌러졌을 때) Drawer 메뉴를 열 수 있는 Drawer Layout을 지정, Navigation 버튼이 Up 버튼으로 설정되지 않을 때, Drawer버튼(햄버거 모양)으로 표시됨
        //=>따라서 시스템상에서 최상위 디렉토리에선 자동으로 햄버거 모양의 아이콘 생성 및 눌렀을 때 메뉴 튀어나오는 액션이 설정되고, 나머지 디렉토리에서는 자동으로 뒤로가기 아이콘과, 뒤로가는 액션이 설정됨.
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.dlMain)

        //액션바, 네비게이션 컨트롤러, 드로워 연결
        //커스텀한 Configuration값을 넣어서 Navigation Controller를 통해 ActionBar를 세팅해줌
        setupActionBarWithNavController(navController, appBarConfiguration!!)

        navController.addOnDestinationChangedListener { _, navDestination, _ ->
            //이미 이렇게 따로 setHomeAsUpIndicator 같은걸 쓰지 않아도 햄버거 아이콘과 뒤로가기 아이콘 및 액션 생성되지만, 하얀색 아이콘으로 바꾸기 위해
            // 이 코드가 없으면 검은 색 버튼들로 동작

            supportActionBar?.let {
                if (navDestination.id != R.id.main) { // ★이게 어떤 눌렀을 때 행동을 설정하는 것 X, 그냥 겉보기 디자인만 바꾸는 것
                    it.setHomeAsUpIndicator(R.drawable.arrow_back_24dp)
                } else {
                    it.setHomeAsUpIndicator(R.drawable.menu_24dp)
                }
            }

            //프래그먼트에 따라서 툴바 가운데 텍스트를 변경
            when (navDestination.id) {
                R.id.main -> {
                    binding.tvToolbarName.text = getString(R.string.app_name)
                }

                R.id.use_stat -> {
                    binding.tvToolbarName.text = getString(R.string.use_stat)
                }

                R.id.alarm_main -> {
                    binding.tvToolbarName.text = getString(R.string.alarm)
                }

                R.id.direct_lock_main -> {
                    binding.tvToolbarName.text = getString(R.string.direct_lock)
                }

                R.id.phone_lock_main -> {
                    binding.tvToolbarName.text = getString(R.string.phone_lock)
                }

                R.id.calendar -> {
                    binding.tvToolbarName.text = getString(R.string.calendar)
                }

                R.id.exception -> {
                    binding.tvToolbarName.text = getString(R.string.exception_app_name)
                }
            }
        }
    }

    // 액션바로부터 액티비티 계층 내에서 Up(뒤로가기)버튼을 눌렀을 때 어떻게 처리할지 설정
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.frag_container) as NavHostFragment
        val navController = navHostFragment.navController
        //Navigation Controller가 up 버튼을 눌렀을 때 어떻게 처리할지 설정 => 앞서 설정했던 configuration을 넣어서 처리
        return navController.navigateUp(appBarConfiguration!!)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbar) //toolbar -> actionbar
        supportActionBar?.let {
            val actionbar = it    //actionbar object (getSupportActionBar())
            actionbar.setDisplayShowTitleEnabled(false) //Delete Title(App name)
            //actionbar!!.setDisplayHomeAsUpEnabled(true) //Activate Menu Button
            //actionbar!!.setHomeAsUpIndicator(R.drawable.menu_24dp)  //Replace Home button with Menu button
        }
    }

    //onCreateOptionsMenu는 액티비티 실행될 때 딱 한번 호출,
    //앱바 우측에 공유 모양 아이콘인 아이템을 추가 **아이템 내부에 showAsAction이 always로 설정되어 땡땡이 아이콘 없이 공유 아이콘만 깔끔하게 나타남**
    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Activate Share button
        menuInflater.inflate(R.menu.actionbar_share_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> {  //share button 누른게 공유 아이콘 버튼일 때 작성할 메소드
                ShareDialog().show(supportFragmentManager, null)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}