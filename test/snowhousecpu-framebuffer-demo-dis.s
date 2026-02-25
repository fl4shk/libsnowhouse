
snowhousecpu-framebuffer-demo.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_my_text_start>:
	...
  10:	01 00 00 01 	add	r1, r0, 1
  14:	04 00 00 02 	add	r2, r0, 4
  18:	02 00 00 03 	add	r3, r0, 2
  1c:	05 00 23 81 	udivw	r1, r2, r3
  20:	ff ff 00 01 	add	r1, r0, -1
  24:	fe ff 00 03 	add	r3, r0, -2
  28:	06 00 23 81 	sdivw	r1, r2, r3
  2c:	00 00 00 01 	add	r1, r0, 0
  30:	00 00 00 02 	add	r2, r0, 0
  34:	00 00 00 03 	add	r3, r0, 0
  38:	00 00 00 04 	add	r4, r0, 0
  3c:	00 00 00 05 	add	r5, r0, 0
  40:	00 00 00 06 	add	r6, r0, 0
  44:	00 00 00 07 	add	r7, r0, 0
  48:	00 00 00 08 	add	r8, r0, 0
  4c:	00 00 00 09 	add	r9, r0, 0
  50:	00 00 00 0a 	add	r10, r0, 0
  54:	00 00 00 0b 	add	r11, r0, 0
  58:	00 00 00 0c 	add	r12, r0, 0
  5c:	00 00 00 0d 	add	lr, r0, 0
  60:	00 00 00 0e 	add	fp, r0, 0
  64:	00 00 00 0f 	add	sp, r0, 0
  68:	75 00 00 b0 	bl	r0, 468
  6c:	00 00 00 00 	add	r0, r0, 0

00000070 <_deregister_tm_clones>:
  70:	fc ff f0 0f 	add	sp, sp, -4
  74:	00 00 f1 9d 	str	lr, sp, 0
  78:	c0 04 00 02 	add	r2, r0, 1216
  7c:	c0 04 00 01 	add	r1, r0, 1216
  80:	05 00 10 a2 	beq	r2, r1, 20
  84:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
  88:	00 00 00 02 
  8c:	00 00 00 03 	add	r3, r0, 0
  90:	01 00 30 a2 	beq	r2, r3, 4
  94:	00 00 26 ad 	jl	lr, r2

00000098 <.L1>:
  98:	00 00 f0 9d 	ldr	lr, sp, 0
  9c:	04 00 f0 0f 	add	sp, sp, 4
  a0:	00 00 d6 a0 	jl	r0, lr
	...

000000b0 <_register_tm_clones>:
  b0:	fc ff f0 0f 	add	sp, sp, -4
  b4:	00 00 f1 9d 	str	lr, sp, 0
  b8:	c0 04 00 01 	add	r1, r0, 1216
  bc:	c0 04 10 01 	add	r1, r1, 1216
  c0:	25 00 10 71 	asr	r1, r1, 2
  c4:	f3 01 10 72 	lsr	r2, r1, 31
  c8:	00 00 21 02 	add	r2, r2, r1
  cc:	15 00 20 72 	asr	r2, r2, 1
  d0:	00 00 00 01 	add	r1, r0, 0
  d4:	05 00 10 a2 	beq	r2, r1, 20
  d8:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  dc:	00 00 00 03 
  e0:	02 00 10 a3 	beq	r3, r1, 8
  e4:	c0 04 00 01 	add	r1, r0, 1216
  e8:	00 00 36 ad 	jl	lr, r3

000000ec <.L5>:
  ec:	00 00 f0 9d 	ldr	lr, sp, 0
  f0:	04 00 f0 0f 	add	sp, sp, 4
  f4:	00 00 d6 a0 	jl	r0, lr
  f8:	00 00 00 00 	add	r0, r0, 0

000000fc <___do_global_dtors_aux>:
  fc:	f8 ff f0 0f 	add	sp, sp, -8
 100:	00 00 f1 97 	str	r7, sp, 0
 104:	04 00 f1 9d 	str	lr, sp, 4
 108:	c0 04 00 07 	add	r7, r0, 1216
 10c:	00 00 74 92 	ldub	r2, r7, 0
 110:	00 00 00 01 	add	r1, r0, 0
 114:	03 00 11 a2 	bne	r2, r1, 12
 118:	d5 ff ff bd 	bl	lr, -172
 11c:	01 00 00 01 	add	r1, r0, 1
 120:	00 00 77 91 	stb	r1, r7, 0

