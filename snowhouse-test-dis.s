
snowhousecpu-test.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_push_loop-0x50>:
	...
   c:	78 01 00 01 	add	r1, r0, 376
  10:	01 00 00 01 	add	r1, r0, 1
  14:	09 00 10 60 	cpy	ie, r1
  18:	31 00 10 61 	lsl	r1, r1, 3
  1c:	01 00 00 02 	add	r2, r0, 1
  20:	00 10 00 03 	add	r3, r0, 4096
  24:	08 00 00 04 	add	r4, r0, 8
  28:	24 01 00 05 	add	r5, r0, 292
  2c:	00 08 00 0f 	add	sp, r0, 2048
  30:	20 00 00 06 	add	r6, r0, 32
  34:	00 00 31 86 	str	r6, r3, 0
  38:	00 00 66 70 	mul	r0, r6, r6
  3c:	00 00 30 85 	ldr	r5, r3, 0
  40:	00 10 31 85 	str	r5, r3, 4096
  44:	00 10 30 86 	ldr	r6, r3, 4096
  48:	04 00 00 07 	add	r7, r0, 4
  4c:	00 00 57 79 	mul	r9, r5, r7

00000050 <_push_loop>:
  50:	00 00 f1 87 	str	r7, sp, 0
  54:	00 00 f0 88 	ldr	r8, sp, 0
  58:	01 00 80 09 	add	r9, r8, 1
  5c:	01 00 90 09 	add	r9, r9, 1
  60:	ff ff 70 07 	add	r7, r7, -1
  64:	ec ff 01 97 	bne	r7, r0, -20
  68:	00 00 61 77 	mul	r7, r6, r1
  6c:	01 00 61 77 	udiv	r7, r6, r1
  70:	03 00 61 78 	umod	r8, r6, r1
  74:	00 00 00 60 	lsl	r0, r0, r0
  78:	00 00 00 60 	lsl	r0, r0, r0
  7c:	00 00 00 60 	lsl	r0, r0, r0
  80:	00 00 00 60 	lsl	r0, r0, r0
  84:	00 00 00 60 	lsl	r0, r0, r0
  88:	00 00 00 60 	lsl	r0, r0, r0
  8c:	00 00 00 60 	lsl	r0, r0, r0
  90:	00 00 00 60 	lsl	r0, r0, r0
  94:	00 00 00 60 	lsl	r0, r0, r0
  98:	00 00 00 60 	lsl	r0, r0, r0
  9c:	00 00 00 60 	lsl	r0, r0, r0
  a0:	00 00 00 60 	lsl	r0, r0, r0
  a4:	00 00 00 60 	lsl	r0, r0, r0
  a8:	00 00 00 60 	lsl	r0, r0, r0
  ac:	00 00 00 60 	lsl	r0, r0, r0
  b0:	00 00 00 60 	lsl	r0, r0, r0
  b4:	00 00 00 60 	lsl	r0, r0, r0
  b8:	00 00 00 60 	lsl	r0, r0, r0
  bc:	00 00 00 60 	lsl	r0, r0, r0
  c0:	00 00 00 60 	lsl	r0, r0, r0

000000c4 <_loop>:
  c4:	00 00 30 86 	ldr	r6, r3, 0
  c8:	5c 00 d0 9d 	bl	lr, 92
  cc:	04 00 31 86 	str	r6, r3, 4
  d0:	04 00 30 03 	add	r3, r3, 4
  d4:	ff ff 10 01 	add	r1, r1, -1
  d8:	94 00 d0 9d 	bl	lr, 148
  dc:	e8 ff 01 91 	bne	r1, r0, -24

000000e0 <_infin>:
  e0:	00 00 00 90 	beq	r0, r0, 0
	...

00000124 <_increment>:
 124:	01 00 60 06 	add	r6, r6, 1
 128:	00 00 d6 90 	jl	r0, lr
	...

0000016c <_divmod>:
 16c:	01 00 61 77 	udiv	r7, r6, r1
 170:	03 00 61 78 	umod	r8, r6, r1
 174:	00 00 d6 90 	jl	r0, lr

00000178 <_irq_handler>:
 178:	01 00 a0 0a 	add	r10, r10, 1
 17c:	0a 00 00 60 	ret	ira
	...
