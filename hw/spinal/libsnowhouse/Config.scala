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
  def spinalWithFreq(
    clkRate: HertzNumber
  ) = (
    SpinalConfig(
      targetDirectory="hw/gen",
      defaultConfigForClockDomains=ClockDomainConfig(
        resetActiveLevel=HIGH,
        resetKind=SYNC,
      ),
      formalAsserts=true,
      defaultClockDomainFrequency=FixedFrequency(clkRate),
    )
      //.addStandardMemBlackboxing(blackboxAllWhatsYouCan)
  )

  def simWithFreq(
    clkRate: HertzNumber,
  ) = SimConfig.withConfig(spinalWithFreq(clkRate=clkRate)).withFstWave

  //def sim = SimConfig.withConfig(spinal).withFstWave

  def spinal = (
    SpinalConfig(
      targetDirectory="hw/gen",
      defaultConfigForClockDomains=ClockDomainConfig(
        resetActiveLevel=HIGH,
        resetKind=SYNC,
      ),
      formalAsserts=true,
      defaultClockDomainFrequency=FixedFrequency(100.0 MHz),
    )
      //.addStandardMemBlackboxing(blackboxAllWhatsYouCan)
  )
  def sim = SimConfig.withConfig(spinal).withFstWave

  def spinalFormal = SpinalFormalConfig(
    _spinalConfig=spinal,
    _keepDebugInfo=true,
  )
}
