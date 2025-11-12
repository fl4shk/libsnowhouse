
snowhousecpu-test-4.elf:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_push_loop-0x58>:
	...
   c:	c4 01 00 01 	add	r1, r0, 452
  10:	07 00 10 70 	cpy	ids, r1
  14:	01 00 00 02 	add	r2, r0, 1
  18:	09 00 20 70 	cpy	ie, r2
  1c:	31 00 20 71 	lsl	r1, r2, 3
  20:	01 00 00 02 	add	r2, r0, 1
  24:	00 10 00 03 	add	r3, r0, 4096
  28:	08 00 00 04 	add	r4, r0, 8
  2c:	30 01 00 05 	add	r5, r0, 304
  30:	00 08 00 0f 	add	sp, r0, 2048
  34:	20 00 00 06 	add	r6, r0, 32
  38:	00 00 31 96 	str	r6, r3, 0
  3c:	00 00 66 8a 	mul	r10, r6, r6
  40:	00 00 30 95 	ldr	r5, r3, 0
  44:	00 10 31 95 	str	r5, r3, 4096
  48:	00 10 30 96 	ldr	r6, r3, 4096
  4c:	04 00 00 07 	add	r7, r0, 4
  50:	00 10 30 98 	ldr	r8, r3, 4096
  54:	00 00 57 89 	mul	r9, r5, r7

00000058 <_push_loop>:
  58:	00 00 f1 97 	str	r7, sp, 0
  5c:	00 00 f0 98 	ldr	r8, sp, 0
  60:	01 00 80 09 	add	r9, r8, 1
  64:	01 00 90 09 	add	r9, r9, 1
  68:	ff ff 70 07 	add	r7, r7, -1
  6c:	fa ff 01 a7 	bne	r7, r0, -24
  70:	34 12 00 c0 	add	r10, r0, 305419896 // pre #0x1234
  74:	78 56 00 0a 
	...
  80:	00 00 61 87 	mul	r7, r6, r1
  84:	01 00 71 87 	udiv	r7, r7, r1
  88:	03 00 61 88 	umod	r8, r6, r1
  8c:	10 00 00 a0 	beq	r0, r0, 64
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
  c4:	03 00 10 0a 	add	r10, r1, 3
  c8:	03 00 10 0a 	add	r10, r1, 3
  cc:	03 00 10 0a 	add	r10, r1, 3

000000d0 <_loop>:
  d0:	00 00 30 96 	ldr	r6, r3, 0
  d4:	16 00 00 bd 	bl	lr, 88
  d8:	04 00 31 96 	str	r6, r3, 4
  dc:	04 00 30 03 	add	r3, r3, 4
  e0:	ff ff 10 01 	add	r1, r1, -1
  e4:	24 00 00 bd 	bl	lr, 144
  e8:	f9 ff 01 a1 	bne	r1, r0, -28

000000ec <_infin>:
  ec:	ff ff 00 a0 	beq	r0, r0, -4
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
 124:	03 00 10 0b 	add	r11, r1, 3
 128:	03 00 10 0b 	add	r11, r1, 3
 12c:	03 00 10 0b 	add	r11, r1, 3

00000130 <_increment>:
 130:	01 00 60 06 	add	r6, r6, 1
 134:	00 00 d6 a0 	jl	r0, lr
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
 16c:	03 00 10 0c 	add	r12, r1, 3
 170:	03 00 10 0c 	add	r12, r1, 3
 174:	03 00 10 0c 	add	r12, r1, 3

00000178 <_divmod>:
 178:	01 00 61 87 	udiv	r7, r6, r1
 17c:	03 00 61 88 	umod	r8, r6, r1
 180:	00 00 d6 a0 	jl	r0, lr
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
 1b8:	03 00 10 04 	add	r4, r1, 3
 1bc:	03 00 10 04 	add	r4, r1, 3
 1c0:	03 00 10 04 	add	r4, r1, 3

000001c4 <_irq_handler>:
 1c4:	01 00 a0 0a 	add	r10, r10, 1
 1c8:	0a 00 00 70 	ret	ira
	...
