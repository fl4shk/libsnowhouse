package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.sim._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._

import libcheesevoyage.general._
import libcheesevoyage.math._
import libcheesevoyage.bus.lcvStall._

object SampleCpuInstrEnc {
  val numOps: Int = 16
  val opWidth: Int = log2Up(numOps)
  val numGprs: Int = 16
  val gprIdxWidth: Int = log2Up(numGprs)
  val simmWidth: Int = 16
}
object SampleCpuOp {
  private var _opCnt: Int = 0
  def mkOp(
    //opAsInt: Int,
    name: String,
  ): (/*UInt,*/ Int, String) = {
    val ret = (
      //U(s"${SampleCpuInstrEnc.opWidth}'d${opAsInt}"),
      //opAsInt,
      _opCnt,
      name,
    )
    _opCnt += 1
    ret
  }
  //--------
  val AddRaRbRc = mkOp("AddRaRbRc")     // 0
  val SubRaRbRc = mkOp("SubRaRbRc")     // 1
  val SltuRaRbRc = mkOp("SltuRaRbRc")   // 2
  //val SltsRaRbRc = mkOp()             // 3
  //--------
  val AndRaRbRc = mkOp("AndRaRbRc")     // 3
  val OrrRaRbRc = mkOp("OrrRaRbRc")     // 4
  val XorRaRbRc = mkOp("XorRaRbRc")     // 5
  val LslRaRbRc = mkOp("LslRaRbRc")     // 6
  val LsrRaRbRc = mkOp("LsrRaRbRc")     // 7
  //val AsrRaRbRc = mkOp("AsrRaRbRc")   // 8
  val MulRaRbRc = mkOp("MulRaRbRc")     // 8
  //--------
  val BzRaSimm = mkOp("BzRaSimm")       // 9
  val BnzRaSimm = mkOp("BnzRaSimm")     // 10
  val JmpRa = mkOp("JmpRa")             // 11
  val LdrRaRbSimm = mkOp("LdrRaRbSimm") // 12
  val StrRaRbSimm = mkOp("StrRaRbSimm") // 13
  val CpyuiRaSimm = mkOp("CpyuiRaSimm") // 14
  val CpyiRaSimm = mkOp("CpyiRaSimm")   // 15
  //--------
  val OpLim = _opCnt
  assert(
    OpLim == 16,
    s"eek! "
    + s"${OpLim} != 16"
  )
}
case class SampleCpuEncInstr(
) extends PackedBundle {
  val simm16 = UInt(SampleCpuInstrEnc.simmWidth bits)
  val rcIdx = UInt(SampleCpuInstrEnc.gprIdxWidth bits)
  val rbIdx = UInt(SampleCpuInstrEnc.gprIdxWidth bits)
  val raIdx = UInt(SampleCpuInstrEnc.gprIdxWidth bits)
  val op = UInt(SampleCpuInstrEnc.opWidth bits)
}
object SampleCpuPipeStageInstrDecode {
  def apply(
    psId: SnowHousePipeStageInstrDecode
  ) = new Area {
    def upPayload = psId.upPayload
    def io = psId.io
    val encInstr = SampleCpuEncInstr()
    encInstr.assignFromBits(io.ibus.devData.instr.asBits)
    upPayload.op := encInstr.op
    upPayload.gprIdxVec(0) := encInstr.raIdx
    upPayload.gprIdxVec(1) := encInstr.rbIdx
    upPayload.gprIdxVec(2) := encInstr.rcIdx
    upPayload.imm := Cat(
      Mux[UInt](
        encInstr.simm16.msb,
        U"16'hffff",
        U"16'h0000",
      ),
      encInstr.simm16.asSInt
    ).asUInt
  }
}
//case class SampleCpuPipeStageInstrDecode(
//  //args: 
//  override val args: SnowHousePipeStageArgs,
//  override val psIdHaltIt: Bool,
//  override val psExSetPc: Flow[SnowHousePsExSetPcPayload],
//) extends SnowHousePipeStageInstrDecode(
//  args=args,
//  psIdHaltIt=psIdHaltIt,
//  psExSetPc=psExSetPc,
//) {
//  def doDecode() = new Area {
//  }
//  //def cfg = super.cfg
//  //def opInfoMap = super.opInfoMap
//  //def io = super.io
//  //def cId = super.cId
//  //def payload = super.payload
//  //def optFormal = super.optFormal
//  //private val _decInstr: UInt = U"32'd0"
//  //def decInstr: UInt = _decInstr
//  //args match {
//  //  case Some(args) => {
//  //    //--------
//  //    def cfg = super.cfg
//  //    def opInfoMap = super.opInfoMap
//  //    def io = super.io
//  //    def cId = super.cId
//  //    def payload = super.payload
//  //    def optFormal = super.optFormal
//  //    //--------
//  //    //assert(opInfoMap != null)
//  //    //assert(io != null)
//  //    //assert(cId != null)
//  //    //--------
//  //    when (cId.up.isFiring) {
//  //    }
//  //    //--------
//  //  }
//  //  case None => {
//  //    assert(false)
//  //  }
//  //}
//}
object SampleCpuOpInfoMap {
  //--------
  val opInfoMap = LinkedHashMap[Any, OpInfo]()
  //--------
  opInfoMap += (
    SampleCpuOp.AddRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Add,
    )
  )
  opInfoMap += (
    SampleCpuOp.SubRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Sub,
    )
  )
  opInfoMap += (
    SampleCpuOp.SltuRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Sltu,
    )
  )
  //opInfoMap += (
  //  SampleCpuOp.SltsRaRbRc -> OpInfo.mkAlu(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
  //    aluOp=AluOpKind.Slts,
  //  )
  //)
  //--------
  opInfoMap += (
    SampleCpuOp.AndRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.And,
    )
  )
  opInfoMap += (
    SampleCpuOp.OrrRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Or,
    )
  )
  opInfoMap += (
    SampleCpuOp.XorRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Xor,
    )
  )
  //--------
  opInfoMap += (
    SampleCpuOp.LslRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Lsl,
    )
  )
  opInfoMap += (
    SampleCpuOp.LsrRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Lsr,
    )
  )
  //opInfoMap += (
  //  SampleCpuOp.AsrRaRbRc -> OpInfo.mkAlu(
  //    dstArr=Array[DstKind](DstKind.Gpr),
  //    srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
  //    aluOp=AluOpKind.Asr,
  //  )
  //)
  opInfoMap += (
    SampleCpuOp.MulRaRbRc -> OpInfo.mkMultiCycle(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      multiCycleOp=MultiCycleOpKind.Umul,
    )
  )
  //--------
  opInfoMap += (
    SampleCpuOp.BzRaSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(Some(true))),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Z,
    )
  )
  opInfoMap += (
    SampleCpuOp.BnzRaSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(Some(true))),
      cpyOp=CpyOpKind.Br,
      cond=CondKind.Nz,
    )
  )
  opInfoMap += (
    SampleCpuOp.JmpRa -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Pc),
      srcArr=Array[SrcKind](SrcKind.Gpr),
      cpyOp=CpyOpKind.Jmp,
    )
  )
  //--------
  opInfoMap += (
    SampleCpuOp.LdrRaRbSimm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(Some(true))),
      //loadOp=LoadOpKind.LdU32,
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(false)),
    )
  )
  opInfoMap += (
    SampleCpuOp.StrRaRbSimm -> OpInfo.mkLdSt(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(Some(true))),
      //storeOp=StoreOpKind.St32,
      modify=MemAccessKind.Mem32(isSigned=false, isStore=Some(true)),
    )
  )
  opInfoMap += (
    SampleCpuOp.CpyuiRaSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Imm(Some(true))),
      cpyOp=CpyOpKind.Cpyui,
    )
  )
  opInfoMap += (
    SampleCpuOp.CpyiRaSimm -> OpInfo.mkCpy(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Imm(Some(true))),
      cpyOp=CpyOpKind.Cpy,
    )
  )
  //--------
}
case class SampleCpuConfig(
  optFormal: Boolean,
) {
  //--------
  val instrMainWidth = 32
  val mainWidth = (
    32
    //16
  )
  val numGprs = SampleCpuInstrEnc.numGprs
  val modRdPortCnt = 3
  val pipeName="SnowHouseSampleCpu"
  //--------
  val shCfg = SnowHouseConfig(
    //encInstrType=SampleCpuEncInstr(),
    instrMainWidth=instrMainWidth,
    shRegFileCfg=SnowHouseRegFileConfig(
      mainWidth=mainWidth,
      wordCountArr=(
        Array.fill(1)(numGprs)
        //Array.fill(2)(
        //  numGprs >> 1
        //)
        //Array[Int](
        //  4,
        //  12,
        //)
      ),
      modRdPortCnt=modRdPortCnt,
      pipeName=pipeName,
      optHowToSlice=(
        //None
        Some({
          val tempArr = ArrayBuffer[LinkedHashSet[Int]]()
          tempArr += LinkedHashSet[Int]()
          for (idx <- 0 until numGprs) {
            tempArr.last += idx
          }
          //for (jdx <- 0 until 2) {
          //  tempArr += {
          //    val tempSet = LinkedHashSet[Int]()
          //    if (jdx == 0) {
          //      //for (idx <- 0 until 4) {
          //      //  tempSet += idx
          //      //}
          //      tempSet ++= LinkedHashSet[Int](
          //        0,
          //        2,
          //        4,
          //        6,
          //      )
          //    } else { // if (jdx == 1)
          //      tempSet ++= LinkedHashSet[Int](
          //        1,
          //        3,
          //        5,
          //        7,
          //        8,
          //        9,
          //        10,
          //        11,
          //        12,
          //        13,
          //        14,
          //        15,
          //      )
          //    }
          //    //for (idx <- 0 until (numGprs >> 1)) {
          //    //  tempSet += {
          //    //    val temp = (
          //    //      (2 * idx) + jdx
          //    //    )
          //    //    println(
          //    //      s"debug: "
          //    //      + s"temp:${temp} idx:${idx} jdx:${jdx}"
          //    //    )
          //    //    temp
          //    //  }
          //    //}
          //    tempSet
          //  }
          //}
          tempArr
        })
      ),
    ),
    opInfoMap=SampleCpuOpInfoMap.opInfoMap,
    //psDecode=SampleCpuPipeStageInstrDecode(),
    //mkPipeStageInstrDecode=(
    //  (
    //    args,
    //    psIdHaltIt,
    //    psExSetPc,
    //  ) => (
    //    SampleCpuPipeStageInstrDecode(
    //      args=args,
    //      psIdHaltIt=psIdHaltIt,
    //      psExSetPc=psExSetPc,
    //    )
    //  )
    //),
    doInstrDecodeFunc=SampleCpuPipeStageInstrDecode.apply,
    //decodeFunc=(
    //  io: SnowHouseIo[SampleCpuEncInstr],
    //  cId: CtrlLink,
    //  decInstr: UInt,
    //) => new Area {
    //  //decInstr := U"${mainWidth}'d0"
    //},
    optFormal=optFormal,
    //maxNumGprsPerInstr=3,
  )
  //--------
  val program = ArrayBuffer[AsmStmt]()
}

