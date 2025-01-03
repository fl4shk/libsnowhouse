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
  private val optHowToSlice: Option[Seq[LinkedHashSet[Int]]],
  //linkArr: Option[ArrayBuffer[Link]]=None,
) {
  assert(
    wordCountArr.size > 0,
    s"wordCountArr.size (${wordCountArr.size}) must be greater than zero"
  )
  for ((wordCount, wordCountIdx) <- wordCountArr.view.zipWithIndex) {
    assert(
      wordCount > 0,
      s"wordCount (value:${wordCount} index:${wordCountIdx}) "
      + s"must be greater than zero"
    )
  }
  assert(
    modRdPortCnt >= 2
    && modRdPortCnt <= 3,
    s"modRdPortCnt (${modRdPortCnt}) must be >= 2 and <= 3 "
    + s"(different number of GPRs per instruction not yet supported)"
  )
  val modStageCnt: Int = 1
  val howToSlice: Seq[LinkedHashSet[Int]] = (
    optHowToSlice match {
      case Some(optHowToSlice) => {
        //var cnt: Int = 0
        assert(
          optHowToSlice.size == wordCountArr.size,
          s"howToSlice.size (${optHowToSlice.size}) "
          + s"is not equal to wordCountArr.size (${wordCountArr.size})"
        )
        val wordCountSum: Int = {
          var tempSum: Int = 0
          for (wordCount <- wordCountArr.view) {
            tempSum += wordCount
          }
          tempSum
        }
        val foundSet = LinkedHashSet[Int]()
        for (sliceHowSet <- optHowToSlice.view) {
          for (sliceHow: Int <- sliceHowSet.view) {
            assert (
              sliceHow >= 0
              && sliceHow < wordCountSum,
              s"sliceHow of ${sliceHow} is outside valid range of "
              + s"(0 inclusive, ${wordCountSum} exclusive)"
            )
            assert(
              !foundSet.contains(sliceHow),
              s"duplicate sliceHow: ${sliceHow}"
            )
            foundSet += sliceHow
          }
        }
        optHowToSlice
      }
      case None => {
        //(false, null)
        val howToSlice = ArrayBuffer[LinkedHashSet[Int]]()
        var cnt: Int = 0
        for (wordCount <- wordCountArr.view) {
          val tempSet = LinkedHashSet[Int]()
          for (tempCnt <- 0 until wordCount) {
            tempSet += wordCount - 1 - cnt
            //tempSet += cnt
            cnt += 1
          }
          howToSlice += tempSet
        }
        howToSlice
      }
    }
  )
}

