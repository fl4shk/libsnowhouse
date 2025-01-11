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
  case object Ira extends SrcKind
  case object Ids extends SrcKind
  case object Ie extends SrcKind
  case object IndexReg extends SrcKind
  //case class PcPlusImm(
  //  isSImm: Option[Boolean],
  //) extends SrcKind
  //case object Mem extends SrcKind   // data read from mem by a load
  //                                  // instruction
  //case object ZImm extends SrcKind  // Zero-extended
  //                                  // (or cfg.mainWidth bits) immediate
  //case object SImm extends SrcKind  // Sign-extended immediate
  case class Imm(
    //isSImm: Option[Boolean], // `None` means "either unsigned or signed"
  ) extends SrcKind
  case object AluFlags extends SrcKind  // 
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
  case object Ira extends DstKind
  case object Ids extends DstKind
  case object Ie extends DstKind
  case object IndexReg extends DstKind
  case object AluFlags extends DstKind
}
//--------
sealed trait MemAccessKind
object MemAccessKind {
  case object NoMemAccess extends MemAccessKind
  sealed trait SubKind
  object SubKind {
    case object Sz8 extends SubKind
    case object Sz16 extends SubKind
    case object Sz32 extends SubKind
    case object Sz64 extends SubKind
  }
  case class Mem(
    isSigned: Boolean,
    isStore: Option[Boolean],   // `None` means this is atomic
    subKind: SubKind,
  ) extends MemAccessKind
  def Mem8(
    isSigned: Boolean,
    isStore: Option[Boolean],
  ) = Mem(
    isSigned=isSigned,
    isStore=isStore,
    subKind=SubKind.Sz8,
  )
  def Mem16(
    isSigned: Boolean,
    isStore: Option[Boolean],
  ) = Mem(
    isSigned=isSigned,
    isStore=isStore,
    subKind=SubKind.Sz16,
  )
  def Mem32(
    isSigned: Boolean,
    isStore: Option[Boolean],
  ) = Mem(
    isSigned=isSigned,
    isStore=isStore,
    subKind=SubKind.Sz32,
  )
  def Mem64(
    isSigned: Boolean,
    isStore: Option[Boolean],
  ) = Mem(
    isSigned=isSigned,
    isStore=isStore,
    subKind=SubKind.Sz64,
  )
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
  case class AddReduce(
    //toIndexReg: Boolean,
    fromIndexReg: Boolean,
  ) extends AddrCalcKind {
    private[libsnowhouse] val _options = AddrCalcKindOptions(
      minNum=1,
      maxNum=None,
      lslAmount=None,
    )
    def options: AddrCalcKindOptions = _options
  }
  private[libsnowhouse] case class LslThenMaybeAdd(
    lslAmount: Int,
  ) extends AddrCalcKind {
    private[libsnowhouse] val _options = AddrCalcKindOptions(
      minNum=1,
      maxNum=None,
      lslAmount=Some(lslAmount),
    )
    def options: AddrCalcKindOptions = _options
  }
  def LslFor16ThenMaybeAdd() = (
    LslThenMaybeAdd(
      lslAmount=1
    )
  )
  def LslFor32ThenMaybeAdd() = (
    LslThenMaybeAdd(
      lslAmount=2
    )
  )
  def LslFor64ThenMaybeAdd() = (
    LslThenMaybeAdd(
      lslAmount=3
    )
  )
  //case object LslFor32ThenMaybeAdd extends AddrCalcKind {
  //  private[libsnowhouse] val _options = AddrCalcKindOptions(
  //    minNum=1,
  //    maxNum=None,
  //    lslAmount=Some(2),
  //  )
  //  def options: AddrCalcKindOptions = _options
  //}
  //case object LslFor64ThenMaybeAdd extends AddrCalcKind {
  //  private[libsnowhouse] val _options = AddrCalcKindOptions(
  //    minNum=1,
  //    maxNum=None,
  //    lslAmount=Some(3),
  //  )
  //  def options: AddrCalcKindOptions = _options
  //}
}
//--------

//sealed trait SrcModOpKind
//object SrcModOpKind {
//  //case object Dont extends SrcModOpKind  // don't use sub-op
//  ////case object Alu extends SrcModOpKind   // use the ALU op
//  case object Add extends SrcModOpKind  // add source operands
//  case object Sub extends SrcModOpKind  // subtract source operands
//}
//trait MultiCycleOp

