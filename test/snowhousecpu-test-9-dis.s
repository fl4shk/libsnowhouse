
snowhousecpu-test-9.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_start-0x10>:
	...

00000010 <_start>:
  10:	01 00 00 01 	add	r1, r0, 1
  14:	02 00 00 02 	add	r2, r0, 2
  18:	03 00 00 03 	add	r3, r0, 3
  1c:	00 00 30 07 	add	r7, r3, 0

00000020 <_start_loop>:
  20:	ff ff 70 07 	add	r7, r7, -1
  24:	fe ff 01 a7 	bne	r7, r0, -8
	...

00000054 <_loop>:
  54:	00 00 10 05 	add	r5, r1, 0
  58:	00 00 20 06 	add	r6, r2, 0
  5c:	00 00 30 07 	add	r7, r3, 0
  60:	ff ff 20 02 	add	r2, r2, -1

00000064 <_post_loop>:
  64:	fb ff 01 a2 	bne	r2, r0, -20
  68:	01 00 10 01 	add	r1, r1, 1
  6c:	01 00 20 02 	add	r2, r2, 1
  70:	01 00 30 03 	add	r3, r3, 1
  74:	01 00 40 04 	add	r4, r4, 1
  78:	01 00 50 05 	add	r5, r5, 1
  7c:	01 00 60 06 	add	r6, r6, 1
  80:	01 00 70 07 	add	r7, r7, 1
	...
  a4:	da ff 00 a0 	beq	r0, r0, -152
  a8:	00 00 80 08 	add	r8, r8, 0
  ac:	00 00 80 08 	add	r8, r8, 0
  b0:	00 00 80 08 	add	r8, r8, 0
  b4:	00 00 80 08 	add	r8, r8, 0
  b8:	00 00 80 08 	add	r8, r8, 0
  bc:	00 00 80 08 	add	r8, r8, 0
  c0:	00 00 80 08 	add	r8, r8, 0
  c4:	00 00 80 08 	add	r8, r8, 0
  c8:	00 00 80 08 	add	r8, r8, 0
  cc:	00 00 80 08 	add	r8, r8, 0
  d0:	00 00 80 08 	add	r8, r8, 0
  d4:	00 00 80 08 	add	r8, r8, 0
  d8:	00 00 80 08 	add	r8, r8, 0
  dc:	00 00 80 08 	add	r8, r8, 0
  e0:	00 00 80 08 	add	r8, r8, 0
  e4:	00 00 80 08 	add	r8, r8, 0
  e8:	00 00 80 08 	add	r8, r8, 0
  ec:	00 00 80 08 	add	r8, r8, 0
  f0:	00 00 80 08 	add	r8, r8, 0
  f4:	00 00 80 08 	add	r8, r8, 0
