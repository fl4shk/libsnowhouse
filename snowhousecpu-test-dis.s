
snowhousecpu-test.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_push_loop-0x54>:
	...
   c:	c4 00 00 01 	add	r1, r0, 196
  10:	01 00 00 01 	add	r1, r0, 1
  14:	09 00 10 60 	cpy	ie, r1
  18:	31 00 10 61 	lsl	r1, r1, 3
  1c:	01 00 00 02 	add	r2, r0, 1
  20:	00 10 00 03 	add	r3, r0, 4096
  24:	08 00 00 04 	add	r4, r0, 8
  28:	b0 00 00 05 	add	r5, r0, 176
  2c:	00 08 00 0f 	add	sp, r0, 2048
  30:	20 00 00 06 	add	r6, r0, 32
  34:	00 00 31 86 	str	r6, r3, 0
  38:	00 00 66 70 	mul	r0, r6, r6
  3c:	00 00 30 85 	ldr	r5, r3, 0
  40:	00 10 31 85 	str	r5, r3, 4096
  44:	00 10 30 86 	ldr	r6, r3, 4096
  48:	04 00 00 07 	add	r7, r0, 4
  4c:	00 10 30 88 	ldr	r8, r3, 4096
  50:	00 00 57 79 	mul	r9, r5, r7

00000054 <_push_loop>:
  54:	00 00 f1 87 	str	r7, sp, 0
  58:	00 00 f0 88 	ldr	r8, sp, 0
  5c:	01 00 80 09 	add	r9, r8, 1
  60:	01 00 90 09 	add	r9, r9, 1
  64:	ff ff 70 07 	add	r7, r7, -1
  68:	ec ff 01 97 	bne	r7, r0, -20
  6c:	34 12 00 a0 	add	r0, r0, 305419896 // pre #0x1234
  70:	78 56 00 00 
	...
  80:	00 00 61 77 	mul	r7, r6, r1
  84:	01 00 71 77 	udiv	r7, r7, r1
  88:	03 00 61 78 	umod	r8, r6, r1
  8c:	04 00 00 90 	beq	r0, r0, 4

00000090 <_loop>:
  90:	00 00 30 86 	ldr	r6, r3, 0
  94:	1c 00 d0 9d 	bl	lr, 28
  98:	04 00 31 86 	str	r6, r3, 4
  9c:	04 00 30 03 	add	r3, r3, 4
  a0:	ff ff 10 01 	add	r1, r1, -1
  a4:	14 00 d0 9d 	bl	lr, 20
  a8:	e8 ff 01 91 	bne	r1, r0, -24

000000ac <_infin>:
  ac:	00 00 00 90 	beq	r0, r0, 0

000000b0 <_increment>:
  b0:	01 00 60 06 	add	r6, r6, 1
  b4:	00 00 d6 90 	jl	r0, lr

000000b8 <_divmod>:
  b8:	01 00 61 77 	udiv	r7, r6, r1
  bc:	03 00 61 78 	umod	r8, r6, r1
  c0:	00 00 d6 90 	jl	r0, lr

000000c4 <_irq_handler>:
  c4:	01 00 a0 0a 	add	r10, r10, 1
  c8:	0a 00 00 60 	ret	ira
	...