case class SnowHouseSampleCpuProgram(
  cfg: SampleCpuConfig
) {
  val outpArr = ArrayBuffer[BigInt]()
  val assembler = SampleCpuAssembler(
    stmtArr=cfg.program,
    outpArr=outpArr,
  )
  def doPrint(): Unit = {
    for ((encoded, idx) <- outpArr.view.zipWithIndex) {
      printf(
        //s"encoded ${idx}: ${encoded}"
        "%X: %X\n",
        idx << 2,
        (encoded.toLong & 0xffffffff).toInt
      )
    }
  }
}
case class SnowHouseSampleCpuTestProgram(
  cfg: SampleCpuConfig,
) {
  import SampleCpuRegs._
  //val program = ArrayBuffer[AsmStmt]()
  //val cfg = SampleCpuConfig(
  //  optFormal=false,
  //  program=program,
  //)
  //def program = cfg.program
  import libsnowhouse.Label._
  val tempData: Int = 0x17000
  cfg.program ++= Array[AsmStmt](
    //--------
    cpyi(r0, 0x0),        // r0 = 0
    cpyi(r1, 0x8),        // r1 = 8
    cpyi(r2, 0x1),        // r2 = 1
    cpyi(r3, 0x1000),     // r3 = 0x1000
    cpyi(r4, 0x8),        // r4 = 4
    //cpyi(r5, 0x0),
    //--------
    Lb"loop",
    //add(r0, r1, r2),
    //cpyui(r2, tempData >> 16),
    //cpyi(r2, tempData & 0xffff),
    ldr(r5, r3, 0x0),     // r5 = [r3 + 0]
    add(r5, r5, r2),      // r5 += 1
    str(r5, r3, 0x4),     // [r3 + 4] = r5
    add(r3, r3, r4),      // r3 += 4
    sub(r1, r1, r2),      // r1 -= 1 
    bnz(r1, LbR"loop"),   // if (r1 != 0) goto LbR"loop"
    //--------
    cpyi(r12, 0x0),
    Lb"infin",
    //--------
    bz(r12, LbR"infin"),
    //Db32(0x3f),
    //--------
  )
  val program = SnowHouseSampleCpuProgram(cfg=cfg)
  //val outpArr = ArrayBuffer[BigInt]()
  //val assembler = SampleCpuAssembler(
  //  stmtArr=program,
  //  outpArr=outpArr,
  //)
}
case class SampleCpuWithDualRamIo(
  program: SnowHouseSampleCpuProgram,
) extends Bundle {
  def cfg = program.cfg
}
case class SampleCpuWithDualRam(
  program: SnowHouseSampleCpuProgram,
) extends Component {
  val io = SampleCpuWithDualRamIo(program=program)
  def cfg = program.cfg
  val cpu = SnowHouse(cfg=cfg.shCfg)
  val dualRam = SnowHouseSampleInstrDataDualRam(
    cfg=cfg.shCfg,
    instrInitBigInt=program.outpArr,
    dataInitBigInt=(
      Array.fill(1 << 16)(BigInt(0))
    ),
  )
  cpu.io.ibus <> dualRam.io.ibus
  cpu.io.dbus <> dualRam.io.dbus
}
object SnowHouseSampleCpuWithDualRamSim extends App {
  //Config.spinal.generateVerilog({
  //  val cfg = SampleCpuConfig(
  //    optFormal=(
  //      //true
  //      false
  //    )
  //  )
  //  SnowHouse(
  //    cfg=cfg.shCfg
  //  )
  //})
  val cfg = SampleCpuConfig(
    optFormal=(
      //true
      false
    )
  )
  val testProgram = SnowHouseSampleCpuTestProgram(cfg=cfg)
  SimConfig.withVcdWave.compile(
    SampleCpuWithDualRam(program=testProgram.program)
  ).doSim{dut => {
    dut.clockDomain.forkStimulus(10)
    for (i <- 0 until 256) {
      dut.clockDomain.waitSampling()
      //for (gprIdx <- 0 until cfg.numGprs) {
      //  printf(
      //    "r%i=%x ",
      //    gprIdx,
      //    dut.cpu.regFile.modMem(0)(0).readAsync(
      //      address=gprIdx
      //    ).toInt
      //  )
      //  if (gprIdx % 4 == 3) {
      //    printf("\n")
      //  }
      //}
    }
  }}
}
object SnowHouseSampleCpuToVerilog extends App {
  Config.spinal.generateVerilog({
    val cfg = SampleCpuConfig(
      optFormal=(
        //true
        false
      )
    )
    SnowHouse(
      cfg=cfg.shCfg
    )
  })
}
object SnowHouseSampleCpuFormal extends App {
  //--------
  //--------
  def myProveNumCycles = (
    //8
    10
    //15
    //16
  )
  case class SnowHouseSampleCpuFormalDut1() extends Component {
    val dut = FormalDut(
      SnowHouse(
        cfg=SampleCpuConfig(
          optFormal=(
            true
            //false
          )
        ).shCfg
      )
    )

    assumeInitial(clockDomain.isResetActive)
    anyseq(dut.io.ibus.devData)
    anyseq(dut.io.ibus.ready)
    if (dut.io.haveMultiCycleBusVec) {
      for (
        (multiCycleBus, busIdx)
        <- dut.io.multiCycleBusVec.view.zipWithIndex
      ) {
        anyseq(multiCycleBus.devData)
        anyseq(multiCycleBus.ready)
      }
    }
    anyseq(dut.io.dbus.devData)
    anyseq(dut.io.dbus.ready)
  }
  //--------
  new SpinalFormalConfig(
    _spinalConfig=SpinalConfig(
      defaultConfigForClockDomains=ClockDomainConfig(
        resetActiveLevel=HIGH,
        resetKind=SYNC,
      ),
      formalAsserts=true,
    ),
    _keepDebugInfo=true,
  )
    .withBMC(
      //20
      //15
      //16
      myProveNumCycles
    )
    //.withProve(
    //  //20
    //  //40
    //  //10
    //  myProveNumCycles
    //)
    //.withCover(
    //  //myProveNumCycles
    //  //20
    //  //60
    //  //20
    //  15
    //)
    .doVerify(SnowHouseSampleCpuFormalDut1())
  //--------
}