private[libsnowhouse] case class OpInfoValidArgsTuple(
  validArgs: OpKindValidArgs,
  setIdx: Int,
  //dstIdx: Int,
  //srcIdx: Int,
) {
}
class OpInfo(
  val dstArr: Seq[DstKind],
  val srcArr: Seq[SrcKind],
  val select: OpSelect,
  val cond: CondKind=CondKind.Always,
  val memAccess: MemAccessKind=MemAccessKind.NoMemAccess,
  val addrCalc: AddrCalcKind=AddrCalcKind.AddReduce(false),
  //var aluOp: Option[AluOpKind]=None,
  //val opCond: AluOpKind | LoadOpKind | StoreOpKind,
  //var cond: Option[CondKind]=None,
) {
  def findValidArgs(opKind: OpKindBase): Option[OpInfoValidArgsTuple] = {
    //opKind.validArgsSet.find(validArgs => (
    //  this.dstArr.size == validArgs.dstSize 
    //  && this.srcArr.size == validArgs.srcSize
    //))
    //opKind.validArgsSet.find(validArgs => 
    for ((validArgs, setIdx) <- opKind.validArgsSet.view.zipWithIndex) {
      if (
        if (
          dstArr.size == validArgs.dst.size
          && srcArr.size == validArgs.src.size
        ) {
          var found: Boolean = true
          for ((dst: DstKind, dstIdx: Int) <- dstArr.view.zipWithIndex) {
            if (!validArgs.dst(dstIdx).contains(dst)) {
              found = false
            }
          }
          if (found) {
            for ((src, srcIdx) <- srcArr.view.zipWithIndex) {
              if (!validArgs.src(srcIdx).contains(src)) {
                found = false
              }
            }
          }
          //if (memAccess != validArgs.memAccess) {
          //  found = false
          //}
          //if (addrCalc != validArgs.addrCalc) {
          //  found = false
          //}
          //if (found) {
          //  if (validArgs.cond.size == 0) {
          //    
          //  }
          //}
          found
        } else {
          false
        }
      ) {
        _validArgsTuple = (
          OpInfoValidArgsTuple(validArgs=validArgs, setIdx=setIdx)
        )
        return Some(
          _validArgsTuple
        )
      }
      //else {
      //}
    }
    return None
    //)
  }
  private[libsnowhouse] var _validArgsTuple: OpInfoValidArgsTuple = null
  //--------
  //private[libsnowhouse] var _dst: Seq[DstKind] = null
  //private[libsnowhouse] var _srcArr: Seq[SrcKind] = null
  //private[libsnowhouse] var _select: OpSelect = null
  private[libsnowhouse] var _cpyOp: CpyOpKind = null
  private[libsnowhouse] var _aluOp: AluOpKind = null
  private[libsnowhouse] var _multiCycleOp: MultiCycleOpKind = null
  //private[libsnowhouse] var _multiCycleArea: Area = null
  //private[libsnowhouse] var _loadOp: LoadOpKind = null
  //private[libsnowhouse] var _storeOp: StoreOpKind = null
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
  //def loadOp: Option[LoadOpKind] = (
  //  if (select == OpSelect.Load) {
  //    if (_loadOp != null) {
  //      Some(_loadOp)
  //    } else {
  //      None
  //    }
  //  } else {
  //    None
  //  }
  //)
  //def storeOp: Option[StoreOpKind] = (
  //  if (select == OpSelect.Store) {
  //    if (_storeOp != null) {
  //      Some(_storeOp)
  //    } else {
  //      None
  //    }
  //  } else {
  //    None
  //  }
  //)
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
    //multiCycleArea: Area,
  ): OpInfo = {
    val ret = new OpInfo(
      dstArr=dstArr,
      srcArr=srcArr,
      select=OpSelect.MultiCycle,
      cond=cond,
      //multiCycleArea=Some(multiCycleArea),
    )
    ret._multiCycleOp = multiCycleOp
    ret
  }
  def mkLdSt(
    dstArr: Seq[DstKind],
    srcArr: Seq[SrcKind],
    //loadOp: LoadOpKind,
    modify: MemAccessKind,
    cond: CondKind=CondKind.Always,
    addrCalc: AddrCalcKind=AddrCalcKind.AddReduce(false),
  ): OpInfo = {
    val ret = new OpInfo(
      dstArr=dstArr,
      srcArr=srcArr,
      select=(
        //OpSelect.Load
        OpSelect.Cpy
      ),
      cond=cond,
      memAccess={
        assert(modify != MemAccessKind.NoMemAccess)
        //ModifySrcDstKind.Mem
        modify
      },
      addrCalc=addrCalc,
    )
    ret._cpyOp = CpyOpKind.Cpy
    //ret._loadOp = loadOp
    ret
  }
  //def mkStore(
  //  dstArr: Seq[DstKind],
  //  srcArr: Seq[SrcKind],
  //  //storeOp: StoreOpKind,
  //  modify: MemAccessKind,
  //  cond: CondKind=CondKind.Always,
  //  addrCalc: AddrCalcKind=AddrCalcKind.AddReduce,
  //): OpInfo = {
  //  val ret = new OpInfo(
  //    dstArr=dstArr,
  //    srcArr=srcArr,
  //    select=(
  //      //OpSelect.Store
  //      OpSelect.Cpy
  //    ),
  //    cond=cond,
  //    memAccess={
  //      assert(modify != MemAccessKind.NoMemAccess)
  //      //ModifySrcDstKind.Mem
  //      modify
  //    },
  //    addrCalc=addrCalc,
  //  )
  //  //ret._storeOp = storeOp
  //  ret
  //}
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
  //case object Load extends OpSelect
  //case object Store extends OpSelect
  //case object Jump extends OpSelect // jump or a relative Branch
  //case object Branch extends MainOpKind
  //--------
}
//--------
case class OpKindValidArgs(
  //minD: Int,
  //maxD: Int,
  //minS: Int,
  //maxS: Int,
  //--------
  //dstSize: Int,
  //srcSize: Int,
  dst: Seq[HashSet[DstKind]],
  src: Seq[HashSet[SrcKind]],
  cond: HashSet[CondKind], // size of zero indicates "not implemented yet"
  //modifyDst: ModifySrcDstKind=ModifySrcDstKind.NoModify,
  //modifySrc: ModifySrcDstKind=ModifySrcDstKind.NoModify,
  //modify: ModifySrcDstKind=ModifySrcDstKind.NoModify,
  //addrCalc: AddrCalcKind=AddrCalcKind.AddReduce,
  //--------
  //optExtraDstKind: Option[DstKind]=None,
  //optExtraSrcKind: Option[SrcKind]=None,
) {
  //val dstSize = dst.size
  //val srcSize = src.size
  //def eq(that: OpKindNumArgs): Boolean = (
  //  dst == that.dst
  //  && src == that.dst
  //)
  //def notEq(that: OpKindNumArgs): Boolean = (
  //  !(this eq that) 
  //)
}
sealed trait OpKindBase {
  //def minNumDsts: Int
  //def maxNumDsts: Int
  //def minNumSrcs: Int
  //def maxNumSrcs: Int
  def validArgsSet: LinkedHashSet[OpKindValidArgs]
  def anyNumSrcCondSet = HashSet[CondKind](
    CondKind.Always,
    //CondKind.Link,
  )
  def requireCmpAluFlagsCondSet = HashSet[CondKind](
    //CondKind.Always,
    //CondKind.Link,
    CondKind.Eq,      // do it if EQual
    CondKind.Ne,      // do it if Not Equal
    ////--------
    CondKind.Mi,      // do it if MInus
    CondKind.Pl,      // do it if PLus
    CondKind.Vs,      // do it if oVerflow Set
    CondKind.Vc,      // do it if oVerflow Clear
    //--------
    CondKind.Geu,     // do it if Greater than or Equal,
                                        // Unsigned
    CondKind.Ltu,     // do it if Less Than, Unsigned
    CondKind.Gtu,     // do it if Greater Than, Unsigned
    CondKind.Leu,     // do it if Less than or Equal,
                                        // Unsigned
    //--------
    CondKind.Ges,     // do it if Greater than or Equal,
                                        // Signed
    CondKind.Lts,     // do it if Less Than, Signed
    CondKind.Gts,     // do it if Greater Than, Signed
    CondKind.Les,     // do it if Less than or Equal,
  )
  def requireCmpTwoSrcCondSet = HashSet[CondKind](
    //CondKind.Always,
    //CondKind.Link,
    CondKind.Eq,      // do it if EQual
    CondKind.Ne,      // do it if Not Equal
    ////--------
    //CondKind.Mi,      // do it if MInus
    //CondKind.Pl,      // do it if PLus
    //CondKind.Vs,      // do it if oVerflow Set
    //CondKind.Vc,      // do it if oVerflow Clear
    //--------
    CondKind.Geu,     // do it if Greater than or Equal,
                                        // Unsigned
    CondKind.Ltu,     // do it if Less Than, Unsigned
    CondKind.Gtu,     // do it if Greater Than, Unsigned
    CondKind.Leu,     // do it if Less than or Equal,
                                        // Unsigned
    //--------
    CondKind.Ges,     // do it if Greater than or Equal,
                                        // Signed
    CondKind.Lts,     // do it if Less Than, Signed
    CondKind.Gts,     // do it if Greater Than, Signed
    CondKind.Les,     // do it if Less than or Equal,
  )
  def requireCmpOneSrcCondSet = HashSet[CondKind](
    CondKind.Z,
    CondKind.Nz,
  )
}
//--------
sealed trait CpyOpKind extends OpKindBase
object CpyOpKind {
  //--------
  case object Cpy extends CpyOpKind {
    // copy ("move"/"load immediate")
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      OpKindValidArgs(
        // word
        // load/store/atomic
        dst=Array[HashSet[DstKind]](
          HashSet(
            DstKind.Gpr, //DstKind.AluFlags
            //DstKind.IndexReg, // DstKind.IndexReg turns this into an Add
          ),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(
            SrcKind.Gpr,
            SrcKind.Pc,
            //SrcKind.IndexReg,
          ),
          HashSet(
            SrcKind.Gpr,
            SrcKind.Pc,
            //SrcKind.ZImm,
            //SrcKind.SImm,
            SrcKind.Imm(/*None*/),
            //SrcKind.Imm(Some(true)),
            //SrcKind.Imm(Some(false)),
            //SrcKind.AluFlags,
          ),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      OpKindValidArgs(
        // word
        dst=Array[HashSet[DstKind]](
          HashSet(
            DstKind.Gpr, 
            DstKind.AluFlags,
            //DstKind.Ira,
            //DstKind.Ids,
            //DstKind.Ie,
          ),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(
            SrcKind.Gpr,
            SrcKind.Pc,
            //SrcKind.ZImm,
            //SrcKind.SImm,
            SrcKind.Imm(/*None*/),
            //SrcKind.Imm(Some(true)),
            //SrcKind.Imm(Some(false)),
            SrcKind.AluFlags,
            //SrcKind.Ira,
            //SrcKind.Ids,
            //SrcKind.Ie,
          ),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      OpKindValidArgs(
        // two-word
        //dstSize=2, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
        ),
        cond=HashSet[CondKind](
        ),
      ),
    )
    def validArgsSet = _validArgsSet
  }
  case object Cpyu extends CpyOpKind {
    // copy upper
    // alternatively,
    // copy upper immediate ("load upper immediate")
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=2, minS=1, maxS=2
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=1
        dst=Array[HashSet[DstKind]](
          HashSet(
            DstKind.Gpr,
            //DstKind.AluFlags
          ),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(
            SrcKind.Gpr,
            SrcKind.Imm(/*None*/),
            //SrcKind.Imm(Some(true)),
            //SrcKind.Imm(Some(false)),
          ),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindValidArgs(
      //  // two-word
      //  //dstSize=2, srcSize=2
      //  dst=Array[HashSet[DstKind]](
      //  ),
      //  src=Array[HashSet[SrcKind]](
      //  ),
      //),
    )
    def validArgsSet = _validArgsSet
  }
  // NOTE: `Jmp` and `Br` these are special so that the implementation
  // will use separate adders from the ALU (and so there's more fmax)  
  case object Jmp extends CpyOpKind {
    // jump
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1,
      //maxD=2, // for "jump and link"
      //minS=1,
      //maxS=3, // for "compare and jump" (in one instruction)
      OpKindValidArgs(
        //dstSize=1, srcSize=1
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Pc),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      // TODO: add these back later
      OpKindValidArgs( // for "jump and link"
        //dstSize=2, srcSize=1
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Pc),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr/*, SrcKind.Imm(Some(false))*/)
        ),
        cond=HashSet[CondKind](
          //CondKind.Link
          CondKind.Always
        ),
      ),
      //OpKindValidArgs( // for "compare and jump"
      //  //dstSize=1, srcSize=3
      //  dst=Array[HashSet[DstKind]](
      //    HashSet(DstKind.Pc),
      //  ),
      //  src=Array[HashSet[SrcKind]](
      //    HashSet(SrcKind.Gpr),
      //    HashSet(SrcKind.Gpr),
      //    HashSet(SrcKind.Gpr, SrcKind.Imm(Some(false))),
      //  ),
      //),
                                    // (in one instruction)
    )
    def validArgsSet = _validArgsSet
  }
  case object Br extends CpyOpKind {
    // branch relative
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1,
      //maxD=2, // for "branch and link"
      //minS=1,
      //maxS=3, // for "compare and branch" (in one instruction)
      OpKindValidArgs(
        //dstSize=1, srcSize=1
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Pc)
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr),
          HashSet(
            SrcKind.Imm(/*None*/),
            //SrcKind.Imm(Some(false)),
            //SrcKind.Imm(Some(true))
          )
        ),
        cond=(
          //HashSet[CondKind](
          //  CondKind.Always
          //)
          //++ 
          requireCmpOneSrcCondSet
        ),
      ),
      OpKindValidArgs(
        // for "branch and link"
        //dstSize=2, srcSize=1
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Pc),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          //CondKind.Link
          CondKind.Always
        ),
      ),
      OpKindValidArgs(
        // for "compare and branch" (in one instruction)
        //dstSize=1, srcSize=3
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Pc),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Imm(/*None*/)),
        ),
        cond=(
          requireCmpTwoSrcCondSet
        )
      ),
      OpKindValidArgs(
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Pc)
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.AluFlags),
          HashSet(SrcKind.Imm(/*None*/)),
        ),
        cond=requireCmpAluFlagsCondSet
      )
    )
    def validArgsSet = _validArgsSet
  }
  //--------
}
//--------
// ALU-type instructions evaluated entirely in the EX pipeline stage
// (besides forwarding!)
case class InstrResult(
  cfg: SnowHouseConfig,
)(
  width: Int=cfg.mainWidth,
) extends Area {
  assert(
    width <= cfg.mainWidth,
    s"not permitted: width:${width} > cfg.mainWidth:${cfg.mainWidth}"
  )
  assert(
    width > 0,
    s"not permitted: width:${width} <= 0"
  )
  //private[libsnowhouse] def _vnMask: BigInt = (
  //  (1.toLong << (width.toLong - 1.toLong)).toLong
  //)
  //val vnMask = Cat(
  //  True,
  //  U((width - 1) bits, default -> False),
  //).asUInt
  val leftMsb = Bool()
  val rightMsb = Bool()
  val main = UInt(cfg.mainWidth bits)
  val flagN: Bool = main(width - 1)
  def getAddSubFlagV = (
    (leftMsb ^ flagN)
    & (rightMsb ^ flagN)
  )
  val flagV = Bool()
  val flagC = Bool()
  val flagZ: Bool = (
    main(width - 1 downto 0) =/= 0
  )
}
sealed trait AluOpKind extends OpKindBase {
  def binopFunc(
    cfg: SnowHouseConfig,
    left: UInt,
    right: UInt,
    carry: Bool,
  )(
    width: Int=cfg.mainWidth,
  ): InstrResult
}
object AluOpKind {
  //--------
  case object Add extends AluOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(
            DstKind.Gpr, DstKind.IndexReg,
          )
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.IndexReg, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(dst=2, src=4),  // two-word
      OpKindValidArgs(
        // word, with output flags
        //dstSize=2, srcSize=2,
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(                // two-word, with flags
      //  dst=2,
      //  src=4,
      //),
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      //Some(left + right)
      val ret = InstrResult(cfg=cfg)(width=width)
      val tempSum = UInt((width + 1) bits)
      val tempLeft = Cat(False, Cat(left)(width - 1 downto 0)).asUInt
      val tempRight = Cat(False, Cat(right)(width - 1 downto 0)).asUInt
      //val tempCarryIn = Cat(U(s"${width}'d0"), True).asUInt
      tempSum := tempLeft + tempRight //+ tempCarryIn
      ret.main := tempSum.resized
      ret.leftMsb := left.msb
      ret.rightMsb := right.msb
      ret.flagV := ret.getAddSubFlagV
      ret.flagC := tempSum.msb
      ret
    }
    
  }
  case object Adc extends AluOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        //dstSize=1, srcSize=3
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr)
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.AluFlags),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        )
      ),  // word, with input and output flags
      //OpKindNumArgs(dst=2, src=5),  // two-word
      OpKindValidArgs(
        // word, with input and output flags
        //dstSize=2,
        //srcSize=3,
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.AluFlags),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        )
      ),
      //OpKindNumArgs(                // two-word, with flags
      //  dst=2,
      //  src=5,
      //),
    )
    def validArgsSet = _validArgsSet
    //def binopFunc(left: UInt, right: UInt): Option[UInt] = None
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      //Some(left + right)
      val ret = InstrResult(cfg=cfg)(width=width)
      val tempSum = UInt((width + 1) bits)
      val tempLeft = Cat(False, Cat(left)(width - 1 downto 0)).asUInt
      val tempRight = Cat(False, Cat(right)(width - 1 downto 0)).asUInt
      val tempCarryIn = Cat(U(s"${width}'d0"), True).asUInt
      tempSum := tempLeft + tempRight + tempCarryIn
      ret.main := tempSum.resized
      ret.leftMsb := left.msb
      ret.rightMsb := right.msb
      ret.flagV := ret.getAddSubFlagV
      ret.flagC := tempSum.msb
      ret
    }
  }
  case object Sub extends AluOpKind {
    private[libsnowhouse] val _subCmpSrc = (
      Array[HashSet[SrcKind]](
        HashSet(SrcKind.Gpr, SrcKind.Pc),
        HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
      ),
    )
    private[libsnowhouse] val _subCmpCond = (
      HashSet[CondKind](
        CondKind.Always
      )
    )
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word `Sub` or `Cmp`
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(
            DstKind.Gpr,
            //DstKind.AluFlags
            //  // `DstKind.AluFlags` means this is a `Cmp` instruction
          ),
        ),
        src=_subCmpSrc,
        cond=_subCmpCond,
      ),
      OpKindValidArgs(
        // word `Sub` or `Cmp`
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(
            //DstKind.Gpr,
            DstKind.AluFlags
            //  // `DstKind.AluFlags` means this is a `Cmp` instruction
          ),
        ),
        src=_subCmpSrc,
        cond=(
          //HashSet[CondKind](
          //  CondKind.Always
          //)
          _subCmpCond
        ),
      ),
      ////OpKindNumArgs(dst=2, src=4),  // two-word
      OpKindValidArgs(
        // word, with output flags
        //dstSize=2,
        //srcSize=2,
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=(
          _subCmpSrc
          //Array[HashSet[SrcKind]](
          //  HashSet(SrcKind.Gpr, SrcKind.Pc),
          //  HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
          //)
        ),
        cond=(
          _subCmpCond
          //HashSet[CondKind](
          //  CondKind.Always
          //)
        ),
      ),
      //OpKindNumArgs(                // two-word, with flags
      //  dst=2,
      //  src=4,
      //),
    )
    def validArgsSet = _validArgsSet
    //def binopFunc(left: UInt, right: UInt): Option[UInt] = (
    //  Some(left - right)
    //)
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      //Some(left + right)
      val ret = InstrResult(cfg=cfg)(width=width)
      val tempSum = UInt((width + 1) bits)
      val tempLeft = Cat(False, Cat(left)(width - 1 downto 0)).asUInt
      val tempRight = Cat(False, Cat(right)(width - 1 downto 0)).asUInt
      val tempCarryIn = Cat(U(s"${width}'d0"), True).asUInt
      tempSum := (
        tempLeft - tempRight
        //tempLeft + (~tempRight) + tempCarryIn
      )
      ret.main := tempSum.resized
      ret.leftMsb := left.msb
      ret.rightMsb := right.msb
      ret.flagV := ret.getAddSubFlagV
      ret.flagC := tempSum.msb
      ret
    }
  }
  case object Sbc extends AluOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      ////minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word, with input and output flags
        //dstSize=1, srcSize=3
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.AluFlags),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(dst=2, src=5),  // two-word
      OpKindValidArgs(
        // word, with output flags
        //dstSize=2,
        //srcSize=3,
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.AluFlags),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(                // two-word, with flags
      //  dst=2,
      //  src=5,
      //),
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      //Some(left + right)
      val ret = InstrResult(cfg=cfg)(width=width)
      val tempSum = UInt((width + 1) bits)
      val tempLeft = Cat(False, Cat(left)(width - 1 downto 0)).asUInt
      val tempRight = Cat(False, Cat(right)(width - 1 downto 0)).asUInt
      val tempCarryIn = Cat(U(s"${width}'d0"), True).asUInt
      tempSum := (
        //tempLeft - tempRight
        tempLeft + (~tempRight) + tempCarryIn
      )
      ret.main := tempSum.resized
      ret.leftMsb := left.msb
      ret.rightMsb := right.msb
      ret.flagV := ret.getAddSubFlagV
      ret.flagC := tempSum.msb
      ret
    }
  }
  //--------
  //case object Cpy extends AluOpKind // "move"/"load immediate" instruction
  case object Lsl extends AluOpKind {
    // logical shift left
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*Some(false)*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(dst=2, src=4),  // two-word
      OpKindValidArgs(
        // word, with output flags
        //dstSize=2,
        //srcSize=2,
        dst=Array[HashSet[DstKind]](
          //HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*Some(false)*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(                // two-word, with flags
      //  dst=2,
      //  src=4,
      //  optExtraDstKind=Some(DstKind.AluFlags),
      //),
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      val ret = InstrResult(cfg=cfg)(width=width)
      val tempLeft = Cat(left).asUInt(width - 1 downto 0)
      val tempRight = Cat(right).asUInt(width - 1 downto 0)
      ret.leftMsb := left(width - 1)
      ret.rightMsb := right(width - 1)
      ret.main := (
        tempLeft << tempRight(log2Up(width) downto 0)
      ).resized//(ret.main.bitsRange)
      ret.flagV := False
      ret.flagC := False
      ret
    }
  }
  case object Lsr extends AluOpKind {
    // logical shift right
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*Some(false)*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(dst=2, src=4),  // two-word
      OpKindValidArgs(
        // word, with output flags
        //dstSize=2,
        //srcSize=2,
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*Some(false)*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(                // two-word, with flags
      //  dst=2,
      //  src=4,
      //  optExtraDstKind=Some(DstKind.AluFlags),
      //),
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      val ret = InstrResult(cfg=cfg)(width=width)
      val tempLeft = Cat(left).asUInt(width - 1 downto 0)
      val tempRight = Cat(right).asUInt(width - 1 downto 0)
      ret.leftMsb := left(width - 1)
      ret.rightMsb := right(width - 1)
      ret.main := (
        tempLeft >> tempRight//(log2Up(cfg.mainWidth) downto 0)
      ).resized
      ret.flagV := False
      ret.flagC := False
      ret
    }
  }
  case object Asr extends AluOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*Some(false)*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),  // word
      ////OpKindNumArgs(dst=2, src=4),  // two-word
      OpKindValidArgs(
        // word, with output flags
        //dstSize=2,
        //srcSize=2,
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*Some(false)*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      val ret = InstrResult(cfg=cfg)(width=width)
      val tempLeft = Cat(left).asUInt(width - 1 downto 0)
      val tempRight = Cat(right).asUInt(width - 1 downto 0)
      ret.leftMsb := left(width - 1)
      ret.rightMsb := right(width - 1)
      ret.main := (
        tempLeft.asSInt >> tempRight//(log2Up(cfg.mainWidth) downto 0)
      ).asUInt.resized
      ret.flagV := False
      ret.flagC := False
      ret
    }
  }
  case object And extends AluOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(dst=2, src=4),  // two-word
      OpKindValidArgs(
        // word, with output flags
        //dstSize=2,
        //srcSize=2,
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      val ret = InstrResult(cfg=cfg)(width=width)
      val tempLeft = Cat(left).asUInt(width - 1 downto 0)
      val tempRight = Cat(right).asUInt(width - 1 downto 0)
      ret.leftMsb := left(width - 1)
      ret.rightMsb := right(width - 1)
      ret.main := (
        tempLeft & tempRight
      ).resized
      ret.flagV := False
      ret.flagC := False
      ret
    }
  }
  case object Or extends AluOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      ////OpKindNumArgs(dst=2, src=4),  // two-word
      OpKindValidArgs(
        // word, with output flags
        //dstSize=2,
        //srcSize=2,
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      val ret = InstrResult(cfg=cfg)(width=width)
      ret.main := (
        left | right
      )
      ret.flagV := False
      ret.flagC := False
      ret
    }
  }
  case object Xor extends AluOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      //OpKindNumArgs(dst=2, src=4),  // two-word
      OpKindValidArgs(
        // word, with output flags
        //dstSize=2,
        //srcSize=2,
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.AluFlags),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      val ret = InstrResult(cfg=cfg)(width=width)
      val tempLeft = Cat(left).asUInt(width - 1 downto 0)
      val tempRight = Cat(right).asUInt(width - 1 downto 0)
      ret.leftMsb := left(width - 1)
      ret.rightMsb := right(width - 1)
      ret.main := (
        tempLeft ^ tempRight
      ).resized
      ret.flagV := False
      ret.flagC := False
      ret
    }
  }
  //--------
  //case object Cmp extends AluOpKind {
  //  private[libsnowhouse] val _validArgsSet = LinkedHashSet[
  //    OpKindNumArgs
  //  ](
  //    //minD=1, maxD=1, minS=2, maxS=2
  //  )
  //  def validArgsSet = _validArgsSet
  //}
  //--------
  // TODO: support CmpBc
  //case object CmpBc extends AluOpKind {
  //  // see the `flare_cpu` git repo's `docs/flare_cpu` for more info
  //  private[libsnowhouse] val _validArgsSet = LinkedHashSet[
  //    OpKindNumArgs
  //  ](
  //    //minD=1, maxD=1, minS=2, maxS=2
  //    OpKindNumArgs(dst=1, src=2),  // word
  //  )
  //  def validArgsSet = _validArgsSet
  //}
                                      
  case object Sltu extends AluOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      )
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      val ret = InstrResult(cfg=cfg)(width=width)
      when (left < right) {
        ret.main := 1
      } otherwise {
        ret.main := 0
      }
      ret.flagV := False
      ret.flagC := False
      ret
    }
  }
  case object Slts extends AluOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=1, minS=2, maxS=2
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      )
    )
    def validArgsSet = _validArgsSet
    def binopFunc(
      cfg: SnowHouseConfig, left: UInt, right: UInt, carry: Bool
    )(
      width: Int=cfg.mainWidth
    ) = {
      val ret = InstrResult(cfg=cfg)(width=width)
      when (left.asSInt < right.asSInt) {
        ret.main := 1
      } otherwise {
        ret.main := 0
      }
      ret.flagV := False
      ret.flagC := False
      ret
    }
  }
  //--------
  //--------
}
sealed trait MultiCycleOpKind extends OpKindBase
object MultiCycleOpKind {
  //--------
  case object Umul extends MultiCycleOpKind {
    // `Umul` also represents non-full-product multiplies
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=2, minS=2, maxS=4
      OpKindValidArgs(
        // word, non-full-product
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      OpKindValidArgs(
        // word, unsigned full-product
        //dstSize=2, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
  }
  case object Smul extends MultiCycleOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      // word, signed full product
      //minD=1, maxD=2, minS=2, maxS=4
      OpKindValidArgs(
        //dstSize=2, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
  }
  case object Udiv extends MultiCycleOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=2, minS=2, maxS=4
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      OpKindValidArgs(
        // dual-word
        //dstSize=2, srcSize=4
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
  }
  case object Sdiv extends MultiCycleOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=2, minS=2, maxS=4
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      OpKindValidArgs(
        // dual-word
        //dstSize=2, srcSize=4
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
  }
  case object Umod extends MultiCycleOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=2, minS=2, maxS=4
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      OpKindValidArgs(
        // two-word
        //dstSize=2, srcSize=4
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
  }
  case object Smod extends MultiCycleOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=1, maxD=2, minS=2, maxS=4
      OpKindValidArgs(
        // word
        //dstSize=1, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(/*None*/)),
        ),
        cond=HashSet[CondKind](
          CondKind.Always
        ),
      ),
      OpKindValidArgs(
        // two-word
        //dstSize=2, srcSize=4
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
          HashSet(SrcKind.Gpr, SrcKind.Pc),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
  }
  case object Udivmod extends MultiCycleOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=2, maxD=4, minS=2, maxS=4
      OpKindValidArgs(
        //dstSize=2, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),
      OpKindValidArgs(
        // dual-word
        //dstSize=4, srcSize=4
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
  }
  case object Sdivmod extends MultiCycleOpKind {
    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
      OpKindValidArgs
    ](
      //minD=2, maxD=4, minS=2, maxS=4
      OpKindValidArgs(
        //dstSize=2, srcSize=2
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),  // word
      OpKindValidArgs(
        // dual-word
        //dstSize=4, srcSize=4
        dst=Array[HashSet[DstKind]](
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
          HashSet(DstKind.Gpr),
        ),
        src=Array[HashSet[SrcKind]](
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
          HashSet(SrcKind.Gpr),
        ),
        cond=HashSet[CondKind](
          //CondKind.Always
        ),
      ),
    )
    def validArgsSet = _validArgsSet
  }
  //--------
  //case object Lumul extends MultiCycleOpKind {
  //  // unsigned full product
  //  private[libsnowhouse] val _validArgsSet = Array[OpKindLimits](
  //    minD=2, maxD=2, minS=2, maxS=2
  //  )
  //  def validArgsSet = _validArgsSet
  //}
  //case object Lsmul extends MultiCycleOpKind {
  //  // signed full product
  //  private[libsnowhouse] val _validArgsSet = Array[OpKindLimits](
  //    minD=2, maxD=2, minS=2, maxS=2
  //  )
  //  def validArgsSet = _validArgsSet
  //}
  //case object Lsmul extends MultiCycleOpKind
  //--------
}

// Load-type instructions evaluated within both the EX and MEM pipeline
// stages
//sealed trait LoadOpKind extends OpKindBase
//object LoadOpKind {
//  //--------
//  case object LdU8 extends LoadOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc),
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object LdS8 extends LoadOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc),
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object LdU16 extends LoadOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc),
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object LdS16 extends LoadOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc),
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object LdU32 extends LoadOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc),
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object LdS32 extends LoadOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc),
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object LdU64 extends LoadOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc),
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object LdS64 extends LoadOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//          HashSet(DstKind.Gpr),
//        ),
//        src=Array[HashSet[SrcKind]](
//          HashSet(SrcKind.Gpr, SrcKind.Pc),
//          HashSet(SrcKind.Gpr, SrcKind.Pc, SrcKind.Imm(None)),
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  //--------
//}

