
snowhousecpu-test-12.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_loop_0-0x3c>:
	...
  10:	6c 00 00 07 	add	r7, r0, 108
  14:	07 00 70 70 	cpy	ids, r7
  18:	01 00 00 08 	add	r8, r0, 1
  1c:	09 00 80 70 	cpy	ie, r8
  20:	00 00 00 07 	add	r7, r0, 0
  24:	01 00 00 01 	add	r1, r0, 1
  28:	02 00 00 02 	add	r2, r0, 2
  2c:	10 00 00 03 	add	r3, r0, 16
  30:	00 00 12 84 	mul	r4, r1, r2
  34:	00 00 42 85 	mul	r5, r4, r2
  38:	00 00 52 86 	mul	r6, r5, r2

0000003c <_loop_0>:
  3c:	00 00 12 81 	mul	r1, r1, r2
  40:	fe ff 32 a1 	bltu	r1, r3, -8
  44:	01 00 20 02 	add	r2, r2, 1
  48:	01 00 30 03 	add	r3, r3, 1
  4c:	01 00 40 04 	add	r4, r4, 1
  50:	01 00 50 05 	add	r5, r5, 1
  54:	01 00 60 06 	add	r6, r6, 1

00000058 <_infin>:
  58:	ff ff 63 a6 	bgeu	r6, r6, -4
  5c:	01 00 00 01 	add	r1, r0, 1
  60:	02 00 00 02 	add	r2, r0, 2
  64:	03 00 00 03 	add	r3, r0, 3
  68:	04 00 00 04 	add	r4, r0, 4

0000006c <_irq_handler>:
  6c:	0a 00 00 70 	ret	ira
  70:	01 00 00 01 	add	r1, r0, 1
  74:	02 00 00 02 	add	r2, r0, 2
  78:	03 00 00 03 	add	r3, r0, 3
  7c:	04 00 00 04 	add	r4, r0, 4
  80:	05 00 00 05 	add	r5, r0, 5
  84:	06 00 00 06 	add	r6, r0, 6
