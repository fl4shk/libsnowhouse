package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.sim._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._

import libcheesevoyage.general._
import libcheesevoyage.math._
import libcheesevoyage.bus.lcvStall._
import libcheesevoyage.bus.lcvBus._

import java.io._

object Riscv32Op {
  def mainWidth = 32
  def numGprs = 32
  def maxNumGprsPerInstr = 3

  def fieldWidth(
    fieldRange: (Int, Int)
  ): Int = {
    if (fieldRange._1 < fieldRange._2) {
      return (fieldRange._2 - fieldRange._1) + 1
    } else {
      return (fieldRange._1 - fieldRange._2) + 1
    }
  }
  def fieldHi(
    fieldRange: (Int, Int)
  ): Int = {
    if (fieldRange._1 < fieldRange._2) {
      return fieldRange._2
    } else {
      return fieldRange._1
    }
  }
  def fieldLo(
    fieldRange: (Int, Int)
  ): Int = {
    if (fieldRange._1 < fieldRange._2) {
      return fieldRange._1
    } else {
      return fieldRange._2
    }
  }
  //private var _opCnt: Int = 0
  //def mkOp(
  //  name: String,
  //  kind: (Int, Int),
  //  update: Boolean,
  //): (Int, (Int, Int), String) = {
  //  val ret = (_opCnt, kind, name)
  //  if (update) {
  //    _opCnt += 
  //  }
  //  ret
  //}
}

object Rv32RType {
  val fieldOpcode = (6, 0)
  val fieldRd = (11, 7)
  val fieldFunct3 = (14, 12)
  val fieldRs1 = (19, 15)
  val fieldRs2 = (24, 20)
  val fieldFunct7 = (31, 25)

  //val opcode = 0x33

  case class EncInstr(
  ) extends Bundle {
    val opcode = UInt(Riscv32Op.fieldWidth(fieldOpcode) bits)
    val rd = UInt(Riscv32Op.fieldWidth(fieldRd) bits)
    val funct3 = UInt(Riscv32Op.fieldWidth(fieldFunct3) bits)
    val rs1 = UInt(Riscv32Op.fieldWidth(fieldRs1) bits)
    val rs2 = UInt(Riscv32Op.fieldWidth(fieldRs2) bits)
    val funct7 = UInt(Riscv32Op.fieldWidth(fieldFunct7) bits)
  }

  case class OpFields(
    op: Int,
    f3: Int,
    f7: Int,
  ) {
  }

  object Op {
    //--------
    val AddRdRs1Rs2 = OpFields(op=0x33, f3=0x0, f7=0x00)
    val SubRdRs1Rs2 = OpFields(op=0x33, f3=0x0, f7=0x20)
    val XorRdRs1Rs2 = OpFields(op=0x33, f3=0x4, f7=0x00)
    val OrRdRs1Rs2 = OpFields(op=0x33, f3=0x6, f7=0x00)
    val AndRdRs1Rs2 = OpFields(op=0x33, f3=0x7, f7=0x00)
    val SllRdRs1Rs2 = OpFields(op=0x33, f3=0x1, f7=0x0)
    val SrlRdRs1Rs2 = OpFields(op=0x33, f3=0x5, f7=0x00)
    val SraRdRs1Rs2 = OpFields(op=0x33, f3=0x5, f7=0x20)
    val SltRdRs1Rs2 = OpFields(op=0x33, f3=0x2, f7=0x00)
    val SltuRdRs1Rs2 = OpFields(op=0x33, f3=0x3, f7=0x00)
    //--------
    // "M" extension
    val MulRdRs1Rs2 = OpFields(op=0x33, f3=0x0, f7=0x01)
    val MulhRdRs1Rs2 = OpFields(op=0x33, f3=0x1, f7=0x01)
    val MulhsuRdRs1Rs2 = OpFields(op=0x33, f3=0x2, f7=0x01)
    val MulhuRdRs1Rs2 = OpFields(op=0x33, f3=0x3, f7=0x01)
    val DivRdRs1Rs2 = OpFields(op=0x33, f3=0x4, f7=0x01)
    val DivuRdRs1Rs2 = OpFields(op=0x33, f3=0x5, f7=0x01)
    val RemRdRs1Rs2 = OpFields(op=0x33, f3=0x6, f7=0x01)
    val RemuRdRs1Rs2 = OpFields(op=0x33, f3=0x7, f7=0x01)
    //--------
    // "Zba" extension
    val Sh1addRdRs1Rs2 = OpFields(op=0x33, f3=0x2, f7=0x10)
    val Sh2addRdRs1Rs2 = OpFields(op=0x33, f3=0x4, f7=0x10)
    val Sh3addRdRs1Rs2 = OpFields(op=0x33, f3=0x6, f7=0x10)
    //--------
    // "Zicond" extension
    val CzeroEqzRdRs1Rs2 = OpFields(op=0x33, f3=0x5, f7=0x7)
    val CzeroNezRdRs1Rs2 = OpFields(op=0x33, f3=0x7, f7=0x7)
    //--------
  }

}

object Rv32IType {
  val fieldOpcode = (6, 0)
  val fieldRd = (11, 7)
  val fieldFunct3 = (14, 12)
  val fieldRs1 = (19, 15)
  val fieldImm11dt0 = (31, 20)

  //val opcode = 0x13

  case class EncInstr(
  ) extends Bundle {
    val opcode = UInt(Riscv32Op.fieldWidth(fieldOpcode) bits)
    val rd = UInt(Riscv32Op.fieldWidth(fieldRd) bits)
    val funct3 = UInt(Riscv32Op.fieldWidth(fieldFunct3) bits)
    val rs1 = UInt(Riscv32Op.fieldWidth(fieldRs1) bits)
    val imm11dt0 = UInt(Riscv32Op.fieldWidth(fieldImm11dt0) bits)

    def myTempImm(
    ): SInt = (
      imm11dt0.asSInt.resize(Riscv32Op.mainWidth)
    )

    def myImm11dt5(
    ): UInt = (
      imm11dt0(11 downto 5)
    )
    def myImm4dt0(
    ): UInt = (
      imm11dt0(4 downto 0).resize(Riscv32Op.mainWidth)
    )
  }

  case class OpFields(
    op: Int,
    f3: Int,
    imm11dt5: Option[Int]=None,
  ) {
  }

  object Op {
    //--------
    val AddiRdRs1Imm = OpFields(op=0x13, f3=0x0) //rd = rs1 + imm
    val XoriRdRs1Imm = OpFields(op=0x13, f3=0x4) //rd = rs1 ˆ imm
    val OriRdRs1Imm = OpFields(op=0x13, f3=0x6) //rd = rs1 | imm
    val AndiRdRs1Imm = OpFields(op=0x13, f3=0x7) //rd = rs1 & imm

    // rd = rs1 << imm[0:4]
    val SlliRdRs1Imm = OpFields(op=0x13, f3=0x1, imm11dt5=Some(0x00))

    // rd = rs1 >> imm[0:4]
    val SrliRdRs1Imm = OpFields(op=0x13, f3=0x5, imm11dt5=Some(0x00))

    // rd = rs1 >> imm[0:4] msb-extends
    val SraiRdRs1Imm = OpFields(op=0x13, f3=0x5, imm11dt5=Some(0x20))

    val SltiRdRs1Imm = OpFields(op=0x13, f3=0x2)
    val SltiuRdRs1Imm = OpFields(op=0x13, f3=0x3)
    //--------
    val LbRdRs1Imm = OpFields(op=0x03, f3=0x0)
    val LhRdRs1Imm = OpFields(op=0x03, f3=0x1)
    val LwRdRs1Imm = OpFields(op=0x03, f3=0x2)
    val LbuRdRs1Imm = OpFields(op=0x03, f3=0x4)
    val LhuRdRs1Imm = OpFields(op=0x03, f3=0x5)
    //--------
    // rd = PC+4; PC = rs1 + imm
    val JalrRdRs1Imm = OpFields(op=0x67, f3=0x0)
    //--------
  }

  //object Op {
  //  // (funct3, imm[11:5])
  //  //val AddiRaRbImm12 = (0, 0)
  //  //val SltiRaRbImm12 = (1, 0)
  //  //val SltiuRaRbImm12 = (2, 0)
  //  //val AndiRaRbImm12 = (3, 0)
  //  //val OriRaRbImm12 = (4, 0)
  //  //val XoriRaRbImm12 = (5, 0)

  //  //val SlliRaRbImm5 = (6, 0)
  //  //val SrliRaRbImm5 = (7, 0)
  //  //val SraiRaRbImm5 = (8, 1 << 5)
  //}
}

object Rv32SType {
  val fieldOpcode = (6, 0)
  val fieldImm4dt0 = (11, 7)
  val fieldFunct3 = (14, 12)
  val fieldRs1 = (19, 15)
  val fieldRs2 = (24, 20)
  val fieldImm11dt5 = (31, 25)

  case class EncInstr(
  ) extends Bundle {
    val opcode = UInt(Riscv32Op.fieldWidth(fieldOpcode) bits)
    val imm4dt0 = UInt(Riscv32Op.fieldWidth(fieldImm4dt0) bits)
    val funct3 = UInt(Riscv32Op.fieldWidth(fieldFunct3) bits)
    val rs1 = UInt(Riscv32Op.fieldWidth(fieldRs1) bits)
    val rs2 = UInt(Riscv32Op.fieldWidth(fieldRs2) bits)
    val imm11dt5 = UInt(Riscv32Op.fieldWidth(fieldImm11dt5) bits)

    def myTempImm(
    ): SInt = (
      Cat(
        imm11dt5,
        imm4dt0,
      ).asSInt.resize(Riscv32Op.mainWidth)
    )
  }

  case class OpFields(
    op: Int,
    f3: Int,
  ) {
  }

  object Op {
    val SbRs2Rs1Imm = OpFields(op=0x23, f3=0x0)
    val ShRs2Rs1Imm = OpFields(op=0x23, f3=0x1)
    val SwRs2Rs1Imm = OpFields(op=0x23, f3=0x2)
  }
}

object Rv32BType {
  val fieldOpcode = (6, 0)
  val fieldImm11dt11 = (7, 7)
  val fieldImm4dt1 = (11, 8)
  val fieldFunct3 = (14, 12)
  val fieldRs1 = (19, 15)
  val fieldRs2 = (24, 20)
  val fieldImm10dt5 = (30, 25)
  val fieldImm12dt12 = (31, 31)

  case class EncInstr(
  ) extends Bundle {
    val opcode = UInt(Riscv32Op.fieldWidth(fieldOpcode) bits)
    val imm11dt11 = UInt(Riscv32Op.fieldWidth(fieldImm11dt11) bits)
    val imm4dt1 = UInt(Riscv32Op.fieldWidth(fieldImm4dt1) bits)
    val funct3 = UInt(Riscv32Op.fieldWidth(fieldFunct3) bits)
    val rs1 = UInt(Riscv32Op.fieldWidth(fieldRs1) bits)
    val rs2 = UInt(Riscv32Op.fieldWidth(fieldRs2) bits)
    val imm10dt5 = UInt(Riscv32Op.fieldWidth(fieldImm10dt5) bits)
    val imm12dt12 = UInt(Riscv32Op.fieldWidth(fieldImm12dt12) bits)

    def myTempImm(
    ): SInt = (
      Cat(
        imm12dt12,
        imm11dt11,
        imm10dt5,
        imm4dt1,
        U"1'd0",
      ).asSInt.resize(Riscv32Op.mainWidth)
    )
  }

  case class OpFields(
    op: Int,
    f3: Int,
  ) {
  }

  object Op {
    val BeqRs1Rs2Imm = OpFields(op=0x63, f3=0x0)
    val BneRs1Rs2Imm = OpFields(op=0x63, f3=0x1)
    val BltRs1Rs2Imm = OpFields(op=0x63, f3=0x4)
    val BgeRs1Rs2Imm = OpFields(op=0x63, f3=0x5)
    val BltuRs1Rs2Imm = OpFields(op=0x63, f3=0x6)
    val BgeuRs1Rs2Imm = OpFields(op=0x63, f3=0x7)
  }
}

object Rv32JType {
  val fieldOpcode = (6, 0)
  val fieldRd = (11, 7)
  val fieldImm19dt12 = (19, 12)
  val fieldImm11dt11 = (20, 20)
  val fieldImm10dt1 = (30, 21)
  val fieldImm20dt20 = (31, 31)

  case class EncInstr(
  ) extends Bundle {
    val opcode = UInt(Riscv32Op.fieldWidth(fieldOpcode) bits)
    val rd = UInt(Riscv32Op.fieldWidth(fieldRd) bits)
    val imm19dt12 = UInt(Riscv32Op.fieldWidth(fieldImm19dt12) bits)
    val imm11dt11 = UInt(Riscv32Op.fieldWidth(fieldImm11dt11) bits)
    val imm10dt1 = UInt(Riscv32Op.fieldWidth(fieldImm10dt1) bits)
    val imm20dt20 = UInt(Riscv32Op.fieldWidth(fieldImm20dt20) bits)

    def myTempImm(
    ): SInt = (
      Cat(
        imm20dt20,
        imm19dt12,
        imm11dt11,
        imm10dt1,
        U"1'd0",
      ).asSInt.resize(Riscv32Op.mainWidth)
    )
  }

  case class OpFields(
    op: Int,
  ) {
  }

  object Op {
    // rd = PC+4; PC += imm
    val JalRdImm = OpFields(op=0x6f)
  }
}

object Rv32UType {
  val fieldOpcode = (6, 0)
  val fieldRd = (11, 7)
  val fieldImm = (31, 12)

  case class EncInstr(
  ) extends Bundle {
    val opcode = UInt(Riscv32Op.fieldWidth(fieldOpcode) bits)
    val rd = UInt(Riscv32Op.fieldWidth(fieldRd) bits)
    val imm = UInt(Riscv32Op.fieldWidth(fieldImm) bits)

    def myTempImm(
    ): SInt = (
      Cat(
        imm,
        U"12'd0",
      ).asSInt.resize(Riscv32Op.mainWidth)
    )
  }

  case class OpFields(
    op: Int,
  ) {
  }

  object Op {
    val LuiRdImm31Downto12 = OpFields(op=0x37)
    val AuipcRdImm31Downto12 = OpFields(op=0x17)
  }
}

object Riscv32OpInfoMap {
  val opInfoMap = LinkedHashMap[Any, OpInfo]()

