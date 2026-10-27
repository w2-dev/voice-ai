package com.tapbi.spark.voiceai.utils

import android.content.Context
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig
import com.k2fsa.sherpa.onnx.SileroVadModelConfig
import com.k2fsa.sherpa.onnx.SpeakerEmbeddingExtractor
import com.k2fsa.sherpa.onnx.SpeakerEmbeddingExtractorConfig
import com.k2fsa.sherpa.onnx.Vad
import com.k2fsa.sherpa.onnx.VadModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

import com.tapbi.spark.voiceai.common.Constant

object ModelManager {

    private const val BUFFER_SIZE = 8192

    val LANGUAGE_MODEL_URLS get() = Constant.ZIPFORMER_MODEL_URLS

    private var cachedRecognizer: OfflineRecognizer? = null
    private var cachedRecognizerLang: String? = null
    private var cachedExtractor: SpeakerEmbeddingExtractor? = null
    private var cachedVad: Vad? = null

    @Synchronized
    fun getOrNewRecognizer(context: Context, languageCode: String = "vi-VN"): OfflineRecognizer {
        if (cachedRecognizer != null && cachedRecognizerLang == languageCode) {
            return cachedRecognizer!!
        }
        try {
            cachedRecognizer?.release()
        } catch (e: Exception) {
            Timber.e(e)
        }
        val recognizer = createRecognizer(context, languageCode)
        cachedRecognizer = recognizer
        cachedRecognizerLang = languageCode
        return recognizer
    }

    @Synchronized
    fun getOrNewExtractor(context: Context): SpeakerEmbeddingExtractor {
        if (cachedExtractor != null) {
            return cachedExtractor!!
        }
        val config = SpeakerEmbeddingExtractorConfig(
            Constant.MODEL_SPEAKER_CAMPPLUS_PATH,
            2,
            false,
            "cpu"
        )
        val extractor = SpeakerEmbeddingExtractor(context.assets, config)
        cachedExtractor = extractor
        return extractor
    }

    @Synchronized
    fun getOrNewVad(context: Context): Vad {
        if (cachedVad != null) {
            return cachedVad!!
        }
        val vadConfig = VadModelConfig(
            sileroVadModelConfig = SileroVadModelConfig(
                model = Constant.MODEL_SILERO_VAD_PATH,
                threshold = 0.3F,
                minSilenceDuration = 0.25F,
                minSpeechDuration = 0.15F,
                windowSize = 512
            ),
            sampleRate = 16000,
            numThreads = 1,
            provider = "cpu"
        )
        val vad = Vad(context.assets, vadConfig)
        cachedVad = vad
        return vad
    }

    fun prewarmModels(context: Context, languageCode: String = "vi-VN") {
        try {
            getOrNewRecognizer(context, languageCode)
            getOrNewExtractor(context)
            getOrNewVad(context)
            Timber.d("ONNX Models pre-warmed successfully for $languageCode")
        } catch (e: Exception) {
            Timber.e(e, "Error pre-warming models")
        }
    }

    fun isModelDownloaded(context: Context, languageCode: String): Boolean {
        if (languageCode == "vi-VN") return true // Built-in assets fallback
        val modelDir = File(context.filesDir, "models/zipformer_$languageCode")
        if (!modelDir.exists()) return false

        val allFiles = modelDir.walkTopDown().filter { it.isFile }.toList()
        if (allFiles.isEmpty()) return false

        val hasEncoder = allFiles.any { it.name.contains("encoder", ignoreCase = true) }
        val hasDecoder = allFiles.any { it.name.contains("decoder", ignoreCase = true) }
        val hasTokens = allFiles.any { it.name.contains("tokens", ignoreCase = true) }

        val isComplete = (hasEncoder && hasDecoder && hasTokens) || allFiles.size >= 3
        Timber.d("isModelDownloaded check for $languageCode in ${modelDir.absolutePath}: filesCount=${allFiles.size}, isComplete=$isComplete")

        return isComplete
    }

    fun createRecognizer(context: Context, languageCode: String = "vi-VN"): OfflineRecognizer {
        // 1. Check local downloaded model in filesDir
        val modelDir = File(context.filesDir, "models/zipformer_$languageCode")

        if (modelDir.exists()) {
            val allFiles = modelDir.walkTopDown().filter { it.isFile }.toList()
            val encoderFile = allFiles.firstOrNull { it.name.contains("encoder", ignoreCase = true) }
            val decoderFile = allFiles.firstOrNull { it.name.contains("decoder", ignoreCase = true) }
            val joinerFile = allFiles.firstOrNull { it.name.contains("joiner", ignoreCase = true) }
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
        val assetFolder = Constant.MODEL_ZIPFORMER_ASSET_FOLDER

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
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            connection.setRequestProperty("Accept", "*/*")

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
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            connection.setRequestProperty("Accept", "*/*")
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
