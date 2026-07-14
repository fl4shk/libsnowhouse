package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

import libcheesevoyage.general._
import libcheesevoyage.math._
import libcheesevoyage.bus.lcvStall._
import libcheesevoyage.bus.lcvBus._


//case class SnowHousePipePayload(
//  cfg: SnowHouseConfig,
//) extends Bundle {
//  val regPc = UInt(cfg.mainAddrWidth bits)
//  val encInstr = UInt(cfg.instrMainWidth bits)
//}

//case class SnowHouseForFmaxSharedPsIo(
//  cfg: SnowHouseConfig
//) extends Bundle {
//  val up = slave(
//    Stream(SnowHousePipePayload(cfg=cfg))
//  )
//  val down = master(
//    Stream(SnowHousePipePayload(cfg=cfg))
//  )
//}

case class SnowHouseForFmaxPipeStageInstrFetchIo(
  cfg: SnowHouseConfig
) extends Bundle {
  //--------
  val down = (
    master(Stream(
      SnowHousePipePayload(cfg=cfg)
    ))
  )
  //--------
  val lcvIbus = (
    master(LcvBusIo(
      cfg=cfg.subCfg.lcvIbusEtcCfg.loBusCfg,
    ))
  )
  //--------
  val psExSetPc = (
    slave(Flow(
      SnowHousePsExSetPcPayload(cfg=cfg)
    ))
  )
  //--------
}

