package com.fpi.biometricsystem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.fpi.biometricsystem.databinding.ActivityMainBinding
import com.fpi.biometricsystem.utils.displayDialog
import com.fpi.biometricsystem.utils.makeToast
import com.fpi.biometricsystem.utils.showProgressDialog
import com.fpi.biometricsystem.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: HomeViewModel by viewModels()
    private var regStudentCount = 0
    private var regStaffCount = 0
    private var studentCount = 0
    private var staffCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setupUI()
//        observeListeners()
    }

//    private fun observeListeners() {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                    viewModel.studentsUpdateFlow.collect { studentList ->
//                        studentCount = studentList.size
//                        regStudentCount = studentList.filter { it.fingerprint.isNotEmpty() }.size
//                    }
//                }
//
//                launch {
//                    viewModel.staffUpdateFlow.collect { staffList ->
//                        staffCount = staffList.size
//                        regStaffCount = staffList.filter { it.fingerprint.isNotEmpty() }.size
//                    }
//                }
//                launch {
//                    viewModel.gettingAllUsers.observe(this@MainActivity) { done ->
//                        if (done) {
//                            showProgressDialog(on = false)
//                            makeToast("Database updated")
//                        }
//                    }
//                }
//            }
//        }
//    }

//    private fun setupUI() {
////        binding.toolbar.inflateMenu(R.menu.menu)
//        binding.toolbar.setOnMenuItemClickListener { item ->
//            when (item.itemId) {
//                R.id.updateDatabase -> {
////                    showProgressDialog("Updating Database...", true)
////                    viewModel.getAllUsers()
//                    Toast.makeText(this, "This has been removed", Toast.LENGTH_SHORT).show()
//                    true
//                }
//
//                R.id.clearDatabase -> {
//                    Toast.makeText(this, "This has been removed", Toast.LENGTH_SHORT).show()
////                    showProgressDialog(
////                        "Clearing and Updating Database... \nPlease hold on, this might take a while",
////                        true
////                    )
////                    viewModel.clearAndUpdateDatabase()
//                    true
//                }
//
//                R.id.showDatabaseDetails -> {
//                    displayDialog(
//                        "Status",
//                        "Total No. of Students: $regStudentCount/$studentCount\nTotal No. of Lecturers: $regStaffCount/$staffCount\n",
//                        this
//                    )
//                    true
//                }
//
//                else -> super.onOptionsItemSelected(item)
//            }
//        };
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//    }
}