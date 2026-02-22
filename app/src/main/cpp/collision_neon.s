.text
.align 4
.global check_and_find_fruit_index
check_and_find_fruit_index:
    fmul s2, s2, s2             // radius^2
    mov x3, #0                  // این ریجیستر اندیس فعلی را نگه می‌دارد (Index Counter)
collision_loop:
    cmp x3, x1
    b.ge no_hit                 // اگر به انتهای لیست رسیدیم و چیزی نبود
    add x4, x0, x3, lsl #2      // محاسبات آدرس دهی پیچیده (x3 * 4) - اما ما ۵ تا فلوت داریم
    mov x5, #20
    mul x4, x3, x5
    add x4, x0, x4              // آدرس میوه فعلی در x4
    ldr s10, [x4]               // f.x
    ldr s11, [x4, #4]           // f.y
    fsub s10, s10, s0           // dx
    fsub s11, s11, s1           // dy
    fmul s10, s10, s10
    fmul s11, s11, s11
    fadd s10, s10, s11          // distance squared
    fcmpe s10, s2
    b.mi found_it               // اگر فاصله کمتر از شعاع بود، پریدن به بخش پیدا شده
    add x3, x3, #1              // ایندکس بعدی
    b collision_loop
found_it:
    mov x0, x3                  // برگرداندن ایندکس میوه برخورد شده
    ret
no_hit:
    mov x0, #-1                 // برگرداندن -1 یعنی برخوردی نبود
    ret