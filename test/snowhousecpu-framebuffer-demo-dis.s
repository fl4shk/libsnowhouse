
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
  84:	aa 00 00 b0 	bl	r0, 680
	...

00000090 <_deregister_tm_clones>:
  90:	fc ff f0 0f 	add	sp, sp, -4
  94:	00 00 f1 9d 	str	lr, sp, 0
  98:	b0 05 00 02 	add	r2, r0, 1456
  9c:	b0 05 00 01 	add	r1, r0, 1456
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
  d8:	b0 05 00 01 	add	r1, r0, 1456
  dc:	b0 05 10 01 	add	r1, r1, 1456
  e0:	25 00 10 71 	asr	r1, r1, 2
  e4:	f3 01 10 72 	lsr	r2, r1, 31
  e8:	00 00 21 02 	add	r2, r2, r1
  ec:	15 00 20 72 	asr	r2, r2, 1
  f0:	00 00 00 01 	add	r1, r0, 0
  f4:	05 00 10 a2 	beq	r2, r1, 20
  f8:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  fc:	00 00 00 03 
 100:	02 00 10 a3 	beq	r3, r1, 8
 104:	b0 05 00 01 	add	r1, r0, 1456
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
 128:	b0 05 00 07 	add	r7, r0, 1456
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

000001b0 <_mul_test>:
 1b0:	00 00 12 81 	umulw	r1, r1, r2
 1b4:	00 00 d6 a0 	jl	r0, lr
	...

000001c0 <_udivw_test>:
 1c0:	fc ff f0 0f 	add	sp, sp, -4
 1c4:	00 00 f1 97 	str	r7, sp, 0
 1c8:	0c 00 10 70 	cpy	hi, r1
 1cc:	00 00 20 07 	add	r7, r2, 0
 1d0:	00 00 30 01 	add	r1, r3, 0
 1d4:	00 00 40 02 	add	r2, r4, 0
 1d8:	06 00 12 87 	udivw	r7, r1, r2
 1dc:	0b 00 00 71 	cpy	r1, hi
 1e0:	00 00 70 02 	add	r2, r7, 0
 1e4:	00 00 f0 97 	ldr	r7, sp, 0
 1e8:	04 00 f0 0f 	add	sp, sp, 4
 1ec:	00 00 d6 a0 	jl	r0, lr

000001f0 <_sdivw_test>:
 1f0:	fc ff f0 0f 	add	sp, sp, -4
 1f4:	00 00 f1 97 	str	r7, sp, 0
 1f8:	0c 00 10 70 	cpy	hi, r1
 1fc:	00 00 20 07 	add	r7, r2, 0
 200:	00 00 30 01 	add	r1, r3, 0
 204:	00 00 40 02 	add	r2, r4, 0
 208:	07 00 12 87 	sdivw	r7, r1, r2
 20c:	0b 00 00 71 	cpy	r1, hi
 210:	00 00 70 02 	add	r2, r7, 0
 214:	00 00 f0 97 	ldr	r7, sp, 0
 218:	04 00 f0 0f 	add	sp, sp, 4
 21c:	00 00 d6 a0 	jl	r0, lr

00000220 <_cmp_ltu>:
 220:	00 00 12 21 	sltu	r1, r1, r2
 224:	00 00 d6 a0 	jl	r0, lr
	...

00000230 <_cmp_lts>:
 230:	01 00 12 21 	slts	r1, r1, r2
 234:	00 00 d6 a0 	jl	r0, lr
	...

00000240 <_add64_test>:
 240:	00 00 10 05 	add	r5, r1, 0
 244:	00 00 13 01 	add	r1, r1, r3
 248:	00 00 15 25 	sltu	r5, r1, r5
 24c:	00 00 24 02 	add	r2, r2, r4
 250:	00 00 52 02 	add	r2, r5, r2
 254:	00 00 d6 a0 	jl	r0, lr
	...

00000260 <_sub64_test>:
 260:	00 00 13 13 	sub	r3, r1, r3
 264:	01 00 00 05 	add	r5, r0, 1
 268:	01 00 32 a1 	bltu	r1, r3, 4
 26c:	00 00 00 05 	add	r5, r0, 0

