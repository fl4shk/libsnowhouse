package libsnowhouse

import spinal.core._
import spinal.core.sim._
import spinal.core.formal._

object Config {
  //def spinal = SpinalConfig(
  //  targetDirectory = "hw/gen",
  //  defaultConfigForClockDomains = ClockDomainConfig(
  //    resetActiveLevel = HIGH
  //  ),
  //  onlyStdLogicVectorAtTopLevelIo = true
  //)

  def spinal = (
    SpinalConfig(
      targetDirectory="hw/gen",
      defaultConfigForClockDomains=ClockDomainConfig(
        resetActiveLevel=HIGH,
        resetKind=SYNC,
      ),
      formalAsserts=true,
    )
      //.addStandardMemBlackboxing(blackboxAllWhatsYouCan)
  )
  def sim = SimConfig.withConfig(spinal).withFstWave

  def spinalFormal = SpinalFormalConfig(
    _spinalConfig=spinal,
    _keepDebugInfo=true,
  )
}
