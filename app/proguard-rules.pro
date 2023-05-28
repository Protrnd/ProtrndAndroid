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

#Retrofit2
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

-dontnote okhttp3.**, okio.**, retrofit2.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep public class com.your.packages.model.** {*;}
#GSON Annotations
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class com.google.api.** { *; }

-dontwarn com.gowtham.library**
#-dontwarn org.bouncycastle.jsse.BCSSLParameters.**
#-dontwarn org.bouncycastle.jsse.BCSSLSocket.**
#-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider.**
#-dontwarn org.conscrypt.Conscrypt$Version.**
#-dontwarn org.conscrypt.Conscrypt.**
#-dontwarn org.conscrypt.ConscryptHostnameVerifier.**
#-dontwarn org.openjsse.javax.net.ssl.SSLParameters.**
#-dontwarn org.openjsse.javax.net.ssl.SSLSocket.**
#-dontwarn org.openjsse.net.ssl.OpenJSSE.**
-keep class com.gowtham.library** { *; }
-keep interface com.gowtham.library** { *; }