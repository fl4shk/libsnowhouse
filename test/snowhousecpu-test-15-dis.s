
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
  28:	fc ff 40 67 	and	r7, r4, -4
  2c:	00 00 70 92 	ldr	r2, r7, 0
  30:	00 00 47 91 	stb	r1, r4, 0
  34:	00 00 70 93 	ldr	r3, r7, 0
  38:	00 00 44 96 	ldub	r6, r4, 0
  3c:	01 00 10 01 	add	r1, r1, 1
  40:	01 00 40 04 	add	r4, r4, 1
  44:	f6 ff 52 a4 	bltu	r4, r5, -40
  48:	00 08 00 04 	add	r4, r0, 2048
  4c:	08 00 40 05 	add	r5, r4, 8
  50:	ee ff 00 c0 	add	r1, r0, -1163216 // pre #0xffee
  54:	30 40 00 01 

00000058 <_loop_ldst_uh>:
  58:	00 00 00 02 	add	r2, r0, 0
  5c:	00 00 00 03 	add	r3, r0, 0
  60:	fc ff 40 67 	and	r7, r4, -4
  64:	00 00 70 92 	ldr	r2, r7, 0
  68:	00 00 46 91 	sth	r1, r4, 0
  6c:	00 00 70 93 	ldr	r3, r7, 0
  70:	00 00 42 96 	lduh	r6, r4, 0
  74:	01 00 10 01 	add	r1, r1, 1
  78:	02 00 40 04 	add	r4, r4, 2
  7c:	f6 ff 52 a4 	bltu	r4, r5, -40
  80:	00 20 00 04 	add	r4, r0, 8192
  84:	08 00 40 05 	add	r5, r4, 8
  88:	55 44 00 c0 	add	r1, r0, 1146447488 // pre #0x4455
  8c:	80 66 00 01 

00000090 <_loop_ldst_sb>:
  90:	00 00 00 02 	add	r2, r0, 0
  94:	00 00 00 03 	add	r3, r0, 0
  98:	fc ff 40 67 	and	r7, r4, -4
  9c:	00 00 70 92 	ldr	r2, r7, 0
  a0:	00 00 47 91 	stb	r1, r4, 0
  a4:	00 00 70 93 	ldr	r3, r7, 0
  a8:	00 00 45 96 	ldsb	r6, r4, 0
  ac:	01 00 10 01 	add	r1, r1, 1
  b0:	01 00 40 04 	add	r4, r4, 1
  b4:	f6 ff 52 a4 	bltu	r4, r5, -40
  b8:	00 18 00 04 	add	r4, r0, 6144
  bc:	08 00 40 05 	add	r5, r4, 8
  c0:	55 44 00 c0 	add	r1, r0, 1146454064 // pre #0x4455
  c4:	30 80 00 01 

000000c8 <_loop_ldst_sh>:
  c8:	00 00 00 02 	add	r2, r0, 0
  cc:	00 00 00 03 	add	r3, r0, 0
  d0:	fc ff 40 67 	and	r7, r4, -4
  d4:	00 00 70 92 	ldr	r2, r7, 0
  d8:	00 00 46 91 	sth	r1, r4, 0
  dc:	00 00 70 93 	ldr	r3, r7, 0
  e0:	00 00 43 96 	ldsh	r6, r4, 0
  e4:	01 00 10 01 	add	r1, r1, 1
  e8:	02 00 40 04 	add	r4, r4, 2
  ec:	f6 ff 52 a4 	bltu	r4, r5, -40

000000f0 <_infin>:
  f0:	ff ff 00 a0 	beq	r0, r0, -4
	...
