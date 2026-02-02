.text
_start:
cpy r0, 0x0
cpy r1, 0x0
cpy r2, 0x0
cpy r3, 0x0


//cpy r1, 0x1000
cpy r4, 0x1000
add r5, r4, 0x8

cpy r1, 0xffeedd30//0x11223344

_loop_ldst_ub:
cpy r2, r0
cpy r3, r0
and r7, r4, ~(0x3)
ldr r2, r7, 0x0
stb r1, r4, 0x0
ldr r3, r7, 0x0
ldub r6, r4, 0x0
add r1, r1, 1
add r4, r4, 0x1
bltu r4, r5, _loop_ldst_ub


//cpy r1, 0x1000
cpy r4, 0x800
add r5, r4, 0x8

cpy r1, 0xffee4030//0x11223344

_loop_ldst_uh:
cpy r2, r0
cpy r3, r0
and r7, r4, ~(0x3)
ldr r2, r7, 0x0
sth r1, r4, 0x0
ldr r3, r7, 0x0
lduh r6, r4, 0x0
add r1, r1, 1
add r4, r4, 0x2
bltu r4, r5, _loop_ldst_uh


//cpy r1, 0x1000
cpy r4, 0x2000
add r5, r4, 0x8

cpy r1, 0x44556680//0x11223344

_loop_ldst_sb:
cpy r2, r0
cpy r3, r0
and r7, r4, ~(0x3)
ldr r2, r7, 0x0
stb r1, r4, 0x0
ldr r3, r7, 0x0
ldsb r6, r4, 0x0
add r1, r1, 1
add r4, r4, 0x1
bltu r4, r5, _loop_ldst_sb



//cpy r1, 0x1000
cpy r4, 0x1800
add r5, r4, 0x8

cpy r1, 0x44558030//0x11223344

_loop_ldst_sh:
cpy r2, r0
cpy r3, r0
and r7, r4, ~(0x3)
ldr r2, r7, 0x0
sth r1, r4, 0x0
ldr r3, r7, 0x0
ldsh r6, r4, 0x0
add r1, r1, 1
add r4, r4, 0x2
bltu r4, r5, _loop_ldst_sh


_infin:
beq r0, r0, _infin

.rept 20
cpy r0, 0x0
.endr