00000124 <.L9>:
 124:	00 00 f0 97 	ldr	r7, sp, 0
 128:	04 00 f0 9d 	ldr	lr, sp, 4
 12c:	08 00 f0 0f 	add	sp, sp, 8
 130:	00 00 d6 a0 	jl	r0, lr
	...

00000140 <_call___do_global_dtors_aux>:
 140:	fc ff f0 0f 	add	sp, sp, -4
 144:	00 00 f1 9d 	str	lr, sp, 0
 148:	00 00 f0 9d 	ldr	lr, sp, 0
 14c:	04 00 f0 0f 	add	sp, sp, 4
 150:	00 00 d6 a0 	jl	r0, lr
	...

00000160 <_frame_dummy>:
 160:	fc ff f0 0f 	add	sp, sp, -4
 164:	00 00 f1 9d 	str	lr, sp, 0
 168:	d1 ff ff bd 	bl	lr, -188
 16c:	00 00 f0 9d 	ldr	lr, sp, 0
 170:	04 00 f0 0f 	add	sp, sp, 4
 174:	00 00 d6 a0 	jl	r0, lr
 178:	00 00 00 00 	add	r0, r0, 0

0000017c <_call_frame_dummy>:
 17c:	fc ff f0 0f 	add	sp, sp, -4
 180:	00 00 f1 9d 	str	lr, sp, 0
 184:	00 00 f0 9d 	ldr	lr, sp, 0
 188:	04 00 f0 0f 	add	sp, sp, 4
 18c:	00 00 d6 a0 	jl	r0, lr

00000190 <_memcpy>:
 190:	13 00 00 a3 	beq	r3, r0, 76
 194:	00 00 10 04 	add	r4, r1, 0
 198:	00 00 20 05 	add	r5, r2, 0
 19c:	fc ff 30 66 	and	r6, r3, -4
 1a0:	00 00 16 06 	add	r6, r1, r6
 1a4:	00 00 13 03 	add	r3, r1, r3
 1a8:	00 00 12 52 	or	r2, r1, r2
 1ac:	03 00 20 62 	and	r2, r2, 3
 1b0:	06 00 01 a2 	bne	r2, r0, 24

000001b4 <.Lmemcpy_words_loop>:
 1b4:	00 00 50 92 	ldr	r2, r5, 0
 1b8:	00 00 41 92 	str	r2, r4, 0
 1bc:	04 00 40 04 	add	r4, r4, 4
 1c0:	04 00 50 05 	add	r5, r5, 4
 1c4:	fb ff 61 a4 	bne	r4, r6, -20
 1c8:	05 00 30 a6 	beq	r6, r3, 20

000001cc <.Lmemcpy_remaining_bytes_loop>:
 1cc:	00 00 54 92 	ldub	r2, r5, 0
 1d0:	00 00 47 92 	stb	r2, r4, 0
 1d4:	01 00 40 04 	add	r4, r4, 1
 1d8:	01 00 50 05 	add	r5, r5, 1
 1dc:	fb ff 31 a4 	bne	r4, r3, -20

000001e0 <.Lmemcpy_end>:
 1e0:	00 00 d6 a0 	jl	r0, lr

000001e4 <_memset>:
 1e4:	12 00 00 a3 	beq	r3, r0, 72
 1e8:	00 00 10 04 	add	r4, r1, 0
 1ec:	ff 00 20 62 	and	r2, r2, 255
 1f0:	01 01 00 c0 	add	r5, r0, 16843009 // pre #0x101
 1f4:	01 01 00 05 
 1f8:	00 00 52 85 	mul	r5, r5, r2
 1fc:	fc ff 30 66 	and	r6, r3, -4
 200:	00 00 16 06 	add	r6, r1, r6
 204:	00 00 13 03 	add	r3, r1, r3
 208:	03 00 10 62 	and	r2, r1, 3
 20c:	05 00 01 a2 	bne	r2, r0, 20

00000210 <.Lmemset_words_loop>:
 210:	00 00 41 95 	str	r5, r4, 0
 214:	04 00 40 04 	add	r4, r4, 4
 218:	04 00 50 05 	add	r5, r5, 4
 21c:	fc ff 61 a4 	bne	r4, r6, -16
 220:	03 00 30 a6 	beq	r6, r3, 12

00000224 <.Lmemset_remaining_bytes_loop>:
 224:	00 00 47 95 	stb	r5, r4, 0
 228:	01 00 40 04 	add	r4, r4, 1
 22c:	fd ff 31 a4 	bne	r4, r3, -12

