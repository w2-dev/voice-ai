package com.tapbi.spark.voiceai.utils

import android.content.Context
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ModelManager {

    private const val BUFFER_SIZE = 8192

    // Official Zipformer ONNX Model URLs (.tar.bz2)
    val LANGUAGE_MODEL_URLS = mapOf(
        "vi-VN" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-vi-30M-int8-2026-02-09.tar.bz2",
        "ru-RU" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-ru-int8-2025-04-20.tar.bz2",
        "en-US" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-small-en-2023-06-26.tar.bz2",
        "zh-CN" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-multi-zh-hans-2023-9-2.tar.bz2",
        "ja-JP" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-ja-reazonspeech-2024-08-01.tar.bz2",
        "ko-KR" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-korean-2024-06-24.tar.bz2",
        "th-TH" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-thai-2024-06-20.tar.bz2",
        "yue-HK" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-cantonese-2024-03-13.tar.bz2"
    )

    fun isModelDownloaded(context: Context, languageCode: String): Boolean {
        if (languageCode == "vi-VN" || languageCode == "en-US") return true // Built-in assets fallback
        val modelDir = File(context.filesDir, "models/zipformer_$languageCode")
        if (!modelDir.exists()) return false

        val allFiles = modelDir.walkTopDown().filter { it.isFile }.toList()
        val hasEncoder = allFiles.any { it.name.contains("encoder", ignoreCase = true) && it.name.endsWith(".onnx") }
        val hasDecoder = allFiles.any { it.name.contains("decoder", ignoreCase = true) && it.name.endsWith(".onnx") }
        val hasTokens = allFiles.any { it.name.contains("tokens", ignoreCase = true) }

        Timber.d("isModelDownloaded check for $languageCode in ${modelDir.absolutePath}: filesCount=${allFiles.size}, hasEncoder=$hasEncoder, hasDecoder=$hasDecoder, hasTokens=$hasTokens")

        return hasEncoder && hasDecoder && hasTokens
    }

    fun createRecognizer(context: Context, languageCode: String = "vi-VN"): OfflineRecognizer {
        // 1. Check local downloaded model in filesDir
        val modelDir = File(context.filesDir, "models/zipformer_$languageCode")

        if (modelDir.exists()) {
            val allFiles = modelDir.walkTopDown().filter { it.isFile }.toList()
            val encoderFile = allFiles.firstOrNull { it.name.contains("encoder", ignoreCase = true) && it.name.endsWith(".onnx") }
            val decoderFile = allFiles.firstOrNull { it.name.contains("decoder", ignoreCase = true) && it.name.endsWith(".onnx") }
            val joinerFile = allFiles.firstOrNull { it.name.contains("joiner", ignoreCase = true) && it.name.endsWith(".onnx") }
            val tokensFile = allFiles.firstOrNull { it.name.contains("tokens", ignoreCase = true) }

            if (encoderFile != null && decoderFile != null && joinerFile != null && tokensFile != null) {
                Timber.d("Loading local Zipformer model for $languageCode from ${encoderFile.absolutePath}")
                val config = OfflineRecognizerConfig(
                    featConfig = FeatureConfig(
                        sampleRate = 16000,
                        featureDim = 80
                    ),
                    modelConfig = OfflineModelConfig(
                        transducer = OfflineTransducerModelConfig(
                            encoder = encoderFile.absolutePath,
                            decoder = decoderFile.absolutePath,
                            joiner = joinerFile.absolutePath
                        ),
                        tokens = tokensFile.absolutePath,
                        numThreads = 2,
                        debug = false,
                        provider = "cpu"
                    ),
                    decodingMethod = "greedy_search"
                )
                return OfflineRecognizer(null, config)
            }
        }

        // 2. Default: Load built-in asset model from assets/model/zipformer/
        val assetFolder = "model/zipformer"

        Timber.d("Loading built-in Zipformer asset model from $assetFolder")
        val config = OfflineRecognizerConfig(
            featConfig = FeatureConfig(
                sampleRate = 16000,
                featureDim = 80
            ),
            modelConfig = OfflineModelConfig(
                transducer = OfflineTransducerModelConfig(
                    encoder = "$assetFolder/encoder.int8.onnx",
                    decoder = "$assetFolder/decoder.onnx",
                    joiner = "$assetFolder/joiner.int8.onnx"
                ),
                tokens = "$assetFolder/tokens.txt",
                numThreads = 2,
                debug = false,
                provider = "cpu"
            ),
            decodingMethod = "greedy_search"
        )
        return OfflineRecognizer(context.assets, config)
    }

    private fun getRedirectedUrl(initialUrl: String): String {
        var urlStr = initialUrl
        var redirectCount = 0

        while (redirectCount < 10) {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 20000
            connection.readTimeout = 30000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")

            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                status == HttpURLConnection.HTTP_MOVED_PERM ||
                status == HttpURLConnection.HTTP_SEE_OTHER ||
                status == 307 || status == 308
            ) {
                val newUrl = connection.getHeaderField("Location")
                if (!newUrl.isNullOrBlank()) {
                    urlStr = newUrl
                    redirectCount++
                    connection.disconnect()
                    continue
                }
            }
            connection.disconnect()
            break
        }
        return urlStr
    }

    suspend fun downloadAndExtractModel(
        context: Context,
        languageCode: String,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        if (isModelDownloaded(context, languageCode)) {
            Timber.d("Model for $languageCode is already downloaded. Skipping download.")
            return@withContext true
        }

        val initialUrl = LANGUAGE_MODEL_URLS[languageCode] ?: LANGUAGE_MODEL_URLS["en-US"] ?: return@withContext false
        val modelDir = File(context.filesDir, "models/zipformer_$languageCode")
        if (!modelDir.exists()) modelDir.mkdirs()

        val ext = when {
            initialUrl.contains(".tar.bz2") -> "tar.bz2"
            initialUrl.contains(".tar") -> "tar"
            else -> "zip"
        }
        val tempArchiveFile = File(context.cacheDir, "temp_model_$languageCode.$ext")

        try {
            val finalUrlStr = getRedirectedUrl(initialUrl)
            Timber.d("Downloading Zipformer model for $languageCode from final URL: $finalUrlStr")

            val url = URL(finalUrlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 20000
            connection.readTimeout = 60000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.connect()

            val fileLength = connection.contentLength
            val input = BufferedInputStream(connection.inputStream)
            val output = FileOutputStream(tempArchiveFile)

            val data = ByteArray(BUFFER_SIZE)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt()
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            if (!tempArchiveFile.exists() || tempArchiveFile.length() < 100_000) {
                Timber.e("Downloaded temp file is too small or invalid (size = ${tempArchiveFile.length()} bytes)")
                if (tempArchiveFile.exists()) tempArchiveFile.delete()
                return@withContext false
            }

            // Extract archive using ZipUtils
            val extracted = ZipUtils.extractArchive(tempArchiveFile, modelDir.absolutePath)
            if (tempArchiveFile.exists()) tempArchiveFile.delete()

            if (extracted) {
                Timber.d("Model downloaded and extracted successfully to ${modelDir.absolutePath}")
                return@withContext true
            } else {
                Timber.e("Failed to extract archive for $languageCode")
                return@withContext false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error downloading model for $languageCode")
            if (tempArchiveFile.exists()) tempArchiveFile.delete()
            return@withContext false
        }
    }
}
