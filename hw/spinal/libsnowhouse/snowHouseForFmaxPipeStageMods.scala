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
  val cLink = CtrlLink()
  val sLink = StageLink(
    up=cLink.down,
    down={
      val temp = Node()
      temp.setName("sIf_down")
      temp
    }
  )
  val s2mLink = S2MLink(
    up=sLink.down,
    down={
      val temp = Node()
      temp.setName("s2mIf_down")
      temp
    }
  )
  linkArr += cLink
  linkArr += sLink
  linkArr += s2mLink

  val up = cLink.up
  val down = cLink.down
  val psExSetPc = io.psExSetPc

  val pipeStageLink = SnowHousePipeStageInstrFetch(
    args=SnowHousePipeStageArgs(
      cfg=cfg,
      io=null,
      link=cLink,
      prevPayload=null,
      currPayload=pIf,
      myDbusIo=null.asInstanceOf[SnowHouseDbusIo],
      regFile=null,
    ),
    //psIdHaltIt=null,
    psExSetPc=psExSetPc,
    lcvIbus=io.lcvIbus,
  )

  s2mLink.down.driveTo(
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
  val cLink = CtrlLink()
  val sLink = StageLink(
    up=cLink.down,
    down={
      val temp = Node()
      temp.setName("sLink_down")
      temp
    }
  )
  val s2mLink = S2MLink(
    up=sLink.down,
    down={
      val temp = Node()
      temp.setName("s2mLink_down")
      temp
    }
  )
  linkArr += cLink
  linkArr += sLink
  linkArr += s2mLink

  val innerPsId = SnowHousePipeStageInstrDecode(
    SnowHousePipeStageArgs(
      cfg=cfg,
      io=null,
      link=cLink,
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
    //psIdHaltIt=null,
    psExSetPc=io.psExSetPc,
    //pcChangeState=null,
    //shouldIgnoreInstr=null,
    doDecodeFunc=cfg.doInstrDecodeFunc,
    //psIdFoundBubble=psIdFoundBubble,
  )

  cLink.up.driveFrom(io.up)(
    con=(node, inp) => {
      node(pIdInp) := inp
    }
  )

  s2mLink.down.driveTo(
    io.down
  )(
    con=(outp, node) => {
      outp := node(pIdOutp)
    }
  )

  Builder(linkArr)
  //--------
}

case class SnowHouseForFmaxPipeStagePreExIo(
  cfg: SnowHouseConfig
) extends Bundle {
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
  val myBranchMispredictEtc = in(Bool())
  //--------
}

case class SnowHouseForFmaxPipeStagePostIdPreEx(
  cfg: SnowHouseConfig,
) extends Component {
  //--------
  val io = SnowHouseForFmaxPipeStagePreExIo(cfg=cfg)
  //def up = io.up
  //def down = io.down
  //--------
  val linkArr = PipeHelper.mkLinkArr()

  //def opInfoMap = cfg.opInfoMap

  //val pPreExInp = Payload(SnowHousePipePayload(cfg=cfg))
  val pPreExOutp = Payload(SnowHousePipePayload(cfg=cfg))
  val cLink = CtrlLink()
  //val sLink = StageLink(
  //  up=cLink.down,
  //  down={
  //    val temp = Node()
  //    temp.setName("sLink_down")
  //    temp
  //  }
  //)
  //val s2mLink = S2MLink(
  //  up=sLink.down,
  //  down={
  //    val temp = Node()
  //    temp.setName("s2mLink_down")
  //    temp
  //  }
  //)
  linkArr += cLink
  //linkArr += sLink
  //linkArr += s2mLink

  val myOutp = SnowHousePipePayload(cfg=cfg)
  val myInp = SnowHousePipePayload(cfg=cfg)
  //myInp := RegNext(myInp)
  myOutp := RegNext(myOutp)
  myOutp.allowOverride
  when (cLink.up.isValid) {
    myOutp := myInp
  }

  val innerPsPostIdPreEx = SnowHousePipeStagePostIdPreEx(
    cfg=cfg,
    outp=myOutp,
    inp=myInp,
    //link=cLink,
    upIsFiring=cLink.up.isFiring,
    myBranchMispredictEtc=io.myBranchMispredictEtc,
  )

  cLink.up.driveFrom(io.up)(
    con=(node, inp) => {
      //node(pPreExInp) := inp
      myInp := inp
      node(pPreExOutp) := myOutp
    }
  )
  //when (cLink.up.valid) {
  //}
  //cLink.up(pPreExOutp) := myOutp

  cLink.down.driveTo(io.down)(
    con=(outp, node) => {
      outp := node(pPreExOutp)
    }
  )

  Builder(linkArr)
  //--------
}

case class SnowHouseForFmaxPipeStageExecuteIo(
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
  val myRegFileWrPulse = (
    slave(Flow(
      PipeSimpleDualPortMemDrivePayload(
        dataType=UInt(cfg.mainWidth bits),
        wordCount=cfg.regFileCfg.wordCountArr(0),
      )
    ))
  )
  //--------
  val psExSetPc = (
    master(Flow(
      SnowHousePsExSetPcPayload(cfg=cfg)
    ))
  )
  //--------
  val psWbToEarlierStallRequest = (
    in(Bool())
  )
  //--------
  val idsIraIrq = (
    cfg.myHaveIrqIdsIra
  ) generate (
    slave(new LcvStallIo[Bool, Bool](
      sendPayloadType=None,
      recvPayloadType=None,
    ))
  )
  //--------
  val myLcvDbusH2dStm = (
    master(Stream(
      LcvBusH2dPayload(cfg=cfg.subCfg.lcvIbusEtcCfg.loBusCfg)
    ))
  )
  val multiCycleBusVec = (
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
        //((_, opInfo), idx) <- cfg.multiCycleOpInfoMap.view.zipWithIndex
        (group, _) <- cfg.multiCycleOpInfoMap.view
      ) {
        //assert(
        //  opInfo.select == OpSelect.MultiCycle
        //)
        //if (opInfo.select == OpSelect.MultiCycle) {
          tempArr += new LcvStallIo(
            sendPayloadType=(
              Some(MultiCycleHostPayload(
                cfg=cfg,
                group=group,
                //opInfo=opInfo
                //maxSrcArrSize=(
                //  cfg.
                //)
              ))
            ),
            recvPayloadType=(
              Some(MultiCycleDevPayload(
                cfg=cfg,
                group=group,
                //opInfo=opInfo
              ))
            ),
          )
        //}
      }
      tempArr
    }
  )
  for (idx <- 0 until multiCycleBusVec.size) {
    master(
      multiCycleBusVec(idx)
    )
  }
  //val myModMemWord = (
  //  out(SInt(cfg.mainWidth bits))
  //)
  //--------
}
case class SnowHouseForFmaxPipeStageExecute(
  cfg: SnowHouseConfig
) extends Component {
  //--------
  val io = SnowHouseForFmaxPipeStageExecuteIo(cfg=cfg)
  //def up = io.up
  //def down = io.down
  //--------
  val linkArr = PipeHelper.mkLinkArr()

  //def opInfoMap = cfg.opInfoMap

  val pExInp = Payload(SnowHousePipePayload(cfg=cfg))
  val pExOutp = Payload(SnowHousePipePayload(cfg=cfg))
  val cLink = CtrlLink()
  val sLink = StageLink(
    up=cLink.down,
    down={
      val temp = Node()
      temp.setName("sLink_down")
      temp
    }
  )
  val s2mLink = S2MLink(
    up=sLink.down,
    down={
      val temp = Node()
      temp.setName("s2m_down")
      temp
    }
  )
  linkArr += cLink
  linkArr += sLink
  linkArr += s2mLink

  val myModMemWord = SInt(cfg.mainWidth bits)
  val innerPsEx = SnowHousePipeStageExecute(
    SnowHousePipeStageArgs(
      cfg=cfg,
      io=null,
      link=cLink,
      prevPayload=pExInp,
      currPayload=pExOutp,
      myDbusIo=(
        //if (!cfg.useLcvDataBus) (
        //  myDbusIo
        //) else (
          null.asInstanceOf[SnowHouseDbusIo]
        //)
      ),
      regFile=null,
    ),
    //psExHaltIt=null,
    psExSetPc=io.psExSetPc,
    doModInMid0FrontParams=null,
    myModMemWord=myModMemWord,
    psWbToEarlierStallRequest=io.psWbToEarlierStallRequest,
    myLcvDbusH2dStm=io.myLcvDbusH2dStm,
    multiCycleBusVec=io.multiCycleBusVec,
    idsIraIrq=io.idsIraIrq,
    //pcChangeState=null,
    //shouldIgnoreInstr=null,
    //psExFoundBubble=psExFoundBubble,
    forFmaxRegFileWrPulseArr=Array(
      io.myRegFileWrPulse
    )
  )

  cLink.up.driveFrom(io.up)(
    con=(node, inp) => {
      node(pExInp) := inp
    }
  )

  s2mLink.down.driveTo(
    io.down
  )(
    con=(outp, node) => {
      outp := node(pExOutp)
    }
  )

  Builder(linkArr)
  //--------
}

