
snowhousecpu-test-gcc-0.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_deregister_tm_clones>:
   0:	fc ff f0 0f 	add	sp, sp, -4
   4:	00 00 f1 9d 	str	lr, sp, 0
   8:	10 04 00 02 	add	r2, r0, 1040
   c:	10 04 00 01 	add	r1, r0, 1040
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
  48:	10 04 00 01 	add	r1, r0, 1040
  4c:	10 04 10 01 	add	r1, r1, 1040
  50:	25 00 10 71 	asr	r1, r1, 2
  54:	f3 01 10 72 	lsr	r2, r1, 31
  58:	00 00 21 02 	add	r2, r2, r1
  5c:	15 00 20 72 	asr	r2, r2, 1
  60:	00 00 00 01 	add	r1, r0, 0
  64:	05 00 10 a2 	beq	r2, r1, 20
  68:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  6c:	00 00 00 03 
  70:	02 00 10 a3 	beq	r3, r1, 8
  74:	10 04 00 01 	add	r1, r0, 1040
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
  98:	10 04 00 07 	add	r7, r0, 1040
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

Disassembly of section .init:

00000120 <_start>:
	...
 128:	03 00 00 c0 	add	sp, r0, 196608 // pre #0x3
 12c:	00 00 00 0f 
 130:	02 00 00 a0 	beq	r0, r0, 8
	...

0000013c <__cstart>:
 13c:	fc ff f0 0f 	add	sp, sp, -4
 140:	00 00 f1 9d 	str	lr, sp, 0
 144:	04 04 00 03 	add	r3, r0, 1028
 148:	e0 03 00 02 	add	r2, r0, 992
 14c:	e0 03 00 01 	add	r1, r0, 992
 150:	67 00 00 bd 	bl	lr, 412
 154:	11 04 00 03 	add	r3, r0, 1041
 158:	00 00 00 02 	add	r2, r0, 0
 15c:	04 04 00 01 	add	r1, r0, 1028
 160:	6f 00 00 bd 	bl	lr, 444
 164:	36 00 00 bd 	bl	lr, 216
 168:	00 00 00 02 	add	r2, r0, 0
 16c:	00 00 20 01 	add	r1, r2, 0
 170:	07 00 00 bd 	bl	lr, 28

00000174 <.L2>:
 174:	ff ff 00 a0 	beq	r0, r0, -4

00000178 <_init>:
 178:	dd ff ff bd 	bl	lr, -140
 17c:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .fini:

00000180 <_fini>:
 180:	c2 ff ff bd 	bl	lr, -248
 184:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.startup:

00000190 <_main>:
 190:	f8 ff f0 0f 	add	sp, sp, -8
 194:	00 00 f1 97 	str	r7, sp, 0
 198:	04 00 f1 9d 	str	lr, sp, 4
 19c:	00 04 00 01 	add	r1, r0, 1024
 1a0:	00 00 10 97 	ldr	r7, r1, 0
 1a4:	f0 03 00 01 	add	r1, r0, 1008
 1a8:	00 00 10 92 	ldr	r2, r1, 0
 1ac:	00 00 70 03 	add	r3, r7, 0
 1b0:	03 00 00 01 	add	r1, r0, 3
 1b4:	00 00 71 91 	str	r1, r7, 0
 1b8:	00 00 70 91 	ldr	r1, r7, 0
 1bc:	00 00 20 94 	ldr	r4, r2, 0
 1c0:	00 00 14 01 	add	r1, r1, r4
 1c4:	00 00 21 91 	str	r1, r2, 0
 1c8:	04 00 20 04 	add	r4, r2, 4
 1cc:	01 00 00 06 	add	r6, r0, 1
 1d0:	08 00 00 01 	add	r1, r0, 8

000001d4 <.L2>:
 1d4:	00 00 30 05 	add	r5, r3, 0
 1d8:	04 00 30 03 	add	r3, r3, 4
 1dc:	00 00 50 95 	ldr	r5, r5, 0
 1e0:	01 00 50 05 	add	r5, r5, 1
 1e4:	00 00 31 95 	str	r5, r3, 0
 1e8:	00 00 30 9d 	ldr	lr, r3, 0
 1ec:	00 00 40 95 	ldr	r5, r4, 0
 1f0:	00 00 5d 05 	add	r5, r5, lr
 1f4:	00 00 41 95 	str	r5, r4, 0
 1f8:	01 00 60 06 	add	r6, r6, 1
 1fc:	04 00 40 04 	add	r4, r4, 4
 200:	f4 ff 11 a6 	bne	r6, r1, -48
 204:	20 00 00 03 	add	r3, r0, 32
 208:	00 00 70 01 	add	r1, r7, 0
 20c:	38 00 00 bd 	bl	lr, 224
 210:	fc 03 00 03 	add	r3, r0, 1020
 214:	00 00 00 02 	add	r2, r0, 0

