package com.obecto.gattakka.genetics.descriptors

import scala.math.BigInt
import scala.math.BigDecimal

object DoubleGeneDescriptor {
  def apply(resolution: Int): DoubleGeneDescriptor = {
    DoubleGeneDescriptor(0, 1, resolution)
  }
}

case class DoubleGeneDescriptor(val from: Double, val to: Double, val resolution: Int = 8) extends GeneDescriptor {
  def length = resolution
  val byteLength: Int = (length.toDouble / 8).ceil.toInt
  val range: Double = to - from
  val maxUnscaledBigInt = BigInt(1) << length
  val maxUnscaledDouble = maxUnscaledBigInt.toDouble

  def apply(rnd: scala.util.Random): DoubleGene = {
    val unscaledDouble = BigInt(numbits = length, rnd = rnd).toDouble / maxUnscaledDouble
    new DoubleGene(unscaledDouble * range + from, this)
  }

  def apply(byteArray: Array[Byte]): DoubleGene = {
    val unscaledDouble = BigInt(Array[Byte](0) ++ byteArray.take(byteLength)).toDouble / maxUnscaledDouble
    new DoubleGene(unscaledDouble * range + from, this)
  }

  def apply(value: Double): DoubleGene = {
    val normalizedValue = (value  - from) % range
    new DoubleGene(normalizedValue, this)
  }
}

case class DoubleGene(val value: Double, descriptor: DoubleGeneDescriptor) extends Gene {
  def toByteArray: Array[Byte] = {
    val unscaledDouble = (value - descriptor.from) / descriptor.range * descriptor.maxUnscaledDouble
    var unpadded = BigDecimal(unscaledDouble).toBigInt.toByteArray
    if (unpadded.head == 0) {
      unpadded = unpadded.slice(1, unpadded.length)
    }
    val padding = Array[Byte]().padTo(descriptor.byteLength - unpadded.length, 0.toByte)
    padding ++ unpadded
  }

}
