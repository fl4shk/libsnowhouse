
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
  84:	42 01 00 b0 	bl	r0, 1288 /* dst_pc=0x590 */
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
  d4:	91 00 00 bd 	bl	lr, 580 /* dst_pc=0x31c */
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
 108:	30 07 00 02 	add	r2, r0, 1840
 10c:	30 07 00 01 	add	r1, r0, 1840
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
 148:	30 07 00 01 	add	r1, r0, 1840
 14c:	ff ff 00 c0 	add	r1, r1, -1840 /* pre #0xffff */
 150:	d0 f8 10 01 
 154:	25 00 10 71 	asr	r1, r1, 2
 158:	f3 01 10 72 	lsr	r2, r1, 31
 15c:	00 00 21 02 	add	r2, r2, r1
 160:	15 00 20 72 	asr	r2, r2, 1
 164:	00 00 00 01 	add	r1, r0, 0
 168:	05 00 10 a2 	beq	r2, r1, 20 /* dst_pc=0x180 */
 16c:	00 00 00 c0 	add	r3, r0, 0 /* pre #0x0 */
 170:	00 00 00 03 
 174:	02 00 10 a3 	beq	r3, r1, 8 /* dst_pc=0x180 */
 178:	30 07 00 01 	add	r1, r0, 1840
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
 19c:	08 08 00 07 	add	r7, r0, 2056
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
 230:	fc ff f0 0f 	add	sp, sp, -4
 234:	00 00 f1 97 	str	r7, sp, 0
 238:	00 00 12 81 	umulw	r1, r1, r2
 23c:	00 00 f0 97 	ldr	r7, sp, 0
 240:	04 00 f0 0f 	add	sp, sp, 4
 244:	00 00 d6 a0 	jl	r0, lr
	...

00000250 <_udivw_test>:
 250:	fc ff f0 0f 	add	sp, sp, -4
 254:	00 00 f1 97 	str	r7, sp, 0
 258:	00 00 10 07 	add	r7, r1, 0
 25c:	0c 00 20 70 	cpy	hi, r2
 260:	00 00 30 01 	add	r1, r3, 0
 264:	00 00 40 02 	add	r2, r4, 0
 268:	06 00 21 87 	udivw	r7, r2, r1
 26c:	00 00 70 01 	add	r1, r7, 0
 270:	0b 00 00 72 	cpy	r2, hi
 274:	00 00 f0 97 	ldr	r7, sp, 0
 278:	04 00 f0 0f 	add	sp, sp, 4
 27c:	00 00 d6 a0 	jl	r0, lr

00000280 <_sdivw_test>:
 280:	fc ff f0 0f 	add	sp, sp, -4
 284:	00 00 f1 97 	str	r7, sp, 0
 288:	00 00 10 07 	add	r7, r1, 0
 28c:	0c 00 20 70 	cpy	hi, r2
 290:	00 00 30 01 	add	r1, r3, 0
 294:	00 00 40 02 	add	r2, r4, 0
 298:	07 00 21 87 	sdivw	r7, r2, r1
 29c:	00 00 70 01 	add	r1, r7, 0
 2a0:	0b 00 00 72 	cpy	r2, hi
 2a4:	00 00 f0 97 	ldr	r7, sp, 0
 2a8:	04 00 f0 0f 	add	sp, sp, 4
 2ac:	00 00 d6 a0 	jl	r0, lr

000002b0 <_cmp_ltu>:
 2b0:	00 00 12 21 	sltu	r1, r1, r2
 2b4:	00 00 d6 a0 	jl	r0, lr
	...

000002c0 <_cmp_lts>:
 2c0:	01 00 12 21 	slts	r1, r1, r2
 2c4:	00 00 d6 a0 	jl	r0, lr
	...

000002d0 <_add64_test>:
 2d0:	00 00 10 05 	add	r5, r1, 0
 2d4:	00 00 13 01 	add	r1, r1, r3
 2d8:	00 00 15 25 	sltu	r5, r1, r5
 2dc:	00 00 24 02 	add	r2, r2, r4
 2e0:	00 00 52 02 	add	r2, r5, r2
 2e4:	00 00 d6 a0 	jl	r0, lr
	...

000002f0 <_sub64_test>:
 2f0:	00 00 13 13 	sub	r3, r1, r3
 2f4:	01 00 00 05 	add	r5, r0, 1
 2f8:	01 00 32 a1 	bltu	r1, r3, 4 /* dst_pc=0x300 */
 2fc:	00 00 00 05 	add	r5, r0, 0

