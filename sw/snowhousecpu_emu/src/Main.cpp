#include "MiscIncludes.hpp"
#include <sys/time.h>


using namespace liborangepower::misc_output;
using namespace liborangepower::integer_types;

//using snowhousecpu_dasm_info_rd32_func = int (*)(
//    //struct snowhousecpu_dasm_info_t * /* self */
//    u8* buf, size_t offset
//);

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

//static std::unique_ptr<u8[]> main_mem;

//static constexpr size_t MAIN_MEM_SIZE = 128ull * 1024ull * 1024ull; 

static int my_dasm_rd32_func(u8* buf, size_t offset);

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
    static constexpr u32 ADDR_TIMER_USEC = 0x6000000ul;
    static constexpr u32 ADDR_TIMER_SEC = 0x6000004ul;
    static constexpr u32 ADDR_FB_START = 0x2000000ul;
    static constexpr u32 ADDR_FB_END = (
        ADDR_FB_START
        + ((SCREENWIDTH * SCREENHEIGHT - 1) * sizeof(u16))
    );

    //static constexpr u32 BUS_ADDR_DOOM_WAD_DBG = 0x2697ce8ull;
    //static constexpr u32 HAVE_DOOM_DBG_WR = 0b01;
    //static constexpr u32 HAVE_DOOM_DBG_RD = 0b10;

    //static constexpr u32 PC_ADDR_DOOM_WAD_MALLOC_DBG = 0x54010ull;
