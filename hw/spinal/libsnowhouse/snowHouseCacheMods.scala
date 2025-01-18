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
  //val valid = Bool()
  //def fire = valid
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
      log2Up(cacheCfg.lineSizeBytes) - log2Up(cacheCfg.wordSizeBytes)
      bits
    )
  )
  val rLineAddrCnt = (
    KeepAttribute(
      RegNextWhen(
        next=nextLineAddrCnt,
        cond=io.tlBus.a.fire,
        init=nextLineAddrCnt.getZero,
      )
    )
  )
  nextLineAddrCnt := rLineAddrCnt
  tempLineBusAddr := (
    Cat(
      rBusAddr(rBusAddr.high downto log2Up(cacheCfg.lineSizeBytes)),
      //U(log2Up(cacheCfg.lineSizeBytes) bits, default -> False),
      rLineAddrCnt,
      U(log2Up(cacheCfg.wordSizeBytes) bits, default -> False),
    ).asUInt
  )
  val nextBusSource = (
    UInt(io.tlBus.a.source.getWidth bits)
  )
  val rBusSource = (
    KeepAttribute(
      RegNextWhen(
        next=nextBusSource,
        cond=io.tlBus.a.fire,
        init=nextBusSource.getZero,
      )
    )
  )
  nextBusSource := rBusSource
  def busNextValid = (
    //if (isIcache) (
      io.bus.nextValid
    //) else (
    //  io.bus.nextValid
    //)
  )
  def busRvalid = (
    io.bus.rValid
  )
  def busReady = (
    io.bus.ready
  )
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
  //val busLineAddr = cloneOf(busAddr)
  //val nextTempBusAddr = (
  //  cloneOf(
  //    if (isIcache) (
  //      io.ibus.sendData.addr
  //    ) else (
  //      io.dbus.sendData.addr
  //    )
  //  )
  //)
  //val rTempBusAddr = (
  //  RegNext(
  //    next=nextTempBusAddr,
  //    init=nextTempBusAddr.getZero,
  //  )
  //)
  //nextTempBusAddr := rTempBusAddr
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
      HANDLE_WR_LINE_TO_BUS,
      HANDLE_RD_LINES_FROM_BUS,
      HANDLE_STORE_HIT,
      //HANDLE_STORE_MISS_RD,
      //HANDLE_STORE_MISS_WR,
      HANDLE_NON_CACHED_BUS_ACC,
      //HANDLE_NON_CACHED_STORE,
      YIELD_RESULT
      = newElement()
  }
  val nextState = State()
  val rState = (
    RegNext(nextState)
    init(State.IDLE)
  )
  nextState := rState
  lineWordRam.io.rdEn := busNextValid
  val myLineRamAddrRshift = (
    log2Up(cacheCfg.wordSizeBytes)
  )
  val tempLineRamRdAddr = cloneOf(lineWordRam.io.rdAddr)
  tempLineRamRdAddr := (
    (io.bus.sendData.addr >> myLineRamAddrRshift)
    .resize(
      width=tempLineRamRdAddr.getWidth
    )
    //(lineRam.io.rdAddr.bitsRange)
  )
  lineWordRam.io.rdAddr := (
    tempLineRamRdAddr
  )
  val tempLineRamWrAddr = cloneOf(lineWordRam.io.rdAddr)
  tempLineRamWrAddr := (
    (rBusAddr >> myLineRamAddrRshift)
    .resize(
      width=tempLineRamWrAddr.getWidth
    )
    //(lineRam.io.rdAddr.bitsRange)
  )
  lineWordRam.io.wrEn := False
  lineWordRam.io.wrAddr := tempLineRamWrAddr

  lineAttrsRam.io.rdEn := busNextValid
  lineAttrsRam.io.rdAddr := (
    (io.bus.sendData.addr >> log2Up(cacheCfg.lineSizeBytes))
    .resize(
      width=lineAttrsRam.io.rdAddr.getWidth
    )
    //(lineAttrsRam.io.rdAddr.bitsRange)
  )
  lineAttrsRam.io.wrEn := False
  lineAttrsRam.io.wrAddr := (
    (rBusAddr >> log2Up(cacheCfg.lineSizeBytes))
    .resize(
      width=lineAttrsRam.io.wrAddr.getWidth
    )
  )

  //// these aren't real bursts; they will instead pseudo-pipeline accesses 
  //// to the TileLink bus
  //val rWrFakeBurstCnt = (
  //  Reg(SInt(log2Up(cacheCfg.numWordsPerLine) + 1 bits))
  //  init(cacheCfg.numWordsPerLine - 2)
  //)
  //val rRdFakeBurstCnt = (
  //  Reg(SInt(log2Up(cacheCfg.numWordsPerLine) + 1 bits))
  //  init(cacheCfg.numWordsPerLine - 2)
  //)
  //--------
  val rdLineWord = UInt(cacheCfg.wordWidth bits)
  val wrLineWord = UInt(cacheCfg.wordWidth bits)
  wrLineWord := (
    RegNext(
      next=wrLineWord,
      init=wrLineWord.getZero,
    )
  )
  val rdLineAttrs = SnowHouseCacheLineAttrs(
    cfg=cfg,
    isIcache=isIcache,
  )
  val wrLineAttrs = SnowHouseCacheLineAttrs(
    cfg=cfg,
    isIcache=isIcache,
  )
  rdLineWord := lineWordRam.io.rdData.asUInt
  rdLineAttrs.assignFromBits(lineAttrsRam.io.rdData)

  lineWordRam.io.wrData := wrLineWord.asBits
  lineAttrsRam.io.wrData := wrLineAttrs.asBits
  //def lineAttrsTag = (
  //  lineAttrsRam.io.rdData
  //)
  //val rTempHostAddr = Reg(cloneOf(busAddr)) init(busAddr.getZero)
  //val rSavedBusHostData = (
  //  Reg(cloneOf(io.bus.sendData))
  //  init(io.bus.sendData.getZero)
  //)
  //--------
  busReady := False
  //io.tlBus.a.valid.setAsReg
  //io.tlBus.a.valid.init(io.tlBus.a.valid.getZero)
  //io.tlBus.a.payload.setAsReg
  //io.tlBus.a.payload.init(io.tlBus.a.payload.getZero)
  //io.tlBus.d.ready := False
  def rBusAddrIsNonCached = (
    (rBusAddr(cacheCfg.nonCachedRange) =/= 0x0)
    //init(False)
  )
  def rBusAddrTag = (
    (rBusAddr(cacheCfg.tagRange))
    //init(0x0)
  )
  val lineAttrsValidMem = (
    //Vec.fill(cacheCfg.depthLines)(
    //  Reg(
    //    Bool(),
    //    init=False
    //  )
    //)
    Mem(
      wordType=Bool(),
      wordCount=cacheCfg.depthLines
    )
    .initBigInt(Array.fill(cacheCfg.depthLines)(BigInt(0)))
  )
  val lineAttrsValidMemWrEnable = Bool()
  val lineAttrsValidMemWrData = Bool()
  lineAttrsValidMemWrData := True
  lineAttrsValidMemWrEnable := False
  lineAttrsValidMem.write(
    address=lineAttrsRam.io.wrAddr,
    data=lineAttrsValidMemWrData,
    enable=lineAttrsValidMemWrEnable,
  )
  def currLineValid = (
    lineAttrsValidMem.readSync(
      address=lineAttrsRam.io.rdAddr
    )
  )
  def haveHit = (
    //rdLineAttrs.fire
    currLineValid
    && rdLineAttrs.tag === rBusAddrTag
  )

  //def setTlBusA(
  //  isStore: Bool,
  //  //addr: UInt,
  //  data: UInt,
  //  //src: UInt,
  //  isFakeBurst: Boolean,
  //  isFirstFakeBurstTxn: Boolean,
  //): Unit = {
  //  io.tlBus.a.valid := True
  //  when (!isStore) {
  //    io.tlBus.a.opcode := tilelink.Opcode.A.PUT_FULL_DATA
  //  } otherwise {
  //    io.tlBus.a.opcode := tilelink.Opcode.A.GET
  //  }
  //  io.tlBus.a.param := 0x0
  //  io.tlBus.a.size := 1//(cfg.dataWidth / 8)
  //  if (isFakeBurst) {
  //    io.tlBus.a.address := (
  //      tempLineBusAddr
  //    )
  //    if (isFirstFakeBurstTxn) {
  //      io.tlBus.a.source := (
  //        U(s"${io.tlBus.a.source.getWidth}'d${cacheCfg.srcStart}")
  //      )
  //      nextLineAddrCnt := 0x0
  //      nextBusSource
  //    } else {
  //      nextLineAddrCnt := rLineAddrCnt + 1
  //      io.tlBus.a.source := (
  //        //io.tlBus.a.source + 1
  //        rBusSource
  //      )
  //      //io.tlBus.a.address := (
  //      //  io.tlBus.a.address + cacheCfg.wordSizeBytes
  //      //)
  //    }
  //  } else {
  //    io.tlBus.a.address := (
  //      io.bus.sendData.addr
  //    )
  //    io.tlBus.a.source := (
  //      U(s"${io.tlBus.a.source.getWidth}'d${cacheCfg.srcStart}")
  //    )
  //  }
  //  io.tlBus.a.mask := B(io.tlBus.a.mask.getWidth bits, default -> True)
  //  io.tlBus.a.corrupt := False
  //  io.tlBus.a.data := (
  //    //io.lcvStall.sendData.data.asBits
  //    data.asBits
  //  )
  //}
  val rSendData = (
  )
  wrLineAttrs := (
    RegNext(
      next=wrLineAttrs,
      init=wrLineAttrs.getZero,
    )
  )
  switch (rState) {
    is (State.IDLE) {
      nextBridgeSavedFires := 0x0
      when (busRvalid) {
        //rSavedBusHostData := io.bus.sendData
        rLineAddrCnt := 0x0
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
              busReady := True
              busDevData := rdLineWord
            } otherwise {
              // cache miss upon a load
              when (if (isIcache) (False) else (rdLineAttrs.dirty)) {
                nextState := State.HANDLE_WR_LINE_TO_BUS
              } otherwise {
                nextState := State.HANDLE_RD_LINES_FROM_BUS
              }
              //--------
              //setTlBusA(
              //  isStore=(
              //    if (isIcache) (
              //      False
              //    ) else (
              //      rdLineAttrs.dirty
              //    )
              //  ),
              //  //addr=(
              //  //  busAddr
              //  //),
              //  data=(
              //    rdLineWord
              //    // since this can be anything (per the Tilelink spec)
              //    // for an opcode of `GET`, we can put the data read
              //    // from `lineRam` here every time
              //  ),
              //  isFakeBurst=true,
              //  isFirstFakeBurstTxn=true,
              //)
              //--------
              //nextTempBusAddr := busAddr + cacheCfg.wordSizeBytes
            }
          } otherwise {
            if (!isIcache) {
              when (haveHit) {
                // cached store (write-through!)
                lineWordRam.io.wrEn := True
                //lineRam.io.wrAddr := tempLineRamWrAddr
                // the default `tempLineRamWrAddr` is fine

                //lineRam.io.wrAddr := (
                //  (busAddr >> myLineRamAddrRshift)
                //  .resize(
                //    width=lineRam.io.wrAddr.getWidth
                //  )
                //)
                wrLineWord := rBusSendData.data

                lineAttrsRam.io.wrEn := True
                wrLineAttrs.dirty := (
                  True

                  // tiny optimization
                  // not very helpful in practice, probably
                  //wrLineWord =/= rdLineWord
                )
                wrLineAttrs.tag := rdLineAttrs.tag //busAddrTag
                //lineAttrsValidMem.write(
                //  address=
                //)
                lineAttrsValidMemWrEnable := True
                lineAttrsValidMemWrData := True
                nextState := State.HANDLE_STORE_HIT
                //when (io.tlBus.d.fire) {
                //  busReady := True
                //}
                myH2dBus.nextValid := True
                rH2dSendData.addr := rBusAddr.resized
                rH2dSendData.data := rBusSendData.data
                rH2dSendData.src := cacheCfg.srcStart
                rH2dSendData.isWrite := True
                rH2dSendData.size := 0x1
                //--------
                //setTlBusA(
                //  isStore=True,
                //  data=wrLineWord,
                //  isFakeBurst=false,
                //  isFirstFakeBurstTxn=false,
                //)
                //--------
              } otherwise {
              }
            }
          }
        } otherwise {
          if (!isIcache) {
            //when (!rBusSendData.accKind.asBits(1)) {
            //  // non-cached load
            //  nextState := State.HANDLE_NON_CACHED_LOAD
            //} otherwise {
            //  // non-cached store
            //  nextState := State.HANDLE_NON_CACHED_STORE
            //}
            nextState := State.HANDLE_NON_CACHED_BUS_ACC
            //--------
            //setTlBusA(
            //  isStore=rBusSendData.accKind.asBits(1),
            //  //addr=busAddr,
            //  data=(
            //    rdLineWord
            //    // since this can be anything (per the Tilelink spec)
            //    // for an opcode of `GET`, we can put the data read
            //    // from `lineRam` here every time
            //  ),
            //  isFakeBurst=false,
            //  isFirstFakeBurstTxn=false,
            //)
            //--------

            //--------
            // TODO: Implement this
            //when (io.tlBus.d.fire) {
            //  busReady := True
            //  when (!rBusSendData.accKind.asBits(1)) {
            //    busDevData := io.tlBus.d.data.asUInt
            //  }
            //}
          }
        }
      }
    }
    //def handleWriteLineRam(
    //  isStoreHit: Boolean
    //): Unit = {
    //  if (!isStoreHit) {
    //  } else {
    //  }
    //}
    is (State.HANDLE_WR_LINE_TO_BUS) {
      //handleWriteLineRam(
      //  isStoreHit=false
      //)
    }
    is (State.HANDLE_RD_LINES_FROM_BUS) {
      if (isIcache) {
        // just do a plain cache eviction here
      } else {
      }
    }
    is (State.HANDLE_STORE_HIT) {
      ////handleWriteLineRam(
      ////  isStoreHit=true
      ////)
      //when (myH2dBus.fire) {
      //  myH2dBus.nextValid := False
      //  nextBridgeSavedFires(0) := True
      //}
      //when (
      //  myD2hBus.rValid
      //  && (
      //    nextBridgeSavedFires(0)
      //    || rBridgeSavedFires(0)
      //  )
      //) {
      //  myD2hBus.ready := True
      //  nextState := State.IDLE
      //  //nextBridgeSavedFires(1) := True
      //}
    }
    //is (State.HANDLE_STORE_MISS_RD) {
    //  if (!isIcache) {
    //  }
    //}
    //is (State.HANDLE_STORE_MISS_WR) {
    //  if (!isIcache) {
    //  }
    //}
    is (State.HANDLE_NON_CACHED_BUS_ACC) {
      if (!isIcache) {
      }
    }
    //is (State.HANDLE_NON_CACHED_STORE) {
    //  if (!isIcache) {
    //  }
    //}
    is (State.YIELD_RESULT) {
      //when (busRvalid) {
      //  busReady := True
      //}
    }
  }
}
