# LSPosed module entry point
-keep class eu.hxreborn.cleanshare.CleanShareModule { *; }

# UI
-keep class eu.hxreborn.cleanshare.ui.MainActivity { *; }

# Keep all @XposedHooker annotated classes
-keep @io.github.libxposed.api.annotations.XposedHooker class * { *; }

# Keep annotation classes
-keep class io.github.libxposed.api.annotations.** { *; }

# Preserve annotations
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations

# Keep hook methods
-keepclassmembers class * {
    @io.github.libxposed.api.annotations.BeforeInvocation <methods>;
    @io.github.libxposed.api.annotations.AfterInvocation <methods>;
}

# Keep XposedModule subclasses
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onPackageLoaded(...);
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

# Strip Java null checks
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}
