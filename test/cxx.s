
cxx:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_deregister_tm_clones>:
   0:	fc ff f0 0f 	add	sp, sp, -4
   4:	00 00 f1 9d 	str	lr, sp, 0
   8:	50 03 00 02 	add	r2, r0, 848
   c:	50 03 00 01 	add	r1, r0, 848
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
  48:	50 03 00 01 	add	r1, r0, 848
  4c:	50 03 10 01 	add	r1, r1, 848
  50:	25 00 10 71 	asr	r1, r1, 2
  54:	f3 01 10 72 	lsr	r2, r1, 31
  58:	00 00 21 02 	add	r2, r2, r1
  5c:	15 00 20 72 	asr	r2, r2, 1
  60:	00 00 00 01 	add	r1, r0, 0
  64:	05 00 10 a2 	beq	r2, r1, 20
  68:	00 00 00 c0 	add	r3, r0, 0 // pre #0x0
  6c:	00 00 00 03 
  70:	02 00 10 a3 	beq	r3, r1, 8
  74:	50 03 00 01 	add	r1, r0, 848
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
  98:	50 03 00 07 	add	r7, r0, 848
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
 144:	44 03 00 03 	add	r3, r0, 836
 148:	40 03 00 02 	add	r2, r0, 832
 14c:	40 03 00 01 	add	r1, r0, 832
 150:	3f 00 00 bd 	bl	lr, 252
 154:	51 03 00 03 	add	r3, r0, 849
 158:	00 00 00 02 	add	r2, r0, 0
 15c:	44 03 00 01 	add	r1, r0, 836
 160:	47 00 00 bd 	bl	lr, 284
 164:	0e 00 00 bd 	bl	lr, 56
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
 190:	00 00 00 01 	add	r1, r0, 0
 194:	00 00 d6 a0 	jl	r0, lr

Disassembly of section .text.__libc_init_array:

000001a0 <___libc_init_array>:
 1a0:	f4 ff f0 0f 	add	sp, sp, -12
 1a4:	00 00 f1 97 	str	r7, sp, 0
 1a8:	04 00 f1 98 	str	r8, sp, 4
 1ac:	08 00 f1 9d 	str	lr, sp, 8
 1b0:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 1b4:	00 00 00 07 
 1b8:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 1bc:	00 00 70 07 
 1c0:	25 00 70 77 	asr	r7, r7, 2
 1c4:	00 00 00 08 	add	r8, r0, 0

000001c8 <.L2>:
 1c8:	12 00 71 a8 	bne	r8, r7, 72
 1cc:	00 00 00 c0 	add	r2, r0, 0 // pre #0x0
 1d0:	00 00 00 02 
 1d4:	00 00 00 01 	add	r1, r0, 0
 1d8:	02 00 10 a2 	beq	r2, r1, 8
 1dc:	ff ff 00 c0 	bl	lr, -484 // pre #0xffff
 1e0:	87 ff ff bd 

000001e4 <.L4>:
 1e4:	00 00 00 c0 	add	r7, r0, 0 // pre #0x0
 1e8:	00 00 00 07 
 1ec:	00 00 00 c0 	add	r7, r7, 0 // pre #0x0
 1f0:	00 00 70 07 
 1f4:	25 00 70 77 	asr	r7, r7, 2
 1f8:	00 00 00 08 	add	r8, r0, 0

000001fc <.L5>:
 1fc:	0b 00 71 a8 	bne	r8, r7, 44
 200:	00 00 f0 9d 	ldr	lr, sp, 0
 204:	04 00 f0 98 	ldr	r8, sp, 4
 208:	08 00 f0 97 	ldr	r7, sp, 8
 20c:	0c 00 f0 0f 	add	sp, sp, 12
 210:	00 00 d6 a0 	jl	r0, lr

00000214 <.L3>:
 214:	21 00 80 71 	lsl	r1, r8, 2
 218:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 21c:	00 00 10 91 
 220:	00 00 16 ad 	jl	lr, r1
 224:	01 00 80 08 	add	r8, r8, 1
 228:	e7 ff 00 a0 	beq	r0, r0, -100

0000022c <.L6>:
 22c:	21 00 80 71 	lsl	r1, r8, 2
 230:	00 00 00 c0 	ldr	r1, r1, 0 // pre #0x0
 234:	00 00 10 91 
 238:	00 00 16 ad 	jl	lr, r1
 23c:	01 00 80 08 	add	r8, r8, 1
 240:	ee ff 00 a0 	beq	r0, r0, -72

Disassembly of section .text.memcpy:

00000250 <_memcpy>:
 250:	00 00 00 04 	add	r4, r0, 0

00000254 <.L2>:
 254:	01 00 41 a3 	bne	r3, r4, 4
 258:	00 00 d6 a0 	jl	r0, lr

0000025c <.L3>:
 25c:	00 00 14 05 	add	r5, r1, r4
 260:	00 00 24 06 	add	r6, r2, r4
 264:	00 00 64 96 	ldub	r6, r6, 0
 268:	00 00 57 96 	stb	r6, r5, 0
 26c:	01 00 40 04 	add	r4, r4, 1
 270:	f8 ff 00 a0 	beq	r0, r0, -32

Disassembly of section .text.memset:

00000280 <_memset>:
 280:	00 00 10 04 	add	r4, r1, 0
 284:	00 00 13 03 	add	r3, r1, r3

00000288 <.L2>:
 288:	01 00 31 a4 	bne	r4, r3, 4
 28c:	00 00 d6 a0 	jl	r0, lr

00000290 <.L3>:
 290:	00 00 47 92 	stb	r2, r4, 0
 294:	01 00 40 04 	add	r4, r4, 1
 298:	fb ff 00 a0 	beq	r0, r0, -20
