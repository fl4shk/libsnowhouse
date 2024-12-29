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
  case object Pc extends SrcKind
  //case object PcPlusSimm extends SrcKind
  //case object Mem extends SrcKind   // data read from mem by a load
  //                                  // instruction
  case object ZImm extends SrcKind  // Zero-extended
                                    // (or cfg.mainWidth bits) immediate
  case object SImm extends SrcKind  // Sign-extended immediate
}

// kinds of destination operands of instruction
sealed trait DstKind
object DstKind {
  case object Gpr extends DstKind
  // TODO: support `MultiGpr`
  //case object MultiGpr extends DstKind 
  case object Pc extends DstKind
  //case object Mem extends DstKind   // data written to mem by a store
  //                                  // instruction
  case object AluFlags extends DstKind
}
//--------
sealed trait ModifySrcDstKind
object ModifySrcDstKind {
  case object NoModify extends ModifySrcDstKind
  case object Mem extends ModifySrcDstKind
}
case class AddrCalcKindOptions(
  minNum: Int,
  maxNum: Option[Int],
  lslAmount: Option[Int],
)
sealed trait AddrCalcKind {
  //def limits: (Int, Option[Int]) // min, max
  //def lslAmount: Option[Int]     // how much to left shift by
  def options: AddrCalcKindOptions
}
object AddrCalcKind {
  case object AddReduce extends AddrCalcKind {
    //def limits: (Int, Option[Int]) = (1, None)
    //def lslAmount: Option[Int] = None
    private[libsnowhouse] val _options = AddrCalcKindOptions(
      minNum=1,
      maxNum=None,
      lslAmount=None,
    )
    def options: AddrCalcKindOptions = _options
  }
  case object LslFor16ThenMaybeAdd extends AddrCalcKind {
    //def limits: (Int, Option[Int]) = (1, None)
    //def lslAmount: Option[Int] = Some(1)
    private[libsnowhouse] val _options = AddrCalcKindOptions(
      minNum=1,
      maxNum=None,
      lslAmount=Some(1),
    )
    def options: AddrCalcKindOptions = _options
  }
  case object LslFor32ThenMaybeAdd extends AddrCalcKind {
    //def limits: (Int, Option[Int]) = (1, None)
    //def lslAmount: Option[Int] = Some(2)
    private[libsnowhouse] val _options = AddrCalcKindOptions(
      minNum=1,
      maxNum=None,
      lslAmount=Some(2),
    )
    def options: AddrCalcKindOptions = _options
  }
  case object LslFor64ThenMaybeAdd extends AddrCalcKind {
    //def limits: (Int, Option[Int]) = (1, None)
    //def lslAmount: Option[Int] = Some(3)
    private[libsnowhouse] val _options = AddrCalcKindOptions(
      minNum=1,
      maxNum=None,
      lslAmount=Some(3),
    )
    def options: AddrCalcKindOptions = _options
  }
}
//--------

//sealed trait SrcModOpKind
//object SrcModOpKind {
//  //case object Dont extends SrcModOpKind  // don't use sub-op
//  ////case object Alu extends SrcModOpKind   // use the ALU op
//  case object Add extends SrcModOpKind  // add source operands
//  case object Sub extends SrcModOpKind  // subtract source operands
//}

