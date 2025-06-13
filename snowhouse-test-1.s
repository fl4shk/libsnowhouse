.text
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r1, _irq_handler
cpy r1, 0x1
cpy ie, r1
lsl r1, r1, 3
cpy r2, 0x1
cpy r3, 0x1000
cpy r4, 0x1000
cpy r5, _increment
cpy sp, 0x800
cpy r6, 0x20
str r6, r3, 0x0
ldr r5, r3, 0x0
str r5, r4, 0x1000
ldr r6, r4, 0x1000
cpy r7, 0x4
mul r9, r5, r7
_push_loop:
str r7, sp, 0
ldr r8, sp, 0
add r5, r5, 1
add r9, r8, 1
add r9, r9, 1
add r7, r7, -1
bne r7, r0, _push_loop
mul r7, r6, r1
udiv r7, r6, r1
umod r8, r6, r1
_loop:
ldr r6, r3, 0x0
bl _increment
str r6, r3, 0x4
add r3, r3, 0x4
add r1, r1, -0x1
bl _divmod
bne r1, r0, _loop
_infin:
beq r0, r0, _infin
_increment:
add r6, r6, 0x1
jmp lr
_divmod:
udiv r7, r6, r1
umod r8, r6, r1
jmp lr
_irq_handler:
add r10, r10, 1
ret ira 