00000300 <.L17>:
 300:	00 00 24 12 	sub	r2, r2, r4
 304:	00 00 30 01 	add	r1, r3, 0
 308:	00 00 25 12 	sub	r2, r2, r5
 30c:	00 00 d6 a0 	jl	r0, lr
	...

0000031c <_irq_handler_primary_logic>:
 31c:	00 00 d6 a0 	jl	r0, lr

00000320 <_memcpy>:
 320:	58 00 00 a3 	beq	r3, r0, 352 /* dst_pc=0x484 */
 324:	f8 ff f0 0f 	add	sp, sp, -8
 328:	00 00 f1 97 	str	r7, sp, 0
 32c:	04 00 f1 98 	str	r8, sp, 4
 330:	00 00 10 04 	add	r4, r1, 0
 334:	00 00 20 05 	add	r5, r2, 0
 338:	00 00 30 06 	add	r6, r3, 0
 33c:	00 00 30 08 	add	r8, r3, 0
 340:	fc ff 30 67 	and	r7, r3, -4
 344:	00 00 17 07 	add	r7, r1, r7
 348:	00 00 13 03 	add	r3, r1, r3
 34c:	00 00 12 52 	or	r2, r1, r2
 350:	03 00 20 62 	and	r2, r2, 3
 354:	43 00 01 a2 	bne	r2, r0, 268 /* dst_pc=0x464 */
 358:	04 00 00 02 	add	r2, r0, 4
 35c:	41 00 22 a6 	bltu	r6, r2, 260 /* dst_pc=0x464 */
 360:	10 00 00 02 	add	r2, r0, 16
 364:	39 00 22 a6 	bltu	r6, r2, 228 /* dst_pc=0x44c */
 368:	40 00 00 02 	add	r2, r0, 64
 36c:	28 00 22 a6 	bltu	r6, r2, 160 /* dst_pc=0x410 */
 370:	c0 ff 60 66 	and	r6, r6, -64
 374:	00 00 46 06 	add	r6, r4, r6

00000378 <.Lmemcpy_words_x16_loop>:
 378:	00 00 50 92 	ldr	r2, r5, 0
 37c:	00 00 41 92 	str	r2, r4, 0
 380:	04 00 50 92 	ldr	r2, r5, 4
 384:	04 00 41 92 	str	r2, r4, 4
 388:	08 00 50 92 	ldr	r2, r5, 8
 38c:	08 00 41 92 	str	r2, r4, 8
 390:	0c 00 50 92 	ldr	r2, r5, 12
 394:	0c 00 41 92 	str	r2, r4, 12
 398:	10 00 50 92 	ldr	r2, r5, 16
 39c:	10 00 41 92 	str	r2, r4, 16
 3a0:	14 00 50 92 	ldr	r2, r5, 20
 3a4:	14 00 41 92 	str	r2, r4, 20
 3a8:	18 00 50 92 	ldr	r2, r5, 24
 3ac:	18 00 41 92 	str	r2, r4, 24
 3b0:	1c 00 50 92 	ldr	r2, r5, 28
 3b4:	1c 00 41 92 	str	r2, r4, 28
 3b8:	20 00 50 92 	ldr	r2, r5, 32
 3bc:	20 00 41 92 	str	r2, r4, 32
 3c0:	24 00 50 92 	ldr	r2, r5, 36
 3c4:	24 00 41 92 	str	r2, r4, 36
 3c8:	28 00 50 92 	ldr	r2, r5, 40
 3cc:	28 00 41 92 	str	r2, r4, 40
 3d0:	2c 00 50 92 	ldr	r2, r5, 44
 3d4:	2c 00 41 92 	str	r2, r4, 44
 3d8:	30 00 50 92 	ldr	r2, r5, 48
 3dc:	30 00 41 92 	str	r2, r4, 48
 3e0:	34 00 50 92 	ldr	r2, r5, 52
 3e4:	34 00 41 92 	str	r2, r4, 52
 3e8:	38 00 50 92 	ldr	r2, r5, 56
 3ec:	38 00 41 92 	str	r2, r4, 56
 3f0:	3c 00 50 92 	ldr	r2, r5, 60
 3f4:	3c 00 41 92 	str	r2, r4, 60
 3f8:	40 00 40 04 	add	r4, r4, 64
 3fc:	40 00 50 05 	add	r5, r5, 64
 400:	dd ff 61 a4 	bne	r4, r6, -140 /* dst_pc=0x378 */
 404:	1c 00 30 a6 	beq	r6, r3, 112 /* dst_pc=0x478 */
 408:	10 00 30 a7 	beq	r7, r3, 64 /* dst_pc=0x44c */
 40c:	15 00 00 b0 	bl	r0, 84 /* dst_pc=0x464 */

