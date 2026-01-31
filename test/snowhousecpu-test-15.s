.text
cpy r0, 0x0
cpy r1, 0x0
cpy r2, 0x0
cpy r3, 0x0


//cpy r1, 0x1000
cpy r4, 0x1000
add r5, r4, 0x8

cpy r1, 0x30//0x11223344

_loop_stb:
cpy r2, r0
cpy r3, r0
ldr r2, r4, 0x0
stb r1, r4, 0x0
ldr r3, r4, 0x0
ldub r6, r4, 0x0
add r1, r1, 1
add r4, r4, 0x1
bltu r4, r5, _loop_stb


//cpy r1, 0x1000
cpy r4, 0x800
add r5, r4, 0x8

cpy r1, 0x4030//0x11223344

_loop_sth:
cpy r2, r0
cpy r3, r0
ldr r2, r4, 0x0
sth r1, r4, 0x0
ldr r3, r4, 0x0
lduh r6, r4, 0x0
add r1, r1, 1
add r4, r4, 0x2
bltu r4, r5, _loop_sth


_infin:
beq r0, r0, _infin

.rept 20
cpy r0, 0x0
.endr
