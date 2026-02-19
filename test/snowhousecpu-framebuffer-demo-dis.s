
snowhousecpu-framebuffer-demo.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_my_text_start>:
	...
  10:	77 00 00 b0 	bl	r0, 476
	...

00000020 <_deregister_tm_clones>:
  20:	fc ff f0 0f 	add	sp, sp, -4
  24:	00 00 f1 9d 	str	lr, sp, 0
  28:	70 04 00 02 	add	r2, r0, 1136
  2c:	70 04 00 01 	add	r1, r0, 1136
  30:	05 00 10 a2 	beq	r2, r1, 20
  34:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
  38:	00 00 00 02 
  3c:	00 00 00 03 	add	r3, r0, 0
  40:	01 00 30 a2 	beq	r2, r3, 4
  44:	00 00 26 ad 	jl	lr, r2

00000048 <.L1>:
  48:	00 00 f0 9d 	ldr	lr, sp, 0
  4c:	04 00 f0 0f 	add	sp, sp, 4
  50:	00 00 d6 a0 	jl	r0, lr
	...

00000060 <_register_tm_clones>:
  60:	fc ff f0 0f 	add	sp, sp, -4
  64:	00 00 f1 9d 	str	lr, sp, 0
  68:	70 04 00 01 	add	r1, r0, 1136
  6c:	70 04 10 01 	add	r1, r1, 1136
  70:	25 00 10 71 	asr	r1, r1, 2
  74:	f3 01 10 72 	lsr	r2, r1, 31
  78:	00 00 21 02 	add	r2, r2, r1
  7c:	15 00 20 72 	asr	r2, r2, 1
  80:	00 00 00 01 	add	r1, r0, 0
  84:	05 00 10 a2 	beq	r2, r1, 20
  88:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  8c:	00 00 00 03 
  90:	02 00 10 a3 	beq	r3, r1, 8
  94:	70 04 00 01 	add	r1, r0, 1136
  98:	00 00 36 ad 	jl	lr, r3

0000009c <.L5>:
  9c:	00 00 f0 9d 	ldr	lr, sp, 0
  a0:	04 00 f0 0f 	add	sp, sp, 4
  a4:	00 00 d6 a0 	jl	r0, lr
  a8:	00 00 00 00 	add	r0, r0, 0

000000ac <___do_global_dtors_aux>:
  ac:	f8 ff f0 0f 	add	sp, sp, -8
  b0:	00 00 f1 97 	str	r7, sp, 0
  b4:	04 00 f1 9d 	str	lr, sp, 4
  b8:	70 04 00 07 	add	r7, r0, 1136
  bc:	00 00 74 92 	ldub	r2, r7, 0
  c0:	00 00 00 01 	add	r1, r0, 0
  c4:	03 00 11 a2 	bne	r2, r1, 12
  c8:	d5 ff ff bd 	bl	lr, -172
  cc:	01 00 00 01 	add	r1, r0, 1
  d0:	00 00 77 91 	stb	r1, r7, 0

000000d4 <.L9>:
  d4:	00 00 f0 97 	ldr	r7, sp, 0
  d8:	04 00 f0 9d 	ldr	lr, sp, 4
  dc:	08 00 f0 0f 	add	sp, sp, 8
  e0:	00 00 d6 a0 	jl	r0, lr
	...

000000f0 <_call___do_global_dtors_aux>:
  f0:	fc ff f0 0f 	add	sp, sp, -4
  f4:	00 00 f1 9d 	str	lr, sp, 0
  f8:	00 00 f0 9d 	ldr	lr, sp, 0
  fc:	04 00 f0 0f 	add	sp, sp, 4
 100:	00 00 d6 a0 	jl	r0, lr
	...

00000110 <_frame_dummy>:
 110:	fc ff f0 0f 	add	sp, sp, -4
 114:	00 00 f1 9d 	str	lr, sp, 0
 118:	d1 ff ff bd 	bl	lr, -188
 11c:	00 00 f0 9d 	ldr	lr, sp, 0
 120:	04 00 f0 0f 	add	sp, sp, 4
 124:	00 00 d6 a0 	jl	r0, lr
 128:	00 00 00 00 	add	r0, r0, 0

