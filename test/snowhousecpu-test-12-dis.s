
snowhousecpu-test-12.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_loop_0-0x28>:
	...
  10:	01 00 00 01 	add	r1, r0, 1
  14:	02 00 00 02 	add	r2, r0, 2
  18:	10 00 00 03 	add	r3, r0, 16
  1c:	00 00 12 84 	mul	r4, r1, r2
  20:	00 00 42 85 	mul	r5, r4, r2
  24:	00 00 52 86 	mul	r6, r5, r2

00000028 <_loop_0>:
  28:	00 00 12 81 	mul	r1, r1, r2
  2c:	fe ff 32 a1 	bltu	r1, r3, -8
  30:	01 00 20 02 	add	r2, r2, 1
  34:	01 00 30 03 	add	r3, r3, 1
  38:	01 00 40 04 	add	r4, r4, 1
  3c:	01 00 50 05 	add	r5, r5, 1
  40:	01 00 60 06 	add	r6, r6, 1

00000044 <_infin>:
  44:	ff ff 63 a6 	bgeu	r6, r6, -4
  48:	01 00 00 01 	add	r1, r0, 1
  4c:	02 00 00 02 	add	r2, r0, 2
  50:	03 00 00 03 	add	r3, r0, 3
  54:	04 00 00 04 	add	r4, r0, 4
