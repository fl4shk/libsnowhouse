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


case class SnowHouseForFmaxScoreboardIo(
  cfg: SnowHouseConfig,
) extends Bundle {
  require(
    cfg.optScoreboard
  )

  val gprIdxVec = (
    in(
      //Vec.fill(cfg.numMultiIssue)(
        Vec.fill(cfg.maxNumGprsPerInstr)(
          UInt(log2Up(cfg.numGprs) bits)
        )
      //)
    )
  )
  val myTempOpMayNeedHazardCheck = (
    in(
      Bool()
    )
  )
  val issue = (
    //Vec.fill(cfg.numMultiIssue)(
      Stream(
        UInt(cfg.optScoreboardTagWidth bits)
      )
    //)
  )

  val commit = (
    //Vec.fill(cfg.numMultiIssue)(
      Stream(
        UInt(cfg.optScoreboardTagWidth bits)
      )
    //)
  )

  //for (idx <- 0 until cfg.numMultiIssue) {
  //  master(issue(idx))
  //  slave(commit(idx))
  //}
  master(issue)
  slave(commit)

  //commit.foreach(item => {
  //  slave(item)
  //})
}
case class SnowHouseForFmaxScoreboard(
  cfg: SnowHouseConfig,
) extends Component {
  require(
    cfg.optScoreboard
  )

  val io = SnowHouseForFmaxScoreboardIo(cfg=cfg)

  case class MyInfo(
  ) extends Bundle {
    val hazardValid = Bool()
    //def fire = hazardValid
    val allocValid = Bool()

    val gprIdxVec = (
      Vec.fill(cfg.maxNumGprsPerInstr)(
        UInt(log2Up(cfg.numGprs) bits)
      )
    )
  }
  val rMyInfoVec = (
    Vec.fill(cfg.optMaxNumScoreboardInstrs)({
      val temp = (
        //Vec.fill(cfg.numMultiIssue)(
          Reg(MyInfo())
        //)
      )
      //temp.foreach(item => item.init(item.getZero))
      temp.init(temp.getZero)
      temp
    })
  )


  val myIssueHazardCheckVecInnerSize = (
    //(io.gprIdxVec.size - 1) * 2 + 1
    //io.gprIdxVec.size + 3
    //io.gprIdxVec.size - 1
    io.gprIdxVec.size
  )
  val tempHaveIssueHazardAddrCheckVec = (
    // RAW/WAW hazards
    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
      Vec.fill(
        //io.gprIdxVec.size + 2
        myIssueHazardCheckVecInnerSize
      )(
        Bool()
      )
    )
  )

  val myCommitHazardCheckVecInnerSize = (
    // WAR hazards
    io.gprIdxVec.size - 1
  )
  val tempHaveCommitHazardAddrCheckVec = (
    // 
    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
      Vec.fill(myCommitHazardCheckVecInnerSize)(
        Bool()
      )
    )
  )

  for (
    idx <- 0 until myIssueHazardCheckVecInnerSize//io.gprIdxVec.size + 2
    //idx <- 0 until upPayload.gprIdxVec.size - 1
  ) {
    if (idx < io.gprIdxVec.size - 1) {
      val tempRegIdx = io.gprIdxVec(idx)
      for (jdx <- 0 until cfg.optMaxNumScoreboardInstrs) {
        tempHaveIssueHazardAddrCheckVec(jdx)(idx) := (
          (
            //tempRegIdx === myHistLastGprIdx(jdx + 1).last
            tempRegIdx === rMyInfoVec(jdx).gprIdxVec.last
            && tempRegIdx.orR // check for non-zero
            && rMyInfoVec(jdx).hazardValid
            && rMyInfoVec(jdx).allocValid
          )
        )
      }
    } 
    else { // if (idx >= upPayload.gprIdxVec.size - 1)
      val tempRegIdx = io.gprIdxVec.last
      for (jdx <- 0 until tempHaveIssueHazardAddrCheckVec.size) {
        tempHaveIssueHazardAddrCheckVec(jdx)(idx) := (
          (
            //tempRegIdx === myHistLastGprIdx(jdx + 1)(idx % 3)

            //tempRegIdx === rMyInfoVec(jdx).gprIdxVec(
            //  idx % (io.gprIdxVec.size - 1)
            //)
            tempRegIdx === rMyInfoVec(jdx).gprIdxVec.last
            && tempRegIdx.orR // check for non-zero
            && rMyInfoVec(jdx).hazardValid
            && rMyInfoVec(jdx).allocValid
          )
        )
      }
    }
  }
  for (idx <- 0 until myCommitHazardCheckVecInnerSize) {
    val tempRegIdx = (
      rMyInfoVec(io.commit.payload).gprIdxVec(idx)
      //rMyInfoVec(io.commit.payload).gprIdxVec.last
    )
    for (jdx <- 0 until cfg.optMaxNumScoreboardInstrs) {
      tempHaveCommitHazardAddrCheckVec(jdx)(idx) := (
        //tempRegIdx === myHistLastGprIdx(jdx + 1).last
        //tempRegIdx === rMyInfoVec(jdx).gprIdxVec(idx)
        tempRegIdx === rMyInfoVec(jdx).gprIdxVec.last
        && rMyInfoVec(jdx).gprIdxVec.last.orR // check for non-zero
        && rMyInfoVec(jdx).hazardValid
        && rMyInfoVec(jdx).allocValid
        && io.commit.payload =/= jdx
        && io.commit.valid
      )
    }
  }
  io.commit.ready := (
    io.commit.valid && !tempHaveCommitHazardAddrCheckVec.asBits.orR
  )

  val myInfoAllocValidVec = (
    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
      Bool()
    )
    //Vec(rMyInfoVec.reverse.map(item => item.hazardValid))
  )

  for (jdx <- 0 until cfg.optMaxNumScoreboardInstrs) {
    when (io.commit.fire && io.commit.payload === jdx) {
      //myInfoAllocValidVec(jdx) := False
      //tempHaveIssueHazardAddrCheckVec(jdx).foreach(
      //  item => (
      //    item := False
      //  )
      //)
      rMyInfoVec(jdx).allocValid := False
      rMyInfoVec(jdx).hazardValid := False
    } otherwise {
      //myInfoAllocValidVec(jdx) := rMyInfoVec(jdx).allocValid
    }
    myInfoAllocValidVec(jdx) := rMyInfoVec(jdx).allocValid
  }

  def bitscan(
    x: UInt
  ): UInt = (
    x & ~(x - 1)
  )

