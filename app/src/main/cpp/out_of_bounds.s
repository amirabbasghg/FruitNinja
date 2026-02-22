.text
.align 4
.global find_fallen_fruit_index
find_fallen_fruit_index:
    mov x2, #0                  // شمارنده حلقه (Index Counter)
    mov x3, #-1                 // مقدار پیش‌فرض بازگشتی (یعنی هیچ میوه‌ای نیفتاده)
check_loop:
    cmp x2, x1
    b.ge none_found             // اگر به انتهای لیست رسیدیم و چیزی نبود
    mov x4, #20
    mul x5, x2, x4
    add x5, x0, x5              // آدرس شروع میوه فعلی
    ldr s10, [x5, #4]           // بارگذاری مقدار Y در s10
    fcmp s10, s0
    b.gt found_it               // اگر Y > خط مرگ بود، یعنی میوه سقوط کرده
    add x2, x2, #1
    b check_loop
found_it:
    mov x0, x2                  // بازگرداندن ایندکس میوه
    ret
none_found:
    mov x0, #-1                 // بازگرداندن -1 (هیچ میوه‌ای نیفتاده)
    ret