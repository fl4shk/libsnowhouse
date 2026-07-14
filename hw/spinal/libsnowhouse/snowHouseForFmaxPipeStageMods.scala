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
      temp.setName("s_down")
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
}

case class SnowHouseForFmaxPipeStagePreEx(
  cfg: SnowHouseConfig,
) extends Component {
  //--------
  val io = SnowHouseForFmaxPipeStagePreExIo(cfg=cfg)
  //def up = io.up
  //def down = io.down
  //--------
  val linkArr = PipeHelper.mkLinkArr()

  //def opInfoMap = cfg.opInfoMap

  val pPreExInp = Payload(SnowHousePipePayload(cfg=cfg))
  val pPreExOutp = Payload(SnowHousePipePayload(cfg=cfg))
  val cLink = CtrlLink()
  val sLink = StageLink(
    up=cLink.down,
    down={
      val temp = Node()
      temp.setName("s_down")
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

  val myOutp = SnowHousePipePayload(cfg=cfg)
  val myInp = SnowHousePipePayload(cfg=cfg)
  //myInp := RegNext(myInp)
  myOutp := RegNext(myOutp)
  myOutp.allowOverride

  val innerPsPreEx = SnowHousePrePipeStageExSetBranchPredictEtcArea(
    cfg=cfg,
    outp=myOutp,
    inp=myInp,
    link=cLink,
  )

  cLink.up.driveFrom(io.up)(
    con=(node, inp) => {
      node(pPreExInp) := inp
      myInp := inp
      when (node.isValid) {
        myOutp := myInp
      }
    }
  )
  //when (cLink.up.valid) {
  //}
  cLink.up(pPreExOutp) := myOutp

  s2mLink.down.driveTo(
    io.down
  )(
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
  val psExSetPc = (
    master(Flow(
      SnowHousePsExSetPcPayload(cfg=cfg)
    ))
  )
  //--------
  val psWbToEarlierStallRequest = (
    in(Bool())
  )
  val myLcvDbusH2dStm = (
    master(Stream(
      LcvBusH2dPayload(cfg=cfg.subCfg.lcvIbusEtcCfg.loBusCfg)
    ))
  )
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
      temp.setName("s_down")
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
    //pcChangeState=null,
    //shouldIgnoreInstr=null,
    //psExFoundBubble=psExFoundBubble,
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
