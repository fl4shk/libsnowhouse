
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
  84:	3e 01 00 b0 	bl	r0, 1272 /* dst_pc=0x580 */
  88:	00 00 00 00 	add	r0, r0, 0

0000008c <_do_enable_irqs>:
  8c:	00 01 00 c0 	str	r1, r0, 16777220 /* pre #0x100 */
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
  cc:	00 01 00 c0 	ldr	r1, r0, 16777216 /* pre #0x100 */
  d0:	00 00 00 91 
  d4:	8d 00 00 bd 	bl	lr, 564 /* dst_pc=0x30c */
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
 108:	20 07 00 02 	add	r2, r0, 1824
 10c:	20 07 00 01 	add	r1, r0, 1824
 110:	05 00 10 a2 	beq	r2, r1, 20 /* dst_pc=0x128 */
 114:	00 00 00 c0 	add	r2, r0, 0 /* pre #0x0 */
 118:	00 00 00 02 
 11c:	00 00 00 03 	add	r3, r0, 0
 120:	01 00 30 a2 	beq	r2, r3, 4 /* dst_pc=0x128 */
 124:	00 00 26 ad 	jl	lr, r2

00000128 <.L1>:
 128:	00 00 f0 9d 	ldr	lr, sp, 0
 12c:	04 00 f0 0f 	add	sp, sp, 4
 130:	00 00 d6 a0 	jl	r0, lr
	...

00000140 <_register_tm_clones>:
 140:	fc ff f0 0f 	add	sp, sp, -4
 144:	00 00 f1 9d 	str	lr, sp, 0
 148:	20 07 00 01 	add	r1, r0, 1824
 14c:	ff ff 00 c0 	add	r1, r1, -1824 /* pre #0xffff */
 150:	e0 f8 10 01 
 154:	25 00 10 71 	asr	r1, r1, 2
 158:	f3 01 10 72 	lsr	r2, r1, 31
 15c:	00 00 21 02 	add	r2, r2, r1
 160:	15 00 20 72 	asr	r2, r2, 1
 164:	00 00 00 01 	add	r1, r0, 0
 168:	05 00 10 a2 	beq	r2, r1, 20 /* dst_pc=0x180 */
 16c:	00 00 00 c0 	add	r3, r0, 0 /* pre #0x0 */
 170:	00 00 00 03 
 174:	02 00 10 a3 	beq	r3, r1, 8 /* dst_pc=0x180 */
 178:	20 07 00 01 	add	r1, r0, 1824
 17c:	00 00 36 ad 	jl	lr, r3

00000180 <.L5>:
 180:	00 00 f0 9d 	ldr	lr, sp, 0
 184:	04 00 f0 0f 	add	sp, sp, 4
 188:	00 00 d6 a0 	jl	r0, lr
 18c:	00 00 00 00 	add	r0, r0, 0

00000190 <___do_global_dtors_aux>:
 190:	f8 ff f0 0f 	add	sp, sp, -8
 194:	00 00 f1 97 	str	r7, sp, 0
 198:	04 00 f1 9d 	str	lr, sp, 4
 19c:	f8 07 00 07 	add	r7, r0, 2040
 1a0:	00 00 74 92 	ldub	r2, r7, 0
 1a4:	00 00 00 01 	add	r1, r0, 0
 1a8:	03 00 11 a2 	bne	r2, r1, 12 /* dst_pc=0x1b8 */
 1ac:	d4 ff ff bd 	bl	lr, -176 /* dst_pc=0x100 */
 1b0:	01 00 00 01 	add	r1, r0, 1
 1b4:	00 00 77 91 	stb	r1, r7, 0

000001b8 <.L9>:
 1b8:	00 00 f0 97 	ldr	r7, sp, 0
 1bc:	04 00 f0 9d 	ldr	lr, sp, 4
 1c0:	08 00 f0 0f 	add	sp, sp, 8
 1c4:	00 00 d6 a0 	jl	r0, lr
	...

000001d4 <_call___do_global_dtors_aux>:
 1d4:	fc ff f0 0f 	add	sp, sp, -4
 1d8:	00 00 f1 9d 	str	lr, sp, 0
 1dc:	00 00 f0 9d 	ldr	lr, sp, 0
 1e0:	04 00 f0 0f 	add	sp, sp, 4
 1e4:	00 00 d6 a0 	jl	r0, lr
	...