//// Store-type instructions evaluated within both the EX and MEM pipeline
//// stages
//sealed trait StoreOpKind extends OpKindBase
//object StoreOpKind {
//  //--------
//  case object St8 extends StoreOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//        ),
//        src=Array[HashSet[SrcKind]](
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//        ),
//        src=Array[HashSet[SrcKind]](
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object St16 extends StoreOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//        ),
//        src=Array[HashSet[SrcKind]](
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//        ),
//        src=Array[HashSet[SrcKind]](
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object St32 extends StoreOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//        ),
//        src=Array[HashSet[SrcKind]](
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//        ),
//        src=Array[HashSet[SrcKind]](
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  case object St64 extends StoreOpKind {
//    private[libsnowhouse] val _validArgsSet = LinkedHashSet[
//      OpKindValidArgs
//    ](
//      //minD=1, maxD=1, minS=2, maxS=3
//      OpKindValidArgs(
//        //dstSize=1, srcSize=1
//        dst=Array[HashSet[DstKind]](
//        ),
//        src=Array[HashSet[SrcKind]](
//        ),
//      ),
//      OpKindValidArgs(
//        //dstSize=1, srcSize=2
//        dst=Array[HashSet[DstKind]](
//        ),
//        src=Array[HashSet[SrcKind]](
//        ),
//      ),
//    )
//    def validArgsSet = _validArgsSet
//  }
//  //--------
//}


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
  //case object Link extends CondKind {   // do it and Link
  //  //def numSrcs: Int = 1
  //}
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
