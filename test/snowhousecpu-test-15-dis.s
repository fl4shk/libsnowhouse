
snowhousecpu-test-15.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_start>:
   0:	00 00 00 00 	add	r0, r0, 0
   4:	00 00 00 01 	add	r1, r0, 0
   8:	00 00 00 02 	add	r2, r0, 0
   c:	00 00 00 03 	add	r3, r0, 0
  10:	00 10 00 04 	add	r4, r0, 4096
  14:	08 00 40 05 	add	r5, r4, 8
  18:	ee ff 00 c0 	add	r1, r0, -1123024 // pre #0xffee
  1c:	30 dd 00 01 

00000020 <_loop_ldst_ub>:
  20:	00 00 00 02 	add	r2, r0, 0
  24:	00 00 00 03 	add	r3, r0, 0
  28:	00 00 40 92 	ldr	r2, r4, 0
  2c:	00 00 47 91 	stb	r1, r4, 0
  30:	00 00 40 93 	ldr	r3, r4, 0
  34:	00 00 44 96 	ldub	r6, r4, 0
  38:	01 00 10 01 	add	r1, r1, 1
  3c:	01 00 40 04 	add	r4, r4, 1
  40:	f7 ff 52 a4 	bltu	r4, r5, -36
  44:	00 08 00 04 	add	r4, r0, 2048
  48:	08 00 40 05 	add	r5, r4, 8
  4c:	ee ff 00 c0 	add	r1, r0, -1163216 // pre #0xffee
  50:	30 40 00 01 

00000054 <_loop_ldst_uh>:
  54:	00 00 00 02 	add	r2, r0, 0
  58:	00 00 00 03 	add	r3, r0, 0
  5c:	00 00 40 92 	ldr	r2, r4, 0
  60:	00 00 46 91 	sth	r1, r4, 0
  64:	00 00 40 93 	ldr	r3, r4, 0
  68:	00 00 42 96 	lduh	r6, r4, 0
  6c:	01 00 10 01 	add	r1, r1, 1
  70:	02 00 40 04 	add	r4, r4, 2
  74:	f7 ff 52 a4 	bltu	r4, r5, -36
  78:	00 20 00 04 	add	r4, r0, 8192
  7c:	08 00 40 05 	add	r5, r4, 8
  80:	55 44 00 c0 	add	r1, r0, 1146447488 // pre #0x4455
  84:	80 66 00 01 

00000088 <_loop_ldst_sb>:
  88:	00 00 00 02 	add	r2, r0, 0
  8c:	00 00 00 03 	add	r3, r0, 0
  90:	00 00 40 92 	ldr	r2, r4, 0
  94:	00 00 47 91 	stb	r1, r4, 0
  98:	00 00 40 93 	ldr	r3, r4, 0
  9c:	00 00 45 96 	ldsb	r6, r4, 0
  a0:	01 00 10 01 	add	r1, r1, 1
  a4:	01 00 40 04 	add	r4, r4, 1
  a8:	f7 ff 52 a4 	bltu	r4, r5, -36
  ac:	00 18 00 04 	add	r4, r0, 6144
  b0:	08 00 40 05 	add	r5, r4, 8
  b4:	55 44 00 c0 	add	r1, r0, 1146454064 // pre #0x4455
  b8:	30 80 00 01 

000000bc <_loop_ldst_sh>:
  bc:	00 00 00 02 	add	r2, r0, 0
  c0:	00 00 00 03 	add	r3, r0, 0
  c4:	00 00 40 92 	ldr	r2, r4, 0
  c8:	00 00 46 91 	sth	r1, r4, 0
  cc:	00 00 40 93 	ldr	r3, r4, 0
  d0:	00 00 43 96 	ldsh	r6, r4, 0
  d4:	01 00 10 01 	add	r1, r1, 1
  d8:	02 00 40 04 	add	r4, r4, 2
  dc:	f7 ff 52 a4 	bltu	r4, r5, -36

000000e0 <_infin>:
  e0:	ff ff 00 a0 	beq	r0, r0, -4
	...
