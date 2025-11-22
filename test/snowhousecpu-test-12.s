.text
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0

cpy r7, _irq_handler
cpy ids, r7
cpy r8, 0x1
cpy ie, r8
cpy r7, 0x4


cpy r1, 0x1
cpy r2, 0x2
cpy r3, 0x10
mul r4, r1, r2
mul r5, r4, r2
mul r6, r5, r2

_loop_0:
mul r1, r1, r2
//bne r1, r3, _loop_0
bltu r1, r3, _loop_0

add r4, r4, 1
//add r5, r4, 1
add r9, r9, 1
bne r4, r7, _loop_0

add r2, r2, 1
add r3, r3, 1
add r4, r4, 1
add r5, r5, 1
add r6, r6, 1

_infin:
//beq r6, r6, _infin
bleu r6, r6, _infin


cpy r1, 0x1
cpy r2, 0x2
cpy r3, 0x3
cpy r4, 0x4


_irq_handler:
//add r10, r10, 0x1
ret ira

cpy r1, 0x1
cpy r2, 0x2
cpy r3, 0x3
cpy r4, 0x4
cpy r5, 0x5
cpy r6, 0x6
