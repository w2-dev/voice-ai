package com.tapbi.spark.voiceai.ui.main.biometric

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.tapbi.spark.voiceai.common.Constant
import com.tapbi.spark.voiceai.extension.isAtLeastApi27
import com.tapbi.spark.voiceai.extension.isAtLeastApi30

class BiometricActivity : FragmentActivity() {

    private var isUnlockedSuccess = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hiển thị BiometricActivity phía trên Màn hình khóa Overlay và Keyguard hệ thống
        if (isAtLeastApi27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        try {
            sendBroadcast(Intent(Constant.ACTION_BIOMETRIC_STARTED).setPackage(packageName))
        } catch (_: Exception) {}

        val biometricManager = BiometricManager.from(this)
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    this,
                    "Chưa cài đặt vân tay/khuôn mặt. Vui lòng cài đặt trước!",
                    Toast.LENGTH_LONG
                ).show()
                val enrollIntent = if (isAtLeastApi30) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            authenticators
                        )
                    }
                } else {
                    Intent(Settings.ACTION_SECURITY_SETTINGS)
                }
                startActivity(enrollIntent)
                finishActivityAndRemove()
            }

            else -> {
                Toast.makeText(
                    this,
                    "Không hỗ trợ sinh trắc học trên thiết bị này.",
                    Toast.LENGTH_LONG
                ).show()
                finishActivityAndRemove()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finishActivityAndRemove()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isUnlockedSuccess = true
                    // Tránh lỗi giao diện trong suốt treo trên Realme/Oppo/Xiaomi:
                    // Chờ BiometricPrompt đóng animation xong (~200ms) trước khi phát broadcast unlock và finish
                    handler.postDelayed({
                        if (!isFinishing && !isDestroyed) {
                            val intent = Intent(Constant.ACTION_BIOMETRIC_UNLOCK).apply {
                                setPackage(packageName)
                            }
                            sendBroadcast(intent)
                            finishActivityAndRemove()
                        }
                    }, 200L)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Vân tay không khớp!", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mở khóa bằng sinh trắc học")
            .setSubtitle("Xác thực vân tay hoặc khuôn mặt để truy cập")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    @Suppress("DEPRECATION")
    private fun finishActivityAndRemove() {
        finishAndRemoveTask()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (!isUnlockedSuccess) {
            try {
                sendBroadcast(Intent(Constant.ACTION_BIOMETRIC_CANCELLED).setPackage(packageName))
            } catch (_: Exception) {}
        }
    }
}