0000012c <_call_frame_dummy>:
 12c:	fc ff f0 0f 	add	sp, sp, -4
 130:	00 00 f1 9d 	str	lr, sp, 0
 134:	00 00 f0 9d 	ldr	lr, sp, 0
 138:	04 00 f0 0f 	add	sp, sp, 4
 13c:	00 00 d6 a0 	jl	r0, lr

00000140 <_memcpy>:
 140:	13 00 00 a3 	beq	r3, r0, 76
 144:	00 00 10 04 	add	r4, r1, 0
 148:	00 00 20 05 	add	r5, r2, 0
 14c:	fc ff 30 66 	and	r6, r3, -4
 150:	00 00 16 06 	add	r6, r1, r6
 154:	00 00 13 03 	add	r3, r1, r3
 158:	00 00 12 52 	or	r2, r1, r2
 15c:	03 00 20 62 	and	r2, r2, 3
 160:	06 00 01 a2 	bne	r2, r0, 24

00000164 <.Lmemcpy_words_loop>:
 164:	00 00 50 92 	ldr	r2, r5, 0
 168:	00 00 41 92 	str	r2, r4, 0
 16c:	04 00 40 04 	add	r4, r4, 4
 170:	04 00 50 05 	add	r5, r5, 4
 174:	fb ff 61 a4 	bne	r4, r6, -20
 178:	05 00 30 a6 	beq	r6, r3, 20

0000017c <.Lmemcpy_remaining_bytes_loop>:
 17c:	00 00 54 92 	ldub	r2, r5, 0
 180:	00 00 47 92 	stb	r2, r4, 0
 184:	01 00 40 04 	add	r4, r4, 1
 188:	01 00 50 05 	add	r5, r5, 1
 18c:	fb ff 31 a4 	bne	r4, r3, -20

00000190 <.Lmemcpy_end>:
 190:	00 00 d6 a0 	jl	r0, lr

00000194 <_memset>:
 194:	12 00 00 a3 	beq	r3, r0, 72
 198:	00 00 10 04 	add	r4, r1, 0
 19c:	ff 00 20 62 	and	r2, r2, 255
 1a0:	01 01 00 c0 	add	r5, r0, 16843009 // pre #0x101
 1a4:	01 01 00 05 
 1a8:	00 00 52 85 	mul	r5, r5, r2
 1ac:	fc ff 30 66 	and	r6, r3, -4
 1b0:	00 00 16 06 	add	r6, r1, r6
 1b4:	00 00 13 03 	add	r3, r1, r3
 1b8:	03 00 10 62 	and	r2, r1, 3
 1bc:	05 00 01 a2 	bne	r2, r0, 20

000001c0 <.Lmemset_words_loop>:
 1c0:	00 00 41 95 	str	r5, r4, 0
 1c4:	04 00 40 04 	add	r4, r4, 4
 1c8:	04 00 50 05 	add	r5, r5, 4
 1cc:	fc ff 61 a4 	bne	r4, r6, -16
 1d0:	03 00 30 a6 	beq	r6, r3, 12

000001d4 <.Lmemset_remaining_bytes_loop>:
 1d4:	00 00 47 95 	stb	r5, r4, 0
 1d8:	01 00 40 04 	add	r4, r4, 1
 1dc:	fd ff 31 a4 	bne	r4, r3, -12

000001e0 <.Lmemset_end>:
 1e0:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .init:

000001f0 <_start>:
	...
 1f8:	00 10 00 0f 	add	sp, r0, 4096
 1fc:	02 00 00 a0 	beq	r0, r0, 8
	...

