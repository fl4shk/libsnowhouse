
snowhousecpu-framebuffer-demo.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_my_text_start>:
	...
  10:	00 00 00 01 	add	r1, r0, 0
  14:	00 00 00 02 	add	r2, r0, 0
  18:	00 00 00 03 	add	r3, r0, 0
  1c:	00 00 00 04 	add	r4, r0, 0
  20:	00 00 00 05 	add	r5, r0, 0
  24:	00 00 00 06 	add	r6, r0, 0
  28:	00 00 00 07 	add	r7, r0, 0
  2c:	00 00 00 08 	add	r8, r0, 0
  30:	00 00 00 09 	add	r9, r0, 0
  34:	00 00 00 0a 	add	r10, r0, 0
  38:	00 00 00 0b 	add	r11, r0, 0
  3c:	00 00 00 0c 	add	r12, r0, 0
  40:	00 00 00 0d 	add	lr, r0, 0
  44:	00 00 00 0e 	add	fp, r0, 0
  48:	00 00 00 0f 	add	sp, r0, 0
  4c:	74 00 00 b0 	bl	r0, 464

00000050 <_deregister_tm_clones>:
  50:	fc ff f0 0f 	add	sp, sp, -4
  54:	00 00 f1 9d 	str	lr, sp, 0
  58:	a0 04 00 02 	add	r2, r0, 1184
  5c:	a0 04 00 01 	add	r1, r0, 1184
  60:	05 00 10 a2 	beq	r2, r1, 20
  64:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
  68:	00 00 00 02 
  6c:	00 00 00 03 	add	r3, r0, 0
  70:	01 00 30 a2 	beq	r2, r3, 4
  74:	00 00 26 ad 	jl	lr, r2

00000078 <.L1>:
  78:	00 00 f0 9d 	ldr	lr, sp, 0
  7c:	04 00 f0 0f 	add	sp, sp, 4
  80:	00 00 d6 a0 	jl	r0, lr
	...

00000090 <_register_tm_clones>:
  90:	fc ff f0 0f 	add	sp, sp, -4
  94:	00 00 f1 9d 	str	lr, sp, 0
  98:	a0 04 00 01 	add	r1, r0, 1184
  9c:	a0 04 10 01 	add	r1, r1, 1184
  a0:	25 00 10 71 	asr	r1, r1, 2
  a4:	f3 01 10 72 	lsr	r2, r1, 31
  a8:	00 00 21 02 	add	r2, r2, r1
  ac:	15 00 20 72 	asr	r2, r2, 1
  b0:	00 00 00 01 	add	r1, r0, 0
  b4:	05 00 10 a2 	beq	r2, r1, 20
  b8:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  bc:	00 00 00 03 
  c0:	02 00 10 a3 	beq	r3, r1, 8
  c4:	a0 04 00 01 	add	r1, r0, 1184
  c8:	00 00 36 ad 	jl	lr, r3

000000cc <.L5>:
  cc:	00 00 f0 9d 	ldr	lr, sp, 0
  d0:	04 00 f0 0f 	add	sp, sp, 4
  d4:	00 00 d6 a0 	jl	r0, lr
  d8:	00 00 00 00 	add	r0, r0, 0

000000dc <___do_global_dtors_aux>:
  dc:	f8 ff f0 0f 	add	sp, sp, -8
  e0:	00 00 f1 97 	str	r7, sp, 0
  e4:	04 00 f1 9d 	str	lr, sp, 4
  e8:	a0 04 00 07 	add	r7, r0, 1184
  ec:	00 00 74 92 	ldub	r2, r7, 0
  f0:	00 00 00 01 	add	r1, r0, 0
  f4:	03 00 11 a2 	bne	r2, r1, 12
  f8:	d5 ff ff bd 	bl	lr, -172
  fc:	01 00 00 01 	add	r1, r0, 1
 100:	00 00 77 91 	stb	r1, r7, 0

00000104 <.L9>:
 104:	00 00 f0 97 	ldr	r7, sp, 0
 108:	04 00 f0 9d 	ldr	lr, sp, 4
 10c:	08 00 f0 0f 	add	sp, sp, 8
 110:	00 00 d6 a0 	jl	r0, lr
	...

