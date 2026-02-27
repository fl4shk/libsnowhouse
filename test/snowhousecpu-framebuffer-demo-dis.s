
snowhousecpu-framebuffer-demo.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_my_text_start>:
	...
  10:	01 00 00 07 	add	r7, r0, 1
  14:	0c 00 70 70 	cpy	hi, r7
  18:	04 00 00 01 	add	r1, r0, 4
  1c:	00 00 00 02 	add	r2, r0, 0
  20:	02 00 00 03 	add	r3, r0, 2
  24:	06 00 23 81 	udivw	r1, r2, r3
  28:	0b 00 00 78 	cpy	r8, hi
  2c:	ff ff 00 07 	add	r7, r0, -1
  30:	0c 00 70 70 	cpy	hi, r7
  34:	04 00 00 01 	add	r1, r0, 4
  38:	00 00 00 02 	add	r2, r0, 0
  3c:	fe ff 00 03 	add	r3, r0, -2
  40:	07 00 23 81 	sdivw	r1, r2, r3
  44:	0b 00 00 79 	cpy	r9, hi
  48:	00 00 00 01 	add	r1, r0, 0
  4c:	00 00 00 02 	add	r2, r0, 0
  50:	00 00 00 03 	add	r3, r0, 0
  54:	00 00 00 04 	add	r4, r0, 0
  58:	00 00 00 05 	add	r5, r0, 0
  5c:	00 00 00 06 	add	r6, r0, 0
  60:	00 00 00 07 	add	r7, r0, 0
  64:	00 00 00 08 	add	r8, r0, 0
  68:	00 00 00 09 	add	r9, r0, 0
  6c:	00 00 00 0a 	add	r10, r0, 0
  70:	00 00 00 0b 	add	r11, r0, 0
  74:	00 00 00 0c 	add	r12, r0, 0
  78:	00 00 00 0d 	add	lr, r0, 0
  7c:	00 00 00 0e 	add	fp, r0, 0
  80:	00 00 00 0f 	add	sp, r0, 0
  84:	76 00 00 b0 	bl	r0, 472
	...

00000090 <_deregister_tm_clones>:
  90:	fc ff f0 0f 	add	sp, sp, -4
  94:	00 00 f1 9d 	str	lr, sp, 0
  98:	e0 04 00 02 	add	r2, r0, 1248
  9c:	e0 04 00 01 	add	r1, r0, 1248
  a0:	05 00 10 a2 	beq	r2, r1, 20
  a4:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
  a8:	00 00 00 02 
  ac:	00 00 00 03 	add	r3, r0, 0
  b0:	01 00 30 a2 	beq	r2, r3, 4
  b4:	00 00 26 ad 	jl	lr, r2

000000b8 <.L1>:
  b8:	00 00 f0 9d 	ldr	lr, sp, 0
  bc:	04 00 f0 0f 	add	sp, sp, 4
  c0:	00 00 d6 a0 	jl	r0, lr
	...

000000d0 <_register_tm_clones>:
  d0:	fc ff f0 0f 	add	sp, sp, -4
  d4:	00 00 f1 9d 	str	lr, sp, 0
  d8:	e0 04 00 01 	add	r1, r0, 1248
  dc:	e0 04 10 01 	add	r1, r1, 1248
  e0:	25 00 10 71 	asr	r1, r1, 2
  e4:	f3 01 10 72 	lsr	r2, r1, 31
  e8:	00 00 21 02 	add	r2, r2, r1
  ec:	15 00 20 72 	asr	r2, r2, 1
  f0:	00 00 00 01 	add	r1, r0, 0
  f4:	05 00 10 a2 	beq	r2, r1, 20
  f8:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  fc:	00 00 00 03 
 100:	02 00 10 a3 	beq	r3, r1, 8
 104:	e0 04 00 01 	add	r1, r0, 1248
 108:	00 00 36 ad 	jl	lr, r3

0000010c <.L5>:
 10c:	00 00 f0 9d 	ldr	lr, sp, 0
 110:	04 00 f0 0f 	add	sp, sp, 4
 114:	00 00 d6 a0 	jl	r0, lr
 118:	00 00 00 00 	add	r0, r0, 0

0000011c <___do_global_dtors_aux>:
 11c:	f8 ff f0 0f 	add	sp, sp, -8
 120:	00 00 f1 97 	str	r7, sp, 0
 124:	04 00 f1 9d 	str	lr, sp, 4
 128:	e0 04 00 07 	add	r7, r0, 1248
 12c:	00 00 74 92 	ldub	r2, r7, 0
 130:	00 00 00 01 	add	r1, r0, 0
 134:	03 00 11 a2 	bne	r2, r1, 12
 138:	d5 ff ff bd 	bl	lr, -172
 13c:	01 00 00 01 	add	r1, r0, 1
 140:	00 00 77 91 	stb	r1, r7, 0

