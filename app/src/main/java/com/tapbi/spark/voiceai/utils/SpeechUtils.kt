package com.tapbi.spark.voiceai.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.math.sqrt

// DTW and Cosine Distance implementation requested by user
// Tính khoảng cách cô-sin (Cosine Distance) giữa 2 vector đặc trưng 96 chiều.
// Công thức: 1 - Cosine Similarity. Khoảng cách càng nhỏ (gần 0) nghĩa là 2 vector đặc trưng càng giống nhau.
fun cosineDistance(bVar: FloatArray, bVar2: FloatArray): Float {
    var f12 = 0.0f
    var norm1 = 0.0f
    var norm2 = 0.0f
    val len = minOf(bVar.size, bVar2.size)
    for (i12 in 0 until len) {
        f12 += bVar[i12] * bVar2[i12]
        norm1 += bVar[i12] * bVar[i12]
        norm2 += bVar2[i12] * bVar2[i12]
    }
    val f10 = sqrt(norm1)
    val f11 = sqrt(norm2)
    if (f10 == 0.0f || f11 == 0.0f) return 1.0f
    val cosineSimilarity = f12 / (f10 * f11)
    return 1.0f - cosineSimilarity
}

suspend fun recognizeSpeech(context: Context, languageCode: String = "vi-VN"): String =
    suspendCancellableCoroutine { continuation ->
        val mainHandler = Handler(context.mainLooper)
        mainHandler.post {
            try {
                Timber.tag("SpeechRecognizerDebug").d("Creating SpeechRecognizer...")
                val speechRecognizer =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(
                            context
                        )
                    ) {
                        Timber.d("Using on-device SpeechRecognizer")
                        SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                    } else {
                        Timber.d("Using standard SpeechRecognizer")
                        SpeechRecognizer.createSpeechRecognizer(context)
                    }

                var isFinished = false

                // Thiết lập timeout tối đa 5 giây cho SpeechRecognizer
                val timeoutRunnable = Runnable {
                    if (!isFinished) {
                        isFinished = true
                        Timber.tag("SpeechRecognizerDebug")
                            .w("Timeout triggered (5s reached)! Cancelling SpeechRecognizer...")
                        try {
                            speechRecognizer.cancel()
                        } catch (e: Exception) {
                            Timber.tag("SpeechRecognizerDebug").e(e, "Error cancelling on timeout")
                        }
                        speechRecognizer.destroy()
                        if (continuation.isActive) {
                            continuation.resume("Hết thời gian (Quá 5 giây không có kết quả)")
                        }
                    }
                }
                mainHandler.postDelayed(timeoutRunnable, 10000L)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    // 1. EXTRA_LANGUAGE_MODEL: Mô hình nhận diện tự do (free_form)
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    // 2. EXTRA_LANGUAGE: Ngôn ngữ chính nhận diện
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    // 3. EXTRA_LANGUAGE_PREFERENCE: Ngôn ngữ ưu tiên
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                    // 4. EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE: Chỉ định trả về theo ngôn ngữ ưu tiên
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
                    // 5. EXTRA_MAX_RESULTS: Giới hạn số kết quả trả về tối đa
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                }

                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Timber.tag("SpeechRecognizerDebug")
                            .d("onReadyForSpeech: Sẵn sàng nhận giọng nói")
                    }

                    override fun onBeginningOfSpeech() {
                        Timber.tag("SpeechRecognizerDebug")
                            .d("onBeginningOfSpeech: Người dùng đã bắt đầu nói")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Log.v("SpeechRecognizerDebug", "onRmsChanged: rmsdB = $rmsdB")
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        Timber.tag("SpeechRecognizerDebug").d("onBufferReceived: Nhận audio buffer")
                    }

                    override fun onEndOfSpeech() {
                        Timber.d("onEndOfSpeech: Người dùng đã dừng nói")
                    }

                    override fun onError(error: Int) {
                        if (!isFinished) {
                            isFinished = true
                            mainHandler.removeCallbacks(timeoutRunnable)
                            speechRecognizer.destroy()
                            val errorMsg = when (error) {
                                SpeechRecognizer.ERROR_NETWORK -> "Lỗi mạng (Network error)"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout mạng"
                                SpeechRecognizer.ERROR_AUDIO -> "Lỗi audio (Audio recording error)"
                                SpeechRecognizer.ERROR_SERVER -> "Lỗi server"
                                SpeechRecognizer.ERROR_CLIENT -> "Lỗi client"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Hết thời gian (Không phát hiện giọng nói)"
                                SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận diện được từ (No match)"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer đang bận"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Không đủ quyền"
                                else -> "Lỗi nhận diện không xác định"
                            }
                            Timber.e("onError: code = $error -> $errorMsg")
                            if (continuation.isActive) {
                                continuation.resume("$errorMsg (code: $error)")
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        if (!isFinished) {
                            isFinished = true
                            mainHandler.removeCallbacks(timeoutRunnable)
                            val matches =
                                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            Timber.d("onResults: matches = $matches")
                            speechRecognizer.destroy()
                            if (continuation.isActive) {
                                if (!matches.isNullOrEmpty()) {
                                    continuation.resume(matches[0])
                                } else {
                                    continuation.resume("Không nhận diện được từ")
                                }
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial =
                            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        Timber.d("onPartialResults: $partial")
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {
                        Timber.d("onEvent: eventType = $eventType")
                    }
                })

                Timber.d("Calling startListening...")
                speechRecognizer.startListening(intent)
            } catch (e: Exception) {
                Timber.e(e, "Exception in recognizeSpeech")
                if (continuation.isActive) {
                    continuation.resume("Lỗi SpeechRecognizer: ${e.localizedMessage}")
                }
            }
        }
    }

fun stringSimilarity(s1: String, s2: String): Double {
    val len1 = s1.length
    val len2 = s2.length
    val dp = Array(len1 + 1) { IntArray(len2 + 1) }

    for (i in 0..len1) dp[i][0] = i
    for (j in 0..len2) dp[0][j] = j

    for (i in 1..len1) {
        for (j in 1..len2) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            dp[i][j] = minOf(
                dp[i - 1][j] + 1,
                dp[i][j - 1] + 1,
                dp[i - 1][j - 1] + cost
            )
        }
    }
    val maxLen = maxOf(len1, len2)
    if (maxLen == 0) return 1.0
    return 1.0 - dp[len1][len2].toDouble() / maxLen
}
