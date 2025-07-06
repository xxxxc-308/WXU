#include <jni.h>

static jstring getNativeString(JNIEnv* env, jobject /*thiz*/) {
    return env->NewStringUTF("Hello from manually bound JNI!");
}

static JNINativeMethod methods[] = {
        {"getNativeString", "()Ljava/lang/String;", (void*)getNativeString}
};

extern "C"
void registerNatives(JNIEnv* env) {
    jclass clazz = env->FindClass("dev/mmrl/Module");
    if (clazz == nullptr) return;
    env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(methods[0]));
}

static JavaVM* gJvm = nullptr;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* /*reserved*/) {
    gJvm = vm;
    return JNI_VERSION_1_6;
}

extern "C"
JavaVM* getJavaVM() {
    return gJvm;
}
