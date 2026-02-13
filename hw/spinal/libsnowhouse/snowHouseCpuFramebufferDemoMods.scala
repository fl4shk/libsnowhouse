package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.sim._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._

import spinal.lib.graphic.vga._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig

import libcheesevoyage.general._
import libcheesevoyage.gfx._
import libcheesevoyage.math._
import libcheesevoyage.bus.lcvBus._
import libcheesevoyage.hwdev.SnesCtrlIo
import libcheesevoyage.hwdev.SnesButtons

case class SnowHouseCpuFramebufferDemoConfig(
  program: SnowHouseCpuProgram,
  clkRate: HertzNumber,
  rgbCfg: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  fbCnt2dShift: ElabVec2[Int],
  fbAddrSliceHi: Int=24,
  fbAddrSliceLo: Int=24,
) {
  def cpuCfg = program.cfg
  val myDbusCfg = cpuCfg.shCfg.subCfg.lcvDbusEtcCfg.loBusCfg
  //def myDbusCfg = //cpu.io.lcvDbus.cfg

  val myFbOptAddrSliceVal = Some(1)
  val myFbDbusSlicerMmapCfg = LcvBusMemMapConfig(
    busCfg=(
      myDbusCfg
      //LcvBusConfig(
      //  mainCfg=myDbusCfg.mainCfg.mkCopyWithAllowingBurst(),
      //  cacheCfg=myDbusCfg.cacheCfg,
      //)
    ),
    addrSliceHi=fbAddrSliceHi,//24,
    addrSliceLo=fbAddrSliceLo,//24,
    optAddrSliceVal=(
      // the framebuffer has bit 24 of the address asserted!
      //Some(1)
      myFbOptAddrSliceVal
    )
  )
  val myFbCtrlMmapCfg = LcvBusMemMapConfig(
    busCfg=(
      //myDbusCfg
      LcvBusConfig(
        mainCfg=myDbusCfg.mainCfg.mkCopyWithAllowingBurst(),
        cacheCfg=myDbusCfg.cacheCfg,
      )
    ),
    addrSliceHi=fbAddrSliceHi,//24,
    addrSliceLo=fbAddrSliceLo,//24,
    optAddrSliceVal=(
      // the framebuffer has bit 24 of the address asserted!
      //Some(1)
      myFbOptAddrSliceVal
    )
  )
  val myFbCfg = LcvBusFramebufferConfig(
    fbMmapCfg=myFbCtrlMmapCfg,
    rgbCfg=rgbCfg,
    //vgaTimingInfo=(
    //  //LcvVgaTimingInfoMap.map("320x240@60")
    //  vgaTimingInfo
    //),
    fbSize2d=(
      //vgaTimingInfo.fbSize2d
      ElabVec2[Int](
        x=(
          vgaTimingInfo.fbSize2d.x
          //320
          //>> (if (fbCnt2dShift.x) (1) else (0))
          >> fbCnt2dShift.x
        ),
        y=(
          vgaTimingInfo.fbSize2d.y
          //240
          //>> (if (fbCnt2dShift.y) (1) else (0))
          >> fbCnt2dShift.y
        ),
      )
    ),
    cnt2dShift=fbCnt2dShift,
    dblBuf=(
      //true
      false
    ),
  )
}

