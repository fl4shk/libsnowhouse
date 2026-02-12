
snowhousecpu-framebuffer-demo.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_my_text_start>:
	...
  10:	8b 00 00 b0 	bl	r0, 556
	...

00000020 <_deregister_tm_clones>:
  20:	fc ff f0 0f 	add	sp, sp, -4
  24:	00 00 f1 9d 	str	lr, sp, 0
  28:	10 04 00 02 	add	r2, r0, 1040
  2c:	10 04 00 01 	add	r1, r0, 1040
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
  68:	10 04 00 01 	add	r1, r0, 1040
  6c:	10 04 10 01 	add	r1, r1, 1040
  70:	25 00 10 71 	asr	r1, r1, 2
  74:	f3 01 10 72 	lsr	r2, r1, 31
  78:	00 00 21 02 	add	r2, r2, r1
  7c:	15 00 20 72 	asr	r2, r2, 1
  80:	00 00 00 01 	add	r1, r0, 0
  84:	05 00 10 a2 	beq	r2, r1, 20
  88:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  8c:	00 00 00 03 
  90:	02 00 10 a3 	beq	r3, r1, 8
  94:	10 04 00 01 	add	r1, r0, 1040
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
  b8:	10 04 00 07 	add	r7, r0, 1040
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
 140:	08 00 00 a3 	beq	r3, r0, 32
 144:	00 00 13 03 	add	r3, r1, r3
 148:	00 00 10 04 	add	r4, r1, 0
 14c:	00 00 20 05 	add	r5, r2, 0

00000150 <.Lmemcpy_loop>:
 150:	00 00 54 92 	ldub	r2, r5, 0
 154:	00 00 47 92 	stb	r2, r4, 0
 158:	01 00 40 04 	add	r4, r4, 1
 15c:	01 00 50 05 	add	r5, r5, 1
 160:	fb ff 31 a4 	bne	r4, r3, -20

00000164 <.Lmemcpy_end>:
 164:	00 00 d6 a0 	jl	r0, lr

00000168 <_memset>:
 168:	05 00 00 a3 	beq	r3, r0, 20
 16c:	00 00 13 03 	add	r3, r1, r3
 170:	00 00 10 04 	add	r4, r1, 0

00000174 <.Lmemset_loop>:
 174:	00 00 47 92 	stb	r2, r4, 0
 178:	01 00 40 04 	add	r4, r4, 1
 17c:	fd ff 31 a4 	bne	r4, r3, -12

00000180 <.Lmemset_end>:
 180:	00 00 d6 a0 	jl	r0, lr
	...

00000190 <___libc_init_array>:
 190:	f4 ff f0 0f 	add	sp, sp, -12
 194:	00 00 f1 97 	str	r7, sp, 0
 198:	04 00 f1 98 	str	r8, sp, 4
 19c:	08 00 f1 9d 	str	lr, sp, 8
 1a0:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 1a4:	00 00 00 07 
 1a8:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 1ac:	00 00 70 07 
 1b0:	25 00 70 77 	asr	r7, r7, 2
 1b4:	00 00 00 08 	add	r8, r0, 0

000001b8 <.L2>:
 1b8:	12 00 71 a8 	bne	r8, r7, 72
 1bc:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 1c0:	00 00 00 02 
 1c4:	00 00 00 01 	add	r1, r0, 0
 1c8:	02 00 10 a2 	beq	r2, r1, 8
 1cc:	ff ff 00 c0 	bl	lr, -468 // pre #0xffff
 1d0:	8b ff ff bd 

000001d4 <.L4>:
 1d4:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 1d8:	00 00 00 07 
 1dc:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 1e0:	00 00 70 07 
 1e4:	25 00 70 77 	asr	r7, r7, 2
 1e8:	00 00 00 08 	add	r8, r0, 0

000001ec <.L5>:
 1ec:	0b 00 71 a8 	bne	r8, r7, 44
 1f0:	00 00 f0 9d 	ldr	lr, sp, 0
 1f4:	04 00 f0 98 	ldr	r8, sp, 4
 1f8:	08 00 f0 97 	ldr	r7, sp, 8
 1fc:	0c 00 f0 0f 	add	sp, sp, 12
 200:	00 00 d6 a0 	jl	r0, lr

00000204 <.L3>:
 204:	21 00 80 71 	lsl	r1, r8, 2
 208:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 20c:	00 00 10 91 
 210:	00 00 16 ad 	jl	lr, r1
 214:	01 00 80 08 	add	r8, r8, 1
 218:	e7 ff 00 a0 	beq	r0, r0, -100

0000021c <.L6>:
 21c:	21 00 80 71 	lsl	r1, r8, 2
 220:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 224:	00 00 10 91 
 228:	00 00 16 ad 	jl	lr, r1
 22c:	01 00 80 08 	add	r8, r8, 1
 230:	ee ff 00 a0 	beq	r0, r0, -72

