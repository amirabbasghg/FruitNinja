#include <jni.h>

extern "C" {
void update_all_fruits_neon(float* data, int count , float gameSpeed, float screenWidth);
int check_and_find_fruit_index(float* data, int count, float tx, float ty, float r);
void calculate_split_velocities(float vx, float vy, float* results);
}

// تابع آپدیت فیزیک (همان که قبلاً نوشتیم)
extern "C" JNIEXPORT void JNICALL
Java_com_example_myproject_MainActivity_processPhysicsNeon(JNIEnv *env, jobject thiz,
                                                           jfloatArray data, jint count,
                                                           jfloat gameSpeed, jfloat screenWidth) {
    jfloat* ptr = env->GetFloatArrayElements(data, NULL);

    // s0 = gameSpeed, s1 = screenWidth
    update_all_fruits_neon(ptr, (int)count, gameSpeed, screenWidth);

    env->ReleaseFloatArrayElements(data, ptr, 0);
}

// تابع جدید برخورد
extern "C" JNIEXPORT jint JNICALL
Java_com_example_myproject_MainActivity_findHitFruitIndex(JNIEnv *env, jobject thiz,
                                                          jfloatArray data, jint count,
                                                          jfloat tx, jfloat ty, jfloat r) {
    // ۱. دسترسی به دیتای آرایه کاتلین
    jfloat* ptr = env->GetFloatArrayElements(data, NULL);

    // ۲. فراخوانی تابع اسمبلی
    int index = check_and_find_fruit_index(ptr, count, tx, ty, r);

    // ۳. آزاد کردن آرایه
    env->ReleaseFloatArrayElements(data, ptr, 0);

    return index;
}
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_example_myproject_MainActivity_getSplitPhysics(JNIEnv *env, jobject thiz, jfloat vx, jfloat vy) {
    float results[3];

    // فراخوانی تابع اسمبلی
    // نکته: ما آدرس آرایه results را می‌فرستیم تا اسمبلی آن را پر کند
    calculate_split_velocities(vx, vy, results);

    jfloatArray out = env->NewFloatArray(3);
    env->SetFloatArrayRegion(out, 0, 3, results);
    return out;
}