00000410 <.Lmemcpy_words_x4_loop_pre>:
 410:	f0 ff 80 66 	and	r6, r8, -16
 414:	00 00 46 06 	add	r6, r4, r6

00000418 <.Lmemcpy_words_x4_loop>:
 418:	00 00 50 92 	ldr	r2, r5, 0
 41c:	00 00 41 92 	str	r2, r4, 0
 420:	04 00 50 92 	ldr	r2, r5, 4
 424:	04 00 41 92 	str	r2, r4, 4
 428:	08 00 50 92 	ldr	r2, r5, 8
 42c:	08 00 41 92 	str	r2, r4, 8
 430:	0c 00 50 92 	ldr	r2, r5, 12
 434:	0c 00 41 92 	str	r2, r4, 12
 438:	10 00 50 05 	add	r5, r5, 16
 43c:	10 00 40 04 	add	r4, r4, 16
 440:	f5 ff 61 a4 	bne	r4, r6, -44 /* dst_pc=0x418 */
 444:	0c 00 30 a6 	beq	r6, r3, 48 /* dst_pc=0x478 */
 448:	06 00 31 a7 	bne	r7, r3, 24 /* dst_pc=0x464 */

0000044c <.L0_mac_memcpy_words_loop>:
 44c:	00 00 50 92 	ldr	r2, r5, 0
 450:	00 00 41 92 	str	r2, r4, 0
 454:	04 00 40 04 	add	r4, r4, 4
 458:	04 00 50 05 	add	r5, r5, 4
 45c:	fb ff 71 a4 	bne	r4, r7, -20 /* dst_pc=0x44c */
 460:	05 00 30 a7 	beq	r7, r3, 20 /* dst_pc=0x478 */

00000464 <.L1_mac_mac_memcpy_bytes_loop>:
 464:	00 00 54 92 	ldub	r2, r5, 0
 468:	00 00 47 92 	stb	r2, r4, 0
 46c:	01 00 40 04 	add	r4, r4, 1
 470:	01 00 50 05 	add	r5, r5, 1
 474:	fb ff 31 a4 	bne	r4, r3, -20 /* dst_pc=0x464 */

00000478 <.Lmemcpy_pop>:
 478:	00 00 f0 97 	ldr	r7, sp, 0
 47c:	04 00 f0 98 	ldr	r8, sp, 4
 480:	08 00 f0 0f 	add	sp, sp, 8

00000484 <.Lmemcpy_end>:
 484:	00 00 d6 a0 	jl	r0, lr

00000488 <_memset>:
 488:	40 00 00 a3 	beq	r3, r0, 256 /* dst_pc=0x58c */
 48c:	f8 ff f0 0f 	add	sp, sp, -8
 490:	00 00 f1 97 	str	r7, sp, 0
 494:	04 00 f1 98 	str	r8, sp, 4
 498:	00 00 10 04 	add	r4, r1, 0
 49c:	00 00 30 06 	add	r6, r3, 0
 4a0:	00 00 30 08 	add	r8, r3, 0
 4a4:	ff 00 20 62 	and	r2, r2, 255
 4a8:	01 01 00 c0 	add	r5, r0, 16843009 /* pre #0x101 */
 4ac:	01 01 00 05 
 4b0:	00 00 52 85 	umulw	r5, r5, r2
 4b4:	fc ff 30 67 	and	r7, r3, -4
 4b8:	00 00 17 07 	add	r7, r1, r7
 4bc:	00 00 13 03 	add	r3, r1, r3
 4c0:	03 00 10 62 	and	r2, r1, 3
 4c4:	2b 00 01 a2 	bne	r2, r0, 172 /* dst_pc=0x574 */
 4c8:	04 00 00 02 	add	r2, r0, 4
 4cc:	29 00 22 a6 	bltu	r6, r2, 164 /* dst_pc=0x574 */
 4d0:	10 00 00 02 	add	r2, r0, 16
 4d4:	23 00 22 a6 	bltu	r6, r2, 140 /* dst_pc=0x564 */
 4d8:	40 00 00 02 	add	r2, r0, 64
 4dc:	17 00 22 a6 	bltu	r6, r2, 92 /* dst_pc=0x53c */
 4e0:	c0 ff 60 66 	and	r6, r6, -64
 4e4:	00 00 46 06 	add	r6, r4, r6

