package kfile

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
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

actual class PlatformFile(val url: NSURL)

actual fun PlatformFile.exists(): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(url.path ?: return false)
}

actual val PlatformFile.fileName: String
    get() = url.lastPathComponent ?: ""

actual val PlatformFile.path: String
    get() = url.absoluteString ?: ""

@OptIn(ExperimentalForeignApi::class)
actual val PlatformFile.isDirectory: Boolean
    get() {
        val result = url.resourceValuesForKeys(listOf(NSURLIsDirectoryKey), null)
        return result?.get(NSURLIsDirectoryKey) == true
    }

actual val PlatformFile.extension: String
    get() = url.pathExtension ?: ""

@OptIn(ExperimentalForeignApi::class)
actual suspend fun PlatformFile.readText(): String {
    return withContext(Dispatchers.IO) {
        NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null) ?: ""
    }
}

@Suppress("CAST_NEVER_SUCCEEDS")
@OptIn(ExperimentalForeignApi::class)
actual suspend fun PlatformFile.writeText(text: String) {
    withContext(Dispatchers.IO) {
        (text as NSString).writeToURL(url, true, NSUTF8StringEncoding, null)
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun PlatformFile.delete(): Boolean {
    return withContext(Dispatchers.IO) {
        NSFileManager.defaultManager.removeItemAtURL(url, null)
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
actual fun PlatformFile.lastModified(): Instant {
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