package kfile

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsDirectoryKey
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfURL

actual class PlatformFile(
    val url: NSURL
)

actual fun PlatformFile.exists(): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(url.path ?: return false)
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun PlatformFile.readText(): String {
    return NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null) ?: ""
}

actual fun PlatformFile.getFileName(): String {
    return url.lastPathComponent?.toString() ?: ""
}

actual fun PlatformFile.getPath(): String {
    return url.absoluteString ?: ""
}

@OptIn(ExperimentalForeignApi::class)
actual fun PlatformFile.isDirectory(): Boolean {
    val result = url.resourceValuesForKeys(listOf(NSURLIsDirectoryKey), null)
    return result?.get(NSURLIsDirectoryKey) == true
}

actual fun PlatformFile.getExtension(): String {
    return url.pathExtension?.toString() ?: ""
}