00000270 <.L17>:
 270:	00 00 24 12 	sub	r2, r2, r4
 274:	00 00 30 01 	add	r1, r3, 0
 278:	00 00 25 12 	sub	r2, r2, r5
 27c:	00 00 d6 a0 	jl	r0, lr

00000280 <_memcpy>:
 280:	13 00 00 a3 	beq	r3, r0, 76
 284:	00 00 10 04 	add	r4, r1, 0
 288:	00 00 20 05 	add	r5, r2, 0
 28c:	fc ff 30 66 	and	r6, r3, -4
 290:	00 00 16 06 	add	r6, r1, r6
 294:	00 00 13 03 	add	r3, r1, r3
 298:	00 00 12 52 	or	r2, r1, r2
 29c:	03 00 20 62 	and	r2, r2, 3
 2a0:	06 00 01 a2 	bne	r2, r0, 24

000002a4 <.Lmemcpy_words_loop>:
 2a4:	00 00 50 92 	ldr	r2, r5, 0
 2a8:	00 00 41 92 	str	r2, r4, 0
 2ac:	04 00 40 04 	add	r4, r4, 4
 2b0:	04 00 50 05 	add	r5, r5, 4
 2b4:	fb ff 61 a4 	bne	r4, r6, -20
 2b8:	05 00 30 a6 	beq	r6, r3, 20

000002bc <.Lmemcpy_remaining_bytes_loop>:
 2bc:	00 00 54 92 	ldub	r2, r5, 0
 2c0:	00 00 47 92 	stb	r2, r4, 0
 2c4:	01 00 40 04 	add	r4, r4, 1
 2c8:	01 00 50 05 	add	r5, r5, 1
 2cc:	fb ff 31 a4 	bne	r4, r3, -20

000002d0 <.Lmemcpy_end>:
 2d0:	00 00 d6 a0 	jl	r0, lr

000002d4 <_memset>:
 2d4:	12 00 00 a3 	beq	r3, r0, 72
 2d8:	00 00 10 04 	add	r4, r1, 0
 2dc:	ff 00 20 62 	and	r2, r2, 255
 2e0:	01 01 00 c0 	add	r5, r0, 16843009 // pre #0x101
 2e4:	01 01 00 05 
 2e8:	00 00 52 85 	umulw	r5, r5, r2
 2ec:	fc ff 30 66 	and	r6, r3, -4
 2f0:	00 00 16 06 	add	r6, r1, r6
 2f4:	00 00 13 03 	add	r3, r1, r3
 2f8:	03 00 10 62 	and	r2, r1, 3
 2fc:	05 00 01 a2 	bne	r2, r0, 20

00000300 <.Lmemset_words_loop>:
 300:	00 00 41 95 	str	r5, r4, 0
 304:	04 00 40 04 	add	r4, r4, 4
 308:	04 00 50 05 	add	r5, r5, 4
 30c:	fc ff 61 a4 	bne	r4, r6, -16
 310:	03 00 30 a6 	beq	r6, r3, 12

00000314 <.Lmemset_remaining_bytes_loop>:
 314:	00 00 47 95 	stb	r5, r4, 0
 318:	01 00 40 04 	add	r4, r4, 1
 31c:	fd ff 31 a4 	bne	r4, r3, -12

00000320 <.Lmemset_end>:
 320:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .init:

00000330 <_start>:
	...
 338:	01 00 00 c0 	add	sp, r0, 98304 // pre #0x1
 33c:	00 80 00 0f 
 340:	02 00 00 a0 	beq	r0, r0, 8
	...

0000034c <__cstart>:
 34c:	fc ff f0 0f 	add	sp, sp, -4
 350:	00 00 f1 9d 	str	lr, sp, 0
 354:	a4 05 00 03 	add	r3, r0, 1444
 358:	80 05 00 02 	add	r2, r0, 1408
 35c:	80 05 00 01 	add	r1, r0, 1408
 360:	c7 ff ff bd 	bl	lr, -228
 364:	b1 05 00 03 	add	r3, r0, 1457
 368:	00 00 00 02 	add	r2, r0, 0
 36c:	a4 05 00 01 	add	r1, r0, 1444
 370:	d8 ff ff bd 	bl	lr, -160
 374:	22 00 00 bd 	bl	lr, 136
 378:	00 00 00 02 	add	r2, r0, 0
 37c:	00 00 20 01 	add	r1, r2, 0
 380:	07 00 00 bd 	bl	lr, 28

