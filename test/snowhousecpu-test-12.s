.text
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0

cpy r1, 0x1
cpy r2, 0x2
cpy r3, 0x10
mul r4, r1, r2
mul r5, r4, r2
mul r6, r5, r2

_loop_0:
mul r1, r1, r2
bne r1, r3, _loop_0

add r2, r2, 1
add r3, r3, 1
add r4, r4, 1
add r5, r5, 1
add r6, r6, 1

_infin:
beq r6, r6, _infin


cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
