
snowhousecpu-framebuffer-demo.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_deregister_tm_clones>:
   0:	fc ff f0 0f 	add	sp, sp, -4
   4:	00 00 f1 9d 	str	lr, sp, 0
   8:	e0 03 00 02 	add	r2, r0, 992
   c:	e0 03 00 01 	add	r1, r0, 992
  10:	05 00 10 a2 	beq	r2, r1, 20
  14:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
  18:	00 00 00 02 
  1c:	00 00 00 03 	add	r3, r0, 0
  20:	01 00 30 a2 	beq	r2, r3, 4
  24:	00 00 26 ad 	jl	lr, r2

00000028 <.L1>:
  28:	00 00 f0 9d 	ldr	lr, sp, 0
  2c:	04 00 f0 0f 	add	sp, sp, 4
  30:	00 00 d6 a0 	jl	r0, lr
	...

00000040 <_register_tm_clones>:
  40:	fc ff f0 0f 	add	sp, sp, -4
  44:	00 00 f1 9d 	str	lr, sp, 0
  48:	e0 03 00 01 	add	r1, r0, 992
  4c:	e0 03 10 01 	add	r1, r1, 992
  50:	25 00 10 71 	asr	r1, r1, 2
  54:	f3 01 10 72 	lsr	r2, r1, 31
  58:	00 00 21 02 	add	r2, r2, r1
  5c:	15 00 20 72 	asr	r2, r2, 1
  60:	00 00 00 01 	add	r1, r0, 0
  64:	05 00 10 a2 	beq	r2, r1, 20
  68:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  6c:	00 00 00 03 
  70:	02 00 10 a3 	beq	r3, r1, 8
  74:	e0 03 00 01 	add	r1, r0, 992
  78:	00 00 36 ad 	jl	lr, r3

0000007c <.L5>:
  7c:	00 00 f0 9d 	ldr	lr, sp, 0
  80:	04 00 f0 0f 	add	sp, sp, 4
  84:	00 00 d6 a0 	jl	r0, lr
  88:	00 00 00 00 	add	r0, r0, 0

0000008c <___do_global_dtors_aux>:
  8c:	f8 ff f0 0f 	add	sp, sp, -8
  90:	00 00 f1 97 	str	r7, sp, 0
  94:	04 00 f1 9d 	str	lr, sp, 4
  98:	e0 03 00 07 	add	r7, r0, 992
  9c:	00 00 74 92 	ldub	r2, r7, 0
  a0:	00 00 00 01 	add	r1, r0, 0
  a4:	03 00 11 a2 	bne	r2, r1, 12
  a8:	d5 ff ff bd 	bl	lr, -172
  ac:	01 00 00 01 	add	r1, r0, 1
  b0:	00 00 77 91 	stb	r1, r7, 0

000000b4 <.L9>:
  b4:	00 00 f0 9d 	ldr	lr, sp, 0
  b8:	04 00 f0 97 	ldr	r7, sp, 4
  bc:	08 00 f0 0f 	add	sp, sp, 8
  c0:	00 00 d6 a0 	jl	r0, lr
	...

000000d0 <_call___do_global_dtors_aux>:
  d0:	fc ff f0 0f 	add	sp, sp, -4
  d4:	00 00 f1 9d 	str	lr, sp, 0
  d8:	00 00 f0 9d 	ldr	lr, sp, 0
  dc:	04 00 f0 0f 	add	sp, sp, 4
  e0:	00 00 d6 a0 	jl	r0, lr
	...

000000f0 <_frame_dummy>:
  f0:	fc ff f0 0f 	add	sp, sp, -4
  f4:	00 00 f1 9d 	str	lr, sp, 0
  f8:	d1 ff ff bd 	bl	lr, -188
  fc:	00 00 f0 9d 	ldr	lr, sp, 0
 100:	04 00 f0 0f 	add	sp, sp, 4
 104:	00 00 d6 a0 	jl	r0, lr
 108:	00 00 00 00 	add	r0, r0, 0

0000010c <_call_frame_dummy>:
 10c:	fc ff f0 0f 	add	sp, sp, -4
 110:	00 00 f1 9d 	str	lr, sp, 0
 114:	00 00 f0 9d 	ldr	lr, sp, 0
 118:	04 00 f0 0f 	add	sp, sp, 4
 11c:	00 00 d6 a0 	jl	r0, lr

00000120 <___libc_init_array>:
 120:	f4 ff f0 0f 	add	sp, sp, -12
 124:	00 00 f1 97 	str	r7, sp, 0
 128:	04 00 f1 98 	str	r8, sp, 4
 12c:	08 00 f1 9d 	str	lr, sp, 8
 130:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 134:	00 00 00 07 
 138:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 13c:	00 00 70 07 
 140:	25 00 70 77 	asr	r7, r7, 2
 144:	00 00 00 08 	add	r8, r0, 0

00000148 <.L2>:
 148:	12 00 71 a8 	bne	r8, r7, 72
 14c:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 150:	00 00 00 02 
 154:	00 00 00 01 	add	r1, r0, 0
 158:	02 00 10 a2 	beq	r2, r1, 8
 15c:	ff ff 00 c0 	bl	lr, -356 // pre #0xffff
 160:	a7 ff ff bd 

00000164 <.L4>:
 164:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 168:	00 00 00 07 
 16c:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 170:	00 00 70 07 
 174:	25 00 70 77 	asr	r7, r7, 2
 178:	00 00 00 08 	add	r8, r0, 0

