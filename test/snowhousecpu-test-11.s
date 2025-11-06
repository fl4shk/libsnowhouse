.text
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r2, 0x1
cpy r3, 0x800
str r2, r3, 0x8
ldr r4, r3, 0x8

infin:
beq r0, r0, infin
.rept 200
//beq r0, r0, infin
cpy r1, r2
.endr
