package com.tapbi.spark.voiceai.utils

import com.tapbi.spark.voiceai.App
import com.tapbi.spark.voiceai.common.Constant
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.zip.ZipFile

object ZipUtils {

    private const val BUFFER_SIZE = 8192

    fun downloadZip(url: String): Boolean {
        val basePath =
            App.instance.filesDir.toString() + File.separator + Constant.FILE_THEME_STORAGE
        val folder = File(basePath)
        if (!folder.exists()) folder.mkdirs()
        val strings = url.split("/").dropLastWhile { it.isEmpty() }

        try {
            val u = URL(url)
            val conn = u.openConnection()
            val contentLength = conn.contentLength

            val stream = DataInputStream(conn.getInputStream())

            val buffer = ByteArray(contentLength)
            stream.readFully(buffer)
            stream.close()

            val desFile = File(basePath + File.separator + strings[strings.size - 1])
            val fos = DataOutputStream(FileOutputStream(desFile))
            fos.write(buffer)
            fos.flush()
            fos.close()
            unzip(desFile, basePath)
            desFile.delete()
        } catch (e: FileNotFoundException) {
            return false
        } catch (e: IOException) {
            val f = File(basePath + File.separator + strings[strings.size - 1])
            if (f.exists()) f.delete()
            return false
        }

        return true
    }

    fun extractArchive(
        archiveFile: File,
        destDirectory: String,
        onProgress: ((Int) -> Unit)? = null
    ): Boolean {
        val destDir = File(destDirectory)
        if (!destDir.exists()) destDir.mkdirs()

        val name = archiveFile.name.lowercase()
        return try {
            when {
                name.endsWith(".tar.bz2") || name.endsWith(".tbz2") || name.endsWith(".bz2") -> {
                    unTarBz2(archiveFile, destDirectory, onProgress)
                    true
                }
                name.endsWith(".tar") -> {
                    unTar(archiveFile, destDirectory, onProgress)
                    true
                }
                else -> {
                    unzip(archiveFile, destDirectory)
                    true
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting archive ${archiveFile.name}")
            false
        }
    }

    fun unTarBz2(
        tarBz2File: File,
        destDirectory: String,
        onProgress: ((Int) -> Unit)? = null
    ) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) destDir.mkdirs()

        onProgress?.invoke(91)

        // 1. Thử dùng lệnh C++ Native tar của Android (/system/bin/tar) để gia tốc giải nén gấp 10 lần (~10-15s)
        var nativeSuccess = false
        try {
            val tarPath = File("/system/bin/tar")
            val toyboxPath = File("/system/bin/toybox")
            val nativeBin = when {
                tarPath.exists() -> tarPath.absolutePath
                toyboxPath.exists() -> "${toyboxPath.absolutePath} tar"
                else -> null
            }

            if (nativeBin != null) {
                Timber.d("Attempting native C++ system tar extraction using $nativeBin...")
                onProgress?.invoke(95)
                val process = ProcessBuilder(
                    "sh", "-c",
                    "$nativeBin -xf \"${tarBz2File.absolutePath}\" -C \"${destDir.absolutePath}\""
                ).redirectErrorStream(true).start()

                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    Timber.d("Native C++ system tar extraction succeeded in seconds!")
                    moveSubfolderFilesToRoot(destDirectory)
                    cleanUpNonInt8Models(destDirectory)
                    onProgress?.invoke(100)
                    nativeSuccess = true
                } else {
                    Timber.w("Native tar exited with code $exitCode, falling back to Java BZip2 stream")
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Native tar extraction failed, falling back to Java BZip2 stream")
        }

        if (nativeSuccess) return

        // 2. Dự phòng: Luồng giải nén Java BZip2 tiêu chuẩn
        var currentProgress = 90
        onProgress?.invoke(90)

        FileInputStream(tarBz2File).use { fis ->
            BufferedInputStream(fis, 65536).use { bis ->
                BZip2CompressorInputStream(bis).use { bzIn ->
                    TarArchiveInputStream(bzIn).use { tarIn ->
                        var entry = tarIn.nextTarEntry
                        while (entry != null) {
                            val entryName = entry.name.lowercase()
                            val fileName = entry.name.substringAfterLast("/")

                            // Chỉ bỏ qua file Encoder FP32 thừa (578MB) và các file âm thanh test_wavs
                            val isRedundantFp32Onnx = entryName.contains("encoder", ignoreCase = true) && entryName.contains("epoch", ignoreCase = true) && !entryName.contains("int8", ignoreCase = true)
                            val isTestWav = entryName.contains("test_wavs", ignoreCase = true)

                            if (!entry.isDirectory && fileName.isNotBlank() && !isRedundantFp32Onnx && !isTestWav) {
                                val outputFile = File(destDirectory, fileName)
                                BufferedOutputStream(FileOutputStream(outputFile), 65536).use { out ->
                                    tarIn.copyTo(out)
                                }
                                if (currentProgress < 99) {
                                    currentProgress += 2
                                    onProgress?.invoke(currentProgress.coerceAtMost(99))
                                }
                            }
                            entry = tarIn.nextTarEntry
                        }
                    }
                }
            }
        }
        cleanUpNonInt8Models(destDirectory)
        onProgress?.invoke(100)
    }

    fun unTar(
        tarFile: File,
        destDirectory: String,
        onProgress: ((Int) -> Unit)? = null
    ) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) destDir.mkdirs()

