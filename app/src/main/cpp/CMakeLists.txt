cmake_minimum_required(VERSION 3.22.1)

project("myproject")

# فعال‌سازی زبان اسمبلی به صورت صریح
enable_language(ASM)

add_library(
        myproject
        SHARED
        native-lib.cpp
        ball_logic.s # اگر نام فایل در پوشه با s کوچک است همین بماند، اما بهتر است به S تغییر دهی
        processPhysicsNeon.s
        collision_neon.s
        split_fruit_physics.s
        out_of_bounds.s
)

find_library(log-lib log)

target_link_libraries(
        myproject
        ${log-lib}
)