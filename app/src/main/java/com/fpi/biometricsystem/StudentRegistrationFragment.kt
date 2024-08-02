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
import com.fpi.biometricsystem.data.local.models.StudentInfo
import com.fpi.biometricsystem.data.local.models.toStudentInfo
import com.fpi.biometricsystem.data.request.Biometric
import com.fpi.biometricsystem.data.request.StudentRegistrationRequest
import com.fpi.biometricsystem.databinding.StudentRegistrationFragmentBinding
import com.fpi.biometricsystem.utils.EventObserver
import com.fpi.biometricsystem.utils.hideKeyboard
import com.fpi.biometricsystem.utils.makeToast
import com.fpi.biometricsystem.utils.replaceIfEmpty
import com.fpi.biometricsystem.utils.sentenceCase
import com.fpi.biometricsystem.utils.showErrorDialog
import com.fpi.biometricsystem.utils.showProgressDialog
import com.fpi.biometricsystem.utils.showRegistrationDialog
import com.fpi.biometricsystem.viewmodels.StudentRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StudentRegistrationFragment : Fragment() {
    private lateinit var binding: StudentRegistrationFragmentBinding
    private val fpm = FPModule()
    private var worktype = 0
    private var refStrings = arrayListOf("", "", "", "", "")
    private var arrayBmpSizes = arrayListOf(0, 0, 0, 0, 0)
    private var arrayRefSizes = arrayListOf(0, 0, 0, 0, 0)
    private var bmpDataArray =
        arrayListOf(
            ByteArray(Constants.RESBMP_SIZE),
            ByteArray(Constants.RESBMP_SIZE),
            ByteArray(Constants.RESBMP_SIZE),
            ByteArray(Constants.RESBMP_SIZE),
            ByteArray(Constants.RESBMP_SIZE)
        )
    private var commonApi: CommonApi? = null

    private val viewModel: StudentRegistrationViewModel by activityViewModels()
    private var finalStudentInfo: StudentInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StudentRegistrationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initFPM()
        observeResponse()
    }

    private fun observeResponse() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.messageResponseLiveData.observe(viewLifecycleOwner, EventObserver {
                        requireContext().showProgressDialog(on = false)
                        if (it.messageType == MessageType.ERROR) {
                            requireContext().makeToast(it.message)
                        } else {
                            finalStudentInfo?.let { info ->
                                viewModel.updateStudentInfo(info)
                            }
                            showRegistrationDialog(
                                "Successfully saved to database",
                                requireContext()
                            ) { findNavController().popBackStack() }
                        }
                    })
                }

                launch {
                    viewModel.finalResponse.observe(viewLifecycleOwner, EventObserver {
                        requireContext().showProgressDialog(on = false)
                        val studentInfo = it!!.actualData
                        finalStudentInfo = studentInfo.toStudentInfo()
                        binding.apply {
                            firstNameEt.setText(studentInfo.firstname)
                            lastNameEt.setText(studentInfo.lastname)
                            departmentEt.setText(studentInfo.department.dept)
                            levelEt.setText(studentInfo.level.level)
                            fingerPrintViews.isVisible = true
                            enrolBtn.isVisible = false
                        }
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

    private fun initFPM() {
        fpm.SetContextHandler(requireContext(), mHandler)
        fpm.SetTimeOut(Constants.TIMEOUT_LONG);
        fpm.SetLastCheckLift(true);
        fpm.SetTimeOut(20)
        fpm.SetLastCheckLift(false)
    }

    private fun initViews() {
        with(binding) {
            arrowBackBtn.setOnClickListener { findNavController().popBackStack() }

            captureBtn.setOnClickListener {
                val studentMatricNo = staffNoEt.text
                if (
                    studentMatricNo.toString().isEmpty()
                    || refStrings.filter { it.isEmpty() }.size > 2
                ) {
                    requireContext().makeToast("Please fill all fields")
                    return@setOnClickListener
                } else {
                    val bioList =
                        refStrings.map { refStr -> Biometric(data = refStr, type = "thumb") }
                    val studentRegReq = StudentRegistrationRequest(
                        biometric = bioList,
                        matricnumber = studentMatricNo.toString()
                    )
                    finalStudentInfo = finalStudentInfo?.copy(fingerprint = refStrings)
                    viewModel.createUser(studentRegReq)
                    requireContext().showProgressDialog("Saving...", on = true)
                }
            }

            enrolBtn.setOnClickListener {
                hideKeyboard()
                val staffNo = staffNoEt.text
                if (staffNo.isEmpty()
                ) {
                    requireContext().makeToast("Please fill all fields")
                } else {
                    viewModel.fetchStudentById(staffNoEt.text.toString().trim())
                    requireContext().showProgressDialog("Loading...", on = true)
                }
            }

            actualRightFingerprintIv.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 2
                } else {
                    requireContext().makeToast("Busy")
                }
            }

            actualLeftFingerprintIv.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 1
                } else {
                    requireContext().makeToast("Busy")
                }
            }

            thirdFingerprintIv.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 3
                } else {
                    requireContext().makeToast("Busy")
                }
            }

            fourthFingerprintIv.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 4
                } else {
                    requireContext().makeToast("Busy")
                }
            }

            fifthFingerprintIv.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 5
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
                            tvFpStatus.text = "Open Device OK"

                            //FP08 && android 11 设置方向
                            if (Build.MODEL == "FP08" && Build.VERSION.RELEASE == "11") {
                                fpm.FPSetDri1() // Default
                            }
                        }

                        Constants.DEV_FAIL -> tvFpStatus.text = "Open Device Fail"
                        Constants.DEV_ATTACHED -> tvFpStatus.text = "USB Device Attached"
                        Constants.DEV_DETACHED -> tvFpStatus.text = "USB Device Detached"
                        Constants.DEV_CLOSE -> tvFpStatus.text = "Device Close"
                    }

                    Constants.FPM_PLACE -> {
                        requireContext().makeToast("Please place your finger")
                    }

                    Constants.FPM_LIFT -> {}

                    Constants.FPM_GENCHAR -> {
                        if (msg.arg1 == 1) {
                            val arrayPos = worktype - 1
                            arrayRefSizes[arrayPos] = fpm.GetTemplateByGen(bmpDataArray[arrayPos])
                            refStrings[arrayPos] = Base64.encodeToString(bmpDataArray[arrayPos], 0)
                        } else {
                            tvFpStatus.text = "Generate Template Fail"
                        }
                    }

                    Constants.FPM_NEWIMAGE -> {
                        val arrayPos = worktype - 1
                        arrayBmpSizes[arrayPos] = fpm.GetBmpImage(bmpDataArray[arrayPos])
                        val bm = BitmapFactory.decodeByteArray(
                            bmpDataArray[arrayPos],
                            0,
                            arrayBmpSizes[arrayPos]
                        )
                        when (worktype) {
                            1 -> {
                                actualLeftFingerprintIv.setImageBitmap(bm)
                                actualLeftFingerprintIv.isVisible = true
                            }

                            2 -> {
                                actualRightFingerprintIv.setImageBitmap(bm)
                                actualRightFingerprintIv.isVisible = true
                            }

                            3 -> {
                                thirdFingerprintIv.setImageBitmap(bm)
                                thirdFingerprintIv.isVisible = true
                            }

                            4 -> {
                                fourthFingerprintIv.setImageBitmap(bm)
                                fourthFingerprintIv.isVisible = true
                            }

                            5 -> {
                                fifthFingerprintIv.setImageBitmap(bm)
                                fifthFingerprintIv.isVisible = true

                            }
                        }
                        ImageUtils.ConvertBitmap2BMP(bm)
                    }

                    Constants.FPM_TIMEOUT -> tvFpStatus.text = "Time Out"
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