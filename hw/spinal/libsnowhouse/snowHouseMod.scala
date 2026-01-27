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
import libcheesevoyage.bus.lcvBus._

//sealed trait SnowHouseInstrSourceKind
//case class SnowHouseInstrRamIo(
//  cfg: SnowHouseConfig
//) extends Component {
//}
case class SnowHouseInstrDataDualRamIo(
  cfg: SnowHouseConfig,
) extends Bundle {
  val ibus = (
    !cfg.useLcvInstrBus
  ) generate (
    new LcvStallIo[BusHostPayload, BusDevPayload](
      sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=true)),
      recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=true)),
    )
  )
  val lcvIbus = (
    cfg.useLcvInstrBus
  ) generate (
    slave(LcvBusIo(
      cfg=cfg.subCfg.lcvIbusEtcCfg.loBusCfg,
    ))
  )
  val dbus = (
    !cfg.useLcvDataBus
  ) generate (
    new LcvStallIo[BusHostPayload, BusDevPayload](
      sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=false)),
      recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=false)),
    )
  )
  val lcvDbus = (
    cfg.useLcvDataBus
  ) generate (
    slave(LcvBusIo(
      cfg=cfg.subCfg.lcvDbusEtcCfg.loBusCfg,
    ))
  )
  //val dcacheHaveHazard = Bool()
  val dbusExtraReady = (
    !cfg.useLcvDataBus
  ) generate (
    Vec.fill(cfg.lowerMyFanout)(
      Bool()
    )
  )
  val dbusLdReady = (
    !cfg.useLcvDataBus
  ) generate (
    Bool()
  )
  if (!cfg.useLcvInstrBus) {
    slave(ibus)
  } else {
  }
  if (!cfg.useLcvDataBus) {
    slave(dbus)
    out(dbusExtraReady)
    out(dbusLdReady)
  } else {
  }
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
    ////false
    ////true
    ////0
    ////5
    ////2
    //1
    cfg.instrRamKind
  )
  // BEGIN: old, non-icache code
  val myNonIcacheArea = (
    //noIcache
    instrRamKind > 0
    && !cfg.useLcvInstrBus
  ) generate (new Area {
    val instrRamDepth = instrInitBigInt.size
    val instrRam = FpgacpuRamSimpleDualPort(
      cfg=FpgacpuRamSimpleDualPortConfig(
        wordType=UInt(cfg.instrMainWidth bits),
        depth=instrRamDepth,
        initBigInt=Some(instrInitBigInt),
      )
    )
    //--------
    val fastIbusReady = false
    val rReadyPipe1 = Reg(Bool(), init=False)
    val rInstrPipe1 = Reg(UInt(cfg.instrMainWidth bits)) init(0x0)

    val rIbusReadyCnt = Reg(UInt(8 bits)) init(0)
    val rIbusReadyState = Reg(Bool(), init=False)

    val nextReady = Bool()
    if (fastIbusReady) {
      nextReady := RegNext(nextReady, init=nextReady.getZero)
    } else {
      if (!cfg.useLcvInstrBus) {
        io.ibus.ready.setAsReg() init(False)
      } else {
        io.lcvIbus.h2dBus.ready.setAsReg() init(False)
        io.lcvIbus.d2hBus.valid.setAsReg() init(False)
      }
    }

    if (fastIbusReady) {
      io.ibus.ready := io.ibus.rValid
    } else {
      if (!cfg.useLcvInstrBus) {
        io.ibus.ready := nextReady
      } else {
        io.lcvIbus.h2dBus.ready := nextReady
        io.lcvIbus.d2hBus.valid := nextReady
      }
      nextReady := False
      when (
        if (!cfg.useLcvInstrBus) (
          io.ibus.nextValid
        ) else (
          io.lcvIbus.h2dBus.valid
        )
      ) {
        when (rIbusReadyCnt > 0) {
          rIbusReadyCnt := rIbusReadyCnt - 1
        } otherwise {
          rIbusReadyState := !rIbusReadyState
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
          nextReady := True
        }
      }
    }
    instrRam.io.rdEn := (
      (
        if (!cfg.useLcvInstrBus) (
          io.ibus.rValid
        ) else (
          RegNext(io.lcvIbus.h2dBus.valid, init=False)
        )
      )
      && nextReady
    )
    instrRam.io.rdAddr := (
      if (!cfg.useLcvInstrBus) (
        (io.ibus.sendData.addr >> 2)
        .resized
      ) else (
        (io.lcvIbus.h2dBus.addr >> 2)
        .resized
      )
    )
    val tempInstr = UInt(instrRam.io.rdData.getWidth bits)
    tempInstr := RegNext(tempInstr, init=tempInstr.getZero)
    when (
      RegNext(
        next=(
          if (!cfg.useLcvInstrBus) (
            io.ibus.rValid
          ) else (
            RegNext(io.lcvIbus.h2dBus.valid, init=False)
          )
        ),
        init=False,
      ) && RegNext(
        next=nextReady,
        init=False,
      )
    ) {
      tempInstr := RegNext(instrRam.io.rdData.asUInt)
    }
    //val myLcvIbusSrcFifo = (
    //  cfg.useLcvInstrBus
    //) generate (
    //  StreamFifo(
    //    dataType=cloneOf(io.lcvIbus.h2dBus.src),
    //    depth=2,
    //    latency=0,
    //    forFMax=true,
    //  )
    //)
    if (!cfg.useLcvInstrBus) {
      io.ibus.recvData.instr := tempInstr
    } else {
      io.lcvIbus.d2hBus.data := tempInstr
      //io.lcvIbus.d2hBus.payload := io.lcvIbus.d2hBus.payload.getZero
      //io.lcvIbus.d2hBus.src.allowOverride

      ////myLcvIbusSrcFifo.io.push.valid := io.lcvIbus.h2dBus.fire
      ////myLcvIbusSrcFifo.io.push.payload := io.lcvIbus.h2dBus.src
      ////myLcvIbusSrcFifo.io.pop.ready := io.lcvIbus.d2hBus.fire

      ////val myHistH2dSrc = (
      ////  History[UInt](
      ////    that=io.lcvIbus.h2dBus.src,
      ////    length=3,
      ////    when=(
      ////      io.lcvIbus.h2dBus.fire
      ////    ),
      ////    init=io.lcvIbus.h2dBus.src.getZero,
      ////  )
      ////)
      def myPopStm = (
        io.lcvIbus.h2dBus
      )
      def myPopStmPayloadSrc = (
        io.lcvIbus.h2dBus.src
      )

      io.lcvIbus.d2hBus.src := (
        //RegNext(io.lcvIbus.h2dBus.src, init=io.lcvIbus.h2dBus.src.getZero)
        //myLcvIbusSrcFifo.io.pop.payload
        //RegNextWhen(
          RegNextWhen(
            RegNextWhen(
              next=myPopStmPayloadSrc,
              cond=myPopStm.fire,
              init=myPopStmPayloadSrc.getZero,
            ),
            cond=myPopStm.fire,
            init=myPopStmPayloadSrc.getZero,
          ),
        //  cond=myD2hPopStm.fire,
        //  init=myD2hPopPayloadSrc.getZero
        //)
      )
      //io.lcvIbus.d2hBus.data.allowOverride
      //io.lcvIbus.d2hBus.data := tempInstr
    }
    instrRam.io.wrEn := False
    instrRam.io.wrAddr := instrRam.io.wrAddr.getZero
    instrRam.io.wrData := instrRam.io.wrData.getZero
  })
  // END: old, non-icache code
  //--------
  val instrRamArea = (
    //!noIcache
    instrRamKind == 0
    && !cfg.useLcvInstrBus
  ) generate (new Area {
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
  })
  //--------
  val myLcvNonIcacheArea = (
    instrRamKind > 0
    && cfg.useLcvInstrBus
  ) generate (new Area {
    val depth = instrInitBigInt.size
    val myMemCfg = LcvBusMemConfig(
      busCfg=cfg.subCfg.lcvIbusEtcCfg.hiBusCfg,
      depth=depth,
      initBigInt=Some(instrInitBigInt),
      arrRamStyleAltera="no_rw_check, M10K",
      arrRamStyleXilinx="block",
    )
    val memSlowNonBurst = (
      instrRamKind >= 5
    ) generate (
      LcvBusMemSlowNonBurst(
        cfg=myMemCfg
      )
    )
    val memFastNonBurst = (
      instrRamKind < 5
    ) generate (
      LcvBusMem(
        cfg=myMemCfg
      )
    )
    val myMemIo = (
      if (instrRamKind >= 5) (
        memSlowNonBurst.io
      ) else (
        memFastNonBurst.io
      )
    )
    myMemIo.bus.h2dBus.valid := io.lcvIbus.h2dBus.valid
    myMemIo.bus.h2dBus.mainNonBurstInfo := (
      io.lcvIbus.h2dBus.mainNonBurstInfo
    )
    myMemIo.bus.h2dBus.mainBurstInfo.burstCnt := 0x0
    myMemIo.bus.h2dBus.mainBurstInfo.burstFirst := False
    myMemIo.bus.h2dBus.mainBurstInfo.burstLast := False
    io.lcvIbus.h2dBus.ready := myMemIo.bus.h2dBus.ready

    io.lcvIbus.d2hBus.valid := myMemIo.bus.d2hBus.valid
    io.lcvIbus.d2hBus.mainNonBurstInfo := (
      myMemIo.bus.d2hBus.mainNonBurstInfo
    )
    myMemIo.bus.d2hBus.ready := io.lcvIbus.d2hBus.ready
  })
  val lcvInstrRamArea = (
    instrRamKind == 0
    && cfg.useLcvInstrBus
  ) generate (new Area {
    val depth = instrInitBigInt.size
    val icache = LcvBusCache(
      cfg=cfg.subCfg.lcvIbusEtcCfg
    )
    val haveFastLcvBusMem = (
      //true
      false
    )
    val myMemCfg = LcvBusMemConfig(
      busCfg=cfg.subCfg.lcvIbusEtcCfg.hiBusCfg,
      depth=depth,
      initBigInt=Some(instrInitBigInt),
      arrRamStyleAltera="no_rw_check, M10K",
      arrRamStyleXilinx="block",
    )
    val memSlowNonBurst = (
      !haveFastLcvBusMem
    ) generate (
      LcvBusMemSlowNonBurst(
        cfg=myMemCfg
      )
    )
    val memFastNonBurst = (
      haveFastLcvBusMem
    ) generate (
      LcvBusMem(
        cfg=myMemCfg
      )
    )
    val myMemIo = (
      if (!haveFastLcvBusMem) (
        memSlowNonBurst.io
      ) else (
        memFastNonBurst.io
      )
    )
    //io.lcvIbus <> icache.io.loBus
    io.lcvIbus.h2dBus >/-> icache.io.loBus.h2dBus
    io.lcvIbus.d2hBus <-/< icache.io.loBus.d2hBus
    myMemIo.bus <> icache.io.hiBus
  })
  val lcvDataRamArea = (
    cfg.useLcvDataBus
  ) generate (new Area {
    val haveDcache = (
      //true
      false
    )
    val haveFastLcvBusMem = (
      true
      //false
    )
    val depth = dataInitBigInt.size
    val dcache = (
      haveDcache
    ) generate (
      LcvBusCache(cfg=cfg.subCfg.lcvDbusEtcCfg)
    )
    val myMemCfg = LcvBusMemConfig(
      busCfg=cfg.subCfg.lcvDbusEtcCfg.hiBusCfg,
      depth=depth,
      initBigInt=Some(dataInitBigInt),
      arrRamStyleAltera="no_rw_check, M10K",
      arrRamStyleXilinx="block",
    )
    val memSlowNonBurst = (
      !haveFastLcvBusMem
    ) generate (LcvBusMemSlowNonBurst(
      cfg=myMemCfg
    ))
    val memFastNonBurst = (
      haveFastLcvBusMem
    ) generate (
      LcvBusMem(
        cfg=myMemCfg
      )
    )
    val myMemIo = (
      if (!haveFastLcvBusMem) (
        memSlowNonBurst.io
      ) else (
        memFastNonBurst.io
      )
    )
    if (!haveDcache) {
      io.lcvDbus.h2dBus.translateInto(myMemIo.bus.h2dBus)(
        dataAssignment=(
          thatPayload, selfPayload
        ) => {
          thatPayload.mainNonBurstInfo := selfPayload.mainNonBurstInfo
          thatPayload.mainBurstInfo := thatPayload.mainBurstInfo.getZero
        }
      )
      myMemIo.bus.d2hBus.translateInto(io.lcvDbus.d2hBus)(
        dataAssignment=(
          thatPayload, selfPayload
        ) => {
          thatPayload.mainNonBurstInfo := selfPayload.mainNonBurstInfo
        }
      )
    } else { // if (haveDcache)
      //io.lcvDbus <> dcache.io.loBus
      dcache.io.loBus.h2dBus << io.lcvDbus.h2dBus 
      io.lcvDbus.d2hBus <-/< dcache.io.loBus.d2hBus

      myMemIo.bus <> dcache.io.hiBus
    }
  })

  val dataRamArea = (
    !cfg.useLcvDataBus
  ) generate (new Area {
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
    myRam.mem.addAttribute("ram_style", "block")

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
    io.dbusLdReady := dcache.io.busLdReady

    //myRam.io.up << dcache.io.tlBus
    //myRam.io.up.a << dcache.io.tlBus.a
    myRam.io.up.a <-< dcache.io.tlBus.a
    //myRam.io.up.a.opcode := dcache.io.tlBus.a.opcode
    //myRam.io.up.a.param := dcache.io.tlBus.a.param
    //myRam.io.up.a.source := dcache.io.tlBus.a.source
    //myRam.io.up.a.address := dcache.io.tlBus.a.address.resize(
    //  myRam.io.up.a.address.getWidth
    //)
    //myRam.io.up.a.size := dcache.io.tlBus.a.size
    //myRam.io.up.a.mask := dcache.io.tlBus.a.mask
    //myRam.io.up.a.data := dcache.io.tlBus.a.data
    //myRam.io.up.a.corrupt := dcache.io.tlBus.a.corrupt
    //myRam.io.up.a.debugId := dcache.io.tlBus.a.debugId

    //myRam.io.up.a.valid := dcache.io.tlBus.a.valid
    //dcache.io.tlBus.a.ready := myRam.io.up.a.ready

    //myRam.io.up.a.address.allowOverride
    //myRam.io.up.a.address := (
    //  dcache.io.tlBus.a.address.resized
    //)
    dcache.io.tlBus.d <-< myRam.io.up.d
    //myRam.io.up.a <-/< dcache.io.tlBus.a
    //dcache.io.tlBus.d <-/< myRam.io.up.d
  })

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
private[libsnowhouse] case class SnowHouseDbus(
  cfg: SnowHouseConfig,
  inSnowHouseIo: Boolean,
) extends Bundle {
  val nextValid = Bool()
  val ready = Bool()
  val sendData = BusHostPayload(cfg=cfg, isIbus=false)
  val recvData = BusDevPayload(cfg=cfg, isIbus=false)

  if (inSnowHouseIo) {
    out(nextValid)
    in(ready)
    out(sendData)
    in(recvData)
  }
  def >>(
    that: LcvStallIo[BusHostPayload, BusDevPayload]
  ): Unit = {
    //require(inSnowHouseIo)
    that.nextValid := this.nextValid
    this.ready := that.ready
    //that.sendData := this.sendData
    //this.recvData := that.recvData
    that.sendData.nonSrc := this.sendData.nonSrc
    if (
      that.sendData.src != null
      && this.sendData.src != null
    ) {
      that.sendData.src := this.sendData.src
    }
    this.recvData.word := that.recvData.word
    if (
      that.recvData.src != null
      && this.recvData.src != null
    ) {
      that.recvData.src := this.recvData.src
    }
  }
  def <<(
    that: LcvStallIo[BusHostPayload, BusDevPayload],
  ): Unit = {
    //require(inSnowHouseIo)
    this.nextValid := that.nextValid
    that.ready := this.ready
    //this.sendData := that.sendData
    //that.recvData := this.recvData
    this.sendData.nonSrc := that.sendData.nonSrc
    if (
      this.sendData.src != null
      && that.sendData.src != null
    ) {
      this.sendData.src := that.sendData.src
    }
    that.recvData.word := this.recvData.word
    if (
      this.recvData.src != null
      && that.recvData.src != null
    ) {
      this.recvData.src := that.recvData.src
    }
  }
}

