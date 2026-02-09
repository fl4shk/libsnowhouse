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

case class SnowHouseCpuFramebufferDemoConfig(
  program: SnowHouseCpuProgram,
  clkRate: HertzNumber,
  rgbCfg: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  fbCnt2dShiftOne: ElabVec2[Boolean],
) {
  def cpuCfg = program.cfg
  val myDbusCfg = cpuCfg.shCfg.subCfg.lcvDbusEtcCfg.loBusCfg
  //def myDbusCfg = //cpu.io.lcvDbus.cfg

  val myFbMmapCfg = LcvBusMemMapConfig(
    busCfg=(
      //myDbusCfg
      LcvBusConfig(
        mainCfg=myDbusCfg.mainCfg.mkCopyWithAllowingBurst(),
        cacheCfg=myDbusCfg.cacheCfg,
      )
    ),
    addrSliceHi=24,
    addrSliceLo=24,
    optAddrSliceVal=(
      // the framebuffer has bit 24 of the address asserted!
      Some(1)
    )
  )
  val myFbCfg = LcvBusFramebufferConfig(
    fbMmapCfg=myFbMmapCfg,
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
          >> (if (fbCnt2dShiftOne.x) (1) else (0))
        ),
        y=(
          vgaTimingInfo.fbSize2d.y
          >> (if (fbCnt2dShiftOne.y) (1) else (0))
        ),
      )
    ),
    cnt2dShiftOne=fbCnt2dShiftOne,
    dblBuf=true,
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
  val phys = out(LcvVgaPhys(rgbConfig=cfg.rgbCfg))
  val misc = out(LcvVgaCtrlMiscIo(
    clkRate=cfg.clkRate,
    vgaTimingInfo=cfg.vgaTimingInfo,
    fifoDepth=1,  // we don't care about this since we're using `VgaCtrl`
                  // instead of `LcvVgaCtrl`
    optIncludeMiscVgaStates=true,
  ))
}
case class SnowHouseCpuFramebufferDemo(
  cfg: SnowHouseCpuFramebufferDemoConfig
) extends Component {
  //--------
  def cpuCfg = cfg.cpuCfg

  val io = SnowHouseCpuFramebufferDemoIo(cfg=cfg)
  //--------
  val cpu = SnowHouseCpuWithoutRam(program=cfg.program)
  cpu.io.idsIraIrq.nextValid := False
  //--------
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
  //  cnt2dShiftOne=cfg.fbCnt2dShiftOne,
  //  dblBuf=true,
  //)

  // bus stuff goes here

  val myFbCtrl = LcvBusFramebufferCtrl(
    cfg=(
      cfg.myFbCfg
      //myDbgFbCfg
    )
  )

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
          var toAdd = BigInt(idx)
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
      mmapCfg=cfg.myFbMmapCfg,
    )
  )

  val myMainMemArbiter = LcvBusArbiter(
    cfg=LcvBusArbiterConfig(
      busCfg=myMainMemCfg.busCfg,
      numHosts=2,
    ),
  )
  myMainMem.io.bus <-/< myMainMemArbiter.io.dev

  val myFbArbiter = LcvBusArbiter(
    cfg=LcvBusArbiterConfig(
      busCfg=(
        //cfg.myDbusCfg
        cfg.myFbMmapCfg.busCfg
      ),
      numHosts=2,
    )
  )

  cpu.io.lcvDbus.h2dBus.translateInto(myFbDbusSlicer.io.host.h2dBus)(
    dataAssignment=(outp, inp) => {
      outp.mainNonBurstInfo := inp.mainNonBurstInfo
      outp.mainBurstInfo := outp.mainBurstInfo.getZero
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

  myFbArbiter.io.hostVec.head <-/< (
    myFbDbusSlicer.io.devVec(cfg.myFbMmapCfg.optAddrSliceVal.get)
  )
  myFbArbiter.io.hostVec.last <-/< myFbCtrl.io.bus
  myFbMem.io.bus <-/< myFbArbiter.io.dev


  //io.fbBus <-/< myFbArbiter.io.dev

  vgaCtrl.io.softReset := RegNext(False) init(True)
  vgaCtrl.io.pixels <-/< myFbCtrl.io.pop
  //--------
}

object SnowHouseCpuFramebufferDemoToVerilog extends App {
  Config.spinal.generateVerilog({
    //val cfg = SnowHouseCpuConfig(
    //  optFormal=(
    //    false
    //  )
    //)
    val cpuCfg = SnowHouseCpuConfig(
      optFormal=(
        //true
        false
      ),
      //targetAltera=(
      //  true
      //),
      programStr=(
        "test/snowhousecpu-test-0.bin"
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
    //SnowHouseCpuWithDualRam(program=testProgram.program)
    SnowHouseCpuFramebufferDemo(
      cfg=SnowHouseCpuFramebufferDemoConfig(
        program=testProgram.program,
        clkRate=100 MHz,
        rgbCfg=(
          //RgbConfig(rWidth=5, gWidth=5, bWidth=5)
          RgbConfig(rWidth=8, gWidth=8, bWidth=8)
        ),
        vgaTimingInfo=LcvVgaTimingInfoMap.map("640x480@60"),
        fbCnt2dShiftOne=ElabVec2[Boolean](
          x=true,
          y=true,
        )
      )
    )
  })
}

object SnowHouseCpuFramebufferDemoSim extends App {

  val testOptTwoCycleRegFileReads = (
    //true
    false
  )

  val programStr = "test/snowhousecpu-framebuffer-demo.bin"
  val numClkCycles = 8192 * 8 //* 4 * 8
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
  val demoCfg = SnowHouseCpuFramebufferDemoConfig(
    program=testProgram.program,
    clkRate=100.0 MHz,
    rgbCfg=RgbConfig(
      //rWidth=5, gWidth=5, bWidth=5
      rWidth=8, gWidth=8, bWidth=8
    ),
    vgaTimingInfo=LcvVgaTimingInfoMap.map("640x480@60"),
    fbCnt2dShiftOne=ElabVec2[Boolean](
      x=true,
      y=true,
    ),
  )
  Config.sim.compile({
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

    dut.clockDomain.forkStimulus(10)
    for (i <- 0 until numClkCycles) {
      dut.clockDomain.waitSampling()
    }
  }}
}