0000017c <.L5>:
 17c:	0b 00 71 a8 	bne	r8, r7, 44
 180:	00 00 f0 9d 	ldr	lr, sp, 0
 184:	04 00 f0 98 	ldr	r8, sp, 4
 188:	08 00 f0 97 	ldr	r7, sp, 8
 18c:	0c 00 f0 0f 	add	sp, sp, 12
 190:	00 00 d6 a0 	jl	r0, lr

00000194 <.L3>:
 194:	21 00 80 71 	lsl	r1, r8, 2
 198:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 19c:	00 00 10 91 
 1a0:	00 00 16 ad 	jl	lr, r1
 1a4:	01 00 80 08 	add	r8, r8, 1
 1a8:	e7 ff 00 a0 	beq	r0, r0, -100

000001ac <.L6>:
 1ac:	21 00 80 71 	lsl	r1, r8, 2
 1b0:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 1b4:	00 00 10 91 
 1b8:	00 00 16 ad 	jl	lr, r1
 1bc:	01 00 80 08 	add	r8, r8, 1
 1c0:	ee ff 00 a0 	beq	r0, r0, -72
	...

000001d0 <_memcpy>:
 1d0:	00 00 00 04 	add	r4, r0, 0

000001d4 <.L2>:
 1d4:	01 00 41 a3 	bne	r3, r4, 4
 1d8:	00 00 d6 a0 	jl	r0, lr

000001dc <.L3>:
 1dc:	00 00 14 05 	add	r5, r1, r4
 1e0:	00 00 24 06 	add	r6, r2, r4
 1e4:	00 00 64 96 	ldub	r6, r6, 0
 1e8:	00 00 57 96 	stb	r6, r5, 0
 1ec:	01 00 40 04 	add	r4, r4, 1
 1f0:	f8 ff 00 a0 	beq	r0, r0, -32
	...

00000200 <_memset>:
 200:	00 00 10 04 	add	r4, r1, 0
 204:	00 00 13 03 	add	r3, r1, r3

00000208 <.L2>:
 208:	01 00 31 a4 	bne	r4, r3, 4
 20c:	00 00 d6 a0 	jl	r0, lr

00000210 <.L3>:
 210:	00 00 47 92 	stb	r2, r4, 0
 214:	01 00 40 04 	add	r4, r4, 1
 218:	fb ff 00 a0 	beq	r0, r0, -20

Disassembly of section .init:

00000220 <_start>:
	...
 228:	03 00 00 c0 	add	sp, r0, 196608 // pre #0x3
 22c:	00 00 00 0f 
 230:	02 00 00 a0 	beq	r0, r0, 8
	...

0000023c <__cstart>:
 23c:	fc ff f0 0f 	add	sp, sp, -4
 240:	00 00 f1 9d 	str	lr, sp, 0
 244:	d4 03 00 03 	add	r3, r0, 980
 248:	c0 03 00 02 	add	r2, r0, 960
 24c:	c0 03 00 01 	add	r1, r0, 960
 250:	df ff ff bd 	bl	lr, -132
 254:	e1 03 00 03 	add	r3, r0, 993
 258:	00 00 00 02 	add	r2, r0, 0
 25c:	d4 03 00 01 	add	r1, r0, 980
 260:	e7 ff ff bd 	bl	lr, -100
 264:	ae ff ff bd 	bl	lr, -328
 268:	00 00 00 02 	add	r2, r0, 0
 26c:	00 00 20 01 	add	r1, r2, 0
 270:	07 00 00 bd 	bl	lr, 28

00000274 <.L2>:
 274:	ff ff 00 a0 	beq	r0, r0, -4

00000278 <_init>:
 278:	9d ff ff bd 	bl	lr, -396
 27c:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

00000280 <_fini>:
 280:	82 ff ff bd 	bl	lr, -504
 284:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

00000290 <_main>:
 290:	fc ff f0 0f 	add	sp, sp, -4
 294:	00 00 f1 9d 	str	lr, sp, 0
 298:	d0 03 00 01 	add	r1, r0, 976
 29c:	00 00 10 9d 	ldr	lr, r1, 0
 2a0:	40 01 00 03 	add	r3, r0, 320
 2a4:	01 00 00 c0 	add	r6, r0, 76800 // pre #0x1
 2a8:	00 2c 00 06 

000002ac <.L4>:
 2ac:	00 00 d0 05 	add	r5, lr, 0
 2b0:	00 00 00 04 	add	r4, r0, 0
 2b4:	00 00 50 02 	add	r2, r5, 0
 2b8:	00 00 00 01 	add	r1, r0, 0

000002bc <.L3>:
 2bc:	00 00 21 91 	str	r1, r2, 0
 2c0:	01 00 10 01 	add	r1, r1, 1
 2c4:	04 00 20 02 	add	r2, r2, 4
 2c8:	fc ff 31 a1 	bne	r1, r3, -16
 2cc:	00 05 50 05 	add	r5, r5, 1280
 2d0:	40 01 40 04 	add	r4, r4, 320
 2d4:	f5 ff 60 a4 	beq	r4, r6, -44
 2d8:	00 00 50 02 	add	r2, r5, 0
 2dc:	00 00 00 01 	add	r1, r0, 0
 2e0:	f6 ff 00 a0 	beq	r0, r0, -40
