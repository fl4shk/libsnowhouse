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

//sealed trait SnowHouseInstrSourceKind


//--------
case class SnowHouseIo(
  cfg: SnowHouseConfig
) extends Bundle {
  //val icache = 
  // instruction bus
  val ibus = master(new LcvStallIo(
    hostPayloadType=Some(HardType(IbusHostPayload(cfg=cfg))),
    devPayloadType=Some(HardType(IbusDevPayload(cfg=cfg))),
  ))
  val dbus = master(new LcvStallIo(
    hostPayloadType=Some(HardType(DbusHostPayload(cfg=cfg))),
    devPayloadType=Some(HardType(DbusDevPayload(cfg=cfg))),
  ))
}
case class SnowHouse
//[
//  GprWordT <: Data
//]
(
  //gprWordType: HardType[GprWordT],
  cfg: SnowHouseConfig,
) extends Component {
  //--------
  val io = SnowHouseIo(cfg=cfg)
  //--------
  val linkArr = PipeHelper.mkLinkArr()
  cfg.regFileCfg.linkArr = Some(linkArr)
  //--------
  io.ibus.nextValid := False
  io.ibus.hostData.addr := 3

  io.dbus.nextValid := False
  io.dbus.hostData.addr := 8
  io.dbus.hostData.data := 0x10c
  io.dbus.hostData.accKind := DbusHostMemAccKind.Load
}

object SnowHouseToVerilog extends App {
  Config.spinal.generateVerilog(SnowHouse(
    cfg=SnowHouseConfig(
      instrMainWidth=32,
      shRegFileCfg=RegFileConfig(
        mainWidth=32,
        wordCountArr=(
          Array.fill(1)(16)
        ),
        modRdPortCnt=3,
        pipeName="SnowHouseToVerilog",
      ),
      ldKindSet=LinkedHashSet[LoadOpKind](),
      stKindSet=LinkedHashSet[StoreOpKind](),
    )
  ))
}
