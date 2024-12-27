package libsnowhouse

import scala.collection.immutable
import scala.collection.mutable._
//import scala.reflect.macros.whitebox.Context
//import scala.language.experimental.macros

import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._

import libcheesevoyage.general._
import libcheesevoyage.math._
import libcheesevoyage.bus.lcvStall._

//object SealedTraitToSpinalEnum {
//  def apply[
//    MyTraitT
//  ]
//  (c: Context) 
//  = {
//    q"object MyTraitEnum extends SpinalEnum(defaultEncoding=binarySequential)"
//  }
//}

//class SealedTraitToSpinalEnumImpl(
//  val c: Context
//) {
//  //val myType = macro tq"SrcKind"
//  def myType[T: c.WeakTypeTag] = tq"val x = T"
//}
//object SealedTraitToSpinalEnum {
//  def myType[T] = macro SealedTraitToSpinalEnumImpl.myType[T]
//}

// kinds of source operands of instructions
sealed trait SrcKind
object SrcKind {
  case object Gpr extends SrcKind
  // TODO: support `MultiGpr`
  //case object MultiGpr extends SrcKind
  //case object ZeroExtGpr extends SrcKind  // zero-extended
  //                                        // general purpose register (to
  //                                        // double the normal size of a
  //                                        // general purpose register)
  //case object SignExtGpr extends SrcKind  // sign-extended
  //                                        // general purpose register (to
  //                                        // double the normal size of a
  //                                        // general purpose register)
  case object RegPc extends SrcKind
  case object Mem extends SrcKind   // data read from mem by a load
                                    // instruction
  case object ZImm                  // Zero-extended
                                    // (or cfg.mainWidth bits) immediate
  case object SImm                  // Sign-extended immediate
}

// kinds of destination operands of instruction
sealed trait DstKind
object DstKind {
  case object Gpr extends DstKind
  // TODO: support `MultiGpr`
  //case object MultiGpr extends DstKind 
  case object RegPc extends DstKind
  case object Mem extends DstKind   // data written to mem by a store
                                    // instruction
}

//sealed trait AddrCalcKind
//object AddrCalcKind {
//  //--------
//  case object OnlyOne extends AddrCalcKind
//  case object AddTwo extends AddrCalcKind
//  //--------
//}

//case class OpInfo(
//) {
//}
//trait OpInfo { 
//  def isAlu: Boolean
//}
//trait OpInfo {
//}

