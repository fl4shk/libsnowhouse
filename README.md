# Lib Snow House

1. This is a SpinalHDL library that primarily exists for developing
  RISC (or RISC-like) CPUs with strictly-in-order pipeline structures.
2. The primary inputs to this library for creating a new CPU are as
  follows:
  * A data structure indicating the kinds of instructions that your CPU
    supports.
  * A Scala function implementing the SpinalHDL code for your CPU's
    specific instruction decoder (though not all aspects of the Instruction
    Decode pipeline stage need to be implemented).
3. NOTE: There is no support implemented yet for virtual memory at the time
  of this writing, but in theory a user of `libsnowhouse` could implement
  **most** of the support needed for that through the instruction/data
  memory-access "bus"es/"port"s of a CPU implemented via this library.

4. There is a sample CPU that has been developed with the library, called
  the `SnowHouseCpu`. This CPU, given a, is able to reach clock rates
  around 140 MHz in a Speed Grade -1, Xilinx Artix-7 FPGA, at least in a
  version of this CPU that lacks support for any kind of interrupt.
  * This CPU's implementation, besides the generic parts of
    `libsnowhouse`, lives mostly within this source file:
    `hw/gen/libsnowhouse/snowHouseCpuMod.scala`.
  * Other CPUs can be modeled in a similar way.
  * A GNU Binutils port has been written for `SnowHouseCpu`:
    [binutils-gdb snowhousecpu branch](https://github.com/fl4shk/binutils-gdb/tree/snowhousecpu)
  * A GCC port has been written for `SnowHouseCpu`:
    [gcc snowhousecpu branch](https://github.com/fl4shk/gcc/tree/snowhousecpu)
  * A Picolibc port has been written for `SnowHouseCpu`:
    [picolibc snowhouse branch](https://github.com/fl4shk/picolibc/tree/snowhousecpu)
  * This command will simulate a test program running on the CPU if you
    have `Verilator` installed (you also probably want `gtkwave` or
    `surfer` to view the simulation output):
    * `sbt "runMain libsnowhouse.SnowHouseCpuWithDualRamSim"`

## *OLD STUFF*: Initial Notes:

1. It'll always be the same strictly-in-order pipeline structure, and most
  likely some variant of the Classic RISC Pipeline (which you can see on
  Wikipedia).

1. I'd have (in the SpinalHDL code) configuration things like this (though
  this is a non-exhaustive list):
  * single-cycle ALU-type instructions (which you could pass in as a Scala
    function that does the operation)
  * whether the CPU has interrupts (some kinds of CPUs might not need them)
  * (this will come later) whether the CPU has virtual memory/supervisor
    mode
  * multiply/divide/mod/divmod instructions
  * load/store instructions
  * kinds of jumps/branches/calls
  * general-purpose registers' width/primary data/address width (I might
    restrict these to have all be the same)
  * Number of general-purpose registers
  * SIMD instructions/registers.
    * Block loads/stores into/out of SIMD registers?

3. Probably it'd be the case that stalling is implemented the same way each
  time, but I could easily adjust the number and kinds of instructions this
  way

3. I already have an integer divider that takes generics (in
  libcheesevoyage)
