
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
  84:	9e 00 00 b0 	bl	r0, 632
  88:	00 00 00 00 	add	r0, r0, 0

0000008c <_do_enable_irqs>:
  8c:	00 01 00 c0 	str	r1, r0, 16777220 // pre #0x100
  90:	04 00 01 91 
  94:	a8 00 00 01 	add	r1, r0, 168
  98:	07 00 10 70 	cpy	ids, r1
  9c:	01 00 00 01 	add	r1, r0, 1
  a0:	09 00 10 70 	cpy	ie, r1
  a4:	00 00 d6 a0 	jl	r0, lr

000000a8 <_irq_handler>:
  a8:	e0 ff f0 0f 	add	sp, sp, -32
  ac:	00 00 f1 91 	str	r1, sp, 0
  b0:	04 00 f1 92 	str	r2, sp, 4
  b4:	08 00 f1 93 	str	r3, sp, 8
  b8:	0c 00 f1 94 	str	r4, sp, 12
  bc:	10 00 f1 95 	str	r5, sp, 16
  c0:	14 00 f1 96 	str	r6, sp, 20
  c4:	18 00 f1 9d 	str	lr, sp, 24
  c8:	1c 00 f1 9e 	str	fp, sp, 28
  cc:	00 01 00 c0 	ldr	r1, r0, 16777216 // pre #0x100
  d0:	00 00 00 91 
  d4:	89 00 00 bd 	bl	lr, 548
  d8:	00 00 f1 91 	str	r1, sp, 0
  dc:	04 00 f1 92 	str	r2, sp, 4
  e0:	08 00 f1 93 	str	r3, sp, 8
  e4:	0c 00 f1 94 	str	r4, sp, 12
  e8:	10 00 f1 95 	str	r5, sp, 16
  ec:	14 00 f1 96 	str	r6, sp, 20
  f0:	18 00 f1 9d 	str	lr, sp, 24
  f4:	1c 00 f1 9e 	str	fp, sp, 28
  f8:	20 00 f0 0f 	add	sp, sp, 32
  fc:	0a 00 00 70 	ret	ira

00000100 <_deregister_tm_clones>:
 100:	fc ff f0 0f 	add	sp, sp, -4
 104:	00 00 f1 9d 	str	lr, sp, 0
 108:	80 04 00 02 	add	r2, r0, 1152
 10c:	80 04 00 01 	add	r1, r0, 1152
 110:	05 00 10 a2 	beq	r2, r1, 20
 114:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 118:	00 00 00 02 
 11c:	00 00 00 03 	add	r3, r0, 0
 120:	01 00 30 a2 	beq	r2, r3, 4
 124:	00 00 26 ad 	jl	lr, r2

00000128 <.L1>:
 128:	00 00 f0 9d 	ldr	lr, sp, 0
 12c:	04 00 f0 0f 	add	sp, sp, 4
 130:	00 00 d6 a0 	jl	r0, lr
	...

00000140 <_register_tm_clones>:
 140:	fc ff f0 0f 	add	sp, sp, -4
 144:	00 00 f1 9d 	str	lr, sp, 0
 148:	80 04 00 01 	add	r1, r0, 1152
 14c:	80 04 10 01 	add	r1, r1, 1152
 150:	25 00 10 71 	asr	r1, r1, 2
 154:	f3 01 10 72 	lsr	r2, r1, 31
 158:	00 00 21 02 	add	r2, r2, r1
 15c:	15 00 20 72 	asr	r2, r2, 1
 160:	00 00 00 01 	add	r1, r0, 0
 164:	05 00 10 a2 	beq	r2, r1, 20
 168:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
 16c:	00 00 00 03 
 170:	02 00 10 a3 	beq	r3, r1, 8
 174:	80 04 00 01 	add	r1, r0, 1152
 178:	00 00 36 ad 	jl	lr, r3

0000017c <.L5>:
 17c:	00 00 f0 9d 	ldr	lr, sp, 0
 180:	04 00 f0 0f 	add	sp, sp, 4
 184:	00 00 d6 a0 	jl	r0, lr
 188:	00 00 00 00 	add	r0, r0, 0

0000018c <___do_global_dtors_aux>:
 18c:	f8 ff f0 0f 	add	sp, sp, -8
 190:	00 00 f1 97 	str	r7, sp, 0
 194:	04 00 f1 9d 	str	lr, sp, 4
 198:	58 05 00 07 	add	r7, r0, 1368
 19c:	00 00 74 92 	ldub	r2, r7, 0
 1a0:	00 00 00 01 	add	r1, r0, 0
 1a4:	03 00 11 a2 	bne	r2, r1, 12
 1a8:	d5 ff ff bd 	bl	lr, -172
 1ac:	01 00 00 01 	add	r1, r0, 1
 1b0:	00 00 77 91 	stb	r1, r7, 0