  opInfoMap += (
    //--------
    Rv32RType.Op.AddRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.Add
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.ADD)
      ),
    )
  )

  opInfoMap += (
    Rv32RType.Op.SubRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.Sub
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.SUB)
      ),
    )
  )

  opInfoMap += (
    Rv32RType.Op.XorRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.Xor
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.XOR)
      ),
    )
  )

  opInfoMap += (
    Rv32RType.Op.OrRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.Or
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.OR)
      ),
    )
  )

  opInfoMap += (
    Rv32RType.Op.AndRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.And
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.AND)
      ),
    )
  )
  //--------
  // these are placed earlier for efficiency purposes of checking the
  // `kind` signal
  opInfoMap += (
    //--------
    Rv32RType.Op.MulRdRs1Rs2 -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Umul,
    )
  )

  opInfoMap += (
    Rv32RType.Op.MulhuRdRs1Rs2 -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr, DstKind.DontCare),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Umul,
    )
  )

  opInfoMap += (
    Rv32RType.Op.MulhRdRs1Rs2 -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr, DstKind.DontCare),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Smul,
    )
  )

  opInfoMap += (
    Rv32RType.Op.MulhsuRdRs1Rs2 -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr, DstKind.DontCare),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.SUmul,
    )
  )
  //--------
  opInfoMap += (
    Rv32RType.Op.DivuRdRs1Rs2 -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Udiv,
    )
  )

  opInfoMap += (
    Rv32RType.Op.DivRdRs1Rs2 -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Sdiv,
    )
  )

  opInfoMap += (
    Rv32RType.Op.RemuRdRs1Rs2 -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Umod,
    )
  )

  opInfoMap += (
    Rv32RType.Op.RemRdRs1Rs2 -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Smod,
    )
  )
  //--------
  opInfoMap += (
    //--------
    Rv32RType.Op.Sh1addRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.Lsl1Add
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.ADD)
      ),
    )
  )
  opInfoMap += (
    //--------
    Rv32RType.Op.Sh2addRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.Lsl2Add
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.ADD)
      ),
    )
  )
  opInfoMap += (
    //--------
    Rv32RType.Op.Sh3addRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.Lsl3Add
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.ADD)
      ),
    )
  )
  //--------
  opInfoMap += (
    //--------
    Rv32RType.Op.CzeroEqzRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.CzeroEqz
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.ADD)
      ),
    )
  )
  opInfoMap += (
    //--------
    Rv32RType.Op.CzeroNezRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.CzeroNez
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.ADD)
      ),
    )
  )
  //--------

  opInfoMap += (
    Rv32RType.Op.SllRdRs1Rs2 -> OpInfo./*mkAlu*//*mkAluShift*/mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      //aluShiftOp=AluShiftOpKind.Lsl,
      multiCycleOp=(
        MultiCycleOpKind.Lsl
      ),
      //aluOp=(
      //  AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.LSL)
      //)
    )
  )
  opInfoMap += (
    // rd = rs1 << imm[0:4]
    Rv32IType.Op.SlliRdRs1Imm -> OpInfo./*mkAlu*//*mkAluShift*/mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      //aluShiftOp=AluShiftOpKind.Lsl,
      multiCycleOp=(
        MultiCycleOpKind.Lsl
      ),
      //aluOp=(
      //  AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.LSL)
      //)
    )
  )
  opInfoMap += (
    Rv32RType.Op.SrlRdRs1Rs2 -> OpInfo./*mkAlu*//*mkAluShift*/mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      //aluShiftOp=AluShiftOpKind.Lsr,
      multiCycleOp=(
        MultiCycleOpKind.Lsr
      ),
      //aluOp=(
      //  AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.LSL)
      //)
    )
  )
  opInfoMap += (
    // rd = rs1 >> imm[0:4]
    Rv32IType.Op.SrliRdRs1Imm -> OpInfo./*mkAlu*//*mkAluShift*/mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      //aluShiftOp=AluShiftOpKind.Lsr,
      multiCycleOp=(
        MultiCycleOpKind.Lsr
      ),
      //aluOp=(
      //  AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.LSL)
      //)
    )
  )


  opInfoMap += (
    Rv32RType.Op.SraRdRs1Rs2 -> OpInfo./*mkAlu*//*mkAluShift*/mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      //aluShiftOp=AluShiftOpKind.Asr,
      multiCycleOp=(
        MultiCycleOpKind.Asr
      ),
      //aluOp=(
      //  AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.LSL)
      //)
    )
  )

  opInfoMap += (
    // rd = rs1 >> imm[0:4] msb-extends
    Rv32IType.Op.SraiRdRs1Imm -> OpInfo./*mkAlu*//*mkAluShift*/mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      //aluShiftOp=AluShiftOpKind.Asr,
      multiCycleOp=(
        MultiCycleOpKind.Asr
      ),
      //aluOp=(
      //  AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.LSL)
      //)
    )
  )

  opInfoMap += (
    Rv32RType.Op.SltRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.Slts
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.SLTS)
      ),
    )
  )

  opInfoMap += (
    Rv32RType.Op.SltuRdRs1Rs2 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=(
        AluOpKind.Sltu
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.SLTU)
      ),
    )
  )

  //--------
  opInfoMap += (
    Rv32IType.Op.AddiRdRs1Imm -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      aluOp=(
        AluOpKind.Add
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.ADD)
      ),
    )
  )

  opInfoMap += (
    Rv32IType.Op.XoriRdRs1Imm -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      aluOp=(
        AluOpKind.Xor
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.XOR)
      ),
    )
  )

  opInfoMap += (
    Rv32IType.Op.OriRdRs1Imm -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      aluOp=(
        AluOpKind.Or
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.OR)
      ),
    )
  )

  opInfoMap += (
    Rv32IType.Op.AndiRdRs1Imm -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      aluOp=(
        AluOpKind.And
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.AND)
      ),
    )
  )

  opInfoMap += (
    Rv32IType.Op.SltiRdRs1Imm -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      aluOp=(
        AluOpKind.Slts
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.SLTS)
      ),
    )
  )

  opInfoMap += (
    Rv32IType.Op.SltiuRdRs1Imm -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      aluOp=(
        AluOpKind.Sltu
        //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.SLTU)
      ),
    )
  )

  opInfoMap += (
    Rv32IType.Op.LbRdRs1Imm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem8(isSigned=true, isStore=false),
    )
  )

  opInfoMap += (
    Rv32IType.Op.LhRdRs1Imm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem16(isSigned=true, isStore=false),
    )
  )

  opInfoMap += (
    Rv32IType.Op.LwRdRs1Imm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      modify=MemAccessKind.Mem32(isSigned=false, isStore=false),
    )
  )

  opInfoMap += (
    Rv32IType.Op.LbuRdRs1Imm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem8(isSigned=false, isStore=false),
    )
  )

  opInfoMap += (
    Rv32IType.Op.LhuRdRs1Imm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem16(isSigned=false, isStore=false),
    )
  )

  opInfoMap += (
    // rd = PC+4; PC = rs1 + imm
    Rv32IType.Op.JalrRdRs1Imm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc, DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      cpyOp=CpyOpKind.Jmp,
    )
  )
  //--------
  opInfoMap += (
    Rv32SType.Op.SbRs2Rs1Imm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem8(isSigned=false, isStore=true),
    )
  )

  opInfoMap += (
    Rv32SType.Op.ShRs2Rs1Imm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem16(isSigned=false, isStore=true),
    )
  )

  opInfoMap += (
    Rv32SType.Op.SwRs2Rs1Imm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      modify=MemAccessKind.Mem32(isSigned=false, isStore=true),
    )
  )
  //--------
  opInfoMap += (
    Rv32BType.Op.BeqRs1Rs2Imm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Eq,
    )
  )

  opInfoMap += (
    Rv32BType.Op.BneRs1Rs2Imm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Ne
    )
  )

  opInfoMap += (
    Rv32BType.Op.BltRs1Rs2Imm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Lts
    )
  )

  opInfoMap += (
    Rv32BType.Op.BgeRs1Rs2Imm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Ges
    )
  )

  opInfoMap += (
    Rv32BType.Op.BltuRs1Rs2Imm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Ltu
    )
  )

  opInfoMap += (
    Rv32BType.Op.BgeuRs1Rs2Imm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Geu
    )
  )
  //--------

  opInfoMap += (
    // rd = PC+4; PC += imm
    Rv32JType.Op.JalRdImm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc, DstKind.Gpr),
      srcArr=Array[SrcKind](
        SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
    )
  )
  //--------
  opInfoMap += (
    Rv32UType.Op.LuiRdImm31Downto12 -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](
        SrcKind.Imm()
      ),
      cpyOp=CpyOpKind.Cpy
    )
  )
  opInfoMap += (
    Rv32UType.Op.AuipcRdImm31Downto12 -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Pc, SrcKind.Imm(/*Some(true)*/)),
      //aluOp=(
      //  AluOpKind.Add
      //  //AluOpKind.LcvAlu(LcvAluDel1InpOpEnum.ADD)
      //),
      multiCycleOp=(
        MultiCycleOpKind.AddRaPcImm
      )
    )
  )
}
object SnowHouseRiscv32PipeStageInstrDecode {
  def apply(
    psId: SnowHousePipeStageInstrDecode
  ) = new Area {
    import Rv32RType.Op._
    import Rv32IType.Op._
    import Rv32SType.Op._
    import Rv32BType.Op._
    import Rv32JType.Op._
    import Rv32UType.Op._

    def upPayload = psId.upPayload(1)
    def myTempBtbElem = psId.myTempBtbElem
    myTempBtbElem := (
      myTempBtbElem.getZero
    )
    myTempBtbElem.dstRegPc.allowOverride

    def io = psId.io
    def cfg = psId.cfg
    def cId = psId.cId

    def myEncInstrSize = 2
    val encInstrR = Vec.fill(myEncInstrSize)(Rv32RType.EncInstr())
    val encInstrI = Vec.fill(myEncInstrSize)(Rv32IType.EncInstr())
    val encInstrS = Vec.fill(myEncInstrSize)(Rv32SType.EncInstr())
    val encInstrB = Vec.fill(myEncInstrSize)(Rv32BType.EncInstr())
    val encInstrJ = Vec.fill(myEncInstrSize)(Rv32JType.EncInstr())
    val encInstrU = Vec.fill(myEncInstrSize)(Rv32UType.EncInstr())

    for (idx <- 0 until myEncInstrSize) {
      encInstrR(idx).assignFromBits(psId.myInstr.asBits)
      encInstrI(idx).assignFromBits(psId.myInstr.asBits)
      encInstrS(idx).assignFromBits(psId.myInstr.asBits)
      encInstrB(idx).assignFromBits(psId.myInstr.asBits)
      encInstrJ(idx).assignFromBits(psId.myInstr.asBits)
      encInstrU(idx).assignFromBits(psId.myInstr.asBits)
    }

    val myNonScoreboardArea = (
      !cfg.optScoreboard
    ) generate (new Area {
      val tempHaveHazardAddrCheckVec = (
        Vec.fill(upPayload.myDoHaveHazardAddrCheckVec.size)(
          Vec.fill(
            // old "scoreboard" code: //upPayload.gprIdxVec.size + 2
            upPayload.gprIdxVec.size - 1
          )(
            Bool()
          )
        )
      )
      val myHistLastGprIdx = (
        History(
          that=(
            upPayload.gprIdxVec
          ),
          length=(
            tempHaveHazardAddrCheckVec.size + 1
          ),
          when=(
            //psId.up.isFiring
            //psId.down.isFiring
            psId.myNonBubbleCond
          ),
          init=(
            //encInstr.last.raIdx.getZero
            upPayload.gprIdxVec.getZero
          )
        )
      )
      for (
        // old "scoreboard" code:
        //idx <- 0 until upPayload.gprIdxVec.size + 2
        idx <- 0 until upPayload.gprIdxVec.size - 1
      ) {
        //if (idx < upPayload.gprIdxVec.size - 1) {
          val tempRegIdx: UInt = (
            if (idx == 0) {
              //encInstrR.head.rd
              encInstrR.head.rs1
            } else if (idx == 1) {
              //encInstrR.head.rs1
              encInstrR.head.rs2
            }
            //else if (idx == 2) 
            //{
            //  //encInstrR.head.rs2
            //} 
            else {
              assert(
                false,
                s"${idx} ${upPayload.gprIdxVec.size - 1}"
              )
              encInstrR.head.rd
            }
          )
          for (jdx <- 0 until tempHaveHazardAddrCheckVec.size) {
            tempHaveHazardAddrCheckVec(jdx)(idx) := (
              (
                tempRegIdx === myHistLastGprIdx(jdx + 1).last
                && tempRegIdx.orR // check for non-zero
              )
              //|| (
              //  tempRegIdx === 3
              //)
            )
          }
        //} else { // if (idx >= upPayload.gprIdxVec.size - 1)
        //  val tempRegIdx = encInstrR.head.rd
        //  for (jdx <- 0 until tempHaveHazardAddrCheckVec.size) {
        //    tempHaveHazardAddrCheckVec(jdx)(idx) := (
        //      (
        //        tempRegIdx === myHistLastGprIdx(jdx + 1)(idx % 3)
        //        && tempRegIdx.orR // check for non-zero
        //      )
        //    )
        //  }
        //}
      }
      for (
        jdx <- 0 until tempHaveHazardAddrCheckVec.size
      ) {
        upPayload.myDoHaveHazardAddrCheckVec(jdx) := (
          tempHaveHazardAddrCheckVec(jdx).reduceLeft(_ || _)
        )
      }
    })
    when (upPayload.instrCnt.myPsIdBubble.head) {
      //encInstr.last := encInstr.last.getZero

      //for (idx <- 0 until myEncInstrSize) {
        encInstrR.last := encInstrR.last.getZero
        encInstrI.last := encInstrI.last.getZero
        encInstrS.last := encInstrS.last.getZero
        encInstrB.last := encInstrB.last.getZero
        encInstrJ.last := encInstrJ.last.getZero
        encInstrU.last := encInstrU.last.getZero
      //}
    }
    for (idx <- 0 until cfg.maxNumGprsPerInstr) {
      for (jdx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
        upPayload.gprIsZeroVec(idx)(jdx) := (
          upPayload.gprIdxVec(idx) === 0x0
          || upPayload.instrCnt.myPsIdBubble.last
        )
      }
      for (jdx <- 0 until cfg.regFileCfg.modMemWordValidSize + 1) {
        upPayload.gprIsNonZeroVec(idx)(jdx) := (
          upPayload.gprIdxVec(idx) =/= 0x0
          && !upPayload.instrCnt.myPsIdBubble.last
        )
      }
    }

    upPayload.gprIdxVec(0) := encInstrR.last.rs1.resized
    upPayload.gprIdxVec(1) := encInstrR.last.rs2.resized
    //upPayload.gprIdxVec(2) := encInstrR.last.rd.resized
    upPayload.gprIdxVec.last := encInstrR.last.rd.resized

    upPayload.splitOp.setToDefault()
    upPayload.splitOp.opIsDualWidth := False
    upPayload.aluOp := (
      LcvAluDel1InpOpEnum.ZERO
      //LcvAluDel1InpOpEnum.OP_GET_INP_A
    )
    upPayload.aluOp.allowOverride

    val mySplitOp = upPayload.splitOp

    psId.myTempOpIsMemAccessLoad := (
      encInstrI.head.opcode === LwRdRs1Imm.op
    )
    psId.myTempOpIsMemAccessStore := (
      //False
      encInstrS.head.opcode === SwRs2Rs1Imm.op
    )
    psId.myTempOpMayNeedHazardCheck := (
      encInstrI.head.opcode === LwRdRs1Imm.op
      //|| encInstrS.head.opcode === SwRs2Rs1Imm.op
      ////|| upPayload.splitOp.opIsMultiCycle
      //|| encInstrU.head.opcode === AuipcRdImm31Downto12.op
      //// this is also updated later...
    )
    psId.myTempOpIsJmpBr := (
      encInstrB.head.opcode === BeqRs1Rs2Imm.op
      // please excuse the use of `encInstrB` for `Jal` and `Jalr`!
      || encInstrB.head.opcode === JalRdImm.op
      || encInstrB.head.opcode === JalrRdRs1Imm.op
    )
    psId.myTempOpIsDualWidth := False

    def setImm(
      encInstr: Any,
      haveShiftImm5: Boolean=false,
    ): Area = new Area {
      if (
        encInstr == encInstrR.last
      ) {
        upPayload.imm.foreach(item => {
          item := 0x0 //encInstrR.last.myTempImm()
        })
      } else if (
        encInstr == encInstrI.last
      ) {
        upPayload.imm.foreach(item => {
          item := (
            if (!haveShiftImm5) (
              encInstrI.last.myTempImm().asUInt
            ) else (
              encInstrI.last.myImm4dt0()
            )
          )
        })
      } else if (
        encInstr == encInstrS.last
      ) {
        //psId.myTempOpIsMemAccessStore := True
        upPayload.imm.foreach(item => {
          item := encInstrS.last.myTempImm().asUInt
        })
      } else if (
        encInstr == encInstrB.last
      ) {
        upPayload.imm.foreach(item => {
          item := encInstrB.last.myTempImm().asUInt
        })
      } else if (
        encInstr == encInstrJ.last
      ) {
        upPayload.imm.foreach(item => {
          item := encInstrJ.last.myTempImm().asUInt
        })
      } else if (
        encInstr == encInstrU.last
      ) {
        upPayload.imm.foreach(item => {
          item := encInstrU.last.myTempImm().asUInt
        })
      } else {
        require(
          false
        )
      }
    }

    def setOp(
      someOp: Any,
      encInstr: Any,
    ): Area = new Area {
      //--------
      val tempHaveJal = (
        someOp == JalRdImm
      )
      val tempHaveJalr = (
        someOp == JalrRdRs1Imm
      )
      val tempHaveBrCond = (
        encInstr == encInstrB.last
      )

      //val tempHavePredictableJmpBr = (
      //  tempHaveJal
      //  //|| tempHaveJalr
      //  || tempHaveBrCond
      //)
      //--------
      val mySplitOp = upPayload.splitOp

      val tempBtbElemWithBrKindArea = new Area {
        val temp = BranchTgtBufElemWithBrKind(cfg=cfg)

        temp.btbElem.srcRegPc := temp.btbElem.srcRegPc.getZero

        if (
          tempHaveJal
          || tempHaveBrCond
        ) {
          temp.btbElem.valid := upPayload.imm(0).msb 
          if (tempHaveBrCond) {
            temp.branchKind.assignFromBits(
              // this is only for `FwdNotTknBakTknEnum`!
              Cat(upPayload.imm(0).msb).asUInt
              .resize(temp.branchKind.getWidth).asBits
            )
          } else {
            temp.branchKind.assignFromBits(
              // this is only for `FwdNotTknBakTknEnum`!
              Cat(True).asUInt
              .resize(temp.branchKind.getWidth).asBits
            )
          }
          temp.btbElem.dontPredict := False
        } else if (tempHaveJalr) {
          //temp.btbElem.valid := False
          //temp.branchKind := temp.branchKind.getZero
          //temp.btbElem.dontPredict := True
          temp.btbElem.valid := True
          temp.branchKind.assignFromBits(
            // this is only for `FwdNotTknBakTknEnum`!
            Cat(True).asUInt
            .resize(temp.branchKind.getWidth).asBits
          )
          temp.btbElem.dontPredict := False
        } else {
          temp := temp.getZero
        }

        if (tempHaveBrCond) {
          mySplitOp.exSetNextPcKind := (
            SnowHousePsExSetNextPcKind.PcPlusImm
          )
        } else if (tempHaveJal) {
          mySplitOp.exSetNextPcKind := (
            SnowHousePsExSetNextPcKind.PcPlusImm
          )
        } else if (tempHaveJalr) {
          mySplitOp.exSetNextPcKind := (
            SnowHousePsExSetNextPcKind.RdMemWordPlusImm
          )
        } else {
          mySplitOp.exSetNextPcKind := (
            SnowHousePsExSetNextPcKind.Dont
          )
        }
      }

      var found = false
      var didFirstPrint: Boolean = false
      //print(
      //  someOp
      //)
      for (
        ((tuple, opInfo), opInfoIdx)
        <- cfg.opInfoMap.view.zipWithIndex
      ) {
        if (someOp == tuple) {
          for (
            ((_, jmpOpInfo), jmpOpInfoIdx)
            <- cfg.jmpBrAlwaysEqNeOpInfoMap.view.zipWithIndex
          ) {
            if (
              //opInfo == jmpOpInfo
              //someOp == jmpOpTuple
              jmpOpInfo == opInfo
            ) {
              //println(
              //  s"jmpBrAlwaysEqNeOp: "
              //    // "${opInfoIdx} -> ${jmpOpInfoIdx} "
              //  + s"${someOp._3} // ${jmpOpInfoIdx}"
              //)
              mySplitOp.jmpBrAlwaysEqNeOp := (
                jmpOpInfoIdx
                //1 << jmpOpInfoIdx
              )
              found = true
            }
          }
          for (
            ((_, jmpOpInfo), jmpOpInfoIdx)
            <- cfg.jmpBrOtherOpInfoMap.view.zipWithIndex
          ) {
            if (jmpOpInfo == opInfo) {
              //println(
              //  s"jmpBrOtherOp: "
              //    // "${opInfoIdx} -> ${jmpOpInfoIdx} "
              //  + s"${someOp._3} // ${jmpOpInfoIdx}"
              //)
              mySplitOp.jmpBrOtherOp := (
                //jmpOpInfoIdx
                1 << jmpOpInfoIdx
              )
              found = true
            }
          }
          //for (
          //  ((_, aluShiftOpInfo), aluShiftOpInfoIdx)
          //  <- cfg.aluShiftOpInfoMap.view.zipWithIndex
          //) {
          //  if (aluShiftOpInfo == opInfo) {
          //    if (
          //      //opInfo == aluShiftOpInfo
          //      //someOp == aluShiftOpTuple
          //      aluShiftOpInfo == opInfo
          //    ) {
          //      println(
          //        s"aluShiftOp: " //"${opInfoIdx} -> ${aluShiftOpInfoIdx} "
          //        + s"${someOp._3} // ${aluShiftOpInfoIdx}"
          //      )
          //      mySplitOp.aluShiftOp := (
          //        aluShiftOpInfoIdx
          //      )
          //      //when (
          //      //  !rDoAluShiftPost
          //      //  && psId.up.isFiring
          //      //) {
          //      //  rDoAluShiftPost := True
          //      //  psId.nextMultiInstrCnt := 0x2
          //      //}
          //      found = true
          //    }
          //  }
          //}
          for (
            ((_, cpyCpyuiAluNonShiftOpInfo), cpyCpyuiAluNonShiftOpInfoIdx)
            <- cfg.cpyCpyuiAluNonShiftOpInfoMap.view.zipWithIndex
          ) {
            if (cpyCpyuiAluNonShiftOpInfo == opInfo) {
              assert(
                !found
              )
              //mySplitOp.nonMultiCycleOp := nonMultiCycleNonJmpOpInfoIdx
              if (!cfg.allAluOpsUseLcvAluDel1) {
                //println(
                //  s"cfg.allAluOpsUseLcvAluDel1:"
                //  + s"${cfg.allAluOpsUseLcvAluDel1}"
                //)
                mySplitOp.cpyCpyuiAluNonShiftOp := (
                  //cpyCpyuiAluNonShiftOpInfoIdx
                  1 << cpyCpyuiAluNonShiftOpInfoIdx
                )
              }
              //println(
              //  s"test: ${cpyCpyuiAluNonShiftOpInfoIdx}"
              //)
              //found = true
              for (
                ((_, memAccOpInfo), memAccOpInfoIdx)
                <- cfg.memAccOpInfoMap.view.zipWithIndex
              ) {
                if (
                  memAccOpInfo == opInfo
                ) {
                  mySplitOp.opIsMemAccess := True
                  memAccOpInfo.memAccess match {
                    case MemAccessKind.NoMemAccess => {
                    }
                    case mem: MemAccessKind.Mem => {
                      val isStore = mem.isStore
                      if (!isStore) {
                        val tempMemAccessKind = (
                          if (!mem.isSigned) (
                            SnowHouseMemAccessKind.LoadU
                          ) else (
                            SnowHouseMemAccessKind.LoadS
                          )
                        )
                        upPayload.inpDecodeExt.foreach(item => {
                          item.memAccessKind := (
                            tempMemAccessKind
                          )
                        })
                      } else { // if (isStore)
                        val tempMemAccessKind = (
                          SnowHouseMemAccessKind.Store
                        )
                        upPayload.inpDecodeExt.foreach(item => {
                          item.memAccessKind := (
                            tempMemAccessKind
                          )
                        })
                      }
                      for (idx <- 0 until upPayload.inpDecodeExt.size) {
                        //val tempSubKind = SnowHouseMemAccessSubKind()
                        def mySubKind = (
                          upPayload.inpDecodeExt(idx).memAccessSubKind
                        )
                        def myMemAccessIsLtWordSize = (
                          upPayload.inpDecodeExt(
                            idx
                          ).memAccessIsLtWordWidth
                        )
                        def myMemAccessLcvDbusByteSize = (
                          upPayload.inpDecodeExt(
                            idx
                          ).memAccessLcvDbusByteSize
                        )

                        //if (idx == 0 || !isStore) {
                          mem.subKind match {
                            case MemAccessKind.SubKind.Sz8 => {
                              mySubKind := (
                                SnowHouseMemAccessSubKind.Sz8
                              )
                              myMemAccessIsLtWordSize := True
                              myMemAccessLcvDbusByteSize := 0x0
                            }
                            case MemAccessKind.SubKind.Sz16 => {
                              mySubKind := (
                                SnowHouseMemAccessSubKind.Sz16
                              )
                              myMemAccessIsLtWordSize := True
                              myMemAccessLcvDbusByteSize := 0x1
                            }
                            case MemAccessKind.SubKind.Sz32 => {
                              mySubKind := (
                                SnowHouseMemAccessSubKind.Sz32
                              )
                              myMemAccessIsLtWordSize := False
                              myMemAccessLcvDbusByteSize := 0x2
                            }
                            case MemAccessKind.SubKind.Sz64 => {
                              mySubKind := (
                                SnowHouseMemAccessSubKind.Sz64
                              )
                              myMemAccessIsLtWordSize := False
                              myMemAccessLcvDbusByteSize := 0x3
                            }
                          }
                        //} else {
                        //  SnowHouseMemAccessSubKind.Sz32
                        //}
                        //upPayload.inpDecodeExt(idx).memAccessSubKind := (
                        //  tempSubKind
                        //)
                        //upPayload.inpDecodeExt.foreach(item => {
                        //  item.memAccessSubKind := (
                        //    tempSubKind
                        //  )
                        //})
                      }
                    }
                  }
                }
              }
              for (
                ((_, cpyOpInfo), cpyOpInfoIdx)
                <- cfg.cpyCpyuiOpInfoMap.view.zipWithIndex
              ) {
                if (
                  //nonMultiCycleNonJmpOpInfo == cpyOpInfo
                  //someOp == cpyOpTuple
                  cpyOpInfo == opInfo
                ) {
                  //println(
                  //  //s"pureCpyOp (${cpyOpInfoIdx}): "
                  //  //+ s"${opInfoIdx}: ${someOp._3}"
                  //  s"cpyCpyuiOp: " //"${opInfoIdx} -> ${cpyOpInfoIdx} "
                  //  + s"${someOp._3} // ${cpyCpyuiAluNonShiftOpInfoIdx}"
                  //)
                  if (cfg.allAluOpsUseLcvAluDel1) {
                    mySplitOp.cpyCpyuiOp := (
                      //cpyOpInfoIdx
                      1 << cpyOpInfoIdx
                    )
                  }
                  found = true
                }
              }
              for (
                ((_, aluOpInfo), aluOpInfoIdx)
                <- cfg.aluOpInfoMap.view.zipWithIndex
              ) {
                if (
                  //opInfo == aluOpInfo
                  //someOp == aluOpTuple
                  aluOpInfo == opInfo
                ) {
                  //println(
                  //  s"aluOp: " //"${opInfoIdx} -> ${aluOpInfoIdx} "
                  //  + s"${someOp._3} // ${cpyCpyuiAluNonShiftOpInfoIdx}"
                  //)
                  aluOpInfo.aluOp.get match {
                    case AluOpKind.LcvAlu(aluOp) => {
                      upPayload.aluOp := aluOp
                      aluOpInfo.srcArr(1) match {
                        case SrcKind.Gpr => {
                          upPayload.aluInpBIsImm := False
                        }
                        case SrcKind.Imm() => {
                          upPayload.aluInpBIsImm := True
                        }
                        case _ => {
                          assert(
                            false,
                            "eek!"
                          )
                        }
                      }
                    }
                    case _ => {
                    }
                  }
                  found = true
                }
              }
              //for (
              //  ((_, aluShiftOpInfo), aluShiftOpInfoIdx)
              //  <- cfg.aluShiftOpInfoMap.view.zipWithIndex
              //) {
              //  if (
              //    //opInfo == aluShiftOpInfo
              //    //someOp == aluShiftOpTuple
              //    aluShiftOpInfo == opInfo
              //  ) {
              //    println(
              //      s"aluShiftOp: " //"${opInfoIdx} -> ${aluShiftOpInfoIdx} "
              //      + s"${someOp._3} // ${cpyCpyuiAluNonShiftOpInfoIdx}"
              //    )
              //    found = true
              //  }
              //}
            }
          }
          for (
            ((group, innerMap), groupIdx)
            <- cfg.multiCycleOpInfoMap.view.zipWithIndex
          ) {
            for (
              ((_, multiCycleOpInfo), multiCycleOpInfoIdx)
              <- innerMap.view.zipWithIndex
            ) {
              if (
                //opInfo == multiCycleOpInfo
                //someOp == tuple
                multiCycleOpInfo == opInfo
              ) {
                //println(
                //  s"multiCycleOp: " // ${opInfoIdx} -> ${multiCycleOpInfoIdx} "
                //  + s"${someOp._3} // ${multiCycleOpInfoIdx}"
                //)
                ////upPayload.op := opInfoIdx
                ////mySplitOp.multiCycleOp.valid := True
                //mySplitOp.kind := SnowHouseSplitOpKind.MULTI_CYCLE
                mySplitOp.opIsMultiCycle := True
                mySplitOp.multiCycleOpGroup := (
                  1 << groupIdx 
                )
                mySplitOp.multiCycleOpKind := (
                  multiCycleOpInfoIdx
                )
                ////return
                found = true
              }
            }
          }
        }
      }

      if (tempHaveJal || tempHaveJalr || tempHaveBrCond) {
        when (cId.up.isFiring) {
          myTempBtbElem := (
            tempBtbElemWithBrKindArea.temp.btbElem
          )
          upPayload.btbElemBranchKind(1) := (
            tempBtbElemWithBrKindArea.temp.branchKind
          )
        }
      }

    }

    switch (encInstrR.last.opcode) {
      is (AddRdRs1Rs2.op) {
        switch (encInstrR.last.funct7) {
          is (AddRdRs1Rs2.f7) {
            switch (encInstrR.last.funct3) {
              is (AddRdRs1Rs2.f3) {
                setOp(AddRdRs1Rs2, encInstrR.last)
              }
              is (XorRdRs1Rs2.f3) {
                setOp(XorRdRs1Rs2, encInstrR.last)
              }
              is (OrRdRs1Rs2.f3) {
                setOp(OrRdRs1Rs2, encInstrR.last)
              }
              is (AndRdRs1Rs2.f3) {
                setOp(AndRdRs1Rs2, encInstrR.last)
              }
              is (SllRdRs1Rs2.f3) {
                setOp(SllRdRs1Rs2, encInstrR.last)
              }
              is (SrlRdRs1Rs2.f3) {
                setOp(SrlRdRs1Rs2, encInstrR.last)
              }
              is (SltRdRs1Rs2.f3) {
                setOp(SltRdRs1Rs2, encInstrR.last)
              }
              is (SltuRdRs1Rs2.f3) {
                setOp(SltuRdRs1Rs2, encInstrR.last)
              }
            }
          }
          is (SubRdRs1Rs2.f7) {
            when (!encInstrR.last.funct3.orR) {
              setOp(SubRdRs1Rs2, encInstrR.last)
            } otherwise {
              setOp(SraRdRs1Rs2, encInstrR.last)
            }
          }
          is (Sh1addRdRs1Rs2.f7) {
            // "Zba" extension
            switch (encInstrR.last.funct3) {
              is (Sh1addRdRs1Rs2.f3) {
                setOp(Sh1addRdRs1Rs2, encInstrR.last)
              }
              is (Sh2addRdRs1Rs2.f3) {
                setOp(Sh2addRdRs1Rs2, encInstrR.last)
              }
              //is (Sh3addRdRs1Rs2.f3) 
              default {
                setOp(Sh3addRdRs1Rs2, encInstrR.last)
              }
            }
          }
          is (CzeroEqzRdRs1Rs2.f7) {
            // "Zicond" extension
            when (!encInstrR.last.funct3(1)) {
              setOp(CzeroEqzRdRs1Rs2, encInstrR.last)
            } otherwise {
              setOp(CzeroNezRdRs1Rs2, encInstrR.last)
            }
          }
          default {
            // assume it's one of the `M` extension instructions
            //psId.myTempOpMayNeedHazardCheck := True
            switch (encInstrR.last.funct3) {
              is (MulRdRs1Rs2.f3) {
                setOp(MulRdRs1Rs2, encInstrR.last)
              }
              is (MulhRdRs1Rs2.f3) {
                setOp(MulhRdRs1Rs2, encInstrR.last)
              }
              is (MulhsuRdRs1Rs2.f3) {
                setOp(MulhsuRdRs1Rs2, encInstrR.last)
              }
              is (MulhuRdRs1Rs2.f3) {
                setOp(MulhuRdRs1Rs2, encInstrR.last)
              }
              is (DivRdRs1Rs2.f3) {
                setOp(DivRdRs1Rs2, encInstrR.last)
              }
              is (DivuRdRs1Rs2.f3) {
                setOp(DivuRdRs1Rs2, encInstrR.last)
              }
              is (RemRdRs1Rs2.f3) {
                setOp(RemRdRs1Rs2, encInstrR.last)
              }
              is (RemuRdRs1Rs2.f3) {
                setOp(RemuRdRs1Rs2, encInstrR.last)
              }
            }
          }
        }
      }
      is (AddiRdRs1Imm.op) {
        setImm(encInstr=encInstrI.last)
        upPayload.gprIdxVec(1) := 0x0

        switch (encInstrI.last.funct3) {
          is (AddiRdRs1Imm.f3) {
            setOp(AddiRdRs1Imm, encInstrI.last)
          }
          is (XoriRdRs1Imm.f3) {
            setOp(XoriRdRs1Imm, encInstrI.last)
          }
          is (OriRdRs1Imm.f3) {
            setOp(OriRdRs1Imm, encInstrI.last)
          }
          is (AndiRdRs1Imm.f3) {
            setOp(AndiRdRs1Imm, encInstrI.last)
          }

          // rd = rs1 << imm[0:4]
          is (SlliRdRs1Imm.f3) {
            setImm(encInstr=encInstrI.last, haveShiftImm5=true)
            setOp(SlliRdRs1Imm, encInstrI.last)
          }

          is (SrliRdRs1Imm.f3) {
            setImm(encInstr=encInstrI.last, haveShiftImm5=true)
            when (!encInstrI.last.myImm11dt5().orR) {
              // rd = rs1 >> imm[0:4]
              setOp(SrliRdRs1Imm, encInstrI.last)
            } otherwise {
              // rd = rs1 >> imm[0:4] msb-extends
              setOp(SraiRdRs1Imm, encInstrI.last)
            }
          }

          is (SltiRdRs1Imm.f3) {
            setOp(SltiRdRs1Imm, encInstrI.last)
          }
          is (SltiuRdRs1Imm.f3) {
            setOp(SltiuRdRs1Imm, encInstrI.last)
          }
        }
      }
      is (LbRdRs1Imm.op) {
        setImm(encInstr=encInstrI.last)
        upPayload.gprIdxVec(1) := 0x0

        switch (encInstrI.last.funct3) {
          is (LbRdRs1Imm.f3) {
            setOp(LbRdRs1Imm, encInstrI.last)
          }
          is (LhRdRs1Imm.f3) {
            setOp(LhRdRs1Imm, encInstrI.last)
          }
          is (LwRdRs1Imm.f3) {
            setOp(LwRdRs1Imm, encInstrI.last)
          }
          is (LbuRdRs1Imm.f3) {
            setOp(LbuRdRs1Imm, encInstrI.last)
          }
          is (LhuRdRs1Imm.f3) {
            setOp(LhuRdRs1Imm, encInstrI.last)
          }
        }
        upPayload.gprIdxVec.last := encInstrR.last.rd
        upPayload.gprIdxVec(0) := encInstrR.last.rs1
        upPayload.gprIdxVec(1) := 0x0
      }
      is (JalrRdRs1Imm.op) {
        setImm(encInstr=encInstrI.last)
        upPayload.gprIdxVec(1) := 0x0
        setOp(JalrRdRs1Imm, encInstrI.last)
      }
      is (SbRs2Rs1Imm.op) {
        setImm(encInstr=encInstrS.last)
        switch (encInstrS.last.funct3) {
          is (SbRs2Rs1Imm.f3) {
            setOp(SbRs2Rs1Imm, encInstrS.last)
          }
          is (ShRs2Rs1Imm.f3) {
            setOp(ShRs2Rs1Imm, encInstrS.last)
          }
          is (SwRs2Rs1Imm.f3) {
            setOp(SwRs2Rs1Imm, encInstrS.last)
          }
        }
        //upPayload.gprIdxVec.last := 0x0
        //if (cfg.optScoreboard) {
        //  upPayload.gprIdxVec.last := encInstrR.last.rs2
        //} else {
          upPayload.gprIdxVec.last := 0x0
        //}
        upPayload.gprIdxVec(0) := encInstrR.last.rs1
        upPayload.gprIdxVec(1) := encInstrR.last.rs2
      }
      is (BeqRs1Rs2Imm.op) {
        setImm(encInstr=encInstrB.last)
        switch (encInstrB.last.funct3) {
          is (BeqRs1Rs2Imm.f3) {
            setOp(BeqRs1Rs2Imm, encInstrB.last)
          }
          is (BneRs1Rs2Imm.f3) {
            setOp(BneRs1Rs2Imm, encInstrB.last)
          }
          is (BltRs1Rs2Imm.f3) {
            setOp(BltRs1Rs2Imm, encInstrB.last)
          }
          is (BgeRs1Rs2Imm.f3) {
            setOp(BgeRs1Rs2Imm, encInstrB.last)
          }
          is (BltuRs1Rs2Imm.f3) {
            setOp(BltuRs1Rs2Imm, encInstrB.last)
          }
          is (BgeuRs1Rs2Imm.f3) {
            setOp(BgeuRs1Rs2Imm, encInstrB.last)
          }
        }
        upPayload.gprIdxVec.last := 0x0
      }
      is (JalRdImm.op) {
        setImm(encInstr=encInstrJ.last)
        setOp(JalRdImm, encInstrJ.last)
        upPayload.gprIdxVec(0) := 0x0
        upPayload.gprIdxVec(1) := 0x0
      }
      is (LuiRdImm31Downto12.op) {
        setImm(encInstr=encInstrU.last)
        setOp(LuiRdImm31Downto12, encInstrU.last)
        upPayload.gprIdxVec(0) := 0x0
        upPayload.gprIdxVec(1) := 0x0
      }
      is (AuipcRdImm31Downto12.op) {
        setImm(encInstr=encInstrU.last)
        setOp(AuipcRdImm31Downto12, encInstrU.last)
        upPayload.gprIdxVec(0) := 0x0
        upPayload.gprIdxVec(1) := 0x0
      }
    }
  }
}

