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
object DbusHostMemAccKind
extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    Load,
    Store
    = newElement();
}
case class MultiCycleHostPayload[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Bundle {
}
//object DbusHostMemAccSz
//extends SpinalEnum(defaultEncoding=binarySequential) {
//  val
//    sz
//}
case class DbusHostPayload[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Bundle {
  val addr = UInt(cfg.mainWidth bits)
  val data = UInt(cfg.mainWidth bits)
  val accKind = DbusHostMemAccKind()
}
case class DbusDevPayload[
  EncInstrT <: Data
](
  cfg: SnowHouseConfig[EncInstrT],
) extends Bundle {
  val data = UInt(cfg.mainWidth bits)
}
//--------