00000384 <.L2>:
 384:	ff ff ff b0 	bl	r0, -4

00000388 <_init>:
 388:	7d ff ff bd 	bl	lr, -524
 38c:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

00000390 <_fini>:
 390:	62 ff ff bd 	bl	lr, -632
 394:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

000003a0 <_main>:
 3a0:	f8 ff f0 0f 	add	sp, sp, -8
 3a4:	00 00 f1 97 	str	r7, sp, 0
 3a8:	04 00 f1 9d 	str	lr, sp, 4
 3ac:	a0 05 00 01 	add	r1, r0, 1440
 3b0:	00 00 10 9d 	ldr	lr, r1, 0
 3b4:	00 00 00 06 	add	r6, r0, 0
 3b8:	40 01 00 05 	add	r5, r0, 320
 3bc:	f0 00 00 07 	add	r7, r0, 240

000003c0 <.L21>:
 3c0:	33 00 60 74 	lsr	r4, r6, 3
 3c4:	51 00 40 74 	lsl	r4, r4, 5
 3c8:	00 00 d0 03 	add	r3, lr, 0
 3cc:	00 00 00 02 	add	r2, r0, 0

000003d0 <.L22>:
 3d0:	33 00 20 71 	lsr	r1, r2, 3
 3d4:	1f 00 10 61 	and	r1, r1, 31
 3d8:	00 00 14 51 	or	r1, r1, r4
 3dc:	00 00 36 91 	sth	r1, r3, 0
 3e0:	01 00 20 02 	add	r2, r2, 1
 3e4:	02 00 30 03 	add	r3, r3, 2
 3e8:	f9 ff 51 a2 	bne	r2, r5, -28
 3ec:	01 00 60 06 	add	r6, r6, 1
 3f0:	80 02 d0 0d 	add	lr, lr, 640
 3f4:	f2 ff 71 a6 	bne	r6, r7, -56

000003f8 <.L23>:
 3f8:	ff ff ff b0 	bl	r0, -4

Disassembly of section .text.__libc_init_array:

00000400 <___libc_init_array>:
 400:	f0 ff f0 0f 	add	sp, sp, -16
 404:	00 00 f1 97 	str	r7, sp, 0
 408:	04 00 f1 98 	str	r8, sp, 4
 40c:	08 00 f1 99 	str	r9, sp, 8
 410:	0c 00 f1 9d 	str	lr, sp, 12
 414:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 418:	00 00 00 09 
 41c:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 420:	00 00 00 07 
 424:	08 00 70 a9 	beq	r9, r7, 32
 428:	00 00 97 19 	sub	r9, r9, r7
 42c:	25 00 90 79 	asr	r9, r9, 2
 430:	00 00 00 08 	add	r8, r0, 0

00000434 <.L3>:
 434:	00 00 70 91 	ldr	r1, r7, 0
 438:	00 00 16 ad 	jl	lr, r1
 43c:	01 00 80 08 	add	r8, r8, 1
 440:	04 00 70 07 	add	r7, r7, 4
 444:	fb ff 92 a8 	bltu	r8, r9, -20

00000448 <.L2>:
 448:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 44c:	00 00 00 02 
 450:	00 00 00 01 	add	r1, r0, 0
 454:	02 00 10 a2 	beq	r2, r1, 8
 458:	ff ff 00 c0 	bl	lr, -1120 // pre #0xffff
 45c:	e8 fe ff bd 

00000460 <.L4>:
 460:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 464:	00 00 00 09 
 468:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 46c:	00 00 00 07 
 470:	08 00 70 a9 	beq	r9, r7, 32
 474:	00 00 97 19 	sub	r9, r9, r7
 478:	25 00 90 79 	asr	r9, r9, 2
 47c:	00 00 00 08 	add	r8, r0, 0

