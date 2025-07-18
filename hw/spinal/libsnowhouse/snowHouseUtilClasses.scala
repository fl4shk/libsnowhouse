package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.bus.tilelink
import spinal.core.fiber.Fiber

import libcheesevoyage.general._
import libcheesevoyage.math._
import libcheesevoyage.bus.lcvStall._

case class SnowHouseRegFileConfig(
  mainWidth: Int,
  wordCountArr: Seq[Int],
  modRdPortCnt: Int,
  pipeName: String,
  private val optHowToSlice: Option[Seq[LinkedHashSet[Int]]],
  memRamStyle: String="auto",
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
sealed trait SnowHouseIrqConfig {
  private[libsnowhouse] def _doBlockIrqCntMax: Option[Int]
  private[libsnowhouse] def _doBlockIrqCntWidth: Option[Int] = (
    _doBlockIrqCntMax match {
      case Some(myBlockIrqCntMax) => {
        Some(log2Up(myBlockIrqCntMax) + 1)
      }
      case None => {
        None
      }
    }
  )
  //private[libsnowhouse] def _doBlockIrqCntWidth: Option[Int]
  //def numIrqs: Int
  //private[libsnowhouse] def _allowIrqStorm: Boolean
}
object SnowHouseIrqConfig {
  case class IraIds(
    ////val iraRegIdx: Int,
    ////val idsRegIdx: Int,
    ////val allowNestedIrqs: Boolean,
    //val allowIrqStorm: Boolean,
    ////val numIrqs: Int
    //val doBlockIrqCntWidthMinus1: Option[Int]=None,
    val doBlockIrqCntMax: Option[Int]=None,
  ) extends SnowHouseIrqConfig {
    // TODO: possibly support multiple IRQ lines?
    //def _allowIrqStorm: Boolean = allowIrqStorm
    private[libsnowhouse] def _doBlockIrqCntMax: Option[Int] = (
      doBlockIrqCntMax
    )
  }
  //case class Vector(
  //  val numIrqs: Int,
  //) extends SnowHouseIrqConfig {
  //}
}
//case class SnowHouseIrqConfig(
//  numIrqs: Int
//) {
//  assert(
//    numIrqs > 0,
//    s"numIrqs (${numIrqs}) must be greater than 0.",
//  )
//}
case class SnowHouseCacheConfig(
  addrWidth: Int,
  wordWidth: Int,
  //maxWordWidth: Int,
  lineSizeBytes: Int,
  depthWords: Int, // this is in number of words
  //srcWidth: Int,
  srcId: Int,
  totalNumBusHosts: Int,
  isIcache: Boolean,
  //var srcStart: Int=0,
  memRamStyle: String="auto",
  var bridgeCfg: LcvStallToTilelinkConfig=null,
) {
  //--------
  assert(
    addrWidth == (1 << log2Up(addrWidth)),
    s"addrWidth: need power of two: "
    + s"${addrWidth} != ${(1 << log2Up(addrWidth))}"
  )
  assert(
    addrWidth == (addrWidth / 8).toInt * 8,
    s"addrWidth: need multiple of 8: "
    + s"${addrWidth} != ${(addrWidth / 8).toInt * 8}"
  )
  assert(
    wordWidth == (1 << log2Up(wordWidth)),
    s"wordWidth: need power of two: "
    + s"${wordWidth} != ${(1 << log2Up(wordWidth))}"
  )
  assert(
    wordWidth == (wordWidth / 8).toInt * 8,
    s"wordWidth: need multiple of 8: "
    + s"${wordWidth} != ${(wordWidth / 8).toInt * 8}"
  )
  assert(
    lineSizeBytes == (1 << log2Up(lineSizeBytes)),
    s"lineSizeBytes: need power of two: "
    + s"${lineSizeBytes} != ${(1 << log2Up(lineSizeBytes))}"
  )
  assert(
    depthWords == (1 << log2Up(depthWords)),
    s"depthWords: need power of two: "
    + s"${depthWords} != ${(1 << log2Up(depthWords))}"
  )
  //--------
  val wordSizeBytes = wordWidth / 8
  val depthBytes = depthWords * wordSizeBytes
  val depthLines = (
    // number of cache lines
    depthBytes / lineSizeBytes
  )
  val numWordsPerLine = (
    lineSizeBytes / wordSizeBytes
  )
  val tagWidth = (
    //addrWidth - log2Up(depthBytes)
    //tag bits = addr bits - index bits - offset bits
    //index bits = log2(lines)
    //offset bits = log2(words per line)
    //(assuming your addresses are word-based ofc) (edited)
    addrWidth - log2Up(depthLines) - log2Up(numWordsPerLine) - 1
  )
  val tagRange = (
    addrWidth - 2 downto (addrWidth - 1 - tagWidth)
  )
  val nonCachedRange = (
    addrWidth - 1 downto addrWidth - 1
  )
  println(
    s"isIcache:${isIcache}: "
    + s"tagWidth:${tagWidth} "
    + s"tagRange:${tagRange} "
    + s"nonCachedRange:${nonCachedRange}"
  )
  //--------
  //--------
  // TileLink stuff follows
  val m2sTransfers = tilelink.M2sTransfers(
    get=tilelink.SizeRange(1, wordWidth),
    putFull=(
      if (isIcache) (
        tilelink.SizeRange.none
      ) else (
        tilelink.SizeRange(1, wordWidth)
      )
    ),
  )
  val m2sCfg = {
    tilelink.M2sParameters(
      support=tilelink.M2sSupport(
        transfers=m2sTransfers,
        addressWidth=addrWidth,
        dataWidth=wordWidth,
      ),
      sourceCount=totalNumBusHosts,
    )
  }
  //val tlSrcBase = (
  //  log2Up(numWordsPerLine)
  //)
  //def m2sCfg(
  //  masters: Seq[tilelink.M2sAgent]
  //): tilelink.M2sParameters = {
  //  tilelink.M2sParameters(
  //    addressWidth=addrWidth,
  //    dataWidth=mainWidth,
  //    masters=masters,
  //  )
  //}
  //def m2sAgentSeq(
  //): tilelink.
  //val addrMapping = spinal.lib.bus.misc.SizeMapping(
  //  base=0x0,
  //)
  //def addrMapping = 
  private[libsnowhouse] def _mkBridgeCfg(
    srcWidth: Int
  ) = {
    LcvStallToTilelinkConfig(
      addrWidth=(
        //addrWidth
        //16
        14
      ),
      dataWidth=wordWidth,
      sizeBytes=(
        //(wordWidth / 8)
        lineSizeBytes
      ),
      srcWidth=(
        srcWidth
        //log2Up(totalNumBusHosts)
      ),
      isDual=true,
    )
  }
}
case class SnowHouseSubConfig(
  instrMainWidth: Int,
  shRegFileCfg: SnowHouseRegFileConfig,
  //haveIcache: Boolean=false,
  //--------
  //srcWidth: Int=1,
  haveIcache: Boolean=true,
  icacheDepth: Int=8192,
  icacheLineSizeBytes: Int=64,
  icacheBusSrcNum: Int=0,
  icacheMemRamStyle: String="auto",
  //--------
  haveDcache: Boolean=true,
  dcacheDepth: Int=8192,
  dcacheLineSizeBytes: Int=64,
  dcacheBusSrcNum: Int=1,
  dcacheMemRamStyle: String="auto",
  //--------
  totalNumBusHosts: Int=2,
  optCacheBusSrcWidth: Option[Int]=None,
  //--------
) {
  //val haveIcache = icacheDepth > 0
  //val haveDcache = dcacheDepth > 0
  //val cacheMaxWordWidth = (
  //  log2Up(icacheCfg.numWordsPerLine)
  //  .max(log2Up(dcacheCfg.numWordsPerLine))
  //)
  val icacheCfg = (
    haveIcache
  ) generate (
    SnowHouseCacheConfig(
      addrWidth=shRegFileCfg.mainWidth,
      wordWidth=instrMainWidth,
      lineSizeBytes=icacheLineSizeBytes,
      depthWords=icacheDepth,
      //srcWidth=srcWidth,
      srcId=icacheBusSrcNum,
      totalNumBusHosts=totalNumBusHosts,
      memRamStyle=icacheMemRamStyle,
      isIcache=true,
    )
  )
  val dcacheCfg = (
    haveDcache
  ) generate (
    SnowHouseCacheConfig(
      addrWidth=shRegFileCfg.mainWidth,
      wordWidth=shRegFileCfg.mainWidth,
      lineSizeBytes=dcacheLineSizeBytes,
      depthWords=dcacheDepth,
      //srcWidth=srcWidth,
      srcId=dcacheBusSrcNum,
      totalNumBusHosts=totalNumBusHosts,
      memRamStyle=dcacheMemRamStyle,
      isIcache=false,
    )
  )
  val cacheBusSrcWidth = (
    optCacheBusSrcWidth match {
      case Some(cacheBusSrcWidth) => {
        cacheBusSrcWidth
      }
      case None => {
        //assert(
        //  totalNumBusHosts == 2,
        //  s"totalNumBusHosts:${totalNumBusHosts} must be 2 in this case"
        //)
        //(
        //  log2Up(icacheCfg.numWordsPerLine)
        //  .max(log2Up(dcacheCfg.numWordsPerLine))
        //) * (
          log2Up(totalNumBusHosts)
        //)
      }
    }
  )
  //icacheCfg.srcStart = (
  //  //(
  //  //  log2Up(icacheCfg.numWordsPerLine)
  //  //  .max(log2Up(dcacheCfg.numWordsPerLine))
  //  //) * (
  //    icacheCfg.srcNum
  //  //)
  //)
  //dcacheCfg.srcStart = (
  //  //(
  //  //  log2Up(icacheCfg.numWordsPerLine)
  //  //  .max(log2Up(dcacheCfg.numWordsPerLine))
  //  //) * (
  //    dcacheCfg.srcNum
  //  //)
  //)
  icacheCfg.bridgeCfg = (
    icacheCfg._mkBridgeCfg(
      srcWidth=cacheBusSrcWidth
    )
    //LcvStallToTilelinkConfig(
    //  addrWidth=icacheCfg.addrWidth,
    //  dataWidth=icacheCfg.wordWidth,
    //  sizeBytes=(icacheCfg.wordWidth / 8),
    //  srcWidth=cacheBusSrcWidth,
    //  isDual=true,
    //)
  )
  dcacheCfg.bridgeCfg = (
    dcacheCfg._mkBridgeCfg(
      srcWidth=cacheBusSrcWidth
    )
    //LcvStallToTilelinkConfig(
    //  addrWidth=dcacheCfg.addrWidth,
    //  dataWidth=dcacheCfg.wordWidth,
    //  sizeBytes=(dcacheCfg.wordWidth / 8),
    //  srcWidth=cacheBusSrcWidth,
    //  isDual=true,
    //)
  )
  //val icacheBridgeCfg = (
  //  haveIcache
  //) generate (
  //  LcvStallToTilelinkConfig(
  //    addrWidth=log2Up(icacheDepth),
  //    dataWidth=instrMainWidth,
  //    sizeBytes=instrMainWidth / 8,
  //    srcWidth=srcWidth,
  //  )
  //)
  //val dcacheBridgeCfg = (
  //  haveDcache
  //) generate (
  //  LcvStallToTilelinkConfig(
  //    addrWidth=log2Up(dcacheDepth),
  //    dataWidth=shRegFileCfg.mainWidth,
  //    sizeBytes=shRegFileCfg.mainWidth / 8,
  //    srcWidth=srcWidth,
  //  )
  //)
}

sealed trait SnowHouseBranchPredictorKind {
  //private[libsnowhouse] def _doHaveBranchInstr(
  //  mainWidth: Int,
  //  encInstr: UInt,
  //  upIsFiring: Bool,
  //): BranchTgtBufElem
  private[libsnowhouse] def _branchKindEnumWidth: Int
  private[libsnowhouse] def _branchTgtBufSizeLog2: Int
  private[libsnowhouse] def _branchTgtBufSize: Int = (
    1 << _branchTgtBufSizeLog2
  )
}
object SnowHouseBranchPredictorKind {
  //def branchKindEnumMaxWidth = (
  //  // subject to change
  //  4
  //)
  //case class AssumeTkn(
  //  val doHaveBranchInstr: (UInt) => UInt
  //) extends SnowHouseBranchPredictorKind {
  //  def _doHaveBranchInstr(
  //    encInstr: UInt,
  //  ): UInt = (
  //    doHaveBranchInstr(encInstr)
  //  )
  //}
  object FwdNotTknBakTknEnum
  extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      //NO_BRANCH,
      //HAVE_BRANCH,
      //HAVE_PRE_BRANCH
      FWD,
      BAK,
      NO_PREDICT
      = newElement()
  }
  case class FwdNotTknBakTkn(
    //val mainWidth: Int,
    //val doHaveBranchInstr: (
    //  Int,
    //  UInt,
    //  Bool,
    //) => BranchTgtBufElem,
    //val cfg: SnowHouseConfig,
    val branchTgtBufSizeLog2: Int,
  ) extends SnowHouseBranchPredictorKind {
    //def _doHaveBranchInstr(
    //  mainWidth: Int,
    //  encInstr: UInt,
    //  upIsFiring: Bool,
    //): BranchTgtBufElem = {
    //  doHaveBranchInstr(
    //    mainWidth,
    //    encInstr,
    //    upIsFiring,
    //  )
    //  //val ret = Flow(Bits(temp.payload.asBits.getWidth bits))
    //  //ret.valid := temp.valid
    //  //ret.payload.assignFromBits(temp.payload.asBits)
    //  //ret
    //}
    def _branchKindEnumWidth: Int = (
      FwdNotTknBakTknEnum().asBits.getWidth
    )
    def _branchTgtBufSizeLog2: Int = (
      branchTgtBufSizeLog2
    )
  }
}