000001f4 <_frame_dummy>:
 1f4:	fc ff f0 0f 	add	sp, sp, -4
 1f8:	00 00 f1 9d 	str	lr, sp, 0
 1fc:	d0 ff ff bd 	bl	lr, -192 /* dst_pc=0x140 */
 200:	00 00 f0 9d 	ldr	lr, sp, 0
 204:	04 00 f0 0f 	add	sp, sp, 4
 208:	00 00 d6 a0 	jl	r0, lr
 20c:	00 00 00 00 	add	r0, r0, 0

00000210 <_call_frame_dummy>:
 210:	fc ff f0 0f 	add	sp, sp, -4
 214:	00 00 f1 9d 	str	lr, sp, 0
 218:	00 00 f0 9d 	ldr	lr, sp, 0
 21c:	04 00 f0 0f 	add	sp, sp, 4
 220:	00 00 d6 a0 	jl	r0, lr
	...

00000230 <_mul_test>:
 230:	00 00 12 81 	umulw	r1, r1, r2
 234:	00 00 d6 a0 	jl	r0, lr
	...

00000240 <_udivw_test>:
 240:	fc ff f0 0f 	add	sp, sp, -4
 244:	00 00 f1 97 	str	r7, sp, 0
 248:	00 00 10 07 	add	r7, r1, 0
 24c:	0c 00 20 70 	cpy	hi, r2
 250:	00 00 30 01 	add	r1, r3, 0
 254:	00 00 40 02 	add	r2, r4, 0
 258:	06 00 21 87 	udivw	r7, r2, r1
 25c:	00 00 70 01 	add	r1, r7, 0
 260:	0b 00 00 72 	cpy	r2, hi
 264:	00 00 f0 97 	ldr	r7, sp, 0
 268:	04 00 f0 0f 	add	sp, sp, 4
 26c:	00 00 d6 a0 	jl	r0, lr

00000270 <_sdivw_test>:
 270:	fc ff f0 0f 	add	sp, sp, -4
 274:	00 00 f1 97 	str	r7, sp, 0
 278:	00 00 10 07 	add	r7, r1, 0
 27c:	0c 00 20 70 	cpy	hi, r2
 280:	00 00 30 01 	add	r1, r3, 0
 284:	00 00 40 02 	add	r2, r4, 0
 288:	07 00 21 87 	sdivw	r7, r2, r1
 28c:	00 00 70 01 	add	r1, r7, 0
 290:	0b 00 00 72 	cpy	r2, hi
 294:	00 00 f0 97 	ldr	r7, sp, 0
 298:	04 00 f0 0f 	add	sp, sp, 4
 29c:	00 00 d6 a0 	jl	r0, lr

000002a0 <_cmp_ltu>:
 2a0:	00 00 12 21 	sltu	r1, r1, r2
 2a4:	00 00 d6 a0 	jl	r0, lr
	...

000002b0 <_cmp_lts>:
 2b0:	01 00 12 21 	slts	r1, r1, r2
 2b4:	00 00 d6 a0 	jl	r0, lr
	...

000002c0 <_add64_test>:
 2c0:	00 00 10 05 	add	r5, r1, 0
 2c4:	00 00 13 01 	add	r1, r1, r3
 2c8:	00 00 15 25 	sltu	r5, r1, r5
 2cc:	00 00 24 02 	add	r2, r2, r4
 2d0:	00 00 52 02 	add	r2, r5, r2
 2d4:	00 00 d6 a0 	jl	r0, lr
	...

000002e0 <_sub64_test>:
 2e0:	00 00 13 13 	sub	r3, r1, r3
 2e4:	01 00 00 05 	add	r5, r0, 1
 2e8:	01 00 32 a1 	bltu	r1, r3, 4 /* dst_pc=0x2f0 */
 2ec:	00 00 00 05 	add	r5, r0, 0

000002f0 <.L17>:
 2f0:	00 00 24 12 	sub	r2, r2, r4
 2f4:	00 00 30 01 	add	r1, r3, 0
 2f8:	00 00 25 12 	sub	r2, r2, r5
 2fc:	00 00 d6 a0 	jl	r0, lr
	...

0000030c <_irq_handler_primary_logic>:
 30c:	00 00 d6 a0 	jl	r0, lr

