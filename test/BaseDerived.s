	.file	"BaseDerived.cpp"
	.section	.text
	.align 4
.global __ZN4Base7consumeERKS_
	.type	__ZN4Base7consumeERKS_, @function
__ZN4Base7consumeERKS_:
	add r2, r2, 4  // addsi3: =r, r, i
	add r3, r1, 4  // addsi3: =r, r, i
	add r5, r1, 36  // addsi3: =r, r, i
.L2:
	ldr r4, r3, 0x0        // *mov32: =r, B
	ldr r6, r2, 0x0        // *mov32: =r, B
	add r4, r4, r6  // addsi3: =r, r, r
	str r4, r3, 0x0        // *mov32: =B, r
	add r2, r2, 4  // addsi3: =r, r, i
	add r3, r3, 4  // addsi3: =r, r, i
	bne r3, r5, .L2
	jmp lr
	.size	__ZN4Base7consumeERKS_, .-__ZN4Base7consumeERKS_
	.align 4
.global __ZN7Derived7consumeERK4Base
	.type	__ZN7Derived7consumeERK4Base, @function
__ZN7Derived7consumeERK4Base:
	add r2, r2, 4  // addsi3: =r, r, i
	add r3, r1, 4  // addsi3: =r, r, i
	add r5, r1, 36  // addsi3: =r, r, i
.L7:
	ldr r4, r3, 0x0        // *mov32: =r, B
	ldr r6, r2, 0x0        // *mov32: =r, B
	sub r4, r4, r6  // subsi3: =r, r, r
	str r4, r3, 0x0        // *mov32: =B, r
	add r2, r2, 4  // addsi3: =r, r, i
	add r3, r3, 4  // addsi3: =r, r, i
	bne r3, r5, .L7
	jmp lr
	.size	__ZN7Derived7consumeERK4Base, .-__ZN7Derived7consumeERK4Base
	.section	.text.startup,"ax",@progbits
	.align 4
	.type	__GLOBAL__sub_I__ZN4Base7consumeERKS_, @function
__GLOBAL__sub_I__ZN4Base7consumeERKS_:
	cpy r1, __ZTV7Derived+8        // *mov32: =r, i
	cpy r2, _a        // *mov32: =r, i
	str r1, r2, 0x0        // *mov32: =B, r
	cpy r2, _b        // *mov32: =r, i
	str r1, r2, 0x0        // *mov32: =B, r
	jmp lr
	.size	__GLOBAL__sub_I__ZN4Base7consumeERKS_, .-__GLOBAL__sub_I__ZN4Base7consumeERKS_
	.section	.init_array,"aw",%init_array
	.align 4
	.long	__GLOBAL__sub_I__ZN4Base7consumeERKS_
	.weak	__ZTS4Base
	.section	.rodata._ZTS4Base,"aG",@progbits,_ZTS4Base,comdat
	.align 4
	.type	__ZTS4Base, @object
	.size	__ZTS4Base, 6
__ZTS4Base:
	.string	"4Base"
	.weak	__ZTI4Base
	.section	.rodata._ZTI4Base,"aG",@progbits,_ZTI4Base,comdat
	.align 4
	.type	__ZTI4Base, @object
	.size	__ZTI4Base, 8
__ZTI4Base:
	.long	__ZTVN10__cxxabiv117__class_type_infoE+8
	.long	__ZTS4Base
	.weak	__ZTS7Derived
	.section	.rodata._ZTS7Derived,"aG",@progbits,_ZTS7Derived,comdat
	.align 4
	.type	__ZTS7Derived, @object
	.size	__ZTS7Derived, 9
__ZTS7Derived:
	.string	"7Derived"
	.weak	__ZTI7Derived
	.section	.rodata._ZTI7Derived,"aG",@progbits,_ZTI7Derived,comdat
	.align 4
	.type	__ZTI7Derived, @object
	.size	__ZTI7Derived, 12
__ZTI7Derived:
	.long	__ZTVN10__cxxabiv120__si_class_type_infoE+8
	.long	__ZTS7Derived
	.long	__ZTI4Base
	.weak	__ZTV4Base
	.section	.rodata._ZTV4Base,"aG",@progbits,_ZTV4Base,comdat
	.align 4
	.type	__ZTV4Base, @object
	.size	__ZTV4Base, 12
__ZTV4Base:
	.long	0
	.long	__ZTI4Base
	.long	__ZN4Base7consumeERKS_
	.weak	__ZTV7Derived
	.section	.rodata._ZTV7Derived,"aG",@progbits,_ZTV7Derived,comdat
	.align 4
	.type	__ZTV7Derived, @object
	.size	__ZTV7Derived, 12
__ZTV7Derived:
	.long	0
	.long	__ZTI7Derived
	.long	__ZN7Derived7consumeERK4Base
.global _b
	.section	.bss
	.align 4
	.type	_b, @object
	.size	_b, 36
_b:
	.zero	36
.global _a
	.align 4
	.type	_a, @object
	.size	_a, 36
_a:
	.zero	36
	.ident	"GCC: (GNU) 15.0.1 20250125 (experimental)"
