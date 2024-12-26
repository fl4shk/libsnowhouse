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

sealed trait SnowHouseLdKind
object SnowHouseLdKind {
  case object LdU8 extends SnowHouseLdKind
  case object LdS8 extends SnowHouseLdKind
  case object LdU16 extends SnowHouseLdKind
  case object LdS16 extends SnowHouseLdKind
  case object LdU32 extends SnowHouseLdKind
  case object LdS32 extends SnowHouseLdKind
  case object Ld64 extends SnowHouseLdKind
}
sealed trait SnowHouseStKind
object SnowHouseStKind {
  case object St8 extends SnowHouseStKind
  case object St16 extends SnowHouseStKind
  case object St32 extends SnowHouseStKind
  case object St64 extends SnowHouseStKind
}

case class SnowHouseRegFileConfig(
  mainWidth: Int,
  wordCountArr: Seq[Int],
  modRdPortCnt: Int,
  pipeName: String,
  //linkArr: Option[ArrayBuffer[Link]]=None,
) {
  val modStageCnt: Int = 1
}

case class SnowHouseConfig(
  //gprFileDepth: Int,
  //sprFileDepth: Int,
  instrMainWidth: Int,
  shRegFileCfg: SnowHouseRegFileConfig,
  ldKindSet: LinkedHashSet[SnowHouseLdKind],
  stKindSet: LinkedHashSet[SnowHouseStKind],
) {
  def mainWidth = shRegFileCfg.mainWidth
  def regFileWordCountArr = shRegFileCfg.wordCountArr
  def regFileModRdPortCnt = shRegFileCfg.modRdPortCnt
  def regFileModStageCnt = shRegFileCfg.modStageCnt
  def regFilePipeName = shRegFileCfg.pipeName
  val regFileCfg = PipeMemRmwConfig[UInt, Bool](
    wordType=UInt(mainWidth bits),
    wordCountArr=regFileWordCountArr,
    hazardCmpType=Bool(),
    modRdPortCnt=regFileModRdPortCnt,
    modStageCnt=regFileModStageCnt,
    pipeName=regFilePipeName,
    //linkArr=linkArr
  )
  regFileCfg.linkArr = None
}

case class SnowHouseIoIbusHostPayload(
  cfg: SnowHouseConfig,
) extends Bundle {
  val addr = UInt(cfg.mainWidth bits)
  //val data = UInt(cfg.mainWidth bits)
}
case class SnowHouseIoIbusDevPayload(
  cfg: SnowHouseConfig,
) extends Bundle {
  val instr = UInt(cfg.instrMainWidth bits)
}
case class SnowHouseIoDbusHostPayload(
  cfg: SnowHouseConfig,
) extends Bundle {
  val addr = UInt(cfg.mainWidth bits)
  val data = UInt(cfg.mainWidth bits)
}
case class SnowHouseIoDbusDevPayload(
  cfg: SnowHouseConfig,
) extends Bundle {
  val data = UInt(cfg.mainWidth bits)
}

case class SnowHouseIo(
  cfg: SnowHouseConfig
) extends Bundle {
  //val icache = 
  // instruction bus
  val ibus = master(new LcvStallIo(
    hostPayloadType=Some(HardType(SnowHouseIoIbusHostPayload(cfg=cfg))),
    devPayloadType=Some(HardType(SnowHouseIoIbusDevPayload(cfg=cfg))),
  ))
  val dbus = master(new LcvStallIo(
    hostPayloadType=Some(HardType(SnowHouseIoDbusHostPayload(cfg=cfg))),
    devPayloadType=Some(HardType(SnowHouseIoDbusDevPayload(cfg=cfg))),
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
}

object SnowHouseToVerilog extends App {
  Config.spinal.generateVerilog(SnowHouse(
    cfg=SnowHouseConfig(
      instrMainWidth=32,
      shRegFileCfg=SnowHouseRegFileConfig(
        mainWidth=32,
        wordCountArr=(
          Array.fill(1)(16)
        ),
        modRdPortCnt=3,
        pipeName="SnowHouseToVerilog",
      ),
      ldKindSet=LinkedHashSet[SnowHouseLdKind](),
      stKindSet=LinkedHashSet[SnowHouseStKind](),
    )
  ))
}
