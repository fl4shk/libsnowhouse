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

//trait SampleCpuAsmExpr {
//}
//--------
object SampleCpuAsmAstFuncs {
  def zeroExtend(
    toExtend: Int
  ): Long = {
    val temp = toExtend.toLong & ((1.toLong << 32.toLong) - 1.toLong)
    temp
  }
}
sealed trait SampleCpuExpr {
  private[libsnowhouse] var _isSigned: Boolean = false
  //private[libsnowhouse] var _pc: Option[Int] = None
  //--------
  def asUInt = {
    SampleCpuExpr.ExprAsUInt(child=this)
  }
  def asSInt = {
    SampleCpuExpr.ExprAsSInt(child=this)
  }
  //--------
  def +(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprPlus(left=this, right=that)
  }
  def -(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprMinus(left=this, right=that)
  }
  //--------
  def unary_- = {
    SampleCpuExpr.ExprUnopMinus(child=this)
  }
  //--------
  def *(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprMul(left=this, right=that)
  }
  def /(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprDiv(left=this, right=that)
  }
  def %(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprMod(left=this, right=that)
  }
  //--------
  def ===(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprCmpEq(left=this, right=that)
  }
  def =/=(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprCmpNe(left=this, right=that)
  }
  def <(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprCmpLt(left=this, right=that)
  }
  def >=(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprCmpGe(left=this, right=that)
  }
  def >(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprCmpGt(left=this, right=that)
  }
  def <=(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprCmpLe(left=this, right=that)
  }
  //--------
  def &(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprBitAnd(left=this, right=that)
  }
  def |(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprBitOr(left=this, right=that)
  }
  def ^(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprBitXor(left=this, right=that)
  }
  def ~ = {
    SampleCpuExpr.ExprBitInvert(child=this)
  }
  //--------
  def <<(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprBitLshift(left=this, right=that)
  }
  def >>(that: SampleCpuExpr) = {
    //if (!this._isSigned) (
    SampleCpuExpr.ExprBitRshift(left=this, right=that)
    //) else (
    //  SampleCpuAsmAst.ExprBitAsr(left=this, right=that)
    //)
  }
  //def >>>(that: SampleCpuAsmAst) = {
  //  ExprBitAsr(left=this, right=that)
  //}
  //--------
  def &&(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprLogicAnd(left=this, right=that)
  }
  def ||(that: SampleCpuExpr) = {
    SampleCpuExpr.ExprLogicOr(left=this, right=that)
  }