00000120 <_call___do_global_dtors_aux>:
 120:	fc ff f0 0f 	add	sp, sp, -4
 124:	00 00 f1 9d 	str	lr, sp, 0
 128:	00 00 f0 9d 	ldr	lr, sp, 0
 12c:	04 00 f0 0f 	add	sp, sp, 4
 130:	00 00 d6 a0 	jl	r0, lr
	...

00000140 <_frame_dummy>:
 140:	fc ff f0 0f 	add	sp, sp, -4
 144:	00 00 f1 9d 	str	lr, sp, 0
 148:	d1 ff ff bd 	bl	lr, -188
 14c:	00 00 f0 9d 	ldr	lr, sp, 0
 150:	04 00 f0 0f 	add	sp, sp, 4
 154:	00 00 d6 a0 	jl	r0, lr
 158:	00 00 00 00 	add	r0, r0, 0

0000015c <_call_frame_dummy>:
 15c:	fc ff f0 0f 	add	sp, sp, -4
 160:	00 00 f1 9d 	str	lr, sp, 0
 164:	00 00 f0 9d 	ldr	lr, sp, 0
 168:	04 00 f0 0f 	add	sp, sp, 4
 16c:	00 00 d6 a0 	jl	r0, lr

00000170 <_memcpy>:
 170:	13 00 00 a3 	beq	r3, r0, 76
 174:	00 00 10 04 	add	r4, r1, 0
 178:	00 00 20 05 	add	r5, r2, 0
 17c:	fc ff 30 66 	and	r6, r3, -4
 180:	00 00 16 06 	add	r6, r1, r6
 184:	00 00 13 03 	add	r3, r1, r3
 188:	00 00 12 52 	or	r2, r1, r2
 18c:	03 00 20 62 	and	r2, r2, 3
 190:	06 00 01 a2 	bne	r2, r0, 24

00000194 <.Lmemcpy_words_loop>:
 194:	00 00 50 92 	ldr	r2, r5, 0
 198:	00 00 41 92 	str	r2, r4, 0
 19c:	04 00 40 04 	add	r4, r4, 4
 1a0:	04 00 50 05 	add	r5, r5, 4
 1a4:	fb ff 61 a4 	bne	r4, r6, -20
 1a8:	05 00 30 a6 	beq	r6, r3, 20

000001ac <.Lmemcpy_remaining_bytes_loop>:
 1ac:	00 00 54 92 	ldub	r2, r5, 0
 1b0:	00 00 47 92 	stb	r2, r4, 0
 1b4:	01 00 40 04 	add	r4, r4, 1
 1b8:	01 00 50 05 	add	r5, r5, 1
 1bc:	fb ff 31 a4 	bne	r4, r3, -20

000001c0 <.Lmemcpy_end>:
 1c0:	00 00 d6 a0 	jl	r0, lr

000001c4 <_memset>:
 1c4:	12 00 00 a3 	beq	r3, r0, 72
 1c8:	00 00 10 04 	add	r4, r1, 0
 1cc:	ff 00 20 62 	and	r2, r2, 255
 1d0:	01 01 00 c0 	add	r5, r0, 16843009 // pre #0x101
 1d4:	01 01 00 05 
 1d8:	00 00 52 85 	mul	r5, r5, r2
 1dc:	fc ff 30 66 	and	r6, r3, -4
 1e0:	00 00 16 06 	add	r6, r1, r6
 1e4:	00 00 13 03 	add	r3, r1, r3
 1e8:	03 00 10 62 	and	r2, r1, 3
 1ec:	05 00 01 a2 	bne	r2, r0, 20

000001f0 <.Lmemset_words_loop>:
 1f0:	00 00 41 95 	str	r5, r4, 0
 1f4:	04 00 40 04 	add	r4, r4, 4
 1f8:	04 00 50 05 	add	r5, r5, 4
 1fc:	fc ff 61 a4 	bne	r4, r6, -16
 200:	03 00 30 a6 	beq	r6, r3, 12

00000204 <.Lmemset_remaining_bytes_loop>:
 204:	00 00 47 95 	stb	r5, r4, 0
 208:	01 00 40 04 	add	r4, r4, 1
 20c:	fd ff 31 a4 	bne	r4, r3, -12

00000210 <.Lmemset_end>:
 210:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .init:

00000220 <_start>:
	...
 228:	00 10 00 0f 	add	sp, r0, 4096
 22c:	02 00 00 a0 	beq	r0, r0, 8
	...