// >>> for x in range(8):
// ...     print(x, bin(x), bin(x ^ 0x7), bin(bitscan(x ^ 0x7)))
// ...     
// 0 0b0 0b111 0b1
// 1 0b1 0b110 0b10
// 2 0b10 0b101 0b1
// 3 0b11 0b100 0b100
// 4 0b100 0b11 0b1
// 5 0b101 0b10 0b10
// 6 0b110 0b1 0b1
// 7 0b111 0b0 0b0

// >>> for idx in range(size):
// ...     print(idx, ("-" * (size - idx - 1) + "1" + ("0" * idx)))
// ...     
// 0 ---1
// 1 --10
// 2 -100
// 3 1000
  switch (
    //io.issue.ready
    //## 
    bitscan(~myInfoAllocValidVec.asBits.asUInt)
  ) {
    val size = myInfoAllocValidVec.size
    for (idx <- 0 until size) {
      is (MaskedLiteral(
        //"1" + 
        ("-" * (size - idx - 1) + "1" + ("0" * idx))
      )) {
        // fast-ish (regarding fmax) search to implement the free list
        // search
        io.issue.valid := (
          //True
          !tempHaveIssueHazardAddrCheckVec.asBits.orR
        )
        io.issue.payload := (
          RegNext(io.issue.payload, init=io.issue.payload.getZero)
        )
        when (io.issue.fire) {
          io.issue.payload := idx
          rMyInfoVec(idx).hazardValid := (
            io.myTempOpMayNeedHazardCheck
            //True
          )
          rMyInfoVec(idx).allocValid := (
            True
          )
          rMyInfoVec(idx).gprIdxVec := io.gprIdxVec
        }
      }
    }
    default {
      io.issue.valid := False
      //io.issue.payload := 0x0
      io.issue.payload := (
        RegNext(io.issue.payload, init=io.issue.payload.getZero)
      )
    }
  }
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
  val myScoreboardCommmit = (
    cfg.optScoreboard
  ) generate (
    slave(Stream(
      UInt(cfg.optScoreboardTagWidth bits)
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

  val scoreboard = (
    cfg.optScoreboard
  ) generate (
    SnowHouseForFmaxScoreboard(cfg=cfg)
  )
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

  if (cfg.optScoreboard) {
    scoreboard.io.myTempOpMayNeedHazardCheck := (
      innerPsId.myTempOpMayNeedHazardCheck
    )
    scoreboard.io.issue.ready := (
      //cLink.up.isFiring // cLink.down.isFiring
      cLink.down.isFiring
      //cLink.down.isFiring
      //cLink.up.isValid
      //&& cLink.down.isReady
    )
    scoreboard.io.gprIdxVec := innerPsId.upPayload(1).gprIdxVec
    innerPsId.upPayload(1).instrCnt.scoreboardTag := (
      scoreboard.io.issue.payload
    )
    //innerPsId.upPayload(1).tempUpMod
    when (!scoreboard.io.issue.valid) {
      cLink.duplicateIt()
      cLink.down(pIdOutp).setAsBubbleMain()
      //innerPsId.upPayload(1).myDoHaveHazardAddrCheckVec.foreach(
      //  item => {
      //    item := True
      //  }
      //)
      //innerPsId.upPayload(1).myDoHaveHazardAddrCheckVec.head := (
      //  True
      //)
    }
    scoreboard.io.commit << io.myScoreboardCommmit
  }

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

case class SnowHouseForFmaxPsWbCommmitEtc(
  cfg: SnowHouseConfig
) extends Bundle {
  val myRegFileWrPulse = (
    master(
      Flow(
        PipeSimpleDualPortMemDrivePayload(
          dataType=UInt(cfg.mainWidth bits),
          wordCount=cfg.regFileCfg.wordCountArr(0),
        )
      )
    )
  )
  val scoreboardTag = (
    cfg.optScoreboard
  ) generate (
    master(
      Stream(
        UInt(cfg.optScoreboardTagWidth bits)
      )
    )
  )
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
  val commitEtc = (
    SnowHouseForFmaxPsWbCommmitEtc(cfg=cfg)
  )
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
  val myWbValidVec = (
    Vec.fill(currWbPayloadOuterVecSize)(
      Bool()
    )
  )

  val myMemWbPayload = myWbPayloadVec.head
  val myNonMemWbPayload = myWbPayloadVec.last

  val myMemWbValid = myWbValidVec.head
  val myNonMemWbValid = myWbValidVec.last

  //val myCurrWbPayloadOuterIdxInfo = (
  //  cfg.optScoreboard
  //) generate (
  //  UInt(log2Up(currWbPayloadOuterVecSize) bits)
  //)

  //val rCurrWbPayloadOuterIdx = (
  //  cfg.optScoreboard
  //) generate ({
  //  val temp = Reg(
  //    //Flow(
  //      UInt(log2Up(currWbPayloadOuterVecSize) bits)
  //    //)
  //  )
  //  temp.init(temp.getZero)
  //  temp
  //  //UInt(log2Up(currWbPayloadOuterVecSize) bits)
  //})

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

  //def myWbPayload = (
  //  if (cfg.optScoreboard) (
  //    myWbPayloadVec(
  //      (
  //        //myCurrWbPayloadOuterIdxInfo
  //        //| rCurrWbPayloadOuterIdx
  //        //Mux(
  //        //  myCurrWbPayloadOuterIdxInfo.lsb,
  //        //  False.asUInt.resize(myCurrWbPayloadOuterIdxInfo.getWidth),
  //          rCurrWbPayloadOuterIdx
  //        //)
  //      )
  //    )
  //  ) else (
  //    myWbPayloadVec.head
  //  )
  //)

  myWbPayloadVec.foreach(item => {
    if (cfg.optScoreboard) {
      //item(0) := item(0).getZero
      item(1) := (
        RegNext(
          item(1),
          init=item(1).getZero
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

  myMemWbValid := (
    RegNext(myMemWbValid, init=myMemWbValid.getZero)
  )
  if (cfg.optScoreboard) {
    myNonMemWbValid := (
      RegNext(myNonMemWbValid, init=myNonMemWbValid.getZero)
    )
  }

  val myD2hBus = cloneOf(io.myLcvDbusD2hStm)
  //val rSeenD2hBusFire = Reg(Bool(), init=False)

  val rInstrCntMem = (
    Reg(cloneOf(myWbPayloadVec.head(1).instrCnt.mem))
    init(0x0)
  )
  val rInstrCntNonMem = (
    Reg(cloneOf(myWbPayloadVec.head(1).instrCnt.nonMem))
    init(0x0)
  )
  //when (myD2hBus.fire) {
  //  rSeenD2hBusFire := True
  //}
  val rSeenMyD2hBusFire = (
    cfg.optScoreboard
  ) generate (
    Reg(Bool(), init=False)
  )
  val stickyMyD2hBusFire = (
    if (cfg.optScoreboard) (
      myD2hBus.fire
      || rSeenMyD2hBusFire
    ) else (
      myD2hBus.fire
    )
  )
  if (cfg.optScoreboard) {
    when (myD2hBus.fire) {
      rSeenMyD2hBusFire := True
    }
    when (io.commitEtc.scoreboardTag.fire) {
      rSeenMyD2hBusFire := False
    }

    when (
      RegNext(
        (
          //io.myRegFileWrPulse.fire
          (
            //myD2hBus.fire
            ////&& myMemWbValid
            //|| rSeenMyD2hBusFire
            stickyMyD2hBusFire
          )
          && io.commitEtc.scoreboardTag.fire
        ),
        init=False
      )
    ) {
      myMemWbValid := False
    }
    when (
      RegNext(
        (
          //io.myRegFileWrPulse.fire
          //cLink.up.isFiring
          myNonMemWbValid
          && io.commitEtc.scoreboardTag.fire
        ),
        init=False
      )
    ) {
      myNonMemWbValid := False
    }

    when (
      cLink.up.isValid
      && myWbPayloadVec.head(0).outpDecodeExt.opIsMemAccess(0)
      && !myWbPayloadVec.head(0).instrCnt.myPsIdBubble(0)
      && (
        RegNext(
          (
            !myMemWbValid
            || (
              //|| myD2hBus.fire
              //|| rSeenMyD2hBusFire
              stickyMyD2hBusFire
              && io.commitEtc.scoreboardTag.fire
            )
          ),
          init=False
        )
      )
    ) {
      myMemWbValid := True
      myMemWbPayload(1) := myMemWbPayload(0)
      //rInstrCntMem := myWbPayloadVec.head(1).instrCnt.mem
    }
    //when (
    //  //cLink.up.isFiring
    //  //&& myWbPayloadVec.head(0).outpDecodeExt.opIsMemAccess(0)
    //  //&& (
    //  //  RegNext(
    //  //    (
    //  //      !myMemWbValid
    //  //      || myD2hBus.fire
    //  //    ),
    //  //    init=False
    //  //  )
    //  //)
    //  //cLink.up.isValid
    //  //&& myWbPayloadVec.head(0).outpDecodeExt.opIsMemAccess(0)
    //  (
    //    //myD2hBus.fire
    //    //|| rSeenMyD2hBusFire
    //    stickyMyD2hBusFire
    //  )
    //  && io.commitEtc.scoreboardTag.fire
    //) {
    //  //myMemWbValid := True
    //  //myMemWbPayload(1) := myMemWbPayload(0)
    //  rInstrCntMem := rInstrCntMem + 1 //myWbPayloadVec.head(1).instrCnt.mem
    //}

    //when (
    //  cLink.up.isValid
    //  && myMemWbValid
    //  //&& !myD2hBus.fire
    //  && (
    //    rInstrCntMem =/= myWbPayloadVec.head(0).instrCnt.mem
    //  )
    //) {
    //  cLink.duplicateIt()
    //}

    when (
      cLink.up.isValid
      && !myWbPayloadVec.head(0).outpDecodeExt.opIsMemAccess(0)
      && !myWbPayloadVec.head(0).instrCnt.myPsIdBubble(0)
      //&& (
      //  RegNext(
      //    (
      //      !myNonMemWbValid
      //      || (
      //        cLink.up.isFiring
      //      )
      //    ),
      //    init=False
      //  )
      //)
    ) {
      myNonMemWbValid := True
      myNonMemWbPayload(1) := myNonMemWbPayload(0)
    }
  } else {
    when (cLink.up.isValid) {
      myWbPayloadVec.head(1) := myWbPayloadVec.head(0)
    }
  }

  //if (cfg.optScoreboard) {
  //  when (cLink.up.isValid) {
  //    myWbPayload(1) := myWbPayload(0)
  //  }
  //  when (
  //    RegNext(
  //      ( 
  //        myD2hBus.fire
  //        //&& rCurrWbPayloadOuterIdx.lsb
  //      ),
  //      init=False
  //    )
  //    //&& (
  //    //  !rCurrWbPayloadOuterIdx.lsb
  //    //)
  //  ) {
  //    when (
  //      fell(
  //        rCurrWbPayloadOuterIdx.lsb
  //      )
  //    ) {
  //      myWbPayloadVec.head(1) := (
  //        RegNext(
  //          myWbPayloadVec.last(1),
  //          init=myWbPayloadVec.last(1).getZero,
  //        )
  //      )
  //    } otherwise {
  //      myWbPayloadVec.head(1) := myWbPayloadVec.head(1).getZero
  //    }
  //    myWbPayloadVec.last := myWbPayloadVec.last.getZero
  //    //setRegFileWrPulseEtc(myWbPayloadVec.head)
  //  } otherwise {
  //  }
  //} else {
  //  when (cLink.up.isValid) {
  //    myWbPayload(1) := myWbPayload(0)
  //  }
  //}


  val myLcvDbusArea = new Area {
    //myDbusIo.myDbusExtraValid := (
    //  cWb.up.isValid
    //  && myWbPayload.outpDecodeExt.opIsMemAccess.last
    //)
    //myD2hBus <-/< io.lcvDbus.d2hBus
    myD2hBus << io.myLcvDbusD2hStm
    myD2hBus.ready := False

    //myCurrWbPayloadOuterIdxInfo.lsb := (
    //  myD2hBus.fire
    //)

    when (
      (
        if (cfg.optScoreboard) (
          //cLink.up.isValid
          //|| rCurrWbPayloadOuterIdx.lsb
          //&& 
          myMemWbValid
        ) else (
          cLink.up.isValid
        )
      )
      && myMemWbPayload(1).outpDecodeExt.opIsMemAccess.last
    ) {
      myD2hBus.ready := True
    }

    //if (!cfg.optScoreboard) {
      when (
        (
          if (cfg.optScoreboard) (
            // TODO: maybe try `isValid` later (for fmax)?
            //cLink.up.isValid
            //&& !rCurrWbPayloadOuterIdx.lsb
            //RegNext(
            //  !myMemWbValid,
            //  init=False
            //)
            //!rose(myMemWbValid)
            //&& !myNonMemWbValid
            myMemWbValid
          ) else (
            cLink.up.isValid
          )
        )
        //cLink.up.isValid
        && myMemWbPayload(1).outpDecodeExt.opIsMemAccess.last
        && !myD2hBus.valid
      ) {
        if (cfg.optScoreboard) {
          //rCurrWbPayloadOuterIdx.lsb := True
          //cLink.duplicateIt()
        } else {
          cLink.duplicateIt()
        }
      }
    //}
    if (cfg.optScoreboard) {
      //when (
      //  (
      //    //cLink.up.isValid
      //    //|| 
      //    rCurrWbPayloadOuterIdx.lsb
      //  )
      //  && myMemWbPayload(1).outpDecodeExt.opIsMemAccess.last
      //  && (
      //    // this is checking for `myD2hBus.fire`
      //    myD2hBus.valid
      //  )
      //) {
      //  //rCurrWbPayloadOuterIdx.lsb := False
      //  cLink.duplicateIt()
      //}
    }
    switch (
      (
        (
          if (cfg.optScoreboard) (
            //cLink.up.isValid
            //|| rCurrWbPayloadOuterIdx.lsb
            myMemWbValid
          ) else (
            cLink.up.isValid
          )
        )
        && myMemWbPayload(1).outpDecodeExt.opIsMemAccess.head
        && !myMemWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
        && (
          //myD2hBus.valid
          myD2hBus.fire
          //stickyMyD2hBusFire
        )
      )
      ## myMemWbPayload(1).outpDecodeExt.memAccessKind.asBits(0)
      ## myMemWbPayload(1).outpDecodeExt.memAccessSubKind.asBits
    ) {
      //--------
      // This stuff might need to be changed for the purposes of
      // atomic operations that are larger than `cfg.mainWidth`.
      // It's currently limited to at max 32-bit values, for example, on a
      // 32-bit `cfg.mainWidth` CPU. More work will be needed later.
      //--------
      val myDecodeExt = myMemWbPayload(1).outpDecodeExt
      val mapElem = myMemWbPayload(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myMemWbPayload(1).myExt(
            0
          )
        ) else (
          myMemWbPayload(1).myExt(
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
          //cLink.up.isValid
          //|| 
          myMemWbValid //rCurrWbPayloadOuterIdx.lsb
        ) else (
          cLink.up.isValid
        )
      )
      && !myMemWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
      && (
        //myD2hBus.valid
        myD2hBus.fire
        //stickyMyD2hBusFire
      )
    ) {
      val myDecodeExt = myMemWbPayload(1).outpDecodeExt
      val mapElem = myMemWbPayload(1).gprIdxToMemAddrIdxMap(0)
      val myCurrExt = (
        if (!mapElem.haveHowToSetIdx) (
          myMemWbPayload(1).myExt(
            0
          )
        ) else (
          myMemWbPayload(1).myExt(
            mapElem.howToSetIdx
          )
        )
      )
      //myCurrExt.modMemWord := myDbus.recvData.word
      //myCurrExt.modMemWord := myD2hBus.data
      //myCurrExt.modMemWordValid.foreach(current => {
      //  current := (
      //    // TODO: support more destination GPRs
      //    //!myMemWbPayload.gprIsZeroVec(0)
      //    True
      //  )
      //})
      for (idx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
        myCurrExt.modMemWordValid(idx) := (
          !myMemWbPayload(1).gprIsZeroVec.last(idx)
        )
      }
    }
  }

  cLink.up.driveFrom(io.up)(
    con=(node, inp) => {
      //node(pwbInp) := inp
      //myWbPayload(0) := inp
      if (cfg.optScoreboard) {
        myMemWbPayload(0) := inp
        myNonMemWbPayload(0) := inp
      } else {
        myWbPayloadVec.head(0) := inp
      }
    }
  )
  cLink.down.ready := True

  //if (cfg.optScoreboard) {
  //  when (io.myRegFileWrPulse.fire) {
  //    rWbPayloadOuterIdx.lsb := !rWbPayloadOuterIdx.lsb
  //  }
  //}
  def setCommitEtc(
    someMyWbPayload: Vec[SnowHousePipePayload],
    isMem: Boolean,
  ): Unit = {
    val myNonMemRegFileWrPulseValidPartial = (
      cfg.optScoreboard
    ) generate (
      myNonMemWbValid
      //&& io.commitEtc.scoreboardTag.fire
      //&& cLink.up.isFiring
      //cLink.up.isValid
      ////&& myMemWbValid
      ////&& !myD2hBus.fire
      //&& !myD2hBus.fire
      //&& (
      //  !myMemWbValid
      //  || myNonMemWbValid
      //)
      && (
        rInstrCntNonMem === myWbPayloadVec.head(0).instrCnt.nonMem
      )
      && (
        !myNonMemWbPayload(1).instrCnt.myPsIdBubble.head
      )
    )
    io.commitEtc.myRegFileWrPulse.valid := (
        //if (
        //  cfg.optScoreboard
        //  && !isMem
        //) (
        //  cLink.up.isFiring
        //  //|| myD2hBus.fire
        //) else (
        //  //cLink.up.isFiring
        //  myD2hBus.fire
        //)
      (
        if (cfg.optScoreboard) (
          io.commitEtc.scoreboardTag.fire
          && (
            if (isMem) (
              //myMemWbValid
              ////True
              //&& 
              (
                //myMemWbPayload(1).outpDecodeExt.opIsMemAccess(0)
                //&& 
                !myMemWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
              )
            ) else (
              myNonMemRegFileWrPulseValidPartial
            )
          )
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
    if (
      cfg.optScoreboard
      && !isMem
    ) {
      when (
        //myNonMemWbValid
        //&& cLink.up.isFiring
        //&& (
        //  rInstrCntNonMem === myWbPayloadVec.head(0).instrCnt.nonMem
        //)
        myNonMemRegFileWrPulseValidPartial
      ) {
        rInstrCntNonMem := rInstrCntNonMem + 1
      }
    }
    io.commitEtc.myRegFileWrPulse.addr := (
      someMyWbPayload(1).gprIdxVec.last
    )
    io.commitEtc.myRegFileWrPulse.data := {
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
    if (cfg.optScoreboard) {
      //val rSeenUpIsFiring = (
      //  isMem
      //) generate (
      //  Reg(Bool(), init=False)
      //)
      io.commitEtc.scoreboardTag.valid := (
        (
          if (isMem) (
            (
              //myD2hBus.fire
              //|| (
              //  cLink.up.isFiring
              //  && myMemWbValid
              //  //&& !myNonMemWbValid
              //  //&& someMyWbPayload(1).outpDecodeExt.opIsMemAccess(0)
              //)
              //True
              //!myMemWbPayload(1).instrCnt.myPsIdBubble.last
              //True
              //!myMemWbPayload(1).instrCnt.myPsIdBubble.last
              myMemWbValid
            )
          ) else (
            //cLink.up.isFiring
            //&& 
            cLink.up.isValid
            && myNonMemWbValid
            //&& !myNonMemWbPayload(1).instrCnt.myPsIdBubble.last
          )
        )
      )
      io.commitEtc.scoreboardTag.payload := (
        someMyWbPayload(1).instrCnt.scoreboardTag
      )
    }
    if (io.dbgInfo != null) {
      io.dbgInfo.regFileWriteData := (
        io.commitEtc.myRegFileWrPulse.data
      )
      io.dbgInfo.regFileWriteAddr := (
        io.commitEtc.myRegFileWrPulse.addr
      )
      io.dbgInfo.regFileWriteEnable := (
        io.commitEtc.myRegFileWrPulse.fire
        && (
          if (!isMem) (
            (
              cLink.up.isFiring
              && !myNonMemWbPayload(1).instrCnt.myPsIdBubble.last
            )
          ) else (
            True
          )
        )
      )
      io.dbgInfo.laggingRegPcAtRegFileWrite := (
        someMyWbPayload(1).laggingRegPc.resize(cfg.mainWidth bits)
      )
      io.dbgInfo.shouldIgnoreInstrAtRegFileWrite := (
        someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
        || (
          if (!isMem) (
            !cLink.up.isFiring
            || !myNonMemWbValid
          ) else (
            False
          )
        )
      )
      io.dbgInfo.myPsIdBubbleAtRegFileWrite := (
        someMyWbPayload(1).instrCnt.myPsIdBubble.last
        //|| !cLink.up.isFiring
        || (
          if (!isMem) (
            !cLink.up.isFiring
            || !myNonMemWbValid
          ) else (
            False
          )
        )
        //&& (
        //  if (!isMem) (
        //    cLink.up.isFiring
        //  ) else (
        //    True
        //  )
        //)
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
      //myD2hBus.fire
      //myD2hBus.fire
      //|| rSeenMyD2hBusFire
      stickyMyD2hBusFire
      //|| (
      //  cLink.up.isFiring
      //  && myMemWbValid
      //  && myMemWbPayload(0).instrCnt.myPsIdBubble(0)
      //  //&& !myNonMemWbValid
      //  //&& someMyWbPayload(1).outpDecodeExt.opIsMemAccess(0)
      //)
    ) {
      setCommitEtc(myMemWbPayload, isMem=true)
    } otherwise {
      setCommitEtc(myNonMemWbPayload, isMem=false)
    }

    when (
      ( 
        (
          (
            //myD2hBus.fire
            //|| rSeenMyD2hBusFire
            stickyMyD2hBusFire
          )
          && myMemWbValid
          && !myMemWbPayload(1).instrCnt.myPsIdBubble.head
          //&& !myMemWbPayload(1).instrCnt.shouldIgnoreInstr.head
        )
        //|| (
        //  //(
        //  //  !(
        //  //    myD2hBus.fire
        //  //    || rSeenMyD2hBusFire
        //  //  ) 
        //  //)
        //  //&& 
        //  myNonMemWbValid
        //  && !myNonMemWbPayload(1).instrCnt.myPsIdBubble.last
        //  //&& !myNonMemWbPayload(1).instrCnt.shouldIgnoreInstr.last
        //)
      )
      && io.commitEtc.scoreboardTag.valid
      && !io.commitEtc.scoreboardTag.ready//fire
    ) {
      cLink.duplicateIt()
    }

    //when (
    //  myD2hBus.fire
    //) {
    //  rScoreboardStallCnt := 0
    //  //rCurrWbPayloadOuterIdx.lsb := False
    //  //when (rCurrWbPayloadOuterIdx.lsb) {
    //  //  cLink.duplicateIt()
    //  //}
    //  //myMemWbValid := False
    //} elsewhen (
    //  !myNonMemWbPayload(1).instrCnt.shouldIgnoreInstr.head
    //  //&& !myWbPayloadVec.last(1).instrCnt.myPsIdBubble.head
    //  && rScoreboardStallCnt >= cfg.optMaxNumScoreboardInstrs - 1
    //  //&& rCurrWbPayloadOuterIdx.lsb
    //  && myNonMemWbValid
    //  && myMemWbValid
    //) {
    //  cLink.duplicateIt()
    //} elsewhen (
    //  !myNonMemWbPayload(1).instrCnt.shouldIgnoreInstr.last
    //  //&& !myNonMemWbPayload(1).instrCnt.myPsIdBubble.last
    //  && cLink.up.isFiring
    //  //&& rCurrWbPayloadOuterIdx.lsb
    //  && myNonMemWbValid
    //  && myMemWbValid
    //) {
    //  rScoreboardStallCnt := rScoreboardStallCnt + 1
    //}
  } else {
    setCommitEtc(myWbPayloadVec.head, isMem=false)
  }

  Builder(linkArr)
  //--------
}