00000480 <.L6>:
 480:	00 00 70 91 	ldr	r1, r7, 0
 484:	00 00 16 ad 	jl	lr, r1
 488:	01 00 80 08 	add	r8, r8, 1
 48c:	04 00 70 07 	add	r7, r7, 4
 490:	fb ff 92 a8 	bltu	r8, r9, -20

00000494 <.L1>:
 494:	00 00 f0 97 	ldr	r7, sp, 0
 498:	04 00 f0 98 	ldr	r8, sp, 4
 49c:	08 00 f0 99 	ldr	r9, sp, 8
 4a0:	0c 00 f0 9d 	ldr	lr, sp, 12
 4a4:	10 00 f0 0f 	add	sp, sp, 16
 4a8:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .rodata:

000004b0 <_snowhousecpu_regno_to_class>:
 4b0:	01 00 00 00 	add	r0, r0, 1
 4b4:	01 00 00 00 	add	r0, r0, 1
 4b8:	01 00 00 00 	add	r0, r0, 1
 4bc:	01 00 00 00 	add	r0, r0, 1
 4c0:	01 00 00 00 	add	r0, r0, 1
 4c4:	01 00 00 00 	add	r0, r0, 1
 4c8:	02 00 00 00 	add	r0, r0, 2
 4cc:	01 00 00 00 	add	r0, r0, 1
 4d0:	01 00 00 00 	add	r0, r0, 1
 4d4:	01 00 00 00 	add	r0, r0, 1
 4d8:	01 00 00 00 	add	r0, r0, 1
 4dc:	01 00 00 00 	add	r0, r0, 1
 4e0:	01 00 00 00 	add	r0, r0, 1
 4e4:	01 00 00 00 	add	r0, r0, 1
 4e8:	04 00 00 00 	add	r0, r0, 4
 4ec:	05 00 00 00 	add	r0, r0, 5
 4f0:	01 00 00 00 	add	r0, r0, 1
 4f4:	01 00 00 00 	add	r0, r0, 1
 4f8:	06 00 00 00 	add	r0, r0, 6
 4fc:	00 00 00 00 	add	r0, r0, 0

00000500 <_FB_SIZE>:
 500:	00 2c 01 00 	add	r0, r0, r1
	...

00000510 <_FB_HEIGHT>:
 510:	f0 00 00 00 	add	r0, r0, 240
	...

00000520 <_FB_WIDTH>:
 520:	40 01 00 00 	add	r0, r0, 320
	...

00000530 <_snowhousecpu_regno_to_class>:
 530:	01 00 00 00 	add	r0, r0, 1
 534:	01 00 00 00 	add	r0, r0, 1
 538:	01 00 00 00 	add	r0, r0, 1
 53c:	01 00 00 00 	add	r0, r0, 1
 540:	01 00 00 00 	add	r0, r0, 1
 544:	01 00 00 00 	add	r0, r0, 1
 548:	02 00 00 00 	add	r0, r0, 2
 54c:	01 00 00 00 	add	r0, r0, 1
 550:	01 00 00 00 	add	r0, r0, 1
 554:	01 00 00 00 	add	r0, r0, 1
 558:	01 00 00 00 	add	r0, r0, 1
 55c:	01 00 00 00 	add	r0, r0, 1
 560:	01 00 00 00 	add	r0, r0, 1
 564:	01 00 00 00 	add	r0, r0, 1
 568:	04 00 00 00 	add	r0, r0, 4
 56c:	05 00 00 00 	add	r0, r0, 5
 570:	01 00 00 00 	add	r0, r0, 1
 574:	01 00 00 00 	add	r0, r0, 1
 578:	06 00 00 00 	add	r0, r0, 6

Disassembly of section .data:

00000580 <___dso_handle>:
	...

00000590 <_to_keep_loop_going>:
 590:	04 00 00 00 	add	r0, r0, 4
	...

000005a0 <_fb>:
 5a0:	00 00 80 00 	add	r0, r8, 0

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

