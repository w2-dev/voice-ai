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
            BufferedInputStream(fis).use { bis ->
                BZip2CompressorInputStream(bis).use { bzIn ->
                    TarArchiveInputStream(bzIn).use { tarIn ->
                        var entry = tarIn.nextTarEntry
                        while (entry != null) {
                            val fileName = entry.name.substringAfterLast("/")
                            if (!entry.isDirectory && fileName.isNotBlank()) {
                                val outputFile = File(destDirectory, fileName)
                                BufferedOutputStream(FileOutputStream(outputFile)).use { out ->
                                    tarIn.copyTo(out)
                                }
                            }
                            entry = tarIn.nextTarEntry
                        }
                    }
                }
            }
        }
    }

    fun unTar(tarFile: File, destDirectory: String) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) destDir.mkdirs()

        FileInputStream(tarFile).use { fis ->
            BufferedInputStream(fis).use { bis ->
                TarArchiveInputStream(bis).use { tarIn ->
                    var entry = tarIn.nextTarEntry
                    while (entry != null) {
                        val fileName = entry.name.substringAfterLast("/")
                        if (!entry.isDirectory && fileName.isNotBlank()) {
                            val outputFile = File(destDirectory, fileName)
                            BufferedOutputStream(FileOutputStream(outputFile)).use { out ->
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
}
