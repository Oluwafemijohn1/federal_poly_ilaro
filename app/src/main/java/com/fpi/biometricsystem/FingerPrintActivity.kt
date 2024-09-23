package com.fpi.biometricsystem

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.hibory.CommonApi
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.fgtit.data.ConversionsEx
import com.fgtit.data.wsq
import com.fgtit.device.Constants
import com.fgtit.device.FPModule
import com.fgtit.device.ImageUtils
import com.fgtit.fpcore.FPMatch
import com.fpi.biometricsystem.databinding.ActivityFingerprintBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.RandomAccessFile

@AndroidEntryPoint
class FingerPrintActivity : AppCompatActivity() {
    private val sDir = Environment.getExternalStorageDirectory().toString() + "/MidX"
    private val fpm = FPModule()
    private val bmpdata = ByteArray(Constants.RESBMP_SIZE)
    private var bmpsize = 0
    private val refdata = ByteArray(Constants.TEMPLATESIZE * 2)
    private var refsize = 0
    private val matdata = ByteArray(Constants.TEMPLATESIZE * 2)
    private var matsize = 0
    private var refstring = ""
    private var matstring = ""
    private var worktype = 0
    private lateinit var mToolbar: Toolbar
    private var commonApi: CommonApi? = null
    private lateinit var mtsList: List<ByteArray>
    private lateinit var tvFpData: TextView
    private lateinit var tvFpType: TextView
    private lateinit var activityFingerprintBinding: ActivityFingerprintBinding
    private val threshold = 50
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFingerprintBinding = ActivityFingerprintBinding.inflate(layoutInflater)
        val view = activityFingerprintBinding.root
        setContentView(view)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog()
                .build()
        )
        initView()
        val i = fpm.InitMatch()
        Log.d("MainActivity", "i:$i")
        fpm.SetContextHandler(this, mHandler)
        fpm.SetTimeOut(Constants.TIMEOUT_LONG);
        fpm.SetLastCheckLift(true);
        fpm.SetTimeOut(8)
        fpm.SetLastCheckLift(false)
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

    private fun showMatchResultDialog(result: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Match Result")
        builder.setMessage(result)
        builder.setCancelable(false)
        builder.setNegativeButton("ok") { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun matchTemplateFromFile(matstring: String) {
        val tf = File("$sDir/template.txt")
        if (!tf.exists()) {
            return
        }
        try {
            FileReader(tf).use { fr ->
                val chars = CharArray(tf.length().toInt())
                fr.read(chars)

                val fileContent = chars.toString()
                val strs = fileContent.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (strs.isNotEmpty()) {
                    val result = StringBuffer()
                    for (i in strs.indices) {
                        val sc = matchIsoTemplateStr(strs[i], matstring)
                        Log.d("hello", "MatchIsoTemplateStr score: $sc, index: $i")
                        if (sc >= threshold) {
                            result.append("index: $i   Match Successful! score: $sc\n")
                        } else {
                            result.append("index: $i   Match Failed. score: $sc\n")
                        }
                    }
                    showMatchResultDialog(result.toString())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveTemplateToFile(template: String?) {
        if (template.isNullOrEmpty()) return
        val destDir = File(sDir)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        try {
            val fw = FileWriter("$sDir/template.txt", true)
            fw.write(
                """
    $template
    
    """.trimIndent()
            )
            fw.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            activityFingerprintBinding.apply {
                when (msg.what) {
                    Constants.FPM_DEVICE -> when (msg.arg1) {
                        Constants.DEV_OK -> {
                            tvFpStatus.text = "Open Device OK"

                            //FP08 && android 11 设置方向
                            if (Build.MODEL == "FP08" && Build.VERSION.RELEASE == "11") {
                                fpm.FPSetDri1()
                            }
                        }

                        Constants.DEV_FAIL -> tvFpStatus.text = "Open Device Fail"
                        Constants.DEV_ATTACHED -> tvFpStatus.text = "USB Device Attached"
                        Constants.DEV_DETACHED -> tvFpStatus.text = "USB Device Detached"
                        Constants.DEV_CLOSE -> tvFpStatus.text = "Device Close"
                    }

                    Constants.FPM_PLACE -> {
                        tvFpStatus.text = "Place Finger"
                        Log.d("hello", "FPM_PLACE")
                    }

                    Constants.FPM_LIFT -> {
                        Log.d("hello", "FPM_LIFT")
                        tvFpStatus.text = "Lift Finger"
                    }

                    Constants.FPM_GENCHAR -> {

                        //ProgressDialogUtils.dismissProgressDialog();
                        if (msg.arg1 == 1) {
                            if (worktype == 0) {
                                // When comparing from saved fingerprint
                                tvFpStatus.text = "Generate Template OK"
                                matsize = fpm.GetTemplateByGen(matdata)
                                matstring = Base64.encodeToString(matdata, 0)
                                tvFpData.text = matstring
                                val sc = matchIsoTemplateStr(refstring, matstring)
                                tvFpStatus.text = "Match Result:$sc/" + FPMatch.getInstance()
                                    .MatchTemplate(refdata, matdata).toString()
                                matchTemplateFromFile(matstring)
                            } else {
                                tvFpStatus.text = "Enrol Template OK"
                                refsize = fpm.GetTemplateByGen(refdata)
                                tvFpType.text =
                                    "raw FP template type: " + ConversionsEx.getInstance()
                                        .GetDataType(refdata).toString()
                                refstring = Base64.encodeToString(refdata, 0)
                                saveTemplateToFile(refstring)
                                tvFpData.text = refstring
                            }
                        } else {
                            tvFpStatus.text = "Generate Template Fail"
                        }
                    }

                    Constants.FPM_NEWIMAGE -> {
                        bmpsize = fpm.GetBmpImage(bmpdata)
                        val bm1 = BitmapFactory.decodeByteArray(bmpdata, 0, bmpsize)
                        ivFpImage.setImageBitmap(bm1)
                        ImageUtils.ConvertBitmap2BMP(bm1)
                    }

                    Constants.FPM_TIMEOUT -> tvFpStatus.text = "Time Out"
                }
            }
        }
    }

    fun MatchIsoTemplateByte(piFeatureA: ByteArray?, piFeatureB: ByteArray?): Int {
        val adat = ByteArray(512)
        val bdat = ByteArray(512)
        val sc = 0

        ConversionsEx.getInstance()
            .AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ISO_19794_2005)
        ConversionsEx.getInstance()
            .AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ISO_19794_2005)
        return FPMatch.getInstance().MatchTemplate(adat, bdat)

//        when (radioGroup!!.checkedRadioButtonId) {
//            R.id.radio1 -> {
//                ConversionsEx.getInstance()
//                    .AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ANSI_378_2004)
//                ConversionsEx.getInstance()
//                    .AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ANSI_378_2004)
//                return FPMatch.getInstance().MatchTemplate(adat, bdat)
//            }
//
//            R.id.radio2 -> {
//                ConversionsEx.getInstance()
//                    .AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ISO_19794_2005)
//                ConversionsEx.getInstance()
//                    .AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ISO_19794_2005)
//                return FPMatch.getInstance().MatchTemplate(adat, bdat)
//            }
//
//            R.id.radio3 ->                 //如果硬件直接设置为ISO模板，则指纹模块直接返回ISO数据，需要将其转换成私有模板
//                /*ConversionsEx.getInstance().AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ISO_19794_2005);
//				ConversionsEx.getInstance().AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ISO_19794_2005);
//				return FPMatch.getInstance().MatchTemplate(adat, bdat);
//				*/
//                //硬件设置为私有，指纹返回私有格式，直接传入比对函数
//                return FPMatch.getInstance().MatchTemplate(piFeatureA, piFeatureB)
//        }
//        return 0
    }

    fun matchIsoTemplateStr(strFeatureA: String?, strFeatureB: String?): Int {
        val piFeatureA = Base64.decode(strFeatureA, Base64.DEFAULT)
        val piFeatureB = Base64.decode(strFeatureB, Base64.DEFAULT)
        val score = MatchIsoTemplateByte(piFeatureA, piFeatureB)  // Get match score

        // Check if the score meets the required match threshold
        if (score >= threshold) {
            Log.d("Fingerprint", "Match Successful with score: $score")
        } else {
            Log.d("Fingerprint", "Match Failed with score: $score")
        }

        return score  // You can return the score or handle the result as needed
    }

    override fun onDestroy() {
        super.onDestroy()
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

    fun SaveWsqFile(rawdata: ByteArray?, rawsize: Int, filename: String) {
        val outdata = ByteArray(73728)
        val outsize = IntArray(1)
        wsq.getInstance().RawToWsq(rawdata, rawsize, 256, 288, outdata, outsize, 2.833755f)
        try {
            val fs = File("/sdcard/$filename")
            if (fs.exists()) {
                fs.delete()
            }
            File("/sdcard/$filename")
            val randomFile = RandomAccessFile("/sdcard/$filename", "rw")
            val fileLength = randomFile.length()
            randomFile.seek(fileLength)
            randomFile.write(outdata, 0, outsize[0])
            randomFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun initView() {
        with(activityFingerprintBinding) {
            mToolbar = toolbar
            tvFpData = textView3
            tvFpType = textView4
            tvDevStatus.text = fpm.deviceType.toString()
            btnEnrol.setOnClickListener {
                if (fpm.GenerateTemplate(2)) {
                    worktype = 1
                } else {
                    Toast.makeText(this@FingerPrintActivity, "Busy", Toast.LENGTH_SHORT).show()
                }
            }
            btnCapture.setOnClickListener {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 0
                } else {
                    Toast.makeText(this@FingerPrintActivity, "Busy", Toast.LENGTH_SHORT).show()
                }
            }
            mToolbar.setNavigationOnClickListener { finish() }
        }
    }

    companion object {
        var GPIO_FINGER_POWER = 140
    }
}