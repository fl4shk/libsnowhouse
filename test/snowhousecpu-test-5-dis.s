
snowhousecpu-test-5.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_push_loop-0x54>:
	...
   c:	b8 01 00 01 	add	r1, r0, 440
  10:	01 00 00 02 	add	r2, r0, 1
  14:	09 00 20 60 	cpy	ie, r2
  18:	31 00 20 61 	lsl	r1, r2, 3
  1c:	01 00 00 02 	add	r2, r0, 1
  20:	00 10 00 03 	add	r3, r0, 4096
  24:	08 00 00 04 	add	r4, r0, 8
  28:	24 01 00 05 	add	r5, r0, 292
  2c:	00 08 00 0f 	add	sp, r0, 2048
  30:	20 00 00 06 	add	r6, r0, 32
  34:	00 00 31 86 	str	r6, r3, 0
  38:	00 00 66 7a 	mul	r10, r6, r6
  3c:	00 00 30 85 	ldr	r5, r3, 0
  40:	00 10 31 85 	str	r5, r3, 4096
  44:	00 10 30 86 	ldr	r6, r3, 4096
  48:	04 00 00 07 	add	r7, r0, 4
  4c:	00 10 30 88 	ldr	r8, r3, 4096
  50:	00 00 57 79 	mul	r9, r5, r7

00000054 <_push_loop>:
  54:	00 00 f1 87 	str	r7, sp, 0
  58:	00 00 f0 88 	ldr	r8, sp, 0
  5c:	01 00 80 09 	add	r9, r8, 1
  60:	01 00 90 09 	add	r9, r9, 1
  64:	ff ff 70 07 	add	r7, r7, -1
  68:	e8 ff 01 97 	bne	r7, r0, -24
  6c:	34 12 00 a0 	add	r10, r0, 305419896 // pre #0x1234
  70:	78 56 00 0a 
  74:	00 00 61 77 	mul	r7, r6, r1
  78:	01 00 71 77 	udiv	r7, r7, r1
  7c:	03 00 61 78 	umod	r8, r6, r1
  80:	40 00 00 90 	beq	r0, r0, 64
  84:	03 00 10 0a 	add	r10, r1, 3
  88:	03 00 10 0a 	add	r10, r1, 3
  8c:	03 00 10 0a 	add	r10, r1, 3
  90:	03 00 10 0a 	add	r10, r1, 3
  94:	03 00 10 0a 	add	r10, r1, 3
  98:	03 00 10 0a 	add	r10, r1, 3
  9c:	03 00 10 0a 	add	r10, r1, 3
  a0:	03 00 10 0a 	add	r10, r1, 3
  a4:	03 00 10 0a 	add	r10, r1, 3
  a8:	03 00 10 0a 	add	r10, r1, 3
  ac:	03 00 10 0a 	add	r10, r1, 3
  b0:	03 00 10 0a 	add	r10, r1, 3
  b4:	03 00 10 0a 	add	r10, r1, 3
  b8:	03 00 10 0a 	add	r10, r1, 3
  bc:	03 00 10 0a 	add	r10, r1, 3
  c0:	03 00 10 0a 	add	r10, r1, 3

000000c4 <_loop>:
  c4:	00 00 30 86 	ldr	r6, r3, 0
  c8:	58 00 d0 9d 	bl	lr, 88
  cc:	04 00 31 86 	str	r6, r3, 4
  d0:	04 00 30 03 	add	r3, r3, 4
  d4:	ff ff 10 01 	add	r1, r1, -1
  d8:	90 00 d0 9d 	bl	lr, 144
  dc:	e4 ff 01 91 	bne	r1, r0, -28

000000e0 <_infin>:
  e0:	fc ff 00 90 	beq	r0, r0, -4
  e4:	03 00 10 0b 	add	r11, r1, 3
  e8:	03 00 10 0b 	add	r11, r1, 3
  ec:	03 00 10 0b 	add	r11, r1, 3
  f0:	03 00 10 0b 	add	r11, r1, 3
  f4:	03 00 10 0b 	add	r11, r1, 3
  f8:	03 00 10 0b 	add	r11, r1, 3
  fc:	03 00 10 0b 	add	r11, r1, 3
 100:	03 00 10 0b 	add	r11, r1, 3
 104:	03 00 10 0b 	add	r11, r1, 3
 108:	03 00 10 0b 	add	r11, r1, 3
 10c:	03 00 10 0b 	add	r11, r1, 3
 110:	03 00 10 0b 	add	r11, r1, 3
 114:	03 00 10 0b 	add	r11, r1, 3
 118:	03 00 10 0b 	add	r11, r1, 3
 11c:	03 00 10 0b 	add	r11, r1, 3
 120:	03 00 10 0b 	add	r11, r1, 3

00000124 <_increment>:
 124:	01 00 60 06 	add	r6, r6, 1
 128:	00 00 d6 90 	jl	r0, lr
 12c:	03 00 10 0c 	add	r12, r1, 3
 130:	03 00 10 0c 	add	r12, r1, 3
 134:	03 00 10 0c 	add	r12, r1, 3
 138:	03 00 10 0c 	add	r12, r1, 3
 13c:	03 00 10 0c 	add	r12, r1, 3
 140:	03 00 10 0c 	add	r12, r1, 3
 144:	03 00 10 0c 	add	r12, r1, 3
 148:	03 00 10 0c 	add	r12, r1, 3
 14c:	03 00 10 0c 	add	r12, r1, 3
 150:	03 00 10 0c 	add	r12, r1, 3
 154:	03 00 10 0c 	add	r12, r1, 3
 158:	03 00 10 0c 	add	r12, r1, 3
 15c:	03 00 10 0c 	add	r12, r1, 3
 160:	03 00 10 0c 	add	r12, r1, 3
 164:	03 00 10 0c 	add	r12, r1, 3
 168:	03 00 10 0c 	add	r12, r1, 3

0000016c <_divmod>:
 16c:	01 00 61 77 	udiv	r7, r6, r1
 170:	03 00 61 78 	umod	r8, r6, r1
 174:	00 00 d6 90 	jl	r0, lr
 178:	03 00 10 04 	add	r4, r1, 3
 17c:	03 00 10 04 	add	r4, r1, 3
 180:	03 00 10 04 	add	r4, r1, 3
 184:	03 00 10 04 	add	r4, r1, 3
 188:	03 00 10 04 	add	r4, r1, 3
 18c:	03 00 10 04 	add	r4, r1, 3
 190:	03 00 10 04 	add	r4, r1, 3
 194:	03 00 10 04 	add	r4, r1, 3
 198:	03 00 10 04 	add	r4, r1, 3
 19c:	03 00 10 04 	add	r4, r1, 3
 1a0:	03 00 10 04 	add	r4, r1, 3
 1a4:	03 00 10 04 	add	r4, r1, 3
 1a8:	03 00 10 04 	add	r4, r1, 3
 1ac:	03 00 10 04 	add	r4, r1, 3
 1b0:	03 00 10 04 	add	r4, r1, 3
 1b4:	03 00 10 04 	add	r4, r1, 3

000001b8 <_irq_handler>:
 1b8:	01 00 a0 0a 	add	r10, r10, 1
 1bc:	0a 00 00 60 	ret	ira
	...