case class SnowHouseRiscv32Config(
  optFormal: Boolean,
  optMainAddrWidth: Option[Int]=None,
  optForFmaxCfg: Option[SnowHouseForFmaxConfig]=Some(
    SnowHouseForFmaxConfig(
      numPostExPreWbPipeStages=(
        //0
        1
      ),
      //optDualIssue=true,
      optScoreboard=true,
      //optMaxNumScoreboardInstrs=(
      //  //None
      //  Some(
      //    //1
      //    //2
      //    //4
      //    //6
      //    8
      //  )
      //)
    )
  ),
  targetAltera: Boolean=(
    //false
    true
  ),
  instrRamKind: Int,
  //instrRamFetchLatency: Int,
  programStr: String,
  //exposeRegFileWriteDataToIo: Boolean=false,
  //exposeRegFileWriteAddrToIo: Boolean=false,
  //exposeRegFileWriteEnableToIo: Boolean=false,
  dbgExposeExtrasAtRegFileWrite: Boolean=false,
  optTwoCycleRegFileReads: Boolean=(
    //true
    false
  ),
  regFileMemRamStyleAltera: String=(
    "no_rw_check, M10K"
  ),
  regFileMemRamStyleXilinx: String=(
    //"distributed"
    "block"
  ),
  icacheDepthWords: Int=(
    /*8192*/ 1024 //2048 // 4 kiB icache (by default)
  ),
  icacheNumWays: Int=(
    2
  ),
  dcacheDepthWords: Int=(
    /*8192*/ 1024 //2048 // 4 kiB dcache (by default)
  ),
  dcacheNumWays: Int=(
    2
  ),
  icacheLineWordMemRamStyleAltera: String=(
    "no_rw_check, M10K"
  ),
  icacheLineWordMemRamStyleXilinx: String=(
    //"auto"
    "block"
  ),
  icacheLineAttrsMemRamStyleAltera: String=(
    "no_rw_check, M10K"
  ),
  icacheLineAttrsMemRamStyleXilinx: String=(
    //"auto"
    "block"
  ),
  dcacheLineWordMemRamStyleAltera: String=(
    "no_rw_check, M10K"
  ),
  dcacheLineWordMemRamStyleXilinx: String=(
    //"auto"
    "block"
  ),
  dcacheLineAttrsMemRamStyleAltera: String=(
    "no_rw_check, M10K"
  ),
  dcacheLineAttrsMemRamStyleXilinx: String=(
    //"auto"
    "block"
  ),
  branchTgtBufSizeLog2: Int=(
    //log2Up(128)
    log2Up(64)
    //log2Up(32)
  ),
  branchTgtBufNumWays: Int = (
    2
  ),
) {
  //--------
  val instrMainWidth = (
    32
  )
  val mainWidth = (
    32
    //16
  )
  val numGprs = Riscv32Op.numGprs //+ 1
  val modRdPortCnt = (
    //3
    2
  )
  val pipeName="SnowHouseRiscv32"
  //--------
  val shCfg = SnowHouseConfig(
    haveZeroReg=Some(0),
    irqCfg=(
      None
      //Some(
      //  SnowHouseIrqConfig.IraIds(
      //    ////iraRegIdx
      //    ////allowNestedIrqs=true,
      //    //allowIrqStorm=(
      //    //  true
      //    //  //false
      //    //),
      //    //doBlockIrqCntWidthMinus1=Some(1)
      //    doBlockIrqCntMax=Some(1)
      //  ),
      //)
    ),
    haveAluFlags=false,
    optInvertTwoRegCmp=false,
    optTwoCycleRegFileReads=optTwoCycleRegFileReads,
    subCfg={
      //val icacheDepthWords = (
      //  /*8192*/ 1024 //2048 // 4 kiB icache
      //)
      val icacheLineSizeBytes = 64
      val icacheBusSrcNum = 0x0
      //val dcacheDepthWords = (
      //  /*512*/ /*128*/ 1024 //2048 /*8192*/ // 4 kiB dcache
      //)
      val dcacheLineSizeBytes = 64
      val dcacheBusSrcNum = 0x1
      SnowHouseSubConfig(
        instrMainWidth=instrMainWidth,
        optMainAddrWidth=optMainAddrWidth,
        shRegFileCfg=SnowHouseRegFileConfig(
          mainWidth=mainWidth,
          wordCountArr=(
            Array.fill(1)(numGprs)
            //Array.fill(2)(
            //  numGprs >> 1
            //)
            //Array[Int](
            //  4,
            //  12,
            //)
          ),
          modRdPortCnt=modRdPortCnt,
          pipeName=pipeName,
          optHowToSlice=(
            //None
            Some({
              val tempArr = ArrayBuffer[LinkedHashSet[Int]]()
              tempArr += LinkedHashSet[Int]()
              for (idx <- 0 until numGprs) {
                tempArr.last += idx
              }
              //for (jdx <- 0 until 2) {
              //  tempArr += {
              //    val tempSet = LinkedHashSet[Int]()
              //    if (jdx == 0) {
              //      //for (idx <- 0 until 4) {
              //      //  tempSet += idx
              //      //}
              //      tempSet ++= LinkedHashSet[Int](
              //        0,
              //        2,
              //        4,
              //        6,
              //      )
              //    } else { // if (jdx == 1)
              //      tempSet ++= LinkedHashSet[Int](
              //        1,
              //        3,
              //        5,
              //        7,
              //        8,
              //        9,
              //        10,
              //        11,
              //        12,
              //        13,
              //        14,
              //        15,
              //      )
              //    }
              //    //for (idx <- 0 until (numGprs >> 1)) {
              //    //  tempSet += {
              //    //    val temp = (
              //    //      (2 * idx) + jdx
              //    //    )
              //    //    println(
              //    //      s"debug: "
              //    //      + s"temp:${temp} idx:${idx} jdx:${jdx}"
              //    //    )
              //    //    temp
              //    //  }
              //    //}
              //    tempSet
              //  }
              //}
              tempArr
            })
          ),
          memRamStyleAltera=(
            regFileMemRamStyleAltera
          ),
          memRamStyleXilinx=(
            //"distributed"
            regFileMemRamStyleXilinx
          ),
        ),
        optScoreboard=optForFmaxCfg match {
          case Some(forFmaxCfg) => {
            forFmaxCfg.optScoreboard,
          }
          case None => {
            false
          }
        },
        haveIcache=true,
        icacheDepthWords=icacheDepthWords,
        icacheNumWays=icacheNumWays,
        icacheLineSizeBytes=icacheLineSizeBytes,
        icacheBusSrcNum=icacheBusSrcNum,
        icacheLineWordMemRamStyleAltera=icacheLineWordMemRamStyleAltera,
        icacheLineWordMemRamStyleXilinx=icacheLineWordMemRamStyleXilinx,
        icacheLineAttrsMemRamStyleAltera=icacheLineAttrsMemRamStyleAltera,
        icacheLineAttrsMemRamStyleXilinx=icacheLineAttrsMemRamStyleXilinx,
        haveDcache=true,
        dcacheDepthWords=dcacheDepthWords,
        dcacheNumWays=dcacheNumWays,
        dcacheLineSizeBytes=dcacheLineSizeBytes,
        dcacheBusSrcNum=dcacheBusSrcNum,
        dcacheLineWordMemRamStyleAltera=dcacheLineWordMemRamStyleAltera,
        dcacheLineWordMemRamStyleXilinx=dcacheLineWordMemRamStyleXilinx,
        dcacheLineAttrsMemRamStyleAltera=dcacheLineAttrsMemRamStyleAltera,
        dcacheLineAttrsMemRamStyleXilinx=dcacheLineAttrsMemRamStyleXilinx,
        totalNumBusHosts=2,
        optCacheBusSrcWidth=None,
      )
    },
    opInfoMap=Riscv32OpInfoMap.opInfoMap,
    irqJmpOp={
      var myIrqJmpOp: Int = 0x0
      //for (
      //  ((tuple, opInfo), opInfoIdx)
      //  <- SnowHouseCpuOpInfoMap.opInfoMap.view.zipWithIndex
      //) {
      //  if (
      //    //tuple == SnowHouseCpuOp.JlRaRb
      //    tuple == SnowHouseCpuOp.JmpieIds
      //  ) {
      //    myIrqJmpOp = opInfoIdx
      //  }
      //}
      myIrqJmpOp
    },
    optForFmaxCfg=(
      optForFmaxCfg
    ),
    doInstrDecodeFunc=SnowHouseRiscv32PipeStageInstrDecode.apply,
    optBranchPredictorKind=Some(
      //SnowHouseBranchPredictorKind.FwdNotTknBakTkn(
      //  branchTgtBufSizeLog2=(
      //    branchTgtBufSizeLog2
      //  ),
      //)
      SnowHouseBranchPredictorKind.SatCnt2Bit(
        branchTgtBufSizeLog2=(
          branchTgtBufSizeLog2
        ),
        branchTgtBufNumWays=(
          branchTgtBufNumWays
        ),
      )
    ),
    supportUcode=(
      //true
      false
    ),
    instrRamKind=instrRamKind,
    exposeRegFileWriteDataToIo=dbgExposeExtrasAtRegFileWrite,
    exposeRegFileWriteAddrToIo=dbgExposeExtrasAtRegFileWrite,
    exposeRegFileWriteEnableToIo=dbgExposeExtrasAtRegFileWrite,
    dbgExposeExtrasAtRegFileWrite=dbgExposeExtrasAtRegFileWrite,
    targetAltera=targetAltera,
    optFormal=optFormal,
  )
  //--------
  val program = ArrayBuffer[AsmStmt]()
}

