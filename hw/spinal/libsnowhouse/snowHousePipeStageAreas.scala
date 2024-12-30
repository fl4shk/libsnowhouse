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
  opInfoMap: LinkedHashMap[Any, OpInfo],
  io: SnowHouseIo[EncInstrT],
  link: CtrlLink,
  payload: Payload[SnowHousePipeStagePayload[EncInstrT]],
  optFormal: Boolean,
) {
}
case class SnowHousePipeStagePayload[
  EncInstrT <: Data
](
  encInstrType: HardType[EncInstrT],
) extends Bundle {
}
abstract class SnowHousePsDecode[
  EncInstrT <: Data
](
  var args: Option[SnowHousePipeStageArgs[EncInstrT]]=None
) extends Area {
  def decInstr: UInt
}

case class SnowHousePsExecute[
  EncInstrT <: Data,
](
  cfg: SnowHouseConfig[EncInstrT],
  io: SnowHouseIo[EncInstrT],
  doModInModFrontParams: PipeMemRmwDoModInModFrontFuncParams[
    UInt, Bool, SnowHouseRegFileModType[EncInstrT]
  ],
  //extraAssumes: Option[
  //  (
  //    SnowHouseConfig[EncInstrT],           // cfg
  //    SnowHouseIo[EncInstrT],               // io
  //    PipeMemRmwDoModInModFrontFuncParams[  // doModInModFrontParams
  //      UInt, Bool, SnowHouseRegFileModType[EncInstrT]
  //    ],
  //    EncInstrT,                            // myCurrOp
  //  ) => Area
  //]=None,
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
  val currDuplicateIt = Bool()
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
  //val myExtraAssumes: (
  //  Boolean,
  //  (
  //    SnowHouseConfig[EncInstrT],           // cfg
  //    SnowHouseIo[EncInstrT],               // io
  //    PipeMemRmwDoModInModFrontFuncParams[  // doModInModFrontParams
  //      UInt, Bool, SnowHouseRegFileModType[EncInstrT]
  //    ],
  //    EncInstrT,                            // myCurrOp
  //  ) => Area
  //) = (
  //  //if (cfg.optFormal) {
  //    extraAssumes match {
  //      case Some(extraAssumes) => {
  //        (true, extraAssumes)
  //      }
  //      case None => {
  //        (false, null)
  //      }
  //    }
  //  //}
  //)
  //val extraAssumesArea = (
  //  cfg.optFormal
  //  && myExtraAssumes._1
  //) generate (
  //  myExtraAssumes._2(cfg, io, doModInModFrontParams, myCurrOp)
  //)
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
}
