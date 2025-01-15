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
case class SnowHousePipeStageArgs(
  //encInstrType: HardType,
  cfg: SnowHouseConfig,
  //opInfoMap: LinkedHashMap[Any, OpInfo],
  io: SnowHouseIo,
  link: CtrlLink,
  //prevLink: Option[CtrlLink],
  //nextLink: Option[CtrlLink],
  prevPayload: Payload[SnowHousePipePayload],
  currPayload: Payload[SnowHousePipePayload],
  //optFormal: Boolean,
  var regFile: PipeMemRmw[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeMemRmwDualRdTypeDisabled[UInt, Bool],
  ]
) {
  //def opInfoMap = cfg.opInfoMap
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
case class SnowHousePsExSetPcPayload(
  cfg: SnowHouseConfig
) extends Bundle {
  val nextPc = UInt(cfg.mainWidth bits)
  //val cnt = UInt(cfg.instrCntWidth bits)
}
case class SnowHousePipeStageInstrFetch(
  args: SnowHousePipeStageArgs,
  psIdHaltIt: Bool,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
) extends Area {
  //val io = SnowHousePipeStageInstrFetchIo(cfg=cfg)
  def cfg = args.cfg
  def io = args.io
  def cIf = args.link
  def pIf = args.currPayload
  //--------
  val up = cIf.up
  val down = cIf.down
  //--------
  //val psIdHaltIt = Bool()
  //val psExSetPc = Flow(SnowHousePsExSetPcPayload(cfg=cfg))
  val upModExt = (
    KeepAttribute(
      SnowHousePipePayload(cfg=cfg)
    )
    .setName(s"PipeStageInstrFetch_upModExt")
  )
  //val myInstrCnt = SnowHouseFormalInstrCnt(cfg=cfg)
  up(pIf) := upModExt
  upModExt := (
    RegNext(
      next=upModExt,
      init=upModExt.getZero
    )
  )
  def myInstrCnt = upModExt.instrCnt
  def nextRegPc = upModExt.regPc
  def nextRegPcSetItCnt = upModExt.regPcSetItCnt
  val rSavedExSetPc = {
    val temp = KeepAttribute(
      Reg(Flow(
        //UInt(cfg.mainWidth bits)
        SnowHousePsExSetPcPayload(cfg=cfg)
      ))
    )
    temp.init(temp.getZero)
    temp.setName(s"rSavedExSetPc")
  }
  //rSavedExSetPc.init(rSavedExSetPc.getZero)

  when (
    psExSetPc.fire
    && !rSavedExSetPc.fire
  ) {
    rSavedExSetPc := psExSetPc
  }
  //rSavedExSetPc := psExSetPc
  val rPrevRegPc = (
    RegNextWhen(
      //upModExt.regPc,
      next=nextRegPc,
      cond=up.isFiring,
    )
    init(nextRegPc.getZero)
  )
  val rPrevInstrCnt = /*(cfg.optFormal) generate*/ (
    RegNextWhen(
      next=myInstrCnt,
      cond=up.isFiring,
      init=myInstrCnt.getZero,
    )
    //init(upModExt.instrCnt.getZero)
  )

  when (up.isFiring) {
    //--------
    //rSavedExSetPc := rSavedExSetPc.getZero
    //if (cfg.optFormal) {
      myInstrCnt.any := rPrevInstrCnt.any + 1
    //}
    //--------
    //when (psExSetPc.fire) {
    //  nextRegPc := psExSetPc.nextPc //- (cfg.instrMainWidth / 8)
    //  //if (cfg.optFormal) {
    //    //myInstrCnt.jmp := rPrevInstrCnt.jmp + 1
    //    myInstrCnt.jmp := psExSetPc.cnt
    //  //}
    //} else
    when (rSavedExSetPc.fire) {
      rSavedExSetPc := rSavedExSetPc.getZero
      nextRegPcSetItCnt := 0x1
      nextRegPc := (
        rSavedExSetPc.nextPc //- (cfg.instrMainWidth / 8)
      )
      //if (cfg.optFormal) {
        myInstrCnt.jmp := rPrevInstrCnt.jmp + 1
        //myInstrCnt.jmp := rSavedExSetPc.cnt
      //}
    } otherwise {
      nextRegPcSetItCnt := 0x0
      nextRegPc := rPrevRegPc + (cfg.instrMainWidth / 8)
      //if (cfg.optFormal) {
        myInstrCnt.fwd := rPrevInstrCnt.fwd + 1
      //}
    }
  }
  //--------
  io.ibus.nextValid := (
    True
    //down.isReady // don't un-comment-out this
  )
  io.ibus.hostData.addr := nextRegPc //upModExt.regPc
  //--------
  //--------
  // TODO: formal
  //if (cfg.optFormal) {
  //  when (
  //    !io.ibus.ready //|| psIdHaltIt
  //  ) {
  //    assert(!cIf.up.isReady)
  //    //assert(!cIf.down.isValid)
  //    assert(myDoStallIt)
  //  }
  //  when (pastValidAfterReset) {
  //    when (!io.ibus.ready) {
  //      assert(stable(io.ibus.hostData.addr))
  //    }
  //  }
  //  when (pastValidAfterReset) {
  //    //when (past(io.ibus.nextValid)) {
  //    //  when (io.ibus.ready) {
  //        //when (!io.ibus.nextValid) {
  //        //  assume(!RegNext(io.ibus.ready))
  //        //}
  //        when (!past(io.ibus.nextValid)) {
  //          assume(!io.ibus.ready)
  //        }
  //    //  }
  //    //}
  //  }
  //  when (up.isFiring) {
  //    assert(!myDoStallIt)
  //  }
  //  when (pastValidAfterReset) {
  //    when (past(up.isFiring)) {
  //      assert(rSavedExSetPc === rSavedExSetPc.getZero)
  //    }
  //    when (up.isFiring) {
  //      when (
  //        !psExSetPc.fire
  //        && !rSavedExSetPc.fire
  //      ) {
  //        assert(nextRegPc === rPrevRegPc + (cfg.instrMainWidth / 8))
  //        assert(myInstrCnt.fwd === rPrevInstrCnt.fwd + 1)
  //        assert(stable(myInstrCnt.jmp))
  //      } elsewhen (psExSetPc.fire) {
  //        assert(nextRegPc === psExSetPc.nextPc)
  //        assert(stable(myInstrCnt.fwd))
  //        assert(myInstrCnt.jmp === rPrevInstrCnt.jmp + 1)
  //      } otherwise { // when (rSavedExSetPc.fire)
  //        assert(nextRegPc === rSavedExSetPc.nextPc)
  //        assert(stable(myInstrCnt.fwd))
  //        assert(myInstrCnt.jmp === rPrevInstrCnt.jmp + 1)
  //      }
  //      assert(myInstrCnt.any === rPrevInstrCnt.any + 1)
  //    } otherwise {
  //      assert(stable(nextRegPc))
  //      assert(stable(myInstrCnt.fwd))
  //      assert(stable(myInstrCnt.jmp))
  //      assert(stable(myInstrCnt.any))
  //    }
  //  }
  //}
  //--------
}
case class SnowHousePipeStageInstrDecode(
  //val cfg: SnowHouseConfig,
  //var args: Option[SnowHousePipeStageArgs]=None,
  val args: SnowHousePipeStageArgs,
  val psIdHaltIt: Bool,
  val psExSetPc: Flow[SnowHousePsExSetPcPayload],
  val pcChangeState: Bool,
  val shouldIgnoreInstr: Bool,
  val doDecodeFunc: (SnowHousePipeStageInstrDecode) => Area,
) extends Area {
  //--------
  //def decInstr: UInt
  def cfg = args.cfg
  //def opInfoMap = args.opInfoMap
  def modIo = args.io
  def pIf = args.prevPayload
  def pId = args.currPayload
  def opInfoMap = cfg.opInfoMap
  def io = args.io
  def cId = args.link
  def payload = args.currPayload
  def optFormal = cfg.optFormal
  def regFile = args.regFile
  //--------
  //def doDecode(): Area
  //--------
  //val psIdHaltIt = Bool()
  //def myDoStallIt(): Unit = {
  //  //psIdHaltIt := True
  //  cId.haltIt()
  //  //cId.terminateIt()
  //  //cId.duplicateIt()
  //}
  //val decInstr = UInt(log2Up(opInfoMap.size) bits)
  //--------
  val up = cId.up
  val down = cId.down
  //--------
  val upPayload = /*Vec.fill(2)*/(
    SnowHousePipePayload(cfg=cfg)
  )
  //val rPrevUpPayload = {
  //  val temp = Reg(SnowHousePipePayload(cfg=cfg))
  //  temp.init(temp.getZero)
  //  temp
  //}
  //val rPrevUpPayload = (
  //  RegNextWhen(
  //    next=upPayload,
  //    cond=up.isFiring,
  //    init=upPayload.getZero,
  //  )
  //)
  val startDecode = Bool()
  //val nextPrevInstrWasJump = Bool()
  //val rPrevInstrWasJump = (
  //  RegNextWhen(
  //    next=nextPrevInstrWasJump,
  //    cond=(
  //      up.isFiring
  //      && startDecode
  //    ),
  //    init=nextPrevInstrWasJump.getZero
  //  )
  //)
  //nextPrevInstrWasJump := rPrevInstrWasJump

  //up(pId) := upModExt
  up(pId) := upPayload//(0)
  val nextSetUpPayloadState = Vec.fill(2)(
    Bool()
  )
  val rSetUpPayloadState = {
    val temp = RegNext(
      nextSetUpPayloadState,
      //init=nextS
    )
    for (idx <- 0 until nextSetUpPayloadState.size) {
      temp(idx).init(temp(idx).getZero)
    }
    temp
  }
  //nextSetUpPayloadState := rSetUpPayloadState
  for (idx <- 0 until rSetUpPayloadState.size) {
    nextSetUpPayloadState(idx) := rSetUpPayloadState(idx)
  }
  upPayload := (
    RegNext(
      next=upPayload,
      init=upPayload.getZero,
    )
    init(upPayload.getZero)
  )
  upPayload.allowOverride

  val multiInstrCntWidth = 3
  val nextMultiInstrCnt = UInt(multiInstrCntWidth bits)
  val rMultiInstrCnt = (
    RegNext(
      next=nextMultiInstrCnt,
      init=(
        //nextMultiInstrCnt.getZero
        U(multiInstrCntWidth bits, default -> True)
      )
    )
  )
  nextMultiInstrCnt := rMultiInstrCnt
  val myInstr = UInt(cfg.instrMainWidth bits)
  myInstr := (
    RegNext(
      next=myInstr,
      init=myInstr.getZero
    )
  )
  when (up.isValid) {
    when (
      io.ibus.rValid
      //&& !io.ibus.ready
    ) {
      when (!rSetUpPayloadState(1)) {
        when (!io.ibus.ready) {
          cId.haltIt()
        } otherwise {
          nextSetUpPayloadState(1) := True
          myInstr := (
            io.ibus.devData.instr
          )
        }
      }
      //myDoStallIt()
      //cId.duplicateIt()
    }
  }
  when (
    if (cfg.supportUcode) (
      rMultiInstrCnt.msb
    ) else (
      True
    )
  ) {
    when (up.isValid) {
      //upPayload.regPc := cId.up(pIf).regPc
      //upPayload.instrCnt := cId.up(pIf).instrCnt
      when (!rSetUpPayloadState(0)) {
        //when (io.ibus.rValid && io.ibus.ready) {
          //rPrevUpPayload := RegNext(
          //  next=upPayload,
          //  init=upPayload.getZero,
          //)
          upPayload := up(pIf)
          //val myDecodeArea = doDecodeFunc(this)
          nextSetUpPayloadState(0) := True
        //}
      }
      //when (up.isFiring) {
      //  //up(pId) := upPayload
      //  nextSetUpPayloadState(0) := False
      //}
    }
  } otherwise {
    //when (nextMultiInstrCnt =/= 0) {
    //  cId.duplicateIt()
    //}
    //when (!(
    //  down.isFiring
    //  && rMultiInstrCnt - 1 === 0
    //)) {
    //  cId.duplicateIt()
    //}
    cId.duplicateIt()
    when (down.isFiring) {
      nextMultiInstrCnt := rMultiInstrCnt - 1
    }
  }
  when (up.isFiring) {
    //up(pId) := upPayload
    nextSetUpPayloadState(0) := False
    nextSetUpPayloadState(1) := False
    //rPrevUpPayload := RegNext(
    //  next=upPayload,
    //  init=upPayload.getZero,
    //)
  }
  //if (optFormal) {
  //  when (pastValidAfterReset) {
  //    when (past(up.isFiring) init(False)) {
  //      assert(!rSetUpPayloadState)
  //    }
  //    when (!(past(up.isValid) init(False))) {
  //      assert(stable(rSetUpPayloadState))
  //    }
  //    when (rSetUpPayloadState) {
  //      assert(up.isValid)
  //    }
  //    //when (rose(rSetUpPayloadState)) {
  //    //  assert(
  //    //    upPayload.
  //    //  )
  //    //}
  //  }
  //}
  upPayload.regPcPlusInstrSize := (
    upPayload.regPc + (cfg.instrMainWidth / 8)
  )
  //--------
  //upPayload.regPcPlusImm := (
  //  upPayload.regPc + upPayload.imm //+ (cfg.instrMainWidth / 8)
  //)
  //--------
  //val upGprIdxToRegFileMemAddrMap = (
  //  upPayload.gprIdxToRegFileMemAddrMap
  //)
  //for (
  //  (mapElem, mapIdx) <- upGprIdxToRegFileMemAddrMap.view.zipWithIndex
  //) {
  //  //mapElem := (
  //  //  upPayload.gprIdxVec(mapIdx)
  //  //)
  //  val howToSlice = cfg.shRegFileCfg.howToSlice
  //  for ((howToSet, outerIdx) <- howToSlice.view.zipWithIndex) {
  //  }
  //}
  val upGprIdxToMemAddrIdxMap = upPayload.gprIdxToMemAddrIdxMap
  //for ((mapElem, ydx) <- upGprIdxToMemAddrIdxMap.view.zipWithIndex) {
  //  switch (mapElem) {
  //    val howToSlice = cfg.shRegFileCfg.howToS
  //    for ((howTo, howToIdx) <- howToSlice
  //  }
  //}
  for ((gprIdx, zdx) <- upPayload.gprIdxVec.view.zipWithIndex) {
    upPayload.myExt(0).memAddr(zdx) := gprIdx
    //switch (gprIdx) {
    //  val howToSlice = cfg.shRegFileCfg.howToSlice
    //  var outerCnt: Int = 0
    //  for ((howToSet, howToSetIdx) <- howToSlice.view.zipWithIndex) {
    //    //var cnt: Int = 0
    //    for ((howTo, howToIdx) <- howToSet.view.zipWithIndex) {
    //      is (
    //        //outerCnt
    //        howTo
    //      ) {
    //        //println(
    //        //  s"debug: "
    //        //  + s"outerCnt:${outerCnt}"
    //        //  + s"; "
    //        //  //+ s"cnt:${cnt}; "
    //        //  + s"zdx:${zdx} "
    //        //  + s"howTo:${howTo} howToIdx:${howToIdx} "
    //        //  + s"howToSetIdx:${howToSetIdx}"
    //        //)
    //        val mapElem = upGprIdxToMemAddrIdxMap(zdx)
    //        mapElem.idx := (
    //          //howTo
    //          //cnt
    //          howToIdx
    //        )
    //        if (mapElem.haveHowToSetIdx) {
    //          mapElem.howToSetIdx := howToSetIdx
    //        }
    //        upPayload.myExt(howToSetIdx).memAddr(zdx) := howToIdx
    //        //:= (
    //        //  howToSetIdx
    //        //  //howToIdx
    //        //)
    //      }
    //      //cnt += 1
    //      outerCnt += 1
    //    }
    //  }
    //  assert(
    //    outerCnt == cfg.numGprs,
    //    s"eek! cnt:${outerCnt} != cfg.numGprs:${cfg.numGprs}"
    //  )
    //}
  }
  //--------
  if (cfg.optFormal) {
    when (pastValidAfterReset()) {
      //when (!io.ibus.ready) {
      //  assert(!up.isFiring)
      //}
      when (
        !past(up.isFiring)
        && io.ibus.ready
      ) {
        //assert(stable(io.ibus.ready))
        assume(stable(io.ibus.ready))
      }
      when (past(io.ibus.nextValid)) {
        when (io.ibus.ready) {
          //cover(
          //  up.isValid
          //)
          cover(up.isFiring)
          assert(
            //up.isFiring
            up.isValid
          )
          when (!io.ibus.nextValid) {
            assume(!(RegNext(next=io.ibus.ready, init=False)))
          }
        }
      }
    }
  }
  //val myDecodeArea = doDecodeFunc(this)
  val nextDoDecodeState = Bool()
  val rDoDecodeState = RegNext(
    next=nextDoDecodeState,
    init=nextDoDecodeState.getZero,
  )
  nextDoDecodeState := rDoDecodeState
  //when (!rDoDecodeState) {
  //}
  //when (
  //  io.ibus.rValid
  //  && io.ibus.ready
  //  //&& down.isReady
  //) {
  //  //when (down.isReady) {
  //  when (!rSetUpPayloadState) {
  //    val myDecodeArea = doDecodeFunc(this)
  //  }
  //  //}
  //} otherwise {
  //  //cId.haltIt()
  //  when (
  //    //nextSetUpPayloadState
  //    up.isValid
  //    //&& rSetUpPayloadState
  //    && !down.isFiring
  //  ) {
  //    cId.duplicateIt()
  //  }
  //  //cId.terminateIt()
  //}
  //val shouldBubble = Bool()
  //shouldBubble := False
  val tempInstr = UInt(cfg.instrMainWidth bits)
  tempInstr := (
    RegNext(
      next=tempInstr,
      init=tempInstr.getZero,
    )
  )
  tempInstr.allowOverride
  startDecode := False
  //val rSavedExSetPc = {
  //  val temp = KeepAttribute(
  //    Reg(Flow(
  //      //UInt(cfg.mainWidth bits)
  //      SnowHousePsExSetPcPayload(cfg=cfg)
  //    ))
  //  )
  //  temp.init(temp.getZero)
  //  temp.setName(s"rSavedExSetPc")
  //}
  //when (
  //  psExSetPc.fire
  //  && !rSavedExSetPc.fire
  //) {
  //  rSavedExSetPc := psExSetPc
  //}
  //when (up.isValid) {
  //  when (io.ibus.rValid && io.ibus.ready) {
  //    myInstr := (
  //      io.ibus.devData.instr
  //    )
  //  }
  //}
  //val nextMultiCycleStateIsIdle = Bool()
  //nextMultiCycleStateIsIdle := (
  //  False
  //  //RegNext(
  //  //  next=nextMultiCycleStateIsIdle,
  //  //  init=nextMultiCycleStateIsIdle.getZero,
  //  //)
  //)
  if (cfg.irqCfg != None) {
    upPayload.takeIrq := False
    //io.idsIraIrq.ready := False
  }
  val nextPrevInstrBlockedIrq = (
    //cfg.irqCfg != None
    true
  ) generate (
    Bool()
  )
  val rPrevInstrBlockedIrq = (
    //cfg.irqCfg != None
    true
  ) generate (
    RegNext(
      next=nextPrevInstrBlockedIrq,
      init=nextPrevInstrBlockedIrq.getZero,
    )
  )
  //io.idsIraIrq.ready := False
  nextPrevInstrBlockedIrq := rPrevInstrBlockedIrq
  val tempIsFiring = (
    KeepAttribute(
      Bool()
    )
    .setName(s"GenInstrDecode_tempIsFiring")
  )
  tempIsFiring := up.isFiring
  //when (
  //  nextMultiInstrCnt(0 downto 0) =/= 0x0
  //  && rMultiInstrCnt(0 downto 0) === 0x0
  //) {
  //  tempIsFiring := False//up.isFiring
  //  //when (!rPrevInstrBlockedIrq) {
  //  //  when (
  //  //    //psId.nextMultiInstrCnt === 1
  //  //    //rPrevInstrBlockedIrq
  //  //    upPayload.blockIrq
  //  //    && io.idsIraIrq.rValid
  //  //  ) {
  //  //    //io.idsIraIrq.ready := True
  //  //    nextPrevInstrBlockedIrq := True
  //  //  }
  //  //} otherwise { // when (rPrevInstrBlockedIrq)
  //  //}
  //}
  //elsewhen(
  //  nextMultiInstrCnt === 0x0
  //  && rMultiInstrCnt === 0x0
  //) {
  //  //tempIsFiring := 
  //} otherwise {
  //  //tempIsFiring := (
  //  //  down.isFiring
  //  //  && rMultiInstrCnt - 1 === 0x0
  //  //)
  //}
  //val rPrevTakeIrq = (
  //  RegNextWhen(
  //    next=upPayload.takeIrq,
  //    cond=tempIsFiring,
  //    init=upPayload.takeIrq.getZero,
  //  )
  //)
  //tempInstr := myInstr
  val myDecodeAreaWithoutUcode = (
    !cfg.supportUcode
  ) generate(
    doDecodeFunc(this)
  )
  when (up.isValid) {
    when (
      //upPayload.regPcSetItCnt =/= 0
      if (cfg.supportUcode) (
        //!rPrevInstrWasJump
        //|| 
        !pcChangeState
        || (
          pcChangeState
          && upPayload.regPcSetItCnt =/= 0x0
        )
      ) else (
        True
      )
    ) {
      startDecode := True
      tempInstr := myInstr
      val myDecodeAreaWithUcode = (
        cfg.supportUcode
      ) generate(
        doDecodeFunc(this)
      )
      when (
        if (cfg.supportUcode) (
          rMultiInstrCnt.msb// === 0x0
        ) else (
          True
        )
      ) {
        //when (io.ibus.rValid && io.ibus.ready) {
        //  myInstr := (
        //    io.ibus.devData.instr
        //  )
        //}
        //doDecode := True
        //nextDoDecodeState := True
        //val myDecodeArea = doDecodeFunc(this)
        //when (!down.isFiring) {
        //  cId.duplicateIt()
        //}
        //when (!rDoDecodeState) {
        //  when (io.ibus.rValid && io.ibus.ready) {
        //    tempInstr := io.ibus.devData.instr
        //    nextDoDecodeState := True
        //  } otherwise {
        //    //shouldBubble := True
        //    //cId.duplicateIt()
        //    //cId.haltIt()
        //  }
        //}
        //when (up.isFiring) {
        //  nextDoDecodeState := False
        //}
        val irqArea = (
          cfg.irqCfg != None
        ) generate (
          new Area {
            when (tempIsFiring) {
              when (
                (
                  !upPayload.blockIrq
                  || rPrevInstrBlockedIrq
                ) && (
                  io.idsIraIrq.rValid
                )
              ) {
                //io.idsIraIrq.ready := True
                //when (!rPrevTakeIrq) {
                  upPayload.takeIrq := True
                  //nextPrevInstrWasJump := False
                //}
              }
              nextPrevInstrBlockedIrq := (
                upPayload.blockIrq
              )
            }
          }
        )
      }
    }
    //when (
    //  rMultiInstrCnt === 0x0
    //) {
    //}
  }
  //val rPrevPcChangeState = (
  //  KeepAttribute(
  //    RegNextWhen(
  //      next=(
  //        pcChangeState
  //        && upPayload.regPcSetItCnt =/= 0x0
  //        //shouldIgnoreInstr
  //      ),
  //      cond=down.isFiring,
  //      init=(
  //        //shouldIgnoreInstr.getZero
  //        False
  //      ),
  //    )
  //  )
  //  .setName(s"GenInstrDecode_rPrevPcChangeState")
  //)
  //when (tempIsFiring) {
  //  when (
  //    (
  //      !upPayload.blockIrq
  //      || rPrevInstrBlockedIrq
  //    ) && (
  //      io.idsIraIrq.rValid
  //    ) && (
  //      //!rPrevPcChangeState
  //      !(
  //        pcChangeState
  //        && upPayload.regPcSetItCnt =/= 0x0
  //      )
  //    )
  //  ) {
  //    //io.idsIraIrq.ready := True
  //    when (!rPrevTakeIrq) {
  //      upPayload.takeIrq := True
  //      nextPrevInstrWasJump := False
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
private[libsnowhouse] object PcChangeState
extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    Idle,
    WaitTwoInstrs
    = newElement()
}
case class SnowHousePipeStageExecuteSetOutpModMemWordIo(
  cfg: SnowHouseConfig,
) extends Bundle {
  val currOp = /*in*/(UInt(log2Up(cfg.opInfoMap.size) bits))
  val splitOp = /*in*/(SnowHouseSplitOp(cfg=cfg))
  //println(
  //  s"currOp.getWidth: ${currOp.getWidth}"
  //)
  //val doIt = /*in*/(Bool())
  val tempVecSize = 3 // TODO: temporary size of `3`
  val gprIsZeroVec = (
    cfg.myHaveZeroReg
  ) generate (
    Vec.fill(tempVecSize)(
      Bool()
    )
  )
  //val dbus = new LcvStallIo[DbusHostPayload, DbusDevPayload](
  //  hostPayloadType=Some(DbusHostPayload(cfg=cfg)),
  //  devPayloadType=Some(DbusDevPayload(cfg=cfg)),
  //)
  val dbusHostPayload = (
    DbusHostPayload(cfg=cfg)
  )
  val rdMemWord = /*in*/(Vec.fill(tempVecSize)(
    UInt(cfg.mainWidth bits)
  ))
  val regPc = /*in*/(UInt(cfg.mainWidth bits))
  val regPcSetItCnt = /*in*/(UInt(
    //cfg.instrCntWidth bits
    1 bits
  ))
  //val instrCnt = /*in*/(SnowHouseInstrCnt(cfg=cfg))
  val upIsFiring = /*in*/(Bool())
  val upIsValid = /*in*/(Bool())
  val upIsReady = /*in*/(Bool())
  val downIsFiring = /*in*/(Bool())
  val downIsValid = /*in*/(Bool())
  val downIsReady = /*in*/(Bool())
  //val downIsFiring = /*in*/(Bool())
  val regPcPlusInstrSize = /*in*/(UInt(cfg.mainWidth bits))
  val regPcPlusImm = /*in*/(UInt(cfg.mainWidth bits))
  val imm = /*in*/(UInt(cfg.mainWidth bits))
  val pcChangeState = /*out*/(Bool()) ///*in*/(Flow(PcChangeState()))
  val shouldIgnoreInstr = /*out*/(Bool())
  val rAluFlags = (
    cfg.myHaveAluFlags
  ) generate (
    /*out*/(UInt(cfg.mainWidth bits))
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
    /*out*/(UInt(cfg.mainWidth bits))
  )
  val rIra = (
    /*out*/(UInt(cfg.mainWidth bits))
  )
  val rIe = (
    /*out*/(
      //UInt(cfg.mainWidth bits)
      Bool()
    )
  )
  val rIty = (
    /*out*/(UInt(cfg.mainWidth bits))
  )
  val rSty = (
    /*out*/(UInt(cfg.mainWidth bits))
  )
  val rHi = (
    /*out*/(UInt(cfg.mainWidth bits))
  )
  val rLo = (
    /*out*/(UInt(cfg.mainWidth bits))
  )
  val rIndexReg = (
    /*out*/UInt(cfg.mainWidth bits)
  )
  val rMulHiOutp = (
    /*out*/UInt(cfg.mainWidth bits)
  )
  val rDivHiOutp = (
    /*out*/UInt(cfg.mainWidth bits)
  )
  val rModHiOutp = (
    /*out*/UInt(cfg.mainWidth bits)
  )
  val takeIrq = /*in*/(
    Bool()
  )
  //val doIrq = (
  //  cfg.irqCfg != None
  //) generate (/*in*/(
  //  Bool()
  //))
  def selRdMemWord(
    opInfo: OpInfo,
    idx: Int,
  ): UInt = {
    //return rdMemWord(idx)
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
          //case DstKind.Pc => {
          //  regPc
          //}
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
            imm
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
    //return innerFunc(idx=idx)
    opInfo.select match {
      case OpSelect.Cpy => {
        opInfo.cpyOp.get match {
          case CpyOpKind.Br => {
            //assert(
            //  opInfo._validArgsTuple != null,
            //  s"select:${opInfo.select} "
            //  + s"dst:(${opInfo.dstArr}) src:(${opInfo.srcArr})"
            //)
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
        if (
          opInfo.dstArr(0) == DstKind.Spr(SprKind.AluFlags)
        ) {
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
  val outpWrMemAddr = /*out*/(
    UInt(log2Up(cfg.regFileCfg.wordCountMax) bits)
  )
  val inpPushMemAddr = /*in*/(
    Vec.fill(2)(
      UInt(log2Up(cfg.regFileCfg.wordCountMax) bits)
    )
  )
  val modMemWordValid = /*out*/(Bool())
  val modMemWord = /*out*/(Vec.fill(1)( // TODO: temporary size of `1`
    UInt(cfg.mainWidth bits)
  ))
  val psExSetPc = /*out*/(Flow(
    //UInt(cfg.mainWidth bits)
    SnowHousePsExSetPcPayload(cfg=cfg)
  ))
  //val isCpyAlu = /*out*/Bool()
  //val isMemAccess = /*out*/Bool()
  //val isMultiCycle = /*out*/Bool()
  val decodeExt = /*out*/(
    SnowHouseDecodeExt(cfg=cfg)
  )
  val multiCycleOpInfoIdx = /*out*/(
    UInt(log2Up(cfg.multiCycleOpInfoMap.size) bits)
  )
  def opIs = decodeExt.opIs
  def opIsMemAccess = decodeExt.opIsMemAccess
  def opIsCpyNonJmpAlu = decodeExt.opIsCpyNonJmpAlu
  def opIsJmp = decodeExt.opIsJmp
  def opIsMultiCycle = decodeExt.opIsMultiCycle
  //--------
  //val dbusHostPayload = /*out*/(DbusHostPayload(cfg=cfg))
  def jmpAddrIdx = (
    //2
    //0
    1
  )
  def brCondIdx = Array[Int](0, 1)
  val haveRetIraState = (
    cfg.irqCfg match {
      case Some(irqCfg) => {
        irqCfg match {
          case SnowHouseIrqConfig.IraIds(allowIrqStorm) => {
            //!allowIrqStorm
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
    Reg(Bool(), init=False)
  )
}
case class SnowHousePipeStageExecuteSetOutpModMemWord(
  //cfg: SnowHouseConfig,
  args: SnowHousePipeStageArgs,
) extends Area {
  def cfg = args.cfg
  val modIo = args.io
  val io = SnowHousePipeStageExecuteSetOutpModMemWordIo(cfg=cfg)
  //val io.currOp = UInt(log2Up(cfg.opInfoMap.size) bits)
  //io.modMemWordValid := (
  //  RegNext(
  //    next=io.modMemWordValid,
  //    init=io.modMemWordValid.getZero,
  //  )
  //)
  io.modMemWord := (
    RegNext(
      next=io.modMemWord,
      init=io.modMemWord.getZero,
    )
  )
  //when (someCond) {
  val myModMemWordValid = (
    //io.doIt //True
    if (cfg.myHaveZeroReg) (
      // TODO: support more register simultaneous writes
      !io.gprIsZeroVec(0)
    ) else (
      True
    )
  )
  io.modMemWordValid := (
    myModMemWordValid
  )
  io.psExSetPc := io.psExSetPc.getZero
  io.dbusHostPayload := (
    RegNext(
      next=io.dbusHostPayload,
      init=io.dbusHostPayload.getZero,
    )
  )
  io.dbusHostPayload.addr.allowOverride
  io.dbusHostPayload.data.allowOverride
  //io.psExSetPc.valid := (
  //  //RegNext(
  //  //  next=io.psExSetPc.valid,
  //  //  init=io.psExSetPc.valid.getZero,
  //  //)
  //  //io.doIt
  //  False
  //)
  //io.psExSetPc.payload := (
  //  //RegNext(
  //  //  next=io.psExSetPc.payload,
  //  //  init=io.psExSetPc.payload.getZero,
  //  //)
  //  io.psExSetPc.payload.getZero
  //)
  //if (cfg.optFormal) {
  //  if ((1 << io.currOp.getWidth) != cfg.opInfoMap.size) {
  //    assert(
  //      (1 << io.currOp.getWidth) < cfg.opInfoMap.size
  //    )
  //  }
  //}
  io.opIs := 0x0
  //io.decodeExt.memAccessLdStKind := False
  io.decodeExt.memAccessKind := SnowHouseMemAccessKind.LoadU
  io.decodeExt.memAccessSubKind := SnowHouseMemAccessSubKind.Sz8
  io.decodeExt.memAccessIsPush := False
  //io.outpWrMemAddr := io.inpPushMemAddr(PipeMemRmw.modWrIdx)
  //--------
  //io.outpWrMemAddr := (
  //  RegNext(
  //    next=io.outpWrMemAddr,
  //    init=io.outpWrMemAddr.getZero,
  //  )
  //)
  //io.outpWrMemAddr := io.inpPushMemAddr(PipeMemRmw.modRdIdxStart)
  io.opIsJmp.allowOverride
  io.opIsJmp := (
    //io.pcChangeState.payload === PcChangeState.Idle
    //&& io.pcChangeState.fire
    //io.pcChangeState
    //&& 
    io.psExSetPc.fire
  )
  io.shouldIgnoreInstr := (
    //False
    RegNext(
      next=io.shouldIgnoreInstr,
      init=io.shouldIgnoreInstr.getZero,
    )
  )
  io.pcChangeState := (
    RegNext(
      next=io.pcChangeState,
      init=io.pcChangeState.getZero,
    )
  )
  //val rSavedInstrCnt = (
  //  RegNextWhen(
  //    next=io.instrCnt,
  //    cond=io.upIsFiring,
  //    init=io.instrCnt.getZero,
  //  )
  //)
  //val rSavedRegPc = (
  //  //Reg(UInt(cfg.mainWidth bits))
  //  //init(0x0)
  //  RegNextWhen(
  //    next=(
  //      //io.psExSetPc.nextPc
  //      io.regPc
  //    ),
  //    cond=(
  //      //!io.pcChangeState
  //      //&& io.upIsFiring
  //      io.psExSetPc.valid
  //      && !io.shouldIgnoreInstr
  //      //&& !io.shouldIgnoreInstr
  //      //&& io.upIsFiring
  //    ),
  //    init=(
  //      //io.psExSetPc.nextPc.getZero
  //      io.regPc.getZero
  //    ),
  //  )
  //)
  //io.rAluFlags := (
  //  RegNext(
  //    next=io.rAluFlags,
  //    init=io.rAluFlags.getZero,
  //  )
  //)
  //if (cfg.myHaveAluFlags) {
  //  io.rAluFlags.setAsReg()
  //  io.rAluFlags.init(io.rAluFlags.getZero)
  //}

  io.multiCycleOpInfoIdx := 0x0
  val nextShouldIgnoreInstrState = Bool()
  val rShouldIgnoreInstrState = (
    RegNext(
      next=nextShouldIgnoreInstrState,
      init=nextShouldIgnoreInstrState.getZero,
    )
  )
  nextShouldIgnoreInstrState := rShouldIgnoreInstrState
  //--------
  when (io.shouldIgnoreInstr) {
    io.modMemWordValid := False
    io.modMemWord.foreach(modMemWord => {
      modMemWord := modMemWord.getZero
    })
  }
  when (!rShouldIgnoreInstrState) {
    io.shouldIgnoreInstr := False
    io.pcChangeState := False
    when (io.opIsJmp) {
      io.pcChangeState := True
      when (io.upIsFiring) {
        nextShouldIgnoreInstrState := True
      }
    }
  } otherwise {
    when (io.regPcSetItCnt =/= 0) {
      io.shouldIgnoreInstr := False
      when (io.opIsJmp) {
        //io.shouldIgnoreInstr := False
        io.pcChangeState := True
      } otherwise {
        when (io.upIsFiring) {
          io.pcChangeState := False
          nextShouldIgnoreInstrState := False
        }
      }
    } otherwise {
      io.pcChangeState := True
      io.shouldIgnoreInstr := True
    }
  }
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
  //--------
  val nextIndexReg = UInt(cfg.mainWidth bits)
  //val rIndexReg = (
  //  RegNext(
  //    ne
  //  )
  //)
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
    when (io.upIsFiring) {
      //when (!io.rHadRetIra && nextHadRetIra) {
      //  io.rHadRetIra := True
      //}
      io.rHadRetIra := nextHadRetIra
    }
  }
  //--------
  io.psExSetPc.nextPc.allowOverride
  io.psExSetPc.nextPc := (
    io.regPcPlusImm
  )
  io.dbusHostPayload.data := io.rdMemWord(0) //selRdMemWord(0)
  // TODO: support other kinds of addressing...
  io.dbusHostPayload.addr := io.rdMemWord(1) + io.imm
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
    //is (U(s"${io.currOp.getWidth}'d${opInfoIdx}")) {
      opInfo.select match {
        case OpSelect.Cpy => {
          opInfo.cpyOp.get match {
            case CpyOpKind.Cpy => {
              nextIndexReg := 0x0
              io.opIsCpyNonJmpAlu := True
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
                        //case SprKind.Modhi => {
                        //}
                        //case SprKind.Modlo => {
                        //}
                        case _ => {
                          assert(
                            false,
                            s"not yet implemented: ${kind}"
                          )
                        }
                        //case SprKind.Ie => {
                        //}
                        //case SprKind.IndexReg => {
                        //}
                      }
                    }
                    case DstKind.HiddenReg(kind) => {
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
                  io.opIsMemAccess := True
                  //assert(
                  //  opInfo.dstArr.size == 1,
                  //  s"invalid opInfo.dstArr.size: "
                  //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  //)
                  //assert(
                  //  mem.isSigned == None,
                  //  s"not yet implemented: "
                  //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  //)
                  //--------
                  //if (!mem.isPush) {
                    io.modMemWordValid := False
                    io.modMemWord(0) := 0x0
                  //}
                  //--------
                  //else {
                  //  //io.modMemWordValid := True
                  //  io.modMemWord(0) := (
                  //  )
                  //}
                  if (!mem.isAtomic) {
                    val isStore = mem.isStore
                    val tempSubKind = (
                      mem.subKind match {
                        case MemAccessKind.SubKind.Sz8 => {
                          SnowHouseMemAccessSubKind.Sz8
                        }
                        case MemAccessKind.SubKind.Sz16 => {
                          SnowHouseMemAccessSubKind.Sz16
                        }
                        case MemAccessKind.SubKind.Sz32 => {
                          SnowHouseMemAccessSubKind.Sz32
                        }
                        case MemAccessKind.SubKind.Sz64 => {
                          SnowHouseMemAccessSubKind.Sz64
                        }
                      }
                    )
                    io.dbusHostPayload.subKind := (
                      //DbusHostMemAccessSubKind.fromWordSize(cfg=cfg)
                      //mem.subKind match {
                      //  case MemAccessKind.SubKind.Sz8 => {
                      //    SnowHouseMemAccessSubKind.Sz8
                      //  }
                      //  case MemAccessKind.SubKind.Sz16 => {
                      //    SnowHouseMemAccessSubKind.Sz16
                      //  }
                      //  case MemAccessKind.SubKind.Sz32 => {
                      //    SnowHouseMemAccessSubKind.Sz32
                      //  }
                      //  case MemAccessKind.SubKind.Sz64 => {
                      //    SnowHouseMemAccessSubKind.Sz64
                      //  }
                      //}
                      tempSubKind
                    )
                    io.decodeExt.memAccessSubKind := (
                      tempSubKind
                    )
                    //--------
                    //if (!mem.isPush) {
                      io.decodeExt.memAccessIsPush := False
                      //io.outpWrMemAddr := io.inpPushMemAddr(
                      //  PipeMemRmw.modWrIdx
                      //)
                    //} else {
                    //  io.decodeExt.memAccessIsPush := True
                    //  //io.outpWrMemAddr := io.inpPushMemAddr(
                    //  //  PipeMemRmw.modRdIdxStart
                    //  //)
                    //  io.modMemWordValid := True
                    //  io.modMemWord(0) := (
                    //    selRdMemWord(1) - (cfg.mainWidth / 8)
                    //  )
                    //  io.outpWrMemAddr := (
                    //    io.inpPushMemAddr(PipeMemRmw.modRdIdxStart)
                    //  )
                    //}
                    //--------
                    //opInfo.addrCalc match {
                    //  case AddrCalcKind.AddReduce(true) => (
                    //    //when (io.upIsFiring) {
                    //    //rIndexReg := 0x0
                    //    nextIndexReg := 0x0
                    //    //}
                    //  )
                    //  case _ => {
                    //  }
                    //}
                    //--------
                    //val tempAddr = (
                    //  (
                    //    opInfo.addrCalc match {
                    //      case AddrCalcKind.AddReduce(
                    //        //fromIndexReg
                    //      ) => (
                    //        //if (!fromIndexReg) (
                    //          selRdMemWord(1)
                    //        //) else (
                    //        //  io.rIndexReg
                    //        //)
                    //      )
                    //      case kind:
                    //      AddrCalcKind.LslThenMaybeAdd => (
                    //        selRdMemWord(1)
                    //        << kind.options.lslAmount.get
                    //      )
                    //      //case _ => {
                    //      //  selRdMemWord(1)
                    //      //}
                    //    }
                    //  ) 
                    //  //+ (
                    //  //  opInfo.srcArr.size match {
                    //  //    case 1 => {
                    //  //      U(s"${cfg.mainWidth}'d0")
                    //  //    }
                    //  //    case 2 => {
                    //  //      selRdMemWord(2)
                    //  //    }
                    //  //    case _ => {
                    //  //      assert(
                    //  //        false,
                    //  //        s"invalid opInfo.srcArr.size: "
                    //  //        + s"opInfo(${opInfo}) "
                    //  //        + s"index:${opInfoIdx}"
                    //  //      )
                    //  //      U("s${cfg.mainWidth}'d0")
                    //  //    }
                    //  //  }
                    //  //)
                    //)
                    //--------
                    //io.dbusHostPayload.addr := (
                    //  opInfo.srcArr.size match {
                    //    case 1 => (
                    //      //U(s"${cfg.mainWidth}'d0")
                    //      tempAddr
                    //    )
                    //    case 2 => (
                    //      tempAddr + selRdMemWord(2)
                    //    )
                    //    case _ => {
                    //      assert(
                    //        false,
                    //        s"invalid opInfo.srcArr.size: "
                    //        + s"opInfo(${opInfo}) "
                    //        + s"index:${opInfoIdx}"
                    //      )
                    //      U(s"${cfg.mainWidth}'d0")
                    //    }
                    //  }
                    //)
                    //--------
                    //io.dbusHostPayload.subKind := (
                    //  tempSubKind
                    //)
                    if (!isStore) {
                      //io.decodeExt.memAccessKind := (
                      //  //io.decodeExt._memAccessLdStKindLoad
                      //)
                      val tempMemAccessKind = (
                        if (!mem.isSigned) (
                          SnowHouseMemAccessKind.LoadU
                        ) else (
                          SnowHouseMemAccessKind.LoadS
                        )
                      )
                      io.decodeExt.memAccessKind := (
                        tempMemAccessKind
                      )
                      io.dbusHostPayload.accKind := (
                        tempMemAccessKind
                      )
                      io.dbusHostPayload.data := (
                        io.dbusHostPayload.data.getZero
                      )
                    } else { // if (isStore)
                      val tempMemAccessKind = (
                        SnowHouseMemAccessKind.Store
                      )
                      io.decodeExt.memAccessKind := (
                        //io.decodeExt._memAccessLdStKindStore
                        tempMemAccessKind
                      )
                      io.dbusHostPayload.accKind := (
                        tempMemAccessKind
                      )
                      //io.dbusHostPayload.data := selRdMemWord(0)
                    }
                  } else {
                    assert(
                      false,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                  }
                  //case None => {
                  //  assert(
                  //    false,
                  //    s"not yet implemented: "
                  //    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  //  )
                  //}
                }
              }
            }
            case CpyOpKind.Cpyu => {
              nextIndexReg := 0x0
              io.opIsCpyNonJmpAlu := True
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
                //opInfo.addrCalc == AddrCalcKind.AddReduce(_),
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
              //io.rIndexReg := 0x0
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
                //opInfo.addrCalc == AddrCalcKind.AddReduce(),
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
              when (!io.takeIrq) {
                if (opInfo.dstArr.size == 1) (
                  io.modMemWordValid := False
                ) else if (
                  opInfo.dstArr(1) == DstKind.Spr(SprKind.Ie)
                ) {
                  io.modMemWordValid := False
                } else {
                  //io.modMemWordValid := True
                  // TODO: *maybe* support more outputs
                  io.modMemWordValid := !io.gprIsZeroVec(0)
                  //io.shouldIgnoreInstr := io.modMemWordValid
                }
              } otherwise {
                io.modMemWordValid := False
              }
              io.modMemWord(0) := (
                //io.regPcPlusInstrSize
                //io.regPc + ((cfg.instrMainWidth / 8) * 2)

                io.regPc + ((cfg.instrMainWidth / 8) * 1)

                //io.regPc //+ ((cfg.instrMainWidth / 8) * 1/*2*/)
              )
              io.psExSetPc.valid := True
              when (
                //!io.takeIrq
                !io.shouldIgnoreInstr
              ) {
                opInfo.srcArr(0) match {
                  case SrcKind.Gpr => {
                    io.psExSetPc.nextPc := (
                      io.rdMemWord(io.jmpAddrIdx)
                      //- ((cfg.instrMainWidth / 8) * 1/*2*/)
                    )
                  }
                  case SrcKind.Spr(SprKind.Ira) => {
                    io.psExSetPc.nextPc := (
                      io.rIra
                      //- ((cfg.instrMainWidth / 8) * 1/*2*/)
                    )
                    assert(
                      opInfo.dstArr.size == 2,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    if (opInfo.dstArr(1) == DstKind.Ie) {
                      nextHadRetIra := True
                    }
                    //nextIe := 0x1
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
              //otherwise {
              //  nextIra := io.regPc
              //  //nextIe := 0x0
              //  io.psExSetPc.nextPc := (
              //    io.rIds
              //  )
              //}
            }
            case CpyOpKind.Br => {
              if (opInfo.dstArr.size == 1) (
                io.modMemWordValid := False
              )
              //io.rIndexReg := 0x0
              nextIndexReg := 0x0
              opInfo.cond match {
                case CondKind.Always => {
                  //io.opIsJmp := True
                  //assert(
                  //  opInfo.dstArr.size == 1,
                  //  s"not yet implemented: "
                  //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  //)
                  //assert(
                  //  opInfo.srcArr.size == 1,
                  //  s"not yet implemented: "
                  //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  //)
                  //io.modMemWordValid := False
                  io.psExSetPc.valid := True
                  //io.psExSetPc.nextPc := io.regPcPlusImm

                  io.modMemWord(0) := (
                    //io.regPcPlusInstrSize
                    io.regPc + ((cfg.instrMainWidth / 8) * 1)
                  )
                  //io.modMemWordValid := True
                  if (opInfo.dstArr.size == 1) (
                    io.modMemWordValid := False
                  ) else {
                    io.modMemWordValid := (
                      // TODO: support more outputs
                      !io.gprIsZeroVec(0)
                      //True
                    )
                    //io.shouldIgnoreInstr := io.modMemWordValid
                  }
                }
                //case CondKind.Link => {
                //  io.opIsJmp := True
                //  assert(
                //    opInfo.dstArr.size == 2,
                //  )
                //}
                case CondKind.Eq => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      io.rFlagZ
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0))
                      === io.rdMemWord(io.brCondIdx(1))
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Ne => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      !io.rFlagZ
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0))
                      =/= io.rdMemWord(io.brCondIdx(1))
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Mi => {
                  io.psExSetPc.valid := (
                    io.rFlagN
                  )
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Pl => {
                  io.psExSetPc.valid := (
                    !io.rFlagN
                  )
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Vs => {
                  io.psExSetPc.valid := (
                    io.rFlagV
                  )
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Vc => {
                  io.psExSetPc.valid := (
                    !io.rFlagV
                  )
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Geu => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      io.rFlagC
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0))
                      >= io.rdMemWord(io.brCondIdx(1))
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Ltu => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      !io.rFlagC
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0))
                      < io.rdMemWord(io.brCondIdx(1))
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Gtu => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      //io.rFlagC && !io.rFlagZ
                      io.rFlagC && !io.rFlagZ
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0))
                      > io.rdMemWord(io.brCondIdx(1))
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Leu => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      //!io.rFlagC || io.rFlagZ
                      !io.rFlagC || io.rFlagZ
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0))
                      <= io.rdMemWord(io.brCondIdx(1))
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Ges => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      !(io.rFlagN ^ io.rFlagV)
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0)).asSInt
                      >= io.rdMemWord(io.brCondIdx(1)).asSInt
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Lts => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      (io.rFlagN ^ io.rFlagV)
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0)).asSInt
                      < io.rdMemWord(io.brCondIdx(1)).asSInt
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Gts => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      //io.rFlagC && !io.rFlagZ
                      (!(io.rFlagN ^ io.rFlagV)) & !io.rFlagZ
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0)).asSInt
                      > io.rdMemWord(io.brCondIdx(1)).asSInt
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Les => {
                  //io.modMemWordValid := False
                  if (
                    opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                    && opInfo.srcArr(1) == SrcKind.Imm()
                  ) {
                    io.psExSetPc.valid := (
                      (io.rFlagN ^ io.rFlagV) | io.rFlagZ
                    )
                  } else {
                    assert(
                      opInfo.srcArr(0) == SrcKind.Gpr
                      && opInfo.srcArr(1) == SrcKind.Gpr,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := (
                      io.rdMemWord(io.brCondIdx(0)).asSInt
                      <= io.rdMemWord(io.brCondIdx(1)).asSInt
                    )
                  }
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Z => {
                  //assert(
                  //  opInfo.dstArr.size == 1,
                  //  s"not yet implemented: "
                  //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  //)
                  //assert(
                  //  opInfo.srcArr.size == 1,
                  //  s"not yet implemented: "
                  //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  //)
                  //io.modMemWordValid := False
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  io.psExSetPc.valid := (
                    io.rdMemWord(io.brCondIdx(0)) === 0
                  )
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
                }
                case CondKind.Nz => {
                  //assert(
                  //  opInfo.dstArr.size == 1,
                  //  s"not yet implemented: "
                  //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  //)
                  //assert(
                  //  opInfo.srcArr.size == 1,
                  //  s"not yet implemented: "
                  //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  //)
                  //io.modMemWordValid := False
                  assert(
                    opInfo.srcArr(0) == SrcKind.Gpr,
                    s"not yet implemented: "
                    + s"opInfo(${opInfo}) index:${opInfoIdx}"
                  )
                  io.psExSetPc.valid := (
                    io.rdMemWord(io.brCondIdx(0)) =/= 0
                  )
                  //io.psExSetPc.nextPc := (
                  //  io.regPcPlusImm
                  //)
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
          io.opIsCpyNonJmpAlu := True
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
            //opInfo.addrCalc == AddrCalcKind.AddReduce(),
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
                case DstKind.Spr(SprKind.AluFlags) => {
                  nextFlagN := myBinop.flagN
                  nextFlagV := myBinop.flagV
                  nextFlagC := myBinop.flagC
                  nextFlagZ := myBinop.flagZ
                  //io.rIndexReg := 0x0
                  nextIndexReg := 0x0
                }
                case DstKind.HiddenReg(HiddenRegKind.IndexReg) => {
                  //io.rIndexReg := myBinop.main
                  nextIndexReg := myBinop.main
                }
                case _ => {
                  //io.rIndexReg := 0x0
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
              //io.modMemWord(0) := (
              //  selRdMemWord(1) - selRdMemWord(2)
              //)
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
              //when (io.upIsFiring) {
              //io.rIndexReg := 0x0
              nextIndexReg := 0x0
              //}
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
              //io.rIndexReg := 0x0
              nextIndexReg := 0x0
            }
          }
        }
        case OpSelect.MultiCycle => {
          //io.rIndexReg := 0x0
          nextIndexReg := 0x0
          for (
            ((_, innerOpInfo), idx)
            <- cfg.multiCycleOpInfoMap.view.zipWithIndex
          ) {
            //cfg.multiCycleOpInfoMap.find(_._2 == opInfo).get
            if (opInfo == innerOpInfo) {
              io.multiCycleOpInfoIdx := idx
              for ((dst, dstIdx) <- opInfo.dstArr.view.zipWithIndex) {
                val tempDst = (
                  modIo.multiCycleBusVec(idx).devData.dstVec(dstIdx)
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
              //io.modMemWord(0) := (
              //  modIo.multiCycleBusVec(idx).devData.dstVec(0)
              //)
            }
          }
          io.opIsMultiCycle := True
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
            //opInfo.addrCalc == AddrCalcKind.AddReduce(),
            s"not yet implemented: "
            + s"opInfo(${opInfo}) index:${opInfoIdx}"
          )
          //opInfo.multiCycleOp.get match {
          //  case MultiCycleOpKind.Umul => {
          //    //assert(
          //    //  opInfo.dstArr.size == 1,
          //    //  s"not yet implemented: "
          //    //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
          //    //)
          //    //assert(
          //    //  opInfo.srcArr.size == 2,
          //    //  s"not yet implemented: "
          //    //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
          //    //)
          //    io.modMemWord(0) := (
          //      (selRdMemWord(1) * selRdMemWord(2))(
          //        io.modMemWord(0).bitsRange
          //      )
          //    )
          //  }
          //  case _ => {
          //    assert(
          //      false,
          //      s"not yet implemented"
          //    )
          //  }
          //}
        }
      }
    //}
  }
  switch (io.splitOp.kind) {
    is (SnowHouseSplitOpKind.CPY_CPYUI) {
      switch (io.splitOp.cpyCpyuiOp) {
        for (
          ((_, opInfo), idx) <- cfg.cpyCpyuiOpInfoMap.view.zipWithIndex
        ) {
          is (idx) {
            innerFunc(
              opInfo=opInfo,
              opInfoIdx=idx,
            )
          }
        }
      }
    }
    is (SnowHouseSplitOpKind.JMP_BR) {
      switch (io.splitOp.jmpBrOp) {
        for (
          ((_, opInfo), idx) <- cfg.jmpBrOpInfoMap.view.zipWithIndex
        ) {
          is (idx) {
            innerFunc(
              opInfo=opInfo,
              opInfoIdx=idx,
            )
          }
        }
      }
    }
    //is (SnowHouseSplitOpKind.PURE_BR) {
    //  switch (io.splitOp.pureBrOp) {
    //    for (
    //      ((_, opInfo), idx) <- cfg.pureBrOpInfoMap.view.zipWithIndex
    //    ) {
    //      is (idx) {
    //        innerFunc(
    //          opInfo=opInfo,
    //          opInfoIdx=idx,
    //        )
    //      }
    //    }
    //  }
    //}
    is (SnowHouseSplitOpKind.ALU) {
      switch (io.splitOp.aluOp) {
        for (
          ((_, opInfo), idx) <- cfg.aluOpInfoMap.view.zipWithIndex
        ) {
          is (idx) {
            innerFunc(
              opInfo=opInfo,
              opInfoIdx=idx,
            )
          }
        }
      }
    }
    is (SnowHouseSplitOpKind.MULTI_CYCLE) {
      switch (io.splitOp.multiCycleOp) {
        for (
          ((_, opInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
        ) {
          is (idx) {
            innerFunc(
              opInfo=opInfo,
              opInfoIdx=idx,
            )
          }
        }
      }
    }
    //is (SnowHouseSplitOpKind.PURE_CPYUI) 
    //default {
    //  switch (io.splitOp.pureCpyuiOp) {
    //    for (
    //      ((_, opInfo), idx) <- cfg.pureCpyOpInfoMap.view.zipWithIndex
    //    ) {
    //      is (idx) {
    //        innerFunc(
    //          opInfo=opInfo,
    //          opInfoIdx=idx,
    //        )
    //      }
    //    }
    //  }
    //}
  }
  when (io.takeIrq) {
    nextIra := io.regPc
    nextIe/*(0)*/ := False //0x0
    io.psExSetPc.nextPc := (
      io.rIds
    )
  }
  //when (!io.takeIrq) {
  //} otherwise { // when (io.takeIrq)
  //  io.psExSetPc.valid := True
  //  io.psExSetPc.nextPc := io.rIds
  //  nextIra := io.regPc
  //  nextIe := 0x0
  //  //io.rIra :=
  //}
  //when (io.upIsFiring) {
  //  when (io.takeIrq) {
  //    when (io.rIe =/= 0x0) {
  //      //io.psExSetPc.valid := True
  //      //io.psExSetPc.nextPc := io.rIds
  //      nextIra := io.regPc
  //      nextIe := 0x0
  //    }
  //  }
  //}
//  }
  //  is (PcChangeState.WaitTwoInstrs) {
  //    io.modMemWordValid := False
  //    io.modMemWord.foreach(modMemWord => {
  //      modMemWord := modMemWord.getZero
  //    })
  //  }
  //}
  //}
}

case class SnowHousePipeStageExecute(
  //cfg: SnowHouseConfig,
  //io: SnowHouseIo,
  args: SnowHousePipeStageArgs,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
  psMemStallHost: LcvStallHost[
    DbusHostPayload,
    DbusDevPayload,
  ],
  doModInModFrontParams: PipeMemRmwDoModInModFrontFuncParams[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeMemRmwDualRdTypeDisabled[UInt, Bool],
  ],
  pcChangeState: Bool,
  shouldIgnoreInstr: Bool,
) extends Area {
  //--------
  def cfg = args.cfg
  def io = args.io
  //--------
  def nextPrevTxnWasHazard = (
    doModInModFrontParams.nextPrevTxnWasHazardVec(0)
  )
  def rPrevTxnWasHazard = (
    doModInModFrontParams.rPrevTxnWasHazardVec(0)
  )
  def rPrevTxnWasHazardAny = (
    doModInModFrontParams.rPrevTxnWasHazardAny
  )
  def outp = doModInModFrontParams.outp//Vec(ydx)
  def inp = doModInModFrontParams.inp//Vec(ydx)
  def cMid0Front = doModInModFrontParams.cMid0Front
  def tempModFrontPayload = (
    doModInModFrontParams.tempModFrontPayload//Vec(ydxr
  )
  //def regFile = args.regFile
  //assume(
  //  inp.op
  //  //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
  //)
  //assume(
  //  outp.op.asBits.asUInt
  //  //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
  //  < PipeMemRmwSimDut.postMaxModOp
  //)
  //--------
  //val currDuplicateIt = Bool()
  //val myIrqOp = /*cloneOf(inp.op)*/ UInt(inp.op.getWidth bits)
  //myIrqOp := (
  //  RegNext(
  //    next=myIrqOp,
  //    init=U(s"${myIrqOp.getWidth}'d0")
  //  )
  //)
  if (cfg.optFormal) {
    if ((1 << outp.op.getWidth) != cfg.opInfoMap.size) {
      assume(inp.op < cfg.opInfoMap.size)
      assume(outp.op < cfg.opInfoMap.size)
      //assume(myIrqOp < cfg.opInfoMap.size)
    }
  }
  //--------
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
  //val psExStallHost = (
  //  //PipeMemRmwSimDut.haveModOpMul
  //  havePsExStall
  //) generate (
  //  cfg.mkLcvStallHost[
  //    MultiCycleHostPayload,
  //    MultiCycleDevPayload,
  //  ](
  //    stallIo=(
  //      //io.psExStallIo
  //      // TODO: support multiple external `MultiCycleOp`
  //      if (!io.haveMultiCycleBusVec) {
  //        None
  //      } else { // if (io.haveMultiCycleBusVec)
  //        Some(io.multiCycleBusVec(0))
  //      }
  //    ),
  //  )
  //)
  val psExStallHostArr = ArrayBuffer[LcvStallHost[
    MultiCycleHostPayload, MultiCycleDevPayload
  ]]()
  for (
    ((_, opInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  ) {
    psExStallHostArr += (
      cfg.mkLcvStallHost[MultiCycleHostPayload, MultiCycleDevPayload](
        stallIo=(
          //io.psExStallIo
          // TODO: support multiple external `MultiCycleOp`
          //if (!io.haveMultiCycleBusVec) {
          //  None
          //} else { // if (io.haveMultiCycleBusVec)
            Some(io.multiCycleBusVec(idx))
          //}
        ),
      )
    )
  }
  //--------
  //def psExStallHost(
  //  opInfo: OpInfo
  //): (LcvStallHost[MultiCycleHostPayload, MultiCycleDevPayload], Int) = {
  //  for (
  //    ((_, innerOpInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  //  ) {
  //    //cfg.multiCycleOpInfoMap.find(_._2 == opInfo).get
  //    if (opInfo == innerOpInfo) {
  //      return (psExStallHostArr(idx), idx)
  //    }
  //  }
  //  assert(
  //    false,
  //    s"eek! ${opInfo.select}"
  //  )
  //  return (null, -1)
  //}
  //--------
  //val psMemStallHost = (
  //  mkLcvStallHost(
  //    stallIo=(
  //      Some(io.dbus)
  //    ),
  //  )
  //)
  //--------
  val nextSetOutpState = (
    Bool()
  )
  val rSetOutpState = (
    RegNext(
      next=nextSetOutpState,
      init=nextSetOutpState.getZero,
    )
  )
  nextSetOutpState := rSetOutpState
  outp := (
    RegNext(
      next=outp,
      init=outp.getZero,
    )
  )
  outp.allowOverride
  val tempExt = (
    cloneOf(outp.myExt)
  )
  tempExt := (
    RegNext(
      next=tempExt,
      init=tempExt.getZero,
    )
  )
  def myRdMemWord(
    ydx: Int,
    modIdx: Int,
  ) = (
    doModInModFrontParams.getMyRdMemWordFunc(ydx, modIdx)
  )
  when (cMid0Front.up.isValid ) {
    when (!rSetOutpState) {
      outp := inp
      tempExt := inp.myExt
      //myIrqOp := cfg.irqJmpOp //inp.irqJmpOp
      nextSetOutpState := True
    }
    when (cMid0Front.up.isFiring) {
      nextSetOutpState := False
    }
  }
  if (cfg.optFormal) {
    when (pastValidAfterReset) {
      when (past(cMid0Front.up.isFiring) init(False)) {
        assert(!rSetOutpState)
      }
      when (!(past(cMid0Front.up.isValid) init(False))) {
        assert(stable(rSetOutpState))
      }
      when (rSetOutpState) {
        assert(cMid0Front.up.isValid)
      }
    }
  }
  if (cfg.optFormal) {
    //assert(myIrqOp === outp.op)
    when (pastValidAfterReset) {
      when (rose(rSetOutpState)) {
        //assert(myIrqOp === past(inp.op))
        assert(outp.op === past(inp.op))
        assert(outp.opCnt === past(inp.opCnt))
      }
    }
  }
  for (ydx <- 0 until outp.myExt.size) {
    outp.myExt(ydx).rdMemWord := (
      inp.myExt(ydx).rdMemWord
    )
  }
  //val doTestModOpMainArea = new Area {
  //--------
  //val savedPsExStallHost = (
  //  LcvStallHostSaved(
  //    stallHost=psExStallHost,
  //    someLink=cMid0Front,
  //  )
  //)
  val savedPsMemStallHost = (
    LcvStallHostSaved(
      stallHost=psMemStallHost,
      someLink=cMid0Front,
    )
  )
  //--------
  def stallKindMem = 0
  def stallKindMultiCycle = 1
  def stallKindLim = 2

  //val currDuplicateIt = (
  //  KeepAttribute(
  //    Vec.fill(2)(
  //      Bool()
  //    )
  //  )
  //)
  //currDuplicateIt.foreach(current => {
  //  current := False
  //})
  val myDoStall = (
    KeepAttribute(
      Vec.fill(stallKindLim)(
        Bool()
      )
    )
  )
  //myDoStall.foreach(item => {
  //  item := False
  //})
  myDoStall(stallKindMem) := False
  myDoStall(stallKindMultiCycle) := (
    RegNext(
      next=myDoStall(stallKindMultiCycle),
      init=myDoStall(stallKindMultiCycle).getZero,
    )
  )
  //--------
  val doCheckHazard = (
    Bool()
  )
  //doCheckHazard := (
  //  RegNext(
  //    next=doCheckHazard,
  //    init=doCheckHazard.getZero,
  //  )
  //)
  val myDoHaveHazardAddrCheckVec = Vec[Bool](
    {
      //val temp = ArrayBuffer[Vec[Bool]]()
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
      //temp += tempArr.reduceLeft(_ || _)
      // TODO: support multiple register writes per instruction
      temp += (
        outp.myDoHaveHazardAddrCheckVec(0)
      )

      //for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      //  //println(s"begin: ${ydx} ${temp.size}")
      //  temp += {
      //    val tempArr = ArrayBuffer[Bool]()
      //    for (
      //      zdx 
      //      <- 0 until cfg.regFileModRdPortCnt
      //      //outp.myExt(ydx).memAddr.size
      //    ) {
      //      assert(
      //        outp.myExt(ydx).memAddr.size
      //        == cfg.regFileModRdPortCnt
      //      )
      //      tempArr += (
      //        outp.myExt(ydx).memAddr(zdx)
      //        === tempModFrontPayload.myExt(ydx).memAddr(zdx)
      //      )
      //    }
      //    //toReduce.reduce(_ || _)
      //    //toFold.foldLeft(False)((left, right) => (left || right))
      //    //toFold.sFindFirst(_ === True)._1
      //    tempArr.reduceLeft(_ || _)
      //  }
      //  //println(s"end: ${ydx} ${temp.size}")
      //}
      temp
    },
    Bool()
  )
  val myDoHaveHazardValidCheckVec = Vec[Bool](
    {
      //(
      //  !tempModFrontPayload.myExt(0).modMemWordValid
      //)
      val temp = ArrayBuffer[Bool]()
      for (
        ydx
        <- 0 until cfg.regFileCfg.memArrSize
        //tempModFrontPayload.myExt.size
      ) {
        //println(s"begin: ${ydx} ${temp.size}")
        temp += (
          //tempModFrontPayload.decodeExt.memAccessIsPush
          //||
          !tempModFrontPayload.myExt(ydx).modMemWordValid
        )
        //println(s"end: ${ydx} ${temp.size}")
      }
      temp
    },
    Bool()
  )
  //--------
  //val myDoHaveHazardV2d = Vec[Vec[Bool]]{
  //  val temp = ArrayBuffer[Vec[Bool]]()
  //  for (ydx <- 0 until myDoHaveHazardValidCheck.size) {
  //    temp += {
  //      val innerTemp = ArrayBuffer[Bool]()
  //      //temp += (
  //      //  myDoHaveHazardAddrCheck
  //      //  && myDoHaveHazardValidCheck(ydx)
  //      //)
  //      for (zdx <- 0 until cfg.regFileModRdPortCnt) {
  //        innerTemp += (
  //          myDoHaveHazardAddrCheck(ydx)(zdx)
  //          && myDoHaveHazardValidCheck(ydx)
  //        )
  //      }
  //      Vec[Bool]{innerTemp}
  //    }
  //  }
  //  temp
  //}
  val myDoHaveHazardVec = KeepAttribute(
    Vec[Bool]{
      //val tempFindFirst = Vec[(Bool, UInt)]
      //val tempFindFirst_1 = Bool()
      //val tempFindFirst_2: UInt = null
      //tempFindFirst_1
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
  val myDoHaveHazard = KeepAttribute(
    //myDoHaveHazardVec.reduce(_ || _)
    //myDoHaveHazardVec.foldLeft(False)((left, right) => (left || right))
    //myDoHaveHazardVec.sFindFirst(_ === True)._1
    myDoHaveHazardVec.reduceLeft(_ || _)
  )
  //val rTempPrevOp = (
  //  RegNextWhen(
  //    next=myCurrOp,
  //    cond=cMid0Front.up.isFiring,
  //    init=U(s"${myCurrOp.getWidth}'d0")
  //  )
  //)
  val setOutpModMemWord = SnowHousePipeStageExecuteSetOutpModMemWord(
    //cfg=cfg
    args=args
  )
  setOutpModMemWord.io.takeIrq := (
    RegNext(
      next=setOutpModMemWord.io.takeIrq,
      init=setOutpModMemWord.io.takeIrq.getZero,
    )
  )
  //case class PrevCurrOpPayload(
  //) extends Bundle {
  //  val currOp = UInt(log2Up(cfg.opInfoMap.size) bits)
  //}
  val rIrqHndlState = {
    val temp = Reg(
      //Flow(PrevCurrOpPayload())
      Bool()
    )
    temp.init(temp.getZero)
    temp
  }
  if (cfg.irqCfg != None) {
    when (io.idsIraIrq.rValid) {
      setOutpModMemWord.io.takeIrq := (
        cMid0Front.up.isValid
        && outp.takeIrq
        //&& myDoStall.sFindFirst(_ === True)._1
        //&& cMid0Front.up.isReady
        //&& (setOutpModMemWord.io.rIe(0 downto 0) =/= 0x0)
        && (
          RegNextWhen(
            next=(setOutpModMemWord.io.rIe/*(0)*/ === True),//0x0
            cond=cMid0Front.up.isFiring,
            init=False,
          )
        ) && (
          !rIrqHndlState//.valid
          //&& rPrevCurrOp.currOp =/= cfg.irqRetIraOp
          //True
        ) && (
          if (setOutpModMemWord.io.haveRetIraState) (
            !setOutpModMemWord.io.rHadRetIra
          ) else (
            True
          )
          //setOutpModMemWord.nextIe =/= 0x0
          //!pcChangeState
          //True
        )
        //&& (
        //  !outp.instrCnt.shouldIgnoreInstr
        //) && (
        //  //!myDoStall(stallKindMultiCycle)
        //  //True
        //  cMid0Front.down.isReady
        //)
        //&& (
        //  cMid0Front.up.isFiring
        //)
        //&& (
        //  RegNextWhen(
        //    next=(setOutpModMemWord.io.rIe =/= 0x0),
        //    cond=cMid0Front.up.isFiring,
        //    init=False,
        //  )
        //)
      )
    }
  }
  val nextTempIrqCond = (
    cfg.irqCfg != None
  ) generate (
    cMid0Front.up.isFiring
    && setOutpModMemWord.io.takeIrq
    //&& !rIrqHndlState//.fire
    //&& !pcChangeState
    //&& setOutpModMemWord.nextIe =/= 0x0 
    //&& setOutpModMemWord.io.rIe =/= 0
  )
  if (
    cfg.irqCfg != None
  ) {
    when (
      nextTempIrqCond
    ) {
      rIrqHndlState/*.valid*/ := True
    }
    when (rIrqHndlState) {
      setOutpModMemWord.io.takeIrq := (
        False
        //rIrqHndlState
      )
    }
    io.idsIraIrq.ready := False
    when (io.idsIraIrq.rValid) {
      when (setOutpModMemWord.io.takeIrq) {
        when (cMid0Front.up.isFiring) {
          io.idsIraIrq.ready := True
        }
      }
    }
  }
  val reEnableIrqsCond = (
    cfg.irqCfg != None
  ) generate (
    cMid0Front.up.isFiring
    && rIrqHndlState//.fire
    //--------
    //&& (
    //  !pcChangeState
    //  || (
    //    pcChangeState
    //    && outp.regPcSetItCnt =/= 0x0
    //  )
    //)
    ////--------
    //&& !outp.instrCnt.shouldIgnoreInstr
    //&& setOutpModMemWord.io.Ie === 0
    //&& setOutpModMemWord.nextIe =/= 0x0 
    //&& setOutpModMemWord.nextHadRetIra
    && (
      if (setOutpModMemWord.io.haveRetIraState) (
        setOutpModMemWord.io.rHadRetIra
      ) else (
        True
      )
    ) && (
      RegNextWhen(
        next=(setOutpModMemWord.io.rIe/*(0)*/ === False),//0x0
        cond=cMid0Front.up.isFiring,
        init=False,
      )
    )
  )
  if (cfg.irqCfg != None) {
    //when (
    //  reEnableIrqsCond
    //  //&& RegNextWhen(
    //  //  next=setOutpModMemWord.io.rIe =/= 0,
    //  //  cond=(
    //  //    //reEnableIrqsCond
    //  //    cMid0Front.up.isFiring
    //  //    && rPrevCurrOp.fire
    //  //  ),
    //  //  init=False,
    //  //)
    //) {
    //  rIrqHndlState/*.valid*/ := False
    //  if (setOutpModMemWord.io.haveRetIraState) {
    //    setOutpModMemWord.io.rHadRetIra := False
    //  }
    //}
    //when (
    //  //cMid0Front.up.isFiring
    //  reEnableIrqsCond
    //  //&& RegNextWhen(
    //  //  next=reEnableIrqsCond, 
    //  //  cond=cMid0Front.up.isFiring,
    //  //  init=False,
    //  //)
    //) {
    //  //setOutpModMemWord.nextIe(0) := True//0x1
    //  rIrqHndlState/*.valid*/ := False
    //  if (setOutpModMemWord.io.haveRetIraState) {
    //    setOutpModMemWord.io.rHadRetIra := False
    //  }
    //}
    when (
      //cMid0Front.up.isFiring
      //&& RegNextWhen(
      //  next=reEnableIrqsCond, 
      //  cond=cMid0Front.up.isFiring,
      //  init=False,
      //)
      reEnableIrqsCond
    ) {
      setOutpModMemWord.nextIe/*(0)*/ := True//0x1
      rIrqHndlState/*.valid*/ := False
      if (setOutpModMemWord.io.haveRetIraState) {
        //setOutpModMemWord.io.rHadRetIra := False
      }
    }
  }
  //when (
  //  //setOutpModMemWord.io.pcChangeState
  //  outp.instrCnt.shouldIgnoreInstr
  //) {
  //  setOutpModMemWord.io.takeIrq := (
  //    False
  //    //outp.takeIrq
  //    //&& myDoStall.sFindFirst(_ === True)._1
  //    //&& cMid0Front.up.isReady
  //    //&& (setOutpModMemWord.io.rIe =/= 0x0)
  //  )
  //}
  //setOutpModMemWord.io.currOp := (
  //  RegNext(
  //    next=setOutpModMemWord.io.currOp,
  //    init=setOutpModMemWord.io.currOp.getZero,
  //  )
  //)
  setOutpModMemWord.io.splitOp.kind.allowOverride
  setOutpModMemWord.io.splitOp.jmpBrOp.allowOverride
  setOutpModMemWord.io.splitOp := (
    RegNext(
      next=setOutpModMemWord.io.splitOp,
      init=setOutpModMemWord.io.splitOp.getZero,
    )
  )
  when (cMid0Front.up.isValid) {
    when (!setOutpModMemWord.io.takeIrq) {
      //setOutpModMemWord.io.currOp := outp.op
      setOutpModMemWord.io.splitOp := outp.splitOp
    } otherwise {
      //setOutpModMemWord.io.currOp := cfg.irqJmpOp //myIrqOp
      setOutpModMemWord.io.splitOp := setOutpModMemWord.io.splitOp.getZero
      //setOutpModMemWord.io.splitOp.pureJmpOp.valid := (
      //  True
      //  //setOutpModMemWord.io.splitOp.pureJmpOp.getZero
      //)
      setOutpModMemWord.io.splitOp.kind := (
        SnowHouseSplitOpKind.JMP_BR
        //setOutpModMemWord.io.splitOp.pureJmpOp.getZero
      )
      setOutpModMemWord.io.splitOp.jmpBrOp := {
        val temp = UInt(log2Up(cfg.jmpBrOpInfoMap.size) bits)
        //temp.allowOverride
        //temp := 0x0
        for (
          ((idx, pureJmpOpInfo), jmpOp)
          <- cfg.jmpBrOpInfoMap.view.zipWithIndex
        ) {
          if (idx == cfg.irqJmpOp) {
            temp := jmpOp
          }
        }
        temp
      }
    }
  }
  //when (
  //  psExSetPc.fire
  //) {
  //  when (cMid0Front.up.isFiring) {
  //    rPrevCurrOp.valid := True
  //  }
  //}
  //when (cMid0Front.up.isFiring) {
  //  when (
  //    //!outp.instrCnt.shouldIgnoreInstr
  //    //!pcChangeState
  //    //&& rPrevCurrOp =/= cfg.irqJmpOp
  //    !pcChangeState
  //    || (
  //      pcChangeState
  //      && (
  //        outp.regPcSetItCnt =/= 0
  //        || setOutpModMemWord.io.currOp === cfg.irqRetIraOp
  //      )
  //    )
  //  ) {
  //    when (cMid0Front.up.isFiring) {
  //      rPrevCurrOp.currOp := (
  //        setOutpModMemWord.io.currOp
  //      )
  //    }
  //  }
  //}
  //when (
  //  rPrevCurrOp.fire
  //  && (
  //    rPrevCurrOp.currOp === cfg.irqJmpOp
  //    || rPrevCurrOp.currOp === cfg.irqRetIraOp
  //  )
  //) {
  //  setOutpModMemWord.io.takeIrq := (
  //    False
  //  )
  //  when (cMid0Front.up.isFiring) {
  //    rPrevCurrOp.valid := False
  //  }
  //}

  setOutpModMemWord.io.regPcSetItCnt := outp.regPcSetItCnt
  setOutpModMemWord.io.regPc := outp.regPc
  setOutpModMemWord.io.regPcPlusInstrSize := outp.regPcPlusInstrSize
  setOutpModMemWord.io.regPcPlusImm := outp.regPcPlusImm
  setOutpModMemWord.io.imm := outp.imm
  outp.decodeExt := setOutpModMemWord.io.decodeExt
  //psExSetPc := setOutpModMemWord.io.psExSetPc
  //if (cfg.optFormal) {
    outp.psExSetPc := psExSetPc
  //}
  if (io.haveMultiCycleBusVec) {
    for (
      (multiCycleBus, busIdx) <- io.multiCycleBusVec.view.zipWithIndex
    ) {
      for (idx <- 0 until multiCycleBus.hostData.srcVec.size) {
        multiCycleBus.hostData.srcVec(idx) := (
          RegNext(
            next=multiCycleBus.hostData.srcVec(idx),
            init=multiCycleBus.hostData.srcVec(idx).getZero,
          )
          //0x0
          //setOutpModMemWord.io.selRdMemWord(
          //  opInfo=multiCycleBus.hostData.opInfo,
          //  idx=(idx + 1),
          //)
        )
      }
    }
  }
  if (cfg.myHaveZeroReg) {
    for ((gprIdx, idx) <- outp.gprIdxVec.view.zipWithIndex) {
      setOutpModMemWord.io.gprIsZeroVec(idx) := (
        //gprIdx === cfg.myZeroRegIdx
        outp.gprIsZeroVec(idx)
      )
    }
  }
  //setOutpModMemWord.io.doIt
  //}
  //--------
  //val myOutpModMemWordValid = (
  //  KeepAttribute(
  //    Bool()
  //  )
  //)
  //val myOutpModMemWord = (
  //  KeepAttribute(
  //    UInt(cfg.mainWidth bits)
  //  )
  //)
  //--------
  //setOutpModMemWord.io.rdMemWord(zdx) := (
  //)
  //--------
  //setOutpModMemWord.io.instrCnt := outp.instrCnt
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
        //when (setOutpModMemWord.io.decodeExt.memAccessIsPush) {
          //tempExt.memAddr(zdx) := (
          //  setOutpModMemWord.io.outpWrMemAddr
          //)
        //}
        tempExt.modMemWord := (
          // TODO: support multiple output `modMemWord`s
          setOutpModMemWord.io.modMemWord(0)
        )
        //when (!outp.gprIsZeroVec(zdx)) {
          tempExt.modMemWordValid := (
            setOutpModMemWord.io.modMemWordValid
          )
        //} otherwise {
        //  tempExt.modMemWordValid := False
        //}
      }
      //if (!cfg.optFormal) {
      //  outp.gprRdMemWordVec(zdx) := tempRdMemWord
      //}
    def tempRdMemWord = setOutpModMemWord.io.rdMemWord(zdx)
    //when (!outp.gprIsZeroVec(zdx)) {
      tempRdMemWord := myRdMemWord(ydx=ydx, modIdx=zdx)
    //} otherwise {
    //  tempRdMemWord := 0x0
    //}
    //tempRdMemWord := RegNext(
    //  next=tempRdMemWord,
    //  init=tempRdMemWord.getZero,
    //)
    //val rSetTempRdMemWordState = Reg(Bool(), init=False)
    //when (cMid0Front.up.isValid) {
    //  when (!rSetTempRdMemWordState) {
    //    when (cMid0Front.down.isReady) {
    //      rSetTempRdMemWordState := True
    //      tempRdMemWord := myRdMemWord(ydx=ydx, modIdx=zdx)
    //    }
    //  }
    //  when (cMid0Front.up.isFiring) {
    //    rSetTempRdMemWordState := False
    //  }
    //}
    // TODO (maybe): support multiple register writes per instruction
    //--------
    //if (
    //  zdx == PipeMemRmw.modWrIdx
    //  || zdx == PipeMemRmw.modRdIdxStart
    //) {
    //  //when (setOutpModMemWord.io.decodeExt.memAccessIsPush) {
    //    setOutpModMemWord.io.inpPushMemAddr(zdx) := (
    //      tempExt(ydx).memAddr(zdx)
    //    )
    //  //}
    //}
    //--------
    //if (zdx == PipeMemRmw.modWrIdx) {
    //  when (setOutpModMemWord.io.decodeExt.memAccessIsPush) {
    //    when (cMid0Front.up.isFiring) {
    //      def tempExt = outp.myExt(ydx)
    //      tempExt.memAddr(PipeMemRmw.modWrIdx) := (
    //        tempExt.memAddr(PipeMemRmw.modRdIdxStart)
    //      )
    //      tempExt.modMemWord := (
    //        //tempExt.rdMemWord(PipeMemRmw.modRdIdxStart)
    //        - (cfg.mainWidth / 8)
    //      )
    //    }
    //  }
    //}
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
        //tempExt=outp.myExt(ydx),
        ydx=ydx,
        //tempRdMemWord=tempRdMemWord,
        zdx=zdx
      )
      //tempRdMemWord := myRdMemWord(ydx=ydx, modIdx=zdx)
    }
  } else { // if (cfg.regFileWordCountArr.size > 1)
    for (
      (tempRdMemWord, zdx) <- setOutpModMemWord.io.rdMemWord.zipWithIndex
    ) {
      val mapElem = outp.gprIdxToMemAddrIdxMap(zdx)
      ////if (ydx == 0) {
      //  // prevent multiple drivers
      //  tempRdMemWord := 0x0
      //}
      assert(mapElem.haveHowToSetIdx)
      switch (mapElem.howToSetIdx) {
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          is (ydx) {
            //val howToSlice = cfg.shRegFileCfg.howToSlice
            //switch (mapElem.idx) {
            //  for (
            //    //howToIdx <- 0 until (1 << mapElem.idx.getWidth)
            //    howToIdx
            //    <- 0 until 
            //  ) {
            //    is (howToIdx) {
            //      println(
            //        s"debug: switch (mapElem.howToSetIdx): "
            //        + s"zdx:${zdx}; "
            //        + s"howToIdx:${howToIdx} howToSetIdx:${ydx} "
            //      )
            //    }
            //  }
            //}
            doFinishSetOutpModMemWord(
              ydx=ydx,
              zdx=zdx,
            )
          }
        }
      }
    }
    //for ((tempExt, ydx) <- outp.myExt.zipWithIndex) {
    //  for (
    //    (tempRdMemWord, zdx) <- setOutpModMemWord.io.rdMemWord.zipWithIndex
    //  ) {
    //    if (ydx == 0) {
    //      // prevent multiple drivers
    //      tempRdMemWord := 0x0
    //    }
    //    val mapElem = outp.gprIdxToMemAddrIdxMap(zdx)
    //    assert(
    //      mapElem.haveHowToSetIdx
    //    )
    //    //switch (mapElem.howToSetIdx) {
    //    //}
    //    //switch (
    //    //  //tempExt.memAddr(zdx)
    //    //  outp.gprIdxVec(zdx)
    //    //) {
    //    //  val howToSlice = cfg.shRegFileCfg.howToSlice
    //    //  assert(
    //    //    howToSlice.size == outp.myExt.size,
    //    //    s"${howToSlice.size} ${outp.myExt.size}"
    //    //  )
    //    //  for ((howTo, howToIdx) <- howToSlice(ydx).view.zipWithIndex) {
    //    //    is (howTo) {
    //    //      doFinishSetOutpModMemWord(ydx=ydx, zdx=zdx)
    //    //      //if (zdx == PipeMemRmw.modWrIdx) {
    //    //      //  tempExt.modMemWord := (
    //    //      //    // TODO: support multiple `modMemWord`s
    //    //      //    setOutpModMemWord.io.modMemWord(0)
    //    //      //  )
    //    //      //  tempExt.modMemWordValid := (
    //    //      //    setOutpModMemWord.io.modMemWordValid
    //    //      //  )
    //    //      //}
    //    //      //tempRdMemWord := (
    //    //      //  myRdMemWord(
    //    //      //    ydx=ydx,
    //    //      //    modIdx=zdx,
    //    //      //  )
    //    //      //)
    //    //      println(
    //    //        s"debug: "
    //    //        + s"howTo(${howTo} ${howToIdx}) "
    //    //        + s"ydx:${ydx} "
    //    //        + s"zdx:${zdx}"
    //    //      )
    //    //    }
    //    //  }
    //    //}
    //  }
    //}
  }
  //def handleCurrFire(
  //  //someRdMemWord: UInt//=myRdMemWord,
  //): Unit = {
  //  //outp.myExt(0).valid := True
  //  nextPrevTxnWasHazard := False
  //  // TODO: update code to support `setOutpModMemWord.io... :=`
  //  //setOutpModMemWord(
  //  //  someRdMemWord=someRdMemWord
  //  //)
  //  //switch (
  //  //  outp.myExt(ydx)
  //  //) {
  //  //}
  //  //setOutpModMemWord.io.doIt := True
  //  for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //    outp.myExt(ydx).valid := (
  //      outp.myExt(ydx).modMemWordValid
  //    )
  //  }
  //}
  //def handleDuplicateIt(
  //  //someModMemWordValid: Bool=False,
  //): Unit = {
  //  for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //    outp.myExt(ydx).valid := False
  //    outp.myExt(ydx).modMemWordValid := (
  //      //someModMemWordValid
  //      False
  //    )
  //  }
  //  cMid0Front.duplicateIt()
  //}
  val nextSavedStall = Bool()
  val rSavedStall = (
    KeepAttribute(
      RegNext(
        next=nextSavedStall,
        init=nextSavedStall.getZero
      )
    )
  )
  nextSavedStall := rSavedStall
  when (doCheckHazard) {
    when (myDoHaveHazard) {
      myDoStall(stallKindMem) := True
    }
    if (cfg.optFormal) {
      cover(
        (
          ///outp.myExt(0).memAddr(0)
          //=== tempModFrontPayload.myExt(0).memAddr(0)
          //myDoHaveHazardAddrCheckVec.reduce(_ || _)
          //myDoHaveHazardAddrCheckVec.foldLeft(False)(
          //  (left, right) => (left || right)
          //)
          //myDoHaveHazardAddrCheckVec.sFindFirst(_ === True)._1
          myDoHaveHazardAddrCheckVec.reduceLeft(_ || _)
        ) && (
          cMid0Front.up.isFiring
        )
      )
    }
  }
  //when (cMid0Front.up.isValid) {
  //  when (doCheckHazard && myDoHaveHazard) {
  //    when (cMid0Front.down.isFiring) {
  //      myDoStall(stallKindMem) := False
  //      //nextSavedStall := True
  //    }
  //  }
  //}
  //when (cMid0Front.up.isFiring) {
  //  nextSavedStall := False
  //}
  when (psMemStallHost.fire) {
    psMemStallHost.nextValid := False
  }
  //--------
  when (cMid0Front.up.isFiring) {
    //handleCurrFire(
    //  /*U(s"${cfg.mainWidth}'d0")*/
    //)
    nextPrevTxnWasHazard := False
    //// TODO: update code to support `setOutpModMemWord.io... :=`
    //setOutpModMemWord(
    //  someRdMemWord=someRdMemWord
    //)
    //switch (
    //  outp.myExt(ydx)
    //) {
    //}
    //val mapElem = outp.gprIdxToMemAddrIdxMap(0)
    //if (!mapElem.haveHowToSetIdx) {
    //} else { // if (mapElem.haveHowToSetIdx)
    //  //switch (mapElem.howToSetIdx) {
    //  //  for (howToSetIdx <- 0 until )
    //  //}
    //  mapElem.howToSetIdx
    //}
    //setOutpModMemWord.io.doIt := True
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      outp.myExt(ydx).valid := (
        outp.myExt(ydx).modMemWordValid
      )
    }
  }
  //val nextPcChangeState = PcChangeState()
  //val rPcChangeState = (
  //  RegNext(next=nextPcChangeState)
  //  init(PcChangeState.Idle)
  //)
  //nextPcChangeState := rPcChangeState
  //setOutpModMemWord.io.pcChangeState.payload := rPcChangeState
  //setOutpModMemWord.io.pcChangeState.valid := True
  //setOutpModMemWord.io.pcChangeState := True
  //if (cfg.optFormal) {
  //  assume(
  //    nextPcChangeState.asBits.asUInt
  //    <= PcChangeState.SecondInstr.asBits.asUInt
  //  )
  //  assume(
  //    nextPcChangeState.asBits.asUInt
  //    <= PcChangeState.SecondInstr.asBits.asUInt
  //  )
  //}
  //val rPcChangeState = Reg(UInt(2
  //next
  //val rSavedStallCnt = {
  //  RegNextWhen(
  //    next=outp.instrCnt,
  //    cond=cMid0Front.up.isFiring,
  //    init=outp.instrCnt.getZero,
  //  )
  //  //val temp = Reg(
  //  //  SnowHouseInstrCnt(cfg=cfg)
  //  //)
  //  //temp.init(temp.getZero)
  //  ////temp.jmpState.allowOverride
  //  //temp
  //}
  val rSavedJmpCnt = {
    val temp = Reg(
      SnowHouseInstrCnt(cfg=cfg)
    )
    temp.init(temp.getZero)
    //temp.jmpState.allowOverride
    temp
  }
  //val doWriteSavedStall = (
  //  KeepAttribute(
  //    Bool()
  //  )
  //)
  //doWriteSavedStall := False
  //when (
  //  cMid0Front.up.valid//isFiring
  //  && (
  //    outp.instrCnt.any === rSavedStallCnt.any + 1
  //  )
  //) {
  //  //nextSavedStall := False
  //  doWriteSavedStall := True
  //}
  val nextSetPcCnt = (
    Flow(UInt(
      //cfg.instrCntWidth bits
      cfg.mainWidth bits
    ))
  )
  val rSetPcCnt = {
    val temp = KeepAttribute(
      RegNext(next=nextSetPcCnt)
    )
    temp.valid.init(False)
    temp.payload.init(0x0)

    //temp.init(temp.getZero)
    temp
  }
  nextSetPcCnt := rSetPcCnt
  //psExSetPc.valid := (
  //  False
  //)
  psExSetPc.payload := (
    RegNext(
      next=psExSetPc.payload,
      init=psExSetPc.payload.getZero,
    )
  )
  psExSetPc.nextPc.allowOverride
  val condForAssertSetPcValid = (
    //setOutpModMemWord.io.psExSetPc.fire
    //&& !rSetPcCnt.valid
    setOutpModMemWord.io.opIsJmp
  )
  //val myPcChangeState = Bool()
  //myPcChangeState := (
  //  RegNext(
  //    next=myPcChangeState,
  //    init=myPcChangeState.getZero,
  //  )
  //)
  //val rPcChangeState = Reg(Bool(), init=False)
  //setOutpModMemWord.io.pcChangeState := (
  //  RegNext(
  //    next=setOutpModMemWord.io.pcChangeState,
  //    init=True,
  //  )
  //  //!rSetPcCnt.valid
  //)
  outp.instrCnt.shouldIgnoreInstr := (
    //!setOutpModMemWord.io.pcChangeState
    setOutpModMemWord.io.shouldIgnoreInstr
    //RegNext(
    //  next=setOutpModMemWord.io.pcChangeState,
    //  init=setOutpModMemWord.io.pcChangeState.getZero,
    //)
    //RegNext(
    //  next=outp.instrCnt.shouldIgnoreInstr,
    //  init=outp.instrCnt.shouldIgnoreInstr.getZero,
    //)
    //rSetPcCnt.valid
  )
  pcChangeState := (
    setOutpModMemWord.io.pcChangeState
  )
  psExSetPc.valid := (
    setOutpModMemWord.io.psExSetPc.valid
    && !outp.instrCnt.shouldIgnoreInstr
  )
  psExSetPc.nextPc := setOutpModMemWord.io.psExSetPc.nextPc
  io.dbus.allowOverride
  io.dbus.hostData := (
    RegNext(
      next=io.dbus.hostData,
      init=io.dbus.hostData.getZero,
    )
  )
  //io.dbus.hostData.addr.allowOverride
  //io.dbus.hostData := 
  when (
    //!outp.instrCnt.shouldIgnoreInstr
    cMid0Front.up.isFiring
  ) {
    io.dbus.hostData := setOutpModMemWord.io.dbusHostPayload
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
      multiCycleBus.hostData.srcVec.foreach(src => {
        src.allowOverride
      })
      multiCycleBus.hostData.srcVec(0) := (
        setOutpModMemWord.io.selRdMemWord(
          opInfo=opInfo,
          idx=1,
        )
      )
      multiCycleBus.hostData.srcVec(1) := (
        setOutpModMemWord.io.selRdMemWord(
          opInfo=opInfo,
          idx=2,
        )
      )
    }
  }
  //io.dbus.hostData := setOutpModMemWord.io.dbusHostPayload
  //io.dbus.hostData.addr := setOutpModMemWord.io.dbusHostPayload.addr
  //psExSetPc.cnt := rSetPcCnt.payload + 1
  //switch (rPcChangeState) {
  //  is (PcChangeState.Idle) {
      //--------
      when (
        //!rSetPcCnt.valid
        //|| 
        //outp.regPc === rSetPcCnt.payload
        //|| 
        !outp.instrCnt.shouldIgnoreInstr
      ) {
        when (
          setOutpModMemWord.io.opIsMemAccess
        ) {
          nextPrevTxnWasHazard := True
          when (cMid0Front.up.isFiring) {
            psMemStallHost.nextValid := True
          }
        }
        when (
          //setOutpModMemWord.io.opIsMultiCycle
          outp.splitOp.kind === SnowHouseSplitOpKind.MULTI_CYCLE
        ) {
          switch (
            //setOutpModMemWord.io.multiCycleOpInfoIdx
            //outp.op
            outp.splitOp.multiCycleOp
          ) {
            for (
              ((_, opInfo), opInfoIdx)
              <- cfg.multiCycleOpInfoMap.view.zipWithIndex
            ) {
              is (
                //psExStallHostArrIdx
                opInfoIdx
              ) {
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
                  def doStart(): Unit = {
                    myDoStall(stallKindMem) := False
                    myDoStall(stallKindMultiCycle) := True
                    psExStallHost.nextValid := True
                    //def multiCycleBus = io.multiCycleBusVec(busIdx)
                    //multiCycleBus.hostData.srcVec(0) := (
                    //  setOutpModMemWord.io.selRdMemWord(
                    //    opInfo=opInfo,
                    //    idx=1,
                    //  )
                    //)
                    //multiCycleBus.hostData.srcVec(1) := (
                    //  setOutpModMemWord.io.selRdMemWord(
                    //    opInfo=opInfo,
                    //    idx=2,
                    //  )
                    //)
                    nextSavedStall := True
                  }
                  when (
                    !rSavedStall
                    && doCheckHazard && myDoHaveHazard
                  ) {
                    psExStallHost.nextValid := False
                    when (psMemStallHost.fire) {
                      doStart()
                    }
                  } otherwise {
                    doStart()
                  }
                  when (
                    psExStallHost.rValid
                    && psExStallHost.ready
                  ) {
                    psExStallHost.nextValid := False
                    myDoStall(stallKindMultiCycle) := False
                  }
                  when (rSavedStall) {
                    myDoStall(stallKindMem) := False
                  }
                  when (cMid0Front.up.isFiring) {
                    nextSavedStall := False
                  }
                }
              }
            }
          }
        }
        //switch (setOutpModMemWord.io.opIs) {
        //  // TODO: support mem access in more kinds of instructions
        //  //is (M"0010") {
        //  //  // instruction is of type Cpy (non-Jmp, non-Br)/Alu,
        //  //  // but NO mem access
        //  //  if (cfg.optFormal) {
        //  //    when (cMid0Front.up.isValid) {
        //  //      when (!doCheckHazard) {
        //  //        assert(!myDoStall(stallKindMem))
        //  //        assert(!myDoStall(stallKindMultiCycle))
        //  //      }
        //  //    }
        //  //  }
        //  //}
        //  is (M"0011") {
        //    // instruction is of type Cpy (non-Jmp, non-Br)/Alu,
        //    // but WITH mem access
        //    when (cMid0Front.up.isFiring) {
        //      nextPrevTxnWasHazard := True
        //      psMemStallHost.nextValid := True
        //      //io.dbus.hostData := setOutpModMemWord.io.dbusHostPayload
        //    }
        //    //when (cMid0Front.down.isReady) {
        //    //  io.dbus.hostData := setOutpModMemWord.io.dbusHostPayload
        //    //}
        //  }
        //  is (M"01--") {
        //    // instruction is of type Cpy (TAKEN Jmp or Br),
        //    // but with NO mem access
        //    //--------
        //    //nextSetPcCnt.valid := False
        //    //when (setOutpModMemWord.io.psExSetPc.fire) {
        //    //  nextSetPcCnt.valid := True
        //    //  nextSetPcCnt.payload := rSetPcCnt.payload + 1
        //    //  psExSetPc.valid := setOutpModMemWord.io.psExSetPc.valid
        //    //  psExSetPc.nextPc := setOutpModMemWord.io.psExSetPc.nextPc
        //    //  psExSetPc.cnt := rSetPcCnt.payload + 1
        //    //  //nextSetPcCnt.payload := psExSetPc.cnt
        //    //}
        //    //--------
        //    when (cMid0Front.up.isFiring) {
        //      //nextPcChangeState := PcChangeState.WaitTwoInstrs
        //      //rSavedJmpCnt := outp.instrCnt
        //      //for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        //      //  outp.myExt(ydx).modMemWordValid := False
        //      //}
        //      //outp.instrCnt.shouldIgnoreInstr := True
        //    }
        //  }
        //  is (M"1---") {
        //    // instruction is of type MultiCycle,
        //    // but with NO mem access
        //    //val rTempSavedRegPc = (
        //    //  KeepAttribute(
        //    //    RegNextWhen(
        //    //      next=outp.regPc,
        //    //      cond=cMid0Front.up.isFiring,
        //    //      init=outp.regPc.getZero,
        //    //    )
        //    //    //Reg(UInt(cfg.mainWidth bits))
        //    //    //init(0x0)
        //    //  )
        //    //  .setName(
        //    //    //s"rTempSavedRegPc_${psExStallHostArrIdx}"
        //    //    s"rTempSavedRegPc"
        //    //  )
        //    //)
        //    switch (
        //      //setOutpModMemWord.io.multiCycleOpInfoIdx
        //      //outp.op
        //      outp.splitOp.multiCycleOp
        //    ) {
        //      for (
        //        //(psExStallHost, psExStallHostArrIdx)
        //        //<- psExStallHostArr.view.zipWithIndex
        //        //((_, opInfo), opInfoIdx)
        //        //<- cfg.opInfoMap.view.zipWithIndex

        //        ((_, opInfo), opInfoIdx)
        //        <- cfg.multiCycleOpInfoMap.view.zipWithIndex
        //      ) {
        //        is (
        //          //psExStallHostArrIdx
        //          opInfoIdx
        //        ) {
        //          var busIdxFound: Boolean = false
        //          var busIdx: Int = 0
        //          for (
        //            ((_, multiCycleOpInfo), myBusIdx)
        //            <- cfg.multiCycleOpInfoMap.view.zipWithIndex
        //          ) {
        //            if (opInfo == multiCycleOpInfo) {
        //              busIdxFound = true
        //              busIdx = myBusIdx
        //            }
        //          }
        //          //when (
        //          //  !rSavedStall
        //          //  //|| doWriteSavedStall
        //          //) {
        //          //  myDoStall(stallKindMultiCycle) := True
        //          //  when (doCheckHazard && myDoHaveHazard) {
        //          //    //when (!rSavedStall) {
        //          //    //}
        //          //    when (
        //          //      //!rSavedStall
        //          //      //&& 
        //          //      cMid0Front.down.isFiring
        //          //    ) {
        //          //      nextSavedStall := True
        //          //    }
        //          //  } otherwise {
        //          //    nextSavedStall := True
        //          //    psExStallHost.nextValid := True
        //          //    //myDoStall(stallKindMultiCycle) := True
        //          //  }
        //          //}
        //          //when (!myDoStall(stallKindMem)) {
        //          //}
        //          if (busIdxFound) {
        //            val psExStallHost = psExStallHostArr(busIdx)
        //            //when (
        //            //  (
        //            //    outp.regPc(3 downto 0)
        //            //    === (
        //            //      rTempSavedRegPc(3 downto 0)
        //            //      + (cfg.instrMainWidth / 8)
        //            //    )
        //            //  )
        //            //  //|| (
        //            //  //  outp.regPc
        //            //  //  === rTempSavedRegPc + ((cfg.instrMainWidth / 8) * 2)
        //            //  //)
        //            //) {
        //              def doStart(): Unit = {
        //                myDoStall(stallKindMem) := False
        //                myDoStall(stallKindMultiCycle) := True
        //                psExStallHost.nextValid := True
        //                def multiCycleBus = io.multiCycleBusVec(busIdx)
        //                multiCycleBus.hostData.srcVec(0) := (
        //                  setOutpModMemWord.io.selRdMemWord(
        //                    opInfo=opInfo,
        //                    idx=1,
        //                  )
        //                )
        //                multiCycleBus.hostData.srcVec(1) := (
        //                  setOutpModMemWord.io.selRdMemWord(
        //                    opInfo=opInfo,
        //                    idx=2,
        //                  )
        //                )
        //                nextSavedStall := True
        //              }
        //              when (
        //                !rSavedStall
        //                && doCheckHazard && myDoHaveHazard
        //              ) {
        //                psExStallHost.nextValid := False
        //                when (psMemStallHost.fire) {
        //                  doStart()
        //                }
        //              } otherwise {
        //                //myDoStall(stallKindMem) := False
        //                //myDoStall(stallKindMultiCycle) := True
        //                //psExStallHost.nextValid := True
        //                //nextSavedStall := True
        //                doStart()
        //              }
        //            //}
        //            when (
        //              psExStallHost.rValid
        //              && psExStallHost.ready
        //            ) {
        //              psExStallHost.nextValid := False
        //              //myDoStall(stallKindMem) := False
        //              myDoStall(stallKindMultiCycle) := False
        //              //nextSavedStall := False
        //            }
        //            when (
        //              rSavedStall
        //              //&& !myDoStall(stallKindMultiCycle)
        //            ) {
        //              myDoStall(stallKindMem) := False
        //            }
        //            when (cMid0Front.up.isFiring) {
        //              nextSavedStall := False
        //            }
        //          }
        //        }
        //      }
        //    }
        //  }
        //  //when (savedPsExStallHost.myDuplicateIt) {
        //  //  currDuplicateIt := True
        //  //}
        //  //--------
        //  // TODO: replace this formal verification
        //  //if (cfg.optFormal) {
        //  //  when (!doCheckHazard) {
        //  //    when (!savedPsExStallHost.myDuplicateIt) {
        //  //      assert(!myDoStall(stallKindMem))
        //  //    }
        //  //  }
        //  //}
        //  //--------
        //  default {
        //  }
        //}
      } otherwise {
        //setOutpModMemWord.io.pcChangeState := False
        //outp.instrCnt.shouldIgnoreInstr := True
      }
  //  }
  //  is (PcChangeState.WaitTwoInstrs) {
  //    if (cfg.optFormal) {
  //      when (pastValidAfterReset) {
  //        when (past(rPcChangeState) === PcChangeState.Idle) {
  //          assert(past(cMid0Front.up.isFiring))
  //          assert(rSavedJmpCnt === past(outp.instrCnt))
  //          assert(!past(outp.instrCnt.shouldIgnoreInstr))
  //          assert(past(setOutpModMemWord.io.opIsJmp))
  //        } otherwise {
  //          assert(stable(rSavedJmpCnt))
  //          //assert(stable(outp.instrCnt.shouldIgnoreInstr))
  //        }
  //        //when (past(cMid0Front.up.isFiring)) {
  //        //}
  //      }
  //    }
  //    //outp.instrCnt.shouldIgnoreInstr := True
  //    outp.instrCnt.shouldIgnoreInstr := True
  //    when (cMid0Front.up.isFiring) {
  //      when (
  //        //outp.instrCnt.any === rSavedJmpCnt.any + 2
  //      ) {
  //        // with wrapping arithmetic,
  //        // it's okay if we overflow with the + 2!
  //        //outp.instrCnt.shouldIgnoreInstr := False
  //        nextPcChangeState := PcChangeState.Idle
  //      }
  //    }
  //  }
  //  //is (PcChangeState.SecondInstr) {
  //  //}
  //  //default {
  //  //  //assert(
  //  //  //  False
  //  //  //)
  //  //}
  //}
  psExStallHostArr.foreach(psExStallHost => {
    when (psExStallHost.fire) {
      psExStallHost.nextValid := False
    }
  })
  doCheckHazard := rPrevTxnWasHazard
  //when (rPrevTxnWasHazard) {
  //  //assert(cfg.regFileModRdPortCnt == 1)
  //  doCheckHazard := True
  //} otherwise {
  //  doCheckHazard := False
  //}
  //switch (myCurrOp) {
  //  //is (PipeMemRmwSimDut.ModOp.AddRaRb) {
  //  //  if (cfg.optFormal) {
  //  //    when (cMid0Front.up.isValid) {
  //  //      when (!doCheckHazard) {
  //  //        assert(!currDuplicateIt)
  //  //      }
  //  //    }
  //  //  }
  //  //}
  //  //is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
  //  //  when (cMid0Front.up.isFiring) {
  //  //    nextPrevTxnWasHazard := True
  //  //    psMemStallHost.nextValid := True
  //  //  }
  //  //}
  //  //is (PipeMemRmwSimDut.ModOp.MulRaRb) {
  //  //  if (PipeMemRmwSimDut.haveModOpMul) {
  //  //    //--------
  //  //    when (cMid0Front.up.isValid) {
  //  //      when (doCheckHazard) {
  //  //        when (!currDuplicateIt) {
  //  //          psExStallHost.nextValid := (
  //  //            True
  //  //          )
  //  //        }
  //  //      } otherwise { // when (!doCheckHazard)
  //  //        psExStallHost.nextValid := (
  //  //          True
  //  //        )
  //  //      }
  //  //      //--------
  //  //    }
  //  //    when (savedPsExStallHost.myDuplicateIt) {
  //  //      currDuplicateIt := True
  //  //    }
  //  //    if (cfg.optFormal) {
  //  //      when (!doCheckHazard) {
  //  //        when (!savedPsExStallHost.myDuplicateIt) {
  //  //          assert(!currDuplicateIt)
  //  //        }
  //  //      }
  //  //    }
  //  //  }
  //  //}
  //}
  // TODO: convert this
  //if (PipeMemRmwSimDut.haveModOpMul) {
  //  when (psExStallHost.fire) {
  //    psExStallHost.nextValid := False
  //  }
  //}
  when (
    myDoStall.sFindFirst(_ === True)._1
    //|| outp.instrCnt.shouldIgnoreInstr
  ) {
    //handleDuplicateIt()
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      outp.myExt(ydx).valid := False
      outp.myExt(ydx).modMemWordValid := (
        //someModMemWordValid
        False
      )
    }
    when (myDoStall.sFindFirst(_ === True)._1) {
      cMid0Front.duplicateIt()
    }
  }
  if (cfg.optFormal) {
    outp.psExSetOutpModMemWordIo := setOutpModMemWord.io
  }
  outp.regPcPlusImm := (
    outp.regPc + outp.imm //+ (cfg.instrMainWidth / 8)
  )
}
case class SnowHousePipeStageMem(
  args: SnowHousePipeStageArgs,
  psWb: SnowHousePipeStageWriteBack,
  psMemStallHost: LcvStallHost[
    DbusHostPayload,
    DbusDevPayload,
  ],
) extends Area {
  def cfg = args.cfg
  def io = args.io
  def regFile = args.regFile
  def front = regFile.io.front
  def frontPayload = regFile.io.frontPayload
  def modFront = regFile.io.modFront
  //def modFront = doModInModFrontParams.modFront
  def modFrontPayload = regFile.io.modFrontPayload
  def modBack = regFile.io.modBack
  def modBackPayload = regFile.io.modBackPayload
  def back = regFile.io.back
  def backPayload = regFile.io.backPayload
  def tempModFrontPayload = (
    //doModInModFrontParams.tempModFrontPayload//Vec(ydxr
    regFile.io.tempModFrontPayload
  )
  //--------
  val modFrontFormalAssumes = modFront(modFrontPayload).formalAssumes()
  val modBackFormalAssumes = modBack(modBackPayload).formalAssumes()
  //--------
  def extIdxUp = PipeMemRmw.extIdxUp
  def extIdxSaved = PipeMemRmw.extIdxSaved
  def extIdxLim = PipeMemRmw.extIdxLim
  //--------
  def doMidMod = true
  val midModPayload = (
    Vec.fill(extIdxLim)(
      SnowHousePipePayload(cfg=cfg)
    )
  )
  val myShouldIgnoreInstr = (
    modFront(modFrontPayload).instrCnt.shouldIgnoreInstr
  )
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
  val sMidModFront = (doMidMod) generate (
    StageLink(
      up=cMidModFront.down,
      down={
        modBack
        ////Node()
        //val temp = Node()
        //temp.setName(s"sMidModFront_down")
        //temp
      },
    )
  )
  //val s2mMidModFront = (doMidMod) generate (
  //  S2MLink(
  //    up=sMidModFront.down,
  //    down=(
  //      modBack
  //    ),
  //  )
  //)
  regFile.myLinkArr += cMidModFront
  regFile.myLinkArr += sMidModFront
  //regFile.myLinkArr += s2mMidModFront
  val formalFwdMidModArea = (regFile.myHaveFormalFwd) generate (
    new Area {
      val myFwd = (
        KeepAttribute(
          Vec.fill(extIdxLim)(
            regFile.mkFwd()
          )
        )
        .setName(
          s"formalFwdMidModArea_"
          + s"myFwd"
        )
      )
      for (extIdx <- 0 until extIdxLim) {
        myFwd(extIdx) := midModPayload(extIdx).myFwd
        //myFwd(extIdx).myFindFirst_0.allowOverride
        //myFwd(extIdx).myFindFirst_1.allowOverride
      }
      val doFormalFwdUp =  (
        PipeMemRmwDoFwdArea(
          fwdAreaName=s"formalFwdMidModArea_doFormalFwdUp",
          fwd=(
            myFwd(extIdxUp)
            //midModPayload(extIdxUp).myFwd
          ),
          setToMyFwdDataFunc=(
            ydx: Int,
            zdx: Int,
            myFwdData: UInt,
          ) => {
            when (pastValidAfterReset) {
              assert(
                midModPayload(extIdxUp).myExt(ydx).rdMemWord(zdx)
                === myFwdData
              )
            }
          }
        )
      )
      val doFormalFwdSaved =  (
        PipeMemRmwDoFwdArea(
          fwdAreaName=s"formalFwdMidModArea_doFormalFwdSaved",
          fwd=(
            myFwd(extIdxSaved)
            //midModPayload(extIdxSaved).myFwd
          ),
          setToMyFwdDataFunc=(
            ydx: Int,
            zdx: Int,
            myFwdData: UInt,
          ) => {
            when (pastValidAfterReset) {
              assert(
                midModPayload(extIdxSaved).myExt(ydx).rdMemWord(zdx)
                === myFwdData
              )
            }
          }
        )
      )
    }
  )
  //if (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd) {
  //  assert(modStageCnt == 1)
  //}
  //--------
  val nextSetMidModPayloadState = (
    KeepAttribute(
      Bool()
    )
    .setName(s"nextSetMidModPayloadState")
  )
  val rSetMidModPayloadState = (
    KeepAttribute(
      RegNext(
        next=nextSetMidModPayloadState,
        init=nextSetMidModPayloadState.getZero,
      )
    )
    .setName(s"rSetMidModPayloadState")
  )
  nextSetMidModPayloadState := rSetMidModPayloadState
  //--------
  if (regFile.myHaveFormalFwd) {
    when (pastValidAfterReset) {
      def myExt(
        someExtIdx: Int
      ) = (
        midModPayload(someExtIdx).myExt
      )
      def myFwd(
        someExtIdx: Int
      ) = (
        midModPayload(someExtIdx).myFwd
      )
      when (
        !RegNextWhen(
          next=True,
          cond=cMidModFront.up.isFiring,
          init=False,
        )
      ) {
        when (!cMidModFront.up.isValid) {
          //assert(
          //  myExt(extIdxUp)(0).main
          //  === myExt(extIdxUp)(0).main.getZero
          //)
          myExt(extIdxUp).foreach(current => {
            assert(current.main === current.main.getZero)
          })
          assert(
            myFwd(extIdxUp)
            === myFwd(extIdxUp).getZero
          )
        }
        //assert(
        //  myExt(extIdxSaved)(0)
        //  === myExt(extIdxSaved)(0).getZero
        //)
        myExt(extIdxSaved).foreach(current => {
          assert(current === current.getZero)
        })
        assert(
          myFwd(extIdxSaved)
          === myFwd(extIdxSaved).getZero
        )
      } 
      when (
        past(cMidModFront.up.isFiring) init(False)
      ) {
        assert(
          midModPayload(extIdxSaved)
          === past(midModPayload(extIdxUp))
        )
      }
      when (
        !cMidModFront.up.isValid
        && !past(cMidModFront.up.isValid)
      ) {
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          for (zdx <- 0 until cfg.regFileModRdPortCnt) {
            assert(
              stable(myExt(extIdxUp)(ydx).memAddr(zdx))
            )
            assert(
              stable(myExt(extIdxUp)(ydx).rdMemWord(zdx))
            )
          }
        }
        when (
          //midModPayload(extIdxUp).op
          //=/= PipeMemRmwSimDut.ModOp.LdrRaRb.asBits.asUInt
          midModPayload(extIdxUp).decodeExt.opIsMemAccess
        ) {
          for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
            for (zdx <- 0 until cfg.regFileModRdPortCnt) {
              assert(stable(myExt(extIdxUp)(ydx).modMemWord(zdx)))
            }
          }
        }
        //assert(
        //  stable(myExt(extIdxSaved)(0))
        //)
        myExt(extIdxSaved).foreach(current => {
          assert(stable(current))
        })
        assert(
          stable(myFwd(extIdxUp))
        )
        assert(
          stable(myFwd(extIdxSaved))
        )
      }
      when (
        cMidModFront.up.isValid
        && !rSetMidModPayloadState
      ) {
        assert(
          myFwd(extIdxUp)
          === cMidModFront.up(modFrontPayload).myFwd
        )
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          for (zdx <- 0 until cfg.regFileModRdPortCnt) {
            assert(
              myExt(extIdxUp)(ydx).rdMemWord(zdx)
              === (
                cMidModFront.up(modFrontPayload).myExt(ydx).rdMemWord(zdx)
              )
            )
            assert(
              myExt(extIdxUp)(ydx).memAddr(zdx)
              === (
                cMidModFront.up(modFrontPayload).myExt(ydx).memAddr(zdx)
              )
            )
          }
          when (
            cMidModFront.up(modFrontPayload).myExt(ydx).modMemWordValid
          ) {
            assert(
              myExt(extIdxUp)(ydx).modMemWordValid
            )
          }
        }
      }
    }
  }

  midModPayload(extIdxSaved) := (
    RegNextWhen(
      next=midModPayload(extIdxUp),
      cond=cMidModFront.up.isFiring,
      init=midModPayload(extIdxSaved).getZero,
    )
  )
  //for (extIdx <- 0 until extIdxLim) {
  //  if (extIdx != extIdxSaved) {
  //    midModPayload(extIdx) := (
  //      RegNext(
  //        next=midModPayload(extIdx),
  //        init=midModPayload(extIdx).getZero,
  //      )
  //    )
  //  }
  //}
  tempModFrontPayload := midModPayload(extIdxUp)
  for (idx <- 0 until tempModFrontPayload.gprIdxVec.size) {
    tempModFrontPayload.gprIdxVec(idx).allowOverride
    tempModFrontPayload.gprIdxVec(idx) := (
      modFront(modFrontPayload).gprIdxVec(idx)
    )
  }
  val savedPsMemStallHost = (
    LcvStallHostSaved(
      stallHost=psMemStallHost,
      someLink=cMidModFront,
    )
    .setName(s"psMem_savedPsMemStallHost")
  )
  if (cfg.optFormal) {
    when (pastValidAfterReset) {
      when (past(cMidModFront.up.isFiring) init(False)) {
        assert(!rSetMidModPayloadState)
      }
      when (!(past(cMidModFront.up.isValid) init(False))) {
        assert(stable(rSetMidModPayloadState))
      }
      when (rSetMidModPayloadState) {
        assert(cMidModFront.up.isValid)
      }
    }
  }
  midModPayload(extIdxUp) := modFront(modFrontPayload)
  //when (cMidModFront.up.isValid ) {
  //  when (!rSetMidModPayloadState) {
  //    midModPayload(extIdxUp) := modFront(modFrontPayload)
  //    nextSetMidModPayloadState := True
  //  }
  //  when (cMidModFront.up.isFiring) {
  //    nextSetMidModPayloadState := False
  //  }
  //  //--------
  //  //switch (midModPayload(extIdxUp).op) {
  //  //  for (((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex) {
  //  //    is (opInfoIdx) {
  //  //      def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
  //  //      def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
  //  //      opInfo.select match {
  //  //        //is (PipeMemRmwSimDut.ModOp.LdrRaRb)
  //  //        case OpSelect.Cpy => {
  //  //          for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //  //            val myExtLeft = tempExtLeft(ydx=ydx)
  //  //            val myExtRight = tempExtRight(ydx=ydx)
  //  //            opInfo.cpyOp.get match {
  //  //              case CpyOpKind.Cpy if (
  //  //                opInfo.memAccess != MemAccessKind.NoMemAccess
  //  //              ) => {
  //  //                when (!myExtLeft.modMemWordValid) {
  //  //                } otherwise {
  //  //                  if (cfg.optFormal) {
  //  //                    when (!myShouldIgnoreInstr) {
  //  //                      //assert(!savedPsMemStallHost.myDuplicateIt)
  //  //                    }
  //  //                  }
  //  //                }
  //  //                if (cfg.optFormal) {
  //  //                  opInfo.memAccess match {
  //  //                    case mem: MemAccessKind.Mem => {
  //  //                      if (!mem.isAtomic) {
  //  //                        when (
  //  //                          cMidModFront.up.isFiring
  //  //                          && !myShouldIgnoreInstr
  //  //                        ) {
  //  //                          //if (!isStore) {
  //  //                          assert(
  //  //                            myExtLeft.modMemWordValid
  //  //                            === (
  //  //                              if (!mem.isStore) (
  //  //                                True
  //  //                              ) else ( // if (isStore)
  //  //                                False
  //  //                              )
  //  //                            )
  //  //                          )
  //  //                          //}
  //  //                        }
  //  //                      } else {
  //  //                        assert(
  //  //                          false,
  //  //                          s"atomic operations not yet supported: "
  //  //                          + s"opInfo(${opInfo} ${opInfo.select}) "
  //  //                          + s"opInfoIdx:${opInfoIdx}"
  //  //                        )
  //  //                      }
  //  //                    }
  //  //                    case _ => {
  //  //                    }
  //  //                  }
  //  //                }
  //  //              }
  //  //              case _ => {
  //  //                myExtLeft.modMemWord := (
  //  //                  myExtRight.modMemWord
  //  //                )
  //  //                myExtLeft.modMemWordValid := (
  //  //                  myExtRight.modMemWordValid
  //  //                )
  //  //                for (zdx <- 0 until cfg.regFileModRdPortCnt) {
  //  //                  myExtLeft.rdMemWord(zdx) := (
  //  //                    myExtRight.rdMemWord(zdx)
  //  //                  )
  //  //                  myExtLeft.memAddr(zdx) := (
  //  //                    myExtRight.memAddr(zdx)
  //  //                  )
  //  //                  if (cfg.optFormal) {
  //  //                    when (pastValidAfterReset) {
  //  //                      when (rose(rSetMidModPayloadState)) {
  //  //                        assert(
  //  //                          myExtLeft.memAddr(zdx)
  //  //                          === past(myExtRight.memAddr(zdx))
  //  //                        )
  //  //                        assert(
  //  //                          myExtLeft.rdMemWord(zdx)
  //  //                          === past(myExtRight.rdMemWord(zdx))
  //  //                        )
  //  //                        if (zdx == 0) {
  //  //                          assert(
  //  //                            myExtLeft.modMemWord
  //  //                            === past(myExtRight.modMemWord)
  //  //                          )
  //  //                          assert(
  //  //                            myExtLeft.modMemWordValid
  //  //                            === past(myExtRight.modMemWordValid)
  //  //                          )
  //  //                        }
  //  //                      }
  //  //                    }
  //  //                  }
  //  //                }
  //  //              }
  //  //            }
  //  //          }
  //  //        }
  //  //        case OpSelect.Alu => {
  //  //          for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //  //            val myExtLeft = tempExtLeft(ydx=ydx)
  //  //            val myExtRight = tempExtRight(ydx=ydx)
  //  //            myExtLeft.modMemWord := (
  //  //              myExtRight.modMemWord
  //  //            )
  //  //            myExtLeft.modMemWordValid := (
  //  //              myExtRight.modMemWordValid
  //  //            )
  //  //            for (zdx <- 0 until cfg.regFileModRdPortCnt) {
  //  //              myExtLeft.rdMemWord(zdx) := (
  //  //                myExtRight.rdMemWord(zdx)
  //  //              )
  //  //              myExtLeft.memAddr(zdx) := (
  //  //                myExtRight.memAddr(zdx)
  //  //              )
  //  //              if (cfg.optFormal) {
  //  //                when (pastValidAfterReset) {
  //  //                  when (rose(rSetMidModPayloadState)) {
  //  //                    assert(
  //  //                      myExtLeft.memAddr(zdx)
  //  //                      === past(myExtRight.memAddr(zdx))
  //  //                    )
  //  //                    assert(
  //  //                      myExtLeft.rdMemWord(zdx)
  //  //                      === past(myExtRight.rdMemWord(zdx))
  //  //                    )
  //  //                    if (zdx == 0) {
  //  //                      assert(
  //  //                        myExtLeft.modMemWord
  //  //                        === past(myExtRight.modMemWord)
  //  //                      )
  //  //                      assert(
  //  //                        myExtLeft.modMemWordValid
  //  //                        === past(myExtRight.modMemWordValid)
  //  //                      )
  //  //                    }
  //  //                  }
  //  //                }
  //  //              }
  //  //            }
  //  //          }
  //  //        }
  //  //        //is (PipeMemRmwSimDut.ModOp.MulRaRb)
  //  //        case OpSelect.MultiCycle => {
  //  //          for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //  //            val myExtLeft = tempExtLeft(ydx=ydx)
  //  //            val myExtRight = tempExtRight(ydx=ydx)
  //  //            myExtLeft.modMemWord := (
  //  //              myExtRight.modMemWord
  //  //            )
  //  //            myExtLeft.modMemWordValid := (
  //  //              myExtRight.modMemWordValid
  //  //            )
  //  //            for (zdx <- 0 until cfg.regFileModRdPortCnt) {
  //  //              myExtLeft.memAddr(zdx) := (
  //  //                myExtRight.memAddr(zdx)
  //  //              )
  //  //              myExtLeft.rdMemWord(zdx) := (
  //  //                myExtRight.rdMemWord(zdx)
  //  //              )
  //  //              if (cfg.optFormal) {
  //  //                when (pastValidAfterReset) {
  //  //                  when (rose(rSetMidModPayloadState)) {
  //  //                    assert(
  //  //                      myExtLeft.memAddr(zdx)
  //  //                      === past(myExtRight.memAddr(zdx))
  //  //                    )
  //  //                    assert(
  //  //                      myExtLeft.rdMemWord(zdx)
  //  //                      === past(myExtRight.rdMemWord(zdx))
  //  //                    )
  //  //                    if (zdx == 0) {
  //  //                      assert(
  //  //                        myExtLeft.modMemWord
  //  //                        === past(myExtRight.modMemWord)
  //  //                      )
  //  //                      assert(
  //  //                        myExtLeft.modMemWordValid
  //  //                        === past(myExtRight.modMemWordValid)
  //  //                      )
  //  //                    }
  //  //                  }
  //  //                }
  //  //              }
  //  //            }
  //  //          }
  //  //        }
  //  //      }
  //  //    }
  //  //  }
  //  //}
  //}
  //when (!midModPayload(extIdxUp).instrCnt.shouldIgnoreInstr) {
  //} otherwise {
  //  midModPayload(extIdxUp).myExt.foreach(someExt => {
  //    someExt.modMemWordValid := False
  //    someExt.valid := False
  //  })
  //  tempModFrontPayload.myExt.foreach(someExt => {
  //    someExt.modMemWordValid := False
  //    someExt.valid := False
  //  })
  //}
  //if (cfg.optFormal) {
  //  when (
  //    pastValidAfterReset
  //    && !myShouldIgnoreInstr
  //  ) {
  //    when (
  //      (
  //        /*past*/(cMidModFront.up.isValid) //init(False)
  //      ) && (
  //        !(
  //          /*past*/(rSetMidModPayloadState) //init(False)
  //        )
  //      )
  //    ) {
  //      def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
  //      def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
  //      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //        val myExtLeft = tempExtLeft(ydx=ydx)
  //        val myExtRight = tempExtRight(ydx=ydx)
  //        for (zdx <- 0 until cfg.regFileModRdPortCnt) {
  //          assert(
  //            myExtLeft.rdMemWord(zdx)
  //            === /*past*/(
  //              myExtRight.rdMemWord(zdx)
  //            )
  //          )
  //        }
  //      }
  //      assert(
  //        midModPayload(extIdxUp).op
  //        === /*past*/(modFront(modFrontPayload).op)
  //      )
  //      assert(
  //        midModPayload(extIdxUp).opCnt
  //        === /*past*/(modFront(modFrontPayload).opCnt)
  //      )
  //    }
  //    when (rose(rSetMidModPayloadState)) {
  //      def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
  //      //def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
  //      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
  //        val myExtLeft = tempExtLeft(ydx=ydx)
  //        //val myExtRight = tempExtRight(ydx=ydx)
  //        assert(stable(myExtLeft.rdMemWord))
  //        switch (midModPayload(extIdxUp).op) {
  //          for (
  //            ((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex
  //          ) {
  //            //is (PipeMemRmwSimDut.ModOp.AddRaRb)
  //            is (opInfoIdx) {
  //              val myPsExSetOutpModMemWordIo = (
  //                midModPayload(extIdxUp).psExSetOutpModMemWordIo
  //              )
  //              def selRdMemWord(
  //                idx: Int,
  //              ): UInt = {
  //                myPsExSetOutpModMemWordIo.selRdMemWord(
  //                  opInfo=opInfo,
  //                  idx=idx,
  //                )
  //              }
  //              opInfo.select match {
  //                case OpSelect.Cpy if (
  //                  opInfo.memAccess == MemAccessKind.NoMemAccess
  //                ) => {
  //                  assert(stable(myExtLeft.modMemWord))
  //                  //when (myExtLeft.modMemWordValid) {
  //                  //  assert(
  //                  //    myExtLeft.modMemWord
  //                  //    === myExtLeft.rdMemWord(0) + 1
  //                  //  )
  //                  //}
  //                  opInfo.cpyOp.get match {
  //                    case CpyOpKind.Cpy => {
  //                      when (myExtLeft.modMemWordValid) {
  //                        assert(
  //                          myExtLeft.modMemWord
  //                          === selRdMemWord(1)
  //                        )
  //                      }
  //                    }
  //                    case CpyOpKind.Cpyu => {
  //                      when (myExtLeft.modMemWordValid) {
  //                        assert(
  //                          myExtLeft.modMemWord(
  //                            cfg.mainWidth - 1
  //                            downto (cfg.mainWidth >> 1)
  //                          ) === (
  //                            selRdMemWord(1)(
  //                              (cfg.mainWidth >> 1) - 1
  //                              downto 0
  //                            )
  //                          )
  //                        )
  //                        assert(
  //                          myExtLeft.modMemWord(
  //                            (cfg.mainWidth >> 1) - 1
  //                            downto 0
  //                          ) === (
  //                            selRdMemWord(0)(
  //                              (cfg.mainWidth >> 1) - 1
  //                              downto 0
  //                            )
  //                          )
  //                        )
  //                      }
  //                    }
  //                    case CpyOpKind.Jmp => {
  //                    }
  //                    case CpyOpKind.Br => {
  //                    }
  //                  }
  //                }
  //                case OpSelect.Alu => {
  //                  assert(stable(myExtLeft.modMemWord))
  //                  opInfo.aluOp.get match {
  //                    //case AluOpKind.Adc => {
  //                    //  assert(
  //                    //    false,
  //                    //    s"not yet implemented: "
  //                    //    + s"opInfo(${opInfo}) index:${opInfoIdx}"
  //                    //  )
  //                    //}
  //                    //case AluOpKind.Sbc => {
  //                    //  assert(
  //                    //    false,
  //                    //    s"not yet implemented: "
  //                    //    + s"opInfo(${opInfo}) index:${opInfoIdx}"
  //                    //  )
  //                    //}
  //                    case op => {
  //                      val result = op.binopFunc(
  //                        cfg=cfg,
  //                        left=selRdMemWord(1),
  //                        right=selRdMemWord(2),
  //                        carry=False,
  //                      )(
  //                      )
  //                      when (myExtLeft.modMemWordValid) {
  //                        assert(
  //                          myPsExSetOutpModMemWordIo.modMemWord(0)
  //                          === result.main
  //                        )
  //                      }
  //                    }
  //                  }
  //                }
  //                case OpSelect.MultiCycle => {
  //                  var found: Boolean = false
  //                  for (
  //                    ((_, multiCycleOpInfo), busIdx)
  //                    <- cfg.multiCycleOpInfoMap.view.zipWithIndex
  //                  ) {
  //                    if (opInfo == multiCycleOpInfo) {
  //                      //modIo.multiCycleBusVec(busIdx)
  //                      found = true
  //                    }
  //                  }
  //                  assert(
  //                    found,
  //                    s"eek! ${opInfo.select} ${opInfo.multiCycleOp.get}"
  //                  )
  //                  //opInfo.multiCycleOp.get match {
  //                  //  case MultiCycleOpKind.Umul => {
  //                  //    when (myExtLeft.modMemWordValid) {
  //                  //      assert(
  //                  //        myPsExSetOutpModMemWordIo.modMemWord(0)
  //                  //        === (
  //                  //          (selRdMemWord(1) * selRdMemWord(2))(
  //                  //            cfg.mainWidth - 1 downto 0
  //                  //          )
  //                  //        )
  //                  //      )
  //                  //    }
  //                  //  }
  //                  //  case _ => {
  //                  //    assert(
  //                  //      false,
  //                  //      s"not yet implemented: "
  //                  //      + s"opInfo(${opInfo}) index:${opInfoIdx}"
  //                  //    )
  //                  //  }
  //                  //}
  //                }
  //                case _ => {
  //                }
  //              }
  //            }
  //          }
  //        }
  //      }
  //      assert(stable(midModPayload(extIdxUp).op))
  //      assert(stable(midModPayload(extIdxUp).opCnt))
  //    }
  //  }
  //}
  //--------
  //when (!myShouldIgnoreInstr) {
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
      def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
      val myExtLeft = tempExtLeft(ydx=ydx)
      val myExtRight = tempExtRight(ydx=ydx)
      myExtLeft.allowOverride
      myExtLeft.valid := (
        cMidModFront.up.isValid
      )
      myExtLeft.ready := (
        cMidModFront.up.isReady
      )
      myExtLeft.fire := (
        cMidModFront.up.isFiring
      )
    }
  //}
  //val myModMemWord = (
  //  modFront(modFrontPayload).myExt(0).rdMemWord
  //  (
  //    PipeMemRmw.modWrIdx
  //  )
  //  + 1
  //  //+ 2
  //)
  //def myProveNumCycles = PipeMemRmwFormal.myProveNumCycles

  when (
    //midModPayload(extIdxUp).op
    ////modFront(modFrontPayload).op
    //=== PipeMemRmwSimDut.ModOp.LdrRaRb
    midModPayload(extIdxUp).decodeExt.opIsMemAccess
    && !myShouldIgnoreInstr
  ) {
    def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
    def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
    //--------
    //midModPayload(extIdxUp).myExt.foreach(
    //  someExt => {
    //    someExt.modMemWordValid := (
    //      //False
    //      (
    //        midModPayload(extIdxUp).decodeExt.memAccessKind
    //        =/= SnowHouseMemAccessKind.Store
    //      ) && (
    //        // TODO: support more destination GPRs
    //        !midModPayload(extIdxUp).gprIsZeroVec(0)
    //      )
    //    )
    //  }
    //)
    when (
      //savedPsMemStallHost.myDuplicateIt
      //&& (
      //  modFront(modFrontPayload).instrCnt.any
      //  === midModPayload(extIdxUp).instrCnt.any + 1
      //)
      io.dbus.rValid && !io.dbus.ready
    ) {
      //--------
      cMidModFront.duplicateIt()
      //--------
      //--------
      val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          midModPayload(extIdxUp).myExt(
            //U(s"${log2Up(cfg.shRegFileCfg.howToSlice.size).max(1)}'d0")
            0
          )
        ) else (
          midModPayload(extIdxUp).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      myCurrExt.modMemWordValid := False
    } otherwise {
      ////--------
      //midModPayload(extIdxUp).myExt(0).modMemWordValid := True
      ////--------
      //midModPayload(extIdxUp).myExt(0).modMemWord := (
      //  //if (PipeMemRmwSimDut.allModOpsSameChange) (
      //  //  midModPayload(extIdxUp).myExt(0).rdMemWord(0) + 1
      //  //) else (
      //  //  midModPayload(extIdxUp).myExt(0).rdMemWord(0) - 1
      //  //)
      //)
      val myDecodeExt = midModPayload(extIdxUp).decodeExt
      //for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      //  val myExtLeft = tempExtLeft(ydx=ydx)
      //  myExtLeft.modMemWord := myExtLeft.modMemWord.getZero
      //}
      val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          midModPayload(extIdxUp).myExt(
            //U(s"${log2Up(cfg.shRegFileCfg.howToSlice.size).max(1)}'d0")
            0
          )
        ) else (
          midModPayload(extIdxUp).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      when (midModPayload(extIdxUp).gprIsZeroVec(0)) {
      } otherwise {
      }
      myCurrExt.modMemWordValid := (
        // TODO: support more destination GPRs
        //!midModPayload(extIdxUp).gprIsZeroVec(0)
        True
      )
      if (cfg.optFormal) {
        assume(
          myDecodeExt.memAccessKind.asBits.asUInt
          <= SnowHouseMemAccessKind.Store.asBits.asUInt
        )
      }
    }
    val mapElem = midModPayload(extIdxUp).gprIdxToMemAddrIdxMap(0)
    val myCurrExt = (
      if (!mapElem.haveHowToSetIdx) (
        midModPayload(extIdxUp).myExt(
          //U(s"${log2Up(cfg.shRegFileCfg.howToSlice.size).max(1)}'d0")
          0
        )
      ) else (
        midModPayload(extIdxUp).myExt(
          mapElem.howToSetIdx
        )
      )
    )
    val myDecodeExt = midModPayload(extIdxUp).decodeExt
    switch (myDecodeExt.memAccessKind) {
      is (SnowHouseMemAccessKind.LoadU) {
        //when (midModPayload(extIdxUp).gprIsZeroVec(0)) {
        //myCurrExt.modMemWordValid := (
        //  // TODO: support more destination GPRs
        //  !midModPayload(extIdxUp).gprIsZeroVec(0)
        //)
        //}
        when (!midModPayload(extIdxUp).gprIsZeroVec(0)) {
          switch (myDecodeExt.memAccessSubKind) {
            is (SnowHouseMemAccessSubKind.Sz8) {
              if (cfg.mainWidth >= 8) {
                myCurrExt.modMemWord := (
                  io.dbus.devData.data.resized
                )
              }
            }
            is (SnowHouseMemAccessSubKind.Sz16) {
              if (cfg.mainWidth >= 16) {
                myCurrExt.modMemWord := (
                  io.dbus.devData.data.resized
                )
              }
            }
            is (SnowHouseMemAccessSubKind.Sz32) {
              if (cfg.mainWidth >= 32) {
                myCurrExt.modMemWord := (
                  io.dbus.devData.data.resized
                )
              }
            }
            is (SnowHouseMemAccessSubKind.Sz64) {
              if (cfg.mainWidth >= 64) {
                myCurrExt.modMemWord := (
                  io.dbus.devData.data.resized
                )
              }
            }
          }
        } otherwise {
          myCurrExt.modMemWord := (
            myCurrExt.rdMemWord(PipeMemRmw.modWrIdx)
          )
        }
      }
      is (SnowHouseMemAccessKind.LoadS) {
        //myCurrExt.modMemWordValid := (
        //  //True
        //  // TODO: support more destination GPRs
        //  !midModPayload(extIdxUp).gprIsZeroVec(0)
        //)
        when (!midModPayload(extIdxUp).gprIsZeroVec(0)) {
          switch (myDecodeExt.memAccessSubKind) {
            is (SnowHouseMemAccessSubKind.Sz8) {
              if (cfg.mainWidth >= 8) {
                val temp = SInt(cfg.mainWidth bits)
                temp := (
                  io.dbus.devData.data(7 downto 0).asSInt.resized
                )
                myCurrExt.modMemWord := (
                  temp.asUInt
                )
              }
            }
            is (SnowHouseMemAccessSubKind.Sz16) {
              if (cfg.mainWidth >= 16) {
                val temp = SInt(cfg.mainWidth bits)
                temp := (
                  io.dbus.devData.data(15 downto 0).asSInt.resized
                )
                myCurrExt.modMemWord := (
                  temp.asUInt
                )
              }
            }
            is (SnowHouseMemAccessSubKind.Sz32) {
              if (cfg.mainWidth >= 32) {
                val temp = SInt(cfg.mainWidth bits)
                temp := (
                  io.dbus.devData.data(31 downto 0).asSInt.resized
                )
                myCurrExt.modMemWord := (
                  temp.asUInt
                )
              }
            }
            is (SnowHouseMemAccessSubKind.Sz64) {
              if (cfg.mainWidth >= 64) {
                val temp = SInt(cfg.mainWidth bits)
                temp := (
                  io.dbus.devData.data(63 downto 0).asSInt.resized
                )
                myCurrExt.modMemWord := (
                  temp.asUInt
                )
              }
            }
          }
        } otherwise {
          myCurrExt.modMemWord := (
            myCurrExt.rdMemWord(PipeMemRmw.modWrIdx)
          )
        }
      }
      is (SnowHouseMemAccessKind.Store) {
        ////myCurrExt.modMemWordValid := False //True
        //when (!myDecodeExt.memAccessIsPush) {
          myCurrExt.modMemWord := (
            myCurrExt.rdMemWord(PipeMemRmw.modWrIdx)
          )
        //} otherwise {
        //  //myCurrExt.modMemWord := (
        //  //  myCurrExt.rdMemWord(PipeMemRmw.modRdIdxStart)
        //  //  + (cfg.mainWidth / 8)
        //  //)
        //}

        //otherwise {
        //  //myCurrExt.memAddr(PipeMemRmw.modWrIdx) := (
        //  //  myCurrExt.memAddr(PipeMemRmw.modRdIdxStart)
        //  //)
        //  //myCurrExt.modMemWord := (
        //  //  myCurrExt.rdMemWord(PipeMemRmw.modRdIdxStart)
        //  //  - (cfg.mainWidth / 8)
        //  //)
        //}
      }
    }
    //--------
  }
  if (cfg.optFormal) {
    when (
      pastValidAfterReset
      && !myShouldIgnoreInstr
    ) {
      def tempMyFindFirstUp(
        ydx: Int,
        zdx: Int,
      ) = (
        regFile.cMid0FrontArea.myFindFirst_0(ydx)(zdx)(extIdxUp),
        regFile.cMid0FrontArea.myFindFirst_1(ydx)(zdx)(extIdxUp),
      )
      def tempMyFindFirstSaved(
        ydx: Int,
        zdx: Int,
      ) = (
        regFile.cMid0FrontArea.myFindFirst_0(ydx)(zdx)(extIdxSaved),
        regFile.cMid0FrontArea.myFindFirst_1(ydx)(zdx)(extIdxSaved),
      )
      val myUpExtDel = (
        regFile.mod.front.myUpExtDel
      )
      val myUpExtDel2 = (
        regFile.mod.front.myUpExtDel2
      )
      println(
        s"${myUpExtDel2.size}"
      )
      when (
        past(front.isFiring)
        && front.isValid
      ) {
        when (
          front(frontPayload).opCnt
          =/= past(front(frontPayload).opCnt)
        ) {
          assert(
            front(frontPayload).opCnt
            === past(front(frontPayload).opCnt) + 1
          )
        }
      }
      val myTempUpMod = regFile.cMid0FrontArea.tempUpMod(2)
      //when (!psWb.tempHadFrontIsFiring._1) {
      //  //--------
      //  for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      //    assert(!myTempUpMod.myExt(ydx).modMemWordValid)
      //    assert(!modFront(modFrontPayload).myExt(ydx).modMemWordValid)
      //    assert(!modBack(modBackPayload).myExt(ydx).modMemWordValid)
      //  }
      //  //--------
      //  assert(!psWb.myHaveAnyCurrWrite)

      //  assert(!psWb.tempHadMid0FrontUpIsValid._1)
      //  assert(!psWb.tempHadMid0FrontDownIsFiring._1)
      //  assert(!psWb.tempHadModFrontIsValid._1)
      //  assert(!psWb.tempHadModBackIsFiring._1)
      //  assert(!psWb.tempHadBackIsFiring._1)
      //  assert(!psWb.myHaveSeenPipeToWrite.sFindFirst(_ === True)._1)
      //  for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      //    for (zdx <- 0 until cfg.regFileModRdPortCnt) {
      //      assert(!tempMyFindFirstUp(ydx, zdx)._1)
      //      assert(!tempMyFindFirstSaved(ydx, zdx)._1)
      //    }
      //  }
      //  assert(!regFile.cMid0FrontArea.up.isValid)
      //  assert(!modFront.isValid)
      //  assert(!modBack.isValid)
      //  assert(!back.isValid)
      //}
      //when (!psWb.tempHadMid0FrontUpIsValid._1) {
      //  assert(!psWb.myHaveAnyCurrWrite)
      //  when (!regFile.cMid0FrontArea.up.isValid) {
      //    //--------
      //    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      //      assert(!modFront(modFrontPayload).myExt(ydx).modMemWordValid)
      //      assert(!midModPayload(extIdxUp).myExt(ydx).modMemWordValid)
      //      for (zdx <- 0 until cfg.regFileModRdPortCnt) {
      //        assert(!tempMyFindFirstUp(ydx, zdx)._1)
      //        assert(!tempMyFindFirstSaved(ydx, zdx)._1)
      //      }
      //    }
      //    //--------
      //    //assert(!tempMyFindFirstUp._1)
      //    //assert(!tempMyFindFirstSaved._1)
      //  }
      //  for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      //    assert(!modBack(modBackPayload).myExt(ydx).modMemWordValid)
      //  }
      //  assert(!psWb.tempHadMid0FrontDownIsFiring._1)
      //  assert(!psWb.tempHadModFrontIsValid._1)
      //  assert(!psWb.tempHadModBackIsValid._1)
      //  assert(!psWb.tempHadModBackIsFiring._1)
      //  assert(!psWb.myHaveSeenPipeToWrite.sFindFirst(_ === True)._1)
      //  assert(!psWb.tempHadBackIsFiring._1)
      //  assert(!modFront.isValid)
      //  assert(!modBack.isValid)
      //  assert(!back.isValid)
      //} 
      //when (!psWb.tempHadModFrontIsValid._1) {
      //  //--------
      //  assert(!psWb.tempHadModBackIsValid._1)
      //  assert(!psWb.tempHadModBackIsFiring._1)
      //  assert(!psWb.myHaveSeenPipeToWrite.sFindFirst(_ === True)._1)
      //  assert(!psWb.tempHadBackIsFiring._1)
      //  assert(!modBack.isValid)
      //  assert(!back.isValid)
      //  //--------
      //  when (modFront.isValid) {
      //    when (pastValidAfterReset) {
      //      assert(
      //        psWb.tempHadMid0FrontDownIsValid._1
      //        || regFile.cMid0FrontArea.down.isValid
      //        || (
      //          past(regFile.cMid0FrontArea.down.isValid) init(False)
      //        )
      //      )
      //    }
      //  } otherwise {
      //    assert(!psWb.tempHadBackIsValid._1)
      //    assert(!psWb.myHaveAnyCurrWrite)
      //  }
      //  //--------
      //}
      //when (!psWb.tempHadModBackIsValid._1) {
      //  when (!modBack.isValid) {
      //    assert(
      //      !regFile.mod.back.myWriteEnable.foldLeft(False)(
      //        (l, r) => (l || r)
      //      )
      //    )
      //    assert(!psWb.myHaveAnyCurrWrite)
      //  }
      //  when (!modBack.isValid) {
      //    assert(!back.isValid)
      //    assert(!psWb.myHaveAnyCurrWrite)
      //    assert(!psWb.tempHadBackIsValid._1)
      //    assert(!back.isValid)
      //    assert(!back.isFiring)
      //    assert(!psWb.tempHadBackIsFiring._1)
      //  }
      //  assert(!psWb.tempHadModBackIsFiring._1)
      //} otherwise {
      //  assert(psWb.tempHadMid0FrontDownIsValid._1)
      //}
      //when (!psWb.tempHadModBackIsFiring._1) {
      //} otherwise {
      //  assert(
      //    psWb.tempHadMid0FrontDownIsValid._1
      //    || regFile.cMid0FrontArea.down.isValid
      //    || (
      //      past(regFile.cMid0FrontArea.down.isValid) init(False)
      //    )
      //  )
      //}
      //when (
      //  cMidModFront.up.isValid
      //  && psWb.tempHadFrontIsFiring._1
      //  && psWb.tempHadMid0FrontUpIsValid._1
      //  && psWb.tempHadMid0FrontDownIsValid._1
      //) {
      //  def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
      //  for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      //    val myExtLeft = tempExtLeft(ydx=ydx)
      //    for (extIdx <- 0 until extIdxLim) {
      //      when (
      //        if (extIdx == extIdxUp) (
      //          cMidModFront.up.isFiring
      //        ) else ( // if (extIdx == extIdxSaved)
      //          False
      //        )
      //      ) {
      //        switch (midModPayload(extIdx).op) {
      //          for (
      //            ((_, opInfo), opInfoIdx)
      //            <- cfg.opInfoMap.view.zipWithIndex
      //          ) {
      //            val myPsExSetOutpModMemWordIo = (
      //              midModPayload(extIdx).psExSetOutpModMemWordIo
      //            )
      //            def selRdMemWord(
      //              idx: Int,
      //            ): UInt = {
      //              myPsExSetOutpModMemWordIo.selRdMemWord(
      //                opInfo=opInfo,
      //                idx=idx,
      //              )
      //            }
      //            is (opInfoIdx) {
      //              opInfo.select match {
      //                case OpSelect.Cpy if (
      //                  opInfo.memAccess == MemAccessKind.NoMemAccess
      //                ) => {
      //                  opInfo.cpyOp.get match {
      //                    case CpyOpKind.Cpy => {
      //                      when (myExtLeft.modMemWordValid) {
      //                        assert(
      //                          myExtLeft.modMemWord
      //                          === selRdMemWord(1)
      //                        )
      //                      }
      //                    }
      //                    case CpyOpKind.Cpyu => {
      //                      when (myExtLeft.modMemWordValid) {
      //                        assert(
      //                          myExtLeft.modMemWord(
      //                            cfg.mainWidth - 1
      //                            downto (cfg.mainWidth >> 1)
      //                          ) === (
      //                            selRdMemWord(1)(
      //                              (cfg.mainWidth >> 1) - 1
      //                              downto 0
      //                            )
      //                          )
      //                        )
      //                        assert(
      //                          myExtLeft.modMemWord(
      //                            (cfg.mainWidth >> 1) - 1
      //                            downto 0
      //                          ) === (
      //                            selRdMemWord(0)(
      //                              (cfg.mainWidth >> 1) - 1
      //                              downto 0
      //                            )
      //                          )
      //                        )
      //                      }
      //                    }
      //                    case CpyOpKind.Jmp => {
      //                    }
      //                    case CpyOpKind.Br => {
      //                    }
      //                  }
      //                }
      //                case OpSelect.Alu => {
      //                  opInfo.aluOp.get match {
      //                    //case AluOpKind.Adc => {
      //                    //  assert(
      //                    //    false,
      //                    //    s"not yet implemented: "
      //                    //    + s"opInfo(${opInfo}) index:${opInfoIdx}"
      //                    //  )
      //                    //}
      //                    //case AluOpKind.Sbc => {
      //                    //  assert(
      //                    //    false,
      //                    //    s"not yet implemented: "
      //                    //    + s"opInfo(${opInfo}) index:${opInfoIdx}"
      //                    //  )
      //                    //}
      //                    case op => {
      //                      val result = op.binopFunc(
      //                        cfg=cfg,
      //                        left=selRdMemWord(1),
      //                        right=selRdMemWord(2),
      //                        carry=False,
      //                      )(
      //                      )
      //                      when (myExtLeft.modMemWordValid) {
      //                        assert(
      //                          myPsExSetOutpModMemWordIo.modMemWord(0)
      //                          === result.main
      //                        )
      //                      }
      //                    }
      //                  }
      //                }
      //                case OpSelect.MultiCycle => {
      //                  opInfo.multiCycleOp.get match {
      //                    case MultiCycleOpKind.Umul => {
      //                      when (myExtLeft.modMemWordValid) {
      //                        assert(
      //                          myPsExSetOutpModMemWordIo.modMemWord(0)
      //                          === (
      //                            (selRdMemWord(1) * selRdMemWord(2))(
      //                              cfg.mainWidth - 1 downto 0
      //                            )
      //                          )
      //                        )
      //                      }
      //                    }
      //                    case _ => {
      //                      assert(
      //                        false,
      //                        s"not yet implemented: "
      //                        + s"opInfo(${opInfo}) index:${opInfoIdx}"
      //                      )
      //                    }
      //                  }
      //                }
      //                case _ => {
      //                }
      //              }
      //            }
      //          }
      //          //is (PipeMemRmwSimDut.ModOp.AddRaRb) {
      //          //  when (midModPayload(extIdx).myExt(0).modMemWordValid) {
      //          //    assert(
      //          //      midModPayload(extIdx).myExt(0).modMemWord
      //          //      === midModPayload(extIdx).myExt(0).rdMemWord(0) + 1
      //          //    )
      //          //  }
      //          //}
      //          //is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
      //          //  when (
      //          //    midModPayload(extIdx).myExt(0).modMemWordValid
      //          //  ) {
      //          //    if (PipeMemRmwSimDut.allModOpsSameChange) {
      //          //      assert(
      //          //        midModPayload(extIdx).myExt(0).modMemWord
      //          //        === (
      //          //          midModPayload(extIdx).myExt(0).rdMemWord(0)
      //          //          + 1
      //          //        )
      //          //      )
      //          //    } else {
      //          //      assert(
      //          //        midModPayload(extIdx).myExt(0).modMemWord
      //          //        === (
      //          //          midModPayload(extIdx).myExt(0).rdMemWord(0)
      //          //          - 1
      //          //        )
      //          //      )
      //          //    }
      //          //  }
      //          //}
      //          //is (PipeMemRmwSimDut.ModOp.MulRaRb) {
      //          //  when (midModPayload(extIdx).myExt(0).modMemWordValid) {
      //          //    if (PipeMemRmwSimDut.allModOpsSameChange) {
      //          //      assert(
      //          //        midModPayload(extIdx).myExt(0).modMemWord
      //          //        === (
      //          //          midModPayload(extIdx).myExt(0).rdMemWord(0)
      //          //          + 1
      //          //        )
      //          //      )
      //          //    } else {
      //          //      val tempBitsRange = (
      //          //        //wordType().bitsRange
      //          //        cfg.mainWidth - 1 downto 0
      //          //      )
      //          //      assert(
      //          //        midModPayload(extIdx).myExt(0).modMemWord(
      //          //          tempBitsRange
      //          //        ) === (
      //          //          midModPayload(extIdx).myExt(0).rdMemWord(0)
      //          //          << 1
      //          //        )(
      //          //          tempBitsRange
      //          //        )
      //          //      )
      //          //    }
      //          //  }
      //          //}
      //        }
      //      }
      //    }
      //  }
      //}
    }
  }

  def setMidModStages(): Unit = {
    regFile.io.midModStages(0) := midModPayload
  }
  setMidModStages()

  //when (cMidModFront.up.isFiring) {
    modFront(modBackPayload) := midModPayload(extIdxUp)
  //}
  when (modFront.isValid) {
    //modFront(modBackPayload) := midModPayload(extIdxUp)
  } otherwise {
  }
}
case class SnowHousePipeStageWriteBack(
  args: SnowHousePipeStageArgs,
  //psEx: SnowHousePipeStageExecute,
  //regFile: PipeMemRmw[
  //  UInt,
  //  Bool,
  //  SnowHousePipePayload,
  //  PipeMemRmwDualRdTypeDisabled[UInt, Bool],
  //],
) extends Area {
  def cfg = args.cfg
  def regFile = args.regFile
  def front = regFile.io.front
  def frontPayload = regFile.io.frontPayload
  def modFront = regFile.io.modFront
  //def modFront = doModInModFrontParams.modFront
  def modFrontPayload = regFile.io.modFrontPayload
  def modBack = regFile.io.modBack
  def modBackPayload = regFile.io.modBackPayload
  def back = regFile.io.back
  def backPayload = regFile.io.backPayload
  def tempModFrontPayload = (
    //doModInModFrontParams.tempModFrontPayload//Vec(ydxr
    regFile.io.tempModFrontPayload
  )
  //--------
  val myHaveCurrWrite = Vec[Bool]({
    val tempArr = ArrayBuffer[Bool]()
    for (ydx <- 0 until regFile.memArrSize) {
      tempArr += (
        pastValidAfterReset
        && modBack.isFiring
        && regFile.mod.back.myWriteEnable(ydx)
      )
    }
    tempArr
  })

  val myHaveAnyCurrWrite = (
    //myHaveCurrWrite.foldLeft(False)(
    //  (left, right) => (left || right)
    //)
    //myHaveCurrWrite.sFindFirst(_ === True)._1
    myHaveCurrWrite.reduceLeft(_ || _)
  )
  val tempLeft = (
    regFile.mod.back.myWriteData
  )
  val tempHadFrontIsFiring: (Bool, Bool) = (
    RegNextWhen[Bool](
      next=True,
      cond=front.isFiring,
      init=False,
    ),
    null
  )
  val tempHadMid0FrontUpIsValid: (Bool, Bool) = (
    {
      val cond = (
        regFile.cMid0FrontArea.up.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False,
      )
    },
    null
  )
  val tempHadMid0FrontDownIsValid: (Bool, UInt) = (
    {
      val cond = (
        regFile.cMid0FrontArea.down.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || regFile.cMid0FrontArea.up.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  val tempHadMid0FrontDownIsFiring: (Bool, UInt) = (
    {
      val cond = (
        regFile.cMid0FrontArea.down.isFiring
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || regFile.cMid0FrontArea.up.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  val tempHadModFrontIsValid: (Bool, Bool) = (
    {
      val cond = (
        modFront.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || regFile.cMid0FrontArea.up.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  if (cfg.optFormal) {
    when (!tempHadModFrontIsValid._1) {
      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        val wordCount = cfg.regFileWordCountArr(ydx)
        for (zdx <- 0 until cfg.regFileModRdPortCnt) {
          assume(
            (
              regFile.modMem(ydx)(zdx).readAsync(
                address=U(s"${log2Up(wordCount)}'d${zdx}")
              )
            ) === (
              0x0
            )
          )
        }
      }
    }
  }
  val tempHadModBackIsFiring: (Bool, Bool) = (
    {
      val cond = (
        modBack.isFiring
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || regFile.cMid0FrontArea.up.isValid
        ) && (
          tempHadModFrontIsValid._1
          || modFront.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  val tempHadModBackIsValid: (Bool, Bool) = (
    {
      val cond = (
        modBack.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || regFile.cMid0FrontArea.up.isValid
        ) && (
          tempHadModFrontIsValid._1
          || modFront.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  val tempHadBackIsFiring: (Bool, Bool) = (
    {
      val cond = (
        back.isFiring
        //&& tempHadFrontIsFiring._1
        //&& tempHadMid0FrontUpIsValid._1
        //&& tempHadModFrontIsValid._1
        //&& tempHadModBackIsFiring._1
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || regFile.cMid0FrontArea.up.isValid
        ) && (
          tempHadModFrontIsValid._1
          || modFront.isValid
        ) && (
          tempHadModBackIsValid._1
          || modBack.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  val tempHadBackIsValid: (Bool, Bool) = (
    {
      val cond = (
        back.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || regFile.cMid0FrontArea.up.isValid
        ) && (
          tempHadModFrontIsValid._1
          || modFront.isValid
        ) && (
          tempHadModBackIsValid._1
          || modBack.isValid
        ) && (
          tempHadModBackIsFiring._1
          || modBack.isFiring
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  val myHaveSeenPipeToModFrontFire = (
    KeepAttribute(
      tempHadFrontIsFiring._1
      && tempHadMid0FrontUpIsValid._1
      && tempHadModFrontIsValid._1
    )
    .setName(s"myHaveSeenPipeToModFrontFire")
  )
  val myHaveSeenPipeToWrite = Vec[Bool]({
    val tempArr = ArrayBuffer[Bool]()
    for (tempHaveCurrWrite <- myHaveCurrWrite.view) {
      tempArr += (
        myHaveSeenPipeToModFrontFire
        && tempHaveCurrWrite
      )
    }
    tempArr
  })
  def getMyHistHaveSeenPipeToWriteVecCond(
    ydx: Int,
    idx: Int,
  ) = (
    myHaveSeenPipeToWrite(ydx)
    && (
      regFile.mod.back.myWriteAddr(ydx) === idx
    )
  )
  def tempHistHaveSeenPipeToWriteV2dOuterDim = (
    //3
    4
  )
  val tempHaveSeenPipeToWriteV2dFindFirst_0 = (
    KeepAttribute(
      Vec.fill(tempHistHaveSeenPipeToWriteV2dOuterDim)({
        //Vec.fill(wordCount)(
        //  Bool()
        //)
        val temp = ArrayBuffer[Vec[Bool]]()
        for (wordCount <- cfg.regFileWordCountArr.view) {
          temp += Vec.fill(wordCount)(
            Bool()
          )
        }
        Vec[Vec[Bool]](temp)
      })
    )
    .setName(s"tempHaveSeenPipeToWriteV2dFindFirst_0")
  )
  for ((wordCount, ydx) <- cfg.regFileWordCountArr.view.zipWithIndex) {
    for (idx <- 0 until wordCount) {
      for (jdx <- 0 until tempHistHaveSeenPipeToWriteV2dOuterDim) {
        def tempFunc(
          someJdx: Int
        ) = (
          tempHaveSeenPipeToWriteV2dFindFirst_0(someJdx)(ydx)
        )
        if (jdx == 0) {
          tempFunc(jdx)(idx) := (
            getMyHistHaveSeenPipeToWriteVecCond(
              ydx=ydx,
              idx=idx,
            )
            //RegNextWhen(
            //)
          )
        } else {
          tempFunc(jdx)(idx) := (
            RegNext(
              next=tempFunc(jdx)(idx),
              init=tempFunc(jdx)(idx).getZero,
            )
          )
          when (tempFunc(jdx - 1)(idx)) {
            tempFunc(jdx)(idx) := (
              RegNext(
                next=tempFunc(jdx - 1)(idx),
                init=tempFunc(jdx)(idx).getZero,
              )
            )
          }
        }
      }
    }
  }
  //if (cfg.optFormal) {
  //  when (pastValidAfterReset) {
      val rPrevOpCnt = Vec({
        val tempArr = ArrayBuffer[UInt]()
        for (ydx <- 0 until regFile.memArrSize) {
          tempArr += (
            RegNextWhen(
              next=modBack(modBackPayload).opCnt,
              //modBack.isFiring && pipeMem.mod.back.myWriteEnable(0)
              cond=myHaveCurrWrite(ydx),
            )
            .init(0x0)
            .setName(s"rPrevOpCnt_${ydx}")
          )
        }
        tempArr
      })
      for ((rPrevOpCntElem, ydx) <- rPrevOpCnt.view.zipWithIndex) {
        assumeInitial(
          rPrevOpCntElem === 0x0
        )
      }
      val myCoverCond = (
        //modBack.isFiring
        //&& pipeMem.mod.back.myWriteEnable(0)
        myHaveAnyCurrWrite
      )
      def myCoverVecSize = 8
      val tempMyCoverInit = SnowHousePipePayload(cfg=cfg)
      tempMyCoverInit.allowOverride
      tempMyCoverInit := tempMyCoverInit.getZero
      //tempMyCoverInit.op := PipeMemRmwSimDut.ModOp.LIM
      val myHistCoverVec = (
        KeepAttribute(
          History(
            that=modBack(modBackPayload),
            length=myCoverVecSize,
            when=myCoverCond,
            init=tempMyCoverInit,
          )
        )
      )
  //  }
  //}
  val myHadWriteAt = (
    Vec({
      val tempArr = ArrayBuffer[Vec[Bool]]()
      for ((wordCount, ydx) <- cfg.regFileWordCountArr.view.zipWithIndex) {
        tempArr += (
          Vec.fill(wordCount)(
            Bool()
          )
        )
      }
      tempArr
    })
  )
  val myPrevWriteData = (
    KeepAttribute(
      Vec[Vec[UInt]]({
        val tempArr = ArrayBuffer[Vec[UInt]]()
        for ((wordCount, ydx) <- regFile.wordCountArr.view.zipWithIndex) {
          tempArr += {
            val myArr = new ArrayBuffer[UInt]()
            for (idx <- 0 until wordCount) {
              myArr += (
                //wordType()
                UInt(cfg.mainWidth bits)
              )
            }
            Vec[UInt](myArr)
          }
        }
        tempArr
      })
    )
    .setName(s"myPrevWriteData")
  )
  for ((wordCount, ydx) <- regFile.wordCountArr.view.zipWithIndex) {
    for (idx <- 0 until wordCount) {
      myHadWriteAt(ydx)(idx) := (
        RegNext(
          next=myHadWriteAt(ydx)(idx),
          init=myHadWriteAt(ydx)(idx).getZero,
        )
      )
      myPrevWriteData(ydx)(idx) := (
        RegNext(
          next=myPrevWriteData(ydx)(idx),
          init=myPrevWriteData(ydx)(idx).getZero,
        )
      )
    }
  }
  def extIdxUp = PipeMemRmw.extIdxUp
  def extIdxSaved = PipeMemRmw.extIdxSaved
  def extIdxLim = PipeMemRmw.extIdxLim
  val formalFwdModBackArea = (regFile.myHaveFormalFwd) generate (
    new Area {
      val myExt = (
        KeepAttribute(
          regFile.mkExt()
        )
        .setName(
          s"formalFwdModBackArea_"
          + s"myExt"
        )
      )
      val myFwd = (
        KeepAttribute(
          Vec.fill(extIdxLim)(
            regFile.mkFwd()
          )
        )
        .setName(
          s"formalFwdModBackArea_"
          + s"myFwd"
        )
      )
      //def tempPayload = (
      //  //Ved.fill(extIdxLim)(
      //  //  modBack(modBackPayload)
      //  //)
      //  regFile.cBackArea.upFwd
      //)
      for (extIdx <- 0 until extIdxLim) {
        for (ydx <- 0 until regFile.memArrSize) {
          myExt(ydx)(extIdx) := regFile.cBackArea.upExt(1)(ydx)(extIdx)
        }
        myFwd(extIdx) := regFile.cBackArea.upFwd(extIdx)
        //myFwd(extIdx).myFindFirst_0.allowOverride
        //myFwd(extIdx).myFindFirst_1.allowOverride
      }
      if (regFile.myHaveFormalFwd) {
        when (pastValidAfterReset) {
          when (
            !RegNextWhen(
              next=True,
              cond=modBack.isFiring,
              init=False,
            )
          ) {
            when (!modBack.isValid) {
              myExt.foreach(someExt => {
                assert(
                  someExt(extIdxUp).main
                  === someExt(extIdxUp).main.getZero
                )
              })
              assert(myFwd(extIdxUp) === myFwd(extIdxUp).getZero)
            }
            myExt.foreach(someExt => {
              assert(someExt(extIdxSaved) === someExt(extIdxSaved).getZero)
            })
            assert(myFwd(extIdxSaved) === myFwd(extIdxSaved).getZero)
          } 
          when (
            past(modBack.isFiring) init(False)
          ) {
            myExt.foreach(someExt => {
              assert(
                someExt(extIdxSaved)
                === (
                  past(someExt(extIdxUp)) init(someExt(extIdxUp).getZero)
                )
              )
            })
            assert(
              myFwd(extIdxSaved)
              === (
                past(myFwd(extIdxUp)) init(myFwd(extIdxUp).getZero)
              )
            )
          }
          when (modBack.isValid) {
            assert(myFwd(extIdxUp) === modBack(modBackPayload).myFwd)
            for (ydx <- 0 until regFile.memArrSize) {
              assert(
                myExt(ydx)(extIdxUp).main
                === modBack(modBackPayload).myExt(ydx).main
              )
            }
          }
        }
      }
      val doFormalFwdUp = (
        PipeMemRmwDoFwdArea(
          fwdAreaName=s"formalFwdModBackArea_doFormalFwdUp",
          fwd=(
            myFwd(extIdxUp)
            //midModPayload(extIdxUp).myFwd
          ),
          setToMyFwdDataFunc=(
            ydx: Int,
            zdx: Int,
            myFwdData: UInt,
          ) => {
            when (pastValidAfterReset) {
              assert(myExt(ydx)(extIdxUp).rdMemWord(zdx) === myFwdData)
            }
          }
        )
      )
      val doFormalFwdSaved =  (
        PipeMemRmwDoFwdArea(
          fwdAreaName=s"formalFwdModBackArea_doFormalFwdSaved",
          fwd=(
            myFwd(extIdxSaved)
          ),
          setToMyFwdDataFunc=(
            ydx: Int,
            zdx: Int,
            myFwdData: UInt,
          ) => {
            when (pastValidAfterReset) {
              assert(myExt(ydx)(extIdxSaved).rdMemWord(zdx) === myFwdData)
            }
          }
        )
      )
    }
  )
  //def myHistMainCond = (
  //  pastValidAfterReset
  //  && modBack.isFiring
  //  //// TODO: verify that `myHaveAnyCurrWrite` is correct here
  //  //&& myHaveAnyCurrWrite
  //  //&& !modBack(modBackPayload).instrCnt.shouldIgnoreInstr
  //)
  case class HistMain(
  ) extends Bundle {
    val myHaveCurrWrite = Vec.fill(regFile.memArrSize)(
      Bool()
    )
    val flow = Flow(SnowHousePipePayload(cfg=cfg))
  }
  def myHistMainSize = 8
  val myHistMain: Vec[HistMain] = (
    KeepAttribute(Vec[HistMain]{
      //History(
      //  that={
      //    //modBack(modBackPayload)
      //    val temp = HistMain()
      //    temp.myHaveCurrWrite := myHaveCurrWrite
      //    temp.flow.payload := modBack(modBackPayload)
      //    temp.flow.valid := True
      //    temp
      //  },
      //  length=myHistMainSize,
      //  when=myHistMainCond,
      //  init=(
      //    //modBack(modBackPayload).getZero
      //    HistMain().getZero
      //  ),
      //)
      //def builder(that: T, left: Int): List[T] = {
      //  left match {
      //    case 0 => Nil
      //    case 1 => that :: Nil
      //    case _ => that :: builder({
      //      if (when != null)
      //        RegNextWhen(that, when, init = init)
      //      else
      //        RegNext(that, init = init)
      //    }, left - 1)
      //  }
      //}
      val tempArr = ArrayBuffer[HistMain]()
      for (idx <- 0 until myHistMainSize) {
        //--------
        def myHistMainCond = (
          pastValidAfterReset
          && modBack.isFiring
          //// TODO: verify that `myHaveAnyCurrWrite` is correct here
          //&& myHaveAnyCurrWrite
          //&& !modBack(modBackPayload).instrCnt.shouldIgnoreInstr
        )
        //--------
        val temp = HistMain()
        temp.myHaveCurrWrite := myHaveCurrWrite
        temp.flow.payload := modBack(modBackPayload)
        temp.flow.valid := True
        //temp
        //--------
        tempArr += (
          if (idx == 0) {
            temp
          } else {
            RegNextWhen(
              next=temp,
              cond=(
                myHistMainCond
                && (
                  tempArr.last.flow.instrCnt.any
                  === temp.flow.instrCnt.any + 1
                )
              ),
              init=HistMain().getZero,
            )
          }
        )
        //--------
      }
      tempArr
    })
  )
  val myHistAssumes = ArrayBuffer[Area]()
  for ((myHistMainElem, myHistMainIdx) <- myHistMain.view.zipWithIndex) {
    myHistAssumes += myHistMainElem.flow.formalAssumes()
  }
  val myHistMainFireFindFirst = myHistMain.sFindFirst(_.flow.fire)
  if (cfg.optFormal) {
    when (
      //RegNextWhen(
      //  next=True,
      //  cond=(
      //    RegNextWhen(
      //      next=True,
      //      cond=myHistMainCond,
      //      init=False,
      //    )
      //  ),
      //  init=False
      //)
      //myHistMain(0).flow.fire
      //|| myHistMain(1).flow.fire
      //|| myHistMain(2).flow.fire
      //|| myHistMain(3).flow.fire
      //myHistMain.reduceLeft(
      //  (left, right) => (
      //    left.flow.fire || right.flow.fire
      //  )
      //)
      myHistMain(0).flow.fire
      && myHistMain(1).flow.fire
      && myHistMain(2).flow.fire
      && myHistMain(3).flow.fire
      //&& myHistMainFireFindFirst._1
    ) {
      def flow0 = myHistMain(0).flow
      def flow1 = myHistMain(1).flow
      def flow2 = myHistMain(2).flow
      def flow3 = myHistMain(3).flow
      switch (flow3.op) {
        for (
          ((_, opInfoJmp), opInfoJmpIdx)
          <- cfg.opInfoMap.view.zipWithIndex
        ) {
          is (opInfoJmpIdx) {
            def jmpSelRdMemWord(
              //someHistMain: HistMain,
              //histMainIdx: Int,
              idx: Int,
            ): UInt = {
              //val someHistMain = myHistMain(3)
              flow3.psExSetOutpModMemWordIo.selRdMemWord(
                opInfo=opInfoJmp,
                idx=idx,
              )
            }
            opInfoJmp.select match {
              case OpSelect.Cpy => {
                def handlePcChange(
                  cond: Bool,
                ): Unit = {
                  //switch (myHistMain(0).flow.op) {
                  //  for (
                  //    ((_, opInfo), opInfoIdx)
                  //    <- cfg.opInfoMap.view.zipWithIndex
                  //  ) {
                  //    is (opInfoIdx) {
                  //      opInfo.select match {
                  //        case OpSelect.Cpy => {
                  //        }
                  //        case OpSelect.Alu => {
                  //        }
                  //        case OpSelect.MultiCycle => {
                  //        }
                  //      }
                  //    }
                  //  }
                  //}
                  assert(flow3.decodeExt.opIsJmp)
                  assert(
                    flow3.psExSetPc.fire === cond
                  )
                  when (cond) {
                    //assert(flow3.decodeExt.opIsJmp)
                    //assert(flow3.formalPsExSetPc.fire)
                    assert(
                      flow2.regPc
                      === flow3.regPc + (cfg.instrMainWidth / 8)
                    )
                    assert(
                      flow1.regPc
                      === flow2.regPc + (cfg.instrMainWidth / 8)
                    )
                    assert(
                      flow0.regPc === flow3.psExSetPc.nextPc
                    )
                    //assert(
                    //  flow2.regPc
                    //  === 
                    //)
                    //assert(
                    //)
                  } otherwise {
                    when (!flow2.decodeExt.opIsJmp) {
                      assert(!flow2.psExSetPc.fire)
                      assert(
                        flow2.regPc
                        === flow3.regPc + (cfg.instrMainWidth / 8)
                      )
                    }
                    when (!flow1.decodeExt.opIsJmp) {
                      assert(!flow1.psExSetPc.fire)
                    }
                    when (!flow0.decodeExt.opIsJmp) {
                      assert(!flow0.psExSetPc.fire)
                    }
                    //assert(
                    //)
                  }
                  assert(
                    flow0.instrCnt.any
                    === flow1.instrCnt.any + 1
                  )
                  assert(
                    flow1.instrCnt.any
                    === flow2.instrCnt.any + 1
                  )
                  assert(
                    flow2.instrCnt.any
                    === flow3.instrCnt.any + 1
                  )
                }
                opInfoJmp.cpyOp.get match {
                  case CpyOpKind.Jmp | CpyOpKind.Br => {
                    opInfoJmp.cond match {
                      case CondKind.Always => {
                        handlePcChange(
                          cond=True
                        )
                      }
                      case CondKind.Eq => {
                        handlePcChange(
                          cond=(
                            jmpSelRdMemWord(0) === jmpSelRdMemWord(1)
                          )
                        )
                      }
                      case CondKind.Ne => {
                        handlePcChange(
                          cond=(
                            jmpSelRdMemWord(0) =/= jmpSelRdMemWord(1)
                          )
                        )
                      }
                      case CondKind.Z => {
                        handlePcChange(
                          cond=(jmpSelRdMemWord(0) === 0)
                        )
                      }
                      case CondKind.Nz => {
                        handlePcChange(
                          cond=(jmpSelRdMemWord(0) =/= 0)
                        )
                      }
                      case cond => {
                        assert(
                          false,
                          s"not yet implemented: "
                          + s"opInfoJmp(${opInfoJmp} ${opInfoJmp.select}) "
                          + s"opInfoJmpIdx:${opInfoJmpIdx} "
                          //+ s"opInfo(${opInfo} ${opInfo.select}) "
                          //+ s"opInfoIdx:${opInfoJmpIdx}; "
                        )
                      }
                    }
                  }
                  //case CpyOpKind.Br => {
                  //  //handleJmp(
                  //  //)
                  //}
                  case _ => {
                  }
                }
              }
              case _ => {
              }
              //case OpSelect.Alu => {
              //}
              //case OpSelect.MultiCycle => {
              //}
            }
          }
        }
      }
    }
    when (pastValidAfterReset) {
      val tempCond = (
        (
          //myHaveCurrWrite
          //past(myHaveSeenPipeToWrite)
          //past(myHaveSeenPipeToModFrontFire)
          //&&
          ///*past*/(modBack.isValid)
          //&& /*past*/(pipeMem.mod.back.myWriteEnable(0))
          myHaveCurrWrite
        )
      )
      //when (
      //  (
      //    RegNextWhen(next=True, cond=myHistMainCond, init=False)
      //  ) && (
      //    //tempCond.foldLeft(False)((left, right) => (left || right))
      //    //tempCond.sFindFirst(_ === True)._1
      //    //tempCond.reduceLeft(_ || _)
      //    myHaveAnyCurrWrite
      //  )
      //) {
      //}
      for ((wordCount, ydx) <- regFile.wordCountArr.view.zipWithIndex) {
        when (past(tempCond(ydx))) {
          for (idx <- 0 until wordCount) {
            when (
              //getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
              past(regFile.mod.back.myWriteAddr(ydx)) === idx
            ) {
              myHadWriteAt(ydx)(idx) := (
                past(True) init(False)
              )
              myPrevWriteData(ydx)(idx) := (
                past(regFile.mod.back.myWriteData(ydx))
              )
            }
          }
        }
        //val tempCond1 = Vec[Bool]({
        //  val tempArr = ArrayBuffer[Bool]()
        //  for (zdx <- 0 until cfg.regFileModRdPortCnt) {
        //    tempArr += (
        //      //modBack.isValid
        //      //&& 
        //      /*past*/(regFile.mod.back.myWriteEnable(ydx))
        //      && (
        //        myHadWriteAt(ydx)(
        //        /*past*/(regFile.mod.back.myWriteAddr(ydx))
        //        )
        //      ) && (
        //        tempHaveSeenPipeToWriteV2dFindFirst_0(0)(ydx)(
        //          regFile.mod.back.myWriteAddr(ydx)
        //        )
        //      ) && (
        //        tempHaveSeenPipeToWriteV2dFindFirst_0(1)(ydx)(
        //          regFile.mod.back.myWriteAddr(ydx)
        //        )
        //      ) && (
        //        tempHaveSeenPipeToWriteV2dFindFirst_0(2)(ydx)(
        //          regFile.mod.back.myWriteAddr(ydx)
        //        )
        //      )
        //    )
        //  }
        //  tempArr
        //})
        val tempCond1 = (
          KeepAttribute(
            //modBack.isValid
            //&& 
            /*past*/(regFile.mod.back.myWriteEnable(ydx))
            && (
              myHadWriteAt(ydx)(
              /*past*/(regFile.mod.back.myWriteAddr(ydx)(
                //log2Up(myHadWriteAt(ydx).size) - 1 downto 0
                log2Up(wordCount) - 1 downto 0
              ))
              )
            ) && (
              tempHaveSeenPipeToWriteV2dFindFirst_0(0)(ydx)(
                regFile.mod.back.myWriteAddr(ydx)(
                  log2Up(wordCount) - 1 downto 0
                )
              )
            ) && (
              tempHaveSeenPipeToWriteV2dFindFirst_0(1)(ydx)(
                regFile.mod.back.myWriteAddr(ydx)(
                  log2Up(wordCount) - 1 downto 0
                )
              )
            ) && (
              tempHaveSeenPipeToWriteV2dFindFirst_0(2)(ydx)(
                regFile.mod.back.myWriteAddr(ydx)(
                  log2Up(wordCount) - 1 downto 0
                )
              )
            )
          )
          .setName(s"tempCond1_${ydx}")
        )
        //val myTempRight = Vec[Vec[UInt]]({
        //  val tempArr = ArrayBuffer[Vec[UInt]]()
        //  for (zdx <- 0 until cfg.regFileModRdPortCnt) {
        //    tempArr += (
        //      Vec[UInt]({
        //        val myArr = new ArrayBuffer[UInt]()
        //        myArr += (
        //          myPrevWriteData(ydx)(
        //            /*past*/(regFile.mod.back.myWriteAddr(ydx)(
        //              log2Up(wordCount) - 1 downto 0
        //            ))
        //          )
        //        )
        //        myArr += (
        //          modBack(modBackPayload).myExt(ydx).rdMemWord(
        //            zdx
        //          )
        //        )
        //        myArr
        //      })
        //    )
        //  }
        //  tempArr
        //})
        val myTempRight = (
          KeepAttribute(
            Vec[UInt]({
              val myArr = new ArrayBuffer[UInt]()
              //myArr += (
              //  myPrevWriteData(ydx)(
              //    /*past*/(
              //      regFile.mod.back.myWriteAddr(ydx)(
              //        log2Up(wordCount) - 1 downto 0
              //      )
              //    )
              //  )
              //)
              myArr += (
                modBack(modBackPayload).myExt(ydx)
                .rdMemWord(PipeMemRmw.modWrIdx)
              )
              myArr
            })
          )
          .setName(s"${regFile.pipeName}_myTempRight_${ydx}")
        )
        when (tempCond1) {
          val myTempLeft = (
            regFile.mod.back.myWriteData
          )
          val myDoFormalAssertRegular = Bool()
          myDoFormalAssertRegular := True
          for ((right, rightIdx) <- myTempRight.view.zipWithIndex) {
            switch (modBack(modBackPayload).op) {
              for (
                ((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex
              ) {
                is (opInfoIdx) {
                  //opInfo
                  val psExOutpTempIo = (
                    modBack(modBackPayload)
                    .psExSetOutpModMemWordIo
                  )
                  def myGpr0 = (
                    psExOutpTempIo.selRdMemWord(opInfo=opInfo, idx=0)
                  )
                  def myLeft = (
                    psExOutpTempIo.selRdMemWord(opInfo=opInfo, idx=1)
                  )
                  def myRight = (
                    psExOutpTempIo.selRdMemWord(opInfo=opInfo, idx=2)
                  )
                  val howToSlice = cfg.shRegFileCfg.howToSlice
                  val tempInfo = (
                    modBack(modBackPayload).gprIdxToMemAddrIdxMap(0)
                  )
                  val result: InstrResult = opInfo.select match {
                    case OpSelect.Cpy => {
                      opInfo.cpyOp.get match {
                        case CpyOpKind.Cpy => {
                          val result = InstrResult(cfg=cfg)()
                          opInfo.memAccess match {
                            case MemAccessKind.NoMemAccess => {
                              result.main := myLeft
                            }
                            case mem: MemAccessKind.Mem => {
                              myDoFormalAssertRegular := False
                              //mem.isStore match {
                              //  case Some(isStore) => {
                              //    if (!isStore) {
                              //      // don't formally verify stores I guess?
                              //      //result.main := myGpr0
                              //      myDoFormalAssert := False
                              //    } else {
                              //    }
                              //  }
                              //  case None => {
                              //    // TODO: support atomics
                              //    assert(
                              //      false,
                              //      s"atomics not supported yet"
                              //    )
                              //  }
                              //}
                            }
                          }
                          result
                        }
                        case CpyOpKind.Cpyu => {
                          val result = InstrResult(cfg=cfg)()
                          result.main.allowOverride
                          result.main := myGpr0
                          result.main(
                            (cfg.mainWidth - 1)
                            downto (cfg.mainWidth >> 1)
                          ) := (
                            myLeft(
                              (cfg.mainWidth >> 1) - 1
                              downto 0
                            )
                          )
                          result
                        }
                        case CpyOpKind.Jmp => {
                          val result = InstrResult(cfg=cfg)()
                          myDoFormalAssertRegular := False
                          //when (
                          //  modBack(modBackPayload).instrCnt
                          //  .shouldIgnoreInstr
                          //) {
                          //}
                          result
                        }
                        case CpyOpKind.Br => {
                          val result = InstrResult(cfg=cfg)()
                          myDoFormalAssertRegular := False
                          //opInfo.cond match {
                          //  case CondKind.Always => {
                          //  }
                          //  case CondKind.Z => {
                          //  }
                          //  case CondKind.Nz => {
                          //  }
                          //  case _ => {
                          //    assert(
                          //      false,
                          //      s"not yet implemented: "
                          //      + s"opInfo(${opInfo} ${opInfo.select}) "
                          //      + s"cond(${opInfo.cond})"
                          //    )
                          //  }
                          //}
                          result
                        }
                        case _ => {
                          assert(
                            false,
                            s"not yet implemented: "
                            + s"opInfo(${opInfo}) idx:${opInfoIdx}"
                          )
                          InstrResult(cfg=cfg)()
                        }
                      }
                    }
                    case OpSelect.Alu => {
                      opInfo.aluOp.get match {
                        //case AluOpKind.Adc => {
                        //  assert(
                        //    false,
                        //    s"not yet implemented: "
                        //    + s"opInfo(${opInfo}) idx:${opInfoIdx}"
                        //  )
                        //  InstrResult(cfg=cfg)()
                        //}
                        //case AluOpKind.Sbc => {
                        //  assert(
                        //    false,
                        //    s"not yet implemented: "
                        //    + s"opInfo(${opInfo}) idx:${opInfoIdx}"
                        //  )
                        //  InstrResult(cfg=cfg)()
                        //}
                        case op => {
                          op.binopFunc(
                            cfg=cfg,
                            left=myLeft,
                            right=myRight,
                            carry=False,
                          )(
                          )
                        }
                      }
                    }
                    case OpSelect.MultiCycle => {
                      opInfo.multiCycleOp.get match {
                        case MultiCycleOpKind.Umul => {
                          val result = InstrResult(cfg=cfg)()
                          result.main := (
                            (myLeft * myRight)(result.main.bitsRange)
                          )
                          result
                        }
                        case _ => {
                          assert(
                            false,
                            s"not yet implemented: "
                            + s"opInfo(${opInfo}) idx:${opInfoIdx}"
                          )
                          InstrResult(cfg=cfg)()
                        }
                      }
                    }
                  }
                  when (myDoFormalAssertRegular) {
                    assert(
                      myTempLeft(
                        if (tempInfo.haveHowToSetIdx) (
                          tempInfo.howToSetIdx
                        ) else (
                          U(s"${log2Up(cfg.regFileCfg.memArrSize)}'d0")
                        )
                      ) === result.main
                    )
                  }
                  //switch (modBack(modBackPayload).gprIdxVec(0)) {
                  //  for (
                  //    (howToSet, howToSetIdx) <- howToSlice.view.zipWithIndex
                  //  ) {
                  //    for ((howTo, howToIdx) <- howToSet.view.zipWithIndex) {
                  //      is (howTo) {
                  //      }
                  //    }
                  //  }
                  //}
                  //assert(
                  //  myTempLeft === binop.main
                  //)
                }
              }
            }
            if ((1 << log2Up(cfg.opInfoMap.size)) != cfg.opInfoMap.size) {
              assume(modBack(modBackPayload).op < cfg.opInfoMap.size)
            }
          }
        }
      }
    }
  }
}
