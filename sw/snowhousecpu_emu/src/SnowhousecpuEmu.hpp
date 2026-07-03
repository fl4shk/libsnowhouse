#ifndef SRC_SNOWHOUSECPU_EMUU_HPP
#define SRC_SNOWHOUSECPU_EMUU_HPP

#include "MiscIncludes.hpp"
#include <sys/time.h>

using namespace liborangepower::misc_output;
using namespace liborangepower::integer_types;

extern "C" {
//--------
//extern int snprint_one_insn_snowhousecpu(
//    u32* curr_pc,
//    char* str_buf, size_t str_buf_size,
//    snowhousecpu_dasm_info_rd32_func rd32_func,
//    u32* just_check_for_pre,
//    bool show_enc_instr
//);
extern void
snowhousecpu_dasm_info_ctor(
    snowhousecpu_dasm_info_t* self,
    snowhousecpu_dasm_info_rd32_func rd32_func,
    bool show_enc_instr
);

extern void
snowhousecpu_dasm_info_do_disassemble(snowhousecpu_dasm_info_t* self);

extern void
do_snprintf_insn_snowhousecpu_main(
  snowhousecpu_dasm_info_t* args,
  char* temp_buf, size_t temp_buf_lim
);

//--------
}

int my_snowhousecpu_dasm_rd32_func(u8* buf, size_t offset);

static constexpr size_t SCREENWIDTH = 320u;
static constexpr size_t SCREENHEIGHT = 200u;

class SnowhousecpuEmu final {
public:     // constants
    // 64 MiB of main RAM
    static constexpr size_t MEM_SIZE = (
        64ull * 1024ull * 1024ull
    );
    static constexpr u32 ADDR_PRINT = 0x6000000ul;
    static constexpr u32 ADDR_EXIT = 0x6000004ul;
    static constexpr u32 ADDR_TIMER_USEC_LO = 0x6000000ul;
    static constexpr u32 ADDR_TIMER_USEC_HI = 0x6000004ul;
    static constexpr u32 ADDR_TIMER_SEC_LO = 0x6000008ul;
    static constexpr u32 ADDR_TIMER_SEC_HI = 0x600000cul;
    static constexpr u32 ADDR_FB_START = 0x2000000ul;
    static constexpr u32 ADDR_FB_END = (
        ADDR_FB_START
        + ((SCREENWIDTH * SCREENHEIGHT - 1) * sizeof(u16))
    );

    //static constexpr u32 BUS_ADDR_DOOM_WAD_DBG = 0x2697ce8ull;
    //static constexpr u32 HAVE_DOOM_DBG_WR = 0b01;
    //static constexpr u32 HAVE_DOOM_DBG_RD = 0b10;

    //static constexpr u32 PC_ADDR_DOOM_WAD_MALLOC_DBG = 0x54010ull;

    static constexpr size_t NUM_GPRS = SNOWHOUSECPU_NUM_GPRS;
    static constexpr std::array<const char*, NUM_GPRS> GPR_NAMES_ARR = {
        "r0",
        "r1", "r2", "r3",
        "r4", "r5", "r6", "r7",
        "r8", "r9", "r10", "r11",
        "r12", "lr", "fp", "sp"
    };
    static constexpr size_t NUM_SPRS = SNOWHOUSECPU_NUM_SPRS;
    static constexpr std::array<const char*, NUM_SPRS> SPR_NAMES_ARR = {
        "ids", "ira", "ie",
        "hi", "lo",
    };
public:     // types
    class ExecOneInstrRet final {
    public:     // constants
        static constexpr size_t NUM_GPRS = (
            SnowhousecpuEmu::NUM_GPRS
        );
        static constexpr auto GPR_NAMES_ARR = (
            SnowhousecpuEmu::GPR_NAMES_ARR
        );
        static constexpr size_t NUM_SPRS = (
            SnowhousecpuEmu::NUM_SPRS
        );
        static constexpr auto SPR_NAMES_ARR = (
            SnowhousecpuEmu::SPR_NAMES_ARR
        );
    public:     // variables
        std::optional<u8*> sw_wrote_to_fb_end = std::nullopt;
        bool sw_read_from_tp = false;
        std::array<u32, NUM_GPRS>* gpr_file = nullptr;
        std::array<u32, NUM_SPRS>* spr_file = nullptr;
        u32 saved_pc = 0;
        u32 pc = 0;
        u32 enc_instr = 0;
        #ifdef MELTED_MOON_DO_DISASM
        std::string disasm_str;
        #endif      // DEBUG
    };

private:        // variables
    bool _do_extra_print = false;
    std::string _to_dbg_print;
    std::unique_ptr<u8[]> _mem;
    std::array<u32, SNOWHOUSECPU_NUM_GPRS> _gpr_file;
    std::array<u32, SNOWHOUSECPU_NUM_SPRS> _spr_file;
    snowhousecpu_dasm_info_t _dasm;
    u32 _instr_start_pc = 0u;
    //u32 _pc = 0u;
    //u32 _have_doom_dbg = false;

