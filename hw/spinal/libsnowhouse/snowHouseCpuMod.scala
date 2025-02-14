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

  //val regIdxPopData = numGprs + 1 - 1
}
object SnowHouseCpuOp {
  //private var _rawOpCnt: Int = 0
  private var _opCnt: Int = 0
  def mkOp(
    //opAsInt: Int,
    //lim: Int,
    name: String,
    kind: (Int, Int),
    update: Boolean,
    //kindLim: Int,
  ): (/*UInt,*/ Int, (Int, Int), String) = {
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
  def AddKindRc = (0x0, 0x0)
  def AddKindSimm16 = (0x1, 0x0)

  val SubRaRbRc = mkOp(                         // 1, 0
    "sub rA, rB, rC", SubKindRc, false
  )
  //val SubRaRbSimm16 = mkOp("sub rA, rB, simm16", SubKindSimm16, true)
  val SubReserved = mkOp(                       // 1, 1
    "SubReserved", SubKindReserved, true
  )
  def SubKindRc = (0x0, 0x0)
  //def SubKindSimm16 = 0x1
  def SubKindReserved = (0x1, 0x0)

  val SltuRaRbRc = mkOp(                        // 2, 0
    "sltu rA, rB, rC", SltKindSltuRc, false
  )
  val SltsRaRbRc = mkOp(                        // 2, 1
    "slts rA, rB, rC", SltKindSltsRc, true
  )
  def SltKindSltuRc = (0x0, 0x0)
  def SltKindSltsRc = (0x1, 0x0)
  //--------
  val XorRaRbRc = mkOp(                     // 3, 0
    "xor rA, rB, rC", XorKindRc, false
  )
  val XorRaRbImm16 = mkOp(                  // 3, 1
    "xor rA, rB, imm16", XorKindSimm16, true
  )
  def XorKindRc = (0x0, 0x0)
  def XorKindSimm16 = (0x1, 0x0)

  val OrRaRbRc = mkOp(                           // 4, 0
    "or rA, rB, rC", OrKindRc, false
  )
  val OrRaRbImm16 = mkOp(                        // 4, 1
    "or rA, rB, simm16", OrKindSimm16, true
  )
  def OrKindRc = (0x0, 0x0)
  def OrKindSimm16 = (0x1, 0x0)

  // `AndRaRbSimm16` is moved down here to help with synthesis/routing of
  // executing/decoding instructions
  val AndRaRbSimm16 = mkOp(                       // 5, 0
    "and rA, rB, simm16", AndKindSimm16, true
  )
  //def AndKindRc = (0x0, 0x0)
  def AndKindSimm16 = (0x0, 0x0)

  val LslRaRbRc = mkOp(                         // 6, 0
    "lsl rA, rB, rC", ShiftEtcSprKindLslRc, false
  )
  val LslReserved = mkOp(                         // 6, 1
    "lsl <Reserved>", ShiftEtcSprKindLslReserved, false
  )
  val LsrRaRbRc = mkOp(                         // 6, 2
    "lsr rA, rB, rC", ShiftEtcSprKindLsrRc, false
  )
  val LsrReserved = mkOp(                         // 6, 3
    "lsr <Reserved>", ShiftEtcSprKindLsrReserved, false
  )
  val AsrRaRbRc = mkOp(                         // 6, 4
    "asr rA, rB, rC", ShiftEtcSprKindAsrRc, false
  )
  val AsrReserved = mkOp(                         // 6, 5
    "asr <Reserved>", ShiftEtcSprKindAsrReserved, false
  )
  val AndRaRbRc = mkOp(                         // 6, 6
    "and rA, rB, rC", ShiftEtcSprKindAndRc, false
  )
  //val AndReserved = mkOp(                         // 6, ?
  //  "and <Reserved>", ShiftEtcSprKindAndReserved, false
  //)
  //val CpyRaIds = mkOp(                          // 6, ?
  //  "cpy rA, ids", ShiftEtcSprKindCpyRaIds, false
  //)
  val CpyIdsRb = mkOp(                          // 6, 7
    "cpy ids, rB", ShiftEtcSprKindCpyIdsRb, false
  )
  val CpyRaIra = mkOp(                          // 6, 8
    "cpy rA, ira", ShiftEtcSprKindCpyRaIra, false
  )
  //val CpyIraRb = mkOp(                          // 6, ?
  //  "cpy ira, rB", ShiftEtcSprKindCpyIraRb, false
  //)
  //val CpyRaIe = mkOp(                           // 6, ?
  //  "cpy rA, ie", ShiftEtcSprKindCpyRaIe, false
  //)
  val CpyIeRb = mkOp(                           // 6, 9
    "cpy ie, rB", ShiftEtcSprKindCpyIeRb, false
  )
  val RetIra = mkOp(                            // 6, 10
    "ret ira", ShiftEtcSprKindRetIra, true
  )
  //val ShiftEtcSprOpReserved = mkOp(                // 6, 10
  //  "ShiftEtcSprOpReserved", ShiftEtcSprKindReserved, true
  //)

  def ShiftEtcSprKindLslRc = (0x0, 0x0)
  def ShiftEtcSprKindLslReserved = (0x1, 0x0)
  def ShiftEtcSprKindLsrRc = (0x2, 0x0)
  def ShiftEtcSprKindLsrReserved = (0x3, 0x0)
  def ShiftEtcSprKindAsrRc = (0x4, 0x0)
  def ShiftEtcSprKindAsrReserved = (0x5, 0x0)
  def ShiftEtcSprKindAndRc = (0x6, 0x0)
  def ShiftEtcSprKindAndReserved = (0x7, 0x0)
  //def ShiftEtcSprKindCpyRaIds = (0x7, 0x0)
  def ShiftEtcSprKindCpyIdsRb = (0x8, 0x0)
  def ShiftEtcSprKindCpyRaIra = (0x9, 0x0)
  //def ShiftEtcSprKindCpyIraRb = (0xa, 0x0)
  //def ShiftEtcSprKindCpyRaIe = (0xb, 0x0)
  def ShiftEtcSprKindCpyIeRb = (0xa, 0x0)
  def ShiftEtcSprKindRetIra = (0xb, 0x0)
  //def ShiftEtcSprKindReserved = (0xc, 0x0)

  //val AddRaPcSimm16 = mkOp(                   // 7, 0
  //  "AddRaPcSimm16", AddPcKindMain, true
  //)
  //def AddPcKindMain = 0x0
  //val Asr = mkOp("Asr")                     // 8
  val MulRaRbRc = mkOp(                           // 7, 0
    "mul rA, rB, rC", MultiCycleKindMulRc, false
  )
  val UdivRaRbRc = mkOp(                           // 7, 1
    "udiv rA, rB, rC", MultiCycleKindUdivRc, false
  )
  val SdivRaRbRc = mkOp(                           // 7, 2
    "sdiv rA, rB, rC", MultiCycleKindSdivRc, false
  )
  val UmodRaRbRc = mkOp(                           // 7, 3
    "umod rA, rB, rC", MultiCycleKindUmodRc, false
  )
  val SmodRaRbRc = mkOp(                           // 7, 4
    "smod rA, rB, rC", MultiCycleKindSmodRc, true
  )
  def MultiCycleKindMulRc = (0x0, 0x0)
  def MultiCycleKindUdivRc = (0x1, 0x0)
  def MultiCycleKindSdivRc = (0x2, 0x0)
  def MultiCycleKindUmodRc = (0x3, 0x0)
  def MultiCycleKindSmodRc = (0x4, 0x0)

  //val LdrRaRbRc = mkOp(                       // 8, ?
  //  "ldr rA, rB, rC", LdKind32Rc, false
  //)
  val LdrRaRbSimm16 = mkOp(                   // 8, 0
    "ldr rA, rB, simm16", LdKind32Simm16, false
  )
  //def LdKind32Rc = (0x0, 0x0)
  def LdKind32Simm16 = (0x0, 0x0)
  //def LdKind32Simm16 = (0x1, 0x0)

  //val StrRaRbRc = mkOp(                       // 8, ?
  //  "str rA, rB, rC", StKind32Rc, false
  //)
  val StrRaRbSimm16 = mkOp(                   // 8, 1
    "str rA, rB, simm16", StKind32Simm16, false
  )
  //def StKind32Rc = (0x0, 0x0)
  def StKind32Simm16 = (0x1, 0x0)
  //def StKind32Simm16 = (0x1, 0x0)

  //val LduhRaRbRc = mkOp(                      // 8, ?
  //  "lduh rA, rB, rC", LdSmallKindU16Rc, false
  //)
  val LduhRaRbSimm16 = mkOp(                  // 8, 2
    "lduh rA, rB, simm16", LdSmallKindU16Simm16, false
  )
  //val LdshRaRbRc = mkOp(                      // 8, ?
  //  "ldsh rA, rB, rC", LdSmallKindS16Rc, false
  //)
  val LdshRaRbSimm16 = mkOp(                  // 8, 3
    "ldsh rA, rB, simm16", LdSmallKindS16Simm16, false
  )
  //val LdubRaRbRc = mkOp(                      // 8, ?
  //  "ldub rA, rB, rC", LdSmallKindU8Rc, false
  //)
  val LdubRaRbSimm16 = mkOp(                  // 8, 4
    "ldub rA, rB, simm16", LdSmallKindU8Simm16, false
  )
  //val LdsbRaRbRc = mkOp(                      // 8, ?
  //  "ldsb rA, rB, rC", LdSmallKindS8Rc, false
  //)
  val LdsbRaRbSimm16 = mkOp(                  // 8, 5
    "ldsb rA, rB, simm16", LdSmallKindS8Simm16, false
  )
  //def LdSmallKindU16Rc = (0x0, 0x0)
  def LdSmallKindU16Simm16 = (0x2, 0x0)
  //def LdSmallKindS16Rc = (0x1, 0x0)
  def LdSmallKindS16Simm16 = (0x3, 0x0)
  //def LdSmallKindU8Rc = (0x2, 0x0)
  def LdSmallKindU8Simm16 = (0x4, 0x0)
  //def LdSmallKindS8Rc = (0x3, 0x0)
  def LdSmallKindS8Simm16 = (0x5, 0x0)


  //val SthRaRbRc = mkOp(                         // 8, 0
  //  "sth rA, rB, rC", StSmallKind16Rc, false
  //)
  val SthRaRbSimm16 = mkOp(                   // 8, 6
    "sth rA, rB, simm16", StKind16Simm16, false
  )
  //val StbRaRbRc = mkOp(                         // 8, 1
  //  "stb rA, rB, rC", StSmallKind8Rc, true
  //)
  val StbRaRbSimm16 = mkOp(                   // 8, 7
    "stb rA, rB, simm16", StKind8Simm16, true
  )
  //def StSmallKind16Rc = (0x0, 0x0)
  def StKind16Simm16 = (0x6, 0x0)
  //def StSmallKind8Rc = (0x1, 0x0)
  def StKind8Simm16 = (0x7, 0x0)
  //--------
  val BeqRaRbSimm = mkOp(                     // 9, 0
    "beq rA, rB, simm16", JmpKindBeq, false
  )
  //val BzRaSimm = mkOp(                     // 9, 0
  //  "bz rA, simm16", JmpKindBz, false
  //)
  val BlSimm = mkOp(                     // 9, 0
    "bl simm16", JmpKindBeq, false
  )
  val BneRaRbSimm = mkOp(                     // 9, 1
    "bne rA, rB, simm16", JmpKindBne, false
  )
  //val BnzRaSimm = mkOp(                     // 9, 1
  //  "bnz rA, simm16", JmpKindBnz, false
  //)
  val AddRaPcSimm16 = mkOp(                   // 9, 1
    "add rA, pc, simm16", JmpKindBne, false
  )

  val BltuRaRbSimm = mkOp(
    "bltu rA, rB, simm16", JmpKindBltu, false
  )
  val BgeuRaRbSimm = mkOp(
    "bgeu rA, rB, simm16", JmpKindBgeu, false
  )
  val BltsRaRbSimm = mkOp(
    "blts rA, rB, simm16", JmpKindBlts, false
  )
  val BgesRaRbSimm = mkOp(
    "bges rA, rB, simm16", JmpKindBges, false
  )

  val JlRaRb = mkOp(                          // 9, 2
    "jl rA, rB", JmpKindJlRaRb, true
  )
  //val JmpReserved = mkOp(                     // 9, 3
  //  "<JmpReserved>", JmpKindReserved, true
  //)
  //def JmpKindBz = (0x0, 0x0)
  def JmpKindBeq = (0x0, 0x0)
  def JmpKindBne = (0x1, 0x0)
  def JmpKindBltu = (0x2, 0x0)
  def JmpKindBgeu = (0x3, 0x0)
  def JmpKindBlts = (0x4, 0x0)
  def JmpKindBges = (0x5, 0x0)
  def JmpKindJlRaRb = (0x6, 0x0)
  //def AddPcKindMain = 0x3
  //def JmpKindReserved = (0x3, 0x0)

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
  //--------
  //val PushRaRb = mkOp(                            // 14, 0
  //  "push rA, rB", StackKindPushRaRb, false
  //)
  //val PopRaRb = (                                 // 14, 1
  //  mkOp(
  //    "pop rA, rB", StackKindPopRaRb, false //
  //  ),
  //  //mkOp(
  //  //  "internal: pop0", StackKindPopRaRbInternal0, false
  //  //),
  //  //mkOp(
  //  //  "internal: pop1", StackKindPopRaRbInternal1, false
  //  //)
  //)
  //val PopPcRb = (
  //  mkOp(
  //    "pop pc, rB", StackKindPopPcRb, false
  //  ),
  //  mkOp(
  //    "internal 0: pop pc, rB", StackKindPopPcRbInternal0, true
  //  ),
  //  //mkOp(
  //  //  "internal 1: pop pc, rB", StackKindPopPcRbInternal1, true
  //  //),
  //)
  //val LdrPdRbSimm16 = mkOp(
  //  "ldr pop_data, rB, simm16", StackKindLdrPdRbSimm16, false
  //)
  //val JmpPd = mkOp(
  //  "jmp pop_data", StackKindJmpPd, true
  //)
  //def StackKindLdrPdRbSimm16 = (0x0, 0x0)
  //def StackKindJmpPd = (0x1, 0x0)
  ////def StackKindPushRaRb = (0x0, 0x0)
  ////def StackKindPopRaRb = (0x1, 0x0)
  //////def StackKindPopRaRbInternal0 = (StackKindPopRaRb._1, 0x1)
  //////def StackKindPopRaRbInternal1 = (StackKindPopRaRb._1, 0x2)
  ////def StackKindPopPcRb = (0x2, 0x0)
  ////def StackKindPopPcRbInternal0 = (StackKindPopPcRb._1, 0x1)
  //////def StackKindPopPcRbInternal1 = (StackKindPopPcRb._1, 0x2)
  //--------
  val PreImm16 = mkOp(                      // 10, 0
    "pre simm16", PreKindImm16, true
  )
  def PreKindImm16 = (0x0, 0x0)
  //--------
  val OpLim = _opCnt
  val expectedNumOpcodes = 11
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
    // NOTE: the `/*KeepAttribute*/(...)`s seem to be required for signals
    // defined in this function.
    //--------
    import SnowHouseCpuOp._
    def upPayload = psId.upPayload
    def io = psId.io
    def cfg = psId.cfg
    def cId = psId.cId
    val encInstr = (
      /*KeepAttribute*/(
        SnowHouseCpuEncInstr()
      )
      .setName("InstrDecode_encInstr")
    )
    encInstr.assignFromBits(
      //io.ibus.recvData.instr.asBits
      //Mux[Bits](
      //  !psId.shouldBubble,
        psId.tempInstr.asBits,
      //  psId.tempInstr.asBits.getZero
      //)
    )
    val tempHaveHazardAddrCheckVec = Vec.fill(upPayload.gprIdxVec.size)(
      Bool()
    )
    for (idx <- 0 until upPayload.gprIdxVec.size) {
      val tempRegIdx: UInt = (
        if (idx == 0) {
          encInstr.raIdx
        } else if (idx == 1) {
          encInstr.rbIdx
        } else if (idx == 2) {
          encInstr.rcIdx
        } else {
          assert(
            //idx == 2
            false,
            s"${idx} ${upPayload.gprIdxVec.size}"
          )
          encInstr.raIdx
        }
      )
      tempHaveHazardAddrCheckVec(idx) := (
        tempRegIdx
        === (
          RegNextWhen(
            next=encInstr.raIdx,
            cond=psId.up.isFiring,
            init=encInstr.raIdx.getZero
          )
        )
      )
    }
    upPayload.myDoHaveHazardAddrCheckVec(0) := (
      tempHaveHazardAddrCheckVec.reduceLeft(_ || _)
    )
    //val rTempState = (
    //  /*KeepAttribute*/(
    //    Reg(Bool(), init=False)
    //  )
    //  .setName(s"InstrDecode_PopRaRb_rTempState")
    //)
    val nextMultiCycleState = Bool()
    val rMultiCycleState = (
      /*KeepAttribute*/(
        RegNext(
          next=nextMultiCycleState,
          init=nextMultiCycleState.getZero,
        )
      )
      .setName(s"InstrDecode_rMultiCycleState")
    )
    nextMultiCycleState := rMultiCycleState
    //psId.nextMultiCycleStateIsIdle := nextMultiCycleState
    for (idx <- 0 until cfg.maxNumGprsPerInstr) {
      upPayload.gprIsZeroVec(idx) := (
        upPayload.gprIdxVec(idx) === 0x0
      )
    }
    upPayload.gprIdxVec(0) := encInstr.raIdx.resized
    upPayload.gprIdxVec(1) := encInstr.rbIdx.resized
    upPayload.gprIdxVec(2) := encInstr.rcIdx.resized
    //val tempImm = Cat(
    //  Mux[UInt](
    //    encInstr.imm16.msb,
    //    U"16'hffff",
    //    U"16'h0000",
    //  ),
    //  encInstr.imm16.asSInt
    //).asUInt
    val tempImm = UInt(cfg.mainWidth bits)
    tempImm := (
      Cat(
        Mux[UInt](
          encInstr.imm16.msb,
          U"16'hffff",
          U"16'h0000",
        ),
        encInstr.imm16.asSInt
      ).asUInt
    )
    val rPrevPreImm = (
      /*KeepAttribute*/(
        RegNextWhen(
          next=Cat(
            encInstr.raIdx,
            encInstr.rbIdx,
            encInstr.rcIdx,
            encInstr.imm16
          ).asUInt,
          cond=cId.up.isFiring,
          //init=encInstr.imm16.getZero,
        )
      )
      .setName(s"InstrDecode_rPrevPreImm16")
    )
    def setOp(
      someOp: (Int, (Int, Int), String),
      //someOutpOp: UInt=upPayload.op,
    ): Area = new Area {
      setName(s"setOp_${someOp._1}")
      var found = false
      for (
        ((tuple, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex
      ) {
        if (someOp == tuple) {
          val mySplitOp = upPayload.splitOp
          //for ((src, srcIdx) <- opInfo.srcArr.view.zipWithIndex) {
          //  mySplitOp.srcKindVec
          //}
          for (
            ((_, cpyOpInfo), cpyOpInfoIdx)
            <- cfg.cpyCpyuiOpInfoMap.view.zipWithIndex
          ) {
            //if (cpyOpInfoIdx == 0) {
            //  println(
            //    //s"pureCpyOp (${cpyOpInfoIdx}): "
            //    //+ s"${opInfoIdx}: ${someOp._3}"
            //    s"test: pureCpyOp: ${cpyOpInfoIdx} "
            //    + s"${someOp._3} // ${opInfoIdx}"
            //  )
            //}
            if (opInfo == cpyOpInfo) {
              println(
                //s"pureCpyOp (${cpyOpInfoIdx}): "
                //+ s"${opInfoIdx}: ${someOp._3}"
                s"cpyCpyuiOp: ${cpyOpInfoIdx} "
                + s"${someOp._3} // ${opInfoIdx}"
              )
              //upPayload.op := opInfoIdx
              //mySplitOp.pureCpyOp.valid := True
              mySplitOp.kind := SnowHouseSplitOpKind.CPY_CPYUI
              mySplitOp.cpyCpyuiOp := cpyOpInfoIdx
              //return
              found = true
            }
          }
          //for (
          //  ((_, cpyuiOpInfo), cpyuiOpInfoIdx)
          //  <- cfg.pureCpyuiOpInfoMap.view.zipWithIndex
          //) {
          //  if (opInfo == cpyuiOpInfo) {
          //    println(
          //      s"pureCpyuiOp: ${cpyuiOpInfoIdx} "
          //      + s"${someOp._3} // ${opInfoIdx}"
          //    )
          //    //upPayload.op := opInfoIdx
          //    //mySplitOp.pureCpyuiOp.valid := True
          //    mySplitOp.kind := SnowHouseSplitOpKind.PURE_CPYUI
          //    mySplitOp.pureCpyuiOp := cpyuiOpInfoIdx
          //    return
          //  }
          //}
          for (
            ((_, jmpOpInfo), jmpOpInfoIdx)
            <- cfg.jmpBrOpInfoMap.view.zipWithIndex
          ) {
            if (opInfo == jmpOpInfo) {
              println(
                s"jmpBrOp: ${jmpOpInfoIdx} "
                + s"${someOp._3} // ${opInfoIdx}"
              )
              //upPayload.op := opInfoIdx
              //mySplitOp.pureJmpOp.valid := True
              mySplitOp.kind := SnowHouseSplitOpKind.JMP_BR
              mySplitOp.jmpBrOp := jmpOpInfoIdx
              //return
              found = true
            }
          }
          //for (
          //  ((_, brOpInfo), brOpInfoIdx)
          //  <- cfg.pureJmpOpInfoMap.view.zipWithIndex
          //) {
          //  if (opInfo == brOpInfo) {
          //    println(
          //      s"pureBrOp: ${brOpInfoIdx} "
          //      + s"${someOp._3} // ${opInfoIdx}"
          //    )
          //    //upPayload.op := opInfoIdx
          //    //mySplitOp.pureBrOp.valid := True
          //    mySplitOp.kind := SnowHouseSplitOpKind.PURE_BR
          //    mySplitOp.pureBrOp := brOpInfoIdx
          //    return
          //  }
          //}
          for (
            ((_, aluOpInfo), aluOpInfoIdx)
            <- cfg.aluOpInfoMap.view.zipWithIndex
          ) {
            if (opInfo == aluOpInfo) {
              println(
                s"aluOp: ${aluOpInfoIdx} "
                + s"${someOp._3} // ${opInfoIdx}"
              )
              //upPayload.op := opInfoIdx
              //mySplitOp.aluOp.valid := True
              mySplitOp.kind := SnowHouseSplitOpKind.ALU
              mySplitOp.aluOp := aluOpInfoIdx
              //return
              found = true
            }
          }
          for (
            ((_, multiCycleOpInfo), multiCycleOpInfoIdx)
            <- cfg.multiCycleOpInfoMap.view.zipWithIndex
          ) {
            if (opInfo == multiCycleOpInfo) {
              println(
                s"multiCycleOp: ${multiCycleOpInfoIdx} "
                + s"${someOp._3} // ${opInfoIdx}"
              )
              //upPayload.op := opInfoIdx
              //mySplitOp.multiCycleOp.valid := True
              mySplitOp.kind := SnowHouseSplitOpKind.MULTI_CYCLE
              mySplitOp.multiCycleOp := multiCycleOpInfoIdx
              //return
              found = true
            }
          }
        }
      }
      if (!found) {
        assert(
          false,
          s"eek! ${someOp}"
        )
      }
    }
    def doDefault(
      doSetImm: Boolean=true
    ): Unit = {
      // just do a NOP
      setOp(AddRaRbRc)
      //upPayload.gprIdxVec.foreach{gprIdx => {
      //  gprIdx := 0x0
      //}}
      if (doSetImm) {
        upPayload.imm.foreach(imm => {
          imm := 0x0
        })
      }
    }
    //psId.nextPrevInstrWasJump := False
    //when (cId.up.isFiring) {
    //  rTempState := False
    //}
    upPayload.splitOp := upPayload.splitOp.getZero
    upPayload.splitOp.kind := SnowHouseSplitOpKind.CPY_CPYUI
    switch (rMultiCycleState) {
      is (False) {
        upPayload.imm.foreach(imm => {
          imm := tempImm
        })
        //upPayload.blockIrq := False
      }
      is (True) {
        upPayload.imm.foreach(imm => {
          imm := (
            Cat(
              rPrevPreImm,
              encInstr.imm16,
            ).asUInt.resized
          )
        })
        when (cId.up.isFiring) {
          if (cfg.irqCfg != None) {
            upPayload.blockIrq := False
          }
          nextMultiCycleState := False
        }
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
          is (SltuRaRbRc._2._1) {
            setOp(SltuRaRbRc)
          }
          is (SltsRaRbRc._2._1) {
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
      is (OrRaRbRc._1) {
        when (encInstr.rcIdx =/= 0x0) {
          setOp(OrRaRbRc)
        } otherwise {
          setOp(OrRaRbImm16)
        }
      }
      is (AndRaRbSimm16._1) {
        setOp(AndRaRbSimm16)
      }
      is (LslRaRbRc._1) {
        switch (encInstr.imm16(3 downto 0)) {
          is (LslRaRbRc._2._1) {
            setOp(LslRaRbRc)
          }
          is (LsrRaRbRc._2._1) {
            setOp(LsrRaRbRc)
          }
          is (AsrRaRbRc._2._1) {
            setOp(AsrRaRbRc)
          }
          is (AndRaRbRc._2._1) {
            setOp(AndRaRbRc)
          }
          //is (CpyRaIds._2._1) {
          //  setOp(CpyRaIds)
          //}
          is (CpyIdsRb._2._1) {
            setOp(CpyIdsRb)
          }
          is (CpyRaIra._2._1) {
            setOp(CpyRaIra)
          }
          //is (CpyIraRb._2._1) {
          //  setOp(CpyIraRb)
          //}
          //is (CpyRaIe._2._1) {
          //  setOp(CpyRaIe)
          //}
          is (CpyIeRb._2._1) {
            setOp(CpyIeRb)
          }
          is (RetIra._2._1) {
            //psId.nextPrevInstrWasJump := True
            setOp(RetIra)
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
        switch (encInstr.imm16(2 downto 0)) {
          is (MulRaRbRc._2._1) {
            setOp(MulRaRbRc)
          }
          is (UdivRaRbRc._2._1) {
            setOp(UdivRaRbRc)
          }
          is (SdivRaRbRc._2._1) {
            setOp(SdivRaRbRc)
          }
          is (UmodRaRbRc._2._1) {
            setOp(UmodRaRbRc)
          }
          is (SmodRaRbRc._2._1) {
            setOp(SmodRaRbRc)
          }
          default {
            doDefault()
          }
        }
      }
      is (LdrRaRbSimm16._1) {
        //when (encInstr.rcIdx =/= 0x0) {
        //  setOp(LdrRaRbRc)
        //} otherwise {
        //  setOp(LdrRaRbSimm16)
        //}
        switch (encInstr.rcIdx(2 downto 0)) {
          is (LdrRaRbSimm16._2._1) {
            setOp(LdrRaRbSimm16)
          }
          is (StrRaRbSimm16._2._1) {
            setOp(StrRaRbSimm16)
          }
          is (LduhRaRbSimm16._2._1) {
            setOp(LduhRaRbSimm16)
          }
          is (LdshRaRbSimm16._2._1) {
            setOp(LdshRaRbSimm16)
          }
          is (LdubRaRbSimm16._2._1) {
            setOp(LdubRaRbSimm16)
          }
          is (LdsbRaRbSimm16._2._1) {
            setOp(LdsbRaRbSimm16)
          }
          is (SthRaRbSimm16._2._1) {
            setOp(SthRaRbSimm16)
          }
          is (StbRaRbSimm16._2._1) {
            setOp(StbRaRbSimm16)
          }
        }
      }
      //is (StrRaRbSimm16._1) {
      //  //when (encInstr.rcIdx =/= 0x0) {
      //  //  setOp(StrRaRbRc)
      //  //} otherwise {
      //    setOp(StrRaRbSimm16)
      //  //}
      //}
      //is (LduhRaRbRc._1) {
      //  switch (encInstr.imm16(1 downto 0)) {
      //    is (LduhRaRbRc._2._1) {
      //      setOp(LduhRaRbRc)
      //    }
      //    is (LdshRaRbRc._2._1) {
      //      setOp(LdshRaRbRc)
      //    }
      //    is (LdubRaRbRc._2._1) {
      //      setOp(LdubRaRbRc)
      //    }
      //    is (LdsbRaRbRc._2._1) {
      //      setOp(LdsbRaRbRc)
      //    }
      //  }
      //}
      //is (SthRaRbRc._1) {
      //  switch (encInstr.imm16(0 downto 0)) {
      //    is (SthRaRbRc._2._1) {
      //      setOp(SthRaRbRc)
      //    }
      //    is (StbRaRbRc._2._1) {
      //      setOp(StbRaRbRc)
      //    }
      //  }
      //}
      is (BeqRaRbSimm._1) {
        switch (encInstr.rcIdx(2 downto 0)) {
          is (BeqRaRbSimm._2._1) {
            //when (psId.startDecode) {
              //psId.nextPrevInstrWasJump := True
            //}
            when (
              encInstr.raIdx === encInstr.rbIdx
              && encInstr.raIdx =/= 0
              //&& encInstr.rbIdx === 0x0
            ) {
              setOp(BlSimm)
            } otherwise {
              setOp(BeqRaRbSimm)
              //setOp(BzRaSimm)
            }
          }
          is (BneRaRbSimm._2._1) {
            when (
              encInstr.raIdx === encInstr.rbIdx
              && encInstr.raIdx =/= 0x0
              //encInstr.rbIdx === 0x0
            ) {
              setOp(AddRaPcSimm16)
            } otherwise {
              //setOp(BnRaSimm)
              setOp(BneRaRbSimm)
              //when (psId.startDecode) {
                //psId.nextPrevInstrWasJump := True
              //}
            }
          }
          is (BltuRaRbSimm._2._1) {
            setOp(BltuRaRbSimm)
          }
          is (BgeuRaRbSimm._2._1) {
            setOp(BgeuRaRbSimm)
          }
          is (BltsRaRbSimm._2._1) {
            setOp(BltsRaRbSimm)
          }
          is (BgesRaRbSimm._2._1) {
            setOp(BgesRaRbSimm)
          }
          is (JlRaRb._2._1) {
            //when (psId.startDecode) {
              //psId.nextPrevInstrWasJump := True
            //}
            setOp(JlRaRb)
          }
          default {
            doDefault()
          }
        }
      }
      //is (CpyuRaRb._1) {
      //  switch (encInstr.rcIdx(0 downto 0)) {
      //    //is (CpyRaRb._2._1) {
      //    //  setOp(CpyRaRb)
      //    //}
      //    //is (CpyRaSimm16._2._1) {
      //    //  setOp(CpyRaSimm16)
      //    //}
      //    is (CpyuRaRb._2._1) {
      //      setOp(CpyuRaRb)
      //    }
      //    is (CpyuRaSimm16._2._1) {
      //      setOp(CpyuRaSimm16)
      //    }
      //  }
      //}
      //is (PushRaRb._1) {
      //  //doDefault()
      //  switch (encInstr.imm16(1 downto 0)) {
      //    //when (psId.rMultIn
      //    is (PushRaRb._2._1) {
      //      ////upPayload.gprIdxVec(0) := encInstr.rbIdx
      //      ////upPayload.gprIdxVec(1) := encInstr.raIdx
      //      //setOp(PushRaRb)
      //      ////when (psId.startDecode) {
      //      ////  psId.nextMultiInstrCnt := 1
      //      ////  setOp(StrRaRbSimm16)
      //      ////  upPayload.gprIdxVec(0) := encInstr.raIdx
      //      ////  upPayload.gprIdxVec(1) := encInstr.rbIdx
      //      ////  upPayload.gprIdxVec(2) := SnowHouseCpuRegs.r0.index
      //      ////  upPayload.imm := 0x0
      //      ////} otherwise {
      //      ////  setOp(AddRaRbSimm16)
      //      ////  upPayload.gprIdxVec(0) := encInstr.rbIdx
      //      ////  upPayload.gprIdxVec(1) := encInstr.rbIdx
      //      ////  upPayload.gprIdxVec(2) := SnowHouseCpuRegs.r0.index
      //      ////  val tempSImm = SInt(cfg.mainWidth bits)
      //      ////  tempSImm := -(cfg.mainWidth / 8)
      //      ////  upPayload.imm := tempSImm.asUInt
      //      ////}
      //      if (cfg.irqCfg != None) {
      //        upPayload.blockIrq := True
      //      }
      //      when (psId.rMultiInstrCnt.msb) {
      //        when (psId.startDecode) {
      //          when (cId.up.isFiring) {
      //            psId.nextMultiInstrCnt := 0x0
      //            setOp(LdrRaRbSimm16)
      //          }
      //          upPayload.imm.foreach(imm => {
      //            imm := 0x0
      //          })
      //        }
      //      } otherwise {
      //        when (cId.down.isFiring) {
      //          setOp(AddRaRbSimm16)
      //        }
      //        upPayload.gprIdxVec(0) := encInstr.rbIdx

      //        upPayload.imm.foreach(imm => {
      //          imm := (cfg.mainWidth / 8) //tempSImm.asUInt
      //        })
      //      }
      //    }
      //    is (PopRaRb._2._1) {
      //      if (cfg.irqCfg != None) {
      //        upPayload.blockIrq := True
      //      }
      //      when (psId.rMultiInstrCnt.msb) {
      //        when (psId.startDecode) {
      //          when (cId.up.isFiring) {
      //            psId.nextMultiInstrCnt := 0x0
      //            setOp(AddRaRbSimm16)
      //          }
      //        }
      //        upPayload.gprIdxVec(0) := encInstr.rbIdx
      //        val tempSImm = SInt(cfg.mainWidth bits)
      //        tempSImm := -(cfg.mainWidth / 8)
      //        upPayload.imm.foreach(imm => {
      //          imm := tempSImm.asUInt
      //        })
      //      } otherwise {
      //        when (cId.down.isFiring) {
      //          setOp(LdrRaRbSimm16)
      //        }
      //        upPayload.imm.foreach(imm => {
      //          imm := 0x0
      //        })
      //      }
      //    }
      //    is (PopPcRb._2._1) {
      //      if (cfg.irqCfg != None) {
      //        upPayload.blockIrq := True
      //      }
      //      when (
      //        //!rTempState
      //        psId.rMultiInstrCnt.msb
      //      ) {
      //        when (psId.startDecode) {
      //          when (
      //            cId.up.isFiring
      //            //cId.down.isFiring
      //          ) {
      //            //rTempState := True
      //            //dontChangeTempState := True
      //            psId.nextMultiInstrCnt := 0x1
      //            //doDefault()
      //            setOp(AddRaRbSimm16)
      //          }
      //        }
      //        upPayload.gprIdxVec(0) := encInstr.rbIdx
      //        //upPayload.gprIdxVec(1) := encInstr.rbIdx
      //        //upPayload.gprIdxVec(2) := SnowHouseCpuRegs.r0.index
      //        //val tempSImm = SInt(cfg.mainWidth bits)
      //        //tempSImm := -(cfg.mainWidth / 8)
      //        val tempSImm = SInt(cfg.mainWidth bits)
      //        tempSImm := -(cfg.mainWidth / 8)
      //        upPayload.imm.foreach(imm => {
      //          imm := tempSImm.asUInt
      //        })
      //      } otherwise {
      //        //when (psId.startDecode) {
      //        when (cId.down.isFiring) {
      //          when (psId.rMultiInstrCnt(0 downto 0) === 1) {
      //            setOp(
      //              //LdrRaRbSimm16
      //              PopPcRb._1
      //            )
      //            //upPayload.gprIdxVec(0) := SnowHouseCpuRegs.r0.index//encInstr.raIdx
      //            //upPayload.gprIdxVec(1) := encInstr.rbIdx
      //            //upPayload.gprIdxVec(2) := SnowHouseCpuRegs.r0.index
      //            upPayload.imm.foreach(imm => {
      //              imm := 0x0
      //            })
      //          } otherwise {
      //            setOp(
      //              //LdrRaRbSimm16
      //              PopPcRb._2
      //            )
      //            //upPayload.gprIdxVec(0) := SnowHouseCpuRegs.r0.index//encInstr.raIdx
      //            //upPayload.gprIdxVec(1) := encInstr.rbIdx
      //            //upPayload.gprIdxVec(2) := SnowHouseCpuRegs.r0.index
      //            upPayload.imm.foreach(imm => {
      //              imm := 0x0
      //            })
      //          }
      //        }
      //        //}
      //      }
      //    }
      //    default {
      //      doDefault()
      //    }
      //  }
      //}
      //is (LdrPdRbSimm16._1) {
      //  switch (encInstr.rcIdx(0 downto 0)) {
      //    is (LdrPdRbSimm16._2._1) {
      //      //setOp(LdrPdRbSimm16)
      //      upPayload.gprIdxVec(0) := SnowHouseCpuRegs.popData.index
      //      setOp(LdrRaRbSimm16)
      //    }
      //    is (JmpPd._2._1) {
      //      //setOp(JmpPd)
      //      //setOp(
      //      //  
      //      //)
      //      upPayload.gprIdxVec(0) := SnowHouseCpuRegs.r0.index
      //      upPayload.gprIdxVec(1) := SnowHouseCpuRegs.popData.index
      //    }
      //  }
      //}
      is (PreImm16._1) {
        doDefault(
          //doSetImm=false
        )
        //when (!rMultiCycleState) {
          when (
            cId.up.isFiring
          ) {
            if (cfg.irqCfg != None) {
              upPayload.blockIrq := True
            }
            when (!rMultiCycleState) {
              nextMultiCycleState := True
            }
          }
        //} otherwise {
        //  if(cfg.irqCfg != None) {
        //    upPayload.blockIrq := False
        //  }
        //}
      }
      default {
        doDefault()
      }
    }
    //setOp(
    //  JlRaRb,
    //  upPayload.irqJmpOp,
    //)
    //upPayload.irqJmpOp := JlRaRb
    //when (upPayload.takeIrq) {
    //  setOp(JlRaRb)
    //}
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
    SnowHouseCpuOp.SubReserved -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      aluOp=AluOpKind.Sub,
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
    SnowHouseCpuOp.OrRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Or,
    )
  )
  opInfoMap += (
    SnowHouseCpuOp.OrRaRbImm16 -> OpInfo.mkAlu(
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
  opInfoMap += (
    // and rA, rB, simm16
    SnowHouseCpuOp.AndRaRbSimm16 -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
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
    // lsl <Reserved>
    SnowHouseCpuOp.LslReserved -> OpInfo.mkAlu(
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
    // lsr <Reserved>
    SnowHouseCpuOp.LsrReserved -> OpInfo.mkAlu(
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
    // asr <Reserved>
    SnowHouseCpuOp.AsrReserved -> OpInfo.mkAlu(
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
  ////opInfoMap += (
  ////  SnowHouseCpuOp.CpyRaIds -> OpInfo.mkCpy(
  ////    dstArr=Array[DstKind](DstKind.Gpr),
  ////    srcArr=Array[SrcKind](SrcKind.Ids),
  ////    cpyOp=CpyOpKind.Cpy,
  ////  )
  ////)
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyIdsRb -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Ids),
  //    srcArr=Array[SrcKind](SrcKind.Gpr),
  //    cpyOp=CpyOpKind.Cpy,
  //  )
  //  //SnowHouseCpuOp.CpyIdsRb -> OpInfo.mkAlu(
  //  //  dstArr=Array[DstKind](DstKind.Ids),
  //  //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
  //  //  aluOp=AluOpKind.Add,
  //  //)
  //)
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyRaIra -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Ira),
  //    cpyOp=CpyOpKind.Cpy,
  //  )
  //  //SnowHouseCpuOp.CpyRaIra -> OpInfo.mkAlu(
  //  //  dstArr=Array[DstKind](DstKind.Gpr),
  //  //  srcArr=Array[SrcKind](SrcKind.Ira, SrcKind.Gpr),
  //  //  aluOp=AluOpKind.Add,
  //  //)
  //)
  ////opInfoMap += (
  ////  SnowHouseCpuOp.CpyIraRb -> OpInfo.mkCpy(
  ////    dstArr=Array[DstKind](DstKind.Ira),
  ////    srcArr=Array[SrcKind](SrcKind.Gpr),
  ////    cpyOp=CpyOpKind.Cpy,
  ////  )
  ////)
  ////opInfoMap += (
  ////  SnowHouseCpuOp.CpyRaIe -> OpInfo.mkCpy(
  ////    dstArr=Array[DstKind](DstKind.Gpr),
  ////    srcArr=Array[SrcKind](SrcKind.Ie),
  ////    cpyOp=CpyOpKind.Cpy,
  ////  )
  ////)
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyIeRb -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Ie),
  //    srcArr=Array[SrcKind](SrcKind.Gpr),
  //    cpyOp=CpyOpKind.Cpy,
  //  )
  //  //SnowHouseCpuOp.CpyIeRb -> OpInfo.mkAlu(
  //  //  dstArr=Array[DstKind](DstKind.Ie),
  //  //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
  //  //  aluOp=AluOpKind.Add,
  //  //)
  //)
  ////opInfoMap += (
  ////  SnowHouseCpuOp.RetIra -> OpInfo.mkCpy(
  ////    dstArr=Array[DstKind](DstKind.Pc, DstKind.Ie),
  ////    srcArr=Array[SrcKind](SrcKind.Ira),
  ////    cpyOp=CpyOpKind.Jmp,
  ////  )
  ////)
  //--------
  opInfoMap += (
    // mul rA, rB, rC
    SnowHouseCpuOp.MulRaRbRc -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Umul,
    )
  )
  opInfoMap += (
    // udiv rA, rB, rC
    SnowHouseCpuOp.UdivRaRbRc -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Udiv,
    )
  )
  opInfoMap += (
    // sdiv rA, rB, rC
    SnowHouseCpuOp.SdivRaRbRc -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Sdiv,
    )
  )
  opInfoMap += (
    // umod rA, rB, rC
    SnowHouseCpuOp.UmodRaRbRc -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Umod,
    )
  )
  opInfoMap += (
    // smod rA, rB, rC
    SnowHouseCpuOp.SmodRaRbRc -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Smod,
    )
  )
  //--------
  //opInfoMap += (
  //  SnowHouseCpuOp.LdrRaRbRc -> OpInfo.mkLdSt(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
  //    modify=MemAccessKind.Mem32(isSigned=false, isStore=false),
  //  )
  //)
  opInfoMap += (
    SnowHouseCpuOp.LdrRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      modify=MemAccessKind.Mem32(isSigned=false, isStore=false),
    )
  )
  //--------
  //opInfoMap += (
  //  SnowHouseCpuOp.StrRaRbRc -> OpInfo.mkLdSt(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
  //    modify=MemAccessKind.Mem32(isSigned=false, isStore=true),
  //  )
  //)
  opInfoMap += (
    SnowHouseCpuOp.StrRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)),
      modify=MemAccessKind.Mem32(isSigned=false, isStore=true),
    )
  )
  //--------
  opInfoMap += (
    //SnowHouseCpuOp.LduhRaRbRc -> OpInfo.mkLdSt(
    //  dstArr=Array[DstKind](DstKind.Gpr),
    //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
    //  modify=MemAccessKind.Mem16(isSigned=false, isStore=false),
    //)
    SnowHouseCpuOp.LduhRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem16(isSigned=false, isStore=false),
    )
  )
  opInfoMap += (
    //SnowHouseCpuOp.LdshRaRbRc -> OpInfo.mkLdSt(
    //  dstArr=Array[DstKind](DstKind.Gpr),
    //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
    //  modify=MemAccessKind.Mem16(isSigned=true, isStore=false),
    //)
    SnowHouseCpuOp.LdshRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem16(isSigned=true, isStore=false),
    )
  )
  opInfoMap += (
    //SnowHouseCpuOp.LdubRaRbRc -> OpInfo.mkLdSt(
    //  dstArr=Array[DstKind](DstKind.Gpr),
    //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
    //  modify=MemAccessKind.Mem8(isSigned=false, isStore=false),
    //)
    SnowHouseCpuOp.LdubRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem8(isSigned=false, isStore=false),
    )
  )
  opInfoMap += (
    //SnowHouseCpuOp.LdsbRaRbRc -> OpInfo.mkLdSt(
    //  dstArr=Array[DstKind](DstKind.Gpr),
    //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
    //  modify=MemAccessKind.Mem8(isSigned=true, isStore=false),
    //)
    SnowHouseCpuOp.LdsbRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem8(isSigned=true, isStore=false),
    )
  )
  //--------
  opInfoMap += (
    //SnowHouseCpuOp.SthRaRbRc -> OpInfo.mkLdSt(
    //  dstArr=Array[DstKind](DstKind.Gpr),
    //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
    //  modify=MemAccessKind.Mem16(isSigned=false, isStore=true),
    //)
    SnowHouseCpuOp.SthRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem16(isSigned=false, isStore=true),
    )
  )
  opInfoMap += (
    //SnowHouseCpuOp.StbRaRbRc -> OpInfo.mkLdSt(
    //  dstArr=Array[DstKind](DstKind.Gpr),
    //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
    //  modify=MemAccessKind.Mem8(isSigned=false, isStore=true),
    //)
    SnowHouseCpuOp.StbRaRbSimm16 -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
      modify=MemAccessKind.Mem8(isSigned=false, isStore=true),
    )
  )
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyRaIds -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Ids),
  //    cpyOp=CpyOpKind.Cpy,
  //  )
  //)
  opInfoMap += (
    SnowHouseCpuOp.CpyIdsRb -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Ids),
      srcArr=Array[SrcKind](SrcKind.Gpr),
      cpyOp=CpyOpKind.Cpy,
    )
    //SnowHouseCpuOp.CpyIdsRb -> OpInfo.mkAlu(
    //  dstArr=Array[DstKind](DstKind.Ids),
    //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
    //  aluOp=AluOpKind.Add,
    //)
  )
  opInfoMap += (
    SnowHouseCpuOp.CpyRaIra -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Ira),
      cpyOp=CpyOpKind.Cpy,
    )
    //SnowHouseCpuOp.CpyRaIra -> OpInfo.mkAlu(
    //  dstArr=Array[DstKind](DstKind.Gpr),
    //  srcArr=Array[SrcKind](SrcKind.Ira, SrcKind.Gpr),
    //  aluOp=AluOpKind.Add,
    //)
  )
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyIraRb -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Ira),
  //    srcArr=Array[SrcKind](SrcKind.Gpr),
  //    cpyOp=CpyOpKind.Cpy,
  //  )
  //)
  //opInfoMap += (
  //  SnowHouseCpuOp.CpyRaIe -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Ie),
  //    cpyOp=CpyOpKind.Cpy,
  //  )
  //)
  opInfoMap += (
    SnowHouseCpuOp.CpyIeRb -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Ie),
      srcArr=Array[SrcKind](SrcKind.Gpr),
      cpyOp=CpyOpKind.Cpy,
    )
    //SnowHouseCpuOp.CpyIeRb -> OpInfo.mkAlu(
    //  dstArr=Array[DstKind](DstKind.Ie),
    //  srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
    //  aluOp=AluOpKind.Add,
    //)
  )
  //opInfoMap += (
  //  SnowHouseCpuOp.RetIra -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Pc, DstKind.Ie),
  //    srcArr=Array[SrcKind](SrcKind.Ira),
  //    cpyOp=CpyOpKind.Jmp,
  //  )
  //)
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
    // bz rA, simm
    //SnowHouseCpuOp.BzRaSimm -> OpInfo.mkCpy(
    //  dstArr=Array[DstKind](DstKind.Pc),
    //  srcArr=Array[SrcKind](
    //    SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
    //  ),
    //  cpyOp=CpyOpKind.Br,
    //  cond=CondKind.Z,
    //)
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
    // bnz rA, simm
    //SnowHouseCpuOp.BnzRaSimm -> OpInfo.mkCpy(
    //  dstArr=Array[DstKind](DstKind.Pc),
    //  srcArr=Array[SrcKind](
    //    SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
    //  ),
    //  cpyOp=CpyOpKind.Br,
    //  cond=CondKind.Nz,
    //)
  )
  opInfoMap += (
    // bltu rA, rB, simm16
    SnowHouseCpuOp.BltuRaRbSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Ltu
    )
  )
  opInfoMap += (
    // bgeu rA, rB, simm16
    SnowHouseCpuOp.BgeuRaRbSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Geu
    )
  )
  opInfoMap += (
    // blts rA, rB, simm16
    SnowHouseCpuOp.BltsRaRbSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Lts
    )
  )
  opInfoMap += (
    // bges rA, rB, simm16
    SnowHouseCpuOp.BgesRaRbSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](
        SrcKind.Gpr, SrcKind.Gpr, SrcKind.Imm(/*Some(true)*/)
      ),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Ges
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
    SnowHouseCpuOp.RetIra -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc, DstKind.Ie),
      srcArr=Array[SrcKind](SrcKind.Ira),
      cpyOp=CpyOpKind.Jmp,
    )
  )
  //opInfoMap += (
  //  SnowHouseCpuOp.JmpPd -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Pc),
  //    srcArr=Array[SrcKind](SrcKind.Gpr),
  //    cpyOp=CpyOpKind.Jmp
  //  )
  //)
  //opInfoMap += (
  //  SnowHouseCpuOp.JmpReserved -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Pc),
  //    srcArr=Array[SrcKind](SrcKind.Gpr),
  //    cpyOp=CpyOpKind.Jmp,
  //  )
  //)
  //--------
  //opInfoMap += (
  //  SnowHouseCpuOp.PushRaRb -> OpInfo.mkLdSt(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Gpr),
  //    modify=MemAccessKind.Mem32(
  //      isSigned=false, isStore=true, isPush=true,
  //    )
  //  )
  //)
  //opInfoMap ++ (
  //  SnowHouseCpuOp.PopRaRb -> OpInfo.mkLdSt(
  //  )
  //)
  //opInfoMap += (
  //  SnowHouseCpuOp.PopPcRb._1 -> OpInfo.mkLdSt(
  //    dstArr=Array[DstKind](DstKind.PopData),
  //    srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm()),
  //    modify=MemAccessKind.Mem32(
  //      isSigned=false, isStore=false
  //    )
  //  )
  //)
  //opInfoMap += (
  //  SnowHouseCpuOp.PopPcRb._2 -> OpInfo.mkCpy(
  //    dstArr=Array[DstKind](DstKind.Pc),
  //    srcArr=Array[SrcKind](SrcKind.PopData),
  //    cpyOp=CpyOpKind.Jmp,
  //  )
  //)
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
  //exposeGprsToIo: Option[Seq[Int]]=Some({
  //  import SnowHouseCpuRegs._
  //  val tempArr = ArrayBuffer[Int]()
  //  //tempArr += r1.index
  //  tempArr += r7.index
  //  tempArr += r8.index
  //  tempArr
  //})
  exposeModMemWordToIo: Boolean=false,
) {
  //--------
  val instrMainWidth = 32
  val mainWidth = (
    32
    //16
  )
  val numGprs = SnowHouseCpuInstrEnc.numGprs //+ 1
  val modRdPortCnt = 3
  val pipeName="SnowHouseCpu"
  //--------
  val shCfg = SnowHouseConfig(
    haveZeroReg=Some(0),
    irqCfg=(
      //None
      Some(
        SnowHouseIrqConfig.IraIds(
          ////iraRegIdx
          ////allowNestedIrqs=true,
          //allowIrqStorm=(
          //  true
          //  //false
          //),
        ),
      )
    ),
    haveAluFlags=false,
    //encInstrType=SnowHouseCpuEncInstr(),
    subCfg={
      val icacheDepth = /*8192*/ 2048 // 8 kiB icache
      val icacheLineSizeBytes = 64
      val icacheBusSrcNum = 0x0
      val dcacheDepth = /*512*/ 2048 /*8192*/ // 8 kiB dcache
      val dcacheLineSizeBytes = 64
      val dcacheBusSrcNum = 0x1
      SnowHouseSubConfig(
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
          memRamStyle="distributed",
        ),
        haveIcache=true,
        icacheDepth=icacheDepth,
        icacheLineSizeBytes=icacheLineSizeBytes,
        icacheBusSrcNum=icacheBusSrcNum,
        haveDcache=true,
        dcacheDepth=dcacheDepth,
        dcacheLineSizeBytes=dcacheLineSizeBytes,
        dcacheBusSrcNum=dcacheBusSrcNum,
        totalNumBusHosts=2,
        optCacheBusSrcWidth=None,
      )
    },
    opInfoMap=SnowHouseCpuOpInfoMap.opInfoMap,
    irqJmpOp={
      var myIrqJmpOp: Int = 0x0
      for (
        ((tuple, opInfo), opInfoIdx)
        <- SnowHouseCpuOpInfoMap.opInfoMap.view.zipWithIndex
      ) {
        if (tuple == SnowHouseCpuOp.JlRaRb) {
          myIrqJmpOp = opInfoIdx
        }
      }
      myIrqJmpOp
    },
    //irqRetIraOp={
    //  var myIrqRetIraOp: Int = 0x0
    //  for (
    //    ((tuple, opInfo), opInfoIdx)
    //    <- SnowHouseCpuOpInfoMap.opInfoMap.view.zipWithIndex
    //  ) {
    //    if (tuple == SnowHouseCpuOp.RetIra) {
    //      myIrqRetIraOp = opInfoIdx
    //    }
    //  }
    //  myIrqRetIraOp
    //},
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
    supportUcode=(
      //true
      false
    ),
    //decodeFunc=(
    //  io: SnowHouseIo[SnowHouseCpuEncInstr],
    //  cId: CtrlLink,
    //  decInstr: UInt,
    //) => new Area {
    //  //decInstr := U"${mainWidth}'d0"
    //},
    //maxNumGprsPerInstr=3,
    //exposeGprsToIo=exposeGprsToIo
    exposeModMemWordToIo=exposeModMemWordToIo,
    optFormal=optFormal,
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
    cpy(r0, 0x0),             // 0x0: r0 = 0
    cpy(r0, 0x0),             // 0x4: r0 = 0
    //cpy(r0, 0x0),             // 0x4: r0 = 0
    cpy(r1, LbR"irq_handler"),  // 0x8
    cpy(ids, r1),             // 0xc
    cpy(r1, 0x1),             // 0x10
    //cpy(r1, 0x0),
    cpy(ie, r1),              // 0x14
    //cpy(r0, r0),
    cpy(r1, 0x8),             // 0x18: r1 = 8
    cpy(r2, 0x1),             // 0x1c: r2 = 1
    cpy(r3, 0x1000),          // 0x20: r3 = 0x1000
    cpy(r4, 0x8),             // 0x24: r4 = 4
    cpy(r5, LbR"increment"),  // 0x28
    cpy(sp, 0x800),          // 0x2c
    cpy(r6, 0x20),            // 0x30: r6 = 0x20
    str(r6, r3, 0x0),         // 0x34: [r0 + r3] = r6
    ldr(r5, r3, 0x0),         // 0x38
    str(r5, r3, 0x1000),         // 0x3c
    ldr(r6, r3, 0x1000),         // 0x40
    //add(r7, r6, 4),
    cpy(r7, 0x4),             // 0x44
    mul(r9, r5, r7),          // 0x48
    //--------
    Lb"push_loop",
    str(r7, sp, 0),           // 0x4c
    ldr(r8, sp, 0),           // 0x50
    //add(sp, sp, 4),
    add(r9, r8, 1),           // 0x54
    add(r9, r9, 1),           // 0x58
    //add(r12, r8, 1),
    //add(r0, r0, 1),
    //cpy(r0, r0),        
    //add(r9, r9, 1),
    //sub(sp, sp, 4),
    //ldr(r8, sp, 0),
    //push(r7),
    //pop(r8),
    //push(r8),
    //pop(r9),
    sub(r7, r7, 1),           // 0x5c
    //sub(r6, r6, 1),
    bnz(r7, LbR"push_loop"),  // 0x60
    //cpy(r0, r0),
    //cpy(r0, r0),
    //cpy(r0, r0),
    mul(r7, r6, r1),          // 0x64
    udiv(r7, r6, r1),         // 0x68
    umod(r8, r6, r1),         // 0x6c
    //add(r7, r6, r1),
    //sub(r8, r6, r1),
    //--------
    Lb"loop",
    //add(r0, r1, r2),
    //cpyu(r2, tempData >> 16),
    //cpy(r2, tempData & 0xffff),
    ldr(r6, r3, 0x0),         // 0x70:
    //add(r6, r6, 0x1),       
    //jl(r5),
    bl(LbR"increment"),       // 0x74:
    str(r6, r3, 0x4),         // 0x78:
    add(r3, r3, 0x4),         // 0x7c: r3 += 4
    sub(r1, r1, 0x1),         // 0x80: r1 -= 1 
    bl(LbR"divmod"),          // 0x84
    //mul(r7, r6, r1),
    bnz(r1, LbR"loop"),       // 0x88: if (r1 != 0) goto LbR"loop"
    ////--------
    //cpy(r12, 0x0),              // 0x4c
    Lb"infin",
    //cpy(r12, 0x0),              // 0x4c
    bz(r0, LbR"infin"),       // 0x8c
    //Db32(0x3f),
    ////--------
    Lb"increment",
    add(r6, r6, 0x1),         // 0x90
    //add(r6, r6, r0),
    jmp(lr),                  // 0x94
    ////--------
    Lb"divmod",
    ////mul(r7, r6, r1),
    //cpy(r0, r0),
    //--------
    udiv(r7, r6, r1),         // 0x98
    umod(r8, r6, r1),         // 0x9c
    //add(r7, r6, r1),
    //sub(r8, r6, r1),
    //--------
    jmp(lr),                  // 0xa0
    //cpy(r0, r0),
    //cpy(r0, r0),
    //--------
    Lb"irq_handler",
    add(r10, r10, 1),         // 0xa4
    retIra(),                 // 0xa8
  )
  val program = SnowHouseCpuProgram(cfg=cfg)
  //val outpArr = ArrayBuffer[BigInt]()
  //val assembler = SnowHouseCpuAssembler(
  //  stmtArr=program,
  //  outpArr=outpArr,
  //)
}
case class SnowHouseCpuMul32(
  cpuIo: SnowHouseIo,
) extends Area {
  def cfg = cpuIo.cfg
  for (
    //(multiCycleBus, busIdx) <- cpuIo.multiCycleBusVec.view.zipWithIndex
    ((_, opInfo), busIdx)
    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    opInfo.multiCycleOp.get match {
      case MultiCycleOpKind.Umul => {
        //cpuIo.multiCycleBusVec
        val multiCycleBus = cpuIo.multiCycleBusVec(busIdx)
        def dstVec = multiCycleBus.recvData.dstVec
        //dstVec.setAsReg
        //dstVec(0) := (
        //  RegNext(
        //    next=dstVec(0),
        //    init=dstVec(0).getZero
        //  )
        //)
        //when (
        //  multiCycleBus.rValid
        //  && multiCycleBus.ready
        //) {
        //  // TODO: add support for more kinds of operations
        //  multiCycleBus.recvData.dstVec(0) := (
        //    multiCycleBus.sendData.srcVec(0)
        //    * multiCycleBus.sendData.srcVec(1)
        //  )(cfg.mainWidth - 1 downto 0)
        //}
        //multiCycleBus.ready := multiCycleBus.rValid
        def srcVec = multiCycleBus.sendData.srcVec
        def mainWidth = cfg.mainWidth
        object UMul32State
        extends SpinalEnum(defaultEncoding=binarySequential) {
          val
            DO_THREE_MUL16X16,
            FIRST_ADD,
            SECOND_ADD,
            YIELD_RESULT
            = newElement()
        }
        val rState = (
          Reg(UMul32State()) init(UMul32State.DO_THREE_MUL16X16)
        )
        val low = (mainWidth >> 1) - 1 downto 0
        val high = (mainWidth - 1 downto (mainWidth >> 1))
        val shiftAmount = mainWidth >> 1
        //val mulCond = (
        //  rState === UMul32State.DO_THREE_MUL16X16
        //  && multiCycleBus.rValid
        //)
        val rY0X0 = (
          RegNext(
            //UInt(cfg.mainWidth bits)
            next=(
              srcVec(1)(low) * srcVec(0)(low)
            ),
            //cond=mulCond
          )
          init(0x0)
        )
        val rY1X0 = (
          RegNext(
            //UInt(cfg.mainWidth bits)
            next=(
              srcVec(1)(high) * srcVec(0)(low)
            ),
            //cond=mulCond,
          )
          init(0x0)
        )
        val rY0X1 = (
          RegNext(
            //UInt(cfg.mainWidth bits)
            next=(
              srcVec(1)(low) * srcVec(0)(high)
            ),
            //cond=mulCond,
          )
          init(0x0)
        )
        val rPartialSum = Vec.fill(2)(
          Reg(UInt(mainWidth bits))
          init(0x0)
        )
        rPartialSum(0) := (
          rY1X0 + rY0X1
        )
        rPartialSum(1) := (
          (rPartialSum(0) << shiftAmount)
          + rY0X0
        )(rPartialSum(1).bitsRange)

        multiCycleBus.ready := False
        val rDst = (
          Reg(
            cloneOf(dstVec(0)),
            init=dstVec(0).getZero
          )
        )
        dstVec(0) := rDst
        switch (rState) {
          is (UMul32State.DO_THREE_MUL16X16) {
            when (RegNext(multiCycleBus.nextValid)) {
              rState := UMul32State.FIRST_ADD
            }
          }
          is (UMul32State.FIRST_ADD) {
            rState := UMul32State.SECOND_ADD
            rDst := rPartialSum(1)
          }
          is (UMul32State.SECOND_ADD) {
            rState := UMul32State.YIELD_RESULT
            //dstVec(0) := (
            //)
            //rDst := rPartialSum(1)
          }
          is (UMul32State.YIELD_RESULT) {
            rState := UMul32State.DO_THREE_MUL16X16
            multiCycleBus.ready := True
          }
        }
      }
      case _ => {
      }
    }
  }
}
case class SnowHouseCpuDivmod32(
  cpuIo: SnowHouseIo
) extends Area {
  def cfg = cpuIo.cfg
  val divmod = LongDivMultiCycle(
    mainWidth=cfg.mainWidth,
    denomWidth=cfg.mainWidth,
    chunkWidth=1,//2,
    signedReset=0x0,
  )
  object Divmod32State
  extends SpinalEnum(defaultEncoding=binarySequential) {
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
  val rState = Reg(Divmod32State()) init(Divmod32State.IDLE)
  object Divmod32Kind
  extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      UDIV,
      SDIV,
      UMOD,
      SMOD
      = newElement()
  }
  val rKind = (
    Reg(Divmod32Kind())
    init(Divmod32Kind.UDIV)
  )
  val rPrevKind = {
    val temp = (
      Reg(Flow(Divmod32Kind()))
    )
    temp.valid.init(temp.valid.getZero)
    temp.payload.init(Divmod32Kind.UDIV)
    temp
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
      //(multiCycleBus, busIdx) <- cpuIo.multiCycleBusVec.view.zipWithIndex
      ((_, opInfo), busIdx)
      <- cfg.multiCycleOpInfoMap.view.zipWithIndex
    ) {
      opInfo.multiCycleOp.get match {
        case MultiCycleOpKind.Udiv => {
          if (!needBusRvalid) {
            val tempArea = doItFunc(
              opInfo,
              busIdx,
              cpuIo.multiCycleBusVec(busIdx)
            )
          } else {
            when (RegNext(cpuIo.multiCycleBusVec(busIdx).nextValid)) {
              if (setKind) {
                rKind := Divmod32Kind.UDIV
              }
              val tempArea = doItFunc(
                opInfo,
                busIdx,
                cpuIo.multiCycleBusVec(busIdx)
              )
            }
          }
        }
        case MultiCycleOpKind.Sdiv => {
          if (!needBusRvalid) {
            val tempArea = doItFunc(
              opInfo,
              busIdx,
              cpuIo.multiCycleBusVec(busIdx)
            )
          } else {
            when (RegNext(cpuIo.multiCycleBusVec(busIdx).nextValid)) {
              if (setKind) {
                rKind := Divmod32Kind.SDIV
              }
              val tempArea = doItFunc(
                opInfo,
                busIdx,
                cpuIo.multiCycleBusVec(busIdx)
              )
            }
          }
        }
        case MultiCycleOpKind.Umod => {
          if (!needBusRvalid) {
            val tempArea = doItFunc(
              opInfo,
              busIdx,
              cpuIo.multiCycleBusVec(busIdx)
            )
          } else {
            when (RegNext(cpuIo.multiCycleBusVec(busIdx).nextValid)) {
              if (setKind) {
                rKind := Divmod32Kind.UMOD
              }
              val tempArea = doItFunc(
                opInfo,
                busIdx,
                cpuIo.multiCycleBusVec(busIdx)
              )
            }
          }
        }
        case MultiCycleOpKind.Smod => {
          if (!needBusRvalid) {
            val tempArea = doItFunc(
              opInfo,
              busIdx,
              cpuIo.multiCycleBusVec(busIdx)
            )
          } else {
            when (RegNext(cpuIo.multiCycleBusVec(busIdx).nextValid)) {
              if (setKind) {
                rKind := Divmod32Kind.SMOD
              }
              val tempArea = doItFunc(
                opInfo,
                busIdx,
                cpuIo.multiCycleBusVec(busIdx)
              )
            }
          }
        }
        case _ => {
        }
      }
    }
  }
  val rSavedSrcVec = Vec.fill(2)(
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
  divmod.io.inp.numer := rSavedSrcVec(0)
  divmod.io.inp.denom := rSavedSrcVec(1)
  divmod.io.inp.signed := rKind.asBits(0)
  //switch (rKind) {
    for (
      ((_, opInfo), busIdx)
      <- cfg.multiCycleOpInfoMap.view.zipWithIndex
    ) {
      opInfo.multiCycleOp.get match {
        case MultiCycleOpKind.Udiv => {
          //is (Divmod32Kind.UDIV) {
            val stallIo = (
              cpuIo.multiCycleBusVec(busIdx)
            )
            def dstVec = stallIo.recvData.dstVec
            //stallIo.ready := True
            dstVec(0) := rSavedResult.last(0)
          //}
        }
        case MultiCycleOpKind.Sdiv => {
          //is (Divmod32Kind.SDIV) {
            val stallIo = (
              cpuIo.multiCycleBusVec(busIdx)
            )
            def dstVec = stallIo.recvData.dstVec
            //stallIo.ready := True
            dstVec(0) := rSavedResult.last(1)
          //}
        }
        case MultiCycleOpKind.Umod => {
          //is (Divmod32Kind.UMOD) {
            val stallIo = (
              cpuIo.multiCycleBusVec(busIdx)
            )
            def dstVec = stallIo.recvData.dstVec
            //stallIo.ready := True
            dstVec(0) := rSavedResult.last(2)
          //}
        }
        case MultiCycleOpKind.Smod => {
          //is (Divmod32Kind.SMOD) {
            val stallIo = (
              cpuIo.multiCycleBusVec(busIdx)
            )
            def dstVec = stallIo.recvData.dstVec
            //stallIo.ready := True
            dstVec(0) := rSavedResult.last(3)
          //}
        }
        case _ => {
        }
      }
    }
  //}
  switch (rState) {
    is (Divmod32State.IDLE) {
      val idleArea = myFunc(
        doItFunc=(
          opInfo,
          busIdx,
          stallIo,
        ) => new Area {
          rState := Divmod32State.CHECK_PREV
          def dstVec = stallIo.recvData.dstVec
          def srcVec = stallIo.sendData.srcVec
          rSavedSrcVec(0) := srcVec(0)
          rSavedSrcVec(1) := srcVec(1)
        },
        setKind=true,
      )
    }
    is (Divmod32State.CHECK_PREV) {
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
      //    //    // encoding of `Divmod32Kind`
      //    //    rPrevKind.payload.asBits(0) === rKind.asBits(0)
      //    //  ) && (
      //    //    srcVec(0) === rSavedSrcVec(0)
      //    //  ) && (
      //    //    srcVec(1) === rSavedSrcVec(1)
      //    //  )
      //    //) {
      //    //  stallIo.ready := True
      //    //  rState := Divmod32State.IDLE
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

      //    //  rState := Divmod32State.RUNNING
      //    //}
      //    divmod.io.inp.valid := True
      //    divmod.io.inp.numer := rSavedSrcVec(0)
      //    divmod.io.inp.denom := rSavedSrcVec(1)
      //    divmod.io.inp.signed := rKind.asBits(0)
      //    rState := Divmod32State.RUNNING
      //  },
      //  setKind=false,
      //)
      divmod.io.inp.valid := True
      rState := Divmod32State.RUNNING
    }
    is (Divmod32State.RUNNING) {
      //val runningArea = myFunc(
      //  doItFunc=(
      //    opInfo,
      //    busIdx,
      //    stallIo,
      //  ) => new Area {
      //  },
      //  setKind=false,
      //)
      when (divmod.io.outp.ready) {
        rState := Divmod32State.YIELD_RESULT_PIPE_3
      }
    }
    is (Divmod32State.YIELD_RESULT_PIPE_3) {
      rState := Divmod32State.YIELD_RESULT_PIPE_2
    }
    is (Divmod32State.YIELD_RESULT_PIPE_2) {
      rState := Divmod32State.YIELD_RESULT_PIPE_1
    }
    is (Divmod32State.YIELD_RESULT_PIPE_1) {
      rState := Divmod32State.YIELD_RESULT
    }
    is (Divmod32State.YIELD_RESULT) {
      rPrevKind.valid := True
      rPrevKind.payload := rKind
      //val yieldResultArea = myFunc(
      //  doItFunc=(
      //    opInfo,
      //    busIdx,
      //    stallIo,
      //  ) => new Area {
      //    def dstVec = stallIo.recvData.dstVec
      //    rState := Divmod32State.IDLE
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
          ((_, opInfo), busIdx)
          <- cfg.multiCycleOpInfoMap.view.zipWithIndex
        ) {
          opInfo.multiCycleOp.get match {
            case MultiCycleOpKind.Udiv => {
              //is (Divmod32Kind.UDIV) {
                val stallIo = (
                  cpuIo.multiCycleBusVec(busIdx)
                )
                def dstVec = stallIo.recvData.dstVec
                stallIo.ready := True
                //dstVec(0) := rSavedResult.last(0)
              //}
            }
            case MultiCycleOpKind.Sdiv => {
              //is (Divmod32Kind.SDIV) {
                val stallIo = (
                  cpuIo.multiCycleBusVec(busIdx)
                )
                def dstVec = stallIo.recvData.dstVec
                stallIo.ready := True
                //dstVec(0) := rSavedResult.last(1)
              //}
            }
            case MultiCycleOpKind.Umod => {
              //is (Divmod32Kind.UMOD) {
                val stallIo = (
                  cpuIo.multiCycleBusVec(busIdx)
                )
                def dstVec = stallIo.recvData.dstVec
                stallIo.ready := True
                //dstVec(0) := rSavedResult.last(2)
              //}
            }
            case MultiCycleOpKind.Smod => {
              //is (Divmod32Kind.SMOD) {
                val stallIo = (
                  cpuIo.multiCycleBusVec(busIdx)
                )
                def dstVec = stallIo.recvData.dstVec
                stallIo.ready := True
                //dstVec(0) := rSavedResult.last(3)
              //}
            }
            case _ => {
            }
          }
        }
      //}
      rState := Divmod32State.IDLE
    }
  }
}
case class SnowHouseCpuWithDualRamIo(
  program: SnowHouseCpuProgram,
) extends Bundle {
  def cfg = program.cfg
  val idsIraIrq = (
    slave(new LcvStallIo[Bool, Bool](
      sendPayloadType=None,
      recvPayloadType=None,
    ))
  )
  val modMemWord = (
    cfg.exposeModMemWordToIo
  ) generate (
    out(UInt(cfg.shCfg.mainWidth bits))
  )
}
case class SnowHouseCpuWithDualRam(
  program: SnowHouseCpuProgram,
  doConnExternIrq: Boolean=true,
) extends Component {
  val io = SnowHouseCpuWithDualRamIo(program=program)
  def cfg = program.cfg
  val cpu = SnowHouse(cfg=cfg.shCfg)
  val dualRam = SnowHouseInstrDataDualRam(
    cfg=cfg.shCfg,
    instrInitBigInt=program.outpArr,
    dataInitBigInt=(
      Array.fill(
        //1 << 16
        1 << (16 - 2)
        //1 << (16 - 4)
      )(BigInt(0))
    ),
  )
  cpu.io.ibus <> dualRam.io.ibus
  cpu.io.dbus <> dualRam.io.dbus
  dualRam.io.dcacheHaveHazard := cpu.io.dcacheHaveHazard
  if (cfg.exposeModMemWordToIo) {
    cpu.io.modMemWord <> io.modMemWord
  }
  //for ((multiCycleBus, idx) <- cpu.io.multiCycleBusVec.view.zipWithIndex) {
  //  if (idx != 0) {
  //    multiCycleBus.ready := True
  //    multiCycleBus.recvData.dstVec.foreach(dst => {
  //      dst := dst.getZero
  //    })
  //  }
  //}
  val mul32 = SnowHouseCpuMul32(cpuIo=cpu.io)
  val divmod32 = SnowHouseCpuDivmod32(cpuIo=cpu.io)

  if (doConnExternIrq) {
    cpu.io.idsIraIrq <> io.idsIraIrq
  } else {
    io.idsIraIrq.ready := True
    //cpu.io.idsIraIrq.nextValid := True
    val cntWidth = 8
    val rIrqValidCnt = (
      Reg(UInt(cntWidth bits))
      init(U(cntWidth bits, default -> True))
    )
    //cpu.io.idsIraIrq.nextValid := True
    cpu.io.idsIraIrq.nextValid := False
    when (rIrqValidCnt =/= 0) {
      rIrqValidCnt := rIrqValidCnt - 1
    } otherwise {
      cpu.io.idsIraIrq.nextValid := True
      when (cpu.io.idsIraIrq.rValid && cpu.io.idsIraIrq.ready) {
        cpu.io.idsIraIrq.nextValid := False
        rIrqValidCnt := U(cntWidth bits, default -> True)
      }
    }
  }
  //--------
  //val rMultiCycleBusReadyCnt = (
  //  Reg(UInt(8 bits))
  //  init(0x3)
  //)
  //val rMultiCycleBusState = (
  //  Reg(Bool(), init=False)
  //)
  //when (rMultiCycleBusReadyCnt > 0) {
  //  rMultiCycleBusReadyCnt := rMultiCycleBusReadyCnt - 1
  //} otherwise {
  //  multiCycleBus.ready := True
  //  when (!rMultiCycleBusState) {
  //    rMultiCycleBusReadyCnt := 3
  //  } otherwise {
  //    rMultiCycleBusReadyCnt := 5
  //  }
  //}
}
object SnowHouseCpuWithDualRamToVerilog extends App {
  Config.spinal.generateVerilog({
    //val cfg = SnowHouseCpuConfig(
    //  optFormal=(
    //    false
    //  )
    //)
    val cfg = SnowHouseCpuConfig(
      optFormal=(
        //true
        false
      ),
      exposeModMemWordToIo=true,
    )
    val testProgram = SnowHouseCpuTestProgram(cfg=cfg)
    SnowHouseCpuWithDualRam(program=testProgram.program)
  })
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
    SnowHouseCpuWithDualRam(
      program=testProgram.program,
      doConnExternIrq=false,
    )
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
    anyseq(dut.io.ibus.recvData)
    anyseq(dut.io.ibus.ready)
    if (dut.io.haveMultiCycleBusVec) {
      for (
        (multiCycleBus, busIdx)
        <- dut.io.multiCycleBusVec.view.zipWithIndex
      ) {
        anyseq(multiCycleBus.recvData)
        anyseq(multiCycleBus.ready)
      }
    }
    anyseq(dut.io.dbus.recvData)
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
