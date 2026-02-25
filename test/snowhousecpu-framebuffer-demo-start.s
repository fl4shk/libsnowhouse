.section .text.start
.align 4
.global _my_text_start
_my_text_start:
	cpy r0, r0
	cpy r0, r0
	cpy r0, r0
	cpy r0, r0

	cpy r1, 0x1
	cpy r2, 0x4
	cpy r3, 0x2

	udivw r1, r2, r3
	//sdivw r1, r2, r3

	cpy r1, -1
	cpy r3, -2
	sdivw r1, r2, r3

	cpy r1, r0
	cpy r2, r0
	cpy r3, r0
	cpy r4, r0
	cpy r5, r0
	cpy r6, r0
	cpy r7, r0
	cpy r8, r0
	cpy r9, r0
	cpy r10, r0
	cpy r11, r0
	cpy r12, r0
	cpy lr, r0
	cpy fp, r0
	cpy sp, r0
	bl r0, _start
	//cpy sp, 0x800
	//bl r0, _main
