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
//    cfg: SnowHouseConfig[EncInstrT],
//  ): ArrayBuffer[OpInfo] = {
//    val ret = ArrayBuffer[OpInfo]()
//    for (((_, opInfo), idx) <- cfg.opInfoMap.view.zipWithIndex) {
//      ret += null
//    }
//    ret
//  }
//}
case class SnowHousePipeStageArgs[
  EncInstrT <: Data
](
  //encInstrType: HardType[EncInstrT],
  cfg: SnowHouseConfig[EncInstrT],
  opInfoMap: LinkedHashMap[Any, OpInfo],
  io: SnowHouseIo[EncInstrT],
  link: CtrlLink,
  //prevLink: Option[CtrlLink],
  //nextLink: Option[CtrlLink],
  payload: Payload[SnowHouseRegFileModType[EncInstrT]],
  //optFormal: Boolean,
) {
}
//case class SnowHousePipeStagePayload[
//  EncInstrT <: Data
//](
//  cfg: SnowHouseConfig[EncInstrT],
//  //encInstrType: HardType[EncInstrT],
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
//  cfg: SnowHouseConfig[EncInstrT],
//) extends Area {
//}
case class SnowHousePipeStageInstrFetch[
  EncInstrT <: Data
](
  args: SnowHousePipeStageArgs[EncInstrT],
  psIdHaltIt: Bool,
  psExSetPc: Flow[UInt],
) extends Area {
  //val io = SnowHousePipeStageInstrFetchIo(cfg=cfg)
  val cfg = args.cfg
  val io = args.io
  val cIf = args.link
  val pIf = args.payload
  //--------
  val up = cIf.up
  val down = cIf.down
  //--------
  val upModExt = SnowHouseRegFileModType(cfg=cfg)
  val myInstrCnt = SnowHouseFormalInstrCnt(cfg=cfg)
  up(pIf) := upModExt
  upModExt := (
    RegNext(upModExt)
    init(upModExt.getZero)
  )
  def nextRegPc = upModExt.regPc
  val rSavedExSetPc = (
    Reg(Flow(UInt(cfg.mainWidth bits)))
  )
  rSavedExSetPc.init(rSavedExSetPc.getZero)

  when (psExSetPc.fire) {
    rSavedExSetPc := psExSetPc
  }
  val rPrevRegPc = (
    RegNextWhen(
      //upModExt.regPc,
      nextRegPc,
      up.isFiring,
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
      nextRegPc := psExSetPc.payload //+ (cfg.instrMainWidth / 8)
      if (cfg.optFormal) {
        myInstrCnt.jmp := rPrevInstrCnt.jmp + 1
      }
    } elsewhen (rSavedExSetPc.fire) {
      nextRegPc := (
        rSavedExSetPc.payload //+ (cfg.instrMainWidth / 8)
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
  //when (exSetPc.fire) {
  //  upModExt.regPc := exSetPc.payload + (cfg.instrMainWidth / 8)
  //}
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
      //init(io.ibus.hostData.addr.getZero)
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
          assert(nextRegPc === psExSetPc.payload)
          assert(stable(myInstrCnt.fwd))
          assert(myInstrCnt.jmp === rPrevInstrCnt.jmp + 1)
        } otherwise { // when (rSavedExSetPc.fire)
          assert(nextRegPc === rSavedExSetPc.payload)
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
abstract class SnowHousePipeStageInstrDecode[
  EncInstrT <: Data
](
  var args: Option[SnowHousePipeStageArgs[EncInstrT]]=None
) extends Area {
  //def decInstr: UInt
  def cfg = args.get.cfg
  def opInfoMap = args.get.opInfoMap
  def io = args.get.io
  def cId = args.get.link
  def payload = args.get.payload
  def optFormal = cfg.optFormal
}
case class SnowHousePipeStageExecuteSetOutpModMemWordIo[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Bundle {
  val currOp = in(UInt(log2Up(cfg.opInfoMap.size) bits))
  val rdMemWord = in(Vec.fill(2)( // temporary size of `2`
    UInt(cfg.mainWidth bits)
  ))
  val modMemWordValid = out(Bool())
  val modMemWord = out(UInt(cfg.mainWidth bits))
}
case class SnowHousePipeStageExecuteSetOutpModMemWord[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Component {
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
    True
  )
  switch (io.currOp) {
    //--------
    for (((_, opInfo), opInfoIdx) <- cfg.opInfoMap.view.zipWithIndex) {
      is (U"${io.currOp.getWidth}'d${opInfoIdx}") {
        opInfo.select match {
          case OpSelect.Cpy => {
            opInfo.cpyOp.get match {
              case CpyOpKind.Cpy => {
                assert(
                  opInfo.cond == CondKind.Always,
                  s"not yet implemented"
                )
                //assert(
                //  opInfo.memAccess == MemAccessKind.NoMemAccess,
                //  s"not yet implemented"
                //)
                //assert(
                //  opInfo.addrCalc == AddrCalcKind.AddReduce,
                //  s"not yet implemented"
                //)
                io.modMemWord := io.rdMemWord(0)
              }
              case CpyOpKind.Cpyui => {
                assert(
                  opInfo.cond == CondKind.Always,
                  s"not yet implemented"
                )
                assert(
                  opInfo.memAccess == MemAccessKind.NoMemAccess,
                  s"not yet implemented"
                )
                assert(
                  opInfo.addrCalc == AddrCalcKind.AddReduce,
                  s"not yet implemented"
                )
                io.modMemWord(
                  cfg.mainWidth - 1 downto (cfg.mainWidth >> 1)
                ) := (
                  io.rdMemWord(0)((cfg.mainWidth >> 1) - 1 downto 0)
                )
              }
              case CpyOpKind.Jmp => {
                assert(
                  opInfo.cond == CondKind.Always,
                  s"not yet implemented"
                )
                assert(
                  opInfo.memAccess == MemAccessKind.NoMemAccess,
                  s"not yet implemented"
                )
                assert(
                  opInfo.addrCalc == AddrCalcKind.AddReduce,
                  s"not yet implemented"
                )
              }
              case CpyOpKind.Br => {
                opInfo.cond match {
                  case CondKind.Z => {
                  }
                  case CondKind.Nz => {
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
              s"not yet implemented"
            )
            assert(
              opInfo.memAccess == MemAccessKind.NoMemAccess,
              s"not yet implemented"
            )
            assert(
              opInfo.addrCalc == AddrCalcKind.AddReduce,
              s"not yet implemented"
            )
            opInfo.aluOp.get match {
              case AluOpKind.Add => {
                io.modMemWord := (
                  io.rdMemWord(0) + io.rdMemWord(1)
                )
              }
              case AluOpKind.Adc => {
                assert(
                  false,
                  "not yet implemented"
                )
              }
              case AluOpKind.Sub => {
                io.modMemWord := (
                  io.rdMemWord(0) - io.rdMemWord(1)
                )
              }
              case AluOpKind.Sbc => {
                assert(
                  false,
                  "not yet implemented"
                )
              }
              case AluOpKind.Lsl => {
                io.modMemWord := (
                  (io.rdMemWord(0) << io.rdMemWord(1)).resized
                )
              }
              case AluOpKind.Lsr => {
                io.modMemWord := (
                  (io.rdMemWord(0) >> io.rdMemWord(1)).resized
                )
              }
              case AluOpKind.Asr => {
                io.modMemWord := (
                  io.rdMemWord(0).asSInt >> io.rdMemWord(1)
                ).asUInt.resized
              }
              case AluOpKind.And => {
                io.modMemWord := (
                  io.rdMemWord(0) & io.rdMemWord(1)
                )
              }
              case AluOpKind.Or => {
                io.modMemWord := (
                  io.rdMemWord(0) | io.rdMemWord(1)
                )
              }
              case AluOpKind.Xor => {
                io.modMemWord := (
                  io.rdMemWord(0) ^ io.rdMemWord(1)
                )
              }
              case AluOpKind.Sltu => {
                io.modMemWord := Cat(
                  U"${cfg.mainWidth - 1}'d0",
                  io.rdMemWord(0) < io.rdMemWord(1),
                ).asUInt
              }
              case AluOpKind.Slts => {
                io.modMemWord := Cat(
                  U"${cfg.mainWidth - 1}'d0",
                  io.rdMemWord(0).asSInt < io.rdMemWord(1).asSInt,
                ).asUInt
              }
            }
          }
          case OpSelect.MultiCycle => {
            assert(
              opInfo.cond == CondKind.Always,
              s"not yet implemented"
            )
            assert(
              opInfo.memAccess == MemAccessKind.NoMemAccess,
              s"not yet implemented"
            )
            assert(
              opInfo.addrCalc == AddrCalcKind.AddReduce,
              s"not yet implemented"
            )
          }
        }
      }
    }
    default {
      assert(False)
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

case class SnowHousePipeStageExecute[
  EncInstrT <: Data,
](
  cfg: SnowHouseConfig[EncInstrT],
  io: SnowHouseIo[EncInstrT],
  doModInModFrontParams: PipeMemRmwDoModInModFrontFuncParams[
    UInt, Bool, SnowHouseRegFileModType[EncInstrT]
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
    doModInModFrontParams.tempModFrontPayload//Vec(ydx)
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
      assume(
        inp.op
        < cfg.opInfoMap.size
      )
      assume(
        outp.op
        < cfg.opInfoMap.size
      )
      assume(
        myCurrOp
        < cfg.opInfoMap.size
      )
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
        None
      ),
    )
  )
  val psMemStallHost = (
    mkPipeMemRmwSimDutStallHost(
      stallIo=(
        //io.psMemStallIo
        //None,
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
        assert(
          !rSetOutpState
        )
      }
      when (!(past(cMid0Front.up.isValid) init(False))) {
        assert(
          stable(rSetOutpState)
        )
      }
      when (rSetOutpState) {
        assert(
          cMid0Front.up.isValid
        )
      }
    }
  }
  if (cfg.optFormal) {
    assert(
      myCurrOp === outp.op
    )
    when (pastValidAfterReset) {
      when (
        rose(rSetOutpState)
      ) {
        assert(
          myCurrOp
          === past(inp.op)
        )
        assert(
          outp.op === past(inp.op)
        )
        assert(
          outp.opCnt
          === past(inp.opCnt)
        )
      }
    }
  }
  for (ydx <- 0 until outp.myExt.size) {
    outp.myExt(ydx).rdMemWord := (
      //myRdMemWord
      inp.myExt(ydx).rdMemWord
    )
  }
  val doTestModOpMainArea = new Area {
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
      Bool()
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
    val myDoHaveHazardAddrCheck = Vec[Bool]{
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
    val myDoHaveHazardValidCheck = Vec[Bool]{
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
    val myDoHaveHazard = Vec[Bool]{
      //val tempFindFirst = Vec[(Bool, UInt)]
      //val tempFindFirst_1 = Bool()
      //val tempFindFirst_2: UInt = null
      //tempFindFirst_1
      val tempArr = ArrayBuffer[Bool]()
      assert(
        myDoHaveHazardAddrCheck.size
        == myDoHaveHazardValidCheck.size
      )
      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        tempArr += (
          myDoHaveHazardAddrCheck(ydx)
          && myDoHaveHazardValidCheck(ydx)
        )
      }
      tempArr//.reduce(_ || _)
    }
    val rTempPrevOp = (
      RegNextWhen(
        next=myCurrOp,
        cond=cMid0Front.up.isFiring,
        init=U"${myCurrOp.getWidth}'d0"
      )
    )
  }
  val myOutpModMemWordValid = (
    KeepAttribute(
      Bool()
    )
  )
  val myOutpModMemWord = (
    KeepAttribute(
      UInt(cfg.mainWidth bits)
    )
  )
}