    timeval* _tp = nullptr;
    //std::optional<u8*> _sw_wrote_to_fb_end = std::nullopt;
    ExecOneInstrRet _my_exec_one_instr_ret;
    bool _do_printing = true;
public:     // functions
    SnowhousecpuEmu() = default;
    SnowhousecpuEmu(
        const char* filename, bool s_do_extra_print=false
    );
    SnowhousecpuEmu(
        SnowhousecpuEmu&& to_move
    ) = default;

    ~SnowhousecpuEmu() = default;

    SnowhousecpuEmu& operator = (
        SnowhousecpuEmu&& to_move
    ) = default;
    
    inline int my_dasm_rd32_func(u8* buf, size_t offset) {
        //memcpy(buf, &rd32_buf_src, sizeof(rd32_buf_src));
        //printout(
        //    "SnowhousecpuEmu::my_rd32_func(): debug: ",
        //    "offset:", offset,
        //    "\n"
        //);
        if (
            offset == 0
            || offset == 4
        ) {
            //fprintf(
            //    stderr,
            //    "offset:%lu pc:%lx\n",
            //    offset,
            //    _pc()
            //);
            if (offset == 0) {
                _instr_start_pc = _pc();
            }
            memcpy(
                buf,
                _mem.get() + _instr_start_pc + offset,
                sizeof(u32)
            );
            _pc() += sizeof(u32);
            return 0;
        }
        //else if (offset == 4) {
        //    memcpy(
        //        buf,
        //        &_mem[offset / sizeof(u32)],
        //        sizeof(u32)
        //    );
        //    //_have_pre = false;
        //    return 0;
        //}
        else {
            return 1;
        }
    }
    ExecOneInstrRet exec_one_instr(
        struct timeval& n_tp,
        bool n_do_printing=true
    );
private:        // functions
    inline decltype(_dasm.curr_pc)& _pc() {
        return _dasm.curr_pc;
    }
    inline u32 _ra() {
        return _gpr_file.at(_dasm.ra_idx);
    }
    inline u32 _rb() {
        return _gpr_file.at(_dasm.rb_idx);
    }
    inline u32 _rc() {
        return _gpr_file.at(_dasm.rc_idx);
    }
    inline u32 _simm24() {
        return _dasm.simm24;
    }
    inline u32& _hi() {
        return _spr_file.at(SNOWHOUSECPU_SPR_ENUM_HI);
    }
    inline u32& _ie() {
        return _spr_file.at(SNOWHOUSECPU_SPR_ENUM_IE);
    }
    inline u32& _ids() {
        return _spr_file.at(SNOWHOUSECPU_SPR_ENUM_IDS);
    }
    inline u32& _ira() {
        return _spr_file.at(SNOWHOUSECPU_SPR_ENUM_IRA);
    }
    void _bus_write(
        u32 data, u32 addr, size_t byte_count
    );
    inline void _bus_write_u32(
        u32 data, u32 addr
    ) {
        _bus_write(data, addr, sizeof(data));
    }
    inline void _bus_write_u16(
        u16 data, u32 addr
    ) {
        _bus_write(data, addr, sizeof(data));
    }
    inline void _bus_write_u8(
        u8 data, u32 addr
    ) {
        _bus_write(data, addr, sizeof(data));
    }

    u32 _bus_read(
        u32 addr, size_t byte_count
    );
    inline u32 _bus_read_u32(
        u32 addr
    ) {
        return _bus_read(addr, sizeof(u32));
    }
    inline u32 _bus_read_u16(
        u32 addr
    ) {
        return u32(u16(_bus_read(addr, sizeof(u16))));
    }
    inline u32 _bus_read_u8(
        u32 addr
    ) {
        return u32(u8(_bus_read(addr, sizeof(u8))));
    }
    inline u32 _bus_read_i16(
        u32 addr
    ) {
        return u32(i32(i16(_bus_read(addr, sizeof(i16)))));
    }
    inline u32 _bus_read_i8(
        u32 addr
    ) {
        return u32(i32(i8(_bus_read(addr, sizeof(i8)))));
    }

    //inline u32 _read_mem
};

extern SnowhousecpuEmu emu;

#endif      // SRC_SNOWHOUSECPU_EMUU_HPP