00000310 <_memcpy>:
 310:	58 00 00 a3 	beq	r3, r0, 352 /* dst_pc=0x474 */
 314:	f8 ff f0 0f 	add	sp, sp, -8
 318:	00 00 f1 97 	str	r7, sp, 0
 31c:	04 00 f1 98 	str	r8, sp, 4
 320:	00 00 10 04 	add	r4, r1, 0
 324:	00 00 20 05 	add	r5, r2, 0
 328:	00 00 30 06 	add	r6, r3, 0
 32c:	00 00 30 08 	add	r8, r3, 0
 330:	fc ff 30 67 	and	r7, r3, -4
 334:	00 00 17 07 	add	r7, r1, r7
 338:	00 00 13 03 	add	r3, r1, r3
 33c:	00 00 12 52 	or	r2, r1, r2
 340:	03 00 20 62 	and	r2, r2, 3
 344:	43 00 01 a2 	bne	r2, r0, 268 /* dst_pc=0x454 */
 348:	04 00 00 02 	add	r2, r0, 4
 34c:	41 00 22 a6 	bltu	r6, r2, 260 /* dst_pc=0x454 */
 350:	10 00 00 02 	add	r2, r0, 16
 354:	39 00 22 a6 	bltu	r6, r2, 228 /* dst_pc=0x43c */
 358:	40 00 00 02 	add	r2, r0, 64
 35c:	28 00 22 a6 	bltu	r6, r2, 160 /* dst_pc=0x400 */
 360:	c0 ff 60 66 	and	r6, r6, -64
 364:	00 00 46 06 	add	r6, r4, r6

00000368 <.Lmemcpy_words_x16_loop>:
 368:	00 00 50 92 	ldr	r2, r5, 0
 36c:	00 00 41 92 	str	r2, r4, 0
 370:	04 00 50 92 	ldr	r2, r5, 4
 374:	04 00 41 92 	str	r2, r4, 4
 378:	08 00 50 92 	ldr	r2, r5, 8
 37c:	08 00 41 92 	str	r2, r4, 8
 380:	0c 00 50 92 	ldr	r2, r5, 12
 384:	0c 00 41 92 	str	r2, r4, 12
 388:	10 00 50 92 	ldr	r2, r5, 16
 38c:	10 00 41 92 	str	r2, r4, 16
 390:	14 00 50 92 	ldr	r2, r5, 20
 394:	14 00 41 92 	str	r2, r4, 20
 398:	18 00 50 92 	ldr	r2, r5, 24
 39c:	18 00 41 92 	str	r2, r4, 24
 3a0:	1c 00 50 92 	ldr	r2, r5, 28
 3a4:	1c 00 41 92 	str	r2, r4, 28
 3a8:	20 00 50 92 	ldr	r2, r5, 32
 3ac:	20 00 41 92 	str	r2, r4, 32
 3b0:	24 00 50 92 	ldr	r2, r5, 36
 3b4:	24 00 41 92 	str	r2, r4, 36
 3b8:	28 00 50 92 	ldr	r2, r5, 40
 3bc:	28 00 41 92 	str	r2, r4, 40
 3c0:	2c 00 50 92 	ldr	r2, r5, 44
 3c4:	2c 00 41 92 	str	r2, r4, 44
 3c8:	30 00 50 92 	ldr	r2, r5, 48
 3cc:	30 00 41 92 	str	r2, r4, 48
 3d0:	34 00 50 92 	ldr	r2, r5, 52
 3d4:	34 00 41 92 	str	r2, r4, 52
 3d8:	38 00 50 92 	ldr	r2, r5, 56
 3dc:	38 00 41 92 	str	r2, r4, 56
 3e0:	3c 00 50 92 	ldr	r2, r5, 60
 3e4:	3c 00 41 92 	str	r2, r4, 60
 3e8:	40 00 40 04 	add	r4, r4, 64
 3ec:	40 00 50 05 	add	r5, r5, 64
 3f0:	dd ff 61 a4 	bne	r4, r6, -140 /* dst_pc=0x368 */
 3f4:	1c 00 30 a6 	beq	r6, r3, 112 /* dst_pc=0x468 */
 3f8:	10 00 30 a7 	beq	r7, r3, 64 /* dst_pc=0x43c */
 3fc:	15 00 00 b0 	bl	r0, 84 /* dst_pc=0x454 */

00000400 <.Lmemcpy_words_x4_loop_pre>:
 400:	f0 ff 80 66 	and	r6, r8, -16
 404:	00 00 46 06 	add	r6, r4, r6

