package com.fpi.biometricsystem

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.fpi.biometricsystem.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        var delayPeriod = if (Date.now().isAfter())
        Handler().postDelayed(Runnable {
            Intent(this@SplashScreenActivity, MainActivity::class.java).apply {
                startActivity(this)
            }
            finish()
        }, 3000)
    }
}