public:     // variables

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

    std::optional<u8*> _sw_wrote_to_fb_end = std::nullopt;
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
    std::optional<u8*> exec_one_instr();
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
SnowhousecpuEmu::SnowhousecpuEmu(
    const char* filename, bool s_do_extra_print
)
    :
    _do_extra_print(s_do_extra_print),
    _mem(new u8[MEM_SIZE]) {
//--------
    memset(_mem.get(), 0, sizeof(u8) * MEM_SIZE);
    if (
        std::ifstream ifile(
            filename,
            std::ios_base::in | std::ios_base::binary
        );
        ifile.is_open()
    ) {
        std::streamsize i;
        for (i=0; i<std::streamsize(MEM_SIZE) && !ifile.eof(); ++i) {
            _mem[i] = ifile.get();
            //fprintf(
            //    stderr,
            //    "Test: i=%li %x\n",
            //    i, u32(_mem[i])
            //);
            //ifile.seekg(i);
        }
        if (i > std::streamsize(MEM_SIZE)) {
            std::fprintf(
                stderr,
                "SnowhousecpuEmu::SnowhousecpuEmu(): "
                "\"%s\" is too large\n",
                filename
            );
            std::exit(1);
        }
    } else {
        std::fprintf(
            stderr,
            "SnowhousecpuEmu::SnowhousecpuEmu(): "
            "couldn't open \"%s\" for reading!\n",
            filename
        );
        std::exit(1);
    }
    _gpr_file.fill(0x0u);
    _spr_file.fill(0x0u);

    snowhousecpu_dasm_info_ctor(
        &_dasm,
        ::my_dasm_rd32_func,
        false
    );
//--------
}
std::optional<u8*> SnowhousecpuEmu::exec_one_instr() {
    _sw_wrote_to_fb_end = std::nullopt;
    const u32 saved_pc = _dasm.curr_pc;

    snowhousecpu_dasm_info_ctor(
        &_dasm,
        ::my_dasm_rd32_func,
        false
    );
    _dasm.curr_pc = saved_pc;
    snowhousecpu_dasm_info_do_disassemble(&_dasm);
    //_pc() += _dasm.length;
    auto dbg_print = [&]() -> void {
        if (!_do_extra_print) {
            return;
        }
        printf(
            "saved_pc=%x   ", saved_pc
        );
        do {
            std::array<char, 1024> buf;
            buf.fill('\0');
            const u32 other_saved_pc = _dasm.curr_pc;
            _dasm.curr_pc = saved_pc;
            do_snprintf_insn_snowhousecpu_main(
                &_dasm,
                buf.data(), buf.size()
            );
            _dasm.curr_pc = other_saved_pc;
            printf(
                "(%s)    ",
                buf.data()
            );
        } while (0);
        printf(
            "ra_idx=%lu rb_idx=%lu rc_idx=%lu "
            "uimm=%lx simm=%lx simm24=%lx      ",
            _dasm.ra_idx, _dasm.rb_idx, _dasm.rc_idx,
            _dasm.uimm, _dasm.simm, _dasm.simm24
        );

        printf(
            "pc=%lx ", _pc()
        );
        for (size_t i=0; i<_gpr_file.size(); ++i) {
            if (i <= 12) {
                printf(
                    "r%lu=%x",
                    i,
                    _gpr_file.at(i)
                );
            } else if (i == 13) {
                printf(
                    "lr=%x",
                    _gpr_file.at(i)
                );
            } else if (i == 14) {
                printf(
                    "fp=%x",
                    _gpr_file.at(i)
                );
            } else if (i == 15) {
                printf(
                    "sp=%x",
                    _gpr_file.at(i)
                );
            } else {
                std::fprintf(
                    stderr,
                    "GPR idx Eek!\n"
                );
                std::exit(1);
            }
            if (i + 1 < _gpr_file.size()) {
                printf(" ");
            }
        }
        printf("\n");
    };

    const snowhousecpu_opc_info_t* opc_info = _dasm.opc_info;

    const snowhousecpu_temp_t iword = _dasm.iword;
    const snowhousecpu_temp_t uimm = _dasm.uimm;
    const snowhousecpu_temp_t simm = _dasm.simm;
    const snowhousecpu_temp_t simm24 = _dasm.simm24;

    u32 temp_ra = _ra();
    u32 temp_rb = _rb();
    auto have_opc_info = [&opc_info](
        const snowhousecpu_opc_info_t* other 
    ) -> bool {
        if (
            //std::string(opc_info->name) == std::string(other->name)
            (std::strcmp(opc_info->name, other->name) == 0)
            && opc_info->oparg == other->oparg
            && opc_info->op == other->op
            && (
                std::memcmp(
                    &opc_info->subop,
                    &other->subop,
                    sizeof(opc_info->subop)
                ) == 0
            )
        ) {
            return true;
        } else {
            return false;
        }
    };
    if (have_opc_info(&snowhousecpu_opc_info_add_ra_rb_rc)) {
        temp_ra = temp_rb + _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_add_ra_rb_simm16)) {
        temp_ra = temp_rb + simm;
    } else if (have_opc_info(&snowhousecpu_opc_info_cpy_ra_rb)) {
        temp_ra = temp_rb;
    } else if (have_opc_info(&snowhousecpu_opc_info_cpy_ra_simm16)) {
        temp_ra = simm;
    } else if (have_opc_info(&snowhousecpu_opc_info_sub_ra_rb_rc)) {
        temp_ra = temp_rb - _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_sltu_ra_rb_rc)) {
        if (temp_rb < _rc()) {
            temp_ra = 1u;
        } else {
            temp_ra = 0u;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_slts_ra_rb_rc)) {
        if (i32(temp_rb) < i32(_rc())) {
            temp_ra = 1u;
        } else {
            temp_ra = 0u;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_sltu_ra_rb_imm16)) {
        if (temp_rb < u32(uimm)) {
            temp_ra = 1u;
        } else {
            temp_ra = 0u;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_slts_ra_rb_simm16)) {
        if (i32(temp_rb) < i32(simm)) {
            temp_ra = 1u;
        } else {
            temp_ra = 0u;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_xor_ra_rb_rc)) {
        temp_ra = temp_rb ^ _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_xor_ra_rb_simm16)) {
        temp_ra = temp_rb ^ simm;
    } else if (have_opc_info(&snowhousecpu_opc_info_or_ra_rb_rc)) {
        temp_ra = temp_rb | _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_or_ra_rb_imm16)) {
        temp_ra = temp_rb | uimm;
    } else if (have_opc_info(&snowhousecpu_opc_info_and_ra_rb_simm16)) {
        //if (saved_pc == 0x2cb54ull) {
        //    printf(
        //        "saved_pc == "
        //    );
        //    dbg_print();
        //}
        temp_ra = temp_rb & simm;
    } else if (have_opc_info(&snowhousecpu_opc_info_lsl_ra_rb_rc)) {
        if (_rc() >= 32) {
            std::fprintf(
                stderr,
                "Error: Unknown behavior of bit shift (%s, rc:%u): %lu\n",
                "snowhousecpu_opc_info_lsl_ra_rb_rc",
                _rc(),
                iword
            );
            std::exit(1);
        }
        temp_ra = temp_rb << _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_lsl_ra_rb_imm5)) {
        temp_ra = temp_rb << uimm;
    } else if (have_opc_info(&snowhousecpu_opc_info_lsr_ra_rb_rc)) {
        if (_rc() >= 32) {
            std::fprintf(
                stderr,
                "Error: Unknown behavior of bit shift (%s, rc:%u): %lu\n",
                "snowhousecpu_opc_info_lsr_ra_rb_rc",
                _rc(),
                iword
            );
            std::exit(1);
        }
        temp_ra = temp_rb >> _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_lsr_ra_rb_imm5)) {
        temp_ra = temp_rb >> uimm;
    } else if (have_opc_info(&snowhousecpu_opc_info_asr_ra_rb_rc)) {
        if (_rc() >= 32) {
            std::fprintf(
                stderr,
                "Error: Unknown behavior of bit shift (%s, rc:%u): %lu\n",
                "snowhousecpu_opc_info_asr_ra_rb_rc",
                _rc(),
                iword
            );
            std::exit(1);
        }
        temp_ra = i32(temp_rb) >> _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_asr_ra_rb_imm5)) {
        temp_ra = i32(temp_rb) >> uimm;
    } else if (have_opc_info(&snowhousecpu_opc_info_and_ra_rb_rc)) {
        temp_ra = temp_rb & _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_cpy_ids_rb)) {
        _ids() = temp_rb;
        //std::fprintf(
        //    stderr,
        //    "Error: Unimplemented instruction (%s): %lu\n",
        //    "snowhousecpu_opc_info_cpy_ids_rb",
        //    iword
        //);
        //std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_cpy_ra_ira)) {
        temp_ra = _ira();
        //std::fprintf(
        //    stderr,
        //    "Error: Unimplemented instruction (%s): %lu\n",
        //    "snowhousecpu_opc_info_cpy_ra_ira",
        //    iword
        //);
        //std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_cpy_ie_rb)) {
        _ie() = temp_rb;
        //std::fprintf(
        //    stderr,
        //    "Error: Unimplemented instruction (%s): %lu\n",
        //    "snowhousecpu_opc_info_cpy_ie_rb",
        //    iword
        //);
        //std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_ret_ira)) {
        _ie() = 0x1;
        _pc() = _ira();
        //std::fprintf(
        //    stderr,
        //    "Error: Unimplemented instruction (%s): %lu\n",
        //    "snowhousecpu_opc_info_ret_ira",
        //    iword
        //);
        //std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_cpy_ra_hi)) {
        temp_ra = _hi();
    } else if (have_opc_info(&snowhousecpu_opc_info_cpy_hi_rb)) {
        _hi() = temp_rb;
    } else if (have_opc_info(&snowhousecpu_opc_info_umulw_ra_rb_rc)) {
        const u64 full_prod = u64(temp_rb) * u64(_rc());
        _hi() = (full_prod >> 32) & 0xfffffffful;
        temp_ra = full_prod & 0xfffffffful;
    } else if (have_opc_info(&snowhousecpu_opc_info_smulw_ra_rb_rc)) {
        const i64 full_prod = i64(temp_rb) * i64(_rc());
        _hi() = (full_prod >> 32) & 0xfffffffful;
        temp_ra = full_prod & 0xfffffffful;
    } else if (have_opc_info(&snowhousecpu_opc_info_udiv_ra_rb_rc)) {
        temp_ra = temp_rb / _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_sdiv_ra_rb_rc)) {
        temp_ra = u32(i32(i32(temp_rb) / i32(_rc())));
    } else if (have_opc_info(&snowhousecpu_opc_info_umod_ra_rb_rc)) {
        temp_ra = temp_rb % _rc();
    } else if (have_opc_info(&snowhousecpu_opc_info_smod_ra_rb_rc)) {
        temp_ra = u32(i32(i32(temp_rb) % i32(_rc())));
    } else if (have_opc_info(&snowhousecpu_opc_info_udivw_ra_rb_rc)) {
        const u64 left = u64(
            (u64(_hi()) << 32ull) | u64(temp_ra)
        );
        const u64 right = u64(
            (u64(temp_rb) << 32ull) | u64(_rc())
        );
        const u64 quot = left / right;
        _hi() = (quot >> 32) & 0xfffffffful;
        temp_ra = quot & 0xfffffffful;
    } else if (have_opc_info(&snowhousecpu_opc_info_sdivw_ra_rb_rc)) {
        const i64 left = i64(
            (u64(_hi()) << 32ull) | u64(temp_ra)
        );
        const i64 right = i64(
            (u64(temp_rb) << 32ull) | u64(_rc())
        );
        const i64 quot = left / right;
        _hi() = (quot >> 32) & 0xfffffffful;
        temp_ra = quot & 0xfffffffful;
    } else if (have_opc_info(&snowhousecpu_opc_info_ldr_ra_rb_simm16)) {
        temp_ra = _bus_read_u32(temp_rb + simm);
    } else if (have_opc_info(&snowhousecpu_opc_info_str_ra_rb_simm16)) {
        _bus_write_u32(temp_ra, temp_rb + simm);
    } else if (have_opc_info(&snowhousecpu_opc_info_lduh_ra_rb_simm16)) {
        temp_ra = _bus_read_u16(temp_rb + simm);
    } else if (have_opc_info(&snowhousecpu_opc_info_ldsh_ra_rb_simm16)) {
        temp_ra = _bus_read_i16(temp_rb + simm);
    } else if (have_opc_info(&snowhousecpu_opc_info_ldub_ra_rb_simm16)) {
        temp_ra = _bus_read_u8(temp_rb + simm);
    } else if (have_opc_info(&snowhousecpu_opc_info_ldsb_ra_rb_simm16)) {
        temp_ra = _bus_read_i8(temp_rb + simm);
    } else if (have_opc_info(&snowhousecpu_opc_info_sth_ra_rb_simm16)) {
        _bus_write_u16(temp_ra, temp_rb + simm);
    } else if (have_opc_info(&snowhousecpu_opc_info_stb_ra_rb_simm16)) {
        _bus_write_u8(temp_ra, temp_rb + simm);
    } else if (have_opc_info(&snowhousecpu_opc_info_beq_ra_rb_simm16)) {
        if (temp_ra == temp_rb) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_bne_ra_rb_simm16)) {
        if (temp_ra != temp_rb) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_add_ra_pc_simm16)) {
        temp_ra = _pc() + simm;
    } else if (have_opc_info(&snowhousecpu_opc_info_bltu_ra_rb_simm16)) {
        if (temp_ra < temp_rb) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_bgtu_ra_rb_simm16)) {
        if (temp_ra > temp_rb) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_bgeu_ra_rb_simm16)) {
        if (temp_ra >= temp_rb) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_bleu_ra_rb_simm16)) {
        if (temp_ra <= temp_rb) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_blts_ra_rb_simm16)) {
        if (i32(temp_ra) < i32(temp_rb)) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_bgts_ra_rb_simm16)) {
        if (i32(temp_ra) > i32(temp_rb)) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_bges_ra_rb_simm16)) {
        if (i32(temp_ra) >= i32(temp_rb)) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_bles_ra_rb_simm16)) {
        if (i32(temp_ra) <= i32(temp_rb)) {
            _pc() += simm;
        }
    } else if (have_opc_info(&snowhousecpu_opc_info_jl_ra_rb)) {
        temp_ra = _pc();
        _pc() = temp_rb;
    } else if (have_opc_info(&snowhousecpu_opc_info_jmp_rb)) {
        _pc() = temp_rb;
    } else if (have_opc_info(&snowhousecpu_opc_info_bl_ra_simm24)) {
        temp_ra = _pc();
        _pc() += simm24;
    } else if (have_opc_info(&snowhousecpu_opc_info_bl_simm24)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_bl_simm24",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_pre_simm16)) {
        std::fprintf(
            stderr,
            "Error: Standalone `pre` instruction (%s): %lu\n",
            "snowhousecpu_opc_info_pre_simm16",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_llr_ra_rb)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_llr_ra_rb",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_scr_ra_rb_rc)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_scr_ra_rb_rc",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_lluh_ra_rb)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_lluh_ra_rb",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_llsh_ra_rb)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_llsh_ra_rb",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_llub_ra_rb)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_llub_ra_rb",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_llsb_ra_rb)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_llsb_ra_rb",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_sch_ra_rb_rc)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_sch_ra_rb_rc",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_scb_ra_rb_rc)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_scb_ra_rb_rc",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_llp_ra_rb)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_llp_ra_rb",
            iword
        );
        std::exit(1);
    } else if (have_opc_info(&snowhousecpu_opc_info_scp_ra_rb_rc)) {
        std::fprintf(
            stderr,
            "Error: Unimplemented instruction (%s): %lu\n",
            "snowhousecpu_opc_info_scp_ra_rb_rc",
            iword
        );
        std::exit(1);
    } else {
        std::fprintf(
            stderr,
            "Error: Invalid instruction found: %x %lu\n",
            _instr_start_pc, iword
        );
        std::exit(1);
    }
    //if (
    //    //_have_doom_dbg
    //    //|| saved_pc == PC_ADDR_DOOM_WAD_MALLOC_DBG
    //    true
    //) {
    //    bool need_extra_newline = false;
    //    if (_have_doom_dbg) {
    //        printf(
    //            "NOTE: _have_doom_dbg: wr:%x rd:%x\n",
    //            u32(bool(_have_doom_dbg & HAVE_DOOM_DBG_WR)),
    //            u32(bool(_have_doom_dbg & HAVE_DOOM_DBG_RD))
    //        );
    //        need_extra_newline = true;
    //    } else if (saved_pc == PC_ADDR_DOOM_WAD_MALLOC_DBG) {
    //        printf(
    //            "NOTE: found saved_pc=%x\n",
    //            saved_pc
    //        );
    //        need_extra_newline = true;
    //    }
    //    dbg_print();
    //    if (need_extra_newline) {
    //        printf(
    //            "\n"
    //        );
    //    }
    //}

    if (_dasm.ra_idx != 0) {
        //printf(
        //    "before: writing rA: r%lu: %x %x\n",
        //    _dasm.ra_idx,
        //    temp_ra,
        //    _gpr_file.at(_dasm.ra_idx)
        //);
        //if (
        //    _dasm.ra_idx == 15
        //    && temp_ra > 0x2000000u //== (0x45d0fe4 - 12)
        //) {
        //    std::printf(
        //        "debug: setting sp: "
        //        "_instr_start_pc=%x old_sp=%x new_sp=%x\n",
        //        _instr_start_pc,
        //        _gpr_file.at(_dasm.ra_idx),
        //        temp_ra
        //    );
        //    std::exit(1);
        //}
        _gpr_file.at(_dasm.ra_idx) = temp_ra;
        //printf(
        //    "after: writing rA: r%lu: %x %x\n",
        //    _dasm.ra_idx,
        //    temp_ra,
        //    _gpr_file.at(_dasm.ra_idx)
        //);
    }
    //if (_dasm.rb_idx != 0) {
    //    _gpr_file.at(_dasm.rb_idx) = temp_rb;
    //}
    dbg_print();
    return _sw_wrote_to_fb_end;
}