00000144 <.L9>:
 144:	00 00 f0 97 	ldr	r7, sp, 0
 148:	04 00 f0 9d 	ldr	lr, sp, 4
 14c:	08 00 f0 0f 	add	sp, sp, 8
 150:	00 00 d6 a0 	jl	r0, lr
	...

00000160 <_call___do_global_dtors_aux>:
 160:	fc ff f0 0f 	add	sp, sp, -4
 164:	00 00 f1 9d 	str	lr, sp, 0
 168:	00 00 f0 9d 	ldr	lr, sp, 0
 16c:	04 00 f0 0f 	add	sp, sp, 4
 170:	00 00 d6 a0 	jl	r0, lr
	...

00000180 <_frame_dummy>:
 180:	fc ff f0 0f 	add	sp, sp, -4
 184:	00 00 f1 9d 	str	lr, sp, 0
 188:	d1 ff ff bd 	bl	lr, -188
 18c:	00 00 f0 9d 	ldr	lr, sp, 0
 190:	04 00 f0 0f 	add	sp, sp, 4
 194:	00 00 d6 a0 	jl	r0, lr
 198:	00 00 00 00 	add	r0, r0, 0

0000019c <_call_frame_dummy>:
 19c:	fc ff f0 0f 	add	sp, sp, -4
 1a0:	00 00 f1 9d 	str	lr, sp, 0
 1a4:	00 00 f0 9d 	ldr	lr, sp, 0
 1a8:	04 00 f0 0f 	add	sp, sp, 4
 1ac:	00 00 d6 a0 	jl	r0, lr

000001b0 <_memcpy>:
 1b0:	13 00 00 a3 	beq	r3, r0, 76
 1b4:	00 00 10 04 	add	r4, r1, 0
 1b8:	00 00 20 05 	add	r5, r2, 0
 1bc:	fc ff 30 66 	and	r6, r3, -4
 1c0:	00 00 16 06 	add	r6, r1, r6
 1c4:	00 00 13 03 	add	r3, r1, r3
 1c8:	00 00 12 52 	or	r2, r1, r2
 1cc:	03 00 20 62 	and	r2, r2, 3
 1d0:	06 00 01 a2 	bne	r2, r0, 24

000001d4 <.Lmemcpy_words_loop>:
 1d4:	00 00 50 92 	ldr	r2, r5, 0
 1d8:	00 00 41 92 	str	r2, r4, 0
 1dc:	04 00 40 04 	add	r4, r4, 4
 1e0:	04 00 50 05 	add	r5, r5, 4
 1e4:	fb ff 61 a4 	bne	r4, r6, -20
 1e8:	05 00 30 a6 	beq	r6, r3, 20

000001ec <.Lmemcpy_remaining_bytes_loop>:
 1ec:	00 00 54 92 	ldub	r2, r5, 0
 1f0:	00 00 47 92 	stb	r2, r4, 0
 1f4:	01 00 40 04 	add	r4, r4, 1
 1f8:	01 00 50 05 	add	r5, r5, 1
 1fc:	fb ff 31 a4 	bne	r4, r3, -20

00000200 <.Lmemcpy_end>:
 200:	00 00 d6 a0 	jl	r0, lr

00000204 <_memset>:
 204:	12 00 00 a3 	beq	r3, r0, 72
 208:	00 00 10 04 	add	r4, r1, 0
 20c:	ff 00 20 62 	and	r2, r2, 255
 210:	01 01 00 c0 	add	r5, r0, 16843009 // pre #0x101
 214:	01 01 00 05 
 218:	00 00 52 85 	umulw	r5, r5, r2
 21c:	fc ff 30 66 	and	r6, r3, -4
 220:	00 00 16 06 	add	r6, r1, r6
 224:	00 00 13 03 	add	r3, r1, r3
 228:	03 00 10 62 	and	r2, r1, 3
 22c:	05 00 01 a2 	bne	r2, r0, 20

00000230 <.Lmemset_words_loop>:
 230:	00 00 41 95 	str	r5, r4, 0
 234:	04 00 40 04 	add	r4, r4, 4
 238:	04 00 50 05 	add	r5, r5, 4
 23c:	fc ff 61 a4 	bne	r4, r6, -16
 240:	03 00 30 a6 	beq	r6, r3, 12

