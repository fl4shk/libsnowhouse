
snowhousecpu-test.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_push_loop-0x3c>:
	...
   8:	94 00 00 01 	add	r1, r0, 148
   c:	07 00 10 60 	cpy	ids, r1
  10:	01 00 00 01 	add	r1, r0, 1
  14:	09 00 10 60 	cpy	ie, r1
  18:	31 00 10 61 	lsl	r1, r1, 3
  1c:	01 00 00 02 	add	r2, r0, 1
  20:	00 10 00 03 	add	r3, r0, 4096
  24:	08 00 00 04 	add	r4, r0, 8
  28:	80 00 00 05 	add	r5, r0, 128
  2c:	00 08 00 0f 	add	sp, r0, 2048
  30:	20 00 00 06 	add	r6, r0, 32
  34:	00 00 31 86 	str	r6, r3, 0
  38:	04 00 00 07 	add	r7, r0, 4

0000003c <_push_loop>:
  3c:	00 00 f1 87 	str	r7, sp, 0
  40:	00 00 f0 88 	ldr	r8, sp, 0
  44:	01 00 80 09 	add	r9, r8, 1
  48:	01 00 90 09 	add	r9, r9, 1
  4c:	ff ff 70 07 	add	r7, r7, -1
  50:	ec ff 01 97 	bne	r7, r0, -20
  54:	00 00 61 77 	mul	r7, r6, r1
  58:	01 00 61 77 	udiv	r7, r6, r1
  5c:	03 00 61 78 	umod	r8, r6, r1

00000060 <_loop>:
  60:	00 00 30 86 	ldr	r6, r3, 0
  64:	1c 00 d0 9d 	bl	lr, 28
  68:	04 00 31 86 	str	r6, r3, 4
  6c:	04 00 30 03 	add	r3, r3, 4
  70:	ff ff 10 01 	add	r1, r1, -1
  74:	14 00 d0 9d 	bl	lr, 20
  78:	e8 ff 01 91 	bne	r1, r0, -24

0000007c <_infin>:
  7c:	00 00 00 90 	beq	r0, r0, 0

00000080 <_increment>:
  80:	01 00 60 06 	add	r6, r6, 1
  84:	00 00 d6 90 	jl	r0, lr

00000088 <_divmod>:
  88:	01 00 61 77 	udiv	r7, r6, r1
  8c:	03 00 61 78 	umod	r8, r6, r1
  90:	00 00 d6 90 	jl	r0, lr

00000094 <_irq_handler>:
  94:	01 00 a0 0a 	add	r10, r10, 1
  98:	0a 00 00 60 	ret	ira
	...