000001b4 <.L9>:
 1b4:	00 00 f0 97 	ldr	r7, sp, 0
 1b8:	04 00 f0 9d 	ldr	lr, sp, 4
 1bc:	08 00 f0 0f 	add	sp, sp, 8
 1c0:	00 00 d6 a0 	jl	r0, lr
	...

000001d0 <_call___do_global_dtors_aux>:
 1d0:	fc ff f0 0f 	add	sp, sp, -4
 1d4:	00 00 f1 9d 	str	lr, sp, 0
 1d8:	00 00 f0 9d 	ldr	lr, sp, 0
 1dc:	04 00 f0 0f 	add	sp, sp, 4
 1e0:	00 00 d6 a0 	jl	r0, lr
	...

000001f0 <_frame_dummy>:
 1f0:	fc ff f0 0f 	add	sp, sp, -4
 1f4:	00 00 f1 9d 	str	lr, sp, 0
 1f8:	d1 ff ff bd 	bl	lr, -188
 1fc:	00 00 f0 9d 	ldr	lr, sp, 0
 200:	04 00 f0 0f 	add	sp, sp, 4
 204:	00 00 d6 a0 	jl	r0, lr
 208:	00 00 00 00 	add	r0, r0, 0

0000020c <_call_frame_dummy>:
 20c:	fc ff f0 0f 	add	sp, sp, -4
 210:	00 00 f1 9d 	str	lr, sp, 0
 214:	00 00 f0 9d 	ldr	lr, sp, 0
 218:	04 00 f0 0f 	add	sp, sp, 4
 21c:	00 00 d6 a0 	jl	r0, lr

00000220 <_mul_test>:
 220:	00 00 12 81 	umulw	r1, r1, r2
 224:	00 00 d6 a0 	jl	r0, lr
	...

00000230 <_udivw_test>:
 230:	fc ff f0 0f 	add	sp, sp, -4
 234:	00 00 f1 97 	str	r7, sp, 0
 238:	0c 00 10 70 	cpy	hi, r1
 23c:	00 00 20 07 	add	r7, r2, 0
 240:	00 00 30 01 	add	r1, r3, 0
 244:	00 00 40 02 	add	r2, r4, 0
 248:	06 00 12 87 	udivw	r7, r1, r2
 24c:	0b 00 00 71 	cpy	r1, hi
 250:	00 00 70 02 	add	r2, r7, 0
 254:	00 00 f0 97 	ldr	r7, sp, 0
 258:	04 00 f0 0f 	add	sp, sp, 4
 25c:	00 00 d6 a0 	jl	r0, lr

00000260 <_sdivw_test>:
 260:	fc ff f0 0f 	add	sp, sp, -4
 264:	00 00 f1 97 	str	r7, sp, 0
 268:	0c 00 10 70 	cpy	hi, r1
 26c:	00 00 20 07 	add	r7, r2, 0
 270:	00 00 30 01 	add	r1, r3, 0
 274:	00 00 40 02 	add	r2, r4, 0
 278:	07 00 12 87 	sdivw	r7, r1, r2
 27c:	0b 00 00 71 	cpy	r1, hi
 280:	00 00 70 02 	add	r2, r7, 0
 284:	00 00 f0 97 	ldr	r7, sp, 0
 288:	04 00 f0 0f 	add	sp, sp, 4
 28c:	00 00 d6 a0 	jl	r0, lr

00000290 <_cmp_ltu>:
 290:	00 00 12 21 	sltu	r1, r1, r2
 294:	00 00 d6 a0 	jl	r0, lr
	...

000002a0 <_cmp_lts>:
 2a0:	01 00 12 21 	slts	r1, r1, r2
 2a4:	00 00 d6 a0 	jl	r0, lr
	...

000002b0 <_add64_test>:
 2b0:	00 00 10 05 	add	r5, r1, 0
 2b4:	00 00 13 01 	add	r1, r1, r3
 2b8:	00 00 15 25 	sltu	r5, r1, r5
 2bc:	00 00 24 02 	add	r2, r2, r4
 2c0:	00 00 52 02 	add	r2, r5, r2
 2c4:	00 00 d6 a0 	jl	r0, lr
	...

