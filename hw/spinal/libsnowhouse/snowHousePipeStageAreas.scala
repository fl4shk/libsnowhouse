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
import libcheesevoyage.bus.lcvBus._

//object SnowHouseHaveFindOpInfo {
//  def apply[
//    EncInstrT <: Data
//  ](
//    cfg: SnowHouseConfig,
//  ): ArrayBuffer[OpInfo] = {
//    val ret = ArrayBuffer[OpInfo]()
//    for (((_, opInfo), idx) <- cfg.opInfoMap.view.zipWithIndex) {
//      ret += null
//    }
//    ret
//  }
//}
//object LcvFastOrR {
//  def apply(
//    self: UInt
//  ): Bool = {
//    val q = Bool()
//    val unusedSumOut = UInt(self.getWidth bits)
//    (q, unusedSumOut) := (
//      Cat(False, self).asUInt
//      + U(self.getWidth bits, default -> True)
//    )
//    q
//  }
//}
//object LcvFastAndR {
//  def apply(
//    self: UInt
//  ): Bool = {
//    val q = Bool()
//    val unusedSumOut = UInt(self.getWidth bits)
//    (q, unusedSumOut) := (
//      Cat(False, self).asUInt
//      + U(self.getWidth + 1 bits, 0 -> True, default -> False)
//    )
//    q
//  }
//}
//object LcvFastCmpEq {
//  def apply(
//    left: UInt,
//    right: UInt,
//  ): Bool = {
//    assert(
//      left.getWidth == right.getWidth,
//      f"leftWidth:${left.getWidth} != rightWidth:${right.getWidth}"
//    )
//    val q = Bool()
//    val unusedSumOut = UInt(left.getWidth bits)
//    (q, unusedSumOut) := (
//      Cat(False, left ^ (~right)).asUInt
//      + U(left.getWidth + 1 bits, 0 -> True, default -> False)
//    )
//
//    q
//  }
//}
case class SnowHousePipeStageArgs(
  cfg: SnowHouseConfig,
  io: SnowHouseIo,
  link: CtrlLink,
  prevPayload: Payload[SnowHousePipePayload],
  currPayload: Payload[SnowHousePipePayload],
  myDbusIo: SnowHouseDbusIo,
  var regFile: PipeMemRmw[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeMemRmwDualRdTypeDisabled[UInt, Bool],
  ],
) {
}
//case class SnowHousePipeStagePayload[
//  EncInstrT <: Data
//](
//  cfg: SnowHouseConfig,
//  //encInstrType: HardType,
//) extends Bundle {
//  val gprIdxVec = Vec.fill(cfg.maxNumGprsPerInstr)(
//    UInt(log2Up(cfg.numGprs) bits)
//  )
//  val gprRdMemWordVec = Vec.fill(cfg.regFileModRdPortCnt)(
//    UInt(cfg.mainWidth bits)
//  )
//  val regPc = UInt(cfg.mainWidth bits)
//  val regPcPlusImm = UInt(cfg.mainWidth bits)
//  val imm = UInt(cfg.mainWidth bits)
//  val op = UInt(log2Up(cfg.opInfoMap.size) bits)
//  // decoded instruction select
//}
//case class SnowHousePipeStageInstrFetchIo[
//  EncInstrT <: Data
//](
//  cfg: SnowHouseConfig,
//) extends Area {
//}


case class SnowHouseShiftIo(
  //cfg: SnowHouseConfig,
  mainWidth: Int,
) extends Bundle {
  val inpToShift = in(UInt(mainWidth bits))
  val inpAmount = in(UInt(mainWidth bits))
  val outpResult = out(UInt(mainWidth bits))
}
case class SnowHouseLslDel1(
  mainWidth: Int,
) extends Component {
  val io = SnowHouseShiftIo(mainWidth=mainWidth)
  io.outpResult.setAsReg() init(0x0)
  io.outpResult := (
    (
      io.inpToShift << io.inpAmount(log2Up(mainWidth) downto 0)
    )(
      io.outpResult.bitsRange
    )
  )
}
case class SnowHouseLsrDel1(
  mainWidth: Int,
) extends Component {
  val io = SnowHouseShiftIo(mainWidth=mainWidth)
  io.outpResult.setAsReg() init(0x0)
  io.outpResult := (
    (
      io.inpToShift >> io.inpAmount(log2Up(mainWidth) downto 0)
    ).resize(io.outpResult.getWidth)
    //(
    //  io.outpResult.bitsRange
    //)
  )
}
case class SnowHouseAsrDel1(
  mainWidth: Int,
) extends Component {
  val io = SnowHouseShiftIo(mainWidth=mainWidth)
  io.outpResult.setAsReg() init(0x0)
  io.outpResult := (
    (
      io.inpToShift.asSInt >> io.inpAmount(log2Up(mainWidth) downto 0)
    ).resize(io.outpResult.getWidth).asUInt
  )
}
case class SnowHouseSltIo(
  mainWidth: Int,
) extends Bundle {
  val inpA = in(UInt(mainWidth bits))
  val inpB = in(UInt(mainWidth bits))
  val outpResult = out(UInt(mainWidth bits))
}
case class SnowHouseSltDel1(
  mainWidth: Int,
  isSigned: Boolean,
) extends Component {
  val io = SnowHouseSltIo(mainWidth=mainWidth)
  io.outpResult.setAsReg() init(0x0)
  io.outpResult := (
    if (!isSigned) (
      Cat(io.inpA < io.inpB).asUInt.resize(mainWidth bits)
    ) else (
      Cat(io.inpA.asSInt < io.inpB.asSInt).asUInt.resize(mainWidth bits)
    )
  )
}

case class BranchTgtBufElem(
  //mainWidth: Int,
  cfg: SnowHouseConfig,
  //optIncludeTargetEtc: Boolean,
) extends Bundle {
  // branch target buffer element
  val valid = Bool() // whether or not we even have a branch here.
  def fire = valid
  //val branchKind = (
  //  cfg.haveBranchPredictor
  //) generate (
  //  Bits(
  //    //SnowHouseBranchPredictorKind.branchKindEnumMaxWidth bits
  //    cfg.optBranchPredictorKind.get._branchKindEnumWidth bits
  //  )
  //)
  val dontPredict = (
    Bool()
  )
  val srcRegPc = UInt(
    //cfg.mySrcRegPcCmpEqWidth bits
    cfg.mainWidth bits
  )
  val dstRegPc = UInt(cfg.mainWidth bits) 
  //val dbgEncInstr = UInt(cfg.instrMainWidth bits)
}
case class BranchTgtBufElemWithBrKind(
  cfg: SnowHouseConfig
) extends Bundle {
  val branchKind = (
    Bits(
      //SnowHouseBranchPredictorKind.branchKindEnumMaxWidth bits
      cfg.optBranchPredictorKind.get._branchKindEnumWidth bits
    )
  )
  val btbElem = BranchTgtBufElem(cfg=cfg)
}
case class SnowHousePsExSetPcPayload(
  cfg: SnowHouseConfig
) extends Bundle {
  //val valid1 = Bool()
  //val extValid = Bool()

  // whether or not the branch predictor was correct. Wait, maybe this
  // isn't needed, with reuse of the "assume not taken" meaning that we had
  // a branch mis-predict when `psExSetPc.valid` is asserted.
  //val predictGood = Bool()
  //val badPredict = Bool()

  val nextPc = UInt(cfg.mainWidth bits)
  //val badPredictNextPc = UInt(cfg.mainWidth bits)
  //val encInstr = Flow(UInt(cfg.instrMainWidth bits))
  //val branchTgtBufElem = BranchTgtBufElem(cfg=cfg)
  val btbElemWithBrKind = BranchTgtBufElemWithBrKind(cfg=cfg)
  def branchTgtBufElem = btbElemWithBrKind.btbElem
  def branchKind = btbElemWithBrKind.branchKind
  //val btbWrEn = (
  //  Bool()
  //)

}
//object SnowHouseShouldIgnoreInstrState
//extends SpinalEnum(defaultEncoding=binaryOneHot) {
//  val
//    Idle,
//    IgnoreInstr0,
//    IgnoreInstr1//,
//    //IgnoreInstr2
//    = newElement()
//}
case class SnowHouseBranchPredictorResult(
  cfg: SnowHouseConfig,
) extends Bundle {
  // `valid`/`fire` indicates that we have a branch here at all
  val valid = Bool()
  def fire = valid

  val nextRegPc = UInt(cfg.mainWidth bits)

  // whether or not we're predicting the branch is taken
  val predictTkn = Bool()
  val rdBtbElem = BranchTgtBufElem(cfg=cfg)
}
case class SnowHouseBranchPredictorIo(
  cfg: SnowHouseConfig,
) extends Bundle {
  val psExSetPc = slave(
    Flow(SnowHousePsExSetPcPayload(cfg=cfg))
  )
  //val stickyExSetPc = in(
  //  Vec.fill(1)(
  //    Flow(SnowHousePsExSetPcPayload(cfg=cfg))
  //  )
  //)
  //val upModExt = in(
  //  SnowHousePipePayload(cfg=cfg)
  //)
  val inpRegPc = in(
    Vec.fill(SnowHouseBranchPredictorKind._predictorInpRegPcSize)(
      UInt(cfg.mainWidth bits)
    )
  )
  val upIsFiring = in(
    Bool()
  )
  val upIsReady = in(
    Bool()
  )
  //val downIsReady = in(
  //  Bool()
  //)
  //val psIfDoStall = out(
  //  Bool()
  //)
  //val outpUpModExt = out(
  //  SnowHousePipePayload(cfg=cfg)
  //)
  val result = out(
    SnowHouseBranchPredictorResult(cfg=cfg)
  )
}
case class SnowHouseBranchPredictor(
  //psIf: SnowHousePipeStageInstrFetch,
  cfg: SnowHouseConfig
) extends Component {
  val io = SnowHouseBranchPredictorIo(
    cfg=cfg
  )
  //def cfg = psIf.cfg
  //def up = psIf.up
  //def down = psIf.down
  //def upModExt = psIf.upModExt
  val branchTgtBufSize = (
    cfg.optBranchPredictorKind.get._branchTgtBufSize
  )
  assert (
    branchTgtBufSize > 0
  )
  //def myRegPc = io.upModExt.regPc
  //def myRegPc = io.regPc

  //val tgtBuf = (
  //  Mem(
  //    wordType=BranchTgtBufElem(
  //      cfg=cfg
  //    ),
  //    initialContent={
  //      Array.fill(branchTgtBufSize)(
  //        BranchTgtBufElem(
  //          cfg=cfg
  //        ).getZero
  //      )
  //    }
  //  )
  //  //.addAttribute(
  //  //  "asdf", "yes"
  //  //)
  //)
  //val tgtBuf = (
  //  RamSimpleDualPort(
  //    wordType=BranchTgtBufElem(
  //      //mainWidth=cfg.mainWidth,
  //      cfg=cfg,
  //    ),
  //    depth=branchTgtBufSize,
  //    initBigInt=(
  //      Some(Array.fill(branchTgtBufSize)(BigInt(0)))
  //    ),
  //    arrRamStyle="distributed",
  //  )
  //)
  val tgtBufRdAddr = (
    Vec.fill(
      //io.inpRegPc.size - 1
      //1
      SnowHouseBranchPredictorKind._branchTgtBufRdAddrSize
    )(
      UInt(log2Up(branchTgtBufSize) bits)
    )
  )
  def myDstRegPcWidth = (
    //if (!cfg.supportInstrByteAddressing) (
    cfg.mainWidth - log2Up(cfg.instrSizeBytes)
    //) else (
    //  cfg.mainWidth
    //)
  )
  //def myDstRegPcRange = (
  //  myRegPcWidth
  //)
  def myTgtBufAddrRange: Range = (
    tgtBufRdAddr(0).high + log2Up(cfg.instrSizeBytes)
    downto log2Up(cfg.instrSizeBytes)
  )
  println(
    s"myDstRegPcWidth:${myDstRegPcWidth} "
    + s"mySrcRegPcWidth:${cfg.mySrcRegPcWidth} "
    + s"mySrcRegPcRange:${cfg.mySrcRegPcRange} "
    + s"myTgtBufAddrRange:${myTgtBufAddrRange}"
  )
  val tgtSrcRegPcAndValidBufCfg = RamSimpleDualPortConfig(
    wordType=Flow(UInt(
      //cfg.mainWidth bits
      //cfg.mySrcRegPcWidth bits
      cfg.mySrcRegPcCmpEqWidth bits
    )),
    depth=branchTgtBufSize,
    initBigInt=(
      Some(Array.fill(branchTgtBufSize)(BigInt(0)))
    ),
    arrRamStyleAltera=(
      //"no_rw_check, logic"
      "no_rw_check, MLAB"
      //"MLAB"
    ),
    arrRamStyleXilinx=(
      "auto"
      //"block"
      //"distributed"
    ),
  )
  val tgtSrcRegPcAndValidBuf = (
    RamSimpleDualPort(cfg=tgtSrcRegPcAndValidBufCfg)
  )
  //val tgtSrcRegPcBuf = (
  //  RamSimpleDualPort(
  //    wordType=/*Flow*/(UInt(
  //      //cfg.mainWidth bits
  //      //cfg.mySrcRegPcWidth bits
  //      cfg.mySrcRegPcCmpEqWidth bits
  //    )),
  //    depth=branchTgtBufSize,
  //    initBigInt=(
  //      Some(Array.fill(branchTgtBufSize)(BigInt(0)))
  //    ),
  //    arrRamStyle=(
  //      "auto"
  //      //"block"
  //    ),
  //  )
  //)
  val tgtDstRegPcBufCfg = RamSimpleDualPortConfig(
    wordType=UInt(myDstRegPcWidth bits),
    depth=branchTgtBufSize,
    initBigInt=(
      Some(Array.fill(branchTgtBufSize)(BigInt(0)))
    ),
    arrRamStyleAltera=(
      //"no_rw_check, logic"
      "no_rw_check, MLAB"
      //"MLAB"
    ),
    arrRamStyleXilinx=(
      ////"auto"
      ////"distributed"
      ////"block"
      //if (!cfg.targetAltera) (
      //  //"auto"
        "distributed"
      //) else (
      //  "no_rw_check, logic"
      //  //"no_rw_check, MLAB"
      //  //"MLAB"
      //)
    ),
  )
  val tgtDstRegPcBuf = RamSimpleDualPort(cfg=tgtDstRegPcBufCfg)
  //val tgtDstRegPcAndValidBuf = (
  //  RamSimpleDualPort(
  //    wordType=Flow(UInt(myDstRegPcWidth bits)),
  //    depth=branchTgtBufSize,
  //    initBigInt=(
  //      Some(Array.fill(branchTgtBufSize)(BigInt(0)))
  //    ),
  //    arrRamStyle=(
  //      "auto"
  //      //"distributed"
  //    ),
  //  )
  //)
  //val tgtValidBuf = (
  //  RamSimpleDualPort(
  //    wordType=Bool(),
  //    depth=branchTgtBufSize,
  //    initBigInt=(
  //      Some(Array.fill(branchTgtBufSize)(BigInt(0)))
  //    ),
  //    arrRamStyle="auto",
  //  )
  //)
  //tgtBuf.io.ramIo.rdEn := True
  //tgtBuf.readAsync(
  //)
  for (idx <- 0 until tgtBufRdAddr.size) {
    tgtBufRdAddr(idx) := (
      io.inpRegPc(idx)(myTgtBufAddrRange) //- 1//- 2 //- 1 //- 2//- 3
    )
  }
  val myRdBtbElem = BranchTgtBufElem(cfg=cfg)
  //myRdBtbElem.assignFromBits(tgtBuf.io.ramIo.rdData)
  val myRdSrcRegPcAndValid = Flow(UInt(
    //cfg.mainWidth bits
    //cfg.mySrcRegPcWidth bits
    cfg.mySrcRegPcCmpEqWidth bits
  ))
  myRdSrcRegPcAndValid := (
    tgtSrcRegPcAndValidBuf.io.ramIo.rdData
  )
  myRdBtbElem.srcRegPc := (
    //myRdSrcRegPcAndValid.payload
    Cat(
      myRdSrcRegPcAndValid.payload,
      //tgtSrcRegPcBuf.io.ramIo.rdData,
      //RegNextWhen(
      //  next=tgtBufRdAddr,
      //  cond=io.upIsFiring,
      //  init=tgtBufRdAddr.getZero,
      //),
      //U(s"${log2Up(cfg.instrSizeBytes)}'d0"),
      U(s"${cfg.mainWidth - cfg.mySrcRegPcCmpEqWidth}'d0")
    ).asUInt
  )

  myRdBtbElem.valid := myRdSrcRegPcAndValid.valid

  //myRdBtbElem.srcRegPc.assignFromBits(
  //  tgtSrcRegPcBuf.io.ramIo.rdData
  //)
  //val myRdDstRegPcAndValid = (
  //  Flow(UInt(
  //    myDstRegPcWidth bits
  //  ))
  //)
  //myRdDstRegPcAndValid.assignFromBits(
  //  tgtDstRegPcAndValidBuf.io.ramIo.rdData
  //)
  //myRdBtbElem.valid := myRdDstRegPcAndValid.valid

  myRdBtbElem.dstRegPc.assignFromBits(
    Cat(
      tgtDstRegPcBuf.io.ramIo.rdData,
      //myRdDstRegPcAndValid.payload,
      U(s"${log2Up(cfg.instrSizeBytes)}'d0"),
    )
  )

  //myRdBtbElem.valid.assignFromBits(
  //  tgtValidBuf.io.ramIo.rdData
  //)
  myRdBtbElem.dontPredict := False
  //myRdBtbElem := tgtBuf.readSync(
  //  address=tgtBufRdAddr,
  //  enable=io.upIsFiring,
  //)
  //tgtSrcRegPcBuf.io.ramIo.rdAddr := tgtBufRdAddr(0)
  tgtSrcRegPcAndValidBuf.io.ramIo.rdAddr := (
    tgtBufRdAddr(
      //0
      SnowHouseBranchPredictorKind._branchTgtBufRdAddrIdx0
    )
  )
  tgtDstRegPcBuf.io.ramIo.rdAddr := (
    tgtBufRdAddr(
      //1
      //0
      SnowHouseBranchPredictorKind._branchTgtBufRdAddrIdx1
    )
  )
  //tgtDstRegPcAndValidBuf.io.ramIo.rdAddr := tgtBufRdAddr(1)
  //tgtValidBuf.io.ramIo.rdAddr := tgtBufRdAddr

  //tgtSrcRegPcBuf.io.ramIo.rdEn := io.upIsFiring
  tgtSrcRegPcAndValidBuf.io.ramIo.rdEn := (
    //io.upIsFiring
    //io.downIsReady
    io.upIsReady
  )
  tgtDstRegPcBuf.io.ramIo.rdEn := (
    //io.upIsFiring
    //io.downIsReady
    io.upIsReady
  )
  //tgtDstRegPcAndValidBuf.io.ramIo.rdEn := io.upIsFiring
  //tgtValidBuf.io.ramIo.rdEn := io.upIsFiring

  //tgtBuf.io.ramIo.rdEn := io.upIsFiring
  //myRdBtbElem := (
  //  RegNext(
  //    next=myRdBtbElem,
  //    init=myRdBtbElem.getZero,
  //  )
  //)
  //when (io.upIsFiring) {
  //  myRdBtbElem := (
  //    tgtBuf.readAsync(
  //      address=tgtBufRdAddr,
  //    )
  //  )
  //}
  io.result.rdBtbElem := myRdBtbElem
  //val rRdBtbElem = RegNextWhen(
  //  next=nextRdBtbElem,
  //  cond=io.upIsFiring,
  //  init=nextRdBtbElem.getZero,
  //)
  val wrBtbElem = BranchTgtBufElem(cfg=cfg)
  val otherWrBtbElemWithBrKind = BranchTgtBufElemWithBrKind(cfg=cfg)
  val otherWrBranchKind = (
    SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum()
  )
  otherWrBranchKind.assignFromBits(
    otherWrBtbElemWithBrKind.branchKind
  )
  val tgtBufWrEn = (
    io.psExSetPc.valid
    //&& io.psExSetPc.btbWrEn
    && (
      !otherWrBtbElemWithBrKind.btbElem.dontPredict
    ) && RegNext(
      next=(
        (
          io.psExSetPc.branchTgtBufElem.fire
        ) && (
          otherWrBranchKind
          === SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum.BAK
        )
      ),
      init=False,
    )
    //&& RegNext(
    //  next=io.psExSetPc.branchTgtBufElem.fire,
    //  init=False,
    //) //wrBtbElem.fire
    //&& !wrBtbElem.dontPredict
    //&& (
    //  wrBranchKind
    //  === SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum.BAK
    //)
    ////&& wrBtbElem.branchKind
    ////|| RegNext(next=io.stickyExSetPc(0).valid, init=False)
  )
  //tgtBuf.io.ramIo.rdAddr := (
  //  tgtBufRdAddr
  //)
  val tgtBufWrAddr = (
    //io.stickyExSetPc(0).nextPc(myTgtBufAddrRange)
    RegNext(
      //io.psExSetPc.nextPc(myTgtBufAddrRange)
      io.psExSetPc.branchTgtBufElem.srcRegPc(myTgtBufAddrRange)
      //+ (1 * cfg.instrSizeBytes)
    )
    init(0x0)
  )
  //tgtBuf.io.ramIo.wrAddr := tgtBufWrAddr
  //tgtBuf.write(
  //  address=io.stickyExSetPc(0).nextPc,
  //  data=wrBtbElem,
  //  enable=btbWrEn,
  //)
  //wrBtbElem := rRdBtbElem

  //wrBtbElem := (
  //  RegNext(
  //    next=wrBtbElem,
  //    init=wrBtbElem.getZero,
  //  )
  //)
  //wrBtbElem.dstRegPc.allowOverride

  //val otherWrBtbElem = (
  //  io.psExSetPc.branchTgtBufElem
  //)
  //otherWrBtbElemWithBrKind := (
  //  RegNext(
  //    next=otherWrBtbElemWithBrKind,
  //    init=otherWrBtbElemWithBrKind.getZero,
  //  )
  //)
  otherWrBtbElemWithBrKind := io.psExSetPc.btbElemWithBrKind
  wrBtbElem := (
    RegNext(
      next=otherWrBtbElemWithBrKind.btbElem,
      init=otherWrBtbElemWithBrKind.btbElem.getZero,
    )
  )
  //when (io.psExSetPc.valid) {
  //  //otherWrBtbElemWithBrKind := io.psExSetPc.btbElemWithBrKind
  //  wrBtbElem := (
  //    RegNext(
  //      next=otherWrBtbElemWithBrKind.btbElem,
  //      init=otherWrBtbElemWithBrKind.btbElem.getZero,
  //    )
  //  )
  //  //wrBtbElem.dstRegPc := io.psExSetPc.nextPc
  //}
  wrBtbElem.valid.allowOverride
  wrBtbElem.valid := True

  //val rdBranchKind = SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum()
  //rdBranchKind.assignFromBits(myRdBtbElem.branchKind)
  val myResultValidCmpEqLeft = (
    myRdBtbElem.srcRegPc(
      cfg.mySrcRegPcCmpEqRange
    )
  )
  val myResultValidCmpEqRight = (
    RegNextWhen(
      next=(
        io.inpRegPc(
          //2
          //0
          SnowHouseBranchPredictorKind._predictorInpRegPcIdxCmpEq
        )
        //- cfg.instrSizeBytes
      )(cfg.mySrcRegPcCmpEqRange),
      cond=(
        //io.upIsFiring
        io.upIsReady
      ),
      init=io.inpRegPc(
        //2
        //0
        SnowHouseBranchPredictorKind._predictorInpRegPcIdxCmpEq
      )(cfg.mySrcRegPcCmpEqRange).getZero,
    )
  )
  io.result.valid := (
    myRdBtbElem.fire
    && (
      //(
      //  cfg.mySrcRegPcCmpEqRange
      //)
      if (!cfg.targetAltera) (
        myResultValidCmpEqLeft
        === myResultValidCmpEqRight
      ) else (
        LcvFastCmpEq(
          left=myResultValidCmpEqLeft,
          right=myResultValidCmpEqRight,
          cmpEqIo=null,
        )._1
      )
    )
  )
  val tempNextRegPc = (
    //if (!cfg.useLcvInstrBus) (
      myRdBtbElem.dstRegPc //+ (1 * cfg.instrSizeBytes)
    //) else (
    //  myRdBtbElem.dstRegPc + (1 * cfg.instrSizeBytes)
    //)
  )
  io.result.nextRegPc := (
    tempNextRegPc
  )
  //when (
  //  io.psExSetPc.valid
  //) {
  //}
  //io.result.predictTkn := (
  //  rdBranchKind === SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum.BAK
  //)
  //tgtBuf.io.ramIo.wrData := wrBtbElem.asBits
  //tgtBuf.write(
  //  address=tgtBufWrAddr,
  //  data=wrBtbElem,
  //  enable=tgtBufWrEn,
  //)
  //tgtBuf.io.ramIo.wrAddr := tgtBufWrAddr
  //tgtSrcRegPcBuf.io.ramIo.wrAddr := tgtBufWrAddr
  tgtSrcRegPcAndValidBuf.io.ramIo.wrAddr := tgtBufWrAddr
  tgtDstRegPcBuf.io.ramIo.wrAddr := tgtBufWrAddr
  //tgtDstRegPcAndValidBuf.io.ramIo.wrAddr := tgtBufWrAddr
  //tgtValidBuf.io.ramIo.wrAddr := tgtBufWrAddr
  //tgtBuf.io.ramIo.wrData := wrBtbElem.asBits

  val myWrSrcRegPcAndValid = (
    Flow(UInt(
      //cfg.mySrcRegPcWidth bits
      cfg.mySrcRegPcCmpEqWidth bits
    ))
  )

  //val myWrDstRegPcAndValid = (
  //  Flow(UInt(
  //    myDstRegPcWidth bits
  //  ))
  //)

  //tgtSrcRegPcBuf.io.ramIo.wrData := (
  //  wrBtbElem.srcRegPc.asBits
  //)
  myWrSrcRegPcAndValid.payload := (
    wrBtbElem.srcRegPc(cfg.mySrcRegPcCmpEqRange)
  )
  myWrSrcRegPcAndValid.valid := (
    //wrBtbElem.valid
    True
  )
  tgtSrcRegPcAndValidBuf.io.ramIo.wrData := (
    myWrSrcRegPcAndValid
  )
  //tgtSrcRegPcBuf.io.ramIo.wrData := (
  //  wrBtbElem.srcRegPc(cfg.mySrcRegPcCmpEqRange).asBits
  //)
  //myWrDstRegPcAndValid.payload := (
  //  wrBtbElem.dstRegPc(
  //    wrBtbElem.dstRegPc.high
  //    downto log2Up(cfg.instrSizeBytes)
  //  )
  //)
  //myWrDstRegPcAndValid.valid := (
  //  wrBtbElem.valid
  //)
  //tgtDstRegPcAndValidBuf.io.ramIo.wrData := (
  //  myWrDstRegPcAndValid.asBits
  //)
  tgtDstRegPcBuf.io.ramIo.wrData := (
    wrBtbElem.dstRegPc(
      wrBtbElem.dstRegPc.high
      downto log2Up(cfg.instrSizeBytes)
    )
  )
  //tgtValidBuf.io.ramIo.wrData := (
  //  wrBtbElem.valid.asBits
  //)
  //tgtBuf.io.ramIo.wrEn := tgtBufWrEn
  //tgtSrcRegPcBuf.io.ramIo.wrEn := tgtBufWrEn
  tgtSrcRegPcAndValidBuf.io.ramIo.wrEn := tgtBufWrEn
  tgtDstRegPcBuf.io.ramIo.wrEn := tgtBufWrEn
  //tgtDstRegPcAndValidBuf.io.ramIo.wrEn := tgtBufWrEn
  //tgtValidBuf.io.ramIo.wrEn := tgtBufWrEn
  //when (rRdBtbElem.fire) {
  //}
  //when (tgtBuf.io.ramIo.wrEn) {
  //  //wrBtbElem
  //}
  //when (!rRdBtbElem.fire) {
  //}
  //when (btbWrEn) {
  //  when (!rRdBtbElem.fire) {
  //  } otherwise {
  //  }
  //}
}

