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
  val numOps = 16
  val opWidth = log2Up(numOps)
  val numGprs = 16
  val gprIdxWidth = log2Up(numGprs)
  val simmWidth = 16
}
object SampleCpuOp {
  def mkOp(
    opAsInt: Int
  ): (UInt, Int) = {
    (U(s"${SampleCpuInstrEnc.opWidth}'d${opAsInt}"), opAsInt)
  }
  //--------
  def AddRaRbRc = mkOp(0)
  def SubRaRbRc = mkOp(1)
  def SltuRaRbRc = mkOp(2)
  def SltsRaRbRc = mkOp(3)
  //--------
  def AndRaRbRc = mkOp(4)
  def OrrRaRbRc = mkOp(5)
  def XorRaRbRc = mkOp(6)
  def LslRaRbRc = mkOp(7)
  //--------
  def LsrRaRbRc = mkOp(8)
  def AsrRaRbRc = mkOp(9)
  //--------
  def BzRaSimm = mkOp(10)
  def BnzRaSimm = mkOp(11)
  def JmpRa = mkOp(12)
  def LdrRaRbSimm = mkOp(13)
  def StrRaRbSimm = mkOp(14)
  def CpyiRaSimm = mkOp(15)
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
object SampleCpuParams {
  //--------
  val instrMainWidth = 32
  val mainWidth = 32
  val numGprs = SampleCpuInstrEnc.numGprs
  val modRdPortCnt = 3
  val pipeName="SnowHouseSampleCpu"
  //--------
  //--------
  val cfg = SnowHouseConfig(
    instrMainWidth=instrMainWidth,
    shRegFileCfg=SnowHouseRegFileConfig(
      mainWidth=mainWidth,
      wordCountArr=(
        Array.fill(1)(numGprs)
      ),
      modRdPortCnt=modRdPortCnt,
      pipeName=pipeName,
    ),
    opInfoMap={
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
      opInfoMap += (
        SampleCpuOp.SltsRaRbRc -> OpInfo.mkAlu(
          dstArr=Array[DstKind](DstKind.Gpr),
          srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.Gpr),
          aluOp=AluOpKind.Slts,
        )
      )
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
          srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.SImm),
          cpyOp=CpyOpKind.Br,
          cond=CondKind.Z,
        )
      )
      opInfoMap += (
        SampleCpuOp.BnzRaSimm -> OpInfo.mkCpy(
          dstArr=Array[DstKind](DstKind.Pc),
          srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.SImm),
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
          srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.SImm),
          loadOp=LoadOpKind.LdU32,
        )
      )
      opInfoMap += (
        SampleCpuOp.StrRaRbSimm -> OpInfo.mkStore(
          dstArr=Array[DstKind](DstKind.Gpr),
          srcArr=Array[SrcKind](SrcKind.Gpr, SrcKind.SImm),
          storeOp=StoreOpKind.St32,
        )
      )
      opInfoMap += (
        SampleCpuOp.CpyiRaSimm -> OpInfo.mkCpy(
          dstArr=Array[DstKind](DstKind.Gpr),
          srcArr=Array[SrcKind](SrcKind.SImm),
          cpyOp=CpyOpKind.Cpy,
        )
      )
      //--------
      opInfoMap
    },
  )
  //--------
}