void SnowhousecpuEmu::_bus_write(
    u32 data, u32 addr, size_t byte_count
) {
    //_have_doom_dbg = (
    //    (addr == BUS_ADDR_DOOM_WAD_DBG)
    //    ? HAVE_DOOM_DBG_WR
    //    : 0u
    //);
    if (_do_extra_print) {
        std::printf(
            "_bus_write(): data:%x addr:%x byte_count:%lu\n",
            data, addr, byte_count
        );
    }
    if (
        byte_count == sizeof(u8)
        || byte_count == sizeof(u16)
        || byte_count == sizeof(u32)
    ) {
        if (addr == ADDR_PRINT) {
            std::array<u8, sizeof(u32)> buf_u8;
            memcpy(buf_u8.data(), &data, sizeof(u32));

            for (size_t i=0; i<byte_count; ++i) {
                const char to_add = char(buf_u8.at(i));
                if (to_add != '\n') {
                    _to_dbg_print += to_add;
                } else {
                    //printout(
                    //    _to_dbg_print,
                    //    "\n"
                    //);
                    std::printf(
                        "%s\n",
                        _to_dbg_print.c_str()
                    );
                    //if (
                    //    _to_dbg_print
                    //    == (
                    //        "Error: R_InitTextures: "
                    //        "Missing patch in texture COMP2"
                    //    )
                    //) {
                    //    std::exit(1);
                    //}
                    //else if (
                    //    _to_dbg_print
                    //    == (
                    //        "R_Init: Init DOOM refresh daemon - "
                    //        "nummappatches:350 "
                    //        "patchlookup_addr=268DEEC "
                    //        "patchlookup_size:1400"
                    //    )
                    //) {
                    //    for (size_t i=0x268DEEC
                    //}
                    _to_dbg_print = "";
                }
            }
        } else if (addr == ADDR_EXIT) {
            std::exit(data);
        } else if (addr > MEM_SIZE) {
            std::fprintf(
                stderr,
                "SnowhousecpuEmu::_bus_write(): "
                "invalid bus write: "
                "data:%x addr:%x byte_count:%lu\n",
                data, addr, byte_count
            );
            std::exit(1);
        } else {
            if (
                addr == ADDR_FB_END
                && byte_count == sizeof(u16)
            ) {
                _sw_wrote_to_fb_end = &_mem[ADDR_FB_START];
            }
            memcpy(&_mem[addr], &data, byte_count);
        }
    } else {
        std::fprintf(
            stderr,
            "SnowhousecpuEmu::_bus_write(): "
            "Invalid `byte_count`! (debug note: %lu)\n",
            byte_count
        );
        std::exit(1);
    }
}
u32 SnowhousecpuEmu::_bus_read(
    u32 addr, size_t byte_count
) {
    //_have_doom_dbg = (
    //    (addr == BUS_ADDR_DOOM_WAD_DBG)
    //    ? HAVE_DOOM_DBG_WR
    //    : 0u
    //);
    u32 ret = 0; 
    if (
        byte_count == sizeof(u8)
        || byte_count == sizeof(u16)
        || byte_count == sizeof(u32)
    ) {
        if (addr > MEM_SIZE) {
            if (addr == ADDR_TIMER_USEC) {
                //struct timeval tp;
                //gettimeofday(&tp, NULL);
                //////*sec = tp.tv_sec;
                //////*usec = tp.tv_usec;
                //memcpy(&ret, &tp.tv_usec, byte_count);
                ////static u32 temp = 0;
                //////temp += 1000u;
                ////memcpy(&ret, &temp, byte_count); 
                ////temp++;
                //////temp += 1000;

                //static bool did_first_check = false;
                //static struct timeval first_tp;

                //if (!did_first_check) {
                //    did_first_check = true;
                //    gettimeofday(&first_tp, NULL);
                //    memset(&ret, 0, byte_count);
                //} else {
                    struct timeval tp;
                    gettimeofday(&tp, NULL);

                    //*sec = tp.tv_sec;
                    //*usec = tp.tv_usec;
                    //tp.tv_usec -= first_tp.tv_usec;
                    memcpy(&ret, &tp.tv_usec, byte_count);
                //}
            } else if (addr == ADDR_TIMER_SEC) {
                //static bool did_first_check = false;
                //static struct timeval first_tp;

                //if (!did_first_check) {
                //    did_first_check = true;
                //    gettimeofday(&first_tp, NULL);
                //    memset(&ret, 0, byte_count);
                //} else {
                    struct timeval tp;
                    gettimeofday(&tp, NULL);

                    //*sec = tp.tv_sec;
                    //*usec = tp.tv_sec;
                    //tp.tv_sec -= first_tp.tv_sec;
                    memcpy(&ret, &tp.tv_sec, byte_count);
                   // ret = ret >> 1;
                //}

                //static u32 temp = 0;
                ////u32 temp_cnt = temp / 8u;
                //u32 temp_cnt = temp;
                //memcpy(&ret, &temp_cnt, byte_count); 
                //temp++;
            } else {
                std::fprintf(
                    stderr,
                    "SnowhousecpuEmu::_bus_read(): "
                    "invalid bus read: "
                    "addr:%x byte_count:%lu\n",
                    addr, byte_count
                );
                std::exit(1);
            }
        } else {
            memcpy(&ret, &_mem[addr], byte_count);
        }
    } else {
        std::fprintf(
            stderr,
            "SnowhousecpuEmu::_bus_read(): "
            "Invalid `byte_count`! (debug note: %lu)\n",
            byte_count
        );
        std::exit(1);
    }
    if (_do_extra_print) {
        std::printf(
            "_bus_read(): data:%x addr:%x byte_count:%lu\n",
            ret, addr, byte_count
        );
    }
    return ret;
}