case class SnowHouseRiscv32WithoutRamIo(
  cfg: SnowHouseRiscv32Config
) extends Bundle {
  val lcvIbus = (
    master(LcvBusIo(
      cfg=cfg.shCfg.subCfg.lcvIbusEtcCfg.loBusCfg,
    ))
  )
  val lcvDbus = (
    master(LcvBusIo(
      cfg=cfg.shCfg.subCfg.lcvDbusEtcCfg.loBusCfg,
    ))
  )

  val dbgInfo = (
    cfg.dbgExposeExtrasAtRegFileWrite
  ) generate (
    out(SnowHouseDebugInfo(cfg=cfg.shCfg))
  )
  def regFileWriteData = (
    dbgInfo.regFileWriteData
  )
  def regFileWriteAddr = (
    dbgInfo.regFileWriteAddr
  )
  def regFileWriteEnable = (
    dbgInfo.regFileWriteEnable
  )
  def laggingRegPcAtRegFileWrite = (
    dbgInfo.laggingRegPcAtRegFileWrite
  )
  def shouldIgnoreInstrAtRegFileWrite = (
    dbgInfo.shouldIgnoreInstrAtRegFileWrite
  )
  def encInstrAtRegFileWrite = (
    dbgInfo.encInstrAtRegFileWrite
  )
}


case class SnowHouseRiscv32AddMultiCycle(
  cpuIo: SnowHouseIo,
  //bridge: SnowHouseMcDualBusToMcBusBridge,
) extends Area {
  def cfg = cpuIo.cfg
  //def cfg = bridge.cfg
  for (
    ((group, innerMap), busIdx)
    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    val multiCycleBus = cpuIo.multiCycleBusVec(busIdx)
    def dstVec = multiCycleBus.recvData.dstVec
    def srcVec = multiCycleBus.sendData.srcVec
    //switch (
    //  multiCycleBus.sendData.kind
    //) {
      for (
        ((_, opInfo), kindIdx)
        <- innerMap.view.zipWithIndex
      ) {
        opInfo.multiCycleOp.get match {
          case MultiCycleOpKind.AddRaPcImm => {
            //is (kindIdx) {
              def mainWidth = cfg.mainWidth
              val rSrc0 = (
                RegNextWhen(
                  next=(
                    RegNext(srcVec(0))
                    init(0x0)
                  ),
                  cond=rose(multiCycleBus.rValid)
                )
                init(0x0)
              )
              val rSrc1 = (
                RegNextWhen(
                  next=(
                    RegNext(srcVec(1))
                    init(0x0)
                  ),
                  cond=rose(multiCycleBus.rValid)
                )
                init(0x0)
              )
              val rDst = (
                Reg(
                  cloneOf(dstVec(0)),
                  init=dstVec(0).getZero,
                )
                setName(
                  "SnowHouseRiscv32Cpy32_AddRaPcImm_rDst"
                )
              )
              multiCycleBus.ready := False
              dstVec(0) := rDst
              rDst := rSrc0 + rSrc1
              when (
                RegNext(
                  next=RegNext(
                    next=rose(multiCycleBus.rValid),
                    init=False,
                  ),
                  init=False,
                )
              ) {
                multiCycleBus.ready := True
              }
            //}
          }
          case _ => {
          }
        }
      }
      //default {
      //  dstVec.foreach(dst => dst := dst.getZero)
      //  multiCycleBus.ready := False
      //}
    //}
  }
}

