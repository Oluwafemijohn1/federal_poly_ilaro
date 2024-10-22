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
//    private const val baseUrl = Constants.BASE_URL
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
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): FpibService =
        retrofit.create(FpibService::class.java)



    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create();

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson, sharedPreferences: SharedPreferences): Retrofit {
        var baseUrl = Constants.BASE_URL
        baseUrl = sharedPreferences.getString(PrefKey.BASE_URL, null)
            ?.let {
                Log.d("TAG", "provideRetrofit getHeaderInterceptor let: $it")
                if (it == Constants.LOCAL) "http://192.168.7.181:8080/api/" else Constants.BASE_URL
            }
            ?: kotlin.run {
                Log.d("TAG", "provideRetrofit getHeaderInterceptor run")
                baseUrl
            }
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS) // Increase the timeout for connection
            .readTimeout(120, TimeUnit.SECONDS) // Increase the timeout for reading response
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .addNetworkInterceptor(getHeaderInterceptor())
            .build()

    private fun getHeaderInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request =
                chain.request().newBuilder()
                    .header("accesskey", "YTpWVb573X2vtgkbC7RsCo8DGfYgiR")
                    .header("content-type", "application/json")
                    .header("accept", "application/json")
                    .build()
            chain.proceed(request)
        }
    }

}