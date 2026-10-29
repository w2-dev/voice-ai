package com.tapbi.spark.voiceai.utils

import android.media.AudioRecord
import android.media.MediaRecorder
import timber.log.Timber

object VoiceUtils {

    /**
     * So sánh 2 khẩu lệnh văn bản nghiêm ngặt:
     * - Trùng khớp hoàn toàn (sau khi chuẩn hóa hoa/thường & khoảng trắng)
     * - HOẶC cùng số lượng từ và có độ tương đồng Levenshtein >= 75%
     */
    fun isStrictTextMatch(s1: String, s2: String, threshold: Double = 0.75): Boolean {
        val c1 = cleanPassphraseText(s1)
        val c2 = cleanPassphraseText(s2)
        if (c1.isEmpty() || c2.isEmpty()) return false

        val t1 = c1.lowercase()
        val t2 = c2.lowercase()
        if (t1 == t2) return true

        val words1 = t1.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val words2 = t2.split("\\s+".toRegex()).filter { it.isNotBlank() }

        // Bắt buộc phải bằng số lượng từ (Ví dụ: "Hello" vs "Hello Hello" có số từ khác nhau -> Loại ngay)
        if (words1.size != words2.size) return false

        val similarity = stringSimilarity(t1, t2)
        return similarity >= threshold
    }

    /**
     * Làm sạch khẩu lệnh văn bản: loại bỏ các ký tự đặc biệt, dấu câu, thẻ <unk>, <sos>, <eos>
     */
    fun cleanPassphraseText(rawText: String): String {
        return rawText
            .replace(Regex("<[^>]*>"), "")
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), "")
            .trim()
    }

    /**
     * Tính năng lượng âm thanh RMS (Root Mean Square) để phát hiện im lặng
     */
    fun calculateRms(audioData: FloatArray): Float {
        if (audioData.isEmpty()) return 0f
        var sum = 0f
        for (sample in audioData) {
            sum += sample * sample
        }
        return kotlin.math.sqrt(sum / audioData.size)
    }

    /**
     * Khởi tạo AudioRecord an toàn với cơ chế Thử lại (Retry 3 lần) & Fallback AudioSource (MIC -> VOICE_RECOGNITION -> DEFAULT)
     * Tránh lỗi SecurityException / AudioFlinger status -20 ("Micro bận") trên Service Overlay.
     */
    fun createAudioRecord(
        sampleRate: Int,
        channelConfig: Int,
        audioFormat: Int,
        minBufferSize: Int
    ): AudioRecord? {
        val bufferSize = maxOf(minBufferSize, 3200)
        val sources = intArrayOf(
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.DEFAULT
        )

        for (attempt in 1..3) {
            for (source in sources) {
                try {
                    val recorder = AudioRecord(source, sampleRate, channelConfig, audioFormat, bufferSize)
                    if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                        Timber.d("AudioRecord initialized successfully with audio source $source (attempt $attempt)")
                        return recorder
                    } else {
                        recorder.release()
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to initialize AudioRecord with source $source (attempt $attempt)")
                }
            }
            try {
                Thread.sleep(100L)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return null
    }

    /**
     * Tính trung bình cộng của danh sách các Vector Speaker Embedding (96d)
     */
    fun averageEmbeddings(embeddings: List<FloatArray>): FloatArray {
        if (embeddings.isEmpty()) return FloatArray(0)
        val size = embeddings[0].size
        val result = FloatArray(size)
        for (i in 0 until size) {
            var sum = 0f
            for (emb in embeddings) {
                if (i < emb.size) sum += emb[i]
            }
            result[i] = sum / embeddings.size
        }
        return result
    }
}