private[libsnowhouse] case class SnowHouseDbusIo(
  cfg: SnowHouseConfig,
  inSnowHouseIo: Boolean=false,
) extends Bundle {
  //val dbus = (
  //  new LcvStallIo[BusHostPayload, BusDevPayload](
  //    sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=false)),
  //    recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=false)),
  //  )
  //)
  val dbus = SnowHouseDbus(
    cfg=cfg,
    inSnowHouseIo=(
      !cfg.useLcvDataBus
      && inSnowHouseIo
    ),
  )

  val dbusExtraReady = (
    Vec.fill(cfg.lowerMyFanout)(
      Bool()
    )
  )
  val dbusLdReady = Bool()
  val myUpFireIshCond = (
    cfg.useLcvDataBus
  ) generate (
    Bool()
  )
  val myUpFireIshUpdateSrcCond = (
    cfg.useLcvDataBus
  ) generate (
    Bool()
  )
  val myDbusExtraValid = (
    cfg.useLcvDataBus
  ) generate (
    Bool()
  )
  if (
    !cfg.useLcvDataBus
    && inSnowHouseIo
  ) {
    //master(dbus)
    //out(dcacheHaveHazard)

    in(dbusExtraReady)
    in(dbusLdReady)
  }
}
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
  val regFileWriteData = (
    cfg.exposeRegFileWriteDataToIo
  ) generate (
    out(UInt(cfg.mainWidth bits))
  )
  val regFileWriteAddr = (
    cfg.exposeRegFileWriteAddrToIo
  ) generate (
    out(UInt(log2Up(cfg.regFileCfg.wordCountArr(0)) bits))
  )
  val regFileWriteEnable = (
    cfg.exposeRegFileWriteEnableToIo
  ) generate (
    out(Bool())
  )
  // instruction bus
  val ibus = (
    !cfg.useLcvInstrBus
  ) generate (
    new LcvStallIo[BusHostPayload, BusDevPayload](
      sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=true)),
      recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=true)),
    )
  )
  val lcvIbus = (
    cfg.useLcvInstrBus
  ) generate (
    master(LcvBusIo(
      cfg=cfg.subCfg.lcvIbusEtcCfg.loBusCfg,
    ))
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
  val myDbusIo = (
    !cfg.useLcvDataBus
  ) generate (
    SnowHouseDbusIo(
      cfg=cfg,
      inSnowHouseIo=true,
    )
  )
  def dbus = myDbusIo.dbus
  def dbusExtraReady = myDbusIo.dbusExtraReady
  def dbusLdReady = myDbusIo.dbusLdReady
  val lcvDbus = (
    cfg.useLcvDataBus
  ) generate (
    master(LcvBusIo(
      cfg=cfg.subCfg.lcvDbusEtcCfg.loBusCfg,
    ))
  )
  //val dcacheHaveHazard = Bool()
  if (!cfg.useLcvInstrBus) {
    master(ibus)
  }
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
      /*Stream*/Flow(SnowHousePsExSetPcPayload(cfg=cfg))
    )
    .setName(s"SnowHouse_psExSetPc")
  )
  //val myDbus = (
  //  if (!cfg.useLcvDataBus) (
  //    io.dbus
  //  ) else (
  //    new LcvStallIo[BusHostPayload, BusDevPayload](
  //      sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=false)),
  //      recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=false)),
  //    )
  //  )
  //)
  //val psMemStallHost = (
  //  cfg.mkLcvStallHost(
  //    stallIo=Some(
  //      //myDbus
  //      if (!cfg.useLcvDataBus) (
  //        io.dbus
  //      ) else (
  //        new LcvStallIo[BusHostPayload, BusDevPayload](
  //          sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=false)),
  //          recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=false)),
  //        )
  //      )
  //    ),
  //  )
  //)
  //val myDbusExtraReady = (
  //  if (!cfg.useLcvDataBus) (
  //    io.dbusExtraReady
  //  ) else (
  //    Vec.fill(cfg.lowerMyFanout)(
  //      Bool()
  //    )
  //  )
  //)
  //val myDbusLdReady = (
  //  if (!cfg.useLcvDataBus) (
  //    io.dbusLdReady
  //  ) else (
  //    Bool()
  //  )
  //)
  val myDbusIo = SnowHouseDbusIo(cfg=cfg)
  if (!cfg.useLcvDataBus) {
    //io.myDbusIo <> myDbusIo
    //io.myDbusIo.nextValid := myDbus
    io.myDbusIo <> myDbusIo
  }
  val myLcvDbusArea = (
    cfg.useLcvDataBus
  ) generate (new Area {
    //val myBridge = SnowHouseDbusToLcvDbusBridge(cfg=cfg)
    //val myBridgeCtrl = SnowHouseBusBridgeCtrl(
    //  cfg=cfg,
    //  isIbus=false,
    //)
    //io.lcvDbus <> myBridge.io.lcvBus
    //myBridgeCtrl.io.bridgeBus <> myBridge.io.bus
    //myBridgeCtrl.io.bridgeH2dPushDelay := myBridge.io.h2dPushDelay
    //myBridgeCtrl.io.myUpFireIshCond := myDbusIo.myUpFireIshCond
    //myBridgeCtrl.io.myUpFireIshUpdateSrcCond := (
    //  myDbusIo.myUpFireIshUpdateSrcCond
    //)
    //myBridgeCtrl.io.cpuDbusExtraValid := myDbusIo.myDbusExtraValid
    //myDbusIo.dbus >> myBridgeCtrl.io.cpuBus
    ////myBridgeCtrl.io.cpuBus := myDbusIo.dbus.nextValid
  })

  val pcChangeState = (
    Bool()
    //UInt(
    //  SnowHouseShouldIgnoreInstrState().asBits.getWidth bits
    //)
  )
  val shouldIgnoreInstr = Bool()
  //--------
  val myModMemWord = SInt(cfg.mainWidth bits)
  //val psIdFoundBubble = Bool()
  //val psIdPostFoundBubble = Bool()
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
    doModInMid0FrontFunc=Some(
      //(
      //  doModInMid0FrontParams,
      //  myRegFile1,
      //) => 
      mkPipeStageExecute
      //(
      //  doModInMid0FrontParams=doModInMid0FrontParams,
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
  def myHaveS2mIfId = (
    cfg.useLcvInstrBus
    && cfg.useLcvDataBus
  )
  val s2mIf = (
    myHaveS2mIfId
  ) generate (S2MLink(
    up={
      sIf.down
    },
    down={
      val node = Node()
      node.setName("s2mIf_down")
      node
    }
  ))
  if (myHaveS2mIfId) {
    linkArr += s2mIf
  }
  val pipeStageIf = SnowHousePipeStageInstrFetch(
    args=SnowHousePipeStageArgs(
      cfg=cfg,
      io=io,
      link=cIf,
      prevPayload=null,
      currPayload=pIf,
      myDbusIo=(
        if (!cfg.useLcvDataBus) (
          myDbusIo
        ) else (
          null.asInstanceOf[SnowHouseDbusIo]
        )
      ),
      regFile=regFile,
    ),
    psIdHaltIt=psIdHaltIt,
    psExSetPc=psExSetPc,
  )

  //val cIfPostLcvIbus = (
  //  cfg.useLcvInstrBus
  //) generate (CtrlLink(
  //  up=sIf.down,
  //  down={
  //    val node = Node()
  //    node.setName("cIfPostLcvIbus_down")
  //    node
  //  }
  //))
  //if (cfg.useLcvInstrBus) {
  //  linkArr += cIfPostLcvIbus
  //}
  //val sIfPostLcvIbus = (
  //  cfg.useLcvInstrBus
  //) generate (StageLink(
  //  up=cIfPostLcvIbus.down,
  //  down={
  //    val node = Node()
  //    node.setName("sIfPostLcvIbus_down")
  //    node
  //  }
  //))
  //if (cfg.useLcvInstrBus) {
  //  linkArr += sIfPostLcvIbus
  //}

  val cId = CtrlLink(
    up={
      if (
        //!cfg.useLcvInstrBus
        !myHaveS2mIfId
      ) (
        sIf.down
      ) else ( // if (myHaveS2mIfId)
        s2mIf.down
        //sIfPostLcvIbus.down
      )
      //s2mIf.down
    },
    down={
      if (!myHaveS2mIfId) (
        regFile.io.front
      ) else {
        val node = Node()
        node.setName("cId_down")
        node
      }
    }
  )
  linkArr += cId
  val sId = (
    myHaveS2mIfId
  ) generate (StageLink(
    up=cId.down,
    down={
      val node = Node()
      node.setName("sId_down")
      node
    }
  ))
  if (myHaveS2mIfId) {
    linkArr += sId
  }
  val s2mId = (
    myHaveS2mIfId
  ) generate (StageLink(
    up=sId.down,
    down=regFile.io.front,
  ))
  if (myHaveS2mIfId) {
    linkArr += s2mId
  }
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
      myDbusIo=(
        if (!cfg.useLcvDataBus) (
          myDbusIo
        ) else (
          null.asInstanceOf[SnowHouseDbusIo]
        )
      ),
      regFile=regFile,
    ),
    psIdHaltIt=psIdHaltIt,
    psExSetPc=psExSetPc,
    pcChangeState=pcChangeState,
    shouldIgnoreInstr=shouldIgnoreInstr,
    doDecodeFunc=cfg.doInstrDecodeFunc,
    //psIdFoundBubble=psIdFoundBubble,
  )
  //--------
  //val pEx = Payload(SnowHouseRegFileModType(cfg=cfg))
  def mkPipeStageExecute(
    doModInMid0FrontParams: PipeMemRmwDoModInMid0FrontFuncParams[
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
      myDbusIo=(
        if (!cfg.useLcvDataBus) (
          myDbusIo
        ) else (
          null.asInstanceOf[SnowHouseDbusIo]
        )
      ),
      regFile=null,
    ),
    psExSetPc=psExSetPc,
    //psMemStallHost=psMemStallHost,
    doModInMid0FrontParams=doModInMid0FrontParams,
    //myDbusExtraReady=myDbusExtraReady,
    //myDbusLdReady=myDbusLdReady,
    pcChangeState=pcChangeState,
    shouldIgnoreInstr=shouldIgnoreInstr,
    myModMemWord=myModMemWord,
    //prevStageFoundBubble=(
    //  if (!cfg.optTwoCycleRegFileReads) (
    //    psIdFoundBubble
    //  ) else (
    //    psIdPostFoundBubble
    //  )
    //)
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
  val cMem = (
    CtrlLink(
      up=regFile.io.modFront,
      down={
        val temp = Node()
        temp.setName(s"cMem_down")
        temp
      },
    )
  )
  linkArr += cMem
  //val pMem = regFile.io.modFrontAfterPayload
  val pMem = (
    if (!cfg.useLcvDataBus) (
      regFile.io.modBackPayload
    ) else (
      Payload(SnowHousePipePayload(cfg=cfg))
    )
  )
  val pipeStageMem = (
    SnowHousePipeStageMem(
      args=SnowHousePipeStageArgs(
        cfg=cfg,
        io=io,
        link=cMem,
        prevPayload=(
          //null
          regFile.io.modFrontAfterPayload
        ),
        currPayload=(
          pMem
        ),
        myDbusIo=(
          if (!cfg.useLcvDataBus) (
            myDbusIo
          ) else (
            null.asInstanceOf[SnowHouseDbusIo]
          )
        ),
        regFile=regFile,
      ),
      //psWb=(
      //  //pipeStageWb
      //  null
      //),
      //psMemStallHost=psMemStallHost,
      //myDbusExtraReady=myDbusExtraReady,
      //myDbusLdReady=myDbusLdReady,
      myModMemWord=myModMemWord,
    )
  )
  val cWb = (
    cfg.useLcvDataBus
  ) generate (
    CtrlLink(
      up=pipeStageMem.sMem.down,
      down={
        val temp = Node()
        temp.setName(s"cWb_down")
        temp
      }
    )
  )
  if (cfg.useLcvDataBus) {
    linkArr += cWb
  }
  val pipeStageWb = (
    cfg.useLcvDataBus
  ) generate (
    SnowHousePipeStageWriteBack(
      args=SnowHousePipeStageArgs(
        cfg=cfg,
        io=io,
        link=cWb,
        prevPayload=pMem,
        currPayload=regFile.io.modBackPayload,
        myDbusIo=(
          if (!cfg.useLcvDataBus) (
            myDbusIo
          ) else (
            null.asInstanceOf[SnowHouseDbusIo]
          )
        ),
        regFile=regFile,
      ),
      //psMemStallHost=psMemStallHost,
      //myDbusExtraReady=myDbusExtraReady,
      //myDbusLdReady=myDbusLdReady,
      //myModMemWord=myModMemWord,
    )
  )
  if (cfg.exposeRegFileWriteDataToIo) {
    if (
      !cfg.exposeRegFileWriteAddrToIo
      && !cfg.exposeRegFileWriteEnableToIo
    ) {
      io.regFileWriteData := (
        regFile.io.back(regFile.io.modBackPayload).myExt(0).modMemWord
      )
    } else {
      io.regFileWriteData := (
        regFile.mod.back.myWriteData(1)(0)(0)
      )
    }
  }
  if (cfg.exposeRegFileWriteAddrToIo) {
    io.regFileWriteAddr := (
      regFile.mod.back.myWriteAddr(1)(0)(0)
    )
  }
  if (cfg.exposeRegFileWriteEnableToIo) {
    io.regFileWriteEnable := (
      regFile.mod.back.myWriteEnable(0)
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