00000408 <.Lmemcpy_words_x4_loop>:
 408:	00 00 50 92 	ldr	r2, r5, 0
 40c:	00 00 41 92 	str	r2, r4, 0
 410:	04 00 50 92 	ldr	r2, r5, 4
 414:	04 00 41 92 	str	r2, r4, 4
 418:	08 00 50 92 	ldr	r2, r5, 8
 41c:	08 00 41 92 	str	r2, r4, 8
 420:	0c 00 50 92 	ldr	r2, r5, 12
 424:	0c 00 41 92 	str	r2, r4, 12
 428:	10 00 50 05 	add	r5, r5, 16
 42c:	10 00 40 04 	add	r4, r4, 16
 430:	f5 ff 61 a4 	bne	r4, r6, -44 /* dst_pc=0x408 */
 434:	0c 00 30 a6 	beq	r6, r3, 48 /* dst_pc=0x468 */
 438:	06 00 31 a7 	bne	r7, r3, 24 /* dst_pc=0x454 */

0000043c <.L0_mac_memcpy_words_loop>:
 43c:	00 00 50 92 	ldr	r2, r5, 0
 440:	00 00 41 92 	str	r2, r4, 0
 444:	04 00 40 04 	add	r4, r4, 4
 448:	04 00 50 05 	add	r5, r5, 4
 44c:	fb ff 71 a4 	bne	r4, r7, -20 /* dst_pc=0x43c */
 450:	05 00 30 a7 	beq	r7, r3, 20 /* dst_pc=0x468 */

00000454 <.L1_mac_mac_memcpy_bytes_loop>:
 454:	00 00 54 92 	ldub	r2, r5, 0
 458:	00 00 47 92 	stb	r2, r4, 0
 45c:	01 00 40 04 	add	r4, r4, 1
 460:	01 00 50 05 	add	r5, r5, 1
 464:	fb ff 31 a4 	bne	r4, r3, -20 /* dst_pc=0x454 */

00000468 <.Lmemcpy_pop>:
 468:	00 00 f0 97 	ldr	r7, sp, 0
 46c:	04 00 f0 98 	ldr	r8, sp, 4
 470:	08 00 f0 0f 	add	sp, sp, 8

00000474 <.Lmemcpy_end>:
 474:	00 00 d6 a0 	jl	r0, lr

00000478 <_memset>:
 478:	40 00 00 a3 	beq	r3, r0, 256 /* dst_pc=0x57c */
 47c:	f8 ff f0 0f 	add	sp, sp, -8
 480:	00 00 f1 97 	str	r7, sp, 0
 484:	04 00 f1 98 	str	r8, sp, 4
 488:	00 00 10 04 	add	r4, r1, 0
 48c:	00 00 30 06 	add	r6, r3, 0
 490:	00 00 30 08 	add	r8, r3, 0
 494:	ff 00 20 62 	and	r2, r2, 255
 498:	01 01 00 c0 	add	r5, r0, 16843009 /* pre #0x101 */
 49c:	01 01 00 05 
 4a0:	00 00 52 85 	umulw	r5, r5, r2
 4a4:	fc ff 30 67 	and	r7, r3, -4
 4a8:	00 00 17 07 	add	r7, r1, r7
 4ac:	00 00 13 03 	add	r3, r1, r3
 4b0:	03 00 10 62 	and	r2, r1, 3
 4b4:	2b 00 01 a2 	bne	r2, r0, 172 /* dst_pc=0x564 */
 4b8:	04 00 00 02 	add	r2, r0, 4
 4bc:	29 00 22 a6 	bltu	r6, r2, 164 /* dst_pc=0x564 */
 4c0:	10 00 00 02 	add	r2, r0, 16
 4c4:	23 00 22 a6 	bltu	r6, r2, 140 /* dst_pc=0x554 */
 4c8:	40 00 00 02 	add	r2, r0, 64
 4cc:	17 00 22 a6 	bltu	r6, r2, 92 /* dst_pc=0x52c */
 4d0:	c0 ff 60 66 	and	r6, r6, -64
 4d4:	00 00 46 06 	add	r6, r4, r6

