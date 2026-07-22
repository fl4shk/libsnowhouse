#include "MeltedMoonDebugRiscvEmu.hpp"

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

std::optional<std::string> MeltedMoonDebugRiscvEmu::disasm_one_instr(
    u32 some_enc_instr,
    u32 some_saved_pc
) {
    Rv32RType::EncInstr temp_enc_instr_r;
    std::memcpy(&temp_enc_instr_r, &some_enc_instr, sizeof(u32));

    switch (temp_enc_instr_r.opcode) {
    case Rv32RType::Op::AddRdRs1Rs2.op: {
        std::string instr_name;
        switch (temp_enc_instr_r.funct7) {
        case Rv32RType::Op::AddRdRs1Rs2.f7: {
            switch (temp_enc_instr_r.funct3) {
            case Rv32RType::Op::AddRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x0, .f7=0x00},
                instr_name = "add";
            }
                break;
            case Rv32RType::Op::XorRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x4, .f7=0x00},
                instr_name = "xor";
            }
                break;
            case Rv32RType::Op::OrRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x6, .f7=0x00},
                instr_name = "or";
            }
                break;
            case Rv32RType::Op::AndRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x7, .f7=0x00},
                instr_name = "and";
            }
                break;
            case Rv32RType::Op::SllRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x1, .f7=0x0},
                instr_name = "sll";
            }
                break;
            case Rv32RType::Op::SrlRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x5, .f7=0x00},
                instr_name = "srl";
            }
                break;
            case Rv32RType::Op::SltRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x2, .f7=0x00},
                instr_name = "slt";
            }
                break;
            case Rv32RType::Op::SltuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x3, .f7=0x00},
                instr_name = "sltu";
            }
                break;
            default: {
                //bad_instr();
                return std::nullopt;
            }
                break;
            }
        }
            break;
        case Rv32RType::Op::SubRdRs1Rs2.f7: {
            switch (temp_enc_instr_r.funct3) {
            case Rv32RType::Op::SubRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x0, .f7=0x20},
                instr_name = "sub";
            }
                break;
            case Rv32RType::Op::SraRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x5,
                instr_name = "sra";
            }
                break;
            default: {
                //bad_instr();
                return std::nullopt;
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
                instr_name = "mul";
            }
                break;
            case Rv32RType::Op::MulhRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x1, .f7=0x01},
                instr_name = "mulh";
            }
                break;
            case Rv32RType::Op::MulhsuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x2, .f7=0x01},
                instr_name = "mulhsu";
            }
                break;
            case Rv32RType::Op::MulhuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x3, .f7=0x01},
                instr_name = "mulhu";
            }
                break;
            case Rv32RType::Op::DivRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x4, .f7=0x01},
                instr_name = "div";
            }
                break;
            case Rv32RType::Op::DivuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x5, .f7=0x01},
                instr_name = "divu";
            }
                break;
            case Rv32RType::Op::RemRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x6, .f7=0x01},
                instr_name = "rem";
            }
                break;
            case Rv32RType::Op::RemuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x7, .f7=0x01};
                instr_name = "remu";
            }
                break;
            default: {
                //bad_instr();
                return std::nullopt;
            }
                break;
            }
        }
            break;
        default: {
            //bad_instr();
            return std::nullopt;
        }
            break;
        }
        //#ifdef MELTED_MOON_DO_DISASM
        //disasm_str = sconcat(
        //    std::move(instr_name), " ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rs2]
        //);
        //#endif
        return sconcat(
            std::move(instr_name), " ",
            GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
            GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
            GPR_NAMES_ARR[temp_enc_instr_r.rs2]
        );
    }
        break;
    case Rv32IType::Op::AddiRdRs1Imm.op: {
        Rv32IType::EncInstr temp_enc_instr_i;
        std::memcpy(&temp_enc_instr_i, &temp_enc_instr_r, sizeof(u32));
        std::string instr_name;
        switch (temp_enc_instr_i.funct3) {
        case Rv32IType::Op::AddiRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x0, .imm11dt5=-1},
            instr_name = "addi";
        }
            break;
        case Rv32IType::Op::XoriRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x4, .imm11dt5=-1},
            instr_name = "xori";
        }
            break;
        case Rv32IType::Op::OriRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x6, .imm11dt5=-1},
            instr_name = "ori";
        }
            break;
        case Rv32IType::Op::AndiRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x7, .imm11dt5=-1},
            instr_name = "andi";
        }
            break;

        // rd = rs1 << imm[0:4]
        case Rv32IType::Op::SlliRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x1, .imm11dt5=0x00},
            instr_name = "slli";
        }
            break;

        // rd = rs1 >> imm[0:4]
        case Rv32IType::Op::SrliRdRs1Imm.f3: {
            switch (temp_enc_instr_i.my_imm11dt5()) {
            // rd = rs1 >> imm[0:4]
            case Rv32IType::Op::SrliRdRs1Imm.imm11dt5: {
                // = {.op=0x13, .f3=0x5, .imm11dt5=0x00},
                instr_name = "srli";
            }
                break;

            // rd = rs1 >> imm[0:4] msb-extends
            case Rv32IType::Op::SraiRdRs1Imm.imm11dt5: {
                // = {.op=0x13, .f3=0x5, .imm11dt5=0x20},
                instr_name = "srai";
            }
                break;
            default: {
                //bad_instr();
                return std::nullopt;
            }
                break;
            }
        }
            break;

        case Rv32IType::Op::SltiRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x2, .imm11dt5=-1},
            instr_name = "slti";
        }
            break;
        case Rv32IType::Op::SltiuRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x3, .imm11dt5=-1},
            instr_name = "sltiu";
        }
            break;
        default: {
            //bad_instr();
            return std::nullopt;
        }
            break;
        }
        //#ifdef MELTED_MOON_DO_DISASM
        //disasm_str = sconcat(
        //    std::move(instr_name), " ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
        //    std::hex, "0x", temp_enc_instr_i.my_temp_imm(), std::dec
        //);
        //#endif
        return sconcat(
            std::move(instr_name), " ",
            GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
            GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
            std::hex, "0x", temp_enc_instr_i.my_temp_imm(), std::dec
        );
    }
        break;

    case Rv32IType::Op::LbRdRs1Imm.op: {
        Rv32IType::EncInstr temp_enc_instr_i;
        std::memcpy(&temp_enc_instr_i, &temp_enc_instr_r, sizeof(u32));
        std::string instr_name;
        switch (temp_enc_instr_i.funct3) {
        //--------
        case Rv32IType::Op::LbRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x0, .imm11dt5=-1},
            instr_name = "lb";
        }
            break;
        case Rv32IType::Op::LhRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x1, .imm11dt5=-1},
            instr_name = "lh";
        }
            break;
        case Rv32IType::Op::LwRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x2, .imm11dt5=-1},
            instr_name = "lw";
        }
            break;
        case Rv32IType::Op::LbuRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x4, .imm11dt5=-1},
            instr_name = "lbu";
        }
            break;
        case Rv32IType::Op::LhuRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x5, .imm11dt5=-1},
            instr_name = "lhu";
        }
            break;
        default: {
            //bad_instr();
            return std::nullopt;
        }
            break;
        }
        //#ifdef MELTED_MOON_DO_DISASM
        //disasm_str = sconcat(
        //    std::move(instr_name), " ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
        //    std::hex, "0x", temp_enc_instr_i.my_temp_imm(), std::dec
        //);
        //#endif
        return sconcat(
            std::move(instr_name), " ",
            GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
            GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
            std::hex, "0x", temp_enc_instr_i.my_temp_imm(), std::dec
        );
    }
        break;
    //--------
    case Rv32IType::Op::JalrRdRs1Imm.op: {
        // = {.op=0x67, .f3=0x0, .imm11dt5=-1};
        // rd = PC+4; PC = rs1 + imm
        Rv32IType::EncInstr temp_enc_instr_i;
        std::memcpy(&temp_enc_instr_i, &temp_enc_instr_r, sizeof(u32));
        //#ifdef MELTED_MOON_DO_DISASM
        //disasm_str = sconcat(
        //    "jalr ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
        //    std::hex, "0x", temp_enc_instr_i.my_temp_imm(), std::dec
        //);
        //#endif
        return sconcat(
            "jalr ",
            GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
            GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
            std::hex, "0x", temp_enc_instr_i.my_temp_imm(), std::dec
        );
    }
        break;

    case Rv32SType::Op::SbRs2Rs1Imm.op: {
        Rv32SType::EncInstr temp_enc_instr_s;
        std::memcpy(&temp_enc_instr_s, &temp_enc_instr_r, sizeof(u32));
        std::string instr_name;
        switch (temp_enc_instr_s.funct3) {
        case Rv32SType::Op::SbRs2Rs1Imm.f3: {
            // = {.op=0x23, .f3=0x0},
            instr_name = "sb";
        }
            break;
        case Rv32SType::Op::ShRs2Rs1Imm.f3: {
            // = {.op=0x23, .f3=0x1},
            instr_name = "sh";
        }
            break;
        case Rv32SType::Op::SwRs2Rs1Imm.f3: {
            // = {.op=0x23, .f3=0x2};
            instr_name = "sw";
        }
            break;
        default: {
            //bad_instr();
            return std::nullopt;
        }
            break;
        }
        //#ifdef MELTED_MOON_DO_DISASM
        //disasm_str = sconcat(
        //    std::move(instr_name), " ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rs2], ", ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
        //    std::hex, "0x", temp_enc_instr_s.my_temp_imm(), std::dec
        //);
        //#endif
        return sconcat(
            std::move(instr_name), " ",
            GPR_NAMES_ARR[temp_enc_instr_r.rs2], ", ",
            GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
            std::hex, "0x", temp_enc_instr_s.my_temp_imm(), std::dec
        );
    }
        break;
    case Rv32BType::Op::BeqRs1Rs2Imm.op: {
        Rv32BType::EncInstr temp_enc_instr_b;
        std::memcpy(&temp_enc_instr_b, &temp_enc_instr_r, sizeof(u32));
        std::string instr_name;
        switch (temp_enc_instr_b.funct3) {
        case Rv32BType::Op::BeqRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x0},
            instr_name = "beq";
        }
            break;
        case Rv32BType::Op::BneRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x1},
            instr_name = "bne";
        }
            break;
        case Rv32BType::Op::BltRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x4},
            instr_name = "blt";
        }
            break;
        case Rv32BType::Op::BgeRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x5},
            instr_name = "bge";
        }
            break;
        case Rv32BType::Op::BltuRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x6},
            instr_name = "bltu";
        }
            break;
        case Rv32BType::Op::BgeuRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x7};
            instr_name = "bgeu";
        }
            break;
        default: {
            //bad_instr();
            return std::nullopt;
        }
            break;
        }
        //#ifdef MELTED_MOON_DO_DISASM
        //disasm_str = sconcat(
        //    std::move(instr_name), " ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rs2], ", ",
        //    std::hex, "0x",
        //        (saved_pc + temp_enc_instr_b.my_temp_imm()),
        //    std::dec
        //);
        //#endif
        return sconcat(
            std::move(instr_name), " ",
            GPR_NAMES_ARR[temp_enc_instr_r.rs1], ", ",
            GPR_NAMES_ARR[temp_enc_instr_r.rs2], ", ",
            std::hex, "0x",
                (some_saved_pc + temp_enc_instr_b.my_temp_imm()),
            std::dec
        );
    }
        break;

    case Rv32JType::Op::JalRdImm.op: {
        // = {.op=0x6f};
        // rd = PC+4; PC += imm
        Rv32JType::EncInstr temp_enc_instr_j;
        std::memcpy(&temp_enc_instr_j, &temp_enc_instr_r, sizeof(u32));
        //#ifdef MELTED_MOON_DO_DISASM
        //disasm_str = sconcat(
        //    "jal ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
        //    std::hex, "0x",
        //        (saved_pc + temp_enc_instr_j.my_temp_imm()),
        //    std::dec
        //);
        //#endif
        return sconcat(
            "jal ",
            GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
            std::hex, "0x",
                (some_saved_pc + temp_enc_instr_j.my_temp_imm()),
            std::dec
        );
    }
        break;
    case Rv32UType::Op::LuiRdImm31Downto12.op: {
        // = {.op=0x37},
        Rv32UType::EncInstr temp_enc_instr_u;
        std::memcpy(&temp_enc_instr_u, &temp_enc_instr_r, sizeof(u32));
        //#ifdef MELTED_MOON_DO_DISASM
        //disasm_str = sconcat(
        //    "lui ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
        //    std::hex, "0x", temp_enc_instr_u.my_temp_imm(), std::dec
        //);
        //#endif
        return sconcat(
            "lui ",
            GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
            std::hex, "0x", temp_enc_instr_u.my_temp_imm(), std::dec
        );
    }
        break;
    case Rv32UType::Op::AuipcRdImm31Downto12.op: {
        // = {.op=0x17};
        Rv32UType::EncInstr temp_enc_instr_u;
        std::memcpy(&temp_enc_instr_u, &temp_enc_instr_r, sizeof(u32));
        //#ifdef MELTED_MOON_DO_DISASM
        //disasm_str = sconcat(
        //    "auipc ",
        //    GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
        //    std::hex, "0x", temp_enc_instr_u.my_temp_imm(), std::dec
        //);
        //#endif
        return sconcat(
            "auipc ",
            GPR_NAMES_ARR[temp_enc_instr_r.rd], ", ",
            std::hex, "0x", temp_enc_instr_u.my_temp_imm(), std::dec
        );
    }
        break;

    default: {
        //bad_instr();
        return std::nullopt;
    }
        break;
    }
}

