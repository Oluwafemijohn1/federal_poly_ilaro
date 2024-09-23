package com.fpi.biometricsystem.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment

private var progressDialog: ProgressDialog? = null

fun Context.makeToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showProgressDialog(message: String = "", on: Boolean) {
    when (on) {
        true -> {
            progressDialog = ProgressDialog(this)
            progressDialog?.setTitle("Please wait")
            progressDialog?.setMessage(message)
            progressDialog?.setCanceledOnTouchOutside(false)
            progressDialog?.show()
        }
        false -> progressDialog?.dismiss()
    }
}

fun showErrorDialog(message: String, context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Error")
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setNegativeButton("ok") { dialog, which -> dialog.dismiss() }
    builder.create().show()
}

fun showMatchResultDialog(result: String, context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Match Result")
    builder.setMessage(result)
    builder.setCancelable(false)
    builder.setNegativeButton("ok") { dialog, which -> dialog.dismiss() }
    builder.create().show()
}

fun displayDialog(title: String, message: String, context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setNegativeButton("ok") { dialog, _ -> dialog.dismiss() }
    builder.create().show()
}


fun showRegistrationDialog(result: String, context: Context, function: () -> (Unit)) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Registration Successful")
    builder.setMessage(result)
    builder.setCancelable(false)
    builder.setPositiveButton(
        "OK"
    ) { dialog, id ->
        function()
    }
    builder.create().show()
}

fun Group.setAllOnClickListener(listener: (View) -> Unit) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}
fun View.show(){
    visibility = View.VISIBLE
}

fun View.hide(){
    visibility = View.GONE
}