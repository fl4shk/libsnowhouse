	.file	"snowhousecpu-framebuffer-demo.c"
	.section	.text
	.section	.text.startup,"ax",@progbits
	.align 4
.global _main
	.type	_main, @function
_main:
	cpy r0, r0
	cpy r0, r0
	cpy r0, r0
	cpy r0, r0
	cpy sp, 0x800
	add sp, sp, -12  // addsi3: =r, r, i
	str r7, sp, 0        // *mov32: =B, r
	str r8, sp, 4        // *mov32: =B, r
	str lr, sp, 8        // *mov32: =B, r
	cpy r1, _fb        // *mov32: =r, i
	ldr r8, r1, 0x0        // *mov32: =r, B
	cpy r5, 320        // *mov32: =r, i
	cpy r7, 76800        // *mov32: =r, i
.L4:
	cpy lr, r8        // *mov32: =r, r
	cpy r4, 0        // *mov32: =r, i
	cpy r6, r4        // *mov32: =r, r
	cpy r3, lr        // *mov32: =r, r
	cpy r1, 0        // *mov32: =r, i
.L3:
	and r2, r1, 255
	add r2, r2, r4  // addsi3: =r, r, r
	str r2, r3, 0x0        // *mov32: =B, r
	add r1, r1, 1  // addsi3: =r, r, i
	add r3, r3, 4  // addsi3: =r, r, i
	bne r1, r5, .L3
	add lr, lr, 1280  // addsi3: =r, r, i
	add r6, r6, 320  // addsi3: =r, r, i
	add r4, r4, 256  // addsi3: =r, r, i
	beq r6, r7, .L4
	cpy r3, lr        // *mov32: =r, r
	cpy r1, 0        // *mov32: =r, i
	beq r0, r0, .L3
	.size	_main, .-_main
.global _FB_SIZE
	.section	.rodata
	.align 4
	.type	_FB_SIZE, @object
	.size	_FB_SIZE, 4
_FB_SIZE:
	.long	76800
.global _FB_HEIGHT
	.align 4
	.type	_FB_HEIGHT, @object
	.size	_FB_HEIGHT, 4
_FB_HEIGHT:
	.long	240
.global _FB_WIDTH
	.align 4
	.type	_FB_WIDTH, @object
	.size	_FB_WIDTH, 4
_FB_WIDTH:
	.long	320
.global _fb
	.section	.data
	.align 4
	.type	_fb, @object
	.size	_fb, 4
_fb:
	.long	16777216
	.ident	"GCC: (GNU) 15.0.1 20250125 (experimental)"
