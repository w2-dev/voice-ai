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

    fun extractArchive(archiveFile: File, destDirectory: String): Boolean {
        val destDir = File(destDirectory)
        if (!destDir.exists()) destDir.mkdirs()

        val name = archiveFile.name.lowercase()
        return try {
            when {
                name.endsWith(".tar.bz2") || name.endsWith(".tbz2") || name.endsWith(".bz2") -> {
                    unTarBz2(archiveFile, destDirectory)
                    true
                }
                name.endsWith(".tar") -> {
                    unTar(archiveFile, destDirectory)
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

    fun unTarBz2(tarBz2File: File, destDirectory: String) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) destDir.mkdirs()

        FileInputStream(tarBz2File).use { fis ->
            BufferedInputStream(fis, 65536).use { bis ->
                BZip2CompressorInputStream(bis).use { bzIn ->
                    TarArchiveInputStream(bzIn).use { tarIn ->
                        var entry = tarIn.nextTarEntry
                        while (entry != null) {
                            val entryName = entry.name.lowercase()
                            val fileName = entry.name.substringAfterLast("/")

                            // Bỏ qua file FP32 thừa (578MB) và các file âm thanh test_wavs để giải nén siêu tốc
                            val isRedundantFp32Onnx = entryName.endsWith(".onnx") && entryName.contains("epoch") && !entryName.contains("int8")
                            val isTestWav = entryName.contains("test_wavs")

                            if (!entry.isDirectory && fileName.isNotBlank() && !isRedundantFp32Onnx && !isTestWav) {
                                val outputFile = File(destDirectory, fileName)
                                BufferedOutputStream(FileOutputStream(outputFile), 65536).use { out ->
                                    tarIn.copyTo(out)
                                }
                            }
                            entry = tarIn.nextTarEntry
                        }
                    }
                }
            }
        }
        cleanUpNonInt8Models(destDirectory)
    }

    fun unTar(tarFile: File, destDirectory: String) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) destDir.mkdirs()

        FileInputStream(tarFile).use { fis ->
            BufferedInputStream(fis, 65536).use { bis ->
                TarArchiveInputStream(bis).use { tarIn ->
                    var entry = tarIn.nextTarEntry
                    while (entry != null) {
                        val entryName = entry.name.lowercase()
                        val fileName = entry.name.substringAfterLast("/")

                        val isRedundantFp32Onnx = entryName.endsWith(".onnx") && entryName.contains("epoch") && !entryName.contains("int8")
                        val isTestWav = entryName.contains("test_wavs")

                        if (!entry.isDirectory && fileName.isNotBlank() && !isRedundantFp32Onnx && !isTestWav) {
                            val outputFile = File(destDirectory, fileName)
                            BufferedOutputStream(FileOutputStream(outputFile), 65536).use { out ->
                                tarIn.copyTo(out)
                            }
                        }
                        entry = tarIn.nextTarEntry
                    }
                }
            }
        }
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

    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
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
