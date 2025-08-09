
snowhousecpu-test-gcc:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_start>:
	...
   8:	03 00 00 c0 	add	sp, r0, 196608 // pre #0x3
   c:	00 00 00 0f 
  10:	02 00 00 a0 	beq	r0, r0, 8
	...

0000001c <__cstart>:
  1c:	fc ff f0 0f 	add	sp, sp, -4
  20:	00 00 f1 9d 	str	lr, sp, 0
  24:	b4 03 00 03 	add	r3, r0, 948
  28:	90 03 00 02 	add	r2, r0, 912
  2c:	90 03 00 01 	add	r1, r0, 912
  30:	9b 00 00 bd 	bl	lr, 620
  34:	d1 03 00 03 	add	r3, r0, 977
  38:	00 00 00 02 	add	r2, r0, 0
  3c:	c4 03 00 01 	add	r1, r0, 964
  40:	a3 00 00 bd 	bl	lr, 652
  44:	6a 00 00 bd 	bl	lr, 424
  48:	00 00 00 02 	add	r2, r0, 0
  4c:	00 00 20 01 	add	r1, r2, 0
  50:	47 00 00 bd 	bl	lr, 284

00000054 <.L2>:
  54:	ff ff 00 a0 	beq	r0, r0, -4

00000058 <_init>:
  58:	35 00 00 bd 	bl	lr, 212
  5c:	00 00 d6 a0 	jl	r0, lr

00000060 <_deregister_tm_clones>:
  60:	fc ff f0 0f 	add	sp, sp, -4
  64:	00 00 f1 9d 	str	lr, sp, 0
  68:	d0 03 00 02 	add	r2, r0, 976
  6c:	d0 03 00 01 	add	r1, r0, 976
  70:	05 00 10 a2 	beq	r2, r1, 20
  74:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
  78:	00 00 00 02 
  7c:	00 00 00 03 	add	r3, r0, 0
  80:	01 00 30 a2 	beq	r2, r3, 4
  84:	00 00 26 ad 	jl	lr, r2

00000088 <.L1>:
  88:	00 00 f0 9d 	ldr	lr, sp, 0
  8c:	04 00 f0 0f 	add	sp, sp, 4
  90:	00 00 d6 a0 	jl	r0, lr
	...

000000a0 <_register_tm_clones>:
  a0:	fc ff f0 0f 	add	sp, sp, -4
  a4:	00 00 f1 9d 	str	lr, sp, 0
  a8:	d0 03 00 01 	add	r1, r0, 976
  ac:	d0 03 10 01 	add	r1, r1, 976
  b0:	25 00 10 71 	asr	r1, r1, 37
  b4:	f3 01 10 72 	lsr	r2, r1, 499
  b8:	00 00 21 02 	add	r2, r2, r1
  bc:	15 00 20 72 	asr	r2, r2, 21
  c0:	00 00 00 01 	add	r1, r0, 0
  c4:	05 00 10 a2 	beq	r2, r1, 20
  c8:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  cc:	00 00 00 03 
  d0:	02 00 10 a3 	beq	r3, r1, 8
  d4:	d0 03 00 01 	add	r1, r0, 976
  d8:	00 00 36 ad 	jl	lr, r3

000000dc <.L5>:
  dc:	00 00 f0 9d 	ldr	lr, sp, 0
  e0:	04 00 f0 0f 	add	sp, sp, 4
  e4:	00 00 d6 a0 	jl	r0, lr
  e8:	00 00 00 00 	add	r0, r0, 0

000000ec <___do_global_dtors_aux>:
  ec:	f8 ff f0 0f 	add	sp, sp, -8
  f0:	00 00 f1 97 	str	r7, sp, 0
  f4:	04 00 f1 9d 	str	lr, sp, 4
  f8:	d0 03 00 07 	add	r7, r0, 976
  fc:	00 00 74 92 	ldub	r2, r7, 0
 100:	00 00 00 01 	add	r1, r0, 0
 104:	03 00 11 a2 	bne	r2, r1, 12
 108:	d5 ff ff bd 	bl	lr, -172
 10c:	01 00 00 01 	add	r1, r0, 1
 110:	00 00 77 91 	stb	r1, r7, 0

00000114 <.L9>:
 114:	00 00 f0 9d 	ldr	lr, sp, 0
 118:	04 00 f0 97 	ldr	r7, sp, 4
 11c:	08 00 f0 0f 	add	sp, sp, 8
 120:	00 00 d6 a0 	jl	r0, lr
	...

00000130 <_frame_dummy>:
 130:	fc ff f0 0f 	add	sp, sp, -4
 134:	00 00 f1 9d 	str	lr, sp, 0
 138:	d9 ff ff bd 	bl	lr, -156
 13c:	00 00 f0 9d 	ldr	lr, sp, 0
 140:	04 00 f0 0f 	add	sp, sp, 4
 144:	00 00 d6 a0 	jl	r0, lr
 148:	00 00 00 00 	add	r0, r0, 0

