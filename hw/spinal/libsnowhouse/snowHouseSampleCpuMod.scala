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
  //private var _opCnt: Int = 0
  def mkOp(
    opAsInt: Int,
    name: String,
  ): (/*UInt,*/ Int, String) = {
    //_opCnt += 1
    (
      //U(s"${SampleCpuInstrEnc.opWidth}'d${opAsInt}"),
      opAsInt,
      name,
    )
  }
  //--------
  def AddRaRbRc = mkOp(0, "AddRaRbRc") // 0
  def SubRaRbRc = mkOp(1, "SubRaRbRc") // 1
  def SltuRaRbRc = mkOp(2, "SltuRaRbRc") // 2
  //def SltsRaRbRc = mkOp() // 3
  //--------
  def AndRaRbRc = mkOp(3, "AndRaRbRc") // 3
  def OrrRaRbRc = mkOp(4, "OrrRaRbRc") // 4
  def XorRaRbRc = mkOp(5, "XorRaRbRc") // 5
  def LslRaRbRc = mkOp(6, "LslRaRbRc") // 6
  def LsrRaRbRc = mkOp(7, "LsrRaRbRc") // 7
  def AsrRaRbRc = mkOp(8, "AsrRaRbRc") // 8
  //--------
  def BzRaSimm = mkOp(9, "BzRaSimm") // 9
  def BnzRaSimm = mkOp(10, "BnzRaSimm") // 10
  def JmpRa = mkOp(11, "JmpRa") // 11
  def LdrRaRbSimm = mkOp(12, "LdrRaRbSimm") // 12
  def StrRaRbSimm = mkOp(13, "StrRaRbSimm") // 13
  def CpyuiRaSimm = mkOp(14, "CpyuiRaSimm") // 14
  def CpyiRaSimm = mkOp(15, "CpyiRaSimm") // 15
  //--------
}
case class SampleCpuEncInstr(
) extends PackedBundle {
  val op = UInt(SampleCpuInstrEnc.opWidth bits)
  val raIdx = UInt(SampleCpuInstrEnc.gprIdxWidth bits)
  val rbIdx = UInt(SampleCpuInstrEnc.gprIdxWidth bits)
  val rcIdx = UInt(SampleCpuInstrEnc.gprIdxWidth bits)
  val simm16 = UInt(SampleCpuInstrEnc.simmWidth bits)
}
case class SampleCpuPsDecode(
) extends SnowHousePsDecode[SampleCpuEncInstr] {
  private val _decInstr: UInt = U"32'd0"
  def decInstr: UInt = _decInstr
  args match {
    case Some(args) => {
      //--------
      def opInfoMap = args.opInfoMap
      def io = args.io
      def cId = args.link
      def payload = args.payload
      def optFormal = args.optFormal
      //--------
      //assert(opInfoMap != null)
      //assert(io != null)
      //assert(cId != null)
      //--------
      when (cId.up.isFiring) {
      }
      //--------
    }
    case None => {
      assert(false)
    }
  }
}
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
  opInfoMap += (
    SampleCpuOp.AsrRaRbRc -> OpInfo.mkAlu(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
      aluOp=AluOpKind.Asr,
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
    SampleCpuOp.LdrRaRbSimm -> OpInfo.mkLoad(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(Some(true))),
      //loadOp=LoadOpKind.LdU32,
      modify=ModifySrcDstKind.Mem32(isSigned=None, isStore=Some(false)),
    )
  )
  opInfoMap += (
    SampleCpuOp.StrRaRbSimm -> OpInfo.mkStore(
      dstArr=Array[DstKind](DstKind.Gpr),
      srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Imm(Some(true))),
      //storeOp=StoreOpKind.St32,
      modify=ModifySrcDstKind.Mem32(isSigned=None, isStore=Some(true)),
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
    encInstrType=SampleCpuEncInstr(),
    instrMainWidth=instrMainWidth,
    shRegFileCfg=SnowHouseRegFileConfig(
      mainWidth=mainWidth,
      wordCountArr=(
        Array.fill(1)(numGprs.toInt)
      ),
      modRdPortCnt=modRdPortCnt,
      pipeName=pipeName,
    ),
    opInfoMap=SampleCpuOpInfoMap.opInfoMap,
    psDecode=SampleCpuPsDecode(),
    //decodeFunc=(
    //  io: SnowHouseIo[SampleCpuEncInstr],
    //  cId: CtrlLink,
    //  decInstr: UInt,
    //) => new Area {
    //  //decInstr := U"${mainWidth}'d0"
    //},
    optFormal=optFormal,
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
      optFormal=true
    ).cfg
  ))
}
