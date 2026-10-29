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
import com.tapbi.spark.voiceai.common.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import android.os.Handler
import android.os.Looper
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

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
        NativeLibLoader.ensureLoaded()
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
        val hasInt8Encoder = allFiles.any {
            it.name.contains(
                "encoder",
                ignoreCase = true
            ) && it.name.contains("int8", ignoreCase = true)
        }
        val hasDecoder = allFiles.any {
            it.name.contains(
                "decoder",
                ignoreCase = true
            ) && it.name.endsWith(".onnx") && !it.name.contains("int8", ignoreCase = true)
        }
        val hasInt8Joiner = allFiles.any {
            it.name.contains("joiner", ignoreCase = true) && it.name.contains(
                "int8",
                ignoreCase = true
            )
        }
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
                it.name.contains("encoder", ignoreCase = true) && it.name.contains(
                    "int8",
                    ignoreCase = true
                )
            }
            val decoderFile = allFiles.firstOrNull {
                it.name.contains("decoder", ignoreCase = true)
                        && it.name.endsWith(".onnx")
                        && !it.name.contains("int8", ignoreCase = true)
            }
            val joinerFile = allFiles.firstOrNull {
                it.name.contains("joiner", ignoreCase = true) && it.name.contains(
                    "int8",
                    ignoreCase = true
                )
            }
            val tokensFile = allFiles.firstOrNull { it.name.contains("tokens", ignoreCase = true) }

            // Only encoder and joiner are required to be int8
            val missingInt8 = listOfNotNull(
                if (encoderFile == null) "encoder.int8" else null,
                if (joinerFile == null) "joiner.int8" else null
            )
            if (missingInt8.isNotEmpty()) {
                Timber.w("Missing int8 model files for $languageCode: $missingInt8. Deleting model dir to force re-download.")
                try {
                    modelDir.deleteRecursively()
                } catch (_: Exception) {
                }
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
                    Timber.e(
                        e,
                        "Error loading int8 model for $languageCode, deleting corrupt folder and falling back"
                    )
                    try {
                        modelDir.deleteRecursively()
                    } catch (_: Exception) {
                    }
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
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
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

        val initialUrl = LANGUAGE_MODEL_URLS[languageCode] ?: LANGUAGE_MODEL_URLS["en-US"]
        ?: return@withContext false
        val modelDir = File(context.filesDir, "models/zipformer_$languageCode")
        if (!modelDir.exists()) modelDir.mkdirs()

        // Ưu tiên: tải trực tiếp 4 file int8 đã giải nén (không cần giải mã bz2)
        Constant.ZIPFORMER_DIRECT_FILES[languageCode]?.let { fileUrls ->
            if (downloadFilesDirect(context, languageCode, fileUrls, modelDir, onProgress)) {
                return@withContext true
            }
            Timber.w("Direct download failed for $languageCode — falling back to archive")
            if (!modelDir.exists()) modelDir.mkdirs()
        }

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

        // .tar.bz2: giải nén TRỰC TIẾP từ HTTP stream (download + giải nén song song, không file tạm)
        if (ext == "tar.bz2") {
            return@withContext streamDownloadAndExtractTarBz2(context, languageCode, initialUrl, modelDir, onProgress)
        }

        try {
            val finalUrlStr = getRedirectedUrl(initialUrl)
            Timber.d("Downloading Zipformer model for $languageCode from final URL: $finalUrlStr")

            val url = URL(finalUrlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 300000  // 5 minutes — large model files can be 750MB+
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
            connection.setRequestProperty("Accept", "*/*")
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.connect()

            // Use contentLengthLong() — contentLength() returns Int and overflows for files >2GB
            val fileLength = connection.contentLengthLong
            Timber.d("Content-Length for $languageCode: $fileLength bytes (${fileLength / 1024 / 1024} MB)")

            val input = BufferedInputStream(connection.inputStream, 131072)
            val output = BufferedOutputStream(FileOutputStream(tempArchiveFile), 131072)

            val data = ByteArray(131072)
            var total: Long = 0
            var count: Int
            var lastProgress = -1

            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    val progress = ((total * 90) / fileLength).toInt().coerceIn(0, 90)
                    if (progress != lastProgress) {
                        lastProgress = progress
                        withContext(Dispatchers.Main) { onProgress(progress) }
                    }
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

            withContext(Dispatchers.Main) { onProgress(90) }
            val extracted = ZipUtils.extractArchive(tempArchiveFile, modelDir.absolutePath) { pct ->
                try {
                    kotlinx.coroutines.runBlocking(Dispatchers.Main) { onProgress(pct) }
                } catch (_: Exception) {}
            }
            if (tempArchiveFile.exists()) tempArchiveFile.delete()

            if (extracted && isModelDownloaded(context, languageCode)) {
                withContext(Dispatchers.Main) { onProgress(100) }
                Timber.d("Extraction complete and model verified for $languageCode")
                return@withContext true
            } else {
                Timber.e("Extraction failed for $languageCode or model incomplete")
                return@withContext false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error downloading model for $languageCode")
            if (tempArchiveFile.exists()) tempArchiveFile.delete()
            return@withContext false
        }
    }

    private fun openHttp(urlStr: String, method: String = "GET"): HttpURLConnection =
        (URL(urlStr).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            requestMethod = method
            connectTimeout = 30000
            readTimeout = 120000
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
            setRequestProperty("Accept", "*/*")
            setRequestProperty("Accept-Encoding", "identity")
        }

    /**
     * Tải trực tiếp từng file model (encoder.int8, decoder, joiner.int8, tokens) — không giải nén.
     * Ghi ra `*.part` rồi rename để file dở dang không bị coi là model hợp lệ.
     */
    private fun downloadFilesDirect(
        context: Context,
        languageCode: String,
        fileUrls: List<String>,
        modelDir: File,
        onProgress: (Int) -> Unit
    ): Boolean {
        val mainHandler = Handler(Looper.getMainLooper())
        val startMs = System.currentTimeMillis()
        try {
            // 1. Lấy tổng dung lượng để hiển thị progress chính xác
            val sizes = fileUrls.map { u ->
                val c = openHttp(u, "HEAD")
                try { c.connect(); c.contentLengthLong } catch (_: Exception) { -1L } finally { c.disconnect() }
            }
            val totalBytes = sizes.filter { it > 0 }.sum()
            Timber.d("Direct download $languageCode: ${fileUrls.size} files, total ${totalBytes / 1024 / 1024} MB")

            var doneBytes = 0L
            var lastProgress = -1
            val buffer = ByteArray(256 * 1024)

            for (u in fileUrls) {
                val fileName = u.substringAfterLast("/")
                val target = File(modelDir, fileName)
                val part = File(modelDir, "$fileName.part")
                part.delete()

                val conn = openHttp(u)
                try {
                    conn.connect()
                    if (conn.responseCode !in 200..299) {
                        Timber.e("HTTP ${conn.responseCode} for $u")
                        return false
                    }
                    val expected = conn.contentLengthLong
                    var written = 0L
                    BufferedInputStream(conn.inputStream, 256 * 1024).use { input ->
                        BufferedOutputStream(FileOutputStream(part), 256 * 1024).use { out ->
                            var n: Int
                            while (input.read(buffer).also { n = it } != -1) {
                                out.write(buffer, 0, n)
                                written += n
                                doneBytes += n
                                if (totalBytes > 0) {
                                    val p = ((doneBytes * 99) / totalBytes).toInt().coerceIn(0, 99)
                                    if (p != lastProgress) {
                                        lastProgress = p
                                        mainHandler.post { onProgress(p) }
                                    }
                                }
                            }
                        }
                    }
                    if (expected > 0 && written != expected) {
                        Timber.e("Incomplete $fileName: $written / $expected bytes")
                        part.delete()
                        return false
                    }
                    target.delete()
                    if (!part.renameTo(target)) {
                        Timber.e("Rename failed for $fileName")
                        return false
                    }
                    Timber.d("Downloaded $fileName (${written / 1024 / 1024} MB)")
                } finally {
                    conn.disconnect()
                }
            }

            Timber.d("Direct download $languageCode finished in ${(System.currentTimeMillis() - startMs) / 1000}s")
            if (isModelDownloaded(context, languageCode)) {
                mainHandler.post { onProgress(100) }
                return true
            }
            return false
        } catch (e: Exception) {
            Timber.e(e, "Direct download error for $languageCode")
            return false
        } finally {
            modelDir.listFiles { f -> f.name.endsWith(".part") }?.forEach { it.delete() }
            if (!isModelDownloaded(context, languageCode)) modelDir.deleteRecursively()
        }
    }

    private fun streamDownloadAndExtractTarBz2(
        context: Context,
        languageCode: String,
        initialUrl: String,
        modelDir: File,
        onProgress: (Int) -> Unit
    ): Boolean {
        val mainHandler = Handler(Looper.getMainLooper())
        var connection: HttpURLConnection? = null
        val startMs = System.currentTimeMillis()
        try {
            val finalUrlStr = getRedirectedUrl(initialUrl)
            Timber.d("Streaming Zipformer model for $languageCode from: $finalUrlStr")

            connection = (URL(finalUrlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30000
                readTimeout = 300000
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                )
                setRequestProperty("Accept", "*/*")
                setRequestProperty("Accept-Encoding", "identity")
                connect()
            }

            val fileLength = connection.contentLengthLong
            Timber.d("Content-Length for $languageCode: $fileLength bytes (${fileLength / 1024 / 1024} MB)")

            var lastProgress = -1
            val progressStream = ProgressInputStream(
                BufferedInputStream(connection.inputStream, 256 * 1024)
            ) { readBytes ->
                if (fileLength > 0) {
                    val p = ((readBytes * 99) / fileLength).toInt().coerceIn(0, 99)
                    if (p != lastProgress) {
                        lastProgress = p
                        mainHandler.post { onProgress(p) }
                    }
                }
            }

            val ok = progressStream.use {
                ZipUtils.extractModelTarBz2Stream(it, modelDir.absolutePath)
            }
            Timber.d("Stream extract for $languageCode finished in ${(System.currentTimeMillis() - startMs) / 1000}s, ok=$ok")

            if (ok && isModelDownloaded(context, languageCode)) {
                mainHandler.post { onProgress(100) }
                return true
            }
            Timber.e("Streamed model incomplete for $languageCode")
            modelDir.deleteRecursively()
            return false
        } catch (e: Exception) {
            Timber.e(e, "Error streaming model for $languageCode")
            modelDir.deleteRecursively()
            return false
        } finally {
            try { connection?.disconnect() } catch (_: Exception) {}
        }
    }

    /** Đếm số byte đã đọc từ network để tính progress khi stream-extract. */
    private class ProgressInputStream(
        input: InputStream,
        private val onRead: (Long) -> Unit
    ) : FilterInputStream(input) {
        private var total = 0L

        override fun read(): Int {
            val b = super.read()
            if (b != -1) { total++; onRead(total) }
            return b
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val n = super.read(b, off, len)
            if (n > 0) { total += n; onRead(total) }
            return n
        }
    }
}
