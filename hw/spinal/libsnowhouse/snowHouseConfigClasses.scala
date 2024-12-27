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

case class RegFileConfig(
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
case class SnowHouseConfig(
  //gprFileDepth: Int,
  //sprFileDepth: Int,
  instrMainWidth: Int,
  shRegFileCfg: RegFileConfig,
  ldKindSet: LinkedHashSet[LoadOpKind],
  stKindSet: LinkedHashSet[StoreOpKind],
) {
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

