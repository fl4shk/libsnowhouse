package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
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
  var regFile: PipeRegFile[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeRegFileDualRdTypeDisabled[UInt, Bool],
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

  //val includesLdBubble = (
  //  cfg.useLcvDataBus
  //) generate (
  //  Bool()
  //)
  val dontPredict = (
    Bool()
  )
  val srcRegPc = UInt(
    //cfg.mySrcRegPcCmpEqWidth bits
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  )
  val dstRegPc = UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  )
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
case class SnowHousePsExSetPcTakenPayload(
  cfg: SnowHouseConfig
) extends Bundle {
  val myPsExSetPcValid = Bool()
  val srcRegPc = UInt(cfg.mainAddrWidth bits)
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

  val nextPc = UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  )
  //val dstPc = UInt(cfg.mainWidth bits)
  //val badPredictNextPc = UInt(cfg.mainWidth bits)
  //val encInstr = Flow(UInt(cfg.instrMainWidth bits))
  //val branchTgtBufElem = BranchTgtBufElem(cfg=cfg)
  val btbElemWithBrKind = BranchTgtBufElemWithBrKind(cfg=cfg)
  def branchTgtBufElem = btbElemWithBrKind.btbElem
  def branchKind = btbElemWithBrKind.branchKind
  //val brKindValid = Bool()
  //val taken = Flow(Bool())
  val taken = Flow(SnowHousePsExSetPcTakenPayload(cfg=cfg))

  val reorderBufIdx = (
    cfg.optScoreboard
  ) generate (
    UInt(cfg.optScoreboardReorderBufWidth bits)
  )
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
case class SnowHouseBranchTgtBufResult(
  cfg: SnowHouseConfig,
) extends Bundle {
  // `valid`/`fire` indicates that we have a branch here at all
  val valid = Bool()
  def fire = valid

  val nextRegPc = UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  )

  // whether or not we're predicting the branch is taken
  //val predictTkn = Bool()
  val rdBtbElem = BranchTgtBufElem(cfg=cfg)
}
case class SnowHouseBranchTgtBufIo(
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
      UInt(
        //cfg.mainWidth bits
        cfg.mainAddrWidth bits
      )
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
    SnowHouseBranchTgtBufResult(cfg=cfg)
  )
}

case class SnowHouseBranchTgtBufSingle(
  //psIf: SnowHousePipeStageInstrFetch,
  cfg: SnowHouseConfig
) extends Component {
  val io = SnowHouseBranchTgtBufIo(
    cfg=cfg
  )
  val branchTgtBufSize = (
    cfg.optBranchPredictorKind.get._branchTgtBufSize
  )
  require(
    branchTgtBufSize > 0
  )
  val tgtBufRdAddr = (
    Vec.fill(
      SnowHouseBranchPredictorKind._branchTgtBufRdAddrSize
    )(
      UInt(log2Up(branchTgtBufSize) bits)
    )
  )
  def myDstRegPcWidth = (
    cfg.mainAddrWidth - log2Up(cfg.instrSizeBytes)
  )
  def myTgtBufAddrRange: Range = (
    tgtBufRdAddr(0).high + log2Up(cfg.instrSizeBytes) + 1
    downto log2Up(cfg.instrSizeBytes) + 1
  )
  println(
    s"myDstRegPcWidth:${myDstRegPcWidth} "
    + s"mySrcRegPcWidth:${cfg.mySrcRegPcWidth} "
    + s"mySrcRegPcRange:${cfg.mySrcRegPcRange} "
    + s"myTgtBufAddrRange:${myTgtBufAddrRange}"
  )

  //--------
  val tgtSrcRegPcBufCfg = RamSimpleDualPortConfig(
    wordType=UInt(cfg.mySrcRegPcCmpEqWidth bits),
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
    doAsyncRead=true,
  )

  val tgtSrcRegPcBuf = (
    RamSimpleDualPort(cfg=tgtSrcRegPcBufCfg)
  )
  //--------
  val tgtValidBuf = {
    val temp = Vec.fill(branchTgtBufSize)(
      Reg(Bool(), init=False)
    )
    temp
  }
  //--------
  val tgtDstRegPcBufCfg = RamSimpleDualPortConfig(
    wordType=UInt(
      (
        if (!cfg.useLcvDataBus) (
          myDstRegPcWidth
        ) else (
          myDstRegPcWidth //+ 1
        )
      )
      bits
    ),
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
    doAsyncRead=true,
  )
  val tgtDstRegPcBuf = RamSimpleDualPort(cfg=tgtDstRegPcBufCfg)
  for (idx <- 0 until tgtBufRdAddr.size) {
    tgtBufRdAddr(idx) := (
      io.inpRegPc(idx)(myTgtBufAddrRange) //- 1//- 2 //- 1 //- 2//- 3
    )
  }
  val myRdBtbElem = BranchTgtBufElem(cfg=cfg)
  val myRdSrcRegPcAndValid = Flow(UInt(
    cfg.mySrcRegPcCmpEqWidth bits
  ))
  //--------
  val tgtBrKindBuf = (
    Vec.fill(branchTgtBufSize)(
      Reg(UInt(cfg.optBranchPredictorKind.get._branchKindEnumWidth bits))
      init(0x1) // init to "weakly not taken"
    )
  )

  val myRdBrKind = (
    UInt(cfg.optBranchPredictorKind.get._branchKindEnumWidth bits)
  )
  myRdBrKind := Mux(
    io.upIsReady,
    tgtBrKindBuf(
      tgtBufRdAddr(SnowHouseBranchPredictorKind._branchTgtBufRdAddrIdx0)
    ),
    RegNext(
      myRdBrKind,
      init=myRdBrKind.getZero
    )
  )
  //--------
  myRdSrcRegPcAndValid.valid := Mux(
    io.upIsReady,
    tgtValidBuf(
      tgtBufRdAddr(SnowHouseBranchPredictorKind._branchTgtBufRdAddrIdx0)
    ),
    RegNext(
      myRdSrcRegPcAndValid.valid,
      init=False
    )
  )
  myRdSrcRegPcAndValid.payload := (
    tgtSrcRegPcBuf.io.ramIo.rdData
  )
  //--------

  myRdBtbElem.srcRegPc := (
    Cat(
      myRdSrcRegPcAndValid.payload(
        myRdSrcRegPcAndValid.payload.high downto 1
      ),
      tgtBufRdAddr(0),
      myRdSrcRegPcAndValid.payload(0),
      U(s"${log2Up(cfg.instrSizeBytes)}'d0"),
    ).asUInt
  )

  myRdBtbElem.valid := myRdSrcRegPcAndValid.valid

  myRdBtbElem.dstRegPc.assignFromBits(
    Cat(
      tgtDstRegPcBuf.io.ramIo.rdData,
      //myRdDstRegPcAndValid.payload,
      U(s"${log2Up(cfg.instrSizeBytes)}'d0"),
    )
  )

  myRdBtbElem.dontPredict := False
  //--------
  tgtSrcRegPcBuf.io.ramIo.rdAddr := (
    tgtBufRdAddr(
      SnowHouseBranchPredictorKind._branchTgtBufRdAddrIdx0
    )
  )
  //--------

  tgtDstRegPcBuf.io.ramIo.rdAddr := (
    tgtBufRdAddr(
      SnowHouseBranchPredictorKind._branchTgtBufRdAddrIdx1
    )
  )
  //--------
  tgtSrcRegPcBuf.io.ramIo.rdEn := io.upIsReady
  //--------
  tgtDstRegPcBuf.io.ramIo.rdEn := io.upIsReady
  io.result.rdBtbElem := myRdBtbElem
  val wrBtbElem = BranchTgtBufElem(cfg=cfg)
  val otherWrBtbElemWithBrKind = BranchTgtBufElemWithBrKind(cfg=cfg)
  //val otherWrBranchKind = (
  //  SnowHouseBranchPredictorKind.SatCnt2BitEnum()
  //)
  //otherWrBranchKind.assignFromBits(
  //  otherWrBtbElemWithBrKind.branchKind
  //)

  val rTgtBufWrAddr = (
    Vec[UInt](
      (
        RegNext(
          io.psExSetPc.branchTgtBufElem.srcRegPc(myTgtBufAddrRange)
        )
        init(0x0)
      ),
      (
        RegNext(
          io.psExSetPc.taken.srcRegPc(myTgtBufAddrRange),
        )
        init(0x0)
      )
    )
  )
  //nextTgtBufWrAddr := rTgtBufWrAddr

  val rTgtBufWrEn = Vec.fill(2)(
    Reg(Bool(), init=False)
  )

  rTgtBufWrEn.head := (
    (
      io.psExSetPc.valid
      && io.psExSetPc.branchTgtBufElem.fire
      && (
        !otherWrBtbElemWithBrKind.btbElem.dontPredict
      )
      //&& (
      //  otherWrBranchKind
      //  === SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum.BAK
      //)
    )
  )
  rTgtBufWrEn.last := (
    io.psExSetPc.taken.fire
    && (
      !otherWrBtbElemWithBrKind.btbElem.dontPredict
    )
    //io.psExSetPc.brKindValid
    //&& (
    //  //otherWrBranchKind.asBits(1)
    //  otherWrBranchKind.asBits.asUInt
    //  =/= tgtBrKindBuf(nextTgtBufWrAddr)
    //)
  )
  otherWrBtbElemWithBrKind := io.psExSetPc.btbElemWithBrKind
  wrBtbElem := (
    RegNext(
      next=otherWrBtbElemWithBrKind.btbElem,
      init=otherWrBtbElemWithBrKind.btbElem.getZero,
    )
  )

  wrBtbElem.valid.allowOverride
  wrBtbElem.valid := True

  val myResultValidCmpEqLeft = (
    Cat(
      myRdBtbElem.srcRegPc(cfg.mySrcRegPcCmpEqRangeHi),
      myRdBtbElem.srcRegPc(cfg.mySrcRegPcCmpEqRangeLo),
    ).asUInt

  )
  val myResultValidCmpEqRight = (
    Cat(
      io.inpRegPc(
        //2
        //0
        SnowHouseBranchPredictorKind._predictorInpRegPcIdxCmpEq
      )(cfg.mySrcRegPcCmpEqRangeHi),
      io.inpRegPc(
        SnowHouseBranchPredictorKind._predictorInpRegPcIdxCmpEq
      )(cfg.mySrcRegPcCmpEqRangeLo),
    ).asUInt
  )
  io.result.valid := (
    myRdBtbElem.fire
    && myRdBrKind(1)
    && (
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
    myRdBtbElem.dstRegPc
  )
  io.result.nextRegPc := (
    tempNextRegPc
  )
  //--------
  tgtSrcRegPcBuf.io.ramIo.wrAddr := rTgtBufWrAddr.head
  //--------
  tgtDstRegPcBuf.io.ramIo.wrAddr := rTgtBufWrAddr.head

  val myWrSrcRegPcAndValid = (
    Flow(
      UInt(cfg.mySrcRegPcCmpEqWidth bits)
    )
  )

  myWrSrcRegPcAndValid.payload := (
    Cat(
      wrBtbElem.srcRegPc(cfg.mySrcRegPcCmpEqRangeHi),
      wrBtbElem.srcRegPc(cfg.mySrcRegPcCmpEqRangeLo),
    ).asUInt
  )
  myWrSrcRegPcAndValid.valid := True
  //--------
  when (rTgtBufWrEn.head) {
    tgtValidBuf(rTgtBufWrAddr.head) := True
  }
  switch (
    //(
    //  rTgtBufWrEn.head
    //  //&& rTgtBufWrAddr.head === rTgtBufWrAddr.last
    //)
    //## 
    rTgtBufWrEn.last
    ## RegNext(
      io.psExSetPc.taken.myPsExSetPcValid,
      init=False
    )
    //## RegNext(
    //  io.psExSetPc.take
    //)
  ) {
    //tgtBrKindBuf(rTgtBufWrAddr) := otherWrBranchKind.asBits.asUInt
    //is (M"1-0") {
    //  tgtBrKindBuf(rTgtBufWrAddr.last) := U"2'b01" // weakly not taken
    //}
    //is (M"1-1") {
    //  tgtBrKindBuf(rTgtBufWrAddr.last) := U"2'b10" // weakly taken
    //}
    is (
      //M"010"
      M"10"
    ) {
      when (tgtBrKindBuf(rTgtBufWrAddr.last).orR) {
        tgtBrKindBuf(rTgtBufWrAddr.last) := (
          tgtBrKindBuf(rTgtBufWrAddr.last) - 1
        )
      }
    }
    is (
      //M"011"
      M"11"
    ) {
      when ((~tgtBrKindBuf(rTgtBufWrAddr.last)).orR) {
        tgtBrKindBuf(rTgtBufWrAddr.last) := (
          tgtBrKindBuf(rTgtBufWrAddr.last) + 1
        )
      }
    }
    default {
    }
  }
  tgtSrcRegPcBuf.io.ramIo.wrData := (
    myWrSrcRegPcAndValid.payload
  )
  //--------
  def myTempDstRegPc = (
    wrBtbElem.dstRegPc(
      wrBtbElem.dstRegPc.high
      downto log2Up(cfg.instrSizeBytes)
    )
  )
  tgtDstRegPcBuf.io.ramIo.wrData := (
    if (!cfg.useLcvDataBus) (
      myTempDstRegPc
    ) else (
      Cat(
        myTempDstRegPc,
      ).asUInt
    )
  )
  //--------
  tgtSrcRegPcBuf.io.ramIo.wrEn := rTgtBufWrEn.head
  //--------
  tgtDstRegPcBuf.io.ramIo.wrEn := rTgtBufWrEn.head
  //--------
}

case class SnowHouseBranchTgtBuf(
  //psIf: SnowHousePipeStageInstrFetch,
  cfg: SnowHouseConfig
) extends Component {
  val io = SnowHouseBranchTgtBufIo(cfg=cfg)

  val branchTgtBufSize = (
    cfg.optBranchPredictorKind.get._branchTgtBufSize
  )
  val branchTgtBufNumWays = (
    cfg.optBranchPredictorKind.get._branchTgtBufNumWays
  )

  require(
    branchTgtBufNumWays > 0
  )

  val myDirectMappedArea = (
    branchTgtBufNumWays == 1
  ) generate (new Area {
    val btbSingle = SnowHouseBranchTgtBufSingle(cfg=cfg)
    io <> btbSingle.io
  })
  val myNonDirectMappedArea = (
    branchTgtBufNumWays > 1
  ) generate (new Area {
    //val tgtBufRdAddr = (
    //  UInt(log2Up(branchTgtBufSize) bits)
    //)

    //def myTgtBufAddrRange: Range = (
    //  log2Up(branchTgtBufSize) - 1 + log2Up(cfg.instrSizeBytes)
    //  downto log2Up(cfg.instrSizeBytes)
    //)
    def myTgtBufAddrRange: Range = (
      log2Up(branchTgtBufSize) - 1 + log2Up(cfg.instrSizeBytes) + 1
      downto log2Up(cfg.instrSizeBytes) + 1
    )

    val rTgtBufWrEn = (
      Reg(Bool(), init=False)
    )

    rTgtBufWrEn := (
      io.psExSetPc.valid
      && io.psExSetPc.branchTgtBufElem.fire
      && (
        !io.psExSetPc.btbElemWithBrKind.btbElem.dontPredict
      )
    )
    val rTgtBufWrAddr = (
      //Vec[UInt](
        (
          RegNext(
            io.psExSetPc.branchTgtBufElem.srcRegPc(myTgtBufAddrRange)
          )
          init(0x0)
        )//,
      //  (
      //    RegNext(
      //      io.psExSetPc.taken.srcRegPc(myTgtBufAddrRange),
      //    )
      //    init(0x0)
      //  )
      //)
    )

    //for (idx <- 0 until tgtBufRdAddr.size) {
    //  tgtBufRdAddr := (
    //    io.inpRegPc(0)(myTgtBufAddrRange) //- 1//- 2 //- 1 //- 2//- 3
    //  )
    //}

    //def myDstRegPcWidth = (
    //  cfg.mainAddrWidth - log2Up(cfg.instrSizeBytes)
    //)

    val btbArr = Array.fill(branchTgtBufNumWays)(
      SnowHouseBranchTgtBufSingle(cfg=cfg)
    )

    for (idx <- 0 until btbArr.size) {
      val btb = btbArr(idx)
      btb.io.psExSetPc := btb.io.psExSetPc.getZero
      btb.io.psExSetPc.taken.allowOverride
      btb.io.psExSetPc.taken := io.psExSetPc.taken
      btb.io.inpRegPc := io.inpRegPc
      btb.io.upIsReady := io.upIsReady
      btb.io.upIsFiring := io.upIsFiring
    }

    //val tgtFifoIdxBuf = Vec.fill(branchTgtBufSize)(
    //  Reg(UInt(log2Up(btbArr.size) bits))
    //  init(0x0)
    //)
    val tgtFifoIdxBuf = Mem(
      initialContent=Array.fill(branchTgtBufSize)(
        U(s"${log2Up(btbArr.size)}'d0")
      ),
    )

    val tempTgtFifoFifoReadSync = tgtFifoIdxBuf.readSync(
      address=rTgtBufWrAddr,
      enable=rTgtBufWrEn,
    )
    tgtFifoIdxBuf.write(
      data=RegNext(tempTgtFifoFifoReadSync) + 1,
      address=RegNext(RegNext(rTgtBufWrAddr)),
      enable=RegNext(RegNext(rTgtBufWrEn, init=False), init=False),
    )
    switch (
      RegNext(RegNext(rTgtBufWrEn, init=False), init=False)
      ## RegNext(tempTgtFifoFifoReadSync)
    ) {
      for (idx <- 0 until btbArr.size) {
        is (
          btbArr.size
          | idx
        ) {
          btbArr(idx).io.psExSetPc := (
            RegNext(RegNext(RegNext(io.psExSetPc)))
          )
        }
      }
      default {
      }
    }
    //switch (
    //  //tgtBufRdAddr
    //  //tgtBufRdAddr
    //  //rTgtBufWrAddr
    //  rTgtBufWrEn
    //  ## rTgtBufWrAddr
    //) {
    //  for (idx <- 0 until branchTgtBufSize) {
    //    is (
    //      branchTgtBufSize
    //      | idx
    //    ) {
    //      //btbArr
    //      //tgtFifoIdxBuf(idx) := tgtFifoIdxBuf(idx) + 1
    //      switch (tgtFifoIdxBuf(idx)) {
    //        for (jdx <- 0 until btbArr.size) {
    //          is (jdx) {
    //            btbArr(jdx).io.psExSetPc := RegNext(io.psExSetPc)
    //          }
    //        }
    //        default {
    //        }
    //      }
    //    }
    //  }
    //  default {
    //  }
    //}
    val myResultVec = Vec.fill(btbArr.size)(
      cloneOf(io.result)
    )
    val myResultValidVec = Vec.fill(btbArr.size)(
      Bool()
    )
    for (idx <- 0 until btbArr.size) {
      myResultVec(idx) := btbArr(idx).io.result
      myResultValidVec(idx) := btbArr(idx).io.result.fire
    }
    io.result.valid := myResultValidVec.orR
    io.result.nextRegPc := 0x0
    io.result.rdBtbElem := io.result.rdBtbElem.getZero

    //val myResultFindFirst = myResultVec.sFindFirst(item => item.valid)
    //io.result.payload := myResultFindFirst._2
    

    switch (myResultValidVec.asBits) {
      for (idx <- 1 until (1 << myResultValidVec.size)) {
        is (idx) {
          println(
            s"idx:${idx} log2Thing:${log2Up(idx + 1) - 1}"
          )
          val myBtb = btbArr(
            //log2Up(idx)
            log2Up(idx + 1) - 1
          )
          //io.result.payload := btbArr(1 << log2Up(idx)).io.result.payload
          io.result.nextRegPc := myBtb.io.result.nextRegPc
          io.result.rdBtbElem := myBtb.io.result.rdBtbElem
        }
      }
    }

    //switch (
    //  tgtBufRdAddr
    //) {
    //}
  })

}

