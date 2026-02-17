#include <stdint.h>

//volatile uint32_t* fb = (volatile uint32_t*)0x1000000ull;
volatile uint16_t* fb = (volatile uint16_t*)0x1000000ull;

const uint32_t FB_WIDTH = (
	/*64;*/ /*76 >> 1*/
	//320
	320 >> 1
	//160
	//160 >> 1
);
const uint32_t FB_HEIGHT = (
	/*64;*/ 
	//240
	240 >> 1
	//76 >> 1
);
const uint32_t FB_SIZE = FB_HEIGHT * FB_WIDTH;
volatile uint32_t* to_keep_loop_going = (
	(volatile uint32_t*)0x4ull
);

int main(int argc, char** argv) {
	//for (;;) {

		//uint32_t r = 0;
		//uint32_t g = 0;
		//uint32_t b = 0;

		for (uint32_t j=0; j<FB_HEIGHT; ++j) {
			for (uint32_t i=0; i<FB_WIDTH; ++i) {
				const uint32_t fb_idx = j * FB_WIDTH + i;
				fb[fb_idx] = (
					//((j & 0xffu) << 8u)
					//+ ((i & 0xffu) << 0u)
					(((j >> 3) & 0x1fu) << 5u)
					+ (((i >> 3) & 0x1fu) << 0u)
					//--------
					//((j & 0x1fu) << 5u)
					//+ ((i & 0x1fu) << 0u)
				);
				//fb[fb_idx] = (
				//	((r & 0xffu) << 0);
				//);
				//fb[j * FB_WIDTH + i] = /*i;*/ j * FB_WIDTH + i;//0x88ffdd;//i;
				//fb[j * FB_WIDTH + i]
			}
		}
	//}
	for (;;) {
	}
}
