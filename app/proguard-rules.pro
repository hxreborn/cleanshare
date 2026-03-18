# LSPosed module entry point
-keep class eu.hxreborn.cleanshare.CleanShareModule { *; }

# Prevent R8 from merging hook classes into app process code (compileOnly API)
-keep,allowobfuscation class eu.hxreborn.cleanshare.hook.** { *; }

# Preserve annotations
-keepattributes RuntimeVisibleAnnotations

# Keep XposedModule subclasses
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
    public void onPackageLoaded(...);
    public void onPackageReady(...);
}

# ContentProvider registered in manifest must survive shrinking
-keep class eu.hxreborn.cleanshare.deletion.DeletionProvider { *; }

# Enum classes used for JSON serialization in DeletionQueue
-keepclassmembers enum eu.hxreborn.cleanshare.deletion.RequestStatus { *; }
-keepclassmembers enum eu.hxreborn.cleanshare.prefs.DeletionMode { *; }

# Kotlin intrinsics - strip null checks in release
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

# Strip debug logging
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Obfuscation
-repackageclasses
-allowaccessmodification
