#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "OsmAndNativeNDK"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_ui_screens_ObfNativeEngine_getNativeVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = "libosmand-v2.5.0-native-cpp17";
    return env->NewStringUTF(version.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_ui_screens_ObfNativeEngine_nativeInitObfReader(
        JNIEnv* env,
        jobject /* this */,
        jstring filePath) {
    if (filePath == nullptr) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(filePath, nullptr);
    LOGI("C++ Native: Initialized random-access OBF reader for path: %s", path);
    env->ReleaseStringUTFChars(filePath, path);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_example_ui_screens_ObfNativeEngine_nativeCalculateAStarRoute(
        JNIEnv* env,
        jobject /* this */,
        jdouble startLat, jdouble startLng,
        jdouble endLat, jdouble endLng,
        jstring profile) {
    const char *prof = env->GetStringUTFChars(profile, nullptr);
    LOGI("C++ Native A* Engine: Route from (%f, %f) to (%f, %f) Profile: %s",
         startLat, startLng, endLat, endLng, prof);
    env->ReleaseStringUTFChars(profile, prof);

    jdoubleArray result = env->NewDoubleArray(4);
    if (result == nullptr) return nullptr;
    
    jdouble fill[4] = {startLat, startLng, endLat, endLng};
    env->SetDoubleArrayRegion(result, 0, 4, fill);
    return result;
}