00000208 <__cstart>:
 208:	fc ff f0 0f 	add	sp, sp, -4
 20c:	00 00 f1 9d 	str	lr, sp, 0
 210:	64 04 00 03 	add	r3, r0, 1124
 214:	40 04 00 02 	add	r2, r0, 1088
 218:	40 04 00 01 	add	r1, r0, 1088
 21c:	c8 ff ff bd 	bl	lr, -224
 220:	71 04 00 03 	add	r3, r0, 1137
 224:	00 00 00 02 	add	r2, r0, 0
 228:	64 04 00 01 	add	r1, r0, 1124
 22c:	d9 ff ff bd 	bl	lr, -156
 230:	23 00 00 bd 	bl	lr, 140
 234:	00 00 00 02 	add	r2, r0, 0
 238:	00 00 20 01 	add	r1, r2, 0
 23c:	08 00 00 bd 	bl	lr, 32

00000240 <.L2>:
 240:	ff ff ff b0 	bl	r0, -4

00000244 <_init>:
 244:	b2 ff ff bd 	bl	lr, -312
 248:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

0000024c <_fini>:
 24c:	97 ff ff bd 	bl	lr, -420
 250:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

00000260 <_main>:
 260:	f8 ff f0 0f 	add	sp, sp, -8
 264:	00 00 f1 97 	str	r7, sp, 0
 268:	04 00 f1 9d 	str	lr, sp, 4
 26c:	60 04 00 01 	add	r1, r0, 1120
 270:	00 00 10 9d 	ldr	lr, r1, 0
 274:	00 00 00 06 	add	r6, r0, 0
 278:	40 01 00 05 	add	r5, r0, 320
 27c:	f0 00 00 07 	add	r7, r0, 240

00000280 <.L2>:
 280:	33 00 60 74 	lsr	r4, r6, 3
 284:	51 00 40 74 	lsl	r4, r4, 5
 288:	00 00 d0 03 	add	r3, lr, 0
 28c:	00 00 00 02 	add	r2, r0, 0

00000290 <.L3>:
 290:	33 00 20 71 	lsr	r1, r2, 3
 294:	1f 00 10 61 	and	r1, r1, 31
 298:	00 00 14 51 	or	r1, r1, r4
 29c:	00 00 36 91 	sth	r1, r3, 0
 2a0:	01 00 20 02 	add	r2, r2, 1
 2a4:	02 00 30 03 	add	r3, r3, 2
 2a8:	f9 ff 51 a2 	bne	r2, r5, -28
 2ac:	01 00 60 06 	add	r6, r6, 1
 2b0:	80 02 d0 0d 	add	lr, lr, 640
 2b4:	f2 ff 71 a6 	bne	r6, r7, -56

000002b8 <.L4>:
 2b8:	ff ff ff b0 	bl	r0, -4

Disassembly of section .text.__libc_init_array:

000002c0 <___libc_init_array>:
 2c0:	f0 ff f0 0f 	add	sp, sp, -16
 2c4:	00 00 f1 97 	str	r7, sp, 0
 2c8:	04 00 f1 98 	str	r8, sp, 4
 2cc:	08 00 f1 99 	str	r9, sp, 8
 2d0:	0c 00 f1 9d 	str	lr, sp, 12
 2d4:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 2d8:	00 00 00 09 
 2dc:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 2e0:	00 00 00 07 
 2e4:	08 00 70 a9 	beq	r9, r7, 32
 2e8:	00 00 97 19 	sub	r9, r9, r7
 2ec:	25 00 90 79 	asr	r9, r9, 2
 2f0:	00 00 00 08 	add	r8, r0, 0

000002f4 <.L3>:
 2f4:	00 00 70 91 	ldr	r1, r7, 0
 2f8:	00 00 16 ad 	jl	lr, r1
 2fc:	01 00 80 08 	add	r8, r8, 1
 300:	04 00 70 07 	add	r7, r7, 4
 304:	fb ff 92 a8 	bltu	r8, r9, -20

00000308 <.L2>:
 308:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 30c:	00 00 00 02 
 310:	00 00 00 01 	add	r1, r0, 0
 314:	02 00 10 a2 	beq	r2, r1, 8
 318:	ff ff 00 c0 	bl	lr, -800 // pre #0xffff
 31c:	38 ff ff bd 

