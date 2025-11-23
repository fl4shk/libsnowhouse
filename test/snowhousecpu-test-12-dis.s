
snowhousecpu-test-12.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_loop_0-0x3c>:
	...
  10:	9c 00 00 07 	add	r7, r0, 156
  14:	07 00 70 70 	cpy	ids, r7
  18:	01 00 00 08 	add	r8, r0, 1
  1c:	09 00 80 70 	cpy	ie, r8
  20:	04 00 00 07 	add	r7, r0, 4
  24:	01 00 00 01 	add	r1, r0, 1
  28:	02 00 00 02 	add	r2, r0, 2
  2c:	10 00 00 03 	add	r3, r0, 16
  30:	00 00 12 84 	mul	r4, r1, r2
  34:	00 00 42 85 	mul	r5, r4, r2
  38:	00 00 52 86 	mul	r6, r5, r2

0000003c <_loop_0>:
  3c:	00 00 12 81 	mul	r1, r1, r2
  40:	fe ff 32 a1 	bltu	r1, r3, -8
  44:	01 00 40 04 	add	r4, r4, 1
  48:	01 00 90 09 	add	r9, r9, 1
  4c:	fb ff 71 a4 	bne	r4, r7, -20
  50:	01 00 20 02 	add	r2, r2, 1
  54:	01 00 30 03 	add	r3, r3, 1
  58:	01 00 40 04 	add	r4, r4, 1
  5c:	01 00 50 05 	add	r5, r5, 1
  60:	01 00 60 06 	add	r6, r6, 1
  64:	10 00 00 07 	add	r7, r0, 16
  68:	00 00 00 08 	add	r8, r0, 0

0000006c <_loop_2>:
  6c:	fc ff 70 07 	add	r7, r7, -4
  70:	00 00 70 9b 	ldr	r11, r7, 0
  74:	fd ff b4 a8 	blts	r8, r11, -12
  78:	10 00 00 07 	add	r7, r0, 16

0000007c <_loop_3>:
  7c:	fc ff 70 07 	add	r7, r7, -4
  80:	00 00 70 9a 	ldr	r10, r7, 0
  84:	fd ff 74 a8 	blts	r8, r7, -12

00000088 <_infin>:
  88:	ff ff 63 a6 	bgeu	r6, r6, -4
  8c:	01 00 00 01 	add	r1, r0, 1
  90:	02 00 00 02 	add	r2, r0, 2
  94:	03 00 00 03 	add	r3, r0, 3
  98:	04 00 00 04 	add	r4, r0, 4

0000009c <_irq_handler>:
  9c:	0a 00 00 70 	ret	ira
  a0:	11 00 00 01 	add	r1, r0, 17
  a4:	12 00 00 02 	add	r2, r0, 18
  a8:	13 00 00 03 	add	r3, r0, 19
  ac:	14 00 00 04 	add	r4, r0, 20
  b0:	15 00 00 05 	add	r5, r0, 21
  b4:	16 00 00 06 	add	r6, r0, 22
