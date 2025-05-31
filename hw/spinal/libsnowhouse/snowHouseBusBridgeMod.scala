package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._

import libcheesevoyage.general._
import libcheesevoyage.bus.lcvStall._

case class SnowHouseToLcvStallBusBridgeIo(
  cfg: SnowHouseConfig,
) extends Bundle {
}