case class SnowHouseRiscv32Divmod(
  cpuIo: SnowHouseIo
  //bridge: SnowHouseMcDualBusToMcBusBridge,
) extends Area {
  def cfg = cpuIo.cfg
  //def cfg = bridge.cfg
  val divmod = LongDivMultiCycle(
    mainWidth=cfg.mainWidth,
    denomWidth=cfg.mainWidth,
    chunkWidth=1,//2,//1,//2,//3,//4,//2,//1,//2,
    signedReset=0x0,
  )
  object DivmodState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      CHECK_PREV,
      RUNNING,
      YIELD_RESULT_PIPE_3,
      YIELD_RESULT_PIPE_2,
      YIELD_RESULT_PIPE_1,
      YIELD_RESULT
      = newElement()
  }
  val rState = Reg(DivmodState()) init(DivmodState.IDLE)
  object DivmodKind
  extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      UDIV,
      SDIV,
      UMOD,
      SMOD
      //UDIVW,
      //SDIVW
      = newElement()
  }
  val rKind = (
    Reg(DivmodKind())
    init(DivmodKind.UDIV)
  )
  val rPrevKind = {
    val temp = (
      Reg(Flow(DivmodKind()))
    )
    temp.valid.init(temp.valid.getZero)
    temp.payload.init(DivmodKind.UDIV)
    temp
  }

  for (
    ((group, innerMap), busIdx)
    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    val multiCycleBus = cpuIo.multiCycleBusVec(busIdx)
    def dstVec = multiCycleBus.recvData.dstVec
    def srcVec = multiCycleBus.sendData.srcVec
    if (
      group == MultiCycleOpKind.Udiv.group
      && multiCycleBus.sendData.kind != null
    ) {
      multiCycleBus.recvData.dstVec.foreach(dst => dst := 0x0)
      multiCycleBus.ready := False
    }
  }
  def myFunc(
    doItFunc: (
      OpInfo,
      Int,
      LcvStallIo[MultiCycleHostPayload, MultiCycleDevPayload]
    ) => Area,
    setKind: Boolean,
    needBusRvalid: Boolean=true,
    //setPrevKind: Boolean,
  ): Area = new Area {
    //if (setPrevKind) {
    //  rPrevKind.valid := True
    //  rPrevKind.payload := rKind
    //}
    for (
      ((group, innerMap), busIdx)
      <- cfg.multiCycleOpInfoMap.view.zipWithIndex
    ) {
      val multiCycleBus = cpuIo.multiCycleBusVec(busIdx)
      def dstVec = multiCycleBus.recvData.dstVec
      def srcVec = multiCycleBus.sendData.srcVec
      if (
        group == MultiCycleOpKind.Udiv.group
        && multiCycleBus.sendData.kind != null
      ) {
        switch (multiCycleBus.sendData.kind) {
          for (
            //(multiCycleBus, busIdx) <- cpuIo.multiCycleBusVec.view.zipWithIndex
            ((_, opInfo), kindIdx)
            <- innerMap.view.zipWithIndex
          ) {
            opInfo.multiCycleOp.get match {
              case MultiCycleOpKind.Udiv => {
                is (kindIdx) {
                  if (!needBusRvalid) {
                    val tempArea = doItFunc(
                      opInfo,
                      busIdx,
                      cpuIo.multiCycleBusVec(busIdx)
                    )
                  } else {
                    when (
                      rose(
                        RegNext(
                          next=(
                            cpuIo.multiCycleBusVec(busIdx).nextValid
                          ),
                          init=False,
                        )
                      )
                    ) {
                      if (setKind) {
                        rKind := DivmodKind.UDIV
                      }
                      val tempArea = doItFunc(
                        opInfo,
                        busIdx,
                        cpuIo.multiCycleBusVec(busIdx)
                      )
                    }
                  }
                }
              }
              case MultiCycleOpKind.Sdiv => {
                is (kindIdx) {
                  if (!needBusRvalid) {
                    val tempArea = doItFunc(
                      opInfo,
                      busIdx,
                      cpuIo.multiCycleBusVec(busIdx)
                    )
                  } else {
                    when (
                      rose(
                        RegNext(
                          next=(
                            cpuIo.multiCycleBusVec(busIdx).nextValid
                          ),
                          init=False,
                        )
                      )
                    ) {
                      if (setKind) {
                        rKind := DivmodKind.SDIV
                      }
                      val tempArea = doItFunc(
                        opInfo,
                        busIdx,
                        cpuIo.multiCycleBusVec(busIdx)
                      )
                    }
                  }
                }
              }
              case MultiCycleOpKind.Umod => {
                is (kindIdx) {
                  if (!needBusRvalid) {
                    val tempArea = doItFunc(
                      opInfo,
                      busIdx,
                      cpuIo.multiCycleBusVec(busIdx)
                    )
                  } else {
                    when (
                      rose(
                        RegNext(
                          next=(
                            cpuIo.multiCycleBusVec(busIdx).nextValid
                          ),
                          init=False
                        )
                      )
                    ) {
                      if (setKind) {
                        rKind := DivmodKind.UMOD
                      }
                      val tempArea = doItFunc(
                        opInfo,
                        busIdx,
                        cpuIo.multiCycleBusVec(busIdx)
                      )
                    }
                  }
                }
              }
              case MultiCycleOpKind.Smod => {
                is (kindIdx) {
                  if (!needBusRvalid) {
                    val tempArea = doItFunc(
                      opInfo,
                      busIdx,
                      cpuIo.multiCycleBusVec(busIdx)
                    )
                  } else {
                    when (
                      rose(
                        RegNext(
                          next=(
                            cpuIo.multiCycleBusVec(busIdx).nextValid
                          ),
                          init=False,
                        )
                      )
                    ) {
                      if (setKind) {
                        rKind := DivmodKind.SMOD
                      }
                      val tempArea = doItFunc(
                        opInfo,
                        busIdx,
                        cpuIo.multiCycleBusVec(busIdx)
                      )
                    }
                  }
                }
              }
              //case MultiCycleOpKind.Udivw => {
              //  is (kindIdx) {
              //    if (!needBusRvalid) {
              //      val tempArea = doItFunc(
              //        opInfo,
              //        busIdx,
              //        cpuIo.multiCycleBusVec(busIdx)
              //      )
              //    } else {
              //      when (
              //        rose(
              //          RegNext(
              //            next=cpuIo.multiCycleBusVec(busIdx).nextValid,
              //            init=False,
              //          )
              //        )
              //      ) {
              //        if (setKind) {
              //          rKind := DivmodKind.UDIVW
              //        }
              //        val tempArea = doItFunc(
              //          opInfo,
              //          busIdx,
              //          cpuIo.multiCycleBusVec(busIdx)
              //        )
              //      }
              //    }
              //  }
              //}
              //case MultiCycleOpKind.Sdivw => {
              //  is (kindIdx) {
              //    if (!needBusRvalid) {
              //      val tempArea = doItFunc(
              //        opInfo,
              //        busIdx,
              //        cpuIo.multiCycleBusVec(busIdx)
              //      )
              //    } else {
              //      when (
              //        rose(
              //          RegNext(
              //            next=cpuIo.multiCycleBusVec(busIdx).nextValid,
              //            init=False,
              //          )
              //        )
              //      ) {
              //        if (setKind) {
              //          rKind := DivmodKind.SDIVW
              //        }
              //        val tempArea = doItFunc(
              //          opInfo,
              //          busIdx,
              //          cpuIo.multiCycleBusVec(busIdx)
              //        )
              //      }
              //    }
              //  }
              //}
              case _ => {
              }
            }
          }
          //default {
          //  multiCycleBus.ready := False
          //}
        }
      }
    }
  }
  val rSavedSrcVec = Vec.fill(4)(
    Reg(UInt(cfg.mainWidth bits))
    init(0x0)
  )
  val rSavedQuot = (
    Vec.fill(4)(
      Reg(UInt(cfg.mainWidth bits))
      init(0x0)
    )
  )
  val rSavedRema = (
    Vec.fill(4)(
      Reg(UInt(cfg.mainWidth bits))
      init(0x0)
    )
  )
  val rSavedResult = (
    Vec.fill(3)(
      Vec.fill(4)(
        Reg(UInt(cfg.mainWidth bits))
        init(0x0)
      )
    )
  )
  def mainWidth = cfg.mainWidth
  val myArea = myFunc(
    doItFunc=(
      opInfo,
      busIdx,
      stallIo
    ) => new Area {
      //stallIo.recvData.dstVec.foreach(dst => {
      //  dst := (
      //    RegNext(
      //      next=dst,
      //      init=dst.getZero
      //    )
      //  )
      //})
      stallIo.ready.allowOverride
      stallIo.ready := False
    },
    setKind=false,
    needBusRvalid=false,
  )
  divmod.io.inp.valid := False
  //divmod.io.inp.numer := (
  //  RegNext(
  //    next=divmod.io.inp.numer,
  //    init=divmod.io.inp.numer.getZero,
  //  )
  //)
  //divmod.io.inp.denom := (
  //  RegNext(
  //    next=divmod.io.inp.denom,
  //    init=divmod.io.inp.denom.getZero,
  //  )
  //)
  //divmod.io.inp.signed := (
  //  RegNext(
  //    next=divmod.io.inp.signed,
  //    init=divmod.io.inp.signed.getZero,
  //  )
  //)
  for (idx <- 0 until 4) {
    when (divmod.io.outp.ready) {
      rSavedQuot(idx) := divmod.io.outp.quot
      rSavedRema(idx) := divmod.io.outp.rema
    }
    when (!rKind.asBits(1)) {
      //rSavedResult(0).foreach(result => result := rSavedQuot)
      rSavedResult(0)(idx) := rSavedQuot(idx)
    } otherwise {
      //rSavedResult(0).foreach(result => result := rSavedRema)
      rSavedResult(0)(idx) := rSavedRema(idx)
    }
  }
  rSavedResult(1) := rSavedResult(0)
  rSavedResult(2) := rSavedResult(1)
  switch (
    //rKind.asBits(2 downto 1)
    rKind//.asBits
  ) {
    is (DivmodKind.UDIV) {
      divmod.io.inp.numer := (
        rSavedSrcVec(0).resize(divmod.io.inp.numer.getWidth)
      )
      divmod.io.inp.denom := (
        rSavedSrcVec(1).resize(divmod.io.inp.denom.getWidth)
      )
    }
    is (DivmodKind.SDIV) {
      divmod.io.inp.numer := (
        rSavedSrcVec(0).asSInt.resize(divmod.io.inp.numer.getWidth).asUInt
      )
      divmod.io.inp.denom := (
        rSavedSrcVec(1).asSInt.resize(divmod.io.inp.denom.getWidth).asUInt
      )
    }
    is (DivmodKind.UMOD) {
      divmod.io.inp.numer := (
        rSavedSrcVec(0).resize(divmod.io.inp.numer.getWidth)
      )
      divmod.io.inp.denom := (
        rSavedSrcVec(1).resize(divmod.io.inp.denom.getWidth)
      )
    }
    is (DivmodKind.SMOD)  {
      divmod.io.inp.numer := (
        rSavedSrcVec(0).asSInt.resize(divmod.io.inp.numer.getWidth).asUInt
      )
      divmod.io.inp.denom := (
        rSavedSrcVec(1).asSInt.resize(divmod.io.inp.denom.getWidth).asUInt
      )
    }
    //is (DivmodKind.UDIVW) {
    //  divmod.io.inp.numer := (
    //    //Cat(rSavedSrcVec(2), rSavedSrcVec(0)).asUInt
    //    Cat(rSavedSrcVec(0), rSavedSrcVec(3)).asUInt
    //  )
    //  divmod.io.inp.denom := (
    //    //rSavedSrcVec(1).resize(divmod.io.inp.denom.getWidth)
    //    Cat(rSavedSrcVec(1), rSavedSrcVec(2)).asUInt
    //  )
    //}
    //is (DivmodKind.SDIVW) {
    //  divmod.io.inp.numer := (
    //    //Cat(rSavedSrcVec(2), rSavedSrcVec(0)).asUInt
    //    Cat(rSavedSrcVec(0), rSavedSrcVec(3)).asUInt
    //  )
    //  divmod.io.inp.denom := (
    //    //rSavedSrcVec(1).asSInt.resize(divmod.io.inp.denom.getWidth).asUInt

    //    Cat(rSavedSrcVec(1), rSavedSrcVec(2)).asUInt
    //  )
    //}
    //is () {
    //  // udiv, sdiv, umod, smod
    //  divmod.io.inp.numer := rSavedSrcVec(0)
    //  divmod.io.inp.denom := rSavedSrcVec(1)
    //}
    //is (B"01") {
    //  // umod/smod
    //}
    //is (B"10") {
    //}
    //is (B"11") {
    //}
  }
  //divmod.io.inp.numer := rSavedSrcVec(0)
  //divmod.io.inp.denom := rSavedSrcVec(1)
  divmod.io.inp.signed := rKind.asBits(0)
  //switch (rKind) {
  for (
    ((group, innerMap), busIdx)
    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    val multiCycleBus = cpuIo.multiCycleBusVec(busIdx)
    def dstVec = multiCycleBus.recvData.dstVec
    def srcVec = multiCycleBus.sendData.srcVec
    if (
      group == MultiCycleOpKind.Udiv.group
      && multiCycleBus.sendData.kind != null
    ) {
      switch (multiCycleBus.sendData.kind) {
        for (
          ((_, opInfo), kindIdx)
          <- innerMap.view.zipWithIndex
        ) {
          opInfo.multiCycleOp.get match {
            case MultiCycleOpKind.Udiv => {
              is (kindIdx) {
                //is (DivmodKind.UDIV) {
                  val stallIo = (
                    cpuIo.multiCycleBusVec(busIdx)
                  )
                  def dstVec = stallIo.recvData.dstVec
                  //stallIo.ready := True
                  dstVec(0) := rSavedResult.last(0)(dstVec(0).bitsRange)
                //}
              }
            }
            case MultiCycleOpKind.Sdiv => {
              is (kindIdx) {
                //is (DivmodKind.SDIV) {
                  val stallIo = (
                    cpuIo.multiCycleBusVec(busIdx)
                  )
                  def dstVec = stallIo.recvData.dstVec
                  //stallIo.ready := True
                  dstVec(0) := rSavedResult.last(1)(dstVec(0).bitsRange)
                //}
              }
            }
            case MultiCycleOpKind.Umod => {
              is (kindIdx) {
                //is (DivmodKind.UMOD) {
                  val stallIo = (
                    cpuIo.multiCycleBusVec(busIdx)
                  )
                  def dstVec = stallIo.recvData.dstVec
                  //stallIo.ready := True
                  dstVec(0) := rSavedResult.last(2)(dstVec(0).bitsRange)
                //}
              }
            }
            case MultiCycleOpKind.Smod => {
              is (kindIdx) {
                //is (DivmodKind.SMOD) {
                  val stallIo = (
                    cpuIo.multiCycleBusVec(busIdx)
                  )
                  def dstVec = stallIo.recvData.dstVec
                  //stallIo.ready := True
                  dstVec(0) := rSavedResult.last(3)(dstVec(0).bitsRange)
                //}
              }
            }
            case MultiCycleOpKind.Udivw => {
              is (kindIdx) {
                //is (DivmodKind.UDIV) {
                  val stallIo = (
                    cpuIo.multiCycleBusVec(busIdx)
                  )
                  def dstVec = stallIo.recvData.dstVec
                  //stallIo.ready := True
                  //dstVec(0) := rSavedResult.last(0)
                  dstVec(0) := rSavedResult.last(0)(63 downto 32)
                  dstVec(1) := rSavedResult.last(0)(31 downto 0)
                //}
              }
            }
            case MultiCycleOpKind.Sdivw => {
              is (kindIdx) {
                //is (DivmodKind.SDIV) {
                  val stallIo = (
                    cpuIo.multiCycleBusVec(busIdx)
                  )
                  def dstVec = stallIo.recvData.dstVec
                  //stallIo.ready := True
                  dstVec(0) := rSavedResult.last(1)(63 downto 32)
                  dstVec(1) := rSavedResult.last(1)(31 downto 0)

                //}
              }
            }
            case _ => {
            }
          }
        }
      }
    }
  }
  //}
  switch (rState) {
    is (DivmodState.IDLE) {
      val idleArea = myFunc(
        doItFunc=(
          opInfo,
          busIdx,
          stallIo,
        ) => new Area {
          rState := DivmodState.CHECK_PREV
          def dstVec = stallIo.recvData.dstVec
          def srcVec = stallIo.sendData.srcVec
          rSavedSrcVec(0) := (
            RegNext(
              next=srcVec(0),
              init=srcVec(0).getZero,
            )
          )
          rSavedSrcVec(1) := (
            RegNext(
              next=srcVec(1),
              init=srcVec(1).getZero,
            )
          )
          if (srcVec.size > 2) {
            rSavedSrcVec(2) := (
              RegNext(
                next=srcVec(2),
                init=srcVec(2).getZero,
              )
            )
          }
          if (srcVec.size > 3) {
            rSavedSrcVec(3) := (
              RegNext(
                next=srcVec(3),
                init=srcVec(3).getZero,
              )
            )
          }
        },
        setKind=true,
      )
    }
    is (DivmodState.CHECK_PREV) {
      //val checkPrevInpArea = myFunc(
      //  doItFunc=(
      //    opInfo,
      //    busIdx,
      //    stallIo,
      //  ) => new Area {
      //    def dstVec = stallIo.recvData.dstVec
      //    def srcVec = stallIo.sendData.srcVec
      //    //when (
      //    //  rPrevKind.fire
      //    //  && (
      //    //    // this checks if the signedness is the same due to the
      //    //    // encoding of `DivmodKind`
      //    //    rPrevKind.payload.asBits(0) === rKind.asBits(0)
      //    //  ) && (
      //    //    srcVec(0) === rSavedSrcVec(0)
      //    //  ) && (
      //    //    srcVec(1) === rSavedSrcVec(1)
      //    //  )
      //    //) {
      //    //  stallIo.ready := True
      //    //  rState := DivmodState.IDLE
      //    //  when (
      //    //    !rKind.asBits(1)
      //    //  ) {
      //    //    dstVec(0) := rSavedQuot
      //    //  } otherwise {
      //    //    dstVec(0) := rSavedRema
      //    //  }
      //    //} otherwise {
      //    //  divmod.io.inp.valid := True
      //    //  divmod.io.inp.numer := srcVec(0)
      //    //  divmod.io.inp.denom := srcVec(1)
      //    //  divmod.io.inp.signed := rKind.asBits(0)
      //    //  rSavedSrcVec(0) := srcVec(0)
      //    //  rSavedSrcVec(1) := srcVec(1)

      //    //  rState := DivmodState.RUNNING
      //    //}
      //    divmod.io.inp.valid := True
      //    divmod.io.inp.numer := rSavedSrcVec(0)
      //    divmod.io.inp.denom := rSavedSrcVec(1)
      //    divmod.io.inp.signed := rKind.asBits(0)
      //    rState := DivmodState.RUNNING
      //  },
      //  setKind=false,
      //)
      //--------
      // BEGIN: FMAX debugging
      divmod.io.inp.valid := True
      // END: FMAX debugging
      //--------
      rState := DivmodState.RUNNING
    }
    is (DivmodState.RUNNING) {
      //val runningArea = myFunc(
      //  doItFunc=(
      //    opInfo,
      //    busIdx,
      //    stallIo,
      //  ) => new Area {
      //  },
      //  setKind=false,
      //)
      when (
        //--------
        // BEGIN: FMAX debugging
        divmod.io.outp.ready
        // END: FMAX debugging
        //--------
        //True
      ) {
        rState := (
          DivmodState.YIELD_RESULT_PIPE_3
          //DivmodState.YIELD_RESULT
        )
      }
    }
    is (DivmodState.YIELD_RESULT_PIPE_3) {
      rState := DivmodState.YIELD_RESULT_PIPE_2
    }
    is (DivmodState.YIELD_RESULT_PIPE_2) {
      rState := DivmodState.YIELD_RESULT_PIPE_1
    }
    is (DivmodState.YIELD_RESULT_PIPE_1) {
      rState := DivmodState.YIELD_RESULT
    }
    is (DivmodState.YIELD_RESULT) {
      rPrevKind.valid := True
      rPrevKind.payload := rKind
      //val yieldResultArea = myFunc(
      //  doItFunc=(
      //    opInfo,
      //    busIdx,
      //    stallIo,
      //  ) => new Area {
      //    def dstVec = stallIo.recvData.dstVec
      //    rState := DivmodState.IDLE
      //    //when (!rKind.asBits(1)) {
      //    //  dstVec(0) := rSavedQuot
      //    //} otherwise {
      //    //  dstVec(0) := rSavedRema
      //    //}
      //    dstVec(0) := rSavedResult
      //    stallIo.ready := True
      //  },
      //  setKind=false,
      //  needBusRvalid=false,
      //)
      //switch (rKind) {
      for (
        ((group, innerMap), busIdx)
        <- cfg.multiCycleOpInfoMap.view.zipWithIndex
      ) {
        val multiCycleBus = cpuIo.multiCycleBusVec(busIdx)
        def dstVec = multiCycleBus.recvData.dstVec
        def srcVec = multiCycleBus.sendData.srcVec
        //var haveCorrectBus: Boolean = false
        if (group == MultiCycleOpKind.Udiv.group) {
          val stallIo = (
            cpuIo.multiCycleBusVec(busIdx)
          )
          def dstVec = stallIo.recvData.dstVec
          stallIo.ready := True
        }
        }
      //}
      rState := DivmodState.IDLE
    }
  }
}