case class SnowHouseConfig(
  haveZeroReg: Option[Int],
  irqCfg: Option[SnowHouseIrqConfig],
  haveAluFlags: Boolean,
  //encInstrType: HardType,
  //gprFileDepth: Int,
  //sprFileDepth: Int,
  //shRegFileCfg: SnowHouseRegFileConfig,
  subCfg: SnowHouseSubConfig,
  opInfoMap: LinkedHashMap[Any, OpInfo],
  irqJmpOp: Int,
  //irqRetIraOp: Int,
  //--------
  doInstrDecodeFunc: (SnowHousePipeStageInstrDecode) => Area,
  optBranchPredictorKind: Option[SnowHouseBranchPredictorKind],
  //--------
  instrRamKind: Int,
  //--------
  //maxNumGprsPerInstr: Int,
  //modOpCntWidth: Int=8,
  supportUcode: Boolean=false, // whether or not to support microcode
  instrCntWidth: Int=(
    //8
    //4
    3
  ),
  //exposeGprsToIo: Option[Seq[Int]]=None,
  exposeRegFileWriteDataToIo: Boolean=false,
  exposeRegFileWriteAddrToIo: Boolean=false,
  exposeRegFileWriteEnableToIo: Boolean=false,
  //splitAluOp: Boolean=false,
  optFormal: Boolean=false,
) {
  def haveBranchPredictor = (
    optBranchPredictorKind != None
  )
  def lowerMyFanout = 4
  def lowerMyFanoutRegPcSetItCnt = (
    //2
    //3
    4
    //5
    //1
  )
  def lowerMyFanoutNextPc = (
    2
  )
  //def lowerMyFanoutDec = multiCycle
  def instrMainWidth = subCfg.instrMainWidth
  def shRegFileCfg = subCfg.shRegFileCfg
  val instrSizeBytes = (instrMainWidth.toLong / 8.toLong).toLong
  val myHaveIrqIdsIra = (
    irqCfg != None
  )
  val myZeroRegIdx: Int = (
    haveZeroReg match {
      case Some(regIdx) => {
        regIdx
      }
      case None => {
        -1
      }
    }
  )
  val myHaveZeroReg = (
    myZeroRegIdx >= 0
  )
  val myHaveAluFlags: Boolean = {
    var found: Boolean = false
    var foundAnyDst: Boolean = false
    var foundAnySrc: Boolean = false
    for (((_, opInfo), opInfoIdx) <- opInfoMap.view.zipWithIndex) {
      val foundDst = opInfo.dstArr.find(_ == DstKind.Spr(SprKind.AluFlags))
      val foundSrc = opInfo.srcArr.find(_ == SrcKind.Spr(SprKind.AluFlags))
      if (foundDst != None) {
        foundAnyDst = true
        found = true
      }
      if (foundSrc != None) {
        foundAnySrc = true
        found = true
      }

    }
    if (foundAnyDst) {
      assert(
        foundAnySrc,
        s"Can't only destinations for AluFlags"
      )
    }
    found
  }
  assert(
    (instrMainWidth / 8) * 8 == instrMainWidth,
    s"instrMainWidth must be a multiple of 8"
  )
  // TODO: support more than 3 general purpose registers per instruction
  // (probably going up to 4 or 5 or something at max?)
  val maxNumGprsPerInstr = regFileModRdPortCnt
  assert(
    //4 >= (1 << instrCntWidth),
    instrCntWidth >= 3,
    s"instrCntWidth (${instrCntWidth}) must be at least 3"
  )
  //--------
  //psDecode.args = Some(SnowHousePipeStageArgs(
  //  cfg=this,
  //  opInfoMap=opInfoMap,
  //))
  def mkLcvStallHost[
    HostDataT <: Data,
    DevDataT <: Data,
  ](
    stallIo: Option[LcvStallIo[
      HostDataT,
      DevDataT,
    ]]
  ) = {
    LcvStallHost[
      HostDataT,
      DevDataT,
    ](
      stallIo=stallIo,
      optFormalJustHost=optFormal,
    )
  }
  def mainWidth = shRegFileCfg.mainWidth
  def regFileWordCountArr = shRegFileCfg.wordCountArr
  def regFileModRdPortCnt = shRegFileCfg.modRdPortCnt
  def regFileModStageCnt = shRegFileCfg.modStageCnt
  def regFilePipeName = shRegFileCfg.pipeName
  def regFileMemRamStyle = shRegFileCfg.memRamStyle
  val regFileCfg = PipeMemRmwConfig[UInt, Bool](
    wordType=UInt(mainWidth bits),
    wordCountArr=regFileWordCountArr,
    hazardCmpType=Bool(),
    modRdPortCnt=regFileModRdPortCnt,
    modStageCnt=regFileModStageCnt,
    pipeName=regFilePipeName,
    optIncludePreMid0Front=false,
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
    memRamStyle=regFileMemRamStyle,
    optModHazardKind=PipeMemRmw.ModHazardKind.Fwd,
    optFwdHaveZeroReg=haveZeroReg,
    optIncludeModFrontS2MLink=false,
    optFormal=optFormal,
    numForkJoin=/*2*/1,
  )
  regFileCfg.linkArr = None
  def numGprs = regFileCfg.wordCountSum
  //--------
  val cpyCpyuiOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //val pureCpyOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val jmpBrOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //val pureJmpOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //val cpyOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val aluOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val aluShiftOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val nonMultiCycleOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val nonMultiCycleNonJmpOpInfoMap = LinkedHashMap[Int, OpInfo]()
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
      case MemAccessKind.Mem(
        isSigned, isStore, isAtomic, accSize
      ) => {
        if (!isAtomic) {
          //if (!isStore) {
          //  loadOpInfoMap += (idx -> opInfo)
          //} else { // if (isStore)
          //  storeOpInfoMap += (idx -> opInfo)
          //}
          memAccOpInfoMap += (idx -> opInfo)
        } else {
          assert(
            false,
            s"Error: Atomic operations are not yet implemented: "
            + s"opInfo(${opInfo}), instructionIndex:${idx}"
          )
          false
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
            case Some(OpInfoValidArgsTuple(validArgs, setIdx)) => {
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
            cpyCpyuiOpInfoMap += (idx -> opInfo)
            nonMultiCycleOpInfoMap += (idx -> opInfo)
            nonMultiCycleNonJmpOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.Cpyu => {
            //assert(
            //  opInfo.dstArr.find(_ == DstKind.Pc) == None,
            //  s"Error: unsupported PC as destination of a CpyOpKind.Cpyui "
            //  + s"instruction: "
            //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
            //)
            //pureCpyuiOpInfoMap += (idx -> opInfo)
            cpyCpyuiOpInfoMap += (idx -> opInfo)
            nonMultiCycleOpInfoMap += (idx -> opInfo)
            nonMultiCycleNonJmpOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.Jmp => { // non-relative jumps
            //assert(
            //  opInfo.dstArr.find(_ == DstKind.Pc) != None,
            //  s"Error: unsupported lack of PC as (any) destination of a "
            //  + s"CpyOpKind.Jmp "
            //  + s"instruction: "
            //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
            //)
            jmpBrOpInfoMap += (idx -> opInfo)
            nonMultiCycleOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.Br => { // relative branches
            //pureBrOpInfoMap += (idx -> opInfo)
            jmpBrOpInfoMap += (idx -> opInfo)
            nonMultiCycleOpInfoMap += (idx -> opInfo)
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
        nonMultiCycleOpInfoMap += (idx -> opInfo)
        nonMultiCycleNonJmpOpInfoMap += (idx -> opInfo)
      }
      case OpSelect.AluShift => {
        checkValidArgs(opInfo.aluShiftOp)
        aluShiftOpInfoMap += (idx -> opInfo)
        nonMultiCycleOpInfoMap += (idx -> opInfo)
        nonMultiCycleNonJmpOpInfoMap += (idx -> opInfo)
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
  val allMainLdstUseGprPlusImm = {
    var found: Boolean = false
    for ((_, cpyOpInfo) <- cpyCpyuiOpInfoMap.view) {
      if (!found) {
        cpyOpInfo.memAccess match {
          case MemAccessKind.NoMemAccess => {
          }
          case mem: MemAccessKind.Mem => {
            if (cpyOpInfo.srcArr.size == 2) {
              cpyOpInfo.srcArr(0) match {
                case SrcKind.Gpr => {
                  cpyOpInfo.srcArr(1) match {
                    case imm: SrcKind.Imm => {
                    }
                    case SrcKind.Gpr => {
                      found = true
                    }
                    case _ => {
                    }
                  }
                }
                case _ => {
                }
              }
            } else {
              found = true
            }
          }
        }
      }
    }
    (!found)
  }
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
  val shouldIgnoreInstr = Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
    Bool()
  )
  //def shouldIgnoreInstr = (pcChangeState === True)
}
//object SnowHouseDecodeExtLdStKind
//extends SpinalEnum(defaultEncoding=binarySequential) {
//  val
//    Load,
//    Store
//    = newElement()
//}
//object SnowHouseDecodeExt
case class SnowHouseDecodeExt(
  cfg: SnowHouseConfig
) extends Bundle {
  //--------
  //private val _opIsMemAccessIdx = 0
  //private val _opIsCpyNonJmpAluIdx = 0
  //private val _opIsAluShiftIdx = 1
  ////private val _opIsJmpIdx = 2
  ////private val _opIsMultiCycleIdx = 4
  //val opIsLim = 2
  //val opIs = /*out*/(UInt(opIsLim bits))

  //val memAccessLdStKind = SnowHouseDecodeExtLdStKind() //Bool()
  //def memAccessIsLoad = (
  //  memAccessLdStKind === SnowHouseDecodeExtLdStKind.Load
  //)
  //def memAccessIsStore = (
  //  memAccessLdStKind === SnowHouseDecodeExtLdStKind.Store
  //)
  val memAccessKind = SnowHouseMemAccessKind()
  val memAccessSubKind = SnowHouseMemAccessSubKind(/*binaryOneHot*/)
  val memAccessIsLtWordWidth = Bool()
  val memAccessIsPush = Bool()
  // TODO: add support for atomic operations
  // (probably just read-modify-write)
  //val memAccessIsAtomic = Bool()
  //--------
  //def opIsMemAccess = opIs(_opIsMemAccessIdx)
  val opIsMemAccess = Vec.fill(
    //cfg.memAccOpInfoMap.size
    1
  )(
    Bool()
  )
  //def opIsCpyNonJmpAlu = opIs(_opIsCpyNonJmpAluIdx)
  //def opIsAluShift = opIs(_opIsAluShiftIdx)
  //def opIsJmp = opIs(_opIsJmpIdx)
  //def opIsMultiCycle = opIs(_opIsMultiCycleIdx)
  val opIsJmp = Bool()
  val opIsAnyMultiCycle = Bool()
  val opIsMultiCycle = Vec.fill(
    cfg.multiCycleOpInfoMap.size
  )(
    Bool()
  )
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
object SnowHouseSplitOpKind
extends SpinalEnum(
  defaultEncoding=(
    binaryOneHot
    //binarySequential
  )
) {
  val
    CPY_CPYUI,
    JMP_BR,
    //PURE_BR,
    ALU,
    ALU_SHIFT,
    MULTI_CYCLE
    //PURE_CPYUI
    = newElement()
}
//object SnowHouseSplitOpAluSrcKind
//extends SpinalEnum(defaultEncoding=binarySequential) {
//  val
//    IMM,
//    GPR
//    //ALU_FLAGS
//    //IDS,
//    //IRA,
//    //IE
//    = newElement()
//}
case class SnowHouseSplitOp(
  cfg: SnowHouseConfig
) extends Bundle {
  //val fullOp = /*Flow*/(
  //  UInt(log2Up(cfg.opInfoMap.size) bits)
  //)
  val opIsMultiCycle = Bool()
  val opIsMemAccess = Bool()
  //val opIsJmp = Bool()
  //val nonMultiCycleOp = /*Flow*/(
  //  UInt(log2Up(cfg.nonMultiCycleOpInfoMap.size + 1) bits)
  //)
  val nonMultiCycleNonJmpOp = /*Flow*/(
    UInt(log2Up(cfg.nonMultiCycleNonJmpOpInfoMap.size + 1) bits)
  )
  val kind = SnowHouseSplitOpKind(
    //binaryOneHot
  )
  val cpyCpyuiOp = /*Flow*/(
    UInt(log2Up(cfg.cpyCpyuiOpInfoMap.size) bits)
  )
  val exSetNextPcKind = (
    SnowHousePsExSetNextPcKind(encoding=binarySequential)
  )
  val jmpBrOp = /*Flow*/(
    UInt(log2Up(cfg.jmpBrOpInfoMap.size + 1) bits)
  )
  //val pureJmpOp = /*Flow*/(
  //  UInt(log2Up(cfg.pureJmpOpInfoMap.size) bits)
  //)
  val aluOp = /*Flow*/(
    UInt(log2Up(cfg.aluOpInfoMap.size) bits)
  )
  val aluShiftOp = /*Flow*/(
    UInt(log2Up(cfg.aluShiftOpInfoMap.size) bits)
  )
  val multiCycleOp = /*Flow*/(
    UInt(/*log2Up*/(cfg.multiCycleOpInfoMap.size) bits)
  )
  //val pureCpyuiOp = /*Flow*/(
  //  UInt(log2Up(cfg.pureCpyOpInfoMap.size) bits)
  //)
  //val aluSrcKindVec = Vec.fill(cfg.numGprs)(
  //  SnowHouseSplitOpAluSrcKind()
  //)
  //val lastAluSrcKind = SnowHouseSplitOpAluSrcKind()
  def setToDefault(): Unit = {
    this := this.getZero
    kind := SnowHouseSplitOpKind.CPY_CPYUI
    opIsMultiCycle := False
    //nonMultiCycleOp := (
    //  (1 << nonMultiCycleOp.getWidth) - 1
    //)
    nonMultiCycleNonJmpOp := (
      (1 << nonMultiCycleNonJmpOp.getWidth) - 1
    )
    multiCycleOp := 0x0
    opIsMemAccess := False
    jmpBrOp := (
      (1 << jmpBrOp.getWidth) - 1
    )
    exSetNextPcKind := (
      //SnowHousePsExSetNextPcKind.PcPlusImm
      //SnowHousePsExSetNextPcKind.PcPlusInstrSize
      SnowHousePsExSetNextPcKind.Dont
    )
  }
}
object SnowHousePsExSetNextPcKind
extends SpinalEnum(defaultEncoding=binaryOneHot) {
  val
    //PcPlusInstrSize,
    Dont,
    PcPlusImm,
    RdMemWord,
    Ira//,
    //Ids
    = newElement()
}
case class SnowHousePipePayload(
  cfg: SnowHouseConfig,
) extends Bundle with PipeMemRmwPayloadBase[UInt, Bool] {
  def myHaveFormalFwd = (
    cfg.optFormal
  )
  val blockIrq = (
    cfg.irqCfg != None
  ) generate (
    Bool()
  )
  val takeIrq = (
    //cfg.irqCfg != None
    true
  ) generate (
    Bool()
  )
  val branchPredictTkn = (
    cfg.haveBranchPredictor
  ) generate (
    // whether the branch predictor predicted the branch would be taken.
    // This is to be checked in EX,
    // and if the predictor was correct,
    // we assert `psExSetPc.valid`,
    // but otherwise we deassert `psExSetPc.valid`.
    //
    // If the predictor was wrong, we can run the the existing code from
    // when there was only an "assume not not taken" branch predictor.
    // As such we will effectively be transforming branch mispredicts 
    // into a pretend "taken jump" from the old "assume not taken" branch
    // predictor. This enables us to have reuse that old code.
    Bool()
  )
  val encInstr = Flow(UInt(cfg.instrMainWidth bits))
  val branchTgtBufElem = Vec.fill(2)(
    BranchTgtBufElem(cfg=cfg)
  )
  val inpDecodeExt = (
    Vec.fill(2)(
      SnowHouseDecodeExt(cfg=cfg)
    )
  )
  val outpDecodeExt = SnowHouseDecodeExt(cfg=cfg) //simPublic()
  val instrCnt = SnowHouseInstrCnt(cfg=cfg) //simPublic()
  //val opCnt = UInt(cfg.instrCntWidth bits)
  def opCnt = instrCnt.any
  val op = UInt(log2Up(cfg.opInfoMap.size) bits) //simPublic()
  val splitOp = SnowHouseSplitOp(cfg=cfg)
  val myDoHaveHazardAddrCheckVec = Vec.fill(1)(Bool())

  val irqJmpOp = UInt(log2Up(cfg.opInfoMap.size) bits)
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
          < U(s"${op.getWidth}'d${cfg.opInfoMap.size}")
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
  } //simPublic()
  // `gprIdxVec` is to be driven by the class derived from
  // `SnowHousePipeStageInstrDecode`
  val gprIdxVec = Vec.fill(cfg.maxNumGprsPerInstr)(
    UInt(log2Up(cfg.numGprs) bits)
  ) //simPublic()
  val gprIsZeroVec = Vec.fill(cfg.maxNumGprsPerInstr)(
    Vec.fill(cfg.regFileCfg.modMemWordValidSize)(
      Bool()
    )
  )
  val gprIsNonZeroVec = Vec.fill(cfg.maxNumGprsPerInstr)(
    Vec.fill(cfg.regFileCfg.modMemWordValidSize)(
      Bool()
    )
  )
  val gprIdxToMemAddrIdxMap = Vec[SnowHouseGprIdxToMemAddrIdxMapElem]({
    val myArr = ArrayBuffer[SnowHouseGprIdxToMemAddrIdxMapElem]()
    for (zdx <- 0 until cfg.maxNumGprsPerInstr) {
      myArr += SnowHouseGprIdxToMemAddrIdxMapElem(cfg=cfg)
    }
    myArr
  })
  //val gprRdMemWordVec = (
  //  !cfg.optFormal
  //) generate (
  //  Vec.fill(cfg.regFileModRdPortCnt)(
  //    UInt(cfg.mainWidth bits)
  //  )
  //)
  // TODO: add support for writing multiple GPRs
  //def formalGprModMemWordSize = (
  //  //8 
  //  1
  //)
  //val formalGprModMemWord = (
  //  cfg.optFormal
  //) generate (
  //  Vec.fill(formalGprModMemWordSize)(
  //    UInt(cfg.mainWidth bits)
  //  )
  //)
  val psExSetPc = (
    //cfg.optFormal
    true
  ) generate (
    Flow(SnowHousePsExSetPcPayload(cfg=cfg)) //simPublic()
  )
  val psExSetOutpModMemWordIo = (
    //cfg.optFormal
    true
  ) generate (
    SnowHousePipeStageExecuteSetOutpModMemWordIo(cfg=cfg) //simPublic()
  )
  //psExSetOutpModMemWordIo.simPublic()
  val regPc = UInt(cfg.mainWidth bits)//.simPublic()
  val regPcPlus1Instr = UInt(cfg.mainWidth bits)
  val psIfRegPcSetItCnt = UInt(2 bits)
  val regPcSetItCnt = Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
    UInt(
      //cfg.instrCntWidth bits
      1 bits
    ) //Bool()
  )
  val regPcPlusInstrSize = UInt(cfg.mainWidth bits)
  val regPcPlusImm = UInt(cfg.mainWidth bits)//.simPublic()
  val imm = Vec.fill(4)(UInt(cfg.mainWidth bits))//.simPublic()
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
    for (
      (myMemAddrFwdCmp, zdx)
      <- myExt(ydx).main.memAddrFwdCmp.view.zipWithIndex
    ) {
      myMemAddrFwdCmp := inpExt.main.memAddrFwdCmp(zdx)
    }
    for (
      (myMemAddrFwd, zdx)
      <- myExt(ydx).main.memAddrFwd.view.zipWithIndex
    ) {
      myMemAddrFwd := inpExt.main.memAddrFwd(zdx)
    }
    for (
      (myMemAddrFwdMmw, zdx)
      <- myExt(ydx).main.memAddrFwdMmw.view.zipWithIndex
    ) {
      myMemAddrFwdMmw := inpExt.main.memAddrFwdMmw(zdx)
    }
    for (
      (myMemAddr, zdx)
      <- myExt(ydx).main.memAddr.view.zipWithIndex
    ) {
      myMemAddr := inpExt.main.memAddr(zdx).resized
    }
    for (
      (myMemAddrAlt, zdx)
      <- myExt(ydx).main.memAddrAlt.view.zipWithIndex
    ) {
      myMemAddrAlt := inpExt.main.memAddrAlt(zdx).resized
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
    for (
      (myMemAddrFwdCmp, zdx)
      <- myExt(ydx).main.memAddrFwdCmp.view.zipWithIndex
    ) {
      outpExt.main.memAddrFwdCmp(zdx) := myMemAddrFwdCmp
    }
    for (
      (myMemAddrFwd, zdx)
      <- myExt(ydx).main.memAddrFwd.view.zipWithIndex
    ) {
      outpExt.main.memAddrFwd(zdx) := myMemAddrFwd.resized
    }
    for (
      (myMemAddrFwdMmw, zdx)
      <- myExt(ydx).main.memAddrFwdMmw.view.zipWithIndex
    ) {
      outpExt.main.memAddrFwdMmw(zdx) := myMemAddrFwdMmw.resized
    }
    for (
      (myMemAddr, zdx)
      <- myExt(ydx).main.memAddr.view.zipWithIndex
    ) {
      outpExt.main.memAddr(zdx) := myMemAddr.resized
      //(
      //  outpExt.main.memAddr(zdx).bitsRange
      //)
    }
    for (
      (myMemAddrAlt, zdx) <- myExt(ydx).main.memAddrAlt.view.zipWithIndex
    ) {
      outpExt.main.memAddrAlt(zdx) := myMemAddrAlt.resized
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
