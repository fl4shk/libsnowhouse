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

//trait SnowHouseCpuAsmExpr {
//}
//--------
object SnowHouseCpuAsmAstFuncs {
  def zeroExtend(
    toExtend: Int
  ): Long = {
    val temp = toExtend.toLong & ((1.toLong << 32.toLong) - 1.toLong)
    temp
  }
}
sealed trait SnowHouseCpuExpr {
  private[libsnowhouse] var _isSigned: Boolean = false
  //private[libsnowhouse] var _pc: Option[Int] = None
  //--------
  def asUInt = {
    SnowHouseCpuExpr.ExprAsUInt(child=this)
  }
  def asSInt = {
    SnowHouseCpuExpr.ExprAsSInt(child=this)
  }
  //--------
  def +(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprPlus(left=this, right=that)
  }
  def -(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprMinus(left=this, right=that)
  }
  //--------
  def unary_- = {
    SnowHouseCpuExpr.ExprUnopMinus(child=this)
  }
  //--------
  def *(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprMul(left=this, right=that)
  }
  def /(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprDiv(left=this, right=that)
  }
  def %(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprMod(left=this, right=that)
  }
  //--------
  def ===(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprCmpEq(left=this, right=that)
  }
  def =/=(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprCmpNe(left=this, right=that)
  }
  def <(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprCmpLt(left=this, right=that)
  }
  def >=(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprCmpGe(left=this, right=that)
  }
  def >(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprCmpGt(left=this, right=that)
  }
  def <=(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprCmpLe(left=this, right=that)
  }
  //--------
  def &(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprBitAnd(left=this, right=that)
  }
  def |(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprBitOr(left=this, right=that)
  }
  def ^(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprBitXor(left=this, right=that)
  }
  def ~ = {
    SnowHouseCpuExpr.ExprBitInvert(child=this)
  }
  //--------
  def <<(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprBitLshift(left=this, right=that)
  }
  def >>(that: SnowHouseCpuExpr) = {
    //if (!this._isSigned) (
    SnowHouseCpuExpr.ExprBitRshift(left=this, right=that)
    //) else (
    //  SnowHouseCpuAsmAst.ExprBitAsr(left=this, right=that)
    //)
  }
  //def >>>(that: SnowHouseCpuAsmAst) = {
  //  ExprBitAsr(left=this, right=that)
  //}
  //--------
  def &&(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprLogicAnd(left=this, right=that)
  }
  def ||(that: SnowHouseCpuExpr) = {
    SnowHouseCpuExpr.ExprLogicOr(left=this, right=that)
  }

