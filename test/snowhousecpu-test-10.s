.text
.global _start
_start:
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
//cpy r3, 1
cpy r1, 4
//cpy r2, 4

_loop:
add r1, r1, -1
//slts r2, r1, r0			//	if r1.asSInt < 0.asSInt:
slts r2, r1, 0x0			//	if r1.asSInt < 0.asSInt:
//lsr r2, r2, r3
//cpy r2, r1
//bne r2, r0, _loop			//		goto _loop
beq r2, r0, _loop			//		goto _loop


.rept 15
//cpy r1, r2
add r3, r3, 1
.endr


_test_add:
cpy r5, _infin
jmp r5

//_infin:
//beq r0, r0, _infin
.rept 2048
cpy r5, r3
.endr

_infin:
beq r0, r0, _infin
