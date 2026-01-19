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
_infin:
beq r0, r0, _infin
