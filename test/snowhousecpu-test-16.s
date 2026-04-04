.text
_start:
cpy r0, 0x0
cpy r1, 0x0
cpy r2, 0x0
cpy r3, 0x0

cpy r4, 0x0
cpy r5, 0x0
cpy r6, 0x0
cpy r7, 0x0
cpy r8, 0x0
cpy r9, 0x0
cpy r10, 0x0
cpy r11, 0x0
cpy r12, 0x0
cpy lr, 0x0
cpy fp, 0x0

.set STACK_START, 0x1000
cpy sp, STACK_START

cpy r1, _star_star_star_var
ldr r2, r1, 0       // *_star_star_star_var
ldr r3, r2, 0       // **_star_star_star_var
ldr r4, r3, 0       // ***_star_star_star_var
ldr r5, r4, 0

_infin:
beq r0, r0, _infin

add sp, sp, -4
add fp, sp, -4
add lr, sp, -4

_star_star_star_var:
.i32 _star_star_var

_star_star_var:
.i32 _star_var

_star_var:
.i32 _var

_var:
.i32 0x12345678