private[libsnowhouse] case class SnowHouseBusToLcvBusBridgeIo(
  cfg: SnowHouseConfig,
  isIbus: Boolean,
) extends Bundle with IMasterSlave{
  require(cfg.useLcvInstrBus)
  //val didChangeAddrMaybe = in(Bool())
  val bus = slave(
    new LcvStallIo[BusHostPayload, BusDevPayload](
      sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=isIbus)),
      recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=isIbus)),
    )
  )
  val lcvBus = master(
    LcvBusIo(cfg=cfg.subCfg.lcvIbusEtcCfg.loBusCfg)
  )

  val h2dPushDelay = out(Bool())
  def asMaster(): Unit = {
    master(bus)
    slave(lcvBus)
    in(h2dPushDelay)
  }
}
private[libsnowhouse] case class SnowHouseIbusToLcvIbusBridge(
  cfg: SnowHouseConfig
) extends Component {
  //--------
  require(cfg.useLcvInstrBus)
  //--------
  val io = slave(SnowHouseBusToLcvBusBridgeIo(
    cfg=cfg,
    isIbus=true,
  ))
  io.bus.ready := False//RegNext(io.ibus.ready, init=False)
  io.bus.recvData := (
    RegNext(io.bus.recvData, init=io.bus.recvData.getZero)
  )
  //--------
  def myH2dPushStm = io.lcvBus.h2dBus
  def myD2hPopStm = io.lcvBus.d2hBus

  io.h2dPushDelay := (
    //myH2dPushStm.valid && 
    !myH2dPushStm.ready
  )
  myH2dPushStm.valid := io.bus.nextValid

  myH2dPushStm.payload := myH2dPushStm.payload.getZero
  myH2dPushStm.src.allowOverride
  myH2dPushStm.src := (
    RegNext(myH2dPushStm.src, init=myH2dPushStm.src.getZero)
  )
  when (
    myH2dPushStm.fire
    //myH2dPushStm.valid
  ) {
    myH2dPushStm.src := (
      io.bus.sendData.src
    )
  }
  myH2dPushStm.addr.allowOverride
  myH2dPushStm.addr := (
    RegNext(myH2dPushStm.addr, init=myH2dPushStm.addr.getZero)
  )
  when (
    myH2dPushStm.fire
    //myH2dPushStm.valid
  ) {
    myH2dPushStm.addr := (
      io.bus.sendData.addr
    )
  }
  //--------
  myD2hPopStm.ready := False//True

  when (myD2hPopStm.valid) {
    io.bus.ready := True
    io.bus.recvData.word := myD2hPopStm.data
    io.bus.recvData.src := myD2hPopStm.src
    myD2hPopStm.ready := True
  }
  //--------
}
private[libsnowhouse] case class SnowHouseDbusToLcvDbusBridge(
  cfg: SnowHouseConfig
) extends Component {
  //--------
  require(cfg.useLcvDataBus)
  //--------
  val io = slave(SnowHouseBusToLcvBusBridgeIo(
    cfg=cfg,
    isIbus=false,
  ))
  io.bus.ready := False//RegNext(io.ibus.ready, init=False)
  io.bus.recvData := (
    RegNext(io.bus.recvData, init=io.bus.recvData.getZero)
  )
  //--------
  def myH2dPushStm = io.lcvBus.h2dBus
  def myD2hPopStm = io.lcvBus.d2hBus

  io.h2dPushDelay := (
    //myH2dPushStm.valid && 
    !myH2dPushStm.ready
  )
  myH2dPushStm.valid := io.bus.nextValid

  myH2dPushStm.payload := myH2dPushStm.payload.getZero
  myH2dPushStm.mainNonBurstInfo.allowOverride
  myH2dPushStm.mainNonBurstInfo := (
    RegNext(
      myH2dPushStm.mainNonBurstInfo,
      init=myH2dPushStm.mainNonBurstInfo.getZero,
    )
  )
  //myH2dPushStm.src.allowOverride
  //myH2dPushStm.src := (
  //  RegNext(myH2dPushStm.src, init=myH2dPushStm.src.getZero)
  //)
  //when (
  //  myH2dPushStm.fire
  //  //myH2dPushStm.valid
  //) {
  //  myH2dPushStm.src := (
  //    io.bus.sendData.src
  //  )
  //}
  ////myH2dPushStm.addr.allowOverride
  //myH2dPushStm.addr := (
  //  RegNext(myH2dPushStm.addr, init=myH2dPushStm.addr.getZero)
  //)
  //when (
  //  myH2dPushStm.fire
  //  //myH2dPushStm.valid
  //) {
  //  myH2dPushStm.addr := (
  //    io.bus.sendData.addr
  //  )
  //}
  myH2dPushStm.byteEn := (
    U(myH2dPushStm.byteEn.getWidth bits, default -> True)
  )
  when (
    myH2dPushStm.fire
    //myH2dPushStm.valid
  ) {
    myH2dPushStm.src := io.bus.sendData.src
    myH2dPushStm.addr := io.bus.sendData.addr
    myH2dPushStm.data := io.bus.sendData.data
    myH2dPushStm.isWrite := io.bus.sendData.accKind.asBits(1)
    //myH2dPushStm.byteEn := (
    //  U(myH2dPushStm.byteEn.getWidth bits, default -> True)
    //)
    // TODO: support smaller-than-word-size loads/stores
  }
  //--------
  myD2hPopStm.ready := False//True

  when (myD2hPopStm.valid) {
    io.bus.ready := True
    io.bus.recvData.word := myD2hPopStm.data
    io.bus.recvData.src := myD2hPopStm.src
    myD2hPopStm.ready := True
  }
  //--------
}
private[libsnowhouse] case class SnowHouseBusBridgeCtrlIo(
  cfg: SnowHouseConfig,
  isIbus: Boolean,
) extends Bundle {
  //val bridgeIo = master(SnowHouseBusToLcvBusBridgeIo(
  //  cfg=cfg,
  //  isIbus=isIbus,
  //))
  val bridgeBus = master(
    new LcvStallIo[BusHostPayload, BusDevPayload](
      sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=isIbus)),
      recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=isIbus)),
    )
  )
  val bridgeH2dPushDelay = in(Bool())

  val myUpFireIshCond = in(Bool())
  //val myHaltIt = out(Bool())

  val cpuBus = slave(
    new LcvStallIo[BusHostPayload, BusDevPayload](
      sendPayloadType=Some(BusHostPayload(
        cfg=cfg,
        isIbus=isIbus,
        inCpu=true,
      )),
      recvPayloadType=Some(BusDevPayload(
        cfg=cfg,
        isIbus=isIbus,
        inCpu=true,
      )),
    )
  )
}
private[libsnowhouse] case class SnowHouseBusBridgeCtrl(
  cfg: SnowHouseConfig,
  isIbus: Boolean,
) extends Component {
  val io = SnowHouseBusBridgeCtrlIo(
    cfg=cfg,
    isIbus=isIbus,
  )
  //def lcvBus = io.bridgeIo.lcvBus
  //def bridgeBus = (
  //  //io.bridgeIo.bus
  //  io.bridgeBus
  //)
  //def myReadyIshCond = io.myReadyIshCond
  val rStallState = (
    Reg(Bool(), init=False)
  )

  val nextSrc = (
    cloneOf(io.bridgeBus.sendData.src.asSInt)
  )
  val rSrc = (
    RegNext(nextSrc)
    init(
      //0x0
      //0xf
      -2
    )
  )
  nextSrc := rSrc
  io.bridgeBus.nextValid := io.cpuBus.nextValid
  //io.bridgeBus.sendData := io.cpuBus.sendData
  //io.bridgeBus.sendData <> io.cpuBus.sendData
  io.bridgeBus.sendData.nonSrc := io.cpuBus.sendData.nonSrc
  io.bridgeBus.sendData.src.allowOverride
  io.bridgeBus.sendData.src := rSrc.asUInt
  when (io.myUpFireIshCond) {
    nextSrc := rSrc + 1
  } otherwise {
    val tempRnw = (
      RegNextWhen(
        next=rSrc,
        cond=io.myUpFireIshCond,
      )
      init(-2)
    )
    io.bridgeBus.sendData.src := tempRnw.asUInt
  }

  val tempCond = Vec.fill(2)(Bool())
  case class ExtraBusReadyPayload(
  ) extends Bundle {
    val busRdWord = Vec.fill(2)(
      Flow(cloneOf(io.bridgeBus.recvData.word))
    )
    val src = Vec.fill(2)(
      cloneOf(io.bridgeBus.recvData.src)
    )

    val myCurrIdx = UInt(log2Up(busRdWord.size) bits)

    def myOtherIdx = Cat(!myCurrIdx.lsb).asUInt
    def myCurrBusRdWord = busRdWord(myCurrIdx).payload
    def myOtherBusRdWord = busRdWord(myOtherIdx).payload
    def myCurrFire = busRdWord(myCurrIdx).valid
    def myOtherFire = busRdWord(myOtherIdx).valid

    def myCurrSrc = src(myCurrIdx)
    def myOtherSrc = src(myOtherIdx)
  }
  val rHadExtraIbusReady = {
    val temp = Reg(ExtraBusReadyPayload())
    temp.init(temp.getZero)
    temp
  }
  val myZeroStallStateHaltItCond = Bool()
  myZeroStallStateHaltItCond := !io.bridgeBus.ready
  tempCond(1) := (
    !History[Bool](
      that=False,
      when=(
        io.myUpFireIshCond
      ),
      length=8,
      init=True,
    ).last
  )
  val myTempCond = Bool()
  val rMyTempSrc = (
    Reg(cloneOf(io.bridgeBus.recvData.src))
    init(0x0)
  )
  myTempCond := (
    io.bridgeBus.recvData.src
    =/= rMyTempSrc
  )
  //io.busRdWord := RegNext(io.busRdWord, init=io.busRdWord.getZero)
  //io.myHaltIt := RegNext(io.myHaltIt, init=io.myHaltIt.getZero)
  io.cpuBus.recvData := (
    RegNext(io.cpuBus.recvData, init=io.cpuBus.recvData.getZero)
  )
  io.cpuBus.ready := (
    //io.bridgeBus.ready
    True
  )

  switch (
    rStallState
    ## rHadExtraIbusReady.myCurrFire
    ## (
      myZeroStallStateHaltItCond
      || (
        !myTempCond
        && History[Bool](
          that=True,
          when=io.myUpFireIshCond,
          length=5,
          init=False,
        ).last
      )
    )
  ) {
    is (M"01-") {
      when (io.myUpFireIshCond) {
        io.cpuBus.recvData.word := rHadExtraIbusReady.myCurrBusRdWord
        rHadExtraIbusReady.myCurrFire := False
        rHadExtraIbusReady.myCurrIdx.lsb := (
          !rHadExtraIbusReady.myCurrIdx.lsb
        )
      }
      when (
        !rHadExtraIbusReady.myOtherFire
        && rMyTempSrc =/= io.bridgeBus.recvData.src
        && rHadExtraIbusReady.myCurrSrc =/= io.bridgeBus.recvData.src
      ) {
        rHadExtraIbusReady.myOtherFire := True
        rHadExtraIbusReady.myOtherBusRdWord := io.bridgeBus.recvData.word
        rHadExtraIbusReady.myOtherSrc := io.bridgeBus.recvData.src
        rMyTempSrc := io.bridgeBus.recvData.src
      }
    }
    is (M"001") {
      //cIf.haltIt()
      //io.myHaltIt := True
      io.cpuBus.ready := False
    }
    is (M"000") {
      rHadExtraIbusReady.myCurrFire := False
      rHadExtraIbusReady.myOtherFire := False
      
      rMyTempSrc := io.bridgeBus.recvData.src
      io.cpuBus.recvData.word := io.bridgeBus.recvData.word
      rStallState := True
    }
    default {
      when (
        !rHadExtraIbusReady.myCurrFire
        && io.bridgeBus.ready
        && rMyTempSrc =/= io.bridgeBus.recvData.src
      ) {
        rHadExtraIbusReady.myCurrFire := True
        rHadExtraIbusReady.myCurrBusRdWord := io.bridgeBus.recvData.word
        rHadExtraIbusReady.myCurrSrc := io.bridgeBus.recvData.src
      }
    }
  }

  when (
    if (isIbus) (
      io.bridgeH2dPushDelay
    ) else ( // if (!isIbus)
      RegNext(
        RegNext(io.cpuBus.nextValid, init=False),
        init=False
      )
      && io.bridgeH2dPushDelay
    )
  ) {
    io.cpuBus.ready := False
  }

  when (io.myUpFireIshCond) {
    rStallState := False
  }
}

