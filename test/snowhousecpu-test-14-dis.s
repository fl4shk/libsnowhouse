
snowhousecpu-test-14.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_loop_2-0x44>:
	...
   c:	a4 00 00 01 	add	r1, r0, 164
  10:	07 00 10 70 	cpy	ids, r1
  14:	80 00 00 01 	add	r1, r0, 128
  18:	00 00 10 92 	ldr	r2, r1, 0
  1c:	04 00 11 92 	str	r2, r1, 4
  20:	00 00 21 83 	mul	r3, r2, r1
	...
  3c:	10 00 00 07 	add	r7, r0, 16
  40:	00 00 00 08 	add	r8, r0, 0

00000044 <_loop_2>:
  44:	fc ff 70 07 	add	r7, r7, -4
  48:	00 00 70 9b 	ldr	r11, r7, 0
  4c:	fd ff b4 a8 	blts	r8, r11, -12

00000050 <_prep_test_irqs>:
  50:	00 00 00 04 	add	r4, r0, 0
  54:	02 00 00 02 	add	r2, r0, 2
  58:	00 01 00 07 	add	r7, r0, 256
  5c:	01 00 00 06 	add	r6, r0, 1
  60:	09 00 60 70 	cpy	ie, r6

00000064 <_test_irqs>:
  64:	00 00 12 01 	add	r1, r1, r2
  68:	00 00 10 05 	add	r5, r1, 0
  6c:	04 00 40 04 	add	r4, r4, 4
  70:	01 00 50 05 	add	r5, r5, 1
  74:	00 00 40 05 	add	r5, r4, 0
  78:	00 00 50 93 	ldr	r3, r5, 0
  7c:	f9 ff 72 a3 	bltu	r3, r7, -28

00000080 <_infin>:
	...
  a0:	f7 ff 00 a0 	beq	r0, r0, -36

000000a4 <_irq_handler>:
  a4:	01 00 a0 0a 	add	r10, r10, 1
  a8:	0a 00 00 70 	ret	ira
	...
