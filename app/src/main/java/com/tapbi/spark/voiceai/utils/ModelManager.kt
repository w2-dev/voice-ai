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
    fun clearCache() {
        try {
            cachedRecognizer?.release()
        } catch (e: Exception) {
            Timber.e(e)
        }
        cachedRecognizer = null
        cachedRecognizerLang = null
    }

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
        } catch (t: Throwable) {
            Timber.e(t, "Error pre-warming models")
        }
    }

    fun isModelDownloaded(context: Context, languageCode: String): Boolean {
        if (languageCode == "vi-VN") return true // Built-in assets fallback
        val modelDir = File(context.filesDir, "models/zipformer_$languageCode")
        if (!modelDir.exists()) return false

        val allFiles = modelDir.walkTopDown().filter { it.isFile }.toList()
        if (allFiles.isEmpty()) return false

        // Must match exactly what createRecognizer requires:
        //   - encoder.int8.onnx  (int8 required — FP32 too large for Android)
        //   - decoder.onnx       (FP32 only, no int8 variant)
        //   - joiner.int8.onnx   (int8 required)
        //   - tokens file
        val hasInt8Encoder = allFiles.any { it.name.contains("encoder", ignoreCase = true) && it.name.contains("int8", ignoreCase = true) }
        val hasDecoder = allFiles.any { it.name.contains("decoder", ignoreCase = true) && it.name.endsWith(".onnx") && !it.name.contains("int8", ignoreCase = true) }
        val hasInt8Joiner = allFiles.any { it.name.contains("joiner", ignoreCase = true) && it.name.contains("int8", ignoreCase = true) }
        val hasTokens = allFiles.any { it.name.contains("tokens", ignoreCase = true) }

        val isComplete = hasInt8Encoder && hasDecoder && hasInt8Joiner && hasTokens
        Timber.d("isModelDownloaded[$languageCode]: int8Encoder=$hasInt8Encoder decoder=$hasDecoder int8Joiner=$hasInt8Joiner tokens=$hasTokens → $isComplete")
        return isComplete
    }

    fun createRecognizer(context: Context, languageCode: String = "vi-VN"): OfflineRecognizer {
        // 1. Check local downloaded model in filesDir
        val modelDir = File(context.filesDir, "models/zipformer_$languageCode")

        if (modelDir.exists()) {
            ZipUtils.cleanUpNonInt8Models(modelDir.absolutePath)
            val allFiles = modelDir.walkTopDown().filter { it.isFile }.toList()

            // Per-language int8 file naming conventions:
            //   encoder → *.int8.onnx  (MUST be int8 — FP32 is too large, causes ONNX crash)
            //   decoder → *.onnx       (no int8 variant exists; decoder is small FP32, safe)
            //   joiner  → *.int8.onnx  (MUST be int8)
            val encoderFile = allFiles.firstOrNull {
                it.name.contains("encoder", ignoreCase = true) && it.name.contains("int8", ignoreCase = true)
            }
            val decoderFile = allFiles.firstOrNull {
                it.name.contains("decoder", ignoreCase = true)
                    && it.name.endsWith(".onnx")
                    && !it.name.contains("int8", ignoreCase = true)
            }
            val joinerFile = allFiles.firstOrNull {
                it.name.contains("joiner", ignoreCase = true) && it.name.contains("int8", ignoreCase = true)
            }
            val tokensFile = allFiles.firstOrNull { it.name.contains("tokens", ignoreCase = true) }

            // Only encoder and joiner are required to be int8
            val missingInt8 = listOfNotNull(
                if (encoderFile == null) "encoder.int8" else null,
                if (joinerFile == null) "joiner.int8" else null
            )
            if (missingInt8.isNotEmpty()) {
                Timber.w("Missing int8 model files for $languageCode: $missingInt8. Deleting model dir to force re-download.")
                try { modelDir.deleteRecursively() } catch (_: Exception) {}
            } else if (decoderFile != null && tokensFile != null) {
                try {
                    Timber.d("Loading int8 Zipformer model for $languageCode | encoder: ${encoderFile!!.length() / 1024 / 1024}MB decoder: ${decoderFile!!.length() / 1024 / 1024}MB joiner: ${joinerFile!!.length() / 1024 / 1024}MB")
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
                } catch (e: Throwable) {
                    Timber.e(e, "Error loading int8 model for $languageCode, deleting corrupt folder and falling back")
                    try { modelDir.deleteRecursively() } catch (_: Exception) {}
                }
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

        // Always delete stale temp file before starting a fresh download
        if (tempArchiveFile.exists()) {
            Timber.w("Deleting stale temp archive: ${tempArchiveFile.name} (${tempArchiveFile.length()} bytes)")
            tempArchiveFile.delete()
        }

        try {
            val finalUrlStr = getRedirectedUrl(initialUrl)
            Timber.d("Downloading Zipformer model for $languageCode from final URL: $finalUrlStr")

            val url = URL(finalUrlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 300000  // 5 minutes — large model files can be 750MB+
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            connection.setRequestProperty("Accept", "*/*")
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.connect()

            // Use contentLengthLong() — contentLength() returns Int and overflows for files >2GB
            val fileLength = connection.contentLengthLong
            Timber.d("Content-Length for $languageCode: $fileLength bytes (${fileLength / 1024 / 1024} MB)")

            val input = BufferedInputStream(connection.inputStream)
            val output = FileOutputStream(tempArchiveFile)

            val data = ByteArray(BUFFER_SIZE)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt().coerceIn(0, 99)
                    withContext(Dispatchers.Main) { onProgress(progress) }
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            val downloadedSize = tempArchiveFile.length()
            Timber.d("Download complete: $downloadedSize bytes (${downloadedSize / 1024 / 1024} MB)")

            // Validate: file must be at least 10MB, and match Content-Length if known
            if (downloadedSize < 10_000_000L) {
                Timber.e("Downloaded file too small: $downloadedSize bytes — likely corrupt or incomplete")
                tempArchiveFile.delete()
                return@withContext false
            }
            if (fileLength > 0 && downloadedSize < fileLength) {
                Timber.e("Incomplete download: got $downloadedSize / $fileLength bytes")
                tempArchiveFile.delete()
                return@withContext false
            }

            // Signal extraction starting (100%)
            withContext(Dispatchers.Main) { onProgress(100) }

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