  def unary_! : SnowHouseCpuExpr = {
    SnowHouseCpuExpr.ExprLogicInvert(child=this)
  }
  //--------
  def evaluate(
    assembler: SnowHouseCpuAssembler
  ): Int = {
    this match {
      //case SnowHouseCpuAsmAst.Gpr(
      //  index: Int
      //) => {
      //  assert(false)
      //  0
      //}
      //--------
      case label: SnowHouseCpuExpr.LabRef => {
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
      case SnowHouseCpuExpr.Dot => {
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
      case exprInt: SnowHouseCpuExpr.ExprInt => {
        exprInt.value
      }
      //--------
      case SnowHouseCpuExpr.ExprAsUInt(
        child: SnowHouseCpuExpr,
      ) => {
        child._isSigned = false
        child.evaluate(assembler)
      }
      case SnowHouseCpuExpr.ExprAsSInt(
        child: SnowHouseCpuExpr,
      ) => {
        child._isSigned = true
        child.evaluate(assembler)
      }
      case SnowHouseCpuExpr.ExprPlus( // left + right
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        left.evaluate(assembler) + right.evaluate(assembler)
      }
      case SnowHouseCpuExpr.ExprMinus( // left - right
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        left.evaluate(assembler) - right.evaluate(assembler)
      }
      //--------
      case SnowHouseCpuExpr.ExprUnopMinus( // -value
        child: SnowHouseCpuExpr,
      ) => {
        -child.evaluate(assembler)
      }
      //--------
      case SnowHouseCpuExpr.ExprMul( // *
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        left.evaluate(assembler) * right.evaluate(assembler)
      }
      case SnowHouseCpuExpr.ExprDiv( // /
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          (
            SnowHouseCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            / SnowHouseCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
          ).toInt
        ) else (
          left.evaluate(assembler) / right.evaluate(assembler)
        )
      }
      case SnowHouseCpuExpr.ExprMod( // %
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          (
            SnowHouseCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            % SnowHouseCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
          ).toInt
        ) else (
          left.evaluate(assembler) % right.evaluate(assembler)
        )
      }
      //--------
      case SnowHouseCpuExpr.ExprCmpEq( // ===
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        if (left.evaluate(assembler) == right.evaluate(assembler)) (
          1
        ) else (
          0
        )
      }
      case SnowHouseCpuExpr.ExprCmpNe( // =/=
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        if (left.evaluate(assembler) != right.evaluate(assembler)) (
          1
        ) else (
          0
        )
      }
      case SnowHouseCpuExpr.ExprCmpLt( // <
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          if (
            SnowHouseCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            < SnowHouseCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
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
      case SnowHouseCpuExpr.ExprCmpGe( // >=
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          if (
            SnowHouseCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            >= SnowHouseCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
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
      case SnowHouseCpuExpr.ExprCmpGt( // >
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          if (
            SnowHouseCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            > SnowHouseCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
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
      case SnowHouseCpuExpr.ExprCmpLe( // <=
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        if (!left._isSigned && !right._isSigned) (
          if (
            SnowHouseCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            <= SnowHouseCpuAsmAstFuncs.zeroExtend(right.evaluate(assembler))
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
      case SnowHouseCpuExpr.ExprBitAnd( // &
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        left.evaluate(assembler) & right.evaluate(assembler)
      }
      case SnowHouseCpuExpr.ExprBitOr( // |
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        left.evaluate(assembler) | right.evaluate(assembler)
      }
      case SnowHouseCpuExpr.ExprBitXor( // ^
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        left.evaluate(assembler) ^ right.evaluate(assembler)
      }
      case SnowHouseCpuExpr.ExprBitInvert( // ~
        child: SnowHouseCpuExpr,
      ) => {
        ~child.evaluate(assembler)
      }
      //--------
      case SnowHouseCpuExpr.ExprBitLshift( // <<
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        left.evaluate(assembler) << right.evaluate(assembler)
      }
      case SnowHouseCpuExpr.ExprBitRshift( // >>
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
      ) => {
        if (!left._isSigned) (
          (
            SnowHouseCpuAsmAstFuncs.zeroExtend(left.evaluate(assembler))
            >> right.evaluate(assembler).toLong
          ).toInt
        ) else (
          left.evaluate(assembler) >> right.evaluate(assembler)
        )
      }
      //case SnowHouseCpuAsmAst.ExprBitAsr( // >>>
      //  left: SnowHouseCpuAsmAst,
      //  right: SnowHouseCpuAsmAst,
      //) => {
      //  //val temp: Long = left.evaluate(assembler)
      //  left.evaluate(assembler) >> right.evaluate(assembler)
      //}
      //--------
      case SnowHouseCpuExpr.ExprLogicAnd( // &&
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
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
      case SnowHouseCpuExpr.ExprLogicOr( // ||
        left: SnowHouseCpuExpr,
        right: SnowHouseCpuExpr,
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
      case SnowHouseCpuExpr.ExprLogicInvert( // !
        child: SnowHouseCpuExpr,
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
object SnowHouseCpuRegs {
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
  val pc = RegPc()
}
//--------
object add {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.AddRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=0,
    )
  }
  def apply(
    rA: Gpr,
    rB: Gpr,
    imm16: SnowHouseCpuExpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.AddRaRbSimm16,
      rA=rA,
      rB=rB,
      rC=SnowHouseCpuRegs.r0,
      imm16=imm16,
    )
  }
  def apply(
    rA: Gpr,
    pc: RegPc,
    imm16: SnowHouseCpuExpr
  ) = {
    Instruction(
      op=SnowHouseCpuOp.AddRaPcSimm16,
      rA=rA,
      rB=SnowHouseCpuRegs.r0,
      rC=SnowHouseCpuRegs.r0,
      imm16=imm16,
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
      op=SnowHouseCpuOp.SubRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=0,
    )
  }
  def apply(
    rA: Gpr,
    rB: Gpr,
    //rC: Gpr,
    imm16: SnowHouseCpuExpr
  ) = {
    Instruction(
      op=SnowHouseCpuOp.AddRaRbSimm16,
      rA=rA,
      rB=rB,
      rC=SnowHouseCpuRegs.r0,
      imm16=(-imm16),
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
      op=SnowHouseCpuOp.SltuRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=SnowHouseCpuOp.SltuRaRbRc._2,
    )
  }
  //def apply(
  //  rA: Gpr,
  //  rB: Gpr,
  //  //rC: Gpr,
  //  imm16: SnowHouseCpuExpr
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.Slt,
  //    rA=rA,
  //    rB=rB,
  //    rC=SnowHouseCpuRegs.r0,
  //    imm16=imm16,
  //  )
  //}
}
object slts {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.SltsRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=SnowHouseCpuOp.SltsRaRbRc._2,
    )
  }
  //def apply(
  //  rA: Gpr,
  //  rB: Gpr,
  //  //rC: Gpr,
  //  imm16: SnowHouseCpuExpr
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.Slt,
  //    rA=rA,
  //    rB=rB,
  //    rC=SnowHouseCpuRegs.r0,
  //    imm16=imm16,
  //  )
  //}
}
object and {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.AndRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=0,
    )
  }
  //def apply(
  //  rA: Gpr,
  //  rB: Gpr,
  //  //rC: Gpr,
  //  imm16: SnowHouseCpuExpr
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.And,
  //    rA=rA,
  //    rB=rB,
  //    rC=SnowHouseCpuRegs.r0,
  //    imm16=imm16,
  //  )
  //}
}
object orr {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.OrrRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=0,
    )
  }
  def apply(
    rA: Gpr,
    rB: Gpr,
    //rC: Gpr,
    imm16: SnowHouseCpuExpr
  ) = {
    Instruction(
      op=SnowHouseCpuOp.OrrRaRbSimm16,
      rA=rA,
      rB=rB,
      rC=SnowHouseCpuRegs.r0,
      imm16=imm16,
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
      op=SnowHouseCpuOp.XorRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=0,
    )
  }
  def apply(
    rA: Gpr,
    rB: Gpr,
    //rC: Gpr,
    imm16: SnowHouseCpuExpr
  ) = {
    Instruction(
      op=SnowHouseCpuOp.XorRaRbSimm16,
      rA=rA,
      rB=rB,
      rC=SnowHouseCpuRegs.r0,
      imm16=imm16,
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
      op=SnowHouseCpuOp.LslRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=SnowHouseCpuOp.LslRaRbRc._2,
    )
  }
  //def apply(
  //  rA: Gpr,
  //  rB: Gpr,
  //  //rC: Gpr,
  //  imm16: SnowHouseCpuExpr
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.Shift,
  //    rA=rA,
  //    rB=rB,
  //    rC=SnowHouseCpuRegs.r0,
  //    imm16=imm16,
  //  )
  //}
}
object lsr {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.LsrRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=SnowHouseCpuOp.LsrRaRbRc._2,
    )
  }
}
object asr {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.AsrRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=SnowHouseCpuOp.AsrRaRbRc._2,
    )
  }
}
object mul {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr
  ) = {
    Instruction(
      op=SnowHouseCpuOp.MulRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=SnowHouseCpuOp.MulRaRbRc._2,
    )
  }
  //def apply(
  //  rA: Gpr,
  //  rB: Gpr,
  //  //rC: Gpr,
  //  imm16: SnowHouseCpuExpr
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.MulRaRbRc,
  //    rA=rA,
  //    rB=rB,
  //    rC=SnowHouseCpuRegs.r0,
  //    imm16=imm16,
  //  )
  //}
}
object bz {
  def apply(
    rA: Gpr,
    //rB: SnowHouseCpuAsmAst.Gpr,
    //rC: SnowHouseCpuAsmAst.Gpr,
    imm16: SnowHouseCpuExpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.BzRaSimm,
      rA=rA,
      rB=SnowHouseCpuRegs.r0,
      rC=Gpr(SnowHouseCpuOp.BzRaSimm._2),
      imm16=(
        imm16 - SnowHouseCpuExpr.Dot - 4
      ),
    )
  }
  //def apply(
  //  rA: Gpr,
  //  //rB: SnowHouseCpuAsmAst.Gpr,
  //  //rC: SnowHouseCpuAsmAst.Gpr,
  //  imm16: Int,
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.BzRaSimm,
  //    rA=rA,
  //    rB=SnowHouseRegs.r0,
  //    rC=SnowHouseRegs.r0,
  //    imm16=SnowHouseCpuExpr.ExprInt(imm16),
  //  )
  //}
}
object bnz {
  def apply(
    rA: Gpr,
    //rB: SnowHouseCpuAsmAst.Gpr,
    //rC: SnowHouseCpuAsmAst.Gpr,
    imm16: SnowHouseCpuExpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.BnzRaSimm,
      rA=rA,
      rB=SnowHouseCpuRegs.r0,
      rC=Gpr(SnowHouseCpuOp.BnzRaSimm._2),
      imm16=(
        //imm16,
        imm16 - SnowHouseCpuExpr.Dot - 4
      ),
    )
  }
  //def apply(
  //  rA: Gpr,
  //  //rB: SnowHouseCpuAsmAst.Gpr,
  //  //rC: SnowHouseCpuAsmAst.Gpr,
  //  imm16: Int,
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.BnzRaSimm,
  //    rA=rA,
  //    rB=SnowHouseRegs.r0,
  //    rC=SnowHouseRegs.r0,
  //    imm16=SnowHouseCpuExpr.ExprInt(imm16),
  //  )
  //}
}
object jmp {
  def apply(
    rA: Gpr
  ) = {
    Instruction(
      op=SnowHouseCpuOp.JmpRa,
      rA=rA,
      rB=SnowHouseCpuRegs.r0,
      rC=Gpr(SnowHouseCpuOp.JmpRa._2),
      imm16=0x0,
    )
  }
}
object ldr {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
    //imm16: SnowHouseCpuExpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.LdrRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=0x0,
    )
  }
  def apply(
    rA: Gpr,
    rB: Gpr,
    imm16: SnowHouseCpuExpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.LdrRaRbSimm16,
      rA=rA,
      rB=rB,
      rC=SnowHouseCpuRegs.r0,
      imm16=imm16,
    )
  }
  //def apply(
  //  rA: Gpr,
  //  rB: Gpr,
  //  imm16: Int
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.LdrRaRbSimm,
  //    rA=rA,
  //    rB=rB,
  //    rC=SnowHouseRegs.r0,
  //    imm16=SnowHouseCpuExpr.ExprInt(imm16),
  //  )
  //}
}
object str {
  def apply(
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
    //imm16: SnowHouseCpuExpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.StrRaRbRc,
      rA=rA,
      rB=rB,
      rC=rC,
      imm16=0x0,
    )
  }
  def apply(
    rA: Gpr,
    rB: Gpr,
    imm16: SnowHouseCpuExpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.StrRaRbSimm16,
      rA=rA,
      rB=rB,
      rC=SnowHouseCpuRegs.r0,
      imm16=imm16,
    )
  }
  //def apply(
  //  rA: Gpr,
  //  rB: Gpr,
  //  imm16: Int
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.StrRaRbSimm,
  //    rA=rA,
  //    rB=rB,
  //    rC=SnowHouseRegs.r0,
  //    imm16=SnowHouseCpuExpr.ExprInt(imm16),
  //  )
  //}
}
object cpy {
  def apply(
    rA: Gpr,
    rB: Gpr,
  ) = {
    add(
      rA=rA,
      rB=rB,
      rC=SnowHouseCpuRegs.r0,
    )
    //Instruction(
    //  op=SnowHouseCpuOp.AddRaRbRc,
    //  rA=rA,
    //  rB=rB,
    //  rC=Gpr(SnowHouseCpuOp.CpyRaRb._2),
    //  imm16=0x0,
    //)
  }
  def apply(
    rA: Gpr,
    imm16: SnowHouseCpuExpr,
  ) = {
    add(
      rA=rA,
      rB=SnowHouseCpuRegs.r0,
      imm16=imm16,
    )
    //Instruction(
    //  op=SnowHouseCpuOp.CpyRaSimm16,
    //  rA=rA,
    //  rB=SnowHouseCpuRegs.r0,
    //  rC=Gpr(SnowHouseCpuOp.CpyRaSimm16._2),
    //  imm16=imm16
    //)
  }
  //def apply(
  //  rA: Gpr,
  //  imm16: Int
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.CpyiRaSimm,
  //    rA=rA,
  //    rB=SnowHouseRegs.r0,
  //    rC=SnowHouseRegs.r0,
  //    imm16=SnowHouseCpuExpr.ExprInt(imm16),
  //  )
  //}
}
object cpyu {
  def apply(
    rA: Gpr,
    rB: Gpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.CpyuRaRb,
      rA=rA,
      rB=rB,
      rC=Gpr(SnowHouseCpuOp.CpyuRaRb._2),
      imm16=0x0
    )
  }
  def apply(
    rA: Gpr,
    imm16: SnowHouseCpuExpr,
  ) = {
    Instruction(
      op=SnowHouseCpuOp.CpyuRaSimm16,
      rA=rA,
      rB=SnowHouseCpuRegs.r0,
      rC=Gpr(SnowHouseCpuOp.CpyuRaSimm16._2),
      imm16=imm16
    )
  }
  //def apply(
  //  rA: Gpr,
  //  imm16: Int
  //) = {
  //  Instruction(
  //    op=SnowHouseCpuOp.CpyuiRaSimm,
  //    rA=rA,
  //    rB=SnowHouseRegs.r0,
  //    rC=SnowHouseRegs.r0,
  //    imm16=SnowHouseCpuExpr.ExprInt(imm16),
  //  )
  //}
}
//object cpy {
//  def apply(
//    rA: Gpr,
//    rB: Gpr,
//  ) = {
//    Instruction(
//      op=SnowHouseCpuOp.Cpy
//    )
//  }
//}
//object MyTest {
//  val asdf = bnz(r0, 3)
//}
case class RegPc(
) {
}
case class Gpr(
  val index: Int=0
) /*extends SnowHouseCpuAsmAst*/ {
  //println(
  //  s"${index} ${SnowHouseCpuParams.numGprs}: "
  //  + s"${index < SnowHouseCpuParams.numGprs}"
  //)
  assert(
    index >= 0,
    s"${index}",
  )
  assert(
    index < SnowHouseCpuInstrEnc.numGprs,
    s"${index}",
  )
}
object Instruction {
  def apply(
    op: (/*UInt,*/ Int, Int, String),
    rA: Gpr,
    rB: Gpr,
    rC: Gpr,
    imm16: SnowHouseCpuExpr,
  ) = {
    AsmStmt(
      instr=Some(
        new Instruction(
          op=op,
          rA=rA,
          rB=rB,
          rC=rC,
          imm16=imm16,
        )
      ),
      label=None,
      db32=None,
    )
  }
}
class Instruction(
  val op: (/*UInt,*/ Int, Int, String),
  val rA: Gpr,
  val rB: Gpr,
  val rC: Gpr,
  val imm16: SnowHouseCpuExpr,
) {
  def encode(assembler: SnowHouseCpuAssembler): Long = {
    var ret: Long = 0
    //--------
    ret = ret | op._1
    //printf(
    //  "%X\n", ret
    //)
    //--------
    ret = ret << SnowHouseCpuInstrEnc.gprIdxWidth
    ret = ret | rA.index
    //printf(
    //  "%X\n", ret
    //)
    //--------
    ret = ret << SnowHouseCpuInstrEnc.gprIdxWidth
    ret = ret | rB.index
    //printf(
    //  "%X\n", ret
    //)
    //--------
    ret = ret << SnowHouseCpuInstrEnc.gprIdxWidth
    ret = ret | rC.index
    //printf(
    //  "%X\n", ret
    //)
    //println(
    //  s"${SnowHouseCpuInstrEnc.opWidth} ${SnowHouseCpuInstrEnc.gprIdxWidth}"
    //)
    //--------
    ret = ret << SnowHouseCpuInstrEnc.simmWidth
    ret = ret | {
      val temp = imm16.evaluate(assembler=assembler)
      assert(
        temp.toShort.toInt == temp.toInt,
        (
          s"Statement_Number=${assembler._stmtNum}: "
          + "\""
          + s"${op._3} r${rA.index}, r${rB.index}, r${rC.index}, "
          + s"imm16:${temp}"
          + "\": "
          + s"(at pc ${assembler._pc}) "
          + s"imm16:${temp} out of range of signed 16-bit immediate"
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
    def LbR(args: Any*): SnowHouseCpuExpr.LabRef = {
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
    imm16: SnowHouseCpuExpr,
  ): AsmStmt = {
    AsmStmt(
      instr=None,
      label=None,
      db32=Some(new Db32(imm16=imm16))
    )
  }
  //def apply(
  //  imm16: Int,
  //): AsmStmt = {
  //  AsmStmt(  
  //    instr=None,
  //    label=None,
  //    db32=Some(new Db32(imm16=SnowHouseCpuExpr.ExprInt(imm16)))
  //  )
  //}
}
class Db32(
  val imm16: SnowHouseCpuExpr
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
    new SnowHouseCpuExpr.LabRef(name=name)
  }
}
object SnowHouseCpuExpr {
  //trait SnowHouseCpuAsmAst extends SnowHouseCpuAsmAst {
  //}
  //--------
  //def r0 = new Gpr(0)
  //case class Add(
  //  rA: Gpr,
  //  rB: Gpr,
  //  rC: Gpr
  //) extends SnowHouseCpuAsmAst {
  //}
  //val tempLabel = Label(
  //  name="asdf"
  //)
  class LabRef(
    val name: String,
  ) extends SnowHouseCpuExpr {
    private[libsnowhouse] var _value: ExprInt = null
  }
  //implicit def toExprInt(value: Int) = ExprInt(value=value)
  case object Dot // the current pc
  extends SnowHouseCpuExpr {
    //private[libsnowhouse] var _pc: Int = 0x0
  }
  implicit class ExprInt(
    val value: Int,
  ) extends SnowHouseCpuExpr {
  }
  //--------
  case class ExprAsUInt(
    val child: SnowHouseCpuExpr
  ) extends SnowHouseCpuExpr
  case class ExprAsSInt(
    val child: SnowHouseCpuExpr
  ) extends SnowHouseCpuExpr
  //--------
  case class ExprPlus( // left + right
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprMinus( // left - right
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  //--------
  case class ExprUnopMinus( // -value
    child: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  //--------
  case class ExprMul( // *
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprDiv( // /
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprMod( // %
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  //--------
  case class ExprCmpEq( // ===
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprCmpNe( // =/=
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprCmpLt( // <
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprCmpGe( // >=
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprCmpGt( // >
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprCmpLe( // <=
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  //--------
  case class ExprBitAnd( // &
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprBitOr( // |
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprBitXor( // ^
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprBitInvert( // ~
    child: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  //--------
  case class ExprBitLshift( // <<
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprBitRshift( // >>
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  //case class ExprBitAsr( // >>>
  //  left: SnowHouseCpuAsmAst,
  //  right: SnowHouseCpuAsmAst,
  //) extends SnowHouseCpuAsmAst
  //--------
  case class ExprLogicAnd( // &&
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprLogicOr( // ||
    left: SnowHouseCpuExpr,
    right: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  case class ExprLogicInvert( // !
    child: SnowHouseCpuExpr,
  ) extends SnowHouseCpuExpr
  //--------
}
//class SnowHouseCpuAssembler(
//  val instrArr: Seq[
//)
//class SnowHouseCpuAsmAst(
//) {
//  val children = ArrayBuffer[SnowHouseCpuAsmAst]()
//  def addChild(
//    newChild: SnowHouseCpuAsmAst,
//  ): Unit = {
//    assert(newChild != null)
//    children += newChild
//  }
//}
//--------
case class SnowHouseCpuAssembler(
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
        outpArr += db32.imm16.evaluate(this)
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
