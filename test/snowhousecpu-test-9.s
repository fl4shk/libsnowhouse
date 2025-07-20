.text
.rept 4
cpy r0, r0
.endr

_start:
cpy r1, 0x1
cpy r2, 0x2
cpy r3, 0x3
//cpy r4, 0x4
cpy r7, r3

_start_loop:
add r7, r7, -1
bne r7, r0, _start_loop

.rept 11
cpy r0, r0
.endr


_loop:
cpy r5, r1
cpy r6, r2
cpy r7, r3
//cpy r8, r4
add r2, r2, -1
_post_loop:
bne r2, r0, _loop


add r1, r1, 1
add r2, r2, 1
add r3, r3, 1
add r4, r4, 1
add r5, r5, 1
add r6, r6, 1
add r7, r7, 1
//_infin:
.rept 8
cpy r0, r0
.endr
beq r0, r0, _start

.rept 20
cpy r8, r8
.endr
