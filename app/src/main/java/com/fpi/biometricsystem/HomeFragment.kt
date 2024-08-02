package com.fpi.biometricsystem

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fpi.biometricsystem.adapters.HomeMenuListAdapter
import com.fpi.biometricsystem.adapters.OnHomeItemClickListener
import com.fpi.biometricsystem.data.HomeItem
import com.fpi.biometricsystem.databinding.HomeFragmentBinding
import com.fpi.biometricsystem.utils.makeToast
import com.fpi.biometricsystem.utils.showProgressDialog
import com.fpi.biometricsystem.viewmodels.HomeViewModel
import kotlinx.coroutines.launch


class HomeFragment : Fragment(), OnHomeItemClickListener {
    private lateinit var binding: HomeFragmentBinding
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        viewModel.getAllUsers()
        initListeners()
        requestPermissions()
//        requireContext().showProgressDialog("Updating Database...", true)
    }

    private fun initListeners() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.studentsUpdateFlow.collect {
                        binding.studentsNoValue.text = it.size.toString()
                    }
                }

                launch {
                    viewModel.staffUpdateFlow.collect {
                        binding.staffNoValue.text = it.size.toString()
                    }
                }
                launch {
                    viewModel.gettingAllUsers.observe(viewLifecycleOwner) { done ->
                        if (done) {
                            requireContext().showProgressDialog(on = false)
//                            requireContext().makeToast("Database updated")
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        with(binding) {
            val menuItemList: List<HomeItem> = listOf(
                HomeItem(title = "Student\nRegistration", resId = 0),
                HomeItem(title = "Staff\nRegistration", resId = 1),
                HomeItem(title = "Student\nAttendance", resId = 2),
                HomeItem(title = "Staff\nAttendance", resId = 3),
                HomeItem(title = "Examination\nAttendance", resId = 4),
//                HomeItem(title = "Info", resId = 5),
            )

            val gridLayoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            val homeAdapter = HomeMenuListAdapter(menuItemList, this@HomeFragment)
            homeItemsRecycler.apply {
                layoutManager = gridLayoutManager
                adapter = homeAdapter
            }
            homeAdapter.notifyDataSetChanged()
        }
    }

    private fun requestPermissions() {
        val permissions: MutableList<String> = ArrayList()
        if (requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissions.size > 0) {
            val permissionArray = permissions.toTypedArray()
            requestPermissions(permissionArray, REQUESRT_PERMISSION_CODE)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
            Environment.isExternalStorageManager()
        ) {
            requireContext().makeToast("Welcome to FPI Biometric system")
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUESRT_PERMISSION_CODE) {
            // Request for WRITE_EXTERNAL_STORAGE permission.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission request was denied.
                //Toast.makeText(this, "未授权", Toast.LENGTH_SHORT).show();
                //finish();
            }
        }
    }

    companion object {
        private const val REQUESRT_PERMISSION_CODE = 1
        private val RERMISSION_GROUP = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val deviceSN: String?
            get() {
                var serial: String? = null
                try {
                    val c = Class.forName("android.os.SystemProperties")
                    val get = c.getMethod("get", String::class.java)
                    serial = get.invoke(c, "vendor.gsm.serial") as String
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return serial
            }
    }

    override fun onItemClick(item: HomeItem) {
        when (item.resId) {
            0 -> findNavController().navigate(HomeFragmentDirections.toStudentRegistrationFragment())
            1 -> findNavController().navigate(HomeFragmentDirections.toStaffRegistrationFragment())
            2 -> findNavController().navigate(
                HomeFragmentDirections.toEventSelectionFragment(
                    direction = "student"
                )
            )

            3 -> findNavController().navigate(
                HomeFragmentDirections.toEventSelectionFragment(
                    direction = "staff"
                )
            )

            4 -> findNavController().navigate(
                HomeFragmentDirections.toEventSelectionFragment(
                    direction = "exam"
                )
            )

            5 -> findNavController().navigate(HomeFragmentDirections.toEventSelectionFragment())
        }
    }
}