00000244 <.Lmemset_remaining_bytes_loop>:
 244:	00 00 47 95 	stb	r5, r4, 0
 248:	01 00 40 04 	add	r4, r4, 1
 24c:	fd ff 31 a4 	bne	r4, r3, -12

00000250 <.Lmemset_end>:
 250:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .init:

00000260 <_start>:
	...
 268:	01 00 00 c0 	add	sp, r0, 98304 // pre #0x1
 26c:	00 80 00 0f 
 270:	02 00 00 a0 	beq	r0, r0, 8
	...

0000027c <__cstart>:
 27c:	fc ff f0 0f 	add	sp, sp, -4
 280:	00 00 f1 9d 	str	lr, sp, 0
 284:	d4 04 00 03 	add	r3, r0, 1236
 288:	b0 04 00 02 	add	r2, r0, 1200
 28c:	b0 04 00 01 	add	r1, r0, 1200
 290:	c7 ff ff bd 	bl	lr, -228
 294:	e1 04 00 03 	add	r3, r0, 1249
 298:	00 00 00 02 	add	r2, r0, 0
 29c:	d4 04 00 01 	add	r1, r0, 1236
 2a0:	d8 ff ff bd 	bl	lr, -160
 2a4:	22 00 00 bd 	bl	lr, 136
 2a8:	00 00 00 02 	add	r2, r0, 0
 2ac:	00 00 20 01 	add	r1, r2, 0
 2b0:	07 00 00 bd 	bl	lr, 28

000002b4 <.L2>:
 2b4:	ff ff ff b0 	bl	r0, -4

000002b8 <_init>:
 2b8:	b1 ff ff bd 	bl	lr, -316
 2bc:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

000002c0 <_fini>:
 2c0:	96 ff ff bd 	bl	lr, -424
 2c4:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

000002d0 <_main>:
 2d0:	f8 ff f0 0f 	add	sp, sp, -8
 2d4:	00 00 f1 97 	str	r7, sp, 0
 2d8:	04 00 f1 9d 	str	lr, sp, 4
 2dc:	d0 04 00 01 	add	r1, r0, 1232
 2e0:	00 00 10 9d 	ldr	lr, r1, 0
 2e4:	00 00 00 06 	add	r6, r0, 0
 2e8:	40 01 00 05 	add	r5, r0, 320
 2ec:	f0 00 00 07 	add	r7, r0, 240

000002f0 <.L2>:
 2f0:	33 00 60 74 	lsr	r4, r6, 3
 2f4:	51 00 40 74 	lsl	r4, r4, 5
 2f8:	00 00 d0 03 	add	r3, lr, 0
 2fc:	00 00 00 02 	add	r2, r0, 0

00000300 <.L3>:
 300:	33 00 20 71 	lsr	r1, r2, 3
 304:	1f 00 10 61 	and	r1, r1, 31
 308:	00 00 14 51 	or	r1, r1, r4
 30c:	00 00 36 91 	sth	r1, r3, 0
 310:	01 00 20 02 	add	r2, r2, 1
 314:	02 00 30 03 	add	r3, r3, 2
 318:	f9 ff 51 a2 	bne	r2, r5, -28
 31c:	01 00 60 06 	add	r6, r6, 1
 320:	80 02 d0 0d 	add	lr, lr, 640
 324:	f2 ff 71 a6 	bne	r6, r7, -56

00000328 <.L4>:
 328:	ff ff ff b0 	bl	r0, -4

Disassembly of section .text.__libc_init_array:

00000330 <___libc_init_array>:
 330:	f0 ff f0 0f 	add	sp, sp, -16
 334:	00 00 f1 97 	str	r7, sp, 0
 338:	04 00 f1 98 	str	r8, sp, 4
 33c:	08 00 f1 99 	str	r9, sp, 8
 340:	0c 00 f1 9d 	str	lr, sp, 12
 344:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 348:	00 00 00 09 
 34c:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 350:	00 00 00 07 
 354:	08 00 70 a9 	beq	r9, r7, 32
 358:	00 00 97 19 	sub	r9, r9, r7
 35c:	25 00 90 79 	asr	r9, r9, 2
 360:	00 00 00 08 	add	r8, r0, 0

00000364 <.L3>:
 364:	00 00 70 91 	ldr	r1, r7, 0
 368:	00 00 16 ad 	jl	lr, r1
 36c:	01 00 80 08 	add	r8, r8, 1
 370:	04 00 70 07 	add	r7, r7, 4
 374:	fb ff 92 a8 	bltu	r8, r9, -20

