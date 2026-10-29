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

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-keepclassmembers class android.arch.** { *; }
-keep class android.arch.** { *; }
-dontwarn android.arch.**
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
#Các class được bảo mật
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}
#-renamesourcefileattribute SourceFile

-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.
-keepattributes *Annotation*

-dontwarn com.google.android.material.**
-keep class com.google.** { *; }
-keep public class * extends com.google.** { *; }

-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep public class * extends androidx.** { *; }
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }
# Giữ annotation
-keepattributes Signature
-keepattributes *Annotation*

# Giữ class dùng Gson
-keep class com.google.gson.** { *; }

# Giữ enum
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


# Prevent R8 from leaving Data object members always null
-keepclassmembers enum * { *; }

-dontwarn com.facebook.ads.internal.**
-keeppackagenames com.facebook.*
-keep public class com.facebook.ads.** {*;}
-keep public class com.facebook.ads.**
{ public protected *; }

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
#noinspection ShrinkerUnresolvedReference
#unity
-keep class com.google.android.gms.ads.** {public *;}
-keep class com.google.android.gms.appset.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
#adapters
-keep class com.ironsource.adapters.** { *; }
#sdk
-dontwarn com.ironsource.**
-dontwarn com.ironsource.adapters.**
-keepclassmembers class com.ironsource.** { public *; }
-keep public class com.ironsource.**
-keep class com.ironsource.adapters.** { *;
}
#omid
-dontwarn com.iab.omid.**
-keep class com.iab.omid.** {*;}
#javascript
-keepattributes JavascriptInterface
-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }
# For AdColony integration
-keep class com.jirbo.adcolony.* {
    static *;
}
-keep public interface com.adcolony.sdk** {*; }
#For AmazonAps integration
-keep class com.amazon.device.ads.DtbThreadService {
    static *;
}
-keep public interface com.amazon.device.ads** {*; }
#For AppLovin integration
-keepclassmembers class com.applovin.sdk.AppLovinSdk {
    static *;
}
-keep public interface com.applovin.sdk** {*; }
-keep public interface com.applovin.adview** {*; }
-keep public interface com.applovin.mediation** {*; }
-keep public interface com.applovin.communicator** {*; }
#For Bytedance integration
-keep public interface com.bytedance.sdk.openadsdk** {*; }
#For Facebook integration
-keepclassmembers class com.facebook.ads.internal.AdSdkVersion {
    static *;
}
-keepclassmembers class com.facebook.ads.internal.settings.AdSdkVersion {
    static *;
 }
-keepclassmembers class com.facebook.ads.BuildConfig {
    static *;
 }