case class SnowHousePipeStageInstrFetch(
  args: SnowHousePipeStageArgs,
  psIdHaltIt: Bool,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
) extends Area {
  def cfg = args.cfg
  def io = args.io
  def cIf = args.link
  def pIf = args.currPayload
  val up = cIf.up
  val down = cIf.down
  val upModExt = (
    /*KeepAttribute*/(
      SnowHousePipePayload(cfg=cfg)
    )
    .setName(s"PipeStageInstrFetch_upModExt")
  )
  up(pIf) := upModExt
  upModExt := RegNext(upModExt, init=upModExt.getZero)
  def myInstrCnt = upModExt.instrCnt
  //val myUseLcvIbusFifo = (
  //  cfg.useLcvInstrBus
  //) generate (
  //  StreamFifo(
  //    dataType=UInt(cfg.instrMainWidth bits),
  //    depth=4,
  //    latency=0,
  //    forFMax=true,
  //  )
  //)
  //val rDidFirstStallStateFall = (
  //  cfg.useLcvInstrBus
  //) generate (
  //  Reg(Bool(), init=False)
  //)
  //val myStallStateCond = (
  //  Bool()
  //)
  //val rStallState = (
  //  Reg(Bool(), init=False)
  //)
  //val myStallStateCond = Bool()
  //if (!cfg.useLcvInstrBus) {
  //  myStallStateCond := rStallState
  //} else {
  //  myStallStateCond := rStallState
  //}

  //if (cfg.useLcvInstrBus) {
  //  when (!rDidFirstStallStateFall && rStallState) {
  //    cIf.haltIt()
  //    rDidFirstStallStateFall := True
  //    rStallState := False
  //  }
  //}
  val myBridge = (
    cfg.useLcvInstrBus
  ) generate (
    SnowHouseIbusToLcvIbusBridge(cfg=cfg)
  )
  val myBridgeCtrl = (
    cfg.useLcvInstrBus
  ) generate (
    SnowHouseBusBridgeCtrl(
      cfg=cfg,
      isIbus=true,
    )
  )
  def myBusNextValid = (
    if (!cfg.useLcvInstrBus) (
      io.ibus.nextValid
    ) else (
      myBridgeCtrl.io.cpuBus.nextValid
    )
  )
  def myBusAddr = (
    if (!cfg.useLcvInstrBus) (
      io.ibus.sendData.addr
    ) else (
      myBridgeCtrl.io.cpuBus.sendData.addr
    )
  )

  //def myIbus = (
  //  if (!cfg.useLcvInstrBus) (
  //    io.ibus
  //  ) else (
  //    //myBridge.io.bus
  //    myBridgeCtrl.io.bridgeBus
  //  )
  //)
  val myReadyIshCond = Bool()
  val myReadyIshCondShared = up.isReady

  if (cfg.useLcvInstrBus) {
    io.lcvIbus <> myBridge.io.lcvBus
    myBridgeCtrl.io.bridgeBus <> myBridge.io.bus
    myBridgeCtrl.io.bridgeH2dPushDelay := myBridge.io.h2dPushDelay
    myBridgeCtrl.io.myUpFireIshCond := myReadyIshCond
    when (!myBridgeCtrl.io.cpuBus.ready) {
      cIf.haltIt()
    }
  }

  //val myReadyIshCondLcvMostCmpSrc = (
  //  cfg.useLcvInstrBus
  //) generate (
  //  //myIbus.recvData.src
  //  myIbus.sendData.src
  //  === myIbus.recvData.src + 2
  //  //=== myUseLcvIbusFifo.io.pop.src + 2
  //  //myUseLcvIbusFifo.io.pop.src 
  //  //=== myIbus.recvData.src //+ 2
  //)

  //val myReadyIshCondNonFifo = (
  //  up.isReady
  //)

  def myRegPcSetItCnt = upModExt.psIfRegPcSetItCnt
  val rPrevRegPcSetItCnt = {
    val temp = (
      RegNextWhen(
        next=myRegPcSetItCnt,
        cond=(
          //up.isFiring
          myReadyIshCond
          //myUpdatePcCond
        )
      )
      init(0x0)
    )
    temp
  }
  myRegPcSetItCnt.allowOverride
  myRegPcSetItCnt := rPrevRegPcSetItCnt

  val stickyExSetPc = {
    val temp = (
      Vec.fill(1)(
        Flow(
          SnowHousePsExSetPcPayload(cfg=cfg)
        )
      )
    )
    temp.foreach(item => {
      item := RegNext(item, init=item.getZero)
    })
    temp.setName(s"psIf_stickyExSetPc")
  }
  val branchPredictor = (
    cfg.haveBranchPredictor
  ) generate (
    SnowHouseBranchPredictor(
      //psIf=this,
      cfg=cfg
    )
  )
  if (cfg.haveBranchPredictor) {
    branchPredictor.io.psExSetPc := psExSetPc
    branchPredictor.io.upIsFiring := up.isFiring
    branchPredictor.io.upIsReady := myReadyIshCond //myUpdatePcCond
  }

  val takeJumpCntMaxVal = cfg.takeJumpCntMaxVal
  val rTakeJumpCnt = {
    val temp = Reg(Flow(UInt(
      //cfg.mainWidth bits
      log2Up(takeJumpCntMaxVal + 1) + 1 bits
    )))
    temp.init(temp.getZero)
    temp
  }

  when (rTakeJumpCnt.fire) {
    stickyExSetPc(0).valid := False
  }

  when (psExSetPc.valid) {
    stickyExSetPc.foreach(_.valid := True)
    stickyExSetPc(0).btbElemWithBrKind.allowOverride
    stickyExSetPc(0).btbElemWithBrKind := psExSetPc.btbElemWithBrKind
    stickyExSetPc(0).nextPc.allowOverride
    stickyExSetPc(0).nextPc := psExSetPc.nextPc
  }

  val myNextRegPcInit = 0
  val myRegPcShiftThing = (
    S(s"${log2Up(cfg.instrSizeBytes)}'d0")
  )
  val myPrevRegPcPlusInstrSizeWidth = (
    upModExt.regPc.getWidth - log2Up(cfg.instrSizeBytes)
  )
  val rPrevRegPc = {
    val temp = RegNextWhen(
      next=(
        Vec.fill(2)(
          upModExt.regPc.asSInt(
            upModExt.regPc.high downto log2Up(cfg.instrSizeBytes)
          )
        )
      ), 
      cond=(
        //up.isFiring
        //up.isReady
        myReadyIshCond
        //myReadyIshCondMaybeDel1
        //myUpdatePcCond
      ),
    )
    temp.foreach(item => {
      item.init(item.getZero)
      //item.init(-(1 * cfg.instrSizeBytes))
      //item.init(-1)
    })
    temp
  }
  //rPrevRegPcPlusInstrSize.addAttribute("use_dsp", "yes")
  upModExt.encInstr.allowOverride
  upModExt.encInstr := (
    RegNext(
      next=upModExt.encInstr,
      init=upModExt.encInstr.getZero,
    )
  )
  //myIbus.sendData.addr := (
  //  RegNext(
  //    next=myIbus.sendData.addr,
  //    init=myIbus.sendData.addr.getZero,
  //  )
  //)
  myBusAddr := RegNext(myBusAddr, init=myBusAddr.getZero)
  upModExt.regPc := (
    RegNext(
      next=upModExt.regPc,
      init=upModExt.regPc.getZero,
    )
  )
  val myHistRegPc = (
    History[UInt](
      that=(
        // TODO: check that this is correct
        upModExt.regPc
      ),
      length=3,
      when=(
        myReadyIshCond
        //myUpdatePcCond
      ),
      init=upModExt.regPc.getZero,
    )
  )
  upModExt.laggingRegPc.allowOverride
  upModExt.laggingRegPc := (
    myHistRegPc.last
  )
  val myMainPredictCond = (
    branchPredictor.io.result.fire
    && !rTakeJumpCnt.fire
  )
  val predictCond = (
    cfg.haveBranchPredictor
  ) generate (
    myMainPredictCond
  )

  if (cfg.haveBranchPredictor) {
    for (idx <- 0 until branchPredictor.io.inpRegPc.size) {
      branchPredictor.io.inpRegPc(idx) := (
        Cat(
          (rPrevRegPc(0) + 1),
          myRegPcShiftThing,
        ).asUInt
      )
    }
  }
  val tempNextRegPc = (
    cfg.haveBranchPredictor
  ) generate (
    Cat(
      rPrevRegPc.last + 1,
      myRegPcShiftThing,
    ).asSInt
  )
  val myTempNextRegPcMaybeDel1 = (
    cfg.haveBranchPredictor
  ) generate (
    tempNextRegPc
  )
  val myPredictedNextPc = (
    cfg.haveBranchPredictor
  ) generate (
    Mux[SInt](
      predictCond,
      branchPredictor.io.result.nextRegPc.asSInt,
      (
        //tempNextRegPc
        myTempNextRegPcMaybeDel1
      ),
    ).asUInt
  )
  def doInitTakeJumpCnt(): Unit = {
    rTakeJumpCnt.valid := True
    rTakeJumpCnt.payload := takeJumpCntMaxVal
  }

  //myIbus.nextValid := (
  //  if (!cfg.useLcvInstrBus) (
  //    True
  //  ) else (
  //    True
  //  )
  //)
  myBusNextValid := True

  val myUpdateRegPcCondUInt = (
    Cat(
      List(
        myReadyIshCond,
        //myUpdatePcCond,
        stickyExSetPc.head.fire,
      ).reverse
    )
  )
  case class MyIbusRegPcInfo(
  ) extends Bundle {
    val regPc = UInt(cfg.mainWidth bits)
    val branchPredictTkn = (
      cfg.haveBranchPredictor
    ) generate (
      Bool()
    )
    val branchTgtBufElem = (
      cfg.haveBranchPredictor
    ) generate (
      Vec.fill(2)(
        BranchTgtBufElem(cfg=cfg)
      )
    )
    def setUpModExt(
    ): Unit = {
      upModExt.regPc := this.regPc
      if (cfg.haveBranchPredictor) {
        upModExt.branchPredictTkn := this.branchPredictTkn
        upModExt.branchTgtBufElem := this.branchTgtBufElem
      }
    }
  }
  val myIbusRegPcInfo = MyIbusRegPcInfo()
  myIbusRegPcInfo := (
    // set everything to zero for debugging purposes
    myIbusRegPcInfo.getZero
  )
  for (idx <- 0 until stickyExSetPc.size) {
    def doPsExSetPcValid(
      useStickyNextPc: Boolean
    ): Unit = {
      doInitTakeJumpCnt()

      val temp = (
        if (useStickyNextPc) (
          stickyExSetPc(0).nextPc
        ) else (
          psExSetPc.nextPc
        )
      )
      val tempNextRegPc = temp
      myIbusRegPcInfo.regPc := tempNextRegPc//.asUInt
      if (cfg.haveBranchPredictor) {
        myIbusRegPcInfo.branchPredictTkn := False
      }
      //if (!cfg.useLcvInstrBus) {
        myIbusRegPcInfo.setUpModExt()
      //}
      //myIbus.sendData.addr := tempNextRegPc//.asUInt
      myBusAddr := tempNextRegPc//.asUInt
    }
    switch (myUpdateRegPcCondUInt) {
      is (M"0-") {
      }
      is (M"10") {
        if (cfg.haveBranchPredictor) {
          val temp = myPredictedNextPc
          //myIbus.sendData.addr := temp
          myBusAddr := temp
          myIbusRegPcInfo.regPc := temp
          myIbusRegPcInfo.branchPredictTkn.allowOverride
          myIbusRegPcInfo.branchPredictTkn := (
            //predictCond
            myMainPredictCond
          )
          myIbusRegPcInfo.branchTgtBufElem.foreach(item => {
            item := branchPredictor.io.result.rdBtbElem
          })
        } else {
          val temp = (
            Cat(
              rPrevRegPc.last + 1,
              myRegPcShiftThing,
            ).asUInt
          )
          //myIbus.sendData.addr := temp
          myBusAddr := temp
          myIbusRegPcInfo.regPc := temp
        }
        //if (!cfg.useLcvInstrBus) {
          myIbusRegPcInfo.setUpModExt()
        //}
      }
      //is (M"1-1") {
      //  doPsExSetPcValid(
      //    useStickyNextPc=true
      //  )
      //}
      default {
        doPsExSetPcValid(
          useStickyNextPc=(
            //false
            true
          )
        )
      }
    }
  }
  upModExt.regPc.allowOverride

  //val rRegPcStallState = Reg(Bool(), init=False)
  //object MyLcvIbusState
  //extends SpinalEnum(defaultEncoding=binaryOneHot) {
  //  val
  //    IDLE,
  //    POST_HALT_IT,
  //    POST_PS_EX_SET_PC
  //    = newElement();
  //}

  myReadyIshCond := (
    myReadyIshCondShared
  )
  val myNonLcvIbusStallArea = (
    !cfg.useLcvInstrBus
  ) generate (new Area {
    val rStallState = (
      Reg(Bool(), init=False)
    )
    when (!rStallState) {
      when (!io.ibus.ready) {
        cIf.haltIt()
        //cIf.duplicateIt()
      } otherwise {
        upModExt.encInstr.payload := io.ibus.recvData.word
        rStallState := True
      }
    }
    when (myReadyIshCond) {
      rStallState := False
    }
  })

  val myLcvIbusStallStateArea = (
    cfg.useLcvInstrBus   
  ) generate (new Area {
    upModExt.encInstr.payload := myBridgeCtrl.io.cpuBus.recvData.word
  })
  when (myReadyIshCond) {
    myRegPcSetItCnt := 0x0
    when (rTakeJumpCnt.fire) {
      rTakeJumpCnt.payload := rTakeJumpCnt.payload - 1
      when (rTakeJumpCnt.payload.msb) {
        rTakeJumpCnt.valid := False
        myRegPcSetItCnt := 0x1
      } otherwise {
      }
    }
  }
}
//case class SnowHouseDspAddSubHistoryIo(
//  width: Int,
//  size: Int,
//  optIncludeCond: Boolean,
//) extends Bundle {
//  val inp = new Bundle {
//    val a = in(SInt(width bits))
//    val b = in(SInt(width bits))
//    val cond = (optIncludeCond) generate (
//      in(Bool())
//    )
//  }
//  val outp = new Bundle {
//    val myHistSumCarry = out(Vec.fill(size)(SInt(width + 1 bits)))
//  }
//}
//case class SnowHouseDspAddSubHistory(
//  width: Int,
//  size: Int,
//  optIncludeCond: Boolean,
//  isSub: Boolean,
//) extends Component {
//  addAttribute("use_dsp", "yes")
//  val io = SnowHouseDspAddSubHistoryIo(
//    width=width,
//    size=size,
//    optIncludeCond=optIncludeCond,
//  )
//  //io.outp.sumCarry.setAsReg() init(io.outp.sumCarry.getZero)
//  val myHistThat = (
//    if (!isSub) (
//      Cat(False, io.inp.a).asSInt + Cat(False, io.inp.b).asSInt
//    ) else (
//      Cat(False, io.inp.a).asSInt - Cat(False, io.inp.b).asSInt
//    )
//  )
//  myHistThat.addAttribute("use_dsp", "yes")
//  io.outp.myHistSumCarry := (
//    if (optIncludeCond) (
//      History[SInt](
//        that=myHistThat,
//        length=size,
//        when=io.inp.cond,
//        init=myHistThat.getZero,
//      )
//    ) else (
//      History[SInt](
//        that=myHistThat,
//        length=size,
//        init=myHistThat.getZero,
//      )
//    )
//  )
//  io.outp.myHistSumCarry.addAttribute("use_dsp", "yes")
//  //io.outp.sumCarry := (
//  //  RegNextWhen(
//  //    next=(
//  //      if (!isSub) (
//  //        Cat(False, io.inp.a).asSInt + Cat(False, io.inp.b).asSInt
//  //      ) else (
//  //        Cat(False, io.inp.a).asSInt - Cat(False, io.inp.b).asSInt
//  //      )
//  //    ),
//  //    cond=io.inp.cond,
//  //    init=io.outp.sumCarry.getZero,
//  //  )
//  //)
//}
case class SnowHousePipeStageInstrDecode(
  val args: SnowHousePipeStageArgs,
  val psIdHaltIt: Bool,
  val psExSetPc: Flow[SnowHousePsExSetPcPayload],
  val pcChangeState: Bool/*UInt*/,
  val shouldIgnoreInstr: Bool,
  val doDecodeFunc: (SnowHousePipeStageInstrDecode) => Area,
) extends Area {
  def cfg = args.cfg
  def modIo = args.io
  def pIf = args.prevPayload
  def pId = args.currPayload
  def opInfoMap = cfg.opInfoMap
  def io = args.io
  def cId = args.link
  def payload = args.currPayload
  def optFormal = cfg.optFormal
  def regFile = args.regFile
  val up = cId.up
  val down = cId.down
  val upPayload = Vec.fill(2)(
    SnowHousePipePayload(cfg=cfg)
  )
  val startDecode = Reg(Bool(), init=False)

  //val rSavedExSetPc = {
  //  val temp = /*KeepAttribute*/(
  //    Reg(Flow(
  //      SnowHousePsExSetPcPayload(cfg=cfg)
  //    ))
  //  )
  //  temp.init(temp.getZero)
  //  temp.setName(s"psId_rSavedExSetPc")
  //}

  val rShouldFinishJumpCnt = (
    Reg(UInt(4 bits))
    init(
      //0x2
      0xf
    )
  )
  //when (!io.ibus.ready) {
  //  cId.haltIt()
  //}

  //when (
  //  //up.isFiring
  //  //&& 
  //  psExSetPc.valid
  //) {
  //  rSavedExSetPc.valid := True
  //  rSavedExSetPc.nextPc := (
  //    psExSetPc.nextPc //- (cfg.instrMainWidth.toLong / 8.toLong).toLong
  //  )
  //  //rShouldFinishJumpCnt := 0x2
  //}

  //when (
  //  upPayload(1).psIfRegPcSetItCnt(0)
  //) {
  //  rShouldFinishJumpCnt := 0x1
  //}

  //rSavedExSetPc.payload := psExSetPc.payload
  //rSavedExSetPc.nextPc.allowOverride

  //when (up.isFiring) {
  up(pId) := upPayload(1)//(0)
  //}
  val nextSetUpPayloadState = Vec.fill(2)(
    Bool()
  )
  val rSetUpPayloadState = {
    val temp = RegNext(
      nextSetUpPayloadState,
    )
    for (idx <- 0 until nextSetUpPayloadState.size) {
      temp(idx).init(temp(idx).getZero)
    }
    temp
  }
  for (idx <- 0 until rSetUpPayloadState.size) {
    nextSetUpPayloadState(idx) := rSetUpPayloadState(idx)
  }
  upPayload.foreach(item => {
    item := (
      RegNext(
        next=item,
        init=item.getZero,
      )
      init(item.getZero)
    )
  })
  upPayload.allowOverride

  val multiInstrCntWidth = (
    //3
    2
  )
  val nextMultiInstrCnt = UInt(multiInstrCntWidth bits)
  val rMultiInstrCnt = (
    RegNext(
      next=nextMultiInstrCnt,
      init=(
        U(
          multiInstrCntWidth bits,
          default -> False //True
        )
      )
    )
  )
  nextMultiInstrCnt := rMultiInstrCnt
  val myIraPc = UInt(cfg.mainWidth bits)
  val myInstr = UInt(cfg.instrMainWidth bits)
  //myInstr := myInstr
  //when (upPayload(1).encInstr.fire) {
    myInstr := upPayload(1).encInstr.payload
  //} otherwise {
  //  //myInstr := myInstr.getZero
  //  cId.throwIt()
  //}
  upPayload(0) := up(pIf)
  when (up.isValid) {
    upPayload(1) := upPayload(0)
  }
  val shouldFinishJump = (
    //--------
    //upPayload(1).psIfRegPcSetItCnt(0)
    RegNextWhen(
      next=upPayload(1).psIfRegPcSetItCnt(0),
      cond=(
        up.isFiring
        //up.isValid
      ),
      init=upPayload(1).psIfRegPcSetItCnt(0).getZero,
    )
    //--------
    //upPayload.psIfRegPcSetItCnt(0)
    //Bool()
  )
  //shouldFinishJump := (
  //  RegNext(
  //    next=shouldFinishJump,
  //    init=False,
  //  )
  //)
  //when (up.isValid) {
  //  when (!rShouldFinishJumpState) {
  //    shouldFinishJump := upPayload.psIfRegPcSetItCnt(0)
  //    rShouldFinishJumpState := True
  //  }
  //}
  //when (up.isFiring) {
  //  rShouldFinishJumpState := False
  //}

  //shouldFinishJump := upPayload.psIfRegPcSetItCnt(0)

  for (idx <- 0 until upPayload(1).regPcSetItCnt.size) {
    //val rPrevRegPcSetItCnt = (
    //  RegNextWhen(
    //    next=upPayload(1).regPcSetItCnt(idx),
    //    cond=up.isFiring,
    //    init=upPayload(1).regPcSetItCnt(idx).getZero,
    //  )
    //)
    when (up.isFiring) {
      when (
        (
          shouldFinishJump
        )
      ) {
        upPayload(1).regPcSetItCnt(idx) := (
          //0x2
          0x1
        )
      } 
      //elsewhen (
      //  //RegNextWhen(
      //  //  next=(upPayload(1).regPcSetItCnt(idx) > 0),
      //  //  cond=up.isFiring,
      //  //  init=False,
      //  //)
      //  rPrevRegPcSetItCnt > 0
      //)
      .otherwise {
        upPayload(1).regPcSetItCnt(idx) := 0x0
        //upPayload(1).regPcSetItCnt(idx)
        //upPayload(1).regPcSetItCnt(idx) := (
        //  rPrevRegPcSetItCnt - 1
        //)
      }
    }
  }
  when (up.isFiring) {
    //nextSetUpPayloadState(0) := False
    nextSetUpPayloadState(1) := False
    //when (
    //  //modIo.ibus.fire
    //  //&& 
    //  !rShouldFinishJumpCnt.msb
    //) {
    //  rShouldFinishJumpCnt := rShouldFinishJumpCnt - 1
    //}
  }
  //when (up.isFiring) {
  //  upPayload.regPcSetItCnt.foreach(_ := upPayload.psIfRegPcSetItCnt)
  //}
  //upPayload(1).regPc
  upPayload(1).regPcPlusInstrSize := (
    //upPayload(1).regPc - (1 * cfg.instrSizeBytes) //- cfg.instrSizeBytes
    upPayload(1).branchTgtBufElem(1).srcRegPc
    + (1 * cfg.instrSizeBytes)
    //+ (2 * cfg.instrSizeBytes)
    //+ (2 * cfg.instrSizeBytes)
    //+ (2 * cfg.instrSizeBytes)
    ////- (cfg.instrMainWidth.toLong / 8.toLong)
    //upPayload.regPcPlus1Instr
  )
  upPayload(1).branchPredictReplaceBtbElem := (
    RegNextWhen(
      next=upPayload(1).branchPredictTkn,
      cond=cId.up.isFiring,
      init=upPayload(1).branchPredictTkn.getZero,
    )
    //&& upPayload(0).branchTgtBufElem(0).fire
    && upPayload(1).branchTgtBufElem(1).fire
    && !upPayload(1).branchTgtBufElem(1).dontPredict

    && upPayload(1).btbElemBranchKind(1).asBits(0)
    && (
      !LcvFastCmpEq(
        left=(
          upPayload(1).branchTgtBufElem(0).srcRegPc(
            cfg.mySrcRegPcCmpEqRange
          )
        ),
        right=(
          upPayload(1).branchTgtBufElem(1).srcRegPc(
            cfg.mySrcRegPcCmpEqRange
          )
        ),
        cmpEqIo=(
          null
        ),
        optDsp=(
          false,
        ),
        optReg=false,
      )._1
      //(
      //  upPayload(1).branchTgtBufElem(0).srcRegPc(
      //    cfg.mySrcRegPcCmpEqRange
      //  ) =/= upPayload(1).branchTgtBufElem(1).srcRegPc(
      //    cfg.mySrcRegPcCmpEqRange
      //  )
      //)
      //|| (
      //  upPayload(1).branchTgtBufElem(0).dstRegPc
      //  =/= upPayload(1).branchTgtBufElem(1).srcRegPc
      //)
    )
  )
  //val myPredictTkn = (
  //  upPayload(1).branchPredictTkn
  //  && upPayload(1).branchTgtBufElem(0).valid
  //  && !upPayload(1).branchTgtBufElem(0).dontPredict
  //)
  //val myHistRegPcPlusImmCond = (
  //)
  //val myTempRegPcPlusInstrSize = /*Flow*/(UInt(cfg.mainWidth bits))
  //val myHistRegPcPlusInstrSize = (
  //  History[/*Flow[*/UInt/*]*/](
  //    that=myTempRegPcPlusInstrSize,
  //    length=upPayload(1).myHistRegPcPlusInstrSize.size,
  //    when=up.isFiring,
  //    init=myTempRegPcPlusInstrSize.getZero,
  //  )
  //)
  //myTempRegPcPlusInstrSize/*.payload*/ := (
  //  upPayload(1).regPc + (1 * cfg.instrSizeBytes)
  //)
  ////myTempRegPcPlusImm.valid := myPredictTkn
  //upPayload(1).myHistRegPcPlusInstrSize := (
  //  myHistRegPcPlusInstrSize
  //)
  def myRegPcRange = (
    upPayload(1).regPc.high downto log2Up(cfg.instrSizeBytes)
  )
  val myHistRegPc = (
    History[SInt](
      that=upPayload(1).regPc(myRegPcRange).asSInt,
      length=upPayload(1).myHistRegPcSize,
      when=up.isFiring,
      init=upPayload(1).regPc(myRegPcRange).asSInt.getZero,
    )
  )
  //val myHistRegPcMinus2InstrSize = (
  //  History[UInt](
  //    that=(
  //      upPayload(1).regPc - (2 * cfg.instrSizeBytes)
  //    ),
  //    length=upPayload(1).myHistRegPc.size,
  //    when=up.isFiring,
  //    init=upPayload(1).regPc.getZero,
  //  )
  //)
  //val myHistRegPcPlus1InstrSize = (
  //  History[UInt](
  //    that=(
  //      upPayload(1).regPc + (1 * cfg.instrSizeBytes)
  //    ),
  //    length=upPayload(1).myHistRegPc.size,
  //    when=up.isFiring,
  //    init=upPayload(1).regPc.getZero,
  //  )
  //)
  //upPayload(1).myHistRegPc := myHistRegPc
  //val myHistRegPc = (
  //  History[UInt](
  //    that=upPayload(1).regPc,
  //    length=(
  //      //upPayload(1).myHistRegPc.size
  //      //3
  //      upPayload(1).myHistRegPcSize
  //    ),
  //    when=up.isFiring,
  //    init=upPayload(1).regPc.getZero,
  //  )
  //)

  val myDspRegPcMinus2InstrSize = {
    val myWordWidth = (
      cfg.mainWidth - log2Up(cfg.instrSizeBytes) //- 1
    )
    //LcvCondSubDel1(
    //  wordWidth=myWordWidth
    //)
    //LcvSubDel1(
    //  wordWidth=myWordWidth
    //)
    new Area {
      val wordWidth = myWordWidth
      val io = (
        //LcvCondAddJustCarryDel1Io(wordWidth=wordWidth)
        new Bundle {
          val inp = new Bundle {
            val a = SInt(wordWidth bits)
            val carry = Bool()
            val cond = Bool()
          }
          val outp = new Bundle {
            val sum_carry = SInt(wordWidth + 1 bits)
          }
        }
      )
      val tempA = Cat(False, io.inp.a(io.inp.a.high downto 1)).asSInt
      //val tempB = Cat(False, io.inp.b(io.inp.b.high downto 1)).asSInt
      val tempCarry = Cat(
        U(s"${wordWidth - 1}'d0"), 
        io.inp.carry
      ).asSInt

      val myTempSumCarry = (
        Cat(
          //Cat(False, io.inp.a).asSInt - Cat(False, io.inp.b).asSInt
          (tempA - tempCarry),
          io.inp.a(0),
        ).asSInt
      )
      //if (!cfg.useLcvInstrBus) {
        io.outp.sum_carry := (
          RegNextWhen(
            next=myTempSumCarry,
            cond=io.inp.cond,
          )
          init(0x0)
        )
      //} else {
      //  io.outp.sum_carry := (
      //    RegNext(io.outp.sum_carry, init=io.outp.sum_carry.getZero)
      //  )
      //  when (io.inp.cond) {
      //    io.outp.sum_carry := myTempSumCarry
      //  }
      //}
    }
  }
  val myHistRegPcMinus2InstrSize = (
    Vec.fill(upPayload(1).myHistRegPcSize - 1)(
      SInt(
        //cfg.mainWidth - log2Up(cfg.instrSizeBytes)
        myDspRegPcMinus2InstrSize.wordWidth
        bits
      )
    )
  )
  myDspRegPcMinus2InstrSize.io.inp.a := (
    //myHistRegPc(1)
    //myHistRegPc(0)(myHistRegPc(0).high downto 1)
    myHistRegPc(0)
  )
  myDspRegPcMinus2InstrSize.io.inp.carry := True
  myDspRegPcMinus2InstrSize.io.inp.cond := up.isFiring
  for (idx <- 0 until myHistRegPcMinus2InstrSize.size) {
    if (idx == 0) {
      myHistRegPcMinus2InstrSize(idx) := (
        myDspRegPcMinus2InstrSize.io.outp.sum_carry(
          myHistRegPcMinus2InstrSize(idx).bitsRange
        )
      )
    } else {
      myHistRegPcMinus2InstrSize(idx) := (
        RegNext(
          next=myHistRegPcMinus2InstrSize(idx),
          init=myHistRegPcMinus2InstrSize(idx).getZero,
        )
      )
      when (RegNext(next=up.isFiring, init=False)) {
        myHistRegPcMinus2InstrSize(idx) := (
          RegNext(
            next=myHistRegPcMinus2InstrSize(idx - 1),
            init=myHistRegPcMinus2InstrSize(idx - 1).getZero,
          )
        )
      }
    }
  }
  val myDspRegPcPlus1InstrSize = {
    val myWordWidth = (
      cfg.mainWidth - log2Up(cfg.instrSizeBytes)
    )
    //LcvCondAddJustCarryDel1(
    //  wordWidth=myWordWidth
    //)
    //LcvAddJustCarryDel1(
    //  wordWidth=myWordWidth
    //)
    new Area {
      val wordWidth = myWordWidth
      val io = new Bundle {
        val inp = new Bundle {
          val a = SInt(wordWidth bits)
          //val b = SInt(wordWidth bits)
          val carry = Bool()
          val cond = Bool()
        }
        val outp = new Bundle {
          val sum_carry = SInt(wordWidth + 1 bits)
        }
      }
      val tempA = Cat(False, io.inp.a).asSInt
      //val tempB = Cat(False, io.inp.b).asSInt
      val tempCarry = Cat(
        U(s"${wordWidth}'d0"), 
        io.inp.carry
      ).asSInt
      val myTempSumCarry = tempA + tempCarry

      //if (!cfg.useLcvInstrBus) {
        io.outp.sum_carry := (
          RegNextWhen(
            next=myTempSumCarry,
            cond=io.inp.cond,
          )
          init(0x0)
        )
      //} else {
      //  io.outp.sum_carry := (
      //    RegNext(io.outp.sum_carry, init=io.outp.sum_carry.getZero)
      //  )
      //  when (io.inp.cond) {
      //    io.outp.sum_carry := myTempSumCarry
      //  }
      //}
    }
  }
  val myHistRegPcPlus1InstrSize = (
    Vec.fill(
      upPayload(1).myHistRegPcSize - 1
    )(
      SInt(
        cfg.mainWidth - log2Up(cfg.instrSizeBytes)
        bits
      )
    )
  )
  myDspRegPcPlus1InstrSize.io.inp.a := (
    //myHistRegPc(1)
    myHistRegPc(0)
  )
  myDspRegPcPlus1InstrSize.io.inp.carry := True
  myDspRegPcPlus1InstrSize.io.inp.cond := up.isFiring
  for (idx <- 0 until myHistRegPcPlus1InstrSize.size) {
    if (idx == 0) {
      myHistRegPcPlus1InstrSize(idx) := (
        myDspRegPcPlus1InstrSize.io.outp.sum_carry(
          myHistRegPcPlus1InstrSize(idx).bitsRange
        )
      )
    } else {
      myHistRegPcPlus1InstrSize(idx) := (
        RegNext(
          next=myHistRegPcPlus1InstrSize(idx),
          init=myHistRegPcPlus1InstrSize(idx).getZero,
        )
      )
      when (RegNext(next=up.isFiring, init=False)) {
        myHistRegPcPlus1InstrSize(idx) := (
          RegNext(
            next=myHistRegPcPlus1InstrSize(idx - 1),
            init=myHistRegPcPlus1InstrSize(idx - 1).getZero,
          )
        )
      }
    }
  }
  def laggingRegPcMinus2InstrSize = (
    (
      myHistRegPcMinus2InstrSize.last(
        myHistRegPcMinus2InstrSize.last.high - 1 downto 0
      )
    )
  )
  upPayload(1).regPcPlusImm := 0x0
  upPayload(1).regPcPlusImm.allowOverride
  upPayload(1).regPcPlusImm(myRegPcRange) := (
    (
      laggingRegPcMinus2InstrSize//.asSInt
      //+ (if (!cfg.useLcvInstrBus) (0) else (1))
      + (
        upPayload(1).imm(2).asSInt
      )
    ).asUInt.resize(upPayload(1).regPcPlusImm(myRegPcRange).getWidth)
    //- (cfg.instrMainWidth.toLong / 8.toLong)
  )
  val upGprIdxToMemAddrIdxMap = upPayload(1).gprIdxToMemAddrIdxMap
  for ((gprIdx, zdx) <- upPayload(1).gprIdxVec.view.zipWithIndex) {
    upPayload(1).myExt(0).memAddr(zdx) := gprIdx
  }

  val tempInstr = UInt(cfg.instrMainWidth bits)
  tempInstr := (
    RegNext(
      next=tempInstr,
      init=tempInstr.getZero,
    )
  )
  tempInstr.allowOverride
  //startDecode := False
  if (cfg.irqCfg != None) {
    upPayload(1).takeIrq := False
  }
  upPayload(1).irqIraRegPc := (
    //upPayload(1).regPc
    upPayload(1).laggingRegPc
  )
  val myDecodeAreaWithoutUcode = (
    !cfg.supportUcode
  ) generate(
    doDecodeFunc(this)
  )
  startDecode := True
  tempInstr := myInstr
}
//private[libsnowhouse] object PcChangeState
//extends SpinalEnum(defaultEncoding=binarySequential) {
//  val
//    Idle,
//    WaitTwoInstrs
//    = newElement()
//}
case class SnowHousePipeStageExecuteSetOutpModMemWordIo(
  cfg: SnowHouseConfig,
  isComponentIo: Boolean=false
) extends Bundle {
  private def setAsInp[T <: Data](
    signal: T
  ): T = {
    if (isComponentIo) (
      in(signal)
    ) else (
      signal
    )
  }
  private def setAsOutp[T <: Data](
    signal: T
  ): T = {
    if (isComponentIo) (
      out(signal)
    ) else (
      signal
    )
  }
  val multiCycleBusRecvDataVec = (
    cfg.havePsExStall
  ) generate (
    setAsInp(Vec[MultiCycleDevPayload]{
      val tempArr = ArrayBuffer[
        MultiCycleDevPayload
      ]()
      for (
        ((_, opInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
      ) {
        assert(
          opInfo.select == OpSelect.MultiCycle
        )
        tempArr += (
          MultiCycleDevPayload(cfg=cfg, opInfo=opInfo)
        )
      }
      tempArr
    })
  )
  val currOp = setAsInp(UInt(log2Up(cfg.opInfoMap.size) bits))
  val inMultiCycleOp = setAsInp(Bool())
  val splitOp = setAsInp(SnowHouseSplitOp(cfg=cfg))
  val tempVecSize = 2 // TODO: temporary size of `2`
  val gprIsZeroVec = (
    cfg.myHaveZeroReg
  ) generate (
    setAsInp(
      Vec.fill(tempVecSize + 1)(
        Vec.fill(cfg.regFileCfg.modMemWordValidSize)(
          Bool()
        )
      )
    )
  )
  val gprIsNonZeroVec = (
    cfg.myHaveZeroReg
  ) generate (
    setAsInp(
      Vec.fill(tempVecSize + 1)(
        Vec.fill(cfg.regFileCfg.modMemWordValidSize + 1)(
          Bool()
        )
      )
    )
  )
  val dbusHostPayload = (
    setAsOutp(BusHostPayload(cfg=cfg, isIbus=false))
  )
  val rdMemWord = setAsInp(Vec.fill(tempVecSize)(
    UInt(cfg.mainWidth bits)
  ))
  val regPc = setAsInp(UInt(cfg.mainWidth bits))
  val mySavedRegPcPlusInstrSize = setAsInp(
    /*Flow*/(UInt(cfg.mainWidth bits))
  )
  val laggingRegPc = setAsInp(UInt(cfg.mainWidth bits))
  val regPcSetItCnt = setAsInp(Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
    UInt(
      //1 bits
      cfg.regPcSetItCntWidth bits
    )
  ))
  val upIsFiring = setAsInp(Bool())
  val upIsValid = setAsInp(Bool())
  val upIsReady = setAsInp(Bool())
  val downIsFiring = setAsInp(Bool())
  val downIsValid = setAsInp(Bool())
  val downIsReady = setAsInp(Bool())
  val regPcPlusInstrSize = setAsInp(UInt(cfg.mainWidth bits))
  val regPcPlusImm = setAsInp(UInt(cfg.mainWidth bits))
  //val regPcPlusImmRealDst = setAsInp(UInt(cfg.mainWidth bits))
  val imm = setAsInp(Vec.fill(4)(UInt(cfg.mainWidth bits)))
  val pcChangeState = setAsOutp(
    Bool()
    //SnowHouseShouldIgnoreInstrState()
    //UInt(
    //  //3 
    //  SnowHouseShouldIgnoreInstrState().asBits.getWidth bits
    //)
  ) ///*in*/(Flow(PcChangeState()))

  val shouldIgnoreInstr = (
    /*setAsOutp*/
    setAsInp(
      Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
        Bool()
      )
    )
  )
  val rAluFlags = (
    cfg.myHaveAluFlags
  ) generate (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  def aluFlagsIdxZ = 0
  def aluFlagsIdxC = 1
  def aluFlagsIdxV = 2
  def aluFlagsIdxN = 3
  def rFlagZ = rAluFlags(aluFlagsIdxZ)
  def rFlagC = rAluFlags(aluFlagsIdxC)
  def rFlagV = rAluFlags(aluFlagsIdxV)
  def rFlagN = rAluFlags(aluFlagsIdxN)
  val rIds = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val rIra = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val rIe = (
    setAsOutp(
      Bool()
    )
  )
  val rIty = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val rSty = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val rHi = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val rLo = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val rIndexReg = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val rMulHiOutp = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val rDivHiOutp = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val rModHiOutp = (
    setAsOutp(UInt(cfg.mainWidth bits))
  )
  val takeIrq = setAsInp(
    Bool()
  )
  val irqIraRegPc = setAsInp(
    UInt(cfg.mainWidth bits)
  )
  def selRdMemWord(
    opInfo: OpInfo,
    idx: Int,
    gprIdxAddend: Int=0,
  ): UInt = {
    def innerFunc(
      idx: Int,
      //isPostPcDst: Boolean,
    ) = {
      if (
        //idx == 0
        //|| isPostPcDst
        idx < 0
        || idx >= cfg.regFileCfg.modRdPortCnt
      ) {
        assert(
          false,
          s"eek! idx:${idx}"
        )
        rdMemWord(0).getZero
        //opInfo.dstArr(idx) match {
        //  case DstKind.Gpr => {
        //    rdMemWord(idx)
        //  }
        //  case DstKind.Spr(kind) => {
        //    kind match {
        //      case SprKind.AluFlags => {
        //        rAluFlags
        //      }
        //      case SprKind.Ids => {
        //        rIds
        //      }
        //      case SprKind.Ira => {
        //        rIra
        //      }
        //      case SprKind.Ie => {
        //        Cat(rIe).asUInt.resized
        //      }
        //      case SprKind.Ity => {
        //        rIty
        //      }
        //      case SprKind.Sty => {
        //        rSty
        //      }
        //      case SprKind.Hi => {
        //        rHi
        //      }
        //      case SprKind.Lo => {
        //        rLo
        //      }
        //      case _ => {
        //        assert(
        //          false,
        //          s"not yet implemented"
        //          + s"opInfo(${opInfo} ${opInfo.select}) "
        //          + s"${opInfo.srcArr(idx)}"
        //        )
        //        U(s"${cfg.mainWidth}'d0")
        //      }
        //    }
        //  }
        //  case _ => {
        //    assert(
        //      false,
        //      s"not yet implemented: "
        //      + s"opInfo(${opInfo} ${opInfo.select}) "
        //      + s"${opInfo.dstArr(idx)}"
        //    )
        //    U(s"${cfg.mainWidth}'d0")
        //  }
        //}
      } else {
        //val tempIdx = idx - 1
        val tempIdx = idx
        opInfo.srcArr(tempIdx) match {
          case SrcKind.Gpr => {
            rdMemWord(idx + gprIdxAddend)
          }
          case SrcKind.Pc => {
            regPc
          }
          case SrcKind.Spr(kind) => {
            kind match {
              case SprKind.AluFlags => {
                rAluFlags
              }
              case SprKind.Ids => {
                rIds
              }
              case SprKind.Ira => {
                rIra
              }
              case SprKind.Ie => {
                Cat(rIe).asUInt.resized
              }
              case SprKind.Ity => {
                rIty
              }
              case SprKind.Sty => {
                rSty
              }
              case SprKind.Hi => {
                rHi
              }
              case SprKind.Lo => {
                rLo
              }
              case _ => {
                assert(
                  false,
                  s"not yet implemented"
                  + s"opInfo(${opInfo} ${opInfo.select}) "
                  + s"${opInfo.srcArr(idx)}"
                )
                U(s"${cfg.mainWidth}'d0")
              }
            }
          }
          case SrcKind.HiddenReg(kind) => {
            kind match {
              case HiddenRegKind.IndexReg => {
                rIndexReg
              }
              case HiddenRegKind.MulHiOutp => {
                rMulHiOutp
              }
              case HiddenRegKind.DivHiOutp => {
                rDivHiOutp
              }
              case HiddenRegKind.ModHiOutp => {
                rModHiOutp
              }
            }
          }
          case SrcKind.Imm(/*isSImm*/) => {
            imm(0)
          }
          case _ => {
            assert(
              false,
              s"not yet implemented"
              + s"opInfo(${opInfo} ${opInfo.select}) "
              + s"${opInfo.srcArr(idx)}"
            )
            U(s"${cfg.mainWidth}'d0")
          }
        }
      }
    }
    //opInfo.select match {
    //  case OpSelect.Cpy => {
    //    opInfo.cpyOp.get match {
    //      case CpyOpKind.Br => {
    //        for ((dst, dstIdx) <- opInfo.dstArr.view.zipWithIndex) {
    //          if (dst == DstKind.Gpr) {
    //            return innerFunc(idx=idx + 1, isPostPcDst=true)
    //          }
    //        }
    //        return innerFunc(idx=idx, isPostPcDst=false)
    //      }
    //      case _ => {
    //        return innerFunc(idx=idx, isPostPcDst=false)
    //      }
    //    }
    //  }
    //  case OpSelect.Alu => {
    //    if (opInfo.dstArr(0) == DstKind.Spr(SprKind.AluFlags)) {
    //      return innerFunc(idx=(idx + 1), isPostPcDst=false)
    //    } else {
    //      return innerFunc(idx=idx, isPostPcDst=false)
    //    }
    //  }
    //  case OpSelect.AluShift => {
    //    if (opInfo.dstArr(0) == DstKind.Spr(SprKind.AluFlags)) {
    //      return innerFunc(idx=(idx + 1), isPostPcDst=false)
    //    } else {
    //      return innerFunc(idx=idx, isPostPcDst=false)
    //    }
    //  }
    //  case _ => {
    //    return innerFunc(idx=idx, isPostPcDst=false)
    //  }
    //}
    return innerFunc(idx=idx)
  }
  //val outpWrMemAddr = setAsOutp(
  //  UInt(log2Up(cfg.regFileCfg.wordCountMax) bits)
  //)
  //val inpPushMemAddr = setAsInp(
  //  Vec.fill(2)(
  //    UInt(log2Up(cfg.regFileCfg.wordCountMax) bits)
  //  )
  //)
  val modMemWordValid = setAsOutp(
    Vec.fill(
      cfg.regFileCfg.modMemWordValidSize //+ 1
    )(
      Bool()
    )
  )
  val modMemWord = setAsOutp(Vec.fill(1)( // TODO: temporary size of `1`
    UInt(cfg.mainWidth bits)
  ))
  //val myModMemWord = setAsOutp(Vec.fill(1)(
  //  UInt(cfg.mainWidth bits)
  //))
  //val aluInpA = setAsOutp(
  //  SInt(cfg.mainWidth bits)
  //)
  //val aluInpB = setAsOutp(
  //  SInt(cfg.mainWidth bits)
  //)
  //val aluOp = setAsOutp(
  //  UInt(LcvAluDel1InpOpEnum.OP_WIDTH bits)
  //)
  //val aluModMemWordValid = setAsOutp(
  //  Vec.fill(
  //    //cfg.regFileCfg.modMemWordValidSize
  //    1
  //  )(
  //    Bool()
  //  )
  //)
  //val nonShiftModMemWord = setAsOutp(
  //  UInt(cfg.mainWidth bits)
  //)
  //val shiftModMemWordValid = setAsOutp(
  //  Vec.fill(
  //    //cfg.regFileCfg.modMemWordValidSize //+ 1
  //    1
  //  )(
  //    Bool()
  //  )
  //)
  //val shiftModMemWord = setAsOutp(
  //  UInt(cfg.mainWidth bits)
  //)
  //val branchTgtBufElem = setAsInp(
  //  BranchTgtBufElem(cfg=cfg)
  //)
  val btbElemValid = setAsInp(
    Bool()
  )
  val btbElemDontPredict = setAsInp(
    Bool()
  )
  val branchPredictTkn = setAsInp(
    Bool()
  )
  val branchPredictReplaceBtbElem = setAsInp(
    Bool()
  )
  //val psExSetPcStateVec = setAsOutp(
  //  Vec.fill(2)(
  //    Bool()
  //  )
  //)
  val psExSetPc = (Flow(
    SnowHousePsExSetPcPayload(cfg=cfg)
  ))
  if (isComponentIo) {
    master(psExSetPc)
  }
  val inpDecodeExt = setAsInp(
    Vec.fill(2)(
      SnowHouseDecodeExt(cfg=cfg)
    )
  )
  val outpDecodeExt = setAsOutp(
    SnowHouseDecodeExt(cfg=cfg)
  )
  val multiCycleOpInfoIdx = setAsOutp(
    UInt(log2Up(cfg.multiCycleOpInfoMap.size) bits)
  )
  //def opIs = decodeExt.opIs
  def opIsMemAccess = outpDecodeExt.opIsMemAccess
  //def opIsCpyNonJmpAlu = decodeExt.opIsCpyNonJmpAlu
  //def opIsAluShift = outpDecodeExt.opIsAluShift
  def opIsJmp = outpDecodeExt.opIsJmp
  def opIsAnyMultiCycle = outpDecodeExt.opIsAnyMultiCycle
  def opIsMultiCycle = outpDecodeExt.opIsMultiCycle
  def jmpAddrIdx = (
    //1
    0
  )
  def brCondIdx = (
    if (
      !cfg.optInvertTwoRegCmp
    ) (
      Array[Int](0, 1)
    ) else (
      Array[Int](1, 0)
    )
  )
  val haveRetIraState = (
    cfg.irqCfg match {
      case Some(irqCfg) => {
        irqCfg match {
          case SnowHouseIrqConfig.IraIds(_) => {
            true
          }
        }
      }
      case None => {
        false
      }
    }
  )
  val rHadRetIra = (
    haveRetIraState
  ) generate (
    //Reg(Bool(), init=False)
    setAsOutp(Bool())
  )
}
case class SnowHousePipeStageExecuteSetOutpModMemWord(
  args: SnowHousePipeStageArgs,
) extends Component {
  def cfg = args.cfg
  //val modIo = args.io
  val io = SnowHousePipeStageExecuteSetOutpModMemWordIo(
    cfg=cfg,
    isComponentIo=true,
  )
  io.modMemWord := (
    RegNext(
      next=io.modMemWord,
      init=io.modMemWord.getZero,
    )
  )
  //io.modMemWord.foreach(item => {
  //  item := 0x0
  //})
  //io.shiftModMemWordValid := (
  //  False
  //)
  //io.shiftModMemWord := (
  //  RegNext(
  //    next=io.shiftModMemWord,
  //    init=io.shiftModMemWord.getZero,
  //  )
  //  //0x0
  //)
  //val myModMemWordValid = (
  //  if (cfg.myHaveZeroReg) (
  //    // TODO: support more register simultaneous writes
  //    !io.gprIsZeroVec(0)
  //  ) else (
  //    True
  //  )
  //)
  def getTempModMemWordValid(idx: Int) = (
    if (cfg.myHaveZeroReg) (
      //!io.gprIsZeroVec(0)(idx)
      io.gprIsNonZeroVec.last(idx)
    ) else (
      True
    )
    //myModMemWordValid
  )
  for (idx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
    io.modMemWordValid(idx) := getTempModMemWordValid(idx=idx)
  }
  //for (idx <- 0 until io.aluModMemWordValid.size) {
  //  io.aluModMemWordValid(idx) := (
  //    getTempModMemWordValid(idx)
  //  )
  //}

  //io.aluOp := (
  //  RegNext(
  //    next=io.aluOp,
  //    init=LcvAluDel1InpOpEnum.ZERO_UINT,
  //  )
  //)
  //io.aluInpA := (
  //  RegNext(
  //    next=io.aluInpA,
  //    init=io.aluInpA.getZero,
  //  )
  //)
  //io.aluInpB := (
  //  RegNext(
  //    next=io.aluInpB,
  //    init=io.aluInpB.getZero,
  //  )
  //)

  //io.myModMemWord.foreach(item => {
  //  item := RegNext(
  //    next=item,
  //    init=item.getZero,
  //  )
  //})

  def enumExSetPcValidCond/*U*/ = 0
  //def enumExSetPcValidCondS = 1
  def enumExSetPcValidOther = 1
  def enumExSetPcValidLim = 2
  val rExSetPcValid = {
    val temp = (
      Reg(
        Vec.fill(enumExSetPcValidLim)(
          Bool()
        )
      )
    )
    temp.foreach(item => item.init(item.getZero))
    if (!cfg.targetAltera) (
      temp
    ) else (
      KeepAttribute(temp)
    )
  }
  case class SetPcCmp(
    //mulAcc: LcvMulAcc32Del1
    //adder: LcvAddDel1,
    //cmpEqDel1: LcvCmpEqDel1,
  ) extends Area {
    val rValid = (
      Reg(Bool(), init=False)
      //Bool()
    )
    //nextValid := (
    //  RegNext(
    //    next=nextValid,
    //    init=nextValid.getZero,
    //  )
    //)
    //val nextValid = 
    val myCmp = UInt(cfg.mainWidth + 1 bits)
    //val myStickyCmp = Bool()
    //val mulAccIo = (
    //  LcvMulAcc32Io(
    //    optIncludeClk=true
    //  )
    //)
    //val mulAcc = LcvMulAcc32Del1()
    //val left = io.rdMemWord(io.brCondIdx(0))
    //val right = io.rdMemWord(io.brCondIdx(1))
    //val cmpEq = (
    //  left === right
    //)
    //val cmpEq = (
    //  RegNext(
    //    next=(
    //      io.rdMemWord(io.brCondIdx(0)) === io.rdMemWord(io.brCondIdx(1))
    //    ),
    //    init=False
    //  )
    //)
    val (cmpEq, cmpEqQ) = (
      LcvFastCmpEq(
        //left=RegNext/*When*/(
        //  next=io.rdMemWord(io.brCondIdx(0)),
        //  //cond=io.upIsValid,
        //  init=io.rdMemWord(io.brCondIdx(0)).getZero,
        //),
        //right=RegNext/*When*/(
        //  next=io.rdMemWord(io.brCondIdx(1)),
        //  //cond=io.upIsValid,
        //  init=io.rdMemWord(io.brCondIdx(1)).getZero,
        //),
        left=io.rdMemWord(io.brCondIdx(0)),
        right=io.rdMemWord(io.brCondIdx(1)),
        //mulAccIo=(
        //  //mulAccIo
        //  mulAcc.io
        //),
        //addIo=(
        //  adder.io
        //),
        cmpEqIo=(
          //cmpEqIo
          //cmpEqDel1.io
          null
        ),
        optDsp=(
          //true
          false
        ),
        optReg=true,
        //kind=LcvFastCmpEq.Kind.UseFastCarryChain,
      )
    )
    //mulAcc.io <> mulAccIo
  }
  //val mySetPcCmpEqAdder = LcvAddDel1(cfg.mainWidth + 1)
  //mySetPcCmpEqAdder.io.do_inv := False

  //val myCmpEqDel1ForEq = LcvCmpEqDel1(cfg.mainWidth)
  val myPsExSetPcCmpEq = SetPcCmp(
    //cmpEqDel1=(
    //  myCmpEqDel1ForEq
    //  //null
    //)
  )

  //val rMyPsExSetPcCmpEqValid = Reg(Bool(), init=False)
  //val myPsExSetPcCmpEq = /*Reg*/(UInt(cfg.mainWidth + 1 bits)) //init(0x0)
  //val myPsExSetPcCmpEq.myStickyCmp = Bool()
  //val mySetPcCmpNeAdder = LcvAddDel1(cfg.mainWidth + 1)
  //mySetPcCmpNeAdder.io.do_inv := True
  //val myCmpEqDel1 = LcvCmpEqDel1(cfg.mainWidth)
  //val myPsExSetPcCmpNe = SetPcCmp(adder=mySetPcCmpNeAdder)
  //val rMyPsExSetPcCmpNeValid = Reg(Bool(), init=False)
  //val myPsExSetPcCmpNe = /*Reg*/(UInt(cfg.mainWidth + 1 bits)) //init(0x0)
  //val myPsExSetPcCmpNe.myStickyCmp = Bool()

  //val myCmpEqDel1ForNe = LcvCmpEqDel1(cfg.mainWidth)
  val myPsExSetPcCmpNe = SetPcCmp(
    //cmpEqDel1=(
    //  myCmpEqDel1ForNe
    //  //null
    //)
  )

  rExSetPcValid.foreach(_ := False)
  myPsExSetPcCmpEq.myCmp := (
    0x0
    //False
    //RegNextWhen(
    //  next=myPsExSetPcCmpEq,
    //  cond=(
    //    io.upIsValid
    //    && io.downIsReady
    //  ),
    //  init=myPsExSetPcCmpEq.getZero,
    //)
    //RegNext(
    //  next=myPsExSetPcCmpEq.myCmp,
    //  init=myPsExSetPcCmpEq.myCmp.getZero,
    //)
  )
  myPsExSetPcCmpNe.myCmp := (
    0x0
    //False
    //RegNextWhen(
    //  next=myPsExSetPcCmpNe,
    //  cond=(
    //    io.upIsValid
    //    && io.downIsReady
    //  ),
    //  init=myPsExSetPcCmpNe.getZero,
    //)
    //RegNext(
    //  next=myPsExSetPcCmpNe,
    //  init=myPsExSetPcCmpEq.getZero,
    //)
    //RegNext(
    //  next=myPsExSetPcCmpNe.myCmp,
    //  init=myPsExSetPcCmpNe.myCmp.getZero,
    //)
  )

  val myPsExSetPcValidToOrReduce = (
    Cat(
      //RegNext/*When*/(
      //  next=rExSetPcValid,
      //  //cond=(!io.shouldIgnoreInstr.last),
      //  init=rExSetPcValid.getZero
      //),
      rExSetPcValid,
      myPsExSetPcCmpEq.myCmp.msb,
      //RegNext(myPsExSetPcCmpEq.myStickyCmp, init=False),
      myPsExSetPcCmpNe.myCmp.msb,
      //RegNext(myPsExSetPcCmpNe.myStickyCmp, init=False),
    ).asUInt
  )

  val myPsExSetPcValid = (
    /*LcvFastOrR*/(
      ///*self=*/Vec[Bool](
      //  RegNext/*When*/(
      //    next=nextExSetPcValid,
      //    //cond=(!io.shouldIgnoreInstr.last),
      //    init=nextExSetPcValid.getZero
      //  ),
      //  myPsExSetPcCmpEq.myCmp.msb,
      //  //RegNext(myPsExSetPcCmpEq.myStickyCmp, init=False),
      //  myPsExSetPcCmpNe.myCmp.msb,
      //  //RegNext(myPsExSetPcCmpNe.myStickyCmp, init=False),
      //).asBits.asUInt.orR
      //if (cfg.targetAltera) (
      //  LcvFastOrR(
      //    myPsExSetPcValidToOrReduce
      //  )
      //) else (
        myPsExSetPcValidToOrReduce.orR
      //)
      //optDsp=false
    )
  )
  val tempPsExSetPcValid = Bool() //Reg(Bool(), init=False)
  val rSavedTempPsExSetPcValid = Reg(Bool(), init=False)
  val stickyTempPsExSetPcValid = (
    tempPsExSetPcValid
    || rSavedTempPsExSetPcValid
  )
  when (io.upIsValid) {
    when (tempPsExSetPcValid) {
      rSavedTempPsExSetPcValid := True
    }
  }
  when (
    RegNext(
      next=io.upIsFiring,
      init=False,
    )
  ) {
    rSavedTempPsExSetPcValid := False
  }
  val tempBranchMispredictNotTaken = Bool()
  val tempBranchPredictTkn = (
    rose(
      //RegNext(next=io.branchPredictTkn, init=False)
      RegNextWhen(
        next=RegNextWhen(
          next=(
            io.branchPredictTkn
            //|| io.branchPredictReplaceBtbElem
          ),
          cond=io.upIsReady,
          init=False
        ),
        cond=io.upIsFiring,
        init=False,
      )
    )
  )
  val tempReplaceBtbElem = (
    rose(
      RegNext/*When*/(
        next=(
          //RegNext(next=io.branchPredictReplaceBtbElem, init=False)
          io.branchPredictReplaceBtbElem
        ),
        //cond=io.upIsFiring,
        init=False,
      )
    )
  )
  val tempBtbFire = (
    rose(
      RegNextWhen(
        next=(
          RegNextWhen(
            next=(
              //rose(
                (
                  io.btbElemValid
                  && (
                    !io.btbElemDontPredict
                    //|| io.branchPredictReplaceBtbElem
                  )
                )
              //)
            ),
            cond=io.upIsReady,
            init=False
          )
        ),
        cond=io.upIsFiring,
        init=False,
      )
    )
  )
  tempPsExSetPcValid := False
  tempBranchMispredictNotTaken := False

  val nextTempPsExSetPcValid = (
    /*rose*/(
      (
        myPsExSetPcValid
        =/= tempBranchPredictTkn
      ) || (
        //tempBtbFire
        tempReplaceBtbElem
      )
    )
  )
  //when (
  //  !rose(
  //    RegNext(
  //      next=io.shouldIgnoreInstr.last,
  //      init=io.shouldIgnoreInstr.last.getZero,
  //    )
  //  )
  //) {
    when (tempBtbFire) {
      tempPsExSetPcValid := (
        nextTempPsExSetPcValid
        || tempBranchMispredictNotTaken
      )
      tempBranchMispredictNotTaken := (
        (
          (tempBranchPredictTkn && !myPsExSetPcValid)
          //(tempBranchPredictTkn === myPsExSetPcValid)
          //&& !tempBranchPredictTkn
          //myPsExSetPcValid =/= tempBranchPredictTkn
        )
        //|| (
        //  //RegNext(
        //  //  next=io.branchPredictReplaceBtbElem,
        //  //  init=io.branchPredictReplaceBtbElem.getZero,
        //  //)
        //  tempReplaceBtbElem
        //)
      )
      //when (
      //  //nextTempPsExSetPcValid && !myPsExSetPcValid
      //  tempBranchPredictTkn && !myPsExSetPcValid
      //) {
      //  tempPsExSetPcTaken := False
      //} otherwise {
      //  tempPsExSetPcTaken := True
      //}
    } otherwise {
      tempPsExSetPcValid := (
        //False
        /*rose*/(
          myPsExSetPcValid
        )
      )
      tempBranchMispredictNotTaken := (
        //myPsExSetPcValid
        False
        //True
      )
    }
  //}
  val myTakeIrq = (
    //rose(
    //  io.takeIrq
    //  && io.upIsFiring
    //  && io.rIe
    //)
    Bool()
  )
  myTakeIrq := (
    io.takeIrq
    //&& io.upIsFiring
    && io.upIsValid
    && io.rIe
  )
  val rSavedTakeIrq = Reg(Bool(), init=False)
  val stickyTakeIrq = (
    //io.takeIrq
    myTakeIrq
    || rSavedTakeIrq
  )
  //myTakeIrq := False
  when (
    //io.upIsValid
    io.upIsFiring
  ) {
    when (myTakeIrq) {
      rSavedTakeIrq := True
      //myTakeIrq := True
      //tempPsExSetPcValid := True
      //tempBranchMispredictNotTaken := False
    }
  }
  when (
    RegNext(io.upIsFiring, init=False)
    //&& rSavedTakeIrq
  ) {
    rSavedTakeIrq := False
  }
  when (
    //stickyTakeIrq
    rose(RegNext(
      (
        io.upIsFiring
        && stickyTakeIrq
      ),
      init=False
    ))
  ) {
    tempPsExSetPcValid := True
  }
  //val rTakeIrqTempPsExSetPcValid = (
  //  Reg(
  //    Bool(),
  //    init=False,
  //  )
  //)

  //when (
  //  //RegNext(
  //  //  next=io.takeIrq,
  //  //  init=False,
  //  //)
  //  io.takeIrq
  //) {
  //  tempPsExSetPcValid := True
  //}

  //when (io.takeIrq) {
  //  io.psExSetPc.branchTgtBufElem.dontPredict := True
  //}
  //when (!RegNext(
  //  io.shouldIgnoreInstr.last
  //)) {
  //val rTempPsExSetPcValid = (
  //  RegNextWhen(
  //    next=tempPsExSetPcValid,
  //    cond=(
  //      //!rose(
  //        io.shouldIgnoreInstr(0)
  //      //)
  //    ),
  //    init=tempPsExSetPcValid.getZero,
  //  )
  //)
  io.psExSetPc.valid := (
    //RegNext(
      /*next=*/(
        //tempPsExSetPcValid
        stickyTempPsExSetPcValid
        //rSavedTempPsExSetPcValid
      ),
    //  init=tempPsExSetPcValid.getZero,
    //)
  )
  //} otherwise {
  //  io.psExSetPc.valid := False
  //}

  //io.psExSetPc := io.psExSetPc.getZero
  io.psExSetPc.payload := io.psExSetPc.payload.getZero
  io.psExSetPc.nextPc.allowOverride
  io.psExSetPc.nextPc := (
    RegNext(
      next=io.psExSetPc.nextPc,
      init=io.psExSetPc.nextPc.getZero,
    )
    //io.regPcPlusImm 
  )
  io.dbusHostPayload := (
    RegNext(
      next=io.dbusHostPayload,
      init=io.dbusHostPayload.getZero,
    )
  )
  io.dbusHostPayload.addr.allowOverride
  io.dbusHostPayload.data.allowOverride
  io.dbusHostPayload.accKind.allowOverride
  io.dbusHostPayload.subKind.allowOverride
  io.dbusHostPayload.subKindIsLtWordWidth.allowOverride
  //io.opIs := 0x0
  io.opIsMemAccess.foreach(current => {
    current := (
      //False
      io.splitOp.opIsMemAccess
    )
  })
  //io.opIsMultiCycle.foreach(current => {
  //  current := False
  //})
  io.opIsAnyMultiCycle := (
    io.splitOp.opIsMultiCycle
  )
  for (idx <- 0 until cfg.multiCycleOpInfoMap.size) {
    io.opIsMultiCycle(idx) := (
      io.splitOp.multiCycleOp(idx)
    )
  }
  //io.outpDecodeExt.memAccessKind := SnowHouseMemAccessKind.LoadU
  //io.outpDecodeExt.memAccessSubKind := SnowHouseMemAccessSubKind.Sz8
  //io.outpDecodeExt.memAccessIsPush := False
  val nextShouldIgnoreInstrState = (
    Vec.fill(
      io.regPcSetItCnt.size
    )(
      Bool()
      //SInt(3 bits)
    )
    //SnowHouseShouldIgnoreInstrState()
  )
  val rShouldIgnoreInstrState = {
    val temp = RegNext(nextShouldIgnoreInstrState)
    //init(
    //  nextShouldIgnoreInstrState.getZero
    //  //SnowHouseShouldIgnoreInstrState.Idle
    //)
    temp.foreach(item => {
      item.init(
        item.getZero
        //-1
      )
    })
    temp
  }
  //for (idx <- 0 until rShouldIgnoreInstrState.size) {
    nextShouldIgnoreInstrState := rShouldIgnoreInstrState
  //}
  //io.opIsJmp.allowOverride
  //io.opIsJmp := (
  //  io.psExSetPc.fire
  //  //&& !rShouldIgnoreInstrState.asBits(0)
  //  //&& !io.shouldIgnoreInstr
  //  && (
  //    //io.upIsValid
  //    io.upIsFiring
  //  )
  //)
  //io.shouldIgnoreInstr := (
  //  RegNext(
  //    next=io.shouldIgnoreInstr,
  //    init=io.shouldIgnoreInstr.getZero,
  //  )
  //)
  io.pcChangeState := (
    RegNext(io.pcChangeState)
    init(
      io.pcChangeState.getZero
      //SnowHouseShouldIgnoreInstrState.Idle
      //U"1'b1".resized
    )
  )

  io.multiCycleOpInfoIdx := 0x0
  //val lowerMyFanoutShouldIgnoreInstr = Bool()
  //when (
  //  //io.shouldIgnoreInstr
  //  lowerMyFanoutShouldIgnoreInstr
  //) {
  //  io.modMemWordValid.foreach(current => {
  //    current := False
  //  })
  //  io.modMemWord.foreach(modMemWord => {
  //    modMemWord := modMemWord.getZero
  //  })
  //}
  //io.shouldIgnoreInstr := False
  //lowerMyFanoutShouldIgnoreInstr := False

  //when (!rShouldIgnoreInstrState) {
  //  //io.shouldIgnoreInstr := False
  //  io.pcChangeState := False
  //  when (io.opIsJmp) {
  //    io.pcChangeState := True
  //    when (io.upIsFiring) {
  //      nextShouldIgnoreInstrState := True
  //    }
  //  }
  //} otherwise {
  //  when (
  //    //if (io.regPcSetItCnt.getWidth == 1) (
  //      io.regPcSetItCnt.msb
  //    //) else (
  //    //  io.regPcSetItCnt =/= 0
  //    //)
  //  ) {
  //    //io.shouldIgnoreInstr := False
  //    when (io.opIsJmp) {
  //      io.pcChangeState := True
  //    } otherwise {
  //      when (io.upIsFiring) {
  //        io.pcChangeState := False
  //        nextShouldIgnoreInstrState := False
  //      }
  //    }
  //  } otherwise {
  //    io.pcChangeState := True
  //    lowerMyFanoutShouldIgnoreInstr := True
  //    io.shouldIgnoreInstr := True
  //  }
  //}
  //val rShouldIgnoreInstrShift = (
  //  Reg(UInt(4 bits))
  //  init(0x0)
  //)
  //when (!rShouldIgnoreInstrShift.lsb) {
  //  io.shouldIgnoreInstr := True
  //  lowerMyFanoutShouldIgnoreInstr := True
  //}

  //val rShouldIgnoreInstrCnt = (
  //  Reg(SInt(3 bits))
  //  init(-1)
  //)
  //when (
  //  //!rShouldIgnoreInstrCnt.msb
  //  //!io.regPcSetItCnt.msb
  //  //|| 
  //  rShouldIgnoreInstrState(0)
  //) {
  //  io.shouldIgnoreInstr := True
  //}
  //when (
  //  rShouldIgnoreInstrState(1)
  //) {
  //  lowerMyFanoutShouldIgnoreInstr := True
  //}
  //io.psExSetPc.nextPc := (
  //  io.regPcPlusImm 
  //)
  // TODO: change this to `io.gprIsZeroVec`

  //switch (rShouldIgnoreInstrState) {
  //}

  //when (io.upIsFiring) {
  //  //rShouldIgnoreInstrShift := (
  //  //  Cat(
  //  //    False,
  //  //    rShouldIgnoreInstrShift(rShouldIgnoreInstrShift.high downto 1)
  //  //  ).asUInt
  //  //)
  //  when (
  //    //rShouldIgnoreInstrShift.lsb
  //    //!rShouldIgnoreInstrCnt.msb
  //    !io.regPcSetItCnt.msb
  //  ) {
  //    //rShouldIgnoreInstrCnt := (
  //    //  rShouldIgnoreInstrCnt - 1
  //    //)
  //  } otherwise {
  //    when (io.opIsJmp) {
  //      rShouldIgnoreInstrCnt := (
  //        2
  //      )
  //      //rShouldIgnoreInstrShift := (
  //      //  U(
  //      //    rShouldIgnoreInstrShift.getWidth bits,
  //      //    rShouldIgnoreInstrShift.high -> True,
  //      //    default -> False
  //      //  )
  //      //)
  //    }
  //  }
  //  //when (!rShouldIgnoreInstrState(0)) {
  //  //  io.shouldIgnoreInstr := False
  //  //  //when (io.opIsJmp) {
  //  //  //  io.shouldIgnoreInstr := False
  //  //  //}
  //  //}
  //}
  ////io.shouldIgnoreInstr := !rShouldIgnoreInstrState.asBits(0)
  ////lowerMyFanoutShouldIgnoreInstr := !rShouldIgnoreInstrState.asBits(0)
  //switch (rShouldIgnoreInstrState) {
  //  is (SnowHouseShouldIgnoreInstrState.Idle) {
  //    io.shouldIgnoreInstr := False
  //    lowerMyFanoutShouldIgnoreInstr := False
  //    when (io.upIsFiring) {
  //      when (io.opIsJmp) {
  //        nextShouldIgnoreInstrState := (
  //          SnowHouseShouldIgnoreInstrState.IgnoreInstr1
  //        )
  //      }
  //    }
  //  }
  //  is (SnowHouseShouldIgnoreInstrState.IgnoreInstr0) {
  //    when (io.upIsFiring) {
  //      nextShouldIgnoreInstrState := (
  //        SnowHouseShouldIgnoreInstrState.Idle
  //      )
  //    }
  //    io.shouldIgnoreInstr := True
  //    lowerMyFanoutShouldIgnoreInstr := True
  //  }
  //  is (SnowHouseShouldIgnoreInstrState.IgnoreInstr1) {
  //    when (io.upIsFiring) {
  //      nextShouldIgnoreInstrState := (
  //        SnowHouseShouldIgnoreInstrState.IgnoreInstr0
  //      )
  //    }
  //    io.shouldIgnoreInstr := True
  //    lowerMyFanoutShouldIgnoreInstr := True
  //  }
  //  //is (SnowHouseShouldIgnoreInstrState.IgnoreInstr2) {
  //  //  when (io.upIsFiring) {
  //  //    nextShouldIgnoreInstrState := (
  //  //      SnowHouseShouldIgnoreInstrState.IgnoreInstr1
  //  //    )
  //  //  }
  //  //  io.shouldIgnoreInstr := True
  //  //  lowerMyFanoutShouldIgnoreInstr := True
  //  //}
  //}
  ////switch (nextShouldIgnoreInstrState) {
  ////}
  //io.pcChangeState.assignFromBits(
  //  rShouldIgnoreInstrState.asBits
  //)

  val nextAluFlags = (
    cfg.myHaveAluFlags
  ) generate (
    UInt(cfg.mainWidth bits)
  )
  def nextFlagZ = nextAluFlags(io.aluFlagsIdxZ)
  def nextFlagC = nextAluFlags(io.aluFlagsIdxC)
  def nextFlagV = nextAluFlags(io.aluFlagsIdxV)
  def nextFlagN = nextAluFlags(io.aluFlagsIdxN)
  if (cfg.myHaveAluFlags) {
    io.rAluFlags := (
      RegNextWhen(
        next=nextAluFlags,
        cond=io.upIsFiring,
        init=nextAluFlags.getZero
      )
    )
    nextAluFlags := io.rAluFlags 
  }
  val nextIds = UInt(cfg.mainWidth bits)
  io.rIds := (
    RegNextWhen(
      next=nextIds,
      cond=io.upIsFiring,
      init=nextIds.getZero
    )
  )
  if (cfg.onlyOneMultiCycleWriteToIdsOpInfo == None) {
    nextIds := io.rIds
  }

  val nextIra = UInt(cfg.mainWidth bits)
  io.rIra := (
    RegNextWhen(
      next=nextIra,
      cond=io.upIsFiring,
      init=nextIra.getZero
    )
  )
  nextIra := io.rIra
  val nextIe = Bool()//UInt(cfg.mainWidth bits)
  io.rIe := (
    RegNextWhen(
      next=nextIe,
      cond=io.upIsFiring,
      init=nextIe.getZero
    )
  )
  nextIe := io.rIe
  //val myHistHadRetIra = (
  //  History(
  //    that=io.rHadRetIra,
  //    length=(
  //      2
  //      //32
  //    ),
  //    when=(
  //      io.upIsFiring
  //      //&& !io.shouldIgnoreInstr(2)
  //    ),
  //    init=io.rHadRetIra.getZero,
  //  )
  //)
  //when (
  //  //io.rHadRetIra
  //  myHistHadRetIra.last
  //  //&& io.upIsFiring
  //) {
  //  nextIe := True
  //}
  val nextIty = UInt(cfg.mainWidth bits)
  io.rIty := (
    RegNextWhen(
      next=nextIty,
      cond=io.upIsFiring,
      init=nextIty.getZero
    )
  )
  nextIty := io.rIty
  val nextSty = UInt(cfg.mainWidth bits)
  io.rSty := (
    RegNextWhen(
      next=nextSty,
      cond=io.upIsFiring,
      init=nextSty.getZero
    )
  )
  nextSty := io.rSty
  val nextHi = UInt(cfg.mainWidth bits)
  io.rHi := (
    RegNextWhen(
      next=nextHi,
      cond=io.upIsFiring,
      init=nextHi.getZero
    )
  )
  nextHi := io.rHi
  val nextLo = UInt(cfg.mainWidth bits)
  io.rLo := (
    RegNextWhen(
      next=nextLo,
      cond=io.upIsFiring,
      init=nextLo.getZero
    )
  )
  nextLo := io.rLo
  val nextIndexReg = UInt(cfg.mainWidth bits)
  io.rIndexReg := (
    RegNextWhen(
      next=nextIndexReg,
      cond=io.upIsFiring,
      init=nextIndexReg.getZero
    )
  )
  //nextIndexReg := io.rIndexReg
  nextIndexReg := 0x0
  val nextMulHiOutp = UInt(cfg.mainWidth bits)
  io.rMulHiOutp := (
    RegNextWhen(
      next=nextMulHiOutp,
      cond=io.upIsFiring,
      init=nextMulHiOutp.getZero
    )
  )
  nextMulHiOutp := io.rMulHiOutp

  val nextDivHiOutp = UInt(cfg.mainWidth bits)
  io.rDivHiOutp := (
    RegNextWhen(
      next=nextDivHiOutp,
      cond=io.upIsFiring,
      init=nextDivHiOutp.getZero
    )
  )
  nextDivHiOutp := io.rDivHiOutp

  val nextModHiOutp = UInt(cfg.mainWidth bits)
  io.rModHiOutp := (
    RegNextWhen(
      next=nextModHiOutp,
      cond=io.upIsFiring,
      init=nextModHiOutp.getZero
    )
  )
  nextModHiOutp := io.rModHiOutp
  val nextHadRetIra = Bool()
  //nextHadRetIra := False
  if (io.haveRetIraState) {
    //io.rHadRetIra.setAsReg() init(False)
    //when (io.upIsFiring) {
    //  io.rHadRetIra := nextHadRetIra
    //}
    io.rHadRetIra := (
      RegNextWhen(
        next=nextHadRetIra,
        cond=io.upIsFiring,
        init=nextHadRetIra.getZero,
      )
    )
    nextHadRetIra := io.rHadRetIra
  }
  //io.psExSetPc.nextPc.allowOverride
  //io.psExSetPc.nextPc := (
  //  io.regPcPlusImm 
  //)
  io.dbusHostPayload.data := io.rdMemWord(1) //selRdMemWord(0)
  if (cfg.allMainLdstUseGprPlusImm) {
    io.dbusHostPayload.addr := io.rdMemWord(0) + io.imm(1)
  }
  io.dbusHostPayload.accKind := (
    io.inpDecodeExt(0).memAccessKind
  )
  io.dbusHostPayload.subKind := (
    io.inpDecodeExt(0).memAccessSubKind
  )
  io.dbusHostPayload.subKindIsLtWordWidth := (
    io.inpDecodeExt(0).memAccessIsLtWordWidth
  )
  io.outpDecodeExt.memAccessKind := (
    io.inpDecodeExt(1).memAccessKind
  )
  io.outpDecodeExt.memAccessSubKind := (
    io.inpDecodeExt(1).memAccessSubKind
  )
  io.outpDecodeExt.memAccessIsLtWordWidth := (
    io.inpDecodeExt(1).memAccessIsLtWordWidth
  )
  println(
    f"cfg.allMainLdstUseGprPlusImm:${cfg.allMainLdstUseGprPlusImm}"
  )
  //io.modMemWordValid.foreach(current => {
  //  current := True
  //})
  //var myMemAccIdx: Int = 0
  def innerFunc(
    opInfo: OpInfo,
    opInfoIdx: Int,
    fullOpInfoIdx: Option[Int]=None,
    isSingleWriteToIds: Boolean=false,
  ): Unit = {
    def selRdMemWord(
      srcArrIdx: Int,
      gprIdxAddend: Int=0,
    ): UInt = {
      io.selRdMemWord(
        opInfo=opInfo,
        idx=srcArrIdx,
        gprIdxAddend=gprIdxAddend,
      )
    }
    assert(
      //opInfo.dstArr.size == 1 || opInfo.dstArr.size == 2
      opInfo.dstArr.size >= 1 && opInfo.dstArr.size <= 3,
      s"not yet implemented: "
      + s"opInfo(${opInfo}) index:${opInfoIdx}"
    )
    assert(
      opInfo.srcArr.size == 1
      || opInfo.srcArr.size == 2
      || opInfo.srcArr.size == 3
      || opInfo.srcArr.size == 4,
      s"not yet implemented: "
      + s"opInfo(${opInfo}) index:${opInfoIdx}"
    )
    opInfo.select match {
      case OpSelect.Cpy => {
        //io.aluModMemWordValid.foreach(_ := False)
        //io.aluOp := LcvAluDel1InpOpEnum.ZERO
        //io.shiftModMemWord := 0x0
        opInfo.cpyOp.get match {
          case CpyOpKind.Cpy => {
            //nextIndexReg := 0x0
            //io.opIsCpyNonJmpAlu := True
            assert(
              opInfo.cond == CondKind.Always,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            opInfo.memAccess match {
              case MemAccessKind.NoMemAccess => {
                assert(
                  opInfo.dstArr.size == 1,
                  s"invalid opInfo.dstArr.size: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                assert(
                  opInfo.srcArr.size == 1,
                  s"invalid opInfo.srcArr.size: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                opInfo.dstArr(0) match {
                  case DstKind.Gpr => {
                    io.modMemWord(0) := selRdMemWord(0)
                  }
                  case DstKind.Spr(kind) => {
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                    kind match {
                      case SprKind.AluFlags => {
                        nextAluFlags := selRdMemWord(0)
                      }
                      case SprKind.Ids => {
                        nextIds := selRdMemWord(0)
                      }
                      case SprKind.Ira => {
                        nextIra := selRdMemWord(0)
                      }
                      case SprKind.Ie => {
                        nextIe := selRdMemWord(0)(0)
                      }
                      case SprKind.Ity => {
                        nextIty := selRdMemWord(0)
                      }
                      case SprKind.Sty => {
                        nextSty := selRdMemWord(0)
                      }
                      case SprKind.Hi => {
                        nextHi := selRdMemWord(0)
                      }
                      case SprKind.Lo => {
                        nextLo := selRdMemWord(0)
                      }
                      case _ => {
                        assert(
                          false,
                          s"not yet implemented: ${kind}"
                        )
                      }
                    }
                  }
                  case DstKind.HiddenReg(kind) => {
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                    kind match {
                      case HiddenRegKind.IndexReg => {
                        //nextIndexReg := selRdMemWord(0)
                      }
                      case HiddenRegKind.MulHiOutp => {
                        nextMulHiOutp := selRdMemWord(0)
                      }
                      case HiddenRegKind.DivHiOutp => {
                        nextDivHiOutp := selRdMemWord(0)
                      }
                      case HiddenRegKind.ModHiOutp => {
                        nextModHiOutp := selRdMemWord(0)
                      }
                    }
                  }
                  case _ => {
                    assert(
                      false,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                  }
                }
              }
              case mem: MemAccessKind.Mem => {
                //io.opIsMemAccess.foreach(current => {
                //  current := True
                //})
                //io.opIsMemAccess(myMemAccIdx) := True
                //myMemAccIdx += 1
                //if (!mem.isAtomicLlSc) {
                  val isStore = mem.isStore
                  //io.modMemWordValid.foreach(current => {
                  //  current := False
                  //})
                  if (!isStore) {
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.modMemWordValid.foreach(current => {
                    //  current := False
                    //})
                    ////io.modMemWord.foreach(modMemWord => {
                    ////  modMemWord := modMemWord.getZero
                    ////})
                  }
                  else 
                  //if (isStore)
                  {
                    io.modMemWordValid.foreach(current => {
                      current := True
                    })
                    io.modMemWord(0) := selRdMemWord(0, 1)
                  }

                  //val tempSubKind = (
                  //  mem.subKind match {
                  //    case MemAccessKind.SubKind.Sz8 => {
                  //      SnowHouseMemAccessSubKind.Sz8
                  //    }
                  //    case MemAccessKind.SubKind.Sz16 => {
                  //      SnowHouseMemAccessSubKind.Sz16
                  //    }
                  //    case MemAccessKind.SubKind.Sz32 => {
                  //      SnowHouseMemAccessSubKind.Sz32
                  //    }
                  //    case MemAccessKind.SubKind.Sz64 => {
                  //      SnowHouseMemAccessSubKind.Sz64
                  //    }
                  //  }
                  //)
                  //io.dbusHostPayload.subKind := (
                  //  tempSubKind
                  //)
                  //io.outpDecodeExt.memAccessSubKind := (
                  //  tempSubKind
                  //)
                  //io.outpDecodeExt.memAccessIsPush := False
                  val tempAddr = (
                    (
                      opInfo.addrCalc match {
                        case AddrCalcKind.AtomicLlSc(
                          startSrcIdx
                        ) => {
                          assert(
                            false
                          )
                          selRdMemWord(0)
                        }
                        case AddrCalcKind.AddReduce(
                        ) => (
                          selRdMemWord(0)
                        )
                        case kind: AddrCalcKind.LslThenMaybeAdd => (
                          selRdMemWord(0)
                          << kind.options.lslAmount.get
                        )
                      }
                    ) 
                  )
                  if (!cfg.allMainLdstUseGprPlusImm) {
                    io.dbusHostPayload.addr := (
                      opInfo.srcArr.size match {
                        case 1 => (
                          tempAddr
                        )
                        case 2 => (
                          tempAddr + selRdMemWord(1)
                        )
                        case _ => {
                          assert(
                            false,
                            s"invalid opInfo.srcArr.size: "
                            + s"opInfo(${opInfo}) "
                            + s"index:${opInfoIdx}"
                          )
                          U(s"${cfg.mainWidth}'d0")
                        }
                      }
                    )
                  }
                  //if (!isStore) {
                  //  val tempMemAccessKind = (
                  //    if (!mem.isSigned) (
                  //      SnowHouseMemAccessKind.LoadU
                  //    ) else (
                  //      SnowHouseMemAccessKind.LoadS
                  //    )
                  //  )
                  //  io.outpDecodeExt.memAccessKind := (
                  //    tempMemAccessKind
                  //  )
                  //  io.dbusHostPayload.accKind := (
                  //    tempMemAccessKind
                  //  )
                  //  //io.dbusHostPayload.data := (
                  //  //  io.dbusHostPayload.data.getZero
                  //  //)
                  //} else { // if (isStore)
                  //  val tempMemAccessKind = (
                  //    SnowHouseMemAccessKind.Store
                  //  )
                  //  io.outpDecodeExt.memAccessKind := (
                  //    tempMemAccessKind
                  //  )
                  //  io.dbusHostPayload.accKind := (
                  //    tempMemAccessKind
                  //  )
                  //}
                //} else {
                //  //assert(
                //  //  false,
                //  //  s"not yet implemented: "
                //  //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                //  //)
                //}
              }
            }
          }
          case CpyOpKind.Cpyu => {
            //nextIndexReg := 0x0
            //io.opIsCpyNonJmpAlu := True
            assert(
              opInfo.dstArr.size == 1,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            assert(
              opInfo.srcArr.size == 1,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            assert(
              opInfo.cond == CondKind.Always,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            assert(
              opInfo.memAccess == MemAccessKind.NoMemAccess,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            assert(
              opInfo.addrCalc match {
                case AddrCalcKind.AddReduce() => {
                  true
                }
                case _ => {
                  false
                }
              },
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            io.modMemWord(0)(
              cfg.mainWidth - 1 downto (cfg.mainWidth >> 1)
            ) := (
              selRdMemWord(1)((cfg.mainWidth >> 1) - 1 downto 0)
            )
            io.modMemWord(0)(
              (cfg.mainWidth >> 1) - 1 downto 0
            ) := (
              selRdMemWord(0)(
                (cfg.mainWidth >> 1) - 1 downto 0
              )
            )
          }
          case CpyOpKind.AtomicLl => {
            //nextIndexReg := 0x0
            assert(
              //opInfo.dstArr.size
              false,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:{opInfoIdx}"
            )
          }
          case CpyOpKind.AtomicSc => {
            //nextIndexReg := 0x0
            assert(
              //opInfo.dstArr.size
              false,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:{opInfoIdx}"
            )
          }
          case CpyOpKind.Jmp => {
            //nextIndexReg := 0x0
            assert(
              opInfo.dstArr.size == 1
              || opInfo.dstArr.size == 2
              || opInfo.dstArr.size == 3,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            assert(
              opInfo.srcArr.size == 1,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            assert(
              opInfo.cond == CondKind.Always,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            assert(
              opInfo.memAccess == MemAccessKind.NoMemAccess,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            assert(
              opInfo.addrCalc match {
                case AddrCalcKind.AddReduce() => {
                  true
                }
                case _ => {
                  false
                }
              },
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            //when (!io.takeIrq) {
              if (opInfo.dstArr.size == 1) {
                io.modMemWordValid.foreach(current => {
                  current := False
                })
              } else if (opInfo.dstArr(1) == DstKind.Spr(SprKind.Ie)) {
                io.modMemWordValid.foreach(current => {
                  current := False
                })
                if (opInfo.dstArr.size == 3) {
                  assert(
                    opInfo.dstArr(2) == DstKind.Spr(SprKind.Ira),
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  assert(
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.Ids),
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  nextIra := (
                    //io.regPc
                    io.irqIraRegPc
                  )
                  nextIe/*(0)*/ := False //0x0
                }
              } else {
                // TODO: *maybe* support more outputs
                //for (idx <- 0 until io.modMemWordValid.size) {
                //  io.modMemWordValid(idx) := !io.gprIsZeroVec(0)(idx)
                //}
              }
            //} otherwise {
            //  io.modMemWordValid.foreach(current => {
            //    current := False
            //  })
            //}
            //io.modMemWord(0) := (
            //  //io.regPc + ((cfg.instrMainWidth / 8) * 1)
            //  io.regPcPlusInstrSize
            //)
            //io.psExSetPc.valid := True
            //when (
            //  //!io.shouldIgnoreInstr
            //  !lowerMyFanoutShouldIgnoreInstr
            //) {
              if (
                opInfo.dstArr.size > 1
              ) {
                if (fullOpInfoIdx.get != cfg.irqJmpOp) {
                  //when (io.gprIsNonZeroVec.last(0)) {
                    io.modMemWord(0) := (
                      //io.regPc + ((cfg.instrMainWidth / 8) * 1)
                      io.regPcPlusInstrSize
                    )
                  //} otherwise {
                  //  io.modMemWord(0) := (
                  //    0x0
                  //  )
                  //}
                }
              } else {
                //io.modMemWord.foreach(item => {
                //  item := io.rdMemWord(0)
                //})
                //io.modMemWord(0) := (
                //  0x0
                //)
                io.modMemWordValid.foreach(item => {
                  item := False
                })
              }
              rExSetPcValid(enumExSetPcValidOther) := True
              myPsExSetPcCmpEq.rValid := (
                False
                //0x0
              )
              myPsExSetPcCmpNe.rValid := (
                False
                //0x0
              )
              //io.psExSetPc.valid := RegNext(
              //  next=nextExSetPcValid,
              //  init=False,
              //)
              opInfo.srcArr(0) match {
                case SrcKind.Gpr => {
                  // BEGIN: make sure
                  //io.psExSetPc.nextPc := (
                  //  io.rdMemWord(io.jmpAddrIdx)
                  //)
                  // END: make sure
                }
                case SrcKind.Spr(SprKind.Ira) => {
                  // BEGIN: make sure
                  //io.psExSetPc.nextPc := (
                  //  io.rIra
                  //)
                  // END: make sure
                  assert(
                    opInfo.dstArr.size == 2,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  if (opInfo.dstArr(1) == DstKind.Ie) {
                    when (!io.shouldIgnoreInstr(2)) {
                      nextHadRetIra := True
                    }
                  }
                }
                case SrcKind.Spr(SprKind.Ids) => {
                  assert(
                    opInfo.dstArr.size == 3,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                }
                case _ => {
                  assert(
                    false,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                }
              }
            //}
          }
          case CpyOpKind.Br => {
            if (opInfo.dstArr.size == 1) (
              io.modMemWordValid.foreach(current => {
                current := False
              })
            )
            //nextIndexReg := 0x0
            opInfo.cond match {
              case CondKind.Always => {
                rExSetPcValid(enumExSetPcValidOther) := True
                myPsExSetPcCmpEq.rValid := (
                  False
                  //0x0
                )
                myPsExSetPcCmpNe.rValid := (
                  False
                  //0x0
                )
                //io.psExSetPc.valid := RegNext(
                //  next=nextExSetPcValid,
                //  init=False,
                //)

                if (opInfo.dstArr.size > 1) {
                  //io.modMemWord(0) := (
                  //  //io.regPc + ((cfg.instrMainWidth / 8) * 1)
                  //  io.regPcPlusInstrSize
                  //)
                  //when (io.gprIsNonZeroVec.last(0)) {
                    io.modMemWord(0) := (
                      //io.regPc + ((cfg.instrMainWidth / 8) * 1)
                      io.regPcPlusInstrSize
                    )
                  //} otherwise {
                  //  io.modMemWord(0) := (
                  //    0x0
                  //  )
                  //}
                } else {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (opInfo.dstArr.size == 1) (
                  //io.modMemWordValid.foreach(current => {
                  //  current := False
                  //})
                ) else {
                  //for (idx <- 0 until io.modMemWordValid.size) {
                  //  io.modMemWordValid(idx) := (
                  //    // TODO: support more outputs
                  //    !io.gprIsZeroVec(0)(idx)
                  //  )
                  //}
                }
              }
              case CondKind.Eq => {
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidOther) := (
                    (io.rFlagZ) //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  //nextExSetPcValid := (
                  //  //io.psExSetPc.valid
                  //  False
                  //)
                  if (opInfo.dstArr.size == 1) {
                    //io.modMemWord.foreach(item => {
                    //  item := io.rdMemWord(0)
                    //})
                    //io.modMemWordValid.foreach(item => {
                    //  item := False
                    //})
                  }
                  when (!io.shouldIgnoreInstr(2)) {
                    myPsExSetPcCmpEq.rValid := (
                      True
                      //!myPsExSetPcCmpEq.rValid
                      //&& !io.shouldIgnoreInstr(2)
                    )
                  }
                  //myPsExSetPcCmpEq.myCmp := {
                  //  //(
                  //  //  (
                  //  //    io.rdMemWord(io.brCondIdx(0))
                  //  //    === io.rdMemWord(io.brCondIdx(1))
                  //  //  )
                  //  //  //init(False)
                  //  //)
                  //  //val q = Bool()
                  //  //val unusedSumOut = UInt(cfg.mainWidth bits)
                  //  //(
                  //  //  q,
                  //  //  unusedSumOut
                  //  //) := (
                  //  //  (
                  //  //    Cat(
                  //  //      False,
                  //  //      (
                  //  //        io.rdMemWord(io.brCondIdx(0))
                  //  //        ^ (
                  //  //          ~io.rdMemWord(io.brCondIdx(1))
                  //  //        )
                  //  //      )
                  //  //    ).asUInt
                  //  //  ) + (
                  //  //    Cat(
                  //  //      U{
                  //  //        val myWidth = (
                  //  //          io.rdMemWord(io.brCondIdx(0)).getWidth
                  //  //        )
                  //  //        f"${myWidth}'d0"
                  //  //      },
                  //  //      True
                  //  //    ).asUInt
                  //  //  )
                  //  //)
                  //  //q
                  //  LcvFastCmpEq(
                  //    left=RegNext/*When*/(
                  //      next=io.rdMemWord(io.brCondIdx(0)),
                  //      //cond=io.upIsFiring,
                  //      init=io.rdMemWord(io.brCondIdx(0)).getZero,
                  //    ),
                  //    right=RegNext/*When*/(
                  //      next=io.rdMemWord(io.brCondIdx(1)),
                  //      //cond=io.upIsFiring,
                  //      init=io.rdMemWord(io.brCondIdx(1)).getZero,
                  //    ),
                  //    //left=io.rdMemWord(io.brCondIdx(0)),
                  //    //right=io.rdMemWord(io.brCondIdx(1)),
                  //    optDsp=true,
                  //    //optReg=true,
                  //  )._2//.msb
                  //}
                  myPsExSetPcCmpNe.rValid := (
                    False
                  )
                  //myPsExSetPcCmpNe.myCmp := (
                  //  //False
                  //  0x0
                  //)
                }
              }
              case CondKind.Ne => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidOther) := (
                    (!io.rFlagZ) //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  //nextExSetPcValid := {
                  //  False
                  //  //(
                  //  //  (
                  //  //    io.rdMemWord(io.brCondIdx(0))
                  //  //    =/= io.rdMemWord(io.brCondIdx(1))
                  //  //  )
                  //  //  //init(False)
                  //  //)
                  //  //val q = Bool()
                  //  //val unusedSumOut = UInt(cfg.mainWidth bits)
                  //  //(
                  //  //  q,
                  //  //  unusedSumOut
                  //  //) := (
                  //  //  (
                  //  //    Cat(
                  //  //      False,
                  //  //      (
                  //  //        io.rdMemWord(io.brCondIdx(0))
                  //  //        ^ (
                  //  //          ~io.rdMemWord(io.brCondIdx(1))
                  //  //        )
                  //  //      )
                  //  //    ).asUInt
                  //  //  ) + (
                  //  //    Cat(
                  //  //      U{
                  //  //        val myWidth = (
                  //  //          io.rdMemWord(io.brCondIdx(0)).getWidth
                  //  //        )
                  //  //        f"${myWidth}'d0"
                  //  //      },
                  //  //      True
                  //  //    ).asUInt
                  //  //  )
                  //  //)
                  //  //(!q)
                  //  //!LcvFastCmpEq(
                  //  //  left=io.rdMemWord(io.brCondIdx(0)),
                  //  //  right=io.rdMemWord(io.brCondIdx(1)),
                  //  //  optDsp=true,
                  //  //  optReg=true,
                  //  //)._1
                  //}
                  myPsExSetPcCmpEq.rValid := (
                    False
                    //0x0
                  )
                  when (!io.shouldIgnoreInstr(2)) {
                    myPsExSetPcCmpNe.rValid := (
                      True
                      //!myPsExSetPcCmpNe.rValid
                      //&& !io.shouldIgnoreInstr(2)
                      //True
                      //~LcvFastCmpEq(
                      //  left=RegNext/*When*/(
                      //    next=io.rdMemWord(io.brCondIdx(0)),
                      //    //cond=io.upIsFiring,
                      //    init=io.rdMemWord(io.brCondIdx(0)).getZero,
                      //  ),
                      //  right=RegNext/*When*/(
                      //    next=io.rdMemWord(io.brCondIdx(1)),
                      //    //cond=io.upIsFiring,
                      //    init=io.rdMemWord(io.brCondIdx(1)).getZero,
                      //  ),
                      //  //left=io.rdMemWord(io.brCondIdx(0)),
                      //  //right=io.rdMemWord(io.brCondIdx(1)),
                      //  optDsp=true,
                      //  //optReg=true,
                      //)._2
                    )
                  }
                }
              }
              case CondKind.Mi => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                rExSetPcValid(enumExSetPcValidOther) := (
                  (io.rFlagN) //init(False)
                )
              }
              case CondKind.Pl => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                rExSetPcValid(enumExSetPcValidOther) := (
                  (!io.rFlagN) //init(False)
                )
              }
              case CondKind.Vs => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                rExSetPcValid(enumExSetPcValidOther) := (
                  (io.rFlagV) //init(False)
                )
              }
              case CondKind.Vc => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                rExSetPcValid(enumExSetPcValidOther) := (
                  (!io.rFlagV) //init(False)
                )
              }
              case CondKind.Geu => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidCond) := (
                    (io.rFlagC)
                    //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  rExSetPcValid(enumExSetPcValidCond) := {
                    (
                      (
                        io.rdMemWord(io.brCondIdx(0))
                        >= io.rdMemWord(io.brCondIdx(1))
                      )
                      //init(False)
                    )
                    //(
                    //  Cat(False, io.rdMemWord(io.brCondIdx(0))).asUInt
                    //  + Cat(False, ~io.rdMemWord(io.brCondIdx(1))).asUInt
                    //  + Cat(
                    //    U{
                    //      val myWidth = (
                    //        io.rdMemWord(io.brCondIdx(0)).getWidth
                    //      )
                    //      f"${myWidth}'d0"
                    //    },
                    //    True
                    //  ).asUInt
                    //).msb
                    //val myBinop = AluOpKind.Sub.binopFunc(
                    //  cfg=cfg,
                    //  left=io.rdMemWord(io.brCondIdx(0)),
                    //  right=io.rdMemWord(io.brCondIdx(1)),
                    //  carry=(
                    //    False
                    //  )
                    //)(
                    //  width=cfg.mainWidth
                    //)
                    //myBinop.flagC
                  }
                }
              }
              case CondKind.Ltu => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidCond) := (
                    (!io.rFlagC)
                    //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  rExSetPcValid(enumExSetPcValidCond) := {
                    (
                      (
                        io.rdMemWord(io.brCondIdx(0))
                        < io.rdMemWord(io.brCondIdx(1))
                      )
                      //init(False)
                    )
                    //val myBinop = AluOpKind.Sub.binopFunc(
                    //  cfg=cfg,
                    //  left=io.rdMemWord(io.brCondIdx(0)),
                    //  right=io.rdMemWord(io.brCondIdx(1)),
                    //  carry=(
                    //    False
                    //  )
                    //)(
                    //  width=cfg.mainWidth
                    //)
                    //(!myBinop.flagC)
                  }
                }
              }
              case CondKind.Gtu => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidCond) := (
                    (io.rFlagC && !io.rFlagZ)
                    //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  rExSetPcValid(enumExSetPcValidCond) := {
                    (
                      (
                        io.rdMemWord(io.brCondIdx(0))
                        > io.rdMemWord(io.brCondIdx(1))
                      )
                      //init(False)
                    )
                    //val myBinop = AluOpKind.Sub.binopFunc(
                    //  cfg=cfg,
                    //  left=io.rdMemWord(io.brCondIdx(0)),
                    //  right=io.rdMemWord(io.brCondIdx(1)),
                    //  carry=(
                    //    False
                    //  )
                    //)(
                    //  width=cfg.mainWidth
                    //)

                    //(myBinop.flagC && !myBinop.flagZ)
                  }
                }
              }
              case CondKind.Leu => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidCond) := (
                    (!io.rFlagC || io.rFlagZ)
                    //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  rExSetPcValid(enumExSetPcValidCond) := {
                    (
                      (
                        io.rdMemWord(io.brCondIdx(0))
                        <= io.rdMemWord(io.brCondIdx(1))
                      )
                      //init(False)
                    )
                    //val myBinop = AluOpKind.Sub.binopFunc(
                    //  cfg=cfg,
                    //  left=io.rdMemWord(io.brCondIdx(0)),
                    //  right=io.rdMemWord(io.brCondIdx(1)),
                    //  carry=(
                    //    False
                    //  )
                    //)(
                    //  width=cfg.mainWidth
                    //)
                    //(!myBinop.flagC || myBinop.flagZ)
                  }
                }
              }
              case CondKind.Ges => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidCond) := (
                    (!(io.rFlagN ^ io.rFlagV))
                    //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  rExSetPcValid(enumExSetPcValidCond) := {
                    (
                      (
                        io.rdMemWord(io.brCondIdx(0)).asSInt
                        >= io.rdMemWord(io.brCondIdx(1)).asSInt
                      )
                      //init(False)
                    )
                    //val myBinop = AluOpKind.Sub.binopFunc(
                    //  cfg=cfg,
                    //  left=io.rdMemWord(io.brCondIdx(0)),
                    //  right=io.rdMemWord(io.brCondIdx(1)),
                    //  carry=(
                    //    False
                    //  )
                    //)(
                    //  width=cfg.mainWidth
                    //)
                    //!(myBinop.flagN ^ myBinop.flagV)
                  }
                }
              }
              case CondKind.Lts => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidCond) := (
                    (io.rFlagN ^ io.rFlagV)
                    //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  rExSetPcValid(enumExSetPcValidCond) := {
                    (
                      (
                        io.rdMemWord(io.brCondIdx(0)).asSInt
                        < io.rdMemWord(io.brCondIdx(1)).asSInt
                      )
                      //init(False)
                    )
                    //val myBinop = AluOpKind.Sub.binopFunc(
                    //  cfg=cfg,
                    //  left=io.rdMemWord(io.brCondIdx(0)),
                    //  right=io.rdMemWord(io.brCondIdx(1)),
                    //  carry=(
                    //    False
                    //  )
                    //)(
                    //  width=cfg.mainWidth
                    //)
                    //(myBinop.flagN ^ myBinop.flagV)
                  }
                }
              }
              case CondKind.Gts => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidCond) := (
                    ((!(io.rFlagN ^ io.rFlagV)) & !io.rFlagZ)
                    //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  rExSetPcValid(enumExSetPcValidCond) := {
                    (
                      (
                        io.rdMemWord(io.brCondIdx(0)).asSInt
                        > io.rdMemWord(io.brCondIdx(1)).asSInt
                      )
                      init(False)
                    )
                    //val myBinop = AluOpKind.Sub.binopFunc(
                    //  cfg=cfg,
                    //  left=io.rdMemWord(io.brCondIdx(0)),
                    //  right=io.rdMemWord(io.brCondIdx(1)),
                    //  carry=(
                    //    False
                    //  )
                    //)(
                    //  width=cfg.mainWidth
                    //)
                    //((!(myBinop.flagN ^ myBinop.flagV)) & !myBinop.flagZ)
                  }
                }
              }
              case CondKind.Les => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  rExSetPcValid(enumExSetPcValidCond) := (
                    ((io.rFlagN ^ io.rFlagV) | io.rFlagZ)
                    //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  rExSetPcValid(enumExSetPcValidCond) := {
                    (
                      (
                        io.rdMemWord(io.brCondIdx(0)).asSInt
                        <= io.rdMemWord(io.brCondIdx(1)).asSInt
                      )
                      //init(False)
                    )
                    //val myBinop = AluOpKind.Sub.binopFunc(
                    //  cfg=cfg,
                    //  left=io.rdMemWord(io.brCondIdx(0)),
                    //  right=io.rdMemWord(io.brCondIdx(1)),
                    //  carry=(
                    //    False
                    //  )
                    //)(
                    //  width=cfg.mainWidth
                    //)
                    //((myBinop.flagN ^ myBinop.flagV) | myBinop.flagZ)
                  }
                }
              }
              case CondKind.Z => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                assert(
                  opInfo.srcArr(0) == SrcKind.Gpr,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                rExSetPcValid(enumExSetPcValidOther) := (
                  (io.rdMemWord(io.brCondIdx(0)) === 0)
                  //init(False)
                  //!(io.rdMemWord(io.brCondIdx(0)).orR)
                )
              }
              case CondKind.Nz => {
                if (opInfo.dstArr.size == 1) {
                  //io.modMemWord.foreach(item => {
                  //  item := io.rdMemWord(0)
                  //})
                  //io.modMemWordValid.foreach(item => {
                  //  item := False
                  //})
                }
                assert(
                  opInfo.srcArr(0) == SrcKind.Gpr,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                rExSetPcValid(enumExSetPcValidOther) := (
                  (io.rdMemWord(io.brCondIdx(0)) =/= 0)
                  //init(False)
                  //io.rdMemWord(io.brCondIdx(0)).orR
                )
              }
              case _ => {
                assert(
                  false,
                  "not yet implemented"
                )
              }
            }
          }
        }
      }
      case OpSelect.Alu => {
        //io.opIsCpyNonJmpAlu := True
        //io.shiftModMemWord := 0x0
        assert(
          opInfo.cond == CondKind.Always,
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
        assert(
          opInfo.memAccess == MemAccessKind.NoMemAccess,
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
        assert(
          opInfo.addrCalc match {
            case AddrCalcKind.AddReduce() => {
              true
            }
            case _ => {
              false
            }
          },
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
        //io.aluModMemWordValid.foreach(_ := False)
        //io.aluOp := LcvAluDel1InpOpEnum.ZERO
        /*val binop: InstrResult =*/ opInfo.aluOp.get match {
          case AluOpKind.Add => {
            val myBinop = AluOpKind.Add.binopFunc(
              cfg=cfg,
              left=selRdMemWord(0),
              right=selRdMemWord(1),
              carry=(
                if (cfg.myHaveAluFlags) (
                  io.rFlagC
                ) else (
                  False
                )
              ),
            )(
              width=cfg.mainWidth
            )
            opInfo.dstArr(0) match {
              case DstKind.Spr(kind) => {
                //nextIndexReg := 0x0
                kind match {
                  case SprKind.AluFlags => {
                    if (opInfo.dstArr.size == 1) {
                      nextAluFlags := myBinop.main
                      io.modMemWordValid.foreach(current => {
                        current := False
                      })
                      //io.aluModMemWordValid.foreach(current => {
                      //  current := False
                      //})
                      //io.modMemWord.foreach(modMemWord => {
                      //  modMemWord := modMemWord.getZero
                      //})
                    } else {
                      nextFlagN := myBinop.flagN
                      nextFlagV := myBinop.flagV
                      nextFlagC := myBinop.flagC
                      nextFlagZ := myBinop.flagZ
                    }
                  }
                  case SprKind.Ids => {
                    nextIds := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.aluModMemWordValid.foreach(current => {
                    //  current := False
                    //})
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Ira => {
                    nextIra := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.aluModMemWordValid.foreach(current => {
                    //  current := False
                    //})
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Ie => {
                    nextIe := myBinop.main(0)
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.aluModMemWordValid.foreach(current => {
                    //  current := False
                    //})
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Ity => {
                    nextIty := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.aluModMemWordValid.foreach(current => {
                    //  current := False
                    //})
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Sty => {
                    nextSty := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.aluModMemWordValid.foreach(current => {
                    //  current := False
                    //})
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Hi => {
                    nextHi := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.aluModMemWordValid.foreach(current => {
                    //  current := False
                    //})
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Lo => {
                    nextLo := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.aluModMemWordValid.foreach(current => {
                    //  current := False
                    //})
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case _ => {
                    assert(
                      false,
                      s"not yet implemented: ${kind}"
                    )
                  }
                }
              }
              case DstKind.HiddenReg(HiddenRegKind.IndexReg) => {
                //nextIndexReg := myBinop.main
                io.modMemWordValid.foreach(current => {
                  current := False
                })
                //io.aluModMemWordValid.foreach(current => {
                //  current := False
                //})
                //io.modMemWord.foreach(modMemWord => {
                //  modMemWord := modMemWord.getZero
                //})
              }
              case _ => {
                //nextIndexReg := 0x0
              }
            }
            io.modMemWord(0) := (
              if (
                opInfo.dstArr.find(_ == DstKind.Gpr) != None
              ) {
                myBinop.main
              } else {
                selRdMemWord(0).getZero
              }
            )
          }
          case AluOpKind.Sub => {
            val myBinop = AluOpKind.Sub.binopFunc(
              cfg=cfg,
              left=selRdMemWord(0),
              right=selRdMemWord(1),
              carry=(
                if (cfg.myHaveAluFlags) (
                  io.rFlagC
                ) else (
                  False
                )
              ),
            )(
              width=cfg.mainWidth
            )
            if (opInfo.dstArr(0) == DstKind.Spr(SprKind.AluFlags)) {
              nextFlagN := myBinop.flagN
              nextFlagV := myBinop.flagV
              nextFlagC := myBinop.flagC
              nextFlagZ := myBinop.flagZ
            }
            io.modMemWord(0) := (
              if (
                opInfo.dstArr.find(_ == DstKind.Gpr) != None
              ) {
                myBinop.main
              } else {
                selRdMemWord(0).getZero
              }
            )
            //nextIndexReg := 0x0
          }
          case AluOpKind.LcvAlu(aluOp) => {
            //io.modMemWord(0) := (
            //  io.modMemWord(0).getZero
            //)
            //for (idx <- 0 until io.aluModMemWordValid.size) {
            //  when (io.upIsValid) {
            //    //if (idx == 0) {
            //    //  io.aluOp := aluOp
            //    //  io.aluInpA := selRdMemWord(0).asSInt
            //    //  io.aluInpB := selRdMemWord(1).asSInt
            //    //}
            //    io.aluModMemWordValid(idx) := (
            //      getTempModMemWordValid(idx)
            //    )
            //  } otherwise {
            //    if (idx == 0) {
            //      //io.aluOp := (
            //      //  RegNext(
            //      //    next=io.aluOp,
            //      //    init=LcvAluDel1InpOpEnum.ZERO_UINT,
            //      //  )
            //      //)
            //    }
            //    io.aluModMemWordValid(idx) := (
            //      RegNext(
            //        next=io.aluModMemWordValid(idx),
            //        init=io.aluModMemWordValid(idx).getZero,
            //      )
            //    )
            //  }
            //}
          }
          case op => {
            val binop = op.binopFunc(
              cfg=cfg,
              left=selRdMemWord(0),
              right=selRdMemWord(1),
              carry=(
                if (cfg.myHaveAluFlags) (
                  io.rFlagC
                ) else (
                  False
                )
              ),
            )(
              // TODO: support more widths than just
              // `cfg.mainWidth`
              width=cfg.mainWidth
            )
            if (opInfo.dstArr(0) == DstKind.Spr(SprKind.AluFlags)) {
              nextFlagN := binop.flagN
              nextFlagV := binop.flagV
              nextFlagC := binop.flagC
              nextFlagZ := binop.flagZ
            }
            io.modMemWord(0) := binop.main
            //nextIndexReg := 0x0
          }
        }
      }
      case OpSelect.AluShift => {
        //io.opIsAluShift := True
        //io.modMemWord.foreach(item => {
        //  item := 0x0
        //})
        //io.aluModMemWordValid.foreach(_ := False)
        //io.aluOp := LcvAluDel1InpOpEnum.ZERO
        assert(
          opInfo.cond == CondKind.Always,
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
        assert(
          opInfo.memAccess == MemAccessKind.NoMemAccess,
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
        assert(
          opInfo.addrCalc match {
            case AddrCalcKind.AddReduce() => {
              true
            }
            case _ => {
              false
            }
          },
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
        /*val binop: InstrResult =*/ opInfo.aluShiftOp.get match {
          case op => {
            val binop = op.binopFunc(
              cfg=cfg,
              left=selRdMemWord(0),
              right=selRdMemWord(1),
              carry=(
                if (cfg.myHaveAluFlags) (
                  io.rFlagC
                ) else (
                  False
                )
              ),
            )(
              // TODO: support more widths than just
              // `cfg.mainWidth`
              width=cfg.mainWidth
            )
            if (opInfo.dstArr(0) == DstKind.Spr(SprKind.AluFlags)) {
              nextFlagN := binop.flagN
              nextFlagV := binop.flagV
              nextFlagC := binop.flagC
              nextFlagZ := binop.flagZ
            }
            io.modMemWord(0) := binop.main
            io.modMemWordValid.foreach(item => {
              item := True
            })
            //io.shiftModMemWord := binop.main
            //io.shiftModMemWordValid := (
            //  //True
            //  if (cfg.myHaveZeroReg) (
            //    //!io.gprIsZeroVec(0)(idx)
            //    io.gprIsNonZeroVec(0).last
            //  ) else (
            //    True
            //  )
            //)
            //nextIndexReg := 0x0
          }
        }
      }
      case OpSelect.MultiCycle => {
        //io.shiftModMemWord := 0x0
        if (!isSingleWriteToIds) {
          //io.aluModMemWordValid.foreach(_ := False)
          //io.aluOp := LcvAluDel1InpOpEnum.ZERO
          //nextIndexReg := 0x0
        }
        for (
          ((_, innerOpInfo), idx)
          <- cfg.multiCycleOpInfoMap.view.zipWithIndex
        ) {
          if (opInfo == innerOpInfo) {
            if (!isSingleWriteToIds) {
              io.multiCycleOpInfoIdx := idx
            }
            for ((dst, dstIdx) <- opInfo.dstArr.view.zipWithIndex) {
              val tempDst = (
                //modIo.multiCycleBusVec(idx).recvData.dstVec(dstIdx)
                io.multiCycleBusRecvDataVec(idx).dstVec(dstIdx)
              )
              dst match {
                case DstKind.Gpr => {
                  // TODO: *maybe* support multiple output regs
                  io.modMemWord(0) := (
                    tempDst
                  )
                }
                case DstKind.HiddenReg(kind) => {
                  kind match {
                    case HiddenRegKind.MulHiOutp => {
                      nextMulHiOutp := tempDst
                    }
                    case HiddenRegKind.DivHiOutp => {
                      nextDivHiOutp := tempDst
                    }
                    case HiddenRegKind.ModHiOutp => {
                      nextModHiOutp := tempDst
                    }
                    case _ => {
                      assert(
                        false,
                        s"not yet implemented: "
                        + s"opInfo(${opInfo}) index:${opInfoIdx}"
                      )
                    }
                  }
                }
                case DstKind.Spr(kind) => {
                  kind match {
                    case SprKind.Hi => {
                      nextHi := tempDst
                    }
                    case SprKind.Lo => {
                      nextLo := tempDst
                    }
                    case SprKind.Ids => {
                      if (
                        isSingleWriteToIds
                        || cfg.onlyOneMultiCycleWriteToIdsOpInfo == None
                      ) {
                        nextIds := tempDst
                      }
                    }
                    case SprKind.Ira => {
                      nextIra := tempDst
                    }
                    case SprKind.Ie => {
                      nextIe := tempDst(0)
                    }
                    case _ => {
                      assert(
                        false,
                        s"not yet implemented: "
                        + s"opInfo(${opInfo}) index:${opInfoIdx}"
                      )
                    }
                  }
                }
                case _ => {
                  assert(
                    false,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                }
              }
            }
          }
        }
        //io.opIsMultiCycle(opInfoIdx) := (
        //  True
        //  //False
        //)
        assert(
          opInfo.cond == CondKind.Always,
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
        assert(
          opInfo.memAccess == MemAccessKind.NoMemAccess,
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
        assert(
          opInfo.addrCalc match {
            case AddrCalcKind.AddReduce() => {
              true
            }
            case _ => {
              false
            }
          },
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
      }
    }
  }
  //when (!io.splitOp.opIsMultiCycle) {
    //if (cfg.allMainLdstUseGprPlusImm) {
    //  io.dbusHostPayload.addr := io.rdMemWord(1) + io.imm(1)
    //}
    if (!cfg.allAluOpsUseLcvAluDel1) {
      switch (io.splitOp.cpyCpyuiAluNonShiftOp) {
        for (
          ((_, opInfo), idx)
          <- cfg.cpyCpyuiAluNonShiftOpInfoMap.view.zipWithIndex
        ) {
          //if (
          //  idx + 1 < cfg.nonMultiCycleOpInfoMap.size
          //) {
            is (
              //idx
              new MaskedLiteral(
                value=(
                  (1 << idx)
                ),
                careAbout=(
                  (1 << idx)
                  | ((1 << idx) - 1)
                ),
                width=(
                  cfg.cpyCpyuiAluNonShiftOpInfoMap.size + 1
                )
              )
            ) {
              innerFunc(
                opInfo=opInfo,
                opInfoIdx=idx,
              )
              //io.shiftModMemWord := 0x0
            }
          //}
        }
        default {
        }
      }
    } else {
      println(
        "we do have allAluOpsUseLcvAluDel1 == true"
      )
      switch (io.splitOp.cpyCpyuiOp) {
        for (
          ((_, opInfo), idx)
          <- cfg.cpyCpyuiOpInfoMap.view.zipWithIndex
        ) {
          //if (
          //  idx + 1 < cfg.nonMultiCycleOpInfoMap.size
          //) {
            is (
              //idx
              new MaskedLiteral(
                value=(
                  (1 << idx)
                ),
                careAbout=(
                  (1 << idx)
                  | ((1 << idx) - 1)
                ),
                width=(
                  cfg.cpyCpyuiOpInfoMap.size + 1
                )
              )
            ) {
              innerFunc(
                opInfo=opInfo,
                opInfoIdx=idx,
              )
              //io.shiftModMemWord := 0x0
            }
          //}
        }
        default {
        }
      }
    }
    //switch (io.splitOp.aluShiftOp) {
    //  for (
    //    ((_, opInfo), idx)
    //    <- cfg.aluShiftOpInfoMap.view.zipWithIndex
    //  ) {
    //    is (idx) {
    //      innerFunc(
    //        opInfo=opInfo,
    //        opInfoIdx=idx,
    //      )
    //      //io.modMemWord.foreach(item => {
    //      //  item := 0x0
    //      //})
    //      //io.shiftModMemWordValid := True
    //    }
    //  }
    //  default {
    //  }
    //}
    //io.shiftModMemWordValid.foreach(item => {
    //  item := (
    //    io.splitOp.aluShiftOp
    //    =/= ((1 << io.splitOp.aluShiftOp.getWidth) - 1)
    //  )
    //})
    //io.opIsAluShift.foreach(item => {
    //  item := (
    //    io.splitOp.aluShiftOp
    //    =/= ((1 << io.splitOp.aluShiftOp.getWidth) - 1)
    //  )
    //})
    switch (io.splitOp.jmpBrAlwaysEqNeOp) {
      for (
        ((fullOpInfoIdx, opInfo), idx)
        <- cfg.jmpBrAlwaysEqNeOpInfoMap.view.zipWithIndex
      ) {
        is (
          idx
          //new MaskedLiteral(
          //  value=(
          //    (1 << idx)
          //  ),
          //  careAbout=(
          //    (1 << idx)
          //    | ((1 << idx) - 1)
          //  ),
          //  width=(
          //    cfg.jmpBrAlwaysEqNeOpInfoMap.size + 1
          //  )
          //)
        ) {
          innerFunc(
            opInfo=opInfo,
            opInfoIdx=idx,
            fullOpInfoIdx=Some(fullOpInfoIdx),
          )
          //io.shiftModMemWord := 0x0
        }
      }
      default {
      }
    }
    switch (io.splitOp.jmpBrOtherOp) {
      for (
        ((fullOpInfoIdx, opInfo), idx)
        <- cfg.jmpBrOtherOpInfoMap.view.zipWithIndex
      ) {
        is (
          //idx
          new MaskedLiteral(
            value=(
              (1 << idx)
            ),
            careAbout=(
              (1 << idx)
              | ((1 << idx) - 1)
            ),
            width=(
              cfg.jmpBrOtherOpInfoMap.size + 1
            )
          )
        ) {
          innerFunc(
            opInfo=opInfo,
            opInfoIdx=idx,
            fullOpInfoIdx=Some(fullOpInfoIdx),
          )
          //io.shiftModMemWord := 0x0
        }
      }
      default {
      }
    }
  //} otherwise { // when (io.splitOp.opIsMultiCycle)
  //  //if (cfg.allMainLdstUseGprPlusImm) {
  //  //  io.dbusHostPayload.addr := RegNext(
  //  //    next=io.dbusHostPayload.addr,
  //  //    init=io.dbusHostPayload.addr.getZero,
  //  //  )
  //  //}
  //}
  def doHandleSetNextPc(): Unit = {
    switch (
      RegNext(
        next=io.splitOp.exSetNextPcKind,
        init=io.splitOp.exSetNextPcKind.getZero
      )
    ) {
      //is (SnowHousePsExSetNextPcKind.PcPlusImm) {
      //}
      is (SnowHousePsExSetNextPcKind.Dont) {
        io.psExSetPc.nextPc := (
          //io.regPcPlusImm 
          RegNext(
            next=io.psExSetPc.nextPc,
            init=io.psExSetPc.nextPc.getZero,
          )
        )
      }
      is (SnowHousePsExSetNextPcKind.PcPlusImm) {
        io.psExSetPc.nextPc := (
          //Mux[UInt](
          //  tempPsExSetPcTaken,
            RegNext(
              next=io.regPcPlusImm,
              init=io.regPcPlusImm.getZero,
            ) //+ cfg.instrSizeBytes
            //io.regPcPlusImmRealDst//, //+ cfg.instrSizeBytes
          //  io.mySavedRegPcPlusInstrSize/*.payload*/,
          //  //RegNextWhen(
          //  //  next=RegNextWhen(
          //  //    next=io.regPc,
          //  //    cond=io.upIsFiring,
          //  //    init=io.regPc.getZero,
          //  //  ),
          //  //  cond=io.upIsFiring,
          //  //  init=io.regPc.getZero,
          //  //) + (1 * cfg.instrSizeBytes),
          //)
          //+ Mux[UInt](
          //  /*RegNext*/(
          //    /*next=*/(
          //      //io.btbElemValid
          //      //&& !io.btbElemDontPredict
          //      //&& 
          //      io.branchPredictTkn
          //    ),
          //  //  init=False,
          //  ),
          //  U(s"${cfg.mainWidth}'d${2 * cfg.instrSizeBytes}"),
          //  //(-S(s"${cfg.mainWidth}'d1")).asUInt,
          //  U(s"${cfg.mainWidth}'d0"),
          //)
        )
      }
      is (SnowHousePsExSetNextPcKind.RdMemWord) {
        io.psExSetPc.nextPc := (
          RegNext(
            io.rdMemWord(io.jmpAddrIdx) //- (1 * cfg.instrSizeBytes)
            - (3 * cfg.instrSizeBytes)
          )
          init(0x0)
        )
      }
      is (SnowHousePsExSetNextPcKind.Ira) {
        io.psExSetPc.nextPc := (
          RegNext(
            io.rIra
            - (3 * cfg.instrSizeBytes)
          )
          init(0x0)
        )
      }
      is (SnowHousePsExSetNextPcKind.Ids) {
        io.psExSetPc.nextPc := (
          RegNext(
            io.rIds
            - (3 * cfg.instrSizeBytes)
          )
          init(0x0)
        )
      }
      //default {
      //  io.psExSetPc.nextPc := (
      //    io.regPcPlusImm 
      //  )
      //}
    }
  }
  if (cfg.onlyOneMultiCycleWriteToIdsOpInfo != None) {
    for (
      ((_, opInfo), idx)
      <- cfg.multiCycleOpInfoMap.view.zipWithIndex
    ) {
      if (opInfo == cfg.onlyOneMultiCycleWriteToIdsOpInfo.get) {
        innerFunc(
          opInfo=opInfo,
          opInfoIdx=idx,
          isSingleWriteToIds=true,
        )
      }
    }
  }
  switch (
    io.inMultiCycleOp
    ## (
      RegNext(io.splitOp.multiCycleOp)
      init(0x1) // arbitrarily chosen, but still set to something one-hot
    )
  ) {
    for (
      ((_, opInfo), idx)
      <- cfg.multiCycleOpInfoMap.view.zipWithIndex
    ) {
      /*when*/ is(
        //idx
        //io.splitOp.multiCycleOp(idx)
        new MaskedLiteral(
          value=(
            (1 << idx)
            | (1 << io.splitOp.multiCycleOp.getWidth)
          ),
          careAbout=(
            (1 << idx)
            | ((1 << idx) - 1)
            | (1 << io.splitOp.multiCycleOp.getWidth)
          ),
          width=(
            cfg.multiCycleOpInfoMap.size + 1
          )
        )
      ) {
        innerFunc(
          opInfo=opInfo,
          opInfoIdx=idx,
          isSingleWriteToIds=false,
        )
        //cfg.onlyOneMultiCycleWriteToIdsOpInfo match {
        //  case Some(writeToIdsOpInfo) => {
        //    if (opInfo != writeToIdsOpInfo) {
        //      innerFunc(
        //        opInfo=opInfo,
        //        opInfoIdx=idx,
        //        isSingleWriteToIds=false,
        //      )
        //    }
        //  }
        //  case None => {
        //    innerFunc(
        //      opInfo=opInfo,
        //      opInfoIdx=idx,
        //      isSingleWriteToIds=false,
        //    )
        //  }
        //}
      }
    }
  }
  //switch (io.splitOp.fullOp) {
  //  for (
  //    ((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex
  //  ) {
  //    is (opInfoIdx) {
  //      //innerFunc(
  //      //  opInfo=opInfo,
  //      //  opInfoIdx=idx,
  //      //)
  //      for (
  //        ((_, cpyOpInfo), idx)
  //        <- cfg.cpyCpyuiOpInfoMap.view.zipWithIndex
  //      ) {
  //        if (opInfo == cpyOpInfo) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //      for (
  //        ((_, jmpOpInfo), idx)
  //        <- cfg.jmpBrAlwaysEqNeOpInfoMap.view.zipWithIndex
  //      ) {
  //        if (opInfo == jmpOpInfo) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //      for (
  //        ((_, aluOpInfo), idx)
  //        <- cfg.aluOpInfoMap.view.zipWithIndex
  //      ) {
  //        if (opInfo == aluOpInfo) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //      for (
  //        ((_, aluShiftOpInfo), idx)
  //        <- cfg.aluShiftOpInfoMap.view.zipWithIndex
  //      ) {
  //        if (opInfo == aluShiftOpInfo) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //      for (
  //        ((_, multiCycleOpInfo), idx)
  //        <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  //      ) {
  //        if (opInfo == multiCycleOpInfo) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //    }
  //  }
  //}
  //switch (io.splitOp.kind) {
  //  is (SnowHouseSplitOpKind.CPY_CPYUI) {
  //    switch (io.splitOp.cpyCpyuiOp) {
  //      for (
  //        ((_, opInfo), idx) <- cfg.cpyCpyuiOpInfoMap.view.zipWithIndex
  //      ) {
  //        is (idx) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //    }
  //  }
  //  is (SnowHouseSplitOpKind.JMP_BR) {
  //    switch (io.splitOp.jmpBrAlwaysEqNeOp) {
  //      for (
  //        ((_, opInfo), idx) <- cfg.jmpBrAlwaysEqNeOpInfoMap.view.zipWithIndex
  //      ) {
  //        is (idx) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //    }
  //  }
  //  is (SnowHouseSplitOpKind.ALU) {
  //    switch (io.splitOp.aluOp) {
  //      for (
  //        ((_, opInfo), idx) <- cfg.aluOpInfoMap.view.zipWithIndex
  //      ) {
  //        is (idx) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //    }
  //  }
  //  is (SnowHouseSplitOpKind.ALU_SHIFT) {
  //    switch (io.splitOp.aluShiftOp) {
  //      for (
  //        ((_, opInfo), idx) <- cfg.aluShiftOpInfoMap.view.zipWithIndex
  //      ) {
  //        is (idx) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //    }
  //  }
  //  is (SnowHouseSplitOpKind.MULTI_CYCLE) {
  //    switch (io.splitOp.multiCycleOp) {
  //      for (
  //        ((_, opInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  //      ) {
  //        is (idx) {
  //          innerFunc(
  //            opInfo=opInfo,
  //            opInfoIdx=idx,
  //          )
  //        }
  //      }
  //    }
  //  }
  //}
  if (cfg.myHaveZeroReg) {
    for (idx <- 0 until io.gprIsZeroVec.size) {
      when (io.gprIsZeroVec.last(0)) {
        io.modMemWord(0) := 0x0
      }
    }
  }
  //when (
  //  //!rShouldIgnoreInstrState(1)
  //  RegNext(
  //    next=(!io.shouldIgnoreInstr(1)),
  //    init=False,
  //  )
  //) {
    //if (idx == 1) {
      //io.psExSetPc.nextPc := (
      //  io.regPcPlusImm 
      //)
      //io.shouldIgnoreInstr := False
      doHandleSetNextPc()
    //}
  //}
  io.psExSetPc.branchTgtBufElem.dontPredict.allowOverride
  io.psExSetPc.branchTgtBufElem.dontPredict := (
    io.btbElemDontPredict
  )
  when (
    //!io.takeIrq
    //&&
    tempBranchMispredictNotTaken
    //&& !tempReplaceBtbElem
    //&& !RegNext(
    //  next=io.branchPredictReplaceBtbElem,
    //  init=io.branchPredictReplaceBtbElem.getZero,
    //)
    //&& !tempReplaceBtbElem
  ) {
    io.psExSetPc.nextPc := (
      //RegNextWhen(
      //  next=io.mySavedRegPcPlusInstrSize,
      //  cond=io.upIsFiring,
      //  init=io.mySavedRegPcPlusInstrSize.getZero,
      //)
      io.mySavedRegPcPlusInstrSize
    )
    io.psExSetPc.branchTgtBufElem.dontPredict := True
  }
  //when (io.takeIrq) {
  //  //tempBranchMispredictNotTaken := (
  //  //  //myPsExSetPcValid
  //  //  False
  //  //  //True
  //  //)
  //  io.psExSetPc.branchTgtBufElem.dontPredict := True
  //}
  def doShouldIgnoreState2(): Unit = {
    //io.aluModMemWordValid.foreach(current => {
    //  current := False
    //})
    //io.aluOp := LcvAluDel1InpOpEnum.ZERO
    //io.myModMemWord.foreach(myModMemWord => {
    //  myModMemWord := myModMemWord.getZero
    //})
    io.modMemWordValid.foreach(current => {
      current := False
    })
    io.modMemWord.foreach(modMemWord => {
      modMemWord := modMemWord.getZero
    })
    //io.shiftModMemWordValid.foreach(item => {
    //  item := False
    //})
    //io.shiftModMemWord := 0x0
    //io.opIs := 0x0
    //io.opIsAluShift.foreach(item => {
    //  item := False
    //})
    io.opIsMemAccess.foreach(item => {
      item := False
    })
    io.opIsAnyMultiCycle := (
      False
    )
    io.opIsMultiCycle.foreach(item => {
      item := False
    })
  }

  for (idx <- 0 until rShouldIgnoreInstrState.size) {
    when (
      io.regPcSetItCnt(idx)(0)
      && io.upIsValid
    ) {
      //io.shouldIgnoreInstr(idx) := False
      when (
        io.rHadRetIra
      ) {
        nextIe := True
      }
    } 
    //elsewhen (
    //  tempPsExSetPcValid
    //  //fell(
    //  //stickyTempPsExSetPcValid
    //  //)
    //  //&& io.upIsValid
    //  //&& io.upIsFiring
    //  && (
    //    RegNext(
    //      next=(
    //        !io.shouldIgnoreInstr(idx)
    //        && io.upIsFiring
    //      ),
    //      init=False,
    //    )
    //    //|| (
    //    //  io.takeIrq
    //    //)
    //  )
    //) {
    //  io.shouldIgnoreInstr(idx) := True
    //}

    if (idx == 2) {
      when (io.shouldIgnoreInstr(idx)) {
        doShouldIgnoreState2()
      }
    }
  }
  //--------
  when (
    rose(myPsExSetPcCmpEq.rValid)
  ) {
    myPsExSetPcCmpEq.myCmp.msb := (
      //myPsExSetPcCmpEq.cmpEqQ
      //(
      //  //Cat(myPsExSetPcCmpEq.cmpEq, myPsExSetPcCmpEq.cmpEqQ).asUInt >> 1
      //  //myPsExSetPcCmpEq.cmpEq << myPsExSetPcCmpEq.cmpEqQ.getWidth
      //  Cat(myPsExSetPcCmpEq.cmpEq).asSInt.resize(
      //    myPsExSetPcCmpEq.myCmp.getWidth
      //  ).asUInt
      //)
      myPsExSetPcCmpEq.cmpEq
    )
  }
  when (/*RegNext*/(/*next=*/myPsExSetPcCmpEq.rValid/*, init=False*/)) {
    when (
      io.shouldIgnoreInstr(3)
    ) {
      myPsExSetPcCmpEq.rValid := False
    }
    when (io.upIsFiring) {
      myPsExSetPcCmpEq.rValid := False
    }
  }
  when (rose(myPsExSetPcCmpNe.rValid)) {
    myPsExSetPcCmpNe.myCmp.msb := (
      //~myPsExSetPcCmpNe.cmpEqQ
      //(
      //  Cat(!myPsExSetPcCmpNe.cmpEq, myPsExSetPcCmpNe.cmpEqQ).asUInt >> 1
      //)
      //Cat(!myPsExSetPcCmpNe.cmpEq).asSInt.resize(
      //  myPsExSetPcCmpNe.myCmp.getWidth
      //).asUInt
      !myPsExSetPcCmpNe.cmpEq
    )
  }
  when (/*RegNext*/(/*next=*/myPsExSetPcCmpNe.rValid/*, init=False*/)) {
    when (io.shouldIgnoreInstr(3)) {
      myPsExSetPcCmpNe.rValid := False
    }
    when (io.upIsFiring) {
      myPsExSetPcCmpNe.rValid := False
    }
  }
  //when (io.aluModMemWordValid.head) {
  //  io.modMemWord.foreach(item => {
  //    item := item.getZero
  //  })
  //} otherwise {
  //  //io.myModMemWord.foreach(item => {
  //  //  item := item.getZero
  //  //})
  //  //io.aluOp := LcvAluDel1InpOpEnum.ZERO
  //}
}

case class SnowHousePipeStageExecute(
  args: SnowHousePipeStageArgs,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
  //psMemStallHost: LcvStallHost[
  //  BusHostPayload,
  //  BusDevPayload,
  //],
  //myDbusIo: SnowHouseDbusIo,
  doModInMid0FrontParams: PipeMemRmwDoModInMid0FrontFuncParams[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeMemRmwDualRdTypeDisabled[UInt, Bool],
  ],
  pcChangeState: Bool/*UInt*/,
  shouldIgnoreInstr: Bool,
  myModMemWord: SInt,
) extends Area {
  def myDbusIo = args.myDbusIo
  def myDbus = myDbusIo.dbus
  def myDbusExtraReady = myDbusIo.dbusExtraReady
  def myDbusLdReady = myDbusIo.dbusLdReady
  def cfg = args.cfg
  def io = args.io
  def nextPrevTxnWasHazard = (
    doModInMid0FrontParams.nextPrevTxnWasHazardVec(0)
  )
  def rPrevTxnWasHazard = (
    doModInMid0FrontParams.rPrevTxnWasHazardVec(0)
  )
  def rPrevTxnWasHazardAny = (
    doModInMid0FrontParams.rPrevTxnWasHazardAny
  )
  def outp = doModInMid0FrontParams.outp//Vec(ydx)
  def inp = doModInMid0FrontParams.inp//Vec(ydx)
  def cMid0Front = doModInMid0FrontParams.cMid0Front
  def tempModFrontPayload = (
    doModInMid0FrontParams.tempModFrontPayload//Vec(ydxr
  )
  if (cfg.optFormal) {
    if ((1 << outp.op.getWidth) != cfg.opInfoMap.size) {
      assume(inp.op < cfg.opInfoMap.size)
      assume(outp.op < cfg.opInfoMap.size)
    }
  }
  def regFileFwd = doModInMid0FrontParams.myFwd //args.regFile
  //def myDbus = (
  //  psMemStallHost.stallIo.get
  //)
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
      optFormalJustHost=cfg.optFormal,
    )
  }
  val havePsExStall = cfg.havePsExStall
  val psExStallHostArr = ArrayBuffer[LcvStallHost[
    MultiCycleHostPayload, MultiCycleDevPayload
  ]]()
  for (
    ((_, opInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    psExStallHostArr += (
      cfg.mkLcvStallHost[MultiCycleHostPayload, MultiCycleDevPayload](
        stallIo=(
          Some(io.multiCycleBusVec(idx))
        ),
      )
    )
  }
  //val nextSetOutpState = (
  //  Vec.fill(3)(
  //    Bool()
  //  )
  //)
  //val rSetOutpState = (
  //  RegNext(
  //    next=nextSetOutpState,
  //    //init=nextSetOutpState.getZero,
  //  )
  //)
  //for (idx <- 0 until nextSetOutpState.size) {
  //  rSetOutpState(idx).init(nextSetOutpState(idx).getZero)
  //  nextSetOutpState(idx) := rSetOutpState(idx)
  //}
  val myShouldIgnoreInstr = (
    Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
      Bool()
    )
  )
  for (idx <- 0 until cfg.lowerMyFanoutRegPcSetItCnt) {
    outp.instrCnt.shouldIgnoreInstr(idx) := (
      myShouldIgnoreInstr(idx)
    )
  }
  //val rTakeJumpState /*rTakeJumpCnt*/ = {
  //  //val temp = Reg(Flow(UInt(
  //  //  log2Up(cfg.takeJumpCntMaxVal + 1 /*+ 3*/) + 1 bits
  //  //)))
  //  //temp.init(temp.getZero)
  //  //temp
  //  Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
  //    Reg(Bool(), init=False)
  //  )
  //}
  //when (
  //  rTakeJumpCnt.fire
  //) {
  //}
  for (idx <- 0 until cfg.lowerMyFanoutRegPcSetItCnt) {
    myShouldIgnoreInstr(idx) := (
      //rTakeJumpState(idx)
      RegNext(
        next=myShouldIgnoreInstr(idx),
        init=myShouldIgnoreInstr(idx).getZero,
      )
    )
  }
  outp := (
    RegNext(
      next=outp,
      init=outp.getZero,
    )
  )
  outp.allowOverride
  def myRdMemWord(
    ydx: Int,
    modIdx: Int,
  ) = (
    doModInMid0FrontParams.getMyRdMemWordFunc(ydx, modIdx)
  )
  //when (!io.ibus.ready) {
  //  cMid0Front.haltIt()
  //}
  when (cMid0Front.up.isValid) {
    outp := inp
    //when (
    //  //!rSetOutpState
    //  !rSetOutpState(0)
    //  //!RegNext(next=nextSetOutpState, init=nextSetOutpState.getZero)
    //) {
    //  outp := inp
    //}
    ////when (
    ////  !RegNext(next=nextSetOutpState, init=nextSetOutpState.getZero)
    ////) {
    ////  tempExt := inp.myExt
    ////}
    //when (
    //  //!RegNext(next=nextSetOutpState, init=nextSetOutpState.getZero)
    //  !rSetOutpState(1)
    //) {
    //  nextSetOutpState.foreach(current => {
    //    current := True
    //  })
    //}
  }
  //when (cMid0Front.up.isFiring) {
  //  nextSetOutpState.foreach(current => {
  //    current := False
  //  })
  //}
  for (ydx <- 0 until outp.myExt.size) {
    outp.myExt(ydx).rdMemWord := (
      inp.myExt(ydx).rdMemWord
    )
    //for (zdx <- 0 until outp.myExt(ydx).fwdIdx.size) {
    //  outp.myExt(ydx).fwdIdx(zdx) := (
    //    inp.myExt(ydx).fwdIdx(zdx)
    //  )
    //}
  }
  //val savedPsMemStallHost = (
  //  LcvStallHostSaved(
  //    stallHost=psMemStallHost,
  //    someLink=cMid0Front,
  //  )
  //)
  def stallKindMem = 0
  //def stallKindMultiCycle = 1
  //def stallKindMultiCycle1 = 2
  //def stallKindAluShift = 1
  def stallKindLim = (
    //3
    //2
    1
  )

  val myDoStall = (
    /*KeepAttribute*/(
      Vec.fill(stallKindLim)(
        Bool()
      )
    )
  )
  myDoStall(stallKindMem) := False
  //myDoStall(stallKindMultiCycle) := (
  //  RegNext(
  //    next=myDoStall(stallKindMultiCycle),
  //    init=myDoStall(stallKindMultiCycle).getZero,
  //  )
  //)
  //myDoStall(stallKindMultiCycle1) := (
  //  False
  //)
  val setOutpModMemWord = SnowHousePipeStageExecuteSetOutpModMemWord(
    args=args
  )
  for (
    ((_, opInfo), idx)
    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    for ((dst, dstIdx) <- opInfo.dstArr.view.zipWithIndex) {
      val tempDst = (
        //modIo.multiCycleBusVec(idx).recvData.dstVec(dstIdx)
        setOutpModMemWord.io.multiCycleBusRecvDataVec(idx).dstVec(dstIdx)
      )
      tempDst := (
        args.io.multiCycleBusVec(idx).recvData.dstVec(dstIdx)
      )
    }
  }
  val doCheckHazard = (
    Vec.fill(
      //cfg.multiCycleOpInfoMap.size + 1
      1
    )(
      Bool()
    )
  )
  val myNextPrevTxnWasHazardVec = (
    Vec.fill(
      //cfg.multiCycleOpInfoMap.size + 1
      1
    )(
      Bool()
    )
  )
  myNextPrevTxnWasHazardVec.foreach(current => {
    current := nextPrevTxnWasHazard
  })

  //val myDoHaveHazardAddrCheckVec = Vec[Bool](
  //  {
  //    assert(
  //      outp.myExt.size == cfg.regFileCfg.memArrSize
  //    )
  //    val temp = ArrayBuffer[Bool]()
  //    // TODO: support multiple register writes per instruction
  //    //val tempArr = ArrayBuffer[Bool]()
  //    //for (idx <- 0 until outp.gprIdxVec.size) {
  //    //  tempArr += (
  //    //    //(
  //    //    //  //outp.gprIdxVec(idx)
  //    //    //  outp.myExt(0).memAddr(idx)
  //    //    //  === (
  //    //    //    //tempModFrontPayload.gprIdxVec(0)
  //    //    //    // TODO: *maybe* support multiple output registers!
  //    //    //    tempModFrontPayload.myExt(0).memAddr(0)
  //    //    //  )
  //    //    //) ||
  //    //    (
  //    //      //True
  //    //      //outp.gprIdxVec(idx)
  //    //      outp.myExt(0).memAddr(idx)
  //    //      === RegNextWhen(
  //    //        next=(
  //    //          //outp.gprIdxVec(0)
  //    //          outp.myExt(0).memAddr(0)
  //    //        ),
  //    //        cond=cMid0Front.up.isFiring,
  //    //        init=(
  //    //          //outp.gprIdxVec(0).getZero
  //    //          outp.myExt(0).memAddr(0).getZero
  //    //        ),
  //    //      )
  //    //    )
  //    //  )
  //    //}
  //    // TODO: support multiple register writes per instruction
  //    temp += (
  //      outp.myDoHaveHazardAddrCheckVec(0)
  //    )

  //    temp
  //  },
  //  Bool()
  //)
  val myDoHaveHazardValidCheckVec = Vec[Bool](
    {
      val temp = ArrayBuffer[Bool]()
      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        val tempYdx = (
          if (ydx < cfg.regFileCfg.modMemWordValidSize) (
            ydx
          ) else (
            cfg.regFileCfg.modMemWordValidSize - 1
          )
        )
        temp += (
          !tempModFrontPayload.myExt(ydx).modMemWordValid(tempYdx)
        )
      }
      temp
    },
    Bool()
  )
  val myDoHaveHazardVec = /*KeepAttribute*/(
    Vec[Bool]{
      val tempArr = ArrayBuffer[Bool]()
      //assert(
      //  myDoHaveHazardAddrCheckVec.size
      //  == myDoHaveHazardValidCheckVec.size,
      //  s"${myDoHaveHazardAddrCheckVec.size} "
      //  + s"${myDoHaveHazardValidCheckVec.size}"
      //)
      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        tempArr += (
          //myDoHaveHazardAddrCheckVec(ydx)
          //&& 
          myDoHaveHazardValidCheckVec(ydx)
        )
      }
      tempArr
    }
  )
  //val myDoHaveHazard1 = (
  //  myDoHaveHazardVec.reduceLeft(_ || _)
  //)
  val myDoHaveHazard = /*KeepAttribute*/(
    Vec.fill(
      //cfg.multiCycleOpInfoMap.size + 1
      1
    )(
      myDoHaveHazardVec.reduceLeft(_ || _)
    )
  )
  setOutpModMemWord.io.irqIraRegPc := (
    outp.irqIraRegPc
  )
  val rIrqHndlState = {
    val temp = Reg(
      Bool()
    )
    temp.init(temp.getZero)
    temp
  }
  val tempTakeIrqCond = (
    cfg.irqCfg != None
  ) generate (
    /*LcvFastAndR*/(
      Vec[Bool](
        //cMid0Front.up.isValid,
        ////RegNextWhen(
        ////  next=setOutpModMemWord.nextIe,
        ////  cond=cMid0Front.up.isFiring,
        ////  init=False,
        ////),
        setOutpModMemWord.io.rIe,
        ////!setOutpModMemWord.io.shouldIgnoreInstr(0),
        ////!shouldIgnoreInstr
        //!myShouldIgnoreInstr(0)
        ////cMid0Front.up.isFiring,
      ).asBits.asUInt.andR
    )
  )
  val rHadIrqReady = (
    cfg.irqCfg != None
  ) generate (
    Reg(
      Bool(),
      init=False
    )
  )
  val rHaveIrqValid = (
    cfg.irqCfg != None
  ) generate (
    RegNext(
      next=RegNext(
        next=io.idsIraIrq.nextValid,
        init=False,
      ),
      init=False,
    )
  )
  val nextMyTakeIrq = (
    cfg.irqCfg != None
  ) generate (
    Bool()
    //Reg(
    //  Bool(),
    //  init=False,
    //)
  )
  val rMyTakeIrq = (
    cfg.irqCfg != None
  ) generate (
    RegNextWhen(
      next=nextMyTakeIrq,
      cond=cMid0Front.up.isFiring,
      init=nextMyTakeIrq.getZero,
    )
  )
  if (cfg.irqCfg != None) {
    nextMyTakeIrq := rMyTakeIrq
    io.idsIraIrq.ready := False
    val tempCond = (
      //setOutpModMemWord.io.regPcSetItCnt(0)(0)
      //&& setOutpModMemWord.io.upIsValid
      //!setOutpModMemWord.io.shouldIgnoreInstr(0)
      //!shouldIgnoreInstr
      !myShouldIgnoreInstr(0)
      || (
        //cMid0Front.
        cMid0Front.up.isValid
        && setOutpModMemWord.io.regPcSetItCnt(0)(0)
      )
    )
    when (
      (
        rHaveIrqValid
      ) && (
        RegNext/*When*/(
          next=tempTakeIrqCond,
          //cond=cMid0Front.up.isFiring,
          init=tempTakeIrqCond.getZero,
        )
      )
    ) {
      when (
        cMid0Front.up.isFiring
        && !rMyTakeIrq
      ) {
        nextMyTakeIrq := (
          //rTempTakeIrq
          //True
          tempCond
        )
      }
    }
    when (
      rMyTakeIrq
      && /*RegNext*/(
        /*next=*/cMid0Front.up.isFiring//,
        //init=False,
      )
      //&& cMid0Front.up.isFiring
      //&& !setOutpModMemWord.io.psExSetPc.valid
      //&& !setOutpModMemWord.io.shouldIgnoreInstr
      && tempCond
      //&& psExSetPc.valid
    ) {
      nextMyTakeIrq := False
      io.idsIraIrq.ready := True
    }
    when (
      RegNext(
        next=io.idsIraIrq.ready,
        init=io.idsIraIrq.ready.getZero,
      )
    ) {
      io.idsIraIrq.ready := False
    }
  }

  setOutpModMemWord.io.regPcSetItCnt := outp.regPcSetItCnt
  setOutpModMemWord.io.mySavedRegPcPlusInstrSize := (
    //outp.myHistRegPcPlusInstrSize.head
    RegNextWhen(
      next=(
        outp.branchTgtBufElem(1).srcRegPc
        //+ (1 * cfg.instrSizeBytes)
        //- (1 * cfg.instrSizeBytes)
        - (3 * cfg.instrSizeBytes)
      ),
      cond=cMid0Front.up.isFiring,
      init=outp.branchTgtBufElem(1).srcRegPc.getZero,
    )
  )
  setOutpModMemWord.io.regPc := outp.regPc
  setOutpModMemWord.io.regPcPlusInstrSize := outp.regPcPlusInstrSize
  setOutpModMemWord.io.regPcPlusImm := outp.regPcPlusImm
  //setOutpModMemWord.io.regPcPlusImmRealDst := (
  //  outp.branchTgtBufElem(1).dstRegPc
  //)
  setOutpModMemWord.io.imm := outp.imm
  setOutpModMemWord.io.inpDecodeExt := outp.inpDecodeExt
  outp.outpDecodeExt := setOutpModMemWord.io.outpDecodeExt
  outp.psExSetPc := outp.psExSetPc.getZero
  //outp.psExSetPc := psExSetPc
  if (io.haveMultiCycleBusVec) {
    for (
      (multiCycleBus, busIdx) <- io.multiCycleBusVec.view.zipWithIndex
    ) {
      for (idx <- 0 until multiCycleBus.sendData.srcVec.size) {
        multiCycleBus.sendData.srcVec(idx) := (
          RegNext(
            next=multiCycleBus.sendData.srcVec(idx),
            init=multiCycleBus.sendData.srcVec(idx).getZero,
          )
        )
      }
    }
  }
  if (cfg.myHaveZeroReg) {
    for ((gprIdx, idx) <- outp.gprIdxVec.view.zipWithIndex) {
      for (jdx <- 0 until outp.gprIsNonZeroVec(idx).size) {
        setOutpModMemWord.io.gprIsNonZeroVec(idx)(jdx) := (
          outp.gprIsNonZeroVec(idx)(jdx)
        )
      }
      for (jdx <- 0 until outp.gprIsZeroVec(idx).size) {
        setOutpModMemWord.io.gprIsZeroVec(idx)(jdx) := (
          outp.gprIsZeroVec(idx)(jdx)
        )
      }
    }
  }
  setOutpModMemWord.io.upIsFiring := cMid0Front.up.isFiring
  setOutpModMemWord.io.upIsValid := cMid0Front.up.isValid
  setOutpModMemWord.io.upIsReady := cMid0Front.up.isReady
  setOutpModMemWord.io.downIsFiring := cMid0Front.down.isFiring
  setOutpModMemWord.io.downIsValid := cMid0Front.down.isValid
  setOutpModMemWord.io.downIsReady := cMid0Front.down.isReady

  val alu = LcvAluDel1(
    wordWidth=cfg.mainWidth
  )
  //alu.io.inp_a := setOutpModMemWord.io.aluInpA
  //alu.io.inp_b := setOutpModMemWord.io.aluInpB
  //alu.io.inp_op := setOutpModMemWord.io.aluOp
  //alu.io.inp_a := (
  //  RegNext(
  //    next=alu.io.inp_a,
  //    init=alu.io.inp_a.getZero,
  //  )
  //)
  //alu.io.inp_b := (
  //  RegNext(
  //    next=alu.io.inp_b,
  //    init=alu.io.inp_b.getZero,
  //  )
  //)
  //alu.io.inp_op := (
  //  RegNext(
  //    next=alu.io.inp_op,
  //    init=(
  //      LcvAluDel1InpOpEnum.ZERO_UINT
  //      //alu.io.inp_op.getZero
  //    ),
  //  )
  //)
  //alu.io.inp_b_sel := (
  //  RegNext(
  //    next=alu.io.inp_b_sel,
  //    init=alu.io.inp_b_sel.getZero,
  //  )
  //)
  //myModMemWord := (
  //  RegNext(
  //    next=myModMemWord,
  //    init=myModMemWord.getZero,
  //  )
  //  //RegNextWhen(
  //  //  next=alu.io.inp_a,
  //  //  cond=cMid0Front.up.isFiring,
  //  //  init=alu.io.inp_a.getZero,
  //  //)
  //)

  //myModMemWord := (
  //  RegNext(
  //    next=myModMemWord,
  //    init=myModMemWord.getZero,
  //  )
  //  //RegNextWhen(
  //  //  next=alu.io.inp_a,
  //  //  cond=cMid0Front.up.isFiring,
  //  //  init=alu.io.inp_a.getZero,
  //  //)
  //)
  //myModMemWord := (
  //  alu.io.outp_data
  //)
  //--------
  // BEGIN: this worked pretty well for fmax, so let's try another approach
  val mostTempToSwitchMyModMemWord = (
    (
      RegNext(
        next=(
          cMid0Front.up.isFiring
          && setOutpModMemWord.io.modMemWordValid.head
          //&& alu.io.inp_op =/= LcvAluDel1InpOpEnum.OP_GET_INP_A
        ),
        init=False,
      )
    ) ## (
      RegNext(
        next=(
          //alu.io.inp_op === LcvAluDel1InpOpEnum.OP_GET_INP_A
          alu.io.inp_op === LcvAluDel1InpOpEnum.ZERO
        ),
        init=False,
      )
    )
  )
  val tempToSwitchMyModMemWord = (
    if (!cfg.useLcvDataBus) (
      mostTempToSwitchMyModMemWord
      ## (
        //rose(
          myDbusLdReady
        //)
        && rose(
          myDbus.ready
        )
        //RegNext(
        //  next=(
        //  ),
        //  init=False
        //)
      )
    ) else (
      mostTempToSwitchMyModMemWord
    )
  )
  switch (tempToSwitchMyModMemWord) {
    is (
      if (!cfg.useLcvDataBus) (
        M"100"
      ) else (
        M"10"
      )
    ) {
      myModMemWord := (
        RegNext(
          next=myModMemWord,
          init=myModMemWord.getZero,
        )
      )
      when (RegNext(cMid0Front.up.isFiring, init=False)) {
        myModMemWord := alu.io.outp_data
      }
    }
    is (
      if (!cfg.useLcvDataBus) (
        M"110"
      ) else (
        M"11"
      )
    ) {
      myModMemWord := (
        RegNext(
          next=myModMemWord,
          init=myModMemWord.getZero,
        )
      )
      when (RegNext(cMid0Front.up.isFiring, init=False)) {
        myModMemWord := (
          RegNext(
            next=setOutpModMemWord.io.modMemWord(0).asSInt,
            init=setOutpModMemWord.io.modMemWord(0).asSInt.getZero
          )
        )
      }
    }
    if (!cfg.useLcvDataBus) {
      is (M"--1") {
        myModMemWord := myDbus.recvData.data.asSInt.resized
      }
    }
    default {
      myModMemWord := (
        RegNext(
          next=myModMemWord,
          init=myModMemWord.getZero,
        )
        //RegNextWhen(
        //  next=alu.io.inp_a,
        //  cond=cMid0Front.up.isFiring,
        //  init=alu.io.inp_a.getZero,
        //)
      )
    }
  }
  //switch (
  //  (
  //    RegNext(
  //      next=(
  //        cMid0Front.up.isFiring
  //        && setOutpModMemWord.io.modMemWordValid.head
  //        //&& alu.io.inp_op =/= LcvAluDel1InpOpEnum.OP_GET_INP_A
  //      ),
  //      init=False,
  //    )
  //  ) ## (
  //    RegNext(
  //      next=(
  //        //alu.io.inp_op === LcvAluDel1InpOpEnum.OP_GET_INP_A
  //        alu.io.inp_op === LcvAluDel1InpOpEnum.ZERO
  //      ),
  //      init=False,
  //    )
  //  ) ## (
  //    //rose(
  //      myDbusLdReady
  //    //)
  //    && rose(
  //      myDbus.ready
  //    )
  //    //RegNext(
  //    //  next=(
  //    //  ),
  //    //  init=False
  //    //)
  //  )
  //) {
  //  is (M"100") {
  //    myModMemWord := (
  //      RegNext(
  //        next=myModMemWord,
  //        init=myModMemWord.getZero,
  //      )
  //    )
  //    when (RegNext(cMid0Front.up.isFiring, init=False)) {
  //      myModMemWord := alu.io.outp_data
  //    }
  //  }
  //  is (M"110") {
  //    myModMemWord := (
  //      RegNext(
  //        next=myModMemWord,
  //        init=myModMemWord.getZero,
  //      )
  //    )
  //    when (RegNext(cMid0Front.up.isFiring, init=False)) {
  //      myModMemWord := (
  //        RegNext(
  //          next=setOutpModMemWord.io.modMemWord(0).asSInt,
  //          init=setOutpModMemWord.io.modMemWord(0).asSInt.getZero
  //        )
  //      )
  //    }
  //  }
  //  is (M"--1") {
  //    myModMemWord := (
  //      myDbus.recvData.data.asSInt.resized
  //    )
  //  }
  //  default {
  //    myModMemWord := (
  //      RegNext(
  //        next=myModMemWord,
  //        init=myModMemWord.getZero,
  //      )
  //      //RegNextWhen(
  //      //  next=alu.io.inp_a,
  //      //  cond=cMid0Front.up.isFiring,
  //      //  init=alu.io.inp_a.getZero,
  //      //)
  //    )
  //  }
  //}
  // END: this worked pretty well for fmax, so let's try another approach
  //--------

  //when (
  //  RegNext(
  //    next=(
  //      cMid0Front.up.isFiring
  //      && setOutpModMemWord.io.modMemWordValid.head
  //      && alu.io.inp_op =/= LcvAluDel1InpOpEnum.OP_GET_INP_A
  //    ),
  //    init=False,
  //  )
  //) {
  //  myModMemWord := alu.io.outp_data
  //} elsewhen (
  //  RegNext(
  //    next=(
  //      cMid0Front.up.isFiring
  //      && setOutpModMemWord.io.modMemWordValid.head
  //      && alu.io.inp_op === LcvAluDel1InpOpEnum.OP_GET_INP_A
  //    ),
  //    init=False,
  //  )
  //) {
  //  myModMemWord := (
  //    RegNext(
  //      next=(
  //        setOutpModMemWord.io.modMemWord(0).asSInt
  //      ),
  //      init=setOutpModMemWord.io.modMemWord(0).asSInt.getZero
  //    )
  //  )
  //} otherwise {
  //  myModMemWord := (
  //    RegNext(
  //      next=myModMemWord,
  //      init=myModMemWord.getZero,
  //    )
  //    //RegNextWhen(
  //    //  next=alu.io.inp_a,
  //    //  cond=cMid0Front.up.isFiring,
  //    //  init=alu.io.inp_a.getZero,
  //    //)
  //  )
  //}


  //when (
  //  myDbusLdReady
  //) {
  //  myModMemWord := (
  //    myDbus.recvData.data.asSInt.resized
  //  )
  //}

  def doFinishSetOutpModMemWord(
    ydx: Int,
    zdx: Int,
  ): Unit = {
    def tempExt = outp.myExt(ydx)
    if (
      //zdx == PipeMemRmw.modWrIdx
      zdx == cfg.regFileCfg.modRdPortCnt
    ) {
      //when (cMid0Front.up.isFiring) {
        tempExt.modMemWord := (
          // TODO: support multiple output `modMemWord`s
          //setOutpModMemWord.io.modMemWord(0)
          tempExt.modMemWord.getZero
        )
        for (idx <- 0 until tempExt.modMemWordValid.size) {
          //tempExt.modMemWordValid.foreach(current =>{
          //  current := (
          //    setOutpModMemWord.io.modMemWordValid
          //  )
          //})
          tempExt.modMemWordValid(idx) := (
            setOutpModMemWord.io.modMemWordValid(idx)
          )
        }
        //outp.myModMemWord := (
        //  setOutpModMemWord.io.myModMemWord.head
        //)
        //for (idx <- 0 until outp.aluModMemWordValid.size) {
        //  outp.aluModMemWordValid(idx) := (
        //    setOutpModMemWord.io.aluModMemWordValid(idx)
        //  )
        //}
      //}
      //outp.shiftModMemWord := (
      //  setOutpModMemWord.io.shiftModMemWord
      //)
      //outp.shiftModMemWordValid := (
      //  setOutpModMemWord.io.shiftModMemWordValid
      //)
      //outp.shiftModMemWordValid.foreach(item => {
      //  item := setOutpModMemWord.io.opIsAluShift.head
      //})
    } else {
      def tempRdMemWord = setOutpModMemWord.io.rdMemWord(zdx)
      val tempMyRdMemWord = myRdMemWord(ydx=ydx, modIdx=zdx)
      //when (
      //  //outp.aluOp === LcvAluDel1InpOpEnum.OP_GET_INP_A
      //  RegNextWhen(
      //    next=(
      //      //(
      //      //  cMid0Front.down.isReady
      //      //) && 
      //      (
      //        alu.io.inp_op === LcvAluDel1InpOpEnum.OP_GET_INP_A
      //      ) && (
      //        setOutpModMemWord.io.modMemWordValid.head
      //      )
      //    ),
      //    cond=(
      //      //cMid0Front.down.isReady
      //      cMid0Front.up.isFiring
      //    ),
      //    init=False,
      //  )
      //) {
      //  //alu.io.inp_op := LcvAluDel1InpOpEnum.ZERO
      //  //alu.io.inp_op := LcvAluDel1InpOpEnum.OP_GET_INP_A
      //  //alu.io.inp_a := setOutpModMemWord.io.modMemWord(0).asSInt
      //  myModMemWord := RegNextWhen(
      //    next=setOutpModMemWord.io.modMemWord(0).asSInt,
      //    cond=(
      //      //cMid0Front.down.isReady
      //      cMid0Front.up.isFiring
      //    ),
      //    init=setOutpModMemWord.io.modMemWord(0).asSInt.getZero,
      //  )
      //}

      if (zdx == 0) {
        alu.io.inp_a := tempMyRdMemWord.asSInt
        alu.io.inp_op := outp.aluOp
        //when (
        //  cMid0Front.down.isReady
        //  //cMid0Front.up.isFiring
        //) {
        //  //alu.io.inp_a := tempMyRdMemWord.asSInt
        //  //when (
        //  //  //!myShouldIgnoreInstr(0)
        //  //  //&& 
        //  //  alu.io.inp_op === LcvAluDel1InpOpEnum.OP_GET_INP_A
        //  //) {
        //  //  alu.io.inp_a := setOutpModMemWord.io.modMemWord(0).asSInt
        //  //}
        //  when (setOutpModMemWord.io.modMemWordValid.head) {
        //    alu.io.inp_op := outp.aluOp
        //  } otherwise {
        //    alu.io.inp_op := LcvAluDel1InpOpEnum.OP_GET_INP_A
        //  }

        //  //when (
        //  //  //outp.aluOp === LcvAluDel1InpOpEnum.OP_GET_INP_A
        //  //  alu.io.inp_op === LcvAluDel1InpOpEnum.OP_GET_INP_A
        //  //) {
        //  //  //alu.io.inp_op := LcvAluDel1InpOpEnum.ZERO
        //  //  //alu.io.inp_op := LcvAluDel1InpOpEnum.OP_GET_INP_A
        //  //  alu.io.inp_a := setOutpModMemWord.io.modMemWord(0).asSInt
        //  //}

        //  //when (alu.io.inp_op === LcvAluDel1InpOpEnum.OP_GET_INP_A) {
        //  //  alu.io.inp_a := setOutpModMemWord.io.modMemWord(0).asSInt
        //  //}
        //  //when (setOutpModMemWord.io.aluModMemWordValid.head) {
        //  //  alu.io.inp_a := tempMyRdMemWord.asSInt
        //  //  alu.io.inp_op := outp.aluOp
        //  //} otherwise {
        //  //  alu.io.inp_a := 0x0
        //  //  alu.io.inp_op := LcvAluDel1InpOpEnum.ADD
        //  //}
        //} 
        //when (myShouldIgnoreInstr.last) {
        //  //alu.io.inp_op := LcvAluDel1InpOpEnum.ZERO
        //  alu.io.inp_op := 
        //}
      } else if (zdx == 1) {
        //alu.io.inp_b_sel := True
        //when (
        //  cMid0Front.down.isReady
        //  //cMid0Front.up.isFiring
        //) {
          alu.io.inp_b(0) := tempMyRdMemWord.asSInt
          alu.io.inp_b(1) := outp.imm.last.asSInt
          alu.io.inp_b_sel := outp.aluInpBIsImm
          //when (
          //  //setOutpModMemWord.io.aluModMemWordValid.head
          //  setOutpModMemWord.io.modMemWordValid.head
          //) {
          //  alu.io.inp_b(1) := outp.imm.last.asSInt
          //  alu.io.inp_b_sel := outp.aluInpBIsImm
          //  //when (!outp.aluInpBIsImm) {
          //  //  alu.io.inp_b := tempMyRdMemWord.asSInt
          //  //} otherwise {
          //  //  alu.io.inp_b := outp.imm(0).asSInt
          //  //}
          //} otherwise {
          //  //alu.io.inp_b := 0x0 
          //  alu.io.inp_b(1) := 0x0
          //  //alu.io.inp_b.foreach(_ := 0x0)
          //  //alu.io.inp_b_sel := False
          //}
        //}
      }
      //val rRdMemWordState = (
      //  Reg(Bool(), init=False)
      //  .setName(
      //    s"${cfg.shRegFileCfg.pipeName}_rRdMemWordState_${ydx}_${zdx}"
      //  )
      //)

      //when (
      //  cMid0Front.down.isReady
      //)
      //tempRdMemWord := (
      //  RegNext(
      //    next=tempRdMemWord,
      //    init=tempRdMemWord.getZero,
      //  )
      //)
      ////when (
      ////  outp.gprIsZeroVec(zdx).last
      ////) {
      ////  tempRdMemWord := 0x0
      ////} else
      //when (
      //  //tempExt.memAddr(zdx) =/= 0x0
      //  regFileFwd.myFwdMmwValidUp(ydx)(zdx)
      //) {
        tempRdMemWord := tempMyRdMemWord //myRdMemWord(ydx=ydx, modIdx=zdx)
      //}
      //when (
      //  cMid0Front.up.isValid
      //  //////&& cMid0Front.down.isValid
      //  //&& cMid0Front.down.isReady
      //  && cMid0Front.down.isReady
      //) {
      //  when (!rRdMemWordState) {
      //    tempRdMemWord := myRdMemWord(ydx=ydx, modIdx=zdx)
      //    rRdMemWordState := True
      //  }
      //}
      //when (cMid0Front.up.isFiring) {
      //  rRdMemWordState := False
      //}
      // TODO (maybe): support multiple register writes per instruction
    }
  }
  if (cfg.regFileWordCountArr.size == 0) {
    assert(
      false,
      s"cfg.regFileWordCountArr.size(${cfg.regFileWordCountArr.size}) "
      + s"must be greater than 0"
    )
  } else if (cfg.regFileWordCountArr.size == 1) {
    for (
      //(tempRdMemWord, zdx) <- setOutpModMemWord.io.rdMemWord.zipWithIndex
      zdx <- 0 until setOutpModMemWord.io.rdMemWord.size + 1
    ) {
      val mapElem = outp.gprIdxToMemAddrIdxMap(zdx)
      assert(!mapElem.haveHowToSetIdx)
      val ydx = 0
      doFinishSetOutpModMemWord(
        ydx=ydx,
        zdx=zdx
      )
    }
  } else { // if (cfg.regFileWordCountArr.size > 1)
    for (
      //(tempRdMemWord, zdx) <- setOutpModMemWord.io.rdMemWord.zipWithIndex
      zdx <- 0 until setOutpModMemWord.io.rdMemWord.size + 1
    ) {
      val mapElem = outp.gprIdxToMemAddrIdxMap(zdx)
      assert(mapElem.haveHowToSetIdx)
      switch (mapElem.howToSetIdx) {
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          is (ydx) {
            doFinishSetOutpModMemWord(
              ydx=ydx,
              zdx=zdx,
            )
          }
        }
      }
    }
  }
  //val rMemStallState = (
  //  Reg(Bool(), init=False)
  //)
  //when (
  //  //!rMemStallState
  //  //&& 
  //  cMid0Front.up.isValid
  //  && doCheckHazard.head
  //  && myDoHaveHazard.head
  //) {
  //  when (myDoHaveHazard.head) {
  //    myDoStall(stallKindMem) := True
  //  }
  //}
  myDbus.nextValid := (
    RegNext(myDbus.nextValid, init=False)
  )
  when (
    RegNext(
      next=(
        //psMemStallHost.nextValid
        myDbus.nextValid
      ),
      init=False,
    )
  ) {
    when (
      //psMemStallHost.ready
      myDbus.ready
    ) {
      //psMemStallHost.nextValid := False
      myDbus.nextValid := False
      //myDoStall(stallKindMem) := False
    } otherwise {
      myDoStall(stallKindMem) := True
    }
  }
  when (cMid0Front.up.isFiring) {
    nextPrevTxnWasHazard := False
  }
  val rSavedJmpCnt = {
    val temp = Reg(
      SnowHouseInstrCnt(cfg=cfg)
    )
    temp.init(temp.getZero)
    temp
  }
  val nextSetPcCnt = (
    Flow(UInt(
      cfg.mainWidth bits
    ))
  )
  val rSetPcCnt = {
    val temp = /*KeepAttribute*/(
      RegNext(next=nextSetPcCnt)
    )
    temp.valid.init(False)
    temp.payload.init(0x0)

    temp
  }
  nextSetPcCnt := rSetPcCnt
  psExSetPc.nextPc := (
    RegNext(
      next=psExSetPc.nextPc,
      init=psExSetPc.nextPc.getZero,
    )
  )
  //psExSetPc.valid1.allowOverride
  psExSetPc.nextPc.allowOverride
  for (idx <- 0 until cfg.lowerMyFanoutRegPcSetItCnt) {
    setOutpModMemWord.io.shouldIgnoreInstr(idx) := (
      myShouldIgnoreInstr(idx)
    )
  }

  pcChangeState.assignFromBits(
    setOutpModMemWord.io.pcChangeState.asBits
  )

  val nextPsExSetPcValid = Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
    Bool()
  )
  for (idx <- 0 until cfg.lowerMyFanoutRegPcSetItCnt) {
    nextPsExSetPcValid(idx) := (
      setOutpModMemWord.io.psExSetPc.valid
      && RegNext(
        next=(
          !myShouldIgnoreInstr(0)
          && cMid0Front.up.isFiring
        ),
        init=False
      )
    )
  }

  psExSetPc.valid := (
    RegNext(
      next=(
        nextPsExSetPcValid(0)
        //&& cMid0Front.up.isFiring
        //&& RegNext(
        //  next=(
        //    !myShouldIgnoreInstr(0)
        //    && cMid0Front.up.isFiring
        //  ),
        //  init=False
        //)
      ),
      init=False,
    )
  )
  for (idx <- 0 until cfg.lowerMyFanoutRegPcSetItCnt) {
    when (
      nextPsExSetPcValid(idx)
    ) {
      myShouldIgnoreInstr(idx) := True
    }
    when (
      cMid0Front.up.isValid
      && (
        RegNext(
          next=myShouldIgnoreInstr(idx),
          init=False,
        )
        //|| psExSetPc.valid
      )
      //True
    ) {
      when (outp.regPcSetItCnt(idx)(0)) {
        myShouldIgnoreInstr(idx) := False
      }
    }
  }

  //val rPsExSetPcValidState = (
  //  Reg(Bool(), init=False)
  //)
  //when (cMid0Front.up.isValid) {
  //  when (
  //    !rPsExSetPcValidState
  //    //&& psExSetPc.valid
  //    && rPsExSetPcValid
  //  ) {
  //    rPsExSetPcValidState := True
  //  }
  //  when (cMid0Front.up.isFiring) {
  //    when (
  //      rPsExSetPcValidState
  //      || rPsExSetPcValid
  //    ) {
  //      psExSetPc.valid := True
  //    }
  //    rPsExSetPcValidState := False
  //  }
  //}

  //otherwise {
  //  psExSetPc.valid := False
  //}

  //when (
  //  RegNext(
  //    next=cMid0Front.up.isFiring,
  //    init=False,
  //  )
  //  && psExSetPc.valid
  //) {
  //}

  //when (cMid0Front.up.isValid) {
  //  //when (cMid0Front.up.isFiring) {
  //  //  psExSetPc.valid := (
  //  //    //False
  //  //    rPsExSetPcValid
  //  //  )
  //  //}

  //  when (
  //    !rPsExSetPcValidState
  //    && rPsExSetPcValid
  //  ) {
  //    psExSetPc.valid := True
  //    rPsExSetPcValidState := True
  //  }
  //  when (cMid0Front.up.isFiring) {
  //    rPsExSetPcValidState := False
  //  }
  //}

  //psExSetPc.extValid := (
  //  //&& !outp.instrCnt.shouldIgnoreInstr
  //  !setOutpModMemWord.io.shouldIgnoreInstr(0)
  //  && (
  //    //cMid0Front.up.isValid
  //    cMid0Front.up.isFiring
  //    //cMid0Front.down.isFiring
  //    //cMid0Front.down.isReady
  //  )
  //)
  //psExSetPc.valid1 := (
  //  !outp.instrCnt.shouldIgnoreInstr
  //  && cMid0Front.up.isValid
  //)

  //setOutpModMemWord.io.psExSetPc.ready := psExSetPc.ready

  //setOutpModMemWord.io.branchTgtBufElem := outp.branchTgtBufElem
  setOutpModMemWord.io.btbElemValid := outp.branchTgtBufElem(0).valid
  setOutpModMemWord.io.btbElemDontPredict := (
    outp.branchTgtBufElem(1).dontPredict
  )
  setOutpModMemWord.io.branchPredictTkn := (
    outp.branchPredictTkn
    //outp.branchTgtBufElem(1).branchKind.asBits(0)
  )
  setOutpModMemWord.io.branchPredictReplaceBtbElem := (
    outp.branchPredictReplaceBtbElem
  )
  setOutpModMemWord.io.splitOp.kind.allowOverride
  setOutpModMemWord.io.splitOp.allowOverride
  setOutpModMemWord.io.splitOp.jmpBrAlwaysEqNeOp.allowOverride
  setOutpModMemWord.io.splitOp := (
    RegNext(
      next=setOutpModMemWord.io.splitOp,
      init=setOutpModMemWord.io.splitOp.getZero,
    )
    //init(SnowHouseSplitOpKind.CPY_CPYUI)
  )
  setOutpModMemWord.io.takeIrq := False
  when (cMid0Front.up.isValid) {
    setOutpModMemWord.io.splitOp := outp.splitOp
    when (
      (
        rMyTakeIrq
        //&& cMid0Front.up.isFiring
        //&& RegNext(
        //  next=cMid0Front.up.isFiring,
        //  init=False
        //)
      )
      //&& cMid0Front.up.isFiring
    ) {
      setOutpModMemWord.io.btbElemDontPredict := True
      setOutpModMemWord.io.splitOp.setToDefault()
      setOutpModMemWord.io.splitOp.exSetNextPcKind := (
        SnowHousePsExSetNextPcKind.Ids
      )
      setOutpModMemWord.io.splitOp.jmpBrAlwaysEqNeOp.allowOverride
      setOutpModMemWord.io.splitOp.jmpBrAlwaysEqNeOp := {
        val temp = UInt(
          log2Up(cfg.jmpBrAlwaysEqNeOpInfoMap.size) bits
          //(cfg.jmpBrAlwaysEqNeOpInfoMap.size + 1) bits
        )
        for (
          ((idx, pureJmpOpInfo), jmpBrAlwaysEqNeOp)
          <- cfg.jmpBrAlwaysEqNeOpInfoMap.view.zipWithIndex
        ) {
          if (idx == cfg.irqJmpOp) {
            temp := (
              jmpBrAlwaysEqNeOp
              //1 << jmpBrAlwaysEqNeOp
            )
          }
        }
        temp
      }
      setOutpModMemWord.io.takeIrq := True
      //setOutpModMemWord.io.splitOp.jmpBrOtherOp := (
      //  //(1 << setOutpModMemWord.io.splitOp.jmpBrOtherOp.getWidth) - 1
      //  1 << (setOutpModMemWord.io.splitOp.jmpBrOtherOp.getWidth - 1)
      //)

      // Due to how jumps/branches are handled, I'm pretty sure we can just
      // leave this value as whatever we got from `outp.splitOp` because
      // the lt, ge, etc. comparison is ignored due to there also being a
      // forced unconditional jump from the IRQ being responded to.
      // See these signals in
      // `SnowHousePipeStageExecuteSetOutpModMemWord`: 
      // * `myPsExSetPcValid`
      // * `myPsExSetPcValidToOrReduce`
      //setOutpModMemWord.io.splitOp.setJmpBrOtherOpToDefault()
    }
  } otherwise {
    //setOutpModMemWord.io.splitOp.jmpBrAlwaysEqNeOp := (
    //  (1 << setOutpModMemWord.io.splitOp.jmpBrAlwaysEqNeOp.getWidth) - 1
    //  //1 << (setOutpModMemWord.io.splitOp.jmpBrAlwaysEqNeOp.getWidth - 1)
    //)
    //setOutpModMemWord.io.splitOp.jmpBrOtherOp := (
    //  //(1 << setOutpModMemWord.io.splitOp.jmpBrOtherOp.getWidth) - 1
    //  1 << (setOutpModMemWord.io.splitOp.jmpBrOtherOp.getWidth - 1)
    //)
    setOutpModMemWord.io.splitOp.setJmpBrAlwaysEqNeOpToDefault()
    setOutpModMemWord.io.splitOp.setJmpBrOtherOpToDefault()
  }
  psExSetPc.nextPc := (
    RegNext(
      next=setOutpModMemWord.io.psExSetPc.nextPc,
      init=setOutpModMemWord.io.psExSetPc.nextPc.getZero,
    )
  )
  //psExSetPc.encInstr := outp.encInstr
  psExSetPc.branchKind := (
    RegNext(
      next=outp.btbElemBranchKind(1),
      init=outp.btbElemBranchKind(1).getZero,
    )
  )
  psExSetPc.branchTgtBufElem := (
    RegNext(
      next=outp.branchTgtBufElem(1),
      init=outp.branchTgtBufElem(1).getZero,
    )
  )
  psExSetPc.branchTgtBufElem.dontPredict.allowOverride
  psExSetPc.branchTgtBufElem.dontPredict := (
    RegNext(
      next=setOutpModMemWord.io.psExSetPc.branchTgtBufElem.dontPredict,
      init=False
    )
  )
  myDbus.allowOverride
  myDbus.sendData := (
    RegNext(
      next=myDbus.sendData,
      init=myDbus.sendData.getZero,
    )
  )
  object MultiCycleOpState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      Idle,
      Main//,
      //NoMoreStall
      = newElement()
  }
  val rMultiCycleOpState = {
    val temp = (
      Reg(
        //Bool()
        MultiCycleOpState()
      )
    )
    temp.init(
      //temp.getZero
      MultiCycleOpState.Idle
    )
    temp
  }
  val rOpIsMultiCycle = {
    val temp = (
      Reg(Vec.fill(cfg.multiCycleOpInfoMap.size)(
        Bool()
      ))
    )
    for ((elem, tempIdx) <- temp.view.zipWithIndex) {
      elem.init(elem.getZero)
    }
    temp
  }
  var busIdxFound: Boolean = false
  var busIdx: Int = 0
  for (
    ((_, opInfo), opInfoIdx)
    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    for (
      ((_, multiCycleOpInfo), myBusIdx)
      <- cfg.multiCycleOpInfoMap.view.zipWithIndex
    ) {
      if (opInfo == multiCycleOpInfo) {
        busIdxFound = true
        busIdx = myBusIdx
      }
    }
    if (busIdxFound) {
      def multiCycleBus = io.multiCycleBusVec(busIdx)
      multiCycleBus.sendData.srcVec.foreach(src => {
        src.allowOverride
      })
      multiCycleBus.sendData.srcVec(0) := (
        RegNext/*When*/(
          setOutpModMemWord.io.selRdMemWord(
            opInfo=opInfo,
            idx=0,
          ),
          //cond=(
          //  //True
          //  //!myDoStall(stallKindMultiCycle)
          //  rMultiCycleOpState === MultiCycleOpState.Idle
          //),
        )
        init(0x0)
      )
      if (multiCycleBus.sendData.srcVec.size > 1) {
        multiCycleBus.sendData.srcVec(1) := (
          RegNext/*When*/(
            setOutpModMemWord.io.selRdMemWord(
              opInfo=opInfo,
              idx=1,
            ),
            //cond=(
            //  //True
            //  //!myDoStall(stallKindMultiCycle)
            //  rMultiCycleOpState === MultiCycleOpState.Idle
            //),
          )
          init(0x0)
        )
      }
    }
  }
  //io.dcacheHaveHazard := (
  //  !rSavedStall
  //  && doCheckHazard && myDoHaveHazard1
  //)
  when (/*LcvFastOrR*/(
    setOutpModMemWord.io.opIsMemAccess.head
    //|| setOutpModMemWord.io.opIsAluShift.head
    //.asBits.asUInt
    //.orR
  )) {
    nextPrevTxnWasHazard := True
    when (cMid0Front.up.isFiring) {
      //psMemStallHost.nextValid := True
      myDbus.nextValid := True
      //myDbus.sendData := setOutpModMemWord.io.dbusHostPayload
    }
  }
  myDbus.sendData.addr.allowOverride
  when (
    cMid0Front.up.isFiring
    //&&
    //outp.splitOp.opIsMemAccess
    //cMid0Front.down.isFiring
    //cMid0Front.down.isReady
  ) {
    myDbus.sendData := setOutpModMemWord.io.dbusHostPayload
  }
  //when (
  //  //cMid0Front.up.isFiring
  //  cMid0Front.up.isValid
  //) {
  //  myDbus.sendData.addr := setOutpModMemWord.io.dbusHostPayload.addr
  //}

  //when (cMid0Front.up.isFiring)
  //when (
  //  cMid0Front.up.isValid && !setOutpModMemWord.io.shouldIgnoreInstr(1)
  //) {
  //  myDbus.sendData := setOutpModMemWord.io.dbusHostPayload
  //}
  def doMultiCycleStart(
    myPsExStallHost: LcvStallHost[
      MultiCycleHostPayload,
      MultiCycleDevPayload
    ],
    idx: Int,
  ): Unit = {
    //myDoStall(stallKindMem) := False
    //myDoStall(stallKindMultiCycle) := True
    myPsExStallHost.nextValid := True
  }
  val rHaveDoneMultiCycleOp = (
    Reg(
      Bool(),
      init=False,
    )
  )
  switch (rMultiCycleOpState) {
    is (
      //False
      MultiCycleOpState.Idle
    ) {
      setOutpModMemWord.io.inMultiCycleOp := False
      when (
        //LcvFastOrR(
        //  setOutpModMemWord.io.opIsMultiCycle.asBits.asUInt
        //  //=/= 0x0
        //  //.orR
        //)
        (
          !rHaveDoneMultiCycleOp
          && cMid0Front.up.isValid
          && setOutpModMemWord.io.opIsAnyMultiCycle
          //&& !setOutpModMemWord.rShouldIgnoreInstrState(2)
          //&& !setOutpModMemWord.io.shouldIgnoreInstr(2)
          && !myShouldIgnoreInstr(2)
          //&& !shouldIgnoreInstr
        )
      ) {
        //rMultiCycleOpState := (
        //  //True
        //  MultiCycleOpState.Main
        //)
        for (idx <- 0 until rOpIsMultiCycle.size) {
          rOpIsMultiCycle(idx) := (
            setOutpModMemWord.io.opIsMultiCycle(idx)
          )
        }
        cMid0Front.haltIt()
        //outp.myExt.foreach(item => {
        //  item.modMemWordValid.foreach(mmwValidItem => {
        //    mmwValidItem := False
        //  })
        //})
        val toOrReduce = (
          /*RegNext*/(
            Vec[Bool](
              (
                Vec[Bool](
                  //!rSavedStall.head/*(idx)*/,
                  /*RegNext*/(doCheckHazard).head/*(idx)*/,
                  /*RegNext*/(myDoHaveHazard).head/*(idx)*/,
                  RegNext(
                    //psMemStallHost.nextValid
                    myDbus.nextValid, init=False
                  ),
                  //psMemStallHost.ready,
                  myDbus.ready,
                ).asBits.asUInt.andR
              ),
              (
                !Vec[Bool](
                  //!rSavedStall.head/*(idx)*/,
                  /*RegNext*/(doCheckHazard).head/*(idx)*/,
                  /*RegNext*/(myDoHaveHazard).head/*(idx)*/,
                ).asBits.asUInt.andR
              )
            ).asBits.asUInt//.orR
          )
          //init(False)
        )
        when (
          //if (cfg.targetAltera) (
          //  LcvFastOrR(toOrReduce)
          //) else (
            toOrReduce.orR
          //)
        ) {
          //doMultiCycleStart(psExStallHost, idx=idx)
          rMultiCycleOpState := (
            //True
            MultiCycleOpState.Main
          )
          myDoStall(stallKindMem) := False
        }
      }
      //myDoStall(stallKindMultiCycle) := False
      when (cMid0Front.up.isFiring) {
        rHaveDoneMultiCycleOp := False
      }
    }
    is (
      //True
      MultiCycleOpState.Main
    ) {
      setOutpModMemWord.io.inMultiCycleOp := True
      myDoStall(stallKindMem) := False
      //myDoStall(stallKindMultiCycle) := True
      //cMid0Front.haltIt()
      rHaveDoneMultiCycleOp := True
      //myDoStall(stallKindMultiCycle) := True
      //switch (rOpIsMultiCycle.asBits.asUInt) {
        for (idx <- 0 until cfg.multiCycleOpInfoMap.size) {
          //--------
          // BEGIN: working, slower than desired multi-cycle op handling code
          when /*is*/ (
            //setOutpModMemWord.io.opIsMultiCycle(idx)
            rOpIsMultiCycle(idx)
            //new MaskedLiteral(
            //  value=(
            //    (1 << idx)
            //  ),
            //  careAbout=(
            //    (1 << idx)
            //    | ((1 << idx) - 1)
            //  ),
            //  width=(
            //    cfg.multiCycleOpInfoMap.size
            //  )
            //)
          ) {
            for (
              ((_, opInfo), opInfoIdx)
              <- cfg.multiCycleOpInfoMap.view.zipWithIndex
            ) {
              //is (opInfoIdx)
              if (opInfoIdx == idx) {
                var busIdxFound: Boolean = false
                var busIdx: Int = 0
                for (
                  ((_, multiCycleOpInfo), myBusIdx)
                  <- cfg.multiCycleOpInfoMap.view.zipWithIndex
                ) {
                  if (opInfo == multiCycleOpInfo) {
                    busIdxFound = true
                    busIdx = myBusIdx
                  }
                }
                if (busIdxFound) {
                  val psExStallHost = psExStallHostArr(busIdx)
                  doMultiCycleStart(psExStallHost, idx=idx)
                }
              }
            }
          }
          for (
            ((_, opInfo), opInfoIdx)
            <- cfg.multiCycleOpInfoMap.view.zipWithIndex
          ) {
            //is (opInfoIdx)
            if (opInfoIdx == idx) {
              var busIdxFound: Boolean = false
              var busIdx: Int = 0
              for (
                ((_, multiCycleOpInfo), myBusIdx)
                <- cfg.multiCycleOpInfoMap.view.zipWithIndex
              ) {
                if (opInfo == multiCycleOpInfo) {
                  busIdxFound = true
                  busIdx = myBusIdx
                }
              }
              if (busIdxFound) {
                val psExStallHost = psExStallHostArr(busIdx)
                //doMultiCycleStart(psExStallHost, idx=idx)
                when (
                  RegNext(psExStallHost.nextValid, init=False)
                  && psExStallHost.ready
                ) {
                  psExStallHost.nextValid := False
                  rMultiCycleOpState := MultiCycleOpState.Idle
                } elsewhen (rOpIsMultiCycle(idx)) {
                  cMid0Front.haltIt()
                  //outp.myExt.foreach(item => {
                  //  item.modMemWordValid.foreach(mmwValidItem => {
                  //    mmwValidItem := False
                  //  })
                  //})
                }
              }
            }
          }
          // END: working, slower than desired multi-cycle op handling code
          //--------
        }
        when (cMid0Front.up.isFiring) {
          rHaveDoneMultiCycleOp := False
        }
      //}
    }
  }
  //--------
  psExStallHostArr.foreach(psExStallHost => {
    when (
      //psExStallHost.fire
      RegNext(psExStallHost.nextValid, init=False)
      && psExStallHost.ready
    ) {
      psExStallHost.nextValid := False
    }
  })
  for (idx <- 0 until doCheckHazard.size) {
    doCheckHazard(idx) := (
      RegNextWhen(
        next=myNextPrevTxnWasHazardVec(idx),
        cond=cMid0Front.up.isFiring,
        init=myNextPrevTxnWasHazardVec(idx).getZero,
      )
    )
  }
  when (
    myDoStall.sFindFirst(_ === True)._1
  ) {
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      //outp.myExt(ydx).valid.foreach(current => {
      //  current := False
      //})
      //outp.myExt(ydx).memAddrFwd.foreach(current => {
      //  current := 
      //})
      //outp.myExt(ydx).memAddrFwdCmp.foreach(_.foreach(_ := 0x0))
      outp.myExt(ydx).modMemWordValid.foreach(_ := False)
    }
    cMid0Front.haltIt()
  }
  if (cfg.optFormal) {
    outp.psExSetOutpModMemWordIo := setOutpModMemWord.io
  }
  //when (!(outp.imm(2) - (3 * (cfg.instrMainWidth / 8))).msb) {
  //  outp.regPcPlusImm := (
  //    outp.regPc + outp.imm(2) - (2 * (cfg.instrMainWidth / 8))
  //  )
  //} otherwise {
  //  outp.regPcPlusImm := (
  //    outp.regPc + outp.imm(2) //- (3 * (cfg.instrMainWidth / 8))
  //  )
  //}
  for (idx <- 0 until cfg.regFileCfg.memArrSize) {
    outp.myExt.foreach(item => {
      item.fwdCanDoIt.foreach(item => {
        item := (
          //!setOutpModMemWord.io.shouldIgnoreInstr.last
          !myShouldIgnoreInstr.last
          //&& (
          //  if (idx < outp.myExt(0).modMemWordValid.size) (
          //    outp.myExt(0).modMemWordValid(idx)
          //  ) else (
          //    outp.myExt(0).modMemWordValid.last
          //  )
          //)
          //!shouldIgnoreInstr
          //&& !outp.shiftModMemWordValid.last
          //&& setOutpModMemWord.io.modMemWordValid(0)
        )
      })
    })
  }
}
case class SnowHousePipeStageMem(
  args: SnowHousePipeStageArgs,
  //psWb: SnowHousePipeStageWriteBack,
  //psMemStallHost: LcvStallHost[
  //  BusHostPayload,
  //  BusDevPayload,
  //],
  //myDbusExtraReady: Vec[Bool],
  //myDbusLdReady: Bool,
  //myDbusIo: SnowHouseDbusIo,
  myModMemWord: SInt,
) extends Area {
  def myDbusIo = args.myDbusIo
  def myDbus = myDbusIo.dbus
  def myDbusExtraReady = myDbusIo.dbusExtraReady
  def myDbusLdReady = myDbusIo.dbusLdReady
  def cfg = args.cfg
  def io = args.io
  def regFile = args.regFile
  def front = regFile.io.front
  def frontPayload = regFile.io.frontPayload
  def modFront = regFile.io.modFront
  //def modFrontAfterPayload = regFile.io.modFrontAfterPayload
  def prevPayload = args.prevPayload
  def modBack = regFile.io.modBack
  def pMem = args.currPayload //regFile.io.modBackPayload
  def back = regFile.io.back
  def backPayload = regFile.io.backPayload
  def tempModFrontPayload = (
    regFile.io.tempModFrontPayload
  )
  //val modFrontFormalAssumes = modFront(modFrontPayload).formalAssumes()
  val modBackFormalAssumes = (
    !cfg.useLcvDataBus
  ) generate (
    modBack(pMem).formalAssumes()
  )
  def extIdxUp = PipeMemRmw.extIdxUp
  def extIdxSaved = PipeMemRmw.extIdxSaved
  def extIdxLim = PipeMemRmw.extIdxLim
  def doPsMemFork = (
    //true
    !cfg.useLcvDataBus
  )
  val midModPayload = (
    Vec.fill(extIdxLim)(
      SnowHousePipePayload(cfg=cfg)
    )
  )
  //val myDbus = (
  //  psMemStallHost.stallIo.get
  //)
  //val myShouldIgnoreInstr = (
  //  modFront(modFrontPayload).instrCnt.shouldIgnoreInstr
  //)
  val midModFormalAssumesArr = (
    !cfg.useLcvDataBus
  ) generate (
    ArrayBuffer[Area]()
  )
  if (!cfg.useLcvDataBus) {
    for ((midModElem, midModIdx) <- midModPayload.view.zipWithIndex) {
      midModFormalAssumesArr += midModElem.formalAssumes()
    }
  }
  //val cMem = (doMidMod) generate (
  //  CtrlLink(
  //    up=modFront,
  //    down={
  //      val temp = Node()
  //      temp.setName(s"cMem_down")
  //      temp
  //    },
  //  )
  //)
  def cMem = args.link
  val fMem = (
    doPsMemFork
  ) generate (
    ForkLink(
      up=cMem.down,
      downs={
        Array.fill(2)(Node())
      },
      synchronous=(
        false
        //true
      )
    )
  )
  val sMemFwd = (
    doPsMemFork
  ) generate (
    StageLink(
      up=fMem.downs(0),
      down={
        regFile.io.modBackFwd
      }
    )
  )
  val sMem = (
    StageLink(
      up=(
        if (!doPsMemFork) (
          cMem.down
        ) else (
          fMem.downs(1)
        )
      ),
      down={
        if (!doPsMemFork) {
          val temp = Node()
          temp.setName(s"sMem_down")
          temp
        } else {
          modBack
        }
      },
    )
  )
  //regFile.myLinkArr += cMem
  if (doPsMemFork) {
    regFile.myLinkArr += fMem
    regFile.myLinkArr += sMemFwd
    //regFile.myLinkArr += sMem
  } else {
    //regFile.myLinkArr += sMem
  }
  regFile.myLinkArr += sMem
  object MmwState extends SpinalEnum(
    defaultEncoding=binaryOneHot
  ) {
    val
      //WAIT_FIRST_UP_VALID,
      WAIT_DATA,
      WAIT_UP_FIRE
      = newElement();
  }
  val rMmwState = {
    val temp = Reg(
      Vec.fill(cfg.regFileCfg.memArrSize)(
        Vec.fill(2 /*1*/)(
          MmwState()
        )
      )
    )
    temp.foreach(item => {
      item.foreach(innerItem => innerItem.init(MmwState.WAIT_DATA))
    })
    temp
  }

  midModPayload(extIdxSaved) := (
    RegNextWhen(
      next=midModPayload(extIdxUp),
      cond=cMem.up.isFiring,
      init=midModPayload(extIdxSaved).getZero,
    )
  )
  for (extIdx <- 0 until extIdxLim) {
    if (extIdx != extIdxSaved) {
      midModPayload(extIdx) := (
        RegNext(
          next=midModPayload(extIdx),
          init=midModPayload(extIdx).getZero,
        )
      )
      //midModPayload(extIdx).nonExt := (
      //  RegNext(
      //    next=midModPayload(extIdx).nonExt,
      //    init=midModPayload(extIdx).nonExt.getZero,
      //  )
      //)
      //midModPayload(extIdx).myExt.foreach(item => {
      //  item.main.memAddr := (
      //    RegNext(
      //      next=item.main.memAddr,
      //      init=item.main.memAddr.getZero,
      //    )
      //  )
      //  item.main.nonMemAddrMost := (
      //    RegNext(
      //      next=item.main.nonMemAddrMost,
      //      init=item.main.nonMemAddrMost.getZero,
      //    )
      //  )
      //})
    }
  }
  for (fjIdx <- 0 until tempModFrontPayload.size) {
    tempModFrontPayload(fjIdx) := midModPayload(extIdxUp)
    for (idx <- 0 until tempModFrontPayload(fjIdx).gprIdxVec.size) {
      tempModFrontPayload(fjIdx).gprIdxVec(idx).allowOverride
      tempModFrontPayload(fjIdx).gprIdxVec(idx) := (
        //cMem.up(modFrontPayload(fjIdx)).gprIdxVec(idx)
        cMem.up(prevPayload).gprIdxVec(idx)
      )
    }
  }

  for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
    //val tempMyExt = midModPayload(extIdxUp).myExt
    def tempPayloadRight = cMem.up(prevPayload)
    def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
    def tempExtRight(ydx: Int) = tempPayloadRight.myExt(ydx)
    val myExtLeft = tempExtLeft(ydx=ydx)
    val myExtRight = tempExtRight(ydx=ydx)
    myExtLeft.allowOverride

    when (
      //cMem.up.isValid
      //&& 
      rMmwState(ydx)(0) === MmwState.WAIT_DATA
      //&& (
      //  RegNext(
      //    next=(rMmwState(ydx) == MmwState.WAIT_UP_FIRE),
      //    init=False
      //  )
      //)
      //&& myExtRight.modMemWordValid.last
    ) {
      midModPayload(extIdxUp).nonExt := (
        cMem.up(prevPayload).nonExt
      )
      myExtLeft.main.memAddr := myExtRight.main.memAddr
      myExtLeft.main.nonMemAddrMost := myExtRight.main.nonMemAddrMost
    }
    myExtLeft.modMemWord := myModMemWord.asUInt

    when (cMem.up.isValid) {
      rMmwState(ydx)(0) := MmwState.WAIT_UP_FIRE
    }
    when (cMem.up.isFiring) {
      rMmwState(ydx).foreach(item => item := MmwState.WAIT_DATA)
    }
    myExtLeft.valid.foreach(current => {
      current := (
        cMem.up.isValid
      )
    })
    myExtLeft.ready := cMem.up.isReady
    myExtLeft.fire := cMem.up.isFiring
  }

  val myNonLcvDbusArea = (
    !cfg.useLcvDataBus
  ) generate (new Area {

    def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
    def tempExtRight(ydx: Int) = cMem.up(prevPayload).myExt(ydx)
    val rDbusState = (
      Reg(Bool(), init=False)
    )
    when (
      RegNext(myDbus.nextValid) init(False)
      //midModPayload(extIdxUp).decodeExt.opIsMemAccess.sFindFirst(
      //  _ === True
      //)._1
    ) {
      def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
      def tempExtRight(ydx: Int) = cMem.up(prevPayload).myExt(ydx)
      when (
        //!myDbus.ready
        !myDbusExtraReady(3)
      ) {
        //cMem.duplicateIt()
        cMem.haltIt()
        val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
        val myCurrExt = (
          if (!mapElem.haveHowToSetIdx) (
            midModPayload(extIdxUp).myExt(0)
          ) else (
            midModPayload(extIdxUp).myExt(mapElem.howToSetIdx)
          )
        )
        myCurrExt.modMemWordValid.foreach(mmwValidItem => {
          mmwValidItem := False
        })
      }
    }
    when (myDbusExtraReady(2)) {
      val myDecodeExt = midModPayload(extIdxUp).outpDecodeExt
      val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          midModPayload(extIdxUp).myExt(
            0
          )
        ) else (
          midModPayload(extIdxUp).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      myCurrExt.modMemWordValid.foreach(current => {
        current := (
          // TODO: support more destination GPRs
          //!midModPayload(extIdxUp).gprIsZeroVec(0)
          True
        )
      })
    }
  })
  val myLcvDbusArea = (
    cfg.useLcvDataBus
  ) generate (new Area {
    when (
      ////RegNext(
      //  RegNext(myDbus.nextValid, init=False),
      ////  init=False
      ////)
      ////myWbPayload.decodeExt.opIsMemAccess.sFindFirst(
      ////  _ === True
      ////)._1
      //cMem.up.isValid
      //&& 
      midModPayload(extIdxUp).outpDecodeExt.opIsMemAccess.last
    ) {
      val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          midModPayload(extIdxUp).myExt(0)
        ) else (
          midModPayload(extIdxUp).myExt(mapElem.howToSetIdx)
        )
      )
      myCurrExt.modMemWordValid.foreach(mmwValidItem => {
        mmwValidItem := False
      })
    }
  })

  def setMidModStages(): Unit = {
    regFile.io.midModStages(0) := midModPayload
  }
  setMidModStages()

  //modFront(pMem) := midModPayload(extIdxUp)
  cMem.up(pMem) := midModPayload(extIdxUp)
  //when (modFront.isValid) {
  //} otherwise {
  //}
}
case class SnowHousePipeStageWriteBack(
  args: SnowHousePipeStageArgs,
  //psMemStallHost: LcvStallHost[
  //  BusHostPayload,
  //  BusDevPayload,
  //],
  //myDbusExtraReady: Vec[Bool],
  //myDbusLdReady: Bool,
  //myDbusIo: SnowHouseDbusIo,
  //myModMemWord: SInt,
) extends Area {
  def myDbusIo = args.myDbusIo
  def myDbus = myDbusIo.dbus
  def myDbusExtraReady = myDbusIo.dbusExtraReady
  def myDbusLdReady = myDbusIo.dbusLdReady
  def cfg = args.cfg
  def io = args.io
  def regFile = args.regFile
  def front = regFile.io.front
  def frontPayload = regFile.io.frontPayload
  def modFront = regFile.io.modFront
  //def modFrontAfterPayload = regFile.io.modFrontAfterPayload
  def pMem = args.prevPayload
  def modBack = regFile.io.modBack
  def modBackPayload = args.currPayload
  def back = regFile.io.back
  def backPayload = regFile.io.backPayload
  def cWb = args.link

  val fWb = (
    ForkLink(
      up=cWb.down,
      downs={
        //Array.fill(2)(Node())
        List[Node](
          regFile.io.modBackFwd,
          modBack,
        )
      },
      synchronous=(
        false
        //true
      )
    )
  )
  //val sWbFwd = (
  //  StageLink(
  //    up=fWb.downs(0),
  //    down={
  //      regFile.io.modBackFwd
  //    }
  //  )
  //)
  //val sWb = (
  //  StageLink(
  //    up=fWb.downs(1),
  //    down=modBack,
  //  )
  //)
  regFile.myLinkArr += fWb
  //regFile.myLinkArr += sWbFwd
  //regFile.myLinkArr += sWb
  val myWbPayload = (
    //Vec.fill(2)(
      SnowHousePipePayload(cfg=cfg)
    //)
  )
  myWbPayload := (
    RegNext(myWbPayload, init=myWbPayload.getZero)
  )
  //when (cWb.up.isValid) {
  //  myWbPayload.head := cWb.up(pMem)
  //}
  ////myWbPayload.last := (
  ////  RegNext(myWbPayload.last, init=myWbPayload.last.getZero)
  ////)
  //when (cWb.up.isFiring) {
  //  myWbPayload.last := myWbPayload.head
  //}
  ////when (cWb.up.isFiring) {
  //  cWb.up(modBackPayload) := myWbPayload.last
  ////}
  object MmwState extends SpinalEnum(
    defaultEncoding=binaryOneHot
  ) {
    val
      //WAIT_FIRST_UP_VALID,
      WAIT_DATA,
      WAIT_UP_FIRE
      = newElement();
  }
  val rMmwState = {
    val temp = Reg(
      Vec.fill(cfg.regFileCfg.memArrSize)(
        Vec.fill(2 /*1*/)(
          MmwState()
        )
      )
    )
    temp.foreach(item => {
      item.foreach(innerItem => innerItem.init(MmwState.WAIT_DATA))
    })
    temp
  }
  for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
    //val tempMyExt = myWbPayload.myExt
    def tempPayloadRight = cWb.up(pMem)
    def tempExtLeft(ydx: Int) = myWbPayload.myExt(ydx)
    def tempExtRight(ydx: Int) = tempPayloadRight.myExt(ydx)
    val myExtLeft = tempExtLeft(ydx=ydx)
    val myExtRight = tempExtRight(ydx=ydx)
    myExtLeft.allowOverride

    when (
      //cWb.up.isValid
      //&& 
      rMmwState(ydx)(0) === MmwState.WAIT_DATA
      //&& (
      //  RegNext(
      //    next=(rMmwState(ydx) == MmwState.WAIT_UP_FIRE),
      //    init=False
      //  )
      //)
      //&& myExtRight.modMemWordValid.last
    ) {
      myWbPayload.nonExt := (
        cWb.up(pMem).nonExt
      )
      myExtLeft.main.memAddr := myExtRight.main.memAddr
      myExtLeft.main.nonMemAddrMost := myExtRight.main.nonMemAddrMost
      myExtLeft.main.modMemWord := myExtRight.main.modMemWord
    }
    //myExtLeft.modMemWord := myModMemWord.asUInt

    when (cWb.up.isValid) {
      rMmwState(ydx)(0) := MmwState.WAIT_UP_FIRE
    }
    when (cWb.up.isFiring) {
      rMmwState(ydx).foreach(item => item := MmwState.WAIT_DATA)
    }
    myExtLeft.valid.foreach(current => {
      current := (
        cWb.up.isValid
      )
    })
    myExtLeft.ready := cWb.up.isReady
    myExtLeft.fire := cWb.up.isFiring
  }


  //def tempExtLeft(ydx: Int) = myWbPayload.myExt(ydx)
  //def tempExtRight(ydx: Int) = cWb.up(pMem).myExt(ydx)
  //val rDbusState = Reg(Bool(), init=False)

  myDbusIo.myUpFireIshCond := cWb.up.isFiring

  when (
    ////RegNext(
    //  RegNext(myDbus.nextValid, init=False),
    ////  init=False
    ////)
    ////myWbPayload.decodeExt.opIsMemAccess.sFindFirst(
    ////  _ === True
    ////)._1
    cWb.up.isValid
    && myWbPayload.outpDecodeExt.opIsMemAccess.last
  ) {
    //def tempExtLeft(ydx: Int) = myWbPayload.myExt(ydx)
    //def tempExtRight(ydx: Int) = cWb.up(pMem).myExt(ydx)
    when (
      !myDbus.ready
      //!myDbusExtraReady(3)
    ) {
      cWb.haltIt()
      val mapElem = myWbPayload.gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myWbPayload.myExt(0)
        ) else (
          myWbPayload.myExt(mapElem.howToSetIdx)
        )
      )
      myCurrExt.modMemWordValid.foreach(mmwValidItem => {
        mmwValidItem := False
      })
    } otherwise {
      val myDecodeExt = myWbPayload.outpDecodeExt
      val mapElem = myWbPayload.gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myWbPayload.myExt(
            0
          )
        ) else (
          myWbPayload.myExt(
            mapElem.howToSetIdx
          )
        )
      )
      when (!myWbPayload.outpDecodeExt.memAccessKind.asBits(1)) {
        myCurrExt.modMemWord := myDbus.recvData.word
      }
      myCurrExt.modMemWordValid.foreach(current => {
        current := (
          // TODO: support more destination GPRs
          //!myWbPayload.gprIsZeroVec(0)
          True
        )
      })
    }
  }
  //when (
  //  //myDbusExtraReady(2)
  //  myDbus.ready
  //) {
  //  val myDecodeExt = myWbPayload.outpDecodeExt
  //  val mapElem = myWbPayload.gprIdxToMemAddrIdxMap(0)
  //  val myCurrExt = (
  //    if (!mapElem.haveHowToSetIdx) (
  //      myWbPayload.myExt(
  //        0
  //      )
  //    ) else (
  //      myWbPayload.myExt(
  //        mapElem.howToSetIdx
  //      )
  //    )
  //  )
  //  myCurrExt.modMemWordValid.foreach(current => {
  //    current := (
  //      // TODO: support more destination GPRs
  //      //!myWbPayload.gprIsZeroVec(0)
  //      True
  //    )
  //  })
  //}
  cWb.up(modBackPayload) := myWbPayload
}
