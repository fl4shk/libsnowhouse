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

//case class SnowHouseCacheConfig(
//  cfg: SnowHouseConfig
//) {
//}
case class SnowHouseCacheIo(
  cfg: SnowHouseConfig,
  isIcache: Boolean,
) extends Bundle /*with IMasterSlave*/ {
  def subCfg = cfg.subCfg
  if (isIcache) {
    assert(
      subCfg.haveIcache,
      s"icache requires subCfg.haveIcache"
    )
  } else {
    assert(
      subCfg.haveDcache,
      s"dcache requires subCfg.haveDcache"
    )
  }
  def cacheCfg = (
    if (isIcache) (
      subCfg.icacheCfg
    ) else (
      subCfg.dcacheCfg
    )
  )
  //val haveHazard = (
  //  !isIcache
  //) generate (
  //  in(Bool())
  //)
  val bus = (
    slave(
      new LcvStallIo[BusHostPayload, BusDevPayload](
        sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=isIcache)),
        recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=isIcache)),
      )
    )
  )
  val busExtraReady = out(Vec.fill(
    cfg.lowerMyFanout
  )(
    Bool()
  ))
  //val bridgeCfg = (
  //  LcvStallToTilelinkConfig(
  //    addrWidth=(
  //      //cfg.mainWidth
  //      cacheCfg.addrWidth
  //    ),
  //    dataWidth=(
  //      cacheCfg.wordWidth
  //      //if (isIcache) (
  //      //  subCfg.icacheCfg.addrWidth
  //      //) else (
  //      //  subCfg.dcacheCfg.addrWidth
  //      //)
  //    ),
  //    sizeBytes=(
  //      //if (isIcache) (
  //      //  subCfg.icacheCfg.wordWidth / 8
  //      //) else (
  //      //  subCfg.dcacheCfg.wordWidth / 8
  //      //)
  //      cacheCfg.wordWidth / 8
  //    ),
  //    srcWidth=(
  //      subCfg.cacheBusSrcWidth
  //    ),
  //    isDual=true,
  //  )
  //)
  def bridgeCfg = cacheCfg.bridgeCfg
  def tlCfg = bridgeCfg.tlCfg
  //val tlCfg = tilelink.BusParameter.simple(
  //  addressWidth=(
  //    //cfg.mainWidth
  //    cacheCfg.addrWidth
  //  ),
  //  dataWidth=(
  //    cacheCfg.wordWidth
  //    //if (isIcache) (
  //    //  subCfg.icacheCfg.addrWidth
  //    //) else (
  //    //  subCfg.dcacheCfg.addrWidth
  //    //)
  //  ),
  //  sizeBytes=(
  //    //if (isIcache) (
  //    //  subCfg.icacheCfg.wordWidth / 8
  //    //) else (
  //    //  subCfg.dcacheCfg.wordWidth / 8
  //    //)
  //    cacheCfg.wordWidth / 8
  //  ),
  //  sourceWidth=(
  //    subCfg.cacheBusSrcWidth
  //  )
  //)
  val tlBus = master(tilelink.Bus(
    p=tlCfg
  ))
}
case class SnowHouseCacheLineAttrs(
  cfg: SnowHouseConfig,
  isIcache: Boolean,
) extends Bundle {
  //--------
  def cacheCfg = (
    if (isIcache) (
      cfg.subCfg.icacheCfg
    ) else (
      cfg.subCfg.dcacheCfg
    )
  )
  //--------
  val valid = Bool()
  def fire = valid
  //--------
  val tag = UInt(
    cacheCfg.tagWidth bits
  )
  val dirty = (
    !isIcache
  ) generate (
    Bool()
  )
  //--------
}
case class SnowHouseCacheHigherFmax(
  cfg: SnowHouseConfig,
  isIcache: Boolean,
) extends Component {
  val io = SnowHouseCacheIo(
    cfg=cfg,
    isIcache=isIcache,
  )
  def bridgeCfg = io.bridgeCfg
  def cacheCfg = (
    io.cacheCfg
    //if (isIcache) (
    //  subCfg.icacheCfg
    //) else (
    //  subCfg.dcacheCfg
    //)
  )
  val bridge = LcvStallDualToTilelink(
    cfg=bridgeCfg
  )
  def myH2dBus = bridge.io.h2dBus
  def myD2hBus = bridge.io.d2hBus
  myH2dBus.nextValid := (
    RegNext(
      next=myH2dBus.nextValid,
      init=myH2dBus.nextValid.getZero,
    )
  )
  //myH2dBus.sendData := (
  //  RegNext(
  //    next=myH2dBus.sendData,
  //    init=myH2dBus.sendData.getZero
  //  )
  //)
  //myH2dBus.sendData.setAsReg
  //myH2dBus.sendData.init(myH2dBus.sendData.getZero)
  val rH2dSendData = {
    val temp = Reg(cloneOf(myH2dBus.sendData))
    temp.init(temp.getZero)
    temp
  }
  myH2dBus.sendData := rH2dSendData
  myD2hBus.ready := (
    //RegNext(
    //  next=myD2hBus.ready,
    //  init=myD2hBus.ready.getZero,
    //)
    False
  )
  io.tlBus << bridge.io.tlBus
  val nextBridgeSavedFires = UInt(2 bits)
  val rBridgeSavedFires = (
    RegNext(
      next=nextBridgeSavedFires,
      init=nextBridgeSavedFires.getZero,
    )
  )
  nextBridgeSavedFires := rBridgeSavedFires

  def subCfg = cfg.subCfg
  def depthWords = (
    cacheCfg.depthWords
  )
  def depthBytes = (
    cacheCfg.depthBytes
  )
  def depthLines = (
    //cacheCfg.depth / (cacheCfg.lineSizeBytes * cacheCfg.dataWidth)
    cacheCfg.depthLines
  )
  println(
    s"depthWords:${cacheCfg.depthWords} "
    + s"depthBytes:${cacheCfg.depthBytes} "
    + s"depthLines:${cacheCfg.depthLines} "
  )
  val lineWordRam = FpgacpuRamSimpleDualPort(
    wordType=UInt(cacheCfg.wordWidth bits),
    depth=depthWords,
    initBigInt=Some(Array.fill(depthWords)(BigInt(0))),
    arrRamStyle=(
      if (isIcache) (
        cfg.subCfg.icacheCfg.memRamStyle
      ) else (
        cfg.subCfg.dcacheCfg.memRamStyle
      )
    )
  )
  val lineAttrsRam = FpgacpuRamSimpleDualPort(
    wordType=SnowHouseCacheLineAttrs(
      cfg=cfg,
      isIcache=isIcache,
    ),
    depth=depthLines,
    initBigInt=Some(Array.fill(depthLines)(BigInt(0))),
    arrRamStyle=(
      if (isIcache) (
        cfg.subCfg.icacheCfg.memRamStyle
      ) else (
        cfg.subCfg.dcacheCfg.memRamStyle
      )
    )
  )
  val tempLineBusAddr = (
    /*KeepAttribute*/(cloneOf(rBusAddr))
  )
  val nextLineAddrCnt = (
    UInt(
      log2Up(cacheCfg.lineSizeBytes) - log2Up(cacheCfg.wordSizeBytes) //+ 1
      bits
    )
  )
  //println(
  //  1 << nextLineAddrCnt.getWidth
  //)
  //println(
  //  nextLineAddrCnt.getWidth
  //)
  val rLineAddrCnt = (
    /*KeepAttribute*/(
      RegNext/*When*/(
        next=nextLineAddrCnt,
        //cond=io.tlBus.a.fire,
        init=nextLineAddrCnt.getZero,
      )
    )
  )
  nextLineAddrCnt := rLineAddrCnt
  val rRevLineAddrCnt = (
    Reg(UInt((rLineAddrCnt.getWidth + 1) bits))
    init(
      //io.tlCfg.beatMax - 2
      (
        1
        << (
          log2Up(cacheCfg.lineSizeBytes) - log2Up(cacheCfg.wordSizeBytes)
        )
      ) - 1
    )
  )
  tempLineBusAddr := (
    Cat(
      rBusAddr(rBusAddr.high downto log2Up(cacheCfg.lineSizeBytes)),
      //U(log2Up(cacheCfg.lineSizeBytes) bits, default -> False),
      //rLineAddrCnt(rLineAddrCnt.high - 1 downto 0),
      //nextLineAddrCnt,
      rLineAddrCnt,
      U(log2Up(cacheCfg.wordSizeBytes) bits, default -> False),
    ).asUInt
  )
  println(
    rBusAddr.high downto log2Up(cacheCfg.lineSizeBytes)
  )
  val rSavedRdLineAttrs = {
    val temp = Reg(
      SnowHouseCacheLineAttrs(
        cfg=cfg,
        isIcache=isIcache,
      )
    )
    temp.init(temp.getZero)
    temp
  }
  val rdLineAttrs = (
    /*KeepAttribute*/(
      SnowHouseCacheLineAttrs(
        cfg=cfg,
        isIcache=isIcache,
      )
    )
  )
  rdLineAttrs.assignFromBits(lineAttrsRam.io.rdData)
  val tempRdLineAttrsAddr = (
    Cat(
      rSavedRdLineAttrs.tag,
      rBusAddr(
        rBusAddr.high - cacheCfg.tagWidth - 1
        downto log2Up(cacheCfg.lineSizeBytes)
      ),
      nextLineAddrCnt,
      U(log2Up(cacheCfg.wordSizeBytes) bits, default -> False),
    ).asUInt
  )
  val tempRdLineAttrsAddr1 = (
    Cat(
      rSavedRdLineAttrs.tag,
      //U(log2Up(cacheCfg.lineSizeBytes) bits, default -> False),
      //rLineAddrCnt(rLineAddrCnt.high - 1 downto 0),
      //nextLineAddrCnt,
      rBusAddr(
        rBusAddr.high - cacheCfg.tagWidth - 1
        downto log2Up(cacheCfg.lineSizeBytes)
      ),
      rLineAddrCnt,
      //nextLineAddrCnt,
      U(log2Up(cacheCfg.wordSizeBytes) bits, default -> False),
    ).asUInt
  )
  val tempRdLineAttrsAddr2 = (
    Cat(
      rdLineAttrs.tag,
      //U(log2Up(cacheCfg.lineSizeBytes) bits, default -> False),
      //rLineAddrCnt(rLineAddrCnt.high - 1 downto 0),
      //nextLineAddrCnt,
      rBusAddr(
        rBusAddr.high - cacheCfg.tagWidth - 1
        downto log2Up(cacheCfg.lineSizeBytes)
      ),
      rLineAddrCnt,
      //nextLineAddrCnt,
      U(log2Up(cacheCfg.wordSizeBytes) bits, default -> False),
    ).asUInt
  )
  //println(
  //  s"test: "
  //  + Cat(
  //    rLineAddrCnt,
  //    U(cacheCfg.wordSizeBytes bits, default -> False),
  //  ).getWidth
  //)
  println(
    s"tagWidth:${cacheCfg.tagWidth} "
    + s"tempLineBusAddr.getWidth:${tempLineBusAddr.getWidth} "
    + s"tempRdLineAttrsAddr.getWidth:${tempRdLineAttrsAddr.getWidth}"
  )
  def incrLineBusAddrCnts(): Unit = {
    nextLineAddrCnt := rLineAddrCnt + 1
    rRevLineAddrCnt := rRevLineAddrCnt - 1
  }
  def setLineBusAddrCntsToStart(): Unit = {
    nextLineAddrCnt := 0x0
    rRevLineAddrCnt := (
      ///*log2Up*/(io.tlCfg.beatMax) - 2
      (
        1
        << (
          log2Up(cacheCfg.lineSizeBytes) - log2Up(cacheCfg.wordSizeBytes)
        )
      ) - 1
    )
    //rRecvCnt := io.tlCfg.beatMax - 2
  }
  def atLastLineBusAddrCnt() = (
    //rLineAddrCnt.msb
    rRevLineAddrCnt.msb
  )
  //def atLastRecvCnt() = (
  //  rRecvCnt.msb
  //)
  def rBusSendData = (
    RegNext(
      next=io.bus.sendData,
      init=io.bus.sendData.getZero,
    )
  )
  def rBusAddr = (
    rBusSendData.addr
    ////if (isIcache) (
    //  RegNext(
    //    next=io.bus.sendData.addr,
    //    init=io.bus.sendData.addr.getZero
    //  )
    ////) else (
    //  io.bus.sendData.addr
    //)
  )
  def busDevData = (
    if (isIcache) (
      io.bus.recvData.instr
    ) else (
      io.bus.recvData.data
    )
  )
  if (!isIcache) {
    //io.bus.recvData.setAsReg()
    //io.bus.ready.setAsReg() init(False)
    //io.busExtraReady.setAsReg() //init(False)
    //io.busExtraReady.foreach(extraReady => {
    //  extraReady.init(extraReady.getZero)
    //})
    //io.busExtraReady.addAttribute(KeepAttribute.keep)
  }
  def doSetBusReadyEtc(
    someReady: Bool
  ): Unit = {
    io.bus.ready := someReady
    io.busExtraReady.foreach(extraReady => {
      extraReady := someReady
    })
  }
  if (!isIcache) {
    busDevData := (
      RegNext(
        next=busDevData,
        init=busDevData.getZero,
      )
    )
  }
  //doSetBusReadyEtc(
  //  someReady=False
  //)
  val rBusDevData = (
    !isIcache
  ) generate (
    Reg(
      cloneOf(busDevData),
      init=busDevData.getZero
    )
  )
  if (isIcache) {
    busDevData := (
      RegNext(
        next=busDevData,
        init=busDevData.getZero,
      )
    )
  } else {
    //busDevData := rBusDevData
  }
  object State extends SpinalEnum(
    defaultEncoding=(
      //binarySequential
      binaryOneHot
    )
  ) {
    val
      IDLE,
      //HANDLE_DCACHE_LOAD_HIT_EXTEND,
      HANDLE_DCACHE_STORE_HIT,
      HANDLE_SEND_LINE_TO_BUS_PIPE_1,
      HANDLE_SEND_LINE_TO_BUS,
      //HANDLE_RECV_LINE_FROM_BUS_PIPE_1,
      HANDLE_RECV_LINE_FROM_BUS,
      HANDLE_NON_CACHED_BUS_ACC
      = newElement()
  }
  val nextState = State()
  val rState = (
    RegNext(nextState)
    init(State.IDLE)
  )
  nextState := rState
  //--------
  //lineWordRam.io.rdEn := False
  //lineWordRam.io.rdAddr := (
  //  0x0
  //)
  lineWordRam.io.wrEn := False

  //lineAttrsRam.io.rdEn := False
  //lineAttrsRam.io.rdAddr := (
  //  0x0
  //)
  lineAttrsRam.io.wrEn := False

  val myLineRamAddrRshift = (
    log2Up(cacheCfg.wordSizeBytes)
  )
  val rdLineWord = (
    /*KeepAttribute*/(
      //UInt(lineWordRam.io.rdData.getWidth bits)
      UInt(cacheCfg.wordWidth bits)
    )
  )
  rdLineWord := lineWordRam.io.rdData.asUInt
  val wrLineAttrs = SnowHouseCacheLineAttrs(
    cfg=cfg,
    isIcache=isIcache,
  )
  wrLineAttrs.allowOverride
  //wrLineAttrs := wrLineAttrs.getZero
  wrLineAttrs := (
    RegNext(
      next=wrLineAttrs,
      init=wrLineAttrs.getZero
    )
  )
  doLineAttrsRamWrite(
    busAddr=RegNext(next=rBusAddr, init=rBusAddr.getZero),
    setEn=false,
  )
  val rPastBusSendDataData = (
    Reg(
      cloneOf(rBusSendData.data),
      init=rBusSendData.data.getZero,
    )
  )
  rPastBusSendDataData.allowOverride
  doLineWordRamWrite(
    busAddr=RegNext(next=rBusAddr, init=rBusAddr.getZero),
    lineWord=rPastBusSendDataData,
    setEn=false,
  )

  def haveHit = (
    rdLineAttrs.fire
    //currLineValid
    && rdLineAttrs.tag === rBusAddrTag
  )

  def doAllLineRamsReadSync(
    busAddr: UInt,
  ): Unit = {
    doLineWordRamReadSync(busAddr=busAddr)
    doLineAttrsRamReadSync(busAddr=busAddr)
  }
  def doLineWordRamReadSync(
    busAddr: UInt,
  ): Unit = {
    lineWordRam.io.rdEn := True
    lineWordRam.io.rdAddr := (
      (busAddr >> myLineRamAddrRshift)
      .resize(lineWordRam.io.rdAddr.getWidth)
    )
  }
  def doLineWordRamWrite(
    busAddr: UInt,
    lineWord: UInt,
    setEn: Boolean=true,
  ): Unit = {
    if (setEn) {
      lineWordRam.io.wrEn := True
    }
    lineWordRam.io.wrAddr := (
      (busAddr >> myLineRamAddrRshift)
      .resize(lineWordRam.io.wrAddr.getWidth)
    )
    lineWordRam.io.wrData := lineWord.asBits
  }
  def doLineAttrsRamReadSync(
    busAddr: UInt,
  ): Unit = {
    lineAttrsRam.io.rdEn := True
    lineAttrsRam.io.rdAddr := (
      (busAddr >> log2Up(cacheCfg.lineSizeBytes))
      .resize(lineAttrsRam.io.rdAddr.getWidth)
    )
  }
  def doLineAttrsRamWrite(
    busAddr: UInt,
    lineAttrs: SnowHouseCacheLineAttrs=wrLineAttrs,
    setEn: Boolean=true,
  ): Unit = {
    if (setEn) {
      lineAttrsRam.io.wrEn := True
    }
    lineAttrsRam.io.wrAddr := (
      (busAddr >> log2Up(cacheCfg.lineSizeBytes))
      .resize(lineAttrsRam.io.wrAddr.getWidth)
    )
    lineAttrsRam.io.wrData := lineAttrs.asBits
  }
  //--------
  //io.bus.ready := False
  doSetBusReadyEtc(False)
  def rBusAddrIsNonCached = (
    (rBusAddr(cacheCfg.nonCachedRange) =/= 0x0)
    //init(False)
  )
  def rBusAddrTag = (
    (rBusAddr(cacheCfg.tagRange))
    //init(0x0)
  )
  val rSavedBusAddr = (
    Reg(cloneOf(rBusAddr), init=rBusAddr.getZero)
  )
  def rSavedBusAddrTag = (
    rSavedBusAddr(cacheCfg.tagRange)
  )

  //when (
  //  //nextState === State.IDLE
  //  //&&
  //  io.bus.nextValid
  //) {
    doAllLineRamsReadSync(busAddr=io.bus.sendData.addr)
    //doLineWordRamReadSync(busAddr=io.bus.sendData.addr)
    //doLineAttrsRamReadSync(busAddr=io.bus.sendData.addr)
  //}
  val rBusReadyCnt = (
    /*KeepAttribute*/(
      Reg(Bool(), init=False)
      //Reg(UInt(2 bits))
      //init(0x0)
    )
  )
  //val rTempBusReady = Reg(Bool(), init=False)
  //val rSavedHaveHit = Reg(Bool(), init=False)
  val rSavedRdLineWord = Reg(cloneOf(rdLineWord), init=rdLineWord.getZero)
  //def doPipe(): Unit = {
  //}
  val rSavedBusSendData = Reg(cloneOf(io.bus.sendData))
  val rPleaseFinish = (
    Vec.fill(3)(
      Vec.fill(3)(
        Reg(
          Bool(),
          init=False,
        )
      )
    )
  )
  when (rPleaseFinish(2).sFindFirst(_ === True)._1) {
    //io.bus.ready := True
    doSetBusReadyEtc(True)
  }
  when (
    //io.bus.fire
    //(RegNext(io.bus.nextValid) init(False))
    //&& 
    io.bus.ready
  ) {
    rPleaseFinish.foreach(myVec => {
      myVec.foreach(current => {
        current := False
      })
    })
    //io.bus.ready := False
    //doSetBusReadyEtc(False)
  }
  when (rPleaseFinish(0).sFindFirst(_ === True)._1) {
    when (rSavedBusSendData.accKind.asBits(1)) {
      busDevData := rSavedBusSendData.data //RegNext(rBusSendData.data)
    } otherwise {
      busDevData := rBusDevData
    }
  }

  //rBusDevData := rdLineWord
  rPastBusSendDataData := /*RegNext*/(rdLineWord)
  //when (!RegNext(rBusSendData.accKind).asBits(1)) {
    switch (
      RegNext(next=rBusSendData.subKind, init=rBusSendData.subKind)
    ) {
      is (SnowHouseMemAccessSubKind.Sz8) {
        if (rBusDevData.getWidth > 8) {
          when (!RegNext(rBusSendData).accKind.asBits(0)) {
            rBusDevData := (
              rdLineWord(
                7 downto 0
              ).resize(rBusDevData.getWidth)
            )
          } otherwise {
            rBusDevData := (
              rdLineWord(
                7 downto 0
              ).asSInt.resize(rBusDevData.getWidth).asUInt
            )
          }
          //RegNext(rBusSendData).data
          rPastBusSendDataData(
            offset=/*RegNext*/(rBusSendData).addr(
              log2Up(cacheCfg.wordWidth) - 1 downto 0
            ),
            8 bits
          ) := (
            /*RegNext*/(rBusSendData).data(7 downto 0)
          )
        } else {
          rBusDevData := rdLineWord.resized
          rPastBusSendDataData := /*RegNext*/(rBusSendData).data.resized
        }
      }
      is (SnowHouseMemAccessSubKind.Sz16) {
        if (rBusDevData.getWidth > 16) {
          when (!RegNext(rBusSendData).accKind.asBits(0)) {
            rBusDevData := (
              rdLineWord(
                15 downto 0
              ).resize(rBusDevData.getWidth)
            )
          } otherwise {
            rBusDevData := (
              rdLineWord(
                15 downto 0
              ).asSInt.resize(rBusDevData.getWidth).asUInt
            )
          }
          rPastBusSendDataData(
            offset=/*RegNext*/(rBusSendData).addr(
              log2Up(cacheCfg.wordWidth) - 1 downto 1
            ),
            16 bits
          ) := (
            /*RegNext*/(rBusSendData).data(15 downto 0)
          )
        } else {
          rBusDevData := rdLineWord.resized
          rPastBusSendDataData := /*RegNext*/(rBusSendData).data.resized
        }
      }
      is (SnowHouseMemAccessSubKind.Sz32) {
        if (rBusDevData.getWidth > 32) {
          when (!RegNext(rBusSendData).accKind.asBits(0)) {
            rBusDevData := (
              rdLineWord(
                31 downto 0
              ).resize(rBusDevData.getWidth)
            )
          } otherwise {
            rBusDevData := (
              rdLineWord(
                31 downto 0
              ).asSInt.resize(rBusDevData.getWidth).asUInt
            )
          }
          rPastBusSendDataData(
            offset=/*RegNext*/(rBusSendData).addr(
              log2Up(cacheCfg.wordWidth) - 1 downto 2
            ),
            32 bits
          ) := (
            /*RegNext*/(rBusSendData).data(31 downto 0)
          )
        } else {
          rBusDevData := rdLineWord.resized
          rPastBusSendDataData := /*RegNext*/(rBusSendData).data.resized
        }
      }
      is (SnowHouseMemAccessSubKind.Sz64) {
        if (rBusDevData.getWidth > 64) {
          when (!RegNext(rBusSendData).accKind.asBits(0)) {
            rBusDevData := (
              rdLineWord(
                63 downto 0
              ).resize(rBusDevData.getWidth)
            )
          } otherwise {
            rBusDevData := (
              rdLineWord(
                63 downto 0
              ).asSInt.resize(rBusDevData.getWidth).asUInt
            )
          }
          rPastBusSendDataData(
            offset=/*RegNext*/(rBusSendData).addr(
              log2Up(cacheCfg.wordWidth) - 1 downto 3
            ),
            64 bits
          ) := (
            /*RegNext*/(rBusSendData).data(63 downto 0)
          )
        } else {
          rBusDevData := rdLineWord.resized
          rPastBusSendDataData := /*RegNext*/(rBusSendData).data.resized
        }
      }
    }
  //} //otherwise 
  switch (rState) {
    is (State.IDLE) {
      when (
        (RegNext(io.bus.nextValid) init(False))
        && !rPleaseFinish(1).sFindFirst(_ === True)._1
      ) {
        nextBridgeSavedFires := 0x0
        setLineBusAddrCntsToStart()
        myH2dBus.nextValid := False
        rSavedBusAddr := rBusAddr
        rSavedRdLineAttrs := rdLineAttrs
        rSavedRdLineWord := rdLineWord
        //rSavedHaveHit := haveHit
        rSavedBusSendData := rBusSendData
        when (RegNext(io.bus.nextValid) init(False)) {
          when (if (isIcache) (True) else (!rBusAddrIsNonCached)) {
            when (
              if (isIcache) (
                True
              ) else (
                !rBusSendData.accKind.asBits(1)
              )
            ) {
              when (haveHit) {
                // cached load
                if (isIcache) {
                  doSetBusReadyEtc(True)
                  busDevData := (
                    rdLineWord.resize(busDevData.getWidth)
                  )
                } else {
                  rPleaseFinish.foreach(current => {
                    current(0) := True
                  })
                }
              } otherwise {
                // cache miss upon a load
                when (if (isIcache) (False) else (rdLineAttrs.dirty)) {
                  nextState := State.HANDLE_SEND_LINE_TO_BUS_PIPE_1
                } otherwise {
                  nextState := State.HANDLE_RECV_LINE_FROM_BUS
                }
              }
            } otherwise {
              if (!isIcache) {
                when (haveHit) {
                  // cached store
                  nextState := State.HANDLE_DCACHE_STORE_HIT
                  lineAttrsRam.io.rdEn := False
                  lineWordRam.io.rdEn := False
                  rBusReadyCnt := True
                  wrLineAttrs := rdLineAttrs
                  wrLineAttrs.dirty := True
                } otherwise {
                  // cache miss upon a store
                  when (if (isIcache) (False) else (rdLineAttrs.dirty)) {
                    nextState := State.HANDLE_SEND_LINE_TO_BUS_PIPE_1
                  } otherwise {
                    nextState := State.HANDLE_RECV_LINE_FROM_BUS
                  }
                }
              }
            }
          } otherwise {
            if (!isIcache) {
              // non-cached access to the bus
              nextState := State.HANDLE_NON_CACHED_BUS_ACC
              myH2dBus.nextValid := True
              rH2dSendData.isWrite := rBusSendData.accKind.asBits(1)
              rH2dSendData.addr := (
                rBusAddr.resize(rH2dSendData.addr.getWidth)
              )
              rH2dSendData.data := rBusSendData.data
              rH2dSendData.size := 1
              rH2dSendData.mask := (
                U(rH2dSendData.mask.getWidth bits, default -> True)
              )
              //--------
            }
          }
        }
      }
    }
    is (State.HANDLE_DCACHE_STORE_HIT) {
      nextState := State.IDLE
      rPleaseFinish.foreach(current => {
        current(1) := True
      })
      lineAttrsRam.io.wrEn := True
      lineWordRam.io.wrEn := True
    }
    is (State.HANDLE_SEND_LINE_TO_BUS_PIPE_1) {
      nextState := State.HANDLE_SEND_LINE_TO_BUS 
      doLineWordRamReadSync(
        busAddr=(
          RegNext(
            next=tempRdLineAttrsAddr2,
            init=tempRdLineAttrsAddr2.getZero
          )
        )
      )
    }
    is (State.HANDLE_SEND_LINE_TO_BUS) {
      rH2dSendData.isWrite := True
      //rH2dSendData.data := (
      //  0x0
      //  ////rBusSendData.data
      //  //rdLineWord
      //  //// since this can be anything (per the Tilelink spec)
      //  //// for an opcode of `GET`, we can put the data read
      //  //// from `lineRam` here every time
      //)
      rH2dSendData.size := (
        //log2Up(io.tlCfg.beatMax)
        log2Up(cacheCfg.lineSizeBytes) //- log2Up(cacheCfg.wordSizeBytes)
      )
      rH2dSendData.src := cacheCfg.srcId
      rH2dSendData.data := rdLineWord
      rH2dSendData.mask := (
        U(rH2dSendData.mask.getWidth bits, default -> True)
      )
      val tempBridgeSavedFires = Bool()
      tempBridgeSavedFires := False
      doLineWordRamReadSync(
        busAddr=(
          tempRdLineAttrsAddr
        )
      )
      rH2dSendData.addr := (
        tempRdLineAttrsAddr1.resize(rH2dSendData.addr.getWidth)
      )
      when (
        !RegNext(myH2dBus.nextValid)
        && !rBridgeSavedFires(0)
      ) {
        myH2dBus.nextValid := True
        incrLineBusAddrCnts()
      } elsewhen (
        RegNext(myH2dBus.nextValid)
        && myH2dBus.ready
      ) {
        when (!atLastLineBusAddrCnt) {
          incrLineBusAddrCnts()
        } otherwise {
          myH2dBus.nextValid := False
          nextBridgeSavedFires(0) := True
          tempBridgeSavedFires := True
        }
      }
      when (
        RegNext(myD2hBus.nextValid)
        && (
          //nextBridgeSavedFires(0)
          tempBridgeSavedFires
          || rBridgeSavedFires(0)
        )
      ) {
        myD2hBus.ready := True
        setLineBusAddrCntsToStart()
        nextBridgeSavedFires := 0x0
        nextState := State.HANDLE_RECV_LINE_FROM_BUS
      }
    }
    is (State.HANDLE_RECV_LINE_FROM_BUS) {
      rH2dSendData.isWrite := False
      rH2dSendData.size := (
        log2Up(cacheCfg.lineSizeBytes) //- log2Up(cacheCfg.wordSizeBytes)
      )
      rH2dSendData.src := cacheCfg.srcId
      rH2dSendData.mask := (
        U(rH2dSendData.mask.getWidth bits, default -> True)
      )
      rH2dSendData.addr := (
        tempLineBusAddr.resize(rH2dSendData.addr.getWidth)
      )
      when (
        !RegNext(myH2dBus.nextValid)
        && !rBridgeSavedFires(1)
      ) {
        myH2dBus.nextValid := True
        setLineBusAddrCntsToStart()
        wrLineAttrs.tag := (
          rSavedBusAddrTag
        )
        //wrLineAttrs.valid := True
        if (!isIcache) {
          wrLineAttrs.dirty := False
        }
        doLineAttrsRamWrite(
          busAddr=tempLineBusAddr,
        )
      }
      when (
        RegNext(next=myH2dBus.nextValid, init=False)
        && myH2dBus.ready
      ) {
        myH2dBus.nextValid := False
        nextBridgeSavedFires(1) := True
      }
      when (
        RegNext(next=myD2hBus.nextValid, init=False)
        && (
          rBridgeSavedFires(1)
        )
      ) {
        when (!atLastLineBusAddrCnt()) {
          incrLineBusAddrCnts()
        } otherwise {
          setLineBusAddrCntsToStart()
          nextState := State.IDLE
        }
        doLineWordRamWrite(
          busAddr=tempLineBusAddr,
          lineWord=myD2hBus.sendData.data,
        )
        myD2hBus.ready := True
      }
    }
    //--------
    //--------
    is (State.HANDLE_NON_CACHED_BUS_ACC) {
      when (
        RegNext(next=myH2dBus.nextValid, init=False)
        && myH2dBus.ready
      ) {
        myH2dBus.nextValid := False
        nextBridgeSavedFires(0) := True
      }
      when (
        RegNext(myD2hBus.nextValid)
        && (
          nextBridgeSavedFires(0)
          || rBridgeSavedFires(0)
        )
      ) {
        myD2hBus.ready := True
        if (!isIcache) {
          rBusDevData := myD2hBus.sendData.data
        }
        nextState := (
          State.IDLE
        )
        rPleaseFinish.foreach(current => {
          current(2) := True
        })
      }
    }
  }
  wrLineAttrs.valid := True
}
case class SnowHouseCache(
  cfg: SnowHouseConfig,
  isIcache: Boolean,
) extends Component {
  val io = SnowHouseCacheIo(
    cfg=cfg,
    isIcache=isIcache,
  )
  def bridgeCfg = io.bridgeCfg
  def cacheCfg = (
    io.cacheCfg
    //if (isIcache) (
    //  subCfg.icacheCfg
    //) else (
    //  subCfg.dcacheCfg
    //)
  )
  val bridge = LcvStallDualToTilelink(
    cfg=bridgeCfg
  )
  def myH2dBus = bridge.io.h2dBus
  def myD2hBus = bridge.io.d2hBus
  myH2dBus.nextValid := (
    RegNext(
      next=myH2dBus.nextValid,
      init=myH2dBus.nextValid.getZero,
    )
  )
  //myH2dBus.sendData := (
  //  RegNext(
  //    next=myH2dBus.sendData,
  //    init=myH2dBus.sendData.getZero
  //  )
  //)
  //myH2dBus.sendData.setAsReg
  //myH2dBus.sendData.init(myH2dBus.sendData.getZero)
  val rH2dSendData = {
    val temp = Reg(cloneOf(myH2dBus.sendData))
    temp.init(temp.getZero)
    temp
  }
  myH2dBus.sendData := rH2dSendData
  myD2hBus.ready := (
    //RegNext(
    //  next=myD2hBus.ready,
    //  init=myD2hBus.ready.getZero,
    //)
    False
  )
  io.tlBus << bridge.io.tlBus
  val nextBridgeSavedFires = UInt(2 bits)
  val rBridgeSavedFires = (
    RegNext(
      next=nextBridgeSavedFires,
      init=nextBridgeSavedFires.getZero,
    )
  )
  nextBridgeSavedFires := rBridgeSavedFires

  def subCfg = cfg.subCfg
  def depthWords = (
    cacheCfg.depthWords
  )
  def depthBytes = (
    cacheCfg.depthBytes
  )
  def depthLines = (
    //cacheCfg.depth / (cacheCfg.lineSizeBytes * cacheCfg.dataWidth)
    cacheCfg.depthLines
  )
  println(
    s"depthWords:${cacheCfg.depthWords} "
    + s"depthBytes:${cacheCfg.depthBytes} "
    + s"depthLines:${cacheCfg.depthLines} "
  )
  val lineWordRam = FpgacpuRamSimpleDualPort(
    wordType=UInt(cacheCfg.wordWidth bits),
    depth=depthWords,
    initBigInt=Some(Array.fill(depthWords)(BigInt(0))),
    arrRamStyle=(
      if (isIcache) (
        cfg.subCfg.icacheCfg.memRamStyle
      ) else (
        cfg.subCfg.dcacheCfg.memRamStyle
      )
    )
  )
  val lineAttrsRam = FpgacpuRamSimpleDualPort(
    wordType=SnowHouseCacheLineAttrs(
      cfg=cfg,
      isIcache=isIcache,
    ),
    depth=depthLines,
    initBigInt=Some(Array.fill(depthLines)(BigInt(0))),
    arrRamStyle=(
      if (isIcache) (
        cfg.subCfg.icacheCfg.memRamStyle
      ) else (
        cfg.subCfg.dcacheCfg.memRamStyle
      )
    )
  )
  val tempLineBusAddr = (
    /*KeepAttribute*/(cloneOf(rBusAddr))
  )
  val nextLineAddrCnt = (
    UInt(
      log2Up(cacheCfg.lineSizeBytes) - log2Up(cacheCfg.wordSizeBytes) //+ 1
      bits
    )
  )
  //println(
  //  1 << nextLineAddrCnt.getWidth
  //)
  //println(
  //  nextLineAddrCnt.getWidth
  //)
  val rLineAddrCnt = (
    /*KeepAttribute*/(
      RegNext/*When*/(
        next=nextLineAddrCnt,
        //cond=io.tlBus.a.fire,
        init=nextLineAddrCnt.getZero,
      )
    )
  )
  nextLineAddrCnt := rLineAddrCnt
  val rRevLineAddrCnt = (
    Reg(UInt((rLineAddrCnt.getWidth + 1) bits))
    init(
      //io.tlCfg.beatMax - 2
      (
        1
        << (
          log2Up(cacheCfg.lineSizeBytes) - log2Up(cacheCfg.wordSizeBytes)
        )
      ) - 1
    )
  )
  tempLineBusAddr := (
    Cat(
      rBusAddr(rBusAddr.high downto log2Up(cacheCfg.lineSizeBytes)),
      //U(log2Up(cacheCfg.lineSizeBytes) bits, default -> False),
      //rLineAddrCnt(rLineAddrCnt.high - 1 downto 0),
      //nextLineAddrCnt,
      rLineAddrCnt,
      U(log2Up(cacheCfg.wordSizeBytes) bits, default -> False),
    ).asUInt
  )
  println(
    rBusAddr.high downto log2Up(cacheCfg.lineSizeBytes)
  )
  val rSavedRdLineAttrs = {
    val temp = Reg(
      SnowHouseCacheLineAttrs(
        cfg=cfg,
        isIcache=isIcache,
      )
    )
    temp.init(temp.getZero)
    temp
  }
  val rdLineAttrs = (
    /*KeepAttribute*/(
      SnowHouseCacheLineAttrs(
        cfg=cfg,
        isIcache=isIcache,
      )
    )
  )
  rdLineAttrs.assignFromBits(lineAttrsRam.io.rdData)
  val tempRdLineAttrsAddr = (
    Cat(
      rSavedRdLineAttrs.tag,
      rBusAddr(
        rBusAddr.high - cacheCfg.tagWidth - 1
        downto log2Up(cacheCfg.lineSizeBytes)
      ),
      nextLineAddrCnt,
      U(log2Up(cacheCfg.wordSizeBytes) bits, default -> False),
    ).asUInt
  )
  val tempRdLineAttrsAddr1 = (
    Cat(
      rSavedRdLineAttrs.tag,
      //U(log2Up(cacheCfg.lineSizeBytes) bits, default -> False),
      //rLineAddrCnt(rLineAddrCnt.high - 1 downto 0),
      //nextLineAddrCnt,
      rBusAddr(
        rBusAddr.high - cacheCfg.tagWidth - 1
        downto log2Up(cacheCfg.lineSizeBytes)
      ),
      rLineAddrCnt,
      //nextLineAddrCnt,
      U(log2Up(cacheCfg.wordSizeBytes) bits, default -> False),
    ).asUInt
  )
  val tempRdLineAttrsAddr2 = (
    Cat(
      rdLineAttrs.tag,
      //U(log2Up(cacheCfg.lineSizeBytes) bits, default -> False),
      //rLineAddrCnt(rLineAddrCnt.high - 1 downto 0),
      //nextLineAddrCnt,
      rBusAddr(
        rBusAddr.high - cacheCfg.tagWidth - 1
        downto log2Up(cacheCfg.lineSizeBytes)
      ),
      rLineAddrCnt,
      //nextLineAddrCnt,
      U(log2Up(cacheCfg.wordSizeBytes) bits, default -> False),
    ).asUInt
  )
  //println(
  //  s"test: "
  //  + Cat(
  //    rLineAddrCnt,
  //    U(cacheCfg.wordSizeBytes bits, default -> False),
  //  ).getWidth
  //)
  println(
    s"tagWidth:${cacheCfg.tagWidth} "
    + s"tempLineBusAddr.getWidth:${tempLineBusAddr.getWidth} "
    + s"tempRdLineAttrsAddr.getWidth:${tempRdLineAttrsAddr.getWidth}"
  )
  def incrLineBusAddrCnts(): Unit = {
    nextLineAddrCnt := rLineAddrCnt + 1
    rRevLineAddrCnt := rRevLineAddrCnt - 1
  }
  def setLineBusAddrCntsToStart(): Unit = {
    nextLineAddrCnt := 0x0
    rRevLineAddrCnt := (
      ///*log2Up*/(io.tlCfg.beatMax) - 2
      (
        1
        << (
          log2Up(cacheCfg.lineSizeBytes) - log2Up(cacheCfg.wordSizeBytes)
        )
      ) - 1
    )
    //rRecvCnt := io.tlCfg.beatMax - 2
  }
  def atLastLineBusAddrCnt() = (
    //rLineAddrCnt.msb
    rRevLineAddrCnt.msb
  )
  //def atLastRecvCnt() = (
  //  rRecvCnt.msb
  //)
  def rBusSendData = (
    RegNextWhen(
      next=io.bus.sendData,
      cond=io.bus.nextValid,
      init=io.bus.sendData.getZero,
    )
  )
  def rBusAddr = (
    rBusSendData.addr
    ////if (isIcache) (
    //  RegNext(
    //    next=io.bus.sendData.addr,
    //    init=io.bus.sendData.addr.getZero
    //  )
    ////) else (
    //  io.bus.sendData.addr
    //)
  )
  def busDevData = (
    if (isIcache) (
      io.bus.recvData.instr
    ) else (
      io.bus.recvData.data
    )
  )
  if (!isIcache) {
    //io.bus.recvData.setAsReg()
    //io.bus.ready.setAsReg() init(False)
    //io.busExtraReady.setAsReg() //init(False)
    //io.busExtraReady.foreach(extraReady => {
    //  extraReady.init(extraReady.getZero)
    //})
    //io.busExtraReady.addAttribute(KeepAttribute.keep)
  }
  def doSetBusReadyEtc(
    someReady: Bool
  ): Unit = {
    io.bus.ready := someReady
    io.busExtraReady.foreach(extraReady => {
      extraReady := someReady
    })
  }
  //if (!isIcache) {
  //  busDevData := (
  //    RegNext(
  //      next=busDevData,
  //      init=busDevData.getZero,
  //    )
  //  )
  //}
  //doSetBusReadyEtc(
  //  someReady=False
  //)
  val myBusDevData = (
  //  !isIcache
  //) generate (
    /*Reg*/(
      cloneOf(busDevData),
      //init=busDevData.getZero
    )
  )
  //if (isIcache) {
    busDevData := (
      RegNext(
        next=busDevData,
        init=busDevData.getZero,
      )
    )
  //} else {
  //  //busDevData := rBusDevData
  //}
  object State extends SpinalEnum(
    defaultEncoding=(
      //binarySequential
      binaryOneHot
    )
  ) {
    val
      IDLE,
      //HANDLE_DCACHE_LOAD_HIT_EXTEND,
      HANDLE_DCACHE_STORE_HIT,
      HANDLE_SEND_LINE_TO_BUS_PIPE_1,
      HANDLE_SEND_LINE_TO_BUS,
      //HANDLE_RECV_LINE_FROM_BUS_PIPE_1,
      HANDLE_RECV_LINE_FROM_BUS,
      HANDLE_NON_CACHED_BUS_ACC
      = newElement()
  }
  val nextState = State()
  val rState = (
    RegNext(nextState)
    init(State.IDLE)
  )
  nextState := rState
  //--------
  //lineWordRam.io.rdEn := False
  //lineWordRam.io.rdAddr := (
  //  0x0
  //)
  lineWordRam.io.wrEn := False

  //lineAttrsRam.io.rdEn := False
  //lineAttrsRam.io.rdAddr := (
  //  0x0
  //)
  lineAttrsRam.io.wrEn := False

  val myLineRamAddrRshift = (
    log2Up(cacheCfg.wordSizeBytes)
  )
  val rdLineWord = (
    /*KeepAttribute*/(
      //UInt(lineWordRam.io.rdData.getWidth bits)
      UInt(cacheCfg.wordWidth bits)
    )
  )
  rdLineWord := lineWordRam.io.rdData.asUInt
  val wrLineAttrs = SnowHouseCacheLineAttrs(
    cfg=cfg,
    isIcache=isIcache,
  )
  wrLineAttrs.allowOverride
  //wrLineAttrs := wrLineAttrs.getZero
  wrLineAttrs := (
    RegNext(
      next=wrLineAttrs,
      init=wrLineAttrs.getZero
    )
  )
  doLineAttrsRamWrite(
    busAddr=(
      //RegNext(next=rBusAddr, init=rBusAddr.getZero)
      rBusAddr
    ),
    setEn=false,
  )
  val myBusSendDataData = (
    /*Reg*/(
      cloneOf(rBusSendData.data),
      //init=rBusSendData.data.getZero,
    )
  )
  myBusSendDataData.allowOverride
  doLineWordRamWrite(
    busAddr=(
      //RegNext(next=rBusAddr, init=rBusAddr.getZero)
      rBusAddr
    ),
    lineWord=myBusSendDataData,
    setEn=false,
  )

  def haveHit = (
    rdLineAttrs.fire
    //currLineValid
    && rdLineAttrs.tag === rBusAddrTag
  )

  def doAllLineRamsReadSync(
    busAddr: UInt,
  ): Unit = {
    doLineWordRamReadSync(busAddr=busAddr)
    doLineAttrsRamReadSync(busAddr=busAddr)
  }
  def doLineWordRamReadSync(
    busAddr: UInt,
  ): Unit = {
    lineWordRam.io.rdEn := True
    lineWordRam.io.rdAddr := (
      (busAddr >> myLineRamAddrRshift)
      .resize(lineWordRam.io.rdAddr.getWidth)
    )
  }
  def doLineWordRamWrite(
    busAddr: UInt,
    lineWord: UInt,
    setEn: Boolean=true,
  ): Unit = {
    if (setEn) {
      lineWordRam.io.wrEn := True
    }
    lineWordRam.io.wrAddr := (
      (busAddr >> myLineRamAddrRshift)
      .resize(lineWordRam.io.wrAddr.getWidth)
    )
    lineWordRam.io.wrData := lineWord.asBits
  }
  def doLineAttrsRamReadSync(
    busAddr: UInt,
  ): Unit = {
    lineAttrsRam.io.rdEn := True
    lineAttrsRam.io.rdAddr := (
      (busAddr >> log2Up(cacheCfg.lineSizeBytes))
      .resize(lineAttrsRam.io.rdAddr.getWidth)
    )
  }
  def doLineAttrsRamWrite(
    busAddr: UInt,
    lineAttrs: SnowHouseCacheLineAttrs=wrLineAttrs,
    setEn: Boolean=true,
  ): Unit = {
    if (setEn) {
      lineAttrsRam.io.wrEn := True
    }
    lineAttrsRam.io.wrAddr := (
      (busAddr >> log2Up(cacheCfg.lineSizeBytes))
      .resize(lineAttrsRam.io.wrAddr.getWidth)
    )
    lineAttrsRam.io.wrData := lineAttrs.asBits
  }
  //--------
  //io.bus.ready := False
  doSetBusReadyEtc(False)
  def rBusAddrIsNonCached = (
    (rBusAddr(cacheCfg.nonCachedRange) =/= 0x0)
    //init(False)
  )
  def rBusAddrTag = (
    (rBusAddr(cacheCfg.tagRange))
    //init(0x0)
  )
  val rSavedBusAddr = (
    Reg(cloneOf(rBusAddr), init=rBusAddr.getZero)
  )
  def rSavedBusAddrTag = (
    rSavedBusAddr(cacheCfg.tagRange)
  )

  //when (
  //  //nextState === State.IDLE
  //  //&&
  //  io.bus.nextValid
  //) {
    doAllLineRamsReadSync(busAddr=io.bus.sendData.addr)
    //doLineWordRamReadSync(busAddr=io.bus.sendData.addr)
    //doLineAttrsRamReadSync(busAddr=io.bus.sendData.addr)
  //}
  val rBusReadyCnt = (
    /*KeepAttribute*/(
      Reg(Bool(), init=False)
      //Reg(UInt(2 bits))
      //init(0x0)
    )
  )
  //val rTempBusReady = Reg(Bool(), init=False)
  val rSavedRdLineWord = Reg(cloneOf(rdLineWord), init=rdLineWord.getZero)
  //def doPipe(): Unit = {
  //}
  //val rSavedBusSendData = Reg(cloneOf(io.bus.sendData))
  val rPleaseFinish = (
    Vec.fill(3)(
      Vec.fill(3)(
        Reg(
          Bool(),
          init=False,
        )
      )
    )
  )
  when (rPleaseFinish(2).sFindFirst(_ === True)._1) {
    //io.bus.ready := True
    doSetBusReadyEtc(True)
    rPleaseFinish.foreach(myVec => {
      myVec.foreach(current => {
        current := False
      })
    })
  }
  //when (
  //  //io.bus.fire
  //  //(RegNext(io.bus.nextValid) init(False))
  //  //&& 
  //  io.bus.ready
  //) {
  //  rPleaseFinish.foreach(myVec => {
  //    myVec.foreach(current => {
  //      current := False
  //    })
  //  })
  //  //io.bus.ready := False
  //  //doSetBusReadyEtc(False)
  //}
  when (
    //rPleaseFinish(0).sFindFirst(_ === True)._1
    RegNext(next=io.bus.nextValid, init=False)
  ) {
    when (
      /*rSavedBusSendData*/rBusSendData.accKind.asBits(1)
    ) {
      busDevData := (
        /*rSavedBusSendData*/rBusSendData.data
        //RegNext(rBusSendData.data)
      )
    } otherwise {
      busDevData := myBusDevData
    }
  }

  //rBusDevData := rdLineWord
  myBusSendDataData := /*RegNext*/(rdLineWord)
  //when (!RegNext(rBusSendData.accKind).asBits(1)) {
    switch (
      //RegNext(next=rBusSendData.subKind, init=rBusSendData.subKind)
      rBusSendData.subKind
    ) {
      is (SnowHouseMemAccessSubKind.Sz8) {
        if (myBusDevData.getWidth > 8) {
          when (!/*RegNext*/(rBusSendData).accKind.asBits(0)) {
            myBusDevData := (
              rdLineWord(
                7 downto 0
              ).resize(myBusDevData.getWidth)
            )
          } otherwise {
            myBusDevData := (
              rdLineWord(
                7 downto 0
              ).asSInt.resize(myBusDevData.getWidth).asUInt
            )
          }
          //RegNext(rBusSendData).data
          myBusSendDataData(
            offset=/*RegNext*/(rBusSendData).addr(
              log2Up(cacheCfg.wordWidth) - 1 downto 0
            ),
            8 bits
          ) := (
            /*RegNext*/(rBusSendData).data(7 downto 0)
          )
        } else {
          myBusDevData := rdLineWord.resized
          myBusSendDataData := /*RegNext*/(rBusSendData).data.resized
        }
      }
      is (SnowHouseMemAccessSubKind.Sz16) {
        if (myBusDevData.getWidth > 16) {
          when (!RegNext(rBusSendData).accKind.asBits(0)) {
            myBusDevData := (
              rdLineWord(
                15 downto 0
              ).resize(myBusDevData.getWidth)
            )
          } otherwise {
            myBusDevData := (
              rdLineWord(
                15 downto 0
              ).asSInt.resize(myBusDevData.getWidth).asUInt
            )
          }
          myBusSendDataData(
            offset=/*RegNext*/(rBusSendData).addr(
              log2Up(cacheCfg.wordWidth) - 1 downto 1
            ),
            16 bits
          ) := (
            /*RegNext*/(rBusSendData).data(15 downto 0)
          )
        } else {
          myBusDevData := rdLineWord.resized
          myBusSendDataData := /*RegNext*/(rBusSendData).data.resized
        }
      }
      is (SnowHouseMemAccessSubKind.Sz32) {
        if (myBusDevData.getWidth > 32) {
          when (!RegNext(rBusSendData).accKind.asBits(0)) {
            myBusDevData := (
              rdLineWord(
                31 downto 0
              ).resize(myBusDevData.getWidth)
            )
          } otherwise {
            myBusDevData := (
              rdLineWord(
                31 downto 0
              ).asSInt.resize(myBusDevData.getWidth).asUInt
            )
          }
          myBusSendDataData(
            offset=/*RegNext*/(rBusSendData).addr(
              log2Up(cacheCfg.wordWidth) - 1 downto 2
            ),
            32 bits
          ) := (
            /*RegNext*/(rBusSendData).data(31 downto 0)
          )
        } else {
          myBusDevData := rdLineWord.resized
          myBusSendDataData := /*RegNext*/(rBusSendData).data.resized
        }
      }
      is (SnowHouseMemAccessSubKind.Sz64) {
        if (myBusDevData.getWidth > 64) {
          when (!RegNext(rBusSendData).accKind.asBits(0)) {
            myBusDevData := (
              rdLineWord(
                63 downto 0
              ).resize(myBusDevData.getWidth)
            )
          } otherwise {
            myBusDevData := (
              rdLineWord(
                63 downto 0
              ).asSInt.resize(myBusDevData.getWidth).asUInt
            )
          }
          myBusSendDataData(
            offset=/*RegNext*/(rBusSendData).addr(
              log2Up(cacheCfg.wordWidth) - 1 downto 3
            ),
            64 bits
          ) := (
            /*RegNext*/(rBusSendData).data(63 downto 0)
          )
        } else {
          myBusDevData := rdLineWord.resized
          myBusSendDataData := /*RegNext*/(rBusSendData).data.resized
        }
      }
    }
  //} //otherwise 
  switch (rState) {
    is (State.IDLE) {
      when (
        (RegNext(io.bus.nextValid) init(False))
        && !rPleaseFinish(1).sFindFirst(_ === True)._1
      ) {
        nextBridgeSavedFires := 0x0
        setLineBusAddrCntsToStart()
        myH2dBus.nextValid := False
        rSavedBusAddr := rBusAddr
        rSavedRdLineAttrs := rdLineAttrs
        rSavedRdLineWord := rdLineWord
        //rSavedBusSendData := rBusSendData
        when (RegNext(io.bus.nextValid) init(False)) {
          when (if (isIcache) (True) else (!rBusAddrIsNonCached)) {
            when (
              if (isIcache) (
                True
              ) else (
                !rBusSendData.accKind.asBits(1)
              )
            ) {
              when (haveHit) {
                // cached load
                //if (isIcache) {
                  //io.bus.ready := True
                  doSetBusReadyEtc(True)
                  busDevData := (
                    rdLineWord.resize(busDevData.getWidth)
                  )
                //} else {
                //  rPleaseFinish.foreach(current => {
                //    current(0) := True
                //  })
                //}
              } otherwise {
                // cache miss upon a load
                when (if (isIcache) (False) else (rdLineAttrs.dirty)) {
                  nextState := State.HANDLE_SEND_LINE_TO_BUS_PIPE_1
                } otherwise {
                  nextState := State.HANDLE_RECV_LINE_FROM_BUS
                }
              //println(
              //  io.tlCfg.beatMax
              //)
              }
            } otherwise {
              if (!isIcache) {
                //when (RegNext(rState) === State.HANDLE_DCACHE_STORE_HIT) {
                //  rBusReadyCnt := False
                //  io.bus.ready := True
                //} otherwise {
                  when (haveHit) {
                    // cached store
                    nextState := State.HANDLE_DCACHE_STORE_HIT
                    lineAttrsRam.io.rdEn := False
                    lineWordRam.io.rdEn := False
                    rBusReadyCnt := True
                    wrLineAttrs := rdLineAttrs
                    wrLineAttrs.dirty := True
                    rPleaseFinish.foreach(current => {
                      current(1) := True
                    })
                    lineAttrsRam.io.wrEn := True
                    lineWordRam.io.wrEn := True
                  } otherwise {
                    // cache miss upon a store
                    when (if (isIcache) (False) else (rdLineAttrs.dirty)) {
                      nextState := State.HANDLE_SEND_LINE_TO_BUS_PIPE_1
                      //doLineWordRamReadSync(
                      //  busAddr=(
                      //    //tempLineBusAddr
                      //    tempRdLineAttrsAddr2
                      //  )
                      //)
                    } otherwise {
                      nextState := State.HANDLE_RECV_LINE_FROM_BUS
                      //rH2dSendData.isWrite := False
                    }
                  }
                //}
              }
            }
          } otherwise {
            if (!isIcache) {
              // non-cached access to the bus
              nextState := State.HANDLE_NON_CACHED_BUS_ACC
              myH2dBus.nextValid := True
              rH2dSendData.isWrite := rBusSendData.accKind.asBits(1)
              rH2dSendData.addr := (
                rBusAddr.resize(rH2dSendData.addr.getWidth)
              )
              rH2dSendData.data := rBusSendData.data
              rH2dSendData.size := 1
              rH2dSendData.mask := (
                U(rH2dSendData.mask.getWidth bits, default -> True)
              )
              //--------
            }
          }
        }
      }
    }
    //is (State.HANDLE_DCACHE_LOAD_HIT) {
    //  nextState := State.IDLE
    //  rPleaseFinish := True
    //  if (!isIcache) {
    //    rBusDevData := (
    //      rSavedRdLineWord
    //      //RegNext(
    //      //  next=rSavedRdLineWord,
    //      //  init=rSavedRdLineWord.getZero,
    //      //).resize(busDevData.getWidth)
    //    )
    //  }
    //}
    is (State.HANDLE_DCACHE_STORE_HIT) {
      nextState := State.IDLE
      //rPleaseFinish(1) := True
      //rPleaseFinish.foreach(current => {
      //  current(1) := True
      //})
      lineAttrsRam.io.wrEn := False
      lineWordRam.io.wrEn := False
      //doLineAttrsRamWrite(
      //  busAddr=RegNext(rBusAddr)
      //)
      //doLineWordRamWrite(
      //  busAddr=RegNext(rBusAddr),
      //  lineWord=RegNext(rBusSendData.data),
      //)
    }
    is (State.HANDLE_SEND_LINE_TO_BUS_PIPE_1) {
      nextState := State.HANDLE_SEND_LINE_TO_BUS 
      doLineWordRamReadSync(
        busAddr=(
          //tempLineBusAddr
          RegNext(
            next=tempRdLineAttrsAddr2,
            init=tempRdLineAttrsAddr2.getZero
          )
        )
      )
    }
    is (State.HANDLE_SEND_LINE_TO_BUS) {
      //handleWriteLineRam(
      //  isStoreHit=false
      //)
      rH2dSendData.isWrite := True
      //rH2dSendData.data := (
      //  0x0
      //  ////rBusSendData.data
      //  //rdLineWord
      //  //// since this can be anything (per the Tilelink spec)
      //  //// for an opcode of `GET`, we can put the data read
      //  //// from `lineRam` here every time
      //)
      rH2dSendData.size := (
        //log2Up(io.tlCfg.beatMax)
        log2Up(cacheCfg.lineSizeBytes) //- log2Up(cacheCfg.wordSizeBytes)
      )
      rH2dSendData.src := cacheCfg.srcId
      rH2dSendData.data := rdLineWord
      rH2dSendData.mask := (
        U(rH2dSendData.mask.getWidth bits, default -> True)
      )
      val tempBridgeSavedFires = Bool()
      tempBridgeSavedFires := False
      doLineWordRamReadSync(
        busAddr=(
          //tempLineBusAddr
          tempRdLineAttrsAddr
        )
      )
      rH2dSendData.addr := (
        //Cat(
        //  rBusAddr >> log2Up(cacheCfg.lineSizeBytes),
        //  U(s"${log2Up(cacheCfg.lineSizeBytes)}'d0"),
        //).asUInt
        //tempLineBusAddr.resize(rH2dSendData.addr.getWidth)
        tempRdLineAttrsAddr1.resize(rH2dSendData.addr.getWidth)
      )
      when (
        !RegNext(myH2dBus.nextValid)
        && !rBridgeSavedFires(0)
      ) {
        myH2dBus.nextValid := True
        //setLineBusAddrCntsToStart()
        incrLineBusAddrCnts()
        //setLineBusAddrToFirst()
        //rRecvCnt := 0x0
      } elsewhen (
        //myH2dBus.fire
        RegNext(myH2dBus.nextValid)
        && myH2dBus.ready
      ) {
        //rH2dSendData.addr := (
        //  //Cat(
        //  //  rBusAddr >> log2Up(cacheCfg.lineSizeBytes),
        //  //  U(s"${log2Up(cacheCfg.lineSizeBytes)}'d0"),
        //  //).asUInt
        //  //tempLineBusAddr.resize(rH2dSendData.addr.getWidth)
        //  tempRdLineAttrsAddr.resize(rH2dSendData.addr.getWidth)
        //)
        //doLineWordRamReadSync(
        //  busAddr=(
        //    //tempLineBusAddr
        //    tempRdLineAttrsAddr
        //  )
        //)
        //rH2dSendData.addr := (
        //  //Cat(
        //  //  rBusAddr >> log2Up(cacheCfg.lineSizeBytes),
        //  //  U(s"${log2Up(cacheCfg.lineSizeBytes)}'d0"),
        //  //).asUInt
        //  //tempLineBusAddr.resize(rH2dSendData.addr.getWidth)
        //  tempRdLineAttrsAddr.resize(rH2dSendData.addr.getWidth)
        //)
        when (!atLastLineBusAddrCnt) {
          incrLineBusAddrCnts()
        } otherwise {
          myH2dBus.nextValid := False
          nextBridgeSavedFires(0) := True
          tempBridgeSavedFires := True
        }
        //rH2dSendData.addr := (
        //  tempLineBusAddr.resize(rH2dSendData.addr.getWidth)
        //)
      }
      when (
        RegNext(myD2hBus.nextValid)
        && (
          //nextBridgeSavedFires(0)
          tempBridgeSavedFires
          || rBridgeSavedFires(0)
        )
      ) {
        myD2hBus.ready := True
        setLineBusAddrCntsToStart()
        nextBridgeSavedFires := 0x0
        nextState := State.HANDLE_RECV_LINE_FROM_BUS
      }
    }
    is (State.HANDLE_RECV_LINE_FROM_BUS) {
      rH2dSendData.isWrite := False
      rH2dSendData.size := (
        //log2Up(io.tlCfg.beatMax)
        log2Up(cacheCfg.lineSizeBytes) //- log2Up(cacheCfg.wordSizeBytes)
      )
      rH2dSendData.src := cacheCfg.srcId
      rH2dSendData.mask := (
        U(rH2dSendData.mask.getWidth bits, default -> True)
      )
      rH2dSendData.addr := (
        //Cat(
        //  rBusAddr >> log2Up(cacheCfg.lineSizeBytes),
        //  U(s"${log2Up(cacheCfg.lineSizeBytes)}'d0"),
        //).asUInt
        tempLineBusAddr.resize(rH2dSendData.addr.getWidth)
      )
      //when (
      //  RegNext(rState) === State.HANDLE_SEND_LINE_TO_BUS
      //) {
      //  nextBridgeSavedFires := 0x0
      //}
      when (
        !RegNext(myH2dBus.nextValid)
        && !rBridgeSavedFires(1)
      ) {
        myH2dBus.nextValid := True
        setLineBusAddrCntsToStart()
        wrLineAttrs.tag := (
          rSavedBusAddrTag
        )
        //wrLineAttrs.valid := True
        if (!isIcache) {
          wrLineAttrs.dirty := False
        }
        doLineAttrsRamWrite(
          busAddr=tempLineBusAddr,
        )
      }
      when (
        //myH2dBus.fire
        RegNext(next=myH2dBus.nextValid, init=False)
        && myH2dBus.ready
      ) {
        myH2dBus.nextValid := False
        nextBridgeSavedFires(1) := True
        //incrLineBusAddrCnts()
        //doLineWordRamWrite(
        //  busAddr=(
        //    tempLineBusAddr
        //    //myD2hBus.sendData.addr
        //  ),
        //  lineWord=myD2hBus.sendData.data,
        //)
      }
      when (
        RegNext(next=myD2hBus.nextValid, init=False)
        && (
          //nextBridgeSavedFires(1)
          //|| 
          rBridgeSavedFires(1)
        )
      ) {
        when (!atLastLineBusAddrCnt()) {
          incrLineBusAddrCnts()
        } otherwise {
          setLineBusAddrCntsToStart()
          nextState := State.IDLE
        }
        doLineWordRamWrite(
          busAddr=(
            tempLineBusAddr
            //myD2hBus.sendData.addr
          ),
          lineWord=myD2hBus.sendData.data,
        )
        myD2hBus.ready := True
      }
      //when (myD2hBus.rValid) {
      //  doLineWordRamWrite(
      //    busAddr=tempLineBusAddr,
      //    lineWord=myD2hBus.sendData.data,
      //  )
      //}
    }
    //--------
    //--------
    is (State.HANDLE_NON_CACHED_BUS_ACC) {
      when (
        //myH2dBus.fire
        RegNext(next=myH2dBus.nextValid, init=False)
        && myH2dBus.ready
      ) {
        myH2dBus.nextValid := False
        nextBridgeSavedFires(0) := True
      }
      when (
        RegNext(myD2hBus.nextValid)
        && (
          nextBridgeSavedFires(0)
          || rBridgeSavedFires(0)
        )
      ) {
        myD2hBus.ready := True
        if (!isIcache) {
          //busDevData := myD2hBus.sendData.data
          myBusDevData := myD2hBus.sendData.data
        }
        nextState := (
          State.IDLE
          //State.HANDLE_DCACHE_LOAD_HIT
        )
        //rPleaseFinish(2) := True
        rPleaseFinish.foreach(current => {
          current(2) := True
        })
        //nextBridgeSavedFires(1) := True
        //io.bus.ready := True
      }
      //setLineBusAddrCntsToStart()
    }
  }
  wrLineAttrs.valid := True
}
