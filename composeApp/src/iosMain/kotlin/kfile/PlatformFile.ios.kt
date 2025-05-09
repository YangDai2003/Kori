package kfile

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsDirectoryKey
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL

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

@Suppress("CAST_NEVER_SUCCEEDS")
@OptIn(ExperimentalForeignApi::class)
actual suspend fun PlatformFile.writeText(text: String) {
    (text as NSString).writeToURL(url, true, NSUTF8StringEncoding, null)
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun PlatformFile.delete(): Boolean {
    return NSFileManager.defaultManager.removeItemAtURL(url, null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun PlatformFile.getLastModified(): Instant {
    val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(url.path ?: return Clock.System.now(), null)
    return (attributes?.get("NSFileModificationDate") as? NSDate)?.toKotlinInstant() ?: Clock.System.now()
}