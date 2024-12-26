# Lib Snow House

## Initial Notes:

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

4. I already have an integer divider that takes generics (in
  libcheesevoyage)