-keep public interface com.facebook.ads** {*; }
#For Fairbid
-keep public interface com.fyber.fairbid.ads.interstitial** {*; }
-keep public interface com.fyber.fairbid.ads.rewarded** {*; }
-keep class com.fyber.offerwall.*
#For Fivead
-keep public interface com.five_corp.ad** {*; }
#For Fyber(Inneractive) integration
-keep public interface com.fyber.inneractive.sdk.external** {*; }
-keep public interface com.fyber.inneractive.sdk.activities** {*; }
-keep public interface com.fyber.inneractive.sdk.ui** {*; }
#For HyprMX integration
-keepclassmembers class com.hyprmx.android.sdk.utility.HyprMXProperties {
    static *;
}
-keepclassmembers class com.hyprmx.android.BuildConfig {
    static *;
}
-keep public interface com.hyprmx.android.sdk.activity** {*; }
-keep public interface com.hyprmx.android.sdk.graphics** {*; }
# For Inmobi integration
-keep class com.inmobi.*
-keep public interface com.inmobi.ads.listeners** {*; }
-keep public interface com.inmobi.ads.InMobiInterstitial** {*; }
-keep public interface com.inmobi.ads.InMobiBanner** {*; }
# For ironSource integration
-keep public interface com.ironsource.mediationsdk.sdk** {*; }
-keep public interface com.ironsource.mediationsdk.impressionData.ImpressionDataListener {*; }
#For Maio integration
-keep public interface jp.maio.sdk.android.MaioAdsListenerInterface {*; }
# For Mintergral integration
-keep public interface com.mbridge.msdk.out** {*; }
-keep public interface com.mbridge.msdk.videocommon.listener** {*; }
-keep public interface com.mbridge.msdk.interstitialvideo.out** {*; }
-keep public interface com.mintegral.msdk.out** {*; }
-keep public interface com.mintegral.msdk.videocommon.listener** {*; }
-keep public interface com.mintegral.msdk.interstitialvideo.out** {*; }
#For MyTarget integration
-keep class com.my.target.** {*;}
#For Ogury integration
-keep public interface io.presage.interstitial** {*; }
-keep public interface io.presage.interstitial.PresageInterstitialCallback {*; }
#For Pubnative integration
-keep public interface net.pubnative.lite.sdk.interstitial.HyBidInterstitialAd** {*; }
-keep public interface net.pubnative.lite.sdk.rewarded.HyBidRewardedAd** {*; }
-keep public interface net.pubnative.lite.sdk.views.HyBidAdView** {*; }
#For Smaato integration
-keep public interface com.smaato.sdk.interstitial** {*; }
-keep public interface com.smaato.sdk.video.vast** {*; }
-keep public interface com.smaato.sdk.banner.widget** {*; }
-keep public interface com.smaato.sdk.core.util** {*; }
# For Tapjoy integration
-keep public interface com.tapjoy.** {*; }
# For Tencent integration
-keep public interface com.qq.e.ads.interstitial2** {*; }
-keep public interface com.qq.e.ads.interstitial3** {*; }
-keep public interface com.qq.e.ads.rewardvideo** {*; }
-keep public interface com.qq.e.ads.rewardvideo2** {*; }
-keep public interface com.qq.e.ads.banner2** {*; }
-keep public interface com.qq.e.comm.adevent** {*; }
#For Verizon integration
-keepclassmembers class com.verizon.ads.edition.BuildConfig {
    static *;
}
-keep public interface com.verizon.ads.interstitialplacement** {*; }
-keep public interface com.verizon.ads.inlineplacement** {*; }
-keep public interface com.verizon.ads.vastcontroller** {*; }
-keep public interface com.verizon.ads.webcontroller** {*; }
#For Vungle integration
-keep public interface com.vungle.warren.PlayAdCallback {*; }
-keep public interface com.vungle.warren.ui.contract** {*; }
-keep public interface com.vungle.warren.ui.view** {*; }
#For Yahoo integration
-keep public interface com.yahoo.ads.interstitialplacement** {*; }
-keep public interface com.yahoo.ads.inlineplacement** {*; }
-keep public interface com.yahoo.ads.vastcontroller** {*; }
-keep public interface com.yahoo.ads.webcontroller** {*; }
#For AndroidX
-keep class androidx.localbroadcastmanager.content.LocalBroadcastManager { *;}
-keep class androidx.recyclerview.widget.RecyclerView { *;}
-keep class androidx.recyclerview.widget.RecyclerView$OnScrollListener { *;}
#For Android
-keep class * extends android.app.Activity

-keep class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }

-dontwarn javax.annotation.Nullable
-keep class com.facebook.jni.** { *; }

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder {
    *;
}

#For Moshi
#keep class name end by "JsonAdapter"
-keepnames class *JsonAdapter
-keep class *JsonAdapter { *; }

-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

-keep class com.tapbi.spark.voiceai.data.model.** {
    #save fields and constructor
    public <fields>;
    public <init>(...);
    *;
}

-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe

#retrofit
-keep interface retrofit2.** { *; }
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepnames class * implements java.io.Serializable

-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Sherpa-ONNX JNI & ONNX Runtime Keep Rules
-keep class com.k2fsa.sherpa.onnx.** { *; }
-keepclassmembers class com.k2fsa.sherpa.onnx.** { *; }
-keep class ai.onnxruntime.** { *; }
-keepclassmembers class ai.onnxruntime.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}