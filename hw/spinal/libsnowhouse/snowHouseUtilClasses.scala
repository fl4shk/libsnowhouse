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
import libcheesevoyage.bus.lcvBus._

case class SnowHouseRegFileConfig(
  mainWidth: Int,
  wordCountArr: Seq[Int],
  modRdPortCnt: Int,
  pipeName: String,
  private val optHowToSlice: Option[Seq[LinkedHashSet[Int]]],
  memRamStyleAltera: String="no_rw_check, MLAB",
  memRamStyleXilinx: String="auto",
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
  //val modStageCnt: Int = (
  //  1
  //)
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
  lineWordMemRamStyleAltera: String=(
    //"auto"
    //"block"
    "no_rw_check, M10K"
  ),
  lineWordMemRamStyleXilinx: String=(
    //"auto"
    "block"
  ),
  lineAttrsMemRamStyleAltera: String=(
    "no_rw_check, M10K"
  ),
  lineAttrsMemRamStyleXilinx: String=(
    //"auto"
    "block"
  ),
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
  val setWidth = (
    addrWidth - tagWidth - 1
  )
  val setRange = (
    addrWidth - 1 - tagWidth - 1
    downto log2Up(lineSizeBytes)
  )
  println(
    s"isIcache:${isIcache}: "
    + s"tagWidth:${tagWidth} "
    + s"tagRange:${tagRange} "
    + s"nonCachedRange:${nonCachedRange} "
    + s"setWidth:${setWidth} "
    + s"setRange:${setRange}"
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
  optMainAddrWidth: Option[Int],
  shRegFileCfg: SnowHouseRegFileConfig,
  //haveIcache: Boolean=false,
  //--------
  //srcWidth: Int=1,
  haveIcache: Boolean=true,
  icacheDepthWords: Int=(
    //8192
    1024
  ),
  icacheLineSizeBytes: Int=64,
  icacheBusSrcNum: Int=0,
  icacheLineWordMemRamStyleAltera: String="no_rw_check, M10K",
  icacheLineWordMemRamStyleXilinx: String="auto",
  icacheLineAttrsMemRamStyleAltera: String="no_rw_check, MLAB",
  icacheLineAttrsMemRamStyleXilinx: String="auto",
  //--------
  haveDcache: Boolean=true,
  dcacheDepthWords: Int=(
    //8192
    1024
  ),
  dcacheLineSizeBytes: Int=64,
  dcacheBusSrcNum: Int=1,
  dcacheLineWordMemRamStyleAltera: String="no_rw_check, M10K",
  dcacheLineWordMemRamStyleXilinx: String="auto",
  dcacheLineAttrsMemRamStyleAltera: String="no_rw_check, MLAB",
  dcacheLineAttrsMemRamStyleXilinx: String="auto",
  //--------
  totalNumBusHosts: Int=2,
  optCacheBusSrcWidth: Option[Int]=None,
  //--------
) {
  val mainAddrWidth = (
    optMainAddrWidth match {
      case Some(mainAddrWidth) => {
        require(
          mainAddrWidth > 0
          && mainAddrWidth <= shRegFileCfg.mainWidth
        )
        mainAddrWidth
      }
      case None => shRegFileCfg.mainWidth
    }
  )
  val instrSizeBytes = (instrMainWidth.toLong / 8.toLong).toLong
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
      depthWords=icacheDepthWords,
      //srcWidth=srcWidth,
      srcId=icacheBusSrcNum,
      totalNumBusHosts=totalNumBusHosts,
      lineWordMemRamStyleAltera=icacheLineWordMemRamStyleAltera,
      lineWordMemRamStyleXilinx=icacheLineWordMemRamStyleXilinx,
      lineAttrsMemRamStyleAltera=icacheLineAttrsMemRamStyleAltera,
      lineAttrsMemRamStyleXilinx=icacheLineAttrsMemRamStyleXilinx,
      isIcache=true,
    )
  )
  def myLcvBusSrcWidth = 2
  val lcvIbusMainCfg = (
    LcvBusMainConfig(
      dataWidth=(
        //instrMainWidth
        shRegFileCfg.mainWidth
      ),
      addrWidth=shRegFileCfg.mainWidth,
      allowBurst=false,
      burstAlwaysMaxSize=false,
      srcWidth=(
        //if (haveIcache) (
          //shRegFileCfg.mainWidth - log2Up(instrSizeBytes)
          //4
          //3
          myLcvBusSrcWidth
        //) else (
        //  1
        //)
      ),
      haveByteEn=(
        // TODO: support `instrMainWidth < shRegFileCfg.mainWidth`
        //true
        false
      ),
      keepByteSize=false
    )
  )
  val lcvIbusEtcCfg = (
    LcvBusCacheBusPairConfig(
      mainCfg=lcvIbusMainCfg,
      loBusCacheCfg=LcvBusCacheConfig(
        kind=LcvCacheKind.I,
        lineSizeBytes=icacheLineSizeBytes,
        depthWords=icacheDepthWords,
        numCpus=1,
        lineWordMemRamStyleAltera=icacheLineWordMemRamStyleAltera,
        lineWordMemRamStyleXilinx=icacheLineWordMemRamStyleXilinx,
        lineAttrsMemRamStyleAltera=icacheLineAttrsMemRamStyleAltera,
        lineAttrsMemRamStyleXilinx=icacheLineAttrsMemRamStyleXilinx,
      ),
      hiBusCacheCfg=None,
    )
  )
  val dcacheCfg = (
    haveDcache
  ) generate (
    SnowHouseCacheConfig(
      addrWidth=shRegFileCfg.mainWidth,
      wordWidth=shRegFileCfg.mainWidth,
      lineSizeBytes=dcacheLineSizeBytes,
      depthWords=dcacheDepthWords,
      //srcWidth=srcWidth,
      srcId=dcacheBusSrcNum,
      totalNumBusHosts=totalNumBusHosts,
      lineWordMemRamStyleAltera=dcacheLineWordMemRamStyleAltera,
      lineWordMemRamStyleXilinx=dcacheLineWordMemRamStyleXilinx,
      lineAttrsMemRamStyleAltera=dcacheLineAttrsMemRamStyleAltera,
      lineAttrsMemRamStyleXilinx=dcacheLineAttrsMemRamStyleXilinx,
      isIcache=false,
    )
  )
  val lcvDbusMainCfg = (
    LcvBusMainConfig(
      dataWidth=shRegFileCfg.mainWidth,
      addrWidth=shRegFileCfg.mainWidth,
      allowBurst=false,
      burstAlwaysMaxSize=false,
      srcWidth=myLcvBusSrcWidth,
      haveByteEn=false,
      keepByteSize=false,
    )
  )
  val lcvDbusEtcCfg = (
    LcvBusCacheBusPairConfig(
      mainCfg=lcvDbusMainCfg,
      loBusCacheCfg=LcvBusCacheConfig(
        kind=LcvCacheKind.D,
        lineSizeBytes=dcacheLineSizeBytes,
        depthWords=dcacheDepthWords,
        numCpus=1,
        lineWordMemRamStyleAltera=dcacheLineWordMemRamStyleAltera,
        lineWordMemRamStyleXilinx=dcacheLineWordMemRamStyleXilinx,
        lineAttrsMemRamStyleAltera=dcacheLineAttrsMemRamStyleAltera,
        lineAttrsMemRamStyleXilinx=dcacheLineAttrsMemRamStyleXilinx,
      ),
      hiBusCacheCfg=None,
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
  if (haveIcache) {
    icacheCfg.bridgeCfg = (
      icacheCfg._mkBridgeCfg(
        srcWidth=cacheBusSrcWidth
      )
    )
  }
  if (haveDcache) {
    dcacheCfg.bridgeCfg = (
      dcacheCfg._mkBridgeCfg(
        srcWidth=cacheBusSrcWidth
      )
    )
  }
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
  //private[libsnowhouse] def _branchTgtBufNumRdAddrs: Int = 2
}
object SnowHouseBranchPredictorKind {
  private[libsnowhouse] def _predictorInpRegPcSize: Int = (
    3
    //2
    //1
  )
  private[libsnowhouse] def _predictorInpRegPcIdxCmpEq: Int = (
    2
  )
  private[libsnowhouse] def _branchTgtBufRdAddrSize: Int = (
    _branchTgtBufRdAddrIdx1 + 1
  )
  private[libsnowhouse] def _branchTgtBufRdAddrIdx0: Int = (
    0
  )
  private[libsnowhouse] def _branchTgtBufRdAddrIdx1: Int = (
    //0
    1
  )
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
      BAK//,
      //NO_PREDICT
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
  optInvertTwoRegCmp: Boolean=false,
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
  //instrRamFetchLatency: Int,
  optTwoCycleRegFileReads: Boolean=(
    false
    //true
  ),
  //--------
  //maxNumGprsPerInstr: Int,
  //modOpCntWidth: Int=8,
  //supportInstrByteAddressing: Boolean=false,
  //supportPre: Boolean=false,
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
  useLcvInstrBus: Boolean=(
    //false
    true
  ),
  useLcvDataBus: Boolean=(
    //false
    true
  ),
  //splitAluOp: Boolean=false,
  targetAltera: Boolean=false,
  optFormal: Boolean=false,
) {
  def mainAddrWidth = subCfg.mainAddrWidth
  def myHaveS2mIf = (
    useLcvInstrBus
    && useLcvDataBus
  )
  def regPcSetItCntWidth = (
    //2
    1
  )
  def takeJumpCntMaxVal = (
    //if (!useLcvInstrBus) (
      2//1//3//2
    //) else (
    //  1//3//1//3//4//1//4//3
    //)
     //+ (if (!useLcvInstrBus) (0) else (1))
  )
  def haveBranchPredictor = (
    optBranchPredictorKind != None
  )
  def branchTgtBufSize = (
    optBranchPredictorKind match {
      case Some(myBranchPredictorKind) => {
        myBranchPredictorKind._branchTgtBufSize
      }
      case None => {
        0
      }
    }
  )
  def mySrcRegPcWidth = (
    //mainWidth
    mainAddrWidth
    //- (2 * log2Up(cfg.instrSizeBytes))
    - log2Up(instrSizeBytes)
    //- log2Up(branchTgtBufSize)
  )
  def mySrcRegPcCmpEqWidth = (
    mySrcRegPcWidth
    - log2Up(branchTgtBufSize)
  )
  def mySrcRegPcRange = (
    //mainWidth - 1
    //downto mainWidth - mySrcRegPcWidth
    mainAddrWidth - 1
    downto mainAddrWidth - mySrcRegPcWidth
  )
  def mySrcRegPcCmpEqRange = (
    //mainWidth - 1
    //downto mainWidth - mySrcRegPcCmpEqWidth
    mainAddrWidth - 1
    downto mainAddrWidth - mySrcRegPcCmpEqWidth
  )
  println(
    s"mySrcRegPcWidth:${mySrcRegPcWidth} "
    + s"mySrcRegPcCmpEqWidth:${mySrcRegPcCmpEqWidth} "
    + s"mySrcRegPcRange:${mySrcRegPcRange} "
    + s"mySrcRegPcCmpEqRange:${mySrcRegPcCmpEqRange}"
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
  //def regPcWidth = (
  //  if (!supportInstrByteAddressing) (
  //    mainWidth - log2Up(instrSizeBytes)
  //  ) else (
  //    mainWidth
  //  )
  //)
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
        s"Can't have only destinations for AluFlags"
      )
    }
    found
  }
  val myHaveLcvAlu: Boolean = {
    var found: Boolean = false
    for (((_, opInfo), opInfoIdx) <- opInfoMap.view.zipWithIndex) {
      opInfo.select match {
        case OpSelect.Alu => {
          opInfo.aluOp.get match {
            case AluOpKind.LcvAlu(op) => {
              found = true
            }
            case _ => {
            }
          }
        }
        case _ => {
        }
      }
    }
    found
  }

  assert(
    (instrMainWidth / 8) * 8 == instrMainWidth,
    s"instrMainWidth must be a multiple of 8"
  )
  // TODO: support more than 3 general purpose registers per instruction
  // (probably going up to 4 or 5 or something at max?)
  val maxNumGprsPerInstr = regFileModRdPortCnt + 1
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
  def regFileModStageCnt = (
    //if (!useLcvDataBus) (
      1
    //) else (
    //  2
    //)
    //shRegFileCfg.modStageCnt
  )
  def regFilePipeName = shRegFileCfg.pipeName
  def regFileMemRamStyleAltera = shRegFileCfg.memRamStyleAltera
  def regFileMemRamStyleXilinx = shRegFileCfg.memRamStyleXilinx
  val regFileCfg = PipeMemRmwConfig[UInt, Bool](
    wordType=UInt(mainWidth bits),
    wordCountArr=regFileWordCountArr,
    hazardCmpType=Bool(),
    modRdPortCnt=regFileModRdPortCnt,
    modStageCnt=regFileModStageCnt,
    pipeName=regFilePipeName,
    optIncludePreMid0Front=optTwoCycleRegFileReads,
    //linkArr=linkArr
    optDualRd=(
      false
    ),
    initBigInt={
      val myInitBigInt = ArrayBuffer[ArrayBuffer[BigInt]]()
      for ((wordCount, jdx) <- regFileWordCountArr.view.zipWithIndex) {
        val tempArr = ArrayBuffer[BigInt]()
        for (idx <- 0 until wordCount - 1) {
          val toAdd: Int = 0x0
          tempArr += toAdd
        }
        tempArr += BigInt(0x800)
        myInitBigInt += tempArr
      }
      Some(myInitBigInt)
    },
    memRamStyleAltera=regFileMemRamStyleAltera,
    memRamStyleXilinx=regFileMemRamStyleXilinx,
    optModHazardKind=PipeMemRmw.ModHazardKind.Fwd,
    optFwdHaveZeroReg=haveZeroReg,
    fwdForFmaxStageMax=(
      if (!useLcvDataBus) (
        0
      ) else (
        //2
        1
        //0
      )
    ),
    optIncludeModFrontS2MLink=false,
    optFormal=optFormal,
    numForkJoin=/*2*/1,
  )
  regFileCfg.linkArr = None
  def numGprs = regFileCfg.wordCountSum
  //--------
  val cpyCpyuiOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //val pureCpyOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val jmpBrAlwaysEqNeOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val jmpBrOtherOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //val pureJmpOpInfoMap = LinkedHashMap[Int, OpInfo]()
  //val cpyOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val aluOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val aluShiftOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val aluLcvDel1OpInfoMap = LinkedHashMap[Int, OpInfo]()
  val nonMultiCycleOpInfoMap = LinkedHashMap[Int, OpInfo]()
  val cpyCpyuiAluNonShiftOpInfoMap = LinkedHashMap[Int, OpInfo]()
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
        isSigned, isStore, /*isAtomicCmpxchg,*/ accSize
      ) => {
        //if (!isAtomicCmpxchg) {
          //if (!isStore) {
          //  loadOpInfoMap += (idx -> opInfo)
          //} else { // if (isStore)
          //  storeOpInfoMap += (idx -> opInfo)
          //}
          memAccOpInfoMap += (idx -> opInfo)
        //} else {
        //  assert(
        //    false,
        //    s"Error: Atomic operations are not yet implemented: "
        //    + s"opInfo(${opInfo}), instructionIndex:${idx}"
        //  )
        //  false
        //}
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
            cpyCpyuiAluNonShiftOpInfoMap += (idx -> opInfo)
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
            cpyCpyuiAluNonShiftOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.AtomicLl => {
            cpyCpyuiOpInfoMap += (idx -> opInfo)
            nonMultiCycleOpInfoMap += (idx -> opInfo)
            cpyCpyuiAluNonShiftOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.AtomicSc => {
            cpyCpyuiOpInfoMap += (idx -> opInfo)
            nonMultiCycleOpInfoMap += (idx -> opInfo)
            cpyCpyuiAluNonShiftOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.Jmp => { // non-relative jumps
            //assert(
            //  opInfo.dstArr.find(_ == DstKind.Pc) != None,
            //  s"Error: unsupported lack of PC as (any) destination of a "
            //  + s"CpyOpKind.Jmp "
            //  + s"instruction: "
            //  + s"opInfo(${opInfo}), instructionIndex:${idx}"
            //)
            opInfo.cond match {
              case CondKind.Always => {
                jmpBrAlwaysEqNeOpInfoMap += (idx -> opInfo)
              }
              case CondKind.Eq => {
                jmpBrAlwaysEqNeOpInfoMap += (idx -> opInfo)
              }
              case CondKind.Ne => {
                jmpBrAlwaysEqNeOpInfoMap += (idx -> opInfo)
              }
              case _ => {
                jmpBrOtherOpInfoMap += (idx -> opInfo)
              }
            }
            nonMultiCycleOpInfoMap += (idx -> opInfo)
          }
          case CpyOpKind.Br => { // relative branches
            //pureBrOpInfoMap += (idx -> opInfo)
            //jmpBrAlwaysEqNeOpInfoMap += (idx -> opInfo)
            opInfo.cond match {
              case CondKind.Always => {
                jmpBrAlwaysEqNeOpInfoMap += (idx -> opInfo)
              }
              case CondKind.Eq => {
                jmpBrAlwaysEqNeOpInfoMap += (idx -> opInfo)
              }
              case CondKind.Ne => {
                jmpBrAlwaysEqNeOpInfoMap += (idx -> opInfo)
              }
              case _ => {
                jmpBrOtherOpInfoMap += (idx -> opInfo)
              }
            }
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
        cpyCpyuiAluNonShiftOpInfoMap += (idx -> opInfo)
        opInfo.aluOp.get match {
          case AluOpKind.LcvAlu(_) => {
            aluLcvDel1OpInfoMap += (idx -> opInfo)
          }
          case _ => {
          }
        }
      }
      case OpSelect.AluShift => {
        checkValidArgs(opInfo.aluShiftOp)
        aluShiftOpInfoMap += (idx -> opInfo)
        nonMultiCycleOpInfoMap += (idx -> opInfo)
        //cpyCpyuiAluNonShiftOpInfoMap += (idx -> opInfo)
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
  val allAluOpsUseLcvAluDel1 = (
    aluShiftOpInfoMap.size == 0
    && aluOpInfoMap.size == aluLcvDel1OpInfoMap.size
    && aluLcvDel1OpInfoMap.size > 0
  )
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
            cpyOpInfo._cpyOp match {
              case CpyOpKind.AtomicLl => {
              }
              case CpyOpKind.AtomicSc => {
              }
              case _ => {
                if (
                  //!mem.isAtomicLlSc
                  //&& 
                  cpyOpInfo.srcArr.size == 2
                ) {
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
      }
    }
    (!found)
  }
  val onlyOneMultiCycleWriteToIdsOpInfo = {
    var result: Option[OpInfo] = None
    var foundMultiCycleCnt: Int = 0
    var foundNonMultiCycle: Boolean = false
    for ((_, opInfo) <- opInfoMap.view) {
      if (
        foundMultiCycleCnt < 2
        && !foundNonMultiCycle
      ) {
        for (dst <- opInfo.dstArr.view) {
          //println(
          //  s"dst: ${dst}"
          //)
          dst match {
            case DstKind.Spr(sprKind) => {
              if (sprKind == SprKind.Ids) {
                opInfo.select match {
                  case OpSelect.MultiCycle => {
                    foundMultiCycleCnt += 1
                    if (result == None) {
                      result = Some(opInfo)
                    }
                  }
                  case _ => {
                    foundNonMultiCycle = true
                  }
                }
              }
            }
            case _ => {
            }
          }
        }
      }
    }
    if (!(foundMultiCycleCnt == 1 && !foundNonMultiCycle)) {
      //println(
      //  s"I found this: ${result}" 
      //)
      result = None
    }
    //println(
    //  s"final `result`: ${result}" 
    //)
    result
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
  val fwdCanDoItInfo = Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
    Bool()
  )
  val myPsIdBubble = Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
    Bool()
  )
  //val myPsIdPreBubble = Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
  //  Bool()
  //)
  //val myPsIdEitherBubble = Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
  //  Bool()
  //)
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
  val memAccessLcvDbusByteSize = (
    !cfg.subCfg.lcvDbusEtcCfg.loBusCfg.haveByteEn
  ) generate (
    UInt(cfg.subCfg.lcvDbusEtcCfg.loBusCfg.byteSizeWidth bits)
  )
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
  //val opIsAluShift = Vec.fill(
  //  1
  //)(
  //  Bool()
  //)
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
  val cpyCpyuiAluNonShiftOp = (
    !cfg.allAluOpsUseLcvAluDel1
  ) generate /*Flow*/(
    //UInt(log2Up(cfg.cpyCpyuiAluNonShiftOpInfoMap.size + 1) bits)
    UInt((cfg.cpyCpyuiAluNonShiftOpInfoMap.size + 1) bits)
  )
  val kind = SnowHouseSplitOpKind(
    //binaryOneHot
  )
  val cpyCpyuiOp = (
    cfg.allAluOpsUseLcvAluDel1
  ) generate /*Flow*/(
    //UInt(log2Up(cfg.cpyCpyuiOpInfoMap.size) bits)
    UInt((cfg.cpyCpyuiOpInfoMap.size + 1) bits)
  )
  val exSetNextPcKind = (
    SnowHousePsExSetNextPcKind(encoding=binarySequential)
  )
  val jmpBrAlwaysEqNeOp = /*Flow*/(
    UInt(log2Up(cfg.jmpBrAlwaysEqNeOpInfoMap.size + 1) bits)
    //UInt((cfg.jmpBrAlwaysEqNeOpInfoMap.size + 1) bits)
  )
  val jmpBrOtherOp = (
    //UInt(log2Up(cfg.jmpBrOtherOpInfoMap.size + 1) bits)
    UInt((cfg.jmpBrOtherOpInfoMap.size + 1) bits)
    //UInt(cfg.jmpBrOtherOpInfoMap.size bits)
  )
  val havePredictableJmpBr = Bool()
  //val jmpBrAlwaysEqNeOpOneHot = (
  //  UInt((cfg.jmpBrAlwaysEqNeOpInfoMap.size + 1) bits)
  //)
  //val pureJmpOp = /*Flow*/(
  //  UInt(log2Up(cfg.pureJmpOpInfoMap.size) bits)
  //)
  //val aluOp = (
  //  //!cfg.allAluOpsUseLcvAluDel1
  //  true
  //) generate /*Flow*/(
  //  UInt(log2Up(cfg.aluOpInfoMap.size) bits)
  //)

  //val aluLcvDel1Op = (
  //  cfg.allAluOpsUseLcvAluDel1
  //) generate (
  //  UInt(log2Up(cfg.aluLcvDel1OpInfoMap.size) bits)
  //)

  //val aluShiftOp = /*Flow*/(
  //  UInt(log2Up(cfg.aluShiftOpInfoMap.size + 1) bits)
  //)
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
  def getJmpBrAlwaysEqNeOpDefault() = (
    (1 << jmpBrAlwaysEqNeOp.getWidth) - 1
    //1 << (jmpBrAlwaysEqNeOp.getWidth - 1)
  )
  def setJmpBrAlwaysEqNeOpToDefault(
  ): Unit = {
    jmpBrAlwaysEqNeOp := getJmpBrAlwaysEqNeOpDefault()
  }
  def getJmpBrOtherOpToDefault() = (
    //(1 << jmpBrOtherOp.getWidth) - 1
    1 << (jmpBrOtherOp.getWidth - 1)
    //0x0
  )
  def setJmpBrOtherOpToDefault(
  ): Unit = {
    jmpBrOtherOp := (
      getJmpBrOtherOpToDefault()
    )
  }
  def setToDefault(
  ): Unit = {
    this := this.getZero
    kind := SnowHouseSplitOpKind.CPY_CPYUI
    opIsMultiCycle := False
    //nonMultiCycleOp := (
    //  (1 << nonMultiCycleOp.getWidth) - 1
    //)
    if (!cfg.allAluOpsUseLcvAluDel1) {
      cpyCpyuiAluNonShiftOp := (
        //(1 << cpyCpyuiAluNonShiftOp.getWidth) - 1
        1 << (cpyCpyuiAluNonShiftOp.getWidth - 1)
      )
    } else {
      cpyCpyuiOp := (
        //(1 << cpyCpyuiOp.getWidth) - 1
        1 << (cpyCpyuiOp.getWidth - 1)
      )
    }
    //aluShiftOp := (
    //  (1 << aluShiftOp.getWidth) - 1
    //)
    multiCycleOp := 0x0
    opIsMemAccess := False
    setJmpBrAlwaysEqNeOpToDefault()
    setJmpBrOtherOpToDefault()
    havePredictableJmpBr := False
    //jmpBrAlwaysEqNeOpOneHot := (
    //  1 << (jmpBrAlwaysEqNeOpOneHot.getWidth - 1)
    //)
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
    Ira,
    Ids
    = newElement()
}
case class SnowHousePipePayloadNonExt(
  cfg: SnowHouseConfig
) extends Bundle {
  val lcvDataBusSrc = (
    cfg.useLcvDataBus
  ) generate (
    UInt(cfg.subCfg.myLcvBusSrcWidth bits)
  )
  val shouldFinishJump = Bool()
  //val psIfReadyIshCond = (
  //  cfg.useLcvInstrBus
  //) generate (
  //  Bool()
  //)
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
    //Vec.fill(2)(
      Bool()
    //)
  )
  val branchPredictReplaceBtbElem = (
    cfg.haveBranchPredictor
  ) generate (
    Bool()
  )
  val encInstr = Flow(UInt(cfg.instrMainWidth bits))
  val branchTgtBufElem = Vec.fill(2)(
    BranchTgtBufElem(cfg=cfg)
  )
  val btbElemBranchKind = (
    cfg.haveBranchPredictor
  ) generate (
    Vec.fill(2)(
      Bits(
        cfg.optBranchPredictorKind.get._branchKindEnumWidth bits
      )
    )
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
  //val nonShiftModMemWord = UInt(cfg.mainWidth bits)
  //val shiftModMemWordValid = (
  //  Vec.fill(
  //    //cfg.regFileCfg.modMemWordValidSize //+ 1
  //    1
  //  )(
  //    Bool()
  //  )
  //)
  //val aluModMemWord = UInt(cfg.mainWidth bits)
  val aluInpBIsImm = Bool()
  val aluOp = UInt(LcvAluDel1InpOpEnum.OP_WIDTH bits)
  //val aluModMemWordValid = (
  //  Vec.fill(
  //    //cfg.regFileCfg.modMemWordValidSize //+ 1
  //    1
  //  )(
  //    Bool()
  //  )
  //)

  //val shiftModMemWord = (
  //  //Vec.fill(3)(
  //    UInt(cfg.mainWidth bits)
  //  //)
  //)
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
    Vec.fill(cfg.regFileCfg.modMemWordValidSize + 1)(
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
  val regPc = UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  )//.simPublic()
  val laggingRegPc = /*Vec.fill(2)*/(
    //Vec.fill(3)(
      UInt(
        //cfg.mainWidth bits
        cfg.mainAddrWidth bits
      )//.simPublic()
    //)
  )
  def myHistRegPcSize = (
    3
  )
  //val laggingRegPcPlus1InstrSize = (
  //  //Vec.fill(myHistRegPc.size - 1)(
  //    UInt(cfg.mainWidth bits)
  //  //)
  //)
  val laggingRegPcPlus1InstrSize = (
    //Vec.fill(myHistRegPc.size - 1)(
      UInt(
        //cfg.mainWidth bits
        cfg.mainAddrWidth bits
      )
    //)
  )
  ////val myHistRegPcPlusInstrSize = Vec.fill(myHistRegPc.size)(
  //  /*Flow*/(UInt(cfg.mainWidth bits))//.simPublic()
  //)

  val irqIraRegPc = Vec.fill(2)(
    UInt(
      //cfg.mainWidth bits
      cfg.mainAddrWidth bits
    )
  )
  //val haveLcvDbusMemAccDelay = (
  //  cfg.useLcvDataBus
  //) generate (
  //  Bool()
  //)

  val regPcPlus1Instr = UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  )
  val psIfRegPcSetItCnt = UInt(2 bits)
  val regPcSetItCnt = Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
    UInt(
      //cfg.instrCntWidth bits
      //2 bits
      cfg.regPcSetItCntWidth bits
    ) //Bool()
  )
  val regPcPlusInstrSize = UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  )
  val regPcPlusImm = UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  )//.simPublic()
  val imm = Vec.fill(4)(UInt(cfg.mainWidth bits))//.simPublic()
  //val op = UInt(log2Up(cfg.opInfoMap.size) bits)
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
}
case class SnowHousePipePayload(
  cfg: SnowHouseConfig,
) extends Bundle with PipeMemRmwPayloadBase[UInt, Bool] {
  val nonExt = SnowHousePipePayloadNonExt(cfg=cfg)
  def shouldFinishJump = nonExt.shouldFinishJump
  //def psIfReadyIshCond = nonExt.psIfReadyIshCond
  def blockIrq = nonExt.blockIrq
  def takeIrq = nonExt.takeIrq
  def branchPredictTkn = nonExt.branchPredictTkn

  def branchPredictReplaceBtbElem = nonExt.branchPredictReplaceBtbElem
  def encInstr = nonExt.encInstr
  def branchTgtBufElem = nonExt.branchTgtBufElem
  def btbElemBranchKind = nonExt.btbElemBranchKind
  def inpDecodeExt = nonExt.inpDecodeExt
  def outpDecodeExt = nonExt.outpDecodeExt
  def instrCnt = nonExt.instrCnt
  def opCnt = nonExt.opCnt
  def op = nonExt.op
  def splitOp = nonExt.splitOp
  def myDoHaveHazardAddrCheckVec = nonExt.myDoHaveHazardAddrCheckVec

  def irqJmpOp = nonExt.irqJmpOp
  def formalAssumes() = nonExt.formalAssumes()
  //def aluModMemWord = nonExt.aluModMemWord
  def aluInpBIsImm = nonExt.aluInpBIsImm
  def aluOp = nonExt.aluOp
  //def aluModMemWordValid = nonExt.aluModMemWordValid
  def gprIdxVec = nonExt.gprIdxVec
  def gprIsZeroVec = nonExt.gprIsZeroVec
  def gprIsNonZeroVec = nonExt.gprIsNonZeroVec
  def gprIdxToMemAddrIdxMap = nonExt.gprIdxToMemAddrIdxMap
  def psExSetPc = nonExt.psExSetPc
  def psExSetOutpModMemWordIo = nonExt.psExSetOutpModMemWordIo
  def regPc = nonExt.regPc
  def laggingRegPc = nonExt.laggingRegPc
  def laggingRegPcPlus1InstrSize = nonExt.laggingRegPcPlus1InstrSize
  def myHistRegPcSize = (
    nonExt.myHistRegPcSize
  )

  def irqIraRegPc = nonExt.irqIraRegPc
  //def haveLcvDbusMemAccDelay = nonExt.haveLcvDbusMemAccDelay

  def regPcPlus1Instr = nonExt.regPcPlus1Instr
  def psIfRegPcSetItCnt = nonExt.psIfRegPcSetItCnt
  def regPcSetItCnt = nonExt.regPcSetItCnt
  def regPcPlusInstrSize = nonExt.regPcPlusInstrSize
  def regPcPlusImm = nonExt.regPcPlusImm
  def imm = nonExt.imm

  def myHaveFormalFwd = nonExt.myHaveFormalFwd

  def myFwd = nonExt.myFwd

  def mkOneExt(ydx: Int) = (
    PipeMemRmwPayloadExt(
      cfg=cfg.regFileCfg,
      wordCount=cfg.regFileCfg.wordCountArr(ydx),
    )
  )
  val myExt = Vec[PipeMemRmwPayloadExt[UInt, Bool]]{
    val myArr = ArrayBuffer[PipeMemRmwPayloadExt[UInt, Bool]]()
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      myArr += mkOneExt(ydx=ydx)
    }
    myArr
  } //simPublic()
  def setPipeMemRmwExt(
    inpExt: PipeMemRmwPayloadExt[UInt, Bool],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
    myExt(ydx).pipeFlags := inpExt.pipeFlags
    myExt(ydx).main.nonMemAddrMost := inpExt.main.nonMemAddrMost
    myExt(ydx).main.modMemWord := inpExt.main.modMemWord
    for (
      (myMemAddrFwdCmp, zdx)
      <- myExt(ydx).memAddrFwdCmp.view.zipWithIndex
    ) {
      myMemAddrFwdCmp := inpExt.memAddrFwdCmp(zdx)
    }
    for (
      (myMemAddrFwd, zdx)
      <- myExt(ydx).memAddrFwd.view.zipWithIndex
    ) {
      myMemAddrFwd := inpExt.memAddrFwd(zdx)
    }
    for (
      (myMemAddrFwdMmw, zdx)
      <- myExt(ydx).memAddrFwdMmw.view.zipWithIndex
    ) {
      myMemAddrFwdMmw := inpExt.memAddrFwdMmw(zdx)
    }
    for (
      (myMemAddr, zdx)
      <- myExt(ydx).memAddr.view.zipWithIndex
    ) {
      myMemAddr := inpExt.memAddr(zdx).resized
    }
    for (
      (myMemAddrAlt, zdx)
      <- myExt(ydx).memAddrAlt.view.zipWithIndex
    ) {
      myMemAddrAlt := inpExt.memAddrAlt(zdx).resized
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
    outpExt.main.nonMemAddrMost := myExt(ydx).main.nonMemAddrMost
    outpExt.main.modMemWord := myExt(ydx).main.modMemWord
    for (
      (myMemAddrFwdCmp, zdx)
      <- myExt(ydx).memAddrFwdCmp.view.zipWithIndex
    ) {
      outpExt.memAddrFwdCmp(zdx) := myMemAddrFwdCmp
    }
    for (
      (myMemAddrFwd, zdx)
      <- myExt(ydx).memAddrFwd.view.zipWithIndex
    ) {
      outpExt.memAddrFwd(zdx) := myMemAddrFwd.resized
    }
    for (
      (myMemAddrFwdMmw, zdx)
      <- myExt(ydx).memAddrFwdMmw.view.zipWithIndex
    ) {
      outpExt.memAddrFwdMmw(zdx) := myMemAddrFwdMmw.resized
    }
    for (
      (myMemAddr, zdx)
      <- myExt(ydx).memAddr.view.zipWithIndex
    ) {
      outpExt.memAddr(zdx) := myMemAddr.resized
      //(
      //  outpExt.memAddr(zdx).bitsRange
      //)
    }
    for (
      (myMemAddrAlt, zdx) <- myExt(ydx).memAddrAlt.view.zipWithIndex
    ) {
      outpExt.memAddrAlt(zdx) := myMemAddrAlt.resized
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
