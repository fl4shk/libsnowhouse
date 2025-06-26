package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.bus.tilelink

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
  val ibus = new LcvStallIo[BusHostPayload, BusDevPayload](
    sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=true)),
    recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=true)),
  )
  val dbus = new LcvStallIo[BusHostPayload, BusDevPayload](
    sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=false)),
    recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=false)),
  )
  //val dcacheHaveHazard = Bool()
  val dbusExtraReady = Vec.fill(
    cfg.lowerMyFanout
  )(
    Bool()
  )
  slave(
    ibus,
    dbus,
  )
  out(dbusExtraReady)
  //in(dcacheHaveHazard)
}
//case class SnowHouseDirectMappedIcacheIo(
//  cfg: SnowHouseConfig
//) extends Bundle {
//}
//case class SnowHouseDirectMappedIcache(
//  cfg: SnowHouseConfig
//) extends Component {
//}
case class SnowHouseInstrDataDualRam(
  cfg: SnowHouseConfig,
  //isInstr: Boolean,
  instrInitBigInt: Seq[BigInt],
  dataInitBigInt: Seq[BigInt],
) extends Component {
  //val io = slave(new LcvStallIo[HostPayloadT, DevPayloadT](
  //  sendPayloadType=Some(sendPayloadType()),
  //  recvPayloadType=Some(recvPayloadType()),
  //))
  val io = SnowHouseInstrDataDualRamIo(cfg=cfg)
  //--------
  val instrRamKind = (
    //false
    //true
    //0
    //5
    //2
    1
  )
  // BEGIN: old, non-icache code
  val myNonIcacheArea = (
    //noIcache
    instrRamKind > 0
  ) generate (
    new Area {
      val instrRamDepth = instrInitBigInt.size
      val instrRam = FpgacpuRamSimpleDualPort(
        wordType=UInt(cfg.instrMainWidth bits),
        depth=instrRamDepth,
        initBigInt=Some(instrInitBigInt),
      )
      //--------
      val fastIbusReady = (
        //true
        false
      )
      //val fastDbusReady = (
      //  //true
      //  false
      //)
      val rReadyPipe1 = (
        Reg(Bool())
        init(False)
      )
      val rInstrPipe1 = (
        Reg(UInt(cfg.instrMainWidth bits))
        init(0x0)
      )

      val rIbusReadyCnt = Reg(UInt(8 bits)) init(0)
      val rIbusReadyState = Reg(Bool()) init(False)
      if (fastIbusReady) {
        io.ibus.ready := io.ibus.rValid
      } else {
        io.ibus.ready.setAsReg() init(False)
        io.ibus.ready := False
        //rReadyPipe.head := False
        //io.ibus.ready := rReadyPipe1
        //rReadyPipe1 := False
        //io.ibus.recvData.instr.setAsReg() init(0)
        when (/*RegNext*/(io.ibus.nextValid)) {
          when (
            rIbusReadyCnt > 0
          ) {
            rIbusReadyCnt := rIbusReadyCnt - 1
          } otherwise {
            rIbusReadyState := !rIbusReadyState
            //io.ibus.ready := True
            when (!rIbusReadyState) {
              rIbusReadyCnt := (
                //5
                instrRamKind
              )
            } otherwise {
              rIbusReadyCnt := 0
            }
          }
          when ((rIbusReadyCnt - 1).msb) {
            io.ibus.ready := True
          }
        }
      }
      instrRam.io.rdEn := /*RegNext*/(io.ibus.nextValid)
      instrRam.io.rdAddr := (
        /*RegNext*/(io.ibus.sendData.addr >> 2).resized
      )
      io.ibus.recvData.instr := /*RegNext*/(instrRam.io.rdData.asUInt)
      //--------
      //if (fastIbusReady) {
      //  //instrRam.io.rdEn := io.ibus.nextValid
      //  //instrRam.io.rdAddr := (
      //  //  (io.ibus.sendData.addr >> 2).resized
      //  //)
      //  io.ibus.recvData.instr := instrRam.io.rdData.asUInt
      //} else {
      //  //instrRam.io.rdEn := /*RegNext*/(io.ibus.nextValid) //init(False)
      //  //instrRam.io.rdAddr := (
      //  //  (/*RegNext*/((io.ibus.sendData.addr >> 2)) /*init(0x0)*/).resized
      //  //)
      //  io.ibus.recvData.instr := RegNext(instrRam.io.rdData.asUInt) init(0x0)
      //}
      instrRam.io.wrEn := False
      instrRam.io.wrAddr := instrRam.io.wrAddr.getZero
      instrRam.io.wrData := instrRam.io.wrData.getZero
    }
  )
  // END: old, non-icache code
  //--------
  //--------
  val instrRamArea = (
    //!noIcache
    instrRamKind == 0
  ) generate (
    new Area {
      setName("SnowHouseInstrDataDualRam_instrRamArea")
      val depth = instrInitBigInt.size
      val icache = SnowHouseCache(
        cfg=cfg,
        isIcache=true,
        //forFmax=(
        //  false
        //  //true
        //),
      )
      //icache.io.haveHazard := io.icacheHaveHazard
      val m2sTransfers = tilelink.M2sTransfers(
        get=tilelink.SizeRange(
          cfg.mainWidth / 8,
          //cfg.mainWidth / 8,
          icache.cacheCfg.lineSizeBytes
        ),
        putFull=tilelink.SizeRange(
          cfg.mainWidth / 8,
          //cfg.mainWidth / 8
          icache.cacheCfg.lineSizeBytes
        ),
      )
      val addrMapping = spinal.lib.bus.misc.SizeMapping(
        base=0x0,
        size=(
          //1
          //cfg.mainWidth / 8
          //depth
          //2
          cfg.subCfg.totalNumBusHosts
          //icache.bridgeCfg.tlCfg.sizeBytes
        ),
      )
      val m2sSource = tilelink.M2sSource(
        id=addrMapping,
        emits=m2sTransfers,
      )
      val m2sAgent = tilelink.M2sAgent(
        name=this,
        mapping=m2sSource
      )
      val m2sCfg = tilelink.M2sParameters(
        addressWidth=(
          //cfg.mainWidth
          log2Up(depth * (cfg.mainWidth / 8))
        ),
        dataWidth=cfg.instrMainWidth,
        masters=Array[tilelink.M2sAgent](m2sAgent),
      )
      val myRam = new tilelink.Ram(
        p=m2sCfg.toNodeParameters(),
        bytes=(
          depth * (cfg.instrMainWidth / 8)
        ),
      )
      myRam.mem.initBigInt(instrInitBigInt, allowNegative=true)

      icache.io.bus.nextValid := (
        io.ibus.nextValid
      )
      icache.io.bus.sendData := (
        io.ibus.sendData
      )
      io.ibus.recvData := (
        icache.io.bus.recvData
      )
      io.ibus.ready := (
        icache.io.bus.ready
      )
      //io.ibusExtraReady.addAttribute(KeepAttribute.keep)
      //io.ibusExtraReady := icache.io.busExtraReady

      //myRam.io.up << icache.io.tlBus
      //myRam.io.up.a << icache.io.tlBus.a
      myRam.io.up.a.opcode := icache.io.tlBus.a.opcode
      myRam.io.up.a.param := icache.io.tlBus.a.param
      myRam.io.up.a.source := icache.io.tlBus.a.source
      myRam.io.up.a.address := icache.io.tlBus.a.address.resize(
        myRam.io.up.a.address.getWidth
      )
      myRam.io.up.a.size := icache.io.tlBus.a.size
      myRam.io.up.a.mask := icache.io.tlBus.a.mask
      myRam.io.up.a.data := icache.io.tlBus.a.data
      myRam.io.up.a.corrupt := icache.io.tlBus.a.corrupt
      myRam.io.up.a.debugId := icache.io.tlBus.a.debugId

      myRam.io.up.a.valid := icache.io.tlBus.a.valid
      icache.io.tlBus.a.ready := myRam.io.up.a.ready

      //myRam.io.up.a.address.allowOverride
      //myRam.io.up.a.address := (
      //  icache.io.tlBus.a.address.resized
      //)
      icache.io.tlBus.d << myRam.io.up.d
      //myRam.io.up.a <-/< icache.io.tlBus.a
      //icache.io.tlBus.d <-/< myRam.io.up.d
    }
  )
  val dataRamArea = new Area {
    setName("SnowHouseInstrDataDualRam_dataRamArea")
    val depth = dataInitBigInt.size
    val dcache = SnowHouseCache(
      cfg=cfg,
      isIcache=false,
      //forFmax=true,
    )
    //dcache.io.haveHazard := io.dcacheHaveHazard
    val m2sTransfers = tilelink.M2sTransfers(
      get=tilelink.SizeRange(
        cfg.mainWidth / 8,
        //cfg.mainWidth / 8,
        dcache.cacheCfg.lineSizeBytes
      ),
      putFull=tilelink.SizeRange(
        cfg.mainWidth / 8,
        //cfg.mainWidth / 8
        dcache.cacheCfg.lineSizeBytes
      ),
    )
    val addrMapping = spinal.lib.bus.misc.SizeMapping(
      base=0x0,
      size=(
        //1
        //cfg.mainWidth / 8
        //depth
        //2
        cfg.subCfg.totalNumBusHosts
        //dcache.bridgeCfg.tlCfg.sizeBytes
      ),
    )
    val m2sSource = tilelink.M2sSource(
      id=addrMapping,
      emits=m2sTransfers,
    )
    val m2sAgent = tilelink.M2sAgent(
      name=this,
      mapping=m2sSource
    )
    val m2sCfg = tilelink.M2sParameters(
      addressWidth=(
        //cfg.mainWidth
        log2Up(depth * (cfg.mainWidth / 8))
      ),
      dataWidth=cfg.mainWidth,
      masters=Array[tilelink.M2sAgent](m2sAgent),
    )
    val myRam = new tilelink.Ram(
      p=m2sCfg.toNodeParameters(),
      bytes=(
        depth * (cfg.mainWidth / 8)
      ),
    )
    myRam.mem.initBigInt(dataInitBigInt)
    //val bridgeCfg = LcvStallToTilelinkConfig(
    //  addrWidth=(
    //    //cfg.mainWidth
    //    log2Up(depth * (cfg.mainWidth / 8))
    //  ),
    //  dataWidth=cfg.mainWidth,
    //  sizeBytes=cfg.mainWidth / 8,
    //  srcWidth=1,
    //  isDual=false,
    //)
    //val bridge = LcvStallToTilelink(
    //  cfg=bridgeCfg,
    //)
    //bridge.io.lcvStall.nextValid := io.dbus.nextValid
    //bridge.io.lcvStall.sendData.addr := io.dbus.sendData.addr.resized
    //bridge.io.lcvStall.sendData.data := io.dbus.sendData.data
    //bridge.io.lcvStall.sendData.src := 0x0
    //bridge.io.lcvStall.sendData.isWrite := (
    //  io.dbus.sendData.accKind.asBits(1)
    //)
    //io.dbus.ready := bridge.io.lcvStall.ready
    //io.dbus.recvData.data := bridge.io.lcvStall.recvData.data
    //myRam.io.up << bridge.io.tlBus

    //dcache.io.bus <> io.dbus
    dcache.io.bus.nextValid := (
      /*RegNext*/(io.dbus.nextValid)
      //init(io.dbus.nextValid.getZero)
    )
    dcache.io.bus.sendData := (
      /*RegNext*/(io.dbus.sendData)
      //init(io.dbus.sendData.getZero)
    )
    io.dbus.recvData := (
      /*RegNext*/(dcache.io.bus.recvData)
      //init(dcache.io.bus.recvData.getZero)
    )
    io.dbus.ready := (
      /*RegNext*/(dcache.io.bus.ready)
      //init(dcache.io.bus.ready.getZero)
    )
    io.dbusExtraReady.addAttribute(KeepAttribute.keep)
    io.dbusExtraReady := dcache.io.busExtraReady

    //myRam.io.up << dcache.io.tlBus
    //myRam.io.up.a << dcache.io.tlBus.a
    myRam.io.up.a.opcode := dcache.io.tlBus.a.opcode
    myRam.io.up.a.param := dcache.io.tlBus.a.param
    myRam.io.up.a.source := dcache.io.tlBus.a.source
    myRam.io.up.a.address := dcache.io.tlBus.a.address.resize(
      myRam.io.up.a.address.getWidth
    )
    myRam.io.up.a.size := dcache.io.tlBus.a.size
    myRam.io.up.a.mask := dcache.io.tlBus.a.mask
    myRam.io.up.a.data := dcache.io.tlBus.a.data
    myRam.io.up.a.corrupt := dcache.io.tlBus.a.corrupt
    myRam.io.up.a.debugId := dcache.io.tlBus.a.debugId

    myRam.io.up.a.valid := dcache.io.tlBus.a.valid
    dcache.io.tlBus.a.ready := myRam.io.up.a.ready

    //myRam.io.up.a.address.allowOverride
    //myRam.io.up.a.address := (
    //  dcache.io.tlBus.a.address.resized
    //)
    dcache.io.tlBus.d << myRam.io.up.d
    //myRam.io.up.a <-/< dcache.io.tlBus.a
    //dcache.io.tlBus.d <-/< myRam.io.up.d
  }

  //val dataRamDepth = dataInitBigInt.size
  //val dataRam = FpgacpuRamSimpleDualPort(
  //  wordType=UInt(cfg.mainWidth bits),
  //  depth=dataRamDepth,
  //  initBigInt=Some(dataInitBigInt),
  //)
  //val rDbusReadyCnt = Reg(UInt(5 bits)) init(0)
  //val rDbusReadyState = Reg(Bool(), init=False)

  //if (fastDbusReady) {
  //  io.dbus.ready := io.dbus.rValid
  //} else {
  //  io.dbus.ready := False
  //  when (io.dbus.rValid) {
  //    when (
  //      //rDbusReadyCnt > 0
  //      !rDbusReadyCnt.msb
  //    ) {
  //      rDbusReadyCnt := rDbusReadyCnt - 1
  //    } otherwise {
  //      io.dbus.ready := True
  //      rDbusReadyState := !rDbusReadyState
  //      when (!rDbusReadyState) {
  //        rDbusReadyCnt := (
  //          //4
  //          2
  //        )
  //      } otherwise {
  //        rDbusReadyCnt := 0
  //      }
  //    }
  //  }
  //}

  ////dataRam.io.rdEn := False
  //dataRam.io.wrEn := False
  //dataRam.io.rdEn := io.dbus.nextValid
  //when (io.dbus.rValid && io.dbus.ready) {
  //}
  //dataRam.io.rdAddr := (
  //  (io.dbus.sendData.addr >> 2).resized
  //)
  //dataRam.io.wrAddr := (
  //  (io.dbus.sendData.addr >> 2).resized
  //)
  //io.dbus.recvData.data := dataRam.io.rdData.asUInt
  //when (io.dbus.sendData.accKind.asBits(1)) {
  //  // TODO: possibly update this to work better?
  //  dataRam.io.wrEn := io.dbus.nextValid
  //  //dataRam.io.wrAddr := (
  //  //  (io.dbus.sendData.addr >> 2).resized
  //  //)
  //}
  //dataRam.io.wrData := io.dbus.sendData.data.asBits
  ////switch (io.dbus.sendData.accKind) {
  ////  is (SnowHouseMemAccessKind.LoadU) {
  ////    //dataRam.io.rdEn := io.dbus.nextValid
  ////    //dataRam.io.rdAddr := (
  ////    //  (io.dbus.sendData.addr >> 2).resized
  ////    //)
  ////    //when (io.dbus.rValid && io.dbus.ready) {
  ////    //  io.dbus.recvData.data := dataRam.io.rdData.asUInt
  ////    //}
  ////  }
  ////  is (SnowHouseMemAccessKind.LoadS) {
  ////    //dataRam.io.rdEn := io.dbus.nextValid
  ////    //dataRam.io.rdAddr := (
  ////    //  (io.dbus.sendData.addr >> 2).resized
  ////    //)
  ////    //when (io.dbus.rValid && io.dbus.ready) {
  ////    //  io.dbus.recvData.data := dataRam.io.rdData.asUInt
  ////    //}
  ////  }
  ////  is (SnowHouseMemAccessKind.Store) {
  ////    // TODO: possibly update this to work better?
  ////    dataRam.io.wrEn := io.dbus.nextValid
  ////    //dataRam.io.wrAddr := (
  ////    //  (io.dbus.sendData.addr >> 2).resized
  ////    //)
  ////    dataRam.io.wrData := io.dbus.sendData.data.asBits
  ////  }
  ////}
  
}
//--------
case class SnowHouseIo(
  cfg: SnowHouseConfig
) extends Bundle {
  val myHaveIrqIdsIra = (
    cfg.myHaveIrqIdsIra
  )
  //val irqValid = (
  //  myHaveIrqIdsIra
  //) generate (
  //  in(Bool())
  //)
  //val idsIraIgnt = (
  //  myHaveIrqIdsIra
  //) generate (
  //  out(Bool())
  //)
  val idsIraIrq = (
    myHaveIrqIdsIra
  ) generate (
    slave(new LcvStallIo[Bool, Bool](
      sendPayloadType=None,
      recvPayloadType=None,
    ))
  )
  val modMemWord = (
    cfg.exposeModMemWordToIo
  ) generate (
    out(UInt(cfg.mainWidth bits))
  )
  // instruction bus
  val ibus = new LcvStallIo[BusHostPayload, BusDevPayload ](
    sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=true)),
    recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=true)),
  )
  val haveMultiCycleBusVec = (
    //cfg.opInfoMap.find(_._2.select == OpSelect.MultiCycle) != None
    cfg.havePsExStall
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
      for (
        ((_, opInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
      ) {
        assert(
          opInfo.select == OpSelect.MultiCycle
        )
        //if (opInfo.select == OpSelect.MultiCycle) {
          tempArr += new LcvStallIo(
            sendPayloadType=(
              Some(MultiCycleHostPayload(cfg=cfg, opInfo=opInfo))
            ),
            recvPayloadType=(
              Some(MultiCycleDevPayload(cfg=cfg, opInfo=opInfo))
            ),
          )
        //}
      }
      tempArr
    }
  )
  val dbus = new LcvStallIo[BusHostPayload, BusDevPayload](
    sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=false)),
    recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=false)),
  )
  val dbusExtraReady = Vec.fill(cfg.lowerMyFanout)(
    Bool()
  )
  //val dcacheHaveHazard = Bool()
  master(
    ibus,
    dbus,
  )
  if (haveMultiCycleBusVec) {
    for (idx <- 0 until multiCycleBusVec.size) {
      master(multiCycleBusVec(idx))
    }
  }
  //out(dcacheHaveHazard)
  in(dbusExtraReady)
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
  //if (io.haveMultiCycleBusVec) {
  //  io.multiCycleBusVec.foreach(multiCycleBus => {
  //    multiCycleBus.sendData.srcVec.foreach(src => {
  //      src := (
  //        RegNext(
  //          next=src,
  //          init=src.getZero,
  //        )
  //      )
  //    })
  //  })
  //}
  //--------
  val psIdHaltIt = Bool()
  val psExSetPc = (
    /*KeepAttribute*/(
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
  val pcChangeState = (
    Bool()
    //UInt(
    //  SnowHouseShouldIgnoreInstrState().asBits.getWidth bits
    //)
  )
  val shouldIgnoreInstr = Bool()
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
    psIdHaltIt=psIdHaltIt,
    psExSetPc=psExSetPc,
    pcChangeState=pcChangeState,
    shouldIgnoreInstr=shouldIgnoreInstr,
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
    pcChangeState=pcChangeState,
    shouldIgnoreInstr=shouldIgnoreInstr,
    doModInModFrontParams=doModInModFrontParams,
  )
  //--------
  //val pipeStageWb = (
  //  //cfg.optFormal
  //  true
  //) generate (
  //  SnowHousePipeStageWriteBack(
  //    args=SnowHousePipeStageArgs(
  //      cfg=cfg,
  //      io=io,
  //      link=null,
  //      prevPayload=null,
  //      currPayload=null,
  //      regFile=regFile,
  //    ),
  //  )
  //)
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
      //psWb=(
      //  //pipeStageWb
      //  null
      //),
      psMemStallHost=psMemStallHost,
    )
  )
  if (cfg.exposeModMemWordToIo) {
    io.modMemWord := (
      regFile.io.back(regFile.io.modBackPayload).myExt(0).modMemWord
    )
  }
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
