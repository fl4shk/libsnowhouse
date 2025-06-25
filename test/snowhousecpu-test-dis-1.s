
snowhousecpu-test.o:     file format elf32-snowhousecpu


Disassembly of section .text:

00000000 <_push_loop-0x5c>:
	...
   c:	00 00 00 a0 	add	r1, r0, 0 // pre #0x0
  10:	00 00 00 01 
  14:	01 00 00 01 	add	r1, r0, 1
  18:	09 00 10 60 	cpy	ie, r1
  1c:	31 00 10 61 	lsl	r1, r1, 3
  20:	01 00 00 02 	add	r2, r0, 1
  24:	00 10 00 03 	add	r3, r0, 4096
  28:	08 00 00 04 	add	r4, r0, 8
  2c:	00 00 00 a0 	add	r5, r0, 0 // pre #0x0
  30:	00 00 00 05 
  34:	00 08 00 0f 	add	sp, r0, 2048
  38:	20 00 00 06 	add	r6, r0, 32
  3c:	00 00 31 86 	str	r6, r3, 0
  40:	00 00 66 70 	mul	r0, r6, r6
  44:	00 00 30 85 	ldr	r5, r3, 0
  48:	00 10 31 85 	str	r5, r3, 4096
  4c:	00 10 30 86 	ldr	r6, r3, 4096
  50:	04 00 00 07 	add	r7, r0, 4
  54:	00 10 30 88 	ldr	r8, r3, 4096
  58:	00 00 57 79 	mul	r9, r5, r7

0000005c <_push_loop>:
  5c:	00 00 f1 87 	str	r7, sp, 0
  60:	00 00 f0 88 	ldr	r8, sp, 0
  64:	01 00 80 09 	add	r9, r8, 1
  68:	01 00 90 09 	add	r9, r9, 1
  6c:	ff ff 70 07 	add	r7, r7, -1
  70:	00 00 00 a0 	bne	r7, r0, 0 // pre #0x0
  74:	00 00 01 97 
  78:	34 12 00 a0 	add	r10, r0, 305419896 // pre #0x1234
  7c:	78 56 00 0a 
	...
  8c:	00 00 61 77 	mul	r7, r6, r1
  90:	01 00 71 77 	udiv	r7, r7, r1
  94:	03 00 61 78 	umod	r8, r6, r1
  98:	00 00 00 a0 	beq	r0, r0, 0 // pre #0x0
  9c:	00 00 00 90 
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
  d0:	03 00 10 0a 	add	r10, r1, 3
  d4:	03 00 10 0a 	add	r10, r1, 3
  d8:	03 00 10 0a 	add	r10, r1, 3
  dc:	03 00 10 0a 	add	r10, r1, 3

000000e0 <_loop>:
  e0:	00 00 30 86 	ldr	r6, r3, 0
  e4:	00 00 00 a0 	bl	lr, 0 // pre #0x0
  e8:	00 00 d0 9d 
  ec:	04 00 31 86 	str	r6, r3, 4
  f0:	04 00 30 03 	add	r3, r3, 4
  f4:	ff ff 10 01 	add	r1, r1, -1
  f8:	00 00 00 a0 	bl	lr, 0 // pre #0x0
  fc:	00 00 d0 9d 
 100:	00 00 00 a0 	bne	r1, r0, 0 // pre #0x0
 104:	00 00 01 91 

00000108 <_infin>:
 108:	00 00 00 a0 	beq	r0, r0, 0 // pre #0x0
 10c:	00 00 00 90 
 110:	03 00 10 0b 	add	r11, r1, 3
 114:	03 00 10 0b 	add	r11, r1, 3
 118:	03 00 10 0b 	add	r11, r1, 3
 11c:	03 00 10 0b 	add	r11, r1, 3
 120:	03 00 10 0b 	add	r11, r1, 3
 124:	03 00 10 0b 	add	r11, r1, 3
 128:	03 00 10 0b 	add	r11, r1, 3
 12c:	03 00 10 0b 	add	r11, r1, 3
 130:	03 00 10 0b 	add	r11, r1, 3
 134:	03 00 10 0b 	add	r11, r1, 3
 138:	03 00 10 0b 	add	r11, r1, 3
 13c:	03 00 10 0b 	add	r11, r1, 3
 140:	03 00 10 0b 	add	r11, r1, 3
 144:	03 00 10 0b 	add	r11, r1, 3
 148:	03 00 10 0b 	add	r11, r1, 3
 14c:	03 00 10 0b 	add	r11, r1, 3

00000150 <_increment>:
 150:	01 00 60 06 	add	r6, r6, 1
 154:	00 00 d6 90 	jl	r0, lr
 158:	03 00 10 0c 	add	r12, r1, 3
 15c:	03 00 10 0c 	add	r12, r1, 3
 160:	03 00 10 0c 	add	r12, r1, 3
 164:	03 00 10 0c 	add	r12, r1, 3
 168:	03 00 10 0c 	add	r12, r1, 3
 16c:	03 00 10 0c 	add	r12, r1, 3
 170:	03 00 10 0c 	add	r12, r1, 3
 174:	03 00 10 0c 	add	r12, r1, 3
 178:	03 00 10 0c 	add	r12, r1, 3
 17c:	03 00 10 0c 	add	r12, r1, 3
 180:	03 00 10 0c 	add	r12, r1, 3
 184:	03 00 10 0c 	add	r12, r1, 3
 188:	03 00 10 0c 	add	r12, r1, 3
 18c:	03 00 10 0c 	add	r12, r1, 3
 190:	03 00 10 0c 	add	r12, r1, 3
 194:	03 00 10 0c 	add	r12, r1, 3

00000198 <_divmod>:
 198:	01 00 61 77 	udiv	r7, r6, r1
 19c:	03 00 61 78 	umod	r8, r6, r1
 1a0:	00 00 d6 90 	jl	r0, lr
 1a4:	03 00 10 04 	add	r4, r1, 3
 1a8:	03 00 10 04 	add	r4, r1, 3
 1ac:	03 00 10 04 	add	r4, r1, 3
 1b0:	03 00 10 04 	add	r4, r1, 3
 1b4:	03 00 10 04 	add	r4, r1, 3
 1b8:	03 00 10 04 	add	r4, r1, 3
 1bc:	03 00 10 04 	add	r4, r1, 3
 1c0:	03 00 10 04 	add	r4, r1, 3
 1c4:	03 00 10 04 	add	r4, r1, 3
 1c8:	03 00 10 04 	add	r4, r1, 3
 1cc:	03 00 10 04 	add	r4, r1, 3
 1d0:	03 00 10 04 	add	r4, r1, 3
 1d4:	03 00 10 04 	add	r4, r1, 3
 1d8:	03 00 10 04 	add	r4, r1, 3
 1dc:	03 00 10 04 	add	r4, r1, 3
 1e0:	03 00 10 04 	add	r4, r1, 3

000001e4 <_irq_handler>:
 1e4:	01 00 a0 0a 	add	r10, r10, 1
 1e8:	0a 00 00 60 	ret	ira
	...
