	.file	"snowhousecpu-test-cxx-1.cpp"
	.section	.text
	.align 4
.global __Z7add_u64mm
	.type	__Z7add_u64mm, @function
__Z7add_u64mm:
	cpy r3, r1        // *mov32: =r, r
	add r1, r1, r2  // addsi3: =r, r, r
	cpy r2, 1        // *mov32: =r, i
	bltu r1, r3, .L2
	cpy r2, 0        // *mov32: =r, i
.L2:
	jmp lr
	.size	__Z7add_u64mm, .-__Z7add_u64mm
	.ident	"GCC: (GNU) 15.0.1 20250125 (experimental)"
