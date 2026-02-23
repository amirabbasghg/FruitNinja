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

// راهنمای درک عملکرد این فایل (مرکز مخابرات JNI):
// -----------------------------------------------------------------
// ۱. این فایل نقش "پل" یا "مترجم" رو بین کاتلین و اسمبلی بازی می‌کنه.
// ۲. دریافت Buffer: کاتلین یک بافر مستقیم (Direct) شامل دیتای میوه‌ها رو می‌فرسته.
// ۳. استخراج آدرس (Pointer): آدرس دقیق فیزیکی اون بافر رو در RAM پیدا می‌کنیم.
// ۴. ارسال آدرس : آدرس رو به اسمبلی می‌دیم.
// ۵. فراخوانی اسمبلی: پوینتر رو به توابع NEON می‌دیم تا محاسبات انجام بشه.

// ۱. تابع آپدیت فیزیک (تحلیل ساختار JNI):
// نام تابع: Java + نام پکیج + نام کلاس + نام متد در کاتلین (یک آدرس پستی برای پیدا شدن توسط اندروید)
// پارامتر JNIEnv *env: همان جعبه‌ابزار ما برای حرف زدن با کاتلین و سیستم‌عامل.
// پارامتر jobject buffer: همان بافر حاوی مختصات میوه‌ها که از کاتلین رسیده.
extern "C" JNIEXPORT void JNICALL
Java_com_example_myproject_MainActivity_processPhysicsNeonDirect(
        JNIEnv *env, jobject thiz,
        jobject buffer, // دریافت مستقیم بافر از کاتلین
        jint count, jfloat gameSpeed, jfloat screenWidth) {

    // استخراج آدرس حافظه محلی (Native Address):
    // متد GetDirectBufferAddress آدرس شروع بلوک حافظه را در RAM بازمی‌گرداند.
    // این رویکرد مانع از کپی شدن داده‌ها (Zero-Copy) بین Heap ماشین مجازی و محیط Native می‌شود.
    float* ptr = (float*) env->GetDirectBufferAddress(buffer);

    // انتقال کنترل به واحد پردازش اسمبلی:
    // ارسال اشاره‌گر (Pointer) مستقیم به تابع اسمبلی جهت اعمال محاسبات برداری بر روی داده‌های فیزیک.
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

    // گرفتن آدرس مستقیم (ByteBuffer)
    float* data = (float*)env->GetDirectBufferAddress(buffer);
    if (data == nullptr) return -1;

    // فراخوانی موتور جستجوی موازی NEON برای پیدا کردن اولین میوه سقوط کرده
    return (jint)find_fallen_fruit_index_neon(data, count, deathLine);
}