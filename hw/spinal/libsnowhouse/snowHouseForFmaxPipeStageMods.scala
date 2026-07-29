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
  val cntOverflow = Bool()

 // reorder buffer index
  val reorderBufIdx = UInt(cfg.optScoreboardReorderBufWidth bits)
  val tag = UInt(cfg.optScoreboardTagWidth bits)
}

case class SnowHouseScoreboardReadGprsPayload(
  cfg: SnowHouseConfig,
) extends Bundle {
  val gprIdxVec = (
    Vec.fill(cfg.maxNumGprsPerInstr)(
      UInt(log2Up(cfg.numGprs) bits)
    )
  )
  val regPcSetItCnt = in(
    Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
      UInt(
        //cfg.instrCntWidth bits
        //2 bits
        cfg.regPcSetItCntWidth bits
      ) //Bool()
    )
  )
  val tag = UInt(cfg.optScoreboardTagWidth bits)
  val someNodeIsFiring = Bool()
}

case class SnowHouseScoreboardCommitPayload(
  cfg: SnowHouseConfig,
) extends Bundle {
  val tag = UInt(cfg.optScoreboardTagWidth bits)
}

case class SnowHouseForFmaxScoreboardIo(
  cfg: SnowHouseConfig,
) extends Bundle {
  require(
    cfg.optScoreboard
  )
  //--------
  val myBranchMispredictEtc = in(Bool())
  val issueRegPcSetItCnt = in(
    Vec.fill(cfg.lowerMyFanoutRegPcSetItCnt)(
      UInt(
        //cfg.instrCntWidth bits
        //2 bits
        cfg.regPcSetItCntWidth bits
      ) //Bool()
    )
  )
  //val regPcSetItCnt = in(
  //--------
  val issueGprIdxVec = (
    in(
      //Vec.fill(cfg.numMultiIssue)(
        Vec.fill(cfg.maxNumGprsPerInstr)(
          UInt(log2Up(cfg.numGprs) bits)
        )
      //)
    )
  )
  val issueMyTempOpMayNeedHazardCheck = (
    in(
      Bool()
    )
  )

  val issue = (
    //Vec.fill(cfg.numMultiIssue)(
      Stream(
        //UInt(cfg.optScoreboardTagWidth bits)
        SnowHouseScoreboardIssuePayload(cfg=cfg)
      )
    //)
  )

  //val readGprsPayload = (
  //  in(
  //    //Vec.fill(cfg.numMultiIssue)(
  //      //Stream(
  //        SnowHouseScoreboardReadGprsPayload(cfg=cfg)
  //      //)
  //    //)
  //  )
  //)

  //val readGprsReady = (
  //  out(Bool())
  //)
  val readGprs = (
    Stream(SnowHouseScoreboardReadGprsPayload(cfg=cfg))
  )

  val reorderBufWrite = (
    //Vec.fill(cfg.numMultiIssue)(
      Stream(
        //UInt(cfg.optScoreboardTagWidth bits)
        SnowHouseScoreboardCommitPayload(cfg=cfg)
      )
    //)
  )

  //for (idx <- 0 until cfg.numMultiIssue) {
  //  master(issue(idx))
  //  slave(commit(idx))
  //}
  master(issue)
  slave(readGprs)
  slave(reorderBufWrite)

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
  //--------
  val io = SnowHouseForFmaxScoreboardIo(cfg=cfg)
  //--------
  val rMyPsExSetPcState = (
    Vec.fill(2)(
      Reg(Bool(), init=False)
    )
  )

  //for (idx <- 0 until rMyPsExSetPcState.size) {
  //  when (!rMyPsExSetPcState(idx)) {
  //    when (io.myBranchMispredictEtc) {
  //      rMyPsExSetPcState(idx) := True
  //    }
  //  } otherwise {
  //    when (
  //      if (idx == 0) (
  //        io.issue.fire
  //        && io.issueRegPcSetItCnt(0).lsb
  //      ) else (
  //        io.readGprs.fire
  //        && io.readGprs.regPcSetItCnt(0).lsb
  //      )
  //    ) {
  //      rMyPsExSetPcState(idx) := False
  //    }
  //  }
  //}

  //val myIssueSharedShouldIgnoreCond = (
  //  !(
  //    rMyPsExSetPcState.head
  //    && !io.issueRegPcSetItCnt(1).lsb
  //  )
  //)
  //val myReadGprsSharedShouldIgnoreCond = (
  //  !(
  //    rMyPsExSetPcState.last
  //    && !io.readGprs.regPcSetItCnt(1).lsb
  //  )
  //)

  val myInstrAgeWidth = 12//4//5//4//6//8//12
  val myMaxInstrAge = (
    // we flush the pipeline when this counter gets close to overflowing!
    // it is assumed there are fewer pipeline stages
    // than the subtract amount 
    (1 << myInstrAgeWidth) - 1 - 32//2//1//8 //- //32//64
  )

  case class FlushInfoPayload(
  ) extends Bundle {
    //val instrAgeCnt = UInt(myInstrAgeWidth bits)
    val dontCare = Bool()
  }
  val rFlushInfo = {
    val temp = Reg(Flow(FlushInfoPayload()))
    temp.init(temp.getZero)
    temp
  }

  case class MyInfo(
  ) extends Bundle {
    //val hazardValid = Bool()
    val issueHazardValid = Bool()
    val readGprsHazardValid = Bool()
    val readGprsHazardValidFwdLimit = Bool()
    //def fire = hazardValid
    val issueAllocValid = Bool()
    val instrAge = UInt(myInstrAgeWidth bits) //cloneOf(rFlushInfo)

    val gprIsNonZeroVec = (
      Vec.fill(
        cfg.maxNumGprsPerInstr
        //1
      )(
        Bool()
      )
    )
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
    //(io.gprIdxVec.size - 1) * 2 + 1
    //io.issueGprIdxVec.size
    1
  )
  val tempHaveIssueHazardAddrCheckVec = (
    // WAW hazards
    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
      Vec.fill(
        //io.gprIdxVec.size + 2
        myIssueHazardCheckVecInnerSize
      )(
        Bool()
      )
    )
  )

  val myReadGprsHazardCheckVecInnerSize = (
    io.readGprs.gprIdxVec.size - 1
  )

  val tempHaveReadGprsHazardAddrCheckVec = (
    // non-forwardable RAW hazards
    // TODO: this should be switched to be computed in the "Issue" stage
    // at some point (for fmax)
    //--------
    // perhaps instead it'd make sense to just add more pipeline stages
    // (at least one)
    // between `...ScoreboardReadGprs` and `...PreFwd`?
    //--------
    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
      Vec.fill(
        myReadGprsHazardCheckVecInnerSize
      )(
        Bool()
      )
    )
  )
  //val tempHaveReadGprsHazardAddrCheckFwdLimitVec = (
  //  // TODO: this should be switched to be computed in the "Issue" stage
  //  // at some point (for fmax)
  //  cloneOf(
  //    tempHaveReadGprsHazardAddrCheckVec
  //  )
  //)

  val myCommitHazardCheckVecInnerSize = (
    // WAR hazards
    io.issueGprIdxVec.size - 1
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
    // WAW hazards
    val tempRegIdx = io.issueGprIdxVec.last
    for (jdx <- 0 until tempHaveIssueHazardAddrCheckVec.size) {
      //tempHaveIssueHazardAddrCheckVec(jdx)(idx) := False
      tempHaveIssueHazardAddrCheckVec(jdx)(idx) := (
        //False
        (
          //tempRegIdx === myHistLastGprIdx(jdx + 1)(idx % 3)

          //tempRegIdx === rMyInfoVec(jdx).gprIdxVec(
          //  idx % io.gprIdxVec.size
          //)
          tempRegIdx === rMyInfoVec(jdx).gprIdxVec.last
          //&& tempRegIdx.orR // check for non-zero
          && rMyInfoVec(jdx).gprIsNonZeroVec.last
          //&& (
          //  rMyInfoVec(jdx).hazardValid
          //  //|| io.myTempOpMayNeedHazardCheck
          //)
          && rMyInfoVec(jdx).issueAllocValid
        )
      )
    }
  }

  for (idx <- 0 until myReadGprsHazardCheckVecInnerSize) {
    // (non-forwardable) RAW hazards
    val tempRegIdx = io.readGprs.gprIdxVec(idx) //io.issueGprIdxVec(idx)
    for (jdx <- 0 until cfg.optMaxNumScoreboardInstrs) {
      val tempCmp = (
        (
          (
            tempRegIdx === rMyInfoVec(jdx).gprIdxVec.last
          )
          //|| (
          //  // technically this is a WAR hazard
          //  rMyInfoVec(jdx).gprIdxVec(idx) === io.readGprs.gprIdxVec.last
          //)
        )
        //&& tempRegIdx.orR // check for non-zero
        && rMyInfoVec(jdx).gprIsNonZeroVec.last
        //&& (
        //  rMyInfoVec(jdx).hazardValid
        //  //|| io.myTempOpMayNeedHazardCheck
        //)
        && rMyInfoVec(jdx).issueAllocValid
      )
      tempHaveReadGprsHazardAddrCheckVec(jdx)(idx) := (
        tempCmp
        && (
          rMyInfoVec(jdx).readGprsHazardValid
          //|| rMyInfoVec(io.readGprs.tag).issueHazardValid
        )
      )
      //tempHaveReadGprsHazardAddrCheckFwdLimitVec(jdx)(idx) := (
      //  tempCmp
      //  && (
      //    //!rMyInfoVec(jdx).readGprsHazardValid
      //    //&& 
      //    rMyInfoVec(jdx).readGprsHazardValidFwdLimit
      //    //|| rMyInfoVec(io.readGprs.tag).issueHazardValid
      //  )
      //)
      //tempHaveReadGprsHazardAddrCheckVec(jdx)(idx) := (
      //  (
      //    //tempRegIdx === myHistLastGprIdx(jdx + 1).last
      //    tempRegIdx === rMyInfoVec(jdx).gprIdxVec.last
      //    //&& tempRegIdx.orR // check for non-zero
      //    && rMyInfoVec(jdx).gprIsNonZeroVec.last
      //    && (
      //      // other "RAW" hazards will be handled via my implementation of
      //      // fast forwarding!
      //      rMyInfoVec(jdx).readGprsHazardValid
      //      || rMyInfoVec(io.readGprs.tag).issueHazardValid
      //      //rMyInfoVec(io.readGprs.tag).readGprsHazardValid
      //      //|| io.myTempOpMayNeedHazardCheck
      //      //|| (
      //      //  io.readGprs.valid
      //      //  && io.readGprs.tag === jdx
      //      //  //&& tempHaveReadGprsHazardAddrCheckVec(jdx).orR
      //      //  //&& rMyInfoVec(jdx).issueAllocValid
      //      //  && rMyInfoVec(jdx).issueHazardValid
      //      //)
      //    )
      //    && rMyInfoVec(jdx).issueAllocValid
      //    //&& io.readGprs.valid
      //  )
      //)
      when (
        //io.readGprs.valid
        //&& tempHaveReadGprsHazardAddrCheckVec(jdx).orR
        //&& 
        io.readGprs.fire
        && io.readGprs.tag === jdx
        && rMyInfoVec(jdx).issueAllocValid
        //&& rMyInfoVec(jdx).issueHazardValid
      ) {
        rMyInfoVec(jdx).readGprsHazardValid := (
          rMyInfoVec(jdx).issueHazardValid
          //True
        )
        //rMyInfoVec(jdx).readGprsHazardValidFwdLimit := (
        //  True
        //)
      }
    }
  }

  //def myReadGprsInstrMayPassCntInitVal = (
  //  cfg.optForFmaxPsExFwdSize - 3//2//3//2//1//2//1
  //)
  //def myReadGprsInstrMayPassCntInitVal = 2

  //val rReadGprsInstrMayPassCnt = (
  //  cfg.optScoreboard
  //) generate (
  //  Reg(UInt(log2Up(myReadGprsInstrMayPassCntInitVal + 1) bits))
  //  init(myReadGprsInstrMayPassCntInitVal)
  //)
  io.readGprs.ready := (
    (
      io.readGprs.valid
      && (
        !tempHaveReadGprsHazardAddrCheckVec.asBits.orR
        //&& rReadGprsInstrMayPassCnt.orR
        //|| (
        //  io.reorderBufWrite.fire
        //  && (
        //    io.reorderBufWrite.tag === io.readGprs.tag
        //  )
        //)
      )
      //&& !rMyInfoVec(io.readGprs.tag).readGprsHazardValid
    )
    //|| rFlushInfo.fire
  )
  //--------
  //when (
  //  io.readGprs.fire
  //) {
  //  rReadGprsInstrMayPassCnt := rReadGprsInstrMayPassCnt - 1
  //}
  //when (
  //  io.readGprs.valid
  //  && !io.readGprs.ready
  //  && !tempHaveReadGprsHazardAddrCheckVec.asBits.orR
  //  && io.readGprs.someNodeIsFiring
  //  //&& !tempHaveReadGprsHazardAddrCheckFwdLimitVec.asBits.orR
  //) {
  //  rReadGprsInstrMayPassCnt := myReadGprsInstrMayPassCntInitVal
  //}
  //--------
  //switch (
  //  //io.readGprs.fire
  //  io.readGprs.valid
  //  ## io.readGprs.ready
  //  //## io.readGprs.someNodeIsFiring
  //  ## tempHaveReadGprsHazardAddrCheckVec.asBits.orR
  //  //## tempHaveReadGprsHazardAddrCheckFwdLimitVec.asBits.orR
  //  //## (rReadGprsInstrMayPassCnt < myReadGprsInstrMayPassCntInitVal)
  //) {
  //  is (
  //    //M"11--"
  //    M"11-"
  //    //M"101"
  //    //M"101"
  //  ) {
  //    rReadGprsInstrMayPassCnt := rReadGprsInstrMayPassCnt - 1
  //  }
  //  is (
  //    //M"0--"
  //    //M"0-"
  //    //M"1-0"
  //    //M"1-0-"
  //    M"1-0"

  //    //M"1-0"
  //  ) {
  //    //when (rReadGprsInstrMayPassCnt < myReadGprsInstrMayPassCntInitVal) {
  //    //  rReadGprsInstrMayPassCnt := rReadGprsInstrMayPassCnt + 1
  //    //} otherwise {
  //    //}
  //    rReadGprsInstrMayPassCnt := myReadGprsInstrMayPassCntInitVal
  //  }
  //  default {
  //  }
  //}
  //when (
  //  io.readGprs.fire
  //  //&& !tempHaveReadGprsHazardAddrCheckVec.asBits.orR
  //  && tempHaveReadGprsHazardAddrCheckFwdLimitVec.asBits.orR
  //) {
  //  rReadGprs
  //}

  //for (idx <- 0 until myCommitHazardCheckVecInnerSize) {
  //  // WAR hazards
  //  val tempRegIdx = (
  //    //rMyInfoVec(io.commit.tag).gprIdxVec(idx)
  //    rMyInfoVec(io.reorderBufWrite.tag).gprIdxVec.last
  //  )
  //  for (jdx <- 0 until cfg.optMaxNumScoreboardInstrs) {
  //    val myTempInfoGprIdx = (
  //      //rMyInfoVec(jdx).gprIdxVec.last
  //      rMyInfoVec(jdx).gprIdxVec(idx)
  //    )
  //    tempHaveCommitHazardAddrCheckVec(jdx)(idx) := (
  //      //tempRegIdx === myHistLastGprIdx(jdx + 1).last
  //      //tempRegIdx === rMyInfoVec(jdx).gprIdxVec(idx)
  //      tempRegIdx === myTempInfoGprIdx
  //      //&& myTempInfoGprIdx.orR // check for non-zero
  //      && rMyInfoVec(jdx).gprIsNonZeroVec(idx)
  //      && (
  //        rMyInfoVec(io.reorderBufWrite.tag).instrAge
  //        > rMyInfoVec(jdx).instrAge
  //      )
  //      //&& rMyInfoVec(io.commit.tag).allocValid
  //      //&& rMyInfoVec(jdx).hazardValid
  //      //&& (
  //      //  //rMyInfoVec(jdx).hazardValid
  //      //  //|| 
  //      //  rMyInfoVec(io.commit.tag).hazardValid
  //      //)
  //      && rMyInfoVec(jdx).issueAllocValid
  //      && io.reorderBufWrite.tag =/= jdx
  //      //&& io.commit.valid
  //    )
  //  }
  //}
  io.reorderBufWrite.ready := (
    //io.reorderBufWrite.valid
    //&& 
    //!tempHaveCommitHazardAddrCheckVec.asBits.orR
    True
  )

  val myInfoAllocValidVec = (
    Vec.fill(cfg.optMaxNumScoreboardInstrs)(
      Bool()
    )
    //Vec(rMyInfoVec.reverse.map(item => item.hazardValid))
  )

  for (jdx <- 0 until cfg.optMaxNumScoreboardInstrs) {
    when (io.reorderBufWrite.fire && io.reorderBufWrite.tag === jdx) {
      //myInfoAllocValidVec(jdx) := False
      //tempHaveIssueHazardAddrCheckVec(jdx).foreach(
      //  item => (
      //    item := False
      //  )
      //)
      rMyInfoVec(jdx).issueAllocValid := False
      //rMyInfoVec(jdx).hazardValid := False
      rMyInfoVec(jdx).issueHazardValid := False
      rMyInfoVec(jdx).readGprsHazardValid := False
      //rMyInfoVec(jdx).readGprsHazardValidFwdLimit := False
    } otherwise {
      //myInfoAllocValidVec(jdx) := rMyInfoVec(jdx).allocValid
    }
    myInfoAllocValidVec(jdx) := rMyInfoVec(jdx).issueAllocValid
  }


  io.issue.payload.allowOverride
  io.issue.valid := (
    //True
    !tempHaveIssueHazardAddrCheckVec.asBits.orR
    //&& !tempHaveCommitHazardAddrCheckVec.asBits.orR
    && !rFlushInfo.fire
  )
  io.issue.payload := (
    RegNext(io.issue.payload, init=io.issue.payload.getZero)
  )
  //io.issue.cntOverflow := rFlushInfo.fire
  io.issue.cntOverflow := rFlushInfo.fire
  io.issue.reorderBufIdx := (
    RegNextWhen(
      (io.issue.reorderBufIdx + 1),
      cond=io.issue.fire,
      init=io.issue.reorderBufIdx.getZero,
    )
  )

