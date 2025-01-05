package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
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
  val op = UInt(SampleCpuInstrEnc.opWidth bits)
  val raIdx = UInt(SampleCpuInstrEnc.gprIdxWidth bits)
  val rbIdx = UInt(SampleCpuInstrEnc.gprIdxWidth bits)
  val rcIdx = UInt(SampleCpuInstrEnc.gprIdxWidth bits)
  val simm16 = UInt(SampleCpuInstrEnc.simmWidth bits)
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
case class SampleCpuParams(
  optFormal: Boolean
) {
  //--------
  val instrMainWidth = 32
  val mainWidth = 32
  val numGprs = SampleCpuInstrEnc.numGprs
  val modRdPortCnt = 3
  val pipeName="SnowHouseSampleCpu"
  //--------
  val cfg = SnowHouseConfig(
    //encInstrType=SampleCpuEncInstr(),
    instrMainWidth=instrMainWidth,
    shRegFileCfg=SnowHouseRegFileConfig(
      mainWidth=mainWidth,
      wordCountArr=(
        //Array.fill(2)(
        //  numGprs >> 1
        //)
        Array[Int](
          4,
          12,
        )
      ),
      modRdPortCnt=modRdPortCnt,
      pipeName=pipeName,
      optHowToSlice=(
        //None
        Some({
          val tempArr = ArrayBuffer[LinkedHashSet[Int]]()
          for (jdx <- 0 until 2) {
            tempArr += {
              val tempSet = LinkedHashSet[Int]()
              if (jdx == 0) {
                //for (idx <- 0 until 4) {
                //  tempSet += idx
                //}
                tempSet ++= LinkedHashSet[Int](
                  0,
                  2,
                  4,
                  6,
                )
              } else { // if (jdx == 1)
                tempSet ++= LinkedHashSet[Int](
                  1,
                  3,
                  5,
                  7,
                  8,
                  9,
                  10,
                  11,
                  12,
                  13,
                  14,
                  15,
                )
              }
              //for (idx <- 0 until (numGprs >> 1)) {
              //  tempSet += {
              //    val temp = (
              //      (2 * idx) + jdx
              //    )
              //    println(
              //      s"debug: "
              //      + s"temp:${temp} idx:${idx} jdx:${jdx}"
              //    )
              //    temp
              //  }
              //}
              tempSet
            }
          }
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
}

object SnowHouseSampleCpuTestProgram extends App {
  import SnowHouseRegs._
  val program = ArrayBuffer[AsmStmt]()
  import libsnowhouse.Label._
  val tempData: Int = 0x17000
  program ++= Array[AsmStmt](
    //--------
    Lb"loop",
    add(r0, r1, r2),
    cpyui(r2, tempData >> 16),
    cpyi(r2, tempData & 0xffff),
    bz(r0, LbR"loop"),
    //--------
    cpyi(r12, LbR"infin"),
    Lb"infin",
    //--------
    bz(r12, LbR"infin"),
    Db32(0x3f),
    //--------
  )
  val outpArr = ArrayBuffer[BigInt]()
  val assembler = SampleCpuAssembler(
    stmtArr=program,
    outpArr=outpArr,
  )
  for ((encoded, idx) <- outpArr.view.zipWithIndex) {
    printf(
      //s"encoded ${idx}: ${encoded}"
      "%X: %X\n",
      idx << 2,
      (encoded.toLong & 0xffffffff).toInt
    )
  }
}
object SnowHouseSampleCpuToVerilog extends App {
  Config.spinal.generateVerilog(SnowHouse(
    cfg=SampleCpuParams(
      optFormal=(
        true
        //false
      )
    ).cfg
  ))
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
  case class SnowHouseSampleCpuFormalDutWithBranches() extends Component {
    val dut = FormalDut(
      SnowHouse(
        cfg=SampleCpuParams(
          optFormal=(
            true
            //false
          )
        ).cfg
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
    .doVerify(SnowHouseSampleCpuFormalDutWithBranches())
  //--------
}
