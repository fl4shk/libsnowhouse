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

//--------
case class IbusHostPayload[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Bundle {
  val addr = UInt(cfg.mainWidth bits)
  //val data = UInt(cfg.mainWidth bits)
}
case class IbusDevPayload[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Bundle {
  val instr = UInt(cfg.instrMainWidth bits)
}
//--------
//object MultiCycleOpEnum
//extends SpinalEnum(defaultEncoding=native) {
//  val
//    Umul,
//    Smul,
//    Udiv,
//    Sdiv,
//    Umod,
//    Smod,
//    Udivmod,
//    Sdivmod
//    = newElement()
//}
case class MultiCycleHostPayload[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
  opInfo: OpInfo,
) extends Bundle {
  assert(
    cfg.opInfoMap.find(_._2 == opInfo) != None
  )
  assert(
    opInfo.select == OpSelect.MultiCycle
  )
  val srcVec = Vec.fill(opInfo.srcArr.size)(
    UInt(cfg.mainWidth bits)
  )
  //opInfo.multiCycleOp.get match {
  //  case MultiCycleOpKind.Umul => {
  //  }
  //  case MultiCycleOpKind.Smul => {
  //  }
  //  case MultiCycleOpKind.Udiv => {
  //  }
  //  case MultiCycleOpKind.Sdiv => {
  //  }
  //  case MultiCycleOpKind.Umod => {
  //  }
  //  case MultiCycleOpKind.Smod => {
  //  }
  //  case MultiCycleOpKind.Udivmod => {
  //  }
  //  case MultiCycleOpKind.Sdivmod => {
  //  }
  //  case _ => {
  //    assert(false)
  //  }
  //}
}
case class MultiCycleDevPayload[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
  opInfo: OpInfo,
) extends Bundle {
  assert(
    cfg.opInfoMap.find(_._2 == opInfo) != None
  )
  assert(
    opInfo.select == OpSelect.MultiCycle
  )
  val dstVec = Vec.fill(opInfo.dstArr.size)(
    UInt(cfg.mainWidth bits)
  )
}
//--------
object DbusHostMemAccessKind
extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    Load,
    Store
    = newElement();
}
object DbusHostMemAccessSubKind
extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    Sz8,
    Sz16,
    Sz32,
    Sz64
    = newElement()
}
case class DbusHostPayload[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Bundle {
  val addr = UInt(cfg.mainWidth bits)
  val data = UInt(cfg.mainWidth bits)
  val accKind = DbusHostMemAccessKind()
  val subKind = DbusHostMemAccessSubKind()
  val lock = Bool() // for atomics
}
case class DbusDevPayload[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Bundle {
  val data = UInt(cfg.mainWidth bits)
}
//--------
