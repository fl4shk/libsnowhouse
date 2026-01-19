
snowhousecpu-test-13.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_push_loop-0x6c>:
	...
   c:	d8 00 00 01 	add	r1, r0, 216
  10:	07 00 10 70 	cpy	ids, r1
  14:	01 00 00 02 	add	r2, r0, 1
  18:	31 00 20 71 	lsl	r1, r2, 3
  1c:	c1 00 20 73 	lsl	r3, r2, 12
  20:	08 00 00 04 	add	r4, r0, 8
  24:	c4 00 00 05 	add	r5, r0, 196
  28:	00 08 00 0f 	add	sp, r0, 2048
  2c:	00 00 30 96 	ldr	r6, r3, 0
  30:	00 00 00 00 	add	r0, r0, 0
  34:	20 00 00 06 	add	r6, r0, 32
  38:	00 00 31 96 	str	r6, r3, 0
  3c:	00 00 00 00 	add	r0, r0, 0
  40:	31 00 60 7a 	lsl	r10, r6, 3
  44:	00 00 00 00 	add	r0, r0, 0
  48:	00 00 30 95 	ldr	r5, r3, 0
  4c:	00 00 00 00 	add	r0, r0, 0
  50:	00 10 31 95 	str	r5, r3, 4096
  54:	00 00 00 00 	add	r0, r0, 0
  58:	00 10 30 96 	ldr	r6, r3, 4096
  5c:	04 00 00 07 	add	r7, r0, 4
  60:	00 10 30 98 	ldr	r8, r3, 4096
  64:	00 00 00 00 	add	r0, r0, 0
  68:	00 00 87 89 	mul	r9, r8, r7

0000006c <_push_loop>:
  6c:	00 00 f1 97 	str	r7, sp, 0
  70:	00 00 00 00 	add	r0, r0, 0
  74:	00 00 f0 98 	ldr	r8, sp, 0
  78:	00 00 00 00 	add	r0, r0, 0
  7c:	01 00 80 09 	add	r9, r8, 1
  80:	01 00 90 09 	add	r9, r9, 1
  84:	ff ff 70 07 	add	r7, r7, -1
  88:	f8 ff 01 a7 	bne	r7, r0, -32
  8c:	00 00 61 87 	mul	r7, r6, r1
  90:	01 00 71 87 	udiv	r7, r7, r1
  94:	03 00 61 88 	umod	r8, r6, r1
  98:	00 00 00 a0 	beq	r0, r0, 0

0000009c <_loop>:
  9c:	00 00 30 96 	ldr	r6, r3, 0
  a0:	00 00 00 00 	add	r0, r0, 0
  a4:	07 00 00 bd 	bl	lr, 28
  a8:	04 00 31 96 	str	r6, r3, 4
  ac:	00 00 00 00 	add	r0, r0, 0
  b0:	04 00 30 03 	add	r3, r3, 4
  b4:	ff ff 10 01 	add	r1, r1, -1
  b8:	04 00 00 bd 	bl	lr, 16
  bc:	f7 ff 01 a1 	bne	r1, r0, -36

000000c0 <_infin>:
  c0:	ff ff 00 a0 	beq	r0, r0, -4

000000c4 <_increment>:
  c4:	01 00 60 06 	add	r6, r6, 1
  c8:	00 00 d6 a0 	jl	r0, lr

000000cc <_divmod>:
  cc:	01 00 61 87 	udiv	r7, r6, r1
  d0:	03 00 61 88 	umod	r8, r6, r1
  d4:	00 00 d6 a0 	jl	r0, lr

000000d8 <_irq_handler>:
  d8:	01 00 a0 0a 	add	r10, r10, 1
  dc:	0a 00 00 70 	ret	ira
	...
