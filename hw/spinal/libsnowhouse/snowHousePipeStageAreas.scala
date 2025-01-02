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
  opInfoMap: LinkedHashMap[Any, OpInfo],
  io: SnowHouseIo,
  link: CtrlLink,
  //prevLink: Option[CtrlLink],
  //nextLink: Option[CtrlLink],
  payload: Payload[SnowHouseRegFileModType],
  //optFormal: Boolean,
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
  //val io = SnowHousePipeStageInstrFetchIo(cfg=cfg)
  def cfg = args.cfg
  def io = args.io
  def cIf = args.link
  def pIf = args.payload
  //--------
  val up = cIf.up
  val down = cIf.down
  //--------
  val upModExt = SnowHouseRegFileModType(cfg=cfg)
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
  val rSavedExSetPc = (
    Reg(Flow(
      //UInt(cfg.mainWidth bits)
      SnowHousePsExSetPcPayload(cfg=cfg)
    ))
  )
  rSavedExSetPc.init(rSavedExSetPc.getZero)

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
abstract class SnowHousePipeStageInstrDecode(
  var args: Option[SnowHousePipeStageArgs]=None
) extends Area {
  //def decInstr: UInt
  def cfg = args.get.cfg
  def opInfoMap = args.get.opInfoMap
  def io = args.get.io
  def cId = args.get.link
  def payload = args.get.payload
  def optFormal = cfg.optFormal
}
case class SnowHousePipeStageExecuteSetOutpModMemWordIo(
  cfg: SnowHouseConfig,
) extends Area {
  val currOp = /*in*/(UInt(log2Up(cfg.opInfoMap.size) bits))
  val doIt = /*in*/(Bool())
  val rdMemWord = /*in*/(Vec.fill(3)( // temporary size of `3`
    UInt(cfg.mainWidth bits)
  ))
  val regPcPlusImm = /*in*/(UInt(cfg.mainWidth bits))
  val modMemWordValid = /*out*/(Bool())
  val modMemWord = /*out*/(Vec.fill(1)( // temporary size of `1`
    UInt(cfg.mainWidth bits)
  ))
  val psExSetPc = /*out*/(Flow(
    //UInt(cfg.mainWidth bits)
    SnowHousePsExSetPcPayload(cfg=cfg)
  ))
  val dbusHostPayload = /*out*/(DbusHostPayload(cfg=cfg))
  def jmpAddrIdx = 2
}
case class SnowHousePipeStageExecuteSetOutpModMemWord(
  cfg: SnowHouseConfig,
) extends Area {
  val io = SnowHousePipeStageExecuteSetOutpModMemWordIo(cfg=cfg)
  //val io.currOp = UInt(log2Up(cfg.opInfoMap.size) bits)
  io.modMemWordValid := (
    RegNext(
      next=io.modMemWordValid,
      init=io.modMemWordValid.getZero,
    )
  )
  io.modMemWord := (
    RegNext(
      next=io.modMemWord,
      init=io.modMemWord.getZero,
    )
  )
  //when (someCond) {
  io.modMemWordValid := (
    io.doIt //True
  )
  io.psExSetPc := io.psExSetPc.getZero
  io.dbusHostPayload := (
    RegNext(
      next=io.dbusHostPayload,
      init=io.dbusHostPayload.getZero,
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
      assert(io.currOp < cfg.opInfoMap.size)
    }
  }
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
      is (U"${io.currOp.getWidth}'d${opInfoIdx}") {
        opInfo.select match {
          case OpSelect.Cpy => {
            opInfo.cpyOp.get match {
              case CpyOpKind.Cpy => {
                assert(
                  opInfo.cond == CondKind.Always,
                  s"not yet implemented: "
                  + s"opInfo(${opInfo}) index:${opInfoIdx}"
                )
                opInfo.memAccess match {
                  case MemAccessKind.NoMemAccess => {
                    assert(
                      opInfo.dstArr.size == 1,
                      s"invalid opInfo.dstArr.size"
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    assert(
                      opInfo.srcArr.size == 1,
                      s"invalid opInfo.srcArr.size"
                      + s"opInfo(${opInfo}) index:${opInfoIdx}"
                    )
                    io.modMemWord(0) := io.rdMemWord(0)
                  }
                  case mem: MemAccessKind.Mem => {
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
                        io.dbusHostPayload.subKind := (
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
                        io.dbusHostPayload.addr := (
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
                                U"${cfg.mainWidth}'d0"
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
                          io.dbusHostPayload.accKind := (
                            if (!mem.isSigned) (
                              DbusHostMemAccessKind.LoadU
                            ) else (
                              DbusHostMemAccessKind.LoadS
                            )
                          )
                          io.dbusHostPayload.data := (
                            io.dbusHostPayload.data.getZero
                          )
                        } else { // if (isStore)
                          io.dbusHostPayload.accKind := (
                            DbusHostMemAccessKind.Store
                          )
                          io.dbusHostPayload.data := io.rdMemWord(0)
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
                io.psExSetPc.valid := io.doIt
                io.psExSetPc.nextPc := io.rdMemWord(io.jmpAddrIdx)
              }
              case CpyOpKind.Br => {
                opInfo.cond match {
                  case CondKind.Z => {
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
                    io.psExSetPc.valid := (
                      io.doIt
                      && io.rdMemWord(0) === 0
                    )
                    io.psExSetPc.nextPc := (
                      io.regPcPlusImm
                      //io.rdMemWord(io.regPcPlusImm)
                    )
                  }
                  case CondKind.Nz => {
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
                    io.psExSetPc.valid := (
                      io.doIt
                      && io.rdMemWord(0) =/= 0
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
                  (io.rdMemWord(1) << io.rdMemWord(2))(
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
                  U"${cfg.mainWidth - 1}'d0",
                  io.rdMemWord(1) < io.rdMemWord(2),
                ).asUInt
              }
              case AluOpKind.Slts => {
                io.modMemWord(0) := Cat(
                  U"${cfg.mainWidth - 1}'d0",
                  io.rdMemWord(1).asSInt < io.rdMemWord(2).asSInt,
                ).asUInt
              }
            }
          }
          case OpSelect.MultiCycle => {
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
  cfg: SnowHouseConfig,
  io: SnowHouseIo,
  psExSetPc: Flow[SnowHousePsExSetPcPayload],
  doModInModFrontParams: PipeMemRmwDoModInModFrontFuncParams[
    UInt, Bool, SnowHouseRegFileModType
  ],
) extends Area {
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
      init=U"${myCurrOp.getWidth}'d0"
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
      Bool,
      Bool,
    ](
      stallIo=(
        //io.psExStallIo
        // TODO: support external `MultiCycleOp`
        None
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
  } otherwise {
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
  doCheckHazard := (
    RegNext(
      next=doCheckHazard,
      init=doCheckHazard.getZero,
    )
  )
  val myDoHaveHazardAddrCheckVec = Vec[Bool]{
    //val temp = ArrayBuffer[Vec[Bool]]()
    assert(
      outp.myExt.size == cfg.regFileCfg.memArrSize
    )
    val temp = ArrayBuffer[Bool]()
    //for (
    //  ydx
    //  <- 0 until cfg.regFileCfg.memArrSize
    //  //outp.myExt.size
    //) {
    //  temp += {
    //    val innerTemp = ArrayBuffer[Bool]()
    //    for (
    //      zdx 
    //      <- 0 until cfg.regFileModRdPortCnt
    //      //outp.myExt(ydx).memAddr.size
    //    ) {
    //      assert(
    //        outp.myExt(ydx).memAddr.size
    //        == cfg.regFileModRdPortCnt
    //      )
    //      innerTemp += (
    //        outp.myExt(ydx).memAddr(zdx)
    //        === tempModFrontPayload.myExt(ydx).memAddr(zdx)
    //      )
    //    }
    //    Vec[Bool]{innerTemp}
    //  }
    //}
    for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
      temp += {
        val toReduce = ArrayBuffer[Bool]()
        for (
          zdx 
          <- 0 until cfg.regFileModRdPortCnt
          //outp.myExt(ydx).memAddr.size
        ) {
          assert(
            outp.myExt(ydx).memAddr.size
            == cfg.regFileModRdPortCnt
          )
          toReduce += (
            outp.myExt(ydx).memAddr(zdx)
            === tempModFrontPayload.myExt(ydx).memAddr(zdx)
          )
        }
        toReduce.reduce(_ || _)
      }
    }
    temp
  }
  val myDoHaveHazardValidCheckVec = Vec[Bool]{
    //(
    //  !tempModFrontPayload.myExt(0).modMemWordValid
    //)
    val temp = ArrayBuffer[Bool]()
    for (
      ydx
      <- 0 until cfg.regFileCfg.memArrSize
      //tempModFrontPayload.myExt.size
    ) {
      !tempModFrontPayload.myExt(ydx).modMemWordValid
    }
    temp
  }
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
        == myDoHaveHazardValidCheckVec.size
      )
      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        tempArr += (
          myDoHaveHazardAddrCheckVec(ydx)
          && myDoHaveHazardValidCheckVec(ydx)
        )
      }
      tempArr//.reduce(_ || _)
    }
  )
  val myDoHaveHazard = KeepAttribute(
    myDoHaveHazardVec.reduce(_ || _)
  )
  val rTempPrevOp = (
    RegNextWhen(
      next=myCurrOp,
      cond=cMid0Front.up.isFiring,
      init=U"${myCurrOp.getWidth}'d0"
    )
  )
  val setOutpModMemWord = SnowHousePipeStageExecuteSetOutpModMemWord(
    cfg=cfg
  )
  setOutpModMemWord.io.currOp := myCurrOp
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
  //def handleCurrFire(
  //  someRdMemWord: UInt=myRdMemWord,
  //): Unit = {
  //  //outp.myExt(0).valid := True
  //  nextPrevTxnWasHazard := False
  //  setOutpModMemWord(
  //    someRdMemWord=someRdMemWord
  //  )
  //  outp.myExt(0).valid := (
  //    outp.myExt(0).modMemWordValid
  //  )
  //}
  //def handleDuplicateIt(
  //  someModMemWordValid: Bool=False,
  //): Unit = {
  //  outp.myExt(0).valid := False
  //  outp.myExt(0).modMemWordValid := (
  //    someModMemWordValid
  //  )
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
          myDoHaveHazardAddrCheckVec.reduce(_ || _)
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
    // TODO: re-add this
    //handleCurrFire()
  }
  switch (myCurrOp) {
    // TODO: convert this
    //is (PipeMemRmwSimDut.ModOp.AddRaRb) {
    //  if (cfg.optFormal) {
    //    when (cMid0Front.up.isValid) {
    //      when (!doCheckHazard) {
    //        assert(!currDuplicateIt)
    //      }
    //    }
    //  }
    //}
    //is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
    //  when (cMid0Front.up.isFiring) {
    //    nextPrevTxnWasHazard := True
    //    psMemStallHost.nextValid := True
    //  }
    //}
    //is (PipeMemRmwSimDut.ModOp.MulRaRb) {
    //  if (PipeMemRmwSimDut.haveModOpMul) {
    //    //--------
    //    when (cMid0Front.up.isValid) {
    //      when (doCheckHazard) {
    //        when (!currDuplicateIt) {
    //          psExStallHost.nextValid := (
    //            True
    //          )
    //        }
    //      } otherwise { // when (!doCheckHazard)
    //        psExStallHost.nextValid := (
    //          True
    //        )
    //      }
    //      //--------
    //    }
    //    when (savedPsExStallHost.myDuplicateIt) {
    //      currDuplicateIt := True
    //    }
    //    if (cfg.optFormal) {
    //      when (!doCheckHazard) {
    //        when (!savedPsExStallHost.myDuplicateIt) {
    //          assert(!currDuplicateIt)
    //        }
    //      }
    //    }
    //  }
    //}
  }
  // TODO: convert this
  //if (PipeMemRmwSimDut.haveModOpMul) {
  //  when (psExStallHost.fire) {
  //    psExStallHost.nextValid := False
  //  }
  //}
  when (currDuplicateIt) {
    // TODO: add this
    //handleDuplicateIt()
  }
}
