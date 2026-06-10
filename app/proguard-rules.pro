# Add project specific Proguard rules here.
# By default, the Proguard rules in this file are appended to the default Proguard rules.

# Keep JNI / native symbols
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep llama models / Room classes
-keep class com.edgeai.chat.llama.** { *; }
-keep class com.edgeai.chat.database.** { *; }
