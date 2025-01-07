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

object SnowHouseCpuInstrEnc {
  val numOps: Int = 16
  val opWidth: Int = log2Up(numOps)
  val numGprs: Int = 16
  val gprIdxWidth: Int = log2Up(numGprs)
  val simmWidth: Int = 16
}
object SnowHouseCpuOp {
  //private var _rawOpCnt: Int = 0
  private var _opCnt: Int = 0
  def mkOp(
    //opAsInt: Int,
    //lim: Int,
    name: String,
    kind: Int,
    update: Boolean,
    //kindLim: Int,
  ): (/*UInt,*/ Int, Int, String) = {
    val ret = (
      //U(s"${SnowHouseCpuInstrEnc.opWidth}'d${opAsInt}"),
      //opAsInt,
      //(
      //  _rawOpCnt
      //  /// SnowHouseCpuInstrEnc.numGprs
      //  >> 1
      //),
      _opCnt,
      kind,
      name,
    )
    if (update) {
      _opCnt += 1
    }
    //_rawOpCnt += 1
    ret
  }
  //--------
  val AddRaRbRc = mkOp("AddRaRbRc", AddKindRc, false)             // 0, 0
  val AddRaRbSimm16 = mkOp("AddRaRbSimm16", AddKindSimm16, true)  // 0, 1
  def AddKindRc = 0x0
  def AddKindSimm16 = 0x1

  val SubRaRbRc = mkOp("SubRaRbRc", SubKindRc, false)           // 1, 0
  //val SubRaRbSimm16 = mkOp("SubRaRbSimm16", SubKindSimm16, true)
  val SubReserved = mkOp("SubReserved", SubKindReserved, true)  // 1, 1
  def SubKindRc = 0x0
  //def SubKindSimm16 = 0x1
  def SubKindReserved = 0x1

  val SltuRaRbRc = mkOp("SltuRaRbRc", SltKindSltuRc, false)       // 2, 0
  val SltsRaRbRc = mkOp("SltsRaRbRc", SltKindSltsRc, true)        // 2, 1
  def SltKindSltuRc = 0x0
  def SltKindSltsRc = 0x1
  //--------
  val XorRaRbRc = mkOp("XorRaRbRc", XorKindRc, false)             // 3, 0
  val XorRaRbSimm16 = mkOp("XorRaRbSimm16", XorKindSimm16, true)  // 3, 1
  def XorKindRc = 0x0
  def XorKindSimm16 = 0x1

  val OrrRaRbRc = mkOp("OrrRaRbRc", OrrKindRc, false)             // 4, 0
  val OrrRaRbSimm16 = mkOp("OrrRaRbSimm16", OrrKindSimm16, true)  // 4, 1
  def OrrKindRc = 0x0
  def OrrKindSimm16 = 0x1

  // `AndRaRbRc` is moved down here to help with synthesis/routing of
  // executing/decoding instructions
  val AndRaRbRc = mkOp("AndRaRbRc", AndKindRc, true)              // 5, 0
  def AndKindRc = 0x0

  val LslRaRbRc = mkOp("LslRaRbRc", ShiftKindLslRc, false)  // 6, 0
  val LsrRaRbRc = mkOp("LsrRaRbRc", ShiftKindLsrRc, false)  // 6, 1
  val AsrRaRbRc = mkOp("AsrRaRbRc", ShiftKindAsrRc, true)   // 6, 2
  def ShiftKindLslRc = 0x0
  def ShiftKindLsrRc = 0x1
  def ShiftKindAsrRc = 0x2

  val AddRaPcSimm16 = mkOp("AddRaPcSimm16", AddPcKindMain, true)  // 7, 0
  def AddPcKindMain = 0x0
  //val Asr = mkOp("Asr")               // 8
  val MulRaRbRc = mkOp("MulRaRbRc", MulitCycleKindMulRc, true)  // 8, 0
  def MulitCycleKindMulRc = 0x0
  //def MultiCycleKindUdivRc = 0x1
  //def MultiCycleKindSdivRc = 0x2
  //def MultiCycleKindUmodRc = 0x3
  //def MultiCycleKindSmodRc = 0x3

  val LdrRaRbRc = mkOp("LdrRaRbRc", LdstKindRc, false)            // 9, 0
  val LdrRaRbSimm16 = mkOp("LdrRaRbSimm16", LdstKindSimm16, true) // 9, 1

