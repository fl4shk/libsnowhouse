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
  val immWidth: Int = 16
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
  val AddRaRbRc = mkOp(                         // 0, 0
    "add rA, rB, rC", AddKindRc, false
  )
  val AddRaRbSimm16 = mkOp(                     // 0, 1
    "add rA, rB, simm16", AddKindSimm16, true
  )
  def AddKindRc = 0x0
  def AddKindSimm16 = 0x1

  val SubRaRbRc = mkOp(                         // 1, 0
    "sub rA, rB, rC", SubKindRc, false
  )
  //val SubRaRbSimm16 = mkOp("sub rA, rB, simm16", SubKindSimm16, true)
  val SubReserved = mkOp(                       // 1, 1
    "SubReserved", SubKindReserved, true
  )
  def SubKindRc = 0x0
  //def SubKindSimm16 = 0x1
  def SubKindReserved = 0x1

  val SltuRaRbRc = mkOp(                        // 2, 0
    "sltu rA, rB, rC", SltKindSltuRc, false
  )
  val SltsRaRbRc = mkOp(                        // 2, 1
    "slts rA, rB, rC", SltKindSltsRc, true
  )
  def SltKindSltuRc = 0x0
  def SltKindSltsRc = 0x1
  //--------
  val XorRaRbRc = mkOp(                     // 3, 0
    "xor rA, rB, rC", XorKindRc, false
  )
  val XorRaRbImm16 = mkOp(                  // 3, 1
    "xor rA, rB, imm16", XorKindSimm16, true
  )
  def XorKindRc = 0x0
  def XorKindSimm16 = 0x1

  val OrrRaRbRc = mkOp(                           // 4, 0
    "orr rA, rB, rC", OrrKindRc, false
  )
  val OrrRaRbImm16 = mkOp(                        // 4, 1
    "orr rA, rB, simm16", OrrKindSimm16, true
  )
  def OrrKindRc = 0x0
  def OrrKindSimm16 = 0x1

  // `AndRaRbRc` is moved down here to help with synthesis/routing of
  // executing/decoding instructions
  val AndRaRbRc = mkOp(                       // 5, 0
    "and rA, rB, rC", AndKindRc, true
  )
  def AndKindRc = 0x0

  val LslRaRbRc = mkOp(                         // 6, 0
    "lsl rA, rB, rC", ShiftKindLslRc, false
  )
  val LsrRaRbRc = mkOp(                         // 6, 1
    "lsr rA, rB, rC", ShiftKindLsrRc, false
  )
  val AsrRaRbRc = mkOp(                         // 6, 2
    "asr rA, rB, rC", ShiftKindAsrRc, true
  )
  def ShiftKindLslRc = 0x0
  def ShiftKindLsrRc = 0x1
  def ShiftKindAsrRc = 0x2

  //val AddRaPcSimm16 = mkOp(                   // 7, 0
  //  "AddRaPcSimm16", AddPcKindMain, true
  //)
  //def AddPcKindMain = 0x0
  //val Asr = mkOp("Asr")                     // 8
  val MulRaRbRc = mkOp(                           // 7, 0
    "mul rA, rB, rC", MulitCycleKindMulRc, true
  )
  def MulitCycleKindMulRc = 0x0
  //def MultiCycleKindUdivRc = 0x1
  //def MultiCycleKindSdivRc = 0x2
  //def MultiCycleKindUmodRc = 0x3
  //def MultiCycleKindSmodRc = 0x3

  val LdrRaRbRc = mkOp(                       // 8, 0
    "ldr rA, rB, rC", LdKind32Rc, false
  )
  val LdrRaRbSimm16 = mkOp(                   // 8, 1
    "ldr rA, rB, simm16", LdKind32Simm16, true
  )
  def LdKind32Rc = 0x0
  def LdKind32Simm16 = 0x1

  val LduhRaRbRc = mkOp(                      // 9, 0
    "lduh rA, rB, rC", LdSmallKindU16Rc, false
  )
  //val LduhRaRbSimm16 = mkOp(                  // 9, ?
  //  "lduh rA, rB, simm16", LdSmallKindU16Simm16, false
  //)
  val LdshRaRbRc = mkOp(                      // 9, 1
    "ldsh rA, rB, rC", LdSmallKindS16Rc, false
  )
  //val LdshRaRbSimm16 = mkOp(                  // 9, ?
  //  "ldsh rA, rB, simm16", LdSmallKindS16Simm16, false
  //)
  val LdubRaRbRc = mkOp(                      // 9, 2
    "ldub rA, rB, rC", LdSmallKindU8Rc, false
  )
  //val LdubRaRbSimm16 = mkOp(                  // 9, ?
  //  "ldub rA, rB, simm16", LdSmallKindU8Simm16, false
  //)
  val LdsbRaRbRc = mkOp(                      // 9, 3
    "ldsb rA, rB, rC", LdSmallKindS8Rc, true
  )
  //val LdsbRaRbSimm16 = mkOp(                  // 9, ?
  //  "ldsb rA, rB, simm16", LdSmallKindS8Simm16, true
  //)
  def LdSmallKindU16Rc = 0x0
  //def LdSmallKindU16Simm16 = ?
  def LdSmallKindS16Rc = 0x1
  //def LdSmallKindS16Simm16 = ?
  def LdSmallKindU8Rc = 0x2
  //def LdSmallKindU8Simm16 = ?
  def LdSmallKindS8Rc = 0x3
  //def LdSmallKindS8Simm16 = ?

  val StrRaRbRc = mkOp(                       // 10, 0
    "str rA, rB, rC", StKind32Rc, false
  )
  val StrRaRbSimm16 = mkOp(                   // 10, 1
    "str rA, rB, simm16", StKind32Simm16, true
  )
  def StKind32Rc = 0x0
  def StKind32Simm16 = 0x1

  val SthRaRbRc = mkOp(                         // 11, 0
    "sth rA, rB, rC", StSmallKind16Rc, false
  )
  //val SthRaRbSimm16 = mkOp(                   // 11, ?
  //  "sth rA, rB, simm16", StKind16Simm16, false
  //)
  val StbRaRbRc = mkOp(                         // 11, 1
    "stb rA, rB, rC", StSmallKind8Rc, true
  )
  //val StbRaRbSimm16 = mkOp(                   // 11, ?
  //  "stb rA, rB, simm16", StKind8Simm16, true
  //)
  def StSmallKind16Rc = 0x0
  //def StKind16Simm16 = 0x?
  def StSmallKind8Rc = 0x1
  //def StKind8Simm16 = 0x?
  //--------
  val BeqRaRbSimm = mkOp(                     // 12, 0
    "beq rA, rB, simm16", JmpKindBeq, false
  )
  val BlSimm = mkOp(                     // 12, 0
    "bl rA, simm16", JmpKindBeq, false
  )
  val BneRaRbSimm = mkOp(                     // 12, 1
    "bne rA, rB, simm16", JmpKindBne, false
  )
  val AddRaPcSimm16 = mkOp(                   // 12, 1
    "add rA, pc, simm16", JmpKindBne, false
  )
  val JlRaRb = mkOp(                          // 12, 2
    "jl rA, rB", JmpKindJlRaRb, false
  )
  val JmpReserved = mkOp(                     // 12, 3
    "<JmpReserved>", JmpKindReserved, true
  )
  def JmpKindBeq = 0x0
  def JmpKindBne = 0x1
  def JmpKindJlRaRb = 0x2
  //def AddPcKindMain = 0x3
  def JmpKindReserved = 0x3

  //val CpyRaRb = mkOp("cpy rA, rB", CpyKindCpyRb, false)              // 12, 0
  //val CpyRaSimm16 = mkOp("cpy rA, simm16", CpyKindCpySimm16, false)  // 12, 1
  //val CpyuRaRb = mkOp("cpyu rA, rB", CpyKindCpyuRb, false)           // 12, 2
  //val CpyuRaSimm16 = mkOp(                                        // 12, 3
  //  "cpyu rA, simm16", CpyKindCpyuSimm16, true
  //)
  ////val OldCpy = mkOp("OldCpy")           // 15
  ////def CpyKindCpyRb = 0x0
  ////def CpyKindCpySimm16 = 0x1
  //def CpyKindCpyuRb = 0x0//0x2
  //def CpyKindCpyuSimm16 = 0x1//0x3
  val PreImm16 = mkOp(                      // 13, 0
    "pre simm16", PreKindImm16, true
  )
  def PreKindImm16 = 0x0
  //--------
  val OpLim = _opCnt
  val expectedNumOpcodes = 14
  assert(
    OpLim == expectedNumOpcodes,
    s"eek! "
    + s"${OpLim} != ${expectedNumOpcodes}"
  )
}
case class SnowHouseCpuEncInstr(
) extends PackedBundle {
  val imm16 = UInt(SnowHouseCpuInstrEnc.immWidth bits)
  val rcIdx = UInt(SnowHouseCpuInstrEnc.gprIdxWidth bits)
  val rbIdx = UInt(SnowHouseCpuInstrEnc.gprIdxWidth bits)
  val raIdx = UInt(SnowHouseCpuInstrEnc.gprIdxWidth bits)
  val op = UInt(SnowHouseCpuInstrEnc.opWidth bits)
}
object SnowHouseCpuPipeStageInstrDecode {
  def apply(
    psId: SnowHousePipeStageInstrDecode
  ) = new Area {
    //--------
    // NOTE: the `KeepAttribute(...)`s seem to be required for signals
    // defined in this function.
    //--------
    import SnowHouseCpuOp._
    def upPayload = psId.upPayload
    def io = psId.io
    def cfg = psId.cfg
    def cId = psId.cId
    val encInstr = (
      KeepAttribute(
        SnowHouseCpuEncInstr()
      )
      .setName("InstrDecode_encInstr")
    )
    encInstr.assignFromBits(
      //io.ibus.devData.instr.asBits
      Mux[Bits](
        !psId.shouldBubble,
        psId.tempInstr.asBits,
        psId.tempInstr.asBits.getZero
      )
    )
    val nextMultiCycleState = Bool()
    val rMultiCycleState = (
      KeepAttribute(
        RegNext(
          next=nextMultiCycleState,
          init=nextMultiCycleState.getZero,
        )
      )
      .setName(s"InstrDecode_rMultiCycleState")
    )
    nextMultiCycleState := rMultiCycleState
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
    val rPrevPreImm16 = (
      KeepAttribute(
        RegNextWhen(
          next=encInstr.imm16,
          cond=cId.up.isFiring,
          init=encInstr.imm16.getZero,
        )
      )
      .setName(s"InstrDecode_rPrevPreImm16")
    )
    switch (rMultiCycleState) {
      is (False) {
        upPayload.imm := tempImm
      }
      is (True) {
        upPayload.imm := (
          Cat(
            rPrevPreImm16,
            encInstr.imm16,
          ).asUInt
        )
        when (cId.up.isFiring) {
          nextMultiCycleState := False
        }
      }
    }
    def setOp(
      someOp: (Int, Int, String)
    ): Unit = {
      for (
        ((tuple, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex
      ) {
        if (someOp == tuple) {
          println(
            s"${opInfoIdx}: ${someOp._3}"
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
    def doDefault(
      doSetImm: Boolean=true
    ): Unit = {
      // this function could be useful for decoding variable width
      // instructions!
      // just do a NOP
      setOp(AddRaRbRc)
      upPayload.gprIdxVec.foreach{gprIdx => {
        gprIdx := 0x0
      }}
      if (doSetImm) {
        upPayload.imm := 0x0
      }
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
          setOp(XorRaRbImm16)
        }
      }
      is (OrrRaRbRc._1) {
        when (encInstr.rcIdx =/= 0x0) {
          setOp(OrrRaRbRc)
        } otherwise {
          setOp(OrrRaRbImm16)
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
      //is (AddRaPcSimm16._1) {
      //  setOp(AddRaPcSimm16)
      //}
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
      is (LduhRaRbRc._1) {
        switch (encInstr.imm16(1 downto 0)) {
          is (LduhRaRbRc._2) {
            setOp(LduhRaRbRc)
          }
          is (LdshRaRbRc._2) {
            setOp(LdshRaRbRc)
          }
          is (LdubRaRbRc._2) {
            setOp(LdubRaRbRc)
          }
          is (LdsbRaRbRc._2) {
            setOp(LdsbRaRbRc)
          }
        }
      }
      is (StrRaRbRc._1) {
        when (encInstr.rcIdx =/= 0x0) {
          setOp(StrRaRbRc)
        } otherwise {
          setOp(StrRaRbSimm16)
        }
      }
      is (SthRaRbRc._1) {
        switch (encInstr.imm16(0 downto 0)) {
          is (SthRaRbRc._2) {
            setOp(SthRaRbRc)
          }
          is (StbRaRbRc._2) {
            setOp(StbRaRbRc)
          }
        }
      }
      is (BeqRaRbSimm._1) {
        switch (encInstr.rcIdx(1 downto 0)) {
          is (BeqRaRbSimm._2) {
            when (
              encInstr.raIdx === encInstr.rbIdx
              && encInstr.raIdx =/= 0x0
            ) {
              setOp(BlSimm)
            } otherwise {
              setOp(BeqRaRbSimm)
            }
          }
          is (BneRaRbSimm._2) {
            when (
              encInstr.raIdx === encInstr.rbIdx
              && encInstr.raIdx =/= 0x0
            ) {
              setOp(AddRaPcSimm16)
            } otherwise {
              setOp(BneRaRbSimm)
            }
          }
          is (JlRaRb._2) {
            setOp(JlRaRb)
          }
          default {
            doDefault()
          }
        }
      }
      //is (CpyuRaRb._1) {
      //  switch (encInstr.rcIdx(0 downto 0)) {
      //    //is (CpyRaRb._2) {
      //    //  setOp(CpyRaRb)
      //    //}
      //    //is (CpyRaSimm16._2) {
      //    //  setOp(CpyRaSimm16)
      //    //}
      //    is (CpyuRaRb._2) {
      //      setOp(CpyuRaRb)
      //    }
      //    is (CpyuRaSimm16._2) {
      //      setOp(CpyuRaSimm16)
      //    }
      //  }
      //}
      is (PreImm16._1) {
        doDefault(
          //doSetImm=false
        )
        when (
          cId.up.isFiring
          && !rMultiCycleState
        ) {
          nextMultiCycleState := True
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
    SnowHouseCpuOp.XorRaRbImm16 -> OpInfo.mkAlu(
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
    SnowHouseCpuOp.OrrRaRbImm16 -> OpInfo.mkAlu(
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
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(false)),
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.LdrRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(false)),
    )
  )
  //--------
  opInfoMap += (
    SnowHouseCpuOp.LduhRaRbRc -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      modify=MemAccessKind.Mem16(isSigned=false, isStore=Some(false)),
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.LdshRaRbRc -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      modify=MemAccessKind.Mem16(isSigned=true, isStore=Some(false)),
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.LdubRaRbRc -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      modify=MemAccessKind.Mem8(isSigned=false, isStore=Some(false)),
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.LdsbRaRbRc -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      modify=MemAccessKind.Mem8(isSigned=true, isStore=Some(false)),
    )
  )
  //--------
  opInfoMap += (
    SnowHouseCpuOp.StrRaRbRc -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(true)),
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.StrRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(true)),
    )
  )
  //--------
  opInfoMap += (
    SnowHouseCpuOp.SthRaRbRc -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      modify=MemAccessKind.Mem16(isSigned=false, isStore=Some(true)),
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.StbRaRbRc -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      modify=MemAccessKind.Mem8(isSigned=false, isStore=Some(true)),
    )
  )
  //--------
  opInfoMap += (
    // beq rA, rB, simm
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
    SnowHouseCpuOp.BlSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc, DstKind.Gpr),
      srcArr=Array[SrcKind](
        SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
    )
  )
  opInfoMap += (
    // bne rA, rB, simm16
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
    // add rA, pc, simm16
    SnowHouseCpuOp.AddRaPcSimm16 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Pc, SrcKind.Imm(/*Some(true)*/)),
      aluOp=AluOpKind.Add,
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.JlRaRb -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc, DstKind.Gpr),
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
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyuRaRb -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Gpr),
  //    cpyOp=CpyOpKind.Cpyu,
  //  )
  //)
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyuRaSimm16 -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Imm(/*Some(true)*/)),
  //    cpyOp=CpyOpKind.Cpyu,
  //  )
  //)
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
    cpy(r0, 0x0),             // 0: r0 = 0
                              // instruction fetcher has trouble
                              // reading first instruction, so put
                              // in a dummy
    cpy(r0, 0x0),             // 0x4: r0 = 0
    cpy(r0, 0x0),             // 0x8
    cpy(r1, 0x8),             // 0xc: r1 = 8
    cpy(r2, 0x1),             // 0x10: r2 = 1
    //pre(0xabcd),                // 0x10
    cpy(r3, 0x1000),          // 0x14: r3 = 0x1000
    cpy(r4, 0x8),             // 0x18: r4 = 4
    ////cpy(r6, 0x0),       //
    cpy(r5, LbR"increment"),  // 0x1c
    cpy(r6, 0x20),            // 0x20: r6 = 0x20
    str(r6, r0, r3),          // 0x24: [r0 + r3] = r6
    ldr(r6, r0, r3),          // 0x28
    //cpy(r0, r0),
    mul(r7, r6, r1),          // 0x2c
    //--------
    Lb"loop",
    //add(r0, r1, r2),
    //cpyu(r2, tempData >> 16),
    //cpy(r2, tempData & 0xffff),
    ldr(r6, r3, 0x0),         // 0x30:
    //add(r6, r6, 0x1),       
    //jl(r5),
    bl(LbR"increment"),       // 0x34:
    str(r6, r3, 0x4),         // 0x38:
    add(r3, r3, 0x4),         // 0x3c: r3 += 4
    sub(r1, r1, 0x1),         // 0x40: r1 -= 1 
    bl(LbR"multiply"),        // 0x44
    bnz(r1, LbR"loop"),       // 0x48: if (r1 != 0) goto LbR"loop"
    //--------
    cpy(r12, 0x0),            // 0x4c
    Lb"infin",
    bz(r12, LbR"infin"),      // 0x50
    //Db32(0x3f),
    //--------
    Lb"increment",
    add(r6, r6, 0x1),         // 0x54
    //add(r6, r6, r0),
    jmp(lr),                  // 0x58
    //--------
    Lb"multiply",
    mul(r7, r6, r1),          // 0x5c
    jmp(lr),                  // 0x60
    cpy(r0, r0),
    cpy(r0, r0),
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
  if (cpu.io.haveMultiCycleBusVec) {
    for (
      (multiCycleBus, busIdx) <- cpu.io.multiCycleBusVec.view.zipWithIndex
    ) {
      //cpu.io.multiCycleBusVec
      multiCycleBus.ready := multiCycleBus.rValid
    }
  }
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
    for (i <- 0 until 1024) {
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
