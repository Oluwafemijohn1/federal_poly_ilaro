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
import androidx.navigation.fragment.navArgs
import com.fgtit.device.Constants
import com.fgtit.device.FPModule
import com.fgtit.device.ImageUtils
import com.fpi.biometricsystem.data.MessageType
import com.fpi.biometricsystem.data.local.models.StaffInfo
import com.fpi.biometricsystem.data.request.StaffAttendanceRequest
import com.fpi.biometricsystem.databinding.StaffAttendanceFragmentBinding
import com.fpi.biometricsystem.utils.EventObserver
import com.fpi.biometricsystem.utils.MatchIsoTemplateStr
import com.fpi.biometricsystem.utils.displayDialog
import com.fpi.biometricsystem.utils.makeToast
import com.fpi.biometricsystem.utils.replaceIfEmpty
import com.fpi.biometricsystem.utils.sentenceCase
import com.fpi.biometricsystem.utils.showErrorDialog
import com.fpi.biometricsystem.utils.showProgressDialog
import com.fpi.biometricsystem.viewmodels.StaffVerificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StaffAttendanceFragment : Fragment() {
    private lateinit var binding: StaffAttendanceFragmentBinding
    private val viewModel: StaffVerificationViewModel by activityViewModels()
    private val fpm = FPModule()
    private var worktype = 0
    private var matsize = 0
    private val matdata = ByteArray(Constants.TEMPLATESIZE * 2)
    private var refstring = ""
    private var matstring = ""
    private val bmpdata = ByteArray(Constants.RESBMP_SIZE)
    private var bmpsize = 0
    private var commonApi: CommonApi? = null
    private lateinit var allStaffInfo: List<StaffInfo>
    private val args: StaffAttendanceFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StaffAttendanceFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initFPM()
        viewModel.allUsers()
        requireContext().showProgressDialog("Loading...", true)
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
                    viewModel.allStaffs.collect {
                        requireContext().showProgressDialog(on = false)
                        allStaffInfo = it
                    }
                }
                launch {
                    viewModel.messageResponseLiveData.observe(viewLifecycleOwner, EventObserver {
                        requireContext().showProgressDialog(on = false)
                        if (it.messageType == MessageType.ERROR) {
                            requireContext().makeToast(it.message)
                        } else {

                        }
                    })
                }

                launch {
                    viewModel.finalResponse.observe(viewLifecycleOwner, EventObserver {
                        requireContext().showProgressDialog(on = false)
                        displayDialog(
                            title = "Attendance Saved",
                            message = it?.message.toString(),
                            requireContext()
                        )
                    })
                }
                launch {
                    viewModel.errorResponse.observe(viewLifecycleOwner, EventObserver {
                        requireContext().showProgressDialog(on = false)
                        showErrorDialog(
                            it?.message.replaceIfEmpty("Something went wrong").sentenceCase(),
                            requireContext()
                        )
                    })
                }
            }
        }
    }

    private fun initViews() {
        with(binding) {
            arrowBackBtn.setOnClickListener { findNavController().popBackStack() }
            val details = args.eventDetails
            eventTitle.text = "${details.title}\n${details.description}\nDate:${details.date} Time:${details.time}"
            checkDetailsBtn.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 0
                } else {
                    requireContext().makeToast("Busy")
                }
            }

            fingerprintIvBg.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 0
                } else {
                    requireContext().makeToast("Busy")
                }
            }

            actualFingerprintIv.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 0
                } else {
                    requireContext().makeToast("Busy")
                }
            }
            checkDetailsBtn.setOnClickListener {
//                fakeMarkAttendance()
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
                        }

                        else -> {

                        }
                    }

                    Constants.FPM_PLACE -> {}

                    Constants.FPM_LIFT -> {}

                    Constants.FPM_GENCHAR -> {
                        if (msg.arg1 == 1) {
                            if (worktype == 0) {
                                val scores = arrayListOf<Int>()
                                val matchingScores = arrayListOf<Pair<StaffInfo, Int>>()
                                matsize = fpm.GetTemplateByGen(matdata)
                                matstring = Base64.encodeToString(matdata, 0)
                                allStaffInfo.forEach { staffInfo ->
                                    staffInfo.fingerprint.forEach { fin ->
                                        refstring = fin
                                        val score = MatchIsoTemplateStr(matstring, refstring)
                                        scores.add(score)
                                        matchingScores.add(Pair(staffInfo, score))
                                        println(score)
                                    }
                                }

                                var checkingValue: Pair<StaffInfo?, Int> = Pair(null, 0)
                                for (item in matchingScores) {
                                    if (item.second > checkingValue.second) {
                                        checkingValue = item
                                    }
                                }
                                println(checkingValue)

                                if (checkingValue.second >= 75) {
                                    displayDialog(
                                        title = "Verification Successful",
                                        message = "Name: ${checkingValue.first?.name} \nStaff ID: ${checkingValue.first?.idNo}",
                                        requireContext()
                                    )
                                    val staffAttendanceRequest = StaffAttendanceRequest(
                                        eventId = args.eventDetails.eventNumber,
                                        staffId = checkingValue.first?.idNo.toString()
                                    )

                                    println(staffAttendanceRequest)
                                    viewModel.markAttendance(staffAttendanceRequest)
                                    requireContext().showProgressDialog("Marking Attendance", true)

                                } else {
                                    displayDialog(
                                        title = "Verification Failed",
                                        message = "Please try again\n\nscore: ${checkingValue.second}",
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
                        actualFingerprintIv.setImageBitmap(bm1)
                        actualFingerprintIv.isVisible = true
                        ImageUtils.ConvertBitmap2BMP(bm1)
                    }

                    Constants.FPM_TIMEOUT -> requireContext().makeToast("Time Out")
                }
            }
        }
    }

    private fun fakeMarkAttendance() {
        val staffAttendanceRequest = StaffAttendanceRequest(
            eventId = args.eventDetails.eventNumber,
            staffId = "PSS100"
        )

        println(staffAttendanceRequest)
        viewModel.markAttendance(staffAttendanceRequest)
        requireContext().showProgressDialog("Marking Attendance", true)


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
        if (Build.MODEL == "FP07" || Build.MODEL == "FP09") {
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