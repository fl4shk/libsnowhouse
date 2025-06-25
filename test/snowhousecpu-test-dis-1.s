
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
  80:	78 56 00 a0 	add	r11, r0, 1450709556 // pre #0x5678
  84:	34 12 00 0b 
  88:	00 00 61 77 	mul	r7, r6, r1
  8c:	01 00 71 77 	udiv	r7, r7, r1
  90:	03 00 61 78 	umod	r8, r6, r1
  94:	00 00 00 a0 	beq	r0, r0, 0 // pre #0x0
  98:	00 00 00 90 

0000009c <_loop>:
  9c:	00 00 30 86 	ldr	r6, r3, 0
  a0:	00 00 00 a0 	bl	lr, 0 // pre #0x0
  a4:	00 00 d0 9d 
  a8:	04 00 31 86 	str	r6, r3, 4
  ac:	04 00 30 03 	add	r3, r3, 4
  b0:	ff ff 10 01 	add	r1, r1, -1
  b4:	00 00 00 a0 	bl	lr, 0 // pre #0x0
  b8:	00 00 d0 9d 
  bc:	00 00 00 a0 	bne	r1, r0, 0 // pre #0x0
  c0:	00 00 01 91 

000000c4 <_infin>:
  c4:	00 00 00 a0 	beq	r0, r0, 0 // pre #0x0
  c8:	00 00 00 90 

000000cc <_increment>:
  cc:	01 00 60 06 	add	r6, r6, 1
  d0:	00 00 d6 90 	jl	r0, lr

000000d4 <_divmod>:
  d4:	01 00 61 77 	udiv	r7, r6, r1
  d8:	03 00 61 78 	umod	r8, r6, r1
  dc:	00 00 d6 90 	jl	r0, lr

000000e0 <_irq_handler>:
  e0:	01 00 a0 0a 	add	r10, r10, 1
  e4:	0a 00 00 60 	ret	ira
	...
