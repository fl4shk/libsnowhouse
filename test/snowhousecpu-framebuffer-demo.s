	.file	"snowhousecpu-framebuffer-demo.c"
	.section	.text
	.align 4
.global _mul_test
	.type	_mul_test, @function
_mul_test:
	umulw r1, r1, r2 // r1 * r2 => {hi, r1} 
	jmp lr
	.size	_mul_test, .-_mul_test
	.align 4
.global _udivw_test
	.type	_udivw_test, @function
_udivw_test:
	add sp, sp, -4  // addsi3: =r, r, i
	str r7, sp, 0        // *mov32: =B, r
	cpy hi, r1        // *mov32: =h, r
	cpy r7, r2        // *mov32: =r, r
	cpy r1, r3        // *mov32: =r, r
	cpy r2, r4        // *mov32: =r, r
	udivw r7, r1, r2 // {hi, r7} / {r1, r2} => {hi, r7}
	cpy r1, hi        // *mov32: =r, h
	cpy r2, r7        // *mov32: =r, r
	ldr r7, sp, 0        // *mov32: =r, B
	add sp, sp, 4  // addsi3: =r, r, i
	jmp lr
	.size	_udivw_test, .-_udivw_test
	.align 4
.global _sdivw_test
	.type	_sdivw_test, @function
_sdivw_test:
	add sp, sp, -4  // addsi3: =r, r, i
	str r7, sp, 0        // *mov32: =B, r
	cpy hi, r1        // *mov32: =h, r
	cpy r7, r2        // *mov32: =r, r
	cpy r1, r3        // *mov32: =r, r
	cpy r2, r4        // *mov32: =r, r
	sdivw r7, r1, r2 // {hi, r7} / {r1, r2} => {hi, r7}
	cpy r1, hi        // *mov32: =r, h
	cpy r2, r7        // *mov32: =r, r
	ldr r7, sp, 0        // *mov32: =r, B
	add sp, sp, 4  // addsi3: =r, r, i
	jmp lr
	.size	_sdivw_test, .-_sdivw_test
	.align 4
.global _cmp_ltu
	.type	_cmp_ltu, @function
_cmp_ltu:
	sltu r1, r1, r2   // sltsi3: =r, r, r
	jmp lr
	.size	_cmp_ltu, .-_cmp_ltu
	.align 4
.global _cmp_lts
	.type	_cmp_lts, @function
_cmp_lts:
	slts r1, r1, r2   // sltsi3: =r, r, r
	jmp lr
	.size	_cmp_lts, .-_cmp_lts
	.align 4
.global _add64_test
	.type	_add64_test, @function
_add64_test:
	cpy r5, r1        // *mov32: =r, r
	add r1, r1, r3  // addsi3: =r, r, r
	sltu r5, r1, r5   // sltsi3: =r, r, r
	add r2, r2, r4  // addsi3: =r, r, r
	add r2, r5, r2  // addsi3: =r, r, r
	jmp lr
	.size	_add64_test, .-_add64_test
	.align 4
.global _sub64_test
	.type	_sub64_test, @function
_sub64_test:
	sub r3, r1, r3  // subsi3: =r, r, r
	cpy r5, 1        // *mov32: =r, i
	bgtu r3, r1, .L17
	cpy r5, 0        // *mov32: =r, i
.L17:
	sub r2, r2, r4  // subsi3: =r, r, r
	cpy r1, r3        // *mov32: =r, r
	sub r2, r2, r5  // subsi3: =r, r, r
	jmp lr
	.size	_sub64_test, .-_sub64_test
	.section	.text.startup,"ax",@progbits
	.align 4
.global _main
	.type	_main, @function
_main:
	add sp, sp, -8  // addsi3: =r, r, i
	str r7, sp, 0        // *mov32: =B, r
	str lr, sp, 4        // *mov32: =B, r
	cpy r1, _fb        // *mov32: =r, i
	ldr lr, r1, 0x0        // *mov32: =r, B
	cpy r6, 0        // *mov32: =r, i
	cpy r5, 320        // *mov32: =r, i
	cpy r7, 240        // *mov32: =r, i
.L21:
	lsr r4, r6, 3
	lsl r4, r4, 5
	cpy r3, lr        // *mov32: =r, r
	cpy r2, 0        // *mov32: =r, i
.L22:
	lsr r1, r2, 3
	and r1, r1, 31
	or r1, r1, r4
	sth r1, r3, 0x0    // *mov16: =B, r
	add r2, r2, 1  // addsi3: =r, r, i
	add r3, r3, 2  // addsi3: =r, r, i
	bne r2, r5, .L22
	add r6, r6, 1  // addsi3: =r, r, i
	add lr, lr, 640  // addsi3: =r, r, i
	bne r6, r7, .L21
.L23:
	bl r0, .L23
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
	.long	8388608
	.ident	"GCC: (GNU) 15.0.1 20250125 (experimental)"