auto MeltedMoonDebugRiscvEmu::exec_one_instr(
    timeval& n_tp,
    bool n_do_printing,
    const std::optional<u32>& n_pc,
    const std::optional<u32>& n_enc_instr
) -> ExecOneInstrRet {
    _tp = &n_tp;
    _do_printing = n_do_printing;

    //_my_exec_one_instr_ret.sw_wrote_to_fb_end = std::nullopt;
    //_my_exec_one_instr_ret.sw_read_from_tp = false;
    _my_exec_one_instr_ret = ExecOneInstrRet();
    _my_exec_one_instr_ret.gpr_file = &_gpr_file;
    const u32 saved_pc = (
        n_pc ? *n_pc : _pc
    );
    _my_exec_one_instr_ret.saved_pc = saved_pc;

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

    if (!n_enc_instr) {
        std::memcpy(
            &_my_exec_one_instr_ret.enc_instr,
            _mem.get() + saved_pc,
            sizeof(_my_exec_one_instr_ret.enc_instr)
        );
    } else {
        const u32 temp_enc_instr = *n_enc_instr;
        std::memcpy(
            &_my_exec_one_instr_ret.enc_instr,
            &temp_enc_instr,
            sizeof(_my_exec_one_instr_ret.enc_instr)
        );
    }
    std::memcpy(
        &_enc_instr_r,
        &_my_exec_one_instr_ret.enc_instr,
        sizeof(_enc_instr_r)
    );
    static constexpr u32 ENC_INSTR_EBREAK = 0x00100073u;
    static constexpr u32 ENC_INSTR_ECALL = 0x00000073u;
    u32 my_temp_enc_instr = 0;
    std::memcpy(&my_temp_enc_instr, &_enc_instr_r, sizeof(u32));
    if (
        my_temp_enc_instr == ENC_INSTR_EBREAK
        || my_temp_enc_instr == ENC_INSTR_ECALL
    ) {
        std::fprintf(
            stderr,
            "Error: at saved_pc:%x, found ebreak:%i or ecall:%i!\n",
            saved_pc,
            int(my_temp_enc_instr == ENC_INSTR_EBREAK),
            int(my_temp_enc_instr == ENC_INSTR_ECALL)
        );
        std::exit(1);
    }

    const u32 inp_rd = _rd();
    const u32 inp_rs1 = _rs1();
    const u32 inp_rs2 = _rs2();
    Rv32RType::EncInstr temp_enc_instr_r;
    std::memcpy(&temp_enc_instr_r, &_enc_instr_r, sizeof(_enc_instr_r));
    const std::string prev_to_dbg_print = _to_dbg_print;

    #ifdef MELTED_MOON_DO_DISASM
    auto& disasm_str = _my_exec_one_instr_ret.disasm_str;
    if (
        auto temp_disasm_str = disasm_one_instr(
            my_temp_enc_instr,
            saved_pc
        );
        temp_disasm_str
    ) {
        disasm_str = *temp_disasm_str;
    } else {
        disasm_str = sconcat(
            "bad (0x",
            std::hex, my_temp_enc_instr, std::dec,
            ")"
        );
    }
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
        #ifdef MELTED_MOON_DO_DISASM
        std::printf(
            "disasm:(%s)    ", disasm_str.c_str()
        );
        #endif
        std::printf(
            "inp_gprs:(%s:%x %s:%x %s:%x)    ",
            GPR_NAMES_ARR[temp_enc_instr_r.rd], inp_rd,
            GPR_NAMES_ARR[temp_enc_instr_r.rs1], inp_rs1,
            GPR_NAMES_ARR[temp_enc_instr_r.rs2], inp_rs2
        );
        for (size_t i=0u; i<_gpr_file.size(); ++i) {
            std::printf(
                "%s=%x",
                GPR_NAMES_ARR[i],
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
        switch (temp_enc_instr_r.funct7) {
        case Rv32RType::Op::AddRdRs1Rs2.f7: {
            switch (temp_enc_instr_r.funct3) {
            case Rv32RType::Op::AddRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x0, .f7=0x00},
                _write_gpr_rd(
                    inp_rs1 + inp_rs2
                );
            }
                break;
            case Rv32RType::Op::XorRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x4, .f7=0x00},
                _write_gpr_rd(
                    inp_rs1 ^ inp_rs2
                );
            }
                break;
            case Rv32RType::Op::OrRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x6, .f7=0x00},
                _write_gpr_rd(
                    inp_rs1 | inp_rs2
                );
            }
                break;
            case Rv32RType::Op::AndRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x7, .f7=0x00},
                _write_gpr_rd(
                    inp_rs1 & inp_rs2
                );
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
            }
                break;
            case Rv32RType::Op::SltRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x2, .f7=0x00},
                if (i32(inp_rs1) < i32(inp_rs2)) {
                    _write_gpr_rd(0x1u);
                } else {
                    _write_gpr_rd(0x0u);
                }
            }
                break;
            case Rv32RType::Op::SltuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x3, .f7=0x00},
                if (inp_rs1 < inp_rs2) {
                    _write_gpr_rd(0x1u);
                } else {
                    _write_gpr_rd(0x0u);
                }
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
            }
                break;
            case Rv32RType::Op::MulhRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x1, .f7=0x01},
                const i64 temp_rs1 = i64(sign_extend(inp_rs1, 32u));
                const i64 temp_rs2 = i64(sign_extend(inp_rs2, 32u));
                _write_gpr_rd(
                    u32(i64(temp_rs1 * temp_rs2) >> 32u)
                );
            }
                break;
            case Rv32RType::Op::MulhsuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x2, .f7=0x01},
                const i64 temp_rs1 = i64(sign_extend(inp_rs1, 32u));
                const u64 temp_rs2 = zero_extend(inp_rs2, 32u);
                _write_gpr_rd(
                    u32(u64(temp_rs1 * temp_rs2) >> 32u)
                );
            }
                break;
            case Rv32RType::Op::MulhuRdRs1Rs2.f3: {
                // {.op=0x33, .f3=0x3, .f7=0x01},
                const u64 temp_rs1 = zero_extend(inp_rs1, 32u);
                const u64 temp_rs2 = zero_extend(inp_rs2, 32u);
                _write_gpr_rd(
                    u32(u64(temp_rs1 * temp_rs2) >> 32u)
                );
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
        }
            break;
        case Rv32IType::Op::XoriRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x4, .imm11dt5=-1},
            _write_gpr_rd(
                u32(inp_rs1) ^ u32(_enc_instr_i.my_temp_imm())
            );
        }
            break;
        case Rv32IType::Op::OriRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x6, .imm11dt5=-1},
            _write_gpr_rd(
                u32(inp_rs1) | u32(_enc_instr_i.my_temp_imm())
            );
        }
            break;
        case Rv32IType::Op::AndiRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x7, .imm11dt5=-1},
            _write_gpr_rd(
                u32(inp_rs1) & u32(_enc_instr_i.my_temp_imm())
            );
        }
            break;

        // rd = rs1 << imm[0:4]
        case Rv32IType::Op::SlliRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x1, .imm11dt5=0x00},
            _write_gpr_rd(
                inp_rs1
                << (u32(_enc_instr_i.my_imm4dt0()) & 0x1fu)
            );
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
            }
                break;

            // rd = rs1 >> imm[0:4] msb-extends
            case Rv32IType::Op::SraiRdRs1Imm.imm11dt5: {
                // = {.op=0x13, .f3=0x5, .imm11dt5=0x20},
                //std::printf(
                //    "NOTE: srai %s, %s, %x\n",
                //    GPR_NAMES_ARR[temp_enc_instr_r.rd],
                //    GPR_NAMES_ARR[temp_enc_instr_r.rs1],
                //    _enc_instr_i.my_imm4dt0()
                //);
                _write_gpr_rd(
                    i32(inp_rs1)
                    >> (u32(_enc_instr_i.my_imm4dt0()) & 0x1fu)
                );
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
        }
            break;
        case Rv32IType::Op::SltiuRdRs1Imm.f3: {
            // = {.op=0x13, .f3=0x3, .imm11dt5=-1},
            if (inp_rs1 < u32(i32(_enc_instr_i.my_temp_imm()))) {
                _write_gpr_rd(0x1u);
            } else {
                _write_gpr_rd(0x0u);
            }
        }
            break;
        default: {
            bad_instr();
        }
            break;
        }
    }
        break;

    case Rv32IType::Op::LbRdRs1Imm.op: {
        std::memcpy(&_enc_instr_i, &temp_enc_instr_r, sizeof(u32));
        _my_exec_one_instr_ret.bus_addr = (
            inp_rs1 + _enc_instr_i.my_temp_imm()
        );
        switch (_enc_instr_i.funct3) {
        //--------
        case Rv32IType::Op::LbRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x0, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_i8(_my_exec_one_instr_ret.bus_addr)
            );
        }
            break;
        case Rv32IType::Op::LhRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x1, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_i16(_my_exec_one_instr_ret.bus_addr)
            );
        }
            break;
        case Rv32IType::Op::LwRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x2, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_u32(_my_exec_one_instr_ret.bus_addr)
            );
        }
            break;
        case Rv32IType::Op::LbuRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x4, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_u8(_my_exec_one_instr_ret.bus_addr)
            );
        }
            break;
        case Rv32IType::Op::LhuRdRs1Imm.f3: {
            // = {.op=0x03, .f3=0x5, .imm11dt5=-1},
            _write_gpr_rd(
                _bus_read_u16(_my_exec_one_instr_ret.bus_addr)
            );
        }
            break;
        default: {
            bad_instr();
        }
            break;
        }
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
    }
        break;

    case Rv32SType::Op::SbRs2Rs1Imm.op: {
        std::memcpy(&_enc_instr_s, &temp_enc_instr_r, sizeof(u32));
        switch (_enc_instr_s.funct3) {
        case Rv32SType::Op::SbRs2Rs1Imm.f3: {
            // = {.op=0x23, .f3=0x0},

            _my_exec_one_instr_ret.bus_addr = (
                inp_rs1 + _enc_instr_s.my_temp_imm()
            );
            _bus_write_u8(
                inp_rs2, 
                _my_exec_one_instr_ret.bus_addr
                //inp_rs1 + _enc_instr_s.my_temp_imm()
            );
        }
            break;
        case Rv32SType::Op::ShRs2Rs1Imm.f3: {
            // = {.op=0x23, .f3=0x1},

            _my_exec_one_instr_ret.bus_addr = (
                inp_rs1 + _enc_instr_s.my_temp_imm()
            );
            _bus_write_u16(
                inp_rs2, _my_exec_one_instr_ret.bus_addr
            );
        }
            break;
        case Rv32SType::Op::SwRs2Rs1Imm.f3: {
            // = {.op=0x23, .f3=0x2};
            //std::printf(
            //    
            //);
            _my_exec_one_instr_ret.bus_addr = (
                inp_rs1 + _enc_instr_s.my_temp_imm()
            );
            _bus_write_u32(
                inp_rs2, _my_exec_one_instr_ret.bus_addr
            );
        }
            break;
        default: {
            bad_instr();
        }
            break;
        }
    }
        break;
    case Rv32BType::Op::BeqRs1Rs2Imm.op: {
        std::memcpy(&_enc_instr_b, &temp_enc_instr_r, sizeof(u32));
        switch (_enc_instr_b.funct3) {
        case Rv32BType::Op::BeqRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x0},
            if (inp_rs1 == inp_rs2) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
        }
            break;
        case Rv32BType::Op::BneRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x1},
            if (inp_rs1 != inp_rs2) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
        }
            break;
        case Rv32BType::Op::BltRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x4},
            if (i32(inp_rs1) < i32(inp_rs2)) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
        }
            break;
        case Rv32BType::Op::BgeRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x5},
            if (i32(inp_rs1) >= i32(inp_rs2)) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
        }
            break;
        case Rv32BType::Op::BltuRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x6},
            if (inp_rs1 < inp_rs2) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
        }
            break;
        case Rv32BType::Op::BgeuRs1Rs2Imm.f3: {
            // = {.op=0x63, .f3=0x7};
            if (inp_rs1 >= inp_rs2) {
                _pc = saved_pc + _enc_instr_b.my_temp_imm();
                //_pc = _pc + _enc_instr_b.my_temp_imm();
            }
        }
            break;
        default: {
            bad_instr();
        }
            break;
        }
    }
        break;

    case Rv32JType::Op::JalRdImm.op: {
        // = {.op=0x6f};
        // rd = PC+4; PC += imm
        std::memcpy(&_enc_instr_j, &temp_enc_instr_r, sizeof(u32));
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
        _write_gpr_rd(
            _enc_instr_u.my_temp_imm()
        );
    }
        break;
    case Rv32UType::Op::AuipcRdImm31Downto12.op: {
        // = {.op=0x17};
        std::memcpy(&_enc_instr_u, &temp_enc_instr_r, sizeof(u32));
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

    //return std::pair(_my_exec_one_instr_ret.sw_read_from_tp, _my_exec_one_instr_ret.sw_wrote_to_fb_end);
    _my_exec_one_instr_ret.pc = _pc;
    return _my_exec_one_instr_ret;
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

    const u32 temp_addr = addr & ~0x8000000;

    if (
        byte_count == sizeof(u8)
        || byte_count == sizeof(u16)
        || byte_count == sizeof(u32)
    ) {
        if (temp_addr == ADDR_PRINT) {
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
                    if (_do_printing) {
                        std::printf(
                            "%s\n",
                            _to_dbg_print.c_str()
                        );
                    }
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
        } else if (temp_addr == ADDR_EXIT) {
            std::exit(data);
        } else if (temp_addr == ADDR_UDIV64_INP_LEFT_LO) {
            _mmio_udiv64_inp_left &= u64(i64(-1ll) << 32u);
            _mmio_udiv64_inp_left |= data;
        } else if (temp_addr == ADDR_UDIV64_INP_LEFT_HI) {
            _mmio_udiv64_inp_left &= u64(u32(i32(-1l)));
            _mmio_udiv64_inp_left |= (u64(data) << 32u);
        } else if (temp_addr == ADDR_UDIV64_INP_RIGHT_LO) {
            _mmio_udiv64_inp_right &= u64(i64(-1ll) << 32u);
            _mmio_udiv64_inp_right |= data;
        } else if (temp_addr == ADDR_UDIV64_INP_RIGHT_HI) {
            _mmio_udiv64_inp_right &= u64(u32(i32(-1l)));
            _mmio_udiv64_inp_right |= (u64(data) << 32u);
        } else if (temp_addr == ADDR_IDIV64_INP_LEFT_LO) {
            _mmio_idiv64_inp_left &= u64(i64(-1ll) << 32u);
            _mmio_idiv64_inp_left |= data;
        } else if (temp_addr == ADDR_IDIV64_INP_LEFT_HI) {
            _mmio_idiv64_inp_left &= u64(u32(i32(-1l)));
            _mmio_idiv64_inp_left |= (u64(data) << 32u);
        } else if (temp_addr == ADDR_IDIV64_INP_RIGHT_LO) {
            _mmio_idiv64_inp_right &= u64(i64(-1ll) << 32u);
            _mmio_idiv64_inp_right |= data;
        } else if (temp_addr == ADDR_IDIV64_INP_RIGHT_HI) {
            _mmio_idiv64_inp_right &= u64(u32(i32(-1l)));
            _mmio_idiv64_inp_right |= (u64(data) << 32u);
        } else if (temp_addr > MEM_SIZE) {
            std::fprintf(
                stderr,
                "MeltedMoonDebugRiscvEmu::_bus_write(): "
                "invalid bus write: "
                "pc:%x data:%x temp_addr:%x byte_count:%lu\n",
                _pc, data, temp_addr, byte_count
            );
            std::exit(1);
        } else {
            if (
                temp_addr == ADDR_FB_END
                && byte_count == sizeof(u16)
            ) {
                _my_exec_one_instr_ret.sw_wrote_to_fb_end = (
                    &_mem[ADDR_FB_START]
                );
            }
            memcpy(&_mem[temp_addr], &data, byte_count);
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
    const u32 temp_addr = addr & ~0x8000000;
    if (
        byte_count == sizeof(u8)
        || byte_count == sizeof(u16)
        || byte_count == sizeof(u32)
    ) {
        if (temp_addr > MEM_SIZE) {
            if (temp_addr == ADDR_TIMER_USEC_LO) {
                _my_exec_one_instr_ret.sw_read_from_tp = true;
                i32 temp_usec = i32(_tp->tv_usec);
                memcpy(&ret, &temp_usec, byte_count);
            } else if (temp_addr == ADDR_TIMER_USEC_HI) {
                _my_exec_one_instr_ret.sw_read_from_tp = true;
                i32 temp_usec = i32(i64(_tp->tv_usec) >> 32u);
                memcpy(&ret, &temp_usec, byte_count);
            } else if (temp_addr == ADDR_TIMER_SEC_LO) {
                _my_exec_one_instr_ret.sw_read_from_tp = true;
                i32 temp_sec = i32(_tp->tv_sec);
                memcpy(&ret, &temp_sec, byte_count);
            } else if (temp_addr == ADDR_TIMER_SEC_HI) {
                _my_exec_one_instr_ret.sw_read_from_tp = true;
                i32 temp_sec = i32(i64(_tp->tv_sec) >> 32u);
                memcpy(&ret, &temp_sec, byte_count);
            } else if (temp_addr == ADDR_UDIV64_OUTP_QUOT_LO) {
                _mmio_udiv64_outp_quot = (
                    _mmio_udiv64_inp_left / _mmio_udiv64_inp_right
                );
                ret = u32(_mmio_udiv64_outp_quot);
            } else if (temp_addr == ADDR_UDIV64_OUTP_QUOT_HI) {
                _mmio_udiv64_outp_quot = (
                    _mmio_udiv64_inp_left / _mmio_udiv64_inp_right
                );
                ret = u32(_mmio_udiv64_outp_quot >> 32ul);
            } else if (temp_addr == ADDR_UDIV64_OUTP_REMA_LO) {
                _mmio_udiv64_outp_rema = (
                    _mmio_udiv64_inp_left % _mmio_udiv64_inp_right
                );
                ret = u32(_mmio_udiv64_outp_rema);
            } else if (temp_addr == ADDR_UDIV64_OUTP_REMA_HI) {
                _mmio_udiv64_outp_rema = (
                    _mmio_udiv64_inp_left % _mmio_udiv64_inp_right
                );
                ret = u32(_mmio_udiv64_outp_rema >> 32ul);
            } else if (temp_addr == ADDR_IDIV64_OUTP_QUOT_LO) {
                _mmio_idiv64_outp_quot = (
                    i64(_mmio_idiv64_inp_left)
                    / i64(_mmio_idiv64_inp_right)
                );
                ret = u32(_mmio_idiv64_outp_quot);
            } else if (temp_addr == ADDR_IDIV64_OUTP_QUOT_HI) {
                _mmio_idiv64_outp_quot = (
                    i64(_mmio_idiv64_inp_left)
                    / i64(_mmio_idiv64_inp_right)
                );
                ret = u32(_mmio_idiv64_outp_quot >> 32ul);
            } else if (temp_addr == ADDR_IDIV64_OUTP_REMA_LO) {
                _mmio_idiv64_outp_rema = (
                    i64(_mmio_idiv64_inp_left)
                    % i64(_mmio_idiv64_inp_right)
                );
                ret = u32(_mmio_idiv64_outp_rema);
            } else if (temp_addr == ADDR_IDIV64_OUTP_REMA_HI) {
                _mmio_idiv64_outp_rema = (
                    i64(_mmio_idiv64_inp_left)
                    % i64(_mmio_idiv64_inp_right)
                );
                ret = u32(_mmio_idiv64_outp_rema >> 32ul);
            } else {
                std::fprintf(
                    stderr,
                    "MeltedMoonDebugRiscvEmu::_bus_read(): "
                    "invalid bus read: "
                    "temp_addr:%x byte_count:%lu\n",
                    temp_addr, byte_count
                );
                std::exit(1);
            }
        } else {
            memcpy(&ret, &_mem[temp_addr], byte_count);
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