  val StrRaRbRc = mkOp("StrRaRbRc", LdstKindRc, false)            // 10, 0
  val StrRaRbSimm16 = mkOp("StrRaRbSimm16", LdstKindRc, true)     // 10, 1
  def LdstKindRc = 0x0
  def LdstKindSimm16 = 0x1
  //--------
  val BeqRaRbSimm = mkOp("BeqRaRbSimm", JmpKindBeq, false)      // 11, 0
  val BneRaRbSimm = mkOp("BneRaRbSimm", JmpKindBne, false)      // 11, 1
  val JmpRa = mkOp("JmpRa", JmpKindJmpRa, false)                // 11, 2
  val JmpReserved = mkOp("JmpReserved", JmpKindReserved, true)  // 11, 3
  def JmpKindBeq = 0x0
  def JmpKindBne = 0x1
  def JmpKindJmpRa = 0x2
  def JmpKindReserved = 0x3

  //val CpyRaRb = mkOp("CpyRaRb", CpyKindCpyRb, false)              // 12, 0
  //val CpyRaSimm16 = mkOp("CpyRaSimm16", CpyKindCpySimm16, false)  // 12, 1
  val CpyuRaRb = mkOp("CpyuRaRb", CpyKindCpyuRb, false)           // 12, 2
  val CpyuRaSimm16 = mkOp(                                        // 12, 3
    "CpyuRaSimm16", CpyKindCpyuSimm16, true
  )
  //val OldCpy = mkOp("OldCpy")           // 15
  //def CpyKindCpyRb = 0x0
  //def CpyKindCpySimm16 = 0x1
  def CpyKindCpyuRb = 0x0//0x2
  def CpyKindCpyuSimm16 = 0x1//0x3
  //--------
  val OpLim = _opCnt
  assert(
    OpLim == 13,
    s"eek! "
    + s"${OpLim} != 13"
  )
}
case class SnowHouseCpuEncInstr(
) extends PackedBundle {
  val imm16 = UInt(SnowHouseCpuInstrEnc.simmWidth bits)
  val rcIdx = UInt(SnowHouseCpuInstrEnc.gprIdxWidth bits)
  val rbIdx = UInt(SnowHouseCpuInstrEnc.gprIdxWidth bits)
  val raIdx = UInt(SnowHouseCpuInstrEnc.gprIdxWidth bits)
  val op = UInt(SnowHouseCpuInstrEnc.opWidth bits)
}
object SnowHouseCpuPipeStageInstrDecode {
  def apply(
    psId: SnowHousePipeStageInstrDecode
  ) = new Area {
    import SnowHouseCpuOp._
    def upPayload = psId.upPayload
    def io = psId.io
    def cfg = psId.cfg
    val encInstr = (
      KeepAttribute(
        SnowHouseCpuEncInstr()
      )
      .setName("InstrDecode_encInstr")
    )
    encInstr.assignFromBits(io.ibus.devData.instr.asBits)
    for (idx <- 0 until cfg.maxNumGprsPerInstr) {
      upPayload.gprIsZeroVec(idx) := (
        upPayload.gprIdxVec(idx) === 0x0
      )
    }
    upPayload.gprIdxVec(0) := encInstr.raIdx
    upPayload.gprIdxVec(1) := encInstr.rbIdx
    upPayload.gprIdxVec(2) := encInstr.rcIdx
    val tempImm = Cat(
      Mux[UInt](
        encInstr.imm16.msb,
        U"16'hffff",
        U"16'h0000",
      ),
      encInstr.imm16.asSInt
    ).asUInt
    upPayload.imm := tempImm
    def setOp(
      someOp: (Int, Int, String)
    ): Unit = {
      for (
        ((tuple, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex
      ) {
        if (someOp == tuple) {
          println(
            s"tuple: ${tuple} ${opInfoIdx}"
          )
          upPayload.op := opInfoIdx
          return
        }
      }
      assert(
        false,
        s"eek! ${someOp}"
      )
    }
    def doDefault(): Unit = {
      // this function could be useful for decoding variable width
      // instructions!
      // just do a NOP
      setOp(AddRaRbRc)
      upPayload.gprIdxVec.foreach{gprIdx => {
        gprIdx := 0x0
      }}
      upPayload.imm := 0x0
    }
    switch (encInstr.op) {
      is (AddRaRbRc._1) {
        when (encInstr.rcIdx =/= 0x0) {
          setOp(AddRaRbRc)
        } otherwise {
          setOp(AddRaRbSimm16)
        }
      }
      is (SubRaRbRc._1) {
        //when (encInstr.rcIdx =/= 0x0) {
          setOp(SubRaRbRc)
        //} otherwise {
        //  setOp(SubRaRbSimm16)
        //}
      }
      is (SltuRaRbRc._1) {
        switch (encInstr.imm16(0 downto 0)) {
          is (SltuRaRbRc._2) {
            setOp(SltuRaRbRc)
          }
          is (SltsRaRbRc._2) {
            setOp(SltsRaRbRc)
          }
        }
      }
      is (XorRaRbRc._1) {
        when (encInstr.rcIdx =/= 0x0) {
          setOp(XorRaRbRc)
        } otherwise {
          setOp(XorRaRbSimm16)
        }
      }
      is (OrrRaRbRc._1) {
        when (encInstr.rcIdx =/= 0x0) {
          setOp(OrrRaRbRc)
        } otherwise {
          setOp(OrrRaRbSimm16)
        }
      }
      is (AndRaRbRc._1) {
        setOp(AndRaRbRc)
      }
      is (LslRaRbRc._1) {
        switch (encInstr.imm16(1 downto 0)) {
          is (LslRaRbRc._2) {
            setOp(LslRaRbRc)
          }
          is (LsrRaRbRc._2) {
            setOp(LsrRaRbRc)
          }
          is (AsrRaRbRc._2) {
            setOp(AsrRaRbRc)
          }
          default {
            doDefault()
          }
        }
      }
      is (AddRaPcSimm16._1) {
        setOp(AddRaPcSimm16)
      }
      is (MulRaRbRc._1) {
        setOp(MulRaRbRc)
      }
      is (LdrRaRbRc._1) {
        when (encInstr.rcIdx =/= 0x0) {
          setOp(LdrRaRbRc)
        } otherwise {
          setOp(LdrRaRbSimm16)
        }
      }
      is (StrRaRbRc._1) {
        when (encInstr.rcIdx =/= 0x0) {
          setOp(StrRaRbRc)
        } otherwise {
          setOp(StrRaRbSimm16)
        }
      }
      is (BeqRaRbSimm._1) {
        switch (encInstr.rcIdx(1 downto 0)) {
          is (BeqRaRbSimm._2) {
            setOp(BeqRaRbSimm)
          }
          is (BneRaRbSimm._2) {
            setOp(BneRaRbSimm)
          }
          is (JmpRa._2) {
            setOp(JmpRa)
          }
          default {
            doDefault()
          }
        }
      }
      is (CpyuRaRb._1) {
        switch (encInstr.rcIdx(0 downto 0)) {
          //is (CpyRaRb._2) {
          //  setOp(CpyRaRb)
          //}
          //is (CpyRaSimm16._2) {
          //  setOp(CpyRaSimm16)
          //}
          is (CpyuRaRb._2) {
            setOp(CpyuRaRb)
          }
          is (CpyuRaSimm16._2) {
            setOp(CpyuRaSimm16)
          }
        }
      }
      default {
        doDefault()
      }
    }
    //upPayload.op := encInstr.op
  }
}
//case class SnowHouseCpuPipeStageInstrDecode(
//  //args: 
//  override val args: SnowHousePipeStageArgs,
//  override val psIdHaltIt: Bool,
//  override val psExSetPc: Flow[SnowHousePsExSetPcPayload],
//) extends SnowHousePipeStageInstrDecode(
//  args=args,
//  psIdHaltIt=psIdHaltIt,
//  psExSetPc=psExSetPc,
//) {
//  def doDecode() = new Area {
//  }
//  //def cfg = super.cfg
//  //def opInfoMap = super.opInfoMap
//  //def io = super.io
//  //def cId = super.cId
//  //def payload = super.payload
//  //def optFormal = super.optFormal
//  //private val _decInstr: UInt = U"32'd0"
//  //def decInstr: UInt = _decInstr
//  //args match {
//  //  case Some(args) => {
//  //    //--------
//  //    def cfg = super.cfg
//  //    def opInfoMap = super.opInfoMap
//  //    def io = super.io
//  //    def cId = super.cId
//  //    def payload = super.payload
//  //    def optFormal = super.optFormal
//  //    //--------
//  //    //assert(opInfoMap != null)
//  //    //assert(io != null)
//  //    //assert(cId != null)
//  //    //--------
//  //    when (cId.up.isFiring) {
//  //    }
//  //    //--------
//  //  }
//  //  case None => {
//  //    assert(false)
//  //  }
//  //}
//}
object SnowHouseCpuOpInfoMap {
  //--------
  val opInfoMap = LinkedHashMap[Any, OpInfo]()
  //--------
  opInfoMap += (
    // add rA, rB, rC
    SnowHouseCpuOp.AddRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Add,
    )
  )
  opInfoMap += (
    // add rA, rB, simm16
    SnowHouseCpuOp.AddRaRbSimm16 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      aluOp=AluOpKind.Add,
    )
  )
  //--------
  opInfoMap += (
    // sub rA, rB, rC
    SnowHouseCpuOp.SubRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Sub,
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.SubReserved -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr),
      cpyOp=CpyOpKind.Cpy,
    )
    //// sub rA, rB, simm16
    //SnowHouseCpuOp.SubRaRbSimm16 -> OpInfo.mkAlu(
    //  dstArr=Array[DstKind](DstKind.Gpr),
    //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
    //  aluOp=AluOpKind.Sub,
    //)
  )
  //--------
  opInfoMap += (
    // sltu rA, rB, rC
    SnowHouseCpuOp.SltuRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Sltu,
    )
  )
  opInfoMap += (
    // slts rA, rB, rC
    SnowHouseCpuOp.SltsRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Slts,
    )
  )
  //opInfoMap += (
  //  SnowHouseCpuOp.SltsRaRbRc -> OpInfo.mkAlu(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
  //    aluOp=AluOpKind.Slts,
  //  )
  //)
  //--------
  opInfoMap += (
    SnowHouseCpuOp.XorRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Xor,
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.XorRaRbSimm16 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      aluOp=AluOpKind.Xor,
    )
  )
  //--------
  opInfoMap += (
    SnowHouseCpuOp.OrrRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Or,
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.OrrRaRbSimm16 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      aluOp=AluOpKind.Or,
    )
  )
  //--------
  opInfoMap += (
    // and rA, rB, rC
    SnowHouseCpuOp.AndRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.And,
    )
  )
  //opInfoMap += (
  //  SnowHouseCpuOp.And -> OpInfo.mkAlu(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
  //    aluOp=AluOpKind.And,
  //  )
  //)
  //--------
  opInfoMap += (
    // lsl rA, rB, rC
    SnowHouseCpuOp.LslRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Lsl,
    )
  )
  opInfoMap += (
    // lsr rA, rB, rC
    SnowHouseCpuOp.LsrRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Lsr,
    )
  )
  opInfoMap += (
    // asr rA, rB, rC
    SnowHouseCpuOp.AsrRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Asr,
    )
  )
  opInfoMap += (
    // add rA, pc, simm16
    SnowHouseCpuOp.AddRaPcSimm16 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Pc, SrcKind.Imm(/*Some(true)*/)),
      aluOp=AluOpKind.Add,
    )
  )
  opInfoMap += (
    // mul rA, rB, rC
    SnowHouseCpuOp.MulRaRbRc -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Umul,
    )
  )
  //--------
  opInfoMap += (
    SnowHouseCpuOp.LdrRaRbRc -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      //loadOp=LoadOpKind.LdU32,
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(false)),
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.LdrRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      //loadOp=LoadOpKind.LdU32,
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(false)),
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.StrRaRbRc -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      //storeOp=StoreOpKind.St32,
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(true)),
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.StrRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      //storeOp=StoreOpKind.St32,
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(true)),
    )
  )
  //--------
  opInfoMap += (
    SnowHouseCpuOp.BeqRaRbSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Eq,
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.BneRaRbSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Ne
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.JmpRa -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](SrcKind.Gpr),
      cpyOp=CpyOpKind.Jmp,
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.JmpReserved -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](SrcKind.Gpr),
      cpyOp=CpyOpKind.Jmp,
    )
  )
  //--------
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyRaRb -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Gpr),
  //    cpyOp=CpyOpKind.Cpy,
  //  )
  //)
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyRaSimm16 -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Imm(/*Some(true)*/)),
  //    cpyOp=CpyOpKind.Cpy,
  //  )
  //)
  opInfoMap += (
    SnowHouseCpuOp.CpyuRaRb -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr),
      cpyOp=CpyOpKind.Cpyu,
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.CpyuRaSimm16 -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Imm(/*Some(true)*/)),
      cpyOp=CpyOpKind.Cpyu,
    )
  )
  //--------
}
case class SnowHouseCpuConfig(
  optFormal: Boolean,
) {
  //--------
  val instrMainWidth = 32
  val mainWidth = (
    32
    //16
  )
  val numGprs = SnowHouseCpuInstrEnc.numGprs
  val modRdPortCnt = 3
  val pipeName="SnowHouseCpu"
  //--------
  val shCfg = SnowHouseConfig(
    haveZeroReg=Some(0),
    //encInstrType=SnowHouseCpuEncInstr(),
    instrMainWidth=instrMainWidth,
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
    ),
    opInfoMap=SnowHouseCpuOpInfoMap.opInfoMap,
    //psDecode=SnowHouseCpuPipeStageInstrDecode(),
    //mkPipeStageInstrDecode=(
    //  (
    //    args,
    //    psIdHaltIt,
    //    psExSetPc,
    //  ) => (
    //    SnowHouseCpuPipeStageInstrDecode(
    //      args=args,
    //      psIdHaltIt=psIdHaltIt,
    //      psExSetPc=psExSetPc,
    //    )
    //  )
    //),
    doInstrDecodeFunc=SnowHouseCpuPipeStageInstrDecode.apply,
    //decodeFunc=(
    //  io: SnowHouseIo[SnowHouseCpuEncInstr],
    //  cId: CtrlLink,
    //  decInstr: UInt,
    //) => new Area {
    //  //decInstr := U"${mainWidth}'d0"
    //},
    optFormal=optFormal,
    //maxNumGprsPerInstr=3,
  )
  //--------
  val program = ArrayBuffer[AsmStmt]()
}

