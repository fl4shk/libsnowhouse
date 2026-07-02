#ifndef src_melted_moon_debug_riscv_emu_hpp
#define src_melted_moon_debug_riscv_emu_hpp

#include "MiscIncludes.hpp"

using namespace liborangepower::misc_output;
using namespace liborangepower::integer_types;

static constexpr size_t SCREENWIDTH = 320u;
static constexpr size_t SCREENHEIGHT = 200u;

using Field = std::pair<size_t, size_t>;
static consteval inline size_t field_width(
    const Field& field
) {
    if (field.second < field.first) {
        return field.first - field.second + size_t(1u);
    } else {
        return field.second - field.first + size_t(1u);
    }
}

static constexpr inline u64 sign_extend(u64 val, size_t width) {
    if (width <= 64u) {
        if (val & u64(1u << (width - 1u))) {
            const u64 MASK = u64(i64(-1) << width);
            //std::printf(
            //    "sign_extend: NOTE: %llx = %llx & %llx\n",
            //    (unsigned long long)val,
            //    (unsigned long long)(val & MASK),
            //    (unsigned long long)MASK
            //);
            val |= MASK;
        }
    }
    return val;
}
static constexpr inline u64 zero_extend(u64 val, size_t width) {
    if (width <= 64u) {
        const u64 MASK = ~(u64(i64(-1)) << width);
        //std::printf(
        //    "zero_extend: NOTE: %llx = %llx & %llx\n",
        //    (unsigned long long)val,
        //    (unsigned long long)(val & MASK),
        //    (unsigned long long)MASK
        //);
        val &= MASK;
    }
    return val;
}

class MeltedMoonDebugRiscvEmu final {
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
    static constexpr u32 ADDR_UDIV64_INP_LEFT_LO = 0x6000010ul;
    static constexpr u32 ADDR_UDIV64_INP_LEFT_HI = 0x6000014ul;
    static constexpr u32 ADDR_UDIV64_INP_RIGHT_LO = 0x6000018ul;
    static constexpr u32 ADDR_UDIV64_INP_RIGHT_HI = 0x600001cul;
    static constexpr u32 ADDR_UDIV64_OUTP_QUOT_LO = 0x6000010ul;
    static constexpr u32 ADDR_UDIV64_OUTP_QUOT_HI = 0x6000014ul;
    static constexpr u32 ADDR_UDIV64_OUTP_REMA_LO = 0x6000018ul;
    static constexpr u32 ADDR_UDIV64_OUTP_REMA_HI = 0x600001cul;

    static constexpr u32 ADDR_IDIV64_INP_LEFT_LO = 0x6000020ul;
    static constexpr u32 ADDR_IDIV64_INP_LEFT_HI = 0x6000024ul;
    static constexpr u32 ADDR_IDIV64_INP_RIGHT_LO = 0x6000028ul;
    static constexpr u32 ADDR_IDIV64_INP_RIGHT_HI = 0x600002cul;
    static constexpr u32 ADDR_IDIV64_OUTP_QUOT_LO = 0x6000020ul;
    static constexpr u32 ADDR_IDIV64_OUTP_QUOT_HI = 0x6000024ul;
    static constexpr u32 ADDR_IDIV64_OUTP_REMA_LO = 0x6000028ul;
    static constexpr u32 ADDR_IDIV64_OUTP_REMA_HI = 0x600002cul;

    static constexpr u32 ADDR_FB_START = 0x2000000ul;
    static constexpr u32 ADDR_FB_END = (
        ADDR_FB_START
        + ((SCREENWIDTH * SCREENHEIGHT - 1) * sizeof(u16))
    );
    static constexpr size_t NUM_GPRS = 32u;
    static constexpr std::array<const char*, NUM_GPRS> GPR_NAMES_ARR = {
        "zero",                 // x0,
        "ra", "sp", "gp", "tp", // x1-x4
        "t0", "t1", "t2",       // x5-x7: temporary registers
        "s0", "s1",             // x8, x9: saved registers (s0 can be fp!)
        "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
                                // x10-x17: arguments
        "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11",
                                // x18-x27: saved registers
        "t3", "t4", "t5", "t6", // x28-x31: temporary registers
    };

