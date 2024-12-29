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

case class SnowHouseRegFileConfig(
  mainWidth: Int,
  wordCountArr: Seq[Int],
  modRdPortCnt: Int,
  pipeName: String,
  //linkArr: Option[ArrayBuffer[Link]]=None,
) {
  val modStageCnt: Int = 1
}

//case class SnowHouseRegFileSliceInfo(
//  shRegFileCfg: SnowHouseRegFileConfig,
//) {
//}
case class SnowHousePsDecodeArgs[
  EncInstrT <: Data
](
) {
  var opInfoMap: LinkedHashMap[Any, OpInfo] = null
  var io: SnowHouseIo[EncInstrT] = null
  var cId: CtrlLink = null
  var optFormal: Boolean = false
}
abstract class SnowHousePsDecode[
  EncInstrT <: Data
](
) extends Area {
  var args: Option[SnowHousePsDecodeArgs[EncInstrT]]=None
  def decInstr: UInt
}
case class SnowHouseConfig[
  EncInstrT <: Data,
](
  encInstrType: HardType[EncInstrT],
  //gprFileDepth: Int,
  //sprFileDepth: Int,
  instrMainWidth: Int,
  shRegFileCfg: SnowHouseRegFileConfig,
  opInfoMap: LinkedHashMap[Any, OpInfo],
  //decodeFunc: (
  //  SnowHouseIo[EncInstrT], // io
  //  CtrlLink,               // cId
  //  UInt,                   // output the decoded instruction
  //) => Area,                
  psDecode: SnowHousePsDecode[EncInstrT],
  optFormal: Boolean,
) {
  //def optFormal: Boolean = psDecode.optFormal
  def mainWidth = shRegFileCfg.mainWidth
  def regFileWordCountArr = shRegFileCfg.wordCountArr
  def regFileModRdPortCnt = shRegFileCfg.modRdPortCnt
  def regFileModStageCnt = shRegFileCfg.modStageCnt
  def regFilePipeName = shRegFileCfg.pipeName
  val regFileCfg = PipeMemRmwConfig[UInt, Bool](
    wordType=UInt(mainWidth bits),
    wordCountArr=regFileWordCountArr,
    hazardCmpType=Bool(),
    modRdPortCnt=regFileModRdPortCnt,
    modStageCnt=regFileModStageCnt,
    pipeName=regFilePipeName,
    //linkArr=linkArr
  )
  regFileCfg.linkArr = None
}