Disassembly of section .init:

00000240 <_start>:
	...
 248:	00 10 00 0f 	add	sp, r0, 4096
 24c:	02 00 00 a0 	beq	r0, r0, 8
	...

00000258 <__cstart>:
 258:	fc ff f0 0f 	add	sp, sp, -4
 25c:	00 00 f1 9d 	str	lr, sp, 0
 260:	04 04 00 03 	add	r3, r0, 1028
 264:	e0 03 00 02 	add	r2, r0, 992
 268:	e0 03 00 01 	add	r1, r0, 992
 26c:	b4 ff ff bd 	bl	lr, -304
 270:	11 04 00 03 	add	r3, r0, 1041
 274:	00 00 00 02 	add	r2, r0, 0
 278:	04 04 00 01 	add	r1, r0, 1028
 27c:	ba ff ff bd 	bl	lr, -280
 280:	c3 ff ff bd 	bl	lr, -244
 284:	00 00 00 02 	add	r2, r0, 0
 288:	00 00 20 01 	add	r1, r2, 0
 28c:	08 00 00 bd 	bl	lr, 32

00000290 <.L2>:
 290:	ff ff 00 a0 	beq	r0, r0, -4

00000294 <_init>:
 294:	9e ff ff bd 	bl	lr, -392
 298:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

0000029c <_fini>:
 29c:	83 ff ff bd 	bl	lr, -500
 2a0:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

000002b0 <_main>:
 2b0:	f8 ff f0 0f 	add	sp, sp, -8
 2b4:	00 00 f1 97 	str	r7, sp, 0
 2b8:	04 00 f1 9d 	str	lr, sp, 4
 2bc:	00 04 00 01 	add	r1, r0, 1024
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
 308:	ff ff 00 a0 	beq	r0, r0, -4

Disassembly of section .rodata:

00000310 <_snowhousecpu_regno_to_class>:
 310:	01 00 00 00 	add	r0, r0, 1
 314:	01 00 00 00 	add	r0, r0, 1
 318:	01 00 00 00 	add	r0, r0, 1
 31c:	01 00 00 00 	add	r0, r0, 1
 320:	01 00 00 00 	add	r0, r0, 1
 324:	01 00 00 00 	add	r0, r0, 1
 328:	01 00 00 00 	add	r0, r0, 1
 32c:	01 00 00 00 	add	r0, r0, 1
 330:	01 00 00 00 	add	r0, r0, 1
 334:	01 00 00 00 	add	r0, r0, 1
 338:	01 00 00 00 	add	r0, r0, 1
 33c:	01 00 00 00 	add	r0, r0, 1
 340:	01 00 00 00 	add	r0, r0, 1
 344:	02 00 00 00 	add	r0, r0, 2
 348:	03 00 00 00 	add	r0, r0, 3
 34c:	01 00 00 00 	add	r0, r0, 1
 350:	01 00 00 00 	add	r0, r0, 1
 354:	04 00 00 00 	add	r0, r0, 4
	...

00000360 <_FB_SIZE>:
 360:	00 2c 01 00 	add	r0, r0, r1
	...

00000370 <_FB_HEIGHT>:
 370:	f0 00 00 00 	add	r0, r0, 240
	...

00000380 <_FB_WIDTH>:
 380:	40 01 00 00 	add	r0, r0, 320
	...

00000390 <_snowhousecpu_regno_to_class>:
 390:	01 00 00 00 	add	r0, r0, 1
 394:	01 00 00 00 	add	r0, r0, 1
 398:	01 00 00 00 	add	r0, r0, 1
 39c:	01 00 00 00 	add	r0, r0, 1
 3a0:	01 00 00 00 	add	r0, r0, 1
 3a4:	01 00 00 00 	add	r0, r0, 1
 3a8:	01 00 00 00 	add	r0, r0, 1
 3ac:	01 00 00 00 	add	r0, r0, 1
 3b0:	01 00 00 00 	add	r0, r0, 1
 3b4:	01 00 00 00 	add	r0, r0, 1
 3b8:	01 00 00 00 	add	r0, r0, 1
 3bc:	01 00 00 00 	add	r0, r0, 1
 3c0:	01 00 00 00 	add	r0, r0, 1
 3c4:	02 00 00 00 	add	r0, r0, 2
 3c8:	03 00 00 00 	add	r0, r0, 3
 3cc:	01 00 00 00 	add	r0, r0, 1
 3d0:	01 00 00 00 	add	r0, r0, 1
 3d4:	04 00 00 00 	add	r0, r0, 4

Disassembly of section .data:

000003e0 <___dso_handle>:
	...

000003f0 <_to_keep_loop_going>:
 3f0:	04 00 00 00 	add	r0, r0, 4
	...

00000400 <_fb>:
 400:	00 00 00 01 	add	r1, r0, 0

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