case class SnowHouseRiscv32MulIo(
  cfg: SnowHouseConfig,
) extends Bundle {
  val multiCycleBus = slave(
    new LcvStallIo[
      MultiCycleHostPayload,
      MultiCycleDevPayload,
    ](
      sendPayloadType=(
        Some(MultiCycleHostPayload(
          cfg=cfg,
          group=MultiCycleOpGroup.Mul,
        ))
      ),
      recvPayloadType=(
        Some(MultiCycleDevPayload(
          cfg=cfg,
          group=MultiCycleOpGroup.Mul,
        ))
      ),
    )
  )
  //val inpVec = in(
  //  Vec.fill(2)(
  //    UInt(cfg.mainWidth * 2 bits)
  //  )
  //)
  //val outpProd = out(UInt(cfg.mainWidth * 2 bits))
  //val isSigned = in(Bool())
}
//case class SnowHouseRiscv32Mul(
//  cfg: SnowHouseConfig
//) extends Component {
//  val io = SnowHouseRiscv32MulIo(cfg=cfg)
//  def multiCycleBus = io.multiCycleBus
//  //val rIsSignedFullProd = Reg(Bool(), init=False)
//  val rSignVec = (
//    Vec.fill(multiCycleBus.sendData.srcVec.size)(
//      Reg(Bool(), init=False)
//    )
//  )
//  val rNeedToNegateResultSign = Reg(Bool(), init=False)
//
//  val rAbsSrcVec = {
//    val temp = Reg(cloneOf(multiCycleBus.sendData.srcVec))
//    temp.foreach(item => item.init(0x0))
//    temp
//  }
//  def dstVec = multiCycleBus.recvData.dstVec
//  def mainWidth = cfg.mainWidth
//  object State
//  extends SpinalEnum(defaultEncoding=binaryOneHot) {
//    val
//      IDLE,
//      DO_ABS_BOTH_INPUTS_IF_NEGATIVE,
//      DO_ABS_LEFT_INPUT_IF_NEGATIVE,
//      DO_FOUR_MUL16X16,
//      FIRST_TWO_ADDS,
//      //FINAL_ADD_KIND_ZERO,
//      FINAL_ADD_KIND_NON_ZERO,
//      DO_NEGATE_RESULT,
//      YIELD_RESULT
//      = newElement()
//  }
//  val rState = (
//    Reg(State())
//    init(State.IDLE)
//    //setName("SnowHouseRiscv32Mul32_Umul_rState")
//  )
//  val low = (mainWidth >> 1) - 1 downto 0
//  val high = (mainWidth - 1 downto (mainWidth >> 1))
//  val shiftAmount = mainWidth >> 1
//  println(
//    s"low:${low} high:${high} shiftAmount:${shiftAmount}"
//  )
//  val rX0Y0 = (
//    Reg(UInt(mainWidth bits))
//    init(0x0)
//  )
//  val rX0Y1 = (
//    Reg(UInt(mainWidth bits))
//    init(0x0)
//  )
//  val rX1Y0 = (
//    Reg(UInt(mainWidth bits))
//    init(0x0)
//  )
//  val rX1Y1 = (
//    Reg(UInt(mainWidth bits))
//    init(0x0)
//  )
//  val rPartialSum = (
//    Vec.fill(2)(
//      Reg(UInt((mainWidth * 2) bits))
//      init(0x0)
//    )
//  )
//
//  val z2 = rX1Y1
//  val z1 = Cat(False, rX1Y0).asUInt + Cat(False, rX0Y1).asUInt
//  val z0 = rX0Y0
//
//  //rPartialSum(0) := (
//  //  rX0Y0
//  //)
//
//  multiCycleBus.ready := False
//  val rDstVec = {
//    //Reg(
//    //  cloneOf(dstVec(0)),
//    //  init=dstVec(0).getZero
//    //)
//    val temp = Reg(cloneOf(dstVec))
//    temp.foreach(item => item.init(item.getZero))
//    temp
//  }
//  //dstVec(0) := rDst
//  dstVec := rDstVec
//  //val rSavedKindIsZero = Reg(Bool(), init=False)
//
//  when (
//    rState.asBits(0)
//    && RegNext(rose(multiCycleBus.nextValid), init=False)
//    && RegNext(!multiCycleBus.sendData.kind(1 downto 0).orR)
//  ) {
//    dstVec(0) := (
//      RegNext(
//        multiCycleBus.sendData.srcVec(0)
//        * multiCycleBus.sendData.srcVec(1)
//      ).resize(dstVec(0).getWidth)
//    )
//    multiCycleBus.ready := True
//  }
//
//  switch (rState) {
//    is (State.IDLE) {
//      switch (
//        RegNext(rose(multiCycleBus.nextValid), init=False)
//        ## RegNext(multiCycleBus.sendData.kind(1 downto 0))
//      ) {
//        is (M"100") {
//          //rSavedKindIsZero := True
//          //rState := State.DO_FOUR_MUL16X16
//        }
//        is (M"101") {
//          //rSavedKindIsZero := False
//          rState := State.DO_FOUR_MUL16X16
//        }
//        is (B"110") {
//          //rSavedKindIsZero := False
//          rState := State.DO_ABS_BOTH_INPUTS_IF_NEGATIVE
//        }
//        is (B"111") {
//          //rSavedKindIsZero := False
//          rState := State.DO_ABS_LEFT_INPUT_IF_NEGATIVE
//        }
//        default {
//        }
//      }
//      for (idx <- 0 until rAbsSrcVec.size) {
//        rAbsSrcVec(idx) := (
//          RegNext(
//            multiCycleBus.sendData.srcVec(idx),
//            init=rAbsSrcVec(idx).getZero
//          )
//        )
//      }
//      //rAbsSrcVec := RegNext(
//      //  multiCycleBus.sendData.srcVec
//      //)
//      rSignVec := rSignVec.getZero
//      //rIsSignedFullProd := multiCycleBus.sendData.kind.lsb
//    }
//    is (State.DO_ABS_BOTH_INPUTS_IF_NEGATIVE) {
//      rState := State.DO_FOUR_MUL16X16
//      for (idx <- 0 until rAbsSrcVec.size) {
//        def myAbsSrc = rAbsSrcVec(idx)
//        rSignVec(idx) := myAbsSrc.msb
//        when (myAbsSrc.msb) {
//          myAbsSrc := (-myAbsSrc.asSInt).asUInt
//        }
//      }
//    }
//    is (State.DO_ABS_LEFT_INPUT_IF_NEGATIVE) {
//      rState := State.DO_FOUR_MUL16X16
//      def myAbsSrc = rAbsSrcVec(0)
//      rSignVec(0) := myAbsSrc.msb
//      when (myAbsSrc.msb) {
//        myAbsSrc := (-myAbsSrc.asSInt).asUInt
//      }
//    }
//    is (State.DO_FOUR_MUL16X16) {
//      rNeedToNegateResultSign := (
//        // This will always result in a `False` when we are doing
//        // an unsigned full product because in this case we never ended up
//        // in the `rState` of `State.DO_ABS_INPUTS_IF_NEGATIVE`
//        rSignVec(0) =/= rSignVec(1)
//      )
//      rX0Y0 := rAbsSrcVec(0)(low) * rAbsSrcVec(1)(low)
//      rX0Y1 := rAbsSrcVec(0)(low) * rAbsSrcVec(1)(high)
//      rX1Y0 := rAbsSrcVec(0)(high) * rAbsSrcVec(1)(low)
//      rX1Y1 := rAbsSrcVec(0)(high) * rAbsSrcVec(1)(high)
//      rState := State.FIRST_TWO_ADDS
//    }
//    is (State.FIRST_TWO_ADDS) {
//      //when (rSavedKindIsZero) {
//      //  rState := State.FINAL_ADD_KIND_ZERO
//      //} otherwise {
//        rState := State.FINAL_ADD_KIND_NON_ZERO
//      //}
//      rPartialSum(0) := Cat(z2, z0).asUInt
//      rPartialSum(1) := (
//        Cat(
//          //U(s"${shiftAmount}'d0"),
//          //z1(cfg.mainWidth - 1 downto 0),
//          z1,
//          U(s"${shiftAmount}'d0"),
//        ).asUInt.resize(rPartialSum(1).getWidth)
//      )
//    }
//    //is (State.FINAL_ADD_KIND_ZERO) {
//    //  (rDstVec(1), rDstVec(0)) := rPartialSum(1) + rPartialSum(0)
//    //  when (!rNeedToNegateResultSign) {
//    //    rState := State.YIELD_RESULT
//    //  } otherwise {
//    //    rState := State.DO_NEGATE_RESULT
//    //  }
//    //}
//    is (State.FINAL_ADD_KIND_NON_ZERO) {
//      (rDstVec(0), rDstVec(1)) := rPartialSum(1) + rPartialSum(0)
//      when (!rNeedToNegateResultSign) {
//        rState := State.YIELD_RESULT
//      } otherwise {
//        rState := State.DO_NEGATE_RESULT
//      }
//    }
//    is (State.DO_NEGATE_RESULT) {
//      (rDstVec(0), rDstVec(1)) := (
//        (-Cat(rDstVec(0), rDstVec(1)).asSInt).asUInt
//      )
//      rState := State.YIELD_RESULT
//    }
//    is (State.YIELD_RESULT) {
//      rState := State.IDLE
//      //rIsSignedFullProd := False
//      multiCycleBus.ready := True
//    }
//  }
//}

