package xnornet4fpga
import chisel3._
import chisel3.util._

/**
  * Created by anranw on 6/4/17.
  */
class LayerParamShifter(hwConfig:HardwareConfig, topo:NNTopology) extends Module {
  def round(x: Int, k: Int) = (x - 1) / k * k + k

  val currentLayer = RegInit(0.U(4.W))
  val featureCnts = (0 until topo.getTotalLayers()) map {
    i => round(topo(i)._2, hwConfig.XNORFanout).U(16.W)
  } map {RegInit(_)}
  val actualFeatureCnts=(0 until topo.getTotalLayers()) map {
    i => topo(i)._2.U(16.W)
  } map {RegInit(_)}
  val featureCnts65536 = (0 until topo.getTotalLayers()) map {
    i => (65536 / round(topo(i)._2, hwConfig.XNORFanout)).U(16.W)
  } map {RegInit(_)}
  val accWidth = (0 until topo.getTotalLayers()) map {
    i => (round(topo(i)._2, hwConfig.XNORFanout) / hwConfig.XNORFanout).U(16.W)
  } map {RegInit(_)}
  //how many input shift does current layer take
  val inputRound = (0 until topo.getTotalLayers()) map {
    i => (round(topo(i)._1, hwConfig.XNORBitWidth) / hwConfig.XNORBitWidth).U(16.W)
  } map {RegInit(_)}
  //total ticks current layer take
  val totalRound = (0 until topo.getTotalLayers()) map {
    i => ((round(topo(i)._1, hwConfig.XNORBitWidth) / hwConfig.XNORBitWidth) *
      (round(topo(i)._2, hwConfig.XNORFanout) / hwConfig.XNORFanout)).U(16.W)
  } map {RegInit(_)}

  val io = IO(new Bundle {
    val shift = Input(Bool())
    val currentLayer = Output(UInt(4.W))
    val currentFeatureCnt = Output(UInt(16.W))
    val actualFeatureCnt=Output(UInt(16.W))
    val currentFeatureCnt65536 = Output(UInt(16.W))
    val currentInputRound = Output(UInt(16.W))
    val currentTotalRound = Output(UInt(16.W))
    val currentAccWidth=Output(UInt(16.W))
    val lastLayer=Output(Bool())
  })

  io.currentLayer:=currentLayer
  io.currentFeatureCnt:=featureCnts(0)
  io.actualFeatureCnt:=actualFeatureCnts(0)
  io.currentFeatureCnt65536:=featureCnts65536(0)
  io.currentInputRound:=inputRound(0)
  io.currentTotalRound:=totalRound(0)
  io.currentAccWidth:=accWidth(0)
  io.lastLayer:=(currentLayer===(topo.getTotalLayers()-1).U)

  when(io.shift) {
    for (i <- 0 until topo.getTotalLayers()) {
      featureCnts(i) := featureCnts((i + 1) % topo.getTotalLayers())
      actualFeatureCnts(i) := actualFeatureCnts((i + 1) % topo.getTotalLayers())
      featureCnts65536(i) := featureCnts65536((i + 1) % topo.getTotalLayers())
      accWidth(i):=accWidth((i+1)%topo.getTotalLayers())
      inputRound(i):=inputRound((i+1)%topo.getTotalLayers())
      totalRound(i):=totalRound((i+1)%topo.getTotalLayers())
      when(currentLayer===(topo.getTotalLayers()-1).U) {
        currentLayer := 0.U
      } otherwise {
        currentLayer:=currentLayer+1.U
      }
    }
  }
}
