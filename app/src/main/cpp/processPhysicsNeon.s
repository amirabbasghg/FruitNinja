.text
.align 4
.global update_all_fruits_neon
update_all_fruits_neon:
    fmov s12, s0               // s12 = speedMultiplier
    ldr w2, =0x3f7c28f6        // DRAG (0.985)
    fmov s10, w2
    ldr w2, =0x40400000
    fmov s11, w2
    ldr w2, =0x40a00000        // ROTATION_STEP (5.0)
    fmov s13, w2
    ldr w2, =0x42a00000        // MARGIN (80.0)
    fmov s14, w2
    fsub s15, s1, s14          // s15 = screenWidth - MARGIN (دیوار راست)
    ldr w2, =0xbf333333        // BOUNCE_DAMPING (-0.7)
    fmov s16, w2
physics_loop:
    cmp x1, #0
    b.le physics_end
    ldp s2, s3, [x0]           // s2=x, s3=y
    ldp s4, s5, [x0, #8]       // s4=vx, s5=vy
    ldr s6, [x0, #16]          // s6=rotation
    fmul s17, s4, s12
    fmul s18, s5, s12
    fadd s2, s2, s17
    fadd s3, s3, s18
    fcmp s2, s14               // if (x < MARGIN)
    b.lt bounce_left
    fcmp s2, s15               // if (x > screenWidth - MARGIN)
    b.gt bounce_right
    b update_physics_end
bounce_left:
    fmov s2, s14               // x = MARGIN
    fmul s4, s4, s16           // vx *= -0.7
    b update_physics_end
bounce_right:
    fmov s2, s15               // x = screenWidth - MARGIN
    fmul s4, s4, s16           // vx *= -0.7
update_physics_end:
    fmul s19, s11, s12         // effective_gravity
    fadd s5, s5, s19           // vy += gravity
    fmul s4, s4, s10           // vx *= drag
    fmul s20, s13, s12         // rot_step * speed
    fadd s6, s6, s20
    stp s2, s3, [x0]
    stp s4, s5, [x0, #8]
    str s6, [x0, #16]
    add x0, x0, #20
    sub x1, x1, #1
    b physics_loop
physics_end:
    ret
.ltorg