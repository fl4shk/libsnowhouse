
snowhousecpu-test-8.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_loop-0x14>:
	...
   c:	04 00 00 01 	add	r1, r0, 4
  10:	00 00 00 02 	add	r2, r0, 0

00000014 <_loop>:
  14:	00 00 21 02 	add	r2, r2, r1
  18:	ff ff 10 01 	add	r1, r1, -1
  1c:	f4 ff 01 91 	bne	r1, r0, -12
  20:	ff 33 00 01 	add	r1, r0, 13311
  24:	02 00 00 01 	add	r1, r0, 2
  28:	03 00 00 01 	add	r1, r0, 3
  2c:	04 00 00 01 	add	r1, r0, 4
  30:	05 00 00 01 	add	r1, r0, 5

00000034 <_infin>:
  34:	fc ff 00 90 	beq	r0, r0, -4