case class SnowHouseCpuFramebufferDemoIo(
  cfg: SnowHouseCpuFramebufferDemoConfig,
) extends Bundle {
  // framebuffer bus
  //val fbBus = master(LcvBusIo(
  //  cfg=(
  //    //cfg.myDbusCfg
  //    cfg.myFbMmapCfg.busCfg
  //  )
  //))
  val rawSnesButtons = slave(
    Stream(UInt(SnesButtons.rawButtonsWidth bits))
  )
  val phys = out(LcvVgaPhys(rgbConfig=cfg.rgbCfg))
  val misc = out(LcvVgaCtrlMiscIo(
    clkRate=cfg.clkRate,
    vgaTimingInfo=cfg.vgaTimingInfo,
    fifoDepth=(
      //1 << log2Up(cfg.vgaTimingInfo.htiming.visib)
      //16
      32
      //128
    ),
    optIncludeMiscVgaStates=true,
  ))
}
case class SnowHouseCpuFramebufferDemo(
  cfg: SnowHouseCpuFramebufferDemoConfig
) extends Component {
  //--------
  def cpuCfg = cfg.cpuCfg

  val io = SnowHouseCpuFramebufferDemoIo(cfg=cfg)
  io.rawSnesButtons.ready := True//False
  //--------
  val cpu = SnowHouseCpuWithoutRam(program=cfg.program)
  //cpu.io.idsIraIrq.nextValid := False
  //--------
  val vgaClockDomain = ClockDomain.external(
    name="vgaClk",
    withReset=true,//false,
    frequency=FixedFrequency(
      //25.0 MHz
      cfg.vgaTimingInfo.pixelClk
    ),
  )
  val pixelFifo = StreamFifoCC(
    dataType=Rgb(cfg.rgbCfg),
    depth=io.misc.fifoDepth,
    pushClock=ClockDomain.current,
    popClock=vgaClockDomain,
  )

  //val lcvVgaCtrl = (
  //  LcvVgaCtrl(
  //    clkRate=cfg.clkRate,
  //    //rgbConfig=physRgbConfig,
  //    rgbConfig=cfg.rgbCfg,
  //    vgaTimingInfo=cfg.vgaTimingInfo,
  //    fifoDepth=(
  //      //cfg.ctrlFifoDepth
  //      io.misc.fifoDepth
  //    ),
  //  )
  //)
  //io.phys.setAsReg() init(io.phys.getZero)
  //io.misc.setAsReg() init(io.misc.getZero)
  //io.phys := lcvVgaCtrl.io.phys
  //io.misc := lcvVgaCtrl.io.misc
  //lcvVgaCtrl.io.en := True
  //when (
  //  //!io.misc.visib
  //  !lcvVgaCtrl.io.misc.visib
  //) {
  //  io.phys.col := io.phys.col.getZero
  //}

  object MyIrqState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      VBLANK
      = newElement();
  }
  val rMyIrqState = (
    Reg(MyIrqState())
    init(MyIrqState.IDLE)
  )
  //cpu.io.idsIraIrq.nextValid
  val rIrqValid = Reg(Bool(), init=False)
  cpu.io.idsIraIrq.nextValid := (
    rIrqValid
    //RegNext(
    //  cpu.io.idsIraIrq.nextValid,
    //  init=cpu.io.idsIraIrq.nextValid.getZero
    //)
  )

  //switch (rMyIrqState) {
  //  is (MyIrqState.IDLE) {
  //    when (
  //      rose(
  //        RegNext(
  //          lcvVgaCtrl.io.misc.vpipeS =/= LcvVgaState.visib,
  //          init=False
  //        )
  //      )
  //    ) {
  //      rMyIrqState := MyIrqState.VBLANK
  //      rIrqValid := True
  //    }
  //  }
  //  is (MyIrqState.VBLANK) {
  //    //cpu.io.idsIraIrq 
  //    when (
  //      //RegNext(
  //      //  cpu.io.idsIraIrq.nextValid
  //      //  init=False
  //      //)
  //      rIrqValid
  //      && cpu.io.idsIraIrq.ready
  //    ) {
  //      rIrqValid := False
  //      rMyIrqState := MyIrqState.IDLE
  //    }
  //  }
  //}

  val vgaClockingArea = new ClockingArea(vgaClockDomain) {
    val vgaCtrl = VgaCtrl(rgbConfig=cfg.rgbCfg)

    val vgaTimingInfo = cfg.vgaTimingInfo
    if (vgaTimingInfo == LcvVgaTimingInfoMap.map("640x480@60")) {
      vgaCtrl.io.timings.setAs_h640_v480_r60
    } else if (vgaTimingInfo == LcvVgaTimingInfoMap.map("1920x1080@60")) {
      vgaCtrl.io.timings.setAs_h1920_v1080_r60
    } else {
      // TODO: check if this works?
      vgaTimingInfo.driveSpinalVgaTimings(
        clkRate=cfg.clkRate,
        spinalVgaTimings=vgaCtrl.io.timings,
      )
    }

    //val lcvVgaCtrl = (
    //  LcvVgaCtrl(
    //    clkRate=cfg.clkRate,
    //    //rgbConfig=physRgbConfig,
    //    rgbConfig=cfg.rgbCfg,
    //    vgaTimingInfo=cfg.vgaTimingInfo,
    //    fifoDepth=(
    //      //cfg.ctrlFifoDepth
    //      io.misc.fifoDepth
    //    ),
    //  )
    //)
    //io.phys := lcvVgaCtrl.io.phys
    //io.misc := lcvVgaCtrl.io.misc
    //lcvVgaCtrl.io.en := True

    //--------
    when (vgaCtrl.io.vga.colorEn) {
      io.phys.col := vgaCtrl.io.vga.color
    } otherwise {
      io.phys.col := io.phys.col.getZero
    }
    io.phys.hsync := vgaCtrl.io.vga.hSync
    io.phys.vsync := vgaCtrl.io.vga.vSync
    io.misc := io.misc.getZero
    io.misc.allowOverride
    io.misc.pastVisib := RegNext(io.misc.visib) init(False)
    io.misc.visib := vgaCtrl.io.vga.colorEn
    //io.misc.pixelEn := (
    //  True
    //)
    vgaCtrl.io.softReset := RegNext(False) init(True)
    //vgaCtrl.io.pixels <-/< myFbCtrl.io.pop
    vgaCtrl.io.pixels <-/< pixelFifo.io.pop
  }
  def cpp = (cfg.clkRate / cfg.vgaTimingInfo.pixelClk).toInt
  println(
    s"here we go: cpp:${cpp}"
  )
  val rPixelEnCnt = Reg(UInt(
    log2Up(cpp) bits
  ))
  io.misc.pixelEn := (
    rPixelEnCnt === cpp - 1
  )
  when (rPixelEnCnt < cpp - 1) {
    rPixelEnCnt := rPixelEnCnt + 1
  } otherwise {
    rPixelEnCnt := 0x0
  }
  //--------
  //val myDbgFbCfg = LcvBusFramebufferConfig(
  //  fbMmapCfg=cfg.myFbMmapCfg,
  //  rgbCfg=cfg.rgbCfg,
  //  //vgaTimingInfo=(
  //  //  //LcvVgaTimingInfoMap.map("320x240@60")
  //  //  vgaTimingInfo
  //  //),
  //  fbSize2d=(
  //    //vgaTimingInfo.fbSize2d
  //    ElabVec2[Int](
  //      x=(
  //        //32
  //        //8
  //        //16
  //        24
  //      ),
  //      y=(
  //        //24
  //        //4
  //        8
  //      ),
  //    )
  //  ),
  //  cnt2dShift=cfg.fbCnt2dShift,
  //  dblBuf=true,
  //)

  // bus stuff goes here

  val myFbCtrl = LcvBusFramebufferCtrl(
    cfg=(
      cfg.myFbCfg
      //myDbgFbCfg
    )
  )
  pixelFifo.io.push <-/< myFbCtrl.io.pop
  //lcvVgaCtrl.io.push <-/< myFbCtrl.io.pop

  val myFbMemDepth = (
    (if (cfg.myFbCfg.dblBuf) (2) else (1))
    * cfg.myFbCfg.fbSize2d.y * cfg.myFbCfg.fbSize2d.x
  )
  def rgbUpWidth = 1 << log2Up(Rgb(c=cfg.rgbCfg).asBits.getWidth)
  val myFbMem = LcvBusMem(
    cfg=LcvBusMemConfig(
      busCfg=(
        cfg.myFbCfg.busCfg
        //myDbgFbCfg.busCfg
      ),
      depth=myFbMemDepth,
      initBigInt={
        val myArr = new ArrayBuffer[BigInt]()
        for (idx <- 0 until myFbMemDepth) {
          //var toAdd = BigInt(idx)
          val toAdd = BigInt(0x0)
          myArr += toAdd
          //myArr += BigInt(idx)
        }
        Some(myArr)
      }
    )
  )
  //myFbMem.io.bus << myFbCtrl.io.bus

  //myFbCtrl.io.pop.ready := True
  val myMainMemInitBigInt = {
    val depth = 1 << (16 - 4)
    val tempArr = new ArrayBuffer[BigInt]()
    tempArr ++= cfg.program.outpArr.view
    //while (tempArr.size < depth) {
    //  tempArr += BigInt(0)
    //}
    val programSize = tempArr.size
    for (idx <- programSize until (1 << (16 - 4))) {
      if (idx < /*1024*/0x800) {
        //println(
        //  s"idx < 0x800: ${idx}"
        //)
        tempArr += BigInt(idx)
      } else {
        //println(
        //  s"idx < 0x800: ${idx}"
        //)
        tempArr += BigInt(0)
      }
    }
    tempArr
    //for (elem <- program.outpArr.view) {
    //  tempArr +=
    //}
    //program.outpArr
  }
  val myMainMemCfg = LcvBusMemConfig(
    busCfg=(
      //if (!haveFastLcvBusMem) (
        LcvBusConfig(
          mainCfg=(
            cpuCfg.shCfg.subCfg.lcvDbusEtcCfg.hiBusCfg.mainCfg
            //.mkCopyWithAllowingBurst()
            //.mkCopyWithoutByteEn(None)
            .mkCopyWithByteEn(None)
          ),
          cacheCfg=cpuCfg.shCfg.subCfg.lcvDbusEtcCfg.hiBusCfg.cacheCfg
        )
      //) else (
      //  cfg.subCfg.lcvDbusEtcCfg.loBusCfg
      //)
    ),
    depth=myMainMemInitBigInt.size,
    initBigInt=Some(myMainMemInitBigInt),
    arrRamStyleAltera="no_rw_check, M10K",
    arrRamStyleXilinx="block",
  )

  val myMainMem = LcvBusMem(cfg=myMainMemCfg)

  val myFbDbusSlicer = LcvBusSlicer(
    cfg=LcvBusSlicerConfig(
      mmapCfg=cfg.myFbDbusSlicerMmapCfg,
    )
  )

  val myMainMemArbiter = LcvBusArbiter(
    cfg=LcvBusArbiterConfig(
      busCfg=myMainMemCfg.busCfg,
      numHosts=2,
      kind=LcvBusArbiterKind.RoundRobin
    ),
  )
  myMainMem.io.bus <-/< myMainMemArbiter.io.dev

  val myFbArbiter = LcvBusArbiter(
    cfg=LcvBusArbiterConfig(
      busCfg=(
        //cfg.myDbusCfg
        cfg.myFbCtrlMmapCfg.busCfg
      ),
      numHosts=2,
      kind=LcvBusArbiterKind.Priority
    )
  )

  cpu.io.lcvDbus.h2dBus.translateInto(myFbDbusSlicer.io.host.h2dBus)(
    dataAssignment=(outp, inp) => {
      outp.mainNonBurstInfo := inp.mainNonBurstInfo
      //outp.mainBurstInfo := outp.mainBurstInfo.getZero
    }
  )
  myFbDbusSlicer.io.host.d2hBus.translateInto(cpu.io.lcvDbus.d2hBus)(
    dataAssignment=(outp, inp) => {
      outp.mainNonBurstInfo := inp.mainNonBurstInfo
    }
  )
  //myFbDbusSlicer.io.host <-/< (
  //  //myFbCtrl.io.bus
  //  cpu.io.lcvDbus
  //)
  //myMainMemArbiter.io.hostVec(0) << myFbDbusSlicer.io.devVec(0)

  val dcache = LcvBusCache(
    cfg=cpuCfg.shCfg.subCfg.lcvDbusEtcCfg
  )
  val icache = LcvBusCache(
    cfg=cpuCfg.shCfg.subCfg.lcvIbusEtcCfg
  )

  myFbDbusSlicer.io.devVec(0).h2dBus.translateInto(
    dcache.io.loBus.h2dBus
  )(
    dataAssignment=(outp, inp) => {
      outp.mainNonBurstInfo := inp.mainNonBurstInfo
      //outp.mainBurstInfo := outp.mainBurstInfo.getZero
    }
  )
  dcache.io.loBus.d2hBus.translateInto(
    myFbDbusSlicer.io.devVec(0).d2hBus
  )(
    dataAssignment=(outp, inp) => {
      outp.mainNonBurstInfo := inp.mainNonBurstInfo
    }
  )
  //dcache.io.loBus << myFbDbusSlicer.io.devVec(0)
  val myTempDcacheHiBus = cloneOf(dcache.io.hiBus)
  myTempDcacheHiBus <-/< dcache.io.hiBus
  //myTempDcacheHiBus.h2dBus.translateInto(
  //  myMainMemArbiter.io.hostVec(0).h2dBus
  //)(
  //  dataAssignment=(outp, inp) => {
  //  }
  //)
  myMainMemArbiter.io.hostVec(0) << myTempDcacheHiBus

  //myMainMemArbiter.io.hostVec(0) <-/< dcache.io.hiBus

  //myMainMemArbiter.io.hostVec(1) <-/< cpu.io.lcvIbus
  //val myTempLcvIbusIo = cloneOf(cpu.io.lcvIbus)

  //myTempLcvIbusIo <-/< cpu.io.lcvIbus
  icache.io.loBus <-/< cpu.io.lcvIbus
  //myTempLcvIbusIo <-/< 
  //val myTempLcvIbusIo = icache.io.hiBus

  val myIcacheDeburster = LcvBusDeburster(
    cfg=LcvBusDebursterConfig(
      loBusCfg=icache.io.hiBus.cfg
    )
  )
  myIcacheDeburster.io.loBus <-/< icache.io.hiBus
  
  val myTempLcvIbusIo = myIcacheDeburster.io.hiBus

  myTempLcvIbusIo.h2dBus.translateInto(
    myMainMemArbiter.io.hostVec(1).h2dBus
  )(
    dataAssignment=(outp, inp) => {
      outp.mainNonBurstInfo := inp.mainNonBurstInfo
      outp.mainBurstInfo := outp.mainBurstInfo.getZero
    }
  )
  myMainMemArbiter.io.hostVec(1).d2hBus.translateInto(
    myTempLcvIbusIo.d2hBus
  )(
    dataAssignment=(outp, inp) => {
      outp.mainNonBurstInfo := inp.mainNonBurstInfo
    }
  )

  def myFbDevBus = (
    myFbDbusSlicer.io.devVec(cfg.myFbCtrlMmapCfg.optAddrSliceVal.get)
  )
  val myTempFbDbusSlicerDevBus = cloneOf(myFbArbiter.io.hostVec.head)
  myFbDevBus.h2dBus.translateInto(myTempFbDbusSlicerDevBus.h2dBus)(
    dataAssignment=(outp, inp) => {
      outp.mainNonBurstInfo := inp.mainNonBurstInfo
      outp.mainBurstInfo := outp.mainBurstInfo.getZero
    }
  )
  myTempFbDbusSlicerDevBus.d2hBus.translateInto(myFbDevBus.d2hBus)(
    dataAssignment=(outp, inp) => {
      outp.mainNonBurstInfo := inp.mainNonBurstInfo
    }
  )
  myFbArbiter.io.hostVec.head <-/< (
    myTempFbDbusSlicerDevBus
  )
  myFbArbiter.io.hostVec.last <-/< myFbCtrl.io.bus
  myFbMem.io.bus << myFbArbiter.io.dev


  //io.fbBus <-/< myFbArbiter.io.dev

  //lcvVgaCtrl.io.push <-/< myFbCtrl.io.pop
  //--------
}
object SnowHouseCpuFramebufferDemoSharedInfo {
  val cpuCfg = SnowHouseCpuConfig(
    optFormal=(
      //true
      false
    ),
    //targetAltera=(
    //  true
    //),
    programStr=(
      "test/snowhousecpu-framebuffer-demo.bin"
      //"test/snowhousecpu-test-0.bin"
      //"test/snowhousecpu-test-1.bin"
      //"test/snowhousecpu-test-2.bin"
      //"test/snowhousecpu-test-3.bin"
      //"test/snowhousecpu-test-4.bin"
      //"test/snowhousecpu-test-5.bin"
    ),
    instrRamKind=(
      0//,
      //1,
      //2,
      //5
    ),
    //instrRamFetchLatency=(
    //  2
    //),
    exposeRegFileWriteDataToIo=true,
  )
  val testProgram = SnowHouseCpuTestProgram(cfg=cpuCfg)
  val demoCfg = SnowHouseCpuFramebufferDemoConfig(
    program=testProgram.program,
    clkRate=(
      //200 MHz
      //150 MHz
      //125 MHz
      100 MHz
      //24.0 MHz
      //6.0 MHz
      //25.0 MHz
    ),
    rgbCfg=(
      RgbConfig(rWidth=5, gWidth=5, bWidth=5)
      //RgbConfig(rWidth=8, gWidth=8, bWidth=8)
    ),
    vgaTimingInfo=(
      LcvVgaTimingInfoMap.map("640x480@60")
      //LcvVgaTimingInfoMap.map("320x240@60")
    ),
    fbCnt2dShift=ElabVec2[Int](
      x=1,
      y=1,
    )
  )
}