private[libsnowhouse] case class SnowHouseBusToLcvBusBridgeIo(
  cfg: SnowHouseConfig,
  isIbus: Boolean,
) extends Bundle with IMasterSlave{
  if (isIbus) {
    require(cfg.useLcvInstrBus)
  } else {
    require(cfg.useLcvDataBus)
  }
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
private[libsnowhouse] case class MyIbusRegPcInfo(
  cfg: SnowHouseConfig,
  includeRegPc: Boolean=true,
) extends Bundle {
  val regPc = (
    includeRegPc
  ) generate (
    UInt(cfg.mainAddrWidth bits)
  )
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
}
case class SnowHousePipeStageInstrFetch(
  args: SnowHousePipeStageArgs,
  //psIdHaltIt: Bool,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
  lcvIbus: LcvBusIo,
) extends Area {
  require(
    cfg.useLcvInstrBus
  )

  def cfg = args.cfg
  def io = args.io
  def cIf = args.link
  def pIf = args.currPayload
  val up = cIf.up
  val down = cIf.down
  val myH2dPushStm = (
    cloneOf(lcvIbus.h2dBus)
  )
  def myBusH2dValid = (
    myH2dPushStm.valid
  )
  def myBusAddr = (
    myH2dPushStm.addr
  )
  myBusAddr := RegNext(myBusAddr, init=myBusAddr.getZero)
  myH2dPushStm.byteSize := log2Up(cfg.instrMainWidth / 8)
  myH2dPushStm.isWrite := False
  myH2dPushStm.data := 0x0
  val nextSrc = cloneOf(myH2dPushStm.src)
  val rSrc = (
    RegNext(
      nextSrc,
      init=nextSrc.getZero,
    )
  )
  val tempSrcRnw = (
    RegNextWhen(
      next=rSrc.asSInt,
      cond=myH2dPushStm.fire,
    )
    init(-2)
  )
  nextSrc := rSrc
  myH2dPushStm.src := rSrc
  when (myH2dPushStm.fire) {
    nextSrc := rSrc + 1
  } otherwise {
    myH2dPushStm.src := tempSrcRnw.asUInt
  }

  val myReadyIshCond = Bool()
  val myReadyIshCondShared = (
    myH2dPushStm.fire
  )
  myReadyIshCond := (
    myReadyIshCondShared
  )

  val myRegPcSetItCnt = (
    UInt(cfg.myPsIfRegPcSetItCntWidth bits)
  )
  val rPrevRegPcSetItCnt = {
    val temp = (
      RegNextWhen(
        next=myRegPcSetItCnt,
        cond=myReadyIshCond,
      )
      init(0x0)
    )
    temp
  }
  myRegPcSetItCnt.allowOverride
  myRegPcSetItCnt := rPrevRegPcSetItCnt

  case class MyIbusTempPayload(
    hasInstr: Boolean,
  ) extends Bundle {
    val instr = (
      hasInstr
    ) generate (
      UInt(cfg.instrMainWidth bits)
    )

    val psIfRegPcSetItCnt = (
      cloneOf(myRegPcSetItCnt)
    )

    val myIbusRegPcInfo = (
      MyIbusRegPcInfo(cfg=cfg)
    )
  }
  val myIbusTempRam = {
    val depth = 1 << cfg.subCfg.myLcvBusSrcWidth
    //val initBigtInt = Array(
    //  Array.fill(depth)(
    //    BigInt(0)
    //  ).toSeq
    //).toSeq
    def mySetWordFunc(
      outp: MyIbusTempPayload,
      inp: MyIbusTempPayload,
      word: MyIbusTempPayload,
      upIsFiring: Bool,
      myExternalInpCond: Bool,
      wrPulse: Flow[
        PipeSimpleDualPortMemDrivePayload[MyIbusTempPayload]
      ],
    ): Unit = {
      outp.psIfRegPcSetItCnt := word.psIfRegPcSetItCnt
      outp.myIbusRegPcInfo := word.myIbusRegPcInfo
      outp.instr.allowOverride
      outp.instr := inp.instr
    }
    val ramCfg = WrPulseRdPipeRamConfig(
      modType=(
        MyIbusTempPayload(hasInstr=true)
      ),
      wordType=MyIbusTempPayload(hasInstr=false),
      wordCount=depth,
      //pipeName="pipeStageIf",
      setWordFunc=mySetWordFunc,
      optRdLatency=(
        //1
        2
        //3
        //4
      ),
      optWrHistLength=(
        //1
        2
        //3
        //4
      ),
      initBigInt=(
        //Some(initBigInt)
        Some({
          val myArr = new ArrayBuffer[ArrayBuffer[BigInt]]()
          myArr += new ArrayBuffer[BigInt]()
          for (idx <- 0 until depth) {
            myArr.last += BigInt(0)
          }
          myArr
        })
      ),
      arrRamStyleAltera=(
        //"no_rw_check, M10K",//"MLAB",//"M10K"
        "no_rw_check, MLAB",//"MLAB",//"M10K"
      ),
      arrRamStyleXilinx=(
        //"block"
        "distributed"
      ),
    )
    WrPulseRdPipeRam(cfg=ramCfg)
  }

  val rIbusTempRamInitCnt = {
    val temp = Reg(UInt((cfg.subCfg.myLcvBusSrcWidth + 2) bits))
    temp.init(temp.getZero)
    temp
  }

  // NOTE: setting `myBusH2dValid` to a constant `True` can cause issues
  // if the `libsnowhouse` implementation of a CPU is hooked up a
  // non-pipelined `LcvBus` instruction source
  // (such as FL4SHK's own `LcvBusMemSlowUnlessBurst` module)
  myBusH2dValid := (
    //rIbusTempRamInitCnt.msb
    True
  )
  val myIbusRegPcInfo = MyIbusRegPcInfo(cfg=cfg)
  def myD2hPopStm = lcvIbus.d2hBus
  //when (rIbusTempRamInitCnt.msb) {
    cIf.up.driveFrom(myIbusTempRam.io.rdDataPipe)(
      con=(node, payload) => {
        node(pIf) := node(pIf).getZero
        node(pIf).encInstr.payload.allowOverride
        node(pIf).psIfRegPcSetItCnt.allowOverride
        node(pIf).regPc.allowOverride
        node(pIf).laggingRegPc.allowOverride
        node(pIf).branchPredictTkn.allowOverride
        node(pIf).branchTgtBufElem.allowOverride

        node(pIf).encInstr.payload := payload.instr
        node(pIf).psIfRegPcSetItCnt := payload.psIfRegPcSetItCnt
        node(pIf).regPc := payload.myIbusRegPcInfo.regPc
        node(pIf).laggingRegPc := payload.myIbusRegPcInfo.regPc
        node(pIf).branchPredictTkn := (
          payload.myIbusRegPcInfo.branchPredictTkn
          //True
        )
        node(pIf).branchTgtBufElem := (
          payload.myIbusRegPcInfo.branchTgtBufElem
        )
      }
    )

    myIbusTempRam.io.wrPulse.valid := myH2dPushStm.fire
    myIbusTempRam.io.wrPulse.addr := myH2dPushStm.src //+ 1
    myIbusTempRam.io.wrPulse.data.psIfRegPcSetItCnt := (
      myRegPcSetItCnt
    )
    myIbusTempRam.io.wrPulse.data.myIbusRegPcInfo := (
      myIbusRegPcInfo
    )
  //} otherwise {
  //  cIf.up.valid := False
  //  cIf.up(pIf) := cIf.up(pIf).getZero
  //  myIbusTempRam.io.wrPulse.valid := True
  //  myIbusTempRam.io.wrPulse.addr := rIbusTempRamInitCnt(
  //    rIbusTempRamInitCnt.high - 2 downto 0
  //  )
  //  myIbusTempRam.io.wrPulse.data := (
  //    myIbusTempRam.io.wrPulse.data.getZero
  //  )
  //  myIbusTempRam.io.rdDataPipe.ready := True

  //  rIbusTempRamInitCnt := rIbusTempRamInitCnt + 1
  //}
  lcvIbus.h2dBus << myH2dPushStm
  myD2hPopStm.translateInto(myIbusTempRam.io.rdAddrPipe)(
    dataAssignment=(outp, inp) => {
      outp := outp.getZero
      outp.data.instr.allowOverride
      outp.data.instr := inp.data.resize(outp.data.instr.getWidth)
      outp.addr.allowOverride
      outp.addr := inp.src
    }
  )

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
  val branchTgtBuf = (
    cfg.haveBranchPredictor
  ) generate (
    SnowHouseBranchTgtBuf(cfg=cfg)
    //SnowHouseBranchTgtBufSingle(cfg=cfg)
  )
  if (cfg.haveBranchPredictor) {
    branchTgtBuf.io.psExSetPc := psExSetPc
    branchTgtBuf.io.upIsFiring := up.isFiring
    branchTgtBuf.io.upIsReady := myReadyIshCond //myUpdatePcCond
  }

  val takeJumpCntMaxVal = cfg.takeJumpCntMaxVal
  val rTakeJumpCnt = {
    val temp = Reg(Flow(UInt(
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
  def myRegPc = myIbusRegPcInfo.regPc
  val myPrevRegPcPlusInstrSizeWidth = (
    myRegPc.getWidth - log2Up(cfg.instrSizeBytes)
  )
  val rPrevRegPc = {
    val temp = RegNextWhen(
      next=(
        Vec.fill(2)(
          myRegPc.asSInt(myRegPc.high downto log2Up(cfg.instrSizeBytes))
        )
      ), 
      cond=myReadyIshCond,
    )
    temp.foreach(item => {
      item.init(-1)
    })
    temp
  }
  val myRawPredictCond = Bool()
  myRawPredictCond := (
    branchTgtBuf.io.result.fire
    && !rTakeJumpCnt.fire
    && !stickyExSetPc(0).fire
  )
  val rMyMainPredictCond = (
    //Reg(Bool(), init=False)
    RegNextWhen(
      myRawPredictCond,
      cond=myReadyIshCond,
      init=myRawPredictCond.getZero,
    )
  )
  val predictCond = (
    cfg.haveBranchPredictor
  ) generate (
    //myMainPredictCond
    rMyMainPredictCond
  )

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
      rMyMainPredictCond,
      (
        RegNextWhen(
          branchTgtBuf.io.result.nextRegPc.asSInt,
          cond=myReadyIshCond,
          init=branchTgtBuf.io.result.nextRegPc.asSInt.getZero,
        )
      ),
      (
        myTempNextRegPcMaybeDel1
      ),
    ).asUInt
  )
  if (cfg.haveBranchPredictor) {
    for (idx <- 0 until branchTgtBuf.io.inpRegPc.size) {
      //when (!rTakeJumpCnt.fire) {
        // TODO: determine if this is correct!
        branchTgtBuf.io.inpRegPc(idx) := (
          myPredictedNextPc
          //RegNextWhen(
          //  myPredictedNextPc + cfg.instrSizeBytes,
          //  cond=myReadyIshCond,
          //  init=myPredictedNextPc.getZero,
          //)
          //Cat(
          //  (rPrevRegPc(0) + 1),
          //  myRegPcShiftThing,
          //).asUInt
        )
      //} otherwise {
      //  branchPredictor.io.inpRegPc(idx) := (
      //    //myPredictedNextPc
      //    Cat(
      //      (rPrevRegPc(0) + 1),
      //      myRegPcShiftThing,
      //    ).asUInt
      //  )
      //}
    }
  }
  def doInitTakeJumpCnt(): Unit = {
    rTakeJumpCnt.valid := True
    rTakeJumpCnt.payload := takeJumpCntMaxVal
  }

  val myUpdateRegPcCondUInt = (
    Cat(
      List(
        myReadyIshCond,
        //myUpdatePcCond,
        stickyExSetPc.head.fire,
      ).reverse
    )
  )
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
      myBusAddr := tempNextRegPc.resize(myBusAddr.getWidth)//.asUInt
    }
    switch (myUpdateRegPcCondUInt) {
      is (M"0-") {
      }
      is (M"10") {
        if (cfg.haveBranchPredictor) {
          val temp = myPredictedNextPc
          myBusAddr := temp.resize(myBusAddr.getWidth)
          myIbusRegPcInfo.regPc := temp
          myIbusRegPcInfo.branchPredictTkn.allowOverride
          myIbusRegPcInfo.branchPredictTkn := (
            myRawPredictCond
          )
          myIbusRegPcInfo.branchTgtBufElem.foreach(item => {
            item := (
              branchTgtBuf.io.result.rdBtbElem
            )
          })
        } else {
          val temp = (
            Cat(
              rPrevRegPc.last + 1,
              myRegPcShiftThing,
            ).asUInt
          )
          myBusAddr := temp.resize(myBusAddr.getWidth)
          myIbusRegPcInfo.regPc := temp
        }
      }
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

  when (myReadyIshCond) {
    myRegPcSetItCnt := 0x0
    when (rTakeJumpCnt.fire) {
      when (
        (
          myH2dPushStm.addr
          === RegNext(
            stickyExSetPc(0).branchTgtBufElem.dstRegPc,
            init=stickyExSetPc(0).branchTgtBufElem.dstRegPc.getZero,
          )
        )
      ) {
        rTakeJumpCnt.valid := False
        myRegPcSetItCnt := 0x1
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
  //val psIdHaltIt: Bool,
  val psExSetPc: Flow[SnowHousePsExSetPcPayload],
  //val pcChangeState: Bool/*UInt*/,
  //val shouldIgnoreInstr: Bool,
  val doDecodeFunc: (SnowHousePipeStageInstrDecode) => Area,
  //val psIdFoundBubble: Bool,
  val myScoreboardCommitStm: Stream[SnowHouseScoreboardCommitPayload],
  val myScoreboardBubbleRetireStm:
    // needed because bubbles still need to have tagging info
    // erased from psId
    Stream[SnowHouseScoreboardCommitPayload],
  val myScoreboardSavedGprTagVec: UInt,
  val myScoreboardReorderBufInFlushEtc: Bool,
  val myScoreboardReorderBufPsIdCanIssue: Bool,
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
  val myTempOpIsMemAccessLoad = Bool()
  val myTempOpIsMemAccessStore = Bool()
  val myTempOpMayNeedHazardCheck = Bool()
  val myTempOpIsDualWidth = Bool()
  val myTempOpIsJmpBr = Bool()

  val myTempBtbElem = BranchTgtBufElem(cfg=cfg)

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
  val myNonBubbleCond = Bool()

  //when (up.isFiring) {
  if (!cfg.optScoreboard) {
    up(pId) := upPayload(1)//(0)
  } else {
    down(pId) := upPayload(1)
  }
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
    item := RegNext(item, init=item.getZero)
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
  //val myIraPc = UInt(
  //  //cfg.mainWidth bits
  //  cfg.mainAddrWidth bits
  //)
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

  val shouldClearExtraDecodeInfo = Bool()
  shouldClearExtraDecodeInfo := (
    RegNext(
      shouldClearExtraDecodeInfo,
      init=shouldClearExtraDecodeInfo.getZero
    )
  )
  when (psExSetPc.valid) {
    shouldClearExtraDecodeInfo := True
  }
  //--------
  val mySeenPsIfRegPcSetItCntLsb = upPayload(1).psIfRegPcSetItCnt(0)
  val rSavedSeenPsIfRegPcSetItCntLsb = Reg(Bool(), init=False)

  val stickySeenPsIfRegPcSetItCntLsb = (
    mySeenPsIfRegPcSetItCntLsb
    || rSavedSeenPsIfRegPcSetItCntLsb
  )
  when (rose(mySeenPsIfRegPcSetItCntLsb)) {
    rSavedSeenPsIfRegPcSetItCntLsb := True
  }
  val shouldFinishJump = (
    stickySeenPsIfRegPcSetItCntLsb
    && (
      upPayload(1).laggingRegPc(myRegPcRange)
      === RegNextWhen(
        //(psExSetPc.nextPc(myRegPcRange) + 3),
        psExSetPc.branchTgtBufElem.dstRegPc(myRegPcRange),
        cond=psExSetPc.fire,
        init=psExSetPc.branchTgtBufElem.dstRegPc(myRegPcRange).getZero
      )
    )
  )
  when (shouldFinishJump) {
    shouldClearExtraDecodeInfo := False
  }

  for (idx <- 0 until upPayload(1).regPcSetItCnt.size) {
    //val rPrevRegPcSetItCnt = (
    //  RegNextWhen(
    //    next=upPayload(1).regPcSetItCnt(idx),
    //    cond=up.isFiring,
    //    init=upPayload(1).regPcSetItCnt(idx).getZero,
    //  )
    //)
    when (up.isFiring) {
      when (shouldFinishJump) {
        rSavedSeenPsIfRegPcSetItCntLsb := False
        upPayload(1).regPcSetItCnt(idx) := (
          //0x2
          0x1
        )
      } otherwise {
        upPayload(1).regPcSetItCnt(idx) := 0x0
      }
    }
  }
  //--------
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

  upPayload(1).regPcPlusInstrSize := (
    //upPayload(1).regPc - (1 * cfg.instrSizeBytes) //- cfg.instrSizeBytes
    //upPayload(1).branchTgtBufElem(1).srcRegPc
    upPayload(1).laggingRegPc
    + (1 * cfg.instrSizeBytes)
    //+ (2 * cfg.instrSizeBytes)
    //+ (2 * cfg.instrSizeBytes)
    //+ (2 * cfg.instrSizeBytes)
    ////- (cfg.instrMainWidth.toLong / 8.toLong)
    //upPayload.regPcPlus1Instr
  )
  def myRegPcRange = (
    upPayload(1).regPc.high downto log2Up(cfg.instrSizeBytes)
  )
  //val myHistRegPc = (
  //  History[SInt](
  //    that=(
  //      //upPayload(1).regPc(myRegPcRange).asSInt
  //      upPayload(1).laggingRegPc(myRegPcRange).asSInt
  //    ),
  //    length=upPayload(1).myHistRegPcSize,
  //    when=up.isFiring,
  //    init=(
  //      //upPayload(1).regPc(myRegPcRange).asSInt.getZero
  //      upPayload(1).laggingRegPc(myRegPcRange).asSInt.getZero
  //    ),
  //  )
  //)

  //val myDspRegPcPlus1InstrSize = {
  //  val myWordWidth = (
  //    //cfg.mainWidth - log2Up(cfg.instrSizeBytes)
  //    cfg.mainAddrWidth - log2Up(cfg.instrSizeBytes)
  //  )
  //  //LcvCondAddJustCarryDel1(
  //  //  wordWidth=myWordWidth
  //  //)
  //  //LcvAddJustCarryDel1(
  //  //  wordWidth=myWordWidth
  //  //)
  //  new Area {
  //    val wordWidth = myWordWidth
  //    val io = new Bundle {
  //      val inp = new Bundle {
  //        val a = SInt(wordWidth bits)
  //        //val b = SInt(wordWidth bits)
  //        val carry = Bool()
  //        val cond = Bool()
  //      }
  //      val outp = new Bundle {
  //        val sum_carry = SInt(wordWidth + 1 bits)
  //      }
  //    }
  //    val tempA = Cat(False, io.inp.a).asSInt
  //    //val tempB = Cat(False, io.inp.b).asSInt
  //    val tempCarry = Cat(
  //      U(s"${wordWidth}'d0"), 
  //      io.inp.carry
  //    ).asSInt
  //    val myTempSumCarry = tempA + tempCarry

  //    //if (!cfg.useLcvInstrBus) {
  //      io.outp.sum_carry := (
  //        RegNextWhen(
  //          next=myTempSumCarry,
  //          cond=io.inp.cond,
  //        )
  //        init(0x0)
  //      )
  //    //} else {
  //    //  io.outp.sum_carry := (
  //    //    RegNext(io.outp.sum_carry, init=io.outp.sum_carry.getZero)
  //    //  )
  //    //  when (io.inp.cond) {
  //    //    io.outp.sum_carry := myTempSumCarry
  //    //  }
  //    //}
  //  }
  //}
  //val myHistRegPcPlus1InstrSize = (
  //  Vec.fill(
  //    upPayload(1).myHistRegPcSize - 1
  //  )(
  //    SInt(
  //      //cfg.mainWidth - log2Up(cfg.instrSizeBytes)
  //      cfg.mainAddrWidth - log2Up(cfg.instrSizeBytes)
  //      bits
  //    )
  //  )
  //)
  //myDspRegPcPlus1InstrSize.io.inp.a := (
  //  //myHistRegPc(1)
  //  myHistRegPc(0)
  //)
  //myDspRegPcPlus1InstrSize.io.inp.carry := True
  //myDspRegPcPlus1InstrSize.io.inp.cond := up.isFiring
  //for (idx <- 0 until myHistRegPcPlus1InstrSize.size) {
  //  if (idx == 0) {
  //    myHistRegPcPlus1InstrSize(idx) := (
  //      myDspRegPcPlus1InstrSize.io.outp.sum_carry(
  //        myHistRegPcPlus1InstrSize(idx).bitsRange
  //      )
  //    )
  //  } else {
  //    myHistRegPcPlus1InstrSize(idx) := (
  //      RegNext(
  //        next=myHistRegPcPlus1InstrSize(idx),
  //        init=myHistRegPcPlus1InstrSize(idx).getZero,
  //      )
  //    )
  //    when (RegNext(next=up.isFiring, init=False)) {
  //      myHistRegPcPlus1InstrSize(idx) := (
  //        RegNext(
  //          next=myHistRegPcPlus1InstrSize(idx - 1),
  //          init=myHistRegPcPlus1InstrSize(idx - 1).getZero,
  //        )
  //      )
  //    }
  //  }
  //}
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

  //if (cfg.irqCfg != None) {
  //  upPayload(1).takeIrq := False
  //}
  //upPayload(1).irqIraRegPc.head := (
  //  //upPayload(1).laggingRegPc
  //  Cat(
  //    (
  //      upPayload(1).laggingRegPc(
  //        upPayload(1).laggingRegPc.high
  //        downto log2Up(cfg.instrSizeBytes)
  //      )
  //    ),
  //    U(s"${log2Up(cfg.instrSizeBytes)}'d0"),
  //  ).asUInt
  //)
  //if (!cfg.useLcvDataBus) {
  //  //upPayload(1).irqIraRegPc.last := (
  //  //  upPayload(1).laggingRegPc
  //  //)
  //} else {
  //  //for (idx <- 0 until upPayload(1).irqIraRegPc
  //  //upPayload(1).irqIraRegPc.head := (
  //  //  upPayload(1).laggingRegPc
  //  //)
  //  upPayload(1).irqIraRegPc.last := (
  //    //upPayload(1).laggingRegPc + cfg.instrSizeBytes
  //    Cat(
  //      (
  //        upPayload(1).laggingRegPc(
  //          upPayload(1).laggingRegPc.high
  //          downto log2Up(cfg.instrSizeBytes)
  //        ) + 1
  //      ),
  //      U(s"${log2Up(cfg.instrSizeBytes)}'d0"),
  //    ).asUInt
  //  )
  //}
  //  when (!upPayload(1).haveLcvDbusMemAccDelay) {
  //    upPayload(1).irqIraRegPc := (
  //      //upPayload(1).regPc
  //      upPayload(1).laggingRegPc
  //    )
  //  } otherwise {
  //    upPayload(1).irqIraRegPc(
  //      upPayload(1).irqIraRegPc.high
  //      downto log2Up(cfg.instrSizeBytes)
  //    ) := (
  //      upPayload(1).laggingRegPc(
  //        upPayload(1).laggingRegPc.high
  //        downto log2Up(cfg.instrSizeBytes)
  //      ) + 1
  //    )
  //  }
  //}
  //if (!cfg.useLcvDataBus) {
  //} else { // if (cfg.useLcvDataBus)
  //}

  val myDecodeAreaWithoutUcode = (
    !cfg.supportUcode
  ) generate (
    doDecodeFunc(this)
  )
  startDecode := True
  tempInstr := myInstr

  val myNonLcvDbusPartAArea = (
    !cfg.useLcvDataBus
  ) generate (new Area {
    upPayload(1).branchTgtBufElem(1) := myTempBtbElem
  })

  object MyLcvDbusStallState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      POST_LD_0,
      POST_LD_1
      = newElement();
  }
  upPayload(1).instrCnt.myPsIdBubble.foreach(item => {
    item := False
  })
  def doSendBubbleMainMost(
    myPsIdBubble: Option[Bool]=Some(True),
    //myUpdateGprIsOrIsntZero: Boolean=true,
    //myPsIdReorderBufForceValid: Option[Bool]=None,
    myPsIdOtherBubble: Option[Bool]=None,
    myPsIdFwdBubble: Option[Bool]=None,
    myInFlushCond: Option[Bool]=None,
  ): Unit = {
    require(cfg.useLcvDataBus)
    cId.duplicateIt()
    //upPayload(1).setAsBubbleMain(Some(True))

    //down(pId) := upPayload(1)
    down(pId).setAsBubbleMain(
      //Some(True)
      myPsIdBubble=myPsIdBubble,
      //myUpdateGprIsOrIsntZero=myUpdateGprIsOrIsntZero
      //myPsIdReorderBufForceValid=myPsIdReorderBufForceValid,
    )
    if (myPsIdOtherBubble != None) {
      down(pId).instrCnt.myPsIdOtherBubble.foreach(item => {
        item := myPsIdOtherBubble.get
      })
    }
    if (myPsIdFwdBubble != None) {
      down(pId).instrCnt.myPsIdFwdBubble.foreach(item => {
        item := myPsIdFwdBubble.get
      })
    }
    if (
      cfg.optScoreboard
      && myInFlushCond != None
    ) {
      //when (!myInFlushCond.get) {
        down(pId).instrCnt.scoreboardIssuePayload.fwdTag := 0x0
        down(pId).instrCnt.scoreboardIssuePayload.nonFwdTag := 0x0
      //} otherwise {
      //  down(pId).instrCnt.scoreboardIssuePayload.fwdTag := (
      //    upPayload(1).instrCnt.scoreboardIssuePayload.fwdTag
      //  )
      //  down(pId).instrCnt.scoreboardIssuePayload.nonFwdTag := (
      //    upPayload(1).instrCnt.scoreboardIssuePayload.nonFwdTag
      //  )
      //}
    }
  }

  val myNonScoreboardLcvDbusPartAArea = (
    cfg.useLcvDataBus
    && !cfg.optScoreboard
  ) generate (new Area {
    //psIdFoundBubble := RegNext(psIdFoundBubble, init=False)
    down(pId).allowOverride
    //val mySeenDownFire = Bool()
    //val rSavedSeenDownFire
    val rStallState = (
      //Reg(Bool(), init=False)
      Reg(MyLcvDbusStallState())
      init(MyLcvDbusStallState.IDLE)
    )
    upPayload(1).branchTgtBufElem(1) := (
      //upPayload(1).branchTgtBufElem(1).getZero
      myTempBtbElem
    )
    def numFollowingInstrs = (
      cfg.myPsIdBubbleNumFollowingInstrs
      //upPayload(1).myDoHaveHazardAddrCheckVec.size
      //1
      //2
      // OLD notes:
      //// up to two following instructions,
      //// per the overall pipeline structure of EX -> MEM -> WB -> LastBack
    )
    //val myHistTempBtbElem = (
    //  History[BranchTgtBufElem](
    //    that=myTempBtbElem,
    //    length=(
    //      numFollowingInstrs + 1
    //    ),
    //    when=down.isFiring,
    //    init=myTempBtbElem.getZero,
    //  )
    //)

    //val nextBubbleCnt = (
    //  UInt(log2Up(numFollowingInstrs + 1) + 1 bits)
    //)
    val rBubbleCnt = (
      cfg.optForFmax
    ) generate (
      Reg(SInt(log2Up(numFollowingInstrs + 1) + 1 bits))
      //RegNext(nextBubbleCnt)
      init(
        -1
        //numFollowingInstrs - 1
      )
    )
    if (!cfg.optForFmax) {
      myNonBubbleCond := down.isFiring
    } else {
      myNonBubbleCond := (
        //up.isFiring
        down.isFiring
        //&& rBubbleCnt.msb
      )
    }
    val myHistCondAnyBubble = (
      History[Bool](
        that=(
          //upPayload(1).splitOp.opIsMemAccess
          if (!cfg.optForFmax) (
            myTempOpIsMemAccessLoad
            || myTempOpIsMemAccessStore
          ) else (
            //myTempOpIsMemAccessLoad
            //myTempOpHasHazard
            myTempOpMayNeedHazardCheck
          )
          //--------
          // FL4SHK NOTE:
          // Without a bubble,
          // there appears to be non-working behavior in
          // operand forwarding for *any* kind of memory access
          // (i.e. *not only for loads*),
          // so we insert a bubble following *any* memory access
          // instruction that makes use of this.
          // I think this has something to do with the fact that
          // `PipeRegFile`'s
          // support for operand forwarding is computed ahead of time for
          // the purposes of having a higher maximum clock rate.
          // All of this is to say,
          // the below line apparently needs to stay
          // commented-out.
          //&& !upPayload(1).inpDecodeExt.head.memAccessKind.asBits(1)
          //--------
          //&& !shouldClearExtraDecodeInfo
        ),
        length=(
          //upPayload(1).myDoHaveHazardAddrCheckVec.size + 1
          numFollowingInstrs + 1
        ),
        when=(
          //down.isFiring
          myNonBubbleCond
          //myNonBubbleCond
          //if (!cfg.optForFmax) (
          //  down.isFiring
          //) else (
          //  //&& !rBubbleCnt.orR
          //  //down.isFiring
          //  //&& rBubbleCnt.msb
          //)
            //up.isFiring
        ),
        init=False
      )
      //RegNextWhen(
      //  (
      //    //setOutpModMemWord.io.opIsMemAccess.last
      //    upPayload(1).splitOp.opIsMemAccess
      //    && !upPayload(1).inpDecodeExt.head.memAccessKind.asBits(1)
      //  ),
      //  cond=up.isFiring,
      //  init=False,
      //)
    )
    switch (rStallState) {
      is (MyLcvDbusStallState.IDLE) {
        when (up.isValid) {
          //for (idx <- 0 until numFollowingInstrs) {
          //  when (
          //    (
          //      upPayload(1).myDoHaveHazardAddrCheckVec(idx + 0)
          //      || (
          //        (
          //          //myHistCondStoreBubble.asBits(
          //          //  myHistCondStoreBubble.asBits.high -1
          //          //  downto 0
          //          //).orR
          //          myHistCondStoreBubble.asBits.orR
          //        )
          //        && (
          //          !(
          //            //upPayload(1).inpDecodeExt.last.memAccessKind
          //            //.asBits(1)
          //            myTempOpIsMemAccessStore
          //          )
          //        )
          //      )
          //    )
          //    && myHistCondMemAccBubble(idx + 1)
          //    && !shouldClearExtraDecodeInfo
          //  ) {
          //    doSendBubbleMainMost()
          //    when (down.isFiring) {
          //      //rStallState := True
          //      rStallState := (
          //        //if (idx == 0) (
          //          MyLcvDbusStallState.POST_LD_0
          //        //) else (
          //        //  MyLcvDbusStallState.POST_LD_1
          //        //)
          //      )
          //    }
          //  }
          //}
          for (idx <- 0 until numFollowingInstrs) {
            when (
              //(
              //  (
              //    //upPayload(1).myDoHaveHazardAddrCheckVec(idx + 0)
              //    //&& myHistCondMemAccBubble(idx + 1)
              //  )
              //  //|| (
              //  //  (
              //  //    //myHistCondStoreBubble.asBits(
              //  //    //  myHistCondStoreBubble.asBits.high -1
              //  //    //  downto 0
              //  //    //).orR
              //  //    myHistCondStoreBubble.asBits.orR
              //  //  )
              //  //  && (
              //  //    //!(
              //  //      //upPayload(1).inpDecodeExt.last.memAccessKind
              //  //      //.asBits(1)
              //  //      //myTempOpIsMemAccessStore
              //  //    //)
              //  //    myTempOpIsMemAccessLoad
              //  //  )
              //  //  //&& myHistCondMemAccBubble.head
              //  //)
              //)

              //(
              //  upPayload(1).myDoHaveHazardAddrCheckVec(idx + 0)
              //  || (
              //    //myHistCondMemAccBubble(idx + 1)
              //    //myHistCondStoreBubble(idx + 1)
              //    //&& myTempOpIsMemAccessLoad
              //    myHistCondMemAccBubble(idx + 1)
              //    && (
              //      myHistCondMemAccBubble(0)
              //    )
              //  )
              //  || (
              //    myHistCondMemAccBubble(idx + 1)
              //    && myTempOpIsJmpBr
              //  )
              //)
              //&& 
              //(
              //  upPayload(1).myDoHaveHazardAddrCheckVec(idx + 0)
              //  || myTempOpIsJmpBr
              //)
              //&& 
              (
                myHistCondAnyBubble(idx + 1)
                //&& upPayload(1).myDoHaveHazardAddrCheckVec(idx)
                && upPayload(1).myDoHaveHazardAddrCheckVec.orR
              )
              && !shouldClearExtraDecodeInfo
            ) {
              doSendBubbleMainMost()
              when (down.isFiring) {
                //rStallState := True
                //rBubbleCnt := rBubbleCnt + 1
                if (cfg.optForFmax) {
                  rBubbleCnt := numFollowingInstrs - 1//2//1
                }

                rStallState := (
                  if (!cfg.optForFmax) (
                    MyLcvDbusStallState.POST_LD_1
                  ) else ( // if (cfg.optForFmax)
                    MyLcvDbusStallState.POST_LD_0
                  )
                  ////if (idx == 0) (
                  //  MyLcvDbusStallState.POST_LD_0
                  ////) else (
                  ////  MyLcvDbusStallState.POST_LD_1
                  ////)
                )
              }
            }
          }
        }
      }
      if (cfg.optForFmax) {
        is (MyLcvDbusStallState.POST_LD_0) {
          doSendBubbleMainMost()
          when (down.isFiring) {
            rBubbleCnt := rBubbleCnt - 1//+ 1
          }
          when (
            down.isFiring
            //&& rBubbleCnt === numFollowingInstrs //- 1
            && (rBubbleCnt - 1/*2*/).msb
          ) {
            rStallState := MyLcvDbusStallState.POST_LD_1
          }
        }
        is (MyLcvDbusStallState.POST_LD_1) {
          //rBubbleCnt := 0x0
          when (up.isFiring) {
            rBubbleCnt := rBubbleCnt - 1
          }
        }
      }
      //is (MyLcvDbusStallState.POST_LD_1) {
      //  //when (up.isFiring) {
      //  //  rStallState := MyLcvDbusStallState.IDLE
      //  //}
      //}
    }
    when (up.isFiring) {
      rStallState := MyLcvDbusStallState.IDLE
      upPayload(1).splitOp.opIsDualWidth := (
        myTempOpIsDualWidth
      )
    } otherwise {
      upPayload(1).splitOp.opIsDualWidth := (
        False
      )
    }
    //upPayload(1).splitOp.opIsDualWidth := (
    //  myTempOpIsDualWidth
    //)
    //for (idx <- 0 until numFollowingInstrs) {
    //  //when (rose(rStallState.asBits(idx + 1))) {
    //  //  upPayload(1).branchTgtBuf
    //  //}
    //}
  })

  object ScoreboardFlushState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      FLUSH
      = newElement()
  }

  val myScoreboardLcvDbusPartAArea = (
    cfg.useLcvDataBus
    && cfg.optScoreboard
  ) generate (new Area {
    //psIdFoundBubble := RegNext(psIdFoundBubble, init=False)
    down(pId).allowOverride

    val rScoreboardFlushState = (
      Reg(ScoreboardFlushState())
      init(ScoreboardFlushState.IDLE)
    )

    upPayload(1).branchTgtBufElem(1) := (
      //upPayload(1).branchTgtBufElem(1).getZero
      myTempBtbElem
    )

    myScoreboardCommitStm.ready := True
    myScoreboardBubbleRetireStm.ready := True

    def myTempNonFwdTag = (
      upPayload(1).instrCnt.scoreboardIssuePayload.nonFwdTag
    )
    def myTempFwdTag = (
      upPayload(1).instrCnt.scoreboardIssuePayload.fwdTag
    )

    myTempNonFwdTag := (
      RegNext(
        myTempNonFwdTag,
        init=myTempNonFwdTag.getZero
      )
    )
    myTempFwdTag := (
      RegNext(
        myTempFwdTag,
        init=myTempFwdTag.getZero
      )
    )

    val rMyNonFwdGprTagVec = (
      //Reg(UInt(cfg.numGprs bits))
      //init(0x0)
      Vec.fill(cfg.numGprs)(
        Reg(Bool(), init=False)
      )
    )
    case class MyFwdGprTag(
    ) extends Bundle {
      val valid = Bool()
      def fire = valid
      val tag = UInt(cfg.optScoreboardTagWidth bits)
      val cnt = UInt(log2Up(cfg.optForFmaxPsExFwdSize - 2 + 1) + 1 bits)
    }

    val rMyFwdGprTagVec = {
      //Reg(UInt(cfg.numGprs bits))
      //init(0x0)
      val temp = Vec.fill(cfg.numGprs)(
        //Reg(Bool(), init=False)
        Reg(MyFwdGprTag())
      )
      temp.foreach(item => item.init(item.getZero))
      temp
    }

    //val myGprUseCntRamArr = {
    //  Array.fill(cfg.maxNumGprsPerInstr)({
    //    // handle RAW hazards, i.e. for when we run out of room in the
    //    // forwarding history tables in the PreFwd and EX
    //    val depth = cfg.numGprs
    //    val ramCfg = RamSimpleDualPortConfig(
    //      wordType=UInt(
    //        //3 bits
    //        //log2Up(cfg.myPsIdBubbleNumFollowingInstrs + 1) + 1 bits
    //        //log2Up(cfg.optForFmaxPsExFwdSize + 1) + 1 bits
    //        log2Up(cfg.optForFmaxPsExFwdSize + 1) + 1 bits
    //      ),
    //      depth=depth,
    //      initBigInt=Some(
    //        Array.fill(depth)(
    //          //BigInt(0)
    //          BigInt(cfg.optForFmaxPsExFwdSize - 1)
    //        )
    //      ),
    //      arrRamStyleAltera="no_rw_check, MLAB",
    //      arrRamStyleXilinx="auto",
    //      doAsyncRead=true
    //    )
    //    RamSimpleDualPort(cfg=ramCfg)
    //    //val temp = Mem(
    //    //  wordType=(
    //    //    UInt(
    //    //      //3 bits
    //    //      //log2Up(cfg.myPsIdBubbleNumFollowingInstrs + 1) + 1 bits
    //    //      //log2Up(cfg.optForFmaxPsExFwdSize + 1) + 1 bits
    //    //      log2Up(cfg.optForFmaxPsExFwdSize + 1) + 1 bits
    //    //    )
    //    //  ),
    //    //  wordCount=(
    //    //    cfg.numGprs
    //    //  )
    //    //)
    //    //temp.initBigInt(
    //    //  Array.fill(temp.wordCount)(
    //    //    //BigInt(0)
    //    //    BigInt(cfg.optForFmaxPsExFwdSize - 1)
    //    //  )
    //    //)
    //    //temp
    //  })
    //}
    //for (idx <- 0 until cfg.maxNumGprsPerInstr) {
    //  val ram = myGprUseCntRamArr(idx)
    //  ram.io.ramIo.rdEn := True
    //  ram.io.ramIo.rdAddr := upPayload(1).gprIdxVec(idx)
    //  //ram.io.ramIo.wrEn := 
    //  ram.io.ramIo.wrAddr := upPayload(1).gprIdxVec.last
    //}

    //val rMyGprInUseCntVec = (
    //  Vec.fill(cfg.numGprs)(
    //    //Reg(Bool(), init=False)
    //    Reg(UInt(
    //      //3 bits
    //      //log2Up(cfg.myPsIdBubbleNumFollowingInstrs + 1) + 1 bits
    //      //log2Up(cfg.optForFmaxPsExFwdSize + 1) + 1 bits
    //      log2Up(cfg.optForFmaxPsExFwdSize + 1) + 1 bits
    //    ))
    //    init(
    //      //0x3
    //      //cfg.myPsIdBubbleNumFollowingInstrs - 1
    //      cfg.optForFmaxPsExFwdSize - 1
    //    )
    //  )
    //)

    val myPartialWriteTagInfoCond = (
      //down.isFiring
      up.isFiring
      //&& !myInFlushCond//shouldClearExtraDecodeInfo
    )

    val myLeftGprIdxVec = Vec.fill(
      //cfg.regFileCfg.modRdPortCnt
      cfg.maxNumGprsPerInstr
    )(
      UInt(log2Up(cfg.numGprs) bits)
    )
    //val myRightGprIdxVec = Vec.fill(
    //  //cfg.regFileCfg.modRdPortCnt
    //  cfg.maxNumGprsPerInstr
    //)(
    //  UInt(log2Up(cfg.numGprs) bits)
    //)

    for (
      //idx <- 0 until cfg.regFileCfg.modRdPortCnt
      idx <- 0 until cfg.maxNumGprsPerInstr
    ) {
      myLeftGprIdxVec(idx) := upPayload(1).gprIdxVec(idx)
      //myRightGprIdxVec(idx) := myScoreboardCommitStm.gprIdxVec(idx)
    }

    //case class MyGprTagInfo(
    //) extends Bundle {
    //  //val valid = Bool()
    //  //def fire = valid
    //  val opIsFwd = Bool()
    //  val nonFwdTag = (
    //    cloneOf(upPayload(1).instrCnt.scoreboardIssuePayload.nonFwdTag)
    //  )
    //  val fwdTag = (
    //    cloneOf(upPayload(1).instrCnt.scoreboardIssuePayload.fwdTag)
    //  )
    //  //val gprIdxVec = Vec.fill(
    //  //  cfg.maxNumGprsPerInstr
    //  //)(
    //  //  UInt(log2Up(cfg.numGprs) bits)
    //  //)

    //  val myGprIdx = UInt(log2Up(cfg.numGprs) bits)

    //}

    //val myGprTagInfoFifo = (
    //  StreamFifo(
    //    dataType=MyGprTagInfo(),
    //    depth=(
    //      8
    //      //16
    //    ),
    //    latency=(
    //      //0
    //      1
    //    ),
    //    forFMax=true,
    //  )
    //)
    val myInFlushCond = (
      shouldClearExtraDecodeInfo
      || (
        rScoreboardFlushState.asBits(1)
        //&& !up.isFiring
        //&& up.isValid
        //&& !myGprTagInfoFifo.io.pop.valid
      )
    )

    //myGprTagInfoFifo.io.flush := False
    //myGprTagInfoFifo.io.push.valid := False
    ////myGprTagInfoFifo.io.push.payload := (
    ////  myGprTagInfoFifo.io.push.payload.getZero
    ////)
    //myGprTagInfoFifo.io.push.opIsFwd := (
    //  //!myTempOpMayNeedHazardCheck
    //  !upPayload(1).splitOp.opIsMemAccess
    //)
    //myGprTagInfoFifo.io.push.nonFwdTag := (
    //  myTempNonFwdTag
    //)
    //myGprTagInfoFifo.io.push.fwdTag := (
    //  myTempFwdTag
    //)
    //myGprTagInfoFifo.io.push.myGprIdx := upPayload(1).gprIdxVec.last
    ////myGprTagInfoFifo.io.push.gprIdxVec := upPayload(1).gprIdxVec

    //myGprTagInfoFifo.io.pop.ready := False

    //when (
    //  myPartialWriteTagInfoCond
    //  && myTempOpMayNeedHazardCheck
    //) {
    //  myGprTagInfoFifo.io.push.valid := (
    //    //True
    //    (
    //      if (cfg.myHaveZeroReg) (
    //        upPayload(1).gprIdxVec.last.orR
    //      ) else (
    //        True
    //      )
    //    )
    //  )
    //}

    //when (
    //  //up.isFiring
    //  //&& myTempOpMayNeedHazardCheck
    //  //&& !myInFlushCond//shouldClearExtraDecodeInfo
    //  myPartialWriteTagInfoCond
    //  && myTempOpMayNeedHazardCheck
    //) {
    //  myGprTagInfoFifo.io.push.valid := (
    //    //True
    //    (
    //      if (cfg.myHaveZeroReg) (
    //        upPayload(1).gprIdxVec.last.orR
    //      ) else (
    //        True
    //      )
    //    )
    //  )
    //}

    //when (
    //  up.isFiring
    //  && !myInFlushCond//shouldClearExtraDecodeInfo
    //  && myGprTagInfoFifo.io.availability <= 2
    //) {
    //  myGprTagInfoFifo.io.pop.ready := True
    //}

    myScoreboardSavedGprTagVec := rMyNonFwdGprTagVec.asBits.asUInt

    val myNonFwdHazardCheckVec = Vec.fill(
      cfg.regFileCfg.modRdPortCnt
    )(
      Bool()
    )
    val myFwdHazardCheckVec = Vec.fill(
      cfg.regFileCfg.modRdPortCnt
    )(
      Bool()
    )
    for (jdx <- 0 until cfg.regFileCfg.modRdPortCnt) {
      //myMainHazardCheckVec(jdx) := (
      //  rMyNonFwdGprTagVec(
      //    upPayload(1).gprIdxVec(jdx)
      //  )
      //)
      switch (upPayload(1).gprIdxVec(jdx)) {
        for (idx <- 0 until cfg.numGprs) {
          is (idx) {
            myNonFwdHazardCheckVec(jdx) := (
              rMyNonFwdGprTagVec(idx)
              //|| rSavedGprInUseCntVec(idx).msb
            )
            myFwdHazardCheckVec(jdx) := (
              //rMyGprInUseCntVec(idx).msb
              rMyFwdGprTagVec(idx).fire
              && rMyFwdGprTagVec(idx).cnt.msb
            )
          }
        }
      }
    }

    //for (
    //  idx <- 0 until cfg.regFileCfg.modRdPortCnt
    //) {
    //}

    when (
      myScoreboardCommitStm.fire
      && myScoreboardCommitStm.myNonFwdValid
    ) {
      rMyNonFwdGprTagVec(
        myScoreboardCommitStm.gprIdxVec.last
      ) := (
        False
      )
    }
    when (
      myScoreboardBubbleRetireStm.fire
      //&& !myScoreboardBubbleRetireStm.opIsFwd
      && myScoreboardBubbleRetireStm.myNonFwdValid
    ) {
      rMyNonFwdGprTagVec(
        myScoreboardBubbleRetireStm.gprIdxVec.last
      ) := (
        False
      )
    }

    //val rSavedReorderBufIdxAbsDiff = (
    //  Reg(
    //    UInt(
    //      log2Up(myGprTagInfoFifo.depth + 1) + 1 bits
    //    )
    //  )
    //)

    //def myTempReorderBufIdx = (
    //  upPayload(1).instrCnt.scoreboardIssuePayload.reorderBufIdx
    //)

    //myTempReorderBufIdx := (
    //  RegNext(
    //    myTempReorderBufIdx,
    //    init=myTempReorderBufIdx.getZero
    //  )
    //)

    when (
      myPartialWriteTagInfoCond
      //&& myTempOpMayNeedHazardCheck
      //&& !myInFlushCond//shouldClearExtraDecodeInfo
      //&& up.isFiring
      && upPayload(1).splitOp.opIsMemAccess
    ) {
      rMyNonFwdGprTagVec(
        upPayload(1).gprIdxVec.last
      ) := (
        //True
        if (cfg.myHaveZeroReg) (
          upPayload(1).gprIdxVec.last.orR //=/= 0x0
        ) else (
          True
        )
      )
    }

    //val rMostRecentIncrWasFlushEnd = Reg(Bool(), init=False)

    //when (
    //  up.isFiring
    //  //down.isFiring
    //  //&& upPayload(1).inpDecodeExt.head.opIsMemAccess.last
    //  && upPayload(1).splitOp.opIsMemAccess
    //) {
    //  myTempNonFwdTag := (
    //    RegNext(
    //      myTempNonFwdTag,
    //      init=myTempNonFwdTag.getZero
    //    ) + 1
    //  )
    //}

//// >>> for x in range(8):
//// ...     print(x, bin(x), bin(x ^ 0x7), bin(Bitscan(x ^ 0x7)))
//// ...     
//// 0 0b0 0b111 0b1
//// 1 0b1 0b110 0b10
//// 2 0b10 0b101 0b1
//// 3 0b11 0b100 0b100
//// 4 0b100 0b11 0b1
//// 5 0b101 0b10 0b10
//// 6 0b110 0b1 0b1
//// 7 0b111 0b0 0b0
//
// >>> for idx in range(size):
// ...     print(idx, ("-" * (size - idx - 1) + "1" + ("0" * idx)))
// ...     
// 0 ---1
// 1 --10
// 2 -100
// 3 1000

    //val myFwdTagAllocVec = (
    //  Vec(rMyFwdGprTagVec.map(item => item.fire))
    //)

    val rNonFwdTagAllocVec = (
      Vec.fill(1 << myTempNonFwdTag.getWidth)(
        Reg(Bool(), init=False)
      )
    )
    val rFwdTagAllocVec = (
      Vec.fill(1 << myTempFwdTag.getWidth)(
        Reg(Bool(), init=False)
      )
    )

    val myReducedNonFwdTagAllocVec = (
      Vec.fill(rNonFwdTagAllocVec.size - 1)(
        Bool()
      )
    )
    val myReducedFwdTagAllocVec = (
      Vec.fill(rFwdTagAllocVec.size - 1)(
        Bool()
      )
    )

    for (idx <- 0 until (1 << myTempNonFwdTag.getWidth) - 1) {
      myReducedNonFwdTagAllocVec(idx) := rNonFwdTagAllocVec(idx + 1)
      myReducedFwdTagAllocVec(idx) := rFwdTagAllocVec(idx + 1)
    }

    switch (
      //io.issue.ready
      (
        (
          up.isFiring
          //|| (
          //  down.isFiring
          //  && !myInFlushCond
          //)
        )
        //&& !upPayload(1).inpDecodeExt.head.opIsMemAccess.last
        && upPayload(1).splitOp.opIsMemAccess
        //&& !myNonFwdHazardCheckVec.orR
        //&& !myInFlushCond
      )
      ## Bitscan(
        //~rNonFwdTagAllocVec.asBits.asUInt
        ~myReducedNonFwdTagAllocVec.asBits.asUInt
      )
    ) {
      val size = myReducedNonFwdTagAllocVec.size
      for (idx <- 0 until size) {
        is (MaskedLiteral(
          "1"
          + ("-" * (size - idx - 1) + "1" + ("0" * idx))
        )) {
          // fast-ish (regarding fmax) search to implement the free list
          // search
          myTempNonFwdTag := idx + 1
          rNonFwdTagAllocVec(idx + 1) := True
        }
      }
      default {
      }
    }


    switch (
      //io.issue.ready
      (
        //up.isFiring
        //down.isFiring
        (
          up.isFiring
          //|| (
          //  down.isFiring
          //  && myInFlushCond
          //)
        )
        //&& !upPayload(1).inpDecodeExt.head.opIsMemAccess.last
        && !upPayload(1).splitOp.opIsMemAccess
        //&& !myFwdHazardCheckVec.orR
      )
      ## Bitscan(
        //~rFwdTagAllocVec.asBits.asUInt
        ~myReducedFwdTagAllocVec.asBits.asUInt
      )
    ) {
      val size = myReducedFwdTagAllocVec.size
      for (idx <- 0 until size) {
        is (MaskedLiteral(
          "1"
          + ("-" * (size - idx - 1) + "1" + ("0" * idx))
        )) {
          // fast-ish (regarding fmax) search to implement the free list
          // search
          myTempFwdTag := idx + 1
          rFwdTagAllocVec(idx + 1) := True
        }
      }
      default {
      }
    }

    //when (
    //  rFwdTagAllocVec.asBits.andR
    //) {
    //  doSendBubbleMainMost(
    //    myPsIdBubble=Some(
    //      //!myInFlushCond//shouldClearExtraDecodeInfo
    //      //True
    //      //False
    //      myNonFwdHazardCheckVec.orR
    //      //&& !myInFlushCond//shouldClearExtraDecodeInfo
    //    ),
    //    myPsIdOtherBubble=Some(
    //      True
    //    ),
    //    myPsIdFwdBubble=Some(
    //      myFwdHazardCheckVec.orR
    //      //&& !myInFlushCond//shouldClearExtraDecodeInfo
    //    ),
    //    //myUpdateGprIsOrIsntZero=false,
    //  )
    //}

    //when (
    //  up.isFiring
    //  //down.isFiring
    //  //&& !upPayload(1).inpDecodeExt.head.opIsMemAccess.last
    //  && !upPayload(1).splitOp.opIsMemAccess
    //) {
    //  myTempFwdTag := (
    //    RegNext(
    //      myTempFwdTag,
    //      init=myTempFwdTag.getZero
    //    ) + 1
    //  )
    //}


    //when (
    //  //RegNext(
    //  //  (
    //  //    up.isFiring
    //  //    //&& !rose(rScoreboardFlushState.asBits(0))
    //  //  ),
    //  //  init=False
    //  //)

    //  up.isFiring
    //  //&& !rMostRecentIncrWasFlushEnd
    //  //&& !rose(rScoreboardFlushState.asBits(0))
    //  //||
    //  //(
    //  //  down.isFiring
    //  //  && myNonFwdHazardCheckVec.orR
    //  //  && !myInFlushCond//shouldClearExtraDecodeInfo
    //  //)
    //  //|| 
    //  //(
    //  //  down.isFiring
    //  //)
    //) {
    //  myTempReorderBufIdx := (
    //    RegNext(
    //      myTempReorderBufIdx,
    //      init=myTempReorderBufIdx.getZero
    //    )
    //    + 1
    //  )
    //}

    when (
      (
        (
          (
            myNonFwdHazardCheckVec.orR
            || myFwdHazardCheckVec.orR
          )
          //&& (
          //  //!myInFlushCond//shouldClearExtraDecodeInfo
          //  !myInFlushCond
          //)
        )
        || myReducedFwdTagAllocVec.asBits.andR
        || myReducedNonFwdTagAllocVec.asBits.andR
      )
      //&& (
      //  //!myInFlushCond//shouldClearExtraDecodeInfo
      //  !myInFlushCond
      //)
      || myInFlushCond
    ) {
      doSendBubbleMainMost(
        myPsIdBubble=Some(
          //!myInFlushCond//shouldClearExtraDecodeInfo
          //True
          //False
          myNonFwdHazardCheckVec.orR
          //&& !myInFlushCond
          //&& !myInFlushCond//shouldClearExtraDecodeInfo
        ),
        myPsIdOtherBubble=Some(
          True
          //!myInFlushCond
        ),
        myPsIdFwdBubble=Some(
          myFwdHazardCheckVec.orR
          //&& !myInFlushCond
          //&& !myInFlushCond//shouldClearExtraDecodeInfo
        ),
        //myUpdateGprIsOrIsntZero=false,
        myInFlushCond=Some(myInFlushCond)
      )
    }

    for (idx <- 0 until cfg.numGprs) {
      when (
        //myPartialWriteTagInfoCond
        //up.isFiring
        down.isFiring
        //&& !myNonFwdHazardCheckVec.orR
        //&& !myInFlushCond//shouldClearExtraDecodeInfo
        && rMyFwdGprTagVec(idx).fire
        && !rMyFwdGprTagVec(idx).cnt.msb
        //&& myScoreboardCommitStm.fire
        //&& (
        //  myScoreboardCommitStm.fwdTag
        //  === 
        //)
      ) {
        rMyFwdGprTagVec(idx).cnt := (
          rMyFwdGprTagVec(idx).cnt - 1
        )
      }
      when (
        rMyFwdGprTagVec(idx).fire
        && (
          (
            myScoreboardCommitStm.fire
            && myScoreboardCommitStm.opIsFwd
            && myScoreboardCommitStm.myFwdValid
            && (
              rMyFwdGprTagVec(idx).tag
              === myScoreboardCommitStm.fwdTag
            )
            //&& (
            //  myScoreboardCommitStm
            //)
          )
          || (
            myScoreboardBubbleRetireStm.fire
            && myScoreboardBubbleRetireStm.opIsFwd
            && (
              rMyFwdGprTagVec(idx).tag
              === myScoreboardBubbleRetireStm.fwdTag
            )
          )
        )
      ) {
        //rFwdTagAllocVec(myScoreboardCommitStm.fwdTag) := False
        rMyFwdGprTagVec(idx).valid := False
      }
    }
    //--------
    switch (
      (
        myScoreboardCommitStm.fire
        && myScoreboardCommitStm.opIsFwd
      )
      ## (
        myScoreboardCommitStm.fwdTag
      )
    ) {
      for (
        idx <- 0 until (1 << myScoreboardCommitStm.fwdTag.getWidth)
      ) {
        is (
          (1 << myScoreboardCommitStm.fwdTag.getWidth)
          | idx
        ) {
          // needed because the tag stored in `rMyFwdGprTagVec` gets
          // overwritten sometimes by a "can-be-forwarded-from"
          // instruction writing to the same register!
          rFwdTagAllocVec(idx) := False
        }
      }
      default {
      }
    }
    switch (
      (
        myScoreboardBubbleRetireStm.fire
        && myScoreboardBubbleRetireStm.opIsFwd
      )
      ## (
        myScoreboardBubbleRetireStm.fwdTag
      )
    ) {
      for (
        idx <- 0
        until (1 << myScoreboardBubbleRetireStm.fwdTag.getWidth)
      ) {
        is (
          (1 << myScoreboardBubbleRetireStm.fwdTag.getWidth)
          | idx
        ) {
          // Bubbles being retired means we need to clear our tag
          // allocations used for those bubbles!
          rFwdTagAllocVec(idx) := False
        }
      }
      default {
      }
    }
    //--------

    switch (
      (
        myScoreboardCommitStm.fire
        && myScoreboardCommitStm.opIsFwd
      )
      ## (
        myScoreboardCommitStm.nonFwdTag
      )
    ) {
      for (
        idx <- 0 until (1 << myScoreboardCommitStm.nonFwdTag.getWidth)
      ) {
        is (
          (1 << myScoreboardCommitStm.nonFwdTag.getWidth)
          | idx
        ) {
          rNonFwdTagAllocVec(idx) := False
        }
      }
      default {
      }
    }
    switch (
      (
        myScoreboardBubbleRetireStm.fire
        && myScoreboardBubbleRetireStm.opIsFwd
      )
      ## (
        myScoreboardBubbleRetireStm.nonFwdTag
      )
    ) {
      for (
        idx <- 0
        until (1 << myScoreboardBubbleRetireStm.nonFwdTag.getWidth)
      ) {
        is (
          (1 << myScoreboardBubbleRetireStm.nonFwdTag.getWidth)
          | idx
        ) {
          // Bubbles being retired means we need to clear our tag
          // allocations used for those bubbles!
          rNonFwdTagAllocVec(idx) := False
        }
      }
      default {
      }
    }
    //--------

    //when (
    //  myScoreboardCommitStm.fire
    //  && myScorebor
    //) {
    //}

    switch (
      (
        //myPartialWriteTagInfoCond
        up.isFiring
        //down.isFiring
        //&& !myInFlushCond//shouldClearExtraDecodeInfo
        && !myNonFwdHazardCheckVec.orR
        //&& !myTempOpMayNeedHazardCheck
        //&& !upPayload(1).inpDecodeExt.head.opIsMemAccess.last
        && !upPayload(1).splitOp.opIsMemAccess
      )
      ## myLeftGprIdxVec.last
    ) {
      for (idx <- 0 until cfg.numGprs) {
        if (
          !cfg.myHaveZeroReg
          || idx != 0
        ) {
          is (
            (1 << log2Up(cfg.numGprs))
            | idx
          ) {
            when (
              !rMyFwdGprTagVec(idx).fire
            ) {
              rMyFwdGprTagVec(idx).valid := True
              rMyFwdGprTagVec(idx).cnt := (
                cfg.optForFmaxPsExFwdSize - 2//1
              )
              //rMyFwdGprTagVec(idx).tag := myTempFwdTag
            }
            rMyFwdGprTagVec(idx).tag := myTempFwdTag
          }
        }
      }
      if (cfg.myHaveZeroReg) {
        default {
        }
      }
    }
    //switch (rScoreboardFlushState) {
    //  is (ScoreboardFlushState.IDLE) {
    //    when (
    //      myPartialWriteTagInfoCond
    //      //&& myTempOpMayNeedHazardCheck
    //      && !shouldClearExtraDecodeInfo
    //      && upPayload(1).splitOp.opIsMemAccess
    //    ) {
    //      myGprTagInfoFifo.io.push.valid := (
    //        //True
    //        (
    //          if (cfg.myHaveZeroReg) (
    //            upPayload(1).gprIdxVec.last.orR
    //          ) else (
    //            True
    //          )
    //        )
    //      )
    //    }
    //    when (
    //      up.isFiring
    //      && !shouldClearExtraDecodeInfo//myInFlushCond
    //      && myGprTagInfoFifo.io.availability <= 2
    //    ) {
    //      myGprTagInfoFifo.io.pop.ready := True
    //    }

    //    when (
    //      shouldClearExtraDecodeInfo
    //    ) {
    //      rScoreboardFlushState := ScoreboardFlushState.FLUSH
    //      //myTempReorderBufIdx := psExSetPc.reorderBufIdx //- 1
    //    }
    //    //when (
    //    //  up.isFiring
    //    //) {
    //    //  rMostRecentIncrWasFlushEnd := False
    //    //}
    //  }
    //  is (ScoreboardFlushState.FLUSH) {
    //    //myTempReorderBufIdx := (
    //    //  RegNext(
    //    //    myTempReorderBufIdx
    //    //  )
    //    //)
    //    when (
    //      !shouldClearExtraDecodeInfo
    //      && myGprTagInfoFifo.io.pop.valid
    //    ) {
    //      //myTempReorderBufIdx := (
    //      //  RegNext(
    //      //    myTempReorderBufIdx
    //      //  )
    //      //)
    //      doSendBubbleMainMost(
    //        myPsIdBubble=Some(
    //          //!myInFlushCond//shouldClearExtraDecodeInfo
    //          //True
    //          //False
    //          //myNonFwdHazardCheckVec.orR
    //          //&& !myInFlushCond//shouldClearExtraDecodeInfo
    //          //True
    //          //False
    //          True
    //        ),
    //        myPsIdOtherBubble=Some(
    //          //True
    //          False
    //        ),
    //        myPsIdFwdBubble=Some(
    //          False
    //          //True
    //          //False
    //          //myFwdHazardCheckVec.orR
    //          //&& !myInFlushCond//shouldClearExtraDecodeInfo
    //        ),
    //      )
    //    }

    //    when (
    //      //up.isFiring
    //      //&& 
    //      !shouldClearExtraDecodeInfo
    //      && !myGprTagInfoFifo.io.pop.valid
    //    ) {
    //      //rMostRecentIncrWasFlushEnd := True

    //      //when (
    //      //  up.isFiring
    //      //) {
    //      //  myTempReorderBufIdx := (
    //      //    RegNext(
    //      //      myTempReorderBufIdx
    //      //    ) + 1
    //      //  )
    //      //}

    //      //doSendBubbleMainMost(
    //      //  myPsIdBubble=Some(
    //      //    //!myInFlushCond//shouldClearExtraDecodeInfo
    //      //    //True
    //      //    //False
    //      //    //myNonFwdHazardCheckVec.orR
    //      //    //&& !myInFlushCond//shouldClearExtraDecodeInfo
    //      //    //True
    //      //    //False
    //      //    True
    //      //  ),
    //      //  myPsIdOtherBubble=Some(
    //      //    //True
    //      //    False
    //      //  ),
    //      //  myPsIdFwdBubble=Some(
    //      //    False
    //      //    //True
    //      //    //False
    //      //    //myFwdHazardCheckVec.orR
    //      //    //&& !myInFlushCond//shouldClearExtraDecodeInfo
    //      //  ),
    //      //)
    //      rScoreboardFlushState := ScoreboardFlushState.IDLE
    //    }

    //    myGprTagInfoFifo.io.pop.ready := True

    //    //switch (
    //    //  (
    //    //    myGprTagInfoFifo.io.pop.valid 
    //    //    && myGprTagInfoFifo.io.pop.opIsFwd
    //    //  )
    //    //  ## myGprTagInfoFifo.io.pop.myGprIdx
    //    //) {
    //    //  for (idx <- 0 until cfg.numGprs) {
    //    //    is (
    //    //      (1 << log2Up(cfg.numGprs))
    //    //      | idx
    //    //    ) {
    //    //      rFwdTagAllocVec(myGprTagInfoFifo.io.pop.fwdTag) := False
    //    //      rMyFwdGprTagVec(idx).valid := False

    //    //    }
    //    //  }
    //    //}
    //    //switch (
    //    //  (
    //    //    myGprTagInfoFifo.io.pop.valid 
    //    //    && !myGprTagInfoFifo.io.pop.opIsFwd
    //    //  )
    //    //  ## myGprTagInfoFifo.io.pop.myGprIdx
    //    //) {
    //    //  for (idx <- 0 until cfg.numGprs) {
    //    //    is (
    //    //      (1 << log2Up(cfg.numGprs))
    //    //      | idx
    //    //    ) {
    //    //      rMyNonFwdGprTagVec(idx) := False
    //    //    }
    //    //  }
    //    //}
    //  }
    //}

    //when (
    //  myInFlushCond//shouldClearExtraDecodeInfo
    //) {
    //  rMyFwdGprTagVec.foreach(item => {
    //    item.valid := False
    //  })
    //}

    //when (
    //  !myScoreboardReorderBufPsIdCanIssue
    //) {
    //  //cId.haltIt()
    //  //when (
    //  //  down.isFiring
    //  //) {
    //  //  myTempReorderBufIdx := (
    //  //    RegNext(
    //  //      myTempReorderBufIdx,
    //  //      init=myTempReorderBufIdx.getZero
    //  //    )
    //  //    + 1
    //  //  )
    //  //}
    //  doSendBubbleMainMost(
    //    myPsIdBubble=(
    //      //Some(True)
    //      None
    //    ),
    //    myPsIdOtherBubble=(
    //      None
    //      //Some(
    //      //  True
    //      //)
    //    ),
    //    myPsIdFwdBubble=(
    //      None
    //      //Some(
    //      //  True
    //      //)
    //    )
    //  )
    //}


    down(pId).splitOp.scoreboardOpIsMemAccess := (
      upPayload(1).splitOp.opIsMemAccess
    )
  })
  if (cfg.optScoreboard) {
    upPayload(1).instrCnt.myScoreboardOpMayNeedHazardCheck := (
      myTempOpMayNeedHazardCheck
    )
  }
}