    //static constexpr u32 BUS_ADDR_DOOM_WAD_DBG = 0x2697ce8ull;
    //static constexpr u32 HAVE_DOOM_DBG_WR = 0b01;
    //static constexpr u32 HAVE_DOOM_DBG_RD = 0b10;

    //static constexpr u32 PC_ADDR_DOOM_WAD_MALLOC_DBG = 0x54010ull;
public:     // types
    class ExecOneInstrRet final {
    public:     // constants
        static constexpr size_t NUM_GPRS = (
            MeltedMoonDebugRiscvEmu::NUM_GPRS
        );
        static constexpr auto GPR_NAMES_ARR = (
            MeltedMoonDebugRiscvEmu::GPR_NAMES_ARR
        );
    public:     // variables
        std::optional<u8*> sw_wrote_to_fb_end = std::nullopt;
        bool sw_read_from_tp = false;
        std::array<u32, NUM_GPRS>* gpr_file = nullptr;
        u32 saved_pc = 0;
        u32 pc = 0;
        u32 enc_instr = 0;
        #ifdef MELTED_MOON_DO_DISASM
        std::string disasm_str;
        #endif      // DEBUG
    };
    class Rv32RType final {
    public:     // types
        static constexpr Field field_opcode = {6u, 0u};
        static constexpr Field field_rd = {11u, 7u};
        static constexpr Field field_funct3 = {14u, 12u};
        static constexpr Field field_rs1 = {19, 15};
        static constexpr Field field_rs2 = {24, 20};
        static constexpr Field field_funct7 = {31, 25};

        class EncInstr final {
        public:     // variables
            u32 opcode: field_width(field_opcode);
            u32 rd: field_width(field_rd);
            u32 funct3: field_width(field_funct3);
            u32 rs1: field_width(field_rs1);
            u32 rs2: field_width(field_rs2);
            u32 funct7: field_width(field_funct7);
        };

        class OpFields final {
        public:     // variables
            u32 op;
            u32 f3;
            u32 f7;
        };

        class Op final {
        public:     // constants
            //--------
            static constexpr OpFields
                AddRdRs1Rs2 = {.op=0x33, .f3=0x0, .f7=0x00},
                SubRdRs1Rs2 = {.op=0x33, .f3=0x0, .f7=0x20},
                XorRdRs1Rs2 = {.op=0x33, .f3=0x4, .f7=0x00},
                OrRdRs1Rs2 = {.op=0x33, .f3=0x6, .f7=0x00},
                AndRdRs1Rs2 = {.op=0x33, .f3=0x7, .f7=0x00},
                SllRdRs1Rs2 = {.op=0x33, .f3=0x1, .f7=0x0},
                SrlRdRs1Rs2 = {.op=0x33, .f3=0x5, .f7=0x00},
                SraRdRs1Rs2 = {.op=0x33, .f3=0x5, .f7=0x20},
                SltRdRs1Rs2 = {.op=0x33, .f3=0x2, .f7=0x00},
                SltuRdRs1Rs2 = {.op=0x33, .f3=0x3, .f7=0x00},
                //------
                MulRdRs1Rs2 = {.op=0x33, .f3=0x0, .f7=0x01},
                MulhRdRs1Rs2 = {.op=0x33, .f3=0x1, .f7=0x01},
                MulhsuRdRs1Rs2 = {.op=0x33, .f3=0x2, .f7=0x01},
                MulhuRdRs1Rs2 = {.op=0x33, .f3=0x3, .f7=0x01},
                DivRdRs1Rs2 = {.op=0x33, .f3=0x4, .f7=0x01},
                DivuRdRs1Rs2 = {.op=0x33, .f3=0x5, .f7=0x01},
                RemRdRs1Rs2 = {.op=0x33, .f3=0x6, .f7=0x01},
                RemuRdRs1Rs2 = {.op=0x33, .f3=0x7, .f7=0x01};
            //--------
        };
    };

    class Rv32IType final {
    public:     // types
        static constexpr Field field_opcode = {6, 0};
        static constexpr Field field_rd = {11, 7};
        static constexpr Field field_funct3 = {14, 12};
        static constexpr Field field_rs1 = {19, 15};
        static constexpr Field field_imm11dt0 = {31, 20};

        //val opcode = 0x13

        class EncInstr final {
        public:     // variables
            u32 opcode: field_width(field_opcode);
            u32 rd: field_width(field_rd);
            u32 funct3: field_width(field_funct3);
            u32 rs1: field_width(field_rs1);
            u32 imm11dt0: field_width(field_imm11dt0);

