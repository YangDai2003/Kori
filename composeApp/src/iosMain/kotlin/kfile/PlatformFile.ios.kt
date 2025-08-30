package kfile

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsDirectoryKey
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToURL
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
    return url.lastPathComponent ?: ""
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
    return url.pathExtension ?: ""
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

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
actual fun PlatformFile.getLastModified(): Instant {
    val path = url.path ?: return Clock.System.now()
    val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, null)
    val nsDate = attributes?.get("NSFileModificationDate") as? NSDate ?: return Clock.System.now()
    return nsDate.toInstant()
}

@OptIn(ExperimentalTime::class)
private fun NSDate.toInstant(): Instant {
    val secs = timeIntervalSince1970()
    val fullSeconds = secs.toLong()
    val nanos = (secs - fullSeconds) * 1_000_000_000
    return Instant.fromEpochSeconds(fullSeconds, nanos.toLong())
}