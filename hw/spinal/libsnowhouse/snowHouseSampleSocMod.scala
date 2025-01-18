package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.bus.tilelink
import spinal.core.fiber.Fiber

import libcheesevoyage.general._
import libcheesevoyage.math._
import libcheesevoyage.bus.lcvStall._

case class SnowHouseSocConfig(
  shCfg: SnowHouseConfig,
) {
}
case class SnowHouseSocIo(
  socCfg: SnowHouseSocConfig,
) extends Bundle {
  def shCfg = socCfg.shCfg
}
case class SnowHouseSoc(
  socCfg: SnowHouseSocConfig,
) extends Component {
  def shCfg = socCfg.shCfg
}
