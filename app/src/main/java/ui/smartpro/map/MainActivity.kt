package ui.smartpro.map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import ui.smartpro.map.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var bottom_navigation: BottomNavigationView
    private lateinit var bottomCompanyNavigation: BottomNavigationView
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottom_navigation = binding.bottomNavigation
        bottomCompanyNavigation = binding.bottomNavigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_main_fragment) as NavHostFragment
        navController = navHostFragment.navController
        initBottomNavigation()
    }

    private fun initBottomNavigation() {

        bottom_navigation.setOnItemSelectedListener {
            when (it.itemId) {

                R.id.action_main_screen -> {
                    navController.navigate(R.id.mapsFragment)
                    true
                }
                R.id.action_detail -> {
                    navController.navigate(R.id.detailMarkerFragment)
                    true
                }
                else -> true
            }
        }
    }
}