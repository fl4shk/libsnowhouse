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
case class SnowHousePsExSetPcPayload(
  cfg: SnowHouseConfig
) extends Bundle {
  val nextPc = UInt(cfg.mainWidth bits)
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
  def nextRegPc = upModExt.regPc
  def nextRegPcSetItCnt = upModExt.regPcSetItCnt
  val rSavedExSetPc = {
    val temp = /*KeepAttribute*/(
      Reg(Flow(
        SnowHousePsExSetPcPayload(cfg=cfg)
      ))
    )
    temp.init(temp.getZero)
    temp.setName(s"rSavedExSetPc")
  }

  when (
    psExSetPc.fire
    && !rSavedExSetPc.fire
  ) {
    rSavedExSetPc := psExSetPc
  }
  val rPrevRegPc = (
    RegNextWhen(
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
  )

  when (up.isFiring) {
      myInstrCnt.any := rPrevInstrCnt.any + 1
    when (rSavedExSetPc.fire) {
      rSavedExSetPc := rSavedExSetPc.getZero
      nextRegPcSetItCnt := 0x1
      nextRegPc := (
        rSavedExSetPc.nextPc //- (cfg.instrMainWidth / 8)
      )
        myInstrCnt.jmp := rPrevInstrCnt.jmp + 1
    } otherwise {
      nextRegPcSetItCnt := 0x0
      nextRegPc := rPrevRegPc + (cfg.instrMainWidth / 8)
        myInstrCnt.fwd := rPrevInstrCnt.fwd + 1
    }
  }
  io.ibus.nextValid := (
    True
  )
  io.ibus.sendData.addr := nextRegPc //upModExt.regPc
}
case class SnowHousePipeStageInstrDecode(
  val args: SnowHousePipeStageArgs,
  val psIdHaltIt: Bool,
  val psExSetPc: Flow[SnowHousePsExSetPcPayload],
  val pcChangeState: Bool,
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
  val upPayload = /*Vec.fill(2)*/(
    SnowHousePipePayload(cfg=cfg)
  )
  val startDecode = Bool()

  up(pId) := upPayload//(0)
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
      RegNext(io.ibus.nextValid)
    ) {
      when (!rSetUpPayloadState(1)) {
        when (!io.ibus.ready) {
          cId.haltIt()
        } otherwise {
          nextSetUpPayloadState(1) := True
          myInstr := (
            io.ibus.recvData.instr
          )
        }
      }
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
      when (!rSetUpPayloadState(0)) {
          upPayload := up(pIf)
          nextSetUpPayloadState(0) := True
      }
    }
  } otherwise {
    cId.duplicateIt()
    when (down.isFiring) {
      nextMultiInstrCnt := rMultiInstrCnt - 1
    }
  }
  when (up.isFiring) {
    nextSetUpPayloadState(0) := False
    nextSetUpPayloadState(1) := False
  }
  upPayload.regPcPlusInstrSize := (
    upPayload.regPc + (cfg.instrMainWidth / 8)
  )
  val upGprIdxToMemAddrIdxMap = upPayload.gprIdxToMemAddrIdxMap
  for ((gprIdx, zdx) <- upPayload.gprIdxVec.view.zipWithIndex) {
    upPayload.myExt(0).memAddr(zdx) := gprIdx
  }
  if (cfg.optFormal) {
    when (pastValidAfterReset()) {
      when (
        !past(up.isFiring)
        && io.ibus.ready
      ) {
        assume(stable(io.ibus.ready))
      }
      when (past(io.ibus.nextValid)) {
        when (io.ibus.ready) {
          cover(up.isFiring)
          assert(
            up.isValid
          )
          when (!io.ibus.nextValid) {
            assume(!(RegNext(next=io.ibus.ready, init=False)))
          }
        }
      }
    }
  }
  val nextDoDecodeState = Bool()
  val rDoDecodeState = RegNext(
    next=nextDoDecodeState,
    init=nextDoDecodeState.getZero,
  )
  nextDoDecodeState := rDoDecodeState
  val tempInstr = UInt(cfg.instrMainWidth bits)
  tempInstr := (
    RegNext(
      next=tempInstr,
      init=tempInstr.getZero,
    )
  )
  tempInstr.allowOverride
  startDecode := False
  if (cfg.irqCfg != None) {
    upPayload.takeIrq := False
  }
  val nextPrevInstrBlockedIrq = (
    true
  ) generate (
    Bool()
  )
  val rPrevInstrBlockedIrq = (
    true
  ) generate (
    RegNext(
      next=nextPrevInstrBlockedIrq,
      init=nextPrevInstrBlockedIrq.getZero,
    )
  )
  nextPrevInstrBlockedIrq := rPrevInstrBlockedIrq
  val tempIsFiring = (
    /*KeepAttribute*/(
      Bool()
    )
    .setName(s"GenInstrDecode_tempIsFiring")
  )
  tempIsFiring := up.isFiring
  val myDecodeAreaWithoutUcode = (
    !cfg.supportUcode
  ) generate(
    doDecodeFunc(this)
  )
  when (up.isValid) {
    when (
      if (cfg.supportUcode) (
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
                  RegNext(io.idsIraIrq.nextValid)
                )
              ) {
                  upPayload.takeIrq := True
              }
              nextPrevInstrBlockedIrq := (
                upPayload.blockIrq
              )
            }
          }
        )
      }
    }
  }
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
  val tempVecSize = 3 // TODO: temporary size of `3`
  val gprIsZeroVec = (
    cfg.myHaveZeroReg
  ) generate (
    Vec.fill(tempVecSize)(
      Bool()
    )
  )
  val dbusHostPayload = (
    BusHostPayload(cfg=cfg, isIbus=false)
  )
  val rdMemWord = /*in*/(Vec.fill(tempVecSize)(
    UInt(cfg.mainWidth bits)
  ))
  val regPc = /*in*/(UInt(cfg.mainWidth bits))
  val regPcSetItCnt = /*in*/(UInt(
    1 bits
  ))
  val upIsFiring = /*in*/(Bool())
  val upIsValid = /*in*/(Bool())
  val upIsReady = /*in*/(Bool())
  val downIsFiring = /*in*/(Bool())
  val downIsValid = /*in*/(Bool())
  val downIsReady = /*in*/(Bool())
  val regPcPlusInstrSize = /*in*/(UInt(cfg.mainWidth bits))
  val regPcPlusImm = /*in*/(UInt(cfg.mainWidth bits))
  val imm = /*in*/(Vec.fill(4)(UInt(cfg.mainWidth bits)))
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
    SnowHousePsExSetPcPayload(cfg=cfg)
  ))
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
  def jmpAddrIdx = (
    1
  )
  def brCondIdx = Array[Int](0, 1)
  val haveRetIraState = (
    cfg.irqCfg match {
      case Some(irqCfg) => {
        irqCfg match {
          case SnowHouseIrqConfig.IraIds(/*allowIrqStorm*/) => {
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
  args: SnowHousePipeStageArgs,
) extends Area {
  def cfg = args.cfg
  val modIo = args.io
  val io = SnowHousePipeStageExecuteSetOutpModMemWordIo(cfg=cfg)
  io.modMemWord := (
    RegNext(
      next=io.modMemWord,
      init=io.modMemWord.getZero,
    )
  )
  val myModMemWordValid = (
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
  io.opIs := 0x0
  io.decodeExt.memAccessKind := SnowHouseMemAccessKind.LoadU
  io.decodeExt.memAccessSubKind := SnowHouseMemAccessSubKind.Sz8
  io.decodeExt.memAccessIsPush := False
  io.opIsJmp.allowOverride
  io.opIsJmp := (
    io.psExSetPc.fire
  )
  io.shouldIgnoreInstr := (
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

  io.multiCycleOpInfoIdx := 0x0
  val nextShouldIgnoreInstrState = Bool()
  val rShouldIgnoreInstrState = (
    RegNext(
      next=nextShouldIgnoreInstrState,
      init=nextShouldIgnoreInstrState.getZero,
    )
  )
  nextShouldIgnoreInstrState := rShouldIgnoreInstrState
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
    when (io.upIsFiring) {
      io.rHadRetIra := nextHadRetIra
    }
  }
  io.psExSetPc.nextPc.allowOverride
  io.psExSetPc.nextPc := (
    io.regPcPlusImm
  )
  io.dbusHostPayload.data := io.rdMemWord(0) //selRdMemWord(0)
  //if (cfg.allMainLdstUseGprPlusImm) {
  //  io.dbusHostPayload.addr := io.rdMemWord(1) + io.imm(1)
  //}
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
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
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
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
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
                if (!mem.isAtomic) {
                  val isStore = mem.isStore
                  if (!isStore) {
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
                  } else {
                    io.modMemWordValid := True
                    io.modMemWord(0) := selRdMemWord(0)
                  }
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
                    tempSubKind
                  )
                  io.decodeExt.memAccessSubKind := (
                    tempSubKind
                  )
                    io.decodeExt.memAccessIsPush := False
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
                  if (!isStore) {
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
                      tempMemAccessKind
                    )
                    io.dbusHostPayload.accKind := (
                      tempMemAccessKind
                    )
                  }
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
            when (!io.takeIrq) {
              if (opInfo.dstArr.size == 1) (
                io.modMemWordValid := False
              ) else if (
                opInfo.dstArr(1) == DstKind.Spr(SprKind.Ie)
              ) {
                io.modMemWordValid := False
              } else {
                // TODO: *maybe* support more outputs
                io.modMemWordValid := !io.gprIsZeroVec(0)
              }
            } otherwise {
              io.modMemWordValid := False
            }
            io.modMemWord(0) := (

              io.regPc + ((cfg.instrMainWidth / 8) * 1)

            )
            io.psExSetPc.valid := True
            when (
              !io.shouldIgnoreInstr
            ) {
              opInfo.srcArr(0) match {
                case SrcKind.Gpr => {
                  io.psExSetPc.nextPc := (
                    io.rdMemWord(io.jmpAddrIdx)
                  )
                }
                case SrcKind.Spr(SprKind.Ira) => {
                  io.psExSetPc.nextPc := (
                    io.rIra
                  )
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
            }
          }
          case CpyOpKind.Br => {
            if (opInfo.dstArr.size == 1) (
              io.modMemWordValid := False
            )
            nextIndexReg := 0x0
            opInfo.cond match {
              case CondKind.Always => {
                io.psExSetPc.valid := True

                io.modMemWord(0) := (
                  io.regPc + ((cfg.instrMainWidth / 8) * 1)
                )
                if (opInfo.dstArr.size == 1) (
                  io.modMemWordValid := False
                ) else {
                  io.modMemWordValid := (
                    // TODO: support more outputs
                    !io.gprIsZeroVec(0)
                  )
                }
              }
              case CondKind.Eq => {
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
              }
              case CondKind.Ne => {
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
              }
              case CondKind.Mi => {
                io.psExSetPc.valid := (
                  io.rFlagN
                )
              }
              case CondKind.Pl => {
                io.psExSetPc.valid := (
                  !io.rFlagN
                )
              }
              case CondKind.Vs => {
                io.psExSetPc.valid := (
                  io.rFlagV
                )
              }
              case CondKind.Vc => {
                io.psExSetPc.valid := (
                  !io.rFlagV
                )
              }
              case CondKind.Geu => {
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
              }
              case CondKind.Ltu => {
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
              }
              case CondKind.Gtu => {
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  io.psExSetPc.valid := (
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
              }
              case CondKind.Leu => {
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  io.psExSetPc.valid := (
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
              }
              case CondKind.Ges => {
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
              }
              case CondKind.Lts => {
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
              }
              case CondKind.Gts => {
                if (
                  opInfo.srcArr(0) == SrcKind.Spr(SprKind.AluFlags)
                  && opInfo.srcArr(1) == SrcKind.Imm()
                ) {
                  io.psExSetPc.valid := (
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
              }
              case CondKind.Les => {
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
              }
              case CondKind.Z => {
                assert(
                  opInfo.srcArr(0) == SrcKind.Gpr,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                io.psExSetPc.valid := (
                  io.rdMemWord(io.brCondIdx(0)) === 0
                )
              }
              case CondKind.Nz => {
                assert(
                  opInfo.srcArr(0) == SrcKind.Gpr,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                io.psExSetPc.valid := (
                  io.rdMemWord(io.brCondIdx(0)) =/= 0
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
                      io.modMemWordValid := False
                      io.modMemWord.foreach(modMemWord => {
                        modMemWord := modMemWord.getZero
                      })
                    } else {
                      nextFlagN := myBinop.flagN
                      nextFlagV := myBinop.flagV
                      nextFlagC := myBinop.flagC
                      nextFlagZ := myBinop.flagZ
                    }
                  }
                  case SprKind.Ids => {
                    nextIds := myBinop.main
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
                  }
                  case SprKind.Ira => {
                    nextIra := myBinop.main
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
                  }
                  case SprKind.Ie => {
                    nextIe := myBinop.main(0)
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
                  }
                  case SprKind.Ity => {
                    nextIty := myBinop.main
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
                  }
                  case SprKind.Sty => {
                    nextSty := myBinop.main
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
                  }
                  case SprKind.Hi => {
                    nextHi := myBinop.main
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
                  }
                  case SprKind.Lo => {
                    nextLo := myBinop.main
                    io.modMemWordValid := False
                    io.modMemWord.foreach(modMemWord => {
                      modMemWord := modMemWord.getZero
                    })
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
                io.modMemWordValid := False
                io.modMemWord.foreach(modMemWord => {
                  modMemWord := modMemWord.getZero
                })
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
                modIo.multiCycleBusVec(idx).recvData.dstVec(dstIdx)
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
          s"not yet implemented: "
          + s"opInfo(${opInfo}) index:${opInfoIdx}"
        )
      }
    }
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
  }
  when (io.takeIrq) {
    nextIra := io.regPc
    nextIe/*(0)*/ := False //0x0
    io.psExSetPc.nextPc := (
      io.rIds
    )
  }
  when (io.shouldIgnoreInstr) {
    io.opIs := 0x0
  }
//  }
}

case class SnowHousePipeStageExecute(
  args: SnowHousePipeStageArgs,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
  psMemStallHost: LcvStallHost[
    BusHostPayload,
    BusDevPayload,
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
  def cfg = args.cfg
  def io = args.io
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
  if (cfg.optFormal) {
    if ((1 << outp.op.getWidth) != cfg.opInfoMap.size) {
      assume(inp.op < cfg.opInfoMap.size)
      assume(outp.op < cfg.opInfoMap.size)
    }
  }
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
    when (pastValidAfterReset) {
      when (rose(rSetOutpState)) {
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
  //val savedPsMemStallHost = (
  //  LcvStallHostSaved(
  //    stallHost=psMemStallHost,
  //    someLink=cMid0Front,
  //  )
  //)
  def stallKindMem = 0
  def stallKindMultiCycle = 1
  def stallKindLim = 2

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
  val doCheckHazard = (
    Bool()
  )
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
      for (
        ydx
        <- 0 until cfg.regFileCfg.memArrSize
      ) {
        temp += (
          !tempModFrontPayload.myExt(ydx).modMemWordValid
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
    myDoHaveHazardVec.reduceLeft(_ || _)
  )
  val setOutpModMemWord = SnowHousePipeStageExecuteSetOutpModMemWord(
    args=args
  )
  setOutpModMemWord.io.takeIrq := (
    RegNext(
      next=setOutpModMemWord.io.takeIrq,
      init=setOutpModMemWord.io.takeIrq.getZero,
    )
  )
  val rIrqHndlState = {
    val temp = Reg(
      Bool()
    )
    temp.init(temp.getZero)
    temp
  }
  if (cfg.irqCfg != None) {
    when (RegNext(io.idsIraIrq.nextValid)) {
      setOutpModMemWord.io.takeIrq := (
        cMid0Front.up.isValid
        && outp.takeIrq
        && (
          RegNextWhen(
            next=(setOutpModMemWord.io.rIe/*(0)*/ === True),//0x0
            cond=cMid0Front.up.isFiring,
            init=False,
          )
        ) && (
          !rIrqHndlState//.valid
        ) && (
          if (setOutpModMemWord.io.haveRetIraState) (
            !setOutpModMemWord.io.rHadRetIra
          ) else (
            True
          )
        )
      )
    }
  }
  val nextTempIrqCond = (
    cfg.irqCfg != None
  ) generate (
    cMid0Front.up.isFiring
    && setOutpModMemWord.io.takeIrq
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
  ) generate (
    cMid0Front.up.isFiring
    && rIrqHndlState//.fire
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
    when (
      reEnableIrqsCond
    ) {
      setOutpModMemWord.nextIe/*(0)*/ := True//0x1
      rIrqHndlState/*.valid*/ := False
      if (setOutpModMemWord.io.haveRetIraState) {
      }
    }
  }
  setOutpModMemWord.io.splitOp.kind.allowOverride
  setOutpModMemWord.io.splitOp.jmpBrOp.allowOverride
  setOutpModMemWord.io.splitOp := (
    RegNext(
      next=setOutpModMemWord.io.splitOp,
      //init=setOutpModMemWord.io.splitOp.getZero,
    )
    //init(SnowHouseSplitOpKind.CPY_CPYUI)
  )
  when (cMid0Front.up.isValid) {
    when (!setOutpModMemWord.io.takeIrq) {
      setOutpModMemWord.io.splitOp := outp.splitOp
    } otherwise {
      setOutpModMemWord.io.splitOp := setOutpModMemWord.io.splitOp.getZero
      setOutpModMemWord.io.splitOp.kind := (
        SnowHouseSplitOpKind.JMP_BR
      )
      setOutpModMemWord.io.splitOp.jmpBrOp := {
        val temp = UInt(log2Up(cfg.jmpBrOpInfoMap.size) bits)
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

  setOutpModMemWord.io.regPcSetItCnt := outp.regPcSetItCnt
  setOutpModMemWord.io.regPc := outp.regPc
  setOutpModMemWord.io.regPcPlusInstrSize := outp.regPcPlusInstrSize
  setOutpModMemWord.io.regPcPlusImm := outp.regPcPlusImm
  setOutpModMemWord.io.imm := outp.imm
  outp.decodeExt := setOutpModMemWord.io.decodeExt
    outp.psExSetPc := psExSetPc
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
      setOutpModMemWord.io.gprIsZeroVec(idx) := (
        outp.gprIsZeroVec(idx)
      )
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
        tempExt.modMemWordValid := (
          setOutpModMemWord.io.modMemWordValid
        )
      }
    def tempRdMemWord = setOutpModMemWord.io.rdMemWord(zdx)
    tempRdMemWord := myRdMemWord(ydx=ydx, modIdx=zdx)
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
  val nextSavedStall = Bool()
  val rSavedStall = (
    /*KeepAttribute*/(
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
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      outp.myExt(ydx).valid := (
        outp.myExt(ydx).modMemWordValid
      )
    }
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
  psExSetPc.payload := (
    RegNext(
      next=psExSetPc.payload,
      init=psExSetPc.payload.getZero,
    )
  )
  psExSetPc.nextPc.allowOverride
  val condForAssertSetPcValid = (
    setOutpModMemWord.io.opIsJmp
  )
  outp.instrCnt.shouldIgnoreInstr := (
    setOutpModMemWord.io.shouldIgnoreInstr
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
  io.dbus.sendData := (
    RegNext(
      next=io.dbus.sendData,
      init=io.dbus.sendData.getZero,
    )
  )
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
        setOutpModMemWord.io.selRdMemWord(
          opInfo=opInfo,
          idx=1,
        )
      )
      multiCycleBus.sendData.srcVec(1) := (
        setOutpModMemWord.io.selRdMemWord(
          opInfo=opInfo,
          idx=2,
        )
      )
    }
  }
  //io.dcacheHaveHazard := (
  //  !rSavedStall
  //  && doCheckHazard && myDoHaveHazard1
  //)
  when (
    setOutpModMemWord.io.opIsMemAccess
  ) {
    nextPrevTxnWasHazard := True
    when (cMid0Front.up.isFiring) {
      psMemStallHost.nextValid := True
      io.dbus.sendData := setOutpModMemWord.io.dbusHostPayload
    }
  }
  when (setOutpModMemWord.io.opIsMultiCycle) {
    switch (outp.splitOp.multiCycleOp) {
      for (
        ((_, opInfo), opInfoIdx)
        <- cfg.multiCycleOpInfoMap.view.zipWithIndex
      ) {
        is (opInfoIdx) {
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
              RegNext(psExStallHost.nextValid)
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

  psExStallHostArr.foreach(psExStallHost => {
    when (psExStallHost.fire) {
      psExStallHost.nextValid := False
    }
  })
  doCheckHazard := rPrevTxnWasHazard
  when (
    myDoStall.sFindFirst(_ === True)._1
  ) {
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      outp.myExt(ydx).valid := False
      outp.myExt(ydx).modMemWordValid := (
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
    outp.regPc + outp.imm(2) //+ (cfg.instrMainWidth / 8)
  )
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
  def modFrontPayload = regFile.io.modFrontPayload
  def modBack = regFile.io.modBack
  def modBackPayload = regFile.io.modBackPayload
  def back = regFile.io.back
  def backPayload = regFile.io.backPayload
  def tempModFrontPayload = (
    regFile.io.tempModFrontPayload
  )
  val modFrontFormalAssumes = modFront(modFrontPayload).formalAssumes()
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
  val fMidModFront = (doMidMod) generate (
    ForkLink(
      up=cMidModFront.down,
      downs={
        Array.fill(2)(Node())
      },
      synchronous=false
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
  val formalFwdMidModArea = (regFile.myHaveFormalFwd) generate (
    new Area {
      val myFwd = (
        /*KeepAttribute*/(
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
      }
      val doFormalFwdUp =  (
        PipeMemRmwDoFwdArea(
          fwdAreaName=s"formalFwdMidModArea_doFormalFwdUp",
          fwd=(
            myFwd(extIdxUp)
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
          myExt(extIdxUp).foreach(current => {
            assert(current.main === current.main.getZero)
          })
          assert(
            myFwd(extIdxUp)
            === myFwd(extIdxUp).getZero
          )
        }
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
          midModPayload(extIdxUp).decodeExt.opIsMemAccess
        ) {
          for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
            for (zdx <- 0 until cfg.regFileModRdPortCnt) {
              assert(stable(myExt(extIdxUp)(ydx).modMemWord(zdx)))
            }
          }
        }
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
  tempModFrontPayload := midModPayload(extIdxUp)
  for (idx <- 0 until tempModFrontPayload.gprIdxVec.size) {
    tempModFrontPayload.gprIdxVec(idx).allowOverride
    tempModFrontPayload.gprIdxVec(idx) := (
      modFront(modFrontPayload).gprIdxVec(idx)
    )
  }
  //val savedPsMemStallHost = (
  //  LcvStallHostSaved(
  //    stallHost=psMemStallHost,
  //    someLink=cMidModFront,
  //  )
  //  .setName(s"psMem_savedPsMemStallHost")
  //)
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
  when (cMidModFront.up.isValid) {
    midModPayload(extIdxUp) := modFront(modFrontPayload)
  }
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

  def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
  def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
  val rDbusState = (
    Reg(Bool(), init=False)
  )
  when (
    RegNext(io.dbus.nextValid)
  ) {
    def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
    def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
    when (
      !io.dbus.ready
    ) {
      cMidModFront.duplicateIt()
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
      val myDecodeExt = midModPayload(extIdxUp).decodeExt
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
      if (!mapElem.haveHowToSetIdx) {
        midModPayload(extIdxUp).myExt(
          0
        )
      } else {
        //assert(false)
        midModPayload(extIdxUp).myExt(
          mapElem.howToSetIdx
        )
      }
    )
    val myDecodeExt = midModPayload(extIdxUp).decodeExt
  }
  when (
    //RegNext(io.dbus.nextValid)
    //io.dbus.ready
    io.dbusExtraReady
  ) {
    val myDecodeExt = midModPayload(extIdxUp).decodeExt
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
    //when (!myDecodeExt.memAccessKind.asBits(1)) {
      myCurrExt.modMemWord := (
        io.dbus.recvData.data.resized
      )
    //} otherwise {
    //}
  }

  def setMidModStages(): Unit = {
    regFile.io.midModStages(0) := midModPayload
  }
  setMidModStages()

  modFront(modBackPayload) := midModPayload(extIdxUp)
  when (modFront.isValid) {
  } otherwise {
  }
}
case class SnowHousePipeStageWriteBack(
  args: SnowHousePipeStageArgs,
) extends Area {
  def cfg = args.cfg
  def regFile = args.regFile
  def front = regFile.io.front
  def frontPayload = regFile.io.frontPayload
  def modFront = regFile.io.modFront
  def modFrontPayload = regFile.io.modFrontPayload
  def modBack = regFile.io.modBack
  def modBackPayload = regFile.io.modBackPayload
  def back = regFile.io.back
  def backPayload = regFile.io.backPayload
  def tempModFrontPayload = (
    regFile.io.tempModFrontPayload
  )
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
    /*KeepAttribute*/(
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
    4
  )
  val tempHaveSeenPipeToWriteV2dFindFirst_0 = (
    /*KeepAttribute*/(
      Vec.fill(tempHistHaveSeenPipeToWriteV2dOuterDim)({
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
      val rPrevOpCnt = Vec({
        val tempArr = ArrayBuffer[UInt]()
        for (ydx <- 0 until regFile.memArrSize) {
          tempArr += (
            RegNextWhen(
              next=modBack(modBackPayload).opCnt,
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
        myHaveAnyCurrWrite
      )
      def myCoverVecSize = 8
      val tempMyCoverInit = SnowHousePipePayload(cfg=cfg)
      tempMyCoverInit.allowOverride
      tempMyCoverInit := tempMyCoverInit.getZero
      val myHistCoverVec = (
        /*KeepAttribute*/(
          History(
            that=modBack(modBackPayload),
            length=myCoverVecSize,
            when=myCoverCond,
            init=tempMyCoverInit,
          )
        )
      )
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
    /*KeepAttribute*/(
      Vec[Vec[UInt]]({
        val tempArr = ArrayBuffer[Vec[UInt]]()
        for ((wordCount, ydx) <- regFile.wordCountArr.view.zipWithIndex) {
          tempArr += {
            val myArr = new ArrayBuffer[UInt]()
            for (idx <- 0 until wordCount) {
              myArr += (
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
        /*KeepAttribute*/(
          regFile.mkExt()
        )
        .setName(
          s"formalFwdModBackArea_"
          + s"myExt"
        )
      )
      val myFwd = (
        /*KeepAttribute*/(
          Vec.fill(extIdxLim)(
            regFile.mkFwd()
          )
        )
        .setName(
          s"formalFwdModBackArea_"
          + s"myFwd"
        )
      )
      for (extIdx <- 0 until extIdxLim) {
        for (ydx <- 0 until regFile.memArrSize) {
          myExt(ydx)(extIdx) := regFile.cBackArea.upExt(1)(ydx)(extIdx)
        }
        myFwd(extIdx) := regFile.cBackArea.upFwd(extIdx)
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
  case class HistMain(
  ) extends Bundle {
    val myHaveCurrWrite = Vec.fill(regFile.memArrSize)(
      Bool()
    )
    val flow = Flow(SnowHousePipePayload(cfg=cfg))
  }
  def myHistMainSize = 8
  val myHistMain: Vec[HistMain] = (
    /*KeepAttribute*/(Vec[HistMain]{
      val tempArr = ArrayBuffer[HistMain]()
      for (idx <- 0 until myHistMainSize) {
        def myHistMainCond = (
          pastValidAfterReset
          && modBack.isFiring
        )
        val temp = HistMain()
        temp.myHaveCurrWrite := myHaveCurrWrite
        temp.flow.payload := modBack(modBackPayload)
        temp.flow.valid := True
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
      myHistMain(0).flow.fire
      && myHistMain(1).flow.fire
      && myHistMain(2).flow.fire
      && myHistMain(3).flow.fire
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
              idx: Int,
            ): UInt = {
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
                  assert(flow3.decodeExt.opIsJmp)
                  assert(
                    flow3.psExSetPc.fire === cond
                  )
                  when (cond) {
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
                        )
                      }
                    }
                  }
                  case _ => {
                  }
                }
              }
              case _ => {
              }
            }
          }
        }
      }
    }
    when (pastValidAfterReset) {
      val tempCond = (
        (
          myHaveCurrWrite
        )
      )
      for ((wordCount, ydx) <- regFile.wordCountArr.view.zipWithIndex) {
        when (past(tempCond(ydx))) {
          for (idx <- 0 until wordCount) {
            when (
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
        val tempCond1 = (
          /*KeepAttribute*/(
            /*past*/(regFile.mod.back.myWriteEnable(ydx))
            && (
              myHadWriteAt(ydx)(
              /*past*/(regFile.mod.back.myWriteAddr(ydx)(
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
        val myTempRight = (
          /*KeepAttribute*/(
            Vec[UInt]({
              val myArr = new ArrayBuffer[UInt]()
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
                          result
                        }
                        case CpyOpKind.Br => {
                          val result = InstrResult(cfg=cfg)()
                          myDoFormalAssertRegular := False
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
