
snowhousecpu-framebuffer-demo.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_my_text_start>:
	...
  10:	9f 00 00 b0 	bl	r0, 636
	...

00000020 <_deregister_tm_clones>:
  20:	fc ff f0 0f 	add	sp, sp, -4
  24:	00 00 f1 9d 	str	lr, sp, 0
  28:	60 04 00 02 	add	r2, r0, 1120
  2c:	60 04 00 01 	add	r1, r0, 1120
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
  68:	60 04 00 01 	add	r1, r0, 1120
  6c:	60 04 10 01 	add	r1, r1, 1120
  70:	25 00 10 71 	asr	r1, r1, 2
  74:	f3 01 10 72 	lsr	r2, r1, 31
  78:	00 00 21 02 	add	r2, r2, r1
  7c:	15 00 20 72 	asr	r2, r2, 1
  80:	00 00 00 01 	add	r1, r0, 0
  84:	05 00 10 a2 	beq	r2, r1, 20
  88:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  8c:	00 00 00 03 
  90:	02 00 10 a3 	beq	r3, r1, 8
  94:	60 04 00 01 	add	r1, r0, 1120
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
  b8:	60 04 00 07 	add	r7, r0, 1120
  bc:	00 00 74 92 	ldub	r2, r7, 0
  c0:	00 00 00 01 	add	r1, r0, 0
  c4:	03 00 11 a2 	bne	r2, r1, 12
  c8:	d5 ff ff bd 	bl	lr, -172
  cc:	01 00 00 01 	add	r1, r0, 1
  d0:	00 00 77 91 	stb	r1, r7, 0

000000d4 <.L9>:
  d4:	00 00 f0 9d 	ldr	lr, sp, 0
  d8:	04 00 f0 97 	ldr	r7, sp, 4
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
 140:	15 00 00 a3 	beq	r3, r0, 84
 144:	00 00 10 04 	add	r4, r1, 0
 148:	fc ff 30 66 	and	r6, r3, -4
 14c:	00 00 16 06 	add	r6, r1, r6
 150:	00 00 13 03 	add	r3, r1, r3
 154:	0b 00 60 a3 	beq	r3, r6, 44

00000158 <.Lmemcpy_both_words_and_bytes_loop>:
 158:	00 00 20 95 	ldr	r5, r2, 0
 15c:	00 00 41 95 	str	r5, r4, 0
 160:	04 00 40 04 	add	r4, r4, 4
 164:	04 00 20 02 	add	r2, r2, 4
 168:	fb ff 61 a4 	bne	r4, r6, -20

0000016c <.Lmemcpy_remaining_bytes_loop>:
 16c:	00 00 24 95 	ldub	r5, r2, 0
 170:	00 00 47 95 	stb	r5, r4, 0
 174:	01 00 40 04 	add	r4, r4, 1
 178:	01 00 20 02 	add	r2, r2, 1
 17c:	fb ff 31 a4 	bne	r4, r3, -20
 180:	05 00 00 b0 	bl	r0, 20

00000184 <.Lmemcpy_only_words_loop>:
 184:	00 00 20 95 	ldr	r5, r2, 0
 188:	00 00 41 95 	str	r5, r4, 0
 18c:	04 00 40 04 	add	r4, r4, 4
 190:	04 00 20 02 	add	r2, r2, 4
 194:	fb ff 61 a4 	bne	r4, r6, -20

00000198 <.Lmemcpy_end>:
 198:	00 00 d6 a0 	jl	r0, lr

0000019c <_memset>:
 19c:	0f 00 00 a3 	beq	r3, r0, 60
 1a0:	00 00 10 04 	add	r4, r1, 0
 1a4:	fc ff 30 66 	and	r6, r3, -4
 1a8:	00 00 16 06 	add	r6, r1, r6
 1ac:	00 00 13 03 	add	r3, r1, r3
 1b0:	07 00 60 a3 	beq	r3, r6, 28

000001b4 <.Lmemset_both_words_and_bytes_loop>:
 1b4:	00 00 41 92 	str	r2, r4, 0
 1b8:	04 00 40 04 	add	r4, r4, 4
 1bc:	fd ff 61 a4 	bne	r4, r6, -12

