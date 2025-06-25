for i in 0 1 2 3 4 5; do
	snowhousecpu-unknown-elf-as -c snowhousecpu-test-"$i".s -o snowhousecpu-test-"$i".o
	snowhousecpu-unknown-elf-ld --relax snowhousecpu-test-"$i".o -o snowhousecpu-test-"$i".elf
	snowhousecpu-unknown-elf-objcopy -O binary snowhousecpu-test-"$i".elf snowhousecpu-test-"$i".bin
	#snowhousecpu-unknown-elf-objdump -d snowhousecpu-test.o > snowhousecpu-test-dis-"$i"-nolink.s
	snowhousecpu-unknown-elf-objdump -d snowhousecpu-test-"$i".elf > snowhousecpu-test-"$i"-dis.s
done
