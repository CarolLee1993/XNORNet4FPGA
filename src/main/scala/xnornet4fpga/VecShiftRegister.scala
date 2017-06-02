// See LICENSE.txt for license details.
package xnornet4fpga

import chisel3._

// Problem:
//
// Implement a parametrized simple shift register.
// 'n' is the number of elements in the shift register.
// 'w' is the width of one element.

class VecShiftRegister(val n: Int, val w: Int) extends Module {
  val io = IO(new Bundle {
    val in  = Input(UInt(w.W))
    val out = Output(UInt(w.W))
    val shift=Input(Bool())
  })

  val delays = Vec(n, RegInit(0.S(w.W)))

  when(io.shift) {
    for (i <- n - 1 to 1 by -1) {
      delays(i) := delays(i - 1)
    }
    delays(0) := io.in
  }

  io.out := delays(n-1)
}
