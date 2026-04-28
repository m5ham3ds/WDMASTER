package com.wdmaster.app.presentation

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.wdmaster.app.R
import com.wdmaster.app.databinding.ActivityMainBinding
import com.wdmaster.app.presentation.common.AboutDialog
import com.wdmaster.app.presentation.common.ThemeManager
import com.wdmaster.app.data.local.preferences.ThemePreferences
import com.wdmaster.app.util.LocaleHelper
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val themePreferences: ThemePreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // ضبط الثيم واللغة قبل setContentView
        ThemeManager.applyTheme(themePreferences)
        LocaleHelper.setLocale(this, LocaleHelper.getPersistedLocale(this))
        super.onCreate(savedInstanceState)

        // ربط الواجهة عبر ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // إعداد Toolbar
        setSupportActionBar(binding.toolbarContainer.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // إعداد Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavContainer.bottomNav.setupWithNavController(navController)
        NavigationUI.setupWithNavController(binding.navView, navController)

        // زر القائمة الجانبية
        binding.toolbarContainer.ivMenu.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // التعامل مع عناصر Navigation Drawer
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_about_drawer -> {
                    showAboutDialog()
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
                R.id.nav_exit_drawer -> {
                    finish()
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
            }
        }

        // معالجة الضغط على زر العودة
        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            } else if (!navController.popBackStack()) {
                finish()
            }
        }
    }

    private fun showAboutDialog() {
        val aboutDialog = AboutDialog()
        aboutDialog.show(supportFragmentManager, AboutDialog.TAG)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}