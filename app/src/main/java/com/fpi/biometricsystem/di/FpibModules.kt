package com.fpi.biometricsystem.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.fpi.biometricsystem.FpibApplication
import com.fpi.biometricsystem.data.local.EventDao
import com.fpi.biometricsystem.data.local.FpibDatabase
import com.fpi.biometricsystem.data.local.StaffDao
import com.fpi.biometricsystem.data.local.StudentDao
import com.fpi.biometricsystem.data.local.store.PrefKey
import com.fpi.biometricsystem.data.remote.FpibService
import com.fpi.biometricsystem.utils.Constants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FpibModules {
    private const val baseUrl = Constants.BASE_URL
    private const val DATA_STORE = Constants.FPIB_STORE


    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): FpibDatabase {
        return FpibDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun providePreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(DATA_STORE, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesApplication(@ApplicationContext context: Context): FpibApplication {
        return context as FpibApplication
    }

    @Provides
    @Singleton
    fun providesStaffDao(database: FpibDatabase): StaffDao {
        return database.staffDao()
    }

    @Provides
    @Singleton
    fun providesStudentDao(database: FpibDatabase): StudentDao {
        return database.studentDao()
    }

    @Provides
    @Singleton
    fun providesEventsDao(database: FpibDatabase): EventDao {
        return database.eventDao()
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
        sharedPreferences: SharedPreferences
    ): Retrofit  {
//        var url = sharedPreferences.getString(PrefKey.BASE_URL, null)
//        url = url?.let { if (it.endsWith("/")) it else "$it/" } ?: baseUrl
//        Log.d("TAG", "provideRetrofit: url: $url")
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(baseUrl)  // Ensure non-null
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create();
    @Singleton
    @Provides
    fun providesOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        sharedPreferences: SharedPreferences
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS) // Increase the timeout for connection
            .readTimeout(120, TimeUnit.SECONDS) // Increase the timeout for reading response
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .addNetworkInterceptor(getHeaderInterceptor(sharedPreferences))
            .build()


    @Singleton
    @Provides
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): FpibService =
        retrofit.create(FpibService::class.java)


    private fun getHeaderInterceptor(sharedPreferences: SharedPreferences): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val originalHttpUrl = originalRequest.url

            // Build the initial request with headers
            val requestBuilder = originalRequest.newBuilder()
                .header("accesskey", "YTpWVb573X2vtgkbC7RsCo8DGfYgiR")
                .header("content-type", "application/json")
                .header("accept", "application/json")
            // Create a new URL only if needed and ensure it retains the same host and port
            val newHttpUrl = if (!originalHttpUrl.encodedPath.contains("settings/base_url")) {
                // If the path does NOT contain "settings/base_url", change the base URL
                val newBaseUrl = sharedPreferences.getString(PrefKey.BASE_URL, null)
                    ?.let { if (it.endsWith("/")) it else "$it/" }
                    ?: baseUrl // Fallback to the default base URL

                // Update the original request URL to the new base URL while keeping the same path and query
                updateBaseUrl(originalHttpUrl, newBaseUrl)
            } else {
                // If the path contains "settings/base_url", retain the original URL
                originalHttpUrl
            }

            // Proceed with the new request
            val newRequest = requestBuilder.url(newHttpUrl!!).build()
            Log.d("API_CALL", "Modified request URL: ${newRequest.url}")
            return@Interceptor chain.proceed(newRequest)
        }
    }

    // Function to update the base URL
    private fun updateBaseUrl(originalHttpUrl: HttpUrl, baseUrl: String): HttpUrl? {
        val parsedNewBaseUrl = baseUrl.toHttpUrlOrNull()
        return parsedNewBaseUrl?.let { newBaseUrl ->
            originalHttpUrl.newBuilder()
                .scheme(newBaseUrl.scheme)  // Update scheme (http/https)
                .host(newBaseUrl.host)      // Update host
                .port(newBaseUrl.port)      // Update port
                .build()
        }
    }

//    private fun getHeaderInterceptor(sharedPreferences: SharedPreferences): Interceptor {
//        return Interceptor { chain ->
//            val request =
//                chain.request().newBuilder()
//                    .header("accesskey", "YTpWVb573X2vtgkbC7RsCo8DGfYgiR")
//                    .header("content-type", "application/json")
//                    .header("accept", "application/json")
//                    .build()
//            chain.proceed(request)
//        }
//    }

}