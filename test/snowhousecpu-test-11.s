.text
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r2, 0x2
//cpy r5, r2
//cpy r3, 0x400
//cpy r1, 0x200

lsl r3, r2, 1
lsl r4, r2, 1
//str r2, r3, 0x8
////ldr r4, r1, 0x8
//ldr r4, r3, 0x8
////mul r4, r3, r2
////mul r5, r4, r2
//add r5, r4, 1
//add r6, r5, 1
//add r7, r6, 1
//add r8, r7, 1

infin:
//cpy r5, r2
//add r5, r5, 1
beq r0, r0, infin
//beq r0, r0, fwd_br_dst
.rept 200
//beq r0, r0, infin
cpy r1, r2
.endr

fwd_br_dst:
add r1, r1, r5

//infin:
////cpy r5, r2
////add r5, r5, 1
//beq r0, r0, infin