class OpInfo(
  val dstArr: Seq[DstKind],
  val srcArr: Seq[SrcKind],
  val select: OpSelect,
  val cond: CondKind=CondKind.Always,
  val modify: ModifySrcDstKind=ModifySrcDstKind.NoModify,
  val addrCalc: AddrCalcKind=AddrCalcKind.AddReduce,
  //var aluOp: Option[AluOpKind]=None,
  //val opCond: AluOpKind | LoadOpKind | StoreOpKind,
  //var cond: Option[CondKind]=None,
) {
  //--------
  //private[libsnowhouse] var _dst: Seq[DstKind] = null
  //private[libsnowhouse] var _srcArr: Seq[SrcKind] = null
  //private[libsnowhouse] var _select: OpSelect = null
  private[libsnowhouse] var _cpyOp: CpyOpKind = null
  private[libsnowhouse] var _aluOp: AluOpKind = null
  private[libsnowhouse] var _multiCycleOp: MultiCycleOpKind = null
  private[libsnowhouse] var _loadOp: LoadOpKind = null
  private[libsnowhouse] var _storeOp: StoreOpKind = null
  //private[libsnowhouse] var _cond: CondOpKind = null
  //--------
  //def dstArr: Seq[DstKind] = _dst
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
    dstArr: Seq[DstKind],
    srcArr: Seq[SrcKind],
    cpyOp: CpyOpKind,
    cond: CondKind=CondKind.Always,
  ): OpInfo = {
    val ret = new OpInfo(
      dstArr=dstArr,
      srcArr=srcArr,
      select=OpSelect.Cpy,
      cond=cond
    )
    ret._cpyOp = cpyOp
    ret
  }
  def mkAlu(
    dstArr: Seq[DstKind],
    srcArr: Seq[SrcKind],
    aluOp: AluOpKind,
    cond: CondKind=CondKind.Always,
  ): OpInfo = {
    val ret = new OpInfo(
      dstArr=dstArr,
      srcArr=srcArr,
      select=OpSelect.Alu,
      cond=cond
    )
    ret._aluOp = aluOp
    ret
  }
  def mkMultiCycle(
    dstArr: Seq[DstKind],
    srcArr: Seq[SrcKind],
    multiCycleOp: MultiCycleOpKind,
    cond: CondKind=CondKind.Always,
  ): OpInfo = {
    val ret = new OpInfo(
      dstArr=dstArr,
      srcArr=srcArr,
      select=OpSelect.MultiCycle,
      cond=cond
    )
    ret._multiCycleOp = multiCycleOp
    ret
  }
  def mkLoad(
    dstArr: Seq[DstKind],
    srcArr: Seq[SrcKind],
    loadOp: LoadOpKind,
    cond: CondKind=CondKind.Always,
    addrCalc: AddrCalcKind=AddrCalcKind.AddReduce,
  ): OpInfo = {
    val ret = new OpInfo(
      dstArr=dstArr,
      srcArr=srcArr,
      select=OpSelect.Load,
      cond=cond,
      modify=ModifySrcDstKind.Mem,
      addrCalc=addrCalc,
    )
    ret._loadOp = loadOp
    ret
  }
  def mkStore(
    dstArr: Seq[DstKind],
    srcArr: Seq[SrcKind],
    storeOp: StoreOpKind,
    cond: CondKind=CondKind.Always,
    addrCalc: AddrCalcKind=AddrCalcKind.AddReduce,
  ): OpInfo = {
    val ret = new OpInfo(
      dstArr=dstArr,
      srcArr=srcArr,
      select=OpSelect.Store,
      cond=cond,
      modify=ModifySrcDstKind.Mem,
      addrCalc=addrCalc,
    )
    ret._storeOp = storeOp
    ret
  }
}
sealed trait OpSelect
object OpSelect {
  // This is to guarantee we `match` properly and have the Scala compiler
  // definitely check that we covered every kind
  //--------
  // "jump"/"branch"/"move"/"load immediate" operation
  case object Cpy extends OpSelect 
  case object Alu extends OpSelect // just an ALU operation
  case object MultiCycle extends OpSelect
  case object Load extends OpSelect
  case object Store extends OpSelect
  //case object Jump extends OpSelect // jump or a relative Branch
  //case object Branch extends MainOpKind
  //--------
}
//--------
case class OpKindLimits(
  minD: Int,
  maxD: Int,
  minS: Int,
  maxS: Int,
) {
}
sealed trait OpKindBase {
  //def minNumDsts: Int
  //def maxNumDsts: Int
  //def minNumSrcs: Int
  //def maxNumSrcs: Int
  def limits: OpKindLimits
}
//--------
sealed trait CpyOpKind extends OpKindBase
object CpyOpKind {
  //--------
  case object Cpy extends CpyOpKind {
    // copy ("move"/"load immediate")
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=2, minS=1, maxS=2
    )
    def limits = _limits
  }
  case object Cpyui extends CpyOpKind {
    // copy upper immediate ("load upper immediate")
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=2, minS=1, maxS=2
    )
    def limits = _limits
  }
  // NOTE: `Jmp` and `Br` these are special so that the implementation will 
  // use separate adders from the ALU
  case object Jmp extends CpyOpKind {
    // jump
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1,
      maxD=2, // for "jump and link"
      minS=1,
      maxS=3, // for "compare and jump" (in one instruction)
    )
    def limits = _limits
  }
  case object Br extends CpyOpKind {
    // branch relative
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1,
      maxD=2, // for "branch and link"
      minS=1,
      maxS=3, // for "compare and branch" (in one instruction)
    )
    def limits = _limits
  }
  //--------
}
//--------
// ALU-type instructions evaluated entirely in the EX pipeline stage
// (besides forwarding!)
sealed trait AluOpKind extends OpKindBase
object AluOpKind {
  //--------
  case object Add extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object Adc extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object Sub extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object Sbc extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  //--------
  //case object Cpy extends AluOpKind // "move"/"load immediate" instruction
  case object Lsl extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object Lsr extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object Asr extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object And extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object Or extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object Xor extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  //--------
  case object Cmp extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object CmpBc extends AluOpKind {
    // see the `flare_cpu` git repo's `docs/flare_cpu` for more info
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
                                      
  case object Sltu extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  case object Slts extends AluOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=2
    )
    def limits = _limits
  }
  //--------
  //--------
}
sealed trait MultiCycleOpKind extends OpKindBase
object MultiCycleOpKind {
  //--------
  case object Mul extends MultiCycleOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=2, minS=2, maxS=4
    )
    def limits = _limits
  }
  case object Udiv extends MultiCycleOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=2, minS=2, maxS=4
    )
    def limits = _limits
  }
  case object Umod extends MultiCycleOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=2, minS=2, maxS=4
    )
    def limits = _limits
  }
  case object Udivmod extends MultiCycleOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=2, maxD=4, minS=2, maxS=4
    )
    def limits = _limits
  }
  case object Sdiv extends MultiCycleOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=2, minS=2, maxS=4
    )
    def limits = _limits
  }
  case object Smod extends MultiCycleOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=2, minS=2, maxS=4
    )
    def limits = _limits
  }
  case object Sdivmod extends MultiCycleOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=2, maxD=4, minS=2, maxS=4
    )
    def limits = _limits
  }
  //--------
  //case object Umull extends MultiCycleOpKind
  //case object Smull extends MultiCycleOpKind
  //--------
}

