
snowhousecpu-test-14.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_loop_2-0x3c>:
	...
   c:	80 00 00 01 	add	r1, r0, 128
  10:	00 00 10 92 	ldr	r2, r1, 0
  14:	04 00 11 92 	str	r2, r1, 4
  18:	00 00 21 83 	mul	r3, r2, r1
	...
  34:	10 00 00 07 	add	r7, r0, 16
  38:	00 00 00 08 	add	r8, r0, 0

0000003c <_loop_2>:
  3c:	fc ff 70 07 	add	r7, r7, -4
  40:	00 00 70 9b 	ldr	r11, r7, 0
  44:	fd ff b4 a8 	blts	r8, r11, -12

00000048 <_infin>:
  48:	ff ff 00 a0 	beq	r0, r0, -4
	...
