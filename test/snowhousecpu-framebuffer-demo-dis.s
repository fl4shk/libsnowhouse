
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
  84:	a2 00 00 b0 	bl	r0, 648 /* dst_pc=0x310 */
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
 108:	a0 04 00 02 	add	r2, r0, 1184
 10c:	a0 04 00 01 	add	r1, r0, 1184
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
 148:	a0 04 00 02 	add	r2, r0, 1184
 14c:	ff ff 00 c0 	add	r2, r2, -1184 /* pre #0xffff */
 150:	60 fb 20 02 
 154:	25 00 20 71 	asr	r1, r2, 2
 158:	f3 01 20 72 	lsr	r2, r2, 31
 15c:	00 00 21 02 	add	r2, r2, r1
 160:	15 00 20 72 	asr	r2, r2, 1
 164:	00 00 00 01 	add	r1, r0, 0
 168:	05 00 10 a2 	beq	r2, r1, 20 /* dst_pc=0x180 */
 16c:	00 00 00 c0 	add	r3, r0, 0 /* pre #0x0 */
 170:	00 00 00 03 
 174:	02 00 10 a3 	beq	r3, r1, 8 /* dst_pc=0x180 */
 178:	a0 04 00 01 	add	r1, r0, 1184
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
 19c:	78 05 00 07 	add	r7, r0, 1400
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

Disassembly of section .init:

00000310 <_start>:
	...
 318:	ff 00 00 c0 	add	sp, r0, 16777212 /* pre #0xff */
 31c:	fc ff 00 0f 
 320:	02 00 00 a0 	beq	r0, r0, 8 /* dst_pc=0x32c */
	...

0000032c <__cstart>:
 32c:	fc ff f0 0f 	add	sp, sp, -4
 330:	00 00 f1 9d 	str	lr, sp, 0
 334:	26 00 00 bd 	bl	lr, 152 /* dst_pc=0x3d0 */
 338:	00 00 00 02 	add	r2, r0, 0
 33c:	00 00 20 01 	add	r1, r2, 0
 340:	0b 00 00 bd 	bl	lr, 44 /* dst_pc=0x370 */
 344:	00 00 00 02 	add	r2, r0, 0
 348:	00 0c 00 c0 	str	r1, r2, 201326596 /* pre #0xc00 */
 34c:	04 00 21 91 

00000350 <.L2>:
 350:	ff ff ff b0 	bl	r0, -4 /* dst_pc=0x350 */

00000354 <_init>:
 354:	a7 ff ff bd 	bl	lr, -356 /* dst_pc=0x1f4 */
 358:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

0000035c <_fini>:
 35c:	8c ff ff bd 	bl	lr, -464 /* dst_pc=0x190 */
 360:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

00000370 <_main>:
 370:	f8 ff f0 0f 	add	sp, sp, -8
 374:	00 00 f1 97 	str	r7, sp, 0
 378:	04 00 f1 9d 	str	lr, sp, 4
 37c:	90 04 00 01 	add	r1, r0, 1168
 380:	00 00 10 9d 	ldr	lr, r1, 0
 384:	00 00 00 06 	add	r6, r0, 0
 388:	40 01 00 05 	add	r5, r0, 320
 38c:	f0 00 00 07 	add	r7, r0, 240

00000390 <.L23>:
 390:	33 00 60 74 	lsr	r4, r6, 3
 394:	51 00 40 74 	lsl	r4, r4, 5
 398:	00 00 d0 03 	add	r3, lr, 0
 39c:	00 00 00 02 	add	r2, r0, 0

000003a0 <.L24>:
 3a0:	33 00 20 71 	lsr	r1, r2, 3
 3a4:	1f 00 10 61 	and	r1, r1, 31
 3a8:	00 00 14 51 	or	r1, r1, r4
 3ac:	00 00 36 91 	sth	r1, r3, 0
 3b0:	01 00 20 02 	add	r2, r2, 1
 3b4:	02 00 30 03 	add	r3, r3, 2
 3b8:	f9 ff 51 a2 	bne	r2, r5, -28 /* dst_pc=0x3a0 */
 3bc:	01 00 60 06 	add	r6, r6, 1
 3c0:	80 02 d0 0d 	add	lr, lr, 640
 3c4:	f2 ff 71 a6 	bne	r6, r7, -56 /* dst_pc=0x390 */

000003c8 <.L25>:
 3c8:	ff ff ff b0 	bl	r0, -4 /* dst_pc=0x3c8 */

Disassembly of section .text.__libc_init_array:

000003d0 <___libc_init_array>:
 3d0:	f0 ff f0 0f 	add	sp, sp, -16
 3d4:	00 00 f1 97 	str	r7, sp, 0
 3d8:	04 00 f1 98 	str	r8, sp, 4
 3dc:	08 00 f1 99 	str	r9, sp, 8
 3e0:	0c 00 f1 9d 	str	lr, sp, 12
 3e4:	00 00 00 c0 	add	r9, r0, 0 /* pre #0x0 */
 3e8:	00 00 00 09 
 3ec:	00 00 00 c0 	add	r7, r0, 0 /* pre #0x0 */
 3f0:	00 00 00 07 
 3f4:	08 00 70 a9 	beq	r9, r7, 32 /* dst_pc=0x418 */
 3f8:	00 00 97 19 	sub	r9, r9, r7
 3fc:	25 00 90 79 	asr	r9, r9, 2
 400:	00 00 00 08 	add	r8, r0, 0

00000404 <.L3>:
 404:	00 00 70 91 	ldr	r1, r7, 0
 408:	00 00 16 ad 	jl	lr, r1
 40c:	01 00 80 08 	add	r8, r8, 1
 410:	04 00 70 07 	add	r7, r7, 4
 414:	fb ff 92 a8 	bltu	r8, r9, -20 /* dst_pc=0x404 */

