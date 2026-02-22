.text
.align 4
.global calculate_split_velocities
calculate_split_velocities:
    mov w1, #0x41200000        // مقدار 10.0f
    fmov s2, w1                // s2 = 10.0 (نیروی انفجار افقی)
    mov w1, #0x40a00000        // مقدار 5.0f
    fmov s3, w1                // s3 = 5.0 (نیروی پرتاب به بالا)
    fsub s10, s0, s2           // left_vx = vx - 10.0
    fadd s11, s0, s2           // right_vx = vx + 10.0
    fsub s12, s1, s3           // new_vy = vy - 5.0
    str s10, [x0]              // results[0] = left_vx
    str s11, [x0, #4]          // results[1] = right_vx
    str s12, [x0, #8]          // results[2] = new_vy
    ret