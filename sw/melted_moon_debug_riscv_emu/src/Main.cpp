#include "MiscIncludes.hpp"
#include <sys/time.h>


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
    static constexpr const char* GPR_NAMES[NUM_GPRS] = {
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

    std::optional<u8*> _sw_wrote_to_fb_end = std::nullopt;
    timeval* _tp = nullptr;
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
    std::optional<u8*> exec_one_instr(struct timeval& n_tp);
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
MeltedMoonDebugRiscvEmu::MeltedMoonDebugRiscvEmu(
    const char* filename, u32 s_do_extra_print_start_pc
)
    :
    _do_extra_print_start_pc(s_do_extra_print_start_pc),
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
                "MeltedMoonDebugRiscvEmu::MeltedMoonDebugRiscvEmu(): "
                "\"%s\" is too large\n",
                filename
            );
            std::exit(1);
        }
    } else {
        std::fprintf(
            stderr,
            "MeltedMoonDebugRiscvEmu::MeltedMoonDebugRiscvEmu(): "
            "couldn't open \"%s\" for reading!\n",
            filename
        );
        std::exit(1);
    }
    _gpr_file.fill(0x0u);
    _pc = 0x0u;
    //_spr_file.fill(0x0u);

    //snowhousecpu_dasm_info_ctor(
    //    &_dasm,
    //    ::my_dasm_rd32_func,
    //    false
    //);
//--------
}

