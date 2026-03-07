.section .text.start
.align 4
.global _my_text_start
_my_text_start:
	cpy r0, r0
	cpy r0, r0
	cpy r0, r0
	cpy r0, r0

	//cpy r1, 0x1
	//cpy r2, 0x4
	//cpy r3, 0x2

	//udivw r1, r2, r3
	////sdivw r1, r2, r3

	//cpy r1, -1
	//cpy r3, -2
	//sdivw r1, r2, r3
	//--------
	cpy r7, 0x1
	cpy hi, r7

	cpy r1, 0x4
	add r2, r0, r0
	cpy r3, 0x2
	udivw r1, r2, r3
	cpy r8, hi
	//--------
	cpy r7, -1
	cpy hi, r7

	cpy r1, 0x4
	add r2, r0, r0
	cpy r3, -2
	sdivw r1, r2, r3
	cpy r9, hi
	//--------

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

.set VBLANK_IRQ, (0x1 << 0x0)
.set TIMER_IRQ, (0x1 << 0x1)

.set IRQ_ID_REG, (0x1000000)
.set IRQ_ENABLE_REG, (0x1000004)

.align 4
.global _do_enable_irqs
_do_enable_irqs:
    // This function exists so as to make sure an IRQ does *not* occur
    // before `sp` is initialized.
    // It is intended to be called in `main()` in the C code.

    //cpy r1, (VBLANK_IRQ | TIMER_IRQ)
    str r1, r0, IRQ_ENABLE_REG

    cpy r1, _irq_handler
    cpy ids, r1

    cpy r1, 0x1
    cpy ie, r1

    jmp lr

.align 4
.global _irq_handler
_irq_handler:
    add sp, sp, -32
    str r1, sp, 0
    str r2, sp, 4
    str r3, sp, 8
    str r4, sp, 12
    str r5, sp, 16
    str r6, sp, 20
    str lr, sp, 24
    str fp, sp, 28

    ldr r1, r0, IRQ_ID_REG
    bl _irq_handler_primary_logic

.Lirq_handler_return:
    str r1, sp, 0
    str r2, sp, 4
    str r3, sp, 8
    str r4, sp, 12
    str r5, sp, 16
    str r6, sp, 20
    str lr, sp, 24
    str fp, sp, 28
    add sp, sp, 32
    ret ira