00000230 <.Lmemset_end>:
 230:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .init:

00000240 <_start>:
	...
 248:	01 00 00 c0 	add	sp, r0, 98304 // pre #0x1
 24c:	00 80 00 0f 
 250:	02 00 00 a0 	beq	r0, r0, 8
	...

0000025c <__cstart>:
 25c:	fc ff f0 0f 	add	sp, sp, -4
 260:	00 00 f1 9d 	str	lr, sp, 0
 264:	b4 04 00 03 	add	r3, r0, 1204
 268:	90 04 00 02 	add	r2, r0, 1168
 26c:	90 04 00 01 	add	r1, r0, 1168
 270:	c7 ff ff bd 	bl	lr, -228
 274:	c1 04 00 03 	add	r3, r0, 1217
 278:	00 00 00 02 	add	r2, r0, 0
 27c:	b4 04 00 01 	add	r1, r0, 1204
 280:	d8 ff ff bd 	bl	lr, -160
 284:	22 00 00 bd 	bl	lr, 136
 288:	00 00 00 02 	add	r2, r0, 0
 28c:	00 00 20 01 	add	r1, r2, 0
 290:	07 00 00 bd 	bl	lr, 28

00000294 <.L2>:
 294:	ff ff ff b0 	bl	r0, -4

00000298 <_init>:
 298:	b1 ff ff bd 	bl	lr, -316
 29c:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

000002a0 <_fini>:
 2a0:	96 ff ff bd 	bl	lr, -424
 2a4:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

000002b0 <_main>:
 2b0:	f8 ff f0 0f 	add	sp, sp, -8
 2b4:	00 00 f1 97 	str	r7, sp, 0
 2b8:	04 00 f1 9d 	str	lr, sp, 4
 2bc:	b0 04 00 01 	add	r1, r0, 1200
 2c0:	00 00 10 9d 	ldr	lr, r1, 0
 2c4:	00 00 00 06 	add	r6, r0, 0
 2c8:	40 01 00 05 	add	r5, r0, 320
 2cc:	f0 00 00 07 	add	r7, r0, 240

000002d0 <.L2>:
 2d0:	33 00 60 74 	lsr	r4, r6, 3
 2d4:	51 00 40 74 	lsl	r4, r4, 5
 2d8:	00 00 d0 03 	add	r3, lr, 0
 2dc:	00 00 00 02 	add	r2, r0, 0

000002e0 <.L3>:
 2e0:	33 00 20 71 	lsr	r1, r2, 3
 2e4:	1f 00 10 61 	and	r1, r1, 31
 2e8:	00 00 14 51 	or	r1, r1, r4
 2ec:	00 00 36 91 	sth	r1, r3, 0
 2f0:	01 00 20 02 	add	r2, r2, 1
 2f4:	02 00 30 03 	add	r3, r3, 2
 2f8:	f9 ff 51 a2 	bne	r2, r5, -28
 2fc:	01 00 60 06 	add	r6, r6, 1
 300:	80 02 d0 0d 	add	lr, lr, 640
 304:	f2 ff 71 a6 	bne	r6, r7, -56

00000308 <.L4>:
 308:	ff ff ff b0 	bl	r0, -4

Disassembly of section .text.__libc_init_array:

00000310 <___libc_init_array>:
 310:	f0 ff f0 0f 	add	sp, sp, -16
 314:	00 00 f1 97 	str	r7, sp, 0
 318:	04 00 f1 98 	str	r8, sp, 4
 31c:	08 00 f1 99 	str	r9, sp, 8
 320:	0c 00 f1 9d 	str	lr, sp, 12
 324:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 328:	00 00 00 09 
 32c:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 330:	00 00 00 07 
 334:	08 00 70 a9 	beq	r9, r7, 32
 338:	00 00 97 19 	sub	r9, r9, r7
 33c:	25 00 90 79 	asr	r9, r9, 2
 340:	00 00 00 08 	add	r8, r0, 0

00000344 <.L3>:
 344:	00 00 70 91 	ldr	r1, r7, 0
 348:	00 00 16 ad 	jl	lr, r1
 34c:	01 00 80 08 	add	r8, r8, 1
 350:	04 00 70 07 	add	r7, r7, 4
 354:	fb ff 92 a8 	bltu	r8, r9, -20