0000014c <_call_frame_dummy>:
 14c:	fc ff f0 0f 	add	sp, sp, -4
 150:	00 00 f1 9d 	str	lr, sp, 0
 154:	00 00 f0 9d 	ldr	lr, sp, 0
 158:	04 00 f0 0f 	add	sp, sp, 4
 15c:	00 00 d6 a0 	jl	r0, lr

00000160 <_fini>:
 160:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

00000170 <_main>:
 170:	a0 03 00 01 	add	r1, r0, 928
 174:	00 00 10 92 	ldr	r2, r1, 0
 178:	b0 03 00 01 	add	r1, r0, 944
 17c:	00 00 10 91 	ldr	r1, r1, 0
 180:	03 00 00 03 	add	r3, r0, 3
 184:	00 00 11 93 	str	r3, r1, 0
 188:	00 00 10 93 	ldr	r3, r1, 0
 18c:	00 00 20 94 	ldr	r4, r2, 0
 190:	00 00 34 03 	add	r3, r3, r4
 194:	00 00 21 93 	str	r3, r2, 0
 198:	04 00 20 02 	add	r2, r2, 4
 19c:	01 00 00 04 	add	r4, r0, 1
 1a0:	08 00 00 06 	add	r6, r0, 8

000001a4 <.L2>:
 1a4:	00 00 10 03 	add	r3, r1, 0
 1a8:	04 00 10 01 	add	r1, r1, 4
 1ac:	00 00 30 93 	ldr	r3, r3, 0
 1b0:	01 00 30 03 	add	r3, r3, 1
 1b4:	00 00 11 93 	str	r3, r1, 0
 1b8:	00 00 10 95 	ldr	r5, r1, 0
 1bc:	00 00 20 93 	ldr	r3, r2, 0
 1c0:	00 00 35 03 	add	r3, r3, r5
 1c4:	00 00 21 93 	str	r3, r2, 0
 1c8:	01 00 40 04 	add	r4, r4, 1
 1cc:	04 00 20 02 	add	r2, r2, 4
 1d0:	f4 ff 61 a4 	bne	r4, r6, -48
 1d4:	fc 03 00 03 	add	r3, r0, 1020
 1d8:	00 00 00 02 	add	r2, r0, 0

000001dc <.L3>:
 1dc:	00 00 30 91 	ldr	r1, r3, 0
 1e0:	fe ff 20 a1 	beq	r1, r2, -8
 1e4:	00 00 00 01 	add	r1, r0, 0
 1e8:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.__libc_init_array:

000001f0 <___libc_init_array>:
 1f0:	f4 ff f0 0f 	add	sp, sp, -12
 1f4:	00 00 f1 97 	str	r7, sp, 0
 1f8:	04 00 f1 98 	str	r8, sp, 4
 1fc:	08 00 f1 9d 	str	lr, sp, 8
 200:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 204:	00 00 00 07 
 208:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 20c:	00 00 70 07 
 210:	25 00 70 77 	asr	r7, r7, 37
 214:	00 00 00 08 	add	r8, r0, 0

00000218 <.L2>:
 218:	12 00 71 a8 	bne	r8, r7, 72
 21c:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 220:	00 00 00 02 
 224:	00 00 00 01 	add	r1, r0, 0
 228:	02 00 10 a2 	beq	r2, r1, 8
 22c:	ff ff 00 c0 	bl	lr, -564 // pre #0xffff
 230:	73 ff ff bd 

00000234 <.L4>:
 234:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 238:	00 00 00 07 
 23c:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 240:	00 00 70 07 
 244:	25 00 70 77 	asr	r7, r7, 37
 248:	00 00 00 08 	add	r8, r0, 0

0000024c <.L5>:
 24c:	0b 00 71 a8 	bne	r8, r7, 44
 250:	00 00 f0 9d 	ldr	lr, sp, 0
 254:	04 00 f0 98 	ldr	r8, sp, 4
 258:	08 00 f0 97 	ldr	r7, sp, 8
 25c:	0c 00 f0 0f 	add	sp, sp, 12
 260:	00 00 d6 a0 	jl	r0, lr

00000264 <.L3>:
 264:	21 00 80 71 	lsl	r1, r8, 33
 268:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 26c:	00 00 10 91 
 270:	00 00 16 ad 	jl	lr, r1
 274:	01 00 80 08 	add	r8, r8, 1
 278:	e7 ff 00 a0 	beq	r0, r0, -100

0000027c <.L6>:
 27c:	21 00 80 71 	lsl	r1, r8, 33
 280:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 284:	00 00 10 91 
 288:	00 00 16 ad 	jl	lr, r1
 28c:	01 00 80 08 	add	r8, r8, 1
 290:	ee ff 00 a0 	beq	r0, r0, -72

