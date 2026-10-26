package com.tapbi.spark.voiceai.ui.main.biometric

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricActivity : FragmentActivity() {

    companion object {
        const val ACTION_BIOMETRIC_UNLOCK = "com.tapbi.spark.voiceai.ACTION_BIOMETRIC_UNLOCK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "Chưa cài đặt vân tay/khuôn mặt. Vui lòng cài đặt trước!", Toast.LENGTH_LONG).show()
                val enrollIntent = if (Build.VERSION.SDK_INT >= 30) {
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
                finish()
            }
            else -> {
                Toast.makeText(this, "Không hỗ trợ sinh trắc học trên thiết bị này.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val intent = Intent(ACTION_BIOMETRIC_UNLOCK).apply {
                        setPackage(packageName)
                    }
                    sendBroadcast(intent)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Vân tay không khớp!", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mở khóa bằng sinh trắc học")
            .setSubtitle("Xác thực vân tay hoặc khuôn mặt để truy cập")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