            inline i32 my_temp_imm() const {
                //return i32(imm11dt0);
                return i32(sign_extend(
                    u32(imm11dt0), 12u
                ));
            }

            inline i32 my_imm11dt5() const {
                //return i32(i32(imm11dt0) >> 5u);
                return i32(zero_extend(
                    u64(imm11dt0) >> 5u,
                    field_width(Field(11u, 5u))
                ));
            }
            inline i32 my_imm4dt0() const {
                return i32(zero_extend(imm11dt0, 5u));
            }

            //def myTempImm(
            //): SInt = (
            //imm11dt0.asSInt.resize(Riscv32Op.mainWidth)
            //)

            //def myImm11dt5(
            //): UInt = (
            //imm11dt0(11 downto 5)
            //)
            //def myImm4dt0(
            //): UInt = (
            //imm11dt0(4 downto 0).resize(Riscv32Op.mainWidth)
            //)
        };

        class OpFields final {
        public:  // variables
            u32 op;
            u32 f3;
            //std::optional<u32> imm11dt5 = std::nullopt;
            i32 imm11dt5;
        };

        class Op final {
        public:     // constants
            //--------
            static constexpr OpFields
                AddiRdRs1Imm = {.op=0x13, .f3=0x0, .imm11dt5=-1},
                XoriRdRs1Imm = {.op=0x13, .f3=0x4, .imm11dt5=-1},
                OriRdRs1Imm = {.op=0x13, .f3=0x6, .imm11dt5=-1},
                AndiRdRs1Imm = {.op=0x13, .f3=0x7, .imm11dt5=-1},

                // rd = rs1 << imm[0:4]
                SlliRdRs1Imm = {.op=0x13, .f3=0x1, .imm11dt5=0x00},

                // rd = rs1 >> imm[0:4]
                SrliRdRs1Imm = {.op=0x13, .f3=0x5, .imm11dt5=0x00},

                // rd = rs1 >> imm[0:4] msb-extends
                SraiRdRs1Imm = {.op=0x13, .f3=0x5, .imm11dt5=0x20},

                SltiRdRs1Imm = {.op=0x13, .f3=0x2, .imm11dt5=-1},
                SltiuRdRs1Imm = {.op=0x13, .f3=0x3, .imm11dt5=-1},
                //--------
                LbRdRs1Imm = {.op=0x03, .f3=0x0, .imm11dt5=-1},
                LhRdRs1Imm = {.op=0x03, .f3=0x1, .imm11dt5=-1},
                LwRdRs1Imm = {.op=0x03, .f3=0x2, .imm11dt5=-1},
                LbuRdRs1Imm = {.op=0x03, .f3=0x4, .imm11dt5=-1},
                LhuRdRs1Imm = {.op=0x03, .f3=0x5, .imm11dt5=-1},
                //--------
                // rd = PC+4; PC = rs1 + imm
                JalrRdRs1Imm = {.op=0x67, .f3=0x0, .imm11dt5=-1};
            //--------
        };
    };

    class Rv32SType final {
    public:     // variables
        static constexpr Field field_opcode = {6, 0};
        static constexpr Field field_imm4dt0 = {11, 7};
        static constexpr Field field_funct3 = {14, 12};
        static constexpr Field field_rs1 = {19, 15};
        static constexpr Field field_rs2 = {24, 20};
        static constexpr Field field_imm11dt5 = {31, 25};

        class EncInstr final {
        public:     // variables
            u32 opcode: field_width(field_opcode);
            u32 imm4dt0: field_width(field_imm4dt0);
            u32 funct3: field_width(field_funct3);
            u32 rs1: field_width(field_rs1);
            u32 rs2: field_width(field_rs2);
            u32 imm11dt5: field_width(field_imm11dt5);

            inline i32 my_temp_imm() const {
                return (
                    sign_extend(
                        i32((u32(imm11dt5) << 5u) | u32(imm4dt0)),
                        12u
                    )
                );
            }

            //def myTempImm(
            //): SInt = (
            //    Cat(
            //        imm11dt5,
            //        imm4dt0,
            //    ).asSInt.resize(Riscv32Op.mainWidth)
            //)
        };

