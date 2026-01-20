.text
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r1, 0x80
ldr r2, r1, 0x0
//cpy r3, 8
str r2, r1, 0x4

mul r3, r2, r1

cpy r0, r0
cpy r0, r0
cpy r0, r0

cpy r0, r0
cpy r0, r0
cpy r0, r0
//--------
cpy r7, 0x10
//cpy r8, 0x10 >> 2
cpy r8, 0x0

_loop_2:
add r7, r7, -4
ldr r11, r7, 0x0
bgts r11, r8, _loop_2

_infin:
beq r0, r0, _infin

cpy r0, r0
cpy r0, r0
cpy r0, r0

cpy r0, r0
cpy r0, r0
cpy r0, r0
