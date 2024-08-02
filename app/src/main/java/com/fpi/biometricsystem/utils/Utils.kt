package com.fpi.biometricsystem.utils

import android.content.Context
import android.os.Environment
import android.util.Base64
import android.util.Log
import com.fgtit.fpcore.FPMatch
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

val sDir = Environment.getExternalStorageDirectory().toString() + "/FPIB"

fun saveTemplateToFile(template: String?) {
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

fun MatchIsoTemplateByte(piFeatureA: ByteArray?, piFeatureB: ByteArray?): Int {
    return FPMatch.getInstance().MatchTemplate(piFeatureA, piFeatureB)
}

fun MatchIsoTemplateStr(strFeatureA: String?, strFeatureB: String?): Int {
    val piFeatureA = Base64.decode(strFeatureA, Base64.DEFAULT)
    val piFeatureB = Base64.decode(strFeatureB, Base64.DEFAULT)
    return MatchIsoTemplateByte(piFeatureA, piFeatureB)
}

fun matchTemplateFromFile(matstring: String, context: Context) {
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
                    val sc = MatchIsoTemplateStr(strs[i], matstring)
                    Log.d("hello", "MatchIsoTemplateStr sc:" + sc + "index:" + i)
                    result.append("score: $sc\n")
                }
                showMatchResultDialog(result.toString(), context)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun compareTemplateFromFile(
    currentFingerprint: String,
    savedFingerPrint: String,
) {
    val strs = currentFingerprint.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }
        .toTypedArray()
    if (strs.isNotEmpty()) {
        val result = StringBuffer()
        for (i in strs.indices) {
            val sc = MatchIsoTemplateStr(strs[i], savedFingerPrint)
            Log.d("hello", "MatchIsoTemplateStr sc:" + sc + "index:" + i)
            result.append("score: $sc\n")
        }
//        showMatchResultDialog(result.toString(), context)
    }
}

fun String?.replaceIfEmpty(replaceWith: String): String {
    return if (this.isNullOrEmpty()) replaceWith else this
}

fun String.sentenceCase(): String {
    return this.split(" ").map { it.replaceFirstChar { char -> char.uppercaseChar() } }
        .joinToString(" ").trim()
}

fun Int.getLevel() : String {
    return when (this) {
        1 -> "ND I"
        2 -> "ND II"
        3 -> "PND I"
        4 -> "PND II"
        5 -> "HND I"
        6 -> "HND II"
        7 -> "ND III"
        else -> ""
    }
}