000002d0 <_sub64_test>:
 2d0:	00 00 13 13 	sub	r3, r1, r3
 2d4:	01 00 00 05 	add	r5, r0, 1
 2d8:	01 00 32 a1 	bltu	r1, r3, 4
 2dc:	00 00 00 05 	add	r5, r0, 0

000002e0 <.L17>:
 2e0:	00 00 24 12 	sub	r2, r2, r4
 2e4:	00 00 30 01 	add	r1, r3, 0
 2e8:	00 00 25 12 	sub	r2, r2, r5
 2ec:	00 00 d6 a0 	jl	r0, lr
	...

000002fc <_irq_handler_primary_logic>:
 2fc:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .init:

00000300 <_start>:
	...
 308:	ff 02 00 c0 	add	sp, r0, 50331644 // pre #0x2ff
 30c:	fc ff 00 0f 
 310:	02 00 00 a0 	beq	r0, r0, 8
	...

0000031c <__cstart>:
 31c:	fc ff f0 0f 	add	sp, sp, -4
 320:	00 00 f1 9d 	str	lr, sp, 0
 324:	22 00 00 bd 	bl	lr, 136
 328:	00 00 00 02 	add	r2, r0, 0
 32c:	00 00 20 01 	add	r1, r2, 0
 330:	07 00 00 bd 	bl	lr, 28

00000334 <.L2>:
 334:	ff ff ff b0 	bl	r0, -4

00000338 <_init>:
 338:	ad ff ff bd 	bl	lr, -332
 33c:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

00000340 <_fini>:
 340:	92 ff ff bd 	bl	lr, -440
 344:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

00000350 <_main>:
 350:	f8 ff f0 0f 	add	sp, sp, -8
 354:	00 00 f1 97 	str	r7, sp, 0
 358:	04 00 f1 9d 	str	lr, sp, 4
 35c:	70 04 00 01 	add	r1, r0, 1136
 360:	00 00 10 9d 	ldr	lr, r1, 0
 364:	00 00 00 06 	add	r6, r0, 0
 368:	40 01 00 05 	add	r5, r0, 320
 36c:	f0 00 00 07 	add	r7, r0, 240

00000370 <.L23>:
 370:	33 00 60 74 	lsr	r4, r6, 3
 374:	51 00 40 74 	lsl	r4, r4, 5
 378:	00 00 d0 03 	add	r3, lr, 0
 37c:	00 00 00 02 	add	r2, r0, 0

00000380 <.L24>:
 380:	33 00 20 71 	lsr	r1, r2, 3
 384:	1f 00 10 61 	and	r1, r1, 31
 388:	00 00 14 51 	or	r1, r1, r4
 38c:	00 00 36 91 	sth	r1, r3, 0
 390:	01 00 20 02 	add	r2, r2, 1
 394:	02 00 30 03 	add	r3, r3, 2
 398:	f9 ff 51 a2 	bne	r2, r5, -28
 39c:	01 00 60 06 	add	r6, r6, 1
 3a0:	80 02 d0 0d 	add	lr, lr, 640
 3a4:	f2 ff 71 a6 	bne	r6, r7, -56

000003a8 <.L25>:
 3a8:	ff ff ff b0 	bl	r0, -4

Disassembly of section .text.__libc_init_array:

000003b0 <___libc_init_array>:
 3b0:	f0 ff f0 0f 	add	sp, sp, -16
 3b4:	00 00 f1 97 	str	r7, sp, 0
 3b8:	04 00 f1 98 	str	r8, sp, 4
 3bc:	08 00 f1 99 	str	r9, sp, 8
 3c0:	0c 00 f1 9d 	str	lr, sp, 12
 3c4:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 3c8:	00 00 00 09 
 3cc:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 3d0:	00 00 00 07 
 3d4:	08 00 70 a9 	beq	r9, r7, 32
 3d8:	00 00 97 19 	sub	r9, r9, r7
 3dc:	25 00 90 79 	asr	r9, r9, 2
 3e0:	00 00 00 08 	add	r8, r0, 0

000003e4 <.L3>:
 3e4:	00 00 70 91 	ldr	r1, r7, 0
 3e8:	00 00 16 ad 	jl	lr, r1
 3ec:	01 00 80 08 	add	r8, r8, 1
 3f0:	04 00 70 07 	add	r7, r7, 4
 3f4:	fb ff 92 a8 	bltu	r8, r9, -20

