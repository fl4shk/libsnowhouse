
snowhousecpu-test-7.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_str_loop-0x3c>:
	...
   c:	d8 00 00 01 	add	r1, r0, 216
  10:	07 00 10 70 	cpy	ids, r1
  14:	01 00 00 02 	add	r2, r0, 1
  18:	31 00 20 71 	lsl	r1, r2, 49
  1c:	01 00 00 02 	add	r2, r0, 1
  20:	00 10 00 03 	add	r3, r0, 4096
  24:	08 00 00 04 	add	r4, r0, 8
  28:	c4 00 00 05 	add	r5, r0, 196
  2c:	00 08 00 0f 	add	sp, r0, 2048
  30:	04 00 00 07 	add	r7, r0, 4
  34:	04 00 00 08 	add	r8, r0, 4
  38:	20 00 00 06 	add	r6, r0, 32

0000003c <_str_loop>:
  3c:	00 00 87 97 	stb	r7, r8, 0
  40:	ff ff 70 07 	add	r7, r7, -1
  44:	ff ff 80 08 	add	r8, r8, -1
  48:	fc ff 01 a7 	bne	r7, r0, -16
  4c:	04 00 00 07 	add	r7, r0, 4
  50:	00 00 57 89 	mul	r9, r5, r7

00000054 <_push_loop>:
  54:	00 00 f1 97 	str	r7, sp, 0
  58:	00 00 f4 98 	ldub	r8, sp, 0
  5c:	00 00 84 94 	ldub	r4, r8, 0
  60:	01 00 40 04 	add	r4, r4, 1
  64:	01 00 80 09 	add	r9, r8, 1
  68:	ff ff 70 07 	add	r7, r7, -1
  6c:	01 00 90 09 	add	r9, r9, 1
  70:	f8 ff 01 a7 	bne	r7, r0, -32
  74:	34 12 00 0a 	add	r10, r0, 4660
  78:	00 00 00 00 	add	r0, r0, 0
  7c:	8d 04 00 c0 	bne	r0, r0, 305419764 // pre #0x48d
  80:	7d 15 01 a0 
  84:	9c 00 00 0b 	add	r11, r0, 156
  88:	00 00 b6 a0 	jl	r0, r11

0000008c <_come_from>:
  8c:	00 00 61 87 	mul	r7, r6, r1
  90:	01 00 71 87 	udiv	r7, r7, r1
  94:	03 00 61 88 	umod	r8, r6, r1
  98:	01 00 00 a0 	beq	r0, r0, 4

0000009c <_pre_loop>:
  9c:	fb ff 00 a0 	beq	r0, r0, -20

000000a0 <_loop>:
  a0:	03 00 00 02 	add	r2, r0, 3
  a4:	00 00 30 96 	ldr	r6, r3, 0
  a8:	06 00 00 bd 	bl	lr, 24
  ac:	04 00 31 96 	str	r6, r3, 4
  b0:	04 00 30 03 	add	r3, r3, 4
  b4:	ff ff 10 01 	add	r1, r1, -1
  b8:	04 00 00 bd 	bl	lr, 16
  bc:	f8 ff 01 a1 	bne	r1, r0, -32

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
