.text
cpy r0, 0x0
cpy r0, 0x0
cpy r0, 0x0
cpy r1, _irq_handler
cpy ids, r1
cpy r1, 0x80

ldr r2, r1, 0x0
//cpy r3, 8
str r2, r1, 0x4

mul r3, r2, r1

cpy r0, r0
cpy r0, r0
cpy r0, r0

cpy r0, r0
cpy r0, r0
cpy r0, r0
//--------
cpy r7, 0x10
//cpy r8, 0x10 >> 2
cpy r8, 0x0

_loop_2:
add r7, r7, -4
ldr r11, r7, 0x0
bgts r11, r8, _loop_2

//--------
_prep_test_irqs:
cpy r4, r0
cpy r2, 2

cpy r7, 0x100

cpy r6, 0x1
cpy ie, r6

_test_irqs:

//mul r1, r1, r2
add r1, r1, r2
cpy r5, r1
add r4, r4, 0x4

//.rept 16
add r5, r5, 1
//.endr
cpy r5, r4

//.rept 16
//.endr
ldr r3, r5, 0x0

//beq r0, r0, _infin
bltu r3, r7, _test_irqs

_infin:
.rept 8
cpy r0, r0
.endr
beq r0, r0, _infin


_irq_handler:
add r10, r10, 1
ret ira 
//cpy lr, ira
//jmp lr
//jmp ira
.rept 8
cpy r0, r0
.endr
