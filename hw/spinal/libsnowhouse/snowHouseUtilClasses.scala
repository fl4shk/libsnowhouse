package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._

import libcheesevoyage.general._
import libcheesevoyage.math._
import libcheesevoyage.bus.lcvStall._

case class SnowHouseRegFileConfig(
  mainWidth: Int,
  wordCountArr: Seq[Int],
  modRdPortCnt: Int,
  pipeName: String,
  //linkArr: Option[ArrayBuffer[Link]]=None,
) {
  val modStageCnt: Int = 1
}

//case class SnowHouseRegFileSliceInfo(
//  shRegFileCfg: SnowHouseRegFileConfig,
//) {
//}
case class SnowHouseConfig[
  EncInstrT <: Data,
](
  encInstrType: HardType[EncInstrT],
  //gprFileDepth: Int,
  //sprFileDepth: Int,
  instrMainWidth: Int,
  shRegFileCfg: SnowHouseRegFileConfig,
  opInfoMap: LinkedHashMap[Any, OpInfo],
  //decodeFunc: (
  //  SnowHouseIo[EncInstrT], // io
  //  CtrlLink,               // cId
  //  UInt,                   // output the decoded instruction
  //) => Area,                
  psDecode: SnowHousePsDecode[EncInstrT],
  optFormal: Boolean,
  modOpCntWidth: Int=8,
) {
  //--------
  val pureCpyOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val pureCpyuiOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val pureJmpOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val pureBrOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val aluOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val multiCycleOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val loadOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val storeOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //--------
  for (((_, opInfo), idx) <- opInfoMap.view.zipWithIndex) {
    opInfo.select match {
      case OpSelect.Cpy => {
        assert(
          opInfo.findValidArgs(opInfo.cpyOp.get) != None,
          s"Error: unsupported combination or "
          + s"number of destination/source operands: "
          + s"opInfo(${opInfo}), instructionIndex:${idx}"
        )
        //opInfo.cpyOp.get match {
        //  case CpyOpKind.Cpy => {
        //    assert(
        //      opInfo.dstArr.find(_ == DstKind.Pc) == None,
        //      s"Error: unsupported PC as destination of a CpyOpKind.Cpy "
        //      + s"instruction: "
        //      + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //    )
        //    pureCpyOpInfoMap += (idx -> opInfo)
        //  }
        //  case CpyOpKind.Cpyui => {
        //    assert(
        //      opInfo.dstArr.find(_ == DstKind.Pc) == None,
        //      s"Error: unsupported PC as destination of a CpyOpKind.Cpyui "
        //      + s"instruction: "
        //      + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //    )
        //    pureCpyuiOpInfoMap += (idx -> opInfo)
        //  }
        //  case CpyOpKind.Jmp => { // non-relative jumps
        //    assert(
        //      opInfo.dstArr.find(_ == DstKind.Pc) != None,
        //      s"Error: unsupported lack of PC as (any) destination of a "
        //      + s"CpyOpKind.Jmp "
        //      + s"instruction: "
        //      + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //    )
        //    pureJmpOpInfoMap += (idx -> opInfo)
        //  }
        //  case CpyOpKind.Br => { // relative branches
        //    assert(
        //      opInfo.dstArr.find(_ == DstKind.Pc) != None,
        //      s"Error: unsupported lack of PC as (any) destination of a "
        //      + s"CpyOpKind.Br "
        //      + s"instruction: "
        //      + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //    )
        //    pureBrOpInfoMap += (idx -> opInfo)
        //  }
        //}
      }
      case OpSelect.Alu => {
        assert(
          opInfo.findValidArgs(opInfo.aluOp.get) != None,
          s"Error: unsupported combination or "
          + s"number of destination/source operands: "
          + s"opInfo(${opInfo}), instructionIndex:${idx}"
        )
        //assert(
        //  opInfo.dstArr.find(_ == DstKind.Pc) == None,
        //  s"Error: unsupported PC as destination of an ALU "
        //  + s"instruction (though this may be supported later!): "
        //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //)
        //opInfo.aluOp.get match {
        //  case (
        //    AluOpKind.Add 
        //    | AluOpKind.Lsl | AluOpKind.Lsr | AluOpKind.Asr
        //    | AluOpKind.And | AluOpKind.Or | AluOpKind.Xor
        //  ) => {
        //    assert(
        //      opInfo.srcArr.find(_ == SrcKind.AluFlags) == None,
        //      s"Error: unsupported `AluOpKind` with source operand that is "
        //      + s"`SrcKind.AluFlags`: "
        //      + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //    )
        //  }
        //  case (
        //    AluOpKind.Adc
        //    | AluOpKind.Sbc
        //  ) => {
        //    assert(
        //      opInfo.srcArr.find(_ == SrcKind.AluFlags) != None,
        //      s"Error: unsupported `AluOpKind` without any source operand "
        //      + s"that is `SrcKind.AluFlags`: "
        //      + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //    )
        //  }
        //  //case AluOpKind.CmpBc => {
        //  //}
        //  case AluOpKind.Sub => {
        //  }
        //  case (
        //    AluOpKind.Sltu | AluOpKind.Slts
        //  ) => {
        //    assert(
        //      opInfo.dstArr.find(_ == DstKind.AluFlags) == None,
        //      s"Error: unsupported instruction with AluOpKind that uses "
        //      + s"ALU flags. Consider using an `AluOpKind.Sub` that "
        //      + s"becomes a `Cmp` instruction instead: "
        //      + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //    )
        //  }
        //}
        aluOpInfoMap += (idx -> opInfo)
      }
      case OpSelect.MultiCycle => {
        assert(
          opInfo.findValidArgs(opInfo.multiCycleOp.get) != None,
          s"Error: unsupported combination or "
          + s"number of destination/source operands: "
          + s"opInfo(${opInfo}), instructionIndex:${idx}"
        )
        //assert(
        //  opInfo.dstArr.find(_ == DstKind.Pc) == None,
        //  s"Error: unsupported PC as destination of a multi-cycle "
        //  + s"instruction: "
        //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //)
        multiCycleOpInfoMap += (idx -> opInfo)
      }
      //case OpSelect.Load => {
      //  assert(
      //    opInfo.findValidArgs(opInfo.loadOp.get) != None,
      //    s"Error: unsupported combination or "
      //    + s"number of destination/source operands: "
      //    + s"opInfo(${opInfo}), instructionIndex:${idx}"
      //  )
      //  //assert(
      //  //  opInfo.dstArr.find(_ == DstKind.Pc) == None,
      //  //  s"Error: unsupported PC as destination of a load instruction: "
      //  //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
      //  //)
      //  loadOpInfoMap += (idx -> opInfo)
      //}
      //case OpSelect.Store => {
      //  assert(
      //    opInfo.findValidArgs(opInfo.storeOp.get) != None,
      //    s"Error: unsupported combination or "
      //    + s"number of destination/source operands: "
      //    + s"opInfo(${opInfo}), instructionIndex:${idx}"
      //  )
      //  //assert(
      //  //  opInfo.dstArr.find(_ == DstKind.Pc) == None,
      //  //  s"Error: unsupported PC as destination of a store instruction: "
      //  //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
      //  //)
      //  storeOpInfoMap += (idx -> opInfo)
      //}
    }
  }
  val havePsExStall = (
    multiCycleOpInfoMap.size > 0
  )
  val havePsMemStall = (
    loadOpInfoMap.size > 0
    || storeOpInfoMap.size > 0
  )
  //def optFormal: Boolean = psDecode.optFormal
  def mainWidth = shRegFileCfg.mainWidth
  def regFileWordCountArr = shRegFileCfg.wordCountArr
  def regFileModRdPortCnt = shRegFileCfg.modRdPortCnt
  def regFileModStageCnt = shRegFileCfg.modStageCnt
  def regFilePipeName = shRegFileCfg.pipeName
  val regFileCfg = PipeMemRmwConfig[UInt, Bool](
    wordType=UInt(mainWidth bits),
    wordCountArr=regFileWordCountArr,
    hazardCmpType=Bool(),
    modRdPortCnt=regFileModRdPortCnt,
    modStageCnt=regFileModStageCnt,
    pipeName=regFilePipeName,
    //linkArr=linkArr
    optDualRd=(
      false
    ),
    initBigInt={
      val myInitBigInt = ArrayBuffer[ArrayBuffer[BigInt]]()
      for ((wordCount, jdx) <- regFileWordCountArr.view.zipWithIndex) {
        val tempArr = ArrayBuffer[BigInt]()
        for (idx <- 0 until wordCount) {
          val toAdd: Int = 0x0
          tempArr += toAdd
        }
        myInitBigInt += tempArr
      }
      Some(myInitBigInt)
    },
    optModHazardKind=PipeMemRmw.ModHazardKind.Fwd,
    optFormal=optFormal,
  )
  regFileCfg.linkArr = None
}