00000238 <__cstart>:
 238:	fc ff f0 0f 	add	sp, sp, -4
 23c:	00 00 f1 9d 	str	lr, sp, 0
 240:	94 04 00 03 	add	r3, r0, 1172
 244:	70 04 00 02 	add	r2, r0, 1136
 248:	70 04 00 01 	add	r1, r0, 1136
 24c:	c8 ff ff bd 	bl	lr, -224
 250:	a1 04 00 03 	add	r3, r0, 1185
 254:	00 00 00 02 	add	r2, r0, 0
 258:	94 04 00 01 	add	r1, r0, 1172
 25c:	d9 ff ff bd 	bl	lr, -156
 260:	23 00 00 bd 	bl	lr, 140
 264:	00 00 00 02 	add	r2, r0, 0
 268:	00 00 20 01 	add	r1, r2, 0
 26c:	08 00 00 bd 	bl	lr, 32

00000270 <.L2>:
 270:	ff ff ff b0 	bl	r0, -4

00000274 <_init>:
 274:	b2 ff ff bd 	bl	lr, -312
 278:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

0000027c <_fini>:
 27c:	97 ff ff bd 	bl	lr, -420
 280:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

00000290 <_main>:
 290:	f8 ff f0 0f 	add	sp, sp, -8
 294:	00 00 f1 97 	str	r7, sp, 0
 298:	04 00 f1 9d 	str	lr, sp, 4
 29c:	90 04 00 01 	add	r1, r0, 1168
 2a0:	00 00 10 9d 	ldr	lr, r1, 0
 2a4:	00 00 00 06 	add	r6, r0, 0
 2a8:	40 01 00 05 	add	r5, r0, 320
 2ac:	f0 00 00 07 	add	r7, r0, 240

000002b0 <.L2>:
 2b0:	33 00 60 74 	lsr	r4, r6, 3
 2b4:	51 00 40 74 	lsl	r4, r4, 5
 2b8:	00 00 d0 03 	add	r3, lr, 0
 2bc:	00 00 00 02 	add	r2, r0, 0

000002c0 <.L3>:
 2c0:	33 00 20 71 	lsr	r1, r2, 3
 2c4:	1f 00 10 61 	and	r1, r1, 31
 2c8:	00 00 14 51 	or	r1, r1, r4
 2cc:	00 00 36 91 	sth	r1, r3, 0
 2d0:	01 00 20 02 	add	r2, r2, 1
 2d4:	02 00 30 03 	add	r3, r3, 2
 2d8:	f9 ff 51 a2 	bne	r2, r5, -28
 2dc:	01 00 60 06 	add	r6, r6, 1
 2e0:	80 02 d0 0d 	add	lr, lr, 640
 2e4:	f2 ff 71 a6 	bne	r6, r7, -56

000002e8 <.L4>:
 2e8:	ff ff ff b0 	bl	r0, -4

Disassembly of section .text.__libc_init_array:

000002f0 <___libc_init_array>:
 2f0:	f0 ff f0 0f 	add	sp, sp, -16
 2f4:	00 00 f1 97 	str	r7, sp, 0
 2f8:	04 00 f1 98 	str	r8, sp, 4
 2fc:	08 00 f1 99 	str	r9, sp, 8
 300:	0c 00 f1 9d 	str	lr, sp, 12
 304:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 308:	00 00 00 09 
 30c:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 310:	00 00 00 07 
 314:	08 00 70 a9 	beq	r9, r7, 32
 318:	00 00 97 19 	sub	r9, r9, r7
 31c:	25 00 90 79 	asr	r9, r9, 2
 320:	00 00 00 08 	add	r8, r0, 0

00000324 <.L3>:
 324:	00 00 70 91 	ldr	r1, r7, 0
 328:	00 00 16 ad 	jl	lr, r1
 32c:	01 00 80 08 	add	r8, r8, 1
 330:	04 00 70 07 	add	r7, r7, 4
 334:	fb ff 92 a8 	bltu	r8, r9, -20

00000338 <.L2>:
 338:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 33c:	00 00 00 02 
 340:	00 00 00 01 	add	r1, r0, 0
 344:	02 00 10 a2 	beq	r2, r1, 8
 348:	ff ff 00 c0 	bl	lr, -848 // pre #0xffff
 34c:	2c ff ff bd 

