.text
.align 4
.global find_fallen_fruit_index_neon

// x0: آدرس بافر (float*)
// x1: تعداد کل میوه‌ها (count)
// s0: خط مرگ (deathLine)

find_fallen_fruit_index_neon:
    // ۱. تکثیر خط مرگ در تمام ۴ کانال v0
    // s0 در واقع همان v0.s[0] است
    dup v0.4s, v0.s[0]

    mov x2, #0                  // ایندکس فعلی میوه

    cmp x1, #4
    b.lt serial_check

parallel_loop:
    sub x3, x1, x2
    cmp x3, #4
    b.lt serial_check

    // محاسبه آدرس شروع میوه فعلی
    mov x4, #20
    mul x5, x2, x4
    add x5, x0, x5

    // ۲. بارگذاری Y چهار میوه در رجیسترهای جداگانه
    ldr s1, [x5, #4]            // میوه ۱ -> v1.s[0]
    ldr s2, [x5, #24]           // میوه ۲ -> v2.s[0]
    ldr s3, [x5, #44]           // میوه ۳ -> v3.s[0]
    ldr s4, [x5, #64]           // میوه ۴ -> v4.s[0]

    // ۳. بسته‌بندی (Packing) درون v1 به روش صحیح:
    // s1 از قبل در v1.s[0] هست، پس بقیه را به v1 منتقل می‌کنیم
    mov v1.s[1], v2.s[0]
    mov v1.s[2], v3.s[0]
    mov v1.s[3], v4.s[0]

    // ۴. مقایسه موازی: آیا Yها > خط مرگ هستند؟
    fcmgt v5.4s, v1.4s, v0.4s

    // ۵. بررسی اینکه آیا حداقل یکی از بیت‌ها ۱ شده است؟
    umaxv s6, v5.4s
    fmov w6, s6
    cmp w6, #0
    b.ne find_exact_index

    add x2, x2, #4
    b parallel_loop

serial_check:
    cmp x2, x1
    b.ge none_found

    mov x4, #20
    mul x5, x2, x4
    add x5, x0, x5
    ldr s10, [x5, #4]

    fcmp s10, s0
    b.gt found_it

    add x2, x2, #1
    b serial_check

find_exact_index:
    // اگر در دسته ۴تایی برخورد داشتیم، به بخش سریال می‌رویم تا شماره دقیق را پیدا کنیم
    b serial_check

found_it:
    mov x0, x2                  // شماره میوه را برگردان
    ret

none_found:
    mov x0, #-1                 // هیچ میوه‌ای نیفتاده
    ret