        class OpFields final {
        public:     // variables
            u32 op;
            u32 f3;
        };

        class Op final {
        public:     // constants
            static constexpr OpFields
                SbRs2Rs1Imm = {.op=0x23, .f3=0x0},
                ShRs2Rs1Imm = {.op=0x23, .f3=0x1},
                SwRs2Rs1Imm = {.op=0x23, .f3=0x2};
        };
    };

    class Rv32BType final {
    public:     // variables
        static constexpr Field field_opcode = {6, 0};
        static constexpr Field field_imm11dt11 = {7, 7};
        static constexpr Field field_imm4dt1 = {11, 8};
        static constexpr Field field_funct3 = {14, 12};
        static constexpr Field field_rs1 = {19, 15};
        static constexpr Field field_rs2 = {24, 20};
        static constexpr Field field_imm10dt5 = {30, 25};
        static constexpr Field field_imm12dt12 = {31, 31};

        class EncInstr final {
        public:     // varaibles
            u32 opcode: field_width(field_opcode);
            u32 imm11dt11: field_width(field_imm11dt11);
            u32 imm4dt1: field_width(field_imm4dt1);
            u32 funct3: field_width(field_funct3);
            u32 rs1: field_width(field_rs1);
            u32 rs2: field_width(field_rs2);
            u32 imm10dt5: field_width(field_imm10dt5);
            u32 imm12dt12: field_width(field_imm12dt12);

            inline i32 my_temp_imm() const {
                const u32 temp = (
                    (u32(imm12dt12) << 12u)
                    | (u32(imm11dt11) << 11u)
                    | (u32(imm10dt5) << 5u)
                    | (u32(imm4dt1) << 1u)
                );
                return sign_extend(temp, 13u);
            }

            //def myTempImm(
            //): SInt = (
            //    Cat(
            //        imm12dt12,
            //        imm11dt11,
            //        imm10dt5,
            //        imm4dt1,
            //        U"1'd0",
            //    ).asSInt.resize(Riscv32Op.mainWidth)
            //)
        };

        class OpFields final {
        public:     // variables
            u32 op;
            u32 f3;
        };

        class Op final {
        public:     // variables
            static constexpr OpFields
                BeqRs1Rs2Imm = {.op=0x63, .f3=0x0},
                BneRs1Rs2Imm = {.op=0x63, .f3=0x1},
                BltRs1Rs2Imm = {.op=0x63, .f3=0x4},
                BgeRs1Rs2Imm = {.op=0x63, .f3=0x5},
                BltuRs1Rs2Imm = {.op=0x63, .f3=0x6},
                BgeuRs1Rs2Imm = {.op=0x63, .f3=0x7};
        };
    };

    class Rv32JType final {
    public:     // variables
        static constexpr Field field_opcode = {6, 0};
        static constexpr Field field_rd = {11, 7};
        static constexpr Field field_imm19dt12 = {19, 12};
        static constexpr Field field_imm11dt11 = {20, 20};
        static constexpr Field field_imm10dt1 = {30, 21};
        static constexpr Field field_imm20dt20 = {31, 31};

        class EncInstr final {
        public:     // variables
            u32 opcode: field_width(field_opcode);
            u32 rd: field_width(field_rd);
            u32 imm19dt12: field_width(field_imm19dt12);
            u32 imm11dt11: field_width(field_imm11dt11);
            u32 imm10dt1: field_width(field_imm10dt1);
            u32 imm20dt20: field_width(field_imm20dt20);

            inline i32 my_temp_imm() const {
                const u32 temp = (
                    (u32(imm20dt20) << 20u)
                    | (u32(imm19dt12) << 12u)
                    | (u32(imm11dt11) << 11u)
                    | (u32(imm10dt1) << 1u)
                );
                const i32 ret = i32(sign_extend(temp, 21u));
                //u32 iword = 0;
                //std::memcpy(&iword, this, sizeof(iword));

                //std::printf(
                //    "Rv32JType::EncInstr::my_temp_imm(): "
                //    "%x %x; %x\n",
                //    u32(temp),
                //    u32(ret),
                //    u32(iword)
                //);
                return ret;
            }

            //def myTempImm(
            //): SInt = (
            //    Cat(
            //        imm20dt20,
            //        imm19dt12,
            //        imm11dt11,
            //        imm10dt1,
            //        U"1'd0",
            //    ).asSInt.resize(Riscv32Op.mainWidth)
            //)
        };

