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
  //val multiCycleBusVec = (
  //  haveMultiCycleBusVec
  //) generate (
  //  Vec[LcvStallIo[
  //    MultiCycleHostPayload,
  //    MultiCycleDevPayload,
  //  ]]{
  //    val tempArr = ArrayBuffer[
  //      LcvStallIo[
  //        MultiCycleHostPayload,
  //        MultiCycleDevPayload,
  //      ]
  //    ]()
  //    for (((_, opInfo), idx) <- cfg.opInfoMap.view.zipWithIndex) {
  //      if (opInfo.select == OpSelect.MultiCycle) {
  //        tempArr += new LcvStallIo(
  //          hostPayloadType=(
  //            Some(HardType(MultiCycleHostPayload(cfg=cfg, opInfo=opInfo)))
  //          ),
  //          devPayloadType=(
  //            Some(HardType(MultiCycleDevPayload(cfg=cfg, opInfo=opInfo)))
  //          ),
  //        )
  //      }
  //    }
  //    tempArr
  //  }
  //)
  val dbus = new LcvStallIo(
    hostPayloadType=Some(HardType(DbusHostPayload(cfg=cfg))),
    devPayloadType=Some(HardType(DbusDevPayload(cfg=cfg))),
  )
  master(
    ibus,
    dbus,
  )
  //if (haveMultiCycleBusVec) {
  //  for (idx <- 0 until multiCycleBusVec.size) {
  //    master(multiCycleBusVec(idx))
  //  }
  //}
}
case class SnowHouse
//[
//  PipeStageInstrDecode <: SnowHousePipeStageInstrDecode
//]
(
  //gprWordType: HardType[GprWordT],
  cfg: SnowHouseConfig,
) extends Component {
  //--------
  val io = SnowHouseIo(cfg=cfg)
  //--------
  val psIdHaltIt = Bool()
  val psExSetPc = Flow(SnowHousePsExSetPcPayload(cfg=cfg))
  //--------
  val linkArr = PipeHelper.mkLinkArr()
  cfg.regFileCfg.linkArr = Some(linkArr)
  val regFile = new PipeMemRmw[
    UInt,
    Bool,
    SnowHousePipePayload,
    PipeMemRmwDualRdTypeDisabled[UInt, Bool],
  ](
    cfg=cfg.regFileCfg,
    modType=SnowHousePipePayload(cfg=cfg),
    dualRdType=PipeMemRmwDualRdTypeDisabled[UInt, Bool](),
  )(
    doModInFrontFunc=Some(
      (
        outp,
        inp,
        cFront,
        //ydx,
      ) => new Area {
        //GenerationFlags.formal {
          if (cfg.optFormal) {
            when (pastValidAfterReset) {
              when (
                cFront.up.isValid
                && past(cFront.up.isFiring)
              ) {
                assert(inp.opCnt === past(inp.opCnt) + 1)
              }
            }
            //assume(
            //  inp.op.asBits.asUInt
            //  //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
            //  //< PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
            //)
            inp.formalAssumes()
            when (pastValidAfterReset) {
              for ((tempExt, tempIdx) <- inp.myExt.zipWithIndex) {
                assert(stable(tempExt.hazardCmp))
                assert(stable(tempExt.modMemWord))
                assert(
                  stable(tempExt.rdMemWord)
                  //=== inp.myExt.rdMemWord.getZero
                )
                assert(
                  tempExt.rdMemWord
                  === tempExt.rdMemWord.getZero
                )
                assert(
                  stable(tempExt.hazardCmp)
                  //=== inp.myExt.hazardCmp.getZero
                )
                assert(stable(tempExt.modMemWordValid))
              }
            }
          }
        //}
      }
    ),
    doModInModFrontFunc=Some(
      (
        doModInModFrontParams
      ) => mkPipeStageExecute(
        doModInModFrontParams=doModInModFrontParams
      )
    )
  )
  //--------
  val pIf = Payload(SnowHousePipePayload(cfg=cfg))

  val cIf = CtrlLink(
    up={
      val node = Node()
      node.setName("cIf_up")
      node
    },
    down={
      val node = Node()
      node.setName("cIf_down")
      node
    }
  )
  linkArr += cIf
  cIf.up.valid := True
  val sIf = StageLink(
    up={
      cIf.down
    },
    down={
      val node = Node()
      node.setName("sIf_down")
      node
    }
  )
  linkArr += sIf
  val s2mIf = S2MLink(
    up={
      sIf.down
    },
    down={
      val node = Node()
      node.setName("s2mIf_down")
      node
    }
  )
  linkArr += s2mIf
  val pipeStageIf = SnowHousePipeStageInstrFetch(
    args=SnowHousePipeStageArgs(
      cfg=cfg,
      io=io,
      link=cIf,
      prevPayload=null,
      currPayload=pIf,
      regFile=regFile,
    ),
    psIdHaltIt=psIdHaltIt,
    psExSetPc=psExSetPc,
  )

  val cId = CtrlLink(
    up={
      //sIf.down
      s2mIf.down
    },
    down={
      regFile.io.front
    }
  )
  linkArr += cId
  //val pId = Payload(SnowHouseRegFileModType(cfg=cfg))
  val pipeStageId = cfg.mkPipeStageInstrDecode(
    SnowHousePipeStageArgs(
      cfg=cfg,
      io=io,
      link=cId,
      prevPayload=pIf,
      currPayload=(
        //pId
        regFile.io.frontPayload
      ),
      regFile=regFile,
    ),
    psIdHaltIt,
    psExSetPc,
  )
  //--------
  //val pEx = Payload(SnowHouseRegFileModType(cfg=cfg))
  def mkPipeStageExecute(
    doModInModFrontParams: PipeMemRmwDoModInModFrontFuncParams[
      UInt, Bool, SnowHousePipePayload
    ]
  ): SnowHousePipeStageExecute = SnowHousePipeStageExecute(
    args=SnowHousePipeStageArgs(
      cfg=cfg,
      io=io,
      link=null,
      prevPayload=null,
      currPayload=null,
      regFile=regFile,
    ),
    psExSetPc=psExSetPc,
    doModInModFrontParams=doModInModFrontParams,
  )
  //--------
  //io.ibus.nextValid := True
  //io.ibus.hostData.addr := 3

  //io.dbus.nextValid := False
  //io.dbus.hostData.addr := 8
  //io.dbus.hostData.data := 0x10c
  //io.dbus.hostData.accKind := DbusHostMemAccKind.Load
  //--------
  Builder(linkArr)
  //--------
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
