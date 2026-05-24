# Preserve line numbers in stack traces for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Serialization (used for type-safe navigation routes)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.restrusher.partypuzl.**$$serializer { *; }

# Keep enum names (used in DataStore keys and game mode comparisons)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Hilt — keep generated component classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembernames class * {
    @dagger.* <fields>;
    @javax.inject.* <fields>;
    @javax.inject.* <init>(...);
}

# Room — keep entity and DAO classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}
