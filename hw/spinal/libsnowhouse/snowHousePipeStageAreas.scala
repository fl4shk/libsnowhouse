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

  when (psExSetPc.fire) {
    rSavedExSetPc := psExSetPc
  }
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
    rSavedExSetPc := rSavedExSetPc.getZero
    //if (cfg.optFormal) {
      myInstrCnt.any := rPrevInstrCnt.any + 1
    //}
    //--------
    when (psExSetPc.fire) {
      nextRegPc := psExSetPc.nextPc //+ (cfg.instrMainWidth / 8)
      //if (cfg.optFormal) {
        myInstrCnt.jmp := rPrevInstrCnt.jmp + 1
      //}
    } elsewhen (rSavedExSetPc.fire) {
      nextRegPc := (
        rSavedExSetPc.nextPc //+ (cfg.instrMainWidth / 8)
      )
      //if (cfg.optFormal) {
        myInstrCnt.jmp := rPrevInstrCnt.jmp + 1
      //}
    } otherwise {
      nextRegPc := rPrevRegPc + (cfg.instrMainWidth / 8)
      //if (cfg.optFormal) {
        myInstrCnt.fwd := rPrevInstrCnt.fwd + 1
      //}
    }
  }
  //--------
  io.ibus.nextValid := True
  io.ibus.hostData.addr := nextRegPc //upModExt.regPc
  //--------
  val myDoHaltIt = (cfg.optFormal) generate (
    Bool()
  )
  if (cfg.optFormal) {
    myDoHaltIt := False
  }
  def doStallMain(): Unit = {
    io.ibus.hostData.addr := (
      RegNext(
        next=io.ibus.hostData.addr,
        init=io.ibus.hostData.addr.getZero,
      )
    )
  }
  def doHaltItEtc(): Unit = {
    //io.ibus.nextValid := False
    doStallMain()
    cIf.haltIt()
    if (cfg.optFormal) {
      myDoHaltIt := True
    }
  }
  when (
    //io.ibus.nextValid
    //&& 
    !io.ibus.ready
  ) {
    //cIf.duplicateIt()
    doHaltItEtc()
  }
  //--------
  if (cfg.optFormal) {
    when (
      !io.ibus.ready //|| psIdHaltIt
    ) {
      assert(!cIf.up.isReady)
      assert(!cIf.down.isValid)
      assert(myDoHaltIt)
    }
    when (pastValidAfterReset) {
      when (!io.ibus.ready) {
        assert(stable(io.ibus.hostData.addr))
      }
    }
    when (pastValidAfterReset) {
      when (past(io.ibus.nextValid)) {
        when (io.ibus.ready) {
          when (!io.ibus.nextValid) {
            assume(!RegNext(io.ibus.ready))
          }
        }
      }
    }
    when (up.isFiring) {
      assert(!myDoHaltIt)
    }
    when (pastValidAfterReset) {
      when (past(up.isFiring)) {
        assert(rSavedExSetPc === rSavedExSetPc.getZero)
      }
      when (up.isFiring) {
        when (
          !psExSetPc.fire
          && !rSavedExSetPc.fire
        ) {
          assert(nextRegPc === rPrevRegPc + (cfg.instrMainWidth / 8))
          assert(myInstrCnt.fwd === rPrevInstrCnt.fwd + 1)
          assert(stable(myInstrCnt.jmp))
        } elsewhen (psExSetPc.fire) {
          assert(nextRegPc === psExSetPc.nextPc)
          assert(stable(myInstrCnt.fwd))
          assert(myInstrCnt.jmp === rPrevInstrCnt.jmp + 1)
        } otherwise { // when (rSavedExSetPc.fire)
          assert(nextRegPc === rSavedExSetPc.nextPc)
          assert(stable(myInstrCnt.fwd))
          assert(myInstrCnt.jmp === rPrevInstrCnt.jmp + 1)
        }
        assert(myInstrCnt.any === rPrevInstrCnt.any + 1)
      } otherwise {
        assert(stable(nextRegPc))
        assert(stable(myInstrCnt.fwd))
        assert(stable(myInstrCnt.jmp))
        assert(stable(myInstrCnt.any))
      }
    }
  }
  //--------
}
case class SnowHousePipeStageInstrDecode(
  //val cfg: SnowHouseConfig,
  //var args: Option[SnowHousePipeStageArgs]=None,
  val args: SnowHousePipeStageArgs,
  val psIdHaltIt: Bool,
  val psExSetPc: Flow[SnowHousePsExSetPcPayload],
  val doDecodeFunc: (SnowHousePipeStageInstrDecode) => Area,
) extends Area {
  //--------
  //def decInstr: UInt
  def cfg = args.cfg
  //def opInfoMap = args.opInfoMap
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
  def myDoHaltIt(): Unit = {
    //psIdHaltIt := True
    cId.haltIt()
      // this `haltIt()` call prevents `up.isFiring` and prevents
      // deassertion of `rDidHandleG7SubDecode`
  }
  //val decInstr = UInt(log2Up(opInfoMap.size) bits)
  //--------
  val up = cId.up
  val down = cId.down
  //--------
  val upPayload = SnowHousePipePayload(cfg=cfg)
  //up(pId) := upModExt
  up(pId) := upPayload
  val rSetUpPayloadState = Reg(Bool(), init=False)
  upPayload := (
    RegNext(
      next=upPayload,
      init=upPayload.getZero,
    )
    init(upPayload.getZero)
  )
  upPayload.allowOverride
  when (up.isValid) {
    //upPayload.regPc := cId.up(pIf).regPc
    //upPayload.instrCnt := cId.up(pIf).instrCnt
    when (!rSetUpPayloadState) {
      upPayload := up(pIf)
      rSetUpPayloadState := True
    }
    when (up.isFiring) {
      //up(pId) := upPayload
      rSetUpPayloadState := False
    }
  }
  if (optFormal) {
    when (pastValidAfterReset) {
      when (past(up.isFiring) init(False)) {
        assert(!rSetUpPayloadState)
      }
      when (!(past(up.isValid) init(False))) {
        assert(stable(rSetUpPayloadState))
      }
      when (rSetUpPayloadState) {
        assert(up.isValid)
      }
      //when (rose(rSetUpPayloadState)) {
      //  assert(
      //    upPayload.
      //  )
      //}
    }
  }
  upPayload.regPcPlusImm := (
    upPayload.regPc + upPayload.imm
  )
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
    switch (gprIdx) {
      val howToSlice = cfg.shRegFileCfg.howToSlice
      var outerCnt: Int = 0
      for ((howToSet, howToSetIdx) <- howToSlice.view.zipWithIndex) {
        //var cnt: Int = 0
        for ((howTo, howToIdx) <- howToSet.view.zipWithIndex) {
          is (
            //outerCnt
            howTo
          ) {
            println(
              s"debug: "
              + s"outerCnt:${outerCnt}"
              + s"; "
              //+ s"cnt:${cnt}; "
              + s"zdx:${zdx} "
              + s"howTo:${howTo} howToIdx:${howToIdx} "
              + s"howToSetIdx:${howToSetIdx}"
            )
            val mapElem = upGprIdxToMemAddrIdxMap(zdx)
            mapElem.idx := (
              //howTo
              //cnt
              howToIdx
            )
            if (mapElem.haveHowToSetIdx) {
              mapElem.howToSetIdx := howToSetIdx
            }
            upPayload.myExt(howToSetIdx).memAddr(zdx) := howToIdx
            //:= (
            //  howToSetIdx
            //  //howToIdx
            //)
          }
          //cnt += 1
          outerCnt += 1
        }
      }
      assert(
        outerCnt == cfg.numGprs,
        s"eek! cnt:${outerCnt} != cfg.numGprs:${cfg.numGprs}"
      )
    }
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
  val myDecodeArea = doDecodeFunc(this)
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
  //println(
  //  s"currOp.getWidth: ${currOp.getWidth}"
  //)
  //val doIt = /*in*/(Bool())
  val rdMemWord = /*in*/(Vec.fill(3)( // TODO: temporary size of `3`
    UInt(cfg.mainWidth bits)
  ))
  //val regPc = /*in*/(UInt(cfg.mainWidth bits))
  val regPcPlusImm = /*in*/(UInt(cfg.mainWidth bits))
  val imm = /*in*/(UInt(cfg.mainWidth bits))
  val pcChangeState = /*in*/(PcChangeState())
  def selRdMemWord(
    opInfo: OpInfo,
    idx: Int,
  ): UInt = {
    //return rdMemWord(idx)
    def innerFunc(
      idx: Int,
    ) = {
      if (idx == 0) {
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
          case SrcKind.Imm(isSImm) => {
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
          case CpyOpKind.Jmp | CpyOpKind.Br => {
            return innerFunc(idx=idx + 1)
          }
          case _ => {
            return innerFunc(idx=idx)
          }
        }
      }
      case _ => {
        return innerFunc(idx=idx)
      }
    }
  }
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
  def opIs = decodeExt.opIs
  def opIsMemAccess = decodeExt.opIsMemAccess
  def opIsCpyNonJmpAlu = decodeExt.opIsCpyNonJmpAlu
  def opIsJmp = decodeExt.opIsJmp
  def opIsMultiCycle = decodeExt.opIsMultiCycle
  //--------
  //val dbusHostPayload = /*out*/(DbusHostPayload(cfg=cfg))
  def jmpAddrIdx = (
    //2
    0
  )
  def brCondIdx = Array[Int](0, 1)
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
  io.modMemWordValid := (
    //io.doIt //True
    True
  )
  io.psExSetPc := io.psExSetPc.getZero
  modIo.dbus.hostData := (
    RegNext(
      next=modIo.dbus.hostData,
      init=modIo.dbus.hostData.getZero,
    )
  )
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
  if (cfg.optFormal) {
    if ((1 << io.currOp.getWidth) != cfg.opInfoMap.size) {
      assert((1 << io.currOp.getWidth) < cfg.opInfoMap.size)
    }
  }
  io.opIs := 0x0
  //io.decodeExt.memAccessLdStKind := False
  io.decodeExt.memAccessKind := SnowHouseMemAccessKind.LoadU
  io.decodeExt.memAccessSubKind := SnowHouseMemAccessSubKind.Sz8
  io.opIsJmp.allowOverride
  io.opIsJmp := (
    (
      io.pcChangeState === PcChangeState.Idle
    ) && (
      io.psExSetPc.fire
    )
  )
  switch (io.pcChangeState) {
    is (PcChangeState.Idle) {
      switch (io.currOp) {
        //--------
        for (((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex) {
          def selRdMemWord(
            srcArrIdx: Int
          ): UInt = {
            io.selRdMemWord(opInfo=opInfo, idx=srcArrIdx)
          }
          assert(
            opInfo.dstArr.size == 1,
            s"not yet implemented: "
            + s"opInfo(${opInfo}) index:${opInfoIdx}"
          )
          assert(
            opInfo.srcArr.size == 1 || opInfo.srcArr.size == 2,
            s"not yet implemented: "
            + s"opInfo(${opInfo}) index:${opInfoIdx}"
          )
          is (U(s"${io.currOp.getWidth}'d${opInfoIdx}")) {
            opInfo.select match {
              case OpSelect.Cpy => {
                opInfo.cpyOp.get match {
                  case CpyOpKind.Cpy => {
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
                        io.modMemWord(0) := selRdMemWord(1)
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
                        mem.isStore match {
                          case Some(isStore) => {
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
                            modIo.dbus.hostData.subKind := (
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
                            modIo.dbus.hostData.addr := (
                              (
                                opInfo.addrCalc match {
                                  case AddrCalcKind.AddReduce => (
                                    selRdMemWord(1)
                                  )
                                  case kind: AddrCalcKind.LslThenMaybeAdd => (
                                    selRdMemWord(1)
                                    << kind.options.lslAmount.get
                                  )
                                }
                              ) + (
                                opInfo.srcArr.size match {
                                  case 1 => {
                                    U(s"${cfg.mainWidth}'d0")
                                  }
                                  case 2 => {
                                    selRdMemWord(2)
                                  }
                                  case _ => {
                                    assert(
                                      false,
                                      s"invalid opInfo.srcArr.size: "
                                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                                    )
                                    U"s${cfg.mainWidth}'d0"
                                  }
                                }
                              )
                            )
                            //modIo.dbus.hostData.subKind := (
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
                              modIo.dbus.hostData.accKind := (
                                tempMemAccessKind
                              )
                              modIo.dbus.hostData.data := (
                                modIo.dbus.hostData.data.getZero
                              )
                            } else { // if (isStore)
                              val tempMemAccessKind = (
                                SnowHouseMemAccessKind.Store
                              )
                              io.decodeExt.memAccessKind := (
                                //io.decodeExt._memAccessLdStKindStore
                                tempMemAccessKind
                              )
                              modIo.dbus.hostData.accKind := (
                                tempMemAccessKind
                              )
                              modIo.dbus.hostData.data := selRdMemWord(0)
                            }
                          }
                          case None => {
                            assert(
                              false,
                              s"not yet implemented: "
                              + s"opInfo(${opInfo}) index:${opInfoIdx}"
                            )
                          }
                        }
                        io.modMemWordValid := False
                        io.modMemWord(0) := 0x0
                      }
                    }
                  }
                  case CpyOpKind.Cpyui => {
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
                      opInfo.addrCalc == AddrCalcKind.AddReduce,
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
                      opInfo.addrCalc == AddrCalcKind.AddReduce,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.psExSetPc.valid := True
                    io.psExSetPc.nextPc := io.rdMemWord(io.jmpAddrIdx)
                  }
                  case CpyOpKind.Br => {
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
                        io.psExSetPc.valid := True
                        io.psExSetPc.nextPc := io.regPcPlusImm
                      }
                      //case CondKind.Link => {
                      //  io.opIsJmp := True
                      //  assert(
                      //    opInfo.dstArr.size == 2,
                      //  )
                      //}
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
                        io.psExSetPc.valid := (
                          io.rdMemWord(io.brCondIdx(0)) === 0
                        )
                        io.psExSetPc.nextPc := (
                          io.regPcPlusImm
                        )
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
                        io.psExSetPc.valid := (
                          io.rdMemWord(io.brCondIdx(0)) =/= 0
                        )
                        io.psExSetPc.nextPc := (
                          io.regPcPlusImm
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
                  opInfo.addrCalc == AddrCalcKind.AddReduce,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                // TODO: support ALU flags
                val binop: InstrResult = opInfo.aluOp.get match {
                  //case AluOpKind.Add => {
                  //  io.modMemWord(0) := (
                  //    selRdMemWord(1) + selRdMemWord(2)
                  //  )
                  //}
                  case AluOpKind.Adc => {
                    assert(
                      false,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    InstrResult(cfg=cfg)
                  }
                  //case AluOpKind.Sub => {
                  //  io.modMemWord(0) := (
                  //    selRdMemWord(1) - selRdMemWord(2)
                  //  )
                  //}
                  case AluOpKind.Sbc => {
                    assert(
                      false,
                      s"not yet implemented: "
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    InstrResult(cfg=cfg)
                  }
                  //case AluOpKind.Lsl => {
                  //  io.modMemWord(0) := (
                  //    (
                  //      selRdMemWord(1)
                  //      << selRdMemWord(2)(log2Up(cfg.mainWidth) - 1 downto 0)
                  //    )(
                  //      io.modMemWord(0).bitsRange
                  //    )
                  //  )
                  //}
                  //case AluOpKind.Lsr => {
                  //  io.modMemWord(0) := (
                  //    (selRdMemWord(1) >> selRdMemWord(2))(
                  //      io.modMemWord(0).bitsRange
                  //    )
                  //  )
                  //}
                  //case AluOpKind.Asr => {
                  //  io.modMemWord(0) := (
                  //    selRdMemWord(1).asSInt >> selRdMemWord(2)
                  //  ).asUInt(
                  //    io.modMemWord(0).bitsRange
                  //  )
                  //}
                  //case AluOpKind.And => {
                  //  io.modMemWord(0) := (
                  //    selRdMemWord(1) & selRdMemWord(2)
                  //  )
                  //}
                  //case AluOpKind.Or => {
                  //  io.modMemWord(0) := (
                  //    selRdMemWord(1) | selRdMemWord(2)
                  //  )
                  //}
                  //case AluOpKind.Xor => {
                  //  io.modMemWord(0) := (
                  //    selRdMemWord(1) ^ selRdMemWord(2)
                  //  )
                  //}
                  //case AluOpKind.Sltu => {
                  //  io.modMemWord(0) := Cat(
                  //    U(s"${cfg.mainWidth - 1}'d0"),
                  //    selRdMemWord(1) < selRdMemWord(2),
                  //  ).asUInt
                  //}
                  //case AluOpKind.Slts => {
                  //  io.modMemWord(0) := Cat(
                  //    U(s"${cfg.mainWidth - 1}'d0"),
                  //    selRdMemWord(1).asSInt < selRdMemWord(2).asSInt,
                  //  ).asUInt
                  //}
                  case op => {
                    op.binopFunc(
                      cfg=cfg,
                      left=selRdMemWord(1),
                      right=selRdMemWord(2),
                      carry=False,
                    )
                  }
                }
                io.modMemWord(0) := binop.main
              }
              case OpSelect.MultiCycle => {
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
                  opInfo.addrCalc == AddrCalcKind.AddReduce,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                opInfo.multiCycleOp.get match {
                  case MultiCycleOpKind.Umul => {
                    //assert(
                    //  opInfo.dstArr.size == 1,
                    //  s"not yet implemented: "
                    //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    //)
                    //assert(
                    //  opInfo.srcArr.size == 2,
                    //  s"not yet implemented: "
                    //  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    //)
                    io.modMemWord(0) := (
                      (selRdMemWord(1) * selRdMemWord(2))(
                        io.modMemWord(0).bitsRange
                      )
                    )
                  }
                  case _ => {
                    assert(
                      false,
                      s"not yet implemented"
                    )
                  }
                }
              }
            }
          }
        }
        default {
          //assert(False)
        }
        //is (PipeMemRmwSimDut.ModOp.AddRaRb) {
        //  io.modMemWord := (
        //    someRdMemWord + 0x1
        //  )
        //  //io.modMemWordValid := (
        //  //  True
        //  //)
        //}
        //is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
        //  io.modMemWord := (
        //    //someRdMemWord //+ 0x1
        //    0x0
        //  )
        //  io.modMemWordValid := (
        //    False
        //  )
        //}
        //is (PipeMemRmwSimDut.ModOp.MulRaRb) {
        //  io.modMemWord := (
        //    (
        //      if (PipeMemRmwSimDut.allModOpsSameChange) (
        //        someRdMemWord + 0x1
        //      ) else (
        //        (someRdMemWord << 1)(
        //          io.modMemWord.bitsRange
        //        )
        //      )
        //    )                    )
        //  //io.modMemWordValid := (
        //  //  True
        //  //)
        //}
        //--------
      }
    }
    is (PcChangeState.WaitTwoInstrs) {
      io.modMemWordValid := False
      io.modMemWord.foreach(modMemWord => {
        modMemWord := modMemWord.getZero
      })
    }
  }
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
  val myCurrOp = /*cloneOf(inp.op)*/ UInt(inp.op.getWidth bits)
  myCurrOp := (
    RegNext(
      next=myCurrOp,
      init=U(s"${myCurrOp.getWidth}'d0")
    )
  )
  if (cfg.optFormal) {
    if ((1 << myCurrOp.getWidth) != cfg.opInfoMap.size) {
      assume(inp.op < cfg.opInfoMap.size)
      assume(outp.op < cfg.opInfoMap.size)
      assume(myCurrOp < cfg.opInfoMap.size)
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
  val psExStallHost = (
    //PipeMemRmwSimDut.haveModOpMul
    havePsExStall
  ) generate (
    cfg.mkLcvStallHost[
      MultiCycleHostPayload,
      MultiCycleDevPayload,
    ](
      stallIo=(
        //io.psExStallIo
        // TODO: support multiple external `MultiCycleOp`
        if (!io.haveMultiCycleBusVec) {
          None
        } else { // if (io.haveMultiCycleBusVec)
          Some(io.multiCycleBusVec(0))
        }
      ),
    )
  )
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
  def myRdMemWord(
    ydx: Int,
    modIdx: Int,
  ) = (
    doModInModFrontParams.getMyRdMemWordFunc(ydx, modIdx)
  )
  when (cMid0Front.up.isValid ) {
    when (!rSetOutpState) {
      outp := inp
      myCurrOp := inp.op
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
    assert(myCurrOp === outp.op)
    when (pastValidAfterReset) {
      when (rose(rSetOutpState)) {
        assert(myCurrOp === past(inp.op))
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
  val savedPsExStallHost = (
    LcvStallHostSaved(
      stallHost=psExStallHost,
      someLink=cMid0Front,
    )
  )
  val savedPsMemStallHost = (
    LcvStallHostSaved(
      stallHost=psMemStallHost,
      someLink=cMid0Front,
    )
  )
  //--------
  val currDuplicateIt = (
    KeepAttribute(
      Bool()
    )
  )
  currDuplicateIt := False
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
      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        //println(s"begin: ${ydx} ${temp.size}")
        temp += {
          val tempArr = ArrayBuffer[Bool]()
          for (
            zdx 
            <- 0 until cfg.regFileModRdPortCnt
            //outp.myExt(ydx).memAddr.size
          ) {
            assert(
              outp.myExt(ydx).memAddr.size
              == cfg.regFileModRdPortCnt
            )
            tempArr += (
              outp.myExt(ydx).memAddr(zdx)
              === tempModFrontPayload.myExt(ydx).memAddr(zdx)
            )
          }
          //toReduce.reduce(_ || _)
          //toFold.foldLeft(False)((left, right) => (left || right))
          //toFold.sFindFirst(_ === True)._1
          tempArr.reduceLeft(_ || _)
        }
        //println(s"end: ${ydx} ${temp.size}")
      }
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
  setOutpModMemWord.io.currOp := myCurrOp
  setOutpModMemWord.io.regPcPlusImm := outp.regPcPlusImm
  setOutpModMemWord.io.imm := outp.imm
  outp.decodeExt := setOutpModMemWord.io.decodeExt
  psExSetPc := setOutpModMemWord.io.psExSetPc
  //if (cfg.optFormal) {
    outp.psExSetPc := psExSetPc
  //}
  if (io.haveMultiCycleBusVec) {
    for (
      (multiCycleBus, busIdx) <- io.multiCycleBusVec.view.zipWithIndex
    ) {
      for (idx <- 0 until multiCycleBus.hostData.srcVec.size) {
        multiCycleBus.hostData.srcVec(idx) := (
          0x0
          //setOutpModMemWord.io.selRdMemWord(
          //  opInfo=multiCycleBus.hostData.opInfo,
          //  idx=(idx + 1),
          //)
        )
      }
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
  def doFinishSetOutpModMemWord(
    ydx: Int,
    zdx: Int,
  ): Unit = {
    when (cMid0Front.up.isValid) {
      if (zdx == PipeMemRmw.modWrIdx) {
        def tempExt = outp.myExt(ydx)
        tempExt.modMemWord := (
          // TODO: support multiple output `modMemWord`s
          setOutpModMemWord.io.modMemWord(0)
        )
        tempExt.modMemWordValid := (
          setOutpModMemWord.io.modMemWordValid
        )
      }
      if (!cfg.optFormal) {
        outp.gprRdMemWordVec(zdx) := tempRdMemWord
      }
    }
    def tempRdMemWord = setOutpModMemWord.io.rdMemWord(zdx)
    tempRdMemWord := myRdMemWord(ydx=ydx, modIdx=zdx)
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
  when (doCheckHazard) {
    when (myDoHaveHazard) {
      currDuplicateIt := True
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
  when (doCheckHazard && myDoHaveHazard) {
    when (RegNext(cMid0Front.down.isFiring)) {
      currDuplicateIt := False
    }
  }
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
  val nextPcChangeState = PcChangeState()
  val rPcChangeState = (
    RegNext(next=nextPcChangeState)
    init(PcChangeState.Idle)
  )
  nextPcChangeState := rPcChangeState
  setOutpModMemWord.io.pcChangeState := rPcChangeState
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
  val rSavedJmpCnt = {
    val temp = Reg(
      SnowHouseInstrCnt(cfg=cfg)
    )
    temp.init(temp.getZero)
    //temp.jmpState.allowOverride
    temp
  }
  switch (rPcChangeState) {
    is (PcChangeState.Idle) {
      outp.instrCnt.shouldIgnoreInstr := False
      if (cfg.optFormal) {
        when (pastValidAfterReset) {
          when (past(rPcChangeState) === PcChangeState.WaitTwoInstrs) {
            assert(past(cMid0Front.up.isFiring))
          } otherwise {
          }
          //when (past(cMid0Front.up.isFiring)) {
          //}
        }
      }
      switch (setOutpModMemWord.io.opIs) {
        // TODO: support mem access in more kinds of instructions
        is (M"0010") {
          // instruction is of type Cpy (non-Jmp, non-Br)/Alu,
          // but NO mem access
          if (cfg.optFormal) {
            when (cMid0Front.up.isValid) {
              when (!doCheckHazard) {
                assert(!currDuplicateIt)
              }
            }
          }
        }
        is (M"0011") {
          // instruction is of type Cpy (non-Jmp, non-Br)/Alu,
          // but WITH mem access
          when (cMid0Front.up.isFiring) {
            nextPrevTxnWasHazard := True
            psMemStallHost.nextValid := True
          }
        }
        is (M"01--") {
          // instruction is of type Cpy (TAKEN Jmp or Br),
          // but with NO mem access
          when (cMid0Front.up.isFiring) {
            nextPcChangeState := PcChangeState.WaitTwoInstrs
            rSavedJmpCnt := outp.instrCnt
            //outp.instrCnt.shouldIgnoreInstr := True
          }
        }
        is (M"1---") {
          // instruction is of type MultiCycle,
          // but with NO mem access
          when (cMid0Front.up.isValid) {
            when (doCheckHazard) {
              when (!currDuplicateIt) {
                psExStallHost.nextValid := (
                  True
                )
              }
            } otherwise { // when (!doCheckHazard)
              psExStallHost.nextValid := (
                True
              )
            }
            //--------
          }
          when (savedPsExStallHost.myDuplicateIt) {
            currDuplicateIt := True
          }
          if (cfg.optFormal) {
            when (!doCheckHazard) {
              when (!savedPsExStallHost.myDuplicateIt) {
                assert(!currDuplicateIt)
              }
            }
          }
        }
        default {
        }
      }
    }
    is (PcChangeState.WaitTwoInstrs) {
      if (cfg.optFormal) {
        when (pastValidAfterReset) {
          when (past(rPcChangeState) === PcChangeState.Idle) {
            assert(past(cMid0Front.up.isFiring))
            assert(rSavedJmpCnt === past(outp.instrCnt))
            assert(!past(outp.instrCnt.shouldIgnoreInstr))
            assert(past(setOutpModMemWord.io.opIsJmp))
          } otherwise {
            assert(stable(rSavedJmpCnt))
            //assert(stable(outp.instrCnt.shouldIgnoreInstr))
          }
          //when (past(cMid0Front.up.isFiring)) {
          //}
        }
      }
      //outp.instrCnt.shouldIgnoreInstr := True
      outp.instrCnt.shouldIgnoreInstr := True
      when (cMid0Front.up.isFiring) {
        when (outp.instrCnt.any === rSavedJmpCnt.any + 2) {
          // with wrapping arithmetic,
          // it's okay if we overflow with the + 2!
          //outp.instrCnt.shouldIgnoreInstr := False
          nextPcChangeState := PcChangeState.Idle
        }
      }
    }
    //is (PcChangeState.SecondInstr) {
    //}
    //default {
    //  //assert(
    //  //  False
    //  //)
    //}
  }
  when (psExStallHost.fire) {
    psExStallHost.nextValid := False
  }
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
    currDuplicateIt
    || outp.instrCnt.shouldIgnoreInstr
  ) {
    //handleDuplicateIt()
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      outp.myExt(ydx).valid := False
      outp.myExt(ydx).modMemWordValid := (
        //someModMemWordValid
        False
      )
    }
    when (currDuplicateIt) {
      cMid0Front.duplicateIt()
    }
  }
  if (cfg.optFormal) {
    outp.psExSetOutpModMemWordIo := setOutpModMemWord.io
  }
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
        //modBack
        //Node()
        val temp = Node()
        temp.setName(s"sMidModFront_down")
        temp

        //modBack
      },
    )
  )
  val s2mMidModFront = (doMidMod) generate (
    S2MLink(
      up=sMidModFront.down,
      down=(
        modBack
      ),
    )
  )
  regFile.myLinkArr += cMidModFront
  regFile.myLinkArr += sMidModFront
  regFile.myLinkArr += s2mMidModFront
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

  tempModFrontPayload := midModPayload(extIdxUp)
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
  when (cMidModFront.up.isValid ) {
    when (!rSetMidModPayloadState) {
      midModPayload(extIdxUp) := modFront(modFrontPayload)
      nextSetMidModPayloadState := True
    }
    when (cMidModFront.up.isFiring) {
      nextSetMidModPayloadState := False
    }
    //--------
    switch (midModPayload(extIdxUp).op) {
      for (((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex) {
        is (opInfoIdx) {
          def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
          def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
          opInfo.select match {
            //is (PipeMemRmwSimDut.ModOp.LdrRaRb) 
            case OpSelect.Cpy => {
              for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
                val myExtLeft = tempExtLeft(ydx=ydx)
                val myExtRight = tempExtRight(ydx=ydx)
                opInfo.cpyOp.get match {
                  case CpyOpKind.Cpy if (
                    opInfo.memAccess != MemAccessKind.NoMemAccess
                  ) => {
                    when (!myExtLeft.modMemWordValid) {
                    } otherwise {
                      if (cfg.optFormal) {
                        when (!myShouldIgnoreInstr) {
                          assert(!savedPsMemStallHost.myDuplicateIt)
                        }
                      }
                    }
                    if (cfg.optFormal) {
                      opInfo.memAccess match {
                        case mem: MemAccessKind.Mem => {
                          mem.isStore match {
                            case Some(isStore) => {
                              when (
                                cMidModFront.up.isFiring
                                && !myShouldIgnoreInstr
                              ) {
                                //if (!isStore) {
                                assert(
                                  myExtLeft.modMemWordValid
                                  === (
                                    if (!isStore) (
                                      True
                                    ) else ( // if (isStore)
                                      False
                                    )
                                  )
                                )
                                //}
                              }
                            }
                            case None => {
                              assert(
                                false,
                                s"atomic operations not yet supported: "
                                + s"opInfo(${opInfo} ${opInfo.select}) "
                                + s"opInfoIdx:${opInfoIdx}"
                              )
                            }
                          }
                        }
                        case _ =>
                      }
                    }
                  }
                  case _ => {
                    myExtLeft.modMemWord := (
                      myExtRight.modMemWord
                    )
                    myExtLeft.modMemWordValid := (
                      myExtRight.modMemWordValid
                    )
                    for (zdx <- 0 until cfg.regFileModRdPortCnt) {
                      myExtLeft.rdMemWord(zdx) := (
                        myExtRight.rdMemWord(zdx)
                      )
                      myExtLeft.memAddr(zdx) := (
                        myExtRight.memAddr(zdx)
                      )
                      if (cfg.optFormal) {
                        when (pastValidAfterReset) {
                          when (rose(rSetMidModPayloadState)) {
                            assert(
                              myExtLeft.memAddr(zdx)
                              === past(myExtRight.memAddr(zdx))
                            )
                            assert(
                              myExtLeft.rdMemWord(zdx)
                              === past(myExtRight.rdMemWord(zdx))
                            )
                            if (zdx == 0) {
                              assert(
                                myExtLeft.modMemWord
                                === past(myExtRight.modMemWord)
                              )
                              assert(
                                myExtLeft.modMemWordValid
                                === past(myExtRight.modMemWordValid)
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            case OpSelect.Alu => {
              for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
                val myExtLeft = tempExtLeft(ydx=ydx)
                val myExtRight = tempExtRight(ydx=ydx)
                myExtLeft.modMemWord := (
                  myExtRight.modMemWord
                )
                myExtLeft.modMemWordValid := (
                  myExtRight.modMemWordValid
                )
                for (zdx <- 0 until cfg.regFileModRdPortCnt) {
                  myExtLeft.rdMemWord(zdx) := (
                    myExtRight.rdMemWord(zdx)
                  )
                  myExtLeft.memAddr(zdx) := (
                    myExtRight.memAddr(zdx)
                  )
                  if (cfg.optFormal) {
                    when (pastValidAfterReset) {
                      when (rose(rSetMidModPayloadState)) {
                        assert(
                          myExtLeft.memAddr(zdx)
                          === past(myExtRight.memAddr(zdx))
                        )
                        assert(
                          myExtLeft.rdMemWord(zdx)
                          === past(myExtRight.rdMemWord(zdx))
                        )
                        if (zdx == 0) {
                          assert(
                            myExtLeft.modMemWord
                            === past(myExtRight.modMemWord)
                          )
                          assert(
                            myExtLeft.modMemWordValid
                            === past(myExtRight.modMemWordValid)
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
            //is (PipeMemRmwSimDut.ModOp.MulRaRb) 
            case OpSelect.MultiCycle => {
              for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
                val myExtLeft = tempExtLeft(ydx=ydx)
                val myExtRight = tempExtRight(ydx=ydx)
                myExtLeft.modMemWord := (
                  myExtRight.modMemWord
                )
                myExtLeft.modMemWordValid := (
                  myExtRight.modMemWordValid
                )
                for (zdx <- 0 until cfg.regFileModRdPortCnt) {
                  myExtLeft.memAddr(zdx) := (
                    myExtRight.memAddr(zdx)
                  )
                  myExtLeft.rdMemWord(zdx) := (
                    myExtRight.rdMemWord(zdx)
                  )
                  if (cfg.optFormal) {
                    when (pastValidAfterReset) {
                      when (rose(rSetMidModPayloadState)) {
                        assert(
                          myExtLeft.memAddr(zdx)
                          === past(myExtRight.memAddr(zdx))
                        )
                        assert(
                          myExtLeft.rdMemWord(zdx)
                          === past(myExtRight.rdMemWord(zdx))
                        )
                        if (zdx == 0) {
                          assert(
                            myExtLeft.modMemWord
                            === past(myExtRight.modMemWord)
                          )
                          assert(
                            myExtLeft.modMemWordValid
                            === past(myExtRight.modMemWordValid)
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  if (cfg.optFormal) {
    when (
      pastValidAfterReset
      && !myShouldIgnoreInstr
    ) {
      when (
        (
          /*past*/(cMidModFront.up.isValid) //init(False)
        ) && (
          !(
            /*past*/(rSetMidModPayloadState) //init(False)
          )
        )
      ) {
        def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
        def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          val myExtLeft = tempExtLeft(ydx=ydx)
          val myExtRight = tempExtRight(ydx=ydx)
          for (zdx <- 0 until cfg.regFileModRdPortCnt) {
            assert(
              myExtLeft.rdMemWord(zdx)
              === /*past*/(
                myExtRight.rdMemWord(zdx)
              )
            )
          }
        }
        assert(
          midModPayload(extIdxUp).op
          === /*past*/(modFront(modFrontPayload).op)
        )
        assert(
          midModPayload(extIdxUp).opCnt
          === /*past*/(modFront(modFrontPayload).opCnt)
        )
      }
      when (rose(rSetMidModPayloadState)) {
        def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
        //def tempExtRight(ydx: Int) = modFront(modFrontPayload).myExt(ydx)
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          val myExtLeft = tempExtLeft(ydx=ydx)
          //val myExtRight = tempExtRight(ydx=ydx)
          assert(stable(myExtLeft.rdMemWord))
          switch (midModPayload(extIdxUp).op) {
            for (
              ((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex
            ) {
              //is (PipeMemRmwSimDut.ModOp.AddRaRb)
              is (opInfoIdx) {
                val myPsExSetOutpModMemWordIo = (
                  midModPayload(extIdxUp).psExSetOutpModMemWordIo
                )
                def selRdMemWord(
                  idx: Int,
                ): UInt = {
                  myPsExSetOutpModMemWordIo.selRdMemWord(
                    opInfo=opInfo,
                    idx=idx,
                  )
                }
                opInfo.select match {
                  case OpSelect.Cpy if (
                    opInfo.memAccess == MemAccessKind.NoMemAccess
                  ) => {
                    assert(stable(myExtLeft.modMemWord))
                    //when (myExtLeft.modMemWordValid) {
                    //  assert(
                    //    myExtLeft.modMemWord
                    //    === myExtLeft.rdMemWord(0) + 1
                    //  )
                    //}
                    opInfo.cpyOp.get match {
                      case CpyOpKind.Cpy => {
                        when (myExtLeft.modMemWordValid) {
                          assert(
                            myExtLeft.modMemWord
                            === selRdMemWord(1)
                          )
                        }
                      }
                      case CpyOpKind.Cpyui => {
                        when (myExtLeft.modMemWordValid) {
                          assert(
                            myExtLeft.modMemWord(
                              cfg.mainWidth - 1
                              downto (cfg.mainWidth >> 1)
                            ) === (
                              selRdMemWord(1)(
                                (cfg.mainWidth >> 1) - 1
                                downto 0
                              )
                            )
                          )
                          assert(
                            myExtLeft.modMemWord(
                              (cfg.mainWidth >> 1) - 1
                              downto 0
                            ) === (
                              selRdMemWord(0)(
                                (cfg.mainWidth >> 1) - 1
                                downto 0
                              )
                            )
                          )
                        }
                      }
                      case CpyOpKind.Jmp => {
                      }
                      case CpyOpKind.Br => {
                      }
                    }
                  }
                  case OpSelect.Alu => {
                    assert(stable(myExtLeft.modMemWord))
                    opInfo.aluOp.get match {
                      case AluOpKind.Adc => {
                        assert(
                          false,
                          s"not yet implemented: "
                          + s"opInfo(${opInfo}) index:${opInfoIdx}"
                        )
                      }
                      case AluOpKind.Sbc => {
                        assert(
                          false,
                          s"not yet implemented: "
                          + s"opInfo(${opInfo}) index:${opInfoIdx}"
                        )
                      }
                      case op => {
                        val result = op.binopFunc(
                          cfg=cfg,
                          left=selRdMemWord(1),
                          right=selRdMemWord(2),
                          carry=False,
                        )
                        when (myExtLeft.modMemWordValid) {
                          assert(
                            myPsExSetOutpModMemWordIo.modMemWord(0)
                            === result.main
                          )
                        }
                      }
                    }
                  }
                  case OpSelect.MultiCycle => {
                    opInfo.multiCycleOp.get match {
                      case MultiCycleOpKind.Umul => {
                        when (myExtLeft.modMemWordValid) {
                          assert(
                            myPsExSetOutpModMemWordIo.modMemWord(0)
                            === (
                              (selRdMemWord(1) * selRdMemWord(2))(
                                cfg.mainWidth - 1 downto 0
                              )
                            )
                          )
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
                  case _ => {
                  }
                }
              }
            }
          }
        }
        assert(stable(midModPayload(extIdxUp).op))
        assert(stable(midModPayload(extIdxUp).opCnt))
      }
    }
  }
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
    midModPayload(extIdxUp).myExt.foreach(
      someExt => {
        someExt.modMemWordValid := False
      }
    )
    when (savedPsMemStallHost.myDuplicateIt) {
      //--------
      cMidModFront.duplicateIt()
      //--------
      //--------
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
      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        val myExtLeft = tempExtLeft(ydx=ydx)
        myExtLeft.modMemWord := myExtLeft.modMemWord.getZero
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
      switch (myDecodeExt.memAccessKind) {
        is (SnowHouseMemAccessKind.LoadU) {
          myCurrExt.modMemWordValid := True
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
        }
        is (SnowHouseMemAccessKind.LoadS) {
          myCurrExt.modMemWordValid := True
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
        }
        is (SnowHouseMemAccessKind.Store) {
        }
      }
      if (cfg.optFormal) {
        assume(
          myDecodeExt.memAccessKind.asBits.asUInt
          <= SnowHouseMemAccessKind.Store.asBits.asUInt
        )
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
        assert(
          front(frontPayload).opCnt
          === past(front(frontPayload).opCnt) + 1
        )
      }
      val myTempUpMod = regFile.cMid0FrontArea.tempUpMod(2)
      when (!psWb.tempHadFrontIsFiring._1) {
        //--------
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          assert(!myTempUpMod.myExt(ydx).modMemWordValid)
          assert(!modFront(modFrontPayload).myExt(ydx).modMemWordValid)
          assert(!modBack(modBackPayload).myExt(ydx).modMemWordValid)
        }
        //--------
        assert(!psWb.myHaveAnyCurrWrite)

        assert(!psWb.tempHadMid0FrontUpIsValid._1)
        assert(!psWb.tempHadMid0FrontDownIsFiring._1)
        assert(!psWb.tempHadModFrontIsValid._1)
        assert(!psWb.tempHadModBackIsFiring._1)
        assert(!psWb.tempHadBackIsFiring._1)
        assert(!psWb.myHaveSeenPipeToWrite.sFindFirst(_ === True)._1)
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          for (zdx <- 0 until cfg.regFileModRdPortCnt) {
            assert(!tempMyFindFirstUp(ydx, zdx)._1)
            assert(!tempMyFindFirstSaved(ydx, zdx)._1)
          }
        }
        assert(!regFile.cMid0FrontArea.up.isValid)
        assert(!modFront.isValid)
        assert(!modBack.isValid)
        assert(!back.isValid)
      }
      when (!psWb.tempHadMid0FrontUpIsValid._1) {
        assert(!psWb.myHaveAnyCurrWrite)
        when (!regFile.cMid0FrontArea.up.isValid) {
          //--------
          for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
            assert(!modFront(modFrontPayload).myExt(ydx).modMemWordValid)
            assert(!midModPayload(extIdxUp).myExt(ydx).modMemWordValid)
            for (zdx <- 0 until cfg.regFileModRdPortCnt) {
              assert(!tempMyFindFirstUp(ydx, zdx)._1)
              assert(!tempMyFindFirstSaved(ydx, zdx)._1)
            }
          }
          //--------
          //assert(!tempMyFindFirstUp._1)
          //assert(!tempMyFindFirstSaved._1)
        }
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          assert(!modBack(modBackPayload).myExt(ydx).modMemWordValid)
        }
        assert(!psWb.tempHadMid0FrontDownIsFiring._1)
        assert(!psWb.tempHadModFrontIsValid._1)
        assert(!psWb.tempHadModBackIsValid._1)
        assert(!psWb.tempHadModBackIsFiring._1)
        assert(!psWb.myHaveSeenPipeToWrite.sFindFirst(_ === True)._1)
        assert(!psWb.tempHadBackIsFiring._1)
        assert(!modFront.isValid)
        assert(!modBack.isValid)
        assert(!back.isValid)
      } 
      when (!psWb.tempHadModFrontIsValid._1) {
        //--------
        assert(!psWb.tempHadModBackIsValid._1)
        assert(!psWb.tempHadModBackIsFiring._1)
        assert(!psWb.myHaveSeenPipeToWrite.sFindFirst(_ === True)._1)
        assert(!psWb.tempHadBackIsFiring._1)
        assert(!modBack.isValid)
        assert(!back.isValid)
        //--------
        when (modFront.isValid) {
          when (pastValidAfterReset) {
            assert(
              psWb.tempHadMid0FrontDownIsValid._1
              || regFile.cMid0FrontArea.down.isValid
              || (
                past(regFile.cMid0FrontArea.down.isValid) init(False)
              )
            )
          }
        } otherwise {
          assert(!psWb.tempHadBackIsValid._1)
          assert(!psWb.myHaveAnyCurrWrite)
        }
        //--------
      }
      when (!psWb.tempHadModBackIsValid._1) {
        when (!modBack.isValid) {
          assert(
            !regFile.mod.back.myWriteEnable.foldLeft(False)(
              (l, r) => (l || r)
            )
          )
          assert(!psWb.myHaveAnyCurrWrite)
        }
        when (!modBack.isValid) {
          assert(!back.isValid)
          assert(!psWb.myHaveAnyCurrWrite)
          assert(!psWb.tempHadBackIsValid._1)
          assert(!back.isValid)
          assert(!back.isFiring)
          assert(!psWb.tempHadBackIsFiring._1)
        }
        assert(!psWb.tempHadModBackIsFiring._1)
      } otherwise {
        assert(psWb.tempHadMid0FrontDownIsValid._1)
      }
      when (!psWb.tempHadModBackIsFiring._1) {
      } otherwise {
        assert(
          psWb.tempHadMid0FrontDownIsValid._1
          || regFile.cMid0FrontArea.down.isValid
          || (
            past(regFile.cMid0FrontArea.down.isValid) init(False)
          )
        )
      }
      when (
        cMidModFront.up.isValid
        && psWb.tempHadFrontIsFiring._1
        && psWb.tempHadMid0FrontUpIsValid._1
        && psWb.tempHadMid0FrontDownIsValid._1
      ) {
        def tempExtLeft(ydx: Int) = midModPayload(extIdxUp).myExt(ydx)
        for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
          val myExtLeft = tempExtLeft(ydx=ydx)
          for (extIdx <- 0 until extIdxLim) {
            when (
              if (extIdx == extIdxUp) (
                cMidModFront.up.isFiring
              ) else ( // if (extIdx == extIdxSaved)
                False
              )
            ) {
              switch (midModPayload(extIdx).op) {
                for (
                  ((_, opInfo), opInfoIdx)
                  <- cfg.opInfoMap.view.zipWithIndex
                ) {
                  val myPsExSetOutpModMemWordIo = (
                    midModPayload(extIdx).psExSetOutpModMemWordIo
                  )
                  def selRdMemWord(
                    idx: Int,
                  ): UInt = {
                    myPsExSetOutpModMemWordIo.selRdMemWord(
                      opInfo=opInfo,
                      idx=idx,
                    )
                  }
                  is (opInfoIdx) {
                    opInfo.select match {
                      case OpSelect.Cpy if (
                        opInfo.memAccess == MemAccessKind.NoMemAccess
                      ) => {
                        opInfo.cpyOp.get match {
                          case CpyOpKind.Cpy => {
                            when (myExtLeft.modMemWordValid) {
                              assert(
                                myExtLeft.modMemWord
                                === selRdMemWord(1)
                              )
                            }
                          }
                          case CpyOpKind.Cpyui => {
                            when (myExtLeft.modMemWordValid) {
                              assert(
                                myExtLeft.modMemWord(
                                  cfg.mainWidth - 1
                                  downto (cfg.mainWidth >> 1)
                                ) === (
                                  selRdMemWord(1)(
                                    (cfg.mainWidth >> 1) - 1
                                    downto 0
                                  )
                                )
                              )
                              assert(
                                myExtLeft.modMemWord(
                                  (cfg.mainWidth >> 1) - 1
                                  downto 0
                                ) === (
                                  selRdMemWord(0)(
                                    (cfg.mainWidth >> 1) - 1
                                    downto 0
                                  )
                                )
                              )
                            }
                          }
                          case CpyOpKind.Jmp => {
                          }
                          case CpyOpKind.Br => {
                          }
                        }
                      }
                      case OpSelect.Alu => {
                        opInfo.aluOp.get match {
                          case AluOpKind.Adc => {
                            assert(
                              false,
                              s"not yet implemented: "
                              + s"opInfo(${opInfo}) index:${opInfoIdx}"
                            )
                          }
                          case AluOpKind.Sbc => {
                            assert(
                              false,
                              s"not yet implemented: "
                              + s"opInfo(${opInfo}) index:${opInfoIdx}"
                            )
                          }
                          case op => {
                            val result = op.binopFunc(
                              cfg=cfg,
                              left=selRdMemWord(1),
                              right=selRdMemWord(2),
                              carry=False,
                            )
                            when (myExtLeft.modMemWordValid) {
                              assert(
                                myPsExSetOutpModMemWordIo.modMemWord(0)
                                === result.main
                              )
                            }
                          }
                        }
                      }
                      case OpSelect.MultiCycle => {
                        opInfo.multiCycleOp.get match {
                          case MultiCycleOpKind.Umul => {
                            when (myExtLeft.modMemWordValid) {
                              assert(
                                myPsExSetOutpModMemWordIo.modMemWord(0)
                                === (
                                  (selRdMemWord(1) * selRdMemWord(2))(
                                    cfg.mainWidth - 1 downto 0
                                  )
                                )
                              )
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
                      case _ => {
                      }
                    }
                  }
                }
                //is (PipeMemRmwSimDut.ModOp.AddRaRb) {
                //  when (midModPayload(extIdx).myExt(0).modMemWordValid) {
                //    assert(
                //      midModPayload(extIdx).myExt(0).modMemWord
                //      === midModPayload(extIdx).myExt(0).rdMemWord(0) + 1
                //    )
                //  }
                //}
                //is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
                //  when (
                //    midModPayload(extIdx).myExt(0).modMemWordValid
                //  ) {
                //    if (PipeMemRmwSimDut.allModOpsSameChange) {
                //      assert(
                //        midModPayload(extIdx).myExt(0).modMemWord
                //        === (
                //          midModPayload(extIdx).myExt(0).rdMemWord(0)
                //          + 1
                //        )
                //      )
                //    } else {
                //      assert(
                //        midModPayload(extIdx).myExt(0).modMemWord
                //        === (
                //          midModPayload(extIdx).myExt(0).rdMemWord(0)
                //          - 1
                //        )
                //      )
                //    }
                //  }
                //}
                //is (PipeMemRmwSimDut.ModOp.MulRaRb) {
                //  when (midModPayload(extIdx).myExt(0).modMemWordValid) {
                //    if (PipeMemRmwSimDut.allModOpsSameChange) {
                //      assert(
                //        midModPayload(extIdx).myExt(0).modMemWord
                //        === (
                //          midModPayload(extIdx).myExt(0).rdMemWord(0) 
                //          + 1
                //        )
                //      )
                //    } else {
                //      val tempBitsRange = (
                //        //wordType().bitsRange
                //        cfg.mainWidth - 1 downto 0
                //      )
                //      assert(
                //        midModPayload(extIdx).myExt(0).modMemWord(
                //          tempBitsRange
                //        ) === (
                //          midModPayload(extIdx).myExt(0).rdMemWord(0) 
                //          << 1
                //        )(
                //          tempBitsRange
                //        )
                //      )
                //    }
                //  }
                //}
              }
            }
          }
        }
      }
    }
  }

  def setMidModStages(): Unit = {
    regFile.io.midModStages(0) := midModPayload
  }
  setMidModStages()

  modFront(modBackPayload) := midModPayload(extIdxUp)
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
                          val result = InstrResult(cfg=cfg)
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
                        case CpyOpKind.Cpyui => {
                          val result = InstrResult(cfg=cfg)
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
                          val result = InstrResult(cfg=cfg)
                          myDoFormalAssertRegular := False
                          //when (
                          //  modBack(modBackPayload).instrCnt
                          //  .shouldIgnoreInstr
                          //) {
                          //}
                          result
                        }
                        case CpyOpKind.Br => {
                          val result = InstrResult(cfg=cfg)
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
                          InstrResult(cfg=cfg)
                        }
                      }
                    }
                    case OpSelect.Alu => {
                      opInfo.aluOp.get match {
                        case AluOpKind.Adc => {
                          assert(
                            false,
                            s"not yet implemented: "
                            + s"opInfo(${opInfo}) idx:${opInfoIdx}"
                          )
                          InstrResult(cfg=cfg)
                        }
                        case AluOpKind.Sbc => {
                          assert(
                            false,
                            s"not yet implemented: "
                            + s"opInfo(${opInfo}) idx:${opInfoIdx}"
                          )
                          InstrResult(cfg=cfg)
                        }
                        case op => {
                          op.binopFunc(
                            cfg=cfg,
                            left=myLeft,
                            right=myRight,
                            carry=False,
                          )
                        }
                      }
                    }
                    case OpSelect.MultiCycle => {
                      opInfo.multiCycleOp.get match {
                        case MultiCycleOpKind.Umul => {
                          val result = InstrResult(cfg=cfg)
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
                          InstrResult(cfg=cfg)
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
