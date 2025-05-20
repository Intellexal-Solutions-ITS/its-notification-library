######################################
# GENERAL OPTIMIZATION
######################################
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-overloadaggressively
-useuniqueclassmembernames
-flattenpackagehierarchy

######################################
# KEEP YOUR LIBRARY'S PUBLIC API
######################################
-keep class com.fe.mylibrary.** {
    public *;
}

######################################
# OBFUSCATE INTERNAL CLASSES/METHODS
######################################
# Keep only public interfaces; everything else can be obfuscated
-keep interface com.fe.mylibrary.** { *; }

######################################
# FIREBASE AUTH / MESSAGING / FIRESTORE
######################################
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Needed for Firebase Auth internal reflection
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class * {
    @com.google.firebase.auth.* <methods>;
}

# Keep FirebaseMessagingService subclass
-keep public class * extends com.google.firebase.messaging.FirebaseMessagingService

######################################
# FIRESTORE SERIALIZATION
######################################
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}
-keepattributes *Annotation*

######################################
# OKHTTP (Networking)
######################################
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# If using logging interceptor
-keep class okhttp3.logging.** { *; }

######################################
# ANDROID SECURITY CRYPTO
######################################
-dontwarn androidx.security.crypto.**
-keep class androidx.security.crypto.** { *; }

######################################
# GSON or JSON (if applicable)
######################################
-keep class com.fe.mylibrary.model.** {
    <fields>;
}
-keepattributes *Annotation*

######################################
# ANDROID COMPONENTS
######################################
-keep public class * extends android.app.Service
-keep public class * extends android.app.Activity
-keep public class * extends android.content.BroadcastReceiver

######################################
# LOCAL BROADCAST (if used)
######################################
-keep class androidx.localbroadcastmanager.content.LocalBroadcastManager { *; }

######################################
# OPTIONAL: ENCRYPT NAMES (If using dict)
######################################
#-obfuscationdictionary classnames.txt
#-classobfuscationdictionary classnames.txt
#-packageobfuscationdictionary packagenames.txt
