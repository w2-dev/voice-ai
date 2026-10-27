package com.tapbi.spark.voiceai.utils

import android.media.AudioRecord
import android.media.MediaRecorder
import timber.log.Timber

object VoiceUtils {

    /**
     * Khởi tạo AudioRecord an toàn với cơ chế Fallback AudioSource (MIC -> VOICE_RECOGNITION -> DEFAULT)
     * Tránh lỗi SecurityException / AudioFlinger status -20 trên Service Overlay.
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
        for (source in sources) {
            try {
                val recorder = AudioRecord(source, sampleRate, channelConfig, audioFormat, bufferSize)
                if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                    Timber.d("AudioRecord initialized successfully with audio source $source")
                    return recorder
                } else {
                    recorder.release()
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to initialize AudioRecord with source $source")
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
