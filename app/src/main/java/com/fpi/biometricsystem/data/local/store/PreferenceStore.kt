package com.fpi.biometricsystem.data.local.store

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceStore @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) {

    internal companion object {
        const val SCHEMA_VERSION = 0
    }

    init {
        if (sharedPreferences.getInt(PrefKey.SCHEMA_VERSION, 0) != SCHEMA_VERSION) {
            with(sharedPreferences.edit()) {
                clear()
                putInt(PrefKey.SCHEMA_VERSION, SCHEMA_VERSION)
            }
        }
    }

    var lastVisitedPageStudents: Int = sharedPreferences.getInt(PrefKey.LAST_VISITED_PAGE_STUDENTS, 1) ?: 1
        set(value) {
            field = value
            sharedPreferences.edit().putInt(PrefKey.LAST_VISITED_PAGE_STUDENTS, value).apply()
        }

    var lastVisitedPageStaffs: Int = sharedPreferences.getInt(PrefKey.LAST_VISITED_PAGE_STAFFS, 1) ?: 1
        set(value) {
            field = value
            sharedPreferences.edit().putInt(PrefKey.LAST_VISITED_PAGE_STAFFS, value).apply()
        }

    var baseUrl: String?
        get() = sharedPreferences.getString(PrefKey.BASE_URL, null)
        set(value) {
            sharedPreferences.edit().putString(PrefKey.BASE_URL, value).apply()
        }

    fun clearUserData() {
        lastVisitedPageStudents = 1
        lastVisitedPageStaffs = 1
    }
}

internal object PrefKey {
    const val LAST_VISITED_PAGE_STUDENTS = "last_visited_page_students"
    const val LAST_VISITED_PAGE_STAFFS = "last_visited_page_staffs"
    const val SCHEMA_VERSION = "schema_version"
    const val BASE_URL = "base_url"
}