        var currentProgress = 90
        onProgress?.invoke(90)

        FileInputStream(tarFile).use { fis ->
            BufferedInputStream(fis, 65536).use { bis ->
                TarArchiveInputStream(bis).use { tarIn ->
                    var entry = tarIn.nextTarEntry
                    while (entry != null) {
                        val entryName = entry.name.lowercase()
                        val fileName = entry.name.substringAfterLast("/")

                        val isRedundantFp32Onnx = entryName.contains("encoder", ignoreCase = true) && entryName.contains("epoch", ignoreCase = true) && !entryName.contains("int8", ignoreCase = true)
                        val isTestWav = entryName.contains("test_wavs", ignoreCase = true)

                        if (!entry.isDirectory && fileName.isNotBlank() && !isRedundantFp32Onnx && !isTestWav) {
                            val outputFile = File(destDirectory, fileName)
                            BufferedOutputStream(FileOutputStream(outputFile), 65536).use { out ->
                                tarIn.copyTo(out)
                            }
                            if (currentProgress < 99) {
                                currentProgress += 2
                                onProgress?.invoke(currentProgress.coerceAtMost(99))
                            }
                        }
                        entry = tarIn.nextTarEntry
                    }
                }
            }
        }
        onProgress?.invoke(100)
    }

    fun unzip(zipFilePath: File, destDirectory: String) {
        File(destDirectory).run {
            if (!exists()) {
                mkdirs()
            }
        }

        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val fileName = entry.name.substringAfterLast("/")
                if (!entry.isDirectory && fileName.isNotBlank()) {
                    val filePath = destDirectory + File.separator + fileName
                    zip.getInputStream(entry).use { input ->
                        extractFile(input, filePath)
                    }
                }
            }
        }
    }

    /**
     * Giải nén .tar.bz2 trực tiếp từ một InputStream (vd: HTTP stream) — không cần file tạm.
     * Chỉ ghi các file model cần thiết (encoder/joiner int8, decoder FP32, tokens) và
     * DỪNG SỚM ngay khi đã đủ 4 file, bỏ qua phần còn lại của archive.
     *
     * @return true nếu đã có đủ encoder.int8, decoder, joiner.int8, tokens.
     */
    fun extractModelTarBz2Stream(input: InputStream, destDirectory: String): Boolean {
        val destDir = File(destDirectory)
        if (!destDir.exists()) destDir.mkdirs()

        var hasEncoder = false
        var hasDecoder = false
        var hasJoiner = false
        var hasTokens = false
        val buffer = ByteArray(256 * 1024)

        BZip2CompressorInputStream(input).use { bzIn ->
            TarArchiveInputStream(bzIn).use { tarIn ->
                var entry = tarIn.nextTarEntry
                while (entry != null) {
                    val fileName = entry.name.substringAfterLast("/")
                    val lower = fileName.lowercase()
                    val isTestWav = entry.name.contains("test_wavs", ignoreCase = true)

                    val kind = when {
                        entry.isDirectory || isTestWav || fileName.isBlank() -> null
                        lower.endsWith(".onnx") && lower.contains("encoder") && lower.contains("int8") -> "encoder"
                        lower.endsWith(".onnx") && lower.contains("joiner") && lower.contains("int8") -> "joiner"
                        lower.endsWith(".onnx") && lower.contains("decoder") && !lower.contains("int8") -> "decoder"
                        lower.contains("tokens") && lower.endsWith(".txt") -> "tokens"
                        else -> null // FP32 encoder/joiner, README, test wavs... → bỏ qua
                    }

                    if (kind != null) {
                        BufferedOutputStream(FileOutputStream(File(destDir, fileName)), 256 * 1024).use { out ->
                            var n: Int
                            while (tarIn.read(buffer).also { n = it } != -1) out.write(buffer, 0, n)
                        }
                        Timber.d("Extracted $fileName")
                        when (kind) {
                            "encoder" -> hasEncoder = true
                            "joiner" -> hasJoiner = true
                            "decoder" -> hasDecoder = true
                            "tokens" -> hasTokens = true
                        }
                        if (hasEncoder && hasDecoder && hasJoiner && hasTokens) {
                            Timber.d("All required model files extracted — stopping early")
                            return true
                        }
                    }
                    entry = tarIn.nextTarEntry
                }
            }
        }
        return hasEncoder && hasDecoder && hasJoiner && hasTokens
    }

    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    fun moveSubfolderFilesToRoot(destDirectory: String) {
        try {
            val destDir = File(destDirectory)
            if (!destDir.exists()) return
            val subDirs = destDir.listFiles()?.filter { it.isDirectory } ?: return
            for (subDir in subDirs) {
                val subFiles = subDir.listFiles() ?: continue
                for (file in subFiles) {
                    val targetFile = File(destDir, file.name)
                    if (targetFile.exists()) targetFile.delete()
                    file.renameTo(targetFile)
                }
                subDir.deleteRecursively()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error moving subfolder files to root in $destDirectory")
        }
    }

    fun cleanUpNonInt8Models(destDirectory: String) {
        try {
            val destDir = File(destDirectory)
            if (!destDir.exists()) return
            val files = destDir.walkTopDown().filter { it.isFile }.toList()

            // NOTE: decoder has NO int8 variant — keep decoder.onnx (it's small, ~2-11MB)
            // Only encoder and joiner have int8 variants; delete their FP32 counterparts if int8 exists.
            val hasInt8Encoder = files.any { it.name.contains("encoder", ignoreCase = true) && it.name.contains("int8", ignoreCase = true) }
            val hasInt8Joiner = files.any { it.name.contains("joiner", ignoreCase = true) && it.name.contains("int8", ignoreCase = true) }

            if (hasInt8Encoder) {
                files.filter { it.name.contains("encoder", ignoreCase = true) && !it.name.contains("int8", ignoreCase = true) && it.name.endsWith(".onnx") }
                    .forEach { file ->
                        Timber.d("Deleting redundant FP32 encoder: ${file.name} (${file.length() / 1024 / 1024}MB)")
                        file.delete()
                    }
            }
            if (hasInt8Joiner) {
                files.filter { it.name.contains("joiner", ignoreCase = true) && !it.name.contains("int8", ignoreCase = true) && it.name.endsWith(".onnx") }
                    .forEach { file ->
                        Timber.d("Deleting redundant FP32 joiner: ${file.name} (${file.length() / 1024 / 1024}MB)")
                        file.delete()
                    }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up non-int8 model files in $destDirectory")
        }
    }
}