std::optional<u8*> MeltedMoonDebugRiscvEmu::exec_one_instr(
    timeval& n_tp
) {
    _tp = &n_tp;
    _sw_wrote_to_fb_end = std::nullopt;
    const u32 saved_pc = _pc;
    if (
        saved_pc != (saved_pc & ~0b11u)
    ) {
        std::fprintf(
            stderr,
            "Eek! mis-aligned pc:%x\n",
            saved_pc
        );
        std::exit(1);
    }
    _pc += sizeof(u32);

    std::memcpy(
        &_enc_instr_r, _mem.get() + saved_pc, sizeof(_enc_instr_r)
    );

    const u32 inp_rd = _rd();
    const u32 inp_rs1 = _rs1();
    const u32 inp_rs2 = _rs2();
    Rv32RType::EncInstr temp_enc_instr_r;
    std::memcpy(&temp_enc_instr_r, &_enc_instr_r, sizeof(_enc_instr_r));
    const std::string prev_to_dbg_print = _to_dbg_print;

    #ifdef DEBUG
    std::string disasm_str;
    #endif
    auto dbg_print = [&](
        bool force_print=false,
        bool final_start_cond=false
    ) -> void {
        if (!_do_extra_print_start_pc && !force_print) {
            return;
        }
        if (saved_pc == _do_extra_print_start_pc) {
            _seen_do_extra_print_start_pc = true;
        }
        if (final_start_cond) {
            _seen_final_start_print_cond = true;
        }
        if (!_seen_do_extra_print_start_pc && !force_print) {
            return;
        }
        if (!_seen_final_start_print_cond && !force_print) {
            return;
        }

        u32 temp_iword = 0u;
        std::memcpy(&temp_iword, &temp_enc_instr_r, sizeof(u32));

        std::printf(
            "saved_pc=%x    ", unsigned(saved_pc)
        );
        std::printf(
            "pc=%x    ", unsigned(_pc)
        );
        #ifdef DEBUG
        std::printf(
            "disasm:(%s)    ", disasm_str.c_str()
        );
        #endif
        std::printf(
            "inp_gprs:(%s:%x %s:%x %s:%x)    ",
            GPR_NAMES[temp_enc_instr_r.rd], inp_rd,
            GPR_NAMES[temp_enc_instr_r.rs1], inp_rs1,
            GPR_NAMES[temp_enc_instr_r.rs2], inp_rs2
        );
        for (size_t i=0u; i<_gpr_file.size(); ++i) {
            std::printf(
                "%s=%x",
                GPR_NAMES[i],
                unsigned(_gpr_file.at(i))
            );
            if (i + 1u < _gpr_file.size()) {
                std::printf(" ");
            }
        }
        printf("\n");
    };
    auto bad_instr = [&]() -> void {
        u32 temp_iword = 0u;
        std::memcpy(&temp_iword, &temp_enc_instr_r, sizeof(u32));

        dbg_print(true);

        std::fprintf(
            stderr,
            "Error: Unimplemented instruction: saved_pc=%x iword=%x\n",
            saved_pc,
            temp_iword
        );
        std::exit(1);
    };

    switch (temp_enc_instr_r.opcode) {
    case Rv32RType::Op::AddRdRs1Rs2.op: {
        std::string instr_name;
        switch (temp_enc_instr_r.funct7) {
        case Rv32RType::Op::AddRdRs1Rs2.f7: {
            switch (temp_enc_instr_r.funct3) {
            case Rv32RType::Op::AddRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x0, .f7=0x00},
                _write_gpr_rd(
                    inp_rs1 + inp_rs2
                );
                instr_name = "add";
            }
                break;
            case Rv32RType::Op::XorRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x4, .f7=0x00},
                _write_gpr_rd(
                    inp_rs1 ^ inp_rs2
                );
                instr_name = "xor";
            }
                break;
            case Rv32RType::Op::OrRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x6, .f7=0x00},
                _write_gpr_rd(
                    inp_rs1 | inp_rs2
                );
                instr_name = "or";
            }
                break;
            case Rv32RType::Op::AndRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x7, .f7=0x00},
                _write_gpr_rd(
                    inp_rs1 & inp_rs2
                );
                instr_name = "and";
            }
                break;
            case Rv32RType::Op::SllRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x1, .f7=0x0},
                //if (inp_rs2 >= 32u) {
                //    std::fprintf(
                //        stderr,
                //        "Error: "
                //        "Unknown behavior of bit shift (rs2:%u): %x\n",
                //        inp_rs2,
                //        saved_pc
                //    );
                //    dbg_print(true);
                //    std::exit(1);
                //    //_write_gpr_rd(0x0u);
                //} else {
                    _write_gpr_rd(
                        inp_rs1 << u32(inp_rs2 & 0x1fu)
                    );
                //}
                instr_name = "sll";
            }
                break;
            case Rv32RType::Op::SrlRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x5, .f7=0x00},
                //if (inp_rs2 >= 32u) {
                //    std::fprintf(
                //        stderr,
                //        "Error: "
                //        "Unknown behavior of bit shift (rs2:%u): %x\n",
                //        inp_rs2,
                //        saved_pc
                //    );
                //    dbg_print(true);
                //    std::exit(1);
                //    //_write_gpr_rd(0x0u);
                //} else {
                    _write_gpr_rd(
                        inp_rs1 >> u32(inp_rs2 & 0x1fu)
                    );
                //}
                instr_name = "srl";
            }
                break;
            case Rv32RType::Op::SltRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x2, .f7=0x00},
                if (i32(inp_rs1) < i32(inp_rs2)) {
                    _write_gpr_rd(0x1u);
                } else {
                    _write_gpr_rd(0x0u);
                }
                instr_name = "slt";
            }
                break;
            case Rv32RType::Op::SltuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x3, .f7=0x00},
                if (inp_rs1 < inp_rs2) {
                    _write_gpr_rd(0x1u);
                } else {
                    _write_gpr_rd(0x0u);
                }
                instr_name = "sltu";
            }
                break;
            default: {
                bad_instr();
            }
                break;
            }
        }
            break;
        case Rv32RType::Op::SubRdRs1Rs2.f7: {
            switch (temp_enc_instr_r.funct3) {
            case Rv32RType::Op::SubRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x0, .f7=0x20},
                _write_gpr_rd(
                    inp_rs1 - inp_rs2
                );
                instr_name = "sub";
            }
                break;
            case Rv32RType::Op::SraRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x5, .f7=0x20},
                //if (inp_rs2 >= 32u) {
                //    std::fprintf(
                //        stderr,
                //        "Error: "
                //        "Unknown behavior of bit shift (rs2:%u): %x\n",
                //        inp_rs2,
                //        saved_pc
                //    );
                //    dbg_print(true);
                //    std::exit(1);
                //    //if ((inp_rs1 >> 31u) & 0b1) {
                //    //    _write_gpr_rd(u32(i32(-1)));
                //    //} else {
                //    //    _write_gpr_rd(0x0u);
                //    //}
                //} else {
                    _write_gpr_rd(
                        i32(inp_rs1) >> u32(inp_rs2 & 0x1fu)
                    );
                //}
                instr_name = "sra";
            }
                break;
            default: {
                bad_instr();
            }
                break;
            }
        }
            break;
        //------
        case Rv32RType::Op::MulRdRs1Rs2.f7: {
            switch (temp_enc_instr_r.funct3) {
            case Rv32RType::Op::MulRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x0, .f7=0x01},
                _write_gpr_rd(
                    inp_rs1 * inp_rs2
                );
                instr_name = "mul";
            }
                break;
            case Rv32RType::Op::MulhRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x1, .f7=0x01},
                const i64 temp_rs1 = i64(sign_extend(inp_rs1, 32u));
                const i64 temp_rs2 = i64(sign_extend(inp_rs2, 32u));
                _write_gpr_rd(
                    u32(i64(temp_rs1 * temp_rs2) >> 32u)
                );
                instr_name = "mulh";
            }
                break;
            case Rv32RType::Op::MulhsuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x2, .f7=0x01},
                const i64 temp_rs1 = i64(sign_extend(inp_rs1, 32u));
                const u64 temp_rs2 = zero_extend(inp_rs2, 32u);
                _write_gpr_rd(
                    u32(u64(temp_rs1 * temp_rs2) >> 32u)
                );
                instr_name = "mulhsu";
            }
                break;
            case Rv32RType::Op::MulhuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x3, .f7=0x01},
                const u64 temp_rs1 = zero_extend(inp_rs1, 32u);
                const u64 temp_rs2 = zero_extend(inp_rs2, 32u);
                _write_gpr_rd(
                    u32(u64(temp_rs1 * temp_rs2) >> 32u)
                );
                instr_name = "mulhu";
            }
                break;
            case Rv32RType::Op::DivRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x4, .f7=0x01},
                if (inp_rs2 != 0) {
                    _write_gpr_rd(
                        u32(i32(i32(inp_rs1) / i32(inp_rs2)))
                    );
                } else {
                    _write_gpr_rd(u32(i32(-1)));
                }
                instr_name = "div";
            }
                break;
            case Rv32RType::Op::DivuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x5, .f7=0x01},
                if (inp_rs2 != 0) {
                    _write_gpr_rd(
                        inp_rs1 / inp_rs2
                    );
                } else {
                    _write_gpr_rd(u32(i32(-1)));
                }
                instr_name = "divu";
            }
                break;
            case Rv32RType::Op::RemRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x6, .f7=0x01},
                if (inp_rs2 != 0) {
                    _write_gpr_rd(
                        i32(i32(inp_rs1) % i32(inp_rs2))
                    );
                } else {
                    _write_gpr_rd(
                        inp_rs1
                    );
                }
                instr_name = "rem";
            }
                break;
            case Rv32RType::Op::RemuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x7, .f7=0x01};
                if (inp_rs2 != 0) {
                    _write_gpr_rd(
                        u32(inp_rs1 % inp_rs2)
                    );
                } else {
                    _write_gpr_rd(
                        inp_rs1
                    );
                }
                instr_name = "remu";
            }
                break;
            default: {
                bad_instr();
            }
                break;
            }
        }
            break;
        default: {
            bad_instr();
        }
            break;
        }
        #ifdef DEBUG
        disasm_str = sconcat(
            std::move(instr_name), " ",
            GPR_NAMES[temp_enc_instr_r.rd], ", ",
            GPR_NAMES[temp_enc_instr_r.rs1], ", ",
            GPR_NAMES[temp_enc_instr_r.rs2]
        );
        #endif
    }
        break;
    case Rv32IType::Op::AddiRdRs1Imm.op: {
        std::memcpy(&_enc_instr_i, &temp_enc_instr_r, sizeof(u32));
        std::string instr_name;
        switch (_enc_instr_i.funct3) {
        case Rv32IType::Op::AddiRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x0, .imm11dt5=-1},
            _write_gpr_rd(
                u32(inp_rs1) + _enc_instr_i.my_temp_imm()
            );
            instr_name = "addi";
        }
            break;
        case Rv32IType::Op::XoriRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x4, .imm11dt5=-1},
            _write_gpr_rd(
                u32(inp_rs1) ^ u32(_enc_instr_i.my_temp_imm())
            );
            instr_name = "xori";
        }
            break;
        case Rv32IType::Op::OriRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x6, .imm11dt5=-1},
            _write_gpr_rd(
                u32(inp_rs1) | u32(_enc_instr_i.my_temp_imm())
            );
            instr_name = "ori";
        }
            break;
        case Rv32IType::Op::AndiRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x7, .imm11dt5=-1},
            _write_gpr_rd(
                u32(inp_rs1) & u32(_enc_instr_i.my_temp_imm())
            );
            instr_name = "andi";
        }
            break;

        // rd = rs1 << imm[0:4]
        case Rv32IType::Op::SlliRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x1, .imm11dt5=0x00},
            _write_gpr_rd(
                inp_rs1
                << (u32(_enc_instr_i.my_imm4dt0()) & 0x1fu)
            );
            instr_name = "slli";
        }
            break;

        // rd = rs1 >> imm[0:4]
        case Rv32IType::Op::SrliRdRs1Imm.f3: {
            switch (_enc_instr_i.my_imm11dt5()) {
            // rd = rs1 >> imm[0:4]
            case Rv32IType::Op::SrliRdRs1Imm.imm11dt5: {
                // = {.op=0x13, .f3=0x5, .imm11dt5=0x00},
                _write_gpr_rd(
                    inp_rs1
                    >> (u32(_enc_instr_i.my_imm4dt0()) & 0x1fu)
                );
                instr_name = "srli";
            }
                break;

            // rd = rs1 >> imm[0:4] msb-extends
            case Rv32IType::Op::SraiRdRs1Imm.imm11dt5: {
                // = {.op=0x13, .f3=0x5, .imm11dt5=0x20},
                //std::printf(
                //    "NOTE: srai %s, %s, %x\n",
                //    GPR_NAMES[temp_enc_instr_r.rd],
                //    GPR_NAMES[temp_enc_instr_r.rs1],
                //    _enc_instr_i.my_imm4dt0()
                //);
                _write_gpr_rd(
                    i32(inp_rs1)
                    >> (u32(_enc_instr_i.my_imm4dt0()) & 0x1fu)
                );
                instr_name = "srai";
            }
                break;
            default: {
                bad_instr();
            }
                break;
            }
        }
            break;

        case Rv32IType::Op::SltiRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x2, .imm11dt5=-1},
            if (i32(inp_rs1) < i32(_enc_instr_i.my_temp_imm())) {
                _write_gpr_rd(0x1u);
            } else {
                _write_gpr_rd(0x0u);
            }
            instr_name = "slti";
        }
            break;
        case Rv32IType::Op::SltiuRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x3, .imm11dt5=-1},
            if (inp_rs1 < u32(i32(_enc_instr_i.my_temp_imm()))) {
                _write_gpr_rd(0x1u);
            } else {
                _write_gpr_rd(0x0u);
            }
            instr_name = "sltiu";
        }
            break;
        default: {
            bad_instr();
        }
            break;
        }
        #ifdef DEBUG
        disasm_str = sconcat(
            std::move(instr_name), " ",
            GPR_NAMES[temp_enc_instr_r.rd], ", ",
            GPR_NAMES[temp_enc_instr_r.rs1], ", ",
            std::hex, "0x", _enc_instr_i.my_temp_imm(), std::dec
        );
        #endif
    }
        break;

    case Rv32IType::Op::LbRdRs1Imm.op: {
        std::memcpy(&_enc_instr_i, &temp_enc_instr_r, sizeof(u32));
        std::string instr_name;
        switch (_enc_instr_i.funct3) {
        //--------
        case Rv32IType::Op::LbRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x0, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_i8(inp_rs1 + _enc_instr_i.my_temp_imm())
            );
            instr_name = "lb";
        }
            break;
        case Rv32IType::Op::LhRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x1, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_i16(inp_rs1 + _enc_instr_i.my_temp_imm())
            );
            instr_name = "lh";
        }
            break;
        case Rv32IType::Op::LwRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x2, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_u32(inp_rs1 + _enc_instr_i.my_temp_imm())
            );
            instr_name = "lw";
        }
            break;
        case Rv32IType::Op::LbuRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x4, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_u8(inp_rs1 + _enc_instr_i.my_temp_imm())
            );
            instr_name = "lbu";
        }
            break;
        case Rv32IType::Op::LhuRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x5, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_u16(inp_rs1 + _enc_instr_i.my_temp_imm())
            );
            instr_name = "lhu";
        }
            break;
        default: {
            bad_instr();
        }
            break;
        }
        #ifdef DEBUG
        disasm_str = sconcat(
            std::move(instr_name), " ",
            GPR_NAMES[temp_enc_instr_r.rd], ", ",
            GPR_NAMES[temp_enc_instr_r.rs1], ", ",
            std::hex, "0x", _enc_instr_i.my_temp_imm(), std::dec
        );
        #endif
    }
        break;
    //--------
    case Rv32IType::Op::JalrRdRs1Imm.op: {
        // = {.op=0x67, .f3=0x0, .imm11dt5=-1};
        // rd = PC+4; PC = rs1 + imm
        std::memcpy(&_enc_instr_i, &temp_enc_instr_r, sizeof(u32));
        _pc = inp_rs1 + _enc_instr_i.my_temp_imm();
        _write_gpr_rd(
            saved_pc + sizeof(u32)
        );
        #ifdef DEBUG
        disasm_str = sconcat(
            "jalr ",
            GPR_NAMES[temp_enc_instr_r.rd], ", ",
            GPR_NAMES[temp_enc_instr_r.rs1], ", ",
            std::hex, "0x", _enc_instr_i.my_temp_imm(), std::dec
        );
        #endif
    }
        break;

    case Rv32SType::Op::SbRs2Rs1Imm.op: {
        std::memcpy(&_enc_instr_s, &temp_enc_instr_r, sizeof(u32));
        std::string instr_name;
        switch (_enc_instr_s.funct3) {
        case Rv32SType::Op::SbRs2Rs1Imm.f3: {
            // = {.op=0x23, .f3=0x0},
            _bus_write_u8(
                inp_rs2, inp_rs1 + _enc_instr_s.my_temp_imm()
            );
            instr_name = "sb";
        }
            break;
        case Rv32SType::Op::ShRs2Rs1Imm.f3: {
            // = {.op=0x23, .f3=0x1},
            _bus_write_u16(
                inp_rs2, inp_rs1 + _enc_instr_s.my_temp_imm()
            );
            instr_name = "sh";
        }
            break;
        case Rv32SType::Op::SwRs2Rs1Imm.f3: {
            // = {.op=0x23, .f3=0x2};
            //std::printf(
            //    
            //);
            _bus_write_u32(
                inp_rs2, inp_rs1 + _enc_instr_s.my_temp_imm()
            );
            instr_name = "sw";
        }
            break;
        default: {
            bad_instr();
        }
            break;
        }
        #ifdef DEBUG
        disasm_str = sconcat(
            std::move(instr_name), " ",
            GPR_NAMES[temp_enc_instr_r.rs2], ", ",
            GPR_NAMES[temp_enc_instr_r.rs1], ", ",
            std::hex, "0x", _enc_instr_s.my_temp_imm(), std::dec
        );
        #endif
    }
        break;
    case Rv32BType::Op::BeqRs1Rs2Imm.op: {
        std::memcpy(&_enc_instr_b, &temp_enc_instr_r, sizeof(u32));
        std::string instr_name;
        switch (_enc_instr_b.funct3) {
        case Rv32BType::Op::BeqRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x0},
            if (inp_rs1 == inp_rs2) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
            instr_name = "beq";
        }
            break;
        case Rv32BType::Op::BneRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x1},
            if (inp_rs1 != inp_rs2) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
            instr_name = "bne";
        }
            break;
        case Rv32BType::Op::BltRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x4},
            if (i32(inp_rs1) < i32(inp_rs2)) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
            instr_name = "blt";
        }
            break;
        case Rv32BType::Op::BgeRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x5},
            if (i32(inp_rs1) >= i32(inp_rs2)) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
            instr_name = "bge";
        }
            break;
        case Rv32BType::Op::BltuRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x6},
            if (inp_rs1 < inp_rs2) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
            instr_name = "bltu";
        }
            break;
        case Rv32BType::Op::BgeuRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x7};
            if (inp_rs1 >= inp_rs2) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
            instr_name = "bgeu";
        }
            break;
        default: {
            bad_instr();
        }
            break;
        }
        #ifdef DEBUG
        disasm_str = sconcat(
            std::move(instr_name), " ",
            GPR_NAMES[temp_enc_instr_r.rs1], ", ",
            GPR_NAMES[temp_enc_instr_r.rs2], ", ",
            std::hex, "0x",
                (saved_pc + _enc_instr_b.my_temp_imm()),
            std::dec
        );
        #endif
    }
        break;

    case Rv32JType::Op::JalRdImm.op: {
        // = {.op=0x6f};
        // rd = PC+4; PC += imm
        std::memcpy(&_enc_instr_j, &temp_enc_instr_r, sizeof(u32));
        #ifdef DEBUG
        disasm_str = sconcat(
            "jal ",
            GPR_NAMES[temp_enc_instr_r.rd], ", ",
            std::hex, "0x",
                (saved_pc + _enc_instr_j.my_temp_imm()),
            std::dec
        );
        #endif
        _pc = saved_pc + _enc_instr_j.my_temp_imm();
        //_pc = _pc + _enc_instr_j.my_temp_imm();
        _write_gpr_rd(
            saved_pc + sizeof(u32)
        );
    }
        break;
    case Rv32UType::Op::LuiRdImm31Downto12.op: {
        // = {.op=0x37},
        std::memcpy(&_enc_instr_u, &temp_enc_instr_r, sizeof(u32));
        #ifdef DEBUG
        disasm_str = sconcat(
            "lui ",
            GPR_NAMES[temp_enc_instr_r.rd], ", ",
            std::hex, "0x", _enc_instr_u.my_temp_imm(), std::dec
        );
        #endif
        _write_gpr_rd(
            _enc_instr_u.my_temp_imm()
        );
    }
        break;
    case Rv32UType::Op::AuipcRdImm31Downto12.op: {
        // = {.op=0x17};
        std::memcpy(&_enc_instr_u, &temp_enc_instr_r, sizeof(u32));
        #ifdef DEBUG
        disasm_str = sconcat(
            "auipc ",
            GPR_NAMES[temp_enc_instr_r.rd], ", ",
            std::hex, "0x", _enc_instr_u.my_temp_imm(), std::dec
        );
        #endif
        _write_gpr_rd(
            saved_pc + _enc_instr_u.my_temp_imm()
            //_pc + _enc_instr_u.my_temp_imm()
        );
    }
        break;

    default: {
        bad_instr();
    }
        break;
    }
    const bool my_final_start_print_cond = (
        prev_to_dbg_print != _to_dbg_print
        && _to_dbg_print == "reading lump:4eb"
        //false
    );
    dbg_print(false, my_final_start_print_cond);

    //_pc += sizeof(u32);

    return _sw_wrote_to_fb_end;
}

