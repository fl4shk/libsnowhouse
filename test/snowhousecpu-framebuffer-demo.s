	.file	"snowhousecpu-framebuffer-demo.c"
	.section	.text
	.section	.text.startup,"ax",@progbits
	.align 4
.global _main
	.type	_main, @function
_main:
	add sp, sp, -8  // addsi3: =r, r, i
	str r7, sp, 0        // *mov32: =B, r
	str lr, sp, 4        // *mov32: =B, r
// 47 "snowhousecpu-framebuffer-demo.c" 1
	cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy r0, r0
cpy sp, 0x800

// 0 "" 2
	cpy r1, _fb        // *mov32: =r, i
	ldr r3, r1, 0x0        // *mov32: =r, B
	cpy r5, 0        // *mov32: =r, i
	cpy r2, r5        // *mov32: =r, r
	cpy r7, 320        // *mov32: =r, i
	cpy lr, 76800        // *mov32: =r, i
.L2:
	cpy r4, r3        // *mov32: =r, r
	cpy r1, 0        // *mov32: =r, i
.L3:
	and r6, r1, 255
	add r6, r6, r5  // addsi3: =r, r, r
	str r6, r4, 0x0        // *mov32: =B, r
	add r1, r1, 1  // addsi3: =r, r, i
	add r4, r4, 4  // addsi3: =r, r, i
	bne r1, r7, .L3
	add r3, r3, 1280  // addsi3: =r, r, i
	add r2, r2, 320  // addsi3: =r, r, i
	add r5, r5, 256  // addsi3: =r, r, i
	bne r2, lr, .L2
.L4:
	beq r0, r0, .L4
	.size	_main, .-_main
.global _to_keep_loop_going
	.section	.data
	.align 4
	.type	_to_keep_loop_going, @object
	.size	_to_keep_loop_going, 4
_to_keep_loop_going:
	.long	4
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
