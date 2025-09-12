# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keepclasseswithmembers class androidx.sqlite.driver.bundled.** { native <methods>; }

-dontwarn io.micrometer.context.ContextAccessor
-dontwarn javax.enterprise.event.Observes
-dontwarn javax.enterprise.inject.Default
-dontwarn javax.enterprise.inject.spi.AfterBeanDiscovery
-dontwarn javax.enterprise.inject.spi.Bean
-dontwarn javax.enterprise.inject.spi.BeanManager
-dontwarn javax.enterprise.inject.spi.Extension
-dontwarn javax.enterprise.inject.spi.ProcessBean
-dontwarn reactor.blockhound.integration.BlockHoundIntegration
-dontwarn org.apache.log4j.Level
-dontwarn org.apache.log4j.Logger
-dontwarn org.apache.log4j.Priority
-dontwarn org.apache.logging.log4j.LogManager
-dontwarn org.apache.logging.log4j.Logger
-dontwarn org.apache.logging.log4j.message.MessageFactory
-dontwarn org.apache.logging.log4j.spi.ExtendedLogger
-dontwarn org.apache.logging.log4j.spi.ExtendedLoggerWrapper