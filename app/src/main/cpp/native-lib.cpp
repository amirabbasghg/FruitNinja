#include <jni.h>

// --- اعلام حضور توابع اسمبلی ---
// این‌ها دستوراتی هستند که در فایل‌های .s نوشتیم.
// اینجا به C++ می‌گوییم که این توابع وجود دارند و در جای دیگری (اسمبلی) تعریف شده‌اند.
extern "C" {
void update_all_fruits_neon(float* data, int count , float gameSpeed, float screenWidth);
int check_and_find_fruit_index(float* data, int count, float tx, float ty, float r);
void calculate_split_velocities(float vx, float vy, float* results);
int find_fallen_fruit_index_neon(float* data, int count, float deathLine);
}

// ۱. آپدیت فیزیک تمام میوه‌ها به صورت یکجا
extern "C" JNIEXPORT void JNICALL
Java_com_example_myproject_MainActivity_processPhysicsNeonDirect(
        JNIEnv *env, jobject thiz,
        jobject buffer, // دریافت مستقیم بافر از کاتلین
        jint count, jfloat gameSpeed, jfloat screenWidth) {

    // جادوی اصلی اینجاست: استخراج آدرس فیزیکی RAM بدون ذره‌ای کپی کردن دیتا!
    // با این کار، کاتلین و اسمبلی هر دو دارند روی یک "میز کار" مشترک تغییرات رو اعمال می‌کنن.
    float* ptr = (float*) env->GetDirectBufferAddress(buffer);

    // فرستادن پوینتر مستقیم به موتور NEON برای محاسبات موازی
    update_all_fruits_neon(ptr, (int)count, gameSpeed, screenWidth);
}

// ۲. تشخیص برخورد انگشت با میوه (Hit Detection)
extern "C" JNIEXPORT jint JNICALL
Java_com_example_myproject_MainActivity_findHitFruitIndexDirect(JNIEnv *env, jobject thiz,
                                                                jobject buffer, jint count,
                                                                jfloat tx, jfloat ty, jfloat r) {
    // باز هم دسترسی مستقیم به بافر برای سرعت حداکثری
    float* ptr = (float*) env->GetDirectBufferAddress(buffer);

    // فراخوانی تابع جستجوی اسمبلی؛ این بخش مثل یک رادار سریع تمام میوه‌ها رو چک می‌کنه
    int index = check_and_find_fruit_index(ptr, (int)count, tx, ty, r);

    return (jint)index; // برگرداندن شماره میوه برخورد کرده به کاتلین
}

// ۳. محاسبه سرعت قطعات میوه بعد از نصف شدن
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_example_myproject_MainActivity_getSplitPhysics(JNIEnv *env, jobject thiz, jfloat vx, jfloat vy) {
    float results[3]; // آرایه موقت برای دریافت نتایج از اسمبلی

    // فرستادن آدرس results به اسمبلی تا ۳ پارامتر خروجی رو برامون پر کنه
    calculate_split_velocities(vx, vy, results);

    // تبدیل آرایه C++ به آرایه کاتلینی برای برگرداندن به محیط بازی
    jfloatArray out = env->NewFloatArray(3);
    env->SetFloatArrayRegion(out, 0, 3, results);
    return out;
}

// ۴. اسکن موازی برای پیدا کردن میوه‌هایی که از پایین صفحه خارج شده‌اند
extern "C" JNIEXPORT jint JNICALL
Java_com_example_myproject_MainActivity_findFallenFruitIndexDirect(
        JNIEnv *env, jobject thiz, jobject buffer, jint count, jfloat deathLine) {

    // گرفتن آدرس مستقیم تخته‌سیاه (ByteBuffer)
    float* data = (float*)env->GetDirectBufferAddress(buffer);
    if (data == nullptr) return -1;

    // فراخوانی موتور جستجوی موازی NEON برای پیدا کردن اولین میوه سقوط کرده
    return (jint)find_fallen_fruit_index_neon(data, count, deathLine);
}