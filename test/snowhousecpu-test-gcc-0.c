#include <stdint.h>

typedef uint8_t u8;
typedef uint16_t u16;
typedef uint32_t u32;
typedef uint64_t u64;

typedef int8_t i8;
typedef int16_t i16;
typedef int32_t i32;
typedef int64_t i64;


#define TEST_ARR_SIZE 8
//u32 test_arr[TEST_ARR_SIZE];
volatile u32* test_arr = (volatile u32*)0x400ul;
volatile u32* test_arr_1 = (volatile u32*)0x800ul;

int main(int argc, char** argv) {
	for (u32 i=0; i<TEST_ARR_SIZE; ++i) {
		if (i == 0) {
			test_arr[i] = 0x3;
		} else {
			test_arr[i] = test_arr[i - 1] + 1;
		}
		test_arr_1[i] += test_arr[i];
	}
	for (;;) {
		if (
			*(volatile u32*)0x3fc
		) {
			break;
		}
	}
}
