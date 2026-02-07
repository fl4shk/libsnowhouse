#include <stdint.h>

volatile uint32_t* fb = (volatile uint32_t*)0x1000000ull;

const uint32_t FB_WIDTH = 320;
const uint32_t FB_HEIGHT = 240;
const uint32_t FB_SIZE = FB_HEIGHT * FB_WIDTH;

int main(int argc, char** argv) {
	for (;;) {
		for (uint32_t j=0; j<FB_HEIGHT; ++j) {
			for (uint32_t i=0; i<FB_WIDTH; ++i) {
				fb[j * FB_WIDTH + i] = i;
			}
		}
	}
}