void MeltedMoonDebugRiscvEmu::_bus_write(
    u32 data, u32 addr, size_t byte_count
) {
    //_have_doom_dbg = (
    //    (addr == BUS_ADDR_DOOM_WAD_DBG)
    //    ? HAVE_DOOM_DBG_WR
    //    : 0u
    //);
    if (
        _do_extra_print_start_pc
        && _seen_do_extra_print_start_pc
        && _seen_final_start_print_cond
    ) {
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
        } else if (addr == ADDR_UDIV64_INP_LEFT_LO) {
            _mmio_udiv64_inp_left &= u64(i64(-1ll) << 32u);
            _mmio_udiv64_inp_left |= data;
        } else if (addr == ADDR_UDIV64_INP_LEFT_HI) {
            _mmio_udiv64_inp_left &= u64(u32(i32(-1l)));
            _mmio_udiv64_inp_left |= (u64(data) << 32u);
        } else if (addr == ADDR_UDIV64_INP_RIGHT_LO) {
            _mmio_udiv64_inp_right &= u64(i64(-1ll) << 32u);
            _mmio_udiv64_inp_right |= data;
        } else if (addr == ADDR_UDIV64_INP_RIGHT_HI) {
            _mmio_udiv64_inp_right &= u64(u32(i32(-1l)));
            _mmio_udiv64_inp_right |= (u64(data) << 32u);
        } else if (addr == ADDR_IDIV64_INP_LEFT_LO) {
            _mmio_idiv64_inp_left &= u64(i64(-1ll) << 32u);
            _mmio_idiv64_inp_left |= data;
        } else if (addr == ADDR_IDIV64_INP_LEFT_HI) {
            _mmio_idiv64_inp_left &= u64(u32(i32(-1l)));
            _mmio_idiv64_inp_left |= (u64(data) << 32u);
        } else if (addr == ADDR_IDIV64_INP_RIGHT_LO) {
            _mmio_idiv64_inp_right &= u64(i64(-1ll) << 32u);
            _mmio_idiv64_inp_right |= data;
        } else if (addr == ADDR_IDIV64_INP_RIGHT_HI) {
            _mmio_idiv64_inp_right &= u64(u32(i32(-1l)));
            _mmio_idiv64_inp_right |= (u64(data) << 32u);
        } else if (addr > MEM_SIZE) {
            std::fprintf(
                stderr,
                "MeltedMoonDebugRiscvEmu::_bus_write(): "
                "invalid bus write: "
                "pc:%x data:%x addr:%x byte_count:%lu\n",
                _pc, data, addr, byte_count
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
            "MeltedMoonDebugRiscvEmu::_bus_write(): "
            "Invalid `byte_count`! (debug note: %lu)\n",
            byte_count
        );
        std::exit(1);
    }
}
u32 MeltedMoonDebugRiscvEmu::_bus_read(
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
            if (addr == ADDR_TIMER_USEC_LO) {
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
                    //struct timeval tp;
                    //gettimeofday(&tp, NULL);

                    ////*sec = tp.tv_sec;
                    ////*usec = tp.tv_usec;
                    ////tp.tv_usec -= first_tp.tv_usec;
                    i32 temp_usec = i32(_tp->tv_usec);
                    //memcpy(&ret, &tp.tv_usec, byte_count);
                    memcpy(&ret, &temp_usec, byte_count);
                //}
            } else if (addr == ADDR_TIMER_USEC_HI) {
                //struct timeval tp;
                //gettimeofday(&tp, NULL);

                //*sec = tp.tv_sec;
                //*usec = tp.tv_usec;
                //tp.tv_usec -= first_tp.tv_usec;
                i32 temp_usec = i32(i64(_tp->tv_usec) >> 32u);
                //memcpy(&ret, &tp.tv_usec, byte_count);
                memcpy(&ret, &temp_usec, byte_count);
            } else if (addr == ADDR_TIMER_SEC_LO) {
                //static bool did_first_check = false;
                //static struct timeval first_tp;

                //if (!did_first_check) {
                //    did_first_check = true;
                //    gettimeofday(&first_tp, NULL);
                //    memset(&ret, 0, byte_count);
                //} else {
                    //struct timeval tp;
                    //gettimeofday(&tp, NULL);

                    ////*sec = tp.tv_sec;
                    ////*usec = tp.tv_sec;
                    ////tp.tv_sec -= first_tp.tv_sec;
                    i32 temp_sec = i32(_tp->tv_sec);
                    //memcpy(&ret, &tp.tv_sec, byte_count);
                    memcpy(&ret, &temp_sec, byte_count);
                   // ret = ret >> 1;
                //}

                //static u32 temp = 0;
                ////u32 temp_cnt = temp / 8u;
                //u32 temp_cnt = temp;
                //memcpy(&ret, &temp_cnt, byte_count); 
                //temp++;
            } else if (addr == ADDR_TIMER_SEC_HI) {
                //struct timeval tp;
                //gettimeofday(&tp, NULL);

                ////*sec = tp.tv_sec;
                ////*usec = tp.tv_sec;
                ////tp.tv_sec -= first_tp.tv_sec;
                i32 temp_sec = i32(i64(_tp->tv_sec) >> 32u);
                //memcpy(&ret, &tp.tv_sec, byte_count);
                memcpy(&ret, &temp_sec, byte_count);
            } else if (addr == ADDR_UDIV64_OUTP_QUOT_LO) {
                _mmio_udiv64_outp_quot = (
                    _mmio_udiv64_inp_left / _mmio_udiv64_inp_right
                );
                ret = u32(_mmio_udiv64_outp_quot);
                //std::printf(
                //    "debug: addr:UDIV64_QUOT_LO: ra:%x _pc:%x ret:%x "
                //    "%llx / %llx = %llx\n",
                //    _gpr_file.at(1), _pc, ret,
                //    (unsigned long long)_mmio_udiv64_inp_left,
                //    (unsigned long long)_mmio_udiv64_inp_right,
                //    (unsigned long long)_mmio_udiv64_outp_quot
                //);
            } else if (addr == ADDR_UDIV64_OUTP_QUOT_HI) {
                _mmio_udiv64_outp_quot = (
                    _mmio_udiv64_inp_left / _mmio_udiv64_inp_right
                );
                ret = u32(_mmio_udiv64_outp_quot >> 32ul);
                //std::printf(
                //    "debug: addr:UDIV64_QUOT_HI: ra:%x _pc:%x ret:%x "
                //    "%llx / %llx = %llx\n",
                //    _gpr_file.at(1), _pc, ret,
                //    (unsigned long long)_mmio_udiv64_inp_left,
                //    (unsigned long long)_mmio_udiv64_inp_right,
                //    (unsigned long long)_mmio_udiv64_outp_quot
                //);
            } else if (addr == ADDR_UDIV64_OUTP_REMA_LO) {
                _mmio_udiv64_outp_rema = (
                    _mmio_udiv64_inp_left % _mmio_udiv64_inp_right
                );
                ret = u32(_mmio_udiv64_outp_rema);
                //std::printf(
                //    "debug: addr:UDIV64_REMA_LO: ra:%x _pc:%x ret:%x "
                //    "%llx %% %llx = %llx\n",
                //    _gpr_file.at(1), _pc, ret,
                //    (unsigned long long)_mmio_udiv64_inp_left,
                //    (unsigned long long)_mmio_udiv64_inp_right,
                //    (unsigned long long)_mmio_udiv64_outp_rema
                //);
            } else if (addr == ADDR_UDIV64_OUTP_REMA_HI) {
                _mmio_udiv64_outp_rema = (
                    _mmio_udiv64_inp_left % _mmio_udiv64_inp_right
                );
                ret = u32(_mmio_udiv64_outp_rema >> 32ul);
                //std::printf(
                //    "debug: addr:UDIV64_REMA_HI: ra:%x _pc:%x ret:%x "
                //    "%llx %% %llx = %llx\n",
                //    _gpr_file.at(1), _pc, ret,
                //    (unsigned long long)_mmio_udiv64_inp_left,
                //    (unsigned long long)_mmio_udiv64_inp_right,
                //    (unsigned long long)_mmio_udiv64_outp_rema
                //);
            } else if (addr == ADDR_IDIV64_OUTP_QUOT_LO) {
                _mmio_idiv64_outp_quot = (
                    i64(_mmio_idiv64_inp_left)
                    / i64(_mmio_idiv64_inp_right)
                );
                ret = u32(_mmio_idiv64_outp_quot);
                //std::printf(
                //    "debug: addr:IDIV64_QUOT_LO: ra:%x _pc:%x ret:%x "
                //    "%llx / %llx = %llx\n",
                //    _gpr_file.at(1), _pc, ret,
                //    (unsigned long long)_mmio_idiv64_inp_left,
                //    (unsigned long long)_mmio_idiv64_inp_right,
                //    (unsigned long long)_mmio_idiv64_outp_quot
                //);
            } else if (addr == ADDR_IDIV64_OUTP_QUOT_HI) {
                _mmio_idiv64_outp_quot = (
                    i64(_mmio_idiv64_inp_left)
                    / i64(_mmio_idiv64_inp_right)
                );
                ret = u32(_mmio_idiv64_outp_quot >> 32ul);
                //std::printf(
                //    "debug: addr:IDIV64_QUOT_HI: ra:%x _pc:%x ret:%x "
                //    "%llx / %llx = %llx\n",
                //    _gpr_file.at(1), _pc, ret,
                //    (unsigned long long)_mmio_idiv64_inp_left,
                //    (unsigned long long)_mmio_idiv64_inp_right,
                //    (unsigned long long)_mmio_idiv64_outp_quot
                //);
            } else if (addr == ADDR_IDIV64_OUTP_REMA_LO) {
                _mmio_idiv64_outp_rema = (
                    i64(_mmio_idiv64_inp_left)
                    % i64(_mmio_idiv64_inp_right)
                );
                ret = u32(_mmio_idiv64_outp_rema);
                //std::printf(
                //    "debug: addr:IDIV64_REMA_LO: ra:%x _pc:%x ret:%x "
                //    "%llx %% %llx = %llx\n",
                //    _gpr_file.at(1), _pc, ret,
                //    (unsigned long long)_mmio_idiv64_inp_left,
                //    (unsigned long long)_mmio_idiv64_inp_right,
                //    (unsigned long long)_mmio_idiv64_outp_rema
                //);
            } else if (addr == ADDR_IDIV64_OUTP_REMA_HI) {
                _mmio_idiv64_outp_rema = (
                    i64(_mmio_idiv64_inp_left)
                    % i64(_mmio_idiv64_inp_right)
                );
                ret = u32(_mmio_idiv64_outp_rema >> 32ul);
                //std::printf(
                //    "debug: addr:IDIV64_REMA_HI: ra:%x _pc:%x ret:%x "
                //    "%llx %% %llx = %llx\n",
                //    _gpr_file.at(1), _pc, ret,
                //    (unsigned long long)_mmio_idiv64_inp_left,
                //    (unsigned long long)_mmio_idiv64_inp_right,
                //    (unsigned long long)_mmio_idiv64_outp_rema
                //);
            } else {
                std::fprintf(
                    stderr,
                    "MeltedMoonDebugRiscvEmu::_bus_read(): "
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
            "MeltedMoonDebugRiscvEmu::_bus_read(): "
            "Invalid `byte_count`! (debug note: %lu)\n",
            byte_count
        );
        std::exit(1);
    }
    if (
        _do_extra_print_start_pc
        && _seen_do_extra_print_start_pc
        && _seen_final_start_print_cond
    ) {
        std::printf(
            "_bus_read(): data:%x addr:%x byte_count:%lu\n",
            ret, addr, byte_count
        );
    }
    return ret;
}

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

    //for (size_t instr_cnt=0; instr_cnt < 1024u; ++instr_cnt) 

    //struct timeval tp;
    size_t update_tp_cnt = 0u;
    struct timeval tp;
    gettimeofday(&tp, nullptr);
    for (;;) 
    {
        //u16 temp_fb_data;
        //u32 temp_fb_addr;
        ++update_tp_cnt;
        if (update_tp_cnt >= 16u) {
            update_tp_cnt = 0u;
            gettimeofday(&tp, nullptr);
        }

        if (auto fb_start = emu.exec_one_instr(tp); fb_start) {
            //printout(
            //    "testificate!\n"
            //);
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
