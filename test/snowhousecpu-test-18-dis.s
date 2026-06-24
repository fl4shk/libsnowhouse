
snowhousecpu-test-18.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_start>:
   0:	00 00 00 00 	add	r0, r0, 0
   4:	00 00 00 01 	add	r1, r0, 0
   8:	00 00 00 02 	add	r2, r0, 0
   c:	00 00 00 03 	add	r3, r0, 0
  10:	00 00 00 04 	add	r4, r0, 0
  14:	00 00 00 05 	add	r5, r0, 0
  18:	00 00 00 06 	add	r6, r0, 0
  1c:	00 00 00 07 	add	r7, r0, 0
  20:	00 00 00 08 	add	r8, r0, 0
  24:	00 00 00 09 	add	r9, r0, 0
  28:	00 00 00 0a 	add	r10, r0, 0
  2c:	00 00 00 0b 	add	r11, r0, 0
  30:	00 00 00 0c 	add	r12, r0, 0
  34:	00 00 00 0d 	add	lr, r0, 0
  38:	00 00 00 0e 	add	fp, r0, 0
  3c:	00 10 00 0f 	add	sp, r0, 4096
  40:	34 12 00 c0 	add	r1, r0, 305419896 /* pre #0x1234 */
  44:	78 56 00 01 
  48:	00 00 f1 91 	str	r1, sp, 0
  4c:	00 00 f2 92 	lduh	r2, sp, 0
  50:	02 00 f2 93 	lduh	r3, sp, 2
  54:	00 00 f0 94 	ldr	r4, sp, 0
  58:	04 10 f6 92 	sth	r2, sp, 4100
  5c:	06 10 f6 93 	sth	r3, sp, 4102
  60:	04 10 f0 95 	ldr	r5, sp, 4100
  64:	04 00 f2 96 	lduh	r6, sp, 4
  68:	06 00 f2 97 	lduh	r7, sp, 6

0000006c <_infin>:
  6c:	ff ff 00 a0 	beq	r0, r0, -4 /* dst_pc=0x6c */
	...
