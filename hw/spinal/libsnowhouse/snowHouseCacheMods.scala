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
  val haveHazard = (
    !isIcache
  ) generate (
    in(Bool())
  )
  val bus = (
    slave(
      new LcvStallIo[BusHostPayload, BusDevPayload](
        sendPayloadType=Some(BusHostPayload(cfg=cfg, isIbus=isIcache)),
        recvPayloadType=Some(BusDevPayload(cfg=cfg, isIbus=isIcache)),
      )
    )
  )
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
  val lineWordRam = FpgacpuRamSimpleDualPort(
    wordType=UInt(cacheCfg.wordWidth bits),
    depth=depthWords,
    initBigInt=Some(Array.fill(depthWords)(BigInt(0))),
  )
  val lineAttrsRam = FpgacpuRamSimpleDualPort(
    wordType=SnowHouseCacheLineAttrs(
      cfg=cfg,
      isIcache=isIcache,
    ),
    depth=depthLines,
    initBigInt=Some(Array.fill(depthLines)(BigInt(0))),
  )
  val tempLineBusAddr = (
    KeepAttribute(cloneOf(rBusAddr))
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
    KeepAttribute(
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
    KeepAttribute(
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
      //rSavedRdLineAttrs.tag,
      //U(log2Up(cacheCfg.lineSizeBytes) bits, default -> False),
      //rLineAddrCnt(rLineAddrCnt.high - 1 downto 0),
      //nextLineAddrCnt,
      rBusAddr(
        rBusAddr.high - cacheCfg.tagWidth - 1
        downto log2Up(cacheCfg.lineSizeBytes)
      ),
      //rLineAddrCnt,
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
  //val rRecvCnt = cloneOf(rRevLineAddrCnt)
  def incrLineBusAddrCnts(): Unit = {
    nextLineAddrCnt := rLineAddrCnt + 1
    rRevLineAddrCnt := rRevLineAddrCnt - 1
  }
  //def decrRecvCnt(): Unit = {
  //  rRecvCnt := rRecvCnt - 1
  //}
  //println(
  //  io.tlCfg.beatMax
  //)
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
  busDevData := (
    RegNext(
      next=busDevData,
      init=busDevData.getZero,
    )
  )
  object State extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      HANDLE_SEND_LINE_TO_BUS,
      HANDLE_RECV_LINE_FROM_BUS,
      //HANDLE_STORE_HIT,
      //HANDLE_STORE_MISS_RD,
      //HANDLE_STORE_MISS_WR,
      HANDLE_NON_CACHED_BUS_ACC
      //HANDLE_NON_CACHED_STORE,
      //YIELD_RESULT
      = newElement()
  }
  val nextState = State()
  val rState = (
    RegNext(nextState)
    init(State.IDLE)
  )
  nextState := rState
  //val tempLineRamRdAddr = cloneOf(lineWordRam.io.rdAddr)
  //tempLineRamRdAddr := (
  //  (io.bus.sendData.addr >> myLineRamAddrRshift)
  //  .resize(
  //    width=tempLineRamRdAddr.getWidth
  //  )
  //  //(lineRam.io.rdAddr.bitsRange)
  //)
  //lineWordRam.io.rdAddr := (
  //  tempLineRamRdAddr
  //)
  //--------
  //val tempLineRamWrAddr = cloneOf(lineWordRam.io.rdAddr)
  //tempLineRamWrAddr := (
  //  (rBusAddr >> myLineRamAddrRshift)
  //  .resize(
  //    width=tempLineRamWrAddr.getWidth
  //  )
  //  //(lineRam.io.rdAddr.bitsRange)
  //)
  //tempLineRamWrAddr := (
  //  RegNext(
  //    next=tempLineRamWrAddr,
  //    init=tempLineRamWrAddr.getZero,
  //  )
  //)
  //lineWordRam.io.rdEn := io.bus.nextValid//io.bus.nextValid

  lineWordRam.io.rdEn := False
  lineWordRam.io.rdAddr := 0x0
  lineWordRam.io.wrEn := False
  lineWordRam.io.wrAddr := 0x0
  lineWordRam.io.wrData := lineWordRam.io.wrData.getZero

  lineAttrsRam.io.rdEn := False
  lineAttrsRam.io.rdAddr := 0x0
  lineAttrsRam.io.wrEn := False
  lineAttrsRam.io.wrAddr := 0x0
  lineAttrsRam.io.wrData := lineAttrsRam.io.wrData.getZero

  val myLineRamAddrRshift = (
    log2Up(cacheCfg.wordSizeBytes)
  )
  //lineWordRam.io.wrEn := False
  //lineWordRam.io.wrAddr := (
  //  //tempLineRamWrAddr
  //  RegNext(
  //    next=lineWordRam.io.wrAddr,
  //    init=lineWordRam.io.wrAddr.getZero,
  //  )
  //)
  //lineWordRam.io.wrData := (
  //  RegNext(
  //    next=lineWordRam.io.wrData,
  //    init=lineWordRam.io.wrData.getZero,
  //  )
  //)
  val rdLineWord = (
    KeepAttribute(
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

  //val lineAttrsValidMem = (
  //  //Vec.fill(cacheCfg.depthLines)(
  //  //  Reg(
  //  //    Bool(),
  //  //    init=False
  //  //  )
  //  //)
  //  Mem(
  //    wordType=Bool(),
  //    wordCount=cacheCfg.depthLines
  //  )
  //  .initBigInt(Array.fill(cacheCfg.depthLines)(BigInt(0)))
  //)
  //val lineAttrsValidMemWrEnable = Bool()
  //val lineAttrsValidMemWrData = Bool()
  //lineAttrsValidMemWrData := True
  //lineAttrsValidMemWrEnable := False

  //lineAttrsValidMem.write(
  //  address=lineAttrsRam.io.wrAddr,
  //  data=lineAttrsValidMemWrData,
  //  enable=lineAttrsValidMemWrEnable,
  //)


  //val currLineValid = (
  //  //lineAttrsValidMem.readSync(
  //  //  address=lineAttrsRam.io.rdAddr
  //  //)
  //  //Bool()
  //)
  def haveHit = (
    rdLineAttrs.fire
    //currLineValid
    && rdLineAttrs.tag === rBusAddrTag
  )


  //val wrLineWord = UInt(cacheCfg.wordWidth bits)
  //wrLineWord := (
  //  RegNext(
  //    next=wrLineWord,
  //    init=wrLineWord.getZero,
  //  )
  //)
  //rdLineWord := lineWordRam.io.rdData.asUInt

  //when (io.bus.nextValid) {
  //  doPrepLineWordRamRead(
  //    busAddr=io.bus.sendData.addr
  //  )
  //}

  //UInt(cacheCfg.wordWidth bits)
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
  ): Unit = {
    lineWordRam.io.wrEn := True
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
  ): Unit = {
    lineAttrsRam.io.wrEn := True
    lineAttrsRam.io.wrAddr := (
      (busAddr >> log2Up(cacheCfg.lineSizeBytes))
      .resize(lineAttrsRam.io.wrAddr.getWidth)
    )
    lineAttrsRam.io.wrData := lineAttrs.asBits
  }
  //--------
  io.bus.ready := False
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

  when (
    //nextState === State.IDLE
    //&&
    io.bus.nextValid
  ) {
    doAllLineRamsReadSync(busAddr=io.bus.sendData.addr)
    //doLineWordRamReadSync(busAddr=io.bus.sendData.addr)
    //doLineAttrsRamReadSync(busAddr=io.bus.sendData.addr)
  }
  val rBusReadyCnt = (
    KeepAttribute(
      Reg(Bool(), init=False)
      //Reg(UInt(2 bits))
      //init(0x0)
    )
  )
  val rTempBusReady = Reg(Bool(), init=False)
  switch (rState) {
    is (State.IDLE) {
      nextBridgeSavedFires := 0x0
      setLineBusAddrCntsToStart()
      myH2dBus.nextValid := False
      rSavedBusAddr := rBusAddr
      rSavedRdLineAttrs := rdLineAttrs
      //val rBusReadyCnt = (
      //  Reg(UInt(1 bits)) init(0x0)
      //)
      rTempBusReady := False

      when (io.bus.rValid) {
        //rSavedBusHostData := io.bus.sendData
        //rLineAddrCnt := 0x0
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
              //when (!io.haveHazard) {
              //  io.bus.ready := True
              //  busDevData := (
              //    rdLineWord
              //    //(
              //    //  31 downto 0
              //    //)
              //    .resize(busDevData.getWidth)
              //  )
              //} otherwise {
                io.bus.ready := rTempBusReady
                rTempBusReady := True
                busDevData := (
                  RegNext(
                    next=rdLineWord,
                    init=rdLineWord.getZero,
                  ).resize(busDevData.getWidth)
                )
              //}
              //switch (rBusSendData.subKind) {
              //  is (SnowHouseMemAccessSubKind.Sz8) {
              //    if (busDevData.getWidth >= 8) {
              //      when (!rBusSendData.accKind.asBits(0)) {
              //        busDevData := (
              //          rdLineWord(
              //            7 downto 0
              //          ).resize(busDevData.getWidth)
              //        )
              //      } otherwise {
              //        busDevData := (
              //          rdLineWord(
              //            7 downto 0
              //          ).asSInt.resize(busDevData.getWidth).asUInt
              //        )
              //      }
              //    } else {
              //      busDevData := rdLineWord.resized
              //    }
              //  }
              //  is (SnowHouseMemAccessSubKind.Sz16) {
              //    if (busDevData.getWidth >= 16) {
              //      when (!rBusSendData.accKind.asBits(0)) {
              //        busDevData := (
              //          rdLineWord(
              //            15 downto 0
              //          ).resize(busDevData.getWidth)
              //        )
              //      } otherwise {
              //        busDevData := (
              //          rdLineWord(
              //            15 downto 0
              //          ).asSInt.resize(busDevData.getWidth).asUInt
              //        )
              //      }
              //    } else {
              //      busDevData := rdLineWord.resized
              //    }
              //  }
              //  is (SnowHouseMemAccessSubKind.Sz32) {
              //    if (busDevData.getWidth >= 32) {
              //      when (!rBusSendData.accKind.asBits(0)) {
              //        busDevData := (
              //          rdLineWord(
              //            31 downto 0
              //          ).resize(busDevData.getWidth)
              //        )
              //      } otherwise {
              //        busDevData := (
              //          rdLineWord(
              //            31 downto 0
              //          ).asSInt.resize(busDevData.getWidth).asUInt
              //        )
              //      }
              //    } else {
              //      busDevData := rdLineWord.resized
              //    }
              //  }
              //  is (SnowHouseMemAccessSubKind.Sz64) {
              //    if (busDevData.getWidth >= 64) {
              //      when (!rBusSendData.accKind.asBits(0)) {
              //        busDevData := (
              //          rdLineWord(
              //            63 downto 0
              //          ).resize(busDevData.getWidth)
              //        )
              //      } otherwise {
              //        busDevData := (
              //          rdLineWord(
              //            63 downto 0
              //          ).asSInt.resize(busDevData.getWidth).asUInt
              //        )
              //      }
              //    } else {
              //      busDevData := rdLineWord.resized
              //    }
              //  }
              //}
            } otherwise {
              // cache miss upon a load
              when (if (isIcache) (False) else (rdLineAttrs.dirty)) {
                nextState := State.HANDLE_SEND_LINE_TO_BUS
                doLineWordRamReadSync(
                  busAddr=(
                    //tempLineBusAddr
                    tempRdLineAttrsAddr2
                  )
                )
              } otherwise {
                nextState := State.HANDLE_RECV_LINE_FROM_BUS
              }
              //println(
              //  io.tlCfg.beatMax
              //)
            }
          } otherwise {
            if (!isIcache) {
              when (haveHit) {
                // cached store
                //nextState := State.HANDLE_STORE_HIT
                when (
                  //rBusReadyCnt === 0
                  !rBusReadyCnt
                ) {
                  lineAttrsRam.io.rdEn := False
                  lineWordRam.io.rdEn := False
                  //rBusReadyCnt := rBusReadyCnt - 1
                  rBusReadyCnt := True
                  wrLineAttrs := rdLineAttrs
                  wrLineAttrs.dirty := True
                  doLineAttrsRamWrite(
                    busAddr=rBusAddr
                  )
                  doLineWordRamWrite(
                    busAddr=rBusAddr,
                    lineWord=rBusSendData.data,
                  )
                } otherwise {
                  //lineAttrsRam.io.rdEn := False
                  //lineWordRam.io.rdEn := False
                  rBusReadyCnt := False
                  io.bus.ready := True
                }
              } otherwise {
                // cache miss upon a store
                when (if (isIcache) (False) else (rdLineAttrs.dirty)) {
                  nextState := State.HANDLE_SEND_LINE_TO_BUS
                  doLineWordRamReadSync(
                    busAddr=(
                      //tempLineBusAddr
                      tempRdLineAttrsAddr2
                    )
                  )
                } otherwise {
                  nextState := State.HANDLE_RECV_LINE_FROM_BUS
                  //rH2dSendData.isWrite := False
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
        !myH2dBus.rValid
        && !rBridgeSavedFires(0)
      ) {
        myH2dBus.nextValid := True
        //setLineBusAddrCntsToStart()
        incrLineBusAddrCnts()
        //setLineBusAddrToFirst()
        //rRecvCnt := 0x0
      } elsewhen (myH2dBus.fire) {
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
        myD2hBus.rValid
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
        !myH2dBus.rValid
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
      when (myH2dBus.fire) {
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
        myD2hBus.rValid
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
    //is (State.HANDLE_STORE_HIT) {
    //  nextState := State.IDLE
    //  //setLineBusAddrCntsToStart()
    //  io.bus.ready := True
    //  //// NOTE: this makes this cache be of the "write-through" variety.
    //  //// TODO: I will need to implement something better later!
    //  //when (myH2dBus.fire) {
    //  //  myH2dBus.nextValid := False
    //  //  nextBridgeSavedFires(0) := True
    //  //}
    //  //when (
    //  //  myD2hBus.rValid
    //  //  && (
    //  //    nextBridgeSavedFires(0)
    //  //    || rBridgeSavedFires(0)
    //  //  )
    //  //) {
    //  //  myD2hBus.ready := True
    //  //  busDevData := myD2hBus.sendData.data
    //  //  nextState := State.IDLE
    //  //  //nextBridgeSavedFires(1) := True
    //  //  io.bus.ready := True
    //  //}
    //}
    //--------
    //is (State.HANDLE_STORE_MISS_RD) {
    //  if (!isIcache) {
    //  }
    //}
    //is (State.HANDLE_STORE_MISS_WR) {
    //  if (!isIcache) {
    //  }
    //}
    is (State.HANDLE_NON_CACHED_BUS_ACC) {
      when (myH2dBus.fire) {
        myH2dBus.nextValid := False
        nextBridgeSavedFires(0) := True
      }
      when (
        myD2hBus.rValid
        && (
          nextBridgeSavedFires(0)
          || rBridgeSavedFires(0)
        )
      ) {
        myD2hBus.ready := True
        busDevData := myD2hBus.sendData.data
        nextState := State.IDLE
        //nextBridgeSavedFires(1) := True
        io.bus.ready := True
      }
      //setLineBusAddrCntsToStart()
    }
    //is (State.HANDLE_NON_CACHED_STORE) {
    //  if (!isIcache) {
    //  }
    //}
    //is (State.YIELD_RESULT) {
    //  //when (io.bus.rValid) {
    //  //  io.bus.ready := True
    //  //}
    //}
  }
  wrLineAttrs.valid := True
}