case class SnowHouseCpuProgram(
  cfg: SnowHouseCpuConfig
) {
  val outpArr = ArrayBuffer[BigInt]()
  val assembler = SnowHouseCpuAssembler(
    stmtArr=cfg.program,
    outpArr=outpArr,
  )
  def doPrint(): Unit = {
    for ((encoded, idx) <- outpArr.view.zipWithIndex) {
      printf(
        //s"encoded ${idx}: ${encoded}"
        "%X: %X\n",
        idx << 2,
        (encoded.toLong & 0xffffffff).toInt
      )
    }
  }
}
case class SnowHouseCpuTestProgram(
  cfg: SnowHouseCpuConfig,
) {
  import SnowHouseCpuRegs._
  //val program = ArrayBuffer[AsmStmt]()
  //val cfg = SnowHouseCpuConfig(
  //  optFormal=false,
  //  program=program,
  //)
  //def program = cfg.program
  import libsnowhouse.Label._
  val tempData: Int = 0x17000
  cfg.program ++= Array[AsmStmt](
    //--------
    cpy(r0, 0x0),         // 0: r0 = 0
                          // instruction fetcher has trouble
                          // reading first instruction, so put
                          // in a dummy
    cpy(r0, 0x0),         // 4: r0 = 0
    cpy(r1, 0x8),         // 0x8: r1 = 8
    cpy(r2, 0x1),         // 0xc: r2 = 1
    cpy(r3, 0x1000),      // 0x10: r3 = 0x1000
    cpy(r4, 0x8),         // 0x14: r4 = 4
    ////cpy(r5, 0x0),
    cpy(r5, 0x20),        // 0x18: r5 = 0x20
    str(r5, r0, r3),      // 0x1c: [r0 + r3] = r5
    //--------
    Lb"loop",
    //add(r0, r1, r2),
    //cpyu(r2, tempData >> 16),
    //cpy(r2, tempData & 0xffff),
    ldr(r5, r3, 0x0),     // 0x20:
    add(r5, r5, 0x1),     // 0x24:
    str(r5, r3, 0x4),     // 0x28:
    add(r3, r3, 0x4),     // 0x2c: r3 += 4
    sub(r1, r1, 0x1),     // 0x30: r1 -= 1 
    bnz(r1, LbR"loop"),   // 0x34: if (r1 != 0) goto LbR"loop"
    //--------
    cpy(r12, 0x0),        // 0x38
    Lb"infin",
    //--------
    bz(r12, LbR"infin"),  // 0x3c
    //Db32(0x3f),
    //--------
  )
  val program = SnowHouseCpuProgram(cfg=cfg)
  //val outpArr = ArrayBuffer[BigInt]()
  //val assembler = SnowHouseCpuAssembler(
  //  stmtArr=program,
  //  outpArr=outpArr,
  //)
}
case class SnowHouseCpuWithDualRamIo(
  program: SnowHouseCpuProgram,
) extends Bundle {
  def cfg = program.cfg
}
case class SnowHouseCpuWithDualRam(
  program: SnowHouseCpuProgram,
) extends Component {
  val io = SnowHouseCpuWithDualRamIo(program=program)
  def cfg = program.cfg
  val cpu = SnowHouse(cfg=cfg.shCfg)
  val dualRam = SnowHouseInstrDataDualRam(
    cfg=cfg.shCfg,
    instrInitBigInt=program.outpArr,
    dataInitBigInt=(
      Array.fill(1 << 16)(BigInt(0))
    ),
  )
  cpu.io.ibus <> dualRam.io.ibus
  cpu.io.dbus <> dualRam.io.dbus
}
object SnowHouseCpuWithDualRamSim extends App {
  //Config.spinal.generateVerilog({
  //  val cfg = SnowHouseCpuConfig(
  //    optFormal=(
  //      //true
  //      false
  //    )
  //  )
  //  SnowHouse(
  //    cfg=cfg.shCfg
  //  )
  //})
  val cfg = SnowHouseCpuConfig(
    optFormal=(
      //true
      false
    )
  )
  val testProgram = SnowHouseCpuTestProgram(cfg=cfg)
  SimConfig.withVcdWave.compile(
    SnowHouseCpuWithDualRam(program=testProgram.program)
  ).doSim{dut => {
    dut.clockDomain.forkStimulus(10)
    for (i <- 0 until 256) {
      dut.clockDomain.waitSampling()
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
  }}
}
object SnowHouseCpuToVerilog extends App {
  Config.spinal.generateVerilog({
    val cfg = SnowHouseCpuConfig(
      optFormal=(
        //true
        false
      )
    )
    SnowHouse(
      cfg=cfg.shCfg
    )
  })
}
object SnowHouseCpuFormal extends App {
  //--------
  //--------
  def myProveNumCycles = (
    //8
    10
    //15
    //16
  )
  case class SnowHouseCpuFormalDut() extends Component {
    val dut = FormalDut(
      SnowHouse(
        cfg=SnowHouseCpuConfig(
          optFormal=(
            true
            //false
          )
        ).shCfg
      )
    )

    assumeInitial(clockDomain.isResetActive)
    anyseq(dut.io.ibus.devData)
    anyseq(dut.io.ibus.ready)
    if (dut.io.haveMultiCycleBusVec) {
      for (
        (multiCycleBus, busIdx)
        <- dut.io.multiCycleBusVec.view.zipWithIndex
      ) {
        anyseq(multiCycleBus.devData)
        anyseq(multiCycleBus.ready)
      }
    }
    anyseq(dut.io.dbus.devData)
    anyseq(dut.io.dbus.ready)
  }
  //--------
  new SpinalFormalConfig(
    _spinalConfig=SpinalConfig(
      defaultConfigForClockDomains=ClockDomainConfig(
        resetActiveLevel=HIGH,
        resetKind=SYNC,
      ),
      formalAsserts=true,
    ),
    _keepDebugInfo=true,
  )
    .withBMC(
      //20
      //15
      //16
      myProveNumCycles
    )
    //.withProve(
    //  //20
    //  //40
    //  //10
    //  myProveNumCycles
    //)
    //.withCover(
    //  //myProveNumCycles
    //  //20
    //  //60
    //  //20
    //  15
    //)
    .doVerify(SnowHouseCpuFormalDut())
  //--------
}