case class SnowHouseForFmaxPipeStageWriteBackIo(
  cfg: SnowHouseConfig
) extends Bundle {
  //--------
  val up = (
    slave(Stream(
      SnowHousePipePayload(cfg=cfg)
    ))
  )
  val dbgInfo = (
    cfg.exposeRegFileWriteDataToIo
    || cfg.exposeRegFileWriteAddrToIo
    || cfg.exposeRegFileWriteEnableToIo
    || cfg.dbgExposeExtrasAtRegFileWrite
  ) generate (
    out(SnowHouseDebugInfo(cfg=cfg))
  )
  //--------
  val myLcvDbusD2hStm = (
    slave(Stream(
      LcvBusD2hPayload(cfg=cfg.subCfg.lcvIbusEtcCfg.loBusCfg)
    ))
  )
  //--------
  val myRegFileWrPulse = master(Flow(
    PipeSimpleDualPortMemDrivePayload(
      dataType=UInt(cfg.mainWidth bits),
      wordCount=cfg.regFileCfg.wordCountArr(0),
    )
  ))
  //--------
}
case class SnowHouseForFmaxPipeStageWriteBack(
  cfg: SnowHouseConfig
) extends Component {
  //--------
  val io = SnowHouseForFmaxPipeStageWriteBackIo(cfg=cfg)
  //def up = io.up
  //def down = io.down
  //--------
  val linkArr = PipeHelper.mkLinkArr()

  //def opInfoMap = cfg.opInfoMap

  //val pwbInp = Payload(SnowHousePipePayload(cfg=cfg))
  //val pwbOutp = Payload(SnowHousePipePayload(cfg=cfg))
  val cLink = CtrlLink()
  //val sLink = StageLink(
  //  up=cLink.down,
  //  down={
  //    val temp = Node()
  //    temp.setName("sLink_down")
  //    temp
  //  }
  //)
  //val s2mLink = S2MLink(
  //  up=sLink.down,
  //  down={
  //    val temp = Node()
  //    temp.setName("s2m_down")
  //    temp
  //  }
  //)
  linkArr += cLink
  //linkArr += sLink
  //linkArr += s2mLink

  val currWbPayloadOuterVecSize = (
    if (cfg.optScoreboard) (2) else (1)
  )
  val myWbPayloadVec = (
    Vec.fill(currWbPayloadOuterVecSize)(
      Vec.fill(2)(
        SnowHousePipePayload(cfg=cfg)
      )
    )
  )

  //val myCurrWbPayloadOuterIdxInfo = (
  //  cfg.optScoreboard
  //) generate (
  //  UInt(log2Up(currWbPayloadOuterVecSize) bits)
  //)
  val rCurrWbPayloadOuterIdx = (
    cfg.optScoreboard
  ) generate ({
    val temp = Reg(
      //Flow(
        UInt(log2Up(currWbPayloadOuterVecSize) bits)
      //)
    )
    temp.init(temp.getZero)
    temp
    //UInt(log2Up(currWbPayloadOuterVecSize) bits)
  })
  val rScoreboardStallCnt = (
    cfg.optScoreboard
  ) generate (
    Reg(UInt(log2Up(cfg.optMaxNumScoreboardInstrs + 1) + 1 bits))
    init(0x0)
  )
  if (cfg.optScoreboard) {
    //rCurrWbPayloadOuterIdx := (
    //  RegNext(
    //    rCurrWbPayloadOuterIdx,
    //    init=rCurrWbPayloadOuterIdx.getZero
    //  )
    //)
  }
  def myWbPayload = (
    if (cfg.optScoreboard) (
      myWbPayloadVec(
        (
          //myCurrWbPayloadOuterIdxInfo
          //| rCurrWbPayloadOuterIdx
          //Mux(
          //  myCurrWbPayloadOuterIdxInfo.lsb,
          //  False.asUInt.resize(myCurrWbPayloadOuterIdxInfo.getWidth),
            rCurrWbPayloadOuterIdx
          //)
        )
      )
    ) else (
      myWbPayloadVec.head
    )
  )
  myWbPayloadVec.foreach(item => {
    if (cfg.optScoreboard) {
      item := (
        RegNext(
          item,
          init=item.getZero
        )
      )
    } else {
      item(1) := (
        RegNext(
          item(1),
          init=item(1).getZero
        )
      )
    }
  })

  when (cLink.up.isValid) {
    myWbPayload(1) := myWbPayload(0)
  }

  val myLcvDbusArea = new Area {
    //myDbusIo.myDbusExtraValid := (
    //  cWb.up.isValid
    //  && myWbPayload.outpDecodeExt.opIsMemAccess.last
    //)
    val myD2hBus = cloneOf(io.myLcvDbusD2hStm)
    //myD2hBus <-/< io.lcvDbus.d2hBus
    myD2hBus << io.myLcvDbusD2hStm
    myD2hBus.ready := False

    //myCurrWbPayloadOuterIdxInfo.lsb := (
    //  myD2hBus.fire
    //)

    //psWbToEarlierStallRequest := False

    when (
      (
        if (cfg.optScoreboard) (
          cLink.up.isValid
          || rCurrWbPayloadOuterIdx.lsb
        ) else (
          cLink.up.isValid
        )
      )
      && myWbPayloadVec.head(1).outpDecodeExt.opIsMemAccess.last
    ) {
      myD2hBus.ready := True
    }

    when (
      (
        if (cfg.optScoreboard) (
          cLink.up.isValid
          && !rCurrWbPayloadOuterIdx.lsb
        ) else (
          cLink.up.isValid
        )
      )
      && myWbPayloadVec.head(1).outpDecodeExt.opIsMemAccess.last
      && !myD2hBus.valid
    ) {
      //psWbToEarlierStallRequest := True
      if (cfg.optScoreboard) {
        rCurrWbPayloadOuterIdx.lsb := True
      } else {
        cLink.duplicateIt()
      }
    }
    if (cfg.optScoreboard) {
      when (
        (
          if (cfg.optScoreboard) (
            cLink.up.isValid
            || rCurrWbPayloadOuterIdx.lsb
          ) else (
            cLink.up.isValid
          )
        )
        && myWbPayloadVec.head(1).outpDecodeExt.opIsMemAccess.last
        && (
          // this is checking for `myD2hBus.fire`
          myD2hBus.valid
        )
      ) {
        //psWbToEarlierStallRequest := True
        rCurrWbPayloadOuterIdx.lsb := False
        cLink.duplicateIt()
      }
    }
    switch (
      (
        (
          if (cfg.optScoreboard) (
            cLink.up.isValid
            || rCurrWbPayloadOuterIdx.lsb
          ) else (
            cLink.up.isValid
          )
        )
        && myWbPayloadVec.head(1).outpDecodeExt.opIsMemAccess.head
        && !myWbPayloadVec.head(1).outpDecodeExt.memAccessKind.asBits(1)
        && (
          //myD2hBus.valid
          myD2hBus.fire
        )
      )
      ## myWbPayloadVec.head(1).outpDecodeExt.memAccessKind.asBits(0)
      ## myWbPayloadVec.head(1).outpDecodeExt.memAccessSubKind.asBits
    ) {
      //--------
      // This stuff might need to be changed for the purposes of
      // atomic operations that are larger than `cfg.mainWidth`.
      // It's currently limited to at max 32-bit values, for example, on a
      // 32-bit `cfg.mainWidth` CPU. More work will be needed later.
      //--------
      val myDecodeExt = myWbPayloadVec.head(1).outpDecodeExt
      val mapElem = myWbPayloadVec.head(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myWbPayloadVec.head(1).myExt(
            0
          )
        ) else (
          myWbPayloadVec.head(1).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      //--------
      is (M"10--") {
        // zero-extending sub-word load or full-word load
        myCurrExt.modMemWord := myD2hBus.data
      }
      is (M"1100") {
        // LoadS, Sz8
        myCurrExt.modMemWord := (
          myD2hBus.data(
            (7.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrExt.modMemWord.getWidth).asUInt
        )
      }
      is (M"1101") {
        // LoadS, Sz16
        myCurrExt.modMemWord := (
          myD2hBus.data(
            (15.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrExt.modMemWord.getWidth).asUInt
        )
      }
      is (M"1110") {
        // LoadS, Sz32
        myCurrExt.modMemWord := (
          myD2hBus.data(
            (31.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrExt.modMemWord.getWidth).asUInt
        )
      }
      is (M"1111") {
        // LoadS, Sz64
        myCurrExt.modMemWord := (
          myD2hBus.data(
            (63.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrExt.modMemWord.getWidth).asUInt
        )
      }
      default {
      }
    }
    when (
      (
        if (cfg.optScoreboard) (
          cLink.up.isValid
          || rCurrWbPayloadOuterIdx.lsb
        ) else (
          cLink.up.isValid
        )
      )
      && !myWbPayloadVec.head(1).outpDecodeExt.memAccessKind.asBits(1)
      && (
        //myD2hBus.valid
        myD2hBus.fire
      )
    ) {
      val myDecodeExt = myWbPayloadVec.head(1).outpDecodeExt
      val mapElem = myWbPayloadVec.head(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myWbPayloadVec.head(1).myExt(
            0
          )
        ) else (
          myWbPayloadVec.head(1).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      //myCurrExt.modMemWord := myDbus.recvData.word
      //myCurrExt.modMemWord := myD2hBus.data
      //myCurrExt.modMemWordValid.foreach(current => {
      //  current := (
      //    // TODO: support more destination GPRs
      //    //!myWbPayloadVec.head.gprIsZeroVec(0)
      //    True
      //  )
      //})
      for (idx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
        myCurrExt.modMemWordValid(idx) := (
          !myWbPayloadVec.head(1).gprIsZeroVec.last(idx)
        )
      }
    }
  }

  cLink.up.driveFrom(io.up)(
    con=(node, inp) => {
      //node(pwbInp) := inp
      myWbPayload(0) := inp
    }
  )
  cLink.down.ready := True

  //if (cfg.optScoreboard) {
  //  when (io.myRegFileWrPulse.fire) {
  //    rWbPayloadOuterIdx.lsb := !rWbPayloadOuterIdx.lsb
  //  }
  //}
  def setRegFileWrPulseEtc(
    someMyWbPayload: Vec[SnowHousePipePayload],
  ): Unit = {
    io.myRegFileWrPulse.valid := (
      (
        if (cfg.optScoreboard) (
          cLink.up.isFiring
          || myLcvDbusArea.myD2hBus.fire
        ) else (
          cLink.up.isFiring
        )
      )
      && !someMyWbPayload(1).gprIsZeroVec.last.last
      && !someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
      && {
        val myDecodeExt = someMyWbPayload(1).outpDecodeExt
        val mapElem = someMyWbPayload(1).gprIdxToMemAddrIdxMap(0)
        val myCurrExt = (
          if (!mapElem.haveHowToSetIdx) (
            someMyWbPayload(1).myExt(
              0
            )
          ) else (
            someMyWbPayload(1).myExt(
              mapElem.howToSetIdx
            )
          )
        )
        //myCurrExt.modMemWord := myDbus.recvData.word
        //someMyWbPayload(1).
        myCurrExt.modMemWordValid(0)
      }
    )
    io.myRegFileWrPulse.addr := (
      someMyWbPayload(1).gprIdxVec.last
    )
    io.myRegFileWrPulse.data := {
      val myDecodeExt = someMyWbPayload(1).outpDecodeExt
      val mapElem = someMyWbPayload(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          someMyWbPayload(1).myExt(
            0
          )
        ) else (
          someMyWbPayload(1).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      //myCurrExt.modMemWord := myDbus.recvData.word
      //someMyWbPayload(1).
      myCurrExt.modMemWord
    }
    if (io.dbgInfo != null) {
      io.dbgInfo.regFileWriteData := io.myRegFileWrPulse.data
      io.dbgInfo.regFileWriteAddr := io.myRegFileWrPulse.addr
      io.dbgInfo.regFileWriteEnable := io.myRegFileWrPulse.fire
      io.dbgInfo.laggingRegPcAtRegFileWrite := (
        someMyWbPayload(1).laggingRegPc.resize(cfg.mainWidth bits)
      )
      io.dbgInfo.shouldIgnoreInstrAtRegFileWrite := (
        someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
        || !cLink.up.isFiring
      )
      io.dbgInfo.myPsIdBubbleAtRegFileWrite := (
        someMyWbPayload(1).instrCnt.myPsIdBubble.last
        || !cLink.up.isFiring
      )
      io.dbgInfo.encInstrAtRegFileWrite := (
        someMyWbPayload(1).encInstr.payload
      )
      io.dbgInfo.immAtRegFileWrite := (
        someMyWbPayload(1).imm.last
      )
      io.dbgInfo.rdMemWordAtRegFileWrite := (
        someMyWbPayload(1).myExt(0).rdMemWord
      )
      io.dbgInfo.gprIdxVecAtRegFileWrite := (
        someMyWbPayload(1).gprIdxVec
      )
    }
  }

  if (cfg.optScoreboard) {
    when (
      RegNext(
        ( 
          myLcvDbusArea.myD2hBus.fire
          && rCurrWbPayloadOuterIdx.lsb
        ),
        init=False
      )
    ) {
      myWbPayloadVec.head := (
        RegNext(
          myWbPayloadVec.last,
          init=myWbPayloadVec.last.getZero,
        )
      )
      setRegFileWrPulseEtc(myWbPayloadVec.head)
    } otherwise {
      setRegFileWrPulseEtc(myWbPayload)
    }

    when (
      myLcvDbusArea.myD2hBus.fire
    ) {
      rScoreboardStallCnt := 0
    } elsewhen (
      rScoreboardStallCnt >= cfg.optMaxNumScoreboardInstrs
      && rCurrWbPayloadOuterIdx.lsb
    ) {
      cLink.duplicateIt()
    } elsewhen (
      cLink.up.isFiring
      && rCurrWbPayloadOuterIdx.lsb
    ) {
      rScoreboardStallCnt := rScoreboardStallCnt + 1
    }
  } else {
    setRegFileWrPulseEtc(myWbPayload)
  }

  Builder(linkArr)
  //--------
}