00000218 <.L3>:
 218:	00 00 30 91 	ldr	r1, r3, 0
 21c:	fe ff 20 a1 	beq	r1, r2, -8
 220:	00 00 00 01 	add	r1, r0, 0
 224:	00 00 f0 9d 	ldr	lr, sp, 0
 228:	04 00 f0 97 	ldr	r7, sp, 4
 22c:	08 00 f0 0f 	add	sp, sp, 8
 230:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.__libc_init_array:

00000240 <___libc_init_array>:
 240:	f4 ff f0 0f 	add	sp, sp, -12
 244:	00 00 f1 97 	str	r7, sp, 0
 248:	04 00 f1 98 	str	r8, sp, 4
 24c:	08 00 f1 9d 	str	lr, sp, 8
 250:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 254:	00 00 00 07 
 258:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 25c:	00 00 70 07 
 260:	25 00 70 77 	asr	r7, r7, 2
 264:	00 00 00 08 	add	r8, r0, 0

00000268 <.L2>:
 268:	12 00 71 a8 	bne	r8, r7, 72
 26c:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 270:	00 00 00 02 
 274:	00 00 00 01 	add	r1, r0, 0
 278:	02 00 10 a2 	beq	r2, r1, 8
 27c:	ff ff 00 c0 	bl	lr, -644 // pre #0xffff
 280:	5f ff ff bd 

00000284 <.L4>:
 284:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 288:	00 00 00 07 
 28c:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 290:	00 00 70 07 
 294:	25 00 70 77 	asr	r7, r7, 2
 298:	00 00 00 08 	add	r8, r0, 0

0000029c <.L5>:
 29c:	0b 00 71 a8 	bne	r8, r7, 44
 2a0:	00 00 f0 9d 	ldr	lr, sp, 0
 2a4:	04 00 f0 98 	ldr	r8, sp, 4
 2a8:	08 00 f0 97 	ldr	r7, sp, 8
 2ac:	0c 00 f0 0f 	add	sp, sp, 12
 2b0:	00 00 d6 a0 	jl	r0, lr

000002b4 <.L3>:
 2b4:	21 00 80 71 	lsl	r1, r8, 2
 2b8:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 2bc:	00 00 10 91 
 2c0:	00 00 16 ad 	jl	lr, r1
 2c4:	01 00 80 08 	add	r8, r8, 1
 2c8:	e7 ff 00 a0 	beq	r0, r0, -100

000002cc <.L6>:
 2cc:	21 00 80 71 	lsl	r1, r8, 2
 2d0:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 2d4:	00 00 10 91 
 2d8:	00 00 16 ad 	jl	lr, r1
 2dc:	01 00 80 08 	add	r8, r8, 1
 2e0:	ee ff 00 a0 	beq	r0, r0, -72

Disassembly of section .text.memcpy:

000002f0 <_memcpy>:
 2f0:	00 00 00 04 	add	r4, r0, 0

000002f4 <.L2>:
 2f4:	01 00 41 a3 	bne	r3, r4, 4
 2f8:	00 00 d6 a0 	jl	r0, lr

000002fc <.L3>:
 2fc:	00 00 14 05 	add	r5, r1, r4
 300:	00 00 24 06 	add	r6, r2, r4
 304:	00 00 64 96 	ldub	r6, r6, 0
 308:	00 00 57 96 	stb	r6, r5, 0
 30c:	01 00 40 04 	add	r4, r4, 1
 310:	f8 ff 00 a0 	beq	r0, r0, -32

Disassembly of section .text.memset:

00000320 <_memset>:
 320:	00 00 10 04 	add	r4, r1, 0
 324:	00 00 13 03 	add	r3, r1, r3

00000328 <.L2>:
 328:	01 00 31 a4 	bne	r4, r3, 4
 32c:	00 00 d6 a0 	jl	r0, lr

00000330 <.L3>:
 330:	00 00 47 92 	stb	r2, r4, 0
 334:	01 00 40 04 	add	r4, r4, 1
 338:	fb ff 00 a0 	beq	r0, r0, -20
