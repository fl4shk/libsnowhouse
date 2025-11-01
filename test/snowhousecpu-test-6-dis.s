
snowhousecpu-test-6.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_push_loop-0x54>:
	...
   c:	d4 00 00 01 	add	r1, r0, 212
  10:	07 00 10 70 	cpy	ids, r1
  14:	01 00 00 02 	add	r2, r0, 1
  18:	31 00 20 71 	lsl	r1, r2, 3
  1c:	01 00 00 02 	add	r2, r0, 1
  20:	00 10 00 03 	add	r3, r0, 4096
  24:	08 00 00 04 	add	r4, r0, 8
  28:	c0 00 00 05 	add	r5, r0, 192
  2c:	00 08 00 0f 	add	sp, r0, 2048
  30:	20 00 00 06 	add	r6, r0, 32
  34:	00 00 31 96 	str	r6, r3, 0
  38:	00 00 66 8a 	mul	r10, r6, r6
  3c:	00 00 30 95 	ldr	r5, r3, 0
  40:	00 10 31 95 	str	r5, r3, 4096
  44:	00 10 30 96 	ldr	r6, r3, 4096
  48:	04 00 00 07 	add	r7, r0, 4
  4c:	00 10 30 98 	ldr	r8, r3, 4096
  50:	00 00 57 89 	mul	r9, r5, r7

00000054 <_push_loop>:
  54:	00 00 f1 97 	str	r7, sp, 0
  58:	00 00 f0 98 	ldr	r8, sp, 0
  5c:	01 00 80 09 	add	r9, r8, 1
  60:	01 00 40 04 	add	r4, r4, 1
  64:	ff ff 70 07 	add	r7, r7, -1
  68:	01 00 90 09 	add	r9, r9, 1
  6c:	f9 ff 01 a7 	bne	r7, r0, -28
  70:	34 12 00 0a 	add	r10, r0, 4660
  74:	00 00 00 00 	add	r0, r0, 0
  78:	8d 04 00 c0 	bne	r0, r0, 305419768 // pre #0x48d
  7c:	7e 15 01 a0 
  80:	98 00 00 0b 	add	r11, r0, 152
  84:	00 00 b6 a0 	jl	r0, r11

00000088 <_come_from>:
  88:	00 00 61 87 	mul	r7, r6, r1
  8c:	01 00 71 87 	udiv	r7, r7, r1
  90:	03 00 61 88 	umod	r8, r6, r1
  94:	01 00 00 a0 	beq	r0, r0, 4

00000098 <_pre_loop>:
  98:	fb ff 00 a0 	beq	r0, r0, -20

0000009c <_loop>:
  9c:	03 00 00 02 	add	r2, r0, 3
  a0:	00 00 30 96 	ldr	r6, r3, 0
  a4:	06 00 00 bd 	bl	lr, 24
  a8:	04 00 31 96 	str	r6, r3, 4
  ac:	04 00 30 03 	add	r3, r3, 4
  b0:	ff ff 10 01 	add	r1, r1, -1
  b4:	04 00 00 bd 	bl	lr, 16
  b8:	f8 ff 01 a1 	bne	r1, r0, -32

000000bc <_infin>:
  bc:	ff ff 00 a0 	beq	r0, r0, -4

000000c0 <_increment>:
  c0:	01 00 60 06 	add	r6, r6, 1
  c4:	00 00 d6 a0 	jl	r0, lr

000000c8 <_divmod>:
  c8:	01 00 61 87 	udiv	r7, r6, r1
  cc:	03 00 61 88 	umod	r8, r6, r1
  d0:	00 00 d6 a0 	jl	r0, lr

000000d4 <_irq_handler>:
  d4:	01 00 a0 0a 	add	r10, r10, 1
  d8:	0a 00 00 70 	ret	ira
	...
