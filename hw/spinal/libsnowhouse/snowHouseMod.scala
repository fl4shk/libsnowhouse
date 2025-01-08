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
//case class SnowHouseInstrRamIo(
//  cfg: SnowHouseConfig
//) extends Component {
//}
case class SnowHouseInstrDataDualRamIo(
  cfg: SnowHouseConfig,
) extends Bundle {
  val ibus = new LcvStallIo[IbusHostPayload, IbusDevPayload ](
    hostPayloadType=Some(IbusHostPayload(cfg=cfg)),
    devPayloadType=Some(IbusDevPayload(cfg=cfg)),
  )
  val dbus = new LcvStallIo[DbusHostPayload, DbusDevPayload](
    hostPayloadType=Some(DbusHostPayload(cfg=cfg)),
    devPayloadType=Some(DbusDevPayload(cfg=cfg)),
  )
  slave(
    ibus,
    dbus,
  )
}
case class SnowHouseInstrDataDualRam(
  cfg: SnowHouseConfig,
  //isInstr: Boolean,
  instrInitBigInt: Seq[BigInt],
  dataInitBigInt: Seq[BigInt],
) extends Component {
  //val io = slave(new LcvStallIo[HostPayloadT, DevPayloadT](
  //  hostPayloadType=Some(hostPayloadType()),
  //  devPayloadType=Some(devPayloadType()),
  //))
  val io = SnowHouseInstrDataDualRamIo(cfg=cfg)
  //--------
  val instrRamDepth = instrInitBigInt.size
  val instrRam = FpgacpuRamSimpleDualPort(
    wordType=UInt(cfg.instrMainWidth bits),
    depth=instrRamDepth,
    initBigInt=Some(instrInitBigInt),
  )
  //--------
  //io.ibus.ready := io.ibus.rValid
  io.ibus.ready := False
  val rIbusReadyCnt = Reg(UInt(8 bits)) init(0)
  val rIbusReadyState = Reg(Bool()) init(False)
  when (io.ibus.rValid) {
    when (rIbusReadyCnt > 0) {
      rIbusReadyCnt := rIbusReadyCnt - 1
    } otherwise {
      io.ibus.ready := True
      rIbusReadyState := !rIbusReadyState
      when (!rIbusReadyState) {
        rIbusReadyCnt := 2
      } otherwise {
        rIbusReadyCnt := 0
      }
    }
  }
  //--------
  instrRam.io.rdEn := io.ibus.nextValid
  instrRam.io.rdAddr := (
    (io.ibus.hostData.addr >> 2).resized
  )
  io.ibus.devData.instr := instrRam.io.rdData.asUInt
  instrRam.io.wrEn := False
  instrRam.io.wrAddr := instrRam.io.wrAddr.getZero
  instrRam.io.wrData := instrRam.io.wrData.getZero
  //--------
  val dataRamDepth = dataInitBigInt.size
  val dataRam = FpgacpuRamSimpleDualPort(
    wordType=UInt(cfg.mainWidth bits),
    depth=dataRamDepth,
    initBigInt=Some(dataInitBigInt),
  )
  io.dbus.ready := io.dbus.rValid
  //io.dbus.ready := False
  //val rDbusReadyCnt = Reg(UInt(8 bits)) init(2)
  //when (io.dbus.rValid) {
  //  when (rDbusReadyCnt > 0) {
  //    rDbusReadyCnt := rDbusReadyCnt - 1
  //  } otherwise {
  //    rDbusReadyCnt := 2
  //    io.dbus.ready := True
  //  }
  //}
  io.dbus.devData.data := io.dbus.hostData.data.getZero

  dataRam.io.rdEn := False
  dataRam.io.rdAddr := dataRam.io.wrAddr.getZero
  dataRam.io.wrEn := False
  dataRam.io.wrAddr := dataRam.io.wrAddr.getZero
  dataRam.io.wrData := dataRam.io.wrData.getZero
  switch (io.dbus.hostData.accKind) {
    is (SnowHouseMemAccessKind.LoadU) {
      dataRam.io.rdEn := io.dbus.nextValid
      dataRam.io.rdAddr := (
        (io.dbus.hostData.addr >> 2).resized
      )
      io.dbus.devData.data := dataRam.io.rdData.asUInt
    }
    is (SnowHouseMemAccessKind.LoadS) {
      dataRam.io.rdEn := io.dbus.nextValid
      dataRam.io.rdAddr := (
        (io.dbus.hostData.addr >> 2).resized
      )
      io.dbus.devData.data := dataRam.io.rdData.asUInt
    }
    is (SnowHouseMemAccessKind.Store) {
      // TODO: possibly update this to work better?
      dataRam.io.wrEn := io.dbus.nextValid
      dataRam.io.wrAddr := (
        (io.dbus.hostData.addr >> 2).resized
      )
      dataRam.io.wrData := io.dbus.hostData.data.asBits
    }
  }
  
}
//--------
case class SnowHouseIo(
  cfg: SnowHouseConfig
) extends Bundle {
  //val icache = 
  // instruction bus
  val ibus = new LcvStallIo[IbusHostPayload, IbusDevPayload ](
    hostPayloadType=Some(IbusHostPayload(cfg=cfg)),
    devPayloadType=Some(IbusDevPayload(cfg=cfg)),
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
              Some(MultiCycleHostPayload(cfg=cfg, opInfo=opInfo))
            ),
            devPayloadType=(
              Some(MultiCycleDevPayload(cfg=cfg, opInfo=opInfo))
            ),
          )
        }
      }
      tempArr
    }
  )
  val dbus = new LcvStallIo[DbusHostPayload, DbusDevPayload](
    hostPayloadType=Some(DbusHostPayload(cfg=cfg)),
    devPayloadType=Some(DbusDevPayload(cfg=cfg)),
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
  val psExSetPc = (
    KeepAttribute(
      Flow(SnowHousePsExSetPcPayload(cfg=cfg))
    )
    .setName(s"SnowHouse_psExSetPc")
  )
  val psMemStallHost = (
    cfg.mkLcvStallHost(
      stallIo=(
        Some(io.dbus)
      ),
    )
  )
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
                when (inp.opCnt =/= past(inp.opCnt)) {
                  assert(inp.opCnt === past(inp.opCnt) + 1)
                }
              }
            }
            //assume(
            //  inp.op.asBits.asUInt
            //  //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
            //  //< PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
            //)
            val temp = inp.formalAssumes()
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
      //(
      //  doModInModFrontParams,
      //  myRegFile1,
      //) => 
      mkPipeStageExecute
      //(
      //  doModInModFrontParams=doModInModFrontParams,
      //  myRegFile=myRegFile1,
      //)
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
  //val s2mIf = S2MLink(
  //  up={
  //    sIf.down
  //  },
  //  down={
  //    val node = Node()
  //    node.setName("s2mIf_down")
  //    node
  //  }
  //)
  //linkArr += s2mIf
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
      sIf.down
      //s2mIf.down
    },
    down={
      regFile.io.front
    }
  )
  linkArr += cId
  //val pId = Payload(SnowHouseRegFileModType(cfg=cfg))
  val pipeStageId = SnowHousePipeStageInstrDecode(
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
    doDecodeFunc=cfg.doInstrDecodeFunc,
  )
  //--------
  //val pEx = Payload(SnowHouseRegFileModType(cfg=cfg))
  def mkPipeStageExecute(
    doModInModFrontParams: PipeMemRmwDoModInModFrontFuncParams[
      UInt,
      Bool,
      SnowHousePipePayload,
      PipeMemRmwDualRdTypeDisabled[UInt, Bool],
    ],
    //myRegFile: PipeMemRmw[
    //  UInt,
    //  Bool,
    //  SnowHousePipePayload,
    //  PipeMemRmwDualRdTypeDisabled[UInt, Bool],
    //],
  ): SnowHousePipeStageExecute = SnowHousePipeStageExecute(
    args=SnowHousePipeStageArgs(
      cfg=cfg,
      io=io,
      link=null,
      prevPayload=null,
      currPayload=null,
      regFile=null,
    ),
    psExSetPc=psExSetPc,
    psMemStallHost=psMemStallHost,
    doModInModFrontParams=doModInModFrontParams,
  )
  //--------
  val pipeStageWb = (
    //cfg.optFormal
    true
  ) generate (
    SnowHousePipeStageWriteBack(
      args=SnowHousePipeStageArgs(
        cfg=cfg,
        io=io,
        link=null,
        prevPayload=null,
        currPayload=null,
        regFile=regFile,
      ),
    )
  )
  val pipeStageMem = (
    SnowHousePipeStageMem(
      args=SnowHousePipeStageArgs(
        cfg=cfg,
        io=io,
        link=null,
        prevPayload=null,
        currPayload=null,
        regFile=regFile,
      ),
      psWb=pipeStageWb,
      psMemStallHost=psMemStallHost,
    )
  )
  regFile.io.back.ready := True
  Builder(linkArr)
  //--------
}

//object SnowHouseToVerilog extends App {
//  Config.spinal.generateVerilog(SnowHouse(
//    cfg=SnowHouseConfig(
//      haveZeroReg=Some(0),
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
