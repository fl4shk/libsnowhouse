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

//--------
//case class BusHostPayload(
//  cfg: SnowHouseConfig,
//) extends Bundle {
//  val addr = UInt(cfg.mainWidth bits)
//  //val data = UInt(cfg.mainWidth bits)
//}
//case class BusDevPayload(
//  cfg: SnowHouseConfig,
//) extends Bundle {
//  val instr = UInt(cfg.instrMainWidth bits)
//}
//--------
//object MultiCycleOpEnum
//extends SpinalEnum(defaultEncoding=native) {
//  val
//    Umul,
//    Smul,
//    Udiv,
//    Sdiv,
//    Umod,
//    Smod,
//    Udivmod,
//    Sdivmod
//    = newElement()
//}
case class MultiCycleHostPayload(
  cfg: SnowHouseConfig,
  opInfo: OpInfo,
) extends Bundle {
  assert(
    cfg.opInfoMap.find(_._2 == opInfo) != None
  )
  assert(
    opInfo.select == OpSelect.MultiCycle
  )
  val srcVec = Vec.fill(opInfo.srcArr.size)(
    UInt(cfg.mainWidth bits)
  )
  //opInfo.multiCycleOp.get match {
  //  case MultiCycleOpKind.Umul => {
  //  }
  //  case MultiCycleOpKind.Smul => {
  //  }
  //  case MultiCycleOpKind.Udiv => {
  //  }
  //  case MultiCycleOpKind.Sdiv => {
  //  }
  //  case MultiCycleOpKind.Umod => {
  //  }
  //  case MultiCycleOpKind.Smod => {
  //  }
  //  case MultiCycleOpKind.Udivmod => {
  //  }
  //  case MultiCycleOpKind.Sdivmod => {
  //  }
  //  case _ => {
  //    assert(false)
  //  }
  //}
}
case class MultiCycleDevPayload(
  cfg: SnowHouseConfig,
  opInfo: OpInfo,
) extends Bundle {
  assert(
    cfg.opInfoMap.find(_._2 == opInfo) != None
  )
  assert(
    opInfo.select == OpSelect.MultiCycle
  )
  val dstVec = Vec.fill(opInfo.dstArr.size)(
    UInt(cfg.mainWidth bits)
  )
}
//--------
object SnowHouseMemAccessKind
extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    LoadU,      // unsigned
    LoadS,      // signed
    Store//,
    //AtomicRmw
    = newElement();
}
object SnowHouseMemAccessSubKind
extends SpinalEnum(defaultEncoding=binaryOneHot) {
  val
    Sz8,
    Sz16,
    Sz32,
    Sz64
    = newElement()
  //def fromWordSize[
  //  EncInstrT <: Data
  //](
  //  cfg: SnowHouseConfig
  //): DbusHostMemAccessSubKind.E = {
  //  cfg.mainWidth match {
  //    case 8 => {
  //      DbusHostMemAccessSubKind.Sz8
  //    }
  //    case 16 => {
  //      DbusHostMemAccessSubKind.Sz16
  //    }
  //    case 32 => {
  //      DbusHostMemAccessSubKind.Sz32
  //    }
  //    case 64 => {
  //      DbusHostMemAccessSubKind.Sz64
  //    }
  //    case _ => {
  //      assert(
  //        false,
  //        s"not yet implemented"
  //      )
  //      DbusHostMemAccessSubKind.Sz8
  //    }
  //  }
  //}
}
object SnowHouseMemAccessSubKindToBinSeq {
  def apply(
    subKind: SnowHouseMemAccessSubKind.C
  ): UInt = {
    //val ret = SnowHouseMemAccessSubKind(binarySequential)
    //ret := subKind
    //ret.asBits.asUInt
    val ret = UInt(log2Up(subKind.asBits.getWidth) bits)
    switch (subKind) {
      is (SnowHouseMemAccessSubKind.Sz8) {
        ret := 0x0
      }
      is (SnowHouseMemAccessSubKind.Sz16) {
        ret := 0x1
      }
      is (SnowHouseMemAccessSubKind.Sz32) {
        ret := 0x2
      }
      is (SnowHouseMemAccessSubKind.Sz64) {
        ret := 0x3
      }
    }
    ret
    //(1 << tempSubKind.asBits.asUInt)
  }
}
case class BusHostPayload(
  cfg: SnowHouseConfig,
  isIbus: Boolean,
) extends Bundle {
  val addr = UInt(cfg.mainWidth bits)
  val data = (!isIbus) generate (
    UInt(cfg.mainWidth bits)
  )
  val accKind = (!isIbus) generate (
    SnowHouseMemAccessKind()
  )
  val subKind = (!isIbus) generate (
    SnowHouseMemAccessSubKind()
  )
  val lock = (!isIbus) generate (
    Bool() // for atomics
  )
  //val haveHazard = (!isIbus) generate (
  //  Bool()
  //)
}
case class BusDevPayload(
  cfg: SnowHouseConfig,
  isIbus: Boolean,
) extends Bundle {
  val instr = (isIbus) generate (UInt(cfg.instrMainWidth bits))
  val data = (!isIbus) generate (UInt(cfg.mainWidth bits))
}
//--------
