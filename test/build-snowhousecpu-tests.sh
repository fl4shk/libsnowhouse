for ((i=0; i<=15; i+=1)); do
#for ((i=0; i<1; i+=1)); do
	snowhousecpu-unknown-elf-as -c snowhousecpu-test-"$i".s -o snowhousecpu-test-"$i".o
	snowhousecpu-unknown-elf-ld --relax snowhousecpu-test-"$i".o -o snowhousecpu-test-"$i".elf
	snowhousecpu-unknown-elf-objcopy -O binary snowhousecpu-test-"$i".elf snowhousecpu-test-"$i".bin
	#snowhousecpu-unknown-elf-objdump -d snowhousecpu-test.o > snowhousecpu-test-dis-"$i"-nolink.s
	#snowhousecpu-unknown-elf-objdump -d snowhousecpu-test-"$i".elf > snowhousecpu-test-"$i"-dis.s
	snowhousecpu-unknown-elf-objdump -D snowhousecpu-test-"$i".elf > snowhousecpu-test-"$i"-dis.s
done
