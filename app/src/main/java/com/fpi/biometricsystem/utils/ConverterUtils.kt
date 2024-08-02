package com.fpi.biometricsystem.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

inline fun <reified T> typeToken(): Type = object : TypeToken<T>() {}.type

inline fun <reified T> String.toObject(): T {
    val type = typeToken<T>()
    return Gson().fromJson(this, type)
}