case class SnowHouseRegFileModType[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Bundle with PipeMemRmwPayloadBase[UInt, Bool] {
  val opCnt = UInt(cfg.modOpCntWidth bits)
  val op = UInt(log2Up(cfg.opInfoMap.size) bits)
  def myHaveFormalFwd = (
    cfg.optFormal
  )
  def mkOneExt(ydx: Int) = (
    PipeMemRmwPayloadExt(
      cfg=cfg.regFileCfg,
      wordCount=cfg.regFileCfg.wordCountArr(ydx),
    )
  )
  //val myExt = Vec.fill(cfg.regFileCfg.memArrSize)(
  //  mkOneExt()
  //)
  val myExt = Vec[PipeMemRmwPayloadExt[UInt, Bool]]{
    val tempArr = ArrayBuffer[PipeMemRmwPayloadExt[UInt, Bool]]()
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      tempArr += mkOneExt(ydx=ydx)
    }
    tempArr
  }
  val myFwd = (
    myHaveFormalFwd
  ) generate (
    PipeMemRmwFwd[UInt, Bool](
      cfg=cfg.regFileCfg,
    )
  )
  def setPipeMemRmwExt(
    inpExt: PipeMemRmwPayloadExt[UInt, Bool],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
    myExt(ydx) := inpExt
  }
  def getPipeMemRmwExt(
    outpExt: PipeMemRmwPayloadExt[UInt, Bool],
      // this is essentially a return value
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
    outpExt := myExt(ydx)
  }
  def formalSetPipeMemRmwFwd(
    inpFwd: PipeMemRmwFwd[UInt, Bool],
    memArrIdx: Int,
  ): Unit = {
    assert(
      myHaveFormalFwd
    )
    myFwd := inpFwd
  }
  def formalGetPipeMemRmwFwd(
    outpFwd: PipeMemRmwFwd[UInt, Bool],
    memArrIdx: Int,
  ): Unit = {
    assert(
      myHaveFormalFwd
    )
    outpFwd := myFwd
  }
}