static SnowhousecpuEmu emu;

static int my_dasm_rd32_func(u8* buf, size_t offset) {
    return emu.my_dasm_rd32_func(buf, offset);
}

int main(int argc, char** argv) {
    if (argc == 2) {
        emu = SnowhousecpuEmu(argv[1]);
    } else if (argc == 3) {
        emu = SnowhousecpuEmu(argv[1], std::atoi(argv[2]));
    } else {
        std::fprintf(
            stderr,
            "Usage 0: %s <program_filename:string>\n"
            "Usage 1: %s "
                "<program_filename:string> <do_extra_print:int_to_bool>\n",
            argv[0],
            argv[0]
        );
        std::exit(1);
    }

    SDL_Window* window = SDL_CreateWindow(
        "Melted Moon - Somewhat Of A Simulator!",   // title
        SDL_WINDOWPOS_CENTERED, // x
        SDL_WINDOWPOS_CENTERED, // y
        SCREENWIDTH * 2,            // WIDTH
        SCREENHEIGHT * 2,           // HEIGHT
                                // flags
        (
            SDL_WINDOW_SHOWN
            //| SDL_WINDOW_RESIZABLE
        )
    );
    SDL_Renderer* renderer = SDL_CreateRenderer(
        window, // window
        -1,     // index
        0       // flags
    );
    SDL_Texture* texture = SDL_CreateTexture(
        renderer,
        SDL_PIXELFORMAT_ARGB8888,
        SDL_TEXTUREACCESS_STATIC,
        SCREENWIDTH * 2,
        SCREENHEIGHT * 2
    );

    std::unique_ptr<Uint32[]> pixels(
        new Uint32[SCREENWIDTH * 2 * SCREENHEIGHT * 2]
    );

    for (;;) {
        //u16 temp_fb_data;
        //u32 temp_fb_addr;
        if (auto fb_start = emu.exec_one_instr(); fb_start) {
            printout(
                "testificate!\n"
            );
            //for (
            //    u16* item=(u16*)(*fb_start);
            //    item<(
            //        ((u16*)(*fb_start))
            //        + SCREENWIDTH * SCREENHEIGHT
            //    );
            //    ++item
            //) {
                for (size_t j=0; j<SCREENHEIGHT * 2; ++j) {
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
                        u16* item = (u16*)(*fb_start) + k;
                        const u32 r = (
                            ((((*item) >> 0) & 0x1f) << 3) | 0x7
                        );
                        const u32 g = (
                            ((((*item) >> 5) & 0x1f) << 3) | 0x7
                        );
                        const u32 b = (
                            ((((*item) >> 10) & 0x1f) << 3) | 0x7
                        );
                        pixels[l] = (
                            ((r & 0xffu) << 16u)
                            | ((g & 0xffu) << 8u)
                            | ((b & 0xffu) << 0u)
                        );
                    }
                }
            //}

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
                sizeof(Uint32) * SCREENWIDTH * 2 * SCREENHEIGHT *2
            );
        }
    }


    SDL_DestroyTexture(texture);
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);

    return 0;
}
