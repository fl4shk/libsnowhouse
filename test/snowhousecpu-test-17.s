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

cpy r1, 0xc486fa11
cpy r3, 0xcccccccd
umulw r4, r1, r3
cpy r7, hi

_infin:
beq r0, r0, _infin

cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0
