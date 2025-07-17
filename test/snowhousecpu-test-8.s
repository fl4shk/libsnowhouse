.text
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r1, 0x4
cpy r2, 0x0
_loop:
add r2, r2, r1
add r1, r1, -1
bne r1, r0, _loop

add r0, r0, r0
add r0, r0, r0
add r0, r0, r0
add r0, r0, r0
add r0, r0, r0

_infin:
beq r0, r0, _infin
