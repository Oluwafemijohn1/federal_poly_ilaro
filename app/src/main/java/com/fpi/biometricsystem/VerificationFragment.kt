package com.fpi.biometricsystem

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.hibory.CommonApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fgtit.device.Constants
import com.fgtit.device.FPModule
import com.fgtit.device.ImageUtils
import com.fpi.biometricsystem.data.MessageType
import com.fpi.biometricsystem.data.local.models.StaffInfo
import com.fpi.biometricsystem.databinding.VerificationFragmentBinding
import com.fpi.biometricsystem.utils.EventObserver
import com.fpi.biometricsystem.utils.MatchIsoTemplateStr
import com.fpi.biometricsystem.utils.displayDialog
import com.fpi.biometricsystem.utils.makeToast
import com.fpi.biometricsystem.viewmodels.StaffVerificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerificationFragment : Fragment() {
    private lateinit var binding: VerificationFragmentBinding
    private val viewModel: StaffVerificationViewModel by activityViewModels()
    private val fpm = FPModule()
    private var worktype = 0
    private var matsize = 0
    private val matdata = ByteArray(Constants.TEMPLATESIZE * 2)
    private var refstring = ""
    private var matstring = ""
    private var refsize = 0
    private val bmpdata = ByteArray(Constants.RESBMP_SIZE)
    private var bmpsize = 0
    private var commonApi: CommonApi? = null
    private val refdata = ByteArray(Constants.TEMPLATESIZE * 2)
    private lateinit var staffInfo: StaffInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = VerificationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initFPM()
        observeMessages()
    }

    private fun initFPM() {
        fpm.SetContextHandler(requireContext(), mHandler)
        fpm.SetTimeOut(Constants.TIMEOUT_LONG);
        fpm.SetLastCheckLift(true);
        fpm.SetTimeOut(20)
        fpm.SetLastCheckLift(false)
    }

    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.messageResponseLiveData.observe(viewLifecycleOwner, EventObserver {
                        if (it.messageType == MessageType.ERROR) {
                            requireContext().makeToast(it.message)
                        } else {

                        }
                    })
                }
            }
        }
    }

    private fun initViews() {
        with(binding) {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

            checkDetailsBtn.setOnClickListener {
                val userId = idEt.text
                if (userId.isNullOrEmpty()) {
                    // Toast
                    return@setOnClickListener
                } else {
                    // Check for details within database
                    viewModel.getUser(userId.toString())
                        .observe(viewLifecycleOwner) { user ->
                            if (user != null) {
                                binding.apply {
                                    userDetailsTxt.text =
                                        "User found! \nName: ${user?.name} \nDepartment: ${user?.department}"
                                    userDetailsTxt.isVisible = true
                                    captureBtn.isVisible = true
                                    staffInfo = user
                                }
                            } else {
                                userDetailsTxt.text = "User not found"
                                captureBtn.isVisible = false
                            }
                        }
                }
            }

            captureBtn.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 0
                } else {
                    requireContext().makeToast("Busy")
                }
            }
        }

    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            binding.apply {
                when (msg.what) {
                    Constants.FPM_DEVICE -> when (msg.arg1) {
                        Constants.DEV_OK -> {
                            //FP08 && android 11 设置方向
                            if (Build.MODEL == "FP08" && Build.VERSION.RELEASE == "11") {
                                fpm.FPSetDri1() // Default
                            }
                        } else -> {

                        }
                    }

                    Constants.FPM_PLACE -> {
                        requireContext().makeToast("Place Finger")
                    }

                    Constants.FPM_LIFT -> {
                        requireContext().makeToast("Lift Finger")
                    }

                    Constants.FPM_GENCHAR -> {
                        requireContext().makeToast("Generate Characters start")
                        if (msg.arg1 == 1) {
                            if (worktype == 0) {
                                val scores = arrayListOf<Int>()
                                staffInfo.fingerprint.forEach {
                                    refstring = staffInfo.fingerprint.first()
                                    matsize = fpm.GetTemplateByGen(matdata)
                                    matstring = Base64.encodeToString(matdata, 0)
                                    val score = MatchIsoTemplateStr(matstring, refstring)
                                    scores.add(score)
                                }

                                if (scores.any {it >= 75}) {
                                    displayDialog(
                                        title = "Verification Successful",
                                        message = "Fingerprint match score: ${scores.max()}",
                                        requireContext()
                                    )
                                } else {
                                    displayDialog(
                                        title = "Verification Failed",
                                        message = "Please try again\n\nscore: ${scores.max()}",
                                        requireContext()
                                    )
                                }
                            }
                        } else {
                            requireContext().makeToast("Generate Template Fail")
                        }
                    }

                    Constants.FPM_NEWIMAGE -> {
                        bmpsize = fpm.GetBmpImage(bmpdata)
                        val bm1 = BitmapFactory.decodeByteArray(bmpdata, 0, bmpsize)
                        fingerPrintIV.setImageBitmap(bm1)
                        fingerPrintIV.isVisible = true
                        ImageUtils.ConvertBitmap2BMP(bm1)
                    }

                    Constants.FPM_TIMEOUT -> requireContext().makeToast("Time Out")
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        fp09_power_onoff(true)
        fpm.ResumeRegister()
        fpm.OpenDevice()
    }

    override fun onStop() {
        super.onStop()
        fpm.PauseUnRegister()
        fpm.CloseDevice()
        fp09_power_onoff(false)
    }

    private fun fp09_power_onoff(on: Boolean) {
        if (commonApi == null) {
            commonApi = CommonApi()
        }
        if (Build.MODEL == "FP09") {
            if (on) {
                commonApi!!.setGpioDir(GPIO_FINGER_POWER, 1)
                commonApi!!.setGpioOut(GPIO_FINGER_POWER, 1)
            } else {
                commonApi!!.setGpioDir(GPIO_FINGER_POWER, 1)
                commonApi!!.setGpioOut(GPIO_FINGER_POWER, 0)
            }
        }
    }

    companion object {
        var GPIO_FINGER_POWER = 140
    }
}