        class OpFields final {
        public:     // variables
            u32 op;
        };

        class Op final {
        public: // variables
            // rd = PC+4; PC += imm
            static constexpr OpFields
                JalRdImm = {.op=0x6f};
        };
    };

    class Rv32UType final {
    public:     // variables
        static constexpr Field field_opcode = {6, 0};
        static constexpr Field field_rd = {11, 7};
        static constexpr Field field_imm = {31, 12};

        class EncInstr final {
        public:     // variables
            u32 opcode: field_width(field_opcode);
            u32 rd: field_width(field_rd);
            u32 imm: field_width(field_imm);

            inline i32 my_temp_imm() const {
                const u32 temp = (
                    u32(imm) << 12u
                );
                //return sign_extend(temp, 32u);
                return temp;
            }

            //def myTempImm(
            //): SInt = (
            //    Cat(
            //        imm,
            //        U"12'd0",
            //    ).asSInt.resize(Riscv32Op.mainWidth)
            //)
        };

        class OpFields final {
        public:     // variables
            u32 op;
        };

        class Op final {
        public:     // variables
            static constexpr OpFields
                LuiRdImm31Downto12 = {.op=0x37},
                AuipcRdImm31Downto12 = {.op=0x17};
        };
    };

private:        // variables
    u32 _do_extra_print_start_pc = 0;
    bool _seen_do_extra_print_start_pc = false;
    bool _seen_final_start_print_cond = false;
    std::string _to_dbg_print;
    std::unique_ptr<u8[]> _mem;
    std::array<u32, NUM_GPRS> _gpr_file;
    //snowhousecpu_dasm_info_t _dasm;
    u32 _instr_start_pc = 0u;
    u32 _pc = 0u;
    u64 _mmio_udiv64_inp_left = 0x0ul;
    u64 _mmio_udiv64_inp_right = 0x0ul;
    u64 _mmio_udiv64_outp_quot = 0x0ul;
    u64 _mmio_udiv64_outp_rema = 0x0ul;
    u64 _mmio_idiv64_inp_left = 0x0ul;
    u64 _mmio_idiv64_inp_right = 0x0ul;
    u64 _mmio_idiv64_outp_quot = 0x0ul;
    u64 _mmio_idiv64_outp_rema = 0x0ul;

    //u32 _have_doom_dbg = false;

    //u32 _dasm_opcode = 0;
    //u32 _dasm_rd_idx = 0;
    //u32 _dasm_rs1_idx = 0;
    //u32 _dasm_rs2_idx = 0;
    //u32 _dasm_funct3 = 0;
    //u32 _dasm_funct7 = 0;
    Rv32RType::EncInstr _enc_instr_r;
    Rv32IType::EncInstr _enc_instr_i;
    Rv32SType::EncInstr _enc_instr_s;
    Rv32BType::EncInstr _enc_instr_b;
    Rv32JType::EncInstr _enc_instr_j;
    Rv32UType::EncInstr _enc_instr_u;

    //std::optional<u8*> _sw_wrote_to_fb_end = std::nullopt;
    timeval* _tp = nullptr;
    //bool _sw_read_from_tp = false;
    ExecOneInstrRet _my_exec_one_instr_ret;
public:     // functions
    MeltedMoonDebugRiscvEmu() = default;
    MeltedMoonDebugRiscvEmu(
        const char* filename, u32 s_do_extra_print_start_pc=false
    );
    MeltedMoonDebugRiscvEmu(
        MeltedMoonDebugRiscvEmu&& to_move
    ) = default;

    ~MeltedMoonDebugRiscvEmu() = default;

    MeltedMoonDebugRiscvEmu& operator = (
        MeltedMoonDebugRiscvEmu&& to_move
    ) = default;
    