000004e8 <.Lmemset_words_x16_loop>:
 4e8:	00 00 41 95 	str	r5, r4, 0
 4ec:	04 00 41 95 	str	r5, r4, 4
 4f0:	08 00 41 95 	str	r5, r4, 8
 4f4:	0c 00 41 95 	str	r5, r4, 12
 4f8:	10 00 41 95 	str	r5, r4, 16
 4fc:	14 00 41 95 	str	r5, r4, 20
 500:	18 00 41 95 	str	r5, r4, 24
 504:	1c 00 41 95 	str	r5, r4, 28
 508:	20 00 41 95 	str	r5, r4, 32
 50c:	24 00 41 95 	str	r5, r4, 36
 510:	28 00 41 95 	str	r5, r4, 40
 514:	2c 00 41 95 	str	r5, r4, 44
 518:	30 00 41 95 	str	r5, r4, 48
 51c:	34 00 41 95 	str	r5, r4, 52
 520:	38 00 41 95 	str	r5, r4, 56
 524:	3c 00 41 95 	str	r5, r4, 60
 528:	40 00 40 04 	add	r4, r4, 64
 52c:	ee ff 61 a4 	bne	r4, r6, -72 /* dst_pc=0x4e8 */
 530:	13 00 30 a6 	beq	r6, r3, 76 /* dst_pc=0x580 */
 534:	0b 00 30 a7 	beq	r7, r3, 44 /* dst_pc=0x564 */
 538:	0e 00 00 b0 	bl	r0, 56 /* dst_pc=0x574 */

0000053c <.Lmemset_words_x4_loop_pre>:
 53c:	f0 ff 80 66 	and	r6, r8, -16
 540:	00 00 46 06 	add	r6, r4, r6

00000544 <.Lmemset_words_x4_loop>:
 544:	00 00 41 95 	str	r5, r4, 0
 548:	04 00 41 95 	str	r5, r4, 4
 54c:	08 00 41 95 	str	r5, r4, 8
 550:	0c 00 41 95 	str	r5, r4, 12
 554:	10 00 40 04 	add	r4, r4, 16
 558:	fa ff 61 a4 	bne	r4, r6, -24 /* dst_pc=0x544 */
 55c:	08 00 30 a6 	beq	r6, r3, 32 /* dst_pc=0x580 */
 560:	04 00 31 a7 	bne	r7, r3, 16 /* dst_pc=0x574 */

00000564 <.Lmemset_remaining_words_loop>:
 564:	00 00 41 95 	str	r5, r4, 0
 568:	04 00 40 04 	add	r4, r4, 4
 56c:	fd ff 71 a4 	bne	r4, r7, -12 /* dst_pc=0x564 */
 570:	03 00 30 a7 	beq	r7, r3, 12 /* dst_pc=0x580 */

00000574 <.Lmemset_remaining_bytes_loop>:
 574:	00 00 47 95 	stb	r5, r4, 0
 578:	01 00 40 04 	add	r4, r4, 1
 57c:	fd ff 31 a4 	bne	r4, r3, -12 /* dst_pc=0x574 */

00000580 <.Lmemset_pop>:
 580:	00 00 f0 97 	ldr	r7, sp, 0
 584:	04 00 f0 98 	ldr	r8, sp, 4
 588:	08 00 f0 0f 	add	sp, sp, 8

0000058c <.Lmemset_end>:
 58c:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .init:

00000590 <_start>:
	...
 598:	ff 01 00 c0 	add	sp, r0, 33554428 /* pre #0x1ff */
 59c:	fc ff 00 0f 
 5a0:	02 00 00 a0 	beq	r0, r0, 8 /* dst_pc=0x5ac */
	...

000005ac <__cstart>:
 5ac:	fc ff f0 0f 	add	sp, sp, -4
 5b0:	00 00 f1 9d 	str	lr, sp, 0
 5b4:	14 00 00 03 	add	r3, r0, 20
 5b8:	10 07 00 02 	add	r2, r0, 1808
 5bc:	10 07 00 01 	add	r1, r0, 1808
 5c0:	57 ff ff bd 	bl	lr, -676 /* dst_pc=0x320 */
 5c4:	0d 00 00 03 	add	r3, r0, 13
 5c8:	00 00 00 02 	add	r2, r0, 0
 5cc:	fc 07 00 01 	add	r1, r0, 2044
 5d0:	ad ff ff bd 	bl	lr, -332 /* dst_pc=0x488 */
 5d4:	22 00 00 bd 	bl	lr, 136 /* dst_pc=0x660 */
 5d8:	00 00 00 02 	add	r2, r0, 0
 5dc:	00 00 20 01 	add	r1, r2, 0
 5e0:	07 00 00 bd 	bl	lr, 28 /* dst_pc=0x600 */