case class SnowHouseRiscv32Mul(
  cfg: SnowHouseConfig
) extends Component {
  val io = SnowHouseRiscv32MulIo(cfg=cfg)
  def multiCycleBus = io.multiCycleBus

  def srcVec = multiCycleBus.sendData.srcVec
  def dstVec = multiCycleBus.recvData.dstVec
  multiCycleBus.ready := False

  //object State
  //extends SpinalEnum(defaultEncoding=binaryOneHot) {
  //  val
  //    IDLE_OR_MUL,
  //    //MUL,
  //    MULHU,
  //    MULH,
  //    MULHSU
  //    ////FULL_PRODUCT_YIELD_RESULT_PIPE_3,
  //    //FULL_PRODUCT_YIELD_RESULT_PIPE_2,
  //    //FULL_PRODUCT_YIELD_RESULT_PIPE_1,
  //    //FULL_PRODUCT_YIELD_RESULT
  //    = newElement();
  //}
  //val rState = (
  //  Reg(State())
  //  init(State.IDLE_OR_MUL)
  //)
  dstVec(0) := (
    RegNext(
      dstVec(0),
      init=dstVec(0).getZero
    )
  )

  when (
    RegNext(multiCycleBus.nextValid, init=False)
    //&& rState.asBits(0)
    && RegNext(!multiCycleBus.sendData.kind.orR)
  ) {
    dstVec(0) := (
      RegNext(
        srcVec(0) * srcVec(1)
      ).resize(dstVec(0).getWidth)
    )
    multiCycleBus.ready := True
  }

  val fullProductNumPipeStages = 3

  def myFullProductOutpRangeHi = 63 downto 32
  val myHistValidMulhu = History[Bool](
    that=(
      RegNext(
        (
          multiCycleBus.nextValid
          && multiCycleBus.sendData.kind === 0x1
        ),
      )
    ),
    length=fullProductNumPipeStages,
    init=False
  )
  val myHistMulhu = History[UInt](
    that=(
      RegNext(
        srcVec(0) * srcVec(1)
      )(myFullProductOutpRangeHi)
    ),
    length=fullProductNumPipeStages,
  )


  val myHistValidMulh = History[Bool](
    that=(
      RegNext(
        (
          multiCycleBus.nextValid
          && multiCycleBus.sendData.kind === 0x2
        ),
      )
    ),
    length=fullProductNumPipeStages,
    init=False
  )
  val myHistMulh = History[SInt](
    that=(
      RegNext(
        srcVec(0).asSInt
        * srcVec(1).asSInt
      )(myFullProductOutpRangeHi)
    ),
    length=fullProductNumPipeStages,
  )

  val myHistValidMulhsu = History[Bool](
    that=(
      RegNext(
        (
          multiCycleBus.nextValid
          && multiCycleBus.sendData.kind.andR
        ),
      )
    ),
    length=fullProductNumPipeStages,
    init=False
  )
  val myHistMulhsu = History[UInt](
    that=(
      RegNext(
        Mux[UInt](
          srcVec(0).msb,
          (
            (-srcVec(0).asSInt).asUInt
            * srcVec(1)
          ),
          (
            srcVec(0)
            * srcVec(1)
          ),
        )
        //* srcVec(1)
      )(myFullProductOutpRangeHi)
    ),
    length=fullProductNumPipeStages,
  )
  switch (
    myHistValidMulhu.last
    ## myHistValidMulh.last
    ## myHistValidMulhsu.last
  ) {
    is (M"1--") {
      dstVec(0) := myHistMulhu.last
      multiCycleBus.ready := True
    }
    is (M"01-") {
      dstVec(0) := myHistMulh.last.asUInt
      multiCycleBus.ready := True
    }
    is (M"001") {
      dstVec(0) := myHistMulhsu.last
      multiCycleBus.ready := True
    }
    default {
    }
  }

  //switch (rState) {
  //  is (State.IDLE_OR_MUL) {
  //    switch (
  //      RegNext(multiCycleBus.nextValid, init=False)
  //      ## RegNext(multiCycleBus.sendData.kind)
  //    ) {
  //      is (B"101") {
  //        rState := State.MULHU
  //      }
  //      is (B"110") {
  //        rState := State.MULH
  //      }
  //      is (B"111") {
  //        rState := State.MULHSU
  //      }
  //      default {
  //      }
  //    }
  //  }
  //}
}
case class SnowHouseRiscv32Mul32(
  cpuIo: SnowHouseIo,
  //bridge: SnowHouseMcDualBusToMcBusBridge,
) extends Area {
  def cfg = cpuIo.cfg
  //def cfg = bridge.cfg

  val myMul = SnowHouseRiscv32Mul(cfg=cfg)
  //val innerMap = cfg.multiCycleOpInfoMap.get(MultiCycleOpGroup.Mul).get
  for (
    ((group, _), busIdx)
    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    if (group == MultiCycleOpGroup.Mul) {
      cpuIo.multiCycleBusVec(busIdx) <> myMul.io.multiCycleBus 
    }
  }
}
case class SnowHouseRiscv32Shift32LowLatency(
  cpuIo: SnowHouseIo,
  //bridge: SnowHouseMcDualBusToMcBusBridge,
) extends Area {
  def cfg = cpuIo.cfg
  //def cfg = bridge.cfg
  //val lslDel1 = SnowHouseLslDel1(mainWidth=cfg.mainWidth)
  //val lsrDel1 = SnowHouseLsrDel1(mainWidth=cfg.mainWidth)
  //val asrDel1 = SnowHouseAsrDel1(mainWidth=cfg.mainWidth)
  ////val sltuDel1 = SnowHouseSltDel1(
  ////  mainWidth=cfg.mainWidth, 
  ////  isSigned=false,
  ////)
  ////val sltsDel1 = SnowHouseSltDel1(
  ////  mainWidth=cfg.mainWidth, 
  ////  isSigned=true,
  ////)
  //lslDel1.io.inpToShift := (
  //  RegNext(
  //    next=lslDel1.io.inpToShift,
  //    init=lslDel1.io.inpToShift.getZero
  //  )
  //)
  //lslDel1.io.inpAmount := (
  //  RegNext(
  //    next=lslDel1.io.inpAmount,
  //    init=lslDel1.io.inpAmount.getZero
  //  )
  //)
  //lsrDel1.io.inpToShift := (
  //  RegNext(
  //    next=lsrDel1.io.inpToShift,
  //    init=lsrDel1.io.inpToShift.getZero
  //  )
  //)
  //lsrDel1.io.inpAmount := (
  //  RegNext(
  //    next=lsrDel1.io.inpAmount,
  //    init=lsrDel1.io.inpAmount.getZero
  //  )
  //)
  //asrDel1.io.inpToShift := (
  //  RegNext(
  //    next=asrDel1.io.inpToShift,
  //    init=asrDel1.io.inpToShift.getZero
  //  )
  //)
  //asrDel1.io.inpAmount := (
  //  RegNext(
  //    next=asrDel1.io.inpAmount,
  //    init=asrDel1.io.inpAmount.getZero
  //  )
  //)
  ////sltuDel1.io.inpA := (
  ////  RegNext(
  ////    next=sltuDel1.io.inpA,
  ////    init=sltuDel1.io.inpA.getZero
  ////  )
  ////)
  ////sltuDel1.io.inpB := (
  ////  RegNext(
  ////    next=sltuDel1.io.inpB,
  ////    init=sltuDel1.io.inpB.getZero
  ////  )
  ////)
  ////sltsDel1.io.inpA := (
  ////  RegNext(
  ////    next=sltsDel1.io.inpA,
  ////    init=sltsDel1.io.inpA.getZero
  ////  )
  ////)
  ////sltsDel1.io.inpB := (
  ////  RegNext(
  ////    next=sltsDel1.io.inpB,
  ////    init=sltsDel1.io.inpB.getZero
  ////  )
  ////)

  for (
    ((group, innerMap), busIdx)
    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    val multiCycleBus = (
      cpuIo.multiCycleBusVec(busIdx)
      //bridge.io.multiCycleBusVec(busIdx)
    )
    //multiCycleBus.ready := False
    def dstVec = multiCycleBus.recvData.dstVec
    def srcVec = multiCycleBus.sendData.srcVec
    if (
      group == MultiCycleOpKind.Lsl.group
      && multiCycleBus.sendData.kind != null
    ) {
      //dstVec.setAsReg() //init(dstVec.getZero)
      //srcVec.setAsReg() //init(srcVec.getZero)
      //multiCycleBus.ready.setAsReg() init(False)
      //multiCycleBus.ready := False
      //val rReady = Reg(Bool(), init=False)
      //rReady := False
      //multiCycleBus.ready := rReady
      multiCycleBus.ready := False
      switch (
        RegNext(multiCycleBus.nextValid, init=False)
        ## RegNext(multiCycleBus.sendData.kind)
      ) {
        for (
          //(multiCycleBus, busIdx) <- cpuIo.multiCycleBusVec.view.zipWithIndex
          ((_, opInfo), kindIdx)
          <- innerMap.view.zipWithIndex
        ) {
          val myKindWidth = multiCycleBus.sendData.kind.getWidth
          opInfo.multiCycleOp.get match {
            case MultiCycleOpKind.Lsl => {
              is (Cat(True, U(s"$myKindWidth'd$kindIdx"))) {
                //rReady := True
                multiCycleBus.ready := True
                val left = srcVec(0)
                val right = srcVec(1)(log2Up(cfg.mainWidth) - 1 downto 0)
                dstVec(0) := RegNext(
                  (left << right)(dstVec(0).bitsRange)
                )
              }
            }
            case MultiCycleOpKind.Lsr => {
              is (Cat(True, U(s"$myKindWidth'd$kindIdx"))) {
                //rReady := True
                multiCycleBus.ready := True
                val left = srcVec(0)
                val right = srcVec(1)(log2Up(cfg.mainWidth) - 1 downto 0)
                dstVec(0) := RegNext(left >> right)
              }
            }
            case MultiCycleOpKind.Asr => {
              is (Cat(True, U(s"$myKindWidth'd$kindIdx"))) {
                //rReady := True
                multiCycleBus.ready := True
                val left = srcVec(0)
                val right = srcVec(1)(log2Up(cfg.mainWidth) - 1 downto 0)
                dstVec(0) := RegNext((left.asSInt >> right).asUInt)
              }
            }
            //case MultiCycleOpKind.Sltu => {
            //  val multiCycleBus = cpuIo.multiCycleBusVec(busIdx)
            //  def dstVec = multiCycleBus.recvData.dstVec
            //  def srcVec = multiCycleBus.sendData.srcVec
            //  def mainWidth = cfg.mainWidth

            //  val width: Int=cfg.mainWidth
            //  val left = (
            //    srcVec(0)
            //  )
            //  val right = (
            //    srcVec(1)
            //  )
            //  val tempLeft = Cat(left).asUInt(width - 1 downto 0)
            //  val tempRight = Cat(right).asUInt(width - 1 downto 0)
            //  dstVec(0) := (
            //    RegNext(
            //      next=dstVec(0),
            //      init=dstVec(0).getZero,
            //    )
            //  )
            //  val rBusValidVec = (
            //    Vec.fill(2)(
            //      RegNext(
            //        next=multiCycleBus.nextValid,
            //        init=False
            //      )
            //    )
            //  )
            //  when (multiCycleBus.nextValid) {
            //    sltuDel1.io.inpA := tempLeft
            //    sltuDel1.io.inpB := tempRight
            //  }
            //  when (rBusValidVec(0)) {
            //    dstVec(0) := sltuDel1.io.outpResult
            //  }
            //  multiCycleBus.ready := (
            //    rBusValidVec(1)
            //  )
            //}
            //case MultiCycleOpKind.Slts => {
            //  val multiCycleBus = cpuIo.multiCycleBusVec(busIdx)
            //  def dstVec = multiCycleBus.recvData.dstVec
            //  def srcVec = multiCycleBus.sendData.srcVec
            //  def mainWidth = cfg.mainWidth

            //  val width: Int=cfg.mainWidth
            //  val left = (
            //    srcVec(0)
            //  )
            //  val right = (
            //    srcVec(1)
            //  )
            //  val tempLeft = Cat(left).asUInt(width - 1 downto 0)
            //  val tempRight = Cat(right).asUInt(width - 1 downto 0)
            //  dstVec(0) := (
            //    RegNext(
            //      next=dstVec(0),
            //      init=dstVec(0).getZero,
            //    )
            //  )
            //  val rBusValidVec = (
            //    Vec.fill(2)(
            //      RegNext(
            //        next=multiCycleBus.nextValid,
            //        init=False
            //      )
            //    )
            //  )
            //  when (multiCycleBus.nextValid) {
            //    sltsDel1.io.inpA := tempLeft
            //    sltsDel1.io.inpB := tempRight
            //  }
            //  when (rBusValidVec(0)) {
            //    dstVec(0) := sltsDel1.io.outpResult
            //  }
            //  multiCycleBus.ready := (
            //    rBusValidVec(1)
            //  )
            //}
            case _ => {
              //default {
              //  multiCycleBus.ready := False
              //}
            }
          }
        }
        default {
          dstVec.foreach(dst => dst := dst.getZero)
          multiCycleBus.ready := False
        }
      }
    }
  }
}
case class SnowHouseRiscv32MultiCycleInstrArea(
  cpuIo: SnowHouseIo
) extends Area {
  //for ((multiCycleBus, idx) <- cpuIo.multiCycleBusVec.view.zipWithIndex) {
  //  if (idx != 0) {
  //    multiCycleBus.ready := True
  //    multiCycleBus.recvData.dstVec.foreach(dst => {
  //      dst := dst.getZero
  //    })
  //  }
  //}
  //val lslRc = SnowHouseRiscv32Lsl32(cpuIo=cpuIo, immShift=false)
  //val lslImm = SnowHouseRiscv32Lsl32(cpuIo=cpuIo, immShift=true)
  //val lsrRc = SnowHouseRiscv32Lsr32(cpuIo=cpuIo, immShift=false)
  //val lsrImm = SnowHouseRiscv32Lsr32(cpuIo=cpuIo, immShift=true)
  //val asrRc = SnowHouseRiscv32Asr32(cpuIo=cpuIo, immShift=false)
  //val asrImm = SnowHouseRiscv32Asr32(cpuIo=cpuIo, immShift=true)
  //val bridgeArr = Array.fill(
  //  cpuIo.cfg.multiCycleOpInfoMap.view.size
  //)(
  //  SnowHouseMcDualBusToMcBusBridge(cfg=cpuIo.cfg)
  //)
  //val bridge = SnowHouseMcDualBusToMcBusBridge(cfg=cpuIo.cfg)

  //for (idx <- 0 until cpuIo.multiCycleH2dBusVec.size) {
  //  def myH2dBus = cpuIo.multiCycleH2dBusVec(idx)
  //  def myD2hBus = cpuIo.multiCycleD2hBusVec(idx)
  //  //myH2dBus.ready := RegNext(myH2dBus.valid, init=False)
  //  myH2dBus.ready := False
  //  myD2hBus.valid := False
  //  myD2hBus.payload := myD2hBus.payload.getZero

  //  val rState = Reg(Bool(), init=False)
  //  when (!rState) {
  //    when (RegNext(myH2dBus.valid, init=False)) {
  //      myH2dBus.ready := True
  //      rState := True
  //    }
  //  } otherwise {
  //    myD2hBus.valid := True
  //    when (myD2hBus.ready) {
  //      rState := False
  //    }
  //  }
  //  //when (myH2dBus.fire) {
  //  //  
  //  //}
  //  //myD2hBus.valid := 
  //}
  //cpuIo.multiCycleH2dBusVec.foreach(myH2dBus => {
  //  myH2dBus.ready := True
  //})

  //--------
  val shift32/*shiftSlt32*/ = (
    //SnowHouseRiscv32Shift32(cpuIo=cpuIo)
    //SnowHouseRiscv32ShiftSlt32LowLatency(cpuIo=cpuIo)
    SnowHouseRiscv32Shift32LowLatency(
      cpuIo=cpuIo
      //bridge=bridge
    )
  )
  //val cpyAdd32 = SnowHouseRiscv32CpyAdd32(cpuIo=cpuIo)
  val addMultiCycle = SnowHouseRiscv32AddMultiCycle(cpuIo=cpuIo)
  val mul32 = SnowHouseRiscv32Mul32(cpuIo=cpuIo)
  //val divmod32 = SnowHouseRiscv32Divmod32(cpuIo=cpuIo)
  //val divmodw = SnowHouseRiscv32Divmodw(cpuIo=cpuIo)
  val divmod = SnowHouseRiscv32Divmod(cpuIo=cpuIo)
  //--------
}
case class SnowHouseRiscv32WithoutRam(
  cfg: SnowHouseRiscv32Config
) extends Component {
  //--------
  val io = SnowHouseRiscv32WithoutRamIo(cfg=cfg)
  val cpu = SnowHouse(cfg=cfg.shCfg)
  //--------
  val multiCycleInstrArea = (
    SnowHouseRiscv32MultiCycleInstrArea(cpuIo=cpu.io)
  )
  //--------
  io.lcvIbus << cpu.io.lcvIbus
  io.lcvDbus << cpu.io.lcvDbus 
  //--------
  if (io.dbgInfo != null) {
    io.dbgInfo := cpu.io.dbgInfo
  }
}
case class SnowHouseRiscv32WithDuplDualRamIo(
  cfg: SnowHouseRiscv32Config
) extends Bundle {
  val dbgInfo = (
    cfg.dbgExposeExtrasAtRegFileWrite
  ) generate (
    out(SnowHouseDebugInfo(cfg=cfg.shCfg))
  )
  def regFileWriteData = (
    dbgInfo.regFileWriteData
  )
  def regFileWriteAddr = (
    dbgInfo.regFileWriteAddr
  )
  def regFileWriteEnable = (
    dbgInfo.regFileWriteEnable
  )
  def laggingRegPcAtRegFileWrite = (
    dbgInfo.laggingRegPcAtRegFileWrite
  )
  def shouldIgnoreInstrAtRegFileWrite = (
    dbgInfo.shouldIgnoreInstrAtRegFileWrite
  )
  def encInstrAtRegFileWrite = (
    dbgInfo.encInstrAtRegFileWrite
  )
}
case class SnowHouseRiscv32WithDuplDualRam(
  cfg: SnowHouseRiscv32Config
) extends Component {
  val io = SnowHouseRiscv32WithDuplDualRamIo(cfg=cfg)

  val cpu = SnowHouseRiscv32WithoutRam(cfg=cfg)
  val program = SnowHouseRam32InitFromBin(
    filename=cfg.programStr
  )

  val myMemDepth = 0x4000
  val myMemInitBigInt = {
    val depth = myMemDepth
    val tempArr = new ArrayBuffer[BigInt]()
    tempArr ++= program.view
    while (tempArr.size < depth) {
      tempArr += BigInt(0)
    }
    tempArr
  }

  //val myLcvIbusEtcCfg = cfg.shCfg.subCfg.lcvIbusEtcCfg.hiBusCfg

  //val myInstrDwAdapter = LcvBusSimpleBurstOnlyDataWidthDownAdapter(
  //  cfg=LcvBusDataWidthAdapterConfig(
  //    loBusMainCfg=myLcvIbusEtcCfg.mainCfg,
  //    hiBusDataWidth=cfg.shCfg.mainWidth,
  //    loBusCacheCfg=myLcvIbusEtcCfg.cacheCfg,
  //    hiBusCacheCfg=myLcvIbusEtcCfg.cacheCfg,
  //  )
  //)
  val myInstrMem = LcvBusMem(
    cfg=LcvBusMemConfig(
      busCfg=(
        cfg.shCfg.subCfg.lcvIbusEtcCfg.hiBusCfg
        //LcvBusConfig(
        //  mainCfg
        //)
        //myInstrDwAdapter.cfg.hiBusCfg
      ),
      depth=myMemDepth,
      initBigInt=Some(myMemInitBigInt),
    )
  )
  val icache = LcvBusCache(
    cfg=cfg.shCfg.subCfg.lcvIbusEtcCfg
  )

  //val myLcvDbusEtcCfg = cfg.shCfg.subCfg.lcvDbusEtcCfg.hiBusCfg
  //val myDataDwAdapter = LcvBusSimpleBurstOnlyDataWidthDownAdapter(
  //  cfg=LcvBusDataWidthAdapterConfig(
  //    loBusMainCfg=myLcvDbusEtcCfg.mainCfg,
  //    hiBusDataWidth=cfg.shCfg.mainWidth,
  //    loBusCacheCfg=myLcvDbusEtcCfg.cacheCfg,
  //    hiBusCacheCfg=myLcvDbusEtcCfg.cacheCfg,
  //  )
  //)
  val myDataMem = LcvBusMem(
    cfg=LcvBusMemConfig(
      busCfg=(
        cfg.shCfg.subCfg.lcvDbusEtcCfg.hiBusCfg
        //myDataDwAdapter.cfg.hiBusCfg
      ),
      depth=myMemDepth,
      initBigInt=Some(myMemInitBigInt),
    )
  )
  val dcache = LcvBusCache(
    cfg=cfg.shCfg.subCfg.lcvDbusEtcCfg
  )
  dcache.io.mmioHiBus.h2dBus.ready := False
  dcache.io.mmioHiBus.d2hBus.valid := False
  dcache.io.mmioHiBus.d2hBus.payload := (
    dcache.io.mmioHiBus.d2hBus.payload.getZero
  )

  //myInstrMem.io.bus << cpu.io.lcvIbus
  //myDataMem.io.bus <-/< cpu.io.lcvDbus
  cpu.io.lcvIbus.h2dBus.translateInto(icache.io.loBus.h2dBus)(
    dataAssignment=(outp, inp) => {
      outp.addr.allowOverride
      outp := inp
      outp.addr.msb := False
    }
  )
  cpu.io.lcvIbus.d2hBus << icache.io.loBus.d2hBus


  myInstrMem.io.bus <-/< icache.io.hiBus 
  //myInstrDwAdapter.io.loBus <-/< icache.io.hiBus
  //myInstrMem.io.bus <-/< myInstrDwAdapter.io.hiBus

  cpu.io.lcvDbus.h2dBus.translateInto(dcache.io.loBus.h2dBus)(
    dataAssignment=(outp, inp) => {
      outp.addr.allowOverride
      outp := inp
      outp.addr.msb := False
    }
  )
  cpu.io.lcvDbus.d2hBus << dcache.io.loBus.d2hBus

  //myDataDwAdapter.io.loBus <-/< dcache.io.hiBus
  //myDataMem.io.bus <-/< myDataDwAdapter.io.hiBus
  myDataMem.io.bus <-/< dcache.io.hiBus 

  if (io.dbgInfo != null) {
    io.dbgInfo := cpu.io.dbgInfo
  }
}


