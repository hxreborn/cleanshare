-keep class eu.hxreborn.cleanshare.CleanShareModule { *; }

-keep,allowobfuscation class eu.hxreborn.cleanshare.hook.** { *; }

-keepattributes RuntimeVisibleAnnotations

-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
    public void onModuleLoaded(...);
    public void onPackageLoaded(...);
    public void onPackageReady(...);
}

-keep class eu.hxreborn.cleanshare.deletion.DeletionProvider { *; }
-keep class eu.hxreborn.cleanshare.deletion.ScreenshotQueryProvider { *; }

-keepclassmembers enum eu.hxreborn.cleanshare.deletion.RequestStatus { *; }
-keepclassmembers enum eu.hxreborn.cleanshare.prefs.DeletionMode { *; }

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

-repackageclasses
-allowaccessmodification
