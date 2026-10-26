package com.tapbi.spark.voiceai.utils

object VoiceUtils {

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
