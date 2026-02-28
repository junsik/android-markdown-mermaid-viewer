# Flexmark library uses java.awt and javax.swing for non-Android parts.
# We must ignore warnings about these missing classes on Android to allow R8/ProGuard to finish.
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.imageio.**

# General Rules to keep Enums consistent for reflection
# Flexmark uses reflection to look up enum constants for configuration options.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Flexmark library classes from being obfuscated or removed
-keep class com.vladsch.flexmark.** { *; }
-keep interface com.vladsch.flexmark.** { *; }
-keep enum com.vladsch.flexmark.** { *; }

# Keep Hilt / Dagger
-keep class dagger.hilt.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

