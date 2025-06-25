snowhousecpu-unknown-elf-as -c snowhousecpu-test.s -o snowhousecpu-test.o
snowhousecpu-unknown-elf-ld --relax snowhousecpu-test.o -o snowhousecpu-test.elf
snowhousecpu-unknown-elf-objcopy -O binary snowhousecpu-test.elf test.bin
snowhousecpu-unknown-elf-objdump -d snowhousecpu-test.o > snowhousecpu-test-dis-1.s
snowhousecpu-unknown-elf-objdump -d snowhousecpu-test.elf > snowhousecpu-test-dis.s
