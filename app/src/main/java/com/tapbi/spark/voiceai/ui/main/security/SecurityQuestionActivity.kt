package com.tapbi.spark.voiceai.ui.main.security

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tapbi.spark.ui_common_sdk.foundation.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecurityQuestionActivity : AppCompatActivity() {

    companion object {
        const val ACTION_SECURITY_QUESTION_UNLOCK = "com.tapbi.spark.voiceai.ACTION_SECURITY_QUESTION_UNLOCK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = AppTheme.color.colorBackgroundWindow
                ) { innerPadding ->
                    val viewModel = hiltViewModel<SecurityQuestionViewModel>()
                    SecurityQuestionScreen(
                        viewModel = viewModel,
                        isUnlockMode = true,
                        onSuccessUnlock = {
                            val intent = Intent(ACTION_SECURITY_QUESTION_UNLOCK).apply {
                                setPackage(packageName)
                            }
                            sendBroadcast(intent)
                            finish()
                        },
                        modifier = Modifier
                            .padding(top = innerPadding.calculateTopPadding())
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}