000004d8 <.Lmemset_words_x16_loop>:
 4d8:	00 00 41 95 	str	r5, r4, 0
 4dc:	04 00 41 95 	str	r5, r4, 4
 4e0:	08 00 41 95 	str	r5, r4, 8
 4e4:	0c 00 41 95 	str	r5, r4, 12
 4e8:	10 00 41 95 	str	r5, r4, 16
 4ec:	14 00 41 95 	str	r5, r4, 20
 4f0:	18 00 41 95 	str	r5, r4, 24
 4f4:	1c 00 41 95 	str	r5, r4, 28
 4f8:	20 00 41 95 	str	r5, r4, 32
 4fc:	24 00 41 95 	str	r5, r4, 36
 500:	28 00 41 95 	str	r5, r4, 40
 504:	2c 00 41 95 	str	r5, r4, 44
 508:	30 00 41 95 	str	r5, r4, 48
 50c:	34 00 41 95 	str	r5, r4, 52
 510:	38 00 41 95 	str	r5, r4, 56
 514:	3c 00 41 95 	str	r5, r4, 60
 518:	40 00 40 04 	add	r4, r4, 64
 51c:	ee ff 61 a4 	bne	r4, r6, -72 /* dst_pc=0x4d8 */
 520:	13 00 30 a6 	beq	r6, r3, 76 /* dst_pc=0x570 */
 524:	0b 00 30 a7 	beq	r7, r3, 44 /* dst_pc=0x554 */
 528:	0e 00 00 b0 	bl	r0, 56 /* dst_pc=0x564 */

0000052c <.Lmemset_words_x4_loop_pre>:
 52c:	f0 ff 80 66 	and	r6, r8, -16
 530:	00 00 46 06 	add	r6, r4, r6

00000534 <.Lmemset_words_x4_loop>:
 534:	00 00 41 95 	str	r5, r4, 0
 538:	04 00 41 95 	str	r5, r4, 4
 53c:	08 00 41 95 	str	r5, r4, 8
 540:	0c 00 41 95 	str	r5, r4, 12
 544:	10 00 40 04 	add	r4, r4, 16
 548:	fa ff 61 a4 	bne	r4, r6, -24 /* dst_pc=0x534 */
 54c:	08 00 30 a6 	beq	r6, r3, 32 /* dst_pc=0x570 */
 550:	04 00 31 a7 	bne	r7, r3, 16 /* dst_pc=0x564 */

00000554 <.Lmemset_remaining_words_loop>:
 554:	00 00 41 95 	str	r5, r4, 0
 558:	04 00 40 04 	add	r4, r4, 4
 55c:	fd ff 71 a4 	bne	r4, r7, -12 /* dst_pc=0x554 */
 560:	03 00 30 a7 	beq	r7, r3, 12 /* dst_pc=0x570 */

00000564 <.Lmemset_remaining_bytes_loop>:
 564:	00 00 47 95 	stb	r5, r4, 0
 568:	01 00 40 04 	add	r4, r4, 1
 56c:	fd ff 31 a4 	bne	r4, r3, -12 /* dst_pc=0x564 */

00000570 <.Lmemset_pop>:
 570:	00 00 f0 97 	ldr	r7, sp, 0
 574:	04 00 f0 98 	ldr	r8, sp, 4
 578:	08 00 f0 0f 	add	sp, sp, 8

0000057c <.Lmemset_end>:
 57c:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .init:

00000580 <_start>:
	...
 588:	ff 01 00 c0 	add	sp, r0, 33554428 /* pre #0x1ff */
 58c:	fc ff 00 0f 
 590:	02 00 00 a0 	beq	r0, r0, 8 /* dst_pc=0x59c */
	...

0000059c <__cstart>:
 59c:	fc ff f0 0f 	add	sp, sp, -4
 5a0:	00 00 f1 9d 	str	lr, sp, 0
 5a4:	14 00 00 03 	add	r3, r0, 20
 5a8:	00 07 00 02 	add	r2, r0, 1792
 5ac:	00 07 00 01 	add	r1, r0, 1792
 5b0:	57 ff ff bd 	bl	lr, -676 /* dst_pc=0x310 */
 5b4:	0d 00 00 03 	add	r3, r0, 13
 5b8:	00 00 00 02 	add	r2, r0, 0
 5bc:	ec 07 00 01 	add	r1, r0, 2028
 5c0:	ad ff ff bd 	bl	lr, -332 /* dst_pc=0x478 */
 5c4:	22 00 00 bd 	bl	lr, 136 /* dst_pc=0x650 */
 5c8:	00 00 00 02 	add	r2, r0, 0
 5cc:	00 00 20 01 	add	r1, r2, 0
 5d0:	07 00 00 bd 	bl	lr, 28 /* dst_pc=0x5f0 */

