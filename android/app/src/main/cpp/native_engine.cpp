#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_codesrahul_unifiedcast_MainActivity_getNativeEngineVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = "UnifiedCast Native NDK Engine v1.0.0 (Zero-Cloud Low Latency)";
    return env->NewStringUTF(version.length() ? version.c_str() : "v1.0.0");
}