  def unary_! : SampleCpuExpr = {
    SampleCpuExpr.ExprLogicInvert(child=this)
  }
  //--------
  def evaluate(
    assembler: SampleCpuAssembler
  ): Int = {
    this match {
      //case SampleCpuAsmAst.Gpr(
      //  index: Int
      //) => {
      //  assert(false)
      //  0
      //}
      //--------
      case label: SampleCpuExpr.LabRef => {
        assembler._labelMap.get(label.name) match {
          case Some((label, value)) => {
            value
          }
          case None => {
            assert(
              false,
              s"Unknown label ${label.name}"
            )
            0x0
          }
        }
        //label._value match {
        //  case Some(value) => {
        //    value.evaluate(assembler)
        //  }
        //  case None => {
        //    assert(false)
        //    0x0
        //  }
        //}
      }
      case SampleCpuExpr.Dot => {
        assembler._pc
        //dot._pc match {
        //  case Some(pc) => {
        //    pc
        //  }
        //  case None => {
        //    assert(false)
        //    0x0
        //  }
        //}
      }
      case exprInt: SampleCpuExpr.ExprInt => {
        exprInt.value
      }
      //--------
      case SampleCpuExpr.ExprAsUInt(
        child: SampleCpuExpr,
      ) => {
        child._isSigned = false
        child.evaluate(assembler)
      }
      case SampleCpuExpr.ExprAsSInt(
        child: SampleCpuExpr,
      ) => {
        child._isSigned = true
        child.evaluate(assembler)
      }
      case SampleCpuExpr.ExprPlus( // left + right
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        left.evaluate(assembler) + right.evaluate(assembler)
      }
      case SampleCpuExpr.ExprMinus( // left - right
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        left.evaluate(assembler) - right.evaluate(assembler)
      }
      //--------
      case SampleCpuExpr.ExprUnopMinus( // -value
        child: SampleCpuExpr,
      ) => {
        -child.evaluate(assembler)
      }
      //--------
      case SampleCpuExpr.ExprMul( // *
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        left.evaluate(assembler) * right.evaluate(assembler)
      }
      case SampleCpuExpr.ExprDiv( // /
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          (
            SampleCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            / SampleCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
          ).toInt
        ) else (
          left.evaluate(assembler) / right.evaluate(assembler)
        )
      }
      case SampleCpuExpr.ExprMod( // %
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          (
            SampleCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            % SampleCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
          ).toInt
        ) else (
          left.evaluate(assembler) % right.evaluate(assembler)
        )
      }
      //--------
      case SampleCpuExpr.ExprCmpEq( // ===
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (left.evaluate(assembler) == right.evaluate(assembler)) (
          1
        ) else (
          0
        )
      }
      case SampleCpuExpr.ExprCmpNe( // =/=
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (left.evaluate(assembler) != right.evaluate(assembler)) (
          1
        ) else (
          0
        )
      }
      case SampleCpuExpr.ExprCmpLt( // <
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          if (
            SampleCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            < SampleCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
          ) (
            1
          ) else (
            0
          )
        ) else (
          if (left.evaluate(assembler) < right.evaluate(assembler)) (
            1
          ) else (
            0
          )
        )
      }
      case SampleCpuExpr.ExprCmpGe( // >=
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          if (
            SampleCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            >= SampleCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
          ) (
            1
          ) else (
            0
          )
        ) else (
          if (left.evaluate(assembler) >= right.evaluate(assembler)) (
            1
          ) else (
            0
          )
        )
      }
      case SampleCpuExpr.ExprCmpGt( // >
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          if (
            SampleCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            > SampleCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
          ) (
            1
          ) else (
            0
          )
        ) else (
          if (left.evaluate(assembler) > right.evaluate(assembler)) (
            1
          ) else (
            0
          )
        )
      }
      case SampleCpuExpr.ExprCmpLe( // <=
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          if (
            SampleCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            <= SampleCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
          ) (
            1
          ) else (
            0
          )
        ) else (
          if (left.evaluate(assembler) <= right.evaluate(assembler)) (
            1
          ) else (
            0
          )
        )
      }
      //--------
      case SampleCpuExpr.ExprBitAnd( // &
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        left.evaluate(assembler) & right.evaluate(assembler)
      }
      case SampleCpuExpr.ExprBitOr( // |
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        left.evaluate(assembler) | right.evaluate(assembler)
      }
      case SampleCpuExpr.ExprBitXor( // ^
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        left.evaluate(assembler) ^ right.evaluate(assembler)
      }
      case SampleCpuExpr.ExprBitInvert( // ~
        child: SampleCpuExpr,
      ) => {
        ~child.evaluate(assembler)
      }
      //--------
      case SampleCpuExpr.ExprBitLshift( // <<
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        left.evaluate(assembler) << right.evaluate(assembler)
      }
      case SampleCpuExpr.ExprBitRshift( // >>
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (!left._isSigned) (
          (
            SampleCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            >> right.evaluate(assembler).toLong
          ).toInt
        ) else (
          left.evaluate(assembler) >> right.evaluate(assembler)
        )
      }
      //case SampleCpuAsmAst.ExprBitAsr( // >>>
      //  left: SampleCpuAsmAst,
      //  right: SampleCpuAsmAst,
      //) => {
      //  //val temp: Long = left.evaluate(assembler)
      //  left.evaluate(assembler) >> right.evaluate(assembler)
      //}
      //--------
      case SampleCpuExpr.ExprLogicAnd( // &&
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (
          (left.evaluate(assembler) != 0)
          && (right.evaluate(assembler) != 0)
        ) (
          1
        ) else (
          0
        )
      }
      case SampleCpuExpr.ExprLogicOr( // ||
        left: SampleCpuExpr,
        right: SampleCpuExpr,
      ) => {
        if (
          (left.evaluate(assembler) != 0)
          || (right.evaluate(assembler) != 0)
        ) (
          1
        ) else (
          0
        )
      }
      case SampleCpuExpr.ExprLogicInvert( // !
        child: SampleCpuExpr,
      ) => {
        if (!(child.evaluate(assembler) != 0)) (
          1
        ) else (
          0
        )
      }
      //--------
      //--------
      //case _ => {
      //  assert(false)
      //  0
      //}
    }
  }
  //--------
}
//--------
object SnowHouseRegs {
  val r0 = Gpr(0)
  val r1 = Gpr(1)
  val r2 = Gpr(2)
  val r3 = Gpr(3)
  val r4 = Gpr(4)
  val r5 = Gpr(5)
  val r6 = Gpr(6)
  val r7 = Gpr(7)
  val r8 = Gpr(8)
  val r9 = Gpr(9)
  val r10 = Gpr(10)
  val r11 = Gpr(11)
  val r12 = Gpr(12)
  val lr = Gpr(13)
  val fp = Gpr(14)
  val sp = Gpr(15)
}
//--------
object add {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SampleCpuOp.AddRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      simm16=0,
    )
  }
}
object sub {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SampleCpuOp.SubRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      simm16=0,
    )
  }
}
object sltu {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SampleCpuOp.SltuRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      simm16=0,
    )
  }
}
object and {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SampleCpuOp.AndRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      simm16=0,
    )
  }
}
object orr {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SampleCpuOp.OrrRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      simm16=0,
    )
  }
}
object xor {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SampleCpuOp.XorRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      simm16=0,
    )
  }
}
object lsl {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SampleCpuOp.LslRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      simm16=0,
    )
  }
}
object lsr {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SampleCpuOp.LsrRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      simm16=0,
    )
  }
}
//object asr {
//  def apply(
//    rA: Gpr,
//    rB: Gpr,
//    rC: Gpr,
//  ) = {
//    Instruction(
//      op=SampleCpuOp.AsrRaRbRc,
//      rA=rA,
//      rB=rB,
//      rC=rC,
//      simm16=0,
//    )
//  }
//}
object mul {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr
  ) = {
    Instruction(
      op=SampleCpuOp.MulRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      simm16=0,
    )
  }
}
object bz {
  def apply(
    rA: Gpr,
    //rB: SampleCpuAsmAst.Gpr,
    //rC: SampleCpuAsmAst.Gpr,
    simm16: SampleCpuExpr,
  ) = {
    Instruction(
      op=SampleCpuOp.BzRaSimm,
      rA=rA,
      rB=SnowHouseRegs.r0,
      rC=SnowHouseRegs.r0,
      simm16=(
        simm16 - SampleCpuExpr.Dot - 4
      ),
    )
  }
  //def apply(
  //  rA: Gpr,
  //  //rB: SampleCpuAsmAst.Gpr,
  //  //rC: SampleCpuAsmAst.Gpr,
  //  simm16: Int,
  //) = {
  //  Instruction(
  //    op=SampleCpuOp.BzRaSimm,
  //    rA=rA,
  //    rB=SnowHouseRegs.r0,
  //    rC=SnowHouseRegs.r0,
  //    simm16=SampleCpuExpr.ExprInt(simm16),
  //  )
  //}
}
object bnz {
  def apply(
    rA: Gpr,
    //rB: SampleCpuAsmAst.Gpr,
    //rC: SampleCpuAsmAst.Gpr,
    simm16: SampleCpuExpr,
  ) = {
    Instruction(
      op=SampleCpuOp.BnzRaSimm,
      rA=rA,
      rB=SnowHouseRegs.r0,
      rC=SnowHouseRegs.r0,
      simm16=(
        //simm16,
        simm16 - SampleCpuExpr.Dot - 4
      ),
    )
  }
  //def apply(
  //  rA: Gpr,
  //  //rB: SampleCpuAsmAst.Gpr,
  //  //rC: SampleCpuAsmAst.Gpr,
  //  simm16: Int,
  //) = {
  //  Instruction(
  //    op=SampleCpuOp.BnzRaSimm,
  //    rA=rA,
  //    rB=SnowHouseRegs.r0,
  //    rC=SnowHouseRegs.r0,
  //    simm16=SampleCpuExpr.ExprInt(simm16),
  //  )
  //}
}
object jmp {
  def apply(
    rA: Gpr
  ) = {
    Instruction(
      op=SampleCpuOp.JmpRa,
      rA=rA,
      rB=SnowHouseRegs.r0,
      rC=SnowHouseRegs.r0,
      simm16=0,
    )
  }
}
object ldr {
  def apply(
    rA: Gpr,
    rB: Gpr,
    simm16: SampleCpuExpr,
  ) = {
    Instruction(
      op=SampleCpuOp.LdrRaRbSimm,
      rA=rA,
      rB=rB,
      rC=SnowHouseRegs.r0,
      simm16=simm16,
    )
  }
  //def apply(
  //  rA: Gpr,
  //  rB: Gpr,
  //  simm16: Int
  //) = {
  //  Instruction(
  //    op=SampleCpuOp.LdrRaRbSimm,
  //    rA=rA,
  //    rB=rB,
  //    rC=SnowHouseRegs.r0,
  //    simm16=SampleCpuExpr.ExprInt(simm16),
  //  )
  //}
}
object str {
  def apply(
    rA: Gpr,
    rB: Gpr,
    simm16: SampleCpuExpr,
  ) = {
    Instruction(
      op=SampleCpuOp.StrRaRbSimm,
      rA=rA,
      rB=rB,
      rC=SnowHouseRegs.r0,
      simm16=simm16,
    )
  }
  //def apply(
  //  rA: Gpr,
  //  rB: Gpr,
  //  simm16: Int
  //) = {
  //  Instruction(
  //    op=SampleCpuOp.StrRaRbSimm,
  //    rA=rA,
  //    rB=rB,
  //    rC=SnowHouseRegs.r0,
  //    simm16=SampleCpuExpr.ExprInt(simm16),
  //  )
  //}
}
object cpyui {
  def apply(
    rA: Gpr,
    simm16: SampleCpuExpr,
  ) = {
    Instruction(
      op=SampleCpuOp.CpyuiRaSimm,
      rA=rA,
      rB=SnowHouseRegs.r0,
      rC=SnowHouseRegs.r0,
      simm16=simm16
    )
  }
  //def apply(
  //  rA: Gpr,
  //  simm16: Int
  //) = {
  //  Instruction(
  //    op=SampleCpuOp.CpyuiRaSimm,
  //    rA=rA,
  //    rB=SnowHouseRegs.r0,
  //    rC=SnowHouseRegs.r0,
  //    simm16=SampleCpuExpr.ExprInt(simm16),
  //  )
  //}
}
object cpyi {
  def apply(
    rA: Gpr,
    simm16: SampleCpuExpr,
  ) = {
    Instruction(
      op=SampleCpuOp.CpyiRaSimm,
      rA=rA,
      rB=SnowHouseRegs.r0,
      rC=SnowHouseRegs.r0,
      simm16=simm16
    )
  }
  //def apply(
  //  rA: Gpr,
  //  simm16: Int
  //) = {
  //  Instruction(
  //    op=SampleCpuOp.CpyiRaSimm,
  //    rA=rA,
  //    rB=SnowHouseRegs.r0,
  //    rC=SnowHouseRegs.r0,
  //    simm16=SampleCpuExpr.ExprInt(simm16),
  //  )
  //}
}
//object MyTest {
//  val asdf = bnz(r0, 3)
//}
case class Gpr(
  val index: Int=0
) /*extends SampleCpuAsmAst*/ {
  //println(
  //  s"${index} ${SampleCpuParams.numGprs}: "
  //  + s"${index < SampleCpuParams.numGprs}"
  //)
  assert(
    index >= 0,
    s"${index}",
  )
  assert(
    index < SampleCpuInstrEnc.numGprs,
    s"${index}",
  )
}
object Instruction {
  def apply(
    op: (/*UInt,*/ Int, String),
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
    simm16: SampleCpuExpr,
  ) = {
    AsmStmt(
      instr=Some(
        new Instruction(
          op=op,
          rA=rA,
          rB=rB,
          rC=rC,
          simm16=simm16,
        )
      ),
      label=None,
      db32=None,
    )
  }
}
class Instruction(
  val op: (/*UInt,*/ Int, String),
  val rA: Gpr,
  val rB: Gpr,
  val rC: Gpr,
  val simm16: SampleCpuExpr,
) {
  def encode(assembler: SampleCpuAssembler): Int = {
    var ret: Int = 0
    //--------
    ret = ret | op._1
    //printf(
    //  "%X\n", ret
    //)
    //--------
    ret = ret << SampleCpuInstrEnc.gprIdxWidth
    ret = ret | rA.index
    //printf(
    //  "%X\n", ret
    //)
    //--------
    ret = ret << SampleCpuInstrEnc.gprIdxWidth
    ret = ret | rB.index
    //printf(
    //  "%X\n", ret
    //)
    //--------
    ret = ret << SampleCpuInstrEnc.gprIdxWidth
    ret = ret | rC.index
    //printf(
    //  "%X\n", ret
    //)
    //println(
    //  s"${SampleCpuInstrEnc.opWidth} ${SampleCpuInstrEnc.gprIdxWidth}"
    //)
    //--------
    ret = ret << SampleCpuInstrEnc.simmWidth
    ret = ret | {
      val temp = simm16.evaluate(assembler=assembler)
      assert(
        temp.toShort.toInt == temp.toInt,
        (
          s"Statement_Number=${assembler._stmtNum}: "
          + "\""
          + s"${op._2} r${rA.index}, r${rB.index}, r${rC.index}, "
          + s"simm16:${temp}"
          + "\": "
          + s"(at pc ${assembler._pc}) "
          + s"simm16:${temp} out of range of signed 16-bit immediate"
        )
      )
      temp.toShort.toInt & 0xffff
      //assert(
      //  (temp <= 0x7fff && temp > -0x8000)
      //)
      //temp.toShort.toInt
    }
    //println(
    //  s"ret: ${ret}"
    //)
    //printf(
    //  "%X\n", ret
    //)
    //--------
    ret
  }
}
object Label {
  implicit class LiteralBuilder(private val sc: StringContext) {
    def Lb(args: Any*): AsmStmt = {
      //var name = sc.parts.flatMap(s => s.toString)
      var name: String = ""
      for (partStr <- sc.parts.view) {
        name = name + partStr.toString
      }
      apply(name=name)
    }
    def LbR(args: Any*): SampleCpuExpr.LabRef = {
      var name: String = ""
      for (partStr <- sc.parts.view) {
        name = name + partStr.toString
      }
      LabRef(name=name)
    }
  }
  def apply(
    name: String
  ) = {
    AsmStmt(
      instr=None,
      label=Some(new Label(name=name)),
      db32=None,
    )
  }
}
class Label(
  val name: String
) {
}
object Db32 {
  def apply(
    simm16: SampleCpuExpr,
  ): AsmStmt = {
    AsmStmt(
      instr=None,
      label=None,
      db32=Some(new Db32(simm16=simm16))
    )
  }
  //def apply(
  //  simm16: Int,
  //): AsmStmt = {
  //  AsmStmt(  
  //    instr=None,
  //    label=None,
  //    db32=Some(new Db32(simm16=SampleCpuExpr.ExprInt(simm16)))
  //  )
  //}
}
class Db32(
  val simm16: SampleCpuExpr
) {
}
case class AsmStmt(
  instr: Option[Instruction],
  label: Option[Label],
  db32: Option[Db32],
) {
}
object LabRef {
  def apply(
    name: String 
  ) = {
    new SampleCpuExpr.LabRef(name=name)
  }
}
object SampleCpuExpr {
  //trait SampleCpuAsmAst extends SampleCpuAsmAst {
  //}
  //--------
  //def r0 = new Gpr(0)
  //case class Add(
  //  rA: Gpr,
  //  rB: Gpr,
  //  rC: Gpr
  //) extends SampleCpuAsmAst {
  //}
  //val tempLabel = Label(
  //  name="asdf"
  //)
  class LabRef(
    val name: String,
  ) extends SampleCpuExpr {
    private[libsnowhouse] var _value: ExprInt = null
  }
  //implicit def toExprInt(value: Int) = ExprInt(value=value)
  case object Dot // the current pc
  extends SampleCpuExpr {
    //private[libsnowhouse] var _pc: Int = 0x0
  }
  implicit class ExprInt(
    val value: Int,
  ) extends SampleCpuExpr {
  }
  //--------
  case class ExprAsUInt(
    val child: SampleCpuExpr
  ) extends SampleCpuExpr
  case class ExprAsSInt(
    val child: SampleCpuExpr
  ) extends SampleCpuExpr
  //--------
  case class ExprPlus( // left + right
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprMinus( // left - right
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  //--------
  case class ExprUnopMinus( // -value
    child: SampleCpuExpr,
  ) extends SampleCpuExpr
  //--------
  case class ExprMul( // *
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprDiv( // /
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprMod( // %
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  //--------
  case class ExprCmpEq( // ===
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprCmpNe( // =/=
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprCmpLt( // <
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprCmpGe( // >=
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprCmpGt( // >
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprCmpLe( // <=
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  //--------
  case class ExprBitAnd( // &
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprBitOr( // |
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprBitXor( // ^
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprBitInvert( // ~
    child: SampleCpuExpr,
  ) extends SampleCpuExpr
  //--------
  case class ExprBitLshift( // <<
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprBitRshift( // >>
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  //case class ExprBitAsr( // >>>
  //  left: SampleCpuAsmAst,
  //  right: SampleCpuAsmAst,
  //) extends SampleCpuAsmAst
  //--------
  case class ExprLogicAnd( // &&
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprLogicOr( // ||
    left: SampleCpuExpr,
    right: SampleCpuExpr,
  ) extends SampleCpuExpr
  case class ExprLogicInvert( // !
    child: SampleCpuExpr,
  ) extends SampleCpuExpr
  //--------
}
//class SampleCpuAssembler(
//  val instrArr: Seq[
//)
//class SampleCpuAsmAst(
//) {
//  val children = ArrayBuffer[SampleCpuAsmAst]()
//  def addChild(
//    newChild: SampleCpuAsmAst,
//  ): Unit = {
//    assert(newChild != null)
//    children += newChild
//  }
//}
//--------
case class SampleCpuAssembler(
  val stmtArr: Seq[AsmStmt],
  val outpArr: ArrayBuffer[BigInt],
) {
  //private[libsnowhouse] val _stmtArr = ArrayBuffer[AsmStmt]()
  assert(outpArr.size == 0)
  private[libsnowhouse] val _labelMap = LinkedHashMap[
    String, (Label, Int)
  ]()
  private[libsnowhouse] var _pc: Int = 0
  private[libsnowhouse] var _stmtNum: Int = 0
  //_stmtArr ++= _stmtSeq
  //--------
  // first pass: find `Label`s
  for ((stmt, idx) <- stmtArr.view.zipWithIndex) {
    //--------
    _stmtNum = idx
    //--------
    var foundInstr: Boolean = false
    var foundLabel: Boolean = false
    //--------
    stmt.instr match {
      case Some(instr) => {
        //--------
        foundInstr = true
        //--------
        //--------
        _pc += 4
      }
      case None => {
      }
    }
    //--------
    stmt.label match {
      case Some(label) => {
        //--------
        foundLabel = true
        if (foundInstr) {
          assert(false)
        }
        _labelMap += (
          label.name -> (label, _pc)
        )
        //--------
        //--------
      }
      case None => {
      }
    }
    //--------
    stmt.db32 match {
      case Some(db32) => {
        //--------
        if (foundInstr) {
          assert(false)
        }
        if (foundLabel) {
          assert(false)
        }
        //--------
        //--------
        _pc += 4
      }
      case None => {
      }
    }
    //--------
    //--------
  }
  //--------
  // second pass: `evaluate()` and `encode()`
  _pc = 0
  for ((stmt, idx) <- stmtArr.view.zipWithIndex) {
    _stmtNum = idx
    //var foundInstr: Boolean = false
    //var foundLabel: Boolean = false
    stmt.instr match {
      case Some(instr) => {
        outpArr += instr.encode(this)
        _pc += 4
      }
      case None => {
      }
    }
    stmt.label match {
      case Some(label) => {
      }
      case None => {
      }
    }
    stmt.db32 match {
      case Some(db32) => {
        outpArr += db32.simm16.evaluate(this)
        _pc += 4
      }
      case None => {
      }
    }
    //--------
    //--------
  }
  //--------
}
