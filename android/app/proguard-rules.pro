# Retrofit + Gson keep the DTO shapes reachable for reflection.
-keep class com.khatabook.clone.data.remote.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations
-dontwarn okhttp3.**
-dontwarn retrofit2.**
