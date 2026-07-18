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

case class SnowHouseForFmaxPipeStagePostIdPreExIo(
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
  //dualIssueIdx: Int,
) extends Component {
  //--------
  val io = SnowHouseForFmaxPipeStagePostIdPreExIo(cfg=cfg)
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

  val dualIssueIdx = 0
  val innerPsPostIdPreExArr = SnowHousePipeStagePostIdPreEx(
    cfg=cfg,
    outp=myOutp,
    inp=myInp,
    //link=cLink,
    upIsFiring=cLink.up.isFiring,
    myBranchMispredictEtc=io.myBranchMispredictEtc,
    dualIssueIdx=dualIssueIdx,
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
  cfg: SnowHouseConfig,
  //dualIssueIdx: Int,
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

  val dualIssueIdx = 0
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
    //forFmaxRegFileWrPulseArr=Array(
    //  io.myRegFileWrPulse
    //),
    otherPsExOutpMmw=null,
    otherPsExOutpMmwValidEtc=null,
    dualIssueIdx=(
      dualIssueIdx
    ),
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
  val myWbPayload = (
    Vec.fill(2)(
      SnowHousePipePayload(cfg=cfg)
    )
  )
  myWbPayload(1) := (
    RegNext(
      myWbPayload(1),
      init=myWbPayload(1).getZero
    )
  )
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

    //psWbToEarlierStallRequest := False

    when (
      cLink.up.isValid
      && myWbPayload(1).outpDecodeExt.opIsMemAccess.last
    ) {
      myD2hBus.ready := True
    }
    when (
      cLink.up.isValid
      && myWbPayload(1).outpDecodeExt.opIsMemAccess.last
      && !myD2hBus.valid
    ) {
      //psWbToEarlierStallRequest := True
      cLink.duplicateIt()
    }
    switch (
      (
        cLink.up.isValid
        && myWbPayload(1).outpDecodeExt.opIsMemAccess.head
        && !myWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
        && (
          //myD2hBus.valid
          myD2hBus.fire
        )
      )
      ## myWbPayload(1).outpDecodeExt.memAccessKind.asBits(0)
      ## myWbPayload(1).outpDecodeExt.memAccessSubKind.asBits
    ) {
      //--------
      // This stuff might need to be changed for the purposes of
      // atomic operations that are larger than `cfg.mainWidth`.
      // It's currently limited to at max 32-bit values, for example, on a
      // 32-bit `cfg.mainWidth` CPU. More work will be needed later.
      //--------
      val myDecodeExt = myWbPayload(1).outpDecodeExt
      val mapElem = myWbPayload(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myWbPayload(1).myExt(
            0
          )
        ) else (
          myWbPayload(1).myExt(
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
      cLink.up.isValid
      && !myWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
      && (
        //myD2hBus.valid
        myD2hBus.fire
      )
    ) {
      val myDecodeExt = myWbPayload(1).outpDecodeExt
      val mapElem = myWbPayload(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myWbPayload(1).myExt(
            0
          )
        ) else (
          myWbPayload(1).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      //myCurrExt.modMemWord := myDbus.recvData.word
      //myCurrExt.modMemWord := myD2hBus.data
      //myCurrExt.modMemWordValid.foreach(current => {
      //  current := (
      //    // TODO: support more destination GPRs
      //    //!myWbPayload.gprIsZeroVec(0)
      //    True
      //  )
      //})
      for (idx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
        myCurrExt.modMemWordValid(idx) := (
          !myWbPayload(1).gprIsZeroVec.last(idx)
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

  io.myRegFileWrPulse.valid := (
    cLink.up.isFiring
    && !myWbPayload(1).gprIsZeroVec.last.last
    && !myWbPayload(1).instrCnt.shouldIgnoreInstr.last
    && {
      val myDecodeExt = myWbPayload(1).outpDecodeExt
      val mapElem = myWbPayload(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myWbPayload(1).myExt(
            0
          )
        ) else (
          myWbPayload(1).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      //myCurrExt.modMemWord := myDbus.recvData.word
      //myWbPayload(1).
      myCurrExt.modMemWordValid(0)
    }
  )
  io.myRegFileWrPulse.addr := (
    myWbPayload(1).gprIdxVec.last
  )
  io.myRegFileWrPulse.data := {
    val myDecodeExt = myWbPayload(1).outpDecodeExt
    val mapElem = myWbPayload(1).gprIdxToMemAddrIdxMap(0)
    val myCurrExt = (
      if (!mapElem.haveHowToSetIdx) (
        myWbPayload(1).myExt(
          0
        )
      ) else (
        myWbPayload(1).myExt(
          mapElem.howToSetIdx
        )
      )
    )
    //myCurrExt.modMemWord := myDbus.recvData.word
    //myWbPayload(1).
    myCurrExt.modMemWord
  }
  if (io.dbgInfo != null) {
    io.dbgInfo.regFileWriteData := io.myRegFileWrPulse.data
    io.dbgInfo.regFileWriteAddr := io.myRegFileWrPulse.addr
    io.dbgInfo.regFileWriteEnable := io.myRegFileWrPulse.fire
    io.dbgInfo.laggingRegPcAtRegFileWrite := (
      myWbPayload(1).laggingRegPc.resize(cfg.mainWidth bits)
    )
    io.dbgInfo.shouldIgnoreInstrAtRegFileWrite := (
      myWbPayload(1).instrCnt.shouldIgnoreInstr.last
      || !cLink.up.isFiring
    )
    io.dbgInfo.myPsIdBubbleAtRegFileWrite := (
      myWbPayload(1).instrCnt.myPsIdBubble.last
      || !cLink.up.isFiring
    )
    io.dbgInfo.encInstrAtRegFileWrite := (
      myWbPayload(1).encInstr.payload
    )
    io.dbgInfo.immAtRegFileWrite := (
      myWbPayload(1).imm.last
    )
    io.dbgInfo.rdMemWordAtRegFileWrite := (
      myWbPayload(1).myExt(0).rdMemWord
    )
    io.dbgInfo.gprIdxVecAtRegFileWrite := (
      myWbPayload(1).gprIdxVec
    )
  }

  Builder(linkArr)
  //--------
}
