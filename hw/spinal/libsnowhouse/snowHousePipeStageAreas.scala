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
  payload: Payload[SnowHouseRegFileModType[EncInstrT]],
  optFormal: Boolean,
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
abstract class SnowHousePsDecode[
  EncInstrT <: Data
](
  var args: Option[SnowHousePipeStageArgs[EncInstrT]]=None
) extends Area {
  //def decInstr: UInt
}

case class SnowHousePsExecute[
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
  def myRdMemWord(ydx: Int) = (
    doModInModFrontParams.getMyRdMemWordFunc(ydx)
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
    val myDoHaveHazardAddrCheck = Vec[Vec[Bool]]{
      val temp = ArrayBuffer[Vec[Bool]]()
      for (ydx <- 0 until outp.myExt.size) {
        temp += {
          val innerTemp = ArrayBuffer[Bool]()
          for (zdx <- 0 until outp.myExt(ydx).memAddr.size) {
            innerTemp += (
              outp.myExt(ydx).memAddr(zdx)
              === tempModFrontPayload.myExt(ydx).memAddr(zdx)
            )
          }
          Vec[Bool]{innerTemp}
        }
      }
      temp
    }
    val myDoHaveHazardValidCheck = Vec[Bool]{
      //(
      //  !tempModFrontPayload.myExt(0).modMemWordValid
      //)
      val temp = ArrayBuffer[Bool]()
      for (ydx <- 0 until tempModFrontPayload.myExt.size) {
        !tempModFrontPayload.myExt(ydx).modMemWordValid
      }
      temp
    }
    //--------
    val nextDoHaveHazardState = (
      KeepAttribute(
        Bool()
      )
      .setName(
        s"doTestModOpMainArea_"
        + s"nextDoHaveHazardState"
      )
    )
    //--------
    //val myDoHaveHazard = Vec[Bool]{
    //  val temp = ArrayBuffer[Bool]()
    //  for (ydx <- 0 until myDoHaveHazardValidCheck.size) {
    //    temp += (
    //      myDoHaveHazardAddrCheck
    //      && myDoHaveHazardValidCheck(ydx)
    //    )
    //  }
    //  temp
    //}
    val rTempPrevOp = (
      RegNextWhen(
        next=myCurrOp,
        cond=cMid0Front.up.isFiring,
        init=U"${myCurrOp.getWidth}'d0"
      )
    )
  }
}
