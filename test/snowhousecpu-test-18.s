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

cpy r1, 0x12345678
str r1, sp, 0
lduh r2, sp, 0
lduh r3, sp, 2
ldr r4, sp, 0

sth r2, sp, 4 + STACK_START
sth r3, sp, 6 + STACK_START
ldr r5, sp, 4 + STACK_START

lduh r6, sp, 4
lduh r7, sp, 6

//cpy r1, 0xc486fa11
//cpy r3, 0xcccccccd
//umulw r4, r1, r3
//cpy r7, hi

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