case class SnowHouseRiscv32WithSharedRamIo(
  cfg: SnowHouseRiscv32Config
) extends Bundle {
  val dbgInfo = (
    cfg.dbgExposeExtrasAtRegFileWrite
  ) generate (
    out(SnowHouseDebugInfo(cfg=cfg.shCfg))
  )
  def regFileWriteData = (
    dbgInfo.regFileWriteData
  )
  def regFileWriteAddr = (
    dbgInfo.regFileWriteAddr
  )
  def regFileWriteEnable = (
    dbgInfo.regFileWriteEnable
  )
  def laggingRegPcAtRegFileWrite = (
    dbgInfo.laggingRegPcAtRegFileWrite
  )
  def shouldIgnoreInstrAtRegFileWrite = (
    dbgInfo.shouldIgnoreInstrAtRegFileWrite
  )
  def encInstrAtRegFileWrite = (
    dbgInfo.encInstrAtRegFileWrite
  )
}
case class SnowHouseRiscv32WithSharedRam(
  cfg: SnowHouseRiscv32Config
) extends Component {
  val io = SnowHouseRiscv32WithSharedRamIo(cfg=cfg)

  val cpu = SnowHouseRiscv32WithoutRam(cfg=cfg)
  val program = SnowHouseRam32InitFromBin(
    filename=cfg.programStr
  )

  val myMemDepth = 0x4000
  val myMemInitBigInt = {
    val depth = myMemDepth
    val tempArr = new ArrayBuffer[BigInt]()
    tempArr ++= program.view
    while (tempArr.size < depth) {
      tempArr += BigInt(0)
    }
    tempArr
  }
  val sharedRam = SnowHouseLcvBusInstrDataSharedRam(
    cfg=cfg.shCfg,
    sharedInitBigInt=myMemInitBigInt
  )

  //val myInstrMem = LcvBusMem(
  //  cfg=LcvBusMemConfig(
  //    busCfg=cfg.shCfg.subCfg.lcvIbusEtcCfg.hiBusCfg,
  //    depth=myMemDepth,
  //    initBigInt=Some(myMemInitBigInt),
  //  )
  //)
  //val icache = LcvBusCache(
  //  cfg=cfg.shCfg.subCfg.lcvIbusEtcCfg
  //)

  //val myDataMem = LcvBusMem(
  //  cfg=LcvBusMemConfig(
  //    busCfg=cfg.shCfg.subCfg.lcvDbusEtcCfg.hiBusCfg,
  //    depth=myMemDepth,
  //    initBigInt=Some(myMemInitBigInt),
  //  )
  //)
  //val dcache = LcvBusCache(
  //  cfg=cfg.shCfg.subCfg.lcvDbusEtcCfg
  //)

  //myInstrMem.io.bus << cpu.io.lcvIbus
  //myDataMem.io.bus <-/< cpu.io.lcvDbus


  cpu.io.lcvIbus.h2dBus.translateInto(
    //icache.io.loBus.h2dBus
    sharedRam.io.lcvIbus.h2dBus
  )(
    dataAssignment=(outp, inp) => {
      outp.addr.allowOverride
      outp := inp
      outp.addr.msb := False
    }
  )
  cpu.io.lcvIbus.d2hBus <-/< (
    //icache.io.loBus.d2hBus
    sharedRam.io.lcvIbus.d2hBus
  )
  //myInstrMem.io.bus <-/< icache.io.hiBus 

  cpu.io.lcvDbus.h2dBus.translateInto(
    //dcache.io.loBus.h2dBus
    sharedRam.io.lcvDbus.h2dBus
  )(
    dataAssignment=(outp, inp) => {
      outp.addr.allowOverride
      outp := inp
      outp.addr.msb := False
    }
  )
  cpu.io.lcvDbus.d2hBus <-/< (
    //dcache.io.loBus.d2hBus
    sharedRam.io.lcvDbus.d2hBus
  )
  //myDataMem.io.bus <-/< dcache.io.hiBus 

  if (io.dbgInfo != null) {
    io.dbgInfo := cpu.io.dbgInfo
  }
}

object SnowHouseRiscv32WithoutRamToVerilog extends App {
  Config.spinal.generateVerilog({
    //val cfg = SnowHouseCpuConfig(
    //  optFormal=(
    //    false
    //  )
    //)
    val cfg = SnowHouseRiscv32Config(
      optFormal=(
        //true
        false
      ),
      //targetAltera=(
      //  true
      //),
      programStr=(
        "fl4shk-riscv-tests.ignore/rv32ui-p-lw.bin"
        //"test/snowhousecpu-test-1.bin"
        //"test/snowhousecpu-test-2.bin"
        //"test/snowhousecpu-test-3.bin"
        //"test/snowhousecpu-test-4.bin"
        //"test/snowhousecpu-test-5.bin"
      ),
      instrRamKind=(
        0//,
        //1,
        //2,
        //5
      ),
      //instrRamFetchLatency=(
      //  2
      //),
      //exposeRegFileWriteDataToIo=true,
    )
    //val testProgram = SnowHouseCpuTestProgram(cfg=cfg)
    SnowHouseRiscv32WithoutRam(cfg=cfg)
  })
}
object SnowHouseRiscv32TestProgramArr {
  val programStrNoExtBasenameArr = Array[String](
    "rv32ui-p-lw",
    "rv32ui-p-sw",
    //"rv32ui-p-lhu",
    //"rv32ui-p-lbu",
    //"rv32ui-p-sb",
    //"rv32ui-p-sh",
    //"rv32ui-p-lh",
    //"rv32ui-p-lb",
    //"rv32ui-p-st_ld",
    //"rv32ui-p-ld_st",
    //"rv32ui-p-jalr",
    //"rv32ui-p-jal",
    //--------
    //"rv32ui-p-slti",
    //"rv32ui-p-or",
    //"rv32ui-p-andi",
    //"rv32ui-p-and",
    //"rv32ui-p-slt",
    //"rv32ui-p-sra",
    //"rv32ui-p-simple",
    //"rv32ui-p-xori",
    //"rv32ui-p-sltiu",
    //"rv32ui-p-srli",
    //"rv32ui-p-blt",
    //"rv32ui-p-srai",

    ////"rv32ui-p-ma_data", // fails
    //"rv32ui-p-auipc",
    //"rv32ui-p-sll",
    //"rv32ui-p-addi",
    //"rv32ui-p-xor",
    //"rv32ui-p-sltu",
    //"rv32ui-p-sub",
    //"rv32ui-p-beq",
    //"rv32ui-p-srl",
    //"rv32ui-p-ori",
    //"rv32ui-p-slli",
    //"rv32ui-p-add",
    //"rv32ui-p-bgeu",
    //"rv32ui-p-lui",
    //"rv32ui-p-bltu",
    //"rv32ui-p-bge",
    //"rv32ui-p-bne",
  )
}
object SnowHouseRiscv32WithDuplDualRamSim extends App {
  
  val programStrNoExtBasenameArr = (
    SnowHouseRiscv32TestProgramArr.programStrNoExtBasenameArr
  )

  val testOptTwoCycleRegFileReads = (
    //true
    false
  )

  //val instrRamKindArr = Array[Int](
  //  0,
  //  //1,
  //  //2,
  //  5,
  //)
  for (testIdx <- 0 until programStrNoExtBasenameArr.size) {
    val programStrNoExtBasename = programStrNoExtBasenameArr(testIdx)
    val programStr = (
      "fl4shk-riscv-tests.ignore/"
      + programStrNoExtBasename
      + ".bin"
    )

    val numClkCycles = (
      8192 * 2
    )

    val cfg = SnowHouseRiscv32Config(
      optFormal=(
        //true
        false
      ),
      //targetAltera=(
      //  true
      //),
      programStr=(
        programStr
      ),
      instrRamKind=(
        //instrRamKind
        0
      ),
      dbgExposeExtrasAtRegFileWrite=true,
    )
    Config.sim.compile({
      val toComp = (
        SnowHouseRiscv32WithDuplDualRam(
          cfg=cfg
        )
      )
      //toComp.setDefinitionName(
      //  s"SnowHouseCpuWithDualRam_${testIdx}_${instrRamKind}"
      //)
      toComp
    }).doSim{dut => {
      val pw = new PrintWriter(new File(
        //s"test/results/test-${testIdx}-results-${instrRamKind}.txt"
        s"fl4shk-riscv-tests.ignore/results/"
        + s"${programStrNoExtBasename}-results.txt"
      ))
      pw.write(
        s"Starting test:"
        //+ s"programStr:${programStr} instrRamKind:${instrRamKind}"
        + s"programStr:${programStr}"
        + s"\n"
      )
      val mySavedGprArr = new ArrayBuffer[Long]()
      for (idx <- 0 until cfg.numGprs) {
        mySavedGprArr += 0.toLong
      }

      dut.clockDomain.forkStimulus(10)
      for (i <- 0 until numClkCycles) {
        dut.clockDomain.waitSampling()
        val myRegFileWriteEnable = dut.io.regFileWriteEnable.toBoolean
        val myRegFileWriteAddr = dut.io.regFileWriteAddr.toLong
        val myRegFileWriteData = dut.io.regFileWriteData.toLong
        val myLaggingRegPc = dut.io.laggingRegPcAtRegFileWrite.toLong

        if (myRegFileWriteEnable) {
          if (
            myRegFileWriteData
            != mySavedGprArr(myRegFileWriteAddr.toInt)
          ) {
            pw.write(
              s"pc:${myLaggingRegPc}    "
              //s""
              + s"addr:${myRegFileWriteAddr} "
              + s"data:${myRegFileWriteData}\n"
            )
            mySavedGprArr(myRegFileWriteAddr.toInt) = myRegFileWriteData
            //for (idx <- 0 until mySavedGprArr.size) {
            //  tempStr += s"r${idx}=${mySavedGprArr(idx)}"
            //  if (idx + 1 < mySavedGprArr.size) {
            //    tempStr += " "
            //  } else {
            //    tempStr += "\n\n"
            //  }
            //}
            //pw.write(tempStr)
          }
        }
        //if (!grabRegFileOutputs) {
        //} else {
        //}
        //for (gprIdx <- 0 until cfg.numGprs) {
        //  printf(
        //    "r%i=%x ",
        //    gprIdx,
        //    dut.cpu.regFile.modMem(0)(0).readAsync(
        //      address=gprIdx
        //    ).toInt
        //  )
        //  if (gprIdx % 4 == 3) {
        //    printf("\n")
        //  }
        //}
      }
      pw.write(
        s"Ending test.\n\n"
      )
      pw.close()
    }}
  }
}

object SnowHouseRiscv32WithSharedRamSim extends App {
  
  val programStrNoExtBasenameArr = (
    SnowHouseRiscv32TestProgramArr.programStrNoExtBasenameArr
  )

  val testOptTwoCycleRegFileReads = (
    //true
    false
  )

  //val instrRamKindArr = Array[Int](
  //  0,
  //  //1,
  //  //2,
  //  5,
  //)
  for (testIdx <- 0 until programStrNoExtBasenameArr.size) {
    val programStrNoExtBasename = programStrNoExtBasenameArr(testIdx)
    val programStr = (
      "fl4shk-riscv-tests.ignore/"
      + programStrNoExtBasename
      + ".bin"
    )

    val numClkCycles = (
      8192 * 2
    )

    val cfg = SnowHouseRiscv32Config(
      optFormal=(
        //true
        false
      ),
      //targetAltera=(
      //  true
      //),
      programStr=(
        programStr
      ),
      instrRamKind=(
        //instrRamKind
        0
      ),
      dbgExposeExtrasAtRegFileWrite=true,
    )
    Config.sim.compile({
      val toComp = (
        SnowHouseRiscv32WithSharedRam(
          cfg=cfg
        )
      )
      //toComp.setDefinitionName(
      //  s"SnowHouseCpuWithDualRam_${testIdx}_${instrRamKind}"
      //)
      toComp
    }).doSim{dut => {
      val pw = new PrintWriter(new File(
        //s"test/results/test-${testIdx}-results-${instrRamKind}.txt"
        s"fl4shk-riscv-tests.ignore/results/"
        + s"${programStrNoExtBasename}-results.txt"
      ))
      pw.write(
        s"Starting test:"
        //+ s"programStr:${programStr} instrRamKind:${instrRamKind}"
        + s"programStr:${programStr}"
        + s"\n"
      )
      val mySavedGprArr = new ArrayBuffer[Long]()
      for (idx <- 0 until cfg.numGprs) {
        mySavedGprArr += 0.toLong
      }

      dut.clockDomain.forkStimulus(10)
      for (i <- 0 until numClkCycles) {
        dut.clockDomain.waitSampling()
        val myRegFileWriteEnable = dut.io.regFileWriteEnable.toBoolean
        val myRegFileWriteAddr = dut.io.regFileWriteAddr.toLong
        val myRegFileWriteData = dut.io.regFileWriteData.toLong
        val myLaggingRegPc = dut.io.laggingRegPcAtRegFileWrite.toLong

        if (myRegFileWriteEnable) {
          if (
            myRegFileWriteData
            != mySavedGprArr(myRegFileWriteAddr.toInt)
          ) {
            pw.write(
              s"pc:${myLaggingRegPc}    "
              //s""
              + s"addr:${myRegFileWriteAddr} "
              + s"data:${myRegFileWriteData}\n"
            )
            mySavedGprArr(myRegFileWriteAddr.toInt) = myRegFileWriteData
            //for (idx <- 0 until mySavedGprArr.size) {
            //  tempStr += s"r${idx}=${mySavedGprArr(idx)}"
            //  if (idx + 1 < mySavedGprArr.size) {
            //    tempStr += " "
            //  } else {
            //    tempStr += "\n\n"
            //  }
            //}
            //pw.write(tempStr)
          }
        }
        //if (!grabRegFileOutputs) {
        //} else {
        //}
        //for (gprIdx <- 0 until cfg.numGprs) {
        //  printf(
        //    "r%i=%x ",
        //    gprIdx,
        //    dut.cpu.regFile.modMem(0)(0).readAsync(
        //      address=gprIdx
        //    ).toInt
        //  )
        //  if (gprIdx % 4 == 3) {
        //    printf("\n")
        //  }
        //}
      }
      pw.write(
        s"Ending test.\n\n"
      )
      pw.close()
    }}
  }
}
//object Riscv32Op {
//  private var _opCnt: Int = 0
//}
