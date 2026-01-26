
snowhousecpu-test-13.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_loop_2-0x18>:
	...
  10:	10 00 00 07 	add	r7, r0, 16
  14:	00 00 00 08 	add	r8, r0, 0

00000018 <_loop_2>:
  18:	01 00 70 0c 	add	r12, r7, 1
  1c:	ff ff 70 0b 	add	r11, r7, -1
  20:	fc ff 70 07 	add	r7, r7, -4
  24:	fc ff b4 a8 	blts	r8, r11, -16

00000028 <_infin>:
  28:	ff ff 63 a6 	bgeu	r6, r6, -4
  2c:	01 00 00 01 	add	r1, r0, 1
  30:	02 00 00 02 	add	r2, r0, 2
  34:	03 00 00 03 	add	r3, r0, 3
  38:	04 00 00 04 	add	r4, r0, 4

0000003c <_irq_handler>:
  3c:	0a 00 00 70 	ret	ira
  40:	11 00 00 01 	add	r1, r0, 17
  44:	12 00 00 02 	add	r2, r0, 18
  48:	13 00 00 03 	add	r3, r0, 19
  4c:	14 00 00 04 	add	r4, r0, 20
  50:	15 00 00 05 	add	r5, r0, 21
  54:	16 00 00 06 	add	r6, r0, 22