000005d4 <.L2>:
 5d4:	ff ff ff b0 	bl	r0, -4 /* dst_pc=0x5d4 */

000005d8 <_init>:
 5d8:	06 ff ff bd 	bl	lr, -1000 /* dst_pc=0x1f4 */
 5dc:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

000005e0 <_fini>:
 5e0:	eb fe ff bd 	bl	lr, -1108 /* dst_pc=0x190 */
 5e4:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

000005f0 <_main>:
 5f0:	f8 ff f0 0f 	add	sp, sp, -8
 5f4:	00 00 f1 97 	str	r7, sp, 0
 5f8:	04 00 f1 9d 	str	lr, sp, 4
 5fc:	10 07 00 01 	add	r1, r0, 1808
 600:	00 00 10 9d 	ldr	lr, r1, 0
 604:	00 00 00 06 	add	r6, r0, 0
 608:	40 01 00 05 	add	r5, r0, 320
 60c:	f0 00 00 07 	add	r7, r0, 240

00000610 <.L23>:
 610:	33 00 60 74 	lsr	r4, r6, 3
 614:	51 00 40 74 	lsl	r4, r4, 5
 618:	00 00 d0 03 	add	r3, lr, 0
 61c:	00 00 00 02 	add	r2, r0, 0

00000620 <.L24>:
 620:	33 00 20 71 	lsr	r1, r2, 3
 624:	1f 00 10 61 	and	r1, r1, 31
 628:	00 00 14 51 	or	r1, r1, r4
 62c:	00 00 36 91 	sth	r1, r3, 0
 630:	01 00 20 02 	add	r2, r2, 1
 634:	02 00 30 03 	add	r3, r3, 2
 638:	f9 ff 51 a2 	bne	r2, r5, -28 /* dst_pc=0x620 */
 63c:	01 00 60 06 	add	r6, r6, 1
 640:	80 02 d0 0d 	add	lr, lr, 640
 644:	f2 ff 71 a6 	bne	r6, r7, -56 /* dst_pc=0x610 */

00000648 <.L25>:
 648:	ff ff ff b0 	bl	r0, -4 /* dst_pc=0x648 */

Disassembly of section .text.__libc_init_array:

00000650 <___libc_init_array>:
 650:	f4 ff f0 0f 	add	sp, sp, -12
 654:	00 00 f1 97 	str	r7, sp, 0
 658:	04 00 f1 98 	str	r8, sp, 4
 65c:	08 00 f1 9d 	str	lr, sp, 8
 660:	00 00 00 c0 	add	r7, r0, 0 /* pre #0x0 */
 664:	00 00 00 07 
 668:	00 00 00 c0 	add	r7, r7, 0 /* pre #0x0 */
 66c:	00 00 70 07 
 670:	25 00 70 77 	asr	r7, r7, 2
 674:	00 00 00 08 	add	r8, r0, 0

00000678 <.L2>:
 678:	12 00 71 a8 	bne	r8, r7, 72 /* dst_pc=0x6c4 */
 67c:	00 00 00 c0 	add	r2, r0, 0 /* pre #0x0 */
 680:	00 00 00 02 
 684:	00 00 00 01 	add	r1, r0, 0
 688:	02 00 10 a2 	beq	r2, r1, 8 /* dst_pc=0x694 */
 68c:	ff ff 00 c0 	bl	lr, -1684 /* dst_pc=0xfffffffc */ /* pre #0xffff */
 690:	5b fe ff bd 

00000694 <.L4>:
 694:	00 00 00 c0 	add	r7, r0, 0 /* pre #0x0 */
 698:	00 00 00 07 
 69c:	00 00 00 c0 	add	r7, r7, 0 /* pre #0x0 */
 6a0:	00 00 70 07 
 6a4:	25 00 70 77 	asr	r7, r7, 2
 6a8:	00 00 00 08 	add	r8, r0, 0

000006ac <.L5>:
 6ac:	0b 00 71 a8 	bne	r8, r7, 44 /* dst_pc=0x6dc */
 6b0:	00 00 f0 97 	ldr	r7, sp, 0
 6b4:	04 00 f0 98 	ldr	r8, sp, 4
 6b8:	08 00 f0 9d 	ldr	lr, sp, 8
 6bc:	0c 00 f0 0f 	add	sp, sp, 12
 6c0:	00 00 d6 a0 	jl	r0, lr