00000350 <.L4>:
 350:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 354:	00 00 00 09 
 358:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 35c:	00 00 00 07 
 360:	08 00 70 a9 	beq	r9, r7, 32
 364:	00 00 97 19 	sub	r9, r9, r7
 368:	25 00 90 79 	asr	r9, r9, 2
 36c:	00 00 00 08 	add	r8, r0, 0

00000370 <.L6>:
 370:	00 00 70 91 	ldr	r1, r7, 0
 374:	00 00 16 ad 	jl	lr, r1
 378:	01 00 80 08 	add	r8, r8, 1
 37c:	04 00 70 07 	add	r7, r7, 4
 380:	fb ff 92 a8 	bltu	r8, r9, -20

00000384 <.L1>:
 384:	00 00 f0 97 	ldr	r7, sp, 0
 388:	04 00 f0 98 	ldr	r8, sp, 4
 38c:	08 00 f0 99 	ldr	r9, sp, 8
 390:	0c 00 f0 9d 	ldr	lr, sp, 12
 394:	10 00 f0 0f 	add	sp, sp, 16
 398:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .rodata:

000003a0 <_snowhousecpu_regno_to_class>:
 3a0:	01 00 00 00 	add	r0, r0, 1
 3a4:	01 00 00 00 	add	r0, r0, 1
 3a8:	01 00 00 00 	add	r0, r0, 1
 3ac:	01 00 00 00 	add	r0, r0, 1
 3b0:	01 00 00 00 	add	r0, r0, 1
 3b4:	01 00 00 00 	add	r0, r0, 1
 3b8:	01 00 00 00 	add	r0, r0, 1
 3bc:	01 00 00 00 	add	r0, r0, 1
 3c0:	01 00 00 00 	add	r0, r0, 1
 3c4:	01 00 00 00 	add	r0, r0, 1
 3c8:	01 00 00 00 	add	r0, r0, 1
 3cc:	01 00 00 00 	add	r0, r0, 1
 3d0:	01 00 00 00 	add	r0, r0, 1
 3d4:	02 00 00 00 	add	r0, r0, 2
 3d8:	03 00 00 00 	add	r0, r0, 3
 3dc:	01 00 00 00 	add	r0, r0, 1
 3e0:	01 00 00 00 	add	r0, r0, 1
 3e4:	04 00 00 00 	add	r0, r0, 4
	...

000003f0 <_FB_SIZE>:
 3f0:	00 2c 01 00 	add	r0, r0, r1
	...

00000400 <_FB_HEIGHT>:
 400:	f0 00 00 00 	add	r0, r0, 240
	...

00000410 <_FB_WIDTH>:
 410:	40 01 00 00 	add	r0, r0, 320
	...

00000420 <_snowhousecpu_regno_to_class>:
 420:	01 00 00 00 	add	r0, r0, 1
 424:	01 00 00 00 	add	r0, r0, 1
 428:	01 00 00 00 	add	r0, r0, 1
 42c:	01 00 00 00 	add	r0, r0, 1
 430:	01 00 00 00 	add	r0, r0, 1
 434:	01 00 00 00 	add	r0, r0, 1
 438:	01 00 00 00 	add	r0, r0, 1
 43c:	01 00 00 00 	add	r0, r0, 1
 440:	01 00 00 00 	add	r0, r0, 1
 444:	01 00 00 00 	add	r0, r0, 1
 448:	01 00 00 00 	add	r0, r0, 1
 44c:	01 00 00 00 	add	r0, r0, 1
 450:	01 00 00 00 	add	r0, r0, 1
 454:	02 00 00 00 	add	r0, r0, 2
 458:	03 00 00 00 	add	r0, r0, 3
 45c:	01 00 00 00 	add	r0, r0, 1
 460:	01 00 00 00 	add	r0, r0, 1
 464:	04 00 00 00 	add	r0, r0, 4

Disassembly of section .data:

00000470 <___dso_handle>:
	...

00000480 <_to_keep_loop_going>:
 480:	04 00 00 00 	add	r0, r0, 4
	...

00000490 <_fb>:
 490:	00 00 80 00 	add	r0, r8, 0

Disassembly of section .comment:

00000000 <_stack-0x1000>:
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

