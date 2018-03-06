package com.obecto.gattakka.genetics.descriptors

import scala.math.BigInt

object LongGeneDescriptor {
  def apply(bits: Int): LongGeneDescriptor = {
    LongGeneDescriptor(0, 1l << bits)
  }
}

case class LongGeneDescriptor(val from: Long, val to: Long) extends GeneDescriptor {
  assert(to >= from)
  val range: Long = to - from + 1
  val length: Int = (Math.log(range.toDouble) / Math.log(2)).ceil.toInt

  def apply(rnd: scala.util.Random): LongGene = {
    new LongGene(rnd.nextLong().abs % range + from, this)
  }

  def apply(byteArray: Array[Byte]): LongGene = {
    new LongGene(BigInt(Array[Byte](0) ++ byteArray.take(byteLength)).toLong % range + from, this)
  }

  def apply(value: Long): LongGene = {
    val normalizedValue = (value  - from) % range
    new LongGene(normalizedValue, this)
  }
}

case class LongGene(val value: Long, descriptor: LongGeneDescriptor) extends Gene {
  def toByteArray: Array[Byte] = {
    var unpadded = BigInt(value - descriptor.from).toByteArray
    if (unpadded.head == 0) {
      unpadded = unpadded.slice(1, unpadded.length)
    }
    val padding = Array[Byte]().padTo(descriptor.byteLength - unpadded.length, 0.toByte)
    padding ++ unpadded
  }

}
