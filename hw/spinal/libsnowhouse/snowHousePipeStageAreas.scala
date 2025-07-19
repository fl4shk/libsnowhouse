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
  var regFile: PipeMemRmw[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeMemRmwDualRdTypeDisabled[UInt, Bool],
  ]
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
case class BranchTgtBufElem(
  //mainWidth: Int,
  cfg: SnowHouseConfig,
  //optIncludeTargetEtc: Boolean,
) extends Bundle {
  // branch target buffer element
  val valid = Bool() // whether or not we even have a branch here.
  def fire = valid
  val branchKind = (
    cfg.haveBranchPredictor
  ) generate (
    Bits(
      //SnowHouseBranchPredictorKind.branchKindEnumMaxWidth bits
      cfg.optBranchPredictorKind.get._branchKindEnumWidth bits
    )
  )
  val dontPredict = (
    Bool()
  )
  val srcRegPc = UInt(cfg.mainWidth bits)
  val dstRegPc = UInt(cfg.mainWidth bits) 
  val dbgEncInstr = UInt(cfg.instrMainWidth bits)
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

  val nextPc = UInt(cfg.mainWidth bits)
  //val encInstr = Flow(UInt(cfg.instrMainWidth bits))
  val branchTgtBufElem = BranchTgtBufElem(cfg=cfg)
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
    UInt(cfg.mainWidth bits)
  )
  val upIsFiring = in(
    Bool()
  )
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

  val tgtBuf = (
    Mem(
      wordType=BranchTgtBufElem(
        cfg=cfg
      ),
      initialContent={
        Array.fill(branchTgtBufSize)(
          BranchTgtBufElem(
            cfg=cfg
          ).getZero
        )
      }
    )
    //.addAttribute(
    //  "asdf", "yes"
    //)
  )
  //val tgtBuf = (
  //  RamSimpleDualPort(
  //    wordType=BranchTgtBufElem(
  //      //mainWidth=cfg.mainWidth,
  //      cfg=cfg,
  //    ),
  //    depth=branchTgtBufSize,
  //    initBigInt=(
  //      Some(Array.fill(branchTgtBufSize)(BigInt(0)))
  //    )
  //  )
  //)
  //tgtBuf.io.ramIo.rdEn := True
  //tgtBuf.readAsync(
  //)
  val tgtBufRdAddr = UInt(log2Up(branchTgtBufSize) bits)
  val myPcAddrRange = (
    tgtBufRdAddr.high + log2Up(cfg.instrSizeBytes)
    downto log2Up(cfg.instrSizeBytes)
  )
  tgtBufRdAddr := (
    io.inpRegPc(myPcAddrRange) //- 1//- 2 //- 1 //- 2//- 3
  )
  //val myRdBtbElem = BranchTgtBufElem(cfg=cfg)
  //myRdBtbElem.assignFromBits(tgtBuf.io.ramIo.rdData)
  val myRdBtbElem = tgtBuf.readSync(
    address=tgtBufRdAddr
  )
  io.result.rdBtbElem := myRdBtbElem
  //val rRdBtbElem = RegNextWhen(
  //  next=nextRdBtbElem,
  //  cond=io.upIsFiring,
  //  init=nextRdBtbElem.getZero,
  //)
  val wrBtbElem = BranchTgtBufElem(
    cfg=cfg
  )
  val wrBranchKind = SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum()
  wrBranchKind.assignFromBits(wrBtbElem.branchKind)
  val tgtBufWrEn = (
    io.psExSetPc.valid
    //&& io.psExSetPc.btbWrEn
    && RegNext(
      next=io.psExSetPc.branchTgtBufElem.fire,
      init=False,
    ) //wrBtbElem.fire
    && !wrBtbElem.dontPredict
    //&& (
    //  wrBranchKind
    //  === SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum.BAK
    //)
    //&& wrBtbElem.branchKind
    //|| RegNext(next=io.stickyExSetPc(0).valid, init=False)
  )
  //tgtBuf.io.ramIo.rdAddr := (
  //  tgtBufRdAddr
  //)
  val tgtBufWrAddr = (
    //io.stickyExSetPc(0).nextPc(myPcAddrRange)
    RegNext(
      //io.psExSetPc.nextPc(myPcAddrRange)
      io.psExSetPc.branchTgtBufElem.srcRegPc(myPcAddrRange)
      //+ (1 * cfg.instrSizeBytes)
    )
    init(0x0)
  )
  //tgtBuf.write(
  //  address=io.stickyExSetPc(0).nextPc,
  //  data=wrBtbElem,
  //  enable=btbWrEn,
  //)
  //wrBtbElem := rRdBtbElem
  wrBtbElem := (
    RegNext(
      next=wrBtbElem,
      init=wrBtbElem.getZero,
    )
  )
  wrBtbElem.dstRegPc.allowOverride
  when (io.psExSetPc.valid) {
    wrBtbElem := (
      RegNext(
        next=io.psExSetPc.branchTgtBufElem,
        init=io.psExSetPc.branchTgtBufElem.getZero,
      )
    )
    //wrBtbElem.dstRegPc := io.psExSetPc.nextPc
  }
  wrBtbElem.valid.allowOverride
  wrBtbElem.valid := True

  val rdBranchKind = SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum()
  rdBranchKind.assignFromBits(myRdBtbElem.branchKind)
  io.result.valid := (
    myRdBtbElem.fire
  )
  val tempNextRegPc = (
    myRdBtbElem.dstRegPc //+ (1 * cfg.instrSizeBytes)
  )
  io.result.nextRegPc := (
    tempNextRegPc
  )
  //when (
  //  io.psExSetPc.valid
  //) {
  //}
  io.result.predictTkn := (
    rdBranchKind === SnowHouseBranchPredictorKind.FwdNotTknBakTknEnum.BAK
  )
  //tgtBuf.io.ramIo.wrData := wrBtbElem.asBits
  tgtBuf.write(
    address=tgtBufWrAddr,
    data=wrBtbElem,
    enable=tgtBufWrEn,
  )
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
  upModExt := (
    RegNext(
      next=upModExt,
      init=upModExt.getZero
    )
  )
  def myInstrCnt = upModExt.instrCnt
  val nextRegPc = SInt(cfg.mainWidth bits) //cloneOf(upModExt.regPc)
  val myRegPc = UInt(cfg.mainWidth bits)
  def myRegPcSetItCnt = upModExt.psIfRegPcSetItCnt
  val rPrevRegPcSetItCnt = {
    val temp = (
      RegNextWhen(
        next=myRegPcSetItCnt,
        cond=up.isFiring
      )
      init(0x0)
    )
    //init(-1)
    //temp.foreach(current => {
    //  current.init(0x0)
    //})
    //init(0x0)
    temp
  }
  myRegPcSetItCnt.allowOverride
  myRegPcSetItCnt := rPrevRegPcSetItCnt

  val stickyExSetPc = {
    val temp = /*KeepAttribute*/(
      Vec.fill(1)(
        /*Reg*/(Flow(
          SnowHousePsExSetPcPayload(cfg=cfg)
        ))
      )
    )
    temp.foreach(item => {
      //item.init(item.getZero)
      item := (
        RegNext(
          next=item,
          init=item.getZero,
        )
      )
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
    //for (idx <- 0 until stickyExSetPc.size) {
    //  branchPredictor.io.stickyExSetPc(idx) := stickyExSetPc(idx)
    //}
    //branchPredictor.io.upModExt := upModExt
    //branchPredictor.io.inpRegPc := 
    //branchPredictor.io.inpRegPc := myRegPc
    branchPredictor.io.upIsFiring := up.isFiring
  }

  when (
    psExSetPc.valid
  ) {
    stickyExSetPc.foreach(
      _.valid := True
      //_.extValid := True
    )
  }
  //psExSetPc.ready.setAsReg() init(False)
  //psExSetPc.ready := False

  stickyExSetPc(0).payload.allowOverride
  stickyExSetPc(0).payload := (
    RegNext(
      next=psExSetPc.payload,
      init=psExSetPc.payload.getZero,
    )
    //- cfg.instrSizeBytes
  )
  val myNextRegPcInit = (
    0
  )
  val rPrevRegPc = (
    RegNextWhen(
      next=(
        upModExt.regPc.asSInt
        //nextRegPc
        //+ (cfg.instrMainWidth / 8),
        //(myRegPc - (2 * cfg.instrSizeBytes)).asSInt
      ), 
      cond=(
        up.isFiring
      ),
    )
    init(
      //nextRegPc.asUInt.getZero

      //-(3.toLong * (cfg.instrMainWidth.toLong / 8.toLong))
      //myNextRegPcInit - (1.toLong * (cfg.instrMainWidth / 8.toLong))
      myNextRegPcInit - (1.toLong * cfg.instrSizeBytes.toLong).toLong
    )
  )
  val rPrevInstrCnt = /*(cfg.optFormal) generate*/ (
    RegNextWhen(
      next=myInstrCnt,
      cond=up.isFiring,
      init=myInstrCnt.getZero,
    )
  )
  //println(
  //  s"myNextRegPcInit:${myNextRegPcInit}"
  //)
  nextRegPc := (
    RegNext(nextRegPc)
    init(myNextRegPcInit)
  )
  myRegPc := (
    RegNext(myRegPc)
    init(myNextRegPcInit)
  )

  //io.ibus.sendData.addr := (
  //  RegNext(
  //    next=io.ibus.sendData.addr,
  //    init=io.ibus.sendData.addr.getZero,
  //  )
  //)
  val rTakeJumpAddr = {
    val temp = Reg(Flow(UInt(cfg.mainWidth bits)))
    temp.init(temp.getZero)
    temp
  }
  upModExt.encInstr.allowOverride
  upModExt.encInstr := (
    RegNext(
      next=upModExt.encInstr,
      init=upModExt.encInstr.getZero,
    )
  )
  //when (up.isFiring) {
  //  myRegPcSetItCnt := 0x0
  //  //upModExt.encInstr.valid := (
  //  //  //True
  //  //  //upModExt.psIfRegPcSetItCnt(0)
  //  //  //False
  //  //  True
  //  //)
  //  when (rTakeJumpAddr.fire) {
  //    when (
  //      //upModExt.regPc === 
  //      nextRegPc.asUInt === rTakeJumpAddr.payload
  //    ) {
  //      rTakeJumpAddr.valid := False
  //      myRegPcSetItCnt := 0x1
  //      //upModExt.encInstr.valid := (
  //      //  //True
  //      //  //upModExt.psIfRegPcSetItCnt(0)
  //      //  True
  //      //)
  //    } otherwise {
  //      //upModExt.encInstr.valid := False
  //    }
  //  } otherwise {
  //    //upModExt.encInstr.valid := True
  //  }
  //}
  //val myMuxedRegPc = (
  //  cfg.haveBranchPredictor
  //) generate (
  //  Mux[UInt](
  //    branchPredictor.io.result.fire,
  //    branchPredictor.io.result.nextRegPc,
  //    myRegPc,
  //  )
  //)
  io.ibus.sendData.addr := (
    RegNext(
      next=io.ibus.sendData.addr,
      init=io.ibus.sendData.addr.getZero,
    )
  )
  upModExt.regPc := (
    RegNext(
      next=upModExt.regPc,
      init=upModExt.regPc.getZero,
    )
  )
  val myHistRegPc = (
    History[UInt](
      that=upModExt.regPc,
      length=upModExt.myHistRegPc.size,
      when=cIf.up.isFiring,
      init=upModExt.regPc.getZero,
    )
  )
  upModExt.myHistRegPc.allowOverride
  upModExt.myHistRegPc := (
    myHistRegPc
  )

  if (cfg.haveBranchPredictor) {
    //branchPredictor.io.inpRegPc := (
    //  RegNext(
    //    next=branchPredictor.io.inpRegPc,
    //    init=branchPredictor.io.inpRegPc.getZero
    //  )
    //)
    branchPredictor.io.inpRegPc := (
      //myRegPc
      myHistRegPc(1) + (1 * cfg.instrSizeBytes)
    )
  }
  //if (cfg.haveBranchPredictor) {
  //}
  for (idx <- 0 until stickyExSetPc.size) {
    switch (
      Cat(
        List(
          up.isFiring,
          (
            //(
            //  (
            //    psExSetPc.fire
            //  ) || (
            //    stickyExSetPc(idx).fire
            //  )
            //)
            //&& stickyExSetPc(idx).extValid
            psExSetPc.valid
            || RegNext(stickyExSetPc(idx).fire, init=False)
          )
        ).reverse
      )
    ) {
      is (M"0-") {
      }
      is (M"10") {
        //myRegPcSetItCnt.foreach(current => {
        //  current := 0x0
        //})
        //myRegPcSetItCnt := 0x0
        //when (!rPrevRegPcSetItCnt.msb) {
        //  myRegPcSetItCnt := rPrevRegPcSetItCnt - 1
        //}
        //myRegPcSetItCnt.foreach(current => {
        //  current := 0x0
        //})
        //nextRegPc.assignFromBits(
        //  Mux[Bits](
        //    branchPredictor.io.result.predictTkn,
        //    branchPredictor.io.result.nextRegPc.asBits,
        //    temp.asBits
        //  )
        //)
        if (cfg.haveBranchPredictor) {
          val tempNextRegPc = (
            rPrevRegPc + cfg.instrSizeBytes
          )
          val predictCond = (
            //RegNextWhen(
            //  next=(
                branchPredictor.io.result.fire
                && branchPredictor.io.result.predictTkn
                && !rTakeJumpAddr.fire
            //  ),
            //  cond=cIf.up.isFiring,
            //  init=False,
            //)
            //branchPredictor.io.result.fire
            //&& branchPredictor.io.result.predictTkn
            //&& !rTakeJumpAddr.fire
          )
          val myPredictedNextPc = (
            Mux[SInt](
              (
                predictCond
              ),
              (
                //RegNextWhen(
                //  next=branchPredictor.io.result.nextRegPc.asSInt,
                //  cond=cIf.up.isFiring,
                //  init=(
                //    branchPredictor.io.result.nextRegPc.asSInt.getZero
                //  ),
                //)
                branchPredictor.io.result.nextRegPc.asSInt
                //- (
                //  1 * cfg.instrSizeBytes
                //)
                //branchPredictor.io.result.nextRegPc.asSInt
                //+ (1 * cfg.instrSizeBytes)
                //+ (2 * cfg.instrSizeBytes)
                //+ (1 * cfg.instrSizeBytes)
                //+ (3 * cfg.instrSizeBytes)
                //+ (1 * cfg.instrSizeBytes)
                //- (1 * cfg.instrSizeBytes)
                //- (3 * cfg.instrSizeBytes)
                //- (1 * cfg.instrSizeBytes)
                //- (2 * cfg.instrSizeBytes)
                //- (3 * cfg.instrSizeBytes)
                //- (4 * cfg.instrSizeBytes)
                //- (3 * cfg.instrSizeBytes)
                //- (2 * cfg.instrSizeBytes)
                //+ (2 * cfg.instrSizeBytes)
                //+ (1 * cfg.instrSizeBytes)
                //- (2 * cfg.instrSizeBytes)
                //+ (1 * cfg.instrSizeBytes)
                //- (1 * cfg.instrSizeBytes)
                //- (2 * cfg.instrSizeBytes)
                //- (1 * cfg.instrSizeBytes)
                //- (1 * cfg.instrSizeBytes)
                //- (3 * cfg.instrSizeBytes)
                //- (cfg.instrSizeBytes)
                //+ (cfg.instrSizeBytes)
              ),
              (
                //RegNextWhen(
                //  next=tempNextRegPc,
                //  cond=cIf.up.isFiring,
                //  init=tempNextRegPc.getZero,
                //) //.asSInt
                tempNextRegPc
                //rPrevRegPc + cfg.instrSizeBytes
                //myRegPc
              )
            ).asUInt //+ (3 * cfg.instrSizeBytes)
          )
          val temp = (
            //nextRegPc + (1 * cfg.instrSizeBytes)
            //nextRegPc + (2 * cfg.instrSizeBytes)
            myPredictedNextPc //+ (2 * cfg.instrSizeBytes)
          )
          myRegPc.assignFromBits(
            //(rPrevRegPc + cfg.instrSizeBytes).asBits
            //temp.asBits
            (
              //rPrevRegPc + cfg.instrSizeBytes
              tempNextRegPc
              //+ (2 * cfg.instrSizeBytes)
              //RegNextWhen(
              //  next=tempNextRegPc,
              //  cond=cIf.up.isFiring,
              //  init=tempNextRegPc.getZero,
              //)
            ).asBits
          )
          //when (predictCond) {
            io.ibus.sendData.addr := (
              //myMuxedRegPc
              temp//.asUInt //+ (2 * cfg.instrSizeBytes)
              //+ (1 * cfg.instrSizeBytes)
            )
            upModExt.regPc := (
              //io.ibus.sendData.addr //- (1 * cfg.instrSizeBytes)
              //nextRegPc.asUInt + (2 * cfg.instrSizeBytes)
              //myMuxedRegPc
              //nextRegPc + (1 * cfg.instrSizeBytes)
              temp//.asUInt //+ (2 * cfg.instrSizeBytes)
              //+ (1 * cfg.instrSizeBytes)
            )
            nextRegPc.assignFromBits(
              (upModExt.regPc - (2 * cfg.instrSizeBytes)).asBits
            )
          //} otherwise {
          //  io.ibus.sendData.addr := (
          //    //myMuxedRegPc
          //    temp//.asUInt //+ (2 * cfg.instrSizeBytes)
          //    //+ (1 * cfg.instrSizeBytes)
          //  )
          //  upModExt.regPc := (
          //    //io.ibus.sendData.addr //- (1 * cfg.instrSizeBytes)
          //    //nextRegPc.asUInt + (2 * cfg.instrSizeBytes)
          //    //myMuxedRegPc
          //    //nextRegPc + (1 * cfg.instrSizeBytes)
          //    temp//.asUInt //+ (2 * cfg.instrSizeBytes)
          //    //+ (1 * cfg.instrSizeBytes)
          //  )
          //  nextRegPc.assignFromBits(
          //    (
          //      //upModExt.regPc - (1 * cfg.instrSizeBytes)
          //      upModExt.regPc - (2 * cfg.instrSizeBytes)
          //    ).asBits
          //  )
          //}
          upModExt.branchPredictTkn.allowOverride
          upModExt.branchPredictTkn := (
            //branchPredictor.io.result.fire
            //&& branchPredictor.io.result.predictTkn
            RegNextWhen(
              next=(
                branchPredictor.io.result.fire
                && branchPredictor.io.result.predictTkn
              ),
              cond=cIf.up.isFiring,
              init=False,
            )
          )
          upModExt.branchTgtBufElem.foreach(item => {
            item := (
              //branchPredictor.io.result.rdBtbElem
              //psExSetPc.branchTgtBufElem
              RegNextWhen(
                next=branchPredictor.io.result.rdBtbElem,
                cond=cIf.up.isFiring,
                init=branchPredictor.io.result.rdBtbElem.getZero,
              )
              //RegNextWhen(
              //  next=psExSetPc.branchTgtBufElem,
              //  cond=cIf.up.isFiring,
              //  init=psExSetPc.branchTgtBufElem.getZero,
              //)
            )
          })
        } else {
          val temp = (
            //rPrevRegPcThenNext
            rPrevRegPc + cfg.instrSizeBytes
          )
          myRegPc.assignFromBits(
            //(temp + (2 * cfg.instrSizeBytes)).asBits
            temp.asBits
          )
          nextRegPc.assignFromBits(
            (temp - (2 * cfg.instrSizeBytes)).asBits
            //upModExt.myHistRegPc(2).asBits
          )
          io.ibus.sendData.addr := (
            myRegPc
          )
          upModExt.regPc := (
            //io.ibus.sendData.addr //- (1 * cfg.instrSizeBytes)
            //nextRegPc.asUInt + (2 * cfg.instrSizeBytes)
            myRegPc
          )
        }
      }
      default {
        //myRegPcSetItCnt.foreach(current => {
        //  current := 0x1
        //})
        //myRegPcSetItCnt := 0x1
        //stickyExSetPc := stickyExSetPc.getZero
        stickyExSetPc(idx).valid := stickyExSetPc(idx).valid.getZero
        rTakeJumpAddr.valid := True
        rTakeJumpAddr.payload := (
          stickyExSetPc(0).nextPc //- (1 * cfg.instrSizeBytes)
          //stickyExSetPc(0).nextPc + (2 * cfg.instrSizeBytes)
          //stickyExSetPc(0).nextPc - (1 * cfg.instrSizeBytes)
          //stickyExSetPc(0).nextPc //+ (1 * cfg.instrSizeBytes)
          //myRegPc - (1 * cfg.instrSizeBytes)
        )
        val temp = (
          //psExSetPc.nextPc - (3 * (cfg.instrSizeBytes))
          //--------
          // BEGIN: old lagging value
          //(stickyExSetPc(0).nextPc) - (2 * (cfg.instrSizeBytes))
          // END: old lagging value
          //--------
          stickyExSetPc(0).nextPc - (3 * cfg.instrSizeBytes)
          //--------
          //(stickyExSetPc(0).nextPc) - (3 * (cfg.instrSizeBytes))
          //(stickyExSetPc(0).nextPc) - (4 * (cfg.instrSizeBytes))
          //(stickyExSetPc(0).nextPc) - (5 * (cfg.instrSizeBytes))
          //(stickyExSetPc(0).nextPc) - (6 * (cfg.instrSizeBytes))
          //RegNext(psExSetPc.nextPc) - (3 * cfg.instrSizeBytes)
          //stickyExSetPc(0).nextPc - (4 * cfg.instrSizeBytes)
          //psExSetPc.nextPc - (3 * (cfg.instrSizeBytes))
        )
        nextRegPc.assignFromBits(
          //(temp - (2 * cfg.instrSizeBytes)).asBits
          myHistRegPc(2).asBits
        )
        val tempNextRegPc = (
          //(temp + (2 * cfg.instrSizeBytes)).asBits
          temp
        )
        myRegPc.assignFromBits(
          tempNextRegPc.asBits
          //temp.asBits
        )
        //upModExt.regPcPlus1Instr := (
        //  (temp + (3 * cfg.instrSizeBytes))
        //)
        io.ibus.sendData.addr := (
          //myRegPc
          tempNextRegPc//.asUInt
        )
        upModExt.regPc := (
          //io.ibus.sendData.addr //- (1 * cfg.instrSizeBytes)
          //nextRegPc.asUInt + (2 * cfg.instrSizeBytes)
          //myRegPc
          tempNextRegPc//.asUInt
        )
        if (cfg.haveBranchPredictor) {
          upModExt.branchPredictTkn := (
            //branchPredictor.io.result.predictTkn
            False
          )
        }
      }
    }
  }
  io.ibus.nextValid := (
    //True
    //up.isFiring
    //down.isFiring
    //up.isFiring
    //down.isReady
    //up.isReady
    True
    //up.isFiring
    //down.isReady
  )
  upModExt.regPc.allowOverride

  val rStallState = Reg(Bool(), init=False)

  when (!rStallState) {
    when (
      !io.ibus.ready
    ) {
      cIf.haltIt()
    } otherwise {
      upModExt.encInstr.payload := (
        io.ibus.recvData.instr
      )
      rStallState := True
    }
  }
  when (cIf.up.isFiring) {
    rStallState := False
  }

  when (up.isFiring) {
    //upModExt.encInstr.valid := True
    myRegPcSetItCnt := 0x0
    when (rTakeJumpAddr.fire) {
      when (rPrevRegPc.asUInt === rTakeJumpAddr.payload) {
        rTakeJumpAddr.valid := False
        myRegPcSetItCnt := 0x1
      } otherwise {
        //upModExt.encInstr.valid := False
      }
    }
  }

  when (cIf.down.isReady) {
    upModExt.encInstr.valid := (
      !upModExt.psIfRegPcSetItCnt(0)
      //True
    )
  }
}
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

  val rSavedExSetPc = {
    val temp = /*KeepAttribute*/(
      Reg(Flow(
        SnowHousePsExSetPcPayload(cfg=cfg)
      ))
    )
    temp.init(temp.getZero)
    temp.setName(s"psId_rSavedExSetPc")
  }

  val rShouldFinishJumpCnt = (
    Reg(UInt(4 bits))
    init(
      //0x2
      0xf
    )
  )

  when (
    //up.isFiring
    //&& 
    psExSetPc.valid
  ) {
    rSavedExSetPc.valid := True
    rSavedExSetPc.nextPc := (
      psExSetPc.nextPc //- (cfg.instrMainWidth.toLong / 8.toLong).toLong
    )
    //rShouldFinishJumpCnt := 0x2
  }

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

  val multiInstrCntWidth = 3
  val nextMultiInstrCnt = UInt(multiInstrCntWidth bits)
  val rMultiInstrCnt = (
    RegNext(
      next=nextMultiInstrCnt,
      init=(
        U(multiInstrCntWidth bits, default -> True)
      )
    )
  )
  nextMultiInstrCnt := rMultiInstrCnt
  val myInstr = UInt(cfg.instrMainWidth bits)
  //myInstr := (
  //  RegNext(
  //    next=myInstr,
  //    init=myInstr.getZero
  //  )
  //)
  //when (up.isValid) {
    //when (
    //  RegNext(next=io.ibus.nextValid, init=False)
    //) {
    //when (down.isReady) {
        //when (io.ibus.fire) {
        //}
      when (upPayload(1).encInstr.fire) {
        myInstr := (
          upPayload(1).encInstr.payload
        )
      } otherwise {
        myInstr := (
          0x0
        )
      }
      //when (!rSetUpPayloadState(1)) {
      //  when (
      //    //!(RegNext(io.ibus.fire) init(False))
      //    //|| 
      //    !io.ibus.ready//fire
      //    //|| shouldIgnoreInstr
      //  ) {
      //    //cId.haltIt()
      //    //cId.duplicateIt()
      //    //cId.throwIt()
      //    //cId.terminateIt()
      //  }
      //  .otherwise {
      //  //.elsewhen (up.isValid)
      //    //when (!rSetUpPayloadState(1)) {
      //      nextSetUpPayloadState(1) := True
      //      myInstr := (
      //        io.ibus.recvData.instr
      //      )
      //      when (
      //        //modIo.ibus.fire
      //        //&& 
      //        !rShouldFinishJumpCnt.msb
      //        && !psExSetPc.valid
      //        //&& !upPayload(1).psIfRegPcSetItCnt(0)
      //        //&& rSavedExSetPc.fire
      //      ) {
      //        rShouldFinishJumpCnt := rShouldFinishJumpCnt - 1
      //      }
      //    //}
      //  }
      //}
      //}
        //when (
        //  !rSetUpPayloadState(1)
        //  && (RegNext(io.ibus.fire) init(False))
        //) {
        ////when (
        ////  io.ibus.rValid
        ////  && io.ibus.ready
        ////  && !rSetUpPayloadState(1)
        ////) {
        //  //when (
        //  //  RegNext(io.ibus.ready) init(False)
        //  //) {
        //    nextSetUpPayloadState(1) := True
        //  //}
        //  myInstr := (
        //    RegNext(
        //      io.ibus.recvData.instr,
        //    )
        //    init(0x0)
        //  )
        //}
      //}
    //}
    //}
  //}
  //when (
  //  psExSetPc.valid
  //  //|| 
  //  //upPayload(1).psIfRegPcSetItCnt(0)
  //) {
  //  rShouldFinishJumpCnt := 0x2
  //}
  //val tempMyInstrCond = (
  //  (/*RegNext*/(io.ibus.ready) /*init(False)*/)
  //  && !rSetUpPayloadState(1)
  //)
  //myInstr := (
  //  io.ibus.recvData.instr
  //  //RegNextWhen(
  //  //  next=io.ibus.recvData.instr,
  //  //  cond=(
  //  //    tempMyInstrCond
  //  //  ),
  //  //)
  //  //init(0x0)
  //)
  //when (tempMyInstrCond) {
  //  nextSetUpPayloadState(1) := True
  //}
  //startDecode := False
  //when (
  //  (
  //    rSetUpPayloadState(1)
  //  ) && (
  //    !RegNext(next=rSetUpPayloadState(1), init=False)
  //  )
  //) {
  //  startDecode := True
  //  myInstr := (
  //    RegNext(io.ibus.recvData.instr) init(0x0)
  //  )
  //}
  //when (
  //  if (cfg.supportUcode) (
  //    rMultiInstrCnt.msb
  //  ) else (
  //    True
  //  )
  //) {
  upPayload(0) := up(pIf)
    when (
      up.isValid
      //&& up.isReady
      //True
      //down.isReady
      //&& io.ibus.ready
      //&& io.ibus.fire
    ) {
      //when (
      //  !rSetUpPayloadState(0)
      //) {
        //upPayload(0) := up(pIf)
        upPayload(1) := upPayload(0)
      //  nextSetUpPayloadState(0) := True
      //}
    }
  //} otherwise {
  //  cId.duplicateIt()
  //  when (down.isFiring) {
  //    nextMultiInstrCnt := rMultiInstrCnt - 1
  //  }
  //}
  //val rShouldFinishJumpState = (
  //  Reg(Bool(), init=False)
  //)
  //val rFinishJumpCnt = (
  //  Reg(UInt(3 bits))
  //  init(0x0)
  //)
  val shouldFinishJump = (
    //rShouldFinishJumpCnt.msb
    //&& !upPayload(1).psIfRegPcSetItCnt(0)
    //&& rSavedExSetPc.fire
    //upPayload(1).psIfRegPcSetItCnt(0)
    //&& (
    //  (
    //    //upPayload(1).regPc //+ (1 * cfg.instrSizeBytes)
    //    io.ibus.sendData.addr
    //    //upPayload.regPcMinus1Instr
    //    //upPayload.regPcPlus1Instr
    //    === (
    //      (
    //        rSavedExSetPc.nextPc + (2 * cfg.instrSizeBytes)
    //        //- cfg.instrSizeBytes
    //        //- (cfg.instrMainWidth.toLong / 8.toLong).toLong
    //      )
    //    )
    //  )
    //)

    upPayload(1).psIfRegPcSetItCnt(0)
    //|| 
    //RegNextWhen(
    //  next=upPayload(1).psIfRegPcSetItCnt(0),
    //  cond=(
    //    up.isFiring
    //    //up.isValid
    //  ),
    //  init=upPayload(1).psIfRegPcSetItCnt(0).getZero,
    //)
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
    //upPayload.regPcSetItCnt(idx) := upPayload.regPcSetItCnt(0)
    //upPayload.regPcSetItCnt(idx) := 0x0
    //upPayload.regPcSetItCnt(idx) := (
    //  RegNextWhen(
    //    next=upPayload.regPcSetItCnt(idx),
    //    cond=up.isFiring,
    //  )
    //  init(0x0)
    //)
    when (up.isFiring) {
      when (
        (
          //rSavedExSetPc.fire
          //&& (
          //  (
          //    upPayload.regPc
          //    //upPayload.regPcMinus1Instr
          //    //upPayload.regPcPlus1Instr
          //    === (
          //      (
          //        rSavedExSetPc.nextPc
          //        - cfg.instrSizeBytes
          //        //- (cfg.instrMainWidth.toLong / 8.toLong).toLong
          //      )
          //    )
          //  )
          //)
          shouldFinishJump
        )
        //|| (
        //  upPayload.psIfRegPcSetItCnt === 0x1
        //)
        //upPayload.psExSetPc.fire
      ) {
        //rSavedExSetPc.valid := False
        //rSavedExSetPc.payload := rSavedExSetPc.payload.getZero
        upPayload(1).regPcSetItCnt(idx) := 0x1
      } otherwise {
        upPayload(1).regPcSetItCnt(idx) := 0x0
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
    upPayload(1).regPc - (1 * cfg.instrSizeBytes) //- cfg.instrSizeBytes
    ////- (cfg.instrMainWidth.toLong / 8.toLong)
    //upPayload.regPcPlus1Instr
  )
  upPayload(1).regPcPlusImm := (
    (
      upPayload(1).regPc.asSInt
      + upPayload(1).imm(2).asSInt
      //- (2 * cfg.instrSizeBytes)
      - (1 * cfg.instrSizeBytes)
      //+ Mux[SInt](
      //  upPayload(1).branchPredictTkn,
      //  (
      //    S(s"${cfg.mainWidth}'d0")
      //    //S(s"${cfg.mainWidth}'d${3 * cfg.instrSizeBytes}"),
      //  ),
      //  -S(s"${cfg.mainWidth}'d${(2 * cfg.instrSizeBytes)}"),
      //)
    ).asUInt
    //- (cfg.instrMainWidth.toLong / 8.toLong)
  )
  val upGprIdxToMemAddrIdxMap = upPayload(1).gprIdxToMemAddrIdxMap
  for ((gprIdx, zdx) <- upPayload(1).gprIdxVec.view.zipWithIndex) {
    upPayload(1).myExt(0).memAddr(zdx) := gprIdx
  }
  //if (cfg.optFormal) {
  //  when (pastValidAfterReset()) {
  //    when (
  //      !past(up.isFiring)
  //      && io.ibus.ready
  //    ) {
  //      assume(stable(io.ibus.ready))
  //    }
  //    when (past(io.ibus.nextValid)) {
  //      when (io.ibus.ready) {
  //        cover(up.isFiring)
  //        assert(
  //          up.isValid
  //        )
  //        when (!io.ibus.nextValid) {
  //          assume(!(RegNext(next=io.ibus.ready, init=False)))
  //        }
  //      }
  //    }
  //  }
  //}
  //val nextDoDecodeState = Bool()
  //val rDoDecodeState = RegNext(
  //  next=nextDoDecodeState,
  //  init=nextDoDecodeState.getZero,
  //)
  //nextDoDecodeState := rDoDecodeState

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
  val nextPrevInstrBlockedIrq = (
    //true
    cfg.irqCfg != None
  ) generate (
    Bool()
  )
  val rPrevInstrBlockedIrq = (
    //true
    cfg.irqCfg != None
  ) generate (
    RegNextWhen(
      next=nextPrevInstrBlockedIrq,
      cond=up.isFiring,
      init=nextPrevInstrBlockedIrq.getZero,
    )
  )
  if (cfg.irqCfg != None) {
    nextPrevInstrBlockedIrq := rPrevInstrBlockedIrq
  }
  //val tempIsFiring = (
  //  /*KeepAttribute*/(
  //    Bool()
  //  )
  //  .setName(s"GenInstrDecode_tempIsFiring")
  //)
  //tempIsFiring := up.isFiring
  val myDecodeAreaWithoutUcode = (
    !cfg.supportUcode
  ) generate(
    doDecodeFunc(this)
  )
  //val rPrevRegPcSetItCnt = (
  //  RegNext/*When*/(
  //    next=upPayload.regPcSetItCnt,
  //    //cond=up.isFiring,
  //  )
  //  init(-1)
  //)
  //when (
  //  psExSetPc.fire
  //) {
  //  upPayload.regPcSetItCnt := 2
  //}
  //when (up.isFiring) {
  //  when (!rPrevRegPcSetItCnt.msb) {
  //    upPayload.regPcSetItCnt := (
  //      rPrevRegPcSetItCnt - 1
  //    )
  //  }
  //}
  //when (up.isValid) {
    startDecode := True
    tempInstr := myInstr
    //startDecode := True
    //tempInstr := myInstr
  //}
  //cfg.irqCfg match {
  //  case Some(irqCfg) => {
  //    when (up.isFiring) {
  //      //irqCfg match {
  //      //  case iraIds: SnowHouseIrqConfig.IraIds(_) => {
  //      //  }
  //      //}
  //    }
  //  }
  //  case None => {
  //  }
  //}
  //if (cfg.irqCfg != None) {
  //  when (
  //    up.isFiring
  //    //&& rPrevInstrBlockedIrq
  //  ) {
  //  }
  //}
  //when (up.isValid) {
  //  when (
  //    if (cfg.supportUcode) (
  //      !pcChangeState(0)
  //      || (
  //        pcChangeState(0)
  //        && upPayload.regPcSetItCnt =/= 0x0
  //      )
  //    ) else (
  //      True
  //    )
  //  ) {
  //    startDecode := True
  //    tempInstr := myInstr
  //    val myDecodeAreaWithUcode = (
  //      cfg.supportUcode
  //    ) generate(
  //      doDecodeFunc(this)
  //    )
  //    when (
  //      if (cfg.supportUcode) (
  //        rMultiInstrCnt.msb// === 0x0
  //      ) else (
  //        True
  //      )
  //    ) {
  //      val irqArea = (
  //        cfg.irqCfg != None
  //      ) generate (
  //        new Area {
  //          when (tempIsFiring) {
  //            when (
  //              (
  //                !upPayload.blockIrq
  //                || rPrevInstrBlockedIrq
  //              ) && (
  //                RegNext(io.idsIraIrq.nextValid)
  //              )
  //            ) {
  //              upPayload.takeIrq := True
  //            }
  //            nextPrevInstrBlockedIrq := (
  //              upPayload.blockIrq
  //            )
  //          }
  //        }
  //      )
  //    }
  //  }
  //}
//when (
//  up.isValid
//  //&& !upPayload.psExSetPc.fire
//) {
//  val myDecodeArea = doDecodeFunc(this)
//  //when (
//  //  pcChangeState
//  //  && upPayload.regPcSetItCnt =/= 0x0
//  //) {
//  //  nextPrevInstrWasJump := False
//  //}
//}
//val myDecodeArea = doDecodeFunc(this)
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
  val splitOp = setAsInp(SnowHouseSplitOp(cfg=cfg))
  val tempVecSize = 3 // TODO: temporary size of `3`
  val gprIsZeroVec = (
    cfg.myHaveZeroReg
  ) generate (
    setAsInp(
      Vec.fill(tempVecSize)(
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
      Vec.fill(tempVecSize)(
        Vec.fill(cfg.regFileCfg.modMemWordValidSize)(
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
  val laggingRegPc = setAsInp(UInt(cfg.mainWidth bits))
  val regPcSetItCnt = setAsInp(Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
    UInt(
      1 bits
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
    setAsOutp(
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
  def selRdMemWord(
    opInfo: OpInfo,
    idx: Int,
  ): UInt = {
    def innerFunc(
      idx: Int,
      isPostPcDst: Boolean,
    ) = {
      if (
        idx == 0
        || isPostPcDst
      ) {
        opInfo.dstArr(idx) match {
          case DstKind.Gpr => {
            rdMemWord(idx)
          }
          case DstKind.Spr(kind) => {
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
          case _ => {
            assert(
              false,
              s"not yet implemented: "
              + s"opInfo(${opInfo} ${opInfo.select}) "
              + s"${opInfo.dstArr(idx)}"
            )
            U(s"${cfg.mainWidth}'d0")
          }
        }
      } else {
        val tempIdx = idx - 1
        opInfo.srcArr(tempIdx) match {
          case SrcKind.Gpr => {
            rdMemWord(idx)
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
    opInfo.select match {
      case OpSelect.Cpy => {
        opInfo.cpyOp.get match {
          case CpyOpKind.Br => {
            for ((dst, dstIdx) <- opInfo.dstArr.view.zipWithIndex) {
              if (dst == DstKind.Gpr) {
                return innerFunc(idx=idx + 1, isPostPcDst=true)
              }
            }
            return innerFunc(idx=idx, isPostPcDst=false)
          }
          case _ => {
            return innerFunc(idx=idx, isPostPcDst=false)
          }
        }
      }
      case OpSelect.Alu => {
        if (opInfo.dstArr(0) == DstKind.Spr(SprKind.AluFlags)) {
          return innerFunc(idx=(idx + 1), isPostPcDst=false)
        } else {
          return innerFunc(idx=idx, isPostPcDst=false)
        }
      }
      case OpSelect.AluShift => {
        if (opInfo.dstArr(0) == DstKind.Spr(SprKind.AluFlags)) {
          return innerFunc(idx=(idx + 1), isPostPcDst=false)
        } else {
          return innerFunc(idx=idx, isPostPcDst=false)
        }
      }
      case _ => {
        return innerFunc(idx=idx, isPostPcDst=false)
      }
    }
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
  //def opIsAluShift = decodeExt.opIsAluShift
  def opIsJmp = outpDecodeExt.opIsJmp
  def opIsAnyMultiCycle = outpDecodeExt.opIsAnyMultiCycle
  def opIsMultiCycle = outpDecodeExt.opIsMultiCycle
  def jmpAddrIdx = (
    1
  )
  def brCondIdx = Array[Int](0, 1)
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
  val modIo = args.io
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
  //val myModMemWordValid = (
  //  if (cfg.myHaveZeroReg) (
  //    // TODO: support more register simultaneous writes
  //    !io.gprIsZeroVec(0)
  //  ) else (
  //    True
  //  )
  //)
  for (idx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
    io.modMemWordValid(idx) := (
      if (cfg.myHaveZeroReg) (
        //!io.gprIsZeroVec(0)(idx)
        io.gprIsNonZeroVec(0)(idx)
      ) else (
        True
      )
      //myModMemWordValid
    )
  }

  val nextExSetPcValid = Bool()
  case class SetPcCmp(
    //mulAcc: LcvMulAcc32Del1
    //adder: LcvAddDel1,
    //cmpEqDel1: LcvCmpEqDel1,
  ) extends Area {
    val nextValid = (
      //Reg(Bool(), init=False)
      Bool()
    )
    nextValid := (
      RegNext(
        next=nextValid,
        init=nextValid.getZero,
      )
    )
    //val nextValid = 
    val myCmp = UInt(cfg.mainWidth + 1 bits)
    //val myStickyCmp = Bool()
    //val mulAccIo = (
    //  LcvMulAcc32Io(
    //    optIncludeClk=true
    //  )
    //)
    //val mulAcc = LcvMulAcc32Del1()
    val left = io.rdMemWord(io.brCondIdx(0))
    val right = io.rdMemWord(io.brCondIdx(1))
    val cmpEq = (
      left === right
    )
    //val (cmpEq, cmpEqQ) = (
    //  LcvFastCmpEq(
    //    //left=RegNext/*When*/(
    //    //  next=io.rdMemWord(io.brCondIdx(0)),
    //    //  //cond=io.upIsValid,
    //    //  init=io.rdMemWord(io.brCondIdx(0)).getZero,
    //    //),
    //    //right=RegNext/*When*/(
    //    //  next=io.rdMemWord(io.brCondIdx(1)),
    //    //  //cond=io.upIsValid,
    //    //  init=io.rdMemWord(io.brCondIdx(1)).getZero,
    //    //),
    //    left=io.rdMemWord(io.brCondIdx(0)),
    //    right=io.rdMemWord(io.brCondIdx(1)),
    //    //mulAccIo=(
    //    //  //mulAccIo
    //    //  mulAcc.io
    //    //),
    //    //addIo=(
    //    //  adder.io
    //    //),
    //    cmpEqIo=(
    //      //cmpEqIo
    //      cmpEqDel1.io
    //    ),
    //    //optDsp=true,
    //    //optReg=true,
    //    //kind=LcvFastCmpEq.Kind.UseFastCarryChain,
    //  )
    //)
    //mulAcc.io <> mulAccIo
  }
  //val mySetPcCmpEqAdder = LcvAddDel1(cfg.mainWidth + 1)
  //mySetPcCmpEqAdder.io.do_inv := False

  //val myCmpEqDel1ForEq = LcvCmpEqDel1(cfg.mainWidth)
  val myPsExSetPcCmpEq = SetPcCmp(
    //cmpEqDel1=myCmpEqDel1ForEq
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
    //cmpEqDel1=myCmpEqDel1ForNe
  )

  nextExSetPcValid := False
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
  //myPsExSetPcCmpEq.myStickyCmp := (
  //  RegNext(
  //    next=myPsExSetPcCmpEq.myStickyCmp,
  //    init=myPsExSetPcCmpEq.myStickyCmp.getZero,
  //  )
  //)
  //when (io.shouldIgnoreInstr(2)) {
  //  myPsExSetPcCmpEq.myStickyCmp := False
  //}

  //myPsExSetPcCmpNe.myStickyCmp := (
  //  RegNext(
  //    next=myPsExSetPcCmpNe.myStickyCmp,
  //    init=myPsExSetPcCmpNe.myStickyCmp.getZero,
  //  )
  //)
  //val myStickySetPcCond = (
  //  RegNext(
  //    next=(
  //      Vec.fill(2)(
  //        io.regPcSetItCnt(2)(0)
  //        && io.upIsFiring
  //        //&& io.upIsValid
  //      )
  //    ),
  //    init=Vec.fill(2)(False)
  //  )
  //)
  //when (
  //  //io.upIsFiring
  //) {
  //  myPsExSetPcCmpEq.myStickyCmp := False
  //  myPsExSetPcCmpNe.myStickyCmp := False
  //}

  //when (
  //  //io.shouldIgnoreInstr(2)
  //  myStickySetPcCond(0)
  //) {
  //  myPsExSetPcCmpEq.myStickyCmp := False
  //} elsewhen (
  //  myPsExSetPcCmpEq
  //  && RegNext(
  //    next=(
  //      !myPsExSetPcCmpEq
  //      && io.upIsFiring
  //    ),
  //    init=False,
  //  )
  //) {
  //  myPsExSetPcCmpEq.myStickyCmp := True
  //}

  //when (
  //  //io.shouldIgnoreInstr(2)
  //  myStickySetPcCond(0)
  //) {
  //  myPsExSetPcCmpNe.myStickyCmp := False
  //} elsewhen (
  //  myPsExSetPcCmpNe
  //  && RegNext(
  //    next=(
  //      !myPsExSetPcCmpNe
  //      && io.upIsFiring
  //    ),
  //    init=False,
  //  )
  //) {
  //  myPsExSetPcCmpNe.myStickyCmp := True
  //}
  //when (myPsExSetPcCmpEq.myCmp.msb) {
  //  myPsExSetPcCmpEq.myStickyCmp := True
  //}
  //when (myPsExSetPcCmpNe.myCmp.msb) {
  //  myPsExSetPcCmpNe.myStickyCmp := True
  //}
  //when (
  //  //RegNext(
  //  //  next=(
  //      io.upIsFiring
  //      && (
  //        myPsExSetPcCmpEq.myCmp.msb
  //        || RegNext(next=myPsExSetPcCmpEq.myStickyCmp, init=False)
  //      )
  //  //  ),
  //  //  init=False
  //  //)
  //) {
  //  myPsExSetPcCmpEq.myStickyCmp := False
  //}
  //when (
  //  //RegNext(
  //  //  next=(
  //      io.upIsFiring
  //      && (
  //        myPsExSetPcCmpNe.myCmp.msb
  //        || RegNext(next=myPsExSetPcCmpNe.myStickyCmp, init=False)
  //      )
  //  //  ),
  //  //  init=False
  //  //)
  //) {
  //  myPsExSetPcCmpNe.myStickyCmp := False
  //}

  val myPsExSetPcValid = (
    /*LcvFastOrR*/(
      Vec[Bool](
        /*RegNext*//*When*/(
          /*next=*/nextExSetPcValid//,
          ////cond=(!io.shouldIgnoreInstr.last),
          //init=nextExSetPcValid.getZero
        ),
        myPsExSetPcCmpEq.myCmp.msb,
        //RegNext(myPsExSetPcCmpEq.myStickyCmp, init=False),
        myPsExSetPcCmpNe.myCmp.msb,
        //RegNext(myPsExSetPcCmpNe.myStickyCmp, init=False),
      ).asBits.asUInt.orR
      //optDsp=false
    )
  )
  //io.psExSetPc.valid := (
  //  Mux
  //)
  val tempPsExSetPcValid = Bool()
  val tempPsExSetPcTaken = Bool()
  when (
    //RegNext(
    //  next=(
        //rose(
          io.btbElemValid
          && !io.btbElemDontPredict
    //    //)
    //  ),
    //  init=False
    //)
  ) {
    tempPsExSetPcValid := (
      /*rose*/(
        myPsExSetPcValid
        =/= /*RegNext(next=*/io.branchPredictTkn/*, init=False)*/
      )
    )
    when (tempPsExSetPcValid && !myPsExSetPcValid) {
      tempPsExSetPcTaken := False
    } otherwise {
      tempPsExSetPcTaken := True
    }
  } otherwise {
    tempPsExSetPcValid := (
      //False
      /*rose*/(
        myPsExSetPcValid
      )
    )
    tempPsExSetPcTaken := (
      myPsExSetPcValid
    )
  }
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
    RegNext(
      next=tempPsExSetPcValid,
      init=tempPsExSetPcValid.getZero,
    )
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
  nextIds := io.rIds
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
  nextIndexReg := io.rIndexReg
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
  nextHadRetIra := False
  if (io.haveRetIraState) {
    io.rHadRetIra.setAsReg() init(False)
    when (io.upIsFiring) {
      io.rHadRetIra := nextHadRetIra
    }
  }
  //io.psExSetPc.nextPc.allowOverride
  //io.psExSetPc.nextPc := (
  //  io.regPcPlusImm 
  //)
  io.dbusHostPayload.data := io.rdMemWord(0) //selRdMemWord(0)
  if (cfg.allMainLdstUseGprPlusImm) {
    io.dbusHostPayload.addr := io.rdMemWord(1) + io.imm(1)
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
  ): Unit = {
    def selRdMemWord(
      srcArrIdx: Int
    ): UInt = {
      io.selRdMemWord(opInfo=opInfo, idx=srcArrIdx)
    }
    assert(
      opInfo.dstArr.size == 1 || opInfo.dstArr.size == 2,
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
        opInfo.cpyOp.get match {
          case CpyOpKind.Cpy => {
            nextIndexReg := 0x0
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
                    io.modMemWord(0) := selRdMemWord(1)
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
                        nextAluFlags := selRdMemWord(1)
                      }
                      case SprKind.Ids => {
                        nextIds := selRdMemWord(1)
                      }
                      case SprKind.Ira => {
                        nextIra := selRdMemWord(1)
                      }
                      case SprKind.Ie => {
                        nextIe := selRdMemWord(1)(0)
                      }
                      case SprKind.Ity => {
                        nextIty := selRdMemWord(1)
                      }
                      case SprKind.Sty => {
                        nextSty := selRdMemWord(1)
                      }
                      case SprKind.Hi => {
                        nextHi := selRdMemWord(1)
                      }
                      case SprKind.Lo => {
                        nextLo := selRdMemWord(1)
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
                        nextIndexReg := selRdMemWord(1)
                      }
                      case HiddenRegKind.MulHiOutp => {
                        nextMulHiOutp := selRdMemWord(1)
                      }
                      case HiddenRegKind.DivHiOutp => {
                        nextDivHiOutp := selRdMemWord(1)
                      }
                      case HiddenRegKind.ModHiOutp => {
                        nextModHiOutp := selRdMemWord(1)
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
                if (!mem.isAtomic) {
                  val isStore = mem.isStore
                  if (!isStore) {
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  } else {
                    //io.modMemWordValid.foreach(current => {
                    //  current := True
                    //})
                    io.modMemWord(0) := selRdMemWord(0)
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
                        case AddrCalcKind.AddReduce(
                        ) => (
                          selRdMemWord(1)
                        )
                        case kind:
                        AddrCalcKind.LslThenMaybeAdd => (
                          selRdMemWord(1)
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
                          tempAddr + selRdMemWord(2)
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
                } else {
                  assert(
                    false,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                }
              }
            }
          }
          case CpyOpKind.Cpyu => {
            nextIndexReg := 0x0
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
          case CpyOpKind.Jmp => {
            nextIndexReg := 0x0
            assert(
              opInfo.dstArr.size == 1
              || opInfo.dstArr.size == 2,
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
              if (opInfo.dstArr.size > 1) {
                when (io.gprIsNonZeroVec(0)(0)) {
                  io.modMemWord(0) := (
                    //io.regPc + ((cfg.instrMainWidth / 8) * 1)
                    io.regPcPlusInstrSize
                  )
                } otherwise {
                  io.modMemWord(0) := (
                    0x0
                  )
                }
              } else {
                io.modMemWord.foreach(item => {
                  item := io.rdMemWord(0)
                })
              }
              nextExSetPcValid := True
              myPsExSetPcCmpEq.nextValid := (
                False
                //0x0
              )
              myPsExSetPcCmpNe.nextValid := (
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
                    nextHadRetIra := True
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
            //}
          }
          case CpyOpKind.Br => {
            if (opInfo.dstArr.size == 1) (
              io.modMemWordValid.foreach(current => {
                current := False
              })
            )
            nextIndexReg := 0x0
            opInfo.cond match {
              case CondKind.Always => {
                nextExSetPcValid := True
                myPsExSetPcCmpEq.nextValid := (
                  False
                  //0x0
                )
                myPsExSetPcCmpNe.nextValid := (
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
                  when (io.gprIsNonZeroVec(0)(0)) {
                    io.modMemWord(0) := (
                      //io.regPc + ((cfg.instrMainWidth / 8) * 1)
                      io.regPcPlusInstrSize
                    )
                  } otherwise {
                    io.modMemWord(0) := (
                      0x0
                    )
                  }
                } else {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (opInfo.dstArr.size == 1) (
                  io.modMemWordValid.foreach(current => {
                    current := False
                  })
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
                  nextExSetPcValid := (
                    (io.rFlagZ) //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  nextExSetPcValid := (
                    //io.psExSetPc.valid
                    False
                  )
                  if (opInfo.dstArr.size == 1) {
                    io.modMemWord.foreach(item => {
                      item := io.rdMemWord(0)
                    })
                  }
                  when (!io.shouldIgnoreInstr(2)) {
                    myPsExSetPcCmpEq.nextValid := (
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
                  myPsExSetPcCmpNe.nextValid := (
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
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  nextExSetPcValid := (
                    (!io.rFlagZ) //init(False)
                  )
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  nextExSetPcValid := {
                    False
                    //(
                    //  (
                    //    io.rdMemWord(io.brCondIdx(0))
                    //    =/= io.rdMemWord(io.brCondIdx(1))
                    //  )
                    //  //init(False)
                    //)
                    //val q = Bool()
                    //val unusedSumOut = UInt(cfg.mainWidth bits)
                    //(
                    //  q,
                    //  unusedSumOut
                    //) := (
                    //  (
                    //    Cat(
                    //      False,
                    //      (
                    //        io.rdMemWord(io.brCondIdx(0))
                    //        ^ (
                    //          ~io.rdMemWord(io.brCondIdx(1))
                    //        )
                    //      )
                    //    ).asUInt
                    //  ) + (
                    //    Cat(
                    //      U{
                    //        val myWidth = (
                    //          io.rdMemWord(io.brCondIdx(0)).getWidth
                    //        )
                    //        f"${myWidth}'d0"
                    //      },
                    //      True
                    //    ).asUInt
                    //  )
                    //)
                    //(!q)
                    //!LcvFastCmpEq(
                    //  left=io.rdMemWord(io.brCondIdx(0)),
                    //  right=io.rdMemWord(io.brCondIdx(1)),
                    //  optDsp=true,
                    //  optReg=true,
                    //)._1
                  }
                  myPsExSetPcCmpEq.nextValid := (
                    False
                    //0x0
                  )
                  when (!io.shouldIgnoreInstr(2)) {
                    myPsExSetPcCmpNe.nextValid := (
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
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                nextExSetPcValid := (
                  (io.rFlagN) //init(False)
                )
              }
              case CondKind.Pl => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                nextExSetPcValid := (
                  (!io.rFlagN) //init(False)
                )
              }
              case CondKind.Vs => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                nextExSetPcValid := (
                  (io.rFlagV) //init(False)
                )
              }
              case CondKind.Vc => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                nextExSetPcValid := (
                  (!io.rFlagV) //init(False)
                )
              }
              case CondKind.Geu => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  nextExSetPcValid := (
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
                  //nextExSetPcValid := {
                  //  (
                  //    (
                  //      io.rdMemWord(io.brCondIdx(0))
                  //      >= io.rdMemWord(io.brCondIdx(1))
                  //    )
                  //    //init(False)
                  //  )
                  //  //(
                  //  //  Cat(False, io.rdMemWord(io.brCondIdx(0))).asUInt
                  //  //  + Cat(False, ~io.rdMemWord(io.brCondIdx(1))).asUInt
                  //  //  + Cat(
                  //  //    U{
                  //  //      val myWidth = (
                  //  //        io.rdMemWord(io.brCondIdx(0)).getWidth
                  //  //      )
                  //  //      f"${myWidth}'d0"
                  //  //    },
                  //  //    True
                  //  //  ).asUInt
                  //  //).msb
                  //  //val myBinop = AluOpKind.Sub.binopFunc(
                  //  //  cfg=cfg,
                  //  //  left=io.rdMemWord(io.brCondIdx(0)),
                  //  //  right=io.rdMemWord(io.brCondIdx(1)),
                  //  //  carry=(
                  //  //    False
                  //  //  )
                  //  //)(
                  //  //  width=cfg.mainWidth
                  //  //)
                  //  //myBinop.flagC
                  //}
                }
              }
              case CondKind.Ltu => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  nextExSetPcValid := (
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
                  //nextExSetPcValid := {
                  //  (
                  //    (
                  //      io.rdMemWord(io.brCondIdx(0))
                  //      < io.rdMemWord(io.brCondIdx(1))
                  //    )
                  //    //init(False)
                  //  )
                  //  //val myBinop = AluOpKind.Sub.binopFunc(
                  //  //  cfg=cfg,
                  //  //  left=io.rdMemWord(io.brCondIdx(0)),
                  //  //  right=io.rdMemWord(io.brCondIdx(1)),
                  //  //  carry=(
                  //  //    False
                  //  //  )
                  //  //)(
                  //  //  width=cfg.mainWidth
                  //  //)
                  //  //(!myBinop.flagC)
                  //}
                }
              }
              case CondKind.Gtu => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  nextExSetPcValid := (
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
                  //nextExSetPcValid := {
                  //  (
                  //    (
                  //      io.rdMemWord(io.brCondIdx(0))
                  //      > io.rdMemWord(io.brCondIdx(1))
                  //    )
                  //    //init(False)
                  //  )
                  //  //val myBinop = AluOpKind.Sub.binopFunc(
                  //  //  cfg=cfg,
                  //  //  left=io.rdMemWord(io.brCondIdx(0)),
                  //  //  right=io.rdMemWord(io.brCondIdx(1)),
                  //  //  carry=(
                  //  //    False
                  //  //  )
                  //  //)(
                  //  //  width=cfg.mainWidth
                  //  //)

                  //  //(myBinop.flagC && !myBinop.flagZ)
                  //}
                }
              }
              case CondKind.Leu => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  nextExSetPcValid := (
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
                  //nextExSetPcValid := {
                  //  (
                  //    (
                  //      io.rdMemWord(io.brCondIdx(0))
                  //      <= io.rdMemWord(io.brCondIdx(1))
                  //    )
                  //    //init(False)
                  //  )
                  //  //val myBinop = AluOpKind.Sub.binopFunc(
                  //  //  cfg=cfg,
                  //  //  left=io.rdMemWord(io.brCondIdx(0)),
                  //  //  right=io.rdMemWord(io.brCondIdx(1)),
                  //  //  carry=(
                  //  //    False
                  //  //  )
                  //  //)(
                  //  //  width=cfg.mainWidth
                  //  //)
                  //  //(!myBinop.flagC || myBinop.flagZ)
                  //}
                }
              }
              case CondKind.Ges => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  nextExSetPcValid := (
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
                  //nextExSetPcValid := {
                  //  (
                  //    (
                  //      io.rdMemWord(io.brCondIdx(0)).asSInt
                  //      >= io.rdMemWord(io.brCondIdx(1)).asSInt
                  //    )
                  //    //init(False)
                  //  )
                  //  //val myBinop = AluOpKind.Sub.binopFunc(
                  //  //  cfg=cfg,
                  //  //  left=io.rdMemWord(io.brCondIdx(0)),
                  //  //  right=io.rdMemWord(io.brCondIdx(1)),
                  //  //  carry=(
                  //  //    False
                  //  //  )
                  //  //)(
                  //  //  width=cfg.mainWidth
                  //  //)
                  //  //!(myBinop.flagN ^ myBinop.flagV)
                  //}
                }
              }
              case CondKind.Lts => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  nextExSetPcValid := (
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
                  //nextExSetPcValid := {
                  //  (
                  //    (
                  //      io.rdMemWord(io.brCondIdx(0)).asSInt
                  //      < io.rdMemWord(io.brCondIdx(1)).asSInt
                  //    )
                  //    //init(False)
                  //  )
                  //  //val myBinop = AluOpKind.Sub.binopFunc(
                  //  //  cfg=cfg,
                  //  //  left=io.rdMemWord(io.brCondIdx(0)),
                  //  //  right=io.rdMemWord(io.brCondIdx(1)),
                  //  //  carry=(
                  //  //    False
                  //  //  )
                  //  //)(
                  //  //  width=cfg.mainWidth
                  //  //)
                  //  //(myBinop.flagN ^ myBinop.flagV)
                  //}
                }
              }
              case CondKind.Gts => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  nextExSetPcValid := (
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
                  //nextExSetPcValid := {
                  //  (
                  //    (
                  //      io.rdMemWord(io.brCondIdx(0)).asSInt
                  //      > io.rdMemWord(io.brCondIdx(1)).asSInt
                  //    )
                  //    init(False)
                  //  )
                  //  //val myBinop = AluOpKind.Sub.binopFunc(
                  //  //  cfg=cfg,
                  //  //  left=io.rdMemWord(io.brCondIdx(0)),
                  //  //  right=io.rdMemWord(io.brCondIdx(1)),
                  //  //  carry=(
                  //  //    False
                  //  //  )
                  //  //)(
                  //  //  width=cfg.mainWidth
                  //  //)
                  //  //((!(myBinop.flagN ^ myBinop.flagV)) & !myBinop.flagZ)
                  //}
                }
              }
              case CondKind.Les => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  //nextExSetPcValid := (
                  //  ((io.rFlagN ^ io.rFlagV) | io.rFlagZ)
                  //  //init(False)
                  //)
                } else {
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr
                    && opInfo.srcArr(1) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  //nextExSetPcValid := {
                  //  (
                  //    (
                  //      io.rdMemWord(io.brCondIdx(0)).asSInt
                  //      <= io.rdMemWord(io.brCondIdx(1)).asSInt
                  //    )
                  //    //init(False)
                  //  )
                  //  //val myBinop = AluOpKind.Sub.binopFunc(
                  //  //  cfg=cfg,
                  //  //  left=io.rdMemWord(io.brCondIdx(0)),
                  //  //  right=io.rdMemWord(io.brCondIdx(1)),
                  //  //  carry=(
                  //  //    False
                  //  //  )
                  //  //)(
                  //  //  width=cfg.mainWidth
                  //  //)
                  //  //((myBinop.flagN ^ myBinop.flagV) | myBinop.flagZ)
                  //}
                }
              }
              case CondKind.Z => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                assert(
                  opInfo.srcArr(0) == SrcKind.Gpr,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                nextExSetPcValid := (
                  (io.rdMemWord(io.brCondIdx(0)) === 0)
                  //init(False)
                  //!(io.rdMemWord(io.brCondIdx(0)).orR)
                )
              }
              case CondKind.Nz => {
                if (opInfo.dstArr.size == 1) {
                  io.modMemWord.foreach(item => {
                    item := io.rdMemWord(0)
                  })
                }
                assert(
                  opInfo.srcArr(0) == SrcKind.Gpr,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                nextExSetPcValid := (
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
        /*val binop: InstrResult =*/ opInfo.aluOp.get match {
          case AluOpKind.Add => {
            val myBinop = AluOpKind.Add.binopFunc(
              cfg=cfg,
              left=selRdMemWord(1),
              right=selRdMemWord(2),
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
                nextIndexReg := 0x0
                kind match {
                  case SprKind.AluFlags => {
                    if (opInfo.dstArr.size == 1) {
                      nextAluFlags := myBinop.main
                      io.modMemWordValid.foreach(current => {
                        current := False
                      })
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
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Ira => {
                    nextIra := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Ie => {
                    nextIe := myBinop.main(0)
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Ity => {
                    nextIty := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Sty => {
                    nextSty := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Hi => {
                    nextHi := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
                    //io.modMemWord.foreach(modMemWord => {
                    //  modMemWord := modMemWord.getZero
                    //})
                  }
                  case SprKind.Lo => {
                    nextLo := myBinop.main
                    io.modMemWordValid.foreach(current => {
                      current := False
                    })
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
                nextIndexReg := myBinop.main
                io.modMemWordValid.foreach(current => {
                  current := False
                })
                //io.modMemWord.foreach(modMemWord => {
                //  modMemWord := modMemWord.getZero
                //})
              }
              case _ => {
                nextIndexReg := 0x0
              }
            }
            io.modMemWord(0) := (
              if (
                opInfo.dstArr.find(_ == DstKind.Gpr) != None
              ) {
                myBinop.main
              } else {
                selRdMemWord(0)
              }
            )
          }
          case AluOpKind.Sub => {
            val myBinop = AluOpKind.Sub.binopFunc(
              cfg=cfg,
              left=selRdMemWord(1),
              right=selRdMemWord(2),
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
                selRdMemWord(0)
              }
            )
            nextIndexReg := 0x0
          }
          case op => {
            val binop = op.binopFunc(
              cfg=cfg,
              left=selRdMemWord(1),
              right=selRdMemWord(2),
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
            nextIndexReg := 0x0
          }
        }
      }
      case OpSelect.AluShift => {
        //io.opIsAluShift := True
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
              left=selRdMemWord(1),
              right=selRdMemWord(2),
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
            nextIndexReg := 0x0
          }
        }
      }
      case OpSelect.MultiCycle => {
        nextIndexReg := 0x0
        for (
          ((_, innerOpInfo), idx)
          <- cfg.multiCycleOpInfoMap.view.zipWithIndex
        ) {
          if (opInfo == innerOpInfo) {
            io.multiCycleOpInfoIdx := idx
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
    switch (io.splitOp.nonMultiCycleNonJmpOp) {
      for (
        ((_, opInfo), idx)
        <- cfg.nonMultiCycleNonJmpOpInfoMap.view.zipWithIndex
      ) {
        //if (
        //  idx + 1 < cfg.nonMultiCycleOpInfoMap.size
        //) {
          is (idx) {
            innerFunc(
              opInfo=opInfo,
              opInfoIdx=idx,
            )
          }
        //}
      }
    }
    switch (io.splitOp.jmpBrOp) {
      for (
        ((_, opInfo), idx)
        <- cfg.jmpBrOpInfoMap.view.zipWithIndex
      ) {
        is (idx) {
          innerFunc(
            opInfo=opInfo,
            opInfoIdx=idx,
          )
        }
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
    switch (io.splitOp.exSetNextPcKind) {
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
          Mux[UInt](
            tempPsExSetPcTaken,
            io.regPcPlusImm, //+ cfg.instrSizeBytes
            RegNextWhen(
              next=RegNextWhen(
                next=io.regPc,
                cond=io.upIsFiring,
                init=io.regPc.getZero,
              ),
              cond=io.upIsFiring,
              init=io.regPc.getZero,
            ) + (1 * cfg.instrSizeBytes),
          )
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
          io.rdMemWord(io.jmpAddrIdx) - (1 * cfg.instrSizeBytes)
        )
      }
      is (SnowHousePsExSetNextPcKind.Ira) {
        io.psExSetPc.nextPc := (
          io.rIra
        )
      }
      //default {
      //  io.psExSetPc.nextPc := (
      //    io.regPcPlusImm 
      //  )
      //}
    }
  }
  switch (io.splitOp.multiCycleOp) {
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
          ),
          careAbout=(
            (1 << idx)
            | ((1 << idx) - 1)
          ),
          width=(
            cfg.multiCycleOpInfoMap.size
          )
        )
      ) {
        innerFunc(
          opInfo=opInfo,
          opInfoIdx=idx,
        )
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
  //        <- cfg.jmpBrOpInfoMap.view.zipWithIndex
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
  //    switch (io.splitOp.jmpBrOp) {
  //      for (
  //        ((_, opInfo), idx) <- cfg.jmpBrOpInfoMap.view.zipWithIndex
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
      when (io.gprIsZeroVec(0)(0)) {
        io.modMemWord(0) := 0x0
      }
    }
  }
  when (
    //!rShouldIgnoreInstrState(1)
    !io.shouldIgnoreInstr(1)
  ) {
    //if (idx == 1) {
      //io.psExSetPc.nextPc := (
      //  io.regPcPlusImm 
      //)
      //io.shouldIgnoreInstr := False
      doHandleSetNextPc()
    //}
  }
  def doShouldIgnoreState2(): Unit = {
    io.modMemWordValid.foreach(current => {
      current := False
    })
    io.modMemWord.foreach(modMemWord => {
      modMemWord := modMemWord.getZero
    })
    //io.opIs := 0x0
    io.opIsMemAccess.foreach(current => {
      current := False
    })
    io.opIsAnyMultiCycle := (
      False
    )
    io.opIsMultiCycle.foreach(current => {
      current := False
    })
  }

  for (idx <- 0 until rShouldIgnoreInstrState.size) {
    //if (idx != 1) {
      io.shouldIgnoreInstr(idx) := (
        RegNext(
          next=io.shouldIgnoreInstr(idx),
          init=io.shouldIgnoreInstr(idx).getZero,
        )
      )
    //}
    //when (!rShouldIgnoreInstrState(idx).msb) {
    //  when (io.upIsFiring) {
    //    nextShouldIgnoreInstrState(idx) := (
    //      rShouldIgnoreInstrState(idx) - 1
    //    )
    //  }
    //  io.shouldIgnoreInstr(idx) := True
    //  if (idx == 2) {
    //    doShouldIgnoreState2()
    //  }
    //} otherwise {
    //  io.shouldIgnoreInstr(idx) := False
    //  when (io.upIsFiring) {
    //    when (io.psExSetPc.fire) {
    //      nextShouldIgnoreInstrState(idx) := 2
    //    }
    //  }
    //}

    //switch (rShouldIgnoreInstrState(idx)) {
    //  is (False) {
    //    //when (
    //    //  RegNext(
    //    //    io.upIsFiring
    //    //    && io.regPcSetItCnt(idx)(0)
    //    //  )
    //    //) {
    //    //   otherwise {
    //    //    io.shouldIgnoreInstr(idx) := (
    //    //      False
    //    //    )
    //    //  }
    //    //} elsewhen (io.psExSetPc.valid) {
    //    //  io.shouldIgnoreInstr(idx) := (
    //    //    True
    //    //  )
    //    //}
    //    io.shouldIgnoreInstr(idx) := (
    //      False
    //    )
    //    when (
    //      io.psExSetPc.valid
    //      && RegNext(
    //        next=RegNext(
    //          next=io.upIsFiring,
    //          init=False,
    //        )
    //      )
    //      //&& RegNextWhen(
    //      //  next=(!io.shouldIgnoreInstr(idx)),
    //      //  cond=io.upIsFiring,
    //      //  init=False
    //      //)
    //    ) {
    //      io.shouldIgnoreInstr(idx) := (
    //        True
    //      )
    //      nextShouldIgnoreInstrState(idx) := True
    //    }
    //    //when (io.psExSetPc.ready) {
    //    //  //io.shouldIgnoreInstr(idx) := 
    //    //  nextShouldIgnoreInstrState(idx) := True
    //    //}
    //  }
    //  is (True) {
    //    //when (io.psExSetPc.ready) {
    //    //}
    //    //when (io.psExSetPc.valid) {
    //    //  io.shouldIgnoreInstr(idx) := (
    //    //    
    //    //  )
    //    //}
    //    io.shouldIgnoreInstr(idx) := (
    //      True
    //    )
    //    when (
    //      io.regPcSetItCnt(idx)(0)
    //      && io.upIsFiring
    //    ) {
    //      nextExSetPcValid := False
    //      nextShouldIgnoreInstrState(idx) := False
    //    }
    //    //when (
    //    //  //io.upIsFiring
    //    //  //io.upIsFiring
    //    //  //&& 
    //    //  io.regPcSetItCnt(idx)(0)
    //    //  //&& io.downIsFiring
    //    //) {
    //    //  io.shouldIgnoreInstr(idx) := (
    //    //    False
    //    //  )
    //    //  when (
    //    //    io.psExSetPc.valid
    //    //    //&& RegNext(!io.shouldIgnoreInstr(idx), init=False)
    //    //    //&& io.upIsFiring
    //    //  ) {
    //    //    when (RegNext(!io.shouldIgnoreInstr(idx), init=False)) {
    //    //      io.shouldIgnoreInstr(idx) := (
    //    //        True
    //    //      )
    //    //    }
    //    //  } otherwise {
    //    //    when (io.upIsFiring) {
    //    //      nextShouldIgnoreInstrState(idx) := False
    //    //    }
    //    //  }
    //    //}
    //  }
    //}

    //when (
    //  //io.upIsFiring
    //  //io.upIsFiring
    //  //&& 
    //  io.regPcSetItCnt(idx)(0)
    //  //&& io.downIsFiring
    //) {
    //  io.shouldIgnoreInstr(idx) := (
    //    False
    //  )
    //  when (
    //    io.psExSetPc.valid
    //    && RegNext(!io.shouldIgnoreInstr(idx), init=False)
    //  ) {
    //    io.shouldIgnoreInstr(idx) := (
    //      True
    //    )
    //  }
    //  when (io.upIsFiring) {
    //    nextShouldIgnoreInstrState(idx) := False
    //  }
    //}
    val myNextShouldIgnoreInstr = Bool()
    myNextShouldIgnoreInstr := True
    when (
      RegNext(
        next=(
          io.regPcSetItCnt(idx)(0)
          && io.upIsFiring
        ),
        init=False
      )
      //io.regPcSetItCnt(idx)(0)
      //io.regPcSetItCnt(idx)(0)
      ////&& RegNext(
      ////  next=io.shouldIgnoreInstr(idx),
      ////  init=False
      ////)
      ////&& io.upIsValid
      ////&& io.upIsValid
      ////&& io.downIsReady
      ////&& io.upIsFiring
      //&& io.upIsValid
      //&& io.downIsReady
    ) {
      //myNextShouldIgnoreInstr := False
      //io.shouldIgnoreInstr(idx) := RegNextWhen(
      //  next=myNextShouldIgnoreInstr,
      //  cond=io.upIsFiring,
      //  init=myNextShouldIgnoreInstr.getZero,
      //)
      io.shouldIgnoreInstr(idx) := False
    } elsewhen (
      //RegNext(io.psExSetPc.valid)
      //&& io.psExSetPc.ready
      io.psExSetPc.valid
      //tempPsExSetPcValid
      //&& RegNext(
      //  next=(!io.shouldIgnoreInstr(idx)),
      //  init=False,
      //)
      //&& io.upIsFiring
      //&& io.upIsValid
      //&& io.upIsValid
      //&& !io.regPcSetItCnt(idx)(0)
      //&& io.upIsFiring
      //&& io.upIsValid

      //&& io.downIsReady
      && RegNext(
        next=(
          !io.shouldIgnoreInstr(idx)
          && io.upIsFiring
        ),
        init=False,
      )
      //&& RegNext(
      //  next=io.upIsFiring,
      //  init=False,
      //)
    ) {
      io.shouldIgnoreInstr(idx) := True
    }
    //when (
    //  io.regPcSetItCnt(idx)(0)
    //  //&& RegNext(
    //  //  next=io.shouldIgnoreInstr(idx),
    //  //  init=False
    //  //)
    //  //&& io.upIsValid
    //  //&& io.upIsValid
    //  //&& io.downIsReady
    //  //&& io.upIsFiring
    //  && io.upIsValid
    //  && io.downIsReady
    //) {
    //  //myNextShouldIgnoreInstr := False
    //  //io.shouldIgnoreInstr(idx) := RegNextWhen(
    //  //  next=myNextShouldIgnoreInstr,
    //  //  cond=io.upIsFiring,
    //  //  init=myNextShouldIgnoreInstr.getZero,
    //  //)
    //  io.shouldIgnoreInstr(idx) := False
    //}

    //when (!rShouldIgnoreInstrState(idx)) {
    //}
    if (idx == 2) {
      when (io.shouldIgnoreInstr(idx)) {
        doShouldIgnoreState2()
      }
    }
    //when (!rShouldIgnoreInstrState(idx)) {
    //} otherwise {
    //  when (io.psExSetPc.fire) {
    //  }
    //}
    //switch (
    //  Cat(
    //    List(
    //      rShouldIgnoreInstrState(idx),
    //      //io.opIsJmp,
    //      //(
    //      //  io.upIsFiring
    //      //  && io.regPcSetItCnt(idx)(0)
    //      //)
    //      //io.upIsFiring,
    //      io.psExSetPc.fire,
    //      io.regPcSetItCnt(idx)(0)
    //    ).reverse
    //  )
    //) {
    //  is (M"00-") {
    //    //if (idx != 1) {
    //      io.shouldIgnoreInstr(idx) := False
    //    //}
    //    //if (idx == 1) {
    //    //  //io.psExSetPc.nextPc := (
    //    //  //  io.regPcPlusImm 
    //    //  //)
    //    //  //io.shouldIgnoreInstr := False
    //    //  doHandleSetNextPc()
    //    //}
    //  }
    //  is (M"01-") {
    //    //if (idx != 1) {
    //      io.shouldIgnoreInstr(idx) := False
    //    //}
    //    //if (idx == 1) {
    //    //  //io.shouldIgnoreInstr := False
    //    //  //io.psExSetPc.nextPc := (
    //    //  //  io.regPcPlusImm 
    //    //  //)
    //    //  doHandleSetNextPc()
    //    //}
    //    when (
    //      io.upIsFiring
    //    ) {
    //      nextShouldIgnoreInstrState(idx) := True
    //    }
    //  }
    //  is (M"1-0") {
    //    //if (idx != 1) {
    //      io.shouldIgnoreInstr(idx) := True
    //    //}
    //    if (idx == 0) {
    //      //io.shouldIgnoreInstr := True
    //    } else if (idx == 2) {
    //      doShouldIgnoreState2()
    //    }
    //  }
    //  is (M"1-1") {
    //    //if (idx != 1) {
    //      io.shouldIgnoreInstr(idx) := True
    //    //}
    //    if (idx == 0) {
    //      //io.shouldIgnoreInstr := True
    //    } else if (idx == 2) {
    //      doShouldIgnoreState2()
    //    }
    //    when (
    //      ////io.regPcSetItCnt.msb
    //      io.upIsFiring
    //      //&& io.regPcSetItCnt(idx)(0)
    //    ) {
    //      nextShouldIgnoreInstrState(idx) := False
    //    }
    //  }
    //  //default {
    //  //  if (idx != 1) {
    //  //    io.shouldIgnoreInstr(idx) := True
    //  //  }
    //  //}
    //}
  }
  when (io.takeIrq) {
    io.modMemWordValid.foreach(current => {
      current := False
    })
    nextIra := io.regPc
    nextIe/*(0)*/ := False //0x0
    io.psExSetPc.nextPc := (
      io.rIds
    )
  }
  //when (
  //  //io.shouldIgnoreInstr
  //  lowerMyFanoutShouldIgnoreInstr
  //) {
  //  io.opIs := 0x0
  //  io.opIsMemAccess.foreach(current => {
  //    current := False
  //  })
  //  io.opIsAnyMultiCycle := (
  //    False
  //  )
  //  io.opIsMultiCycle.foreach(current => {
  //    current := False
  //  })
  //}
//  }
  when (
    rose(myPsExSetPcCmpEq.nextValid)
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
  when (RegNext(next=myPsExSetPcCmpEq.nextValid, init=False)) {
    when (
      io.shouldIgnoreInstr(3)
    ) {
      myPsExSetPcCmpEq.nextValid := False
    }
    when (io.upIsFiring) {
      myPsExSetPcCmpEq.nextValid := False
    }
  }
  when (rose(myPsExSetPcCmpNe.nextValid)) {
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
  when (RegNext(next=myPsExSetPcCmpNe.nextValid, init=False)) {
    when (io.shouldIgnoreInstr(3)) {
      myPsExSetPcCmpNe.nextValid := False
    }
    when (io.upIsFiring) {
      myPsExSetPcCmpNe.nextValid := False
    }
  }
}

case class SnowHousePipeStageExecute(
  args: SnowHousePipeStageArgs,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
  psMemStallHost: LcvStallHost[
    BusHostPayload,
    BusDevPayload,
  ],
  doModInMid0FrontParams: PipeMemRmwDoModInMid0FrontFuncParams[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeMemRmwDualRdTypeDisabled[UInt, Bool],
  ],
  pcChangeState: Bool/*UInt*/,
  shouldIgnoreInstr: Bool,
) extends Area {
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
  val nextSetOutpState = (
    Vec.fill(3)(
      Bool()
    )
  )
  val rSetOutpState = (
    RegNext(
      next=nextSetOutpState,
      //init=nextSetOutpState.getZero,
    )
  )
  for (idx <- 0 until nextSetOutpState.size) {
    rSetOutpState(idx).init(nextSetOutpState(idx).getZero)
    nextSetOutpState(idx) := rSetOutpState(idx)
  }
  outp := (
    RegNext(
      next=outp,
      init=outp.getZero,
    )
  )
  outp.allowOverride
  //outp.splitOp.opIsMemAccess := False
  //val tempExt = (
  //  cloneOf(outp.myExt)
  //)
  //tempExt := (
  //  RegNext(
  //    next=tempExt,
  //    init=tempExt.getZero,
  //  )
  //)
  def myRdMemWord(
    ydx: Int,
    modIdx: Int,
  ) = (
    doModInMid0FrontParams.getMyRdMemWordFunc(ydx, modIdx)
  )
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
  }
  //val savedPsMemStallHost = (
  //  LcvStallHostSaved(
  //    stallHost=psMemStallHost,
  //    someLink=cMid0Front,
  //  )
  //)
  def stallKindMem = 0
  def stallKindMultiCycle = 1
  //def stallKindMultiCycle1 = 2
  def stallKindLim = (
    //3
    2
  )

  val myDoStall = (
    /*KeepAttribute*/(
      Vec.fill(stallKindLim)(
        Bool()
      )
    )
  )
  myDoStall(stallKindMem) := False
  myDoStall(stallKindMultiCycle) := (
    RegNext(
      next=myDoStall(stallKindMultiCycle),
      init=myDoStall(stallKindMultiCycle).getZero,
    )
  )
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
  //for (idx <- 0 until myNextPrevTxnWasHazardVec.size) {
  //  myNextPrevTxnWasHazardVec(idx) :=
  //}

  val myDoHaveHazardAddrCheckVec = Vec[Bool](
    {
      assert(
        outp.myExt.size == cfg.regFileCfg.memArrSize
      )
      val temp = ArrayBuffer[Bool]()
      // TODO: support multiple register writes per instruction
      //val tempArr = ArrayBuffer[Bool]()
      //for (idx <- 0 until outp.gprIdxVec.size) {
      //  tempArr += (
      //    //(
      //    //  //outp.gprIdxVec(idx)
      //    //  outp.myExt(0).memAddr(idx)
      //    //  === (
      //    //    //tempModFrontPayload.gprIdxVec(0)
      //    //    // TODO: *maybe* support multiple output registers!
      //    //    tempModFrontPayload.myExt(0).memAddr(0)
      //    //  )
      //    //) ||
      //    (
      //      //True
      //      //outp.gprIdxVec(idx)
      //      outp.myExt(0).memAddr(idx)
      //      === RegNextWhen(
      //        next=(
      //          //outp.gprIdxVec(0)
      //          outp.myExt(0).memAddr(0)
      //        ),
      //        cond=cMid0Front.up.isFiring,
      //        init=(
      //          //outp.gprIdxVec(0).getZero
      //          outp.myExt(0).memAddr(0).getZero
      //        ),
      //      )
      //    )
      //  )
      //}
      // TODO: support multiple register writes per instruction
      temp += (
        outp.myDoHaveHazardAddrCheckVec(0)
      )

      temp
    },
    Bool()
  )
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
      assert(
        myDoHaveHazardAddrCheckVec.size
        == myDoHaveHazardValidCheckVec.size,
        s"${myDoHaveHazardAddrCheckVec.size} "
        + s"${myDoHaveHazardValidCheckVec.size}"
      )
      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        tempArr += (
          myDoHaveHazardAddrCheckVec(ydx)
          && myDoHaveHazardValidCheckVec(ydx)
        )
      }
      tempArr
    }
  )
  val myDoHaveHazard1 = (
    myDoHaveHazardVec.reduceLeft(_ || _)
  )
  val myDoHaveHazard = /*KeepAttribute*/(
    Vec.fill(
      //cfg.multiCycleOpInfoMap.size + 1
      1
    )(
      myDoHaveHazardVec.reduceLeft(_ || _)
    )
  )
  setOutpModMemWord.io.takeIrq := (
    RegNext(
      next=setOutpModMemWord.io.takeIrq,
      init=setOutpModMemWord.io.takeIrq.getZero,
    )
  )
  //object IrqHndlState extends SpinalEnum(defaultEncoding=binaryOneHot) {
  //  val
  //    Idle,
  //    HndlIrq,
  //    Last
  //    = newElement()
  //}
  //val rSavedTakeIrq = (
  //  Reg(Bool())
  //  init(False)
  //)
  //setOutpModMemWord.io.takeIrq.setAsReg() init(False)
  val rIrqHndlState = {
    val temp = Reg(
      Bool()
    )
    temp.init(temp.getZero)
    temp
  }
  val tempTakeIrq = (
    cfg.irqCfg != None
  ) generate (
    //LcvFastAndR(
    //  Vec[Bool](
    //    (
    //      cMid0Front.up.isValid
    //    ), 
    //    (
    //      outp.takeIrq
    //    ),
    //    (
    //      RegNextWhen(
    //        next=(setOutpModMemWord.nextIe/*(0)*/ === True),//0x0
    //        cond=cMid0Front.up.isFiring,
    //        init=False,
    //      )
    //      //setOutpModMemWord.nextIe/*(0)*/ === True
    //    ),
    //    (
    //      !setOutpModMemWord.io.shouldIgnoreInstr
    //    ),
    //    (
    //      !rIrqHndlState//.valid
    //    ),
    //    (
    //      if (setOutpModMemWord.io.haveRetIraState) (
    //        !setOutpModMemWord.io.rHadRetIra
    //      ) else (
    //        True
    //      )
    //    )
    //  ).asBits.asUInt
    //)
    False
  )
  if (cfg.irqCfg != None) {
    when (RegNext(io.idsIraIrq.nextValid)) {
      setOutpModMemWord.io.takeIrq := /*RegNextWhen*/(
        tempTakeIrq
        //next=tempTakeIrq,
        //cond=cMid0Front.up.isFiring,
        //init=False,
      )
    }
  }
  val nextTempIrqCond = (
    cfg.irqCfg != None
  ) generate (
    cMid0Front.up.isFiring
    && 
    setOutpModMemWord.io.takeIrq
    //RegNextWhen(
    //  next=tempTakeIrq,
    //  cond=cMid0Front.up.isFiring,
    //  init=False,
    //)
  )
  if (cfg.irqCfg != None) {
    when (nextTempIrqCond) {
      rIrqHndlState/*.valid*/ := True
    }
    when (rIrqHndlState) {
      setOutpModMemWord.io.takeIrq := (
        False
      )
    }
    io.idsIraIrq.ready := False
    when (RegNext(io.idsIraIrq.nextValid)) {
      when (setOutpModMemWord.io.takeIrq) {
        when (cMid0Front.up.isFiring) {
          io.idsIraIrq.ready := True
        }
      }
    }
  }
  val reEnableIrqsCond = (
    cfg.irqCfg != None
  ) generate {
    /*LcvFastAndR*/(
      Vec[Bool](
        cMid0Front.up.isFiring,
        rIrqHndlState,//.fire
        (
          if (setOutpModMemWord.io.haveRetIraState) (
            setOutpModMemWord.io.rHadRetIra
          ) else (
            True
          )
        ),
        (
          RegNextWhen(
            next=(
              setOutpModMemWord.io.rIe/*(0)*/ === False
              //setOutpModMemWord.nextIe/*(0)*/ === False
            ),//0x0
            cond=cMid0Front.up.isFiring,
            init=False,
          )
        ),
      ).asBits.asUInt.andR
    )
  }
  //if (cfg.irqCfg != None) {
  //  when (reEnableIrqsCond) {
  //    setOutpModMemWord.nextIe/*(0)*/ := True//0x1
  //    rIrqHndlState/*.valid*/ := False
  //    if (setOutpModMemWord.io.haveRetIraState) {
  //    }
  //  }
  //}
  setOutpModMemWord.io.splitOp.kind.allowOverride
  setOutpModMemWord.io.splitOp.jmpBrOp.allowOverride
  setOutpModMemWord.io.splitOp := (
    RegNext(
      next=setOutpModMemWord.io.splitOp,
      init=setOutpModMemWord.io.splitOp.getZero,
    )
    //init(SnowHouseSplitOpKind.CPY_CPYUI)
  )
  when (cMid0Front.up.isValid) {
    setOutpModMemWord.io.splitOp := outp.splitOp
    //when (!setOutpModMemWord.io.takeIrq) {
    //  setOutpModMemWord.io.splitOp := outp.splitOp
    //} otherwise {
    //  setOutpModMemWord.io.splitOp := setOutpModMemWord.io.splitOp.getZero
    //  setOutpModMemWord.io.splitOp.kind := (
    //    SnowHouseSplitOpKind.JMP_BR
    //  )
    //  setOutpModMemWord.io.splitOp.jmpBrOp := {
    //    val temp = UInt(log2Up(cfg.jmpBrOpInfoMap.size) bits)
    //    for (
    //      ((idx, pureJmpOpInfo), jmpOp)
    //      <- cfg.jmpBrOpInfoMap.view.zipWithIndex
    //    ) {
    //      if (idx == cfg.irqJmpOp) {
    //        temp := jmpOp
    //      }
    //    }
    //    temp
    //  }
    //}
  } otherwise {
    setOutpModMemWord.io.splitOp.jmpBrOp := (
      (1 << setOutpModMemWord.io.splitOp.jmpBrOp.getWidth) - 1
    )
  }

  setOutpModMemWord.io.regPcSetItCnt := outp.regPcSetItCnt
  setOutpModMemWord.io.regPc := outp.regPc
  setOutpModMemWord.io.regPcPlusInstrSize := outp.regPcPlusInstrSize
  setOutpModMemWord.io.regPcPlusImm := outp.regPcPlusImm
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

  def doFinishSetOutpModMemWord(
    ydx: Int,
    zdx: Int,
  ): Unit = {
    def tempExt = outp.myExt(ydx)
    if (zdx == PipeMemRmw.modWrIdx) {
      tempExt.modMemWord := (
        // TODO: support multiple output `modMemWord`s
        setOutpModMemWord.io.modMemWord(0)
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
    }
    def tempRdMemWord = setOutpModMemWord.io.rdMemWord(zdx)
    val rRdMemWordState = (
      Reg(Bool(), init=False)
      .setName(
        s"${cfg.shRegFileCfg.pipeName}_rRdMemWordState_${ydx}_${zdx}"
      )
    )

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
      tempRdMemWord := myRdMemWord(ydx=ydx, modIdx=zdx)
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
  if (cfg.regFileWordCountArr.size == 0) {
    assert(
      false,
      s"cfg.regFileWordCountArr.size(${cfg.regFileWordCountArr.size}) "
      + s"must be greater than 0"
    )
  } else if (cfg.regFileWordCountArr.size == 1) {
    for (
      (tempRdMemWord, zdx) <- setOutpModMemWord.io.rdMemWord.zipWithIndex
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
      (tempRdMemWord, zdx) <- setOutpModMemWord.io.rdMemWord.zipWithIndex
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
  //val nextSavedStall = (
  //  Vec.fill(
  //    //cfg.multiCycleOpInfoMap.size
  //    1
  //  )(
  //    Bool()
  //  )
  //)
  //val rSavedStall = (
  //  /*KeepAttribute*/(
  //    RegNext(
  //      next=nextSavedStall,
  //      //init=nextSavedStall.getZero
  //    )
  //  )
  //)
  //for (idx <- 0 until nextSavedStall.size) {
  //  rSavedStall(idx).init(nextSavedStall(idx).getZero)
  //  nextSavedStall(idx) := rSavedStall(idx)
  //}
  when (doCheckHazard.head) {
    when (myDoHaveHazard.head) {
      myDoStall(stallKindMem) := True
    }
    if (cfg.optFormal) {
      cover(
        (
          myDoHaveHazardAddrCheckVec.reduceLeft(_ || _)
        ) && (
          cMid0Front.up.isFiring
        )
      )
    }
  }
  when (
    RegNext(psMemStallHost.nextValid)
    && psMemStallHost.ready
  ) {
    psMemStallHost.nextValid := False
  }
  when (cMid0Front.up.isFiring) {
    nextPrevTxnWasHazard := False
    //for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
    //  //val tempYdx = (
    //  //  if (ydx < cfg.regFileCfg.modMemWordValidSize) (
    //  //    ydx
    //  //  ) else (
    //  //    cfg.regFileCfg.modMemWordValidSize - 1
    //  //  )
    //  //)
    //  for (kdx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
    //    outp.myExt(ydx).valid(kdx) := (
    //      //outp.myExt(ydx).modMemWordValid.last
    //      //outp.myExt(ydx).modMemWordValid(tempYdx)
    //      outp.myExt(ydx).modMemWordValid(kdx)
    //    )
    //  }
    //}
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
  //val condForAssertSetPcValid = (
  //  setOutpModMemWord.io.opIsJmp
  //)
  //outp.instrCnt.shouldIgnoreInstr.foreach(current => {
  //  current := (
  //    setOutpModMemWord.io.shouldIgnoreInstr(2)
  //  )
  //})
  shouldIgnoreInstr := setOutpModMemWord.io.shouldIgnoreInstr.last
  pcChangeState.assignFromBits(
    setOutpModMemWord.io.pcChangeState.asBits
  )
  //when (setOutpModMemWord.io.psExSetPc.valid) {
  //}
  psExSetPc.valid := (
    //RegNext(next=setOutpModMemWord.io.psExSetPc.valid, init=False)
    setOutpModMemWord.io.psExSetPc.valid
    && RegNext(
      next=(
        !setOutpModMemWord.io.shouldIgnoreInstr(0)
        && cMid0Front.up.isFiring
      ),
      init=False,
    )
    //&& RegNext(
    //  next=cMid0Front.up.isFiring,
    //  init=False,
    //)


    //&& RegNext(!setOutpModMemWord.io.shouldIgnoreInstr(0), init=False)
    //&& RegNext(
    //  next=cMid0Front.up.isFiring,
    //  init=False,
    //)

    //&& !setOutpModMemWord.io.shouldIgnoreInstr(0)
    //&& RegNext(
    //  next=(
    //    //!setOutpModMemWord.io.shouldIgnoreInstr(0)
    //    //&&
    //    //(
    //    //  //cMid0Front.up.isValid
    //      cMid0Front.up.isFiring
    //    //  //cMid0Front.down.isFiring
    //    //  //cMid0Front.down.isReady
    //    //)
    //  ),
    //  init=False
    //)
    //&& cMid0Front.down.isReady
  )
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
    outp.branchTgtBufElem(0).dontPredict
  )
  setOutpModMemWord.io.branchPredictTkn := (
    outp.branchPredictTkn
    //outp.branchTgtBufElem(1).branchKind.asBits(0)
  )
  psExSetPc.nextPc := setOutpModMemWord.io.psExSetPc.nextPc
  //psExSetPc.encInstr := outp.encInstr
  psExSetPc.branchTgtBufElem := outp.branchTgtBufElem(1)
  io.dbus.allowOverride
  io.dbus.sendData := (
    RegNext(
      next=io.dbus.sendData,
      init=io.dbus.sendData.getZero,
    )
  )
  object MultiCycleOpState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      Idle,
      Main,
      NoMoreStall
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
        RegNext(
          setOutpModMemWord.io.selRdMemWord(
            opInfo=opInfo,
            idx=1,
          ),
        )
        init(0x0)
      )
      multiCycleBus.sendData.srcVec(1) := (
        RegNext(
          setOutpModMemWord.io.selRdMemWord(
            opInfo=opInfo,
            idx=2,
          ),
        )
        init(0x0)
      )
    }
  }
  //io.dcacheHaveHazard := (
  //  !rSavedStall
  //  && doCheckHazard && myDoHaveHazard1
  //)
  when (/*LcvFastOrR*/(
    setOutpModMemWord.io.opIsMemAccess.head
    //.asBits.asUInt
    //.orR
  )) {
    nextPrevTxnWasHazard := True
    when (cMid0Front.up.isFiring) {
      psMemStallHost.nextValid := True
      //io.dbus.sendData := setOutpModMemWord.io.dbusHostPayload
    }
  }
  when (
    cMid0Front.up.isFiring
    //&&
    //outp.splitOp.opIsMemAccess
    //cMid0Front.down.isFiring
    //cMid0Front.down.isReady
  ) {
    io.dbus.sendData := setOutpModMemWord.io.dbusHostPayload
  }
  //when (cMid0Front.up.isFiring)
  //when (
  //  cMid0Front.up.isValid && !setOutpModMemWord.io.shouldIgnoreInstr(1)
  //) {
  //  io.dbus.sendData := setOutpModMemWord.io.dbusHostPayload
  //}
  def doMultiCycleStart(
    myPsExStallHost: LcvStallHost[
      MultiCycleHostPayload,
      MultiCycleDevPayload
    ],
    idx: Int,
  ): Unit = {
    //myDoStall(stallKindMem) := False
    myDoStall(stallKindMultiCycle) := True
    myPsExStallHost.nextValid := True
    //nextSavedStall(idx) := True
    //nextSavedStall.head := True
  }
  switch (rMultiCycleOpState) {
    is (
      //False
      MultiCycleOpState.Idle
    ) {
      when (
        //LcvFastOrR(
        //  setOutpModMemWord.io.opIsMultiCycle.asBits.asUInt
        //  //=/= 0x0
        //  //.orR
        //)
        cMid0Front.up.isValid
        && setOutpModMemWord.io.opIsAnyMultiCycle
        //&& !setOutpModMemWord.rShouldIgnoreInstrState(2)
        && !setOutpModMemWord.io.shouldIgnoreInstr(2)
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
        //myDoStall(stallKindMultiCycle1) := True
        //cMid0Front.duplicateIt()
        cMid0Front.haltIt()
        when (
          /*RegNext*/(
            (
              Vec[Bool](
                //!rSavedStall.head/*(idx)*/,
                /*RegNext*/(doCheckHazard).head/*(idx)*/,
                /*RegNext*/(myDoHaveHazard).head/*(idx)*/,
                RegNext(psMemStallHost.nextValid, init=False),
                psMemStallHost.ready,
              ).asBits.asUInt.andR
            ) || (
              !Vec[Bool](
                //!rSavedStall.head/*(idx)*/,
                /*RegNext*/(doCheckHazard).head/*(idx)*/,
                /*RegNext*/(myDoHaveHazard).head/*(idx)*/,
              ).asBits.asUInt.andR
            )
          )
          //init(False)
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
    }
    is (
      //True
      MultiCycleOpState.Main
    ) {
      myDoStall(stallKindMem) := False
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
                  //psExStallHost.nextValid := True
                  //when (
                  //  /*LcvFastAndR*/(
                  //    Vec[Bool](
                  //      !rSavedStall.head/*(idx)*/,
                  //      RegNext(next=doCheckHazard).head/*(idx)*/,
                  //      RegNext(next=myDoHaveHazard).head/*(idx)*/,
                  //    ).asBits.asUInt.andR
                  //  )
                  //) {
                  //  psExStallHost.nextValid := False
                  //  //when (
                  //  //  //psMemStallHost.fire
                  //  //  RegNext(psMemStallHost.nextValid, init=False)
                  //  //  && psMemStallHost.ready
                  //  //) {
                  //  //  doMultiCycleStart(psExStallHost)
                  //  //}
                  //} otherwise {
                  //  //doMultiCycleStart(psExStallHost)
                  //}
                  //when (
                  //  (
                  //    Vec[Bool](
                  //      !rSavedStall.head/*(idx)*/,
                  //      RegNext(next=doCheckHazard).head/*(idx)*/,
                  //      RegNext(next=myDoHaveHazard).head/*(idx)*/,
                  //      RegNext(psMemStallHost.nextValid, init=False),
                  //      psMemStallHost.ready,
                  //    ).asBits.asUInt.andR
                  //  ) || (
                  //    !Vec[Bool](
                  //      !rSavedStall.head/*(idx)*/,
                  //      RegNext(next=doCheckHazard).head/*(idx)*/,
                  //      RegNext(next=myDoHaveHazard).head/*(idx)*/,
                  //    ).asBits.asUInt.andR
                  //  )
                  //) {
                  //  //doMultiCycleStart(psExStallHost, idx=idx)
                  //}
                  doMultiCycleStart(psExStallHost, idx=idx)
                  when (
                    RegNext(psExStallHost.nextValid, init=False)
                    && psExStallHost.ready
                  ) {
                    psExStallHost.nextValid := False
                    myDoStall(stallKindMultiCycle) := False
                    when (
                      cMid0Front.up.isFiring
                      //cMid0Front.down.isReady
                    ) {
                      rMultiCycleOpState := (
                        //False
                        MultiCycleOpState.Idle
                        //MultiCycleOpState.NoMoreStall
                      )
                    } otherwise {
                      rMultiCycleOpState := (
                        MultiCycleOpState.NoMoreStall
                      )
                    }
                  }
                  //when (rSavedStall.head/*(idx)*/) {
                  //  myDoStall(stallKindMem) := False
                  //}
                  //when (cMid0Front.up.isFiring) {
                  //  nextSavedStall.head/*(idx)*/ := False
                  //}
                }
              }
            }
          }
          // END: working, slower than desired multi-cycle op handling code
          //--------
        }
      //}
    }
    is (MultiCycleOpState.NoMoreStall) {
      //myDoStall(stallKindMultiCycle) := False
      when (
        cMid0Front.up.isFiring
        //cMid0Front.down.isReady
      ) {
        rMultiCycleOpState := MultiCycleOpState.Idle
      }
    }
  }
  //when (rSavedStall.head/*(idx)*/) {
  //  myDoStall(stallKindMem) := False
  //}
  //when (cMid0Front.up.isFiring) {
  //  nextSavedStall.head/*(idx)*/ := False
  //}
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
  //doCheckHazard.foreach(current => {
  //  current := //rPrevTxnWasHazard
  //})
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
    //cfg.haveZeroReg match {
    //  case Some(myZeroRegIdx) => {
    //    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
    //      outp.myExt(ydx).memAddrAlt.foreach(current => {
    //        current := myZeroRegIdx
    //      })
    //    }
    //  }
    //  case None => {
    //  }
    //}
    //when (myDoStall.sFindFirst(_ === True)._1) {
      //cMid0Front.duplicateIt()
      cMid0Front.haltIt()
    //}
  } otherwise {
    //outp.myExt(ydx).memAddrFwdCmp.forea
    //outp.myExt.foreach(_.memAddrFwdCmp.foreach(_.foreach(_ := 0x0)))
  }
  //cfg.haveZeroReg match {
  //  case Some(myZeroRegIdx) => {
  //    when (setOutpModMemWord.io.shouldIgnoreInstr(2)) {
  //      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //        //when (setOutpModMemWord.io.shouldIgnoreInstr(2)) {
  //          outp.myExt(ydx).memAddr.foreach(current => {
  //            current := myZeroRegIdx
  //          })
  //        //}
  //        //when (setOutpModMemWord.io.shouldIgnoreInstr(2)) {
  //          outp.myExt(ydx).memAddrAlt.foreach(current => {
  //            current := myZeroRegIdx
  //          })
  //        //}
  //        //when (setOutpModMemWord.io.shouldIgnoreInstr(2)) {
  //          outp.myExt(ydx).memAddrFwd.foreach(current => {
  //            current.foreach(innerCurrent => {
  //              innerCurrent := myZeroRegIdx
  //            })
  //          })
  //        //}
  //      }
  //    }
  //  }
  //  case None => {
  //  }
  //}
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
        item := !setOutpModMemWord.io.shouldIgnoreInstr.last
      })
    })
  }
}
case class SnowHousePipeStageMem(
  args: SnowHousePipeStageArgs,
  //psWb: SnowHousePipeStageWriteBack,
  psMemStallHost: LcvStallHost[
    BusHostPayload,
    BusDevPayload,
  ],
) extends Area {
  def cfg = args.cfg
  def io = args.io
  def regFile = args.regFile
  def front = regFile.io.front
  def frontPayload = regFile.io.frontPayload
  def modFront = regFile.io.modFront
  def modFrontAfterPayload = regFile.io.modFrontAfterPayload
  def modBack = regFile.io.modBack
  def modBackPayload = regFile.io.modBackPayload
  def back = regFile.io.back
  def backPayload = regFile.io.backPayload
  def tempModFrontPayload = (
    regFile.io.tempModFrontPayload
  )
  //val modFrontFormalAssumes = modFront(modFrontPayload).formalAssumes()
  val modBackFormalAssumes = modBack(modBackPayload).formalAssumes()
  def extIdxUp = PipeMemRmw.extIdxUp
  def extIdxSaved = PipeMemRmw.extIdxSaved
  def extIdxLim = PipeMemRmw.extIdxLim
  def doMidMod = true
  val midModPayload = (
    Vec.fill(extIdxLim)(
      SnowHousePipePayload(cfg=cfg)
    )
  )
  //val myShouldIgnoreInstr = (
  //  modFront(modFrontPayload).instrCnt.shouldIgnoreInstr
  //)
  val midModFormalAssumesArr = ArrayBuffer[Area]()
  for ((midModElem, midModIdx) <- midModPayload.view.zipWithIndex) {
    midModFormalAssumesArr += midModElem.formalAssumes()
  }
  val cMidModFront = (doMidMod) generate (
    CtrlLink(
      up=modFront,
      down={
        val temp = Node()
        temp.setName(s"cMidModFront_down")
        temp
      },
    )
  )
  val fMidModFront = (doMidMod) generate (
    ForkLink(
      up=cMidModFront.down,
      downs={
        Array.fill(2)(Node())
      },
      synchronous=(
        false
        //true
      )
    )
  )
  val sMidModFrontFwd = (doMidMod) generate (
    StageLink(
      up=fMidModFront.downs(0),
      down={
        regFile.io.modBackFwd
      }
    )
  )
  val sMidModFront = (doMidMod) generate (
    StageLink(
      up=(
        fMidModFront.downs(1)
      ),
      down={
        modBack
      },
    )
  )
  regFile.myLinkArr += cMidModFront
  regFile.myLinkArr += fMidModFront
  regFile.myLinkArr += sMidModFrontFwd
  regFile.myLinkArr += sMidModFront
  //val formalFwdMidModArea = (regFile.myHaveFormalFwd) generate (
  //  new Area {
  //    val myFwd = (
  //      /*KeepAttribute*/(
  //        Vec.fill(extIdxLim)(
  //          regFile.mkFwd()
  //        )
  //      )
  //      .setName(
  //        s"formalFwdMidModArea_"
  //        + s"myFwd"
  //      )
  //    )
  //    for (extIdx <- 0 until extIdxLim) {
  //      myFwd(extIdx) := midModPayload(extIdx).myFwd
  //    }
  //    val doFormalFwdUp =  (
  //      PipeMemRmwDoFwdArea(
  //        fwdAreaName=s"formalFwdMidModArea_doFormalFwdUp",
  //        fwd=(
  //          myFwd(extIdxUp)
  //        ),
  //        setToMyFwdDataFunc=(
  //          ydx: Int,
  //          zdx: Int,
  //          myFwdData: UInt,
  //        ) => {
  //          when (pastValidAfterReset) {
  //            assert(
  //              midModPayload(extIdxUp).myExt(ydx).rdMemWord(zdx)
  //              === myFwdData
  //            )
  //          }
  //        }
  //      )
  //    )
  //    val doFormalFwdSaved =  (
  //      PipeMemRmwDoFwdArea(
  //        fwdAreaName=s"formalFwdMidModArea_doFormalFwdSaved",
  //        fwd=(
  //          myFwd(extIdxSaved)
  //        ),
  //        setToMyFwdDataFunc=(
  //          ydx: Int,
  //          zdx: Int,
  //          myFwdData: UInt,
  //        ) => {
  //          when (pastValidAfterReset) {
  //            assert(
  //              midModPayload(extIdxSaved).myExt(ydx).rdMemWord(zdx)
  //              === myFwdData
  //            )
  //          }
  //        }
  //      )
  //    )
  //  }
  //)
  val nextSetMidModPayloadState = (
    /*KeepAttribute*/(
      Bool()
    )
    .setName(s"nextSetMidModPayloadState")
  )
  val rSetMidModPayloadState = (
    /*KeepAttribute*/(
      RegNext(
        next=nextSetMidModPayloadState,
        init=nextSetMidModPayloadState.getZero,
      )
    )
    .setName(s"rSetMidModPayloadState")
  )
  nextSetMidModPayloadState := rSetMidModPayloadState

  midModPayload(extIdxSaved) := (
    RegNextWhen(
      next=midModPayload(extIdxUp),
      cond=cMidModFront.up.isFiring,
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
    }
  }
  for (fjIdx <- 0 until tempModFrontPayload.size) {
    tempModFrontPayload(fjIdx) := midModPayload(extIdxUp)
    for (idx <- 0 until tempModFrontPayload(fjIdx).gprIdxVec.size) {
      tempModFrontPayload(fjIdx).gprIdxVec(idx).allowOverride
      tempModFrontPayload(fjIdx).gprIdxVec(idx) := (
        //modFront(modFrontPayload(fjIdx)).gprIdxVec(idx)
        modFront(modFrontAfterPayload).gprIdxVec(idx)
      )
    }
  }
  //val savedPsMemStallHost = (
  //  LcvStallHostSaved(
  //    stallHost=psMemStallHost,
  //    someLink=cMidModFront,
  //  )
  //  .setName(s"psMem_savedPsMemStallHost")
  //)
  if (cfg.optFormal) {
  }
  when (cMidModFront.up.isValid) {
    //when (!rSetMidModPayloadState) {
      midModPayload(extIdxUp) := modFront(modFrontAfterPayload)
      //nextSetMidModPayloadState := True
    //}
  }
  //when (
  //  //cMidModFront.up.isReady
  //  cMidModFront.up.isFiring
  //) {
  //  nextSetMidModPayloadState := False
  //  midModPayload(extIdxUp).myExt(0).modMemWordValid := (
  //    //_ := True
  //    modFront(modFrontAfterPayload).myExt(0).modMemWordValid
  //  )
  //  midModPayload(extIdxUp).myExt(0).memAddrAlt := (
  //    modFront(modFrontAfterPayload).myExt(0).memAddrAlt
  //  )
  //  midModPayload(extIdxUp).myExt(0).memAddr := (
  //    modFront(modFrontAfterPayload).myExt(0).memAddr
  //  )
  //  midModPayload(extIdxUp).myExt(0).memAddrFwd := (
  //    modFront(modFrontAfterPayload).myExt(0).memAddrFwd
  //  )
  //  midModPayload(extIdxUp).myExt(0).memAddrFwdMmw := (
  //    modFront(modFrontAfterPayload).myExt(0).memAddrFwdMmw
  //  )
  //  //midModPayload(extIdxUp).myExt(0).modMemWord := 0x0
  //  when (RegNext(io.dbus.sendData.accKind.asBits(0))) {
  //    midModPayload(extIdxUp).myExt(0).modMemWord := (
  //      modFront(modFrontAfterPayload).myExt(0).modMemWord
  //    )
  //  }
  //}
  for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
    def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
    def tempExtRight(ydx: Int) = modFront(modFrontAfterPayload).myExt(ydx)
    val myExtLeft = tempExtLeft(ydx=ydx)
    val myExtRight = tempExtRight(ydx=ydx)
    myExtLeft.allowOverride
    //myExtLeft.modMemWord := myExtRight.modMemWord
    myExtLeft.valid.foreach(current => {
      current := (
        cMidModFront.up.isValid
      )
    })
    myExtLeft.ready := (
      cMidModFront.up.isReady
    )
    myExtLeft.fire := (
      cMidModFront.up.isFiring
    )
  }

  def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
  def tempExtRight(ydx: Int) = modFront(modFrontAfterPayload).myExt(ydx)
  val rDbusState = (
    Reg(Bool(), init=False)
  )
  when (
    RegNext(io.dbus.nextValid) init(False)
    //True
    //midModPayload(extIdxUp).decodeExt.opIsMemAccess.sFindFirst(
    //  _ === True
    //)._1
  ) {
    def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
    def tempExtRight(ydx: Int) = modFront(modFrontAfterPayload).myExt(ydx)
    when (
      //!io.dbus.ready
      !io.dbusExtraReady(3)
    ) {
      //cMidModFront.duplicateIt()
      cMidModFront.haltIt()
      //midModPayload(extIdxUp).myExt(0).modMemWordValid.foreach(
      //  current => {
      //    current := False
      //  }
      //)
      //midModPayload(extIdxUp).myExt(0).memAddrAlt.foreach(
      //  current => {
      //    current := 0x0
      //  }
      //)
      //midModPayload(extIdxUp).myExt(0).memAddr.foreach(
      //  current => {
      //    current := 0x0
      //  }
      //)
      //midModPayload(extIdxUp).myExt(0).memAddrFwd.foreach(
      //  current => {
      //    current.foreach(current => {
      //      current := 0x0
      //    })
      //  }
      //)
      //midModPayload(extIdxUp).myExt(0).memAddrFwdMmw.foreach(
      //  current => {
      //    current.foreach(current => {
      //      current := 0x0
      //    })
      //  }
      //)
      ////when (io.dbus.sendData.accKind.asBits(0)) {
      //  midModPayload(extIdxUp).myExt(0).modMemWord := 0x0
      ////}
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
    } otherwise {
      //val myDecodeExt = midModPayload(extIdxUp).decodeExt
      //val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
      //val myCurrExt = (
      //  if (!mapElem.haveHowToSetIdx) (
      //    midModPayload(extIdxUp).myExt(
      //      0
      //    )
      //  ) else (
      //    midModPayload(extIdxUp).myExt(
      //      mapElem.howToSetIdx
      //    )
      //  )
      //)
      ////when (midModPayload(extIdxUp).gprIsZeroVec(0)) {
      ////} otherwise {
      ////}
      ////myCurrExt.modMemWordValid.foreach(current => {
      ////  current := (
      ////    // TODO: support more destination GPRs
      ////    //!midModPayload(extIdxUp).gprIsZeroVec(0)
      ////    True
      ////  )
      ////})
      //if (cfg.optFormal) {
      //  assume(
      //    myDecodeExt.memAccessKind.asBits.asUInt
      //    <= SnowHouseMemAccessKind.Store.asBits.asUInt
      //  )
      //}
    }
    //val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
    //val myCurrExt = (
    //  if (!mapElem.haveHowToSetIdx) {
    //    midModPayload(extIdxUp).myExt(
    //      0
    //    )
    //  } else {
    //    //assert(false)
    //    midModPayload(extIdxUp).myExt(
    //      mapElem.howToSetIdx
    //    )
    //  }
    //)
    //val myDecodeExt = midModPayload(extIdxUp).decodeExt
  }
  //cfg.haveZeroReg match {
  //  case Some(myZeroRegIdx) => {
  //    val myDecodeExt = midModPayload(extIdxUp).decodeExt
  //    val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
  //    val myCurrExt = (
  //      if (!mapElem.haveHowToSetIdx) (
  //        midModPayload(extIdxUp).myExt(
  //          0
  //        )
  //      ) else (
  //        midModPayload(extIdxUp).myExt(
  //          mapElem.howToSetIdx
  //        )
  //      )
  //    )
  //    when (
  //      io.dbusExtraReady(1)
  //      || cMidModFront.up.isFiring
  //    ) {
  //      for (zdx <- 0 until cfg.regFileCfg.modRdPortCnt) {
  //        //for (idx <- 0 until cfg.regFileCfg.numMyUpExtDel2) 
  //        //val idx = cfg.regFileCfg.numMyUpExDel2 - 1
  //        myCurrExt.memAddrAlt(zdx) := (
  //          myCurrExt.memAddr(PipeMemRmw.modWrIdx)
  //        )
  //      }
  //    } otherwise {
  //      for (zdx <- 0 until cfg.regFileCfg.modRdPortCnt) {
  //        //for (idx <- 0 until cfg.regFileCfg.numMyUpExtDel2) {
  //        myCurrExt.memAddrAlt(zdx) := (
  //          //myCurrExt.memAddr(PipeMemRmw.modWrIdx)
  //          //0x0
  //          myZeroRegIdx
  //        )
  //        myCurrExt.modMemWord := (
  //          0x0
  //        )
  //        //}
  //      }
  //    }
  //  }
  //  case None => {
  //  }
  //}
  //val rMemAccessNonWordSizeState = (
  //  Reg(Bool(), init=False)
  //)
  //val rTempModMemWord = (
  //  Reg(UInt(cfg.mainWidth bits))
  //  init(0x0)
  //)
  when (
    //RegNext(io.dbus.nextValid)
    //io.dbus.ready
    io.dbusExtraReady(0)
    //&& !rMemAccessNonWordSizeState
  ) {
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
    myCurrExt.modMemWord := io.dbus.recvData.data.resized
    //switch (rMemAccessNonWordSizeState) {
    //}
    //when (!myDecodeExt.memAccessKind.asBits(1)) {
      //myCurrExt.modMemWord := (
      //  io.dbus.recvData.data.resized
      //)
    //switch (
    //  myDecodeExt.memAccessSubKind//.asBits
    //) {
    //  is (SnowHouseMemAccessSubKind.Sz8) {
    //    if (cfg.mainWidth > 8) {
    //      when (!/*RegNext*/myDecodeExt.memAccessKind.asBits(0)) {
    //        rTempModMemWord := (
    //          io.dbus.recvData.data(
    //            //7 downto 0
    //            offset=RegNext(io.dbus.sendData.addr(
    //              (log2Up(cfg.mainWidth / 8)) - 1 downto 0
    //            )) * 8,
    //            8 bits
    //          ).resize(cfg.mainWidth)
    //        )
    //      } otherwise {
    //        rTempModMemWord := (
    //          io.dbus.recvData.data(
    //            //7 downto 0
    //            offset=RegNext(io.dbus.sendData.addr)(
    //              (log2Up(cfg.mainWidth / 8)) - 1 downto 0
    //            ) * 8,
    //            8 bits
    //          ).asSInt.resize(cfg.mainWidth).asUInt
    //        )
    //      }
    //      rMemAccessNonWordSizeState := True
    //      cMidModFront.haltIt()
    //    } else {
    //      myCurrExt.modMemWord := io.dbus.recvData.data.resized
    //      myCurrExt.modMemWordValid.foreach(current => {
    //        current := (
    //          // TODO: support more destination GPRs
    //          //!midModPayload(extIdxUp).gprIsZeroVec(0)
    //          True
    //        )
    //      })
    //    }
    //  }
    //  is (SnowHouseMemAccessSubKind.Sz16) {
    //    if (cfg.mainWidth > 16) {
    //      when (!/*RegNext*/myDecodeExt.memAccessKind.asBits(0)) {
    //        rTempModMemWord := (
    //          io.dbus.recvData.data(
    //            //15 downto 0
    //            offset=RegNext(io.dbus.sendData.addr)(
    //              (log2Up(cfg.mainWidth / 8)) - 1 downto 1
    //            ) * 16,
    //            16 bits
    //          ).resize(cfg.mainWidth)
    //        )
    //      } otherwise {
    //        rTempModMemWord := (
    //          io.dbus.recvData.data(
    //            //15 downto 0
    //            offset=RegNext(io.dbus.sendData.addr)(
    //              (log2Up(cfg.mainWidth / 8)) - 1 downto 1
    //            ) * 16,
    //            16 bits
    //          ).asSInt.resize(cfg.mainWidth).asUInt
    //        )
    //      }
    //      rMemAccessNonWordSizeState := True
    //      cMidModFront.haltIt()
    //    } else {
    //      myCurrExt.modMemWord := io.dbus.recvData.data.resized
    //      myCurrExt.modMemWordValid.foreach(current => {
    //        current := (
    //          // TODO: support more destination GPRs
    //          //!midModPayload(extIdxUp).gprIsZeroVec(0)
    //          True
    //        )
    //      })
    //    }
    //  }
    //  is (SnowHouseMemAccessSubKind.Sz32) {
    //    if (cfg.mainWidth > 32) {
    //      when (!/*RegNext*/myDecodeExt.memAccessKind.asBits(0)) {
    //        rTempModMemWord := (
    //          io.dbus.recvData.data(
    //            //31 downto 0
    //            offset=RegNext(io.dbus.sendData.addr)(
    //              (log2Up(cfg.mainWidth / 8)) - 1 downto 2
    //            ) * 32,
    //            32 bits
    //          ).resize(cfg.mainWidth)
    //        )
    //      } otherwise {
    //        rTempModMemWord := (
    //          io.dbus.recvData.data(
    //            //31 downto 0
    //            offset=RegNext(io.dbus.sendData.addr)(
    //              (log2Up(cfg.mainWidth / 8)) - 1 downto 2
    //            ) * 32,
    //            32 bits
    //          ).asSInt.resize(cfg.mainWidth).asUInt
    //        )
    //      }
    //      rMemAccessNonWordSizeState := True
    //      cMidModFront.haltIt()
    //    } else {
    //      myCurrExt.modMemWord := io.dbus.recvData.data.resized
    //      myCurrExt.modMemWordValid.foreach(current => {
    //        current := (
    //          // TODO: support more destination GPRs
    //          //!midModPayload(extIdxUp).gprIsZeroVec(0)
    //          True
    //        )
    //      })
    //    }
    //  }
    //  is (SnowHouseMemAccessSubKind.Sz64) {
    //    if (cfg.mainWidth > 64) {
    //      when (!/*RegNext*/myDecodeExt.memAccessKind.asBits(0)) {
    //        rTempModMemWord := (
    //          io.dbus.recvData.data(
    //            //63 downto 0
    //            offset=RegNext(io.dbus.sendData.addr)(
    //              (log2Up(cfg.mainWidth / 8)) - 1 downto 3
    //            ) * 64,
    //            64 bits
    //          ).resize(cfg.mainWidth)
    //        )
    //      } otherwise {
    //        rTempModMemWord := (
    //          io.dbus.recvData.data(
    //            //63 downto 0
    //            offset=RegNext(io.dbus.sendData.addr)(
    //              (log2Up(cfg.mainWidth / 8)) - 1 downto 3
    //            ) * 64,
    //            64 bits
    //          ).asSInt.resize(cfg.mainWidth).asUInt
    //        )
    //      }
    //      rMemAccessNonWordSizeState := True
    //      cMidModFront.haltIt()
    //    } else {
    //      myCurrExt.modMemWord := io.dbus.recvData.data.resized
    //      myCurrExt.modMemWordValid.foreach(current => {
    //        current := (
    //          // TODO: support more destination GPRs
    //          //!midModPayload(extIdxUp).gprIsZeroVec(0)
    //          True
    //        )
    //      })
    //    }
    //  }
    //}
    //} otherwise {
    //}
  }
  //elsewhen (rMemAccessNonWordSizeState) {
  //  val myDecodeExt = midModPayload(extIdxUp).outpDecodeExt
  //  val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
  //  val myCurrExt = (
  //    if (!mapElem.haveHowToSetIdx) (
  //      midModPayload(extIdxUp).myExt(
  //        0
  //      )
  //    ) else (
  //      midModPayload(extIdxUp).myExt(
  //        mapElem.howToSetIdx
  //      )
  //    )
  //  )
  //  myCurrExt.modMemWord := rTempModMemWord
  //  myCurrExt.modMemWordValid.foreach(current => {
  //    current := (
  //      // TODO: support more destination GPRs
  //      //!midModPayload(extIdxUp).gprIsZeroVec(0)
  //      True
  //    )
  //  })
  //  when (cMidModFront.up.isFiring) {
  //    rMemAccessNonWordSizeState := False
  //  }
  //}
  //if (cfg.haveZeroReg) {
  //}
  when (io.dbusExtraReady(2)) {
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

  //cfg.haveZeroReg match {
  //  case Some(myZeroRegIdx) => {
  //    when (midModPayload(extIdxUp).instrCnt.shouldIgnoreInstr(0)) {
  //      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //        midModPayload(extIdxUp).myExt(ydx).memAddr.foreach(
  //          current => {
  //            current := myZeroRegIdx
  //          }
  //        )
  //      }
  //    }
  //    when (midModPayload(extIdxUp).instrCnt.shouldIgnoreInstr(1)) {
  //      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //        midModPayload(extIdxUp).myExt(ydx).memAddrAlt.foreach(
  //          current => {
  //            current := myZeroRegIdx
  //          }
  //        )
  //      }
  //    }
  //    when (midModPayload(extIdxUp).instrCnt.shouldIgnoreInstr(2)) {
  //      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //        midModPayload(extIdxUp).myExt(ydx).memAddrFwd.foreach(
  //          current => {
  //            current.foreach(innerCurrent => {
  //              innerCurrent := myZeroRegIdx
  //            })
  //          }
  //        )
  //      }
  //    }
  //  }
  //  case None => {
  //  }
  //}

  def setMidModStages(): Unit = {
    regFile.io.midModStages(0) := midModPayload
  }
  setMidModStages()

  modFront(modBackPayload) := midModPayload(extIdxUp)
  //when (modFront.isValid) {
  //} otherwise {
  //}
}
case class SnowHousePipeStageWriteBack(
  args: SnowHousePipeStageArgs,
) extends Area {
  //def cfg = args.cfg
  //def regFile = args.regFile
  //def front = regFile.io.front
  //def frontPayload = regFile.io.frontPayload
  //def modFront = regFile.io.modFront
  //def modFrontPayload = regFile.io.modFrontPayload
  //def modBack = regFile.io.modBack
  //def modBackPayload = regFile.io.modBackPayload
  //def back = regFile.io.back
  //def backPayload = regFile.io.backPayload
  //def tempModFrontPayload = (
  //  regFile.io.tempModFrontPayload
  //)
  //val myHaveCurrWrite = Vec[Bool]({
  //  val tempArr = ArrayBuffer[Bool]()
  //  for (ydx <- 0 until regFile.memArrSize) {
  //    tempArr += (
  //      pastValidAfterReset
  //      && modBack.isFiring
  //      && regFile.mod.back.myWriteEnable(ydx)
  //    )
  //  }
  //  tempArr
  //})

  //val myHaveAnyCurrWrite = (
  //  myHaveCurrWrite.reduceLeft(_ || _)
  //)
  //val tempLeft = (
  //  regFile.mod.back.myWriteData
  //)
  //val tempHadFrontIsFiring: (Bool, Bool) = (
  //  RegNextWhen[Bool](
  //    next=True,
  //    cond=front.isFiring,
  //    init=False,
  //  ),
  //  null
  //)
  //val tempHadMid0FrontUpIsValid: (Bool, Bool) = (
  //  {
  //    val cond = (
  //      regFile.cMid0FrontArea.up.isValid
  //      && (
  //        tempHadFrontIsFiring._1
  //        || front.isFiring
  //      )
  //    )
  //    RegNextWhen(
  //      next=True,
  //      cond=cond,
  //      init=False,
  //    )
  //  },
  //  null
  //)
  //val tempHadMid0FrontDownIsValid: (Bool, UInt) = (
  //  {
  //    val cond = (
  //      regFile.cMid0FrontArea.down.isValid
  //      && (
  //        tempHadFrontIsFiring._1
  //        || front.isFiring
  //      ) && (
  //        tempHadMid0FrontUpIsValid._1
  //        || regFile.cMid0FrontArea.up.isValid
  //      )
  //    )
  //    RegNextWhen(
  //      next=True,
  //      cond=cond,
  //      init=False
  //    )
  //  },
  //  null
  //)
  //val tempHadMid0FrontDownIsFiring: (Bool, UInt) = (
  //  {
  //    val cond = (
  //      regFile.cMid0FrontArea.down.isFiring
  //      && (
  //        tempHadFrontIsFiring._1
  //        || front.isFiring
  //      ) && (
  //        tempHadMid0FrontUpIsValid._1
  //        || regFile.cMid0FrontArea.up.isValid
  //      )
  //    )
  //    RegNextWhen(
  //      next=True,
  //      cond=cond,
  //      init=False
  //    )
  //  },
  //  null
  //)
  //val tempHadModFrontIsValid: (Bool, Bool) = (
  //  {
  //    val cond = (
  //      modFront.isValid
  //      && (
  //        tempHadFrontIsFiring._1
  //        || front.isFiring
  //      ) && (
  //        tempHadMid0FrontUpIsValid._1
  //        || regFile.cMid0FrontArea.up.isValid
  //      )
  //    )
  //    RegNextWhen(
  //      next=True,
  //      cond=cond,
  //      init=False
  //    )
  //  },
  //  null
  //)
  //if (cfg.optFormal) {
  //  when (!tempHadModFrontIsValid._1) {
  //    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //      val wordCount = cfg.regFileWordCountArr(ydx)
  //      for (zdx <- 0 until cfg.regFileModRdPortCnt) {
  //        assume(
  //          (
  //            regFile.modMem(ydx)(zdx).readAsync(
  //              address=U(s"${log2Up(wordCount)}'d${zdx}")
  //            )
  //          ) === (
  //            0x0
  //          )
  //        )
  //      }
  //    }
  //  }
  //}
  //val tempHadModBackIsFiring: (Bool, Bool) = (
  //  {
  //    val cond = (
  //      modBack.isFiring
  //      && (
  //        tempHadFrontIsFiring._1
  //        || front.isFiring
  //      ) && (
  //        tempHadMid0FrontUpIsValid._1
  //        || regFile.cMid0FrontArea.up.isValid
  //      ) && (
  //        tempHadModFrontIsValid._1
  //        || modFront.isValid
  //      )
  //    )
  //    RegNextWhen(
  //      next=True,
  //      cond=cond,
  //      init=False
  //    )
  //  },
  //  null
  //)
  //val tempHadModBackIsValid: (Bool, Bool) = (
  //  {
  //    val cond = (
  //      modBack.isValid
  //      && (
  //        tempHadFrontIsFiring._1
  //        || front.isFiring
  //      ) && (
  //        tempHadMid0FrontUpIsValid._1
  //        || regFile.cMid0FrontArea.up.isValid
  //      ) && (
  //        tempHadModFrontIsValid._1
  //        || modFront.isValid
  //      )
  //    )
  //    RegNextWhen(
  //      next=True,
  //      cond=cond,
  //      init=False
  //    )
  //  },
  //  null
  //)
  //val tempHadBackIsFiring: (Bool, Bool) = (
  //  {
  //    val cond = (
  //      back.isFiring
  //      && (
  //        tempHadFrontIsFiring._1
  //        || front.isFiring
  //      ) && (
  //        tempHadMid0FrontUpIsValid._1
  //        || regFile.cMid0FrontArea.up.isValid
  //      ) && (
  //        tempHadModFrontIsValid._1
  //        || modFront.isValid
  //      ) && (
  //        tempHadModBackIsValid._1
  //        || modBack.isValid
  //      )
  //    )
  //    RegNextWhen(
  //      next=True,
  //      cond=cond,
  //      init=False
  //    )
  //  },
  //  null
  //)
  //val tempHadBackIsValid: (Bool, Bool) = (
  //  {
  //    val cond = (
  //      back.isValid
  //      && (
  //        tempHadFrontIsFiring._1
  //        || front.isFiring
  //      ) && (
  //        tempHadMid0FrontUpIsValid._1
  //        || regFile.cMid0FrontArea.up.isValid
  //      ) && (
  //        tempHadModFrontIsValid._1
  //        || modFront.isValid
  //      ) && (
  //        tempHadModBackIsValid._1
  //        || modBack.isValid
  //      ) && (
  //        tempHadModBackIsFiring._1
  //        || modBack.isFiring
  //      )
  //    )
  //    RegNextWhen(
  //      next=True,
  //      cond=cond,
  //      init=False
  //    )
  //  },
  //  null
  //)
  //val myHaveSeenPipeToModFrontFire = (
  //  /*KeepAttribute*/(
  //    tempHadFrontIsFiring._1
  //    && tempHadMid0FrontUpIsValid._1
  //    && tempHadModFrontIsValid._1
  //  )
  //  .setName(s"myHaveSeenPipeToModFrontFire")
  //)
  //val myHaveSeenPipeToWrite = Vec[Bool]({
  //  val tempArr = ArrayBuffer[Bool]()
  //  for (tempHaveCurrWrite <- myHaveCurrWrite.view) {
  //    tempArr += (
  //      myHaveSeenPipeToModFrontFire
  //      && tempHaveCurrWrite
  //    )
  //  }
  //  tempArr
  //})
  //def getMyHistHaveSeenPipeToWriteVecCond(
  //  ydx: Int,
  //  idx: Int,
  //) = (
  //  myHaveSeenPipeToWrite(ydx)
  //  && (
  //    regFile.mod.back.myWriteAddr(ydx) === idx
  //  )
  //)
  //def tempHistHaveSeenPipeToWriteV2dOuterDim = (
  //  4
  //)
  //val tempHaveSeenPipeToWriteV2dFindFirst_0 = (
  //  /*KeepAttribute*/(
  //    Vec.fill(tempHistHaveSeenPipeToWriteV2dOuterDim)({
  //      val temp = ArrayBuffer[Vec[Bool]]()
  //      for (wordCount <- cfg.regFileWordCountArr.view) {
  //        temp += Vec.fill(wordCount)(
  //          Bool()
  //        )
  //      }
  //      Vec[Vec[Bool]](temp)
  //    })
  //  )
  //  .setName(s"tempHaveSeenPipeToWriteV2dFindFirst_0")
  //)
  //for ((wordCount, ydx) <- cfg.regFileWordCountArr.view.zipWithIndex) {
  //  for (idx <- 0 until wordCount) {
  //    for (jdx <- 0 until tempHistHaveSeenPipeToWriteV2dOuterDim) {
  //      def tempFunc(
  //        someJdx: Int
  //      ) = (
  //        tempHaveSeenPipeToWriteV2dFindFirst_0(someJdx)(ydx)
  //      )
  //      if (jdx == 0) {
  //        tempFunc(jdx)(idx) := (
  //          getMyHistHaveSeenPipeToWriteVecCond(
  //            ydx=ydx,
  //            idx=idx,
  //          )
  //        )
  //      } else {
  //        tempFunc(jdx)(idx) := (
  //          RegNext(
  //            next=tempFunc(jdx)(idx),
  //            init=tempFunc(jdx)(idx).getZero,
  //          )
  //        )
  //        when (tempFunc(jdx - 1)(idx)) {
  //          tempFunc(jdx)(idx) := (
  //            RegNext(
  //              next=tempFunc(jdx - 1)(idx),
  //              init=tempFunc(jdx)(idx).getZero,
  //            )
  //          )
  //        }
  //      }
  //    }
  //  }
  //}
  //val rPrevOpCnt = Vec({
  //  val tempArr = ArrayBuffer[UInt]()
  //  for (ydx <- 0 until regFile.memArrSize) {
  //    tempArr += (
  //      RegNextWhen(
  //        next=modBack(modBackPayload).opCnt,
  //        cond=myHaveCurrWrite(ydx),
  //      )
  //      .init(0x0)
  //      .setName(s"rPrevOpCnt_${ydx}")
  //    )
  //  }
  //  tempArr
  //})
  //for ((rPrevOpCntElem, ydx) <- rPrevOpCnt.view.zipWithIndex) {
  //  assumeInitial(
  //    rPrevOpCntElem === 0x0
  //  )
  //}
  //val myCoverCond = (
  //  myHaveAnyCurrWrite
  //)
  //def myCoverVecSize = 8
  //val tempMyCoverInit = SnowHousePipePayload(cfg=cfg)
  //tempMyCoverInit.allowOverride
  //tempMyCoverInit := tempMyCoverInit.getZero
  //val myHistCoverVec = (
  //  /*KeepAttribute*/(
  //    History(
  //      that=modBack(modBackPayload),
  //      length=myCoverVecSize,
  //      when=myCoverCond,
  //      init=tempMyCoverInit,
  //    )
  //  )
  //)
  //val myHadWriteAt = (
  //  Vec({
  //    val tempArr = ArrayBuffer[Vec[Bool]]()
  //    for ((wordCount, ydx) <- cfg.regFileWordCountArr.view.zipWithIndex) {
  //      tempArr += (
  //        Vec.fill(wordCount)(
  //          Bool()
  //        )
  //      )
  //    }
  //    tempArr
  //  })
  //)
  //val myPrevWriteData = (
  //  /*KeepAttribute*/(
  //    Vec[Vec[UInt]]({
  //      val tempArr = ArrayBuffer[Vec[UInt]]()
  //      for ((wordCount, ydx) <- regFile.wordCountArr.view.zipWithIndex) {
  //        tempArr += {
  //          val myArr = new ArrayBuffer[UInt]()
  //          for (idx <- 0 until wordCount) {
  //            myArr += (
  //              UInt(cfg.mainWidth bits)
  //            )
  //          }
  //          Vec[UInt](myArr)
  //        }
  //      }
  //      tempArr
  //    })
  //  )
  //  .setName(s"myPrevWriteData")
  //)
  //for ((wordCount, ydx) <- regFile.wordCountArr.view.zipWithIndex) {
  //  for (idx <- 0 until wordCount) {
  //    myHadWriteAt(ydx)(idx) := (
  //      RegNext(
  //        next=myHadWriteAt(ydx)(idx),
  //        init=myHadWriteAt(ydx)(idx).getZero,
  //      )
  //    )
  //    myPrevWriteData(ydx)(idx) := (
  //      RegNext(
  //        next=myPrevWriteData(ydx)(idx),
  //        init=myPrevWriteData(ydx)(idx).getZero,
  //      )
  //    )
  //  }
  //}
  //def extIdxUp = PipeMemRmw.extIdxUp
  //def extIdxSaved = PipeMemRmw.extIdxSaved
  //def extIdxLim = PipeMemRmw.extIdxLim
  //val formalFwdModBackArea = (regFile.myHaveFormalFwd) generate (
  //  new Area {
  //    val myExt = (
  //      /*KeepAttribute*/(
  //        regFile.mkExt()
  //      )
  //      .setName(
  //        s"formalFwdModBackArea_"
  //        + s"myExt"
  //      )
  //    )
  //    val myFwd = (
  //      /*KeepAttribute*/(
  //        Vec.fill(extIdxLim)(
  //          regFile.mkFwd()
  //        )
  //      )
  //      .setName(
  //        s"formalFwdModBackArea_"
  //        + s"myFwd"
  //      )
  //    )
  //    for (extIdx <- 0 until extIdxLim) {
  //      for (ydx <- 0 until regFile.memArrSize) {
  //        myExt(ydx)(extIdx) := regFile.cBackArea.upExt(1)(ydx)(extIdx)
  //      }
  //      myFwd(extIdx) := regFile.cBackArea.upFwd(extIdx)
  //    }
  //    if (regFile.myHaveFormalFwd) {
  //      when (pastValidAfterReset) {
  //        when (
  //          !RegNextWhen(
  //            next=True,
  //            cond=modBack.isFiring,
  //            init=False,
  //          )
  //        ) {
  //          when (!modBack.isValid) {
  //            myExt.foreach(someExt => {
  //              assert(
  //                someExt(extIdxUp).main
  //                === someExt(extIdxUp).main.getZero
  //              )
  //            })
  //            assert(myFwd(extIdxUp) === myFwd(extIdxUp).getZero)
  //          }
  //          myExt.foreach(someExt => {
  //            assert(someExt(extIdxSaved) === someExt(extIdxSaved).getZero)
  //          })
  //          assert(myFwd(extIdxSaved) === myFwd(extIdxSaved).getZero)
  //        } 
  //        when (
  //          past(modBack.isFiring) init(False)
  //        ) {
  //          myExt.foreach(someExt => {
  //            assert(
  //              someExt(extIdxSaved)
  //              === (
  //                past(someExt(extIdxUp)) init(someExt(extIdxUp).getZero)
  //              )
  //            )
  //          })
  //          assert(
  //            myFwd(extIdxSaved)
  //            === (
  //              past(myFwd(extIdxUp)) init(myFwd(extIdxUp).getZero)
  //            )
  //          )
  //        }
  //        when (modBack.isValid) {
  //          assert(myFwd(extIdxUp) === modBack(modBackPayload).myFwd)
  //          for (ydx <- 0 until regFile.memArrSize) {
  //            assert(
  //              myExt(ydx)(extIdxUp).main
  //              === modBack(modBackPayload).myExt(ydx).main
  //            )
  //          }
  //        }
  //      }
  //    }
  //    val doFormalFwdUp = (
  //      PipeMemRmwDoFwdArea(
  //        fwdAreaName=s"formalFwdModBackArea_doFormalFwdUp",
  //        fwd=(
  //          myFwd(extIdxUp)
  //        ),
  //        setToMyFwdDataFunc=(
  //          ydx: Int,
  //          zdx: Int,
  //          myFwdData: UInt,
  //        ) => {
  //          when (pastValidAfterReset) {
  //            assert(myExt(ydx)(extIdxUp).rdMemWord(zdx) === myFwdData)
  //          }
  //        }
  //      )
  //    )
  //    val doFormalFwdSaved =  (
  //      PipeMemRmwDoFwdArea(
  //        fwdAreaName=s"formalFwdModBackArea_doFormalFwdSaved",
  //        fwd=(
  //          myFwd(extIdxSaved)
  //        ),
  //        setToMyFwdDataFunc=(
  //          ydx: Int,
  //          zdx: Int,
  //          myFwdData: UInt,
  //        ) => {
  //          when (pastValidAfterReset) {
  //            assert(myExt(ydx)(extIdxSaved).rdMemWord(zdx) === myFwdData)
  //          }
  //        }
  //      )
  //    )
  //  }
  //)
  //case class HistMain(
  //) extends Bundle {
  //  val myHaveCurrWrite = Vec.fill(regFile.memArrSize)(
  //    Bool()
  //  )
  //  val flow = Flow(SnowHousePipePayload(cfg=cfg))
  //}
  //def myHistMainSize = 8
  //val myHistMain: Vec[HistMain] = (
  //  /*KeepAttribute*/(Vec[HistMain]{
  //    val tempArr = ArrayBuffer[HistMain]()
  //    for (idx <- 0 until myHistMainSize) {
  //      def myHistMainCond = (
  //        pastValidAfterReset
  //        && modBack.isFiring
  //      )
  //      val temp = HistMain()
  //      temp.myHaveCurrWrite := myHaveCurrWrite
  //      temp.flow.payload := modBack(modBackPayload)
  //      temp.flow.valid := True
  //      tempArr += (
  //        if (idx == 0) {
  //          temp
  //        } else {
  //          RegNextWhen(
  //            next=temp,
  //            cond=(
  //              myHistMainCond
  //              && (
  //                tempArr.last.flow.instrCnt.any
  //                === temp.flow.instrCnt.any + 1
  //              )
  //            ),
  //            init=HistMain().getZero,
  //          )
  //        }
  //      )
  //    }
  //    tempArr
  //  })
  //)
  //val myHistAssumes = ArrayBuffer[Area]()
  //for ((myHistMainElem, myHistMainIdx) <- myHistMain.view.zipWithIndex) {
  //  myHistAssumes += myHistMainElem.flow.formalAssumes()
  //}
  //val myHistMainFireFindFirst = myHistMain.sFindFirst(_.flow.fire)
  //if (cfg.optFormal) {
  //  when (
  //    myHistMain(0).flow.fire
  //    && myHistMain(1).flow.fire
  //    && myHistMain(2).flow.fire
  //    && myHistMain(3).flow.fire
  //  ) {
  //    def flow0 = myHistMain(0).flow
  //    def flow1 = myHistMain(1).flow
  //    def flow2 = myHistMain(2).flow
  //    def flow3 = myHistMain(3).flow
  //    switch (flow3.op) {
  //      for (
  //        ((_, opInfoJmp), opInfoJmpIdx)
  //        <- cfg.opInfoMap.view.zipWithIndex
  //      ) {
  //        is (opInfoJmpIdx) {
  //          def jmpSelRdMemWord(
  //            idx: Int,
  //          ): UInt = {
  //            flow3.psExSetOutpModMemWordIo.selRdMemWord(
  //              opInfo=opInfoJmp,
  //              idx=idx,
  //            )
  //          }
  //          opInfoJmp.select match {
  //            case OpSelect.Cpy => {
  //              def handlePcChange(
  //                cond: Bool,
  //              ): Unit = {
  //                assert(flow3.decodeExt.opIsJmp)
  //                assert(
  //                  flow3.psExSetPc.fire === cond
  //                )
  //                when (cond) {
  //                  assert(
  //                    flow2.regPc
  //                    === flow3.regPc + (cfg.instrMainWidth / 8)
  //                  )
  //                  assert(
  //                    flow1.regPc
  //                    === flow2.regPc + (cfg.instrMainWidth / 8)
  //                  )
  //                  assert(
  //                    flow0.regPc === flow3.psExSetPc.nextPc
  //                  )
  //                } otherwise {
  //                  when (!flow2.decodeExt.opIsJmp) {
  //                    assert(!flow2.psExSetPc.fire)
  //                    assert(
  //                      flow2.regPc
  //                      === flow3.regPc + (cfg.instrMainWidth / 8)
  //                    )
  //                  }
  //                  when (!flow1.decodeExt.opIsJmp) {
  //                    assert(!flow1.psExSetPc.fire)
  //                  }
  //                  when (!flow0.decodeExt.opIsJmp) {
  //                    assert(!flow0.psExSetPc.fire)
  //                  }
  //                }
  //                assert(
  //                  flow0.instrCnt.any
  //                  === flow1.instrCnt.any + 1
  //                )
  //                assert(
  //                  flow1.instrCnt.any
  //                  === flow2.instrCnt.any + 1
  //                )
  //                assert(
  //                  flow2.instrCnt.any
  //                  === flow3.instrCnt.any + 1
  //                )
  //              }
  //              opInfoJmp.cpyOp.get match {
  //                case CpyOpKind.Jmp | CpyOpKind.Br => {
  //                  opInfoJmp.cond match {
  //                    case CondKind.Always => {
  //                      handlePcChange(
  //                        cond=True
  //                      )
  //                    }
  //                    case CondKind.Eq => {
  //                      handlePcChange(
  //                        cond=(
  //                          jmpSelRdMemWord(0) === jmpSelRdMemWord(1)
  //                        )
  //                      )
  //                    }
  //                    case CondKind.Ne => {
  //                      handlePcChange(
  //                        cond=(
  //                          jmpSelRdMemWord(0) =/= jmpSelRdMemWord(1)
  //                        )
  //                      )
  //                    }
  //                    case CondKind.Z => {
  //                      handlePcChange(
  //                        cond=(jmpSelRdMemWord(0) === 0)
  //                      )
  //                    }
  //                    case CondKind.Nz => {
  //                      handlePcChange(
  //                        cond=(jmpSelRdMemWord(0) =/= 0)
  //                      )
  //                    }
  //                    case cond => {
  //                      assert(
  //                        false,
  //                        s"not yet implemented: "
  //                        + s"opInfoJmp(${opInfoJmp} ${opInfoJmp.select}) "
  //                        + s"opInfoJmpIdx:${opInfoJmpIdx} "
  //                      )
  //                    }
  //                  }
  //                }
  //                case _ => {
  //                }
  //              }
  //            }
  //            case _ => {
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }
  //  when (pastValidAfterReset) {
  //    val tempCond = (
  //      (
  //        myHaveCurrWrite
  //      )
  //    )
  //    for ((wordCount, ydx) <- regFile.wordCountArr.view.zipWithIndex) {
  //      when (past(tempCond(ydx))) {
  //        for (idx <- 0 until wordCount) {
  //          when (
  //            past(regFile.mod.back.myWriteAddr(ydx)) === idx
  //          ) {
  //            myHadWriteAt(ydx)(idx) := (
  //              past(True) init(False)
  //            )
  //            myPrevWriteData(ydx)(idx) := (
  //              past(regFile.mod.back.myWriteData(ydx))
  //            )
  //          }
  //        }
  //      }
  //      val tempCond1 = (
  //        /*KeepAttribute*/(
  //          /*past*/(regFile.mod.back.myWriteEnable(ydx))
  //          && (
  //            myHadWriteAt(ydx)(
  //            /*past*/(regFile.mod.back.myWriteAddr(ydx)(
  //              log2Up(wordCount) - 1 downto 0
  //            ))
  //            )
  //          ) && (
  //            tempHaveSeenPipeToWriteV2dFindFirst_0(0)(ydx)(
  //              regFile.mod.back.myWriteAddr(ydx)(
  //                log2Up(wordCount) - 1 downto 0
  //              )
  //            )
  //          ) && (
  //            tempHaveSeenPipeToWriteV2dFindFirst_0(1)(ydx)(
  //              regFile.mod.back.myWriteAddr(ydx)(
  //                log2Up(wordCount) - 1 downto 0
  //              )
  //            )
  //          ) && (
  //            tempHaveSeenPipeToWriteV2dFindFirst_0(2)(ydx)(
  //              regFile.mod.back.myWriteAddr(ydx)(
  //                log2Up(wordCount) - 1 downto 0
  //              )
  //            )
  //          )
  //        )
  //        .setName(s"tempCond1_${ydx}")
  //      )
  //      val myTempRight = (
  //        /*KeepAttribute*/(
  //          Vec[UInt]({
  //            val myArr = new ArrayBuffer[UInt]()
  //            myArr += (
  //              modBack(modBackPayload).myExt(ydx)
  //              .rdMemWord(PipeMemRmw.modWrIdx)
  //            )
  //            myArr
  //          })
  //        )
  //        .setName(s"${regFile.pipeName}_myTempRight_${ydx}")
  //      )
  //      when (tempCond1) {
  //        val myTempLeft = (
  //          regFile.mod.back.myWriteData
  //        )
  //        val myDoFormalAssertRegular = Bool()
  //        myDoFormalAssertRegular := True
  //        for ((right, rightIdx) <- myTempRight.view.zipWithIndex) {
  //          switch (modBack(modBackPayload).op) {
  //            for (
  //              ((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex
  //            ) {
  //              is (opInfoIdx) {
  //                val psExOutpTempIo = (
  //                  modBack(modBackPayload)
  //                  .psExSetOutpModMemWordIo
  //                )
  //                def myGpr0 = (
  //                  psExOutpTempIo.selRdMemWord(opInfo=opInfo, idx=0)
  //                )
  //                def myLeft = (
  //                  psExOutpTempIo.selRdMemWord(opInfo=opInfo, idx=1)
  //                )
  //                def myRight = (
  //                  psExOutpTempIo.selRdMemWord(opInfo=opInfo, idx=2)
  //                )
  //                val howToSlice = cfg.shRegFileCfg.howToSlice
  //                val tempInfo = (
  //                  modBack(modBackPayload).gprIdxToMemAddrIdxMap(0)
  //                )
  //                val result: InstrResult = opInfo.select match {
  //                  case OpSelect.Cpy => {
  //                    opInfo.cpyOp.get match {
  //                      case CpyOpKind.Cpy => {
  //                        val result = InstrResult(cfg=cfg)()
  //                        opInfo.memAccess match {
  //                          case MemAccessKind.NoMemAccess => {
  //                            result.main := myLeft
  //                          }
  //                          case mem: MemAccessKind.Mem => {
  //                            myDoFormalAssertRegular := False
  //                            //mem.isStore match {
  //                            //  case Some(isStore) => {
  //                            //    if (!isStore) {
  //                            //      // don't formally verify stores I guess?
  //                            //      //result.main := myGpr0
  //                            //      myDoFormalAssert := False
  //                            //    } else {
  //                            //    }
  //                            //  }
  //                            //  case None => {
  //                            //    // TODO: support atomics
  //                            //    assert(
  //                            //      false,
  //                            //      s"atomics not supported yet"
  //                            //    )
  //                            //  }
  //                            //}
  //                          }
  //                        }
  //                        result
  //                      }
  //                      case CpyOpKind.Cpyu => {
  //                        val result = InstrResult(cfg=cfg)()
  //                        result.main.allowOverride
  //                        result.main := myGpr0
  //                        result.main(
  //                          (cfg.mainWidth - 1)
  //                          downto (cfg.mainWidth >> 1)
  //                        ) := (
  //                          myLeft(
  //                            (cfg.mainWidth >> 1) - 1
  //                            downto 0
  //                          )
  //                        )
  //                        result
  //                      }
  //                      case CpyOpKind.Jmp => {
  //                        val result = InstrResult(cfg=cfg)()
  //                        myDoFormalAssertRegular := False
  //                        result
  //                      }
  //                      case CpyOpKind.Br => {
  //                        val result = InstrResult(cfg=cfg)()
  //                        myDoFormalAssertRegular := False
  //                        result
  //                      }
  //                      case _ => {
  //                        assert(
  //                          false,
  //                          s"not yet implemented: "
  //                          + s"opInfo(${opInfo}) idx:${opInfoIdx}"
  //                        )
  //                        InstrResult(cfg=cfg)()
  //                      }
  //                    }
  //                  }
  //                  case OpSelect.Alu => {
  //                    opInfo.aluOp.get match {
  //                      case op => {
  //                        op.binopFunc(
  //                          cfg=cfg,
  //                          left=myLeft,
  //                          right=myRight,
  //                          carry=False,
  //                        )(
  //                        )
  //                      }
  //                    }
  //                  }
  //                  case OpSelect.AluShift => {
  //                    opInfo.aluShiftOp.get match {
  //                      case op => {
  //                        op.binopFunc(
  //                          cfg=cfg,
  //                          left=myLeft,
  //                          right=myRight,
  //                          carry=False,
  //                        )(
  //                        )
  //                      }
  //                    }
  //                  }
  //                  case OpSelect.MultiCycle => {
  //                    opInfo.multiCycleOp.get match {
  //                      case MultiCycleOpKind.Umul => {
  //                        val result = InstrResult(cfg=cfg)()
  //                        result.main := (
  //                          (myLeft * myRight)(result.main.bitsRange)
  //                        )
  //                        result
  //                      }
  //                      case _ => {
  //                        assert(
  //                          false,
  //                          s"not yet implemented: "
  //                          + s"opInfo(${opInfo}) idx:${opInfoIdx}"
  //                        )
  //                        InstrResult(cfg=cfg)()
  //                      }
  //                    }
  //                  }
  //                }
  //                when (myDoFormalAssertRegular) {
  //                  assert(
  //                    myTempLeft(
  //                      if (tempInfo.haveHowToSetIdx) (
  //                        tempInfo.howToSetIdx
  //                      ) else (
  //                        U(s"${log2Up(cfg.regFileCfg.memArrSize)}'d0")
  //                      )
  //                    ) === result.main
  //                  )
  //                }
  //              }
  //            }
  //          }
  //          if ((1 << log2Up(cfg.opInfoMap.size)) != cfg.opInfoMap.size) {
  //            assume(modBack(modBackPayload).op < cfg.opInfoMap.size)
  //          }
  //        }
  //      }
  //    }
  //  }
  //}
}