Disassembly of section .text.memcpy:

000002a0 <_memcpy>:
 2a0:	00 00 00 04 	add	r4, r0, 0

000002a4 <.L2>:
 2a4:	01 00 41 a3 	bne	r3, r4, 4
 2a8:	00 00 d6 a0 	jl	r0, lr

000002ac <.L3>:
 2ac:	00 00 14 05 	add	r5, r1, r4
 2b0:	00 00 24 06 	add	r6, r2, r4
 2b4:	00 00 64 96 	ldub	r6, r6, 0
 2b8:	00 00 57 96 	stb	r6, r5, 0
 2bc:	01 00 40 04 	add	r4, r4, 1
 2c0:	f8 ff 00 a0 	beq	r0, r0, -32

Disassembly of section .text.memset:

000002d0 <_memset>:
 2d0:	00 00 10 04 	add	r4, r1, 0
 2d4:	00 00 13 03 	add	r3, r1, r3

000002d8 <.L2>:
 2d8:	01 00 31 a4 	bne	r4, r3, 4
 2dc:	00 00 d6 a0 	jl	r0, lr

000002e0 <.L3>:
 2e0:	00 00 47 92 	stb	r2, r4, 0
 2e4:	01 00 40 04 	add	r4, r4, 1
 2e8:	fb ff 00 a0 	beq	r0, r0, -20

Disassembly of section .rodata:

000002f0 <_snowhousecpu_regno_to_class>:
 2f0:	01 00 00 00 	add	r0, r0, 1
 2f4:	01 00 00 00 	add	r0, r0, 1
 2f8:	01 00 00 00 	add	r0, r0, 1
 2fc:	01 00 00 00 	add	r0, r0, 1
 300:	01 00 00 00 	add	r0, r0, 1
 304:	01 00 00 00 	add	r0, r0, 1
 308:	01 00 00 00 	add	r0, r0, 1
 30c:	01 00 00 00 	add	r0, r0, 1
 310:	01 00 00 00 	add	r0, r0, 1
 314:	01 00 00 00 	add	r0, r0, 1
 318:	01 00 00 00 	add	r0, r0, 1
 31c:	01 00 00 00 	add	r0, r0, 1
 320:	01 00 00 00 	add	r0, r0, 1
 324:	02 00 00 00 	add	r0, r0, 2
 328:	03 00 00 00 	add	r0, r0, 3
 32c:	01 00 00 00 	add	r0, r0, 1
 330:	01 00 00 00 	add	r0, r0, 1
 334:	04 00 00 00 	add	r0, r0, 4
	...

00000340 <_snowhousecpu_regno_to_class>:
 340:	01 00 00 00 	add	r0, r0, 1
 344:	01 00 00 00 	add	r0, r0, 1
 348:	01 00 00 00 	add	r0, r0, 1
 34c:	01 00 00 00 	add	r0, r0, 1
 350:	01 00 00 00 	add	r0, r0, 1
 354:	01 00 00 00 	add	r0, r0, 1
 358:	01 00 00 00 	add	r0, r0, 1
 35c:	01 00 00 00 	add	r0, r0, 1
 360:	01 00 00 00 	add	r0, r0, 1
 364:	01 00 00 00 	add	r0, r0, 1
 368:	01 00 00 00 	add	r0, r0, 1
 36c:	01 00 00 00 	add	r0, r0, 1
 370:	01 00 00 00 	add	r0, r0, 1
 374:	02 00 00 00 	add	r0, r0, 2
 378:	03 00 00 00 	add	r0, r0, 3
 37c:	01 00 00 00 	add	r0, r0, 1
 380:	01 00 00 00 	add	r0, r0, 1
 384:	04 00 00 00 	add	r0, r0, 4

Disassembly of section .data:

00000390 <___dso_handle>:
	...

000003a0 <_test_arr_1>:
 3a0:	00 08 00 00 	add	r0, r0, 2048
	...

000003b0 <_test_arr>:
 3b0:	00 04 00 00 	add	r0, r0, 1024

Disassembly of section .fini_array:

000003c0 <___do_global_dtors_aux_fini_array_entry>:
 3c0:	ec 00 00 00 	add	r0, r0, 236

Disassembly of section .comment:

00000000 <_stack-0x30000>:
   0:	47 43 43 3a 	bad
   4:	20 28 47 4e 	xor	fp, r4, r7
   8:	55 29 20 31 	sltu	r1, r2, 10581
   c:	35 2e 30 2e 	bad
  10:	31 20 32 30 	bad
  14:	32 35 30 31 	sltu	r1, r3, 13618
  18:	32 35 20 28 	bad
  1c:	65 78 70 65 	and	r5, r7, 30821
  20:	72 69 6d 65 	and	r5, r6, 26994
  24:	6e 74 61 6c 	and	r12, r6, 29806
  28:	Address 0x28 is out of bounds.

