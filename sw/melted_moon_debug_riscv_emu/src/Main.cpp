#include "MiscIncludes.hpp"
#include "MeltedMoonDebugRiscvEmu.hpp"

using namespace liborangepower::misc_output;
using namespace liborangepower::integer_types;

//using snowhousecpu_dasm_info_rd32_func = int (*)(
//    //struct snowhousecpu_dasm_info_t * /* self */
//    u8* buf, size_t offset
//);

//extern "C" {
////--------
////extern int snprint_one_insn_snowhousecpu(
////    u32* curr_pc,
////    char* str_buf, size_t str_buf_size,
////    snowhousecpu_dasm_info_rd32_func rd32_func,
////    u32* just_check_for_pre,
////    bool show_enc_instr
////);
//extern void
//snowhousecpu_dasm_info_ctor(
//    snowhousecpu_dasm_info_t* self,
//    snowhousecpu_dasm_info_rd32_func rd32_func,
//    bool show_enc_instr
//);
//
//extern void
//snowhousecpu_dasm_info_do_disassemble(snowhousecpu_dasm_info_t* self);
//
//extern void
//do_snprintf_insn_snowhousecpu_main(
//  snowhousecpu_dasm_info_t* args,
//  char* temp_buf, size_t temp_buf_lim
//);
////--------
//}

//static std::unique_ptr<u8[]> main_mem;

//static constexpr size_t MAIN_MEM_SIZE = 128ull * 1024ull * 1024ull; 

//static int my_dasm_rd32_func(u8* buf, size_t offset);


static MeltedMoonDebugRiscvEmu emu;

//static int my_dasm_rd32_func(u8* buf, size_t offset) {
//    return emu.my_dasm_rd32_func(buf, offset);
//}