    //inline int my_dasm_rd32_func(u8* buf, size_t offset) {
    //    //memcpy(buf, &rd32_buf_src, sizeof(rd32_buf_src));
    //    //printout(
    //    //    "MeltedMoonDebugRiscvEmu::my_rd32_func(): debug: ",
    //    //    "offset:", offset,
    //    //    "\n"
    //    //);
    //    if (
    //        offset == 0
    //        || offset == 4
    //    ) {
    //        //fprintf(
    //        //    stderr,
    //        //    "offset:%lu pc:%lx\n",
    //        //    offset,
    //        //    _pc()
    //        //);
    //        if (offset == 0) {
    //            _instr_start_pc = _pc();
    //        }
    //        memcpy(
    //            buf,
    //            _mem.get() + _instr_start_pc + offset,
    //            sizeof(u32)
    //        );
    //        _pc() += sizeof(u32);
    //        return 0;
    //    } 
    //    //else if (offset == 4) {
    //    //    memcpy(
    //    //        buf,
    //    //        &_mem[offset / sizeof(u32)],
    //    //        sizeof(u32)
    //    //    );
    //    //    //_have_pre = false;
    //    //    return 0;
    //    //}
    //    else {
    //        return 1;
    //    }
    //}
    static std::optional<std::string> disasm_one_instr(
        u32 some_enc_instr,
        u32 some_saved_pc
    );
    ExecOneInstrRet exec_one_instr(struct timeval& n_tp);
private:        // functions
    inline u32 _rd() {
        return _gpr_file.at(_enc_instr_r.rd);
    }
    inline u32 _rs1() {
        return _gpr_file.at(_enc_instr_r.rs1);
    }
    inline u32 _rs2() {
        return _gpr_file.at(_enc_instr_r.rs2);
    }

    //inline decltype(_dasm.curr_pc)& _pc() {
    //    return _dasm.curr_pc;
    //}
    //inline u32 _ra() {
    //    return _gpr_file.at(_dasm_ra_idx);
    //}
    //inline u32 _rb() {
    //    return _gpr_file.at(_dasm_rb_idx);
    //}
    //inline u32 _rc() {
    //    return _gpr_file.at(_dasm_rc_idx);
    //}
    //inline u32 _simm24() {
    //    return _dasm.simm24;
    //}
    //inline u32& _hi() {
    //    return _spr_file.at(SNOWHOUSECPU_SPR_ENUM_HI);
    //}
    //inline u32& _ie() {
    //    return _spr_file.at(SNOWHOUSECPU_SPR_ENUM_IE);
    //}
    //inline u32& _ids() {
    //    return _spr_file.at(SNOWHOUSECPU_SPR_ENUM_IDS);
    //}
    //inline u32& _ira() {
    //    return _spr_file.at(SNOWHOUSECPU_SPR_ENUM_IRA);
    //}
    void _bus_write(
        u32 data, u32 addr, size_t byte_count
    );
    inline void _bus_write_u32(
        u32 data, u32 addr
    ) {
        _bus_write(data, addr & ~0b11u, sizeof(data));
    }
    inline void _bus_write_u16(
        u16 data, u32 addr
    ) {
        _bus_write(data, addr & ~0b1u, sizeof(data));
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
        return _bus_read(addr & ~0b11u, sizeof(u32));
    }
    inline u32 _bus_read_u16(
        u32 addr
    ) {
        //return u32(u16(_bus_read(addr, sizeof(u16))));
        //return u32(zero_extend(_bus_read(addr, sizeof(u16)), 16u));
        return u32(
            zero_extend(
                _bus_read(addr & ~0b1u, sizeof(u16)),
            16u)
        );
    }
    inline u32 _bus_read_u8(
        u32 addr
    ) {
        //return u32(u8(_bus_read(addr, sizeof(u8))));
        return u32(zero_extend(_bus_read(addr, sizeof(u8)), 8u));
    }
    inline u32 _bus_read_i16(
        u32 addr
    ) {
        //return u32(i32(i16(_bus_read(addr, sizeof(i16)))));
        return u32(
            sign_extend(
                _bus_read(addr & ~0b1u, sizeof(u16)),
            16u)
        );
    }
    inline u32 _bus_read_i8(
        u32 addr
    ) {
        //return u32(i32(i8(_bus_read(addr, sizeof(i8)))));
        return u32(sign_extend(_bus_read(addr, sizeof(u8)), 8u));
    }

    //inline u32 _read_mem
    inline void _write_gpr_rd(
        u32 val
    ) {
        if (_enc_instr_r.rd != 0) {
            _gpr_file.at(_enc_instr_r.rd) = val;
        }
    }
    //bool _do_disassemble();
};

#endif      // src_melted_moon_debug_riscv_emu_hpp
