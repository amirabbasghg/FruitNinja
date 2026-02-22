#include <jni.h>

extern "C" {
void update_all_fruits_neon(float* data, int count , float gameSpeed, float screenWidth);
int check_and_find_fruit_index(float* data, int count, float tx, float ty, float r);
void calculate_split_velocities(float vx, float vy, float* results);
int find_fallen_fruit_index(float* data, int count, float deathLine);
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_myproject_MainActivity_processPhysicsNeonDirect(
        JNIEnv *env, jobject thiz,
        jobject buffer, // دریافت بافر مستقیم
        jint count, jfloat gameSpeed, jfloat screenWidth) {

    // استخراج آدرس فیزیکی RAM (بدون هیچ کپی کردن!)
    float* ptr = (float*) env->GetDirectBufferAddress(buffer);

    // فراخوانی مستقیم کد اسمبلی NEON
    update_all_fruits_neon(ptr, (int)count, gameSpeed, screenWidth);
}

// تابع جدید برخورد
// تابع جدید برخورد با استفاده از Direct Buffer
extern "C" JNIEXPORT jint JNICALL
Java_com_example_myproject_MainActivity_findHitFruitIndexDirect(JNIEnv *env, jobject thiz,
                                                                jobject buffer, jint count,
                                                                jfloat tx, jfloat ty, jfloat r) {
    // استخراج مستقیم پوینتر بدون کپی کردن دیتای آرایه
    float* ptr = (float*) env->GetDirectBufferAddress(buffer);

    // فراخوانی مستقیم تابع اسمبلی که قبلاً داشتی
    // اسمبلی همان پوینتر خام را می‌گیرد و با سرعت بالا جستجو می‌کند
    int index = check_and_find_fruit_index(ptr, (int)count, tx, ty, r);

    return (jint)index;
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

extern "C" JNIEXPORT jint JNICALL
Java_com_example_myproject_MainActivity_findFallenFruitIndexDirect(
        JNIEnv *env,
        jobject thiz,
        jobject buffer,
        jint count,
        jfloat deathLine
) {
    // ۱. استخراج آدرس حافظه مستقیم از ByteBuffer
    float* data = (float*)env->GetDirectBufferAddress(buffer);

    if (data == nullptr) return -1;

    // ۲. فراخوانی تابع فوق‌سریع اسمبلی
    // این تابع کل لیست را در سطح CPU اسکن می‌کند
    int fallenIndex = find_fallen_fruit_index(data, count, deathLine);

    return (jint)fallenIndex;
}