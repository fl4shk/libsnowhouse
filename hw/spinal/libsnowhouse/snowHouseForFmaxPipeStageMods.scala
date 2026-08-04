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

case class SnowHouseScoreboardIssuePayload(
  cfg: SnowHouseConfig,
) extends Bundle {
  //val cntOverflow = Bool()

 // reorder buffer index
  val reorderBufIdx = UInt(cfg.optScoreboardReorderBufWidth bits)
  val nonFwdTag = UInt(cfg.optScoreboardReorderBufWidth bits)
  val fwdTag = UInt(cfg.optScoreboardReorderBufWidth bits)
  val nonBubbleTag = UInt(cfg.optScoreboardReorderBufWidth bits)
  //val tag = UInt(cfg.optScoreboardTagWidth bits)
}

//case class SnowHouseScoreboardReadGprsPayload(
//  cfg: SnowHouseConfig,
//) extends Bundle {
//  val gprIdxVec = (
//    Vec.fill(cfg.maxNumGprsPerInstr)(
//      UInt(log2Up(cfg.numGprs) bits)
//    )
//  )
//  val regPcSetItCnt = in(
//    Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
//      UInt(
//        //cfg.instrCntWidth bits
//        //2 bits
//        cfg.regPcSetItCntWidth bits
//      ) //Bool()
//    )
//  )
//  val tag = UInt(cfg.optScoreboardTagWidth bits)
//  val someNodeIsFiring = Bool()
//}

case class SnowHouseScoreboardCommitPayload(
  cfg: SnowHouseConfig,
) extends Bundle {
  //val tag = UInt(cfg.optScoreboardTagWidth bits)
  //val myGprIdx = Flow(UInt(log2Up(cfg.numGprs) bits))
  val gprIdxVec = Vec.fill(cfg.maxNumGprsPerInstr)(
    UInt(log2Up(cfg.numGprs) bits)
  )
  val myFwdValid = Bool()
  val myNonFwdValid = Bool()
  val nonFwdTag = UInt(cfg.optScoreboardReorderBufWidth bits)
  val fwdTag = UInt(cfg.optScoreboardReorderBufWidth bits)
  val opIsFwd = Bool()
  //val reorderBufInFlush = Bool()
  //val tag = UInt(cfg.optForFmaxCfg.get.myScoreboardTagWidth bits)
  //val isBubbleEtc = Bool()
}