000005e4 <.L2>:
 5e4:	ff ff ff b0 	bl	r0, -4 /* dst_pc=0x5e4 */

000005e8 <_init>:
 5e8:	02 ff ff bd 	bl	lr, -1016 /* dst_pc=0x1f4 */
 5ec:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

000005f0 <_fini>:
 5f0:	e7 fe ff bd 	bl	lr, -1124 /* dst_pc=0x190 */
 5f4:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

00000600 <_main>:
 600:	f8 ff f0 0f 	add	sp, sp, -8
 604:	00 00 f1 97 	str	r7, sp, 0
 608:	04 00 f1 9d 	str	lr, sp, 4
 60c:	20 07 00 01 	add	r1, r0, 1824
 610:	00 00 10 9d 	ldr	lr, r1, 0
 614:	00 00 00 06 	add	r6, r0, 0
 618:	40 01 00 05 	add	r5, r0, 320
 61c:	f0 00 00 07 	add	r7, r0, 240

00000620 <.L23>:
 620:	33 00 60 74 	lsr	r4, r6, 3
 624:	51 00 40 74 	lsl	r4, r4, 5
 628:	00 00 d0 03 	add	r3, lr, 0
 62c:	00 00 00 02 	add	r2, r0, 0

00000630 <.L24>:
 630:	33 00 20 71 	lsr	r1, r2, 3
 634:	1f 00 10 61 	and	r1, r1, 31
 638:	00 00 14 51 	or	r1, r1, r4
 63c:	00 00 36 91 	sth	r1, r3, 0
 640:	01 00 20 02 	add	r2, r2, 1
 644:	02 00 30 03 	add	r3, r3, 2
 648:	f9 ff 51 a2 	bne	r2, r5, -28 /* dst_pc=0x630 */
 64c:	01 00 60 06 	add	r6, r6, 1
 650:	80 02 d0 0d 	add	lr, lr, 640
 654:	f2 ff 71 a6 	bne	r6, r7, -56 /* dst_pc=0x620 */

00000658 <.L25>:
 658:	ff ff ff b0 	bl	r0, -4 /* dst_pc=0x658 */

Disassembly of section .text.__libc_init_array:

00000660 <___libc_init_array>:
 660:	f0 ff f0 0f 	add	sp, sp, -16
 664:	00 00 f1 97 	str	r7, sp, 0
 668:	04 00 f1 98 	str	r8, sp, 4
 66c:	08 00 f1 99 	str	r9, sp, 8
 670:	0c 00 f1 9d 	str	lr, sp, 12
 674:	00 00 00 c0 	add	r9, r0, 0 /* pre #0x0 */
 678:	00 00 00 09 
 67c:	00 00 00 c0 	add	r7, r0, 0 /* pre #0x0 */
 680:	00 00 00 07 
 684:	08 00 70 a9 	beq	r9, r7, 32 /* dst_pc=0x6a8 */
 688:	00 00 97 19 	sub	r9, r9, r7
 68c:	25 00 90 79 	asr	r9, r9, 2
 690:	00 00 00 08 	add	r8, r0, 0

00000694 <.L3>:
 694:	00 00 70 91 	ldr	r1, r7, 0
 698:	00 00 16 ad 	jl	lr, r1
 69c:	01 00 80 08 	add	r8, r8, 1
 6a0:	04 00 70 07 	add	r7, r7, 4
 6a4:	fb ff 92 a8 	bltu	r8, r9, -20 /* dst_pc=0x694 */

000006a8 <.L2>:
 6a8:	00 00 00 c0 	add	r2, r0, 0 /* pre #0x0 */
 6ac:	00 00 00 02 
 6b0:	00 00 00 01 	add	r1, r0, 0
 6b4:	02 00 10 a2 	beq	r2, r1, 8 /* dst_pc=0x6c0 */
 6b8:	ff ff 00 c0 	bl	lr, -1728 /* dst_pc=0xfffffffc */ /* pre #0xffff */
 6bc:	50 fe ff bd 

