
snowhousecpu-test-15.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_loop_ldst_ub-0x1c>:
   0:	00 00 00 00 	add	r0, r0, 0
   4:	00 00 00 01 	add	r1, r0, 0
   8:	00 00 00 02 	add	r2, r0, 0
   c:	00 00 00 03 	add	r3, r0, 0
  10:	00 10 00 04 	add	r4, r0, 4096
  14:	08 00 40 05 	add	r5, r4, 8
  18:	30 00 00 01 	add	r1, r0, 48

0000001c <_loop_ldst_ub>:
  1c:	00 00 00 02 	add	r2, r0, 0
  20:	00 00 00 03 	add	r3, r0, 0
  24:	00 00 40 92 	ldr	r2, r4, 0
  28:	00 00 47 91 	stb	r1, r4, 0
  2c:	00 00 40 93 	ldr	r3, r4, 0
  30:	00 00 44 96 	ldub	r6, r4, 0
  34:	01 00 10 01 	add	r1, r1, 1
  38:	01 00 40 04 	add	r4, r4, 1
  3c:	f7 ff 52 a4 	bltu	r4, r5, -36
  40:	00 08 00 04 	add	r4, r0, 2048
  44:	08 00 40 05 	add	r5, r4, 8
  48:	30 40 00 01 	add	r1, r0, 16432

0000004c <_loop_ldst_uh>:
  4c:	00 00 00 02 	add	r2, r0, 0
  50:	00 00 00 03 	add	r3, r0, 0
  54:	00 00 40 92 	ldr	r2, r4, 0
  58:	00 00 46 91 	sth	r1, r4, 0
  5c:	00 00 40 93 	ldr	r3, r4, 0
  60:	00 00 42 96 	lduh	r6, r4, 0
  64:	01 00 10 01 	add	r1, r1, 1
  68:	02 00 40 04 	add	r4, r4, 2
  6c:	f7 ff 52 a4 	bltu	r4, r5, -36
  70:	00 20 00 04 	add	r4, r0, 8192
  74:	08 00 40 05 	add	r5, r4, 8
  78:	80 00 00 01 	add	r1, r0, 128

0000007c <_loop_ldst_sb>:
  7c:	00 00 00 02 	add	r2, r0, 0
  80:	00 00 00 03 	add	r3, r0, 0
  84:	00 00 40 92 	ldr	r2, r4, 0
  88:	00 00 47 91 	stb	r1, r4, 0
  8c:	00 00 40 93 	ldr	r3, r4, 0
  90:	00 00 45 96 	ldsb	r6, r4, 0
  94:	01 00 10 01 	add	r1, r1, 1
  98:	01 00 40 04 	add	r4, r4, 1
  9c:	f7 ff 52 a4 	bltu	r4, r5, -36
  a0:	00 18 00 04 	add	r4, r0, 6144
  a4:	08 00 40 05 	add	r5, r4, 8
  a8:	00 00 00 c0 	add	r1, r0, 32816 // pre #0x0
  ac:	30 80 00 01 

000000b0 <_loop_ldst_sh>:
  b0:	00 00 00 02 	add	r2, r0, 0
  b4:	00 00 00 03 	add	r3, r0, 0
  b8:	00 00 40 92 	ldr	r2, r4, 0
  bc:	00 00 46 91 	sth	r1, r4, 0
  c0:	00 00 40 93 	ldr	r3, r4, 0
  c4:	00 00 43 96 	ldsh	r6, r4, 0
  c8:	01 00 10 01 	add	r1, r1, 1
  cc:	02 00 40 04 	add	r4, r4, 2
  d0:	f7 ff 52 a4 	bltu	r4, r5, -36

000000d4 <_infin>:
  d4:	ff ff 00 a0 	beq	r0, r0, -4
	...
