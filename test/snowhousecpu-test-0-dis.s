
snowhousecpu-test-0.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_push_loop-0x54>:
	...
   c:	b0 00 00 01 	add	r1, r0, 176
  10:	07 00 10 70 	cpy	ids, r1
  14:	01 00 00 02 	add	r2, r0, 1
  18:	31 00 20 71 	lsl	r1, r2, 49
  1c:	c1 00 20 73 	lsl	r3, r2, 193
  20:	08 00 00 04 	add	r4, r0, 8
  24:	9c 00 00 05 	add	r5, r0, 156
  28:	00 08 00 0f 	add	sp, r0, 2048
  2c:	00 00 30 96 	ldr	r6, r3, 0
  30:	20 00 00 06 	add	r6, r0, 32
  34:	00 00 31 96 	str	r6, r3, 0
  38:	31 00 60 7a 	lsl	r10, r6, 49
  3c:	00 00 30 95 	ldr	r5, r3, 0
  40:	00 10 31 95 	str	r5, r3, 4096
  44:	00 10 30 96 	ldr	r6, r3, 4096
  48:	04 00 00 07 	add	r7, r0, 4
  4c:	00 10 30 98 	ldr	r8, r3, 4096
  50:	00 00 87 89 	mul	r9, r8, r7

00000054 <_push_loop>:
  54:	00 00 f1 97 	str	r7, sp, 0
  58:	00 00 f0 98 	ldr	r8, sp, 0
  5c:	01 00 80 09 	add	r9, r8, 1
  60:	01 00 90 09 	add	r9, r9, 1
  64:	ff ff 70 07 	add	r7, r7, -1
  68:	fa ff 01 a7 	bne	r7, r0, -24
  6c:	00 00 61 87 	mul	r7, r6, r1
  70:	01 00 71 87 	udiv	r7, r7, r1
  74:	03 00 61 88 	umod	r8, r6, r1
  78:	00 00 00 a0 	beq	r0, r0, 0

0000007c <_loop>:
  7c:	00 00 30 96 	ldr	r6, r3, 0
  80:	06 00 00 bd 	bl	lr, 24
  84:	04 00 31 96 	str	r6, r3, 4
  88:	04 00 30 03 	add	r3, r3, 4
  8c:	ff ff 10 01 	add	r1, r1, -1
  90:	04 00 00 bd 	bl	lr, 16
  94:	f9 ff 01 a1 	bne	r1, r0, -28

00000098 <_infin>:
  98:	ff ff 00 a0 	beq	r0, r0, -4

0000009c <_increment>:
  9c:	01 00 60 06 	add	r6, r6, 1
  a0:	00 00 d6 a0 	jl	r0, lr

000000a4 <_divmod>:
  a4:	01 00 61 87 	udiv	r7, r6, r1
  a8:	03 00 61 88 	umod	r8, r6, r1
  ac:	00 00 d6 a0 	jl	r0, lr

000000b0 <_irq_handler>:
  b0:	01 00 a0 0a 	add	r10, r10, 1
  b4:	0a 00 00 70 	ret	ira
	...