00000358 <.L2>:
 358:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 35c:	00 00 00 02 
 360:	00 00 00 01 	add	r1, r0, 0
 364:	02 00 10 a2 	beq	r2, r1, 8
 368:	ff ff 00 c0 	bl	lr, -880 // pre #0xffff
 36c:	24 ff ff bd 

00000370 <.L4>:
 370:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 374:	00 00 00 09 
 378:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 37c:	00 00 00 07 
 380:	08 00 70 a9 	beq	r9, r7, 32
 384:	00 00 97 19 	sub	r9, r9, r7
 388:	25 00 90 79 	asr	r9, r9, 2
 38c:	00 00 00 08 	add	r8, r0, 0

00000390 <.L6>:
 390:	00 00 70 91 	ldr	r1, r7, 0
 394:	00 00 16 ad 	jl	lr, r1
 398:	01 00 80 08 	add	r8, r8, 1
 39c:	04 00 70 07 	add	r7, r7, 4
 3a0:	fb ff 92 a8 	bltu	r8, r9, -20

000003a4 <.L1>:
 3a4:	00 00 f0 97 	ldr	r7, sp, 0
 3a8:	04 00 f0 98 	ldr	r8, sp, 4
 3ac:	08 00 f0 99 	ldr	r9, sp, 8
 3b0:	0c 00 f0 9d 	ldr	lr, sp, 12
 3b4:	10 00 f0 0f 	add	sp, sp, 16
 3b8:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .rodata:

000003c0 <_snowhousecpu_regno_to_class>:
 3c0:	01 00 00 00 	add	r0, r0, 1
 3c4:	01 00 00 00 	add	r0, r0, 1
 3c8:	01 00 00 00 	add	r0, r0, 1
 3cc:	01 00 00 00 	add	r0, r0, 1
 3d0:	01 00 00 00 	add	r0, r0, 1
 3d4:	01 00 00 00 	add	r0, r0, 1
 3d8:	01 00 00 00 	add	r0, r0, 1
 3dc:	01 00 00 00 	add	r0, r0, 1
 3e0:	01 00 00 00 	add	r0, r0, 1
 3e4:	01 00 00 00 	add	r0, r0, 1
 3e8:	01 00 00 00 	add	r0, r0, 1
 3ec:	01 00 00 00 	add	r0, r0, 1
 3f0:	01 00 00 00 	add	r0, r0, 1
 3f4:	02 00 00 00 	add	r0, r0, 2
 3f8:	03 00 00 00 	add	r0, r0, 3
 3fc:	01 00 00 00 	add	r0, r0, 1
 400:	01 00 00 00 	add	r0, r0, 1
 404:	04 00 00 00 	add	r0, r0, 4
	...

00000410 <_FB_SIZE>:
 410:	00 2c 01 00 	add	r0, r0, r1
	...

00000420 <_FB_HEIGHT>:
 420:	f0 00 00 00 	add	r0, r0, 240
	...

00000430 <_FB_WIDTH>:
 430:	40 01 00 00 	add	r0, r0, 320
	...

00000440 <_snowhousecpu_regno_to_class>:
 440:	01 00 00 00 	add	r0, r0, 1
 444:	01 00 00 00 	add	r0, r0, 1
 448:	01 00 00 00 	add	r0, r0, 1
 44c:	01 00 00 00 	add	r0, r0, 1
 450:	01 00 00 00 	add	r0, r0, 1
 454:	01 00 00 00 	add	r0, r0, 1
 458:	01 00 00 00 	add	r0, r0, 1
 45c:	01 00 00 00 	add	r0, r0, 1
 460:	01 00 00 00 	add	r0, r0, 1
 464:	01 00 00 00 	add	r0, r0, 1
 468:	01 00 00 00 	add	r0, r0, 1
 46c:	01 00 00 00 	add	r0, r0, 1
 470:	01 00 00 00 	add	r0, r0, 1
 474:	02 00 00 00 	add	r0, r0, 2
 478:	03 00 00 00 	add	r0, r0, 3
 47c:	01 00 00 00 	add	r0, r0, 1
 480:	01 00 00 00 	add	r0, r0, 1
 484:	04 00 00 00 	add	r0, r0, 4

Disassembly of section .data:

00000490 <___dso_handle>:
	...

000004a0 <_to_keep_loop_going>:
 4a0:	04 00 00 00 	add	r0, r0, 4
	...

000004b0 <_fb>:
 4b0:	00 00 80 00 	add	r0, r8, 0

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

