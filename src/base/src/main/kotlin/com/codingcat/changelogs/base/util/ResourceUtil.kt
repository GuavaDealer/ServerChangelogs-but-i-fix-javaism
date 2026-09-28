package com.codingcat.changelogs.base.util

import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

object ResourceUtil {
    private val classLoader: ClassLoader = ResourceUtil::class.java.classLoader

    fun readResourcesAsString(dirPath: String): Map<String, String> =
        listResources(dirPath).associateWith { resource ->
            readResourceAsString("${dirPath}/${resource}")
        }

    fun readResourceAsString(path: String): String =
        try {
            val stream = checkNotNull(classLoader.getResourceAsStream(path)) { "Resource not found: ${path}" }
            stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        } catch (e: IOException) {
            throw RuntimeException("Failed to read jar resource at ${path}", e)
        }

    fun listResources(dirPath: String): Collection<String> {
        try {
            val resourceUrl: URL = checkNotNull(classLoader.getResource(dirPath)) { "Resource not found: ${dirPath}" }
            val uri = resourceUrl.toURI()
            return if (uri.scheme.equals("jar", ignoreCase = true)) {
                val (fileSystem, shouldClose) = runCatching { FileSystems.getFileSystem(uri) to false }
                    .getOrElse { FileSystems.newFileSystem(uri, emptyMap<String, Any?>()) to true }
                try {
                    fileSystem.getPath(dirPath).listDirectoryEntries().map { it.name }
                } finally {
                    if (shouldClose) {
                        fileSystem.close()
                    }
                }
            } else {
                Path.of(uri).listDirectoryEntries().map { it.name }
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to list jar resources at ${dirPath}", e)
        } catch (e: URISyntaxException) {
            throw RuntimeException("Failed to list jar resources at ${dirPath}", e)
        }
    }
}