// >>> for x in range(8):
// ...     print(x, bin(x), bin(x ^ 0x7), bin(Bitscan(x ^ 0x7)))
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
    Bitscan(~myInfoAllocValidVec.asBits.asUInt)
  ) {
    val size = myInfoAllocValidVec.size
    for (idx <- 0 until size) {
      is (MaskedLiteral(
        //"1" + 
        ("-" * (size - idx - 1) + "1" + ("0" * idx))
      )) {
        // fast-ish (regarding fmax) search to implement the free list
        // search
        //io.issue.valid := (
        //  //True
        //  !tempHaveIssueHazardAddrCheckVec.asBits.orR
        //  //&& !tempHaveCommitHazardAddrCheckVec.asBits.orR
        //  && !rFlushInfo.fire
        //)
        //io.issue.payload := (
        //  RegNext(io.issue.payload, init=io.issue.payload.getZero)
        //)
        when (io.issue.fire) {
          io.issue.tag := idx
          //rFlushInfo.instrAgeCnt := rFlushInfo.instrAgeCnt + 1
          //rMyInfoVec(idx).instrAge := rFlushInfo.instrAgeCnt
          rMyInfoVec(idx).issueHazardValid := (
            io.issueMyTempOpMayNeedHazardCheck
            //True
          )
          //rMyInfoVec(idx).readGprsHazardValid := (
          //  io.issueMyTempOpMayNeedHazardCheck
          //  && tempHaveReadGprsHazardAddrCheckVec.asBits.orR
          //)
          rMyInfoVec(idx).issueAllocValid := (
            //io.myTempOpMayNeedHazardCheck
            True
          )

          rMyInfoVec(idx).gprIdxVec := io.issueGprIdxVec
          rMyInfoVec(idx).gprIsNonZeroVec := (
            //io.issueGprIdxVec.last.orR // check for non-zero
            // check for non-zero
            Vec(io.issueGprIdxVec.map(item => item.orR))
          )
          //for (jdx <- 0 until io.gprIdxVec.size) {
          //  rMyInfoVec(idx).gprIsNonZeroVec(jdx) := (
          //    io.gprIdxVec(jdx).orR // check for non-zero
          //  )
          //}
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
  switch (
    rFlushInfo.fire
    ## (
      //(rFlushInfo.instrAgeCnt === myMaxInstrAge)
      //|| 
      io.myBranchMispredictEtc
    )
    ## myInfoAllocValidVec.orR
  ) {
    // flush the pipeline
    is (M"01-") {
      rFlushInfo.valid := True
      //rFlushInfo.instrAgeCnt := 0x0

      //io.issue.cntOverflow := True
    }
    is (M"1-0") {
      // we're done flushing the pipeline
      // when every element of `rMyInfoVec` has been deallocated
      rFlushInfo.valid := False
      //io.issue.cntOverflow := False
    }
    default {
    }
  }
  //when (!rFlushInfo.fire) {
  //  when (rFlushInfo.payload === myMaxInstrAge) {
  //  }
  //}
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

  //val myScoreboardCommmit = (
  //  cfg.optScoreboard
  //) generate (
  //  slave(Stream(
  //    //UInt(cfg.optScoreboardTagWidth bits)
  //    SnowHouseScoreboardCommitPayload(cfg=cfg)
  //  ))
  //)
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

case class SnowHouseForFmaxPipeStageScoreboardIssueIo(
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
  val myBranchMispredictEtc = (
    in(
      Bool()
    )
  )
  //--------
  val myScoreboardReadGprs = (
    slave(Stream(
      SnowHouseScoreboardReadGprsPayload(cfg=cfg)
    ))
  )

  val myScoreboardCommmit = (
    cfg.optScoreboard
  ) generate (
    slave(Stream(
      //UInt(cfg.optScoreboardTagWidth bits)
      SnowHouseScoreboardCommitPayload(cfg=cfg)
    ))
  )
  //--------
}

case class SnowHouseForFmaxPipeStageScoreboardIssue(
  cfg: SnowHouseConfig
) extends Component {
  // technically this is the pipeline stage where the scoreboard itself is
  // stored too
  require(
    cfg.optScoreboard
  )
  //--------
  val io = SnowHouseForFmaxPipeStageScoreboardIssueIo(cfg=cfg)
  //--------
  val linkArr = PipeHelper.mkLinkArr()

  //def opInfoMap = cfg.opInfoMap

  //val pScoreboardIssueInp = Payload(SnowHousePipePayload(cfg=cfg))
  val pScoreboardIssueOutp = Payload(SnowHousePipePayload(cfg=cfg))
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

  val sLinkArr = new ArrayBuffer[StageLink]()
  val s2mLinkArr = new ArrayBuffer[S2MLink]()
  sLinkArr += StageLink(
    up=cLink.down,
    down=Node(),
  )
  s2mLinkArr += S2MLink(
    up=sLinkArr.last.down,
    down=Node(),
  )
  //sLinkArr += StageLink(
  //  up=sLinkArr.last.down,
  //  down=Node()
  //)
  //s2mLinkArr += S2MLink(
  //  up=sLinkArr.last.down,
  //  down=Node(),
  //)

  linkArr += cLink
  linkArr ++= sLinkArr
  linkArr ++= s2mLinkArr
  //linkArr += sLink
  //linkArr += s2mLink

  val scoreboard = (
    cfg.optScoreboard
  ) generate (
    SnowHouseForFmaxScoreboard(cfg=cfg)
  )

  val myInp = SnowHousePipePayload(cfg=cfg)
  val myOutp = SnowHousePipePayload(cfg=cfg)

  cLink.up.driveFrom(io.up)(
    con=(node, inp) => {
      //node(pScoreboardIssueInp) := inp
      myInp := inp
    }
  )

  myOutp := RegNext(myOutp, init=myOutp.getZero)
  when (cLink.up.isValid) {
    myOutp := myInp
  }
  
  //val rMyPsExSetPcState = (
  //  Reg(Bool(), init=False)
  //)

  //when (!rMyPsExSetPcState) {
  //  when (io.myBranchMispredictEtc) {
  //    rMyPsExSetPcState := True
  //  }
  //} otherwise {
  //  when (
  //    //cLink.down.isFiring
  //    cLink.up.isFiring
  //    && myOutp.regPcSetItCnt(0).lsb
  //  ) {
  //    rMyPsExSetPcState := False
  //  }
  //}

  //val mySharedNonShouldIgnoreCond = (
  //  (
  //    !rMyPsExSetPcState
  //    || myOutp.regPcSetItCnt(1).lsb
  //  )
  //)

  scoreboard.io.myBranchMispredictEtc := io.myBranchMispredictEtc

  scoreboard.io.issueRegPcSetItCnt := (
    myOutp.regPcSetItCnt
  )
  scoreboard.io.issueMyTempOpMayNeedHazardCheck := (
    myOutp.instrCnt.myScoreboardOpMayNeedHazardCheck
  )
  scoreboard.io.issue.ready := (
    //cLink.up.isFiring // cLink.down.isFiring
    cLink.down.isFiring
    //&& mySharedNonShouldIgnoreCond
    //cLink.down.isFiring
    //cLink.up.isValid
    //&& cLink.down.isReady
  )
  scoreboard.io.issueGprIdxVec := myOutp.gprIdxVec
  //myOutp.instrCnt.scoreboardTag.allowOverride
  //myOutp.instrCnt.scoreboardTag := (
  //  scoreboard.io.issue.tag
  //)
  myOutp.instrCnt.scoreboardIssuePayload.allowOverride
  myOutp.instrCnt.scoreboardIssuePayload := (
    scoreboard.io.issue.payload
  )
  //myOutp.tempUpMod
  cLink.down(pScoreboardIssueOutp) := myOutp
  cLink.down(pScoreboardIssueOutp).allowOverride

  when (
    !scoreboard.io.issue.valid//fire
    //&& mySharedNonShouldIgnoreCond
  ) {
    cLink.duplicateIt()
    cLink.down(pScoreboardIssueOutp).setAsBubbleMain(
      //!scoreboard.io.issue.cntOverflow
      Some(True)
    )
    cLink.down(pScoreboardIssueOutp).gprIdxVec.foreach(gprIdx => {
      gprIdx := 0x0
    })
    //myOutp.instrCnt.scoreboardTag := (
    //  scoreboard.io.issue.tag
    //)
    //myOutp.myDoHaveHazardAddrCheckVec.foreach(
    //  item => {
    //    item := True
    //  }
    //)
    //myOutp.myDoHaveHazardAddrCheckVec.head := (
    //  True
    //)
  }
  scoreboard.io.readGprs << io.myScoreboardReadGprs
  scoreboard.io.reorderBufWrite << io.myScoreboardCommmit

  s2mLinkArr.last.down.driveTo(io.down)(
    con=(outp, node) => {
      outp := node(pScoreboardIssueOutp)
    }
  )

  Builder(linkArr)
}
case class SnowHouseForFmaxPipeStageScoreboardReadGprsIo(
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
  val readGprs = (
    master(Stream(
      SnowHouseScoreboardReadGprsPayload(cfg=cfg)
    ))
  )
  //val readGprsPayload = (
  //  out(
  //    //Vec.fill(cfg.numMultiIssue)(
  //      //Stream(
  //        SnowHouseScoreboardReadGprsPayload(cfg=cfg)
  //      //)
  //    //)
  //  )
  //)

  //val readGprsReady = (
  //  in(Bool())
  //)

  val myBranchMispredictEtc = (
    in(
      Bool()
    )
  )
  //--------
}
case class SnowHouseForFmaxPipeStageScoreboardReadGprs(
  cfg: SnowHouseConfig
) extends Component {
  require(
    cfg.optScoreboard
  )
  //--------
  val io = SnowHouseForFmaxPipeStageScoreboardReadGprsIo(cfg=cfg)
  //--------
  val linkArr = PipeHelper.mkLinkArr()

  //def opInfoMap = cfg.opInfoMap

  //val pScoreboardReadGprsInp = Payload(SnowHousePipePayload(cfg=cfg))
  val pScoreboardReadGprsOutp = Payload(SnowHousePipePayload(cfg=cfg))
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
  val sLinkArr = new ArrayBuffer[StageLink]()
  //val s2mLinkArr = new ArrayBuffer[S2MLink]()
  sLinkArr += StageLink(
    up=cLink.down,
    down=Node(),
  )
  //sLinkArr += StageLink(
  //  up=sLinkArr.last.down,
  //  down=Node(),
  //)
  //sLinkArr += StageLink(
  //  up=sLinkArr.last.down,
  //  down=Node(),
  //)
  //s2mLinkArr += S2MLink(
  //  up=sLinkArr.last.down,
  //  down=Node(),
  //)
  //sLinkArr += StageLink(
  //  up=sLinkArr.last.down,
  //  down=Node()
  //)
  //s2mLinkArr += S2MLink(
  //  up=sLinkArr.last.down,
  //  down=Node(),
  //)
  //sLinkArr += StageLink(
  //  up=sLinkArr.last.down,
  //  down=Node()
  //)
  //sLinkArr += StageLink(
  //  up=sLinkArr.last.down,
  //  down=Node()
  //)
  //sLinkArr += StageLink(
  //  up=sLinkArr.last.down,
  //  down=Node()
  //)
  linkArr += cLink
  linkArr ++= sLinkArr
  //linkArr ++= s2mLinkArr
  //linkArr += sLink
  //linkArr += s2mLink

  val myInp = SnowHousePipePayload(cfg=cfg)
  val myOutp = SnowHousePipePayload(cfg=cfg)

  cLink.up.driveFrom(io.up)(
    con=(node, inp) => {
      //node(pScoreboardReadGprsInp) := inp
      myInp := inp
    }
  )

  myOutp := RegNext(myOutp, init=myOutp.getZero)
  when (cLink.up.isValid) {
    myOutp := myInp
  }

  io.readGprs.gprIdxVec := myOutp.gprIdxVec
  io.readGprs.tag := myOutp.instrCnt.scoreboardTag
  io.readGprs.regPcSetItCnt := myOutp.regPcSetItCnt
  //val rStallState = Reg(Bool(), init=False)

  //when (!rStallState) {
  //}

  //val rSeenReadGprsFire = Reg(Bool(), init=False)
  //val stickyReadGprsFire = (
  //  io.readGprs.fire
  //  || rSeenReadGprsFire
  //)

  //when (io.readGprs.fire) {
  //  rSeenReadGprsFire := True
  //}
  //when (cLink.down.isFiring) {
  //  rSeenReadGprsFire := False
  //}

  //val rMyPsExSetPcState = (
  //  Reg(Bool(), init=False)
  //)

  //when (!rMyPsExSetPcState) {
  //  when (io.myBranchMispredictEtc) {
  //    rMyPsExSetPcState := True
  //  }
  //} otherwise {
  //  when (
  //    //cLink.down.isFiring
  //    cLink.up.isFiring
  //    && myOutp.regPcSetItCnt(0).lsb
  //  ) {
  //    rMyPsExSetPcState := False
  //  }
  //}

  val mySharedNonShouldIgnoreCond = (
    //cLink.up.isValid
    //&& 
    Vec(myOutp.instrCnt.myPsIdBubble.map(
      item => (
        !item
        //&& (
        //  !rMyPsExSetPcState
        //  || !myOutp.regPcSetItCnt(1).lsb
        //)
      )
    ))
    //&& (
    //  !rMyPsExSetPcState
    //  || myOutp.regPcSetItCnt(1).lsb
    //)
  )

  io.readGprs.valid := (
    cLink.up.isValid
    && cLink.down.isReady
    //cLink.up.isFiring
    //cLink.down.isFiring
    //&& !myOutp.instrCnt.myPsIdBubble.head
    && mySharedNonShouldIgnoreCond.head
  )
  io.readGprs.someNodeIsFiring := (
    cLink.down.isFiring
  )


  when (
    cLink.up.isValid
    && mySharedNonShouldIgnoreCond.last
    && io.readGprs.valid
    && !io.readGprs.ready 
  ) {
    cLink.duplicateIt()
    cLink.down(pScoreboardReadGprsOutp).allowOverride
    cLink.down(pScoreboardReadGprsOutp) := myOutp//.getZero

    cLink.down(pScoreboardReadGprsOutp).setAsBubbleMain(
      //!scoreboard.io.issue.cntOverflow
      Some(True)
      //myPsIdBubble=True,
      //myUpdateGprIsOrIsntZero=true
    )
    //cLink.down(pScoreboardReadGprsOutp).gprIsZeroVec.foreach(
    //  outerItem => {
    //    outerItem.foreach(item => {
    //      item := True
    //    })
    //  }
    //)
    //innerPsId.upPayload(1).myDoHaveHazardAddrCheckVec.foreach(
    //  item => {
    //    item := True
    //  }
    //)
    //innerPsId.upPayload(1).myDoHaveHazardAddrCheckVec.head := (
    //  True
    //)
  } otherwise {
    cLink.down(pScoreboardReadGprsOutp) := myOutp
  }
  //when (
  //  cLink.up.isValid
  //  && myInp.instrCnt.myPsIdBubble.head
  //) {
  //  cLink.throwIt()
  //}

  sLinkArr.last.down.driveTo(io.down)(
    con=(outp, node) => {
      outp := node(pScoreboardReadGprsOutp)
    }
  )

  Builder(linkArr)
}

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
  optIncludeBufIdx: Boolean=true,
) extends Bundle {
  val commit = (
    cfg.optScoreboard
  ) generate (
    //cloneOf(io.commitEtc.scoreboardTag.payload)
    SnowHouseScoreboardCommitPayload(cfg=cfg)
  )
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
  def regFileWrite = most.regFileWrite
  def myWbPayload = most.myWbPayload

  val reorderBufIdx = (
    cfg.optScoreboard
    && optIncludeBufIdx
  ) generate (
    UInt(cfg.optScoreboardReorderBufWidth bits)
  )
}

case class SnowHouseForFmaxPsWbReorderBufIo(
  cfg: SnowHouseConfig
) extends Bundle {
  val push = slave(Stream(
    SnowHouseForFmaxPsWbReorderBufPayload(cfg=cfg)
  ))
  val pop = master(Stream(
    SnowHouseForFmaxPsWbReorderBufPayload(
      cfg=cfg,
      optIncludeBufIdx=false,
    )
  ))
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
          switch (
            (
              wrPulse.fire
              && wrPulse.addr === inp.reorderBufIdx
            )
            ## (
              RegNextWhen(
                wrPulse.addr,
                cond=wrPulse.fire,
                init=wrPulse.addr.getZero
              ) === inp.reorderBufIdx
            )
          ) {
            is (M"1-") {
              outp.most := wrPulse.data.most
            }
            is (M"01") {
              outp.most := (
                RegNextWhen(
                wrPulse.data.most,
                  cond=wrPulse.fire,
                  init=wrPulse.data.most.getZero
                )
              )
            }
            default {
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

  //myFifo.io.push << io.push
  //myFifo.io.pop.ready := False
  val rValidVec = Vec.fill(myReorderBufSize)(
    Reg(Bool(), init=False)
  )

  io.push.ready := True
  myRam.io.wrPulse.valid := io.push.valid//fire//valid//valid//fire
  myRam.io.wrPulse.addr := io.push.reorderBufIdx
  //.resize(
  //  log2Up(rValidVec.size) bits
  //)
  myRam.io.wrPulse.data.most := io.push.most
  when (myRam.io.wrPulse.fire) {
    rValidVec(myRam.io.wrPulse.addr) := True
  }
  when (
    myRam.io.rdAddrPipe.fire
  ) {
    rValidVec(myRam.io.rdAddrPipe.addr) := False
  }

  //val myTempPushStm = Vec.fill(2)(
  //  cloneOf(io.pop)
  //  //Stream(
  //  //  cloneOf(io.push.payload)
  //  //)
  //)
  //myTempPushStm.head.valid := True
  //myTempPushStm.head.most := io.push.most
  val myRdAddr = cloneOf(myRam.io.rdAddrPipe.addr)
  myRdAddr := (
    RegNextWhen(
      (myRdAddr + 1),
      cond=myRam.io.rdAddrPipe.fire,
      init=myRdAddr.getZero,
    )
  )
  //myTempPushStm.last << myTempPushStm.head.haltWhen
  myRam.io.rdAddrPipe.valid := (
    rValidVec(
      myRdAddr
    )
    || (
      myRam.io.wrPulse.fire
      && myRam.io.wrPulse.addr === myRdAddr
    )
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

  val myMemWbFifo = StreamFifo(
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
  val myNonMemWbFifo = StreamFifo(
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

  val myMemWbPayload = myWbPayloadVec.head
  val myNonMemWbPayload = myWbPayloadVec.last

  //val myMemWbValid = myWbValidVec.head
  //val myNonMemWbValid = myWbValidVec.last

  val myMemWbValid = myMemWbFifo.io.pop.valid
  val myNonMemWbValid = myNonMemWbFifo.io.pop.valid

  val myScoreboardWbFifoArea = (
    cfg.optScoreboard
  ) generate (new Area {
    //myMemWbFifo.io.pop.ready := False
    //myNonMemWbFifo.io.pop.ready := False

    //myMemWbFifo.io.push.payload := (
    //  myMemWbFifo.io.push.payload.getZero
    //)
    //myNonMemWbFifo.io.push.payload := (
    //  myNonMemWbFifo.io.push.payload.getZero
    //)
    myMemWbFifo.io.push.valid := (
      cLink.up.isValid
      && myMemWbPayload(0).splitOp.opIsMemAccess
      && !myMemWbPayload(0).instrCnt.myPsIdBubble.head
      && !myNonMemWbPayload(0).instrCnt.myPsExMemAccessBubble.last
    )

    myMemWbFifo.io.push.payload.instrCnt := (
      myMemWbPayload(0).instrCnt
    )
    myMemWbFifo.io.push.payload.outpDecodeExt := (
      myMemWbPayload(0).outpDecodeExt
    )
    if (io.dbgInfo != null) {
      myMemWbFifo.io.push.payload.laggingRegPc := (
        myMemWbPayload(0).laggingRegPc
      )
      myMemWbFifo.io.push.payload.imm.last := (
        myMemWbPayload(0).imm.last
      )
      myMemWbFifo.io.push.payload.encInstr := (
        myMemWbPayload(0).encInstr.payload
      )
      myMemWbFifo.io.push.payload.gprIdxVec := (
        myMemWbPayload(0).gprIdxVec
      )
    } else {
      myMemWbFifo.io.push.payload.gprIdxVec.last := (
        myMemWbPayload(0).gprIdxVec.last
      )
    }
    myMemWbFifo.io.push.payload.gprIsZeroVec.last.last := (
      myMemWbPayload(0).gprIsZeroVec.last.last
    )
    myMemWbFifo.io.push.payload.myExt := (
      myMemWbPayload(0).myExt
    )

    myNonMemWbFifo.io.push.valid := (
      cLink.up.isValid
      && !myNonMemWbPayload(0).splitOp.opIsMemAccess
      //&& !myNonMemWbPayload(0).inpDecodeExt.last.opIsMemAccess(0)
      && !myNonMemWbPayload(0).instrCnt.myPsIdBubble.last
      && !myNonMemWbPayload(0).instrCnt.myPsExMultiCycleBubble.last
    )
    myNonMemWbFifo.io.push.payload.instrCnt := (
      myNonMemWbPayload(0).instrCnt
    )
    myNonMemWbFifo.io.push.payload.outpDecodeExt := (
      myNonMemWbPayload(0).outpDecodeExt
    )
    if (io.dbgInfo != null) {
      myNonMemWbFifo.io.push.payload.laggingRegPc := (
        myNonMemWbPayload(0).laggingRegPc
      )
      myNonMemWbFifo.io.push.payload.imm.last := (
        myNonMemWbPayload(0).imm.last
      )
      myNonMemWbFifo.io.push.payload.encInstr := (
        myNonMemWbPayload(0).encInstr.payload
      )
      myNonMemWbFifo.io.push.payload.gprIdxVec := (
        myNonMemWbPayload(0).gprIdxVec
      )
    } else {
      myNonMemWbFifo.io.push.payload.gprIdxVec.last := (
        myNonMemWbPayload(0).gprIdxVec.last
      )
    }
    myNonMemWbFifo.io.push.payload.gprIsZeroVec.last.last := (
      myNonMemWbPayload(0).gprIsZeroVec.last.last
    )
    myNonMemWbFifo.io.push.payload.myExt := (
      myNonMemWbPayload(0).myExt
    )

    when (
      (
        //cLink.up.isValid
        //&& myMemWbPayload.
        myMemWbFifo.io.push.valid
        && !myMemWbFifo.io.push.ready
      )
      || (
        myNonMemWbFifo.io.push.valid
        && !myNonMemWbFifo.io.push.ready
      )
      || (
        !myMemWbFifo.io.push.ready
        && !myNonMemWbFifo.io.push.ready
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


  //myMemWbValid := (
  //  RegNext(myMemWbValid, init=myMemWbValid.getZero)
  //)
  if (cfg.optScoreboard) {
    //myNonMemWbValid := (
    //  RegNext(myNonMemWbValid, init=myNonMemWbValid.getZero)
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
  val rSeenMyD2hBusFire = (
    cfg.optScoreboard
  ) generate (
    Reg(Bool(), init=False)
  )
  val stickyMyD2hBusFire = (
    if (cfg.optScoreboard) (
      myD2hBus.fire
      || rSeenMyD2hBusFire
      || (
        //myMemWbFifo.io.pop.valid
        myMemWbFifo.io.pop.valid
        && myMemWbFifo.io.pop.payload.instrCnt.shouldIgnoreInstr.head
        //&& !myMemWbFifo.io.pop.payload.instrCnt.myPsIdBubble.head
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

    when (myD2hBus.fire) {
      rSeenMyD2hBusFire := True
    }
    when (
      myMemWbFifo.io.pop.valid
    ) {
      //myMemWbPayload(1) := myMemWbFifo.io.pop.payload

      myMemWbPayload(1).instrCnt := (
        myMemWbFifo.io.pop.payload.instrCnt
      )
      myMemWbPayload(1).outpDecodeExt := (
        myMemWbFifo.io.pop.payload.outpDecodeExt
      )
      if (io.dbgInfo != null) {
        myMemWbPayload(1).laggingRegPc := (
          myMemWbFifo.io.pop.payload.laggingRegPc
        )
        myMemWbPayload(1).imm.last := (
          myMemWbFifo.io.pop.payload.imm.last
        )
        myMemWbPayload(1).encInstr.payload := (
          myMemWbFifo.io.pop.payload.encInstr
        )
        myMemWbPayload(1).gprIdxVec := (
          myMemWbFifo.io.pop.payload.gprIdxVec
        )
      } else {
        myMemWbPayload(1).gprIdxVec.last := (
          myMemWbFifo.io.pop.payload.gprIdxVec.last
        )
      }
      myMemWbPayload(1).gprIsZeroVec.last.last := (
        myMemWbFifo.io.pop.payload.gprIsZeroVec.last.last
      )
      myMemWbPayload(1).myExt := (
        myMemWbFifo.io.pop.payload.myExt
      )
    }
    when (
      myNonMemWbFifo.io.pop.valid
    ) {
      //myNonMemWbPayload(1) := myNonMemWbFifo.io.pop.payload
      myNonMemWbPayload(1).instrCnt := (
        myNonMemWbFifo.io.pop.payload.instrCnt
      )
      myNonMemWbPayload(1).outpDecodeExt := (
        myNonMemWbFifo.io.pop.payload.outpDecodeExt
      )
      if (io.dbgInfo != null) {
        myNonMemWbPayload(1).laggingRegPc := (
          myNonMemWbFifo.io.pop.payload.laggingRegPc
        )
        myNonMemWbPayload(1).imm.last := (
          myNonMemWbFifo.io.pop.payload.imm.last
        )
        myNonMemWbPayload(1).encInstr.payload := (
          myNonMemWbFifo.io.pop.payload.encInstr
        )
        myNonMemWbPayload(1).gprIdxVec := (
          myNonMemWbFifo.io.pop.payload.gprIdxVec
        )
      } else {
        myNonMemWbPayload(1).gprIdxVec.last := (
          myNonMemWbFifo.io.pop.payload.gprIdxVec.last
        )
      }
      myNonMemWbPayload(1).gprIsZeroVec.last.last := (
        myNonMemWbFifo.io.pop.payload.gprIsZeroVec.last.last
      )
      myNonMemWbPayload(1).myExt := (
        myNonMemWbFifo.io.pop.payload.myExt
      )
    }
  } else {
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

    if (cfg.optScoreboard) {
      //when (
      //  myMemWbValid
      //  && myMemWbPayload(1).outpDecodeExt.opIsMemAccess.last
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
      //      //  !myMemWbValid,
      //      //  init=False
      //      //)
      //      //!rose(myMemWbValid)
      //      //&& !myNonMemWbValid
      //      myMemWbValid
      //      && (
      //        cLink.up.isValid
      //        && RegNext(myMemWbValid, init=False)
      //        && myWbPayloadVec.head(0).outpDecodeExt.opIsMemAccess.last
      //      )
      //      && !rMemCommitFire
      //    ) else (
      //      cLink.up.isValid
      //      && !myD2hBus.valid
      //      && myMemWbPayload(1).outpDecodeExt.opIsMemAccess.last
      //    )
      //  )
      //  //cLink.up.isValid
      //  //&& myMemWbPayload(1).outpDecodeExt.opIsMemAccess.last
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
        && myMemWbPayload(1).outpDecodeExt.opIsMemAccess.last
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
      //val mapElem = myMemWbPayload(1).gprIdxToMemAddrIdxMap(0)
      //val myCurrExt = (
      //  if (!mapElem.haveHowToSetIdx) (
      //    myMemWbPayload(1).myExt(
      //      0
      //    )
      //  ) else (
      //    myMemWbPayload(1).myExt(
      //      mapElem.howToSetIdx
      //    )
      //  )
      //)
      //val myCurrExt = myMemWbPayload(1).myExt(0)
      val myCurrMmw = (
        if (cfg.optScoreboard) (
          stickyMemMmw
        ) else (
          myMemWbPayload(1).myExt(0).modMemWord
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
        if (cfg.optScoreboard) {
          myCurrMmw := (
            // TODO: support other `rdMemWord` indices
            myMemWbPayload(1).myExt(0).rdMemWord(1)
          )
        }
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
      //&& !myMemWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
      && (
        //myD2hBus.valid
        myD2hBus.fire
        //stickyMyD2hBusFire
      )
    ) {
      val myDecodeExt = myMemWbPayload(1).outpDecodeExt
      //val mapElem = myMemWbPayload(1).gprIdxToMemAddrIdxMap(0)
      //val myCurrExt = (
      //  if (!mapElem.haveHowToSetIdx) (
      //    myMemWbPayload(1).myExt(
      //      0
      //    )
      //  ) else (
      //    myMemWbPayload(1).myExt(
      //      mapElem.howToSetIdx
      //    )
      //  )
      //)
      //val myCurrExt = myMemWbPayload(1).myExt(0)
      val myCurrMmwValid = (
        if (cfg.optScoreboard) (
          stickyMemMmwValid
        ) else (
          myMemWbPayload(1).myExt(0).modMemWordValid.last
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
      //for (idx <- 0 until cfg.regFileCfg.modMemWordValidSize) {
      //  myCurrExt.modMemWordValid(idx) := (
      //    !myMemWbPayload(1).gprIsZeroVec.last(idx)
      //  )
      //}
      myCurrMmwValid := (
        !myMemWbPayload(1).gprIsZeroVec.last.last
        //&& !myMemWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
      )
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
  if (cfg.optScoreboard) {
    for (idx <- 0 until myCommitFrontStmVec.size) {
      myCommitFrontStmVec.last(idx) << (
        myCommitFrontStmVec.head(idx)
      )
    }
  }
  val myCommitBackStm = (
    if (cfg.optScoreboard) (
      StreamArbiterFactory.lowerFirst.noLock.on(
        myCommitFrontStmVec.last
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
  val myCommitFinalOutpStm = (
    if (cfg.optScoreboard) (
      myReorderBuf.io.pop
    ) else (
      myCommitBackStm
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
    myReorderBuf.io.push << (
      //myCommitForkStm.head
      myCommitBackStm
    )
  } else { // if (!cfg.optScoreboard)
    myCommitBackStm
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
      myCommitFinalOutpStm.fire
    ) else (
      //myCommitBackStm.valid
      myCommitFinalOutpStm.valid
    )
  )
  io.commitEtc.myRegFileWrPulse.payload := (
    //myCommitBackStm.regFileWrite
    myCommitFinalOutpStm.regFileWrite
  )
  if (cfg.optScoreboard) {
    (
      myCommitFinalOutpStm
      //myCommitForkStm.last
    )
    .translateInto(io.commitEtc.scoreboardTag)(
      dataAssignment=(outp, inp) => {
        outp := inp.commit
      }
    )
  }

  def setCommitEtc(
    someMyWbPayload: Vec[SnowHousePipePayload],
    someCommitStm: Stream[SnowHouseForFmaxPsWbReorderBufPayload],
    //someRegFileWrPulseStm: Stream[
    //  PipeSimpleDualPortMemDrivePayload[UInt]
    //],
    isMem: Boolean,
  ): Unit = {
    if (cfg.optScoreboard) {
      someCommitStm.reorderBufIdx := (
        someMyWbPayload(1).instrCnt.scoreboardIssuePayload.reorderBufIdx
      )
    } else {
      //someCommitStm.valid := True
      someCommitStm.ready := True
    }
    if (
      cfg.optScoreboard
      && isMem
    ) {
      when (someCommitStm.fire) {
        rSeenMyD2hBusFire := False
      }
    }
    val myNonMemRegFileWrPulseValidPartial = (
      cfg.optScoreboard
    ) generate (
      myNonMemWbValid
      && someCommitStm.fire
      && !myNonMemWbPayload(1).instrCnt.myPsIdBubble.head
    )
    if (io.dbgInfo != null) {
      someCommitStm.myWbPayload := someMyWbPayload(1)
    }

    //someRegFileWrPulseStm.valid := (
    //  (
    //    if (cfg.optScoreboard) (
    //      (
    //        if (isMem) (
    //          (
    //            //myMemWbPayload(1).outpDecodeExt.opIsMemAccess(0)
    //            someCommitStm.fire
    //            && (
    //              !myMemWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
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
    //    if (cfg.optScoreboard && isMem) {
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
    when (
      (
        if (cfg.optScoreboard) (
          (
            if (isMem) (
              (
                //myMemWbPayload(1).outpDecodeExt.opIsMemAccess(0)
                someCommitStm.fire
                //&& (
                //  !myMemWbPayload(1).outpDecodeExt.memAccessKind.asBits(1)
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
      && !someMyWbPayload(1).instrCnt.shouldIgnoreInstr.last
      && {
        if (cfg.optScoreboard && isMem) {
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
        if (cfg.optScoreboard && isMem) {
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
    } otherwise {
      if (!cfg.optScoreboard) {
        someCommitStm.valid := False
      }
      someCommitStm.regFileWrite.addr := 0x0
      someCommitStm.regFileWrite.data := 0x0
    }
    if (cfg.optScoreboard) {
      //val rSeenUpIsFiring = (
      //  isMem
      //) generate (
      //  Reg(Bool(), init=False)
      //)
      someCommitStm.valid := (
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
              && stickyMyD2hBusFire
            )
          ) else (
            //cLink.up.isFiring
            //&& 
            //cLink.up.isValid
            //&& 
            myNonMemWbValid
            //&& rInstrMayPassCnt.orR
            //&& !myNonMemWbPayload(1).instrCnt.myPsIdBubble.last
          )
        )
      )
      if (isMem) {
        //rInstrMayPassCnt := myInstrMayPassCntInitVal
      } else {
        //switch (
        //  myMemWbValid
        //  ## someCommitStm.fire
        //) {
        //  is (M"11") {
        //    rInstrMayPassCnt := rInstrMayPassCnt - 1
        //  }
        //  is (M"0-") {
        //    rInstrMayPassCnt := myInstrMayPassCntInitVal
        //  }
        //}
        //when (
        //  myMemWbValid
        //  && someCommitStm.fire
        //) {
        //}
      }
      someCommitStm.commit.tag := (
        someMyWbPayload(1).instrCnt.scoreboardTag
      )
      if (isMem) {
        myMemWbFifo.io.pop.ready := someCommitStm.fire
      } else {
        myNonMemWbFifo.io.pop.ready := someCommitStm.fire
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
      someMyWbPayload=myMemWbPayload,
      someCommitStm=myMemCommitFrontStm,
      //someCommitStm=myCommitInpStmVec.head.head,
      //someRegFileWrPulseStm=myRegFileWrPulseInpStmVec.head.head,
      isMem=true
    )
    setCommitEtc(
      someMyWbPayload=myNonMemWbPayload,
      someCommitStm=myNonMemCommitFrontStm,
      //someCommitStm=myCommitInpStmVec.head.last,
      //someRegFileWrPulseStm=myRegFileWrPulseInpStmVec.head.last,
      isMem=false
    )
  })

  if (!cfg.optScoreboard) {
    setCommitEtc(
      someMyWbPayload=myWbPayloadVec.head,
      someCommitStm=myCommitFinalOutpStm,
      //someRegFileWrPulseStm=myRegFileWrPulseOutpStm,
      isMem=false
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
    when (
      //myCommitOutpStm.fire
      myCommitFinalOutpStm.fire
    ) {
      io.dbgInfo.regFileWriteData := (
        myCommitFinalOutpStm.regFileWrite.data
      )
      io.dbgInfo.regFileWriteAddr := (
        myCommitFinalOutpStm.regFileWrite.addr
      )
      io.dbgInfo.regFileWriteEnable := (
        if (cfg.optScoreboard) (
          (
            myCommitFinalOutpStm.regFileWrite.addr =/= 0x0
          )
          && (
            myCommitFinalOutpStm.fire
          )
        ) else (
          myCommitFinalOutpStm.fire
        )
      )
      io.dbgInfo.laggingRegPcAtRegFileWrite := (
        myCommitFinalOutpStm.myWbPayload.laggingRegPc.resize(
          cfg.mainWidth bits
        )
      )
      io.dbgInfo.shouldIgnoreInstrAtRegFileWrite := (
        if (cfg.optScoreboard) (
          myCommitFinalOutpStm.myWbPayload.instrCnt.shouldIgnoreInstr.last
          || (
            !myCommitFinalOutpStm.fire
          )
        ) else (
          myCommitFinalOutpStm.myWbPayload.instrCnt.shouldIgnoreInstr.last
        )
      )
      io.dbgInfo.myPsIdBubbleAtRegFileWrite := (
        if (cfg.optScoreboard) {
          val myInstrCnt = myCommitFinalOutpStm.myWbPayload.instrCnt
          (
            myInstrCnt.myPsIdBubble.last
            || myInstrCnt.myPsExMemAccessBubble.last
            || myInstrCnt.myPsExMultiCycleBubble.last
            || (
              !myCommitFinalOutpStm.fire
            )
          )
        } else {
          myCommitFinalOutpStm.myWbPayload.instrCnt.myPsIdBubble.last
        }
      )
      when (myCommitFinalOutpStm.myWbPayload.encInstr.payload.orR) {
        io.dbgInfo.encInstrAtRegFileWrite := (
          myCommitFinalOutpStm.myWbPayload.encInstr.payload
        )
      }
      io.dbgInfo.immAtRegFileWrite := (
        myCommitFinalOutpStm.myWbPayload.imm.last
      )
      io.dbgInfo.rdMemWordAtRegFileWrite := (
        myCommitFinalOutpStm.myWbPayload.myExt(0).rdMemWord
      )
      io.dbgInfo.gprIdxVecAtRegFileWrite := (
        myCommitFinalOutpStm.myWbPayload.gprIdxVec
      )
    }
  }

  Builder(linkArr)
  //--------
}