000001c0 <.Lmemset_remaining_bytes_loop>:
 1c0:	00 00 47 92 	stb	r2, r4, 0
 1c4:	01 00 40 04 	add	r4, r4, 1
 1c8:	fd ff 31 a4 	bne	r4, r3, -12
 1cc:	03 00 00 b0 	bl	r0, 12

000001d0 <.Lmemset_only_words_loop>:
 1d0:	00 00 41 92 	str	r2, r4, 0
 1d4:	04 00 40 04 	add	r4, r4, 4
 1d8:	fd ff 61 a4 	bne	r4, r6, -12

000001dc <.Lmemset_end>:
 1dc:	00 00 d6 a0 	jl	r0, lr

000001e0 <___libc_init_array>:
 1e0:	f4 ff f0 0f 	add	sp, sp, -12
 1e4:	00 00 f1 97 	str	r7, sp, 0
 1e8:	04 00 f1 98 	str	r8, sp, 4
 1ec:	08 00 f1 9d 	str	lr, sp, 8
 1f0:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 1f4:	00 00 00 07 
 1f8:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 1fc:	00 00 70 07 
 200:	25 00 70 77 	asr	r7, r7, 2
 204:	00 00 00 08 	add	r8, r0, 0

00000208 <.L2>:
 208:	12 00 71 a8 	bne	r8, r7, 72
 20c:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 210:	00 00 00 02 
 214:	00 00 00 01 	add	r1, r0, 0
 218:	02 00 10 a2 	beq	r2, r1, 8
 21c:	ff ff 00 c0 	bl	lr, -548 // pre #0xffff
 220:	77 ff ff bd 

00000224 <.L4>:
 224:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 228:	00 00 00 07 
 22c:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 230:	00 00 70 07 
 234:	25 00 70 77 	asr	r7, r7, 2
 238:	00 00 00 08 	add	r8, r0, 0

0000023c <.L5>:
 23c:	0b 00 71 a8 	bne	r8, r7, 44
 240:	00 00 f0 9d 	ldr	lr, sp, 0
 244:	04 00 f0 98 	ldr	r8, sp, 4
 248:	08 00 f0 97 	ldr	r7, sp, 8
 24c:	0c 00 f0 0f 	add	sp, sp, 12
 250:	00 00 d6 a0 	jl	r0, lr

00000254 <.L3>:
 254:	21 00 80 71 	lsl	r1, r8, 2
 258:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 25c:	00 00 10 91 
 260:	00 00 16 ad 	jl	lr, r1
 264:	01 00 80 08 	add	r8, r8, 1
 268:	e7 ff 00 a0 	beq	r0, r0, -100

0000026c <.L6>:
 26c:	21 00 80 71 	lsl	r1, r8, 2
 270:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 274:	00 00 10 91 
 278:	00 00 16 ad 	jl	lr, r1
 27c:	01 00 80 08 	add	r8, r8, 1
 280:	ee ff 00 a0 	beq	r0, r0, -72

Disassembly of section .init:

00000290 <_start>:
	...
 298:	00 10 00 0f 	add	sp, r0, 4096
 29c:	02 00 00 a0 	beq	r0, r0, 8
	...

000002a8 <__cstart>:
 2a8:	fc ff f0 0f 	add	sp, sp, -4
 2ac:	00 00 f1 9d 	str	lr, sp, 0
 2b0:	54 04 00 03 	add	r3, r0, 1108
 2b4:	30 04 00 02 	add	r2, r0, 1072
 2b8:	30 04 00 01 	add	r1, r0, 1072
 2bc:	a0 ff ff bd 	bl	lr, -384
 2c0:	61 04 00 03 	add	r3, r0, 1121
 2c4:	00 00 00 02 	add	r2, r0, 0
 2c8:	54 04 00 01 	add	r1, r0, 1108
 2cc:	b3 ff ff bd 	bl	lr, -308
 2d0:	c3 ff ff bd 	bl	lr, -244
 2d4:	00 00 00 02 	add	r2, r0, 0
 2d8:	00 00 20 01 	add	r1, r2, 0
 2dc:	08 00 00 bd 	bl	lr, 32