00000418 <.L2>:
 418:	00 00 00 c0 	add	r2, r0, 0 /* pre #0x0 */
 41c:	00 00 00 02 
 420:	00 00 00 01 	add	r1, r0, 0
 424:	02 00 10 a2 	beq	r2, r1, 8 /* dst_pc=0x430 */
 428:	ff ff 00 c0 	bl	lr, -1072 /* dst_pc=0xfffffffc */ /* pre #0xffff */
 42c:	f4 fe ff bd 

00000430 <.L4>:
 430:	00 00 00 c0 	add	r9, r0, 0 /* pre #0x0 */
 434:	00 00 00 09 
 438:	00 00 00 c0 	add	r7, r0, 0 /* pre #0x0 */
 43c:	00 00 00 07 
 440:	08 00 70 a9 	beq	r9, r7, 32 /* dst_pc=0x464 */
 444:	00 00 97 19 	sub	r9, r9, r7
 448:	25 00 90 79 	asr	r9, r9, 2
 44c:	00 00 00 08 	add	r8, r0, 0

00000450 <.L6>:
 450:	00 00 70 91 	ldr	r1, r7, 0
 454:	00 00 16 ad 	jl	lr, r1
 458:	01 00 80 08 	add	r8, r8, 1
 45c:	04 00 70 07 	add	r7, r7, 4
 460:	fb ff 92 a8 	bltu	r8, r9, -20 /* dst_pc=0x450 */

00000464 <.L1>:
 464:	00 00 f0 97 	ldr	r7, sp, 0
 468:	04 00 f0 98 	ldr	r8, sp, 4
 46c:	08 00 f0 99 	ldr	r9, sp, 8
 470:	0c 00 f0 9d 	ldr	lr, sp, 12
 474:	10 00 f0 0f 	add	sp, sp, 16
 478:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .data:

00000480 <___dso_handle>:
	...

00000490 <_fb>:
 490:	00 00 00 04 	add	r4, r0, 0

Disassembly of section .rodata:

000004a0 <_snowhousecpu_regno_to_class>:
 4a0:	01 00 00 00 	add	r0, r0, 1
 4a4:	01 00 00 00 	add	r0, r0, 1
 4a8:	01 00 00 00 	add	r0, r0, 1
 4ac:	01 00 00 00 	add	r0, r0, 1
 4b0:	01 00 00 00 	add	r0, r0, 1
 4b4:	01 00 00 00 	add	r0, r0, 1
 4b8:	01 00 00 00 	add	r0, r0, 1
 4bc:	02 00 00 00 	add	r0, r0, 2
 4c0:	01 00 00 00 	add	r0, r0, 1
 4c4:	01 00 00 00 	add	r0, r0, 1
 4c8:	01 00 00 00 	add	r0, r0, 1
 4cc:	01 00 00 00 	add	r0, r0, 1
 4d0:	01 00 00 00 	add	r0, r0, 1
 4d4:	01 00 00 00 	add	r0, r0, 1
 4d8:	04 00 00 00 	add	r0, r0, 4
 4dc:	05 00 00 00 	add	r0, r0, 5
 4e0:	01 00 00 00 	add	r0, r0, 1
 4e4:	01 00 00 00 	add	r0, r0, 1
 4e8:	06 00 00 00 	add	r0, r0, 6
 4ec:	00 00 00 00 	add	r0, r0, 0

000004f0 <_FB_SIZE>:
 4f0:	00 2c 01 00 	add	r0, r0, r1
	...

00000500 <_FB_HEIGHT>:
 500:	f0 00 00 00 	add	r0, r0, 240
	...

00000510 <_FB_WIDTH>:
 510:	40 01 00 00 	add	r0, r0, 320
	...

00000520 <_snowhousecpu_regno_to_class>:
 520:	01 00 00 00 	add	r0, r0, 1
 524:	01 00 00 00 	add	r0, r0, 1
 528:	01 00 00 00 	add	r0, r0, 1
 52c:	01 00 00 00 	add	r0, r0, 1
 530:	01 00 00 00 	add	r0, r0, 1
 534:	01 00 00 00 	add	r0, r0, 1
 538:	01 00 00 00 	add	r0, r0, 1
 53c:	02 00 00 00 	add	r0, r0, 2
 540:	01 00 00 00 	add	r0, r0, 1
 544:	01 00 00 00 	add	r0, r0, 1
 548:	01 00 00 00 	add	r0, r0, 1
 54c:	01 00 00 00 	add	r0, r0, 1
 550:	01 00 00 00 	add	r0, r0, 1
 554:	01 00 00 00 	add	r0, r0, 1
 558:	04 00 00 00 	add	r0, r0, 4
 55c:	05 00 00 00 	add	r0, r0, 5
 560:	01 00 00 00 	add	r0, r0, 1
 564:	01 00 00 00 	add	r0, r0, 1
 568:	06 00 00 00 	add	r0, r0, 6

Disassembly of section .comment:

00000000 <_stack-0xfffffc>:
   0:	47 43 43 3a 	bad
   4:	20 28 47 4e 	xor	fp, r4, r7
   8:	55 29 20 31 	sltu	r1, r2, 10581
   c:	37 2e 30 2e 	bad
  10:	30 20 32 30 	bad
  14:	32 36 30 35 	sltu	r5, r3, 13874
  18:	30 35 20 28 	sltu	r8, r2, r0
  1c:	65 78 70 65 	and	r5, r7, 30821
  20:	72 69 6d 65 	and	r5, r6, 26994
  24:	6e 74 61 6c 	and	r12, r6, 29806
  28:	Address 0x28 is out of bounds.

