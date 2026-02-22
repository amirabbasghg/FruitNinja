.text
.align 4
.global calculate_split_velocities

// آرگومان‌ها طبق استاندارد ARM64:
// s0: vx (سرعت افقی)
// s1: vy (سرعت عمودی)
// x0: آدرس پوینتر results (جایی که باید ۳ عدد را ذخیره کنیم)

calculate_split_velocities:
    // بارگذاری ثوابت فیزیکی با استفاده از یک رجیستر کمکی (مثل x1) نه x0!
    mov w1, #0x41200000        // مقدار 10.0f
    fmov s2, w1                // s2 = 10.0 (نیروی انفجار افقی)

    mov w1, #0x40a00000        // مقدار 5.0f
    fmov s3, w1                // s3 = 5.0 (نیروی پرتاب به بالا)

    // محاسبات فیزیکی
    fsub s10, s0, s2           // left_vx = vx - 10.0
    fadd s11, s0, s2           // right_vx = vx + 10.0
    fsub s12, s1, s3           // new_vy = vy - 5.0

    // ذخیره نتایج در حافظه (پوینتر خروجی در x0 است)
    // ما از دستور str (Store) استفاده می‌کنیم تا مقادیر را در آرایه بنویسیم
    str s10, [x0]              // results[0] = left_vx
    str s11, [x0, #4]          // results[1] = right_vx
    str s12, [x0, #8]          // results[2] = new_vy

    ret