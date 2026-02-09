	.file	"snowhousecpu-framebuffer-demo.c"
	.section	.text
	.section	.text.startup,"ax",@progbits
	.align 4
.global _main
	.type	_main, @function
_main:
	cpy r1, _fb        // *mov32: =r, i
	ldr r5, r1, 0x0        // *mov32: =r, B
	add r6, r5, 1280  // addsi3: =r, r, i
	add r5, r5, 308480  // addsi3: =r, r, i
.L4:
	cpy r3, r6        // *mov32: =r, r
	cpy r4, 0        // *mov32: =r, i
	add r1, r3, -1280  // addsi3: =r, r, i
	cpy r2, r4        // *mov32: =r, r
.L3:
	str r2, r1, 0x0        // *mov32: =B, r
	add r2, r2, 1  // addsi3: =r, r, i
	add r1, r1, 4  // addsi3: =r, r, i
	bne r1, r3, .L3
	add r4, r4, 320  // addsi3: =r, r, i
	add r3, r3, 1280  // addsi3: =r, r, i
	beq r5, r3, .L4
	add r1, r3, -1280  // addsi3: =r, r, i
	cpy r2, r4        // *mov32: =r, r
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