int main(int argc, char** argv) {
    if (argc == 2) {
        emu = MeltedMoonDebugRiscvEmu(argv[1]);
    } else if (argc == 3) {
        emu = MeltedMoonDebugRiscvEmu(argv[1], std::atoi(argv[2]));
    } else {
        std::fprintf(
            stderr,
            "Usage 0: %s <program_filename:string>\n"
            "Usage 1: %s "
                "<program_filename:string> "
                "<do_extra_print_start_pc:uint32>\n",
            argv[0],
            argv[0]
        );
        std::exit(1);
    }

    sdl::Window window = SDL_CreateWindow(
        "Melted Moon - Somewhat Of A Simulator!",   // title
        SDL_WINDOWPOS_CENTERED, // x
        SDL_WINDOWPOS_CENTERED, // y
        SCREENWIDTH * 2,        // WIDTH
        FULL_SCREENHEIGHT * 2,  // HEIGHT
                                // flags
        (
            SDL_WINDOW_SHOWN
            //| SDL_WINDOW_RESIZABLE
        )
    );
    sdl::Renderer renderer = SDL_CreateRenderer(
        window, // window
        -1,     // index
        0       // flags
    );
    sdl::Texture texture = SDL_CreateTexture(
        renderer,
        SDL_PIXELFORMAT_ARGB8888,
        SDL_TEXTUREACCESS_STATIC,
        SCREENWIDTH * 2,
        FULL_SCREENHEIGHT * 2
    );

    std::unique_ptr<Uint32[]> pixels(
        new Uint32[SCREENWIDTH * 2 * FULL_SCREENHEIGHT * 2]
    );

    //struct timeval tp;
    //size_t update_tp_cnt = 0u;
    struct timeval tp;
    gettimeofday(&tp, nullptr);
    //for (size_t instr_cnt=0; instr_cnt < 1024u; ++instr_cnt) 
    bool do_exit = false;

    SDL_AddTimer(
        Uint32(1u), // interval (ms)
        [](Uint32 interval, void* tp_void_ptr) -> Uint32 {
            gettimeofday((struct timeval*)tp_void_ptr, nullptr);
            return interval;
        },
        &tp
    );
    auto temp_func = [](
        Uint32 interval, void* do_exit_void_ptr
    ) -> Uint32 {
        SDL_Event e;
        while (SDL_PollEvent(&e) != 0) {
            if (e.type == SDL_QUIT) {
                printf("Exiting...\n");
                *(bool*)do_exit_void_ptr = true;
                break;
            }
            //else if (
            //    liborangepower::sdl::handle_key_events(
            //        e,
            //        _key_status_umap, 
            //        ksm_perf_total_backup
            //    )
            //) {
            //}
        }
        return interval;
    };
    SDL_AddTimer(
        Uint32(100), // interval (ms)
        temp_func,
        &do_exit
    );

    while (!do_exit) {
        //u16 temp_fb_data;
        //u32 temp_fb_addr;
        auto exec_temp = emu.exec_one_instr(tp);
        //if (exec_temp.sw_read_from_tp) {
        //    //update_tp_cnt = 0u;
        //} else {
        //    ++update_tp_cnt;
        //    if (update_tp_cnt >= 16u) {
        //        update_tp_cnt = 0u;
        //        gettimeofday(&tp, nullptr);
        //    }
        //}

        //gettimeofday(&tp, nullptr);
        //gettimeofday(&tp, nullptr);

        if (auto fb_start = exec_temp.sw_wrote_to_fb_end; fb_start) {
            //printout(
            //    "testificate!\n"
            //);
            for (size_t j=0; j<FULL_SCREENHEIGHT * 2; ++j) {
                for (size_t i=0; i<SCREENWIDTH * 2; ++i) {
                    const size_t k = (j >> 1) * SCREENWIDTH + (i >> 1);
                    const size_t l = j * SCREENWIDTH * 2 + i;
                    //const uint32_t r = (
                    //    screen_palette[(my_screen_buf[k] * 3u) + 0u]
                    //    | 0x7u
                    //);
                    //const uint32_t g = (
                    //    screen_palette[(my_screen_buf[k] * 3u) + 1u]
                    //    | 0x7u
                    //);
                    //const uint32_t b = (
                    //    screen_palette[(my_screen_buf[k] * 3u) + 2u]
                    //    | 0x7u
                    //);
                    u8* pal_idx = (u8*)(*fb_start) + k;
                    u32* item = (
                        (u32*)(
                            //(*fb_start)
                            //+ (
                            //    MeltedMoonDebugRiscvEmu::ADDR_PAL_START
                            //    - (
                            //        !exec_temp.which_fb
                            //        ? (
                            //            MeltedMoonDebugRiscvEmu
                            //            ::ADDR_FB_0_START
                            //        )
                            //        : (
                            //            MeltedMoonDebugRiscvEmu
                            //            ::ADDR_FB_1_START
                            //        )
                            //    )
                            //)
                            exec_temp.pal
                        )
                        + (*pal_idx)
                    );


                    //u16* item = (u16*)(*fb_start) + k;
                    //const u32 r = (
                    //    ((((*item) >> 0) & 0x1f) << 3) | 0x7
                    //);
                    //const u32 g = (
                    //    ((((*item) >> 5) & 0x1f) << 3) | 0x7
                    //);
                    //const u32 b = (
                    //    ((((*item) >> 10) & 0x1f) << 3) | 0x7
                    //);
                    //pixels[l] = (
                    //    ((r & 0xffu) << 16u)
                    //    | ((g & 0xffu) << 8u)
                    //    | ((b & 0xffu) << 0u)
                    //);

                    pixels[l] = (
                        (((*item) & 0xffu) << 16u)
                        | ((((*item) >> 8) & 0xffu) << 8u)
                        | ((((*item) >> 16) & 0xffu) << 0u)
                    );
                }
            }

            SDL_UpdateTexture(
                texture,
                NULL,
                pixels.get(),
                sizeof(Uint32) * SCREENWIDTH * 2// pitch
                //sizeof(Uint32) * HALF_SIZE_2D.x // pitch
                //sizeof(Uint32) * SIZE_2D.x * SIZE_2D.y
            );
            SDL_RenderClear(renderer);
            SDL_RenderCopy(renderer, texture, NULL, NULL);
            SDL_RenderPresent(renderer);
            memset(
                pixels.get(), 0,
                sizeof(Uint32) * SCREENWIDTH * 2 * FULL_SCREENHEIGHT *2
            );
        }
    }

    //SDL_DestroyTexture(texture);
    //SDL_DestroyRenderer(renderer);
    //SDL_DestroyWindow(window);

    return 0;
}
