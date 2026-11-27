# Add project specific ProGuard rules here.
-keepattributes Signature,*Annotation*,EnclosingMethod,InnerClasses

# Keep domain models and DTOs used by Firestore/Room/Gson serialization
-keep class com.riffforge.**.model.** { *; }
-keep class com.riffforge.**.dto.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Credential Manager & Google Identity
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Keep safe on transitive library warnings during R8 minification
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
-dontwarn com.google.errorprone.**