object SnowHouseCpuFramebufferDemoToVerilog extends App {
  val demoCfg = SnowHouseCpuFramebufferDemoSharedInfo.demoCfg
  Config.spinalWithFreq(clkRate=demoCfg.clkRate).generateVerilog({
    //val cfg = SnowHouseCpuConfig(
    //  optFormal=(
    //    false
    //  )
    //)
    //SnowHouseCpuWithDualRam(program=testProgram.program)
    SnowHouseCpuFramebufferDemo(
      cfg=demoCfg
    )
  })
}

object SnowHouseCpuFramebufferDemoSim extends App {

  val testOptTwoCycleRegFileReads = (
    //true
    false
  )

  val programStr = "test/snowhousecpu-framebuffer-demo.bin"
  val numClkCycles = 8192 * 8 * 8 //* 8 * 8 * 8//2 //* 4//* 8 //* 4 * 8
  val cpuCfg = SnowHouseCpuConfig(
    optFormal=(
      //true
      false
    ),
    programStr=(
      programStr
      //"test/snowhousecpu-test-0.bin"
      //"test/snowhousecpu-test-1.bin"
      //"test/snowhousecpu-test-2.bin"
      //"test/snowhousecpu-test-3.bin"
      //"test/snowhousecpu-test-4.bin"
      //"test/snowhousecpu-test-5.bin"
    ),
    instrRamKind=(
      //0//,
      //1,
      //2,
      //5
      //instrRamKind
      0
    ),
    //instrRamFetchLatency=(
    //  0
    //  //1
    //),
    exposeRegFileWriteDataToIo=true,
    exposeRegFileWriteAddrToIo=true,
    exposeRegFileWriteEnableToIo=true,
    optTwoCycleRegFileReads=(
      //true
      testOptTwoCycleRegFileReads
    ),
  )
  val testProgram = SnowHouseCpuTestProgram(cfg=cpuCfg)
  val altDemoCfg = SnowHouseCpuFramebufferDemoConfig(
    program=testProgram.program,
    clkRate=(
      //50.0 MHz
      //125.0 MHz
      //102.0 MHz
      //96.0 MHz
      100.0 MHz
      //48.0 MHz
      //24.0 MHz
      //12.0 MHz
      //6.0 MHz
      //25.0 MHz,
    ),
    rgbCfg=RgbConfig(
      rWidth=5, gWidth=5, bWidth=5
      //rWidth=8, gWidth=8, bWidth=8
    ),
    vgaTimingInfo=(
      //LcvVgaTimingInfoMap.map("640x480@60")
      //LcvVgaTimingInfoMap.map("320x240@60")
      //LcvVgaTimingInfoMap.map("320x240@60")
      LcvVgaTimingInfo(
        pixelClk=(
          //6.0 MHz
          //48.0 MHz
          //24.0 MHz
          //12.0 MHz
          //6.0 MHz
          25.0 MHz
        ),
        htiming=LcvVgaTimingHv(
          visib=(
            //64
            //640
            //76
            //160
            320
          ),
          //front=1,
          //sync=1,
          //back=1
          front=16,
          sync=96,
          back=48
        ),
        vtiming=LcvVgaTimingHv(
          visib=(
            //64
            //480
            76
          ),
          //front=1,
          //sync=1,
          //back=1
          front=10,
          sync=2,
          back=33
        ),
      )
    ),
    fbCnt2dShift=ElabVec2[Int](
      x=1,
      y=1,
    ),
  )
  val demoCfg = (
    //SnowHouseCpuFramebufferDemoSharedInfo.demoCfg
    altDemoCfg
  )
  Config.simWithFreq(demoCfg.clkRate).compile({
    val toComp = (
      SnowHouseCpuFramebufferDemo(
        //program=testProgram.program,
        //doConnExternIrq=false,
        cfg=demoCfg,
      )
    )
    //toComp.setDefinitionName(
    //  s"SnowHouseCpuWithSharedRam_${testIdx}_${instrRamKind}"
    //)
    toComp
  }).doSim{dut => {

    //println(
    //  s"help me out:${(1e9 ns) / demoCfg.clkRate}"
    //)
    //println(
    //  //s"${(((1 sec) / demoCfg.clkRate) * 1e9)} "
    //  //s"${(((1 sec) / demoCfg.clkRate)) ns}"
    //  s"${(((1 sec) / demoCfg.clkRate)) sec}"
    //)
    dut.clockDomain.forkStimulus(
      //(((1e9 ns) / demoCfg.clkRate) * 1.0).toInt //ns
      //8
      //((1e9) / demoCfg.clkRate)
      (((1 sec) / demoCfg.clkRate)) sec //ns //ms
    )
    dut.vgaClockDomain.forkStimulus(
      //40
      (((1 sec) / demoCfg.vgaTimingInfo.pixelClk)) sec //ns //ms
    )
    for (i <- 0 until numClkCycles) {
      dut.clockDomain.waitSampling()
      dut.vgaClockDomain.waitSampling()
      //var tickVgaClk: Boolean = false
      //if (
      //  (
      //    i
      //    % (
      //      //demoCfg.vgaTimingInfo.pixelClk / (1.0 MHz)
      //      demoCfg.clkRate / demoCfg.vgaTimingInfo.pixelClk /// (1.0 MHz)
      //    )
      //  ) == 0
      //) {
      //  tickVgaClk = true
      //  dut.vgaClockDomain.waitSampling()
      //}
      //println(
      //  s"i:${i}, tickVgaClk:${tickVgaClk}"
      //)
    }
  }}
}
