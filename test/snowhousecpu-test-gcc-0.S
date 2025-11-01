	.file	"snowhousecpu-test-gcc-0.c"
	.section	.text
	.section	.text.startup,"ax",@progbits
	.align 4
.global _main
	.type	_main, @function
_main:
	add sp, sp, -8  // addsi3: =r, r, i
	str r7, sp, 0        // *mov32: =B, r
	str lr, sp, 4        // *mov32: =B, r
	cpy r1, _test_arr_0        // *mov32: =r, i
	ldr r7, r1, 0x0        // *mov32: =r, B
	cpy r1, _test_arr_1        // *mov32: =r, i
	ldr r2, r1, 0x0        // *mov32: =r, B
	cpy r3, r7        // *mov32: =r, r
	cpy r1, 3        // *mov32: =r, i
	str r1, r7, 0x0        // *mov32: =B, r
	ldr r1, r7, 0x0        // *mov32: =r, B
	ldr r4, r2, 0x0        // *mov32: =r, B
	add r1, r1, r4  // addsi3: =r, r, r
	str r1, r2, 0x0        // *mov32: =B, r
	add r4, r2, 4  // addsi3: =r, r, i
	cpy r6, 1        // *mov32: =r, i
	cpy r1, 8        // *mov32: =r, i
.L2:
	cpy r5, r3        // *mov32: =r, r
	add r3, r3, 4  // addsi3: =r, r, i
	ldr r5, r5, 0x0        // *mov32: =r, B
	add r5, r5, 1  // addsi3: =r, r, i
	str r5, r3, 0x0        // *mov32: =B, r
	ldr lr, r3, 0x0        // *mov32: =r, B
	ldr r5, r4, 0x0        // *mov32: =r, B
	add r5, r5, lr  // addsi3: =r, r, r
	str r5, r4, 0x0        // *mov32: =B, r
	add r6, r6, 1  // addsi3: =r, r, i
	add r4, r4, 4  // addsi3: =r, r, i
	bne r6, r1, .L2
	cpy r3, 32        // *mov32: =r, i
	cpy r1, r7        // *mov32: =r, r
	bl _memcpy    // *call_value: =r, i
	cpy r3, 1020        // *mov32: =r, i
	cpy r2, 0        // *mov32: =r, i
.L3:
	ldr r1, r3, 0x0        // *mov32: =r, B
	beq r1, r2, .L3
	cpy r1, 0        // *mov32: =r, i
	ldr lr, sp, 0        // *mov32: =r, B
	ldr r7, sp, 4        // *mov32: =r, B
	add sp, sp, 8  // addsi3: =r, r, i
	jmp lr
	.size	_main, .-_main
.global _test_arr_1
	.section	.data
	.align 4
	.type	_test_arr_1, @object
	.size	_test_arr_1, 4
_test_arr_1:
	.long	2048
.global _test_arr_0
	.align 4
	.type	_test_arr_0, @object
	.size	_test_arr_0, 4
_test_arr_0:
	.long	1024
	.ident	"GCC: (GNU) 15.0.1 20250125 (experimental)"