case class SnowHouseForFmaxPipeStageInstrFetch(
  cfg: SnowHouseConfig
) extends Component {
  //--------
  val io = SnowHouseForFmaxPipeStageInstrFetchIo(cfg=cfg)
  //def up = io.up
  //def down = io.down
  //--------
  val linkArr = PipeHelper.mkLinkArr()

  val pIf = Payload(SnowHousePipePayload(cfg=cfg))
  val cIf = CtrlLink()
  val sIf = StageLink(
    up=cIf.down,
    down={
      val temp = Node()
      temp.setName("sIf_down")
      temp
    }
  )
  val s2mIf = S2MLink(
    up=sIf.down,
    down={
      val temp = Node()
      temp.setName("s2mIf_down")
      temp
    }
  )
  linkArr += cIf
  linkArr += sIf
  linkArr += s2mIf

  val up = cIf.up
  val down = cIf.down
  val psExSetPc = io.psExSetPc

  //def cIf = args.link

  //cIf.up.valid := True
  //cIf.up.driveFrom(io.up)(
  //  con=(node, inp) => {
  //  }
  //)

  val myH2dPushStm = (
    cloneOf(io.lcvIbus.h2dBus)
  )
  def myBusH2dValid = (
    myH2dPushStm.valid
  )
  def myBusAddr = (
    myH2dPushStm.addr
  )
  myBusAddr := RegNext(myBusAddr, init=myBusAddr.getZero)
  myH2dPushStm.byteSize := log2Up(cfg.instrMainWidth / 8)
  myH2dPushStm.isWrite := False
  myH2dPushStm.data := 0x0
  val nextSrc = cloneOf(myH2dPushStm.src)
  val rSrc = (
    RegNext(
      nextSrc,
      init=nextSrc.getZero,
    )
  )
  val tempSrcRnw = (
    RegNextWhen(
      next=rSrc.asSInt,
      cond=myH2dPushStm.fire,
    )
    init(-2)
  )
  nextSrc := rSrc
  myH2dPushStm.src := rSrc
  when (myH2dPushStm.fire) {
    nextSrc := rSrc + 1
  } otherwise {
    myH2dPushStm.src := tempSrcRnw.asUInt
  }

  val myReadyIshCond = Bool()
  val myReadyIshCondShared = (
    myH2dPushStm.fire
  )
  myReadyIshCond := (
    myReadyIshCondShared
  )

  val myRegPcSetItCnt = (
    UInt(cfg.myPsIfRegPcSetItCntWidth bits)
  )
  val rPrevRegPcSetItCnt = {
    val temp = (
      RegNextWhen(
        next=myRegPcSetItCnt,
        cond=myReadyIshCond,
      )
      init(0x0)
    )
    temp
  }
  myRegPcSetItCnt.allowOverride
  myRegPcSetItCnt := rPrevRegPcSetItCnt

  case class MyIbusTempPayload(
    hasInstr: Boolean,
  ) extends Bundle {
    val instr = (
      hasInstr
    ) generate (
      UInt(cfg.instrMainWidth bits)
    )

    val psIfRegPcSetItCnt = (
      cloneOf(myRegPcSetItCnt)
    )

    val myIbusRegPcInfo = (
      MyIbusRegPcInfo(cfg=cfg)
    )
  }
  val myIbusTempRam = {
    val depth = 1 << cfg.subCfg.myLcvBusSrcWidth
    def mySetWordFunc(
      outp: MyIbusTempPayload,
      inp: MyIbusTempPayload,
      word: MyIbusTempPayload,
    ): Unit = {
      outp.psIfRegPcSetItCnt := word.psIfRegPcSetItCnt
      outp.myIbusRegPcInfo := word.myIbusRegPcInfo
      outp.instr.allowOverride
      outp.instr := inp.instr
    }
    val ramCfg = WrPulseRdPipeRamSdpPipeConfig(
      modType=(
        MyIbusTempPayload(hasInstr=true)
      ),
      wordType=MyIbusTempPayload(hasInstr=false),
      wordCount=depth,
      pipeName="pipeStageIf",
      setWordFunc=mySetWordFunc,
      optWrHistLength=(
        2
        //3
        //4
      ),
      initBigInt=(
        //Some(initBigInt)
        Some({
          val myArr = new ArrayBuffer[ArrayBuffer[BigInt]]()
          myArr += new ArrayBuffer[BigInt]()
          for (idx <- 0 until depth) {
            myArr.last += BigInt(0)
          }
          myArr
        })
      ),
      arrRamStyleAltera=(
        "no_rw_check, M10K",//"MLAB",//"M10K"
        //"no_rw_check, MLAB",//"MLAB",//"M10K"
      ),
      arrRamStyleXilinx=(
        "block"
        //"distributed"
      ),
    )
    WrPulseRdPipeRamSdpPipe(cfg=ramCfg)
  }

  val rIbusTempRamInitCnt = {
    val temp = Reg(UInt((cfg.subCfg.myLcvBusSrcWidth + 2) bits))
    temp.init(temp.getZero)
    temp
  }

  // NOTE: setting `myBusH2dValid` to a constant `True` can cause issues
  // if the `libsnowhouse` implementation of a CPU is hooked up a
  // non-pipelined `LcvBus` instruction source
  // (such as FL4SHK's own `LcvBusMemSlowUnlessBurst` module)
  myBusH2dValid := (
    //rIbusTempRamInitCnt.msb
    True
  )
  val myIbusRegPcInfo = MyIbusRegPcInfo(cfg=cfg)
  def myD2hPopStm = io.lcvIbus.d2hBus
  //when (rIbusTempRamInitCnt.msb) {
    cIf.up.driveFrom(myIbusTempRam.io.rdDataPipe)(
      con=(node, payload) => {
        node(pIf) := node(pIf).getZero
        node(pIf).encInstr.payload.allowOverride
        node(pIf).psIfRegPcSetItCnt.allowOverride
        node(pIf).regPc.allowOverride
        node(pIf).laggingRegPc.allowOverride
        node(pIf).branchPredictTkn.allowOverride
        node(pIf).branchTgtBufElem.allowOverride

        node(pIf).encInstr.payload := payload.instr
        node(pIf).psIfRegPcSetItCnt := payload.psIfRegPcSetItCnt
        node(pIf).regPc := payload.myIbusRegPcInfo.regPc
        node(pIf).laggingRegPc := payload.myIbusRegPcInfo.regPc
        node(pIf).branchPredictTkn := (
          payload.myIbusRegPcInfo.branchPredictTkn
          //True
        )
        node(pIf).branchTgtBufElem := (
          payload.myIbusRegPcInfo.branchTgtBufElem
        )
      }
    )

    myIbusTempRam.io.wrPulse.valid := myH2dPushStm.fire
    myIbusTempRam.io.wrPulse.addr := myH2dPushStm.src //+ 1
    myIbusTempRam.io.wrPulse.data.psIfRegPcSetItCnt := (
      myRegPcSetItCnt
    )
    myIbusTempRam.io.wrPulse.data.myIbusRegPcInfo := (
      myIbusRegPcInfo
    )
  //} otherwise {
  //  cIf.up.valid := False
  //  cIf.up(pIf) := cIf.up(pIf).getZero
  //  myIbusTempRam.io.wrPulse.valid := True
  //  myIbusTempRam.io.wrPulse.addr := rIbusTempRamInitCnt(
  //    rIbusTempRamInitCnt.high - 2 downto 0
  //  )
  //  myIbusTempRam.io.wrPulse.data := (
  //    myIbusTempRam.io.wrPulse.data.getZero
  //  )
  //  myIbusTempRam.io.rdDataPipe.ready := True

  //  rIbusTempRamInitCnt := rIbusTempRamInitCnt + 1
  //}
  io.lcvIbus.h2dBus << myH2dPushStm
  myD2hPopStm.translateInto(myIbusTempRam.io.rdAddrPipe)(
    dataAssignment=(outp, inp) => {
      outp := outp.getZero
      outp.data.instr.allowOverride
      outp.data.instr := inp.data.resize(outp.data.instr.getWidth)
      outp.addr.allowOverride
      outp.addr := inp.src
    }
  )

  val stickyExSetPc = {
    val temp = (
      Vec.fill(1)(
        Flow(
          SnowHousePsExSetPcPayload(cfg=cfg)
        )
      )
    )
    temp.foreach(item => {
      item := RegNext(item, init=item.getZero)
    })
    temp.setName(s"psIf_stickyExSetPc")
  }
  val branchTgtBuf = (
    cfg.haveBranchPredictor
  ) generate (
    SnowHouseBranchTgtBuf(cfg=cfg)
    //SnowHouseBranchTgtBufSingle(cfg=cfg)
  )
  if (cfg.haveBranchPredictor) {
    branchTgtBuf.io.psExSetPc := psExSetPc
    branchTgtBuf.io.upIsFiring := up.isFiring
    branchTgtBuf.io.upIsReady := myReadyIshCond //myUpdatePcCond
  }

  val takeJumpCntMaxVal = cfg.takeJumpCntMaxVal
  val rTakeJumpCnt = {
    val temp = Reg(Flow(UInt(
      log2Up(takeJumpCntMaxVal + 1) + 1 bits
    )))
    temp.init(temp.getZero)
    temp
  }

  when (rTakeJumpCnt.fire) {
    stickyExSetPc(0).valid := False
  }

  when (psExSetPc.valid) {
    stickyExSetPc.foreach(_.valid := True)
    stickyExSetPc(0).btbElemWithBrKind.allowOverride
    stickyExSetPc(0).btbElemWithBrKind := psExSetPc.btbElemWithBrKind
    stickyExSetPc(0).nextPc.allowOverride
    stickyExSetPc(0).nextPc := psExSetPc.nextPc
  }

  val myNextRegPcInit = 0
  val myRegPcShiftThing = (
    S(s"${log2Up(cfg.instrSizeBytes)}'d0")
  )
  def myRegPc = myIbusRegPcInfo.regPc
  val myPrevRegPcPlusInstrSizeWidth = (
    myRegPc.getWidth - log2Up(cfg.instrSizeBytes)
  )
  val rPrevRegPc = {
    val temp = RegNextWhen(
      next=(
        Vec.fill(2)(
          myRegPc.asSInt(myRegPc.high downto log2Up(cfg.instrSizeBytes))
        )
      ), 
      cond=myReadyIshCond,
    )
    temp.foreach(item => {
      item.init(-1)
    })
    temp
  }
  val myRawPredictCond = Bool()
  myRawPredictCond := (
    branchTgtBuf.io.result.fire
    && !rTakeJumpCnt.fire
    && !stickyExSetPc(0).fire
  )
  val rMyMainPredictCond = (
    //Reg(Bool(), init=False)
    RegNextWhen(
      myRawPredictCond,
      cond=myReadyIshCond,
      init=myRawPredictCond.getZero,
    )
  )
  val predictCond = (
    cfg.haveBranchPredictor
  ) generate (
    //myMainPredictCond
    rMyMainPredictCond
  )

  val tempNextRegPc = (
    cfg.haveBranchPredictor
  ) generate (
    Cat(
      rPrevRegPc.last + 1,
      myRegPcShiftThing,
    ).asSInt
  )
  val myTempNextRegPcMaybeDel1 = (
    cfg.haveBranchPredictor
  ) generate (
    tempNextRegPc
  )
  val myPredictedNextPc = (
    cfg.haveBranchPredictor
  ) generate (
    Mux[SInt](
      rMyMainPredictCond,
      (
        RegNextWhen(
          branchTgtBuf.io.result.nextRegPc.asSInt,
          cond=myReadyIshCond,
          init=branchTgtBuf.io.result.nextRegPc.asSInt.getZero,
        )
      ),
      (
        myTempNextRegPcMaybeDel1
      ),
    ).asUInt
  )
  if (cfg.haveBranchPredictor) {
    for (idx <- 0 until branchTgtBuf.io.inpRegPc.size) {
      //when (!rTakeJumpCnt.fire) {
        // TODO: determine if this is correct!
        branchTgtBuf.io.inpRegPc(idx) := (
          myPredictedNextPc
          //RegNextWhen(
          //  myPredictedNextPc + cfg.instrSizeBytes,
          //  cond=myReadyIshCond,
          //  init=myPredictedNextPc.getZero,
          //)
          //Cat(
          //  (rPrevRegPc(0) + 1),
          //  myRegPcShiftThing,
          //).asUInt
        )
      //} otherwise {
      //  branchPredictor.io.inpRegPc(idx) := (
      //    //myPredictedNextPc
      //    Cat(
      //      (rPrevRegPc(0) + 1),
      //      myRegPcShiftThing,
      //    ).asUInt
      //  )
      //}
    }
  }
  def doInitTakeJumpCnt(): Unit = {
    rTakeJumpCnt.valid := True
    rTakeJumpCnt.payload := takeJumpCntMaxVal
  }

  val myUpdateRegPcCondUInt = (
    Cat(
      List(
        myReadyIshCond,
        //myUpdatePcCond,
        stickyExSetPc.head.fire,
      ).reverse
    )
  )
  myIbusRegPcInfo := (
    // set everything to zero for debugging purposes
    myIbusRegPcInfo.getZero
  )
  for (idx <- 0 until stickyExSetPc.size) {
    def doPsExSetPcValid(
      useStickyNextPc: Boolean
    ): Unit = {
      doInitTakeJumpCnt()

      val temp = (
        if (useStickyNextPc) (
          stickyExSetPc(0).nextPc
        ) else (
          psExSetPc.nextPc
        )
      )
      val tempNextRegPc = temp
      myIbusRegPcInfo.regPc := tempNextRegPc//.asUInt
      if (cfg.haveBranchPredictor) {
        myIbusRegPcInfo.branchPredictTkn := False
      }
      myBusAddr := tempNextRegPc.resize(myBusAddr.getWidth)//.asUInt
    }
    switch (myUpdateRegPcCondUInt) {
      is (M"0-") {
      }
      is (M"10") {
        if (cfg.haveBranchPredictor) {
          val temp = myPredictedNextPc
          myBusAddr := temp.resize(myBusAddr.getWidth)
          myIbusRegPcInfo.regPc := temp
          myIbusRegPcInfo.branchPredictTkn.allowOverride
          myIbusRegPcInfo.branchPredictTkn := (
            myRawPredictCond
          )
          myIbusRegPcInfo.branchTgtBufElem.foreach(item => {
            item := (
              branchTgtBuf.io.result.rdBtbElem
            )
          })
        } else {
          val temp = (
            Cat(
              rPrevRegPc.last + 1,
              myRegPcShiftThing,
            ).asUInt
          )
          myBusAddr := temp.resize(myBusAddr.getWidth)
          myIbusRegPcInfo.regPc := temp
        }
      }
      default {
        doPsExSetPcValid(
          useStickyNextPc=(
            //false
            true
          )
        )
      }
    }
  }

  when (myReadyIshCond) {
    myRegPcSetItCnt := 0x0
    when (rTakeJumpCnt.fire) {
      when (
        (
          myH2dPushStm.addr
          === RegNext(
            stickyExSetPc(0).branchTgtBufElem.dstRegPc,
            init=stickyExSetPc(0).branchTgtBufElem.dstRegPc.getZero,
          )
        )
      ) {
        rTakeJumpCnt.valid := False
        myRegPcSetItCnt := 0x1
      }
    }
  }


  s2mIf.down.driveTo(
    io.down
  )(
    con=(outp, node) => {
      outp := node(pIf)
    }
  )

  Builder(linkArr)
  //--------
}