class OpInfo(
  val dst: DstKind,
  val srcArr: Seq[SrcKind],
  val select: OpSelect,
  val cond: CondKind=CondKind.Always
  //var aluOp: Option[AluOpKind]=None,
  //val opCond: AluOpKind | LoadOpKind | StoreOpKind,
  //var cond: Option[CondKind]=None,
) {
  //--------
  //private[libsnowhouse] var _dst: DstKind = null
  //private[libsnowhouse] var _srcArr: Seq[SrcKind] = null
  //private[libsnowhouse] var _select: OpSelect = null
  private[libsnowhouse] var _cpyOp: CpyOpKind = null
  private[libsnowhouse] var _aluOp: AluOpKind = null
  private[libsnowhouse] var _multiCycleOp: MultiCycleOpKind = null
  private[libsnowhouse] var _loadOp: LoadOpKind = null
  private[libsnowhouse] var _storeOp: StoreOpKind = null
  //private[libsnowhouse] var _cond: CondOpKind = null
  //--------
  //def dst: DstKind = _dst
  //def srcArr: Seq[SrcKind] = _srcArr
  //def select: OpSelect = _select
  def cpyOp: Option[CpyOpKind] = (
    if (select == OpSelect.Cpy) {
      if (_cpyOp != null) {
        Some(_cpyOp)
      } else {
        None
      }
    } else {
      None
    }
  )
  def aluOp: Option[AluOpKind] = (
    if (select == OpSelect.Alu) {
      if (_aluOp != null) {
        Some(_aluOp)
      } else {
        None
      }
    } else {
      None
    }
  )
  def multiCycleOp: Option[MultiCycleOpKind] = (
    if (select == OpSelect.MultiCycle) {
      if (_multiCycleOp != null) {
        Some(_multiCycleOp)
      } else {
        None
      }
    } else {
      None
    }
  )
  def loadOp: Option[LoadOpKind] = (
    if (select == OpSelect.Load) {
      if (_loadOp != null) {
        Some(_loadOp)
      } else {
        None
      }
    } else {
      None
    }
  )
  def storeOp: Option[StoreOpKind] = (
    if (select == OpSelect.Store) {
      if (_storeOp != null) {
        Some(_storeOp)
      } else {
        None
      }
    } else {
      None
    }
  )
  //def condOp: Option[CondOpKind] = (
  //  if (select == OpSelect.Jump) {
  //    if (_cond != null) {
  //      Some(_cond)
  //    } else {
  //      None
  //    }
  //  } else {
  //    None
  //  }
  //)
  //--------
}
object OpInfo {
  def mkCpy(
    dst: DstKind,
    srcArr: Seq[SrcKind],
    cpyOp: CpyOpKind,
    cond: CondKind=CondKind.Always,
  ): OpInfo = {
    val ret = new OpInfo(
      dst=dst,
      srcArr=srcArr,
      select=OpSelect.Cpy,
      cond=cond
    )
    ret._cpyOp = cpyOp
    ret
  }
  def mkAlu(
    dst: DstKind,
    srcArr: Seq[SrcKind],
    aluOp: AluOpKind,
    cond: CondKind=CondKind.Always,
  ): OpInfo = {
    val ret = new OpInfo(
      dst=dst,
      srcArr=srcArr,
      select=OpSelect.Alu,
      cond=cond
    )
    ret._aluOp = aluOp
    ret
  }
  def mkMultiCycle(
    dst: DstKind,
    srcArr: Seq[SrcKind],
    multiCycleOp: MultiCycleOpKind,
    cond: CondKind=CondKind.Always,
  ): OpInfo = {
    val ret = new OpInfo(
      dst=dst,
      srcArr=srcArr,
      select=OpSelect.MultiCycle,
      cond=cond
    )
    ret._multiCycleOp = multiCycleOp
    ret
  }
  def mkLoad(
    dst: DstKind,
    srcArr: Seq[SrcKind],
    loadOp: LoadOpKind,
    cond: CondKind=CondKind.Always,
  ): OpInfo = {
    val ret = new OpInfo(
      dst=dst,
      srcArr=srcArr,
      select=OpSelect.Load,
      cond=cond
    )
    ret._loadOp = loadOp
    ret
  }
  def mkStore(
    dst: DstKind,
    srcArr: Seq[SrcKind],
    storeOp: StoreOpKind,
    cond: CondKind=CondKind.Always,
  ): OpInfo = {
    val ret = new OpInfo(
      dst=dst,
      srcArr=srcArr,
      select=OpSelect.Store,
      cond=cond
    )
    ret._storeOp = storeOp
    ret
  }
}
sealed trait OpSelect
object OpSelect {
  //--------
  case object Cpy extends OpSelect // "move"/"load immediate" operation
  case object Alu extends OpSelect // just an ALU operation
  case object MultiCycle extends OpSelect
  case object Load extends OpSelect
  case object Store extends OpSelect
  //case object Jump extends OpSelect // jump or a relative Branch
  //case object Branch extends MainOpKind
  //--------
}
//--------
sealed trait CpyOpKind
object CpyOpKind {
  //--------
  case object Cpy extends CpyOpKind
  //case object Jump extends CpyOpKind
  //case object Branch extends CpyOpKind 
  //--------
}
//--------
// ALU-type instructions evaluated entirely in the EX pipeline stage
// (besides forwarding!)
sealed trait AluOpKind
object AluOpKind {
  //--------
  case object Add extends AluOpKind
  case object Adc extends AluOpKind
  case object Sub extends AluOpKind
  case object Sbc extends AluOpKind
  //--------
  //case object Cpy extends AluOpKind // "move"/"load immediate" instruction
  case object Lsl extends AluOpKind
  case object Lsr extends AluOpKind
  case object Asr extends AluOpKind
  case object And extends AluOpKind
  case object Orr extends AluOpKind
  case object Xor extends AluOpKind
  //--------
  case object Cmp extends AluOpKind
  case object CmpBc extends AluOpKind // see the `flare_cpu` git repo's
                                      // `docs/flare_cpu` for more info
  //--------
  // TODO: support these other branch condition kinds
  //case object Sltu extends AluOpKind
  //case object Slts extends AluOpKind
  //--------
}
sealed trait MultiCycleOpKind
object MultiCycleOpKind {
  //--------
  case object Mul extends MultiCycleOpKind
  case object Udiv extends MultiCycleOpKind
  case object Umod extends MultiCycleOpKind
  case object Udivmod extends MultiCycleOpKind
  case object Sdiv extends MultiCycleOpKind
  case object Smod extends MultiCycleOpKind
  case object Sdivmod extends MultiCycleOpKind
  //--------
  //case object Umull extends MultiCycleOpKind
  //case object Smull extends MultiCycleOpKind
  //--------
}

// Load-type instructions evaluated within both the EX and MEM pipeline
// stages
sealed trait LoadOpKind
object LoadOpKind {
  //--------
  case object LdU8 extends LoadOpKind
  case object LdS8 extends LoadOpKind
  case object LdU16 extends LoadOpKind
  case object LdS16 extends LoadOpKind
  case object LdU32 extends LoadOpKind
  case object LdS32 extends LoadOpKind
  case object Ld64 extends LoadOpKind
  //--------
}

// Store-type instructions evaluated within both the EX and MEM pipeline
// stages
sealed trait StoreOpKind
object StoreOpKind {
  //--------
  case object St8 extends StoreOpKind
  case object St16 extends StoreOpKind
  case object St32 extends StoreOpKind
  case object St64 extends StoreOpKind
  //--------
}


//// Branch- and Jump-type instructions evaluated primarily within the EX
//// pipeline stage

// various kinds of conditions (most well known for conditional branches,
// but also potentially useful for conditional moves)
sealed trait CondKind
object CondKind {
  //--------
  case object Link extends CondKind   // do it and Link
  case object Always extends CondKind // do it Always 
  //case object Never extends CondKind
  case object Eq extends CondKind     // do it if EQual
  case object Ne extends CondKind     // do it if Not Equal
  //--------
  case object Mi extends CondKind     // do it if MInus
  case object Pl extends CondKind     // do it if PLus
  case object Vs extends CondKind     // do it if oVerflow Set
  case object Vc extends CondKind     // do it if oVerflow Clear
  //--------
  case object Geu extends CondKind    // do it if Greater than or Equal,
                                      // Unsigned
  case object Ltu extends CondKind    // do it if Less Than, Unsigned
  case object Gtu extends CondKind    // do it if Greater Than, Unsigned
  case object Leu extends CondKind    // do it if Less than or Equal,
                                      // Unsigned
  //--------
  case object Ges extends CondKind    // do it if Greater than or Equal,
                                      // Signed
  case object Lts extends CondKind    // do it if Less Than, Signed
  case object Gts extends CondKind    // do it if Greater Than, Signed
  case object Les extends CondKind    // do it if Less than or Equal,
                                      // Signed
  //--------
  // TODO: support these other branch condition kinds
  //case object Nz extends BranchOpCondKind // non-zero 
  //case object Z extends BranchOpCondKind  // zero
}