object Bitscan {

// >>> for x in range(8):
// ...     print(x, bin(x), bin(Bitscan(x)))
// ...     
// 0 0b0 0b0
// 1 0b1 0b1
// 2 0b10 0b10
// 3 0b11 0b1
// 4 0b100 0b100
// 5 0b101 0b1
// 6 0b110 0b10
// 7 0b111 0b1

// >>> for x in range(8):
// ...     print(x, bin(x), bin(x ^ 0x7), bin(Bitscan(x ^ 0x7)))
// ...     
// 0 0b0 0b111 0b1
// 1 0b1 0b110 0b10
// 2 0b10 0b101 0b1
// 3 0b11 0b100 0b100
// 4 0b100 0b11 0b1
// 5 0b101 0b10 0b10
// 6 0b110 0b1 0b1
// 7 0b111 0b0 0b0

// >>> for idx in range(size):
// ...     print(idx, ("-" * (size - idx - 1) + "1" + ("0" * idx)))
// ...     
// 0 ---1
// 1 --10
// 2 -100
// 3 1000


  def apply(
    x: UInt
  ): UInt = (
    x & ~(x - 1)
  )
}
case class SnowHousePipeStagePreFwd(
  cfg: SnowHouseConfig,
  outp: SnowHousePipePayload,
  inp: SnowHousePipePayload,
  //link: CtrlLink,
  upIsValid: Bool,
  upIsFiring: Bool,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
  //myBranchMispredictEtc: Bool,
  forFmaxRegFileWrPulseArr: Seq[
    Flow[
      PipeSimpleDualPortMemDrivePayload[
        UInt
      ]
    ]
  ],
) extends Area {
  //val up = link.up
  //val down = link.down
  outp.allowOverride

  def myRegPcRange = (
    outp.regPc.high downto log2Up(cfg.instrSizeBytes)
  )
  val myHistRegPc = (
    History[SInt](
      that=outp.regPc(myRegPcRange).asSInt,
      length=outp.myHistRegPcSize,
      when=(
        //up.isFiring
        upIsFiring,
      ),
      init=outp.regPc(myRegPcRange).asSInt.getZero,
    )
  )
  val myDspRegPcMinus2InstrSize = {
    val myWordWidth = (
      //cfg.mainWidth - log2Up(cfg.instrSizeBytes) //- 1
      cfg.mainAddrWidth - log2Up(cfg.instrSizeBytes) //- 1
    )
    new Area {
      val wordWidth = myWordWidth
      val io = (
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
      val tempCarry = Cat(
        U(s"${wordWidth - 1}'d0"), 
        io.inp.carry
      ).asSInt

      val myTempSumCarry = (
        Cat(
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
    Vec.fill(outp.myHistRegPcSize - 1)(
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
  myDspRegPcMinus2InstrSize.io.inp.cond := upIsFiring//up.isFiring
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
      when (RegNext(
        //next=up.isFiring,
        upIsFiring,
        init=False
      )) {
        myHistRegPcMinus2InstrSize(idx) := (
          RegNext(
            next=myHistRegPcMinus2InstrSize(idx - 1),
            init=myHistRegPcMinus2InstrSize(idx - 1).getZero,
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
  outp.laggingRegPcPlus1InstrSize := (
    //laggingRegPcMinus2InstrSize.asUInt
    outp.laggingRegPc + cfg.instrSizeBytes
  )
  outp.regPcPlusImm := 0x0
  outp.regPcPlusImm.allowOverride
  if (cfg.optShiftRegPcImmAddend) {
    outp.regPcPlusImm(myRegPcRange) := (
      (
        //laggingRegPcMinus2InstrSize//.asSInt
        //+ (if (!cfg.useLcvInstrBus) (0) else (1))
        outp.laggingRegPcPlus1InstrSize(myRegPcRange).asSInt
        + (
          outp.imm(2).asSInt
        )
      ).asUInt.resize(outp.regPcPlusImm(myRegPcRange).getWidth)
      //- (cfg.instrMainWidth.toLong / 8.toLong)
    )
  } else {
    outp.regPcPlusImm := (
      (
        //outp.laggingRegPcPlus1InstrSize.asSInt
        outp.laggingRegPc.asSInt
        + (
          outp.imm(2).asSInt
        )
      ).asUInt.resize(outp.regPcPlusImm.getWidth)
    )
  }
  outp.branchTgtBufElem(1).srcRegPc := outp.laggingRegPc

  //outp.branchPredictReplaceBtbElemMost := (
  //  //outp.branchPredictTkn
  //  ////RegNextWhen(
  //  ////  next=outp.branchPredictTkn,
  //  ////  cond=link.up.isFiring,
  //  ////  init=outp.branchPredictTkn.getZero,
  //  ////)
  //  ////&& upPayload(0).branchTgtBufElem(0).fire

  //  //&& 
  //  outp.branchTgtBufElem(0).fire
  //  && outp.branchTgtBufElem(1).fire
  //  && !outp.branchTgtBufElem(1).dontPredict

  //  //&& outp.btbElemBranchKind(1).asBits(1)
  //  && (
  //    !LcvFastCmpEq(
  //      left=outp.branchTgtBufElem(0).srcRegPc(cfg.mySrcRegPcCmpEqRange),
  //      right=outp.branchTgtBufElem(1).srcRegPc(cfg.mySrcRegPcCmpEqRange),
  //      cmpEqIo=null,
  //      optDsp=false,
  //      optReg=false,
  //    )._1
  //    //(
  //    //  outp.branchTgtBufElem(0).srcRegPc(
  //    //    cfg.mySrcRegPcCmpEqRange
  //    //  ) =/= outp.branchTgtBufElem(1).srcRegPc(
  //    //    cfg.mySrcRegPcCmpEqRange
  //    //  )
  //    //)
  //    //|| (
  //    //  outp.branchTgtBufElem(0).dstRegPc
  //    //  =/= outp.branchTgtBufElem(1).srcRegPc
  //    //)
  //  )
  //)
  
  if (cfg.irqCfg != None) {
    outp.takeIrq := False
  }
  outp.irqIraRegPc.head := (
    //outp.laggingRegPc
    Cat(
      (
        outp.laggingRegPc(
          outp.laggingRegPc.high
          downto log2Up(cfg.instrSizeBytes)
        )
      ),
      U(s"${log2Up(cfg.instrSizeBytes)}'d0"),
    ).asUInt
  )
  if (!cfg.useLcvDataBus) {
    //outp.irqIraRegPc.last := (
    //  outp.laggingRegPc
    //)
  } else {
    //for (idx <- 0 until outp.irqIraRegPc
    //outp.irqIraRegPc.head := (
    //  outp.laggingRegPc
    //)
    outp.irqIraRegPc.last := (
      //outp.laggingRegPc + cfg.instrSizeBytes
      Cat(
        (
          outp.laggingRegPc(
            outp.laggingRegPc.high
            downto log2Up(cfg.instrSizeBytes)
          ) - 1 //+ 1
        ),
        U(s"${log2Up(cfg.instrSizeBytes)}'d0"),
      ).asUInt
    )
  }

  case class MyFwdInfo(
  ) extends Bundle {
    val valid = Bool()
    //val data = UInt(cfg.mainWidth bits)
    val addr = UInt(log2Up(cfg.regFileCfg.wordCountArr(0)) bits)
    //val isLoadEtc = Bool() // TODO: atomics that read from the bus/mem
    //val memAccessForceToZero = Bool()
    val branchMispredictEtcForceToZero = Bool()
    //val anyForceToZero = Bool()
    //val instrResultInPsWb = Bool()
    //val myFwdIdx = UInt(log2Up(cfg.optForFmaxPsExFwdSize) bits)
  }

  val myForFmaxFwdArea = (
    cfg.optForFmax
  ) generate (new Area {
    val rMyPsExSetPcState = (
      Reg(Bool(), init=False)
    )
    //val rMySavedPsExSetPcReorderBufIdx = (
    //  cfg.optScoreboard
    //) generate (
    //  Reg(
    //    cloneOf(psExSetPc.reorderBufIdx),
    //    //init=psExSetPc.reorderBufIdx.getZero
    //  )
    //)

    //val myTempReorderBufIdx = (
    //  cfg.optScoreboard
    //) generate (
    //  outp.instrCnt.scoreboardIssuePayload.reorderBufIdx
    //)

    //if (cfg.optScoreboard) {
    //  myTempReorderBufIdx := (
    //    RegNext(
    //      myTempReorderBufIdx,
    //      init=myTempReorderBufIdx.getZero
    //    )
    //  )
    //}


    when (!rMyPsExSetPcState) {
      when (psExSetPc.fire) {
        //if (cfg.optScoreboard) {
        //  //rMySavedPsExSetPcReorderBufIdx := psExSetPc.reorderBufIdx
        //  myTempReorderBufIdx := psExSetPc.reorderBufIdx
        //}
        rMyPsExSetPcState := True
      } otherwise {
        //if (cfg.optScoreboard) {
        //  when (
        //    upIsFiring
        //    && !outp.instrCnt.myScoreboardPsWbBubbleMost(0)
        //  ) {
        //    myTempReorderBufIdx := (
        //      RegNext(
        //        myTempReorderBufIdx,
        //        init=myTempReorderBufIdx.getZero
        //      ) + 1
        //    )
        //  }
        //}
      }
    } otherwise {
      when (
        upIsFiring
        && outp.regPcSetItCnt(0).lsb
      ) {
        rMyPsExSetPcState := False

        //if (cfg.optScoreboard) {
        //  myTempReorderBufIdx := (
        //    RegNext(
        //      myTempReorderBufIdx,
        //      init=myTempReorderBufIdx.getZero
        //    ) + 1
        //  )
        //}
      }
      //if (cfg.optScoreboard) {
      //  when (
      //    upIsFiring
      //    && outp.regPcSetItCnt(0).lsb
      //    && !outp.instrCnt.myScoreboardPsWbBubbleMost(1)
      //  ) {
      //    myTempReorderBufIdx := (
      //      RegNext(
      //        myTempReorderBufIdx,
      //        init=myTempReorderBufIdx.getZero
      //      ) + 1
      //    )
      //  }
      //}
    }

    //val rSavedMemAccVec = (
    //  //Vec.fill(cfg.optForFmaxPsExFwdSize)(
    //    Vec.fill(cfg.numGprs)(
    //      Reg(Bool(), init=False)
    //    )
    //    //Reg(UInt(cfg.numGprs bits))
    //    //init(0x0)
    //  //)
    //)
    //for (idx <- 0 until cfg.numGprs) {
    //  when (
    //    outp.gprIsNonZeroVec.last.last
    //    //&& outp.calcForFmaxFwdValidMost(
    //    //  someShouldIgnoreInstr=(
    //    //    !rMyPsExSetPcState
    //    //    //&& !psExSetPc.fire
    //    //  ),
    //    //  someNodeIsFiring=upIsFiring,
    //    //  inPsEx=false
    //    //)
    //    && (
    //      !rMyPsExSetPcState
    //      || outp.regPcSetItCnt(1).lsb
    //    )
    //    && !outp.instrCnt.myPsIdBubble.head
    //    && outp.gprIdxVec.last === idx
    //    && upIsFiring
    //  ) {
    //    // TODO: might need to use a `History` here, with one bit per
    //    // register, but not sure since the pipeline is filled with
    //    // bubbles when 
    //    rSavedMemAccVec(idx) := (
    //      outp.splitOp.opIsMemAccess
    //    )
    //  }
    //}


    val rSavedMostRecentGprWriteWasMemAccess = {
      val temp = Reg(UInt(cfg.numGprs bits))
      temp.init(temp.getZero)
      temp
    }
    //val rSavedMostRecentGprWriteWasPsWbWrPulse = {
    //  val temp = Reg(UInt(cfg.numGprs bits))
    //  temp.init(temp.getZero)
    //  temp
    //}
    val myTempSaveOutpCondMost = (
      upIsFiring
      && !outp.instrCnt.myPsIdBubble.last
    )
    val myTempSaveOutpCond = (
      myTempSaveOutpCondMost
      && (
        !outp.inpDecodeExt.last.opIsMemAccess.head
        //|| outp.instrCnt.myPsIdFwdBubble.head
        && !outp.instrCnt.myPsIdFwdBubble.head
      )
    )

    when (
      //upIsFiring
      //&& !outp.instrCnt.myPsIdBubble.head
      myTempSaveOutpCondMost
      && outp.gprIsNonZeroVec.last.last
      && (
        (
          !psExSetPc.fire
          && !rMyPsExSetPcState
        )
        || outp.regPcSetItCnt(1).lsb
      )
    ) {
      rSavedMostRecentGprWriteWasMemAccess(
        outp.gprIdxVec.last
      ) := (
        outp.splitOp.opIsMemAccess
      )
    }

    //val stickyFwdRegFileWrPulseVec = (
    //  Vec.fill(cfg.regFileCfg.modRdPortCnt)(
    //    Flow(cloneOf(forFmaxRegFileWrPulseArr(0).data))
    //  )
    //)
    //stickyFwdRegFileWrPulseVec := (
    //  RegNext(
    //    stickyFwdRegFileWrPulseVec,
    //    init=stickyFwdRegFileWrPulseVec.getZero
    //  )
    //)

    //for (idx <- 0 until cfg.regFileCfg.modRdPortCnt) {
    //  //when (
    //  //  RegNext(
    //  //    //upIsFiring,//cLink.up.isFiring,
    //  //    (
    //  //      myTempSaveOutpCond
    //  //      //&& (
    //  //      //  (
    //  //      //    !psExSetPc.fire
    //  //      //    && !rMyPsExSetPcState
    //  //      //  )
    //  //      //  || outp.regPcSetItCnt(1).lsb
    //  //      //)
    //  //    ),
    //  //    init=False
    //  //  )
    //  //  && (
    //  //    (
    //  //      !psExSetPc.fire
    //  //      && !rMyPsExSetPcState
    //  //    )
    //  //    || outp.regPcSetItCnt(1).lsb
    //  //  )
    //  //) {
    //  //  stickyFwdRegFileWrPulseVec(idx).valid := False
    //  //}

    //  //when (
    //  //  forFmaxRegFileWrPulseArr(0).fire
    //  //  && (
    //  //    forFmaxRegFileWrPulseArr(0).addr 
    //  //    === outp.gprIdxVec(idx)
    //  //  )
    //  //) {
    //  //  stickyFwdRegFileWrPulseVec(idx).valid := True
    //  //  stickyFwdRegFileWrPulseVec(idx).payload := (
    //  //    forFmaxRegFileWrPulseArr(0).data
    //  //  )
    //  //}

    //  switch (
    //    (
    //      forFmaxRegFileWrPulseArr(0).fire
    //      && (
    //        forFmaxRegFileWrPulseArr(0).addr
    //        === outp.gprIdxVec(idx)
    //      )
    //    )
    //    //stickyFwdRegFileWrPulseVec(idx).fire
    //    ## (
    //      RegNext(
    //        //cLink.up.isFiring,
    //        upIsFiring,
    //        init=False
    //      )
    //      || rose(
    //        //cLink.up.isValid
    //        upIsValid
    //      )
    //    )
    //  ) {
    //    is (M"1-") {
    //      outp.myExt(0).rdMemWord(idx) := (
    //        forFmaxRegFileWrPulseArr(0).data
    //        //stickyFwdRegFileWrPulseVec(idx).payload
    //      )
    //    }
    //    is (M"01") {
    //      outp.myExt(0).rdMemWord(idx) := (
    //        inp.myExt(0).rdMemWord(idx)
    //      )
    //    }
    //    default {
    //      outp.myExt(0).rdMemWord(idx) := (
    //        RegNext(
    //          outp.myExt(0).rdMemWord(idx),
    //          init=outp.myExt(0).rdMemWord(idx).getZero
    //        )
    //      )
    //    }
    //  }
    //}

    //when (
    //  //myWrPulse
    //  forFmaxRegFileWrPulseArr(0).fire
    //) {
    //}


    //val stickyRegFileWrPulse = (
    //  //Vec.fill(cfg.regFileCfg.modRdPortCnt)(
    //    cloneOf(forFmaxRegFileWrPulseArr(0))
    //  //)
    //)
    //stickyRegFileWrPulse := (
    //  RegNext(
    //    stickyRegFileWrPulse,
    //    init=stickyRegFileWrPulse.getZero
    //  )
    //)
    //when (
    //  forFmaxRegFileWrPulseArr(0).fire
    //  //&& (
    //  //  forFmaxRegFileWrPulseArr(0).addr 
    //  //  === outp.gprIdxVec(idx)
    //  //)
    //) {
    //  stickyRegFileWrPulse := forFmaxRegFileWrPulseArr(0)
    //}

    //for (idx <- 0 until cfg.regFileCfg.modRdPortCnt) {
    //  //when (
    //  //  forFmaxRegFileWrPulseArr(0).fire
    //  //  //&& (
    //  //  //  forFmaxRegFileWrPulseArr(0).addr 
    //  //  //  === outp.gprIdxVec(idx)
    //  //  //)
    //  //) {
    //  //  stickyRegFileWrPulse(idx) := forFmaxRegFileWrPulseArr(0)
    //  //}

    //  switch (
    //    (
    //      stickyRegFileWrPulse.fire
    //      && (
    //        stickyRegFileWrPulse.addr === outp.gprIdxVec(idx)
    //      )
    //    )
    //    ## (
    //      RegNext(upIsFiring, init=False)
    //      || rose(upIsValid)
    //    )
    //  ) {
    //    is (M"1-") {
    //      outp.myExt(0).rdMemWord(idx) := stickyRegFileWrPulse.data
    //    }
    //    is (M"01") {
    //      outp.myExt(0).rdMemWord(idx) := inp.myExt(0).rdMemWord(idx)
    //    }
    //    default {
    //      outp.myExt(0).rdMemWord(idx) := (
    //        RegNext(
    //          outp.myExt(0).rdMemWord(idx),
    //          init=outp.myExt(0).rdMemWord(idx).getZero,
    //        )
    //      )
    //    }
    //  }
    //}

    val myHistFwdInfo = {
      val temp = MyFwdInfo()
      temp.valid := (
        //outp.myExt(0).modMemWordValid.last //ram.io.wrEn
        //&& outp.gprIsNonZeroVec.last.last
        //&& !myShouldIgnoreInstr(0)

        (
          outp.gprIsNonZeroVec.last.last
          //|| (
          //  outp.splitOp.opIsMemAccess
          //  && outp.inpDecodeExt.last.memAccessKind.asBits(1)
          //)
        )
        //&& !outp.splitOp.opIsMemAccess
        //&& !outp.instrCnt.myPsIdBubble.last

        && 
        (
          (
            !psExSetPc.fire
            && !rMyPsExSetPcState
          )
          || outp.regPcSetItCnt(1).lsb
        )

        //&& !temp.forceToZero
        //&& outp.calcForFmaxFwdValidMost(
        //  someShouldIgnoreInstr=(
        //    !rMyPsExSetPcState
        //    //&& !psExSetPc.fire
        //  ),
        //  someNodeIsFiring=upIsFiring,
        //  inPsEx=false
        //)
        //&& outp.gprIsNonZeroVec.last.last
        //&& (
        //  (
        //    //!psExSetPc.fire
        //    //&& 
        //    !rMyPsExSetPcState
        //    && !psExSetPc.fire
        //  )
        //  || outp.regPcSetItCnt(1).lsb
        //)
        //&& !outp.instrCnt.myPsIdBubble.head
        //&& (
        //  !outp.splitOp.opIsMemAccess
        //  || outp.inpDecodeExt.last.memAccessKind.asBits(1)
        //)
        //&& !outp.instrCnt.myPsIdBubble.last
      )
      //temp.data := outp.myExt(0).modMemWord //ram.io.wrData
      temp.addr := outp.gprIdxVec.last

      //val myTempMemAccessForcetoZero = (
      //  outp.splitOp.opIsMemAccess
      //  //&& !outp.inpDecodeExt.last.memAccessKind.asBits(1)
      //)

      //temp.memAccessForceToZero := (
      //  //(
      //  //  (
      //  //    rMyPsExSetPcState
      //  //    || psExSetPc.fire
      //  //  )
      //  //  && !outp.regPcSetItCnt(1).lsb
      //  //)
      //  //|| (
      //  //  outp.instrCnt.myPsIdBubble.head
      //  //)
      //  (
      //    outp.splitOp.opIsMemAccess
      //    && !outp.inpDecodeExt.last.memAccessKind.asBits(1)
      //  )
      //)

      temp.branchMispredictEtcForceToZero := (
        (
          (
            psExSetPc.fire
            || rMyPsExSetPcState
          )
          && !outp.regPcSetItCnt(1).lsb
        )
      )
      //temp.anyForceToZero := (
      //  //False
      //  temp.memAccessForceToZero
      //  //|| temp.branchMispredictEtcForceToZero
      //)
      val myTempHist = History(
        that=temp,
        length=(
          cfg.optForFmaxPsExFwdSize
          //cfg.optPreFwdForFmaxPsExFwdSize
        ),
        when=(
          myTempSaveOutpCond
          //upIsFiring
          ////&& outp.gprIsNonZeroVec.last.last
          //&& !outp.instrCnt.myPsIdBubble.last
          ////&& (
          ////  outp.gprIsNonZeroVec.last.last
          ////)
          //////&& (
          //////  (
          //////    //!psExSetPc.fire
          //////    //&& 
          //////    !rMyPsExSetPcState
          //////    //&& !psExSetPc.fire
          //////  )
          //////  || outp.regPcSetItCnt(1).lsb
          //////)
          ////&& !outp.instrCnt.myPsIdBubble.last
          //&& (
          //  //!outp.splitOp.opIsMemAccess
          //  !outp.inpDecodeExt.last.opIsMemAccess.head
          //  //|| outp.inpDecodeExt.last.memAccessKind.asBits(1)
          //)
        ),
        init=temp.getZero
      )
      //myTempHist

      val myFwdInfoVec = Vec.fill(myTempHist.size)(
        cloneOf(temp)
      )
      myFwdInfoVec := myTempHist
      for (idx <- 0 until myFwdInfoVec.size - 1) {
        when (
          myFwdInfoVec(idx).branchMispredictEtcForceToZero
          //myFwdInfoVec(idx).anyForceToZero
        ) {
          // don't forward the EX output of the first instruction
          // following branch mispredict, etc.

          //myFwdInfoVec(idx).valid := False
          //myFwdInfoVec(idx + 1).valid := False
          for (jdx <- idx + 1 until myFwdInfoVec.size) {
            myFwdInfoVec(jdx).valid := False
          }
        }
      }
      //for (idx <- 0 until myFwdInfoVec.size) {
      //  when (
      //    myTempMemAccessForcetoZero
      //    && (
      //      outp.gprIdxVec.last === myFwdInfoVec(idx).addr
      //    )
      //  ) {
      //    myFwdInfoVec(idx).valid := False
      //  }
      //}

      //when (
      //  //(
      //  //  !rMyPsExSetPcState
      //  //  && !outp.regPcSetItCnt(2).lsb
      //  //)
      //  //|| (
      //  //  outp.instrCnt.myPsIdBubble.last
      //  //)
      //  //|| (
      //  //  outp.splitOp.opIsMemAccess
      //  //  && !outp.inpDecodeExt.last.memAccessKind.asBits(1)
      //  //)
      //  //!temp.valid
      //  //(
      //  //  (
      //  //    //!psExSetPc.fire
      //  //    //&& 
      //  //    !rMyPsExSetPcState
      //  //    && !psExSetPc.fire
      //  //  )
      //  //  || outp.regPcSetItCnt(1).lsb
      //  //)
      //  //&& !outp.instrCnt.myPsIdBubble.head
      //  //&& (
      //  //  !outp.splitOp.opIsMemAccess
      //  //  || outp.inpDecodeExt.last.memAccessKind.asBits(1)
      //  //)
      //  //!outp.calcForFmaxFwdValidMost(
      //  //  someShouldIgnoreInstr=(
      //  //    !rMyPsExSetPcState
      //  //    //&& !psExSetPc.fire
      //  //  ),
      //  //  someNodeIsFiring=upIsFiring,
      //  //  inPsEx=false
      //  //)
      //  ////&& outp.gprIsNonZeroVec.last.last
      //  rMyPsExSetPcState
      //  && !outp.regPcSetItCnt(1).lsb
      //) {
      //  myFwdInfoVec.foreach(item => {
      //    item.valid := False
      //  })
      //}

      myFwdInfoVec
    }
    //val myHistForFwdData = (
    //  History(
    //    that=(
    //      ram.io.wrData
    //    ),
    //    length=(
    //      cfg.optWrHistLength + 2,
    //    ),
    //    init=False
    //  )
    //)
    val myTempHistFwdValid = Vec.fill(
      cfg.regFileCfg.modRdPortCnt
    )(
      UInt(myHistFwdInfo.size - 1 bits)
    )
    val myTempHistFwdForceToZero = Vec.fill(
      cfg.regFileCfg.modRdPortCnt
    )(
      //Bool()
      //UInt(myHistFwdInfo.size - 1 bits)
      Bool()
    )

    //val myTempHistFwdOpIsNonMemAccess = Vec.fill(
    //  cfg.regFileCfg.modRdPortCnt
    //)(
    //  //Bool()
    //  UInt(myHistFwdInfo.size - 1 bits)
    //)

    //case class MyRegFileWrPulseFwdInfo(
    //) extends Bundle {
    //  val myRegFileWrPulse = cloneOf(forFmaxRegFileWrPulseArr(0))
    //  def addr = myRegFileWrPulse.addr
    //  def data = myRegFileWrPulse.data
    //  def fire = myRegFileWrPulse.fire
    //  def valid = myRegFileWrPulse.valid
    //  val branchMispredictEtcForceToZero = Bool()
    //}

    //val myHistRegFileWrPulse = (
    //  //!cfg.optScoreboard
    //  true
    //) generate {
    //  val temp = MyRegFileWrPulseFwdInfo()
    //  temp.valid := (
    //    if (cfg.myHaveZeroReg) (
    //      forFmaxRegFileWrPulseArr(0).fire
    //      && forFmaxRegFileWrPulseArr(0).addr =/= 0x0
    //    ) else (
    //      forFmaxRegFileWrPulseArr(0).fire
    //    )
    //  )
    //  temp.myRegFileWrPulse.payload := (
    //    forFmaxRegFileWrPulseArr(0).payload
    //  )
    //  temp.branchMispredictEtcForceToZero := (
    //    (
    //      (
    //        psExSetPc.fire
    //        || rMyPsExSetPcState
    //      )
    //      && !outp.regPcSetItCnt(1).lsb
    //    )
    //  )

    //  val myTempHist = History(
    //    that=(
    //      //forFmaxRegFileWrPulseArr(0)
    //      temp
    //    ),
    //    when=forFmaxRegFileWrPulseArr(0).fire,
    //    length=(
    //      //2
    //      3//2
    //      //+ (if (cfg.optScoreboard) (1) else (0))
    //    ),
    //    init=(
    //      //forFmaxRegFileWrPulseArr(0).getZero
    //      temp.getZero
    //    ),
    //  )

    //  val myFwdInfoVec = Vec.fill(myTempHist.size)(
    //    cloneOf(temp)
    //  )
    //  myFwdInfoVec := myTempHist
    //  for (idx <- 0 until myFwdInfoVec.size - 1) {
    //    when (
    //      myFwdInfoVec(idx).branchMispredictEtcForceToZero
    //      //myFwdInfoVec(idx).anyForceToZero
    //    ) {
    //      for (
    //        jdx <- idx //+ 1 
    //        until myFwdInfoVec.size
    //      ) {
    //        myFwdInfoVec(jdx).valid := False
    //      }
    //    }
    //  }
    //  myFwdInfoVec
    //}
    //val stickyFwdRegFileWrPulse = (
    //  Vec.fill(cfg.regFileCfg.modRdPortCnt)(
    //    Flow(
    //      UInt(cfg.mainWidth bits)
    //    )
    //  )
    //)

    for (jdx <- 0 until myTempHistFwdValid.size) {
      //myTempHistFwdValid(jdx).lsb := False
      myTempHistFwdForceToZero(jdx) := (
        //myHistFwdInfo(idx + 1).valid
        //&&
        //myHistFwdInfo(idx + 1).anyForceToZero
        //&& (
        //  outp.gprIdxVec(jdx)
        //  === myHistFwdInfo(idx + 1).addr
        //)
        rSavedMostRecentGprWriteWasMemAccess(
          outp.gprIdxVec(jdx)
        )
        //False
      )
      for (idx <- 0 until myTempHistFwdValid(jdx).getWidth) {
        myTempHistFwdValid(jdx)(
          //myTempHistFwdValid(jdx).getWidth - idx - 1
          idx
        ) := (
          myHistFwdInfo(idx + 1).valid
          //&& (
          //  (
          //    //!psExSetPc.fire
          //    //&& 
          //    !rMyPsExSetPcState
          //    && !psExSetPc.fire
          //  )
          //  || outp.regPcSetItCnt(1).lsb
          //)

          && (
            if (cfg.optScoreboard) (
              (
                outp.gprIdxVec(jdx)
                === myHistFwdInfo(idx + 1).addr
              )
              //&& !myHistFwdInfo(idx + 1).opIsMemAccess
            ) else (
              outp.gprIdxVec(jdx)
              === myHistFwdInfo(idx + 1).addr
            )
          )

          //&& (
          //  LcvFastCmpEq(
          //    left=outp.gprIdxVec(jdx),
          //    right=myHistFwdInfo(idx + 1).addr,
          //    cmpEqIo=null,
          //  )._1
          //)
        )


        //myTempHistFwdOpIsNonMemAccess(jdx)(idx) := (
        //  //myHistFwdInfo(idx + 1).valid
        //  //&& 
        //  !myHistFwdInfo(idx + 1).forceToZero
        //  //&& (
        //  //  outp.gprIdxVec(jdx)
        //  //  === myHistFwdInfo(idx + 1).addr
        //  //)
        //)
      }

// >>> for idx in range(size):
// ...     print(idx, (("0" * (size - idx - 1))) + "1" + ("-" * idx))
// ...     
// 0 0001
// 1 001-
// 2 01--
// 3 1---

// >>> for idx in range(size):
// ...     print(idx, ("-" * (size - idx - 1) + "1" + ("0" * idx)))
// ...     
// 0 ---1
// 1 --10
// 2 -100
// 3 1000
      //if (cfg.optScoreboard) {
      //} else { // if (!cfg.optScoreboard)
        //val myFwdTempToSwitch = (
        //  Vec(Vec(
        //    myHistRegFileWrPulse.map(myWrPulse => (
        //      myWrPulse.fire
        //      && (
        //        outp.gprIdxVec(jdx)
        //        === myWrPulse.addr
        //      )
        //    ))
        //  ).reverse)
        //)
        //val myFwdTempToSwitchPayload = (
        //  Vec(Vec(
        //    myHistRegFileWrPulse.map(myWrPulse => (
        //      //myWrPulse.fire
        //      //&&
        //      (
        //        outp.gprIdxVec(jdx)
        //        === myWrPulse.addr
        //      )
        //    ))
        //  ).reverse)
        //)
        //switch (
        //  myFwdTempToSwitch.asBits
        //  //## RegNext
        //  ## (
        //    //RegNext(upIsFiring, init=False)
        //    //|| rose(upIsValid)
        //    upIsValid
        //  )
        //) {
        //  is (
        //    //M"1--"
        //    M"1---"
        //    //M"1-"
        //  ) {
        //    //outp.myPreFwdRdMemWord(jdx) := (
        //    //  myHistRegFileWrPulse(0).data
        //    //)
        //    stickyFwdRegFileWrPulse(jdx).valid := True
        //    stickyFwdRegFileWrPulse(jdx).payload := (
        //      myHistRegFileWrPulse(0).data
        //    )
        //  }
        //  is (
        //    //M"01-"
        //    M"01--"
        //    //M"01"
        //  ) {
        //    //outp.myPreFwdRdMemWord(jdx) := (
        //    //  myHistRegFileWrPulse(1).data
        //    //)
        //    stickyFwdRegFileWrPulse(jdx).valid := True
        //    stickyFwdRegFileWrPulse(jdx).payload := (
        //      myHistRegFileWrPulse(1).data
        //    )
        //  }
        //  is (
        //    //M"01-"
        //    M"001-"
        //    //M"01"
        //  ) {
        //    //outp.myPreFwdRdMemWord(jdx) := (
        //    //  myHistRegFileWrPulse(2).data
        //    //)
        //    stickyFwdRegFileWrPulse(jdx).valid := True
        //    stickyFwdRegFileWrPulse(jdx).payload := (
        //      myHistRegFileWrPulse(2).data
        //    )
        //  }
        //  //is (M"001") {
        //  //  outp.myPreFwdRdMemWord(jdx) := (
        //  //    myHistRegFileWrPulse(2).data
        //  //  )
        //  //}
        //  is (
        //    //M"001"
        //    M"0001"
        //  ) {
        //    //outp.myPreFwdRdMemWord(jdx) := 0x0
        //    stickyFwdRegFileWrPulse(jdx).valid := False
        //    stickyFwdRegFileWrPulse(jdx).payload := 0x0
        //  }
        //  default {
        //    //outp.myPreFwdRdMemWord(jdx) := 0x0
        //    stickyFwdRegFileWrPulse(jdx).valid := (
        //      RegNext(
        //        stickyFwdRegFileWrPulse(jdx).valid,
        //        init=stickyFwdRegFileWrPulse(jdx).valid.getZero
        //      )
        //    )
        //    stickyFwdRegFileWrPulse(jdx).payload := (
        //      RegNext(
        //        stickyFwdRegFileWrPulse(jdx).payload,
        //        init=stickyFwdRegFileWrPulse(jdx).payload.getZero
        //      )
        //    )
        //  }
        //}
        //switch (
        //  myFwdTempToSwitchValid.asBits
        //  ## myFwdTempToSwitchPayload.asBits
        //  //## (
        //  //  RegNext(upIsFiring, init=False)
        //  //  || rose(upIsValid)
        //  //)
        //) {
        //  is (M"1-1-") {
        //    stickyFwdRegFileWrPulse(jdx).valid := True
        //    stickyFwdRegFileWrPulse(jdx).payload := (
        //      myHistRegFileWrPulse(0).data
        //    )
        //  }
        //  is (M"0101") {
        //    stickyFwdRegFileWrPulse(jdx).valid := True
        //    stickyFwdRegFileWrPulse(jdx).payload := (
        //      myHistRegFileWrPulse(1).data
        //    )
        //  }
        //  is (M"1-0-") {
        //  }
        //  is (M"0100") {
        //  }
        //  default {
        //    stickyFwdRegFileWrPulse(jdx).valid := (
        //      RegNext(
        //        stickyFwdRegFileWrPulse(jdx).valid,
        //        init=stickyFwdRegFileWrPulse(jdx).valid.getZero
        //      )
        //    )
        //    stickyFwdRegFileWrPulse(jdx).payload := (
        //      RegNext(
        //        stickyFwdRegFileWrPulse(jdx).payload,
        //        init=stickyFwdRegFileWrPulse(jdx).payload.getZero
        //      )
        //    )
        //  }
        //  //is (M"1--") {
        //  //  //outp.myPreFwdRdMemWord(jdx) := (
        //  //  //  myHistRegFileWrPulse(0).data
        //  //  //)
        //  //  stickyFwdRegFileWrPulse(jdx).valid := True
        //  //  stickyFwdRegFileWrPulse(jdx).payload := (
        //  //    myHistRegFileWrPulse(0).data
        //  //  )
        //  //}
        //  //is (M"01-") {
        //  //  //outp.myPreFwdRdMemWord(jdx) := (
        //  //  //  myHistRegFileWrPulse(1).data
        //  //  //)
        //  //  stickyFwdRegFileWrPulse(jdx).valid := True
        //  //  stickyFwdRegFileWrPulse(jdx).payload := (
        //  //    myHistRegFileWrPulse(1).data
        //  //  )
        //  //}
        //  ////is (M"001") {
        //  ////  outp.myPreFwdRdMemWord(jdx) := (
        //  ////    myHistRegFileWrPulse(2).data
        //  ////  )
        //  ////}
        //  //is (M"001") {
        //  //  //outp.myPreFwdRdMemWord(jdx) := 0x0
        //  //  stickyFwdRegFileWrPulse(jdx).valid := False
        //  //  stickyFwdRegFileWrPulse(jdx).payload := 0x0
        //  //}
        //  //default {
        //  //  //outp.myPreFwdRdMemWord(jdx) := 0x0
        //  //  stickyFwdRegFileWrPulse(jdx).valid := (
        //  //    RegNext(
        //  //      stickyFwdRegFileWrPulse(jdx).valid,
        //  //      init=stickyFwdRegFileWrPulse(jdx).valid.getZero
        //  //    )
        //  //  )
        //  //  stickyFwdRegFileWrPulse(jdx).payload := (
        //  //    RegNext(
        //  //      stickyFwdRegFileWrPulse(jdx).payload,
        //  //      init=stickyFwdRegFileWrPulse(jdx).payload.getZero
        //  //    )
        //  //  )
        //  //}
        //}
        //outp.myPreFwdRdMemWord(jdx) := (
        //  stickyFwdRegFileWrPulse(jdx).payload
        //)
        switch (
          myTempHistFwdForceToZero(jdx)
          ## myTempHistFwdForceToZero(jdx)
          ## myTempHistFwdValid(jdx)
        ) {
          val size = myTempHistFwdValid(jdx).getWidth
          for (idx <- 0 until size) {
            is (MaskedLiteral({
              //("-" * idx)
              //+ "1"
              //+ (("0" * (myTempHistFwdValid(jdx).getWidth - idx - 1)))
              "00" + ("-" * (size - idx - 1) + "1" + ("0" * idx))
            })) {
              //when (
              //  !myTempHistFwdForceToZero(jdx)
              //) {
                outp.forFmaxFwdIdx(jdx) := idx + 1
              //} otherwise {
              //  // loads/stores aren't forwarded
              //  outp.forFmaxFwdIdx(jdx) := 0x0
              //}

              //outp.myExt(0).rdMemWord(jdx) := (
              //  myHistFwdInfo(
              //    //myHistFwdInfo.size - 1 - idx //(idx + 1)
              //    idx + 1
              //  ).data
              //)
            }
          }
          default {
            //when (
            //  //!myFwdTempToSwitch.orR
            //  //!stickyFwdRegFileWrPulse.map(_.fire).orR
            //  !stickyFwdRegFileWrPulse(jdx).fire
            //) {
              outp.forFmaxFwdIdx(jdx) := (
                0x0
                //(1 << outp.forFmaxFwdIdx(jdx).getWidth) - 1
              )
            //} otherwise {
            //  outp.forFmaxFwdIdx(jdx) := (
            //    cfg.optForFmaxPsExFwdSize
            //  )
            //}
            //outp.myExt(0).rdMemWord(jdx) := (
            //  inp.myExt(0).rdMemWord(jdx)
            //)
          }
        }
      //}
    }
  })
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
  val instrCnt = setAsInp(
    SnowHouseInstrCnt(cfg=cfg)
  )
  val multiCycleBusRecvDataVec = (
    cfg.havePsExStall
  ) generate (
    setAsInp(Vec[MultiCycleDevPayload]{
      val tempArr = ArrayBuffer[
        MultiCycleDevPayload
      ]()
      for (
        //((_, opInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
        (group, innerMap) <- cfg.multiCycleOpInfoMap.view
      ) {
        for (((_, opInfo), idx) <- innerMap.view.zipWithIndex) {
          require(
            opInfo.select == OpSelect.MultiCycle
          )
          //tempArr += (
          //  MultiCycleDevPayload(cfg=cfg, opInfo=opInfo)
          //)
        }
        //if (innerMap.size == 0) {
          tempArr += (
            MultiCycleDevPayload(
              cfg=cfg,
              //maxDstArrSize=cfg.maxMultiCycleDstArrSizeMap.get(group).get
              group=group
            )
          )
        //} else {
        //}
      }
      tempArr
    })
  )
  //val bubble = setAsInp(Bool())
  val currOp = setAsInp(UInt(log2Up(cfg.opInfoMap.size) bits))
  val inMultiCycleOp = setAsInp(Bool())
  val splitOp = setAsInp(SnowHouseSplitOp(cfg=cfg))
  val tempVecSize = cfg.regFileCfg.modRdPortCnt //2 // TODO: temporary size of `2`
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
  val regPc = setAsInp(UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  ))
  val mySavedRegPcPlusInstrSize = setAsInp(
    /*Flow*/Vec.fill(2)(
      UInt(
        //cfg.mainWidth bits
        cfg.mainAddrWidth bits
      )
    )
  )
  val laggingRegPc = setAsInp(UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  ))
  val laggingRegPcPlus1InstrSize = setAsInp(UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  ))
  val regPcSetItCnt = setAsInp(Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
    UInt(
      //1 bits
      cfg.regPcSetItCntWidth bits
    )
  ))
  val myDoStallAny = setAsInp(Bool())
  val upIsFiring = setAsInp(Bool())
  val upIsValid = setAsInp(Bool())
  val upIsReady = setAsInp(Bool())
  val downIsFiring = setAsInp(Bool())
  val downIsValid = setAsInp(Bool())
  val downIsReady = setAsInp(Bool())
  val regPcPlusInstrSize = setAsInp(UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  ))
  val regPcPlusImm = setAsInp(UInt(
    //cfg.mainWidth bits
    cfg.mainAddrWidth bits
  ))
  //val regPcPlusImmRealDst = setAsInp(UInt(cfg.mainWidth bits))
  val imm = setAsInp(Vec.fill(4)(UInt(cfg.mainWidth bits)))
  //val pcChangeState = setAsOutp(
  //  Bool()
  //  //SnowHouseShouldIgnoreInstrState()
  //  //UInt(
  //  //  //3 
  //  //  SnowHouseShouldIgnoreInstrState().asBits.getWidth bits
  //  //)
  //) ///*in*/(Flow(PcChangeState()))

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
    Vec.fill(2)(
      UInt(
        //cfg.mainWidth bits
        cfg.mainAddrWidth bits 
      )
    )
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
        //|| idx >= cfg.regFileCfg.modRdPortCnt
      ) {
        require(
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
            val zdx = opInfo.myRdMemWordIdxMap.get(tempIdx)
            require(
              zdx != None,
              s"eek! ${opInfo.myRdMemWordIdxMap} ${tempIdx}"
            )

            //opInfo.select match {
            //  case OpSelect.MultiCycle => {
            //    opInfo.multiCycleOp match {
            //      case Some(multiCycleOp) => {
            //        if (multiCycleOp == MultiCycleOpKind.Udivw) {
            //          println(
            //            s"Udivw: idx:${idx} zdx:${zdx.get}"
            //          )
            //        }
            //      }
            //      case None => {
            //        require(
            //          false,
            //          s"eek!"
            //        )
            //      }
            //    }
            //  }
            //  case _ => {
            //  }
            //}
            rdMemWord(zdx.get + gprIdxAddend)
            ////rdMemWord(idx + gprIdxAddend)
            //var tempRdMemWord: Option[UInt] = None
            ////var innerIdx: Int = 0
            ////for (innerIdx <- 0 until tempIdx) {
            ////}
            //val myMap = LinkedHashMap[Int, Int]()
            //var innerRdMemWordIdx: Int = 0
            //for (zdx <- 0 until opInfo.srcArr.view.size) {
            //  if (opInfo.srcArr.view
            //}

            //tempRdMemWord match {
            //  case Some(myTempRdMemWord) => {
            //    myTempRdMemWord
            //  }
            //  case None => {
            //    require(
            //      false,
            //      s"eek! idx:${idx}"
            //    )
            //  }
            //}
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
    val ret = innerFunc(idx=idx)
    //opInfo.select match {
    //  case OpSelect.MultiCycle => {
    //    
    //  }
    //  case _ => {
    //  }
    //}
    ret
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

  val branchKind = setAsInp(
    Bits(
      //SnowHouseBranchPredictorKind.branchKindEnumMaxWidth bits
      cfg.optBranchPredictorKind.get._branchKindEnumWidth bits
    )
  )
  val btbElemValid = setAsInp(
    Bool()
  )
  val btbElemDontPredict = setAsInp(
    Bool()
  )
  val branchPredictTkn = setAsInp(
    Bool()
  )
  val branchPredictReplaceBtbElemMost = setAsInp(
    Bool()
  )
  val btbElemSavedDstRegPc = setAsInp(
    UInt(
      cfg.mainAddrWidth bits
    )
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
  //val multiCycleOpInfoIdx = setAsOutp(
  //  UInt(log2Up(cfg.multiCycleOpInfoMap.size) bits)
  //)
  //def opIs = decodeExt.opIs
  def opIsMemAccess = outpDecodeExt.opIsMemAccess
  //def opIsCpyNonJmpAlu = decodeExt.opIsCpyNonJmpAlu
  //def opIsAluShift = outpDecodeExt.opIsAluShift
  //def opIsJmp = outpDecodeExt.opIsJmp
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
  //args: SnowHousePipeStageArgs,
  cfg: SnowHouseConfig,
) extends Component {
  //def cfg = args.cfg
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
    (
      if (cfg.myHaveZeroReg) (
        //!io.gprIsZeroVec(0)(idx)
        io.gprIsNonZeroVec.last(idx)
      ) else (
        True
      )
    )
    && (
      !io.instrCnt.myPsIdBubble(idx)
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

  val nextExSetPcValid = (
    Vec.fill(enumExSetPcValidLim)(
      Bool()
    )
  )
  val rExSetPcValid = {
    val temp = (
      //Reg(
      //  Vec.fill(enumExSetPcValidLim)(
      //    Bool()
      //  )
      //)
      RegNext/*When*/(
        nextExSetPcValid,
        //cond=io.upIsFiring,
      )
    )
    temp.foreach(item => item.init(item.getZero))
    if (!cfg.targetAltera) (
      temp
    ) else (
      KeepAttribute(temp)
    )
  }

  //--------
  // BEGIN: Old `SetPcCmp`
  //case class SetPcCmp(
  //  //mulAcc: LcvMulAcc32Del1
  //  //adder: LcvAddDel1,
  //  //cmpEqDel1: LcvCmpEqDel1,
  //) extends Area {
  //  val rValid = (
  //    Reg(Bool(), init=False)
  //    //Bool()
  //  )
  //  //nextValid := (
  //  //  RegNext(
  //  //    next=nextValid,
  //  //    init=nextValid.getZero,
  //  //  )
  //  //)
  //  //val nextValid = 
  //  val myCmp = UInt(cfg.mainWidth + 1 bits)
  //  //val myStickyCmp = Bool()
  //  //val mulAccIo = (
  //  //  LcvMulAcc32Io(
  //  //    optIncludeClk=true
  //  //  )
  //  //)
  //  //val mulAcc = LcvMulAcc32Del1()
  //  //val left = io.rdMemWord(io.brCondIdx(0))
  //  //val right = io.rdMemWord(io.brCondIdx(1))
  //  //val cmpEq = (
  //  //  left === right
  //  //)
  //  //val cmpEq = (
  //  //  RegNext(
  //  //    next=(
  //  //      io.rdMemWord(io.brCondIdx(0)) === io.rdMemWord(io.brCondIdx(1))
  //  //    ),
  //  //    init=False
  //  //  )
  //  //)
  //  val (cmpEq, cmpEqQ) = (
  //    LcvFastCmpEq(
  //      //left=RegNext/*When*/(
  //      //  next=io.rdMemWord(io.brCondIdx(0)),
  //      //  //cond=io.upIsValid,
  //      //  init=io.rdMemWord(io.brCondIdx(0)).getZero,
  //      //),
  //      //right=RegNext/*When*/(
  //      //  next=io.rdMemWord(io.brCondIdx(1)),
  //      //  //cond=io.upIsValid,
  //      //  init=io.rdMemWord(io.brCondIdx(1)).getZero,
  //      //),
  //      left=io.rdMemWord(io.brCondIdx(0)),
  //      right=io.rdMemWord(io.brCondIdx(1)),
  //      //mulAccIo=(
  //      //  //mulAccIo
  //      //  mulAcc.io
  //      //),
  //      //addIo=(
  //      //  adder.io
  //      //),
  //      cmpEqIo=(
  //        //cmpEqIo
  //        //cmpEqDel1.io
  //        null
  //      ),
  //      optDsp=(
  //        //true
  //        false
  //      ),
  //      optReg=true,
  //      //kind=LcvFastCmpEq.Kind.UseFastCarryChain,
  //    )
  //  )
  //  //mulAcc.io <> mulAccIo
  //}
  ////val mySetPcCmpEqAdder = LcvAddDel1(cfg.mainWidth + 1)
  ////mySetPcCmpEqAdder.io.do_inv := False

  ////val myCmpEqDel1ForEq = LcvCmpEqDel1(cfg.mainWidth)
  //val myPsExSetPcCmpEq = SetPcCmp(
  //  //cmpEqDel1=(
  //  //  myCmpEqDel1ForEq
  //  //  //null
  //  //)
  //)

  ////val rMyPsExSetPcCmpEqValid = Reg(Bool(), init=False)
  ////val myPsExSetPcCmpEq = /*Reg*/(UInt(cfg.mainWidth + 1 bits)) //init(0x0)
  ////val myPsExSetPcCmpEq.myStickyCmp = Bool()
  ////val mySetPcCmpNeAdder = LcvAddDel1(cfg.mainWidth + 1)
  ////mySetPcCmpNeAdder.io.do_inv := True
  ////val myCmpEqDel1 = LcvCmpEqDel1(cfg.mainWidth)
  ////val myPsExSetPcCmpNe = SetPcCmp(adder=mySetPcCmpNeAdder)
  ////val rMyPsExSetPcCmpNeValid = Reg(Bool(), init=False)
  ////val myPsExSetPcCmpNe = /*Reg*/(UInt(cfg.mainWidth + 1 bits)) //init(0x0)
  ////val myPsExSetPcCmpNe.myStickyCmp = Bool()

  ////val myCmpEqDel1ForNe = LcvCmpEqDel1(cfg.mainWidth)
  //val myPsExSetPcCmpNe = SetPcCmp(
  //  //cmpEqDel1=(
  //  //  myCmpEqDel1ForNe
  //  //  //null
  //  //)
  //)
  // END: Old `SetPcCmp`
  //--------

  //rExSetPcValid.foreach(_ := False)
  nextExSetPcValid.foreach(_ := False)

  //--------
  // BEGIN: Old `SetPcCmp`
  //myPsExSetPcCmpEq.myCmp := (
  //  //0x0
  //  //False
  //  //RegNextWhen(
  //  //  next=myPsExSetPcCmpEq,
  //  //  cond=(
  //  //    io.upIsValid
  //  //    && io.downIsReady
  //  //  ),
  //  //  init=myPsExSetPcCmpEq.getZero,
  //  //)
  //  RegNext(
  //    next=myPsExSetPcCmpEq.myCmp,
  //    init=myPsExSetPcCmpEq.myCmp.getZero,
  //  )
  //)
  //myPsExSetPcCmpNe.myCmp := (
  //  //0x0
  //  //False
  //  //RegNextWhen(
  //  //  next=myPsExSetPcCmpNe,
  //  //  cond=(
  //  //    io.upIsValid
  //  //    && io.downIsReady
  //  //  ),
  //  //  init=myPsExSetPcCmpNe.getZero,
  //  //)
  //  //RegNext(
  //  //  next=myPsExSetPcCmpNe,
  //  //  init=myPsExSetPcCmpEq.getZero,
  //  //)
  //  RegNext(
  //    next=myPsExSetPcCmpNe.myCmp,
  //    init=myPsExSetPcCmpNe.myCmp.getZero,
  //  )
  //)
  // END: Old `SetPcCmp`
  //--------

  val myPsExSetPcValidToOrReduce = (
    Cat(
      //RegNext/*When*/(
      //  next=rExSetPcValid,
      //  //cond=(!io.shouldIgnoreInstr.last),
      //  init=rExSetPcValid.getZero
      //),
      rExSetPcValid,
      //myPsExSetPcCmpEq.myCmp.msb,
      ////RegNext(myPsExSetPcCmpEq.myStickyCmp, init=False),
      //myPsExSetPcCmpNe.myCmp.msb,
      ////RegNext(myPsExSetPcCmpNe.myStickyCmp, init=False),
    ).asUInt
  )

  val myPsExSetPcValid = (
    Bool()
  )
  myPsExSetPcValid := (
    RegNext(
      myPsExSetPcValid,
      init=myPsExSetPcValid.getZero,
    )
    //False
  )
  //--------
  // BEGIN: old code for branches/jumps
  //when (
  //  RegNext(
  //    io.upIsFiring,
  //    init=False
  //  )
  //) {
  //  myPsExSetPcValid := False
  //}
  //when (
  //  myPsExSetPcValidToOrReduce.orR
  //) {
  //  myPsExSetPcValid := True
  //}

  //val rSavedMyPsExSetPcValid = Reg(Bool(), init=False)
  //val stickyMyPsExSetPcValid = (
  //  myPsExSetPcValid
  //)

  //val tempPsExSetPcDontPredict = Bool()
  //tempPsExSetPcDontPredict := False
  //val rSavedTempPsExSetPcDontPredict = Reg(Bool(), init=False)
  //val stickyTempPsExSetPcDontPredict = (
  //  //io.upIsValid
  //  //&& 
  //  (
  //    tempPsExSetPcDontPredict
  //    || rSavedTempPsExSetPcDontPredict
  //  )
  //)
  //when (
  //  io.upIsValid
  //) {
  //  when (tempPsExSetPcDontPredict) {
  //    rSavedTempPsExSetPcDontPredict := True
  //  }
  //}
  //when (io.upIsFiring) {
  //  rSavedTempPsExSetPcDontPredict := False
  //}

  //val tempPsExSetPcValid = Bool() //Reg(Bool(), init=False)
  //val rSavedTempPsExSetPcValid = Reg(Bool(), init=False)
  //val stickyTempPsExSetPcValid = (
  //  //io.upIsValid
  //  //&& 
  //  (
  //    tempPsExSetPcValid
  //    || rSavedTempPsExSetPcValid
  //  )
  //)
  //when (
  //  io.upIsValid
  //) {
  //  when (tempPsExSetPcValid) {
  //    rSavedTempPsExSetPcValid := True
  //  }
  //}
  //when (
  //  io.upIsFiring
  //) {
  //  rSavedTempPsExSetPcValid := False
  //}
  // END: old code for branches/jumps
  //--------
  // BEGIN: old code for branches/jumps
  //val myTempDstRegPc = UInt(cfg.mainAddrWidth bits)
  //myTempDstRegPc := (
  //  RegNext(
  //    myTempDstRegPc,
  //    init=myTempDstRegPc.getZero
  //  )
  //  //io.btbElemSavedDstRegPc
  //  //RegNext(
  //  //  io.btbElemSavedDstRegPc,
  //  //  init=io.btbElemSavedDstRegPc.getZero
  //  //)
  //)
  //val myTempCondDstRegPc = (
  //  RegNext(
  //    (
  //      //io.splitOp.exSetNextPcKind
  //      //=/= SnowHousePsExSetNextPcKind.Dont
  //      (
  //        io.splitOp.exSetNextPcKind
  //        === SnowHousePsExSetNextPcKind.RdMemWord
  //      ) || (
  //        io.splitOp.exSetNextPcKind
  //        === SnowHousePsExSetNextPcKind.RdMemWordPlusImm
  //      )
  //    ),
  //    init=False
  //  )
  //  && (
  //    myTempDstRegPc
  //    =/= RegNext(
  //      io.btbElemSavedDstRegPc,
  //      init=io.btbElemSavedDstRegPc.getZero
  //    )
  //  )
  //)
  ////val rSavedTempCondDstRegPc = Reg(Bool(), init=False)
  ////val stickyTempCondDstRegPc = (
  ////  myTempCondDstRegPc
  ////  || rSavedTempCondDstRegPc
  ////)
  ////when (myTempCondDstRegPc) {
  ////  rSavedTempCondDstRegPc := True
  ////}
  ////when (io.upIsFiring) {
  ////  rSavedTempCondDstRegPc := False
  ////}

  //val tempBranchMispredictNotTaken = Bool()
  //val tempBranchPredictTkn = (
  //  //rose(
  //    //RegNext(next=io.branchPredictTkn, init=False)
  //    //RegNext/*When*/(
  //    //  next=
  //      RegNextWhen(
  //        next=(
  //          io.branchPredictTkn
  //          //|| io.branchPredictReplaceBtbElem
  //        ),
  //        cond=(
  //          // TODO:
  //          // maybe change this back to `io.upIsReady` once the logic
  //          // for branch prediction plus load "delay slot" bubbles
  //          // is put into the `SnowHousePipeStageInstrDecode`
  //          // pipeline stage
  //          //io.upIsReady
  //          io.upIsFiring
  //        ),
  //        init=False
  //      )//,
  //    //  //cond=io.upIsFiring,
  //    //  init=False,
  //    //)
  //    //&& io.upIsReady
  //  //)
  //)
  //val rSavedTempBranchPredictTkn = Reg(Bool(), init=False)
  //val stickyTempBranchPredictTkn = (
  //  tempBranchPredictTkn
  //  || rSavedTempBranchPredictTkn
  //)
  ////when (io.upIsValid) {
  //  when (tempBranchPredictTkn) {
  //    rSavedTempBranchPredictTkn := True
  //  }
  ////}
  //when (io.upIsFiring) {
  //  rSavedTempBranchPredictTkn := False
  //}
  //val tempReplaceBtbElem = (
  //  RegNext/*When*/(
  //    next=(
  //      io.branchPredictReplaceBtbElemMost
  //    ),
  //    //cond=io.upIsFiring,
  //    init=False,
  //  )
  //  //|| stickyTempCondDstRegPc
  //  || myTempCondDstRegPc
  //)
  //val rSavedTempReplaceBtbElem = Reg(Bool(), init=False)
  //val stickyTempReplaceBtbElem = (
  //  tempReplaceBtbElem
  //  || rSavedTempReplaceBtbElem
  //)
  ////when (io.upIsValid) {
  //  when (tempReplaceBtbElem) {
  //    rSavedTempReplaceBtbElem := True
  //  }
  ////}
  //when (io.upIsFiring) {
  //  rSavedTempReplaceBtbElem := False
  //}
  //val tempBtbFire = (
  //  //rose(
  //    //RegNext/*When*/(
  //    //  next=(
  //        RegNextWhen(
  //          next=(
  //            //rose(
  //              (
  //                io.btbElemValid
  //                && (
  //                  !io.btbElemDontPredict
  //                  //|| io.branchPredictReplaceBtbElem
  //                )
  //                //&& !io.shouldIgnoreInstr.last
  //              )
  //            //)
  //          ),
  //          cond=(
  //            // TODO:
  //            // maybe change this back to `io.upIsReady` once the logic
  //            // for branch prediction plus load "delay slot" bubbles
  //            // is put into the `SnowHousePipeStageInstrDecode`
  //            // pipeline stage
  //            //io.upIsReady
  //            io.upIsFiring
  //          ),
  //          init=False
  //        )
  //    //  ),
  //    //  //cond=io.upIsFiring,
  //    //  init=False,
  //    //)
  //    //&& io.upIsReady
  //  //)
  //)
  //val rSavedTempBtbFire = Reg(Bool(), init=False)
  //val stickyTempBtbFire = (
  //  tempBtbFire
  //  //|| rSavedTempBtbFire
  //)
  //when (io.upIsValid) {
  //  when (tempBtbFire) {
  //    rSavedTempBtbFire := True
  //  }
  //}
  //when (io.upIsFiring) {
  //  rSavedTempBtbFire := False
  //}

  //tempPsExSetPcValid := False
  //tempBranchMispredictNotTaken := False

  //val nextTempPsExSetPcValid = (
  //  (
  //    stickyMyPsExSetPcValid
  //    =/= stickyTempBranchPredictTkn
  //  ) || (
  //    stickyTempReplaceBtbElem
  //  )
  //)
  //when (
  //  RegNextWhen(
  //    !io.shouldIgnoreInstr.last,
  //    cond=io.upIsFiring,
  //    init=False
  //  )
  //  &&
  //  RegNext(
  //    (
  //      io.splitOp.exSetNextPcKind
  //      =/= SnowHousePsExSetNextPcKind.Dont
  //    ),
  //    init=False
  //  )
  //) {
  //  when (
  //    //tempBtbFire
  //    stickyTempBtbFire
  //  ) {
  //    tempPsExSetPcValid := (
  //      nextTempPsExSetPcValid
  //      || tempBranchMispredictNotTaken
  //    )
  //    tempBranchMispredictNotTaken := (
  //      stickyTempBranchPredictTkn
  //      && !stickyMyPsExSetPcValid
  //    )
  //  } otherwise {
  //    tempPsExSetPcValid := stickyMyPsExSetPcValid
  //    tempBranchMispredictNotTaken := False
  //  }
  //} otherwise {
  //}
  //val myTakeIrq = (
  //  //rose(
  //  //  io.takeIrq
  //  //  && io.upIsFiring
  //  //  && io.rIe
  //  //)
  //  Bool()
  //)
  //myTakeIrq := (
  //  io.takeIrq
  //  //&& io.upIsFiring
  //  && io.upIsValid
  //  //&& io.downIsReady
  //  && io.rIe
  //)
  //val rSavedTakeIrq = Reg(Bool(), init=False)
  //val stickyTakeIrq = (
  //  //io.takeIrq
  //  myTakeIrq
  //  || rSavedTakeIrq
  //)
  ////myTakeIrq := False
  //when (
  //  //io.upIsValid
  //  io.upIsFiring
  //) {
  //  when (myTakeIrq) {
  //    rSavedTakeIrq := True
  //    //myTakeIrq := True
  //    //tempPsExSetPcValid := True
  //    //tempBranchMispredictNotTaken := False
  //  }
  //}
  //when (
  //  RegNext(io.upIsFiring, init=False)
  //  //&& rSavedTakeIrq
  //) {
  //  rSavedTakeIrq := False
  //}
  //when (
  //  //stickyTakeIrq
  //  rose(RegNext(
  //    (
  //      io.upIsFiring
  //      && (
  //        stickyTakeIrq
  //        || io.btbElemDontPredict
  //      )
  //    ),
  //    init=False
  //  ))
  //) {
  //  //io.psExSetPc.taken.valid := io.psExSetPc.taken.valid.getZero
  //  tempPsExSetPcValid := True
  //  //io.psExSetPc.branchTgtBufElem.dontPredict := True
  //  tempPsExSetPcDontPredict := True
  //}
  //io.psExSetPc.valid := (
  //  stickyTempPsExSetPcValid
  //  && RegNextWhen(
  //    !io.shouldIgnoreInstr(0),
  //    cond=io.upIsFiring,
  //    init=False
  //  )
  //)

  ////io.psExSetPc := io.psExSetPc.getZero
  //io.psExSetPc.payload := io.psExSetPc.payload.getZero
  //io.psExSetPc.taken.allowOverride
  //io.psExSetPc.taken.valid := (
  //  RegNext(
  //    (
  //      //RegNext(
  //        (
  //          io.splitOp.exSetNextPcKind
  //          =/= SnowHousePsExSetNextPcKind.Dont
  //        )//,
  //      //  init=False
  //      //)
  //      && io.upIsFiring
  //    ),
  //    init=False
  //  )
  //)
  //io.psExSetPc.taken.reallyTaken := (
  //  stickyMyPsExSetPcValid
  //)
  //io.psExSetPc.taken.srcRegPc := (
  //  RegNext(io.laggingRegPc)
  //)
  //io.psExSetPc.nextPc.allowOverride
  //io.psExSetPc.nextPc := (
  //  RegNext(
  //    io.psExSetPc.nextPc,
  //    init=io.psExSetPc.nextPc.getZero,
  //  )
  //)
  ////io.psExSetPc.dstPc.allowOverride
  ////io.psExSetPc.dstPc := (
  ////  RegNext(
  ////    io.psExSetPc.dstPc,
  ////    init=io.psExSetPc.dstPc.getZero
  ////  )
  ////)
  //io.psExSetPc.branchKind.allowOverride
  //io.psExSetPc.branchKind := (
  //  RegNext(
  //    io.psExSetPc.branchKind,
  //    init=io.psExSetPc.branchKind.getZero,
  //  )
  //)
  //io.psExSetPc.branchTgtBufElem.valid.allowOverride
  //io.psExSetPc.branchTgtBufElem.valid := (
  //  RegNext(
  //    io.psExSetPc.branchTgtBufElem.valid,
  //    init=io.psExSetPc.branchTgtBufElem.valid.getZero,
  //  )
  //)
  ////io.psExSetPc.branchTgtBufElem.dontPredict.allowOverride
  ////io.psExSetPc.branchTgtBufElem.dontPredict := (
  ////  RegNext(
  ////    io.psExSetPc.branchTgtBufElem.dontPredict,
  ////    init=io.psExSetPc.branchTgtBufElem.dontPredict.getZero,
  ////  )
  ////)
  //io.psExSetPc.branchTgtBufElem
  ////io.psExSetPc.branchTgtBufElem.valid.setAsReg() init(False)
  ////io.psExSetPc.branchTgtBufElem.srcRegPc.allowOverride
  ////io.psExSetPc.branchTgtBufElem.srcRegPc := (
  ////  RegNext(
  ////    io.psExSetPc.branchTgtBufElem.srcRegPc,
  ////    init=io.psExSetPc.branchTgtBufElem.srcRegPc.getZero
  ////  )
  ////)
  //io.psExSetPc.branchTgtBufElem.dstRegPc.allowOverride
  //io.psExSetPc.branchTgtBufElem.dstRegPc := (
  //  RegNext(
  //    io.psExSetPc.branchTgtBufElem.dstRegPc,
  //    init=io.psExSetPc.branchTgtBufElem.dstRegPc.getZero
  //  )
  //)
  // END: old code for branches/jumps
  //--------
  // BEGIN: new code for branches/jumps
  when (
    RegNext(
      io.upIsFiring,
      init=False
    )
  ) {
    myPsExSetPcValid := False
  }
  when (
    myPsExSetPcValidToOrReduce.orR
  ) {
    myPsExSetPcValid := True
  }
  val rMyTempDstRegPc = {
    val temp = Reg(Flow(
      Vec.fill(4)(
        UInt(cfg.mainAddrWidth bits)
      )
    ))
    temp.init(temp.getZero)
    temp
  }
  //myTempDstRegPc := (
  //  RegNext(
  //    myTempDstRegPc,
  //    init=myTempDstRegPc.getZero
  //  )
  //  //io.btbElemSavedDstRegPc
  //  //RegNext(
  //  //  io.btbElemSavedDstRegPc,
  //  //  init=io.btbElemSavedDstRegPc.getZero
  //  //)
  //)
  //val myTempCondDstRegPc = (
  //  RegNextWhen(
  //    (
  //      //io.splitOp.exSetNextPcKind
  //      //=/= SnowHousePsExSetNextPcKind.Dont
  //      (
  //        io.splitOp.exSetNextPcKind
  //        === SnowHousePsExSetNextPcKind.RdMemWord
  //      ) || (
  //        io.splitOp.exSetNextPcKind
  //        === SnowHousePsExSetNextPcKind.RdMemWordPlusImm
  //      )
  //    ),
  //    cond=io.upIsFiring,
  //    init=False
  //  )
  //  && (
  //    myTempDstRegPc
  //    =/= RegNextWhen(
  //      io.btbElemSavedDstRegPc,
  //      cond=io.upIsFiring,
  //      init=io.btbElemSavedDstRegPc.getZero
  //    )
  //  )
  //)
  val myHadBranchLastInstr = (
    //RegNextWhen(
    //  (
    //    (
    //      (
    //        io.splitOp.exSetNextPcKind
    //        === SnowHousePsExSetNextPcKind.PcPlusImm
    //      )
    //      || (
    //        io.splitOp.exSetNextPcKind
    //        === SnowHousePsExSetNextPcKind.RdMemWord
    //      )
    //      || (
    //        io.splitOp.exSetNextPcKind
    //        === SnowHousePsExSetNextPcKind.RdMemWordPlusImm
    //      )
    //    )
    //    && (
    //      !io.shouldIgnoreInstr(0)
    //    )
    //  ),
    //  cond=io.upIsFiring,
    //  init=False
    //)
    rMyTempDstRegPc.fire
  )
  val myTempBranchMispredictTakenMost = (
    Vec[Bool](
      //rose
      (
        myPsExSetPcValid
        =/= RegNextWhen(
          io.branchPredictTkn,
          cond=io.upIsFiring,
          init=False
        )
        //!LcvFastCmpEq(
        //  left=myPsExSetPcValid,
        //  right=RegNext(
        //    io.branchPredictTkn,
        //    init=False
        //  ),
        //  cmpEqIo=null,
        //)._1
      ),
      //rose
      (
        myPsExSetPcValid
        && (
          io.laggingRegPc
          =/= rMyTempDstRegPc.payload(0)
          //!LcvFastCmpEq(
          //  left=io.laggingRegPc,
          //  right=rMyTempDstRegPc.payload,
          //  cmpEqIo=null,
          //)._1
        )
      )
    )
  )
  val myTempBranchMispredictNotTakenMost = (
    Vec[Bool](
      //rose
      (
        !myPsExSetPcValid
        && RegNextWhen(
          io.branchPredictTkn,
          cond=io.upIsFiring,
          init=False
        )
      ),
      //rose
      (
        !myPsExSetPcValid
        && (
          //io.laggingRegPc
          //=/= io.mySavedRegPcPlusInstrSize.last
          (
            io.laggingRegPc
            =/= io.mySavedRegPcPlusInstrSize.last
          )
          //!LcvFastCmpEq(
          //  left=io.laggingRegPc,
          //  right=io.mySavedRegPcPlusInstrSize.last,
          //  cmpEqIo=null,
          //)._1
        )
      ),
    )
  )
  val tempBranchMispredictNotTaken = (
    myHadBranchLastInstr
    && myTempBranchMispredictNotTakenMost.orR
  )
  val myBranchMispredictCond = (
    myHadBranchLastInstr
    && (
      myTempBranchMispredictTakenMost.orR
      || myTempBranchMispredictNotTakenMost.orR
    )
    //&& io.upIsValid
    //&& io.upIsFiring
    //&& (
    //  myPsExSetPcValid
    //)
    //&& (
    //  myTempDstRegPc
    //  =/= io.laggingRegPc
    //)
  )
  //io.psExSetPc.allowOverride
  io.psExSetPc.valid := (
    rose(
      myBranchMispredictCond
    )
    //&& (
    //  RegNextWhen(
    //    !io.shouldIgnoreInstr(0),
    //    cond=io.upIsFiring,
    //    init=False
    //  ) 
    //)
  )
  if (cfg.optScoreboard) {
    io.psExSetPc.reorderBufIdx := (
      io.instrCnt.scoreboardIssuePayload.reorderBufIdx
    )
  }
  io.psExSetPc.branchTgtBufElem.srcRegPc := (
    io.psExSetPc.branchTgtBufElem.srcRegPc.getZero
  )
  io.psExSetPc.taken.valid := (
    myHadBranchLastInstr
    //&& io.upIsFiring
    && RegNext(
      io.upIsFiring,
      init=False
    )
    //&& (
    //  RegNext(
    //    io.laggingRegPc
    //  )
    //)
  )
  io.psExSetPc.taken.myPsExSetPcValid := myPsExSetPcValid
  io.psExSetPc.taken.srcRegPc := (
    RegNextWhen(
      io.laggingRegPc,
      cond=io.upIsFiring,
      init=io.laggingRegPc.getZero,
    )
  )
  //io.psExSetPc.taken.reallyTaken
  //io.psExSetPc
  // END: new code for branches/jumps
  //--------
  io.dbusHostPayload := (
    RegNext(
      io.dbusHostPayload,
      init=io.dbusHostPayload.getZero,
    )
  )
  io.dbusHostPayload.addr.allowOverride
  io.dbusHostPayload.data.allowOverride
  io.dbusHostPayload.accKind.allowOverride
  io.dbusHostPayload.subKind.allowOverride
  io.dbusHostPayload.subKindIsLtWordWidth.allowOverride
  io.dbusHostPayload.myLcvDbusByteSize.allowOverride
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
      io.splitOp.multiCycleOpGroup(idx)
    )
  }
  //val nextShouldIgnoreInstrState = (
  //  Vec.fill(
  //    io.regPcSetItCnt.size
  //  )(
  //    Bool()
  //    //SInt(3 bits)
  //  )
  //  //SnowHouseShouldIgnoreInstrState()
  //)
  //val rShouldIgnoreInstrState = {
  //  val temp = RegNext(nextShouldIgnoreInstrState)
  //  //init(
  //  //  nextShouldIgnoreInstrState.getZero
  //  //  //SnowHouseShouldIgnoreInstrState.Idle
  //  //)
  //  temp.foreach(item => {
  //    item.init(
  //      item.getZero
  //      //-1
  //    )
  //  })
  //  temp
  //}
  ////for (idx <- 0 until rShouldIgnoreInstrState.size) {
  //  nextShouldIgnoreInstrState := rShouldIgnoreInstrState
  ////}
  //io.pcChangeState := (
  //  RegNext(io.pcChangeState)
  //  init(
  //    io.pcChangeState.getZero
  //    //SnowHouseShouldIgnoreInstrState.Idle
  //    //U"1'b1".resized
  //  )
  //)

  //io.multiCycleOpInfoIdx := 0x0
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
    //println(
    //  "NOTICE: we do have cfg.allMainLdstUseGprPlusImm"
    //)
    io.dbusHostPayload.addr(
      io.dbusHostPayload.addr.high
      downto cfg.mainAddrWidth
    ) := 0x0
    io.dbusHostPayload.addr(cfg.mainAddrWidth - 1 downto 0) := (
      io.rdMemWord(0)(cfg.mainAddrWidth - 1 downto 0)
      + io.imm(1)(cfg.mainAddrWidth - 1 downto 0)
    )
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
  io.dbusHostPayload.myLcvDbusByteSize := (
    io.inpDecodeExt(0).memAccessLcvDbusByteSize
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
  io.outpDecodeExt.memAccessLcvDbusByteSize := (
    io.inpDecodeExt(1).memAccessLcvDbusByteSize
  )
  io.outpDecodeExt.memAccessIsPush := False
  io.outpDecodeExt.opIsJmp := False
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
            require(
              opInfo.cond == CondKind.Always,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            opInfo.memAccess match {
              case MemAccessKind.NoMemAccess => {
                require(
                  opInfo.dstArr.size == 1,
                  s"invalid opInfo.dstArr.size: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                require(
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
                    //if (!cfg.useLcvDataBus) {
                      if (!cfg.optScoreboard) {
                        io.modMemWordValid.foreach(current => {
                          current := True
                        })
                      }
                    //} else {
                    //  io.modMemWordValid.foreach(current => {
                    //    current := False
                    //  })
                    //}
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
                    
                    io.dbusHostPayload.addr(
                      io.dbusHostPayload.addr.high
                      downto cfg.mainAddrWidth
                    ) := 0x0
                    //io.dbusHostPayload.addr(
                    //  cfg.mainAddrWidth - 1 downto 0
                    //) := (
                    //  io.rdMemWord(0)(cfg.mainAddrWidth - 1 downto 0)
                    //  + io.imm(1)(cfg.mainAddrWidth - 1 downto 0)
                    //)
                    io.dbusHostPayload.addr(
                      cfg.mainAddrWidth - 1 downto 0
                    ) := (
                      (
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
                      )(
                        cfg.mainAddrWidth - 1 downto 0
                      )
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
              || (
                opInfo.dstArr.size == 2
                && (
                  opInfo.srcArr.size == 1
                  || opInfo.srcArr.size == 2
                )
              )
              || opInfo.dstArr.size == 3,
              s"not yet implemented: "
              + s"opInfo(${opInfo}) index:${opInfoIdx}"
            )
            assert(
              opInfo.srcArr.size == 1
              || (
                opInfo.dstArr.size == 2
                && (
                  opInfo.srcArr.size == 1
                  || opInfo.srcArr.size == 2
                )
              ),
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
              if (
                opInfo.dstArr.size == 1
              ) {
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
                  when (
                    RegNextWhen(
                      (
                        !io.shouldIgnoreInstr.last
                        //&& io.splitOp.opIsDualWidth
                      ),
                      cond=io.upIsFiring,
                      init=False
                    )
                    && io.splitOp.opIsDualWidth
                  ) {
                    nextIra := (
                      //io.regPc
                      //RegNextWhen(
                      //  io.irqIraRegPc.resize(nextIra.getWidth),
                      //  cond=io.upIsFiring,
                      //  init=nextIra.getZero
                      //)
                      io.irqIraRegPc.last.resize(nextIra.getWidth)
                    )
                  } otherwise {
                    //nextIra := 
                    nextIra := (
                      io.irqIraRegPc.head.resize(nextIra.getWidth)
                    )
                  }
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
                  //if (
                  //  opInfo.dstArr.size == 2
                  //  && opInfo.srcArr.size == 2
                  //) {
                  //  def myPcRange = (
                  //    io.psExSetPc.branchTgtBufElem.dstRegPc.high
                  //    downto log2Up(cfg.instrSizeBytes)
                  //  )
                  //  io.modMemWord(0) := (
                  //    (
                  //      if (cfg.optShiftRegPcImmAddend) (
                  //        Cat(
                  //          io.rdMemWord(0)(myPcRange) + io.imm(0), //+ 1,
                  //          U(s"${log2Up(cfg.instrSizeBytes)}'d0"),
                  //        ).asUInt
                  //      ) else (
                  //        io.rdMemWord(0) + io.imm(0)
                  //        - cfg.instrSizeBytes
                  //        //+ (
                  //        //  2 * cfg.instrSizeBytes
                  //        //)
                  //      )
                  //    ).resize(io.modMemWord(0).getWidth)
                  //  )
                  //} else {
                    io.modMemWord(0) := (
                      //io.regPc + ((cfg.instrMainWidth / 8) * 1)
                      io.regPcPlusInstrSize.resize(
                        io.modMemWord(0).getWidth
                      )
                    )
                  //}
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
              nextExSetPcValid(enumExSetPcValidOther) := True
              //--------
              //myPsExSetPcCmpEq.rValid := (
              //  False
              //  //0x0
              //)
              //myPsExSetPcCmpNe.rValid := (
              //  False
              //  //0x0
              //)
              //--------
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
                nextExSetPcValid(enumExSetPcValidOther) := True
                //myPsExSetPcCmpEq.rValid := (
                //  False
                //  //0x0
                //)
                //myPsExSetPcCmpNe.rValid := (
                //  False
                //  //0x0
                //)
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
                      io.regPcPlusInstrSize.resize(
                        io.modMemWord(0).getWidth
                      )
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
                  nextExSetPcValid(enumExSetPcValidOther) := (
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
                  nextExSetPcValid(enumExSetPcValidOther) := (
                    io.rdMemWord(io.brCondIdx(0))
                    === io.rdMemWord(io.brCondIdx(1))
                  )
                  //when (!io.shouldIgnoreInstr(2)) {
                  //  myPsExSetPcCmpEq.rValid := (
                  //    True
                  //    //!myPsExSetPcCmpEq.rValid
                  //    //&& !io.shouldIgnoreInstr(2)
                  //  )
                  //}

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
                  //myPsExSetPcCmpNe.rValid := (
                  //  False
                  //)
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
                  nextExSetPcValid(enumExSetPcValidOther) := (
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

                  nextExSetPcValid(enumExSetPcValidOther) := (
                    io.rdMemWord(io.brCondIdx(0))
                    =/= io.rdMemWord(io.brCondIdx(1))
                  )
                  //myPsExSetPcCmpEq.rValid := (
                  //  False
                  //  //0x0
                  //)
                  //when (!io.shouldIgnoreInstr(2)) {
                  //  myPsExSetPcCmpNe.rValid := (
                  //    True
                  //    //!myPsExSetPcCmpNe.rValid
                  //    //&& !io.shouldIgnoreInstr(2)
                  //    //True
                  //    //~LcvFastCmpEq(
                  //    //  left=RegNext/*When*/(
                  //    //    next=io.rdMemWord(io.brCondIdx(0)),
                  //    //    //cond=io.upIsFiring,
                  //    //    init=io.rdMemWord(io.brCondIdx(0)).getZero,
                  //    //  ),
                  //    //  right=RegNext/*When*/(
                  //    //    next=io.rdMemWord(io.brCondIdx(1)),
                  //    //    //cond=io.upIsFiring,
                  //    //    init=io.rdMemWord(io.brCondIdx(1)).getZero,
                  //    //  ),
                  //    //  //left=io.rdMemWord(io.brCondIdx(0)),
                  //    //  //right=io.rdMemWord(io.brCondIdx(1)),
                  //    //  optDsp=true,
                  //    //  //optReg=true,
                  //    //)._2
                  //  )
                  //}
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
                nextExSetPcValid(enumExSetPcValidOther) := (
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
                nextExSetPcValid(enumExSetPcValidOther) := (
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
                nextExSetPcValid(enumExSetPcValidOther) := (
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
                nextExSetPcValid(enumExSetPcValidOther) := (
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
                  nextExSetPcValid(enumExSetPcValidCond) := (
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
                  nextExSetPcValid(enumExSetPcValidCond) := {
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
                  nextExSetPcValid(enumExSetPcValidCond) := (
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
                  nextExSetPcValid(enumExSetPcValidCond) := {
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
                  nextExSetPcValid(enumExSetPcValidCond) := (
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
                  nextExSetPcValid(enumExSetPcValidCond) := {
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
                  nextExSetPcValid(enumExSetPcValidCond) := (
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
                  nextExSetPcValid(enumExSetPcValidCond) := {
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
                  nextExSetPcValid(enumExSetPcValidCond) := (
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
                  nextExSetPcValid(enumExSetPcValidCond) := {
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
                  nextExSetPcValid(enumExSetPcValidCond) := (
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
                  nextExSetPcValid(enumExSetPcValidCond) := {
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
                  nextExSetPcValid(enumExSetPcValidCond) := (
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
                  nextExSetPcValid(enumExSetPcValidCond) := {
                    (
                      (
                        io.rdMemWord(io.brCondIdx(0)).asSInt
                        > io.rdMemWord(io.brCondIdx(1)).asSInt
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
                  nextExSetPcValid(enumExSetPcValidCond) := (
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
                  nextExSetPcValid(enumExSetPcValidCond) := {
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
                nextExSetPcValid(enumExSetPcValidOther) := (
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
                nextExSetPcValid(enumExSetPcValidOther) := (
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
            //      //    io.aluOp,
            //      //    init=LcvAluDel1InpOpEnum.ZERO_UINT,
            //      //  )
            //      //)
            //    }
            //    io.aluModMemWordValid(idx) := (
            //      RegNext(
            //        io.aluModMemWordValid(idx),
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
        if (!cfg.havePsExStall) {
          return
        }
        for (
          ((group, innerMap), groupIdx)
          <- cfg.multiCycleOpInfoMap.view.zipWithIndex
        ) {
          for (
            ((_, innerOpInfo), kindIdx) <- innerMap.view.zipWithIndex
          ) {
            if (opInfo == innerOpInfo) {
              //if (!isSingleWriteToIds) {
              //  io.multiCycleOpInfoIdx := groupIdx
              //}
              for ((dst, dstIdx) <- opInfo.dstArr.view.zipWithIndex) {
                val tempDst = (
                  //modIo.multiCycleBusVec(idx).recvData.dstVec(dstIdx)
                  io.multiCycleBusRecvDataVec(groupIdx).dstVec(dstIdx)
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
                      case HiddenRegKind.DontCare => {
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
  //  //    io.dbusHostPayload.addr,
  //  //    init=io.dbusHostPayload.addr.getZero,
  //  //  )
  //  //}
  //}
  def doHandleSetNextPcEtc(
    mySwitchArgIsRnw: Boolean
  ): Unit = {
    switch (
      if (mySwitchArgIsRnw) (
        RegNextWhen(
          io.splitOp.exSetNextPcKind,
          cond=io.upIsFiring,
          init=io.splitOp.exSetNextPcKind.getZero
        )
      ) else (
        io.splitOp.exSetNextPcKind
      )
    ) {
      //is (SnowHousePsExSetNextPcKind.PcPlusImm) {
      //}
      is (SnowHousePsExSetNextPcKind.Dont) {
        if (mySwitchArgIsRnw) {
          io.psExSetPc.branchKind := (
            RegNext/*When*/(
              io.psExSetPc.branchKind,
              //cond=io.upIsFiring,
              init=io.psExSetPc.branchKind.getZero,
            )
          )
          io.psExSetPc.nextPc := (
            //io.regPcPlusImm 
            RegNext/*When*/(
              io.psExSetPc.nextPc,
              //cond=io.upIsFiring,
              init=io.psExSetPc.nextPc.getZero,
            )
          )
          io.psExSetPc.branchTgtBufElem.valid := (
            False
          )
          io.psExSetPc.branchTgtBufElem.dstRegPc := (
            RegNext/*When*/(
              io.psExSetPc.branchTgtBufElem.dstRegPc,
              //cond=io.upIsFiring,
              init=io.psExSetPc.branchTgtBufElem.dstRegPc.getZero,
            )
          )
        } else {
          rMyTempDstRegPc.valid := False
        }
        //io.psExSetPc.branchTgtBufElem.srcRegPc := (
        //  RegNext(
        //    io.psExSetPc.branchTgtBufElem.srcRegPc,
        //    init=io.psExSetPc.branchTgtBufElem.srcRegPc.getZero,
        //  )
        //)
      }
      is (SnowHousePsExSetNextPcKind.PcPlusImm) {
        if (mySwitchArgIsRnw) {
          io.psExSetPc.branchKind := (
            RegNext/*When*/(
              io.branchKind,
              //cond=io.upIsFiring,
              init=io.psExSetPc.branchKind.getZero,
            )
          )
          io.psExSetPc.nextPc := (
            (
              io.psExSetPc.branchTgtBufElem.dstRegPc
              - cfg.instrSizeBytes
            )
          )
          io.psExSetPc.branchTgtBufElem.valid := (
            True
          )
          io.psExSetPc.branchTgtBufElem.dstRegPc := (
            rMyTempDstRegPc.payload(1)
          )
        } else {
          def myDstPcRange = (
            rMyTempDstRegPc.payload(1).high
            downto log2Up(cfg.instrSizeBytes)
          )
          rMyTempDstRegPc.valid := (
            //True
            io.upIsFiring
            && !io.shouldIgnoreInstr.last
          )
          rMyTempDstRegPc.payload.foreach(payload => {
            payload := 0x0
            payload(myDstPcRange) := (
              //RegNext/*When*/(
                (
                  if (cfg.optShiftRegPcImmAddend) (
                    io.laggingRegPcPlus1InstrSize(myDstPcRange)
                    + (
                      io.imm.last //- cfg.instrSizeBytes
                    )
                  ) else (
                    io.laggingRegPc(myDstPcRange)
                      + io.imm.last(
                        io.imm.last.high
                        downto log2Up(cfg.instrSizeBytes)
                      )
                      //- 1 // RISC-V stuff here
                  )
                ).resize(
                  payload(
                    myDstPcRange
                  ).getWidth
                )//,
              //  //cond=io.upIsFiring,
              //  init=rMyTempDstRegPc(
              //    myDstPcRange
              //  ).getZero,
              //)
            )
          })
        }
        //def mySrcPcRange = (
        //  io.psExSetPc.branchTgtBufElem.srcRegPc.high
        //  downto log2Up(cfg.instrSizeBytes)
        //)
        //io.psExSetPc.branchTgtBufElem.srcRegPc := 0x0
        //io.psExSetPc.branchTgtBufElem.srcRegPc(mySrcPcRange) := (
        //  RegNext(
        //    (
        //      //io.laggingRegPcPlus1InstrSize(mySrcPcRange) + io.imm.last
        //      io.laggingRegPc(mySrcPcRange)
        //    ).resize(
        //      io.psExSetPc.branchTgtBufElem.srcRegPc(
        //        mySrcPcRange
        //      ).getWidth
        //    ),
        //    init=io.psExSetPc.branchTgtBufElem.srcRegPc(
        //      mySrcPcRange
        //    ).getZero,
        //  )
        //)
      }
      is (SnowHousePsExSetNextPcKind.RdMemWord) {
        if (mySwitchArgIsRnw) {
          io.psExSetPc.branchKind := (
            RegNext/*When*/(
              io.psExSetPc.branchKind,
              //cond=io.upIsFiring,
              init=io.psExSetPc.branchKind.getZero,
            )
          )
          io.psExSetPc.nextPc := (
            //RegNext(
            //  io.rdMemWord(io.jmpAddrIdx) (
            //    cfg.mainAddrWidth - 1 downto 0
            //  )
            //  //- (1 * cfg.instrSizeBytes)
            //  - (3 * cfg.instrSizeBytes)
            //)
            //init(0x0)
            io.psExSetPc.branchTgtBufElem.dstRegPc
            - cfg.instrSizeBytes
          )
          io.psExSetPc.branchTgtBufElem.valid := (
            //False
            True
          )
          io.psExSetPc.branchTgtBufElem.dstRegPc := (
            rMyTempDstRegPc.payload(2)
            //RegNext(
            //  io.rdMemWord(io.jmpAddrIdx)(
            //    cfg.mainAddrWidth - 1 downto 0
            //  ),
            //  init=io.psExSetPc.branchTgtBufElem.dstRegPc.getZero,
            //)
          )
        } else {
          rMyTempDstRegPc.valid := (
            //True
            io.upIsFiring
            && !io.shouldIgnoreInstr.last
            //!io.shouldIgnoreInstr.last
          )
          rMyTempDstRegPc.payload.foreach(payload => {
            payload := (
              //RegNext/*When*/(
                io.rdMemWord(io.jmpAddrIdx)(
                  cfg.mainAddrWidth - 1 downto 0
                )//,
              //  //cond=io.upIsFiring,
              //  init=io.psExSetPc.branchTgtBufElem.dstRegPc.getZero,
              //)
            )
          })
        }
        //io.psExSetPc.branchTgtBufElem.srcRegPc := (
        //  RegNext(
        //    io.psExSetPc.branchTgtBufElem.srcRegPc,
        //    init=io.psExSetPc.branchTgtBufElem.srcRegPc.getZero,
        //  )
        //)
      }
      is (SnowHousePsExSetNextPcKind.RdMemWordPlusImm) {
        if (mySwitchArgIsRnw) {
          io.psExSetPc.branchKind := (
            RegNext/*When*/(
              io.psExSetPc.branchKind,
              //cond=io.upIsFiring,
              init=io.psExSetPc.branchKind.getZero,
            )
          )
          io.psExSetPc.nextPc := (
            //RegNext(
            //  io.rdMemWord(io.jmpAddrIdx) (
            //    cfg.mainAddrWidth - 1 downto 0
            //  )
            //  //- (1 * cfg.instrSizeBytes)
            //  - (3 * cfg.instrSizeBytes)
            //)
            //init(0x0)
            io.psExSetPc.branchTgtBufElem.dstRegPc
            - cfg.instrSizeBytes
          )
          io.psExSetPc.branchTgtBufElem.valid := (
            //False
            True
          )
          io.psExSetPc.branchTgtBufElem.dstRegPc := (
            rMyTempDstRegPc.payload(3)
          )
        } else {
          rMyTempDstRegPc.valid := (
            //True
            io.upIsFiring
            && !io.shouldIgnoreInstr.last
            //!io.shouldIgnoreInstr.last
          )
          rMyTempDstRegPc.payload.foreach(payload => {
              payload := (
              //RegNext/*When*/(
                (
                  if (cfg.optShiftRegPcImmAddend)(
                    io.rdMemWord(io.jmpAddrIdx)(
                      cfg.mainAddrWidth - 1
                      downto log2Up(cfg.instrSizeBytes)
                    )
                    + (
                      io.imm.last
                      //- cfg.instrSizeBytes
                    )
                  ) else (
                    io.rdMemWord(io.jmpAddrIdx)(
                      cfg.mainAddrWidth - 1 downto 0
                    )
                    + (
                      io.imm.last
                      //- cfg.instrSizeBytes
                    )
                  )
                )(
                  payload.bitsRange
                ),
              //  //cond=io.upIsFiring,
              //  init=rMyTempDstRegPc.payload.getZero,
              //)
            )
          })
        }
        //io.psExSetPc.branchTgtBufElem.srcRegPc := (
        //  RegNext(
        //    io.psExSetPc.branchTgtBufElem.srcRegPc,
        //    init=io.psExSetPc.branchTgtBufElem.srcRegPc.getZero,
        //  )
        //)
      }
      is (SnowHousePsExSetNextPcKind.Ira) {
        if (mySwitchArgIsRnw) {
          io.psExSetPc.branchKind := (
            RegNext/*When*/(
              io.psExSetPc.branchKind,
              //cond=io.upIsFiring,
              init=io.psExSetPc.branchKind.getZero,
            )
          )
          io.psExSetPc.nextPc := (
            //RegNext(
            //  io.rIra(
            //    cfg.mainAddrWidth - 1 downto 0
            //  )
            //  - (3 * cfg.instrSizeBytes)
            //)
            //init(0x0)
            io.psExSetPc.branchTgtBufElem.dstRegPc
            - cfg.instrSizeBytes
          )
          io.psExSetPc.branchTgtBufElem.valid := (
            False
          )
          io.psExSetPc.branchTgtBufElem.dstRegPc := (
            RegNext/*When*/(
              io.rIra(
                cfg.mainAddrWidth - 1 downto 0
              ),
              //cond=io.upIsFiring,
              init=io.psExSetPc.branchTgtBufElem.dstRegPc.getZero,
            )
            //(io.psExSetPc.branchTgtBufElem.dstRegPc.bitsRange)
          )
        } else {
          rMyTempDstRegPc.valid := False
        }
        //io.psExSetPc.branchTgtBufElem.srcRegPc := (
        //  RegNext(
        //    io.psExSetPc.branchTgtBufElem.srcRegPc,
        //    init=io.psExSetPc.branchTgtBufElem.srcRegPc.getZero,
        //  )
        //)
      }
      is (SnowHousePsExSetNextPcKind.Ids) {
        if (mySwitchArgIsRnw) {
          io.psExSetPc.branchKind := (
            RegNext/*When*/(
              io.psExSetPc.branchKind,
              //cond=io.upIsFiring,
              init=io.psExSetPc.branchKind.getZero,
            )
          )
          io.psExSetPc.nextPc := (
            //RegNext(
            //  io.rIds(
            //    cfg.mainAddrWidth - 1 downto 0
            //  )
            //  - (3 * cfg.instrSizeBytes)
            //)
            //init(0x0)
            io.psExSetPc.branchTgtBufElem.dstRegPc
            - cfg.instrSizeBytes
          )
          io.psExSetPc.branchTgtBufElem.valid := (
            False
          )
          io.psExSetPc.branchTgtBufElem.dstRegPc := (
            RegNext/*When*/(
              io.rIds(
                cfg.mainAddrWidth - 1 downto 0
              ),
              //cond=io.upIsFiring,
              init=io.psExSetPc.branchTgtBufElem.dstRegPc.getZero,
            )
          )
        } else {
          rMyTempDstRegPc.valid := False
        }
        //io.psExSetPc.branchTgtBufElem.srcRegPc := (
        //  RegNext(
        //    io.psExSetPc.branchTgtBufElem.srcRegPc,
        //    init=io.psExSetPc.branchTgtBufElem.srcRegPc.getZero,
        //  )
        //)
      }
      //default {
      //  io.psExSetPc.nextPc := (
      //    io.regPcPlusImm 
      //  )
      //}
    }
  }
  if (cfg.onlyOneMultiCycleWriteToIdsOpInfo != None) {
    for ((_, innerMap) <- cfg.multiCycleOpInfoMap.view) {
      for (
        ((_, opInfo), idx)
        <- innerMap.zipWithIndex
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
  }
  if (cfg.maxMultiCycleOpKindWidth == None) {
    switch (
      io.inMultiCycleOp
      ## (
        RegNext(io.splitOp.multiCycleOpGroup)
        init(0x1)
        // `init` value arbitrarily chosen,
        // but still set to something one-hot
      )
    ) {
      for ((group, innerMap) <- cfg.multiCycleOpInfoMap.view) {
        require(
          innerMap.size == 1
        )
        for (
          ((_, opInfo), idx)
          <- innerMap.view.zipWithIndex
        ) {
          /*when*/ is(
            //idx
            //io.splitOp.multiCycleOp(idx)
            new MaskedLiteral(
              value=(
                (1 << idx)
                | (1 << io.splitOp.multiCycleOpGroup.getWidth)
              ),
              careAbout=(
                (1 << idx)
                | ((1 << idx) - 1)
                | (1 << io.splitOp.multiCycleOpGroup.getWidth)
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
    }
  } else { // if (cfg.maxMultiCycleOpKindWidth != None)
    switch (
      io.inMultiCycleOp
      ## (
        RegNext(io.splitOp.multiCycleOpGroup)
        init(0x1)
        // `init` value arbitrarily chosen,
        // but still set to something one-hot
      )
      ## (
        RegNext(io.splitOp.multiCycleOpKind)
        init(0x0)
      )
    ) {
      for (
        ((group, innerMap), groupIdx)
        <- cfg.multiCycleOpInfoMap.view.zipWithIndex
      ) {
        for (
          ((_, opInfo), kindIdx) <- innerMap.view.zipWithIndex
        ) {
          val myGroupWidth = io.splitOp.multiCycleOpGroup.getWidth
          val myKindWidth = io.splitOp.multiCycleOpKind.getWidth
          val myTempWidthSum = (
            //io.splitOp.multiCycleOpGroup.getWidth
            //+ io.splitOp.multiCycleOpKind.getWidth
            myGroupWidth + myKindWidth
          )

          /*when*/ is(
            //idx
            //io.splitOp.multiCycleOp(idx)
            new MaskedLiteral(
              value=(
                //(1 << idx)
                //| (1 << io.splitOp.multiCycleOpGroup.getWidth)
                (1 << (groupIdx + myKindWidth))
                | kindIdx
                | (1 << myTempWidthSum)
              ),
              careAbout=(
                //(1 << idx)
                //| ((1 << idx) - 1)
                //| (1 << io.splitOp.multiCycleOpGroup.getWidth)
                (1 << (groupIdx + myKindWidth))
                | ((1 << (groupIdx + myKindWidth)) - 1)
                | (1 << myTempWidthSum)
              ),
              width=(
                1
                + cfg.multiCycleOpInfoMap.size 
                + cfg.maxMultiCycleOpKindWidth.get
              )
            )
          ) {
            innerFunc(
              opInfo=opInfo,
              opInfoIdx=kindIdx,
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
    }
  }

  if (cfg.myHaveZeroReg) {
    for (idx <- 0 until io.gprIsZeroVec.size) {
      when (io.gprIsZeroVec.last(0)) {
        io.modMemWord(0) := 0x0
      }
    }
  }
  doHandleSetNextPcEtc(false)
  doHandleSetNextPcEtc(true)
  io.psExSetPc.branchTgtBufElem.dontPredict.allowOverride
  io.psExSetPc.branchTgtBufElem.dontPredict := (
    //stickyTempPsExSetPcDontPredict
    False
  )
  when (
    tempBranchMispredictNotTaken
    //myBranchMispredictCond
  ) {
    io.psExSetPc.nextPc := (
      io.mySavedRegPcPlusInstrSize.head
    )
    io.psExSetPc.branchTgtBufElem.dstRegPc := (
      io.mySavedRegPcPlusInstrSize.last
    )
    io.psExSetPc.branchTgtBufElem.dontPredict := True
  }

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
    if (cfg.myHaveAluFlags) {
      nextAluFlags := io.rAluFlags 
    }
    nextIds := io.rIds
    nextIe := io.rIe
    nextIra := io.rIra
    nextIty := io.rIty
    nextSty := io.rSty
    nextHi := io.rHi
    nextLo := io.rLo
    nextIndexReg := io.rIndexReg
    nextMulHiOutp := io.rMulHiOutp
    nextDivHiOutp := io.rDivHiOutp
    nextModHiOutp := io.rModHiOutp
    if (io.haveRetIraState) {
      nextHadRetIra := io.rHadRetIra
    }
    io.opIsMemAccess.foreach(item => {
      item := False
    })
    io.opIsAnyMultiCycle := False
    io.opIsMultiCycle.foreach(item => {
      item := False
    })
  }

  for (
    //idx <- 0 until rShouldIgnoreInstrState.size
    idx <- 0 until io.regPcSetItCnt.size
  ) {
    when (
      io.regPcSetItCnt(idx)(0)
      && io.upIsValid
    ) {
      //io.shouldIgnoreInstr(idx) := False
      if (idx == 0) {
        if (io.haveRetIraState) {
          when (io.rHadRetIra) {
            nextIe := True
            nextHadRetIra := False
          }
        }
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
    //      (
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
  //when (
  //  //(
  //  //  rose(myPsExSetPcCmpEq.rValid)
  //  //  //|| (
  //  //  //  myPsExSetPcCmpEq.rValid
  //  //  //  && stable(myPsExSetPcCmpEq.rValid)
  //  //  //)
  //  //)
  //  //&& io.upIsValid
  //  //&& io.downIsReady
  //  //RegNext(
  //  //  io.upIsFiring,
  //  //  init=False
  //  //)
  //  //&& 
  //  rose(myPsExSetPcCmpEq.rValid)
  //) {
  //  myPsExSetPcCmpEq.myCmp.msb := (
  //    myPsExSetPcCmpEq.cmpEq
  //  )
  //} 
  //when (
  //  fell(myPsExSetPcCmpEq.rValid)
  //) {
  //  myPsExSetPcCmpEq.myCmp.msb := False
  //}
  ////otherwise {
  ////  myPsExSetPcCmpEq.myCmp.msb := (
  ////    False//myPsExSetPcCmpEq.cmpEq
  ////  )
  ////}
  //when (myPsExSetPcCmpEq.rValid) {
  //  when (
  //    io.shouldIgnoreInstr(3)
  //  ) {
  //    myPsExSetPcCmpEq.rValid := False
  //  }
  //  when (
  //    io.upIsFiring
  //    && !io.splitOp.jmpBrOpIsEq
  //  ) {
  //    myPsExSetPcCmpEq.rValid := False
  //  }
  //}
  //when (
  //  //rose(myPsExSetPcCmpNe.rValid)
  //  ////|| (
  //  ////  myPsExSetPcCmpNe.rValid
  //  ////  && stable(myPsExSetPcCmpNe.rValid)
  //  ////)
  //  rose(myPsExSetPcCmpNe.rValid)
  //) {
  //  myPsExSetPcCmpNe.myCmp.msb := (
  //    !myPsExSetPcCmpNe.cmpEq
  //  )
  //}
  //when (
  //  fell(myPsExSetPcCmpNe.rValid)
  //) {
  //  myPsExSetPcCmpNe.myCmp.msb := False
  //}
  //when (myPsExSetPcCmpNe.rValid) {
  //  when (io.shouldIgnoreInstr(3)) {
  //    myPsExSetPcCmpNe.rValid := False
  //  }
  //  when (
  //    io.upIsFiring
  //    && !io.splitOp.jmpBrOpIsNe
  //  ) {
  //    myPsExSetPcCmpNe.rValid := False
  //  }
  //}
}

case class SnowHousePipeStageExecute(
  args: SnowHousePipeStageArgs,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
  doModInMid0FrontParams: PipeRegFileDoModInMid0FrontFuncParams[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeRegFileDualRdTypeDisabled[UInt, Bool],
  ],
  myModMemWord: SInt,
  psWbToEarlierStallRequest: Bool,
  myLcvDbusH2dStm: Stream[LcvBusH2dPayload],
  multiCycleBusVec: Vec[LcvStallIo[
    MultiCycleHostPayload,
    MultiCycleDevPayload,
  ]],
  idsIraIrq: LcvStallIo[Bool, Bool],
  forFmaxRegFileWrPulseArr: Seq[
    Flow[
      PipeSimpleDualPortMemDrivePayload[
        UInt
      ]
    ]
  ],
) extends Area {
  def myDbusIo = args.myDbusIo
  def myDbus = myDbusIo.dbus
  def myDbusExtraReady = myDbusIo.dbusExtraReady
  def myDbusLdReady = myDbusIo.dbusLdReady
  def cfg = args.cfg
  def io = args.io

  val nextPrevTxnWasHazard = (
    if (!cfg.optForFmax) (
      doModInMid0FrontParams.nextPrevTxnWasHazardVec(0)
    ) else (
      False
    )
  )
  //val rPrevTxnWasHazard = (
  //  doModInMid0FrontParams.rPrevTxnWasHazardVec(0)
  //)
  //val rPrevTxnWasHazardAny = (
  //  doModInMid0FrontParams.rPrevTxnWasHazardAny
  //)
  val outp = (
    //doModInMid0FrontParams.get.outp//Vec(ydx)
    if (!cfg.optForFmax) (
      doModInMid0FrontParams.outp
    ) else (
      SnowHousePipePayload(cfg=cfg)
    )
  )
  val inp = (
    //doModInMid0FrontParams.get.inp//Vec(ydx)
    if (!cfg.optForFmax) (
      doModInMid0FrontParams.inp
    ) else (
      SnowHousePipePayload(cfg=cfg)
    )
  )
  val cLink = (
    //doModInMid0FrontParams.get.cMid0Front
    if (!cfg.optForFmax) (
      doModInMid0FrontParams.cMid0Front
    ) else (
      args.link
    )
  )
  if (cfg.optForFmax) {
    inp := cLink.up(args.prevPayload)
    if (cfg.optScoreboard) {
      cLink.down(args.currPayload).allowOverride
      cLink.down(args.currPayload) := outp
    } else {
      cLink.up(args.currPayload) := outp
    }
  }
  val tempModFrontPayload = (
    if (!cfg.optForFmax) (
      doModInMid0FrontParams.tempModFrontPayload//Vec(ydxr
    ) else (
      outp.getZero
    )
  )
  if (cfg.optFormal) {
    if ((1 << outp.op.getWidth) != cfg.opInfoMap.size) {
      assume(inp.op < cfg.opInfoMap.size)
      assume(outp.op < cfg.opInfoMap.size)
    }
  }
  //def regFileFwd = doModInMid0FrontParams.myFwd //args.regFile
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
  if (cfg.havePsExStall) {
    for (
      ((_, opInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
    ) {
      psExStallHostArr += (
        cfg.mkLcvStallHost[MultiCycleHostPayload, MultiCycleDevPayload](
          stallIo=(
            Some(multiCycleBusVec(idx))
          ),
        )
      )
    }
  }

  val myTempDownIsReadyMostMost = (
    cLink.down.isReady
    //&& !psMemToEarlierStallRequest
  )
  val myTempDownIsReadyMost = (
    (
      if (!cfg.optForFmax) (
        myTempDownIsReadyMostMost
        //&& !outp.instrCnt.myPsIdBubble.last
        && !psWbToEarlierStallRequest
      ) else (
        myTempDownIsReadyMostMost
        //&& !outp.instrCnt.myPsIdBubble.last
      )
    )
  )
  val myTempDownIsReady = (
    myTempDownIsReadyMost
    //&& !myWaitFinishDuplDbusHostAddr
  )
  if (!cfg.optForFmax) {
    when (
      //psMemToEarlierStallRequest
      //|| 
      psWbToEarlierStallRequest
    ) {
      cLink.haltIt()
    }
  }
  //when (!myTempDownIsReady) {
  //  cMid0Front.duplicateIt()
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
        myShouldIgnoreInstr(idx),
        init=myShouldIgnoreInstr(idx).getZero,
      )
    )
  }
  outp := (
    RegNext(
      outp,
      init=outp.getZero,
    )
  )
  outp.allowOverride
  def myRdMemWord(
    ydx: Int,
    modIdx: Int,
  ) = (
    if (!cfg.optForFmax) (
      doModInMid0FrontParams.getMyRdMemWordFunc(ydx, modIdx)
    ) else (
      outp.myExt(ydx).rdMemWord(modIdx)
    )
  )
  //when (!io.ibus.ready) {
  //  cMid0Front.haltIt()
  //}
  when (cLink.up.isValid) {
    outp := inp
  }
  //when (cMid0Front.up.isFiring) {
  //  nextSetOutpState.foreach(current => {
  //    current := False
  //  })
  //}
  if (!cfg.optForFmax) {
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
  }

  case class MyFwdInfo(
  ) extends Bundle {
    //val valid = Bool()
    val data = UInt(cfg.mainWidth bits)
    //val addr = UInt(log2Up(cfg.regFileCfg.wordCountArr(0)) bits)
  }
  val myForFmaxFwdArea = (
    cfg.optForFmax
  ) generate (new Area {
    val myTempSaveOutpCond = (
      cLink.up.isFiring
      //&& outp.gprIsNonZeroVec.last.last
      && !outp.instrCnt.myPsIdBubble.last
      && (
        !outp.inpDecodeExt.last.opIsMemAccess.head
        //|| outp.instrCnt.myPsIdFwdBubble.head
        && !outp.instrCnt.myPsIdFwdBubble.head
      )
      //&& !myShouldIgnoreInstr.last
    )
    val myHistFwdInfo = {
      val temp = MyFwdInfo()
      //temp.valid := (
      //  outp.myExt(0).modMemWordValid.last //ram.io.wrEn
      //  && outp.gprIsNonZeroVec.last.last
      //  && !outp.splitOp.opIsMemAccess
      //  && !myShouldIgnoreInstr(0)
      //  //&& !outp.instrCnt.myPsIdBubble.last
      //  //&& (
      //  //  !outp.splitOp.opIsMemAccess
      //  //  || outp.outpDecodeExt.memAccessKind.asBits(1)
      //  //)
      //  && outp.gprIsNonZeroVec.last.last
      //  //&& (
      //  //  (
      //  //    //!myBranchMispredictEtc
      //  //    //&& 
      //  //    !rMyPsExSetPcState
      //  //    && !myBranchMispredictEtc
      //  //  )
      //  //  || outp.regPcSetItCnt(1).lsb
      //  //)
      //  && !outp.instrCnt.myPsIdBubble.head
      //  && (
      //    !outp.splitOp.opIsMemAccess
      //    //|| outp.outpDecodeExt.memAccessKind.asBits(1)
      //  )
      //)
      temp.data := outp.myExt(0).modMemWord //ram.io.wrData
      //temp.addr := outp.gprIdxVec.last
      History(
        that=temp,
        length=(
          cfg.optForFmaxPsExFwdSize
        ),
        when=(
          myTempSaveOutpCond
          //cLink.up.isFiring
          ////&& outp.gprIsNonZeroVec.last.last
          //&& !outp.instrCnt.myPsIdBubble.last
          //&& !outp.inpDecodeExt.last.opIsMemAccess.head
          //////////&& !outp.splitOp.memAccessKind.
          //////////&& !outp.splitOp.opIsMemAccess
          ////////&& (
          ////////  !outp.splitOp.opIsMemAccess
          ////////  //|| outp.outpDecodeExt.memAccessKind.asBits(1)
          ////////)
          //////&& outp.myExt(0).modMemWordValid.last //ram.io.wrEn
          ////&& outp.gprIsNonZeroVec.last.last
          ////&& !myShouldIgnoreInstr(0)
          //////&& (
          //////  //(
          //////  //  //!myBranchMispredictEtc
          //////  //  //&& 
          //////  //  !rMyPsExSetPcState
          //////  //  //&& !myBranchMispredictEtc
          //////  //)
          //////  //|| outp.regPcSetItCnt(1).lsb
          //////  !myShouldIgnoreInstr(0)
          //////)
          ////&& !outp.instrCnt.myPsIdBubble.last
          ////&& (
          ////  !outp.splitOp.opIsMemAccess
          ////  //|| outp.outpDecodeExt.memAccessKind.asBits(1)
          ////)
        ),
        init=temp.getZero
      )
    }
    //val myHistForFwdData = (
    //  History(
    //    that=(
    //      ram.io.wrData
    //    ),
    //    length=(
    //      cfg.optWrHistLength + 2,
    //    ),
    //    init=False
    //  )
    //)
    //val myTempHistFwdValid = Vec.fill(
    //  cfg.regFileCfg.modRdPortCnt
    //)(
    //  UInt(myHistFwdInfo.size - 1 bits)
    //)

    //val rSavedRegFileWrPulse = Vec.fill(forFmaxRegFileWrPulseArr.size)(
    //  Reg(
    //    cloneOf(forFmaxRegFileWrPulseArr.head),
    //    init=forFmaxRegFileWrPulseArr.head.getZero,
    //  )
    //)


    //val stickyRegFileWrPulseVec = Vec.fill(
    //  forFmaxRegFileWrPulseArr.size
    //)(
    //  cloneOf(forFmaxRegFileWrPulseArr.head)
    //)
    //stickyRegFileWrPulseVec(0) := (
    //  RegNext(
    //    stickyRegFileWrPulseVec(0),
    //    init=stickyRegFileWrPulseVec(0).getZero
    //  )
    //)
    //when (
    //  RegNext(
    //    (
    //      cLink.up.isFiring
    //      && stickyRegFileWrPulseVec(0).fire
    //    ),
    //    init=False
    //  )
    //) {
    //  stickyRegFileWrPulseVec(0).valid := (
    //    False
    //  )
    //}
    //when (forFmaxRegFileWrPulseArr(0).fire) {
    //  //rSavedRegFileWrPulse(jdx) := 
    //  stickyRegFileWrPulseVec(0) := (
    //    forFmaxRegFileWrPulseArr(0)
    //  )
    //}

    //val stickyFwdRegFileWrPulseVec = (
    //  Vec.fill(cfg.regFileCfg.modRdPortCnt)(
    //    Flow(cloneOf(forFmaxRegFileWrPulseArr(0).data))
    //  )
    //)
    //stickyFwdRegFileWrPulseVec := (
    //  RegNext(
    //    stickyFwdRegFileWrPulseVec,
    //    init=stickyFwdRegFileWrPulseVec.getZero
    //  )
    //)
    //for (idx <- 0 until cfg.regFileCfg.modRdPortCnt) {
    //  when (
    //    RegNext(
    //      //cLink.up.isFiring,
    //      (
    //        myTempSaveOutpCond
    //      ),
    //      init=False
    //    )
    //    && !myShouldIgnoreInstr.last
    //  ) {
    //    stickyFwdRegFileWrPulseVec(idx).valid := False
    //  }
    //  when (
    //    forFmaxRegFileWrPulseArr(0).fire
    //    && (
    //      forFmaxRegFileWrPulseArr(0).addr 
    //      === outp.gprIdxVec(idx)
    //    )
    //  ) {
    //    stickyFwdRegFileWrPulseVec(idx).valid := True
    //    stickyFwdRegFileWrPulseVec(idx).payload := (
    //      forFmaxRegFileWrPulseArr(0).data
    //    )
    //  }
    //}

    //for (idx <- 0 until cfg.regFileCfg.modRdPortCnt) {
    //  //when (
    //  //  forFmaxRegFileWrPulseArr(0).fire
    //  //  //&& (
    //  //  //  forFmaxRegFileWrPulseArr(0).addr 
    //  //  //  === outp.gprIdxVec(idx)
    //  //  //)
    //  //) {
    //  //  stickyRegFileWrPulse(idx) := forFmaxRegFileWrPulseArr(0)
    //  //}

    //  switch (
    //    (
    //      stickyRegFileWrPulse.fire
    //      && (
    //        stickyRegFileWrPulse.addr === outp.gprIdxVec(idx)
    //      )
    //    )
    //    ## (
    //      RegNext(upIsFiring, init=False)
    //      || rose(upIsValid)
    //    )
    //  ) {
    //    is (M"1-") {
    //      outp.myExt(0).rdMemWord(idx) := stickyRegFileWrPulse.data
    //    }
    //    is (M"01") {
    //      outp.myExt(0).rdMemWord(idx) := inp.myExt(0).rdMemWord(idx)
    //    }
    //    default {
    //      outp.myExt(0).rdMemWord(idx) := (
    //        RegNext(
    //          outp.myExt(0).rdMemWord(idx),
    //          init=outp.myExt(0).rdMemWord(idx).getZero,
    //        )
    //      )
    //    }
    //  }
    //}

    //val myHistRegFileWrPulse = (
    //  //!cfg.optScoreboard
    //  true
    //) generate (
    //  History(
    //    that=forFmaxRegFileWrPulseArr(0),
    //    when=forFmaxRegFileWrPulseArr(0).fire,
    //    length=(
    //      1
    //      //2
    //      //+ (if (cfg.optScoreboard) (1) else (0))
    //    ),
    //    init=forFmaxRegFileWrPulseArr(0).getZero,
    //  )
    //)

    for (jdx <- 0 until cfg.regFileCfg.modRdPortCnt) {

      //myTempHistFwdValid(jdx).lsb := False
      //for (idx <- 0 until myTempHistFwdValid(jdx).getWidth) {
      //  myTempHistFwdValid(jdx)(
      //    //myTempHistFwdValid(jdx).getWidth - idx - 1
      //    idx
      //  ) := (
      //    myHistFwdInfo(idx + 1).valid
      //    //&& (
      //    //  LcvFastCmpEq(
      //    //    left=outp.gprIdxVec(jdx),
      //    //    right=myHistFwdInfo(idx + 1).addr,
      //    //    cmpEqIo=null,
      //    //  )._1
      //    //)
      //  )
      //}

// >>> for idx in range(size):
// ...     print(idx, (("0" * (size - idx - 1))) + "1" + ("-" * idx))
// ...     
// 0 0001
// 1 001-
// 2 01--
// 3 1---

// >>> for idx in range(size):
// ...     print(idx, ("-" * (size - idx - 1) + "1" + ("0" * idx)))
// ...     
// 0 ---1
// 1 --10
// 2 -100
// 3 1000
      switch (
        //myTempHistFwdValid(jdx)
        outp.forFmaxFwdIdx(jdx)
      ) {
        for (
          idx
          //<- 0 until myTempHistFwdValid(jdx).getWidth
          //<- 0 until (1 << outp.optForFmaxFwdIdx(jdx).getWidth)
          <- 0 until cfg.optForFmaxPsExFwdSize //+ 1 //- 1
        ) {
          is (
            //MaskedLiteral({
            //  //("-" * idx)
            //  //+ "1"
            //  //+ (("0" * (myTempHistFwdValid(jdx).getWidth - idx - 1)))
            //  val size = (
            //    //myTempHistFwdValid(jdx).getWidth
            //    outp.myExt(0).fwdIdx(jdx).getWidth
            //  )
            //  ("-" * (size - idx - 1) + "1" + ("0" * idx))
            //})
            //idx + 1
            idx
          ) {
            //when (myTempHistFwdValid(jdx)(idx + 1)) {
            //outp.myExt(0).rdMemWord(jdx) := (
            //  if (idx == 0) {
            //    inp.myExt(0).rdMemWord(jdx)
            //  } else {
            //    myHistFwdInfo(
            //      //myHistFwdInfo.size - 1 - idx //(idx + 1)
            //      //idx + 1
            //      idx
            //    ).data
            //  }
            //)
            if (
              idx == 0
              //|| idx >= cfg.optForFmaxPsExFwdSize
            ) {
              //outp.myExt(0).rdMemWord(jdx) := (
              //  RegNext(
              //    outp.myExt(0).rdMemWord(jdx),
              //    init=outp.myExt(0).rdMemWord(jdx).getZero,
              //  )
              //)
              //val rSavedRegFileWrPulse = (
              //  RegNextWhen(
              //    forFmaxRegFileWrPulseArr(0),
              //    cond=forFmaxRegFileWrPulseArr(0).fire,
              //    init=forFmaxRegFileWrPulseArr(0).getZero
              //  )
              //)

              //val myFwdTempToSwitch = (
              //  //!cfg.optScoreboard
              //  true
              //) generate (
              //  Vec(Vec(
              //    myHistRegFileWrPulse.map(myWrPulse => (
              //      myWrPulse.fire
              //      && (
              //        outp.gprIdxVec(jdx)
              //        === myWrPulse.addr
              //      )
              //    ))
              //  ).reverse)
              //)

              //val myFwdTempToSwitch = Vec.fill(
              //  myFwdTempToSwitchReversed.size
              //)(
              //  Bool()
              //)
              //for (idx <- 0 until myFwdTempToSwitch.size) {
              //  myFwdTempToSwitch(idx) := (
              //    myFwdTempToSwitchReversed(
              //      myFwdTempToSwitch.size - idx - 1
              //    )
              //  )
              //}
              //if (cfg.optScoreboard) {
                switch (
                  (
                    forFmaxRegFileWrPulseArr(0).fire
                    && (
                      forFmaxRegFileWrPulseArr(0).addr
                      === outp.gprIdxVec(jdx)
                    )
                  )
                  ## (
                    (
                      RegNextWhen(
                        forFmaxRegFileWrPulseArr(0).addr,
                        cond=(
                          forFmaxRegFileWrPulseArr(0).fire
                        ),
                        init=forFmaxRegFileWrPulseArr(0).addr.getZero
                        //&& (
                        //  forFmaxRegFileWrPulseArr(0).addr
                        //  === outp.gprIdxVec(jdx)
                        //)
                      ) === outp.gprIdxVec(jdx)
                    )
                  )
                  //stickyFwdRegFileWrPulseVec(jdx).fire
                  ## (
                    cLink.up.isValid
                    //RegNext(
                    //  cLink.up.isFiring,
                    //  init=False
                    //)
                    //|| rose(
                    //  cLink.up.isValid
                    //)
                    //|| (
                    //  cLink.up.isValid
                    //  && fell(
                    //    myShouldIgnoreInstr.last
                    //  )
                    //)
                  )
                ) {
                  is (
                    //M"1-"
                    M"1--"
                  ) {
                    outp.myExt(0).rdMemWord(jdx) := (
                      forFmaxRegFileWrPulseArr(0).data
                      //stickyFwdRegFileWrPulseVec(jdx).payload
                    )
                  }
                  is (
                    M"01-"
                  ) {
                    outp.myExt(0).rdMemWord(jdx) := (
                      RegNextWhen(
                        forFmaxRegFileWrPulseArr(0).data,
                        cond=(
                          forFmaxRegFileWrPulseArr(0).fire
                        ),
                        init=forFmaxRegFileWrPulseArr(0).data.getZero
                      )
                    )
                  }
                  is (
                    //M"01"
                    M"001"
                  ) {
                    //if (idx == 0) {
                      outp.myExt(0).rdMemWord(jdx) := (
                        inp.myExt(0).rdMemWord(jdx)
                      )
                    //} else {
                    //  outp.myExt(0).rdMemWord(jdx) := (
                    //    outp.myPreFwdRdMemWord(jdx)
                    //  )
                    //}
                  }
                  default {
                    outp.myExt(0).rdMemWord(jdx) := (
                      RegNext(
                        outp.myExt(0).rdMemWord(jdx),
                        init=outp.myExt(0).rdMemWord(jdx).getZero
                      )
                    )
                  }
                }
                //outp.myExt(0).rdMemWord(jdx) := (
                //  inp.myExt(0).rdMemWord(jdx)
                //)
              //} 
              //else { // if (!cfg.optScoreboard)
              //  switch (
              //    //(
              //    //  forFmaxRegFileWrPulseArr(0).fire
              //    //  && (
              //    //    outp.gprIdxVec(jdx)
              //    //    === forFmaxRegFileWrPulseArr(0).addr
              //    //  )
              //    //)
              //    //## (
              //    //  rSavedRegFileWrPulse.fire
              //    //  && (
              //    //    outp.gprIdxVec(jdx)
              //    //    === rSavedRegFileWrPulse.addr
              //    //  )
              //    //)
              //    myFwdTempToSwitch
              //    ## (
              //      RegNext(
              //        cLink.up.isFiring,
              //        init=False
              //      )
              //      || rose(
              //        cLink.up.isValid
              //      )
              //    )
              //  ) {
              //    is ({
              //      var temp = "1--"
              //      //if (cfg.optScoreboard) {
              //      //  temp += "-"
              //      //}
              //      MaskedLiteral(temp)
              //    }) {
              //      outp.myExt(0).rdMemWord(jdx) := (
              //        myHistRegFileWrPulse(0).data
              //      )
              //    }
              //    is ({
              //      var temp = "01-"
              //      //if (cfg.optScoreboard) {
              //      //  temp += "-"
              //      //}
              //      MaskedLiteral(temp)
              //    }) {
              //      outp.myExt(0).rdMemWord(jdx) := (
              //        myHistRegFileWrPulse(1).data
              //      )
              //    }
              //    //if (cfg.optScoreboard) {
              //    //  is (M"001-") {
              //    //    outp.myExt(0).rdMemWord(jdx) := (
              //    //      myHistRegFileWrPulse(2).data
              //    //    )
              //    //  }
              //    //}
              //    is ({
              //      //var temp = "001"
              //      val temp = (
              //        //if (cfg.optScoreboard) (
              //        //  "0001"
              //        //) else (
              //          "001"
              //        //)
              //      )
              //      MaskedLiteral(temp)
              //    }) {
              //      outp.myExt(0).rdMemWord(jdx) := (
              //        inp.myExt(0).rdMemWord(jdx)
              //      )
              //    }
              //    default {
              //      outp.myExt(0).rdMemWord(jdx) := (
              //        RegNext(
              //          outp.myExt(0).rdMemWord(jdx),
              //          init=outp.myExt(0).rdMemWord(jdx).getZero,
              //        )
              //      )
              //    }
              //  }
              //}
              //when (
              //  RegNext(
              //    cLink.up.isFiring,
              //    init=False
              //  )
              //  || rose(
              //    cLink.up.isValid
              //  )
              //) {
              //  outp.myExt(0).rdMemWord(jdx) := (
              //    //Mux(
              //    //  (
              //    //    stickyRegFileWrPulseVec(0).fire
              //    //    && (
              //    //      outp.gprIdxVec(jdx) 
              //    //      === stickyRegFileWrPulseVec(0).addr
              //    //    )
              //    //    //&& forFmaxRegFileWrPulseArr(0).
              //    //  ),
              //    //  stickyRegFileWrPulseVec(0).data,
              //      inp.myExt(0).rdMemWord(jdx)
              //    //)
              //  )
              //}
              //when (
              //  forFmaxRegFileWrPulseArr(0).fire
              //  && (
              //    outp.gprIdxVec(jdx)
              //    === forFmaxRegFileWrPulseArr(0).addr
              //  )
              //) {
              //  outp.myExt(0).rdMemWord(jdx) := (
              //    forFmaxRegFileWrPulseArr(0).data
              //  )
              //} elsewhen (
              //  rSavedRegFileWrPulse.fire
              //  && (
              //    outp.gprIdxVec(jdx)
              //    === rSavedRegFileWrPulse.addr
              //  )
              //) {
              //  outp.myExt(0).rdMemWord(jdx) := (
              //    rSavedRegFileWrPulse.data
              //  )
              //}
            } else if (idx < cfg.optForFmaxPsExFwdSize) {
              //when (myHistFwdInfo(idx).valid) {
                outp.myExt(0).rdMemWord(jdx) := (
                  myHistFwdInfo(
                    //myHistFwdInfo.size - 1 - idx //(idx + 1)
                    idx //+ 1
                    //idx
                  ).data
                )
              //} otherwise {
              //  outp.myExt(0)
              //}
            } 
            //else {
            //  outp.myExt(0).rdMemWord(jdx) := (
            //    outp.myPreFwdRdMemWord(jdx)
            //  )
            //}
            //} otherwise {
            //}
          }
        }
        //default {
        //  outp.myExt(0).rdMemWord(jdx) := (
        //    inp.myExt(0).rdMemWord(jdx)
        //  )
        //}
      }
    }
  })

  //val savedPsMemStallHost = (
  //  LcvStallHostSaved(
  //    stallHost=psMemStallHost,
  //    someLink=cMid0Front,
  //  )
  //)
  def stallKindMem = 0
  //def stallKindPsWbToEx = 1
  //def stallKindMultiCycle = 1
  //def stallKindMultiCycle1 = 2
  //def stallKindAluShift = 1
  def stallKindLim = (
    //3
    //2
    //if (!cfg.useLcvDataBus) (
      1
    //) else (
    //  2
    //)
  )

  val myDoStall = (
    /*KeepAttribute*/(
      Vec.fill(stallKindLim)(
        Bool()
      )
    )
  )
  myDoStall(stallKindMem) := False
  //if (cfg.useLcvDataBus) {
  //  myDoStall(stallKindPsWbToEx) := psWbToEarlierStallRequest
  //}
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
    //args=args
    cfg=cfg
  )
  setOutpModMemWord.io.instrCnt := outp.instrCnt
  for (
    ((group, innerMap), groupIdx)
    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    for (
      //(dst, dstIdx) <- opInfo.dstArr.view.zipWithIndex
      dstIdx <- 0 until cfg.maxMultiCycleDstArrSizeMap.get(group).get
    ) {
      if (cfg.havePsExStall) {
        val tempDst = (
          //modIo.multiCycleBusVec(idx).recvData.dstVec(dstIdx)
          setOutpModMemWord.io.multiCycleBusRecvDataVec(
            //idx
            groupIdx
          ).dstVec(dstIdx)
        )
        tempDst := (
          multiCycleBusVec(
            //idx
            groupIdx
          ).recvData.dstVec(dstIdx)
        )
      }
      if (cfg.havePsWbMultiCycleStall) {
      }
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
  //val myNonLcvDbusTempArea = (
  //  !cfg.useLcvDataBus
  //) generate (new Area{
  //  setOutpModMemWord.io.irqIraRegPc := outp.irqIraRegPc.head
  //})
  //val myLcvDusTempArea = (
  //  cfg.useLcvDataBus
  //) generate (new Area {
  //  //val mySeenDownFire = Bool()
  //  //val rSavedSeenDownFire
  //  val rStallState = Reg(Bool(), init=False)
  //  setOutpModMemWord.io.irqIraRegPc := outp.irqIraRegPc.head
  //  when (cMid0Front.up.isValid) {
  //    when (
  //      outp.myDoHaveHazardAddrCheckVec(0)
  //      && RegNextWhen(
  //        (
  //          setOutpModMemWord.io.opIsMemAccess.last
  //          && !outp.outpDecodeExt.memAccessKind.asBits(1)
  //        ),
  //        cond=cMid0Front.up.isFiring,
  //        init=False,
  //      )
  //    ) {
  //      setOutpModMemWord.io.irqIraRegPc := outp.irqIraRegPc.last
  //      when (!rStallState) {
  //        cMid0Front.duplicateIt()
  //        myShouldIgnoreInstr.foreach(item => {
  //          item := True
  //        })
  //        // TODO: need to insert a bubble here
  //        when (cMid0Front.down.isFiring) {
  //          rStallState := True
  //        }
  //      }
  //    } otherwise {
  //      //setOutpModMemWord.io.irqIraRegPc := outp.irqIraRegPc.head
  //    }
  //  }
  //  when (rose(rStallState)) {
  //    myShouldIgnoreInstr.foreach(item => {
  //      item := False
  //    })
  //  }
  //  when (cMid0Front.up.isFiring) {
  //    rStallState := False
  //    //myShouldIgnoreInstr.foreach(item => {
  //    //  item := False
  //    //})
  //  }
  //})
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
        next=idsIraIrq.nextValid,
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
      cond=cLink.up.isFiring,
      init=nextMyTakeIrq.getZero,
    )
  )
  if (cfg.irqCfg != None) {
    nextMyTakeIrq := rMyTakeIrq
    idsIraIrq.ready := False
    val tempCondNonLcvDbus = (
      //setOutpModMemWord.io.regPcSetItCnt(0)(0)
      //&& setOutpModMemWord.io.upIsValid
      //!setOutpModMemWord.io.shouldIgnoreInstr(0)
      //!shouldIgnoreInstr
      (
        (
          !myShouldIgnoreInstr(0)
          //&& cMid0Front.up.isValid
          //&& RegNextWhen(
          //  !outp.splitOp.opIsMemAccess,
          //  cond=cMid0Front.up.isFiring,
          //  init=False
          //)
          //&& !outp.splitOp.opIsMemAccess
          //&& myTempDownIsReady
        )
        || (
          cLink.up.isValid
          && myTempDownIsReady
          && setOutpModMemWord.io.regPcSetItCnt(0)(0)
        )
      )
      //|| (
      //  !myShouldIgnoreInstr(0)
      //  && cMid0Front.up.isValid
      //  //&& RegNextWhen(
      //  //  !outp.splitOp.opIsMemAccess,
      //  //  cond=cMid0Front.up.isFiring,
      //  //  init=False
      //  //)
      //  && !outp.splitOp.opIsMemAccess
      //  && myTempDownIsReady
      //)
    )
    val tempCond = (
      if (!cfg.useLcvDataBus) (
        tempCondNonLcvDbus
      ) else (
        tempCondNonLcvDbus
        && (
          !outp.instrCnt.myPsIdBubble.head
          //&& !io.lcvDbus.h2dBus.valid
        )
      )
    )
    val tempCond1 = (
      rMyTakeIrq
      && /*RegNext*/(
        /*next=*/cLink.up.isFiring//,
        //init=False,
      )
      //&& cMid0Front.up.isFiring
      //&& !setOutpModMemWord.io.psExSetPc.valid
      //&& !setOutpModMemWord.io.shouldIgnoreInstr
      && tempCondNonLcvDbus
      //&& psExSetPc.valid
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
        cLink.up.isFiring
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
      tempCond1
    ) {
      nextMyTakeIrq := False
      //idsIraIrq.ready := True
    }
    when (
      rose(
        RegNextWhen(
          tempCond1,
          cond=cLink.up.isFiring,
          init=tempCond1.getZero,
        )
      )
    ) {
      idsIraIrq.ready := True
    }
    when (
      RegNext(
        next=idsIraIrq.ready,
        init=idsIraIrq.ready.getZero,
      )
    ) {
      idsIraIrq.ready := False
    }
  }

  setOutpModMemWord.io.regPcSetItCnt := outp.regPcSetItCnt
  setOutpModMemWord.io.mySavedRegPcPlusInstrSize.head := (
    //outp.myHistRegPcPlusInstrSize.head
    //outp.laggingRegPcPlus1InstrSize
    //outp.laggingRegPc
    RegNextWhen(
      next=(
        //outp.branchTgtBufElem(1).srcRegPc
        outp.laggingRegPc
        //+ (1 * cfg.instrSizeBytes)
        - (1 * cfg.instrSizeBytes)
        //- (3 * cfg.instrSizeBytes)
      ),
      cond=(
        cLink.up.isFiring
        //cMid0Front.up.isValid
        //&& myTempDownIsReady
      ),
      init=(
        //outp.branchTgtBufElem(1).srcRegPc.getZero
        outp.laggingRegPc.getZero
      ),
    )
  )
  setOutpModMemWord.io.mySavedRegPcPlusInstrSize.last := (
    RegNextWhen(
      next=(
        //outp.branchTgtBufElem(1).srcRegPc
        outp.laggingRegPc
        + (1 * cfg.instrSizeBytes)
        //- (1 * cfg.instrSizeBytes)
        //- (3 * cfg.instrSizeBytes)
      ),
      cond=(
        cLink.up.isFiring
        //cMid0Front.up.isValid
        //&& myTempDownIsReady
      ),
      init=(
        //outp.branchTgtBufElem(1).srcRegPc.getZero
        outp.laggingRegPc.getZero
      ),
    )
    ////outp.myHistRegPcPlusInstrSize.head
    //outp.laggingRegPcPlus1InstrSize
    //outp.laggingRegPc
    //RegNextWhen(
    //  next=(
    //    //outp.branchTgtBufElem(1).srcRegPc
    //    outp.laggingRegPc
    //    //+ (2 * cfg.instrSizeBytes)
    //    //outp.laggingRegPcPlus1InstrSize
    //    //+ (1 * cfg.instrSizeBytes)
    //    //- (1 * cfg.instrSizeBytes)
    //    //- (3 * cfg.instrSizeBytes)
    //  ),
    //  cond=cMid0Front.up.isFiring,
    //  init=(
    //    //outp.branchTgtBufElem(1).srcRegPc.getZero
    //    outp.laggingRegPc.getZero
    //  ),
    //)
  )
  setOutpModMemWord.io.regPc := outp.regPc
  setOutpModMemWord.io.regPcPlusInstrSize := outp.regPcPlusInstrSize
  setOutpModMemWord.io.regPcPlusImm := (
    outp.regPcPlusImm
    - (1 * cfg.instrSizeBytes)
  )
  //setOutpModMemWord.io.regPcPlusImmRealDst := (
  //  outp.branchTgtBufElem(1).dstRegPc
  //)
  setOutpModMemWord.io.laggingRegPc := outp.laggingRegPc
  setOutpModMemWord.io.laggingRegPcPlus1InstrSize := (
    outp.laggingRegPcPlus1InstrSize
  )
  setOutpModMemWord.io.imm := outp.imm
  setOutpModMemWord.io.inpDecodeExt := outp.inpDecodeExt
  outp.outpDecodeExt := setOutpModMemWord.io.outpDecodeExt
  outp.psExSetPc := outp.psExSetPc.getZero
  //outp.psExSetPc := psExSetPc
  if (
    //io.haveMultiCycleBusVec
    cfg.havePsExStall
  ) {
    for (
      (multiCycleBus, busIdx) <- multiCycleBusVec.view.zipWithIndex
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
  setOutpModMemWord.io.upIsFiring := cLink.up.isFiring
  setOutpModMemWord.io.upIsValid := cLink.up.isValid
  setOutpModMemWord.io.upIsReady := cLink.up.isReady
  setOutpModMemWord.io.downIsFiring := cLink.down.isFiring
  setOutpModMemWord.io.downIsValid := cLink.down.isValid
  setOutpModMemWord.io.downIsReady := myTempDownIsReady //cMid0Front.down.isReady

  val alu = (
    !cfg.optForFmax
  ) generate (
    LcvAluDel1(
      wordWidth=cfg.mainWidth
    )
  )
  //--------
  // BEGIN: this worked pretty well for fmax, so let's try another approach
  val mostTempToSwitchMyModMemWord = (
    !cfg.optForFmax
  ) generate (
    //RegNext(
      (
        (
          RegNext(
            (
              cLink.up.isFiring
              && outp.myExt(0).modMemWordValid.head
              //&& setOutpModMemWord.io.modMemWordValid.head
              //&& alu.io.inp_op =/= LcvAluDel1InpOpEnum.OP_GET_INP_A
            ),
            init=False,
          )
        ) ## (
          RegNext(
            (
              //alu.io.inp_op === LcvAluDel1InpOpEnum.OP_GET_INP_A
              if (
                LcvAluDel1InpOpEnum.ZERO
                != (1 << (LcvAluDel1InpOpEnum.OP_WIDTH - 1))
              ) (
                // check for one-hot encoding
                alu.io.inp_op === LcvAluDel1InpOpEnum.ZERO
              ) else (
                alu.io.inp_op(log2Up(LcvAluDel1InpOpEnum.ZERO))
              )
            ),
            init=False,
          )
        )
      )
      //.asUInt,
      //init=U"2'b00"
    //)
  )
  val tempToSwitchMyModMemWord = (
    !cfg.optForFmax
  ) generate (
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
  if (!cfg.optForFmax) {
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
        when (RegNext(cLink.up.isFiring, init=False)) {
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
        when (RegNext(cLink.up.isFiring, init=False)) {
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
  }
  // END: this worked pretty well for fmax, so let's try another approach
  //--------

  //val myTempRdMemWord = cloneOf(setOutpModMemWord.io.rdMemWord)
  //val myTempRdMemWordCond = (
  //  Vec.fill(setOutpModMemWord.io.rdMemWord.size)(
  //    Bool()
  //  )
  //)

  def doFinishSetOutpModMemWord(
    ydx: Int,
    zdx: Int,
  ): Unit = {
    def tempExt = outp.myExt(ydx)
    if (
      //zdx == PipeRegFile.modWrIdx
      zdx == cfg.regFileCfg.modRdPortCnt
    ) {
      //when (cMid0Front.up.isFiring) {
        tempExt.modMemWord := (
          // TODO: support multiple output `modMemWord`s
          if (!cfg.optForFmax) (
            tempExt.modMemWord.getZero
          ) else (
            setOutpModMemWord.io.modMemWord(0)
          )
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
    } else {
      def tempRdMemWord = setOutpModMemWord.io.rdMemWord(zdx)
      val tempMyRdMemWord = myRdMemWord(ydx=ydx, modIdx=zdx)
      tempRdMemWord := tempMyRdMemWord

      if (!cfg.optForFmax) {
        if (zdx == 0) {
          alu.io.inp_a := (
            tempMyRdMemWord.asSInt
            //tempRdMemWord.asSInt
          )
          alu.io.inp_op := outp.aluOp
        } else if (zdx == 1) {
          alu.io.inp_b(0) := tempMyRdMemWord.asSInt
          alu.io.inp_b(1) := outp.imm.last.asSInt
          alu.io.inp_b_sel := outp.aluInpBIsImm
        }
      }

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
    require(
      false,
      "not yet implemented (in full)"
    )
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
  when (cLink.up.isFiring) {
    nextPrevTxnWasHazard := False
  }

  val myNonLcvDbusPartAArea = (
    !cfg.useLcvDataBus
  ) generate (new Area{
    setOutpModMemWord.io.irqIraRegPc := outp.irqIraRegPc
    //cMid0Front.up(outpPipePayloadA) := outp
    //doHandleMyDbusPartA()
    myDbus.nextValid := RegNext(myDbus.nextValid, init=False)
    when (RegNext(myDbus.nextValid, init=False)) {
      when (myDbus.ready) {
        myDbus.nextValid := False
        //myDoStall(stallKindMem) := False
      } otherwise {
        myDoStall(stallKindMem) := True
      }
    }
  })
  val myLcvDbusPartAArea = (
    cfg.useLcvDataBus
  ) generate (new Area {
    //val mySeenDownFire = Bool()
    //val rSavedSeenDownFire

    // TODO: implement `io.lcvDbus` stuff
    //val mySeenH2dFire = Bool()
    //val rSavedSeenH2dFire = Reg(Bool(), init=False)
    //val stickySeenH2dFire = (
    //  mySeenH2dFire || rSavedSeenH2dFire
    //)
    //when (mySeenH2dFire) {
    //  rSavedSeenH2dFire := True
    //}

    def myH2dBus = (
      //io.lcvDbus.h2dBus
      myLcvDbusH2dStm
    )
    def myDbusHostPayload = setOutpModMemWord.io.dbusHostPayload

    //outp.myDbusHostPayload := myDbusHostPayload
    //outp.myDbusHostPayload.src.allowOverride
    //outp.myDbusHostPayload.src := (
    //  (
    //    RegNext(outp.myDbusHostPayload.src.asSInt)
    //    init(-1)
    //  ).asUInt
    //  //myH2dBus.src
    //)
    //when (myH2dBus.fire) {
    //  outp.myDbusHostPayload.src := myH2dBus.src
    //}

    val rSeenH2dBusFire = (
      cfg.optScoreboard
    ) generate (
      Reg(Bool(), init=False)
    )
    myH2dBus.valid := (
      //RegNext(myH2dBus.valid, init=False)
      //False
      (
        if (cfg.optScoreboard) (
          cLink.up.isValid
          && myTempDownIsReady
          && !rSeenH2dBusFire
          //&& !outp.instrCnt.myPsIdBubble.head
        ) else (
          cLink.up.isValid
          && myTempDownIsReady
        )
      )
      && setOutpModMemWord.io.opIsMemAccess.last
      //&& cMid0Front.down.isReady
    )
    myH2dBus.byteSize := myDbusHostPayload.myLcvDbusByteSize
    myH2dBus.src.allowOverride
    myH2dBus.src := (
      (
        RegNextWhen(
          myH2dBus.src.asSInt + 1,
          cond=myH2dBus.fire,
          //init=myH2dBus.src.getZero,
        )
        init(-1)
      ).asUInt
    )
    myH2dBus.addr := myDbusHostPayload.addr
    myH2dBus.data := myDbusHostPayload.data
    myH2dBus.isWrite := myDbusHostPayload.accKind.asBits(1)

    if (cfg.optScoreboard) {
      cLink.down(args.currPayload).instrCnt.allowOverride
      //cLink.down(args.currPayload).outpDecodeExt.allowOverride
      //cLink.up(args.currPayload) := outp
    }
    val rInstrCntMem = (
      cfg.optScoreboard
    ) generate (
      Reg(cloneOf(outp.instrCnt.mem))
      init(0)
    )
    val rInstrCntNonMem = (
      cfg.optScoreboard
    ) generate (
      Reg(cloneOf(outp.instrCnt.nonMem))
      init(0)
    )
    if (cfg.optScoreboard) {
      outp.instrCnt.mem := rInstrCntMem
      outp.instrCnt.nonMem := rInstrCntNonMem
      when (
        !myH2dBus.valid
        && !rSeenH2dBusFire
        && cLink.up.isFiring
        && !outp.instrCnt.myPsIdBubble.head
      ) {
        rInstrCntNonMem := rInstrCntNonMem + 1
      }
    }
    when (
      if (cfg.optScoreboard) (
        myH2dBus.valid
        //&& myTempDownIsReady
        && !myH2dBus.ready
      ) else (
        myH2dBus.valid
        && !myH2dBus.ready
      )
    ) {
      if (cfg.optScoreboard) {
        cLink.duplicateIt()
        //outp.instrCnt.shouldIgnoreInstr.foreach(item => {
        //  item := True
        //})
        //cLink.down(args.currPayload) := outp
        //cLink.down(args.currPayload).instrCnt
        //cLink.down(args.currPayload).instrCnt.shouldIgnoreInstr.foreach(
        //  item => {
        //    item := True
        //  }
        //)
        //cLink.down(args.currPayload).instrCnt.myPsIdBubble.foreach(
        //  item => {
        //    item := True
        //  }
        //)
        cLink.down(args.currPayload).setAsBubbleMain(
          //Some(True)
          None
        )
        cLink.down(args.currPayload).instrCnt
        .myPsExMemAccessBubble.foreach(
          item => {
            item := True
          }
        )
        setOutpModMemWord.io.instrCnt.setAsPsIdBubbleMain()
        //setOutpModMemWord.io.instrCnt.myPsExMemAccessBubble.foreach(
        //  item => {
        //    item := True
        //  }
        //)
        //cLink.down(args.currPayload).outpDecodeExt.opIsMemAccess.foreach(
        //  item => {
        //    item := False
        //  }
        //)
      } else {
        myDoStall(stallKindMem) := True
      }
    } otherwise {
      //if (cfg.optScoreboard) {
      //  cLink.up(args.currPayload) := outp
      //}
    }
    when (myH2dBus.fire) {
      //rInstrCntMem.lsb := !rInstrCntMem.lsb
      if (cfg.optScoreboard) {
        rInstrCntMem := rInstrCntMem + 1
        rSeenH2dBusFire := True
        //cLink.down(args.currPayload).instrCnt.shouldIgnoreInstr := (
        //  myShouldIgnoreInstr
        //)
        //cLink.down(args.currPayload).instrCnt.shouldIgnoreInstr.foreach(
        //  item => {
        //    item := False
        //  }
        //)
      }
      //outp.instrCnt.shouldIgnoreInstr.foreach(item => {
      //  item := False
      //})
      //outp.instrCnt.shouldIgnoreInstr := (
      //  myShouldIgnoreInstr
      //)
      //if (cfg.optScoreboard) {
      //  cLink.down(args.currPayload).instrCnt.shouldIgnoreInstr.foreach(
      //    item => {
      //      item := False
      //    }
      //  )
      //}
      nextPrevTxnWasHazard := True
    }
    if (cfg.optScoreboard) {
      when (cLink.up.isFiring) {
        rSeenH2dBusFire := False
      }
    }

    setOutpModMemWord.io.irqIraRegPc := outp.irqIraRegPc
  })


  //val rSavedJmpCnt = {
  //  val temp = Reg(
  //    SnowHouseInstrCnt(cfg=cfg)
  //  )
  //  temp.init(temp.getZero)
  //  temp
  //}
  //val nextSetPcCnt = (
  //  Flow(UInt(
  //    cfg.mainWidth bits
  //  ))
  //)
  //val rSetPcCnt = {
  //  val temp = /*KeepAttribute*/(
  //    RegNext(next=nextSetPcCnt)
  //  )
  //  temp.valid.init(False)
  //  temp.payload.init(0x0)

  //  temp
  //}
  //nextSetPcCnt := rSetPcCnt
  psExSetPc.nextPc := (
    RegNext(
      next=psExSetPc.nextPc,
      init=psExSetPc.nextPc.getZero,
    )
  )
  //psExSetPc.valid1.allowOverride
  psExSetPc.taken.allowOverride
  psExSetPc.nextPc.allowOverride
  for (idx <- 0 until cfg.lowerMyFanoutRegPcSetItCnt) {
    setOutpModMemWord.io.shouldIgnoreInstr(idx) := (
      myShouldIgnoreInstr(idx)
    )
  }

  //pcChangeState.assignFromBits(
  //  setOutpModMemWord.io.pcChangeState.asBits
  //)

  val nextPsExSetPcTakenValid = (
    setOutpModMemWord.io.psExSetPc.taken.fire
    //&& RegNext(
    //  next=(
    //    !myShouldIgnoreInstr(0)
    //    //&& cMid0Front.up.isFiring
    //  ),
    //  init=False
    //)
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
          //&& cMid0Front.up.isFiring
        ),
        init=False
      )
      //rose(
      //  setOutpModMemWord.io.psExSetPc.valid
      //  && RegNext(
      //    !myShouldIgnoreInstr(0),
      //    init=False
      //  )
      //  //&& cMid0Front.up.isFiring
      //  && cMid0Front.up.isValid
      //  && cMid0Front.down.isReady
      //),
    )
  }

  //psExSetPc.valid := (
  //  RegNext(
  //    psExSetPc.valid, init=psExSetPc.valid.getZero
  //  )
  //)
  psExSetPc.valid := (
    RegNext(
      (
        nextPsExSetPcValid(0)
      ), 
      init=False
    )
  )
  psExSetPc.taken.valid := (
    RegNext(
      nextPsExSetPcTakenValid, 
      init=False
    )
  )
  psExSetPc.taken.payload := (
    RegNext(
      setOutpModMemWord.io.psExSetPc.taken.payload,
      init=setOutpModMemWord.io.psExSetPc.taken.payload.getZero
    )
  )
  for (idx <- 0 until cfg.lowerMyFanoutRegPcSetItCnt) {
    when (nextPsExSetPcValid(idx)) {
      myShouldIgnoreInstr(idx) := True
    }
    when (
      cLink.up.isValid
      && myTempDownIsReadyMost
      && RegNext(myShouldIgnoreInstr(idx), init=False)
      && outp.regPcSetItCnt(idx)(0)
    ) {
      //when (outp.regPcSetItCnt(idx)(0)) {
        myShouldIgnoreInstr(idx) := False
      //}
    }
  }

  //setOutpModMemWord.io.psExSetPc.ready := psExSetPc.ready

  //setOutpModMemWord.io.branchTgtBufElem := outp.branchTgtBufElem
  //val myHistBranchPredictOutp = (
  //  History(
  //    that=outp,
  //    length=3,
  //    when=cMid0Front.up.isFiring,
  //    init=outp.getZero,
  //  )
  //)
  def doSetOtherSetOutpMmwBranchPredictorInputs(
    //usePrevInstr: Boolean
  ): Unit = {
    //if (
    //  !cfg.useLcvDataBus
    //  || !usePrevInstr
    //) {
      setOutpModMemWord.io.btbElemDontPredict := (
        outp.branchTgtBufElem(1).dontPredict
        //|| myShouldIgnoreInstr.last
      )
      setOutpModMemWord.io.branchPredictTkn := (
        outp.branchPredictTkn
        //outp.branchTgtBufElem(1).branchKind.asBits(0)
      )
      //setOutpModMemWord.io.branchPredictReplaceBtbElemMost := (
      //  outp.branchPredictReplaceBtbElemMost
      //)
      //setOutpModMemWord.io.btbElemSavedDstRegPc := (
      //  outp.branchTgtBufElem(0).dstRegPc
      //)
    //} else {
    //  // TODO: (maybe) move this logic to `SnowHousePipeStageInstrDecode`
    //  setOutpModMemWord.io.btbElemDontPredict := (
    //    RegNextWhen(
    //      (
    //        outp.branchTgtBufElem(1).dontPredict
    //        //|| myShouldIgnoreInstr.last
    //      ),
    //      cond=cMid0Front.up.isFiring,
    //      init=False
    //    )
    //  )
    //  setOutpModMemWord.io.branchPredictTkn := (
    //    RegNextWhen(
    //      (
    //        outp.branchPredictTkn
    //        //outp.branchTgtBufElem(1).branchKind.asBits(0)
    //      ),
    //      cond=cMid0Front.up.isFiring,
    //      init=False,
    //    )
    //  )
    //  setOutpModMemWord.io.branchPredictReplaceBtbElem := (
    //    RegNextWhen(
    //      outp.branchPredictReplaceBtbElem,
    //      cond=cMid0Front.up.isFiring,
    //      init=False,
    //    )
    //  )
    //}
  }
  val myNonLcvDbusBtbElemValidArea = (
    !cfg.useLcvDataBus   
  ) generate (new Area {
    setOutpModMemWord.io.btbElemValid := (
      //if (!cfg.useLcvDataBus) (
        outp.branchTgtBufElem(0).valid
      //) else (
      //  outp.branchTgtBufElem(0).valid
      //  && !outp.instrCnt.myPsIdBubble.last
      //  //&& !prevStageFoundBubble
      //)
    )
    doSetOtherSetOutpMmwBranchPredictorInputs(
      //false
    )
  })
  val myLcvDbusBtbElemValidArea = (
    cfg.useLcvDataBus   
  ) generate (new Area {
    setOutpModMemWord.io.btbElemValid := (
      //if (!cfg.useLcvDataBus) (
        outp.branchTgtBufElem(0).valid
        //&& !outp.instrCnt.myPsIdBubble.last
      //) else (
      //  outp.branchTgtBufElem(0).valid
      //  && !outp.instrCnt.myPsIdBubble.last
      //  //&& !prevStageFoundBubble
      //)
    )
    doSetOtherSetOutpMmwBranchPredictorInputs(
      //false
    )
  })
  //val myLcvDbusBtbElemValidArea = (
  //  cfg.useLcvDataBus
  //) generate (new Area {
  //  // TODO: (maybe) move this logic to `SnowHousePipeStageInstrDecode`
  //  val rState = Reg(Bool(), init=False)
  //  //when (
  //  //  outp.branchTgtBufElem(0).valid
  //  //) {
  //  //} otherwise {
  //  //}
  //  //when (cMid0Front.up.isFiring) {
  //  //  rState := False
  //  //}
  //  setOutpModMemWord.io.btbElemValid := False
  //  setOutpModMemWord.io.btbElemDontPredict := (
  //    //outp.branchTgtBufElem(1).dontPredict
  //    //|| myShouldIgnoreInstr.last
  //    False
  //  )
  //  setOutpModMemWord.io.branchPredictTkn := (
  //    //outp.branchPredictTkn
  //    ////outp.branchTgtBufElem(1).branchKind.asBits(0)
  //    False
  //  )
  //  setOutpModMemWord.io.branchPredictReplaceBtbElem := (
  //    //outp.branchPredictReplaceBtbElem
  //    False
  //  )
  //  //val myTempBtbElem = (
  //  //  RegNextWhen(
  //  //  )
  //  //)
  //  val myTempBtbElemValid = {
  //    val tempCond = (
  //      outp.branchTgtBufElem(0).valid
  //      && !myShouldIgnoreInstr.last
  //      && cMid0Front.down.isReady
  //    )
  //    //if (!cfg.useLcvDataBus) (
  //      tempCond
  //    //) else (
  //    //  tempCond && !psWbToEarlierStallRequest
  //    //)
  //  }
  //  when (cMid0Front.up.isValid) {
  //    when (!rState) {
  //      switch (
  //        (
  //          // If I understand correctly,
  //          // `outp.branchTgtBufElem(0).valid` should *always* be `True`
  //          // when `includesLdBubble === True`
  //          // because of how previous pipeline stages function

  //          //outp.branchTgtBufElem(0).valid
  //          myTempBtbElemValid
  //          && outp.branchTgtBufElem(0).includesLdBubble
  //        )
  //        ## (
  //          //outp.branchTgtBufElem(0).valid
  //          myTempBtbElemValid
  //          && outp.instrCnt.myPsIdBubble.last
  //        )
  //      ) {
  //        is (M"1-") {
  //          when (cMid0Front.up.isFiring) {
  //            rState := True
  //          }
  //          setOutpModMemWord.io.btbElemValid := False
  //          setOutpModMemWord.io.btbElemDontPredict := (
  //            //outp.branchTgtBufElem(1).dontPredict
  //            //|| myShouldIgnoreInstr.last
  //            False
  //          )
  //          setOutpModMemWord.io.branchPredictTkn := (
  //            //outp.branchPredictTkn
  //            ////outp.branchTgtBufElem(1).branchKind.asBits(0)
  //            False
  //          )
  //          setOutpModMemWord.io.branchPredictReplaceBtbElem := (
  //            //outp.branchPredictReplaceBtbElem
  //            False
  //          )
  //        }
  //        is (M"01") {
  //          setOutpModMemWord.io.btbElemValid := False
  //          setOutpModMemWord.io.btbElemDontPredict := (
  //            //outp.branchTgtBufElem(1).dontPredict
  //            //|| myShouldIgnoreInstr.last
  //            False
  //          )
  //          setOutpModMemWord.io.branchPredictTkn := (
  //            //outp.branchPredictTkn
  //            ////outp.branchTgtBufElem(1).branchKind.asBits(0)
  //            False
  //          )
  //          setOutpModMemWord.io.branchPredictReplaceBtbElem := (
  //            //outp.branchPredictReplaceBtbElem
  //            False
  //          )
  //        }
  //        default {
  //          setOutpModMemWord.io.btbElemValid := (
  //            //if (!cfg.useLcvDataBus) (
  //              //outp.branchTgtBufElem(0).valid

  //              myTempBtbElemValid
  //            //) else (
  //            //  outp.branchTgtBufElem(0).valid
  //            //  && !outp.instrCnt.myPsIdBubble.last
  //            //  //&& !prevStageFoundBubble
  //            //)
  //          )
  //          when (!myShouldIgnoreInstr.last) {
  //            doSetOtherSetOutpMmwBranchPredictorInputs(false)
  //          }
  //        }
  //      }
  //    } otherwise {
  //      when (cMid0Front.up.isFiring) {
  //        rState := False
  //      }
  //      setOutpModMemWord.io.btbElemValid := True
  //      doSetOtherSetOutpMmwBranchPredictorInputs(true)
  //    }
  //  }
  //  //switch (
  //  //  outp.branchTgtBufElem(0).valid
  //  //  ## outp.branchTgtBufElem(0).includesLdBubble
  //  //) {
  //  //}
  //})
  //setOutpModMemWord.io.splitOp.kind.allowOverride
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
  when (
    {
      val tempCond = (
        cLink.up.isValid
        //&& cMid0Front.down.isReady
        //&& myTempDownIsReady
        && myTempDownIsReadyMost
      )
      //if (!cfg.useLcvDataBus) (
        tempCond
      //) else (
      //  tempCond && !psWbToEarlierStallRequest
      //)
    }
  ) {
    setOutpModMemWord.io.splitOp := outp.splitOp
    if (false) {
      if (setOutpModMemWord.io.haveRetIraState) {
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
          setOutpModMemWord.io.splitOp.opIsDualWidth := (
            outp.splitOp.opIsDualWidth
          )
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
      }
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
    //setOutpModMemWord.io.splitOp.setToDefault()
    //setOutpModMemWord.io.splitOp.opIsMultiCycle := False
    setOutpModMemWord.io.splitOp.opIsMultiCycle := False
    setOutpModMemWord.io.splitOp.opIsMemAccess := False
    setOutpModMemWord.io.splitOp.jmpBrOpIsEq := False
    setOutpModMemWord.io.splitOp.jmpBrOpIsNe := False
    setOutpModMemWord.io.splitOp.setJmpBrAlwaysEqNeOpToDefault()
    setOutpModMemWord.io.splitOp.setJmpBrOtherOpToDefault()
  }
  psExSetPc.nextPc := (
    RegNextWhen(
      setOutpModMemWord.io.psExSetPc.nextPc,
      cond=setOutpModMemWord.io.psExSetPc.fire,
      init=setOutpModMemWord.io.psExSetPc.nextPc.getZero,
    )
  )
  //psExSetPc.dstPc := (
  //  RegNext(
  //    next=setOutpModMemWord.io.psExSetPc.dstPc,
  //    init=setOutpModMemWord.io.psExSetPc.dstPc.getZero,
  //  )
  //)
  //psExSetPc.encInstr := outp.encInstr
  setOutpModMemWord.io.branchKind := (
    outp.btbElemBranchKind(1)
  )
  psExSetPc.branchKind := (
    //RegNext(
    //  next=outp.btbElemBranchKind(1),
    //  init=outp.btbElemBranchKind(1).getZero,
    //)
    RegNextWhen(
      setOutpModMemWord.io.psExSetPc.branchKind,
      cond=setOutpModMemWord.io.psExSetPc.fire,
      init=setOutpModMemWord.io.psExSetPc.branchKind.getZero,
    )
  )
  //psExSetPc.branchTgtBufElem := (
  //  RegNext(
  //    RegNext(
  //      RegNext(
  //        outp.branchTgtBufElem(1),
  //        //init=outp.branchTgtBufElem(1).getZero,
  //      ),
  //      //init=outp.branchTgtBufElem(1).getZero,
  //    ),
  //    init=outp.branchTgtBufElem(1).getZero,
  //  )
  //)
  psExSetPc.branchTgtBufElem.allowOverride
  psExSetPc.branchTgtBufElem := (
    RegNextWhen(
      setOutpModMemWord.io.psExSetPc.branchTgtBufElem,
      cond=setOutpModMemWord.io.psExSetPc.fire,
      init=(
        setOutpModMemWord.io.psExSetPc.branchTgtBufElem.getZero
      ),
    )
  )
  psExSetPc.branchTgtBufElem.srcRegPc.allowOverride
  psExSetPc.branchTgtBufElem.srcRegPc := (
    RegNextWhen(
      RegNext(
        outp.laggingRegPc,
        //cond=cMid0Front.up.isFiring,
        init=outp.laggingRegPc.getZero,
      ),
      cond=setOutpModMemWord.io.psExSetPc.fire,
      init=outp.laggingRegPc.getZero,
    )
  )
  //psExSetPc.branchTgtBufElem.includesLdBubble.allowOverride
  //psExSetPc.branchTgtBufElem.includesLdBubble := (
  //  RegNextWhen(
  //    //RegNext(
  //      outp.branchTgtBufElem(1).includesLdBubble,
  //      //cond=cMid0Front.up.isFiring,
  //    //  init=outp.branchTgtBufElem(1).includesLdBubble.getZero,
  //    //),
  //    cond=setOutpModMemWord.io.psExSetPc.fire,
  //    init=outp.branchTgtBufElem(1).includesLdBubble.getZero,
  //  )
  //)
  psExSetPc.branchTgtBufElem.dontPredict.allowOverride
  psExSetPc.branchTgtBufElem.dontPredict := (
    RegNextWhen(
      //RegNext(
        //outp.branchTgtBufElem(1).dontPredict,
        setOutpModMemWord.io.psExSetPc.branchTgtBufElem.dontPredict,
        //cond=cMid0Front.up.isFiring,
      //  init=outp.branchTgtBufElem(1).dontPredict.getZero,
      //),
      cond=setOutpModMemWord.io.psExSetPc.fire,
      init=False,
    )
  )
  //psExSetPc.branchTgtBufElem.srcRegPc := (
  //  RegNext(
  //    setOutpModMemWord.io.psExSetPc.branchTgtBufElem.srcRegPc,
  //    init=(
  //      setOutpModMemWord.io.psExSetPc.branchTgtBufElem.srcRegPc.getZero
  //    ),
  //  )
  //)
  //psExSetPc.branchTgtBufElem.dstRegPc.allowOverride
  //psExSetPc.branchTgtBufElem.dstRegPc := (
  //  RegNext(
  //    setOutpModMemWord.io.psExSetPc.branchTgtBufElem.dstRegPc,
  //    init=(
  //      setOutpModMemWord.io.psExSetPc.branchTgtBufElem.dstRegPc.getZero
  //    ),
  //  )
  //)
  //psExSetPc.branchTgtBufElem.dontPredict.allowOverride
  //psExSetPc.branchTgtBufElem.dontPredict := (
  //  RegNext(
  //    next=setOutpModMemWord.io.psExSetPc.branchTgtBufElem.dontPredict,
  //    init=False
  //  )
  //)

  if (cfg.optScoreboard) {
    psExSetPc.reorderBufIdx.allowOverride
    psExSetPc.reorderBufIdx := (
      RegNextWhen(
        setOutpModMemWord.io.psExSetPc.reorderBufIdx,
        cond=setOutpModMemWord.io.psExSetPc.fire,
        init=(
          setOutpModMemWord.io.psExSetPc.reorderBufIdx.getZero
        ),
      )
    )
  }

  if (!cfg.useLcvDataBus) {
    myDbus.allowOverride
    myDbus.sendData := (
      RegNext(myDbus.sendData, init=myDbus.sendData.getZero)
    )
  } else {
    // TODO
    //myH2dPushStm.addr := io.bus.sendData.addr
    //myH2dPushStm.data := io.bus.sendData.data
    //myH2dPushStm.isWrite := io.bus.sendData.accKind.asBits(1)
  }
  object MultiCycleOpState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      Idle,
      Main//,
      //NoMoreStall
      = newElement()
  }
  val myHavePsExStallArea0 = (
    //!cfg.optForFmax
    cfg.havePsExStall
  ) generate (new Area {
    val rMultiCycleOpState = {
      val temp = Reg(MultiCycleOpState())
      temp.init(MultiCycleOpState.Idle)
      temp
    }
    val rOpIsMultiCycle = {
      val temp = (
        Reg(Vec.fill(cfg.multiCycleOpInfoMap.size)(
          Bool()
        ))
      )
      temp.foreach(elem => elem.init(elem.getZero))
      temp
    }
    //var busIdxFound: Boolean = false
    //var busIdx: Int = 0
    for (
      ((group, innerMap), groupIdx)
      <- cfg.multiCycleOpInfoMap.view.zipWithIndex
    ) {
      switch (
        RegNext(setOutpModMemWord.io.splitOp.multiCycleOpKind)
        init(0x0)
      ) {
        for (((_, opInfo), kindIdx) <- innerMap.view.zipWithIndex) {
          is (kindIdx) {
            def multiCycleBus = multiCycleBusVec(groupIdx)
            multiCycleBus.sendData.srcVec.foreach(src => {
              src.allowOverride
            })
            multiCycleBus.sendData.srcVec(0) := (
              RegNext/*When*/(
                setOutpModMemWord.io.selRdMemWord(
                  opInfo=opInfo,
                  idx=0,
                ).resize(
                  multiCycleBus.sendData.srcVec(0).getWidth
                ),
              )
              init(0x0)
            )
            if (multiCycleBus.sendData.srcVec.size > 1) {
              for (
                multiCycleIdx <- 1 until multiCycleBus.sendData.srcVec.size
              ) {
                if (multiCycleIdx < opInfo.srcArr.size) {
                  multiCycleBus.sendData.srcVec(multiCycleIdx) := (
                    RegNext/*When*/(
                      setOutpModMemWord.io.selRdMemWord(
                        opInfo=opInfo,
                        idx=multiCycleIdx,
                      ),
                    )
                    init(0x0)
                  )
                }
              }
            }
          }
        }
      }
    }
  })
  val myNonLcvDbusPartBArea = (
    !cfg.useLcvDataBus
  ) generate (new Area {
    when (setOutpModMemWord.io.opIsMemAccess.head) {
      nextPrevTxnWasHazard := True
      when (cLink.up.isFiring) {
        myDbus.nextValid := True
      }
    }
    myDbus.sendData.addr.allowOverride
    when (cLink.up.isFiring) {
      myDbus.sendData := setOutpModMemWord.io.dbusHostPayload
    }
  })
  val myLcvDbusPartBArea = (
    cfg.useLcvDataBus
  ) generate (new Area {
  })
  val myHavePsExStallArea1 = (
    //!cfg.optForFmax
    cfg.havePsExStall
  ) generate (new Area {
    val rMultiCycleOpState = myHavePsExStallArea0.rMultiCycleOpState
    val rOpIsMultiCycle = myHavePsExStallArea0.rOpIsMultiCycle
    for (myPsExStallHost <- psExStallHostArr.view) {
      if (
        myPsExStallHost.stallIo.get.sendData.kind != null
        && myPsExStallHost.stallIo.get.sendData.kind.getWidth > 0
      ) {
        myPsExStallHost.stallIo.get.sendData.kind := (
          outp.splitOp.multiCycleOpKind.resize(
            myPsExStallHost.stallIo.get.sendData.kind.getWidth
          )
        )
      }
    }

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
    val rHaveDoneMultiCycleOp = Reg(Bool(), init=False)
    switch (rMultiCycleOpState) {
      is (MultiCycleOpState.Idle) {
        setOutpModMemWord.io.inMultiCycleOp := False
        when (
          !rHaveDoneMultiCycleOp
          && cLink.up.isValid
          && setOutpModMemWord.io.opIsAnyMultiCycle
          && !myShouldIgnoreInstr(2)
        ) {
          //if (cfg.dbgExposeExtrasAtRegFileWrite) {
          //  outp.instrCnt.dbgUnfinishedMultiCycleOp.foreach(item => {
          //    item := True
          //  })
          //}
          for (idx <- 0 until rOpIsMultiCycle.size) {
            rOpIsMultiCycle(idx) := (
              setOutpModMemWord.io.opIsMultiCycle(idx)
            )
          }
          if (cfg.optScoreboard) {
            cLink.duplicateIt()
            //cLink.down(args.currPayload).setAsBubbleMain(Some(True))
            //setOutpModMemWord.io.instrCnt.setAsPsIdBubbleMain()
            cLink.down(args.currPayload).setAsBubbleMain(
              //Some(True)
              None
            )
            cLink.down(args.currPayload).instrCnt
            .myPsExMultiCycleBubble.foreach(
              item => {
                item := True
              }
            )
            setOutpModMemWord.io.instrCnt.setAsPsIdBubbleMain()
          } else {
            cLink.haltIt()
          }
          val toOrReduce = (
            if (!cfg.useLcvDataBus) (
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
            ) else ( // if (cfg.useLcvDataBus)
              Vec[Bool](
                //True
                //cMid0Front.down.isReady
                myTempDownIsReady
                //&& !psWbToEarlierStallRequest
              ).asBits.asUInt
            )
          )
          when (toOrReduce.orR) {
            rMultiCycleOpState := MultiCycleOpState.Main
            myDoStall(stallKindMem) := False
          }
        }
        //myDoStall(stallKindMultiCycle) := False
        when (cLink.up.isFiring) {
          rHaveDoneMultiCycleOp := False
        }
      }
      is (MultiCycleOpState.Main) {
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
              val psExStallHost = psExStallHostArr(
                //busIdx
                idx
              )
              doMultiCycleStart(psExStallHost, idx=idx)
            }
            val psExStallHost = psExStallHostArr(
              //busIdx
              idx
            )
            //doMultiCycleStart(psExStallHost, idx=idx)
            when (
              RegNext(psExStallHost.nextValid, init=False)
              && psExStallHost.ready
            ) {
              psExStallHost.nextValid := False
              rMultiCycleOpState := MultiCycleOpState.Idle
            } elsewhen (rOpIsMultiCycle(idx)) {
              if (cfg.optScoreboard) {
                cLink.duplicateIt()
                //cLink.down(args.currPayload).setAsBubbleMain(Some(True))
                //setOutpModMemWord.io.instrCnt.setAsPsIdBubbleMain()
                cLink.down(args.currPayload).setAsBubbleMain(
                  //Some(True)
                  None
                )
                cLink.down(args.currPayload).instrCnt
                .myPsExMultiCycleBubble.foreach(
                  item => {
                    item := True
                  }
                )
                setOutpModMemWord.io.instrCnt.setAsPsIdBubbleMain()
              } else {
                cLink.haltIt()
              }
              //outp.myExt.foreach(item => {
              //  item.modMemWordValid.foreach(mmwValidItem => {
              //    mmwValidItem := False
              //  })
              //})
            }
            //--------
          }
          when (cLink.up.isFiring) {
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
  })
  for (idx <- 0 until doCheckHazard.size) {
    doCheckHazard(idx) := (
      RegNextWhen(
        next=myNextPrevTxnWasHazardVec(idx),
        cond=cLink.up.isFiring,
        init=myNextPrevTxnWasHazardVec(idx).getZero,
      )
    )
  }
  when (
    //myDoStall.sFindFirst(_ === True)._1
    myDoStall.asBits.orR
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
    if (!cfg.optForFmax) {
      cLink.haltIt()
    } else {
      // TODO: improve IPC here by using `cLink.duplicateIt`
      //cLink.haltIt()
      if (cfg.optScoreboard) {
        cLink.duplicateIt()
        //cLink.down(args.currPayload).setAsBubbleMain(Some(True))
        //setOutpModMemWord.io.instrCnt.setAsPsIdBubbleMain()
        cLink.down(args.currPayload).setAsBubbleMain(
          //Some(True)
          None
        )
        cLink.down(args.currPayload).instrCnt
        .myPsExMemAccessBubble.foreach(
          item => {
            item := True
          }
        )
        setOutpModMemWord.io.instrCnt.setAsPsIdBubbleMain()
      } else {
        cLink.haltIt()
      }
      //cLink.duplicateIt()
      //cLink.down
      //cMid0Front.down
      //cLink.down()
    }
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
  //for (idx <- 0 until cfg.regFileCfg.memArrSize) {
    val myNonBubbleTag = (
      cfg.optScoreboard
    ) generate (
      outp.instrCnt.scoreboardIssuePayload.nonBubbleTag
    )

    val myTempReorderBufIdx = (
      cfg.optScoreboard
    ) generate (
      outp.instrCnt.scoreboardIssuePayload.reorderBufIdx
    )
    if (cfg.optScoreboard) {
      //myTempReorderBufIdx := myNonBubbleTag
      myTempReorderBufIdx := (
        //myNonBubbleTag
        RegNext(
          myTempReorderBufIdx,
          init=myTempReorderBufIdx.getZero
        )
      )
      when (
        cLink.up.isFiring
        && !myShouldIgnoreInstr.last
        && !outp.instrCnt.myPsIdBubble.last
        //&& !outp.instrCnt.myPsIdOtherBubble.last
      ) {
        myTempReorderBufIdx := (
          RegNext(myTempReorderBufIdx) + 1
        )
      }

      myNonBubbleTag := (
        (
          RegNext(
            myNonBubbleTag.asSInt,
            //init=myNonBubbleTag
          )
          init(
            //-1
            0x0
          )
        ).asUInt
      )
      when (
        cLink.up.isFiring
        && !myShouldIgnoreInstr.last
        && !outp.instrCnt.myPsIdBubble.last
        && !outp.instrCnt.myPsIdOtherBubble.last
      ) {
        myNonBubbleTag := (
          RegNext(myNonBubbleTag) + 1
        )
      }
    }

    //myNonBubbleTag := (
    //  RegNextWhen(
    //    (myNonBubbleTag + 1),
    //    cond=(
    //      cLink.up.isFiring
    //      && !myShouldIgnoreInstr.last
    //      && !outp.instrCnt.myPsIdBubble.last
    //    )
    //  )
    //  init(0x0)
    //)

    when (myShouldIgnoreInstr.last) {
      //outp.gprIsZeroVec.last.foreach(item => {
      //  item := True
      //})
      //outp.gprIsZeroVec.foreach(outerItem => {
      //  outerItem.foreach(item => {
      //    item := True
      //  })
      //})
      //outp.gprIsNonZeroVec.foreach(outerItem => {
      //  outerItem.foreach(item => {
      //    item := False
      //  })
      //})
      outp.setAsBubbleMain(
        None,
        myUpdateRegPcSetItCnt=false,
      )
      if (cfg.optScoreboard) {
        outp.splitOp.scoreboardOpIsMemAccess := (
          inp.splitOp.scoreboardOpIsMemAccess
        )
      } else { // if (!cfg.optScoreboard)
        outp.gprIdxVec := outp.gprIdxVec.getZero
      }
      outp.myExt(0).rdMemWord.foreach(item => {
        item := 0x0
      })
      outp.myExt(0).modMemWord := 0x0

      outp.myExt.foreach(item => {
        item.modMemWordValid.foreach(item => {
          item := (
            False
          )
        })
      })
    }
    outp.myExt.foreach(item => {
      item.fwdCanDoIt.foreach(item => {
        item := (
          //!setOutpModMemWord.io.shouldIgnoreInstr.last
          //if (!cfg.useLcvDataBus) (
            !myShouldIgnoreInstr.last
            && outp.gprIsNonZeroVec.last.last
            //&& cMid0Front.up.isValid
          //) else (
          //  !(
          //    myShouldIgnoreInstr.last
          //    //|| setOutpModMemWord.io.opIsMemAccess.last
          //    || outp.outpDecodeExt.opIsMemAccess.last
          //  )
          //)
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
  //}
  if (cfg.useLcvDataBus) {
    outp.instrCnt.shouldIgnoreInstr.foreach(item => {
      item := myShouldIgnoreInstr.last
    })
  }
  //when (!myTempDownIsReady) {
  //}
}

//object SnowHousePsMemStageLink{
//  def apply(up : Node, down : Node) = new SnowHousePsMemStageLink(
//    up, down
//  )
//}
//
//class SnowHousePsMemStageLink(
//  val up : Node, val down : Node
//) extends Link {
//  down.up = this
//  up.down = this
//
//  var holdPayload = false
//  var collapseBubble = true
//
//  def withoutCollapse() : this.type = {
//    collapseBubble = false
//    this
//  }
//  def withPayloadHold() : this.type = {
//    holdPayload = true
//    this
//  }
//
//  override def ups = List(up)
//  override def downs = List(down)
//
//  override def propagateDown(): Unit = {
//    propagateDownAll()
//    if(up.ctrl.valid.nonEmpty) down.valid
//    down.ctrl.forgetOneSupported = true
//  }
//  override def propagateUp(): Unit = {
//    propagateUpAll()
//    if(down.ctrl.ready.nonEmpty) up.ready
//  }
//
//  override def build(): Unit = {
//    val matches = down.fromUp.payload.intersect(up.fromDown.payload)
//    if(down.ctrl.valid.nonEmpty) down.valid.setAsReg() init (False)
//    matches.foreach(p => down(p).setAsReg())
//
//
//    up.ctrl.ready.isEmpty match {
//      case true =>
//        if(down.ctrl.valid.nonEmpty) down.valid := up.isValid
//        matches.foreach(p => down(p) := up(p))
//      case false => {
//        down.ctrl.forgetOne foreach { 
//          cond => down.valid clearWhen (cond) 
//        }
//        if(down.ctrl.valid.nonEmpty) when(up.isReady) {
//          down.valid := up.isValid
//        }
//        when(if (holdPayload) up.isValid && up.isReady else up.isReady) {
//          matches.foreach(p => down(p) := up(p))
//        }
//      }
//    }
//
//    if (up.ctrl.ready.nonEmpty) {
//      up.ready := down.ready
//      if (collapseBubble) up.ready setWhen (!down.isValid)
//    }
//  }
//}

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
  //psMemToEarlierStallRequest: Bool,
  psWbToEarlierStallRequest: Bool,
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
  def extIdxUp = PipeRegFile.extIdxUp
  def extIdxSaved = PipeRegFile.extIdxSaved
  def extIdxLim = PipeRegFile.extIdxLim
  //def doPsMemFork = (
  //  //true
  //  !cfg.useLcvDataBus
  //)
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
  //val fMem = (
  //  doPsMemFork
  //) generate (
  //  ForkLink(
  //    up=cMem.down,
  //    downs={
  //      Array.fill(2)(Node())
  //    },
  //    synchronous=(
  //      false
  //      //true
  //    )
  //  )
  //)
  //val sMemFwd = (
  //  doPsMemFork
  //) generate (
  //  StageLink(
  //    up=fMem.downs(0),
  //    down={
  //      regFile.io.modBackFwd
  //    }
  //  )
  //)
  val sMem = {
    val temp = StageLink(
      up=(
        //if (!doPsMemFork) (
          cMem.down
        //) else (
        //  fMem.downs(1)
        //)
      ),
      down={
        //if (!doPsMemFork) {
        //  val temp = Node()
        //  temp.setName(s"sMem_down")
        //  temp
        //} else {
          modBack
        //}
      },
    )
    //if (!cfg.useLcvDataBus) (
      temp
    //) else (
    //  temp.withoutCollapse
    //)
    
  }
  //regFile.myLinkArr += cMem
  //if (doPsMemFork) {
  //  regFile.myLinkArr += fMem
  //  regFile.myLinkArr += sMemFwd
  //  //regFile.myLinkArr += sMem
  //} else {
  //  //regFile.myLinkArr += sMem
  //}
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
    //def tempPayloadRightA = cMem.up(regFile.io.modFrontAfterPayloadA)
    def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
    def tempExtRight(ydx: Int) = tempPayloadRight.myExt(ydx)
    //def tempExtRightA(ydx: Int) = tempPayloadRightA.myExt(ydx)
    val myExtLeft = tempExtLeft(ydx=ydx)
    val myExtRight = tempExtRight(ydx=ydx)
    //val myExtRightA = tempExtRightA(ydx=ydx)
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
        //tempPayloadRightA.nonExt
      )
      myExtLeft.main.memAddr := myExtRight.main.memAddr
      myExtLeft.main.nonMemAddrMost := myExtRight.main.nonMemAddrMost
      //for (idx <- 0 until myExtLeft.modMemWordValid.size) {
      //  myExtLeft.modMemWordValid(idx) := (
      //    myExtRight.modMemWordValid(idx)
      //  )
      //}
    }
    if (!cfg.useLcvDataBus) {
      myExtLeft.modMemWord := myModMemWord.asUInt
    } else { // if (cfg.useLcvDataBus)
      //when (
      //  RegNextWhen(
      //    midModPayload(extIdxUp).instrCnt.myPsIdBubble.last,
      //    cond=cMem.up.isFiring,
      //    init=False,
      //  )
      //) {
      //  myExtLeft.modMemWord := (
      //    RegNextWhen(
      //      myDbus.recvData.word,
      //      cond=cMem.up.isFiring,
      //      init=myDbus.recvData.word.getZero,
      //    )
      //  )
      //} otherwise {
      myExtLeft.modMemWord := myModMemWord.asUInt
      //}
    }

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
  //val myLcvDbusArea = (
  //  cfg.useLcvDataBus
  //) generate (new Area {
  //  when (
  //    ////RegNext(
  //    //  RegNext(myDbus.nextValid, init=False),
  //    ////  init=False
  //    ////)
  //    ////myWbPayload.decodeExt.opIsMemAccess.sFindFirst(
  //    ////  _ === True
  //    ////)._1
  //    //cMem.up.isValid
  //    //&& 
  //    midModPayload(extIdxUp).outpDecodeExt.opIsMemAccess.last
  //  ) {
  //    val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
  //    val myCurrExt = (
  //      if (!mapElem.haveHowToSetIdx) (
  //        midModPayload(extIdxUp).myExt(0)
  //      ) else (
  //        midModPayload(extIdxUp).myExt(mapElem.howToSetIdx)
  //      )
  //    )
  //    myCurrExt.modMemWordValid.foreach(mmwValidItem => {
  //      mmwValidItem := False
  //    })
  //  }
  //})

  def setMidModStages(): Unit = {
    regFile.io.midModStages(0) := midModPayload
    //when (
    //  midModPayload(extIdxUp).fwdCanDoIt
    //) {
    //  regFile.io.midModStages(0)(extIdxUp).myExt.foreach(item => {
    //    item.fwdCanDoIt.foreach(item => {
    //      
    //    })
    //  })
    //}
  }
  setMidModStages()

  //modFront(pMem) := midModPayload(extIdxUp)
  //when (modFront.isValid) {
  //} otherwise {
  //}
  //val myMemAccessHistSize = 2
  //val myHistDbusHostAddr = (
  //  History[UInt](
  //    that=midModPayload(extIdxUp).myDbusHostPayload.addr,
  //    length=myMemAccessHistSize,
  //    when=cMem.up.isFiring,
  //    init=midModPayload(extIdxUp).myDbusHostPayload.addr.getZero
  //  )
  //)
  //val myHistOpIsMemAccess = (
  //  History[Bool](
  //    that=(
  //      midModPayload(extIdxUp).outpDecodeExt.opIsMemAccess.orR
  //      && !midModPayload(extIdxUp).instrCnt.shouldIgnoreInstr.last
  //    ),
  //    length=myMemAccessHistSize,
  //    when=cMem.up.isFiring,
  //    init=False,
  //  )
  //)
  if (!cfg.useLcvDataBus) {
    cMem.up(pMem) := midModPayload(extIdxUp)
  } else { // if (cfg.useLcvDataBus)
    //psMemToEarlierStallRequest := False
    //when (
    //  cMem.up.isValid
    //  && myHistOpIsMemAccess.head && myHistOpIsMemAccess.last
    //  && myHistDbusHostAddr.head === myHistDbusHostAddr.last
    //  //(
    //  //  midModPayload(extIdxUp).myDbusHostPayload.addr(
    //  //    midModPayload(extIdxUp).myDbusHostPayload.addr.high
    //  //    downto 4
    //  //  ) === (
    //  //  )
    //  //  && midModPayload(extIdxUp).outpDecodeExt.opIsMemAccess
    //  //)
    //) {
    //  psMemToEarlierStallRequest := !cMem.down.isFiring
    //}
    when (
      //psMemToEarlierStallRequest
      //|| 
      psWbToEarlierStallRequest
    ) {
      cMem.duplicateIt()
    }
    //otherwise {
    //  sMem.up.ready setWhen (!sMem.down.isValid)
    //}
    when (
      cMem.up.isFiring
      && !midModPayload(extIdxUp).instrCnt.shouldIgnoreInstr.last
    ) {
      cMem.up(pMem) := midModPayload(extIdxUp)
    } otherwise {
      cMem.up(pMem) := midModPayload(extIdxUp).getZero
      if (cfg.dbgExposeExtrasAtRegFileWrite) {
        cMem.up(pMem).instrCnt.shouldIgnoreInstr.allowOverride
        cMem.up(pMem).instrCnt.shouldIgnoreInstr.last := True
      }
      cMem.up(pMem).splitOp.allowOverride
      cMem.up(pMem).splitOp.setToDefault()
      cMem.up(pMem).gprIsZeroVec.allowOverride
      cMem.up(pMem).gprIsZeroVec.foreach(outerItem => {
        outerItem.foreach(item => {
          item := True
        })
      })
    }
    //sMem.up.ready setWhen (!sMem.down.isValid && !psWbToEarlierStallRequest)
    //for (idx <- 0 until midModPayload(extIdxUp).myExt.size) {
    //  //val tempExtLeft = regFile.io.midModStages(0)(extIdxUp).myExt(idx)
    //  val tempPayloadRight = midModPayload(extIdxUp)
    //  val tempExtRight = tempPayloadRight.myExt(idx)
    //  for (jdx <- 0 until tempExtRight.fwdCanDoIt.size) {
    //    //tempExtRight.fwdCanDoIt(jdx) := tempExtRight.fwdCanDoIt(jdx)
    //    cMem.up(pMem).myExt(idx).fwdCanDoIt(jdx).allowOverride
    //    when (cMem.up.isFiring) {
    //      cMem.up(pMem).myExt(idx).fwdCanDoIt(jdx) := (
    //        tempPayloadRight.instrCnt.shouldIgnoreInstr.last
    //        //tempExtRight.fwdCanDoIt(jdx)
    //        //|| tempPayloadRight.outpDecodeExt.opIsMemAccess.last
    //        //&& !tempPayloadRight.outpDecodeExt.opIsMemAccess.last
    //        //&& !tempPayloadRight.outpDecodeExt.memAccessKind.asBits(1)
    //      )
    //    }
    //    //when (tempExtRight.fwdCanDoIt(jdx)) {
    //    //  //tempExtRight.fwdCanDoIt
    //    //  cMem.up(pMem).myExt(idx).fwdCanDoIt(jdx) := (
    //    //    tempExtRight.fwdCanDoIt(jdx)
    //    //  )
    //    //} otherwise {
    //    //  cMem.up(pMem).myExt(idx).fwdCanDoIt(jdx) := (
    //    //    tempExtRight.fwdCanDoIt(jdx)
    //    //  )
    //    //}
    //  }
    //}
  }
}
case class SnowHousePipeStageWriteBack(
  args: SnowHousePipeStageArgs,
  psWbToEarlierStallRequest: Bool,
  //psMemStallHost: LcvStallHost[
  //  BusHostPayload,
  //  BusDevPayload,
  //],
  //myDbusExtraReady: Vec[Bool],
  //myDbusLdReady: Bool,
  //myDbusIo: SnowHouseDbusIo,
  //myModMemWord: SInt,
  doModInBackEtcParams: PipeRegFileDoModInBackEtcFuncParams[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeRegFileDualRdTypeDisabled[UInt, Bool],
  ],
) extends Area {
  def myDbusIo = args.myDbusIo
  def myDbus = myDbusIo.dbus
  def myDbusExtraReady = myDbusIo.dbusExtraReady
  def myDbusLdReady = myDbusIo.dbusLdReady
  def cfg = args.cfg
  def io = args.io
  //def regFile = args.regFile
  def regFileIo = doModInBackEtcParams.pipeRegFileIo
  def front = regFileIo.front
  def frontPayload = regFileIo.frontPayload
  def modFront = regFileIo.modFront
  //def modFrontAfterPayload = regFileIo.modFrontAfterPayload
  def pMem = regFileIo.modBackPayload//args.prevPayload
  def modBack = regFileIo.modBack
  def modBackPayload = regFileIo.modBackPayload //args.currPayload
  def back = regFileIo.back
  def backPayload = regFileIo.backPayload
  def cWb = doModInBackEtcParams.cBackEtc //args.link

  //val sWb = StageLink(
  //  up=cWb.down,
  //  down={
  //    val temp = Node()
  //    temp.setName(s"sWb_down")
  //    temp
  //  }
  //)
  //regFile.myLinkArr += sWb

  //val fWb = (
  //  ForkLink(
  //    up=(
  //      cWb.down
  //      //sWb.down
  //    ),
  //    downs={
  //      //Array.fill(2)(Node())
  //      List[Node](
  //        regFile.io.modBackFwd,
  //        modBack,
  //      )
  //    },
  //    synchronous=(
  //      false
  //      //true
  //    )
  //  )
  //)
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
  //regFile.myLinkArr += fWb
  //regFile.myLinkArr += sWbFwd
  //regFile.myLinkArr += sWb
  val myWbPayload = (
    //Vec.fill(2)(
    //  SnowHousePipePayload(cfg=cfg)
    //)
    Vec[SnowHousePipePayload](
      doModInBackEtcParams.inp,
      doModInBackEtcParams.outp,
    )
  )
  //when (cWb.up.isValid) {
  //}
  //def extIdxUp = PipeRegFile.extIdxUp
  //def extIdxSaved = PipeRegFile.extIdxSaved
  //def extIdxLim = PipeRegFile.extIdxLim
  //regFile.io.midModStages(1)(extIdxUp) := myWbPayload(1)
  //regFile.io.midModStages(1)(extIdxSaved) := (
  //  RegNextWhen(
  //    myWbPayload(1),
  //    cond=cWb.up.isFiring,
  //    init=myWbPayload(1).getZero,
  //  )
  //)

  //myWbPayload := (
  //  RegNext(myWbPayload, init=myWbPayload.getZero)
  //)
  //when (cWb.up.isValid) {
  //  myWbPayload.head := cWb.up(pMem)
  //}

  //myWbPayload(0) := cWb.up(pMem)
  myWbPayload(1) := (
    RegNext(myWbPayload(1), init=myWbPayload(1).getZero)
  )
  //when (cWb.up.isValid) {
  //  myWbPayload(1) := myWbPayload(0)
  //}

  //when (cWb.up.isValid) {
  //  myWbPayload(1) := myWbPayload(0)
  //}

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
    def tempPayloadRight = myWbPayload(0)//cWb.up(pMem)
    def tempExtLeft(ydx: Int) = myWbPayload(1).myExt(ydx)
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
      myWbPayload(1).nonExt := (
        cWb.up(pMem).nonExt
      )
      myExtLeft.main.memAddr := myExtRight.main.memAddr
      myExtLeft.main.nonMemAddrMost := myExtRight.main.nonMemAddrMost
      if (!cfg.useLcvDataBus) {
        myExtLeft.main.modMemWord := myExtRight.main.modMemWord
      }
    }
    if (cfg.useLcvDataBus) {
      when (
        //cWb.up.isValid
        myExtRight.modMemWordValid.last
        //&& rMmwState(ydx)(0) === MmwState.WAIT_DATA
      ) {
        myExtLeft.main.modMemWord := myExtRight.main.modMemWord
      }
    }
    //myExtLeft.modMemWord := myModMemWord.asUInt

    when (
      if (!cfg.useLcvDataBus) (
        cWb.up.isValid
      ) else ( // if (cfg.useLcvDataBus)
        cWb.up.isValid
        //|| RegNext(io.lcvDbus.d2hBus.valid, init=False)
      )
    ) {
      rMmwState(ydx)(0) := MmwState.WAIT_UP_FIRE
    }
    when (cWb.up.isFiring) {
      rMmwState(ydx).foreach(item => item := MmwState.WAIT_DATA)
    }
    //if (cfg.useLcvDataBus) {
    //  when (io.lcvDbus.d2hBus.valid) {
    //    rMmwState(ydx)(0) := MmwState.WAIT_UP_FIRE
    //  }
    //}
    myExtLeft.valid.foreach(current => {
      current := (
        cWb.up.isValid
      )
    })
    myExtLeft.ready := cWb.up.isReady
    myExtLeft.fire := cWb.up.isFiring
    //cWb.up(modBackPayload) := myWbPayload(1)
  }

  //--------
  val myLcvDbusArea = (
    cfg.useLcvDataBus
  ) generate (new Area {
    //myDbusIo.myDbusExtraValid := (
    //  cWb.up.isValid
    //  && myWbPayload.outpDecodeExt.opIsMemAccess.last
    //)
    val myD2hBus = cloneOf(io.lcvDbus.d2hBus)
    //myD2hBus <-/< io.lcvDbus.d2hBus
    myD2hBus << io.lcvDbus.d2hBus
    myD2hBus.ready := False

    //psWbToEarlierStallRequest := False

    when (
      //myDbusIo.myDbusExtraValid
      //cWb.up.isValid
      //&& 
      myWbPayload(1).outpDecodeExt.opIsMemAccess.last
      //myWbPayload(1).splitOp.opIsMemAccess
    ) {
      myD2hBus.ready := True
      when (
        //!myDbus.ready
        ////!myDbusExtraReady(3)
        !myD2hBus.valid
        //|| (
        //  (
        //    myD2hBus.src
        //    //=/= myWbPayload(1).outpDecodeExt.memAccessSrc
        //    =/= myWbPayload(1).myDbusHostPayload.src
        //  )
        //  && (
        //    myWbPayload(1).myDbusHostPayload.accKind.asBits(1)
        //  )
        //)
      ) {
        //psWbToEarlierStallRequest := True
        cWb.duplicateIt()
        //cWb.haltIt()
        //myWbPayload(1).myExt.foreach(myExt => {
        //  myExt.modMemWordValid.foreach(mmwValidItem => {
        //    mmwValidItem := False
        //  })
        //  myExt.fwdCanDoIt.foreach(fwdIdx => {
        //    fwdCanDoIt := 0x0
        //  })
        //})
        //cWb.down(modBackPayload) := myWbPayload(1).getZero
        cWb.down(modBackPayload).allowOverride
        //cWb.down(modBackPayload) := myWbPayload(1)
        cWb.down(modBackPayload) := myWbPayload(1).getZero
        cWb.down(modBackPayload).myExt.foreach(myExt => {
          myExt.modMemWordValid.allowOverride
          myExt.modMemWordValid.foreach(mmwValidItem => {
            mmwValidItem := False
          })
          myExt.fwdCanDoIt.allowOverride
          myExt.fwdCanDoIt.foreach(fwdCanDoItItem => {
            fwdCanDoItItem := False
          })
          //myExt.extraLastBackMmwValid.allowOverride
          //myExt.extraLastBackMmwValid.foreach(mmwValidItem => {
          //  mmwValidItem := False
          //})
          //myExt.extraLastBackFwdCanDoIt.allowOverride
          //myExt.extraLastBackFwdCanDoIt.foreach(fwdCanDoItItem => {
          //  fwdCanDoItItem := False
          //})
        })
      }
      //when (
      //  myD2hBus.valid
      //  && (
      //    (
      //      RegNextWhen(
      //        True,
      //        cond=myD2hBus.fire,
      //        init=False
      //      )
      //    )
      //    && (
      //      myD2hBus.src
      //      //=/= myWbPayload(1).outpDecodeExt.memAccessSrc
      //      //=/= myWbPayload(1).myDbusHostPayload.src
      //      =/= (
      //        (
      //          RegNextWhen(
      //            myD2hBus.src.asSInt + 1,
      //            cond=myD2hBus.fire,
      //          )
      //          init(-1)
      //        ).asUInt
      //      )
      //    )
      //    //&& (
      //    //  myWbPayload(1).myDbusHostPayload.accKind.asBits(1)
      //    //)
      //  )
      //) {
      //  psWbToEarlierStallRequest := True
      //}
    }
    switch (
      (
        myWbPayload(0).outpDecodeExt.opIsMemAccess.head
        && !myWbPayload(0).outpDecodeExt.memAccessKind.asBits(1)
        && myD2hBus.valid
      )
      ## myWbPayload(0).outpDecodeExt.memAccessKind.asBits(0)
      ## myWbPayload(0).outpDecodeExt.memAccessSubKind.asBits
    ) {
      //--------
      // This stuff might need to be changed for the purposes of
      // atomic operations that are larger than `cfg.mainWidth`.
      // It's currently limited to at max 32-bit values, for example, on a
      // 32-bit `cfg.mainWidth` CPU. More work will be needed later.
      //--------
      val myDecodeExt = myWbPayload(1).outpDecodeExt
      val mapElem = myWbPayload(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myWbPayload(1).myExt(
            0
          )
        ) else (
          myWbPayload(1).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      //--------
      is (M"10--") {
        // zero-extending sub-word load or full-word load
        myCurrExt.modMemWord := myD2hBus.data
      }
      is (M"1100") {
        // LoadS, Sz8
        myCurrExt.modMemWord := (
          myD2hBus.data(
            (7.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrExt.modMemWord.getWidth).asUInt
        )
      }
      is (M"1101") {
        // LoadS, Sz16
        myCurrExt.modMemWord := (
          myD2hBus.data(
            (15.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrExt.modMemWord.getWidth).asUInt
        )
      }
      is (M"1110") {
        // LoadS, Sz32
        myCurrExt.modMemWord := (
          myD2hBus.data(
            (31.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrExt.modMemWord.getWidth).asUInt
        )
      }
      is (M"1111") {
        // LoadS, Sz64
        myCurrExt.modMemWord := (
          myD2hBus.data(
            (63.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrExt.modMemWord.getWidth).asUInt
        )
      }
      default {
      }
    }
    when (
      !myWbPayload(0).outpDecodeExt.memAccessKind.asBits(1)
      && myD2hBus.valid
    ) {
      val myDecodeExt = myWbPayload(1).outpDecodeExt
      val mapElem = myWbPayload(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myWbPayload(1).myExt(
            0
          )
        ) else (
          myWbPayload(1).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      //myCurrExt.modMemWord := myDbus.recvData.word
      //myCurrExt.modMemWord := myD2hBus.data
      //myCurrExt.modMemWordValid.foreach(current => {
      //  current := (
      //    // TODO: support more destination GPRs
      //    //!myWbPayload.gprIsZeroVec(0)
      //    True
      //  )
      //})
      for (idx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
        myCurrExt.modMemWordValid(idx) := (
          !myWbPayload(0).gprIsZeroVec.last(idx)
        )
      }
    }
  })
}
