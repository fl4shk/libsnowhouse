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
  val ibus = new LcvStallIo(
    hostPayloadType=Some(HardType(IbusHostPayload(cfg=cfg))),
    devPayloadType=Some(HardType(IbusDevPayload(cfg=cfg))),
  )
  val haveMultiCycleBusVec = (
    cfg.opInfoMap.find(_._2.select == OpSelect.MultiCycle) != None
  )
  val multiCycleBusVec = (
    haveMultiCycleBusVec
  ) generate (
    Vec[LcvStallIo[
      MultiCycleHostPayload,
      MultiCycleDevPayload,
    ]]{
      val tempArr = ArrayBuffer[
        LcvStallIo[
          MultiCycleHostPayload,
          MultiCycleDevPayload,
        ]
      ]()
      for (((_, opInfo), idx) <- cfg.opInfoMap.view.zipWithIndex) {
        if (opInfo.select == OpSelect.MultiCycle) {
          tempArr += new LcvStallIo(
            hostPayloadType=(
              Some(HardType(MultiCycleHostPayload(cfg=cfg, opInfo=opInfo)))
            ),
            devPayloadType=(
              Some(HardType(MultiCycleDevPayload(cfg=cfg, opInfo=opInfo)))
            ),
          )
        }
      }
      tempArr
    }
  )
  val dbus = new LcvStallIo(
    hostPayloadType=Some(HardType(DbusHostPayload(cfg=cfg))),
    devPayloadType=Some(HardType(DbusDevPayload(cfg=cfg))),
  )
  master(
    ibus,
    dbus,
  )
  if (haveMultiCycleBusVec) {
    for (idx <- 0 until multiCycleBusVec.size) {
      master(multiCycleBusVec(idx))
    }
  }
}
case class SnowHouse(
  //gprWordType: HardType[GprWordT],
  cfg: SnowHouseConfig,
) extends Component {
  //--------
  val io = SnowHouseIo(cfg=cfg)
  //--------
  val linkArr = PipeHelper.mkLinkArr()
  cfg.regFileCfg.linkArr = Some(linkArr)
  //--------
  //io.ibus.nextValid := True
  //io.ibus.hostData.addr := 3

  //io.dbus.nextValid := False
  //io.dbus.hostData.addr := 8
  //io.dbus.hostData.data := 0x10c
  //io.dbus.hostData.accKind := DbusHostMemAccKind.Load
}

//object SnowHouseToVerilog extends App {
//  Config.spinal.generateVerilog(SnowHouse(
//    cfg=SnowHouseConfig(
//      instrMainWidth=32,
//      shRegFileCfg=SnowHouseRegFileConfig(
//        mainWidth=32,
//        wordCountArr=(
//          Array.fill(1)(16)
//        ),
//        modRdPortCnt=3,
//        pipeName="SnowHouseToVerilog",
//      ),
//      opInfoMap={
//        val opInfoMap = LinkedHashMap[Any, OpInfo]()
//        opInfoMap
//      },
//      //ldKindSet=LinkedHashSet[LoadOpKind](),
//      //stKindSet=LinkedHashSet[StoreOpKind](),
//    )
//  ))
//}