//case class SnowHouseForFmaxScoreboardIo(
//  cfg: SnowHouseConfig,
//) extends Bundle {
//  require(
//    cfg.optScoreboard
//  )
//  //--------
//  val myBranchMispredictEtc = in(Bool())
//  val issueRegPcSetItCnt = in(
//    Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
//      UInt(
//        //cfg.instrCntWidth bits
//        //2 bits
//        cfg.regPcSetItCntWidth bits
//      ) //Bool()
//    )
//  )
//  //val regPcSetItCnt = in(
//  //--------
//  val issueGprIdxVec = (
//    in(
//      //Vec.fill(cfg.numMultiIssue)(
//        Vec.fill(cfg.maxNumGprsPerInstr)(
//          UInt(log2Up(cfg.numGprs) bits)
//        )
//      //)
//    )
//  )
//  val issueMyTempOpMayNeedHazardCheck = (
//    in(
//      Bool()
//    )
//  )
//
//  val issue = (
//    //Vec.fill(cfg.numMultiIssue)(
//      Stream(
//        //UInt(cfg.optScoreboardTagWidth bits)
//        SnowHouseScoreboardIssuePayload(cfg=cfg)
//      )
//    //)
//  )
//
//  //val readGprsPayload = (
//  //  in(
//  //    //Vec.fill(cfg.numMultiIssue)(
//  //      //Stream(
//  //        SnowHouseScoreboardReadGprsPayload(cfg=cfg)
//  //      //)
//  //    //)
//  //  )
//  //)
//
//  //val readGprsReady = (
//  //  out(Bool())
//  //)
//  val readGprs = (
//    Stream(SnowHouseScoreboardReadGprsPayload(cfg=cfg))
//  )
//
//  val reorderBufWrite = (
//    //Vec.fill(cfg.numMultiIssue)(
//      Stream(
//        //UInt(cfg.optScoreboardTagWidth bits)
//        SnowHouseScoreboardCommitPayload(cfg=cfg)
//      )
//    //)
//  )
//
//  //for (idx <- 0 until cfg.numMultiIssue) {
//  //  master(issue(idx))
//  //  slave(commit(idx))
//  //}
//  master(issue)
//  slave(readGprs)
//  slave(reorderBufWrite)
//
//  //commit.foreach(item => {
//  //  slave(item)
//  //})
//}
//
//case class SnowHouseForFmaxScoreboard(
//  cfg: SnowHouseConfig,
//) extends Component {
//  require(
//    cfg.optScoreboard
//  )
//  //--------
//  val io = SnowHouseForFmaxScoreboardIo(cfg=cfg)
//  //--------
//  val rMyPsExSetPcState = (
//    Vec.fill(2)(
//      Reg(Bool(), init=False)
//    )
//  )
//
//  //for (idx <- 0 until rMyPsExSetPcState.size) {
//  //  when (!rMyPsExSetPcState(idx)) {
//  //    when (io.myBranchMispredictEtc) {
//  //      rMyPsExSetPcState(idx) := True
//  //    }
//  //  } otherwise {
//  //    when (
//  //      if (idx == 0) (
//  //        io.issue.fire
//  //        && io.issueRegPcSetItCnt(0).lsb
//  //      ) else (
//  //        io.readGprs.fire
//  //        && io.readGprs.regPcSetItCnt(0).lsb
//  //      )
//  //    ) {
//  //      rMyPsExSetPcState(idx) := False
//  //    }
//  //  }
//  //}
//
//  //val myIssueSharedShouldIgnoreCond = (
//  //  !(
//  //    rMyPsExSetPcState.head
//  //    && !io.issueRegPcSetItCnt(1).lsb
//  //  )
//  //)
//  //val myReadGprsSharedShouldIgnoreCond = (
//  //  !(
//  //    rMyPsExSetPcState.last
//  //    && !io.readGprs.regPcSetItCnt(1).lsb
//  //  )
//  //)
//
//  val myInstrAgeWidth = 12//4//5//4//6//8//12
//  val myMaxInstrAge = (
//    // we flush the pipeline when this counter gets close to overflowing!
//    // it is assumed there are fewer pipeline stages
//    // than the subtract amount 
//    (1 << myInstrAgeWidth) - 1 - 32//2//1//8 //- //32//64
//  )
//
//  case class FlushInfoPayload(
//  ) extends Bundle {
//    //val instrAgeCnt = UInt(myInstrAgeWidth bits)
//    val dontCare = Bool()
//  }
//  val rFlushInfo = {
//    val temp = Reg(Flow(FlushInfoPayload()))
//    temp.init(temp.getZero)
//    temp
//  }
//
//  case class MyInfo(
//  ) extends Bundle {
//    //val hazardValid = Bool()
//    val issueHazardValid = Bool()
//    val readGprsHazardValid = Bool()
//    val readGprsHazardValidFwdLimit = Bool()
//    //def fire = hazardValid
//    val issueAllocValid = Bool()
//    val instrAge = UInt(myInstrAgeWidth bits) //cloneOf(rFlushInfo)
//
//    val gprIsNonZeroVec = (
//      Vec.fill(
//        cfg.maxNumGprsPerInstr
//        //1
//      )(
//        Bool()
//      )
//    )
//    val gprIdxVec = (
//      Vec.fill(cfg.maxNumGprsPerInstr)(
//        UInt(log2Up(cfg.numGprs) bits)
//      )
//    )
//  }
//  val rMyInfoVec = (
//    Vec.fill(cfg.optMaxNumScoreboardInstrs)({
//      val temp = (
//        //Vec.fill(cfg.numMultiIssue)(
//          Reg(MyInfo())
//        //)
//      )
//      //temp.foreach(item => item.init(item.getZero))
//      temp.init(temp.getZero)
//      temp
//    })
//  )
//
//
//  val myIssueHazardCheckVecInnerSize = (
//    //(io.gprIdxVec.size - 1) * 2 + 1
//    //io.gprIdxVec.size + 3
//    //io.gprIdxVec.size - 1
//    //(io.gprIdxVec.size - 1) * 2 + 1
//    //io.issueGprIdxVec.size
//    1
//  )
//  val tempHaveIssueHazardAddrCheckVec = (
//    // WAW hazards
//    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
//      Vec.fill(
//        //io.gprIdxVec.size + 2
//        myIssueHazardCheckVecInnerSize
//      )(
//        Bool()
//      )
//    )
//  )
//
//  val myReadGprsHazardCheckVecInnerSize = (
//    io.readGprs.gprIdxVec.size - 1
//  )
//
//  val tempHaveReadGprsHazardAddrCheckVec = (
//    // non-forwardable RAW hazards
//    // TODO: this should be switched to be computed in the "Issue" stage
//    // at some point (for fmax)
//    //--------
//    // perhaps instead it'd make sense to just add more pipeline stages
//    // (at least one)
//    // between `...ScoreboardReadGprs` and `...PreFwd`?
//    //--------
//    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
//      Vec.fill(
//        myReadGprsHazardCheckVecInnerSize
//      )(
//        Bool()
//      )
//    )
//  )
//  //val tempHaveReadGprsHazardAddrCheckFwdLimitVec = (
//  //  // TODO: this should be switched to be computed in the "Issue" stage
//  //  // at some point (for fmax)
//  //  cloneOf(
//  //    tempHaveReadGprsHazardAddrCheckVec
//  //  )
//  //)
//
//  val myCommitHazardCheckVecInnerSize = (
//    // WAR hazards
//    io.issueGprIdxVec.size - 1
//  )
//  val tempHaveCommitHazardAddrCheckVec = (
//    // 
//    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
//      Vec.fill(myCommitHazardCheckVecInnerSize)(
//        Bool()
//      )
//    )
//  )
//
//  for (
//    idx <- 0 until myIssueHazardCheckVecInnerSize//io.gprIdxVec.size + 2
//    //idx <- 0 until upPayload.gprIdxVec.size - 1
//  ) {
//    // WAW hazards
//    val tempRegIdx = io.issueGprIdxVec.last
//    for (jdx <- 0 until tempHaveIssueHazardAddrCheckVec.size) {
//      //tempHaveIssueHazardAddrCheckVec(jdx)(idx) := False
//      tempHaveIssueHazardAddrCheckVec(jdx)(idx) := (
//        //False
//        (
//          //tempRegIdx === myHistLastGprIdx(jdx + 1)(idx % 3)
//
//          //tempRegIdx === rMyInfoVec(jdx).gprIdxVec(
//          //  idx % io.gprIdxVec.size
//          //)
//          tempRegIdx === rMyInfoVec(jdx).gprIdxVec.last
//          //&& tempRegIdx.orR // check for non-zero
//          && rMyInfoVec(jdx).gprIsNonZeroVec.last
//          //&& (
//          //  rMyInfoVec(jdx).hazardValid
//          //  //|| io.myTempOpMayNeedHazardCheck
//          //)
//          && rMyInfoVec(jdx).issueHazardValid
//          && rMyInfoVec(jdx).issueAllocValid
//        )
//      )
//    }
//  }
//
//  for (idx <- 0 until myReadGprsHazardCheckVecInnerSize) {
//    // (non-forwardable) RAW hazards and also some kinds of WAR hazards
//    val tempRegIdx = io.readGprs.gprIdxVec(idx) //io.issueGprIdxVec(idx)
//    for (jdx <- 0 until cfg.optMaxNumScoreboardInstrs) {
//      val tempCmp = (
//        (
//          (
//            tempRegIdx === rMyInfoVec(jdx).gprIdxVec.last
//            && rMyInfoVec(jdx).readGprsHazardValid
//          )
//          || (
//            // technically this is a WAR hazard
//            rMyInfoVec(jdx).gprIdxVec(idx) === io.readGprs.gprIdxVec.last
//            //&& rMyInfoVec(io.readGprs.tag).issueHazardValid
//            && rMyInfoVec(jdx).issueHazardValid
//          )
//        )
//        //&& tempRegIdx.orR // check for non-zero
//        && rMyInfoVec(jdx).gprIsNonZeroVec.last
//        //&& (
//        //  rMyInfoVec(jdx).hazardValid
//        //  //|| io.myTempOpMayNeedHazardCheck
//        //)
//        && rMyInfoVec(jdx).issueAllocValid
//        && io.readGprs.tag =/= jdx
//      )
//      tempHaveReadGprsHazardAddrCheckVec(jdx)(idx) := (
//        tempCmp
//        //&& (
//        //  rMyInfoVec(jdx).readGprsHazardValid
//        //  //|| rMyInfoVec(io.readGprs.tag).issueHazardValid
//        //)
//      )
//      //tempHaveReadGprsHazardAddrCheckFwdLimitVec(jdx)(idx) := (
//      //  tempCmp
//      //  && (
//      //    //!rMyInfoVec(jdx).readGprsHazardValid
//      //    //&& 
//      //    rMyInfoVec(jdx).readGprsHazardValidFwdLimit
//      //    //|| rMyInfoVec(io.readGprs.tag).issueHazardValid
//      //  )
//      //)
//      //tempHaveReadGprsHazardAddrCheckVec(jdx)(idx) := (
//      //  (
//      //    //tempRegIdx === myHistLastGprIdx(jdx + 1).last
//      //    tempRegIdx === rMyInfoVec(jdx).gprIdxVec.last
//      //    //&& tempRegIdx.orR // check for non-zero
//      //    && rMyInfoVec(jdx).gprIsNonZeroVec.last
//      //    && (
//      //      // other "RAW" hazards will be handled via my implementation of
//      //      // fast forwarding!
//      //      rMyInfoVec(jdx).readGprsHazardValid
//      //      || rMyInfoVec(io.readGprs.tag).issueHazardValid
//      //      //rMyInfoVec(io.readGprs.tag).readGprsHazardValid
//      //      //|| io.myTempOpMayNeedHazardCheck
//      //      //|| (
//      //      //  io.readGprs.valid
//      //      //  && io.readGprs.tag === jdx
//      //      //  //&& tempHaveReadGprsHazardAddrCheckVec(jdx).orR
//      //      //  //&& rMyInfoVec(jdx).issueAllocValid
//      //      //  && rMyInfoVec(jdx).issueHazardValid
//      //      //)
//      //    )
//      //    && rMyInfoVec(jdx).issueAllocValid
//      //    //&& io.readGprs.valid
//      //  )
//      //)
//      when (
//        //io.readGprs.valid
//        //&& tempHaveReadGprsHazardAddrCheckVec(jdx).orR
//        //&& 
//        io.readGprs.fire
//        && io.readGprs.tag === jdx
//        && rMyInfoVec(jdx).issueAllocValid
//        //&& rMyInfoVec(jdx).issueHazardValid
//      ) {
//        rMyInfoVec(jdx).readGprsHazardValid := (
//          rMyInfoVec(jdx).issueHazardValid
//          //True
//        )
//        //rMyInfoVec(jdx).readGprsHazardValidFwdLimit := (
//        //  True
//        //)
//      }
//    }
//  }
//
//  //def myReadGprsInstrMayPassCntInitVal = (
//  //  cfg.optForFmaxPsExFwdSize - 3//2//3//2//1//2//1
//  //)
//  //def myReadGprsInstrMayPassCntInitVal = 2
//
//  //val rReadGprsInstrMayPassCnt = (
//  //  cfg.optScoreboard
//  //) generate (
//  //  Reg(UInt(log2Up(myReadGprsInstrMayPassCntInitVal + 1) bits))
//  //  init(myReadGprsInstrMayPassCntInitVal)
//  //)
//  io.readGprs.ready := (
//    (
//      io.readGprs.valid
//      && (
//        !tempHaveReadGprsHazardAddrCheckVec.asBits.orR
//        //&& rReadGprsInstrMayPassCnt.orR
//        //|| (
//        //  io.reorderBufWrite.fire
//        //  && (
//        //    io.reorderBufWrite.tag === io.readGprs.tag
//        //  )
//        //)
//      )
//      //&& !rMyInfoVec(io.readGprs.tag).readGprsHazardValid
//    )
//    //|| rFlushInfo.fire
//  )
//  //--------
//  //when (
//  //  io.readGprs.fire
//  //) {
//  //  rReadGprsInstrMayPassCnt := rReadGprsInstrMayPassCnt - 1
//  //}
//  //when (
//  //  io.readGprs.valid
//  //  && !io.readGprs.ready
//  //  && !tempHaveReadGprsHazardAddrCheckVec.asBits.orR
//  //  && io.readGprs.someNodeIsFiring
//  //  //&& !tempHaveReadGprsHazardAddrCheckFwdLimitVec.asBits.orR
//  //) {
//  //  rReadGprsInstrMayPassCnt := myReadGprsInstrMayPassCntInitVal
//  //}
//  //--------
//  //switch (
//  //  //io.readGprs.fire
//  //  io.readGprs.valid
//  //  ## io.readGprs.ready
//  //  //## io.readGprs.someNodeIsFiring
//  //  ## tempHaveReadGprsHazardAddrCheckVec.asBits.orR
//  //  //## tempHaveReadGprsHazardAddrCheckFwdLimitVec.asBits.orR
//  //  //## (rReadGprsInstrMayPassCnt < myReadGprsInstrMayPassCntInitVal)
//  //) {
//  //  is (
//  //    //M"11--"
//  //    M"11-"
//  //    //M"101"
//  //    //M"101"
//  //  ) {
//  //    rReadGprsInstrMayPassCnt := rReadGprsInstrMayPassCnt - 1
//  //  }
//  //  is (
//  //    //M"0--"
//  //    //M"0-"
//  //    //M"1-0"
//  //    //M"1-0-"
//  //    M"1-0"
//
//  //    //M"1-0"
//  //  ) {
//  //    //when (rReadGprsInstrMayPassCnt < myReadGprsInstrMayPassCntInitVal) {
//  //    //  rReadGprsInstrMayPassCnt := rReadGprsInstrMayPassCnt + 1
//  //    //} otherwise {
//  //    //}
//  //    rReadGprsInstrMayPassCnt := myReadGprsInstrMayPassCntInitVal
//  //  }
//  //  default {
//  //  }
//  //}
//  //when (
//  //  io.readGprs.fire
//  //  //&& !tempHaveReadGprsHazardAddrCheckVec.asBits.orR
//  //  && tempHaveReadGprsHazardAddrCheckFwdLimitVec.asBits.orR
//  //) {
//  //  rReadGprs
//  //}
//
//  //for (idx <- 0 until myCommitHazardCheckVecInnerSize) {
//  //  // WAR hazards
//  //  val tempRegIdx = (
//  //    //rMyInfoVec(io.commit.tag).gprIdxVec(idx)
//  //    rMyInfoVec(io.reorderBufWrite.tag).gprIdxVec.last
//  //  )
//  //  for (jdx <- 0 until cfg.optMaxNumScoreboardInstrs) {
//  //    val myTempInfoGprIdx = (
//  //      //rMyInfoVec(jdx).gprIdxVec.last
//  //      rMyInfoVec(jdx).gprIdxVec(idx)
//  //    )
//  //    tempHaveCommitHazardAddrCheckVec(jdx)(idx) := (
//  //      //tempRegIdx === myHistLastGprIdx(jdx + 1).last
//  //      //tempRegIdx === rMyInfoVec(jdx).gprIdxVec(idx)
//  //      tempRegIdx === myTempInfoGprIdx
//  //      //&& myTempInfoGprIdx.orR // check for non-zero
//  //      && rMyInfoVec(jdx).gprIsNonZeroVec(idx)
//  //      && (
//  //        rMyInfoVec(io.reorderBufWrite.tag).instrAge
//  //        > rMyInfoVec(jdx).instrAge
//  //      )
//  //      //&& rMyInfoVec(io.commit.tag).allocValid
//  //      //&& rMyInfoVec(jdx).hazardValid
//  //      //&& (
//  //      //  //rMyInfoVec(jdx).hazardValid
//  //      //  //|| 
//  //      //  rMyInfoVec(io.commit.tag).hazardValid
//  //      //)
//  //      && rMyInfoVec(jdx).issueAllocValid
//  //      && io.reorderBufWrite.tag =/= jdx
//  //      //&& io.commit.valid
//  //    )
//  //  }
//  //}
//  io.reorderBufWrite.ready := (
//    //io.reorderBufWrite.valid
//    //&& 
//    //!tempHaveCommitHazardAddrCheckVec.asBits.orR
//    True
//  )
//
//  val myInfoAllocValidVec = (
//    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
//      Bool()
//    )
//    //Vec(rMyInfoVec.reverse.map(item => item.hazardValid))
//  )
//
//  for (jdx <- 0 until cfg.optMaxNumScoreboardInstrs) {
//    when (io.reorderBufWrite.fire && io.reorderBufWrite.tag === jdx) {
//      //myInfoAllocValidVec(jdx) := False
//      //tempHaveIssueHazardAddrCheckVec(jdx).foreach(
//      //  item => (
//      //    item := False
//      //  )
//      //)
//      rMyInfoVec(jdx).issueAllocValid := False
//      //rMyInfoVec(jdx).hazardValid := False
//      rMyInfoVec(jdx).issueHazardValid := False
//      rMyInfoVec(jdx).readGprsHazardValid := False
//      //rMyInfoVec(jdx).readGprsHazardValidFwdLimit := False
//    } otherwise {
//      //myInfoAllocValidVec(jdx) := rMyInfoVec(jdx).allocValid
//    }
//    myInfoAllocValidVec(jdx) := rMyInfoVec(jdx).issueAllocValid
//  }
//
//
//  io.issue.payload.allowOverride
//  io.issue.valid := (
//    //True
//    !tempHaveIssueHazardAddrCheckVec.asBits.orR
//    //&& !tempHaveCommitHazardAddrCheckVec.asBits.orR
//    && !rFlushInfo.fire
//  )
//  io.issue.payload := (
//    RegNext(io.issue.payload, init=io.issue.payload.getZero)
//  )
//  //io.issue.cntOverflow := rFlushInfo.fire
//  io.issue.cntOverflow := rFlushInfo.fire
//  io.issue.reorderBufIdx := (
//    RegNextWhen(
//      (io.issue.reorderBufIdx + 1),
//      cond=io.issue.fire,
//      init=io.issue.reorderBufIdx.getZero,
//    )
//  )
//
//// >>> for x in range(8):
//// ...     print(x, bin(x), bin(x ^ 0x7), bin(Bitscan(x ^ 0x7)))
//// ...     
//// 0 0b0 0b111 0b1
//// 1 0b1 0b110 0b10
//// 2 0b10 0b101 0b1
//// 3 0b11 0b100 0b100
//// 4 0b100 0b11 0b1
//// 5 0b101 0b10 0b10
//// 6 0b110 0b1 0b1
//// 7 0b111 0b0 0b0
//
//// >>> for idx in range(size):
//// ...     print(idx, ("-" * (size - idx - 1) + "1" + ("0" * idx)))
//// ...     
//// 0 ---1
//// 1 --10
//// 2 -100
//// 3 1000
//  switch (
//    //io.issue.ready
//    //## 
//    Bitscan(~myInfoAllocValidVec.asBits.asUInt)
//  ) {
//    val size = myInfoAllocValidVec.size
//    for (idx <- 0 until size) {
//      is (MaskedLiteral(
//        //"1" + 
//        ("-" * (size - idx - 1) + "1" + ("0" * idx))
//      )) {
//        // fast-ish (regarding fmax) search to implement the free list
//        // search
//        //io.issue.valid := (
//        //  //True
//        //  !tempHaveIssueHazardAddrCheckVec.asBits.orR
//        //  //&& !tempHaveCommitHazardAddrCheckVec.asBits.orR
//        //  && !rFlushInfo.fire
//        //)
//        //io.issue.payload := (
//        //  RegNext(io.issue.payload, init=io.issue.payload.getZero)
//        //)
//        when (io.issue.fire) {
//          io.issue.tag := idx
//          //rFlushInfo.instrAgeCnt := rFlushInfo.instrAgeCnt + 1
//          //rMyInfoVec(idx).instrAge := rFlushInfo.instrAgeCnt
//          rMyInfoVec(idx).issueHazardValid := (
//            io.issueMyTempOpMayNeedHazardCheck
//            //True
//          )
//          //rMyInfoVec(idx).readGprsHazardValid := (
//          //  io.issueMyTempOpMayNeedHazardCheck
//          //  && tempHaveReadGprsHazardAddrCheckVec.asBits.orR
//          //)
//          rMyInfoVec(idx).issueAllocValid := (
//            //io.myTempOpMayNeedHazardCheck
//            True
//          )
//
//          rMyInfoVec(idx).gprIdxVec := io.issueGprIdxVec
//          rMyInfoVec(idx).gprIsNonZeroVec := (
//            //io.issueGprIdxVec.last.orR // check for non-zero
//            // check for non-zero
//            Vec(io.issueGprIdxVec.map(item => item.orR))
//          )
//          //for (jdx <- 0 until io.gprIdxVec.size) {
//          //  rMyInfoVec(idx).gprIsNonZeroVec(jdx) := (
//          //    io.gprIdxVec(jdx).orR // check for non-zero
//          //  )
//          //}
//        }
//      }
//    }
//    default {
//      io.issue.valid := False
//      //io.issue.payload := 0x0
//      io.issue.payload := (
//        RegNext(io.issue.payload, init=io.issue.payload.getZero)
//      )
//    }
//  }
//  switch (
//    rFlushInfo.fire
//    ## (
//      //(rFlushInfo.instrAgeCnt === myMaxInstrAge)
//      //|| 
//      io.myBranchMispredictEtc
//    )
//    ## myInfoAllocValidVec.orR
//  ) {
//    // flush the pipeline
//    is (M"01-") {
//      rFlushInfo.valid := True
//      //rFlushInfo.instrAgeCnt := 0x0
//
//      //io.issue.cntOverflow := True
//    }
//    is (M"1-0") {
//      // we're done flushing the pipeline
//      // when every element of `rMyInfoVec` has been deallocated
//      rFlushInfo.valid := False
//      //io.issue.cntOverflow := False
//    }
//    default {
//    }
//  }
//  //when (!rFlushInfo.fire) {
//  //  when (rFlushInfo.payload === myMaxInstrAge) {
//  //  }
//  //}
//}

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
  //val myScoreboardReadGprsPayload = (
  //  in(
  //    //Vec.fill(cfg.numMultiIssue)(
  //      //Stream(
  //        SnowHouseScoreboardReadGprsPayload(cfg=cfg)
  //      //)
  //    //)
  //  )
  //)
  //val myScoreboardReadGprsReady = (
  //  out(Bool())
  //)

  //val myBranchMispredictEtc = (
  //  in(
  //    Bool()
  //  )
  //)
  ////--------
  //val myScoreboardReadGprs = (
  //  slave(Stream(
  //    SnowHouseScoreboardReadGprsPayload(cfg=cfg)
  //  ))
  //)

  val myScoreboardCommit = (
    cfg.optScoreboard
  ) generate (
    slave(Stream(
      //UInt(cfg.optScoreboardTagWidth bits)
      SnowHouseScoreboardCommitPayload(cfg=cfg)
    ))
  )
  val myScoreboardReorderBufInFlushEtc = (
    cfg.optScoreboard
  ) generate (
    in(
      Bool()
    )
  )
  val myScoreboardReorderBufPsIdCanIssue = (
    cfg.optScoreboard
  ) generate (
    in(
      Bool()
    )
  )
  //--------
  val myScoreboardSavedGprTagVec = (
    cfg.optScoreboard
  ) generate (
    out(
      UInt(cfg.numGprs bits)
    )
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
    myScoreboardCommitStm=io.myScoreboardCommit,
    myScoreboardSavedGprTagVec=(
      io.myScoreboardSavedGprTagVec
    ),
    myScoreboardReorderBufInFlushEtc=(
      io.myScoreboardReorderBufInFlushEtc
    ),
    myScoreboardReorderBufPsIdCanIssue=(
      io.myScoreboardReorderBufPsIdCanIssue
    ),
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

//case class SnowHouseForFmaxPipeStageScoreboardIssueIo(
//  cfg: SnowHouseConfig
//) extends Bundle {
//  //--------
//  val up = (
//    slave(Stream(
//      SnowHousePipePayload(cfg=cfg)
//    ))
//  )
//  val down = (
//    master(Stream(
//      SnowHousePipePayload(cfg=cfg)
//    ))
//  )
//  //--------
//  val myBranchMispredictEtc = (
//    in(
//      Bool()
//    )
//  )
//  //--------
//  val myScoreboardReadGprs = (
//    slave(Stream(
//      SnowHouseScoreboardReadGprsPayload(cfg=cfg)
//    ))
//  )
//
//  val myScoreboardCommit = (
//    cfg.optScoreboard
//  ) generate (
//    slave(Stream(
//      //UInt(cfg.optScoreboardTagWidth bits)
//      SnowHouseScoreboardCommitPayload(cfg=cfg)
//    ))
//  )
//  //--------
//}
//
//case class SnowHouseForFmaxPipeStageScoreboardIssue(
//  cfg: SnowHouseConfig
//) extends Component {
//  // technically this is the pipeline stage where the scoreboard itself is
//  // stored too
//  require(
//    cfg.optScoreboard
//  )
//  //--------
//  val io = SnowHouseForFmaxPipeStageScoreboardIssueIo(cfg=cfg)
//  //--------
//  val linkArr = PipeHelper.mkLinkArr()
//
//  //def opInfoMap = cfg.opInfoMap
//
//  //val pScoreboardIssueInp = Payload(SnowHousePipePayload(cfg=cfg))
//  val pScoreboardIssueOutp = Payload(SnowHousePipePayload(cfg=cfg))
//  val cLink = CtrlLink()
//  //val sLink = StageLink(
//  //  up=cLink.down,
//  //  down={
//  //    val temp = Node()
//  //    temp.setName("sLink_down")
//  //    temp
//  //  }
//  //)
//  //val s2mLink = S2MLink(
//  //  up=sLink.down,
//  //  down={
//  //    val temp = Node()
//  //    temp.setName("s2mLink_down")
//  //    temp
//  //  }
//  //)
//
//  val sLinkArr = new ArrayBuffer[StageLink]()
//  val s2mLinkArr = new ArrayBuffer[S2MLink]()
//  sLinkArr += StageLink(
//    up=cLink.down,
//    down=Node(),
//  )
//  s2mLinkArr += S2MLink(
//    up=sLinkArr.last.down,
//    down=Node(),
//  )
//  //sLinkArr += StageLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node()
//  //)
//  //s2mLinkArr += S2MLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node(),
//  //)
//
//  linkArr += cLink
//  linkArr ++= sLinkArr
//  linkArr ++= s2mLinkArr
//  //linkArr += sLink
//  //linkArr += s2mLink
//
//  val scoreboard = (
//    cfg.optScoreboard
//  ) generate (
//    SnowHouseForFmaxScoreboard(cfg=cfg)
//  )
//
//  val myInp = SnowHousePipePayload(cfg=cfg)
//  val myOutp = SnowHousePipePayload(cfg=cfg)
//
//  cLink.up.driveFrom(io.up)(
//    con=(node, inp) => {
//      //node(pScoreboardIssueInp) := inp
//      myInp := inp
//    }
//  )
//
//  myOutp := RegNext(myOutp, init=myOutp.getZero)
//  when (cLink.up.isValid) {
//    myOutp := myInp
//  }
//  
//  //val rMyPsExSetPcState = (
//  //  Reg(Bool(), init=False)
//  //)
//
//  //when (!rMyPsExSetPcState) {
//  //  when (io.myBranchMispredictEtc) {
//  //    rMyPsExSetPcState := True
//  //  }
//  //} otherwise {
//  //  when (
//  //    //cLink.down.isFiring
//  //    cLink.up.isFiring
//  //    && myOutp.regPcSetItCnt(0).lsb
//  //  ) {
//  //    rMyPsExSetPcState := False
//  //  }
//  //}
//
//  //val mySharedNonShouldIgnoreCond = (
//  //  (
//  //    !rMyPsExSetPcState
//  //    || myOutp.regPcSetItCnt(1).lsb
//  //  )
//  //)
//
//  scoreboard.io.myBranchMispredictEtc := io.myBranchMispredictEtc
//
//  scoreboard.io.issueRegPcSetItCnt := (
//    myOutp.regPcSetItCnt
//  )
//  scoreboard.io.issueMyTempOpMayNeedHazardCheck := (
//    myOutp.instrCnt.myScoreboardOpMayNeedHazardCheck
//  )
//  scoreboard.io.issue.ready := (
//    //cLink.up.isFiring // cLink.down.isFiring
//    cLink.down.isFiring
//    //&& mySharedNonShouldIgnoreCond
//    //cLink.down.isFiring
//    //cLink.up.isValid
//    //&& cLink.down.isReady
//  )
//  scoreboard.io.issueGprIdxVec := myOutp.gprIdxVec
//  //myOutp.instrCnt.scoreboardTag.allowOverride
//  //myOutp.instrCnt.scoreboardTag := (
//  //  scoreboard.io.issue.tag
//  //)
//  myOutp.instrCnt.scoreboardIssuePayload.allowOverride
//  myOutp.instrCnt.scoreboardIssuePayload := (
//    scoreboard.io.issue.payload
//  )
//  //myOutp.tempUpMod
//  cLink.down(pScoreboardIssueOutp) := myOutp
//  cLink.down(pScoreboardIssueOutp).allowOverride
//
//  when (
//    !scoreboard.io.issue.valid//fire
//    //&& mySharedNonShouldIgnoreCond
//  ) {
//    cLink.duplicateIt()
//    cLink.down(pScoreboardIssueOutp).setAsBubbleMain(
//      //!scoreboard.io.issue.cntOverflow
//      Some(True)
//    )
//    cLink.down(pScoreboardIssueOutp).gprIdxVec.foreach(gprIdx => {
//      gprIdx := 0x0
//    })
//    //myOutp.instrCnt.scoreboardTag := (
//    //  scoreboard.io.issue.tag
//    //)
//    //myOutp.myDoHaveHazardAddrCheckVec.foreach(
//    //  item => {
//    //    item := True
//    //  }
//    //)
//    //myOutp.myDoHaveHazardAddrCheckVec.head := (
//    //  True
//    //)
//  }
//  scoreboard.io.readGprs << io.myScoreboardReadGprs
//  scoreboard.io.reorderBufWrite << io.myScoreboardCommit
//
//  s2mLinkArr.last.down.driveTo(io.down)(
//    con=(outp, node) => {
//      outp := node(pScoreboardIssueOutp)
//    }
//  )
//
//  Builder(linkArr)
//}
//case class SnowHouseForFmaxPipeStageScoreboardReadGprsIo(
//  cfg: SnowHouseConfig
//) extends Bundle {
//  val up = (
//    slave(Stream(
//      SnowHousePipePayload(cfg=cfg)
//    ))
//  )
//  val down = (
//    master(Stream(
//      SnowHousePipePayload(cfg=cfg)
//    ))
//  )
//  //--------
//  val readGprs = (
//    master(Stream(
//      SnowHouseScoreboardReadGprsPayload(cfg=cfg)
//    ))
//  )
//  //val readGprsPayload = (
//  //  out(
//  //    //Vec.fill(cfg.numMultiIssue)(
//  //      //Stream(
//  //        SnowHouseScoreboardReadGprsPayload(cfg=cfg)
//  //      //)
//  //    //)
//  //  )
//  //)
//
//  //val readGprsReady = (
//  //  in(Bool())
//  //)
//
//  val myBranchMispredictEtc = (
//    in(
//      Bool()
//    )
//  )
//  //--------
//}
//case class SnowHouseForFmaxPipeStageScoreboardReadGprs(
//  cfg: SnowHouseConfig
//) extends Component {
//  require(
//    cfg.optScoreboard
//  )
//  //--------
//  val io = SnowHouseForFmaxPipeStageScoreboardReadGprsIo(cfg=cfg)
//  //--------
//  val linkArr = PipeHelper.mkLinkArr()
//
//  //def opInfoMap = cfg.opInfoMap
//
//  //val pScoreboardReadGprsInp = Payload(SnowHousePipePayload(cfg=cfg))
//  val pScoreboardReadGprsOutp = Payload(SnowHousePipePayload(cfg=cfg))
//  val cLink = CtrlLink()
//  //val sLink = StageLink(
//  //  up=cLink.down,
//  //  down={
//  //    val temp = Node()
//  //    temp.setName("sLink_down")
//  //    temp
//  //  }
//  //)
//  //val s2mLink = S2MLink(
//  //  up=sLink.down,
//  //  down={
//  //    val temp = Node()
//  //    temp.setName("s2mLink_down")
//  //    temp
//  //  }
//  //)
//  val sLinkArr = new ArrayBuffer[StageLink]()
//  //val s2mLinkArr = new ArrayBuffer[S2MLink]()
//  sLinkArr += StageLink(
//    up=cLink.down,
//    down=Node(),
//  )
//  //sLinkArr += StageLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node(),
//  //)
//  //sLinkArr += StageLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node(),
//  //)
//  //s2mLinkArr += S2MLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node(),
//  //)
//  //sLinkArr += StageLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node()
//  //)
//  //s2mLinkArr += S2MLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node(),
//  //)
//  //sLinkArr += StageLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node()
//  //)
//  //sLinkArr += StageLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node()
//  //)
//  //sLinkArr += StageLink(
//  //  up=sLinkArr.last.down,
//  //  down=Node()
//  //)
//  linkArr += cLink
//  linkArr ++= sLinkArr
//  //linkArr ++= s2mLinkArr
//  //linkArr += sLink
//  //linkArr += s2mLink
//
//  val myInp = SnowHousePipePayload(cfg=cfg)
//  val myOutp = SnowHousePipePayload(cfg=cfg)
//
//  cLink.up.driveFrom(io.up)(
//    con=(node, inp) => {
//      //node(pScoreboardReadGprsInp) := inp
//      myInp := inp
//    }
//  )
//
//  myOutp := RegNext(myOutp, init=myOutp.getZero)
//  when (cLink.up.isValid) {
//    myOutp := myInp
//  }
//
//  io.readGprs.gprIdxVec := myOutp.gprIdxVec
//  io.readGprs.tag := myOutp.instrCnt.scoreboardTag
//  io.readGprs.regPcSetItCnt := myOutp.regPcSetItCnt
//  //val rStallState = Reg(Bool(), init=False)
//
//  //when (!rStallState) {
//  //}
//
//  //val rSeenReadGprsFire = Reg(Bool(), init=False)
//  //val stickyReadGprsFire = (
//  //  io.readGprs.fire
//  //  || rSeenReadGprsFire
//  //)
//
//  //when (io.readGprs.fire) {
//  //  rSeenReadGprsFire := True
//  //}
//  //when (cLink.down.isFiring) {
//  //  rSeenReadGprsFire := False
//  //}
//
//  //val rMyPsExSetPcState = (
//  //  Reg(Bool(), init=False)
//  //)
//
//  //when (!rMyPsExSetPcState) {
//  //  when (io.myBranchMispredictEtc) {
//  //    rMyPsExSetPcState := True
//  //  }
//  //} otherwise {
//  //  when (
//  //    //cLink.down.isFiring
//  //    cLink.up.isFiring
//  //    && myOutp.regPcSetItCnt(0).lsb
//  //  ) {
//  //    rMyPsExSetPcState := False
//  //  }
//  //}
//
//  val mySharedNonShouldIgnoreCond = (
//    //cLink.up.isValid
//    //&& 
//    Vec(myOutp.instrCnt.myPsIdBubble.map(
//      item => (
//        !item
//        //&& (
//        //  !rMyPsExSetPcState
//        //  || !myOutp.regPcSetItCnt(1).lsb
//        //)
//      )
//    ))
//    //&& (
//    //  !rMyPsExSetPcState
//    //  || myOutp.regPcSetItCnt(1).lsb
//    //)
//  )
//
//  io.readGprs.valid := (
//    cLink.up.isValid
//    && cLink.down.isReady
//    //cLink.up.isFiring
//    //cLink.down.isFiring
//    //&& !myOutp.instrCnt.myPsIdBubble.head
//    && mySharedNonShouldIgnoreCond.head
//  )
//  io.readGprs.someNodeIsFiring := (
//    cLink.down.isFiring
//  )
//
//  //val rSentReadGprsBubble = Reg(Bool(), init=False)
//
//  when (
//    cLink.up.isValid
//    && mySharedNonShouldIgnoreCond.last
//    && io.readGprs.valid
//    && !io.readGprs.ready 
//  ) {
//    cLink.duplicateIt()
//    cLink.down(pScoreboardReadGprsOutp).allowOverride
//    cLink.down(pScoreboardReadGprsOutp) := myOutp//.getZero
//
//    cLink.down(pScoreboardReadGprsOutp).setAsBubbleMain(
//      //!scoreboard.io.issue.cntOverflow
//      Some(True)
//      //myPsIdBubble=True,
//      //myUpdateGprIsOrIsntZero=true
//    )
//
//    //(
//    //  cLink.down(pScoreboardReadGprsOutp).instrCnt
//    //).myScoreboardReadGprsBubble.foreach(
//    //  item => {
//    //    item := !rSentReadGprsBubble //True
//    //  }
//    //)
//    //when (cLink.down.isFiring) {
//    //  rSentReadGprsBubble := True
//    //}
//
//    //cLink.down(pScoreboardReadGprsOutp).gprIsZeroVec.foreach(
//    //  outerItem => {
//    //    outerItem.foreach(item => {
//    //      item := True
//    //    })
//    //  }
//    //)
//    //innerPsId.upPayload(1).myDoHaveHazardAddrCheckVec.foreach(
//    //  item => {
//    //    item := True
//    //  }
//    //)
//    //innerPsId.upPayload(1).myDoHaveHazardAddrCheckVec.head := (
//    //  True
//    //)
//  } otherwise {
//    cLink.down(pScoreboardReadGprsOutp) := myOutp
//  }
//
//  //when (cLink.up.isFiring) {
//  //  rSentReadGprsBubble := False
//  //}
//
//  //when (
//  //  cLink.up.isValid
//  //  && myInp.instrCnt.myPsIdBubble.head
//  //) {
//  //  cLink.throwIt()
//  //}
//
//  sLinkArr.last.down.driveTo(io.down)(
//    con=(outp, node) => {
//      outp := node(pScoreboardReadGprsOutp)
//    }
//  )
//
//  Builder(linkArr)
//}

case class SnowHouseForFmaxPipeStagePreFwdIo(
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
  val myRegFileWrPulse = (
    slave(Flow(
      PipeSimpleDualPortMemDrivePayload(
        dataType=UInt(cfg.mainWidth bits),
        wordCount=cfg.regFileCfg.wordCountArr(0),
      )
    ))
  )
  //--------
}

case class SnowHouseForFmaxPipeStagePreFwd(
  cfg: SnowHouseConfig,
) extends Component {
  //--------
  val io = SnowHouseForFmaxPipeStagePreFwdIo(cfg=cfg)
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

  val innerPsPreFwd = SnowHousePipeStagePreFwd(
    cfg=cfg,
    outp=myOutp,
    inp=myInp,
    //link=cLink,
    upIsValid=cLink.up.isValid,
    upIsFiring=cLink.up.isFiring,
    myBranchMispredictEtc=io.myBranchMispredictEtc,
    forFmaxRegFileWrPulseArr=Array(
      io.myRegFileWrPulse
    )
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
  //val s2mLink = S2MLink(
  //  up=sLink.down,
  //  down={
  //    val temp = Node()
  //    temp.setName("s2m_down")
  //    temp
  //  }
  //)
  linkArr += cLink
  linkArr += sLink
  //linkArr += s2mLink

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

  sLink.down.driveTo(
    io.down
  )(
    con=(outp, node) => {
      outp := node(pExOutp)
    }
  )

  Builder(linkArr)
  //--------
}

case class SnowHouseForFmaxPsWbCommitEtc(
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
  //val myScoreboardFwdRegFileWrPulse = (
  //  cfg.optScoreboard
  //) generate (
  //  master(
  //    Flow(
  //      PipeSimpleDualPortMemDrivePayload(
  //        dataType=UInt(cfg.mainWidth bits),
  //        wordCount=cfg.regFileCfg.wordCountArr(0),
  //      )
  //    )
  //  )
  //)
  val scoreboardReorderBufInFlushEtc = (
    cfg.optScoreboard
  ) generate (
    out(
      Bool()
    )
  )
  val scoreboardReorderBufPsIdCanIssue = (
    cfg.optScoreboard
  ) generate (
    out(
      Bool()
    )
  )
  val scoreboardTag = (
    cfg.optScoreboard
  ) generate (
    master(
      Stream(
        //UInt(cfg.optScoreboardTagWidth bits)
        SnowHouseScoreboardCommitPayload(cfg=cfg)
      )
    )
  )
}

case class SnowHouseForFmaxPsWbReorderBufPayloadMost(
  cfg: SnowHouseConfig,
  //optIncludeBufIdx: Boolean=true,
) extends Bundle {
  val commit = (
    cfg.optScoreboard
  ) generate (
    //cloneOf(io.commitEtc.scoreboardTag.payload)
    SnowHouseScoreboardCommitPayload(cfg=cfg)
  )

  val myShouldIgnoreInstr = Bool()
  val myPsIdBubble = Bool()
  //val opIsMemAccess = Bool()

  val regFileWrite = (
    //cloneOf(
    //  io.commitEtc.myRegFileWrPulse.payload
    //)
    PipeSimpleDualPortMemDrivePayload(
      dataType=UInt(cfg.mainWidth bits),
      wordCount=cfg.regFileCfg.wordCountArr(0),
    )
  )
  //val myWbPayload = (
  //  io.dbgInfo != null
  //) generate (
  //  cloneOf(myWbPayloadVec.head(1))
  //)
  val myWbPayload = (
    cfg.exposeRegFileWriteDataToIo
    || cfg.exposeRegFileWriteAddrToIo
    || cfg.exposeRegFileWriteEnableToIo
    || cfg.dbgExposeExtrasAtRegFileWrite
  ) generate (
    SnowHousePipePayload(cfg=cfg)
  )
}
case class SnowHouseForFmaxPsWbReorderBufPayload(
  cfg: SnowHouseConfig,
  optIncludeBufIdx: Boolean=true,
) extends Bundle {
  val most = SnowHouseForFmaxPsWbReorderBufPayloadMost(cfg=cfg)
  def commit = most.commit
  def myShouldIgnoreInstr = most.myShouldIgnoreInstr
  def myPsIdBubble = most.myPsIdBubble
  //def opIsMemAccess = most.opIsMemAccess
  def regFileWrite = most.regFileWrite
  def myWbPayload = most.myWbPayload

  //val myOpIsNonFwd = (
  //  cfg.optScoreboard
  //  && optIncludeBufIdx
  //) generate (
  //  Bool()
  //)

  val reorderBufIdx = (
    cfg.optScoreboard
    && optIncludeBufIdx
  ) generate (
    UInt(cfg.optScoreboardReorderBufWidth bits)
  )
  val postFlushReorderBufIdx = (
    cfg.optScoreboard
    && optIncludeBufIdx
  ) generate (
    UInt(cfg.optScoreboardReorderBufWidth bits)
  )
}

case class SnowHouseForFmaxPsWbReorderBufIo(
  cfg: SnowHouseConfig
) extends Bundle {
  require(
    cfg.optScoreboard
  )

  val push = slave(Stream(
    SnowHouseForFmaxPsWbReorderBufPayload(cfg=cfg)
  ))
  val pop = master(Stream(
    SnowHouseForFmaxPsWbReorderBufPayload(
      cfg=cfg,
      optIncludeBufIdx=false,
    )
  ))
  val inFlushEtc = (
    out(
      Bool()
    )
  )
  val psIdCanIssue = (
    out(
      Bool()
    )
  )
  val postFlushReorderBufIdx = (
    in(
      UInt(cfg.optScoreboardReorderBufWidth bits)
    )
  )
  //val myBranchMispredictEtc = in(
  //  Bool()
  //)
}

case class SnowHouseForFmaxPsWbReorderBuf(
  cfg: SnowHouseConfig
) extends Component {
  require(
    cfg.optScoreboard
  )
  //--------
  val io = SnowHouseForFmaxPsWbReorderBufIo(cfg=cfg)
  //--------
  val myReorderBufSize = (
    1 << cfg.optScoreboardReorderBufWidth
  )
  val myOccupancySubAmount = (
    8
  )
  //val myFifo = (
  //  StreamFifo(
  //    dataType=SnowHouseForFmaxPsWbReorderBufPayload(cfg=cfg),
  //    depth=myReorderBufSize,
  //    latency=0,
  //    forFMax=true,
  //  )
  //)

  val myRam = (
    WrPulseRdPipeRam(
      cfg=WrPulseRdPipeRamConfig(
        modType=SnowHouseForFmaxPsWbReorderBufPayload(
          cfg=cfg,
          optIncludeBufIdx=true,
        ),
        wordType=SnowHouseForFmaxPsWbReorderBufPayload(
          cfg=cfg,
          optIncludeBufIdx=false
        ),
        wordCount=myReorderBufSize,
        setWordFunc=(
          outp: SnowHouseForFmaxPsWbReorderBufPayload,
          inp: SnowHouseForFmaxPsWbReorderBufPayload,
          rdMemWord: SnowHouseForFmaxPsWbReorderBufPayload,
          upIsFiring: Bool,
          myExternalInpCond: Bool,
          wrPulse: Flow[
            PipeSimpleDualPortMemDrivePayload[
              SnowHouseForFmaxPsWbReorderBufPayload
            ]
          ],
        ) => {
          outp.reorderBufIdx := inp.reorderBufIdx
          //outp.most := rdMemWord.most
          //val myTempWrPulse = (
          //  cloneOf(wrPulse)
          //)
          //myTempWrPulse.valid := (
          //  wrPulse.fire
          //  //&& myExternalInpCond
          //)
          //myTempWrPulse.payload := wrPulse.payload

          //val myHistWrPulseEtc = (
          //  History(
          //    that=myTempWrPulse,
          //    when=(
          //      myTempWrPulse.fire
          //      //&& wrPulse.addr === inp.reorderBufIdx
          //    ),
          //    length=(
          //      2
          //      //1
          //    ),
          //    init=myTempWrPulse.getZero
          //  )
          //)
          switch (
            (
              wrPulse.fire
              //&& (
              //  rMyShouldIgnoreInstrState.asBits(0)
              //  || (
              //    rMyShouldIgnoreInstrState.asBits(1)
              //    && !io.push.myPsIdBubble
              //  )
              //)
              && myExternalInpCond
              && wrPulse.addr === inp.reorderBufIdx
            )
            ## (
              RegNextWhen(
                wrPulse.addr,
                cond=(
                  wrPulse.fire
                  && myExternalInpCond
                ),
                init=wrPulse.addr.getZero
              ) === inp.reorderBufIdx
              //&& myExternalInpCond
            )

            //myHistWrPulseEtc.reverse.asBits
            //myHistWrPulseEtc(0).fire
            //## myHistWrPulseEtc(1).fire
          ) {
            is (M"1-") {
              outp.most := (
                wrPulse.data.most
                //myHistWrPulseEtc(0).data.most
              )
              //outp.reorderBufIdx := (
              //  myHistWrPulseEtc(0).addr
              //)
            }
            is (M"01") {
              //outp.most := (
              //  //wrPulse.data.most
              //  //myHistWrPulseEtc(1).data.most
              //)
              //outp.reorderBufIdx := (
              //  myHistWrPulseEtc(1).addr
              //)
              outp.most := (
                //myHistWrPulseEtc(1).
                //RegNextWhen(
                //  wrPulse.data.most,
                //  cond=wrPulse.fire,
                //  init=wrPulse.data.most.getZero
                //)
                RegNextWhen(
                  wrPulse.data.most,
                  cond=(
                    wrPulse.fire
                    && myExternalInpCond
                  ),
                  init=wrPulse.data.most.getZero
                )
              )
            }
            default {
              //outp.reorderBufIdx := inp.reorderBufIdx
              outp.most := rdMemWord.most
            }
          }
          //when (
          //  wrPulse.fire
          //  && wrPulse.addr === inp.reorderBufIdx
          //) {
          //  outp.most := wrPulse.data.most
          //} 
          //.elsewhen (
          //  RegNextWhen(
          //    wrPulse.addr,
          //    cond=wrPulse.fire,
          //    init=wrPulse.addr.getZero
          //  ) === inp.reorderBufIdx
          //) {
          //  outp.most := (
          //    RegNextWhen(
          //    wrPulse.data.most,
          //      cond=wrPulse.fire,
          //      init=wrPulse.data.most.getZero
          //    )
          //  )
          //} 
          //.otherwise {
          //  outp.most := rdMemWord.most
          //}
        },
        optRdLatency=(
          1//0//1
        ),
        optWrHistLength=1,
        initBigInt=Some({
          val tempArr = new ArrayBuffer[BigInt]()
          tempArr ++= Array.fill(myReorderBufSize)(BigInt(0))
          Array(tempArr).toSeq
        }),
        arrRamStyleAltera=(
          //"no_rw_check, logic"
          "no_rw_check, MLAB"
          //"MLAB"
        ),
        arrRamStyleXilinx=(
          "auto"
          //"block"
          //"distributed"
        ),
      ),
    )
  )

  object MyShouldIgnoreInstrState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      FLUSH,
      CLEAR_VALID_VEC_ETC,
      SET_TO_REORDER_BUF_IDX_ETC
      = newElement()
  }
  val nextMyShouldIgnoreInstrState = MyShouldIgnoreInstrState()//Bool()
  val rMyShouldIgnoreInstrState = (
    //Reg(Bool(), init=False)
    RegNext(
      nextMyShouldIgnoreInstrState,
      //init=False
    )
    init(MyShouldIgnoreInstrState.IDLE)
  )
  nextMyShouldIgnoreInstrState := rMyShouldIgnoreInstrState

  val myRdAddr = cloneOf(myRam.io.rdAddrPipe.addr)

  val myAssertValidCondMost = (
    rMyShouldIgnoreInstrState.asBits(0)
    || (
      rMyShouldIgnoreInstrState.asBits(1)
      //&& !io.push.myPsIdBubble // is this needed anywhere?
    )
  )
  val myAssertValidCond = (
    myRam.io.wrPulse.fire
    && myAssertValidCondMost
  )
  myRam.io.myExternalInpCond := (
    myAssertValidCondMost
    ////True
    //(
    //  rMyShouldIgnoreInstrState.asBits(0)
    //  || (
    //    rMyShouldIgnoreInstrState.asBits(1)
    //    && !io.push.myPsIdBubble
    //  )
    //)
    ////&& 
    ////(
    ////  !io.push.myPsIdBubble
    ////)
  )

  //myFifo.io.push << io.push
  //myFifo.io.pop.ready := False
  val rFlushCnt = (
    Reg(UInt(cfg.optScoreboardReorderBufWidth + 1 bits))
    init(myReorderBufSize - 1)
  )
  val rSeenFullFlush = rFlushCnt.msb

  val rOccupancy = (
    Reg(UInt(log2Up(myReorderBufSize) bits))
    init(0x0)
  )
  val rValidVec = Vec.fill(myReorderBufSize)(
    Reg(Bool(), init=False)
  )
  //val rAttemptPushVec = Vec.fill(myReorderBufSize)(
  //  Reg(Bool(), init=False)
  //)

  //val rPushState = Reg(Bool(), init=False)

  //switch (rPushState) {
  //  is (False) {
  //    when (
  //      io.push.valid
  //      && rValidVec(io.push.reorderBufIdx)
  //    ) {
  //      rPushState := True
  //      io.push.ready := False
  //    } otherwise {
  //      io.push.ready := True
  //    }
  //  }
  //  is (True) {
  //    when (
  //      io.push.valid
  //      && !rValidVec(io.push.reorderBufIdx)
  //    ) {
  //      rPushState := False
  //      io.push.ready := True
  //    } otherwise {
  //      io.push.ready := False
  //    }
  //  }
  //}

  //val myPushStm = cloneOf(io.push)

  //when (!rMyShouldIgnoreInstrState) {
  //  myRdAddr := (
  //    RegNextWhen(
  //      (myRdAddr + 1),
  //      cond=(
  //        myRam.io.rdAddrPipe.fire
  //        //|| 
  //      ),
  //      //init=myRdAddr.getZero,
  //    )
  //    init(0x1)
  //    //init(0x0)
  //  )
  //} otherwise {
  //  when (rOccupancy.orR) {
  //    myRdAddr := (
  //      RegNext(
  //        myRdAddr,
  //        init=myRdAddr.getZero
  //      ) + 1
  //    )
  //  } otherwise {
  //    myRdAddr := (
  //      io.push.reorderBufIdx
  //    )
  //  }
  //}

  //val myMaxValShouldIgnoreInstrCnt = 4//3//4//3//4
  //val rMyShouldIgnoreInstrCnt = (
  //  Reg(UInt(log2Up(myMaxValShouldIgnoreInstrCnt + 1) + 1 bits))
  //  init(0x0)
  //)
  //when (
  //  io.push.fire
  //  && io.push.myShouldIgnoreInstr
  //  && rMyShouldIgnoreInstrCnt < myMaxValShouldIgnoreInstrCnt
  //) {
  //  rMyShouldIgnoreInstrCnt := rMyShouldIgnoreInstrCnt + 1
  //}
  //when (
  //  io.push.fire
  //  && !io.push.myShouldIgnoreInstr
  //) {
  //  rMyShouldIgnoreInstrCnt := 0x0
  //}
  //io.inFlushEtc.setAsReg() init(False)
  io.inFlushEtc := (
    //!rMyShouldIgnoreInstrState.asBits(0)
    rMyShouldIgnoreInstrState.asBits(1)
  )
  myRdAddr := (
    RegNext(myRdAddr)
    init(0x1)
  )
  //val rSavedMyRdAddr = (
  //  Reg(cloneOf(myRdAddr))
  //)

  switch (rMyShouldIgnoreInstrState) {
    is (MyShouldIgnoreInstrState.IDLE) {
      when (
        io.push.fire//valid//fire
        && io.push.myShouldIgnoreInstr
        //&& rOccupancy >= 2//rOccupancy.orR
        //&& (
        //  rOccupancy.orR
        //  || !myRam.io.rdAddrPipe.fire
        //)
      ) {
        nextMyShouldIgnoreInstrState := (
          MyShouldIgnoreInstrState.FLUSH //True
        )
      }
      //when (
      //  RegNext(
      //    myRam.io.rdAddrPipe.fire,
      //    init=False
      //  )
      //) {
      //} elsewhen (
      //)
      myRdAddr := (
        RegNextWhen(
          (myRdAddr + 1),
          cond=(
            myRam.io.rdAddrPipe.fire
            //|| rose(rMyShouldIgnoreInstrState.asBits(0))
            || rMyShouldIgnoreInstrState.asBits(3)
            //|| 
          ),
          //init=myRdAddr.getZero,
        )
        init(0x1)
        //init(0x0)
      )
      //rSavedMyRdAddr := (
      //  myRdAddr - 1
      //)
      rFlushCnt := myReorderBufSize - 1
    }
    is (MyShouldIgnoreInstrState.FLUSH) {
      when (
        (
          (
            !rOccupancy.orR
            || rSeenFullFlush
          )
          //!(rOccupancy >= 2)
          && (
            //!io.push.valid//fire//valid
            //|| !io.push.myShouldIgnoreInstr
            io.push.valid
            && (
              !io.push.myShouldIgnoreInstr
              || io.push.myPsIdBubble
            )
          )
        )
      ) {
        nextMyShouldIgnoreInstrState := (
          //MyShouldIgnoreInstrState.SET_TO_REORDER_BUF_IDX_ETC
          MyShouldIgnoreInstrState.CLEAR_VALID_VEC_ETC
        )
      } otherwise {
        myRdAddr := (
          RegNext(
            myRdAddr,
            init=myRdAddr.getZero
          ) + 1
        )
      }
      when (
        !rSeenFullFlush
        && myRam.io.rdAddrPipe.fire
      ) {
        rFlushCnt := rFlushCnt - 1
      }

      //switch ({
      //  val x = ~(rValidVec.asBits.asUInt)
      //  x & ~(x - 1)
      //  //+ rSavedMyRdAddr
      //}) {
      //  // >>> for idx in range(size):
      //  // ...     print(idx, ("-" * (size - idx - 1) + "1" + ("0" * idx)))
      //  // ...     
      //  // 0 ---1
      //  // 1 --10
      //  // 2 -100
      //  // 3 1000
      //  for (idx <- 0 until rValidVec.size) {
      //    is (
      //      MaskedLiteral(
      //        //"1"
      //        //+ 
      //        ("-" * (rValidVec.size - idx - 1) + "1" + ("0" * idx))
      //      )
      //    ) {
      //      myRdAddr := rSavedMyRdAddr + idx
      //    }
      //  }
      //}
    }
    is (MyShouldIgnoreInstrState.CLEAR_VALID_VEC_ETC) {
      rValidVec.foreach(item => item := False)
      rOccupancy := 0x0
      nextMyShouldIgnoreInstrState := (
        MyShouldIgnoreInstrState.SET_TO_REORDER_BUF_IDX_ETC
        //MyShouldIgnoreInstrState.CLEAR_VALID_VEC_ETC
      )
    }
    is (MyShouldIgnoreInstrState.SET_TO_REORDER_BUF_IDX_ETC) {
      when (
        io.push.valid
        && !io.push.myShouldIgnoreInstr
      ) {
        nextMyShouldIgnoreInstrState := (
          MyShouldIgnoreInstrState.IDLE
        )
      }
      //myRdAddr := (
      //  io.push.payload.reorderBufIdx - 1
      //)
      myRdAddr := (
        //io.push.postFlushReorderBufIdx
        io.postFlushReorderBufIdx - 1
      )

      //when (io.push.valid) {
      //  myRdAddr := io.push.reorderBufIdx - 1
      //} otherwise {
      //  myRdAddr := (
      //    RegNextWhen(
      //      io.push.reorderBufIdx - 1,
      //      cond=io.push.fire,
      //      init=io.push.reorderBufIdx.getZero
      //    )
      //  )
      //}

      //myRdAddr := (
      //  //io.push.payload.reorderBufIdx - 1
      //)
    }
  }
  //when (
  //  rMyShouldIgnoreInstrState
  //  && (
  //    !rOccupancy.orR
  //    //|| !(
  //    //  io.push.fire//valid
  //    //  && io.push.myShouldIgnoreInstr
  //    //)
  //  )
  //  //&& (
  //  //  !(
  //  //    io.push.valid//fire//valid
  //  //    && io.push.myShouldIgnoreInstr
  //  //  )
  //  //)
  //) {
  //  nextMyShouldIgnoreInstrState := False
  //}

  io.psIdCanIssue := (
    rOccupancy
    < (
      myReorderBufSize
      - myOccupancySubAmount
      - (cfg.myPsIdBubbleNumFollowingInstrs + 1)
    )
  )

  switch (io.push.reorderBufIdx) {
    for (idx <- 0 until (1 << io.push.reorderBufIdx.getWidth)) {
      is (idx) {
        io.push.ready := (
          //True
          //!rValidVec.andR
          //!rAttemptPushVec(idx)
          //&& 
          (
            (
              !rValidVec(idx)
              && io.psIdCanIssue
            )
            || (
              //myRam.io.rdAddrPipe.fire
              myAssertValidCondMost
              && myRdAddr === idx
            )
          )
          //&& (
          //  //rOccupancy < myReorderBufSize - myOccupancySubAmount
          //  io.psIdCanIssue
          //  || (
          //    myRdAddr === idx
          //  )
          //)
          //&& io.psIdCanIssue
          //2//6//4//8//- 1
          && (
            !rMyShouldIgnoreInstrState.asBits(2)
            && (
              !rMyShouldIgnoreInstrState.asBits(3)
              //rMyShouldIgnoreInstrState.asBits(3)
              || (
                io.push.valid
                && io.push.myShouldIgnoreInstr
              )
            )
            //rMyShouldIgnoreInstrState.asBits(0)
            //|| (
            //  rMyShouldIgnoreInstrState.asBits(1)
            //  //&& io.push.valid
            //  //&& io.push.myShouldIgnoreInstr
            //)
            //=== MyShouldIgnoreInstrState.IDLE
            //!rMyShouldIgnoreInstrState
            //|| (
            //  io.push.valid
            //  && (
            //    //!io.push.myShouldIgnoreInstr
            //    io.push.myShouldIgnoreInstr
            //  )
            //)
          )
          //&& myRam.io.rdAddrPipe.addr =/= idx
          //|| (
          //  //rValidVec(idx)
          //  //&& 
          //  io.push.valid
          //  && io.push.myWbPayload.instrCnt.shouldIgnoreInstr.head
          //)
          //&& !rPushState
        )
      }
    }
  }

  //io.push.ready := (
  //  //True
  //  //!rValidVec.andR
  //  !rAttemptPushVec(io.push.reorderBufIdx)
  //  && !rValidVec(io.push.reorderBufIdx)
  //  //&& !rPushState
  //)

  //when (
  //  io.push.valid
  //  && !io.push.ready
  //  && !rAttemptPushVec(io.push.reorderBufIdx)
  //) {
  //  rAttemptPushVec(io.push.reorderBufIdx) := True
  //}
  //when (
  //  io.push.fire
  //) {
  //  rAttemptPushVec(io.push.reorderBufIdx) := False
  //}

  myRam.io.wrPulse.valid := (
    io.push.fire//fire//valid//fire//valid//valid//fire
    //&& !io.push.myShouldIgnoreInstr
    //&& !rValidVec(io.push.reorderBufIdx)
    //&& io.push.myWbPayload.instrCnt.shouldIgnoreInstr.head
  )
  myRam.io.wrPulse.addr := io.push.reorderBufIdx
  //.resize(
  //  log2Up(rValidVec.size) bits
  //)

  myRam.io.wrPulse.data.most := io.push.most

  when (
    io.push.fire
    && (
      io.push.myShouldIgnoreInstr
      || io.push.myPsIdBubble
    )
    //&& rMyShouldIgnoreInstrCnt >= myMaxValShouldIgnoreInstrCnt
    //&& io.push.opIsMemAccess
  ) {
    myRam.io.wrPulse.data.commit.myNonFwdValid := False//True
    myRam.io.wrPulse.data.commit.myFwdValid := False//True
    //myRam.io.wrPulse.data.commit.myGprIdx.valid := False//True
  }

  when (
    //&& !io.push.myShouldIgnoreInstr
    myAssertValidCond
  ) {
    rValidVec(myRam.io.wrPulse.addr) := True
  }

  //when (
  //  io.push.valid//fire
  //  //&& !rValidVec(io.push.reorderBufIdx) // check for 
  //  && io.push.myWbPayload.instrCnt.shouldIgnoreInstr.head
  //) {
  //  myRam.io.wrPulse.data.regFileWrite.addr := 0x0
  //  myRam.io.wrPulse.data.regFileWrite.data := 0x0
  //}
  when (
    myRam.io.rdAddrPipe.fire
    //myRam.io.rdDataPipe.fire//valid//fire
  ) {
    rValidVec(
      myRam.io.rdAddrPipe.addr
      //myRam.io.rdDataPipe.reorderBufIdx
    ) := False
  }

  switch (
    (
      myRam.io.wrPulse.fire
      //&& !io.push.myPsIdBubble
      && (
        rMyShouldIgnoreInstrState.asBits(0)
        || (
          rMyShouldIgnoreInstrState.asBits(1)
          && !io.push.myPsIdBubble
        )
      )
      //&& !rOccupancy.andR
    )
    ## (
      myRam.io.rdAddrPipe.fire
      //&& rOccupancy.orR
    )
  ) {
    is (M"10") {
      rOccupancy := rOccupancy + 1
    }
    is (M"01") {
      rOccupancy := rOccupancy - 1
    }
    default {
    }
  }