000006c4 <.L3>:
 6c4:	21 00 80 71 	lsl	r1, r8, 2
 6c8:	00 00 00 c0 	ldr	r1, r1, 0 /* pre #0x0 */
 6cc:	00 00 10 91 
 6d0:	00 00 16 ad 	jl	lr, r1
 6d4:	01 00 80 08 	add	r8, r8, 1
 6d8:	e7 ff ff b0 	bl	r0, -100 /* dst_pc=0x678 */

000006dc <.L6>:
 6dc:	21 00 80 71 	lsl	r1, r8, 2
 6e0:	00 00 00 c0 	ldr	r1, r1, 0 /* pre #0x0 */
 6e4:	00 00 10 91 
 6e8:	00 00 16 ad 	jl	lr, r1
 6ec:	01 00 80 08 	add	r8, r8, 1
 6f0:	ee ff ff b0 	bl	r0, -72 /* dst_pc=0x6ac */

Disassembly of section .data:

00000700 <___dso_handle>:
	...

00000710 <_fb>:
 710:	00 00 00 04 	add	r4, r0, 0

Disassembly of section .rodata:

00000720 <_snowhousecpu_regno_to_class>:
 720:	01 00 00 00 	add	r0, r0, 1
 724:	01 00 00 00 	add	r0, r0, 1
 728:	01 00 00 00 	add	r0, r0, 1
 72c:	01 00 00 00 	add	r0, r0, 1
 730:	01 00 00 00 	add	r0, r0, 1
 734:	01 00 00 00 	add	r0, r0, 1
 738:	01 00 00 00 	add	r0, r0, 1
 73c:	02 00 00 00 	add	r0, r0, 2
 740:	01 00 00 00 	add	r0, r0, 1
 744:	01 00 00 00 	add	r0, r0, 1
 748:	01 00 00 00 	add	r0, r0, 1
 74c:	01 00 00 00 	add	r0, r0, 1
 750:	01 00 00 00 	add	r0, r0, 1
 754:	01 00 00 00 	add	r0, r0, 1
 758:	04 00 00 00 	add	r0, r0, 4
 75c:	05 00 00 00 	add	r0, r0, 5
 760:	01 00 00 00 	add	r0, r0, 1
 764:	01 00 00 00 	add	r0, r0, 1
 768:	06 00 00 00 	add	r0, r0, 6
 76c:	00 00 00 00 	add	r0, r0, 0

00000770 <_FB_SIZE>:
 770:	00 2c 01 00 	add	r0, r0, r1
	...

00000780 <_FB_HEIGHT>:
 780:	f0 00 00 00 	add	r0, r0, 240
	...

00000790 <_FB_WIDTH>:
 790:	40 01 00 00 	add	r0, r0, 320
	...

000007a0 <_snowhousecpu_regno_to_class>:
 7a0:	01 00 00 00 	add	r0, r0, 1
 7a4:	01 00 00 00 	add	r0, r0, 1
 7a8:	01 00 00 00 	add	r0, r0, 1
 7ac:	01 00 00 00 	add	r0, r0, 1
 7b0:	01 00 00 00 	add	r0, r0, 1
 7b4:	01 00 00 00 	add	r0, r0, 1
 7b8:	01 00 00 00 	add	r0, r0, 1
 7bc:	02 00 00 00 	add	r0, r0, 2
 7c0:	01 00 00 00 	add	r0, r0, 1
 7c4:	01 00 00 00 	add	r0, r0, 1
 7c8:	01 00 00 00 	add	r0, r0, 1
 7cc:	01 00 00 00 	add	r0, r0, 1
 7d0:	01 00 00 00 	add	r0, r0, 1
 7d4:	01 00 00 00 	add	r0, r0, 1
 7d8:	04 00 00 00 	add	r0, r0, 4
 7dc:	05 00 00 00 	add	r0, r0, 5
 7e0:	01 00 00 00 	add	r0, r0, 1
 7e4:	01 00 00 00 	add	r0, r0, 1
 7e8:	06 00 00 00 	add	r0, r0, 6

Disassembly of section .comment:

00000000 <_stack-0x1fffffc>:
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