// Load-type instructions evaluated within both the EX and MEM pipeline
// stages
sealed trait LoadOpKind extends OpKindBase
object LoadOpKind {
  //--------
  case object LdU8 extends LoadOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  case object LdS8 extends LoadOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  case object LdU16 extends LoadOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  case object LdS16 extends LoadOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  case object LdU32 extends LoadOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  case object LdS32 extends LoadOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  case object Ld64 extends LoadOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  //--------
}

// Store-type instructions evaluated within both the EX and MEM pipeline
// stages
sealed trait StoreOpKind extends OpKindBase
object StoreOpKind {
  //--------
  case object St8 extends StoreOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  case object St16 extends StoreOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  case object St32 extends StoreOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  case object St64 extends StoreOpKind {
    private[libsnowhouse] val _limits = OpKindLimits(
      minD=1, maxD=1, minS=2, maxS=3
    )
    def limits = _limits
  }
  //--------
}


//// Branch- and Jump-type instructions evaluated primarily within the EX
//// pipeline stage

// various kinds of conditions (most well known for conditional branches,
// but also potentially useful for conditional moves)
sealed trait CondKind {
  //def minNumSrcs: Int
  //def maxNumSrcs: Int
  //def numDsts: Int
}
object CondKind {
  //--------
  case object Link extends CondKind {   // do it and Link
    //def numSrcs: Int = 1
  }
  case object Always extends CondKind { // do it Always 
    //def 
  }
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
  case object Z extends CondKind  // zero
  case object Nz extends CondKind // non-zero 
}