00000320 <.L4>:
 320:	00 00 00 c0 	add	r9, r0, 0 // pre #0x0
 324:	00 00 00 09 
 328:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 32c:	00 00 00 07 
 330:	08 00 70 a9 	beq	r9, r7, 32
 334:	00 00 97 19 	sub	r9, r9, r7
 338:	25 00 90 79 	asr	r9, r9, 2
 33c:	00 00 00 08 	add	r8, r0, 0

00000340 <.L6>:
 340:	00 00 70 91 	ldr	r1, r7, 0
 344:	00 00 16 ad 	jl	lr, r1
 348:	01 00 80 08 	add	r8, r8, 1
 34c:	04 00 70 07 	add	r7, r7, 4
 350:	fb ff 92 a8 	bltu	r8, r9, -20

00000354 <.L1>:
 354:	00 00 f0 97 	ldr	r7, sp, 0
 358:	04 00 f0 98 	ldr	r8, sp, 4
 35c:	08 00 f0 99 	ldr	r9, sp, 8
 360:	0c 00 f0 9d 	ldr	lr, sp, 12
 364:	10 00 f0 0f 	add	sp, sp, 16
 368:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .rodata:

00000370 <_snowhousecpu_regno_to_class>:
 370:	01 00 00 00 	add	r0, r0, 1
 374:	01 00 00 00 	add	r0, r0, 1
 378:	01 00 00 00 	add	r0, r0, 1
 37c:	01 00 00 00 	add	r0, r0, 1
 380:	01 00 00 00 	add	r0, r0, 1
 384:	01 00 00 00 	add	r0, r0, 1
 388:	01 00 00 00 	add	r0, r0, 1
 38c:	01 00 00 00 	add	r0, r0, 1
 390:	01 00 00 00 	add	r0, r0, 1
 394:	01 00 00 00 	add	r0, r0, 1
 398:	01 00 00 00 	add	r0, r0, 1
 39c:	01 00 00 00 	add	r0, r0, 1
 3a0:	01 00 00 00 	add	r0, r0, 1
 3a4:	02 00 00 00 	add	r0, r0, 2
 3a8:	03 00 00 00 	add	r0, r0, 3
 3ac:	01 00 00 00 	add	r0, r0, 1
 3b0:	01 00 00 00 	add	r0, r0, 1
 3b4:	04 00 00 00 	add	r0, r0, 4
	...

000003c0 <_FB_SIZE>:
 3c0:	00 2c 01 00 	add	r0, r0, r1
	...

000003d0 <_FB_HEIGHT>:
 3d0:	f0 00 00 00 	add	r0, r0, 240
	...

000003e0 <_FB_WIDTH>:
 3e0:	40 01 00 00 	add	r0, r0, 320
	...

000003f0 <_snowhousecpu_regno_to_class>:
 3f0:	01 00 00 00 	add	r0, r0, 1
 3f4:	01 00 00 00 	add	r0, r0, 1
 3f8:	01 00 00 00 	add	r0, r0, 1
 3fc:	01 00 00 00 	add	r0, r0, 1
 400:	01 00 00 00 	add	r0, r0, 1
 404:	01 00 00 00 	add	r0, r0, 1
 408:	01 00 00 00 	add	r0, r0, 1
 40c:	01 00 00 00 	add	r0, r0, 1
 410:	01 00 00 00 	add	r0, r0, 1
 414:	01 00 00 00 	add	r0, r0, 1
 418:	01 00 00 00 	add	r0, r0, 1
 41c:	01 00 00 00 	add	r0, r0, 1
 420:	01 00 00 00 	add	r0, r0, 1
 424:	02 00 00 00 	add	r0, r0, 2
 428:	03 00 00 00 	add	r0, r0, 3
 42c:	01 00 00 00 	add	r0, r0, 1
 430:	01 00 00 00 	add	r0, r0, 1
 434:	04 00 00 00 	add	r0, r0, 4

Disassembly of section .data:

00000440 <___dso_handle>:
	...

00000450 <_to_keep_loop_going>:
 450:	04 00 00 00 	add	r0, r0, 4
	...

00000460 <_fb>:
 460:	00 00 80 00 	add	r0, r8, 0

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