00000378 <.L2>:
 378:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 37c:	00 00 00 02 
 380:	00 00 00 01 	add	r1, r0, 0
 384:	02 00 10 a2 	beq	r2, r1, 8
 388:	ff ff 00 c0 	bl	lr, -912 // pre #0xffff
 38c:	1c ff ff bd 

00000390 <.L4>:
 390:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 394:	00 00 00 09 
 398:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 39c:	00 00 00 07 
 3a0:	08 00 70 a9 	beq	r9, r7, 32
 3a4:	00 00 97 19 	sub	r9, r9, r7
 3a8:	25 00 90 79 	asr	r9, r9, 2
 3ac:	00 00 00 08 	add	r8, r0, 0

000003b0 <.L6>:
 3b0:	00 00 70 91 	ldr	r1, r7, 0
 3b4:	00 00 16 ad 	jl	lr, r1
 3b8:	01 00 80 08 	add	r8, r8, 1
 3bc:	04 00 70 07 	add	r7, r7, 4
 3c0:	fb ff 92 a8 	bltu	r8, r9, -20

000003c4 <.L1>:
 3c4:	00 00 f0 97 	ldr	r7, sp, 0
 3c8:	04 00 f0 98 	ldr	r8, sp, 4
 3cc:	08 00 f0 99 	ldr	r9, sp, 8
 3d0:	0c 00 f0 9d 	ldr	lr, sp, 12
 3d4:	10 00 f0 0f 	add	sp, sp, 16
 3d8:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .rodata:

000003e0 <_snowhousecpu_regno_to_class>:
 3e0:	01 00 00 00 	add	r0, r0, 1
 3e4:	01 00 00 00 	add	r0, r0, 1
 3e8:	01 00 00 00 	add	r0, r0, 1
 3ec:	01 00 00 00 	add	r0, r0, 1
 3f0:	01 00 00 00 	add	r0, r0, 1
 3f4:	01 00 00 00 	add	r0, r0, 1
 3f8:	01 00 00 00 	add	r0, r0, 1
 3fc:	01 00 00 00 	add	r0, r0, 1
 400:	01 00 00 00 	add	r0, r0, 1
 404:	01 00 00 00 	add	r0, r0, 1
 408:	01 00 00 00 	add	r0, r0, 1
 40c:	01 00 00 00 	add	r0, r0, 1
 410:	01 00 00 00 	add	r0, r0, 1
 414:	04 00 00 00 	add	r0, r0, 4
 418:	05 00 00 00 	add	r0, r0, 5
 41c:	01 00 00 00 	add	r0, r0, 1
 420:	01 00 00 00 	add	r0, r0, 1
 424:	02 00 00 00 	add	r0, r0, 2
 428:	06 00 00 00 	add	r0, r0, 6
 42c:	00 00 00 00 	add	r0, r0, 0

00000430 <_FB_SIZE>:
 430:	00 2c 01 00 	add	r0, r0, r1
	...

00000440 <_FB_HEIGHT>:
 440:	f0 00 00 00 	add	r0, r0, 240
	...

00000450 <_FB_WIDTH>:
 450:	40 01 00 00 	add	r0, r0, 320
	...

00000460 <_snowhousecpu_regno_to_class>:
 460:	01 00 00 00 	add	r0, r0, 1
 464:	01 00 00 00 	add	r0, r0, 1
 468:	01 00 00 00 	add	r0, r0, 1
 46c:	01 00 00 00 	add	r0, r0, 1
 470:	01 00 00 00 	add	r0, r0, 1
 474:	01 00 00 00 	add	r0, r0, 1
 478:	01 00 00 00 	add	r0, r0, 1
 47c:	01 00 00 00 	add	r0, r0, 1
 480:	01 00 00 00 	add	r0, r0, 1
 484:	01 00 00 00 	add	r0, r0, 1
 488:	01 00 00 00 	add	r0, r0, 1
 48c:	01 00 00 00 	add	r0, r0, 1
 490:	01 00 00 00 	add	r0, r0, 1
 494:	04 00 00 00 	add	r0, r0, 4
 498:	05 00 00 00 	add	r0, r0, 5
 49c:	01 00 00 00 	add	r0, r0, 1
 4a0:	01 00 00 00 	add	r0, r0, 1
 4a4:	02 00 00 00 	add	r0, r0, 2
 4a8:	06 00 00 00 	add	r0, r0, 6

Disassembly of section .data:

000004b0 <___dso_handle>:
	...

000004c0 <_to_keep_loop_going>:
 4c0:	04 00 00 00 	add	r0, r0, 4
	...

000004d0 <_fb>:
 4d0:	00 00 80 00 	add	r0, r8, 0

Disassembly of section .comment:

00000000 <_stack-0x18000>:
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

