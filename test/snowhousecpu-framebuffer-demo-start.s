.section .text.start
.align 4
.global _my_text_start
_my_text_start:
	cpy r0, r0
	cpy r0, r0
	cpy r0, r0
	cpy r0, r0
	cpy sp, 0x800
	bl r0, _start
	//bl r0, _main
