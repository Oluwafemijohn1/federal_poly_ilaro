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
import com.fpi.biometricsystem.data.local.models.StudentInfo
import com.fpi.biometricsystem.data.request.ExamAttendanceRequest
import com.fpi.biometricsystem.data.request.StudentAttendanceRequest
import com.fpi.biometricsystem.databinding.StudentExaminationAttendanceFragmentBinding
import com.fpi.biometricsystem.utils.EventObserver
import com.fpi.biometricsystem.utils.MatchIsoTemplateStr
import com.fpi.biometricsystem.utils.displayDialog
import com.fpi.biometricsystem.utils.makeToast
import com.fpi.biometricsystem.utils.replaceIfEmpty
import com.fpi.biometricsystem.utils.sentenceCase
import com.fpi.biometricsystem.utils.showErrorDialog
import com.fpi.biometricsystem.utils.showProgressDialog
import com.fpi.biometricsystem.viewmodels.StudentVerificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StudentExaminationAttendanceFragment : Fragment() {
    private lateinit var binding: StudentExaminationAttendanceFragmentBinding
    private val viewModel: StudentVerificationViewModel by activityViewModels()
    private val fpm = FPModule()
    private var worktype = 0
    private var matsize = 0
    private val matdata = ByteArray(Constants.TEMPLATESIZE * 2)
    private var refstring = ""
    private var matstring = ""
    private val bmpdata = ByteArray(Constants.RESBMP_SIZE)
    private var bmpsize = 0
    private var commonApi: CommonApi? = null
    private var allStudentInfo: List<StudentInfo> = emptyList()
    private val args: StudentExaminationAttendanceFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StudentExaminationAttendanceFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initFPM()
//        viewModel.allStudents()
//        requireContext().showProgressDialog("Loading...", true)
        // To be removed in favour of a trigger button to update Students list
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
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allStudents.collect {
                        requireContext().showProgressDialog(on = false)
                        allStudentInfo = it
                        requireContext().makeToast("Ready")
                        binding.apply {
                            checkDetailsBtn.isClickable = true
                            actualFingerprintIv.isClickable = true
                            checkDetailsBtn.isEnabled = true
                        }
                    }
                }

                launch {
                    viewModel.finalResponse.collect {
                        requireContext().showProgressDialog(on = false)
                        displayDialog(
                            title = "Exam Attendance Saved",
                            message = it.message,
                            requireContext()
                        )
                    }
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
            val details = args.exam
            eventTitle.text = "${details.courseName} - ${details.courseCode} \n -${details.examDate}"
            arrowBackBtn.setOnClickListener { findNavController().popBackStack() }
            checkDetailsBtn.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 0
                    requireContext().makeToast("Please place your thumb on the fingerprint panel")
                } else {
                    requireContext().makeToast("Busy")
                }
            }

            actualFingerprintIv.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 0
                    requireContext().makeToast("Please place your thumb on the fingerprint panel")
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

                        else -> {}
                    }

                    Constants.FPM_PLACE -> {
                        if (worktype == 1) {
                            requireContext().makeToast("Please place your left thumb")
                        } else if (worktype == 2) {
                            requireContext().makeToast("Please place your right thumb")
                        }
                    }

                    Constants.FPM_LIFT -> {}

                    Constants.FPM_GENCHAR -> {
                        if (msg.arg1 == 1) {
                            if (worktype == 0) {
                                val scores = arrayListOf<Int>()
                                val matchingScores = arrayListOf<Pair<StudentInfo, Int>>()
                                matsize = fpm.GetTemplateByGen(matdata)
                                matstring = Base64.encodeToString(matdata, 0)

                                allStudentInfo.forEach { studentInfo ->
                                    studentInfo.fingerprint.forEach { fin ->
                                        refstring = fin
                                        val score = MatchIsoTemplateStr(matstring, refstring)
                                        scores.add(score)
                                        matchingScores.add(Pair(studentInfo, score))
//                                        println(score)
                                    }
                                }

                                var checkingValue: Pair<StudentInfo?, Int> = Pair(null, 0)
                                for (item in matchingScores) {
                                    if (item.second > checkingValue.second) {
                                        checkingValue = item
                                    }
                                }
                                println(checkingValue)
                                if (checkingValue.second >= 75) {
                                    val studentExaminationAttendanceRequest = ExamAttendanceRequest(
                                        examNumber = args.exam.examNumber,
                                        studentId = checkingValue.first?.idNo.toString()
                                    )
                                    displayDialog(
                                        title = "Verification Successful",
                                        message = "Name: ${checkingValue.first?.name} \nMatric No: ${checkingValue.first?.matricNo}",
                                        requireContext()
                                    )
                                    viewModel.markExaminationAttendance(
                                        studentExaminationAttendanceRequest
                                    )
                                    requireContext().showProgressDialog("Loading...", true)

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