case class SnowHouseForFmaxPipeStageInstrDecodeIo(
  cfg: SnowHouseConfig
) extends Bundle {
  //--------
  val up = (
    slave(Stream(
      SnowHousePipePayload(cfg=cfg)
    ))
  )
  val down = (
    master(Stream(
      SnowHousePipePayload(cfg=cfg)
    ))
  )
  //--------
  val psExSetPc = (
    slave(Flow(
      SnowHousePsExSetPcPayload(cfg=cfg)
    ))
  )
  //--------
}
case class SnowHouseForFmaxPipeStageInstrDecode(
  cfg: SnowHouseConfig,
  val doDecodeFunc: (SnowHousePipeStageInstrDecode) => Area,
) extends Component {
  //--------
  val io = SnowHouseForFmaxPipeStageInstrDecodeIo(cfg=cfg)
  //def up = io.up
  //def down = io.down
  //--------
  val linkArr = PipeHelper.mkLinkArr()

  //def opInfoMap = cfg.opInfoMap

  val pIdInp = Payload(SnowHousePipePayload(cfg=cfg))
  val pIdOutp = Payload(SnowHousePipePayload(cfg=cfg))
  val cId = CtrlLink()
  val sId = StageLink(
    up=cId.down,
    down={
      val temp = Node()
      temp.setName("sId_down")
      temp
    }
  )
  val s2mId = S2MLink(
    up=sId.down,
    down={
      val temp = Node()
      temp.setName("s2mId_down")
      temp
    }
  )
  linkArr += cId
  linkArr += sId
  linkArr += s2mId

  val innerPsId = SnowHousePipeStageInstrDecode(
    SnowHousePipeStageArgs(
      cfg=cfg,
      io=null,
      link=cId,
      prevPayload=pIdInp,
      currPayload=(
        pIdOutp
        //regFile.io.frontPayload
      ),
      myDbusIo=(
        //if (!cfg.useLcvDataBus) (
        //  myDbusIo
        //) else (
          null.asInstanceOf[SnowHouseDbusIo]
        //)
      ),
      regFile=null,
    ),
    psIdHaltIt=null,
    psExSetPc=io.psExSetPc,
    pcChangeState=null,
    shouldIgnoreInstr=null,
    doDecodeFunc=cfg.doInstrDecodeFunc,
    //psIdFoundBubble=psIdFoundBubble,
  )

  cId.up.driveFrom(io.up)(
    con=(node, inp) => {
      node(pIdInp) := inp
    }
  )

  s2mId.down.driveTo(
    io.down
  )(
    con=(outp, node) => {
      outp := node(pIdOutp)
    }
  )

  Builder(linkArr)
  //--------
}
