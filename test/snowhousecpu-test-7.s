.text
//.global _asdf
//_asdf:
//	lsr r4, r5, 31
//	lsr r4, r5, r6
//	cpy r1, _asdf
//	jmp r1
//Lb"_main",
//add(r1, r2, r3),
//add(r2, r0, 12),
//beq(r0, r0, LbR"_my_branch_target"),
//Lb"_my_branch_target",
//beq(r0, r0, LbR"_main"),
//--------
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r1, _irq_handler
cpy ids, r1
cpy r2, 0x1
//cpy ie, r2
lsl r1, r2, 3
cpy r2, 0x1
cpy r3, 0x1000
cpy r4, 0x8
cpy r5, _increment
cpy sp, 0x800
//cpy sp, 0x2000
cpy r7, 0x4
cpy r8, 0x4

cpy r6, 0x20
//cpy r6, 0x4


//str r6, r3, 0x0
//mul r10, r6, r6
//ldr r5, r3, 0x0
//str r5, r3, 0x1000
//ldr r6, r3, 0x1000

//cpy r7, 0x4

_str_loop:
stb r7, r8, 0x0
add r7, r7, -1
//add r8, r8, 0x4
add r8, r8, -1
bne r7, r0, _str_loop

cpy r7, 0x4
//ldr r8, r3, 0x0 //0x1000 //0x0
mul r9, r5, r7
//cpy r5, 0x0

//cpy r2, 0x0
//--------
_push_loop:
str r7, sp, 0
//ldr r8, sp, 0
ldub r8, sp, 0
ldub r4, r8, 0
//ldr r4, r8, 0x0
//add r2, r2, 0x4

//add r12, r0, 1
add r4, r4, 1
add r9, r8, 1
add r7, r7, -1
//add r4, r4, 1
add r9, r9, 1
//sub r7, r7, 1
//bnz r7, _push_loop
bne r7, r0, _push_loop
//add r10, r0, 0x12345678
add r10, r0, 0x1234
add r0, r0, r0
bne r0, r0, 0x12345678
add r11, r0, _pre_loop
jl r0, r11
//add r11, r0, 0x56781234
//add r0, r0, 0x5678
//add r0, r0, 0x1234
//add r0, r0, r0
//add r0, r0, r0
//add r0, r0, r0
//--------
_come_from:
mul r7, r6, r1
udiv r7, r7, r1
umod r8, r6, r1
beq r0, r0, _loop
//beq r0, r0, _push_loop

//.rept 16
////cpy r0, r0
//add r10, r1, 3
//.endr
//--------
//.align 2
//.align 8
//--------
//beq r0, r0, _loop
//cpy r0, r0
_pre_loop:
beq r0, r0, _come_from
_loop:
add r2, r0, 3
ldr r6, r3, 0x0
bl _increment
str r6, r3, 0x4
add r3, r3, 0x4
//sub r1, r1, 0x1
add r1, r1, -1
bl _divmod
//bnz r1, _loop
bne r1, r0, _loop
////--------
_infin:
//bz r0, _infin
beq r0, r0, _infin
////--------
//.rept 16
////cpy r0, r0
//add r11, r1, 3
//.endr
_increment:
add r6, r6, 0x1
jmp lr
////--------
//.rept 16
////cpy r0, r0
//add r12, r1, 3
//.endr
_divmod:
//--------
udiv r7, r6, r1
umod r8, r6, r1
//--------
jmp lr
//.rept 16
////cpy r0, r0
//add r4, r1, 3
//.endr
//--------
_irq_handler:
add r10, r10, 1
ret ira 
cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0




