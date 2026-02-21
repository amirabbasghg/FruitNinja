// فایل calc.s
.text
.align 2
.global addNumbers
.type addNumbers, %function

addNumbers:
    // در معماری ARM64 (AArch64):
    // عدد اول در رجیستر x0 است
    // عدد دوم در رجیستر x1 است
    add x0, x0, x1    // x0 = x0 + x1
    ret               // نتیجه در x0 برگردانده می‌شود