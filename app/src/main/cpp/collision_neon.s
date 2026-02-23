.text
.align 4
.global check_and_find_fruit_index

check_and_find_fruit_index:
    cmp x1, #0
    ble no_hit

    fmul s2, s2, s2             // R^2

    dup v0.4s, v0.s[0]          // X کاراکتر
    dup v1.4s, v1.s[0]          // Y کاراکتر
    dup v2.4s, v2.s[0]          // R^2

    mov x3, #0                  // Index Counter
    mov x4, x0                  // Current Pointer

    // مقادیر پرش را در ریجیسترها تعریف می‌کنیم
    mov x10, #4                 // برای پریدن از X به Y
    mov x11, #16                // برای پریدن از Y به X میوه بعدی (۱۶ + ۴ = ۲۰)

neon_loop:
    sub x5, x1, x3
    cmp x5, #4
    blt scalar_loop

    // میوه اول
    ld1 {v3.s}[0], [x4], x10    // بارگذاری X، پوینتر ۴ تا جلو می‌رود (روی Y)
    ld1 {v4.s}[0], [x4], x11    // بارگذاری Y، پوینتر ۱۶ تا جلو می‌رود (روی X بعدی)
    // میوه دوم
    ld1 {v3.s}[1], [x4], x10
    ld1 {v4.s}[1], [x4], x11
    // میوه سوم
    ld1 {v3.s}[2], [x4], x10
    ld1 {v4.s}[2], [x4], x11
    // میوه چهارم
    ld1 {v3.s}[3], [x4], x10
    ld1 {v4.s}[3], [x4], x11

    // محاسبات فاصله
    fsub v3.4s, v3.4s, v0.4s
    fsub v4.4s, v4.4s, v1.4s
    fmul v3.4s, v3.4s, v3.4s
    fmla v3.4s, v4.4s, v4.4s

    // بررسی برخورد
    fcmgt v5.4s, v2.4s, v3.4s
    umaxv s6, v5.4s
    fmov w6, s6
    cbz w6, next_neon

    // پیدا کردن لاین دقیق برخورد
    umov w6, v5.s[0]
    cbnz w6, hit_0
    umov w6, v5.s[1]
    cbnz w6, hit_1
    umov w6, v5.s[2]
    cbnz w6, hit_2
    add x0, x3, #3
    ret

hit_0: mov x0, x3; ret
hit_1: add x0, x3, #1; ret
hit_2: add x0, x3, #2; ret

next_neon:
    add x3, x3, #4
    b neon_loop

scalar_loop:
    cmp x3, x1
    b.ge no_hit
    ldr s3, [x4]
    ldr s4, [x4, #4]
    fsub s3, s3, s0
    fsub s4, s4, s1
    fmul s3, s3, s3
    fmadd s3, s4, s4, s3
    fcmpe s3, s2
    b.mi found_scalar
    add x3, x3, #1
    add x4, x4, #20
    b scalar_loop

found_scalar:
    mov x0, x3
    ret

no_hit:
    mov x0, #-1
    ret