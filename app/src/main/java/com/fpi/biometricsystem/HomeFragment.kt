package com.fpi.biometricsystem

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
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
import com.fpi.biometricsystem.data.local.store.PreferenceStore
import com.fpi.biometricsystem.databinding.HomeFragmentBinding
import com.fpi.biometricsystem.utils.Constants
import com.fpi.biometricsystem.utils.EventObserver
import com.fpi.biometricsystem.utils.replaceIfEmpty
import com.fpi.biometricsystem.utils.sentenceCase
import com.fpi.biometricsystem.utils.showErrorDialog
import com.fpi.biometricsystem.utils.showProgressDialog
import com.fpi.biometricsystem.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(), OnHomeItemClickListener {
    private lateinit var binding: HomeFragmentBinding
    private val viewModel: HomeViewModel by activityViewModels()
    @Inject
    lateinit var preferenceStore: PreferenceStore
    private var isRemote: Boolean = false
    private var urlSource: String = Constants.REMOTE

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
        preferenceStore.baseUrl?.let {
            initViews(itemList(it))
        }
//        viewModel.getAllUsers()
//        viewModel.fetchBaseUrl()
//        requireContext().showProgressDialog(on = true)
        initListeners()
        requestPermissions()
        val url = preferenceStore.baseUrl
//        requireContext().showProgressDialog("Updating Database...", true)
    }

    private fun itemList(urlSource: String): List<HomeItem>{
        return listOf(
            HomeItem(title = "Student\nRegistration", resId = 0),
            HomeItem(title = "Staff\nRegistration", resId = 1),
            HomeItem(title = "Student\nAttendance", resId = 2),
            HomeItem(title = "Staff\nAttendance", resId = 3),
            HomeItem(title = "Examination\nAttendance", resId = 4),
            HomeItem(title = urlSource, resId = 5),
        )
    }


    private fun initListeners() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    requireContext().showProgressDialog(on = false)
                    viewModel.baseUrl.collect{
                        it?.actualData?.let { url ->
                            initViews(itemList(url))
//                            Log.d("TAG", "initListeners: data $url")
                            preferenceStore.baseUrl = url
//                            if (showUrlToast) {
//                                showErrorDialog("Base Url is updated. \n The new base url is: $url", requireContext(), title = "Successful")
//                                showUrlToast = false
//                            }
                        }
                    }
                }

                launch {
                    requireContext().showProgressDialog(on = false)
                    viewModel.baseUrl.collect{
                        it?.actualData?.let { url ->
                            initViews(itemList(url))
                            preferenceStore.baseUrl = url
                        }
                    }
                }
                launch {
                    viewModel.errorResponse.observe(viewLifecycleOwner, EventObserver {
                        requireContext().showProgressDialog(on = false)
                        showErrorDialog(it?.message.replaceIfEmpty("Something went wrong").sentenceCase(), requireContext())
                    })
                }
            }
        }
    }

    private fun initViews(list: List<HomeItem>) {
        with(binding) {
            val gridLayoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            val homeAdapter = HomeMenuListAdapter(list, this@HomeFragment)
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
//            requireContext().makeToast("Welcome to FPI Biometric system")
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

            5 -> {
                viewModel.fetchBaseUrl(if (preferenceStore.baseUrl == Constants.LOCAL) Constants.REMOTE else Constants.LOCAL)
//                viewModel.fetchBaseUrl()
            }
        }
    }
}