000003f8 <.L2>:
 3f8:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 3fc:	00 00 00 02 
 400:	00 00 00 01 	add	r1, r0, 0
 404:	02 00 10 a2 	beq	r2, r1, 8
 408:	ff ff 00 c0 	bl	lr, -1040 // pre #0xffff
 40c:	fc fe ff bd 

00000410 <.L4>:
 410:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 414:	00 00 00 09 
 418:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 41c:	00 00 00 07 
 420:	08 00 70 a9 	beq	r9, r7, 32
 424:	00 00 97 19 	sub	r9, r9, r7
 428:	25 00 90 79 	asr	r9, r9, 2
 42c:	00 00 00 08 	add	r8, r0, 0

00000430 <.L6>:
 430:	00 00 70 91 	ldr	r1, r7, 0
 434:	00 00 16 ad 	jl	lr, r1
 438:	01 00 80 08 	add	r8, r8, 1
 43c:	04 00 70 07 	add	r7, r7, 4
 440:	fb ff 92 a8 	bltu	r8, r9, -20

00000444 <.L1>:
 444:	00 00 f0 97 	ldr	r7, sp, 0
 448:	04 00 f0 98 	ldr	r8, sp, 4
 44c:	08 00 f0 99 	ldr	r9, sp, 8
 450:	0c 00 f0 9d 	ldr	lr, sp, 12
 454:	10 00 f0 0f 	add	sp, sp, 16
 458:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .data:

00000460 <___dso_handle>:
	...

00000470 <_fb>:
 470:	00 00 00 04 	add	r4, r0, 0

Disassembly of section .rodata:

00000480 <_snowhousecpu_regno_to_class>:
 480:	01 00 00 00 	add	r0, r0, 1
 484:	01 00 00 00 	add	r0, r0, 1
 488:	01 00 00 00 	add	r0, r0, 1
 48c:	01 00 00 00 	add	r0, r0, 1
 490:	01 00 00 00 	add	r0, r0, 1
 494:	01 00 00 00 	add	r0, r0, 1
 498:	02 00 00 00 	add	r0, r0, 2
 49c:	01 00 00 00 	add	r0, r0, 1
 4a0:	01 00 00 00 	add	r0, r0, 1
 4a4:	01 00 00 00 	add	r0, r0, 1
 4a8:	01 00 00 00 	add	r0, r0, 1
 4ac:	01 00 00 00 	add	r0, r0, 1
 4b0:	01 00 00 00 	add	r0, r0, 1
 4b4:	01 00 00 00 	add	r0, r0, 1
 4b8:	04 00 00 00 	add	r0, r0, 4
 4bc:	05 00 00 00 	add	r0, r0, 5
 4c0:	01 00 00 00 	add	r0, r0, 1
 4c4:	01 00 00 00 	add	r0, r0, 1
 4c8:	06 00 00 00 	add	r0, r0, 6
 4cc:	00 00 00 00 	add	r0, r0, 0

000004d0 <_FB_SIZE>:
 4d0:	00 2c 01 00 	add	r0, r0, r1
	...

000004e0 <_FB_HEIGHT>:
 4e0:	f0 00 00 00 	add	r0, r0, 240
	...

000004f0 <_FB_WIDTH>:
 4f0:	40 01 00 00 	add	r0, r0, 320
	...

00000500 <_snowhousecpu_regno_to_class>:
 500:	01 00 00 00 	add	r0, r0, 1
 504:	01 00 00 00 	add	r0, r0, 1
 508:	01 00 00 00 	add	r0, r0, 1
 50c:	01 00 00 00 	add	r0, r0, 1
 510:	01 00 00 00 	add	r0, r0, 1
 514:	01 00 00 00 	add	r0, r0, 1
 518:	02 00 00 00 	add	r0, r0, 2
 51c:	01 00 00 00 	add	r0, r0, 1
 520:	01 00 00 00 	add	r0, r0, 1
 524:	01 00 00 00 	add	r0, r0, 1
 528:	01 00 00 00 	add	r0, r0, 1
 52c:	01 00 00 00 	add	r0, r0, 1
 530:	01 00 00 00 	add	r0, r0, 1
 534:	01 00 00 00 	add	r0, r0, 1
 538:	04 00 00 00 	add	r0, r0, 4
 53c:	05 00 00 00 	add	r0, r0, 5
 540:	01 00 00 00 	add	r0, r0, 1
 544:	01 00 00 00 	add	r0, r0, 1
 548:	06 00 00 00 	add	r0, r0, 6

Disassembly of section .comment:

00000000 <_stack-0x2fffffc>:
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

