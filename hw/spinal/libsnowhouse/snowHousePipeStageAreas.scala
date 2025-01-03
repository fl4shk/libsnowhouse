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
  regFile: PipeMemRmw[
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
  val upModExt = SnowHousePipePayload(cfg=cfg)
  //val myInstrCnt = SnowHouseFormalInstrCnt(cfg=cfg)
  def myInstrCnt = upModExt.instrCnt
  up(pIf) := upModExt
  upModExt := (
    RegNext(
      next=upModExt,
      init=upModExt.getZero
    )
  )
  def nextRegPc = upModExt.regPc
  val rSavedExSetPc = {
    val temp = Reg(Flow(
      //UInt(cfg.mainWidth bits)
      SnowHousePsExSetPcPayload(cfg=cfg)
    ))
    temp.init(temp.getZero)
    temp
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
  val rPrevInstrCnt = (cfg.optFormal) generate (
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
    if (cfg.optFormal) {
      myInstrCnt.any := rPrevInstrCnt.any + 1
    }
    //--------
    when (psExSetPc.fire) {
      nextRegPc := psExSetPc.nextPc //+ (cfg.instrMainWidth / 8)
      if (cfg.optFormal) {
        myInstrCnt.jmp := rPrevInstrCnt.jmp + 1
      }
    } elsewhen (rSavedExSetPc.fire) {
      nextRegPc := (
        rSavedExSetPc.nextPc //+ (cfg.instrMainWidth / 8)
      )
      if (cfg.optFormal) {
        myInstrCnt.jmp := rPrevInstrCnt.jmp + 1
      }
    } otherwise {
      nextRegPc := rPrevRegPc + (cfg.instrMainWidth / 8)
      if (cfg.optFormal) {
        myInstrCnt.fwd := rPrevInstrCnt.fwd + 1
      }
    }
  }
  //--------
  io.ibus.nextValid := True
  io.ibus.hostData.addr := upModExt.regPc
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
    io.ibus.nextValid := False
    doStallMain()
    cIf.haltIt()
    if (cfg.optFormal) {
      myDoHaltIt := True
    }
  }
  when (!io.ibus.ready) {
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
  val myDecodeArea = doDecodeFunc(this)
}
case class SnowHousePipeStageExecuteSetOutpModMemWordIo(
  cfg: SnowHouseConfig,
) extends Area {
  val currOp = /*in*/(UInt(log2Up(cfg.opInfoMap.size) bits))
  //println(
  //  s"currOp.getWidth: ${currOp.getWidth}"
  //)
  //val doIt = /*in*/(Bool())
  val rdMemWord = /*in*/(Vec.fill(3)( // TODO: temporary size of `3`
    UInt(cfg.mainWidth bits)
  ))
  val regPcPlusImm = /*in*/(UInt(cfg.mainWidth bits))
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
  def jmpAddrIdx = 2
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
  io.opIsJmp.allowOverride
  io.opIsJmp := (
    io.psExSetPc.fire
  )
  switch (io.currOp) {
    //--------
    for (((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex) {
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
                    io.modMemWord(0) := io.rdMemWord(0)
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
                        modIo.dbus.hostData.subKind := (
                          //DbusHostMemAccessSubKind.fromWordSize(cfg=cfg)
                          mem.subKind match {
                            case MemAccessKind.SubKind.Sz8 => {
                              DbusHostMemAccessSubKind.Sz8
                            }
                            case MemAccessKind.SubKind.Sz16 => {
                              DbusHostMemAccessSubKind.Sz16
                            }
                            case MemAccessKind.SubKind.Sz32 => {
                              DbusHostMemAccessSubKind.Sz32
                            }
                            case MemAccessKind.SubKind.Sz64 => {
                              DbusHostMemAccessSubKind.Sz64
                            }
                          }
                        )
                        modIo.dbus.hostData.addr := (
                          (
                            opInfo.addrCalc match {
                              case AddrCalcKind.AddReduce => (
                                io.rdMemWord(1)
                              )
                              case kind: AddrCalcKind.LslThenMaybeAdd => (
                                io.rdMemWord(1)
                                << kind.options.lslAmount.get
                              )
                            }
                          ) + (
                            opInfo.srcArr.size match {
                              case 1 => {
                                U(s"${cfg.mainWidth}'d0")
                              }
                              case 2 => {
                                io.rdMemWord(2)
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
                        if (!isStore) {
                          modIo.dbus.hostData.accKind := (
                            if (!mem.isSigned) (
                              DbusHostMemAccessKind.LoadU
                            ) else (
                              DbusHostMemAccessKind.LoadS
                            )
                          )
                          modIo.dbus.hostData.data := (
                            modIo.dbus.hostData.data.getZero
                          )
                        } else { // if (isStore)
                          modIo.dbus.hostData.accKind := (
                            DbusHostMemAccessKind.Store
                          )
                          modIo.dbus.hostData.data := io.rdMemWord(0)
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
                  io.rdMemWord(1)((cfg.mainWidth >> 1) - 1 downto 0)
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
                io.psExSetPc.valid := True//io.doIt
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
                    io.psExSetPc.valid := (
                      //io.doIt
                      True
                    )
                    io.psExSetPc.nextPc := (
                      io.regPcPlusImm
                    )
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
                      //io.doIt
                      //&&
                      io.rdMemWord(0) === 0
                    )
                    io.psExSetPc.nextPc := (
                      io.regPcPlusImm
                      //io.rdMemWord(io.regPcPlusImm)
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
                      //io.doIt
                      //&&
                      io.rdMemWord(0) =/= 0
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
            opInfo.aluOp.get match {
              case AluOpKind.Add => {
                io.modMemWord(0) := (
                  io.rdMemWord(1) + io.rdMemWord(2)
                )
              }
              case AluOpKind.Adc => {
                assert(
                  false,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
              }
              case AluOpKind.Sub => {
                io.modMemWord(0) := (
                  io.rdMemWord(1) - io.rdMemWord(2)
                )
              }
              case AluOpKind.Sbc => {
                assert(
                  false,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
              }
              case AluOpKind.Lsl => {
                io.modMemWord(0) := (
                  (
                    io.rdMemWord(1)
                    << io.rdMemWord(2)(log2Up(cfg.mainWidth) - 1 downto 0)
                  )(
                    io.modMemWord(0).bitsRange
                  )
                )
              }
              case AluOpKind.Lsr => {
                io.modMemWord(0) := (
                  (io.rdMemWord(1) >> io.rdMemWord(2))(
                    io.modMemWord(0).bitsRange
                  )
                )
              }
              case AluOpKind.Asr => {
                io.modMemWord(0) := (
                  io.rdMemWord(1).asSInt >> io.rdMemWord(2)
                ).asUInt(
                  io.modMemWord(0).bitsRange
                )
              }
              case AluOpKind.And => {
                io.modMemWord(0) := (
                  io.rdMemWord(1) & io.rdMemWord(2)
                )
              }
              case AluOpKind.Or => {
                io.modMemWord(0) := (
                  io.rdMemWord(1) | io.rdMemWord(2)
                )
              }
              case AluOpKind.Xor => {
                io.modMemWord(0) := (
                  io.rdMemWord(1) ^ io.rdMemWord(2)
                )
              }
              case AluOpKind.Sltu => {
                io.modMemWord(0) := Cat(
                  U(s"${cfg.mainWidth - 1}'d0"),
                  io.rdMemWord(1) < io.rdMemWord(2),
                ).asUInt
              }
              case AluOpKind.Slts => {
                io.modMemWord(0) := Cat(
                  U(s"${cfg.mainWidth - 1}'d0"),
                  io.rdMemWord(1).asSInt < io.rdMemWord(2).asSInt,
                ).asUInt
              }
            }
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
                  (io.rdMemWord(1) * io.rdMemWord(2))(
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
  //}
}

case class SnowHousePipeStageExecute(
  //cfg: SnowHouseConfig,
  //io: SnowHouseIo,
  args: SnowHousePipeStageArgs,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
  doModInModFrontParams: PipeMemRmwDoModInModFrontFuncParams[
    UInt, Bool, SnowHousePipePayload
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
  def modFront = doModInModFrontParams.modFront
  def tempModFrontPayload = (
    doModInModFrontParams.tempModFrontPayload//Vec(ydxr
  )
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
  def mkPipeMemRmwSimDutStallHost[
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
    mkPipeMemRmwSimDutStallHost[
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
  val psMemStallHost = (
    mkPipeMemRmwSimDutStallHost(
      stallIo=(
        Some(io.dbus)
      ),
    )
  )
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
          val toFold = ArrayBuffer[Bool]()
          for (
            zdx 
            <- 0 until cfg.regFileModRdPortCnt
            //outp.myExt(ydx).memAddr.size
          ) {
            assert(
              outp.myExt(ydx).memAddr.size
              == cfg.regFileModRdPortCnt
            )
            toFold += (
              outp.myExt(ydx).memAddr(zdx)
              === tempModFrontPayload.myExt(ydx).memAddr(zdx)
            )
          }
          //toReduce.reduce(_ || _)
          toFold.foldLeft(False)((left, right) => (left || right))
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
    myDoHaveHazardVec.foldLeft(False)((left, right) => (left || right))
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
  outp.decodeExt := setOutpModMemWord.io.decodeExt
  psExSetPc := setOutpModMemWord.io.psExSetPc
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
    def tempRdMemWord = setOutpModMemWord.io.rdMemWord(zdx)
    tempRdMemWord := myRdMemWord(ydx=ydx, modIdx=zdx)
    outp.gprRdMemWordVec(zdx) := tempRdMemWord
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
          myDoHaveHazardAddrCheckVec.foldLeft(False)(
            (left, right) => (left || right)
          )
        ) && (
          cMid0Front.up.isFiring
        )
      )
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
  object PcChangeState
  extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      Idle,
      WaitTwoInstrs
      = newElement()
  }
  val nextPcChangeState = PcChangeState()
  val rPcChangeState = (
    RegNext(next=nextPcChangeState)
    init(PcChangeState.Idle)
  )
  nextPcChangeState := rPcChangeState
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
        is (M"--10") {
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
        is (M"--11") {
          // instruction is of type Cpy (non-Jmp, non-Br)/Alu,
          // but WITH mem access
          when (cMid0Front.up.isFiring) {
            nextPrevTxnWasHazard := True
            psMemStallHost.nextValid := True
          }
        }
        is (M"-1--") {
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
  when (currDuplicateIt) {
    //handleDuplicateIt()
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      outp.myExt(ydx).valid := False
      outp.myExt(ydx).modMemWordValid := (
        //someModMemWordValid
        False
      )
    }
    cMid0Front.duplicateIt()
  }
}