000006c0 <.L4>:
 6c0:	00 00 00 c0 	add	r9, r0, 0 /* pre #0x0 */
 6c4:	00 00 00 09 
 6c8:	00 00 00 c0 	add	r7, r0, 0 /* pre #0x0 */
 6cc:	00 00 00 07 
 6d0:	08 00 70 a9 	beq	r9, r7, 32 /* dst_pc=0x6f4 */
 6d4:	00 00 97 19 	sub	r9, r9, r7
 6d8:	25 00 90 79 	asr	r9, r9, 2
 6dc:	00 00 00 08 	add	r8, r0, 0

000006e0 <.L6>:
 6e0:	00 00 70 91 	ldr	r1, r7, 0
 6e4:	00 00 16 ad 	jl	lr, r1
 6e8:	01 00 80 08 	add	r8, r8, 1
 6ec:	04 00 70 07 	add	r7, r7, 4
 6f0:	fb ff 92 a8 	bltu	r8, r9, -20 /* dst_pc=0x6e0 */

000006f4 <.L1>:
 6f4:	00 00 f0 97 	ldr	r7, sp, 0
 6f8:	04 00 f0 98 	ldr	r8, sp, 4
 6fc:	08 00 f0 99 	ldr	r9, sp, 8
 700:	0c 00 f0 9d 	ldr	lr, sp, 12
 704:	10 00 f0 0f 	add	sp, sp, 16
 708:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .data:

00000710 <___dso_handle>:
	...

00000720 <_fb>:
 720:	00 00 00 04 	add	r4, r0, 0

Disassembly of section .rodata:

00000730 <_snowhousecpu_regno_to_class>:
 730:	01 00 00 00 	add	r0, r0, 1
 734:	01 00 00 00 	add	r0, r0, 1
 738:	01 00 00 00 	add	r0, r0, 1
 73c:	01 00 00 00 	add	r0, r0, 1
 740:	01 00 00 00 	add	r0, r0, 1
 744:	01 00 00 00 	add	r0, r0, 1
 748:	01 00 00 00 	add	r0, r0, 1
 74c:	02 00 00 00 	add	r0, r0, 2
 750:	01 00 00 00 	add	r0, r0, 1
 754:	01 00 00 00 	add	r0, r0, 1
 758:	01 00 00 00 	add	r0, r0, 1
 75c:	01 00 00 00 	add	r0, r0, 1
 760:	01 00 00 00 	add	r0, r0, 1
 764:	01 00 00 00 	add	r0, r0, 1
 768:	04 00 00 00 	add	r0, r0, 4
 76c:	05 00 00 00 	add	r0, r0, 5
 770:	01 00 00 00 	add	r0, r0, 1
 774:	01 00 00 00 	add	r0, r0, 1
 778:	06 00 00 00 	add	r0, r0, 6
 77c:	00 00 00 00 	add	r0, r0, 0

00000780 <_FB_SIZE>:
 780:	00 2c 01 00 	add	r0, r0, r1
	...

00000790 <_FB_HEIGHT>:
 790:	f0 00 00 00 	add	r0, r0, 240
	...

000007a0 <_FB_WIDTH>:
 7a0:	40 01 00 00 	add	r0, r0, 320
	...

000007b0 <_snowhousecpu_regno_to_class>:
 7b0:	01 00 00 00 	add	r0, r0, 1
 7b4:	01 00 00 00 	add	r0, r0, 1
 7b8:	01 00 00 00 	add	r0, r0, 1
 7bc:	01 00 00 00 	add	r0, r0, 1
 7c0:	01 00 00 00 	add	r0, r0, 1
 7c4:	01 00 00 00 	add	r0, r0, 1
 7c8:	01 00 00 00 	add	r0, r0, 1
 7cc:	02 00 00 00 	add	r0, r0, 2
 7d0:	01 00 00 00 	add	r0, r0, 1
 7d4:	01 00 00 00 	add	r0, r0, 1
 7d8:	01 00 00 00 	add	r0, r0, 1
 7dc:	01 00 00 00 	add	r0, r0, 1
 7e0:	01 00 00 00 	add	r0, r0, 1
 7e4:	01 00 00 00 	add	r0, r0, 1
 7e8:	04 00 00 00 	add	r0, r0, 4
 7ec:	05 00 00 00 	add	r0, r0, 5
 7f0:	01 00 00 00 	add	r0, r0, 1
 7f4:	01 00 00 00 	add	r0, r0, 1
 7f8:	06 00 00 00 	add	r0, r0, 6

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