//  switch (
//    rMyPsExSetPcState
//    ## Bitscan(~rValidVec.reverse.asBits.asUInt)
//  ) {
//    is (
//      MaskedLiteral(
//        "0" + ("-" * rValidVec.size)
//      )
//    ) {
//    }
//
//// >>> for idx in range(size):
//// ...     print(idx, ("-" * (size - idx - 1) + "1" + ("0" * idx)))
//// ...     
//// 0 ---1
//// 1 --10
//// 2 -100
//// 3 1000
//    for (idx <- 0 until rValidVec.size) {
//      is (
//        MaskedLiteral(
//          "1"
//          + ("-" * (rValidVec.size - idx - 1) + "1" + ("0" * idx))
//        )
//      ) {
//      }
//    }
//
//    default {
//    }
//  }


  //val myTempPushStm = Vec.fill(2)(
  //  cloneOf(io.pop)
  //  //Stream(
  //  //  cloneOf(io.push.payload)
  //  //)
  //)
  //myTempPushStm.head.valid := True
  //myTempPushStm.head.most := io.push.most
  //val myRdAddr = cloneOf(myRam.io.rdAddrPipe.addr)
  //myRdAddr := (
  //  RegNext(
  //    myRdAddr,
  //    init=myRdAddr.getZero
  //  )
  //)
  //when (
  //  myRam.io.rdAddrPipe.fire
  //) {
  //  myRdAddr := (
  //    RegNext(
  //      myRdAddr,
  //      init=myRdAddr.getZero
  //    ) + 1
  //  )
  //}

  //when (!rMyShouldIgnoreInstrState) {
  //  myRdAddr := (
  //    RegNextWhen(
  //      (myRdAddr + 1),
  //      cond=(
  //        myRam.io.rdAddrPipe.fire
  //        //|| 
  //      ),
  //      //init=myRdAddr.getZero,
  //    )
  //    init(0x1)
  //    //init(0x0)
  //  )
  //} otherwise {
  //  when (rOccupancy.orR) {
  //    myRdAddr := (
  //      RegNext(
  //        myRdAddr,
  //        init=myRdAddr.getZero
  //      ) + 1
  //    )
  //  } otherwise {
  //    myRdAddr := (
  //      io.push.reorderBufIdx
  //    )
  //  }
  //}

  //when (
  //  (
  //    io.push.valid
  //    && io.push.myWbPayload.instrCnt.shouldIgnoreInstr.last
  //  )
  //) {
  //  io.push.ready := True//False
  //  myRam.io.wrPulse.valid := False
  //  myRam.io.rdAddrPipe.valid := False
  //  myRdAddr := 0x0//0x1//0x0
  //  rValidVec.foreach(item => item := False)
  //} otherwise {
  //  //myTempPushStm.last << myTempPushStm.head.haltWhen
  //  myRam.io.rdAddrPipe.valid := (
  //    //rValidVec(
  //    //  myRdAddr - 1
  //    //)
  //    //&& 
  //    rValidVec(
  //      myRdAddr
  //    )
  //    //RegNext(
  //    //  RegNext(
  //    //    rValidVec(
  //    //      myRdAddr
  //    //    )
  //    //  ),
  //    //  init=False
  //    //)
  //    //|| (
  //    //  myRam.io.wrPulse.fire
  //    //  && myRam.io.wrPulse.addr === myRdAddr
  //    //)
  //  )
  //}

  myRam.io.rdAddrPipe.valid := (
    //rValidVec(
    //  myRdAddr - 1
    //)
    //&& 
    (
      rValidVec(
        myRdAddr
      )
      || (
        //myRam.io.wrPulse.fire
        myAssertValidCond
        && myRam.io.wrPulse.addr === myRdAddr
      )
      //&& rOccupancy.orR
      //|| (
      //  !rMyShouldIgnoreInstrState.asBits(0)
      //  //|| rOccupancy >= 2
      //)
      //&& !rMyShouldIgnoreInstrState
    )
    //&& rOccupancy.orR
    //|| (
    //  //nextMyShouldIgnoreInstrState
    //  rMyShouldIgnoreInstrState
    //  && rOccupancy.orR
    //)
    //&& myRam.io.rdAddrPipe.addr =/= idx
    //&& rOccupancy >= 1
    //rValidVec(
    //  RegNextWhen(
    //    //rValidVec(
    //      myRdAddr,
    //    //),
    //    cond=myRam.io.wrPulse.fire,
    //    //init=myRdAddr.getZero
    //  )
    //  init(0x1)
    //)

    //&& rValidVec(
    //  myRdAddr + 1
    //)

    //RegNext(
    //  RegNext(
    //    rValidVec(
    //      myRdAddr
    //    )
    //  ),
    //  init=False
    //)
    //|| (
    //  myRam.io.wrPulse.fire
    //  && myRam.io.wrPulse.addr === myRdAddr
    //)
  )
  myRam.io.rdAddrPipe.data := myRam.io.rdAddrPipe.data.getZero
  myRam.io.rdAddrPipe.data.reorderBufIdx.allowOverride
  myRam.io.rdAddrPipe.data.reorderBufIdx := myRdAddr
  myRam.io.rdAddrPipe.addr := myRdAddr
  
  //myTempPushStm.last.translateInto(myRam.io.rdAddrPipe)(
  //  dataAssignment=(outp, inp) => {
  //    //outp.data.most := inp.most
  //    //outp.data.most := outp
  //    outp.addr := (
  //      //inp.reorderBufIdx
  //      //RegNextWhen(
  //      //  (outp.addr + 1),
  //      //  cond=myRam.io.rdAddrPipe.fire,
  //      //  init=outp.addr.getZero,
  //      //)
  //      myRdAddr
  //    )
  //    //when (myRam.io.rdAddrPipe.fire) {
  //    //  rValidVec(outp.addr) := False
  //    //}
  //  }
  //)

  io.pop << myRam.io.rdDataPipe
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
    SnowHouseForFmaxPsWbCommitEtc(cfg=cfg)
  )
  //--------
  val myScoreboardSavedGprTagVec = (
    cfg.optScoreboard
  ) generate (
    in(
      UInt(cfg.numGprs bits)
    )
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

  if (!cfg.optScoreboard) {
    when (cLink.up.isValid) {
      myWbPayloadVec.head(1) := myWbPayloadVec.head(0)
    }
  }

  val myWbValidVec = (
    Vec.fill(currWbPayloadOuterVecSize)(
      Bool()
    )
  )

  case class MyWbPayload(
  ) extends Bundle {
    val instrCnt = SnowHouseInstrCnt(cfg=cfg)
    val outpDecodeExt = SnowHouseDecodeExt(cfg=cfg)
    val scoreboardOpIsMemAccess = Bool()

    val encInstr = (
      io.dbgInfo != null
    ) generate (
      UInt(cfg.instrMainWidth bits)
    )
    val laggingRegPc = (
      io.dbgInfo != null
    ) generate (
      UInt(cfg.mainAddrWidth bits)
    )

    val imm = (
      io.dbgInfo != null
    ) generate (
      Vec.fill(1)(
        UInt(cfg.mainWidth bits)
      )
    )

    val gprIdxVec = Vec.fill(
      if (io.dbgInfo != null) (
        cfg.maxNumGprsPerInstr
      ) else (
        1
      )
    )(
      UInt(log2Up(cfg.numGprs) bits)
    ) //simPublic()
    val gprIsZeroVec = Vec.fill(
      //if (io.dbgInfo != null) (
      //  cfg.maxNumGprsPerInstr
      //) else (
        1
      //)
    )(
      Vec.fill(
        //if (io.dbgInfo != null) (
        //  cfg.regFileCfg.modMemWordValidSize
        //) else (
          1
        //)
      )(
        Bool()
      )
    )

    val myExt = Vec[PipeRegFilePayloadExt[UInt, Bool]]{
      val myArr = ArrayBuffer[PipeRegFilePayloadExt[UInt, Bool]]()
      for (ydx <- 0 until cfg.regFileCfg.memArrSize) {
        myArr += myWbPayloadVec.head.head.mkOneExt(ydx=ydx)
      }
      myArr
    } //simPublic()
  }

  val myNonFwdWbFifo = StreamFifo(
    dataType=(
      //SnowHousePipePayload(cfg=cfg)
      MyWbPayload()
    ),
    depth=(
      //4
      //2
      1
    ),
    latency=0,
    forFMax=true
  )
  val myFwdWbFifo = StreamFifo(
    dataType=(
      //SnowHousePipePayload(cfg=cfg)
      MyWbPayload()
    ),
    depth=(
      //4
      //2
      1
    ),
    latency=0,
    forFMax=true
  )

  val myNonFwdWbPayload = myWbPayloadVec.head
  val myFwdWbPayload = myWbPayloadVec.last

  //val myNonFwdWbValid = myWbValidVec.head
  //val myFwdWbValid = myWbValidVec.last

  val myNonFwdWbValid = myNonFwdWbFifo.io.pop.valid
  val myFwdWbValid = myFwdWbFifo.io.pop.valid

  def mkScoreboardRdGprIdxVec(
    someMyWbPayload: SnowHousePipePayload,
  ) = {
    require(
      cfg.optScoreboard
    )
    val ret = (
      Vec.fill(cfg.regFileCfg.modRdPortCnt)(
        UInt(log2Up(cfg.numGprs) bits)
      )
    )
    for (idx <- 0 until cfg.regFileCfg.modRdPortCnt) {
      ret(idx) := someMyWbPayload.gprIdxVec(idx)
    }
    ret
  }

  def mkScoreboardGprTagOrReduce(
    someMyWbPayload: SnowHousePipePayload
  ): Bool = {
    require(
      cfg.optScoreboard
    )

    val temp = mkScoreboardRdGprIdxVec(someMyWbPayload)
    Vec(temp.map(item => {
      io.myScoreboardSavedGprTagVec(item)
    })).orR

    //val ret = (
    //  Vec.fill(cfg.regFileCfg.modRdPortCnt)(
    //    Bool()
    //  )
    //)
  }
  //val rMyScoreboardShouldIgnoreInstrState = (
  //  cfg.optScoreboard
  //) generate (
  //  Reg(Bool(), init=False)
  //)

  val myScoreboardWbFifoArea = (
    cfg.optScoreboard
  ) generate (new Area {
    //myNonFwdWbFifo.io.pop.ready := False
    //myFwdWbFifo.io.pop.ready := False

    //myNonFwdWbFifo.io.push.payload := (
    //  myNonFwdWbFifo.io.push.payload.getZero
    //)
    //myFwdWbFifo.io.push.payload := (
    //  myFwdWbFifo.io.push.payload.getZero
    //)

    //val rMyShouldIgnoreInstrState = Reg(Bool(), init=False)

    //when (
    //  io.up.fire
    //  && io.up.instrCnt.shouldIgnoreInstr.head
    //) {
    //  rMyShouldIgnoreInstrState := True
    //}
    //when (
    //  io.up.valid
    //  && !io.up.instrCnt.shouldIgnoreInstr.last
    //  //&& !myFwdWbFifo.io.pop.valid
    //) {
    //  rMyShouldIgnoreInstrState := False
    //}

    myNonFwdWbFifo.io.push.valid := (
      cLink.up.isValid
      && (
        //myNonFwdWbPayload(0).splitOp.opIsMemAccess
        (
          myNonFwdWbPayload(0).outpDecodeExt.opIsMemAccess.head
          && (
            //!(
            //  myNonFwdWbPayload(0).instrCnt.myPsIdBubble.head
            //  //|| myNonFwdWbPayload(0).instrCnt.shouldIgnoreInstr.head
            //  //&& !myNonFwdWbPayload(0).instrCnt.myScoreboardReadGprsBubble.head
            //)
            //|| myNonFwdWbPayload(0).instrCnt.shouldIgnoreInstr.head
            !myNonFwdWbPayload(0).instrCnt.myPsIdBubble.head
            //|| !io.myScoreboardSavedGprTagVec(
            //  myNonFwdWbPayload(0).gprIdxVec.last
            //)
            //|| !mkScoreboardGprTagOrReduce(myNonFwdWbPayload(0))
            ////!io.myScoreboardSavedGprTagVec(
            ////  myNonFwdWbPayload(0).gprIdxVec.last
            ////)
            //|| myNonFwdWbPayload(0).instrCnt.shouldIgnoreInstr.last
          )
        )
        || (
          myNonFwdWbPayload(0).instrCnt.shouldIgnoreInstr.head
          && !myFwdWbFifo.io.pop.valid
        )
      )
      //&& (
      //  !rMyShouldIgnoreInstrState
      //  || !myFwdWbValid
      //)
      && !myNonFwdWbPayload(0).instrCnt.myPsExMemAccessBubble.head
    )

    myNonFwdWbFifo.io.push.payload.instrCnt := (
      myNonFwdWbPayload(0).instrCnt
    )
    myNonFwdWbFifo.io.push.payload.outpDecodeExt := (
      myNonFwdWbPayload(0).outpDecodeExt
    )
    if (io.dbgInfo != null) {
      myNonFwdWbFifo.io.push.payload.laggingRegPc := (
        myNonFwdWbPayload(0).laggingRegPc
      )
      myNonFwdWbFifo.io.push.payload.imm.last := (
        myNonFwdWbPayload(0).imm.last
      )
      myNonFwdWbFifo.io.push.payload.encInstr := (
        myNonFwdWbPayload(0).encInstr.payload
      )
      myNonFwdWbFifo.io.push.payload.gprIdxVec := (
        myNonFwdWbPayload(0).gprIdxVec
      )
    } else {
      myNonFwdWbFifo.io.push.payload.gprIdxVec.last := (
        myNonFwdWbPayload(0).gprIdxVec.last
      )
    }
    myNonFwdWbFifo.io.push.payload.gprIsZeroVec.last.last := (
      myNonFwdWbPayload(0).gprIsZeroVec.last.last
    )
    myNonFwdWbFifo.io.push.payload.myExt := (
      myNonFwdWbPayload(0).myExt
    )
    myNonFwdWbFifo.io.push.payload.scoreboardOpIsMemAccess := (
      myNonFwdWbPayload(0).splitOp.scoreboardOpIsMemAccess
    )

    myFwdWbFifo.io.push.valid := (
      cLink.up.isValid
      && (
        //!myFwdWbPayload(0).splitOp.opIsMemAccess
        !myFwdWbPayload(0).outpDecodeExt.opIsMemAccess.last
        && !myFwdWbPayload(0).instrCnt.shouldIgnoreInstr.last
      )
      //&& !myFwdWbPayload(0).inpDecodeExt.last.opIsMemAccess(0)
      && (
        //!(
        //  myFwdWbPayload(0).instrCnt.myPsIdBubble.last
        //  //|| myFwdWbPayload(0).instrCnt.shouldIgnoreInstr.last
        //  //&& !myFwdWbPayload(0).instrCnt.myScoreboardReadGprsBubble.last
        //)
        !myFwdWbPayload(0).instrCnt.myPsIdBubble.last
        //|| !io.myScoreboardSavedGprTagVec(
        //  myFwdWbPayload(0).gprIdxVec.last
        //)
        //|| !mkScoreboardGprTagOrReduce(myFwdWbPayload(0))
        //|| myFwdWbPayload(0).instrCnt.shouldIgnoreInstr.last
      )
      && !myFwdWbPayload(0).instrCnt.myPsExMultiCycleBubble.last
    )
    myFwdWbFifo.io.push.payload.instrCnt := (
      myFwdWbPayload(0).instrCnt
    )
    myFwdWbFifo.io.push.payload.outpDecodeExt := (
      myFwdWbPayload(0).outpDecodeExt
    )
    myFwdWbFifo.io.push.payload.scoreboardOpIsMemAccess := (
      myFwdWbPayload(0).splitOp.scoreboardOpIsMemAccess
    )
    if (io.dbgInfo != null) {
      myFwdWbFifo.io.push.payload.laggingRegPc := (
        myFwdWbPayload(0).laggingRegPc
      )
      myFwdWbFifo.io.push.payload.imm.last := (
        myFwdWbPayload(0).imm.last
      )
      myFwdWbFifo.io.push.payload.encInstr := (
        myFwdWbPayload(0).encInstr.payload
      )
      myFwdWbFifo.io.push.payload.gprIdxVec := (
        myFwdWbPayload(0).gprIdxVec
      )
    } else {
      myFwdWbFifo.io.push.payload.gprIdxVec.last := (
        myFwdWbPayload(0).gprIdxVec.last
      )
    }
    myFwdWbFifo.io.push.payload.gprIsZeroVec.last.last := (
      myFwdWbPayload(0).gprIsZeroVec.last.last
    )
    myFwdWbFifo.io.push.payload.myExt := (
      myFwdWbPayload(0).myExt
    )

    when (
      (
        //cLink.up.isValid
        //&& myNonFwdWbPayload.
        myNonFwdWbFifo.io.push.valid
        && !myNonFwdWbFifo.io.push.ready
      )
      || (
        myFwdWbFifo.io.push.valid
        && !myFwdWbFifo.io.push.ready
      )
      || (
        !myNonFwdWbFifo.io.push.ready
        && !myFwdWbFifo.io.push.ready
      )
    ) {
      cLink.duplicateIt()
    }
    //when (
    //  cLink.up.isValid
    //  && myWbPayloadVec.head.head.instrCnt.myPsIdBubble.head
    //) {
    //  cLink.throwIt()
    //}
  })


  //myNonFwdWbValid := (
  //  RegNext(myNonFwdWbValid, init=myNonFwdWbValid.getZero)
  //)
  if (cfg.optScoreboard) {
    //myFwdWbValid := (
    //  RegNext(myFwdWbValid, init=myFwdWbValid.getZero)
    //)
  }

  val myD2hBus = cloneOf(io.myLcvDbusD2hStm)
  //val rSeenD2hBusFire = Reg(Bool(), init=False)

  //val rInstrCntMem = (
  //  Reg(cloneOf(myWbPayloadVec.head(1).instrCnt.mem))
  //  init(0x0)
  //)
  //val rInstrCntNonMem = (
  //  Reg(cloneOf(myWbPayloadVec.head(1).instrCnt.nonMem))
  //  init(0x0)
  //)
  //when (myD2hBus.fire) {
  //  rSeenD2hBusFire := True
  //}
  //val rSeenMyD2hBusFire = (
  //  cfg.optScoreboard
  //) generate (
  //  Reg(Bool(), init=False)
  //)
  val stickyMyD2hBusValid = (
    if (cfg.optScoreboard) (
      //myD2hBus.fire
      myD2hBus.valid
      //|| rSeenMyD2hBusFire
      || (
        //myNonFwdWbFifo.io.pop.valid
        myNonFwdWbFifo.io.pop.valid
        && (
          myNonFwdWbFifo.io.pop.instrCnt.shouldIgnoreInstr.head
          //|| myNonFwdWbFifo.io.pop.instrCnt.myPsIdBubble.head
          //|| (
          //  myNonFwdWbFifo.io.pop.instrCnt.myPsIdBubble.head
          //  && io.myScoreboardSavedGprTagVec(
          //    myNonFwdWbFifo.io.pop.gprIdxVec.last
          //  )
          //)
        )
        //&& !myNonFwdWbFifo.io.pop.payload.instrCnt.myPsIdBubble.head
      )
    ) else (
      myD2hBus.fire
    )
  )

  val stickyMemMmw = (
    cfg.optScoreboard
  ) generate (
    UInt(cfg.mainWidth bits)
  )

  val stickyMemMmwValid = (
    cfg.optScoreboard
  ) generate (
    Bool()
  )

  //val rMemCommitFire = (
  //  cfg.optScoreboard
  //) generate (
  //  Reg(Bool(), init=False)
  //)
  //val rNonMemCommitFire = (
  //  cfg.optScoreboard
  //) generate (
  //  Reg(Bool(), init=False)
  //)
  if (cfg.optScoreboard) {
    stickyMemMmw := (
      RegNext(
        stickyMemMmw,
        init=stickyMemMmw.getZero
      )
    )
    stickyMemMmwValid := (
      RegNext(
        stickyMemMmwValid,
        init=stickyMemMmwValid.getZero
      )
    )

    //when (myD2hBus.fire) {
    //  rSeenMyD2hBusFire := True
    //}
    when (
      myNonFwdWbFifo.io.pop.valid
    ) {
      //myNonFwdWbPayload(1) := myNonFwdWbFifo.io.pop.payload

      myNonFwdWbPayload(1).instrCnt := (
        myNonFwdWbFifo.io.pop.payload.instrCnt
      )
      myNonFwdWbPayload(1).outpDecodeExt := (
        myNonFwdWbFifo.io.pop.payload.outpDecodeExt
      )
      myNonFwdWbPayload(1).splitOp.scoreboardOpIsMemAccess := (
        myNonFwdWbFifo.io.pop.payload.scoreboardOpIsMemAccess
      )
      if (io.dbgInfo != null) {
        myNonFwdWbPayload(1).laggingRegPc := (
          myNonFwdWbFifo.io.pop.payload.laggingRegPc
        )
        myNonFwdWbPayload(1).imm.last := (
          myNonFwdWbFifo.io.pop.payload.imm.last
        )
        myNonFwdWbPayload(1).encInstr.payload := (
          myNonFwdWbFifo.io.pop.payload.encInstr
        )
        myNonFwdWbPayload(1).gprIdxVec := (
          myNonFwdWbFifo.io.pop.payload.gprIdxVec
        )
      } else {
        myNonFwdWbPayload(1).gprIdxVec.last := (
          myNonFwdWbFifo.io.pop.payload.gprIdxVec.last
        )
      }
      myNonFwdWbPayload(1).gprIsZeroVec.last.last := (
        myNonFwdWbFifo.io.pop.payload.gprIsZeroVec.last.last
      )
      myNonFwdWbPayload(1).myExt := (
        myNonFwdWbFifo.io.pop.payload.myExt
      )
    }
    when (
      myFwdWbFifo.io.pop.valid
    ) {
      //myFwdWbPayload(1) := myFwdWbFifo.io.pop.payload
      myFwdWbPayload(1).instrCnt := (
        myFwdWbFifo.io.pop.payload.instrCnt
      )
      myFwdWbPayload(1).outpDecodeExt := (
        myFwdWbFifo.io.pop.payload.outpDecodeExt
      )
      myFwdWbPayload(1).splitOp.scoreboardOpIsMemAccess := (
        myFwdWbFifo.io.pop.payload.scoreboardOpIsMemAccess
      )
      if (io.dbgInfo != null) {
        myFwdWbPayload(1).laggingRegPc := (
          myFwdWbFifo.io.pop.payload.laggingRegPc
        )
        myFwdWbPayload(1).imm.last := (
          myFwdWbFifo.io.pop.payload.imm.last
        )
        myFwdWbPayload(1).encInstr.payload := (
          myFwdWbFifo.io.pop.payload.encInstr
        )
        myFwdWbPayload(1).gprIdxVec := (
          myFwdWbFifo.io.pop.payload.gprIdxVec
        )
      } else {
        myFwdWbPayload(1).gprIdxVec.last := (
          myFwdWbFifo.io.pop.payload.gprIdxVec.last
        )
      }
      myFwdWbPayload(1).gprIsZeroVec.last.last := (
        myFwdWbFifo.io.pop.payload.gprIsZeroVec.last.last
      )
      myFwdWbPayload(1).myExt := (
        myFwdWbFifo.io.pop.payload.myExt
      )
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
          myNonFwdWbValid
          && myNonFwdWbFifo.io.pop.ready
        ) else (
          cLink.up.isValid
        )
      )
      && myNonFwdWbPayload(1).outpDecodeExt.opIsMemAccess.last
    ) {
      myD2hBus.ready := True
    }

    if (cfg.optScoreboard) {
      //when (
      //  myNonFwdWbValid
      //  && myNonFwdWbPayload(1).outpDecodeExt.opIsMemAccess.last
      //  && (
      //    !stickyMyD2hBusFire //!myD2hBus.valid
      //  )
      //) {
      //  cLink.duplicateIt()
      //}
    } else { // if (!cfg.optScoreboard)
      //when (
      //  (
      //    if (cfg.optScoreboard) (
      //      // TODO: maybe try `isValid` later (for fmax)?
      //      //cLink.up.isValid
      //      //&& !rCurrWbPayloadOuterIdx.lsb
      //      //RegNext(
      //      //  !myNonFwdWbValid,
      //      //  init=False
      //      //)
      //      //!rose(myNonFwdWbValid)
      //      //&& !myFwdWbValid
      //      myNonFwdWbValid
      //      && (
      //        cLink.up.isValid
      //        && RegNext(myNonFwdWbValid, init=False)
      //        && myWbPayloadVec.head(0).outpDecodeExt.opIsMemAccess.last
      //      )
      //      && !rMemCommitFire
      //    ) else (
      //      cLink.up.isValid
      //      && !myD2hBus.valid
      //      && myNonFwdWbPayload(1).outpDecodeExt.opIsMemAccess.last
      //    )
      //  )
      //  //cLink.up.isValid
      //  //&& myNonFwdWbPayload(1).outpDecodeExt.opIsMemAccess.last
      //  //&& !myD2hBus.valid
      //) {
      //  if (cfg.optScoreboard) {
      //    //rCurrWbPayloadOuterIdx.lsb := True
      //    cLink.duplicateIt()
      //  } else {
      //    cLink.duplicateIt()
      //  }
      //}

      when (
        cLink.up.isValid
        && myNonFwdWbPayload(1).outpDecodeExt.opIsMemAccess.last
        && !myD2hBus.fire
      ) {
        cLink.duplicateIt()
      }
    }
    if (cfg.optScoreboard) {
      //when (
      //  (
      //    //cLink.up.isValid
      //    //|| 
      //    rCurrWbPayloadOuterIdx.lsb
      //  )
      //  && myNonFwdWbPayload(1).outpDecodeExt.opIsMemAccess.last
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
            myNonFwdWbValid
          ) else (
            cLink.up.isValid
          )
        )
        && myNonFwdWbPayload(1).outpDecodeExt.opIsMemAccess.head
        && !myNonFwdWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
        && (
          //myD2hBus.valid
          myD2hBus.fire
          //stickyMyD2hBusFire
        )
      )
      ## myNonFwdWbPayload(1).outpDecodeExt.memAccessKind.asBits(0)
      ## myNonFwdWbPayload(1).outpDecodeExt.memAccessSubKind.asBits
    ) {
      //--------
      // This stuff might need to be changed for the purposes of
      // atomic operations that are larger than `cfg.mainWidth`.
      // It's currently limited to at max 32-bit values, for example, on a
      // 32-bit `cfg.mainWidth` CPU. More work will be needed later.
      //--------
      val myDecodeExt = myNonFwdWbPayload(1).outpDecodeExt
      //val mapElem = myNonFwdWbPayload(1).gprIdxToMemAddrIdxMap(0)
      //val myCurrExt = (
      //  if (!mapElem.haveHowToSetIdx) (
      //    myNonFwdWbPayload(1).myExt(
      //      0
      //    )
      //  ) else (
      //    myNonFwdWbPayload(1).myExt(
      //      mapElem.howToSetIdx
      //    )
      //  )
      //)
      //val myCurrExt = myNonFwdWbPayload(1).myExt(0)
      val myCurrMmw = (
        if (cfg.optScoreboard) (
          stickyMemMmw
        ) else (
          myNonFwdWbPayload(1).myExt(0).modMemWord
        )
      )
      //--------
      is (M"10--") {
        // zero-extending sub-word load or full-word load
        myCurrMmw := myD2hBus.data
      }
      is (M"1100") {
        // LoadS, Sz8
        myCurrMmw := (
          myD2hBus.data(
            (7.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrMmw.getWidth).asUInt
        )
      }
      is (M"1101") {
        // LoadS, Sz16
        myCurrMmw := (
          myD2hBus.data(
            (15.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrMmw.getWidth).asUInt
        )
      }
      is (M"1110") {
        // LoadS, Sz32
        myCurrMmw := (
          myD2hBus.data(
            (31.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrMmw.getWidth).asUInt
        )
      }
      is (M"1111") {
        // LoadS, Sz64
        myCurrMmw := (
          myD2hBus.data(
            (63.min(myD2hBus.data.high)) downto 0
          ).asSInt.resize(myCurrMmw.getWidth).asUInt
        )
      }
      default {
        //if (cfg.optScoreboard) {
        //  myCurrMmw := (
        //    // TODO: support other `rdMemWord` indices
        //    myNonFwdWbPayload(1).myExt(0).rdMemWord(1)
        //  )
        //}
      }
    }
    when (
      (
        if (cfg.optScoreboard) (
          //cLink.up.isValid
          //|| 
          myNonFwdWbValid //rCurrWbPayloadOuterIdx.lsb
        ) else (
          cLink.up.isValid
        )
      )
      //&& !myNonFwdWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
      && (
        //myD2hBus.valid
        myD2hBus.fire
        //stickyMyD2hBusFire
      )
    ) {
      val myDecodeExt = myNonFwdWbPayload(1).outpDecodeExt
      //val mapElem = myNonFwdWbPayload(1).gprIdxToMemAddrIdxMap(0)
      //val myCurrExt = (
      //  if (!mapElem.haveHowToSetIdx) (
      //    myNonFwdWbPayload(1).myExt(
      //      0
      //    )
      //  ) else (
      //    myNonFwdWbPayload(1).myExt(
      //      mapElem.howToSetIdx
      //    )
      //  )
      //)
      //val myCurrExt = myNonFwdWbPayload(1).myExt(0)
      val myCurrMmwValid = (
        if (cfg.optScoreboard) (
          stickyMemMmwValid
        ) else (
          myNonFwdWbPayload(1).myExt(0).modMemWordValid.last
        )
      )
      //myCurrExt.modMemWord := myDbus.recvData.word
      //myCurrExt.modMemWord := myD2hBus.data
      //myCurrExt.modMemWordValid.foreach(current => {
      //  current := (
      //    // TODO: support more destination GPRs
      //    //!myNonFwdWbPayload.gprIsZeroVec(0)
      //    True
      //  )
      //})
      //for (idx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
      //  myCurrExt.modMemWordValid(idx) := (
      //    !myNonFwdWbPayload(1).gprIsZeroVec.last(idx)
      //  )
      //}
      myCurrMmwValid := (
        if (cfg.optScoreboard) (
          !myNonFwdWbPayload(1).gprIsZeroVec.last.last
          //&& !myNonFwdWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
          && !myNonFwdWbPayload(1).instrCnt.myPsExMemAccessBubble.last
        ) else (
          !myNonFwdWbPayload(1).gprIsZeroVec.last.last
        )
      )
    }
  }

  cLink.up.driveFrom(io.up)(
    con=(node, inp) => {
      //node(pwbInp) := inp
      //myWbPayload(0) := inp
      if (cfg.optScoreboard) {
        myNonFwdWbPayload(0) := inp
        myFwdWbPayload(0) := inp
      } else {
        myWbPayloadVec.head(0) := inp
      }
    }
  )
  cLink.down.ready := True
  //if (!cfg.optScoreboard) {
  //  when (cLink.up.isValid) {
  //    myWbPayloadVec.head(1) := myWbPayloadVec.head(0)
  //  }
  //}

  //if (cfg.optScoreboard) {
  //  when (io.myRegFileWrPulse.fire) {
  //    rWbPayloadOuterIdx.lsb := !rWbPayloadOuterIdx.lsb
  //  }
  //}

  //val myMemCommitStm = cloneOf(io.commitEtc.scoreboardTag)
  //val myNonMemCommitStm = cloneOf(io.commitEtc.scoreboardTag)
  //val myCommitSel = UInt(1 bits)


  val myCommitFrontStmVec = (
    cfg.optScoreboard
  ) generate (
    Vec.fill(2)(
      Vec.fill(myWbPayloadVec.size)(
        //cloneOf(io.commitEtc.scoreboardTag)
        Stream(
          SnowHouseForFmaxPsWbReorderBufPayload(cfg=cfg)
        )
      )
    )
  )

  //def myInstrMayPassCntInitVal = cfg.optForFmaxPsExFwdSize - 2//1

  //val rInstrMayPassCnt = (
  //  cfg.optScoreboard
  //) generate (
  //  Reg(UInt(log2Up(cfg.optForFmaxPsExFwdSize) bits))
  //  init(myInstrMayPassCntInitVal)
  //)
  val myMemCommitFrontStm = (
    cfg.optScoreboard
  ) generate (
    myCommitFrontStmVec.head.head//last//head
  )
  val myNonMemCommitFrontStm = (
    cfg.optScoreboard
  ) generate (
    myCommitFrontStmVec.head.last//head//last
  )
  val myScoreboardCommitFrontStmArea = (
    cfg.optScoreboard
  ) generate (new Area {
    //for (idx <- 0 until myCommitFrontStmVec.size) {
    //  myCommitFrontStmVec.last(idx) << (
    //    myCommitFrontStmVec.head(idx)
    //  )
    //}
    for (idx <- 0 until myCommitFrontStmVec.size) {
      //val myThrowCondVec = Vec.fill(
      //  myCommitFrontStmVec.size //- 1
      //)(
      //  Bool()
      //)
      //for (jdx <- 0 until myCommitFrontStmVec.size) {
      //  myThrowCondVec(jdx) := (
      //    if (jdx == idx) (
      //      myCommitFrontStmVec.head(idx).reorderBufIdx
      //      === RegNextWhen(
      //        myCommitFrontStmVec.head(idx).reorderBufIdx,
      //        cond=myCommitFrontStmVec.last(idx),
      //      )
      //    ) else {
      //    }
      //  )
      //}
      myCommitFrontStmVec.last(idx) << (
        myCommitFrontStmVec.head(idx)
        //.throwWhen(
        //  myThrowCondVec.orR
        //)
      )
    }
  })
  val myCommitBackStm = (
    if (cfg.optScoreboard) (
      StreamArbiterFactory.lowerFirst.noLock.on(
        myCommitFrontStmVec.last
        //Vec(myCommitFrontStmVec.last.reverse)
      )
    ) else (
      Stream(
        SnowHouseForFmaxPsWbReorderBufPayload(cfg=cfg)
      )
    )
  )
  val myReorderBuf = (
    cfg.optScoreboard
  ) generate (
    SnowHouseForFmaxPsWbReorderBuf(cfg=cfg)
  )
  //val myCommitFinalInpStm = (
  //  if (cfg.optScoreboard) (
  //    myReorderBuf.io.push
  //  ) else (
  //    myCommitBackStm
  //  )
  //)
  val myCommitAlmostFinalOutpStm = (
    if (cfg.optScoreboard) (
      myReorderBuf.io.pop
    ) else (
      myCommitBackStm
    )
  )
  val myCommitTrueFinalOutpStm = (
    if (cfg.optScoreboard) (
      cloneOf(myCommitAlmostFinalOutpStm)
    ) else (
      myCommitAlmostFinalOutpStm
    )
  )
  //val myCommitForkStm = (
  //  cfg.optScoreboard
  //) generate (
  //  StreamFork(
  //    input=myCommitBackStm,
  //    portCount=2,
  //    synchronous=true,
  //  )
  //)
  if (cfg.optScoreboard) {
    //myCommitFinalOutpStm.ready := True
    myReorderBuf.io.postFlushReorderBufIdx := (
      RegNext(
        myReorderBuf.io.postFlushReorderBufIdx,
        init=myReorderBuf.io.postFlushReorderBufIdx.getZero
      )
    )
    when (
      io.up.valid
      && fell(
        io.up.instrCnt.shouldIgnoreInstr.last
      )
    ) {
      myReorderBuf.io.postFlushReorderBufIdx := (
        io.up.instrCnt.scoreboardIssuePayload.reorderBufIdx
      )
    }
    //val myTempReorderBufIdx = (
    //  myCommitBackStm.myWbPayload
    //  .instrCnt.scoreboardIssuePayload.reorderBufIdx
    //)

    val myTempCommitStm = (
      myCommitBackStm
    )
    //val myTempCommitStm = (
    //  cloneOf(myCommitBackStm)
    //)

    //myTempCommitStm << (
    //  myCommitBackStm.haltWhen(
    //    (
    //      myNonFwdWbFifo.io.pop.valid
    //      //|| myFwdWbFifo.io.pop.valid
    //      && !stickyMyD2hBusFire
    //    )
    //    && (
    //      myCommitBackStm.myShouldIgnoreInstr
    //    )
    //  )
    //)
    //myTempCommitStm << (
    //  myCommitBackStm.throwWhen(
    //    (
    //      myTempReorderBufIdx
    //      === (
    //        RegNextWhen(
    //          myTempReorderBufIdx,
    //          cond=(
    //            myTempCommitStm.fire
    //            //myCommitBackStm.fire
    //          ),
    //        )
    //        init(0x2)
    //      )
    //    )
    //    && !myCommitBackStm.myShouldIgnoreInstr
    //  )
    //)
    //io.commitEtc.myScoreboardFwdRegFileWrPulse.valid := (
    //  myTempCommitStm.fire
    //  && !myTempCommitStm.myWbPayload.splitOp.opIsMemAccess
    //)
    //io.commitEtc.myScoreboardFwdRegFileWrPulse.payload := (
    //  myTempCommitStm.regFileWrite
    //)
    myReorderBuf.io.push << {
      //myCommitForkStm.head
      myTempCommitStm
    }
  } else { // if (!cfg.optScoreboard)
    //myCommitBackStm
  }

  //val myCommitStmMux = StreamMux(
  //  select=myCommitSel,
  //  inputs=Vec(myMemCommitStm, myNonMemCommitStm)
  //)

  //val myRegFileWrPulseInpStmVec = (
  //  cfg.optScoreboard
  //) generate (
  //  Vec.fill(2)(
  //    Vec.fill(myWbPayloadVec.size)(
  //      Stream(
  //        PipeSimpleDualPortMemDrivePayload(
  //          dataType=UInt(cfg.mainWidth bits),
  //          wordCount=cfg.regFileCfg.wordCountArr(0),
  //        )
  //      )
  //    )
  //  )
  //)
  //val myRegFileWrPulseOutpStm = (
  //  if (cfg.optScoreboard) (
  //    StreamArbiterFactory.roundRobin.noLock.on(
  //      myRegFileWrPulseInpStmVec.last
  //    )
  //  ) else (
  //    Stream(
  //      PipeSimpleDualPortMemDrivePayload(
  //        dataType=UInt(cfg.mainWidth bits),
  //        wordCount=cfg.regFileCfg.wordCountArr(0),
  //      )
  //    )
  //  )
  //)
  //if (cfg.optScoreboard) {
  //  for (idx <- 0 until myRegFileWrPulseInpStmVec.size) {
  //    myRegFileWrPulseInpStmVec.last(idx) << (
  //      myRegFileWrPulseInpStmVec.head(idx)
  //    )
  //  }
  //}

  //myRegFileWrPulseOutpStm.ready := (
  //  if (cfg.optScoreboard) (
  //    io.commitEtc.scoreboardTag.ready
  //  ) else (
  //    True
  //  )
  //)

  //--------
  io.commitEtc.myRegFileWrPulse.valid := (
    if (cfg.optScoreboard) (
      //myCommitBackStm.fire
      myCommitAlmostFinalOutpStm.fire//valid
    ) else (
      //myCommitBackStm.valid
      myCommitAlmostFinalOutpStm.valid
    )
  )
  io.commitEtc.myRegFileWrPulse.payload := (
    //myCommitBackStm.regFileWrite
    myCommitAlmostFinalOutpStm.regFileWrite
  )
  if (cfg.optScoreboard) {
    //myCommitTrueFinalOutpStm <-< myCommitAlmostFinalOutpStm
    myCommitTrueFinalOutpStm << myCommitAlmostFinalOutpStm

    (
      //myCommitAlmostFinalOutpStm
      //myCommitForkStm.last
      myCommitTrueFinalOutpStm
    )
    .translateInto(io.commitEtc.scoreboardTag)(
      dataAssignment=(outp, inp) => {
        outp := inp.commit
      }
    )
    io.commitEtc.scoreboardReorderBufInFlushEtc := (
      myReorderBuf.io.inFlushEtc
    )
    io.commitEtc.scoreboardReorderBufPsIdCanIssue := (
      myReorderBuf.io.psIdCanIssue
    )
  }

  def setCommitEtc(
    someMyWbPayload: Vec[SnowHousePipePayload],
    someCommitStm: Stream[SnowHouseForFmaxPsWbReorderBufPayload],
    //someRegFileWrPulseStm: Stream[
    //  PipeSimpleDualPortMemDrivePayload[UInt]
    //],
    isNonFwd: Boolean,
  ): Unit = {
    val myTempNonFwdTag = (
      someMyWbPayload(1).instrCnt.scoreboardIssuePayload.nonFwdTag
    )
    val myTempFwdTag = (
      someMyWbPayload(1).instrCnt.scoreboardIssuePayload.fwdTag
    )
    val myHistNonFwdTag = (
      isNonFwd
    ) generate (
      History(
        that=myTempNonFwdTag,
        when=someCommitStm.fire,
        length=2,
        init=(
          U(s"${myTempNonFwdTag.getWidth}'d1")
        )
      )
    )
    val haveNewNonFwdTag = (
      isNonFwd
    ) generate (
      myHistNonFwdTag(0) =/= myHistNonFwdTag(1)
    )
    val myHistFwdTag = (
      !isNonFwd
    ) generate (
      History(
        that=myTempFwdTag,
        when=someCommitStm.fire,
        length=2,
        init=(
          U(s"${myTempFwdTag.getWidth}'d1")
        )
      )
    )
    val haveNewFwdTag = (
      !isNonFwd
    ) generate (
      myHistFwdTag(0) =/= myHistFwdTag(1)
    )

    if (cfg.optScoreboard) {
      someCommitStm.myShouldIgnoreInstr := (
        someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
        //&& !someMyWbPayload(1).instrCnt.myPsIdBubble.last
      )
      someCommitStm.myPsIdBubble := (
        someMyWbPayload(1).instrCnt.myPsIdBubble.last
        //&& !someMyWbPayload(1).instrCnt.myPsIdBubble.last
      )
      //someCommitStm.opIsMemAccess := (
      //  someMyWbPayload(1).splitOp.scoreboardOpIsMemAccess
      //)
      someCommitStm.reorderBufIdx := (
        someMyWbPayload(1).instrCnt.scoreboardIssuePayload.reorderBufIdx
      )
      //someCommitStm.postFlushReorderBufIdx := (
      //  //someMyWbPayload(1)
      //  RegNext(
      //  )
      //)
    } else {
      //someCommitStm.valid := True
      someCommitStm.ready := True
    }
    if (
      cfg.optScoreboard
      && isNonFwd
    ) {
      //when (
      //  someCommitStm.fire
      //) {
      //  rSeenMyD2hBusFire := False
      //}
      //when (
      //  myD2hBus.fire
      //) {
      //  rSeenMyD2hBusFire := True
      //}
    }
    val myNonMemRegFileWrPulseValidPartial = (
      cfg.optScoreboard
    ) generate (
      myFwdWbValid
      && someCommitStm.fire
      //&& !(
      //  myFwdWbPayload(1).instrCnt.myPsIdBubble.head
      //  //&& !myFwdWbPayload(1).instrCnt.myScoreboardReadGprsBubble.last
      //)
    )
    if (io.dbgInfo != null) {
      someCommitStm.myWbPayload := someMyWbPayload(1)
    }

    //someRegFileWrPulseStm.valid := (
    //  (
    //    if (cfg.optScoreboard) (
    //      (
    //        if (isNonFwd) (
    //          (
    //            //myNonFwdWbPayload(1).outpDecodeExt.opIsMemAccess(0)
    //            someCommitStm.fire
    //            && (
    //              !myNonFwdWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
    //            )
    //          )
    //        ) else (
    //          myNonMemRegFileWrPulseValidPartial
    //        )
    //      )
    //    ) else (
    //      cLink.up.isFiring
    //    )
    //  )
    //  && !someMyWbPayload(1).gprIsZeroVec.last.last
    //  && !someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
    //  && {
    //    if (cfg.optScoreboard && isNonFwd) {
    //      stickyMemMmwValid
    //    } else {
    //      val myDecodeExt = someMyWbPayload(1).outpDecodeExt
    //      val mapElem = someMyWbPayload(1).gprIdxToMemAddrIdxMap(0)
    //      val myCurrExt = (
    //        if (!mapElem.haveHowToSetIdx) (
    //          someMyWbPayload(1).myExt(
    //            0
    //          )
    //        ) else (
    //          someMyWbPayload(1).myExt(
    //            mapElem.howToSetIdx
    //          )
    //        )
    //      )
    //      //myCurrExt.modMemWord := myDbus.recvData.word
    //      //someMyWbPayload(1).
    //      myCurrExt.modMemWordValid.last
    //    }
    //  }
    //)

    if (isNonFwd) {
      someCommitStm.commit.nonFwdTag := myHistNonFwdTag(0)
      someCommitStm.commit.fwdTag := 0x0
      someCommitStm.commit.opIsFwd := False
    } else {
      someCommitStm.commit.nonFwdTag := 0x0
      someCommitStm.commit.fwdTag := myHistFwdTag(0)
      someCommitStm.commit.opIsFwd := True
    }
    when (
      (
        if (cfg.optScoreboard) (
          (
            if (isNonFwd) (
              (
                //myNonFwdWbPayload(1).outpDecodeExt.opIsMemAccess(0)
                someCommitStm.fire
                //&& (
                //  !myNonFwdWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
                //)
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
      //&& !someMyWbPayload(1).instrCnt.myPsIdBubble.last
      && (
        !someMyWbPayload(1).instrCnt.myPsIdBubble.last
        //|| !io.myScoreboardSavedGprTagVec(
        //  someMyWbPayload(1).gprIdxVec.last
        //)
        //|| !mkScoreboardGprTagOrReduce(someMyWbPayload(1))
      )
      && !someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
      && {
        if (cfg.optScoreboard && isNonFwd) {
          stickyMemMmwValid
        } else {
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
          myCurrExt.modMemWordValid.last
        }
      }
    ) {
      if (!cfg.optScoreboard) {
        someCommitStm.valid := True
      }
      someCommitStm.regFileWrite.addr := (
        someMyWbPayload(1).gprIdxVec.last
      )
      someCommitStm.regFileWrite.data := {
        if (cfg.optScoreboard && isNonFwd) {
          stickyMemMmw
        } else {
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
      }
      //someCommitStm.commit.myGprIdx.valid := (
      //  if (isNonFwd) (
      //    if (cfg.myHaveZeroReg) (
      //      !someMyWbPayload(1).gprIsZeroVec.last.last
      //    ) else (
      //      True
      //    )
      //  ) else (
      //    False
      //  )
      //)
      if (cfg.optScoreboard) {
        if (isNonFwd) {
          someCommitStm.commit.myNonFwdValid := (
            //if (cfg.myHaveZeroReg) (
            //  !someMyWbPayload(1).gprIsZeroVec.last.last
            //) else (
            //  True
            //)
            True
          )
          someCommitStm.commit.myFwdValid := False
        } else {
          someCommitStm.commit.myNonFwdValid := False
          someCommitStm.commit.myFwdValid := (
            //if (cfg.myHaveZeroReg) (
            //  !someMyWbPayload(1).gprIsZeroVec.last.last
            //) else (
            //  True
            //)
            True
          )
        }
        someCommitStm.commit.gprIdxVec := (
          someMyWbPayload(1).gprIdxVec
        )
      }
    } otherwise {
      if (!cfg.optScoreboard) {
        someCommitStm.valid := False
      }
      someCommitStm.regFileWrite.addr := 0x0
      someCommitStm.regFileWrite.data := 0x0

      if (cfg.optScoreboard) {
        someCommitStm.commit.gprIdxVec := (
          someMyWbPayload(1).gprIdxVec
        )
        if (isNonFwd) {
          someCommitStm.commit.myFwdValid := False
          someCommitStm.commit.myNonFwdValid := (
            someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
            && someMyWbPayload(1).splitOp.scoreboardOpIsMemAccess
            //&& !someMyWbPayload(1).instrCnt.myPsIdBubble.head
            //&& myNonFwdWbValid
            && (
              //someMyWbPayload(1).
              haveNewNonFwdTag
            )
          )
        } else {
          someCommitStm.commit.myNonFwdValid := False
          someCommitStm.commit.myFwdValid := (
            //False
            //!someMyWbPayload(1).splitOp.scoreboardOpIsMemAccess
            //!someMyWbPayload(1).instrCnt.myPsIdBubble.last
            //someMyWbPayload(1).instrCnt.myPsIdReorderBufForceValid.last
            //&& myFwdWbValid
            haveNewFwdTag
          )
        }
      }

      //someCommitStm.commit.myGprIdx.valid := (
      //  if (isNonFwd) (
      //    //(
      //    //  if (cfg.myHaveZeroReg) (
      //    //    !someMyWbPayload(1).gprIsZeroVec.last.last
      //    //  ) else (
      //    //    True
      //    //  )
      //    //)
      //    //&& someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
      //    //False
      //    someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
      //    && someMyWbPayload(1).splitOp.scoreboardOpIsMemAccess
      //  ) else (
      //    //False
      //    //(
      //    //  if (cfg.myHaveZeroReg) (
      //    //    !someMyWbPayload(1).gprIsZeroVec.last.last
      //    //  ) else (
      //    //    True
      //    //  )
      //    //)
      //    //&& 
      //    //someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
      //    //&& someMyWbPayload(1).splitOp.scoreboardOpIsMemAccess
      //    False
      //  )
      //)

      //when (
      //  someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
      //) {
      //} otherwise {
      //  someCommitStm.commit.myGprIdx := (
      //    //someMyWbPayload(1).gprIdxVec.last
      //    0x0
      //  )
      //}
    }
    if (cfg.optScoreboard) {
      //val rSeenUpIsFiring = (
      //  isNonFwd
      //) generate (
      //  Reg(Bool(), init=False)
      //)

      someCommitStm.valid := (
        (
          if (isNonFwd) (
            (
              //myD2hBus.fire
              //|| (
              //  cLink.up.isFiring
              //  && myNonFwdWbValid
              //  //&& !myFwdWbValid
              //  //&& someMyWbPayload(1).outpDecodeExt.opIsMemAccess(0)
              //)
              //True
              //!myNonFwdWbPayload(1).instrCnt.myPsIdBubble.last
              //True
              //!myNonFwdWbPayload(1).instrCnt.myPsIdBubble.last
              myNonFwdWbValid
              && stickyMyD2hBusValid
              //&& (
              //  !myFwdWbValid
              //  || !myFwdWbPayload(1).instrCnt.shouldIgnoreInstr.last
              //  || !myScoreboardWbFifoArea.rMyShouldIgnoreInstrState
              //)
              //&& (
              //  !myFwdWbValid
              //  || !someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
              //)
            )
          ) else (
            //cLink.up.isFiring
            //&& 
            //cLink.up.isValid
            //&& 
            myFwdWbValid
            //&& (
            //  !myNonFwdWbValid
            //  || stickyMyD2hBusFire
            //  || !someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
            //  || myScoreboardWbFifoArea.rMyShouldIgnoreInstrState
            //)

            //|| (
            //  myFwdWbFifo.io.pop.valid
            //  && myFwdWbFifo.io.pop.instrCnt.shouldIgnoreInstr.last
            //)
            //|| myFwdWbPayload(1).instrCnt.shouldIgnoreInstr.last
            //&& rInstrMayPassCnt.orR
            //&& !myFwdWbPayload(1).instrCnt.myPsIdBubble.last
          )
        )
      )

      //if (isNonFwd) {
      //  someCommitStm.opIsFwd
      //} else {
      //}

      //someCommitStm.commit.tag := (
      //  someMyWbPayload(1).instrCnt.scoreboardTag
      //)
      //someCommitStm.commit.myGprIdx := (
      //  someMyWbPayload(1).gprIdxVec.last
      //)
      //someCommitStm.commit.isBubbleEtc := (
      //  if (isNonFwd) (
      //  ) else (
      //  )
      //)

      if (isNonFwd) {
        myNonFwdWbFifo.io.pop.ready := someCommitStm.fire
      } else {
        myFwdWbFifo.io.pop.ready := someCommitStm.fire
      }
    } else { // if (!cfg.optScoreboard)
      //someCommitStm.valid := (
      //  myWbPayloadVec.head(1).myExt(0).modMemWordValid.head
      //)
    }
  }

  val myScoreboardArea = (
    cfg.optScoreboard
  ) generate (new Area {
    setCommitEtc(
      someMyWbPayload=myNonFwdWbPayload,
      someCommitStm=myMemCommitFrontStm,
      //someCommitStm=myCommitInpStmVec.head.head,
      //someRegFileWrPulseStm=myRegFileWrPulseInpStmVec.head.head,
      isNonFwd=true
    )
    setCommitEtc(
      someMyWbPayload=myFwdWbPayload,
      someCommitStm=myNonMemCommitFrontStm,
      //someCommitStm=myCommitInpStmVec.head.last,
      //someRegFileWrPulseStm=myRegFileWrPulseInpStmVec.head.last,
      isNonFwd=false
    )
  })

  if (!cfg.optScoreboard) {
    setCommitEtc(
      someMyWbPayload=myWbPayloadVec.head,
      someCommitStm=myCommitAlmostFinalOutpStm,
      //someRegFileWrPulseStm=myRegFileWrPulseOutpStm,
      isNonFwd=false
    )
  }
  if (io.dbgInfo != null) {
    io.dbgInfo := RegNext(io.dbgInfo, init=io.dbgInfo.getZero)
    io.dbgInfo.regFileWriteEnable.allowOverride 
    io.dbgInfo.regFileWriteEnable := False 
    //io.dbgInfo.regFileWriteData := (
    //  RegNext(
    //    io.dbgInfo.regFileWriteData
    //  )
    //)
    //io.dbgInfo.regFileWriteAddr := (
    //  RegNext(
    //    io.dbgInfo.regFileWriteAddr
    //  )
    //)
    //io.dbgInfo.regFileWriteEnable := False
    //io.dbgInfo.laggingRegPcAtRegFileWrite := (
    //  RegNext(
    //    io.dbgInfo.laggingRegPcAtRegFileWrite
    //  )
    //)
    //io.dbgInfo.shouldIgnoreInstrAtRegFileWrite := (
    //  RegNext(
    //    io.dbgInfo.shouldIgnoreInstrAtRegFileWrite
    //  )
    //)
    //io.dbgInfo.myPsIdBubbleAtRegFileWrite := (
    //  RegNext(
    //    io.dbgInfo.myPsIdBubbleAtRegFileWrite
    //  )
    //)
    //io.dbgInfo.encInstrAtRegFileWrite := (
    //  //someMyWbPayload(1).encInstr.payload
    //  RegNext(
    //    io.dbgInfo.encInstrAtRegFileWrite,
    //    init=io.dbgInfo.encInstrAtRegFileWrite.getZero
    //  )
    //)
    //io.dbgInfo.immAtRegFileWrite := (
    //  //someMyWbPayload(1).imm.last
    //  RegNext(
    //    io.dbgInfo.immAtRegFileWrite,
    //  )
    //)
    //io.dbgInfo.rdMemWordAtRegFileWrite := (
    //  //someMyWbPayload(1).myExt(0).rdMemWord
    //  RegNext(
    //    io.dbgInfo.rdMemWordAtRegFileWrite
    //  )
    //)
    //io.dbgInfo.gprIdxVecAtRegFileWrite := (
    //  //someMyWbPayload(1).gprIdxVec
    //  RegNext(
    //    io.dbgInfo.gprIdxVecAtRegFileWrite
    //  )
    //)

    val myDbgTempNonBubbleTag = (
      io.dbgInfo != null
    ) generate (
      //someMyWbPayload(1).instrCnt.scoreboardIssuePayload.nonFwdTag
      myCommitAlmostFinalOutpStm
      .myWbPayload.instrCnt.scoreboardIssuePayload.nonBubbleTag
    )
    val myDbgHistNonBubbleTag = (
      io.dbgInfo != null
    ) generate (
      History(
        that=myDbgTempNonBubbleTag,
        when=myCommitAlmostFinalOutpStm.fire,
        length=2,
        init=(
          U(s"${myDbgTempNonBubbleTag.getWidth}'d1")
        )
      )
    )
    val myDbgHaveNewNonBubbleTag = (
      io.dbgInfo != null
    ) generate (
      myDbgHistNonBubbleTag(0) =/= myDbgHistNonBubbleTag(1)
    )

    //val myDbgTempNonFwdTag = (
    //  io.dbgInfo != null
    //) generate (
    //  //someMyWbPayload(1).instrCnt.scoreboardIssuePayload.nonFwdTag
    //  myCommitAlmostFinalOutpStm
    //  .myWbPayload.instrCnt.scoreboardIssuePayload.nonFwdTag
    //)
    //val myDbgTempFwdTag = (
    //  io.dbgInfo != null
    //) generate (
    //  //someMyWbPayload(1).instrCnt.scoreboardIssuePayload.fwdTag
    //  myCommitAlmostFinalOutpStm
    //  .myWbPayload.instrCnt.scoreboardIssuePayload.fwdTag
    //)
    //val myDbgHistNonFwdTag = (
    //  io.dbgInfo != null
    //) generate (
    //  History(
    //    that=myDbgTempNonFwdTag,
    //    when=myCommitAlmostFinalOutpStm.fire,
    //    length=2,
    //    init=(
    //      U(s"${myDbgTempNonFwdTag.getWidth}'d1")
    //    )
    //  )
    //)
    //val myDbgHaveNewNonFwdTag = (
    //  io.dbgInfo != null
    //) generate (
    //  myDbgHistNonFwdTag(0) =/= myDbgHistNonFwdTag(1)
    //)
    //val myDbgHistFwdTag = (
    //  io.dbgInfo != null
    //) generate (
    //  History(
    //    that=myDbgTempFwdTag,
    //    when=myCommitAlmostFinalOutpStm.fire,
    //    length=2,
    //    init=(
    //      U(s"${myDbgTempFwdTag.getWidth}'d1")
    //    )
    //  )
    //)
    //val myDbgHaveNewFwdTag = (
    //  io.dbgInfo != null
    //) generate (
    //  myDbgHistFwdTag(0) =/= myDbgHistFwdTag(1)
    //)
    when (
      //myCommitOutpStm.fire
      myCommitAlmostFinalOutpStm.fire
    ) {
      io.dbgInfo.regFileWriteData := (
        myCommitAlmostFinalOutpStm.regFileWrite.data
      )
      io.dbgInfo.regFileWriteAddr := (
        myCommitAlmostFinalOutpStm.regFileWrite.addr
      )
      io.dbgInfo.regFileWriteEnable := (
        if (cfg.optScoreboard) (
          (
            myCommitAlmostFinalOutpStm.regFileWrite.addr =/= 0x0
          )
          && (
            myCommitAlmostFinalOutpStm.fire
          )
        ) else (
          myCommitAlmostFinalOutpStm.fire
        )
      )
      io.dbgInfo.laggingRegPcAtRegFileWrite := (
        myCommitAlmostFinalOutpStm.myWbPayload.laggingRegPc.resize(
          cfg.mainWidth bits
        )
      )
      io.dbgInfo.shouldIgnoreInstrAtRegFileWrite := (
        if (cfg.optScoreboard) (
          (
            (
              myCommitAlmostFinalOutpStm.myWbPayload
              .instrCnt.shouldIgnoreInstr.last
              || (
                !myCommitAlmostFinalOutpStm.fire
              )
            )
            || (
              !myDbgHaveNewNonBubbleTag
              //!Mux(
              //  !myCommitAlmostFinalOutpStm.commit.opIsFwd,
              //  myDbgHaveNewNonFwdTag,
              //  myDbgHaveNewFwdTag,
              //)
            )
          )
        ) else (
          myCommitAlmostFinalOutpStm.myWbPayload
          .instrCnt.shouldIgnoreInstr.last
        )
      )
      io.dbgInfo.myPsIdBubbleAtRegFileWrite := (
        if (cfg.optScoreboard) {
          val myInstrCnt = (
            myCommitAlmostFinalOutpStm.myWbPayload.instrCnt
          )
          (
            (
              (
                myInstrCnt.myPsIdBubble.last
                //&& !myInstrCnt.myScoreboardReadGprsBubble.last
                //&& !io.myScoreboardSavedGprTagVec(
                //  myCommitAlmostFinalOutpStm.myWbPayload.gprIdxVec.last
                //)
                //&& !mkScoreboardGprTagOrReduce(
                //  //myNonFwdWbPayload(0)
                //  myCommitAlmostFinalOutpStm.myWbPayload
                //)
              )
              //|| myInstrCnt.myPsExMemAccessBubble.last
              //|| myInstrCnt.myPsExMultiCycleBubble.last
              || (
                !myCommitAlmostFinalOutpStm.fire
              )
            )
            || (
              !myDbgHaveNewNonBubbleTag
              //!Mux(
              //  !myCommitAlmostFinalOutpStm.commit.opIsFwd,
              //  myDbgHaveNewNonFwdTag,
              //  myDbgHaveNewFwdTag,
              //)
            )
          )
        } else {
          myCommitAlmostFinalOutpStm.myWbPayload.instrCnt.myPsIdBubble.last
        }
      )
      when (myCommitAlmostFinalOutpStm.myWbPayload.encInstr.payload.orR) {
        io.dbgInfo.encInstrAtRegFileWrite := (
          myCommitAlmostFinalOutpStm.myWbPayload.encInstr.payload
        )
      }
      io.dbgInfo.immAtRegFileWrite := (
        myCommitAlmostFinalOutpStm.myWbPayload.imm.last
      )
      io.dbgInfo.rdMemWordAtRegFileWrite := (
        myCommitAlmostFinalOutpStm.myWbPayload.myExt(0).rdMemWord
      )
      io.dbgInfo.gprIdxVecAtRegFileWrite := (
        myCommitAlmostFinalOutpStm.myWbPayload.gprIdxVec
      )
    } otherwise {
      if (cfg.optScoreboard) {
        io.dbgInfo.shouldIgnoreInstrAtRegFileWrite := (
          True
          //!Mux(
          //  !myCommitAlmostFinalOutpStm.commit.opIsFwd,
          //  myDbgHaveNewNonFwdTag,
          //  myDbgHaveNewFwdTag,
          //)
        )
        io.dbgInfo.myPsIdBubbleAtRegFileWrite := (
          True
          //!Mux(
          //  !myCommitAlmostFinalOutpStm.commit.opIsFwd,
          //  myDbgHaveNewNonFwdTag,
          //  myDbgHaveNewFwdTag,
          //)
        )
      }
    }
  }

  Builder(linkArr)
  //--------
}
