
snowhousecpu-test-12.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_loop_0-0x40>:
	...
  10:	2a 00 00 bd 	bl	lr, 168
  14:	a0 00 00 07 	add	r7, r0, 160
  18:	07 00 70 70 	cpy	ids, r7
  1c:	01 00 00 08 	add	r8, r0, 1
  20:	09 00 80 70 	cpy	ie, r8
  24:	04 00 00 07 	add	r7, r0, 4
  28:	01 00 00 01 	add	r1, r0, 1
  2c:	02 00 00 02 	add	r2, r0, 2
  30:	10 00 00 03 	add	r3, r0, 16
  34:	00 00 12 84 	mul	r4, r1, r2
  38:	00 00 42 85 	mul	r5, r4, r2
  3c:	00 00 52 86 	mul	r6, r5, r2

00000040 <_loop_0>:
  40:	00 00 12 81 	mul	r1, r1, r2
  44:	fe ff 32 a1 	bltu	r1, r3, -8
  48:	01 00 40 04 	add	r4, r4, 1
  4c:	01 00 90 09 	add	r9, r9, 1
  50:	fb ff 71 a4 	bne	r4, r7, -20
  54:	01 00 20 02 	add	r2, r2, 1
  58:	01 00 30 03 	add	r3, r3, 1
  5c:	01 00 40 04 	add	r4, r4, 1
  60:	01 00 50 05 	add	r5, r5, 1
  64:	01 00 60 06 	add	r6, r6, 1
  68:	10 00 00 07 	add	r7, r0, 16
  6c:	00 00 00 08 	add	r8, r0, 0

00000070 <_loop_2>:
  70:	fc ff 70 07 	add	r7, r7, -4
  74:	00 00 70 9b 	ldr	r11, r7, 0
  78:	fd ff b4 a8 	blts	r8, r11, -12
  7c:	10 00 00 07 	add	r7, r0, 16

00000080 <_loop_3>:
  80:	fc ff 70 07 	add	r7, r7, -4
  84:	00 00 70 9a 	ldr	r10, r7, 0
  88:	fd ff 74 a8 	blts	r8, r7, -12

0000008c <_infin>:
  8c:	ff ff 63 a6 	bgeu	r6, r6, -4
  90:	01 00 00 01 	add	r1, r0, 1
  94:	02 00 00 02 	add	r2, r0, 2
  98:	03 00 00 03 	add	r3, r0, 3
  9c:	04 00 00 04 	add	r4, r0, 4

000000a0 <_irq_handler>:
  a0:	0a 00 00 70 	ret	ira
  a4:	11 00 00 01 	add	r1, r0, 17
  a8:	12 00 00 02 	add	r2, r0, 18
  ac:	13 00 00 03 	add	r3, r0, 19
  b0:	14 00 00 04 	add	r4, r0, 20
  b4:	15 00 00 05 	add	r5, r0, 21
  b8:	16 00 00 06 	add	r6, r0, 22

000000bc <_shared_mem_init>:
  bc:	10 00 00 01 	add	r1, r0, 16

000000c0 <_shared_mem_init_loop>:
  c0:	23 00 10 72 	lsr	r2, r1, 2
  c4:	00 00 11 92 	str	r2, r1, 0
  c8:	fc ff 10 01 	add	r1, r1, -4
  cc:	fc ff 01 a1 	bne	r1, r0, -16
  d0:	00 00 d6 a0 	jl	r0, lr
  d4:	21 00 00 01 	add	r1, r0, 33
  d8:	22 00 00 02 	add	r2, r0, 34
  dc:	23 00 00 03 	add	r3, r0, 35
  e0:	24 00 00 04 	add	r4, r0, 36
  e4:	25 00 00 05 	add	r5, r0, 37
  e8:	26 00 00 06 	add	r6, r0, 38
