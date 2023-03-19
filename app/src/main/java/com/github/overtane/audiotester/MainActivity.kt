package com.github.overtane.audiotester

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.github.overtane.audiotester.ui.MainFragment

class MainActivity : AppCompatActivity() {

    private var navHostFragment: NavHostFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment?.navController?.let {
            val appBarConfiguration = AppBarConfiguration(it.graph)
            NavigationUI.setupActionBarWithNavController(this, it, appBarConfiguration)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navHostFragment?.navController?.let { return it.navigateUp() }
        return false
    }

}