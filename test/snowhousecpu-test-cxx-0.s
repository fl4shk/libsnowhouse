	.file	"snowhousecpu-test-cxx-0.cpp"
	.section	.text
	.section	.text.startup,"ax",@progbits
	.align 4
.global _main
	.type	_main, @function
_main:
	add sp, sp, -12  // addsi3: =r, r, i
	str r7, sp, 0        // *mov32: =B, r
	str r8, sp, 4        // *mov32: =B, r
	str lr, sp, 8        // *mov32: =B, r
	cpy r3, 2        // *mov32: =r, i
	cpy r8, _a        // *mov32: =r, i
	str r3, r8, 4        // *mov32: =B, r
	cpy r2, 4        // *mov32: =r, i
	str r2, r8, 8        // *mov32: =B, r
	cpy r1, 6        // *mov32: =r, i
	str r1, r8, 12        // *mov32: =B, r
	cpy r4, 8        // *mov32: =r, i
	str r4, r8, 16        // *mov32: =B, r
	cpy r4, 10        // *mov32: =r, i
	str r4, r8, 20        // *mov32: =B, r
	cpy r4, 12        // *mov32: =r, i
	str r4, r8, 24        // *mov32: =B, r
	cpy r4, 14        // *mov32: =r, i
	str r4, r8, 28        // *mov32: =B, r
	cpy r4, 16        // *mov32: =r, i
	str r4, r8, 32        // *mov32: =B, r
	cpy r7, 0        // *mov32: =r, i
	cpy r5, _b        // *mov32: =r, i
	str r7, r5, 4        // *mov32: =B, r
	cpy r4, 1        // *mov32: =r, i
	str r4, r5, 8        // *mov32: =B, r
	str r3, r5, 12        // *mov32: =B, r
	cpy r3, 3        // *mov32: =r, i
	str r3, r5, 16        // *mov32: =B, r
	str r2, r5, 20        // *mov32: =B, r
	cpy r2, 5        // *mov32: =r, i
	str r2, r5, 24        // *mov32: =B, r
	cpy r2, r5        // *mov32: =r, r
	str r1, r5, 28        // *mov32: =B, r
	cpy r1, 7        // *mov32: =r, i
	str r1, r5, 32        // *mov32: =B, r
	cpy r1, r8        // *mov32: =r, r
	bl __ZN7Derived7consumeERK4Base    // *call_value: =r, i
	add r2, r8, 4  // addsi3: =r, r, i
	add r3, r8, 36  // addsi3: =r, r, i
	cpy r1, r7        // *mov32: =r, r
.L2:
	ldr r4, r2, 0x0        // *mov32: =r, B
	add r1, r1, r4  // addsi3: =r, r, r
	add r2, r2, 4  // addsi3: =r, r, i
	bne r3, r2, .L2
	ldr lr, sp, 0        // *mov32: =r, B
	ldr r8, sp, 4        // *mov32: =r, B
	ldr r7, sp, 8        // *mov32: =r, B
	add sp, sp, 12  // addsi3: =r, r, i
	jmp lr
	.size	_main, .-_main
	.ident	"GCC: (GNU) 15.0.1 20250125 (experimental)"