000002e0 <.L2>:
 2e0:	ff ff 00 a0 	beq	r0, r0, -4

000002e4 <_init>:
 2e4:	8a ff ff bd 	bl	lr, -472
 2e8:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

000002ec <_fini>:
 2ec:	6f ff ff bd 	bl	lr, -580
 2f0:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

00000300 <_main>:
 300:	f8 ff f0 0f 	add	sp, sp, -8
 304:	00 00 f1 97 	str	r7, sp, 0
 308:	04 00 f1 9d 	str	lr, sp, 4
 30c:	50 04 00 01 	add	r1, r0, 1104
 310:	00 00 10 9d 	ldr	lr, r1, 0
 314:	00 00 00 06 	add	r6, r0, 0
 318:	a0 00 00 05 	add	r5, r0, 160
 31c:	26 00 00 07 	add	r7, r0, 38

00000320 <.L2>:
 320:	33 00 60 74 	lsr	r4, r6, 3
 324:	51 00 40 74 	lsl	r4, r4, 5
 328:	00 00 d0 03 	add	r3, lr, 0
 32c:	00 00 00 01 	add	r1, r0, 0

00000330 <.L3>:
 330:	33 00 10 72 	lsr	r2, r1, 3
 334:	00 00 24 52 	or	r2, r2, r4
 338:	00 00 36 92 	sth	r2, r3, 0
 33c:	01 00 10 01 	add	r1, r1, 1
 340:	02 00 30 03 	add	r3, r3, 2
 344:	fa ff 51 a1 	bne	r1, r5, -24
 348:	01 00 60 06 	add	r6, r6, 1
 34c:	40 01 d0 0d 	add	lr, lr, 320
 350:	f3 ff 71 a6 	bne	r6, r7, -52

00000354 <.L4>:
 354:	ff ff 00 a0 	beq	r0, r0, -4

Disassembly of section .rodata:

00000360 <_snowhousecpu_regno_to_class>:
 360:	01 00 00 00 	add	r0, r0, 1
 364:	01 00 00 00 	add	r0, r0, 1
 368:	01 00 00 00 	add	r0, r0, 1
 36c:	01 00 00 00 	add	r0, r0, 1
 370:	01 00 00 00 	add	r0, r0, 1
 374:	01 00 00 00 	add	r0, r0, 1
 378:	01 00 00 00 	add	r0, r0, 1
 37c:	01 00 00 00 	add	r0, r0, 1
 380:	01 00 00 00 	add	r0, r0, 1
 384:	01 00 00 00 	add	r0, r0, 1
 388:	01 00 00 00 	add	r0, r0, 1
 38c:	01 00 00 00 	add	r0, r0, 1
 390:	01 00 00 00 	add	r0, r0, 1
 394:	02 00 00 00 	add	r0, r0, 2
 398:	03 00 00 00 	add	r0, r0, 3
 39c:	01 00 00 00 	add	r0, r0, 1
 3a0:	01 00 00 00 	add	r0, r0, 1
 3a4:	04 00 00 00 	add	r0, r0, 4
	...

000003b0 <_FB_SIZE>:
 3b0:	c0 17 00 00 	add	r0, r0, 6080
	...

000003c0 <_FB_HEIGHT>:
 3c0:	26 00 00 00 	add	r0, r0, 38
	...

000003d0 <_FB_WIDTH>:
 3d0:	a0 00 00 00 	add	r0, r0, 160
	...

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
 414:	02 00 00 00 	add	r0, r0, 2
 418:	03 00 00 00 	add	r0, r0, 3
 41c:	01 00 00 00 	add	r0, r0, 1
 420:	01 00 00 00 	add	r0, r0, 1
 424:	04 00 00 00 	add	r0, r0, 4

Disassembly of section .data:

00000430 <___dso_handle>:
	...

00000440 <_to_keep_loop_going>:
 440:	04 00 00 00 	add	r0, r0, 4
	...

00000450 <_fb>:
 450:	00 00 00 01 	add	r1, r0, 0

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