//case class SnowHouseRegFileSliceInfo(
//  shRegFileCfg: SnowHouseRegFileConfig,
//) {
//}
case class SnowHouseConfig(
  //encInstrType: HardType,
  //gprFileDepth: Int,
  //sprFileDepth: Int,
  instrMainWidth: Int,
  shRegFileCfg: SnowHouseRegFileConfig,
  opInfoMap: LinkedHashMap[Any, OpInfo],
  //decodeFunc: (
  //  SnowHouseIo, // io
  //  CtrlLink,               // cId
  //  UInt,                   // output the decoded instruction
  //) => Area,                
  //--------
  //psDecode: SnowHousePipeStageInstrDecode,
  //mkPipeStageInstrDecode: (
  //  SnowHousePipeStageArgs,           // args
  //  Bool,                             // psIdHaltIt
  //  Flow[SnowHousePsExSetPcPayload],  // psExSetPc
  //) => SnowHousePipeStageInstrDecode,
  doInstrDecodeFunc: (SnowHousePipeStageInstrDecode) => Area,
  //--------
  optFormal: Boolean,
  //maxNumGprsPerInstr: Int,
  //modOpCntWidth: Int=8,
  instrCntWidth: Int=8,
) {
  // TODO: support more than 3 general purpose registers per instruction
  // (probably going up to 4 or 5 or something at max?)
  val maxNumGprsPerInstr = regFileModRdPortCnt
  assert(
    //4 >= (1 << instrCntWidth),
    instrCntWidth >= 4,
    s"instrCntWidth (${instrCntWidth}) must be at least 4"
  )
  //--------
  //psDecode.args = Some(SnowHousePipeStageArgs(
  //  cfg=this,
  //  opInfoMap=opInfoMap,
  //))
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
  def numGprs = regFileCfg.wordCountSum
  //--------
  val pureCpyOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val pureCpyuiOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val pureJmpOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val pureBrOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //val cpyOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val aluOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val multiCycleOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //val loadOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //val storeOpInfoMap = LinkedHashMap[Int, OpInfo]()
  ////val atomicRmwOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val memAccOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //--------
  for (((_, opInfo), idx) <- opInfoMap.view.zipWithIndex) {
    opInfo.memAccess match {
      case MemAccessKind.NoMemAccess => {
      }
      case MemAccessKind.Mem(isSigned, isStore, accSize) => {
        isStore match {
          case Some(isStore) => {
            //if (!isStore) {
            //  loadOpInfoMap += (idx -> opInfo)
            //} else { // if (isStore)
            //  storeOpInfoMap += (idx -> opInfo)
            //}
            memAccOpInfoMap += (idx -> opInfo)
          }
          case None => {
            assert(
              false,
              s"Error: Atomic operations are not yet implemented: "
              + s"opInfo(${opInfo}), instructionIndex:${idx}"
            )
            false
          }
        }
        //accSize match {
        //  case MemAccessSize.Sz8 => {
        //  }
        //  case MemAccessSize.Sz16 => {
        //  }
        //  case MemAccessSize.Sz32 => {
        //  }
        //  case MemAccessSize.Sz64 => {
        //  }
        //}
      }
    }
    def checkValidArgs[OpKind](op: Option[OpKindBase]): Unit = {
      op match {
        case Some(myGet) => {
          opInfo.findValidArgs(myGet) match {
            case Some(validArgs) => {
              assert(
                //validArgs.cond.contains(opInfo.cond),
                validArgs.cond.size > 0,
                s"Error: This `OpKindBase` is not yet implemented: "
                + s"opInfo(${opInfo}), instructionIndex:${idx}"
              )
              assert(
                validArgs.cond.contains(opInfo.cond),
                s"Error: unsupported condition: "
                + s"opInfo(${opInfo}), instructionIndex:${idx}"
              )
            }
            case None => {
              assert(
                false,
                s"Error: unsupported combination or "
                + s"number of destination/source operands: "
                + s"opInfo(${opInfo}), instructionIndex:${idx}"
              )
            }
          }
        }
        case None => {
          assert(
            false,
            s"debug: ${opInfo.select} ${idx}"
          )
        }
      }
    }
    opInfo.select match {
      case OpSelect.Cpy => {
        checkValidArgs(opInfo.cpyOp)
        //opInfo.findValidArgs(opInfo.cpyOp.get) match {
        //  case Some(validArgs) => {
        //  }
        //  case None => {
        //    assert(
        //      false,
        //      s"Error: unsupported combination or "
        //      + s"number of destination/source operands: "
        //      + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //    )
        //  }
        //}
        opInfo.cpyOp.get match {
          case CpyOpKind.Cpy => {
            //assert(
            //  opInfo.dstArr.find(_ == DstKind.Pc) == None,
            //  s"Error: unsupported PC as destination of a CpyOpKind.Cpy "
            //  + s"instruction: "
            //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
            //)
            pureCpyOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.Cpyui => {
            //assert(
            //  opInfo.dstArr.find(_ == DstKind.Pc) == None,
            //  s"Error: unsupported PC as destination of a CpyOpKind.Cpyui "
            //  + s"instruction: "
            //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
            //)
            pureCpyuiOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.Jmp => { // non-relative jumps
            //assert(
            //  opInfo.dstArr.find(_ == DstKind.Pc) != None,
            //  s"Error: unsupported lack of PC as (any) destination of a "
            //  + s"CpyOpKind.Jmp "
            //  + s"instruction: "
            //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
            //)
            pureJmpOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.Br => { // relative branches
            pureBrOpInfoMap += (idx -> opInfo)
          }
        }
      }
      case OpSelect.Alu => {
        checkValidArgs(opInfo.aluOp)
        //assert(
        //  opInfo.findValidArgs(opInfo.aluOp.get) != None,
        //  s"Error: unsupported combination or "
        //  + s"number of destination/source operands: "
        //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //)
        aluOpInfoMap += (idx -> opInfo)
      }
      case OpSelect.MultiCycle => {
        checkValidArgs(opInfo.multiCycleOp)
        //assert(
        //  opInfo.findValidArgs(opInfo.multiCycleOp.get) != None,
        //  s"Error: unsupported combination or "
        //  + s"number of destination/source operands: "
        //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //)
        multiCycleOpInfoMap += (idx -> opInfo)
      }
    }
  }
  val havePsExStall = (
    multiCycleOpInfoMap.size > 0
  )
  val havePsMemStall = (
    memAccOpInfoMap.size > 0
    //loadOpInfoMap.size > 0
    //|| storeOpInfoMap.size > 0
    ////|| atomicRmwOpInfoMap.size > 0
  )
  //def optFormal: Boolean = psDecode.optFormal
}

//object SnowHouseFormalInstrCnt {
//  def cntWidth = 8
//}
case class SnowHouseInstrCnt(
  cfg: SnowHouseConfig,
) extends Bundle {
  val any = UInt(cfg.instrCntWidth bits)
  val fwd = UInt(cfg.instrCntWidth bits)
  val jmp = UInt(cfg.instrCntWidth bits)
  val shouldIgnoreInstr = Bool()
  //def shouldIgnoreInstr = (pcChangeState === True)
}
object SnowHouseDecodeExtLdStKind
extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    Load,
    Store
    = newElement()
}
case class SnowHouseDecodeExt(
  cfg: SnowHouseConfig
) extends Bundle {
  //--------
  private val _opIsMemAccessIdx = 0
  private val _opIsCpyNonJmpAluIdx = 1
  private val _opIsJmpIdx = 2
  private val _opIsMultiCycleIdx = 3
  val opIsLim = 4
  val opIs = /*out*/(UInt(opIsLim bits))
  //val memAccessLdStKind = SnowHouseDecodeExtLdStKind() //Bool()
  //def memAccessIsLoad = (
  //  memAccessLdStKind === SnowHouseDecodeExtLdStKind.Load
  //)
  //def memAccessIsStore = (
  //  memAccessLdStKind === SnowHouseDecodeExtLdStKind.Store
  //)
  val memAccessKind = SnowHouseMemAccessKind()
  // TODO: add support for atomic operations
  // (probably just read-modify-write)
  //val memAccessIsAtomic = Bool()
  //--------
  def opIsMemAccess = opIs(_opIsMemAccessIdx)
  def opIsCpyNonJmpAlu = opIs(_opIsCpyNonJmpAluIdx)
  def opIsJmp = opIs(_opIsJmpIdx)
  def opIsMultiCycle = opIs(_opIsMultiCycleIdx)
  //--------
}
case class SnowHouseGprIdxToMemAddrIdxMapElem(
  cfg: SnowHouseConfig
) extends Bundle {
  val idx = UInt(log2Up(cfg.numGprs) bits)
  val haveHowToSetIdx = (
    cfg.shRegFileCfg.howToSlice.size > 1
  )
  val howToSetIdx = (
    haveHowToSetIdx
  ) generate (
    UInt(
      (log2Up(cfg.shRegFileCfg.howToSlice.size).max(1))
      bits
    )
  )
}
case class SnowHousePipePayload(
  cfg: SnowHouseConfig,
) extends Bundle with PipeMemRmwPayloadBase[UInt, Bool] {
  def myHaveFormalFwd = (
    cfg.optFormal
  )
  val decodeExt = SnowHouseDecodeExt(cfg=cfg)
  val instrCnt = SnowHouseInstrCnt(cfg=cfg)
  //val opCnt = UInt(cfg.instrCntWidth bits)
  def opCnt = instrCnt.any
  val op = UInt(log2Up(cfg.opInfoMap.size) bits)
  def formalAssumes() = new Area {
    if (cfg.optFormal) {
      if ((1 << op.getWidth) != cfg.opInfoMap.size) {
        assert(
          // if I did my math right, this `assert` should never fire...
          (1 << op.getWidth) > cfg.opInfoMap.size,
          s"Eek! "
          + s"${op.getWidth} ${1 << op.getWidth} ${cfg.opInfoMap.size}"
        )
        assume(
          //Cat(False, op).asUInt
          op//.resized
          < U"${op.getWidth}'d${cfg.opInfoMap.size}"
        )
      }
    }
  }
  val myExt = Vec[PipeMemRmwPayloadExt[UInt, Bool]]{
    val myArr = ArrayBuffer[PipeMemRmwPayloadExt[UInt, Bool]]()
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      myArr += mkOneExt(ydx=ydx)
    }
    myArr
  }
  // `gprIdxVec` is to be driven by the class derived from
  // `SnowHousePipeStageInstrDecode`
  val gprIdxVec = Vec.fill(cfg.maxNumGprsPerInstr)(
    UInt(log2Up(cfg.numGprs) bits)
  )
  val gprIdxToMemAddrIdxMap = Vec[SnowHouseGprIdxToMemAddrIdxMapElem]({
    val myArr = ArrayBuffer[SnowHouseGprIdxToMemAddrIdxMapElem]()
    for (zdx <- 0 until cfg.maxNumGprsPerInstr) {
      myArr += SnowHouseGprIdxToMemAddrIdxMapElem(cfg=cfg)
    }
    myArr
  })
  val gprRdMemWordVec = Vec.fill(cfg.regFileModRdPortCnt)(
    UInt(cfg.mainWidth bits)
  )
  val regPc = UInt(cfg.mainWidth bits)
  val regPcPlusImm = UInt(cfg.mainWidth bits)
  val imm = UInt(cfg.mainWidth bits)
  //val op = UInt(log2Up(cfg.opInfoMap.size) bits)
  def mkOneExt(ydx: Int) = (
    PipeMemRmwPayloadExt(
      cfg=cfg.regFileCfg,
      wordCount=cfg.regFileCfg.wordCountArr(ydx),
    )
  )
  //val myExt = Vec.fill(cfg.regFileCfg.memArrSize)(
  //  mkOneExt()
  //)
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
    myExt(ydx).pipeFlags := inpExt.pipeFlags
    myExt(ydx).main.nonMemAddr := inpExt.main.nonMemAddr
    for ((myMemAddr, zdx) <- myExt(ydx).main.memAddr.view.zipWithIndex) {
      myMemAddr := inpExt.main.memAddr(zdx).resized
    }
  }
  def getPipeMemRmwExt(
    outpExt: PipeMemRmwPayloadExt[UInt, Bool],
      // this is essentially a return value
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
    //outpExt := myExt(ydx)
    outpExt.pipeFlags := myExt(ydx).pipeFlags
    outpExt.main.nonMemAddr := myExt(ydx).main.nonMemAddr
    for ((myMemAddr, zdx) <- myExt(ydx).main.memAddr.view.zipWithIndex) {
      outpExt.main.memAddr(zdx) := myMemAddr.resized
      //(
      //  outpExt.main.memAddr(zdx).bitsRange
      //)
    }
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
